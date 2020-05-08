 /**
  * 
 $Revision: 1.4 $"
 $Id: PrimaryDataset.java,v 1.4 2009/09/21 15:10:38 yuyi Exp $"
  *
  * Data Object from table : PRIMARY_DATASETS
 */
 
 package cms.dbs.dataobjs;
 
 import cms.dbs.dataobjs.PrimaryDSType;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 public class PrimaryDataset extends JSONObject {
 
 	public PrimaryDataset ( ) {
 
 	}
 
         public PrimaryDataset ( int primaryDSID, String primaryDSName, PrimaryDSType pType, int creationDate, String createBy ) throws Exception  {
 		
                 this.putOnce("PRIMARY_DS_ID", primaryDSID );
                 this.putOnce("PRIMARY_DS_NAME",  primaryDSName );
                 this.putOnce("PRIMARY_DS_TYPE_DO", pType );
                 this.putOnce("CREATION_DATE",  creationDate );
                 this.putOnce("CREATE_BY",  createBy );
         }
 
         public void setPrimaryDSID (int primaryDSID) throws Exception {
                 this.put( "PRIMARY_DS_ID", primaryDSID );
         }
 
         public void setPrimaryDSName (String primaryDSName) throws Exception {
                 this.put( "PRIMARY_DS_NAME", primaryDSName );
         }
 
        public void setPrimaryDSTypeID (int primaryDSTypeID) throws Exception {
                this.put( "PRIMARY_DS_TYPE_ID", primaryDSTypeID );
         }
 
         public void setCreationDate (int creationDate) throws Exception {
                 this.put( "CREATION_DATE", creationDate );
         }
 
         public void setCreateBy (String createBy) throws Exception {
                 this.put( "CREATE_BY", createBy );
         }
 
 	int getPrimaryDSID ( ) throws Exception{
 		int primaryDSID = 0;
                	if (!JSONObject.NULL.equals(this.getInt("PRIMARY_DS_ID"))) {
                        	primaryDSID = this.getInt("PRIMARY_DS_ID");
                	}
                 return primaryDSID;
         }
 	
 	public String getPrimaryDSName ( ) throws Exception{
 		String primaryDSName = null;
                	if (!JSONObject.NULL.equals(this.getString("PRIMARY_DS_NAME"))) {
                        	primaryDSName =  this.getString("PRIMARY_DS_NAME");
                	}
                 return primaryDSName;
         }
 	
 	public PrimaryDSType getPrimaryDSTypeDO ( ) throws Exception{
 		PrimaryDSType primaryDSType = null;
                	if (! this.isNull("PRIMARY_DS_TYPE_DO")) {
                        	primaryDSType = (PrimaryDSType)this.getJSONObject("PRIMARY_DS_TYPE_DO");
                	}
                 return primaryDSType;
         }
 	
 	int getCreationDate ( ) throws Exception{
 		int creationDate = 0;
                	if (!JSONObject.NULL.equals(this.getInt("CREATION_DATE"))) {
                        	creationDate = this.getInt("CREATION_DATE");
                	}
                 return creationDate;
         }
 	
 	public String getCreateBy ( ) throws Exception{
 		String createBy = null;
                	if (!JSONObject.NULL.equals(this.getString("CREATE_BY"))) {
                        	createBy = this.getString("CREATE_BY");
                	}
                 return createBy;
         }
 	
 }
