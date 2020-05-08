 package soccerscope.util.analyze;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import soccerscope.model.GameEvent;
 import soccerscope.model.Player;
 import soccerscope.model.Scene;
 import soccerscope.model.Team;
 import soccerscope.util.geom.Point2f;
 import soccerscope.util.geom.Triangle2f;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 public class GoalOpportunityAnalyzer extends SceneAnalyzer implements Xmling {
 
 	public static final String NAME = "Goal Opportunity";
 
 	public class Zone {
 		public int field;
 		public float radius;
 		public Point2f center;
 
 		public Zone(int field, Point2f center, float radius) {
 			this.field = field;
 			this.radius = radius;
 			this.center = center;
 		}
 
 		public boolean contains(Point2f p) {
 			float radius_squared = radius * radius;
 			if (center.distanceSquared(p) < radius_squared) {
 				if (this.field == Team.LEFT_SIDE) {
 					return center.isBehind(p);
 				} else if (this.field == Team.RIGHT_SIDE) {
 					return center.inFrontOf(p);
 				}
 			}
 			return false;
 		}
 	}
 
 	public class Opportunity implements Xmling {
 		int side;
 		int stime;
 		int etime;
 		int player_unum;
 
 		public Opportunity(int side, int time, int player_unum) {
 			this.side = side;
 			this.stime = time;
 			this.etime = time;
 			this.player_unum = player_unum;
 		}
 
 		@Override
 		public void xmlElement(XMLBuilder builder) {
			builder.elem("opportunity").attr("side", Team.name(side)).attr(
 					"start", String.valueOf(stime)).attr("player",
 					String.valueOf(player_unum)).attr("end",
 					String.valueOf(etime));
 		}
 	}
 
 	public static final Point2f[] LEFTSIDE_POLES = {
 			new Point2f(-52.5f, 3.66f), new Point2f(-52.5f, -3.66f) };
 	public static final Point2f[] RIGHTSIDE_POLES = {
 			new Point2f(52.5f, 3.66f), new Point2f(52.5f, -3.66f) };
 
 	List<Zone> zones;
 	List<Opportunity> opportunities = new ArrayList<Opportunity>(20);
 
 	private void initZones() {
 		this.zones = new ArrayList<Zone>(2);
 		this.zones
 				.add(new Zone(Team.LEFT_SIDE, new Point2f(-52.5f, 0f), 16.5f));
 		this.zones
 				.add(new Zone(Team.RIGHT_SIDE, new Point2f(52.5f, 0f), 16.5f));
 	}
 
 	public GoalOpportunityAnalyzer() {
 		initZones();
 	}
 
 	@Override
 	public void init() {
 		opportunities.clear();
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	@Override
 	public GameEvent analyze(Scene scene, Scene prev) {
 		int opportunityTeam = Team.NEUTRAL;
 		int[] myPlayer = null;
 
 		for (Zone z : zones) {
 			// ball in the danger zone
 			if (z.contains(scene.ball.pos)) {
 				if (z.field == Team.LEFT_SIDE) {
 					// left side is threatened by the right team...
 					opportunityTeam = Team.RIGHT_SIDE;
 					myPlayer = Team.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
 				} else if (z.field == Team.RIGHT_SIDE) {
 					// do i need to explain?
 					opportunityTeam = Team.LEFT_SIDE;
 					myPlayer = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
 				}
 
 				// team player in kicking conditions
 				for (int player_it = myPlayer[0]; player_it < myPlayer[1]; player_it++) {
 					if (hasGoalOpportunity(scene.player[player_it], scene)) {
 						if (opportunityTeam == Team.LEFT_SIDE) {
 							System.out.println("LEFT_SIDE OPPORTUNITY time("
 									+ scene.time + ")");
 							this.countUpLeft(scene.time,
 									scene.player[player_it]);
 						} else {
 							System.out.println("RIGHT_SIDE OPPORTUNITY time("
 									+ scene.time + ")");
 							this.countUpRight(scene.time,
 									scene.player[player_it]);
 						}
 						break;
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean countCommon(int time, Player player) {
 		// no opportunities detected so far.. must be a new one ;)
 		if (this.opportunities.size() == 0) {
 			return true;
 		}
 
 		Opportunity lastOpportunity = this.opportunities.get(this.opportunities
 				.size() - 1);
 		// don't count sequential opportunity situations,
 		// they are the same opportunity
 
 		// same team
 		if (player.side == lastOpportunity.side)
 			// the opportunity end time is from the last round.
 			if (lastOpportunity.etime == (time - 1)) {
 				lastOpportunity.etime = time;
 				return false;
 			}
 		return true;
 	}
 
 	public void countUpLeft(int time, Player player) {
 		// don't count sequential opportunity situations,
 		// they are the same opportunity
 		if (!countCommon(time, player))
 			return;
 		super.countUpLeft(time);
 		this.opportunities.add(new Opportunity(Team.LEFT_SIDE, time,
 				player.unum));
 	}
 
 	public void countUpRight(int time, Player player) {
 		// don't count sequential opportunity situations,
 		// they are the same opportunity
 		if (!countCommon(time, player))
 			return;
 		super.countUpRight(time);
 		this.opportunities.add(new Opportunity(Team.RIGHT_SIDE, time,
 				player.unum));
 	}
 
 	public static boolean hasGoalOpportunity(Player player, Scene scene) {
 		int[] opponentPlayer = null;
 		if (player.isKickable(scene.ball.pos)) {
 			Point2f[] poles = null;
 			if (player.side == Team.LEFT_SIDE) {
 				poles = RIGHTSIDE_POLES;
 				opponentPlayer = Team
 						.firstAndLastPlayerIndexes(Team.RIGHT_SIDE);
 			} else if (player.side == Team.RIGHT_SIDE) {
 				poles = LEFTSIDE_POLES;
 				opponentPlayer = Team.firstAndLastPlayerIndexes(Team.LEFT_SIDE);
 			}
 
 			// define the triangle
 			Triangle2f cone = new Triangle2f(player.pos, poles[0], poles[1]);
 			// count the number of opponents in the cone
 			int opponent_in_cone = 0;
 			for (int opp_player_it = opponentPlayer[0]; opp_player_it < opponentPlayer[1]; opp_player_it++) {
 				if (cone.contains(scene.player[opp_player_it].pos)) {
 					opponent_in_cone++;
 				}
 			}
 
 			// only one or zero opponents in triangle
 			if (opponent_in_cone < 2) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public void xmlElement(XMLBuilder builder) {
 		XMLBuilder oppXml = builder.elem("goalopportunities").attr("left",
 				String.valueOf(lcount)).attr("right", String.valueOf(rcount));
 		for (Opportunity opp : this.opportunities) {
 			opp.xmlElement(oppXml);
 		}
 	}
 
 }
