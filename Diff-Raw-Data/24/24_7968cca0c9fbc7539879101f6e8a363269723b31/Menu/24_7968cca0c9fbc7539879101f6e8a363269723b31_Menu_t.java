 import javax.swing.*;
 import java.awt.event.*;
import java.util.*;
 
 public class Menu extends JFrame{

  public JMenuItem cloneMenuItem(JMenuItem item)
  {
    //Hackish method to behave like Object.clone()
    JMenuItem newItem = new JMenuItem(item.getText());
    newItem.setMnemonic(item.getMnemonic());
    return newItem;
  }

   public Menu()
   {
     super("Funky Menu");
 
     JMenu [] states = new JMenu [3];
     JMenuItem [] items = new JMenuItem [3];
    List<JMenuItem> allItems = new ArrayList<JMenuItem>();
     //Initializing the items
     items[0] = new JMenuItem("Industries");
     items[0].setMnemonic('I');
     items[1] = new JMenuItem("Hill Stations");
     items[1].setMnemonic('H');
     items[2] = new JMenuItem("Top Institutions");
     items[2].setMnemonic('T');
 
     //Initializing the states
     //I've set the adjacent keys as the Mnemonics for easy user interaction
     //though it is less intuitive, it can vary on the user preference.
     states[0] =  new JMenu("Tamil Nadu"); states[0].setMnemonic('Q');
     states[1] = new JMenu("West Bengal"); states[1].setMnemonic('W');
     states[2] = new JMenu("Haryana"); states[2].setMnemonic('E');
 
     //Adding all the items to each of the states
     for(int i=0; i<3; ++i)
     {
       for(int j=0; j<3; ++j)
       {
        JMenuItem tmp = cloneMenuItem(items[j]);
        allItems.add(tmp);
        states[i].add(tmp);
       }
     }
 
     //adding action listener to menu items
    for(int j=0; j<allItems.size(); ++j)
     {
      allItems.get(j).addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent evt)
       {
         //The next few lines could be clubbed together in one but for
         //clarity sake I write them seperately
         JMenuItem currentItem = (JMenuItem) evt.getSource();
         String textToDisplay = currentItem.getText();
         System.out.println(textToDisplay + " : located in ...");
         //one liner : System.out.println(((JMenuItem) evt.getSource()).getText() + " : located in ...");
       }
       });
     }
     //finally to fix up the MenuBar
     JMenuBar bar = new JMenuBar();
     setJMenuBar(bar);
     for(int i=0; i<3; ++i)
     {
       bar.add(states[i]);
     }
 
     getContentPane();
     //TODO Create a JLabel add it to the contents
     //Instead of writing to the console, update the frames text
     setSize(500, 500);
     setVisible(true);
   }
 
   public static void main(String[] args)
   {
     Menu app = new Menu();
     app.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
   }
 }
