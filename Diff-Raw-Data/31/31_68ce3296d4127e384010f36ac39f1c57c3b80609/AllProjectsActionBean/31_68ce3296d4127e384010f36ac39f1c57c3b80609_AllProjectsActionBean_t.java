 package bbmri.action.Project;
 
 import bbmri.action.BasicActionBean;
 import bbmri.action.MyActionBeanContext;
 import bbmri.entities.Project;
 import bbmri.entities.ProjectState;
 import bbmri.entities.User;
 import bbmri.service.ProjectService;
 import bbmri.service.UserService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 
 import java.util.List;
 
 
 @UrlBinding("/allprojects/{$event}/{project.id}")
 public class AllProjectsActionBean extends BasicActionBean {
 
     @SpringBean
     private UserService userService;
 
     @SpringBean
     private ProjectService projectService;
 
     private Project project;
     private List<Project> projects;
 
     public List<Project> getProjects() {
         projects = projectService.getAll();
         return projects;
     }
 
     public void setProjects(List<Project> projects) {
         this.projects = projects;
     }
 
     public Project getProject() {
         if (project == null)
             project = getContext().getProject();
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     @DefaultHandler
     public Resolution display() {
         getProjects();
         return new ForwardResolution("/project_all.jsp");
     }
 
     public Resolution create() {
         projectService.create(project, getLoggedUser());
         return new RedirectResolution(this.getClass(), "display");
     }
 
     public Resolution edit() {
         project = projectService.getById(project.getId());
         getContext().setProject(project);
 
         return new ForwardResolution("/project_edit.jsp");
     }
 
     public Resolution requestSample() {
         project = projectService.getById(project.getId());
         // you can't request sample for not approved project
 
         if (project.getProjectState() != ProjectState.NEW) {
             getContext().setProject(project);
             return new ForwardResolution("/sample_request.jsp");
         }
         return new ForwardResolution("/project_all.jsp");
     }
 
     public Resolution leave() {
         User user = projectService.removeUserFromProject(getLoggedUser().getId(), project.getId());
         if (user != null) {
             getContext().setLoggedUser(user);
         }
         refreshLoggedUser();
         return new ForwardResolution("/project_all.jsp");
     }
 
     public Resolution join() {
        Project projectDB = projectService.getById(project.getId());
        if(projectDB.getUsers().isEmpty()){
              return new RedirectResolution(this.getClass(), "display");
         }
        projectService.assignUser(getLoggedUser().getId(), project.getId());
         refreshLoggedUser();
         return new RedirectResolution(this.getClass(), "display");
     }
 
      public void refreshLoggedUser(){
         getContext().setLoggedUser(userService.getById(getLoggedUser().getId()));
      }
 }
 
 
