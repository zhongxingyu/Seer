 package org.w3c.tidy;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeMap;
 
 public class Options {
 	static final String[] EMPTY = new String[0];
 	
 	static interface OptionEnum {
 		String getName();
 		String[] getSynonyms();
 	}
 	
 	static enum Bool implements OptionEnum {
 		No("no", "false", "n", "f", "0"),
 		Yes("yes", "true", "y", "t", "1");
 		
 		private final String name;
 		private final String[] synonyms;
 
 		private Bool(final String name, final String... synonyms) {
 			this.name = name;
 			this.synonyms = synonyms;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return synonyms;
 		}
 	}
 	
 	static enum TriState implements OptionEnum {
 		No("no", "false", "n", "f", "0"),
 		Yes("yes", "true", "y", "t", "1"),
 		/** Automatic */
 		Auto("auto", "a");
 		
 		private final String name;
 		private final String[] synonyms;
 
 		private TriState(final String name, final String... synonyms) {
 			this.name = name;
 			this.synonyms = synonyms;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return synonyms;
 		}
 	}
 	
 	static enum DupAttrModes implements OptionEnum {
 		KeepFirst("keep-first"),
 		KeepLast("keep-last");
 		
 		private final String name;
 
 		private DupAttrModes(final String name) {
 			this.name = name;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return EMPTY;
 		}
 	}
 	
 	static enum AccessibilityLevels implements OptionEnum {
 		Level0("0 (Tidy Classic)"),
 		Level1("1 (Priority 1 Checks)"),
 		Level2("2 (Priority 2 Checks)"),
 		Level3("3 (Priority 3 Checks)");
 		
 		private final String name;
 
 		private AccessibilityLevels(final String name) {
 			this.name = name;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return EMPTY;
 		}
 	}
 	
 	static enum LineEnding implements OptionEnum {
 		/** Use Unix style: LF */
 		LF("LF", "\n"),
 		/** Use DOS/Windows style: CR+LF */
 		CRLF("CRLF", "\r\n"),
 		/** Use Macintosh style: CR */
 		CR("CR", "\r"),
 		/** System default */
 		Auto("auto", System.getProperty("line.separator"));
 		
 		private final String name;
 		private final String value;
 
 		private LineEnding(final String name, final String value) {
 			this.name = name;
 			this.value = value;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return EMPTY;
 		}
 		
 		public String getValue() {
 			return value;
 		}
 	}
 	
 	static enum DoctypeModes implements OptionEnum {
 		/** Omit DOCTYPE altogether */
 		Omit("omit"),
 		/** Keep DOCTYPE in input.  Set version to content */
 		Auto("auto"),
 		/** Convert document to HTML 4 strict content model */
 		Strict("strict"),
 		/** Convert document to HTML 4 transitional content model */
 		Loose("transitional", "loose"),
 		/** Set DOCTYPE FPI explicitly */
 		User("user");
 		
 		private final String name;
 		private final String[] synonyms;
 
 		private DoctypeModes(final String name, final String... synonyms) {
 			this.name = name;
 			this.synonyms = synonyms;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return synonyms;
 		}
 	}
 	
 	static enum AttrSortStrategy implements OptionEnum {
 		None("none"),
 		Alpha("alpha");
 		
 		private final String name;
 
 		private AttrSortStrategy(final String name) {
 			this.name = name;
 		}
 		
 		public String getName() {
 			return name;
 		}
 		
 		public String[] getSynonyms() {
 			return EMPTY;
 		}
 	}
 	
 	enum ConfigCategory {
 		/** Markup options: (X)HTML version, etc */
 		Markup,
 		
 		/** Diagnostics */
 		Diagnostics,
 		
 		/** Output layout */
 		PrettyPrint,
 		
 		/** Character encodings */
 		Encoding,
 		
 		/** File handling, message format, etc. */
 		Miscellaneous
 	}
 	
 	static final ConfigCategory MU = ConfigCategory.Markup;
 	static final ConfigCategory DG = ConfigCategory.Diagnostics;
 	static final ConfigCategory PP = ConfigCategory.PrettyPrint;
 	static final ConfigCategory CE = ConfigCategory.Encoding;
 	static final ConfigCategory MS = ConfigCategory.Miscellaneous;
 
 	enum OptionType {
 		String,
 		Integer,
 		Boolean;
 	}
 
 	static final OptionType IN = OptionType.Integer;
 	static final OptionType BL = OptionType.Boolean;
 	static final OptionType ST = OptionType.String;
 	
 	private static final Map<String, Option> OPTIONS = new TreeMap<String, Option>();
 	
	static void buildOptionsMap() {
 		for (Option o : Option.values()) {
 			OPTIONS.put(o.getName(), o);
 		}
 	}
 	
 	static Option getOption(final String name) {
 		return OPTIONS.get(name);
 	}
 	
 	static Collection<Option> getOptions() {
 		return OPTIONS.values();
 	}
 }
