 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.Vector;
 
 import debugging.Log;
 
 import au.com.bytecode.opencsv.CSVParser;
 
 import main.Message;
 import main.NoiseModule;
 
 import static panacea.Panacea.*;
 
 /**
  * Poll
  *
  * @author Michael Mrozek
  *         Created Jun 16, 2009.
  */
 public class Poll extends NoiseModule {
 	private static final int WAIT_TIME = 180;
 	private static final String COLOR_ERROR = RED;
 	private static final String COLOR_SUCCESS = GREEN;
 	private static final String COLOR_VOTE = PURPLE;
 	
 	private final CSVParser parser = new CSVParser(' ');
 	
 	private Timer pollTimer = null;
 	private String pollText = "";
 	private String pollOwner = "";
 	private long startTime;
 	private Map<String, String> votes;
 	private List<String> validVotes;
 	
 	@Command("\\.vote  *\\$([1-9][0-9]*) *")
 		public void vote(Message message, int vote) {
		this.vote(message, this.validVotes != null ? this.validVotes.get(vote) : null);
 	}
 
 	//@Command("\\.poll \\[((?:" + VOTE_CLASS_REGEX + "+,?)+)\\] ?(.*)")
 	//@Command("\\.poll \\[(" + VOTE_CLASS_REGEX + "+)\\] ?(.*)")
 	@Command("\\.poll (.*)")
 	public void poll(Message message, String argLine) {
 		if(this.pollTimer != null) {
 			this.bot.reply(message, "A poll is in progress");
 			return;
 		}
 		
 		this.pollOwner = message.getSender();
 		this.votes = new HashMap<String, String>();
 		
 
 		try {
 			final String[] args = this.parser.parseLine(argLine);
 			this.pollText = args[0];
 
 			this.validVotes = new LinkedList<String>();
 			if(args.length > 1) {
 				for(int i = 1; i < args.length; i++) {
 					String option = args[i].trim();
 					if(!option.isEmpty() && !this.validVotes.contains(option))
 						this.validVotes.add(option);
 				}
 			} else {
 				this.validVotes.addAll(Arrays.asList(new String[] {"yes", "no"}));
 			}
 		} catch(IOException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Exception attempting to parse vote options");
 			Log.e(e);
 			return;
 		}
 		
 		if(this.validVotes.size() < 2) {
 			this.bot.reply(message, "Polls need at least two options");
 			return;
 		}
 		
 		this.pollTimer = new Timer();
 		this.pollTimer.schedule(new TimerTask() {
 			@Override public void run() {
 				Poll.this.finished();
 			}
 		}, WAIT_TIME * 1000);
 		this.startTime = System.currentTimeMillis();
 		
 		this.bot.sendMessage(message.getSender() + " has started a poll (vote with ." + Help.COLOR_COMMAND + "vote" + NORMAL + " " + Help.COLOR_ARGUMENT + implode(this.validVotes.toArray(new String[0]), "/") + NORMAL + " in the next " + WAIT_TIME + " seconds): " + this.pollText);
 	}
 	
 
 	@Command("\\.vote  *(.+) *")
 	public void vote(Message message, String vote) {
 		if(this.pollTimer == null) {
 			this.bot.reply(message, COLOR_ERROR + "There is no poll in progress to vote on");
 			return;
 		}
 		
 		if(!this.validVotes.contains(vote)) {
 			this.bot.reply(message, COLOR_ERROR + "Invalid vote");
 			return;
 		}
 		
 		this.votes.put(message.getSender(), vote);
 		this.bot.reply(message, COLOR_SUCCESS + "Vote recorded" + NORMAL + ". Current standing: " + this.tabulate());
 	}
 	
 	@Command("\\.pollstats")
 	public void stats(Message message) {
 		if(this.pollTimer == null) {
 			this.bot.reply(message, COLOR_ERROR + "There is no poll in progress to check");
 		} else {
 			final int timeLeft = WAIT_TIME + (int)(this.startTime - System.currentTimeMillis()) / 1000;
 			this.bot.reply(message, "(" + timeLeft + "s remain" + (timeLeft == 1 ? "s" : "") + "): " + this.tabulate());
 		}
 	}
 	
 	@Command("\\.cancelpoll")
 	public void cancel(Message message) {
 		if(this.pollTimer == null) {
 			this.bot.reply(message, COLOR_ERROR + "There is no poll in progress to cancel");
 		} else if(!this.pollOwner.equals(message.getSender())) {
 			this.bot.reply(message, COLOR_ERROR + "Only " + this.pollOwner + " can cancel the poll");
 		} else if(!this.votes.isEmpty()) {
 			this.bot.reply(message, COLOR_ERROR + "You can't cancel a poll once votes are in");
 		} else {
 			this.pollTimer.cancel();
 			this.pollTimer = null;
 			this.bot.reply(message, COLOR_SUCCESS + "Poll canceled");
 		}
 	}
 	
 	private void finished() {
 		this.pollTimer = null;
 		
 		this.bot.sendMessage(COLOR_SUCCESS + "Poll finished" + NORMAL + ": " + this.pollText);
 		this.bot.sendMessage("Results: " + (this.votes.isEmpty() ? COLOR_VOTE + "No votes" : this.tabulate()));
 	}
 	
 	private String tabulate() {
 		final Map<String, LinkedList<String>> nicksPerVote = new HashMap<String, LinkedList<String>>();
 		for(String vote : this.validVotes) {
 			nicksPerVote.put(vote, new LinkedList<String>());
 		}
 		for(String nick : this.votes.keySet()) {
 			nicksPerVote.get(this.votes.get(nick)).add(nick);
 		}
 		final Vector<String> texts = new Vector<String>(nicksPerVote.size());
 		for(String vote : this.validVotes) {
 			final LinkedList<String> nicks = nicksPerVote.get(vote);
 			texts.add(COLOR_VOTE + nicks.size() + " " + vote + NORMAL + (nicks.isEmpty() ? "" : " (" + implode(nicks.toArray(new String[0]), ", ") + ")"));
 		}
 		
 		return implode(texts.toArray(new String[0]), ", ");
 	}
 	
 	@Override public String getFriendlyName() {return "Poll";}
 	@Override public String getDescription() {return "Polls users about a given question";}
 	@Override public String[] getExamples() {
 		return new String[] {
 //				".poll _question_ -- Allow users to vote yes or no on _question_",
 //				".poll [_vote1_,_vote2_,...] _question_ -- Allow users to vote _vote1_ or _vote2_ or ... on _question_",
 				".poll _question_ -- Allow users to vote yes or no on _question_. Double-quote _question_ if it has spaces",
 				".poll _question_ _vote1_ _vote2_ ... -- Allow users to vote _vote1_ or _vote2_ or ... on _question_. Double-quote any arguments if they have spaces",
 				".vote _vote_ -- Cast your vote as _vote_ (must be one of the votes specified in the poll)",
 				".pollstats -- Display time remaining in the poll and the current votes",
 				".cancelpoll -- Cancel the poll if no votes have been cast yet"
 		};
 	}
 
 	@Override public void unload() {
 		if(this.pollTimer != null) {
 			this.pollTimer.cancel();
 			this.pollTimer = null;
 		}
 	}
 }
