 import javax.swing.JLabel;
 import javax.swing.JRadioButton;
 import javax.swing.JPanel;
 import javax.swing.ButtonGroup;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import javax.swing.JFrame;
 import javax.swing.JComponent;
 import java.awt.GridLayout;
 import java.awt.BorderLayout;
 
 public class RadioButton extends JPanel implements ActionListener
 {
     protected JPanel panel;//the panel that this object will be placed on
     protected JLabel label;//the label to show whether or not you hit the right button
     protected ButtonGroup group;//the group of buttons used
     protected JRadioButton[] buttons;//the array of buttons in the group
     protected int choice;//the index of the button that should be chosen
     
     public RadioButton (){
     	//initializes all values
         panel = new JPanel (new GridLayout(0,1));
         label = new JLabel();
         group = new ButtonGroup ();
         buttons = new JRadioButton[(int)(Math.random()*9 + 2)];//creates an array of random length, different number of buttons every time
         
         choice = (int)(Math.random()*group.getButtonCount ());//randomly selects a button you want to pick
         label.setText("Please click the "+choice+" button, or hit the " + choice + " key");//promts which button to press
         add(label);//puts the label on the panel
         for (int i = 0 ; i < buttons.length ; i++){//loop to initialize all the buttons
             buttons[i] = new JRadioButton("Button "+i);//creates the button, and names it
             buttons[i].addActionListener(this);//adds an actionlistener so we can tell if it is hit
            buttons[i].setMnemonic(KeyEvent.VK_1 + i);//allows you to press a key instead of click
             panel.add(buttons[i]);//adds the button to the panel
             group.add(buttons[i]);//adds the button to the group so we can't select more than one
         }
        

         add(panel, BorderLayout.LINE_START);
     }
     
    public void actionPerformed(ActionEvent e){
        if(buttons[choice].isSelected()){
            label.setText("You hit the right button");
         }
        else{
            label.setText("That was the wrong button, please press button "+choice);
         }
     }
 }
