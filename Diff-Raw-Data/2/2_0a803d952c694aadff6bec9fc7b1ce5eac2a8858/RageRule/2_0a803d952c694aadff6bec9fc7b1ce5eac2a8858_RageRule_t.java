 package rules;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.User;
 
 import ballmer.BallmerBot;
 
 
 public class RageRule implements BotRule {
 
 	private enum RageEnum {
 		DEVELOPERS(30,30),
 		CHAIR_THROW(10,20);
 		
 		private int min;
 		private int variable;
 		RageEnum(int min, int variable) {
 			this.min = min;
 			this.variable = variable;
 		}
 		
 		public int getMin() {
 			return min;
 		}
 		
 		public int getVariable() {
 			return variable;
 		}
 	}
 	
 	private BallmerBot bot;
 	private String channel;
 	private RageEnum currentRage;
 	private Random random;
 	
 	private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
 	private Runnable rageRunnable = new Runnable() {
 		public void run() {
 			rage();
 			scheduleNextRage();
 		};
 	};
 	
 	public RageRule(BallmerBot bot, String channel) {
 		this.bot = bot;
 		this.channel = channel;
 		random = new Random();
 		scheduleNextRage();
 	}
 	
 	
 	private void scheduleNextRage() {
 		currentRage = getRage();
 		int nextTime = random.nextInt(currentRage.getVariable()) + currentRage.getMin();
 		
 		executor.schedule(rageRunnable, nextTime, TimeUnit.MINUTES);
 		System.out.println("next rage scheduled in " + nextTime + " minutes with rage " + currentRage);
 	}
 	
 	private void rage() {
 		List<String> rageText = getRageTextFor(currentRage);
 		for (String rage : rageText) {
 			if (rage.startsWith("/me ")) {
 				rage = rage.substring(4);
 				bot.sendAction(channel, rage);
 			} else {
 				bot.sendMessage(channel, rage);
 			}
 		}
 	}
 	/**
 	 * Gets the next rage that ballmer should do.
 	 * @return
 	 */
 	private RageEnum getRage() {
 		RageEnum tempEnum;
 		do {
 			int next = random.nextInt(RageEnum.values().length);
 			tempEnum = RageEnum.values()[next];
 		} while (tempEnum == currentRage);
 		return tempEnum;
 	}
 	
 	private List<String> getRageTextFor(RageEnum rage) {
 		List<String> rv = new ArrayList<String>();
 		
 		switch(rage) {
 		case DEVELOPERS:
 			double chance = 1.0;
			while (chance > random.nextDouble() && rv.size() <= 4) {
 				rv.add("DEVELOPERS DEVELOPERS DEVELOPERS DEVELOPERS");
 				chance /= 2.0;
 			}
 			break;
 		case CHAIR_THROW:
 			rv.add("/me throws a chair at " + getRandNick());
 			break;
 		}
 		return rv;
 	}
 	
 	private String getRandNick() {
 		User[] nicks = bot.getUsers(channel);
 		return nicks[random.nextInt(nicks.length)].getNick();
 	}
 	@Override
 	public boolean processMessage(String channel, String sender, String login, String hostname, String message, PircBot callback) {
 		return false;
 	}
 }
