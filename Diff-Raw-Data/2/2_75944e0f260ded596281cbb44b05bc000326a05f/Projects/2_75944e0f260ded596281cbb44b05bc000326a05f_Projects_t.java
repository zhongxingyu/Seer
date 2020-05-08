 package de.objectcode.time4u.server.web.rest.secure;
 
 import javax.naming.InitialContext;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import de.objectcode.time4u.server.api.IProjectService;
 import de.objectcode.time4u.server.api.data.FilterResult;
 import de.objectcode.time4u.server.api.data.Project;
 import de.objectcode.time4u.server.api.data.ProjectSummary;
 import de.objectcode.time4u.server.api.filter.ProjectFilter;
 
 public class Projects
 {
   private final static Log LOG = LogFactory.getLog(Projects.class);
 
   private final IProjectService m_projectService;
 
   public Projects()
   {
     try {
       final InitialContext ctx = new InitialContext();
 
       m_projectService = (IProjectService) ctx.lookup("time4u-server/ProjectService/remote");
     } catch (final Exception e) {
       LOG.error("Exception", e);
       throw new RuntimeException("Inizialize failed", e);
     }
   }
 
   @GET
   @Path("/")
   @Produces("text/xml")
   public FilterResult<? extends ProjectSummary> getProjectSummaries(@QueryParam("active") final Boolean active,
       @QueryParam("deleted") final Boolean deleted, @QueryParam("minRevision") final Long minRevision,
       @QueryParam("maxRevision") final Long maxRevision, @QueryParam("full") final boolean full,
       @QueryParam("deep") final boolean deep)
   {
     final ProjectFilter filter = new ProjectFilter();
     filter.setActive(active);
     filter.setDeleted(deleted);
     filter.setMinRevision(minRevision);
     filter.setMaxRevision(maxRevision);
     if (!deep) {
      filter.setParentProject("");
     }
 
     if (full) {
       return m_projectService.getProjects(filter);
     }
     return m_projectService.getProjectSumaries(filter);
   }
 
   @Path("/{id}")
   public ProjectResource getProject(@PathParam("id") final String projctId)
   {
     final Project project = m_projectService.getProject(projctId);
 
     if (project == null) {
       return null;
     }
 
     return new ProjectResource(project);
   }
 }
