 package edu.ucdenver.ccp.PhenoGen.data;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 
 import java.lang.Thread;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.ResultSet;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.TreeSet;
 
 import edu.ucdenver.ccp.util.sql.Results;
 import edu.ucdenver.ccp.util.Debugger;
 import edu.ucdenver.ccp.util.FileHandler;
 import edu.ucdenver.ccp.util.ObjectHandler;
 import edu.ucdenver.ccp.PhenoGen.util.DbUtils;
 //import edu.ucdenver.ccp.PhenoGen.util.AsyncCopyFiles;
 import edu.ucdenver.ccp.PhenoGen.web.mail.Email; 
 import edu.ucdenver.ccp.PhenoGen.web.SessionHandler; 
 import java.sql.*;
 
 /* for logging messages */
 import org.apache.log4j.Logger;
 
 /**
  * Class for handling data related to managing <i>in-silico</i> datasets. This includes 
  * 'versions' of the dataset created by normalizing the data in different ways.<br>
  *  @author  Cheryl Hornbaker
  */
 
 public class Dataset {
   	private int dataset_id;
   	private int version;
   	private String dataset_name;
   	private String nameNoSpaces;
   	private String description;
   	private java.sql.Timestamp create_date;
   	private int created_by_user_id;
   	private int number_of_arrays;
   	private int submis_id;
   	private String path;
   	private String platform;
   	private String qc_complete;
         private int arrayTypeID;
   	private String array_type="";
   	private String creator;
   	private String organism;
   	private String organism_fullname;
   	private String create_date_as_string;
   	private String created_by_full_name;
   	private String database;
   	private String dataset_info;
   	private String hybridIDs;
   	private DatasetVersion[] datasetVersions;
   	private String[] dataRow;
 	private String sortColumn = "datasetID";
 	private String sortOrder = "A";
         private boolean visible=true;
         private String visibleNote="";
 
 	public static final String AFFYMETRIX_PLATFORM = "Affymetrix";
 	public static final String CODELINK_PLATFORM = "CodeLink";
 	public static final String BXDRI_DATASET_NAME = "Public BXD RI Mice";
 	public static final String INBRED_DATASET_NAME = "Public Inbred Mice";
 	public static final String BXDRI_INBRED_DATASET_NAME = "Public BXD RI and Inbred Mice";
 	public static final String LXSRI_DATASET_NAME = "Public ILSXISS RI Mice";
 	public static final String HXBRI_DATASET_NAME = "Public HXB/BXH RI Rats";
 	public static final String HXBRI_BRAIN_EXON_DATASET_NAME = "Public HXB/BXH RI Rats (Brain, Exon Arrays)";
 	public static final String HXBRI_HEART_EXON_DATASET_NAME = "Public HXB/BXH RI Rats (Heart, Exon Arrays)";
 	public static final String HXBRI_LIVER_EXON_DATASET_NAME = "Public HXB/BXH RI Rats (Liver, Exon Arrays)";
 	public static final String HXBRI_BROWN_ADIPOSE_EXON_DATASET_NAME = "Public HXB/BXH RI Rats (Brown Adipose, Exon Arrays)";
 	public static final Set<String> DATASETS_WITH_GENOTYPE_DATA = new LinkedHashSet<String>() { 
 		{
 			add(BXDRI_DATASET_NAME);
 			add(HXBRI_DATASET_NAME);
 			add(LXSRI_DATASET_NAME);
 			add(HXBRI_BRAIN_EXON_DATASET_NAME);
 			add(HXBRI_LIVER_EXON_DATASET_NAME);
 			add(HXBRI_HEART_EXON_DATASET_NAME);
 			add(HXBRI_BROWN_ADIPOSE_EXON_DATASET_NAME);
 		}
 	};
 	public static final Set<String> CALC_QTL_DATASETS = new LinkedHashSet<String>() { 
 		{
 			add(BXDRI_DATASET_NAME);
 			add(HXBRI_DATASET_NAME);
 			add(LXSRI_DATASET_NAME);
 		}
 	};
 
 
   	private int[] dataset_users;
   	private edu.ucdenver.ccp.PhenoGen.data.Array[] arrays;
 
   	private Logger log=null;
 
   	private DbUtils myDbUtils = new DbUtils();
   	private Debugger myDebugger = new Debugger();
   	private ObjectHandler myObjectHandler = new ObjectHandler();
 	
   	private String datasetSelectClause = 
                 	"select ds.dataset_id, "+
                 	"ds.name, "+
                 	"ds.description, "+
                 	"u.user_name creator, "+
                 	"to_char(ds.create_date, 'mm/dd/yyyy hh12:mi AM'), "+
                 	"count(distinct dc.user_chip_id), "+
                 	"ds.organism, "+
                 	"ds.platform, "+
                 	"ds.qc_complete, "+
 			"org.organism_name, "+
                 	"' ('||count(distinct dc.user_chip_id)||' '|| "+
                 	"       decode(ds.platform, 'Affymetrix', 'Affy', ds.platform)|| "+
 			"	' '|| "+
 			"	ds.organism "+
 			"	||' arrays)', "+
 			"ds.created_by_user_id, "+
 			"u.title||' '||u.first_name||' '||u.last_name , "+
                         "atype.Array_name, "+
                         "ds.visible, ds.visible_note ";
 
   	private String datasetFromClause =
                 	"from users u, "+
 			"organisms org, "+
 			"datasets ds left join "+ 
 			"dataset_chips dc on ds.dataset_id = dc.dataset_id "+
                         "left join Array_TYPES atype on ds.array_type_id = atype.array_type_id "; 
 
   	private String datasetWhereClause = 
                 	"where ds.created_by_user_id = u.user_id "+
 			"and ds.organism = org.organism "+ 
 			"and ds.name != 'Dummy'||ds.dataset_id ";
 
   	private String datasetGroupByClause = 
                 	"group by ds.dataset_id, "+
 			"ds.name, "+ 
                 	"ds.description, "+ 
                 	"u.user_name, "+ 
                 	"ds.create_date, "+
 			"ds.organism, "+
 			"ds.platform, "+
 			"ds.qc_complete, "+
 			"org.organism_name, "+
 			"dc.dataset_id, "+
 			"ds.created_by_user_id, "+
 			"u.title||' '||u.first_name||' '||u.last_name, "+
                         "ds.ARRAY_TYPE_ID, "+
                         "atype.Array_name, "+
                         "ds.visible, ds.visible_note ";
 
 	private String datasetVersionSelectClause = 
 			datasetSelectClause +
 			", "+
 	        	"dv.version, "+
 			"dv.version_name, "+
                 	"to_char(dv.create_date, 'mm/dd/yyyy hh12:mi AM'), "+
 			"dv.visible, "+
 			"pg.parameter_group_id, "+
 			"dv.version_type, "+
 			"dv.grouping_id ";
 
 	private String datasetVersionDetailsSelectClause = 
 			datasetSelectClause +
 			", "+
 	        	"dv.version, "+
 			"dv.version_name, "+
                 	"to_char(dv.create_date, 'mm/dd/yyyy hh12:mi AM'), "+
 			"dv.visible, "+
 			"dv.parameter_group_id, "+
 			"dv.version_type, "+
 			"dv.grouping_id, "+
 			"dv.normalization_method, "+
 			"dv.number_of_groups, "+
 			"dv.number_of_non_exclude_groups, "+
 			"dv.number_of_genotype_groups ";
 
 			/*
 			datasetVersionSelectClause + 
 			", pv.value, "+
 			//"upper(replace(substr(dv.version_name, "+
 			//"	instr(dv.version_name, 'Normalized using') + 17), '''', '')), "+
 			"count(distinct grps.group_number) number_of_groups, "+
 			"count(distinct decode(grps.group_number, 0, '', grps.group_number)) number_of_non_exclude_groups ";
 			*/
 
   	private String datasetVersionFromClause = 
 			datasetFromClause + 
 			"left join dataset_versions dv on ds.dataset_id = dv.dataset_id " +
 			"left join parameter_groups pg "+
 			"	on ds.dataset_id = pg.dataset_id "+
 			"	and dv.version = pg.version "+
 			"	and pg.master = 1 "
 			;
 
 	private String datasetVersionDetailsFromClause = 
 			datasetFromClause + 
 			"left join dataset_versions_view dv on ds.dataset_id = dv.dataset_id ";
 			/*
 			datasetVersionFromClause + 
 			"left join parameter_values pv "+
 			"	on pg.parameter_group_id = pv.parameter_group_id "+
 			"	and pv.parameter like '%Normalization Method%' "+
 			"left join groups grps on dv.grouping_id = grps.grouping_id ";
 			*/
 
   	private String datasetVersionWhereClause = 
 			datasetWhereClause; 
 
   	private String datasetVersionGroupByClause = 
 			datasetGroupByClause + 
 			", "+
 			"dv.version, "+
 			"dv.version_name, "+
                 	"dv.create_date, "+
 			"dv.visible, "+
   	  		"pg.parameter_group_id, "+	
 			"dv.version_type, "+
 			"dv.grouping_id ";
 
 	private String datasetVersionDetailsGroupByClause = 
 			datasetGroupByClause + 
 			", "+
 			"dv.version, "+
 			"dv.version_name, "+
                 	"dv.create_date, "+
 			"dv.visible, "+
   	  		"dv.parameter_group_id, "+	
 			"dv.version_type, "+
 			"dv.grouping_id, "+
 			"dv.normalization_method, "+
  			"dv.number_of_groups, "+
 			"dv.number_of_non_exclude_groups, "+
 			"dv.number_of_genotype_groups ";
 			/*
 			datasetVersionGroupByClause + 
   	  		", pv.value ";	
 			*/
         
         private String selectArrayTypeID="Select array_type_id from Array_types where ARRAY_NAME like "+
                     "(select distinct case when tardesin_design_name is null then ebi_array_description else tardesin_design_name end "+
                     "from CuratedExperimentDetails expDetails, TARRAY left join TARDESIN on tarray_designid = tardesin_sysuid "+
                     "left join EBI_ARRAY_DESIGNS on tarray_designid*-1 = EBI_ARRAY_SYSUID where expDetails.hybrid_array_id = TARRAY.tarray_sysuid and "+
                     "expDetails.hybrid_id in (select uc.hybrid_id from user_chips uc, dataset_chips dc where uc.user_chip_id = dc.user_chip_id and "+
                     "dc.dataset_id = ?))";
         
         private String datasetArrayTypeUpdate="update DATASETS set ARRAY_TYPE_ID=?  where dataset_id=?";
         
 	private PreparedStatement pstmt = null;
 
   	public Dataset () {
 		log = Logger.getRootLogger();
   	}
 
   	public Dataset (int dataset_id) {
 		log = Logger.getRootLogger();
 		this.setDataset_id(dataset_id);
   	}
 
   	public Dataset (String datasetName) {
 		log = Logger.getRootLogger();
 		this.setName(datasetName);
   	}
 
   	public int getDataset_id() {
     		return dataset_id; 
   	}
 
   	public void setDataset_id(int inInt) {
     		this.dataset_id = inInt;
   	}
 
   	public void setName(String inString) {
     		this.dataset_name = inString;
   	}
 
   	public String getName() {
     		return dataset_name; 
   	}
 
   	public void setNameNoSpaces(String inString) {
     		this.nameNoSpaces = inString;
   	}
 
   	public String getNameNoSpaces() {
     		return nameNoSpaces; 
   	}
 
   	public void setDescription(String inString) {
     		this.description = inString;
   	}
 
   	public String getDescription() {
     		return description; 
   	}
 
   	public void setCreate_date_as_string(String inString) {
     		this.create_date_as_string = inString;
   	}
 
   	public String getCreate_date_as_string() {
     		return create_date_as_string; 
   	}
 
   	public void setCreate_date(java.sql.Timestamp inTimestamp) {
     		this.create_date = inTimestamp;
   	}
 
   	public java.sql.Timestamp getCreate_date() {
     		return create_date;
   	}
 
   	public void setSubmis_id(int inInt) {
     		this.submis_id = inInt;
   	}
 
   	public int getSubmis_id() {
     		return submis_id; 
   	}
 
   	public void setNumber_of_arrays(int inInt) {
     		this.number_of_arrays = inInt;
   	}
 
   	public int getNumber_of_arrays() {
     		return number_of_arrays; 
   	}
 
   	public void setCreated_by_full_name(String inString) {
     		this.created_by_full_name = inString;
   	}
 
   	public String getCreated_by_full_name() {
     		return created_by_full_name; 
   	}
 
   	public void setCreated_by_user_id(int inInt) {
 	    	this.created_by_user_id = inInt;
   	}
 
   	public int getCreated_by_user_id() {
     		return created_by_user_id; 
   	}
 
   	/**
    	* Sets the physical location of the dataset's master directory.  Should be used in conjunction
    	* with setDatasetPath().  NOTE: The path contains a '/' at the end.
    	*/
   	public void setPath(String inString) {
     		this.path = inString;
   	}
 
   	public String getPath() {
     		return path;
   	}
 
   	public void setQc_complete(String inString) {
     		this.qc_complete = inString;
   	}
 
   	public String getQc_complete() {
     		return qc_complete;
   	}
 
 	/** Sets the creator of the dataset to the user_name of the person who created the dataset.
 	 * @param inString	the user_name of the dataset's creator
 	 */
 	public void setCreator(String inString) {
 		this.creator = inString;
 	}
 
 	/** Gets the user_name of the creator of the dataset.
 	 * @return the user_name of the dataset's creator
 	 */
 	public String getCreator() {
 		return creator;
 	}
 
 	/** This is derived by querying the database for the type of arrays used in the dataset. 
 	 * @param inString	the type of array used in the dataset
 	 */
 	public void setArray_type(String inString) {
 		this.array_type = inString;
 	}
 
 	/** This is derived by querying the database for the type of arrays used in the dataset. 
 	 * @return inString	the type of array used in the dataset
 	 */
         
         public void setArrayTypeID(int arrayTypeID){
             
         }
         
 	public String getArray_type() {
 		return array_type;
 	}
 
   	public void setPlatform(String inString) {
     		this.platform = inString;
   	}
 
   	public String getPlatform() {
     		return platform;
   	}
 
   	public void setOrganism(String inString) {
     		this.organism = inString;
   	}
 
   	public String getOrganism() {
     		return organism;
   	}
 
   	public void setOrganism_fullname(String inString) {
     		this.organism_fullname = inString;
   	}
 
   	public String getOrganism_fullname() {
     		return organism_fullname;
   	}
 
   	public void setDatabase(String inString) {
     		this.database = inString;
   	}
 
   	public String getDatabase() {
     		return database;
   	}
 
   	public void setHybridIDs(String inString) {
     		this.hybridIDs = inString;
   	}
 
   	public String getHybridIDs() {
 		if (hybridIDs.equals("()")) {
 			hybridIDs = "('')";
 		}
 		return hybridIDs;
   	}
 
   	public void setDataset_info(String inString) {
     		this.dataset_info = inString;
   	}
 
   	public String getDataset_info() {
     		return dataset_info;
   	}
 
   	public void setDataset_users(int[] inArray) {
     		this.dataset_users = inArray;
   	}
 
   	public int[] getDataset_users() {
     		return dataset_users;
   	}
 
   	public void setArrays(edu.ucdenver.ccp.PhenoGen.data.Array[] inArrays,Connection conn,String userFilesRoot) {
     		this.arrays = inArrays;
                 boolean duplicates=false;
                 HashMap<String,String> hm=new HashMap<String,String>();
                 for (int j=0; j<this.arrays.length&&!duplicates; j++) {
                     String arrayName = this.arrays[j].getHybrid_name().replaceAll("[\\s]", "_");
                     if(hm.containsKey(arrayName)){
                         duplicates=true;
                     }else{
                         hm.put(arrayName, "A");
                     }
                 }
                if(duplicates && conn!=null){
                     User creator=new User();
                     try{
                    String path=this.getDatasetPath(creator.getUser(this.getCreator(),conn).getUserMainDir(userFilesRoot))+this.getNameNoSpaces()+".arrayFiles.txt";
                    FileHandler myFH=new FileHandler();
                    try{
                    String[] contents=myFH.getFileContents(new File(path));
                    HashMap<String,String> hm2=new HashMap<String,String>();
                    for(int i=0;i<contents.length;i++){
                        String[] tabs=contents[i].split("\t");
                        hm2.put(tabs[0], tabs[1]);
                    }
                    for (int j=0; j<this.arrays.length; j++) {
                        this.arrays[j].setHybrid_name(hm2.get(this.arrays[j].getFile_name()));
                    }
                    }catch(IOException e){
                        log.error("Error reading path:"+path+"\n"+e,e);
                    }
                     }catch(SQLException e){
                         log.error("Error finding creator for dataset."+e,e);
                     }
                 }
   	}
 
   	public edu.ucdenver.ccp.PhenoGen.data.Array[] getArrays() {
     		return arrays;
   	}
 
   	public void setDatasetVersions(DatasetVersion[] inArray) {
     		this.datasetVersions = inArray;
   	}
 
   	public DatasetVersion[] getDatasetVersions() {
     		return datasetVersions;
   	}
 
   	public String getSortColumn() {
     		return sortColumn; 
   	}
 
   	public void setSortColumn(String inString) {
     		this.sortColumn = inString;
   	}
 
   	public String getSortOrder() {
     		return sortOrder; 
   	}
 
   	public void setSortOrder(String inString) {
     		this.sortOrder = inString;
   	}
 
         public boolean getVisible(){
             return this.visible;
         }
         
         private void setVisible(String visible)
         {
             if(visible.equals("0")){
                 this.visible=false;
             }else{
                 this.visible=true;
             }
         }
         
         public String getVisibleNote(){
             return this.visibleNote;
         }
         
         private void setVisibleNote(String visible)
         {
             this.visibleNote=visible;
         }
         
 	public boolean hasPhenotypeData(int user_id) {
 		for (DatasetVersion thisDatasetVersion : this.getDatasetVersions()) {
 			if (thisDatasetVersion.hasPhenotypeData(user_id)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasGeneLists() {
 		for (DatasetVersion thisDatasetVersion : this.getDatasetVersions()) {
 			if (thisDatasetVersion.hasGeneLists()) {
 				return true;
 			}
 		}
 		return false;
 	}
         
         public boolean hasFilterStatsResults(int userID,Connection conn) {
 		for (DatasetVersion thisDatasetVersion : this.getDatasetVersions()) {
 			if (thisDatasetVersion.hasFilterStatsResults(userID,conn)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasClusterResults(int user_id) {
 		for (DatasetVersion thisDatasetVersion : this.getDatasetVersions()) {
 			if (thisDatasetVersion.hasClusterResults(user_id)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public boolean hasVisibleVersions() {
 		for (DatasetVersion thisDatasetVersion : this.getDatasetVersions()) {
 			if (thisDatasetVersion.getVisible() == 1) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	public boolean hasGeneratedQCImage() {
 	
 		edu.ucdenver.ccp.PhenoGen.data.Array myArray = new edu.ucdenver.ccp.PhenoGen.data.Array();
 		String hybridName = this.getArrays()[0].getHybrid_name();
 		
 		boolean hasgeneratedQCImage = false;
 		
 		if (this.getPlatform().equals(CODELINK_PLATFORM)) {
 			String [] imageFileNames = myArray.getCodeLinkImageFileNames(hybridName);
 			hasgeneratedQCImage = true;
 			for (String fileName: imageFileNames) {
 				if (!(new File(getImagesDir() + fileName).exists())) {
 					hasgeneratedQCImage = false;
 				}
 				//log.debug("fileName = "+fileName + ", exists = " + hasgeneratedQCImage);
 			}
 		} else if (this.getPlatform().equals(AFFYMETRIX_PLATFORM)) {
 			String [] imageFileNames = myArray.getAffyQCFileNames(hybridName);
 			hasgeneratedQCImage = true;
 			for (String fileName: imageFileNames) {
 				if (!(new File(getImagesDir() + fileName).exists())) {
 					hasgeneratedQCImage = false;
 				}
 				//log.debug("fileName = "+fileName + ", exists = " + hasgeneratedQCImage);
 			}
 		}
 		return hasgeneratedQCImage;
 	}
 	
 	/**
 	 * Retrieves the DatasetVersion object corresponding to the version number.  This assumes the Dataset has already 
 	 * been retrieved and setDatasetVersions.  The version path is also set here.
 	 * @param version	the number of the version
 	 * @return            A DatasetVersion object 
 	 */
 	public DatasetVersion getDatasetVersion(int version) {
 		DatasetVersion myDatasetVersion = 
 			new DatasetVersion().getDatasetVersion(this.getDatasetVersions(), version);
 		myDatasetVersion.setVersion_path(this.getPath(), myDatasetVersion.getVersion());
 		myDatasetVersion.setDataset(this);
 		return myDatasetVersion;
 	}
 
 	/**
 	 * Retrieves the dataset version based on the parameter group id 
 	 * @param parameter_group_id	the ID of the parameter group
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            the DatasetVersion object
 	 */
 	public DatasetVersion getDatasetVersionForParameterGroup(int parameter_group_id, Connection conn) throws SQLException {
 
 		//log.debug("In getDatasetVersionForParameterGroup. parameter_group_id = " + parameter_group_id);
 
         	String query =
 			"select version "+
 			"from parameter_groups "+
 			"where parameter_group_id = ?";
 	
 		//log.debug("query = "+query);
 
                 Results myResults = new Results(query, parameter_group_id, conn);
                 int version = myResults.getIntValueFromFirstRow();
 		myResults.close();
 
 		DatasetVersion thisDatasetVersion = this.getDatasetVersion(version);
 		thisDatasetVersion.setDataset(this);
 
         	return thisDatasetVersion;
 	}
 
 	/**
 	 * Gets the dataset ID of the default public dataset for calculating QTLs
 	 * @param publicDatasets	the datasets to which this user has access
 	 * @return	the dataset_id of the 'Public BXD RI Mice' dataset
 	 */
 	
 	public int getDefaultPublicDatasetID(Dataset[] publicDatasets) { 
 
         	int dataset_id = -99;
 
         	for (int i=0; i<publicDatasets.length; i++) {
                 	Dataset dataset = (Dataset) publicDatasets[i];
                         if (dataset.getName().indexOf(BXDRI_DATASET_NAME) > -1) {
                                 dataset_id = dataset.getDataset_id();
 				break;
                         }
         	}
 		return dataset_id;
 	}
 	
 	/**
 	 * Retrieves the next version number for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            A Dataset object with its values setup 
 	 */
 	 public int getNextVersion(Connection conn) throws SQLException {
 
 		log.debug("in getNextVersion");
 
         	String query =
                 	"select max(version) + 1 "+
                 	"from dataset_versions "+
                 	"where dataset_id = ?";
 
                 Results myResults = new Results(query, this.getDataset_id(), conn);
 
                 int version = myResults.getIntValueFromFirstRow();
 
                 myResults.close();
 
         	return version;
 	}
 
 	/**
 	 * Retrieves a Dataset object with the data values set to those retrieved from the database.  
 	 * Also retrieves
 	 * the DatasetVersions for this Dataset AND does setVersion_path().
 	 * @param dataset_id	the ID of the dataset
 	 * @param userLoggedIn	the User object of the user logged in
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            A Dataset object with its values setup 
 	 */
 	public Dataset getDataset(int dataset_id, User userLoggedIn, Connection conn,String userFileRoot) throws SQLException {
         	log.debug("in getDataset with userLoggedIn. dataset_id = " +dataset_id);
 		Dataset thisDataset = getDataset(dataset_id, conn,userFileRoot);
 		thisDataset.setPath(thisDataset.getDatasetPath(userLoggedIn.getUserMainDir()));
 
 		//log.debug("just set path to "+thisDataset.getPath());
 		if (thisDataset.getDatasetVersions() != null && thisDataset.getDatasetVersions().length > 0) {
 			for (int i=0; i<thisDataset.getDatasetVersions().length; i++) {
 				DatasetVersion thisDatasetVersion = thisDataset.getDatasetVersions()[i];
 				thisDataset.getDatasetVersions()[i].setVersion_path(thisDataset.getPath(), thisDataset.getDatasetVersions()[i].getVersion());
 				// this makes sure the exp path and hybridIDs are available for the version
 				thisDatasetVersion.setDataset(thisDataset);
 			}
 		}
         	//log.debug("setting up parameter values in getDataset with userLoggedIn. dataset_id = " +dataset_id);
 		setupDatasetParameterValues(userLoggedIn.getUser_id(), thisDataset, conn);
 		return thisDataset;
 	}
 
 	/**
 	 * Retrieves a Dataset object with the data values set to those retrieved from the database.  
 	 * Also retrieves the DatasetVersions for this Dataset.  NOTE THAT THIS DOES NOT
 	 * setPath() of the Dataset or setVersion_path() of the versions
 	 * @param dataset_id	the ID of the dataset
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            A Dataset object with its values setup 
 	 */
 	public Dataset getDataset(int dataset_id, Connection conn,String userFilesRoot) throws SQLException {
 
         	log.debug("in getDataset as a Dataset object. dataset_id = " +dataset_id);
 
         	String query =
 			datasetVersionDetailsSelectClause + 
 			datasetVersionDetailsFromClause + 
 			datasetVersionWhereClause + 
                 	"and ds.dataset_id = ? "+ 
 			datasetVersionDetailsGroupByClause; 
 
 		//log.debug("query = "+query);
 
         	Results myResults = new Results(query, dataset_id, conn);
 
 		Dataset thisDataset = setupDatasetVersionValues(myResults, true)[0]; 
 		thisDataset.setHybridIDs(thisDataset.getDatasetHybridIDs(conn));
         	if (!thisDataset.getHybridIDs().equals("()")) {
                         if(array_type.equals("null")){
                             thisDataset.setArray_type(new edu.ucdenver.ccp.PhenoGen.data.Array().getDatasetArrayType(thisDataset.getHybridIDs(), conn));
                         }
                         log.debug("in getDataset right before settingArrays");
                 	thisDataset.setArrays(new edu.ucdenver.ccp.PhenoGen.data.Array().getArraysByHybridIDs(thisDataset.getHybridIDs(), conn),conn,userFilesRoot);
 			log.debug("num arrays = "+thisDataset.getArrays().length);
 		}
 
 		myResults.close();
 
 		//thisDataset.print();
 
         	return thisDataset;
   	}
         public void updateArrayType(int dataset_id, Connection conn) throws SQLException {
 
         	log.debug("in updateArrayType as a Dataset object. dataset_id = " +dataset_id);
                 String arrayType="";
         	String query = this.selectArrayTypeID;
                 PreparedStatement ps=conn.prepareStatement(query);
                 ps.setInt(1, dataset_id);
                 ResultSet rs=ps.executeQuery();
                 if(rs.next()){
                     int arrayTypeID=rs.getInt(1);
                     query=this.datasetArrayTypeUpdate;
                     PreparedStatement ps2=conn.prepareStatement(query);
                     ps2.setInt(1, arrayTypeID);
                     ps2.setInt(2, dataset_id);
                     ps2.executeUpdate();
                     ps2.close();
                     query="select Array_name from array_types where array_type_id=?";
                     ps2=conn.prepareStatement(query);
                     ps2.setInt(1, arrayTypeID);
                     ResultSet rs2=ps2.executeQuery();
                     if(rs2.next()){
                         arrayType=rs2.getString(1);
                         if(arrayType!=null&&!arrayType.equals("")){
                             this.setArray_type(arrayType);
                         }
                     }
                     ps2.close();
                 }
                 ps.close();
 
   	}
 	/**
 	 * Creates a new Dataset object and sets the data values to those retrieved from the database.
    	 * @param dataRow	the row of data corresponding to one Dataset 
 	 * @return            A Dataset object with its values setup 
    	 */
 	private Dataset setupDatasetValues(String[] dataRow) {
 
         	//log.debug("in setupDatasetValues");
         	//log.debug("dataRow= "); myDebugger.print(dataRow);
 
         	Dataset myDataset = new Dataset();
 
         	myDataset.setDataset_id(Integer.parseInt(dataRow[0]));
         	myDataset.setName(dataRow[1]);
         	myDataset.setDescription(dataRow[2]);
         	myDataset.setCreator(dataRow[3]);
         	myDataset.setCreate_date_as_string(dataRow[4]);
 		myDataset.setCreate_date((dataRow[4].equals(" ") ? null : myObjectHandler.getDisplayDateAsTimestamp(dataRow[4])));
         	myDataset.setNumber_of_arrays(Integer.parseInt(dataRow[5]));
         	myDataset.setOrganism(dataRow[6]);
         	myDataset.setPlatform(dataRow[7]);
         	myDataset.setQc_complete(dataRow[8]);
         	myDataset.setOrganism_fullname(dataRow[9]);
         	myDataset.setNameNoSpaces(myObjectHandler.removeBadCharacters(dataRow[1]));
         	myDataset.setDataset_info(dataRow[10]);
         	myDataset.setCreated_by_user_id(Integer.parseInt(dataRow[11]));
         	myDataset.setCreated_by_full_name(dataRow[12]);
                 myDataset.setArray_type(dataRow[13]);
                 myDataset.setVisible(dataRow[14]);
                 myDataset.setVisibleNote(dataRow[15]);
 
         	return myDataset;
 
 	}
 
 	/**
  	 * Retrieves the groupings for this dataset.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	an array of Group objects
 	 */
 	public Group[] getGroupings(Connection conn) throws SQLException {
 
         	String query =
 			"select grpings.grouping_id, "+
 			"grpings.grouping_name, "+
 			"grpings.criterion, "+
 			"grpings.dataset_id "+
                         "from groupings grpings "+
                         "where grpings.dataset_id = ? "+
                         "order by grpings.grouping_name";
 
         	List<Group> myGroupList = new ArrayList<Group>();
 
 		Results myResults = new Results(query, this.getDataset_id(), conn);
                 String[] dataRow;
 		while ((dataRow = myResults.getNextRow()) != null) {
                        	Group newGroup = new Group().setupGrouping(dataRow);
                         myGroupList.add(newGroup);
 		} 
         	myResults.close();
 
 		Group[] myGroupArray = (Group[]) myObjectHandler.getAsArray(myGroupList, Group.class);
 
   		return myGroupArray;
   	}
 
 	/**
  	 * Retrieves the groups for this grouping_id.  Only retrieves groups that have expression data.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	an array of Group objects
 	 */
 	public Group[] getGroupsInGrouping(int grouping_id, Connection conn) throws SQLException {
 
         	String query =
 			"select grpings.grouping_id, "+
 			"grpings.grouping_name, "+
 			"grpings.criterion, "+
 			"grps.group_id, "+
 			"grps.group_number, "+
 			"grps.group_name, "+
 			"grps.has_expression_data, "+
 			"grps.has_genotype_data, "+
 			"grps.parent, "+
 			"count(cg.user_chip_id) "+          
                         "from chip_groups cg, "+ 
 			"groups grps, groupings grpings "+
                         "where grpings.grouping_id = grps.grouping_id "+
 			"and cg.group_id = grps.group_id "+ 
                         "and grps.has_expression_data = 'Y' "+
                         "and grpings.grouping_id = ? "+
 			"group by "+
 			"grpings.grouping_id, "+
 			"grpings.grouping_name, "+
 			"grpings.criterion, "+
 			"grps.group_id, "+
 			"grps.group_number, "+
 			"grps.group_name, "+ 
 			"grps.has_expression_data, "+
 			"grps.has_genotype_data, "+
 			"grps.parent "+
                         "order by to_number(grps.group_number)";
 
 		log.debug("in getGroupsInGrouping.grouping_id = " + grouping_id);
 		//log.debug("query = "+query);
         	List<Group> myGroupList = new ArrayList<Group>();
 
 		Results myResults = new Results(query, grouping_id, conn);
                 String[] dataRow;
 		while ((dataRow = myResults.getNextRow()) != null) {
                        	Group newGroup = new Group().setupGroup(dataRow);
                 	newGroup.setNumber_of_arrays(Integer.parseInt(dataRow[9]));
                         myGroupList.add(newGroup);
 		} 
         	myResults.close();
 
 		Group[] myGroupArray = (Group[]) myObjectHandler.getAsArray(myGroupList, Group.class);
 
   		return myGroupArray;
   	}
 
 	/**
 	 * Gets the ID of the user_chip record for this user's array.
 	 * @param hybrid_id 	the hybrid id of the array
 	 * @param user_id	the id of the user
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            the user_chip_id
 	 */
 	public int getUser_chip_id(int hybrid_id, int user_id, Connection conn) throws SQLException {
 
 		//log.debug("in getUser_chip_id. ");
 		//log.debug("hybrid_id = "+hybrid_id+", and user_id = "+user_id);
         	String query =
                 	"select user_chip_id "+
                 	"from user_chips  "+
                 	"where hybrid_id = ? "+
 			"and user_id = ?";
 
                 Results myResults = new Results(query, new Object[] {hybrid_id, user_id}, conn);
 
                 int user_chip_id = myResults.getIntValueFromFirstRow();
 
                 myResults.close();
 
         	return user_chip_id;
 	}
 
 	/**
 	 * Checks to see if a user_chip record exists for this user and hybrid_id.
 	 * @param hybrid_id     the hybrid id of the array
 	 * @param user_id     the id of the user
 	 * @param conn        the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            true if a record exists
 	 */
 	public boolean checkUserChipExists(int hybrid_id, int user_id, Connection conn) throws SQLException {
 
         	//log.debug("in checkUserChipExists. ");
 		//log.debug("hybrid_id = "+hybrid_id+", and user_id = "+user_id);
         	String query =
                 	"select user_chip_id "+
                 	"from user_chips  "+
                 	"where hybrid_id = ? "+
                 	"and user_id = ?";
 
                 Results myResults = new Results(query, new Object[] {hybrid_id, user_id}, conn);
 
                 int user_chip_id = myResults.getIntValueFromFirstRow();
 
 		//log.debug("user_chip_id = " +user_chip_id + ", so returning " + (user_chip_id == -99 ? false : true));
 
                 myResults.close();
 
 		return (user_chip_id == -99 ? false : true);
 
 	}
 
 	/**
 	 * Gets the hybridIDs for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            a Set of Strings containing the hybridIDs
 	 */
 	public Set getDatasetHybridIDsAsSet(Connection conn) throws SQLException {
 
         	String query =
                 	"select uc.hybrid_id "+
                 	"from user_chips uc, "+
 			"dataset_chips dc "+
                 	"where uc.user_chip_id = dc.user_chip_id "+
                 	"and dc.dataset_id = ? "+
                 	"order by uc.hybrid_id";
 
         	log.info("in getDatasetHybridIDsAsSet");
         	//log.debug("query = "+query);
 
         	Results myResults = new Results(query, this.getDataset_id(), conn);
 		Set hybridIDsSet = myObjectHandler.getResultsAsSet(myResults, 0);
 
 		myResults.close();
         	return hybridIDsSet;
 	}
 
 	/**
 	 * Gets the hybridIDs for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            a comma-separated list of hybrid IDs already contained in this dataset
 	 */
 	public String getDatasetHybridIDs(Connection conn) throws SQLException {
 
         	//log.info("in getDatasetHybridIDs");
 
         	Set hybridIDsSet = getDatasetHybridIDsAsSet(conn);
 		String hybridIDs = "(" +
         		myObjectHandler.getAsSeparatedString(hybridIDsSet, ",", "") +
                 	")";
 
 		if (hybridIDs.equals("()")) {
 			hybridIDs = "('')";
 		}
         	return hybridIDs;
 	}
 
 	/**
 	 * Gets the dataset_chips (the UserChip objects) for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            an array of UserChip objects in this Dataset 
 	 */
 	public User.UserChip[] getDatasetChips(Connection conn) throws SQLException {
 
         	String query =
                 	"select uc.user_chip_id, "+
 			"uc.hybrid_id, "+
 			"uc.owner_user_id "+
                 	"from user_chips uc, "+
 			"dataset_chips dc "+
                 	"where uc.user_chip_id = dc.user_chip_id "+
                 	"and dc.dataset_id = ? "+
                 	"order by uc.hybrid_id";
 
         	//log.info("in getDatasetChips");
         	//log.debug("query = "+query);
 
         	Results myResults = new Results(query, this.getDataset_id(), conn);
 
 		User.UserChip[] myUserChips = new User().setupUserChipValues(myResults); 
 
 		myResults.close();
         	return myUserChips;
 	}
 
 	/**
 	 * Constructs the name of the file on the file system containing the list of arrays in this Dataset for use by the Affymetrix Power Tools.
 	 * @return            a String containing the name (but no path) of the file
 	 */
 	public String getAPTFileListingName() {
 		return this.getNameNoSpaces() + ".APT.arrayFiles.txt";
 	}
 
 	/**
 	 * Constructs the name of the file on the file system containing the list of arrays in this Dataset.
 	 * @return            a String containing the name (but no path) of the file
 	 */
 	public String getFileListingName() {
 		return this.getNameNoSpaces() + ".arrayFiles.txt";
 	}
 
 	/**
 	 * Constructs the path where the files for the public Experiments are stored
 	 * @param userFilesRoot	the location of the userFiles directory for storing files.  
 	 * @return            a String containing the root path for public experiments
 	 */
 	public String getPublicExperimentsPath(String userFilesRoot) {
 		return userFilesRoot + "public/" + "Experiments/"; 
 	}
 
 	/**
 	 * Constructs the path where the files for the public Datasets are held.
 	 * @param userFilesRoot	the location of the userFiles directory for storing files.  
 	 * @return            a String containing the root path for public datasets
 	 */
 	public String getPublicDatasetsPath(String userFilesRoot) {
 		return userFilesRoot + "public/" + "Datasets/"; 
 	}
 
 	/**
 	 * Constructs the path where the files for the public user are held.
 	 * @param userMainDir	the current user's root directory for storing files.  This 
 	 *			changes the userMainDir to the public's userMainDir.
 	 * @return            a String containing the root path for the public user
 	 */
 	public String getPublicUserPath(String userMainDir) {
 		//log.debug("in getPublicUserPath. userMainDir = "+userMainDir);
 		String lastSlashRemoved = userMainDir.substring(0, userMainDir.lastIndexOf("/"));
 		//log.debug("lastSlashRemoved = "+lastSlashRemoved);
 		String nextSlashRemoved = lastSlashRemoved.substring(0,lastSlashRemoved.lastIndexOf("/"));
 		//log.debug("nextSlashRemoved = "+nextSlashRemoved);
 		return nextSlashRemoved + "/public/"; 
 	}
 
 	/**
 	 * Constructs the path where the files for this dataset will be held.
 	 * @param userMainDir	the user's root directory for storing files.  If the dataset is 
 	 *			owned by 'public', this changes the userMainDir to be public's userMainDir.
 	 * @return            a String containing the root path for this dataset.  
 	 */
 	public String getDatasetPath(String userMainDir) {
 		//log.debug("in getDatasetPath. userMainDir = "+userMainDir);
 		return (this.getCreator().equals("public") ?  getPublicUserPath(userMainDir) : userMainDir)  + 
 			"Datasets/"+ this.getNameNoSpaces() + "_Master" + "/"; 
 	}
 
 	/**
 	 * Constructs the path where the downloaded zip files will be held.
 	 * @return            a String containing the download path for this dataset 
 	 *			(e.g., DatasetName_Master/Downloads/)
 	 */
   	public String getDownloadsDir() {
 		return this.getPath() + "Downloads/";
   	}
 
 	/**
 	 * Constructs the path where the resource files will be held.
 	 * @return            a String containing the resource path for this dataset 
 	 *			(e.g., DatasetName_Master/Resources/)
 	 */
   	public String getResourcesDir() {
 		return this.getPath() + "Resources/";
   	}
 
 	/**
 	 * Gets the resources directory for the public dataset name passed in
 	 * @param publicDatasets	the array of Public Datasets
 	 * @param datasetName	the name of the public dataset
 	 * @return            a String containing the resource path for the dataset requested
 	 *			(e.g., DatasetName_Master/Resources/)
 	 */
   	public String getResourcesDir(Dataset[] publicDatasets, String datasetName) {
 		log.debug("in getResourcesDir. datasetName = "+datasetName + ", and there are "+publicDatasets.length + " public datasets");
 		Dataset thisDataset = getDatasetFromMyDatasets(publicDatasets, datasetName);
         	return (thisDataset != null ? thisDataset.getResourcesDir() : "");          
   	}
 
 	/**
 	 * Constructs the path where the phenotype files for the grouping will be held.
 	 * @return            a String containing the groupings path for this dataset 
 	 *			(e.g., DatasetName_Master/Groupings/)
 	 */
   	public String getGroupingsDir() {
 		return this.getPath() + "Groupings/";
   	}
 
 	/**
 	 * Constructs the path where the qc image files will be held.
 	 * @return            a String containing the images path for this dataset 
 	 *			(e.g., DatasetName_Master/Images/)
 	 */
   	public String getImagesDir() {
 		//log.debug("in getImagesDir. it is "+ this.getPath() + "Images/");
 		return this.getPath() + "Images/";
   	}
 
 
 	/**
 	 * Returns the files associated with clustering.  It gets all files ending in '.png', all files starting with 'groups',
 	 * and all files starting with 'Cluster'.
 	 * @param path	directory where the files are located
 	 * @return	an array of File objects that match the FilenameFilter
 	 */
 	public File[] getClusterFiles(String path) {
 		File [] files = new File(path).listFiles(new FilenameFilter() {
                                 		public boolean
                                                 	accept(File dir, String n) {
                                                         	return (new File(n).getName().matches("\\D*\\.png") ||
                                                                 	new File(n).getName().matches("groups.\\D+") ||
                                                                 	new File(n).getName().matches("\\D*Names\\.txt") ||
                                                                 	new File(n).getName().matches("\\D*Table\\.txt") ||
                                                                         new File(n).getName().matches("Cluster\\d*.txt") ||
                                                                         new File(n).getName().matches("Cluster\\D*.\\D+"));
 							}
 						});
 		return files;
 	}
 
 
 	/**
 	 * Removes a record from the datasets table, plus all records from related tables.  It 
 	 * also deletes the directory containing the dataset-specific files and the directories used for doing
 	 * statistical analysis in order to find differentially expressed genes.  <BR>
 	 * NOTE:  Prior to calling this method, this.setPath() must be called.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteDataset(int userID,Connection conn) throws SQLException, Exception {
  
 		int dataset_id = this.getDataset_id();
 		String dsPath = this.getPath();
 
 		conn.setAutoCommit(false);
   
 		log.info("in deleteDataset.  Dataset is "+this.getName() + ", and dsPath = "+dsPath);
 
   		deleteAllDatasetVersions(userID,conn);
 		deleteGroupsForDataset(conn);
 		deleteArraysForDataset(conn);
   	
 		String query = 
 			"delete from datasets " +
 			"where dataset_id = ? ";
     
 		try {
 			PreparedStatement pstmt = conn.prepareStatement(query, 
     					ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
     			pstmt.setInt(1, dataset_id);
 
     			pstmt.executeUpdate();
     			pstmt.close();
 			conn.commit();
 			boolean success = new FileHandler().deleteAllFilesPlusDirectory(new File(dsPath));
 			if (!success) {
 				Email myEmail = new Email();
 				myEmail.setSubject("Error deleting files for Dataset");
 				myEmail.setContent("Path is "+dsPath);
 				try {
 					myEmail.sendEmailToAdministrator("");
 				} catch (Exception e) {
 					log.error("error sending message to administrator");
 				}
 			}
 		} catch (SQLException e) {
 			log.error("In exception of deleteDataset", e);
 			conn.rollback();
 			throw e;
 		}
 		conn.setAutoCommit(true);
 	}
 
 	/**
 	 * Removes the groups, chip_groups, and groupings records for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteGroupsForDataset (Connection conn) throws SQLException {
 
 		log.info("in deleteGroupsForDataset. dataset_id = "+this.dataset_id); 
 		String[] query = new String[3];
 
 		query[0] =
 			"delete from chip_groups " +
 			"where dataset_id = ?";
 
 		query[1] =
 			"delete from groups " +
 			"where grouping_id in "+
 			"(select grouping_id "+
 			"from groupings "+
 			"where dataset_id = ?)";
 
 		query[2] =
 			"delete from groupings " +
 			"where dataset_id = ?";
 
 		for (int i=0; i<3; i++) {
 			log.debug("query = "+query[i]);
 			PreparedStatement pstmt = conn.prepareStatement(query[i],
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 			pstmt.setInt(1, this.dataset_id);
 			pstmt.executeUpdate();
 			pstmt.close();
 		}
 
 	}
 
 	/**
 	 * Removes the dataset_chips records for this dataset. 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void deleteArraysForDataset (Connection conn) throws SQLException {
 
 		log.info("in deleteArraysForDataset");
 		String query =
 			"delete from dataset_chips " +
 			"where dataset_id = ?";
 
 		log.debug("dataset_id = "+this.dataset_id +", query = "+query);
 		PreparedStatement pstmt = conn.prepareStatement(query,
 				ResultSet.TYPE_SCROLL_INSENSITIVE,
 				ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, this.dataset_id);
 		pstmt.executeUpdate();
 		pstmt.close();
 	}
 
 	/**
 	 * Removes the set of parameters used in a cluster analysis, plus deletes the directory containing 
 	 * the files. 
 	 * @param parameterGroupID	the identifier of the parameter group containing the values
 	 * @param clusterPath	the directory where the files are stored
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	 public void deleteClusterAnalysis (int parameterGroupID, String clusterPath, Connection conn) throws SQLException, Exception {
 
 		log.debug("in deleteClusterAnalysis. parameterGroupID = " + parameterGroupID + 
 				", and clusterPath = "+clusterPath);
 
   		new ParameterValue().deleteParameterValues(parameterGroupID, conn);
 
 		boolean success = new FileHandler().deleteAllFilesPlusDirectory(new File(clusterPath));
 		if (!success) {
 			Email myEmail = new Email();
 			myEmail.setSubject("Error deleting cluster files for "+clusterPath);
 			myEmail.setContent("Path is "+clusterPath);
 			try {
 				myEmail.sendEmailToAdministrator("");
 			} catch (Exception e) {
 				log.error("error sending message to administrator");
 			}
 		}
   	
   	}
 
 	/**
 	 * Removes a record from the dataset_chips table, based on the hybrid_id.
 	 * @param user_id	the identifier of the user
 	 * @param hybrid_id	the identifier of the array
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 
 	public void deleteDataset_chip(int user_id, int hybrid_id, Connection conn) throws SQLException {
 		log.debug("in deleteDataset_chip");
 
   		String query =
 			"delete from dataset_chips "+
 			"where dataset_id = ? "+
 			"and user_chip_id = "+
 			"(select user_chip_id "+
 			"from user_chips "+
 			"where user_id = ? "+
 			"and hybrid_id = ?)";
   	
 		//log.debug("query = "+query);
 
   		PreparedStatement pstmt = conn.prepareStatement(query, 
 					ResultSet.TYPE_SCROLL_INSENSITIVE,
 					ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, this.getDataset_id());
 		pstmt.setInt(2, user_id);
 		pstmt.setInt(3, hybrid_id);
 
 		pstmt.executeUpdate();
 		pstmt.close();
 
 		updateQc_complete("N", conn);
 
 	}
 
 	/**
 	 * Removes all of the records from the dataset_versions table for this dataset.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
  
 	public void deleteAllDatasetVersions(int userID,Connection conn) throws SQLException, Exception {
 		log.debug("in deleteAllDatasetVersions");
 	
   		String query =
 			"select version "+
 			"from dataset_versions "+
 			"where dataset_id = ? "+
 			"order by version";
   	
 		//log.debug("query = "+query);
         	Results myResults = new Results(query, this.dataset_id, conn);
 
 		log.debug("Number of versions to delete = "+myResults.getNumRows());
 
         	while ((dataRow = myResults.getNextRow()) != null) {
 			int version = Integer.parseInt(dataRow[0]);
 			(this.new DatasetVersion(version)).deleteDatasetVersion(userID,conn);
         	}
 		myResults.close();
 	}
   
 
 	/**
 	 * Sets up the userChips records for the user
 	 * @param	hybridIDs	an array of Strings containing the hybridIDs
 	 * @param	userLoggedIn	User object of the person logged in
 	 * @param	userFilesRoot	the location of the userFiles directory
 	 * @param	conn	the database connection
 	 * @throws            IOException if an IO error error occurs
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void setupArrayRecords (String[] hybridIDs, User userLoggedIn, String userFilesRoot, Connection conn) throws IOException, SQLException {
 
 		log.debug("in setupArrayRecords");
 
 	        Thread thread = null;
 
 	        List<edu.ucdenver.ccp.PhenoGen.data.Array> arrayList = new ArrayList <edu.ucdenver.ccp.PhenoGen.data.Array>();
                 List<String> hybridIDsWithoutFiles = new ArrayList<String>();
                 List<String> hybridIDsWithoutUserChips = new ArrayList<String>();
 		edu.ucdenver.ccp.PhenoGen.data.Array myArray = new edu.ucdenver.ccp.PhenoGen.data.Array();
 
 		String arrayDir = myArray.getArrayDataFilesLocation(userFilesRoot);
 		int userID = userLoggedIn.getUser_id();
 
                 String hybridIDsString = "(" +
                                 myObjectHandler.getAsSeparatedString(hybridIDs, ",", "") + ")";
 		log.debug("hybridIDsString = " + hybridIDsString);
 
 		edu.ucdenver.ccp.PhenoGen.data.Array[] myArraysToSetup = 
 			myArray.getArraysForDataset(hybridIDsString, conn);
 
                 for (int j=0; j<myArraysToSetup.length; j++) {
                         String fileName = myArraysToSetup[j].getFile_name();
                         int hybrid_id = myArraysToSetup[j].getHybrid_id();
                         //
                         // If the user doesn't have all of the CEL files in his/her directory (probably because
                         // the arrays are public arrays), then copy them to the directory
                         //
                         if (!(new File(arrayDir + fileName).exists())) {
 				//log.debug("file does not exist");
                                 hybridIDsWithoutFiles.add(Integer.toString(hybrid_id));
                         } else {
 				//log.debug("file does exist");
 			}
                         if (!checkUserChipExists(hybrid_id, userID, conn)) {
                                 hybridIDsWithoutUserChips.add(Integer.toString(hybrid_id));
                         }
                 }
                 if (hybridIDsWithoutFiles.size() > 0 || hybridIDsWithoutUserChips.size() > 0) {
                         Set<String> allHybridIDs = new TreeSet<String>(hybridIDsWithoutFiles);
                         allHybridIDs.addAll(hybridIDsWithoutUserChips);
                         String allHybridIDsString = "(" +
                                         myObjectHandler.getAsSeparatedString(allHybridIDs, ",", "") + ")";
                         log.debug("allHybridIDsString = "+allHybridIDsString);
 
                         edu.ucdenver.ccp.PhenoGen.data.Array[] myArraysWithoutFiles =
                                         myArray.getArraysByHybridIDs(allHybridIDsString, conn);
 
                 	Hashtable<String, String> user_arrays = new Hashtable<String, String>();
                         for (int j=0; j<myArraysWithoutFiles.length; j++) {
 				log.debug("this Array = "+myArraysWithoutFiles[j].getHybrid_id() + " " +myArraysWithoutFiles[j].getHybrid_name() + " and pubexpid = "+myArraysWithoutFiles[j].getPublicExpID());
                                 myArraysWithoutFiles[j].setAccess_approval(myArraysWithoutFiles[j].getPublicExpID() != null ? 1:0);
                                 user_arrays.put(Integer.toString(myArraysWithoutFiles[j].getHybrid_id()),
                                                 Integer.toString(myArraysWithoutFiles[j].getOwner_user_id()));
                                 //log.debug("Creating user_chip records for those public chips where user doesn't "+
                                  //               "already have files. hybrid_id = "+
                                   //              myArraysWithoutFiles[j].getHybrid_id());
                         }
                         userLoggedIn.setUser_chips(user_arrays);
                         userLoggedIn.createUser_chips(conn);
 
                         arrayList = Arrays.asList(myArraysWithoutFiles);
                         userLoggedIn.updateArrayApproval(arrayList, conn);
 
 			/*  As of R2.4 (March 2011), no longer need to copy files
                         if (hybridIDsWithoutFiles.size() > 0) {
                                 try {
                                         thread = new Thread(new AsyncCopyFiles(
                                                         arrayList,
 							userFilesRoot,
                                                         arrayDir));
 
                                         log.debug("Starting first thread "+ thread.getName());
 
                                         thread.start();
                                 } catch (Throwable t) {
                                         log.error("in setupArrayRecords.Exception thrown by AsyncCopyFiles", new Exception (t));
                                 }
                         }
 			*/
                 }
 	}
 
 
 	/**
 	 * Waits until all the files have been copied to the user's array directory.
 	 * @param	myArrays	the array of Arrays that need to be copied
 	 * @param	userFilesRoot	location of the userFiles directory
 	 * @throws	InterruptedException if interrupted
 	 * @return	"OK" if files are copied, "NOT OK" if there's a problem
 	 */
 	public String waitForFilesToCopy (edu.ucdenver.ccp.PhenoGen.data.Array[] myArrays, String userFilesRoot) throws InterruptedException {
 		boolean allFilesCopied = false;
 		int totalSleepTime = 0;
 		Email myErrorEmail = new Email();
 		String arrayDir = new edu.ucdenver.ccp.PhenoGen.data.Array().getArrayDataFilesLocation(userFilesRoot);
 		String answer = "OK";
                 while (!allFilesCopied) {
                 	for (int i=0; i<myArrays.length; i++) {
                         	if (new File(arrayDir + myArrays[i].getFile_name()).exists()) {
                                 	//log.debug("i = " + i + " this file is copied");
                                         allFilesCopied = true;
 				} else {
                                 	log.debug("i = " + i +" and all files still not copied");
                                         allFilesCopied = false;
                                         break;
 				}
                                 //log.debug("now i'm here");
 			}
                         log.debug("right before sleeping for 10 seconds");
                         Thread.sleep(10000);
                         totalSleepTime = totalSleepTime + 10000;
                         if (totalSleepTime > 60000) {
                         	log.debug("user has been waiting for over a minute for files to copy");
                                 myErrorEmail.setSubject("Files not copying for quality control");
                                 myErrorEmail.setContent("It's taking over a minute to copy files for "+this.getName()+
                                                         " to "+arrayDir);
 				try {
                                 	myErrorEmail.sendEmailToAdministrator("");
 				} catch (Exception error) {
                                 	log.error("exception while trying to send message to adminstrator about files not copying", error);
 				}
 				answer = "NOT OK";
                                 break;
 			}
                         log.debug("here and now sleepTime = " + totalSleepTime);
 		}
 		return answer;
 	}
 
 	/** 
 	 * Creates the arrayFiles.txt file in the file system.  This contains the names of all the array files in the dataset. This 
 	 * 		allows for a header to be included as well.
 	 * @param	userFilesRoot	the location of the userFiles directory
 	 * @param	withPath	true to include the path of the file
 	 * @param	header	text to include at the top of the file
 	 * @throws            IOException if an IO error error occurs
 	 */
 	public void createFileListing(String userFilesRoot, boolean withPath, String header) throws IOException {
 		log.debug("in createFileListing with header also");
 
                 BufferedWriter bufferedWriter = new FileHandler().getBufferedWriter(this.getPath() + this.getFileListingName()); 
 
                 bufferedWriter.write(header);
 		bufferedWriter.newLine();
 
 		writeArrayInformation(userFilesRoot, bufferedWriter, withPath);
 
 		bufferedWriter.flush();
 		bufferedWriter.close();
 	}
 
 	/**
 	 * Creates the arrayFiles.txt file in the file system.  This contains the names of all the array files in the dataset.
 	 * @param	userFilesRoot	the location of the userFiles directory
 	 * @param	withPath	true to include the path of the file
 	 * @throws            IOException if an IO error error occurs
 	 */
 	public void createFileListing (String userFilesRoot, boolean withPath) throws IOException {
 
 		log.debug("in createFileListing without header");
                 
                 
 
                 BufferedWriter bufferedWriter = new FileHandler().getBufferedWriter(this.getPath() + this.getFileListingName()); 
 
                 writeArrayInformation(userFilesRoot, bufferedWriter, withPath);
 
 		bufferedWriter.flush();
 		bufferedWriter.close();
 
 	}
 
 	private void writeArrayInformation(String userFilesRoot, BufferedWriter bufferedWriter, boolean withPath) throws IOException {
 		
 		edu.ucdenver.ccp.PhenoGen.data.Array[] myArrays = this.getArrays();
 		String arrayDir = new edu.ucdenver.ccp.PhenoGen.data.Array().getArrayDataFilesLocation(userFilesRoot);
                 if (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(this.getArray_type())) {//add cel_files header
                     bufferedWriter.write("cel_files\n");
                 }
                 
                 HashMap<String,String> hm=new HashMap<String,String>();
                 for (int j=0; j<myArrays.length; j++) {
                     String arrayName = myArrays[j].getHybrid_name().replaceAll("[\\s]", "_");
                     if(hm.containsKey(arrayName)){
                         if(hm.get(arrayName).equals("-")){
                             hm.put(arrayName, "A");
                         }
                     }else{
                         hm.put(arrayName, "-");
                     }
                 }
                 
 		for (int j=0; j<myArrays.length; j++) {
 			String fileName = myArrays[j].getFile_name();
 			String arrayName = myArrays[j].getHybrid_name().replaceAll("[\\s]", "_");
                         if(hm.containsKey(arrayName)&&!hm.get(arrayName).equals("-")){
                             //need to add a letter to end of arrayname
                             String letter=hm.get(arrayName);
                             String oldName=arrayName;
                             arrayName=arrayName+"_"+letter;
                             char nextLetter=letter.charAt(0);
                             nextLetter++;
                             hm.put(oldName, Character.toString(nextLetter));
                         }
                         String fullFileName = (withPath ? arrayDir + fileName : fileName);
 
 			if (this.getPlatform().equals(AFFYMETRIX_PLATFORM) ||
 					this.getPlatform().equals(CODELINK_PLATFORM)) {
 				bufferedWriter.write(fullFileName+ "\t" + arrayName);
                         } else if (this.getPlatform().equals("cDNA")) {
 				//
                                 // For cDNA datasets, write out the file name,
                                 // three null columns, and a
                                 // sequence number.
                                 //
                                 bufferedWriter.write(fullFileName + "\t" +
                                                 "NULL\t" +
                                                 "NULL\t" +
                                                 "NULL\t" +
                                                 j);
                         }
 			bufferedWriter.newLine();
 		}
 	}
 
 
 
 	/**
 	 * Creates a 'shell' record in the datasets table.  Will be converted to a regular dataset 
 	 * record when the user fills provides a real name and description.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return the Dataset object created with the dataset_id, platform, organism, and created_by_user_id set
 	 */
 	public Dataset createDummyDataset (Connection conn) throws SQLException {
 
 		int dataset_id = myDbUtils.getUniqueID("datasets_seq", conn);
 
         	String query =
                 	"insert into datasets "+
                 	"(dataset_id, name, description, create_date, created_by_user_id, "+
 			"platform, qc_complete, organism) values "+
                 	"(?, ?, ?, ?, ?, "+
 			"?, ?, ?)";
 
 	
 		log.debug("in Dataset.createDummyDataset. dataset_id = " + dataset_id);
 
 		java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
         	PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
         	pstmt.setInt(1, dataset_id); 
         	pstmt.setString(2, "Dummy" + dataset_id);
         	pstmt.setString(3, "DummyDescription" + dataset_id);   
 
         	// Column 4 is the create_date
         	pstmt.setTimestamp(4, now);
         	pstmt.setInt(5, this.getCreated_by_user_id()); 
         	pstmt.setString(6, this.getPlatform());
         	pstmt.setString(7, "N");
         	pstmt.setString(8, this.getOrganism());
 
         	pstmt.executeUpdate();
 		pstmt.close();
 
 		this.setDataset_id(dataset_id);
 
 		return this;
 	}
 
 	/**
 	 * Updates the 'shell' dataset record with the real name and description.  
 	 * @param dataset_id	the id of the dataset to be updated
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 
 	public void updateDummyDataset (int dataset_id, Connection conn) throws SQLException {
 
         	String query =
                 	"update datasets "+
 			"set name = ?, "+
 			"description = ? "+
 			"where dataset_id = ?";
 	
 		log.debug("in Dataset.updateDummyDataset. dataset_id = " + dataset_id);
 
         	PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
 
         	pstmt.setString(1, this.getName());
         	pstmt.setString(2, this.getDescription());
         	pstmt.setInt(3, dataset_id);
 
         	pstmt.executeUpdate();
 		pstmt.close();
 
 	}
 
 	/**
 	 * Creates a record in the datasets table.
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return the id assigned to the dataset
 	 */
 
 	public int createDataset (Connection conn) throws SQLException {
 
 		int dataset_id = myDbUtils.getUniqueID("datasets_seq", conn);
 
         	String query =
                 	"insert into datasets "+
                 	"(dataset_id, name, description, create_date, created_by_user_id, "+
 			"platform, qc_complete, organism) values "+
                 	"(?, ?, ?, ?, ?, "+
 			"?, ?, ?)";
 
 	
 		log.debug("in Dataset.createDataset. dataset_id = " + dataset_id);
 
 		java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
         	PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
         	pstmt.setInt(1, dataset_id); 
         	pstmt.setString(2, this.getName());
         	pstmt.setString(3, this.getDescription());   
 
         	// Column 4 is the create_date
         	pstmt.setTimestamp(4, now);
         	pstmt.setInt(5, this.getCreated_by_user_id()); 
         	pstmt.setString(6, this.getPlatform());
         	pstmt.setString(7, this.getQc_complete());
         	pstmt.setString(8, this.getOrganism());
 
         	pstmt.executeUpdate();
 		pstmt.close();
 
 		return dataset_id;
 	}
 
 	/**
 	 * Creates a record in the dataset_chips table.
 	 * @param user_chip_id	the ID of the record in the user_chips_table that has been chosen for this dataset
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 
 	public void createDataset_chip(int user_chip_id, Connection conn) throws SQLException {
         	//log.info("in createDataset_chip. user_chip_id = " + user_chip_id + ", dataset_id = "+this.getDataset_id());
 
         	String query =
                 	"insert into dataset_chips "+
                 	"(dataset_id, user_chip_id) "+
                 	"values "+
                 	"(?, ?)";
 
         	PreparedStatement pstmt = conn.prepareStatement(query,
                                         	ResultSet.TYPE_SCROLL_INSENSITIVE,
                                         	ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, this.getDataset_id());
         	pstmt.setInt(2, user_chip_id);
 
         	pstmt.executeUpdate();
         	pstmt.close();
 
   	}
 
 	/**
 	 * Creates a record in the groupings table and records in the groups table.
 	 * @param criterion	the criterion used for creating the grouping
 	 * @param grouping_name	the name of the grouping
 	 * @param groupValues	a HashMap containing the user_chip_id mapped to the group number
 	 * @param groupNames	a Hashtable containing the group number mapped to the group label
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return the id assigned to the grouping
 	 */
 
 	public int createNewGrouping(String criterion, String grouping_name, 
 			LinkedHashMap groupValues, Hashtable groupNames, Connection conn) throws SQLException {
 
         	log.info("in createNewGrouping. ");
                 //log.debug("groupNames = "); myDebugger.print(groupNames);
                 //log.debug("groupValues = "); myDebugger.print(groupValues);
 		int grouping_id = -99;
 
 		conn.setAutoCommit(false);
                 try {
                 	grouping_id = createGrouping(criterion, grouping_name, conn);
 			//log.debug("here grouping_id = "+grouping_id);
                         //
                         // Create groups and chip_groups records containing the user_chip_id and the group
                         // chosen
 			//
 
                         for (Iterator itr = groupValues.keySet().iterator(); itr.hasNext();) {
                         	int user_chip_id = Integer.parseInt((String) itr.next());
                         	//int user_chip_id = (Integer) itr.next();
 				//log.debug("user_chip_id = "+user_chip_id);
                                 int group_num = Integer.parseInt((String) groupValues.get(Integer.toString(user_chip_id)));
                                 //int group_num = (Integer) groupValues.get(user_chip_id);
 				//log.debug("group_num = "+group_num);
 				//log.debug("groupName = "+(String)groupNames.get(Integer.toString(group_num)));
                                 int group_id = createGroup(grouping_id,
                                 				group_num,
 								(String) groupNames.get(Integer.toString(group_num)),
                                                                 conn);
 				//log.debug("group_id = "+group_id);
 				createChip_group(user_chip_id, group_id, conn);
 			}
                         conn.commit();
 		} catch (Exception e) {
                 	log.debug("got Exception while creating grouping", e);
                         conn.rollback();
 		}
 		conn.setAutoCommit(true);
 
 		return grouping_id;
   	}
 
 	/**
 	 * Creates a record in the groupings table.
 	 * @param criterion	the criterion used for creating the grouping
 	 * @param grouping_name	the name of the grouping
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return the id assigned to the grouping
 	 */
 
 	public int createGrouping(String criterion, String grouping_name, Connection conn) throws SQLException {
         	log.info("in createGrouping. ");
 		log.debug("criterion = " + criterion + ", grouping_name = "+grouping_name +", dataset_id = "+this.getDataset_id());
 
 		int grouping_id = myDbUtils.getUniqueID("groupings_seq", conn);
 		log.debug("grouping_id = "+grouping_id);
 
         	String query =
                 	"insert into groupings "+
                 	"(grouping_id, dataset_id, criterion, grouping_name) "+
                 	"values "+
                 	"(?, ?, ?, ?)";
 
         	PreparedStatement pstmt = conn.prepareStatement(query,
                                         	ResultSet.TYPE_SCROLL_INSENSITIVE,
                                         	ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, grouping_id);
 		pstmt.setInt(2, this.getDataset_id());
         	pstmt.setString(3, criterion);
         	pstmt.setString(4, grouping_name);
 
         	pstmt.executeUpdate();
         	pstmt.close();
 
 		return grouping_id;
   	}
 
 	/**
 	 * Creates a record in the groups table.
 	 * @param grouping_id	the identifier of the grouping to which this group belongs
 	 * @param group_number	an integer to identify the order of the group
 	 * @param group_name	the name of the group
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return the id assigned to the group
 	 */
 
 	public int createGroup(int grouping_id, int group_number, String group_name, Connection conn) throws SQLException {
         	//log.info("in createGroup. ");
 		//log.debug("grouping_id = " + grouping_id + ", group_number = "+group_number);
 
 		int group_id = myDbUtils.getUniqueID("groups_seq", conn);
 
         	String query =
                 	"insert into groups "+
                 	"(group_id, grouping_id, group_number, group_name, has_expression_data, has_genotype_data, parent) "+
                 	"values "+
                 	"(?, ?, ?, ?, 'Y', 'N', '')";
 
         	PreparedStatement pstmt = conn.prepareStatement(query,
                                         	ResultSet.TYPE_SCROLL_INSENSITIVE,
                                         	ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, group_id);
 		pstmt.setInt(2, grouping_id);
         	pstmt.setInt(3, group_number);
         	pstmt.setString(4, group_name);
 
 		try {
         		pstmt.executeUpdate();
 		} catch (SQLException e) {
 			if (e.getErrorCode() == 1) {
 				group_id = getGroupID(grouping_id, group_number, conn); 
 			} else {
 				log.error("Got a SQLException while in createGroup for group_number = " +
                                 		group_number + ", and grouping_id = "+grouping_id);
                         	log.debug("Error code = "+e.getErrorCode());
                                 throw e;
 			}
 		}
         	pstmt.close();
 		//log.debug("group_id = "+group_id);
 
 		return group_id;
   	}
 
 	/**
 	 * Retrieves the group_id for a group_number in a grouping
 	 * @param grouping_id	the id of grouping
 	 * @param group_number	the number of the group
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	the group_id
 	 */
 
 	public int getGroupID(int grouping_id, int group_number, Connection conn) throws SQLException {
 
         	String query =
 			"select group_id "+
 			"from groups "+
 			"where grouping_id = ? "+
 			"and group_number = ?";
 
 		Results myResults = new Results(query, new Object[] {grouping_id, group_number}, conn);
 
 		int group_id = -99;
         	while ((dataRow = myResults.getNextRow()) != null) {
         		group_id = Integer.parseInt(dataRow[0]);
 		}
 
         	myResults.close();
 
   		return group_id;
 	}
 
 
 	/**
 	 * Creates a record in the chip_groups table.
 	 * @param user_chip_id	the ID of the record in the user_chips_table that has been chosen for this dataset
 	 * @param group_id	the ID of the group that has been chosen for this chip
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 
 	public void createChip_group(int user_chip_id, int group_id, Connection conn) throws SQLException {
         	//log.info("in createChip_group. user_chip_id = " + user_chip_id + 
 		//	", group_id = " + group_id + ", dataset_id = "+this.getDataset_id());
 
         	String query =
                 	"insert into chip_groups "+
                 	"(dataset_id, user_chip_id, group_id) "+
                 	"values "+
                 	"(?, ?, ?)";
 
         	PreparedStatement pstmt = conn.prepareStatement(query,
                                         	ResultSet.TYPE_SCROLL_INSENSITIVE,
                                         	ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, this.getDataset_id());
         	pstmt.setInt(2, user_chip_id);
         	pstmt.setInt(3, group_id);
 
         	pstmt.executeUpdate();
         	pstmt.close();
 
   	}
 
         /**
          * Gets the datasets that use this list of hybridIDs in this database. 
          * @param hybridIDs        a list of identifiers for arrays
          * @param conn  the database connection
          * @throws      SQLException if a database error occurs
          * @return      An array of Datasets that include the arrays specified. 
          */
         public Dataset[] getDatasetsUsingArrayIDs(String hybridIDs, Connection conn) throws SQLException {
 
                 log.info("in getDatasetsUsingArrayIDs. hybridIDs = " + hybridIDs);
 
                 String query = 
                         "select distinct dc.dataset_id "+
                         "from user_chips uc, "+
                         "dataset_chips dc, "+
                         "datasets ds "+
                         "where uc.user_chip_id = dc.user_chip_id "+
 			"and dc.dataset_id = ds.dataset_id "+
 			"and ds.name != 'Dummy'||ds.dataset_id "+
                         "and uc.hybrid_id in "+
                         hybridIDs + 
                 	" order by dc.dataset_id";
 
                 log.debug("query = "+query);
 
                 int i=0;
 
                 Results myResults = new Results(query, conn);
                 Dataset[] myDatasetArray = new Dataset[myResults.getNumRows()];
 
                 while ((dataRow = myResults.getNextRow()) != null) {
                         int dataset_id = Integer.parseInt(dataRow[0]);
                         //log.debug("getting dataset for dataset_id = "+dataset_id);
 
                         String fromString =
                                 "from datasets ds left join "+
                                 "dataset_chips dc on ds.dataset_id = dc.dataset_id, "+
                                 "users u, "+
                                 "organisms org ";
                         query =
                                 datasetSelectClause +
                                 fromString + " "+
                                 "where ds.dataset_id = ? "+
                                 "and ds.created_by_user_id = u.user_id "+
                                 "and ds.organism = org.organism "+
 				"and ds.name != 'Dummy'||ds.dataset_id "+
                                 datasetGroupByClause;
 
                         log.debug("query = "+query);
 
                         Results myResults2 = new Results(query, dataset_id, conn);
 			if (myResults2 != null && myResults2.getNumRows() > 0) {
                         	String[] dataRow2 = null;
                         	while ((dataRow2 = myResults2.getNextRow()) != null) {
                                 	myDatasetArray[i] = setupDatasetValues(dataRow2);
                         	}
                         	i++;
 			}
                         myResults2.close();
                 }
 
                 myResults.close();
                 return myDatasetArray;
         }
 
 	/**
 	 * Gets the expected duration for a set of programs.
 	 * @param num_arrays	the number of arrays in the dataset
 	 * @param num_probes	the number of probes still remaining
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            a Hashtable showing the program and the number of seconds the program is expected to run
 	 */
 	public Hashtable getExpectedDuration(int num_arrays, int num_probes, Connection conn) throws SQLException {
 
         	String query =
                 	"select program, round(avg_duration * 1.25) "+
                 	"from run_times "+
 			"where num_arrays_low <= ? "+
 			"and num_arrays_high >= ? "+
 			"and num_probes_low <= ? "+
 			"and num_probes_high >= ? "+
 			"order by program";
 
 		String[] dataRow;
 		Hashtable<String, String> myHash = new Hashtable<String, String>();
 
 		//log.debug("in getExpectedDuration query = " + query);
 
 		Results myResults = new Results(query, new Object[] {num_arrays, num_arrays, num_probes, num_probes}, conn);
 
         	while ((dataRow = myResults.getNextRow()) != null) {
 			myHash.put(dataRow[0], dataRow[1]);
 		}
 		myResults.close();
 
         	return myHash;
 	}
 
 	/**
 	 * Gets the expected duration for a single program.
 	 * @param program	the name of the program
 	 * @param num_arrays	the number of arrays in the dataset
 	 * @param num_probes	the number of probes still remaining
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            the number of seconds the program is expected to run
 	 */
 	public int getExpectedDuration(String program, int num_arrays, int num_probes, Connection conn) throws SQLException {
 
         	String query =
                 	"select round(avg_duration * 1.25) "+
                 	"from run_times "+
 			"where num_arrays_low <= ? "+
 			"and num_arrays_high >= ? "+
 			"and num_probes_low <= ? "+
 			"and num_probes_high >= ? "+
 			"and program = ?";
 
 		String[] dataRow;
 		Hashtable myHash = new Hashtable();
 
 		//log.debug("in getExpectedDuration passing in program name.  query = " + query);
 
 		Results myResults = new Results(query, new Object[] {num_arrays, num_arrays, num_probes, num_probes, program}, conn);
 		int expectedDuration = 0;
 
         	while ((dataRow = myResults.getNextRow()) != null) {
 			expectedDuration = Integer.parseInt(dataRow[0]);
 		}
 		myResults.close();
 
         	return expectedDuration;
 	}
 
 	/**
 	 * Update the time it took to run the program
 	 * @param program	the name of the program
 	 * @param num_arrays	the number of arrays in the dataset
 	 * @param num_probes	the number of probes still remaining
 	 * @param duration	the time it took to run
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void updateDuration(String program, int num_arrays, int num_probes, int duration, Connection conn) throws SQLException {
 
 		String query = 
 			"update run_times "+
 			"set avg_duration = ((avg_duration * num_times_run) + ?)/(num_times_run + 1), "+
 			"num_times_run = num_times_run + 1 "+
 			"where program = ? "+
 			"and num_arrays_low <= ? "+
 			"and num_arrays_high >= ? "+
 			"and num_probes_low <= ? "+
 			"and num_probes_high >= ?";
 
 		log.debug("in updateDuration."); 
 		//log.debug("query = "+query);
 		//log.debug("program = "+program + " and duration = "+duration +
 		//		", and num_arrays = "+num_arrays +", and num_probes = "+num_probes);
   		PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
 		pstmt.setInt(1, duration);
 		pstmt.setString(2, program);
 		pstmt.setInt(3, num_arrays);
 		pstmt.setInt(4, num_arrays);
 		pstmt.setInt(5, num_probes);
 		pstmt.setInt(6, num_probes);
 		pstmt.executeUpdate();
 		pstmt.close();
 
 	}
 
 	/**
 	 * Checks to see if the dataset name being created already exists.
 	 * @param expName	the name of the new dataset
 	 * @param userID	the id of the user creating the dataset
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            true if a dataset by this name already exists, or false otherwise
 	 */
 	public boolean datasetNameExists(String expName, int userID, Connection conn) throws SQLException {
 
         	String query =
                 	"select ds.dataset_id "+
                 	"from datasets ds "+
                 	"where ds.name = ? "+
 			"and ds.created_by_user_id = ? "+
 			"and ds.name != 'Dummy'||ds.dataset_id";
 
 		boolean alreadyExists = false;
 
         	Results myResults = new Results(query, new Object[] {expName, userID},  conn);
 
         	if (myResults.getNumRows() != 0) {
 			alreadyExists = true;
 		}
 		myResults.close();
 
         	return alreadyExists;
 	}
 
 	/**
 	 * Checks to see if the particular combination of grouping parameters already exists.
 	 * @param groupValues	user_chip_ids mapped to the group values for the chips in the dataset version
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            the id of the grouping that already exists
 	 */
 	public int checkGroupingExists(
 				LinkedHashMap groupValues,
 				Connection conn) throws SQLException {
 
 		//
 		// First get the grouping values for the dataset
 		// Then loop through the groups and check their values.
         	//
 		log.debug("in checkGroupingExists");
 		//log.debug("groupValues = "); myDebugger.print(groupValues);
 
 		Group[] groupings = getGroupings(conn);
 		int answer = -99;
 		for (int i=0; i<groupings.length; i++) {
 			answer = checkGroupValues(groupValues, new Group().getChipAssignments(groupings[i].getGrouping_id(), conn));
 			// If an existing grouping is found, break out of this loop;
 			if (answer != -99) {
 				break;
 			}
 		}
 
 		log.debug("answer = "+answer);
 
         	return answer;
 	}
 
 	/**
 	 * Checks to see if the particular combination of normalization parameters already exists.
 	 * @param normalize_method	the normalization method of the new dataset version
 	 * @param grouping_id	the identifier that contains the grouping information
 	 * @param probeMask	whether a probeMask was applied
 	 * @param analysis_level	either 'probeset' or 'transcript'.  Used only for exon arrays
 	 * @param annotation_level	either 'core' or 'extended' or 'full'.  Used only for exon arrays
 	 * @param codeLinkParameter1	an additional normalization parameter for CodeLink datasets
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return            the name of the version that already exists
 	 */
 	public String checkNormalizationExists(
 				String normalize_method, 
 				int grouping_id,
 				String probeMask,
 				String analysis_level,
 				String annotation_level,
 				String codeLinkParameter1,
 				Connection conn) throws SQLException {
 
 		log.debug("in checkNormalizationExists");
 
 		String methodName = "Normalization Method";
 
 		if (this.getPlatform().equals("cDNA") || this.getPlatform().equals(CODELINK_PLATFORM)) {
 			methodName = this.getPlatform() + " " +methodName;
 		}
 
 		String platformClause = (this.getPlatform().equals(CODELINK_PLATFORM) ? 
 				"and exists(select 'x' "+
 					"from parameter_values pv1 "+
         				"where pv1.value = ? "+
         				"and pg.parameter_group_id = pv1.parameter_group_id "+
         				"and pv1.parameter = 'CodeLink Normalization Parameter 1') " : 
 				// For Affymetrix
 				"and exists( select 'x' "+
                 		"	from parameter_values pv1 "+
                 		"	where pv1.value = ? "+
                 		"	and pg.parameter_group_id = pv1.parameter_group_id "+
                 		"	and pv1.parameter = 'Probe Mask Applied') ");
 
 		String exonClause = (new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(this.getArray_type()) ?
 				"and exists(select 'x' "+
 					"from parameter_values pv2 "+
         				"where pv2.value = ? "+
         				"and pg.parameter_group_id = pv2.parameter_group_id "+
         				"and pv2.parameter = 'Analysis Level') "+  
 				"and exists(select 'x' "+
 					"from parameter_values pv3 "+
         				"where pv3.value = ? "+
         				"and pg.parameter_group_id = pv3.parameter_group_id "+
         				"and pv3.parameter = 'Annotation Level') " : " "); 
 
         	String query =
                 	"select ds.dataset_id, "+
 			"dv.version, "+
 			"dv.version_name "+
                 	"from datasets ds, "+
                 	"dataset_versions dv, "+
                 	"parameter_values normalize_method, "+
                 	"parameter_groups pg "+
                 	"where ds.dataset_id = ? "+
                 	"and pg.parameter_group_id = normalize_method.parameter_group_id "+
 			"and normalize_method.parameter = '"+
 			methodName +
 			"' "+
 			"and normalize_method.value = ? "+
                 	"and dv.dataset_id = pg.dataset_id "+
                 	"and dv.version = pg.version "+
                 	"and ds.dataset_id = dv.dataset_id "+
 			"and dv.grouping_id = ? "+
                 	"and pg.master = 1 "+
 			"and ds.name != 'Dummy'||ds.dataset_id "+
 			"and dv.visible != -1 "+
 			platformClause +
 			exonClause +
 			"group by ds.dataset_id, dv.version, dv.version_name "+
 			"order by dv.version";
 
 		//log.debug("query = " + query);
 
 		Results myResults = (this.getPlatform().equals(CODELINK_PLATFORM) ? 
 					new Results(query, new Object[] {this.getDataset_id(), normalize_method, grouping_id, codeLinkParameter1}, conn) :
 					(new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(this.getArray_type()) ?
 						new Results(query, new Object[] {this.getDataset_id(), normalize_method, grouping_id, probeMask, analysis_level, annotation_level}, conn) :
 						new Results(query, new Object[] {this.getDataset_id(), normalize_method, grouping_id, probeMask}, conn)));
 
 		String answer = "";
 
 		if (myResults.getNumRows() != 0) {
         		while ((dataRow = myResults.getNextRow()) != null) {
 				answer = dataRow[2];
 			}
 		} else {
 			log.debug("no version rows were returned");
 		}
 
         	myResults.close();
 
         	return answer;
 	}
 
 	/**
 	 * Checks to see if the group values match.
 	 * @param groupValues	user_chip_ids mapped to the group values for the chips in the dataset version
 	 * @param myChipAssignments	the chip assignments for a particular grouping
 	 * @return            the id of the grouping that already exists
 	 */
 
 	private int checkGroupValues(LinkedHashMap groupValues, User.UserChip[] myChipAssignments) {
 
 		log.debug("in checkGroupValues");
 		//log.debug("myChipAssignments = "); myDebugger.print(myChipAssignments);
 
 		int answer = -99;
 		for (int i=0; i<myChipAssignments.length; i++) {
 			String oldGroup = Integer.toString(myChipAssignments[i].getGroup().getGroup_number()); 
 			String newGroup = ((String) groupValues.get(Integer.toString(myChipAssignments[i].getUser_chip_id())));
 			//log.debug("oldGroup = "+oldGroup+", newGroup="+newGroup);
 			if (oldGroup.equals(newGroup)) {
 				answer = myChipAssignments[i].getGroup().getGrouping_id(); 
 			} else {
 				answer = -99;
 				//log.debug("breaking out of loop because oldGroup doesn't equal newGroup");
 				break;
 			}
         	}
 		log.debug("answer is "+answer);
 		return answer;
 	}
 
 	/**
 	 * Gets the public datasets with the groupings that contain genotype information needed to calculate QTLs.
 	 * It is ordered by the dataset name and dataset version.
 	 * @param publicDatasets	an array of public datasets
 	 * @return	an array of Dataset objects
 	 */
 
 	public Dataset[] getPublicDatasetsForQTL(Dataset[] publicDatasets) {
 
 		log.debug("in getPublicDatasetsForQTL");
 
   		List<Dataset> datasetList = new ArrayList<Dataset>();
 
 		for (Dataset thisDataset :publicDatasets) {
 			if (CALC_QTL_DATASETS.contains(thisDataset.getName())) {
 				datasetList.add(thisDataset);
 			}
 		}
 
                 Dataset[] myDatasets = myObjectHandler.getAsArray(datasetList, Dataset.class);
         	myDatasets = sortDatasets(myDatasets, "datasetName", "A");
         	return myDatasets;
 	}
 
         /**
          * Gets either the private datasets for a user, or the public datasets (which have the user's private analysis results).  
 	 * @param allDatasets	an array of Datasets retrieved from getAllDatasetsForUser 
 	 * @param type	either 'private' or 'public'
          * @return      an array of Dataset objects
          */
         public Dataset[] getDatasetsForUser(Dataset[] allDatasets, String type) { 
 
 		List<Dataset> datasetList = new ArrayList<Dataset>();
 		for (Dataset dataset : allDatasets) {
 			if (type.equals("public") && dataset.getCreator().equals("public")) {
 				datasetList.add(dataset);
 			} else if (type.equals("private") && !dataset.getCreator().equals("public")) {
 				datasetList.add(dataset);
 			}
 		}
                 Dataset[] datasetArray = myObjectHandler.getAsArray(datasetList, Dataset.class);
                 return datasetArray;
 	}
 
 	/**
 	 * Retrieves all the datasets and dataset versions created by a user, whether they are visible or not.  
 	 * It is ordered by the dataset name and dataset version.
 	 * @param userLoggedIn	the User object of the user logged in
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	an array of Dataset objects
 	 */
 
 	public Dataset[] getAllDatasetsForUser(User userLoggedIn, Connection conn) throws SQLException {
 
 		int user_id  = userLoggedIn.getUser_id();
 
 		log.debug("in getAllDatasetsForUser user_id = " + user_id);
 
 		String query = 
 /*
 			datasetVersionSelectClause + 
 			datasetVersionFromClause + 
 			datasetVersionWhereClause; 
 */
 
 			datasetVersionDetailsSelectClause + 
 			datasetVersionDetailsFromClause + 
 			datasetVersionWhereClause; 
 
 
 			query = query +
 			"and (ds.created_by_user_id = ? "+
 			"or ds.created_by_user_id = "+
 			"	(select user_id "+
 			"	from users "+
 			"	where user_name = 'public')) ";
 			// March 25, 2011 I have no idea why this is here!
 //			"and dv.number_of_groups is not null ";
 		
 			query = query +	
 //	datasetVersionGroupByClause + 
 				datasetVersionDetailsGroupByClause +  
 				"order by ds.create_date desc, ds.name";
 
 		//log.debug("query  = " + query );
 
 		Dataset[] datasetArray = null;
 
 		Results myResults = (new Results(query, user_id, conn));
 
 		datasetArray = setupDatasetVersionValues(myResults, true);
 
 		ParameterValue[] allParameterValues = new ParameterValue().getParameterValuesForAllDatasetsForUser(user_id, conn);
 		GeneList[] allGeneLists = new GeneList().getGeneListsForAllDatasetsForUser(user_id, conn);
 
 		//log.debug("allParameterValues is this long: " + allParameterValues.length);
 		//log.debug("allGeneLists is this long: " + allGeneLists.length);
 	
 		for (Dataset thisDataset: datasetArray) {
 			thisDataset.setPath(thisDataset.getDatasetPath(userLoggedIn.getUserMainDir()));
 			for (DatasetVersion thisVersion: thisDataset.getDatasetVersions()) {
 				thisVersion.setVersion_path(thisDataset.getPath(), thisVersion.getVersion());
 				List<ParameterValue> pvList = new ArrayList<ParameterValue>();
 				List<GeneList> glList = new ArrayList<GeneList>();
 				for (ParameterValue thisParameterValue : allParameterValues) {
 					if (thisParameterValue.getDataset_id() == thisDataset.getDataset_id() &&
 						thisParameterValue.getVersion() == thisVersion.getVersion()) { 
 						pvList.add(thisParameterValue);
 					}
 				}
 				for (GeneList thisGeneList : allGeneLists) {
 					if (thisGeneList.getDataset_id() == thisDataset.getDataset_id() &&
 						thisGeneList.getVersion() == thisVersion.getVersion()) {
 						glList.add(thisGeneList);
 					}
 				}
 				ParameterValue[] pvArray = (ParameterValue[]) myObjectHandler.getAsArray(pvList, ParameterValue.class);
 				GeneList[] glArray = (GeneList[]) myObjectHandler.getAsArray(glList, GeneList.class);
 				glArray = new GeneList().sortGeneLists(glArray, "geneListName", "A");
 				thisVersion.setParameters(pvArray);
 				thisVersion.setGeneLists(glArray);
 			}
                         setupDatasetParameterValues(userLoggedIn.getUser_id(), thisDataset, conn);
 		}
 		
 		myResults.close();
 
         	return datasetArray;
 	}
 
 	/** Check array of parameters to see if there is one that matches the user_id for this parameter_group_id and user_id.
 	 * @param user_id	the identifier of the user logged in
 	 * @param parameter_group_id	the parameter_group_id to match on
 	 * @return 	true if a user_id is found, otherwise false
 	 */
 	public boolean checkForUserID(int user_id, ParameterValue[] myParameters, int parameter_group_id) {
 		//log.debug("in checkForUserID. user_id = " + user_id + ", parameter_group_id = "+parameter_group_id + ", myParameters = ");
 		//myDebugger.print(myParameters);
 		for (ParameterValue userParameter : myParameters) {
 			if (userParameter.getParameter_group_id() == parameter_group_id &&
 				userParameter.getParameter().equals("User ID") &&
 				userParameter.getValue().equals(Integer.toString(user_id))) {
 				//log.debug("returning true");
 				return true;
 			}
 		}
 		//log.debug("returning false");
 		return false;
 	}
 
 	/**
 	 * Sets the parameter value-related attributes for the dataset and the dataset versions
 	 * @param user_id	the identifier of the user logged in
 	 * @param thisDataset	a Dataset object
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	an array of Dataset objects
 	 */
 
 	public Dataset setupDatasetParameterValues(int user_id, Dataset thisDataset, Connection conn) throws SQLException {
 		log.debug("in setupDatasetParameterValues for a dataset");
 		for (DatasetVersion thisDatasetVersion : thisDataset.getDatasetVersions()) {
 			thisDatasetVersion.setParameters(new ParameterValue().getParameterValuesForDatasetVersion(thisDatasetVersion, conn));
 			thisDatasetVersion.setGeneLists(new GeneList().getGeneListsForDatasetVersion(thisDatasetVersion, conn));
 			thisDatasetVersion.setGroupCounts(thisDatasetVersion.getGroupCounts(conn));
 		}
 		return thisDataset;
 	} 
 
 	/**
 	 * Creates an array of Dataset objects and sets the data values to those retrieved from the database.
 	 * @param myResults	the result set of data returned from the database 
 	 * @param withDetails	0 indicates details are not included, 1 they are
 	 * @return            an array of Dataset objects, including their corresponding DatasetVersions 
 	 */
 	private Dataset[] setupDatasetVersionValues(Results myResults, boolean withDetails) {
 		//log.debug("in setupDatasetVersionValues. withDetails = " + withDetails);
 
 		String prevDatasetID = "";
 		String thisDatasetID = "";
         	Dataset thisDataset = null;
 		List<DatasetVersion> datasetVersionList = null;
   		List<Dataset> datasetList = new ArrayList<Dataset>();
 
 		//log.debug("numRows = "+myResults.getNumRows());
 		while ((dataRow = myResults.getNextRow()) != null) {
 			thisDatasetID = dataRow[0];
 			//log.debug("thisDatasetID = "+thisDatasetID);
 			//log.debug("prevDatasetID = "+prevDatasetID);
 			if (!thisDatasetID.equals(prevDatasetID)) {
 				if (datasetVersionList != null) {
                 			DatasetVersion[] myDatasetVersions = myObjectHandler.getAsArray(datasetVersionList, DatasetVersion.class);
 					thisDataset.setDatasetVersions(myDatasetVersions);
 					datasetList.add(thisDataset);
 				}
         			thisDataset = setupDatasetValues(dataRow);
 				datasetVersionList = new ArrayList<DatasetVersion>();
 			}
 			if (dataRow[16] != null) {
 				//log.debug("dataRow here = "); myDebugger.print(dataRow);
 				DatasetVersion newDatasetVersion = new DatasetVersion();
 				newDatasetVersion.setDataset(thisDataset);
 				newDatasetVersion.setVersion(Integer.parseInt(dataRow[16]));
 				newDatasetVersion.setVersion_name(dataRow[17]);
 				newDatasetVersion.setCreate_date_as_string(dataRow[18]);
 				newDatasetVersion.setCreate_date((dataRow[18].equals(" ") ? null : myObjectHandler.getDisplayDateAsTimestamp(dataRow[18])));
 				newDatasetVersion.setVisible(Integer.parseInt(dataRow[19]));
 				newDatasetVersion.setMasterParameterGroupID(Integer.parseInt(dataRow[20]));
 				newDatasetVersion.setVersion_type(dataRow[21]);
 				newDatasetVersion.setGrouping_id(Integer.parseInt(dataRow[22]));
 
 				if (withDetails) {
 					newDatasetVersion.setNormalization_method(dataRow[23]);
 					newDatasetVersion.setNumber_of_groups(Integer.parseInt(dataRow[24]));
 					newDatasetVersion.setNumber_of_non_exclude_groups(Integer.parseInt(dataRow[25]));
 					newDatasetVersion.setNumber_of_genotype_groups(Integer.parseInt(dataRow[26]));
 				}
 				datasetVersionList.add(newDatasetVersion);
 			}
 			prevDatasetID = thisDatasetID;
 		}
 		if (datasetVersionList != null) {
 			//log.debug("adding last datasetVersionList and dataset ");
                 	DatasetVersion[] myDatasetVersions = myObjectHandler.getAsArray(datasetVersionList, DatasetVersion.class);
 			myDatasetVersions = new DatasetVersion().sortDatasetVersions(myDatasetVersions, "versionNum", "A");
 			thisDataset.setDatasetVersions(myDatasetVersions);
 			datasetList.add(thisDataset);
 		}
                 Dataset[] datasetArray = myObjectHandler.getAsArray(datasetList, Dataset.class);
 
 		//log.debug("datasetArray = "); myDebugger.print(datasetArray);
 
 		return datasetArray;
 	}
 
 	/**
 	 * Retrieves the number of strains that have phenotype data for a correlation dataset.  
 	 * @param parameterGroupID	the id of the parameter group that contains the phenotype information
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 * @return	the number of matching strains
 	 */
 
 	public int getNumStrains(int parameterGroupID, Connection conn) throws SQLException {
 
         	String query =
 			"select count(*) "+
 			"from parameter_values "+
 			"where parameter_group_id = ? "+
 			"and category = 'Phenotype Group Value'";
 
 		Results myResults = new Results(query, parameterGroupID, conn);
 
 		int numStrains = 0;
         	while ((dataRow = myResults.getNextRow()) != null) {
         		numStrains = Integer.parseInt(dataRow[0]);
 		}
 		log.debug("in getNumStrains for parameterGroupID = "+parameterGroupID + " NumStrains = "+numStrains);
 
         	myResults.close();
 
   		return numStrains;
 	}
 
 
 	/**
 	 * Updates the flag indicating which stage of quality control has finished.
 	 * @param qc_value	the quality control stage just completed 
 	 * @param conn	the database connection
 	 * @throws            SQLException if a database error occurs
 	 */
 	public void updateQc_complete (String qc_value, Connection conn) throws SQLException {
 
 		String query = 
 			"update datasets "+
 			"set qc_complete = ? "+
 			"where dataset_id = ?";
 
 		log.debug("in updateQc_complete.  dataset_id = "+this.getDataset_id()+ ", qc_value = "+qc_value);
   		PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
         	pstmt.setString(1, qc_value);
 		pstmt.setInt(2, this.getDataset_id());	
 
 		pstmt.executeUpdate();
 
 	}
 
 	/**
 	 * Gets the Dataset object that contains the datasetName from myDatasets.
 	 * @param myDatasets	an array of Dataset objects
 	 * @param datasetName	the datasetName to find
 	 * @return            the Dataset object for the datasetName
 	 */
 	public Dataset getDatasetFromMyDatasets(Dataset[] myDatasets, String datasetName) {
 
 		//log.debug("in getDatasetFromMyDatasets.  Now sorting by datasetName");
         	myDatasets = sortDatasets(myDatasets, "datasetName", "A");
 
         	int idx = Arrays.binarySearch(myDatasets, new Dataset(datasetName), new DatasetSortComparator());
 	
 		//log.debug(" in getDatasetFromMyDatasets.  datasetName = "+datasetName+
 		//	", numDatasets = "+myDatasets.length+", idx = "+idx);
 
         	Dataset thisDataset = (idx < 0 ? null : myDatasets[idx]);
 
 		//log.debug("thisDataset = "); myDebugger.print(thisDataset);
         	return thisDataset;
   	}
 
 	public String toString() {
 		String datasetInfo = "This Dataset object has dataset_id = " + this.getDataset_id() + 
 			" and name = " + dataset_name + 
 			" and these versions: ";
 			if (this.getDatasetVersions() != null) {
 				for (int i=0; i<this.getDatasetVersions().length; i++) {
 					datasetInfo = datasetInfo + this.getDatasetVersions()[i].toString();
 				}
 			} else {
 				datasetInfo = datasetInfo + " None ";
 			}
 		return datasetInfo;
 	}
 
 	public void print() {
 		log.debug(toString());
 	}
 
 	/**
 	 * Compares Datasets based on different fields.
 	 */
 	public class DatasetSortComparator implements Comparator<Dataset> {
 		int compare;
         	Dataset dataset1, dataset2;
 
         	public int compare(Dataset object1, Dataset object2) {
 			String sortColumn = getSortColumn();
 			String sortOrder = getSortOrder();
 
 			if (sortOrder.equals("A")) {
 				dataset1 = object1;
 	                	dataset2 = object2;
 				// default for null columns for ascending order
 				compare = 1;
 			} else {
 				dataset2 = object1;
 	                	dataset1 = object2;
 				// default for null columns for ascending order
 				compare = -1;
 			}
 
                 	//log.debug("dataset1 = "+dataset1+ ", dataset2 = "+dataset2);
 
 			if (sortColumn.equals("datasetID")) {
                 		compare = new Integer(dataset1.getDataset_id()).compareTo(new Integer(dataset2.getDataset_id()));
 			} else if (sortColumn.equals("datasetName")) {
 				compare = dataset1.getName().compareTo(dataset2.getName());
 			} else if (sortColumn.equals("hasGeneLists")) {
 				compare = Boolean.toString(dataset1.hasGeneLists()).compareTo(Boolean.toString(dataset2.hasGeneLists()));
 			} else if (sortColumn.equals("qualityControl")) {
 				compare = dataset1.getQc_complete().compareTo(dataset2.getQc_complete());
 			} else if (sortColumn.equals("numVersions")) {
 				compare = new Integer(dataset1.getDatasetVersions().length).compareTo(new Integer(dataset2.getDatasetVersions().length));
 			} else if (sortColumn.equals("dateCreated")) {
 				compare = dataset1.getCreate_date().compareTo(dataset2.getCreate_date());
 			}
                 	return compare;
 		}
 	}
 
 	public Dataset[] sortDatasets (Dataset[] myDatasets, String sortColumn, String sortOrder) {
 		setSortColumn(sortColumn);
 		setSortOrder(sortOrder);
 		Arrays.sort(myDatasets, new DatasetSortComparator());
 		return myDatasets;
 	}
 	
 	/**
 	 * Class for handling information related to a particular normalization of a dataset.
 	 */
 	public class DatasetVersion {
 		private int version;
 		private String version_name;
 		private String normalize_method;
 		private int visible;
 		private String create_date_as_string;
   		private java.sql.Timestamp create_date;
 		private String version_type;
 		private String version_path;
 		private int number_of_groups;
 		private int number_of_non_exclude_groups;
 		private int number_of_genotype_groups;
 		private int masterParameterGroupID;
 		private Dataset dataset;
 		private GeneList[] geneLists;
 		private ParameterValue[] parameters;
 		private Group[] groups;
 		private int grouping_id;
 		private String sortColumn = "versionNum";
 		private String sortOrder = "A";
 		private int[] groupCounts;
                 private DSFilterStat[] filterstats=null;
 	
 		public DatasetVersion() {
 			log = Logger.getRootLogger();
 		}
 
 		public DatasetVersion(int version, String dsPath) {
 			log = Logger.getRootLogger();
 			this.setVersion_path(dsPath, version);
 			this.setVersion(version);
 		}
 
 		public DatasetVersion(int version) {
 			log = Logger.getRootLogger();
 			this.setVersion_path(Dataset.this.getPath(), version);
 			this.setDataset(Dataset.this);
 			this.setVersion(version);
 		}
 
 		public void setVersion(int inInt) {
 			version = inInt;
 		}
 
 		public int getVersion() {
 			return version;
 		}
 	
 		public void setVersion_name(String inString) {
 			version_name = inString;
 		}
 
 		public String getVersion_name() {
 			return version_name;
 		}
 	
 		public void setNormalization_method(String inString) {
 			normalize_method = inString;
 		}
 
 		public String getNormalization_method() {
 			return normalize_method;
 		}
 	
 		public void setVisible(int inInt) {
 			visible = inInt;
 		}
 
 		public int getVisible() {
 			return visible;
 		}
 	
 		public void setCreate_date_as_string(String inString) {
 			create_date_as_string = inString;
 		}
 
 		public String getCreate_date_as_string() {
 			return create_date_as_string;
 		}
 
   		public void setCreate_date(java.sql.Timestamp inTimestamp) {
     			this.create_date = inTimestamp;
   		}
 
   		public java.sql.Timestamp getCreate_date() {
     			return create_date;
   		}
 	
 		public void setNumber_of_groups(int inInt) {
 			number_of_groups = inInt;
 		}
 
 		public int getNumber_of_groups() {
 			return number_of_groups;
 		}
 	
 		public void setNumber_of_non_exclude_groups(int inInt) {
 			number_of_non_exclude_groups = inInt;
 		}
 
 		public int getNumber_of_non_exclude_groups() {
 			return number_of_non_exclude_groups;
 		}
 	
 		public void setNumber_of_genotype_groups(int inInt) {
 			number_of_genotype_groups = inInt;
 		}
 
 		public int getNumber_of_genotype_groups() {
 			return number_of_genotype_groups;
 		}
 	
 		public void setMasterParameterGroupID(int inInt) {
 			masterParameterGroupID = inInt;
 		}
 
 		public int getMasterParameterGroupID() {
 			return masterParameterGroupID;
 		}
 	
 		public void setVersion_type(String inString) {
 			version_type = inString;
 		}
 
 		public String getVersion_type() {
 			return version_type;
 		}
 
 		public void setGeneLists(GeneList[] inGeneLists) {
 			geneLists = inGeneLists;
 		}
 	
 		public GeneList[] getGeneLists() {
 			return geneLists;
 		}
                 
                 public void setFilterStats(DSFilterStat[] inFilterStats) {
 			filterstats = inFilterStats;
 		}
 	
 		
 
 		public void setGroups(Group[] inGroups) {
 			groups = inGroups;
 		}
 	
 		public Group[] getGroups() {
 			return groups;
 		}
 
   		public void setGroupCounts(int[] inArray) {
     			this.groupCounts = inArray;
   		}
 
   		public int[] getGroupCounts() {
     			return groupCounts;
   		}
 
 		public void setDataset(Dataset inDataset) {
 			dataset = inDataset;
 		}
 	
 		public Dataset getDataset() {
 			return dataset;
 		}
 	
 		public void setParameters(ParameterValue[] inParameters) {
 			parameters = inParameters;
 		}
 	
 		public ParameterValue[] getParameters() {
 			return parameters;
 		}
 	
 		public boolean hasPhenotypeData(int user_id) {
 			for (ParameterValue thisParameter : this.getParameters()) {
 				if (thisParameter.getCategory().indexOf("Phenotype Data") > -1) {
 					if (this.getDataset().getCreator().equals("public")) {
 						if (checkForUserID(user_id, this.getParameters(), thisParameter.getParameter_group_id())) {
 							return true;
 						}
 					} else {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 	
 		public boolean hasClusterResults(int user_id) {
 			for (ParameterValue thisParameter : this.getParameters()) {
 				if (thisParameter.getCategory().indexOf("Cluster") > -1) {
 					if (this.getDataset().getCreator().equals("public")) {
 						if (checkForUserID(user_id, this.getParameters(), thisParameter.getParameter_group_id())) {
 							return true;
 						}
 					} else {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
                 
                 public boolean hasFilterStatsResults(int userID,Connection conn) {
                     
 			if (this.getFilterStats(userID,conn) != null && this.getFilterStats(userID,conn).length > 0) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	
 		public boolean hasGeneLists() {
 			if (this.getGeneLists() != null && this.getGeneLists().length > 0) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	
 		public boolean readyForCorrelation() {
 			if (this.getNumber_of_non_exclude_groups() > 4) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 	
 		public boolean readyForDifferentialExpression() {
 			int[] groupCounts = this.getGroupCounts();
 			if (groupCounts != null && groupCounts.length > 0) {
 				for (int thisGroupCount : groupCounts) {
 					if (thisGroupCount < 2) {
 						return false;
 					}
 				}
 			} else {
 				return false;
 			}
 			return true;
 		}
 	
 		/** Sets the version's path.  Include a '/' at the end. 
 		 * @param inString the path name
 		 */
 		public void setVersion_path(String inString) {
 			version_path = inString;
 		}
 
 		/** Sets the version's path.  Includes a '/' at the end. 
 		 * @param datasetPath the dataset's path
 		 * @param version version number
 		 */
 		public void setVersion_path(String datasetPath, int version) {
 			version_path = datasetPath + "v" + version + "/";
 		}
 	
 		/** Returns the version's path.  Includes a '/' at the end. 
 		 * @return	the path name
 		 */
 		public String getVersion_path() {
 			return version_path;
 		}
 
   		public String getSortColumn() {
     			return sortColumn; 
   		}
 
   		public void setSortColumn(String inString) {
     			this.sortColumn = inString;
   		}
 
   		public String getSortOrder() {
     			return sortOrder; 
   		}
 
   		public void setSortOrder(String inString) {
     			this.sortOrder = inString;
   		}
 	
 		public void setGrouping_id(int inInt) {
 			grouping_id = inInt;
 		}
 
 		public int getGrouping_id() {
 			return grouping_id;
 		}
 
   		public String toString() {
         		return ("DatasetVersion has v#" + " " + version +
 				", and name = '"+version_name + 
 				"' and # of groups = "+number_of_groups +
 				", and # of params = "+(getParameters() == null ? "none" : Integer.toString(getParameters().length)));
   		}
 		public void print() {
 			log.debug(toString());
 		}
 
 		public boolean equals(Object obj) {
         		if (!(obj instanceof DatasetVersion)) return false;
 			return (this.version == ((DatasetVersion)obj).version);
 		}
 
 		/**
 	 	* Creates a record in the dataset_versions table.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	*/
 
 		public void createDatasetVersion (Connection conn) throws SQLException {
 
 			log.debug("in createDatasetVersion.");
 
         		String query =
                 		"insert into dataset_versions "+
                 		"(dataset_id, version, create_date, visible, version_name, "+
 				"version_type, grouping_id) values  "+
                 		"(?, ?, ?, ?, ?, "+
 				"?, ?)";
 
 			java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
 
         		PreparedStatement pstmt = conn.prepareStatement(query, 
 						ResultSet.TYPE_SCROLL_INSENSITIVE,
 						ResultSet.CONCUR_UPDATABLE);
 
         		pstmt.setInt(1, this.getDataset().getDataset_id());
         		pstmt.setInt(2, this.version);
         		pstmt.setTimestamp(3, now);
         		pstmt.setInt(4, this.visible);
         		pstmt.setString(5, this.version_name);
         		pstmt.setString(6, this.version_type);
         		pstmt.setInt(7, this.grouping_id);
 
         		pstmt.executeUpdate();
 			pstmt.close();
 		}
                 
                 /**
 	 	* Gets DatasetFilterStats from table and creates DSFilterStat.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	*/
                 public DSFilterStat[] getFilterStats(int userID,Connection conn){
                     if(filterstats==null){
                         try{
                             this.getFilterStatsFromDB(userID,conn);
                         }catch(SQLException ex){
                             
                         }
                     }
                     return filterstats;
                 }
 		public void getFilterStatsFromDB (int userID,Connection conn) throws SQLException {
 			filterstats=new DSFilterStat().getFilterStatsFromDB(this.dataset,this,userID,conn);
                         //System.out.println("Returned FilterStats Size:"+filterstats.length);
 		}
                 
                 public int createFilterStats(String filterdate,String filtertime,String analysisType,int userID,Connection conn) throws SQLException{
                     int ret=-1;    
                     DSFilterStat tmp=new DSFilterStat();
                     ret=tmp.createFilterStats(filterdate,filtertime,this.dataset,this,analysisType,userID,conn);
                     //System.out.println("Returned ID:"+ret);
                     this.getFilterStatsFromDB(userID,conn);
                     return ret;
                 }
                 
                 public void addFilterStep(int dsFilterStatID,String method,String parameters,int step_count, int stepNumber,int userID,Connection conn){
                     DSFilterStat curDSFS=getFilterStat(dsFilterStatID,userID,conn);
                     if(curDSFS!=null){
                         curDSFS.addFilterStep(method,parameters,step_count,stepNumber,-1,-1,conn);
                     }
                 }
                 
                 public void addStatsStep(int dsFilterStatID,String method,String parameters, int step_count,int stepNumber,int status,int userID, Connection conn){
                     DSFilterStat curDSFS=getFilterStat(dsFilterStatID,userID,conn);
                     curDSFS.addStatsStep(method,parameters,step_count,stepNumber, status,conn);
                 }
                 
                 public DSFilterStat getFilterStat(int id,int userID,Connection conn){
                     DSFilterStat ret=null;
                     if(filterstats==null){
                         try{
                             this.getFilterStatsFromDB(userID,conn);
                         }catch(SQLException ex){
                             
                         }
                     }
                     //System.out.println("looking for id="+id);
                     for(int i=0;i<filterstats.length;i++){
                         //System.out.println("checking"+filterstats[i].getDSFilterStatID());
                         if(filterstats[i].getDSFilterStatID()==id){
                             ret=filterstats[i];
                         }
                     }
                     return ret;
                 }
                 
                 public DSFilterStat getFilterStat(String curDay,String curTime,int userID,Connection conn){
                     DSFilterStat ret=null;
                     if(filterstats==null){
                         try{
                             this.getFilterStatsFromDB(userID,conn);
                         }catch(SQLException ex){
                             
                         }
                     }
                     //System.out.println("looking for id="+id);
                     for(int i=0;i<filterstats.length;i++){
                         //System.out.println("checking"+filterstats[i].getDSFilterStatID());
                         if(filterstats[i].getFilterDate().equals(curDay)&&filterstats[i].getFilterTime().equals(curTime)){
                             ret=filterstats[i];
                         }
                     }
                     return ret;
                 }
                 
 		/**
 	 	* Removes a record from the dataset_versions table, plus all records from related tables.  It 
 	 	* also deletes the directory containing the version-specific files and the directories used for doing
 	 	* statistical analysis in order to find differentially expressed genes. 
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	*/
                 
                 //TODO Modify to delete DatasetFilterStats entries
 	 	public void deleteDatasetVersion(int userID,Connection conn) throws SQLException, Exception {
 
 			conn.setAutoCommit(false);
   
 			int version = this.getVersion();
 			String dsPath = this.getDataset().getPath();
 
 			// dsPath is something like ~/userFiles/ckh/Datasets/DatasetName_Master
 			// analysisPath is something like ~/userFiles/ckh/GeneLists/DatasetName_v1_Analysis
 			String analysisPath = dsPath.replace("Datasets", "GeneLists").replace("Master/", "v" + version) + "_Analysis";
 
 			log.debug("in deleteDatasetVersion. dataset_id = " + this.getDataset().getDataset_id() + ", version = " + version);
 
 			log.debug("dataset is "+this.getDataset().getName() + 
 					", and path is "+ dsPath+ "v" + version + 
 					" and genelist dir is "+analysisPath);
 
 			try {
                                 if(new edu.ucdenver.ccp.PhenoGen.data.Array().EXON_ARRAY_TYPES.contains(this.getDataset().getArray_type())){
                                     //delete DSFilterStats
                                     if(filterstats==null||filterstats.length==0){
                                         this.getFilterStatsFromDB(userID, conn);
                                     }
 
                                     for(int i=0;i<filterstats.length;i++){
                                         filterstats[i].deleteFromDB(conn);
                                     }
                                     if(!this.getDataset().getCreator().equals("public")){
                                         String execute="{call filterprep.cleanupafterdelete(" + this.getDataset().getDataset_id() + "," + version + ") }";
                                         CallableStatement cs = conn.prepareCall(execute);
                                         cs.execute();
                                         cs.close();
                                     }
                                 }
                                 
                                 new GeneList().deleteGeneListsForDatasetVersion(this, conn);
 
   				new ParameterValue().deleteParameterGroupsForDatasetVersion(this, conn);
   				new SessionHandler().deleteSessionActivitiesForDatasetVersion(this, conn);
   	
   				String query =
         				"delete from dataset_versions "+
         				"where dataset_id = ? "+
 					"and version = ?";
   	
   				PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
 				pstmt.setInt(1, this.getDataset().getDataset_id());
 				pstmt.setInt(2, version);
 
 				pstmt.executeUpdate();
 				pstmt.close();
 				conn.commit();
 				boolean success = new FileHandler().deleteAllFilesPlusDirectory(new File(dsPath + "v" + version));
                                 
 				if (!success) {
 					Email myEmail = new Email();
 					myEmail.setSubject("Error deleting files for dataset version");
 					myEmail.setContent("Path is "+dsPath + "v" + version);
 					try {
 						myEmail.sendEmailToAdministrator("");
 					} catch (Exception e) {
 						log.error("error sending message to administrator");
 					}
 				}
 				success = new FileHandler().deleteAllFilesPlusDirectory(new File(analysisPath));
 				if (!success) {
 					Email myEmail = new Email();
 					myEmail.setSubject("Error deleting files for analysis of dataset version");
 					myEmail.setContent("Path is "+analysisPath);
 					try {
 						myEmail.sendEmailToAdministrator("");
 					} catch (Exception e) {
 						log.error("error sending message to administrator");
 					}
 				}
 			} catch (SQLException e) {
 				log.error("In exception of deleteDatasetVersion", e);
 				conn.rollback();
 				throw e;
 			}
 			conn.setAutoCommit(true);
 		}
 
 
 	 	/**
 	 	 * 
 	 	 * Gets the number of arrays excluded in a group
 	 	 * 
 	 	 * @param grouping_id
 	 	 * @param conn
 	 	 * @return number of arrays excluded in a group
 	 	 * @throws SQLException
 	 	 */
 		public long getNumberOfExcludedArrays( int grouping_id, Connection conn) throws SQLException {
 	    	   
 			long NumberOfExcludedArrays = 0;
 	    	   
 	    	   	String query = "select count(user_chip_id) from chip_groups cg , groups g where " +
 	    	   		" g.group_number = 0 and " + 
 	    	   		" g.group_name= 'Exclude' and " +
 	    	   		" cg.group_id = g.group_id and" +
 	    	   		" g.grouping_id= ?";
 	    	   
 			Results myResults = new Results(query, grouping_id, conn);
 			NumberOfExcludedArrays = myResults.getIntValueFromFirstRow();
 
 			myResults.close();
 	    	   
 			return NumberOfExcludedArrays;
 		}
 	 	
 
 		/**
 	 	* Updates the visible flag in the dataset_versions table to '1'.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	*/
 		public void updateVisible (Connection conn) throws SQLException {
 
 			String query = 
 				"update dataset_versions "+
 				"set visible = 1 "+
 				"where dataset_id = ? "+
 				"and version = ?";
 
 			log.debug("in updateVisible.  dataset = "+
 					this.getDataset().getDataset_id() + ": " + this.getDataset().getName());
   			PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
 			pstmt.setInt(1, this.getDataset().getDataset_id());	
         		pstmt.setInt(2, this.getVersion());
 
 			pstmt.executeUpdate();
 
 		}
 
 		/**
 	 	* Updates the visible flag in the dataset_versions table to '-1' to indicate an error.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	*/
 		public void updateVisibleToError (Connection conn) throws SQLException {
 
 			String query = 
 				"update dataset_versions "+
 				"set visible = -1 "+
 				"where dataset_id = ? "+
 				"and version = ?";
 
 			log.debug("in updateVisibleToError.  dataset = "+
 					this.getDataset().getDataset_id() + ": " + this.getDataset().getName());
   			PreparedStatement pstmt = conn.prepareStatement(query, 
 							ResultSet.TYPE_SCROLL_INSENSITIVE,
 							ResultSet.CONCUR_UPDATABLE);
 			pstmt.setInt(1, this.getDataset().getDataset_id());	
         		pstmt.setInt(2, this.getVersion());
 
 			pstmt.executeUpdate();
 
 		}
 
 		/**
 	 	 * Constructs the name of the file that stores the phenotype values. 
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 		 *			(e.g., DatasetName_Master/Groupings/groupingID/userName/phenotypeName/PhenotypeData.txt)
 	 	 */
   		public String getPhenotypeDataFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "phenotypeData.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the output file from Phenotype.ImportTxt.R 
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 		 *			(e.g., DatasetName_Master/Groupings/groupingID/userName/phenotypeName/Phenotype.output.Rdata)
 	 	 */
   		public String getPhenotypeDataOutputFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "Phenotype.output.Rdata";
   		}
 
 		/**
 	  	 * Constructs the name of the directory where the phenotype files for this version's grouping are stored
 	 	 * @return            a String containing the name of the directory
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingID/
 	 	 */
 		public String getGroupingDir() {
 			return this.getDataset().getGroupingsDir() + 
 				this.getGrouping_id() + "/";
 		}
 
 		/**
 	 	 * Constructs the path where the phenotype files for this DatasetVersion by this user will be held.
 		 * @param userName	the name of the user
 	 	 * @return            a String containing the path for this dataset version for this user 
 		 *			(e.g., DatasetName_Master/Groupings/groupingID/userName/)
 	 	 */
   		public String getGroupingUserDir(String userName) {
 			return getGroupingDir() + userName + "/"; 
   		}
 
 		/**
 	 	 * Constructs the path where the phenotype files for this DatasetVersion by this user and phenotype will be held.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the path for this dataset version 
 		 *			for this user and phenotype
 		 *			(e.g., DatasetName_Master/Groupings/groupingID/userName/phenotypeName/)
 	 	 */
   		public String getGroupingUserPhenotypeDir(String userName, String phenotypeName) {
 			return getGroupingUserDir(userName) + myObjectHandler.removeBadCharacters(phenotypeName) + "/";
   		}
 
 		/**
 	  	 * Constructs the name of the file for downloading phenotype data 
 	 	 * @param userName	the name of the user
 	 	 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/phenotypeValues.txt)
 	 	 */
 		public String getPhenotypeDownloadFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName, phenotypeName) + "phenotypeValues.txt";
 		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the number of strains that 
 		 * match with expression data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/StrainNumber.txt)
 	 	 */
   		public String getExpressionStrainCountFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "StrainNumber.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the summary data that 
 		 * match with expression data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/PhenotypeSummary.txt)
 	 	 */
   		public String getExpressionSummaryFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "PhenotypeSummary.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the graphic for the strains that  
 		 * match with expression data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/PhenotypeSummaryGraph.png)
 	 	 */
   		public String getExpressionGraphFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "PhenotypeSummaryGraph.png";
   		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the number of strains that 
 		 * match with QTL data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/QTLStrainNumber.txt)
 	 	 */
   		public String getQTLStrainCountFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "QTLStrainNumber.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the summary data that 
 		 * match with QTL data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/PhenotypeSummary.txt)
 	 	 */
   		public String getQTLSummaryFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "QTLPhenotypeSummary.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the file output from Phenotype.ImportTxt.R that contains the graphic for the strains that  
 		 * match with QTL data.
 		 * @param userName	the name of the user
 		 * @param phenotypeName	the name of the phenotype
 	 	 * @return            a String containing the name of the file
 	 	 *			(e.g., DatasetName_Master/Groupings/groupingName/userName/phenotypeName/PhenotypeSummaryGraph.png)
 	 	 */
   		public String getQTLGraphFileName(String userName, String phenotypeName) {
 			return getGroupingUserPhenotypeDir(userName,phenotypeName) + "QTLPhenotypeSummaryGraph.png";
   		}
 
 		/**
 	 	 * Constructs the name of the groups file 
 	 	 * @return            a String containing the name of the file
 		 *			(e.g., DatasetName_Master/v1/groups.txt)
 	 	 */
   		public String getGroupFileName() {
 			return this.getVersion_path() + "groups.txt";
   		}
 
 		/**
 	 	 * Constructs the name of the normalization output file 
 	 	 * @return            a String containing the name of the file
 		 *			(e.g., DatasetName_Master/v1/Affymetrix.ExportOutBioC.output.Rdata)
 	 	 */
   		public String getNormalized_RData_FileName() {
 				return this.getVersion_path() +
                                 	this.getDataset().getPlatform() +
                                 	".ExportOutBioC.output.Rdata";
   		}
 
 		/**
 	 	 * Constructs the name of the normalization output file including grouping data (e.g., phenotype values) 
 		 * @param groupingUserPhenotypeDir the directory where the file will be stored
 	 	 * @return            a String containing the name of the file
 		 *			(e.g., DatasetName_Master/Groupings/###/userName/phenotypeName/Affymetrix.ExportOutBioC.output.Rdata)
 	 	 */
   		public String getNormalizedForGrouping_RData_FileName(String groupingUserPhenotypeDir) {
 				return groupingUserPhenotypeDir +
                                 	this.getDataset().getPlatform() +
                                 	".ExportOutBioC.output.Rdata";
   		}
 
 		/**
 	 	* Retrieves the parameter group id for the set of master parameters for this dataset version.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return            the identifier of the master parameter group 
 	 	*/
 		public int getMasterParameterGroupID(Connection conn) throws SQLException {
 
 			log.debug("In getMasterParameterGroupID. dataset_id = " + this.getDataset().getDataset_id() + ", version = " + this.getVersion());
 
         		String query =
 				"select parameter_group_id "+
 				"from parameter_groups "+
 				"where dataset_id = ? "+
 				"and version = ? "+
 				"and master = 1";
 	
 			//log.debug("query = "+query);
 
                 	Results myResults = new Results(query, new Object[] {this.getDataset().getDataset_id(), this.getVersion()}, conn);
 
                 	int parameter_group_id = myResults.getIntValueFromFirstRow();
 
                 	myResults.close();
 
         		return parameter_group_id;
 		}
 
 		/**
 	 	 * Constructs the path where the files for the cluster analyses 
 		 * performed on this DatasetVersion will be held.
 	 	 * @return            a String containing the cluster analysis path for this dataset version  
 	 	 */
   		public String getClusterDir() {
 			return this.getVersion_path() + "ClusterAnalyses/";
   		}
 
 		/**
 	 	* Retrieves the number of datafiles in each group for this dataset version.  
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return	an array containing the number of datafiles in each group.  
 	 	*		Note that this array is ordered by the group's number (e.g., 1, 2, 3). 
 	 	*/
 
 		public int[] getGroupCounts(Connection conn) throws SQLException {
 
 			//log.debug("in get GroupCounts. " );
 
         		String query =
                                 "select grps.group_number, count(*) "+
                                 "from groups grps, chip_groups cg, dataset_versions dv "+
                                 "where dv.grouping_id = grps.grouping_id "+
                                 "and dv.dataset_id = ? "+
                                 "and dv.version = ? "+
                                 "and grps.group_number != 0 "+
                                 "and cg.group_id = grps.group_id "+
                                 "group by grps.group_number "+
                                 "order by grps.group_number";
 
 			//log.debug("query = "+query);
 
 			//log.debug("dataset_id = "+this.getDataset().getDataset_id() + ", version = "+ this.getVersion());
 			Results myResults = new Results(query, new Object[] {this.getDataset().getDataset_id(), this.getVersion()}, conn);
 
 			int[] groupCount = myObjectHandler.getResultsAsIntArray(myResults, 1);
 
         		myResults.close();
 
   			return groupCount;
 		}
 
 		/**
  	 	* Retrieves the groups for this dataset version 
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return	an array of Group objects
 	 	*/
 		public Group[] getGroups(Connection conn) throws SQLException {
 
 			log.debug("in getGroups for a dataset version");
         		String query =
                                 "select grpings.grouping_id, "+
 				"grpings.grouping_name, "+
 				"grpings.criterion, "+
 				"grps.group_id, "+
 				"grps.group_number, "+
 				"grps.group_name, "+
 				"grps.has_expression_data, "+
 				"grps.has_genotype_data, "+
 				"grps.parent "+
                                 "from groups grps, groupings grpings, dataset_versions dv "+
                                 "where dv.grouping_id = grps.grouping_id "+
                                 "and grpings.grouping_id = grps.grouping_id "+
                                 "and grpings.dataset_id = dv.dataset_id "+
                                 "and dv.dataset_id = ? "+
                                 "and dv.version = ? "+
                                 "order by to_number(grps.group_number)";
 
 			//log.debug("query = "+query);
         		List<Group> myGroupList = new ArrayList<Group>();
 
 			Results myResults = new Results(query, new Object[] {this.getDataset().getDataset_id(), this.getVersion()}, conn);
                 	String[] dataRow;
 			while ((dataRow = myResults.getNextRow()) != null) {
                        		Group newGroup = new Group().setupGroup(dataRow);
                         	myGroupList.add(newGroup);
 			} 
         		myResults.close();
 
 			Group[] myGroupArray = (Group[]) myObjectHandler.getAsArray(myGroupList, Group.class);
 
   			return myGroupArray;
   		}
 
 		/**
  	 	* Retrieves the groups for this dataset version 
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return	a Hashtable of the parent name pointing to a list of group names
 	 	*/
 		public Hashtable<String, List<String>> getParentsWithGroups(Connection conn) throws SQLException {
 
 			log.debug("in getParentsWithGroups for a dataset version");
         		String query = 
 				"select "+
 				"nvl(g.parent, g.group_name), decode(g.parent, null, 'Same', g.group_name) "+
                                 "from groups g , dataset_versions dv "+
                                 "where dv.grouping_id = g.grouping_id "+
                                 "and dv.dataset_id = ? "+
                                 "and dv.version = ? "+
                                 "order by 1, 2";
 
 			log.debug("query = "+query);
 
 			Results myResults = new Results(query, new Object[] {this.getDataset().getDataset_id(), this.getVersion()}, conn);
 	
 			Hashtable<String, List<String>> myHashtable = myObjectHandler.getResultsAsHashtablePlusList(myResults);
 
 			myResults.close();
 
   			return myHashtable;
   		}
 
 		/**
  	 	* Retrieves the groups for this dataset version that have expression data.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return	an array of Group objects
 	 	*/
 		public Group[] getGroupsWithExpressionData(Connection conn) throws SQLException {
 
 			log.debug("in getGroupsWithExpressionData for a dataset version");
 
 			Group[] inGroupArray = getGroups(conn); 
 
         		List<Group> myGroupList = new ArrayList<Group>();
 			for (int i=0; i<inGroupArray.length; i++) {
 				if (inGroupArray[i].getHas_expression_data().equals("Y")) {
                         		myGroupList.add(inGroupArray[i]);
 				}
 			} 
 
 			Group[] outGroupArray = (Group[]) myObjectHandler.getAsArray(myGroupList, Group.class);
 
   			return outGroupArray;
   		}
 
 		/**
  	 	* Retrieves the groups for this dataset version that have genotype data.
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return	an array of Group objects
 	 	*/
 		public Group[] getGroupsWithGenotypeData(Connection conn) throws SQLException {
 
 			log.debug("in getGroupsWithGenotypeData for a dataset version");
 
 			Group[] inGroupArray = getGroups(conn); 
 
         		List<Group> myGroupList = new ArrayList<Group>();
 			for (int i=0; i<inGroupArray.length; i++) {
 				if (inGroupArray[i].getHas_genotype_data().equals("Y")) {
                         		myGroupList.add(inGroupArray[i]);
 				}
 			} 
 
 			Group[] outGroupArray = (Group[]) myObjectHandler.getAsArray(myGroupList, Group.class);
 
   			return outGroupArray;
   		}
 
 		/**
 	 	* Gets the chips that are in a given set of groups for this public dataset version.  
 		* It also gets the group that each chip is in.
 	 	* @param groupList	a comma-separated list of group numbers
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return            an array of UserChip objects contained in the given set of groups 
 	 	*/
 		public User.UserChip[] getChipsInOldDataset(String groupList, Connection conn) throws SQLException {
 
         		String query =
                                 "select uc.user_chip_id, "+
                                 "uc.hybrid_id, "+
                                 "uc.owner_user_id, "+
                                 "grps.group_number "+
                                 "from user_chips uc, "+
                                 "dataset_chips dc, "+
                                 "chip_groups cg, "+
                                 "groups grps, "+
                                 "dataset_versions dv, "+
                                 "users u "+
                                 "where uc.user_chip_id = dc.user_chip_id "+
                                 "and dc.dataset_id = ? "+
                                 "and dv.version = ? "+
                                 "and dv.dataset_id = dc.dataset_id "+
                                 "and dv.grouping_id = grps.grouping_id "+
                                 "and grps.group_id = cg.group_id "+
                                 "and grps.group_number in "+
 				groupList +  
                                 " and uc.user_id = u.user_id "+
                                 "and u.user_name = 'public' "+
                                 "and cg.user_chip_id = to_char(uc.user_chip_id) "+
                                 "order by uc.hybrid_id";
 
 
         		log.info("in getChipsInOldDataset. dataset_id = "+this.getDataset().getDataset_id()+
 					", and version = "+this.getVersion());
         		//log.debug("query = "+query);
 
         		Results myResults = new Results(query, new Object[] {this.getDataset().getDataset_id(), this.getVersion()}, conn);
 
 			User.UserChip[] myUserChips = new User().setupUserChipValues(myResults); 
 
 			myResults.close();
         		return myUserChips;
 		}
 
 		/**
 	 	* Retrieves a single DatasetVersion from an array of DatasetVersions
 	 	* @param myDatasetVersions	an array of DatasetVersions
 	 	* @param version	the version number of the desired DatasetVersion
 	 	* @return            the DatasetVersion corresponding to the version requested
 	 	*/
 		public DatasetVersion getDatasetVersion(DatasetVersion[] myDatasetVersions, int version) {
 
         		myDatasetVersions = sortDatasetVersions(myDatasetVersions, "versionNum", "A");
 
         		int versionToFindIndex = Arrays.binarySearch(myDatasetVersions, 
 								new DatasetVersion(version), 
 								new VersionSortComparator());
 
         		DatasetVersion thisDatasetVersion = myDatasetVersions[versionToFindIndex];
 
         		return thisDatasetVersion;
 		}
 
 	
 		/**
 	 	* Sorts an array of DatasetVersions by the specified sortColumn
 	 	* @param myDatasetVersions	an array of DatasetVersions
 	 	* @param sortColumn	the column to be sorted
 	 	* @param sortOrder	the order in which the column should be sorted
 	 	* @return            the array in sorted order
 	 	*/
 		public DatasetVersion[] sortDatasetVersions (DatasetVersion[] myDatasetVersions, String sortColumn, String sortOrder) {
 			setSortColumn(sortColumn);
 			setSortOrder(sortOrder);
         		Arrays.sort(myDatasetVersions, new VersionSortComparator());
         		return myDatasetVersions;
 		}
 
 		/**
  	 	* Compares DatasetVersions based on different fields.
 	 	*/
 		public class VersionSortComparator implements Comparator<DatasetVersion> {
         		int compare;
         		DatasetVersion version1, version2;
 
         		public int compare(DatasetVersion object1, DatasetVersion object2) {
 
 				String sortColumn = getSortColumn();
 				String sortOrder = getSortOrder();
 
 				if (sortOrder.equals("A")) {
 					version1 = object1;
                 			version2 = object2;
 					// default for null columns for ascending order
 					compare = 1;
 				} else {
 					version2 = object1;
                 			version1 = object2;
 					// default for null columns for ascending order
 					compare = -1;
 				}
 
                 		//log.debug("version1 version = "+version1.getVersion()+ 
 						//", version2 version = "+version2.getVersion());
 
 				if (sortColumn.equals("versionNum")) {
                 			compare = new Integer(version1.getVersion()).compareTo(new Integer(version2.getVersion()));
 				} else if (sortColumn.equals("hasGeneLists")) {
 					compare = Boolean.toString(version1.hasGeneLists()).compareTo(Boolean.toString(version2.hasGeneLists()));
 				} else if (sortColumn.equals("readyForCorrelation")) {
 					compare = Boolean.toString(version1.readyForCorrelation()).compareTo(Boolean.toString(version2.readyForCorrelation()));
 				} else if (sortColumn.equals("readyForDifferentialExpression")) {
 					compare = Boolean.toString(version1.readyForDifferentialExpression()).compareTo(Boolean.toString(version2.readyForDifferentialExpression()));
 				} else if (sortColumn.equals("versionName")) {
 					compare = version1.getVersion_name().compareTo(version2.getVersion_name());
 				} else if (sortColumn.equals("numGroups")) {
 					compare = new Integer(version1.getNumber_of_groups()).compareTo(new Integer(version2.getNumber_of_groups()));
 				} else if (sortColumn.equals("visible")) {
 					compare = new Integer(version1.getVisible()).compareTo(new Integer(version2.getVisible()));
 				} else if (sortColumn.equals("dateCreated")) {
 					compare = version1.getCreate_date().compareTo(version2.getCreate_date());
 				}
                 		return compare;
         		}
 
 		}
 
 	}
 
 	/**
 	 * Class for handling information related to a particular grouping of chips for a dataset.
 	 */
 	public class Group implements Comparable {
 		private int grouping_id;
 		private String grouping_name;
 		private String criterion;
 		private int group_id;
 		private int group_number;
 		private int dataset_id;
   		private int number_of_arrays;
 		private String group_name;
 		private String has_expression_data;
 		private String has_genotype_data;
 		private String parent;
 		private String sortOrder;
 		private String sortColumn;
 		private boolean alreadyDisplayed;
 
 		public Group() {
 			log = Logger.getRootLogger();
 		}
 
 		public Group(int group_number) {
 			log = Logger.getRootLogger();
 			this.setGroup_number(group_number);
 		}
 
 		public Group(String group_name) {
 			log = Logger.getRootLogger();
 			this.setGroup_name(group_name);
 		}
 
 		public void setGrouping_id(int inInt) {
 			grouping_id = inInt;
 		}
 
 		public int getGrouping_id() {
 			return grouping_id;
 		}
 
   		public String getGrouping_name() {
     			return grouping_name; 
   		}
 
   		public void setGrouping_name(String inString) {
     			this.grouping_name = inString;
   		}
 
   		public String getCriterion() {
     			return criterion; 
   		}
 
   		public void setCriterion(String inString) {
     			this.criterion = inString;
   		}
 
 		public void setGroup_id(int inInt) {
 			group_id = inInt;
 		}
 
 		public int getGroup_id() {
 			return group_id;
 		}
 
 		public void setGroup_number(int inInt) {
 			group_number = inInt;
 		}
 
 		public int getGroup_number() {
 			return group_number;
 		}
 
   		public void setGroup_name(String inString) {
     			this.group_name = inString;
   		}
 
   		public String getGroup_name() {
     			return group_name; 
   		}
 
   		public void setHas_expression_data(String inString) {
     			this.has_expression_data = inString;
   		}
 
   		public String getHas_expression_data() {
     			return has_expression_data; 
   		}
 
   		public void setHas_genotype_data(String inString) {
     			this.has_genotype_data = inString;
   		}
 
   		public String getHas_genotype_data() {
     			return has_genotype_data; 
   		}
 
   		public void setParent(String inString) {
     			this.parent = inString;
   		}
 
   		public String getParent() {
     			return parent; 
   		}
 
 		public void setDataset_id(int inInt) {
 			dataset_id = inInt;
 		}
 
 		public int getDataset_id() {
 			return dataset_id;
 		}
 
   		public void setNumber_of_arrays(int inInt) {
     			this.number_of_arrays = inInt;
   		}
 
   		public int getNumber_of_arrays() {
     			return number_of_arrays; 
   		}
 
   		public void setAlreadyDisplayed(boolean inBoolean) {
     			this.alreadyDisplayed = inBoolean;
   		}
 
   		public boolean getAlreadyDisplayed() {
     			return alreadyDisplayed; 
   		}
 
   		public String getSortColumn() {
     			return sortColumn; 
   		}
 
   		public void setSortColumn(String inString) {
     			this.sortColumn = inString;
   		}
 
   		public String getSortOrder() {
     			return sortOrder; 
   		}
 
   		public void setSortOrder(String inString) {
     			this.sortOrder = inString;
   		}
 
   		public String toString() {
         		return ("This Group object has grouping_id" + " " + grouping_id +
 				" and grouping name = "+grouping_name + 
 				" and group_name = " + group_name + 
 				" and group_number = " + group_number); 
   		}
 		public void print() {
 			log.debug(toString());
 		}
 
                 /** 
                 * This is required for equals to work 
                 * @return the hashCode for the grouping_id plus the group_name
                 */
 		public int hashCode() {         
 			return ((grouping_id + group_name).hashCode()); 
 		}
 		
                 /** 
                 * A Group object is equal if both the grouping_id and the group_name are the same.
                 * @param obj    a Group object
                 * @return true if the objects are equal, false otherwise
                 */
                 public boolean equals(Object obj) {
                         if (!(obj instanceof Group)) return false;
 			Group thatGroup = (Group) obj;
                         return (this.grouping_id == thatGroup.grouping_id && this.group_name.equals(thatGroup.group_name));
                 }
 
                 public int compareTo(Object myGroupObject) {
                         if (!(myGroupObject instanceof Group)) return -1;
 			Group myGroup = (Group) myGroupObject;
                         //log.debug("this = " + this.grouping_id+this.group_name);
 			//log.debug("that = " + myGroup.grouping_id+myGroup.group_name);
                         //log.debug("this compared to that = " + (this.grouping_id+this.group_name).compareTo(myGroup.grouping_id+myGroup.group_name));
                         return (this.grouping_id+this.group_name).compareTo(myGroup.grouping_id+myGroup.group_name);
                 }
 
         	/**
          	 * Creates a new Group object and sets the data values to those retrieved from the database.
          	 * @param dataRow     the row of data corresponding to one Group
          	 * @return            A Group object with its values setup
          	 */
 
         	public Group setupGroup(String dataRow[]) {
 
                 	//log.debug("in setupGroup");
                 	//log.debug("dataRow= "); new Debugger().print(dataRow);
 
                 	Group newGroup = new Group();
                 	newGroup.setGrouping_id(Integer.parseInt(dataRow[0]));
                 	newGroup.setGrouping_name(dataRow[1]);
                 	newGroup.setCriterion(dataRow[2]);
                 	newGroup.setGroup_id(Integer.parseInt(dataRow[3]));
                 	newGroup.setGroup_number(Integer.parseInt(dataRow[4]));
                 	newGroup.setGroup_name(dataRow[5]);
                 	newGroup.setHas_expression_data(dataRow[6]);
                 	newGroup.setHas_genotype_data(dataRow[7]);
                 	newGroup.setParent(dataRow[8]);
 			return newGroup;
         	}
 
         	/**
          	 * Creates a new Group object and sets the grouping values to those retrieved from the database.
          	 * @param dataRow     the row of data corresponding to one grouping
          	 * @return            A Group object with its grouping values setup
          	 */
 
         	public Group setupGrouping(String dataRow[]) {
 
                 	//log.debug("in setupGrouping");
                 	//log.debug("dataRow= "); new Debugger().print(dataRow);
 
                 	Group newGroup = new Group();
                 	newGroup.setGrouping_id(Integer.parseInt(dataRow[0]));
                 	newGroup.setGrouping_name(dataRow[1]);
                 	newGroup.setCriterion(dataRow[2]);
                 	newGroup.setDataset_id(Integer.parseInt(dataRow[3]));
 			return newGroup;
         	}
 
 		/**
 		 * Gets the Group object that contains the group_number from myGroups.
 		 * @param myGroups     an array of Group objects
 		 * @param group_number     the group_number to find
 		 * @return            the Group object for the group_number
 		 */
         	public Group getGroupFromMyGroups(Group[] myGroups, int group_number) {
                 	for (int i=0;i<myGroups.length;i++) {
                 		if (myGroups[i].getGroup_number() == group_number) {
 					return myGroups[i];
 				}
 			}
 			return new Group(-99);
         	}
 
 		/**
 		 * Gets the Group object that contains the same parent from myGroups.
 		 * @param myGroups     an array of Group objects
 		 * @param parent     the parent to find
 		 * @return            the Group object for the parent
 		 */
         	public Group[] getGroupsWithSameParent(Group[] myGroups, String parent) {
 			//log.debug("in getGroupsWithSameParent. parent = " + parent);
 			List<Group> otherGroups = new ArrayList<Group>();
 			for (Group thisGroup : myGroups) {
                 		if (thisGroup.getParent() != null && thisGroup.getParent().equals(parent)) {
 					//log.debug("adding thisGroup = "+thisGroup);
 					otherGroups.add(thisGroup);	
 				}
 			}
 			return myObjectHandler.getAsArray(otherGroups, Group.class);
         	}
 
 		/**
 	 	* Retrieves the grouping object for the grouping_id.
 	 	* @param grouping_id	the identifier of the grouping
 	 	* @param conn	the database connection
 	 	* @throws            SQLException if a database error occurs
 	 	* @return            the grouping object
 	 	*/
   		public Group getGrouping(int grouping_id, Connection conn) throws SQLException {
 
 			//log.debug("in Group.getGrouping. grouping_id = " + grouping_id);
         		String query =
 				"select grouping_id, grouping_name, criterion, dataset_id "+
 				"from groupings "+
 				"where grouping_id = ?";
 	
 			//log.debug("query = "+query);
 			Results myResults = new Results(query, grouping_id, conn);
                 	String[] dataRow;
 			Group newGroup = null;
 			while ((dataRow = myResults.getNextRow()) != null) {
                        		newGroup = new Group().setupGrouping(dataRow);
 			} 
         		myResults.close();
 
   			return newGroup;
 		}
 
 
         	/**
          	* Gets the group assignments for each chip used in the grouping.
          	* @param grouping_id  the identifier of the grouping
          	* @param conn  the database connection
          	* @return            an array of UserChip objects
          	* @throws            SQLException if a database error occurs
          	*/
         	public User.UserChip[] getChipAssignments (int grouping_id, Connection conn) throws SQLException {
 			log.debug("in getChipAssignments passing in grouping_id. it is" + grouping_id);
 			setGrouping_id(grouping_id);
 			return getChipAssignments(conn);
 		}
 
 
         	/**
          	* Gets the group assignments for each chip used in the grouping.
          	* @param conn  the database connection
          	* @return            an array of UserChip objects
          	* @throws            SQLException if a database error occurs
          	*/
         	public User.UserChip[] getChipAssignments (Connection conn) throws SQLException {
 
 			log.debug("in getChipAssignments");
                 	String query =
                         	"select "+ 
 				"grpings.grouping_id, "+
 				"grpings.grouping_name, "+
 				"grpings.criterion, "+
 				"grps.group_id, "+
 				"grps.group_number, "+
 				"grps.group_name, "+
 				"grps.has_expression_data, "+
 				"grps.has_genotype_data, "+
 				"grps.parent, "+
                         	"uc.user_chip_id, "+
                         	"uc.hybrid_id, "+
 				"h.name "+
                         	"from user_chips uc, "+
 				"hybridizations h, "+
                         	"chip_groups cg, "+
                         	"groups grps, "+
 				"groupings grpings "+
                         	"where grps.grouping_id = grpings.grouping_id "+
 				"and uc.hybrid_id = h.hybrid_id "+
                         	"and cg.group_id = grps.group_id "+
                         	"and uc.user_chip_id = cg.user_chip_id "+
                         	"and grps.grouping_id = ? "+
                         	"order by grps.group_number, uc.hybrid_id";
 
                 	List<User.UserChip> myUserChipList = new ArrayList<User.UserChip>();
 
                 	log.info("In getChipAssignments grouping_id = " + this.grouping_id);
                 	//log.debug("query = "+ query);
 
                 	Results myResults = new Results(query, this.grouping_id, conn);
                 	String[] dataRow;
 
                 	while ((dataRow = myResults.getNextRow()) != null) {
                         	Group newGroup = new Group().setupGroup(dataRow);
 				User.UserChip newUserChip = new User().new UserChip(Integer.parseInt(dataRow[9]));
 				newUserChip.setHybrid_id(Integer.parseInt(dataRow[10]));
 				newUserChip.setHybrid_name(dataRow[11]);
 				newUserChip.setGroup(newGroup);
                         	myUserChipList.add(newUserChip);
                 	}
 
                 	myResults.close();
 
                 	User.UserChip[] myUserChipArray = myObjectHandler.getAsArray(myUserChipList, User.UserChip.class);
                 	//log.debug("myUserChipArray= "); new Debugger().print(myUserChipArray);
 
                 	return myUserChipArray;
         	}
 
 		/**
 	 	* Compares Groups based on different fields.
 	 	*/
 		public class GroupSortComparator implements Comparator<Group> {
 			int compare;
         		Group group1, group2;
 
         		public int compare(Group object1, Group object2) {
 				String sortColumn = getSortColumn();
 				String sortOrder = getSortOrder();
 
 				if (sortOrder.equals("A")) {
 					group1 = object1;
 	                		group2 = object2;
 					// default for null columns for ascending order
 					compare = 1;
 				} else {
 					group2 = object1;
 	                		group1 = object2;
 					// default for null columns for ascending order
 					compare = -1;
 				}
 
                 		//log.debug("group1 = "+group1+ ", group2 = "+group2);
 
 				if (sortColumn.equals("groupID")) {
                 			compare = new Integer(group1.getGroup_id()).compareTo(new Integer(group2.getGroup_id()));
 				} else if (sortColumn.equals("groupName")) {
 					compare = group1.getGroup_name().compareTo(group2.getGroup_name());
 				} else if (sortColumn.equals("has_expression_data")) {
 					compare = group1.getHas_expression_data().compareTo(group2.getHas_expression_data());
 				} else if (sortColumn.equals("has_genotype_data")) {
 					compare = group1.getHas_genotype_data().compareTo(group2.getHas_genotype_data());
 				} else if (sortColumn.equals("parent")) {
 					compare = group1.getParent().compareTo(group2.getParent());
 				} else if (sortColumn.equals("groupNumber")) {
                 			compare = new Integer(group1.getGroup_number()).compareTo(new Integer(group2.getGroup_number()));
 				}
                 		return compare;
 			}
 		}
 
 		public Group[] sortGroups (Group[] myGroups, String sortColumn, String sortOrder) {
 			setSortColumn(sortColumn);
 			setSortOrder(sortOrder);
 			Arrays.sort(myGroups, new GroupSortComparator());
 			return myGroups;
 		}
 	}
 }
 
