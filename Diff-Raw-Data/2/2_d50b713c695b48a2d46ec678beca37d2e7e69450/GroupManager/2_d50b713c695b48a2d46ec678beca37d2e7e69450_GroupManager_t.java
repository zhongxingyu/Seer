 package timeSheet.database.manager;
 
 import timeSheet.database.entity.Employee;
 import timeSheet.database.entity.EmployeeGroup;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import java.util.List;
 
 /**
  * User: John Lawrence
  * Date: 12/26/10
  * Time: 11:32 PM
  */
 public class GroupManager {
     private DatabaseManager manager;
     private EntityManager em;
 
     public GroupManager() {
         manager = new DatabaseManager();
         em = manager.getEntityManager();
     }
 
     public String getGroupSelection(Employee employee) {
         List<EmployeeGroup> groupList = getGroupList();
         StringBuilder groupSelectionList = new StringBuilder();
         for (EmployeeGroup group : groupList) {
            if (employee != null && employee.getGroup() != null && employee.getGroup().getId() == group.getId()) {
                 groupSelectionList.append("<option selected=\"selected\" value=").append(group.getId()).append(">").append(group.getName()).append("</option>");
             } else {
                 groupSelectionList.append("<option value=").append(group.getId()).append(">").append(group.getName()).append("</option>");
             }
         }
         return groupSelectionList.toString();
     }
 
     public List<EmployeeGroup> getGroupList() {
         TypedQuery<EmployeeGroup> query = em.createNamedQuery("findAllGroups", EmployeeGroup.class);
         return query.getResultList();
     }
 
     public EmployeeGroup getGroup(Integer id) {
         TypedQuery<EmployeeGroup> query = em.createNamedQuery("findGroupsById", EmployeeGroup.class);
         query.setParameter("id", id);
         return manager.getSingleResult(query);
     }
 
     public EmployeeGroup getGroup(String name) {
         TypedQuery<EmployeeGroup> query = em.createNamedQuery("findGroupsByName", EmployeeGroup.class);
         query.setParameter("name", name);
         return manager.getSingleResult(query);
     }
 
     public EmployeeGroup saveGroup(EmployeeGroup group) {
         EmployeeGroup existingGroup = getGroup(group.getName());
         if (existingGroup == null) {
             return manager.persist(group);
         } else {
             return null;
         }
     }
 
     public void deleteGroup(int groupId) {
         manager.delete(getGroup(groupId));
     }
 }
