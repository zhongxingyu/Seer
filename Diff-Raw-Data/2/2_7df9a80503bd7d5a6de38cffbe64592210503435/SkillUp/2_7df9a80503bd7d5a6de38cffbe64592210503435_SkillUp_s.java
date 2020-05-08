 package hoplugins.feedback.model.training;
 
 import hoplugins.Commons;
 import hoplugins.Feedback;
 import hoplugins.feedback.model.FeedbackObject;
 import hoplugins.feedback.util.FeedbackHelper;
 
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.List;
 import java.util.Vector;
 
 import plugins.IHOMiniModel;
 import plugins.IJDBCAdapter;
 import plugins.ISpieler;
 
 public class SkillUp {
 	private int hrfId;
 	// timestamp is the date at the END of the current training week (i.e. the next training date) 
 	private Timestamp timestamp;
 	private Timestamp lastSkillup;
 	private ISpieler player;
 	private int skill;
 	private int value;
 	private double length;
 	private static final int NUM_TRAINING_TYPES = 13; // see ITeam.TA_*
 	private int[] trTypes = new int[NUM_TRAINING_TYPES];
 	private int[] osmosis = new int[NUM_TRAINING_TYPES];
 	private List<Integer> hrfList = new Vector<Integer>(); 
 	private static IHOMiniModel miniModel = Feedback.getMiniModel();
 	
 	public SkillUp(int hrfId, Timestamp timestamp,
 			int playerId, int skill, int value) {
 		this(hrfId, timestamp, null, miniModel.getSpielerAtDate(playerId, timestamp), skill, value);
 	}
 	
 	public SkillUp(int hrfId, Timestamp timestamp,
 			ISpieler player, int skill, int value) {
 		this(hrfId, timestamp, null, player, skill, value);
 	}
 
 	public SkillUp(int hrfId, Timestamp timestamp, Timestamp lastSkillup,
 			ISpieler player, int skill, int value) {
 		super();
 		this.hrfId = hrfId;
 		this.timestamp = timestamp;
 		this.lastSkillup = lastSkillup;
 		this.player = player;
 		this.skill = skill;
 		this.value = value;
 	}
 	
 	public int getHrfId() {
 		return hrfId;
 	}
 	public void setHrfId(int hrfId) {
 		this.hrfId = hrfId;
 	}
 	public Timestamp getTimestamp() {
 		return timestamp;
 	}
 	public void setTimestamp(Timestamp timestamp) {
 		this.timestamp = timestamp;
 	}
 	
 	public Timestamp getLastSkillup() {
 		return lastSkillup;
 	}
 
 	public void setLastSkillup(Timestamp lastSkillup) {
 		this.lastSkillup = lastSkillup;
 	}
 
 	public int getPlayerId() {
 		return player.getSpielerID();
 	}
 	
 	public ISpieler getPlayer() {
 		return player;
 	}
 	
 	public void setPlayer(ISpieler player) {
 		this.player = player;
 	}
 	
 	public int getSkill() {
 		return skill;
 	}
 	
 	public void setSkill(int skill) {
 		this.skill = skill;
 	}
 	
 	public int getValue() {
 		return value;
 	}
 	
 	public void setValue(int value) {
 		this.value = value;
 	}
 	
 	public double getLength() {
 		return FeedbackHelper.round2(length);
 	}
 
 	public void setLength(double length) {
 		this.length = length;
 	}
 
 	public void addLength(double length) {
 		this.length += length;
 	}
 
 	public int getHtSeason () {
 		return Commons.getModel().getHelper().getHTSeason(timestamp);
 	}
 
 	public int getHtWeek() {
 		return Commons.getModel().getHelper().getHTWeek(timestamp);
 	}
 
 	@Override
 	public String toString () {
		return ("Skillup: ("+(lastSkillup==null?"?":lastSkillup.toLocaleString())+" -> "+timestamp.toLocaleString()+", "+ getHtSeason()+"."+getHtWeek()+", hrfId="+hrfId+") playerId="+player.getSpielerID()+", skill="+skill+", value="+value+", length="+getLength()+", avgAge="+getAvgPlayerAge()+", avgAssis="+getAvgAssistants());
 	}
 	
 	public String toUrl () {
 		return ("contributor=" + Commons.getModel().getBasics().getTeamId()
 					+ "&htseason=" + getHtSeason() + "&htweek=" + getHtWeek()
 					+ "&playerId=" + player.getSpielerID() 
 					+ "&skill=" + skill + "&val=" + value
 					+ "&len=" + getLength() + "&age=" + getAvgPlayerAge() 
 					+ "&assi="+getAvgAssistants() + "&trainer="+getAvgTrainer()
 					+ "&info="+getTrainings()
 					+ "&ver="+FeedbackObject.getFeedbackVersion());
 	}
 	
 	public double getAvgPlayerAge () {
 		if (hrfList.size() == 0)
 			return -1;
 		IHOMiniModel miniModel = Commons.getModel();
 		IJDBCAdapter adapter = miniModel.getAdapter();
 		String myQuery = "select avg (cast (AGE as float)) as AVGAGE"
 			+" from SPIELER "
 			+" where SPIELERID="+player.getSpielerID()+" and ("
 			+" HRF_ID="+hrfList.get(0);
 		for (int i=1; i<hrfList.size(); i++) {
 			myQuery +=" or HRF_ID="+hrfList.get(i);
 		}
 		myQuery += ")";
 //		System.out.println (myQuery);
 		ResultSet rs = adapter.executeQuery(myQuery);
 		
 		if (rs != null) {
 			try {
 				if (rs.next()) {
 					return (FeedbackHelper.round2(rs.getDouble("AVGAGE")));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return -1;
 	}
 	
 	public double getAvgAssistants() {
 		if (hrfList.size() == 0)
 			return -1;
 		IHOMiniModel miniModel = Commons.getModel();
 		IJDBCAdapter adapter = miniModel.getAdapter();
 		String assistentName = "COTRAINER";
 		String myQuery = "select avg (cast ("+assistentName+" as float)) as AVGASSISTANTS"
 			+" from VEREIN "
 			+" where HRF_ID="+hrfList.get(0);
 		for (int i=1; i<hrfList.size(); i++) {
 			myQuery +=" or HRF_ID="+hrfList.get(i);
 		}
 //		System.out.println (myQuery);
 		ResultSet rs = adapter.executeQuery(myQuery);
 		
 		if (rs != null) {
 			try {
 				if (rs.next()) {
 					return (FeedbackHelper.round2(rs.getDouble("AVGASSISTANTS")));
 //					return (rs.getDouble("AVGASSISTANTS"));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return -1;
 	}
 	
 	public double getAvgTrainer() {
 		if (hrfList.size() == 0)
 			return -1;
 		IHOMiniModel miniModel = Commons.getModel();
 		IJDBCAdapter adapter = miniModel.getAdapter();
 		String myQuery = "select avg (cast (TRAINER as float)) as AVGTRAINER"
 			+" from SPIELER "
 			+" where TRAINERTYP>=0 and (HRF_ID="+hrfList.get(0);
 		for (int i=1; i<hrfList.size(); i++) {
 			myQuery +=" or HRF_ID="+hrfList.get(i);
 		}
 		myQuery += ")";
 //		System.out.println (myQuery);
 		ResultSet rs = adapter.executeQuery(myQuery);
 		
 		if (rs != null) {
 			try {
 				if (rs.next()) {
 					return (FeedbackHelper.round2(rs.getDouble("AVGTRAINER")));
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		return -1;
 	}
 
 	public void addHrf (int hrfId) {
 		hrfList.add (new Integer(hrfId));
 	}
 	
 	public void addTraining (int trainType) {
 		if (trainType >= 0 && trainType < NUM_TRAINING_TYPES)
 			trTypes[trainType]++;
 	}
 
 	public void addOsmosis (int trainType) {
 		if (trainType >= 0 && trainType < NUM_TRAINING_TYPES)
 			osmosis[trainType]++;
 	}
 
 	public String getTrainings () {
 		String retVal = "";
 		for (int i=0; i<trTypes.length; i++) {
 			if (trTypes[i] > 0 || osmosis[i] > 0) {
 				if (!retVal.equals(""))
 					retVal += "|";
 				retVal += i + ":";
 				if (trTypes[i] > 0)
 					retVal += trTypes[i];
 				if (osmosis[i] > 0)
 					retVal += "." + osmosis[i];
 			}
 		}
 		return retVal;
 	}
 }
