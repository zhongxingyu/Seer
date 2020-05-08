 package cc.explain.server.subtitle.composer;
 
 import cc.explain.server.subtitle.Subtitle;
 import cc.explain.server.subtitle.SubtitleElement;
 
 public class WebVTTComposer implements SubtitleComposer {
 
 	private static final String WEBVTT = "WEBVTT";
	public static final String NEW_LINE = "\r\n";
 	public static final String ARROW = " --> ";
 
 	public String compose(Subtitle subtitle) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(WEBVTT).append(NEW_LINE).append(NEW_LINE);
 		for (SubtitleElement element : subtitle.getSubtitleElements()) {
 			String webVVTElement = convertSubtitleElementToString(element);
 			builder.append(webVVTElement);
 		}
 		return builder.toString();
 	}
 
 	String convertSubtitleElementToString(SubtitleElement element) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(element.getId()).append(NEW_LINE);
 		builder.append(element.getStart()).append(ARROW)
 				.append(element.getEnd()).append(NEW_LINE);
 		builder.append(element.getText()).append(NEW_LINE).append(NEW_LINE);
 		return builder.toString();
 	}
 
 }
