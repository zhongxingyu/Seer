 package progAdmin;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import reviewItems.ItemToReview;
 import net.miginfocom.swing.MigLayout;
 import utilities.SwingHelper;
 //-----------------------------------------------------------------------------
 /**
  *
  */
 public class AddItemDialog extends JDialog {
 private static final long serialVersionUID = 1L;
 	JFrame parent;
 	ItemToReview item;
 	JTextField titleField;
 	JTextArea textArea;
 //-----------------------------------------------------------------------------
 	public AddItemDialog(JFrame parent){
 		super(parent, "New Item", true);
 		this.parent=parent;
 		
 		this.setPreferredSize(new Dimension(700,500));
 		this.setSize(new Dimension(700,500));
 
 		//Put the form in the middle of the screen
 		this.setLocationRelativeTo(null);
 
 		//Make sure that if the user hits the 'x', the window calls the closeAndCancel method
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				closeAndCancel();
 			}
 		});
 	    
 		item=new ItemToReview();
 		
 		//JPanel mainPanel = new JPanel(new MigLayout("", "[][]", "[][][]"));
 		JPanel mainPanel = new JPanel(new MigLayout());
 		
 		JLabel titleLabel = new JLabel("Item Title: ");
 		JLabel detailsLabel = new JLabel("Description: ");
 		
 		titleField = new JTextField(SwingHelper.DEFAULT_TEXT_FIELD_LENGTH);
 		
 		textArea = new JTextArea(100, 20);
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 
 		
 		JButton addItem = new JButton("Add Item");
 		addItem.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				saveAndClose();
 			 }
 		});
 		
 		mainPanel.add(titleLabel, "align left");
 		mainPanel.add(titleField, "align left, wrap");
 		mainPanel.add(detailsLabel, "align left");
 		mainPanel.add(textArea, "align left, wrap");
 		mainPanel.add(addItem, "align left");
 		
 	    Container contentPane = getContentPane();
 	    contentPane.add(mainPanel);
 
 	}
 //-----------------------------------------------------------------------------
 	private void saveAndClose(){
 		String title="", details="";
 		
 		if(!titleField.getText().isEmpty()){
 			title=titleField.getText().toString().trim();
 			System.out.println("AddItemDialog: AddItemDialog(): title = "+title);
 			item.setTitle(title);
 		}
 		
 		if(!textArea.getText().isEmpty()){
 			details=textArea.getText().toString().trim();
 			System.out.println("AddItemDialog: AddItemDialog(): details = "+details);
 			item.setDetails(details);
 		}
 		
 		try{ item.addToXML(); } 
 		catch (Exception ex){ ex.printStackTrace(); }
 	
 		this.dispose();
 	}
 //-----------------------------------------------------------------------------
 	private void closeAndCancel(){
 		titleField.setText("");
 		textArea.setText("");
 		
 		this.dispose();
 	}
 //-----------------------------------------------------------------------------
 }
