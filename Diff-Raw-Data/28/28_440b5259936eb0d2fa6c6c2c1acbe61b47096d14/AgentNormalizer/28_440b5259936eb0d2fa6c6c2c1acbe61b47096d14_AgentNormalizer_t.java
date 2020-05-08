 package eu.europeana.uim.enrichment.normalizer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 public final class AgentNormalizer {
 
 	@SuppressWarnings("rawtypes")
 	public static List<String> normalize(Object input) {
 		List<String> normalized = new ArrayList<String>();
 		if (input.getClass().isAssignableFrom(String.class)) {
			normalized.addAll(normalizeInternal((String) input));
		} else if (input.getClass().isAssignableFrom(ArrayList.class)) {
			for (Object str : ((List) input)) {
				normalized.addAll(normalizeInternal((String) str));
 			}
 		}

 		return normalized;
 	}

	private static List<String> normalizeInternal(String input) {
 		List<String> normalized = new ArrayList<String>();
		String str = input;
 		if (str.contains("[")) {
 			str = StringUtils.substringBefore(str, "[").trim();
 		}
 		if (str.contains("(")) {
 			str = StringUtils.substringBefore(str, "(").trim();
 		}
 		if (str.contains(";")) {
 			str = StringUtils.substringBefore(str, ";").trim();
 		}
 		if (str.contains(",")) {
 			String[] split = str.split(",");
			if (split.length > 0) {
				normalized.add(split[0].trim() + " " + split[1].trim());
				normalized.add(split[1].trim() + " " + split[0].trim());
			} else {
				normalized.add(str);
			}
 		} else {
 			normalized.add(str);
 		}
 		return normalized;
 	}
 }
