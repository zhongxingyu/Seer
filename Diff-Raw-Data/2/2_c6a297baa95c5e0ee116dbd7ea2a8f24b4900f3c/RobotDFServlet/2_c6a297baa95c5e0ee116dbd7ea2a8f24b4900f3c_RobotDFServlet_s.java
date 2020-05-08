 package com.github.robotdf;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.google.wave.api.AbstractRobotServlet;
 import com.google.wave.api.Blip;
 import com.google.wave.api.Event;
 import com.google.wave.api.EventType;
 import com.google.wave.api.Range;
 import com.google.wave.api.RobotMessageBundle;
 import com.google.wave.api.TextView;
 import com.google.wave.api.Wavelet;
 
 @SuppressWarnings("serial")
 public class RobotDFServlet extends AbstractRobotServlet {
 
 	private final static String VERSION = "1";
 	
	Pattern pattern = Pattern.compile("owl:sameAs\\[\\.+\\]");
 
 	@Override
 	public void processEvents(RobotMessageBundle bundle) {
 		Wavelet wavelet = bundle.getWavelet();
         
 	    if (bundle.wasSelfAdded()) {
 	      Blip blip = wavelet.appendBlip();
 	      TextView textView = blip.getDocument();
 	      textView.appendMarkup("RobotDF v" + VERSION + ".<br />");
 	    }
 	            
 	    for (Event e: bundle.getEvents()) {
 	      if (e.getType() == EventType.BLIP_SUBMITTED ||
 	    	  e.getType() == EventType.BLIP_VERSION_CHANGED) {
 	    	  Blip blip = e.getBlip();
 	    	  if (!blip.getCreator().equals("RobotDF")) {
 	    		  TextView textView = blip.getDocument();
 	    		  // apply all known commands
 	    		  owlSameAs(textView);
 	    	  }
 	      }
 	    }
 	}
 
 	private void owlSameAs(TextView textView) {
 		while (true) {
 			Matcher matcher = pattern.matcher(textView.getText());
 			if (matcher.find()) {
 				String match = matcher.group();
 				int start = matcher.start();
 				int end = matcher.end();
 
 				String replacement = match.substring("owl:sameAs[".length()).trim();
 				if (replacement != null && replacement.length() > 0) {
 					textView.replace(
 						new Range(start, end),
 						replacement
 					);
 //					textView.setAnnotation(new Range(start, start+replacement.length()), "chem/molForm", replacement);
 				}
 			} else {
 				// OK, nothing more found, so return
 				return;
 			}
 		}
 	}
 
 }
