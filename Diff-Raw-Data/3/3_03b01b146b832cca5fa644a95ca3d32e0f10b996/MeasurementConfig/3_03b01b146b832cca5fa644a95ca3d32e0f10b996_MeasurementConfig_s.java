 package applicationmeasurement;
 
 import java.awt.Dimension;
 
 /**
  * This class contains project wide configuration settings.
  * @author Roland van der Linden
  *
  */
 public class MeasurementConfig
 {
 	public final static String appname = "Affect Based Speech Synthesis";
 	public final static Dimension appsize = new Dimension(1000, 750);
 	public final static int outerBorderSize = 2;
 	
 	public final static String freeTTSSpeakerName = "kevin16"; //Existing voices: alan, kevin8, kevin16
 	
 	public final static long waitTimeForNextTest = 1000;
 	
 	public final static String standardExperimentText = "Hello there. Try to guess my affective state. I get a new random affective state each time you continue or skip.";
 	public final static String explanatoryText = 
 			"Dear participant,\n"
 			+ "We would like you to listen to the affective state (the emotion) of the synthetic voice, and then indicate which affective state you recognized by selecting the associated facial expression on the affectbutton."
 			+ "Please do not base your selection on the emotion contained in the words, but base it on the emotion contained in the speech itself.\n\n"
 			+ "When you press the 'Save & Continue' button, we will automatically save your result to a file. In order to process your results, we would like you to send this file to us when you are done."
 			+ "The file can be found in the location where you are running the program from. The filename is '[TODO INSERT FILENAME]'. Please send this file to '[TODO INSERT EMAILADDRESS]'.\n\n"
 			+ "Some remarks to help you get started:\n"
 			+ " - Press the 'Start' button to start with the first test.\n"
 			+ " - Move over the affectbutton (the one with a face on it) to change the facial expression of the face. Left-mouse press on the affectbutton to record your selection. Pressing it again overwrites your selection.\n"
 			+ " - Press the 'Save & Continue' button if you think the selection fits with the affective state of the voice.\n"
 			+ " - Press the 'Repeat test' button if you would like to hear the same voice again.\n"
 			+ " - Press the 'Compare with neutral' button if you would like to hear a (synthetic) affective neutral voice.\n"
 			+ " - Press the 'Skip (I don't know)' button if you cannot make an accurate selection at all. Use only when you are clueless.\n"
			+ " - You can enter your own text in the textfield below if you prefer a change of topic (not necessary).\n";
 }
