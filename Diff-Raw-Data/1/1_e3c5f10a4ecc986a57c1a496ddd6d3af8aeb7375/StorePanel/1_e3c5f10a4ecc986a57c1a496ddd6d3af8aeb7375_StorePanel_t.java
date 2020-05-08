 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Random;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 
 public class StorePanel extends Building{
 
 	public StorePanel(GameMain frame, Player p, JPanel oldPanel) {
 		super(frame, p, oldPanel);
 		JButton Buy = new JButton("Buy");
 		JButton Sell = new JButton("Sell");
 		JButton Back = new JButton("Back");
 		Sell.addActionListener(new SellListener(p, frame, oldPanel));
		Buy.addActionListener(new BuyListener(p, frame, oldPanel));
 		add(Buy);
 		add(Sell);
 		add(Back);
 	}
 	
     private class SellListener implements ActionListener
     {
     	Player p;
     	GameMain frame;
     	JPanel oldpanel;
     	public SellListener(Player p, GameMain frame, JPanel oldpanel){
     		this.p=p;
     		this.frame=frame;
     		this.oldpanel=oldpanel;
     	}
        public void actionPerformed (ActionEvent event)
     	{
     	   SellPanel p = new SellPanel(frame, GameMain.getCurrPlayer(), oldpanel);
 			frame.setContentPane(p);
     	}
     }
     
     private class BuyListener implements ActionListener{
         Player p;
         GameMain frame;
         JPanel oldPanel;
         public BuyListener(Player p, GameMain frame, JPanel oldPanel){
             this.p = p;
             this.frame = frame;
             this.oldPanel = oldPanel;
         }
         public void actionPerformed(ActionEvent event){
             BuyPanel p = new BuyPanel(frame, GameMain.getCurrPlayer(), oldPanel);
             frame.setContentPane(p);
         }
     }
 }
 
 
