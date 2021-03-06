 package com.its.easyjavadropbox.api.impl;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import com.dropbox.core.DbxClient;
 import com.dropbox.core.DbxEntry;
 import com.dropbox.core.DbxEntry.WithChildren;
 import com.dropbox.core.DbxException;
 import com.dropbox.core.DbxRequestConfig;
 import com.dropbox.core.DbxThumbnailFormat;
 import com.dropbox.core.DbxThumbnailSize;
 import com.its.easyjavadropbox.api.interfaces.EasyJavaDropBoxService;
 
 public class EasyJavaDropBoxServiceImpl implements EasyJavaDropBoxService{
 	private String root = "/";
 	private String dropboxPath = root;
 	private DbxRequestConfig config;
 	private DbxEntry.WithChildren listing = null;
 	private DbxClient client;
 	
 	/**
 	 * 
 	 * @param dropboxPath inform path to be considered. You must have permission to access
 	 * @param token generated by dropbox 
 	 */
 	public EasyJavaDropBoxServiceImpl(String dropboxPath,String token) {
 		this.dropboxPath = dropboxPath;
 		setup(token);
 	}
 	/**
 	 * Path default here is "/"
 	 * @param token token generated by dropbox
 	 */
 	public EasyJavaDropBoxServiceImpl(String token) {
 		setup(token);
 	}
 	private void setup(String token) {
 		config = new DbxRequestConfig("ITS/1.0",Locale.getDefault().toString());
 		client = new DbxClient(config, token);
 	}
 	
 	@Override
 	public void changeDropboxPath(String newpath) {
 		dropboxPath = newpath;
 		
 	}
 
 	public WithChildren getListing() throws DbxException{
 			 listing = client.getMetadataWithChildren(dropboxPath);
 		return listing;
 	}
 
 	public  List<DbxEntry> searchFile(String basePath, String query) throws DbxException{
 		List<DbxEntry> listFileAndFolderNames = new ArrayList<DbxEntry>();
 			listFileAndFolderNames = client.searchFileAndFolderNames(basePath, query);
 		return listFileAndFolderNames; 
 	}
 
 	@Override
 	public List<DbxEntry> listFiles() throws DbxException {
 				DbxEntry.WithChildren listFiles = client.getMetadataWithChildren(dropboxPath);
 		return  listFiles.children;
 	}
 
 	public DbxClient getClient() {
 		return client;
 	}
 
 	@Override
 	public void writeThumbnail(String ident, int width, int height,String fileName, OutputStream outputStream) throws DbxException, IOException {
 		DbxThumbnailSize size = new DbxThumbnailSize(ident, width,height);
 		DbxThumbnailFormat format = DbxThumbnailFormat.bestForFileName(fileName, null);
 		client.getThumbnail(size, format, dropboxPath + fileName, null, outputStream);
 		
 	}
 

 }
