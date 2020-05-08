 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 public class Christmas extends NoiseModule {
 	private static String[] lyrics = {
 		"Deck the halls with boughs of holly,~Fa la la la la la, la la la la.~Tis the season to be jolly,~Fa la la la la la, la la la la.",
 		"Frosty the snowman was a jolly happy soul~With a corncob pipe and a button nose~and two eyes made out of coal~Frosty the snowman is a fairy tale they say~He was made of snow but the children~know how he came to life one day",
 		"Have yourself a merry little Christmas.~Let your heart be light,~From now on our troubles~Will be out of sight.",
 		"Jingle bells~jingle bells~jingle all the way!~O what fun it is to ride~In a one-horse open sleigh",
 		"Rudolph the Red-Nosed Reindeer~Had a very shiny nose,~And if you ever saw it,~You would even say it glows.",
 		"Ma-ma-ma-ma-cita, donde esta Santa Cleese...~The vato wit da bony knees...~He comin' down da street wit no choos on his feet...~And he's going to...",
 		"On the fourth day of Xmas I stole from that lady...~Four family photos~Three jars of pennies~Two former husbands~And a slipper on a shoe tree!",
		"He knows when you are sleeping~He knows when you're on the can~He'll hunt you down and blast your ass from here to Pakistan~Oh, you'd better not breathe, you'd better not move~You're better off dead, I'm telling you, dude~Santa Claus is gunning you down",
 	};
 
 	private boolean odd = false;
 
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		talked(null);
 	}
 
 	@Command("[^\\.].*")
 	public void talked(Message message) {
 		if (getRandomInt(0, 10) == 0) {
 			if (odd) {
 				this.bot.sendMessage(RED + "Ho " + GREEN + "Ho " + RED + "Ho!");
 			} else {
 				this.bot.sendMessage(GREEN + "Ho " + RED + "Ho " + GREEN + "Ho!");
 			}
 			odd = !odd;
 		}
 	}
 
 	@Command("\\.jingle")
 	public void jingle(Message message) {
 		String song = getRandom(lyrics);
 		for(String line : song.split("~")) {
 			this.bot.sendMessage(BLUE + line);
 		}
 	}
 
 	@Override public String getFriendlyName() {return "Christmas";}
 	@Override public String getDescription() {return "Says `Ho Ho Ho' a lot, and maybe other stuff";}
 	@Override public String[] getExamples() {
 		return new String[] {
 		};
 	}
 }
