 package com.google.wave.extensions.emaily.robot;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.wave.api.AbstractRobotServlet;
 import com.google.wave.api.Annotation;
 import com.google.wave.api.Blip;
 import com.google.wave.api.Gadget;
 import com.google.wave.api.RobotMessageBundle;
 import com.google.wave.api.TextView;
 import com.google.wave.api.Wavelet;
 
 @Singleton
 public class EmailyRobotServlet extends AbstractRobotServlet {
 	private static final long serialVersionUID = 8878209094937861353L;
 	
 	/**
 	 * Handles all robot events.
 	 */
 	@Override
 	public void processEvents(RobotMessageBundle bundle) {
 		if (bundle.wasSelfAdded()) {
 			handleSelfAdded(bundle);
 		}
 	}
 
 	/**
 	 * Handles case when the robot is added to the wave. 
 	 * @param bundle All information from the robot event.
 	 */
 	private void handleSelfAdded(RobotMessageBundle bundle) {
 		Wavelet wavelet = bundle.getWavelet();
 		Blip blip = wavelet.getRootBlip();
 		TextView view = blip.getDocument();
 		// Find position for the gadget: Right after the conversation title if any
 		// TODO(dlux): If there is no subject, then keep some space for that.
 		int gadget_position = 0;
 		for (Annotation a: view.getAnnotations("conv/title")) {
 			if (a.getRange().getEnd() > gadget_position) {
 				gadget_position = a.getRange().getEnd();
 			}
 		}
 		// TODO(dlux): Use bundle.getRobotAddress when it is working.
 		view.insertElement(gadget_position, new Gadget("http://2.latest.emaily-wave.appspot.com/gadgets/target-selection-gadget.xml"));
 		debugHelper.DumpWaveletState(wavelet);
 	}
 
 	// Injected dependencies
	privte DebugHelper debugHelper;
 	
 	@Inject
 	public void setDebugHelper(DebugHelper debugHelper) {
 		this.debugHelper = debugHelper;
 	}
 }
