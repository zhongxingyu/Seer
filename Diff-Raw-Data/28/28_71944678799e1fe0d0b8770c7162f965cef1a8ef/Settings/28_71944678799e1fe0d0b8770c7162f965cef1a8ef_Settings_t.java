 package bomberman.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
import java.lang.Integer;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class Settings extends JPanel implements ActionListener {
 
     private JTextField hostTF;
     private JTextField portTF;
 
     /**
      * Affiche le formulaire de configuration
      */
     public Settings() {
         super();
 
         this.hostTF = new JTextField("127.0.0.1");
         this.portTF = new JTextField("1337");
         JButton button = new JButton("Porc au caramel");
 
         this.setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.fill = GridBagConstraints.BOTH;
         c.insets = new Insets(5, 5, 5, 5);
 
         c.gridx = 0;
         c.gridy = 0;
         this.add(new JLabel("Rouleau de printemps :"), c);
         c.gridx = 1;
         c.gridy = 0;
         this.add(hostTF, c);
         c.gridx = 0;
         c.gridy = 1;
         this.add(new JLabel("Nems vapeur :"), c);
         c.gridx = 1;
         c.gridy = 1;
         this.add(portTF, c);
         c.gridx = 1;
         c.gridy = 2;
         this.add(button, c);
 
         button.addActionListener(this);
     }
 
     @Override
     public void actionPerformed(ActionEvent e) {
         String host = this.hostTF.getText();

         try {
             if (!(host + ".").matches("^((1?[0-9]{1,2}|2[0-4][0-9]|25[0-5])\\.){4}$")) {
                 throw new Exception("Rouleau de printemps invalide !");
             }
            try{
                Integer port = Integer.parseInt(this.portTF.getText());
            } catch(Exception ex){
                throw new Exception("Nems vapeur invalides !");
            }

             Window.getInstance().showBoard();
             Window.getInstance().repaint();
 
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(null, ex.getMessage());
         }
     }
 }
