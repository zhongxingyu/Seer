 /*
  cursus - Race series management program
  Copyright 2011  Simon Arlott
 
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
 package eu.lp0.cursus.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Frame;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 
 import eu.lp0.cursus.app.Main;
 import eu.lp0.cursus.db.Database;
 import eu.lp0.cursus.db.data.Event;
 import eu.lp0.cursus.db.data.Race;
 import eu.lp0.cursus.db.data.RaceEntity;
 import eu.lp0.cursus.db.data.Series;
 import eu.lp0.cursus.ui.component.AbstractDatabaseTab;
 import eu.lp0.cursus.ui.component.DatabaseTabSync;
 import eu.lp0.cursus.ui.component.DatabaseWindow;
 import eu.lp0.cursus.ui.component.Displayable;
 import eu.lp0.cursus.ui.event.EventResultsTab;
 import eu.lp0.cursus.ui.menu.MainMenu;
 import eu.lp0.cursus.ui.preferences.JFrameAutoPrefs;
 import eu.lp0.cursus.ui.race.RaceAttendeesTab;
 import eu.lp0.cursus.ui.race.RaceLapsTab;
 import eu.lp0.cursus.ui.race.RacePenaltiesTab;
 import eu.lp0.cursus.ui.race.RaceResultsTab;
 import eu.lp0.cursus.ui.series.SeriesClassesTab;
 import eu.lp0.cursus.ui.series.SeriesPilotsTab;
 import eu.lp0.cursus.ui.series.SeriesResultsTab;
 import eu.lp0.cursus.ui.tree.RaceTree;
 import eu.lp0.cursus.util.Background;
 import eu.lp0.cursus.util.Constants;
 
 public final class MainWindow extends JFrame implements Displayable, DatabaseWindow {
 	private final Main main;
 
 	private JFrameAutoPrefs prefs = new JFrameAutoPrefs(this);
 	private DatabaseManager dbMgr = new DatabaseManager(this);
 	@SuppressWarnings("unused")
 	private RuntimeManager runMgr;
 	private TabbedPaneManager tabMgr;
 	private SelectedTabManager selMgr;
 
 	private MainMenu menuBar;
 	private JSplitPane splitPane;
 	private JScrollPane scrollPane;
 	private RaceTree raceList;
 	private JTabbedPane tabbedPane;
 	private AbstractDatabaseTab<Series> serPilotsTab;
 	private AbstractDatabaseTab<Series> serClassesTab;
 	private AbstractDatabaseTab<Series> serResultsTab;
 	private List<AbstractDatabaseTab<Series>> seriesTabs;
 	private AbstractDatabaseTab<Event> evtResultsTab;
 	private List<AbstractDatabaseTab<Event>> eventTabs;
 	private AbstractDatabaseTab<Race> racAttendeesTab;
 	private AbstractDatabaseTab<Race> racLapsTab;
 	private AbstractDatabaseTab<Race> racPenaltiesTab;
 	private AbstractDatabaseTab<Race> racResultsTab;
 	private List<AbstractDatabaseTab<Race>> raceTabs;
 
 	public MainWindow(Main main) {
 		super();
 		this.main = main;
 
 		initialise();
 		bind();
 		databaseClosed();
 	}
 
 	public void display() {
 		assert (SwingUtilities.isEventDispatchThread());
 
 		prefs.display();
 	}
 
 	public Main getMain() {
 		return main;
 	}
 
 	public Frame getFrame() {
 		return this;
 	}
 
 	public MainMenu getMenu() {
 		return menuBar;
 	}
 
 	public boolean isOpen() {
 		return main.isOpen();
 	}
 
 	public Database getDatabase() {
 		return main.getDatabase();
 	}
 
 	public RaceEntity getSelected() {
 		return isOpen() ? raceList.getSelected() : null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void initialise() {
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		setTitle(Constants.APP_NAME);
 		setSize(800, 600);
 
 		{ // Menu
 			menuBar = new MainMenu(this, dbMgr);
 			setJMenuBar(menuBar);
 		}
 
 		{ // Main
 			splitPane = new JSplitPane();
 			splitPane.setBorder(new EmptyBorder(2, 0, 0, 0));
 			getContentPane().add(splitPane, BorderLayout.CENTER);
 
 			scrollPane = new JScrollPane();
 			scrollPane.setPreferredSize(new Dimension(150, 0));
 			splitPane.setLeftComponent(scrollPane);
 
 			raceList = new RaceTree(this);
 			raceList.setBorder(new EmptyBorder(2, 2, 2, 2));
 			scrollPane.setViewportView(raceList);
 
 			tabbedPane = new JTabbedPane();
 			tabbedPane.setBorder(new EmptyBorder(0, 2, 0, 0));
 			splitPane.setRightComponent(tabbedPane);
 		}
 
 		{ // Series
 			serClassesTab = new SeriesClassesTab(this);
 			serPilotsTab = new SeriesPilotsTab(this);
 			serResultsTab = new SeriesResultsTab(this);
 			seriesTabs = Arrays.asList(serPilotsTab, serClassesTab, serResultsTab);
 		}
 
 		{ // Event
 			evtResultsTab = new EventResultsTab(this);
 
 			eventTabs = Arrays.asList(evtResultsTab);
 		}
 
 		{ // Race
 			racAttendeesTab = new RaceAttendeesTab(this);
 			racLapsTab = new RaceLapsTab(this);
 			racPenaltiesTab = new RacePenaltiesTab(this);
 			racResultsTab = new RaceResultsTab(this);
 
 			raceTabs = Arrays.asList(racAttendeesTab, racLapsTab, racPenaltiesTab, racResultsTab);
 		}
 	}
 
 	private void bind() {
 		runMgr = new RuntimeManager(this, dbMgr, main.getArgs());
 		tabMgr = new TabbedPaneManager(raceList, tabbedPane, seriesTabs, eventTabs, raceTabs);
 		selMgr = new SelectedTabManager(this, tabbedPane);
 	}
 
 	private void sync(final boolean open, final String title) {
 		assert (Background.isExecutorThread());
 
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				splitPane.setVisible(open);
 				menuBar.sync(open);
 				getRootPane().validate();
 
 				if (!open) {
 					tabMgr.showSelected(null);
 				}
 
 				setTitle(title);
 			}
 		});
 
 		if (open) {
 			raceList.databaseRefresh();
 			selMgr.databaseRefresh();
 		} else {
 			raceList.databaseClosed();
 			selMgr.databaseClosed();
 		}
 	}
 
 	public void databaseOpened() {
 		sync(true, Constants.APP_NAME + Constants.EN_DASH + main.getDatabase().getName());
 	}
 
 	public void databaseClosed() {
 		sync(false, Constants.APP_DESC);
 	}
 
 	public void refreshRaceList() {
 		Background.execute(new Runnable() {
 			@Override
 			public void run() {
 				raceList.databaseRefresh();
 			}
 		});
 	}
 
 	public void refreshTab(final DatabaseTabSync<?> tab) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				selMgr.tryRefreshTabLater(tab);
 			}
 		});
 	}
 }
