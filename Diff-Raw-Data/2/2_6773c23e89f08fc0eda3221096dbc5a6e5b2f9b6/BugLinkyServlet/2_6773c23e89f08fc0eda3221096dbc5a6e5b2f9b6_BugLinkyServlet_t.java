 // buglinky - A robot for adding bugtracker links to a wave
 // Copyright 2009 Eric Kidd
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package buglinky;
 
 import java.util.ArrayList;
 import java.util.logging.Logger;
 
 import com.google.wave.api.AbstractRobotServlet;
 import com.google.wave.api.Blip;
 import com.google.wave.api.ElementType;
 import com.google.wave.api.Event;
 import com.google.wave.api.EventType;
 import com.google.wave.api.FormElement;
 import com.google.wave.api.FormView;
 import com.google.wave.api.RobotMessageBundle;
 import com.google.wave.api.TextView;
 import com.google.wave.api.Wavelet;
 
 /** Called via JSON-RPC whenever an event occurs on one of our waves. */
 @SuppressWarnings("serial")
 public class BugLinkyServlet extends AbstractRobotServlet {
 	private static final Logger LOG =
 		Logger.getLogger(BugLinkyServlet.class.getName());
 	
 	/**
 	 * The name of this application on Google App Engine.  This is used
 	 * to ignore our own edits, and as our default profile name.
 	 * 
 	 * BE SURE TO UPDATE THIS FOR YOUR BOT! If you don't update this
 	 * correctly, your bot will go into an infinite loop and generate
 	 * dozens of useless messages per second.
 	 */
 	static final String APP_NAME = "buglinky";
 	
 	/** The wave address for this bot.  Used to ignore our own edits. */
 	private static final String BOT_ADDRESS = APP_NAME + "@appspot.com";
 
 	/** The instructions to display when we join a wave. */
 	private static final String INSTRUCTIONS =
 		"buglinky will attempt to link \"issue #NNN\" to the Wave issue " +
 		"tracker.\n\n" +
 		"Note that the issue number must not be at the very end of " +
 		"a paragraph. This is temporary kludge to discourage buglinky from " +
 		"annotating your insertion point as you type.\n\n" +
 		"Once you've set your preferences, you can delete this blip.";
 
 	/** The URL to a specific bug in our bug tracker, minus the number. */
 	private static final String BUG_URL =
 		"http://code.google.com/p/google-wave-resources/issues/detail?id=";
 	
 	@Override
 	public void processEvents(RobotMessageBundle bundle) {
 		if (bundle.wasSelfAdded())
 			addInstructionsToWave(bundle);
 		processButtonClicks(bundle);
 		processBlips(bundle, getBugUrl(bundle));
 	}
 
 	/** Add an instruction blip to this wave if we were just added. */
 	private void addInstructionsToWave(RobotMessageBundle bundle) {
 		Wavelet wavelet = bundle.getWavelet();
 		LOG.fine("Adding instructions to wavelet " + wavelet.getWaveletId());
 		Blip blip = wavelet.appendBlip();
 		TextView textView = blip.getDocument();
 		textView.append(INSTRUCTIONS);
 		
 		// Our form-handling code is heavily inspired by the original
 		// "Polly the Pollster" bot.
 		textView.append("\n\n");
 		textView.appendElement(new FormElement(ElementType.LABEL,
 				"bugUrlLabel",
 				"Enter your issue URL, minus the issue number:"));
 		textView.appendElement(new FormElement(ElementType.INPUT,
				"bugUrl", getBugUrl(bundle)));
 		textView.append("\n");
 		textView.appendElement(new FormElement(ElementType.BUTTON,
 				"saveButton", "Save Preferences"));
 		textView.setAnnotation("buglinky-admin", "");
 	}
 
 	private void processButtonClicks(RobotMessageBundle bundle) {
 		for (Event e : bundle.getEvents()) {
 			if (e.getType() == EventType.FORM_BUTTON_CLICKED) {
 				LOG.fine("Form button clicked");
 				TextView doc = e.getBlip().getDocument();
 				if (doc.hasAnnotation("buglinky-admin") &&
 						e.getButtonName().equals("saveButton")) {
 					LOG.fine("Buglinky save button clicked");
 					FormView form = doc.getFormView();
 					String newUrl = form.getFormElement("bugUrl").getValue();
 					if (!newUrl.matches("^ *$")) {
 						LOG.fine("Setting issue URL to " + newUrl);
 						e.getWavelet().setDataDocument("buglinky-url", newUrl);
 					}
 				}
 			}
 		}
 	}
 
 	private String getBugUrl(RobotMessageBundle bundle) {
 		String bugUrl = bundle.getWavelet().getDataDocument("buglinky-url");
 		if (bugUrl == null)
 			bugUrl = BUG_URL;
 		LOG.fine("Using issue URL " + bugUrl);
 		return bugUrl;
 	}
 
 	/** Process any blips which have changed. */
 	private void processBlips(RobotMessageBundle bundle, String bugUrl) {
 		// We clean up URLs first, so that we can annotate the newly-created
 		// text in the second pass.
 		ArrayList<BlipProcessor> processors = new ArrayList<BlipProcessor>();
 		processors.add(new BugUrlReplacer(bugUrl)); 
 		processors.add(new BugNumberLinker(bugUrl)); 
 		BlipProcessor.applyProcessorsToChangedBlips(processors, bundle,
 				BOT_ADDRESS);		
 	}
 }
