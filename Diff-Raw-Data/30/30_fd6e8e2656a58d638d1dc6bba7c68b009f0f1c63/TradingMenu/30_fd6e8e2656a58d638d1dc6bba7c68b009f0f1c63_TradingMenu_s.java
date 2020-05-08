 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 import java.util.Vector;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 /**
  * This class is a frame that has some very simple trading. The Frame only needs
  * a player. If this frame is brought up and passed a player that does not have
  * a trade port location unknown things will happen. NEED TO TEST MORE
  * 
  * @author Ryan
  * 
  */
 public class TradingMenu extends JFrame implements WindowFocusListener
 {
 
   private JScrollPane      playerScroll;
   private JScrollPane      merchantScroll;
   private JList            playersInventory;
   private JList            merchantsInventory;
   private JPanel           buttonPanel;
   private JLabel		   topPlayerLabel;
   private JLabel		   topMerchantLabel;
   private JPanel		   playerPanel;
   private JPanel		   merchantPanel;
   private JButton          buy;               // put price of selected
                                                // items in parenthesis on
                                                // button
   private JButton          sell;              // put price of selected
                                                // items in parenthesis on
   // button
   private JButton          leave;
   private JLabel           playerBank;
   private JLabel           merchantBank;
   private Player           thePlayer;
   private TradePort        theMerchant;
   private DefaultListModel merchantList;
   private DefaultListModel playerList;
 
   /**
    * Constructor needs a player. Has no default constructor because you need a
    * player to play
    * 
    * @param player
    *          The player of the game in the correct location
    */
   public TradingMenu(Player player)
   {
 	super("Trading");
     requestFocus();
     addWindowFocusListener(this);
     thePlayer = player;
     makeMerchant();
     addMerchantModel();
     addPlayerModel();
     playersInventory = new JList(playerList);
     merchantsInventory = new JList(merchantList);
 
     playerScroll = new JScrollPane(playersInventory);
     playerScroll
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     playerScroll
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
     merchantScroll = new JScrollPane(merchantsInventory);
     merchantScroll
         .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
     merchantScroll
         .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
     buy = new JButton("<-- Buy ---");
     sell = new JButton("--- Sell -->");
     leave = new JButton("Leave Shop");
     
     topPlayerLabel = new JLabel("Player");
     topMerchantLabel = new JLabel("Merchant");
 
     playerBank = new JLabel("Player Funds: $" + player.getMoney());
     merchantBank = new JLabel("Merchant Funds: $" + theMerchant.getMoney());
     
     playerPanel = new JPanel();
     playerPanel.setLayout(new BorderLayout());
     playerPanel.add(topPlayerLabel, BorderLayout.NORTH);
     playerPanel.add(playerScroll, BorderLayout.CENTER);
     playerPanel.add(playerBank, BorderLayout.SOUTH);
 
     buttonPanel = new JPanel();
     buttonPanel.setLayout(new GridLayout(5, 1));
     buttonPanel.add(new JLabel());
     buttonPanel.add(buy);
     buttonPanel.add(sell);
     buttonPanel.add(leave);
     buttonPanel.add(new JLabel());
     
     merchantPanel = new JPanel();
     merchantPanel.setLayout(new BorderLayout());
     merchantPanel.add(topMerchantLabel, BorderLayout.NORTH);
     merchantPanel.add(merchantScroll, BorderLayout.CENTER);
     merchantPanel.add(merchantBank, BorderLayout.SOUTH);
 
     setLayout(new GridLayout(1, 3));
     //add(playerScroll);
     add(playerPanel);
     add(buttonPanel);
     add(merchantPanel);
     //add(merchantScroll);
     //add(playerBank);
     //add(new JLabel());
     //add(merchantBank);
 
     ButtonListener handler = new ButtonListener();
     buy.addActionListener(handler);
     sell.addActionListener(handler);
     leave.addActionListener(handler);

   }
 
   private void makeMerchant()
   {
     if (thePlayer.hasBeenHere(thePlayer.getLoc()))
     {
       theMerchant = new TradePort(thePlayer.getPort(thePlayer.getLoc()));
     } else
     {
       theMerchant = new TradePort(thePlayer.getLoc());
     }
   }
 
   private void addPlayerModel()
   {
     playerList = new DefaultListModel();
     Vector<Item> v = thePlayer.getInventory();
     for (Item item : v)
     {
       //item.modify();
       playerList.addElement(item);
     }
   }
 
   private void addMerchantModel()
   {
     merchantList = new DefaultListModel();
     Vector<Item> v = theMerchant.getInventory();
     for (Item item : v)
     {
       item.modify();
       merchantList.addElement(item);
     }
 
   }
 
   private void updateBanks()
   {
     playerBank.setText("" + thePlayer.getMoney());
     merchantBank.setText("" + theMerchant.getMoney());
   }
 
   private class ButtonListener implements ActionListener
   {
 
     @Override
     public void actionPerformed(ActionEvent event)
     {
       try
       {
         if (event.getSource() == buy)
         {
           Item item = (Item) merchantsInventory.getSelectedValue();
           merchantList.removeElement(item);
           playerList.addElement(item);
           theMerchant.sale((double) item.getModifiedPrice(), item);
           thePlayer.purchase((double) item.getModifiedPrice(), item);
           updateBanks();
           item.unModify();
           repaint();
         } else if (event.getSource() == sell)
         {
           Item item = (Item) playersInventory.getSelectedValue();
           playerList.removeElement(item);
           merchantList.addElement(item);
           thePlayer.sale((double) item.getModifiedPrice(), item);
           theMerchant.purchase((double) item.getModifiedPrice(), item);
           updateBanks();
           repaint();
         } else if (event.getSource() == leave)
         {
           dispose();
           // You are welcome -Jon -- Thank you -Ryan
           Orbital loc = ((TradePort) theMerchant).getLocale();
           thePlayer.setLoc(loc.getParent());
         } 
       } catch (Exception e)
       {
 
       }
     }
 
   }
 
   public void windowGainedFocus(WindowEvent arg0)
   {
   }
 
   public void windowLostFocus(WindowEvent arg0)
   {
     dispose();
   }
 }
