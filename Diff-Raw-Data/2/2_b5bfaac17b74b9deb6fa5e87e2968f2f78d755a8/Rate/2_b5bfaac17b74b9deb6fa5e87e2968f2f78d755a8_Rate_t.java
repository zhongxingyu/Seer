 package modules;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import panacea.ReduceFunction;
 
 import main.Message;
 import main.NoiseBot;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 /**
  * Rate
  *
  * @author Michael Mrozek
  *         Created Jun 21, 2009.
  */
 public class Rate extends NoiseModule {
 	private long start;
 	private Map<String, Integer> counter = new HashMap<String, Integer>();
 	
 	@Override public void init(NoiseBot bot) {
 		super.init(bot);
 		this.start	= System.currentTimeMillis();
 	}
 	
 	@Override public void processMessage(Message message) {
 		super.processMessage(message);
 		
 		final String nick = message.getSender();
 		this.counter.put(nick, (this.counter.containsKey(nick) ? this.counter.get(nick) : 0) + 1);
 	}
 
 	private String tohms(long sec)
 	{
		return String.format("%d:%02d:%02d", sec/(60*60), (sec/60)%60, sec%60);
 	}
 	
 	@Command("\\.rate")
 	public void general(Message message) {
 		final int numMessages = reduce(this.counter.values().toArray(new Integer[0]), new ReduceFunction<Integer, Integer>() {
 			@Override public Integer reduce(Integer source, Integer accum) {
 				return source + accum;
 			}
 		}, 0);
 		final long secondsElapsed = (System.currentTimeMillis() - this.start) / 1000;
 		final double messagesPerMinute = ((double)numMessages / (double)secondsElapsed) * 60;
 		this.bot.reply(message, numMessages + " messages in " + tohms(secondsElapsed) + " seconds = " + round(messagesPerMinute, 2) + " messages per minute");
 	}
 	
 	@Command("\\.rate (.+)")
 	public void specific(Message message, String nick) {
 		if(nick.equals(this.bot.getNick())) {
 			this.bot.reply(message, "I do not record my own messages");
 			return;
 		}
 		
 		final int numMessages = this.counter.containsKey(nick) ? this.counter.get(nick) : 0;
 		final long secondsElapsed = (System.currentTimeMillis() - this.start) / 1000;
 		final double messagesPerMinute = ((double)numMessages / (double)secondsElapsed) * 60;
 		this.bot.reply(message, numMessages + " messages in " + tohms(secondsElapsed) + " seconds = " + round(messagesPerMinute, 2) + " messages per minute");
 	}
 	
 	@Override public String getFriendlyName() {return "Rate";}
 	@Override public String getDescription() {return "Measures how often people speak";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".rate -- Display how often anyone speaks in the channel",
 				".rate _nick_ -- Display how often _nick_ speaks in the channel"
 		};
 	}
 }
