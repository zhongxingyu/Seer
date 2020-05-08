 package edu.jhu.cvrg.waveform.backing;
 /*
 Copyright 2013 Johns Hopkins University Institute for Computational Medicine
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
 /**
 * @author Chris Jurado
 * 
 */
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ViewScoped;
 import javax.faces.context.FacesContext;
 import javax.portlet.PortletSession;
 
 import org.primefaces.context.RequestContext;
 import org.primefaces.event.FileUploadEvent;
 import org.primefaces.event.NodeSelectEvent;
 
 import com.liferay.portal.model.User;
 
 import edu.jhu.cvrg.dbapi.dto.UploadStatusDTO;
 import edu.jhu.cvrg.dbapi.enums.EnumUploadState;
 import edu.jhu.cvrg.dbapi.factory.ConnectionFactory;
 import edu.jhu.cvrg.waveform.exception.UploadFailureException;
 import edu.jhu.cvrg.waveform.main.UploadManager;
 import edu.jhu.cvrg.waveform.model.FileTreeNode;
 import edu.jhu.cvrg.waveform.model.LocalFileTree;
 import edu.jhu.cvrg.waveform.utility.ResourceUtility;
 
 @ManagedBean(name="fileUploadBacking")
 @ViewScoped
 
 public class FileUploadBacking extends BackingBean implements Serializable{
 	
 	private static final long serialVersionUID = -4715402539808469047L;
 	
 	private LocalFileTree fileTree;
 	private String text;  
 	private User userModel;
 
 	private List<FacesMessage> messages;
 	
 	@PostConstruct
 	public void init() {
 		userModel = ResourceUtility.getCurrentUser();
 		if(fileTree == null && userModel != null){
 			fileTree = new LocalFileTree(userModel.getUserId());
 			
 			loadBackgroundQueue();
 		}
 		messages = new ArrayList<FacesMessage>();
 	}
 	
     public void handleFileUpload(FileUploadEvent event) {
     	//TODO: Implement a means for the user to input the studyID and the datatype.
     	fileUpload(event, "Mesa",  "Rhythm Strips");
     }
     
     private void fileUpload(FileUploadEvent event, String studyID, String datatype) {
 
     	getLog().info("Handling upload... Folder name is " + fileTree.getSelectFolder().getName());
 
     	UploadManager uploadManager = new UploadManager();
     	
 		InputStream fileToSave;
 		
 		UploadStatusDTO status = null;
 		String recordName = null;
 		
 		try {
 			fileToSave = event.getFile().getInputstream();
 
 			String fileName = event.getFile().getFileName();
 			long fileSize = event.getFile().getSize();
 			
 			int location = fileName.indexOf(".");
 			if (location != -1) {
 				recordName = fileName.substring(0, location);
 			} else {
 				recordName = fileName;
 			}
 
 			FileTreeNode existingFileNode = fileTree.getLeafByName(fileName);
 			
 			if( existingFileNode == null){
 				// The new 5th parameter has been added for character encoding, specifically for XML files.  If null is passed in,
 				// the function will use UTF-8 by default
 				status = uploadManager.processUploadedFile(fileToSave, fileName, recordName, fileSize, studyID, datatype, fileTree.getSelectFolder());
 				if(status != null){
 					if(status.getMessage() == null){
 						uploadManager.start();
 						//msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "" , event.getFile().getFileName() + " is uploaded.");	
 					}
 				}
 			}else{
 				throw new UploadFailureException("This file already exist at " + existingFileNode.getTreePath() +" folder.");
 			}
 		} catch (IOException e) {
 			status = new UploadStatusDTO(null, null, null, null, null, Boolean.FALSE, event.getFile().getFileName() + " failed to upload.  Could not read file.");
 			status.setRecordName(recordName);
 			
 		} catch (UploadFailureException ufe) {
 			status = new UploadStatusDTO(null, null, null, null, null, Boolean.FALSE, event.getFile().getFileName() + " failed because:  " + ufe.getMessage());
 			status.setRecordName(recordName);
 			
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			status = new UploadStatusDTO(null, null, null, null, null, Boolean.FALSE, event.getFile().getFileName() + " failed to upload for unknown reasons");
 			status.setRecordName(recordName);
 		}
 		this.addToBackgroundQueue(status);
 		
     }
     
     public void onNodeSelect(NodeSelectEvent event) { 
     	getLog().info("Node selected... ID is " + fileTree.getSelectedNodeId());
     }
     
     public String getText() {return text;}  
     public void setText(String text) {this.text = text;}
 
 	public LocalFileTree getFileTree() {
 		return fileTree;
 	}
 
 	public void setFileTree(LocalFileTree fileTree) {
 		this.fileTree = fileTree;
 	}
 	
     public void onComplete() {
     	fileTree.initialize(userModel.getUserId());
     	messages.clear();
     }
     
     public String getSummary(){
     	int done = 0;
     	int error = 0;
     	List<UploadStatusDTO> backgroundQueue = this.getBackgroundQueue();
     	if(backgroundQueue!=null && !backgroundQueue.isEmpty()){
     		StringBuilder sb = new StringBuilder();
     		for (UploadStatusDTO u : backgroundQueue) {
     			if(u != null){
 	    			switch (u.getState()) {
 						case DONE: done++; break;
 						case ERROR: error++; break;
 						default:break;
 					}
     			}
 				
 			}
     		sb.append("Summary ").append(backgroundQueue.size());
     		
     		if(backgroundQueue.size() > 1){
    			sb.append(" itens [");
     		}else{
     			sb.append(" item [");
     		}
     		
     		sb.append(done).append(" done - ").append(error);
     		
     		if(error > 1){
     			sb.append(" fail(s)]");
     		}else{
     			sb.append(" fail]");
     		}
     		
     		return sb.toString();
     	}
     	
     	
     	return null;
     }
 	
 	public User getUser(){
 		return userModel;
 	}
 	
 	
 	public void loadBackgroundQueue(){
 		Set<Long> listenIds = null;
 		
 		List<UploadStatusDTO> backgroundQueue = this.getBackgroundQueue();
 		
         if(backgroundQueue != null && !backgroundQueue.isEmpty()){
         	listenIds = new HashSet<Long>();
         	for (UploadStatusDTO s : backgroundQueue) {
 				if(s != null && s.getDocumentRecordId() != null){
 					listenIds.add(s.getDocumentRecordId());
 				}
 			}
         }
         if(listenIds != null && !listenIds.isEmpty()){
         	List<UploadStatusDTO> tmpBackgroundQueue = ConnectionFactory.createConnection().getUploadStatusByUserAndDocId(userModel.getUserId(), listenIds);
         	if(tmpBackgroundQueue != null){
 		        for (UploadStatusDTO s : tmpBackgroundQueue) {
 					if(backgroundQueue.contains(s)){
 						backgroundQueue.get(backgroundQueue.indexOf(s)).update(s);
 					}
 				}
 	        }else{
 	        	backgroundQueue.clear();
 	        }
         }
         
         if(backgroundQueue != null){
         	boolean stopListening = false;
         	for (UploadStatusDTO u : backgroundQueue) {
         		stopListening = (EnumUploadState.DONE.equals(u.getState()) || EnumUploadState.ERROR.equals(u.getState()) || EnumUploadState.WAIT.equals(u.getState()));
         		if(!stopListening){
         			break;
         		}
 			}
         	
         	if(stopListening){
         		RequestContext context = RequestContext.getCurrentInstance();
     			context.execute("stopListening();");
     			context.execute("totalFiles = " + backgroundQueue.size());
         	}
         }
         
 	}
 
 	public List<UploadStatusDTO> getBackgroundQueue() {
 		PortletSession session = (PortletSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
 		return (List<UploadStatusDTO>) session.getAttribute("upload.backgroundQueue");
 	}
 	
 	
 	public void addToBackgroundQueue(UploadStatusDTO dto) {
 		if(dto!=null){
 			if(this.getBackgroundQueue() == null){
 				setBackgroundQueue(new ArrayList<UploadStatusDTO>());
 			}
 			int index = this.getBackgroundQueue().indexOf(dto);
 			if(index != -1){
 				if(EnumUploadState.WAIT.equals(this.getBackgroundQueue().get(index).getState())){
 					this.getBackgroundQueue().remove(index);	
 				}
 			}
 			this.getBackgroundQueue().add(dto);	
 		}
 	}
 
 	public void setBackgroundQueue(List<UploadStatusDTO> backgroundQueue) {
 		PortletSession session = (PortletSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
 		session.setAttribute("upload.backgroundQueue", backgroundQueue);
 	}
 	
     public void removeTableItem(){
     	Map<String,String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
         String index = params.get("index");
         
     	if(index != null ){
     		int indexTableToRemove = Integer.parseInt(index);
     		
     		List<UploadStatusDTO> backgroundQueue = this.getBackgroundQueue();
     		
     		if(indexTableToRemove >= 0 && (backgroundQueue != null && backgroundQueue.size() > indexTableToRemove)){
     			backgroundQueue.remove(indexTableToRemove);
     		}
     		
     		RequestContext context = RequestContext.getCurrentInstance();
 			context.execute("removeFile();");
     	}
     }
     
     public void removeAllDoneItem(){
     	List<UploadStatusDTO> backgroundQueue = this.getBackgroundQueue();
     	
 		if(backgroundQueue != null && !backgroundQueue.isEmpty()){
 			
 			List<UploadStatusDTO> toRemove = new ArrayList<UploadStatusDTO>();
 			for (UploadStatusDTO dto : backgroundQueue) {
 				if(EnumUploadState.DONE.equals(dto.getState())){
 					toRemove.add(dto);
 				}
 			}
 			backgroundQueue.removeAll(toRemove);
 			
 			RequestContext context = RequestContext.getCurrentInstance();
 			context.execute("totalFiles = " + backgroundQueue.size());
 		}
 	}
     
     public boolean isShowBackgroundPanel(){
     	return this.getBackgroundQueue() != null && !this.getBackgroundQueue().isEmpty(); 
     }
 
 }
