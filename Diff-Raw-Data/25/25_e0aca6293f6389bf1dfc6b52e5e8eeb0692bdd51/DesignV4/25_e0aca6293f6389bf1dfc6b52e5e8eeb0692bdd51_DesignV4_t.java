 package visual;
 
 import java.awt.Button;
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 
 import base.Eshop;
 import base.EshopV4;
 
 public class DesignV4 extends DesignV3 {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public DesignV4(Eshop eshop) {
 		super(eshop);
 		
 		if (eshop instanceof EshopV4) {
 			((EshopV4)eshop).setDesign(this);
 			
 			this.addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent w) {
 					try {
 						save();
 					} catch (Exception e) {
 						JOptionPane.showMessageDialog(null, e.getMessage(),
 								"Warning", JOptionPane.WARNING_MESSAGE);
 					}
 				}
 			});
 		}
 	}
 	
 	public void save() throws FileNotFoundException, IOException {
 		((EshopV4)eshop).save();
 	}
 	
 	public JPanel ordersPanel() {
 		JPanel p = super.ordersPanel();
 
 		Component[] components = p.getComponents();
 		Component component;
 		for (int i = 0; i < components.length; i++) {
 			component = components[i];
 			if (component.getName() != null
 					&& component.getName().equals("buttons_list")) {
 
 				JPanel cpanel = (JPanel) component;
 
 				Button startShopping = new Button("Start shopping");
 				startShopping.addActionListener(new ActionListener() {
 					public void actionPerformed(ActionEvent e) {
 						if (eshop instanceof EshopV4) {
 							try {
 								((EshopV4) eshop).startShopping();
 							} catch (Exception ex) {
 								JOptionPane.showMessageDialog(null, ex.getMessage(),
 										"Warning", JOptionPane.WARNING_MESSAGE);
 							}
 						}
 					}
 				});
 
 				cpanel.add(startShopping);
 
 			}
 		}
 
 		return p;
 	}
 	
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				Design ex;
				EshopV4 eshop = null;
				
 				try {
					eshop = EshopV4.load();
				} catch (IOException e) {
					eshop = new EshopV4();
					eshop.loadProductsXml();
 				} catch (Exception e) {
 					JOptionPane.showMessageDialog(null, e.getMessage(),
 							"Warning", JOptionPane.WARNING_MESSAGE);
 				}
				
				ex = new DesignV4(eshop);
				ex.setVisible(true);
 			}
 		});
 	}
 	
 }
