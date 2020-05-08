 package explorer.ui.table;
 
 import static org.osgi.service.event.EventConstants.EVENT_TOPIC;
 
 import java.lang.reflect.Array;
 import java.util.Arrays;
 
 import javax.jcr.Node;
 import javax.jcr.Property;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 import javax.swing.table.AbstractTableModel;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Service;
 import org.apache.sling.api.resource.Resource;
 import org.apache.sling.api.resource.ResourceUtil;
 import org.apache.sling.api.resource.ValueMap;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventHandler;
 
 import explorer.ui.EventTypes;
 
 @SuppressWarnings("serial")
 @Component
 @Service(value = { JcrTableModelImpl.class, EventHandler.class })
@Properties(value = { @org.apache.felix.scr.annotations.Property(name = EVENT_TOPIC, value = EventTypes.DEFAULT_VIEW) })
 public class JcrTableModelImpl extends AbstractTableModel implements EventHandler {
 
 	Node node;
 	boolean hasNode;
 
 	private ValueMap map;
 
 	private String[] propertyNames;
 
 	private String[] headers = new String[] { "Name", "Type", "Value" };
 
 	public void setResource(Resource resource) {
 		this.map = ResourceUtil.getValueMap(resource);
 		if (map.size() == 0) {
 			map.put("sling:resourceType", resource.getResourceType());
 		}
 		Object[] keys = map.keySet().toArray();
 		this.propertyNames = new String[Array.getLength(keys)];
 		for (int i = 0; i < keys.length; ++i) {
 			propertyNames[i] = keys[i].toString();
 		}
 		Arrays.sort(propertyNames);
 		node = resource.adaptTo(Node.class);
 		hasNode = node != null;
 	}
 
 	@Override
 	public int getRowCount() {
 		return propertyNames == null ? 0 : propertyNames.length;
 	}
 
 	@Override
 	public int getColumnCount() {
 		return headers.length;
 	}
 
 	@Override
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		StringBuilder reply = new StringBuilder();
 
 		try {
 			Property prop = null;
 			if (hasNode) {
 				prop = node.getProperty(propertyNames[rowIndex]);
 			}
 			switch (columnIndex) {
 			case 0:
 				reply.append(propertyNames[rowIndex]);
 				break;
 			case 1:
 				if (hasNode) {
 					reply.append(PropertyType.nameFromValue(prop.getType()));
 					if (prop.isMultiple()) {
 						reply.append("[]");
 					}
 				} else {
 					Class<?> klass = map.get(propertyNames[rowIndex]).getClass();
 					reply.append(klass.getSimpleName());
 				}
 				break;
 			case 2:
 				if (hasNode) {
 					if (prop.getType() == PropertyType.BINARY) {
 						return "binary";
 					} else {
 						if (prop.isMultiple()) {
 							int i = 0;
 							reply.append("[");
 							for (Value value : prop.getValues()) {
 								if (i++ > 0) {
 									reply.append(", ");
 								}
 								reply.append(value.getString());
 							}
 							reply.append("]");
 						} else {
 							reply.append(prop.getString());
 						}
 					}
 				} else {
 					if (map.get(propertyNames[rowIndex]).getClass().isArray()) {
 						Object[] values = (Object[]) map.get(propertyNames[rowIndex]);
 						reply.append(map.get(propertyNames[rowIndex], Arrays.toString(values)));
 					} else {
 						reply.append(map.get(propertyNames[rowIndex], String.class));
 					}
 				}
 			}
 
 		} catch (RepositoryException e) {
 		}
 		return reply;
 	}
 
 	@Override
 	public String getColumnName(int column) {
 		return headers[column];
 	}
 
 	@Override
 	public void handleEvent(Event event) {
 		setResource((Resource) event.getProperty("data"));
 		fireTableDataChanged();
 	}
 
 }
