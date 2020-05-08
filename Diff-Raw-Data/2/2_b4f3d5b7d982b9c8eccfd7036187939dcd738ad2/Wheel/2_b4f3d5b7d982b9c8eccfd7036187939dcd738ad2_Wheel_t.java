 package modules;
 
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.jibble.pircbot.User;
 
 import panacea.MapFunction;
 import panacea.ReduceFunction;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 import static modules.Slap.slapUser;
 
 /**
  * Wheel
  *
  * @author Michael Mrozek
  *         Created Jun 18, 2009.
  */
 public class Wheel extends NoiseModule implements Serializable {
 	private Map<String, Integer> victims = new HashMap<String, Integer>();
 	
	@Command("\\.(?:wheel|spin)")
 	public void wheel(Message message) {
 		final String[] wheels = new String[] {
 			"justice", "misfortune", "fire", "blame", "doom" // THIS LIST MUST GROW!
 		};
 	
 		this.bot.sendMessage("Spin, Spin, Spin! the wheel of " + getRandom(wheels));
 		sleep(2);
 		
 		final User[] users = this.bot.getUsers();
 		String choice;
 		do {
 			choice = getRandom(users).getNick();
 		} while(choice.equals(this.bot.getNick()));
 		
 		this.victims.put(choice, (this.victims.containsKey(choice) ? this.victims.get(choice) : 0) + 1);
 		this.save();
 
 		this.bot.sendAction(slapUser(choice));
 	}
 	
 	@Command("\\.wheelstats")
 	public void wheelStats(Message message) {
 		if(victims.isEmpty()) {
 			this.bot.sendMessage("No victims yet");
 			return;
 		}
 		
 		final String[] nicks = victims.keySet().toArray(new String[0]);
 		Arrays.sort(nicks, new Comparator<String>() {
 			@Override public int compare(String s1, String s2) {
 				// Reversed to order max->min
 				return victims.get(s2).compareTo(victims.get(s1));
 			}
 		});
 		final int total = reduce(victims.values().toArray(new Integer[0]), new ReduceFunction<Integer, Integer>() {
 			@Override public Integer reduce(Integer source, Integer accum) {
 				return source + accum;
 			}
 		}, 0);
 		
 		this.bot.sendMessage(implode(map(nicks, new MapFunction<String, String>() {
 			@Override public String map(String nick) {
 				final int amt = victims.get(nick);
 				return String.format("(%2.2f%%) %s", ((double)amt/(double)total*100.0), nick);
 			}
 		}), ", "));
 	}
 	
 	@Override public String getFriendlyName() {return "Wheel";}
 	@Override public String getDescription() {return "Slaps a random user";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".wheel -- Choose a random user and slap them",
 				".wheelstats -- Display statistics"
 		};
 	}
 }
