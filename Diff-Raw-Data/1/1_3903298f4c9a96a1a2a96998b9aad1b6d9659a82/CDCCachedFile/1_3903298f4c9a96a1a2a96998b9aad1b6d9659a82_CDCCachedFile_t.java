 package edu.pdx.svl.coDoc.cdc.datacenter;
 
 import edu.pdx.svl.coDoc.cdc.XML.SimpleXML;
 
 public class CDCCachedFile {
 	private String fileName;
 	private CDCModel cdcModel;
 	
 	public CDCCachedFile() {
 		fileName = null;
 		cdcModel = null;
 	}
 	
 	public void open(String filename) {
 		fileName = filename;
 		if(fileName != null) {
 			cdcModel = SimpleXML.readCDCModel(fileName);
 		}
 	}
 	public void close() {
 	}
 	
 	public CDCModel getCDCModel() {
 		return cdcModel;
 	}
 	
 	public void refresh() {
 		if(fileName!=null) {
 			cdcModel = SimpleXML.readCDCModel(fileName);
 		}
 	}
 	public void flush() {
 		if(fileName!=null && cdcModel!=null) {
 			SimpleXML.writeCDCModel(cdcModel, fileName);
 		}
 	}
 	
 	public void addCodeFileEntry(String filename) {
 		cdcModel.addCodeFileEntry(filename);
 		flush();
 	}
 	public String getCodeFilename(String uuid) {
 		return cdcModel.getCodeFilename(uuid);
 	}
 	public void deleteCodeFileEntry(String uuid) {
 		cdcModel.deleteCodeFileEntry(uuid);
 		flush();
 	}
 	
 	public void addSpecFileEntry(String filename) {
 		cdcModel.addSpecFileEntry(filename);
 		flush();
 	}
 	public String getSpecFilename(String uuid) {
 		return cdcModel.getSpecFilename(uuid);
 	}
 	public void deleteSpecFileEntry(String uuid) {
 		cdcModel.deleteSpecFileEntry(uuid);
 		flush();
 	}
 	
 	public FolderMapTreeNode getMapIdTree() {
 		return cdcModel.getMapIdTree();
 	}
 	
 	public String addFolderEntry(String parentfolderuuid, String foldername) {
 		String uuid = cdcModel.addFolderEntry(parentfolderuuid, foldername);
 		flush();
 		return uuid;
 	}
 	public FolderEntry getFolderEntry(String uuid) {
 		return cdcModel.getFolderEntry(uuid);
 	}
 	public void deleteFolderEntry(String uuid) {
 		cdcModel.deleteFolderEntry(uuid);
 		flush();
 	}
 	
 	public String addMapEntry(String parentfolderuuid, String codefilename, CodeSelection codeselpath, String specfilename, SpecSelection specselpath, String comment) {
 		String uuid = cdcModel.addMapEntry(parentfolderuuid, codefilename, codeselpath, specfilename, specselpath, comment);
 		flush();
 		return uuid;
 	}
 	public String editMapEntry(String uuid, String codefilename, CodeSelection codeselpath, String specfilename, SpecSelection specselpath, String comment) {
 		String newuuid = cdcModel.editMapEntry(uuid, codefilename, codeselpath, specfilename, specselpath, comment);
 		flush();
 		return newuuid;
 	}
 	public MapEntry getMapEntry(String uuid) {
 		return cdcModel.getMapEntry(uuid);
 	}
 	public void deleteMapEntry(String uuid) {
 		cdcModel.deleteMapEntry(uuid);
 		flush();
 	}
 	
 	public boolean parentOf(String childuuid, String parentuuid) {
 		return cdcModel.parentOf(childuuid, parentuuid);
 	}
 	public void moveEntry(String sourceuuid, String destuuid) {
 		cdcModel.moveEntry(sourceuuid, destuuid);
		flush();
 	}
 	
 	public void setLastOpenedCodeFilename(String codeFilename) {
 		cdcModel.setLastOpenedCodeFilename(codeFilename);
 		flush();
 	}
 	public String getLastOpenedCodeFilename() {
 		return cdcModel.getLastOpenedCodeFilename();
 	}
 	
 	public void setLastOpenedSpecFilename(String specFilename) {
 		cdcModel.setLastOpenedSpecFilename(specFilename);
 		flush();
 	}
 	public String getLastOpenedSpecFilename() {
 		return cdcModel.getLastOpenedSpecFilename();
 	}
 }
