 package net.idea.restnet.user.alerts.resource;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import net.idea.modbcum.i.IQueryRetrieval;
 import net.idea.modbcum.i.exceptions.DbAmbitException;
 import net.idea.modbcum.r.QueryReporter;
 import net.idea.restnet.db.QueryURIReporter;
 import net.idea.restnet.user.alerts.db.DBAlert;
 
 import org.restlet.Context;
 import org.restlet.Request;
 import org.restlet.data.Reference;
 
 public class AlertJSONReporter <Q extends IQueryRetrieval<DBAlert>>  extends QueryReporter<DBAlert,Q,Writer>  {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4566136103208284105L;
 	protected String comma = null;
 	protected Request request;
 	protected QueryURIReporter uriReporter;
 	
 	public Request getRequest() {
 		return request;
 	}
 	public void setRequest(Request request) {
 		this.request = request;
 	}
 	protected Reference baseReference;
 	public Reference getBaseReference() {
 		return baseReference;
 	}
 
 	public AlertJSONReporter(Request request) {
 		this.baseReference = (request==null?null:request.getRootRef());
 		setRequest(request);
 		uriReporter = new AlertURIReporter<IQueryRetrieval<DBAlert>>();
 	
 	}	
 
	private static String format = "\n{\n\t\"uri\":\"%s\",\n\t\"id\": %s,\n\t\"title\": \"%s\",\n\t\"type\": \"%s\",\n\t\"content\": \"%s\",\n\t\"frequency\": \"%s\",\n\t\"interval\": %d,\n\t\"created\": %s,\n\t\"sent\": %s\n}";
 
 	@Override
 	public Object processItem(DBAlert alert) throws Exception {
 		try {
 			if (comma!=null) getOutput().write(comma);
 			
 			String uri = alert.getID()>0?uriReporter.getURI(alert):"";
 			
 			getOutput().write(String.format(format,
 					uri,
 					(alert.getID()>0)?String.format("\"A%d\"",alert.getID()):null,
 					alert.getTitle()==null?"":alert.getTitle(),
 					alert.getQuery().getType().name(),
 					alert.getQuery().getContent()==null?"":alert.getQuery().getContent(),
 					alert.getRecurrenceFrequency().name(),
 					alert.getRecurrenceInterval(),
 					alert.getCreated(),
 					alert.getSentAt()
 					));
 			/*
 			
 			if ((alert.getUser()!=null) && (alert.getUser().getResourceURL()!=null)) {
 				Resource user = toAddTo.createResource(alert.getUser().getResourceURL().toString());
 				toAddTo.add(user, RDF.type, NCAL.Attendee);
 				toAddTo.add(user, RDF.type, TOXBANK.USER);
 				toAddTo.add(res,NCAL.attendee,user);
 			}			
 			*/
 			comma = ",";
 		} catch (IOException x) {
 			Context.getCurrentLogger().severe(x.getMessage());
 		}
 		return null;
 	}	
 	@Override
 	public void footer(Writer output, Q query) {
 		try {
 			output.write("\n]\n}");
 		} catch (Exception x) {}
 	};
 	@Override
 	public void header(Writer output, Q query) {
 		try {
 			output.write("{\"alert\": [");
 		} catch (Exception x) {}
 		
 	};
 	
 	public void open() throws DbAmbitException {
 		
 	}
 	@Override
 	public void close() throws Exception {
 		setRequest(null);
 		super.close();
 	}
 	@Override
 	public String getFileExtension() {
 		return null;
 	}
 }
