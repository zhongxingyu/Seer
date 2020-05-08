 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package sportsclubmanager.controller;
 
 import java.util.*;
 import java.util.logging.*;
 import sportsclubmanager.controller.contract.*;
 import sportsclubmanager.domain.*;
 import sportsclubmanager.dto.classes.Trainer;
 import sportsclubmanager.dto.contract.ITrainer;
 
 /**
 
  @author Thomas
  */
 public class TrainerController
         implements IController<ITrainer>
 {
     private static TrainerController controller;
 
     public TrainerController()
     {
     }
 
     public static IController<ITrainer> getInstance()
     {
         if (controller == null)
         {
             controller = new TrainerController();
         }
 
         return controller;
     }
 
     public sportsclubmanager.domain.contract.ITrainer getDomainById(Integer id)
             throws IdNotFoundException
     {
         for (sportsclubmanager.domain.contract.ITrainer a : DomainFacade.getAll(sportsclubmanager.domain.contract.ITrainer.class))
         {
             return a;
         }
 
         throw new IdNotFoundException();
     }
 
     @Override
     public ITrainer getById(Integer id)
             throws IdNotFoundException
     {
         for (sportsclubmanager.domain.contract.ITrainer a : DomainFacade.getAll(sportsclubmanager.domain.contract.ITrainer.class))
         {
             if (a.getId() == id)
             {
                 return Trainer.copy(a);
             }
         }
 
         throw new IdNotFoundException();
     }
 
     @Override
     public List<ITrainer> getAll()
     {
         List<ITrainer> result = new LinkedList<>();
 
         for (sportsclubmanager.domain.contract.ITrainer a : DomainFacade.getAll(sportsclubmanager.domain.contract.ITrainer.class))
         {
             result.add(Trainer.copy(a));
         }
 
         return result;
     }
 
     @Override
     public Integer set(ITrainer value)
     {
         Integer returnv = 0;
         try
         {
             sportsclubmanager.domain.classes.Trainer trainer = new sportsclubmanager.domain.classes.Trainer(value.getId());
 
             List<sportsclubmanager.domain.contract.IClubTeam> clubTeamList = new LinkedList<>();
             for (int i : value.getClubTeamList())
             {
                 clubTeamList.add(new ClubTeamController().getDomainById(i));
             }
             trainer.setClubTeamList(clubTeamList);
 
             returnv = DomainFacade.set(trainer);
         }
         catch (IdNotFoundException | CouldNotSaveException ex)
         {
             Logger.getLogger(AddressController.class.getName()).log(Level.SEVERE, null, ex);
         }
         finally
         {
             return returnv;
         }
     }
 
     @Override
     public void delete(ITrainer value)
     {
         try
         {
             sportsclubmanager.domain.classes.Trainer typeofsport = createDomain(value);
 
             DomainFacade.delete(typeofsport);
         }
         catch (IdNotFoundException | CouldNotDeleteException ex)
         {
             Logger.getLogger(AddressController.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private sportsclubmanager.domain.classes.Trainer createDomain(ITrainer value)
             throws IdNotFoundException
     {
         sportsclubmanager.domain.classes.Trainer trainer = new sportsclubmanager.domain.classes.Trainer(value.getId());
 
         List<Integer> clubTeams = value.getClubTeamList();
         List<Integer> permisssions = value.getPermisssionList();
 
         LinkedList<sportsclubmanager.domain.contract.IClubTeam> teams = new LinkedList<>();
         for (int id : clubTeams)
         {
             teams.add(new ClubTeamController().getDomainById(id));
         }
         trainer.setClubTeamList(teams);
 
         LinkedList<sportsclubmanager.domain.contract.IPermission> p = new LinkedList<>();
 
        for (int id : permisssions)
         {
             p.add(new PermissionController().getDomainById(id));
         }
         trainer.setPermisssionList(p);

         return trainer;
     }
 }
