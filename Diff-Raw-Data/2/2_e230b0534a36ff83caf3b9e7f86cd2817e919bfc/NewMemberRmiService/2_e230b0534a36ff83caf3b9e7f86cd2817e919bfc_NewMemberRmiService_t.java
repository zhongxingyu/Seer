 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package server.rmi.controller;
 
 import contract.dto.*;
 import contract.rmi.services.INewMemberRmiService;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.List;
 import server.useCaseController.NewMember;
 
 /**
  *
  * @author Lins Christian (christian.lins87@gmail.com)
  */
 public class NewMemberRmiService extends UnicastRemoteObject implements INewMemberRmiService
 {
 
     public NewMemberRmiService() throws RemoteException {
         super();
     }
     
     @Override
     public void setNewMember(IMember member, IAddress address) throws RemoteException
     {
         NewMember.getInstance().setNewMember(member, address);
     }
 
     @Override
     public List<IDepartment> getDepartments()
     {
         return NewMember.getInstance().getDepartments();
     }
 
     @Override
     public List<IClubTeam> getClubTeams(List<Integer> clubTeams) throws RemoteException
     {
         return NewMember.getInstance().getClubTeams(clubTeams);
     }
 
     @Override
     public void setNewMember(IMember member, IAddress address, IDepartment department, IClubTeam clubTeam, IRole role) throws RemoteException
     {
         NewMember.getInstance().setNewMember(member, address, department, clubTeam, role);
     }
 
     @Override
     public List<ITypeOfSport> getTypeOfSports(List<Integer> typOfSportsList) throws RemoteException
     {
         return NewMember.getInstance().getTypeOfSports(typOfSportsList);
     }
 
     @Override
     public List<IClubTeam> getClubTeamsByTypeOfSport(ITypeOfSport sport) throws RemoteException
     {
        return NewMember.getInstance().getClubTeamsByTypeOfSport(sport);
     }
     
 }
