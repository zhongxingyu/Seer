 import java.awt.event.ActionEvent;
 
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.util.ArrayList;
 import javax.swing.Timer;
 import org.pircbotx.PircBotX;
 
 
 public class Palebot {
 
 	private static boolean SHOULD_BE_CONNECTED;
	private static ArrayList<Long> messageTimes = new ArrayList<Long>();
 	
 
 	static PircBotX bot = new PircBotX();
 
 	public static void main(String[] args) throws Exception {
 
 		final Window window = new Window();
 		window.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				if (bot != null) {
 					if (bot.isConnected()) {
 						bot.disconnect();
 					}
 				}
 			}
 		});
 
 		ActionListener task = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				if(Palebot.shouldBeConnected()&&!bot.isConnected()){
 					
 						window.connect();
 						
 				}
 				
 				if(messageTimes.size()>0 && System.currentTimeMillis()-messageTimes.get(0)>30000)
 				{
 					messageTimes.remove(0);
 					System.out.println("Message Count: " + getMessageCount());
 				}
 			}
 		};
 
 		Timer timer = new Timer(1000, task); // fire every 1 seconds
 		timer.setRepeats(true);
 		timer.start();
 		
 		window.revalidate();
 
 	}
 	
 	public static int getMessageCount()
 	{
 		return messageTimes.size();
 	}
 	
 	public static void addMessageTime(long time)
 	{
 		messageTimes.add(time);
 	}
 
 	/**
 	 * @return the cONNECTED
 	 */
 	public static boolean shouldBeConnected() {
 		return SHOULD_BE_CONNECTED;
 	}
 
 	/**
 	 * @param cONNECTED
 	 *            the cONNECTED to set
 	 */
 	public static void setShouldBeConnected(boolean cONNECTED) {
 		SHOULD_BE_CONNECTED = cONNECTED;
 	}
 
 	public static PircBotX getBot() {
 		// TODO Auto-generated method stub
 		return bot;
 	}
 	
 	public static void sendMessage()
 	{
 		addMessageTime(System.currentTimeMillis());
 		System.out.println("Message sent at: " + System.currentTimeMillis());
 	}
 
 }
