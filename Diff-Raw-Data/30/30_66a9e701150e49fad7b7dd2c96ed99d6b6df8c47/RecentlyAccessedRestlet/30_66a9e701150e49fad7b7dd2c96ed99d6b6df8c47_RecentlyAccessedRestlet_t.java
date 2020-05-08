 package org.iucn.sis.server.extensions.recentasms;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.models.RecentlyAccessed;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.lwxml.shared.NativeDocument;
 import com.solertium.lwxml.shared.NativeNodeList;
 import com.solertium.util.TrivialExceptionHandler;
 
 public class RecentlyAccessedRestlet extends BaseServiceRestlet {
 	
 	private static final int LIMIT = 15;
 	
 	public RecentlyAccessedRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/recent/{type}");
 		paths.add("/recent/{type}/{id}");
 	}
 	
 	private String getType(Request request) {
 		return (String)request.getAttributes().get("type");
 	}
 	
 	private Integer getAccessID(Request request) {
 		return Integer.valueOf((String)request.getAttributes().get("id"));
 	}
 	
 	@Override
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		User user = getUser(request, session);
 		String type = getType(request);
 		
 		List<RecentlyAccessed> list = 
 			session.createCriteria(RecentlyAccessed.class)
 			.add(Restrictions.eq("user", user)).add(Restrictions.eq("type", type))
 			.addOrder(Order.desc("date")).list();
 		
 		final StringBuilder out = new StringBuilder();
 		out.append("<root>");
 		for (RecentlyAccessed accessed : list) {
 			try {
 				RecentInfo info = RecentInfoFactory.load(accessed, session);
 				if (info != null) {
 					info.addField("accessid", accessed.getId() + "");
 					info.addField("accessdate", accessed.getDate().getTime() + "");
 					out.append(info.toXML());
 				}
 			} catch (PersistentException e) {
 				//Entry may not exist anymore, remove it...
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 		}
 		out.append("</root>");
 		
 		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
 	}
 	
 	@Override
 	public void handlePost(Representation entity, Request request, Response response, Session session) throws ResourceException {
 		NativeDocument document = getEntityAsNativeDocument(entity);
 		
 		List<RecentlyAccessed> existing =
 			session.createCriteria(RecentlyAccessed.class)
 			.add(Restrictions.eq("user", getUser(request, session)))
 			.add(Restrictions.eq("type", getType(request)))
 			.addOrder(Order.desc("date")).list();
 		
 		StringBuilder result = new StringBuilder();
 		int newEntries = 0;
 		
 		NativeNodeList nodes = document.getDocumentElement().getElementsByTagName("recent");
 		for (int i = 0; i < nodes.getLength(); i++) {
 			RecentlyAccessed accessed = RecentlyAccessed.fromXML(nodes.elementAt(i));
 		
 			boolean found = false;
 			for (RecentlyAccessed current : existing) {
 				if (current.getObjectid() == accessed.getObjectid()) {
 					current.setDate(accessed.getDate());
 					accessed = current;
 					found = true;
 					break;
 				}
 			}
 			
 			if (!found)
 				newEntries++;
 			
 			try {
 				SIS.get().getManager().saveObject(session, accessed);
 			} catch (PersistentException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 
 			result.append("<id_" + i + ">");
 			result.append(accessed.getId());
 			result.append("</id_" + i + ">");
 		}
 		
 		if (newEntries > 0) {
 			Collections.sort(existing, new DateComparator());
 			//Added anew, so let's update the count by removing the oldest one
 			for (int i = LIMIT - newEntries; i < existing.size(); i++) {
 				try {
 					SIS.get().getManager().deleteObject(session, existing.get(i));
 				} catch (PersistentException e) {
 					TrivialExceptionHandler.ignore(this, e);
 				}
 			}
 		}
 
 		response.setStatus(Status.SUCCESS_OK);
 		response.setEntity(new StringRepresentation("<result>"+result+"</result>", MediaType.TEXT_XML));
 	}
 	
 	@Override
 	public void handleDelete(Request request, Response response, Session session) throws ResourceException {
		Integer accessID = null;
		try {
			accessID = getAccessID(request);
		} catch (Exception e) {
			//It's OK
			TrivialExceptionHandler.ignore(this, e);
		}
 		
 		if (accessID != null) {
 			try {
 				SIS.get().getManager().deleteObject(
 					session, SIS.get().getManager().loadObject(session, RecentlyAccessed.class, accessID)
 				);
 			} catch (PersistentException e) {
 				throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 			}
 		}
 		else {
 			List<RecentlyAccessed> list =
 				session.createCriteria(RecentlyAccessed.class)
 				.add(Restrictions.eq("user", getUser(request, session)))
 				.add(Restrictions.eq("type", getType(request)))
 				.list();
 			
 			for (RecentlyAccessed accessed : list) {
 				try {
 					SIS.get().getManager().deleteObject(session, accessed);
 				} catch (PersistentException e) {
 					throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 				}
 			}
 		}
 	}
 	
 	private static class DateComparator implements Comparator<RecentlyAccessed> {
 		
 		@Override
 		public int compare(RecentlyAccessed o1, RecentlyAccessed o2) {
 			return o2.getDate().compareTo(o1.getDate());
 		}
 		
 	}
 
 }
