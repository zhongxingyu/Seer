 package Client.Presentation;
 
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.DecimalFormat;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import Client.Entities.PeriodInfo;
 
 import Client.Entities.Player;
 
 public class ResultPanel extends TypedPanel {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Insets inset_standard = new Insets(0,0,0,0);
 	private Insets inset_newLine_bottom = new Insets(5,0,15,0);
 	private Insets inset_newLine_top = new Insets(15,0,5,0);
 	private DecimalFormat format = new DecimalFormat();
 	
 	public ResultPanel(FinishReason reason) {
 		super(PanelType.Endbildschirm);
 		
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.WEST;
 		this.setLayout(new GridBagLayout());
 		
 		JLabel title1 = new JLabel("Das Spiel ist vorbei");
 		title1.setFont(new Font("Serif", Font.BOLD, 14));
 		c.insets = inset_newLine_bottom;
 		this.add(title1,c);
 		c.gridy++;
 		c.insets = inset_standard;
 
 		MainWindow mw = MainWindow.getInstance();
 		mw.west.setVisible(true);
 		for(Component comp : mw.west.getComponents()){
			comp.setVisible(false);
 		}
 		mw.next.setVisible(false);
 		mw.prev.setVisible(false);
 		mw.berichtButton.setVisible(true);
 		
 		//Results ausrechnen
 		Map<Player,Double> resultMap = new TreeMap<Player,Double>();
 		for(Iterator<Player> player_it = Player.getPlayers().iterator(); player_it.hasNext();){
 			Player p = player_it.next();
 			if (p.isInsolvent()) continue;
 			double maxresult = 0;
 			for(int i = 0; i<PeriodInfo.getNumberOfActPeriod(); i++){
 				maxresult += p.getCompanyResult(i).profit;
 			}
 			resultMap.put(p, maxresult);
 		}
 		
 		for(Iterator<Entry<Player,Double>> result_it = resultMap.entrySet().iterator(); result_it.hasNext(); ){
 			Entry<Player,Double> res = result_it.next();
 			this.add(new JLabel(res.getKey().getName()),c);
 			c.gridx++;
 			this.add(new JLabel(format.format(res.getValue()) + ""),c);
 			c.gridy++;
 			
 			c.gridx = 0;
 		}
 		
 		c.insets = inset_newLine_top;
 		switch(reason){
 			case EndOfRoundsReached:
 				this.add(new JLabel("Das Spiel wurde beendet, da die maximale Anzahl an Runden erreicht wurde"),c);
 				break;
 			case OnePlayerLeft:
 				this.add(new JLabel("Das Spiel wurde beendet, da nur noch ein Spieler spielbereit ist."),c);
 				break;
 		}
 		
 	}
 	
 	public enum FinishReason{
 		EndOfRoundsReached,
 		OnePlayerLeft
 	}
 
 }
