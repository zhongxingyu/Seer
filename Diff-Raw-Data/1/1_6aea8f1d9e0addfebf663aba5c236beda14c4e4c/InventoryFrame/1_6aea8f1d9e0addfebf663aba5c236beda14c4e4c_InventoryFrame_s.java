 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 public class InventoryFrame extends JFrame
 {
 
   private Player           thePlayer;
   private JButton          use;
   private JButton          drop;
   private JButton          back;
   private JList            inventory;
   private JTextArea        descriptionArea;
   private DefaultListModel inventoryList;
   private JScrollPane      scroll;
   private JPanel           buttons;
   private JPanel           itemPanel;
   private JLabel           message;
   private Vector<Integer>  usableItems;    // Let Bryant decide what is usable
 
   public InventoryFrame(Player player)
   {
     super("Inventory");
 
     thePlayer = player;
     use = new JButton("Use");
     drop = new JButton("Drop");
     back = new JButton("Back");
     buttons = new JPanel();
 
     ButtonListener handler = new ButtonListener();
     use.addActionListener(handler);
     drop.addActionListener(handler);
     back.addActionListener(handler);
 
     buttons.add(use);
     buttons.add(drop);
     buttons.add(back);
 
     itemPanel = new JPanel();
 
     descriptionArea = new JTextArea();
     descriptionArea.setPreferredSize(new Dimension(200, 200));
     descriptionArea.setLineWrap(true);
     descriptionArea.setEditable(false);
 
     usableItems = new Vector<Integer>();
     fillUsuable();
 
     message = new JLabel();
 
     makeInventory();
     inventory = new JList(inventoryList);
     inventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
     inventory.addListSelectionListener(new ListSelectionListener()
     {
 
       @Override
       public void valueChanged(ListSelectionEvent arg0)
       {
         descriptionArea.setText(((Item) inventory.getSelectedValue())
             .getDescription());
       }
 
     });
 
     scroll = new JScrollPane(inventory);
     scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
     itemPanel.add(scroll);
     itemPanel.add(descriptionArea);
 
     this.setLayout(new BorderLayout());
     this.add(itemPanel, BorderLayout.CENTER);
     this.add(buttons, BorderLayout.SOUTH);
     this.add(message, BorderLayout.NORTH);
   }
 
   /**
    * Use this method to fill the array of Items that can be used. If the name of
    * the item is not added to this array it won't be able to be used.!!!! Use
    * the number that is assigned to each Item
    * 
    */
   private void fillUsuable()
   {
     usableItems.add(4);
     usableItems.add(32);
     usableItems.add(31);
     usableItems.add(30);
 
   }
 
   private void makeInventory()
   {
     inventoryList = new DefaultListModel();
     Vector<Item> v = thePlayer.getInventory();
     for (Item item : v)
     {
       inventoryList.addElement(item);
     }
   }
 
   private class ButtonListener implements ActionListener
   {
 
     @Override
     public void actionPerformed(ActionEvent event)
     {
       Item item = (Item) inventory.getSelectedValue();
       int itemNumber = item.getIDNumber();
 
       if (event.getSource().equals(use))
       {
         if (usableItems.contains(itemNumber))
         {
           thePlayer.use(item);
           message.setText("Fuel increased to: " + thePlayer.getFuelLevel());
           
         } else
         {
           message.setText("You can't use this item");
         }
       } else if (event.getSource().equals(drop))
       {
         thePlayer.drop(item);
       }
       
       repaint();  //not working I think it has something to do with the list selection listener. 
       
       
 
     }
 
   }
 }
