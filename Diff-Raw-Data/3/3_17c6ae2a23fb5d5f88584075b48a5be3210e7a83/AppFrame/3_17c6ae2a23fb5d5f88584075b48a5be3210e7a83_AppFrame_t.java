 /*
  * Copyright (c) 2013 mgm technology partners GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.mgmtp.perfload.loadprofiles.ui;
 
 import static com.google.common.collect.Collections2.filter;
 import static com.google.common.collect.Collections2.transform;
 import static com.google.common.collect.Lists.newArrayListWithCapacity;
 import static com.google.common.collect.Lists.newArrayListWithExpectedSize;
 import static com.google.common.collect.Sets.newHashSet;
 import static com.google.common.io.Resources.getResource;
 import static java.lang.Math.max;
 import static org.apache.commons.io.FilenameUtils.getBaseName;
 import static org.apache.commons.io.FilenameUtils.removeExtension;
 import static org.apache.commons.lang3.StringUtils.isBlank;
 
 import java.awt.BasicStroke;
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.inject.Inject;
 import javax.inject.Singleton;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.WindowConstants;
 import javax.swing.border.CompoundBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 import javax.xml.bind.JAXBException;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.text.StrBuilder;
 import org.jfree.chart.ChartFactory;
 import org.jfree.chart.ChartPanel;
 import org.jfree.chart.JFreeChart;
 import org.jfree.chart.annotations.XYPolygonAnnotation;
 import org.jfree.chart.axis.NumberAxis;
 import org.jfree.chart.plot.IntervalMarker;
 import org.jfree.chart.plot.PlotOrientation;
 import org.jfree.chart.plot.XYPlot;
 import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
 import org.jfree.data.UnknownKeyException;
 import org.jfree.data.xy.XYSeries;
 import org.jfree.data.xy.XYSeriesCollection;
 import org.jfree.ui.Layer;
 import org.jfree.ui.RectangleAnchor;
 import org.jfree.ui.RectangleInsets;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ca.odell.glazedlists.EventList;
 import ca.odell.glazedlists.GlazedLists;
 import ca.odell.glazedlists.gui.TableFormat;
 import ca.odell.glazedlists.swing.EventComboBoxModel;
 import ca.odell.glazedlists.swing.EventTableModel;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.mgmtp.perfload.loadprofiles.generation.EventDistributor;
 import com.mgmtp.perfload.loadprofiles.generation.LoadCurveCalculator;
 import com.mgmtp.perfload.loadprofiles.model.BaseLoadProfileEvent;
 import com.mgmtp.perfload.loadprofiles.model.Client;
 import com.mgmtp.perfload.loadprofiles.model.CurveAssignment;
 import com.mgmtp.perfload.loadprofiles.model.LoadCurve;
 import com.mgmtp.perfload.loadprofiles.model.LoadCurveAssignment;
 import com.mgmtp.perfload.loadprofiles.model.LoadEvent;
 import com.mgmtp.perfload.loadprofiles.model.LoadEventComparator;
 import com.mgmtp.perfload.loadprofiles.model.LoadTestConfiguration;
 import com.mgmtp.perfload.loadprofiles.model.MarkerEvent;
 import com.mgmtp.perfload.loadprofiles.model.Operation;
 import com.mgmtp.perfload.loadprofiles.model.Target;
 import com.mgmtp.perfload.loadprofiles.ui.action.FileExitAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.FileNewLoadProfileConfigAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.FileOpenLoadProfileConfigAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.FileSaveLoadProfileConfigAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.FileSaveLoadProfileConfigAsAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.HelpAboutAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsAddMarkerAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsAddOneTimeAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsAddStairsAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsDeleteLoadProfileEntityAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsExportEventListAction;
 import com.mgmtp.perfload.loadprofiles.ui.action.ToolsSettingsAction;
 import com.mgmtp.perfload.loadprofiles.ui.component.JButtonExt;
 import com.mgmtp.perfload.loadprofiles.ui.component.JCheckListTable;
 import com.mgmtp.perfload.loadprofiles.ui.component.JTableExt;
 import com.mgmtp.perfload.loadprofiles.ui.component.LoadProfileEntityPanel;
 import com.mgmtp.perfload.loadprofiles.ui.component.MarkerPanel;
 import com.mgmtp.perfload.loadprofiles.ui.component.OneTimePanel;
 import com.mgmtp.perfload.loadprofiles.ui.component.SaveAccessoryPanel;
 import com.mgmtp.perfload.loadprofiles.ui.component.StairsPanel;
 import com.mgmtp.perfload.loadprofiles.ui.ctrl.ConfigController;
 import com.mgmtp.perfload.loadprofiles.ui.ctrl.LoadProfilesController;
 import com.mgmtp.perfload.loadprofiles.ui.dialog.AboutDialog;
 import com.mgmtp.perfload.loadprofiles.ui.dialog.ModalResult;
 import com.mgmtp.perfload.loadprofiles.ui.dialog.SettingsDialog;
 import com.mgmtp.perfload.loadprofiles.ui.model.EventsTreeModel;
 import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileConfig;
 import com.mgmtp.perfload.loadprofiles.ui.model.LoadProfileEntity;
 import com.mgmtp.perfload.loadprofiles.ui.model.Marker;
 import com.mgmtp.perfload.loadprofiles.ui.model.OneTime;
 import com.mgmtp.perfload.loadprofiles.ui.model.SelectionDecorator;
 import com.mgmtp.perfload.loadprofiles.ui.model.Stairs;
 import com.mgmtp.perfload.loadprofiles.ui.util.EventToLoadEventFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.ExceptionHandler;
 import com.mgmtp.perfload.loadprofiles.ui.util.GraphPointsCalculator;
 import com.mgmtp.perfload.loadprofiles.ui.util.IsLoadEventPredicate;
 import com.mgmtp.perfload.loadprofiles.ui.util.IsMarkerPredicate;
 import com.mgmtp.perfload.loadprofiles.ui.util.IsOneTimePredicate;
 import com.mgmtp.perfload.loadprofiles.ui.util.IsStairsPredicate;
 import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileEntityToMarkerFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileEntityToOneTimeFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileEntityToStairsFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.LoadProfileException;
 import com.mgmtp.perfload.loadprofiles.ui.util.ModelUtils;
 import com.mgmtp.perfload.loadprofiles.ui.util.Point;
 import com.mgmtp.perfload.loadprofiles.ui.util.SelectionDecoratorCheckedPredicate;
 import com.mgmtp.perfload.loadprofiles.ui.util.SelectionDecoratorToClientFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.SelectionDecoratorToTargetFunction;
 import com.mgmtp.perfload.loadprofiles.ui.util.SwingUtils;
 import com.mgmtp.perfload.loadprofiles.util.PlotFileCreator;
 
 /**
  * Main application frame.
  * 
  * @author rnaegele
  */
 @Singleton
 public class AppFrame extends JFrame {
 
 	private static final Logger LOG = LoggerFactory.getLogger(AppFrame.class);
 
 	private static String APP_TITLE = "perfLoad - Load Profile Editor";
 
 	private final EventBus eventBus = new EventBus();
 
 	private JPanel contentPane;
 	private JTree tree;
 	private JTextField txtName;
 	private JLabel lblName;
 	private JLabel lblDescription;
 	private StairsPanel stairsPanel;
 	private OneTimePanel oneTimePanel;
 	private MarkerPanel markerPanel;
 	private CardLayout cardLayout;
 	private JPanel pnlCard;
 	private JButton btnOk;
 	private JButton btnCancel;
 	private ChartPanel chartPanel;
 
 	private JFreeChart chart;
 	private LoadProfileEntityPanel<? extends LoadProfileEntity> activeLoadProfileEntityPanel;
 	private TreePath activeLeafPath;
 
 	private EventsTreeModel treeModel;
 
 	private final Action fileNewLoadProfileConfigAction = new FileNewLoadProfileConfigAction(eventBus);
 	private final Action fileOpenLoadProfileConfigAction = new FileOpenLoadProfileConfigAction(eventBus);
 	private final Action fileSaveLoadProfileConfigAction = new FileSaveLoadProfileConfigAction(eventBus);
 	private final Action fileSaveLoadProfileConfigAsAction = new FileSaveLoadProfileConfigAsAction(eventBus);
 
 	private final Action toolsAddStairsAction = new ToolsAddStairsAction(eventBus);
 	private final Action toolsAddOneTimeAction = new ToolsAddOneTimeAction(eventBus);
 	private final Action toolsAddMarkerAction = new ToolsAddMarkerAction(eventBus);
 	private final Action toolsDeleteLoadProfileEntityAction = new ToolsDeleteLoadProfileEntityAction(eventBus);
 	private final Action toolsExportEventListAction = new ToolsExportEventListAction(eventBus);
 	private final Action toolsSettingsAction = new ToolsSettingsAction(eventBus);
 
 	private final Action helpAboutAction = new HelpAboutAction(eventBus);
 
 	private final DirtyListener dirtyListener = new DirtyListener();
 
 	private final ConfigController configController;
 	private final LoadProfilesController loadProfilesController;
 
 	private JTableExt tblClients;
 	private JTextArea taDescription;
 	private JLabel lblTargets;
 	private JTable tblTargets;
 
 	private EventList<SelectionDecorator> oneTimeDecoratedTargets;
 	private EventList<SelectionDecorator> decoratedClients;
 	private EventList<SelectionDecorator> decoratedTargets;
 
 	private File loadProfileConfigFile;
 	private File loadProfileEventsFile;
 
 	private boolean dirty;
 
 	private LoadProfileConfig activeLoadProfileConfig;
 
 	private final String appVersion;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(final String[] args) {
 		File baseDir = new File(System.getProperty("basedir", "."));
 
 		final Injector inj = Guice.createInjector(new AppModule(baseDir));
 		final ConfigController configCtrl = inj.getInstance(ConfigController.class);
 
 		try {
 			UIManager.setLookAndFeel(configCtrl.getLookAndFeelClassName());
 		} catch (Exception ex) {
 			LOG.error(ex.getMessage(), ex);
 		}
 
 		ExceptionHandler exceptionHandler = inj.getInstance(ExceptionHandler.class);
 		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);
 
 		// This is necessary because an UncaughtExceptionHandler does not work in modal dialogs
 		System.setProperty("sun.awt.exception.handler", ExceptionHandler.class.getName());
 
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					AppFrame frame = inj.getInstance(AppFrame.class);
 
 					Rectangle bounds = configCtrl.getFrameBounds();
 					if (bounds != null) {
 						frame.setBounds(bounds);
 					}
 					ImageIcon icon = new ImageIcon(getResource("com/mgmtp/perfload/loadprofiles/ui/perfLoad_Logo_bild_64x64.png"));
 					frame.setIconImage(icon.getImage());
 					frame.updateLists();
 					frame.updateGraph();
 					frame.addTableListeners();
 					frame.setUpEventBus();
 					frame.setExtendedState(configCtrl.getFrameState());
 					frame.setVisible(true);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private void setUpEventBus() {
 		eventBus.register(this);
 	}
 
 	/**
 	 * Creates the frame.
 	 * 
 	 * @param configController
 	 *            the config controller
 	 * @param loadProfilesController
 	 *            the load profiles controller
 	 */
 	@Inject
 	public AppFrame(final ConfigController configController, final LoadProfilesController loadProfilesController,
 			@AppVersion final String appVersion) {
 		this.loadProfilesController = loadProfilesController;
 		this.configController = configController;
 		this.appVersion = appVersion;
 
 		ThisWindowListener windowAdapter = new ThisWindowListener();
 		addWindowStateListener(windowAdapter);
 		addWindowListener(windowAdapter);
 		addComponentListener(new ThisComponentListener());
 		initComponents();
 		initModels();
 		cardLayout.show(pnlCard, "none");
 		{
 			chartPanel = new ChartPanel((JFreeChart) null);
 			contentPane.add(chartPanel, "cell 0 2 2 1,grow");
 			chartPanel.setBorder(
 					new CompoundBorder(
 							new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Load Profiles", TitledBorder.LEADING,
 									TitledBorder.TOP, null, new Color(0, 0, 0)),
 							new CompoundBorder(new EmptyBorder(4, 4, 4, 4), new EtchedBorder(EtchedBorder.LOWERED, null, null)
 							)
 					)
 					);
 			chartPanel.setName("chartPanel");
 			chartPanel.setPopupMenu(null);
 		}
 
 		String fileName = configController.getAppProperties().getProperty("lpConfig.file");
 		if (fileName != null) {
 			loadProfileConfigFile = new File(fileName);
 		}
 		fileName = configController.getAppProperties().getProperty("lpEvents.file");
 		if (fileName != null) {
 			loadProfileEventsFile = new File(fileName);
 		}
 	}
 
 	@Override
 	protected JRootPane createRootPane() {
 		KeyStroke stroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
 		JRootPane rp = super.createRootPane();
 		rp.registerKeyboardAction(new ActionListener() {
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				// reset and edit again when canceled
 				editOrCancelLoadProfileEntity();
 			}
 		}, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
 		return rp;
 	}
 
 	/**
 	 * Mostly created by Eclipse WindowBuilder
 	 */
 	private void initComponents() {
 		setTitle("perfLoad - Load Profile Configurator");
 		setSize(1032, 984);
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 		SwingUtils.setUIFontStyle(Font.PLAIN);
 		{
 			JMenuBar menuBar = new JMenuBar();
 			menuBar.setName("menuBar");
 			setJMenuBar(menuBar);
 			initMenuBar(menuBar);
 		}
 
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new MigLayout("insets 0", "[grow][]", "[25px][400][grow]"));
 		{
 			JToolBar toolBar = new JToolBar() {
 				@Override
 				protected JButton createActionComponent(final Action a) {
 					JButton button = super.createActionComponent(a);
 					button.setFocusable(false);
 					button.setHideActionText(false);
 					return button;
 				}
 			};
 			toolBar.setName("toolBar");
 			contentPane.add(toolBar, "cell 0 0 2 1,growx,aligny top");
 			initToolBar(toolBar);
 		}
 		{
 			JScrollPane spTree = new JScrollPane();
 			spTree.setBorder(
 					new CompoundBorder(
 							new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Load Profile Elements",
 									TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)),
 							new EmptyBorder(4, 4, 4, 4)
 					)
 					);
 			contentPane.add(spTree, "cell 0 1,grow");
 			spTree.setName("spTree");
 			{
 				tree = new JTree();
 				tree.addKeyListener(new TreeKeyListener());
 				tree.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 				tree.addTreeSelectionListener(new TreeTreeSelectionListener());
 				tree.setShowsRootHandles(true);
 				tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 				tree.setName("tree");
 				spTree.setViewportView(tree);
 			}
 		}
 		{
 			JPanel pnlMain = new JPanel();
 			contentPane.add(pnlMain, "cell 1 1");
 			pnlMain.setName("pnlMain");
 			pnlMain.setLayout(new MigLayout("insets 0", "[664!]", "[grow][]"));
 			{
 				JPanel pnlLoadProfileProperties = new JPanel();
 				pnlLoadProfileProperties.setBorder(new TitledBorder(null, "Load Profile Properties", TitledBorder.LEADING,
 						TitledBorder.TOP, null, null));
 				pnlLoadProfileProperties.setName("pnlLoadProfileProperties");
 				pnlMain.add(pnlLoadProfileProperties, "flowx,cell 0 0,grow");
 				pnlLoadProfileProperties
 						.setLayout(new MigLayout("insets 4", "[270,grow]8[]8[200]8[]8[200]", "[][][][grow]"));
 				{
 					lblName = new JLabel("Name");
 					lblName.setDisplayedMnemonic('N');
 					lblName.setHorizontalAlignment(SwingConstants.CENTER);
 					lblName.setName("lblName");
 					pnlLoadProfileProperties.add(lblName, "cell 0 0");
 				}
 				{
 					JSeparator separator = new JSeparator();
 					separator.setPreferredSize(new Dimension(0, 200));
 					separator.setOrientation(SwingConstants.VERTICAL);
 					separator.setName("separator");
 					pnlLoadProfileProperties.add(separator, "cell 1 0 1 4, growy");
 				}
 				{
 					JLabel lblClient = new JLabel("Clients");
 					lblClient.setName("lblClient");
 					pnlLoadProfileProperties.add(lblClient, "cell 2 0");
 				}
 				{
 					JSeparator separator = new JSeparator();
 					separator.setPreferredSize(new Dimension(0, 200));
 					separator.setOrientation(SwingConstants.VERTICAL);
 					separator.setName("separator");
 					pnlLoadProfileProperties.add(separator, "cell 3 0 1 4, growy");
 				}
 				{
 					lblTargets = new JLabel("Targets");
 					lblTargets.setName("lblTargets");
 					pnlLoadProfileProperties.add(lblTargets, "cell 4 0");
 				}
 				{
 					txtName = new JTextField();
 					lblName.setLabelFor(txtName);
 					txtName.setColumns(10);
 					txtName.setName("txtName");
 					txtName.getDocument().addDocumentListener(dirtyListener);
 					pnlLoadProfileProperties.add(txtName, "cell 0 1,growx");
 				}
 				{
 					JScrollPane spClients = new JScrollPane();
 					spClients.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 					spClients.setName("spClients");
 					pnlLoadProfileProperties.add(spClients, "cell 2 1 1 3,grow");
 					{
 						tblClients = new JCheckListTable();
 						tblClients.setName("tblClients");
 						spClients.setViewportView(tblClients);
 						spClients.setColumnHeaderView(null);
 					}
 				}
 				{
 					JScrollPane spTargets = new JScrollPane();
 					spTargets.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 					spTargets.setName("spTargets");
 					pnlLoadProfileProperties.add(spTargets, "cell 4 1 1 3,grow");
 					{
 						tblTargets = new JCheckListTable();
 						tblTargets.setName("tblTargets");
 						spTargets.setViewportView(tblTargets);
 						spTargets.setColumnHeaderView(null);
 					}
 				}
 				{
 					lblDescription = new JLabel("Description");
 					lblDescription.setDisplayedMnemonic('D');
 					lblDescription.setName("lblDescription");
 					pnlLoadProfileProperties.add(lblDescription, "cell 0 2");
 				}
 				{
 					JScrollPane spDescription = new JScrollPane();
 					spDescription.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
 					spDescription.setName("spDescription");
 					pnlLoadProfileProperties.add(spDescription, "cell 0 3,height 50:50:,grow");
 					{
 						taDescription = new JTextArea();
 						taDescription.setFont(txtName.getFont());
 						lblDescription.setLabelFor(taDescription);
 						taDescription.setRows(3);
 						taDescription.setName("taDescription");
 						taDescription.getDocument().addDocumentListener(dirtyListener);
 						spDescription.setViewportView(taDescription);
 					}
 				}
 			}
 			{
 				JPanel pnlCurveAssignment = new JPanel();
 				pnlCurveAssignment.setBorder(new TitledBorder(null, "Active Load Curve Assignment", TitledBorder.LEADING,
 						TitledBorder.TOP, null, null));
 				pnlMain.add(pnlCurveAssignment, "cell 0 1,grow");
 				pnlCurveAssignment.setLayout(new MigLayout("insets 4", "[grow]", "[grow][]"));
 				{
 					pnlCard = new JPanel();
 					pnlCard.setName("pnlCard");
 					pnlCurveAssignment.add(pnlCard, "cell 0 0,grow");
 					cardLayout = new CardLayout(0, 0);
 					pnlCard.setLayout(cardLayout);
 					{
 						stairsPanel = new StairsPanel();
 						stairsPanel.setName("stairsPanel");
 						pnlCard.add(stairsPanel, "stairs");
 					}
 					{
 						oneTimePanel = new OneTimePanel();
 						oneTimePanel.setName("oneTimePanel");
 						pnlCard.add(oneTimePanel, "oneTime");
 					}
 					{
 						markerPanel = new MarkerPanel();
 						markerPanel.setName("markerPanel");
 						pnlCard.add(markerPanel, "marker");
 					}
 					{
 						JLabel lblNoActiveCurve = new JLabel("no active curve assignment");
 						lblNoActiveCurve.setHorizontalAlignment(SwingConstants.CENTER);
 						pnlCard.add(lblNoActiveCurve, "none");
 						lblNoActiveCurve.setName("lblNoActiveCurve");
 					}
 				}
 				{
 					btnOk = new JButtonExt("OK");
 					getRootPane().setDefaultButton(btnOk);
 					btnOk.setEnabled(false);
 					btnOk.addActionListener(new BtnOkActionListener());
 					btnOk.setMnemonic(KeyEvent.VK_O);
 					btnOk.setName("btnOk");
 					pnlCurveAssignment.add(btnOk, "cell 0 1,alignx right");
 				}
 				{
 					btnCancel = new JButtonExt("Cancel");
 					btnCancel.setEnabled(false);
 					btnCancel.addActionListener(new BtnCancelActionListener());
 					btnCancel.setMnemonic(KeyEvent.VK_C);
 					btnCancel.setName("btnCancel");
 					pnlCurveAssignment.add(btnCancel, "cell 0 1,alignx right");
 				}
 			}
 		}
 	}
 
 	private void initMenuBar(final JMenuBar menuBar) {
 		JMenu menu = new JMenu("File");
 		menu.setMnemonic('F');
 		menu.add(fileNewLoadProfileConfigAction);
 		menu.add(fileOpenLoadProfileConfigAction);
 		menu.addSeparator();
 		menu.add(fileSaveLoadProfileConfigAction);
 		menu.add(fileSaveLoadProfileConfigAsAction);
 		menu.addSeparator();
 		menu.add(new FileExitAction(eventBus));
 		menuBar.add(menu);
 
 		menu = new JMenu("Tools");
 		menu.setMnemonic('T');
 		menu.add(toolsAddStairsAction);
 		menu.add(toolsAddOneTimeAction);
 		menu.add(toolsAddMarkerAction);
 		menu.add(toolsDeleteLoadProfileEntityAction);
 		menu.addSeparator();
 		menu.add(toolsExportEventListAction);
 		menu.addSeparator();
 		menu.add(toolsSettingsAction);
 		menuBar.add(menu);
 
 		menu = new JMenu("Help");
 		menu.setMnemonic('H');
 		menu.add(helpAboutAction);
 		menuBar.add(menu);
 	}
 
 	private void initToolBar(final JToolBar toolBar) {
 		toolBar.add(fileNewLoadProfileConfigAction);
 		toolBar.add(fileOpenLoadProfileConfigAction);
 		toolBar.add(fileSaveLoadProfileConfigAction);
 		toolBar.addSeparator();
 		toolBar.add(toolsAddStairsAction);
 		toolBar.add(toolsAddOneTimeAction);
 		toolBar.add(toolsAddMarkerAction);
 		toolBar.add(toolsDeleteLoadProfileEntityAction);
 		toolBar.addSeparator();
 		toolBar.add(toolsExportEventListAction);
 		toolBar.addSeparator();
 		toolBar.add(toolsSettingsAction);
 		toolBar.addSeparator();
 		toolBar.add(helpAboutAction);
 	}
 
 	private void initModels() {
 		EventList<LoadProfileEntity> treeItems = GlazedLists.eventListOf();
 		treeModel = new EventsTreeModel(treeItems);
 		tree.setModel(treeModel);
 
 		TableFormat<SelectionDecorator> tableFormat = GlazedLists.tableFormat(SelectionDecorator.class, new String[] {
 				"selected",
 				"baseObject.name" },
 				new String[] { "Selected", "Name" }, new boolean[] { true, false });
 
 		decoratedClients = GlazedLists.eventListOf();
 
 		EventTableModel<SelectionDecorator> clientsTableModel = new EventTableModel<SelectionDecorator>(decoratedClients,
 				tableFormat);
 		tblClients.setModel(clientsTableModel);
 		tblClients.getColumnModel().getColumn(0).setMaxWidth(24);
 
 		decoratedTargets = GlazedLists.eventListOf();
 		oneTimeDecoratedTargets = GlazedLists.eventListOf();
 
 		EventTableModel<SelectionDecorator> targetsTableModel = new EventTableModel<SelectionDecorator>(decoratedTargets,
 				tableFormat);
 		targetsTableModel.addTableModelListener(new TableModelListener() {
 			@Override
 			public void tableChanged(final TableModelEvent e) {
 				ModelUtils.updateTargetDecorators(decoratedTargets, oneTimeDecoratedTargets,
 						loadProfilesController.getTargets(), true);
 				if (activeLoadProfileEntityPanel instanceof OneTimePanel) {
 					activeLoadProfileEntityPanel.repaint();
 				}
 			}
 		});
 
 		tblTargets.setModel(targetsTableModel);
 		tblTargets.getColumnModel().getColumn(0).setMaxWidth(24);
 
 		EventList<Target> targets = GlazedLists.<Target>eventListOf();
 		EventList<Operation> operations = GlazedLists.<Operation>eventListOf();
 		EventList<Client> clients = GlazedLists.<Client>eventListOf();
 
 		stairsPanel.setCboOperationModel(new EventComboBoxModel<Operation>(operations));
 		oneTimePanel.setCboOperationModel(new EventComboBoxModel<Operation>(operations));
 
 		loadProfilesController.setTreeItems(treeItems);
 		loadProfilesController.setOperations(operations);
 		loadProfilesController.setTargets(targets);
 		loadProfilesController.setClients(clients);
 
 		oneTimePanel.setTblTargetModel(new EventTableModel<SelectionDecorator>(oneTimeDecoratedTargets, tableFormat));
 	}
 
 	private void updateGraph() {
 		XYSeriesCollection dataset = new XYSeriesCollection();
 		Collection<Stairs> loadProfileEnities = transform(filter(loadProfilesController.getTreeItems(), new IsStairsPredicate()),
 				new LoadProfileEntityToStairsFunction());
 		GraphPointsCalculator calc = new GraphPointsCalculator();
 		Map<String, Set<Point>> pointsMap = calc.calculatePoints(loadProfileEnities);
 
 		for (Entry<String, Set<Point>> entry : pointsMap.entrySet()) {
 			final XYSeries series = new XYSeries(entry.getKey());
 			for (Point point : entry.getValue()) {
 				series.add(point.getX(), point.getY());
 			}
 			dataset.addSeries(series);
 		}
 
 		String name = txtName.getText();
 		chart = ChartFactory.createXYLineChart(name, "t (min)", "Executions (1/h)", dataset, PlotOrientation.VERTICAL, true,
 				true, false);
 
 		XYPlot plot = chart.getXYPlot();
 
 		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
 		plot.setRenderer(renderer);
 
 		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
 		rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
 		domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
 
 		double maxX = 0;
 
 		for (OneTime oneTime : getOneTimes()) {
 			String key = oneTime.operation.getName();
 			XYSeries series;
 			try {
 				// We need the series in order to retrieve paint and stroke
 				series = dataset.getSeries(key);
 			} catch (UnknownKeyException ex) {
 				series = new XYSeries(key);
 				dataset.addSeries(series);
 			}
 
 			int index = dataset.getSeriesIndex(key);
 			BasicStroke stroke = (BasicStroke) renderer.lookupSeriesStroke(index);
 			stroke = new BasicStroke(stroke.getLineWidth() + 1f, stroke.getEndCap(), stroke.getLineJoin(),
 					stroke.getMiterLimit(), stroke.getDashArray(), stroke.getDashPhase());
 			Color paint = (Color) renderer.lookupSeriesPaint(index);
 			paint = new Color(paint.getRed(), paint.getGreen(), paint.getBlue(), 160);
 
 			double height = rangeAxis.getUpperBound() * .05; // five percent of range
 			double width = domainAxis.getUpperBound() * .01; // one percent of range
 			double center = oneTime.t0;
 			double left = center - width / 2;
 			double right = center + width / 2;
 
 			// We only add annotations for one times, but nothing to the series
 			plot.addAnnotation(new XYPolygonAnnotation(new double[] { left, 0d, center, height, right, 0d }, stroke, paint, paint));
 
 			maxX = max(maxX, right);
 		}
 
 		for (Marker marker : getMarkers()) {
 			IntervalMarker im = new IntervalMarker(marker.left, marker.right);
 			im.setLabel(marker.name);
 			im.setLabelFont(new Font(getFont().getName(), getFont().getStyle(), getFont().getSize() + 1));
 			im.setLabelAnchor(RectangleAnchor.TOP);
 			im.setLabelOffset(new RectangleInsets(8d, 0d, 0d, 0d));
 			im.setLabelPaint(Color.BLACK);
 			im.setAlpha(.3f);
 			im.setPaint(Color.WHITE);
 			im.setOutlinePaint(Color.BLACK);
 			im.setOutlineStroke(new BasicStroke(1.0f));
 			plot.addDomainMarker(im, Layer.BACKGROUND);
 
 			maxX = max(maxX, marker.right);
 		}
 
 		if (domainAxis.getUpperBound() < maxX) {
 			domainAxis.setUpperBound(maxX * 1.05);
 		}
 		chartPanel.setChart(chart);
 	}
 
 	/**
 	 * Creates a new load profile configuration. This method is registered on the {@link EventBus}
 	 * and called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void newLoadProfileConfiguration(final FileNewLoadProfileConfigAction.Event e) {
 		if (checkLoadProfileEntityDirty() && checkLoadProfilePropertiesDirty()) {
 			loadProfilesController.getTreeItems().clear();
 			expandTree();
 
 			txtName.setText("");
 			taDescription.setText("");
 
 			for (SelectionDecorator sd : decoratedTargets) {
 				sd.setSelected(false);
 			}
 			for (SelectionDecorator sd : decoratedClients) {
 				sd.setSelected(false);
 			}
 
 			updateGraph();
 			tblClients.repaint();
 			tblTargets.repaint();
 			dirty = false;
 			loadProfileConfigFile = null;
 			loadProfileEventsFile = null;
 		}
 	}
 
 	/**
 	 * Opens a load profile configuration. This method is registered on the {@link EventBus} and
 	 * called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void openLoadProfileConfiguration(final FileOpenLoadProfileConfigAction.Event e) {
 		if (checkLoadProfileEntityDirty() && checkLoadProfilePropertiesDirty()) {
 			File dir = loadProfileConfigFile != null ? loadProfileConfigFile : new File(".");
 			JFileChooser fc = SwingUtils.createFileChooser(dir, "XML Files (*.xml)", "xml");
 			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
 				File file = fc.getSelectedFile();
 				try {
 					removeTableListeners();
 
 					activeLoadProfileConfig = loadProfilesController.loadProfileConfig(file);
 
 					txtName.setText(activeLoadProfileConfig.getName());
 					taDescription.setText(activeLoadProfileConfig.getDescription());
 					for (SelectionDecorator sd : decoratedTargets) {
 						sd.setSelected(activeLoadProfileConfig.getTargets().contains(sd.getBaseObject()));
 					}
 					for (SelectionDecorator sd : decoratedClients) {
 						sd.setSelected(activeLoadProfileConfig.getClients().contains(sd.getBaseObject()));
 					}
 					loadProfilesController.getTreeItems().clear();
 					for (LoadProfileEntity lpe : activeLoadProfileConfig.getLoadProfileEntities()) {
 						loadProfilesController.addOrUpdateLoadProfileEntity(lpe);
 					}
 					ModelUtils.updateTargetDecorators(decoratedTargets, oneTimeDecoratedTargets,
 							loadProfilesController.getTargets(), true);
 					expandTree();
 					updateGraph();
 					tblClients.repaint();
 					tblTargets.repaint();
 					loadProfileConfigFile = file;
 					if (loadProfileEventsFile != null
 							&& !getBaseName(loadProfileConfigFile.getName()).equals(getBaseName(loadProfileEventsFile.getName()))) {
 						loadProfileEventsFile = null;
 					}
 
 					setTitle(APP_TITLE + " - [" + loadProfileConfigFile + "]");
 					dirty = false;
 				} catch (JAXBException ex) {
 					throw new RuntimeException("Error loading load profile configuration: " + file, ex);
 				} finally {
 					addTableListeners();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Saves a load profile configuration. This method is registered on the {@link EventBus} and
 	 * called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void saveLoadProfileConfiguration(final FileSaveLoadProfileConfigAction.Event e) {
 		saveLoadProfileConfiguration(false);
 	}
 
 	/**
 	 * Saves a load profile configuration under a new name. This method is registered on the
 	 * {@link EventBus} and called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void saveLoadProfileConfigurationAs(final FileSaveLoadProfileConfigAsAction.Event e) {
 		saveLoadProfileConfiguration(true);
 	}
 
 	/**
 	 * Saves the load curve configuration to a file. If the configuration has not been saved before
 	 * or if {@code saveAs} is {@code true}, a save dialog is shown for specifying the file.
 	 * 
 	 * @param saveAs
 	 *            {@code true}, if the save dialog should be shown
 	 */
 	private void saveLoadProfileConfiguration(final boolean saveAs) {
 		if (checkLoadProfileEntityDirty()) {
 			checkClientsAndTargets();
 			validateLoadProfile();
 
 			if (saveAs || loadProfileConfigFile == null) {
 				File dir = loadProfileConfigFile != null ? loadProfileConfigFile : new File(".");
 				JFileChooser fc = SwingUtils.createFileChooser(dir, "XML Files (*.xml)", "xml");
 				loadProfileConfigFile = showSaveDialog(fc, loadProfileConfigFile, "xml");
 				if (loadProfileConfigFile == null) {
 					return;
 				}
 			}
 
 			File pngFile = new File(removeExtension(loadProfileConfigFile.getPath()) + ".png");
 			try {
 				loadProfileEventsFile = null;
 				LoadProfileConfig lpc = createLoadProfileConfig();
 				loadProfilesController.saveProfileConfig(loadProfileConfigFile, lpc);
 
 				setTitle(APP_TITLE + " - [" + loadProfileConfigFile + "]");
 				dirty = false;
 
 				BufferedImage image = chart.createBufferedImage(chartPanel.getWidth(), chartPanel.getHeight());
 				ImageIO.write(image, "png", pngFile);
 			} catch (JAXBException ex) {
 				throw new RuntimeException("Error saving load profile configuration: " + loadProfileConfigFile, ex);
 			} catch (IOException ex) {
 				throw new RuntimeException("Error saving load profile image: " + pngFile, ex);
 			}
 		}
 	}
 
 	/**
 	 * Creates a new stairs element. This method is registered on the {@link EventBus} and called
 	 * when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void newStairs(final ToolsAddStairsAction.Event e) {
 		checkClientsAndTargets();
 		if (checkLoadProfileEntityDirty()) {
 			tree.clearSelection();
 			activeLeafPath = null;
 			editLoadProfileEntity(new Stairs());
 			activeLoadProfileEntityPanel.setDirty(true);
 		}
 	}
 
 	/**
 	 * Creates a new one-time element. This method is registered on the {@link EventBus} and called
 	 * when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void newOneTime(final ToolsAddOneTimeAction.Event e) {
 		checkClientsAndTargets();
 		if (checkLoadProfileEntityDirty()) {
 			tree.clearSelection();
 			activeLeafPath = null;
 			editLoadProfileEntity(new OneTime());
 			activeLoadProfileEntityPanel.setDirty(true);
 		}
 	}
 
 	/**
 	 * Creates a new marker element. This method is registered on the {@link EventBus} and called
 	 * when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void newMarker(final ToolsAddMarkerAction.Event e) {
 		checkClientsAndTargets();
 		if (checkLoadProfileEntityDirty()) {
 			tree.clearSelection();
 			activeLeafPath = null;
 			editLoadProfileEntity(new Marker());
 			activeLoadProfileEntityPanel.setDirty(true);
 		}
 	}
 
 	/**
 	 * Deletes the selected load profile entity. This method is registered on the {@link EventBus}
 	 * and called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void deleteLoadProfileEntity(final ToolsDeleteLoadProfileEntityAction.Event e) {
 		Object obj = tree.getLastSelectedPathComponent();
 		if (obj instanceof LoadProfileEntity) {
 			if (JOptionPane.showConfirmDialog(AppFrame.this, "Delete element?", UIManager.getString("OptionPane.titleText"),
 					JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
 				LoadProfileEntity selectedLoadProfileEntity = (LoadProfileEntity) obj;
 				EventList<LoadProfileEntity> treeItems = loadProfilesController.getTreeItems();
 
 				// We need to sort in order to find the correct sibling to select next
 				Collections.sort(treeItems);
 				int index = treeItems.indexOf(selectedLoadProfileEntity);
 				treeItems.remove(index);
 
 				if (!treeItems.isEmpty()) {
 					if (index >= 0) {
 						index = max(0, index - 1);
 					}
 					selectedLoadProfileEntity = treeItems.get(index);
 					expandTreeAndSelect(selectedLoadProfileEntity);
 				}
 				updateGraph();
 			}
 		}
 	}
 
 	/**
 	 * Exports the events file for perfLoad tests. This method is registered on the {@link EventBus}
 	 * and called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void exportEventListForPerfLoad(final ToolsExportEventListAction.Event e) throws IOException {
 		if (checkLoadProfileEntityDirty() && checkLoadProfilePropertiesDirty()) {
 			File dir = loadProfileEventsFile != null ? loadProfileEventsFile : loadProfileConfigFile.getParentFile();
 			JFileChooser fc = SwingUtils.createFileChooser(dir, "Load Profile BaseLoadProfileEvent Files (*.perfload)",
 					"perfload");
 			fc.setAccessory(new SaveAccessoryPanel());
 
 			if (loadProfileEventsFile == null) {
 				loadProfileEventsFile = new File(FilenameUtils.removeExtension(loadProfileConfigFile.getAbsolutePath())
 						+ ".perfload");
 			}
 
 			File file = showSaveDialog(fc, loadProfileEventsFile, "perfload");
 			if (file != null) {
 				loadProfileEventsFile = file;
 
 				LoadProfileConfig lpc = createLoadProfileConfig();
 				LoadTestConfiguration ltc = loadProfilesController.createLoadTestConfiguration(lpc, getSelectedTargets(),
 						getSelectedClients());
 
 				List<LoadCurveAssignment> loadCurveAssignments = ltc.getLoadCurveAssignments();
 				Set<Operation> operations = newHashSet();
 				int caCount = loadCurveAssignments.size();
 
 				double maxTime = 0; // max time for histogram creation
 
 				List<LoadCurve> loadCurves = newArrayListWithCapacity(caCount);
 				for (LoadCurveAssignment loadCurveAssignment : loadCurveAssignments) {
 					LoadCurve loadCurve = loadCurveAssignment.getLoadCurve();
 					loadCurves.add(loadCurve);
 					operations.add(loadCurveAssignment.getOperation());
 					double[] timeValues = loadCurve.getTimeValues();
 					maxTime = max(maxTime, timeValues[timeValues.length - 1]);
 				}
 
 				EventDistributor.addScaledLoadCurvesToAssignments(ltc, loadCurves);
 
 				List<LoadEvent> clientEventList = EventDistributor.createClientEventList(ltc);
 				List<BaseLoadProfileEvent> events = newArrayListWithExpectedSize(clientEventList.size());
 
 				// One time and marker events are added separately
 
 				for (OneTime oneTime : getOneTimes()) {
 					double startTimeInHours = oneTime.t0 / 60d;
 
 					// We must add one event per target
 					for (Target target : oneTime.targets) {
 						LoadEvent event = new LoadEvent(startTimeInHours, oneTime.getOperation());
						event.setProcessId(0); // 1 added later to make it zero-based
						event.setDaemonId(1);
 						event.setTarget(target);
 						clientEventList.add(event);
 					}
 				}
 
 				events.addAll(clientEventList);
 
 				for (Marker marker : getMarkers()) {
 					double time = marker.left / 60d;
 					MarkerEvent event = new MarkerEvent(marker.name, time, MarkerEvent.Type.left);
 					events.add(event);
 
 					time = marker.right / 60d;
 					event = new MarkerEvent(marker.name, time, MarkerEvent.Type.right);
 					events.add(event);
 				}
 
 				Collections.sort(events, new LoadEventComparator());
 
 				StrBuilder sb = new StrBuilder();
 				sb.appendln("# Created: " + new Date());
 				sb.appendln("# Load Profile Config File: " + loadProfileConfigFile.getName());
 				sb.append("# Load Profile Name: " + txtName.getText());
 
 				EventDistributor.writeEventListForPerfLoadClientsToFile(file, sb.toString(), events);
 
 				// Create additional histogram file if selected
 				dir = file.getParentFile();
 				final String baseName = FilenameUtils.getBaseName(file.getName());
 				int numClients = ltc.getClients().size();
 				SaveAccessoryPanel sap = (SaveAccessoryPanel) fc.getAccessory();
 				Collection<LoadEvent> loadEvents = transform(filter(clientEventList, new IsLoadEventPredicate()),
 						new EventToLoadEventFunction());
 
 				if (sap.isEventDistriChecked()) {
 					for (int i = 0; i < numClients; ++i) {
 						for (LoadCurve loadCurve : loadCurves) {
 							File f = new File(dir, baseName + "-event-distri-client-" + i + "-" + loadCurve.getName() + ".csv");
 							PlotFileCreator.createPlot(f, loadEvents, loadCurve, i, LoadCurveCalculator.timeUnit_minute);
 						}
 					}
 				}
 
 				if (sap.isOperationHistogramChecked()) {
 					for (Operation operation : operations) {
 						String opName = operation.getName();
 						File f = new File(dir, baseName + "-histogram-operation-" + opName + ".csv");
 						PlotFileCreator.createOperationHistogram(f, loadEvents, opName, (int) maxTime * 2, 0., maxTime,
 								LoadCurveCalculator.timeUnit_minute);
 					}
 				}
 
 				if (sap.isClientLoadHistrogramChecked()) {
 					for (int i = 0; i < numClients; i++) {
 						File f = new File(dir, baseName + "-histogram-client-load-" + i + ".csv");
 						PlotFileCreator.createClientHistogram(f, loadEvents, i, (int) maxTime * 2, 0., maxTime,
 								LoadCurveCalculator.timeUnit_minute);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Creates a new load profile configuration. This method is registered on the {@link EventBus}
 	 * and called when the specified event is posted.
 	 * 
 	 * @param e
 	 *            the event that triggers calling of this method when posted on the event bus
 	 */
 	@Subscribe
 	public void openSettingsDialog(final ToolsSettingsAction.Event e) {
 		if (checkLoadProfileEntityDirty() && checkLoadProfilePropertiesDirty()) {
 			Cursor oldCursor = getCursor();
 			SettingsDialog dlg = new SettingsDialog(AppFrame.this, configController);
 			try {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				dlg.setLocationRelativeTo(AppFrame.this);
 				dlg.setVisible(true);
 
 				if (dlg.getModalResult() == ModalResult.OK) {
 					boolean oldDirty = dirty;
 
 					updateLists();
 					ModelUtils.updateTargetDecorators(decoratedTargets, oneTimeDecoratedTargets,
 							loadProfilesController.getTargets(), true);
 					loadProfilesController.updateTreeItems();
 					expandTree();
 
 					tblClients.repaint();
 					tblTargets.repaint();
 					if (activeLoadProfileEntityPanel != null) {
 						activeLoadProfileEntityPanel.repaint();
 					}
 					dirty = oldDirty;
 				}
 			} finally {
 				setCursor(oldCursor);
 			}
 		}
 	}
 
 	@Subscribe
 	public void openAboutDialog(@SuppressWarnings("unused") final HelpAboutAction.Event e) {
 		new AboutDialog(this, appVersion).setVisible(true);
 	}
 
 	private void checkClientsAndTargets() {
 		SelectionDecoratorCheckedPredicate p = new SelectionDecoratorCheckedPredicate();
 		if (filter(decoratedTargets, p).isEmpty() || filter(decoratedClients, p).isEmpty()) {
 			throw new LoadProfileException("Please select targets and clients for the load profile first.");
 		}
 	}
 
 	private Collection<Target> getSelectedTargets() {
 		SelectionDecoratorCheckedPredicate p = new SelectionDecoratorCheckedPredicate();
 		return transform(filter(decoratedTargets, p), new SelectionDecoratorToTargetFunction());
 	}
 
 	private Collection<Client> getSelectedClients() {
 		SelectionDecoratorCheckedPredicate p = new SelectionDecoratorCheckedPredicate();
 		return transform(filter(decoratedClients, p), new SelectionDecoratorToClientFunction());
 	}
 
 	private Collection<OneTime> getOneTimes() {
 		return transform(filter(loadProfilesController.getTreeItems(), new IsOneTimePredicate()),
 				new LoadProfileEntityToOneTimeFunction());
 	}
 
 	private Collection<Marker> getMarkers() {
 		return transform(filter(loadProfilesController.getTreeItems(), new IsMarkerPredicate()),
 				new LoadProfileEntityToMarkerFunction());
 	}
 
 	private void editLoadProfileEntity(final LoadProfileEntity lpe) {
 		LOG.debug("editLoadProfileEntity({})", lpe);
 		loadProfilesController.checkCurveCreationPossible();
 
 		removeDirtyListeners();
 
 		if (lpe instanceof OneTime) {
 			oneTimePanel.setLoadProfileEntity((OneTime) lpe);
 			if (activeLoadProfileEntityPanel != oneTimePanel) {
 				activeLoadProfileEntityPanel = oneTimePanel;
 				cardLayout.show(pnlCard, "oneTime");
 			}
 		} else if (lpe instanceof Stairs) {
 			stairsPanel.setLoadProfileEntity((Stairs) lpe);
 			if (activeLoadProfileEntityPanel != stairsPanel) {
 				activeLoadProfileEntityPanel = stairsPanel;
 				cardLayout.show(pnlCard, "stairs");
 			}
 		} else if (lpe instanceof Marker) {
 			markerPanel.setLoadProfileEntity((Marker) lpe);
 			if (activeLoadProfileEntityPanel != markerPanel) {
 				activeLoadProfileEntityPanel = markerPanel;
 				cardLayout.show(pnlCard, "marker");
 			}
 		} else {
 			throw new IllegalStateException("Illegal cure assignment type.");
 		}
 		expandTree();
 		activeLoadProfileEntityPanel.setDirty(false);
 		setCurveAssignmentButtonsEnabled(false);
 		addDirtyListeners();
 		activeLoadProfileEntityPanel.repaint();
 	}
 
 	private void addTableListeners() {
 		tblClients.getModel().addTableModelListener(dirtyListener);
 		tblTargets.getModel().addTableModelListener(dirtyListener);
 	}
 
 	private void removeTableListeners() {
 		tblClients.getModel().removeTableModelListener(dirtyListener);
 		tblTargets.getModel().removeTableModelListener(dirtyListener);
 	}
 
 	private void addDirtyListeners() {
 		if (activeLoadProfileEntityPanel != null) {
 			activeLoadProfileEntityPanel.addPropertyChangeListener("dirty", dirtyListener);
 			activeLoadProfileEntityPanel.enableListeners();
 		}
 	}
 
 	private void removeDirtyListeners() {
 		if (activeLoadProfileEntityPanel != null) {
 			activeLoadProfileEntityPanel.disableListeners();
 			activeLoadProfileEntityPanel.removePropertyChangeListener("dirty", dirtyListener);
 		}
 	}
 
 	public void editOrCancelLoadProfileEntity() {
 		Object obj = tree.getLastSelectedPathComponent();
 		if (obj instanceof LoadProfileEntity) {
 			editLoadProfileEntity((LoadProfileEntity) obj);
 		} else {
 			cancelEditingCurveAssignment();
 		}
 	}
 
 	private void setCurveAssignmentButtonsEnabled(final boolean enabled) {
 		LOG.debug("setCurveAssignmentButtonsEnabled({})", enabled);
 		btnOk.setEnabled(activeLoadProfileEntityPanel != null && enabled);
 		btnCancel.setEnabled(activeLoadProfileEntityPanel != null && enabled);
 	}
 
 	private void expandTree() {
 		List<TreePath> paths = treeModel.getPaths();
 		for (TreePath path : paths) {
 			if (path.equals(activeLeafPath)) {
 				tree.setSelectionPath(path);
 				tree.scrollPathToVisible(path);
 			}
 			tree.expandPath(path.getParentPath());
 		}
 	}
 
 	private void expandTreeAndSelect(final LoadProfileEntity selectedItem) {
 		List<TreePath> paths = treeModel.getPaths();
 		for (TreePath path : paths) {
 			if (path.getLastPathComponent().equals(selectedItem)) {
 				tree.setSelectionPath(path);
 				tree.scrollPathToVisible(path);
 			}
 			tree.expandPath(path.getParentPath());
 		}
 	}
 
 	private void saveLoadProfileEntity() {
 		saveLoadProfileEntity(true);
 	}
 
 	private void saveLoadProfileEntity(final boolean updateTree) {
 		LOG.debug("saveLoadProfileEntity({})", updateTree);
 		validateLoadProfileEntity();
 		LoadProfileEntity lpe = activeLoadProfileEntityPanel.getLoadProfileEntity();
 		activeLoadProfileEntityPanel.setDirty(false);
 		loadProfilesController.addOrUpdateLoadProfileEntity(lpe);
 		if (updateTree) {
 			expandTreeAndSelect(lpe);
 		}
 		updateGraph();
 	}
 
 	private void validateLoadProfileEntity() {
 		LoadProfileEntity lpe = activeLoadProfileEntityPanel.getLoadProfileEntity();
 		if (lpe instanceof CurveAssignment) {
 			CurveAssignment ca = (CurveAssignment) lpe;
 			if (ca.getOperation() == null) {
 				throw new LoadProfileException("Please select an operation.");
 			}
 			if (ca instanceof OneTime) {
 				OneTime oneTime = (OneTime) ca;
 				if (oneTime.targets.isEmpty()) {
 					throw new LoadProfileException("Please select at least one target for the one-time event.");
 				}
 			}
 		}
 	}
 
 	private void cancelEditingCurveAssignment() {
 		LOG.debug("cancelEditingCurveAssignment()");
 		cardLayout.show(pnlCard, "none");
 		if (activeLoadProfileEntityPanel != null) {
 			activeLoadProfileEntityPanel.setDirty(false);
 			removeDirtyListeners();
 			activeLoadProfileEntityPanel = null;
 		}
 	}
 
 	private void updateLists() {
 		ModelUtils.updateOperations(configController.getActiveSettings().getOperations(), loadProfilesController.getOperations());
 		ModelUtils.updateTargets(configController.getActiveSettings().getTargets(), loadProfilesController.getTargets());
 		ModelUtils.updateClients(configController.getActiveSettings().getClients(), loadProfilesController.getClients());
 		ModelUtils.updateTargetDecorators(decoratedTargets, oneTimeDecoratedTargets, loadProfilesController.getTargets(), false);
 		ModelUtils.updateClientDecorators(decoratedClients, loadProfilesController.getClients());
 	}
 
 	private void validateLoadProfile() {
 		boolean valid = true;
 
 		StringBuilder sb = new StringBuilder("The following error(s) occured:\n");
 		if (isBlank(txtName.getText())) {
 			valid = false;
 			sb.append("- Please enter a name for your load profile configuration.\n");
 		}
 		if (isBlank(taDescription.getText())) {
 			valid = false;
 			sb.append("- Please enter a description for your load profile configuration.\n");
 		}
 		if (loadProfilesController.getTreeItems().isEmpty()) {
 			valid = false;
 			sb.append("- You must add at least one load curve assignment.\n");
 		}
 
 		if (!valid) {
 			throw new LoadProfileException(sb.toString());
 		}
 	}
 
 	private class BtnOkActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(final ActionEvent e) {
 			saveLoadProfileEntity();
 		}
 	}
 
 	private class BtnCancelActionListener implements ActionListener {
 		@Override
 		public void actionPerformed(final ActionEvent e) {
 			// reset and edit again when canceled
 			editOrCancelLoadProfileEntity();
 		}
 	}
 
 	private class TreeTreeSelectionListener implements TreeSelectionListener {
 		private boolean changing;
 
 		@Override
 		public void valueChanged(final TreeSelectionEvent e) {
 			if (changing) { // this avoids re-entry
 				return;
 			}
 
 			try {
 				changing = true;
 				Object obj = tree.getLastSelectedPathComponent();
 				if (obj instanceof LoadProfileEntity) {
 					LoadProfileEntity selectedLoadProfileEntity = (LoadProfileEntity) obj;
 					TreePath newPath = e.getNewLeadSelectionPath();
 					TreePath oldPath = e.getOldLeadSelectionPath();
 					if (newPath != null && !newPath.equals(oldPath)) {
 						if (checkLoadProfileEntityDirty()) {
 							if (newPath.getPath().length == 3) {
 								editLoadProfileEntity(selectedLoadProfileEntity);
 								activeLeafPath = newPath;
 								tree.expandPath(activeLeafPath.getParentPath());
 								tree.setSelectionPath(activeLeafPath);
 							} else {
 								cancelEditingCurveAssignment();
 							}
 						}
 					}
 				} else {
 					cancelEditingCurveAssignment();
 				}
 			} finally {
 				changing = false;
 			}
 		}
 	}
 
 	private class TreeKeyListener extends KeyAdapter {
 		@Override
 		public void keyPressed(final KeyEvent e) {
 			if (e.getKeyCode() == KeyEvent.VK_DELETE) {
 				deleteLoadProfileEntity(null);
 			}
 		}
 	}
 
 	private class DirtyListener implements DocumentListener, TableModelListener, PropertyChangeListener {
 
 		@Override
 		public void tableChanged(final TableModelEvent e) {
 			dirty = true;
 		}
 
 		@Override
 		public void insertUpdate(final DocumentEvent e) {
 			dirty = true;
 		}
 
 		@Override
 		public void removeUpdate(final DocumentEvent e) {
 			dirty = true;
 		}
 
 		@Override
 		public void changedUpdate(final DocumentEvent e) {
 			dirty = true;
 		}
 
 		@Override
 		public void propertyChange(final PropertyChangeEvent evt) {
 			LOG.debug("property change: {}-{}", evt.getSource().getClass().getSimpleName(), evt.getNewValue());
 			Boolean flag = (Boolean) evt.getNewValue();
 			setCurveAssignmentButtonsEnabled(flag);
 			if (flag) {
 				dirty = true;
 			}
 		}
 	}
 
 	private class ThisWindowListener extends WindowAdapter {
 		@Override
 		public void windowClosing(final WindowEvent e) {
 			closeFrame();
 		}
 
 		@Override
 		public void windowStateChanged(final WindowEvent e) {
 			int state = e.getNewState();
 			configController.getAppProperties().setProperty("frame.state", String.valueOf(state));
 		}
 	}
 
 	private class ThisComponentListener extends ComponentAdapter {
 		@Override
 		public void componentResized(final ComponentEvent e) {
 			int state = getExtendedState();
 			if ((state & Frame.MAXIMIZED_HORIZ) == 0) {
 				configController.getAppProperties().setProperty("frame.width", String.valueOf(getSize().width));
 			}
 			if ((state & Frame.MAXIMIZED_VERT) == 0) {
 				configController.getAppProperties().setProperty("frame.height", String.valueOf(getSize().height));
 			}
 		}
 
 		@Override
 		public void componentMoved(final ComponentEvent e) {
 			int state = getExtendedState();
 			if ((state & Frame.MAXIMIZED_HORIZ) == 0) {
 				configController.getAppProperties().setProperty("frame.x", String.valueOf(getX()));
 			}
 			if ((state & Frame.MAXIMIZED_VERT) == 0) {
 				configController.getAppProperties().setProperty("frame.y", String.valueOf(getY()));
 			}
 		}
 	}
 
 	private LoadProfileConfig createLoadProfileConfig() {
 		LoadProfileConfig lpc = new LoadProfileConfig();
 		lpc.setName(txtName.getText());
 		lpc.setDescription(taDescription.getText());
 		for (SelectionDecorator sd : decoratedTargets) {
 			if (sd.isSelected()) {
 				lpc.getTargets().add((Target) sd.getBaseObject());
 			}
 		}
 		for (SelectionDecorator sd : decoratedClients) {
 			if (sd.isSelected()) {
 				lpc.getClients().add((Client) sd.getBaseObject());
 			}
 		}
 		EventList<LoadProfileEntity> treeItems = loadProfilesController.getTreeItems();
 		Collections.sort(treeItems);
 		lpc.getLoadProfileEntities().addAll(treeItems);
 		return lpc;
 	}
 
 	private File showSaveDialog(final JFileChooser fc, final File file, final String extension) {
 		fc.setSelectedFile(file);
 		if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
 			File selectedFile = fc.getSelectedFile();
 			if (!FilenameUtils.getExtension(selectedFile.getName()).equalsIgnoreCase(extension)) {
 				selectedFile = new File(selectedFile.getParentFile(), selectedFile.getName() + "." + extension);
 			}
 
 			if (selectedFile.exists()) {
 				String msg = String.format("The file '%s' already exists. Overwrite?", selectedFile);
 				switch (JOptionPane.showConfirmDialog(this, msg)) {
 					case JOptionPane.YES_OPTION:
 						return selectedFile;
 					case JOptionPane.NO_OPTION:
 						return showSaveDialog(fc, file, extension);
 					default:
 						return null;
 				}
 			}
 			return selectedFile;
 		}
 		return null;
 	}
 
 	private boolean checkLoadProfileEntityDirty() {
 		if (activeLoadProfileEntityPanel != null && activeLoadProfileEntityPanel.isDirty()) {
 			switch (JOptionPane.showConfirmDialog(AppFrame.this, "Save active curve assignment?")) {
 				case JOptionPane.YES_OPTION:
 					saveLoadProfileEntity(false);
 					return true;
 				case JOptionPane.NO_OPTION:
 					cancelEditingCurveAssignment();
 					return true;
 				case JOptionPane.CANCEL_OPTION:
 				default:
 					// don't allow to change tree selection
 					tree.setSelectionPath(activeLeafPath);
 					return false;
 			}
 		}
 		return true;
 	}
 
 	private boolean checkLoadProfilePropertiesDirty() {
 		if (dirty) {
 			switch (JOptionPane.showConfirmDialog(AppFrame.this, "Save load profile changes?")) {
 				case JOptionPane.YES_OPTION:
 					saveLoadProfileConfiguration(false);
 					return true;
 				case JOptionPane.NO_OPTION:
 					return true;
 				case JOptionPane.CANCEL_OPTION:
 				default:
 					return false;
 			}
 		}
 		return true;
 	}
 
 	private void closeFrame() {
 		if (checkLoadProfilePropertiesDirty()) {
 			if (loadProfileConfigFile == null) {
 				configController.getAppProperties().remove("lpConfig.file");
 			} else {
 				configController.getAppProperties().put("lpConfig.file", loadProfileConfigFile.getAbsolutePath());
 			}
 			if (loadProfileEventsFile == null) {
 				configController.getAppProperties().remove("lpEvents.file");
 			} else {
 				configController.getAppProperties().put("lpEvents.file", loadProfileEventsFile.getAbsolutePath());
 			}
 			String activeSettingsFile = configController.getActiveSettingsFile();
 			if (activeSettingsFile != null) {
 				configController.getAppProperties().put("app.settings", activeSettingsFile);
 			}
 			configController.saveAppProperties();
 			dispose();
 		}
 	}
 }
