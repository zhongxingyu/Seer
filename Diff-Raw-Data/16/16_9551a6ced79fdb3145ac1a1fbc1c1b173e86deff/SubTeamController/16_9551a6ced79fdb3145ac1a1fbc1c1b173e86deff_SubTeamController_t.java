 package svm.logic.implementation.controller;
 
 import svm.domain.abstraction.DomainFacade;
 import svm.domain.abstraction.exception.DomainException;
 import svm.domain.abstraction.modelInterfaces.*;
 import svm.logic.abstraction.controller.ISubTeamController;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.exception.LogicException;
 import svm.logic.abstraction.exception.NotAllowException;
 import svm.logic.abstraction.transferobjects.IHasModel;
 import svm.logic.abstraction.transferobjects.ITransferAuth;
 import svm.logic.abstraction.transferobjects.ITransferMember;
 import svm.logic.abstraction.transferobjects.ITransferSubTeam;
 import svm.logic.implementation.tranferobjects.TransferMember;
 import svm.logic.implementation.tranferobjects.TransferSubTeam;
 import svm.logic.implementation.transferobjectcreator.TransferObjectCreator;
 import svm.persistence.abstraction.exceptions.ExistingTransactionException;
 import svm.persistence.abstraction.exceptions.NoSessionFoundException;
 import svm.persistence.abstraction.exceptions.NoTransactionException;
 import svm.persistence.abstraction.exceptions.NotSupportedException;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * ProjectTeam: Team C
  * Date: 01.11.12
  */
 public class SubTeamController implements ISubTeamController {
 
     private ITeam team;
     private IContest contest;
 
     private ISubTeam subTeam;
     private ITransferSubTeam transferSubTeam;
     private Integer sessionId;
     private ITransferAuth user;
 
     public SubTeamController(ITeam team, IContest contest, ITransferAuth user) throws NoSessionFoundException, IllegalAccessException, InstantiationException, NotSupportedException {
         this.team = team;
         this.contest = contest;
         this.user = user;
     }
 
     @Override
     public ITransferSubTeam getSubTeam() {
         return transferSubTeam;
     }
 
     @Override
     public List<ITransferMember> getMemberOfTeam() throws IllegalGetInstanceException {
         List<ITransferMember> members = new LinkedList<ITransferMember>();
         for (IMember member : subTeam.getTeam().getMembers()) {
             members.add((ITransferMember) TransferObjectCreator.getInstance(TransferMember.class, member));
         }
         return members;
     }
 
     @Override
     public List<ITransferMember> getMembersOfSubTeam() throws IllegalGetInstanceException {
         List<ITransferMember> members = new LinkedList<ITransferMember>();
         for (ISubTeamsHasMembers member : subTeam.getSubTeamMembers()) {
             members.add((ITransferMember) TransferObjectCreator.getInstance(TransferMember.class, member.getMember()));
         }
         return members;
     }
 
     @Override
     public void setName(String name) throws NotAllowException {
         if (!user.isAllowedForContestSubTeamChanging())
             throw new NotAllowException("Wrong privilege");
         subTeam.setName(name);
     }
 
     @Override
     public void addMember(ITransferMember member) throws LogicException, NoSessionFoundException, DomainException, IllegalAccessException, InstantiationException, NotSupportedException, NotAllowException {
         if (!user.isAllowedForContestSubTeamChanging())
             throw new NotAllowException("Wrong privilege");
 
         IMember m = null;
         IMember toSearch = ((IHasModel<IMember>) member).getModel();
         for (IMember x : subTeam.getTeam().getMembers()) {
             if (x.equals(toSearch)) m = x;
         }
 
         if (m != null)
             this.subTeam.addMember(m);
         else
             System.out.println("NULL addMember [subTeam]");
     }
 
     @Override
     public void removeMember(ITransferMember member) throws NotAllowException {
         if (!user.isAllowedForContestSubTeamChanging())
             throw new NotAllowException("Wrong privilege");
 
        System.out.println("subteamcontroller remove member start");

         IMember m = null;
         IMember toSearch = ((IHasModel<IMember>) member).getModel();
//        for (IMember x : subTeam.getTeam().getMembers()) {
//            if (x.equals(toSearch))
//                m = x;
//        }

        for (ISubTeamsHasMembers x : subTeam.getSubTeamMembers()) {
            if (x.getMember().equals(toSearch))
                m = x.getMember();
         }
 
         if (m != null) {
             this.subTeam.removeMember(m);
            System.out.println("subteamcontroller remove member finsh");
         } else
             System.out.println("NULL removeMember [subTeam]");
     }
 
     @Override
     public void start() throws NoSessionFoundException, IllegalGetInstanceException, NotSupportedException, InstantiationException, IllegalAccessException {
         this.sessionId = DomainFacade.generateSessionId();
         try {
             this.subTeam = DomainFacade.getSubTeamModelDAO().get(sessionId, team, contest);
         } catch (DomainException e) {
             e.printStackTrace();
             throw new NotSupportedException();
         }
         DomainFacade.reattachObjectToSession(sessionId, this.subTeam);
         try {
             DomainFacade.reattachObjectToSession(sessionId, this.subTeam.getTeam());
             DomainFacade.reattachObjectToSession(sessionId, this.subTeam.getContest());
         } catch (Exception e) {
             System.out.println("ERROR");
         }
         this.transferSubTeam = (ITransferSubTeam) TransferObjectCreator.getInstance(TransferSubTeam.class, subTeam);
     }
 
     @Override
     public void commit() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException {
         DomainFacade.startTransaction(sessionId);
         DomainFacade.getSubTeamModelDAO().saveOrUpdate(sessionId, this.subTeam);
         DomainFacade.commitTransaction(sessionId);
         DomainFacade.closeSession(sessionId);
     }
 
     @Override
     public void abort() throws ExistingTransactionException, NoSessionFoundException, NoTransactionException {
         DomainFacade.closeSession(sessionId);
     }
 }
