 /**
  * @(#)BaseSetupSiteProcess.
  * Copyright © 2012 jbundle.org. All rights reserved.
  * GPL3 Open Source Software License.
  */
 package org.jbundle.app.program.demo;
 
 import java.util.*;
 
 import org.jbundle.base.db.*;
 import org.jbundle.thin.base.util.*;
 import org.jbundle.thin.base.db.*;
 import org.jbundle.base.db.event.*;
 import org.jbundle.base.db.filter.*;
 import org.jbundle.base.field.*;
 import org.jbundle.base.field.convert.*;
 import org.jbundle.base.field.event.*;
 import org.jbundle.base.screen.model.*;
 import org.jbundle.base.screen.model.util.*;
 import org.jbundle.base.model.*;
 import org.jbundle.base.util.*;
 import org.jbundle.model.*;
 import org.jbundle.model.db.*;
 import org.jbundle.model.screen.*;
 import org.jbundle.base.message.remote.*;
 import org.jbundle.main.db.*;
 import org.jbundle.main.user.db.*;
 import org.jbundle.thin.base.db.buff.*;
 import org.jbundle.thin.base.thread.*;
 import java.net.*;
 import java.io.*;
 import org.jbundle.app.program.script.scan.*;
 import org.jbundle.app.program.manual.convert.*;
 import java.text.*;
 import org.jbundle.app.program.demo.message.*;
 import org.jbundle.thin.base.message.*;
 import org.jbundle.base.thread.*;
 import org.osgi.framework.*;
 import org.jbundle.util.osgi.webstart.*;
 import org.jbundle.util.osgi.finder.*;
 
 /**
  *  BaseSetupSiteProcess - .
  */
 public class BaseSetupSiteProcess extends BaseMessageProcess
 {
     /**
      * Default constructor.
      */
     public BaseSetupSiteProcess()
     {
         super();
     }
     /**
      * Constructor.
      */
     public BaseSetupSiteProcess(RecordOwnerParent taskParent, Record recordMain, Map<String,Object> properties)
     {
         this();
         this.init(taskParent, recordMain, properties);
     }
     /**
      * Initialize class fields.
      */
     public void init(RecordOwnerParent taskParent, Record recordMain, Map<String, Object> properties)
     {
         super.init(taskParent, recordMain, properties);
     }
     /**
      * Open the main file.
      */
     public Record openMainRecord()
     {
         return new UserInfo(this);
     }
     /**
      * Open the other files.
      */
     public void openOtherRecords()
     {
         super.openOtherRecords();
         new Menus(this);
     }
     /**
      * Process this message and return a reply.
      */
     public BaseMessage processMessage(BaseMessage message)
     {
         RunRemoteProcessMessageData runRemoteProcessMessageData = (RunRemoteProcessMessageData)message.getMessageDataDesc(null);
         if (runRemoteProcessMessageData == null)
             message.addMessageDataDesc(runRemoteProcessMessageData = new RunRemoteProcessMessageData(null, null));
         CreateSiteMessageData siteMessageData = (CreateSiteMessageData)runRemoteProcessMessageData.getMessageDataDesc(CreateSiteMessageData.CREATE_SITE);
         if (siteMessageData == null)
             runRemoteProcessMessageData.addMessageDataDesc(siteMessageData = new CreateSiteMessageData(runRemoteProcessMessageData, null));
         StandardMessageResponseData reply = this.setupUserInfo(siteMessageData);
         if (reply == null)
             return null;
         return reply.getMessage();
     }
     /**
      * SetupUserInfo Method.
      */
     public StandardMessageResponseData setupUserInfo(CreateSiteMessageData siteMessageData)
     {
         MessageRecordDesc userInfoMessageData = (MessageRecordDesc)siteMessageData.getMessageDataDesc(UserInfo.USER_INFO_FILE);
         MessageRecordDesc menusMessageData = (MessageRecordDesc)siteMessageData.getMessageDataDesc(Menus.MENUS_FILE);
         
         TreeMessage replyMessage = new TreeMessage(null, null);
         StandardMessageResponseData runRemoteProcessResponse = new StandardMessageResponseData(replyMessage, null);
         try {
             Record recUser = this.getMainRecord();
             recUser.addNew();
             recUser.setKeyArea(UserInfo.USER_NAME_KEY);
             recUser.getField(UserInfo.USER_NAME).setString((String)userInfoMessageData.get(UserInfo.USER_NAME));
             if (recUser.seek(null))
                 recUser.edit();
             else
                 recUser.addNew(); // If user doesn't exist, create it
             userInfoMessageData.getRawRecordData(recUser);  // User username, password
             ((PropertiesField)recUser.getField(UserInfo.PROPERTIES)).setProperty(DBParams.HOME, (String)menusMessageData.get(MenusMessageData.SITE_HOME_MENU));
             ((PropertiesField)recUser.getField(UserInfo.PROPERTIES)).setProperty(MenusMessageData.DOMAIN_NAME, (String)menusMessageData.get(MenusMessageData.DOMAIN_NAME));
             if (recUser.getEditMode() == DBConstants.EDIT_ADD)
                 recUser.add();
             else
                 recUser.set();
             
             Record recMenus = this.getRecord(Menus.MENUS_FILE);
         
             // First read the template menu record
             String siteTemplate = (String)menusMessageData.get(MenusMessageData.SITE_TEMPLATE_MENU);
             recMenus.getField(Menus.CODE).setString(siteTemplate);
             int iOldOrder = recMenus.getDefaultOrder();
             recMenus.setKeyArea(Menus.CODE_KEY);
             BaseBuffer buffer = null;
             if (recMenus.seek(null))
             {
                 buffer = new VectorBuffer(null);
                 buffer.fieldsToBuffer(recMenus);
             }
             else
             {
                 runRemoteProcessResponse.setMessage("Error: Site template not found: " + siteTemplate);
                 return runRemoteProcessResponse;
             }
         
             // Next, create the new menu for this domain (using template record info)
             recMenus.addNew();
             recMenus.setKeyArea(Menus.CODE_KEY);
             recMenus.getField(Menus.CODE).setString((String)menusMessageData.get(MenusMessageData.DOMAIN_NAME));
             if (recMenus.seek(null))
                 recMenus.edit();    // If it already exists, I must be on the same machine that sent me this message!
             else
             {
                 recMenus.addNew();  // If it doesn't exist, create it
                 if (buffer != null)     // Always
                     buffer.bufferToFields(recMenus, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
             }
             
             recMenus.getField(Menus.SEQUENCE).setValue(100);
             
             // Create customized xslt stylesheet
             String homeDir = System.getProperty("user.home") + File.separator + ".jbundle";
             recMenus.getField(Menus.CODE).setString((String)menusMessageData.get(MenusMessageData.DOMAIN_NAME));
         
             String fullSitePrefix = (String)menusMessageData.get(MenusMessageData.SITE_PREFIX);
             String siteName = (String)menusMessageData.get(MenusMessageData.SITE_NAME);
             String templateArchivePath = (String)menusMessageData.get(MenusMessageData.XSL_TEMPLATE_PATH);
             String siteHomeCode = (String)menusMessageData.get(MenusMessageData.SITE_HOME_MENU);
             recMenus.getField(Menus.NAME).setString(siteName);
         
             String destArchivePath  = templateArchivePath;
             if (destArchivePath.lastIndexOf(File.separator) != -1)  // Always
                 destArchivePath = destArchivePath.substring(destArchivePath.lastIndexOf(File.separator) + 1); // Dest archive dir
             destArchivePath  = Utility.addToPath(homeDir, fullSitePrefix + File.separator + destArchivePath);
         
             ((PropertiesField)recMenus.getField(Menus.PARAMS)).setProperty(DBConstants.USER_ARCHIVE_FOLDER, destArchivePath);
             ((PropertiesField)recMenus.getField(Menus.PARAMS)).setProperty(DBConstants.DB_USER_PREFIX, fullSitePrefix + "_");
         
             String templateFilename = ((PropertiesField)recMenus.getField(Menus.PARAMS)).getProperty(MenusMessageData.XSL_TEMPLATE_PATH);
             if ((templateFilename == null) || (templateFilename.length() == 0))
                 templateFilename = this.getProperty(MenusMessageData.XSL_TEMPLATE_PATH);
             if ((templateFilename == null) || (templateFilename.length() == 0))
                 templateFilename = "docs/styles/xsl/program/fixdemotemplate.xsl";
             ((PropertiesField)recMenus.getField(Menus.PARAMS)).setProperty(MenusMessageData.XSL_TEMPLATE_PATH, templateFilename);
         
             if (recMenus.getEditMode() == DBConstants.EDIT_ADD)
                 recMenus.add();
             else
                 recMenus.set();
         
             this.createCustomArchive(destArchivePath, homeDir, templateArchivePath, templateFilename);
         
             // Make sure the user's account has been added to their new database
             BaseApplication app = (BaseApplication)this.getTask().getApplication();
         
             Map<String,Object> properties = new Hashtable<String,Object>();
             if (app.getProperties() != null)
                 properties.putAll(app.getProperties());
             properties.put(DBConstants.DB_USER_PREFIX, fullSitePrefix + "_");
             properties.put(DBConstants.LOAD_INITIAL_DATA, DBConstants.TRUE);
             properties.put(DBConstants.USER_ARCHIVE_FOLDER, destArchivePath);
             BaseApplication appTemp = new BaseApplication(app.getEnvironment(), properties, null);
             Task task = new AutoTask(appTemp, null, null);
             BaseProcess recordOwner = new BaseProcess(task, null, null);
             UserInfo recUserNew = new UserInfo(recordOwner);
             // Also add this user as an admin
             recUserNew.addNew();
             recUserNew.moveFields(recUser, null, DBConstants.DISPLAY, DBConstants.SCREEN_MOVE, false, false, false, false);
             if (properties.get(BaseRegistrationScreen.ADMIN_HOME_MENU_CODE) != null)
                 ((PropertiesField)recUserNew.getField(UserInfo.PROPERTIES)).setProperty(DBParams.HOME, properties.get(BaseRegistrationScreen.ADMIN_HOME_MENU_CODE).toString());
             UserGroup recUserGroup = new UserGroup(this);
             recUserGroup.setKeyArea(UserGroup.DESCRIPTION_KEY);
             recUserGroup.getField(UserGroup.DESCRIPTION).setString("Admin");
             if (recUserGroup.seek(">="))
                 recUserNew.getField(UserInfo.USER_GROUP_ID).moveFieldToThis(recUserGroup.getCounterField());
             recUserGroup.free();
             recUserNew.add();
             
             // Set all the new users with the same password
             recUserNew.addNew();
             boolean[] rgbEnabled = recUserNew.setEnableListeners(false);
             Object[] rgbfldEnabled = recUserNew.setEnableFieldListeners(false);
             recUserNew.close();
             while (recUserNew.hasNext())
             {
                 recUserNew.next();
                 if (recUserNew.getField(UserInfo.ID).getValue() == 1)
                     continue; // Anonymous user
                 recUserNew.edit();
                 recUserNew.getField(UserInfo.PASSWORD).moveFieldToThis(recUser.getField(UserInfo.PASSWORD), DBConstants.DISPLAY, DBConstants.SCREEN_MOVE);
                 recUserNew.set();
             }
             recUserNew.setEnableListeners(rgbEnabled);
             recUserNew.setEnableFieldListeners(rgbfldEnabled);
             
             String strFreeIfDone = this.getEnvironment().getProperty(DBParams.FREEIFDONE);
             this.getEnvironment().setProperty(DBParams.FREEIFDONE, DBConstants.FALSE);
             appTemp.free();
             this.getEnvironment().setProperty(DBParams.FREEIFDONE, strFreeIfDone);
            
             recMenus.setKeyArea(iOldOrder);
             
             runRemoteProcessResponse.setMessage("Okay: " + fullSitePrefix);
             return runRemoteProcessResponse;
         } catch (DBException ex) {
             ex.printStackTrace();
             runRemoteProcessResponse.setMessage("Error: " + ex.getMessage());
             return runRemoteProcessResponse;
         }
     }
     /**
      * CreateCustomArchive Method.
      */
     public void createCustomArchive(String destArchivePath, String homePath, String templateArchivePath, String templateFilename)
     {
         Record recUser = this.getMainRecord();
         Map<String,String> map = new HashMap<String,String>();
         map.put("${email}", recUser.getField(UserInfo.USER_NAME).toString());
         Date date = new Date();   // Today
         DateField dateField = new DateField(null, null, -1, null, null);
         dateField.setDate(date, DBConstants.DISPLAY, DBConstants.INIT_MOVE);
         map.put("${today}", dateField.toString());
         dateField.free();
         
         URL url = this.getTask().getApplication().getResourceURL(templateFilename, null);
         StringBuilder sb = new StringBuilder(Utility.transferURLStream(url.toString(), null));
         sb = Utility.replace(sb, map);
         String templateFile = Utility.addToPath(destArchivePath, "fixdemo.xsl");
         PrintWriter out = null;
         try {
             File file = new File(templateFile);
             file.getParentFile().getParentFile().mkdirs();  // .tourapp
             file.getParentFile().mkdirs();  // Folder
             file.createNewFile();
             out = new PrintWriter(file);
         } catch (FileNotFoundException e) {} catch (IOException e) {
             e.printStackTrace();
         }
         if (out != null)
         {
             out.print(sb);
             out.close();
             
             // First, make sure the base demo files exist in the file system
             String workDir = templateArchivePath;
             if (workDir.lastIndexOf(File.separator) != -1)  // Always
                 workDir = workDir.substring(workDir.lastIndexOf(File.separator) + 1);
             String workDirPath = Utility.addToPath(homePath, workDir);
             this.populateSourceDir(templateArchivePath, workDirPath);
             
             // Run the xslt against the base demo files.
             Map<String, Object> properties = new HashMap<String, Object>();
             properties.put("sourceDir", workDirPath);
             properties.put("destDir", destArchivePath);
             properties.put("extension", "xml");
             properties.put("filter", ".*");   // Hack (filter is a bad name since it it used in many places)
             properties.put("listenerClass", XMLScanListener.class.getName());
             properties.put("converterPath", templateFile);
             BaseProcess process = new ConvertCode(this.getTask(), null, properties);
             process.run();
             process.free();
         }
     }
     /**
      * PopulateSourceDir Method.
      */
     public boolean populateSourceDir(String templateDir, String srcDir)
     {
         URL fromDirUrl = this.getTask().getApplication().getResourceURL(templateDir, null);
         if ("http".equalsIgnoreCase(fromDirUrl.getProtocol()))
         {
            String packageName = templateDir + "/main_user/org/jbundle/main/user/db";
             packageName = packageName.replace('/', '.');
             Bundle bundle = (Bundle)ClassServiceUtility.getClassService().getClassFinder(null).findBundle(null, null, packageName, null);
             if (bundle == null) {
                 Object resource = ClassServiceUtility.getClassService().getClassFinder(null).deployThisResource(packageName, null, true);
                 if (resource != null)
                     bundle = (Bundle)ClassServiceUtility.getClassService().getClassFinder(null).findBundle(resource, null, packageName, null);
             }
         
             if (bundle == null)
                 return false;   // Couldn't file files
             OsgiWebStartServlet.transferBundleFiles(bundle, templateDir, srcDir);
         }
         else if (!"file".equalsIgnoreCase(fromDirUrl.getProtocol()))
             return false;
         String fromDir = fromDirUrl.getFile();
         File fromDirFile = new File(fromDir);
         File srcDirFile = new File(srcDir);
         Map<String, Object> properties = new HashMap<String, Object>();
         properties.put("extension", "xml");
         properties.put("filter", ".*");   // Hack (filter is a bad name since it it used in many places)
         if (srcDirFile.exists())
         {
             long srcModified = srcDirFile.lastModified();
             long fromModified = fromDirFile.lastModified();
             if (srcModified > fromModified)
                 return true;    // It's already set up and current!
             // Delete the directory so I can replace it with new data
             properties.put("sourceDir", srcDir);
             properties.put("destDir", srcDir);
             properties.put("listenerClass", DeleteScanListener.class.getName());
             properties.put("deleteDir", DBConstants.TRUE);
             BaseProcess process = new ConvertCode(this.getTask(), null, properties);
             process.run();
             process.free();
         }
         properties.put("sourceDir", fromDir);
         properties.put("destDir", srcDir);
         properties.put("listenerClass", BaseScanListener.class.getName());
         BaseProcess process = new ConvertCode(this.getTask(), null, properties);
         process.run();
         process.free();
         return true;
     }
 
 }
