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
             List<Map<String, Object>> projects = select("select * from Project");
             List<Project> list = new ArrayList<>();
             for (int i = 0; i < list.size(); i++) {
                 Map<String, Object> obj = projects.get(i);
                 Project project = new Project(obj);
                 list.add(project);
             }
             return list;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public List<Project> findUserProjects(User user) {
         try {
            List<Map<String, Object>> projects = select("SELECT p.* FROM project p JOIN user_projects up ON (up.user_id = p.leaderId) WHERE p.leaderId = ?", user.getId());
             List<Project> list = new ArrayList<>();
             for (int i = 0; i < projects.size(); i++) {
                 Map<String, Object> obj = projects.get(i);
                 Project project = new Project(obj);
                 list.add(project);
             }
             return list;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 
     public List<Project> findLastProjects(int from,int to){
         try {
             List<Map<String, Object>> projects = select("select * from project order by updatedAt desc limit "+from+","+to);
             List<Project> list = new ArrayList<>();
             for (int i = 0; i < projects.size(); i++) {
                 Map<String, Object> obj = projects.get(i);
                 Project project = new Project(obj);
                 list.add(project);
             }
             return list;
         } catch (SQLException ex) {
             Logger.getLogger(ProjectDAO.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
     
     public List<User> findProjectCollaborators(Project project){
         try {
             List<Map<String, Object>> collaborators = select("select * from user join user_projects on user.id = user_projects.user_id where user_projects.project_id =?",project.getId());
             List<User> list = new ArrayList<>();
             for (int i = 0; i < list.size(); i++) {
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
 }
