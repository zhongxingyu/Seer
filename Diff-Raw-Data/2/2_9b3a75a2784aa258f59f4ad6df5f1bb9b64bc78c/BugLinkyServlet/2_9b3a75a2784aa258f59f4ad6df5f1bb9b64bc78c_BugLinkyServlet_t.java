 package buglinky;
 
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.wave.api.*;
 
 @SuppressWarnings("serial")
 public class BugLinkyServlet extends AbstractRobotServlet {
 	private static final String ME = "buglinky@appspot.com";
 	private static final String LINK = "link/manual";
 	private static final Logger log =
 		Logger.getLogger(BugLinkyServlet.class.getName());
 	private static final String BUG_URL =
 		"http://code.google.com/p/google-wave-resources/issues/detail?id=";
 
 	/**
 	 * Regex used to find bug numbers in the text. Note that we require at least
 	 * one non-numeric character after the bug number (and not a newline). This
 	 * ensures that when the user is adding text at the end of a paragraph, we
 	 * won't add any links until the user is safely outside the area that we
 	 * need to modify. Users making modifications inside of paragraphs will have
 	 * to live with minor glitches.
 	 */
 	private static final Pattern REGEX =
 		Pattern.compile("(?:bug|issue) #(\\d+)(?!\\d|\\r|\\n)");
 
 	/** Called when we receive events from the Wave server. */
 	@Override
 	public void processEvents(RobotMessageBundle bundle) {
 		if (bundle.wasSelfAdded())
 			addInstructionsToWave(bundle);
 		dispatchEvents(bundle);
 	}
 
 	/** Add an instruction blip to this wave if we were just added. */
 	private void addInstructionsToWave(RobotMessageBundle bundle) {
 		log.fine("Adding instructions to wavelet " +
 				bundle.getWavelet().getWaveletId());
 		Blip blip = bundle.getWavelet().appendBlip();
 		TextView textView = blip.getDocument();
 		textView.append("buglinky will attempt to link \"bug #NNN\" to a bug tracker.");
 	}
 
 	/** Dispatch events to the appropriate handler method. */
 	private void dispatchEvents(RobotMessageBundle bundle) {
 		for (Event e: bundle.getEvents()) {
			if (!e.getModifiedBy().equals(ME)) {
 				switch (e.getType()) {
 				// One or the other of these should be wired up in
 				// capabilities.xml.  If we use BLIP_SUBMITTED, we'll apply
 				// our links once the user clicks "Done".  If we use
 				// BLIP_VERSION_CHANGED, we'll apply our links in real time.
 				case BLIP_SUBMITTED:
 				case BLIP_VERSION_CHANGED:
 					addLinksToBlip(e.getBlip());
 					break;
 				}
 			}
 		}
 	}
 
 	/** Add links to the specified blip. */
 	private void addLinksToBlip(Blip blip) {
 		log.fine("Adding links to blip " + blip.getBlipId());
 		// Adapted from http://senikk.com/min-f%C3%B8rste-google-wave-robot,
 		// a robot which links to @names on Twitter.
 		TextView doc = blip.getDocument();
 		Matcher matcher = REGEX.matcher(doc.getText());
 		while (matcher.find()) {
 			log.fine("Found a link: " + matcher.group());
 			Range range = new Range(matcher.start(), matcher.end());
 			String url = BUG_URL.concat(matcher.group(1));
 			maybeAnnotate(doc, range, LINK, url);
 		}
 	}
 
 	/** Add an annotation if it isn't already present. */
 	private void maybeAnnotate(TextView doc, Range range, String name, String value) {
 		// If this annotation is already present, give up now.
 		for (Annotation annotation : doc.getAnnotations(range, name)) {
 			if (annotation.getRange().equals(range) &&
 					annotation.getValue().equals(value))
 				return;
 		}
 		
 		log.fine("Making new link to " + value);
 		doc.setAnnotation(range, name, value);
 	}
 }
