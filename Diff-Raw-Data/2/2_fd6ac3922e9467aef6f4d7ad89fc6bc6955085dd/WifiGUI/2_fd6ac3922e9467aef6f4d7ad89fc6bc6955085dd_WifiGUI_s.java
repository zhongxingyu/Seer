 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 
 /* Simulator: Jeremy Lozano  */
 public class WifiGUI
 {    
 	static JTextArea resultsBox;
 	
    public static void main(String[] args)
    {
 	  
        	//FRAME SETUP
         JFrame frame = new JFrame("Team 4 WIFI Simulator");
         frame.setLayout(new BorderLayout());
         
         //LEFT AND RIGHT PANEL DECLRATION AND SETUP
         final JPanel leftPanel = new JPanel();
         leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
         final JPanel rightPanel = new JPanel();
 
         ////////////TOP PANEL 
         //TOP PANEL bottom
         final NodePanel leftPanel2 = new NodePanel(new Node[0]);
         leftPanel2.setBorder(new LineBorder(Color.RED, 2));
         
         
         //TOP PANEL top
         JPanel leftPanel1 = new JPanel();
         //leftPanel1.setLayout(new BoxLayout(leftPanel1,BoxLayout.PAGE_AXIS));
         JLabel maxPacketSizeLabel = new JLabel("MaxPacketSize (in Bytes)");
         final JTextField maxPacketSize = new JTextField("", 5);
         maxPacketSize.setMaximumSize(maxPacketSize.getPreferredSize());
         JLabel numOfPacketsLabel = new JLabel("Number of packets");
         final JTextField numOfPackets = new JTextField("", 5);
         numOfPackets.setMaximumSize(numOfPackets.getPreferredSize());
         JLabel numOfNodesLabel = new JLabel("Number of nodes");
         final JTextField numOfNodes = new JTextField("", 5);
         numOfNodes.setMaximumSize(numOfNodes.getPreferredSize());
         JButton submitButton = new JButton("Submit");
         
         leftPanel1.add(maxPacketSizeLabel);
         leftPanel1.add(maxPacketSize);
         leftPanel1.add(numOfPacketsLabel);
         leftPanel1.add(numOfPackets);
         leftPanel1.add(numOfNodesLabel);
         leftPanel1.add(numOfNodes);
         leftPanel1.add(submitButton);
 
 
         ////////////BOTTOM PANEL 
         resultsBox = new JTextArea(30,50);
         resultsBox.setLineWrap(true);
         final JScrollPane scrollPane = new JScrollPane(resultsBox); 
         scrollPane.setSize(40, 30);
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         resultsBox.setEditable(false);
 
         
         leftPanel.add(leftPanel1);
         leftPanel.add(leftPanel2);
         rightPanel.add(scrollPane);
 
         frame.add(leftPanel, BorderLayout.WEST);
         frame.add(rightPanel, BorderLayout.CENTER);
         
         redirectSystemStreams();
         frame.setSize(1000, 200);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
        
        submitButton.addActionListener(new ActionListener(){
             @Override
 			  public void actionPerformed(ActionEvent e)
 			  {
             	int nNodes = 0;
             	int packetSize = 0;
             	int nPackets = 0;
             	try {
             		resultsBox.setText("");
             		packetSize = Integer.parseInt(maxPacketSize.getText().replace(" ", ""));
             		nPackets = Integer.parseInt(numOfPackets.getText().replace(" ", ""));
             		nNodes = Integer.parseInt(numOfNodes.getText().replace(" ", ""));
             		resultsBox.append("Max Packet Size: " + packetSize + "\nNumber of packets: " + nPackets + "\nNumber of nodes: " + nNodes + '\n');
             	}
             	catch (Exception E)
             	{
             		resultsBox.removeAll();
             		resultsBox.setText("");
             		resultsBox.append("Please enter appropriate entries into all fields!");
             	}
             	
                Simulator a = new Simulator(packetSize, nNodes, nPackets);
         		leftPanel2.setNodes(a.getNodes());
         		a.run();
         		
         		leftPanel.repaint();
 			  }
 			    
 		   });
 
    }
    private static void updateTextArea(final String text) {
 	   SwingUtilities.invokeLater(new Runnable() {
 	     public void run() {
 	       resultsBox.append(text);
 	     }
 	   });
 	 }
 	  
 	 private static void redirectSystemStreams() {
 	   OutputStream out = new OutputStream() {
 	     @Override
 	     public void write(int b) throws IOException {
 	       updateTextArea(String.valueOf((char) b));
 	     }
 	  
 	     @Override
 	     public void write(byte[] b, int off, int len) throws IOException {
 	       updateTextArea(new String(b, off, len));
 	     }
 	  
 	     @Override
 	     public void write(byte[] b) throws IOException {
 	       write(b, 0, b.length);
 	     }
 	   };
 	  
 	   System.setOut(new PrintStream(out, true));
 	   System.setErr(new PrintStream(out, true));
 	 }
 }
