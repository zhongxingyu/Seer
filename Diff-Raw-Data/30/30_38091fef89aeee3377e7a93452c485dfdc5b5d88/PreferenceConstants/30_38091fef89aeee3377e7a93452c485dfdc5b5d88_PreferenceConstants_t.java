 package org.rubypeople.rdt.ui;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 import org.rubypeople.rdt.core.ILoadpathEntry;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.ui.RubyPlugin;
 import org.rubypeople.rdt.internal.ui.preferences.PreferencesMessages;
 import org.rubypeople.rdt.internal.ui.preferences.formatter.ProfileManager;
 
 public class PreferenceConstants {
 
 	private PreferenceConstants() {}
 	
 	private static String fgDefaultEncoding= System.getProperty("file.encoding"); //$NON-NLS-1$
 
 	public static final String DEBUGGER_USE_RUBY_DEBUG = "useRubyDebug";
 
 	public static final String TEMPLATES_USE_CODEFORMATTER = "templatesUseCodeFormatter"; //$NON-NLS-1$	
 	
 	private static final String LOADPATH_RUBYVMLIBRARY_INDEX= PreferenceConstants.NEWPROJECT_JRELIBRARY_INDEX;
 	private static final String LOADPATH_RUBYVMLIBRARY_LIST= PreferenceConstants.NEWPROJECT_JRELIBRARY_LIST;
 
 	
 	/**
 	 * A named preference that holds a list of possible JRE libraries used by the New Java Project wizard. A library 
 	 * consists of a description and an arbitrary number of <code>IClasspathEntry</code>s, that will represent the 
 	 * JRE on the new project's class path. 
 	 * <p>
 	 * Value is of type <code>String</code>: a semicolon separated list of encoded JRE libraries. 
 	 * <code>NEWPROJECT_JRELIBRARY_INDEX</code> defines the currently used library. Clients
 	 * should use the method <code>encodeJRELibrary</code> to encode a JRE library into a string
 	 * and the methods <code>decodeJRELibraryDescription(String)</code> and <code>
 	 * decodeJRELibraryClasspathEntries(String)</code> to decode the description and the array
 	 * of class path entries from an encoded string.
 	 * </p>
 	 * 
 	 * @see #NEWPROJECT_JRELIBRARY_INDEX
 	 * @see #encodeJRELibrary(String, IClasspathEntry[])
 	 * @see #decodeJRELibraryDescription(String)
 	 * @see #decodeJRELibraryClasspathEntries(String)
 	 */
 	public static final String NEWPROJECT_JRELIBRARY_LIST= "org.rubypeople.rdt.ui.wizards.rubyvm.list"; //$NON-NLS-1$
 
 	/**
 	 * A named preferences that specifies the current active JRE library.
 	 * <p>
 	 * Value is of type <code>Integer</code>: an index into the list of possible JRE libraries.
 	 * </p>
 	 * 
 	 * @see #NEWPROJECT_JRELIBRARY_LIST
 	 */
 	public static final String NEWPROJECT_JRELIBRARY_INDEX= "org.rubypeople.rdt.ui.wizards.rubyvm.index"; //$NON-NLS-1$
 
 
 	/**
 	 * A named preference that controls parameter names rendering of methods in
 	 * the UI.
 	 * <p>
 	 * Value is of type <code>Boolean</code>: if <code>true</code> return
 	 * names are rendered
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String APPEARANCE_METHOD_PARAMETER_NAMES = "org.rubypeople.rdt.ui.methodparameternames";//$NON-NLS-1$
 
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
 	 * Preference key suffix for background text style preference keys.
 	 * 
 	 * @since 2.1
 	 */
 	public static final String EDITOR_BG_SUFFIX = "_background"; //$NON-NLS-1$
 
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
 	 * @since 0.8.0
 	 */
 	public static final String EDITOR_SHOW_TEXT_HOVER_AFFORDANCE = "PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if segmented view (show selected element
 	 * only) is turned on or off.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public static final String EDITOR_SHOW_SEGMENTS = "org.rubypeople.rdt.ui.editor.showSegments"; //$NON-NLS-1$
 	/**
 	 * A named preference that controls whether the outline view selection
 	 * should stay in sync with with the element at the current cursor position.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public final static String EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE = "RubyEditor.SyncOutlineOnCursorMove"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the 'sub-word navigation' feature is
 	 * enabled.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * @since 1.0
 	 */
 	public final static String EDITOR_SUB_WORD_NAVIGATION= "subWordNavigation"; //$NON-NLS-1$
 	
 	/**
 	 * A named preference that defines how member elements are ordered by the
 	 * Ruby views using the <code>RubyElementSorter</code>.
 	 * <p>
 	 * Value is of type <code>String</code>: A comma separated list of the
 	 * following entries. Each entry must be in the list, no duplication. List
 	 * order defines the sort order.
 	 * <ul>
 	 * <li><b>T</b>: Types</li>
 	 * <li><b>C</b>: Constructors</li>
 	 * <li><b>M</b>: Methods</li>
 	 * <li><b>F</b>: Fields</li>
 	 * <li><b>SM</b>: Static Methods</li>
 	 * <li><b>SF</b>: Static Fields</li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String APPEARANCE_MEMBER_SORT_ORDER = "outlinesortoption"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that defines how member elements are ordered by
 	 * visibility in the Ruby views using the <code>RubyElementSorter</code>.
 	 * <p>
 	 * Value is of type <code>String</code>: A comma separated list of the
 	 * following entries. Each entry must be in the list, no duplication. List
 	 * order defines the sort order.
 	 * <ul>
 	 * <li><b>B</b>: Public</li>
 	 * <li><b>V</b>: Private</li>
 	 * <li><b>R</b>: Protected</li>
 	 * </ul>
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String APPEARANCE_VISIBILITY_SORT_ORDER = "org.eclipse.jdt.ui.visibility.order"; //$NON-NLS-1$
 
 	/**
 	 * A named preferences that controls if Ruby elements are also sorted by
 	 * visibility.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER = "org.rubypeople.rdt.ui.enable.visibility.order"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if package name compression is turned on
 	 * or off.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @see #APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW
 	 */
 	public static final String APPEARANCE_COMPRESS_PACKAGE_NAMES = "org.rubypeople.rdt.ui.compresspackagenames";//$NON-NLS-1$
 
 	/**
 	 * A named preference that defines the pattern used for package name
 	 * compression.
 	 * <p>
 	 * Value is of type <code>String</code>. For example for the given
 	 * package name 'org.eclipse.jdt' pattern '.' will compress it to '..jdt',
 	 * '1~' to 'o~.e~.jdt'.
 	 * </p>
 	 */
 	public static final String APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW = "PackagesView.pkgNamePatternForPackagesView";//$NON-NLS-1$
 	/**
 	 * The symbolic font name for the Ruby editor text font (value
 	 * <code>"org.rubypeople.rdt.ui.editors.textfont"</code>).
 	 * 
 	 * @since 0.8.0
 	 */
 	public final static String EDITOR_TEXT_FONT = "org.rubypeople.rdt.ui.editors.textfont"; //$NON-NLS-1$
 	/**
 	 * A named preference that controls which profile is used by the code
 	 * formatter.
 	 * <p>
 	 * Value is of type <code>String</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String FORMATTER_PROFILE = "formatter_profile"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls the layout of the Ruby Browsing views
 	 * vertically. Boolean value.
 	 * <p>
 	 * Value is of type <code>Boolean</code>. If
 	 * <code>true<code> the views are stacked vertical.
 	 * If <code>false</code> they are stacked horizontal.
 	 * </p>
 	 */
 	public static final String BROWSING_STACK_VERTICALLY = "org.rubypeople.rdt.ui.browsing.stackVertically"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the projects view's selection is
 	 * linked to the active editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String LINK_BROWSING_PROJECTS_TO_EDITOR = "org.rubypeople.rdt.ui.browsing.projectstoeditor"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the types view's selection is
 	 * linked to the active editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String LINK_BROWSING_TYPES_TO_EDITOR = "org.rubypeople.rdt.ui.browsing.typestoeditor"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the members view's selection is
 	 * linked to the active editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.8.0
 	 */
 	public static final String LINK_BROWSING_MEMBERS_TO_EDITOR = "org.rubypeople.rdt.ui.browsing.memberstoeditor"; //$NON-NLS-1$
 
 	/**
 	 * Preference key suffix for strikethrough text style preference keys.
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_STRIKETHROUGH_SUFFIX = "_strikethrough"; //$NON-NLS-1$
 
 	/**
 	 * Preference key suffix for underline text style preference keys.
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_UNDERLINE_SUFFIX = "_underline"; //$NON-NLS-1$	
 
 	/**
 	 * A named preference that controls whether bracket matching highlighting is
 	 * turned on or off.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the color used to highlight matching
 	 * brackets.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 */
 	public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if the Ruby code assist gets auto
 	 * activated.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String CODEASSIST_AUTOACTIVATION = "content_assist_autoactivation"; //$NON-NLS-1$
 
 	/**
 	 * A name preference that holds the auto activation delay time in
 	 * milliseconds.
 	 * <p>
 	 * Value is of type <code>Integer</code>.
 	 * </p>
 	 */
 	public final static String CODEASSIST_AUTOACTIVATION_DELAY = "content_assist_autoactivation_delay"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if the Java code assist inserts a
 	 * proposal automatically if only one proposal is available.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String CODEASSIST_AUTOINSERT = "content_assist_autoinsert"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if the Java code assist only inserts
 	 * completions. If set to false the proposals can also _replace_ code.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String CODEASSIST_INSERT_COMPLETION = "content_assist_insert_completion"; //$NON-NLS-1$	
 
 	/**
 	 * A named preference that controls if argument names are filled in when a
 	 * method is selected from as list of code assist proposal.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String CODEASSIST_FILL_ARGUMENT_NAMES = "content_assist_fill_method_arguments"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if method arguments are guessed when a
 	 * method is selected from as list of code assist proposal.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String CODEASSIST_GUESS_METHOD_ARGUMENTS = "content_assist_guess_method_arguments"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the background color used in the code
 	 * assist selection dialog.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 */
 	public final static String CODEASSIST_PROPOSALS_BACKGROUND = "content_assist_proposals_background"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the foreground color used in the code
 	 * assist selection dialog.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 */
 	public final static String CODEASSIST_PROPOSALS_FOREGROUND = "content_assist_proposals_foreground"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the background color used for parameter
 	 * hints.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 */
 	public final static String CODEASSIST_PARAMETERS_BACKGROUND = "content_assist_parameters_background"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the foreground color used in the code
 	 * assist selection dialog.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 */
 	public final static String CODEASSIST_PARAMETERS_FOREGROUND = "content_assist_parameters_foreground"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the background color used in the code
 	 * assist selection dialog to mark replaced code.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 * @since 2.1
 	 */
 	public final static String CODEASSIST_REPLACEMENT_BACKGROUND = "content_assist_completion_replacement_background"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds the foreground color used in the code
 	 * assist selection dialog to mark replaced code.
 	 * <p>
 	 * Value is of type <code>String</code>. A RGB color value encoded as a
 	 * string using class <code>PreferenceConverter</code>
 	 * </p>
 	 * 
 	 * @see org.eclipse.jface.resource.StringConverter
 	 * @see org.eclipse.jface.preference.PreferenceConverter
 	 * @since 2.1
 	 */
 	public final static String CODEASSIST_REPLACEMENT_FOREGROUND = "content_assist_completion_replacement_foreground"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls if content assist inserts the common
 	 * prefix of all proposals before presenting choices.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 3.0
 	 */
 	public final static String CODEASSIST_PREFIX_COMPLETION = "content_assist_prefix_completion"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the 'close strings' feature is
 	 * enabled.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String EDITOR_CLOSE_STRINGS = "closeStrings"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the 'close brackets' feature is
 	 * enabled.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String EDITOR_CLOSE_BRACKETS = "closeBrackets"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the 'close braces' feature is
 	 * enabled.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 2.1
 	 */
 	public final static String EDITOR_CLOSE_BRACES = "closeBraces"; //$NON-NLS-1$
 	
 	/**
 	 * A named preference that controls whether the 'end' should be inserted
 	 * automatically.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 */
 	public final static String EDITOR_END_STATEMENTS = "endStatements"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether the 'smart home-end' feature is
 	 * enabled.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * @since 2.1
 	 */
 	public final static String EDITOR_SMART_HOME_END= AbstractTextEditor.PREFERENCE_NAVIGATION_SMART_HOME_END;
 	
 	/**
 	 * A named preference that controls whether occurrences are marked in the
 	 * editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_OCCURRENCES = "markOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether occurrences are sticky in the
 	 * editor.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_STICKY_OCCURRENCES = "stickyOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether type occurrences are marked.
 	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_TYPE_OCCURRENCES = "markTypeOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether method occurrences are marked.
 	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_METHOD_OCCURRENCES = "markMethodOccurrences"; //$NON-NLS-1$
 	/**
 	 * A named preference that controls whether non-constant field occurrences
 	 * are marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
 	 * <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_FIELD_OCCURRENCES = "markFieldOccurrences"; //$NON-NLS-1$
 	/**
 	 * A named preference that controls whether constant (static final)
 	 * occurrences are marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
 	 * <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_CONSTANT_OCCURRENCES = "markConstantOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether local variable occurrences are
 	 * marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
 	 * <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES = "markLocalVariableOccurrences"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that controls whether method exit points are marked.
 	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * 
 	 * @since 0.9.0
 	 */
 	public static final String EDITOR_MARK_METHOD_EXIT_POINTS = "markMethodExitPoints"; //$NON-NLS-1$
 
 	
 	/**
 	 * A named preference that controls whether new projects are generated using source folder.
 	 * <p>
 	 * Value is of type <code>Boolean</code>. if <code>true</code> new projects are created with a source 
 	 * folder. If <code>false</code> source folder equals to the project.
 	 * </p>
 	 */
 	public static final String SRCBIN_FOLDERS_IN_NEWPROJ= "org.rubypeople.rdt.ui.wizards.srcFoldersInNewProjects"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that specifies the source folder name used when creating a new Ruby project. Value is inactive
 	 * if <code>SRCBIN_FOLDERS_IN_NEWPROJ</code> is set to <code>false</code>.
 	 * <p>
 	 * Value is of type <code>String</code>. 
 	 * </p>
 	 * 
 	 * @see #SRCBIN_FOLDERS_IN_NEWPROJ
 	 */
 	public static final String SRCBIN_SRCNAME= "org.rubypeople.rdt.ui.wizards.srcFoldersSrcName"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that holds a list of semicolon separated fully qualified type names with wild card characters.
 	 * @since 1.0
 	 */	
 	public static final String TYPEFILTER_ENABLED= "org.rubypeople.rdt.ui.typefilter.enabled"; //$NON-NLS-1$	
 	
 	/**
 	 * A named preference that defines the key for the hover modifiers.
 	 *
 	 * @see RubyUI
 	 * @since 1.0
 	 */
 	public static final String EDITOR_TEXT_HOVER_MODIFIERS= "hoverModifiers"; //$NON-NLS-1$
 
 	/**
 	 * A named preference that defines the key for the hover modifier state masks.
 	 * The value is only used if the value of <code>EDITOR_TEXT_HOVER_MODIFIERS</code>
 	 * cannot be resolved to valid SWT modifier bits.
 	 * 
 	 * @see RubyUI
 	 * @see #EDITOR_TEXT_HOVER_MODIFIERS
 	 * @since 1.0
 	 */
 	public static final String EDITOR_TEXT_HOVER_MODIFIER_MASKS= "hoverModifierMasks"; //$NON-NLS-1$
 	
 	/**
 	 * The id of the best match hover contributed for extension point
 	 * <code>rubyEditorTextHovers</code>.
 	 *
 	 * @since 1.0
 	 */
 	public static final String ID_BESTMATCH_HOVER= "org.rubypeople.rdt.ui.BestMatchHover"; //$NON-NLS-1$
 
 	/**
 	 * The id of the source code hover contributed for extension point
 	 * <code>rubyEditorTextHovers</code>.
 	 *
 	 * @since 1.0
 	 */
 	public static final String ID_SOURCE_HOVER= "org.rubypeople.rdt.ui.RubySourceHover"; //$NON-NLS-1$
 	
 	/**
 	 * A named preference that controls a reduced search menu is used in the Ruby editors.
 	 * <p>
 	 * Value is of type <code>Boolean</code>.
 	 * </p>
 	 * @since 1.0
 	 */
 	public static final String SEARCH_USE_REDUCED_MENU= "Search.usereducemenu"; //$NON-NLS-1$
 	
 	private static String getDefaultRubyVMLibraries() {
 		StringBuffer buf= new StringBuffer();
 		ILoadpathEntry cntentry= getRubyVMContainerEntry();
 		buf.append(encodeRubyVMLibrary(PreferencesMessages.NewRubyProjectPreferencePage_jre_container_description, new ILoadpathEntry[] { cntentry} )); 
 		buf.append(';');
 		ILoadpathEntry varentry= getRubyVMVariableEntry();
 		buf.append(encodeRubyVMLibrary(PreferencesMessages.NewRubyProjectPreferencePage_jre_variable_description, new ILoadpathEntry[] { varentry })); 
 		buf.append(';');
 		return buf.toString();
 	}
 	
 	private static ILoadpathEntry getRubyVMVariableEntry() {
 		// FIXME We need to define more library variables! RUBY_CORE, RUBY_STD_LIB, RUBY_SITE_LIB
 		return RubyCore.newVariableEntry(new Path("RUBY_LIB")); //$NON-NLS-1$
 	}
 	
 	public static String encodeRubyVMLibrary(String desc, ILoadpathEntry[] cpentries) {
 		StringBuffer buf= new StringBuffer();
 		for (int i= 0; i < cpentries.length; i++) {
 			ILoadpathEntry entry= cpentries[i];
 			buf.append(encode(desc));
 			buf.append(' ');
 			buf.append(entry.getEntryKind());
 			buf.append(' ');
 			buf.append(encodePath(entry.getPath()));
 			buf.append(' ');
 			buf.append(entry.isExported());
 			buf.append(' ');
 		}
 		return buf.toString();
 	}
 	
 	private static String encodePath(IPath path) {
 		if (path == null) {
 			return "#"; //$NON-NLS-1$
 		} else if (path.isEmpty()) {
 			return "&"; //$NON-NLS-1$
 		} else {
 			return encode(path.toPortableString());
 		}
 	}
 	
 	private static String encode(String str) {
 		try {
 			return URLEncoder.encode(str, fgDefaultEncoding);
 		} catch (UnsupportedEncodingException e) {
 			RubyPlugin.log(e);
 		}
 		return ""; //$NON-NLS-1$
 	}	
 
 	public static void initializeDefaultValues(IPreferenceStore store) {
 		store.setDefault(LOADPATH_RUBYVMLIBRARY_LIST, getDefaultRubyVMLibraries());
 		store.setDefault(LOADPATH_RUBYVMLIBRARY_INDEX, 0); 
 		
 		store.setDefault(PreferenceConstants.EDITOR_SHOW_SEGMENTS, false);
 
 		// FIXME We can't enabling using code formatter yet, because it breaks
 		// on formatting templates (when inserting via content assist)
 		// FIXME Uncomment when we have an AST based formatter which spits out
 		// TextEdits (rather than one huge replace)
 		// store.setDefault(PreferenceConstants.TEMPLATES_USE_CODEFORMATTER,
 		// true);
 		
 		store.setDefault(PreferenceConstants.SEARCH_USE_REDUCED_MENU, false);
 
 		store.setDefault(PreferenceConstants.FORMATTER_PROFILE, ProfileManager.DEFAULT_PROFILE);
 
 		store.setDefault(PreferenceConstants.LINK_BROWSING_PROJECTS_TO_EDITOR, true);
 		store.setDefault(PreferenceConstants.LINK_BROWSING_TYPES_TO_EDITOR, true);
 		store.setDefault(PreferenceConstants.LINK_BROWSING_MEMBERS_TO_EDITOR, true);
 
 		// MembersOrderPreferencePage
 		store.setDefault(PreferenceConstants.APPEARANCE_MEMBER_SORT_ORDER, "T,SF,SM,F,C,M"); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.APPEARANCE_VISIBILITY_SORT_ORDER, "B,V,R"); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER, false);
		
		store.setDefault("MemberFilterActionGroup.org.rubypeople.rdt.ui.RubyOutlinePage.4", true);
 
 		// AppearancePreferencePage
 		store.setDefault(PreferenceConstants.APPEARANCE_COMPRESS_PACKAGE_NAMES, false);
 		store.setDefault(PreferenceConstants.APPEARANCE_PKG_NAME_PATTERN_FOR_PKG_VIEW, ""); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.BROWSING_STACK_VERTICALLY, false);
 
 		store.setDefault(PreferenceConstants.EDITOR_CORRECTION_INDICATION, true);
 		store.setDefault(PreferenceConstants.EDITOR_EVALUTE_TEMPORARY_PROBLEMS, true);
 
 //		 TypeFilterPreferencePage
 		store.setDefault(PreferenceConstants.TYPEFILTER_ENABLED, ""); //$NON-NLS-1$
 		
 		// folding
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_PROVIDER, "org.rubypeople.rdt.ui.text.defaultFoldingProvider"); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_RDOC, false);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_INNERTYPES, false);
 		store.setDefault(PreferenceConstants.EDITOR_FOLDING_METHODS, false);
 
 		store.setDefault(PreferenceConstants.EDITOR_SHOW_TEXT_HOVER_AFFORDANCE, true);
 
 		store.setDefault(PreferenceConstants.DEBUGGER_USE_RUBY_DEBUG, false);
 
 		store.setDefault(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);
 
 		// RubyEditorPreferencePage
 		store.setDefault(PreferenceConstants.EDITOR_MATCHING_BRACKETS, true);
 		PreferenceConverter.setDefault(store, PreferenceConstants.EDITOR_MATCHING_BRACKETS_COLOR, new RGB(192, 192, 192));
 
 		store.setDefault(PreferenceConstants.CODEASSIST_AUTOACTIVATION, true);
 		store.setDefault(PreferenceConstants.CODEASSIST_AUTOACTIVATION_DELAY, 200);
 
 		store.setDefault(PreferenceConstants.CODEASSIST_AUTOINSERT, true);
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_PROPOSALS_BACKGROUND, new RGB(255, 255, 255));
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_PROPOSALS_FOREGROUND, new RGB(0, 0, 0));
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_PARAMETERS_BACKGROUND, new RGB(255, 255, 255));
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_PARAMETERS_FOREGROUND, new RGB(0, 0, 0));
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_REPLACEMENT_BACKGROUND, new RGB(255, 255, 0));
 		PreferenceConverter.setDefault(store, PreferenceConstants.CODEASSIST_REPLACEMENT_FOREGROUND, new RGB(255, 0, 0));
 		store.setDefault(PreferenceConstants.CODEASSIST_INSERT_COMPLETION, true);
 		store.setDefault(PreferenceConstants.CODEASSIST_FILL_ARGUMENT_NAMES, true);
 		store.setDefault(PreferenceConstants.CODEASSIST_GUESS_METHOD_ARGUMENTS, false);
 		store.setDefault(PreferenceConstants.CODEASSIST_PREFIX_COMPLETION, false);
 
 		store.setDefault(PreferenceConstants.EDITOR_SUB_WORD_NAVIGATION, true);
 		store.setDefault(PreferenceConstants.EDITOR_CLOSE_STRINGS, true);
 		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACKETS, true);
 		store.setDefault(PreferenceConstants.EDITOR_CLOSE_BRACES, true);
 		store.setDefault(PreferenceConstants.EDITOR_END_STATEMENTS, true);
 
 		// mark occurrences
 		store.setDefault(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_STICKY_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_FIELD_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES, true);
 		store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS, true);
 		
 		int sourceHoverModifier= SWT.MOD2;
 		String sourceHoverModifierName= Action.findModifierString(sourceHoverModifier);	// Shift
 		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIERS, "org.rubypeople.rdt.ui.BestMatchHover;0;org.rubypeople.rdt.ui.RubySourceHover;" + sourceHoverModifierName); //$NON-NLS-1$
 		store.setDefault(PreferenceConstants.EDITOR_TEXT_HOVER_MODIFIER_MASKS, "org.rubypeople.rdt.ui.BestMatchHover;0;org.rubypeople.rdt.ui.RubySourceHover;" + sourceHoverModifier); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the RDT-UI preference store.
 	 * 
 	 * @return the RDT-UI preference store
 	 */
 	public static IPreferenceStore getPreferenceStore() {
 		return RubyPlugin.getDefault().getPreferenceStore();
 	}
 
 	public static ILoadpathEntry[] getDefaultRubyVMLibrary() {
 		IPreferenceStore store = RubyPlugin.getDefault().getPreferenceStore();
 
 		String str = store.getString(LOADPATH_RUBYVMLIBRARY_LIST);
 		int index = store.getInt(LOADPATH_RUBYVMLIBRARY_INDEX);
 
 		StringTokenizer tok = new StringTokenizer(str, ";"); //$NON-NLS-1$
 		while (tok.hasMoreTokens() && index > 0) {
 			tok.nextToken();
 			index--;
 		}
 
 		if (tok.hasMoreTokens()) {
 			ILoadpathEntry[] res = decodeRubyVMLibraryLoadpathEntries(tok.nextToken());
 			if (res.length > 0) {
 				return res;
 			}
 		}
 		return new ILoadpathEntry[] { getRubyVMContainerEntry() };
 	}
 	
 	private static ILoadpathEntry getRubyVMContainerEntry() {
 		return RubyCore.newContainerEntry(new Path("org.rubypeople.rdt.launching.RUBY_CONTAINER")); //$NON-NLS-1$
 	}
 	
 	public static ILoadpathEntry[] decodeRubyVMLibraryLoadpathEntries(String encoded) {
 		StringTokenizer tok= new StringTokenizer(encoded, " "); //$NON-NLS-1$
 		ArrayList res= new ArrayList();
 		while (tok.hasMoreTokens()) {
 			try {
 				tok.nextToken(); // desc: ignore
 				int kind= Integer.parseInt(tok.nextToken());
 				IPath path= decodePath(tok.nextToken());
 				boolean isExported= Boolean.valueOf(tok.nextToken()).booleanValue();
 				switch (kind) {
 					case ILoadpathEntry.CPE_SOURCE:
 						res.add(RubyCore.newSourceEntry(path));
 						break;
 					case ILoadpathEntry.CPE_LIBRARY:
 						res.add(RubyCore.newLibraryEntry(path, isExported));
 						break;
 					case ILoadpathEntry.CPE_VARIABLE:
 						res.add(RubyCore.newVariableEntry(path, isExported));
 						break;
 					case ILoadpathEntry.CPE_PROJECT:
 						res.add(RubyCore.newProjectEntry(path, isExported));
 						break;
 					case ILoadpathEntry.CPE_CONTAINER:
 						res.add(RubyCore.newContainerEntry(path, isExported));
 						break;
 				}								
 			} catch (NumberFormatException e) {
 				String message= PreferencesMessages.NewRubyProjectPreferencePage_error_decode; 
 				RubyPlugin.log(new Status(IStatus.ERROR, RubyUI.ID_PLUGIN, IStatus.ERROR, message, e));
 			} catch (NoSuchElementException e) {
 				String message= PreferencesMessages.NewRubyProjectPreferencePage_error_decode; 
 				RubyPlugin.log(new Status(IStatus.ERROR, RubyUI.ID_PLUGIN, IStatus.ERROR, message, e));
 			}
 		}
 		return (ILoadpathEntry[]) res.toArray(new ILoadpathEntry[res.size()]);	
 	}
 	
 	private static IPath decodePath(String str) {
 		if ("#".equals(str)) { //$NON-NLS-1$
 			return null;
 		} else if ("&".equals(str)) { //$NON-NLS-1$
 			return Path.EMPTY;
 		} else {
 			return Path.fromPortableString(decode(str));
 		}
 	}
 	
 	private static String decode(String str) {
 		try {
 			return URLDecoder.decode(str, fgDefaultEncoding);
 		} catch (UnsupportedEncodingException e) {
 			RubyPlugin.log(e);
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 }
