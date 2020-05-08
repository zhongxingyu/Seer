 package dbs.project.main.gui;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.CellRendererPane;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTree;
 import javax.swing.ListCellRenderer;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.tree.TreeModel;
 
 import dbs.project.dev.Generator;
 import dbs.project.entity.Tournament;
 import dbs.project.entity.TournamentGroup;
 import dbs.project.service.KnockoutStageService;
 import dbs.project.service.TournamentService;
 import dbs.project.service.group.StandingRow;
 
 public class AppGui extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private final String APP_NAME = "Weltmeisterschaft DB";
 	
     private JPanel mainPanel;
     private JList tournamentsList;
     private JPanel groupStageComponents;
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
     	groupStageComponents = new JPanel();
 		groupStageComponents.setLayout(new BoxLayout(groupStageComponents, BoxLayout.Y_AXIS));
         
         tabbComponents.add("Vorrunde", groupStageComponents);
 
         knockoutTree = new JTree();
         tabbComponents.add("Finalrunde", knockoutTree);
         
     	Tournament firstTournament = (Tournament) tournamentsList.getModel().getElementAt(0);
         refreshTabs(firstTournament);
         
         mainPanel.add(tabbComponents);
 	}
 
 	private void refreshTabs(Tournament tournament) {
 		List<TournamentGroup> groups = tournament.getGroupPhase().getGroups();
 		groupStageComponents.removeAll();
 		
         for(TournamentGroup group : groups) {
         	JTable tmpJTable = new JTable();
         	JScrollPane tmpJScrollPane = new JScrollPane();
             
         	tmpJTable.setModel(StandingRow.getModel(group));
         	tmpJScrollPane.setViewportView(tmpJTable);
         	
             groupStageComponents.add(tmpJScrollPane);
         }
         
         TreeModel treeModel = KnockoutStageService.getAsTreeModel(tournament.getKnockoutPhase());
         knockoutTree.setModel(treeModel);
         
         mainPanel.validate();
 	}
 
 	private void initLeftComponents() {
 		JPanel components = new JPanel();
 		BoxLayout bl = new BoxLayout(components, BoxLayout.Y_AXIS);
 		components.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		components.setLayout(bl);
 		components.setMaximumSize(new Dimension(15, Short.MAX_VALUE));
 		
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
         refreshTournament.setText("Turnier generieren"); // NOI18N
         refreshTournament.setName("tournamentCreateButton"); // NOI18N
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
