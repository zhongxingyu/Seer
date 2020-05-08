 /*
  * Copyright (C) 2010, 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Properties;
 
 import org.glom.libglom.BakeryDocument.LoadFailureCodes;
 import org.glom.libglom.Document;
 import org.glom.libglom.Glom;
 import org.glom.web.client.OnlineGlomService;
 import org.glom.web.shared.DetailsLayoutAndData;
 import org.glom.web.shared.Documents;
 import org.glom.web.shared.GlomDocument;
 import org.glom.web.shared.GlomField;
 import org.glom.web.shared.layout.LayoutGroup;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 import com.mchange.v2.c3p0.DataSources;
 
 /**
  * The servlet class for setting up the server side of Online Glom. The public methods in this class are the methods
  * that can be called by the client side code.
  * 
  * @author Ben Konrath <ben@bagu.org>
  */
 @SuppressWarnings("serial")
 public class OnlineGlomServiceImpl extends RemoteServiceServlet implements OnlineGlomService {
 
 	// convenience class to for dealing with the Online Glom configuration file
 	private class OnlineGlomProperties extends Properties {
 		public String getKey(String value) {
 			for (String key : stringPropertyNames()) {
 				if (getProperty(key).trim().equals(value))
 					return key;
 			}
 			return null;
 		}
 	}
 
 	private final Hashtable<String, ConfiguredDocument> documentMapping = new Hashtable<String, ConfiguredDocument>();
 
 	/*
 	 * This is called when the servlet is started or restarted.
 	 */
 	public OnlineGlomServiceImpl() throws Exception {
 
 		// Find the configuration file. See this thread for background info:
 		// http://stackoverflow.com/questions/2161054/where-to-place-properties-files-in-a-jsp-servlet-web-application
 		OnlineGlomProperties config = new OnlineGlomProperties();
 		InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("onlineglom.properties");
 		if (is == null) {
 			Log.fatal("onlineglom.properties not found.");
 			throw new IOException();
 		}
 		config.load(is);
 
 		// check if we can read the configured glom file directory
 		String documentDirName = config.getProperty("glom.document.directory");
 		File documentDir = new File(documentDirName);
 		if (!documentDir.isDirectory()) {
 			Log.fatal(documentDirName + " is not a directory.");
 			throw new IOException();
 		}
 		if (!documentDir.canRead()) {
 			Log.fatal("Can't read the files in : " + documentDirName);
 			throw new IOException();
 		}
 
 		// get and check the glom files in the specified directory
		final String glomFileExtension = ".glom";
 		File[] glomFiles = documentDir.listFiles(new FilenameFilter() {
 			@Override
 			public boolean accept(File dir, String name) {
				return name.endsWith(glomFileExtension);
 			}
 		});
 		Glom.libglom_init();
 		for (File glomFile : glomFiles) {
 			Document document = new Document();
 			document.set_file_uri("file://" + glomFile.getAbsolutePath());
 			int error = 0;
 			boolean retval = document.load(error);
 			if (retval == false) {
 				String message;
 				if (LoadFailureCodes.LOAD_FAILURE_CODE_NOT_FOUND == LoadFailureCodes.swigToEnum(error)) {
 					message = "Could not find file: " + glomFile.getAbsolutePath();
 				} else {
 					message = "An unknown error occurred when trying to load file: " + glomFile.getAbsolutePath();
 				}
 				Log.error(message);
 				// continue with for loop because there may be other documents in the directory
 				continue;
 			}
 
 			ConfiguredDocument configuredDocument = new ConfiguredDocument(document);
 			// check if a username and password have been set and work for the current document
 			String filename = glomFile.getName();
 			String key = config.getKey(filename);
 			if (key != null) {
 				String[] keyArray = key.split("\\.");
 				if (keyArray.length == 3 && "filename".equals(keyArray[2])) {
 					// username/password could be set, let's check to see if it works
 					String usernameKey = key.replaceAll(keyArray[2], "username");
 					String passwordKey = key.replaceAll(keyArray[2], "password");
 					configuredDocument.setUsernameAndPassword(config.getProperty(usernameKey),
 							config.getProperty(passwordKey));
 				}
 			}
 
 			// check the if the global username and password have been set and work with this document
 			if (!configuredDocument.isAuthenticated()) {
 				configuredDocument.setUsernameAndPassword(config.getProperty("glom.document.username"),
 						config.getProperty("glom.document.password"));
 			}
 
 			// The key for the hash table is the file name without the .glom extension and with spaces ( ) replaced with
 			// pluses (+). The space/plus replacement makes the key more friendly for URLs.
			String documentID = filename.substring(0, glomFile.getName().length() - glomFileExtension.length())
 					.replace(' ', '+');
 			configuredDocument.setDocumentID(documentID);
 			documentMapping.put(documentID, configuredDocument);

 		}
 	}
 
 	/*
 	 * This is called when the servlet is stopped or restarted.
 	 * 
 	 * @see javax.servlet.GenericServlet#destroy()
 	 */
 	@Override
 	public void destroy() {
 		Glom.libglom_deinit();
 
 		for (String documenTitle : documentMapping.keySet()) {
 			ConfiguredDocument configuredDoc = documentMapping.get(documenTitle);
 			try {
 				DataSources.destroy(configuredDoc.getCpds());
 			} catch (SQLException e) {
 				Log.error(documenTitle, "Error cleaning up the ComboPooledDataSource.", e);
 			}
 		}
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getGlomDocument(java.lang.String)
 	 */
 	@Override
 	public GlomDocument getGlomDocument(String documentID) {
 
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getGlomDocument();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getListViewLayout(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public LayoutGroup getListViewLayout(String documentID, String tableName) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getListViewLayoutGroup(tableName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getListViewData(java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public ArrayList<GlomField[]> getListViewData(String documentID, String tableName, int start, int length) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (!configuredDoc.isAuthenticated()) {
 			return new ArrayList<GlomField[]>();
 		}
 		return configuredDoc.getListViewData(tableName, start, length, false, 0, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getSortedListViewData(java.lang.String, java.lang.String, int, int,
 	 * int, boolean)
 	 */
 	@Override
 	public ArrayList<GlomField[]> getSortedListViewData(String documentID, String tableName, int start, int length,
 			int sortColumnIndex, boolean isAscending) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		if (!configuredDoc.isAuthenticated()) {
 			return new ArrayList<GlomField[]>();
 		}
 		return configuredDoc.getListViewData(tableName, start, length, true, sortColumnIndex, isAscending);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDocuments()
 	 */
 	@Override
 	public Documents getDocuments() {
 		Documents documents = new Documents();
 		for (String documentID : documentMapping.keySet()) {
 			ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 			documents.addDocument(documentID, configuredDoc.getDocument().get_database_title());
 		}
 		return documents;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#isAuthenticated(java.lang.String)
 	 */
 	public boolean isAuthenticated(String documentID) {
 		return documentMapping.get(documentID).isAuthenticated();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#checkAuthentication(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public boolean checkAuthentication(String documentID, String username, String password) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		try {
 			return configuredDoc.setUsernameAndPassword(username, password);
 		} catch (SQLException e) {
 			Log.error(documentID, "Unknown SQL Error checking the database authentication.", e);
 			return false;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDetailsData(java.lang.String, java.lang.String, java.lang.String)
 	 */
 	@Override
 	public GlomField[] getDetailsData(String documentID, String tableName, String primaryKeyValue) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getDetailsData(tableName, primaryKeyValue);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getDetailsLayoutAndData(java.lang.String, java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public DetailsLayoutAndData getDetailsLayoutAndData(String documentID, String tableName, String primaryKeyValue) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 		DetailsLayoutAndData initalDetailsView = new DetailsLayoutAndData();
 
 		// FIXME check for authentication
 
 		initalDetailsView.setLayout(configuredDoc.getDetailsLayoutGroup(tableName));
 		initalDetailsView.setData(configuredDoc.getDetailsData(tableName, primaryKeyValue));
 
 		return initalDetailsView;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getRelatedListData(java.lang.String, java.lang.String, int, int)
 	 */
 	@Override
 	public ArrayList<GlomField[]> getRelatedListData(String documentID, String tableName, String relationshipName,
 			String foreignKeyValue, int start, int length) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListData(tableName, relationshipName, foreignKeyValue, start, length, false, 0,
 				false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.glom.web.client.OnlineGlomService#getSortedRelatedListData(java.lang.String, java.lang.String, int, int,
 	 * int, boolean)
 	 */
 	@Override
 	public ArrayList<GlomField[]> getSortedRelatedListData(String documentID, String tableName,
			String relationshipName, String foreignKeyValue, int start, int length, int sortColumnIndex, boolean ascending) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListData(tableName, relationshipName, foreignKeyValue, start, length, true,
 				sortColumnIndex, ascending);
 	}
 
 	public int getRelatedListRowCount(String documentID, String tableName, String relationshipName,
 			String foreignKeyValue) {
 		ConfiguredDocument configuredDoc = documentMapping.get(documentID);
 
 		// FIXME check for authentication
 
 		return configuredDoc.getRelatedListRowCount(tableName, relationshipName, foreignKeyValue);
 	}
 
 }
