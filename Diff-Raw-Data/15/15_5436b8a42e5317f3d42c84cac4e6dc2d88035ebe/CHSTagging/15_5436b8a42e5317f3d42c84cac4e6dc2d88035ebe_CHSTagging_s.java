 package org.sakaiproject.sdata.tool.functions;
 
 import java.sql.ResultSet;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.Kernel;
 import org.sakaiproject.db.api.SqlReader;
 import org.sakaiproject.db.api.SqlService;
 import org.sakaiproject.entity.api.Entity;
 import org.sakaiproject.entity.api.EntityManager;
 import org.sakaiproject.entity.api.Reference;
 import org.sakaiproject.entity.api.ResourceProperties;
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.event.api.EventTrackingService;
 
 public class CHSTagging implements Observer {
 
 	private static final Log log = LogFactory.getLog(CHSTagging.class);
 	private SqlService sqlService;
 	private String[] indexFields = new String[] { "tag" };
 	private EventTrackingService eventTrackingService;
 	private EntityManager entityManager;
 
 	public void init() {
 		sqlService = Kernel.sqlService();
 		boolean autoDDL = Kernel.serverConfigurationService().getBoolean("auto.ddl", false);
 		if (autoDDL) {
 			sqlService.ddl(this.getClass().getClassLoader(), "sdata_tagging");
 		}
 		
 		entityManager = Kernel.entityManager();
 		eventTrackingService = Kernel.eventTrackingService();
 		eventTrackingService.addLocalObserver(this);
 		log.info(" Registring "+this);
 		
 		
 	}
 	public void destroy() {
 		log.info(" Deregistering "+this);
 		eventTrackingService.deleteObserver(this);
 		log.info(" DONE Deregistering "+this);
 	}
 
 	public Map<String, Integer> getPropertyVector(String context,
 			String propertyName) {
 		List<?> results = sqlService.dbRead(
 				"select propertyvalue, count(*)  "
 				+ "  from sdata_property_index " 
 				+ " where context = ? "
 				+ "   and propertyname = ? " 
 				+ "  group by propertyvalue ",
 				new Object[] { context, propertyName }, new SqlReader() {
 
 					public Object readSqlResultRecord(ResultSet result) {
 						try {
 							return new Object[] { result.getString(1),
 									result.getInt(2) };
 						} catch (Exception ex) {
 							return new Object[] { "null", 0 };
 						}
 					}
 
 				});
 		
 		Map<String, Integer> m = new HashMap<String, Integer>();
 		for (Iterator<?> i = results.iterator(); i.hasNext();) {
 			Object[] r = (Object[]) i.next();
 			;
 			m.put((String) r[0], (Integer) r[1]);
 		}
 		return m;
 	}
 
 	public List<String> getPropertyMatches(String context, String propertyName,
 			String[] propertyValue) {
 		Object[] params = new Object[propertyValue.length + 2];
 		params[0] = context;
 		params[1] = propertyName;
 		StringBuilder inTerm = new StringBuilder();
 		params[3] = propertyValue[0];
 		inTerm.append("?");
 		for (int i = 1; i < propertyValue.length; i++) {
 			params[i + 2] = propertyValue[i];
 			inTerm.append(",?");
 		}
 
 		List<?> results = sqlService.dbRead("select reference  "
 				+ "  from sdata_property_index " + " where context = ? "
 				+ "   and propertyname = ? " + "   and propertyvalue in ( "
 				+ inTerm.toString() + ") ", params, new SqlReader() {
 
 			public Object readSqlResultRecord(ResultSet result) {
 				try {
 					return result.getString(1);
 				} catch (Exception ex) {
 					return "";
 				}
 			}
 
 		});
 		return (List<String>) results;
 	}
 
 	public void update(String context, Entity entity) {
 		ResourceProperties properties = entity.getProperties();
 		remove(context, entity);
 		for (String indexField : indexFields) {
 			List<?> property = properties.getPropertyList(indexField);
 			for (Iterator<?> p = property.iterator(); p.hasNext();) {
 				sqlService
 						.dbWrite(
 								"insert into sdata_property_index ( context, reference, propertyname, propertyvalue )"
 										+ "values ( ?,?,?,? ) ", new Object[] {
 										context, entity.getId(), indexField,
 										p.next() });
 			}
 		}
 
 	}
 
 	public void remove(String context, Entity entity) {
 		sqlService.dbWrite("delete " 
 				+ "  from sdata_property_index "
 				+ " where context = ? " 
 				+ "  and reference = ? ", new Object[] {
 				context, entity.getId() });
 	}
 	public void update(Observable o, Object evt) {
 
 		Event event = (org.sakaiproject.event.api.Event) evt;
 		if (event.getEvent().startsWith("content")
 					&& !event.getEvent().equals("content.read")) {
 			Reference r = entityManager.newReference(event.getResource());
 			Entity e = r.getEntity();
			if ( event.getEvent().equals("content.delete") ) {
				remove(r.getContext(), e);
			} else {
				update(r.getContext(),e);	
 			}
 		} 
 	}
 	private String getEventContext(String resource, String type) {
 		return resource.replace(type,
 		"").substring(
 				0,
 				resource.replace(
 						type, "").indexOf(
 						"/"));
 	}
 
 }
