 package ho.module.lineup;
 
 import ho.core.gui.RefreshManager;
 import ho.core.gui.Refreshable;
 import ho.core.gui.Updateable;
 import ho.core.model.HOVerwaltung;
 import ho.core.model.player.SpielerPosition;
 import ho.module.lineup.exchange.UploadDownloadPanel;
 import ho.module.lineup.penalties.PenaltyTaker;
 import ho.module.lineup.penalties.PenaltyTakersView;
 import ho.module.lineup.substitution.SubstitutionOverview;
 
 import java.awt.BorderLayout;
 import java.util.List;
 
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Top-Level Container for the Lineups (contains a tab for the lineup, a tab for
  * the match orders...)
  * 
  * @author kruescho
  * 
  */
 public class LineupMasterView extends JPanel {
 
 	private static final long serialVersionUID = 6557097920433876610L;
 	private JTabbedPane tabbedPane;
 	private LineupPanel lineupPanel;
 	private SubstitutionOverview substitutionOverview;
 	private PenaltyTakersView penaltyTakersView;
 	private int oldTabIndex = -1;
 
 	public LineupMasterView() {
 		initComponents();
 		addListeners();
 	}
 
 	public LineupPanel getLineupPanel() {
 		return this.lineupPanel;
 	}
 
 	private void initComponents() {
 		this.tabbedPane = new JTabbedPane();
 		HOVerwaltung hov = HOVerwaltung.instance();
 
 		this.lineupPanel = new LineupPanel();
 		this.tabbedPane.addTab(hov.getLanguageString("Aufstellung"), this.lineupPanel);
 
 		this.substitutionOverview = new SubstitutionOverview(hov.getModel().getAufstellung());
 		this.tabbedPane.addTab(hov.getLanguageString("subs.Title"), this.substitutionOverview);
 
 		this.penaltyTakersView = new PenaltyTakersView();
 		this.penaltyTakersView.setPlayers(hov.getModel().getAllSpieler());
 		this.penaltyTakersView.setLineup(hov.getModel().getAufstellung());
 		this.tabbedPane.addTab(hov.getLanguageString("lineup.penaltytakers.tab.title"),
 				this.penaltyTakersView);
 		this.tabbedPane.addTab(hov.getLanguageString("lineup.upload.tab.title"),
 				new UploadDownloadPanel());
 
 		setLayout(new BorderLayout());
 		add(this.tabbedPane, BorderLayout.CENTER);
 	}
 
 	private void addListeners() {
 		this.lineupPanel.addUpdateable(new Updateable() {
 
 			@Override
 			public void update() {
 				refreshView();
 			}
 		});
 		
 		this.tabbedPane.addChangeListener(new ChangeListener() {
 			
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				// if penalty takers tab is left, update the lineup
 				if (oldTabIndex == tabbedPane.indexOfComponent(penaltyTakersView) )	{
 					updatePenaltyTakersInLineup();
 				}
 				oldTabIndex = tabbedPane.getSelectedIndex();
 			}
 		});
 		
 		RefreshManager.instance().registerRefreshable(new Refreshable() {
 			
 			@Override
 			public void refresh() {
 				refreshView();
 				
 			}
 			
 			@Override
 			public void reInit() {
 				refreshView();
 			}
 		});
 	}
 	
 	private void updatePenaltyTakersInLineup() {
 		List<PenaltyTaker> list = this.penaltyTakersView.getPenaltyTakers();
 		List<SpielerPosition> takers = HOVerwaltung.instance().getModel().getAufstellung().getPenaltyTakers();
 		for (int i = 0; i < takers.size(); i++) {
 			if (i < list.size()) {
 				takers.get(i).setSpielerId(
 						list.get(i).getPlayer().getSpielerID());
 			} else {
 				takers.get(i).setSpielerId(0);
 			}
 		}
 	}
 	
 	private void refreshView() {
 		this.substitutionOverview.setLineup(HOVerwaltung.instance().getModel().getAufstellung());
		this.penaltyTakersView.setPlayers(HOVerwaltung.instance().getModel().getAllSpieler());
		this.penaltyTakersView.setLineup(HOVerwaltung.instance().getModel().getAufstellung());		
 	}
 }
