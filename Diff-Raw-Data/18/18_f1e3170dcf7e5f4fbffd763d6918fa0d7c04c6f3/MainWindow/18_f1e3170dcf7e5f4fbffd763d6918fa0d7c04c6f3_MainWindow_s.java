 package componentexample;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 
 import javax.swing.*;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreeNode;
 
 import net.miginfocom.swing.MigLayout;
 
 public class MainWindow extends JFrame {
 
 	private JPanel panel = new JPanel();
 	private JLabel classLabel = new JLabel("Class Name"); 
 	private JTextField className = new JTextField("javax.swing.JButton",15);
 	private JButton button = new JButton("get methods");
 	private JMenuBar menu = new JMenuBar();
 	private JTextArea textArea = new JTextArea(30, 35);
 	private JTextArea textArea2 = new JTextArea(30, 35);
 	private JTextArea textArea3 = new JTextArea(30, 35);
 	private JTabbedPane tabPane = new JTabbedPane();
 	private Map<String, Property> properties = new HashMap<String, Property>();
 	private Map<String, EventSet> eventSet = new HashMap<String, EventSet>();
 	private Reflection reflection;
	private JTree tree;
 
 
 	
 
 	public static void main(String[] args){
 		MainWindow main = new MainWindow();
 	}
 
 
 	public MainWindow(){
 		setTitle("Component EG");
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(300, 100);
 		setLayout(new MigLayout());
 		this.add(menu);
 		this.add(classLabel);
 		this.add(className);
 		this.add(button, "span1, split4,wrap");
 		this.add(tabPane, BorderLayout.CENTER);
 		reflection = new Reflection(textArea, textArea2, properties);
 		JComponent panel1 = new JScrollPane(textArea);
 		JComponent panel2 = new JScrollPane(textArea2);
 		JComponent panel3 = new JScrollPane(tree);
 		tabPane.addTab("Methods", panel1);
 		tabPane.addTab("Properties", panel2);
 		tabPane.addTab("Event Sets",panel3);
		
		
		
 		this.pack();
 		button.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String text = className.getText();
 				reflection.printToTextArea(text);
 				setTree();
 			}
 
 		});
 
 		this.setVisible(true);
 
 
 	}
 	
 	public void setTree(){
 		DefaultMutableTreeNode top = new DefaultMutableTreeNode("Event Sets");
 		DefaultMutableTreeNode event;
 		eventSet = reflection.getEvents();
 		for(Map.Entry<String, EventSet> entry : eventSet.entrySet()){
 			EventSet value = entry.getValue();
 			event = new DefaultMutableTreeNode(value.getEventName());
 			top.add(event);
 			Method[] methods = value.getEvents();
 			for(int d=0; d<methods.length; d++){
 				DefaultMutableTreeNode event1 =	new DefaultMutableTreeNode(methods[d].getName());
 				event.add(event1);
 			}
 		}
 		TreeNode rootNode = top;
 		DefaultTreeModel model = new DefaultTreeModel(rootNode);
		tree = new JTree(model);
 		
        JComponent panel3 = new JScrollPane(tree);
       // tabPane.remove(2);
        tree.updateUI();
        //tabPane.addTab("Event Sets", panel3);
 	}
 
 
 
 }
