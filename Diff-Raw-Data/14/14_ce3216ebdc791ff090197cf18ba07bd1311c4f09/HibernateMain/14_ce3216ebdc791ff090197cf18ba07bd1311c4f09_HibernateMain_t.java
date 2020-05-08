 
 import domain.CouldNotFetchException;
 import domain.DomainFacade;
import domain.classes.Department;
 import domain.classes.Role;
import domain.contract.IDepartment;
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
            for(IRole r : roleList)
            {
                System.out.println(r.getName());
            }
            List<Department> all = DomainFacade.getInstance().getAll(Department.class);
            for(Department d : all)
            {
                System.out.println(d.getName());
                System.out.println(d.getDepartmentHead().getName());
            }
         }
         catch (CouldNotFetchException ex)
         {
             System.out.println(ex.getMessage());
         }
     }
 
 }
