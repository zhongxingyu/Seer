 /*
  * Created on Aug 25, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package gov.va.med.iss.meditor.preferences;
 
 
 import java.io.File;
 
 import org.eclipse.jface.preference.*;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IEditorPart;
 /*
 import org.eclipse.ui.texteditor.ITextEditor;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.eclipse.ui.texteditor.IDocumentProvider;
 import gov.va.med.iss.connection.VLConnectionPlugin;
 import gov.va.med.iss.meditor.MEditorSourceViewerConfiguration;
 */
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
 import org.eclipse.core.runtime.Preferences.PropertyChangeEvent;
 import gov.va.med.iss.meditor.MEditorPlugin;
 import gov.va.med.iss.meditor.m.MCodeScanner;
 import gov.va.med.iss.meditor.editors.MEditor;
 import gov.va.med.iss.meditor.utils.MColorProvider;
 import gov.va.med.iss.meditor.preferences.MEditorPrefs;
 import gov.va.med.iss.meditor.utils.MEditorUtilities;
 import gov.va.med.iss.connection.actions.VistaConnection;
 import gov.va.med.iss.connection.utilities.MPiece;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.mumps.pathstructure.vista.RoutinePathResolver;
 import org.mumps.pathstructure.vista.RoutinePathResolverFactory;
 
 /**
  * This class represents a preference page that
  * is contributed to the Preferences dialog. By 
  * subclassing <samp>FieldEditorPreferencePage</samp>, we
  * can use the field support built into JFace that allows
  * us to create a page that is small and knows how to 
  * save, restore and apply itself.
  * <p>
  * This page is used to modify preferences only. They
  * are stored in the preference store that belongs to
  * the main plug-in class. That way, preferences can
  * be accessed directly via the preference store.
  */
 
 
 public class MEditorPreferencesPage
 	extends FieldEditorPreferencePage
 	implements IWorkbenchPreferencePage {
 	public static final String P_PATH = "pathPreference";
 	public static final String P_CHOICE = "choicePreference";
 	public static final String P_STRING = "stringPreference";
 	
 	public MEditorPreferencesPage() {
 		this(null);
 	}
 
 	public MEditorPreferencesPage(MEditor meditor) {
 		super(GRID);
 		setPreferenceStore(MEditorPlugin.getDefault().getPreferenceStore());
 		setDescription("M-Editor Preferences");
 		initializeDefaults();
 		MEditorPlugin.getDefault().getPluginPreferences().addPropertyChangeListener(preferenceChangeListener);
 	}
 /**
  * Sets the default values of the preferences.
  */
 	private void initializeDefaults() {
 		IPreferenceStore store = getPreferenceStore();
 		store.setDefault(MEditorPlugin.OFFLINE_MODE, false);
 		store.setDefault(MEditorPlugin.P_DEFAULT_UPDATE, true);
 		store.setDefault(MEditorPlugin.P_WRAP_LINES, false);
 		store.setDefault(MEditorPlugin.P_PROJECT_NAME,"mcode");
 		store.setDefault(MEditorPlugin.P_SAVE_BY_SERVER,true);
 		store.setDefault(MEditorPlugin.P_SAVE_BY_NAMESPACE,"0");
 		store.setDefault(MEditorPlugin.P_SAVE_DIR_EXAMPLE,
 				MEditorPreferencesPage.getDirectoryPreference("", "Server","ROUTINE"));
 
 		store.setDefault(MEditorPlugin.P_MULTI_LINE_COMMENT_COLOR, "64,128,128");
 		store.setDefault(MEditorPlugin.P_COMMENT_COLOR, "128,128,0");
 		
 		store.setDefault(MEditorPlugin.P_DEFAULT_COLOR, "0,0,0");
 		store.setDefault(MEditorPlugin.P_KEYWORD_COLOR, "127,0,85"); ///  <====
 		store.setDefault(MEditorPlugin.P_TYPE_COLOR, "64,0,200");  /// <====
 		store.setDefault(MEditorPlugin.P_STRING_COLOR, "0,0,255");
 
 		store.setDefault(MEditorPlugin.P_FUNCS_COLOR, "255,0,0");
 		store.setDefault(MEditorPlugin.P_OPS_COLOR, "155,50,50");
 		store.setDefault(MEditorPlugin.P_TAGS_COLOR, "0,127,0");
 		store.setDefault(MEditorPlugin.P_VARS_COLOR, "170,0,170");
 		store.setDefault(MEditorPlugin.P_COMMAND_COLOR, "155,50,50");  // 97,162,97
 		store.setDefault(MEditorPlugin.P_CONDITIONS_COLOR,"0,0,0");
 	}
 	
 	public static String getDirectoryPreference(String projectName, String serverName, String routineName) {
 		if (serverName.compareTo("") == 0) {
 			String currServer = VistaConnection.getCurrentServer();
 			if (currServer.compareTo(";;;") == 0) {
 				//currServer = VistaConnection.getPrimaryServer();
 				VistaConnection.getPrimaryServer();
 				currServer = VistaConnection.getCurrentServer();
 			}
 			serverName = MPiece.getPiece(currServer,";");
 		}
 		IResource resource = null;
 		if (MEditorPrefs.isPrefsActive()) {
 			try {
 				resource = MEditorUtilities.getProject(projectName); //force it to auto discover the correct projectName or use the passed in projectName
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		String str = "";
 		if (!(resource == null)) { //why does it return malformed locations? Before the project pref is active, it returns a relative path, but afterwards it returns a complete path
 			str = resource.getLocation().toString();
 		}
 		IPreferenceStore store = MEditorPlugin.getDefault().getPreferenceStore();
 		//VistaConnection.getPrimaryServer(); //must force the properties to load...
 		boolean vcPorject = !MPiece.getPiece(VistaConnection.getCurrentServer(), ";", 4).equals("");	
 		boolean saveByServer = store.getBoolean(MEditorPlugin.P_SAVE_BY_SERVER);
 		String val = store.getString(MEditorPlugin.P_SAVE_BY_NAMESPACE);
 		int saveByNamespaceCnt = val != null && ! val.equals("") ? Integer.parseInt(val) : 0;
 		
 		if (vcPorject) {
 			if (str.equals("") || !str.contains("/"))
 				try {
 					str = MEditorUtilities.getProject(MPiece.getPiece(VistaConnection.getCurrentServer(), ";", 4)).getLocation().toString();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			
 			File projectPath = new File(str);
 			RoutinePathResolver routinePathResolver = RoutinePathResolverFactory.getInstance().getRoutinePathResolver(projectPath);
 			return str +"/"+routinePathResolver.getRelativePath(routineName);
 		}
 		
 		if (saveByServer) {
 			str += "/"+serverName;
 		}
		str += "/"+ routineName.substring(0, saveByNamespaceCnt);
 
 		
 		if (! ((serverName.compareTo("Server") == 0) && (routineName.compareTo("ROUTINE") == 0) )) {
 			if (! (new File(str).exists())) {
 				new File(str).mkdirs();
 			}
 		}
 		return str;
 	}
 	
 /**
  * Creates the field editors. Field editors are abstractions of
  * the common GUI blocks needed to manipulate various types
  * of preferences. Each field editor knows how to save and
  * restore itself.
  */
 
 	public void createFieldEditors() {
 		addField(new BooleanFieldEditor(MEditorPlugin.OFFLINE_MODE, "Offline Mode", getFieldEditorParent()));
 		addField(
 			new BooleanFieldEditor(
 					MEditorPlugin.P_DEFAULT_UPDATE,
 				"&set default to update Routine File on Routine Save",
 				getFieldEditorParent()));
 		addField( new BooleanFieldEditor(MEditorPlugin.P_WRAP_LINES,"&wrap lines",getFieldEditorParent()));
 		addField(new StringFieldEditor(MEditorPlugin.P_PROJECT_NAME,"Enter the desired project for saving routines: ",getFieldEditorParent()));
 		addField(new BooleanFieldEditor(MEditorPlugin.P_SAVE_BY_SERVER,"Save Routines by Server: ",getFieldEditorParent()));
 		addField(new StringFieldEditor(MEditorPlugin.P_SAVE_BY_NAMESPACE,
 				"Enter 0-3: number of letters of routine name to file by\n"+
 				"0 - all routines saved together\n"+
 				"1 - routines saved by first letter of name\n"+
 				"2 - routines saved by first two letters of name\n"+
 				"3 - routines saved by first three letters of name",getFieldEditorParent()));
 		StringFieldEditor strFldEdtr = new StringFieldEditor(MEditorPlugin.P_SAVE_DIR_EXAMPLE,
 				"Sample Directory for routine 'ROUTINE'",getFieldEditorParent());
 		strFldEdtr.setEnabled(false,getFieldEditorParent());
 		addField(strFldEdtr);
 		addField(new ColorFieldEditor(MEditorPlugin.P_VARS_COLOR,"Variables",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_COMMAND_COLOR,"Commands",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_STRING_COLOR,"Strings",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_COMMENT_COLOR,"Comments",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_FUNCS_COLOR,"Functions",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_CONDITIONS_COLOR,"Conditions",getFieldEditorParent()));
 		addField(new ColorFieldEditor(MEditorPlugin.P_TAGS_COLOR,"Tags && Routines",getFieldEditorParent()));
 	}
 	
 	public void init(IWorkbench workbench) {
 	}
 	
 	private final IPropertyChangeListener preferenceChangeListener = new IPropertyChangeListener() {
 		public void propertyChange(PropertyChangeEvent event) {
 			boolean seenFlag = false;
 			IEditorPart part = MEditorUtilities.getIWorkbenchPage().getActiveEditor();
 			for (int i=0; i<MEditorPlugin.preferenceColors.length; i++) {
 				if (event.getProperty().equals(MEditorPlugin.preferenceColors[i])) {
 					seenFlag = true;
 					MColorProvider.updateColorTable(MEditorPlugin.preferenceColors[i],MEditorPrefs.getPrefs(MEditorPlugin.preferenceColors[i]));
 				}
 			}
 			if (! seenFlag) {
 				if (event.getProperty().equals(MEditorPlugin.P_WRAP_LINES)) {
 					seenFlag = true;
 					boolean wordWrap;
 					if (MEditorPrefs.getPrefs(MEditorPlugin.P_WRAP_LINES).equals("true")) {
 						wordWrap = true;
 					}
 					else wordWrap = false;
 					MEditor.setWordWrapValue(wordWrap);
 					((MEditor) part).setWordWrap();
 				}
 			}
 			if (seenFlag) {
 				MCodeScanner.setTokens();
 				if (part instanceof MEditor) {
 					((MEditor) part).updateSourceViewerConfiguration();
 					((MEditor) part).sourceViewer.invalidateTextPresentation();
 				}
 			}
 		}
 	};
 	
 	static public String getProjectName() {
 		String project = VistaConnection.getCurrentProject();
 		if (project.compareTo("") == 0) {
 			project = MEditorPrefs.getPrefs(MEditorPlugin.P_PROJECT_NAME);
 		}
 		return project;
 	}
 }
