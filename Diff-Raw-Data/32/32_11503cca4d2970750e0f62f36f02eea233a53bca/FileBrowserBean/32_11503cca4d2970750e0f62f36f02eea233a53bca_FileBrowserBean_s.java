 package dk.statsbiblioteket.doms.gui.filebrowser;
 
 
 import dk.statsbiblioteket.doms.central.InvalidResourceException;
 import dk.statsbiblioteket.doms.central.MethodFailedException;
 import dk.statsbiblioteket.doms.client.datastreams.Datastream;
 import dk.statsbiblioteket.doms.client.exceptions.NotFoundException;
 import dk.statsbiblioteket.doms.client.exceptions.ServerOperationFailed;
 import dk.statsbiblioteket.doms.client.objects.DigitalObject;
 import dk.statsbiblioteket.doms.gui.DomsManagerBean;
 import dk.statsbiblioteket.doms.repository.RepositoryBean;
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.*;
 import org.jboss.seam.annotations.datamodel.DataModel;
 import org.jboss.seam.faces.FacesMessages;
 import org.jboss.seam.log.Log;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 import javax.xml.rpc.ServiceException;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 
 @Name("fileBrowserBean")
 @Scope(ScopeType.PAGE)
 public class FileBrowserBean {
 
 	@In(create=true)
     DomsManagerBean domsManager;
 
 	@In(create = true)
 	private RepositoryBean repository;
 
 	@Logger
 	 private Log logger;
 
 	private String sourcePath;
 
 	@DataModel
 	private List<FileSystemNode> srcRoots;
 
 	private FileSystemNode selectedFile;
 
 	public List<FileSystemNode> getSourceRoots() {
 		if (srcRoots == null) {
 			srcRoots = new FileSystemNode(new File(sourcePath)).getNodes();
 		}
 
 		return srcRoots;
 	}
 
 	@Create
 	public void setupFileBrowser() {
 		this.sourcePath = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("sharedFilesPath");
 		File f = new File(sourcePath);
 		if(!f.exists()) {
 			FacesMessages.instance().add(
 					new FacesMessage("Shared drive could not be opened at the assigned path: " + this.sourcePath));
 					logger.error("Shared drive could not be opened at the assigned path: "
 							+ this.sourcePath + " \nThe path can be changed in web.xml:sharedFilesPath");
 		}
 	}
 
 
 	public FileSystemNode getSelectedFile() {
 
 		return selectedFile;
 	}
 
 	public void setSelectedFile(FileSystemNode selectedFile) {
 		this.selectedFile = selectedFile;
 	}
 
 	public void selectFileButtonPressed() throws ServerOperationFailed, NotFoundException {
 		if(selectedFile == null) {
 			FacesMessages.instance().add(new FacesMessage("Please select a file"));
 			return ;
 		}
 		if(selectedFile.isDirectory()){
 			FacesMessages.instance().add(new FacesMessage("Directory selected. Please select a file"));
 			//return ;
 		}
 		DigitalObject dataobject = domsManager.getSelectedDataObject();
 		if (dataobject != null) {
 			Datastream ds = dataobject.getDatastream("UPLOADABLE");
 			if(ds != null) {
 //				ds.setTemppath(this.selectedFile.getFile().getPath());
 			}
 		}
 		//return "/editRecord.xhtml";
 	}
 
 	public boolean getRenderFileBrowser() throws ServerOperationFailed, NotFoundException {
 
 		DigitalObject dataobject = domsManager.getSelectedDataObject();
 		if(dataobject != null) {
 			Datastream ds = null;//dataobject.getDatastream("UPLOADABLE");
 			if(ds != null) {
 //				return (ds.getUrl() == null);
                 return false;
 			}
 		}
 		return false;
 	}
 
 	public void uploadFileButtonPressed() throws ServiceException, IOException, MethodFailedException, InvalidResourceException, ServerOperationFailed {
 		if(selectedFile == null) {
 			FacesMessages.instance().add(new FacesMessage("Please use the 'browse' option to select a file to upload."));
 			return;
 		}
 		File f = selectedFile.getFile();
 		DigitalObject dataobject = domsManager.getSelectedDataObject();
 		repository.uploadFile(f, dataobject.getPid());
 //		dataobject.load(true,true);
 		FacesMessages.instance().add(new FacesMessage("File uploaded"));
 	}
 
 
 
 }
