 package org.iucn.sis.server.extensions.recentasms;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import org.hibernate.Hibernate;
 import org.hibernate.Session;
 import org.iucn.sis.server.api.restlets.BaseServiceRestlet;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.Context;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.ResourceException;
 
 import com.solertium.util.portable.XMLWritingUtils;
 
 public class RecentActivityRestlet extends BaseServiceRestlet {
 	
 	public RecentActivityRestlet(Context context) {
 		super(context);
 	}
 	
 	@Override
 	public void definePaths() {
 		paths.add("/activity/{type}");
 		paths.add("/activity/{type}/{user}");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Representation handleGet(Request request, Response response, Session session) throws ResourceException {
 		Calendar cal = Calendar.getInstance();
 		cal.add(Calendar.DATE, -1);
 		
 		String date = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
 		
 		String query = null;
 		/*
 		 * Queries require 
 		 * 1. user.first_name
 		 * 2. user.last_name
 		 * 3. user.email
 		 * 4. edit.created_date
 		 * 5. edit.reason
 		 * 6. taxon.friendly_name
 		 * 7. assessment.id as asm_id
 		 * 8. taxon.id as taxon_id
 		 */
 		
 		/*
 		 * If you have working sets, it will find recent activity for 
 		 * those working sets.  If you don't then it won't bother.
 		 */
 		if ("ws".equals(request.getAttributes().get("type"))) {
 			User user = getUser(request, session);
 			Hibernate.initialize(user.getSubscribedWorkingSets());
 			if (user.getSubscribedWorkingSets() != null && !user.getSubscribedWorkingSets().isEmpty())
 				query = 
					"SELECT DiSTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
 					"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
 					"FROM assessment_edit ae " +
 					"JOIN edit e ON e.id = ae.editid " +
 					"JOIN \"user\" u ON u.id = e.userid " +
 					"JOIN assessment a ON a.id = ae.assessmentid " +
 					"JOIN taxon t ON t.id = a.taxonid " +
					"JOIN working_set_subscribe_user w ON u.id = w.userid " +
					"JOIN working_set_taxon wt ON t.id = wt.taxonid AND w.working_setid = wt.working_setid " + 
					"WHERE reason is not null AND created_date > '" + date + "' " + 
 					"ORDER BY created_date DESC " +
 					"LIMIT 250";
 		}
 		else if ("mine".equals(request.getAttributes().get("type"))) {
 			User user = getUser(request, session);
 			
 			query = 
 				"SELECT DiSTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
 				"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
 				"FROM assessment_edit ae " +
 				"JOIN edit e ON e.id = ae.editid " +
 				"JOIN \"user\" u ON u.id = e.userid " +
 				"JOIN assessment a ON a.id = ae.assessmentid " +
 				"JOIN taxon t ON t.id = a.taxonid " +
 				"WHERE u.id = " + user.getId() + " AND reason is not null AND created_date > '" + date + "' " + 
 				"ORDER BY created_date DESC " +
 				"LIMIT 250";
 		}
 		
 		if (query == null) {
 			query = 
 				"SELECT DISTINCT u.first_name, u.last_name, u.email, e.created_date, e.reason, " +
 				"t.friendly_name, a.id as asm_id, t.id as taxon_id " +
 				"FROM assessment_edit ae " +
 				"JOIN edit e ON e.id = ae.editid " +
 				"JOIN \"user\" u ON u.id = e.userid " +
 				"JOIN assessment a ON a.id = ae.assessmentid " +
 				"JOIN taxon t ON t.id = a.taxonid " +
 				"WHERE reason is not null AND created_date > '" + date + "' " + 
 				"ORDER BY created_date DESC " +
 				"LIMIT 250";
 		}
 		
 		final List<Object[]> list;
 		try {
 			list = session.createSQLQuery(query).list();
 		} catch (Exception e) {
 			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
 		}
 		
 		final StringBuilder out = new StringBuilder();
 		out.append("<root>");
 		
 		for (Object[] row : list) {
 			User user = new User();
 			user.setFirstName((String)row[0]);
 			user.setLastName((String)row[1]);
 			user.setEmail((String)row[2]);
 			
 			out.append("<activity taxonid=\"" + row[7] + "\" assessmentid=\"" + row[6] + "\">");
 			out.append(XMLWritingUtils.writeCDATATag("user", user.getDisplayableName()));
 			out.append(XMLWritingUtils.writeTag("date", ((Date)row[3]).getTime() + ""));
 			out.append(XMLWritingUtils.writeCDATATag("reason", (String)row[4]));
 			out.append(XMLWritingUtils.writeCDATATag("taxon", (String)row[5]));
 			out.append("</activity>");
 			
 		}
 		
 		out.append("</root>");
 		
 		return new StringRepresentation(out.toString(), MediaType.TEXT_XML);
 	}
 	
 }
