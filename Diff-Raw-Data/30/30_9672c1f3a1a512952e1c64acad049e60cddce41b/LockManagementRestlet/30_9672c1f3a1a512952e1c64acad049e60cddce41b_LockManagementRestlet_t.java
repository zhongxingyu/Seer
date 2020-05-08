 package org.iucn.sis.server.restlets.utils;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.hibernate.Session;
 import org.iucn.sis.server.api.io.AssessmentIO;
 import org.iucn.sis.server.api.io.WorkingSetIO;
import org.iucn.sis.server.api.locking.HibernateLockRepository;
 import org.iucn.sis.server.api.locking.LockException;
 import org.iucn.sis.server.api.locking.LockRepository;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.models.Assessment;
 import org.iucn.sis.shared.api.models.WorkingSet;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.db.CDateTime;
 import com.solertium.db.CInteger;
 import com.solertium.db.CString;
 import com.solertium.db.Row;
 import com.solertium.db.utils.QueryUtils;
 import com.solertium.util.BaseDocumentUtils;
 
 public class LockManagementRestlet extends BaseServiceRestlet {
 	
 	private final LockRepository repository;
 	
 	public LockManagementRestlet(Context context) {
 		super(context);
 		
		repository = new HibernateLockRepository();
		//repository = new PersistentLockRepository();
 	}
 
 	public void definePaths() {
 		paths.add("/management/locks/{protocol}");
 		paths.add("/management/locks/{protocol}/{identifier}");
 	}
 	
 	private String getTable(Request request) throws ResourceException {
 		final String table = (String)request.getAttributes().get("protocol");
 		if (!isValid(table))
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid protocol");
 		
 		return table;
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		try {
 			return doGet(response, getTable(request), session);
 		} catch (LockException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		}
 	}
 
 	private Representation doGet(Response response, final String table, Session session) throws LockException {
 		final List<Row> rows = new ArrayList<Row>();
 		WorkingSetIO workingSetIO = new WorkingSetIO(session);
 		AssessmentIO assessmentIO = new AssessmentIO(session);
 		if ("persistentlock".equals(table)) {
 			for (LockRepository.LockInfo lock : repository.listLocks()) {
 				final Row row = new Row(); 
 				WorkingSet ws = workingSetIO.
 					readWorkingSet(Integer.valueOf(lock.getGroup()));
 				
 				String groupName = ws == null ? lock.getGroup() : ws.getWorkingSetName();
 				
 				Assessment data = assessmentIO.
 					getAssessment(Integer.valueOf(lock.getLockID()));
 				if (data == null)
 					row.add(new CString("species", lock.getLockID().toString()));
 				else
 					row.add(new CString("species", data.getSpeciesName()));
 				row.add(new CString("status", lock.getLockType().toString()));
 				row.add(new CInteger("lockid", lock.getLockID()));
 				row.add(new CString("owner", lock.getUsername()));
 				row.add(new CDateTime("date", new Date(lock.getWhenLockAcquired())));
 				row.add(new CString("groupid", lock.getGroup()));
 				row.add(new CString("groupname", groupName));
 				
 				rows.add(row);
 			}
 		}
 		else {
 			for (Map.Entry<String, List<Integer>> entry : repository.listGroups().entrySet()) {
 				WorkingSet data = workingSetIO.readWorkingSet(Integer.valueOf(entry.getKey()));
 				
 				String groupName;
 				if (data == null)
 					groupName = entry.getKey();
 				else 
 					groupName = data.getWorkingSetName();
 				
 				for (Integer lockID : entry.getValue()) {
 					final Row row = new Row();
 					row.add(new CString("groupid", entry.getKey()));
 					row.add(new CString("groupname", groupName));
 					row.add(new CInteger("persistentlockid", lockID));
 					
 					rows.add(row);
 				}
 			}
 		}
 
 		return new DomRepresentation(
 			MediaType.TEXT_XML, QueryUtils.writeDocumentFromRowSet(rows)
 		);
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
 		try {
 			doDelete(request, response, getTable(request));
 		} catch (LockException e) {
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e.getMessage(), e);
 		}
 	}
 
 	private void doDelete(Request request, Response response, final String table) throws LockException, ResourceException {
 		final String identifier = (String)request.getAttributes().get("identifier");
 		if (identifier == null)
 			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply an identifier");
 		
 		if ("persistentlock".equals(table)) {
 			final Integer id;
 			try {
 				id = Integer.valueOf(identifier);
 			} catch (NumberFormatException e) {
 				e.printStackTrace();
 				response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Please supply a valid identifier");
 				return;
 			}
 			
 			repository.removeLockByID(id);
 
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessment unlocked.")));
 		}
 		else {
 			repository.clearGroup(identifier);
 			
 			response.setStatus(Status.SUCCESS_OK);
 			response.setEntity(new DomRepresentation(MediaType.TEXT_XML, BaseDocumentUtils.impl.createConfirmDocument("Assessments unlocked.")));
 		}
 	}
 	
 	private boolean isValid(String table) {
 		return "persistentlock".equals(table) || 
 			"persistentlockgroup".equals(table);
 	}
 
 }
