 package ee.ut.math.tvt.salessystem.ui;
 
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JTabbedPane;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import org.apache.log4j.Logger;
 
 import com.jgoodies.looks.windows.WindowsLookAndFeel;
 
 import ee.ut.math.tvt.BSS.comboBoxItem;
 import ee.ut.math.tvt.salessystem.domain.controller.SalesDomainController;
 import ee.ut.math.tvt.salessystem.ui.model.SalesSystemModel;
 import ee.ut.math.tvt.salessystem.ui.tabs.HistoryTab;
 import ee.ut.math.tvt.salessystem.ui.tabs.PurchaseTab;
 import ee.ut.math.tvt.salessystem.ui.tabs.StockTab;
 
 /**
  * Graphical user interface of the sales system.
  */
 public class SalesSystemUI extends JFrame {
 
   private static final long serialVersionUID = 1L;
 
   private static final Logger log = Logger.getLogger(SalesSystemUI.class);
 
   private final SalesDomainController domainController;
 
   // Warehouse model
   private SalesSystemModel model;
 
   // Instances of tab classes
   private PurchaseTab purchaseTab;
   private HistoryTab historyTab;
   private StockTab stockTab;
 
   /**
    * Constructs sales system GUI.
    * @param domainController Sales domain controller.
    */
   public SalesSystemUI(SalesDomainController domainController) {
     this.domainController = domainController;
     this.model = new SalesSystemModel(domainController);
 
     // Create singleton instances of the tab classes
     historyTab = new HistoryTab();
     stockTab = new StockTab(model);
     purchaseTab = new PurchaseTab(domainController, model);
 
     setTitle("Sales system");
 
     // set L&F to the nice Windows style
     try {
       UIManager.setLookAndFeel(new WindowsLookAndFeel());
 
     } catch (UnsupportedLookAndFeelException e1) {
       log.warn(e1.getMessage());
     }
 
     drawWidgets();
 
     // size & location
     int width = 600;
     int height = 400;
     setSize(width, height);
     Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
     setLocation((screen.width - width) / 2, (screen.height - height) / 2);
 
     addWindowListener(new WindowAdapter() {
       @Override
       public void windowClosing(WindowEvent e) {
         System.exit(0);
       }
     });
   }
 
   private Component getComponent(String componentName, Component component) {
 	    Component found = null;
 
 	    if (component.getName() != null && component.getName().equals(componentName)) {
 	        found = component;
 	    } else {
 	            for (Component child : ((Container) component).getComponents()) {
 	                    found = getComponent(componentName, child);
 
 	                    if (found != null)
 	                            break;
 	            }
 	        }
 	    return found;
 	}
   
   private void drawWidgets() {
     JTabbedPane tabbedPane = new JTabbedPane();
 
     tabbedPane.add("Point-of-sale", purchaseTab.draw());
     tabbedPane.add("Warehouse", stockTab.draw());
     tabbedPane.add("History", historyTab.draw());
 
     tabbedPane.addChangeListener(new ChangeListener() {
         public void stateChanged(ChangeEvent e) {
         	
         	if (e.getSource() instanceof JTabbedPane) {
         		JTabbedPane tabPages = (JTabbedPane)e.getSource();        		
                 switch (tabPages.getSelectedIndex()) {
                 case 0:
 						Component comp = getComponent("BarCodeComboBox",
 								tabPages.getSelectedComponent());
 						if ((comp != null) && (comp instanceof JComboBox)) {
 							JComboBox barCodeCB = (JComboBox) comp;
							long Id = ((comboBoxItem) barCodeCB.getSelectedItem()).getId().longValue();
							barCodeCB.setModel(new DefaultComboBoxModel(model.getWarehouseTableModel().getProductList()));
 							for (int i = 0; i < barCodeCB.getItemCount(); i++) {
								if (Id == ((comboBoxItem) barCodeCB.getItemAt(i)).getId().longValue()) {
 									barCodeCB.setSelectedIndex(i);
 									break;
 								}
 							}
                 	}
                     break;                
                 default:
                     break;
 
                 }                     		
               log.debug("Tab: " + tabPages.getSelectedComponent().getName());
               //System.out.println("Tab: change active");
         	}
         }
     });
 
     
     getContentPane().add(tabbedPane);
   }
 
 }
 
 
