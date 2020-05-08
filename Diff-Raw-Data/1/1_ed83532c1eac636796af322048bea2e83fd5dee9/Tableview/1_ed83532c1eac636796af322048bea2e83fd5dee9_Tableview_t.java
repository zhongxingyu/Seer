 package gui.steps;
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.util.HashMap;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 
 import javax.swing.WindowConstants;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.SwingUtilities;
 
 import model.steps.InformationGatherStepModel;
 import model.steps.TableviewModel;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class Tableview extends AbstractViewModelConnectionImpl {
 	private JSplitPane jSplitPane1;
 	private JLabel jLabel16;
 	private JLabel jLabel15;
 	private JLabel jLabel14;
 	private JTextField jTextField12;
 	private JLabel jLabel13;
 	private JTextField jTextField11;
 	private JLabel jLabel12;
 	private JScrollPane jScrollPane1;
 	private JLabel jLabel4;
 	private JLabel jLabel5;
 	private JTextField jTextField10;
 	private JLabel jLabel11;
 	private JTextField jTextField9;
 	private JLabel jLabel10;
 	private JTextField jTextField8;
 	private JLabel jLabel9;
 	private JTextField jTextField7;
 	private JLabel jLabel8;
 	private JTextField jTextField6;
 	private JLabel jLabel7;
 	private JTextField jTextField5;
 	private JLabel jLabel6;
 	private JTextField jTextField4;
 	private JTextField jTextField3;
 	private JTextField jTextField2;
 	private JLabel jLabel3;
 	private JTextField jTextField1;
 	private JLabel jLabel2;
 	private JLabel jLabel1;
 	private JPanel jPanel1;
 	private JTable jTable1;
 	private DefaultTableModel jTable1Model;
 	private TableviewModel tvmodel;
 
 	public DefaultTableModel getTableModel() {
 		return jTable1Model;
 	}
 
 	public void setTableModel(DefaultTableModel jTable1Model) {
 		this.jTable1Model = jTable1Model;
 	}
 
 	/**
 	* Auto-generated main method to display this JFrame
 	*/
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				JFrame frame = new JFrame();
 				Tableview inst = new Tableview();
 				frame.add(inst, BorderLayout.CENTER);
 				frame.pack();
 				frame.setVisible(true);
 				 if (inst.getTableModel().getRowCount() == 0) JOptionPane.showMessageDialog(null, "Kein Ergebnis wurde gefunden.", "Nachricht", JOptionPane.ERROR_MESSAGE);
 					
 				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 			}
 		});
 	}
 	
 	public Tableview() {
 		super();
 		initGUI();
 	}
 	
 	private void initGUI() {
 		try {
 			
 			{
 				jSplitPane1 = new JSplitPane();
 				add(getJSplitPane1(), BorderLayout.CENTER);
 				jSplitPane1.setAutoscrolls(true);
 				jSplitPane1.setOrientation(JSplitPane.VERTICAL_SPLIT);
 				jSplitPane1.setDividerLocation(230);
 				jSplitPane1.setPreferredSize(new java.awt.Dimension(700, 520));
 				{
 					jScrollPane1 = new JScrollPane();
 					jSplitPane1.add(jScrollPane1, JSplitPane.RIGHT);
 					jScrollPane1.setPreferredSize(new java.awt.Dimension(700, 520));
 				}
 				{
 					jPanel1 = new JPanel();
 					jSplitPane1.add(jPanel1, JSplitPane.LEFT);
 					jPanel1.setLayout(null);
 					{
 						jLabel1 = new JLabel();
 						jPanel1.add(jLabel1);
 						jLabel1.setText("Informationen zum Event");
 						jLabel1.setBounds(235, 12, 203, 14);
 						jLabel1.setFont(new java.awt.Font("Arial",1,16));
 					}
 					{
 						jLabel2 = new JLabel();
 						jPanel1.add(jLabel2);
 						jLabel2.setText("Name:");
 						jLabel2.setBounds(25, 50, 51, 14);
 					}
 					{
 						jTextField1 = new JTextField();
 						jPanel1.add(jTextField1);
 						jTextField1.setBounds(65, 50, 287, 21);
 					}
 					{
 						jLabel3 = new JLabel();
 						jPanel1.add(jLabel3);
 						jLabel3.setText("Startdatum:");
 						jLabel3.setBounds(356, 57, 76, 14);
 					}
 					{
 						jTextField2 = new JTextField();
 						jPanel1.add(jTextField2);
 						jTextField2.setBounds(438, 54, 248, 21);
 					}
 					{
 						jLabel4 = new JLabel();
 						jPanel1.add(jLabel4);
 						jLabel4.setText("Enddatum:");
 						jLabel4.setBounds(362, 90, 60, 14);
 					}
 					{
 						jTextField3 = new JTextField();
 						jPanel1.add(jTextField3);
 						jTextField3.setBounds(440, 87, 242, 21);
 					}
 					{
 						jLabel5 = new JLabel();
 						jPanel1.add(jLabel5);
 						jLabel5.setText("Ort:");
 						jLabel5.setBounds(25, 90, 38, 14);
 					}
 					{
 						jTextField4 = new JTextField();
 						jPanel1.add(jTextField4);
 						jTextField4.setBounds(63, 87, 107, 21);
 					}
 					{
 						jLabel6 = new JLabel();
 						jPanel1.add(jLabel6);
 						jLabel6.setText("Kinderbetreuung:");
 						jLabel6.setBounds(184, 90, 118, 14);
 					}
 					{
 						jTextField5 = new JTextField();
 						jPanel1.add(jTextField5);
 						jTextField5.setBounds(302, 87, 48, 21);
 					}
 					{
 						jLabel7 = new JLabel();
 						jPanel1.add(jLabel7);
 						jLabel7.setText("Mindestalter:");
 						jLabel7.setBounds(532, 171, 90, 14);
 					}
 					{
 						jTextField6 = new JTextField();
 						jPanel1.add(jTextField6);
 						jTextField6.setBounds(612, 168, 59, 21);
 					}
 					{
 						jLabel8 = new JLabel();
 						jPanel1.add(jLabel8);
 						jLabel8.setText("Preis: (Kinder)");
 						jLabel8.setBounds(25, 132, 98, 14);
 					}
 					{
 						jTextField7 = new JTextField();
 						jPanel1.add(jTextField7);
 						jTextField7.setBounds(112, 129, 75, 21);
 					}
 					{
 						jLabel9 = new JLabel();
 						jPanel1.add(jLabel9);
 						jLabel9.setText("Preis: (Erwachsene)");
 						jLabel9.setBounds(208, 132, 121, 14);
 					}
 					{
 						jTextField8 = new JTextField();
 						jPanel1.add(jTextField8);
 						jTextField8.setBounds(326, 129, 86, 21);
 					}
 					{
 						jLabel10 = new JLabel();
 						jPanel1.add(jLabel10);
 						jLabel10.setText("Kategorie:");
 						jLabel10.setBounds(26, 167, 89, 14);
 					}
 					{
 						jTextField9 = new JTextField();
 						jPanel1.add(jTextField9);
 						jTextField9.setBounds(91, 164, 179, 21);
 					}
 					{
 						jLabel11 = new JLabel();
 						jPanel1.add(jLabel11);
 						jLabel11.setText("Genre:");
 						jLabel11.setBounds(288, 171, 61, 14);
 					}
 					{
 						jTextField10 = new JTextField();
 						jPanel1.add(jTextField10);
 						jTextField10.setBounds(329, 168, 191, 21);
 					}
 					{
 						jLabel12 = new JLabel();
 						jPanel1.add(jLabel12);
 						jLabel12.setText("Beschreibung:");
 						jLabel12.setBounds(26, 204, 89, 14);
 					}
 					{
 						jTextField11 = new JTextField();
 						jPanel1.add(jTextField11);
 						jTextField11.setBounds(115, 204, 567, 21);
 					}
 					{
 						jLabel13 = new JLabel();
 						jPanel1.add(jLabel13);
 						jLabel13.setText("Preis(ermaessigt)");
 						jLabel13.setBounds(433, 129, 110, 16);
 					}
 					{
 						jTextField12 = new JTextField();
 						jPanel1.add(jTextField12);
 						jTextField12.setBounds(537, 126, 106, 23);
 					}
 					{
 						jLabel14 = new JLabel();
 						jPanel1.add(jLabel14);
 						jLabel14.setText("\u20ac");
 						jLabel14.setBounds(192, 131, 10, 16);
 					}
 					{
 						jLabel15 = new JLabel();
 						jPanel1.add(jLabel15);
 						jLabel15.setText("\u20ac");
 						jLabel15.setBounds(417, 131, 10, 16);
 					}
 					{
 						jLabel16 = new JLabel();
 						jPanel1.add(jLabel16);
 						jLabel16.setText("\u20ac");
 						jLabel16.setBounds(651, 129, 10, 16);
 					}
 				}
 				{
 					 jTable1Model = 
 							new DefaultTableModel(
 									new String[][]{},
 									new String[] {"Zeile", "Event ID", "Event Name" });
 					 
 					 
 					jTable1 = new JTable();
 					jScrollPane1.setViewportView(jTable1);
 					jTable1.setModel(jTable1Model);
 
 					jTable1.setLayout(null);
 					jTable1.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
 
 						@Override
 						public void valueChanged(ListSelectionEvent arg0) {
 							int selectedrow = jTable1.getSelectedRow();
 							if (selectedrow == -1) return;
 							HashMap<String, String> selectedevent = tvmodel.getEventInfo(selectedrow);
 							jTextField1.setText(selectedevent.get("name"));
 							jTextField2.setText(selectedevent.get("startdatum"));
 							jTextField3.setText(selectedevent.get("enddatum"));
 							jTextField4.setText(selectedevent.get("ort"));
 							jTextField5.setText((selectedevent.get("kinderbetreuung").equals("t")?"ja":"nein"));
 							jTextField6.setText(selectedevent.get("mindestalter"));
 							jTextField7.setText(round(selectedevent.get("kinder")));
 							jTextField8.setText(round(selectedevent.get("erwachsene")));
 							jTextField9.setText(selectedevent.get("kategorie_name"));
 							jTextField10.setText(selectedevent.get("genre_name"));
 							jTextField11.setText(selectedevent.get("beschreibung"));
 							jTextField12.setText(round(selectedevent.get("ermaessigt")));
 						}
 						
 						
 					});
 				}
 			}
 			
 			//setSize(600, 700);
 				
 		} catch (Exception e) {
 		    //add your error handling code here
 			e.printStackTrace();
 		}
 	}
 	
 	public String round (String num){
		if (num == null) return "";
 		double val = Double.parseDouble(num);
 		val *= 100;
 		val = Math.round(val);
 		val /= 100;
 		return Double.toString(val);
 	}
 	
 	public JSplitPane getJSplitPane1() {
 		return jSplitPane1;
 	}
 
 	@Override
 	public void fillModel() {
 //		 tvmodel = TableviewModel.getInstance();
 //		 tvmodel.fillTableModel();
 	}
 
 	@Override
 	public InformationGatherStepModel getModel() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void fillMask() {
 		// TODO Auto-generated method stub
 		 tvmodel = TableviewModel.getInstance();
 		 tvmodel.fillTableModel();
 			jTable1.getColumnModel().getColumn(0).setPreferredWidth(50);
 			jTable1.getColumnModel().getColumn(1).setPreferredWidth(150);
 			jTable1.getColumnModel().getColumn(2).setPreferredWidth(450);
 	}
 	
 	public void clearFields(){
 		jTextField1.setText("");
 		jTextField2.setText("");
 		jTextField3.setText("");
 		jTextField4.setText("");
 		jTextField5.setText("");
 		jTextField6.setText("");
 		jTextField7.setText("");
 		jTextField8.setText("");
 		jTextField9.setText("");
 		jTextField10.setText("");
 		jTextField11.setText("");
 		jTextField12.setText("");
 	}
 	
 	
 	
 
 }
