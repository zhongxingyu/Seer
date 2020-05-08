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
 		DEVELOPERS(30,35),
 		CHAIR_THROW(10,20),
 		PHOTO(35,25);
 		
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
 	private long lastMessageTime = System.currentTimeMillis();
 	private static final int TRANQ_MODIFIER = 2;
 	
 	private String[] images = {
 			"http://i.imgur.com/epeB9WP.jpg",
 			"http://i.imgur.com/x0jREfE.jpg",
 			"http://i.imgur.com/yiiLJld.jpg",
 			"http://i.imgur.com/aDfcn6i.jpg",
 			"http://i.imgur.com/2lVBgiz.jpg",
 			"http://i.imgur.com/ensgPNg.jpg",
 			"http://i.imgur.com/2UQqdNP.jpg",
 			"http://i.imgur.com/t0UFsn2.jpg",
 			"http://i.imgur.com/dh7OnCF.jpg",
 			"http://i.imgur.com/RcDiM1z.jpg",
 			"http://i.imgur.com/jQAIKRG.jpg",
 			"http://i.imgur.com/Iisp8Ik.jpg",
 			"http://i.imgur.com/spkZrtO.jpg",
 			"http://i.imgur.com/IR1rtkG.jpg",
 			"http://i.imgur.com/ElbnaQU.png"
 	};
 	
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
		nextTime += (((getTimeSinceLastMessage() / 1000) / 60) * TRANQ_MODIFIER);	
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
 			double nextRage = 1.0;
 			while (random.nextDouble() < nextRage && rv.size() <= 4) {
 				rv.add("DEVELOPERS DEVELOPERS DEVELOPERS DEVELOPERS");
 				nextRage /= 2.0;
 			}
 			break;
 		case CHAIR_THROW:
 			rv.add("/me throws a chair at " + getRandNick());
 			break;
 		case PHOTO:
 			rv.add(getRandomPhotoLink());
 			break;
 		}
 		return rv;
 	}
 	
 	private String getRandomPhotoLink() {
 		String rv = "http://i.imgur.com/06HvL.gif";
 		rv = images[random.nextInt(images.length)];
 		return rv;
 	}
 	private String getRandNick() {
 		User[] nicks = bot.getUsers(channel);
 		return nicks[random.nextInt(nicks.length)].getNick();
 	}
 	
 	public long getTimeSinceLastMessage() {
 		return System.currentTimeMillis() - lastMessageTime;
 	}
 	@Override
 	public boolean processMessage(String channel, String sender, String login, String hostname, String message, PircBot callback) {
 		lastMessageTime = System.currentTimeMillis();
 		return false;
 	}
 }
