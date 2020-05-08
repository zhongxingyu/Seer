 package com.maxmind.geoip;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Shell;
 import org.pentaho.di.core.CheckResult;
 import org.pentaho.di.core.CheckResultInterface;
 import org.pentaho.di.core.Counter;
 import org.pentaho.di.core.database.DatabaseMeta;
 import org.pentaho.di.core.exception.KettleDatabaseException;
 import org.pentaho.di.core.exception.KettleException;
 import org.pentaho.di.core.exception.KettleXMLException;
 import org.pentaho.di.core.row.RowMetaInterface;
 import org.pentaho.di.core.variables.VariableSpace;
 import org.pentaho.di.core.xml.XMLHandler;
 import org.pentaho.di.repository.ObjectId;
 import org.pentaho.di.repository.Repository;
 import org.pentaho.di.trans.Trans;
 import org.pentaho.di.trans.TransMeta;
 import org.pentaho.di.trans.step.BaseStepMeta;
 import org.pentaho.di.trans.step.StepDataInterface;
 import org.pentaho.di.trans.step.StepDialogInterface;
 import org.pentaho.di.trans.step.StepInterface;
 import org.pentaho.di.trans.step.StepMeta;
 import org.pentaho.di.trans.step.StepMetaInterface;
 import org.w3c.dom.Node;
 
 
 /*
  * @author Daniel Einspanjer and Doug Moran
  * @since  April-16-2008
  */
 public class MaxMindGeoIPLookupMeta extends BaseStepMeta implements StepMetaInterface
 {
   private String ipAddressFieldName;
   private String dbLocation;
   private String dbType;
   private String fieldName[];
   private String fieldLookupType[];
   private String fieldIfNull[];
 
   private MaxMindDatabase maxMindDatabase = null;
   
   public String getIpAddressFieldName()
   {
     return ipAddressFieldName;
   }
 
   public void setIpAddressFieldName(String ipAddressFieldName)
   {
     this.ipAddressFieldName = ipAddressFieldName;
   }
 
   public String getDbLocation() {
     return dbLocation;
   }
 
   public void setDbLocation(String dbLocation) {
     this.dbLocation = dbLocation;
     maxMindDatabase = null; // set dirty
   }
 
   public String getDbType() {
     return dbType;
   }
 
   public void setDbType(String dbType) {
     this.dbType = dbType;
     maxMindDatabase = null; // set dirty
   }
 
   public String[] getFieldName() {
     return fieldName;
   }
 
   public void setFieldName(String[] fieldName) {
     this.fieldName = fieldName;
     maxMindDatabase = null; // set dirty
   }
 
   public String[] getFieldLookupType() {
     return fieldLookupType;
   }
 
   public void setFieldLookupType(String[] fieldLookupType) {
     this.fieldLookupType = fieldLookupType;
     maxMindDatabase = null; // set dirty
   }
 
   public String[] getFieldIfNull() {
     return fieldIfNull;
   }
 
   public void setFieldIfNull(String[] fieldIfNull) {
     this.fieldIfNull = fieldIfNull;
     maxMindDatabase = null; // set dirty
   }
   
   public MaxMindGeoIPLookupMeta()
   {
     super(); // allocate BaseStepInfo
   }
 
 public boolean setupMaxMindDatabase() {
     maxMindDatabase = null;
     if (dbType != null) {
       maxMindDatabase = MaxMindGeoIP.getDatabase(dbType);
       if (maxMindDatabase != null) {
         try {
           maxMindDatabase.setDbLocation(dbLocation);
           maxMindDatabase.setSelectedFields(fieldLookupType );
           maxMindDatabase.setSelectedFields(fieldLookupType, fieldName, fieldIfNull );
        } catch (Exception e) { // Invalid Location
           maxMindDatabase = null;
         }
       }
     }
     return (maxMindDatabase != null);
   }
 
   public MaxMindDatabase getMaxMindDatabase() {
     if (maxMindDatabase == null) {
       setupMaxMindDatabase();
     }
     return (maxMindDatabase);
   }
     
   @Override
   public String getXML()
   {
     final StringBuilder retval = new StringBuilder(500);
 
     retval.append("   ").append(XMLHandler.addTagValue("ip_address_field_name", ipAddressFieldName)); //$NON-NLS-1$ //$NON-NLS-2$
     retval.append("   ").append(XMLHandler.addTagValue("db_location", dbLocation)); //$NON-NLS-1$ //$NON-NLS-2$
     retval.append("   ").append(XMLHandler.addTagValue("db_type", dbType)); //$NON-NLS-1$ //$NON-NLS-2$
 
     retval.append("    <fields>"); //$NON-NLS-1$
     for (int i = 0; i < fieldName.length; i++)
     {
       retval.append("      <field>"); //$NON-NLS-1$
       retval.append("        ").append(XMLHandler.addTagValue("name", fieldName[i])); //$NON-NLS-1$ //$NON-NLS-2$
       retval.append("        ").append(XMLHandler.addTagValue("lookup_type", fieldLookupType[i])); //$NON-NLS-1$ //$NON-NLS-2$
       retval.append("        ").append(XMLHandler.addTagValue("ifnull", fieldIfNull[i])); //$NON-NLS-1$ //$NON-NLS-2$
       retval.append("      </field>"); //$NON-NLS-1$
     }
     retval.append("    </fields>"); //$NON-NLS-1$
     return retval.toString();
   }
 
   @Override
   public void getFields(RowMetaInterface r, String origin, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space)
   {
     if (setupMaxMindDatabase()) {
       maxMindDatabase.getFields(r, origin);
     }
   }
 
   public void allocate(int nrfields)
   {
     fieldName = new String[nrfields];
     fieldLookupType = new String[nrfields];
     fieldIfNull = new String[nrfields];
   }  
 
   @Override
   public Object clone()
   {
     MaxMindGeoIPLookupMeta retval = (MaxMindGeoIPLookupMeta) super.clone();
     final int nrfields = fieldName.length;
 
     retval.allocate(nrfields);
 
     for (int i = 0; i < nrfields; i++)
     {
       retval.fieldName[i] = fieldName[i];
       retval.fieldLookupType[i] = fieldLookupType[i];
       retval.fieldIfNull[i] = fieldIfNull[i];
     }
     return retval;
   }
 
 	public void loadXML(Node stepnode, List<DatabaseMeta> databases, Map<String,Counter> counters)
 		throws KettleXMLException
   {
     maxMindDatabase = null; // Set Dirty
     try
     {
       setIpAddressFieldName(XMLHandler.getTagValue(stepnode, "ip_address_field_name"));
       setDbLocation(XMLHandler.getTagValue(stepnode, "db_location"));
       setDbType(XMLHandler.getTagValue(stepnode, "db_type"));
 
       final Node fields = XMLHandler.getSubNode(stepnode, "fields"); //$NON-NLS-1$
       final int nrfields = XMLHandler.countNodes(fields, "field"); //$NON-NLS-1$
 
       allocate(nrfields);
 
       for (int i=0;i<nrfields;i++)
       {
         final Node fnode = XMLHandler.getSubNodeByNr(fields, "field", i); //$NON-NLS-1$
 
         fieldName[i] = XMLHandler.getTagValue(fnode, "name"); //$NON-NLS-1$
         fieldLookupType[i] = XMLHandler.getTagValue(fnode, "lookup_type"); //$NON-NLS-1$
         fieldIfNull[i] = XMLHandler.getTagValue(fnode, "ifnull"); //$NON-NLS-1$
       }
 
     }
     catch(Exception e)
     {
       throw new KettleXMLException("Unable to read step info from XML node", e);
     }
     setupMaxMindDatabase();
   }
 
   public void setDefault()
   {
     ipAddressFieldName = ""; //$NON-NLS-1$
     dbLocation = ""; //$NON-NLS-1$
     dbType = ""; //$NON-NLS-1$
     maxMindDatabase = null;
     allocate(0);
   }
 
   public void readRep(Repository rep, ObjectId idStep, List<DatabaseMeta> databases, Map<String, Counter> counters) throws KettleException {
     maxMindDatabase = null;
     try
     {
     	setIpAddressFieldName(rep.getStepAttributeString(idStep, "ip_address_field_name")); //$NON-NLS-1$
       setDbLocation(rep.getStepAttributeString(idStep, "db_location")); //$NON-NLS-1$
       setDbType(rep.getStepAttributeString(idStep, "db_type")); //$NON-NLS-1$
 
       int nrfields = rep.countNrStepAttributes(idStep, "field_name"); //$NON-NLS-1$
 
       allocate(nrfields);
 
       for (int i=0;i<nrfields;i++)
       {
         fieldName[i] = rep.getStepAttributeString(idStep, i, "field_name"); //$NON-NLS-1$
         fieldLookupType[i] = rep.getStepAttributeString(idStep, i, "field_lookup_type"); //$NON-NLS-1$
         fieldIfNull[i] = rep.getStepAttributeString(idStep, i, "field_ifnull"); //$NON-NLS-1$
       }
     } 
     catch (KettleDatabaseException dbe)
     {
       throw new KettleException("error reading step with id_step=" + idStep + " from the repository", dbe);
     }
     catch (Exception e)
     {
       throw new KettleException("Unexpected error reading step with id_step=" + idStep + " from the repository", e);
     }
     setupMaxMindDatabase();
   }
 
   @Override
   public void saveRep(Repository rep, ObjectId idTransformation, ObjectId idStep) throws KettleException
   {
     try
     {
     	rep.saveStepAttribute(idTransformation, idStep, "ip_address_field_name", getIpAddressFieldName()); //$NON-NLS-1$
       rep.saveStepAttribute(idTransformation, idStep, "db_location", getDbLocation()); //$NON-NLS-1$
       rep.saveStepAttribute(idTransformation, idStep, "db_type", getDbType()); //$NON-NLS-1$
 
       for (int i = 0; i < fieldName.length; i++)
       {
         rep.saveStepAttribute(idTransformation, idStep, i, "field_name", fieldName[i]); //$NON-NLS-1$
         rep.saveStepAttribute(idTransformation, idStep, i, "field_lookup_type", fieldLookupType[i]); //$NON-NLS-1$
         rep.saveStepAttribute(idTransformation, idStep, i, "field_ifnull", fieldIfNull[i]); //$NON-NLS-1$
       }
     }
     catch (KettleDatabaseException dbe)
     {
       throw new KettleException("Unable to save step information to the repository, id_step=" + idStep, dbe);
     }
   }
 
 // TODO: This doesn't seem to work
   public void check(List<CheckResultInterface> remarks, TransMeta transmeta, StepMeta stepMeta, RowMetaInterface prev,String input[], String output[], RowMetaInterface info)
   {
     //TODO: l10n all these messages
 		//TODO: Check for maxmind .dat file presence
 		//TODO: Do a full test on the .dat file, maybe by looking up a well known IP address
 
     CheckResult cr;
     if (prev == null || prev.size() == 0) 
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_WARNING, "Not receiving any fields from previous steps!", stepMeta);
       remarks.add(cr);
     }
     else
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is connected to previous one, receiving " + prev.size() + " fields", stepMeta);
       remarks.add(cr);
     }
 
     // See if we have input streams leading to this step!
     if (input.length > 0)
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "Step is receiving info from other steps.", stepMeta);
       remarks.add(cr);
     }
     else
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "No input received from other steps!", stepMeta);
       remarks.add(cr);
     }
 
     if ( (prev == null) || (prev.indexOfValue(getIpAddressFieldName()) < 0) )
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "IP Address field not found.", stepMeta);
       remarks.add(cr);
     }
     else
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "IP Address field found.", stepMeta);
       remarks.add(cr);
     }
 
     if (getDbLocation() == null || getDbLocation().length() == 0)
     {
       cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MaxMind GeoIP DB Location not specified.", stepMeta);
       remarks.add(cr);
     }
     else
     { 
     	try {
       	MaxMindGeoIP.initLookupService(getDbLocation());
         cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "MaxMind GeoIP DB Location is valid.", stepMeta);
     	} catch (IOException e) { // Invalid Location
         cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MaxMind DB file invalid: " + getDbLocation() + ".", stepMeta);
       }
       remarks.add(cr);
     }
 
     if (getDbType() == null || getDbType().length() == 0) {
       cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MaxMind GeoIP DB Type not specified.", stepMeta);
       remarks.add(cr);
     } else {
       cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "MaxMind GeoIP DB Type is specified.", stepMeta);
       remarks.add(cr);
     }
 
     if (setupMaxMindDatabase()) {
       cr = new CheckResult(CheckResult.TYPE_RESULT_OK, "MaxMind database initialized successfully.", stepMeta);
       remarks.add(cr);
     } else {
       cr = new CheckResult(CheckResult.TYPE_RESULT_ERROR, "MaxMind database did not initialize successfully.", stepMeta);
       remarks.add(cr);
     }
 
   }
 
   public StepDialogInterface getDialog(Shell shell, StepMetaInterface meta, TransMeta transMeta, String name)
   {
   	return new MaxMindGeoIPLookupDialog(shell, meta, transMeta, name);
   }
 
   public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,Trans disp)
       {
     return new MaxMindGeoIPLookup(stepMeta, stepDataInterface, cnr, transMeta, disp);
   }
 
   public StepDataInterface getStepData()
   {
     return new MaxMindGeoIPLookupData();
   }
 }
