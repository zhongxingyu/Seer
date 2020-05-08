 import javax.swing.*;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.*;
 
 public class MarketPanel extends JPanel {
 	private JPanel mainPanel;
 	private Market market;
     private Player plr;
 	private ArrayList<JPanel> itemPanels;
 	public ArrayList<TradeGood> dummyData;
 	private boolean dummy;
 
 	public MarketPanel(Market market, Player p) {
 		this.setLayout(new GridLayout(0, 2));
 		dummy = false;
 		this.market = market;
         this.plr = p;
 		this.setupDisplay();
 	}
 
     public void setPlayer(Player plr) {
         this.plr = plr;
     }
 
 	private void setupDummyData() {
 		Random rand = new Random();
 		dummyData = new ArrayList<TradeGood>();
 
 		dummy = true;
 		for (int i = 0; i < 200; i++) {
 			short type = (short)rand.nextInt(TradeGood.ITEMCOUNT);
 			dummyData.add(new TradeGood(type));
 		}
 	}
 
 	private void setupDisplay() {
 		itemPanels = new ArrayList<JPanel>();
 		for (TradeGood good : market.getMarketGoods()) {
 			JButton sellButton = new JButton("Sell");
 			JButton buyButton = new JButton("Buy");
 
 			JLabel typeLabel = new JLabel(good.toString());
 			JLabel priceLabel = new JLabel("price: $" + market.getPrice(good));
 			JLabel quantity = new JLabel("amount: " + market.getQuantity(good));
 
             JPanel itemPanel = new JPanel();
 
            sellButton.addActionListener(new GoodListener(false, good, quantity));
            buyButton.addActionListener(new GoodListener(true, good, quantity));
 
 			itemPanel.setLayout(new BoxLayout(itemPanel, BoxLayout.X_AXIS));
 			itemPanel.add(typeLabel);
 			itemPanel.add(new JSeparator(SwingConstants.VERTICAL));
             itemPanel.add(quantity);
             itemPanel.add(new JSeparator(SwingConstants.VERTICAL));
 			itemPanel.add(priceLabel);
 			itemPanel.add(new JSeparator(SwingConstants.VERTICAL));
 			itemPanel.add(buyButton);
 			itemPanel.add(sellButton);
 			itemPanels.add(itemPanel);
 			this.add(itemPanel);
 		}
 	}
 
     private class GoodListener implements ActionListener {
 
         private boolean buyButton;
         private TradeGood tg;
        private JLabel quantityLabel;
 
        public GoodListener(boolean buyButton, TradeGood tg, JLabel quantityLabel) {
             this.buyButton = buyButton;
             this.tg = tg;
            this.quantityLabel = quantityLabel;
         }
 
         @Override
         public void actionPerformed(ActionEvent actionEvent) {
             if(buyButton) {
                 market.marketSell(plr, tg, 1);
                quantityLabel.setText("amount: " + market.getQuantity(tg));
                 System.out.println(market.getQuantity(tg));
             }
             else {
                 market.marketBuy(plr, tg, 1);
                quantityLabel.setText("amount: " + market.getQuantity(tg));
                 System.out.println(market.getQuantity(tg));
             }
         }
     }
 }
