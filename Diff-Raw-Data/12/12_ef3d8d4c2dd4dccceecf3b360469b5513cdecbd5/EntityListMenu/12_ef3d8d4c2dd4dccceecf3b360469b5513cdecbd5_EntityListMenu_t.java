 package editor.window;
 
 import java.util.ArrayList;
 
 import javax.vecmath.Vector3f;
 import javax.vecmath.Vector4f;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import engine.Engine;
 import engine.entity.Entity;
 import engine.window.components.CollapsiblePanel;
 import engine.window.components.CollapsiblePanel.Direction;
 import engine.window.components.RGBAAdjuster;
 import engine.window.components.Tree;
 import engine.window.components.Window;
 import engine.window.components.XYZAdjuster;
 import engine.window.tree.Model;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 import de.matthiasmann.twl.EditField;
 import de.matthiasmann.twl.Label;
 import de.matthiasmann.twl.ScrollPane;
 import de.matthiasmann.twl.SplitPane;
 import de.matthiasmann.twl.ToggleButton;
 import de.matthiasmann.twl.ValueAdjusterFloat;
 import de.matthiasmann.twl.ValueAdjusterInt;
 import de.matthiasmann.twl.Widget;
 import de.matthiasmann.twl.model.SimpleBooleanModel;
 import de.matthiasmann.twl.model.SimpleFloatModel;
 import de.matthiasmann.twl.model.SimpleIntegerModel;
 import de.matthiasmann.twl.model.SimpleStringModel;
 
 public class EntityListMenu extends Window implements ActionListener {
 	private final Tree tree;
 	private final DialogLayout entity_tree;
 	private Engine engine;
 	private TreeDragNodeEntity tsm;
 	private SplitPane split_pane;
 	private ScrollPane property_editor;
 	private ArrayList<CollapsiblePanel> properties;
 	private Entity ent;
 
 	public EntityListMenu(Engine engine) {
 		super();
 		setTitle("EntityMenu");
 		tree = new Tree();
 		tsm = new TreeDragNodeEntity(tree.getTable());
 		tsm.addActionListener(this);
 		tree.setTreeSelectionManager(tsm);
 		entity_tree = new DialogLayout();
 		entity_tree.setTheme("entitymenu");
 		property_editor = new ScrollPane();
 		property_editor.setTheme("propertymenu");
 		split_pane = new SplitPane();
 		split_pane.setDirection(SplitPane.Direction.VERTICAL);
 		split_pane.setTheme("splitpane");
 		split_pane.add(entity_tree);
 		split_pane.add(property_editor);
 		this.add(split_pane);
 		setEngine(engine);
 		resourceMenuInit();
 		engine.getEntityList().addActionListener(this);
 	}
 
 	public EntityListMenu(Model m, Engine engine) {
 		super();
 		tree = new Tree(m);
 		entity_tree = new DialogLayout();
 		this.engine = engine;
 		resourceMenuInit();
 	}
 
 	private void resourceMenuInit() {
 		tree.setTheme("enttree");
 		Group hgroup = entity_tree.createSequentialGroup().addGroup(
 			entity_tree.createParallelGroup(tree));
 
 		Group vgroup = entity_tree.createSequentialGroup().addGap()
 			.addWidget(tree).addGap();
 
 		entity_tree.setHorizontalGroup(hgroup);
 		entity_tree.setVerticalGroup(vgroup);
 
 		// textree.setSize(getWidth()/3, getHeight()/3);
 
 		//add(entity_tree);
 		
 		// update tree contents after it has been added.
 		//resource_window.createFromProjectResources();
 		createEntityList();
 		properties = new ArrayList<CollapsiblePanel>();
 	}
 	
 	public void createEntityList() {
 		for(Entity e : engine.getEntityList()) {
 			if(!tree.contains((String)e.getProperty("name"))) {
 				tree.createNode(
					(String)e.getProperty("name"), e, tree.getBase()
 				);
 				/*
 			 	Node node = tree.createNode(
 						(String)e.getProperty("name"), e, tree.getBase()
 				);
 				Node subnode;
 				for(String prop: Entity.reqKeys) {
 					if(e.getProperty(prop).getClass() == float.class
 							|| e.getProperty(prop).getClass() == Float.class) { 
 						subnode = node.insert(prop, new EditField());
 						((EditField)subnode.getData(1)).setText((String)e.getProperty(prop));
 						
 					} else if (e.getProperty(prop).getClass() == int.class
 							|| e.getProperty(prop).getClass() == Integer.class) {
 						subnode = node.insert(prop, new ValueAdjusterInt());
 						((ValueAdjusterInt)subnode.getData(1)).setValue((int)e.getProperty(prop));
 					} else if (e.getProperty(prop).getClass() == String.class) {
 						subnode = node.insert(prop, new EditField());
 						((EditField)subnode.getData(1)).setText((String)e.getProperty(prop));
 					} else if (e.getProperty(prop).getClass() == boolean.class ||
 							e.getProperty(prop).getClass() == Boolean.class) {
 						subnode = node.insert(prop, new ToggleButton());
 						((ToggleButton)subnode.getData(1)).getModel().setPressed((Boolean)e.getProperty(prop));
 					} else{
 						node.insert("WTF is this shit?", new Label());
 					}
 				}
 				*/
 			}
 			
 			//for(ResourceManager.ResourceItem resource: entity_list.getResourcesInCategory(category)) {
 			//	resource_window.createNode(resource.name, resource, found_node);
 			//}
 		}
 	}
 	
 	public void createPropertyMenu() {
 		if(ent != null) {
 			properties.clear();
 			this.removeChild(property_editor);
 			Widget field;
 			CollapsiblePanel cp;
 			for(String prop: Entity.reqKeys) {
 				if(ent.getProperty(prop).getClass() == float.class
 						|| ent.getProperty(prop).getClass() == Float.class) { 
 					field = new ValueAdjusterFloat();
 					//((ValueAdjusterFloat)field).setValue((Float)ent.getProperty(prop));
 					((ValueAdjusterFloat)field).setModel(new SimpleFloatModel(0, 100, (Float)ent.getProperty(prop)));
 				} else if (ent.getProperty(prop).getClass() == int.class
 						|| ent.getProperty(prop).getClass() == Integer.class) {
 					field = new ValueAdjusterInt();
 					//((ValueAdjusterInt)field).setValue((Integer)ent.getProperty(prop));
 					((ValueAdjusterInt)field).setModel(new SimpleIntegerModel(0, 100, (Integer)ent.getProperty(prop)));
 				} else if (ent.getProperty(prop).getClass() == String.class) {
 					field = new EditField();
 					//((EditField)field).setText((String)ent.getProperty(prop));
 					((EditField)field).setModel(new SimpleStringModel((String)ent.getProperty(prop)));
 				} else if (ent.getProperty(prop).getClass() == boolean.class ||
 						ent.getProperty(prop).getClass() == Boolean.class) {
 					field = new ToggleButton();
 					((ToggleButton)field).setModel(new SimpleBooleanModel((Boolean)ent.getProperty(prop)));
 					//getModel().setPressed((Boolean)ent.getProperty(prop));
 				} else if (ent.getProperty(prop).getClass() == Vector3f.class) {
 					field = new XYZAdjuster(prop);
 					((XYZAdjuster)field).setValue((Vector3f)ent.getProperty(prop));
 				} else if (ent.getProperty(prop).getClass() == Vector4f.class) {
 					field = new RGBAAdjuster(prop);
 					((RGBAAdjuster)field).setValue((Vector4f)ent.getProperty(prop));
 				} else{
 					field = new Label();
 					((Label)field).setText((String)ent.getProperty(prop));
 				}
 				cp = new CollapsiblePanel(Direction.VERTICAL, prop, field, new SimpleBooleanModel());
 				properties.add(cp);
 			}
 		}
 
 		
 		DialogLayout dialog_layout = new DialogLayout();
 		dialog_layout.setTheme("dialoglayout");
 		// Reset temp row for vertical
 		Group h_grid = dialog_layout.createParallelGroup();
 		for(CollapsiblePanel widget: properties) {
 			h_grid.addWidget(widget);
 		}
 		
 		Group v_grid = dialog_layout.createSequentialGroup();
 		for(CollapsiblePanel widget: properties) {
 			v_grid.addWidget(widget);
 		}
 		
 		dialog_layout.setHorizontalGroup(h_grid);
 		dialog_layout.setVerticalGroup(v_grid);
 		
 		// All Dialog layout groups must have both a HorizontalGroup and
 		// VerticalGroup
 		// Otherwise "incomplete" exception is thrown and layout is not applied
 		property_editor.setContent(dialog_layout);
 	}
 	
 	public void setEngine(Engine engine) {
 		this.engine = engine;
 		tsm.setEngine(engine);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if( ae.getAction() != null)
 			if(ae.getAction().equals("tree_node_clicked"))
 				ent = (Entity)((TreeDragNodeEntity)ae.getSource()).getLastClickedNode().getData(1);
 
 		tree.removeAllNodes();
 		createEntityList();
 		createPropertyMenu();
 	}
 }
