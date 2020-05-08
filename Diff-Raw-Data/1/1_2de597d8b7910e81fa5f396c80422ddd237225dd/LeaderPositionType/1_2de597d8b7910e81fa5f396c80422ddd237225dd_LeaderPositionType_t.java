 package scoutcert;
 
 import static scoutcert.ScoutUnitType.*;
 
 /**
  * User: eric
  * Date: 3/30/11
  * Time: 10:39 PM
  */
 public enum LeaderPositionType {
     CharterRep("CR", Troop, Pack, Crew, Team),
     CommitteeChair("CC", Troop, Pack, Crew, Team),
     CommitteeMember("MC", Troop, Pack, Crew, Team),
     Executive("EX"),
     Volunteer("VO"),
    MeritBadgeCouncilor("MB"),
     Scoutmaster("SM", Troop),
     AssistantScoutMaster("SA", Troop),
     Cubmaster("CM", Pack),
     AssistantCubmaster("CA", Pack),
     TigerLeader("TL", Pack),
     DenLeader("DL", Pack),
     WebelosLeader("WL", Pack),
     AssistantDenLeader("DA", Pack),
     AssistantWebelosLeader("WA", Pack),
     VarsityCoach("VC", Team),
     AssistantVarsityCoach("VA", Team),
     CrewAdvisor("NL", Crew),
     AssistantCrewAdvisor("NA", Crew);
 
 
 
     LeaderPositionType(String code, ScoutUnitType... scoutUnitTypes) {
         this.code = code;
         this.scoutUnitTypes = scoutUnitTypes;
     }
 
     public final ScoutUnitType[] scoutUnitTypes;
     public final String code;
 }
