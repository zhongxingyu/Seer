 package taberystwyth.allocation;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 import java.util.TreeMap;
 
 import taberystwyth.allocation.options.JudgeAllocation;
 import taberystwyth.allocation.options.LocationAllocation;
 import taberystwyth.allocation.options.TeamAllocation;
 import taberystwyth.allocation.exceptions.*;
 import taberystwyth.db.SQLConnection;
 
 public class Allocator {
 
 	Random randomGenerator = new Random(0L);
 
 	SQLConnection conn = SQLConnection.getInstance();
 	ResultSet rs;
 	String query;
 
 	private static Allocator instance = new Allocator();
 
 	public static Allocator getInstance() {
 		return instance;
 	}
 
 	private Allocator() {/* VOID */
 	};
 
 	/**
 	 * Allocate the matches
 	 * 
 	 * @param teamAlgo
 	 * @param judgeAlgo
 	 * @param locationAlgo
 	 * @throws SQLException
 	 * @throws SwingTeamsRequiredException
 	 * @throws LocationsRequiredException
 	 * @throws JudgesRequiredException
 	 */
 	public void allocate(TeamAllocation teamAlgo, JudgeAllocation judgeAlgo,
 			LocationAllocation locationAlgo) throws SQLException,
 			SwingTeamsRequiredException, LocationsRequiredException,
 			JudgesRequiredException {
 		/*
 		 * Figure out the current round
 		 */
 		int round = 0; // FIXME
 
 		/*
 		 * Generate matches
 		 */
 		query = "select count(*) / 4 from teams;";
 		rs = conn.executeQuery(query);
 		rs.next();
 		int nMatches = rs.getInt(1);
 		ArrayList<Match> matches = new ArrayList<Match>();
 		int rank = 0;
 		while (matches.size() > nMatches) {
 			matches.add(new Match(rank));
 			++rank;
 		}
 		rs.close();
 
 		/*
 		 * Allocate teams
 		 */
 		if (teamAlgo == TeamAllocation.WUDC) {
 			TreeMap<Integer, ArrayList<String>> pools = getLeveledPools();
 
 			int highestTeamScore = pools.lastKey();
 
 			rank = 0;
			// FIXME: What if highest team score is zero?  (Initial round)
 			for (int treeIndex = highestTeamScore; treeIndex > 0; --treeIndex) {
 				if (pools.containsKey(treeIndex)) {
 					ArrayList<String> pool = pools.get(treeIndex);
 					for (int i = 0; i < (pools.get(i).size() / 4); ++i) {
 
 						Match match = matches.get(rank);
 
 						match.setFirstProp(pool.remove(randomGenerator
 								.nextInt(pool.size())));
 						match.setFirstOp(pool.remove(randomGenerator
 								.nextInt(pool.size())));
 						match.setSecondProp(pool.remove(randomGenerator
 								.nextInt(pool.size())));
 						match.setSecondOp(pool.remove(randomGenerator
 								.nextInt(pool.size())));
 
 						rank++;
 					}
 				}
 			}
 
 		}
 
 		/*
 		 * Allocate Judges
 		 */
 		if (judgeAlgo == JudgeAllocation.BALANCED) {
 
 		}
 
 		/*
 		 * Allocate locations
 		 */
 		if (locationAlgo == LocationAllocation.RANDOM) {
 		} else if (locationAlgo == LocationAllocation.BEST_TO_BEST) {
 
 		}
 
 		for (Match m : matches) {
 			String insert = "insert into rooms first_prop, second_prop,"
 					+ "first_op, second_prop, location, round values("
 					+ m.getFirstProp() + ", " + m.getSecondProp() + ", "
 					+ m.getFirstOp() + ", " + m.getSecondOp() + ", "
 					+ m.getLocation() + ", " + round;
 		}
 	}
 
 	private TreeMap<Integer, ArrayList<String>> getLeveledPools()
 			throws SQLException {
 		TreeMap<Integer, ArrayList<String>> pools = getPools();
 
 		/*
 		 * For each pool, starting at the top (highest ranked pool) and working
 		 * down...
 		 */
 		for (int i = pools.lastKey(); i > 0; --i) {
 			/*
 			 * if the pool exists...
 			 */
 			if (pools.containsKey(i)) {
 				/*
 				 * and the pools size is not a multiple of 4...
 				 */
 				while (!((pools.get(i).size() % 4) == 0)) {
 					/*
 					 * then pull up (randomly) a member from the pool directly
 					 * below this one
 					 */
 					for (int j = (i - 1); j > 0; --j) {
 						if (pools.containsKey(j)) {
 							ArrayList<String> lowerPool = pools.get(j);
 							int randomElementIndex = randomGenerator
 									.nextInt(lowerPool.size());
 							pools.get(i).add(
 									pools.get(j).remove(randomElementIndex));
 
 							/*
 							 * If the arraylist is empty then delete it
 							 */
 							if (lowerPool.size() == 0) {
 								pools.remove(j);
 							}
 							break;
 						}
 					}
 				}
 			}
 		}
 
 		return pools;
 	}
 
 	private TreeMap<Integer, ArrayList<String>> getPools() throws SQLException {
 		/*
 		 * Construct the map of points to teams (pools)
 		 */
 		TreeMap<Integer, ArrayList<String>> pools = new TreeMap<Integer, ArrayList<String>>();
 
 		/*
 		 * Get the map of team names to points
 		 */
 		HashMap<String, Integer> points = getTeamPoints();
 
 		/*
 		 * For each team, add it to map of pools
 		 */
 		for (String team : points.keySet()) {
 			int innerPoints = points.get(team);
 			/*
 			 * If the pool does not exist, create it
 			 */
 			if (!pools.containsKey(innerPoints)) {
 				pools.put(innerPoints, new ArrayList<String>());
 			}
 			pools.get(innerPoints).add(team);
 		}
 
 		return pools;
 	}
 
 	/**
 	 * Get a list of teams mapped to team points
 	 * 
 	 * @return A HashMap of Team names and Team points
 	 * @throws SQLException
 	 */
 	protected HashMap<String, Integer> getTeamPoints() throws SQLException {
 		HashMap<String, Integer> teamScores = new HashMap<String, Integer>();
 		String query;
 		ResultSet rs;
 
 		/*
 		 * Check if any rounds have happened yet, if not set all scores to 0
 		 */
 		query = "select count (*) from team_results;";
 		rs = SQLConnection.getInstance().executeQuery(query);
 		rs.next();
 		if (rs.getInt(1) == 0) {
 			for (String t : getTeamNames()) {
 				teamScores.put(t, 0);
 			}
 			return teamScores;
 		}
 
 		/*
 		 * Otherwise, calculate the total team score for each team, and add the
 		 * team to the map
 		 */
 		for (String name : getTeamNames()) {
 			int teamPoints = 0;
 			query = "select position from team_results where team = '" + name
 					+ "';";
 			rs = SQLConnection.getInstance().executeQuery(query);
 
 			/*
 			 * Sum the team points according to positions taken in rounds
 			 */
 			boolean scoreCalculated = false;
 			while (rs.next()) {
 				int position = rs.getInt("position");
 				if (position == 1) {
 					teamPoints += 3;
 				} else if (position == 2) {
 					teamPoints += 2;
 				} else if (position == 3) {
 					teamPoints += 1;
 				} else {
 					// VOID
 				}
 				scoreCalculated = true;
 			}
 
 		}
 		
 		return teamScores;
 	}
 
 	/**
 	 * Gets an unordered list of team names
 	 * 
 	 * @return List of teams
 	 * @throws SQLException
 	 */
 	protected ArrayList<String> getTeamNames() throws SQLException {
 		ArrayList<String> teamNames = new ArrayList<String>();
 		String query = "select name from teams;";
 		ResultSet rs = SQLConnection.getInstance().executeQuery(query);
 		while (rs.next()) {
 			teamNames.add(rs.getString("name"));
 		}
 		return teamNames;
 	}
 
 }
