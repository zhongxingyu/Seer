 package gamed.client.SpeedRisk;
 
 import gamed.client.MediaDownloader;
 import java.awt.Point;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import javax.swing.JPopupMenu;
 import javax.swing.JMenuItem;
 import javax.swing.SwingWorker;
 
 public class Display extends gamed.Game implements PropertyChangeListener, ActionListener
 {
 	private gamed.Server server;
 	private RiskBoard board;
 	private int selectedCountry = -1;
 	private int reserve;
 	private RiskPlayer me;
 	private boolean atWar;
 	private ArmyGenerationTimer armyGenerationTimer;
 	private final StatusPanel statusPanel;
 
 	public Display(gamed.Server server, RiskBoard board)
 	{
 		this.server = server;
 		this.board = board;
 		this.statusPanel = new StatusPanel(board, server);
 		initComponents();
 		readyRadio.setOpaque(false);
 		notReadyRadio.setOpaque(false);
 		armyGenerationProgress.setVisible(false);
 		add(statusPanel);
 		statusPanel.setVisible(false);
 		atWar = false;
 		byte cmd[] =
 		{
 			PLAYER_STATUS, 0, 0, 0
 		};
 		server.sendGameData(cmd);
 	}
 
 	@Override
 	public void paintComponent(java.awt.Graphics g)
 	{
 		board.paintComponent(g);
 	}
 
 	public void joinedGame()
 	{
 		for (Country country: board.countries)
 		{
 			country.set(null, 1);
 		}
 		progress.setValue(0);
 		final MediaDownloader mediaDownloader = new MediaDownloader(server, board.getMediaRequestors());
 		mediaDownloader.addPropertyChangeListener(this);
 		mediaDownloader.execute();
 		repaint();
 	}
 
 	public void propertyChange(PropertyChangeEvent pce)
 	{
 		String propertyName = pce.getPropertyName();
 		if (propertyName.equals("progress"))
 		{
 			progress.setValue((Integer) pce.getNewValue());
 		}
 		else if (propertyName.equals("state") && pce.getNewValue() == SwingWorker.StateValue.DONE)
 		{
 			loadingText.setVisible(false);
 			remove(progress);
 			statusPanel.setVisible(true);
 			jButton1.setVisible(true);
 			if (!atWar)
 			{
 				readyRadio.setVisible(true);
 				notReadyRadio.setVisible(true);
 			}
 			for (int i = 0; i < board.countries.length; i++)
 			{
 				board.countries[i].setSelected(board.countries[i].isSelected);
 			}
 			statusPanel.mediaReady();
 			repaint();
 		}
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
 	 */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
 
         buttonGroup1 = new javax.swing.ButtonGroup();
         jProgressBar1 = new javax.swing.JProgressBar();
         progress = new javax.swing.JProgressBar();
         loadingText = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         readyRadio = new javax.swing.JRadioButton();
         notReadyRadio = new javax.swing.JRadioButton();
         armyGenerationProgress = new javax.swing.JProgressBar();
 
         setPreferredSize(new java.awt.Dimension(650, 375));
         addMouseListener(new java.awt.event.MouseAdapter()
         {
             public void mousePressed(java.awt.event.MouseEvent evt)
             {
                 formMousePressed(evt);
             }
             public void mouseReleased(java.awt.event.MouseEvent evt)
             {
                 formMouseReleased(evt);
             }
         });
         setLayout(null);
 
         progress.setFocusable(false);
         progress.setStringPainted(true);
         add(progress);
         progress.setBounds(10, 325, 630, 20);
 
         loadingText.setFont(new java.awt.Font("DejaVu Sans", 0, 28)); // NOI18N
         loadingText.setText("Loading Media...");
         add(loadingText);
         loadingText.setBounds(200, 150, 250, 70);
 
         jButton1.setVisible(false);
         jButton1.setText("Quit Game");
         jButton1.setDefaultCapable(false);
         jButton1.setFocusable(false);
         jButton1.setRequestFocusEnabled(false);
         jButton1.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 jButton1ActionPerformed(evt);
             }
         });
         add(jButton1);
        jButton1.setBounds(546, 340, 100, 23);
 
         readyRadio.setBackground(new Color(0,true));
         buttonGroup1.add(readyRadio);
         readyRadio.setText("Ready");
         readyRadio.setVisible(false);
         readyRadio.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 readyRadioActionPerformed(evt);
             }
         });
         add(readyRadio);
        readyRadio.setBounds(250, 280, 57, 23);
 
         notReadyRadio.setBackground(new Color(0, true));
         buttonGroup1.add(notReadyRadio);
         notReadyRadio.setSelected(true);
         notReadyRadio.setText("Not Ready");
         notReadyRadio.setVisible(false);
         notReadyRadio.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 readyRadioActionPerformed(evt);
             }
         });
         add(notReadyRadio);
        notReadyRadio.setBounds(250, 300, 77, 23);
 
         armyGenerationProgress.setFocusable(false);
         armyGenerationProgress.setStringPainted(true);
         add(armyGenerationProgress);
        armyGenerationProgress.setBounds(230, 350, 200, 17);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
 		server.quitGame();
     }//GEN-LAST:event_jButton1ActionPerformed
 
     private void readyRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_readyRadioActionPerformed
 		sendReady(readyRadio.getModel().isSelected());
     }//GEN-LAST:event_readyRadioActionPerformed
 
     private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
 		if (evt.isPopupTrigger())
 		{
 			byte c = (byte) getCountryAt(evt.getPoint());
 			if (c != -1 && me.equals(board.countries[c].owner))
 			{
 				if (selectedCountry != -1
 					&& board.countries[selectedCountry].armies > 1
 					&& board.borders(selectedCountry, c))
 				{
 					showMovePopup(evt, selectedCountry, c);
 					setSelectedCountry(-1);
 				}
 				else if (reserve > 0)
 				{
 					showPlacementPopup(evt, c);
 				}
 			}
 		}
     }//GEN-LAST:event_formMousePressed
 
     private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
 		byte c = (byte) getCountryAt(evt.getPoint());
 		if (c != -1)
 		{
 			if (me.equals(board.countries[c].owner))
 			{
 				if (selectedCountry != -1
 					&& board.countries[selectedCountry].armies > 1
 					&& board.borders(selectedCountry, c))
 				{
 					if (evt.isPopupTrigger())
 					{
 						showMovePopup(evt, selectedCountry, c);
 					}
 					else
 					{
 						moveArmies((byte) selectedCountry, c, (byte) (board.countries[selectedCountry].armies - 1));
 
 					}
 				}
 				else if (reserve > 0)
 				{
 					if (evt.isPopupTrigger())
 					{
 						showPlacementPopup(evt, c);
 					}
 					else if (evt.getClickCount() > 1)
 					{
 						placeAllArmiesAt(c);
 					}
 				}
 				setSelectedCountry(c);
 				repaint();
 			}
 			else if (selectedCountry != -1
 					 && board.borders(selectedCountry, c))
 			{
 				attack(c);
 			}
 		}
     }//GEN-LAST:event_formMouseReleased
 
 	private void showPlacementPopup(java.awt.event.MouseEvent evt, int country)
 	{
 		showPopup(evt, -1, country, reserve);
 	}
 
 	private void showMovePopup(java.awt.event.MouseEvent evt, int from, int to)
 	{
 		if (atWar)
 		{
 			showPopup(evt, from, to, board.countries[from].armies - 1);
 		}
 	}
 
 	private void showPopup(java.awt.event.MouseEvent evt, int from, int to, int armies)
 	{
 		JPopupMenu popup = new JPopupMenu("Move");
 		float step = (float) armies / 5;
 		float value = armies;
 		int last = 0;
 		int v = armies;
 		do
 		{
 			if (last != v)
 			{
 				JMenuItem i = new JMenuItem(Integer.toString(v));
 				i.setActionCommand(String.format("%d:%d:%d", from, to, v));
 				i.addActionListener(this);
 				popup.add(i);
 				last = v;
 			}
 			value -= step;
 			v = (int) Math.ceil(value);
 		}
 		while (value > 0);
 		popup.show(evt.getComponent(), evt.getX(), evt.getY());
 	}
 
 	public void actionPerformed(ActionEvent evt)
 	{
 		String args[] = evt.getActionCommand().split(":");
 		byte from = Byte.parseByte(args[0]);
 		byte to = Byte.parseByte(args[1]);
 		byte armies = Byte.parseByte(args[2]);
 		if (from == -1)
 		{
 			placeArmiesAt(to, armies);
 		}
 		else
 		{
 			moveArmies(from, to, armies);
 		}
 	}
 
 	public void handleGameData(byte[] data)
 	{
 		RiskPlayer player;
 		switch (data[0])
 		{
 			case PLAYER_JOIN:
 			case PLAYER_QUIT:
 				server.askForPlayerList();
 				break;
 			case SR_ERROR:
 				System.err.println("Error: " + data[1]);
 				System.err.flush();
 				break;
 			case READY:
 				player = statusPanel.get(data[1]);
 				if (player != null)
 					player.setReady(true);
 				break;
 			case NOTREADY:
 				player = statusPanel.get(data[1]);
 				if (player != null)
 					player.setReady(false);
 				break;
 			case START_PLACING:
 				statusPanel.setPhase("Placing Armies");
 				notReadyRadio.getModel().setSelected(true);
 				setSelectedCountry(-1);
 				statusPanel.resetReady();
 				break;
 			case BEGIN:
 				atWar = true;
 				statusPanel.setPhase("At War");
 				readyRadio.setVisible(false);
 				notReadyRadio.setVisible(false);
 				armyGenerationTimer = new ArmyGenerationTimer(armyGenerationProgress);
 				armyGenerationProgress.setVisible(true);
 				break;
 			case GET_ARMIES:
 				reserve = data[3] & 0xFF;
 				statusPanel.setReserve(reserve);
 				break;
 			case NEXT_ARMY_GENERATION:
 				armyGenerationTimer.set(data[1]);
 				break;
 			case ATTACK_RESULT:
 			case MOVE_RESULT:
 				board.countries[data[4]].set(statusPanel.get(data[5]), data[6] & 0xFF);
 				RiskPlayer old_owner = board.countries[data[8]].owner;
 				board.countries[data[8]].set(statusPanel.get(data[9]), data[10] & 0xFF);
 				if (old_owner.id != data[9])
 				{
 					if (data[9] == me.id)
 					{
 						setSelectedCountry(data[8]);
 					}
 					else if (selectedCountry == data[8])
 					{
 						setSelectedCountry(-1);
 					}
 				}
 				break;
 			case GAME_STATUS:
 				for (int i = 1; i <= board.countries.length; i++)
 				{
 					board.countries[data[i * 4]].set(statusPanel.get(data[i * 4 + 1]), data[i * 4 + 2] & 0xFF);
 				}
 				break;
 			case PLAYER_STATUS:
 				me = statusPanel.get(data[1]);
 				reserve = data[3] & 0xFF;
 				statusPanel.setOwner(me);
 				break;
 			case COUNTRY_STATUS:
 				board.countries[data[4]].set(statusPanel.get(data[5]), data[6] & 0xFF);
 				break;
 			case DEFEAT:
 				player = statusPanel.get(data[1]);
 				if (player != null)
 					player.setReady(false);
 				break;
 			case VICTORY:
 				statusPanel.setPhase("Game Over");
 				armyGenerationTimer.stop();
 				armyGenerationProgress.setVisible(false);
 				break;
 		}
 		repaint();
 	}
 
 	public void updatePlayers(gamed.Player[] players)
 	{
 		statusPanel.updatePlayers(players, this);
 	}
 
 	public void renamePlayer(gamed.Player player)
 	{
 		statusPanel.get(player.id).setPlayerName(player.name);
 	}
 
 	private void sendReady(boolean ready)
 	{
 		byte cmd[] =
 		{
 			ready ? READY : NOTREADY, 0, 0, 0
 		};
 		server.sendGameData(cmd);
 	}
 
 	private int getCountryAt(Point p)
 	{
 		for (Country country : board.countries)
 		{
 			if (country.contains(p))
 			{
 				return country.id;
 			}
 		}
 		return -1;
 	}
 
 	private void setSelectedCountry(int c)
 	{
 		if (selectedCountry != c)
 		{
 			if (selectedCountry != -1)
 			{
 				board.countries[selectedCountry].setSelected(false);
 			}
 			if (c != -1)
 			{
 				board.countries[c].setSelected(true);
 			}
 		}
 		selectedCountry = c;
 	}
 
 	private void attack(byte to)
 	{
 		if (board.countries[selectedCountry].armies > 1)
 		{
 			byte cmd[] =
 			{
 				ATTACK,
 				(byte) selectedCountry,
 				to,
 				(byte) (board.countries[selectedCountry].armies - 1)
 			};
 			server.sendGameData(cmd);
 		}
 	}
 
 	private void moveArmies(byte from, byte to, byte armies)
 	{
 		if (board.countries[from].armies > 1)
 		{
 			byte cmd[] =
 			{
 				MOVE,
 				from,
 				to,
 				armies
 			};
 			server.sendGameData(cmd);
 		}
 	}
 
 	private void placeArmiesAt(byte to, byte armies)
 	{
 		if (reserve > 0)
 		{
 			byte cmd[] =
 			{
 				PLACE,
 				0,
 				to,
 				armies
 			};
 			server.sendGameData(cmd);
 		}
 	}
 
 	private void placeAllArmiesAt(byte to)
 	{
 		placeArmiesAt(to, (byte) reserve);
 	}
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JProgressBar armyGenerationProgress;
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton jButton1;
     private javax.swing.JProgressBar jProgressBar1;
     private javax.swing.JLabel loadingText;
     private javax.swing.JRadioButton notReadyRadio;
     private javax.swing.JProgressBar progress;
     private javax.swing.JRadioButton readyRadio;
     // End of variables declaration//GEN-END:variables
 	public static final byte PLAYER_JOIN = 0;
 	public static final byte MESSAGE = 1;
 	public static final byte SR_ERROR = 2;
 	public static final byte READY = 3;
 	public static final byte NOTREADY = 4;
 	public static final byte START_PLACING = 5;
 	public static final byte BEGIN = 6;
 	public static final byte MOVE = 7;
 	public static final byte ATTACK = 8;
 	public static final byte PLACE = 9;
 	public static final byte GET_ARMIES = 10;
 	public static final byte ATTACK_RESULT = 11;
 	public static final byte MOVE_RESULT = 12;
 	public static final byte GAME_STATUS = 13;
 	public static final byte PLAYER_STATUS = 14;
 	public static final byte COUNTRY_STATUS = 15;
 	public static final byte DEFEAT = 16;
 	public static final byte VICTORY = 17;
 	public static final byte PLAYER_QUIT = 18;
 	public static final byte NEXT_ARMY_GENERATION = 19;
 }
