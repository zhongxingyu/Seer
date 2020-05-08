 package gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.JButton;
 import javax.swing.JOptionPane;
 
 import deck.Deck;
 import deck.MTGDeckMain;
 
 public class StatsButton extends JButton implements ActionListener {
 	private Deck theDeck;
 	public StatsButton(String string, Deck deck) {
 		super(string);
 		this.theDeck = deck;
 		this.addActionListener(this);
 		
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		JOptionPane.showMessageDialog(this, getStats(theDeck));
 		
 	}
 
 	private String getStats(Deck theDeck2) {
 		ArrayList<String> stats = theDeck2.statistics();
 		String stat = MTGDeckMain.messages.getString("stats0");
 		stat = stat.concat(" \n ");
		for(int i = 1; i <= stats.size(); i++){
			stat = stat.concat(MTGDeckMain.messages.getString("stats" + i) + " " + stats.get(i-1));
 			stat = stat.concat(" \n ");
 		}
 		
 		return stat;
 	}
 
 }
