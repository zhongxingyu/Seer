 package jbot;
 
 import org.pircbotx.PircBotX;
 import org.pircbotx.cap.SASLCapHandler;
 
 public class Epic_Jbot {
 	public static final PircBotX mecha_jdog = new PircBotX();
 	
 	
 	
 	public static void main(String[] args) throws Exception {
 		
 		
 		mecha_jdog.setName("epic_jbot");
 		mecha_jdog.setCapEnabled(true);
 		mecha_jdog.setVerbose(true);
 		mecha_jdog.setAutoSplitMessage(true);
		mecha_jdog.getCapHandlers().add(new SASLCapHandler("epic_jbot", "250127"));
 		mecha_jdog.setLogin("epic_jbot");
 		mecha_jdog.getListenerManager().addListener(new BasicListen(mecha_jdog));
 		mecha_jdog.getListenerManager().addListener(new MessageEventHandler(mecha_jdog));
 		
 		
 		try {
 			mecha_jdog.connect("irc.esper.net");
 			//mecha_jdog.connect("irc.seion.us");
 			mecha_jdog.joinChannel("#epic_jdog");
 			mecha_jdog.setAutoNickChange(false);
 			
 			
 
 		} catch (Exception ex) {
 			ex.printStackTrace();
 			System.out.println("IT DONE GOOFT ON JOINING!");
 
 		}
 
 	}
 
 }
