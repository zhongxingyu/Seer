 package modules;
 
 import java.io.File;
 
 import eliza.ElizaMain;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 /**
  * Eliza
  *
  * @author Michael Mrozek
  *         Created Jan 1, 2011.
  */
 public class Eliza extends NoiseModule {
 	private static final File SCRIPT_FILE = new File("eliza-script");
 	
 	private ElizaMain eliza;
 	
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		this.eliza = new ElizaMain();
 		this.eliza.readScript(true, SCRIPT_FILE.getAbsolutePath());
 	}
 	
	@Command("([^:]+): (.*)")
 	public void eliza(Message message, String nick, String userMessage) {
 		if(!nick.equals(NoiseBot.me.getNick())) return;
 		this.bot.reply(message, this.eliza.processInput(userMessage));
 	}
 
 	@Override public String getFriendlyName() {return "Eliza";}
 	@Override public String getDescription() {return "Implementation of ELIZA";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				this.bot.getNick() + ": You are the best"
 		};
 	}
 	@Override public File[] getDependentFiles() {return new File[] {SCRIPT_FILE};}
 }
