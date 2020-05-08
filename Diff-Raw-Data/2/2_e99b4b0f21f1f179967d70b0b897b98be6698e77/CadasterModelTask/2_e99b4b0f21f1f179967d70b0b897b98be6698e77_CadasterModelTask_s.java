 package eu.cadaster.opentox;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Properties;
 
 import net.idea.restnet.c.task.CallableProtectedTask;
 import net.idea.restnet.i.task.TaskResult;
 
 import org.opentox.dsl.task.FibonacciSequence;
 import org.opentox.dsl.task.RemoteTask;
 import org.opentox.rdf.OT;
 import org.opentox.rdf.OpenTox;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Reference;
 import org.restlet.data.Status;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.hp.hpl.jena.ontology.OntModel;
 
 public class CadasterModelTask extends CallableProtectedTask<String> {
 	protected CadasterModel model;
 	protected URL dataURL;
 	protected URL dataserviceURL;
 	protected long timeout = 100000;
 	 
 	protected long pollInterval = 1500;
 	protected long pollTimeout = 10000L*60L*5L; //50 min
 	
 	
 	public long getTimeout() {
 		return timeout;
 	}
 
 	public void setTimeout(long timeout) {
 		this.timeout = timeout;
 	}
 
 	public CadasterModelTask(String token, CadasterModel model, URL dataURL,  URL dataserviceURL) {
 		super(token);
 		this.dataURL = dataURL;
 		this.dataserviceURL = dataserviceURL;
 		this.model = model;
 	}
 
 	@Override
 	public TaskResult doCall() throws Exception {
 		if (dataURL!=null) return process(dataURL);
 		else throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,"No data URI");
 	}
 
 	public TaskResult process(URL url) throws Exception {
 		String datasetService = dataserviceURL==null?getDatasetService():dataserviceURL.toExternalForm();
		if ((datasetService==null) || (!datasetService.startsWith("http")) throw new Exception(String.format("Invalid parameter %s=%s",OpenTox.params.dataset_service,datasetService);
 		OChemSOAPWrapper wrapper = new OChemSOAPWrapper();
 		Long taskID = wrapper.applyModel(model,url);
 		if (taskID>0) {
 			OntModel jenaModel = wrapper.poll(taskID, 100000,url,model);
 			ByteArrayOutputStream o = new ByteArrayOutputStream();
 			OT.write(jenaModel, o, MediaType.APPLICATION_RDF_XML, true);
 			//got the result, now posting to the data service
 			RemoteTask task = new RemoteTask(
 					new Reference(datasetService),
 					MediaType.TEXT_URI_LIST,
 					new StringRepresentation(o.toString(),MediaType.APPLICATION_RDF_XML),
 					Method.POST
 				);
 
    			task = wait4task(task, System.currentTimeMillis());
 			return new TaskResult(task.getResult().toString(),true);
 		}
 		return null;
 	}
 	
 	protected String getDatasetService() throws Exception {
 		try {
 			Properties p = new Properties();
 			InputStream in = getClass().getClassLoader().getResourceAsStream("eu/cadaster/opentox/cadaster.properties");
 			p.load(in);
 			in.close();
 			return p.getProperty("opentox.service.dataset");
 		} catch (Exception x) {
 			throw x;
 		}
 	}
 	protected RemoteTask wait4task(RemoteTask task, long now) throws Exception {
 
 		if (task.getError()!=null) throw task.getError();
 		if (task.getResult()==null) throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,String.format("%s returns empty contend instead of URI"));
 		String result = task.getResult().toString();
 		FibonacciSequence sequence = new FibonacciSequence();
 		while (!task.poll()) {
 			if (task.getError()!=null) throw task.getError();
 			Thread.sleep(sequence.sleepInterval(pollInterval,true,1000 * 60 * 5)); 				
 			Thread.yield();
 			if ((System.currentTimeMillis()-now) > pollTimeout) 
 				throw new ResourceException(Status.SERVER_ERROR_GATEWAY_TIMEOUT,
 						String.format("%s %s ms > %s ms",result==null?task.getUrl():result,System.currentTimeMillis()-now,pollTimeout));
 		}
 		
 		if (task.getError()!=null) 
 			if(task.getError() instanceof ResourceException)
 				throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
 						String.format("%s %d %s",result==null?task.getUrl():result,
 						((ResourceException)task.getError()).getStatus().getCode(),
 						task.getError().getMessage()),
 						task.getError());
 			else
 				throw new ResourceException(Status.SERVER_ERROR_BAD_GATEWAY,
 						String.format("%s %s",result==null?task.getUrl():result,task.getError().getMessage()),
 						task.getError());
 		return task;
 }	 
 }
