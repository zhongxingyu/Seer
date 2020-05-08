 package pals.actions.dataset;
 
 import java.io.File;
 
 import pals.actions.UserAwareAction;
 import pals.entity.DataSetVersion;
 import pals.service.DataSetVersionService;
 
 public class QCPlots extends UserAwareAction
 {
     Integer id;
     DataSetVersionService dataSetVersionService;
     DataSetVersion dataSetVersion;
     String imagePath;
 	
 	public String execute()
 	{
 		System.out.println(id);
 		this.dataSetVersion = dataSetVersionService.get(id);
 		File file = new File(dataSetVersion.retrieveQCPlotsFilePath());
 		imagePath = "../User/FileActionPNG.action?username=" +
	        getUser().getUsername() + "&filename=" + file.getName();
 		return SUCCESS;
 	}
 
 	public DataSetVersionService getDataSetVersionService() {
 		return dataSetVersionService;
 	}
 
 	public void setDataSetVersionService(DataSetVersionService dataSetVersionService) {
 		this.dataSetVersionService = dataSetVersionService;
 	}
 
 	public DataSetVersion getDataSetVersion() {
 		return dataSetVersion;
 	}
 
 	public void setDataSetVersion(DataSetVersion dataSetVersion) {
 		this.dataSetVersion = dataSetVersion;
 	}
 
 	public Integer getId() {
 		return id;
 	}
 
 	public void setId(Integer id) {
 		this.id = id;
 	}
 
 	public String getImagePath() {
 		return imagePath;
 	}
 
 	public void setImagePath(String imagePath) {
 		this.imagePath = imagePath;
 	}
 }
