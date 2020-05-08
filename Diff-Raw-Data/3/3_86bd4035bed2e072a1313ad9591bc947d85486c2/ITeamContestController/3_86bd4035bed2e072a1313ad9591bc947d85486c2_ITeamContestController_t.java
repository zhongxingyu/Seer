 package svm.logic.abstraction.controller;
 
import svm.logic.abstraction.transferobjects.ITransferMatch;
import svm.logic.abstraction.transferobjects.ITransferTeam;

 /**
  * ProjectTeam: Team C
  * Date: 30.10.12
  */
 public interface ITeamContestController extends IController {
 
     void addMatch(ITransferTeam home, ITransferTeam away);
 
     void addResult(ITransferMatch match, Integer home, Integer away);
 
     void addTeam(ITransferTeam team);
 
     void removeTeam(ITransferTeam team);
 
 }
