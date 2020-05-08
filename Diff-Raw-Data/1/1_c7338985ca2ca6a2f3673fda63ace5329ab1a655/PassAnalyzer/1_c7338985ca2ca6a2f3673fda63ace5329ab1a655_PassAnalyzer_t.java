 /*
  * $Log: PassAnalyzer.java,v $
  * Revision 1.5  2003/01/04 08:35:36  koji
  * �ѥ���Ϣ�����ѥ��ߥ����ɥ�֥롢���줾�����ײ��Ϥ��ɲ�
  *
  * Revision 1.4  2002/12/23 05:32:11  koji
  * PassDistAnalyzer�ɲ�
  *
  * Revision 1.3  2002/10/17 09:33:10  koji
  * ���祭�å��ΤȤ��ν������
  *
  * Revision 1.2  2002/10/16 12:04:33  koji
  * PlayON�ʳ��ΤȤ��ν�����ɲá����å������礷���Ȥ��ϡ��ɤ���λ��ۤǤ�
  * �ʤ����Ȥˤ���
  *
  * Revision 1.1  2002/10/15 10:09:48  koji
  * ��������Pass������ȡ�Team/�����̥ơ��֥��ɲ�
  *
  */
 
 package soccerscope.util.analyze;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import soccerscope.model.GameEvent;
 import soccerscope.model.Param;
 import soccerscope.model.PlayMode;
 import soccerscope.model.Player;
 import soccerscope.model.Scene;
 import soccerscope.model.Team;
 import soccerscope.model.WorldModel;
 import soccerscope.util.geom.Point2f;
 import soccerscope.util.geom.Rectangle2f;
 
 import com.jamesmurty.utils.XMLBuilder;
 
 public class PassAnalyzer extends SceneAnalyzer implements Xmling {
 
 	public static String NAME = "Complete Pass";
 
 	private static int pTable[] = new int[0];
 	private static ArrayList<Pass> passList = new ArrayList<Pass>();
 	private static ArrayList<Pass> passLeftList = new ArrayList<Pass>();
 	private static ArrayList<Pass> passRightList = new ArrayList<Pass>();
 	private Kicker leftKicker;
 	private Kicker rightKicker;
 
 	public final static int PLAY_OFF = 0;
 	public final static int PLAY_ON = 1;
 	public final static int LEFT_SIDE = 2;
 	public final static int RIGHT_SIDE = 3;
 
 	/* START - zones stuffs */
 	public class Zone {
 		Rectangle2f area;
 		int countLeft;
 		int countRight;
 
 		int side; // side of the field where the zone is...
 
 		public Zone(int side, Rectangle2f a) {
 			area = a;
 			this.side = side;
 			this.countLeft = 0;
 			this.countRight = 0;
 		}
 
 		public boolean contains(Point2f p) {
 			return this.area.contains(p);
 		}
 	}
 
 	ArrayList<Zone> zones;
 	ArrayList<Zone> lastDangerZones;
 
 	private void initZones() {
 		this.zones = new ArrayList<Zone>(2);
 		this.zones.add(new Zone(Team.LEFT_SIDE, new Rectangle2f(new Point2f(
 				-52.5f, -34f), 52.5f, 68f)));
 		this.zones.add(new Zone(Team.RIGHT_SIDE, new Rectangle2f(new Point2f(
 				0.0f, -34f), 52.5f, 68f)));
 
 		this.lastDangerZones = new ArrayList<Zone>(2);
 		this.lastDangerZones.add(new Zone(Team.LEFT_SIDE, new Rectangle2f(
 				new Point2f(-52.5f, -34f), 16.5f, 68f)));
 		this.lastDangerZones.add(new Zone(Team.RIGHT_SIDE, new Rectangle2f(
 				new Point2f(36f, -34f), 16.5f, 68f)));
 	}
 
 	/* END - zones stuffs */
 
 	public void init() {
 		super.init();
 		initZones();
 		Scene scene = WorldModel.getInstance().getSceneSet().lastScene();
 		pTable = new int[scene.time + 1];
 		passList.clear();
 		passLeftList.clear();
 		passRightList.clear();
 		leftKicker = null;
 		rightKicker = null;
 	}
 
 	public String getName() {
 		return NAME;
 	}
 
 	public class Kicker implements Xmling {
 		public int time;
 		public int unum;
 
 		public Kicker(int time, int unum) {
 			this.time = time;
 			this.unum = unum;
 		}
 
 		public void set(Kicker k) {
 			time = k.time;
 			unum = k.unum;
 		}
 
 		public String toString() {
 			return "kicker ::" + time + ": " + unum;
 		}
 
 		// very peculiar.. please don't judge me!
 		@Override
 		public void xmlElement(XMLBuilder builder) {
 			builder.attr("time", String.valueOf(this.time)).attr("player",
 					String.valueOf(this.unum));
 
 		}
 	}
 
 	public static class Pass implements Xmling {
 		public int side;
 		public Kicker sender;
 		public Kicker receiver;
 		public boolean breakPass;
 		public boolean inOffensiveField;
 
 		public Pass(int side, Kicker s, Kicker r) {
 			this.side = side;
 			sender = s;
 			receiver = r;
 			this.breakPass = false;
 			this.inOffensiveField = false;
 		}
 
 		public String toString() {
 			return "team(" + side + ") break(" + breakPass + ") :: time("
 					+ sender.time + ") unum(" + sender.unum + ") -> time("
 					+ receiver.time + ") unum(" + receiver.unum + ")";
 		}
 
 		@Override
 		public void xmlElement(XMLBuilder builder) {
 			XMLBuilder pass = builder.elem("pass")
 					.attr("team", Team.name(side)).attr("breakpass",
 							String.valueOf(this.breakPass)).attr("offensive",
 							String.valueOf(this.inOffensiveField));
 			this.sender.xmlElement(pass.elem("kick"));
 			this.receiver.xmlElement(pass.elem("reception"));
 		}
 	}
 
 	public GameEvent analyze(Scene scene, Scene prev) {
 		if (prev == null)
 			return null;
 
 		if (scene.time < pTable.length)
 			pTable[scene.time] = PLAY_OFF;
 
 		// �ץ쥤��ʳ���, ��
 		if (scene.pmode.pmode != PlayMode.PM_PlayOn) {
 			leftKicker = null;
 			rightKicker = null;
 			return null;
 		}
 
 		pTable[scene.time] = PLAY_ON;
 
 		Kicker left = null;
 		Kicker right = null;
 
 		for (int i = 0; i < Param.MAX_PLAYER; i++) {
 			if ((scene.player[i].isKicking() && prev.player[i]
 					.isKickable(prev.ball.pos))
 					|| (scene.player[i].isCatching() && prev.player[i]
 							.isCatchable(prev.ball.pos))
 					|| (scene.player[i].isTackling() && prev.player[i]
 							.canTackle(prev.ball.pos))) {
 				left = new Kicker(scene.time, scene.player[i].unum);
 			}
 		}
 		for (int i = Param.MAX_PLAYER; i < Param.MAX_PLAYER * 2; i++) {
 			if ((scene.player[i].isKicking() && prev.player[i]
 					.isKickable(prev.ball.pos))
 					|| (scene.player[i].isCatching() && prev.player[i]
 							.isCatchable(prev.ball.pos))
 					|| (scene.player[i].isTackling() && prev.player[i]
 							.canTackle(prev.ball.pos))) {
 				right = new Kicker(scene.time, scene.player[i].unum);
 			}
 		}
 
 		GameEvent ge = null;
 		if (left != null && right != null) {
 			leftKicker = null;
 			rightKicker = null;
 			left = null;
 			right = null;
 		}
 		if (left != null) {
 			if (leftKicker != null && leftKicker.unum != left.unum) {
 				Pass pass = new Pass(Team.LEFT_SIDE, leftKicker, left);
 				checkBreakPass(pass, scene);
 				countUp(pass);
 				ge = new GameEvent(leftKicker.time, GameEvent.PASS);
 			}
 			if (leftKicker != null) {
 				Arrays.fill(pTable, leftKicker.time, left.time + 1, LEFT_SIDE);
 			}
 			leftKicker = left;
 			if (right == null)
 				rightKicker = null;
 		}
 		if (right != null) {
 			if (rightKicker != null && rightKicker.unum != right.unum) {
 				Pass pass = new Pass(Team.RIGHT_SIDE, rightKicker, right);
 				checkBreakPass(pass, scene);
 				countUp(pass);
 				ge = new GameEvent(rightKicker.time, GameEvent.PASS);
 			}
 			if (rightKicker != null) {
 				Arrays.fill(pTable, rightKicker.time, right.time + 1,
 						RIGHT_SIDE);
 			}
 			rightKicker = right;
 			if (left == null)
 				leftKicker = null;
 		}
 
 		return ge;
 	}
 
 	public void countUp(Pass pass) {
 		Point2f sender_ball_pos = WorldModel.getInstance().getSceneSet()
 				.getScene((pass.receiver.time)).ball.pos;
 		for (Zone zone : this.zones) {
 			if (zone.contains(sender_ball_pos)) {
 				if (pass.side == Team.RIGHT_SIDE) {
 					zone.countRight++;
 				} else if (pass.side == Team.LEFT_SIDE) {
 					zone.countLeft++;
 				}
 				if (zone.side != pass.side) {
 					pass.inOffensiveField = true;
 				}
 			}
 
 		}
 
 		passList.add(pass);
 		if (pass.side == Team.LEFT_SIDE) {
 			passLeftList.add(pass);
 			super.countUpLeft(pass.receiver.time);
 			System.out.println("LEFT_PASSHIT START_CYCLE(" + pass.sender.time
 					+ ") END_CYCLE(" + pass.receiver.time + ")");
 		} else if (pass.side == Team.RIGHT_SIDE) {
 			passRightList.add(pass);
 			super.countUpRight(pass.receiver.time);
 			System.out.println("RIGHT_PASSHIT START_CYCLE(" + pass.sender.time
 					+ ") END_CYCLE(" + pass.receiver.time + ")");
 		}
 	}
 
 	private void checkBreakPass(Pass pass, Scene scene) {
 		int[] teamIndexes = Team.firstAndLastPlayerIndexes(pass.side);
 		int opposing_team = Team.opposingTeam(pass.side);
 		Player player = null;
 		for (int iter = teamIndexes[0]; iter < teamIndexes[1]; iter++) {
 			if (scene.player[iter].unum == pass.receiver.unum) {
 				player = scene.player[iter];
 				break;
 			}
 		}
 
 		for (Zone zone : lastDangerZones) {
 			// player in the last third
 			if (zone.side == opposing_team && zone.contains(player.pos)) {
 				if (GoalOpportunityAnalyzer.hasGoalOpportunity(player, scene)) {
 					pass.breakPass = true;
 				}
 				break;
 			}
 		}
 
 	}
 
 	public static int[] getPossessionTable() {
 		return pTable;
 	}
 
 	public static int getPossessionTeam(int time) {
 		if (time < pTable.length)
 			return pTable[time];
 		else
 			return PLAY_OFF;
 	}
 
 	public static ArrayList<Pass> getPassList() {
 		return passList;
 	}
 
 	public static ArrayList<Pass> getPassLeftList() {
 		return passLeftList;
 	}
 
 	public static ArrayList<Pass> getPassRightList() {
 		return passRightList;
 	}
 
 	public static Pass getPass(int time) {
 		Iterator<Pass> it = passList.iterator();
 
 		while (it.hasNext()) {
 			Pass pass = it.next();
 			if (pass.sender.time <= time && time <= pass.receiver.time) {
 				return pass;
 			}
 		}
 		return null;
 	}
 
 	public static Pass getPass(int side, int time) {
 		if (side == Team.LEFT_SIDE)
 			return getLeftPass(time);
 		else if (side == Team.RIGHT_SIDE) {
 			return getRightPass(time);
 		}
 		return null;
 	}
 
 	public static Pass getLeftPass(int time) {
 		Iterator<Pass> it = passLeftList.iterator();
 
 		while (it.hasNext()) {
 			Pass pass = it.next();
 			if (pass.sender.time <= time && time <= pass.receiver.time) {
 				return pass;
 			}
 		}
 		return null;
 	}
 
 	public static Pass getRightPass(int time) {
 		Iterator<Pass> it = passRightList.iterator();
 
 		while (it.hasNext()) {
 			Pass pass = it.next();
 			if (pass.sender.time <= time && time <= pass.receiver.time) {
 				return pass;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * turns PassAnalyzer.LEFT_SIDE into Team.LEFT_SIDE and
 	 * PassAnalyzer.RIGHT_SIDE into Team.RIGHT_SIDE
 	 * 
 	 * @return (...) return Team.NEUTRAL for unknown arguments
 	 */
 	public static int sideToStandardSide(int side) {
 		if (side == PassAnalyzer.LEFT_SIDE)
 			return Team.LEFT_SIDE;
 		if (side == PassAnalyzer.RIGHT_SIDE)
 			return Team.RIGHT_SIDE;
 		return Team.NEUTRAL;
 	}
 
 	@Override
 	public void xmlElement(XMLBuilder builder) {
 		XMLBuilder passesXml = builder.elem("passes").attr("left",
 				String.valueOf(PassAnalyzer.passLeftList.size())).attr("right",
 				String.valueOf(PassAnalyzer.passRightList.size()));
 		for (Pass p : PassAnalyzer.passList)
 			p.xmlElement(passesXml);
 	}
 }
