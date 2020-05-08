 package window.panel;
 
 import game.player.Player;
 import game.player.RealPlayer;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import window.AppliWindow;
 
 public class PanelInfoRealPlayer extends JPanel {
 	
 	private static final long serialVersionUID = 8438028753538578123L;
 	private int widthPanelRealPlayer = 200;
 	private int heightPanelRealPlayer = 100;
	private int visibleHeightPanelRealPlayer = 70;
 	
 	private boolean buildToolsVisible = false;
 	
 	public boolean isBuildToolsVisible() {
 		return this.buildToolsVisible;
 	}
 	
 	public void setBuildToolsVisible(boolean buildToolsVisible) {
 		this.buildToolsVisible = buildToolsVisible;
 	}
 
 	public PanelInfoRealPlayer() {
 		super();
 	}
 	
 	/**
 	 * Builds the PanelInfoRealPlayer for the first time
 	 */
 	public void buildPanel(int widthWindow, final int heightWindow, final RealPlayer realPlayer) {
 		
 		this.setBounds(0, heightWindow - visibleHeightPanelRealPlayer, widthPanelRealPlayer, heightPanelRealPlayer);
 		this.setVisible(true);
 		
 		//final RealPlayer realPlayer = (RealPlayer) Game.getInstance().getPlayerManager().getRealPlayer();
 		JLabel label = new JLabel(realPlayer.getInfosPlayer());
 		
 		this.add(label);
 		this.setBackground(null);
 		
 		JPanel panelAvailableBases = new JPanel();
 		
 		JButton buttonGunTower = new JButton("GunTower (-$50)");
 		buttonGunTower.addActionListener(new ActionListener() {
 								
 		public void actionPerformed(ActionEvent e) {
 			if (realPlayer.getBank().getMoney() >= 50) {
 				realPlayer.setBuildingTower(true);
 			}
 			else {
 				System.out.println("You don't have enough money !");
 			}
 			
 		}
 		});
 		panelAvailableBases.add(buttonGunTower);
 		
 		this.setBackground(Color.RED);
 		this.add(panelAvailableBases);
 		
 		// add actionListener to show or hide the panel
 		this.addMouseListener(new MouseListener() {
 			
 			// shows the panel
 			public void showPanel() {
 				AppliWindow.getInstance().getPanelInfoRealPlayer().setBounds(0, heightWindow - heightPanelRealPlayer, widthPanelRealPlayer, heightPanelRealPlayer);
 				setBuildToolsVisible(true);
 			}
 			
 			// hide the panel
 			public void hidePanel() {
 				AppliWindow.getInstance().getPanelInfoRealPlayer().setBounds(0, heightWindow - visibleHeightPanelRealPlayer, widthPanelRealPlayer, heightPanelRealPlayer);
 				setBuildToolsVisible(false);
 			}
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if(isBuildToolsVisible() ==  true) {
 					hidePanel();
 				}
 				else {
 					showPanel();
 				}
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 			}
 		});
 
 	}
 	
 	/**
 	 * Updates the infos in the PanelInfoRealPlayer
 	 */
 	public void update(Player player, int nbTotalBases, int nbBasesPlayer) {
 		
 		
 		StringBuilder sb = new StringBuilder(player.getName());
 		sb.append(" : $");
 		sb.append(String.format("%.0f", player.getBank().getMoney()));
 		sb.append(" | ");
 		sb.append(nbBasesPlayer);
 		sb.append("/");
 		sb.append(nbTotalBases);
 		
 		// The label is the first Component in the panel
 		//JPanel panelInfoRealPlayer = (JPanel) panelRealPlayer.getComponent(0);
 		JLabel labelInfoRealPlayer = (JLabel) this.getComponent(0);
 		// we can update the text of the button
 		labelInfoRealPlayer.setText(sb.toString());
 		
 		
 	}
 	
 }
