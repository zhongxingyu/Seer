 package frontend;
 
 import java.awt.BorderLayout;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.event.CellEditorListener;
 import javax.swing.event.ChangeEvent;
 
 import backend.environment.Element;
 import backend.environment.Property;
 
 public class PropertiesPanel extends JPanel implements CellEditorListener  {
 	
 
 	JTable jt;
 	JTextField propKey, propVal;
 	JLabel heading;
 	PropertyTableModel model;
 	public PropertiesPanel()
 	{
 		heading = new JLabel("Properties");
 		model = new PropertyTableModel();
 		jt = new JTable(model);
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		this.add(heading);
 		this.add(jt, BorderLayout.CENTER);
 		
 		setVisible(true);
 	}
 	
 	public void targetEntity(Element e) throws Exception	{
 		String [] cols = {"Property", "Value"};
 		ArrayList<Field> props = new ArrayList<Field>();
 		Field[] fields = e.getClass().getFields();
 		for(Field f : fields)
 			//System.out.println(f.getName() + ": " + f.isAnnotationPresent(Property.class) + " " + f.getAnnotations().length);
 			if(f.getAnnotation(Property.class) != null)
 				props.add(f);
 		model.setTarget(e, props);
 		jt.repaint();
 	}
 
	//@Override
 	public void editingCanceled(ChangeEvent arg0) {
 		// TODO Auto-generated method stub
 		System.out.print("derp");
 	}
 
	//@Override
 	public void editingStopped(ChangeEvent arg0) {
 		// TODO Auto-generated method stub
 		System.out.print("herp");
 	}
 }
