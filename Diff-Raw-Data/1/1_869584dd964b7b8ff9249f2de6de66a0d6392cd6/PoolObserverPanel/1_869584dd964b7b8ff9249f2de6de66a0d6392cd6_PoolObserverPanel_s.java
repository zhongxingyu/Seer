 package final_project.view;
 
 import javax.swing.*;
 
 import final_project.control.*;
 import final_project.model.*;
 import final_project.model.store.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import net.java.balloontip.BalloonTip;
 import net.java.balloontip.BalloonTip.*;
 import javax.swing.border.EmptyBorder;
 
 import java.util.*;
 
 public class PoolObserverPanel extends JPanel implements PoolObserver, ActionListener {
 	/**
 	 * Create the panel.
 	 */
 	TournamentController tournament;
 	JLabel statusLabel;
 	JPanel upcomingBoutsPane;
 	JPanel completedBoutsPane;
 	java.util.List<ScoreView> incompleteResults, completeResults;
 	Pool pool;
 	JLabel currentBout;
 	private JButton btnMessageReferee;
 	private MessageRefPanel messageRefPanel;
 	private BalloonTip messageRefTip;
 
 	public PoolObserverPanel(TournamentController tournament, int poolNumber) {
 		this.tournament = tournament;
 		System.out.println(this.tournament.getPools(0));
 		System.out.println(poolNumber);
 		this.pool = this.tournament.getPools(0).get(poolNumber-1);
 		this.pool.addObserver(this);
 		incompleteResults = new LinkedList<ScoreView>();
 		completeResults = new LinkedList<ScoreView>();
 		setBackground(Color.BLACK);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{151, 80, 80, 150, 0};
 		gridBagLayout.rowHeights = new int[]{0, 0, 0, 0, 24, 0, 0, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		setLayout(gridBagLayout);
 
 		String refString = "";
 		Iterator<Integer> iter = this.pool.getRefs().iterator();
 		while(iter.hasNext()) {
 			refString += tournament.getNameFromId(iter.next());
 			if(iter.hasNext())
 				refString += ", ";
 		}
 		JLabel lblRefereeJoeSmith = new JLabel(refString);
 		lblRefereeJoeSmith.setForeground(Color.WHITE);
 		lblRefereeJoeSmith.setFont(new Font("Score Board", Font.PLAIN, 17));
 		GridBagConstraints gbc_lblRefereeJoeSmith = new GridBagConstraints();
 		gbc_lblRefereeJoeSmith.insets = new Insets(0, 0, 5, 5);
 		gbc_lblRefereeJoeSmith.gridx = 0;
 		gbc_lblRefereeJoeSmith.gridy = 0;
 		add(lblRefereeJoeSmith, gbc_lblRefereeJoeSmith);
 
 		JLabel lblPool = new JLabel("pool " + poolNumber);
 		lblPool.setFont(new Font("Score Board", Font.PLAIN, 24));
 		lblPool.setForeground(Color.WHITE);
 		GridBagConstraints gbc_lblPool = new GridBagConstraints();
 		gbc_lblPool.gridwidth = 2;
 		gbc_lblPool.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPool.gridx = 1;
 		gbc_lblPool.gridy = 0;
 		add(lblPool, gbc_lblPool);
 
 		String stripString = "";
 		iter = this.pool.getStrips().iterator();
 		while(iter.hasNext()) {
 			stripString += iter.next();
 			if(iter.hasNext())
 				stripString += ", ";
 		}
 
 		JLabel lblStrip = new JLabel("Strip: " + stripString);
 		lblStrip.setFont(new Font("Score Board", Font.PLAIN, 17));
 		lblStrip.setForeground(Color.WHITE);
 		GridBagConstraints gbc_lblStrip = new GridBagConstraints();
 		gbc_lblStrip.insets = new Insets(0, 0, 5, 0);
 		gbc_lblStrip.gridx = 3;
 		gbc_lblStrip.gridy = 0;
 		add(lblStrip, gbc_lblStrip);
 
 		JLabel statusLabel = new JLabel("-status: fencing-");
 		statusLabel.setForeground(Color.GREEN);
 		statusLabel.setFont(new Font("Score Board", Font.PLAIN, 16));
 		GridBagConstraints gbc_statusLabel = new GridBagConstraints();
 		gbc_statusLabel.gridwidth = 4;
 		gbc_statusLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_statusLabel.gridx = 0;
 		gbc_statusLabel.gridy = 1;
 		add(statusLabel, gbc_statusLabel);
 		
 				upcomingBoutsPane = new JPanel();
 				GridBagConstraints gbc_upcomingBoutsPane = new GridBagConstraints();
 				gbc_upcomingBoutsPane.anchor = GridBagConstraints.NORTH;
 				gbc_upcomingBoutsPane.gridwidth = 4;
 				gbc_upcomingBoutsPane.insets = new Insets(0, 0, 5, 0);
 				gbc_upcomingBoutsPane.gridx = 0;
 				gbc_upcomingBoutsPane.gridy = 2;
 				add(upcomingBoutsPane, gbc_upcomingBoutsPane);
 				upcomingBoutsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 				upcomingBoutsPane.setBackground(Color.BLACK);
 				upcomingBoutsPane.setLayout(new BoxLayout(upcomingBoutsPane, BoxLayout.Y_AXIS));
 				
 						JLabel lblUpcomingBouts = new JLabel("upcoming bouts");
 						lblUpcomingBouts.setAlignmentX(Component.CENTER_ALIGNMENT);
 						upcomingBoutsPane.add(lblUpcomingBouts);
 						lblUpcomingBouts.setHorizontalAlignment(SwingConstants.CENTER);
 						lblUpcomingBouts.setFont(new Font("Score Board", Font.PLAIN, 16));
 						lblUpcomingBouts.setForeground(Color.WHITE);
 
 		JLabel lblCurrentBout = new JLabel("current bout");
 		lblCurrentBout.setHorizontalAlignment(SwingConstants.CENTER);
 		lblCurrentBout.setForeground(Color.WHITE);
 		lblCurrentBout.setFont(new Font("Score Board", Font.PLAIN, 16));
 		GridBagConstraints gbc_lblCurrentBout = new GridBagConstraints();
 		gbc_lblCurrentBout.gridwidth = 4;
 		gbc_lblCurrentBout.insets = new Insets(0, 0, 5, 0);
 		gbc_lblCurrentBout.gridx = 0;
 		gbc_lblCurrentBout.gridy = 3;
 		add(lblCurrentBout, gbc_lblCurrentBout);
 
 		JLabel lblLebronJamesVs = new JLabel();
 		currentBout = lblLebronJamesVs;
 		lblLebronJamesVs.setForeground(Color.CYAN);
 		lblLebronJamesVs.setFont(new Font("Score Board", Font.PLAIN, 18));
 		GridBagConstraints gbc_lblLebronJamesVs = new GridBagConstraints();
 		gbc_lblLebronJamesVs.anchor = GridBagConstraints.SOUTH;
 		gbc_lblLebronJamesVs.gridwidth = 4;
 		gbc_lblLebronJamesVs.insets = new Insets(0, 0, 5, 0);
 		gbc_lblLebronJamesVs.gridx = 0;
 		gbc_lblLebronJamesVs.gridy = 4;
 		add(lblLebronJamesVs, gbc_lblLebronJamesVs);
 		
 				completedBoutsPane = new JPanel();
 				GridBagConstraints gbc_completedBoutsPane = new GridBagConstraints();
 				gbc_completedBoutsPane.gridwidth = 4;
 				gbc_completedBoutsPane.insets = new Insets(0, 0, 5, 0);
 				gbc_completedBoutsPane.gridx = 0;
 				gbc_completedBoutsPane.gridy = 5;
 				add(completedBoutsPane, gbc_completedBoutsPane);
 				completedBoutsPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 				completedBoutsPane.setBackground(Color.BLACK);
 				completedBoutsPane.setLayout(new BoxLayout(completedBoutsPane, BoxLayout.Y_AXIS));
 				
 						JLabel lblCompletedBouts = new JLabel("completed bouts");
 						lblCompletedBouts.setAlignmentX(Component.CENTER_ALIGNMENT);
 						completedBoutsPane.add(lblCompletedBouts);
 						lblCompletedBouts.setHorizontalAlignment(SwingConstants.CENTER);
 						lblCompletedBouts.setForeground(Color.WHITE);
 						lblCompletedBouts.setFont(new Font("Score Board", Font.PLAIN, 16));
 
 		btnMessageReferee = new JButton("Message Referee");
 		btnMessageReferee.addActionListener(this);
 
 		GridBagConstraints gbc_btnMessageReferee = new GridBagConstraints();
 		gbc_btnMessageReferee.gridx = 3;
 		gbc_btnMessageReferee.gridy = 6;
 		add(btnMessageReferee, gbc_btnMessageReferee);
 
         System.out.println("from poolobserver: "+pool.getIncompleteResults());
         System.out.println("numPlayers: "+pool.numPlayers());
         for (IncompleteResult res : pool.getIncompleteResults())
             addIncompleteResult(res);
         for (CompleteResult res : pool.getResults())
             addCompleteResult(res);
 
         setCurrentBout();
 	}
 
 	public enum Status {
 		FENCING, TECHNICAL, MEDICAL;
 	}
 
 	public void setStatus(Status status) {
 		switch (status) {
 		case FENCING:
 			statusLabel.setText("-Status: Fencing-");
 			statusLabel.setForeground(Color.GREEN);
 			break;
 		case TECHNICAL:
 			statusLabel.setText("-Status: Technical-");
 			statusLabel.setForeground(Color.YELLOW);
 			break;
 		case MEDICAL:
 			statusLabel.setText("-Status: Medical-");
 			statusLabel.setForeground(Color.RED);
 			break;
 		default:
 			statusLabel.setText("-Status: Fencing-");
 			statusLabel.setForeground(Color.GREEN);
 			break;
 		}
 	}
 
 
 	public void actionPerformed(ActionEvent e) {
 		if(e.getSource() == this.btnMessageReferee) {
 			if(messageRefPanel == null) {
 				messageRefPanel = new MessageRefPanel();
 				messageRefPanel.getCancelButton().addActionListener(this);
 				messageRefPanel.getSendButton().addActionListener(this);
 				messageRefTip = new BalloonTip(btnMessageReferee, messageRefPanel, new DefaultBalloonStyle(), false);
 				tournament.getMainWindow().registerBalloon(messageRefTip);
 			}
 			else {
 				messageRefTip.setVisible(true);
 			}
 
 		}
 		else if(e.getSource() == messageRefPanel.getSendButton()) {
 			tournament.getSMSController().sendCollectionMessage(messageRefPanel.getTextField().getText(), pool.getRefs());
 			tournament.getMainWindow().hideAllBalloons();
 		}
 		else if(e.getSource() == messageRefPanel.getCancelButton()) {
 			tournament.getMainWindow().hideAllBalloons();
 		}
 	}
 
 
 	public void addIncompleteResult(IncompleteResult incompleteResult) {
 		ScoreView newBout = new ScoreView(tournament, incompleteResult);
 		incompleteResults.add(newBout);
 		upcomingBoutsPane.add(newBout, 1);
 	}
 
 	public void addCompleteResult(CompleteResult completeResult) {
 		ScoreView newBout = new ScoreView(tournament, completeResult);
 		completeResults.add(newBout);
 		setCurrentBout();
 		completedBoutsPane.add(newBout, 1);
 	}
 	
 	public void changeMatchResult(CompleteResult completeResult) {
         String player1Name = tournament.getNameFromId(completeResult.getPlayer1());
         String player2Name = tournament.getNameFromId(completeResult.getPlayer2());
 		for (ListIterator<ScoreView> itr = completeResults.listIterator(); itr.hasNext();) {
             ScoreView v = itr.next();
             if ((v.player1Name.equals(player1Name) &&
                  v.player2Name.equals(player2Name)) ||
                 (v.player1Name.equals(player2Name) &&
                  v.player2Name.equals(player1Name))) {
                 ScoreView n = new ScoreView(tournament,completeResult);
                 itr.remove();
                 itr.add(n);
                 completedBoutsPane.remove(v);
                 completedBoutsPane.add(n);
                 break;
             }
         }
 	}
 
 	public void setCurrentBout() {
 		IncompleteResult next = pool.getNextResult();
 		if (next==null)
 			currentBout.setText("No Bout");
 		else {
 			String player1Name = tournament.getNameFromId(next.getPlayer1());
 			String player2Name = tournament.getNameFromId(next.getPlayer2());
 			currentBout.setText(player1Name +
 					" vs "+
 					player2Name);
 			for (Iterator<ScoreView> itr = incompleteResults.iterator(); itr.hasNext();) {
 				ScoreView v = itr.next();
 				if ((v.player1Name.equals(player1Name) &&
                      v.player2Name.equals(player2Name)) ||
                     (v.player1Name.equals(player2Name) &&
                      v.player2Name.equals(player1Name))) {
 					upcomingBoutsPane.remove(v);
 					itr.remove();
 					break;
 				}
 			}
 		}
 	}
 
 	public JButton getBtnMessageReferee() {
 		return btnMessageReferee;
 	}
 }
