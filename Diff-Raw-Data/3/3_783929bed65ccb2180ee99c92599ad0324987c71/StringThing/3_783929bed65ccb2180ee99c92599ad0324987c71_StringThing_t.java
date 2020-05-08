 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 class MainPanel extends JPanel
     implements ActionListener {
 
     JButton adamButton;
     JButton kateButton;
 
     JTextArea textArea;
     JScrollPane scrollPane;
     
 
   public MainPanel() {
 
     textArea = new JTextArea(5, 20);
     textArea.setText("Enter some text here!");
     scrollPane = new JScrollPane();
     scrollPane.setViewportView(textArea);
 
     adamButton = new JButton("Adam's Button");
   
    add(scrollPane); 
 
     adamButton = new JButton("Adam's Button");  
 	kateButton = new JButton("Kate's Button");
   
     add(scrollPane);
     add(adamButton);
     add(kateButton);
 
 
     adamButton.addActionListener(this);
     kateButton.addActionListener(this);
     
   }
 
   // ActionPerformed method from ActionListener interface
   public void actionPerformed(ActionEvent e){ 
 
     Object source = e.getSource();
 
 
     if (source == adamButton)
     {
     adamMethod(textArea.getText());
 
     }
     if (source == kateButton)
     {
     textArea.setText(kateMethod(textArea.getText()));
     }
     }
 
   public void adamMethod(String val)
   {
     String[] ary;
     ary = val.split(" ");
     for (int i=0; i < ary.length; i++)
     {
          JOptionPane.showMessageDialog(null,ary[i]);
     }
 
   }
 
   public String kateMethod(String val)
   {
 	return val.toUpperCase();
 	
   }
 
 
 } 
 
     class StringThing extends JFrame {
       public StringThing() {
             setTitle("Collaborative String Manipulator");
             setSize(350, 300);
             addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
               System.exit(0);
             }
             }); // addWindowListener
             Container contentPane = getContentPane();
             MainPanel mainp = new MainPanel();
             contentPane.add(mainp);
       } // StringThing
     
       public static void main(String[] args) {
         JFrame StrManip = new StringThing();
         StrManip.show();
       }
     }
