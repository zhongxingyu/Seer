 /*
 	Mafiamanager - a tool to support the referee of the parlor game "Mafia"
     Copyright (C) 2011  Thomas Högner
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.SortedMap;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 public class Controller extends JPanel{
 
 	private static final long serialVersionUID = 5299138914080396570L;
 
 	// general
 	private SortedMap<String, Player> playerlist;
 	private Board board;
 	// TODO logging the game
 	
 	// roundsaves
 	private int round;
 	private String deprotectPlayer;
 	private ArrayList<String> died;
 	
 	// temporary
 	private String bufferHead;
 	private ArrayList<String> bufferCommand;
 	private ArrayList<String> bufferNote;
 	
 	// gui
 	private GridBagConstraints con;
 	private GridBagConstraints conPlayer;
 	
 	private JFrame frame;
 		private JPanel panelPlayers;
 			private ArrayList<JPanel> panelXplayer;
 				private ArrayList<JLabel> labelXplayer;
 	
 				
 				
 	public Controller(SortedMap<String, Player> _playerlist, Board _board, JFrame _frame){
 		
 		// initialize
 		playerlist = _playerlist;
 		board = _board;
 		frame = _frame;
 		round = 1;
 		died = new ArrayList<String>();
 		bufferCommand = new ArrayList<String>();
 		bufferNote = new ArrayList<String>();
 		
 		// gui
 		setLayout(new GridLayout(0,1));		
 		con = new GridBagConstraints();
 		conPlayer = new GridBagConstraints();
 		conPlayer.gridx = 0;
 		conPlayer.gridy = GridBagConstraints.RELATIVE;
 		conPlayer.anchor = GridBagConstraints.CENTER;
 	}
 
 // OVERVIEW
 	
 	// state overview
 	// TODO will be a overview of the lifestate, waking/sleeping and all relevant informations
 	public void player(){
 		panelPlayers = new JPanel(new GridBagLayout());
 		add(panelPlayers);
 		
 		con.insets = new Insets(0,10,0,10);
 		
 		panelXplayer = new ArrayList<JPanel>();
 		labelXplayer = new ArrayList<JLabel>();
 		
 		redrawPlayer(playerlist);
 	}
 	
 	// redraw state overview
 	public void redrawPlayer(SortedMap<String, Player> playerlist){
 		for (JPanel curPanel : panelXplayer){
 			curPanel.setVisible(false);
 			curPanel = null;
 		}
 		for (JLabel curLabel : labelXplayer){
 			curLabel.setVisible(false);
 			curLabel = null;
 		}
 		
 		panelXplayer.clear();
 		labelXplayer.clear();
 		
 		Set<String> playerset = playerlist.keySet();
 		int size = playerlist.size();
 		
 		for (int i=1; i<=size; i++){
 			for (String playerStr : playerset){
 				int num = playerlist.get(playerStr).number;
 				
 				if (i == num){
 					panelXplayer.add(new JPanel(new GridBagLayout()));
 					JPanel curPanel = panelXplayer.get(num-1);
 					
 					con.gridy = 0;
 					con.gridx = GridBagConstraints.RELATIVE;
 					panelPlayers.add(curPanel, con);
 					
 					labelXplayer.add(new JLabel(playerStr));
 					curPanel.add(labelXplayer.get(num-1), conPlayer);
 					break;
 				}
 			}
 		}
 	}
 
 // GAME LOGIC
 	
 	// start game
 	public void start(){
 		ArrayList<String> command = new ArrayList<String>();
 		ArrayList<String> dealout = new ArrayList<String>();
 		
 		command.add(Messages.getString("board.n.dealout"));
 		dealout.add(Messages.getString("board.n.villager")+" "+Integer.toString(Keys.villager));
 		dealout.add(Messages.getString("board.n.mafia")+" "+Integer.toString(Keys.mafia));
 		dealout.add(Messages.getString("board.n.detective")+" "+Integer.toString(Keys.detective));
 		dealout.add(Messages.getString("board.n.doctor")+" "+Integer.toString(Keys.doctor));
 		
 		DialogCommand dialog = new DialogCommand(
 				frame,
 				Messages.getString("gui.beforegame"),
 				command,
 				dealout);
 		
 		night();
 	}
 	
 	// night actions
 	private void night(){
 		bufferHead = Messages.getString("board.n.night")+" "+round;
 		bufferCommand.add(Messages.getString("board.c.allsleep"));
 		
 		// TASKLIST
 		doctor();
 		mafia();
 		detective();
 		
 		// first night
 		if (round == 1){
 			Set<String> playerset = playerlist.keySet();
 			for (String playerStr : playerset){
 				if (playerlist.get(playerStr).character == 0){
 					playerlist.get(playerStr).character = 1;
 				}
 			}
 
 		}
 		
 		// after night
 		playerlist.get(deprotectPlayer).isprotected = false;
 		
 		day();
 	}
 	
 	// day actions
 	private void day(){
 		bufferHead = Messages.getString("board.n.day")+" "+round;
 		
 		if (died.size() == 0){
 			bufferCommand.add(Messages.getString("board.c.nodied"));
 		}
 		else {
 			bufferCommand.add(Messages.getString("board.c.isdied"));
 			for (String player : died){
 				playerlist.get(player).kill();
 				bufferNote.add(player);
 			}
 		}
 		died.clear();
 		
 		DialogCommand day = new DialogCommand(
 				frame,
 				bufferHead,
 				bufferCommand,
 				bufferNote);
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		checkwin();
 		
 		DialogSet lynch = new DialogSet(
 				playerlist,
 				frame,
 				1,
 				bufferHead,
 				bufferCommand,
 				Messages.getString("gui.lynch"),
 				"nodead");
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		Player player = playerlist.get(lynch.getPlayer().get(0));
 		player.kill();
 		
 		// output
 		bufferNote.add("'"+player.name+"' "+Messages.getString("board.n.lynched"));
 		
 		DialogCommand lynced = new DialogCommand(
 				frame,
 				bufferHead,
 				bufferCommand,
 				bufferNote);
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		checkwin();
 		
 		round++;
 
 		
 		night();
 	}
 	
 	// check if one party have won and exit game
 	private void checkwin(){
 		if (Keys.mafia == 0){
 			bufferCommand.add(Messages.getString("gui.villagerswin"));
 			bufferNote.add(Messages.getString("gui.congratulation"));
 			
 			DialogCommand win = new DialogCommand(
 					frame,
 					Messages.getString("gui.endgame"),
 					bufferCommand,
 					bufferNote);
 			
 			System.exit(0);
 		}
 		else if (
 				Keys.detective == 0 &&
 				Keys.doctor == 0 &&
 				Keys.villager == 0){
 			
 			bufferCommand.add(Messages.getString("gui.mafiawin"));
 			bufferNote.add(Messages.getString("gui.congratulation"));
 			
 			DialogCommand win = new DialogCommand(
 					frame,
 					Messages.getString("gui.endgame"),
 					bufferCommand,
 					bufferNote);
 			
 			System.exit(0);
 		}
 	}
 	
 // CHARACTERS
 	
 	// doctor / Seelenretter
 	private void doctor(){ if (Keys.doctor > 0){
 		bufferCommand.add(Messages.getString("board.c.doctorsawake"));
 		
 		// first night
 		if (round == 1){
 			DialogSet getdoctors = new DialogSet(
 					playerlist,
 					frame,
 					Keys.doctor,
 					bufferHead,
 					bufferCommand,
 					Messages.getString("gui.whosdoctor"),
 					"onlyunknown");
 			bufferCommand.clear();
 			bufferNote.clear();
 			
 			ArrayList<String> doctors = getdoctors.getPlayer();
 			for (String doctor : doctors){
 				playerlist.get(doctor).character = 4;
 			}
 		}
 		
 		// every night
 		// get player
 		DialogSet actdoctor = new DialogSet(
 				playerlist,
 				frame,
 				1,
 				bufferHead,
 				bufferCommand,
 				Messages.getString("gui.actdoctor"),
 				"nodead");
 		Player player = playerlist.get(actdoctor.getPlayer().get(0));
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		// action
 		player.isprotected = true;
 		deprotectPlayer = player.name;
 		
 		// output
 		bufferCommand.add(Messages.getString("board.c.doctorssleep"));
 	}
 	}
 	
 	// mafia / Mafia
 	private void mafia(){ if (Keys.mafia > 0){
 		bufferCommand.add(Messages.getString("board.c.mafiaawake"));
 		
 		// first night
 		if (round == 1){
 			DialogSet dialog = new DialogSet(
 					playerlist,
 					frame,
 					Keys.mafia,
 					bufferHead,
 					bufferCommand,
 					Messages.getString("gui.whosmafia"),
 					"onlyunknown");
 			bufferCommand.clear();
 			bufferNote.clear();
 			
 			ArrayList<String> mafias = dialog.getPlayer();
 			for (String mafia : mafias){
 				playerlist.get(mafia).character = 2;
 			}
 		}
 		
 		// every night
 		// get player
 		DialogSet actmafia = new DialogSet(
 				playerlist,
 				frame,
 				1,
 				bufferHead,
 				bufferCommand,
 				Messages.getString("gui.actmafia"),
 				"nodead");
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		Player player = playerlist.get(actmafia.getPlayer().get(0));
 		
 		// action
 		if (!player.isprotected){
 			died.add(player.name);
 		}
 		
 		// output
 		bufferCommand.add(Messages.getString("board.c.mafiasleep"));
 	}
 	}
 	
 	// detective / Detektiv
 	private void detective(){ if (Keys.detective > 0){
 		bufferCommand.add(Messages.getString("board.c.detectivesawake"));
 		
 		// first night
 		if (round == 1){
 			DialogSet dialog = new DialogSet(
 					playerlist,
 					frame,
 					Keys.detective,
 					bufferHead,
 					bufferCommand,
 					Messages.getString("gui.whosdetective"),
 					"onlyunknown");
 			bufferCommand.clear();
 			bufferNote.clear();
 			
 			ArrayList<String> detectives = dialog.getPlayer();
 			for (String detective : detectives){
				playerlist.get(detective).character = 2;
 			}
 		}
 		
 		// every night
 		// get player
 		DialogSet actdetective = new DialogSet(
 				playerlist,
 				frame,
 				1,
 				bufferHead,
 				bufferCommand,
 				Messages.getString("gui.actdetective"),
 				"nodead");
 		bufferCommand.clear();
 		bufferNote.clear();
 		
 		Player player = playerlist.get(actdetective.getPlayer().get(0));
 		
 		if (player.character == 2){
 			bufferCommand.add(Messages.getString("board.c.ismafia"));
 		} else {
 			bufferCommand.add(Messages.getString("board.c.isnomafia"));
 		}
 		
 		bufferCommand.add(Messages.getString("board.c.detectivessleep"));
 		
 		DialogCommand command = new DialogCommand(
 				frame,
 				bufferHead,
 				bufferCommand,
 				bufferNote);
 		bufferCommand.clear();
 		bufferNote.clear();
 	}	
 	}
 
 }
