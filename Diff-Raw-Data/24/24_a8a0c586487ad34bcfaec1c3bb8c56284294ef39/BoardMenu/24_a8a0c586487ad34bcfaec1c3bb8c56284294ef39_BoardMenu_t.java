 package edu.brown.cs32.MFTG.gui.gameboard;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JSeparator;
 import javax.swing.SwingConstants;
 import javax.swing.ToolTipManager;
 
 import edu.brown.cs32.MFTG.gui.Constants.View;
 import edu.brown.cs32.MFTG.gui.MonopolyGui;
 import edu.brown.cs32.MFTG.gui.center.ViewListener;
 import edu.brown.cs32.MFTG.monopoly.Player;
 import edu.brown.cs32.MFTG.tournament.Profile;
 
 public class BoardMenu extends JMenuBar {
 	private Board _board;
 	private MonopolyGui _main;
 	protected JMenu _players;
 	protected List<JMenuItem> _playerItems;
 	protected String _currentPlayer;
 	protected Profile _profile;
 	private JMenuItem _aggregateProperty;
 	private JMenu _boardView;
 	private JButton _delete;
 	public BoardMenu(Board board, MonopolyGui main, Profile profile) {
 		super();
 		_board=board;
 		_main=main;
 		_profile=profile;
 
 		JLabel profileLabel = new JLabel(profile._name);
 		
 		_boardView = new JMenu("Board View");
 		
 		JMenuItem myProperty = new JMenuItem("My Properties");
 		myProperty.addActionListener(new ViewListener(View.ME,_board));
 		_boardView.add(myProperty);
 
 		_aggregateProperty = new JMenuItem("Aggregate Properties");
 		_aggregateProperty.addActionListener(new ViewListener(View.AGGREGATE,_board));
 		_boardView.add(_aggregateProperty);
 
 		JMenuItem colorGroup = new JMenuItem("Color Groups");
 		colorGroup.addActionListener(new ViewListener(View.COLOR,_board));
 		_boardView.add(colorGroup);
 
 
 		_players = new JMenu("Players");
 		_players.setFont(new Font("playerFont", Font.BOLD, 14));
 
 		_playerItems = new ArrayList<>();
 		if(_profile!=null) {
 			for(String p: _profile.getPlayerNames()) {
 				JMenuItem player = new JMenuItem(p);
 				_playerItems.add(player);
 				player.addActionListener(new PlayerListener(p));
 				_players.add(player);
 			}
 		}
 		JMenuItem newplayer = new JMenuItem("Create New Player");
 		newplayer.setFont(new Font("newplayerFont", Font.ITALIC, 12));
 		_playerItems.add(newplayer);
 		newplayer.addActionListener(new NewPlayerListener());
 		_players.addSeparator();
 		_players.add(newplayer);
 		
 		_currentPlayer=_playerItems.get(0).getText();
 		_players.setText(_currentPlayer);
 		_board.setHeuristics(_profile.getPlayer(_currentPlayer));
 
 		JButton save = new JButton("Save");
 		save.addActionListener(new SaveListener());
 		
 		_delete = new JButton("Delete");
 		_delete.addActionListener(new DeleteListener());
 
 		add(profileLabel);
 
 		JCheckBox toolTips = new JCheckBox("Tool Tips On: ", true);
 		toolTips.setHorizontalTextPosition(SwingConstants.LEADING);
 		toolTips.addItemListener(new ToolTipListener());
 
 		
 		JSeparator separator = new JSeparator(JSeparator.VERTICAL);
 		Dimension size = new Dimension(separator.getPreferredSize().width+3,separator.getMaximumSize().height);
 		separator.setMaximumSize(size);
 		
 		add(separator);
 		add(_boardView);
 		add(_players);
 		add(save);
 		add(_delete);
 		add(Box.createHorizontalGlue());
 		add(toolTips);
 
 
 	}
 	
 	protected void removeDeleteButton() {
 		remove(_delete);
 	}
 	
 	protected void removeAggregateButton() {
 		_boardView.remove(_aggregateProperty);
 	}
 	
 	public void resetPlayerMenu() {
 		_players.removeAll();
 		_playerItems.clear();
 		
 		if(_profile!=null) {
 			for(String p: _profile.getPlayerNames()) {
 				JMenuItem player = new JMenuItem(p);
 				_playerItems.add(player);
 				player.addActionListener(new PlayerListener(p));
 				_players.add(player);
 			}
 		}
 		JMenuItem newplayer = new JMenuItem("Create New Player");
 		_playerItems.add(newplayer);
 		newplayer.addActionListener(new NewPlayerListener());
 		_players.addSeparator();
 		_players.add(newplayer);
 		
 		_currentPlayer=_playerItems.get(0).getText();
 		_players.setText(_currentPlayer);
 		_board.setHeuristics(_profile.getPlayer(_currentPlayer));
 		
 	}
 	
 	private class ToolTipListener implements ItemListener{
 
 		@Override
 		public void itemStateChanged(ItemEvent e) {
 			if(e.getStateChange()==ItemEvent.SELECTED){
 				ToolTipManager.sharedInstance().setEnabled(true);
 			}
 			else{
 				ToolTipManager.sharedInstance().setEnabled(false);
 			}
 		}
 		
 	}
 	protected class PlayerListener implements ActionListener{
 		private String _player;
 		public PlayerListener(String player) {
 			_player=player;
 		}
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			_players.setText(_player);
 			_board.setHeuristics(_profile.getPlayer(_player));
 			_currentPlayer=_player;
 		}
 
 	}
 
 	protected class NewPlayerListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			_board.setHeuristics(new Player(-1,""));
 			_players.setText("Create New Player");
 			_currentPlayer=null;
 		}
 
 	}
 
 	protected class SaveListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Player newPlayer = _board.getHeuristics();
 			//if it's a new player
 			if(_currentPlayer==null) {
 				String newPlayerName=JOptionPane.showInputDialog("New player name: ");
 				while(true) {
 					if(newPlayerName==null) {
 						return;
 					}
 					if(_profile.addPlayer(newPlayer, newPlayerName)) {
 						break;
 					}
 					newPlayerName=JOptionPane.showInputDialog("Please choose an unused name: ");
 				}
 				resetPlayerMenu();
 				_currentPlayer=newPlayerName;
 				_players.setText(_currentPlayer);
 				_board.setHeuristics(_profile.getPlayer(_currentPlayer));
 				
 			}
 			else {
 				_profile.replacePlayer(newPlayer, _currentPlayer);
 				_main.saveProfiles();
 			}
 		}
 
 	}
 	
 	protected class DeleteListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(_currentPlayer ==null) {
 				int remove=JOptionPane.showConfirmDialog(_main, "Are you sure you want to delete this new player and start over?");
 				if(remove!=0) {
 					return;
 				}
 				_board.setHeuristics(new Player(-1,""));		
 			}
 			else {
 				int remove=JOptionPane.showConfirmDialog(_main, "Are you sure you want to remove the player \""+_currentPlayer+"\"?\nThis action cannot be reversed.");
 				if(remove!=0) {
 					return;
 				}
 				_profile.removePlayer(_currentPlayer);
 				for(int i = 0; i<_playerItems.size(); i++) {
 					JMenuItem item = _playerItems.get(i);
 					if(item.getText().equals(_currentPlayer)) {
 						_players.remove(item);
 						_playerItems.remove(item);
 						break;
 					}
 				}
 				_players.setText(_playerItems.get(0).getText());

 				
				if (_playerItems.size() > 1) {
 					_currentPlayer = _playerItems.get(0).getText();
					_board.setHeuristics(_profile.getPlayer(_players.getText()));
				}
				else {
 					_currentPlayer = null;
					_board.setHeuristics(new Player(-1,""));
				}

 			}
 			_main.saveProfiles();
 		}		
 	}
 
 }
