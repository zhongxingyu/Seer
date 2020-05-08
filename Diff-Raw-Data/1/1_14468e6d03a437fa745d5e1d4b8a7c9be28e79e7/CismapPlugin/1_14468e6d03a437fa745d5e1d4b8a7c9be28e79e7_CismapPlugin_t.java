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
 import Sirius.navigator.search.dynamic.FormDataBean;
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
 import com.jgoodies.looks.HeaderStyle;
 import com.jgoodies.looks.Options;
 import com.jgoodies.looks.plastic.Plastic3DLookAndFeel;
 import com.vividsolutions.jts.geom.Geometry;
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.RestrictedFileSystemView;
 import de.cismet.cismap.commons.debug.DebugPanel;
 import de.cismet.cismap.commons.features.DefaultFeatureCollection;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.features.FeatureCollectionEvent;
 import de.cismet.cismap.commons.features.FeatureCollectionListener;
 import de.cismet.cismap.commons.features.PureNewFeature;
 import de.cismet.cismap.commons.features.SearchFeature;
 import de.cismet.cismap.commons.gui.ClipboardWaitDialog;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.gui.about.AboutDialog;
 import de.cismet.cismap.commons.gui.capabilitywidget.CapabilityWidget;
 import de.cismet.cismap.commons.gui.featurecontrolwidget.FeatureControl;
 import de.cismet.cismap.commons.gui.featureinfowidget.FeatureInfoWidget;
 import de.cismet.cismap.commons.gui.infowidgets.LayerInfo;
 import de.cismet.cismap.commons.gui.infowidgets.Legend;
 import de.cismet.cismap.commons.gui.infowidgets.ServerInfo;
 import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
 import de.cismet.cismap.commons.gui.layerwidget.LayerWidget;
 //import de.cismet.cismap.commons.gui.overviewwidget.OverviewWidget;
 import de.cismet.cismap.commons.gui.overviewwidget.OverviewComponent;
 import de.cismet.cismap.commons.gui.piccolo.PFeature;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateSearchGeometryListener;
 import de.cismet.cismap.commons.gui.printing.Scale;
 import de.cismet.cismap.commons.gui.statusbar.StatusBar;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.commons.interaction.MapDnDListener;
 import de.cismet.cismap.commons.interaction.MapSearchListener;
 import de.cismet.cismap.commons.interaction.StatusListener;
 import de.cismet.cismap.commons.interaction.events.MapDnDEvent;
 import de.cismet.cismap.commons.interaction.events.MapSearchEvent;
 import de.cismet.cismap.commons.interaction.events.StatusEvent;
 import de.cismet.cismap.commons.gui.piccolo.eventlistener.actions.CustomAction;
 import de.cismet.cismap.commons.interaction.memento.MementoInterface;
 import de.cismet.cismap.commons.wfsforms.AbstractWFSForm;
 import de.cismet.cismap.commons.wfsforms.WFSFormFactory;
 import de.cismet.extensions.timeasy.TimEasyDialog;
 import de.cismet.extensions.timeasy.TimEasyEvent;
 import de.cismet.extensions.timeasy.TimEasyListener;
 import de.cismet.extensions.timeasy.TimEasyPureNewFeature;
 import de.cismet.lookupoptions.gui.OptionsClient;
 import de.cismet.lookupoptions.gui.OptionsDialog;
 import de.cismet.tools.CurrentStackTrace;
 import de.cismet.tools.StaticDebuggingTools;
 import de.cismet.tools.StaticDecimalTools;
 import de.cismet.tools.configuration.Configurable;
 import de.cismet.tools.configuration.ConfigurationManager;
 import de.cismet.tools.groovysupport.GroovierConsole;
 import de.cismet.tools.gui.CheckThreadViolationRepaintManager;
 import de.cismet.tools.gui.EventDispatchThreadHangMonitor;
 
 import de.cismet.tools.gui.JPopupMenuButton;
 import de.cismet.tools.gui.StackedBox;
 import de.cismet.tools.gui.Static2DTools;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.historybutton.HistoryModelListener;
 import de.cismet.tools.gui.historybutton.JHistoryButton;
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 import edu.umd.cs.piccolo.util.PBounds;
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
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Locale;
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
 import javax.swing.filechooser.FileFilter;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
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
 
 /**
  *
  * @author  thorsten.hell@cismet.de
  */
 public class CismapPlugin extends javax.swing.JFrame implements PluginSupport, Observer,
         FloatingPluginUI, Configurable, MapSearchListener, MapDnDListener,
         StatusListener, HistoryModelListener, FeatureCollectionListener {
 
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
     private View vLayers, vCaps, vServerInfo, vLayerInfo, vMap, vLegend, vMetaSearch, vFeatureInfo, vFeatureControl, vDebug, vGroovy, vOverview;
     //private Viewport viewport;
     //private HashMap<View,DockingState> dockingStates=new HashMap<View,DockingState>();
     private RootWindow rootWindow;
     private StringViewMap viewMap = new StringViewMap();
     private HashMap<String, JMenuItem> viewMenuMap = new HashMap<String, JMenuItem>();
     private final ConfigurationManager configurationManager = new ConfigurationManager();
     private ShowObjectsMethod showObjectsMethod = new ShowObjectsMethod();
     private HashMap<String, PluginMethod> pluginMethods = new HashMap<String, PluginMethod>();
     private final MyPluginProperties myPluginProperties = new MyPluginProperties();
     private ArrayList<JMenuItem> menues = new ArrayList<JMenuItem>();
     private HashMap<DefaultMetaTreeNode, CidsFeature> featuresInMap = new HashMap<DefaultMetaTreeNode, CidsFeature>();
     private HashMap<CidsFeature, DefaultMetaTreeNode> featuresInMapReverse = new HashMap<CidsFeature, DefaultMetaTreeNode>();
     private String newGeometryMode = CreateNewGeometryListener.LINESTRING;
     private WFSFormFactory wfsFormFactory = WFSFormFactory.getInstance(mapC);
     private Set<View> wfsFormViews = new HashSet<View>();
     private Vector<View> wfs = new Vector<View>();
     private DockingWindow[] wfsViews;
     private DockingWindow[] legendTab = new DockingWindow[4];
     private ClipboardWaitDialog clipboarder;
     private PluginContext context;
     private boolean plugin = false;
     //private HashMap<String,View> viewsHM=new HashMap<String,View>(8);
     private String home = System.getProperty("user.home");
     private String fs = System.getProperty("file.separator");
     private String standaloneLayoutName = "cismap.layout";
     private String pluginLayoutName = "plugin.layout";
     private ShowObjectsWaitDialog showObjectsWaitDialog;
     private String cismapDirectory = home + fs + ".cismap";
     private javax.swing.ImageIcon miniBack = new javax.swing.ImageIcon(getClass().getResource("/images/miniBack.png"));
     private javax.swing.ImageIcon miniForward = new javax.swing.ImageIcon(getClass().getResource("/images/miniForward.png"));
     private javax.swing.ImageIcon current = new javax.swing.ImageIcon(getClass().getResource("/images/current.png"));
     private javax.swing.ImageIcon logo = new javax.swing.ImageIcon(getClass().getResource("/images/cismetlogo16.png"));
     private AppletContext appletContext;
     private boolean isInit = true;
     private String helpUrl;
     private String newsUrl;
     private final static String VERSION = "cismapPlugin.jar Version:2 ($Date: 2009-09-04 15:36:19 $(+1) $Revision: 1.1.1.1.2.1 $";
     private AboutDialog about;
     int httpInterfacePort = 9098;
     boolean nodeSelectionEventBlocker = false;
     boolean featureCollectionEventBlocker = false;
     DataFlavor fromCapabilityWidget = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType, "SelectionAndCapabilities");
     DataFlavor fromNavigatorNode = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + DefaultMetaTreeNode.class.getName(), "a DefaultMetaTreeNode");
     DataFlavor fromNavigatorCollection = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=" + java.util.Collection.class.getName(), "a java.util.Collection of Sirius.navigator.types.treenode.DefaultMetaTreeNode objects");
     private OverviewComponent overviewComponent = null;
     private Dimension oldWindowDimension = new Dimension(-1, -1);
     private int oldWindowPositionX = -1;
     private int oldWindowPositionY = -1;
     private String dirExtension = "";
     private Element cismapPluginUIPreferences;
     private Vector<String> windows2skip;
 
     private Action searchMenuSelectedAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchMenuSelectedAction");
                     CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                     PureNewFeature lastGeometry = searchListener.getLastSearchFeature();
                     if (lastGeometry == null) {
                         log.debug("null");
                         mniRedo.setIcon(null);
                         mniRedo.setEnabled(false);
                         mniBuffer.setEnabled(false);
                     } else {
                         switch (lastGeometry.getGeometryType()) {
                             case ELLIPSE:
                                 mniRedo.setIcon(mniSearchEllipse.getIcon());
                                 break;
                             case LINESTRING:
                                 mniRedo.setIcon(mniSearchPolyline.getIcon());
                                 break;
                             case POLYGON:
                                 mniRedo.setIcon(mniSearchPolygon.getIcon());
                                 break;
                             case RECTANGLE:
                                 mniRedo.setIcon(mniSearchRectangle.getIcon());
                                 break;
                         }
                         mniRedo.setEnabled(true);
                         mniBuffer.setEnabled(true);
                     }
 
                     // kopieren nach popupmenu im grünen M
                     mniSearchRectangle1.setSelected(mniSearchRectangle.isSelected());
                     mniSearchPolygon1.setSelected(mniSearchPolygon.isSelected());
                     mniSearchEllipse1.setSelected(mniSearchEllipse.isSelected());
                     mniSearchPolyline1.setSelected(mniSearchPolyline.isSelected());
                     mniRedo1.setIcon(mniRedo.getIcon());
                     mniRedo1.setEnabled(mniRedo.isEnabled());
                     mniBuffer1.setEnabled(mniBuffer.isEnabled());
                 }
             });
         }
     };
     private Action searchAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchAction");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                             if (mniSearchRectangle.isSelected()) {
                                 ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.RECTANGLE);
                             } else if (mniSearchPolygon.isSelected()) {
                                 ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.POLYGON);
                             } else if (mniSearchEllipse.isSelected()) {
                                 ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.ELLIPSE);
                             } else if (mniSearchPolyline.isSelected()) {
                                 ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.LINESTRING);
                             }
                         }
                     });
                 }
             });
         }
     };
     private Action searchRectangleAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchRectangleAction");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     mniSearchRectangle.setSelected(true);
                     cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png")));
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                             ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.RECTANGLE);
                         }
                     });
                 }
             });
         }
     };
     private Action searchPolygonAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchPolygonAction");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     mniSearchPolygon.setSelected(true);
                     cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchPolygon.png")));
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                             ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.POLYGON);
                         }
                     });
                 }
             });
         }
     };
     private Action searchEllipseAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchEllipseAction");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     mniSearchEllipse.setSelected(true);
                     cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchEllipse.png")));
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                             ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.ELLIPSE);
                         }
                     });
                 }
             });
         }
     };
     private Action searchPolylineAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("searchPolylineAction");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     mniSearchPolyline.setSelected(true);
                     cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchPolyline.png")));
 
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             mapC.setInteractionMode(MappingComponent.CREATE_SEARCH_POLYGON);
                             ((CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON)).setMode(CreateSearchGeometryListener.LINESTRING);
                         }
                     });
                 }
             });
         }
     };
     private Action searchRedoAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("redoSearchAction");
                     CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                     searchListener.repeatLastSearch();
                 }
             });
         }
     };
     private Action searchBufferAction = new AbstractAction() {
 
         @Override
         public void actionPerformed(ActionEvent e) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     log.debug("bufferSearchGeometry");
                     cmdGroupPrimaryInteractionMode.setSelected(cmdPluginSearch.getModel(), true);
                     EventQueue.invokeLater(new Runnable() {
 
                         @Override
                         public void run() {
                             String s = (String) JOptionPane.showInputDialog(
                                     null,
                                     "Geben Sie den Abstand des zu erzeugenden\n" +
                                     "Puffers der letzten Suchgeometrie an.",
                                     "Puffer",
                                     JOptionPane.PLAIN_MESSAGE,
                                     null,
                                     null,
                                     "");
                             log.debug(s);
 
                             // , statt . ebenfalls erlauben
                             if (s.matches("\\d*,\\d*")) {
                                 s.replace(",", ".");
                             }
 
                             try {
                                 float buffer = Float.valueOf(s);
 
                                 CreateSearchGeometryListener searchListener = (CreateSearchGeometryListener) mapC.getInputListener(MappingComponent.CREATE_SEARCH_POLYGON);
                                 PureNewFeature lastFeature = searchListener.getLastSearchFeature();
 
                                 if (lastFeature != null) {
                                     // Geometrie-Daten holen
                                     Geometry geom = lastFeature.getGeometry();
                                     // Puffer-Geometrie holen
                                     Geometry bufferGeom = geom.buffer(buffer);
                                     // und setzen
                                     lastFeature.setGeometry(bufferGeom);
 
                                     // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder ähnliches mehr)
                                     lastFeature.setGeometryType(PureNewFeature.geomTypes.POLYGON);
 
                                     for (Object feature : mapC.getFeatureCollection().getAllFeatures()) {
 
                                         PFeature sel = (PFeature) mapC.getPFeatureHM().get(feature);
                                         if (sel.getFeature().equals(lastFeature)) {
                                             // Koordinaten der Puffer-Geometrie als Feature-Koordinaten setzen
                                             sel.setCoordArr(bufferGeom.getCoordinates());
 
                                             // refresh
                                             sel.syncGeometry();
                                             Vector v = new Vector();
                                             v.add(sel.getFeature());
                                             ((DefaultFeatureCollection) mapC.getFeatureCollection()).fireFeaturesChanged(v);
                                         }
                                     }
 
                                     searchListener.performSearch(lastFeature);
                                 }
 
 //                for (Object feature : mapC.getFeatureCollection().getSelectedFeatures()) {
 //                    PFeature sel = (PFeature)mapC.getPFeatureHM().get(feature);
 //                    if (sel.getFeature() instanceof SearchFeature) {
 //                        // Geometrie-Daten holen
 //                        Geometry geom = ((SearchFeature)sel.getFeature()).getGeometry();
 //                        // Puffer-Geometrie holen
 //                        Geometry bufferGeom = geom.buffer(buffer);
 //
 //                        // Koordinaten der Puffer-Geometrie als Feature-Koordinaten setzen
 //                        sel.setCoordArr(bufferGeom.getCoordinates());
 //
 //                        // Geometrie ist jetzt eine Polygon (keine Linie, Ellipse, oder ähnliches mehr)
 //                        ((PureNewFeature)sel.getFeature()).setGeometryType(PureNewFeature.geomTypes.POLYGON);
 //
 //                        // refresh
 //                        sel.syncGeometry();
 //                        Vector v = new Vector();
 //                        v.add(sel.getFeature());
 //                        ((DefaultFeatureCollection) mapC.getFeatureCollection()).fireFeaturesChanged(v);
 //                    }
 //                }
                             } catch (NumberFormatException ex) {
                                 JOptionPane.showMessageDialog(null, "Der eingegebene Wert entsprach nicht einer Fließkommazahl!", "Fehler", JOptionPane.ERROR_MESSAGE);
                             } catch (Exception ex) {
                                 log.debug("", ex);
                             }
                         }
                     });
                 }
             });
         }
     };
 
     public CismapPlugin() {
         this(null);
     }
 
     public CismapPlugin(final PluginContext context) {
         try {
             String l = System.getProperty("user.language");
             String c = System.getProperty("user.country");
             System.out.println("Locale=" + l + "_" + c);
             Locale.setDefault(new Locale(l, c));
         } catch (Exception e) {
             log.warn("Error while changing the user language and country");
         }
 
         try {
             String ext = System.getProperty("directory.extension");
 
             System.out.println("SystemdirExtension=:" + ext);
 
             if (ext != null) {
                 dirExtension = ext;
                 cismapDirectory += ext;
             }
         } catch (Exception e) {
             log.warn("Error while adding DirectoryExtension");
         }
 
         CismapBroker.getInstance().setCismapFolderPath(cismapDirectory);
 
         this.setIconImage(logo.getImage());
         System.setSecurityManager(null);
         //this.setIconImage(new javax.swing.ImageIcon(getClass().getResource("/images/cismap.png")).getImage());
         this.context = context;
         plugin = (context != null);
 
         try {
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(0, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin_initialisieren"));
             }
             if (!plugin) {
                 try {
                     org.apache.log4j.PropertyConfigurator.configure(getClass().getResource("/cismap.log4j.properties"));
                     if (StaticDebuggingTools.checkHomeForFile("cismetDebuggingInitEventDispatchThreadHangMonitor")) {
                         EventDispatchThreadHangMonitor.initMonitoring();
                     }
                     if (StaticDebuggingTools.checkHomeForFile("cismetCheckForEDThreadVialoation")) {
                         RepaintManager.setCurrentManager(new CheckThreadViolationRepaintManager());
                     }
 
 
 
                 } catch (Exception e) {
                     System.err.println(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.LOG4J_ist_nicht_richtig_konfiguriert"));
                     e.printStackTrace();
                 }
 
 
 
             }
 
             try {
                 //javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) ;
                 javax.swing.UIManager.setLookAndFeel(new Plastic3DLookAndFeel());
                 //javax.swing.UIManager.setLookAndFeel(new NimbusLookAndFeel());
                 //javax.swing.UIManager.setLookAndFeel(new PlasticLookAndFeel());
                 //javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
                 //javax.swing.UIManager.setLookAndFeel(new com.sun.java.swing.plaf.windows.WindowsLookAndFeel());
                 // UIManager.setLookAndFeel(new PlasticLookAndFeel());
                 //javax.swing.UIManager.setLookAndFeel(new PlasticXPLookAndFeel());
             } catch (Exception e) {
                 log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Fehler_beim_Einstellen_des_Lock_Feels"), e);
             }
 
             clipboarder = new ClipboardWaitDialog(StaticSwingTools.getParentFrame(this), true);
             showObjectsWaitDialog = new ShowObjectsWaitDialog(StaticSwingTools.getParentFrame(this), true);
 
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(100, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Erzeugen_der_Widgets"));
             }
             //Erzeugen der Widgets
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
             groovyConsole.setVariable("map", mapC);
 
             overviewComponent = new OverviewComponent();
             overviewComponent.setMasterMap(mapC);
 
 //            KeyStroke upsideDownStroke = KeyStroke.getKeyStroke('Y',InputEvent.CTRL_MASK);
 //            Action upsidedownAction = new AbstractAction(){
 //                public void actionPerformed(ActionEvent e) {
 //                    java.awt.EventQueue.invokeLater(new Runnable() {
 //                        public void run() {
 //                            mapC.checkAndFixErroneousTransformation();
 //                        }
 //                    });
 //                }
 //            };
 //            getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(upsideDownStroke, "UPSIDEDOWN");
 //            getRootPane().getActionMap().put("UPSIDEDOWN", upsidedownAction);
 
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(200, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Oberflaeche_initialisieren"));
             }
             try {
                 initComponents();
             } catch (Exception e) {
                 log.fatal("Fehler in initComponents. Das wird nix.", e);
             }
 
             if (!plugin) {
                menSearch.setVisible(false);
                 cmdPluginSearch.setVisible(false);
                 KeyStroke configLoggerKeyStroke = KeyStroke.getKeyStroke('L', InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
                 Action configAction = new AbstractAction() {
 
                     public void actionPerformed(ActionEvent e) {
                         java.awt.EventQueue.invokeLater(new Runnable() {
 
                             public void run() {
                                 Log4JQuickConfig.getSingletonInstance().setVisible(true);
                             }
                         });
                     }
                 };
                 getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(configLoggerKeyStroke, "CONFIGLOGGING");
                 getRootPane().getActionMap().put("CONFIGLOGGING", configAction);
             }
 
             //Menu
             menues.add(menFile);
             menues.add(menEdit);
             menues.add(menHistory);
             menues.add(menSearch);
             menues.add(menBookmarks);
             menues.add(menExtras);
             menues.add(menWindows);
             menues.add(menHelp);
 
             panStatus.add(statusBar, BorderLayout.CENTER);
 
             tlbMain.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
             tlbMain.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
             tlbMain.putClientProperty(Options.HEADER_STYLE_KEY, HeaderStyle.BOTH);
 
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(300, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Widgets_verbinden"));
             }
 
             //Wire the components
             //add Listeners
             ((JHistoryButton) cmdForward).setDirection(JHistoryButton.DIRECTION_FORWARD);
             ((JHistoryButton) cmdBack).setDirection(JHistoryButton.DIRECTION_BACKWARD);
             ((JHistoryButton) cmdForward).setHistoryModel(mapC);
             ((JHistoryButton) cmdBack).setHistoryModel(mapC);
 
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
 
             //activeLayers.getMappingModel().addMappingModelListener(legend);
 
             //set the components in the broker
             CismapBroker.getInstance().setMappingComponent(mapC);
 //            broker.setLayerWidget(activeLayers);
 
             String cismapconfig = null;
             String fallBackConfig = null;
             try {
                 String prefix = "cismapconfig:";
                 String username = Sirius.navigator.connection.SessionManager.getSession().getUser().getName();
                 String groupname = Sirius.navigator.connection.SessionManager.getSession().getUser().getUserGroup().getName();
                 String domainname = Sirius.navigator.connection.SessionManager.getSession().getUser().getUserGroup().getDomain();
 
                 //First try: cismapconfig:username@usergroup@domainserver
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + username + "@" + groupname + "@" + domainname);
                 }
                 //Second try: cismapconfig:*@usergroup@domainserver
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + "*" + "@" + groupname + "@" + domainname);
                 }
                 //Third try: cismapconfig:*@*@domainserver
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + "*" + "@" + "*" + "@" + domainname);
                 }
                 //Default from pluginXML
                 if (cismapconfig == null) {
                     cismapconfig = context.getEnvironment().getParameter(prefix + "default");
                 }
                 fallBackConfig = context.getEnvironment().getParameter(prefix + "default");
             } catch (Throwable t) {
                 log.info("cismap started standalone", t);
             }
             //Default
             if (cismapconfig == null) {
                 cismapconfig = "defaultCismapProperties.xml";
 
             }
             if (fallBackConfig == null) {
                 fallBackConfig = "defaultCismapProperties.xml";
 
             }
 
             log.info("ServerConfigFile=" + cismapconfig);
             configurationManager.setDefaultFileName(cismapconfig);
             configurationManager.setFallBackFileName(fallBackConfig);
 
             if (!plugin) {
                 configurationManager.setFileName("configuration.xml");
 
             } else {
                 configurationManager.setFileName("configurationPlugin.xml");
                 configurationManager.addConfigurable(metaSearch);
             }
             configurationManager.setClassPathFolder("/");
             configurationManager.setFolder(".cismap" + dirExtension);
             configurationManager.addConfigurable(this);
 
             configurationManager.addConfigurable(capabilities);
             configurationManager.addConfigurable(wfsFormFactory);
             configurationManager.addConfigurable(mapC);
             configurationManager.addConfigurable(activeLayers);
             configurationManager.addConfigurable(featureControl);
             configurationManager.addConfigurable(overviewComponent);
             configurationManager.addConfigurable(OptionsClient.getInstance());
 
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(400, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Dockingsystem_initialisieren"));
             }
 
             // Flexdock stuff
             //// DockingManager.setFloatingEnabled(false);
             Icon icoLayers = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png"));
             Icon icoServer = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"));
             Icon icoServerInfo = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/serverInfo.png"));
             Icon icoLayerInfo = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/layerInfo.png"));
             Icon icoFeatureInfo = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/featureInfo16.png"));
             Icon icoLegend = new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/navigatorplugin/res/legend.png"));
             Icon icoClassSelection = new javax.swing.ImageIcon(getClass().getResource("/images/classSelection.png"));
             Icon icoMap = new javax.swing.ImageIcon(getClass().getResource("/images/map.png"));
             Icon icoFeatureControl = new javax.swing.ImageIcon(getClass().getResource("/images/objects.png"));
 
             //-------------------------InfoNode initialization-------------------------------------------//
             rootWindow = DockingUtil.createRootWindow(viewMap, true);
 
 
             vMap = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Karte"), Static2DTools.borderIcon(icoMap, 0, 3, 0, 1), mapC);
             viewMap.addView("map", vMap);
             viewMenuMap.put("map", mniMap);
 
             vLayers = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Layer"), Static2DTools.borderIcon(icoLayers, 0, 3, 0, 1), activeLayers);
             viewMap.addView("activeLayers", vLayers);
             viewMenuMap.put("activeLayers", mniLayer);
 
             vCaps = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Capabilities"), Static2DTools.borderIcon(icoServer, 0, 3, 0, 1), capabilities);
             viewMap.addView("capabilities", vCaps);
             viewMenuMap.put("capabilities", mniCapabilities);
 
             vServerInfo = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Server_Info"), Static2DTools.borderIcon(icoServerInfo, 0, 3, 0, 1), serverInfo);
             viewMap.addView("serverinfo", vServerInfo);
             viewMenuMap.put("serverinfo", mniServerInfo);
 
             vOverview = new View("Overview", Static2DTools.borderIcon(icoMap, 0, 3, 0, 1), overviewComponent);
             viewMap.addView("overview", vOverview);
             viewMenuMap.put("overview", mniOverview);
             legendTab[2] = vOverview;
 
             vLayerInfo = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Layerinfo"), Static2DTools.borderIcon(icoLayerInfo, 0, 3, 0, 1), layerInfo);
             viewMap.addView("layerinfo", vLayerInfo);
             viewMenuMap.put("layerinfo", mniLayerInfo);
             legendTab[3] = vLayerInfo;
 
             vLegend = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Legende"), Static2DTools.borderIcon(icoLegend, 0, 3, 0, 1), legend);
             viewMap.addView("legend", vLegend);
             viewMenuMap.put("legend", mniLegend);
             if (plugin) {
                 vMetaSearch = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Suchauswahl"), Static2DTools.borderIcon(icoClassSelection, 0, 3, 0, 1), metaSearch);
                 viewMap.addView("metaSearch", vMetaSearch);
                 legendTab[1] = vMetaSearch;
             } else {
                 legendTab[1] = vLegend;
             }
 
             mniClassTree.setVisible(plugin);
             viewMenuMap.put("metaSearch", mniClassTree);
             vFeatureInfo = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Sachdatenabfrage"), Static2DTools.borderIcon(icoFeatureInfo, 0, 3, 0, 1), featureInfo);
             viewMap.addView("featureInfo", vFeatureInfo);
             viewMenuMap.put("featureInfo", mniFeatureInfo);
 
             vFeatureControl = new View(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Objekte"), Static2DTools.borderIcon(icoFeatureControl, 0, 3, 0, 1), featureControl);
             viewMap.addView("featureControl", vFeatureControl);
             viewMenuMap.put("featureControl", mniFeatureControl);
 
             //vDebug=createView("debug","DebugPanel",debugPanel);
             //vGroovy=createView("groovy","Groovy Console",groovyConsole);
 
             configurationManager.configure(wfsFormFactory);
             //WFSForms
             Set<String> keySet = wfsFormFactory.getForms().keySet();
             JMenu wfsFormsMenu = new JMenu(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("wfsFormMenuTitle"));
             for (String key : keySet) {
                 //View
                 final AbstractWFSForm form = wfsFormFactory.getForms().get(key);
                 form.setMappingComponent(mapC);
                 log.debug("WFSForms: key,form" + key + "," + form);
                 final View formView = new View(form.getTitle(), Static2DTools.borderIcon(form.getIcon(), 0, 3, 0, 1), form);
                 log.debug("WFSForms: formView" + formView);
                 viewMap.addView(form.getId(), formView);
                 wfsFormViews.add(formView);
                 wfs.add(formView);
                 //Menu
                 JMenuItem menuItem = new JMenuItem(form.getMenuString());
                 menuItem.setIcon(form.getIcon());
                 menuItem.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         log.debug("showOrHideView:" + formView);
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
 
             legendTab[0] = vLegend;
 
             rootWindow.addTabMouseButtonListener(DockingWindowActionMouseButtonListener.MIDDLE_BUTTON_CLOSE_LISTENER);
 
             DockingWindowsTheme theme = new ShapedGradientDockingTheme();
             rootWindow.getRootWindowProperties().addSuperObject(
                     theme.getRootWindowProperties());
 
             RootWindowProperties titleBarStyleProperties =
                     PropertiesUtil.createTitleBarStyleRootWindowProperties();
 
             rootWindow.getRootWindowProperties().addSuperObject(
                     titleBarStyleProperties);
 
             rootWindow.getRootWindowProperties().getDockingWindowProperties().setUndockEnabled(true);
 
             AlphaGradientComponentPainter x = new AlphaGradientComponentPainter(java.awt.SystemColor.inactiveCaptionText, java.awt.SystemColor.activeCaptionText, java.awt.SystemColor.activeCaptionText, java.awt.SystemColor.inactiveCaptionText);
             vMap.getViewProperties().getViewTitleBarProperties().getNormalProperties().getCloseButtonProperties().setVisible(true);
             rootWindow.getRootWindowProperties().getDragRectangleShapedPanelProperties().setComponentPainter(x);
 
             if (!EventQueue.isDispatchThread()) {
                 EventQueue.invokeAndWait(new Runnable() {
 
                     public void run() {
                         if (plugin) {
                             //DockingManager.setDefaultPersistenceKey("pluginPerspectives.xml");
                             loadLayout(cismapDirectory + fs + pluginLayoutName);
 
                         } else {
                             //DockingManager.setDefaultPersistenceKey("cismapPerspectives.xml");
                             loadLayout(cismapDirectory + fs + standaloneLayoutName);
                         }
                     }
                 });
             } else {
                 if (plugin) {
                     //DockingManager.setDefaultPersistenceKey("pluginPerspectives.xml");
                     loadLayout(cismapDirectory + fs + pluginLayoutName);
 
                 } else {
                     //DockingManager.setDefaultPersistenceKey("cismapPerspectives.xml");
                     loadLayout(cismapDirectory + fs + standaloneLayoutName);
                 }
             }
 
             if (plugin && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                 this.context.getEnvironment().getProgressObserver().setProgress(500, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Einstellungen_laden"));
             }
         } catch (Exception ex) {
             log.fatal("Fehler im Constructor von CismapPlugin", ex);
             System.err.println("Fehler im Constructor von CismapPlugin");
             ex.printStackTrace();
         }
 
         //Damit mehrere Geometrien angelegt werden koennen
         mapC.setReadOnly(false);
         Object blocker = new Object();
         if (plugin) {
             try {
                 try {
                     synchronized (blocker) {
                         if (context != null && context.getEnvironment() != null && context.getEnvironment().getProgressObserver() != null && this.context.getEnvironment().isProgressObservable()) {
                             this.context.getEnvironment().getProgressObserver().setProgress(500, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Methoden_laden"));
                         }
                     }
                 } catch (Exception e) {
                     log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Keine_Progressmeldung_moeglich"), e);
                 }
                 mniClose.setVisible(false);
                 pluginMethods.put(showObjectsMethod.getId(), showObjectsMethod);
                 appletContext = context.getEnvironment().getAppletContext();
                 if (context != null && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(600, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Suchbaum_laden"));
                 }
                 this.context.getMetadata().addMetaNodeSelectionListener(new NodeChangeListener());
                 Node[] classNodes = Sirius.navigator.connection.SessionManager.getProxy().getClassTreeNodes();
                 final SearchSelectionTree sst = new SearchSelectionTree(classNodes);
                 sst.addMouseListener(new MouseAdapter() {
 
                     @Override
                     public void mouseClicked(MouseEvent e) {
                         if (e.getClickCount() == 1) {
                             TreePath path = sst.getPathForLocation(e.getX(), e.getY());
                             if (path != null) {
                                 DefaultMetaTreeNode node = (DefaultMetaTreeNode) path.getLastPathComponent();
                                 node.selectSubtree(!node.isSelected());
                                 //logger.debug("setting search forms enabled");
                                 Collection userGroups = new LinkedList();
                                 userGroups.add(SessionManager.getSession().getUser().getUserGroup().getKey());
                                 sst.repaint();
                             }
                         }
                     }
                 });
                 metaSearch.setSearchTree(sst);
                 if (context != null && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(700, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin:_Konfiguration_laden"));
                 }
 
 //                configureApp(false);
                 if (context != null && context.getEnvironment() != null && this.context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setProgress(1000, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.cismap_Plugin_fertig"));
                 }
                 if (context != null && context.getEnvironment() != null && context.getEnvironment().isProgressObservable()) {
                     this.context.getEnvironment().getProgressObserver().setFinished(true);
                 }
             } catch (Throwable t) {
                 context.getLogger().fatal(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Fehler_im_Constructor_von_CismapPlugin"), t);
             }
 
             //TimEasy
             ((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).setGeometryFeatureClass(TimEasyPureNewFeature.class);
             TimEasyDialog.addTimTimEasyListener(new TimEasyListener() {
 
                 public void timEasyObjectInserted(TimEasyEvent tee) {
                     mapC.getFeatureCollection().removeFeature(tee.getPureNewfeature());
                     mapC.getFeatureCollection().addFeature(new CidsFeature(tee.getMetaObjectNode()));
                 }
             });
         }
 
         log.info("add InfoNode main component to the panMain Panel");
         panMain.add(rootWindow, BorderLayout.CENTER);
 
         vMap.doLayout();
         mapC.setMappingModel(activeLayers.getMappingModel());
         setVisible(true);
         //validateTree();
         configureApp(false);
         //configureActiveTabAfterVisibility();
         isInit = false;
         for (Scale s : mapC.getScales()) {
             if (s.getDenominator() > 0) {
                 menExtras.add(getScaleMenuItem(s.getText(), s.getDenominator()));
             }
         }
         statusBar.addScalePopups();
         cmdReconfig.setVisible(false);
         jSeparator1.setVisible(false);
         mapC.getFeatureCollection().addFeatureCollectionListener(this);
         repaint();
         if (!StaticDebuggingTools.checkHomeForFile("cismetTurnOffInternalWebserver")) {
             initHttpServer();
         }
 
 //        addComponentListener(new ComponentListener() {
 //            public void componentHidden(ComponentEvent e) {
 //            }
 //            public void componentMoved(ComponentEvent e) {
 //            }
 //            public void componentResized(ComponentEvent e) {
 //                log.fatal("component resized()");
 //                mapC.rescaleStickyNodes();
 //            }
 //            public void componentShown(ComponentEvent e) {
 //            }
 //        });
         log.debug("CismapPlugin als Observer anmelden");
         ((Observable) mapC.getMemUndo()).addObserver(CismapPlugin.this);
         ((Observable) mapC.getMemRedo()).addObserver(CismapPlugin.this);
         mapC.unlock();
         overviewComponent.getOverviewMap().unlock();
         layerInfo.initDividerLocation();
 
     }
 
     private JMenuItem getScaleMenuItem(String t, final int d) {
         JMenuItem jmi = new JMenuItem(t);
         jmi.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(d));
             }
         });
         return jmi;
     }
 
     public MappingComponent getMappingComponent() {
         return mapC;
     }
 
     private void setupDefaultLayout() {
         if (wfsViews.length != 0) {
             rootWindow.setWindow(new SplitWindow(true, 0.716448f,
                     new SplitWindow(false, 0.72572404f,
                     new SplitWindow(false, 0.21391752f,
                     new TabWindow(wfsViews),
                     vMap),
                     new TabWindow(new DockingWindow[]{
                         vLayers,
                         vFeatureControl,
                         vFeatureInfo
                     })),
                     new SplitWindow(false, 0.66f,
                     new TabWindow(new DockingWindow[]{
                         vCaps,
                         vServerInfo
                     }),
                     new TabWindow(legendTab))));
         } else {
             rootWindow.setWindow(new SplitWindow(true, 0.716448f,
                     new SplitWindow(false, 0.72572404f,
                     //auf Verdacht ge�ndert (von Sebastian Puhl) ;-)
                     new TabWindow(vMap),
                     new TabWindow(new DockingWindow[]{
                         vLayers,
                         vFeatureControl,
                         vFeatureInfo
                     })),
                     new SplitWindow(false, 0.66f,
                     new TabWindow(new DockingWindow[]{
                         vCaps,
                         vServerInfo
                     }),
                     new TabWindow(legendTab))));
         }
 
         for (int i = 0; i < wfsViews.length; i++) {
             wfsViews[i].close();
         }
         rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
         rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
 
         vLegend.restoreFocus();
         vCaps.restoreFocus();
         vLayers.restoreFocus();
         vMap.restoreFocus();
 
         if (windows2skip != null) {
             for (String id : windows2skip) {
                 View v = viewMap.getView(id);
                 if (v != null) {
                     v.close();
                 }
                 JMenuItem menu = viewMenuMap.get(id);
                 if (menu != null) {
                     menu.setVisible(false);
                 }
             }
         }
     }
 
     private void openUrlInExternalBrowser(String url) {
         try {
             if (appletContext == null) {
                 de.cismet.tools.BrowserLauncher.openURL(url);
             } else {
                 java.net.URL u = new java.net.URL(url);
                 appletContext.showDocument(u, "cismetBrowser");
             }
         } catch (Exception e) {
             log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("FeatureInfoDisplay.log.Fehler_beim_Oeffnen_von") + url + java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("FeatureInfoDisplay.log.Neuer_Versuch"), e);
             //Nochmal zur Sicherheit mit dem BrowserLauncher probieren
             try {
                 de.cismet.tools.BrowserLauncher.openURL(url);
             } catch (Exception e2) {
                 log.warn(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("FeatureInfoDisplay.log.Auch_das_2te_Mal_ging_schief.Fehler_beim_Oeffnen_von") + url + java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("FeatureInfoDisplay.log.Letzter_Versuch"), e2);
                 try {
                     de.cismet.tools.BrowserLauncher.openURL("file://" + url);
                 } catch (Exception e3) {
                     log.error(java.util.ResourceBundle.getBundle("de/cismet/cismap/commons/GuiBundle").getString("FeatureInfoDisplay.log.Auch_das_3te_Mal_ging_schief.Fehler_beim_Oeffnen_von:file://") + url, e3);
                 }
             }
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
         popMenuSearch = new javax.swing.JPopupMenu();
         mniSearchRectangle1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolygon1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchEllipse1 = new javax.swing.JRadioButtonMenuItem();
         mniSearchPolyline1 = new javax.swing.JRadioButtonMenuItem();
         jSeparator12 = new javax.swing.JSeparator();
         mniRedo1 = new javax.swing.JMenuItem();
         mniBuffer1 = new javax.swing.JMenuItem();
         cmdGroupSearch = new javax.swing.ButtonGroup();
         cmdGroupSearch1 = new javax.swing.ButtonGroup();
         panAll = new javax.swing.JPanel();
         panToolbar = new javax.swing.JPanel();
         panMain = new javax.swing.JPanel();
         tlbMain = new javax.swing.JToolBar();
         cmdReconfig = new JPopupMenuButton();
         ((JPopupMenuButton)cmdReconfig).setPopupMenu(popMen);
         jSeparator1 = new javax.swing.JSeparator();
         cmdBack = new JHistoryButton(){
             public void historyActionPerformed() {
                 if (mapC!=null) {
                     mapC.back(true);
                 }
             }
         };
         cmdHome = new javax.swing.JButton();
         cmdForward = new JHistoryButton(){
             public void historyActionPerformed() {
                 if (mapC!=null) {
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
         mniRedo = new javax.swing.JMenuItem();
         mniBuffer = new javax.swing.JMenuItem();
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
 
         mnuConfigServer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"))); // NOI18N
         java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle"); // NOI18N
         mnuConfigServer.setText(bundle.getString("CismapPlugin.mnuConfigServer.text")); // NOI18N
         mnuConfigServer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuConfigServerActionPerformed(evt);
             }
         });
         popMen.add(mnuConfigServer);
 
         menBookmarks.setMnemonic('L');
         menBookmarks.setText(bundle.getString("CismapPlugin.menBookmarks.text")); // NOI18N
 
         mniAddBookmark.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark_add.png"))); // NOI18N
         mniAddBookmark.setText(bundle.getString("CismapPlugin.mniAddBookmark.text")); // NOI18N
         mniAddBookmark.setEnabled(false);
         menBookmarks.add(mniAddBookmark);
 
         mniBookmarkManager.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark_folder.png"))); // NOI18N
         mniBookmarkManager.setText(bundle.getString("CismapPlugin.mniBookmarkManager.text")); // NOI18N
         mniBookmarkManager.setEnabled(false);
         menBookmarks.add(mniBookmarkManager);
 
         mniBookmarkSidebar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/bookmark.png"))); // NOI18N
         mniBookmarkSidebar.setText(bundle.getString("CismapPlugin.mniBookmarkSidebar.text")); // NOI18N
         mniBookmarkSidebar.setEnabled(false);
         menBookmarks.add(mniBookmarkSidebar);
 
         popMenuSearch.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
             public void popupMenuCanceled(javax.swing.event.PopupMenuEvent evt) {
             }
             public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent evt) {
             }
             public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {
                 popMenuSearchPopupMenuWillBecomeVisible(evt);
             }
         });
 
         mniSearchRectangle1.setAction(searchRectangleAction);
         cmdGroupSearch1.add(mniSearchRectangle1);
         mniSearchRectangle1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchRectangle1.text")); // NOI18N
         mniSearchRectangle1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         popMenuSearch.add(mniSearchRectangle1);
 
         mniSearchPolygon1.setAction(searchPolygonAction);
         cmdGroupSearch1.add(mniSearchPolygon1);
         mniSearchPolygon1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchPolygon1.text")); // NOI18N
         mniSearchPolygon1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         popMenuSearch.add(mniSearchPolygon1);
 
         mniSearchEllipse1.setAction(searchEllipseAction);
         cmdGroupSearch1.add(mniSearchEllipse1);
         mniSearchEllipse1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchEllipse1.text")); // NOI18N
         mniSearchEllipse1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         popMenuSearch.add(mniSearchEllipse1);
 
         mniSearchPolyline1.setAction(searchPolylineAction);
         cmdGroupSearch1.add(mniSearchPolyline1);
         mniSearchPolyline1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchPolyline1.text")); // NOI18N
         mniSearchPolyline1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         popMenuSearch.add(mniSearchPolyline1);
         popMenuSearch.add(jSeparator12);
 
         mniRedo1.setAction(searchRedoAction);
         mniRedo1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         mniRedo1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniRedo1.text")); // NOI18N
         mniRedo1.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniRedo1.toolTipText")); // NOI18N
         popMenuSearch.add(mniRedo1);
 
         mniBuffer1.setAction(searchBufferAction);
         mniBuffer1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniBuffer1.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniBuffer1.text")); // NOI18N
         mniBuffer1.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniBuffer1.toolTipText")); // NOI18N
         popMenuSearch.add(mniBuffer1);
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle(bundle.getString("CismapPlugin.Form.title")); // NOI18N
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosed(java.awt.event.WindowEvent evt) {
                 formWindowClosed(evt);
             }
         });
         addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 formComponentResized(evt);
             }
             public void componentShown(java.awt.event.ComponentEvent evt) {
                 formComponentShown(evt);
             }
         });
 
         panAll.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
         panAll.setLayout(new java.awt.BorderLayout());
 
         panToolbar.setLayout(new java.awt.BorderLayout());
         panAll.add(panToolbar, java.awt.BorderLayout.NORTH);
 
         panMain.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
         panMain.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseEntered(java.awt.event.MouseEvent evt) {
                 panMainMouseEntered(evt);
             }
             public void mouseExited(java.awt.event.MouseEvent evt) {
                 panMainMouseExited(evt);
             }
         });
         panMain.setLayout(new java.awt.BorderLayout());
 
         tlbMain.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 tlbMainMouseClicked(evt);
             }
         });
 
         cmdReconfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/open.gif"))); // NOI18N
         cmdReconfig.setToolTipText(bundle.getString("CismapPlugin.cmdReconfig.toolTipText")); // NOI18N
         cmdReconfig.setBorderPainted(false);
         cmdReconfig.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdReconfigActionPerformed(evt);
             }
         });
         tlbMain.add(cmdReconfig);
 
         jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator1.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator1.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator1);
 
         cmdBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/back.png"))); // NOI18N
         cmdBack.setToolTipText(bundle.getString("CismapPlugin.cmdBack.toolTipText")); // NOI18N
         cmdBack.setBorderPainted(false);
         cmdBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdBackActionPerformed(evt);
             }
         });
         tlbMain.add(cmdBack);
 
         cmdHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home.gif"))); // NOI18N
         cmdHome.setToolTipText(bundle.getString("CismapPlugin.cmdHome.toolTipText")); // NOI18N
         cmdHome.setBorderPainted(false);
         cmdHome.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdHomeActionPerformed(evt);
             }
         });
         tlbMain.add(cmdHome);
 
         cmdForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/forward.png"))); // NOI18N
         cmdForward.setToolTipText(bundle.getString("CismapPlugin.cmdForward.toolTipText")); // NOI18N
         cmdForward.setBorderPainted(false);
         tlbMain.add(cmdForward);
 
         jSeparator2.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator2.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator2.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator2);
 
         cmdRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload.gif"))); // NOI18N
         cmdRefresh.setToolTipText(bundle.getString("CismapPlugin.cmdRefresh.toolTipText")); // NOI18N
         cmdRefresh.setBorderPainted(false);
         cmdRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdRefreshActionPerformed(evt);
             }
         });
         tlbMain.add(cmdRefresh);
 
         jSeparator6.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator6.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator6.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator6);
 
         cmdPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/frameprint.png"))); // NOI18N
         cmdPrint.setText(bundle.getString("CismapPlugin.cmdPrint.text")); // NOI18N
         cmdPrint.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.cmdPrint.toolTipText")); // NOI18N
         cmdPrint.setBorderPainted(false);
         cmdPrint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPrintActionPerformed(evt);
             }
         });
         tlbMain.add(cmdPrint);
 
         cmdClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard.png"))); // NOI18N
         cmdClipboard.setText(bundle.getString("CismapPlugin.cmdClipboard.text")); // NOI18N
         cmdClipboard.setToolTipText(bundle.getString("CismapPlugin.cmdClipboard.toolTipText")); // NOI18N
         cmdClipboard.setBorderPainted(false);
         cmdClipboard.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdClipboardActionPerformed(evt);
             }
         });
         tlbMain.add(cmdClipboard);
 
         jSeparator4.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator4.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator4.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator4);
 
         cmdGroupPrimaryInteractionMode.add(togInvisible);
         togInvisible.setText(bundle.getString("CismapPlugin.togInvisible.text")); // NOI18N
         tlbMain.add(togInvisible);
 
         cmdGroupPrimaryInteractionMode.add(cmdSelect);
         cmdSelect.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/select.png"))); // NOI18N
         cmdSelect.setSelected(true);
         cmdSelect.setToolTipText(bundle.getString("CismapPlugin.cmdSelect.toolTipText")); // NOI18N
         cmdSelect.setBorderPainted(false);
         cmdSelect.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSelectActionPerformed(evt);
             }
         });
         tlbMain.add(cmdSelect);
 
         cmdGroupPrimaryInteractionMode.add(cmdZoom);
         cmdZoom.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/zoom.gif"))); // NOI18N
         cmdZoom.setToolTipText(bundle.getString("CismapPlugin.cmdZoom.toolTipText")); // NOI18N
         cmdZoom.setBorderPainted(false);
         cmdZoom.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdZoomActionPerformed(evt);
             }
         });
         tlbMain.add(cmdZoom);
 
         cmdGroupPrimaryInteractionMode.add(cmdPan);
         cmdPan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pan.gif"))); // NOI18N
         cmdPan.setToolTipText(bundle.getString("CismapPlugin.cmdPan.toolTipText")); // NOI18N
         cmdPan.setBorderPainted(false);
         cmdPan.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPanActionPerformed(evt);
             }
         });
         tlbMain.add(cmdPan);
 
         cmdGroupPrimaryInteractionMode.add(cmdFeatureInfo);
         cmdFeatureInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/featureInfos.gif"))); // NOI18N
         cmdFeatureInfo.setToolTipText(bundle.getString("CismapPlugin.cmdFeatureInfo.toolTipText")); // NOI18N
         cmdFeatureInfo.setBorderPainted(false);
         cmdFeatureInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdFeatureInfoActionPerformed(evt);
             }
         });
         tlbMain.add(cmdFeatureInfo);
 
         ((JPopupMenuButton)cmdPluginSearch).setPopupMenu(popMenuSearch);
         cmdPluginSearch.setAction(searchAction);
         cmdPluginSearch.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/pluginSearchRectangle.png"))); // NOI18N
         cmdPluginSearch.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.cmdPluginSearch.toolTipText")); // NOI18N
         cmdPluginSearch.setBorderPainted(false);
         cmdGroupPrimaryInteractionMode.add(cmdPluginSearch);
         cmdPluginSearch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdPluginSearchActionPerformed(evt);
             }
         });
         tlbMain.add(cmdPluginSearch);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewPolygon);
         cmdNewPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newPolygon.png"))); // NOI18N
         cmdNewPolygon.setToolTipText(bundle.getString("CismapPlugin.cmdNewPolygon.toolTipText")); // NOI18N
         cmdNewPolygon.setBorderPainted(false);
         cmdNewPolygon.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNewPolygonActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNewPolygon);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewLinestring);
         cmdNewLinestring.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newLinestring.png"))); // NOI18N
         cmdNewLinestring.setToolTipText(bundle.getString("CismapPlugin.cmdNewLinestring.toolTipText")); // NOI18N
         cmdNewLinestring.setBorderPainted(false);
         cmdNewLinestring.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createGeometryAction(evt);
             }
         });
         tlbMain.add(cmdNewLinestring);
 
         cmdGroupPrimaryInteractionMode.add(cmdNewPoint);
         cmdNewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/newPoint.png"))); // NOI18N
         cmdNewPoint.setToolTipText(bundle.getString("CismapPlugin.cmdNewPoint.toolTipText")); // NOI18N
         cmdNewPoint.setBorderPainted(false);
         cmdNewPoint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNewPointActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNewPoint);
 
         cmdGroupPrimaryInteractionMode.add(cmdMoveGeometry);
         cmdMoveGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/move.png"))); // NOI18N
         cmdMoveGeometry.setToolTipText(bundle.getString("CismapPlugin.cmdMoveGeometry.toolTipText")); // NOI18N
         cmdMoveGeometry.setBorderPainted(false);
         cmdMoveGeometry.setMaximumSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.setMinimumSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.setPreferredSize(new java.awt.Dimension(29, 29));
         cmdMoveGeometry.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdMoveGeometryActionPerformed(evt);
             }
         });
         tlbMain.add(cmdMoveGeometry);
 
         cmdGroupPrimaryInteractionMode.add(cmdRemoveGeometry);
         cmdRemoveGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/remove.png"))); // NOI18N
         cmdRemoveGeometry.setToolTipText(bundle.getString("CismapPlugin.cmdRemoveGeometry.toolTipText")); // NOI18N
         cmdRemoveGeometry.setBorderPainted(false);
         cmdRemoveGeometry.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
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
         cmdNodeMove.setToolTipText(bundle.getString("CismapPlugin.cmdNodeMove.toolTipText")); // NOI18N
         cmdNodeMove.setBorderPainted(false);
         cmdNodeMove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNodeMoveActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNodeMove);
 
         cmdGroupNodes.add(cmdNodeAdd);
         cmdNodeAdd.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/insertNodes.png"))); // NOI18N
         cmdNodeAdd.setToolTipText(bundle.getString("CismapPlugin.cmdNodeAdd.toolTipText")); // NOI18N
         cmdNodeAdd.setBorderPainted(false);
         cmdNodeAdd.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNodeAddActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNodeAdd);
 
         cmdGroupNodes.add(cmdNodeRemove);
         cmdNodeRemove.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/removeNodes.png"))); // NOI18N
         cmdNodeRemove.setToolTipText(bundle.getString("CismapPlugin.cmdNodeRemove.toolTipText")); // NOI18N
         cmdNodeRemove.setBorderPainted(false);
         cmdNodeRemove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNodeRemoveActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNodeRemove);
 
         cmdGroupNodes.add(cmdNodeRotateGeometry);
         cmdNodeRotateGeometry.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rotate.png"))); // NOI18N
         cmdNodeRotateGeometry.setToolTipText(bundle.getString("CismapPlugin.cmdNodeRotateGeometry.toolTipText")); // NOI18N
         cmdNodeRotateGeometry.setBorderPainted(false);
         cmdNodeRotateGeometry.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdNodeRotateGeometryActionPerformed(evt);
             }
         });
         tlbMain.add(cmdNodeRotateGeometry);
 
         jSeparator5.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator5.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator5.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator5);
 
         cmdSnap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap.png"))); // NOI18N
         cmdSnap.setToolTipText(bundle.getString("CismapPlugin.cmdSnap.toolTipText")); // NOI18N
         cmdSnap.setBorderPainted(false);
         cmdSnap.setMaximumSize(new java.awt.Dimension(29, 29));
         cmdSnap.setMinimumSize(new java.awt.Dimension(29, 29));
         cmdSnap.setPreferredSize(new java.awt.Dimension(29, 29));
         cmdSnap.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png"))); // NOI18N
         cmdSnap.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/images/snap_selected.png"))); // NOI18N
         cmdSnap.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cmdSnapActionPerformed(evt);
             }
         });
         tlbMain.add(cmdSnap);
 
         jSeparator11.setOrientation(javax.swing.SwingConstants.VERTICAL);
         jSeparator11.setMaximumSize(new java.awt.Dimension(2, 32767));
         jSeparator11.setPreferredSize(new java.awt.Dimension(2, 10));
         tlbMain.add(jSeparator11);
 
         cmdUndo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/undo.png"))); // NOI18N
         cmdUndo.setToolTipText(bundle.getString("CismapPlugin.cmdUndo.toolTipText")); // NOI18N
         cmdUndo.setBorderPainted(false);
         cmdUndo.setEnabled(false);
         cmdUndo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniUndoPerformed(evt);
             }
         });
         tlbMain.add(cmdUndo);
 
         cmdRedo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/redo.png"))); // NOI18N
         cmdRedo.setToolTipText(bundle.getString("CismapPlugin.cmdRedo.toolTipText")); // NOI18N
         cmdRedo.setBorderPainted(false);
         cmdRedo.setEnabled(false);
         cmdRedo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
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
         menFile.setText(bundle.getString("CismapPlugin.menFile.text")); // NOI18N
 
         mniLoadConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
         mniLoadConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniLoadConfig.setText(bundle.getString("CismapPlugin.mniLoadConfig.text")); // NOI18N
         mniLoadConfig.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLoadConfigActionPerformed(evt);
             }
         });
         menFile.add(mniLoadConfig);
 
         mniSaveConfig.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
         mniSaveConfig.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniSaveConfig.setText(bundle.getString("CismapPlugin.mniSaveConfig.text")); // NOI18N
         mniSaveConfig.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniSaveConfigActionPerformed(evt);
             }
         });
         menFile.add(mniSaveConfig);
 
         mniLoadConfigFromServer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/config.png"))); // NOI18N
         mniLoadConfigFromServer.setText(bundle.getString("CismapPlugin.mniLoadConfigFromServer.text")); // NOI18N
         mniLoadConfigFromServer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLoadConfigFromServerActionPerformed(evt);
             }
         });
         menFile.add(mniLoadConfigFromServer);
 
         sepServerProfilesStart.setName("sepServerProfilesStart"); // NOI18N
         menFile.add(sepServerProfilesStart);
 
         sepServerProfilesEnd.setName("sepServerProfilesEnd"); // NOI18N
         menFile.add(sepServerProfilesEnd);
 
         mniSaveLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
         mniSaveLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniSaveLayout.setText(bundle.getString("CismapPlugin.mniSaveLayout.text")); // NOI18N
         mniSaveLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniSaveLayoutActionPerformed(evt);
             }
         });
         menFile.add(mniSaveLayout);
 
         mniLoadLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
         mniLoadLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniLoadLayout.setText(bundle.getString("CismapPlugin.mniLoadLayout.text")); // NOI18N
         mniLoadLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLoadLayoutActionPerformed(evt);
             }
         });
         menFile.add(mniLoadLayout);
         menFile.add(jSeparator9);
 
         mniClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
         mniClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png"))); // NOI18N
         mniClipboard.setText(bundle.getString("CismapPlugin.mniClipboard.text")); // NOI18N
         mniClipboard.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniClipboardActionPerformed(evt);
             }
         });
         menFile.add(mniClipboard);
 
         mniGeoLinkClipboard.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         mniGeoLinkClipboard.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/clipboard16.png"))); // NOI18N
         mniGeoLinkClipboard.setText(bundle.getString("CismapPlugin.mniGeoLinkClipboard.text")); // NOI18N
         mniGeoLinkClipboard.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniGeoLinkClipboardActionPerformed(evt);
             }
         });
         menFile.add(mniGeoLinkClipboard);
 
         mniPrint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
         mniPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/frameprint16.png"))); // NOI18N
         mniPrint.setText(bundle.getString("CismapPlugin.mniPrint.text")); // NOI18N
         mniPrint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniPrintActionPerformed(evt);
             }
         });
         menFile.add(mniPrint);
         menFile.add(jSeparator10);
 
         mniClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
         mniClose.setText(bundle.getString("CismapPlugin.mniClose.text")); // NOI18N
         mniClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniCloseActionPerformed(evt);
             }
         });
         menFile.add(mniClose);
 
         mnuBar.add(menFile);
 
         menEdit.setMnemonic('B');
         menEdit.setText(bundle.getString("CismapPlugin.menEdit.text")); // NOI18N
         menEdit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menEditActionPerformed(evt);
             }
         });
 
         mniRefresh.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/reload16.gif"))); // NOI18N
         mniRefresh.setText(bundle.getString("CismapPlugin.mniRefresh.text")); // NOI18N
         mniRefresh.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniRefreshActionPerformed(evt);
             }
         });
         menEdit.add(mniRefresh);
         menEdit.add(jSeparator13);
 
         mniZoomToSelectedObjects.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToSelection.png"))); // NOI18N
         mniZoomToSelectedObjects.setText(bundle.getString("CismapPlugin.mniZoomToSelectedObjects.text")); // NOI18N
         mniZoomToSelectedObjects.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniZoomToSelectedObjectsActionPerformed(evt);
             }
         });
         menEdit.add(mniZoomToSelectedObjects);
 
         mniZoomToAllObjects.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/zoomToAll.png"))); // NOI18N
         mniZoomToAllObjects.setText(bundle.getString("CismapPlugin.mniZoomToAllObjects.text")); // NOI18N
         mniZoomToAllObjects.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniZoomToAllObjectsActionPerformed(evt);
             }
         });
         menEdit.add(mniZoomToAllObjects);
         menEdit.add(jSeparator15);
 
         mniRemoveSelectedObject.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removerow.png"))); // NOI18N
         mniRemoveSelectedObject.setText(bundle.getString("CismapPlugin.mniRemoveSelectedObject.text")); // NOI18N
         mniRemoveSelectedObject.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniRemoveSelectedObjectActionPerformed(evt);
             }
         });
         menEdit.add(mniRemoveSelectedObject);
 
         mniRemoveAllObjects.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/res/removeAll.png"))); // NOI18N
         mniRemoveAllObjects.setText(bundle.getString("CismapPlugin.mniRemoveAllObjects.text")); // NOI18N
         mniRemoveAllObjects.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniRemoveAllObjectsActionPerformed(evt);
             }
         });
         menEdit.add(mniRemoveAllObjects);
 
         mnuBar.add(menEdit);
 
         menHistory.setMnemonic('C');
         menHistory.setText(bundle.getString("CismapPlugin.menHistory.text")); // NOI18N
 
         mniBack.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.CTRL_MASK));
         mniBack.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/back16.png"))); // NOI18N
         mniBack.setText(bundle.getString("CismapPlugin.mniBack.text")); // NOI18N
         mniBack.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniBackActionPerformed(evt);
             }
         });
         menHistory.add(mniBack);
 
         mniForward.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, java.awt.event.InputEvent.CTRL_MASK));
         mniForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/forward16.png"))); // NOI18N
         mniForward.setText(bundle.getString("CismapPlugin.mniForward.text")); // NOI18N
         mniForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniForwardActionPerformed(evt);
             }
         });
         menHistory.add(mniForward);
 
         mniHome.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_HOME, 0));
         mniHome.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/home16.png"))); // NOI18N
         mniHome.setText(bundle.getString("CismapPlugin.mniHome.text")); // NOI18N
         mniHome.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniHomeActionPerformed(evt);
             }
         });
         menHistory.add(mniHome);
         menHistory.add(sepBeforePos);
         menHistory.add(sepAfterPos);
 
         mniHistorySidebar.setText(bundle.getString("CismapPlugin.mniHistorySidebar.text")); // NOI18N
         mniHistorySidebar.setEnabled(false);
         menHistory.add(mniHistorySidebar);
 
         mnuBar.add(menHistory);
 
         menSearch.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.menSearch.text")); // NOI18N
         menSearch.addMenuListener(new javax.swing.event.MenuListener() {
             public void menuCanceled(javax.swing.event.MenuEvent evt) {
             }
             public void menuDeselected(javax.swing.event.MenuEvent evt) {
             }
             public void menuSelected(javax.swing.event.MenuEvent evt) {
                 menSearchMenuSelected(evt);
             }
         });
 
         mniSearchRectangle.setAction(searchRectangleAction);
         cmdGroupSearch.add(mniSearchRectangle);
         mniSearchRectangle.setSelected(true);
         mniSearchRectangle.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchRectangle.text")); // NOI18N
         mniSearchRectangle.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/rectangle.png"))); // NOI18N
         menSearch.add(mniSearchRectangle);
 
         mniSearchPolygon.setAction(searchPolygonAction);
         cmdGroupSearch.add(mniSearchPolygon);
         mniSearchPolygon.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchPolygon.text")); // NOI18N
         mniSearchPolygon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polygon.png"))); // NOI18N
         menSearch.add(mniSearchPolygon);
 
         mniSearchEllipse.setAction(searchEllipseAction);
         cmdGroupSearch.add(mniSearchEllipse);
         mniSearchEllipse.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchEllipse.text_1")); // NOI18N
         mniSearchEllipse.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/ellipse.png"))); // NOI18N
         menSearch.add(mniSearchEllipse);
 
         mniSearchPolyline.setAction(searchPolylineAction);
         cmdGroupSearch.add(mniSearchPolyline);
         mniSearchPolyline.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniSearchPolyline.text_1")); // NOI18N
         mniSearchPolyline.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/polyline.png"))); // NOI18N
         menSearch.add(mniSearchPolyline);
         menSearch.add(jSeparator8);
 
         mniRedo.setAction(searchRedoAction);
         mniRedo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
         mniRedo.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniRedo.text")); // NOI18N
         mniRedo.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniRedo.toolTipText")); // NOI18N
         menSearch.add(mniRedo);
 
         mniBuffer.setAction(searchBufferAction);
         mniBuffer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/buffer.png"))); // NOI18N
         mniBuffer.setText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniBuffer.text_1")); // NOI18N
         mniBuffer.setToolTipText(org.openide.util.NbBundle.getMessage(CismapPlugin.class, "CismapPlugin.mniBuffer.toolTipText")); // NOI18N
         menSearch.add(mniBuffer);
 
         mnuBar.add(menSearch);
 
         menExtras.setMnemonic('E');
         menExtras.setText(bundle.getString("CismapPlugin.menExtras.text")); // NOI18N
 
         mniOptions.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/tooloptions.png"))); // NOI18N
         mniOptions.setText(bundle.getString("CismapPlugin.mniOptions.text")); // NOI18N
         mniOptions.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniOptionsActionPerformed(evt);
             }
         });
         menExtras.add(mniOptions);
         menExtras.add(jSeparator16);
 
         mniGotoPoint.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_MASK));
         mniGotoPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/goto.png"))); // NOI18N
         mniGotoPoint.setText(bundle.getString("CismapPlugin.mniGotoPoint.text")); // NOI18N
         mniGotoPoint.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniGotoPointActionPerformed(evt);
             }
         });
         menExtras.add(mniGotoPoint);
         menExtras.add(jSeparator14);
 
         mniScale.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
         mniScale.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/scale.png"))); // NOI18N
         mniScale.setText(bundle.getString("CismapPlugin.mniScale.text")); // NOI18N
         mniScale.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniScaleActionPerformed(evt);
             }
         });
         menExtras.add(mniScale);
 
         mnuBar.add(menExtras);
 
         menWindows.setMnemonic('F');
         menWindows.setText(bundle.getString("CismapPlugin.menWindows.text")); // NOI18N
         menWindows.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menWindowsActionPerformed(evt);
             }
         });
 
         mniLayer.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_1, java.awt.event.InputEvent.CTRL_MASK));
         mniLayer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/layers.png"))); // NOI18N
         mniLayer.setMnemonic('L');
         mniLayer.setText(bundle.getString("CismapPlugin.mniLayer.text")); // NOI18N
         mniLayer.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLayerActionPerformed(evt);
             }
         });
         menWindows.add(mniLayer);
 
         mniCapabilities.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_2, java.awt.event.InputEvent.CTRL_MASK));
         mniCapabilities.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/raster/wms/res/server.png"))); // NOI18N
         mniCapabilities.setMnemonic('C');
         mniCapabilities.setText(bundle.getString("CismapPlugin.mniCapabilities.text")); // NOI18N
         mniCapabilities.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniCapabilitiesActionPerformed(evt);
             }
         });
         menWindows.add(mniCapabilities);
 
         mniFeatureInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_3, java.awt.event.InputEvent.CTRL_MASK));
         mniFeatureInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/featureinfowidget/res/featureInfo16.png"))); // NOI18N
         mniFeatureInfo.setMnemonic('F');
         mniFeatureInfo.setText(bundle.getString("CismapPlugin.mniFeatureInfo.text")); // NOI18N
         mniFeatureInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniFeatureInfoActionPerformed(evt);
             }
         });
         menWindows.add(mniFeatureInfo);
 
         mniClassTree.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_4, java.awt.event.InputEvent.CTRL_MASK));
         mniClassTree.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/classSelection.png"))); // NOI18N
         mniClassTree.setMnemonic('a');
         mniClassTree.setText(bundle.getString("CismapPlugin.mniClassTree.text")); // NOI18N
         mniClassTree.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniClassTreeActionPerformed(evt);
             }
         });
         menWindows.add(mniClassTree);
 
         mniServerInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_5, java.awt.event.InputEvent.CTRL_MASK));
         mniServerInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/serverInfo.png"))); // NOI18N
         mniServerInfo.setMnemonic('S');
         mniServerInfo.setText(bundle.getString("CismapPlugin.mniServerInfo.text")); // NOI18N
         mniServerInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniServerInfoActionPerformed(evt);
             }
         });
         menWindows.add(mniServerInfo);
 
         mniLayerInfo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_6, java.awt.event.InputEvent.CTRL_MASK));
         mniLayerInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/commons/gui/capabilitywidget/res/layerInfo.png"))); // NOI18N
         mniLayerInfo.setMnemonic('L');
         mniLayerInfo.setText(bundle.getString("CismapPlugin.mniLayerInfo.text")); // NOI18N
         mniLayerInfo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLayerInfoActionPerformed(evt);
             }
         });
         menWindows.add(mniLayerInfo);
 
         mniLegend.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_7, java.awt.event.InputEvent.CTRL_MASK));
         mniLegend.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/cismet/cismap/navigatorplugin/res/legend.png"))); // NOI18N
         mniLegend.setMnemonic('L');
         mniLegend.setText(bundle.getString("CismapPlugin.mniLegend.text")); // NOI18N
         mniLegend.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniLegendActionPerformed(evt);
             }
         });
         menWindows.add(mniLegend);
 
         mniFeatureControl.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_8, java.awt.event.InputEvent.CTRL_MASK));
         mniFeatureControl.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/objects.png"))); // NOI18N
         mniFeatureControl.setMnemonic('O');
         mniFeatureControl.setText(bundle.getString("CismapPlugin.mniFeatureControl.text")); // NOI18N
         mniFeatureControl.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniFeatureControlActionPerformed(evt);
             }
         });
         menWindows.add(mniFeatureControl);
 
         mniMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_9, java.awt.event.InputEvent.CTRL_MASK));
         mniMap.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/map.png"))); // NOI18N
         mniMap.setMnemonic('M');
         mniMap.setText(bundle.getString("CismapPlugin.mniMap.text")); // NOI18N
         mniMap.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniMapActionPerformed(evt);
             }
         });
         menWindows.add(mniMap);
 
         mniOverview.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_0, java.awt.event.InputEvent.CTRL_MASK));
         mniOverview.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/map.png"))); // NOI18N
         mniOverview.setMnemonic('M');
         mniOverview.setText(bundle.getString("CismapPlugin.mniOverview.text")); // NOI18N
         mniOverview.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniOverviewActionPerformed(evt);
             }
         });
         menWindows.add(mniOverview);
         menWindows.add(jSeparator7);
 
         mniResetWindowLayout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
         mniResetWindowLayout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/layout.png"))); // NOI18N
         mniResetWindowLayout.setText(bundle.getString("CismapPlugin.mniResetWindowLayout.text")); // NOI18N
         mniResetWindowLayout.setToolTipText(bundle.getString("CismapPlugin.mniResetWindowLayout.toolTipText")); // NOI18N
         mniResetWindowLayout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniResetWindowLayoutActionPerformed(evt);
             }
         });
         menWindows.add(mniResetWindowLayout);
 
         mnuBar.add(menWindows);
 
         menHelp.setMnemonic('H');
         menHelp.setText(bundle.getString("CismapPlugin.menHelp.text")); // NOI18N
         menHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 menHelpActionPerformed(evt);
             }
         });
 
         mniOnlineHelp.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0));
         mniOnlineHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/help.png"))); // NOI18N
         mniOnlineHelp.setText(bundle.getString("CismapPlugin.mniOnlineHelp.text")); // NOI18N
         mniOnlineHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniOnlineHelpActionPerformed(evt);
             }
         });
         menHelp.add(mniOnlineHelp);
 
         mniNews.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/news.png"))); // NOI18N
         mniNews.setText(bundle.getString("CismapPlugin.mniNews.text")); // NOI18N
         mniNews.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniNewsActionPerformed(evt);
             }
         });
         menHelp.add(mniNews);
 
         mniAbout.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
         mniAbout.setText(bundle.getString("CismapPlugin.mniAbout.text")); // NOI18N
         mniAbout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mniAboutActionPerformed(evt);
             }
         });
         menHelp.add(mniAbout);
 
         mnuBar.add(menHelp);
 
         setJMenuBar(mnuBar);
     }// </editor-fold>//GEN-END:initComponents
     private void mniRedoPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRedoPerformed
         log.info("REDO");
         CustomAction a = mapC.getMemRedo().getLastAction();
         log.debug("... Aktion ausf\u00FChren: " + a.info());
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error beim Ausf\u00FChren der Aktion", e);
         }
         CustomAction inverse = a.getInverse();
         mapC.getMemUndo().addAction(inverse);
         log.debug("... neue Aktion auf UNDO-Stack: " + inverse);
         log.debug("... fertig");
     }//GEN-LAST:event_mniRedoPerformed
 
     private void mniUndoPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniUndoPerformed
         log.info("UNDO");
         CustomAction a = mapC.getMemUndo().getLastAction();
         log.debug("... Aktion ausf\u00FChren: " + a.info());
         try {
             a.doAction();
         } catch (Exception e) {
             log.error("Error beim Ausf\u00FChren der Aktion", e);
         }
         CustomAction inverse = a.getInverse();
         mapC.getMemRedo().addAction(inverse);
         log.debug("... neue Aktion auf REDO-Stack: " + inverse);
         log.debug("... fertig");
     }//GEN-LAST:event_mniUndoPerformed
 
     private void mniGeoLinkClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniGeoLinkClipboardActionPerformed
         Thread t = new Thread(new Runnable() {
 
             public void run() {
 //                EventQueue.invokeLater(new Runnable() {
 //                    public void run() {
 //                        clipboarder.setLocationRelativeTo(CismapPlugin.this);
 //                        clipboarder.setVisible(true);
 //                    }
 //                });
                 BoundingBox bb = mapC.getCurrentBoundingBox();
                 String u = "http://localhost:" + httpInterfacePort + "/gotoBoundingBox?x1=" + bb.getX1() + "&y1=" + bb.getY1() + "&x2=" + bb.getX2() + "&y2=" + bb.getY2();
                 GeoLinkUrl url = new GeoLinkUrl(u);
                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(url, null);
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         clipboarder.dispose();
                     }
                 });
 
             }
         });
         t.start();
     }//GEN-LAST:event_mniGeoLinkClipboardActionPerformed
 
     private void menHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menHelpActionPerformed
     }//GEN-LAST:event_menHelpActionPerformed
 
     private void mniAboutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniAboutActionPerformed
         if (about == null) {
             about = new AboutDialog(this, true);
         }
         about.setLocationRelativeTo(this);
         about.setVisible(true);
     }//GEN-LAST:event_mniAboutActionPerformed
 
     private void mniNewsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniNewsActionPerformed
         openUrlInExternalBrowser(newsUrl);
     }//GEN-LAST:event_mniNewsActionPerformed
 
     private void mniOnlineHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniOnlineHelpActionPerformed
         openUrlInExternalBrowser(helpUrl);
     }//GEN-LAST:event_mniOnlineHelpActionPerformed
 
     private void mniGotoPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniGotoPointActionPerformed
         log.debug("mniGotoPointActionPerformed");
         try {
             BoundingBox c = mapC.getCurrentBoundingBox();
             double x = (c.getX1() + c.getX2()) / 2;
             double y = (c.getY1() + c.getY2()) / 2;
             String s = JOptionPane.showInputDialog(this, "Zentriere auf folgendem Punkt: x,y", StaticDecimalTools.round(x) + "," + StaticDecimalTools.round(y));
 
             String[] sa = s.split(",");
             Double gotoX = new Double(sa[0]);
             Double gotoY = new Double(sa[1]);
             BoundingBox bb = new BoundingBox(gotoX, gotoY, gotoX, gotoY);
             mapC.gotoBoundingBox(bb, true, false, mapC.getAnimationDuration());
         } catch (Exception skip) {
             log.error("Fehler in mniGotoPointActionPerformed", skip);
         }
     }//GEN-LAST:event_mniGotoPointActionPerformed
 
     private void mniScaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniScaleActionPerformed
         try {
             String s = JOptionPane.showInputDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("Massstab_manuell_auswaehlen"), ((int) mapC.getScaleDenominator()) + "");
             Integer i = new Integer(s);
             mapC.gotoBoundingBoxWithHistory(mapC.getBoundingBoxFromScale(i));
         } catch (Exception skip) {
             log.error("Fehler in mniScaleActionPerformed", skip);
         }
     }//GEN-LAST:event_mniScaleActionPerformed
 
     private void mniMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniMapActionPerformed
         showOrHideView(vMap);
     }//GEN-LAST:event_mniMapActionPerformed
 
     private void mniRemoveAllObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRemoveAllObjectsActionPerformed
         if (mapC != null) {
             Vector v = new Vector(mapC.getFeatureCollection().getAllFeatures());
             mapC.getFeatureCollection().removeFeatures(v);
         }
     }//GEN-LAST:event_mniRemoveAllObjectsActionPerformed
 
     private void mniZoomToSelectedObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniZoomToSelectedObjectsActionPerformed
         if (mapC != null) {
             mapC.zoomToSelection();
         }
 
     }//GEN-LAST:event_mniZoomToSelectedObjectsActionPerformed
 
     private void mniZoomToAllObjectsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniZoomToAllObjectsActionPerformed
         if (mapC != null) {
             mapC.zoomToFeatureCollection();
         }
     }//GEN-LAST:event_mniZoomToAllObjectsActionPerformed
 
     private void mniForwardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniForwardActionPerformed
         if (mapC != null && mapC.isForwardPossible()) {
             mapC.forward(true);
         }
     }//GEN-LAST:event_mniForwardActionPerformed
 
     private void mniBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniBackActionPerformed
         if (mapC != null && mapC.isBackPossible()) {
             mapC.back(true);
         }
     }//GEN-LAST:event_mniBackActionPerformed
 
     private void mniHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniHomeActionPerformed
         cmdHomeActionPerformed(null);
     }//GEN-LAST:event_mniHomeActionPerformed
 
     private void mniRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRefreshActionPerformed
         cmdRefreshActionPerformed(null);
     }//GEN-LAST:event_mniRefreshActionPerformed
 
     private void mniRemoveSelectedObjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniRemoveSelectedObjectActionPerformed
         if (mapC != null) {
             Vector v = new Vector(mapC.getFeatureCollection().getSelectedFeatures());
             mapC.getFeatureCollection().removeFeatures(v);
         }
     }//GEN-LAST:event_mniRemoveSelectedObjectActionPerformed
 
     private void mniLoadConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadConfigActionPerformed
         JFileChooser fc;
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
         fc.setFileFilter(new FileFilter() {
 
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".xml");
             }
 
             public String getDescription() {
                 return java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("ConfigDescription");
             }
         });
         int state = fc.showOpenDialog(this);
         if (state == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".xml")) {
                 activeLayers.removeAllLayers();
                 mapC.getRasterServiceLayer().removeAllChildren();
                 configurationManager.configure(name);
             } else {
                 activeLayers.removeAllLayers();
                 mapC.getRasterServiceLayer().removeAllChildren();
                 configurationManager.configure(name + ".xml");
             }
         }
     }//GEN-LAST:event_mniLoadConfigActionPerformed
 
     private void mniSaveConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveConfigActionPerformed
         JFileChooser fc;
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
         fc.setFileFilter(new FileFilter() {
 
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".xml");
             }
 
             public String getDescription() {
                 return java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("ConfigDescription");
             }
         });
         int state = fc.showSaveDialog(this);
         log.debug("state:" + state);
         if (state == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".xml")) {
                 configurationManager.writeConfiguration(name);
             } else {
                 configurationManager.writeConfiguration(name + ".xml");
             }
         }
     }//GEN-LAST:event_mniSaveConfigActionPerformed
 
     private void mniLoadConfigFromServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadConfigFromServerActionPerformed
         activeLayers.removeAllLayers();
         mapC.getMapServiceLayer().removeAllChildren();
         configureApp(true);
     }//GEN-LAST:event_mniLoadConfigFromServerActionPerformed
 
     private void mniPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniPrintActionPerformed
         cmdPrintActionPerformed(null);
     }//GEN-LAST:event_mniPrintActionPerformed
 
     private void mniSaveLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniSaveLayoutActionPerformed
         JFileChooser fc;
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
         fc.setFileFilter(new FileFilter() {
 
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".layout");
             }
 
             public String getDescription() {
                 return "Layout";
             }
         });
         fc.setMultiSelectionEnabled(false);
         int state = fc.showSaveDialog(this);
         log.debug("state:" + state);
         if (state == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             log.debug("file:" + file);
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".layout")) {
                 saveLayout(name);
             } else {
                 saveLayout(name + ".layout");
             }
         }
     }//GEN-LAST:event_mniSaveLayoutActionPerformed
 
     private void mniLoadLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLoadLayoutActionPerformed
         JFileChooser fc;
         try {
             fc = new JFileChooser(cismapDirectory);
         } catch (Exception bug) {
             // Bug Workaround http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6544857
             fc = new JFileChooser(cismapDirectory, new RestrictedFileSystemView());
         }
         fc.setFileFilter(new FileFilter() {
 
             public boolean accept(File f) {
                 return f.getName().toLowerCase().endsWith(".layout");
             }
 
             public String getDescription() {
                 return "Layout";
             }
         });
         fc.setMultiSelectionEnabled(false);
         int state = fc.showOpenDialog(this);
         if (state == JFileChooser.APPROVE_OPTION) {
             File file = fc.getSelectedFile();
             String name = file.getAbsolutePath();
             name = name.toLowerCase();
             if (name.endsWith(".layout")) {
                 loadLayout(name);
             } else {
                 JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.format_failure_message"), java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.message_title"), JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }//GEN-LAST:event_mniLoadLayoutActionPerformed
 
     private void mniClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniClipboardActionPerformed
         cmdClipboardActionPerformed(null);
     }//GEN-LAST:event_mniClipboardActionPerformed
 
     private void mniCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCloseActionPerformed
         this.dispose();
         System.exit(0);
     }//GEN-LAST:event_mniCloseActionPerformed
 
     private void menEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menEditActionPerformed
     }//GEN-LAST:event_menEditActionPerformed
 
     private void menWindowsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menWindowsActionPerformed
     }//GEN-LAST:event_menWindowsActionPerformed
     BoundingBox buffer;
 
     private void tlbMainMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tlbMainMouseClicked
         if (evt.getClickCount() == 3) {
             //DockingManager.dock((Dockable)vDebug,(Dockable)vMap, DockingConstants.SOUTH_REGION, .25f);
             //DockingManager.dock((Dockable)vGroovy,(Dockable)vMap);
         }
     }//GEN-LAST:event_tlbMainMouseClicked
 
     private void cmdPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPrintActionPerformed
         String oldMode = mapC.getInteractionMode();
         log.debug("oldInteractionMode:" + oldMode);
         Enumeration en = cmdGroupPrimaryInteractionMode.getElements();
         togInvisible.setSelected(true);
         mapC.showPrintingSettingsDialog(oldMode);
     }//GEN-LAST:event_cmdPrintActionPerformed
 
     private void cmdClipboardActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdClipboardActionPerformed
         Thread t = new Thread(new Runnable() {
 
             public void run() {
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         clipboarder.setLocationRelativeTo(CismapPlugin.this);
                         clipboarder.setVisible(true);
                     }
                 });
                 ImageSelection imgSel = new ImageSelection(mapC.getImage());
                 Toolkit.getDefaultToolkit().getSystemClipboard().setContents(imgSel, null);
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         clipboarder.dispose();
                     }
                 });
             }
         });
         t.start();
     }//GEN-LAST:event_cmdClipboardActionPerformed
 
     private void cmdSnapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSnapActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setSnappingEnabled(cmdSnap.isSelected());
             }
         });
     }//GEN-LAST:event_cmdSnapActionPerformed
 
     private void cmdRemoveGeometryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRemoveGeometryActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setInteractionMode(MappingComponent.REMOVE_POLYGON);
             }
         });
     }//GEN-LAST:event_cmdRemoveGeometryActionPerformed
 
     private void cmdMoveGeometryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdMoveGeometryActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setInteractionMode(MappingComponent.MOVE_POLYGON);
             }
         });
     }//GEN-LAST:event_cmdMoveGeometryActionPerformed
 
     private void cmdNewPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewPointActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 ((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(CreateNewGeometryListener.POINT);
                 mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
             }
         });
     }//GEN-LAST:event_cmdNewPointActionPerformed
 
     private void cmdNewPolygonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNewPolygonActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 ((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(CreateNewGeometryListener.POLYGON);
                 mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
             }
         });
     }//GEN-LAST:event_cmdNewPolygonActionPerformed
 
     private void createGeometryAction(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createGeometryAction
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 ((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).setMode(CreateNewGeometryListener.LINESTRING);
                 mapC.setInteractionMode(MappingComponent.NEW_POLYGON);
             }
         });
     }//GEN-LAST:event_createGeometryAction
 
     private void cmdNodeRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNodeRemoveActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {               
                 mapC.setHandleInteractionMode(MappingComponent.REMOVE_HANDLE);
                 mapC.setInteractionMode(MappingComponent.SELECT);
             }
         });
     }//GEN-LAST:event_cmdNodeRemoveActionPerformed
 
     private void cmdNodeAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNodeAddActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setHandleInteractionMode(MappingComponent.ADD_HANDLE);
                 mapC.setInteractionMode(MappingComponent.SELECT);
             }
         });
     }//GEN-LAST:event_cmdNodeAddActionPerformed
 
     private void cmdNodeMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNodeMoveActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setHandleInteractionMode(MappingComponent.MOVE_HANDLE);
                 mapC.setInteractionMode(MappingComponent.SELECT);
             }
         });
     }//GEN-LAST:event_cmdNodeMoveActionPerformed
 
     private void cmdSelectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdSelectActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 mapC.setInteractionMode(MappingComponent.SELECT);
             }
         });
     }//GEN-LAST:event_cmdSelectActionPerformed
 
     private void mniResetWindowLayoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniResetWindowLayoutActionPerformed
         setupDefaultLayout();
     }//GEN-LAST:event_mniResetWindowLayoutActionPerformed
 
     private void mniFeatureControlActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniFeatureControlActionPerformed
         showOrHideView(vFeatureControl);
     }//GEN-LAST:event_mniFeatureControlActionPerformed
 
     private void mnuConfigServerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuConfigServerActionPerformed
         activeLayers.removeAllLayers();
         mapC.getRasterServiceLayer().removeAllChildren();
         //mapC.resetWtst();
         configureApp(true);
     }//GEN-LAST:event_mnuConfigServerActionPerformed
 
     private void cmdReconfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdReconfigActionPerformed
         activeLayers.removeAllLayers();
         mapC.getRasterServiceLayer().removeAllChildren();
         //mapC.resetWtst();
         configureApp(false);
     }//GEN-LAST:event_cmdReconfigActionPerformed
 
     private void mniFeatureInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniFeatureInfoActionPerformed
         showOrHideView(vFeatureInfo);
     }//GEN-LAST:event_mniFeatureInfoActionPerformed
 
     private void mniServerInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniServerInfoActionPerformed
         showOrHideView(vServerInfo);
     }//GEN-LAST:event_mniServerInfoActionPerformed
 
     private void mniLayerInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLayerInfoActionPerformed
         showOrHideView(vLayerInfo);
     }//GEN-LAST:event_mniLayerInfoActionPerformed
 
     private void mniLegendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLegendActionPerformed
         showOrHideView(vLegend);
     }//GEN-LAST:event_mniLegendActionPerformed
 
     private void mniCapabilitiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniCapabilitiesActionPerformed
         showOrHideView(vCaps);
     }//GEN-LAST:event_mniCapabilitiesActionPerformed
 
     private void mniClassTreeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniClassTreeActionPerformed
         showOrHideView(vMetaSearch);
     }//GEN-LAST:event_mniClassTreeActionPerformed
 
     private void mniLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniLayerActionPerformed
         showOrHideView(vLayers);
     }//GEN-LAST:event_mniLayerActionPerformed
 
     private void cmdFeatureInfoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdFeatureInfoActionPerformed
         mapC.setInteractionMode(MappingComponent.FEATURE_INFO);
     }//GEN-LAST:event_cmdFeatureInfoActionPerformed
 
     private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
     }//GEN-LAST:event_formComponentShown
 
     private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
         log.info("CLOSE");
     }//GEN-LAST:event_formWindowClosed
 
     private void showOrHideView(View v) {
         ///irgendwas besser als Closable ??
         // Problem wenn floating --> close -> open  (muss zweimal open)
         if (v.isClosable()) {
             v.close();
         } else {
             v.restore();
         }
     }
 
     private void cmdPanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPanActionPerformed
         if (mapC != null) {
             mapC.setInteractionMode(MappingComponent.PAN);
         }
     }//GEN-LAST:event_cmdPanActionPerformed
 
     private void cmdZoomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdZoomActionPerformed
         if (mapC != null) {
             mapC.setInteractionMode(MappingComponent.ZOOM);
         }
     }//GEN-LAST:event_cmdZoomActionPerformed
 
     private void cmdRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdRefreshActionPerformed
         if (mapC != null) {
             mapC.refresh();
         }
     }//GEN-LAST:event_cmdRefreshActionPerformed
 
     private void cmdHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdHomeActionPerformed
         if (mapC != null) {
             mapC.gotoInitialBoundingBox();
         }
     }//GEN-LAST:event_cmdHomeActionPerformed
 
     private void cmdBackActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdBackActionPerformed
 //        mapC.back(true);
     }//GEN-LAST:event_cmdBackActionPerformed
 
     private void panMainMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panMainMouseEntered
     }//GEN-LAST:event_panMainMouseEntered
 
     private void panMainMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_panMainMouseExited
     }//GEN-LAST:event_panMainMouseExited
 
     private void cmdNodeRotateGeometryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdNodeRotateGeometryActionPerformed
         EventQueue.invokeLater(new Runnable() {
 
             public void run() {
                 //mapC.setInteractionMode(MappingComponent.SELECT);
                 mapC.setHandleInteractionMode(MappingComponent.ROTATE_POLYGON);
             }
         });
 }//GEN-LAST:event_cmdNodeRotateGeometryActionPerformed
 
     private void mniOverviewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniOverviewActionPerformed
         showOrHideView(vOverview);
 
 }//GEN-LAST:event_mniOverviewActionPerformed
 
 private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
     if (this.getExtendedState() != MAXIMIZED_BOTH) {
         oldWindowDimension.setSize(getWidth(), getHeight());
         oldWindowPositionX = (int) this.getLocation().getX();
         oldWindowPositionY = (int) this.getLocation().getY();
     }
 
 }//GEN-LAST:event_formComponentResized
 
 private void mniOptionsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mniOptionsActionPerformed
     final OptionsDialog od = new OptionsDialog(this, true);
     od.setLocationRelativeTo(this);
     od.setVisible(true);
 }//GEN-LAST:event_mniOptionsActionPerformed
 
 private void cmdPluginSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmdPluginSearchActionPerformed
 
 }//GEN-LAST:event_cmdPluginSearchActionPerformed
 
 private void popMenuSearchPopupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent evt) {//GEN-FIRST:event_popMenuSearchPopupMenuWillBecomeVisible
     searchMenuSelectedAction.actionPerformed(null);
 }//GEN-LAST:event_popMenuSearchPopupMenuWillBecomeVisible
 
 private void menSearchMenuSelected(javax.swing.event.MenuEvent evt) {//GEN-FIRST:event_menSearchMenuSelected
     searchMenuSelectedAction.actionPerformed(null);
 }//GEN-LAST:event_menSearchMenuSelected
 
 //    private void addShutdownHook() {
 //        ShutdownHook shutdownHook = new ShutdownHook();
 //        Runtime.getRuntime().addShutdownHook(shutdownHook);
 //
 //    }
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
 
         Thread t = new Thread(new Runnable() {
 
             public void run() {
                 final CismapPlugin cp = new CismapPlugin();
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         //        cp.addShutdownHook();
                         cp.setVisible(true);
                     }
                 });
             }
         });
         t.start();
 //        cp.configureApp(false);
     }
 
     private void configureApp(final boolean serverFirst) {
         try {
             EventQueue.invokeLater(new Runnable() {
 
                 public void run() {
                     validateTree();
                 }
             });
         } catch (final Throwable t) {
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 public void run() {
                     log.warn("Fehler in validateTree()", t);
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
 
     public Vector<Feature> getAllFeaturesSorted() {
         return featureControl.getAllFeaturesSorted();
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
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
     private javax.swing.JMenuItem mniBuffer;
     private javax.swing.JMenuItem mniBuffer1;
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
     private javax.swing.JMenuItem mniRedo;
     private javax.swing.JMenuItem mniRedo1;
     private javax.swing.JMenuItem mniRefresh;
     private javax.swing.JMenuItem mniRemoveAllObjects;
     private javax.swing.JMenuItem mniRemoveSelectedObject;
     private javax.swing.JMenuItem mniResetWindowLayout;
     private javax.swing.JMenuItem mniSaveConfig;
     private javax.swing.JMenuItem mniSaveLayout;
     private javax.swing.JMenuItem mniScale;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse;
     private javax.swing.JRadioButtonMenuItem mniSearchEllipse1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon;
     private javax.swing.JRadioButtonMenuItem mniSearchPolygon1;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline;
     private javax.swing.JRadioButtonMenuItem mniSearchPolyline1;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle;
     private javax.swing.JRadioButtonMenuItem mniSearchRectangle1;
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
     private javax.swing.JPopupMenu popMenuSearch;
     private javax.swing.JSeparator sepAfterPos;
     private javax.swing.JSeparator sepBeforePos;
     private javax.swing.JSeparator sepServerProfilesEnd;
     private javax.swing.JSeparator sepServerProfilesStart;
     private javax.swing.JToolBar tlbMain;
     private javax.swing.JToggleButton togInvisible;
     // End of variables declaration//GEN-END:variables
 
     public PluginUI getUI(String str) {
         return this;
     }
 
     public PluginMethod getMethod(String str) {
         return pluginMethods.get(str);
     }
 
     public void setActive(boolean param) {
         log.debug("setActive:" + param);
         if (!param) {
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
             //CismapBroker.getInstance().cleanUpSystemRegistry();
             saveLayout(cismapDirectory + fs + pluginLayoutName);
         }
     }
 
     public java.util.Iterator getUIs() {
         LinkedList ll = new LinkedList();
         ll.add(this);
         return ll.iterator();
     }
 
     public PluginProperties getProperties() {
         return myPluginProperties;
     }
 
     public java.util.Iterator getMethods() {
         return this.pluginMethods.values().iterator();
     }
 
     public void shown() {
     }
 
     public void resized() {
     }
 
     public void moved() {
     }
 
     public void hidden() {
     }
 
     public java.util.Collection getMenus() {
         return menues;
     }
 
     public String getId() {
         return "cismap";
     }
 
     public JComponent getComponent() {
         return panAll;
     }
 
     public java.util.Collection getButtons() {
         //return Arrays.asList(this.tobVerdis.getComponents());
         return null;
     }
 
     public void floatingStopped() {
     }
 
     public void floatingStarted() {
     }
 
     @Override
     public void setVisible(boolean b) {
         if (!plugin) {
             super.setVisible(b);
         }
     }
 
     @Override
     public void dispose() {
         try {
             log.debug("dispose().CIAO");
             saveLayout(cismapDirectory + fs + standaloneLayoutName);
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
             //CismapBroker.getInstance().cleanUpSystemRegistry();
             super.dispose();
             System.exit(0);
         } catch (Throwable t) {
             log.fatal("Fehler beim Beenden. Abschuss freigegeben ;-)", t);
         }
     }
 
     public Element getConfiguration() {
         Element ret = new Element("cismapPluginUIPreferences");
         Element window = new Element("window");
 
         int windowHeight = this.getHeight();
         int windowWidth = this.getWidth();
         int windowX = (int) this.getLocation().getX();
         int windowY = (int) this.getLocation().getY();
         boolean windowMaximised = (this.getExtendedState() == MAXIMIZED_BOTH);
         if (windowMaximised) {
             window.setAttribute("height", "" + (int) oldWindowDimension.getHeight());
             window.setAttribute("width", "" + (int) oldWindowDimension.getWidth());
             window.setAttribute("x", "" + oldWindowPositionX);
             window.setAttribute("y", "" + oldWindowPositionY);
         } else {
             window.setAttribute("height", "" + windowHeight);
             window.setAttribute("width", "" + windowWidth);
             window.setAttribute("x", "" + windowX);
             window.setAttribute("y", "" + windowY);
         }
 
 
         window.setAttribute("max", "" + windowMaximised);
 
         ret.addContent(window);
         //Iterator<View> it=viewport.getViewset().iterator();
 //        Iterator<View> it=viewMap.getViewCount());
 //        while (it.hasNext()) {
 //            View elem = it.next();
 //            Element view=new Element("DockingView");
 //            view.setAttribute(new Attribute("name",elem.getPersistentId()));
 //            //view.setAttribute(new Attribute("shown",new Boolean(elem.isShowing()).toString()));
 //            view.setAttribute(new Attribute("shown",new Boolean(elem.isVisible()).toString()));
 //            view.setAttribute(new Attribute("height",""+elem.getHeight()));
 //            view.setAttribute(new Attribute("width",""+elem.getWidth()));
 //            ret.addContent(view);
 //        }
         return ret;
     }
 
     @Override
     public void masterConfigure(Element e) {
         Element prefs = e.getChild("cismapPluginUIPreferences");
         cismapPluginUIPreferences = prefs;
         try {
             Element help_url_element = prefs.getChild("help_url");
             Element news_url_element = prefs.getChild("news_url");
             Element httpInterfacePortElement = prefs.getChild("httpInterfacePort");
             try {
                 httpInterfacePort = new Integer(httpInterfacePortElement.getText());
             } catch (Throwable t) {
                 log.warn("httpInterface wurde nicht konfiguriert. Wird auf Standardwert gesetzt:" + httpInterfacePort, t);
             }
             helpUrl = help_url_element.getText();
             log.debug("helpUrl:" + helpUrl);
 
             newsUrl = news_url_element.getText();
 
         } catch (Throwable t) {
             log.error("Fehler beim Laden der Hilfeurls (" + prefs.getChildren() + ")", t);
         }
 
         windows2skip = new Vector<String>();
         try {
             Element windows2SkipElement = e.getChild("skipWindows");
             Iterator<Element> it = windows2SkipElement.getChildren("skip").iterator();
             while (it.hasNext()) {
                 Element next = it.next();
                 String id = next.getAttributeValue("windowid");
                 windows2skip.add(id);
                 View v = viewMap.getView(id);
                 if (v != null) {
                     v.close();
                 }
                 JMenuItem menu = viewMenuMap.get(id);
                 if (menu != null) {
                     menu.setVisible(false);
                 }
             }
         } catch (Exception x) {
             log.info("Keine skipWindow Info vorhanden oder Fehler beim Lesen der Einstellungen", x);
         }
 
 
 
 
 
 
         try {
 
             //Analysieren des FileMenues
             Vector<Component> before = new Vector<Component>();
             Vector<Component> after = new Vector<Component>();
             after.add(sepServerProfilesEnd);
 
 
             Component[] comps = menFile.getMenuComponents();
             Vector<Component> active = before;
             for (Component comp : comps) {
                 //Component comp=menFile.getMenuComponent(i);
                 if (active != null) {
                     active.add(comp);
                 }
 
                 if (active == before && comp.getName() != null && comp.getName().trim().equals("sepServerProfilesStart")) { //erster Separator
                     active = null;
                 } else if (active == null && comp.getName() != null && comp.getName().trim().equals("sepServerProfilesEnd")) { //zweiter Separator
                     active = after;
                 }
 
             }
 
 
             Vector<JMenuItem> serverProfileItems = new Vector<JMenuItem>();
 
 
             Element serverprofiles = e.getChild("serverProfiles");
             Iterator<Element> it = serverprofiles.getChildren("profile").iterator();
             while (it.hasNext()) {
                 final Element next = it.next();
                 final String id = next.getAttributeValue("id");
                 final String sorter = next.getAttributeValue("sorter");
                 final String name = next.getAttributeValue("name");
                 final String path = next.getAttributeValue("path");
                 final String icon = next.getAttributeValue("icon");
                 final String descr = next.getAttributeValue("descr");
                 final String descrWidth = next.getAttributeValue("descrwidth");
                 final String complexDescriptionText = next.getTextTrim();
                 final String complexDescriptionSwitch = next.getAttributeValue("complexdescr");
 
                 JMenuItem serverProfileMenuItem = new JMenuItem();
                 serverProfileMenuItem.setText(name);
                 serverProfileMenuItem.addActionListener(new ActionListener() {
 
                     @Override
                     public void actionPerformed(ActionEvent e) {
                         try {
                             ((ActiveLayerModel) mapC.getMappingModel()).removeAllLayers();
                             configurationManager.configureFromClasspath(path, null);
                             setButtonSelectionAccordingToMappingComponent();
                         } catch (Throwable ex) {
                             log.fatal("Nix ServerProfile", ex);
                         }
                     }
                 });
                 serverProfileMenuItem.setName("ServerProfile:" + sorter + ":" + name);
 
                 if (complexDescriptionSwitch != null && complexDescriptionSwitch.equalsIgnoreCase("true") && complexDescriptionText != null) {
                     serverProfileMenuItem.setToolTipText(complexDescriptionText);
                 } else if (descrWidth != null) {
                     serverProfileMenuItem.setToolTipText("<html><table width=\"" + descrWidth + "\" border=\"0\"><tr><td>" + descr + "</p></td></tr></table></html>");
                 } else {
                     serverProfileMenuItem.setToolTipText(descr);
                 }
 
 
 
 
                 try {
                     serverProfileMenuItem.setIcon(new javax.swing.ImageIcon(getClass().getResource(icon)));
                 } catch (Exception iconE) {
                     log.warn("Could not create Icon for ServerProfile.", iconE);
                 }
 
                 serverProfileItems.add(serverProfileMenuItem);
 
             }
 
 
             Collections.sort(serverProfileItems, new Comparator<JMenuItem>() {
 
                 @Override
                 public int compare(JMenuItem o1, JMenuItem o2) {
                     if (o1.getName() != null && o2.getName() != null) {
                         return o1.getName().compareTo(o2.getName());
                     } else {
                         return 0;
                     }
                 }
             });
 
             menFile.removeAll();
 
             for (Component c : before) {
                 menFile.add(c);
             }
             for (JMenuItem jmi : serverProfileItems) {
                 menFile.add(jmi);
             }
             for (Component c : after) {
                 menFile.add(c);
             }
 
         } catch (Exception x) {
             log.info("Keine Serverprofile vorhanden, oder Fehler bei der Auswertung", x);
         }
     }
 
     @Override
     public void configure(Element e) {
         Element prefs = e.getChild("cismapPluginUIPreferences");
         cismapPluginUIPreferences = prefs;
         try {
             Element window = prefs.getChild("window");
             final int windowHeight = window.getAttribute("height").getIntValue();
             final int windowWidth = window.getAttribute("width").getIntValue();
             final int windowX = window.getAttribute("x").getIntValue();
             final int windowY = window.getAttribute("y").getIntValue();
             oldWindowDimension.setSize(windowWidth, windowHeight);
             oldWindowPositionX = windowX;
             oldWindowPositionY = windowY;
             final boolean windowMaximised = window.getAttribute("max").getBooleanValue();
 //            log.fatal("is EDT?"+EventQueue.isDispatchThread());
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
             log.error("Fehler beim Laden der Fenstergroesse", t);
         }
     }
 
 //    private void configureActiveTabAfterVisibility() {
 //        Iterator<Element> it=cismapPluginUIPreferences.getChildren("DockingView").iterator();
 //        while (it.hasNext()) {
 //            Element elem = it.next();
 //            String name=elem.getAttribute("name").getValue();
 //            boolean shown=false;
 //            try {
 //                shown=elem.getAttribute("shown").getBooleanValue();
 //            } catch (Exception ex) {
 //
 //            }
 //            if (viewsHM.get(name)!=null) {
 //                try {
 //                    int height=elem.getAttribute("height").getIntValue();
 //
 //                    int width=elem.getAttribute("width").getIntValue();
 //                    viewsHM.get(name).setPreferredSize(new Dimension(width,height));
 //                } catch(Throwable t) {
 //                    log.warn("Fehler beim setzten von der preferredSize der Views.",t);
 //                }
 //            }
 //            if (shown&&viewsHM.get(name)!=null) {
 //                viewsHM.get(name).setActive(true);
 //            }
 //        }
     //geht nicht... :-( aber warum ???
     //vMap.setActive(true);
 //    }
     class ShutdownHook extends Thread {
 
         @Override
         public void run() {
             log.debug("CIAO");
             configurationManager.writeConfiguration();
             CismapBroker.getInstance().writePropertyFile();
             log.debug("Shutdownhook --> saving layout");
             saveLayout(cismapDirectory + fs + standaloneLayoutName);
         }
     }
 
     public void loadLayout(String file) {
         setupDefaultLayout();
         log.debug("Load Layout.. from " + file);
         File layoutFile = new File(file);
 
         if (layoutFile.exists()) {
             log.debug("Layout File exists");
             try {
                 FileInputStream layoutInput = new FileInputStream(layoutFile);
                 ObjectInputStream in = new ObjectInputStream(layoutInput);
                 rootWindow.read(in);
                 in.close();
                 rootWindow.getWindowBar(Direction.LEFT).setEnabled(true);
                 rootWindow.getWindowBar(Direction.RIGHT).setEnabled(true);
                 /*if (isInit) {
                 int count = viewMap.getViewCount();
                 for (int i = 0; i < count; i++) {
                 View curr = viewMap.getViewAtIndex(i);
                 if (curr.isUndocked()) {
                 curr.dock();
                 }
                 }
                 }*/
                 log.debug("Loading Layout successfull");
             } catch (IOException ex) {
                 log.error("Layout File IO Exception --> loading default Layout", ex);
                 if (isInit) {
                     JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.loading_layout_failure_message_init"), java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.message_title"), JOptionPane.INFORMATION_MESSAGE);
                     setupDefaultLayout();
                 } else {
                     JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.loading_layout_failure_message"), java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.message_title"), JOptionPane.INFORMATION_MESSAGE);
                 }
             }
         } else {
             if (isInit) {
                 log.fatal("Datei exitstiert nicht --> default layout (init)");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         //UGLY WINNING --> Gefixed durch IDW Version 1.5
                         //setupDefaultLayout();
                         //DeveloperUtil.createWindowLayoutFrame("nach setup1",rootWindow).setVisible(true);
                         setupDefaultLayout();
                         //DeveloperUtil.createWindowLayoutFrame("nach setup2",rootWindow).setVisible(true);
                     }
                 });
             } else {
                 log.fatal("Datei exitstiert nicht)");
                 JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.layout_does_not_exist"), java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.message_title"), JOptionPane.INFORMATION_MESSAGE);
             }
         }
     }
 
     public void saveLayout(String file) {
         log.debug("Saving Layout.. to " + file, new CurrentStackTrace());
         File layoutFile = new File(file);
         try {
             if (!layoutFile.exists()) {
                 log.debug("Saving Layout.. File does not exit");
                 layoutFile.createNewFile();
             } else {
                 log.debug("Saving Layout.. File does exit");
             }
             FileOutputStream layoutOutput = new FileOutputStream(layoutFile);
             ObjectOutputStream out = new ObjectOutputStream(layoutOutput);
             rootWindow.write(out);
             out.flush();
             out.close();
             log.debug("Saving Layout.. to " + file + " successfull");
         } catch (IOException ex) {
             JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.saving_layout_failure"), java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.InfoNode.message_title"), JOptionPane.INFORMATION_MESSAGE);
             log.error("A failure occured during writing the layout file", ex);
         }
     }
 
     public void mapSearchStarted(MapSearchEvent mse) {
         initMetaSearch(mse.getGeometry());
     }
 
     private void initMetaSearch(Geometry geom) {
         String geosuche = "";
         try {
             geosuche = context.getEnvironment().getParameter("geosuche");
         } catch (Exception e) {
             log.warn("Parameter geosuche nicht in plugin.xml gefunden, verwende " + geosuche, e);
         }
         Object object = context.getSearch().getDataBeans().get(geosuche);
         FormDataBean coordinatesDataBean = (FormDataBean) object;
 //        double[] boundingBox = new double[4];
 //        boundingBox[0] = mapC.getWtst().getWorldX(bounds.getX());
 //        boundingBox[1] = mapC.getWtst().getWorldY(bounds.getY() + bounds.getHeight());
 //        boundingBox[2] = mapC.getWtst().getWorldX(bounds.getX() + bounds.getWidth());
 //        boundingBox[3] = mapC.getWtst().getWorldY(bounds.getY());
 //        double[][] pointCoordinates = context.getToolkit().getPointCoordinates(boundingBox);
 //        String ogcPolygon = Sirius.navigator.tools.NavigatorToolkit.getToolkit().pointCoordinatesToOGCPolygon(pointCoordinates, true);
 //        log.error("ogcPolygon:"+ogcPolygon);
 //        log.fatal("newStuff:"+ geom.toText());
         coordinatesDataBean.setBeanParameter("featureString", geom.toText());
         context.getSearch().performSearch(metaSearch.getSearchTree().getSelectedClassNodeKeys(), coordinatesDataBean, context.getUserInterface().getFrameFor((PluginUI) this), false);
     }
 
     public void dropOnMap(MapDnDEvent mde) {
         log.fatal("drop on map");
         if (mde.getDte() instanceof DropTargetDropEvent) {
             DropTargetDropEvent dtde = (DropTargetDropEvent) mde.getDte();
             if (dtde.getTransferable().isDataFlavorSupported(fromCapabilityWidget)) {
                 activeLayers.drop(dtde);
             } else if (dtde.getTransferable().isDataFlavorSupported(fromNavigatorNode) && dtde.getTransferable().isDataFlavorSupported(fromNavigatorCollection)) {
                 //Drop von MetaObjects
                 try {
                     Object object = dtde.getTransferable().getTransferData(fromNavigatorCollection);
                     if (object instanceof Collection) {
                         Collection c = (Collection) object;
                         showObjectsMethod.invoke(c);
                     }
                 } catch (Throwable t) {
                     log.fatal(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Fehler_beim_Drop"), t);
                 }
             } else {
                 JOptionPane.showMessageDialog(this, java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Das_cismap_Plugin_konnte_den_Datentyp_nicht_verarbeiten."));
                 log.error(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.Das_cismap_Plugin_konnte_den_Datentyp_nicht_verarbeiten.") + dtde.getTransferable().getTransferDataFlavors()[0]);
             }
         }
 
     }
 
     public void showInMap(Collection c, boolean editable) throws Exception {
         showObjectsMethod.invoke(c, editable);
     }
 
     public CidsFeature showInMap(DefaultMetaTreeNode node, ObjectAttribute oAttr, boolean editable) throws Exception {
         return showObjectsMethod.invoke(node, oAttr, editable);
     }
 
     public void dragOverMap(MapDnDEvent mde) {
 //        if (mde.getDte() instanceof DropTargetDragEvent) {
 //            DropTargetDragEvent dtde=(DropTargetDragEvent)mde.getDte();
 //            if (dtde.getTransferable().isDataFlavorSupported(fromNavigatorNode)&&dtde.getTransferable().isDataFlavorSupported(fromNavigatorCollection)) {
 //                BufferedImage image=ComponentRegistry.getRegistry().getActiveCatalogue().getDragImage();
 //
 //            }
 //
 //        }
     }
 
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
         } else if (mapC.getInteractionMode().equals(MappingComponent.NEW_POLYGON)) {
             if (((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(CreateNewGeometryListener.POLYGON)) {
                 if (!cmdNewPolygon.isSelected()) {
                     cmdNewPolygon.setSelected(true);
                 }
             } else if (((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(CreateNewGeometryListener.LINESTRING)) {
                 if (!cmdNewLinestring.isSelected()) {
                     cmdNewLinestring.setSelected(true);
                 }
             } else if (((CreateNewGeometryListener) mapC.getInputListener(MappingComponent.NEW_POLYGON)).isInMode(CreateNewGeometryListener.POINT)) {
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
 
     public void statusValueChanged(StatusEvent e) {
         if (e.getName().equals(StatusEvent.MAPPING_MODE)) {
             //besser nur aufrufen wenn falsch
             setButtonSelectionAccordingToMappingComponent();
         }
     }
 
     public void historyChanged() {
         Vector backPos = mapC.getBackPossibilities();
         Vector forwPos = mapC.getForwardPossibilities();
         if (menHistory != null) {
             menHistory.removeAll();
             menHistory.add(mniBack);
             menHistory.add(mniForward);
             menHistory.add(mniHome);
             menHistory.add(sepBeforePos);
             int counter = 0;
 
             int start = 0;
             if (backPos.size() - 10 > 0) {
                 start = backPos.size() - 10;
             }
 
             for (int index = start; index < backPos.size(); ++index) {
                 Object elem = backPos.get(index);
                 JMenuItem item = new JMenuItem(elem.toString());//+" :"+new Integer(backPos.size()-1-index));
 
                 item.setIcon(miniBack);
                 final int pos = backPos.size() - 1 - index;
                 item.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
                         for (int i = 0; i < pos; ++i) {
                             mapC.back(false);
                         }
                         mapC.back(true);
                     }
                 });
                 menHistory.add(item);
 //                if (counter++>15) break;
             }
             JMenuItem currentItem = new JMenuItem(mapC.getCurrentElement().toString());
             currentItem.setEnabled(false);
             currentItem.setIcon(current);
             menHistory.add(currentItem);
             counter = 0;
             for (int index = forwPos.size() - 1; index >= 0; --index) {
                 Object elem = forwPos.get(index);
                 JMenuItem item = new JMenuItem(elem.toString());//+":"+new Integer(forwPos.size()-1-index));
 
                 item.setIcon(miniForward);
                 final int pos = forwPos.size() - 1 - index;
                 item.addActionListener(new ActionListener() {
 
                     public void actionPerformed(ActionEvent e) {
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
 
     public void historyActionPerformed() {
         log.fatal("historyActionPerformed");
     }
 
     public void forwardStatusChanged() {
         mniForward.setEnabled(mapC.isForwardPossible());
     }
 
     public void backStatusChanged() {
         mniBack.setEnabled(mapC.isBackPossible());
     }
 
     public void featuresRemoved(FeatureCollectionEvent fce) {
     }
 
     public void featuresChanged(FeatureCollectionEvent fce) {
     }
 
     public void featuresAdded(FeatureCollectionEvent fce) {
     }
 
     public void featureSelectionChanged(FeatureCollectionEvent fce) {
         if (plugin && !featureCollectionEventBlocker) {
             Collection<Feature> fc = new Vector<Feature>(mapC.getFeatureCollection().getSelectedFeatures());
             final Vector<DefaultMutableTreeNode> nodeVector = new Vector<DefaultMutableTreeNode>();
             for (Feature f : fc) {
                 if (f instanceof CidsFeature) {
                     nodeVector.add(featuresInMapReverse.get(f));
                 }
             }
             EventQueue.invokeLater(new Runnable() {
 
                 public void run() {
                     nodeSelectionEventBlocker = true;
                     ComponentRegistry.getRegistry().getActiveCatalogue().setSelectedNodes(nodeVector, true);
                     nodeSelectionEventBlocker = false;
                 }
             });
         }
     }
 
     public void featureReconsiderationRequested(FeatureCollectionEvent fce) {
     }
 
     public void allFeaturesRemoved(FeatureCollectionEvent fce) {
     }
 
     private void initHttpServer() {
         try {
             Thread http = new Thread(new Runnable() {
 
                 public void run() {
                     try {
                         Thread.sleep(1500); //Bugfix Try Deadlock 
 
                         log.debug("Http Interface initialisieren");
                         Server server = new Server();
                         Connector connector = new SelectChannelConnector();
                         connector.setPort(9098);
                         server.setConnectors(new Connector[]{connector});
 
                         Handler param = new AbstractHandler() {
 
                             public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                                 Request base_request = (request instanceof Request) ? (Request) request : HttpConnection.getCurrentConnection().getRequest();
                                 base_request.setHandled(true);
                                 response.setContentType("text/html");
                                 response.setStatus(HttpServletResponse.SC_ACCEPTED);
                                 response.getWriter().println("<html><head><title>HTTP interface</title></head><body><table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"80%\"><tr><td width=\"30%\" align=\"center\" valign=\"middle\"><img border=\"0\" src=\"http://www.cismet.de/images/cismetLogo250M.png\" ><br></td><td width=\"%\">&nbsp;</td><td width=\"50%\" align=\"left\" valign=\"middle\"><font face=\"Arial\" size=\"3\" color=\"#1c449c\">... and <b><font face=\"Arial\" size=\"3\" color=\"#1c449c\">http://</font></b> just works</font><br><br><br></td></tr></table></body></html>");
                             }
                         };
                         Handler hello = new AbstractHandler() {
 
                             public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                                 try {
                                     if (request.getLocalAddr().equals(request.getRemoteAddr())) {
 
                                         log.info("HttpInterface angesprochen");
                                         if (target.equalsIgnoreCase("/gotoBoundingBox")) {
                                             String x1 = request.getParameter("x1");
                                             String y1 = request.getParameter("y1");
                                             String x2 = request.getParameter("x2");
                                             String y2 = request.getParameter("y2");
                                             try {
                                                 BoundingBox bb = new BoundingBox(new Double(x1), new Double(y1), new Double(x2), new Double(y2));
                                                 mapC.gotoBoundingBoxWithHistory(bb);
                                             } catch (Exception e) {
                                                 log.warn("gotoBoundingBox ging schief", e);
                                             }
                                         }
                                         if (target.equalsIgnoreCase("/gotoScale")) {
                                             String x1 = request.getParameter("x1");
                                             String y1 = request.getParameter("y1");
                                             String scaleDenominator = request.getParameter("scaleDenominator");
                                             try {
                                                 BoundingBox bb = new BoundingBox(new Double(x1), new Double(y1), new Double(x1), new Double(y1));
 
                                                 mapC.gotoBoundingBoxWithHistory(mapC.getScaledBoundingBox(new Double(scaleDenominator).doubleValue(), bb));
                                             } catch (Exception e) {
                                                 log.warn("gotoBoundingBox ging schief", e);
                                             }
                                         }
                                         if (target.equalsIgnoreCase("/centerOnPoint")) {
                                             String x1 = request.getParameter("x1");
                                             String y1 = request.getParameter("y1");
                                             try {
                                                 BoundingBox bb = new BoundingBox(new Double(x1), new Double(y1), new Double(x1), new Double(y1));
                                                 mapC.gotoBoundingBoxWithHistory(bb);
                                             } catch (Exception e) {
                                                 log.warn("centerOnPoint ging schief", e);
                                             }
                                         } else {
                                             log.warn("Unbekanntes Target: " + target);
                                         }
                                     } else {
                                         log.warn("Jemand versucht von einem anderen Rechner auf das Http Interface zuzugreifen. Abgelehnt.");
                                     }
                                 } catch (Throwable t) {
                                     log.error("Fehler im Behandeln von HttpRequests", t);
                                 }
                             }
                         };
                         HandlerCollection handlers = new HandlerCollection();
                         handlers.setHandlers(new Handler[]{param, hello});
                         server.setHandler(handlers);
 
                         server.start();
                         server.join();
                     } catch (Throwable t) {
                         log.error("Fehler im HttpInterface von cismap", t);
                     }
                 }
             });
             http.start();
             log.debug("Http Interface initialisieren");
         } catch (Throwable t) {
             log.fatal("GAAR NIX", t);
         }
     }
 
     public void featureCollectionChanged() {
     }
 
     public void update(Observable o, Object arg) {
         if (o.equals(mapC.getMemUndo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdUndo.isEnabled()) {
                 log.debug("UNDO-Button aktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdUndo.setEnabled(true);
                     }
                 });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdUndo.isEnabled()) {
                 log.debug("UNDO-Button deaktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdUndo.setEnabled(false);
                     }
                 });
             }
         } else if (o.equals(mapC.getMemRedo())) {
             if (arg.equals(MementoInterface.ACTIVATE) && !cmdRedo.isEnabled()) {
                 log.debug("REDO-Button aktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdRedo.setEnabled(true);
                     }
                 });
             } else if (arg.equals(MementoInterface.DEACTIVATE) && cmdRedo.isEnabled()) {
                 log.debug("REDO-Button deaktivieren");
                 EventQueue.invokeLater(new Runnable() {
 
                     public void run() {
                         cmdRedo.setEnabled(false);
                     }
                 });
             }
         }
     }
 
     private class NodeChangeListener extends MetaNodeSelectionListener {
 
         private final SingleAttributeIterator attributeIterator;
         private final Collection classNames;
         private final Collection attributeNames;
 
         private NodeChangeListener() {
             this.classNames = context.getEnvironment().getAttributeMappings("className");
             this.attributeNames = context.getEnvironment().getAttributeMappings("attributeName");
             if (this.attributeNames.size() == 0) {
                 this.attributeNames.add("id");
             }
             AttributeRestriction attributeRestriction = new ComplexAttributeRestriction(AttributeRestriction.OBJECT, AttributeRestriction.IGNORE, null, this.attributeNames, null);
             this.attributeIterator = new SingleAttributeIterator(attributeRestriction, false);
         }
 
         protected void nodeSelectionChanged(Collection wirdNichtGebrauchtWeilScheissevonPascalgefuelltCollection) {
             if (!nodeSelectionEventBlocker) {
                 try {
                     Collection c = context.getMetadata().getSelectedNodes();
                     if (c != null && c.size() != 0) {
                         if (featureControl.isWizardMode()) {
                             showObjectsMethod.invoke(c);
                         } else {
                             Object[] nodes = c.toArray();
                             boolean oneHit = false;
                             Vector<Feature> features = new Vector<Feature>();
                             for (Object o : nodes) {
                                 if (o instanceof DefaultMetaTreeNode) {
                                     DefaultMetaTreeNode node = (DefaultMetaTreeNode) o;
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
                                 log.debug("featuresInMap:" + featuresInMap);
                                 //log.debug(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Node_nicht_in_Karte")+":"+node);
                             }
                         }
                     }
                 } catch (Throwable t) {
                     log.error(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Fehler_im_WizardMode"), t);
                 }
             }
         }
     }
 
     private class ShowObjectsMethod implements PluginMethod {
 
         public void invoke() throws Exception {
             Collection selectedNodes = context.getMetadata().getSelectedNodes();
             invoke(selectedNodes);
         }
 
         public void invoke(Collection nodes) throws Exception {
             invoke(nodes, false);
         }
 
         public synchronized CidsFeature invoke(DefaultMetaTreeNode node, ObjectAttribute oAttr, boolean editable) throws Exception {
 //            if (oAttr!=null&&oAttr.getValue()==null&&editable==true) {
 //                return null;
 //            }
             Vector<Feature> v = new Vector<Feature>();
             MetaObject loader = ((ObjectTreeNode) node).getMetaObject();
             MetaObjectNode mon = ((ObjectTreeNode) node).getMetaObjectNode();
             CidsFeature cidsFeature;
             if (oAttr != null) {
                 cidsFeature = new CidsFeature(mon, oAttr);
             } else {
                 cidsFeature = new CidsFeature(mon);
             }
             cidsFeature.setEditable(editable);
             v.add(cidsFeature);
             featuresInMap.put(node, cidsFeature);
             featuresInMapReverse.put(cidsFeature, node);
             log.debug("mapC.getFeatureCollection().getAllFeatures():" + mapC.getFeatureCollection().getAllFeatures());
             log.debug("cidsFeature:" + cidsFeature);
             log.debug("mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature):" + mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature));
             mapC.getFeatureLayer().setVisible(true);
 //            if (mapC.getFeatureCollection().getAllFeatures().contains(cidsFeature)) {
             mapC.getFeatureCollection().removeFeature(cidsFeature);
             log.debug("mapC.getFeatureCollection().getAllFeatures():" + mapC.getFeatureCollection().getAllFeatures());
             //          }
             mapC.getFeatureCollection().substituteFeatures(v);
             if (editable) {
                 //mapC.getFeatureCollection().holdFeature(cidsFeature);
                 mapC.getFeatureCollection().select(v);
             }
             if (!mapC.isFixedMapExtent()) {
                 mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
                 mapC.showHandles(true);
             }
             return cidsFeature;
         }
 
         synchronized public void invoke(final Collection nodes, final boolean editable) throws Exception {
             log.info("invoke zeigt Objekte in der Karte");
             Thread t = new Thread() {
 
                 @Override
                 public void run() {
                     EventQueue.invokeLater(new Runnable() {
 
                         public void run() {
                             showObjectsWaitDialog.setLocationRelativeTo(CismapPlugin.this);
                             showObjectsWaitDialog.setVisible(true);
                         }
                     });
 
                     try {
                         Vector tmpFeaturesInMapRemoveCollection = new Vector();
 
                         for (DefaultMetaTreeNode node : featuresInMap.keySet()) {
                             Feature f = featuresInMap.get(node);
                             if (!mapC.getFeatureCollection().isHoldFeature(f)) {
                                 tmpFeaturesInMapRemoveCollection.add(node);
                                 featuresInMapReverse.remove(f);
                             }
                         }
                         for (Object o : tmpFeaturesInMapRemoveCollection) {
                             featuresInMap.remove(o);
                         }
 
                         Iterator<DefaultMetaTreeNode> it = nodes.iterator();
                         Vector<Feature> v = new Vector<Feature>();
                         while (it.hasNext()) {
                             DefaultMetaTreeNode node = it.next();
                             MetaObject loader = ((ObjectTreeNode) node).getMetaObject();
                             MetaObjectNode mon = ((ObjectTreeNode) node).getMetaObjectNode();
                             CidsFeature cidsFeature = new CidsFeature(mon);
                             cidsFeature.setEditable(editable);
                             //log.fatal("cidsFeature.hashCode():"+cidsFeature.hashCode());
                             //log.fatal("feturesInMap:"+featuresInMap);
 //                            log.fatal("featuresInMap.containsValue(cidsFeature):"+featuresInMap.containsValue(cidsFeature));
                             if (!(featuresInMap.containsValue(cidsFeature))) {
                                 v.add(cidsFeature);
                                 featuresInMap.put(node, cidsFeature);
                                 log.debug("featuresInMap.put(node,cidsFeature):" + node + "," + cidsFeature);
                                 featuresInMapReverse.put(cidsFeature, node);
 //                                log.fatal("feturesInMap:"+featuresInMap);
 //                                log.fatal("featuresInMapReverse:"+featuresInMapReverse);
                             }
                         }
                         mapC.getFeatureLayer().setVisible(true);
                         mapC.getFeatureCollection().substituteFeatures(v);
 
                         if (!mapC.isFixedMapExtent()) {
                             mapC.zoomToFeatureCollection(mapC.isFixedMapScale());
                         }
                     } catch (Exception e) {
                         log.error(java.util.ResourceBundle.getBundle("de/cismet/cismap/navigatorplugin/Bundle").getString("CismapPlugin.log.Fehler_beim_Anzeigen_der_Objekte"), e);
                     } finally {
                         EventQueue.invokeLater(new Runnable() {
 
                             public void run() {
                                 showObjectsWaitDialog.dispose();
                             }
                         });
                     }
                 }
             };
             t.setPriority(Thread.NORM_PRIORITY);
             t.start();
         }
 
         public String getId() {
             return this.getClass().getName();
         }
     }
 
     class MyPluginProperties implements PluginProperties {
 
         public Object getProperty(String propertyName) {
             log.debug("GetProperty von CismapPlugin aufgerufen");
             if (propertyName.equalsIgnoreCase("coordinate")) {
                 //hier erwartet der Navigator ein double[][] mit  allen Punkten
                 double[][] pointCoordinates = new double[4][2];
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
             } else if (propertyName.equalsIgnoreCase("coordinateString")) {
                 return "(" +
                         mapC.getCurrentBoundingBox().getX1() + "," +
                         mapC.getCurrentBoundingBox().getX1() + ") (" +
                         mapC.getCurrentBoundingBox().getX2() + "," +
                         mapC.getCurrentBoundingBox().getX2() + ") (" +
                         mapC.getCurrentBoundingBox().getX2() + "," +
                         mapC.getCurrentBoundingBox().getY2() + ") (" +
                         mapC.getCurrentBoundingBox().getX1() + "," +
                         mapC.getCurrentBoundingBox().getY2() + ")";
 //                mapC.getCurrentBoundingBox().getGeometryFromTextCompatibleString()
             } else if (propertyName.equalsIgnoreCase("ogcFeatureString")) {
                 mapC.getCurrentBoundingBox().getGeometryFromTextCompatibleString();
             }
             return null;
         }
 
         public void setProperty(String propertyName, Object value) {
         }
 
         public void addPropertyChangeListener(PropertyChangeListener listener) {
         }
 
         public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
         }
 
         public void removePropertyChangeListener(PropertyChangeListener listener) {
         }
 
         public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
         }
     }
 }
 
 class GeoLinkUrl implements Transferable {
 
     String url;
 
     public GeoLinkUrl(String url) {
         this.url = url;
     }
 
     public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.stringFlavor};
     }
 
     public boolean isDataFlavorSupported(DataFlavor flavor) {
         return DataFlavor.stringFlavor.equals(flavor);
     }
 
     public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
         if (!DataFlavor.stringFlavor.equals(flavor)) {
             throw new UnsupportedFlavorException(flavor);
         }
         return url;
     }
 }
 
 class ImageSelection implements Transferable {
 
     private Image image;
 
     public ImageSelection(Image image) {
         this.image = image;
     }
 
     // Returns supported flavors
     public DataFlavor[] getTransferDataFlavors() {
         return new DataFlavor[]{DataFlavor.imageFlavor};
     }
 
     // Returns true if flavor is supported
     public boolean isDataFlavorSupported(DataFlavor flavor) {
         return DataFlavor.imageFlavor.equals(flavor);
     }
 
     // Returns image
     public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
         if (!DataFlavor.imageFlavor.equals(flavor)) {
             throw new UnsupportedFlavorException(flavor);
         }
         return image;
     }
 }
