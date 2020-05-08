 package steam.dbexplorer.view;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import steam.dbexplorer.SystemCode;
import steam.dbexplorer.Utils;
 import steam.dbexplorer.controller.ExplorerController;
 import steam.dbexplorer.dbobject.DBReference;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.LinkedHashMap;
  
 /**
  * AddEditDialog creates a special dialog box. It is generated
  * specially depending on the table you are adding or editing.
  *
  * @author Andrew Hollenbach (anh7216@rit.edu)
  */
 @SuppressWarnings("serial")
 class AddEditDialog extends JDialog {
     private String currentTable;
     private String tableDbString;
  
     private String createStr = "Create";
     private String cancelStr = "Cancel";
     private LinkedHashMap<String, JTextField> inputs = new LinkedHashMap<String, JTextField>();
     
     /**
      * Creates a new Add and Edit data entries in the tables
      * 
      * @param parent The parent frame
      * @param motherFrame The parent reusults tab
      * @param tableName The name of a new lable name
      */
     public AddEditDialog(JFrame parent, final ResultsTab motherFrame, String tableName) {
     	super(parent, true);
     	JPanel main = new JPanel();
     	main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
     	
     	currentTable = tableName;
     	tableDbString = DBReference.convertToDBFormat(tableName);
 		
     	String[] attr = DBReference.displayNames.get(tableDbString);
     	for(int i=0;i<attr.length;i++) {
     		JPanel p = new JPanel();
     		JLabel label = new JLabel(attr[i]);
     		JTextField input = new JTextField(15);
     		p.add(label);
     		p.add(input);
     		p.setMinimumSize(new Dimension(400,60));
     		main.add(p);
     		inputs.put(attr[i], input);
     	}
     	
     	JPanel options = new JPanel();
     	JButton create = new JButton(createStr);
     	create.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				String[] keys = new String[inputs.size()];
 				String[] results = new String[inputs.size()];
 				//for putting into jtable with no quotes
 				String[] displayResults = new String[inputs.size()];
 				keys = inputs.keySet().toArray(keys);
 				for(int i=0;i<keys.length;i++) {
 					String attrName = keys[i];
 					String val = inputs.get(attrName).getText();
 					String tmp = val;
 					if(val.length() == 0) {
 						val = null;
 					} else if("string".equals(ExplorerController.getAttrType(attrName)) && val != null ) {
 						tmp = val;
						val = Utils.surroundAndSanitize(val);
 					}
 					results[i] = val;
 					displayResults[i] = tmp;
 				}
 				
 				SystemCode result = ExplorerController.createEntry(currentTable, results);
 				if(!result.isSuccess()) {
 					PopupFactory.errorPopup("Insertion Error", result.getMessage());
 				} else {
 					motherFrame.addElemToTable();
 					AddEditDialog.this.dispose();
 				}
 			}
 		});
     	JButton cancel = new JButton(cancelStr);
     	cancel.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				AddEditDialog.this.dispose();
 			}
 		});
     	options.add(create);
     	options.add(cancel);
     	main.add(options);
     	main.setMinimumSize(new Dimension(300,250));
     	this.add(main);
     	this.setMinimumSize(new Dimension(300,250));
     	this.setTitle("Add new " + tableName);
     	this.setVisible(true);
     }
 }
