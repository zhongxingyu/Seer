 package multiStakeholderGUI;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
import javax.swing.JTextField;
 
 import org.apache.commons.collections15.Factory;
 import org.apache.commons.collections15.Transformer;
 import org.apache.commons.collections15.functors.ConstantTransformer;
 
 import dataStructures.AbstractDocument;
 import dataStructures.Role;
 import dataStructures.SavedAnnotation;
 import dataStructures.Vertex;
 import dataStructures.maps.MemberMap;
 import dataStructures.maps.RoleMap;
 
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
 import edu.uci.ics.jung.visualization.control.ScalingControl;
 import edu.uci.ics.jung.visualization.decorators.EdgeShape;
 import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
 import graph.RoleHierarchy;
 import graph.RoleEditingModalGraphMouse;
 import graph.annotations.VertexAnnotatingGraphMousePlugin;
 
 import mainGUI.UpdatePane;
 
 /**
  * HierarchyPane is an UpdatePane containing a graph in which to create
  * the Stakeholder Hierarchy.
  */
 @SuppressWarnings("serial")
 public class HierarchyPane extends UpdatePane implements ActionListener {
 	
 	private AbstractDocument document;
 	private RoleHierarchy graph;
 	
 	Factory<Integer> edgeFactory;
 
     Factory<Vertex> vertexFactory = new Factory<Vertex>() {
 		public Role create() {
 			return (Role) graphMouse.getEditingPlugin().getSelectedVertex();
 		}};
 		
 	private AbstractLayout<Role, Integer> layout;
 	private VisualizationViewer<Role, Integer> vv;
 	private RoleEditingModalGraphMouse<Role, Integer> graphMouse;
 	
 	// remember GUI Elements so they can be redrawn in update()
 	private JPanel rolePanel;
 	private JComboBox roleBox;
 	private JPanel modePanel;
 	private JPanel zoomPanel;
 	private JButton plus;
 	private JButton minus;
 	private JPanel controls;
 	
 	/**
 	 * Creates the HierarchyPane based on the current Document
 	 * @param abstractDocument
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public HierarchyPane(AbstractDocument abstractDocument) {
 		this.document = abstractDocument;
 		
 		RoleMap rm = abstractDocument.getRoleMap();
 		if( rm.getRoleHierarchy() == null) { // create new RoleHierarchy
 			graph = new RoleHierarchy(document.getRoleMap());
 			layout = new StaticLayout<Role, Integer>(graph);
 			graph.setLayout(layout);
 			edgeFactory = new Factory<Integer>() {
 				int i=0;
 				public Integer create() {
 					return i++;
 				}};
 		} else { // load existing RoleHierarchy
 			graph = rm.getRoleHierarchy();
 			layout = graph.getLayout();
 			edgeFactory = new Factory<Integer>() {
 				int i = graph.getNextEdge();
 				public Integer create() {
 					return i++;
 				}};
 		}
 		
 		// set up view settings
 		vv = new VisualizationViewer<Role, Integer>(layout, new Dimension(500, 575));
 		vv.setBackground(Color.white);
 		vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line()); 
         vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Role>());
 	
         // add a listener for ToolTips
         vv.setVertexToolTipTransformer(new RoleToolTipTransformer());
         vv.getRenderContext().setArrowFillPaintTransformer(new ConstantTransformer(Color.lightGray));
 	
         // add visualizer to zoom/scroll pane
         final GraphZoomScrollPane panel = new GraphZoomScrollPane(vv);
         add(panel);
         
         graphMouse = new RoleEditingModalGraphMouse<Role, Integer>(
 				vv, vertexFactory, edgeFactory);
         
         // Set graphmouse so that annotations can be saved to output file
         graph.setGraphMouse(graphMouse);
         
         vv.setGraphMouse(graphMouse);
         vv.addKeyListener(graphMouse.getModeKeyListener());
         
         addSavedAnnotations();
         
         createZoomControls();
                 
         // set up control panel
         controls = new JPanel();
         update();
         add(controls, BorderLayout.SOUTH);
 	}
 	
 	@Override
 	/**
 	 * Handles actions performed on Role combobox
 	 */
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == roleBox) {
 
 			Role selectedRole = (Role) roleBox
 					.getSelectedItem();
 			if (!selectedRole.isUsed()) {
 				graphMouse.getEditingPlugin().setVertex(selectedRole);
 			}
 		}
 		
 	}
 
 	@Override
 	public void update() {
 		controls.removeAll();
 		
 		setupRoleBox();
 		controls.add(rolePanel);
 		
 		modePanel = new JPanel();
 		modePanel.setBorder(BorderFactory.createTitledBorder("Mode"));
 		modePanel.add(graphMouse.getModeComboBox());
 		controls.add(modePanel);	
 		
 		controls.add(zoomPanel);

		vv.requestFocusInWindow();

		
 	}
 	
 	/**
 	 * Returns the graph form of the RoleHierarchy 
 	 * @return RoleHierarchy
 	 */
 	public RoleHierarchy getGraph() {
 		return graph;
 	}
 	
 	/**
 	 * Creates the combobox containing all the Roles to be used
 	 * in the RoleHierarchy
 	 */
 	private void setupRoleBox() {
 		// make combobox with all attributes
 		Role[] allRoles = document.getRoleMap().values()
 				.toArray(new Role[0]);
 		roleBox = new JComboBox(allRoles);
 
 		// make renderer that greys out already-used roles
 		ComboBoxRenderer renderer = new ComboBoxRenderer();
 		// renderer.setPreferredSize(new Dimension(200, 130));
 		roleBox.setRenderer(renderer);
 
 		roleBox.addActionListener(this);
 		roleBox.invalidate();
 		if (allRoles.length > 0) {
 			roleBox.setSelectedIndex(0);
 		}
 		
 		rolePanel = new JPanel();
 		rolePanel.setBorder(BorderFactory.createTitledBorder("Role"));
 		rolePanel.add(roleBox);
 	}
 	
 	/**
 	 * Adds annotations from a saved project on to the hierarchy
 	 */
 	private void addSavedAnnotations() {
 		ArrayList<SavedAnnotation<Role,String>> savedAnnotations = 
 				graph.getSavedAnnotations();
 		if(savedAnnotations != null) {
 			VertexAnnotatingGraphMousePlugin<Role, Integer> annotatingPlugin = 
 					graphMouse.getAnnotatingPlugin();
 			for(SavedAnnotation<Role,String> savedAnnotation: savedAnnotations) {
 				annotatingPlugin.addSavedAnnotation(savedAnnotation);
 			}
 		}
 		vv.repaint();
 	}
 	
 	/**
 	 * Creates actions for graph zoom controls
 	 */
 	private void createZoomControls() {
 		zoomPanel = new JPanel();
 		zoomPanel.setBorder(BorderFactory.createTitledBorder("Zoom"));
 		
         final ScalingControl scaler = new CrossoverScalingControl();
 
         plus = new JButton("+");
         plus.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 scaler.scale(vv, 1.1f, vv.getCenter());
             }
         });
         minus = new JButton("-");
         minus.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 scaler.scale(vv, 1/1.1f, vv.getCenter());
             }
         });
         
         zoomPanel.add(plus);
         zoomPanel.add(minus);
 	}
 	
 	class ComboBoxRenderer extends DefaultListCellRenderer {
 
 		public ComboBoxRenderer() {
 			super();
 			this.setBackground(Color.WHITE);
 			this.setForeground(Color.BLUE);
 		}
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
 			Role r = (Role) value;
 			if (r != null && r.isUsed())
 				this.setForeground(Color.LIGHT_GRAY);
 			return this;
 		}
 	}
 	
 	class RoleToolTipTransformer implements Transformer<Role,String> {
 		@Override
 		public String transform(Role role) {
 			MemberMap mMap = role.getObject();
 			if(mMap == null)
 				return "";
 			return mMap.toString();
 		}
 	}
 
 }
