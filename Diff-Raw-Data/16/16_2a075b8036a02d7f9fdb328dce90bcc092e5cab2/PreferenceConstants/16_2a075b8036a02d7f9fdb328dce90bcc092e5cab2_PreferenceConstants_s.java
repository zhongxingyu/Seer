 package org.rubypeople.rdt.ui;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.rubypeople.rdt.internal.launching.RubyInterpreter;
 import org.rubypeople.rdt.internal.launching.RubyRuntime;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 
 public class PreferenceConstants {
 
 	private PreferenceConstants() {}
 	
 	public static final String RI_PATH = "riDirectoryPath";
 	public static final String RDOC_PATH = "rdocDirectoryPath";	
 
 	public static final String FORMAT_INDENTATION = "formatIndentation"; //$NON-NLS-1$
 	public static final String FORMAT_USE_TAB = "formatUseTab"; //$NON-NLS-1$	
	public static final String CREATE_PARSER_ANNOTATIONS = "createParserAnnotations"; //$NON-NLS-1$
 	// TODO Finish implementing this option!
 	public static final String TEMPLATES_USE_CODEFORMATTER = "templatesUSeCodeFormatter";
 
 	
 	private final static String DEFAULT_RDOC_CMD = "rdoc"; //$NON-NLS-1$	
 	private final static String DEFAULT_RI_CMD = "ri"; //$NON-NLS-1$	
 
 
 	/**
 	 * A named preference that controls whether folding is enabled in the Ruby
 	 * editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_FOLDING_ENABLED = "editor_folding_enabled"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that stores the configured folding provider.
 	 * <p>
 	 * Value is of type <code>String</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_FOLDING_PROVIDER = "editor_folding_provider"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that stores the value for Rdoc folding for the default
 	 * folding provider.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_FOLDING_RDOC = "editor_folding_default_rdoc"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if temporary problems are evaluated and
 	 * shown in the UI.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String EDITOR_EVALUTE_TEMPORARY_PROBLEMS = "handleTemporaryProblems"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that stores the value for inner type folding for the
 	 * default folding provider.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_FOLDING_INNERTYPES = "editor_folding_default_innertypes"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that stores the value for method folding for the
 	 * default folding provider.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_FOLDING_METHODS = "editor_folding_default_methods"; //$NON-NLS-1$
 
 	/**
 	 * Preference key suffix for bold text style preference keys.
 	 * 
 	 * @since 2.1
 	 */
 	public static final String EDITOR_BOLD_SUFFIX = "_bold"; //$NON-NLS-1$
 
 	/**
 	 * Preference key suffix for italic text style preference keys.
 	 * 
 	 * @since 3.0
 	 */
 	public static final String EDITOR_ITALIC_SUFFIX = "_italic"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if correction indicators are shown in
 	 * the UI.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String EDITOR_CORRECTION_INDICATION = "RubyEditor.ShowTemporaryProblem"; //$NON-NLS-1$
 	/**
 	 * A named preference that defines whether the hint to make hover sticky
 	 * should be shown.
 	 * 
 	 * @see RubyUI
 	 * @since 3.0
 	 */
 	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE = "PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE"; //$NON-NLS-1$
 
 	public static void initializeDefaultValues(IPreferenceStore store) {
 		store.setDefault(PreferenceConstants.FORMAT_INDENTATION, 2);
 		store.setDefault(PreferenceConstants.FORMAT_USE_TAB, false);
		store.setDefault(PreferenceConstants.CREATE_PARSER_ANNOTATIONS, false);
 
 		// TODO Expose these preferences to the user!
 		store.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION, true);
 		store.setDefault(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, true);
 
 		// folding
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROVIDER, "org.rubypeople.rdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_RDOC, false);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_INNERTYPES, true);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_METHODS, false);
 
 		store.setDefault(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, true);
 					
 		store.setDefault( PreferenceConstants.RDOC_PATH, PreferenceConstants.getDefaultPath(PreferenceConstants.DEFAULT_RDOC_CMD)) ; 
 		store.setDefault( PreferenceConstants.RI_PATH, PreferenceConstants.getDefaultPath(PreferenceConstants.DEFAULT_RI_CMD)) ; 
 
 	}
 
 
 	private static String getDefaultPath(String programName) {		
 		RubyInterpreter interpreter = RubyRuntime.getDefault().getSelectedInterpreter();
 		if (interpreter == null) {
 			return programName ;
 		}
 		IPath path = interpreter.getInstallLocation();
 		path = path.uptoSegment(path.segmentCount() - 1).append(programName);
 		return path.toOSString();
 	}
 
 
 	/**
 	 * Returns the RDT-UI preference store.
 	 * 
 	 * @return the RDT-UI preference store
 	 */
 	public static IPreferenceStore getPreferenceStore() {
 		return RubyPlugin.getDefault().getPreferenceStore();
 	}
 
 }
