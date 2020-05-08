 /**
  * 
  */
 package ch.demo.web;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.Response;
 
 import org.slf4j.Logger;
 
 import ch.demo.business.service.StudentService;
 import ch.demo.business.service.StudentStatisticsService;
 import ch.demo.dom.Student;
 import dto.DistributionDto;
 import dto.StudentsDto;
 
 /**
  * @author hostettler
  * 
  */
 @SessionScoped
 @Path("/studentService")
 public class StudentServiceFacade implements Serializable {
 
 	private static final long serialVersionUID = 1318211294294344900L;
 
 	@EJB
	StudentService studentService;
 
 	@EJB
	StudentStatisticsService studentServiceStatistics;
 
 	@Inject
	Logger logger;
 
 	@GET
 	@Produces({ "application/xml", "application/json" })
 	@Path("session")
 	public Response getSessionStatistics() {
 		return Response.ok(studentServiceStatistics.getStatistics()).build();
 	}
 
 	@GET
 	@Produces({ "application/xml", "application/json" })
 	@Path("overall")
 	public Response getOverallStatitics() {
 		return Response.ok(studentService.getStatistics()).build();
 	}
 
 	@GET
 	@Produces({ "application/xml", "application/json" })
 	@Path("clean")
 	public Response clean() {
 		studentServiceStatistics.clean();
 		return Response.ok().build();
 	}
 
 	
 	@GET
 	@Produces({ "application/xml", "application/json" })
 	@Path("all")
 	public Response getStudents() {
 		List<Student> students = studentService.getAll();
 		return Response.ok(new StudentsDto(students)).build();
 	}
 	
 	@GET
 	@Produces({ "application/xml", "application/json" })
 	@Path("distribution")
 	public Response getDistribution() {
 		Integer[] distrib = studentService.getDistribution(10);
 		studentServiceStatistics.count();
 		return Response.ok(new DistributionDto(distrib)).build();
 	}
 }
