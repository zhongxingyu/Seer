 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package it.redoddity.portfolios.dao;
 
 import it.redoddity.dao.BaseDAO;
 import it.redoddity.model.Model;
 import it.redoddity.portfolios.model.Project;
 import it.redoddity.portfolios.model.User;
 import it.redoddity.utils.DatabaseConnectionInfo;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  *
  * @author madchicken
  */
 @Repository
 public class ProjectDAO extends BaseDAO {
 
     private UserDAO userDAO;
     
     @Autowired
     public ProjectDAO(DatabaseConnectionInfo db) {
         super(Project.class, db);
         Project.setDao(this);
     }
 
     @Autowired
     public void setUserDAO(UserDAO userDAO) {
         this.userDAO = userDAO;
     }
     
     public List<Project> findAll() {
         try {
             List<Map<String, Object>> projects = select("select * from project");
             return resultToProjectList(projects);
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public List<Project> findUserProjects(User user) {
         try {
             List<Map<String, Object>> projects = select("SELECT * FROM project WHERE id IN (select project_id from user_projects where user_id = ?)", user.getId());
             return resultToProjectList(projects);
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public List<Project> findLastProjects(int from,int to){
         try {
             List<Map<String, Object>> projects = select("select * from project order by updatedAt desc limit "+from+","+to);
             return resultToProjectList(projects);
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public List<User> findProjectCollaborators(Project project){
         try {
             List<Map<String, Object>> collaborators = select("select * from user join user_projects on user.id = user_projects.user_id where user_projects.project_id =?",project.getId());
             List<User> list = new ArrayList<>();
             for (int i = 0; i < collaborators.size(); i++) {
                 Map<String, Object> obj = collaborators.get(i);
                 User user = new User(obj);
                 list.add(user);
             }
             return list;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
      
     }
     
     @Override
     public void create(Model project) throws SQLException{
         super.create(project);
         execute("insert into user_projects(user_id, project_id) values(?,?)", 
                 ((Project)project).getLeaderId() , project.getId());
     }
     
     public void addCollaborator(Project project, User user) throws SQLException{
         execute("insert into user_projects(user_id, project_id) values(?,?)", 
                 user.getId() , project.getId());
     }
     
     public User findLeaderById(String leaderId) {
         return userDAO.findById(leaderId);
     }
     
     public Project findByName(String name) {
         try {
             List<Map<String, Object>> projects = select("select * from project where name = ?", name);
             return projects.size() == 1 ? resultToProjectList(projects).get(0) : null;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public Project findById(String id) {
         try {
             List<Map<String, Object>> project = select("SELECT * FROM project where id = ?", id);
             return project.size() == 1 ? resultToProjectList(project).get(0) : null;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public boolean exists(Project project) {
         return findByName(project.getName()) != null;
     }
 
     private List<Project> resultToProjectList(List<Map<String, Object>> projects) {
         List<Project> list = new ArrayList<>();
         for (int i = 0; i < projects.size(); i++) {
             Map<String, Object> obj = projects.get(i);
             Project project = new Project(obj);
             list.add(project);
         }
         return list;
     }
 }
