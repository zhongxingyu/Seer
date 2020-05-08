 package com.radicaldynamic.groupinform.tasks;
 
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.ektorp.Attachment;
 import org.ektorp.DbAccessException;
 import org.supercsv.io.CsvListReader;
 import org.supercsv.io.ICsvListReader;
 import org.supercsv.prefs.CsvPreference;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.mycila.xmltool.XMLDoc;
 import com.mycila.xmltool.XMLDocBuilder;
 import com.mycila.xmltool.XMLTag;
 import com.radicaldynamic.gcmobile.android.activities.DataImportActivity;
 import com.radicaldynamic.groupinform.application.Collect;
 import com.radicaldynamic.groupinform.documents.FormInstance;
 import com.radicaldynamic.groupinform.listeners.DataImportListener;
 import com.radicaldynamic.groupinform.logic.AccountDevice;
 import com.radicaldynamic.groupinform.utilities.Base64Coder;
 import com.radicaldynamic.groupinform.xform.Field;
 import com.radicaldynamic.groupinform.xform.FormReader;
 import com.radicaldynamic.groupinform.xform.FormWriter;
 import com.radicaldynamic.groupinform.xform.Instance;
 import com.radicaldynamic.groupinform.xform.XForm;
 
 //public class DataImport extends AsyncTask<Params, Progress, Result> 
 public class DataImportTask extends AsyncTask<Void, Void, ArrayList<List<String>>> 
 {
     private static final String t = "DataImportTask: ";
     
     private DataImportListener mStateListener;
     
     private String mImportFilePath;
     private String mImportMsg;
     private int mImportMode = -1;
     private boolean mImportSuccessful = false;
     
     private String mFormDefinitionId;
         
     // Import options
     private Bundle mImportOptions;
     private Bundle mFormSetup;
        
     private FormReader mFormReader;
     private XMLTag mInstanceXML;
     
     private Map<String, Integer> mFieldImportMap = new HashMap<String, Integer>();
    
     @Override
     protected ArrayList<List<String>> doInBackground(Void... arg0) 
     {
         final String tt = t + "doInBackground(): ";
         
         ArrayList<List<String>> records = new ArrayList<List<String>>();
         List<String> line;
         
         try {
             ICsvListReader inFile = new CsvListReader(new FileReader(mImportFilePath), CsvPreference.EXCEL_PREFERENCE);
             
             // If the user doesn't want to import the first line, skip it and discard
             if (mImportOptions.getBoolean(DataImportActivity.KEY_IMPORT_OPTION_SFR, false)) {
                 inFile.getCSVHeader(true);
             }
 
             switch (mImportMode) {
             case DataImportListener.MODE_PREVIEW:
                 while ((line = inFile.read()) != null) {
                     if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "previewing line " + inFile.getLineNumber() + ": " + line.toString());                    
                     records.add(new ArrayList<String>(line));
                     
                     // Only read a few lines in
                     if (inFile.getLineNumber() > 4)
                         break;
                 }
                 
                 break;
                 
             case DataImportListener.MODE_VERIFY:
                 List<String> allEmailAddresses = new ArrayList<String>(); 
                 
                 // Gather list of email addresses associated with active or unused device profiles
                 for (AccountDevice device : Collect.getInstance().getInformOnlineState().getAccountDevices().values()
                         .toArray(new AccountDevice[Collect.getInstance().getInformOnlineState().getAccountDevices().values().size()])) {
                     
                     if (device.getStatus().contains("active") || device.getStatus().contains("unused"))
                         allEmailAddresses.add(device.getEmail());
                 }
                 
                 while ((line = inFile.read()) != null) {
                     if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "verifying line " + inFile.getLineNumber() + ": " + line.toString());
                     
                     // Verify form assignment
                     if (mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_ASSIGNMENT, 0) > 0) {
                         // 1, 2, 3, ...
                         int column = mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_ASSIGNMENT, 0); 
                         column--;
                         
                         if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "verify assignment from column " + (column + 1) + " (" + line.get(column) + ")");
                         
                         // Split up potential device profile identifiers
                         String [] emailAddresses = line.get(column).split("\\s+");
                         
                         for (int i = 0; i < emailAddresses.length; i++) {
                             String address = emailAddresses[i].trim().toLowerCase();
                             
                             if (!allEmailAddresses.contains(address)) {
                                 mImportMsg = startErrorMsg(inFile.getLineNumber(), column + 1) + address + " does not belong to a device profile in this account.\n\nPlease check that your import file and new form setup options are correct and try again.";
                                 return null;
                             }
                         }
                     }
                     
                     // Verify form status (be liberal)
                     if (mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_STATUS, 0) > 1) {
                         int column = mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_STATUS, 0); 
                         column = column - 2;
                         
                         if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "verify form status from column " + (column + 1) + " (" + line.get(column) + ")");
                         
                         String status = line.get(column).trim().toLowerCase();
                         
                         if (!status.equals("draft") 
                                 && !status.equals("incomplete") 
                                 && !status.equals("complete") 
                                 && !status.equals("completed")) {
                             mImportMsg = startErrorMsg(inFile.getLineNumber(), column + 1) + "\"" + line.get(column).trim() + "\" is not a valid form status.  Expected draft or complete.\n\nPlease check that your import file and new form setup options are correct and try again.";
                             return null;
                         }
                     }
                     
                     // Verify field-to-column import mappings
                     for (String location : mFieldImportMap.keySet().toArray(new String[mFieldImportMap.keySet().size()])) {
                         // Value from CSV file to test against
                         Integer column = mFieldImportMap.get(location);
                         
                         if (column > 0) {
                             column--;
 
                             // Field (with information that tells us what values it expects)
                             Field f = mFormReader.getFlatFieldIndex().get(location);
 
                             if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "verify field-to-column mapping " + (column + 1) + " (" + line.get(column) + ") for " + f.getType() + "." + f.getBind().getType());
                         }
                     }
                 }
                 
                 break;
                 
             case DataImportListener.MODE_IMPORT:                
                 Map<String, String> emailProfileIdMap = new HashMap<String, String>();
                 
                 // Gather list of email addresses associated with active or unused device profiles
                 for (AccountDevice device : Collect.getInstance().getInformOnlineState().getAccountDevices().values()
                         .toArray(new AccountDevice[Collect.getInstance().getInformOnlineState().getAccountDevices().values().size()])) {
                     
                     emailProfileIdMap.put(device.getEmail(), device.getId());
                 }
                 
                 while ((line = inFile.read()) != null) {
                     if (Collect.Log.VERBOSE) Log.v(Collect.LOGTAG, tt + "importing line " + inFile.getLineNumber() + ": " + line.toString());
 
                     FormInstance fi = new FormInstance();
                     fi.setFormId(mFormDefinitionId);
                     
                     // TODO: PRESERVE RECORD ORDER
                     
                     // Set form assignments
                     int assignment = mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_ASSIGNMENT, 0);
                     
                     if (assignment > 0) {
                         List<String> toAssign = new ArrayList<String>();
                         String [] emailAddresses = line.get(assignment - 1).trim().toLowerCase().split("\\s+");
                         
                         for (int i = 0; i < emailAddresses.length; i++) {
                             toAssign.add(emailProfileIdMap.get(emailAddresses[i].trim()));
                         }
                         
                         fi.setAssignedTo(toAssign);
                     }
                     
                     // Set form names
                     int name = mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_NAME, 0);
                     
                     if (name > 0) {
                         fi.setName(line.get(name - 1).trim());
                     }
                     
                     // Set form status
                     int status = mFormSetup.getInt(DataImportActivity.KEY_FORM_SETUP_STATUS, 0);
                     
                     if (status == 0) {
                         fi.setStatus(FormInstance.Status.draft);
                     } else if (status == 1) {
                         fi.setStatus(FormInstance.Status.complete);
                     } else if (status > 1) {
                         String userDefinedStatus = line.get(status - 2).trim().toLowerCase();
                         
                         if (userDefinedStatus.equals("draft") || userDefinedStatus.equals("incomplete"))
                             fi.setStatus(FormInstance.Status.draft);
                         else if (userDefinedStatus.equals("complete") || userDefinedStatus.equals("completed"))
                             fi.setStatus(FormInstance.Status.complete);                        
                     }
                     
                     // Create XForm instance
                     XMLDocBuilder instanceDoc = XMLDoc.newDocument(true);
                     mInstanceXML = instanceDoc.addRoot(mFormReader.getInstanceRoot());
                     mInstanceXML.gotoRoot().addAttribute("id", mFormReader.getInstanceRootId());
                     generateInstanceXML(null, line);
                     
                     fi.addInlineAttachment(new Attachment("xml", new String(Base64Coder.encode(mInstanceXML.toBytes())).toString(), FormWriter.CONTENT_TYPE));
                     
                     Collect.getInstance().getDbService().getDb().create(fi);
                 }
                 
                 mImportMsg = "Import complete. " + inFile.getLineNumber() + " rows processed.";
                 
                 break;
             }
             
             inFile.close();
 
             mImportSuccessful = true;
         } catch (DbAccessException e) {
             mImportMsg = "Failure while writing to database:\n\n" + e.toString();
             if (Collect.Log.ERROR) Log.e(Collect.LOGTAG, t + "error while reading CSV file for import: " + e.toString());
             e.printStackTrace();            
         } catch (Exception e) {
             mImportMsg = "Unexpected error countered:\n\n" + e.toString();
             if (Collect.Log.ERROR) Log.e(Collect.LOGTAG, t + "error while reading CSV file for import: " + e.toString());
             e.printStackTrace();
         }
         
         return records;
     }
     
     @Override
     protected void onPostExecute(ArrayList<List<String>> records)
     {   
         synchronized (this) {
             if (mStateListener != null) {
                 Bundle b = new Bundle();
                 b.putString(DataImportListener.MESSAGE, mImportMsg);
                 b.putInt(DataImportListener.MODE, mImportMode);         
                 b.putBoolean(DataImportListener.SUCCESSFUL, mImportSuccessful);                       
                 mStateListener.importTaskFinished(b, records);
             }
         }
     }
     
     public void setFieldImportMap(Map<String, Integer> map)
     {
         mFieldImportMap = map;
     }
     
     public void setFormDefinitionId(String id)
     {
         mFormDefinitionId = id;
     }
     
     public void setFormReader(FormReader fr)
     {
         mFormReader = fr;
     }
     
     public void setFormSetup(Bundle b)
     {
         mFormSetup = b;
     }        
     
     public void setImportFilePath(String filePath)
     {
         mImportFilePath = filePath;
     }
     
     public void setImportMode(int m)
     {
         mImportMode = m;
     }
 
     public void setImportOptions(Bundle b)
     {        
         mImportOptions = b;     
     }    
     
     public void setListener(DataImportListener sl) 
     {
         synchronized (this) {
             mStateListener = sl;
         }
     }
     
     private void generateInstanceXML(Instance incomingInstance, List<String> recordRow)
     {
         Iterator<Instance> instanceIterator;
         
         if (incomingInstance == null)
             instanceIterator = mFormReader.getInstance().iterator();
         else
             instanceIterator = incomingInstance.getChildren().iterator();
         
         while (instanceIterator.hasNext()) {
             Instance i = instanceIterator.next();
              
             if (i.getChildren().isEmpty()) {
                 /*
                  * For some reason unknown to me we can only call gotoParent() when adding 
                  * an empty tag.  Calling it after adding an empty tag OR a tag with a text
                  * value causes the nesting to get screwed up.  This doesn't make any sense
                  * to me.  It might be a bug with xmltool.
                  */
                 
                 // Hidden instance field OR no field import map entry
                 if (i.getField() == null || !mFieldImportMap.containsKey(i.getField().getLocation())) { 
                     mInstanceXML.addTag(i.getName());
                     mInstanceXML.gotoParent();
                 } else {
                     // If this location is recorded in the field import map it will have a corresponding column value
                     Integer column = mFieldImportMap.get(i.getField().getLocation());
                     
                     if (column > 0) {
                         // Import user defined value
                         column--;
                         mInstanceXML.addTag(i.getName()).setText(recordRow.get(column).trim());
                     } else {
                         // Use template default
                         mInstanceXML.addTag(i.getName());
                         mInstanceXML.gotoParent();                       
                     }
                 }
             } else {
                 // Likely a repeated data set
                 mInstanceXML.addTag(i.getName()).addAttribute(XForm.Attribute.JR_TEMPLATE, "");
                 generateInstanceXML(i, recordRow);
                 mInstanceXML.gotoParent();
             }
         }
     }
     
     private String startErrorMsg(int row, int column)
     {
        return "Error at row " + row + ", column "  + column + ":\n\n";
     }
 }
