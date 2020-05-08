 package org.iucn.sis.server.extensions.user.resources;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 import org.iucn.sis.server.api.persistance.SISPersistentManager;
 import org.iucn.sis.server.api.restlets.TransactionResource;
 import org.iucn.sis.shared.api.debug.Debug;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.Context;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.representation.Variant;
 import org.restlet.resource.Resource;
 import org.restlet.resource.ResourceException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.solertium.db.query.QConstraint;
 import com.solertium.util.BaseDocumentUtils;
 
 /**
  * ProfileSearchResource.java
  * 
  * Allows for perusing the profiles of users and returns pertinent information
  * regarding them, but not full data. Has a list of searchable fields from the
  * profile table that can be updated should you want other fields to be
  * searchable.
  * 
  * @author carl.scott <carl.scott@solertium.com>
  * 
  */
 public class ProfileSearchResource extends TransactionResource {
 
 	private final String[] searchable;
 
 	public ProfileSearchResource(Context context, Request request, Response response) {
 		super(context, request, response);
 		setModifiable(false);
 
 		searchable = new String[] { "firstName", "lastName", "nickname", "affiliation", "userid" };
 
 		getVariants().add(new Variant(MediaType.TEXT_XML));
 	}
 	
 	@Override
 	public Representation represent(Variant variant, Session session) throws ResourceException {
 		Criterion fullCriterion = null;
 		
 		final Form form = getRequest().getResourceRef().getQueryAsForm();
 		final String mode = "or".equals(form.getFirstValue("mode", "or")) ? "or" : "and";
 
 		for (int i = 0; i < searchable.length; i++) {
 			if (form.getFirstValue(searchable[i].toLowerCase()) != null) {
 				int constraintCompare = searchable[i].equals("userid") ? QConstraint.CT_EQUALS : 
 					QConstraint.CT_CONTAINS_IGNORE_CASE;
 				
 				Criterion current = null;
 				for (String value : form.getValuesArray(searchable[i].toLowerCase())) {
 					Criterion crit;
 					if (constraintCompare == QConstraint.CT_EQUALS)
 						crit = Restrictions.eq("id", Integer.valueOf(value));
 					else
 						crit = Restrictions.ilike(searchable[i], value, MatchMode.ANYWHERE);
 					
 					if (current == null)
 						current = crit;
 					else
 						current = Restrictions.or(current, crit); 
 				}
 				
 				if (fullCriterion == null)
 					fullCriterion = current;
 				else if ("or".equals(mode))
 					fullCriterion = Restrictions.or(fullCriterion, current);
 				else if ("and".equals(mode))
 					fullCriterion = Restrictions.and(fullCriterion, current);
 			}
 		}
		
		Criteria criteria = session.createCriteria(User.class)
			.add(Restrictions.eq("state", User.ACTIVE));
		if (fullCriterion != null)
			criteria.add(fullCriterion);
 
 		final List<User> users;
 		try {
			users = criteria.list();
 		} catch (Exception e) {
 			e.printStackTrace();
 			throw new ResourceException(Status.SERVER_ERROR_INTERNAL, e);
 		}
 		
 		final Document document = BaseDocumentUtils.impl.newDocument();
 		final Element root = document.createElement("root");
 		
 		for (User user : users) {
 			try {
 				final Element row = document.createElement("row");
 				final String quickGroup = user.getQuickGroupString();
 				if (form.getFirstValue("quickgroup") != null) {
 					String permString = null;
 					for (String current : form.getValuesArray("quickgroup")) {
 						if (quickGroup.matches(".*?" + current + "rwg(\\b|,).*"))
 							permString = "read,write,grant";
 						else if (quickGroup.matches(".*?" + current + "rw(\\b|,).*"))
 							permString = "read,write";
 						else if (quickGroup.matches(".*?" + current + "rg(\\b|,).*"))
 							permString = "read,grant";
 						else if (quickGroup.matches(".*?" + current + "r(\\b|,).*"))
 							permString = "read";
 						
 						if (permString != null)
 							break;
 					}
 					if (permString == null)
 						continue;
 					else
 						appendField(document, row, "permissions", permString);
 				}
 				appendField(document, row, "firstName", user.getFirstName());
 				appendField(document, row, "lastName", user.getLastName());
 				appendField(document, row, "nickname", user.getNickname());
 				appendField(document, row, "initials", user.getInitials());
 				appendField(document, row, "email", user.getEmail());
 				appendField(document, row, "userid", user.getId()+"");
 				appendField(document, row, "username", user.getUsername());
 				appendField(document, row, "affiliation", user.getAffiliation());
 				appendField(document, row, "quickGroup", user.getQuickGroupString());
 				
 				root.appendChild(row);
 			} catch (Exception e) {
 				Debug.println("Could not add user {0}: {1}", user.getUsername(), e);
 			}
 		}
 		
 		document.appendChild(root);
 		
 		return new StringRepresentation(BaseDocumentUtils.impl.serializeDocumentToString(document, true, true), MediaType.TEXT_XML);
 	}
 	
 	private void appendField(Document document, Element row, String name, String value) {
 		Element fieldEl = 
 			BaseDocumentUtils.impl.createCDATAElementWithText(document, "field", value);
 		fieldEl.setAttribute("name", name);
 		
 		row.appendChild(fieldEl);
 		
 	}
 
 }
