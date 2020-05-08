 package fedora.client.console.access;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JInternalFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.xml.rpc.ServiceException;
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import fedora.client.Administrator;
 import fedora.client.console.Console;
 import fedora.client.console.ConsoleSendButtonListener;
 import fedora.client.console.ConsoleCommand;
 import fedora.client.console.ServiceConsoleCommandFactory;
 import fedora.server.access.FedoraAPIAServiceLocator;
 
 /**
  *
  * <p><b>Title:</b> AccessConsole.java</p>
  * <p><b>Description:</b> </p>
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class AccessConsole
         extends JInternalFrame
         implements Console {
 
     private Administrator m_mainFrame;
    private static FedoraAPIAServiceLocator m_locator=new FedoraAPIAServiceLocator();
     private JTextArea m_outputArea;
     private JTextField m_hostTextField;
     private JTextField m_portTextField;
     private boolean m_isBusy;
 
     public AccessConsole(Administrator mainFrame) {
         super("Access Console",
               true, //resizable
               true, //closable
               true, //maximizable
               true);//iconifiable
         m_mainFrame=mainFrame;
 
 
         m_outputArea = new JTextArea();
         m_outputArea.setFont(new Font("Serif", Font.PLAIN, 16));
         m_outputArea.setEditable(false);
 
         JScrollPane outputScrollPane = new JScrollPane(m_outputArea);
         outputScrollPane.setVerticalScrollBarPolicy(
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
         outputScrollPane.setPreferredSize(new Dimension(250, 250));
         outputScrollPane.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
 
         JPanel controlPanel=new JPanel();
         controlPanel.setLayout(new BorderLayout());
         JPanel hostPortPanel=new JPanel();
 
         hostPortPanel.setLayout(new BorderLayout());
         JPanel hostPanel=new JPanel();
         hostPanel.setLayout(new BorderLayout());
         hostPanel.add(new JLabel("Host : "), BorderLayout.WEST);
        m_hostTextField=new JTextField("localhost", 13);
         hostPanel.add(m_hostTextField, BorderLayout.EAST);
 
         JPanel portPanel=new JPanel();
         portPanel.setLayout(new BorderLayout());
         portPanel.add(new JLabel("  Port : "), BorderLayout.WEST);
        m_portTextField=new JTextField("8080", 4);
         portPanel.add(m_portTextField, BorderLayout.EAST);
 
         hostPortPanel.add(hostPanel, BorderLayout.WEST);
         hostPortPanel.add(portPanel, BorderLayout.EAST);
 
         JPanel commandPanel=new JPanel();
         commandPanel.setLayout(new BorderLayout());
         commandPanel.add(new JLabel("  Command : "), BorderLayout.WEST);
         ConsoleCommand[] commands=null;
         try {
             commands=
                     ServiceConsoleCommandFactory.getConsoleCommands(
                     Class.forName("fedora.server.access.FedoraAPIA"), null);
         } catch (ClassNotFoundException cnfe) {
             System.out.println("Could not find server access interface, FedoraAPIA.");
             System.exit(0);
         }
 
 
         JComboBox commandComboBox=new JComboBox(commands);
         commandComboBox.setSelectedIndex(0);
         commandPanel.add(commandComboBox);
         JButton sendButton=new JButton(" Send.. ");
         sendButton.addActionListener(new ConsoleSendButtonListener(commandComboBox.getModel(), m_mainFrame, this));
 
 
         commandPanel.add(sendButton, BorderLayout.EAST);
 
         controlPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
         controlPanel.add(hostPortPanel, BorderLayout.WEST);
         controlPanel.add(commandPanel);
 
         getContentPane().setLayout(new BorderLayout());
         getContentPane().add(controlPanel, BorderLayout.NORTH);
         getContentPane().add(outputScrollPane);
 
         setFrameIcon(new ImageIcon(this.getClass().getClassLoader().getResource("images/standard/development/Host16.gif")));
 
         pack();
         int w=getSize().width;
         int h=getSize().height;
         if (w>Administrator.getDesktop().getWidth()-10) {
             w=Administrator.getDesktop().getWidth()-10;
         }
         if (h>Administrator.getDesktop().getHeight()-10) {
             h=Administrator.getDesktop().getHeight()-10;
         }
         setSize(w, h);
         m_isBusy=false;
     }
 
     public void setBusy(boolean b) {
         m_isBusy=b;
         if (b) {
             getContentPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));
         } else {
             getContentPane().setCursor(null);
         }
     }
 
     public boolean isBusy() {
         return m_isBusy;
     }
 
     public Object getInvocationTarget(ConsoleCommand cmd)
             throws InvocationTargetException {
         String hostString=m_hostTextField.getText();
         String portString=m_portTextField.getText();
         try {
             URL ourl=new URL(m_locator.getFedoraAPIAPortSOAPHTTPAddress());
             StringBuffer nurl=new StringBuffer();
            nurl.append("http://");
             nurl.append(hostString);
             nurl.append(':');
             nurl.append(portString);
             nurl.append(ourl.getPath());
             if ((ourl.getQuery()!=null) && (!ourl.getQuery().equals("")) ) {
                 nurl.append('?');
                 nurl.append(ourl.getQuery());
             }
             if ((ourl.getRef()!=null) && (!ourl.getRef().equals("")) ) {
                 nurl.append('#');
                 nurl.append(ourl.getRef());
             }
             return m_locator.getFedoraAPIAPortSOAPHTTP(new URL(nurl.toString()));
         } catch (MalformedURLException murle) {
             throw new InvocationTargetException(murle, "Badly formed URL");
         } catch (ServiceException se) {
             throw new InvocationTargetException(se);
         }
     }
 
     public void print(String output) {
         m_outputArea.append(output);
     }
 
     public void clear() {
         m_outputArea.setText("");
     }
 
 }
