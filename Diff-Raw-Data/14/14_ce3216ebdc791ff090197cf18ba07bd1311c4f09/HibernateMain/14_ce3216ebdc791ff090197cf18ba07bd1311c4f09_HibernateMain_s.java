 
 import domain.CouldNotFetchException;
 import domain.DomainFacade;
 import domain.classes.Role;
 import domain.contract.IMember;
 import domain.contract.IRole;
 import java.util.List;
 
 /**
  *
  * @author Markus Mohanty <markus.mo at gmx.net>
  */
 public class HibernateMain {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) 
     {
         try
         {
             IMember m  = DomainFacade.getInstance().getMemberByUsername("mmo7528");
             List<IRole> roleList = m.getRoleList();
         }
         catch (CouldNotFetchException ex)
         {
             System.out.println(ex.getMessage());
         }
     }
 
 }
