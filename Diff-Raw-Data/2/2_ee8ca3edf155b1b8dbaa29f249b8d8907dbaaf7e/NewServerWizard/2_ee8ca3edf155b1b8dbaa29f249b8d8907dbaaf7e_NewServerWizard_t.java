 /**
  * Title:        JSqlIde<p>
  * Description:  A Java SQL Integrated Development Environment
  * <p>
  * Copyright:    Copyright (c) David Martinez<p>
  * Company:      <p>
  * @author David Martinez
  * @version 1.0
  */
 package com.hackerdude.apps.sqlide.wizards;
 
 import com.hackerdude.lib.ui.*;
 import java.util.HashMap;
 import com.hackerdude.apps.sqlide.*;
 import com.hackerdude.apps.sqlide.dataaccess.*;
 import java.awt.Dimension;
 import java.io.File;
 import javax.swing.JFrame;
 
 
 /**
  * This is a server Wizard used to create new servers.
  */
 public class NewServerWizard extends Wizard {
 
 	NewServerWizSelectServerType pageNewServer;
 	ServerDetailsWizardPage      pageServerDetails;
 	SelectClassPathWizardPage    pageSelectClassPath;
 
 	ConnectionConfig databaseSpec;
 
 	public NewServerWizard(JFrame owner, boolean modal) {
 		super(owner, "New Server Profile",
 			  "This wizard will guide you step by step on how to add a server profile "
 	 +"to your configuration.", modal );
 		databaseSpec = ConnectionConfigFactory.createConnectionConfig();
 		pageNewServer = new NewServerWizSelectServerType();
 		pageServerDetails = new ServerDetailsWizardPage();
 		pageSelectClassPath = new SelectClassPathWizardPage();
 		pageServerDetails.setWizard(this);
 		pageSelectClassPath.setWizard(this);
 		pageNewServer.setWizard(this);
 		pageNewServer.setDatabaseSpec(databaseSpec);
//		pageNewServer.cmbServerType.setSelectedIndex(0);
 		pageSelectClassPath.setDatabaseSpec(databaseSpec);
 		WizardPage[] pages = new WizardPage[3];
 		pages[0] = pageSelectClassPath;
 		pages[1] = pageNewServer;
 		pages[2] = pageServerDetails;
 		File defaultFile = new File(ConnectionConfig.DEFAULT_DBSPEC_FILENAME);
 		if ( ! defaultFile.exists() ) {
 			setFileName(ConnectionConfig.DEFAULT_DBSPEC_FILENAME);
 			pageNewServer.setFileNameEnabled(false);
 		}
 		setPages(pages);
 	}
 
 	public ConnectionConfig getDBSpec() { return databaseSpec; }
 
 	public void setFileName(String fileName) {
 		pageNewServer.fFileName.setText(fileName);
 	}
 
 	public void setServerType(String serverType) {
 		pageServerDetails.setServerType(serverType);
 	}
 
 	public void setJDBCURL(String URL) {
 		pageNewServer.setURL(URL);
 	}
 
 	public void setClassName(String className) {
 		pageNewServer.setClassName(className);
 	}
 
 	public void setProperties(HashMap properties) {
 		pageServerDetails.setServerProperties(properties);
 	}
 
 	public void setServerTitle(String title) {
 		pageNewServer.setServerTitle(title);
 	}
 
 	public void doneWizard() {
 		/** @todo Resolve this correctly. fFileName is only a base filename. It no longer has a path or anything. */
 		String baseFileName = pageNewServer.fFileName.getText();
 		String fileName = ProgramConfig.getInstance().getUserProfilePath()+baseFileName+".db.xml";
 		databaseSpec.setFileName(fileName);
 		databaseSpec.setJDBCURL(pageNewServer.fURL.getText());
 		databaseSpec.setPoliteName(pageNewServer.cmbServerType.getSelectedItem().toString()+" on "+pageNewServer.fHostName.getText());
 		databaseSpec.setDriverClassName(pageNewServer.fClassName.getText());
 		databaseSpec.setConnectionProperties(pageServerDetails.propertiesModel.getProperties());
 		databaseSpec.setDefaultCatalog(pageNewServer.fCatalogName.getText());
 		setVisible(false);
 	}
 
 	public String getFileName() {
 		return pageNewServer.fFileName.getText();
 	}
 
 	public static NewServerWizard showWizard(boolean modal) {
 		NewServerWizard wiz = new NewServerWizard(SqlIdeApplication.getFrame(), modal);
 		wiz.setEnabled(true);
 		wiz.pack();
 		Dimension screen = wiz.getToolkit().getScreenSize();
 		wiz.setLocation( ( screen.getSize().width - wiz.getSize().width) / 2,(screen.getSize().height - wiz.getSize().height) / 2);
 		wiz.show();
 		return wiz;
 	}
 
 	public static void main(String[] args) {
 		showWizard(true);
 	}
 
 }
