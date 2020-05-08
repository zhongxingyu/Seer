 package ch.sbs.plugin.preptools;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class PrepToolLoader {
 
 	public static final String ORDINAL_REGEX = "\\d+\\.";
 
 	// ignore case
	public static final String ROMAN_REGEX = "\\b(?i:[IVXCMLD]+\\.)";
 
 	// ignore case
 	public static final String MEASURE_REGEX = "(?i:\\d*['.,]*\\d+\\s?[A-Z]{1,2}\\b)";
 
 	// ignore case
 	public static final String ABBREV_PERIOD_REGEX = "(?i:[A-ZÄÖÜ]\\.\\s?[A-ZÄÖÜ]\\.)";
 
 	// case sensitive
 	public static final String ABBREV_CAPITAL_REGEX = "(\\b[A-ZÄÖÜ]+)(\\d*\\b)";
 
 	// case sensitive
 	public static final String ABBREV_ACRONYM_REGEX = "\\b\\w*[a-z]+[A-Z]+\\w*\\b";
 
 	// TODO: preptools should load themselves.
 	// PrepToolLoader shouldn't know or care about specific tools.
 	public static List<PrepTool> loadPrepTools(
 			final PrepToolsPluginExtension thePrepToolsPluginExtension) {
 		final List<PrepTool> prepTools = new ArrayList<PrepTool>();
 		int i = 0;
 		prepTools.add(new VFormPrepTool(thePrepToolsPluginExtension, i++));
 		prepTools.add(new ParensPrepTool(thePrepToolsPluginExtension, i++));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'o',
 				"Ordinal", ORDINAL_REGEX, "brl:num role=\"ordinal\""));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'r',
 				"Roman", ROMAN_REGEX, "brl:num role=\"roman\""));
 
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'u',
 				"Measure", MEASURE_REGEX, "brl:num role=\"measure\""));

 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'a',
 				"AbbrevPeriod", ABBREV_PERIOD_REGEX, "abbr"));
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 't',
 		// FIXME: the replacement is more complicated:
 		// <abbr>$1</abbr>$2
 				"AbbrevCapital", ABBREV_CAPITAL_REGEX, "abbr"));
 		prepTools.add(new RegexPrepTool(thePrepToolsPluginExtension, i++, 'y',
 				"Acronym", ABBREV_ACRONYM_REGEX, "abbr"));
 		return prepTools;
 	}
 }
