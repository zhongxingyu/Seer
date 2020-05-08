 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.table.*;
 // import java.awt.Dialog.*;
 import javax.swing.JDialog.*;
 import java.awt.Rectangle;
 
 import java.util.Properties;
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 import org.omg.CosNaming.NameComponent;
 import org.omg.CosNaming.NamingContext;
 import org.omg.CosNaming.NamingContextHelper;
 
 public class BoxMonitor {
 private Integer i= new Integer(0);
 private String id;
 private JFrame parent;
 private JTextArea outArea;
 
 private JPanel outPanel;
 private JPanel meanPanel;
 
 private JScrollPane tablePanel;
 
 private JTable outTable;
 
 public DefaultTableModel model = new DefaultTableModel(); 
 
 private GridBagConstraints meanPanelGrid;
 
 private JTextField textBoxGas = new JTextField("",15);
 private JTextField textBoxTyre = new JTextField("",15);
 
 private JLabel labelGas = new JLabel(" l / km ");
 private JLabel labelTyre = new JLabel(" % / km ");
 private JLabel labelGasExpl = new JLabel("Mean Fuel Consumption");
 private JLabel labelTyreExpl = new JLabel("Mean Tyre Consumption");
 
 private String boxCorbaLoc;
 private String monitorBoxCorbaLoc;
 private String configuratorCorbaLoc;
 private String monitorCorbaLoc;
 private ORB orb;
 
BoxMonitor(String id_In){
 id=id_In;
 parent = new JFrame("BoxMonitor n° "+id_In);
 // init();
 }
 
 public void createBoxOutput(){
 outPanel = new JPanel(new BorderLayout());
 outPanel.setLayout(new FlowLayout());
 outPanel.setBorder(BorderFactory.createTitledBorder(null, "Box Output", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 // boxConfigurationGrid = new GridBagConstraints();
 // outArea = new JTextArea(35,30);
 outArea = new JTextArea(25,20);
 outPanel.add(outArea);
 }
 public void createConsumptionMeans(){
 
 textBoxGas.setEditable(false);
 textBoxTyre.setEditable(false);
 meanPanelGrid = new GridBagConstraints();
 meanPanel = new JPanel(new BorderLayout());
 meanPanel.setLayout(new GridBagLayout());
 meanPanel.setBorder(BorderFactory.createTitledBorder(null, "Consumption means", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 0;
 		meanPanelGrid.gridy = 0;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(labelGasExpl, meanPanelGrid);
 
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 0;
 		meanPanelGrid.gridy = 1;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(textBoxGas, meanPanelGrid);
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 1;
 		meanPanelGrid.gridy = 1;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(labelGas, meanPanelGrid);
 
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 0;
 		meanPanelGrid.gridy = 2;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(labelTyreExpl, meanPanelGrid);
 
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 0;
 		meanPanelGrid.gridy = 3;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(textBoxTyre, meanPanelGrid);
 meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 		meanPanelGrid.gridx = 1;
 		meanPanelGrid.gridy = 3;
 		meanPanelGrid.ipady = 5;
 		meanPanel.add(labelTyre, meanPanelGrid);
 
 }
 
 public void createTableOutput(){
 // tablePanel = new JScrollPane();
 // ScrollPaneLayout o = new ScrollPaneLayout();
 // tablePanel.setLayout(o);
 // outTable = new JTable(model);
 // // Create a couple of columns 
 model.addColumn("Id Row");
 model.addColumn("Id Comp"); 
 model.addColumn("Lap");
 model.addColumn("Sector");
 model.addColumn("Fuel Level");
 model.addColumn("Tyre Usury");
 model.addColumn("Time");
 // // Append a row 
 // model.addRow(new Object[]{"v1", "v2"}); 
 // // there are now 2 rows with 2 columns 
 // // Append a row with fewer values than columns. 
 // // The left-most fields in the new row are populated 
 // // with the supplied values (left-to-right) and fields 
 // // without values are set to null. 
 // // model.addRow(new Object[]{"v1"}); 
 // // there are now 3 rows with 2 columns 
 // // Append a row with more values than columns. 
 // // The extra values are ignored. 
 // /*model.addRow(new Object[]{"v1", "v2", "v3"});*/ 
 // o.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
 // o.addLayoutComponent(JScrollPane.COLUMN_HEADER ,outTable);
 
 outTable = new JTable(model);
 // outTable = new JTable(0,5);
 // outTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
 tablePanel = new JScrollPane(outTable);
 // tablePanel.getViewport().add(outTable);
 tablePanel.setVerticalScrollBar(new JScrollBar());
 // outTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 // tablePanel.validate();
 
 
 }
 
 
 public void init(String boxCorbaLocIn, String monitorBoxCorbaLocIn, String configuratorCorbaLocIn, String monitorCorbaLocIn, ORB orbIn){
 System.out.println("BoxMonitor.init");
 boxCorbaLoc = boxCorbaLocIn;
 monitorBoxCorbaLoc = monitorBoxCorbaLocIn;
 configuratorCorbaLoc = configuratorCorbaLocIn;
 monitorCorbaLoc = monitorCorbaLocIn;
 orb = orbIn;
 createBoxOutput();
 createConsumptionMeans();
 createTableOutput();
 parent.add(tablePanel, BorderLayout.EAST);
 parent.add(outPanel,BorderLayout.WEST);
 parent.add(meanPanel,BorderLayout.NORTH);
 JButton startButton = new JButton("Aggiungi Riga");
 		startButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 Integer t = new Integer(i/3);
 Integer q = new Integer(i%3);
 model.insertRow(0,new Object[]{i.toString(), "1",t.toString(), q.toString(), "150.3", "10.0 %", "150.0"});
 // model.addRow(new Object[]{i.toString(), "1","2"});
 ListSelectionModel selectionModel = outTable.getSelectionModel();
 selectionModel.setSelectionInterval(0,0);
 
 System.out.println("casnio");
 i=i+1;
 			}
 		});
 parent.add(startButton, BorderLayout.SOUTH);
 
 parent.pack();
 parent.setVisible(true);
 parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 try{
 org.omg.CORBA.Object obj = orb.string_to_object(monitorBoxCorbaLoc);
 BoxRadio comp = BoxRadioHelper.narrow(obj);
 for(short i=0; i<100; i++)
 comp.RequestStrategy(i);
 model.insertRow(0,new Object[]{i.toString(), "1","t.toString()", "q.toString()", "150.3", "10.0 %", "150.0"});
 // model.addRow(new Object[]{i.toString(), "1","2"});
 ListSelectionModel selectionModel = outTable.getSelectionModel();
 selectionModel.setSelectionInterval(0,0);
 
 System.out.println("casnio");
 }
 catch (Exception e){
 System.out.println("Connessione con il BoxRadioHelper rifiutata");
 JOptionPane.showMessageDialog(parent, "Attention : connection refused by BoxRadioHelper", "Error", JOptionPane.ERROR_MESSAGE);
 
 }
 }
 // public static void main(String[] args){
 // BoxMonitor b = new BoxMonitor(args[0]);
 // b.init();
 // }
 }
