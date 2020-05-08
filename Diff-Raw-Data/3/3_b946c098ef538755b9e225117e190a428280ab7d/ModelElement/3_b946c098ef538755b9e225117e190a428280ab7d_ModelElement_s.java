 package org.oobium.eclipse.designer.editor.models;
 
 import static org.oobium.build.workspace.Module.*;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.draw2d.geometry.Dimension;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.ui.views.properties.IPropertyDescriptor;
 import org.oobium.build.model.ModelAttribute;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.model.ModelRelation;
 import org.oobium.build.model.ModelValidation;
 import org.oobium.build.workspace.Module;
 import org.oobium.build.workspace.Workspace;
 import org.oobium.eclipse.OobiumPlugin;
 
 public class ModelElement extends Element {
 
 	public static final String PROP_STATE = "Model.State";
 	public static final String PROP_LOCATION = "Model.Location";
 	public static final String PROP_SIZE = "Model.Size";
 	public static final String PROP_BOUNDS = "Model.Bounds";
 	public static final String PROP_COLOR = "Model.Color";
 	public static final String PROP_CONN_SOURCE = "Model.Connection.Source";
 	public static final String PROP_CONN_TARGET = "Model.Connection.Target";
 	public static final String PROP_FIELD = "Model.Field";
 	public static final String PROP_FIELD_ADDED = "Model.Field.Added";
 	public static final String PROP_FIELD_REMOVED = "Model.Field.Removed";
 	public static final String PROP_VALIDATION_REMOVED = "Model.Validation.Removed";
 	
 	public static final int DELETED = -1;
 	public static final int CREATED = 1;
 	
 	private final ModuleElement module;
 	
 	private Point location;
 	private Dimension size;
 	private RGB color;
 	
 	private ModelDefinition definition;
 	
 	private final List<Connection> sourceConnections;
 	private final List<Connection> targetConnections;
 	
 	public ModelElement(ModuleElement module, ModelDefinition definition) {
 		this(module, definition, 0);
 	}
 	
 	public ModelElement(ModuleElement module, ModelDefinition definition, int state) {
 		this.module = module;
 		this.definition = definition;
 		
 		location = new Point(10, 10);
 		size = new Dimension(100, 100);
 		
 		sourceConnections = new ArrayList<Connection>();
 		targetConnections = new ArrayList<Connection>();
 	}
 
 	void addConnection(Connection connection) {
 		if(connection.getSourceModel() == this) {
 			if(!sourceConnections.contains(connection)) {
 				sourceConnections.add(connection);
 			}
 			firePropertyChanged(PROP_CONN_SOURCE, null, connection);
 		}
 		if(connection.getTargetModel() == this) {
 			if(!targetConnections.contains(connection)) {
 				targetConnections.add(connection);
 			}
 			firePropertyChanged(PROP_CONN_TARGET, null, connection);
 		}
 	}
 	
 	public Set<File> destroy() {
 		File model = definition.getFile();
 		Module module = getModuleElement().getModule();
 		Set<File> files = new HashSet<File>();
 		if(module.removeModelRoutes(model)) {
 			files.add(module.activator);
 		}
 		files.addAll(Arrays.asList(module.destroyNotifier(model)));
 		files.addAll(Arrays.asList(module.destroyForModel(model, CONTROLLER | VIEW)));
 		files.addAll(Arrays.asList(module.destroyModel(model)));
 		return files;
 	}
 	
 	public Rectangle getBounds() {
 		return new Rectangle(location, size);
 	}
 	
 	public RGB getColor() {
 		return color;
 	}
 	
 	public ModelDefinition getDefinition() {
 		return definition;
 	}
 
 	public File getFile() {
 		return definition.getFile();
 	}
 	
 	public Point getLocation() {
 		return location.getCopy();
 	}
 	
 	public ModuleElement getModuleElement() {
 		return module;
 	}
 	
 	public String getName() {
 		return definition.getSimpleName();
 	}
 
 	@Override
 	public IPropertyDescriptor[] getPropertyDescriptors() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	public Object getPropertyValue(Object id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public Dimension getSize() {
 		return size.getCopy();
 	}
 	
 	public List<Connection> getSourceConnections() {
 		return sourceConnections;
 	}
 	
 	public List<Connection> getTargetConnections() {
 		return targetConnections;
 	}
 	
 	public String getType() {
 		return definition.getCanonicalName();
 	}
 	
 	public boolean hasAttribute(String name) {
 		return definition.hasAttribute(name);
 	}
 	
 	public boolean hasAttributes() {
 		return definition.hasAttributes();
 	}
 
 	public boolean hasRelation(String field) {
 		return definition.hasRelation(field);
 	}
 	
 	@Override
 	public boolean isPropertySet(Object id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public void remove(String field) {
 		if(definition.remove(field)) {
 			firePropertyChanged(PROP_FIELD_REMOVED, null, field);
 		}
 	}
 
 	public void removeValidation(String field) {
 		if(definition.removeValidation(field)) {
 			firePropertyChanged(PROP_VALIDATION_REMOVED, null, field);
 		}
 	}
 
 	void removeConnection(Connection connection) {
 		if(connection.getSourceModel() == this) {
 			sourceConnections.remove(connection);
 			firePropertyChanged(PROP_CONN_SOURCE, connection, null);
 		}
 		if(connection.getTargetModel() == this) {
 			targetConnections.remove(connection);
 			firePropertyChanged(PROP_CONN_TARGET, connection, null);
 		}
 	}
 
 	public void replace(ModelRelation oldRelation, ModelRelation newRelation) {
 		definition.remove(oldRelation.name());
 		definition.addRelation(newRelation);
 		firePropertyChanged(PROP_FIELD, (oldRelation.name().equals(newRelation.name()) ? null : oldRelation.name()), newRelation.name());
 	}
 	
 	@Override
 	public void resetPropertyValue(Object id) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public Map<String, Object> save() {
 		Map<String, Object> data = new LinkedHashMap<String, Object>();
 		if(definition.isNew()) {
 			Workspace workspace = OobiumPlugin.getWorkspace();
 			Module module = getModuleElement().getModule();
 			File model = module.createModel(definition);
 			module.createForModel(workspace, model, CONTROLLER | VIEW);
 			module.createNotifier(model);
 			module.addModelRoutes(model);
 			data.put("recompile", true);
 		} else {
 			ModelDefinition current = new ModelDefinition(definition.getFile());
 			if(!definition.equivalent(current)) {
 				definition.save();
 				data.put("recompile", true);
 			}
 		}
		data.put("bounds", getBounds());
 		if(color != null) {
 			data.put("color", color);
 		}
 		return data;
 	}
 	
 	public void setAllowDelete(boolean allow) {
 		if(definition.allowDelete() != allow) {
 			definition.allowDelete(allow);
 			System.out.println("TODO firePropertyChange for setAllowDelete");
 		}
 	}
 	
 	public void setAllowUpdate(boolean allow) {
 		if(definition.allowUpdate() != allow) {
 			definition.allowUpdate(allow);
 			System.out.println("TODO firePropertyChange for setAllowUpdate");
 		}
 	}
 	
 	public void setAttribute(ModelAttribute attribute) {
 		if(attribute == null) {
 			return;
 		}
 		ModelAttribute attr = definition.getAttribute(attribute.name());
 		if(attr == null) {
 			definition.addAttribute(attribute);
 			firePropertyChanged(PROP_FIELD_ADDED, null, attribute.name());
 		} else {
 			// TODO necessary? definition.remove(attribute.name());
 			definition.addAttribute(attribute);
 			firePropertyChanged(PROP_FIELD_ADDED, null, attribute.name());
 		}
 	}
 
 	public ModelAttribute setAttribute(String name, String type) {
 		ModelAttribute attr = definition.getAttribute(name);
 		if(attr == null) {
 			attr = definition.addAttribute(name, type);
 			firePropertyChanged(PROP_FIELD_ADDED, null, name);
 		} else {
 			attr = definition.addAttribute(attr) // make sure we're working with a copy
 			                 .type(type);
 			firePropertyChanged(PROP_FIELD, null, name);
 		}
 		return attr;
 	}
 	
 	public void setAttributeOrder(String[] names) {
 		definition.setAttributeOrder(names);
 		firePropertyChanged(PROP_FIELD, null, "all");
 	}
 	
 	public void setBounds(Rectangle bounds) {
 		if(bounds != null) {
 			this.location = bounds.getLocation();
 			this.size = bounds.getSize();
 			firePropertyChanged(PROP_BOUNDS, null, location.getCopy());
 		}
 	}
 
 	public void setColor(int red, int green, int blue) {
 		this.color = new RGB(red, green, blue);
 		firePropertyChanged(PROP_COLOR, null, "color");
 	}
 	
 	public void setColor(RGB rgb) {
 		if(rgb == null) {
 			this.color = null;
 			firePropertyChanged(PROP_COLOR, null, "color");
 		} else {
 			setColor(rgb.red, rgb.green, rgb.blue);
 		}
 	}
 	
 	public void setDatestamps(boolean datestamps) {
 		if(definition.datestamps() != datestamps) {
 			definition.datestamps(datestamps);
 			if(datestamps) {
 				firePropertyChanged(PROP_FIELD_ADDED, null, "timestamps");
 			} else {
 				firePropertyChanged(PROP_FIELD_REMOVED, null, "timestamps");
 			}
 		}
 	}
 	
 	public void setLocation(Point location) {
 		if(location != null) {
 			this.location.setLocation(location);
 			firePropertyChanged(PROP_LOCATION, null, location.getCopy());
 		}
 	}
 	
 	void setProperties(Map<?,?> data) {
 		if(data == null) {
 			return;
 		}
 		String s = (String) data.get("bounds");
 		if(s != null && s.startsWith("Rectangle(") && s.endsWith(")")) {
 			String[] sa = s.substring(10, s.length()-1).split("\\s*,\\s*");
 			if(sa.length == 4) {
 				int x = Integer.parseInt(sa[0]);
 				int y = Integer.parseInt(sa[1]);
 				int w = Integer.parseInt(sa[2]);
 				int h = Integer.parseInt(sa[3]);
 				setBounds(new Rectangle(x,y,w,h));
 			}
 		}
 		s = (String) data.get("color");
 		if(s != null && s.startsWith("RGB {") && s.endsWith("}")) {
 			String[] sa = s.substring(5, s.length()-1).split("\\s*,\\s*");
 			if(sa.length == 3) {
 				int r = Integer.parseInt(sa[0]);
 				int g = Integer.parseInt(sa[1]);
 				int b = Integer.parseInt(sa[2]);
 				setColor(r,g,b);
 			}
 		}
 	}
 	
 	@Override
 	public void setPropertyValue(Object id, Object value) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public void setRelation(ModelRelation relation) {
 		if(relation == null) {
 			return;
 		}
 		ModelRelation rel = definition.getRelation(relation.name());
 		if(rel == null) {
 			definition.addRelation(relation);
 			firePropertyChanged(PROP_FIELD_ADDED, null, relation.name());
 		} else {
 			// TODO necessary? definition.remove(relation.name());
 			definition.addRelation(relation);
 			firePropertyChanged(PROP_FIELD, null, relation.name());
 		}
 	}
 
 	public ModelRelation setRelation(String name, String type, String opposite, boolean hasMany) {
 		ModelRelation rel = definition.getRelation(name);
 		if(rel == null) {
 			rel = definition.addRelation(name, type, hasMany)
 			                .opposite(opposite);
 			firePropertyChanged(PROP_FIELD_ADDED, null, name);
 		} else {
 			rel = definition.addRelation(rel) // make sure we're working with a copy
 			                .name(name)
 			                .type(type)
 			                .opposite(opposite);
 			firePropertyChanged(PROP_FIELD, null, name);
 		}
 		return rel;
 	}
 
 	public void setSize(Dimension size) {
 		if(size != null) {
 			this.size.setSize(size);
 			firePropertyChanged(PROP_SIZE, null, size.getCopy());
 		}
 	}
 
 	public ModelRelation setThroughRelation(String name, String type, String through, boolean hasMany) {
 		ModelRelation rel = definition.getRelation(name);
 		if(rel == null) {
 			rel = definition.addRelation(name, type, hasMany)
 			                .through(through);
 			firePropertyChanged(PROP_FIELD_ADDED, null, name);
 		} else {
 			rel = definition.addRelation(rel) // make sure we're working with a copy
 			                .name(name)
 			                .type(type)
 			                .through(through);
 			firePropertyChanged(PROP_FIELD, null, name);
 		}
 		return rel;
 	}
 
 	public void setTimestamps(boolean timestamps) {
 		if(definition.timestamps() != timestamps) {
 			definition.timestamps(timestamps);
 			if(timestamps) {
 				firePropertyChanged(PROP_FIELD_ADDED, null, "timestamps");
 			} else {
 				firePropertyChanged(PROP_FIELD_REMOVED, null, "timestamps");
 			}
 		}
 	}
 
 	public void setValidation(ModelValidation validation) {
 		if(validation == null) {
 			return;
 		}
 		ModelValidation original = definition.getValidation(validation.field());
 		if(original == null) {
 			definition.addValidation(validation);
 			firePropertyChanged(PROP_FIELD_ADDED, null, validation.field());
 		} else {
 			// TODO necessary? definition.remove(relation.name());
 			definition.addValidation(validation);
 			firePropertyChanged(PROP_FIELD, null, validation.field());
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return definition.toString();
 	}
 	
 }
