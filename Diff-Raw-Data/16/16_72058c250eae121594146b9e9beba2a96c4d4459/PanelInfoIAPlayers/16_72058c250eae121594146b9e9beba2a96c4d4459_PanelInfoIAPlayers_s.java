 package window.panel;
 
 import game.player.Player;
 
 import java.awt.Color;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class PanelInfoIAPlayers extends JPanel {
 	
 	private static final long serialVersionUID = -6992006436315699274L;
 	private int widthPanelIAPlayer = 290;
	private int heightPanelIAPlayer = 40;
 	
 	public PanelInfoIAPlayers() {
 		super();
 	}
 	
 	/**
 	 * Builds the PanelInfoIAPlayers for the first time
 	 */
 	public void buildPanel(int widthWindow, int heightWindow, Player player) {
 		this.setBounds(widthWindow - widthPanelIAPlayer, heightWindow - heightPanelIAPlayer, widthPanelIAPlayer, heightPanelIAPlayer);
 		this.setVisible(true);
 		this.setBackground(Color.BLACK);
 		
 		JPanel panel = new JPanel();
 		JLabel label = new JLabel(player.getInfosPlayer());
 		label.setForeground(player.getColor());
 		panel.add(label);
 		panel.setBackground(null);
 		this.add(panel);
 	}
 	
 	/**
 	 * Updates the infos in the PanelInfoIAPlayers
 	 */
 	public void updateComponent(int indexIAPlayer, Player player, int nbTotalBases, int nbBasesPlayer) {
 		// the panel of the player
 		JPanel panel = (JPanel) this.getComponent(indexIAPlayer);
 		// the label of the player = it's the first element
 		JLabel label = (JLabel) panel.getComponent(0);
 		
 		StringBuilder sb = new StringBuilder("<html>");
 		if(player.getIsDead()) {
 			sb.append(player.getName());
 			sb.append("<br />");
 			sb.append("XdeadX");
 			label.setText(sb.toString());
 		}
 		else {
 			sb.append(player.getName());
 			sb.append("<br /> $");
 			sb.append(String.format("%.0f", player.getBank().getMoney()));
 			sb.append(" | ");
 			sb.append(nbBasesPlayer);
 			sb.append("/");
 			sb.append(nbTotalBases);
 			sb.append("</html>");
 			
 			// we change the text of the label
 			label.setText(sb.toString());
 		}
 	}
 
 }
