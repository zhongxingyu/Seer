 package cricketsim.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import cricketsim.model.CollectionOfCricketers;
 import cricketsim.model.Cricketer;
 import cricketsim.model.CricketerNotFoundException;
 import cricketsim.model.ImportCricketer;
 
 /**
  * The frame where the user can manage the team of cricketers
  * @author Ryan Gouldsmith (ryangouldsmith@gmail.com)
  * @author Josh Tumath (josh@joshtumath.me.uk)
  */
 public class MainFrame extends JFrame {
 	private static final long serialVersionUID = -4850706096538947479L;
 	
 	private DefaultListModel<CollectionOfCricketers> cricketerList = new DefaultListModel<CollectionOfCricketers>();
 	private CollectionOfCricketers cricketerCollection = new CollectionOfCricketers();
 	/**
 	 * Construct a new window to manage a team of cricketers
 	 */
 	public MainFrame() {
 		setTitle("Cricket Team Simulator");
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setMinimumSize(new Dimension(400, 450));
 		
 		JPanel teamManager = new JPanel(new BorderLayout());
 		teamManager.setBorder(new EmptyBorder(10, 10, 10, 10));
 		
 		// TODO: Support scrolling
 		
 		JList<CollectionOfCricketers> teamList = new JList<>(cricketerList);
 		
 		JPanel teamManagerControls = new JPanel();
 		teamManagerControls.setLayout(new BoxLayout(teamManagerControls, BoxLayout.Y_AXIS));
 		teamManagerControls.setBorder(new EmptyBorder(0, 10, 0, 0));
 		
 		JButton btn;
 		
 		btn = new JButton("Add...");
 		btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				addCricketer();
 			}
 		});
 		teamManagerControls.add(btn);
 		
 		teamManagerControls.add(Box.createVerticalStrut(5));
 		
 		btn = new JButton("Edit...");
 		btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				editCricketer(null);
 			}
 		});
 		teamManagerControls.add(btn);
 		
 		teamManagerControls.add(Box.createVerticalStrut(5));
 		
 		btn = new JButton("Delete");
 		btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					deleteCricketer(null);
 				} catch (CricketerNotFoundException ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		teamManagerControls.add(btn);
 		
 		teamManagerControls.add(Box.createVerticalStrut(5));
 		
 		btn = new JButton("Import");
 		btn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ImportCricketer c = new ImportCricketer(cricketerCollection);
 				
 				try {
 					c.readCricketers();
 				} catch (IOException ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		teamManagerControls.add(btn);
 		
 		teamManager.add(teamList, BorderLayout.CENTER);
 		teamManager.add(teamManagerControls, BorderLayout.EAST);
 		
 		JPanel playerSelector = new JPanel();
 		playerSelector.setBorder(new TitledBorder(
 				new LineBorder(Color.black),
 				"Select cricketers for a team"));
 		
 		JLabel lblExplanation = new JLabel("Use the stats of your team members to create the perfect team.");
 
 		JLabel lblGender = new JLabel("Gender:");
 		JRadioButton rdbtnMale = new JRadioButton("Male");
 		JRadioButton rdbtnFemale = new JRadioButton("Female");
 		ButtonGroup genderGroup = new ButtonGroup();
 		genderGroup.add(rdbtnMale);
 		genderGroup.add(rdbtnFemale);
 
 		JLabel lblTypeOfTeam = new JLabel("Type of team:");
 		JButton btnAttack = new JButton("Attack...");
		btnAttack.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
					openTeamFrame(TeamType.ATTACK);		
 			}
 		});
 		JButton btnDefence = new JButton("Defence...");
		btnDefence.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				openTeamFrame(TeamType.DEFENSE);
			}
			
		});
 		JButton btnNeutral = new JButton("Neutral...");
		btnNeutral.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				openTeamFrame(TeamType.NEUTRAL);
			}
		});
 
 		GroupLayout groupLayout = new GroupLayout(playerSelector);
 		groupLayout.setHorizontalGroup(
 				groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap()
 						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 								.addComponent(lblExplanation)
 								.addGroup(groupLayout.createSequentialGroup()
 										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 												.addComponent(lblGender)
 												.addComponent(lblTypeOfTeam))
 												.addGap(32)
 												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
 														.addGroup(groupLayout.createSequentialGroup()
 																.addComponent(btnAttack)
 																.addPreferredGap(ComponentPlacement.RELATED)
 																.addComponent(btnDefence)
 																.addPreferredGap(ComponentPlacement.RELATED)
 																.addComponent(btnNeutral))
 																.addGroup(groupLayout.createSequentialGroup()
 																		.addComponent(rdbtnMale)
 																		.addPreferredGap(ComponentPlacement.UNRELATED)
 																		.addComponent(rdbtnFemale)))))
 																		.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 				);
 		groupLayout.setVerticalGroup(
 				groupLayout.createParallelGroup(Alignment.LEADING)
 				.addGroup(groupLayout.createSequentialGroup()
 						.addContainerGap()
 						.addComponent(lblExplanation)
 						.addPreferredGap(ComponentPlacement.UNRELATED)
 						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 								.addComponent(lblGender)
 								.addComponent(rdbtnMale)
 								.addComponent(rdbtnFemale))
 								.addPreferredGap(ComponentPlacement.UNRELATED)
 								.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
 										.addComponent(lblTypeOfTeam)
 										.addComponent(btnAttack)
 										.addComponent(btnDefence)
 										.addComponent(btnNeutral)))
 				);
 		playerSelector.setLayout(groupLayout);
 		
 		getContentPane().add(teamManager, BorderLayout.CENTER);
 		getContentPane().add(playerSelector, BorderLayout.SOUTH);
 		
 		pack();
 		setLocationRelativeTo(null); // Centre window in middle of screen
 	}
 	
 	/**
 	 * Open the editor window to add a new cricketer to the list
 	 */
 	private void addCricketer() {
 		final JFrame owner = this;
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				EditorFrame window = new EditorFrame(owner, cricketerCollection);
 				window.setVisible(true);
 			}
 		});
 		
 		// TODO: Find a way to detect when the dialog window closes so that the
 		//       new cricketer can be added to the cricketerList.
 	}
 	
 	/**
 	 * Open the editor window to edit the currently selected cricketer in the
 	 * list
 	 * @param cricketer the selected cricketer
 	 */
 	private void editCricketer(final Cricketer cricketer) {
 		final JFrame owner = this;
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				EditorFrame window = new EditorFrame(owner, cricketerCollection);
 				window.setVisible(true);
 			}
 		});
 		
 		// TODO: Get the selected cricketer in the cricketerList and open it
 	}
 	
 	/**
 	 * Delete the currently selected cricketer in the list
 	 * @param cricketer
 	 * @throws CricketerNotFoundException 
 	 * @param cricketer the selected cricketer
 	 */
 	private void deleteCricketer(final Cricketer cricketer) throws CricketerNotFoundException {
 		cricketerCollection.removeCricketer(cricketer);
 	}
	/**
	 * Opens the Team Window, displaying the best team based on Attacking, Neutral or Defensive choice
	 * @param type is the type of team style.
	 */
 	private void openTeamFrame(final TeamType type) {
 		List<Cricketer> team;
 		
 		switch(type) {
 		case ATTACK:
 			team = cricketerCollection.attackingTeam();
 			break;
 		case DEFENSE:
 			team = cricketerCollection.defensiveTeam();
 			break;
 		case NEUTRAL:
 			team = cricketerCollection.neutralTeam();
 		default:
 			team = null;
 			break;
 		}
 		
 		final List<Cricketer> finalTeam = team;
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				TeamFrame window = new TeamFrame(type, finalTeam);
 				window.setVisible(true);
 			}
 		});
 	}
 }
