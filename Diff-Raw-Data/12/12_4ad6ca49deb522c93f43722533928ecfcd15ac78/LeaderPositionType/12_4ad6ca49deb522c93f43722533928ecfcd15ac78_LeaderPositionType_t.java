 package scoutinghub;
 
 import static scoutinghub.ScoutGroupType.*;
 import static scoutinghub.ScoutUnitType.*;
 
 /**
  * User: eric
  * Date: 3/30/11
  * Time: 10:39 PM
  */
 public enum LeaderPositionType {
     CharterRep("CR", false, Troop, Pack, Crew, Team),
     CommitteeChair("CC", false, Troop, Pack, Crew, Team),
     CommitteeMember("MC", false, Troop, Pack, Crew, Team),
     Executive("EX", false, Council),
    Volunteer("VO", false, Council, District, CharteringOrg, Stake, CommunityUnits),
    Administrator("AD", false, Council, District, CharteringOrg, Stake, CommunityUnits),
     Commissioner("DC", false, District),
     MeritBadgeCounselor("MB", true, District),
     Chairman("DM", false, District),
     Professional("PF", false, Council),
     Scoutmaster("SM", true, Troop),
     AssistantScoutMaster("SA", true, Troop),
     Cubmaster("CM", true, Pack),
    PackTrainer("PT", false, Pack),
     AssistantCubmaster("CA", true, Pack),
     TigerLeader("TL", true, Pack),
     DenLeader("DL", true, Pack),
     WebelosLeader("WL", true, Pack),
     AssistantDenLeader("DA", true, Pack),
     AssistantWebelosLeader("WA", true, Pack),
     VarsityCoach("VC", true, Team),
     AssistantVarsityCoach("VA", true, Team),
     CrewAdvisor("NL", true, Crew),
     AssistantCrewAdvisor("NA", true, Crew);
 
 
 
     LeaderPositionType(String code, boolean directContact, ScoutUnitType... scoutUnitTypes) {
         this.code = code;
         this.directContact = directContact;
         this.scoutUnitTypes = scoutUnitTypes;
         this.scoutGroupTypes = new ScoutGroupType[0];
     }
 
     LeaderPositionType(String code, boolean directContact, ScoutGroupType... scoutUnitTypes) {
         this.code = code;
         this.directContact = directContact;
         this.scoutGroupTypes = scoutUnitTypes;
         this.scoutUnitTypes = new ScoutUnitType[0];
     }
 
     LeaderPositionType(String code, boolean directContact) {
         this.code = code;
         this.directContact = directContact;
         this.scoutGroupTypes = new ScoutGroupType[0];
         this.scoutUnitTypes = new ScoutUnitType[0];
     }
 
     public final ScoutUnitType[] scoutUnitTypes;
     public final ScoutGroupType[] scoutGroupTypes;
     public final String code;
     public final boolean directContact;
 }
