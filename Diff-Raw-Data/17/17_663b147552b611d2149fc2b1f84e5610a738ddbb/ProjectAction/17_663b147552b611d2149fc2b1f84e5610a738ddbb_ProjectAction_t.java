 package nl.bhit.webapp.action;
 
 import com.opensymphony.xwork2.Preparable;
 import nl.bhit.service.GenericManager;
 import nl.bhit.dao.SearchException;
 import nl.bhit.model.Company;
 import nl.bhit.model.Project;
 import nl.bhit.webapp.action.BaseAction;
 
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
 import java.util.List;
 
 public class ProjectAction extends BaseAction implements Preparable {
     private GenericManager<Project, Long> projectManager;
     private GenericManager<Company, Long> companyManager;
     private List projects;
     private List companies;
     private Project project;
     private Long id;
     private String query;
 
     public void setProjectManager(GenericManager<Project, Long> projectManager) {
         this.projectManager = projectManager;
     }
 
     public void setCompanyManager(GenericManager<Company, Long> companyManager) {
         this.companyManager = companyManager;
     }
     
     public List getProjects() {
         return projects;
     }
 
     /**
      * Grab the entity from the database before populating with request parameters
      */
     public void prepare() {
         if (getRequest().getMethod().equalsIgnoreCase("post")) {
             // prevent failures on new
             String projectId = getRequest().getParameter("project.id");
             if (projectId != null && !projectId.equals("")) {
                 project = projectManager.get(new Long(projectId));
             }
         }
     }
 
     public void setQ(String q) {
         this.query = q;
     }
 
     public String list() {
         try {
             projects = projectManager.search(query, Project.class);
            Collection projectsNew = new LinkedHashSet(projects);
            projects = new ArrayList(projectsNew);
         } catch (SearchException se) {
             addActionError(se.getMessage());
             projects = projectManager.getAll();
         }
         return SUCCESS;
     }
     
     public List getCompanyList(){
     	companies = companyManager.getAll();
     	return companies;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Project getProject() {
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public String delete() {
         projectManager.remove(project.getId());
         saveMessage(getText("project.deleted"));
 
         return SUCCESS;
     }
 
     public String edit() {
         if (id != null) {
             project = projectManager.get(id);
         } else {
             project = new Project();
         }
 
         return SUCCESS;
     }
 
     public String save() throws Exception {
         if (cancel != null) {
             return "cancel";
         }
 
         if (delete != null) {
             return delete();
         }
 
         boolean isNew = (project.getId() == null);
 
         projectManager.save(project);
 
         String key = (isNew) ? "project.added" : "project.updated";
         saveMessage(getText(key));
 
         if (!isNew) {
             return INPUT;
         } else {
             return SUCCESS;
         }
     }
 }
