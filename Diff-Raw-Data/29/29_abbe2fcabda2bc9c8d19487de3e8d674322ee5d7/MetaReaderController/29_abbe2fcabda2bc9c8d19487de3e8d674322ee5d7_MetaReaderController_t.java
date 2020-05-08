 package com.ceng316.controller;
 
 import java.nio.file.Path;
 import java.util.Map.Entry;
 
 import com.ceng316.model.MetaReader;
 import com.ceng316.model.MetaReader.RETCODES;
 import com.ceng316.model.metafile.FileType;
 import com.ceng316.model.util.xml.XMLSaver;
 import com.ceng316.view.FileUI;
 
 
 
 public class MetaReaderController extends MRController{
 	private FileUI fileUI;
 	private MetaReader reader;
 	
 	public MetaReaderController(FileUI fileUI){
 		this.fileUI = fileUI;
 	}
 	
 	public void getFileMetadata(Path filePath){
 		
 		reader = new MetaReader();
 		RETCODES retCode;
 		if(fileUI.controlRdtBtn() != -1){
 			retCode = reader.analyseFile(filePath,FileType.values()[fileUI.controlRdtBtn()]);
 		}else{
 			retCode = reader.analyseFile(filePath);
 		}
 		
 		reader.getMetaFile();
 		reader.getMetaFile().getMetaData();
 		reader.getMetaFile().getFileInfo();
 				
 		if(retCode == RETCODES.SUCCESS){
 			analyseFileMetadata(reader);
 		}else{
 			fileUI.setMsgBox(getErrorMsg(retCode));
 		}
 	}
 	
 	
 	/* The file metadata is gathered and filled to suitable places.*/
 	public void analyseFileMetadata(MetaReader reader){
 		String extra = "";
 		
 		fileUI.getFileData().getTextName().setText(control(reader.getMetaFile().getFileInfo().getFileName()));
 		fileUI.getFileData().getTextPath().setText(control(reader.getMetaFile().getFileInfo().getFilePath().toString()));
 		fileUI.getFileData().getTextType().setText(control(reader.getMetaFile().getFileInfo().getType().toString()));
 			
 		fileUI.getFileData().getTextOwner().setText(control(reader.getMetaFile().getMetaData().getOwner()));
 		fileUI.getFileData().getTextMdf().setText(control(reader.getMetaFile().getMetaData().getModificationDate()));
 		fileUI.getFileData().getTextCreation().setText(control(reader.getMetaFile().getMetaData().getCreationDate()));
 		fileUI.getFileData().getTextLoc().setText(control(reader.getMetaFile().getMetaData().getLocation().toString()));
 		fileUI.getFileData().getTextPlatform().setText(control(reader.getMetaFile().getMetaData().getPlatform()));
 		fileUI.getFileData().getTextApp().setText(control(reader.getMetaFile().getMetaData().getApplication()));
 			
 		for ( Entry<String, String> extraData :reader.getMetaFile().getMetaData().getExtraData().entrySet()){
 			extra += extraData.getKey() + ":" + control(extraData.getValue()) + "\n";
 		}
 		
 		fileUI.getFileData().getTextExtraData().setText(extra);		
 	}
 	
 	public void saveFileMetadata(String filePath){
		String name = reader.getMetaFile().getFileInfo().getFileName().replaceFirst("[.][^.]+$", "");
 		
		XMLSaver.save(filePath + "/" + name + ".xml", reader.getMetaFile());
 	}
 
 }
