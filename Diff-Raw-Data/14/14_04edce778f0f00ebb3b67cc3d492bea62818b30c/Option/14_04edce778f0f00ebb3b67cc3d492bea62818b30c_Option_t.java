 package org.w3c.tidy;
 
 import static org.w3c.tidy.Options.*;
 import static org.w3c.tidy.ParsePropertyImpl.*;
 
 import org.w3c.tidy.Options.AccessibilityLevels;
 import org.w3c.tidy.Options.AttrSortStrategy;
 import org.w3c.tidy.Options.Bool;
 import org.w3c.tidy.Options.ConfigCategory;
 import org.w3c.tidy.Options.DoctypeModes;
 import org.w3c.tidy.Options.DupAttrModes;
 import org.w3c.tidy.Options.LineEnding;
 import org.w3c.tidy.Options.OptionType;
 import org.w3c.tidy.Options.TriState;
 
 enum Option {
 	/** Unknown option! */
 	Unknown                 (MS, "unknown!",                    IN, 0,                     null,                null),
 	/** Indentation n spaces */
 	IndentSpaces            (PP, "indent-spaces",               IN, 2,                     INT,                 null),
 	/** Wrap margin */
 	WrapLen                 (PP, "wrap",                        IN, 68,                    INT,                 null),
 	/** Expand tabs to n spaces */
 	TabSize                 (PP, "tab-size",                    IN, 8,                     INT,                 null),
 	
 	/** In/out character encoding */
 	CharEncoding            (CE, "char-encoding",               IN, "ASCII",               CHAR_ENCODING,       null),
 	/** Input character encoding (if different) */
 	InCharEncoding          (CE, "input-encoding",              IN, "LATIN1",              CHAR_ENCODING,       null),
 	/** Output character encoding (if different) */
 	OutCharEncoding         (CE, "output-encoding",             IN, "ASCII",               CHAR_ENCODING,       null),
 	/** Output line ending (default to platform) */
 	Newline                 (CE, "newline",                     IN, LineEnding.Auto,       NEWLINE,             LineEnding.class),
 	
 	/** See doctype property */
 	DoctypeMode             (MU, "doctype-mode",                IN, DoctypeModes.Auto,     null,                DoctypeModes.class),
 	/** User specified doctype */
 	Doctype                 (MU, "doctype",                     ST, null,                  DOCTYPE,             DoctypeModes.class),
 	
 	/** Keep first or last duplicate attribute */
 	DuplicateAttrs          (MU, "repeated-attributes",         IN, DupAttrModes.KeepLast, REPEATED_ATTRIBUTES, DupAttrModes.class),
 	/** Default text for alt attribute */
 	AltText                 (MU, "alt-text",                    ST, null,                  STRING,              null),
 	
 	// obsolete
 	/** Style sheet for slides: not used for anything yet */
 	SlideStyle              (MS, "slide-style",                 ST, null,                  NAME,                null),
 	
 	/** File name to write errors to */
 	ErrFile                 (MS, "error-file",                  ST, null,                  STRING,              null),
 	/** File name to write markup to */
 	OutFile                 (MS, "output-file",                 ST, null,                  STRING,              null),
 	/** If true then output tidied markup */
 	WriteBack               (MS, "write-back",                  BL, false,                 BOOL,                Bool.class),
 	/** If false, normal output is suppressed */
 	ShowMarkup              (PP, "markup",                      BL, true,                  BOOL,                Bool.class),
 	/** However errors are always shown */
 	ShowWarnings            (DG, "show-warnings",               BL, true,                  BOOL,                Bool.class),
 	/** No 'Parsing X', guessed DTD or summary */
 	Quiet                   (MS, "quiet",                       BL, false,                 BOOL,                Bool.class),
 	/** Indent content of appropriate tags
 	 * "auto" does text/block level content indentation */
 	IndentContent           (PP, "indent",                      IN, TriState.No,           AUTOBOOL,            TriState.class),
 	/** Suppress optional end tags */
 	HideEndTags             (MU, "hide-endtags",                BL, false,                 BOOL,                Bool.class),
 	/** Treat input as XML */
 	XmlTags                 (MU, "input-xml",                   BL, false,                 BOOL,                Bool.class),
 	/** Create output as XML */
 	XmlOut                  (MU, "output-xml",                  BL, false,                 BOOL,                Bool.class),
 	/** Output extensible HTML */
 	XhtmlOut                (MU, "output-xhtml",                BL, false,                 BOOL,                Bool.class),
 	/** Output plain HTML, even for XHTML input.
 	 * Yes means set explicitly. */
 	HtmlOut                 (MU, "output-html",                 BL, false,                 BOOL,                Bool.class),
	/** Add <code>&lt;?xml?&gt;</code> for XML docs */
 	XmlDecl                 (MU, "add-xml-decl",                BL, false,                 BOOL,                Bool.class),
 	/** Output tags in upper not lower case */
 	UpperCaseTags           (MU, "uppercase-tags",              BL, false,                 BOOL,                Bool.class),
 	/** Output attributes in upper not lower case */
 	UpperCaseAttrs          (MU, "uppercase-attributes",        BL, false,                 BOOL,                Bool.class),
 	/** Make bare HTML: remove Microsoft cruft */
 	MakeBare                (MU, "bare",                        BL, false,                 BOOL,                Bool.class),
 	/** Replace presentational clutter by style rules */
 	MakeClean               (MU, "clean",                       BL, false,                 BOOL,                Bool.class),
 	/** Replace i by em and b by strong */
 	LogicalEmphasis         (MU, "logical-emphasis",            BL, false,                 BOOL,                Bool.class),
 	/** Discard proprietary attributes */
 	DropPropAttrs           (MU, "drop-proprietary-attributes", BL, false,                 BOOL,                Bool.class),
 	/** Discard presentation tags */
 	DropFontTags            (MU, "drop-font-tags",              BL, false,                 BOOL,                Bool.class),
 	/** Discard empty p elements */
 	DropEmptyParas          (MU, "drop-empty-paras",            BL, true,                  BOOL,                Bool.class),
 	/** Fix comments with adjacent hyphens */
 	FixComments             (MU, "fix-bad-comments",            BL, true,                  BOOL,                Bool.class),
	/** Output newline before &lt;br&gt; or not? */
 	BreakBeforeBR           (PP, "break-before-br",             BL, false,                 BOOL,                Bool.class),
 	
 	// obsolete
 	/** Create slides on each h2 element */
 	BurstSlides             (PP, "split",                       BL, false,                 BOOL,                Bool.class),
 	
 	/** Use numeric entities */
 	NumEntities             (MU, "numeric-entities",            BL, false,                 BOOL,                Bool.class),
 	/** Output " marks as &quot; */
 	QuoteMarks              (MU, "quote-marks",                 BL, false,                 BOOL,                Bool.class),
 	/** Output non-breaking space as entity */
 	QuoteNbsp               (MU, "quote-nbsp",                  BL, true,                  BOOL,                Bool.class),
 	/** Output naked ampersand as &amp; */
 	QuoteAmpersand          (MU, "quote-ampersand",             BL, true,                  BOOL,                Bool.class),
 	/** Wrap within attribute values */
 	WrapAttVals             (PP, "wrap-attributes",             BL, false,                 BOOL,                Bool.class),
 	/** Wrap within JavaScript string literals */
 	WrapScriptlets          (PP, "wrap-script-literals",        BL, false,                 BOOL,                Bool.class),
	/** Wrap within &lt;![ ... ]&gt; section tags */
 	WrapSection             (PP, "wrap-sections",               BL, true,                  BOOL,                Bool.class),
 	/** Wrap within ASP pseudo elements */
 	WrapAsp                 (PP, "wrap-asp",                    BL, true,                  BOOL,                Bool.class),
 	/** Wrap within JSTE pseudo elements */
 	WrapJste                (PP, "wrap-jste",                   BL, true,                  BOOL,                Bool.class),
 	/** Wrap within PHP pseudo elements */
 	WrapPhp                 (PP, "wrap-php",                    BL, true,                  BOOL,                Bool.class),
 	/** Fix URLs by replacing \ with / */
 	FixBackslash            (MU, "fix-backslash",               BL, true,                  BOOL,                Bool.class),
 	/** Newline+indent before each attribute */
 	IndentAttributes        (PP, "indent-attributes",           BL, false,                 BOOL,                Bool.class),
 	/** If set to yes PIs must end with ?> */
 	XmlPIs                  (MU, "assume-xml-procins",          BL, false,                 BOOL,                Bool.class),
 	/** If set to yes adds xml:space attr as needed */
 	XmlSpace                (MU, "add-xml-space",               BL, false,                 BOOL,                Bool.class),
 	/** If yes text at body is wrapped in P's */
 	EncloseBodyText         (MU, "enclose-text",                BL, false,                 BOOL,                Bool.class),
 	/** If yes text in blocks is wrapped in P's */
 	EncloseBlockText        (MU, "enclose-block-text",          BL, false,                 BOOL,                Bool.class),
 	/** If yes last modified time is preserved */
 	KeepFileTimes           (MS, "keep-time",                   BL, false,                 BOOL,                Bool.class),
 	/** Draconian cleaning for Word2000 */
 	Word2000                (MU, "word-2000",                   BL, false,                 BOOL,                Bool.class),
 	/** Add meta element indicating tidied doc */
 	Mark                    (MS, "tidy-mark",                   BL, true,                  BOOL,                Bool.class),
 	/** If true format error output for GNU Emacs */
 	Emacs                   (MS, "gnu-emacs",                   BL, false,                 BOOL,                Bool.class),
 	/** Name of current Emacs file */
 	EmacsFile               (MS, "gnu-emacs-file",              ST, null,                  STRING,              null),
 	/** If true attributes may use newlines */
 	LiteralAttribs          (MU, "literal-attributes",          BL, false,                 BOOL,                Bool.class),
 	/** Output BODY content only */
 	BodyOnly                (MU, "show-body-only",              IN, false,                 AUTOBOOL,            TriState.class),
 	/** Applies URI encoding if necessary */
 	FixUri                  (MU, "fix-uri",                     BL, true,                  BOOL,                Bool.class),
 	/** Folds known attribute values to lower case */
 	LowerLiterals           (MU, "lower-literals",              BL, true,                  BOOL,                Bool.class),
 	/** Hides all (real) comments in output */
 	HideComments            (MU, "hide-comments",               BL, false,                 BOOL,                Bool.class),
	/** Indent &lt;!CDATA[ ... ]]&gt; section */
 	IndentCdata             (MU, "indent-cdata",                BL, false,                 BOOL,                Bool.class),
 	/** Output document even if errors were found */
 	ForceOutput             (MS, "force-output",                BL, false,                 BOOL,                Bool.class),
 	/** Number of errors to put out */
 	ShowErrors              (DG, "show-errors",                 IN, 6,                     INT,                 null),
 	/** Convert quotes and dashes to nearest ASCII char */
 	AsciiChars              (CE, "ascii-chars",                 BL, false,                 BOOL,                Bool.class),
 	/** Join multiple class attributes */
 	JoinClasses             (MU, "join-classes",                BL, false,                 BOOL,                Bool.class),
 	/** Join multiple style attributes */
 	JoinStyles              (MU, "join-styles",                 BL, true,                  BOOL,                Bool.class),
	/** Replace &lt;![CDATA[]]&gt; sections with escaped text */
 	EscapeCdata             (MU, "escape-cdata",                BL, false,                 BOOL,                Bool.class),
 	
 	/** Language property: not used for anything yet */
 	Language                (CE, "language",                    ST, null,                  NAME,                null),
 	/** Allow numeric character references */
 	NCR                     (MU, "ncr",                         BL, true,                  BOOL,                Bool.class),
 	/** Output a Byte Order Mark (BOM) for UTF-16 encodings
 	 * auto: if input stream has BOM, we output a BOM */
 	OutputBOM               (CE, "output-bom",                  IN, TriState.Auto,         AUTOBOOL,            TriState.class),
 	
 	/** Replace hex color attribute values with names */
 	ReplaceColor            (MU, "replace-color",               BL, false,                 BOOL,                Bool.class),
 	/** CSS class naming for -clean option */
 	CSSPrefix               (MU, "css-prefix",                  ST, null,                  CSS1SELECTOR,        null),
 	
 	/** Declared inline tags */
 	InlineTags              (MU, "new-inline-tags",             ST, null,                  TAGNAMES,            null),
 	/** Declared block tags */
 	BlockTags               (MU, "new-blocklevel-tags",         ST, null,                  TAGNAMES,            null),
 	/** Declared empty tags */
 	EmptyTags               (MU, "new-empty-tags",              ST, null,                  TAGNAMES,            null),
 	/** Declared pre tags */
 	PreTags                 (MU, "new-pre-tags",                ST, null,                  TAGNAMES,            null),
 	
 	/** Accessibility check level
 	 * 0 (old style), or 1, 2, 3 */
 	AccessibilityCheckLevel (DG, "accessibility-check",         IN, 0,                     INT,                 AccessibilityLevels.class),
 	
 	/** degree to which markup is spread out vertically */
 	VertSpace               (PP, "vertical-space",              BL, false,                 BOOL,                Bool.class),
 	/** consider punctuation and breaking spaces for wrapping */
 	PunctWrap               (PP, "punctuation-wrap",            BL, false,                 BOOL,                Bool.class),
 	/** Merge multiple DIVs */
 	MergeDivs               (MU, "merge-divs",                  IN, TriState.Auto,         AUTOBOOL,            TriState.class),
 	/** Mark inferred UL elements with no indent CSS */
 	DecorateInferredUL      (MU, "decorate-inferred-ul",        BL, false,                 BOOL,                Bool.class),
 	/** Preserve entities */
 	PreserveEntities        (MU, "preserve-entities",           BL, false,                 BOOL,                Bool.class),
 	/** Sort attributes */
 	SortAttributes          (PP, "sort-attributes",             IN, AttrSortStrategy.None, SORTER,              AttrSortStrategy.class),
 	/** Merge multiple SPANs */
 	MergeSpans              (MU, "merge-spans",                 IN, TriState.Auto,         AUTOBOOL,            TriState.class),
 	/** Define anchors as name attributes */
 	AnchorAsName            (MU, "anchor-as-name",              BL, true,                  BOOL,                Bool.class);
 	
 	private Option(final ConfigCategory category, final String name, final OptionType type,
 			final Object dflt, final ParseProperty parser, final Class<? extends OptionEnum> enumClass) {
 		this.category = category;
 		this.name = name;
 		this.type = type;
 		this.dflt = dflt;
 		this.parser = parser;
 		this.pickList = new OptionValues(enumClass);
 	}
 	
 	private final ConfigCategory category;
 	private final String name;
 	private final OptionType type;
 	private final Object dflt;
 	private final ParseProperty parser;
 	private final OptionValues pickList;
 	
 	public ConfigCategory getCategory() {
 		return category;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public OptionType getType() {
 		return type;
 	}
 	
 	public Object getDflt() {
 		return dflt;
 	}
 	
 	public ParseProperty getParser() {
 		return parser;
 	}
 	
 	public OptionValues getPickList() {
 		return pickList;
 	}
 }
