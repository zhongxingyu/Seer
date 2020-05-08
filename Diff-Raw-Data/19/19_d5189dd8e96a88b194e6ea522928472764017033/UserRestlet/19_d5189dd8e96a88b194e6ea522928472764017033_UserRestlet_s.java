 package org.iucn.sis.server.extensions.user.resources;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.iucn.sis.server.api.application.SIS;
 import org.iucn.sis.server.api.persistance.hibernate.PersistentException;
 import org.iucn.sis.shared.api.models.PermissionGroup;
 import org.iucn.sis.shared.api.models.User;
 import org.restlet.data.MediaType;
 import org.restlet.data.Status;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.representation.StringRepresentation;
 import org.restlet.resource.Get;
 import org.restlet.resource.Post;
 import org.restlet.resource.ServerResource;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import com.solertium.db.CanonicalColumnName;
 import com.solertium.db.DBException;
 import com.solertium.db.Row;
 import com.solertium.db.RowProcessor;
 import com.solertium.db.query.ExperimentalSelectQuery;
 import com.solertium.db.query.QComparisonConstraint;
 import com.solertium.db.query.QConstraint;
 import com.solertium.db.query.QRelationConstraint;
 
 public class UserRestlet extends ServerResource {
 
 	private String getUsername() {
 		return (String) getRequest().getAttributes().get("username");
 	}
 
 	@Post
 	public void updateUser(Representation entity) {
 		String username = getUsername();
 		if (username != null) {
 			User user = SIS.get().getUserIO().getUserFromUsername(username);
 			if (user != null) {
 				Document doc;
 				try {
 					doc = new DomRepresentation(entity).getDocument();
 				} catch (IOException e) {
 					getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 					return;
 				}
 				NodeList fields = doc.getElementsByTagName("field");
 				for (int i = 0; i < fields.getLength(); i++) {
 					Element field = (Element) fields.item(i);
 					String name = field.getAttribute("name");
 					String value = field.getTextContent();
					if ("firstname".equalsIgnoreCase(name)) {
 						user.setFirstName(value);
					} else if ("lastname".equalsIgnoreCase(name))
 						user.setLastName(value);
 					else if ("initials".equalsIgnoreCase(name))
 						user.setInitials(value);
 					else if ("affiliation".equalsIgnoreCase(name))
 						user.setAffiliation(value);
 					else if ("sisUser".equalsIgnoreCase(name))
 						user.setSisUser(Boolean.valueOf(value));
 					else if ("rapidListUser".equalsIgnoreCase(name))
 						user.setRapidlistUser(Boolean.valueOf(value));
 					else if ("quickgroup".equalsIgnoreCase(name)){
 						user.getPermissionGroups().clear();
 						for (String permName : value.split(",")) {
 							try {
 								PermissionGroup group = SIS.get().getPermissionIO().getPermissionGroup(permName);
 								if (group != null)
 									user.getPermissionGroups().add(group);
 								else {
 									getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 									getResponse().setEntity("The permission group " + permName + " does not exist.", MediaType.TEXT_PLAIN);
 									return;
 								}
 							} catch (PersistentException e) {
 								getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 								return;
 							}
 						}
 						
 					}
 
 					try {
 						if (SIS.get().getUserIO().saveUser(user))
 							getResponse().setStatus(Status.SUCCESS_OK);
 						else
 							getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					} catch (PersistentException e) {
 						getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
 					}
 				}
 
 			} else {
 				getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 			}
 
 		} else {
 			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
 		}
 	}
 
 	@Get("xml")
 	public Representation getUsers() {
 
 		ExperimentalSelectQuery query = new ExperimentalSelectQuery();
 		query.select("user", "*");
 		query.select("user_permission", "permission_group_id");
 		query.join("user_permission", new QRelationConstraint(new CanonicalColumnName("user", "id"),
 				new CanonicalColumnName("user_permission", "user_id")));
 		query.constrain(new QComparisonConstraint(new CanonicalColumnName("user", "state"), QConstraint.CT_EQUALS,
 				User.ACTIVE));
 		if (getUsername() != null) {
 			query.constrain(new QComparisonConstraint(new CanonicalColumnName("user", "username"),
 					QConstraint.CT_EQUALS, getUsername()));
 		}
 
 		String queryString = query.getSQL(SIS.get().getExecutionContext().getDBSession());
 		queryString = queryString.replaceAll("JOIN", "LEFT JOIN");
 
 		final Map<Integer, User> idToUsers = new HashMap<Integer, User>();
 		try {
 			SIS.get().getExecutionContext().doQuery(queryString, new RowProcessor() {
 
 				@Override
 				public void process(Row row) {
 					PermissionGroup group = new PermissionGroup();
 					group.setID(row.get("permission_group_id").getInteger());
 
 					if (idToUsers.containsKey(group.getId())) {
 						if (group.getId() != 0)
 							idToUsers.get(group.getId()).getPermissionGroups().add(group);
 					} else {
 						User user = new User();
 						user.setId(row.get("id").getInteger());
 						user.setUsername(row.get("username").getString());
 						user.setFirstName(row.get("first_name").getString());
 						user.setLastName(row.get("last_name").getString());
 						user.setInitials(row.get("initials").getString());
 						user.setAffiliation(row.get("affiliation").getString());
 						user.setSisUser(row.get("sis_user").getInteger().equals(1));
 						user.setRapidlistUser(row.get("rapidlist_user").getInteger().equals(1));
 						user.setEmail(row.get("email").getString());
 						if (group.getId() != 0)
 							user.getPermissionGroups().add(group);
 						idToUsers.put(user.getId(), user);
 					}
 				}
 			});
 		} catch (DBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 
 		StringBuilder results = new StringBuilder("<xml>");
 		for (User user : idToUsers.values()) {
 			results.append(user.toFullXML());
 		}
 		results.append("</xml>");
 		return new StringRepresentation(results.toString(), MediaType.TEXT_XML);
 
 	}
 
 	public static List<String> getPaths() {
 		List<String> paths = new ArrayList<String>();
 		paths.add("/users/{username}");
 		paths.add("/users");
 		return paths;
 	}
 
 }
