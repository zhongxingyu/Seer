 package com.ceng316.controller;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map.Entry;
 
 import com.ceng316.model.MetaReader.RETCODES;
 import com.ceng316.model.metafile.MetaFile;
 import com.ceng316.model.util.xml.XMLSaver;
 import com.ceng316.view.FileData;
 
 public class MRController {
 	private ArrayList<MetaFile> metaFiles;
 	
 	public MRController(){
 		this.metaFiles = new ArrayList<MetaFile>();
 	}
 	
 	public ArrayList<MetaFile> getMetaFiles(){
 		return metaFiles;
 	}
 	
 	
 	public void fillFileData(FileData fileData,int index){
 		String extra = "";
 
 		if(index >= 0){
 			fileData.getTextName().setText(control(getMetaFiles().get(index).getFileInfo().getFileName()));
 			fileData.getTextPath().setText(control(getMetaFiles().get(index).getFileInfo().getFilePath().toString()));
 			fileData.getTextType().setText(control(getMetaFiles().get(index).getFileInfo().getType().toString()));
 			
 			fileData.getTextOwner().setText(control(getMetaFiles().get(index).getMetaData().getOwner()));
 			fileData.getTextMdf().setText(control(getMetaFiles().get(index).getMetaData().getModificationDate()));
 			fileData.getTextCreation().setText(control(getMetaFiles().get(index).getMetaData().getCreationDate()));
 			fileData.getTextLoc().setText(getMetaFiles().get(index).getMetaData().getLocation().toString());
 			fileData.getTextPlatform().setText(control(getMetaFiles().get(index).getMetaData().getPlatform()));
 			fileData.getTextApp().setText(control(getMetaFiles().get(index).getMetaData().getApplication()));
 
 			for ( Entry<String, String> extraData :getMetaFiles().get(index).getMetaData().getExtraData().entrySet()){
 				extra += extraData.getKey() + ":" + control(extraData.getValue()) + "\n";
 			}
 			fileData.getTextExtraData().setText(extra);
 		}
 				
 	}
 	
 	public void saveMetadata(String filePath, int index){
		String extension;

		extension = metaFiles.get(index).getFileInfo().getFileName().replaceFirst("[.][^.]+$", "");
 		
		XMLSaver.save(filePath + "/" + extension, metaFiles.get(index));
 	}
 	
 	public void saveAllMetadata(String filePath){			
 		Date date = new Date() ;
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss") ;
 		XMLSaver.save(filePath + "/" + new File(filePath).getParentFile().getName() 
							+ "-" + dateFormat.format(date), metaFiles);
 	}
 	
 	public String control(Object object){
 		if (object == null)
 			return "Unknown";
 		return object.toString();
 	}
 	
 	/* error message arrangement */
 	public String getErrorMsg(RETCODES retCode){
 		String errorMSG = "Some thing was wrong.";
 		
 		switch (retCode) {
 		case FILENOTFOUND:
 			errorMSG = "File couldn't found.";				
 			break;
 		case UNKNOWNTYPE:
 			errorMSG = "File type doesn't supported yet.";
 			break;
 		case WRONGFILETYPE:
 			errorMSG = "The selected file type doesn't match the real file type.";
 			break;
 		case UNKNOWNERROR:
 			errorMSG = "An unknown error occured.";
 			break;
 		default:
 			break;
 		}
 		return errorMSG;
 	}
 
 }
