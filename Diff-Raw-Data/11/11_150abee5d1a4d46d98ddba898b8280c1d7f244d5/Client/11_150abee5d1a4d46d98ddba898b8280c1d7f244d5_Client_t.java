 import javax.swing.*;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.net.*;
 import java.io.*;
 
 public class Client {
     static boolean USE_UDP = true;
     static final String DEFAULT_IP = "127.0.0.1";
     static final int PORT = 8800;
 
     public static void main(String[] args) {
         if (1 == args.length) {
             String protocol = args[0].toLowerCase();
 
             if (protocol.equals("tcp")) {
                 USE_UDP = false;
             } else if (protocol.equals("udp")) {
                 USE_UDP = true;
             } else {
                 System.out.println("Error: only TCP or UDP supported\n"
                     + "usage:\n$ java Client [TCP|UDP]");
                 return;
             }
         } else if (1 < args.length) {
             System.out.println("Error: too many arguments\n"
                 + "usage:\n$ java Client [TCP|UDP]");
             return;
         }
 
         NetworkInterface ni = null;
 
         if (USE_UDP) {
             ni = new UDPInterface(PORT);
         } else {
             ni = new TCPInterface(PORT);
         }
 
         JFrame gui = new GUI(ni, DEFAULT_IP);
         gui.setBounds(50, 50, 500, 600);
         gui.setVisible(true);
     }
 }
 
 abstract class NetworkInterface {
     static final int RESPONSE_SIZE = 1024,
                      TIMEOUT = 2000;
 
     abstract protected String sendMsg(String address, String msg);
 
     public String reserve(String address, String name, int count) {
         return sendMsg(address, "reserve " + name + " " + count);
     }
 
     public String search(String address, String name) {
         return sendMsg(address, "search " + name);
     }
 
     public String delete(String address, String name) {
         return sendMsg(address, "delete " + name);
     }
 }
 
 class TCPInterface extends NetworkInterface {
     int port;
 
     public TCPInterface(int port) {
         this.port = port;
     }
 
     protected String sendMsg(String address, String msg) {
         Socket socket = null;
         String response = null;
 
         try {
             System.out.println("sending \"" + msg + "\" to "
                 + address + " on port " + port);
             
             socket = new Socket(address, port);
 
             System.out.println("created socket");
             
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             BufferedReader in = new BufferedReader(
                 new InputStreamReader(socket.getInputStream()));
 
             System.out.println("created in and out");
             
             out.writeBytes(msg + '\n');
 
             System.out.println("sent msg");
             
             response = in.readLine();
             
             System.out.println("received response");
         } catch (Exception ex) {
             System.out.println(ex.toString());
             response = "Exception: see console for details";
         } finally {
             try {
                 if (null != socket) {
                     socket.close();
                 } 
             } catch (IOException ex) {
                 ex.printStackTrace();
                 response = "Exception while closing socket: see console for details";
             }
         }
 
         return response;
     }
 }
 
 class UDPInterface extends NetworkInterface {
     int port;
 
     public UDPInterface(int port) {
         this.port = port;
     }    
 
     protected String sendMsg(String address, String msg) {
         DatagramSocket socket = null;
         String response = null;
 
         try {
             InetAddress host = InetAddress.getByName(address);
             socket = new DatagramSocket();
 
             byte [] data = msg.getBytes();
             DatagramPacket packet = new DatagramPacket(data, data.length, host, port);
 
             System.out.println("sending \"" + msg + "\" to "
                 + address + " on port " + port);
 
             socket.send(packet);
             socket.setSoTimeout(TIMEOUT);
 
             packet.setData(new byte[RESPONSE_SIZE]);
             socket.receive(packet);
             
             response = new String(packet.getData());
         } catch (Exception ex) {
             System.out.println(ex.toString());
             response = "Exception: see console for details";
         } finally {
             if (null != socket) {
                 socket.close();
             }
         }
 
         return response;
     }
 }
 
 class GUI extends JFrame implements ActionListener {
     NetworkInterface ni;    
     JTextField addressField, nameField, countField;
     JButton reserveBtn, searchBtn, deleteBtn;
     JTextArea responseArea;
 
     public GUI(NetworkInterface ni, String defaultAddress) {
         this.ni = ni;
 
         JPanel panel = new JPanel();
         panel.setLayout(new GridLayout(7, 1));
 
         addressField = new JTextField(defaultAddress);
         nameField = new JTextField();
         countField = new JTextField();
         reserveBtn = new JButton("reserve(name, count)");
         searchBtn = new JButton("search(name)");
         deleteBtn = new JButton("delete(name)");
         responseArea = new JTextArea("<server response>");
 
         JPanel addressPanel = new JPanel();
         addressPanel.setLayout(new GridLayout(1, 2));
 
         JPanel namePanel = new JPanel();
         namePanel.setLayout(new GridLayout(1, 2));
 
         JPanel countPanel = new JPanel();
         countPanel.setLayout(new GridLayout(1, 2));
 
         addressPanel.add(new JLabel("IP Address of host:", JLabel.CENTER));
         addressPanel.add(addressField);
 
         namePanel.add(new JLabel("Enter name:", JLabel.CENTER));
         namePanel.add(nameField);
 
         countPanel.add(new JLabel("Enter reservation count:", JLabel.CENTER));
         countPanel.add(countField);
 
         panel.add(addressPanel);
         panel.add(namePanel);
         panel.add(countPanel);
         panel.add(reserveBtn);
         panel.add(searchBtn);
         panel.add(deleteBtn);
         panel.add(responseArea);
 
         this.setContentPane(panel);
 
         reserveBtn.addActionListener(this);
         searchBtn.addActionListener(this);
         deleteBtn.addActionListener(this);
         
         responseArea.setLineWrap(true);
         responseArea.setEditable(false);
     }
 
     private boolean isValidName(String name) {
         if (null == name || name.equals("")) {
             return false;
         }
 
         return name.matches("[a-zA-Z]+"); 
     }
 
     private boolean isValidAddress(String address) {
         if (null == address || address.equals("")) {
             return false;
         }
 
         return address.matches("(([0-1]?[0-9]{1,2}\\.)|(2[0-4][0-9]\\.)"
             + "|(25[0-5]\\.)){3}(([0-1]?[0-9]{1,2})|(2[0-4][0-9])|(25[0-5]))");
     }
 
     public void actionPerformed(ActionEvent e) {
         String address = addressField.getText(),
                name = nameField.getText(),
                response = null;
 
         if (!isValidAddress(address)) {
             response = "Clientside error: that's not a valid IP address!";
         } else if (!isValidName(name)) {
             response = "Clientside error: that's not a valid name!";
         } else if (e.getSource().equals(reserveBtn)) {
             String strCount = countField.getText();
             int numCount = -1;
 
             try {
                 numCount = Integer.parseInt(strCount);
                 System.out.println("reserving " + numCount + " spots for " + name);
                 response = ni.reserve(address, name, numCount);
             } catch (NumberFormatException ex) {
                 response = "Clientside error: {" + strCount + "} is not a valid number!";
             }
         } else if (e.getSource().equals(searchBtn)) {
             System.out.println("searching for " + name + "'s reservations");
             
             response = ni.search(address, name);
         } else if (e.getSource().equals(deleteBtn)) {
             System.out.println("deleting " + name + "'s reservations"); 
 
             response = ni.delete(address, name);
         } else {
             response = "Clientside error: what button is that?";
         }
 
         responseArea.setText(response);
     }    
 }
