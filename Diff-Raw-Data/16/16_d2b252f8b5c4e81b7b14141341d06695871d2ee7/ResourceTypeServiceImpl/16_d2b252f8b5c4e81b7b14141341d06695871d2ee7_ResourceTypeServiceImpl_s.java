 package org.imirsel.nema.webapp.service.impl;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.imirsel.nema.model.FileDataType;
 import org.imirsel.nema.model.GroupDataType;
 import org.imirsel.nema.model.NemaTask;
 import org.imirsel.nema.model.OsDataType;
 import org.imirsel.nema.model.fileTypes.NemaFileType;
 import org.imirsel.nema.model.util.FileConversionUtil;
 import org.imirsel.nema.repository.RepositoryClientConnectionPool;
 import org.imirsel.nema.repositoryservice.RepositoryClientInterface;
 import org.imirsel.nema.webapp.service.ResourceTypeService;
 
 
 
 /**Returns various resource types
  * 
  * @author kumaramit01
  * @since 0.8.0 -moved from the content repository client
  *  
  */
 final public class ResourceTypeServiceImpl implements ResourceTypeService {
 	
 	/**Version of this class
 	 * 
 	 */
 	static private Log logger = LogFactory.getLog(ResourceTypeServiceImpl.class);
 
 	private static final long serialVersionUID = 5186004401706711111L;
 	private final List<OsDataType> supportedOsList  = new ArrayList<OsDataType>();
 	private final List<GroupDataType> groupList = new ArrayList<GroupDataType>();
 	
 	// injected
 	private RepositoryClientConnectionPool repositoryClientConnectionPool;
 	/**
 	 * 
 	 */
 	public ResourceTypeServiceImpl(){
 		supportedOsList.add(new OsDataType("Unix","Unix Like"));
 		supportedOsList.add(new OsDataType("Windows","Windows Like"));
 		groupList.add(new GroupDataType("imirsel","imirsel"));
 	}
 	
 	/**
 	 *  @param taskId the numeric id of the task
 	 *  @return list of the input file data types that are relevant to the taskId
 	 */
 	public final List<FileDataType> getSupportedInputFileDataTypes(int taskId) throws SQLException{
 		logger.debug("Start loading Input file list for task "+taskId+" at "+System.currentTimeMillis());
 		RepositoryClientInterface client = repositoryClientConnectionPool.getFromPool();
 		List<FileDataType> flist = new ArrayList<FileDataType>();
 		try {
 			List<Class<? extends NemaFileType>>  list=FileConversionUtil.getInputFileTypesForTask(taskId, client);
			for(Class<? extends NemaFileType> claszz:list){
				Object object = claszz.newInstance();
				FileDataType fdt = new FileDataType(claszz.newInstance().getTypeName(),claszz.getName());
				flist.add(fdt);
 			}
 			return flist;
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally{
 				if(client!=null)
 				this.repositoryClientConnectionPool.returnToPool(client);
 				logger.debug("Finish loading Input file list for task "+taskId+" at "+System.currentTimeMillis());
 			
 		}
 		return flist;
 	}
 	
 	
 	
 	/**
 	 *  @param taskId the numeric id of the task
 	 *  @return list of the output file data types that are relevant to the taskId
 	 */
 	public final List<FileDataType> getSupportedOutputFileDataTypes(int taskId) throws SQLException{
 		
 		RepositoryClientInterface client = repositoryClientConnectionPool.getFromPool();
 		List<FileDataType> flist = new ArrayList<FileDataType>();
 		try {
 			List<Class<? extends NemaFileType>>  list=FileConversionUtil.getOutputFileTypesForTask(taskId, client);
 			for(Class<? extends NemaFileType> claszz:list){
 				FileDataType fdt = new FileDataType(claszz.newInstance().getTypeName(),claszz.getName());
 				flist.add(fdt);
 			}
 			return flist;
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}finally{
 			if(client!=null)
 			this.repositoryClientConnectionPool.returnToPool(client);
 			
 		}
 		return flist;
 		
 	}
 	
 	
 	/**
 	 * 
 	 */
 	public final List<OsDataType> getSupportedOperatingSystems(){
 		return Collections.unmodifiableList(this.supportedOsList);
 	}
 	/**
 	 * 
 	 */
 	public final List<GroupDataType> getSupportedGroups(){
 		return Collections.unmodifiableList(this.groupList);
 	}
 	
 	
 	/**
 	 * Returns the OsDataType for a string value
 	 * 
 	 * @param value
 	 * @return OsDataType -returns the OsDataType
 	 */
 	public final OsDataType getOsDataType(String value){
 		if(value.equalsIgnoreCase("Unix Like")){
 			OsDataType os = new OsDataType("Unix","Unix Like");
 			return os;
 		}else if(value.equals("Windows Like")){
 			OsDataType os = new OsDataType("Windows","Windows Like");
 			return os;
 		}else{
 			throw new IllegalArgumentException("error invalid Os: " + value);
 		}
 		
 	}
 
 	public void setRepositoryClientConnectionPool(
 			RepositoryClientConnectionPool repositoryClientConnectionPool) {
 		this.repositoryClientConnectionPool = repositoryClientConnectionPool;
 	}
 
 	public RepositoryClientConnectionPool getRepositoryClientConnectionPool() {
 		return repositoryClientConnectionPool;
 	}
 
 	@Override
 	public List<NemaTask> getSupportedTasks() throws SQLException {
 		RepositoryClientInterface client = repositoryClientConnectionPool.getFromPool();
 		List<NemaTask> list = null;
 		try {
 			list=client.getTasks();
 			logger.debug("Load list of taskIds, Total #:"+(list==null?"none":list.size()));
 		}finally{
 				if(client!=null)
 				this.repositoryClientConnectionPool.returnToPool(client);
 				return list;
 		}
 		
 		
 	}
 
 }
