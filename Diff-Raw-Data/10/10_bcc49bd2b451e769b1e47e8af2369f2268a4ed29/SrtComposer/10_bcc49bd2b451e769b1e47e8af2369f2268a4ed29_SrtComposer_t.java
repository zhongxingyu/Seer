 package cc.explain.server.subtitle.composer;
 
 import cc.explain.server.subtitle.Subtitle;
 import cc.explain.server.subtitle.SubtitleElement;
 import org.joda.time.LocalTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 public class SrtComposer implements SubtitleComposer {
 	
	private static final String CR = System.getProperty("line.separator");
 	private final DateTimeFormatter formatter = DateTimeFormat
 			.forPattern("HH:mm:ss,SSS");
 
 	public String compose(Subtitle subtitle) {
 		StringBuilder builder = new StringBuilder();
 		for (SubtitleElement element : subtitle.getSubtitleElements()) {
 			builder.append(element.getId()).append(CR);
 			String start = convertLocalTimeToString(element.getStart());
 			String end = convertLocalTimeToString(element.getEnd());
 			builder.append(start).append(" --> ").append(end).append(CR);
 			builder.append(element.getText()).append(CR);
 			builder.append(CR);
 		}
 		return builder.toString();
 	}
 
 	String convertLocalTimeToString(LocalTime localTime) {
 		return formatter.print(localTime);
 	}
 
 }
