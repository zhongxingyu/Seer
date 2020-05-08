 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 import main.Message;
 import main.NoiseModule;
 
 /**
  * Kool-aid!
  *
  * @author auchter
  *         Created Dec 23ish, 2011.
  */
public class Koolaid extends NoiseModule {
    @Command(".*[oO][hH](,|\\.)* [nN][oO].*")
 	public void ohyeah(Message message) {
 		this.bot.sendMessage(RED + "OH YEAH!");
 	}
 	
 	@Override public String getFriendlyName() {return "Kool-aid";}
 	@Override public String getDescription() {return "Contributes meaningfully to the conversation when needed.";}
 	@Override public String[] getExamples() {
 		return new String[] {
          "oh, no"
 		};
 	}
 }
