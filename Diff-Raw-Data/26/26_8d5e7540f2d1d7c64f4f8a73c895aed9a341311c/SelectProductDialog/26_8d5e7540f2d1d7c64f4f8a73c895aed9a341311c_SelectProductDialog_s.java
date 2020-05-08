 package pimp.gui;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import java.awt.Rectangle;
 import java.awt.Dimension;
 import javax.swing.JList;
 import javax.swing.JButton;
 
 import pimp.Pimp;
 import pimp.productdefs.Product;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.List;
 /**
  * This class provides a dialog window that allows users to select a product
  * class.
  * 
  * @author Daniel Schramm
  *
  */
 public class SelectProductDialog {
 	
 	// Instance variables.
 	private JDialog dialog;
 	private JList list;
 	
 	/**
 	 * Constructor to create the formatted dialog box.
 	 * 
 	 * @param frame the parent frame.
 	 * @param classArray an array of product classes.
 	 */
 	public SelectProductDialog(JFrame frame, 
 			List<Class <? extends Product>> classList) {
 		// Create dialog.
 		dialog = new JDialog(frame, "Create new product", true);
 		
 		// Create JList
 		list = createList(classList); /* the createPanel method is dependent
 										on this method having already run. */
 		
 		// Add content to dialog
 		dialog.getContentPane().add(createPanel());
 		
 		// Adjust dialog settings.
 		dialog.setPreferredSize(new Dimension(300, 400));
 		dialog.setLocationRelativeTo(frame);
 		dialog.pack();
 		
 		// Show the dialog.
 		dialog.setVisible(true);
 	}
 	
 	/**
 	 * Create the JList from whatever disguisting form of list we try to
 	 * inflict on this poor class.
 	 * 
 	 * @param classList a thing that hopefully is a list of product classes.
 	 * @return a JList containing the different types of classes that can be 
 	 * selected.
 	 */
 	private JList createList(List<Class <? extends Product>> classList) {
 		/* The following line gives a warning, see this link for details:
 		 * http://stackoverflow.com/questions/749425/how-do-i-use-generics-with-an-array-of-classes
 		 */
 		Class<? extends Product>[] classArray = 
 			classList.toArray(new Class[0]);
 		return new JList(classArray);
 	}
 	
 	/**
 	 * Create dialog content based on the array of Product classes that users 
 	 * can select from.
 	 * 
 	 * @param classArray an array of product classes. 
 	 * @return the content panel of the dialog.
 	 */
 	private JPanel createPanel() {
 		// Main panel.
 		JPanel panel = new JPanel();
 		panel.add(new JLabel("This is the new product dialog box"));
 		
 		// Add list.
 		list.setPreferredSize(new Dimension(250, 300));
 		panel.add(list);
 		
 		// Create button.
 		JButton btnCreate = new JButton("Create");
 		btnCreate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				dialog.setVisible(false);
 			}
 		});
 		panel.add(btnCreate);
 		
 		// Cancel button.
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				list.clearSelection();
 				dialog.setVisible(false);
 			}
 		});
 		panel.add(btnCancel);
 		
 		// Make button the default.
 		dialog.getRootPane().setDefaultButton(btnCreate);
 		
 		return panel;
 	}
 	
 	/**
 	 * Returns the class selected by the user, or null if no class was
 	 * selected (i.e. the user clicked cancel).
 	 * 
 	 * @return the selected class.
 	 */
 	public Class<? extends Product> getSelectedClass() {
 		
		System.out.println("toString: " + list.getSelectedValue().toString());
		System.out.println("getClass: " + list.getSelectedValue().getClass().getName());
		
 		Object selection = list.getSelectedValue();
 		if (selection == null) {
 			return null;
 		}
 		
		return (Class<? extends Product>) list.getSelectedValue().getClass();
 	}
 }
