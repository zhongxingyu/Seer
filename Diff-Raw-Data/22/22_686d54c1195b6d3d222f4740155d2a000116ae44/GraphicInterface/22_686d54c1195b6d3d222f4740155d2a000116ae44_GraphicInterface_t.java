 package sma.gui;
 
 import java.awt.*;
 import javax.swing.*;
 import java.awt.Font;
 import java.awt.Dimension;
 import java.awt.BorderLayout;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowAdapter;
 import javax.swing.*; // For JPanel, etc.
 import java.awt.*;           // For Graphics, etc.
 import java.awt.geom.*;      // For Ellipse2D, etc.
 import java.util.ArrayList;
 
 import sma.ontology.*;
 
 /**
  * <p><B>Title:</b> IA2-SMA</p> *
  * <p><b>Description:</b> Practical exercise 2011-12. Recycle swarm.</p>
  * Main class of the graphical interface controlled by the Central Agent.
  * It offers several methods to show the changes of the agents within the
  * city., as well as an area with the logs and an area with the main statistical
  * results.
  * <p><b>Copyright:</b> Copyright (c) 2011</p>
  * <p><b>Company:</b> Universitat Rovira i Virgili (<a
  * href="http://www.urv.cat">URV</a>)</p>
 * @author David Isern & Joan Albert L�pez
  * @version 2.0
  */
 public class GraphicInterface extends JFrame {
 
   int inset = 50;
 
   private sma.gui.MapVisualizer jMapPanel;
   private sma.gui.LogPanel jLogPanel;
   private sma.gui.StatisticsPanel jStatisticsPanel;
   private JPanel jAgensStatusPanel;
   
   GridLayout gridMainLayout = new GridLayout();
   JTabbedPane jGameTabbedPane = new JTabbedPane();
   JPanel jGamePanel = new JPanel();
   private JPanel jGamePanel_1;
   
   private JLabel lblNewLabel;
 
   public GraphicInterface(InfoGame p) {
     try {
       jbInit();
       this.showGameMap(p.getInfo().getMap());
       this.showPanelInfo(p.getInfo());
     }
     catch (Exception exception) {
       exception.printStackTrace();
     }
   }
 
   private void jbInit() throws Exception {
 
     try {
       UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
       //     UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
     } catch (Exception e) {
       System.err.println("-> We use the default L&F. Reason: "+e.toString());
     }
 
     Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
     setBounds( inset, inset, screenSize.width - inset * 2, screenSize.height - inset * 2 );
     //Quit this app when the big window closes.
     addWindowListener(new WindowAdapter() {
       public void windowClosing(WindowEvent e) {
         //System.exit(0);
         dispose();
       }
     });
 
     this.getContentPane().setLayout(gridMainLayout);
 //    this.getContentPane().setBackground(Color.BLACK);
     this.setForeground(Color.WHITE);
 //    this.setBackground(Color.BLACK);
     this.setTitle("Practical exercise AI2-MAS");
 
     gridMainLayout.setColumns(1);
 
 
 
 
     /****************************************************/
 
     Font fontLabels = new Font("Arial", Font.PLAIN, 12);
     Font fontTextArea = new Font("Arial", Font.PLAIN, 12);
     Font font = new Font("Arial", Font.PLAIN, 12);
 
 
     jGameTabbedPane.setTabPlacement(JTabbedPane.TOP);
 
     jGameTabbedPane.setBackground(UtilsGUI.colorBackgroundFrame);
     jGameTabbedPane.setForeground(UtilsGUI.colorForegroundText);
     jGameTabbedPane.setFont(new java.awt.Font("Arial", Font.PLAIN, 10));
     jGameTabbedPane.setBorder(BorderFactory.createEtchedBorder());
     jGameTabbedPane.setMinimumSize(new Dimension(640, 480));
     jGameTabbedPane.setPreferredSize(new Dimension(640, 480));
 
 
     /** Partida *****************************/
     jGamePanel_1 = new JPanel();
     jGamePanel_1.setBackground(UtilsGUI.colorBackgroundFrame);
     jGamePanel_1.setForeground(UtilsGUI.colorForegroundText);
     jGamePanel_1.setBorder(BorderFactory.createEtchedBorder());
     jGamePanel_1.setMinimumSize(new Dimension(640, 480));
     jGamePanel_1.setPreferredSize(new Dimension(640, 480));
 
     ImageIcon icon = new ImageIcon(UtilsGUI.pathIconPartida);
     jGameTabbedPane.addTab("Map", icon, jGamePanel_1);
 
 
     /** Logs *****************************/
     this.jLogPanel = new LogPanel();
     icon = new ImageIcon(UtilsGUI.pathIconLogs);
     jGameTabbedPane.addTab("Logs", icon, this.jLogPanel);
 
     this.jLogPanel.showMessage("Initializing components ....\n");
 
     /** Statistics **********************/
     this.jStatisticsPanel = new StatisticsPanel();
     jGameTabbedPane.addTab("Statistics", icon, this.jStatisticsPanel);
 
     this.jStatisticsPanel.showMessage("All tabs initialized successfully!");
 
     /** TABBED PANE *********************/
     this.getContentPane().add(jGameTabbedPane);
 
   } //endof jbInit()
 
 
   /**
    * Repinta *tot* un tauler de caselles
    * @param t Casella[][] tauler a mostrar
    * @see sma.ontology.Cell
    */
   public void showGameMap(Cell[][] t) {
     jGamePanel_1.setLayout(new BorderLayout(0, 0));
     this.jMapPanel = new MapVisualizer(t);
     this.jGamePanel_1.add(jMapPanel, BorderLayout.CENTER);
       
     this.jGamePanel_1.repaint();
   }
   
   public void showPanelInfo(AuxInfo info)
   {
 	  jAgensStatusPanel = new JPanel();
 	  jAgensStatusPanel.setMinimumSize(new Dimension(115, 7));
 	  JScrollPane scrollPane = new JScrollPane(jAgensStatusPanel);
 	  scrollPane.setPreferredSize(new Dimension(135, 77));
 	  jGamePanel_1.add(scrollPane, BorderLayout.EAST);
 	  
 	  
 
     //must get quantity of ports and boats here;
     int ports = info.getNumPorts();
     int boats = info.getNumBoats();
     jAgensStatusPanel.setLayout(new GridLayout(ports + boats, 1, 0, 0));
     
     int portCounter=1;
     for (PortType type :  info.getPortTypesQuantity().keySet())
     {
     	String pType = type.toString();
     	Integer qty = info.getPortTypesQuantity().get(type);
     	
     	//Set the quantity of ports of this particular type
     	for (int m = 0; m < qty; m++)
     	{
     		GraphicAgentPanelInfo port = new GraphicAgentPanelInfo(AgentPanelType.Port);
         	port.setName("Port" + portCounter);
         	port.setMoneyAvailable(info.getMoneyPorts());
         	port.setMaxQuantityOfSeafood(info.getCapacityPorts());
         	port.setCurrentQuantityOfSeafood(0d, 0d, 0d, 0d);
         	port.setPortType(pType);
         	//Set the values in the labels
         	port.setInfo();
         	jAgensStatusPanel.add(port);
         	portCounter++;
     	}
     	
     }
     for (int boatCounter = 1; boatCounter <= boats; boatCounter++)
     {
     	GraphicAgentPanelInfo boat = new GraphicAgentPanelInfo(AgentPanelType.Boat);
     	boat.setName("Boat" + boatCounter);
    	boat.setMaxQuantityOfSeafood(info.getCapacityBoats());
     	boat.setCurrentQuantityOfSeafood(0d, 0d, 0d, 0d);
     	//Set the values in the labels
     	boat.setInfo();
     	jAgensStatusPanel.add(boat);
     }
   }
 
   /**
    * Mostra una cadena en el panell destinat a logs
    * @param msg String per mostrar
    */
   public void showLog(String msg) {
     this.jLogPanel.showMessage(msg);
   }
 
 
   /**
   * Mostra una cadena en el panell destinat a stad�stiques
    * @param msg String per mostrar
    */
   public void showStatistics(String msg) {
     this.jStatisticsPanel.showMessage(msg);
   }
 
 
 
   public static void main(String[] args) {
     GraphicInterface graphicinterface = new GraphicInterface(null);
     graphicinterface.setVisible(true);
   }
 
 
 }
