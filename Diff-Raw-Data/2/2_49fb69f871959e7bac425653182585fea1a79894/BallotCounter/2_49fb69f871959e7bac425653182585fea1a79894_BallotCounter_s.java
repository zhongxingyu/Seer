 package voteVis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import processing.core.*;
 
 public class BallotCounter {
 	private static BallotCounter instance_;
 	private LinkedHashMap<Long, Ballot> ballots_;
 	private VoteVisApp p_;
 	private ArrayList<Ballot> recent_ballots_;
 	
 	BallotCounter(VoteVisApp p_) {
 		instance_ = this;
 		ballots_ = new LinkedHashMap<Long, Ballot>();
 		recent_ballots_ = new ArrayList<Ballot>();
 		this.p_ = p_;
 	}
 	
 	public LinkedHashMap<Long, Ballot> ballots() {
 		return ballots_;
 	}
 	
 	// use fancy algorithms to determine the next ballot
 	public Ballot get_next_ballot() {
 		// get the earliest of the recent ballot if possible
 		if (recent_ballots_.size() != 0) {
 			Ballot b = recent_ballots_.get(0);
 			recent_ballots_.remove(0);
 			return b;
 		}
 		//PApplet.println("getting newest least...");
 		// else get the last
 		return get_newest_least_displayed();
 		
 	}
 	
 	// fuck I wish I hadn't split things up into Users and Ballots that was retarded
 	private Ballot get_newest_least_displayed() {
 		Ballot best = ballots_.get(0);
 		UserManager.User u = UserManager.instance().get_user(best.user_id());
 		
 		for (int i = 0; i < ballots_.size(); i++) {
 			Ballot b = ballots_.get(i);
 			
 			if (UserManager.instance().get_user(b.user_id()).display_count() < u.display_count()) {
 				best = b;
 				u = UserManager.instance().get_user(best.user_id());
 			}
 		}
 		
 		return best;
 	}
 	
 	public Ballot get_ballot(int index) {
 		return ballots_.get(index);
 	}
 	
 	public Ballot get_last_ballot() {
 		return ballots_.get(ballots_.size() -1);
 	}
 	
 	public void add_ballot(Ballot ballot) {
 		if (ballots_.containsKey(Long.valueOf(ballot.user_id())))
 			return;
 		
 		ballot.load_image();
 		UserManager.instance().add_user(ballot.user_id(), ballot.user_name(), ballot.user_photo());
 		
 		ballots_.put(Long.valueOf(ballot.user_id()), ballot);
		recent_ballots_.add(0, ballot);
 	}
 	
 	// generates a random ballot for testing
 	public void add_random_ballot() {
 		int[] votes = new int[5];
 		
 		// iterate through the 5 types of things to vote on
 		votes[Type.MUSIC.ordinal()] = (int) p_.random(Settings.NUM_MUSIC);
 		votes[Type.ART.ordinal()] = (int) p_.random(Settings.NUM_ART);
 		votes[Type.FOOD.ordinal()] = (int) p_.random(Settings.NUM_FOOD);
 		votes[Type.WINE.ordinal()] = (int) p_.random(Settings.NUM_WINE);
 		votes[Type.ECO.ordinal()] = (int) p_.random(Settings.NUM_ECO);
 		
 		int user_id = ballots_.size() + 1;
 		
 		add_ballot(new Ballot(user_id, "Foo Bar", Gender.MALE, votes, "default-male.png"));
 	}
 	
 	// [0] = #1
 	// [1] = #2
 	// [2] = ...
 	public int[] get_top_five(Type type) {
 		// This holds the index (which musician, etc), and the number of votes for him/her.
 		HashMap<Integer, Integer> counter = new HashMap<Integer, Integer>();
 		
 		for (Long key : ballots_.keySet()) {
 			Ballot b = ballots_.get(key);
 			int[] int_arr = b.votes();
 			Integer vote_key = int_arr[type.ordinal()];
 			
 			Integer last_number = counter.get(vote_key);
 			
 			if (last_number == null)
 				counter.put(vote_key, new Integer(1));
 			else
 				counter.put(vote_key, new Integer(last_number + 1));
 		}
 		
 		int[] ret_int = new int[5];
 		for(int i = 0; i < 5; ++i) {
 			Integer top_key = 0;
 			Integer top_amount = 0;
 			
 			for (Integer key : counter.keySet()) {
 				if (counter.get(key) >= top_amount) {
 					top_key = key;
 					top_amount = counter.get(key);
 				}
 			}
 			ret_int[i] = top_key.intValue();
 			if (counter.containsKey(top_key)) // this is needed if there are under 5 ballots
 				counter.remove(top_key);
 		}
 		
 		return ret_int;
 	}
 	
 	public static BallotCounter instance() {
 		return instance_;
 	}
 	
 	
 }
