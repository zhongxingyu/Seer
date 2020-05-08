 package ee.ut.math.tvt.includeName;
 
 import org.apache.log4j.Logger;
 
 public class Intro {
 	private static final Logger log = Logger.getLogger(Intro.class);
 	
 	public static void main(String[] args){
 		/* TODO: In addition, you need to add corresponding message
 		* in the log that would indicate that intro window is opened.
 		* Use Log4J functionality for it. Log should have event time
 		* and name of the component (invocated class name)
 		*/
		log.info("Intro window opened");
 		IntroUI ui = new IntroUI();
 		ui.display();
 	}
 }
