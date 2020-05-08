 package cz.cvut.fit.mi_mpr_dip.admission.endpoint;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationContextAware;
 import org.springframework.roo.addon.javabean.RooJavaBean;
 import org.springframework.security.access.annotation.Secured;
import org.springframework.transaction.annotation.Transactional;
 
 import cz.cvut.fit.mi_mpr_dip.admission.builder.AdmissionsBuilder;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admission;
 import cz.cvut.fit.mi_mpr_dip.admission.domain.Admissions;
 import cz.cvut.fit.mi_mpr_dip.admission.endpoint.helper.AdmissionEndpointHelper;
 import cz.cvut.fit.mi_mpr_dip.admission.endpoint.helper.EndpointHelper;
 import cz.cvut.fit.mi_mpr_dip.admission.jbpm.ProcessService;
 import cz.cvut.fit.mi_mpr_dip.admission.service.UserIdentityService;
 import cz.cvut.fit.mi_mpr_dip.admission.service.deduplication.DeduplicationService;
 import cz.cvut.fit.mi_mpr_dip.admission.util.StringPool;
 import cz.cvut.fit.mi_mpr_dip.admission.validation.AdmissionCodeValidator;
 import cz.cvut.fit.mi_mpr_dip.admission.validation.AnnotatedBeanValidator;
 
 @RooJavaBean
 @Path(AdmissionProcessingEndpoint.ENDPOINT_PATH)
 public class AdmissionProcessingEndpoint implements ProcessingEndpoint, ApplicationContextAware {
 
 	public static final String ADMISSION_PATH = "/admission";
 	public static final String ADMISSIONS_PATH = "/admissions";
 	public static final String ENDPOINT_PATH = "/processing";
 
 	@Autowired
 	private AdmissionCodeValidator admissionCodeValidator;
 
 	@Autowired
 	private AnnotatedBeanValidator beanValidator;
 
 	@Autowired
 	private EndpointHelper endpointHelper;

 	@Autowired
 	private ProcessService processService;
 
 	@Autowired
 	@Qualifier("admissionDeduplicationService")
 	private DeduplicationService<Admission> deduplicationService;
 
 	@Autowired
 	private UserIdentityService userIdentityService;
 
 	private ApplicationContext applicationContext;
 
 	@Path(AdmissionEndpointHelper.IDENTITY_PATH)
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@GET
 	@Override
 	public Response getUserIdentity() {
 		return getEndpointHelper().getUserIdentity();
 	}
 
 	@Secured("PERM_READ_ADMISSION")
 	@Path(ADMISSION_PATH + "/{admissionCode}")
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@GET
 	@Override
 	public Response getAdmission(@PathParam("admissionCode") String admissionCode) {
 		return getEndpointHelper().getAdmission(admissionCode);
 	}
 
 	@Secured("PERM_READ_ADMISSIONS")
 	@Path(ADMISSIONS_PATH)
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@GET
 	@Override
 	public Admissions getAdmissions(@QueryParam("count") Integer count, @QueryParam("page") Integer page) {
 		return buildAdmissions(count, page);
 	}
 
 	private Admissions buildAdmissions(Integer count, Integer page) {
 		AdmissionsBuilder admissionsBuilder = getAdmissionsBuilder();
 
 		admissionsBuilder.createNew();
 		admissionsBuilder.buildLimit(count, page);
 		admissionsBuilder.buildAdmissions();
 
 		return admissionsBuilder.get();
 	}
 
 	private AdmissionsBuilder getAdmissionsBuilder() {
 		return getApplicationContext().getBean(AdmissionsBuilder.class);
 	}
 
 	@Secured("PERM_WRITE_ADMISSION")
 	@Path(ADMISSION_PATH)
 	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@POST
 	@Override
 	public Response addAdmission(Admission admission) throws URISyntaxException {
 		validateAndDeduplicateAndStore(admission);
 		URI uri = new URI(ENDPOINT_PATH + ADMISSION_PATH + StringPool.SLASH + admission.getCode());

 		return Response.created(uri).build();
 	}
 
 	@Secured("PERM_WRITE_ADMISSION")
 	@Path(ADMISSION_PATH + "/{admissionCode}")
 	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@PUT
 	@Override
 	public Response updateAdmission(Admission admission) {
 		// TODO:
 		return null;
 	}
 
	@Transactional
 	private void validateAndDeduplicateAndStore(Admission admission) {
 		validate(admission);
 		deduplicateAndStore(admission);
 		runJbpmProcess(admission);
 	}
 
 	private void validate(Admission admission) {
 		getBeanValidator().validate(admission);
 		getAdmissionCodeValidator().validate(admission);
 	}
 
 	private void deduplicateAndStore(Admission admission) {
 		getUserIdentityService().buildUserIdentity(admission);
 		getDeduplicationService().deduplicateAndStore(admission);
 	}

 	private void runJbpmProcess(Admission admission) {
 		getProcessService().runProcess(admission);
 	}
 
 	@Secured("PERM_DELETE_ADMISSION")
 	@Path(ADMISSION_PATH + "/{admissionCode}")
 	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
 	@DELETE
 	@Override
 	public Response deleteAdmission(@PathParam("admissionCode") String admissionCode) {
 		return getEndpointHelper().deleteAdmission(admissionCode);
 	}
 
 	@Override
 	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
 		this.applicationContext = applicationContext;
 	}
 
 }
