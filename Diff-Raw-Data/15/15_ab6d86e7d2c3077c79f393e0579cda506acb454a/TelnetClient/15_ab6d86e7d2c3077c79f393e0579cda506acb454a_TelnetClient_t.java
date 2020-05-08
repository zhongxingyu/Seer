 package telnetClient;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 public class TelnetClient extends AbstractClient
 {
     private JFrame frame = new JFrame("Telnet client");
     private JTextArea textArea = new JTextArea(40, 80);
     private JScrollPane scrollPane = new JScrollPane(textArea);
     private JTextField message = new JTextField("towel.blinkenlights.nl", 15);
     private JTextField portText = new JTextField("23", 4);
     private JButton connect = new JButton("Connect");
     private JButton send = new JButton("Send");
     private JPanel bottomPanel = new JPanel();
 
     public TelnetClient(String host, int port)
     {
         super(host, port);
         textArea.setEditable(false);
         textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
 
         frame.setLayout(new BorderLayout());
         frame.add(scrollPane, BorderLayout.CENTER);
         bottomPanel.add(message);
         bottomPanel.add(portText);
         bottomPanel.add(connect);
         bottomPanel.add(send);
         frame.add(bottomPanel, BorderLayout.SOUTH);
         frame.pack();
         frame.setVisible(true);
 
         connect.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     if (!isConnected())
                     {
                         setHost(message.getText());
                         setPort(Integer.parseInt(portText.getText()));
                         openConnection();
                         textArea.setText("");
                         message.setText("");
                         connect.setText("Disconnect");
                     } else
                     {
                         closeConnection();
                         connect.setText("Connect");
                     }
                 } catch (IOException e1)
                 {
                     textArea.append("Error connecting to host" + "\n");
                 }
 
             }
         });
 
         send.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e)
             {
                 try
                 {
                     sendToServer(message.getText());
                     textArea.append(message.getText() + "\n");
                 } catch (IOException e1)
                 {
                     // TODO Auto-generated catch block
                     e1.printStackTrace();
                 }
             }
         });
 
     }
 
     public static void main(String[] args)
     {
         TelnetClient telnet = new TelnetClient("", 0);
     }
 
     @Override
     // WOW, THIS FUNCTION IS REALLY BAD
     protected void handleMessageFromServer(String msg)
     {
         // is stringbuffer better for this?
         String buffer = new String();
         // TODO: need to check for special telnet codes here
         for (int x = 0; x < msg.length(); x++)
         {
             System.out.print((int) msg.charAt(x) + " ");
             if (msg.charAt(x) == 27)
             {
                 if (msg.charAt(x + 1) == 91 && msg.charAt(x + 2) == 72)
                 {
                     textArea.setText("");
                     buffer = "";
                 }
 
                 x += 2;
             } else
             {
                 buffer += Character.toString(msg.charAt(x));
             }
         }
         buffer += "\n";
         textArea.append(buffer);
 
     }
 
    @Override
    protected void connectionException(Exception exception)
    {
        textArea.append("Error: Disconnecting from host");
        try
        {
            closeConnection();
        } catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        connect.setText("Connect");
    }

 }
