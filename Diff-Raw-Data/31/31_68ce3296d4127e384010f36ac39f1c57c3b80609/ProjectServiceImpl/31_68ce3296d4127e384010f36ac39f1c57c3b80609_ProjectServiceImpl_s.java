 package bbmri.serviceImpl;
 
 import bbmri.DAO.ProjectDAO;
 import bbmri.DAO.UserDAO;
 import bbmri.entities.Project;
 import bbmri.entities.ProjectState;
 import bbmri.entities.User;
 import bbmri.service.ProjectService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ori
  * Date: 2.11.12
  * Time: 18:15
  * To change this template use File | Settings | File Templates.
  */
 @Transactional
 @Service
 public class ProjectServiceImpl implements ProjectService {
 
     @Autowired
     private UserDAO userDAO;
 
     @Autowired
     private ProjectDAO projectDAO;
 
     public Project create(Project project, User user) {
         project.setProjectState(ProjectState.NEW);
         projectDAO.create(project);
         User resDB = userDAO.get(user.getId());
         projectDAO.assignUserToProject(resDB, project);
         return project;
     }
 
     public void remove(Long id) {
         Project project = projectDAO.get(id);
         if (project != null) {
             projectDAO.remove(project);
         }
     }
 
     public Project update(Project project) {
         Project projectDB = projectDAO.get(project.getId());
 
         if (projectDB.getProjectState() != ProjectState.NEW &&
                 project.getProjectState() != ProjectState.NEW &&
                 project.getProjectState() != ProjectState.APPROVED) {
             projectDB.setProjectState(project.getProjectState());
         }
         projectDB.setDescription(project.getDescription());
         projectDB.setFundingOrganization(project.getFundingOrganization());
         projectDB.setName(project.getName());
 
         projectDAO.update(projectDB);
         return project;
     }
 
     public List<Project> getAll() {
         List<Project> projects = projectDAO.getAll();
         return projects;
     }
 
     public List<Project> getAllByUser(Long id) {
         User userDB = userDAO.get(id);
         List<Project> projects = projectDAO.getAllByUser(userDB);
         return projects;
     }
 
     public List<Project> getAllWhichUserAdministrate(Long id) {
         User userDB = userDAO.get(id);
         List<Project> projects = projectDAO.getAllByUser(userDB);
 
         List<Project> result = new ArrayList<Project>();
         if (projects != null) {
             for (Project project : projects) {
                 if (project.getUsers().get(0).equals(userDB)) {
                     result.add(project);
                 }
             }
         }
         return result;
     }
 
 
     public List<Project> getAllByProjectState(ProjectState projectState) {
         List<Project> projects = projectDAO.getAllByProjectState(projectState);
         return projects;
     }
 
     public List<Project> getAllApprovedByUser(User user) {
         List<Project> projects = projectDAO.getAllByUser(user);
         List<Project> result = new ArrayList<Project>();
         if (projects != null) {
             for (Project project : projects) {
                 if (project.getProjectState() == ProjectState.APPROVED ||
                         project.getProjectState() == ProjectState.STARTED) {
                     result.add(project);
                 }
             }
         }
         return result;
     }
 
 
     public User assignUser(Long userId, Long projectId) {
         User userDB = userDAO.get(userId);
         Project projectDB = projectDAO.get(projectId);
        if (projectDAO.projectContainsUser(userDB, projectDB) == true) {
             return null;
         }
         projectDAO.assignUserToProject(userDB, projectDB);
         return userDB;
     }
 
     public User removeUserFromProject(Long userId, Long projectId) {
         User userDB = userDAO.get(userId);
         Project projectDB = projectDAO.get(projectId);
        if (projectDAO.projectContainsUser(userDB, projectDB) == false) {
             return null;
         }
         projectDAO.removeUserFromProject(userDB, projectDB);
         return userDB;
     }
 
     public List<User> getAllAssignedUsers(Long projectId) {
         Project projectDB = projectDAO.get(projectId);
         List<User> users = projectDAO.getAllUsersByProject(projectDB);
         return users;
     }
 
     public void approve(Long id) {
         Project projectDB = projectDAO.get(id);
         if (projectDB.getProjectState() == ProjectState.NEW) {
             projectDB.setProjectState(ProjectState.APPROVED);
         }
         projectDB.setDescription("HLAVA 22");
         projectDAO.update(projectDB);
     }
 
     public Project getById(Long id) {
         Project project;
         project = projectDAO.get(id);
         return project;
     }
 
     public List<User> getAllNotAssignedUsers(Long id) {
         List<User> result = new ArrayList<User>();
         Project project;
         project = projectDAO.get(id);
         List<User> allUsers = userDAO.getAll();
         if (allUsers != null) {
             for (User user : allUsers) {
                 if (!user.getProjects().contains(project)) {
                     result.add(user);
                 }
             }
         }
         return result;
     }
 
     public Project changeOwnership(Long projectId, Long newOwnerId) {
         Project project = projectDAO.get(projectId);
         User newOwner = userDAO.get(newOwnerId);
 
         User oldOwner = userDAO.get(project.getOwner().getId());
 
         if (project.getUsers().contains(newOwner)) {
             project.getUsers().remove(oldOwner);
             project.getUsers().remove(newOwner);
             projectDAO.update(project);
             project.getUsers().add(0, newOwner);
             project.getUsers().add(oldOwner);
             projectDAO.update(project);
         }
         return project;
     }
 }
