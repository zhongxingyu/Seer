 package ch.sbs.plugin.preptools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrepToolLoader {
 
 	// Ordnungszahlen
 	public static final String ORDINAL_REGEX = "\\b\\d+\\.";
 
 	// Römische Zahlen
 	// case sensitive
 	public static final String ROMAN_REGEX = "\\b[IVXCMLD]+\\b\\.?";
 
 	// Zahl mit Masseinheit
 	// ignore case
 	public static final String MEASURE_REGEX = "(?i:\\d*['.,]*\\d+\\s*[A-Z]{1,2}\\b)";
 
 	// Grossbuchstaben(folgen) des Typs A, A-Z, MM, USA, A4
 	// case sensitive
 	public static final String ABBREV_CAPITAL_REGEX = "(\\b[A-ZÄÖÜ]+)(\\d*\\b)";
 	public static final String ABBREV_CAPITAL_REPLACE = "<abbr>$1</abbr>$2";
 
 	// Abkürzungen des Typs x.y. oder x. y.
 	// ignore case
 	public static final String ABBREV_PERIOD_REGEX = "\\b(?i:[A-ZÄÖÜ]{1,4}\\.\\s*[A-ZÄÖÜ]{1,4}\\.)";
 
 	// Akronyme des Typs GmbH, GSoA, etc.
 	// case sensitive
 	public static final String ABBREV_ACRONYM_REGEX = "\\b\\w*[a-z]+[A-Z]+\\w*\\b";
 
 	// http://redmine.sbszh.ch/issues/show/1203
 	public static final String PAGEBREAK_REGEX = "</p\\s*>\\s*(<pagenum\\s+id\\s*=\\s*\"page-\\d+\" page\\s*=\\s*\"normal\"\\s*>\\s*\\d+\\s*</pagenum\\s*>)\\s*<p\\s*>";
 	public static final String PAGEBREAK_REPLACE = " $1 ";
 
 	// http://redmine.sbszh.ch/issues/show/1201
 
 	public static final String PLACEHOLDER = "_____";
 	public static final String ACCENT_REGEX = "(?iu:(\\b\\w*[àâçéèêëìîïòôœùû]\\w*\\b))";
 	public static final String ACCENT_REPLACE = "<span brl:accents=\""
 			+ PLACEHOLDER + "\">$1</span>";
 
 	// TODO: preptools should load themselves.
 	// PrepToolLoader shouldn't know or care about specific tools.
 	public static List<PrepTool> loadPrepTools(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		final List<PrepTool> prepTools = new ArrayList<PrepTool>();
 		int i = 0;
 		prepTools.add(new VFormPrepTool(thePrepToolsPluginExtension, i++, 'o'));
 		prepTools
 				.add(new ParensPrepTool(thePrepToolsPluginExtension, i++, 's'));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'd',
 				"Ordinal", ORDINAL_REGEX, "brl:num role=\"ordinal\""));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'r',
 				"Roman", ROMAN_REGEX, "brl:num role=\"roman\""));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'u',
 				"Measure", MEASURE_REGEX, "brl:num role=\"measure\""));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'v',
 				"AbbrevPeriod", ABBREV_PERIOD_REGEX, "abbr"));
 		prepTools.add(new FullRegexPrepTool(thePrepToolsPluginExtension, i++,
 				't', "AbbrevCapital", ABBREV_CAPITAL_REGEX, "abbr",
 				ABBREV_CAPITAL_REPLACE));
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'y',
 				"Acronym", ABBREV_ACRONYM_REGEX, "abbr"));
 		prepTools.add(new FullRegexPrepTool(thePrepToolsPluginExtension, i++,
 				'k', "Pagebreak", PAGEBREAK_REGEX, null, PAGEBREAK_REPLACE));
 		prepTools.add(new AccentPrepTool(thePrepToolsPluginExtension, i++, 'a',
				"Accent", ACCENT_REGEX, "span", ACCENT_REPLACE));
 		return prepTools;
 	}
 }
