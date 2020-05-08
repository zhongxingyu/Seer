 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package roman.cwk.managedbeans;
 
 import java.util.List;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import roman.cwk.entity.Organization;
 import roman.cwk.entity.Project;
 import roman.cwk.sessionbeans.OrganizationFacade;
 import roman.cwk.sessionbeans.ProjectFacade;
 
 /**
  *
  * @author Roman Macor
  */
 @ManagedBean
 //@RequestScoped
 @SessionScoped
 public class ProjectBean {
 
     private Project project;
     @EJB
     private ProjectFacade ejbProjectFacade;
     @EJB
     private OrganizationFacade ejbOrganizationFacade;
 //    @ManagedProperty(value="#{registrationBean}")
 //    private RegistrationBean registration;
 
     /**
      * Creates a new instance of ProjectBean
      */
     public ProjectBean() {
     }
 
     public Project getProject() {
         if (project == null) {
             project = new Project();
         }
         return project;
     }
 
     public void setProject(Project project) {
         this.project = project;
     }
 
     public String getLogedUsername() {
         String userName = FacesContext.getCurrentInstance().getExternalContext().getRemoteUser();
         return userName;
     }
 
     public String create() {
         String organizationName = this.getLogedUsername();
         Organization organization = ejbOrganizationFacade.find(organizationName);
         project.setOrganization(organization);
         ejbProjectFacade.create(project);
        return "confirmation";
     }
 
     public List<Project> getAllProjects() {
         return ejbProjectFacade.findAll();
     }
 
     public List<Project> getProjectsForLogedUser() {
         String organizationName = this.getLogedUsername();
         return ejbProjectFacade.findProjectsForLogedinUser(organizationName);
     }
 
     public String prepareDetail(Long id) {
         project = ejbProjectFacade.find(id);
         return "detail";
     }
 
     public String prepareCreate() {
         project = new Project();
         return "/project/create";
     }
 
     public String detele(Project project) {
         ejbProjectFacade.remove(project);
         return "/project/list";
     }
 
     public String prepareEdit(Project project) {
         this.project = project;
         return "/project/edit";
     }
     public String edit(){
         ejbProjectFacade.edit(project);
         return "/project/list";
     }
 }
