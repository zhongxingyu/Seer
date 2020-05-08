 package editor.window;
 
import java.util.HashMap;

 import javax.vecmath.Vector3f;
 import javax.vecmath.Vector4f;
 
 import editor.action_listener.ActionEvent;
 import editor.action_listener.ActionListener;
 import engine.Engine;
 import engine.entity.Entity;
import engine.resource.ResourceManager.ResourceItem;
 import engine.window.components.CollapsiblePanel;
 import engine.window.components.CollapsiblePanel.Direction;
import engine.window.components.ComboBox;
 import engine.window.components.RGBAAdjuster;
 import engine.window.components.Tree;
 import engine.window.components.Window;
 import engine.window.components.XYZAdjuster;
 import engine.window.components.datamodel.BooleanModelWithEntCallbacks;
 import engine.window.components.datamodel.FloatModelWithEntCallbacks;
 import engine.window.components.datamodel.IntegerModelWithEntCallbacks;
 import engine.window.components.datamodel.StringModelWithEntCallbacks;
 import engine.window.tree.Model;
 import de.matthiasmann.twl.DialogLayout;
 import de.matthiasmann.twl.DialogLayout.Group;
 import de.matthiasmann.twl.EditField;
 import de.matthiasmann.twl.EditField.Callback;
 import de.matthiasmann.twl.Label;
 import de.matthiasmann.twl.ScrollPane;
 import de.matthiasmann.twl.SplitPane;
 import de.matthiasmann.twl.ToggleButton;
 import de.matthiasmann.twl.ValueAdjusterFloat;
 import de.matthiasmann.twl.ValueAdjusterInt;
 import de.matthiasmann.twl.Widget;
 import de.matthiasmann.twl.model.SimpleBooleanModel;
 
 public class EntityListMenu extends Window implements ActionListener {
 	private final Tree tree;
 	private final DialogLayout entity_tree;
 	private Engine engine;
 	private TreeDragNodeEntity tsm;
 	private SplitPane split_pane;
 	private ScrollPane property_editor;
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
 
 		createEntityList();
 		createPropertyMenu();
 	}
 	
 	public void createEntityList() {
 		tree.removeAllNodes();
 		for(Entity e : engine.getEntityList()) {
 			tree.createNode(
 				(String)e.getProperty("name"), e, tree.getBase()
 			);
 		}
 	}
 	
	@SuppressWarnings("unchecked")
 	public void createPropertyMenu() {
 		if(ent != null) {
 			DialogLayout dialog_layout = new DialogLayout();
 			dialog_layout.setTheme("dialoglayout");
 			
 			dialog_layout.setHorizontalGroup(dialog_layout.createParallelGroup());
 			dialog_layout.setVerticalGroup(dialog_layout.createSequentialGroup());
 
 			Widget field;
 			CollapsiblePanel cp;
 			
 			for(String prop: Entity.reqKeys) {
 				if(ent.getProperty(prop).getClass() == float.class
 						|| ent.getProperty(prop).getClass() == Float.class) { 
 					field = new ValueAdjusterFloat();
 					((ValueAdjusterFloat)field).setModel(new FloatModelWithEntCallbacks(0, 100, ent, prop));
 					((ValueAdjusterInt)field).setTheme("valueadjuster");
 				} else if (ent.getProperty(prop).getClass() == int.class
 						|| ent.getProperty(prop).getClass() == Integer.class) {
 					field = new ValueAdjusterInt();
 					((ValueAdjusterInt)field).setModel(new IntegerModelWithEntCallbacks(0, 100, ent, prop));
 					((ValueAdjusterInt)field).setTheme("valueadjuster");
 				} else if (ent.getProperty(prop).getClass() == String.class) {
 					field = new EditField();
 					((EditField)field).setModel(new StringModelWithEntCallbacks(ent,prop));
 					((EditField)field).setTheme("editfield");
 					((EditField)field).setForwardUnhandledKeysToCallback(true);
 					((EditField)field).addCallback(new Callback() {
 						@Override
 						public void callback(int key) {
 							createEntityList();
 						}
 					});
 				} else if (ent.getProperty(prop).getClass() == boolean.class 
 						|| ent.getProperty(prop).getClass() == Boolean.class) {
 					field = new ToggleButton();
 					((ToggleButton)field).setModel(new BooleanModelWithEntCallbacks(ent,prop));
 					((ToggleButton)field).setTheme("togglebutton");
 				} else if (ent.getProperty(prop).getClass() == Vector3f.class) {
 					field = new XYZAdjuster(prop);
 					((XYZAdjuster)field).setValue((Vector3f)ent.getProperty(prop));
 					((XYZAdjuster)field).setTheme("xyzadjuster");
 				} else if (ent.getProperty(prop).getClass() == Vector4f.class) {
 					field = new RGBAAdjuster(prop);
 					((RGBAAdjuster)field).setValue((Vector4f)ent.getProperty(prop));
 					((RGBAAdjuster)field).setTheme("rgbaadjuster");
				} else if (ent.getProperty(prop).getClass() == engine.render.Model.class) {
					field = new ComboBox<String>();
					for(ResourceItem item: engine.resource_manager.getResourcesInCategory("models")) {
						((ComboBox<String>)field).addItem(item.name);
					}
					((ComboBox<String>)field).setTheme("combobox");
				} else if (ent.getProperty(prop).getClass() == HashMap.class) {
					field = new ComboBox<String>();
					for(String item: ((HashMap<String,Object>)ent.getProperty(prop)).keySet()) {
						((ComboBox<String>)field).addItem(item);
					}
					((ComboBox<String>)field).setTheme("combobox");
 				} else{
 					field = new Label();
 					((Label)field).setText((String)ent.getProperty(prop));
 					((Label)field).setTheme("label");
 				}
 				cp = new CollapsiblePanel(Direction.VERTICAL, prop, field, new SimpleBooleanModel());
 				cp.setTheme("collapsiblepanel");
 				dialog_layout.getHorizontalGroup().addWidget(cp);
 				dialog_layout.getVerticalGroup().addWidget(cp);
 			}
 			property_editor.setContent(dialog_layout);
 		}
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
 
 		createEntityList();
 		createPropertyMenu();
 	}
 }
