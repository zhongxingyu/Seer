 package elfville.client.views;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.*;
 
 import elfville.client.SocketController;
 import elfville.protocol.*;
 
 public class Clan extends JPanel {
 	private JButton clanName;
 	private JTextArea clanDescription;
 	private JButton leaderName;
 	// TODO: perhaps add leader name with a link to profile
 	
 	private class ClanHandler implements ActionListener {
 		private String clanID;
 		private Clan component;
 		private String clanName;
 		
 		public ClanHandler(String clanID, String clanName, Clan component) {
 			this.clanID = clanID;
 			this.clanName = clanName;
 			this.component = component;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			ClanBoardRequest req = new ClanBoardRequest(clanID);
 			try {
 				ClanBoardResponse resp = SocketController.send(req);
 				if (resp.status == Response.Status.SUCCESS){
 					ClanBoard b = (ClanBoard) ClientWindow.switchScreen("clan_board");
 					b.changeClanLoadPosts(clanName, resp);
 				} else {
					ClientWindow.showError(resp.message, "Login error");
 				}
 			} catch (IOException e1) {
				ClientWindow.showConnectionError();
 			}
 		}
 		
 	}
 	
 	private class LeaderHandler implements ActionListener {
 		private String elfID;
 		private Clan component;
 		
 		public LeaderHandler(String elfID, Clan component) {
 			this.elfID = elfID;
 			this.component = component;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// TODO load the elf profile (which currently doesn't exist (profiles don't))
 		}
 		
 	}
 
 	/**
 	 * Create the panel.
 	 */
 	public Clan(SerializableClan c) {
 		super();
 		
 		clanName = new JButton(c.clanName);
 		clanDescription = new JTextArea(c.clanDescription);
 		leaderName = new JButton(c.leader.elfName);
 		
 		clanName.addActionListener(new ClanHandler(c.clanID, c.clanName, this));
 		leaderName.addActionListener(new LeaderHandler(c.leader.id, this));
 		
 		add(clanName);
 		add(leaderName); // TODO we need a way to distinguish between clan and leader names
 		add(clanDescription);
 	}
 
 }
