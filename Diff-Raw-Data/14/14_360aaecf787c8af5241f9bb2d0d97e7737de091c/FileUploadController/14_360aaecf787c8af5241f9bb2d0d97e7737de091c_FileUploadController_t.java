 package mybeans;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
 import javax.faces.event.AjaxBehaviorEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.servlet.ServletContext;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.io.IOUtils;
 import org.primefaces.context.RequestContext;
 import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RateEvent;
import org.primefaces.event.SlideEndEvent;
 import org.primefaces.event.data.PageEvent;
 import org.primefaces.model.UploadedFile;
 
 import java.lang.System;
 import java.util.Date;
 import java.sql.SQLException;
 
 import mybeans.mydb.assignments.AssignmentBean;
 import mybeans.mydb.assignments.model.Assignment;
 
 @ManagedBean(name="controller")
 @SessionScoped
 public class FileUploadController {
 
     // Init ---------------------------------------------------------------------------------------
 
     private UploadedFile zipFile;
     private UploadedFile imageFile;
     private UploadedFile pdfFile;
     private String pdfFilename;
     private String zipFilename;
     private String imageFilename;
     public String selectedPdfFilename = "Choose a pdf file...";
     public String selectedZipFilename = "Choose a rubric file...";
     public String selectedImageFilename = "Choose an image file...";
     public int point = 0;
     public Integer rating = 0;
     public String description = "";
     public String thumbnail = "/images/noimage.png";
     public String assignmentName = "";
     public String author = "";
     public String outMsg = "";
     
     public UploadedFile getPdfFile() {
 		return pdfFile;
 	}
 
 	public void setPdfFile(UploadedFile pdfFile) {
 		this.pdfFile = pdfFile;
 	}
 
 	public String getPdfFilename() {
 		return pdfFilename;
 	}
 
 	public void setPdfFilename(String pdfFilename) {
 		this.pdfFilename = pdfFilename;
 	}
 
 	public String getSelectedPdfFilename() {
 		return selectedPdfFilename;
 	}
 
 	public void setSelectedPdfFilename(String selectedPdfFilename) {
 		this.selectedPdfFilename = selectedPdfFilename;
 	}
 
 	public String getOutMsg() {
 		return outMsg;
 	}
 
 	public void setOutMsg(String outMsg) {
 		this.outMsg = outMsg;
 	}
 
 	public String getAuthor() {
 		return author;
 	}
 
 	public void setAuthor(String author) {
 		this.author = author;
 	}
 
 	public String getAssignmentName() {
 		return assignmentName;
 	}
 
 	public void setAssignmentName(String assignmentName) {
 		this.assignmentName = assignmentName;
 	}
 
 	public String getThumbnail() {
 		return thumbnail;
 	}
 
 	public void setThumbnail(String thumbnail) {
 		this.thumbnail = thumbnail;
 	}
 
 
 	private String document_dict;
     private String ss_dict;
     private String pdf_dict;
 
 
 
 	public Integer getRating() {
 		return rating;
 	}
 
 	public void setRating(Integer rating) {
 		this.rating = rating;
 	}
 
 	public UploadedFile getZipFile() {
 		return zipFile;
 	}
 
 	public void setZipFile(UploadedFile zipFile) {
 		this.zipFile = zipFile;
 	}
 
 	public UploadedFile getImageFile() {
 		return imageFile;
 	}
 
 	public void setImageFile(UploadedFile imageFile) {
 		this.imageFile = imageFile;
 	}
 
 	public String getZipFilename() {
 		return zipFilename;
 	}
 
 	public void setZipFilename(String zipFilename) {
 		this.zipFilename = zipFilename;
 	}
 
 	public String getImageFilename() {
 		return imageFilename;
 	}
 
 	public void setImageFilename(String imageFilename) {
 		this.imageFilename = imageFilename;
 	}
 
 	public String getSelectedZipFilename() {
 		return selectedZipFilename;
 	}
 
 	public void setSelectedZipFilename(String selectedZipFilename) {
 		this.selectedZipFilename = selectedZipFilename;
 	}
 
 	public String getSelectedImageFilename() {
 		return selectedImageFilename;
 	}
 
 	public void setSelectedImageFilename(String selectedImageFilename) {
 		this.selectedImageFilename = selectedImageFilename;
 	}
 
 	public int getPoint() {
 		return point;
 	}
 
 	public void setPoint(int point) {
 		this.point = point;
 	}
 
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getDocument_dict() {
 		return document_dict;
 	}
 
 	public void setDocument_dict(String document_dict) {
 		this.document_dict = document_dict;
 	}
 
 	public String getSs_dict() {
 		return ss_dict;
 	}
 
 	public void setSs_dict(String ss_dict) {
 		this.ss_dict = ss_dict;
 	}
 
 	public boolean addNew() {
 		if(zipFile!=null){
         // Prepare filename prefix and suffix for an unique filename in upload folder.
         String prefix = FilenameUtils.getBaseName(zipFile.getFileName());
         prefix = prefix.replaceAll("\\s","");
         String suffix = FilenameUtils.getExtension(zipFile.getFileName());
         
         // Prepare file and outputstream.
         File file = null;
         OutputStream output = null;
         
         try {
             // Create file with unique name in upload folder and write to it.
         	ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
         	String deploymentDirectoryPath = ctx.getRealPath("/")+"Rubrics";
         	File target = new File(deploymentDirectoryPath);
         	if(!target.exists())
         		target.mkdir();
         	//if(directory.exists())
         	file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
             output = new FileOutputStream(file);
             IOUtils.copy(zipFile.getInputstream(), output);
             zipFilename = file.getName();
             document_dict = "Rubrics/"+zipFilename;
 
             // Show succes message.
             FacesContext.getCurrentInstance().addMessage("uploadForm", new FacesMessage(
                 FacesMessage.SEVERITY_INFO, deploymentDirectoryPath, null));
             
             AssignmentBean assbean = new AssignmentBean();
             
             if(pdfFile!=null){
             	deploymentDirectoryPath=ctx.getRealPath("/")+"Pdf";
             	target = new File(deploymentDirectoryPath);
                 if(!target.exists())
                 	target.mkdir();
                 
                 prefix = FilenameUtils.getBaseName(pdfFile.getFileName());
                 prefix = prefix.replaceAll("\\s","");
                 suffix = FilenameUtils.getExtension(pdfFile.getFileName());
                 file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
                 output = new FileOutputStream(file);
                 IOUtils.copy(pdfFile.getInputstream(), output);
                 pdfFilename = file.getName().trim();
                 pdf_dict = "Pdf/"+pdfFilename;
             }else
             	pdf_dict="";
             
             try {
             	if(imageFile!=null){
             		deploymentDirectoryPath=ctx.getRealPath("/")+"Screenshot";
             		target = new File(deploymentDirectoryPath);
                 	if(!target.exists())
                 		target.mkdir();
                 	
                     prefix = FilenameUtils.getBaseName(imageFile.getFileName());
                     prefix = prefix.replaceAll("\\s","");
                     suffix = FilenameUtils.getExtension(imageFile.getFileName());
                 	file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
                     output = new FileOutputStream(file);
                     IOUtils.copy(imageFile.getInputstream(), output);
                     imageFilename = file.getName().trim();
                     String ss_dict = "Screenshot/"+imageFilename;
             		assbean.addAssignment(document_dict,this.description,ss_dict, point, rating,assignmentName,author,pdf_dict);
             	}
             	else
             		assbean.addAssignment(document_dict,this.description,"/images/noimage.png", point, rating, assignmentName, author, pdf_dict);
 			} catch (SQLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
             
         } catch (IOException e) {
             // Cleanup.
             if (file != null) file.delete();
 
             // Show error message.
             FacesContext.getCurrentInstance().addMessage("uploadForm", new FacesMessage(
                 FacesMessage.SEVERITY_ERROR, "File upload failed with I/O error.", null));
 
             // Always log stacktraces (with a real logger).
             e.printStackTrace();
         } finally {
             IOUtils.closeQuietly(output);
             reset();
         }
         return true;
 		}
 		else return false;  
     }
     
     public boolean updateExist(int fileID) throws SQLException {
        	AssignmentBean assbean = new AssignmentBean();
        	
     	
         // Prepare filename prefix and suffix for an unique filename in upload folder.
         String prefix;
         String suffix;
         ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
         // Prepare file and outputstream.
         File file = null;
         OutputStream output = null;
         if(zipFile!=null){
         	try {
         		// Create file with unique name in upload folder and write to it.
         		
         		String deploymentDirectoryPath = ctx.getRealPath("/")+"Rubrics";
         		File target = new File(deploymentDirectoryPath);
             	if(!target.exists())
             		target.mkdir();
                 prefix = FilenameUtils.getBaseName(zipFile.getFileName());
                 prefix = prefix.replaceAll("\\s","");
                 suffix = FilenameUtils.getExtension(zipFile.getFileName());
         		file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
         		output = new FileOutputStream(file);
         		IOUtils.copy(zipFile.getInputstream(), output);
         		zipFilename = file.getName().trim();
         		document_dict = "Rubrics/"+zipFilename;
         	}   catch (IOException e) {
                 // Cleanup.
                 if (file != null) file.delete();
         	}finally{
         		IOUtils.closeQuietly(output);
         	}
         	}
 
           if(imageFile!=null){
         	  try {
 				String deploymentDirectoryPath=ctx.getRealPath("/")+"Screenshot";
 				File target = new File(deploymentDirectoryPath);
             	if(!target.exists())
             		target.mkdir();
 			    prefix = FilenameUtils.getBaseName(imageFile.getFileName());
 			    prefix = prefix.replaceAll("\\s","");
 			    suffix = FilenameUtils.getExtension(imageFile.getFileName());
 				file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
 			    output = new FileOutputStream(file);
 			    IOUtils.copy(imageFile.getInputstream(), output);
 			    imageFilename = file.getName();
 			    ss_dict = "Screenshot/"+imageFilename.trim();
         	  }catch (IOException e) {
                   // Cleanup.
                   if (file != null) file.delete();
           		}finally{
           			IOUtils.closeQuietly(output);
           		}
 			}
           
           if(pdfFile!=null){
         	  try {
 				String deploymentDirectoryPath=ctx.getRealPath("/")+"Pdf";
 				File target = new File(deploymentDirectoryPath);
             	if(!target.exists())
             		target.mkdir();
 			    prefix = FilenameUtils.getBaseName(pdfFile.getFileName());
 			    prefix = prefix.replaceAll("\\s","");
 			    suffix = FilenameUtils.getExtension(pdfFile.getFileName());
 				file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
 			    output = new FileOutputStream(file);
 			    IOUtils.copy(pdfFile.getInputstream(), output);
 			    pdfFilename = file.getName();
 			    pdf_dict = "Pdf/"+pdfFilename.trim();
         	  }catch (IOException e) {
                   // Cleanup.
                   if (file != null) file.delete();
           		}finally{
           			IOUtils.closeQuietly(output);
           		}
 			}
           	
           	assbean.updateAssignment(document_dict, fileID, description, ss_dict, point, rating, assignmentName, author, pdf_dict);
           	reset();
           	return true;
     }
     
     public boolean delete(int fileID) throws SQLException{
     	AssignmentBean assb = new AssignmentBean();
     	assb.deleteAssignment(fileID);
     	return true;
     }
 
     // actionListener handle ------------------------------------------------------------------------------------
 
     public void ajaxHandler() throws SQLException{
     	AssignmentBean assb = new AssignmentBean();
     	boolean b = assb.checkDupicateAssignment(assignmentName);
     	if(b)
     		outMsg = "";
     	else
     		outMsg = assignmentName+" is not available, please name another one";
     }
     
     public void handlePdfUpload(FileUploadEvent event){
     	System.out.println("enter pdf upload handler");
     	pdfFile = event.getFile();
     	selectedPdfFilename = event.getFile().getFileName();
     	System.out.println(selectedPdfFilename);
     }
     
     public void handleZipUpload(FileUploadEvent event) {  
     	zipFile=event.getFile();
     	selectedZipFilename=event.getFile().getFileName();
     }
 
     public void handleImageUpload(FileUploadEvent event) {  
     	imageFile=event.getFile();
     	selectedImageFilename=event.getFile().getFileName();
     	
         // Prepare filename prefix and suffix for an unique filename in upload folder.
         String prefix = "thumbnail";
         String suffix;
         ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
         // Prepare file and outputstream.
         File file = null;
         OutputStream output = null;
         if(imageFile!=null){
         	try{				
         		String deploymentDirectoryPath=ctx.getRealPath("/")+"TempThumb";
         		File target = new File(deploymentDirectoryPath);
         		if(!target.exists())
         			target.mkdir();
         		suffix = FilenameUtils.getExtension(imageFile.getFileName());
         		file = File.createTempFile(prefix + "_", "." + suffix,new File(deploymentDirectoryPath));
         		output = new FileOutputStream(file);
         		IOUtils.copy(imageFile.getInputstream(), output);
         		thumbnail = "/TempThumb/"+file.getName();
         	}catch (IOException e) {
               // Cleanup.
               if (file != null) file.delete();
       		}finally{
       			IOUtils.closeQuietly(output);
       		}
         }
     }
     
     public void descriptionHandler(ValueChangeEvent event){
     }
     
     public void loadTarget(int a_id) throws SQLException{
        	AssignmentBean assbean = new AssignmentBean();
     	Assignment targetAss = assbean.getAssignmentByID(a_id);
     	document_dict = targetAss.assignmentDirectory;
     	ss_dict = targetAss.screenDirectory;
     	description = targetAss.description;
     	point = targetAss.point;
     	rating = targetAss.rating;
     	thumbnail = ss_dict;
     	assignmentName = targetAss.assignmentName;
     	author = targetAss.author;
     	pdf_dict = targetAss.pdfDirectory;
     }
     
    public void onrate(RateEvent rateEvent){
    	System.out.println("get rating from client"+ rateEvent.getRating());
    	rating = ((Integer)rateEvent.getRating()).intValue();
    }
    
    public void onSlideEnd(SlideEndEvent event){
    	point = event.getValue();
    }
   
 
 	public void reset(){
     	zipFile=null;
     	imageFile=null;
     	pdfFile = null;
     	point=0;
     	rating=0;
     	description = "";
     	author = "";
     	thumbnail = "/images/noimage.png";
     	selectedPdfFilename = "Choose a pdf file...";
     	selectedZipFilename="Choose a rubric file...";
     	selectedImageFilename="Choose an image file...";
     	outMsg="";
     	assignmentName="";
     }
 }
