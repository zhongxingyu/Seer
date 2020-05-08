 /*
  * Created on Aug 25, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.va.med.iss.meditor.utils;
 
 import gov.va.med.foundations.adapter.cci.VistaLinkConnection;
 import gov.va.med.foundations.rpc.RpcRequest;
 import gov.va.med.foundations.rpc.RpcRequestFactory;
 import gov.va.med.foundations.rpc.RpcResponse;
 import gov.va.med.iss.connection.actions.VistaConnection;
 import gov.va.med.iss.connection.utilities.MPiece;
 import gov.va.med.iss.meditor.MEditorPlugin;
 import gov.va.med.iss.meditor.editors.MEditor;
 import gov.va.med.iss.meditor.preferences.MEditorPreferencesPage;
 import gov.va.med.iss.meditor.preferences.MEditorPrefs;
 import gov.va.med.iss.meditor.preferences.MEditorPropertyPage1;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.IDE;
 import org.eclipse.ui.part.FileEditorInput;
 
 /**
  * @author vhaisfiveyj
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class RoutineLoad {
 	
 	private static VistaLinkConnection myConnection = null;
 	private static boolean isReadOnly = false;
 	private static String currentServer = "";
 	public static boolean newPage = false;
 	
 	public void loadRoutine(String routineName, boolean updateBackup, boolean setReadOnly) {
 		isReadOnly = setReadOnly;
 		loadRoutine(routineName, updateBackup);
 	}
 
 	public void loadRoutine(String routineName, boolean updateBackup) {
 		IResource resource = null;
 		if (! MEditorPrefs.isPrefsActive()) {
 			try {
 //				resource = MEditorUtilities.getProject(MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME)); //"mcode");
 				resource = MEditorUtilities.getProject(MEditorPreferencesPage.getProjectName()); //"mcode");
 			} catch (Exception e) {
 			}
 		}
 //		int value = 0;
 // moved to RoutineEditAction 091029 //		VistaConnection.getPrimaryServer(); //091027 to make check for change in servers
 		if ( ! (routineName.compareTo("") == 0)) {
 			try {
 				String sourceCode = getRoutineFromServer(routineName);
 				if (! (sourceCode.compareTo("") == 0)) {
 					if (sourceCode.indexOf("\r\n") == -1) {
 						sourceCode = sourceCode.replaceAll("\n","\r\n");
 					}
 					String doc = "";
 					// check for 
 					boolean isNew = false;
 					if (sourceCode.indexOf("-1") == 0) {  // routine not found
 						// added to handle third ^-piece as indicator for PRODUCTION system
 						doc = codeForNewRoutine(routineName);
 						if (doc.compareTo("") == 0)
 							return;
 						isNew = true;
 					}
 					else {
 						int value2 = sourceCode.indexOf("\n");
 						// remove first line indicating success or failure;
 						// JLI 101104 value2 = sourceCode.indexOf("\n");
 						doc = sourceCode.substring(value2+1);
 					}
 					saveCode(routineName, doc, updateBackup, isNew);
 					try {
 						MEditorMessageConsole.writeToConsole("");
 					} catch (Exception e) {
 						MessageDialog.openWarning(
 								MEditorUtilities.getIWorkbenchWindow().getShell(),
 								"Meditor Plug-in Routine Save",
 								"Unable to send message to Console: "+e.getLocalizedMessage());
 					}
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 					MessageDialog.openInformation(
 							getWindow().getShell(),
 							"Meditor Plug-in",
 							"Error encountered while loading routine: "+e.getMessage());
 			}
 			
 		}
 		isReadOnly = false;
 	}
 	
 	private static void saveCode(String routineName, String sourceCode, boolean updateBackup, boolean isNew)
 																		throws Exception {
 		SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
 		Date date = new Date();
 		String location = getFullFileLocation(routineName);
 		boolean isOK = true;
 		// if first time loaded, save a copy as prior day.
 		if (! (new File(location).exists())) {
 			new File(location).mkdirs();
 		}
 		//String fileLocation = location+"/"+routineName + ".m";
 		String fileLocation = getLastLoadFileName(routineName,location);
 		File file = new File(fileLocation);
 		FileWriter fw;
 		if (updateBackup) {
 			if (! (file.exists())) {
 				saveOldCopy(routineName, sourceCode, "yyMMdd",location);
 			}
 			else {
 				String oldSourceCode = MEditorUtilities.fileToString(fileLocation);
 				isOK = RoutineSave.checkForServerChange(routineName, oldSourceCode, false,false);
 				if (isOK) {
 					try {
 					file.delete();
 					} catch (Exception e) {
 						throw new Exception(e.getMessage()+" Error 002");
 					}
 					String dtString = dateFormat.format(date);
                     String backupDir = getBackupDir(routineName,location);
 					String fileName = backupDir+"/"+routineName+" "+dtString+".m";
 					File file1 = new File(fileName);
 					if (file1.exists()) {
 						file1.delete();
 					}
 					try {
 					fw = new FileWriter(fileName);
 					fw.write(sourceCode);
 					fw.flush();
 					fw.close();
 					} catch (Exception e) {
 						throw new Exception(e.getMessage()+" Error 003");
 					}
 				}
 			}
 		}
 		if (isOK) {
 			String filename = getBackupFileName(routineName, location);
 			File filelast = new File(filename);
 			if (filelast.exists())
 				filelast.delete();
 			// save working copy
 			FileWriter fw1 = new FileWriter(location+"/"+routineName+".m");
 			fw1.write(sourceCode);
 			fw1.flush();
 			fw1.close();
 			// save last image for later comparisons
 			FileWriter fw2 = new FileWriter(filename);
 			if (isNew)
 				fw2.write("");
 			else
 				fw2.write(sourceCode);
 			fw2.flush();
 			fw2.close();
 			if ((! RoutineSave.isCopy()) || 
 					(currentServer.compareTo(VistaConnection.getPrimaryServerID()) == 0)) {
 				doRefresh(routineName+".m", updateBackup);
 				if (isReadOnly) {
 					file.setReadOnly();
 				}
 			}
 		}
 	}
 	
 	public static String saveOldCopy(String routineName, 
 			String sourceCode, String dateString, 
 			String location) throws Exception {
 		SimpleDateFormat dateFormat = new SimpleDateFormat(dateString);
 		Date date = new Date();
 		String fileName = "";
 		try {
 			GregorianCalendar gc = new GregorianCalendar();
 			gc.setTime(date);
 			if (! dateString.contains("hhmm")) {
 				gc.add(Calendar.DATE,-1);
 			}
 			String dtString1 = dateFormat.format(gc.getTime());
             String backupDir = getBackupDir(routineName,location);
 			fileName = backupDir+"/"+routineName+" "+dtString1+".m";
 			File file1 = new File(fileName);
 			if (! file1.exists()) {
 				FileWriter fw;
 				fw = new FileWriter(fileName);
 				fw.write(sourceCode);
 				fw.flush();
 				fw.close();
 			}
 			return fileName;
 		} catch (Exception e) {
 				throw new Exception(e.getMessage()+" Error 001");
 		}
 	}
 	
 	public static String getBackupFileName(String routineName, String location) {
 		return getBackupFileName(routineName, location, "yyMMdd");
 	}
 	public static String getBackupFileName(String routineName, String location, String dateString) {
         if (!(VistaConnection.getCurrentProject().compareTo("") == 0)) {
         	try {
         		IResource resource = MEditorUtilities.getProject(MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME)); //"mcode");
         		//location = resource.getLocation().makeAbsolute().toString();
         		location = resource.getLocation().toString(); //makeAbsolute() is forcing it into the workspace, which is wrong because a project location's source files may exist outside of a workspace
         	} catch (Exception e) {
         		
         	}
             location = MEditorPreferencesPage.getDirectoryPreference("mcode", MPiece.getPiece(VistaConnection.getCurrentServer(),";"), routineName);
         }
         SimpleDateFormat dateFormat = new SimpleDateFormat(dateString);
         Date date = new Date();
         if (! (new File(location+"/backup").exists())) {
             new File(location+"/backup").mkdirs();
         }
         return location+"/backup/"+routineName+" "+dateFormat.format(date)+"_last.m";
     }
     
 	public static String getLastLoadFileName(String routineName, String location) {
 		return getLastLoadFileName(routineName, location, "yyMMdd");
 	}
 	public static String getLastLoadFileName(String routineName, String location, String dateString) {
         if (!(VistaConnection.getCurrentProject().compareTo("") == 0)) {
         	try {
         		IResource resource = MEditorUtilities.getProject(MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME)); //"mcode");
         		location = resource.getLocation().makeAbsolute().toString();
         	} catch (Exception e) {
         		
         	}
             location = MEditorPreferencesPage.getDirectoryPreference("mcode", MPiece.getPiece(VistaConnection.getCurrentServer(),";"), routineName);
         }
         SimpleDateFormat dateFormat = new SimpleDateFormat(dateString);
         Date date = new Date();
 /*  JLI 100728 need to check backwards until find one or none left
         if (! (new File(location+"/backup").exists())) {
             new File(location+"/backup").mkdirs();
         }
         return location+"/backup/"+routineName+" "+dateFormat.format(date)+"_last.m";
 */
         String fileName = location+"/backup/"+routineName+" "+dateFormat.format(date)+"_last.m";
         if (! (new File(location+"/backup").exists())) {
             new File(location+"/backup").mkdirs();
             return fileName; // if have to create directory, can't be there
         }
         File folder = new File(location+"/backup/");
         File[] files = folder.listFiles();
         File selectedFile = null;
         long dateModified = 0;
         for (int i=0; i<files.length; i++) {
         	File file = files[i];
         	fileName = file.getName();
         	// JLI 101105 add " " to filename to prevent getting next larger if it doesn't exist 
         	if (file.getName().startsWith(routineName+" ")) {
         		if (fileName.endsWith("_last.m")) {
             		if (dateModified == 0) {
             			dateModified = file.lastModified();
             			selectedFile = file;
             		}
             		else if (file.lastModified() > dateModified) {
             			dateModified = file.lastModified();
             			selectedFile = file;
 /*
             			try {
     					MessageDialog.openInformation(
     							getWindow().getShell(),
     							"Meditor Plug-in",
             			        "file.getAbsolutePath = "+file.getAbsolutePath()+"\n"
             					   +"file.getCanonicalPath = "+file.getCanonicalPath()+"\n"
             					   +"file.getName = "+file.getName()+"\n"
             					   +"file.getPath = "+file.getPath());
             			} catch (Exception e){
             				
             			}
 */
             		}
         		}
         	}
         }
         if (selectedFile == null) return location+"/backup/"+routineName+" "+dateFormat.format(date)+"_last.m";
         else return selectedFile.getPath();
     }
 
 	public static String getBackupDir(String routineName, String location) {
         if (!(VistaConnection.getCurrentProject().compareTo("") == 0)) {
             location = MEditorPreferencesPage.getDirectoryPreference("mcode", MPiece.getPiece(VistaConnection.getCurrentServer(),";"),routineName);
         }
         if (! (new File(location+"/backup").exists())) {
             new File(location+"/backup").mkdirs();
         }
         return location+"/backup";
     }
 	
 	private static String codeForNewRoutine(String routineName) {
 		String doc = "";
 		NewRoutineDialogForm nrd = new NewRoutineDialogForm();
 		String str = nrd.askNewRoutine(routineName);
 		if (str == null)
 			return doc;
 		if (str == "") {
 			return doc;
 		}
 		else {
 			int loc, loc1;
 			loc = str.indexOf("~^~");
 			doc = routineName+"\t;"+str.substring(0,loc)+" ;\n";
 			loc1 = loc+3;
 			loc = str.indexOf("~^~",loc1);
 			doc = doc + "\t;;"+str.substring(loc1,loc)+"\n";
 			loc1 = loc + 3;
 			loc = str.indexOf("~^~",loc1);
 			String unitTestName = str.substring(loc1,loc);
 			if (! (unitTestName.compareTo("") == 0)) {
 				setUnitTestName(routineName, unitTestName);
 			}
 			loc1 = loc + 3;
 			String updateRoutineFileEntry = str.substring(loc1,str.length());
 			boolean value = false;
 			if (updateRoutineFileEntry.compareTo("1") == 0) {
 				value = true;
 			}
 			setUpdateRoutineFile(routineName, value);
 		}
 		return doc;
 	}
 	
 	protected static String getRoutineFromServer(String routineName) throws Exception {
 		myConnection = VistaConnection.getCurrentConnection(); //091027 to make check for change in servers
 		String str = "";
 		if (! (myConnection == null)) {
 				RpcRequest vReq = RpcRequestFactory.getRpcRequest("", "XT ECLIPSE M EDITOR");
 					vReq.setUseProprietaryMessageFormat(false);
 				vReq.getParams().setParam(1, "string", "RL");  // RD  RL  GD  GL  RS
 				vReq.getParams().setParam(2, "string", "notused");
 				vReq.getParams().setParam(3, "string", routineName);
 				RpcResponse vResp = myConnection.executeRPC(vReq);
 				str = vResp.getResults();
 		}
 		return str;
 	}
 	
 	public static String getFullFileName(String routineName) throws Exception {
 		return getFullFileLocation(routineName)+"/"+routineName+".m";
 	}
 	
 	public static String getFullFileLocation(String routineName) throws Exception {
 		return getFullFileLocation("", routineName);
 	}
 	
 	
 	public static String getFullFileLocation(String project, String routineName) throws Exception {
 		String location;
 
 		if ((project.compareTo("") == 0) || (project.compareTo("mcode") == 0)) { //TODO: still has references to 'mcode'
 			currentServer = VistaConnection.getCurrentServer();
 			String server = MPiece.getPiece(currentServer,";",1);
 			project = MPiece.getPiece(currentServer,";",4);
 	        //if (project.compareTo("") == 0) { //if this is a VC project, it should always get the location from here, which will in turn be VC location with correct path adjusted
 			location = MEditorPreferencesPage.getDirectoryPreference(project, server, routineName);
 //			if (project.equals("")) {
 //	            location = MEditorPreferencesPage.getDirectoryPreference("", server, routineName);
 //	        }
 //	        else {
 //	            //location = MEditorUtilities.getProject(MPiece.getPiece(currentServer,";",4)).getLocation().toString(); //wrong, should not return the VC root. may need to return the VistaFOIA package path
 //	        }
 		}
 		else {
 			location = MEditorUtilities.getProject(project).getLocation().toString();
 		}
 		if (! (new File(location).exists())) {
 			new File(location).mkdirs();
 		}
 		return location;
 	}
 	
 	public static String getPrimaryFileLocation(String routineName) {
 		String server = MPiece.getPiece(VistaConnection.getPrimaryServerID(),";",1);
 		String location = MEditorPreferencesPage.getDirectoryPreference("", server, routineName);
 		if (! (new File(location).exists())) {
 			new File(location).mkdirs();
 		}
 		return location;
 	}
 	
 	public static String getRelativeFileName(String routineName) {
 		return getRelativeFileLocation(routineName)+"/"+routineName;
 	}
 	
 	public static String getRelativeFileLocation(String routineName) {
 		String projDirectory = "";
         String location = "";
         projDirectory = VistaConnection.getCurrentProject();
         if (projDirectory.equals(""))
         	return "/";
 //        if (! (projDirectory.compareTo("") == 0)) { //this is wrong, if the current project directory is a VC directory... return the correct VistAFoia path or the root path....
 //        	return projDirectory+"/";
 //        }
 //        else {
 //            if (MEditorPrefs.isPrefsActive()) { //previous code was "making sure" this is set to 'mcode'
 //                projDirectory = MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME);
 //            }
             String server = MPiece.getPiece(VistaConnection.getCurrentServer(),";",1);
             location = MEditorPreferencesPage.getDirectoryPreference("", server, routineName);
 //        }
 //		if (! (projDirectory.compareTo("") == 0) ) {
 			int loc = location.indexOf(projDirectory);
 			while (loc > -1) {
 				if ((loc+projDirectory.length()) < location.length()) {
 					location = location.substring(loc+projDirectory.length()+1); //prev: C:/Users/Jspivey/DEV/Github/VistA-FOIA/Packages/Problem List/Routines
 				}
 				else {
 					location = "";
 				}
 				loc = -1;
 			}
 //		}
 		return location;
 	}
 	
 	public static IWorkbenchWindow getWindow() {
 		IWorkbench wb = PlatformUI.getWorkbench();
 		return wb.getActiveWorkbenchWindow();
 	}
 	
    private static boolean updateBU = false;
 
         //Make sure the project is refreshed
         //as the file was created outside the
         //Eclipse API.
 	private static void doRefresh(String routineName, boolean updateBackup) throws Exception {
         String project = VistaConnection.getCurrentProject();
        
        if (project.equals(""))
        	project = "mcode";
        
         if (! (project.compareTo("") == 0)) {
             doRefreshProject(routineName, project);
         }
         else {
             IResource resource = MEditorUtilities.getProject(MEditorPreferencesPage.getProjectName()); //"mcode");
             IContainer container = (IContainer)resource;
             container.refreshLocal(
                     IResource.DEPTH_INFINITE, null);
 
             VistaConnection.getCurrentServer();
             routineName = getRelativeFileName(routineName);
             final IFile iFile = container.getFile(
             new Path(routineName));
             updateBU = updateBackup;
         
             IWorkbenchWindow win = MEditorUtilities.getIWorkbenchWindow();
             win.getShell().getDisplay().asyncExec(
                     new Runnable() {
                         public void run() {
                             IWorkbenchPage page1 =
                                 PlatformUI.getWorkbench().
                                 getActiveWorkbenchWindow().
                                 getActivePage();
                             try {
                                 if (! updateBU) {
                                     IEditorPart activeEditor = page1.getActiveEditor();
                                     page1.closeEditor(activeEditor, false);
                                     activeEditor = IDE.openEditor(
                                             page1,
                                             iFile,
                                             true);
                                     /*
                                     String tooltip = activeEditor.getTitleToolTip();
                                     String title = activeEditor.getTitle();
                                     MessageDialog.openInformation(
                                             getWindow().getShell(),
                                             "Meditor Plug-in",
                                             "ToolTip is: "+tooltip+"\n"+
                                             "Title is: "+title);
                                     */
                                 }
                             else {
                                     FileEditorInput fileEditorInput = new FileEditorInput(iFile);
                                     PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(fileEditorInput,"gov.va.med.iss.meditor.editors.MEditor", false);
                             }
                             page1 = PlatformUI.getWorkbench().
                             getActiveWorkbenchWindow().
                                 getActivePage();
                             IEditorPart part = page1.getActiveEditor();
                             if (part instanceof MEditor) {
                             	((MEditor) part).setTopIndex(MEditor.oldTopIndex);
                             	((MEditor) part).setCaretOffset(MEditor.oldCaretOffset);
                                 ((MEditor) part).setWordWrap();  //update();
                                 ((MEditor) part).updateSourceViewerConfiguration();
                                 ((MEditor) part).getTheSourceViewer().invalidateTextPresentation();
                                 ((MEditor) part).setRoutinePrimaryServer(VistaConnection.getPrimaryServerID());
                             }
                         }
                         catch (PartInitException e) {
                         }
                     }
                 });
         } // else
     }
     
     private static void doRefreshProject(String routineName, String projectName) {
         try {
             IWorkspace workspace = ResourcesPlugin.getWorkspace();
             IWorkspaceRoot root = workspace.getRoot();
             IProject project = root.getProject(projectName);
 
             IResource resource = MEditorUtilities.getProject(projectName);
             IContainer container = (IContainer)resource;
             container.refreshLocal(
                     IResource.DEPTH_INFINITE, null);
 
             VistaConnection.getCurrentServer();
             routineName = getRelativeFileName(routineName);
 
    		IPreferenceStore store = MEditorPlugin.getDefault().getPreferenceStore();
    		//VistaConnection.getPrimaryServer(); //must force the properties to load...
    		boolean saveByServer = store.getBoolean(MEditorPlugin.P_SAVE_BY_SERVER);
    		boolean vcPorject = !MPiece.getPiece(VistaConnection.getCurrentServer(), ";", 4).equals("");	
    		if (saveByServer && !vcPorject) {
    			routineName = "//" +MPiece.getPiece(VistaConnection.getCurrentServer(), ";")+ routineName; 
    		}
    		
             IPath location = new Path(routineName);
             final IFile file = project.getFile(location); //wrong, because a loaded directory may come from location outside of the project root.
             //final IFile file = project.getFile(location.lastSegment());
            
             IWorkbenchWindow win = MEditorUtilities.getIWorkbenchWindow();
             win.getShell().getDisplay().asyncExec(
                 new Runnable() {
                     public void run() {
                         IWorkbenchPage page1 =
                             PlatformUI.getWorkbench().
                             getActiveWorkbenchWindow().
                             getActivePage();
                         try {
                         	IEditorPart activeEditor;
                         	if (! newPage) { 
                         		activeEditor = page1.getActiveEditor();
                         		page1.closeEditor(activeEditor, false);
                         	}
                             activeEditor = IDE.openEditor(
                                     page1,
                                     file, // iFile,
                                     true);
 
                             /*
                             MessageDialog.openInformation(
                                     getWindow().getShell(),
                                     "Meditor Plug-in",
                                     "ToolTip is: "+tooltip+"\n"+
                                     "Title is: "+title);
                             */
 
 /* might work in 3.3 and above
                         final String rouName = routineName;
                         URI uri = new URI(rouName);
                         activeEditor = IDE.openEditor(page1,uri,"gov.va.med.iss.meditor",true);
 */
                             page1 = PlatformUI.getWorkbench().
                                 getActiveWorkbenchWindow().
                                     getActivePage();
                             IEditorPart part = page1.getActiveEditor();
                             if (part instanceof MEditor) {
                             	((MEditor) part).setTopIndex(MEditor.oldTopIndex);
                             	((MEditor) part).setCaretOffset(MEditor.oldCaretOffset);
                                 ((MEditor) part).setWordWrap();  //update();
                                 ((MEditor) part).updateSourceViewerConfiguration();
                                 ((MEditor) part).getTheSourceViewer().invalidateTextPresentation();
                                 ((MEditor) part).setRoutinePrimaryServer(VistaConnection.getPrimaryServerID());
                             }
                         }
                         catch (PartInitException e) {
                         }
                     }
                 });
         }
         catch (Exception e) {
             String x = e.getMessage();
             x = x + "  bb";
         }
     }  // doRefreshProject(
 
 	private static void setUnitTestName(String routineName, String unitTestName) {
 		IFile iFile = getIFile(routineName);
 			MEditorPropertyPage1 mepp = new MEditorPropertyPage1();
 			mepp.setUnitTestNamePropertyValue((IResource) iFile, unitTestName);
 	}
 /*	
 	private static void setReadOnly(String routineName) {
 		IFile iFile = getIFile(routineName);
 		MEditorPropertyPage1 mepp = new MEditorPropertyPage1();
 		mepp.setReadOnlyPropertyValue((IResource) iFile, true);
 	}
 */	
 	private static void setUpdateRoutineFile(String routineName, boolean value) {
 		IFile iFile = getIFile(routineName);
 		MEditorPropertyPage1 mepp = new MEditorPropertyPage1();
 		mepp.setUpdateRoutineFilePropertyValue((IResource) iFile, value);
 	}
 	
 	private static IFile getIFile(String routineName) {
 		routineName = routineName + ".m";
 		try {
 			IResource resource = MEditorUtilities.getProject(MEditorPreferencesPage.getProjectName());
 			String fileLocation = RoutineLoad.getFullFileName(routineName);
 			File file = new File(fileLocation);
 			if (! (file.exists())) {
 				FileWriter fw = new FileWriter(fileLocation);
 				fw.write("");
 				fw.flush();
 				fw.close();
 			}
 			IContainer container = (IContainer)resource;
 	        container.refreshLocal(
 	            IResource.DEPTH_INFINITE, null);
 
 	        routineName = getRelativeFileName(routineName);
 	        IFile iFile = container.getFile(
 	            new Path(routineName));
 	        return iFile;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 }
