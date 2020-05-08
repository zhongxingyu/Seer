 package dbs.project.main.gui;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.plaf.DimensionUIResource;
 import javax.swing.tree.TreeModel;
 
 import dbs.project.dev.Generator;
 import dbs.project.entity.Stadium;
 import dbs.project.entity.Tournament;
 import dbs.project.entity.TournamentGroup;
 import dbs.project.service.KnockoutStageService;
 import dbs.project.service.TournamentService;
 import dbs.project.service.group.StandingRow;
 
 public class AppGui extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private final String APP_NAME = "Weltmeisterschaft DB";
 	
     private JList tournamentsList;
     private JPanel mainPanel, groupStageComponents, statistic;
     private JTree knockoutTree;
 
     public AppGui() {
     	this.setName(APP_NAME);
     	this.setTitle(APP_NAME);
     	this.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE ); 
     	this.setSize(1024, 768);
     	
     	initComponents();
     }
     
     public static void main(String[] args) {
     	AppGui gui = new AppGui();
     	gui.setVisible(true);
     }
 
     private void initComponents() {
 
     	mainPanel = new javax.swing.JPanel();
         mainPanel.setName("mainPanel");
         mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));
         mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
         initLeftComponents();
         initTab();
         
         this.add(mainPanel);
     }
 
     private void initTab() {
     	JTabbedPane tabbComponents = new JTabbedPane();
     	
     	//Vorrunde
     	groupStageComponents = new JPanel();
 		groupStageComponents.setLayout(new BoxLayout(groupStageComponents, BoxLayout.Y_AXIS));
         tabbComponents.add("Vorrunde", new JScrollPane(groupStageComponents));
 
         //Finalrunde
         knockoutTree = new JTree();
         tabbComponents.add("Finalrunde", knockoutTree);
         knockoutTree.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		
         //Statistik
         statistic = new JPanel();
         statistic.setLayout(new BoxLayout(statistic, BoxLayout.Y_AXIS));
         statistic.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         statistic.setAlignmentX(0F);
         JScrollPane bla = new JScrollPane(statistic);
         bla.setAlignmentX(0F);
         tabbComponents.add("Statisik", bla);
         
         
         if(tournamentsList.getModel().getSize() > 0) {
         	Tournament firstTournament = (Tournament) tournamentsList.getModel().getElementAt(0);
         	refreshTabs(firstTournament);
         }
         
         mainPanel.add(tabbComponents);
 	}
 
 	private void refreshTabs(Tournament tournament) {
 		List<TournamentGroup> groups = tournament.getGroupPhase().getGroups();
 		
 		//Updates tables
 		refreshTables(groups);
         
         //Updates tree
         refreshTree(tournament);
         
         //Updates statistic
         refreshStatistic(tournament);
         
         mainPanel.validate();
         mainPanel.repaint();
 	}
 
 	private void refreshStatistic(Tournament tournament) {
 		statistic.removeAll();
 		
 		addLine(statistic, "Torschützenkönig", TournamentService.getTopscorers(tournament));
 		
 		addLine(statistic, "Spieler mit den meisten Karten", TournamentService.getPlayerWithMostCards(tournament));
 
		JLabel stadiumLabel = new JLabel("Stadien");
 		stadiumLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
 		stadiumLabel.setAlignmentY(0F);
 		
 		JPanel stadiumList = new JPanel();
 		stadiumList.setLayout(new BoxLayout(stadiumList, BoxLayout.Y_AXIS));
 		stadiumList.setAlignmentY(0F);
 		for(Stadium stadium : tournament.getStadiums()) {
 			JTextField tmpText = new JTextField(stadium.getCity());
 			tmpText.setMaximumSize(new DimensionUIResource(200, 20));
 			tmpText.setEditable(false);
 			
 			stadiumList.add(tmpText);
 		}
 		
 		JPanel stadiums = new JPanel();
 		stadiums.setLayout(new BoxLayout(stadiums, BoxLayout.X_AXIS));
 		stadiums.add(stadiumLabel);
 		stadiums.add(stadiumList);
 		stadiums.setAlignmentX(0F);
 		statistic.add(stadiums);
 		
 		
 	}
 
 	private void addLine(JPanel component, String label, String text) {
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		panel.setAlignmentY(0F);
 		panel.setAlignmentX(0F);
 		
 		JLabel tmpLabel = new JLabel(label);
 		tmpLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		tmpLabel.setAlignmentX(0F);
 		
 		JTextField tmpText = new JTextField(text);
 		tmpText.setMaximumSize(new DimensionUIResource(400, 20));
 		tmpText.setEditable(false);
 		tmpText.setAlignmentX(1F);
 		
 		panel.add(tmpLabel);
 		panel.add(tmpText);
 		component.add(panel);
 	}
 
 	private void refreshTables(List<TournamentGroup> groups) {
 		groupStageComponents.removeAll();
         for(TournamentGroup group : groups) {
         	JLabel groupLabel = new JLabel(group.getName());
         	JTable tmpJTable = new JTable();
             
         	tmpJTable.setModel(StandingRow.getModel(group));
         	JScrollPane tmpScrollPane = new JScrollPane(tmpJTable);
         	tmpScrollPane.setMaximumSize(new Dimension(900, 100));
         	tmpScrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         	
             groupStageComponents.add(groupLabel);
             groupStageComponents.add(tmpScrollPane);
         }
 	}
 
 	private void refreshTree(Tournament tournament) {
 		TreeModel treeModel = KnockoutStageService.getAsTreeModel(tournament.getKnockoutPhase());
         knockoutTree.setModel(treeModel);
 	}
 
 	private void initLeftComponents() {
 		JPanel components = new JPanel();
 		BoxLayout bl = new BoxLayout(components, BoxLayout.Y_AXIS);
 		components.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		components.setLayout(bl);
 		components.setMaximumSize(new Dimension(200, 768));
 		
 		JLabel label = new JLabel("Verfügbare Turniere:");
 		label.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
 		components.add(label);
 		
 		JScrollPane tournamentsScrollPane = new JScrollPane();
         tournamentsList = new JList();
         tournamentsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         tournamentsScrollPane.setViewportView(tournamentsList);
         refreshList();
         
         ListSelectionListener listListener = new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				int i = ((JList) e.getSource()).getSelectedIndex();
 				Tournament selectedTournament = (Tournament) tournamentsList.getModel().getElementAt(i);
 				refreshTabs(selectedTournament);
 			}
 		};
 		tournamentsList.addListSelectionListener(listListener);
 
         JButton refreshTournament = new JButton();
         refreshTournament.setText("Turnier generieren");
         refreshTournament.setName("tournamentCreateButton");
         ActionListener buttonPressed = new ActionListener() {
 			public void actionPerformed(ActionEvent ev) {
 				try {
 					Generator.generateTournament();
 				} catch(Exception e) {}
 				
 				refreshList();
 			}
 		};
 		refreshTournament.addActionListener(buttonPressed);
         
         //Liste hinzufügen
         components.add(tournamentsScrollPane);
         //Button hinzufügen
         components.add(refreshTournament);
 
         mainPanel.add(components);
 	}
 	
 	private void refreshList() {
 		tournamentsList.setModel(TournamentService.getListModel());
     }
 }
