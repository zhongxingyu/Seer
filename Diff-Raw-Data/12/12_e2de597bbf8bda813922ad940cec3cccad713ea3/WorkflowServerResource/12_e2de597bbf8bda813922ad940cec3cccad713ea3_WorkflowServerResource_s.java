 package com.kdcloud.server.rest.resource;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.logging.Level;
 
 import org.restlet.Application;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.Post;
 
 import com.kdcloud.lib.domain.Report;
 import com.kdcloud.lib.domain.ServerParameter;
 import com.kdcloud.lib.rest.api.WorkflowResource;
 import com.kdcloud.lib.rest.ext.InstancesRepresentation;
 
 public class WorkflowServerResource extends WorkerServerResource implements
 		WorkflowResource {
 
 	private String workflowId;
 
 	public WorkflowServerResource() {
 	}
 
 	public WorkflowServerResource(Application application, String workflowId) {
 		super(application);
 		this.workflowId = workflowId;
 	}
 	
 	@Override
 	public Representation handle() {
 		workflowId = getParameter(ServerParameter.WORKFLOW_ID);
 		return super.handle();
 	}
 
 	@Override
 	@Post
 	public Representation execute(Form form) {
 		InputStream workflow = getClass().getClassLoader().getResourceAsStream(workflowId);
 		if (workflow == null)
 			return notFound();
 		Report report;
 		try {
 			report = execute(form, workflow);
 		} catch (IOException e) {
 			getLogger().log(Level.SEVERE, e.getMessage(), e);
 			setStatus(Status.SERVER_ERROR_INTERNAL);
 			return null;
 		}
 		if (report.getDom() != null)
 			return new DomRepresentation(MediaType.APPLICATION_XML, report.getDom());
		if (report.getData() != null)
 			return new InstancesRepresentation(MediaType.TEXT_CSV, report.getData());
 		return null;
 	}
 
 }
