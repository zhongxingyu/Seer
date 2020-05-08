 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dit;
 
 import dit.panels.MatchDataPanel;
 import java.io.File;
 import java.util.*;
 import javax.swing.table.AbstractTableModel;
 
 /**
  *
  * @author angelo
  */
 public class ManualMatchTableModel extends AbstractTableModel{
     private String[] columnNames = new String[]{"Image File", "Date File ID", "Status"};
     private Object[][] data;
     private int mismatchCount, matchCount;
 
     
     public ManualMatchTableModel() {
         mismatchCount = 0;
         matchCount = 0;
         data = new Object[0][0];
     }
     
     public ManualMatchTableModel(List<File> images, Object[] demoIDs, final String key, final boolean isbyPath,final boolean ismulti) {
         ArrayList<Object[]> dataList = new ArrayList<Object[]>();
         
    
         
         // The demoIDs will ALWAYS be sorted, images probably will not be.
         // The standard string compareTo should be very reliable.
         Collections.sort(images, new Comparator<File>(){
             @Override
             public int compare(File f1, File f2) {
                 if (key != null)
                 {
                     String s1 = filenameconvertor(key, FileUtils.getName(f1));
                     String s2 = filenameconvertor(key, FileUtils.getName(f2));
                     return s1.compareToIgnoreCase(s2);
                 }
                 else
                     return FileUtils.getName(f1).compareToIgnoreCase(FileUtils.getName(f2));
             }
         });
         
         DeidData.IdFilename = new Hashtable<String, String>();
         Iterator<File> imageIt = images.iterator();
         File curFile = null;
         try {
             curFile = imageIt.next();
         } catch (NoSuchElementException e) {
             curFile = null;
         }
         
         // int demoIDNdx = 0;
         if (!isbyPath)
         {
             Hashtable<String, String> id_exist = new Hashtable<String, String>();
             while (curFile != null){
                  String mapDisplaytoFile=fileintoTable(curFile);   //do not delete this line unless you know its function
                 int demoIDNdx = 0;
                 while(demoIDNdx < demoIDs.length){
                     
                     String filename = FileUtils.getName(curFile);
                     String convertedID;
                     if (key != null)
                     {
                         convertedID = keyconvertor(key,(String)demoIDs[demoIDNdx]);
                     }
                     else
                     {
                         convertedID = (String)demoIDs[demoIDNdx];
                     }
                     if (filename.indexOf(convertedID) >= 0) {
                     
                         int properidx = getProperindex(filename.toString(),demoIDs, key);
                         if(ismulti){
                             dataList.add(new Object[]{displayIntoTable(curFile), demoIDs[properidx], new Boolean(true)});
                             DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[properidx] );
                             matchCount++;
                             break;
                         }
                         else {
                             if(!id_exist.containsKey((String)demoIDs[properidx]))
                             {
                                 dataList.add(new Object[]{displayIntoTable(curFile).toString(), demoIDs[properidx], new Boolean(true)});
                                 DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[properidx] );
                                 id_exist.put((String)demoIDs[properidx], "True");
                                 matchCount++;
                                 break;
                             }
                             
                         }
                     }
                     demoIDNdx++;
                     
                 }
                 
                 if (demoIDNdx == demoIDs.length)
                 {
                     //dataList.add(new Object[]{fileintoTable(curFile), null, new Boolean(false)});
                     dataList.add(new Object[]{displayIntoTable(curFile).toString(), null, new Boolean(false)});
                     mismatchCount++;
                 }
                 
                 
                 
                 
                 try {
                     curFile = imageIt.next();
                 } catch (NoSuchElementException e) {
                     curFile = null;
                 }
                 
                 
             }
           
         }
         else  // search by path
         {
            
             Hashtable<String, String> id_exist = new Hashtable<String, String>();
             while (curFile != null){
                  String mapDisplaytoFile=fileintoTable(curFile);   //do not delete this line unless you know its function
                 int matchIndex=findMatchedID(curFile, demoIDs,key);
                
               //  System.out.println("Curr:"+curFile.getAbsolutePath()+" I:"+matchIndex+" C:"+(String)demoIDs[matchIndex]);
                        
                 
                 if (matchIndex >= 0 ) {
                     
                     if(ismulti){
                       
                         dataList.add(new Object[]{displayIntoTable(curFile), demoIDs[matchIndex], new Boolean(true)});
                         DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[matchIndex] );
                         matchCount++;
                     }
                     else {
                         if(!id_exist.containsKey((String)demoIDs[matchIndex]))
                         {
                             
                             dataList.add(new Object[]{displayIntoTable(curFile), demoIDs[matchIndex], new Boolean(true)});
                           
                             DeidData.IdFilename.put(fileintoTable(curFile), (String)demoIDs[matchIndex] );
                             id_exist.put((String)demoIDs[matchIndex], "True");
                             matchCount++;                            
                         }
                         else
                         {
                             LinkedList<Object> newIds=new LinkedList<Object>(Arrays.asList(demoIDs));
                          
                             newIds.remove(matchIndex);
                             while(newIds.size()>0)
                             {
                                 matchIndex=findMatchedID(curFile, newIds.toArray(), key);
                                 
                                 if(matchIndex>= 0)
                                 {
                                     if(!id_exist.containsKey((String)newIds.get(matchIndex)) )
                                     {
                                        
                                         dataList.add(new Object[]{displayIntoTable(curFile), newIds.get(matchIndex), new Boolean(true)});
                                         DeidData.IdFilename.put(fileintoTable(curFile), (String)newIds.get(matchIndex) );
                                         id_exist.put((String)newIds.get(matchIndex), "True");
                                         matchCount++;
                                         break;
                                     }
                                     else
                                     {
                                         newIds.remove(matchIndex);
                                     }
                                 }
                                 else
                                 {
                                    break;
                                 }
                             }
                             if(newIds.size()<1)
                             {
                                 dataList.add(new Object[]{displayIntoTable(curFile), null, new Boolean(false)});
                                 mismatchCount++;
                             }
                         }
                         
                     }
                 }
                 
                 
                 if (matchIndex < 0)
                 {
                     dataList.add(new Object[]{displayIntoTable(curFile), null, new Boolean(false)});
                     mismatchCount++;
                 }
                 
                 
                 try {
                     curFile = imageIt.next();
                 } catch (NoSuchElementException e) {
                     curFile = null;
                 }
                 
                 
             }
             
         }
         Object[][] dataArray = new Object[dataList.size()][3];
         for (int ndx = 0; ndx < dataList.size(); ndx++) {
             dataArray[ndx] = dataList.get(ndx);
         }
         data = dataArray;
         DeidData.data = data;
     }
     
     
     //get a good match
     private int getProperindex(String filename,Object[] demoIDs,String key){
         int index = 0;
         int min = filename.length();
         int demoIDNdx = 0;
         
         while(demoIDNdx < demoIDs.length)
         {
             String convertedID;
             if (key != null)
             {
                 convertedID = keyconvertor(key,(String)demoIDs[demoIDNdx]);
             }
             else
             {
                 convertedID = (String)demoIDs[demoIDNdx];
             }
             if(filename.indexOf(convertedID)>=0){
                 if(filename.indexOf(convertedID) < min){
                     min = filename.indexOf(convertedID);
                     index = demoIDNdx;
                 }
                 
                 
             }
             demoIDNdx ++;
             
         }
         
         return index;
         
         
     }
     // get the number of one sub string appearing in a string
     private int getCountIndex(String str, String substr){
         int count=0;
         int index=0;
         while(true){
             index = str.indexOf(substr,index);
             if(index==-1){
                 break;
             }
             index = index + substr.length();
             count++;
         }
         return count;
     }
     // this funcion should be consistant to line 322 in AuditPanel.java
     private String fileintoTable(File file){
         String abParent = file.getParent();
         String out = FileUtils.getName(file).toString();
        if (!DeidData.parentPath.equals("none"))    {
             
            out = abParent.replaceFirst(DeidData.parentPath, "").replaceFirst(DeidData.anaPath, "").replaceFirst(DeidData.dicomPath, "").replaceAll("/", "") + out;
        }
         MatchDataPanel.displayTofile.put(displayIntoTable(file),out );
         return out;
     }
     
     public static String displayIntoTable(File file)
     {
          String out = file.getAbsolutePath();
         if (!DeidData.parentPath.equals("none"))    {
             
             out = out.replaceFirst(DeidData.parentPath, "").replaceFirst(DeidData.anaPath, "").replaceFirst(DeidData.dicomPath, "");
         }      
         
         return out;
     }
     private String keyconvertor(String key, String IDstr){
         
         if (key.indexOf("#") >= 0){
             int idlength = key.lastIndexOf('#') - key.indexOf('#') + 1;
             
             String IDstr1 = IDstr;
             if (idlength > 0){
                 while(IDstr1.length() < idlength){
                     IDstr1 = "0" + IDstr1;
                 }
             }
             
             //System.out.println(IDstr1);
             IDstr1 = key.replaceFirst("#", IDstr1);
             //System.out.println(IDstr1);
             if (IDstr1.indexOf("#") >= 0)
                 IDstr1 = IDstr1.replaceAll("#", "");
             //System.out.println(IDstr);
             return IDstr1;
         }
         else if (key.indexOf("%") >= 0){
             
             return key.replaceFirst("%", IDstr);
         }
         else return IDstr;
     }
     
     private String filenameconvertor(String key, String filename){
         
         if (key.indexOf("#") >= 0) {
             int idlength = key.lastIndexOf('#') - key.indexOf('#') + 1;
             String keywithoutid = key.substring(0, key.length() - idlength );
             //System.out.println(keywithoutid);
             int keyposition = filename.indexOf(keywithoutid);
             if (keyposition < 0) return filename;
             else{
                 //System.out.println(keyposition);
                 String convertedfilename;
                 try {
                     convertedfilename = filename.substring(keyposition, keyposition + key.length());
                     convertedfilename = convertedfilename.substring(keywithoutid.length());
                     convertedfilename = Integer.toString(Integer.parseInt(convertedfilename));
                 }
                 catch(Exception e)
                 {
                     convertedfilename = filename;
                 }
                 
                 //System.out.println(convertedfilename);
                 //System.out.println(filename);
                 
                 return convertedfilename;
             }
         }
         else return filename;
     }
     
     public int findMatchedID(File file,Object[] demoIDs,String key)
     {
         if(file ==null)
             return -1;
         if(demoIDs.length<1)
             return -1;
         
         String filepath = file.getAbsolutePath();
         
         //file parent path
         String filepp = file.getParentFile().getParentFile().getParentFile().getAbsolutePath();
         
         //file path without parent path
         String filenet = filepath.replaceAll(filepp, ""); //this line may cause problem in windows cause file path contains '\' 
         
         int bestMatchIndex=-1;
         int lcsLength=0;
         for(int i=0;i<demoIDs.length;i++)
         {
             String id=(String)demoIDs[i];
             String lcs="";
             if(key!=null)
             {
                if(filenet.contains((keyconvertor(key,(String)demoIDs[i]))))
                {
                    lcs=(keyconvertor(key,(String)demoIDs[i]));
                }
             }
             else
             {
                 if(filenet.contains((String)demoIDs[i]))
                     lcs=(String)demoIDs[i];                
             }
             if(lcs.length()>lcsLength)
             {
                 lcsLength=lcs.length();
                 bestMatchIndex=i;
             }
         }
         
         return bestMatchIndex;
     }
     
     
     @Override
     public int getRowCount() {
         return data.length;
     }
     
     @Override
     public int getColumnCount() {
         return columnNames.length;
     }
     
     @Override
     public String getColumnName(int col) {
         return columnNames[col];
     }
     
     @Override
     public Object getValueAt(int row, int col) {
         return data[row][col];
     }
     
     public int getMismatchedImageCount() {
         return mismatchCount;
     }
     
     public int getMatchedImageCount(){
         return matchCount;
     }
 }
