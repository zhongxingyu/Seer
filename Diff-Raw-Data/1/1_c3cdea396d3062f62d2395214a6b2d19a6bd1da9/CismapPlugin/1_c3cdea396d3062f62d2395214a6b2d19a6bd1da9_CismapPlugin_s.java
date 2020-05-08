 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * CismapPlugin.java
  *
  * Created on 14. Februar 2006, 12:34
  */
 package de.cismet.cismap.navigatorplugin;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.plugin.context.PluginContext;
 import Sirius.navigator.plugin.interfaces.FloatingPluginUI;
 import Sirius.navigator.plugin.interfaces.PluginMethod;
 import Sirius.navigator.plugin.interfaces.PluginProperties;
 import Sirius.navigator.plugin.interfaces.PluginSupport;
 import Sirius.navigator.plugin.interfaces.PluginUI;
 import Sirius.navigator.plugin.listener.MetaNodeSelectionListener;
 import Sirius.navigator.search.CidsSearchExecutor;
 import Sirius.navigator.search.dynamic.FormDataBean;
 import Sirius.navigator.search.dynamic.SearchProgressDialog;
 import Sirius.navigator.types.iterator.AttributeRestriction;
 import Sirius.navigator.types.iterator.ComplexAttributeRestriction;
 import Sirius.navigator.types.iterator.SingleAttributeIterator;
 import Sirius.navigator.types.treenode.DefaultMetaTreeNode;
 import Sirius.navigator.types.treenode.ObjectTreeNode;
 import Sirius.navigator.ui.ComponentRegistry;
 import Sirius.navigator.ui.tree.SearchSelectionTree;
 
 import Sirius.server.localserver.attribute.ObjectAttribute;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.middleware.types.MetaObjectNode;
 import Sirius.server.middleware.types.Node;
 import Sirius.server.search.builtin.GeoSearch;
 
 import com.jgoodies.looks.HeaderStyle;
 import com.jgoodies.looks.Options;
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import net.infonode.docking.DockingWindow;
 import net.infonode.docking.RootWindow;
 import net.infonode.docking.SplitWindow;
 import net.infonode.docking.TabWindow;
 import net.infonode.docking.View;
 import net.infonode.docking.mouse.DockingWindowActionMouseButtonListener;
 import net.infonode.docking.properties.RootWindowProperties;
 import net.infonode.docking.theme.DockingWindowsTheme;
 import net.infonode.docking.theme.ShapedGradientDockingTheme;
 import net.infonode.docking.util.DockingUtil;
 import net.infonode.docking.util.PropertiesUtil;
 import net.infonode.docking.util.StringViewMap;
 import net.infonode.gui.componentpainter.AlphaGradientComponentPainter;
 import net.infonode.util.Direction;
 
 import org.jdom.Element;
 
 import org.mortbay.jetty.Connector;
 import org.mortbay.jetty.Handler;
 import org.mortbay.jetty.HttpConnection;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.Server;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.jetty.handler.HandlerCollection;
 import org.mortbay.jetty.nio.SelectChannelConnector;
 
 import org.openide.util.Lookup;
 
 import java.applet.AppletContext;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.dnd.DropTargetDropEvent;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.InputEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import java.beans.PropertyChangeListener;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.Icon;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.RepaintManager;
 import javax.swing.SwingWorker;
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.RestrictedFileSystemView;
 import de.cismet.cismap.commons.debug.DebugPanel;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.FeatureCollectionListener;
 import de.cismet.cismap.commons.features.FeatureGroup;
 import de.cismet.cismap.commons.features.FeatureGroups;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.gui.ClipboardWaitDialog;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.ToolbarComponentDescription;
 import de.cismet.cismap.commons.gui.ToolbarComponentsProvider;
 import de.cismet.cismap.commons.gui.about.AboutDialog;
 import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
 import de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl;
 import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoWidget;
 import de.cismet.cismap.commons.gui.infowidgets.LayerInfo;
 import de.cismet.cismap.commons.gui.infowidgets.Legend;
 import de.cismet.cismap.commons.gui.infowidgets.ServerInfo;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
 
 // import de.cismet.cismap.commons.gui.overviewwidget.OverviewWidget;
 import de.cismet.cismap.commons.gui.overviewwidget.OverviewComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
 import de.cismet.cismap.commons.gui.printing.Scale;
 import de.cismet.cismap.commons.gui.statusbar.StatusBar;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.interaction.MapDnDListener;
 import de.cismet.cismap.commons.interaction.MapSearchListener;
 import de.cismet.cismap.commons.interaction.StatusListener;
 import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
 import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
 import de.cismet.cismap.commons.interaction.events.StatusEvent;
 import de.cismet.cismap.commons.interaction.memento.MementoInterface;
 import de.cismet.cismap.commons.util.DnDUtils;
 import de.cismet.cismap.commons.wfsforms.AbstractWFSForm;
 import de.cismet.cismap.commons.wfsforms.WFSFormFactory;
 
 import de.cismet.extensions.timeasy.TimEasyDialog;
 import de.cismet.extensions.timeasy.TimEasyEvent;
 import de.cismet.extensions.timeasy.TimEasyListener;
 import de.cismet.extensions.timeasy.TimEasyPureNewFeature;
 
 import de.cismet.lookupoptions.gui.OptionsClient;
 import de.cismet.lookupoptions.gui.OptionsDialog;
 
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.CurrentStackTrace;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.StaticDecimalTools;
 
 import de.cismet.tools.collections.TypeSafeCollections;
 
 import de.cismet.tools.configuration.Configurable;
 import de.cismet.tools.configuration.ConfigurationManager;
 
 import de.cismet.tools.groovysupport.GroovierConsole;
 
 import de.cismet.tools.gui.BasicGuiComponentProvider;
 import de.cismet.tools.gui.CheckThreadViolationRepaintManager;
 import de.cismet.tools.gui.CustomButtonProvider;
 import de.cismet.tools.gui.EventDispatchThreadHangMonitor;
 import de.cismet.tools.gui.JPopupMenuButton;
 import de.cismet.tools.gui.StackedBox;
 import de.cismet.tools.gui.Static2DTools;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.historybutton.HistoryModelListener;
 import de.cismet.tools.gui.historybutton.JHistoryButton;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
 public class CismapPlugin extends javax.swing.JFrame implements PluginSupport,
     Observer,
     FloatingPluginUI,
     Configurable,
     MapSearchListener,
     MapDnDListener,
     StatusListener,
     HistoryModelListener,
     FeatureCollectionListener {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String VERSION =
         "cismapPlugin.jar Version:2 ($Date: 2009-09-04 15:36:19 $(+1) $Revision: 1.1.1.1.2.1 $"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     int httpInterfacePort = 9098;
     boolean nodeSelectionEventBlocker = false;
     boolean featureCollectionEventBlocker = false;
     DataFlavor fromCapabilityWidget = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "SelectionAndCapabilities"); // NOI18N
     DataFlavor fromNavigatorNode = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="
                     + DefaultMetaTreeNode.class.getName(),
             "a DefaultMetaTreeNode");                                                                                    // NOI18N
     DataFlavor fromNavigatorCollection = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class="
                     + java.util.Collection.class.getName(),
             "a java.util.Collection of Sirius.navigator.types.treenode.DefaultMetaTreeNode objects");                    // NOI18N
     BoundingBox buffer;
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private MappingComponent mapC;
     private LayerWidget activeLayers;
     private CapabilityWidget capabilities;
     private StatusBar statusBar;
     private MetaSearch metaSearch;
     private Legend legend;
     private JScrollPane scroller;
     private StackedBox stackedBox;
     private ServerInfo serverInfo;
     private LayerInfo layerInfo;
     private FeatureInfoWidget featureInfo;
     private FeatureControl featureControl;
     private DebugPanel debugPanel;
     private GroovierConsole groovyConsole;
     private View vLayers;
     private View vCaps;
     private View vServerInfo;
     private View vLayerInfo;
     private View vMap;
     private View vLegend;
     private View vMetaSearch;
     private View vFeatureInfo;
     private View vFeatureControl;
     private View vDebug;
     private View vGroovy;
     private View vOverview;
     // private Viewport viewport;
     // private HashMap<View,DockingState> dockingStates=new HashMap<View,DockingState>();
     private RootWindow rootWindow;
     private final StringViewMap viewMap = new StringViewMap();
     private final Map<String, JMenuItem> viewMenuMap = TypeSafeCollections.newHashMap();
     private final ConfigurationManager configurationManager = new ConfigurationManager();
     private ShowObjectsMethod showObjectsMethod = new ShowObjectsMethod();
     private final Map<String, PluginMethod> pluginMethods = TypeSafeCollections.newHashMap();
     private final MyPluginProperties myPluginProperties = new MyPluginProperties();
     private final List<JMenuItem> menues = TypeSafeCollections.newArrayList();
     private final Map<DefaultMetaTreeNode, CidsFeature> featuresInMap = TypeSafeCollections.newHashMap();
     private final Map<Feature, DefaultMetaTreeNode> featuresInMapReverse = TypeSafeCollections.newHashMap();
     private String newGeometryMode = CreateNewGeometryListener.LINESTRING;
     private WFSFormFactory wfsFormFactory;
     private final Set<View> wfsFormViews = TypeSafeCollections.newHashSet();
     private final Vector<View> wfs = TypeSafeCollections.newVector();
     private DockingWindow[] wfsViews;
     private DockingWindow[] legendTab = new DockingWindow[4];
     private ClipboardWaitDialog clipboarder;
     private PluginContext context;
     private boolean plugin = false;
     // private HashMap<String,View> viewsHM=new HashMap<String,View>(8);
     private String home = System.getProperty("user.home");                                                              // NOI18N
     private String fs = System.getProperty("file.separator");                                                           // NOI18N
     private String standaloneLayoutName = "cismap.layout";                                                              // NOI18N
     private String pluginLayoutName = "plugin.layout";                                                                  // NOI18N
     private ShowObjectsWaitDialog showObjectsWaitDialog;
     private String cismapDirectory = home + fs + ".cismap";                                                             // NOI18N
     private javax.swing.ImageIcon miniBack = new javax.swing.ImageIcon(getClass().getResource("/images/miniBack.png")); // NOI18N
     private javax.swing.ImageIcon miniForward = new javax.swing.ImageIcon(getClass().getResource(
                 "/images/miniForward.png"));                                                                            // NOI18N
     private javax.swing.ImageIcon current = new javax.swing.ImageIcon(getClass().getResource("/images/current.png"));   // NOI18N
     private javax.swing.ImageIcon logo = new javax.swing.ImageIcon(getClass().getResource("/images/cismetlogo16.png")); // NOI18N
     private AppletContext appletContext;
     private boolean isInit = true;
     private String helpUrl;
     private String newsUrl;
     private AboutDialog about;
     private OverviewComponent overviewComponent = null;
     private Dimension oldWindowDimension = new Dimension(-1, -1);
     private int oldWindowPositionX = -1;
     private int oldWindowPositionY = -1;
     private String dirExtension = "";                                                                                   // NOI18N
     private Element cismapPluginUIPreferences;
     private Vector<String> windows2skip;
     private SearchProgressDialog searchProgressDialog;
     private boolean cidsPureServerSearchEnabled = false;
     private Action searchMenuSelectedAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchMenuSelectedAction"); // NOI18N
                             }
 
                             final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
                                 mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                             final PureNewFeature lastGeometry = searchListener.getLastSearchFeature();
 
                             if (lastGeometry == null) {
                                 mniSearchShowLastFeature.setIcon(null);
                                 mniSearchShowLastFeature.setEnabled(false);
                                 mniSearchRedo.setIcon(null);
                                 mniSearchRedo.setEnabled(false);
                                 mniSearchBuffer.setEnabled(false);
                             } else {
                                 switch (lastGeometry.getGeometryType()) {
                                     case ELLIPSE: {
                                         mniSearchRedo.setIcon(mniSearchEllipse.getIcon());
                                         break;
                                     }
 
                                     case LINESTRING: {
                                         mniSearchRedo.setIcon(mniSearchPolyline.getIcon());
                                         break;
                                     }
 
                                     case POLYGON: {
                                         mniSearchRedo.setIcon(mniSearchPolygon.getIcon());
                                         break;
                                     }
 
                                     case RECTANGLE: {
                                         mniSearchRedo.setIcon(mniSearchRectangle.getIcon());
                                         break;
                                     }
                                 }
 
                                 mniSearchShowLastFeature.setIcon(mniSearchRedo.getIcon());
 
                                 mniSearchRedo.setEnabled(true);
                                 mniSearchBuffer.setEnabled(true);
                                 mniSearchShowLastFeature.setEnabled(true);
                             }
 
                             // kopieren nach popupmenu im grünen M
                             mniSearchRectangle1.setSelected(mniSearchRectangle.isSelected());
                             mniSearchPolygon1.setSelected(mniSearchPolygon.isSelected());
                             mniSearchEllipse1.setSelected(mniSearchEllipse.isSelected());
                             mniSearchPolyline1.setSelected(mniSearchPolyline.isSelected());
                             mniSearchRedo1.setIcon(mniSearchRedo.getIcon());
                             mniSearchRedo1.setEnabled(mniSearchRedo.isEnabled());
                             mniSearchBuffer1.setEnabled(mniSearchBuffer.isEnabled());
                             mniSearchShowLastFeature1.setIcon(mniSearchShowLastFeature.getIcon());
                             mniSearchShowLastFeature1.setEnabled(mniSearchShowLastFeature.isEnabled());
 
 // for (int index = 0; index < menSearch.getMenuComponentCount(); index++) {
 // Component component = popMenSearch.getComponent(index);
 // if (component instanceof JRadioButtonMenuItem) {
 // ((JRadioButtonMenuItem)component).setSelected(((JRadioButtonMenuItem)menSearch.getMenuComponent(index)).isSelected());
 // } else if (component instanceof JCheckBoxMenuItem) {
 // ((JCheckBoxMenuItem)component).setSelected(((JCheckBoxMenuItem)menSearch.getMenuComponent(index)).isSelected());
 // }
 // }
 
                         }
                     });
             }
         };
 
     private Action searchAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchAction"); // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
 
                                         if (mniSearchRectangle.isSelected()) {
                                             ((CreateSearchGeometryListener)mapC.getInputListener(
                                                     MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                 CreateSearchGeometryListener.RECTANGLE);
                                         } else if (mniSearchPolygon.isSelected()) {
                                             ((CreateSearchGeometryListener)mapC.getInputListener(
                                                     MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                 CreateSearchGeometryListener.POLYGON);
                                         } else if (mniSearchEllipse.isSelected()) {
                                             ((CreateSearchGeometryListener)mapC.getInputListener(
                                                     MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                 CreateSearchGeometryListener.ELLIPSE);
                                         } else if (mniSearchPolyline.isSelected()) {
                                             ((CreateSearchGeometryListener)mapC.getInputListener(
                                                     MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                                 CreateSearchGeometryListener.LINESTRING);
                                         }
                                     }
                                 });
                         }
                     });
             }
         };
 
     private Action searchRectangleAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchRectangleAction");                                                      // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             mniSearchRectangle.setSelected(true);
                             cmdPluginSearch.setIcon(
                                 new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                         ((CreateSearchGeometryListener)mapC.getInputListener(
                                                 MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                             CreateSearchGeometryListener.RECTANGLE);
                                     }
                                 });
                         }
                     });
             }
         };
 
     private Action searchPolygonAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchPolygonAction");                                                      // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             mniSearchPolygon.setSelected(true);
                             cmdPluginSearch.setIcon(
                                 new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchPolygon.png"))); // NOI18N
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                         ((CreateSearchGeometryListener)mapC.getInputListener(
                                                 MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                             CreateSearchGeometryListener.POLYGON);
                                     }
                                 });
                         }
                     });
             }
         };
 
     private Action searchEllipseAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchEllipseAction");                                                      // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             mniSearchEllipse.setSelected(true);
                             cmdPluginSearch.setIcon(
                                 new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchEllipse.png"))); // NOI18N
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                         ((CreateSearchGeometryListener)mapC.getInputListener(
                                                 MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                             CreateSearchGeometryListener.ELLIPSE);
                                     }
                                 });
                         }
                     });
             }
         };
 
     private Action searchPolylineAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchPolylineAction");                                                      // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             mniSearchPolyline.setSelected(true);
                             cmdPluginSearch.setIcon(
                                 new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchPolyline.png"))); // NOI18N
 
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                                         ((CreateSearchGeometryListener)mapC.getInputListener(
                                                 MappingComponent.CREATE_SEARCH_POLYGON)).setMode(
                                             CreateSearchGeometryListener.LINESTRING);
                                     }
                                 });
                         }
                     });
             }
         };
 
     private Action searchRedoAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("redoSearchAction"); // NOI18N
                             }
 
                             final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
                                 mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                             searchListener.redoLastSearch();
                         }
                     });
             }
         };
 
     private Action searchShowLastFeatureAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("searchShowLastFeatureAction"); // NOI18N
                             }
 
                             final CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener)
                                 mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                             searchListener.showLastFeature();
                         }
                     });
             }
         };
 
     private Action searchBufferAction = new AbstractAction() {
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 java.awt.EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             if (log.isDebugEnabled()) {
                                 log.debug("bufferSearchGeometry"); // NOI18N
                             }
                             cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         final String s = (String)JOptionPane.showInputDialog(
                                                 null,
                                                 "Geben Sie den Abstand des zu erzeugenden\n"       // NOI18N
                                                         + "Puffers der letzten Suchgeometrie an.", // NOI18N
                                                 "Puffer",                                          // NOI18N
                                                 JOptionPane.PLAIN_MESSAGE,
                                                 null,
                                                 null,
                                                 "");                                               // NOI18N
                                         if (log.isDebugEnabled()) {
                                             log.debug(s);
                                         }
 
                                         // , statt . ebenfalls erlauben
                                         if (s.matches("\\d*,\\d*")) { // NOI18N
                                             s.replace(",", ".");      // NOI18N
                                         }
 
                                         try {
                                             final float buffer = Float.valueOf(s);
 
                                             final CreateSearchGeometryListener searchListener =
                                                 (CreateSearchGeometryListener)mapC.getInputListener(
                                                     MappingComponent.CREATE_SEARCH_POLYGON);
                                             final PureNewFeature lastFeature = searchListener.getLastSearchFeature();
 
                                             if (lastFeature != null) {
                                                 // Geometrie-Daten holen
                                                 final Geometry geom = lastFeature.getGeometry();
 
                                                 // Puffer-Geometrie holen
                                                 final Geometry bufferGeom = geom.buffer(buffer);
 
                                                 // und setzen
                                                 lastFeature.setGeometry(bufferGeom);
 
                                                 // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder
                                                 // ähnliches mehr)
                                                 lastFeature.setGeometryType(PureNewFeature.geomTypes.POLYGON);
 
                                                 for (final Object feature
                                                             : mapC.getFeatureCollection().getAllFeatures()) {
                                                     final PFeature sel = (PFeature)mapC.getPFeatureHM().get(feature);
 
                                                     if (sel.getFeature().equals(lastFeature)) {
                                                         // Koordinaten der Puffer-Geometrie als Feature-Koordinaten
                                                         // setzen
                                                         sel.setCoordArr(bufferGeom.getCoordinates());
 
                                                         // refresh
                                                         sel.syncGeometry();
 
                                                         final Vector v = new Vector();
                                                         v.add(sel.getFeature());
                                                         ((DefaultFeatureCollection)mapC.getFeatureCollection())
                                                                 .fireFeaturesChanged(v);
                                                     }
                                                 }
 
                                                 searchListener.search(lastFeature);
                                             }
 
 // for (Object feature : mapC.getFeatureCollection().getSelectedFeatures()) { PFeature sel =
 // (PFeature)mapC.getPFeatureHM().get(feature); if (sel.getFeature() instanceof SearchFeature) { // Geometrie-Daten
 // holen Geometry geom = ((SearchFeature)sel.getFeature()).getGeometry(); // Puffer-Geometrie holen Geometry bufferGeom
 // = geom.buffer(buffer);
 //
 // Koordinaten der Puffer-Geometrie als Feature-Koordinaten setzen sel.setCoordArr(bufferGeom.getCoordinates());
 //
 // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder ähnliches mehr)
 // ((PureNewFeature)sel.getFeature()).setGeometryType(PureNewFeature.geomTypes.POLYGON);
 //
 // refresh sel.syncGeometry(); Vector v = new Vector(); v.add(sel.getFeature()); ((DefaultFeatureCollection)
 // mapC.getFeatureCollection()).fireFeaturesChanged(v); } }
                                         } catch (NumberFormatException ex) {
                                             JOptionPane.showMessageDialog(
                                                 null,
                                                 "The given value was not a floating point value.!",
                                                 "Error",
                                                 JOptionPane.ERROR_MESSAGE); // NOI18N
                                         } catch (Exception ex) {
                                             if (log.isDebugEnabled()) {
                                                 log.debug("", ex);          // NOI18N
                                             }
                                         }
                                     }
                                 });
                         }
                     });
             }
         };
 
     // Variables declaration - do not modify
     private javax.swing.ButtonGroup buttonGroup1;
     private javax.swing.JButton cmdBack;
     private javax.swing.JButton cmdClipboard;
     private javax.swing.JToggleButton cmdFeatureInfo;
     private javax.swing.JButton cmdForward;
     private javax.swing.ButtonGroup cmdGroupNodes;
     private javax.swing.ButtonGroup cmdGroupPrimaryInteractionMode;
     private javax.swing.ButtonGroup cmdGroupSearch;
     private javax.swing.ButtonGroup cmdGroupSearch1;
     private javax.swing.JButton cmdHome;
     private javax.swing.JToggleButton cmdMoveGeometry;
     private javax.swing.JToggleButton cmdNewLinestring;
     private javax.swing.JToggleButton cmdNewPoint;
     private javax.swing.JToggleButton cmdNewPolygon;
     private javax.swing.JToggleButton cmdNodeAdd;
     private javax.swing.JToggleButton cmdNodeMove;
     private javax.swing.JToggleButton cmdNodeRemove;
     private javax.swing.JToggleButton cmdNodeRotateGeometry;
     private javax.swing.JToggleButton cmdPan;
     private javax.swing.JButton cmdPluginSearch;
     private javax.swing.JButton cmdPrint;
     private javax.swing.JButton cmdReconfig;
     private javax.swing.JButton cmdRedo;
     private javax.swing.JButton cmdRefresh;
     private javax.swing.JToggleButton cmdRemoveGeometry;
     private javax.swing.JToggleButton cmdSelect;
     private javax.swing.JToggleButton cmdSnap;
     private javax.swing.JButton cmdUndo;
     private javax.swing.JToggleButton cmdZoom;
     private javax.swing.JPopupMenu jPopupMenu1;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator10;
     private javax.swing.JSeparator jSeparator11;
     private javax.swing.JSeparator jSeparator12;
     private javax.swing.JSeparator jSeparator13;
     private javax.swing.JSeparator jSeparator14;
     private javax.swing.JSeparator jSeparator15;
     private javax.swing.JSeparator jSeparator16;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JSeparator jSeparator4;
     private javax.swing.JSeparator jSeparator5;
     private javax.swing.JSeparator jSeparator6;
     private javax.swing.JSeparator jSeparator7;
     private javax.swing.JSeparator jSeparator8;
     private javax.swing.JSeparator jSeparator9;
     private javax.swing.JMenu menBookmarks;
     private javax.swing.JMenu menEdit;
     private javax.swing.JMenu menExtras;
     private javax.swing.JMenu menFile;
     private javax.swing.JMenu menHelp;
     private javax.swing.JMenu menHistory;
     private javax.swing.JMenu menSearch;
     private javax.swing.JMenu menWindows;
     private javax.swing.JMenuItem mniAbout;
     private javax.swing.JMenuItem mniAddBookmark;
     private javax.swing.JMenuItem mniBack;
     private javax.swing.JMenuItem mniBookmarkManager;
     private javax.swing.JMenuItem mniBookmarkSidebar;
     private javax.swing.JMenuItem mniCapabilities;
     private javax.swing.JMenuItem mniClassTree;
     private javax.swing.JMenuItem mniClipboard;
     private javax.swing.JMenuItem mniClose;
     private javax.swing.JMenuItem mniFeatureControl;
     private javax.swing.JMenuItem mniFeatureInfo;
     private javax.swing.JMenuItem mniForward;
     private javax.swing.JMenuItem mniGeoLinkClipboard;
     private javax.swing.JMenuItem mniGotoPoint;
     private javax.swing.JMenuItem mniHistorySidebar;
     private javax.swing.JMenuItem mniHome;
     private javax.swing.JMenuItem mniLayer;
     private javax.swing.JMenuItem mniLayerInfo;
     private javax.swing.JMenuItem mniLegend;
     private javax.swing.JMenuItem mniLoadConfig;
     private javax.swing.JMenuItem mniLoadConfigFromServer;
     private javax.swing.JMenuItem mniLoadLayout;
     private javax.swing.JMenuItem mniMap;
     private javax.swing.JMenuItem mniNews;
     private javax.swing.JMenuItem mniOnlineHelp;
     private javax.swing.JMenuItem mniOptions;
     private javax.swing.JMenuItem mniOverview;
     private javax.swing.JMenuItem mniPrint;
     private javax.swing.JMenuItem mniRefresh;
     private javax.swing.JMenuItem mniRemoveAllObjects;
     private javax.swing.JMenuItem mniRemoveSelectedObject;
     private javax.swing.JMenuItem mniResetWindowLayout;
     private javax.swing.JMenuItem mniSaveConfig;
     private javax.swing.JMenuItem mniSaveLayout;
     private javax.swing.JMenuItem mniScale;
     private javax.swing.JMenuItem mniSearchBuffer;
     private javax.swing.JMenuItem mniSearchBuffer1;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline1;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle1;
     private javax.swing.JMenuItem mniSearchRedo;
     private javax.swing.JMenuItem mniSearchRedo1;
     private javax.swing.JMenuItem mniSearchShowLastFeature;
     private javax.swing.JMenuItem mniSearchShowLastFeature1;
     private javax.swing.JMenuItem mniServerInfo;
     private javax.swing.JMenuItem mniZoomToAllObjects;
     private javax.swing.JMenuItem mniZoomToSelectedObjects;
     private javax.swing.JMenuBar mnuBar;
     private javax.swing.JMenuItem mnuConfigServer;
     private javax.swing.JPanel panAll;
     private javax.swing.JPanel panMain;
     private javax.swing.JPanel panSearchSelection;
     private javax.swing.JPanel panStatus;
     private javax.swing.JPanel panToolbar;
     private javax.swing.JPopupMenu popMen;
     private javax.swing.JPopupMenu popMenSearch;
     private javax.swing.JSeparator sepAfterPos;
     private javax.swing.JSeparator sepBeforePos;
     private javax.swing.JSeparator sepServerProfilesEnd;
     private javax.swing.JSeparator sepServerProfilesStart;
     private javax.swing.JToolBar tlbMain;
     private javax.swing.JToggleButton togInvisible;
     // End of variables declaration
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new CismapPlugin object.
      */
     public CismapPlugin() {
         this(null);
     }
 
     /**
      * Creates a new CismapPlugin object.
      *
      * @param  context  DOCUMENT ME!
      */
     public CismapPlugin(final PluginContext context) {
         if (StaticDebuggingTools.checkHomeForFile("cismetCheckForEDThreadVialoation")) { // NOI18N
             RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
         }
 
         try {
             final String l = System.getProperty("user.language");                  // NOI18N
             final String c = System.getProperty("user.country");                   // NOI18N
             System.out.println("Locale=" + l + "_" + c);                           // NOI18N
             Locale.setDefault(new Locale(l, c));
         } catch (Exception e) {
             log.warn("Error while changing the user language and country");        // NOI18N
         }
         if (StaticDebuggingTools.checkHomeForFile("cidsNewServerSearchEnabled")) { // NOI18N
             cidsPureServerSearchEnabled = true;
         }
         try {
             final String ext = System.getProperty("directory.extension");          // NOI18N
 
             System.out.println("SystemdirExtension=:" + ext); // NOI18N
 
             if (ext != null) {
                 dirExtension = ext;
                 cismapDirectory += ext;
             }
         } catch (Exception e) {
             log.warn("Error while adding DirectoryExtension"); // NOI18N
         }
 
         CismapBroker.getInstance().setCismapFolderPath(cismapDirectory);
 
         this.setIconImage(logo.getImage());
         System.setSecurityManager(null);
 
         // this.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/images/cismap.png")).getImage());
         this.context = context;
         plugin = (context != null);
 
         try {
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             0,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).initializingCismapPlugin")); // NOI18N
             }
 
             if (!plugin) {
                 try {
                     org.apache.log4j.PropertyConfigurator.configure(getClass().getResource("/cismap.log4j.properties")); // NOI18N
 
                     if (StaticDebuggingTools.checkHomeForFile("cismetDebuggingInitEventDispatchThreadHangMonitor")) { // NOI18N
                         EventDispatchThreadHangMonitor.initMonitoring();
                     }
 
                     if (StaticDebuggingTools.checkHomeForFile("cismetCheckForEDThreadVialoation")) { // NOI18N
                         RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
                     }
                 } catch (Exception e) {
                     System.err.println("LOG4J is not configured propperly\n\n");                     // NOI18N
                     e.printStackTrace();
                 }
             }
 
             try {
                 // javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
                 javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new NimbusLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
                 // javax.swing.UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
                 // UIManager.setLookAndFeel(new PlasticLookAndFeel()); javax.swing.UIManager.setLookAndFeel(new
                 // PlasticXPLookAndFeel());
             } catch (Exception e) {
                 log.warn("Error while creating Look&Feel's!", e); // NOI18N
             }
 
             clipboarder = new ClipboardWaitDialog(StaticSwingTools.getParentFrame(this), true);
             showObjectsWaitDialog = new ShowObjectsWaitDialog(StaticSwingTools.getParentFrame(this), false);
 
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             100,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).createWidgets")); // NOI18N
             }
 
             // Erzeugen der Widgets
             serverInfo = new ServerInfo();
             layerInfo = new LayerInfo();
             mapC = new MappingComponent();
             mapC.addHistoryModelListener(this);
             activeLayers = new LayerWidget(mapC);
             activeLayers.setPreferredSize(new Dimension(100, 120));
             legend = new Legend();
             metaSearch = new MetaSearch();
             statusBar = new StatusBar(mapC);
             featureInfo = new FeatureInfoWidget();
             capabilities = new CapabilityWidget();
             featureControl = new FeatureControl(mapC);
             debugPanel = new DebugPanel();
             debugPanel.setPCanvas(mapC);
             groovyConsole = new GroovierConsole();
             groovyConsole.setVariable("map", mapC); // NOI18N
             wfsFormFactory = WFSFormFactory.getInstance(mapC);
             overviewComponent = new OverviewComponent();
             overviewComponent.setMasterMap(mapC);
 
 // KeyStroke upsideDownStroke = KeyStroke.getKeyStroke('Y',InputEvent.CTRL_MASK);
 // Action upsidedownAction = new AbstractAction(){
 // public void actionPerformed(ActionEvent e) {
 // java.awt.EventQueue.invokeLater(new Runnable() {
 // public void run() {
 // mapC.checkAndFixErroneousTransformation();
 // }
 // });
 // }
 // };
 // getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(upsideDownStroke, "UPSIDEDOWN");
 // getRootPane().getActionMap().put("UPSIDEDOWN", upsidedownAction);
 
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             200,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).initializingGUI")); // NOI18N
             }
 
             try {
                 initComponents();
             } catch (Exception e) {
                 log.fatal("Error in initComponents.", e); // NOI18N
             }
 
             if (!plugin) {
                 menSearch.setVisible(false);
                 cmdPluginSearch.setVisible(false);
 
                 final KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke(
                         'L',
                         InputEvent.CTRL_MASK
                                 + InputEvent.SHIFT_MASK);
                 final Action configAction = new AbstractAction() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             java.awt.EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         Log4JQuickConfig.getSingletonInstance().setVisible(true);
                                     }
                                 });
                         }
                     };
                 getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                         .put(configLoggerKeyStroke,
                             "CONFIGLOGGING");                                    // NOI18N
                 getRootPane().getActionMap().put("CONFIGLOGGING", configAction); // NOI18N
             }
 
             // Menu
             menues.add(menFile);
             menues.add(menEdit);
             menues.add(menHistory);
             menues.add(menSearch);
             menues.add(menBookmarks);
             menues.add(menExtras);
             menues.add(menWindows);
             menues.add(menHelp);
 
             panStatus.add(statusBar, BorderLayout.CENTER);
 
             tlbMain.putClientProperty("JToolBar.isRollover", Boolean.TRUE); // NOI18N
             tlbMain.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
             tlbMain.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
 
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             300,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).connectingWidgets")); // NOI18N
             }
 
             // Wire the components
             // add Listeners
             ((JHistoryButton)cmdForward).setDirection(JHistoryButton.DIRECTION_FORWARD);
             ((JHistoryButton)cmdBack).setDirection(JHistoryButton.DIRECTION_BACKWARD);
             ((JHistoryButton)cmdForward).setHistoryModel(mapC);
             ((JHistoryButton)cmdBack).setHistoryModel(mapC);
 
             CismapBroker.getInstance().addCapabilityListener(serverInfo);
             CismapBroker.getInstance().addCapabilityListener(layerInfo);
             CismapBroker.getInstance().addActiveLayerListener(serverInfo);
             CismapBroker.getInstance().addActiveLayerListener(layerInfo);
             CismapBroker.getInstance().addActiveLayerListener(legend);
             CismapBroker.getInstance().addActiveLayerListener(featureInfo);
             CismapBroker.getInstance().addStatusListener(statusBar);
             CismapBroker.getInstance().addMapClickListener(featureInfo);
             CismapBroker.getInstance().addMapSearchListener(this);
             CismapBroker.getInstance().addMapDnDListener(this);
             CismapBroker.getInstance().addStatusListener(this);
             mapC.getFeatureCollection().addFeatureCollectionListener(featureControl);
             mapC.getFeatureCollection().addFeatureCollectionListener(statusBar);
             CismapBroker.getInstance().addMapBoundsListener(featureControl);
             CismapBroker.getInstance().addMapBoundsListener(capabilities);
 
             // activeLayers.getMappingModel().addMappingModelListener(legend);
 
             // set the components in the broker
             CismapBroker.getInstance().setMappingComponent(mapC);
 // broker.setLayerWidget(activeLayers);
 
             String cismapconfig = null;
             String fallBackConfig = null;
 
             try {
                 final String prefix = "cismapconfig:"; // NOI18N
                 final String username = Sirius.navigator.connection.SessionManager.getSession().getUser().getName();
                 final String groupname = Sirius.navigator.connection.SessionManager.getSession()
                             .getUser()
                             .getUserGroup()
                             .getName();
                 final String domainname = Sirius.navigator.connection.SessionManager.getSession()
                             .getUser()
                             .getUserGroup()
                             .getDomain();
 
                 // First try: cismapconfig:username@usergroup@domainserver
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment()
                                 .getParameter(prefix + username + "@" + groupname + "@"
                                         + domainname); // NOI18N
                 }
 
                 // Second try: cismapconfig:*@usergroup@domainserver
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment()
                                 .getParameter(prefix + "*" + "@" + groupname + "@"
                                         + domainname); // NOI18N
                 }
 
                 // Third try: cismapconfig:*@*@domainserver//NOI18N
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + "*" + "@" + "*" + "@" + domainname); // NOI18N
                 }
 
                 // Default from pluginXML
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + "default"); // NOI18N
                 }
 
                 fallBackConfig = context.getEnvironment().getParameter(prefix + "default"); // NOI18N
             } catch (Throwable t) {
                 log.info("cismap started standalone", t);                                   // NOI18N
             }
 
             // Default
             if (cismapconfig == null) {
                 cismapconfig = "defaultCismapProperties.xml"; // NOI18N
             }
 
             if (fallBackConfig == null) {
                 fallBackConfig = "defaultCismapProperties.xml"; // NOI18N
             }
 
             log.info("ServerConfigFile=" + cismapconfig); // NOI18N
             configurationManager.setDefaultFileName(cismapconfig);
             configurationManager.setFallBackFileName(fallBackConfig);
 
             if (!plugin) {
                 configurationManager.setFileName("configuration.xml");       // NOI18N
             } else {
                 configurationManager.setFileName("configurationPlugin.xml"); // NOI18N
                 configurationManager.addConfigurable(metaSearch);
             }
 
             configurationManager.setClassPathFolder("/");             // NOI18N
             configurationManager.setFolder(".cismap" + dirExtension); // NOI18N
             configurationManager.addConfigurable(this);
 
             configurationManager.addConfigurable(capabilities);
             configurationManager.addConfigurable(wfsFormFactory);
             configurationManager.addConfigurable(mapC);
             configurationManager.addConfigurable(activeLayers);
             configurationManager.addConfigurable(featureControl);
             configurationManager.addConfigurable(overviewComponent);
             configurationManager.addConfigurable(OptionsClient.getInstance());
 
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             400,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).initializingDockingsystem")); // NOI18N
             }
 
             // Flexdock stuff
             //// DockingManager.setFloatingEnabled(false);
             final Icon icoLayers = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/commons/raster/wms/res/layers.png"));                                     // NOI18N
             final Icon icoServer = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/commons/raster/wms/res/server.png"));                                     // NOI18N
             final Icon icoServerInfo = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/commons/gui/capabilitywidget/res/serverInfo.png"));                       // NOI18N
             final Icon icoLayerInfo = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/commons/gui/capabilitywidget/res/layerInfo.png"));                        // NOI18N
             final Icon icoFeatureInfo = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/commons/gui/featureinfowidget/res/featureInfo16.png"));                   // NOI18N
             final Icon icoLegend = new javax.swing.ImageIcon(getClass().getResource(
                         "/de/cismet/cismap/navigatorplugin/res/legend.png"));                                        // NOI18N
             final Icon icoClassSelection = new javax.swing.ImageIcon(getClass().getResource(
                         "/images/classSelection.png"));                                                              // NOI18N
             final Icon icoMap = new javax.swing.ImageIcon(getClass().getResource("/images/map.png"));                // NOI18N
             final Icon icoFeatureControl = new javax.swing.ImageIcon(getClass().getResource("/images/objects.png")); // NOI18N
 
             // -------------------------InfoNode initialization-------------------------------------------//
             rootWindow = DockingUtil.createRootWindow(viewMap, true);
 
             vMap = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vMap.title"), // NOI18N
                     Static2DTools.borderIcon(icoMap, 0, 3, 0, 1),
                     mapC);
             viewMap.addView("map", vMap); // NOI18N
             viewMenuMap.put("map", mniMap); // NOI18N
 
             vLayers = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vLayer.title"), // NOI18N
                     Static2DTools.borderIcon(icoLayers, 0, 3, 0, 1),
                     activeLayers);
             viewMap.addView("activeLayers", vLayers); // NOI18N
             viewMenuMap.put("activeLayers", mniLayer); // NOI18N
 
             vCaps = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vCapabilities.title"), // NOI18N
                     Static2DTools.borderIcon(icoServer, 0, 3, 0, 1),
                     capabilities);
             viewMap.addView("capabilities", vCaps); // NOI18N
             viewMenuMap.put("capabilities", mniCapabilities); // NOI18N
 
             vServerInfo = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vServerInfo.title"), // NOI18N
                     Static2DTools.borderIcon(icoServerInfo, 0, 3, 0, 1),
                     serverInfo);
             viewMap.addView("serverinfo", vServerInfo); // NOI18N
             viewMenuMap.put("serverinfo", mniServerInfo); // NOI18N
 
             vOverview = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vOverview.title"), // NOI18N
                     Static2DTools.borderIcon(icoMap, 0, 3, 0, 1),
                     overviewComponent);
             viewMap.addView("overview", vOverview); // NOI18N
             viewMenuMap.put("overview", mniOverview); // NOI18N
             legendTab[2] = vOverview;
 
             vLayerInfo = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vLayerInfo.title"), // NOI18N
                     Static2DTools.borderIcon(icoLayerInfo, 0, 3, 0, 1),
                     layerInfo);
             viewMap.addView("layerinfo", vLayerInfo); // NOI18N
             viewMenuMap.put("layerinfo", mniLayerInfo); // NOI18N
             legendTab[3] = vLayerInfo;
 
             vLegend = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vLegende.title"), // NOI18N
                     Static2DTools.borderIcon(icoLegend, 0, 3, 0, 1),
                     legend);
             viewMap.addView("legend", vLegend); // NOI18N
             viewMenuMap.put("legend", mniLegend); // NOI18N
 
             if (plugin) {
                 vMetaSearch = new View(org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.CismapPlugin(PluginContext).vMetaSearch.title"), // NOI18N
                         Static2DTools.borderIcon(icoClassSelection, 0, 3, 0, 1),
                         metaSearch);
                 viewMap.addView("metaSearch", vMetaSearch); // NOI18N
                 legendTab[1] = vMetaSearch;
             } else {
                 legendTab[1] = vLegend;
             }
 
             mniClassTree.setVisible(plugin);
             viewMenuMap.put("metaSearch", mniClassTree);                                // NOI18N
             vFeatureInfo = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vFeatureInfo.title"), // NOI18N
                     Static2DTools.borderIcon(icoFeatureInfo, 0, 3, 0, 1),
                     featureInfo);
             viewMap.addView("featureInfo", vFeatureInfo);                               // NOI18N
             viewMenuMap.put("featureInfo", mniFeatureInfo);                             // NOI18N
 
             vFeatureControl = new View(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).vFeatureControl.title"), // NOI18N
                     Static2DTools.borderIcon(icoFeatureControl, 0, 3, 0, 1),
                     featureControl);
             viewMap.addView("featureControl", vFeatureControl); // NOI18N
             viewMenuMap.put("featureControl", mniFeatureControl); // NOI18N
 
             // vDebug=createView("debug","DebugPanel",debugPanel);
             // vGroovy=createView("groovy","Groovy Console",groovyConsole);
 
             configurationManager.configure(wfsFormFactory);
 
             // WFSForms
             final Set<String> keySet = wfsFormFactory.getForms().keySet();
             final JMenu wfsFormsMenu = new JMenu(org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.CismapPlugin(PluginContext).wfsFormMenu.title")); // NOI18N
 
             for (final String key : keySet) {
                 // View
                 final AbstractWFSForm form = wfsFormFactory.getForms().get(key);
                 form.setMappingComponent(mapC);
                 if (log.isDebugEnabled()) {
                     log.debug("WFSForms: key,form" + key + "," + form); // NOI18N
                 }
 
                 final View formView = new View(form.getTitle(),
                         Static2DTools.borderIcon(form.getIcon(), 0, 3, 0, 1),
                         form);
                 if (log.isDebugEnabled()) {
                     log.debug("WFSForms: formView" + formView); // NOI18N
                 }
                 viewMap.addView(form.getId(), formView);
                 wfsFormViews.add(formView);
                 wfs.add(formView);
 
                 // Menu
                 final JMenuItem menuItem = new JMenuItem(form.getMenuString());
                 menuItem.setIcon(form.getIcon());
                 menuItem.addActionListener(new ActionListener() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             if (log.isDebugEnabled()) {
                                 log.debug("showOrHideView:" + formView); // NOI18N
                             }
                             showOrHideView(formView);
                         }
                     });
                 wfsFormsMenu.add(menuItem);
             }
 
             wfsViews = new DockingWindow[wfsFormViews.size()];
 
             for (int i = 0; i < wfsViews.length; i++) {
                 wfsViews[i] = wfs.get(i);
             }
 
             if (keySet.size() > 0) {
                 menues.remove(menHelp);
                 menues.add(wfsFormsMenu);
                 menues.add(menHelp);
 
                 mnuBar.remove(menHelp);
                 mnuBar.add(wfsFormsMenu);
                 mnuBar.add(menHelp);
             }
 
             // Cismap Extensions
             final Collection<? extends BasicGuiComponentProvider> guiCompProviders = Lookup.getDefault()
                         .lookupAll(
                             BasicGuiComponentProvider.class);
 
             if (guiCompProviders != null) {
                 for (final BasicGuiComponentProvider gcp : guiCompProviders) {
                     if (gcp.getType() == BasicGuiComponentProvider.GuiType.GUICOMPONENT) {
                         gcp.setLinkObject(this);
                         if (log.isDebugEnabled()) {
                             log.debug(gcp.getName() + " (try to add)");
                         }
                         Icon icon = null;
                         try {
                             icon = Static2DTools.borderIcon(gcp.getIcon(), 0, 3, 0, 1);
                         } catch (Exception skip) {
                         }
                         final View extensionView = new View(gcp.getName(), icon,
                                 gcp.getComponent());
                         viewMap.addView(gcp.getId(), extensionView);
                         if (log.isDebugEnabled()) {
                             log.debug(gcp.getName() + " added");
                         }
 
                         if (gcp instanceof CustomButtonProvider) {
                             extensionView.getCustomTitleBarComponents()
                                     .addAll(((CustomButtonProvider)gcp).getCustomButtons());
                         }
                     }
                 }
             }
 
             legendTab[0] = vLegend;
 
             rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
 
             final DockingWindowsTheme theme = new ShapedGradientDockingTheme();
             rootWindow.getRootWindowProperties().addSuperObject(
                 theme.getRootWindowProperties());
 
             final RootWindowProperties titleBarStyleProperties = PropertiesUtil
                         .createTitleBarStyleRootWindowProperties();
 
             rootWindow.getRootWindowProperties().addSuperObject(
                 titleBarStyleProperties);
 
             rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
 
             final AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(
                     java.awt.SystemColor.inactiveCaptionText,
                     java.awt.SystemColor.activeCaptionText,
                     java.awt.SystemColor.activeCaptionText,
                     java.awt.SystemColor.inactiveCaptionText);
             vMap.getViewProperties()
                     .getViewTitleBarProperties()
                     .getNormalProperties()
                     .getCloseButtonProperties()
                     .setVisible(true);
             rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);
 
             if (!EventQueue.isDispatchThread()) {
                 EventQueue.invokeAndWait(new Runnable() {
 
                         @Override
                         public void run() {
                             if (plugin) {
                                 // DockingManager.setDefaultPersistenceKey("pluginPerspectives.xml");
                                 loadLayout(cismapDirectory + fs + pluginLayoutName);
                             } else {
                                 // DockingManager.setDefaultPersistenceKey("cismapPerspectives.xml");
                                 loadLayout(cismapDirectory + fs + standaloneLayoutName);
                             }
                         }
                     });
             } else {
                 if (plugin) {
                     // DockingManager.setDefaultPersistenceKey("pluginPerspectives.xml");
                     loadLayout(cismapDirectory + fs + pluginLayoutName);
                 } else {
                     // DockingManager.setDefaultPersistenceKey("cismapPerspectives.xml");
                     loadLayout(cismapDirectory + fs + standaloneLayoutName);
                 }
             }
 
             if (plugin && (context.getEnvironment() != null) && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment()
                         .getProgressObserver()
                         .setProgress(
                             500,
                             org.openide.util.NbBundle.getMessage(
                                 CismapPlugin.class,
                                 "CismapPlugin.CismapPlugin(PluginContext).loadPreferences")); // NOI18N
             }
         } catch (Exception ex) {
             log.fatal("Error in Constructor of CismapPlugin", ex);                            // NOI18N
             System.err.println("Error in Constructor of CismapPlugin");                       // NOI18N
             ex.printStackTrace();
         }
 
         // Damit mehrere Geometrien angelegt werden koennen
         mapC.setReadOnly(false);
 
         final Object blocker = new Object();
 
         if (plugin) {
             try {
                 try {
                     synchronized (blocker) {
                         if ((context != null) && (context.getEnvironment() != null)
                                     && (context.getEnvironment().getProgressObserver() != null)
                                     && this.context.getEnvironment().isProgressObservable()) {
                             this.context.getEnvironment()
                                     .getProgressObserver()
                                     .setProgress(
                                         500,
                                         org.openide.util.NbBundle.getMessage(
                                             CismapPlugin.class,
                                             "CismapPlugin.CismapPlugin(PluginContext).loadMethods")); // NOI18N
                         }
                     }
                 } catch (Exception e) {
                     log.warn("No progress report available", e);                                      // NOI18N
                 }
 
                 mniClose.setVisible(false);
                 pluginMethods.put(showObjectsMethod.getId(), showObjectsMethod);
                 appletContext = context.getEnvironment().getAppletContext();
 
                 if ((context != null) && (context.getEnvironment() != null)
                             && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment()
                             .getProgressObserver()
                             .setProgress(
                                 600,
                                 org.openide.util.NbBundle.getMessage(
                                     CismapPlugin.class,
                                     "CismapPlugin.CismapPlugin(PluginContext).searchTree")); // NOI18N
                 }
 
                 this.context.getMetadata().addMetaNodeSelectionListener(new NodeChangeListener());
 
                 final Node[] classNodes = Sirius.navigator.connection.SessionManager.getProxy().getClassTreeNodes();
                 final SearchSelectionTree sst = new SearchSelectionTree(classNodes);
                 sst.addMouseListener(new MouseAdapter() {
 
                         @Override
                         public void mouseClicked(final MouseEvent e) {
                             if (e.getClickCount() == 1) {
                                 final TreePath path = sst.getPathForLocation(e.getX(), e.getY());
 
                                 if (path != null) {
                                     final DefaultMetaTreeNode node = (DefaultMetaTreeNode)path.getLastPathComponent();
                                     node.selectSubtree(!node.isSelected());
 
                                     // logger.debug("setting search forms enabled");
                                     final Collection userGroups = new LinkedList();
                                     userGroups.add(SessionManager.getSession().getUser().getUserGroup().getKey());
                                     sst.repaint();
                                 }
                             }
                         }
                     });
                 metaSearch.setSearchTree(sst);
 
                 if ((context != null) && (context.getEnvironment() != null)
                             && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment()
                             .getProgressObserver()
                             .setProgress(
                                 700,
                                 org.openide.util.NbBundle.getMessage(
                                     CismapPlugin.class,
                                     "CismapPlugin.CismapPlugin(PluginContext).loadConfiguration")); // NOI18N
                 }
 
 // configureApp(false);
                 if ((context != null) && (context.getEnvironment() != null)
                             && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment()
                             .getProgressObserver()
                             .setProgress(
                                 1000,
                                 org.openide.util.NbBundle.getMessage(
                                     CismapPlugin.class,
                                     "CismapPlugin.CismapPlugin(PluginContext).cismapPluginReady")); // NOI18N
                 }
 
                 if ((context != null) && (context.getEnvironment() != null)
                             && context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setFinished(true);
                 }
             } catch (Throwable t) {
                 context.getLogger().fatal("Error in CismapPlugin constructor", t); // NOI18N
             }
 
             // TimEasy
             ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setGeometryFeatureClass(
                 TimEasyPureNewFeature.class);
             TimEasyDialog.addTimTimEasyListener(new TimEasyListener() {
 
                     @Override
                     public void timEasyObjectInserted(final TimEasyEvent tee) {
                         mapC.getFeatureCollection().removeFeature(tee.getPureNewfeature());
                         mapC.getFeatureCollection().addFeature(new CidsFeature(tee.getMetaObjectNode()));
                     }
                 });
         }
 
         log.info("add InfoNode main component to the panMain Panel"); // NOI18N
         panMain.add(rootWindow, BorderLayout.CENTER);
 
         vMap.doLayout();
         mapC.setMappingModel(activeLayers.getMappingModel());
         setVisible(true);
 
         // validateTree();
         configureApp(false);
 
         // configureActiveTabAfterVisibility();
         isInit = false;
 
         for (final Scale s : mapC.getScales()) {
             if (s.getDenominator() > 0) {
                 menExtras.add(getScaleMenuItem(s.getText(), s.getDenominator()));
             }
         }
 
         statusBar.addScalePopups();
         statusBar.addCrsPopups();
         cmdReconfig.setVisible(false);
         jSeparator1.setVisible(false);
         mapC.getFeatureCollection().addFeatureCollectionListener(this);
         repaint();
 
         if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) { // NOI18N
             initHttpServer();
         }
         if (log.isDebugEnabled()) {
 // addComponentListener(new ComponentListener() {
 // public void componentHidden(ComponentEvent e) {
 // }
 // public void componentMoved(ComponentEvent e) {
 // }
 // public void componentResized(ComponentEvent e) {
 // log.fatal("component resized()");
 // mapC.rescaleStickyNodes();
 // }
 // public void componentShown(ComponentEvent e) {
 // }
 // });
             log.debug("CismapPlugin als Observer anmelden"); // NOI18N
         }
         ((Observable)mapC.getMemUndo()).addObserver(CismapPlugin.this);
         ((Observable)mapC.getMemRedo()).addObserver(CismapPlugin.this);
         mapC.unlock();
         overviewComponent.getOverviewMap().unlock();
         layerInfo.initDividerLocation();
         try {
             initPluginToolbarComponents();
         } catch (Error err) {
             log.error("Exception while initializing Toolbar!", err);
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void initPluginToolbarComponents() {
         final Collection<? extends ToolbarComponentsProvider> toolbarCompProviders = Lookup.getDefault()
                     .lookupAll(
                         ToolbarComponentsProvider.class);
 
         if (toolbarCompProviders != null) {
             for (final ToolbarComponentsProvider toolbarCompProvider : toolbarCompProviders) {
                 if (log.isDebugEnabled()) {
                     log.debug("Registering Toolbar Components for Plugin: " + toolbarCompProvider.getPluginName()); // NOI18N
                 }
 
                 final Collection<ToolbarComponentDescription> componentDescriptions =
                     toolbarCompProvider.getToolbarComponents();
 
                 if (componentDescriptions != null) {
                     for (final ToolbarComponentDescription componentDescription : componentDescriptions) {
                         int insertionIndex = tlbMain.getComponentCount();
                         final String anchor = componentDescription.getAnchorComponentName();
 
                         if (anchor != null) {
                             for (int i = tlbMain.getComponentCount(); --i >= 0;) {
                                 final Component currentAnchorCandidate = tlbMain.getComponent(i);
 
                                 if (anchor.equals(currentAnchorCandidate.getName())) {
                                     if (ToolbarComponentsProvider.ToolbarPositionHint.BEFORE.equals(
                                                     componentDescription.getPositionHint())) {
                                         insertionIndex = i;
                                     } else {
                                         insertionIndex = i + 1;
                                     }
 
                                     break;
                                 }
                             }
                         }
 
                         tlbMain.add(componentDescription.getComponent(), insertionIndex);
                     }
                 }
             }
         }
         final Collection<? extends BasicGuiComponentProvider> toolbarguiCompProviders = Lookup.getDefault()
                     .lookupAll(BasicGuiComponentProvider.class);
         if (toolbarguiCompProviders != null) {
             for (final BasicGuiComponentProvider gui : toolbarguiCompProviders) {
                 if (gui.getType() == BasicGuiComponentProvider.GuiType.TOOLBARCOMPONENT) {
                     final int insertionIndex = tlbMain.getComponentCount();
 //                    int position = insertionIndex;
 //                    log.fatal("tryToAdd1"+gui.getId());
 //                    try {
 //                        position = (Integer) gui.getPositionHint();
 //                    } catch (Exception skip) {
 //                    }
 //
 //                    log.fatal("tryToAdd2"+gui.getId());
                     tlbMain.add(gui.getComponent(), insertionIndex);
                 }
             }
         }
     }
 
     /**
      * private JMenuItem copyMenuItem(JMenuItem from) { JMenuItem to; if (from instanceof JMenuItem) { to = new
      * JMenuItem(); } else if (from instanceof JCheckBoxMenuItem) { to = new JCheckBoxMenuItem(); } else if (from
      * instanceof JRadioButtonMenuItem) { to = new JRadioButtonMenuItem(); } else { return null; }
      * to.setText(from.getText()); to.setToolTipText(from.getToolTipText()); to.setIcon(from.getIcon());
      * to.setAction(from.getAction()); to.setAccelerator(from.getAccelerator()); return to; } private void
      * copyMenu(JMenu from, JPopupMenu to) { for (Component fromComponent : from.getMenuComponents()) { try {
      * to.add(copyMenuItem((JMenuItem)fromComponent)); } catch (ClassCastException ex) { if (fromComponent instanceof
      * JSeparator) { to.addSeparator(); } } } to.pack(); }
      *
      * @param   t  DOCUMENT ME!
      * @param   d  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private JMenuItem getScaleMenuItem(final String t, final int d) {
         final JMenuItem jmi = new JMenuItem(t);
         jmi.addActionListener(new ActionListener() {
 
                 @Override
                 public void actionPerformed(final ActionEvent e) {
                     mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(d));
                 }
             });
 
         return jmi;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public MappingComponent getMappingComponent() {
         return mapC;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void setupDefaultLayout() {
         if (wfsViews.length != 0) {
             rootWindow.setWindow(new SplitWindow(
                     true,
                     0.716448f,
                     new SplitWindow(
                         false,
                         0.72572404f,
                         new SplitWindow(false, 0.21391752f,
                             new TabWindow(wfsViews),
                             vMap),
                         new TabWindow(
                             new DockingWindow[] {
                                 vLayers,
                                 vFeatureControl,
                                 vFeatureInfo
                             })),
                     new SplitWindow(
                         false,
                         0.66f,
                         new TabWindow(
                             new DockingWindow[] {
                                 vCaps,
                                 vServerInfo
                             }),
                         new TabWindow(legendTab))));
         } else {
             rootWindow.setWindow(new SplitWindow(
                     true,
                     0.716448f,
                     new SplitWindow(
                         false,
                         0.72572404f,
                         // auf Verdacht ge�ndert (von Sebastian Puhl) ;-)
                         new TabWindow(vMap),
                         new TabWindow(
                             new DockingWindow[] {
                                 vLayers,
                                 vFeatureControl,
                                 vFeatureInfo
                             })),
                     new SplitWindow(
                         false,
                         0.66f,
                         new TabWindow(
                             new DockingWindow[] {
                                 vCaps,
                                 vServerInfo
                             }),
                         new TabWindow(legendTab))));
         }
 
         for (int i = 0; i
                     < wfsViews.length; i++) {
             wfsViews[i].close();
         }
 
         rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
         rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 
         vLegend.restoreFocus();
         vCaps.restoreFocus();
         vLayers.restoreFocus();
         vMap.restoreFocus();
 
         if (windows2skip != null) {
             for (final String id : windows2skip) {
                 final View v = viewMap.getView(id);
 
                 if (v != null) {
                     v.close();
                 }
 
                 final JMenuItem menu = viewMenuMap.get(id);
 
                 if (menu != null) {
                     menu.setVisible(false);
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  url  DOCUMENT ME!
      */
     private void openUrlInExternalBrowser(final String url) {
         try {
             if (appletContext == null) {
                 de.cismet.tools.BrowserLauncher.openURL(url);
             } else {
                 final java.net.URL u = new java.net.URL(url);
                 appletContext.showDocument(u, "cismetBrowser");         // NOI18N
             }
         } catch (Exception e) {
             log.warn("Error while opening: " + url + ". Try again", e); // NOI18N
 
             // Nochmal zur Sicherheit mit dem BrowserLauncher probieren
 
             try {
                 de.cismet.tools.BrowserLauncher.openURL(url);
             } catch (Exception e2) {
                 log.warn("The second time failed, too. Error while trying to open: " + url + " last attempt", e2); // NOI18N
 
                 try {
                     de.cismet.tools.BrowserLauncher.openURL("file://" + url); // NOI18N
                 } catch (Exception e3) {
                     log.error("3rd time fail:file://" + url, e3);             // NOI18N
                 }
             }
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">
     private void initComponents() {
         cmdGroupPrimaryInteractionMode = new javax.swing.ButtonGroup();
         panSearchSelection = new javax.swing.JPanel();
         popMen = new javax.swing.JPopupMenu();
         mnuConfigServer = new javax.swing.JMenuItem();
         cmdGroupNodes = new javax.swing.ButtonGroup();
         buttonGroup1 = new javax.swing.ButtonGroup();
         jPopupMenu1 = new javax.swing.JPopupMenu();
         menBookmarks = new javax.swing.JMenu();
         mniAddBookmark = new javax.swing.JMenuItem();
         mniBookmarkManager = new javax.swing.JMenuItem();
         mniBookmarkSidebar = new javax.swing.JMenuItem();
         popMenSearch = new javax.swing.JPopupMenu();
         mniSearchRectangle1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolygon1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchEllipse1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolyline1 = new javax.swing.JRadioButtonMenuItem();
         jSeparator12 = new javax.swing.JSeparator();
         mniSearchShowLastFeature1 = new javax.swing.JMenuItem();
         mniSearchRedo1 = new javax.swing.JMenuItem();
         mniSearchBuffer1 = new javax.swing.JMenuItem();
         cmdGroupSearch = new javax.swing.ButtonGroup();
         cmdGroupSearch1 = new javax.swing.ButtonGroup();
         panAll = new javax.swing.JPanel();
         panToolbar = new javax.swing.JPanel();
         panMain = new javax.swing.JPanel();
         tlbMain = new javax.swing.JToolBar();
         cmdReconfig = new JPopupMenuButton();
         ((JPopupMenuButton)cmdReconfig).setPopupMenu(popMen);
         jSeparator1 = new javax.swing.JSeparator();
         cmdBack = new JHistoryButton() {
 
                 @Override
                 public void historyActionPerformed() {
                     if (mapC != null) {
                         mapC.back(true);
                     }
                 }
             };
         cmdHome = new javax.swing.JButton();
         cmdForward = new JHistoryButton() {
 
                 @Override
                 public void historyActionPerformed() {
                     if (mapC != null) {
                         mapC.forward(true);
                     }
                 }
             };
         ;
         jSeparator2 = new javax.swing.JSeparator();
         cmdRefresh = new javax.swing.JButton();
         jSeparator6 = new javax.swing.JSeparator();
         cmdPrint = new javax.swing.JButton();
         cmdClipboard = new javax.swing.JButton();
         jSeparator4 = new javax.swing.JSeparator();
         togInvisible = new javax.swing.JToggleButton();
         togInvisible.setVisible(false);
         cmdSelect = new javax.swing.JToggleButton();
         cmdZoom = new javax.swing.JToggleButton();
         cmdPan = new javax.swing.JToggleButton();
         cmdFeatureInfo = new javax.swing.JToggleButton();
         cmdPluginSearch = new JPopupMenuButton();
         cmdNewPolygon = new javax.swing.JToggleButton();
         cmdNewLinestring = new javax.swing.JToggleButton();
         cmdNewPoint = new javax.swing.JToggleButton();
         cmdMoveGeometry = new javax.swing.JToggleButton();
         cmdRemoveGeometry = new javax.swing.JToggleButton();
         jSeparator3 = new javax.swing.JSeparator();
         cmdNodeMove = new javax.swing.JToggleButton();
         cmdNodeAdd = new javax.swing.JToggleButton();
         cmdNodeRemove = new javax.swing.JToggleButton();
         cmdNodeRotateGeometry = new javax.swing.JToggleButton();
         jSeparator5 = new javax.swing.JSeparator();
         cmdSnap = new javax.swing.JToggleButton();
         jSeparator11 = new javax.swing.JSeparator();
         cmdUndo = new javax.swing.JButton();
         cmdRedo = new javax.swing.JButton();
         panStatus = new javax.swing.JPanel();
         mnuBar = new javax.swing.JMenuBar();
         menFile = new javax.swing.JMenu();
         mniLoadConfig = new javax.swing.JMenuItem();
         mniSaveConfig = new javax.swing.JMenuItem();
         mniLoadConfigFromServer = new javax.swing.JMenuItem();
         sepServerProfilesStart = new javax.swing.JSeparator();
         sepServerProfilesEnd = new javax.swing.JSeparator();
         mniSaveLayout = new javax.swing.JMenuItem();
         mniLoadLayout = new javax.swing.JMenuItem();
         jSeparator9 = new javax.swing.JSeparator();
         mniClipboard = new javax.swing.JMenuItem();
         mniGeoLinkClipboard = new javax.swing.JMenuItem();
         mniPrint = new javax.swing.JMenuItem();
         jSeparator10 = new javax.swing.JSeparator();
         mniClose = new javax.swing.JMenuItem();
         menEdit = new javax.swing.JMenu();
         mniRefresh = new javax.swing.JMenuItem();
         jSeparator13 = new javax.swing.JSeparator();
         mniZoomToSelectedObjects = new javax.swing.JMenuItem();
         mniZoomToAllObjects = new javax.swing.JMenuItem();
         jSeparator15 = new javax.swing.JSeparator();
         mniRemoveSelectedObject = new javax.swing.JMenuItem();
         mniRemoveAllObjects = new javax.swing.JMenuItem();
         menHistory = new javax.swing.JMenu();
         mniBack = new javax.swing.JMenuItem();
         mniForward = new javax.swing.JMenuItem();
         mniHome = new javax.swing.JMenuItem();
         sepBeforePos = new javax.swing.JSeparator();
         sepAfterPos = new javax.swing.JSeparator();
         mniHistorySidebar = new javax.swing.JMenuItem();
         menSearch = new javax.swing.JMenu();
         mniSearchRectangle = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolygon = new javax.swing.JRadioButtonMenuItem();
         mniSearchEllipse = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolyline = new javax.swing.JRadioButtonMenuItem();
         jSeparator8 = new javax.swing.JSeparator();
         mniSearchShowLastFeature = new javax.swing.JMenuItem();
         mniSearchRedo = new javax.swing.JMenuItem();
         mniSearchBuffer = new javax.swing.JMenuItem();
         menExtras = new javax.swing.JMenu();
         mniOptions = new javax.swing.JMenuItem();
         jSeparator16 = new javax.swing.JSeparator();
         mniGotoPoint = new javax.swing.JMenuItem();
         jSeparator14 = new javax.swing.JSeparator();
         mniScale = new javax.swing.JMenuItem();
         menWindows = new javax.swing.JMenu();
         mniLayer = new javax.swing.JMenuItem();
         mniCapabilities = new javax.swing.JMenuItem();
         mniFeatureInfo = new javax.swing.JMenuItem();
         mniClassTree = new javax.swing.JMenuItem();
         mniServerInfo = new javax.swing.JMenuItem();
         mniLayerInfo = new javax.swing.JMenuItem();
         mniLegend = new javax.swing.JMenuItem();
         mniFeatureControl = new javax.swing.JMenuItem();
         mniMap = new javax.swing.JMenuItem();
         mniOverview = new javax.swing.JMenuItem();
         jSeparator7 = new javax.swing.JSeparator();
         mniResetWindowLayout = new javax.swing.JMenuItem();
         menHelp = new javax.swing.JMenu();
         mniOnlineHelp = new javax.swing.JMenuItem();
         mniNews = new javax.swing.JMenuItem();
         mniAbout = new javax.swing.JMenuItem();
 
         mnuConfigServer.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"))); // NOI18N
         mnuConfigServer.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mnuConfigServer.text"));                                           // NOI18N
         mnuConfigServer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mnuConfigServerActionPerformed(evt);
                 }
             });
         popMen.add(mnuConfigServer);
 
         menBookmarks.setMnemonic('L');
         menBookmarks.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.menBookmarks.text")); // NOI18N
 
         mniAddBookmark.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark_add.png"))); // NOI18N
         mniAddBookmark.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniAddBookmark.text"));                                                          // NOI18N
         mniAddBookmark.setEnabled(false);
         menBookmarks.add(mniAddBookmark);
 
         mniBookmarkManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark_folder.png"))); // NOI18N
         mniBookmarkManager.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniBookmarkManager.text"));                                                             // NOI18N
         mniBookmarkManager.setEnabled(false);
         menBookmarks.add(mniBookmarkManager);
 
         mniBookmarkSidebar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark.png"))); // NOI18N
         mniBookmarkSidebar.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniBookmarkSidebar.text"));                                                      // NOI18N
         mniBookmarkSidebar.setEnabled(false);
         menBookmarks.add(mniBookmarkSidebar);
 
         popMenSearch.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
 
                 @Override
                 public void popupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {
                     popMenSearchPopupMenuWillBecomeVisible(evt);
                 }
 
                 @Override
                 public void popupMenuWillBecomeInvisible(final javax.swing.event.PopupMenuEvent evt) {
                 }
 
                 @Override
                 public void popupMenuCanceled(final javax.swing.event.PopupMenuEvent evt) {
                 }
             });
 
         mniSearchRectangle1.setAction(searchRectangleAction);
         cmdGroupSearch1.add(mniSearchRectangle1);
         mniSearchRectangle1.setSelected(true);
         mniSearchRectangle1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRectangle1.text"));                                                       // NOI18N
         mniSearchRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         popMenSearch.add(mniSearchRectangle1);
 
         mniSearchPolygon1.setAction(searchPolygonAction);
         cmdGroupSearch1.add(mniSearchPolygon1);
         mniSearchPolygon1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchPolygon1.text"));                                                     // NOI18N
         mniSearchPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         popMenSearch.add(mniSearchPolygon1);
 
         mniSearchEllipse1.setAction(searchEllipseAction);
         cmdGroupSearch1.add(mniSearchEllipse1);
         mniSearchEllipse1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchEllipse1.text"));                                                     // NOI18N
         mniSearchEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         popMenSearch.add(mniSearchEllipse1);
 
         mniSearchPolyline1.setAction(searchPolylineAction);
         cmdGroupSearch1.add(mniSearchPolyline1);
         mniSearchPolyline1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchPolyline1.text"));                                                      // NOI18N
         mniSearchPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         popMenSearch.add(mniSearchPolyline1);
         popMenSearch.add(jSeparator12);
 
         mniSearchShowLastFeature1.setAction(searchShowLastFeatureAction);
         mniSearchShowLastFeature1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniSearchShowLastFeature1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchShowLastFeature1.text"));        // NOI18N
         mniSearchShowLastFeature1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchShowLastFeature1.toolTipText")); // NOI18N
         popMenSearch.add(mniSearchShowLastFeature1);
 
         mniSearchRedo1.setAction(searchRedoAction);
         mniSearchRedo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         mniSearchRedo1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRedo1.text"));        // NOI18N
         mniSearchRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRedo1.toolTipText")); // NOI18N
         popMenSearch.add(mniSearchRedo1);
 
         mniSearchBuffer1.setAction(searchBufferAction);
         mniSearchBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniSearchBuffer1.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchBuffer1.text"));                                                    // NOI18N
         mniSearchBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchBuffer1.toolTipText"));                                             // NOI18N
         popMenSearch.add(mniSearchBuffer1);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.Form.title")); // NOI18N
         addWindowListener(new java.awt.event.WindowAdapter() {
 
                 @Override
                 public void windowClosed(final java.awt.event.WindowEvent evt) {
                     formWindowClosed(evt);
                 }
             });
         addComponentListener(new java.awt.event.ComponentAdapter() {
 
                 @Override
                 public void componentResized(final java.awt.event.ComponentEvent evt) {
                     formComponentResized(evt);
                 }
 
                 @Override
                 public void componentShown(final java.awt.event.ComponentEvent evt) {
                     formComponentShown(evt);
                 }
             });
 
         panAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         panAll.setLayout(new java.awt.BorderLayout());
 
         panToolbar.setLayout(new java.awt.BorderLayout());
         panAll.add(panToolbar, java.awt.BorderLayout.NORTH);
 
         panMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
         panMain.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseEntered(final java.awt.event.MouseEvent evt) {
                     panMainMouseEntered(evt);
                 }
 
                 @Override
                 public void mouseExited(final java.awt.event.MouseEvent evt) {
                     panMainMouseExited(evt);
                 }
             });
         panMain.setLayout(new java.awt.BorderLayout());
 
         tlbMain.addMouseListener(new java.awt.event.MouseAdapter() {
 
                 @Override
                 public void mouseClicked(final java.awt.event.MouseEvent evt) {
                     tlbMainMouseClicked(evt);
                 }
             });
 
         cmdReconfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.gif"))); // NOI18N
         cmdReconfig.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdReconfig.toolTipText"));                                           // NOI18N
         cmdReconfig.setBorderPainted(false);
         cmdReconfig.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdReconfigActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdReconfig);
 
         jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator1.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator1.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator1);
 
         cmdBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/back.png"))); // NOI18N
         cmdBack.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdBack.toolTipText"));                                           // NOI18N
         cmdBack.setBorderPainted(false);
         cmdBack.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdBackActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdBack);
 
         cmdHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.gif"))); // NOI18N
         cmdHome.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdHome.toolTipText"));                                           // NOI18N
         cmdHome.setBorderPainted(false);
         cmdHome.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdHomeActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdHome);
 
         cmdForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/forward.png"))); // NOI18N
         cmdForward.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdForward.toolTipText"));                                              // NOI18N
         cmdForward.setBorderPainted(false);
         tlbMain.add(cmdForward);
 
         jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator2.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator2.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator2);
 
         cmdRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload.gif"))); // NOI18N
         cmdRefresh.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdRefresh.toolTipText"));                                             // NOI18N
         cmdRefresh.setBorderPainted(false);
         cmdRefresh.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdRefreshActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdRefresh);
 
         jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator6.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator6.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator6);
 
         cmdPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/frameprint.png")));            // NOI18N
         cmdPrint.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.cmdPrint.text")); // NOI18N
         cmdPrint.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdPrint.toolTipText"));                                                            // NOI18N
         cmdPrint.setBorderPainted(false);
         cmdPrint.setName("cmdPrint");                                                                             // NOI18N
         cmdPrint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdPrintActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdPrint);
 
         cmdClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard.png"))); // NOI18N
         cmdClipboard.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdClipboard.text"));                                                       // NOI18N
         cmdClipboard.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdClipboard.toolTipText"));                                                // NOI18N
         cmdClipboard.setBorderPainted(false);
         cmdClipboard.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdClipboardActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdClipboard);
 
         jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator4.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator4.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator4);
 
         cmdGroupPrimaryInteractionMode.add(togInvisible);
         togInvisible.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.togInvisible.text")); // NOI18N
         tlbMain.add(togInvisible);
 
         cmdGroupPrimaryInteractionMode.add(cmdSelect);
         cmdSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select.png"))); // NOI18N
         cmdSelect.setSelected(true);
         cmdSelect.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdSelect.toolTipText"));                                             // NOI18N
         cmdSelect.setBorderPainted(false);
         cmdSelect.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdSelectActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdSelect);
 
         cmdGroupPrimaryInteractionMode.add(cmdZoom);
         cmdZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom.gif"))); // NOI18N
         cmdZoom.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdZoom.toolTipText"));                                           // NOI18N
         cmdZoom.setBorderPainted(false);
         cmdZoom.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdZoomActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdZoom);
 
         cmdGroupPrimaryInteractionMode.add(cmdPan);
         cmdPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pan.gif"))); // NOI18N
         cmdPan.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdPan.toolTipText"));                                          // NOI18N
         cmdPan.setBorderPainted(false);
         cmdPan.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdPanActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdPan);
 
         cmdGroupPrimaryInteractionMode.add(cmdFeatureInfo);
         cmdFeatureInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/featureInfos.gif"))); // NOI18N
         cmdFeatureInfo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdFeatureInfo.toolTipText"));                                                   // NOI18N
         cmdFeatureInfo.setBorderPainted(false);
         cmdFeatureInfo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdFeatureInfoActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdFeatureInfo);
 
         cmdPluginSearch.setAction(searchAction);
         cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
         cmdPluginSearch.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdPluginSearch.toolTipText"));                                                            // NOI18N
         cmdGroupPrimaryInteractionMode.add(cmdPluginSearch);
         cmdPluginSearch.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdPluginSearchActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdPluginSearch);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewPolygon);
         cmdNewPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newPolygon.png"))); // NOI18N
         cmdNewPolygon.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNewPolygon.toolTipText"));                                                 // NOI18N
         cmdNewPolygon.setBorderPainted(false);
         cmdNewPolygon.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNewPolygonActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNewPolygon);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewLinestring);
         cmdNewLinestring.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newLinestring.png"))); // NOI18N
         cmdNewLinestring.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNewLinestring.toolTipText"));                                                    // NOI18N
         cmdNewLinestring.setBorderPainted(false);
         cmdNewLinestring.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     createGeometryAction(evt);
                 }
             });
         tlbMain.add(cmdNewLinestring);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewPoint);
         cmdNewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newPoint.png"))); // NOI18N
         cmdNewPoint.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNewPoint.toolTipText"));                                               // NOI18N
         cmdNewPoint.setBorderPainted(false);
         cmdNewPoint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNewPointActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNewPoint);
 
         cmdGroupPrimaryInteractionMode.add(cmdMoveGeometry);
         cmdMoveGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/move.png"))); // NOI18N
         cmdMoveGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdMoveGeometry.toolTipText"));                                           // NOI18N
         cmdMoveGeometry.setBorderPainted(false);
         cmdMoveGeometry.setMaximumSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.setMinimumSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.setPreferredSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdMoveGeometryActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdMoveGeometry);
 
         cmdGroupPrimaryInteractionMode.add(cmdRemoveGeometry);
         cmdRemoveGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove.png"))); // NOI18N
         cmdRemoveGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdRemoveGeometry.toolTipText"));                                             // NOI18N
         cmdRemoveGeometry.setBorderPainted(false);
         cmdRemoveGeometry.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdRemoveGeometryActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdRemoveGeometry);
 
         jSeparator3.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator3.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator3.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator3);
 
         cmdGroupNodes.add(cmdNodeMove);
         cmdNodeMove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/moveNodes.png"))); // NOI18N
         cmdNodeMove.setSelected(true);
         cmdNodeMove.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNodeMove.toolTipText"));                                                // NOI18N
         cmdNodeMove.setBorderPainted(false);
         cmdNodeMove.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNodeMoveActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNodeMove);
 
         cmdGroupNodes.add(cmdNodeAdd);
         cmdNodeAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertNodes.png"))); // NOI18N
         cmdNodeAdd.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNodeAdd.toolTipText"));                                                  // NOI18N
         cmdNodeAdd.setBorderPainted(false);
         cmdNodeAdd.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNodeAddActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNodeAdd);
 
         cmdGroupNodes.add(cmdNodeRemove);
         cmdNodeRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/removeNodes.png"))); // NOI18N
         cmdNodeRemove.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNodeRemove.toolTipText"));                                                  // NOI18N
         cmdNodeRemove.setBorderPainted(false);
         cmdNodeRemove.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNodeRemoveActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNodeRemove);
 
         cmdGroupNodes.add(cmdNodeRotateGeometry);
         cmdNodeRotateGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rotate.png"))); // NOI18N
         cmdNodeRotateGeometry.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdNodeRotateGeometry.toolTipText"));                                             // NOI18N
         cmdNodeRotateGeometry.setBorderPainted(false);
         cmdNodeRotateGeometry.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdNodeRotateGeometryActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdNodeRotateGeometry);
 
         jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator5.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator5.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator5);
 
         cmdSnap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap.png")));                          // NOI18N
         cmdSnap.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdSnap.toolTipText"));                                                                    // NOI18N
         cmdSnap.setBorderPainted(false);
         cmdSnap.setMaximumSize(new java.awt.Dimension(29, 29));
         cmdSnap.setMinimumSize(new java.awt.Dimension(29, 29));
         cmdSnap.setPreferredSize(new java.awt.Dimension(29, 29));
         cmdSnap.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png"))); // NOI18N
         cmdSnap.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png")));         // NOI18N
         cmdSnap.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     cmdSnapActionPerformed(evt);
                 }
             });
         tlbMain.add(cmdSnap);
 
         jSeparator11.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator11.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator11.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator11);
 
         cmdUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undo.png"))); // NOI18N
         cmdUndo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdUndo.toolTipText"));                                           // NOI18N
         cmdUndo.setBorderPainted(false);
         cmdUndo.setEnabled(false);
         cmdUndo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniUndoPerformed(evt);
                 }
             });
         tlbMain.add(cmdUndo);
 
         cmdRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redo.png"))); // NOI18N
         cmdRedo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.cmdRedo.toolTipText"));                                           // NOI18N
         cmdRedo.setBorderPainted(false);
         cmdRedo.setEnabled(false);
         cmdRedo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniRedoPerformed(evt);
                 }
             });
         tlbMain.add(cmdRedo);
 
         panMain.add(tlbMain, java.awt.BorderLayout.NORTH);
 
         panAll.add(panMain, java.awt.BorderLayout.CENTER);
 
         panStatus.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 4, 1, 4));
         panStatus.setLayout(new java.awt.BorderLayout());
         panAll.add(panStatus, java.awt.BorderLayout.SOUTH);
 
         getContentPane().add(panAll, java.awt.BorderLayout.CENTER);
 
         menFile.setMnemonic('D');
         menFile.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menFile.text")); // NOI18N
 
         mniLoadConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_L,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniLoadConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniLoadConfig.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniLoadConfig.text"));                                                    // NOI18N
         mniLoadConfig.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLoadConfigActionPerformed(evt);
                 }
             });
         menFile.add(mniLoadConfig);
 
         mniSaveConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_K,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniSaveConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniSaveConfig.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSaveConfig.text"));                                                    // NOI18N
         mniSaveConfig.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSaveConfigActionPerformed(evt);
                 }
             });
         menFile.add(mniSaveConfig);
 
         mniLoadConfigFromServer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniLoadConfigFromServer.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniLoadConfigFromServer.text"));                                                    // NOI18N
         mniLoadConfigFromServer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLoadConfigFromServerActionPerformed(evt);
                 }
             });
         menFile.add(mniLoadConfigFromServer);
 
         sepServerProfilesStart.setName("sepServerProfilesStart"); // NOI18N
         menFile.add(sepServerProfilesStart);
 
         sepServerProfilesEnd.setName("sepServerProfilesEnd"); // NOI18N
         menFile.add(sepServerProfilesEnd);
 
         mniSaveLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_S,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniSaveLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniSaveLayout.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSaveLayout.text"));                                                    // NOI18N
         mniSaveLayout.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniSaveLayoutActionPerformed(evt);
                 }
             });
         menFile.add(mniSaveLayout);
 
         mniLoadLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_O,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniLoadLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniLoadLayout.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniLoadLayout.text"));                                                    // NOI18N
         mniLoadLayout.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLoadLayoutActionPerformed(evt);
                 }
             });
         menFile.add(mniLoadLayout);
         menFile.add(jSeparator9);
 
         mniClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_C,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png"))); // NOI18N
         mniClipboard.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniClipboard.text"));                                                         // NOI18N
         mniClipboard.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniClipboardActionPerformed(evt);
                 }
             });
         menFile.add(mniClipboard);
 
         mniGeoLinkClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_C,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         mniGeoLinkClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png"))); // NOI18N
         mniGeoLinkClipboard.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniGeoLinkClipboard.text"));                                                         // NOI18N
         mniGeoLinkClipboard.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniGeoLinkClipboardActionPerformed(evt);
                 }
             });
         menFile.add(mniGeoLinkClipboard);
 
         mniPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_P,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/frameprint16.png")));          // NOI18N
         mniPrint.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniPrint.text")); // NOI18N
         mniPrint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniPrintActionPerformed(evt);
                 }
             });
         menFile.add(mniPrint);
         menFile.add(jSeparator10);
 
         mniClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_F4,
                 java.awt.event.InputEvent.ALT_MASK));
         mniClose.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniClose.text")); // NOI18N
         mniClose.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniCloseActionPerformed(evt);
                 }
             });
         menFile.add(mniClose);
 
         mnuBar.add(menFile);
 
         menEdit.setMnemonic('B');
         menEdit.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menEdit.text")); // NOI18N
         menEdit.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     menEditActionPerformed(evt);
                 }
             });
 
         mniRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload16.gif")));                // NOI18N
         mniRefresh.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniRefresh.text")); // NOI18N
         mniRefresh.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniRefreshActionPerformed(evt);
                 }
             });
         menEdit.add(mniRefresh);
         menEdit.add(jSeparator13);
 
         mniZoomToSelectedObjects.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToSelection.png"))); // NOI18N
         mniZoomToSelectedObjects.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniZoomToSelectedObjects.text"));                                    // NOI18N
         mniZoomToSelectedObjects.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniZoomToSelectedObjectsActionPerformed(evt);
                 }
             });
         menEdit.add(mniZoomToSelectedObjects);
 
         mniZoomToAllObjects.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToAll.png"))); // NOI18N
         mniZoomToAllObjects.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniZoomToAllObjects.text"));                                   // NOI18N
         mniZoomToAllObjects.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniZoomToAllObjectsActionPerformed(evt);
                 }
             });
         menEdit.add(mniZoomToAllObjects);
         menEdit.add(jSeparator15);
 
         mniRemoveSelectedObject.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/res/removerow.png"))); // NOI18N
         mniRemoveSelectedObject.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniRemoveSelectedObject.text"));                               // NOI18N
         mniRemoveSelectedObject.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniRemoveSelectedObjectActionPerformed(evt);
                 }
             });
         menEdit.add(mniRemoveSelectedObject);
 
         mniRemoveAllObjects.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/res/removeAll.png"))); // NOI18N
         mniRemoveAllObjects.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniRemoveAllObjects.text"));                                   // NOI18N
         mniRemoveAllObjects.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniRemoveAllObjectsActionPerformed(evt);
                 }
             });
         menEdit.add(mniRemoveAllObjects);
 
         mnuBar.add(menEdit);
 
         menHistory.setMnemonic('C');
         menHistory.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menHistory.text")); // NOI18N
 
         mniBack.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_LEFT,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/back16.png")));               // NOI18N
         mniBack.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniBack.text")); // NOI18N
         mniBack.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniBackActionPerformed(evt);
                 }
             });
         menHistory.add(mniBack);
 
         mniForward.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_RIGHT,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/forward16.png")));               // NOI18N
         mniForward.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniForward.text")); // NOI18N
         mniForward.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniForwardActionPerformed(evt);
                 }
             });
         menHistory.add(mniForward);
 
         mniHome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, 0));
         mniHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home16.png")));               // NOI18N
         mniHome.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniHome.text")); // NOI18N
         mniHome.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniHomeActionPerformed(evt);
                 }
             });
         menHistory.add(mniHome);
         menHistory.add(sepBeforePos);
         menHistory.add(sepAfterPos);
 
         mniHistorySidebar.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniHistorySidebar.text")); // NOI18N
         mniHistorySidebar.setEnabled(false);
         menHistory.add(mniHistorySidebar);
 
         mnuBar.add(menHistory);
 
         menSearch.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menSearch.text")); // NOI18N
         menSearch.addMenuListener(new javax.swing.event.MenuListener() {
 
                 @Override
                 public void menuSelected(final javax.swing.event.MenuEvent evt) {
                     menSearchMenuSelected(evt);
                 }
 
                 @Override
                 public void menuDeselected(final javax.swing.event.MenuEvent evt) {
                 }
 
                 @Override
                 public void menuCanceled(final javax.swing.event.MenuEvent evt) {
                 }
             });
 
         mniSearchRectangle.setAction(searchRectangleAction);
         cmdGroupSearch.add(mniSearchRectangle);
         mniSearchRectangle.setSelected(true);
         mniSearchRectangle.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRectangle.text"));                                                       // NOI18N
         mniSearchRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         menSearch.add(mniSearchRectangle);
 
         mniSearchPolygon.setAction(searchPolygonAction);
         cmdGroupSearch.add(mniSearchPolygon);
         mniSearchPolygon.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchPolygon.text"));                                                     // NOI18N
         mniSearchPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         menSearch.add(mniSearchPolygon);
 
         mniSearchEllipse.setAction(searchEllipseAction);
         cmdGroupSearch.add(mniSearchEllipse);
         mniSearchEllipse.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchEllipse.text_1"));                                                   // NOI18N
         mniSearchEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         menSearch.add(mniSearchEllipse);
 
         mniSearchPolyline.setAction(searchPolylineAction);
         cmdGroupSearch.add(mniSearchPolyline);
         mniSearchPolyline.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchPolyline.text_1"));                                                    // NOI18N
         mniSearchPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         menSearch.add(mniSearchPolyline);
         menSearch.add(jSeparator8);
 
         mniSearchShowLastFeature.setAction(searchShowLastFeatureAction);
         mniSearchShowLastFeature.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniSearchShowLastFeature.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchShowLastFeature.text"));        // NOI18N
         mniSearchShowLastFeature.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchShowLastFeature.toolTipText")); // NOI18N
         menSearch.add(mniSearchShowLastFeature);
 
         mniSearchRedo.setAction(searchRedoAction);
         mniSearchRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_Y,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         mniSearchRedo.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRedo.text"));        // NOI18N
         mniSearchRedo.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchRedo.toolTipText")); // NOI18N
         menSearch.add(mniSearchRedo);
 
         mniSearchBuffer.setAction(searchBufferAction);
         mniSearchBuffer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniSearchBuffer.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchBuffer.text_1"));                                                  // NOI18N
         mniSearchBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniSearchBuffer.toolTipText"));                                             // NOI18N
         menSearch.add(mniSearchBuffer);
 
         mnuBar.add(menSearch);
         // copyMenu(menSearch, popMenSearch);
         ((JPopupMenuButton)cmdPluginSearch).setPopupMenu(popMenSearch);
 
         menExtras.setMnemonic('E');
         menExtras.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menExtras.text")); // NOI18N
 
         mniOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/tooloptions.png")));             // NOI18N
         mniOptions.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniOptions.text")); // NOI18N
         mniOptions.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniOptionsActionPerformed(evt);
                 }
             });
         menExtras.add(mniOptions);
         menExtras.add(jSeparator16);
 
         mniGotoPoint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_G,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniGotoPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/goto.png"))); // NOI18N
         mniGotoPoint.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniGotoPoint.text"));                                                  // NOI18N
         mniGotoPoint.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniGotoPointActionPerformed(evt);
                 }
             });
         menExtras.add(mniGotoPoint);
         menExtras.add(jSeparator14);
 
         mniScale.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_M,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/scale.png")));                 // NOI18N
         mniScale.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniScale.text")); // NOI18N
         mniScale.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniScaleActionPerformed(evt);
                 }
             });
         menExtras.add(mniScale);
 
         mnuBar.add(menExtras);
 
         menWindows.setMnemonic('F');
         menWindows.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menWindows.text")); // NOI18N
         menWindows.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     menWindowsActionPerformed(evt);
                 }
             });
 
         mniLayer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_1,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniLayer.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png")));                  // NOI18N
         mniLayer.setMnemonic('L');
         mniLayer.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniLayer.text")); // NOI18N
         mniLayer.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLayerActionPerformed(evt);
                 }
             });
         menWindows.add(mniLayer);
 
         mniCapabilities.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_2,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniCapabilities.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"))); // NOI18N
         mniCapabilities.setMnemonic('C');
         mniCapabilities.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniCapabilities.text"));                                           // NOI18N
         mniCapabilities.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniCapabilitiesActionPerformed(evt);
                 }
             });
         menWindows.add(mniCapabilities);
 
         mniFeatureInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_3,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniFeatureInfo.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/featureInfo16.png"))); // NOI18N
         mniFeatureInfo.setMnemonic('F');
         mniFeatureInfo.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniFeatureInfo.text"));                                                              // NOI18N
         mniFeatureInfo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniFeatureInfoActionPerformed(evt);
                 }
             });
         menWindows.add(mniFeatureInfo);
 
         mniClassTree.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_4,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniClassTree.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/classSelection.png"))); // NOI18N
         mniClassTree.setMnemonic('a');
         mniClassTree.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniClassTree.text"));                                                            // NOI18N
         mniClassTree.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniClassTreeActionPerformed(evt);
                 }
             });
         menWindows.add(mniClassTree);
 
         mniServerInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_5,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniServerInfo.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/serverInfo.png"))); // NOI18N
         mniServerInfo.setMnemonic('S');
         mniServerInfo.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniServerInfo.text"));                                                           // NOI18N
         mniServerInfo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniServerInfoActionPerformed(evt);
                 }
             });
         menWindows.add(mniServerInfo);
 
         mniLayerInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_6,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniLayerInfo.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/layerInfo.png"))); // NOI18N
         mniLayerInfo.setMnemonic('L');
         mniLayerInfo.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniLayerInfo.text"));                                                           // NOI18N
         mniLayerInfo.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLayerInfoActionPerformed(evt);
                 }
             });
         menWindows.add(mniLayerInfo);
 
         mniLegend.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_7,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniLegend.setIcon(new javax.swing.ImageIcon(
                 getClass().getResource("/de/cismet/cismap/navigatorplugin/res/legend.png")));                       // NOI18N
         mniLegend.setMnemonic('L');
         mniLegend.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniLegend.text")); // NOI18N
         mniLegend.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniLegendActionPerformed(evt);
                 }
             });
         menWindows.add(mniLegend);
 
         mniFeatureControl.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_8,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniFeatureControl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/objects.png"))); // NOI18N
         mniFeatureControl.setMnemonic('O');
         mniFeatureControl.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniFeatureControl.text"));                                                     // NOI18N
         mniFeatureControl.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniFeatureControlActionPerformed(evt);
                 }
             });
         menWindows.add(mniFeatureControl);
 
         mniMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_9,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/map.png")));                 // NOI18N
         mniMap.setMnemonic('M');
         mniMap.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniMap.text")); // NOI18N
         mniMap.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniMapActionPerformed(evt);
                 }
             });
         menWindows.add(mniMap);
 
         mniOverview.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_0,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniOverview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/map.png")));                      // NOI18N
         mniOverview.setMnemonic('M');
         mniOverview.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniOverview.text")); // NOI18N
         mniOverview.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniOverviewActionPerformed(evt);
                 }
             });
         menWindows.add(mniOverview);
         menWindows.add(jSeparator7);
 
         mniResetWindowLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_R,
                 java.awt.event.InputEvent.CTRL_MASK));
         mniResetWindowLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniResetWindowLayout.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniResetWindowLayout.text"));                                                    // NOI18N
         mniResetWindowLayout.setToolTipText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniResetWindowLayout.toolTipText"));                                             // NOI18N
         mniResetWindowLayout.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniResetWindowLayoutActionPerformed(evt);
                 }
             });
         menWindows.add(mniResetWindowLayout);
 
         mnuBar.add(menWindows);
 
         menHelp.setMnemonic('H');
         menHelp.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menHelp.text")); // NOI18N
         menHelp.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     menHelpActionPerformed(evt);
                 }
             });
 
         mniOnlineHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
         mniOnlineHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
         mniOnlineHelp.setText(org.openide.util.NbBundle.getMessage(
                 CismapPlugin.class,
                 "CismapPlugin.mniOnlineHelp.text"));                                                  // NOI18N
         mniOnlineHelp.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniOnlineHelpActionPerformed(evt);
                 }
             });
         menHelp.add(mniOnlineHelp);
 
         mniNews.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/news.png")));                 // NOI18N
         mniNews.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniNews.text")); // NOI18N
         mniNews.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniNewsActionPerformed(evt);
                 }
             });
         menHelp.add(mniNews);
 
         mniAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(
                 java.awt.event.KeyEvent.VK_A,
                 java.awt.event.InputEvent.ALT_MASK
                         | java.awt.event.InputEvent.CTRL_MASK));
         mniAbout.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniAbout.text")); // NOI18N
         mniAbout.addActionListener(new java.awt.event.ActionListener() {
 
                 @Override
                 public void actionPerformed(final java.awt.event.ActionEvent evt) {
                     mniAboutActionPerformed(evt);
                 }
             });
         menHelp.add(mniAbout);
 
         mnuBar.add(menHelp);
 
         setJMenuBar(mnuBar);
     } // </editor-fold>
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniRedoPerformed(final java.awt.event.ActionEvent evt) {
         log.info("REDO"); // NOI18N
 
         final CustomAction a = mapC.getMemRedo().getLastAction();
         if (log.isDebugEnabled()) {
             log.debug("... execute action: " + a.info()); // NOI18N
         }
 
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error while executing an action", e); // NOI18N
         }
 
         final CustomAction inverse = a.getInverse();
         mapC.getMemUndo().addAction(inverse);
         if (log.isDebugEnabled()) {
             log.debug("... new action on UNDO stack: " + inverse); // NOI18N
             log.debug("... completed");                            // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniUndoPerformed(final java.awt.event.ActionEvent evt) {
         log.info("UNDO"); // NOI18N
 
         final CustomAction a = mapC.getMemUndo().getLastAction();
         if (log.isDebugEnabled()) {
             log.debug("... execute action: " + a.info()); // NOI18N
         }
 
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error while executing action", e); // NOI18N
         }
 
         final CustomAction inverse = a.getInverse();
         mapC.getMemRedo().addAction(inverse);
         if (log.isDebugEnabled()) {
             log.debug("... new action on REDO stack: " + inverse); // NOI18N
             log.debug("... completed");                            // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniGeoLinkClipboardActionPerformed(final java.awt.event.ActionEvent evt) {
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
 // EventQueue.invokeLater(new Runnable() {
 // public void run() {
 // clipboarder.setLocationRelativeTo(CismapPlugin.this);
 // clipboarder.setVisible(true);
 // }
 // });
                         final BoundingBox bb = mapC.getCurrentBoundingBox();
                         final String u = "http://localhost:" + httpInterfacePort + "/gotoBoundingBox?x1=" + bb.getX1()
                                     + "&y1=" + bb.getY1() + "&x2=" + bb.getX2() + "&y2=" + bb.getY2(); // NOI18N
                         final GeoLinkUrl url = new GeoLinkUrl(u);
                         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(url, null);
                         EventQueue.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     clipboarder.dispose();
                                 }
                             });
                     }
                 });
         t.start();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void menHelpActionPerformed(final java.awt.event.ActionEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniAboutActionPerformed(final java.awt.event.ActionEvent evt) {
         if (about == null) {
             about = new AboutDialog(this, true);
         }
 
         about.setLocationRelativeTo(this);
         about.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniNewsActionPerformed(final java.awt.event.ActionEvent evt) {
         openUrlInExternalBrowser(newsUrl);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniOnlineHelpActionPerformed(final java.awt.event.ActionEvent evt) {
         openUrlInExternalBrowser(helpUrl);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniGotoPointActionPerformed(final java.awt.event.ActionEvent evt) {
         if (log.isDebugEnabled()) {
             log.debug("mniGotoPointActionPerformed"); // NOI18N
         }
 
         try {
             final BoundingBox c = mapC.getCurrentBoundingBox();
             final double x = (c.getX1() + c.getX2()) / 2;
             final double y = (c.getY1() + c.getY2()) / 2;
             final String s = JOptionPane.showInputDialog(
                     this,
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.mniGotoPointActionPerformed.JOptionPane.message"),
                     StaticDecimalTools.round(x)
                             + ","
                             + StaticDecimalTools.round(y)); // NOI18N
 
             final String[] sa = s.split(",");                        // NOI18N
             final Double gotoX = new Double(sa[0]);
             final Double gotoY = new Double(sa[1]);
             final BoundingBox bb = new BoundingBox(gotoX, gotoY, gotoX, gotoY);
             mapC.gotoBoundingBox(bb, true, false, mapC.getAnimationDuration());
         } catch (Exception skip) {
             log.error("Error in mniGotoPointActionPerformed", skip); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniScaleActionPerformed(final java.awt.event.ActionEvent evt) {
         try {
             final String s = JOptionPane.showInputDialog(
                     this,
                     org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.scaleManually"),
                     ((int)mapC.getScaleDenominator())
                             + "");                               // NOI18N
             final Integer i = new Integer(s);
             mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(i));
         } catch (Exception skip) {
             log.error("Error in mniScaleActionPerformed", skip); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniMapActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vMap);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniRemoveAllObjectsActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             final Vector v = new Vector(mapC.getFeatureCollection().getAllFeatures());
             mapC.getFeatureCollection().removeFeatures(v);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniZoomToSelectedObjectsActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.zoomToSelection();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniZoomToAllObjectsActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.zoomToFeatureCollection();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniForwardActionPerformed(final java.awt.event.ActionEvent evt) {
         if ((mapC != null) && mapC.isForwardPossible()) {
             mapC.forward(true);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniBackActionPerformed(final java.awt.event.ActionEvent evt) {
         if ((mapC != null) && mapC.isBackPossible()) {
             mapC.back(true);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniHomeActionPerformed(final java.awt.event.ActionEvent evt) {
         cmdHomeActionPerformed(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniRefreshActionPerformed(final java.awt.event.ActionEvent evt) {
         cmdRefreshActionPerformed(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniRemoveSelectedObjectActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             final Vector v = new Vector(mapC.getFeatureCollection().getSelectedFeatures());
             mapC.getFeatureCollection().removeFeatures(v);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLoadConfigActionPerformed(final java.awt.event.ActionEvent evt) {
         JFileChooser fc;
 
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
 
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".xml"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.mniLoadConfigActionPerformed.FileFiltergetDescription.return"); // NOI18N
                 }
             });
 
         final int state = fc.showOpenDialog(this);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
 
             if (name.endsWith(".xml")) {                       // NOI18N
                 activeLayers.removeAllLayers();
                 mapC.getRasterServiceLayer().removeAllChildren();
                 configurationManager.configure(name);
             } else {
                 activeLayers.removeAllLayers();
                 mapC.getRasterServiceLayer().removeAllChildren();
                 configurationManager.configure(name + ".xml"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSaveConfigActionPerformed(final java.awt.event.ActionEvent evt) {
         JFileChooser fc;
 
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
 
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".xml"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.mniSaveConfigActionPerformed.FileFilter.getDescription.return"); // NOI18N
                 }
             });
 
         final int state = fc.showSaveDialog(this);
         if (log.isDebugEnabled()) {
             log.debug("state:" + state); // NOI18N
         }
 
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
 
             if (name.endsWith(".xml")) {                                // NOI18N
                 configurationManager.writeConfiguration(name);
             } else {
                 configurationManager.writeConfiguration(name + ".xml"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLoadConfigFromServerActionPerformed(final java.awt.event.ActionEvent evt) {
         activeLayers.removeAllLayers();
         mapC.getMapServiceLayer().removeAllChildren();
         configureApp(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniPrintActionPerformed(final java.awt.event.ActionEvent evt) {
         cmdPrintActionPerformed(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniSaveLayoutActionPerformed(final java.awt.event.ActionEvent evt) {
         JFileChooser fc;
 
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
 
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".layout"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return "Layout"; // NOI18N
                 }
             });
         fc.setMultiSelectionEnabled(false);
 
         final int state = fc.showSaveDialog(this);
         if (log.isDebugEnabled()) {
             log.debug("state:" + state); // NOI18N
         }
 
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             if (log.isDebugEnabled()) {
                 log.debug("file:" + file); // NOI18N
             }
 
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
 
             if (name.endsWith(".layout")) {   // NOI18N
                 saveLayout(name);
             } else {
                 saveLayout(name + ".layout"); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLoadLayoutActionPerformed(final java.awt.event.ActionEvent evt) {
         JFileChooser fc;
 
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
 
         fc.setFileFilter(new FileFilter() {
 
                 @Override
                 public boolean accept(final File f) {
                     return f.getName().toLowerCase().endsWith(".layout"); // NOI18N
                 }
 
                 @Override
                 public String getDescription() {
                     return "Layout"; // NOI18N
                 }
             });
         fc.setMultiSelectionEnabled(false);
 
         final int state = fc.showOpenDialog(this);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             final File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
 
             if (name.endsWith(".layout")) {                                                          // NOI18N
                 loadLayout(name);
             } else {
                 JOptionPane.showMessageDialog(
                     this,
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.mniLoadLayoutActionPerformed(ActionEvent).JOptionPane.msg"),   // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.mniLoadLayoutActionPerformed(ActionEvent).JOptionPane.title"), // NOI18N
                     JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniClipboardActionPerformed(final java.awt.event.ActionEvent evt) {
         cmdClipboardActionPerformed(null);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniCloseActionPerformed(final java.awt.event.ActionEvent evt) {
         this.dispose();
         System.exit(0);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void menEditActionPerformed(final java.awt.event.ActionEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void menWindowsActionPerformed(final java.awt.event.ActionEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void tlbMainMouseClicked(final java.awt.event.MouseEvent evt) {
         if (evt.getClickCount() == 3) {
             // DockingManager.dock((Dockable)vDebug,(Dockable)vMap, DockingConstants.SOUTH_REGION, .25f);
             // DockingManager.dock((Dockable)vGroovy,(Dockable)vMap);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPrintActionPerformed(final java.awt.event.ActionEvent evt) {
         final String oldMode = mapC.getInteractionMode();
         if (log.isDebugEnabled()) {
             log.debug("oldInteractionMode:" + oldMode); // NOI18N
         }
         togInvisible.setSelected(true);
         mapC.showPrintingSettingsDialog(oldMode);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdClipboardActionPerformed(final java.awt.event.ActionEvent evt) {
         final Thread t = new Thread(new Runnable() {
 
                     @Override
                     public void run() {
                         EventQueue.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     clipboarder.setLocationRelativeTo(CismapPlugin.this);
                                     clipboarder.setVisible(true);
                                 }
                             });
 
                         final ImageSelection imgSel = new ImageSelection(mapC.getImage());
                         Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
                         EventQueue.invokeLater(new Runnable() {
 
                                 @Override
                                 public void run() {
                                     clipboarder.dispose();
                                 }
                             });
                     }
                 });
         t.start();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdSnapActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setSnappingEnabled(cmdSnap.isSelected());
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdRemoveGeometryActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setInteractionMode(MappingComponent.REMOVE_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdMoveGeometryActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setInteractionMode(MappingComponent.MOVE_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNewPointActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                         CreateNewGeometryListener.POINT);
                     mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNewPolygonActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                         CreateNewGeometryListener.POLYGON);
                     mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void createGeometryAction(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     ((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(
                         CreateNewGeometryListener.LINESTRING);
                     mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNodeRemoveActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
                     mapC.setInteractionMode(MappingComponent.SELECT);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNodeAddActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setHandleInteractionMode(MappingComponent.ADD_HANDLE);
                     mapC.setInteractionMode(MappingComponent.SELECT);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNodeMoveActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
                     mapC.setInteractionMode(MappingComponent.SELECT);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdSelectActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     mapC.setInteractionMode(MappingComponent.SELECT);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniResetWindowLayoutActionPerformed(final java.awt.event.ActionEvent evt) {
         setupDefaultLayout();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniFeatureControlActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vFeatureControl);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdReconfigActionPerformed(final java.awt.event.ActionEvent evt) {
         activeLayers.removeAllLayers();
         mapC.getRasterServiceLayer().removeAllChildren();
 
         // mapC.resetWtst();
         configureApp(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniFeatureInfoActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vFeatureInfo);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniServerInfoActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vServerInfo);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLayerInfoActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vLayerInfo);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLegendActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vLegend);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniCapabilitiesActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vCaps);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniClassTreeActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vMetaSearch);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniLayerActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vLayers);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdFeatureInfoActionPerformed(final java.awt.event.ActionEvent evt) {
         mapC.setInteractionMode(MappingComponent.FEATURE_INFO);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formComponentShown(final java.awt.event.ComponentEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formWindowClosed(final java.awt.event.WindowEvent evt) {
         log.info("CLOSE"); // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  v  DOCUMENT ME!
      */
     private void showOrHideView(final View v) {
         ///irgendwas besser als Closable ??
         // Problem wenn floating --> close -> open  (muss zweimal open)
         if (v.isClosable()) {
             v.close();
         } else {
             v.restore();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPanActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.setInteractionMode(MappingComponent.PAN);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdZoomActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.setInteractionMode(MappingComponent.ZOOM);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdRefreshActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.refresh();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdHomeActionPerformed(final java.awt.event.ActionEvent evt) {
         if (mapC != null) {
             mapC.gotoInitialBoundingBox();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdBackActionPerformed(final java.awt.event.ActionEvent evt) {
 // mapC.back(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void panMainMouseEntered(final java.awt.event.MouseEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void panMainMouseExited(final java.awt.event.MouseEvent evt) {
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdNodeRotateGeometryActionPerformed(final java.awt.event.ActionEvent evt) {
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     // mapC.setInteractionMode(MappingComponent.SELECT);
                     mapC.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
                 }
             });
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniOverviewActionPerformed(final java.awt.event.ActionEvent evt) {
         showOrHideView(vOverview);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void formComponentResized(final java.awt.event.ComponentEvent evt) {
         if (this.getExtendedState() != MAXIMIZED_BOTH) {
             oldWindowDimension.setSize(getWidth(), getHeight());
             oldWindowPositionX = (int)this.getLocation().getX();
             oldWindowPositionY = (int)this.getLocation().getY();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mniOptionsActionPerformed(final java.awt.event.ActionEvent evt) {
         final OptionsDialog od = new OptionsDialog(this, true);
         od.setLocationRelativeTo(this);
         od.setVisible(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void popMenSearchPopupMenuWillBecomeVisible(final javax.swing.event.PopupMenuEvent evt) {
         searchMenuSelectedAction.actionPerformed(new ActionEvent(popMenSearch, ActionEvent.ACTION_PERFORMED, null));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void menSearchMenuSelected(final javax.swing.event.MenuEvent evt) {
         searchMenuSelectedAction.actionPerformed(new ActionEvent(menSearch, ActionEvent.ACTION_PERFORMED, null));
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void mnuConfigServerActionPerformed(final java.awt.event.ActionEvent evt) {
         activeLayers.removeAllLayers();
         mapC.getRasterServiceLayer().removeAllChildren();
 
         // mapC.resetWtst();
         configureApp(true);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  evt  DOCUMENT ME!
      */
     private void cmdPluginSearchActionPerformed(final java.awt.event.ActionEvent evt) {
         // TODO add your handling code here:
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  evt DOCUMENT ME!
      */
 // private void addShutdownHook() {
 // ShutdownHook shutdownHook = new ShutdownHook();
 // Runtime.getRuntime().addShutdownHook(shutdownHook);
 //
 // }
     /**
      * DOCUMENT ME!
      *
      * @param  args  the command line arguments
      */
     public static void main(final String[] args) {
 //        final Thread t = new Thread(new Runnable() {
 //
 //                    @Override
 //                    public void run() {
 
         EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     final CismapPlugin cp = new CismapPlugin();
                     // cp.addShutdownHook();
                     cp.setVisible(true);
                 }
             });
 //                    }
 //                });
 //        t.start();
 // cp.configureApp(false);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  serverFirst  DOCUMENT ME!
      */
     private void configureApp(final boolean serverFirst) {
         try {
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         validateTree();
                     }
                 });
         } catch (final Throwable t) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         log.warn("Error in validateTree()", t); // NOI18N
                         validateTree();
                     }
                 });
         }
 
         if (serverFirst) {
             configurationManager.configureFromClasspath();
         } else {
             configurationManager.configure();
         }
 
         setButtonSelectionAccordingToMappingComponent();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Vector<Feature> getAllFeaturesSorted() {
         return featureControl.getAllFeaturesSorted();
     }
 
     @Override
     public PluginUI getUI(final String str) {
         return this;
     }
 
     @Override
     public PluginMethod getMethod(final String str) {
         return pluginMethods.get(str);
     }
 
     @Override
     public void setActive(final boolean param) {
         if (log.isDebugEnabled()) {
             log.debug("setActive:" + param); // NOI18N
         }
 
         if (!param) {
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
 
             // CismapBroker.getInstance().cleanUpSystemRegistry();
             saveLayout(cismapDirectory + fs + pluginLayoutName);
         }
     }
 
     @Override
     public java.util.Iterator getUIs() {
         final LinkedList ll = new LinkedList();
         ll.add(this);
 
         return ll.iterator();
     }
 
     @Override
     public PluginProperties getProperties() {
         return myPluginProperties;
     }
 
     @Override
     public java.util.Iterator getMethods() {
         return this.pluginMethods.values().iterator();
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void shown() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void resized() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void moved() {
     }
 
     /**
      * DOCUMENT ME!
      */
     @Override
     public void hidden() {
     }
 
     @Override
     public java.util.Collection getMenus() {
         return menues;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public String getId() {
         return "cismap"; // NOI18N
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     @Override
     public JComponent getComponent() {
         return panAll;
     }
 
     @Override
     public java.util.Collection getButtons() {
         // return Arrays.asList(this.tobVerdis.getComponents());
         return null;
     }
 
     @Override
     public void floatingStopped() {
     }
 
     @Override
     public void floatingStarted() {
     }
 
     @Override
     public void setVisible(final boolean b) {
         if (!plugin) {
             super.setVisible(b);
         }
     }
 
     @Override
     public void dispose() {
         try {
             if (log.isDebugEnabled()) {
                 log.debug("dispose().CIAO"); // NOI18N
             }
             saveLayout(cismapDirectory + fs + standaloneLayoutName);
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
 
             // CismapBroker.getInstance().cleanUpSystemRegistry();
             super.dispose();
             System.exit(0);
         } catch (Throwable t) {
             log.fatal("Error during disposing frame.", t); // NOI18N
         }
     }
 
     @Override
     public Element getConfiguration() {
         final Element ret = new Element("cismapPluginUIPreferences"); // NOI18N
         final Element window = new Element("window");                 // NOI18N
 
         final int windowHeight = this.getHeight();
         final int windowWidth = this.getWidth();
         final int windowX = (int)this.getLocation().getX();
         final int windowY = (int)this.getLocation().getY();
         final boolean windowMaximised = (this.getExtendedState() == MAXIMIZED_BOTH);
 
         if (windowMaximised) {
             window.setAttribute("height", "" + (int)oldWindowDimension.getHeight()); // NOI18N
             window.setAttribute("width", "" + (int)oldWindowDimension.getWidth());   // NOI18N
             window.setAttribute("x", "" + oldWindowPositionX);                       // NOI18N
             window.setAttribute("y", "" + oldWindowPositionY);                       // NOI18N
         } else {
             window.setAttribute("height", "" + windowHeight);                        // NOI18N
             window.setAttribute("width", "" + windowWidth);                          // NOI18N
             window.setAttribute("x", "" + windowX);                                  // NOI18N
             window.setAttribute("y", "" + windowY);                                  // NOI18N
         }
 
         window.setAttribute("max", "" + windowMaximised); // NOI18N
 
         ret.addContent(window);
 
         // Iterator<View> it=viewport.getViewset().iterator();
 // Iterator<View> it=viewMap.getViewCount());
 // while (it.hasNext()) {
 // View elem = it.next();
 // Element view=new Element("DockingView");
 // view.setAttribute(new Attribute("name",elem.getPersistentId()));
 // //view.setAttribute(new Attribute("shown",new Boolean(elem.isShowing()).toString()));
 // view.setAttribute(new Attribute("shown",new Boolean(elem.isVisible()).toString()));
 // view.setAttribute(new Attribute("height",""+elem.getHeight()));
 // view.setAttribute(new Attribute("width",""+elem.getWidth()));
 // ret.addContent(view);
 // }
         return ret;
     }
 
     @Override
     public void masterConfigure(final Element e) {
         final Element prefs = e.getChild("cismapPluginUIPreferences"); // NOI18N
         cismapPluginUIPreferences = prefs;
 
         try {
             final Element help_url_element = prefs.getChild("help_url");                  // NOI18N
             final Element news_url_element = prefs.getChild("news_url");                  // NOI18N
             final Element httpInterfacePortElement = prefs.getChild("httpInterfacePort"); // NOI18N
 
             try {
                 httpInterfacePort = new Integer(httpInterfacePortElement.getText());
             } catch (Throwable t) {
                 log.warn("httpInterface was not configured. Set default value: " + httpInterfacePort, t); // NOI18N
             }
 
             helpUrl = help_url_element.getText();
             if (log.isDebugEnabled()) {
                 log.debug("helpUrl:" + helpUrl); // NOI18N
             }
 
             newsUrl = news_url_element.getText();
         } catch (Throwable t) {
             log.error("Error while loading the help urls (" + prefs.getChildren() + ")", t); // NOI18N
         }
 
         windows2skip = new Vector<String>();
 
         try {
             final Element windows2SkipElement = e.getChild("skipWindows");                   // NOI18N
             final Iterator<Element> it = windows2SkipElement.getChildren("skip").iterator(); // NOI18N
 
             while (it.hasNext()) {
                 final Element next = it.next();
                 final String id = next.getAttributeValue("windowid"); // NOI18N
                 windows2skip.add(id);
 
                 final View v = viewMap.getView(id);
 
                 if (v != null) {
                     v.close();
                 }
 
                 final JMenuItem menu = viewMenuMap.get(id);
 
                 if (menu != null) {
                     menu.setVisible(false);
                 }
             }
         } catch (Exception x) {
             log.info("No skipWindow Info available or error while reading the configuration", x); // NOI18N
         }
 
         try {
             // Analysieren des FileMenues
             final Vector<Component> before = new Vector<Component>();
             final Vector<Component> after = new Vector<Component>();
             after.add(sepServerProfilesEnd);
 
             final Component[] comps = menFile.getMenuComponents();
             Vector<Component> active = before;
 
             for (final Component comp : comps) {
                 // Component comp=menFile.getMenuComponent(i);
                 if (active != null) {
                     active.add(comp);
                 }
 
                 if ((active == before) && (comp.getName() != null)
                             && comp.getName().trim().equals("sepServerProfilesStart")) { // erster Separator//NOI18N
                     active = null;
                 } else if ((active == null) && (comp.getName() != null)
                             && comp.getName().trim().equals("sepServerProfilesEnd")) {   // zweiter Separator//NOI18N
                     active = after;
                 }
             }
 
             final Vector<JMenuItem> serverProfileItems = new Vector<JMenuItem>();
 
             final Element serverprofiles = e.getChild("serverProfiles");                   // NOI18N
             final Iterator<Element> it = serverprofiles.getChildren("profile").iterator(); // NOI18N
 
             while (it.hasNext()) {
                 final Element next = it.next();
                 final String id = next.getAttributeValue("id");                                 // NOI18N
                 final String sorter = next.getAttributeValue("sorter");                         // NOI18N
                 final String name = next.getAttributeValue("name");                             // NOI18N
                 final String path = next.getAttributeValue("path");                             // NOI18N
                 final String icon = next.getAttributeValue("icon");                             // NOI18N
                 final String descr = next.getAttributeValue("descr");                           // NOI18N
                 final String descrWidth = next.getAttributeValue("descrwidth");                 // NOI18N
                 final String complexDescriptionText = next.getTextTrim();
                 final String complexDescriptionSwitch = next.getAttributeValue("complexdescr"); // NOI18N
 
                 final JMenuItem serverProfileMenuItem = new JMenuItem();
                 serverProfileMenuItem.setText(name);
                 serverProfileMenuItem.addActionListener(new ActionListener() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             try {
                                 ((ActiveLayerModel)mapC.getMappingModel()).removeAllLayers();
                                 configurationManager.configureFromClasspath(path, null);
                                 setButtonSelectionAccordingToMappingComponent();
                             } catch (Throwable ex) {
                                 log.fatal("No ServerProfile", ex); // NOI18N
                             }
                         }
                     });
                 serverProfileMenuItem.setName("ServerProfile:" + sorter + ":" + name); // NOI18N
 
                 if ((complexDescriptionSwitch != null) && complexDescriptionSwitch.equalsIgnoreCase("true")
                             && (complexDescriptionText != null)) {                                       // NOI18N
                     serverProfileMenuItem.setToolTipText(complexDescriptionText);
                 } else if (descrWidth != null) {
                     serverProfileMenuItem.setToolTipText("<html><table width=\"" + descrWidth
                                 + "\" border=\"0\"><tr><td>" + descr + "</p></td></tr></table></html>"); // NOI18N
                 } else {
                     serverProfileMenuItem.setToolTipText(descr);
                 }
 
                 try {
                     serverProfileMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon)));
                 } catch (Exception iconE) {
                     log.warn("Could not create Icon for ServerProfile.", iconE); // NOI18N
                 }
 
                 serverProfileItems.add(serverProfileMenuItem);
             }
 
             Collections.sort(serverProfileItems, new Comparator<JMenuItem>() {
 
                     @Override
                     public int compare(final JMenuItem o1, final JMenuItem o2) {
                         if ((o1.getName() != null) && (o2.getName() != null)) {
                             return o1.getName().compareTo(o2.getName());
                         } else {
                             return 0;
                         }
                     }
                 });
 
             menFile.removeAll();
 
             for (final Component c : before) {
                 menFile.add(c);
             }
 
             for (final JMenuItem jmi : serverProfileItems) {
                 menFile.add(jmi);
             }
 
             for (final Component c : after) {
                 menFile.add(c);
             }
         } catch (Exception x) {
             log.info("No server profile available, or error while cerating analysis.", x); // NOI18N
         }
     }
 
     @Override
     public void configure(final Element e) {
         final Element prefs = e.getChild("cismapPluginUIPreferences"); // NOI18N
         cismapPluginUIPreferences = prefs;
 
         try {
             final Element window = prefs.getChild("window");                      // NOI18N
             final int windowHeight = window.getAttribute("height").getIntValue(); // NOI18N
             final int windowWidth = window.getAttribute("width").getIntValue();   // NOI18N
             final int windowX = window.getAttribute("x").getIntValue();           // NOI18N
             final int windowY = window.getAttribute("y").getIntValue();           // NOI18N
             oldWindowDimension.setSize(windowWidth, windowHeight);
             oldWindowPositionX = windowX;
             oldWindowPositionY = windowY;
 
             final boolean windowMaximised = window.getAttribute("max").getBooleanValue(); // NOI18N
 
 // log.fatal("is EDT?"+EventQueue.isDispatchThread());
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         CismapPlugin.this.setSize(windowWidth, windowHeight);
                         CismapPlugin.this.setLocation(windowX, windowY);
                         mapC.formComponentResized(null);
 
                         if (windowMaximised) {
                             CismapPlugin.this.setExtendedState(MAXIMIZED_BOTH);
                         }
                     }
                 });
         } catch (Throwable t) {
             log.error("Error while loading the sie of the window.", t); // NOI18N
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  file  DOCUMENT ME!
      */
     public void loadLayout(final String file) {
         setupDefaultLayout();
         if (log.isDebugEnabled()) {
             log.debug("Load Layout.. from " + file); // NOI18N
         }
 
         final File layoutFile = new File(file);
 
         if (layoutFile.exists()) {
             if (log.isDebugEnabled()) {
                 log.debug("Layout File exists"); // NOI18N
             }
 
             try {
                 final FileInputStream layoutInput = new FileInputStream(layoutFile);
                 final ObjectInputStream in = new ObjectInputStream(layoutInput);
                 rootWindow.read(in);
                 in.close();
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.DOWN).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
                 if (log.isDebugEnabled()) {
                     /*if (isInit) {
                      * int count = viewMap.getViewCount(); for (int i = 0; i < count; i++) { View curr =
                      * viewMap.getViewAtIndex(i); if (curr.isUndocked()) { curr.dock(); } }}*/
                     log.debug("Loading Layout successfull");                          // NOI18N
                 }
             } catch (IOException ex) {
                 log.error("Layout File IO Exception --> loading default Layout", ex); // NOI18N
 
                 if (isInit) {
                     JOptionPane.showMessageDialog(
                         this,
                         org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.loadLayout(String).JOptionPane.message1"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.loadLayout(String).JOptionPane.title"), // NOI18N
                         JOptionPane.INFORMATION_MESSAGE);
                     setupDefaultLayout();
                 } else {
                     JOptionPane.showMessageDialog(
                         this,
                         org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.loadLayout(String).JOptionPane.message2"), // NOI18N
                         org.openide.util.NbBundle.getMessage(
                             CismapPlugin.class,
                             "CismapPlugin.loadLayout(String).JOptionPane.title"), // NOI18N
                         JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         } else {
             if (isInit) {
                 log.fatal("File does not exist --> default layout (init)");       // NOI18N
                 EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             // UGLY WINNING --> Gefixed durch IDW Version 1.5
                             // setupDefaultLayout();
                             // DeveloperUtil.createWindowLayoutFrame("nach setup1",rootWindow).setVisible(true);
                             setupDefaultLayout();
                             // DeveloperUtil.createWindowLayoutFrame("nach setup2",rootWindow).setVisible(true);
                         }
                     });
             } else {
                 log.fatal("File does not exist)");                               // NOI18N
                 JOptionPane.showMessageDialog(
                     this,
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.loadLayout(String).JOptionPane.message3"), // NOI18N
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.loadLayout(String).JOptionPane.title"),
                     JOptionPane.INFORMATION_MESSAGE);                            // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  file  DOCUMENT ME!
      */
     public void saveLayout(final String file) {
         if (log.isDebugEnabled()) {
             log.debug("Saving Layout.. to " + file, new CurrentStackTrace()); // NOI18N
         }
 
         final File layoutFile = new File(file);
 
         try {
             if (!layoutFile.exists()) {
                 if (log.isDebugEnabled()) {
                     log.debug("Saving Layout.. File does not exit"); // NOI18N
                 }
                 layoutFile.createNewFile();
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("Saving Layout.. File does exit");     // NOI18N
                 }
             }
 
             final FileOutputStream layoutOutput = new FileOutputStream(layoutFile);
             final ObjectOutputStream out = new ObjectOutputStream(layoutOutput);
             rootWindow.write(out);
             out.flush();
             out.close();
             if (log.isDebugEnabled()) {
                 log.debug("Saving Layout.. to " + file + " successfull");      // NOI18N
             }
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(
                 this,
                 org.openide.util.NbBundle.getMessage(
                     CismapPlugin.class,
                     "CismapPlugin.saveLayout(String).JOptionPane.message"),    // NOI18N
                 org.openide.util.NbBundle.getMessage(
                     CismapPlugin.class,
                     "CismapPlugin.saveLayout(String).JOptionPane.title"),      // NOI18N
                 JOptionPane.INFORMATION_MESSAGE);
             log.error("A failure occured during writing the layout file", ex); // NOI18N
         }
     }
 
     @Override
     public void mapSearchStarted(final MapSearchEvent mse) {
         initMetaSearch(mse.getGeometry());
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  geom  DOCUMENT ME!
      */
     private void initMetaSearch(final Geometry geom) {
         String geosuche = "";                                                               // NOI18N
         if (!cidsPureServerSearchEnabled) {
             try {
                 geosuche = context.getEnvironment().getParameter("geosuche");               // NOI18N
             } catch (Exception e) {
                 log.warn("Parameter geosuche not found in plugin.xml, use " + geosuche, e); // NOI18N
             }
 
             final Object object = context.getSearch().getDataBeans().get(geosuche);
             final FormDataBean coordinatesDataBean = (FormDataBean)object;
 
             coordinatesDataBean.setBeanParameter("featureString", geom.toText()); // NOI18N
             context.getSearch()
                     .performSearch(metaSearch.getSearchTree().getSelectedClassNodeKeys(),
                         coordinatesDataBean,
                         context.getUserInterface().getFrameFor((PluginUI)this),
                         false);
         } else {
             log.fatal(metaSearch.getSearchTree().getSelectedClassNodeKeys());
             final GeoSearch gs = new GeoSearch(geom);
             gs.setValidClassesFromStrings(metaSearch.getSearchTree().getSelectedClassNodeKeys());
             CidsSearchExecutor.executeCidsSearchAndDisplayResults(gs);
         }
     }
 
     @Override
     public void dropOnMap(final MapDnDEvent mde) {
         if (log.isDebugEnabled()) {
             log.debug("drop on map"); // NOI18N
         }
 
         if (mde.getDte() instanceof DropTargetDropEvent) {
             final DropTargetDropEvent dtde = (DropTargetDropEvent)mde.getDte();
 
             if (dtde.getTransferable().isDataFlavorSupported(fromCapabilityWidget)) {
                 activeLayers.drop(dtde);
             } else if (dtde.getTransferable().isDataFlavorSupported(fromNavigatorNode)
                         && dtde.getTransferable().isDataFlavorSupported(fromNavigatorCollection)) {
                 // Drop von MetaObjects
                 try {
                     final Object object = dtde.getTransferable().getTransferData(fromNavigatorCollection);
 
                     if (object instanceof Collection) {
                         final Collection c = (Collection)object;
                         showObjectsMethod.invoke(c);
                     }
                 } catch (Throwable t) {
                     log.fatal("Error on drop", t); // NOI18N
                 }
             } else if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)
                         || dtde.isDataFlavorSupported(DnDUtils.URI_LIST_FLAVOR)) {
                 activeLayers.drop((DropTargetDropEvent)mde.getDte());
             } else {
                 JOptionPane.showMessageDialog(
                     this,
                     org.openide.util.NbBundle.getMessage(
                         CismapPlugin.class,
                         "CismapPlugin.dropOnMap(MapDnDEvent).JOptionPane.message")); // NOI18N
                 log.error("Unable to process the datatype." + dtde.getTransferable().getTransferDataFlavors()[0]); // NOI18N
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   c         DOCUMENT ME!
      * @param   editable  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public void showInMap(final Collection c, final boolean editable) throws Exception {
         showObjectsMethod.invoke(c, editable);
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   mo        DOCUMENT ME!
      * @param   editable  DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  Exception  DOCUMENT ME!
      */
     public CidsFeature showInMap(final MetaObject mo, final boolean editable) throws Exception {
         return showObjectsMethod.invoke(mo, editable);
     }
 
     @Override
     public void dragOverMap(final MapDnDEvent mde) {
 // if (mde.getDte() instanceof DropTargetDragEvent) { DropTargetDragEvent dtde=(DropTargetDragEvent)mde.getDte(); if
 // (dtde.getTransferable().isDataFlavorSupported(fromNavigatorNode)&&dtde.getTransferable().isDataFlavorSupported(fromNavigatorCollection))
 // { BufferedImage image=ComponentRegistry.getRegistry().getActiveCatalogue().getDragImage();
 //
 // }
 //
 // }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void setButtonSelectionAccordingToMappingComponent() {
         if (mapC.getInteractionMode().equals(MappingComponent.ZOOM)) {
             if (!cmdZoom.isSelected()) {
                 cmdZoom.setSelected(true);
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.PAN)) {
             if (!cmdPan.isSelected()) {
                 cmdPan.setSelected(true);
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.FEATURE_INFO)) {
             if (!cmdFeatureInfo.isSelected()) {
                 cmdFeatureInfo.setSelected(true);
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.CREATE_SEARCH_POLYGON)) {
             if (!cmdPluginSearch.isSelected()) {
                 cmdPluginSearch.setSelected(true);
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.SELECT)) {
             if (!cmdSelect.isSelected()) {
                 cmdSelect.setSelected(true);
             }
 //        } else if (mapC.getInteractionMode().equals(MappingComponent.LINEMEASUREMENT)) {
 //            if (!cmdMeasurement.isSelected()) {
 //                cmdMeasurement.setSelected(true);
 //            }
         } else if (mapC.getInteractionMode().equals(MappingComponent.NEW_POLYGON)) {
             if (((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(
                             CreateNewGeometryListener.POLYGON)) {
                 if (!cmdNewPolygon.isSelected()) {
                     cmdNewPolygon.setSelected(true);
                 }
             } else if (((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(
                             CreateNewGeometryListener.LINESTRING)) {
                 if (!cmdNewLinestring.isSelected()) {
                     cmdNewLinestring.setSelected(true);
                 }
             } else if (((CreateNewGeometryListener)mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(
                             CreateNewGeometryListener.POINT)) {
                 if (!cmdNewPoint.isSelected()) {
                     cmdNewPoint.setSelected(true);
                 }
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.MOVE_POLYGON)) {
             if (!cmdMoveGeometry.isSelected()) {
                 cmdMoveGeometry.setSelected(true);
             }
         } else if (mapC.getInteractionMode().equals(MappingComponent.REMOVE_POLYGON)) {
             if (!cmdRemoveGeometry.isSelected()) {
                 cmdRemoveGeometry.setSelected(true);
             }
         }
 
         if (mapC.getHandleInteractionMode().equals(MappingComponent.MOVE_HANDLE)) {
             if (!cmdNodeMove.isSelected()) {
                 cmdNodeMove.setSelected(true);
             }
         } else if (mapC.getHandleInteractionMode().equals(MappingComponent.ADD_HANDLE)) {
             if (!cmdNodeAdd.isSelected()) {
                 cmdNodeAdd.setSelected(true);
             }
         } else if (mapC.getHandleInteractionMode().equals(MappingComponent.REMOVE_HANDLE)) {
             if (!cmdNodeRemove.isSelected()) {
                 cmdNodeRemove.setSelected(true);
             }
         } else if (mapC.getHandleInteractionMode().equals(MappingComponent.ROTATE_POLYGON)) {
             if (!cmdNodeRemove.isSelected()) {
                 cmdNodeRotateGeometry.setSelected(true);
             }
         }
 
         if (mapC.isSnappingEnabled()) {
             if (!cmdSnap.isSelected()) {
                 cmdSnap.setSelected(true);
             }
         } else {
             if (cmdSnap.isSelected()) {
                 cmdSnap.setSelected(false);
             }
         }
     }
 
     @Override
     public void statusValueChanged(final StatusEvent e) {
         if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
             // besser nur aufrufen wenn falsch
             setButtonSelectionAccordingToMappingComponent();
         }
     }
 
     @Override
     public void historyChanged() {
         final Vector backPos = mapC.getBackPossibilities();
         final Vector forwPos = mapC.getForwardPossibilities();
 
         if (menHistory != null) {
             menHistory.removeAll();
             menHistory.add(mniBack);
             menHistory.add(mniForward);
             menHistory.add(mniHome);
             menHistory.add(sepBeforePos);
 
             int counter = 0;
 
             int start = 0;
 
             if ((backPos.size() - 10) > 0) {
                 start = backPos.size() - 10;
             }
 
             for (int index = start; index < backPos.size(); ++index) {
                 final Object elem = backPos.get(index);
                 final JMenuItem item = new JMenuItem(elem.toString()); // +" :"+new Integer(backPos.size()-1-index));
 
                 item.setIcon(miniBack);
 
                 final int pos = backPos.size() - 1 - index;
                 item.addActionListener(new ActionListener() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             for (int i = 0; i < pos; ++i) {
                                 mapC.back(false);
                             }
 
                             mapC.back(true);
                         }
                     });
                 menHistory.add(item);
 // if (counter++>15) break;
             }
 
             final JMenuItem currentItem = new JMenuItem(mapC.getCurrentElement().toString());
             currentItem.setEnabled(false);
             currentItem.setIcon(current);
             menHistory.add(currentItem);
             counter = 0;
 
             for (int index = forwPos.size() - 1; index >= 0; --index) {
                 final Object elem = forwPos.get(index);
                 final JMenuItem item = new JMenuItem(elem.toString()); // +":"+new Integer(forwPos.size()-1-index));
 
                 item.setIcon(miniForward);
 
                 final int pos = forwPos.size() - 1 - index;
                 item.addActionListener(new ActionListener() {
 
                         @Override
                         public void actionPerformed(final ActionEvent e) {
                             for (int i = 0; i < pos; ++i) {
                                 mapC.forward(false);
                             }
 
                             mapC.forward(true);
                         }
                     });
 
                 menHistory.add(item);
 
                 if (counter++ > 10) {
                     break;
                 }
             }
 
             menHistory.add(sepAfterPos);
             menHistory.add(mniHistorySidebar);
         }
     }
 
     @Override
     public void historyActionPerformed() {
         log.fatal("historyActionPerformed"); // NOI18N
     }
 
     @Override
     public void forwardStatusChanged() {
         mniForward.setEnabled(mapC.isForwardPossible());
     }
 
     @Override
     public void backStatusChanged() {
         mniBack.setEnabled(mapC.isBackPossible());
     }
 
     @Override
     public void featuresRemoved(final FeatureCollectionEvent fce) {
         for (final Feature feature : fce.getEventFeatures()) {
             final DefaultMetaTreeNode node = featuresInMapReverse.get(feature);
             if (node != null) {
                 featuresInMapReverse.remove(feature);
                 featuresInMap.remove(node);
             }
         }
     }
 
     @Override
     public void featuresChanged(final FeatureCollectionEvent fce) {
     }
 
     @Override
     public void featuresAdded(final FeatureCollectionEvent fce) {
     }
 
     @Override
     public void featureSelectionChanged(final FeatureCollectionEvent fce) {
         if (plugin && !featureCollectionEventBlocker) {
             final Collection<Feature> fc = new Vector<Feature>(mapC.getFeatureCollection().getSelectedFeatures());
             final Vector<DefaultMutableTreeNode> nodeVector = new Vector<DefaultMutableTreeNode>();
 
             for (final Feature f : fc) {
                 if ((f instanceof CidsFeature) || (f instanceof FeatureGroup)) {
 // if (f instanceof CidsFeature) {
                     nodeVector.add(featuresInMapReverse.get(f));
                 }
             }
 
             EventQueue.invokeLater(new Runnable() {
 
                     @Override
                     public void run() {
                         nodeSelectionEventBlocker = true;
 
                         // Baumselektion wird hier propagiert
                         ComponentRegistry.getRegistry().getActiveCatalogue().setSelectedNodes(nodeVector, true);
                         nodeSelectionEventBlocker = false;
                     }
                 });
         }
     }
 
     @Override
     public void featureReconsiderationRequested(final FeatureCollectionEvent fce) {
     }
 
     @Override
     public void allFeaturesRemoved(final FeatureCollectionEvent fce) {
         featuresInMap.clear();
         featuresInMapReverse.clear();
     }
 
     /**
      * DOCUMENT ME!
      */
     private void initHttpServer() {
         try {
             final Thread http = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             try {
                                 Thread.sleep(1500);                             // Bugfix Try Deadlock
                                 if (log.isDebugEnabled()) {
                                     log.debug("Http Interface initialisieren"); // NOI18N
                                 }
 
                                 final Server server = new Server();
                                 final Connector connector = new SelectChannelConnector();
                                 connector.setPort(9098);
                                 server.setConnectors(new Connector[] { connector });
 
                                 final Handler param = new AbstractHandler() {
 
                                         @Override
                                         public void handle(final String target,
                                                 final HttpServletRequest request,
                                                 final HttpServletResponse response,
                                                 final int dispatch) throws IOException, ServletException {
                                             final Request base_request = (request instanceof Request)
                                                 ? (Request)request : HttpConnection.getCurrentConnection().getRequest();
                                             base_request.setHandled(true);
                                             response.setContentType("text/html");                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 // NOI18N
                                             response.setStatus(HttpServletResponse.SC_ACCEPTED);
                                             response.getWriter()
                                                     .println(
                                                         "<html><head><title>HTTP interface</title></head><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"80%\"><tr><td width=\"30%\" align=\"center\" valign=\"middle\"><img border=\"0\" src=\"http://www.cismet.de/images/cismetLogo250M.png\" ><br></td><td width=\"%\">&nbsp;</td><td width=\"50%\" align=\"left\" valign=\"middle\"><font face=\"Arial\" size=\"3\" color=\"#1c449c\">... and <b><font face=\"Arial\" size=\"3\" color=\"#1c449c\">http://</font></b> just works</font><br><br><br></td></tr></table></body></html>"); // NOI18N
                                         }
                                     };
 
                                 final Handler hello = new AbstractHandler() {
 
                                         @Override
                                         public void handle(final String target,
                                                 final HttpServletRequest request,
                                                 final HttpServletResponse response,
                                                 final int dispatch) throws IOException, ServletException {
                                             try {
                                                 if (request.getLocalAddr().equals(request.getRemoteAddr())) {
                                                     log.info("HttpInterface connected"); // NOI18N
 
                                                     if (target.equalsIgnoreCase("/gotoBoundingBox")) { // NOI18N
 
                                                         final String x1 = request.getParameter("x1"); // NOI18N
                                                         final String y1 = request.getParameter("y1"); // NOI18N
                                                         final String x2 = request.getParameter("x2"); // NOI18N
                                                         final String y2 = request.getParameter("y2"); // NOI18N
 
                                                         try {
                                                             final BoundingBox bb = new BoundingBox(
                                                                     new Double(x1),
                                                                     new Double(y1),
                                                                     new Double(x2),
                                                                     new Double(y2));
                                                             mapC.gotoBoundingBoxWithHistory(bb);
                                                         } catch (Exception e) {
                                                             log.warn("gotoBoundingBox failed", e); // NOI18N
                                                         }
                                                     }
 
                                                     if (target.equalsIgnoreCase("/gotoScale")) { // NOI18N
 
                                                         final String x1 = request.getParameter("x1"); // NOI18N
                                                         final String y1 = request.getParameter("y1"); // NOI18N
                                                         final String scaleDenominator = request.getParameter(
                                                                 "scaleDenominator");                  // NOI18N
 
                                                         try {
                                                             final BoundingBox bb = new BoundingBox(
                                                                     new Double(x1),
                                                                     new Double(y1),
                                                                     new Double(x1),
                                                                     new Double(y1));
 
                                                             mapC.gotoBoundingBoxWithHistory(
                                                                 mapC.getScaledBoundingBox(
                                                                     new Double(scaleDenominator).doubleValue(),
                                                                     bb));
                                                         } catch (Exception e) {
                                                             log.warn("gotoBoundingBox failed", e); // NOI18N
                                                         }
                                                     }
 
                                                     if (target.equalsIgnoreCase("/centerOnPoint")) { // NOI18N
 
                                                         final String x1 = request.getParameter("x1"); // NOI18N
                                                         final String y1 = request.getParameter("y1"); // NOI18N
 
                                                         try {
                                                             final BoundingBox bb = new BoundingBox(
                                                                     new Double(x1),
                                                                     new Double(y1),
                                                                     new Double(x1),
                                                                     new Double(y1));
                                                             mapC.gotoBoundingBoxWithHistory(bb);
                                                         } catch (Exception e) {
                                                             log.warn("centerOnPoint failed", e); // NOI18N
                                                         }
                                                     } else {
                                                         log.warn("Unknown target: " + target);   // NOI18N
                                                     }
                                                 } else {
                                                     log.warn(
                                                         "Someone tries to access the http interface from an other computer. Access denied."); // NOI18N
                                                 }
                                             } catch (Throwable t) {
                                                 log.error("Error while handle http requests", t); // NOI18N
                                             }
                                         }
                                     };
 
                                 final HandlerCollection handlers = new HandlerCollection();
                                 handlers.setHandlers(new Handler[] { param, hello });
                                 server.setHandler(handlers);
 
                                 server.start();
                                 server.join();
                             } catch (Throwable t) {
                                 log.error("Error in the HttpInterface of cismap", t); // NOI18N
                             }
                         }
                     });
             http.start();
             if (log.isDebugEnabled()) {
                 log.debug("Initialise HTTP interface");                               // NOI18N
             }
         } catch (Throwable t) {
             log.fatal("Nothing at all", t);                                           // NOI18N
         }
     }
 
     @Override
     public void featureCollectionChanged() {
     }
 
     @Override
     public void update(final Observable o, final Object arg) {
         if (o.equals(mapC.getMemUndo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdUndo.isEnabled()) {
                 if (log.isDebugEnabled()) {
                     log.debug("activate UNDO button"); // NOI18N
                 }
                 EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             cmdUndo.setEnabled(true);
                         }
                     });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdUndo.isEnabled()) {
                 if (log.isDebugEnabled()) {
                     log.debug("deactivate UNDO button"); // NOI18N
                 }
                 EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             cmdUndo.setEnabled(false);
                         }
                     });
             }
         } else if (o.equals(mapC.getMemRedo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdRedo.isEnabled()) {
                 if (log.isDebugEnabled()) {
                     log.debug("activate REDO button"); // NOI18N
                 }
                 EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             cmdRedo.setEnabled(true);
                         }
                     });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdRedo.isEnabled()) {
                 if (log.isDebugEnabled()) {
                     log.debug("deactivate REDO button"); // NOI18N
                 }
                 EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             cmdRedo.setEnabled(false);
                         }
                     });
             }
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * private void configureActiveTabAfterVisibility() { Iterator<Element>
      * it=cismapPluginUIPreferences.getChildren("DockingView").iterator(); while (it.hasNext()) { Element elem =
      * it.next(); String name=elem.getAttribute("name").getValue(); boolean shown=false; try {
      * shown=elem.getAttribute("shown").getBooleanValue(); } catch (Exception ex) { } if (viewsHM.get(name)!=null) { try
      * { int height=elem.getAttribute("height").getIntValue(); int width=elem.getAttribute("width").getIntValue();
      * viewsHM.get(name).setPreferredSize(new Dimension(width,height)); } catch(Throwable t) { log.warn("Fehler beim
      * setzten von der preferredSize der Views.",t); } } if (shown&&viewsHM.get(name)!=null) {
      * viewsHM.get(name).setActive(true); } } geht nicht... :-( aber warum ??? vMap.setActive(true); }
      *
      * @author   $author$
      * @version  $Revision$, $Date$
      */
     class ShutdownHook extends Thread {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void run() {
             if (log.isDebugEnabled()) {
                 log.debug("CIAO");                           // NOI18N
             }
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
             if (log.isDebugEnabled()) {
                 log.debug("Shutdownhook --> saving layout"); // NOI18N
             }
             saveLayout(cismapDirectory + fs + standaloneLayoutName);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author   $author$
      * @version  $Revision$, $Date$
      */
     private class NodeChangeListener extends MetaNodeSelectionListener {
 
         //~ Instance fields ----------------------------------------------------
 
         private final SingleAttributeIterator attributeIterator;
         private final Collection classNames;
         private final Collection attributeNames;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new NodeChangeListener object.
          */
         private NodeChangeListener() {
             this.classNames = context.getEnvironment().getAttributeMappings("className");         // NOI18N
             this.attributeNames = context.getEnvironment().getAttributeMappings("attributeName"); // NOI18N
 
             if (this.attributeNames.size() == 0) {
                 this.attributeNames.add("id"); // NOI18N
             }
 
             final AttributeRestriction attributeRestriction = new ComplexAttributeRestriction(
                     AttributeRestriction.OBJECT,
                     AttributeRestriction.IGNORE,
                     null,
                     this.attributeNames,
                     null);
             this.attributeIterator = new SingleAttributeIterator(attributeRestriction, false);
         }
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         protected void nodeSelectionChanged(
                 final Collection wirdNichtGebrauchtWeilScheissevonPascalgefuelltCollection) {
             if (!nodeSelectionEventBlocker) {
                 try {
                     final Collection c = context.getMetadata().getSelectedNodes();
 
                     if ((c != null) && (c.size() != 0)) {
                         if (featureControl.isWizardMode()) {
                             showObjectsMethod.invoke(c);
                         } else {
                             final Object[] nodes = c.toArray();
                             boolean oneHit = false;
                             final Vector<Feature> features = new Vector<Feature>();
 
                             for (final Object o : nodes) {
                                 if (o instanceof DefaultMetaTreeNode) {
                                     final DefaultMetaTreeNode node = (DefaultMetaTreeNode)o;
 
                                     if (featuresInMap.containsKey(node)) {
                                         oneHit = true;
                                         features.add(featuresInMap.get(node));
                                     }
                                 }
                             }
 
                             if (oneHit) {
                                 featureCollectionEventBlocker = true;
                                 mapC.getFeatureCollection().select(features);
                                 featureCollectionEventBlocker = false;
                             } else {
                                 featureCollectionEventBlocker = true;
                                 mapC.getFeatureCollection().unselectAll();
                                 featureCollectionEventBlocker = false;
                                 if (log.isDebugEnabled()) {
                                     log.debug("featuresInMap:" + featuresInMap); // NOI18N
                                 }
                             }
                         }
                     }
                 } catch (Throwable t) {
                     log.error("Error in WizardMode:", t);                        // NOI18N
                 }
             }
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author   $author$
      * @version  $Revision$, $Date$
      */
     private class ShowObjectsMethod implements PluginMethod {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public void invoke() throws Exception {
             final Collection selectedNodes = context.getMetadata().getSelectedNodes();
             invoke(selectedNodes);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   nodes  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         public void invoke(final Collection nodes) throws Exception {
             invoke(nodes, false);
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   mo        DOCUMENT ME!
          * @param   editable  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         public synchronized CidsFeature invoke(final MetaObject mo, final boolean editable) throws Exception {
             final CidsFeature cidsFeature = new CidsFeature(mo);
             invoke(cidsFeature, editable);
 
             return cidsFeature;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   cidsFeature  DOCUMENT ME!
          * @param   editable     DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         private void invoke(final CidsFeature cidsFeature, final boolean editable) throws Exception {
             final Vector<Feature> v = new Vector<Feature>();
             cidsFeature.setEditable(editable);
             v.add(cidsFeature);
             if (log.isDebugEnabled()) {
                 log.debug("mapC.getFeatureCollection().getAllFeatures():"
                             + mapC.getFeatureCollection().getAllFeatures());                       // NOI18N
             }
             if (log.isDebugEnabled()) {
                 log.debug("cidsFeature:" + cidsFeature);                                           // NOI18N
                 log.debug("mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature):"
                             + mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)); // NOI18N
             }
             mapC.getFeatureLayer().setVisible(true);
 // if (mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)) {
             mapC.getFeatureCollection().removeFeature(cidsFeature);
             if (log.isDebugEnabled()) {
                 log.debug("mapC.getFeatureCollection().getAllFeatures():"
                             + mapC.getFeatureCollection().getAllFeatures());                       // NOI18N
             }
 
             // }
             mapC.getFeatureCollection().substituteFeatures(v);
 
             if (editable) {
                 // mapC.getFeatureCollection().holdFeature(cidsFeature);
                 mapC.getFeatureCollection().select(v);
             }
 
             if (!mapC.isFixedMapExtent()) {
                 mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
                 mapC.showHandles(true);
             }
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param   node      DOCUMENT ME!
          * @param   oAttr     DOCUMENT ME!
          * @param   editable  DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         public synchronized CidsFeature invoke(final DefaultMetaTreeNode node,
                 final ObjectAttribute oAttr,
                 final boolean editable) throws Exception {
 // if (oAttr!=null&&oAttr.getValue()==null&&editable==true) {
 // return null;
 // }
             final MetaObject loader = ((ObjectTreeNode)node).getMetaObject();
             final MetaObjectNode mon = ((ObjectTreeNode)node).getMetaObjectNode();
             CidsFeature cidsFeature = invoke(loader, editable);
 
             if (oAttr != null) {
                 cidsFeature = new CidsFeature(mon, oAttr);
             } else {
                 cidsFeature = new CidsFeature(mon);
             }
 
             featuresInMap.put(node, cidsFeature);
             featuresInMapReverse.put(cidsFeature, node);
             invoke(cidsFeature, editable);
 
             return cidsFeature;
         }
 // public synchronized CidsFeature invoke(DefaultMetaTreeNode node, ObjectAttribute oAttr, boolean editable) throws
 // Exception { //            if (oAttr!=null&&oAttr.getValue()==null&&editable==true) { //                return null;
 // //            } Vector<Feature> v = new Vector<Feature>(); MetaObject loader = ((ObjectTreeNode)
 // node).getMetaObject(); MetaObjectNode mon = ((ObjectTreeNode) node).getMetaObjectNode(); CidsFeature cidsFeature; if
 // (oAttr != null) { cidsFeature = new CidsFeature(mon, oAttr); } else { cidsFeature = new CidsFeature(mon); }
 // cidsFeature.setEditable(editable); v.add(cidsFeature); featuresInMap.put(node, cidsFeature);
 // featuresInMapReverse.put(cidsFeature, node); log.debug("mapC.getFeatureCollection().getAllFeatures():" +
 // mapC.getFeatureCollection().getAllFeatures()); log.debug("cidsFeature:" + cidsFeature);
 // log.debug("mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature):" +
 // mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)); mapC.getFeatureLayer().setVisible(true); // if
 // (mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)) {
 // mapC.getFeatureCollection().removeFeature(cidsFeature); log.debug("mapC.getFeatureCollection().getAllFeatures():" +
 // mapC.getFeatureCollection().getAllFeatures()); //          } mapC.getFeatureCollection().substituteFeatures(v); if
 // (editable) { //mapC.getFeatureCollection().holdFeature(cidsFeature); mapC.getFeatureCollection().select(v); } if
 // (!mapC.isFixedMapExtent()) { mapC.zoomToFeatureCollection(mapC.isFixedMapScale()); mapC.showHandles(true); } return
 // cidsFeature; }
 
         /**
          * DOCUMENT ME!
          *
          * @param   nodes     DOCUMENT ME!
          * @param   editable  DOCUMENT ME!
          *
          * @throws  Exception  DOCUMENT ME!
          */
         public synchronized void invoke(final Collection<DefaultMetaTreeNode> nodes, final boolean editable)
                 throws Exception {
             log.info("invoke shows objects in the map"); // NOI18N
 
             final Runnable showWaitRunnable = new Runnable() {
 
                     @Override
                     public void run() {
                         showObjectsWaitDialog.setLocationRelativeTo(CismapPlugin.this);
                         showObjectsWaitDialog.setVisible(true);
 
                         final SwingWorker<Vector<Feature>, Void> addToMapWorker =
                             new SwingWorker<Vector<Feature>, Void>() {
 
                                 @Override
                                 protected Vector<Feature> doInBackground() throws Exception {
                                     final Iterator<DefaultMetaTreeNode> mapIter = featuresInMap.keySet().iterator();
 
                                     while (mapIter.hasNext()) {
                                         final DefaultMetaTreeNode node = mapIter.next();
                                         final Feature f = featuresInMap.get(node);
 
                                         if (!mapC.getFeatureCollection().isHoldFeature(f)) {
                                             mapIter.remove();
                                             featuresInMapReverse.remove(f);
                                         }
                                     }
 
                                     final Vector<Feature> v = new Vector<Feature>();
 
                                     for (final DefaultMetaTreeNode node : nodes) {
                                         final MetaObjectNode mon = ((ObjectTreeNode)node).getMetaObjectNode();
                                         MetaObject mo = mon.getObject();
 
                                         if (mo == null) {
                                             mo = ((ObjectTreeNode)node).getMetaObject();
                                         }
 
                                         final CidsFeature cidsFeature = new CidsFeature(mo);
                                         cidsFeature.setEditable(editable);
 
                                         final List<Feature> allFeaturesToAdd = TypeSafeCollections.newArrayList(
                                                 FeatureGroups.expandAll(cidsFeature));
                                         if (log.isDebugEnabled()) {
                                             log.debug("allFeaturesToAdd:" + allFeaturesToAdd); // NOI18N
                                         }
 
                                         // log.fatal("cidsFeature.hashCode():"+cidsFeature.hashCode());
                                         // log.fatal("feturesInMap:"+featuresInMap);
 
 // log.fatal("featuresInMap.containsValue(cidsFeature):"+featuresInMap.containsValue(cidsFeature));
                                         if (!(featuresInMap.containsValue(cidsFeature))) {
                                             v.addAll(allFeaturesToAdd);
 
                                             // node -> masterfeature
                                             featuresInMap.put(node, cidsFeature);
 
                                             for (final Feature feature : allFeaturesToAdd) {
                                                 // master and all subfeatures -> node
                                                 featuresInMapReverse.put(feature, node);
                                             }
                                             if (log.isDebugEnabled()) {
 // featuresInMap.put(node, cidsFeature);
 // featuresInMapReverse.put(cidsFeature, node);
                                                 log.debug("featuresInMap.put(node,cidsFeature):" + node + ","
                                                             + cidsFeature); // NOI18Ns
                                             }
 
 // log.fatal("feturesInMap:"+featuresInMap);
 // log.fatal("featuresInMapReverse:"+featuresInMapReverse);
                                         }
                                     }
 
                                     return v;
                                 }
 
 // private Feature getFeatureParent(Feature feature) {
 // if (feature instanceof SubFeature) {
 // SubFeature current = (SubFeature) feature;
 // if (current.getParentFeature() != null) {
 // return getFeatureParent(current.getParentFeature());
 // }
 // }
 // return feature;
 // }
                                 @Override
                                 protected void done() {
                                     try {
                                         showObjectsWaitDialog.setVisible(false);
 // showObjectsWaitDialog.dispose();
                                         final Vector<Feature> v = get();
 
                                         mapC.getFeatureLayer().setVisible(true);
                                         mapC.getFeatureCollection().substituteFeatures(v);
 
                                         if (!mapC.isFixedMapExtent()) {
                                             mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
                                         }
                                     } catch (InterruptedException e) {
                                         if (log.isDebugEnabled()) {
                                             log.debug(e, e);
                                         }
                                     } catch (Exception e) {
                                         log.error("Error while displaying objects:", e); // NOI18N
                                     }
                                 }
                             };
                         CismetThreadPool.execute(addToMapWorker);
                     }
                 };
 
             if (EventQueue.isDispatchThread()) {
                 showWaitRunnable.run();
             } else {
                 EventQueue.invokeLater(showWaitRunnable);
             }
         }
 
         @Override
         public String getId() {
             return this.getClass().getName();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @author   $author$
      * @version  $Revision$, $Date$
      */
     class MyPluginProperties implements PluginProperties {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public Object getProperty(final String propertyName) {
             if (log.isDebugEnabled()) {
                 log.debug("GetProperty was invoked from CismapPlugin"); // NOI18N
             }
 
             if (propertyName.equalsIgnoreCase("coordinate")) { // NOI18N
 
                 // hier erwartet der Navigator ein double[][] mit  allen Punkten
                 final double[][] pointCoordinates = new double[4][2];
 
                 // x1 y1
                 pointCoordinates[0][0] = mapC.getCurrentBoundingBox().getX1();
                 pointCoordinates[0][1] = mapC.getCurrentBoundingBox().getY1();
 
                 // x2 y1
                 pointCoordinates[1][0] = mapC.getCurrentBoundingBox().getX2();
                 pointCoordinates[1][1] = mapC.getCurrentBoundingBox().getY1();
 
                 // x2 y2
                 pointCoordinates[2][0] = mapC.getCurrentBoundingBox().getX2();
                 pointCoordinates[2][1] = mapC.getCurrentBoundingBox().getY2();
 
                 // x1 y2
                 pointCoordinates[3][0] = mapC.getCurrentBoundingBox().getX1();
                 pointCoordinates[3][1] = mapC.getCurrentBoundingBox().getY2();
 
                 return pointCoordinates;
             } else if (propertyName.equalsIgnoreCase("coordinateString")) { // NOI18N
                 return "("                                                  // NOI18N
                             + mapC.getCurrentBoundingBox().getX1() + ","    // NOI18N
                             + mapC.getCurrentBoundingBox().getX1() + ") ("  // NOI18N
                             + mapC.getCurrentBoundingBox().getX2() + ","    // NOI18N
                             + mapC.getCurrentBoundingBox().getX2() + ") ("  // NOI18N
                             + mapC.getCurrentBoundingBox().getX2() + ","    // NOI18N
                             + mapC.getCurrentBoundingBox().getY2() + ") ("  // NOI18N
                             + mapC.getCurrentBoundingBox().getX1() + ","    // NOI18N
                             + mapC.getCurrentBoundingBox().getY2() + ")";   // NOI18N
 
 // mapC.getCurrentBoundingBox().getGeometryFromTextCompatibleString()
             } else if (propertyName.equalsIgnoreCase("ogcFeatureString")) { // NOI18N
                 mapC.getCurrentBoundingBox().getGeometryFromTextCompatibleString();
             }
 
             return null;
         }
 
         @Override
         public void setProperty(final String propertyName, final Object value) {
         }
 
         @Override
         public void addPropertyChangeListener(final PropertyChangeListener listener) {
         }
 
         @Override
         public void addPropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
         }
 
         @Override
         public void removePropertyChangeListener(final PropertyChangeListener listener) {
         }
 
         @Override
         public void removePropertyChangeListener(final String propertyName, final PropertyChangeListener listener) {
         }
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @author   $author$
  * @version  $Revision$, $Date$
  */
 class GeoLinkUrl implements Transferable {
 
     //~ Instance fields --------------------------------------------------------
 
     String url;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new GeoLinkUrl object.
      *
      * @param  url  DOCUMENT ME!
      */
     public GeoLinkUrl(final String url) {
         this.url = url;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[] { DataFlavor.stringFlavor };
     }
 
     @Override
     public boolean isDataFlavorSupported(final DataFlavor flavor) {
         return DataFlavor.stringFlavor.equals(flavor);
     }
 
     @Override
     public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
         if (!DataFlavor.stringFlavor.equals(flavor)) {
             throw (new UnsupportedFlavorException(flavor));
         }
 
         return url;
     }
 }
 
 /**
  * DOCUMENT ME!
  *
  * @author   $author$
  * @version  $Revision$, $Date$
  */
 class ImageSelection implements Transferable {
 
     //~ Instance fields --------------------------------------------------------
 
     private Image image;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ImageSelection object.
      *
      * @param  image  DOCUMENT ME!
      */
     public ImageSelection(final Image image) {
         this.image = image;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     // Returns supported flavors
     @Override
     public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[] { DataFlavor.imageFlavor };
     }
 
     // Returns true if flavor is supported
     @Override
     public boolean isDataFlavorSupported(final DataFlavor flavor) {
         return DataFlavor.imageFlavor.equals(flavor);
     }
 
     // Returns image
     @Override
     public Object getTransferData(final DataFlavor flavor) throws UnsupportedFlavorException, IOException {
         if (!DataFlavor.imageFlavor.equals(flavor)) {
             throw (new UnsupportedFlavorException(flavor));
         }
 
         return image;
     }
 }
