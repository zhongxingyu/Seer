 package org.codefaces.ui.codeLanguages;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Pattern;
 
 public class CodeLanguage {
 	public static final CodeLanguage PLAIN_TEXT = new CodeLanguage(
			"no-highlight", "no-highlight", "no-highlight", new String[0]);
 
 	private static final String FILE_EXTENSION_PATTERN = "*.";
 
 	private static final String FILE_EXTENSION_REGEXP = ".*.";
 
 	private final List<Pattern> filePatternsRegexps = new ArrayList<Pattern>();
 
 	private final String id;
 
 	private final String name;
 
 	private final String resource;
 
 	CodeLanguage(String id, String name, String resource,
 			String... filePatterns) {
 		this.id = id;
 		this.name = name;
 		this.resource = resource;
 
 		Set<String> uniqueFilePatterns = new HashSet<String>(Arrays
 				.asList(filePatterns));
 		for (String filePattern : uniqueFilePatterns) {
 			Pattern filePatternRegexp = createFileExtensionRegexp(filePattern);
 			filePatternsRegexps.add(filePatternRegexp);
 		}
 	}
 
 	private Pattern createFileExtensionRegexp(String filePattern) {
 		if (filePattern.startsWith(FILE_EXTENSION_PATTERN)) {
 			String extension = filePattern.substring(FILE_EXTENSION_PATTERN
 					.length());
 			return Pattern.compile(FILE_EXTENSION_REGEXP + extension);
 		}
 
 		return Pattern.compile(filePattern, Pattern.CASE_INSENSITIVE);
 	}
 
 	Collection<Pattern> getFilePatternsRegexps() {
 		return filePatternsRegexps;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getResource() {
 		return resource;
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		CodeLanguage other = (CodeLanguage) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	public boolean matchesFileName(String fileName) {
 		for (Pattern filePatternRegexp : getFilePatternsRegexps()) {
 			if (filePatternRegexp.matcher(fileName).matches()) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 }
