 /*
  * This file is part of DRBD Management Console by Rasto Levrinc,
  * LINBIT HA-Solutions GmbH
  *
  * Copyright (C) 2009, Rastislav Levrinc.
  *
  * DRBD Management Console is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as published
  * by the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * DRBD Management Console is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with drbd; see the file COPYING.  If not, write to
  * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 
 package drbd.gui;
 
 import drbd.AddDrbdConfigDialog;
 import drbd.AddDrbdSplitBrainDialog;
 import drbd.AddHostDialog;
 
 import drbd.utilities.Tools;
 import drbd.utilities.Unit;
 import drbd.utilities.DRBD;
 import drbd.utilities.UpdatableItem;
 import drbd.Exceptions;
 import drbd.gui.dialog.ClusterLogs;
 import drbd.gui.dialog.ServiceLogs;
 import drbd.gui.dialog.ClusterDrbdLogs;
 
 import drbd.data.Host;
 import drbd.data.Cluster;
 import drbd.data.ClusterStatus;
 import drbd.data.CRMXML;
 import drbd.data.DrbdXML;
 import drbd.utilities.NewOutputCallback;
 
 import drbd.utilities.ExecCallback;
 import drbd.utilities.Heartbeat;
 import drbd.utilities.CRM;
 import drbd.data.resources.Resource;
 import drbd.data.resources.Service;
 import drbd.data.resources.CommonBlockDevice;
 import drbd.data.resources.BlockDevice;
 import drbd.data.resources.Network;
 import drbd.data.resources.DrbdResource;
 import drbd.data.ResourceAgent;
 import drbd.data.Subtext;
 import drbd.utilities.MyMenu;
 import drbd.utilities.MyMenuItem;
 
 import drbd.gui.HostBrowser.BlockDevInfo;
 import drbd.gui.HostBrowser.HostInfo;
 
 import java.awt.Color;
 
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JTree;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.Box;
 import javax.swing.DefaultListModel;
 import javax.swing.JRadioButton;
 
 import java.awt.Component;
 import java.awt.BorderLayout;
 import java.awt.geom.Point2D;
 import java.awt.Dimension;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 import EDU.oswego.cs.dl.util.concurrent.Mutex;
 import org.apache.commons.collections.map.MultiKeyMap;
 
 /**
  * This class holds cluster resource data in a tree. It shows panels that allow
  * to edit data of services etc.
  * Every resource has its Info object, that accessible through the tree view.
  *
  * @author Rasto Levrinc
  * @version $Id$
  *
  */
 public class ClusterBrowser extends Browser {
     /**
      * Cluster object that holds data of the cluster. (One Browser belongs to
      * one cluster).
      */
     private final Cluster cluster;
     /** Menu's all hosts node. */
     private DefaultMutableTreeNode allHostsNode;
     /** Menu's all hosts in the cluster node. */
     private DefaultMutableTreeNode clusterHostsNode;
     /** Menu's networks node. */
     private DefaultMutableTreeNode networksNode;
     /** Menu's common block devices node. */
     private DefaultMutableTreeNode commonBlockDevicesNode;
     /** Menu's available heartbeat services node. */
     private DefaultMutableTreeNode availableServicesNode;
     /** Menu's heartbeat services node. */
     private DefaultMutableTreeNode servicesNode;
     /** Menu's drbd node. */
     private DefaultMutableTreeNode drbdNode;
     /** Common file systems on all cluster nodes. */
     private String[] commonFileSystems;
     /** Common mount points on all cluster nodes. */
     private String[] commonMountPoints;
 
     /** name (hb type) + id to service info hash. */
     private final Map<String, Map<String, ServiceInfo>> nameToServiceInfoHash =
                                 new HashMap<String, Map<String, ServiceInfo>>();
     /** drbd resource name string to drbd resource info hash. */
     private final Map<String, DrbdResourceInfo> drbdResHash =
                                 new HashMap<String, DrbdResourceInfo>();
     /** drbd resource device string to drbd resource info hash. */
     private final Map<String, DrbdResourceInfo> drbdDevHash =
                                 new HashMap<String, DrbdResourceInfo>();
     /** Heartbeat id to service info hash. */
     private final Map<String, ServiceInfo> heartbeatIdToServiceInfo =
                                           new HashMap<String, ServiceInfo>();
     /** List of heartbeat ids of all services. */
     private final List<String> heartbeatIdList = new ArrayList<String>();
 
     /** Heartbeat graph. */
     private final HeartbeatGraph heartbeatGraph;
     /** Drbd graph. */
     private final DrbdGraph drbdGraph;
     /** object that holds current heartbeat status. */
     private ClusterStatus clusterStatus;
     /** Object that holds hb ocf data. */
     private CRMXML crmXML;
     /** Object that drbd status and data. */
     private DrbdXML drbdXML;
 
     /** Common block device icon. */
     private static final ImageIcon COMMON_BD_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.CommonBlockDeviceIcon"));
     /** Started service icon. */
     private static final ImageIcon SERVICE_STARTED_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.ServiceStartedIcon"));
     /** Stopped service icon. */
     private static final ImageIcon SERVICE_STOPPED_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.ServiceStoppedIcon"));
     /** Network icon. */
     private static final ImageIcon NETWORK_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.NetworkIcon"));
     /** Available services icon. */
     private static final ImageIcon AVAIL_SERVICES_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.ServiceStoppedIcon"));
     /** Remove icon. */
     private static final ImageIcon REMOVE_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("ClusterBrowser.RemoveIcon"));
     /** Migrate icon. */
     private static final ImageIcon MIGRATE_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("HeartbeatGraph.MigrateIcon"));
     /** Running service icon. */
     private static final ImageIcon SERVICE_RUNNING_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("HeartbeatGraph.ServiceRunningIcon"));
     /** Not running service icon. */
     private static final ImageIcon SERVICE_NOT_RUNNING_ICON =
         Tools.createImageIcon(
                 Tools.getDefault("HeartbeatGraph.ServiceNotRunningIcon"));
     /** Add host icon. */
     private static final ImageIcon HOST_ICON = Tools.createImageIcon(
                                         Tools.getDefault("HostTab.HostIcon"));
     /** Start service icon. */
     private static final ImageIcon START_ICON = SERVICE_RUNNING_ICON;
     /** Stop service icon. */
     private static final ImageIcon STOP_ICON  = SERVICE_NOT_RUNNING_ICON;
 
     /** Whether drbd status was canceled by user. */
     private boolean drbdStatusCanceled = true;
     /** Whether hb status was canceled by user. */
     private boolean clStatusCanceled = true;
     /** Tree menu root. */
     private JTree treeMenu;
     /** Global hb status lock. */
     private final Mutex mClStatusLock = new Mutex();
 
     /** last dc host detected. */
     private Host lastDcHost = null;
 
     /** dc host as reported by crm. */
     private Host realDcHost = null;
 
     /** Panel that holds this browser. */
     private ClusterViewPanel clusterViewPanel = null;
 
     /** Hash that holds all hb classes with descriptions that appear in the
      * pull down menus. */
     private static final Map<String, String> HB_CLASS_MENU =
                                                 new HashMap<String, String>();
 
     /** Whether the hb status is run for the first time. (For the progress
      * indicator. */
     private boolean clStatusFirstTime;
 
 
     /** Width of the label in the info panel. */
     private static final int SERVICE_LABEL_WIDTH =
                     Tools.getDefaultInt("ClusterBrowser.ServiceLabelWidth");
     /** Width of the field in the info panel. */
     private static final int SERVICE_FIELD_WIDTH =
                     Tools.getDefaultInt("ClusterBrowser.ServiceFieldWidth");
 
     /** Color of the most of backgrounds. */
     private static final Color PANEL_BACKGROUND =
                     Tools.getDefaultColor("ViewPanel.Background");
     /** Color of the extra (advanced options) panel background. */
     private static final Color EXTRA_PANEL_BACKGROUND =
                     Tools.getDefaultColor("ViewPanel.Status.Background");
     /** Color of the status bar background. */
     private static final Color STATUS_BACKGROUND =
                     Tools.getDefaultColor("ViewPanel.Status.Background");
 
     /** Name of the drbd resource name parameter. */
     private static final String DRBD_RES_PARAM_NAME     = "name";
     /** Name of the drbd device parameter. */
     private static final String DRBD_RES_PARAM_DEV      = "device";
     /** Name of the boolean type in drbd. */
     private static final String DRBD_RES_BOOL_TYPE_NAME = "boolean";
     /** Name of the drbd after parameter. */
     private static final String DRBD_RES_PARAM_AFTER    = "after";
 
     /** Name of the empty parameter, that is used while passing the parameters
      * to the drbd-gui-helper script. */
     private static final String HB_NONE_ARG             = "..none..";
     /** Name of the group hearbeat service. */
     private static final String PM_GROUP_NAME =
                                         Tools.getConfigData().PM_GROUP_NAME;
         
     /** Name of the clone service. */
     private static final String PM_CLONE_SET_NAME =
                                 Tools.getConfigData().PM_CLONE_SET_NAME;
     /** Name of the master / slave. */
     private static final String PM_MASTER_SLAVE_SET_NAME =
                                 Tools.getConfigData().PM_MASTER_SLAVE_SET_NAME;
     /** Name of ocf style resource (heartbeat 2). */
     private static final String HB_OCF_CLASS            = "ocf";
     /** Name of heartbeat style resource (heartbeat 1). */
     private static final String HB_HEARTBEAT_CLASS      = "heartbeat";
     /** Name of lsb style resource (/etc/init.d/*). */
     private static final String HB_LSB_CLASS            = "lsb";
 
     /** Name of the provider. TODO: other providers? */
     private static final String HB_HEARTBEAT_PROVIDER   = "heartbeat";
 
     /** String array with all hb classes. */
     private static final String[] HB_CLASSES = {HB_OCF_CLASS,
                                                 HB_HEARTBEAT_CLASS,
                                                 HB_LSB_CLASS};
 
     /** Hb start operation. */
     private static final String HB_OP_START = "start";
     /** Hb stop operation. */
     private static final String HB_OP_STOP = "stop";
     /** Hb status operation. */
     private static final String HB_OP_STATUS = "status";
     /** Hb monitor operation. */
     private static final String HB_OP_MONITOR = "monitor";
     /** Hb meta-data operation. */
     private static final String HB_OP_META_DATA = "meta-data";
     /** Hb validate-all operation. */
     private static final String HB_OP_VALIDATE_ALL = "validate-all";
     /** Promote operation. */
     private static final String HB_OP_PROMOTE = "promote";
     /** Demote operation. */
     private static final String HB_OP_DEMOTE = "demote";
 
     /** Hb desc parameter. */
     private static final String HB_PAR_DESC = "description";
     /** Hb interval parameter. */
     private static final String HB_PAR_INTERVAL = "interval";
     /** Hb timeout parameter. */
     private static final String HB_PAR_TIMEOUT = "timeout";
     /** Hb start-delay parameter. */
     private static final String HB_PAR_START_DELAY = "start-delay";
     /** Hb disabled parameter. */
     private static final String HB_PAR_DISABLED = "disabled";
     /** Hb role parameter. */
     private static final String HB_PAR_ROLE = "role";
     /** Hb prereq parameter. */
     private static final String HB_PAR_PREREQ = "prereq";
     /** Hb on-fail parameter. */
     private static final String HB_PAR_ON_FAIL = "on-fail";
 
     /** String array with all hb operations. */
     private static final String[] HB_OPERATIONS = {HB_OP_START,
                                                    HB_OP_PROMOTE,
                                                    HB_OP_DEMOTE,
                                                    HB_OP_STOP,
                                                    HB_OP_STATUS,
                                                    HB_OP_MONITOR,
                                                    HB_OP_META_DATA,
                                                    HB_OP_VALIDATE_ALL};
     /** Which operations are basic and do not go to the advanced section. */
     private static final List<String> HB_OP_BASIC =
                     Arrays.asList(new String[]{HB_OP_START, HB_OP_STOP});
     /** Parameters for the hb operations. */
     private static final Map<String, List<String>> HB_OPERATION_PARAMS =
                                            new HashMap<String, List<String>>();
     /** All parameters for the hb operations, so that it is possible to create
      * arguments for up_rsc_full_ops. */
     private static final String[] HB_OPERATION_PARAM_LIST = {HB_PAR_DESC,
                                                              HB_PAR_INTERVAL,
                                                              HB_PAR_TIMEOUT,
                                                              HB_PAR_START_DELAY,
                                                              HB_PAR_DISABLED,
                                                              HB_PAR_ROLE,
                                                              HB_PAR_PREREQ,
                                                              HB_PAR_ON_FAIL};
 
     /**
      * Prepares a new <code>CusterBrowser</code> object.
      */
     public ClusterBrowser(final Cluster cluster) {
         super();
         this.cluster = cluster;
         heartbeatGraph = new HeartbeatGraph(this);
         drbdGraph = new DrbdGraph(this);
         setTreeTop();
         HB_CLASS_MENU.put(HB_OCF_CLASS,       "heartbeat 2 (ocf)");
         HB_CLASS_MENU.put(HB_HEARTBEAT_CLASS, "heartbeat 1 (hb)");
         HB_CLASS_MENU.put(HB_LSB_CLASS,       "lsb (init.d)");
 
     }
 
     private void initOperations() {
         HB_OPERATION_PARAMS.put(HB_OP_START,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
         HB_OPERATION_PARAMS.put(HB_OP_STOP,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
         HB_OPERATION_PARAMS.put(HB_OP_META_DATA,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
         HB_OPERATION_PARAMS.put(HB_OP_VALIDATE_ALL,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
 
         HB_OPERATION_PARAMS.put(HB_OP_STATUS,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
 
         // TODO: need two monitors for role='Slave' and 'Master' in
         // master/slave resources
         final String hbV = getDCHost().getHeartbeatVersion();
         if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
             HB_OPERATION_PARAMS.put(HB_OP_MONITOR,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
         } else {
             HB_OPERATION_PARAMS.put(HB_OP_MONITOR,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL,
                                                           HB_PAR_START_DELAY)));
         }
         HB_OPERATION_PARAMS.put(HB_OP_PROMOTE,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
         HB_OPERATION_PARAMS.put(HB_OP_DEMOTE,
                                 new ArrayList<String>(
                                             Arrays.asList(HB_PAR_TIMEOUT,
                                                           HB_PAR_INTERVAL)));
     }
 
     /**
      * Sets the cluster view panel.
      */
     public final void setClusterViewPanel(
                                     final ClusterViewPanel clusterViewPanel) {
         this.clusterViewPanel = clusterViewPanel;
     }
 
     /**
      * Returns cluster view panel.
      */
     public final ClusterViewPanel getClusterViewPanel() {
         return clusterViewPanel;
     }
 
     /**
      * Returns all nodes that belong to this cluster.
      */
     public final Host[] getClusterHosts() {
         return cluster.getHostsArray();
     }
 
     /**
      * Returns cluster data object.
      */
     public final Cluster getCluster() {
         return cluster;
     }
 
     /**
      * Sets the info panel component in the cluster view panel.
      */
     public final void setRightComponentInView(final Info i) {
         clusterViewPanel.setRightComponentInView(this, i);
     }
 
     /**
      * Saves positions of service and block devices from the heartbeat and drbd
      * graphs to the config files on every node.
      */
     public final void saveGraphPositions() {
         final Map<String, Point2D> positions = new HashMap<String, Point2D>();
         if (drbdGraph != null) {
             drbdGraph.getPositions(positions);
         }
         if (heartbeatGraph != null) {
             heartbeatGraph.getPositions(positions);
         }
 
         final Host[] hosts = getClusterHosts();
         for (Host host : hosts) {
             host.saveGraphPositions(positions);
         }
     }
 
     /**
      * Returns heartbeat graph for this cluster.
      */
     public final ResourceGraph getHeartbeatGraph() {
         return heartbeatGraph;
     }
 
     /**
      * Returns drbd graph for this cluster.
      */
     public final ResourceGraph getDrbdGraph() {
         return drbdGraph;
     }
 
     /**
      * Returns all hosts are don't get cluster status.
      */
     public final boolean allHostsDown() {
        boolean hostsDown = true;
        final Host[] hosts = cluster.getHostsArray();
        for (Host host : hosts) {
            if (host.isClStatus()) {
                hostsDown = false;
                break;
            }
        }
        return hostsDown;
     }
 
     /**
      * Returns whether there is at least one drbddisk resource.
      */
     public final boolean isOneDrbddisk() {
         for (final String id : heartbeatIdToServiceInfo.keySet()) {
             final ServiceInfo si = heartbeatIdToServiceInfo.get(id);
             if (si.getResourceAgent().isDrbddisk()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Returns whether there is at least one drbddisk resource.
      */
     public final boolean isOneLinbitDrbd() {
         for (final String id : heartbeatIdToServiceInfo.keySet()) {
             final ServiceInfo si = heartbeatIdToServiceInfo.get(id);
             if (si.getResourceAgent().isLinbitDrbd()) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Initializes cluster resources for cluster view.
      */
     public final void initClusterResources() {
 
         /* all hosts */
         allHostsNode = new DefaultMutableTreeNode(new AllHostsInfo());
         setNode(allHostsNode);
         topAdd(allHostsNode);
         /* hosts */
         clusterHostsNode = new DefaultMutableTreeNode(
             new CategoryInfo(Tools.getString("ClusterBrowser.ClusterHosts")));
         setNode(clusterHostsNode);
         topAdd(clusterHostsNode);
 
         /* networks */
         networksNode = new DefaultMutableTreeNode(
             new CategoryInfo(Tools.getString("ClusterBrowser.Networks")));
         setNode(networksNode);
         topAdd(networksNode);
 
         /* drbd */
         drbdNode = new DefaultMutableTreeNode(
             new DrbdInfo(Tools.getString("ClusterBrowser.Drbd")));
         setNode(drbdNode);
         topAdd(drbdNode);
 
         /* heartbeat */
         final HeartbeatInfo heartbeatInfo =
             new HeartbeatInfo(
                     Tools.getString("ClusterBrowser.Heartbeat"));
         final DefaultMutableTreeNode heartbeatNode =
                             new DefaultMutableTreeNode(heartbeatInfo);
         setNode(heartbeatNode);
         topAdd(heartbeatNode);
 
         /* available services */
         availableServicesNode = new DefaultMutableTreeNode(
             new HbCategoryInfo(
                 Tools.getString("ClusterBrowser.availableServices")));
         setNode(availableServicesNode);
         heartbeatNode.add(availableServicesNode);
 
         /* block devices / shared disks, TODO: */
         commonBlockDevicesNode = new DefaultMutableTreeNode(
             new HbCategoryInfo(
                 Tools.getString("ClusterBrowser.CommonBlockDevices")));
         setNode(commonBlockDevicesNode);
         /* heartbeatNode.add(commonBlockDevicesNode); */
 
         /* services */
         final ServicesInfo servicesInfo =
                 new ServicesInfo(Tools.getString("ClusterBrowser.Services"));
         servicesNode = new DefaultMutableTreeNode(servicesInfo);
         setNode(servicesNode);
         heartbeatNode.add(servicesNode);
     }
 
     /**
      * Updates resources of a cluster in the tree.
      *
      * @param clusterHosts
      *          hosts in this cluster
      * @param commonFileSystems
      *          filesystems that are common on both hosts
      * @param commonMountPoints
      *          mount points that are common on both hosts
      */
     public final void updateClusterResources(final JTree treeMenu,
                                              final Host[] clusterHosts,
                                              final String[] commonFileSystems,
                                              final String[] commonMountPoints) {
         this.treeMenu = treeMenu;
         this.commonFileSystems = commonFileSystems;
         this.commonMountPoints = commonMountPoints;
         DefaultMutableTreeNode resource;
 
         /* all hosts */
         final Host[] allHosts =
                                Tools.getConfigData().getHosts().getHostsArray();
         allHostsNode.removeAllChildren();
         for (Host host : allHosts) {
             final HostBrowser hostBrowser = host.getBrowser();
             resource = new DefaultMutableTreeNode(hostBrowser.getHostInfo());
             setNode(resource);
             allHostsNode.add(resource);
         }
         reload(allHostsNode);
 
         /* cluster hosts */
         clusterHostsNode.removeAllChildren();
         for (Host clusterHost : clusterHosts) {
             final HostBrowser hostBrowser = clusterHost.getBrowser();
             resource = new DefaultMutableTreeNode(hostBrowser.getHostInfo());
             setNode(resource);
             clusterHostsNode.add(resource);
             //drbdGraph.addHost(hostBrowser.getHostInfo());
             heartbeatGraph.addHost(hostBrowser.getHostInfo());
         }
 
         reload(clusterHostsNode);
 
         /* block devices */
         updateCommonBlockDevices();
 
         /* networks */
         updateNetworks();
 
         updateHeartbeatDrbdThread();
     }
 
     /**
      * Starts everything.
      */
     private void updateHeartbeatDrbdThread() {
         final Runnable runnable = new Runnable() {
             public void run() {
                 Host firstHost = null;
                 final Host[] hosts = cluster.getHostsArray();
                 for (final Host host : hosts) {
                     final HostBrowser hostBrowser = host.getBrowser();
                     drbdGraph.addHost(hostBrowser.getHostInfo());
                 }
                 do { /* wait here until a host is connected. */
                     boolean notConnected = true;
                     for (final Host host : hosts) {
                         // TODO: fix that, use latches or callback
                         if (host.isConnected()) {
                             /* at least one connected. */
                             notConnected = false;
                             break;
                         } else {
                             //jfinal HostBrowser hostBrowser = host.getBrowser();
                             //jdrbdGraph.addHost(hostBrowser.getHostInfo());
                         }
                     }
                     if (!notConnected) {
                         firstHost = getFirstHost();
                     }
                     if (firstHost == null) {
                         try {
                             Thread.sleep(2000);
                         } catch (InterruptedException ex) {
                             Thread.currentThread().interrupt();
                         }
                         cluster.connect(null);
                     }
                 } while (firstHost == null);
 
                 crmXML = new CRMXML(firstHost);
                 clusterStatus = new ClusterStatus(firstHost, crmXML);
                 initOperations();
                 drbdXML = new DrbdXML(cluster.getHostsArray());
                 /* available services */
                 final String clusterName = getCluster().getName();
                 Tools.startProgressIndicator(clusterName,
                         Tools.getString("ClusterBrowser.HbUpdateResources"));
 
                 updateAvailableServices();
                 Tools.stopProgressIndicator(clusterName,
                     Tools.getString("ClusterBrowser.HbUpdateResources"));
                 Tools.startProgressIndicator(clusterName,
                     Tools.getString("ClusterBrowser.DrbdUpdate"));
 
                 updateDrbdResources();
                 //SwingUtilities.invokeLater(new Runnable() {
                 //    public void run() {
                 //        drbdGraph.scale();
                 //    }
                 //});
                 //try { Thread.sleep(10000); }
                 //catch (InterruptedException ex) {}
                 drbdGraph.getDrbdInfo().getInfoPanel();
                 drbdGraph.getDrbdInfo().selectMyself();
                 Tools.stopProgressIndicator(clusterName,
                     Tools.getString("ClusterBrowser.DrbdUpdate"));
                 cluster.getBrowser().startServerStatus();
                 cluster.getBrowser().startDrbdStatus();
                 cluster.getBrowser().startClStatus();
             }
         };
         final Thread thread = new Thread(runnable);
         //thread.setPriority(Thread.MIN_PRIORITY);
         thread.start();
     }
 
     /**
      * Starts polling of the server status on all hosts, for all the stuff that can
      * change on the server on the fly, like for example the block devices.
      */
     public final void startServerStatus() {
         final Host[] hosts = cluster.getHostsArray();
         for (final Host host : hosts) {
             final Thread thread = new Thread(new Runnable() {
                 public void run() {
                     startServerStatus(host);
                 }
             });
             thread.start();
         }
     }
 
     /**
      * Starts polling of the server status on one host.
      */
     public final void startServerStatus(final Host host) {
         final String hostName = host.getName();
         while (true) {
             host.setIsLoading();
             host.getHWInfo();
             drbdGraph.addHost(host.getBrowser().getHostInfo());
             updateDrbdResources();
             SwingUtilities.invokeLater(
                 new Runnable() {
                     public void run() {
                          drbdGraph.scale();
                     }
                 });
             try {
                 Thread.sleep(10000);
             } catch (InterruptedException ex) {
                 Thread.currentThread().interrupt();
             }
         }
     }
 
     /**
      * Starts drbd status on all hosts.
      */
     public final void startDrbdStatus() {
         final Host[] hosts = cluster.getHostsArray();
         for (final Host host : hosts) {
             final Thread thread = new Thread(new Runnable() {
                 public void run() {
                     startDrbdStatus(host);
                 }
             });
             thread.start();
         }
     }
 
     /**
      * Starts drbd status.
      */
     public final void stopDrbdStatus() {
         drbdStatusCanceled = true;
         final Host[] hosts = cluster.getHostsArray();
         for (Host host : hosts) {
             host.stopDrbdStatus();
         }
         for (Host host : hosts) {
             host.waitOnDrbdStatus();
         }
     }
 
     /**
      * Starts drbd status on host.
      */
     public final void startDrbdStatus(final Host host) {
         boolean firstTime = true; // TODO: can use a latch for this shit too.
         host.setDrbdStatus(false);
         final String hostName = host.getName();
         while (true) {
             final boolean ft = firstTime;
             drbdStatusCanceled = false;
             host.execDrbdStatusCommand(
                   new ExecCallback() {
                        public void done(final String ans) {
                            host.setDrbdStatus(true);
                            drbdGraph.repaint();
                        }
 
                        public void doneError(final String ans,
                                              final int exitCode) {
                            Tools.debug(this, "drbd status failed: "
                                              + host.getName()
                                              + "exit code: "
                                              + exitCode,
                                        2);
                            if (exitCode != 143) {
                                /* was killed intentionally */
                                host.setDrbdStatus(false);
                                drbdGraph.repaint();
                                if (exitCode == 255) {
                                    /* looks like connection was lost */
                                    host.getSSH().forceReconnect();
                                }
                            }
                            //TODO: repaint ok?
                            //repaintSplitPane();
                            //drbdGraph.updatePopupMenus();
                            //drbdGraph.repaint();
                        }
                    },
 
                    new NewOutputCallback() {
                        public void output(final String output) {
                            if (output.indexOf(
                                     "No response from the DRBD driver") >= 0) {
                                host.setDrbdStatus(false);
                                drbdGraph.repaint();
                                return;
                            } else {
                                host.setDrbdStatus(true);
                            }
                            if (ft) {
                                Tools.startProgressIndicator(
                                                 hostName,
                                                 ": updating drbd status...");
                            }
                            final String[] lines = output.split("\n");
                            drbdXML.update(host);
                            host.setDrbdStatus(true);
                            drbdGraph.repaint();
                            for (int i = 0; i < lines.length; i++) {
                                parseDrbdEvent(host.getName(), lines[i]);
                            }
 
                            final Thread thread = new Thread(
                                new Runnable() {
                                    public void run() {
                                        repaintSplitPane();
                                        drbdGraph.updatePopupMenus();
                                        SwingUtilities.invokeLater(
                                            new Runnable() {
                                                public void run() {
                                                    repaintTree();
                                                }
                                            }
                                        );
                                    }
                                });
                            thread.start();
                            if (ft) {
                                Tools.stopProgressIndicator(
                                                 hostName,
                                                 ": updating drbd status...");
                            }
                        }
                    });
             if (!host.isDrbdStatus()) {
                 try {
                     Thread.sleep(2000);
                 } catch (InterruptedException ex) {
                     Thread.currentThread().interrupt();
                 }
             }
             host.waitOnDrbdStatus();
             firstTime = false;
             if (drbdStatusCanceled) {
                 firstTime = true;
                 break;
             }
         }
     }
 
     /**
      * Stops hb status.
      */
     public final void stopClStatus() {
         clStatusCanceled = true;
         final Host[] hosts = cluster.getHostsArray();
         for (Host host : hosts) {
             host.stopClStatus();
         }
     }
 
 
     /**
      * Returns true if hb status on all hosts failed.
      */
     public final boolean clStatusFailed() {
         final Host[] hosts = cluster.getHostsArray();
         for (Host host : hosts) {
             if (host.isClStatus()) {
                 return false;
             }
         }
         return true;
     }
 
     /**
      * Sets hb status (failed / not failed for every node).
      */
     public final void setClStatus() {
         final Host[] hosts = cluster.getHostsArray();
         for (Host host : hosts) {
             host.setClStatus(clusterStatus.isActiveNode(host.getName()));
         }
     }
 
     /**
      * Starts hb status progress indicator.
      */
     public final void startClStatusProgressIndicator(final String hostName) {
         // TODO; clStatusFirstTime closure?
         Tools.startProgressIndicator(
                             hostName,
                             Tools.getString("ClusterBrowser.HbUpdateStatus"));
     }
 
     /**
      * Stops hb status progress indicator.
      */
     public final void stopClStatusProgressIndicator(final String hostName) {
         Tools.stopProgressIndicator(
                             hostName,
                             Tools.getString("ClusterBrowser.HbUpdateStatus"));
     }
 
     /**
      * Starts hb status.
      */
     public final void startClStatus() {
         clStatusFirstTime = true;
         while (true) {
             final Host host = getDCHost();
             if (host == null) {
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException ex) {
                     Thread.currentThread().interrupt();
                 }
                 continue;
             }
             final String hostName = host.getName();
             if (clStatusFirstTime) {
                 startClStatusProgressIndicator(hostName);
             }
             clStatusCanceled = false;
             host.execClStatusCommand(
                  new ExecCallback() {
                      public void done(final String ans) {
                          if (clStatusFirstTime) {
                              clStatusFirstTime = false;
                              selectServices();
                              SwingUtilities.invokeLater(new Runnable() {
                                  public void run() {
                                     heartbeatGraph.scale();
                                 }
                              });
                              stopClStatusProgressIndicator(hostName);
                          }
                      }
 
                      public void doneError(final String ans, final int exitCode) {
                          Tools.progressIndicatorFailed(hostName,
                                     "Heartbeat status failed");
                          if (clStatusFirstTime) {
                              Tools.debug(this, "hb status failed: "
                                                + host.getName()
                                                + ", ec: "
                                                + exitCode, 2);
                          }
                          clStatusLock();
                          final boolean prevClStatusFailed = clStatusFailed();
                          host.setClStatus(false);
                          clusterStatus.setDC(null);
                          clStatusUnlock();
                          if (prevClStatusFailed != clStatusFailed()) {
                              heartbeatGraph.getServicesInfo().selectMyself();
                          }
                          if (exitCode == 255) {
                              /* looks like connection was lost */
                              heartbeatGraph.repaint();
                              host.getSSH().forceReconnect();
                          }
                          done(ans);
                      }
                  },
 
                  new NewOutputCallback() {
                      //TODO: check this buffer's size
                      private StringBuffer clusterStatusOutput =
                                                         new StringBuffer(300);
                      public void output(final String output) {
                          clStatusLock();
                          final boolean prevClStatusFailed = clStatusFailed();
                          if (clStatusCanceled) {
                              clStatusUnlock();
                              if (clStatusFirstTime) {
                                  clStatusFirstTime = false;
                                  selectServices();
                                  SwingUtilities.invokeLater(new Runnable() {
                                      public void run() {
                                          heartbeatGraph.scale();
                                      }
                                  });
                                  stopClStatusProgressIndicator(hostName);
                              }
                              return;
                          }
                          if (output == null) {
                              host.setClStatus(false);
                              if (prevClStatusFailed != clStatusFailed()) {
                                  heartbeatGraph.getServicesInfo().selectMyself();
                              }
                          } else {
                              clusterStatusOutput.append(output);
                              if (clusterStatusOutput.length() > 12) {
                                  //host.setClStatus(true);
                                  final String e = clusterStatusOutput.substring(
                                             clusterStatusOutput.length() - 12);
                                  if (e.trim().equals("---done---")) {
                                      final int i = clusterStatusOutput.lastIndexOf("---start---");
                                      if (i >= 0) {
                                          if (clusterStatusOutput.indexOf("is stopped") >= 0) {
                                              /* TODO: heartbeat's not running. */
                                          } else {
                                              final String status = clusterStatusOutput.substring(i);
                                              clusterStatusOutput.delete(
                                               0,
                                               clusterStatusOutput.length() - 1);
                                              if ("---start---\r\nerror\r\n\r\n---done---\r\n".equals(status)) {
                                                  final boolean oldStatus =
                                                               host.isClStatus();
                                                  host.setClStatus(false);
                                                  if (oldStatus) {
                                                     heartbeatGraph.repaint();
                                                  }
                                              } else {
                                                  clusterStatus.parseStatus(status);
                                                  // TODO; servicesInfo can be null
                                                  heartbeatGraph.getServicesInfo().setGlobalConfig();
                                                  heartbeatGraph.getServicesInfo().setAllResources();
                                                  repaintTree();
                                              }
                                          }
                                      }
                                  }
                                  setClStatus();
                              } else {
                                 final boolean oldStatus =
                                              host.isClStatus();
                                 host.setClStatus(false);
                                 if (oldStatus) {
                                    heartbeatGraph.repaint();
                                 }
                              }
                          }
                         if (prevClStatusFailed != clStatusFailed()) {
                              heartbeatGraph.getServicesInfo().selectMyself();
                         }
                          if (clStatusFirstTime) {
                              clStatusFirstTime = false;
                              selectServices();
                              SwingUtilities.invokeLater(new Runnable() {
                                  public void run() {
                                      heartbeatGraph.scale();
                                  }
                              });
                              stopClStatusProgressIndicator(hostName);
                          }
                          clStatusUnlock();
                      }
                  });
             host.waitOnClStatus();
             if (clStatusCanceled) {
                 clStatusFirstTime = true;
                 break;
             }
             final boolean hbSt = clStatusFailed();
             if (hbSt) {
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException ex) {
                     Thread.currentThread().interrupt();
                 }
             }
         }
     }
 
     /**
      * Returns 'add service' list for menus.
      */
     public final List<ResourceAgent> globalGetAddServiceList(
                                                             final String cl) {
         return crmXML.getServices(cl);
     }
 
     /**
      * Updates common block devices.
      */
     private void updateCommonBlockDevices() {
         if (commonBlockDevicesNode != null) {
             DefaultMutableTreeNode resource;
             final List<String> bd = cluster.getCommonBlockDevices();
             final Enumeration e = commonBlockDevicesNode.children();
             final List<DefaultMutableTreeNode> nodesToRemove =
                                         new ArrayList<DefaultMutableTreeNode>();
             while (e.hasMoreElements()) {
                 final DefaultMutableTreeNode node =
                                     (DefaultMutableTreeNode) e.nextElement();
                 final Info cbdi = (Info) node.getUserObject();
                 if (bd.contains(cbdi.getName())) {
                     /* keeping */
                     bd.remove(bd.indexOf(cbdi.getName()));
                 } else {
                     /* remove not existing block devices */
                     nodesToRemove.add(node);
                 }
             }
 
             /* remove nodes */
             for (DefaultMutableTreeNode node : nodesToRemove) {
                 node.removeFromParent();
             }
 
             /* block devices */
             for (String device : bd) {
                 /* add new block devices */
                 resource = new DefaultMutableTreeNode(
                      new CommonBlockDevInfo(
                                           device,
                                           cluster.getHostBlockDevices(device)));
                 setNode(resource);
                 commonBlockDevicesNode.add(resource);
             }
             reload(commonBlockDevicesNode);
         }
     }
 
     /**
      * Updates available services.
      */
     private void updateAvailableServices() {
         DefaultMutableTreeNode resource;
         availableServicesNode.removeAllChildren();
         for (final String cl : HB_CLASSES) {
             final DefaultMutableTreeNode classNode =
                     new DefaultMutableTreeNode(
                         new HbCategoryInfo(cl.toUpperCase()));
             for (final ResourceAgent ra : crmXML.getServices(cl)) {
                 resource = new DefaultMutableTreeNode(
                                     new AvailableServiceInfo(ra));
                 setNode(resource);
                 classNode.add(resource);
             }
             availableServicesNode.add(classNode);
         }
         //reload(availableServicesNode);
     }
 
     /**
      * Updates drbd resources.
      */
     private void updateDrbdResources() {
         final String[] drbdResources = drbdXML.getResources();
         for (int i = 0; i < drbdResources.length; i++) {
             final String resName = drbdResources[i];
             final String drbdDev = drbdXML.getDrbdDevice(resName);
             final Map<String, String> hostDiskMap =
                                                 drbdXML.getHostDiskMap(resName);
             BlockDevInfo bd1 = null;
             BlockDevInfo bd2 = null;
             for (String hostName : hostDiskMap.keySet()) {
                 if (!cluster.contains(hostName)) {
                     continue;
                 }
                 final String disk = hostDiskMap.get(hostName);
                 final BlockDevInfo bdi = drbdGraph.findBlockDevInfo(hostName,
                                                                     disk);
                 if (bdi == null) {
                     if (drbdDevHash.containsKey(disk)) {
                         /* TODO: ignoring stacked device */
                         continue;
                     } else {
                         Tools.appWarning("could not find disk: " + disk
                                          + " on host: " + hostName);
                         continue;
                     }
                 }
                 if (bdi.getBlockDevice().isDrbd()) {
                     continue;
                 }
                 bdi.getBlockDevice().setValue(
                                       "DrbdNetInterfacePort",
                                       drbdXML.getVirtualInterfacePort(hostName,
                                                                       resName));
                 bdi.getBlockDevice().setValue(
                                           "DrbdNetInterface",
                                           drbdXML.getVirtualInterface(hostName,
                                                                       resName));
                 final String drbdMetaDisk = drbdXML.getMetaDisk(hostName,
                                                                 resName);
                 bdi.getBlockDevice().setValue("DrbdMetaDisk", drbdMetaDisk);
                 bdi.getBlockDevice().setValue(
                                             "DrbdMetaDiskIndex",
                                             drbdXML.getMetaDiskIndex(hostName,
                                                                      resName));
                 if (drbdMetaDisk != null && !drbdMetaDisk.equals("internal")) {
                     final BlockDevInfo mdI =
                                       drbdGraph.findBlockDevInfo(hostName,
                                                                  drbdMetaDisk);
                     mdI.getBlockDevice().setIsDrbdMetaDisk(true);
                     bdi.getBlockDevice().setMetaDisk(mdI.getBlockDevice());
                 }
                 if (bd1 == null) {
                     bd1 = bdi;
                 } else {
                     bd2 = bdi;
                 }
             }
             if (bd1 != null
                 && bd2 != null
                 && !bd1.getBlockDevice().isDrbd()
                 && !bd2.getBlockDevice().isDrbd()) {
                 drbdGraph.getDrbdInfo().addDrbdResource(resName,
                                                         drbdDev,
                                                         bd1,
                                                         bd2,
                                                         false);
             }
         }
     }
 
     /**
      * Updates networks.
      */
     private void updateNetworks() {
         if (networksNode != null) {
             DefaultMutableTreeNode resource;
             final Network[] networks = cluster.getCommonNetworks();
             networksNode.removeAllChildren();
             for (int i = 0; i < networks.length; i++) {
                 resource = new DefaultMutableTreeNode(
                                     new NetworkInfo(networks[i].getName(),
                                                     networks[i]));
                 setNode(resource);
                 networksNode.add(resource);
             }
             reload(networksNode);
         }
     }
 
     /**
      * Returns first host. Used for heartbeat commands, that can be
      * executed on any host.
      * It changes terminal panel to this host.
      */
     public final Host getFirstHost() {
         /* TODO: if none of the hosts is connected the null causes error during
          * loading. */
         Host[] hosts = getClusterHosts();
         for (Host host : hosts) {
             if (host.isConnected()) {
                 return host;
             }
         }
         //if (hosts != null && hosts.length > 0) {
         //    return hosts[0];
         //}
         //Tools.appError("Could not find any hosts");
         return null;
     }
 
     /**
      * Returns whether the host is the real dc host as reported by dc.
      */
     public final boolean isRealDcHost(final Host host) {
         return host.equals(realDcHost);
     }
 
     /**
      * Finds and returns DC host.
      * TODO: document what's going on.
      */
     public final Host getDCHost() {
         Host dcHost = null;
         final String dc = clusterStatus.getDC();
         final List<Host> hosts = new ArrayList<Host>();
         int lastHostIndex = -1;
         int i = 0;
         for (Host host : getClusterHosts()) {
             if (host.getName().equals(dc) && host.isClStatus()) {
                 dcHost = host;
                 break;
             }
             hosts.add(host);
 
             if (host == lastDcHost) {
                 lastHostIndex = i;
             }
             i++;
         }
         if (dcHost == null) {
             int ix = lastHostIndex;
             do {
                 ix++;
                 if (ix > hosts.size() - 1) {
                     ix = 0;
                 }
                 if (hosts.get(ix).isConnected()) {
                     lastDcHost = hosts.get(ix);
                     break;
                 }
             } while (ix != lastHostIndex);
             dcHost = lastDcHost;
             realDcHost = null;
         } else {
             realDcHost = dcHost;
         }
 
         lastDcHost = dcHost;
         return dcHost;
     }
 
     /**
      * clStatusLock global lock.
      */
     public final void clStatusLock() {
         try {
             mClStatusLock.acquire();
         } catch (InterruptedException e) {
             Thread.currentThread().interrupt();
         }
     }
 
     /**
      * clStatusLock global unlock.
      */
     public final void clStatusUnlock() {
         mClStatusLock.release();
     }
 
     /**
      * Parses drbd event from host.
      */
     public final void parseDrbdEvent(final String hostName,
                                      final String output) {
         if (drbdXML == null) {
             return;
         }
         drbdXML.parseDrbdEvent(hostName, drbdGraph, output);
     }
 
     /**
      * Highlights drbd node.
      */
     public final void selectDrbd() {
         reload(drbdNode);
     }
 
     /**
      * Highlights services.
      */
     public final void selectServices() {
         // this fires treeStructureChanged in ViewPanel.
         reload(servicesNode);
     }
 
     /**
      * Returns ServiceInfo object identified by name and id.
      */
     protected final ServiceInfo getServiceInfoFromId(final String name,
                                                      final String id) {
         final Map<String, ServiceInfo> idToInfoHash =
                                                 nameToServiceInfoHash.get(name);
         if (idToInfoHash == null) {
             return null;
         }
         return idToInfoHash.get(id);
     }
 
     /**
      * Removes ServiceInfo from the ServiceInfo hash.
      *
      * @param serviceInfo
      *              service info object
      */
     protected final void removeFromServiceInfoHash(
                                                 final ServiceInfo serviceInfo) {
         final Service service = serviceInfo.getService();
         final Map<String, ServiceInfo> idToInfoHash =
                         nameToServiceInfoHash.get(service.getName());
         if (idToInfoHash != null) {
             idToInfoHash.remove(service.getId());
         }
     }
 
     /**
      * Adds heartbeat id from service to the list. If service does not have an
      * id it is generated.
      *
      * @param si
      *          service info object
      */
     private void addToHeartbeatIdList(final ServiceInfo si) {
         String id = si.getService().getHeartbeatId();
         if (id == null) {
             if (PM_GROUP_NAME.equals(si.getName())) {
                 id = Service.GRP_ID_PREFIX;
             } else if (PM_CLONE_SET_NAME.equals(si.getName())
                        || PM_MASTER_SLAVE_SET_NAME.equals(si.getName())) {
                 if (si.getService().isMaster()) {
                     id = Service.MS_ID_PREFIX;
                 } else {
                     id = Service.CL_ID_PREFIX;
                 }
             } else {
                 id = Service.RES_ID_PREFIX + si.getName() + "_";
             }
             int i = 1;
             while (heartbeatIdList.contains(id + Integer.toString(i))) {
                 i++;
             }
             final String newId = id + Integer.toString(i);
             heartbeatIdList.add(newId);
             si.getService().setHeartbeatId(newId);
             heartbeatIdToServiceInfo.put(newId, si);
         } else {
             if (!heartbeatIdList.contains(id)) {
                 heartbeatIdList.add(id);
             }
             if (heartbeatIdToServiceInfo.get(id) == null) {
                 heartbeatIdToServiceInfo.put(id, si);
             }
         }
     }
 
     /**
      * Deletes caches of all Filesystem infoPanels.
      * This is usefull if something have changed.
      */
     public final void resetFilesystems() {
         for (String hbId : heartbeatIdToServiceInfo.keySet()) {
             final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
             if (si.getName().equals("Filesystem")) {
                 si.setInfoPanel(null);
             }
         }
     }
 
     /**
      * Adds ServiceInfo in the name to ServiceInfo hash. Id and name
      * are taken from serviceInfo object. nameToServiceInfoHash
      * contains a hash with id as a key and ServiceInfo as a value.
      *
      * @param serviceInfo
      *              service info object
      */
     protected final void addNameToServiceInfoHash(
                                                 final ServiceInfo serviceInfo) {
         /* add to the hash with service name and id as
          * keys */
         final Service service = serviceInfo.getService();
         Map<String, ServiceInfo> idToInfoHash =
                                 nameToServiceInfoHash.get(service.getName());
         String csPmId = null;
         if (serviceInfo.getContainedService() != null) {
             csPmId =
                serviceInfo.getContainedService().getService().getHeartbeatId();
         }
         if (idToInfoHash == null) {
             idToInfoHash = new HashMap<String, ServiceInfo>();
             if (service.getId() == null) {
                 if (csPmId == null) {
                     service.setId("1");
                 } else {
                     service.setId(csPmId);
                 }
             }
         } else {
             if (service.getId() == null) {
                 final Iterator it = idToInfoHash.keySet().iterator();
                 int index = 0;
                 while (it.hasNext()) {
                     final String id =
                       idToInfoHash.get((String) it.next()).getService().getId();
                     Pattern p;
                     if (csPmId == null) {
                         p = Pattern.compile("^(\\d+)$");
                     } else {
                         p = Pattern.compile("^" + csPmId + "_(\\d+)$");
                         if (csPmId.equals(id)) {
                             index++;
                         }
                     }
 
                     final Matcher m = p.matcher(id);
                     if (m.matches()) {
                         try {
                             final int i = Integer.parseInt(m.group(1));
                             if (i > index) {
                                 index = i;
                             }
                         } catch (NumberFormatException nfe) {
                             /* not a number */
                         }
                     }
                 }
                 if (csPmId == null) {
                     service.setId(Integer.toString(index + 1));
                 } else {
                     if (index == 0) {
                         service.setId(csPmId);
                     } else {
                         service.setId(csPmId
                                       + "_"
                                       + Integer.toString(index + 1));
                     }
                 }
             }
         }
         idToInfoHash.remove(service.getId());
         idToInfoHash.put(service.getId(), serviceInfo);
         nameToServiceInfoHash.remove(service.getName());
         nameToServiceInfoHash.put(service.getName(), idToInfoHash);
     }
 
     /**
      * Returns true if user wants the linbit:drbd even, for old version of
      * hb or simply true if we have pacemaker.
      */
     private boolean linbitDrbdConfirmDialog() {
         // TODO: warn about drbd < 8.3.3 as well
         final String hbV = getDCHost().getHeartbeatVersion();
         if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
             String desc = Tools.getString(
                         "ClusterBrowser.confirmLinbitDrbd.Description");
 
             return Tools.confirmDialog(
                Tools.getString("ClusterBrowser.confirmLinbitDrbd.Title"),
                desc.replaceAll("@VERSION@", hbV),
                Tools.getString("ClusterBrowser.confirmLinbitDrbd.Yes"),
                Tools.getString("ClusterBrowser.confirmLinbitDrbd.No"));
         }
         return true;
     }
 
     /**
      * Starts heartbeats on all nodes.
      */
     public final void startHeartbeats() {
         final Host[] hosts = cluster.getHostsArray();
         for (Host host : hosts) {
             Heartbeat.startHeartbeat(host);
         }
     }
 
     /**
      * This class holds info data for a network.
      */
     class NetworkInfo extends Info {
 
         /**
          * Prepares a new <code>NetworkInfo</code> object.
          */
         public NetworkInfo(final String name, final Network network) {
             super(name);
             setResource(network);
         }
 
         /**
          * Returns network info.
          */
         public String getInfo() {
             final String ret = "Net info: " + getNetwork().getName() + "\n"
                                + "     IPs: " + getNetwork().getIps()  + "\n"
                                + "Net mask: " + getNetwork().getNetMask()
                                + "\n";
             return ret;
         }
 
         /**
          * Returns network resource object.
          */
         public Network getNetwork() {
             return (Network) getResource();
         }
 
         /**
          * Returns menu icon for network.
          */
         public ImageIcon getMenuIcon() {
             return NETWORK_ICON;
         }
     }
 
     /**
      * This interface provides getDevice function for drbd block devices or
      * block devices that don't have drbd over them but are used by crm.
      */
     public interface CommonDeviceInterface {
         /** Returns the name. */
         String getName();
         /** Returns the device name. */
         String getDevice();
         /** Sets whether the device is used by crm. */
         void setUsedByCRM(boolean isUsedByCRM);
         /** Returns whether the device is used by crm. */
         boolean isUsedByCRM();
         /** Returns the last created filesystem. */
         String getCreatedFs();
         /** Returns how much of the device is used. */
         int getUsed();
     }
 
     /**
      * Returns common file systems on all nodes as StringInfo array.
      * The defaultValue is stored as the first item in the array.
      */
     public final StringInfo[] getCommonFileSystems(final String defaultValue) {
         StringInfo[] cfs =  new StringInfo[commonFileSystems.length + 1];
         cfs[0] = new StringInfo(defaultValue, null);
         int i = 1;
         for (String cf : commonFileSystems) {
             cfs[i] = new StringInfo(cf, cf);
             i++;
         }
         return cfs;
     }
 
 
     /**
      * this class holds info data, menus and configuration
      * for a drbd resource.
      */
     public class DrbdResourceInfo extends EditableInfo
                                   implements CommonDeviceInterface {
         /** BlockDevInfo object of the first block device. */
         private final BlockDevInfo blockDevInfo1;
         /** BlockDevInfo object of the second block device. */
         private final BlockDevInfo blockDevInfo2;
         /**
          * Whether the block device is used by heartbeat via Filesystem
          * service.
          */
         private boolean isUsedByCRM;
         /** Cache for getInfoPanel method. */
         private JComponent infoPanel = null;
         /** Whether the meta-data has to be created or not. */
         private boolean haveToCreateMD = false;
         /** Last created filesystem. */
         private String createdFs = null;
 
         /**
          * Prepares a new <code>DrbdResourceInfo</code> object.
          *
          * @param name
          *      name that will be shown in the tree
          * @param drbdDev
          *      drbd device
          * @param blockDevInfo1
          *      first block device info object belonging to this drbd device
          * @param blockDevInfo2
          *      second block device info object belonging to this drbd device
          */
         public DrbdResourceInfo(final String name,
                                 final String drbdDev,
                                 final BlockDevInfo blockDevInfo1,
                                 final BlockDevInfo blockDevInfo2) {
             super(name);
             setResource(new DrbdResource(name, null)); // TODO: ?
             initApplyButton();
             setResource(new DrbdResource(name, drbdDev)); // TODO: ?
             // TODO: drbdresource
             getResource().setValue(DRBD_RES_PARAM_DEV, drbdDev);
             this.blockDevInfo1 = blockDevInfo1;
             this.blockDevInfo2 = blockDevInfo2;
             initApplyButton(); // TODO: twice?
         }
 
         /**
          * Returns device name, like /dev/drbd0.
          */
         public final String getDevice() {
             return getDrbdResource().getDevice();
         }
 
         /**
          * Returns menu icon for drbd resource.
          */
         public final ImageIcon getMenuIcon() {
             return null;
         }
 
         /**
          * Returns cluster object to resource belongs.
          */
         public final Cluster getCluster() {
             return cluster;
         }
 
         /**
          * Returns other block device in the drbd cluster.
          */
         public final BlockDevInfo getOtherBlockDevInfo(final BlockDevInfo bdi) {
             if (bdi.equals(blockDevInfo1)) {
                 return blockDevInfo2;
             } else if (bdi.equals(blockDevInfo2)) {
                 return blockDevInfo1;
             } else {
                 return null;
             }
         }
 
         /**
          * Returns first block dev info.
          */
         public final BlockDevInfo getFirstBlockDevInfo() {
             return blockDevInfo1;
         }
 
         /**
          * Returns second block dev info.
          */
         public final BlockDevInfo getSecondBlockDevInfo() {
             return blockDevInfo2;
         }
 
         /**
          * Returns true if this is first block dev info.
          */
         public final boolean isFirstBlockDevInfo(final BlockDevInfo bdi) {
             return blockDevInfo1 == bdi;
         }
 
         /**
          * Creates drbd config for sections and returns it. Removes 'drbd: '
          * from the 'after' parameter.
          * TODO: move this out of gui
          */
         private String drbdSectionsConfig()
                                         throws Exceptions.DrbdConfigException {
             final StringBuffer config = new StringBuffer("");
             final String[] sections = drbdXML.getSections();
             for (String section : sections) {
                 if ("resource".equals(section) || "global".equals(section)) {
                     // TODO: Tools.getString
                     continue;
                 }
                 final String[] params = drbdXML.getSectionParams(section);
 
                 if (params.length != 0) {
                     final StringBuffer sectionConfig = new StringBuffer("");
                     for (String param : params) {
                         final String value = getResource().getValue(param);
                         if (value == null) {
                             Tools.debug(this, "section: " + section
                                     + ", param " + param + " ("
                                     + getName() + ") not defined");
                             throw new Exceptions.DrbdConfigException("param "
                                                     + param + " (" + getName()
                                                     + ") not defined");
                         }
                         if (!value.equals(getParamDefault(param))) {
                             if (isCheckBox(param)
                                 && value.equals(
                                             Tools.getString("Boolean.True"))) {
                                 /* boolean parameter */
                                 sectionConfig.append("\t\t" + param + ";\n");
                             } else if (DRBD_RES_PARAM_AFTER.equals(param)) {
                                 /* after parameter */
                                 if (!value.equals(Tools.getString(
                                                     "ClusterBrowser.None"))) {
                                     sectionConfig.append("\t\t");
                                     sectionConfig.append(param);
                                     sectionConfig.append('\t');
                                     sectionConfig.append(
                                                     Tools.escapeConfig(value));
                                     sectionConfig.append(";\n");
                                 }
                             } else { /* name value parameter */
                                 sectionConfig.append("\t\t");
                                 sectionConfig.append(param);
                                 sectionConfig.append('\t');
                                 sectionConfig.append(Tools.escapeConfig(value));
                                 sectionConfig.append(";\n");
                             }
                         }
                     }
 
                     if (sectionConfig.length() > 0) {
                         config.append("\t" + section + " {\n");
                         config.append(sectionConfig);
                         config.append("\t}\n\n");
                     }
                 }
             }
             return config.toString();
         }
         /**
          * Creates and returns drbd config for resources.
          *
          * TODO: move this out of gui
          */
         public final String drbdResourceConfig()
                                         throws Exceptions.DrbdConfigException {
             final StringBuffer config = new StringBuffer(50);
             config.append("resource " + getName() + " {\n");
             /* protocol... */
             final String[] params = drbdXML.getSectionParams("resource");
             for (String param : params) {
                 config.append('\t');
                 config.append(param);
                 config.append('\t');
                 config.append(getResource().getValue(param));
                 config.append(";\n");
             }
             if (params.length != 0) {
                 config.append('\n');
             }
             /* section config */
             try {
                 config.append(drbdSectionsConfig());
             } catch (Exceptions.DrbdConfigException dce) {
                 throw dce;
             }
             /* startup disk syncer net */
             config.append('\n');
             config.append(blockDevInfo1.drbdNodeConfig(getName(), getDevice()));
             config.append('\n');
             config.append(blockDevInfo2.drbdNodeConfig(getName(), getDevice()));
             config.append("}\n");
             return config.toString();
         }
 
         /**
          * Clears info panel cache.
          */
         public final boolean selectAutomaticallyInTreeMenu() {
             return infoPanel == null;
         }
 
         /**
          * Returns sync progress in percent.
          */
         public final String getSyncedProgress() {
             return blockDevInfo1.getBlockDevice().getSyncedProgress();
         }
 
         /**
          * Returns whether the cluster is syncing.
          */
         public final boolean isSyncing() {
             return blockDevInfo1.getBlockDevice().isSyncing();
         }
 
         /**
          * Connect block device from the specified host.
          */
         public final void connect(final Host host) {
             if (blockDevInfo1.getHost() == host
                 && !blockDevInfo1.getBlockDevice().isConnectedOrWF()) {
                 blockDevInfo1.connect();
             } else if (blockDevInfo2.getHost() == host
                        && !blockDevInfo2.getBlockDevice().isConnectedOrWF()) {
                 blockDevInfo2.connect();
             }
         }
 
         /**
          * Returns whether the resources is connected, meaning both devices are
          * connected.
          */
         public final boolean isConnected() {
             return blockDevInfo1.getBlockDevice().isConnected()
                    && blockDevInfo2.getBlockDevice().isConnected();
         }
 
         /**
          * Returns whether any of the sides in the drbd resource are in
          * paused-sync state.
          */
         public final boolean isPausedSync() {
             return blockDevInfo1.getBlockDevice().isPausedSync()
                    || blockDevInfo2.getBlockDevice().isPausedSync();
         }
 
         /**
          * Returns whether any of the sides in the drbd resource are in
          * split-brain.
          */
         public final boolean isSplitBrain() {
             return blockDevInfo1.getBlockDevice().isSplitBrain()
                    || blockDevInfo2.getBlockDevice().isSplitBrain();
         }
 
         /**
          * Returns drbd graphical view.
          */
         public final JPanel getGraphicalView() {
             return drbdGraph.getGraphPanel();
         }
 
         /**
          * Returns the DrbdInfo object (for all drbds).
          */
         public final DrbdInfo getDrbdInfo() {
             return drbdGraph.getDrbdInfo();
         }
 
         /**
          * Returns all parameters.
          */
         public final String[] getParametersFromXML() {
             return drbdXML.getParameters();
         }
 
         /**
          * Checks the new value of the parameter if it is conforms to its type
          * and other constraints.
          */
         protected final boolean checkParam(final String param,
                                            final String newValue) {
             return drbdXML.checkParam(param, newValue);
         }
 
         /**
          * Returns the default value for the drbd parameter.
          */
         protected final String getParamDefault(final String param) {
             return drbdXML.getParamDefault(param);
         }
 
         /**
          * Returns the preferred value for the drbd parameter.
          */
         protected final String getParamPreferred(final String param) {
             return drbdXML.getParamPreferred(param);
         }
 
         /**
          * Returns the possible values for the pulldown menus, if applicable.
          */
         protected final Object[] getParamPossibleChoices(final String param) {
             return drbdXML.getPossibleChoices(param);
         }
 
         /**
          * Returns the short description of the drbd parameter that is used as
          * a label.
          */
         protected final String getParamShortDesc(final String param) {
             return drbdXML.getParamShortDesc(param);
         }
 
         /**
          * Returns a long description of the parameter that is used for tool
          * tip.
          */
         protected final String getParamLongDesc(final String param) {
             return drbdXML.getParamLongDesc(param);
         }
 
         /**
          * Returns section to which this drbd parameter belongs.
          */
         protected final String getSection(final String param) {
             return drbdXML.getSection(param);
         }
 
         /**
          * Returns whether this drbd parameter is required parameter.
          */
         protected final boolean isRequired(final String param) {
             return drbdXML.isRequired(param);
         }
 
         /**
          * Returns whether this drbd parameter is of integer type.
          */
         protected final boolean isInteger(final String param) {
             return drbdXML.isInteger(param);
         }
 
         /**
          * Returns whether this drbd parameter is of time type.
          */
         protected final boolean isTimeType(final String param) {
             /* not required */
             return false;
         }
 
         /**
          * Returns whether this parameter has a unit prefix.
          */
         protected final boolean hasUnitPrefix(final String param) {
             return drbdXML.hasUnitPrefix(param);
         }
 
         /**
          * Returns the long unit name.
          */
         protected final String getUnitLong(final String param) {
             return drbdXML.getUnitLong(param);
         }
 
         /**
          * Returns the default unit for the parameter.
          */
         protected final String getDefaultUnit(final String param) {
             return drbdXML.getDefaultUnit(param);
         }
 
         /**
          * Returns whether the parameter is of the boolean type and needs the
          * checkbox.
          */
         protected final boolean isCheckBox(final String param) {
             final String type = drbdXML.getParamType(param);
             if (type == null) {
                 return false;
             }
             if (DRBD_RES_BOOL_TYPE_NAME.equals(type)) {
                 return true;
             }
             return false;
         }
 
         /**
          * Returns the type of the parameter (like boolean).
          */
         protected final String getParamType(final String param) {
             return drbdXML.getParamType(param);
         }
 
         /**
          * Returns the widget that is used to edit this parameter.
          */
         protected final GuiComboBox getParamComboBox(final String param,
                                                      final String prefix,
                                                      final int width) {
             GuiComboBox paramCb;
             final Object[] possibleChoices = getParamPossibleChoices(param);
             getResource().setPossibleChoices(param, possibleChoices);
             if (DRBD_RES_PARAM_NAME.equals(param)) {
                 String resName;
                 if (getResource().getValue(DRBD_RES_PARAM_NAME) == null) {
                     resName =
                             getResource().getDefaultValue(DRBD_RES_PARAM_NAME);
                 } else {
                     resName = getResource().getName();
                 }
                 paramCb = new GuiComboBox(resName,
                                           null,
                                           null,
                                           null,
                                           width,
                                           null);
                 paramCb.setEnabled(!getDrbdResource().isCommited());
                 paramComboBoxAdd(param, prefix, paramCb);
             } else if (DRBD_RES_PARAM_DEV.equals(param)) {
                 final List<String> drbdDevices = new ArrayList<String>();
                 if (getResource().getValue(DRBD_RES_PARAM_DEV) == null) {
                     final String defaultItem =
                         getDrbdResource().getDefaultValue(DRBD_RES_PARAM_DEV);
                     drbdDevices.add(defaultItem);
                     int i = 0;
                     int index = 0;
                     while (i < 11) {
                         final String drbdDevStr = "/dev/drbd"
                                                   + Integer.toString(index);
                         if (!drbdDevHash.containsKey(drbdDevStr)) {
                             drbdDevices.add(drbdDevStr);
                             i++;
                         }
                         index++;
                     }
                     paramCb = new GuiComboBox(defaultItem,
                                               drbdDevices.toArray(
                                                 new String[drbdDevices.size()]),
                                               null,
                                               null,
                                               width,
                                               null);
                     paramCb.setEditable(true);
                 } else {
                     final String defaultItem = getDevice();
                     String regexp = null;
                     if (isInteger(param)) {
                         regexp = "^-?\\d*$";
                     }
                     paramCb = new GuiComboBox(
                                         defaultItem,
                                         getResource().getPossibleChoices(param),
                                         null,
                                         regexp,
                                         width,
                                         null);
                 }
                 paramCb.setEnabled(!getDrbdResource().isCommited());
                 paramComboBoxAdd(param, prefix, paramCb);
 
             } else if (DRBD_RES_PARAM_AFTER.equals(param)) {
                 final List<Object> l = new ArrayList<Object>();
                 String defaultItem =
                                 getResource().getValue(DRBD_RES_PARAM_AFTER);
                 final StringInfo di = new StringInfo(
                                         Tools.getString("ClusterBrowser.None"),
                                         "-1");
                 l.add(di);
                 if (defaultItem == null) {
                     defaultItem = Tools.getString("ClusterBrowser.None");
                 }
 
                 for (final String drbdRes : drbdResHash.keySet()) {
                     final DrbdResourceInfo r = drbdResHash.get(drbdRes);
                     DrbdResourceInfo odri = r;
                     boolean cyclicRef = false;
                     while ((odri = drbdResHash.get(
                               odri.getResource().getValue("after"))) != null) {
                         if (odri == this) {
                             cyclicRef = true;
                         }
                     }
                     if (r != this && !cyclicRef) {
                         l.add(r);
                     }
                 }
 
                 paramCb = new GuiComboBox(defaultItem,
                                           l.toArray(new Object[l.size()]),
                                           null,
                                           null,
                                           width,
                                           null);
 
                 paramComboBoxAdd(param, prefix, paramCb);
             } else if (hasUnitPrefix(param)) {
                 String selectedValue = getResource().getValue(param);
                 if (selectedValue == null) {
                     selectedValue = getParamPreferred(param);
                     if (selectedValue == null) {
                         selectedValue = getParamDefault(param);
                     }
                 }
                 String unit = getUnitLong(param);
                 if (unit == null) {
                     unit = "";
                 }
 
                 final int index = unit.indexOf('/');
                 String unitPart = "";
                 if (index > -1) {
                     unitPart = unit.substring(index);
                 }
 
                 final Unit[] units = {
                     new Unit("", "", "Byte", "Bytes"),
 
                     new Unit("K",
                              "k",
                              "KiByte" + unitPart,
                              "KiBytes" + unitPart),
 
                     new Unit("M",
                              "m",
                              "MiByte" + unitPart,
                              "MiBytes" + unitPart),
 
                     new Unit("G",
                              "g",
                              "GiByte" + unitPart,
                              "GiBytes" + unitPart),
 
                     new Unit("S",
                              "s",
                              "Sector" + unitPart,
                              "Sectors" + unitPart)
                 };
 
                 String regexp = null;
                 if (isInteger(param)) {
                     regexp = "^-?\\d*$";
                 }
                 paramCb = new GuiComboBox(selectedValue,
                                           getPossibleChoices(param),
                                           units,
                                           GuiComboBox.Type.TEXTFIELDWITHUNIT,
                                           regexp,
                                           width,
                                           null);
 
                 paramComboBoxAdd(param, prefix, paramCb);
             } else {
                 paramCb = super.getParamComboBox(param, prefix, width);
                 if (possibleChoices != null) {
                     paramCb.setEditable(false);
                 }
             }
             return paramCb;
         }
 
         /**
          * Returns the DrbdResource object of this drbd resource.
          */
         private DrbdResource getDrbdResource() {
             return (DrbdResource) getResource();
         }
 
         /**
          * Applies changes that user made to the drbd resource fields.
          */
         public final void apply() {
             final String[] params = getParametersFromXML();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                 }
             });
             drbdResHash.remove(getName());
             drbdDevHash.remove(getDevice());
             storeComboBoxValues(params);
 
             final String name = getResource().getValue(DRBD_RES_PARAM_NAME);
             final String drbdDevStr =
                             getResource().getValue(DRBD_RES_PARAM_DEV);
             getDrbdResource().setName(name);
             setName(name);
             getDrbdResource().setDevice(drbdDevStr);
 
             drbdResHash.put(name, this);
             drbdDevHash.put(drbdDevStr, this);
             drbdGraph.repaint();
         }
 
         /**
          * Returns panel with form to configure a drbd resource.
          */
         public final JComponent getInfoPanel() {
             drbdGraph.pickInfo(this);
             if (infoPanel != null) {
                 return infoPanel;
             }
 
             final JPanel mainPanel = new JPanel();
             mainPanel.setBackground(PANEL_BACKGROUND);
             mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 
             final JPanel buttonPanel = new JPanel(new BorderLayout());
             buttonPanel.setBackground(STATUS_BACKGROUND);
             buttonPanel.setMinimumSize(new Dimension(0, 50));
             buttonPanel.setPreferredSize(new Dimension(0, 50));
             buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
 
             final JPanel optionsPanel = new JPanel();
             optionsPanel.setBackground(PANEL_BACKGROUND);
             optionsPanel.setLayout(new BoxLayout(optionsPanel,
                                                  BoxLayout.Y_AXIS));
             optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             final JPanel extraOptionsPanel = new JPanel();
             extraOptionsPanel.setBackground(EXTRA_PANEL_BACKGROUND);
             extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel,
                                                       BoxLayout.Y_AXIS));
             extraOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             mainPanel.add(buttonPanel);
 
             /* Actions */
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             final JMenu serviceCombo = getActionsMenu();
             updateMenus(null);
             mb.add(serviceCombo);
             buttonPanel.add(mb, BorderLayout.EAST);
 
             /* resource name */
             getResource().setValue(DRBD_RES_PARAM_NAME,
                                    getDrbdResource().getName());
             getResource().setValue(DRBD_RES_PARAM_DEV,
                                    getDevice());
 
 
             final String[] params = getParametersFromXML();
             addParams(optionsPanel,
                       extraOptionsPanel,
                       params,
                       Tools.getDefaultInt("ClusterBrowser.DrbdResLabelWidth"),
                       Tools.getDefaultInt("ClusterBrowser.DrbdResFieldWidth")
                       );
 
             applyButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         Thread thread = new Thread(new Runnable() {
                             public void run() {
                                 clStatusLock();
                                 apply();
                                 try {
                                     getDrbdInfo().createDrbdConfig();
                                     for (final Host h : cluster.getHostsArray()) {
                                         DRBD.adjust(h, "all");
                                     }
                                 } catch (Exceptions.DrbdConfigException dce) {
                                     clStatusUnlock();
                                     Tools.appError("config failed");
                                 }
                                 clStatusUnlock();
                             }
                         });
                         thread.start();
                     }
                 }
             );
 
             addApplyButton(buttonPanel);
             applyButton.setEnabled(checkResourceFields(null, params));
             /* expert mode */
             buttonPanel.add(Tools.expertModeButton(extraOptionsPanel));
 
             mainPanel.add(optionsPanel);
             mainPanel.add(extraOptionsPanel);
 
             infoPanel = new JPanel();
             infoPanel.setBackground(PANEL_BACKGROUND);
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             infoPanel.add(buttonPanel);
             infoPanel.add(new JScrollPane(mainPanel));
             infoPanel.add(Box.createVerticalGlue());
             return infoPanel;
         }
 
         /**
          * Removes this drbd resource with confirmation dialog.
          */
         public final void removeMyself() {
             String desc = Tools.getString(
                         "ClusterBrowser.confirmRemoveDrbdResource.Description");
             desc = desc.replaceAll("@RESOURCE@", getName());
             if (Tools.confirmDialog(
                     Tools.getString(
                           "ClusterBrowser.confirmRemoveDrbdResource.Title"),
                     desc,
                     Tools.getString(
                           "ClusterBrowser.confirmRemoveDrbdResource.Yes"),
                     Tools.getString(
                           "ClusterBrowser.confirmRemoveDrbdResource.No"))) {
                 removeMyselfNoConfirm();
             }
         }
 
         /**
          * removes this object from jtree and from list of drbd resource
          * infos without confirmation dialog.
          */
         public final void removeMyselfNoConfirm() {
             drbdGraph.removeDrbdResource(this);
             final Host[] hosts = cluster.getHostsArray();
             for (Host host : hosts) {
                 DRBD.down(host, getName());
             }
             super.removeMyself();
             final DrbdResourceInfo dri = drbdResHash.get(getName());
             drbdResHash.remove(getName());
             dri.setName(null);
             reload(servicesNode);
             //reload(drbdNode);
             //getDrbdInfo().selectMyself();
             drbdDevHash.remove(getDevice());
             blockDevInfo1.removeFromDrbd();
             blockDevInfo2.removeFromDrbd();
             blockDevInfo1.removeMyself();
             blockDevInfo2.removeMyself();
 
             updateCommonBlockDevices();
 
             try {
                 drbdGraph.getDrbdInfo().createDrbdConfig();
             } catch (Exceptions.DrbdConfigException dce) {
                 Tools.appError("config failed");
             }
             drbdGraph.getDrbdInfo().setSelectedNode(null);
             drbdGraph.getDrbdInfo().selectMyself();
             //SwingUtilities.invokeLater(new Runnable() { public void run() {
                 drbdGraph.updatePopupMenus();
             //} });
             resetFilesystems();
             infoPanel = null;
             reload(drbdNode);
         }
 
         /**
          * Returns string of the drbd resource.
          */
         public final String toString() {
             String name = getName();
             if (name == null) {
                 name = Tools.getString("ClusterBrowser.DrbdResUnconfigured");
             }
             return "drbd: " + name;
         }
 
         /**
          * Returns whether two drbd resources are equal.
          */
         public final boolean equals(final Object value) {
             if (value == null) {
                 return false;
             }
             if (Tools.isStringClass(value)) {
                 return getDrbdResource().getValue(DRBD_RES_PARAM_DEV).equals(
                                                               value.toString());
             } else {
                 if (toString() == null) {
                     return false;
                 }
                 return toString().equals(value.toString());
             }
         }
 
         //public int hashCode() {
         //    return toString().hashCode();
         //}
 
         /**
          * Returns the device name that is used as the string value of the drbd
          * resource in Filesystem hb service.
          */
         public final String getStringValue() {
             return getDevice();
         }
 
         /**
          * Adds old style drbddisk service in the heartbeat and graph.
          *
          * @param fi
          *              File system before which this drbd info should be
          *              started
          */
         public final void addDrbdDisk(final FilesystemInfo fi) {
             final Point2D p = null;
             final DrbddiskInfo di =
                 (DrbddiskInfo) heartbeatGraph.getServicesInfo().addServicePanel(
                                         crmXML.getHbDrbddisk(), p, true, null);
             di.setGroupInfo(fi.getGroupInfo());
             addToHeartbeatIdList(di);
             fi.setDrbddiskInfo(di);
             di.getInfoPanel();
             di.paramComboBoxGet("1", null).setValueAndWait(getName());
             di.apply();
             heartbeatGraph.addColocation(di, fi);
         }
 
         /**
          * Adds linbit::drbd service in the pacemaker graph.
          *
          * @param fi
          *              File system before which this drbd info should be
          *              started
          */
         public final void addLinbitDrbd(final FilesystemInfo fi) {
             final Point2D p = null;
             final LinbitDrbdInfo ldi =
              (LinbitDrbdInfo) heartbeatGraph.getServicesInfo().addServicePanel(
                                  crmXML.getHbLinbitDrbd(), p, true, null);
             ldi.setGroupInfo(fi.getGroupInfo());
             addToHeartbeatIdList(ldi);
             fi.setLinbitDrbdInfo(ldi);
             ldi.getInfoPanel();
             ldi.paramComboBoxGet("drbd_resource", null).setValueAndWait(
                                                                     getName());
             ldi.apply();
             heartbeatGraph.addColocation(ldi.getCloneInfo(), fi);
         }
 
 
         /**
          * Remove drbddisk heartbeat service.
          */
         public final void removeDrbdDisk(final FilesystemInfo fi) {
             final DrbddiskInfo drbddiskInfo = fi.getDrbddiskInfo();
             if (drbddiskInfo != null) {
                 drbddiskInfo.removeMyselfNoConfirm();
             }
         }
 
         /**
          * Remove drbddisk heartbeat service.
          */
         public final void removeLinbitDrbd(final FilesystemInfo fi) {
             final LinbitDrbdInfo linbitDrbdInfo = fi.getLinbitDrbdInfo();
             if (linbitDrbdInfo != null) {
                 linbitDrbdInfo.removeMyselfNoConfirm();
             }
         }
 
         /**
          * Sets that this drbd resource is used by hb.
          */
         public final void setUsedByCRM(final boolean isUsedByCRM) {
             this.isUsedByCRM = isUsedByCRM;
         }
 
         /**
          * Returns whether this drbd resource is used by crm.
          */
         public final boolean isUsedByCRM() {
             return isUsedByCRM;
         }
 
         /**
          * Returns common file systems. This is call from a dialog and it calls
          * the normal getCommonFileSystems function. TODO: It's a hack.
          */
         public final StringInfo[] getCommonFileSystems2(
                                                     final String defaultValue) {
             return getCommonFileSystems(defaultValue);
         }
 
         /**
          * Returns both hosts of the drbd connection, sorted alphabeticaly.
          */
         public final Host[] getHosts() {
             final Host h1 = blockDevInfo1.getHost();
             final Host h2 = blockDevInfo2.getHost();
             if (h1.getName().compareToIgnoreCase(h2.getName()) < 0) {
                 return new Host[]{h1, h2};
             } else {
                 return new Host[]{h2, h1};
             }
         }
 
         /**
          * Starts resolve split brain dialog.
          */
         public final void resolveSplitBrain() {
             final AddDrbdSplitBrainDialog adrd =
                                             new AddDrbdSplitBrainDialog(this);
             adrd.showDialogs();
         }
 
         /**
          * Returns whether the specified host has this drbd resource.
          */
         public final boolean resourceInHost(final Host host) {
             if (blockDevInfo1.getHost() == host
                 || blockDevInfo2.getHost() == host) {
                 return true;
             }
             return false;
         }
 
         /**
          * Returns the list of items for the popup menu for drbd resource.
          */
         public final List<UpdatableItem> createPopup() {
             final List<UpdatableItem> items = new ArrayList<UpdatableItem>();
 
             final MyMenuItem removeResMenu = new MyMenuItem(
                             Tools.getString("ClusterBrowser.Drbd.RemoveEdge"),
                             REMOVE_ICON,
                             Tools.getString(
                                     "ClusterBrowser.Drbd.RemoveEdge.ToolTip")
                            ) {
                 private static final long serialVersionUID = 1L;
                 public void action() {
                     /* this drbdResourceInfo remove myself and this calls
                        removeDrbdResource in this class, that removes the edge
                        in the graph. */
                     removeMyself();
                 }
 
                 public boolean enablePredicate() {
                     return !isUsedByCRM();
                 }
             };
             registerMenuItem(removeResMenu);
             items.add(removeResMenu);
             final DrbdResourceInfo thisClass = this;
 
             final MyMenuItem connectMenu = new MyMenuItem(
                         Tools.getString("ClusterBrowser.Drbd.ResourceConnect"),
                         null,
                         Tools.getString(
                                 "ClusterBrowser.Drbd.ResourceConnect.ToolTip"),
 
                         Tools.getString(
                                 "ClusterBrowser.Drbd.ResourceDisconnect"),
                         null,
                         Tools.getString(
                             "ClusterBrowser.Drbd.ResourceDisconnect.ToolTip")
                        ) {
 
                 private static final long serialVersionUID = 1L;
 
                 public boolean predicate() {
                     return !isConnected();
                 }
 
                 public boolean enablePredicate() {
                     return !isSyncing();
                 }
 
                 public void action() {
                     BlockDevInfo sourceBDI = drbdGraph.getSource(thisClass);
                     BlockDevInfo destBDI   = drbdGraph.getDest(thisClass);
                     if (this.getText().equals(Tools.getString(
                                     "ClusterBrowser.Drbd.ResourceConnect"))) {
                         if (!destBDI.getBlockDevice().isConnectedOrWF()) {
                             destBDI.connect();
                         }
                         if (!sourceBDI.getBlockDevice().isConnectedOrWF()) {
                             sourceBDI.connect();
                         }
                     } else {
                         destBDI.disconnect();
                         sourceBDI.disconnect();
                     }
                 }
             };
             registerMenuItem(connectMenu);
             items.add(connectMenu);
 
             final MyMenuItem resumeSync = new MyMenuItem(
                    Tools.getString("ClusterBrowser.Drbd.ResourceResumeSync"),
                    null,
                    Tools.getString(
                             "ClusterBrowser.Drbd.ResourceResumeSync.ToolTip"),
 
                    Tools.getString("ClusterBrowser.Drbd.ResourcePauseSync"),
                    null,
                    Tools.getString(
                             "ClusterBrowser.Drbd.ResourcePauseSync.ToolTip")
                   ) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean predicate() {
                     return isPausedSync();
                 }
 
                 public boolean enablePredicate() {
                     return isSyncing();
                 }
                 public void action() {
                     BlockDevInfo sourceBDI = drbdGraph.getSource(thisClass);
                     BlockDevInfo destBDI   = drbdGraph.getDest(thisClass);
                     if (this.getText().equals(Tools.getString(
                                 "ClusterBrowser.Drbd.ResourceResumeSync"))) {
                         if (destBDI.getBlockDevice().isPausedSync()) {
                             destBDI.resumeSync();
                         }
                         if (sourceBDI.getBlockDevice().isPausedSync()) {
                             sourceBDI.resumeSync();
                         }
                     } else {
                             sourceBDI.pauseSync();
                             destBDI.pauseSync();
                     }
                 }
             };
             registerMenuItem(resumeSync);
             items.add(resumeSync);
 
             /* resolve split-brain */
             final MyMenuItem splitBrainMenu = new MyMenuItem(
                     Tools.getString("ClusterBrowser.Drbd.ResolveSplitBrain"),
                     null,
                     Tools.getString(
                                 "ClusterBrowser.Drbd.ResolveSplitBrain.ToolTip")
                    ) {
 
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     return isSplitBrain();
                 }
 
                 public void action() {
                     resolveSplitBrain();
                 }
             };
             registerMenuItem(splitBrainMenu);
             items.add(splitBrainMenu);
 
             /* view log */
             final MyMenuItem viewLogMenu = new MyMenuItem(
                                 Tools.getString("ClusterBrowser.Drbd.ViewLogs"),
                                 null,
                                 null) {
 
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     return true;
                 }
 
                 public void action() {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             getPopup().setVisible(false);
                         }
                     });
                     final String device = getDevice();
                     ClusterDrbdLogs l = new ClusterDrbdLogs(getCluster(),
                                                             device);
                     l.showDialog();
                 }
             };
             registerMenuItem(viewLogMenu);
             items.add(viewLogMenu);
             return items;
         }
 
         /**
          * Sets whether the meta-data have to be created, meaning there are no
          * existing meta-data for this resource on both nodes.
          */
         public final void setHaveToCreateMD(final boolean haveToCreateMD) {
             this.haveToCreateMD = haveToCreateMD;
         }
 
         /**
          * Returns whether the md has to be created or not.
          */
         public final boolean isHaveToCreateMD() {
             return haveToCreateMD;
         }
 
         /**
          * Returns meta-disk device for the specified host.
          */
         public final String getMetaDiskForHost(final Host host) {
             return drbdXML.getMetaDisk(host.getName(), getName());
         }
 
         /**
          * Returns tool tip when mouse is over the resource edge.
          */
         public String getToolTipForGraph() {
             return getName();
         }
 
         /**
          * Returns the last created filesystem.
          */
         public final String getCreatedFs() {
             return createdFs;
         }
 
         /**
          * Sets the last created filesystem.
          */
         public final void setCreatedFs(final String createdFs) {
             this.createdFs = createdFs;
         }
 
         /**
          * Returns how much diskspace is used on the primary.
          */
         public final int getUsed() {
             if (blockDevInfo1.getBlockDevice().isPrimary()) {
                 return blockDevInfo1.getBlockDevice().getUsed();
             } else if (blockDevInfo2.getBlockDevice().isPrimary()) {
                 return blockDevInfo2.getBlockDevice().getUsed();
             }
             return -1;
         }
     }
 
     /**
      * This class holds the information about heartbeat service from the ocfs,
      * to show it to the user.
      */
     class AvailableServiceInfo extends HbCategoryInfo {
         /** Info about the service. */
         private final ResourceAgent resourceAgent;
 
         /**
          * Prepares a new <code>AvailableServiceInfo</code> object.
          */
         public AvailableServiceInfo(final ResourceAgent resourceAgent) {
             super(resourceAgent.getName());
             this.resourceAgent = resourceAgent;
         }
 
         /**
          * Returns heartbeat service class.
          */
         public ResourceAgent getResourceAgent() {
             return resourceAgent;
         }
 
         /**
          * Returns icon for this menu category.
          */
         public ImageIcon getMenuIcon() {
             return AVAIL_SERVICES_ICON;
         }
 
         /**
          * Returns type of the info text. text/plain or text/html.
          */
         protected String getInfoType() {
             return "text/html";
         }
         /**
          * Returns the info about the service.
          */
         public String getInfo() {
             final StringBuffer s = new StringBuffer(30);
             s.append("<h2>");
             s.append(getName());
             s.append(" (");
             s.append(crmXML.getVersion(resourceAgent));
             s.append(")</h2><h3>");
             s.append(crmXML.getShortDesc(resourceAgent));
             s.append("</h3>");
             s.append(crmXML.getLongDesc(resourceAgent));
             final String[] params = crmXML.getParameters(resourceAgent);
             for (String param : params) {
                 s.append(crmXML.getParamLongDesc(resourceAgent, param));
                 s.append("<br>");
             }
             return s.toString();
         }
     }
 
     /**
      * This class holds info data for a block device that is common
      * in all hosts in the cluster and can be chosen in the scrolling list in
      * the filesystem service.
      */
     class CommonBlockDevInfo extends HbCategoryInfo
                              implements CommonDeviceInterface {
         /** block devices of this common block device on all nodes. */
         private final BlockDevice[] blockDevices;
 
         /**
          * Prepares a new <code>CommonBlockDevInfo</code> object.
          */
         public CommonBlockDevInfo(final String name,
                                   final BlockDevice[] blockDevices) {
             super(name);
             setResource(new CommonBlockDevice(name));
             this.blockDevices = blockDevices;
         }
 
         /**
          * Returns icon for common block devices menu category.
          */
         public ImageIcon getMenuIcon() {
             return COMMON_BD_ICON;
         }
 
         /**
          * Returns device name of this block device.
          */
         public String getDevice() {
             return getCommonBlockDevice().getDevice();
         }
 
         /**
          * Returns info for this block device.
          */
         public String getInfo() {
             return "Device    : " + getCommonBlockDevice().getName() + "\n";
         }
 
         /**
          * Returns string representation of the block devices, used in the pull
          * down menu.
          */
         public String toString() {
             String name = getName();
             if (name == null) {
                 name = Tools.getString(
                                    "ClusterBrowser.CommonBlockDevUnconfigured");
             }
             return name;
         }
 
         /**
          * Sets this block device on all nodes ass used by crm.
          */
         public void setUsedByCRM(final boolean isUsedByCRM) {
             for (BlockDevice bd : blockDevices) {
                 bd.setUsedByCRM(isUsedByCRM);
             }
         }
 
         /**
          * Returns if all of the block devices are used by crm.
          * TODO: or any is used by hb?
          */
         public boolean isUsedByCRM() {
             boolean is = true;
             for (int i = 0; i < blockDevices.length; i++) {
                 is = is && blockDevices[i].isUsedByCRM();
             }
             return is;
         }
 
         /**
          * Retruns resource object of this block device.
          */
         public CommonBlockDevice getCommonBlockDevice() {
             return (CommonBlockDevice) getResource();
         }
         /** Returns the last created filesystem. */
         public final String getCreatedFs() {
             return null;
         }
 
         /**
          * Returns how much of the filesystem is used.
          */
         public final int getUsed() {
             int used = -1;
             for (BlockDevice bd : blockDevices) {
                 if (bd.getUsed() > used) {
                     used = bd.getUsed();
                 }
             }
             return used;
         }
     }
 
     /**
      * This class holds info about IPaddr/IPaddr2 heartbeat service. It adds a
      * better ip entering capabilities.
      */
     class IPaddrInfo extends ServiceInfo {
         /**
          * Creates new IPaddrInfo object.
          */
         public IPaddrInfo(final String name, final ResourceAgent ra) {
             super(name, ra);
         }
 
         /**
          * Creates new IPaddrInfo object.
          */
         public IPaddrInfo(final String name,
                           final ResourceAgent ra,
                           final String hbId,
                           final Map<String, String> resourceNode) {
             super(name, ra, hbId, resourceNode);
         }
 
         /**
          * Adds if field.
          */
         protected void addIdField(final JPanel panel,
                                   final int leftWidth,
                                   final int rightWidth) {
             super.addIdField(panel, leftWidth, rightWidth);
         }
 
         /**
          * Returns whether all the parameters are correct. If param is null,
          * all paremeters will be checked, otherwise only the param, but other
          * parameters will be checked only in the cache. This is good if only
          * one value is changed and we don't want to check everything.
          */
         public boolean checkResourceFieldsCorrect(final String param,
                                                   final String[] params) {
             boolean ret = super.checkResourceFieldsCorrect(param, params);
             final GuiComboBox cb;
             if (getResourceAgent().isHeartbeatClass()) {
                 cb = paramComboBoxGet("1", null);
             } else if (getResourceAgent().isOCFClass()) {
                 cb = paramComboBoxGet("ip", null);
             } else {
                 return true;
             }
             if (cb == null) {
                 return false;
             }
             cb.setEditable(true);
             cb.selectSubnet();
             if (ret) {
                 final String ip = cb.getStringValue();
                 if (!Tools.checkIp(ip)) {
                     ret = false;
                 }
             }
             return ret;
         }
 
         /**
          * Returns combo box for parameter.
          */
         protected GuiComboBox getParamComboBox(final String param,
                                                final String prefix,
                                                final int width) {
             GuiComboBox paramCb;
             if ("ip".equals(param)) {
                 /* get networks */
                 final String ip = getResource().getValue("ip");
                 Info defaultValue;
                 if (ip == null) {
                     defaultValue = new StringInfo(
                         Tools.getString("ClusterBrowser.SelectNetInterface"),
                         null);
                 } else {
                     defaultValue = new StringInfo(ip, ip);
                 }
                 final Info[] networks =
                                     enumToInfoArray(defaultValue,
                                                     getName(),
                                                     networksNode.children());
 
                 final String regexp = "^[\\d.*]*|Select\\.\\.\\.$";
                 paramCb = new GuiComboBox(ip,
                                           networks,
                                           GuiComboBox.Type.COMBOBOX,
                                           regexp,
                                           width,
                                           null);
 
                 paramCb.setAlwaysEditable(true);
                 paramComboBoxAdd(param, prefix, paramCb);
             } else {
                 paramCb = super.getParamComboBox(param, prefix, width);
             }
             return paramCb;
         }
 
         /**
          * Returns string representation of the ip address.
          * In the form of 'ip (interface)'
          */
         public String toString() {
             final String id = getService().getId();
             if (id == null) {
                 return super.toString(); /* this is for 'new IPaddrInfo' */
             }
 
             final StringBuffer s = new StringBuffer(getName());
             String inside = "";
             if (!id.matches("^\\d+$")) {
                 inside = id + " / ";
             }
             String ip = getResource().getValue("ip");
             if (ip == null) {
                 ip = Tools.getString("ClusterBrowser.Ip.Unconfigured");
             }
             s.append(" (" + inside + ip + ")");
 
             return s.toString();
         }
     }
 
     /**
      * This class holds info about Filesystem service. It is treated in special
      * way, so that it can use block device information and drbd devices. If
      * drbd device is selected, the drbddisk service will be added too.
      */
     class FilesystemInfo extends ServiceInfo {
         /** linbit::drbd service object. */
         private LinbitDrbdInfo linbitDrbdInfo = null;
         /** drbddisk service object. */
         private DrbddiskInfo drbddiskInfo = null;
         /** Block device combo box. */
         private GuiComboBox blockDeviceParamCb = null;
         /** Filesystem type combo box. */
         private GuiComboBox fstypeParamCb = null;
         /** Whether old style drbddisk is preferred. */
         private boolean drbddiskIsPreferred = false;
 
         /**
          * Creates the FilesystemInfo object.
          */
         public FilesystemInfo(final String name,
                               final ResourceAgent ra) {
             super(name, ra);
         }
 
         /**
          * Creates the FilesystemInfo object.
          */
         public FilesystemInfo(final String name,
                               final ResourceAgent ra,
                               final String hbId,
                               final Map<String, String> resourceNode) {
             super(name, ra, hbId, resourceNode);
         }
 
         /**
          * Sets Linbit::drbd info object for this Filesystem service if it uses
          * drbd block device.
          */
         public void setLinbitDrbdInfo(final LinbitDrbdInfo linbitDrbdInfo) {
             this.linbitDrbdInfo = linbitDrbdInfo;
         }
 
         /**
          * Returns linbit::drbd info object that is associated with the drbd device
          * or null if it is not a drbd device.
          */
         public LinbitDrbdInfo getLinbitDrbdInfo() {
             return linbitDrbdInfo;
         }
 
         /**
          * Sets DrbddiskInfo object for this Filesystem service if it uses drbd
          * block device.
          */
         public void setDrbddiskInfo(final DrbddiskInfo drbddiskInfo) {
             this.drbddiskInfo = drbddiskInfo;
         }
 
         /**
          * Returns DrbddiskInfo object that is associated with the drbd device
          * or null if it is not a drbd device.
          */
         public DrbddiskInfo getDrbddiskInfo() {
             return drbddiskInfo;
         }
 
         /**
          * Adds id field.
          */
         protected void addIdField(final JPanel panel,
                                   final int leftWidth,
                                   final int rightWidth) {
             super.addIdField(panel, leftWidth, rightWidth);
         }
 
         /**
          * Returns whether all the parameters are correct. If param is null,
          * all paremeters will be checked, otherwise only the param, but other
          * parameters will be checked only in the cache. This is good if only
          * one value is changed and we don't want to check everything.
          */
         public boolean checkResourceFieldsCorrect(final String param,
                                                   final String[] params) {
             final boolean ret = super.checkResourceFieldsCorrect(param, params);
             if (!ret) {
                 return false;
             }
             final GuiComboBox cb = paramComboBoxGet(DRBD_RES_PARAM_DEV, null);
             if (cb == null || cb.getValue() == null) {
                 return false;
             }
             return true;
         }
 
         /**
          * Applies changes to the Filesystem service paramters.
          */
         public void apply() {
             final String dir = getComboBoxValue("directory");
             for (Host host : getClusterHosts()) {
                 final String hostName = host.getName();
                 final String ret = Tools.execCommandProgressIndicator(
                                                        host,
                                                        "stat -c \"%F\" " + dir,
                                                        null,
                                                        true);
 
                 if (ret == null || !"directory".equals(ret.trim())) {
                     String title =
                             Tools.getString("ClusterBrowser.CreateDir.Title");
                     String desc  =
                         Tools.getString("ClusterBrowser.CreateDir.Description");
                     title = title.replaceAll("@DIR@", dir);
                     title = title.replaceAll("@HOST@", host.getName());
                     desc  = desc.replaceAll("@DIR@", dir);
                     desc  = desc.replaceAll("@HOST@", host.getName());
                     if (Tools.confirmDialog(
                             title,
                             desc,
                             Tools.getString("ClusterBrowser.CreateDir.Yes"),
                             Tools.getString("ClusterBrowser.CreateDir.No"))) {
                         Tools.execCommandProgressIndicator(host, "mkdir " + dir, null, true);
                     }
                 }
             }
             super.apply();
             //TODO: escape dir
         }
 
         /**
          * Returns editable element for the parameter.
          */
         protected GuiComboBox getParamComboBox(final String param,
                                                final String prefix,
                                                final int width) {
             GuiComboBox paramCb;
             if (DRBD_RES_PARAM_DEV.equals(param)) {
                 final String selectedValue =
                                     getResource().getValue(DRBD_RES_PARAM_DEV);
                 Info defaultValue = null;
                 if (selectedValue == null) {
                     defaultValue = new StringInfo(
                             Tools.getString("ClusterBrowser.SelectBlockDevice"),
                             null);
                 }
                 final Info[] commonBlockDevInfos =
                                         getCommonBlockDevInfos(defaultValue,
                                                                getName());
                 paramCb = new GuiComboBox(selectedValue,
                                           commonBlockDevInfos,
                                           null,
                                           null,
                                           width,
                                           null);
                 blockDeviceParamCb = paramCb;
 
                 paramCb.addListeners(
                     new ItemListener() {
                         public void itemStateChanged(final ItemEvent e) {
                             if (e.getStateChange() == ItemEvent.SELECTED
                                 && fstypeParamCb != null) {
                                 final Thread thread = new Thread(new Runnable() {
                                     public void run() {
                                         final Info item = (Info) e.getItem();
                                         if (item.getStringValue() == null) {
                                             return;
                                         }
                                         final String selectedValue =
                                                     getResource().getValue("fstype");
                                         String createdFs;
                                         if (selectedValue != null) {
                                             createdFs = selectedValue;
                                         } else {
                                             final CommonDeviceInterface cdi =
                                                 (CommonDeviceInterface) item;
                                             createdFs = cdi.getCreatedFs();
                                         }
                                         fstypeParamCb.setValue(createdFs);
                                     }
                                 });
                                 thread.start();
                             }
                         }
                     },
                     null);
 
                 paramComboBoxAdd(param, prefix, paramCb);
             } else if ("fstype".equals(param)) {
                 final String defaultValue =
                             Tools.getString("ClusterBrowser.SelectFilesystem");
                 String selectedValue = getResource().getValue("fstype");
                 paramCb = new GuiComboBox(selectedValue,
                                           getCommonFileSystems(defaultValue),
                                           null,
                                           null,
                                           width,
                                           null);
                 fstypeParamCb = paramCb;
 
                 paramComboBoxAdd(param, prefix, paramCb);
                 paramCb.setEditable(false);
             } else if ("directory".equals(param)) {
                 Object[] items = new Object[commonMountPoints.length + 1];
                 System.arraycopy(commonMountPoints,
                                  0,
                                  items,
                                  1,
                                  commonMountPoints.length);
                 items[0] = new StringInfo(
                             Tools.getString("ClusterBrowser.SelectMountPoint"),
                             null);
                 //for (int i = 0; i < commonMountPoints.length; i++) {
                 //    items[i + 1] = commonMountPoints[i];
                 //}
                 getResource().setPossibleChoices(param, items);
                 final String selectedValue =
                                             getResource().getValue("directory");
                 final String regexp = "^/.*$";
                 paramCb = new GuiComboBox(selectedValue,
                                           items,
                                           null,
                                           regexp,
                                           width,
                                           null);
                 paramComboBoxAdd(param, prefix, paramCb);
                 paramCb.setAlwaysEditable(true);
             } else {
                 paramCb = super.getParamComboBox(param, prefix, width);
             }
             return paramCb;
         }
 
         /**
          * Returns string representation of the filesystem service.
          */
         public String toString() {
             String id = getService().getId();
             if (id == null) {
                 return super.toString(); /* this is for 'new Filesystem' */
             }
 
             final StringBuffer s = new StringBuffer(getName());
             final DrbdResourceInfo dri =
                 drbdDevHash.get(getResource().getValue(DRBD_RES_PARAM_DEV));
             if (dri == null) {
                 id = getResource().getValue(DRBD_RES_PARAM_DEV);
             } else {
                 id = dri.getName();
                 s.delete(0, s.length());
                 s.append("Filesystem / Drbd");
             }
             if (id == null) {
                 id = Tools.getString(
                             "ClusterBrowser.ClusterBlockDevice.Unconfigured");
             }
             s.append(" (" + id + ")");
 
             return s.toString();
         }
 
         /**
          * Adds DrbddiskInfo before the filesysteminfo is added, returns true
          * if something was added.
          */
         public final ServiceInfo addResourceBefore() {
             final DrbdResourceInfo oldDri =
                     drbdDevHash.get(getResource().getValue(DRBD_RES_PARAM_DEV));
             final DrbdResourceInfo newDri =
                     drbdDevHash.get(getComboBoxValue(DRBD_RES_PARAM_DEV));
             if (newDri == null || newDri.equals(oldDri)) {
                 return null;
             }
             boolean oldDrbddisk = false;
             if (getDrbddiskInfo() != null) {
                 oldDrbddisk = true;
             } else {
                 oldDrbddisk = drbddiskIsPreferred;
             }
             if (oldDri != null) {
                 if (oldDrbddisk) {
                     oldDri.removeDrbdDisk(this);
                 } else {
                     oldDri.removeLinbitDrbd(this);
                 }
                 oldDri.setUsedByCRM(false);
                 if (oldDrbddisk) {
                     setDrbddiskInfo(null);
                 } else {
                     setLinbitDrbdInfo(null);
                 }
             }
 
             if (newDri != null) {
                 newDri.setUsedByCRM(true);
                 if (oldDrbddisk) {
                     newDri.addDrbdDisk(this);
                     return getDrbddiskInfo();
                 } else {
                     newDri.addLinbitDrbd(this);
                     return getLinbitDrbdInfo();
                 }
             }
             return null;
         }
 
         /**
          * Returns how much of the filesystem is used.
          */
         public final int getUsed() {
             if (blockDeviceParamCb != null) {
                 final Object value = blockDeviceParamCb.getValue();
                 if (Tools.isStringClass(value)) {
                     // TODO:
                     return -1;
                 }
                 final Info item = (Info) value;
                 if (item.getStringValue() == null) {
                     return -1;
                 }
                 final CommonDeviceInterface cdi = (CommonDeviceInterface) item;
                 return cdi.getUsed();
             }
             return -1;
         }
 
         /**
          * Sets whether the old style drbddisk is preferred.
          */
         public final void setDrbddiskIsPreferred(
                                            final boolean drbddiskIsPreferred) {
             this.drbddiskIsPreferred = drbddiskIsPreferred;
         }
     }
 
     /**
      * DrbddiskInfo class is used for drbddisk heartbeat service that is
      * treated in special way.
      */
     class DrbddiskInfo extends ServiceInfo {
 
         /**
          * Creates new DrbddiskInfo object.
          */
         public DrbddiskInfo(final String name,
                             final ResourceAgent ra) {
             super(name, ra);
         }
 
         /**
          * Creates new DrbddiskInfo object.
          */
         public DrbddiskInfo(final String name,
                             final ResourceAgent ra,
                             final String resourceName) {
             super(name, ra);
             getResource().setValue("1", resourceName);
         }
 
         /**
          * Creates new DrbddiskInfo object.
          */
         public DrbddiskInfo(final String name,
                             final ResourceAgent ra,
                             final String hbId,
                             final Map<String, String> resourceNode) {
             super(name, ra, hbId, resourceNode);
         }
 
         /**
          * Returns string representation of the drbddisk service.
          */
         public String toString() {
             return getName() + " (" + getResource().getValue("1") + ")";
         }
 
         /**
          * Returns resource name / parameter "1".
          */
         public String getResourceName() {
             return getResource().getValue("1");
         }
 
         /**
          * Sets resource name / parameter "1".
          */
         public void setResourceName(final String resourceName) {
             getResource().setValue("1", resourceName);
         }
 
         /**
          * Removes the drbddisk service.
          */
         public void removeMyselfNoConfirm() {
             super.removeMyselfNoConfirm();
             final DrbdResourceInfo dri = drbdResHash.get(getResourceName());
             if (dri != null) {
                 dri.setUsedByCRM(false);
             }
         }
     }
 
     /**
      * linbit::drbd info class is used for drbd pacemaker service that is
      * treated in special way.
      */
     class LinbitDrbdInfo extends ServiceInfo {
         /**
          * Creates new LinbitDrbdInfo object.
          */
         public LinbitDrbdInfo(final String name,
                               final ResourceAgent ra) {
             super(name, ra);
         }
 
         /**
          * Creates new linbit::drbd info object.
          */
         public LinbitDrbdInfo(final String name,
                               final ResourceAgent ra,
                               final String hbId,
                               final Map<String, String> resourceNode) {
             super(name, ra, hbId, resourceNode);
         }
 
         /**
          * Returns string representation of the linbit::drbd service.
          */
         public String toString() {
             final StringBuffer s = new StringBuffer(30);
             final String provider = getResourceAgent().getProvider();
             if (!HB_HEARTBEAT_PROVIDER.equals(provider)
                 && !"".equals(provider)) {
                 s.append(provider);
                 s.append(':');
             }
             s.append(getName());
             final String string = getResource().getValue("drbd_resource");
             if (string == null) {
                 s.insert(0, "new ");
             } else {
                 if (!"".equals(string)) {
                     s.append(" (" + string + ")");
                 }
             }
             return s.toString();
         }
 
         /**
          * Returns resource name.
          */
         public String getResourceName() {
             return getResource().getValue("drbd_resource");
         }
 
         /**
          * Sets resource name.
          */
         public void setResourceName(final String resourceName) {
             getResource().setValue("drbd_resource", resourceName);
         }
 
         /**
          * Removes the linbit::drbd service.
          */
         public void removeMyselfNoConfirm() {
             super.removeMyselfNoConfirm();
             final DrbdResourceInfo dri = drbdResHash.get(getResourceName());
             if (dri != null) {
                 dri.setUsedByCRM(false);
             }
         }
     }
 
     /**
      * GroupInfo class holds data for heartbeat group, that is in some ways
      * like normal service, but it can contain other services.
      */
     class GroupInfo extends ServiceInfo {
         // should extend EditableInfo: TODO
 
         /**
          * Creates new GroupInfo object.
          */
         public GroupInfo(final ResourceAgent ra) {
             super(PM_GROUP_NAME, ra);
         }
 
         /**
          * Returns all group parameters. (empty)
          */
         public String[] getParametersFromXML() {
             return new String[]{};
         }
 
         /**
          * Applies the changes to the group parameters.
          */
         public void apply() {
             final String[] params = getParametersFromXML();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                     idField.setEnabled(false);
                 }
             });
 
             /* add myself to the hash with service name and id as
              * keys */
             removeFromServiceInfoHash(this);
             final String oldHeartbeatId = getService().getHeartbeatId();
             if (oldHeartbeatId != null) {
                 heartbeatIdToServiceInfo.remove(oldHeartbeatId);
                 heartbeatIdList.remove(oldHeartbeatId);
             }
             if (getService().isNew()) {
                 final String id = idField.getStringValue();
                 getService().setId(id);
             }
             setHeartbeatIdLabel();
             addToHeartbeatIdList(this);
             addNameToServiceInfoHash(this);
 
             /*
                 MSG_ADD_GRP group
                         param_id1 param_name1 param_value1
                         param_id2 param_name2 param_value2
                         ...
                         param_idn param_namen param_valuen
             */
             final String heartbeatId = getService().getHeartbeatId();
             if (getService().isNew()) {
                 final String[] parents = heartbeatGraph.getParents(this);
                 final List<Map<String, String>> colAttrsList =        
                                        new ArrayList<Map<String, String>>();
                 final List<Map<String, String>> ordAttrsList =
                                        new ArrayList<Map<String, String>>();
                 for (final String parentId : parents) {
                     final ServiceInfo parentInfo =
                                     heartbeatIdToServiceInfo.get(parentId);
                     final Map<String, String> colAttrs =        
                                        new LinkedHashMap<String, String>();
                     final Map<String, String> ordAttrs =
                                        new LinkedHashMap<String, String>();
                     colAttrs.put("score", "INFINITY");
                     ordAttrs.put("score", "INFINITY");
                     if (getService().isMaster()) {
                         colAttrs.put("with-rsc-role", "Master");
                         ordAttrs.put("first-action", "promote");
                         ordAttrs.put("then-action", "start");
                     }
                     colAttrsList.add(colAttrs);
                     ordAttrsList.add(ordAttrs);
                 }
                 CRM.setOrderAndColocation(getDCHost(),
                                           heartbeatId,
                                           parents,
                                           colAttrsList,
                                           ordAttrsList);
             }
             setLocations(heartbeatId);
             storeComboBoxValues(params);
 
             reload(getNode());
             heartbeatGraph.repaint();
         }
 
         /**
          * Returns the list of services that can be added to the group.
          */
         public List<ResourceAgent> getAddGroupServiceList(final String cl) {
             return crmXML.getServices(cl);
         }
 
         /**
          * Adds service to this group. Adds it in the submenu in the menu tree
          * and initializes it.
          *
          * @param newServiceInfo
          *      service info object of the new service
          */
         public void addGroupServicePanel(final ServiceInfo newServiceInfo) {
             newServiceInfo.getService().setResourceClass(
                     newServiceInfo.getResourceAgent().getResourceClass());
             newServiceInfo.setGroupInfo(this);
 
             addToHeartbeatIdList(newServiceInfo);
 
             final DefaultMutableTreeNode newServiceNode =
                                     new DefaultMutableTreeNode(newServiceInfo);
             newServiceInfo.setNode(newServiceNode);
             getNode().add(newServiceNode);
             reload(getNode());
             reload(newServiceNode);
         }
 
         /**
          * Adds service to this group and creates new service info object.
          */
         public void addGroupServicePanel(final ResourceAgent newRA) {
             ServiceInfo newServiceInfo;
 
             final String name = newRA.getName();
             if (newRA.isFilesystem()) {
                 newServiceInfo = new FilesystemInfo(name, newRA);
             } else if (newRA.isLinbitDrbd()) {
                 newServiceInfo = new LinbitDrbdInfo(name, newRA);
             } else if (newRA.isDrbddisk()) {
                 newServiceInfo = new DrbddiskInfo(name, newRA);
             } else if (newRA.isIPaddr()) {
                 newServiceInfo = new IPaddrInfo(name, newRA);
             } else if (newRA.isGroup()) {
                 Tools.appError("No groups in group allowed");
                 return;
             } else {
                 newServiceInfo = new ServiceInfo(name, newRA);
             }
             addGroupServicePanel(newServiceInfo);
         }
 
         /**
          * Returns on which node this group is running, meaning on which node
          * all the services are running. Null if they running on different
          * nodes or not at all.
          */
         public final List<String> getRunningOnNodes() {
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             final List<String> allNodes = new ArrayList<String>();
             if (resources != null) {
                 for (final String hbId : resources) {
                     final List<String> ns =
                                         clusterStatus.getRunningOnNodes(hbId);
                     if (ns != null) {
                         for (final String n : ns) {
                             if (!allNodes.contains(n)) {
                                 allNodes.add(n);
                             }
                         }
                     }
                 }
             }
             return allNodes;
         }
 
         /**
          * Starts all resources in the group.
          */
         public final void startResource() {
             setUpdated(true);
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             final Host dc = getDCHost();
             if (resources != null) {
                 for (final String hbId : resources) {
                     CRM.startResource(dc, hbId);
                 }
             }
         }
 
         /**
          * Stops all resources in the group.
          */
         public final void stopResource() {
             setUpdated(true);
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             final Host dc = getDCHost();
             if (resources != null) {
                 for (final String hbId : resources) {
                     CRM.stopResource(dc, hbId);
                 }
             }
         }
 
         /**
          * Cleans up all resources in the group.
          */
         public final void cleanupResource() {
             setUpdated(true);
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             if (resources != null) {
                 for (final String hbId : resources) {
                     heartbeatIdToServiceInfo.get(hbId).cleanupResource();
                 }
             }
         }
 
         /**
          * Sets whether the group services are managed.
          */
         public final void setManaged(final boolean isManaged) {
             setUpdated(true);
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             final Host dc = getDCHost();
             if (resources != null) {
                 for (final String hbId : resources) {
                     CRM.setManaged(dc, hbId, isManaged);
                 }
             }
         }
 
         /**
          * Returns items for the group popup.
          */
         public List<UpdatableItem> createPopup() {
             final List<UpdatableItem>items = super.createPopup();
             /* add group service */
             final MyMenu addGroupServiceMenuItem =new MyMenu(
                         Tools.getString("ClusterBrowser.Hb.AddGroupService")) {
                 private static final long serialVersionUID = 1L;
 
                 public void update() {
                     super.update();
 
                     removeAll();
                     for (final String cl : HB_CLASSES) {
                         final MyMenu classItem =
                                             new MyMenu(HB_CLASS_MENU.get(cl));
                         DefaultListModel m = new DefaultListModel();
                         for (final ResourceAgent ra
                                                 : getAddGroupServiceList(cl)) {
                             final MyMenuItem mmi =
                                     new MyMenuItem(ra.getMenuName()) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     if (ra.isLinbitDrbd()
                                         && !linbitDrbdConfirmDialog()) {
                                         return;
                                     }
                                     addGroupServicePanel(ra);
                                     repaint();
                                 }
                             };
                             m.addElement(mmi);
                         }
                         classItem.add(Tools.getScrollingMenu(classItem, m));
                         add(classItem);
                     }
                 }
             };
             items.add(1, (UpdatableItem) addGroupServiceMenuItem);
             registerMenuItem((UpdatableItem) addGroupServiceMenuItem);
 
             return items;
         }
 
         /**
          * Removes this group from the cib.
          */
         public void removeMyself() {
             String desc = Tools.getString(
                             "ClusterBrowser.confirmRemoveGroup.Description");
 
             final StringBuffer services = new StringBuffer();
 
             final Enumeration e = getNode().children();
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                                     (DefaultMutableTreeNode) e.nextElement();
                 final ServiceInfo child = (ServiceInfo) n.getUserObject();
                 services.append(child.toString());
                 if (e.hasMoreElements()) {
                     services.append(", ");
                 }
 
             }
 
             desc  = desc.replaceAll("@GROUP@", "'" + toString() + "'");
             desc  = desc.replaceAll("@SERVICES@", services.toString());
             if (Tools.confirmDialog(
                     Tools.getString("ClusterBrowser.confirmRemoveGroup.Title"),
                     desc,
                     Tools.getString("ClusterBrowser.confirmRemoveGroup.Yes"),
                     Tools.getString("ClusterBrowser.confirmRemoveGroup.No"))) {
                 getService().setRemoved(true);
                 removeMyselfNoConfirm();
             }
             getService().doneRemoving();
             getService().setNew(false);
         }
 
         /**
          * Remove all the services in the group and the group.
          */
         public void removeMyselfNoConfirm() {
             super.removeMyselfNoConfirm();
             if (!getService().isNew()) {
                 CRM.removeResource(
                                 getDCHost(),
                                 null,
                                 getService().getHeartbeatId(),
                                 null, /* group id */
                                 false);
             }
         }
 
         /**
          * Removes the group, but not the services.
          */
         public void removeMyselfNoConfirmFromChild() {
             super.removeMyselfNoConfirm();
         }
 
         /**
          * Returns tool tip for the group vertex.
          */
         public String getToolTipText() {
             final List<String> hostNames = getRunningOnNodes();
             final StringBuffer sb = new StringBuffer(220);
             sb.append("<b>");
             sb.append(toString());
             if (hostNames == null || hostNames.size() == 0) {
                 sb.append(" not running");
             } else if (hostNames.size() == 1) {
                 sb.append(" running on node: ");
             } else {
                 sb.append(" running on nodes: ");
             }
             sb.append(Tools.join(", ", hostNames.toArray(
                                                new String[hostNames.size()])));
             sb.append("</b>");
 
             final Enumeration e = getNode().children();
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                                     (DefaultMutableTreeNode) e.nextElement();
                 final ServiceInfo child = (ServiceInfo) n.getUserObject();
                 sb.append("\n&nbsp;&nbsp;&nbsp;");
                 sb.append(child.getToolTipText());
             }
 
             return sb.toString();
         }
 
         /**
          * Returns whether one of the services on one of the hosts failed.
          */
          public final boolean isOneFailed() {
                 final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
              if (resources != null) {
                  for (final String hbId : resources) {
                      if (heartbeatIdToServiceInfo.get(hbId).isOneFailed()) {
                          return true;
                      }
                  }
              }
              return false;
          }
 
         /**
          * Returns whether one of the services on one of the hosts failed.
          */
          public final boolean isOneFailedCount() {
                 final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
              if (resources != null) {
                  for (final String hbId : resources) {
                      if (heartbeatIdToServiceInfo.get(hbId).isOneFailedCount()) {
                          return true;
                      }
                  }
              }
              return false;
          }
 
         /**
          * Returns whether one of the services failed to start.
          */
          public final boolean isFailed() {
              final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
              if (resources != null) {
                  for (final String hbId : resources) {
                      final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                      if (si == null) {
                          continue;
                      }
                      if (si.isFailed()) {
                          return true;
                      }
                  }
              }
              return false;
          }
 
          /**
           * Returns subtexts that appears in the service vertex.
           */
          public final Subtext[] getSubtextsForGraph() {
              final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
              final List<Subtext> texts = new ArrayList<Subtext>();
              Subtext prevSubtext = null;
              if (resources != null) {
                  for (final String hbId : resources) {
                      final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                      if (si != null) {
                          Subtext[] subtexts = si.getSubtextsForGraph();
                          Subtext sSubtext = null;
                          if (subtexts == null || subtexts.length == 0) {
                              continue;
                          }
                          sSubtext = subtexts[0];
                          if (prevSubtext == null
                              || !sSubtext.getSubtext().equals(
                                                    prevSubtext.getSubtext())) {
                              texts.add(new Subtext(sSubtext.getSubtext() + ":",
                                                    sSubtext.getColor()));
                              prevSubtext = sSubtext;
                          }
                          if (si != null) {
                              String unmanaged = "";
                              if (!si.isManaged()) {
                                  unmanaged = " / unmanaged";
                              }
                              texts.add(new Subtext("   " + si.toString() 
                                                    + unmanaged,
                                                    sSubtext.getColor()));
                              boolean skip = true;
                              for (final Subtext st : subtexts) {
                                  if (skip) {
                                      skip = false;
                                      continue;
                                  }
                                  texts.add(new Subtext("   " + st.getSubtext(),
                                                        st.getColor()));
                              }
                          }
                      }
                  }
              }
              return texts.toArray(new Subtext[texts.size()]);
          }
 
         /**
          * Returns whether all services are unmaneged.
          */
         public final boolean isManaged() {
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             if (resources == null) {
                 return true;
             } else {
                 if (resources.isEmpty()) {
                     return true;
                 }
                 for (final String hbId : resources) {
                     final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                     if (si != null) {
                         if (si.isManaged()) {
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
 
         /**
          * Returns whether all of the services are started.
          */
         public final boolean isStarted() {
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             if (resources != null) {
                 for (final String hbId : resources) {
                     final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                     if (si != null) {
                         if (!si.isStarted()) {
                             return false;
                         }
                     }
                 }
             }
             return true;
         }
 
         /**
          * Returns whether one of the services is stopped.
          */
         public final boolean isStopped() {
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             if (resources != null) {
                 for (final String hbId : resources) {
                     final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                     if (si != null) {
                         if (si.isStopped()) {
                             return true;
                         }
                     }
                 }
             }
             return false;
         }
 
         /**
          * Returns true if all services in the group are running.
          */
         public final boolean isRunning() {
             final List<String> resources = clusterStatus.getGroupResources(
                                                 getService().getHeartbeatId());
             if (resources != null) {
                 for (final String hbId : resources) {
                     final ServiceInfo si = heartbeatIdToServiceInfo.get(hbId);
                     if (si != null) {
                         if (!si.isRunning()) {
                             return false;
                         }
                     }
                 }
             }
             return true;
         }
     }
 
     /**
      * This class holds info data for one hearteat service and allows to enter
      * its arguments and execute operations on it.
      */
     class ServiceInfo extends EditableInfo {
         /** This is a map from host to the combobox with scores. */
         private final Map<HostInfo, GuiComboBox> scoreComboBoxHash =
                                     new HashMap<HostInfo, GuiComboBox>();
         /** A map from host to stored score. */
         private Map<HostInfo, String> savedHostScores =
                                                new HashMap<HostInfo, String>();
         /** A map from operation to the stored value. First key is
          * operation name like "start" and second key is parameter like
          * "timeout". */
         private MultiKeyMap savedOperation = new MultiKeyMap();
         /** A map from operation to its combo box. */
         private MultiKeyMap operationsComboBoxHash = new MultiKeyMap();
         /** idField text field. */
         protected GuiComboBox idField = null;
         /** Cache for the info panel. */
         private JComponent infoPanel = null;
         /** Group info object of the group this service is in or null, if it is
          * not in any group. */
         private GroupInfo groupInfo = null;
         /** Master/Slave info object, if is null, it is not master/slave
          * resource. */
         private CloneInfo cloneInfo = null;
         /** ResourceAgent object of the service, with name, ocf informations
          * etc. */
         private final ResourceAgent resourceAgent;
         /** Heartbeat id label */
         private JLabel heartbeatIdLabel = null;
         /** Radio buttons for clone/master/slave primitive resources. */
         private GuiComboBox typeRadioGroup;
 
         /**
          * Prepares a new <code>ServiceInfo</code> object and creates
          * new service object.
          */
         public ServiceInfo(final String name,
                            final ResourceAgent resourceAgent) {
             super(name);
             this.resourceAgent = resourceAgent;
             setResource(new Service(name));
 
             /* init save button */
             initApplyButton();
             /* add item listeners to the apply button. */
             applyButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         final Thread thread = new Thread(
                             new Runnable() {
                                 public void run() {
                                     clStatusLock();
                                     apply();
                                     clStatusUnlock();
                                 }
                             }
                         );
                         thread.start();
                     }
                 }
             );
 
 
             getService().setNew(true);
         }
 
         /**
          * Prepares a new <code>ServiceInfo</code> object and creates
          * new service object. It also initializes parameters along with
          * heartbeat id with values from xml stored in resourceNode.
          */
         public ServiceInfo(final String name,
                            final ResourceAgent ra,
                            final String heartbeatId,
                            final Map<String, String> resourceNode) {
             this(name, ra);
             getService().setHeartbeatId(heartbeatId);
             setParameters(resourceNode);
         }
 
         /**
          * Returns id of the service, which is heartbeatId.
          */
         public String getId() {
             return getService().getHeartbeatId();
         }
 
         /**
          * Sets info panel of the service.
          */
         public void setInfoPanel(final JPanel infoPanel) {
             this.infoPanel = infoPanel;
         }
 
         /**
          * Returns true if the node is active.
          */
         public boolean isActiveNode(final String node) {
             return clusterStatus.isActiveNode(node);
         }
 
         /**
          * Returns whether all the parameters are correct. If param is null,
          * all paremeters will be checked, otherwise only the param, but other
          * parameters will be checked only in the cache. This is good if only
          * one value is changed and we don't want to check everything.
          */
         public boolean checkResourceFieldsCorrect(final String param,
                                                   final String[] params) {
             boolean ret = true;
             if (cloneInfo != null
                 && !cloneInfo.checkResourceFieldsCorrect(
                                      param,
                                      cloneInfo.getParametersFromXML())) {
                 /* the next super checkResourceFieldsCorrect must be run at
                   least once. */
                 ret = false;
             }
             if (!super.checkResourceFieldsCorrect(param, params)) {
                 return false;
             }
             if (!ret) {
                 return false;
             }
             if (idField == null) {
                 return false;
             }
             final String id = idField.getStringValue();
             // TODO: check uniq id
             if (id == null || id.equals("")) {
                 return false;
             }
             return true;
         }
 
         /**
          * Returns whether the specified parameter or any of the parameters
          * have changed. If param is null, only param will be checked,
          * otherwise all parameters will be checked.
          */
         public boolean checkResourceFieldsChanged(final String param,
                                                   final String[] params) {
             boolean ret;
             if (super.checkResourceFieldsChanged(param, params)) {
                 ret = true;
             } else if (cloneInfo != null
                        && cloneInfo.checkResourceFieldsChanged(
                                            param,
                                            cloneInfo.getParametersFromXML())) {
                 ret = true;
             } else {
                 final String id = idField.getStringValue();
                 final String heartbeatId = getService().getHeartbeatId();
                 if (PM_GROUP_NAME.equals(getName())) {
                     if (heartbeatId.equals(Service.GRP_ID_PREFIX + id)
                         || heartbeatId.equals(id)) {
                         ret = checkHostScoreFieldsChanged() || checkOperationFieldsChanged();
                     } else {
                         ret = true;
                     }
                 } else if (PM_CLONE_SET_NAME.equals(getName())
                            || PM_MASTER_SLAVE_SET_NAME.equals(getName())) {
                     String prefix;
                     if (getService().isMaster()) {
                         prefix = Service.MS_ID_PREFIX;
                     } else {
                         prefix = Service.CL_ID_PREFIX;
                     }
                     if (heartbeatId.equals(prefix + id)
                         || heartbeatId.equals(id)) {
                         ret = checkHostScoreFieldsChanged();
                     } else {
                         ret = true;
                     }
                 } else {
                     if (heartbeatId.equals(
                                  Service.RES_ID_PREFIX + getName() + "_" + id)
                         || heartbeatId.equals(id)) {
                         ret = checkHostScoreFieldsChanged() || checkOperationFieldsChanged();
                     } else {
                         ret = true;
                     }
                 }
             }
             final String cl = getService().getResourceClass();
             if (cl != null && cl.equals(HB_HEARTBEAT_CLASS)) {
                 /* in old style resources don't show all the textfields */
                 boolean visible = false;
                 GuiComboBox cb = null;
                 for (int i = params.length - 1; i >= 0; i--) {
                     final GuiComboBox prevCb = paramComboBoxGet(params[i],
                                                                 null);
                     if (prevCb == null) {
                         continue;
                     }
                     if (!visible && !prevCb.getStringValue().equals("")) {
                         visible = true;
                     }
                     if (cb != null && cb.isVisible() != visible) {
                         final boolean v = visible;
                         final GuiComboBox c = cb;
                         SwingUtilities.invokeLater(
                             new Runnable() {
                                 public void run() {
                                     c.setVisible(v);
                                     getLabel(c).setVisible(v);
                                 }
                             });
                     }
                     cb = prevCb;
                 }
             }
             return ret;
         }
 
         /**
          * Sets service parameters with values from resourceNode hash.
          */
         public void setParameters(final Map<String, String> resourceNode) {
             if (crmXML == null) {
                 Tools.appError("crmXML is null");
                 return;
             }
             final String[] params = crmXML.getParameters(resourceAgent);
             if (params != null) {
                 for (String param : params) {
                     String value = resourceNode.get(param);
                     if (value == null) {
                         value = getParamDefault(param);
                     }
                     if (value == null) {
                         value = "";
                     }
                     final String oldValue = getResource().getValue(param);
                     if ((value == null && value != oldValue)
                         || (value != null && !value.equals(oldValue))) {
                         getResource().setValue(param, value);
                         if (infoPanel != null) {
                             final GuiComboBox cb = paramComboBoxGet(param,
                                                                     null);
                             if (cb != null) {
                                 cb.setValue(value);
                             }
                         }
                     }
                 }
             }
 
             /* set scores */
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 String score = clusterStatus.getScore(
                                             getService().getHeartbeatId(),
                                             hi.getName());
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 final String savedScore = savedHostScores.get(hi);
                 if ((score == null && score != savedScore)
                      || (score != null
                          && !score.equals(savedScore))) {
                     if (score == null) {
                         savedHostScores.remove(hi);
                     } else {
                         savedHostScores.put(hi, score);
                     }
                     if (cb != null) {
                         cb.setValue(score);
                     }
                 }
             }
 
             /* set operations */
             for (final String op : HB_OPERATIONS) {
                 for (final String param : HB_OPERATION_PARAMS.get(op)) {
                     String defaultValue =
                                  resourceAgent.getOperationDefault(op, param);
                     if (defaultValue == null) {
                         continue;
                     }
                     final GuiComboBox cb =
                             (GuiComboBox) operationsComboBoxHash.get(op, param);
                     String value = clusterStatus.getOperation(
                                                 getService().getHeartbeatId(),
                                                 op,
                                                 param);
                     if (value == null) {
                         value = "";
                     }
                     if (!value.equals(savedOperation.get(op, param))) {
                         savedOperation.put(op, param, value);
                         if (cb != null && value != null) {
                             cb.setValue(value);
                         }
                     }
                 }
             }
             getService().setAvailable();
         }
 
         /**
          * Returns the main text that appears in the graph.
          */
         public String getMainTextForGraph() {
             return toString();
         }
 
         /**
          * Returns a name of the service with id in the parentheses.
          * It adds prefix 'new' if id is null.
          */
         public String toString() {
             final StringBuffer s = new StringBuffer(30);
             final String provider = resourceAgent.getProvider();
             if (!HB_HEARTBEAT_PROVIDER.equals(provider)
                 && !"".equals(provider)) {
                 s.append(':');
                 s.append(resourceAgent.getProvider());
             }
             s.append(getName());
             String string = getService().getId();
 
             /* 'string' contains the last string if there are more dependent
              * resources, although there is usually only one. */
             if (string == null) {
                 s.insert(0, "new ");
             } else {
                 if (!"".equals(string)) {
                     s.append(" (" + string + ")");
                 }
             }
             return s.toString();
         }
 
         /**
          * Sets id in the service object.
          */
         public void setId(final String id) {
             getService().setId(id);
         }
 
         /**
          * Returns node name of the host where this service is running.
          */
         public List<String> getRunningOnNodes() {
             return clusterStatus.getRunningOnNodes(
                                                 getService().getHeartbeatId());
         }
 
        /**
         * Returns whether service is started.
         */
         public boolean isStarted() {
             final String hbV = getDCHost().getHeartbeatVersion();
             String targetRoleString = "target-role";
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 targetRoleString = "target_role";
             }
             String targetRole =
                 clusterStatus.getParameter(getService().getHeartbeatId(),
                                              targetRoleString);
             if (targetRole == null) {
                 targetRole = getParamDefault(targetRoleString);
             }
             if (!"stopped".equals(targetRole)) {
                 return true;
             }
             return false;
         }
 
        /**
         * Returns whether service is stopped.
         */
         public boolean isStopped() {
             final String hbV = getDCHost().getHeartbeatVersion();
             String targetRoleString = "target-role";
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 targetRoleString = "target_role";
             }
             String targetRole =
                 clusterStatus.getParameter(getService().getHeartbeatId(),
                                            targetRoleString);
             if (targetRole == null) {
                 targetRole = getParamDefault(targetRoleString);
             }
             if ("stopped".equals(targetRole)) {
                 return true;
             }
             return false;
         }
 
         /**
          * Returns whether service is managed.
          * TODO: "default" value
          */
         public boolean isManaged() {
             final String hbV = getDCHost().getHeartbeatVersion();
             String isManagedString = "is-managed";
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 isManagedString = "is_managed";
             }
             String isManaged =
                 clusterStatus.getParameter(getService().getHeartbeatId(),
                                              isManagedString);
             if (isManaged == null) {
                 isManaged = getParamDefault(isManagedString);
             }
             if ("true".equals(isManaged)) {
                 return true;
             }
             return false;
         }
 
         /**
          * Returns whether the service is running.
          */
         public boolean isRunning() {
             final List<String> runningOnNodes = getRunningOnNodes();
             return runningOnNodes != null && !runningOnNodes.isEmpty();
         }
 
         /**
          * Returns fail count string that appears in the graph.
          */
         private String getFailCountString(final String hostName) {
             String fcString = "";
             final String failCount = getFailCount(hostName);
             if (failCount != null) {
                 if ("INFINITY".equals(failCount)) {
                     fcString = " failed";
                 } else {
                     fcString = " failed: " + failCount;
                 }
             }
             return fcString;
         }
 
 
         /**
          * Returns fail count.
          */
         protected String getFailCount(final String hostName) {
              return clusterStatus.getFailCount(
                                                 hostName,
                                                 getService().getHeartbeatId());
         }
 
         /**
          * Returns whether the resource failed on the specified host.
          */
         protected final boolean failedOnHost(final String hostName) {
             final String failCount = getFailCount(hostName);
             return failCount != null && "INFINITY".equals(failCount);
         }
 
         /**
          * Returns whether the resource has failed to start.
          */
         public boolean isFailed() {
             for (final Host host : getClusterHosts()) {
                 if (host.isClStatus() && failedOnHost(host.getName())) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * Returns whether the resource has failed on one of the nodes.
          */
         public boolean isOneFailed() {
             for (final Host host : getClusterHosts()) {
                 if (failedOnHost(host.getName())) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * Returns whether the resource has fail-count on one of the nodes.
          */
         public boolean isOneFailedCount() {
             for (final Host host : getClusterHosts()) {
                 if (getFailCount(host.getName()) != null) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * Sets whether the service is managed.
          */
         public void setManaged(final boolean isManaged) {
             setUpdated(true);
             CRM.setManaged(getDCHost(),
                            getService().getHeartbeatId(),
                            isManaged);
         }
 
         /**
          * Returns color for the host vertex.
          */
         public List<Color> getHostColors() {
             return cluster.getHostColors(getRunningOnNodes());
         }
 
         /**
          * Returns service icon in the menu. It can be started or stopped.
          * TODO: broken icon, not managed icon.
          */
         public ImageIcon getMenuIcon() {
             if (allHostsDown() || isStopped()) {
                 return SERVICE_STOPPED_ICON;
             }
             return SERVICE_STARTED_ICON;
         }
 
         /**
          * Gets saved host scores.
          */
         public Map<HostInfo, String> getSavedHostScores() {
             return savedHostScores;
         }
 
         /**
          * Returns list of all host names in this cluster.
          */
         public List<String> getHostNames() {
             final List<String> hostNames = new ArrayList<String>();
             final Enumeration e = clusterHostsNode.children();
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                                     (DefaultMutableTreeNode) e.nextElement();
                 final String hostName =
                                     ((HostInfo) n.getUserObject()).getName();
                 hostNames.add(hostName);
             }
             return hostNames;
         }
 
         /**
          * TODO: wrong doku
          * Converts enumeration to the info array, get objects from
          * hash if they exist.
          */
         protected Info[] enumToInfoArray(final Info defaultValue,
                                          final String serviceName,
                                          final Enumeration e) {
             final List<Info> list = new ArrayList<Info>();
             if (defaultValue != null) {
                 list.add(defaultValue);
             }
 
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                                       (DefaultMutableTreeNode) e.nextElement();
                 final Info i =(Info) n.getUserObject();
                 final String name = i.getName();
                 final ServiceInfo si = getServiceInfoFromId(serviceName,
                                                             i.getName());
 
                 if (si == null && !name.equals(defaultValue)) {
                     list.add(i);
                 }
             }
             return list.toArray(new Info[list.size()]);
         }
 
         /**
          * Stores scores for host.
          */
         private void storeHostScores() {
             savedHostScores.clear();
             for (final Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 final String score = cb.getStringValue();
                 if (score == null || "".equals(score)) {
                     savedHostScores.remove(hi);
                 } else {
                     savedHostScores.put(hi, score);
                 }
             }
             //// TODO: rename this
             //heartbeatGraph.setHomeNode(this, savedHostScores);
         }
 
         /**
          * Returns thrue if an operation field changed.
          */
         private boolean checkOperationFieldsChanged() {
             boolean changed = false;
             for (final String op : HB_OPERATIONS) {
                 for (final String param : HB_OPERATION_PARAMS.get(op)) {
                     String defaultValue =
                                  resourceAgent.getOperationDefault(op, param);
                     if (defaultValue == null) {
                         continue;
                     }
                     final GuiComboBox cb =
                             (GuiComboBox) operationsComboBoxHash.get(op, param);
                     if (cb == null) {
                         return false;
                     }
                     final String value = cb.getStringValue();
                     final String savedOp =
                                         (String) savedOperation.get(op, param);
                     if (savedOp == null) {
                         if (value != null && !value.equals(defaultValue)) {
                             changed = true;
                         }
                     } else if (!savedOp.equals(value)) {
                         changed = true;
                     }
                 }
             }
             return changed;
         }
 
         /**
          * Returns true if some of the scores have changed.
          */
         private boolean checkHostScoreFieldsChanged() {
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 final String hsSaved = savedHostScores.get(hi);
                 if (cb == null) {
                     return false;
                 }
                 final String hs = cb.getStringValue();
                 if (hsSaved == null && !"".equals(hs)) {
                     return true;
                 } else if (hsSaved != null && !hs.equals(hsSaved)) {
                     return true;
                 }
             }
             return false;
         }
 
         /**
          * Returns the list of all services, that can be used in the 'add
          * service' action.
          */
         public List<ResourceAgent> getAddServiceList(final String cl) {
             return globalGetAddServiceList(cl);
         }
 
         /**
          * Returns 'existing service' list for graph popup menu.
          */
         public List<ServiceInfo> getExistingServiceList(final ServiceInfo p) {
             final List<ServiceInfo> existingServiceList =
                                                    new ArrayList<ServiceInfo>();
             for (final String name : nameToServiceInfoHash.keySet()) {
                 final Map<String, ServiceInfo> idHash =
                                                 nameToServiceInfoHash.get(name);
                 for (final String id : idHash.keySet()) {
                     final ServiceInfo si = idHash.get(id);
                     if (!heartbeatGraph.existsInThePath(si, p)) {
                         existingServiceList.add(si);
                     }
                 }
             }
             return existingServiceList;
         }
 
         /**
          * Returns info object of all block devices on all hosts that have the
          * same names and other attributes.
          */
         Info[] getCommonBlockDevInfos(final Info defaultValue,
                                       final String serviceName) {
             final List<Info> list = new ArrayList<Info>();
 
             /* drbd resources */
             final Enumeration drbdResources = drbdNode.children();
 
             if (defaultValue != null) {
                 list.add(defaultValue);
             }
             while (drbdResources.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                           (DefaultMutableTreeNode) drbdResources.nextElement();
                 final CommonDeviceInterface drbdRes =
                                     (CommonDeviceInterface) n.getUserObject();
                 list.add((Info) drbdRes);
             }
 
             /* block devices that are the same on all hosts */
             final Enumeration cbds = commonBlockDevicesNode.children();
             while (cbds.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                                    (DefaultMutableTreeNode) cbds.nextElement();
                 final CommonDeviceInterface cbd =
                                     (CommonDeviceInterface) n.getUserObject();
                 list.add((Info) cbd);
             }
 
             return list.toArray(new Info[list.size()]);
         }
 
         /**
          * Selects the node in the menu and reloads everything underneath.
          */
         public void selectMyself() {
             super.selectMyself();
             nodeChanged(getNode());
         }
 
         /**
          * Creates id field with id as default value.
          */
         public final GuiComboBox createIdField(final int rightWidth) {
             final String id = getService().getId();
             final String regexp = "^[\\w-]+$";
             idField = new GuiComboBox(id, null, null, regexp, rightWidth, null);
             idField.setValue(id);
             final String[] params = getParametersFromXML();
             idField.getDocument().addDocumentListener(
                 new DocumentListener() {
                     private void check() {
                         Thread thread = new Thread(new Runnable() {
                             public void run() {
                                 final boolean enable =
                                           checkResourceFields("id",
                                                               params);
                                 SwingUtilities.invokeLater(
                                 new Runnable() {
                                     public void run() {
                                         applyButton.setEnabled(enable);
                                     }
                                 });
                             }
                         });
                         thread.start();
                     }
 
                     public void insertUpdate(final DocumentEvent e) {
                         check();
                     }
 
                     public void removeUpdate(final DocumentEvent e) {
                         check();
                     }
 
                     public void changedUpdate(final DocumentEvent e) {
                         check();
                     }
                 }
             );
             paramComboBoxAdd("id", null, idField);
             if (!getService().isNew()) {
                 idField.setEnabled(false);
             }
             return idField;
         }
 
         /**
          * Creates id text field with label and adds it to the panel.
          */
         protected void addIdField(final JPanel optionsPanel,
                                   final int leftWidth,
                                   final int rightWidth) {
             final JPanel panel = getParamPanel("ID");
             int rows = 1;
             createIdField(rightWidth);
             addField(panel, new JLabel("ID"), idField, leftWidth, rightWidth);
             SpringUtilities.makeCompactGrid(panel, rows, 2, /* rows, cols */
                                             1, 1,           /* initX, initY */
                                             1, 1);          /* xPad, yPad */
             optionsPanel.add(panel);
         }
 
         /**
          * Sets value for id field.
          */
         protected void setIdField(final String id) {
             idField.setValue(id);
         }
 
         /**
          * Adds clone fields to the option pane.
          */
         protected void addCloneFields(final JPanel optionsPanel,
                                             final JPanel extraOptionsPanel,
                                             final int leftWidth,
                                             final int rightWidth) {
             String title = "Clone Set";
             if (cloneInfo.getService().isMaster()) {
                 title = "Master/Slave Set";
             }
             final JPanel panel = getParamPanel(title);
             final GuiComboBox msIdField = cloneInfo.createIdField(rightWidth);
             addField(panel,
                      new JLabel("M/S Set ID"),
                      msIdField,
                      leftWidth,
                      rightWidth);
             cloneInfo.createNewHeartbeatIdLabel();
             addField(panel,
                      new JLabel(Tools.getString("ClusterBrowser.HeartbeatId")),
                      cloneInfo.getHeartbeatIdLabel(),
                      leftWidth,
                      rightWidth);
             SpringUtilities.makeCompactGrid(panel, 2, 2, /* rows, cols */
                                                    1, 1, /* initX, initY */
                                                    1, 1);/* xPad, yPad */
             optionsPanel.add(panel);
 
             cloneInfo.paramComboBoxClear();
 
             final String[] params = cloneInfo.getParametersFromXML();
             cloneInfo.addParams(optionsPanel,
                                 extraOptionsPanel,
                                 params,
                                 SERVICE_LABEL_WIDTH,
                                 SERVICE_FIELD_WIDTH);
 
             cloneInfo.addHostScores(optionsPanel,
                                     SERVICE_LABEL_WIDTH,
                                     SERVICE_FIELD_WIDTH);
         }
 
 
         /**
          * Creates heartbeat id and group text field with label and adds them
          * to the panel.
          */
         protected void addHeartbeatFields(final JPanel optionsPanel,
                                           final int leftWidth,
                                           final int rightWidth) {
             /* heartbeat id */
             createNewHeartbeatIdLabel();
             final JPanel panel = getParamPanel("Heartbeat");
             addField(panel,
                      new JLabel(Tools.getString("ClusterBrowser.HeartbeatId")),
                      heartbeatIdLabel,
                      leftWidth,
                      rightWidth);
             /* heartbeat provider */
             final JLabel heartbeatProviderLabel =
                                  new JLabel(resourceAgent.getProvider());
             addField(panel,
                      new JLabel(Tools.getString(
                                           "ClusterBrowser.HeartbeatProvider")),
                      heartbeatProviderLabel,
                      leftWidth,
                      rightWidth);
             /* heartbeat class */
             final JLabel resourceClassLabel =
                                  new JLabel(getService().getResourceClass());
             addField(panel,
                      new JLabel(Tools.getString(
                                              "ClusterBrowser.ResourceClass")),
                      resourceClassLabel,
                      leftWidth,
                      rightWidth);
             int rows = 3;
 
             if (groupInfo != null) {
                 final String groupId = groupInfo.getService().getHeartbeatId();
                 final JLabel groupLabel = new JLabel(groupId);
                 addField(panel,
                          new JLabel(Tools.getString("ClusterBrowser.Group")),
                          groupLabel,
                          leftWidth,
                          rightWidth);
                 rows++;
             }
             SpringUtilities.makeCompactGrid(panel, rows, 2, /* rows, cols */
                                             1, 1,           /* initX, initY */
                                             1, 1);          /* xPad, yPad */
             optionsPanel.add(panel);
         }
 
         /**
          * Creates host score combo boxes with labels, one per host.
          */
         protected void addHostScores(final JPanel optionsPanel,
                                      final int leftWidth,
                                      final int rightWidth) {
             int rows = 0;
             scoreComboBoxHash.clear();
 
             final JPanel panel =
                    getParamPanel(Tools.getString("ClusterBrowser.HostScores"));
 
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final Map<String, String> abbreviations =
                                                  new HashMap<String, String>();
                 abbreviations.put("i", "INFINITY");
                 abbreviations.put("I", "INFINITY");
                 final GuiComboBox cb =
                     new GuiComboBox(null,
                                     new String[]{null,
                                                  "0",
                                                  "2",
                                                  "INFINITY",
                                                  "-INFINITY"},
                                     null,
                                     null,
                                     "^(-?(\\d*|INFINITY))|@NOTHING_SELECTED@$",
                                     rightWidth,
                                     abbreviations);
                 cb.setEditable(true);
                 scoreComboBoxHash.put(hi, cb);
 
                 /* set selected host scores in the combo box from
                  * savedHostScores */
                 final String hsSaved = savedHostScores.get(hi);
                 cb.setValue(hsSaved);
             }
 
             /* host score combo boxes */
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 addField(panel,
                          new JLabel("on " + hi.getName()),
                          cb,
                          leftWidth,
                          rightWidth);
                 rows++;
             }
 
             SpringUtilities.makeCompactGrid(panel, rows, 2, /* rows, cols */
                                             1, 1,           /* initX, initY */
                                             1, 1);          /* xPad, yPad */
             optionsPanel.add(panel);
         }
 
         /**
          * Creates operations combo boxes with labels,
          */
         protected void addOperations(final JPanel optionsPanel,
                                      final JPanel extraOptionsPanel,
                                      final int leftWidth,
                                      final int rightWidth) {
             int rows = 0;
             int extraRows = 0;
             operationsComboBoxHash.clear();
 
             final JPanel panel = getParamPanel(
                                 Tools.getString("ClusterBrowser.Operations"));
             final JPanel extraPanel = getParamPanel(
                         Tools.getString("ClusterBrowser.AdvancedOperations"),
                         EXTRA_PANEL_BACKGROUND
                         );
 
             for (final String op : HB_OPERATIONS) {
                 for (final String param : HB_OPERATION_PARAMS.get(op)) {
                     String defaultValue =
                                   resourceAgent.getOperationDefault(op, param);
                     if (defaultValue == null) {
                         continue;
                     }
                     GuiComboBox.Type type;
                     final String regexp = "^-?\\d*$";
                     type = GuiComboBox.Type.TEXTFIELDWITHUNIT;
                     // TODO: old style resources
                     if (defaultValue == null) {
                         defaultValue = "0";
                     }
                     final GuiComboBox cb = new GuiComboBox(defaultValue,
                                                            null,
                                                            getUnits(),
                                                            type,
                                                            regexp,
                                                            rightWidth,
                                                            null);
 
                     operationsComboBoxHash.put(op, param, cb);
                     final String savedValue =
                                         (String) savedOperation.get(op, param);
                     if (savedValue != null) {
                         cb.setValue(savedValue);
                     }
 
                     JPanel p;
                     if (HB_OP_BASIC.contains(op)) {
                         p = panel;
                         rows++;
                     } else {
                         p = extraPanel;
                         extraRows++;
                     }
 
                     addField(p,
                              new JLabel(Tools.ucfirst(op)
                                         + " / " + Tools.ucfirst(param)),
                              cb,
                              leftWidth,
                              rightWidth);
                 }
             }
 
             SpringUtilities.makeCompactGrid(panel, rows, 2, /* rows, cols */
                                             1, 1,           /* initX, initY */
                                             1, 1);          /* xPad, yPad */
             SpringUtilities.makeCompactGrid(extraPanel, extraRows, 2,
                                             1, 1,
                                             1, 1);
             optionsPanel.add(panel);
             extraOptionsPanel.add(extraPanel);
         }
 
         /**
          * Returns parameters.
          */
         public String[] getParametersFromXML() {
             return crmXML.getParameters(resourceAgent);
         }
 
         /**
          * Returns true if the value of the parameter is ok.
          */
         protected boolean checkParam(final String param,
                                      final String newValue) {
             if (param.equals("ip")
                 && newValue != null
                 && !Tools.checkIp(newValue)) {
                 return false;
             }
             return crmXML.checkParam(resourceAgent,
                                      param,
                                      newValue);
         }
 
         /**
          * Returns default value for specified parameter.
          */
         protected String getParamDefault(final String param) {
             return crmXML.getParamDefault(resourceAgent, param);
         }
 
         /**
          * Returns preferred value for specified parameter.
          */
         protected String getParamPreferred(final String param) {
             return crmXML.getParamPreferred(resourceAgent, param);
         }
 
         /**
          * Returns possible choices for drop down lists.
          */
         protected Object[] getParamPossibleChoices(final String param) {
             if (isCheckBox(param)) {
                 return crmXML.getCheckBoxChoices(resourceAgent, param);
             } else {
                 // TODO: this does nothing, I think
                 return crmXML.getParamPossibleChoices(resourceAgent, param);
             }
         }
 
         /**
          * Returns short description of the specified parameter.
          */
         protected String getParamShortDesc(final String param) {
             return crmXML.getParamShortDesc(resourceAgent, param);
         }
 
         /**
          * Returns long description of the specified parameter.
          */
         protected String getParamLongDesc(final String param) {
             return crmXML.getParamLongDesc(resourceAgent, param);
         }
 
         /**
          * Returns section to which the specified parameter belongs.
          */
         protected String getSection(final String param) {
             return crmXML.getSection(resourceAgent, param);
         }
 
         /**
          * Returns true if the specified parameter is required.
          */
         protected boolean isRequired(final String param) {
             return crmXML.isRequired(resourceAgent, param);
         }
 
         /**
          * Returns true if the specified parameter is meta attribute.
          */
         protected boolean isMetaAttr(final String param) {
             return crmXML.isMetaAttr(resourceAgent, param);
         }
 
         /**
          * Returns true if the specified parameter is integer.
          */
         protected boolean isInteger(final String param) {
             return crmXML.isInteger(resourceAgent, param);
         }
 
         /**
          * Returns true if the specified parameter is of time type.
          */
         protected boolean isTimeType(final String param) {
             return crmXML.isTimeType(resourceAgent, param);
         }
 
         /**
          * Returns whether parameter is checkbox.
          */
         protected boolean isCheckBox(final String param) {
             return crmXML.isBoolean(resourceAgent, param);
         }
 
         /**
          * Returns the type of the parameter according to the OCF.
          */
         protected String getParamType(final String param) {
             return crmXML.getParamType(resourceAgent, param);
         }
 
         /**
          * Is called before the service is added. This is for example used by
          * FilesystemInfo so that it can add LinbitDrbdInfo or DrbddiskInfo before
          * it adds itself.
          */
         public ServiceInfo addResourceBefore() {
             return null;
         }
 
         /**
          * Change type to Master, Clone or Primitive.
          */
         protected final void changeType(final String value) {
             boolean masterSlave = false;
             boolean clone = false;
             if ("Master / Slave".equals(value)) {
                 masterSlave = true;
                 clone = true;
             } else if ("Clone".equals(value)) {
                 clone = true;
             }
 
             if (clone) {
                 final CloneInfo oldCI = cloneInfo;
                 cloneInfo = new CloneInfo(crmXML.getHbClone(),
                                           PM_MASTER_SLAVE_SET_NAME,
                                           masterSlave);
                 if (oldCI == null) {
                     heartbeatGraph.exchangeObjectInTheVertex(cloneInfo, this);
                 } else {
                     servicesNode.remove(oldCI.getNode());
                     heartbeatGraph.exchangeObjectInTheVertex(cloneInfo, oldCI);
                 } 
                 cloneInfo.setCloneServicePanel(this);
                 infoPanel = null;
                 selectMyself();
             } else if ("Primitive".equals(value)) {
                 cloneInfo.getNode().remove(getNode());
                 servicesNode.remove(cloneInfo.getNode());
                 servicesNode.add(getNode());
                 heartbeatGraph.exchangeObjectInTheVertex(this, cloneInfo);
                 heartbeatIdToServiceInfo.remove(
                                     cloneInfo.getService().getHeartbeatId());
                 heartbeatIdList.remove(cloneInfo.getService().getHeartbeatId());
                 removeFromServiceInfoHash(cloneInfo);
                 cloneInfo = null;
                 infoPanel = null;
                 selectMyself();
             }
         }
 
         /**
          * Adds host score listeners.
          */
         protected void addHostScoreListeners() {
             final String[] params = getParametersFromXML();
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 cb.addListeners(
                     new ItemListener() {
                         public void itemStateChanged(final ItemEvent e) {
                             if (cb.isCheckBox()
                                 || e.getStateChange() == ItemEvent.SELECTED) {
                                 final Thread thread = new Thread(
                                                               new Runnable() {
                                     public void run() {
                                         final boolean enable =
                                           checkResourceFields("cached", params);
                                         SwingUtilities.invokeLater(
                                         new Runnable() {
                                             public void run() {
                                                 cb.setEditable();
                                                 applyButton.setEnabled(enable);
                                             }
                                         });
                                     }
                                 });
                                 thread.start();
                             }
                         }
                     },
 
                     new DocumentListener() {
                         private void check() {
                             Thread thread = new Thread(new Runnable() {
                                 public void run() {
                                     final boolean enable =
                                         checkResourceFields("cached", params);
                                     SwingUtilities.invokeLater(
                                     new Runnable() {
                                         public void run() {
                                             applyButton.setEnabled(enable);
                                         }
                                     });
                                 }
                             });
                             thread.start();
                         }
 
                         public void insertUpdate(final DocumentEvent e) {
                             check();
                         }
 
                         public void removeUpdate(final DocumentEvent e) {
                             check();
                         }
 
                         public void changedUpdate(final DocumentEvent e) {
                             check();
                         }
                     }
                 );
             }
         }
 
         /**
          * Returns info panel with comboboxes for service parameters.
          */
         public JComponent getInfoPanel() {
             if (cloneInfo == null) {
                 heartbeatGraph.pickInfo(this);
             } else {
                 cloneInfo.applyButton = applyButton;
                 heartbeatGraph.pickInfo(cloneInfo);
             }
             if (infoPanel != null) {
                 return infoPanel;
             }
             /* main, button and options panels */
             final JPanel mainPanel = new JPanel();
             mainPanel.setBackground(PANEL_BACKGROUND);
             mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 
             final JPanel buttonPanel = new JPanel(new BorderLayout());
             buttonPanel.setBackground(STATUS_BACKGROUND);
             buttonPanel.setMinimumSize(new Dimension(0, 50));
             buttonPanel.setPreferredSize(new Dimension(0, 50));
             buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
 
             final JPanel optionsPanel = new JPanel();
             optionsPanel.setBackground(PANEL_BACKGROUND);
             optionsPanel.setLayout(new BoxLayout(optionsPanel,
                                                  BoxLayout.Y_AXIS));
             optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             final JPanel extraOptionsPanel = new JPanel();
             extraOptionsPanel.setBackground(EXTRA_PANEL_BACKGROUND);
             extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel,
                                         BoxLayout.Y_AXIS));
             extraOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             /* Actions */
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             JMenu serviceCombo;
             if (cloneInfo == null) {
                 serviceCombo = getActionsMenu();
                 updateMenus(null);
             } else {
                 serviceCombo = cloneInfo.getActionsMenu();
                 cloneInfo.updateMenus(null);
             }
             mb.add(serviceCombo);
             buttonPanel.add(mb, BorderLayout.EAST);
             String defaultValue = "Primitive";
             if (cloneInfo != null) {
                 if (cloneInfo.getService().isMaster()) {
                     defaultValue = "Master / Slave";
                 } else {
                     defaultValue = "Clone";
                 }
             }
             if (!getResourceAgent().isClone() &&
                 !getResourceAgent().isGroup()) {
                 final ServiceInfo thisClass = this;
                 typeRadioGroup = new GuiComboBox(defaultValue,
                                                  new String[]{"Primitive",
                                                               "Clone",
                                                               "Master / Slave"},
                                                  null,
                                                  GuiComboBox.Type.RADIOGROUP,
                                                  null,
                                                  SERVICE_LABEL_WIDTH
                                                  + SERVICE_FIELD_WIDTH,
                                                  null);
                 typeRadioGroup.setBackgroundColor(PANEL_BACKGROUND);
 
                 if (!getService().isNew()) {
                     typeRadioGroup.setEnabled(false);
                 }
                 typeRadioGroup.addListeners(new ItemListener() {
                     public void itemStateChanged(final ItemEvent e) {
                         final Thread thread = new Thread(new Runnable() {
                             public void run() {
                                 if (e.getStateChange() == ItemEvent.SELECTED) {
                                     final String value =
                                         ((JRadioButton) e.getItem()).getText();
                                     changeType(value);
                                 }
                             }
                         });
                         thread.start();
                     }
                 }, null);
                 optionsPanel.add(typeRadioGroup);
             }
 
             if (cloneInfo != null) {
                 /* add clone fields */
                 addCloneFields(optionsPanel,
                                extraOptionsPanel,
                                SERVICE_LABEL_WIDTH,
                                SERVICE_FIELD_WIDTH);
             }
 
             /* id textfield */
             addIdField(optionsPanel, SERVICE_LABEL_WIDTH, SERVICE_FIELD_WIDTH);
 
             /* heartbeat fields */
             addHeartbeatFields(optionsPanel,
                                SERVICE_LABEL_WIDTH,
                                SERVICE_FIELD_WIDTH);
 
             if (cloneInfo == null) {
                 /* score combo boxes */
                 addHostScores(optionsPanel,
                               SERVICE_LABEL_WIDTH,
                               SERVICE_FIELD_WIDTH);
             }
 
             /* get dependent resources and create combo boxes for ones, that
              * need parameters */
             paramComboBoxClear();
             final String[] params = getParametersFromXML();
             addParams(optionsPanel,
                       extraOptionsPanel,
                       params,
                       SERVICE_LABEL_WIDTH,
                       SERVICE_FIELD_WIDTH);
             if (!getResourceAgent().isGroup()
                 && !getResourceAgent().isClone()) {
                 /* Operations */
                 addOperations(optionsPanel,
                               extraOptionsPanel,
                               SERVICE_LABEL_WIDTH,
                               SERVICE_FIELD_WIDTH);
                 /* add item listeners to the operations combos */
                 for (final String op : HB_OPERATIONS) {
                     for (final String param : HB_OPERATION_PARAMS.get(op)) {
                         String dv = resourceAgent.getOperationDefault(op,
                                                                       param);
                         if (dv == null) {
                             continue;
                         }
                         final GuiComboBox cb =
                                 (GuiComboBox) operationsComboBoxHash.get(op, param);
                         cb.addListeners(
                             new ItemListener() {
                                 public void itemStateChanged(final ItemEvent e) {
 
                                     if (cb.isCheckBox()
                                         || e.getStateChange() == ItemEvent.SELECTED) {
                                         Thread thread = new Thread(new Runnable() {
                                             public void run() {
                                                 final boolean enable =
                                                     checkResourceFields("cached", params);
                                                 SwingUtilities.invokeLater(
                                                 new Runnable() {
                                                     public void run() {
                                                         applyButton.setEnabled(enable);
                                                     }
                                                 });
                                             }
                                         });
                                         thread.start();
                                     }
                                 }
                             },
 
                             new DocumentListener() {
                                 private void check() {
                                     Thread thread = new Thread(new Runnable() {
                                         public void run() {
                                             final boolean enable =
                                                 checkResourceFields("cached",
                                                                     params);
                                             SwingUtilities.invokeLater(
                                             new Runnable() {
                                                 public void run() {
                                                     applyButton.setEnabled(enable);
                                                 }
                                             });
                                         }
                                     });
                                     thread.start();
                                 }
 
                                 public void insertUpdate(final DocumentEvent e) {
                                     check();
                                 }
 
                                 public void removeUpdate(final DocumentEvent e) {
                                     check();
                                 }
 
                                 public void changedUpdate(final DocumentEvent e) {
                                     check();
                                 }
                             }
                         );
                     }
                 }
             }
             /* add item listeners to the host scores combos */
             if (cloneInfo == null) {
                 addHostScoreListeners();
             } else {
                 cloneInfo.addHostScoreListeners();
             }
             /* apply button */
             addApplyButton(buttonPanel);
             applyButton.setEnabled(
                 checkResourceFields(null, params)
             );
             /* expert mode */
             buttonPanel.add(Tools.expertModeButton(extraOptionsPanel));
 
             mainPanel.add(optionsPanel);
             mainPanel.add(extraOptionsPanel);
             infoPanel = new JPanel();
             infoPanel.setBackground(PANEL_BACKGROUND);
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             infoPanel.add(buttonPanel);
             infoPanel.add(new JScrollPane(mainPanel));
             infoPanel.add(Box.createVerticalGlue());
             /* if id textfield was changed and this id is not used,
              * enable apply button */
             return infoPanel;
         }
 
         /**
          * Clears the info panel cache, forcing it to reload.
          */
         public boolean selectAutomaticallyInTreeMenu() {
             return infoPanel == null;
         }
 
         /**
          * Goes through the scores and sets preferred locations.
          */
         protected void setLocations(final String heartbeatId) {
             final Host dcHost = getDCHost();
             for (Host host : getClusterHosts()) {
                 final HostInfo hi = host.getBrowser().getHostInfo();
                 final GuiComboBox cb = scoreComboBoxHash.get(hi);
                 final String hs = cb.getStringValue();
                 final String hsSaved = savedHostScores.get(hi);
                 if ((hsSaved == null && !"".equals(hs))
                     || (hsSaved != null && !hs.equals(hsSaved))) {
                     final String onHost = hi.getName();
                     final String score = hs;
                     final String locationId =
                               clusterStatus.getLocationId(
                                                 getService().getHeartbeatId(),
                                                 onHost);
                     if (score == null || "".equals(score)) {
                         CRM.removeLocation(dcHost,
                                            locationId,
                                            getService().getHeartbeatId(),
                                            hsSaved);
                     } else {
                         CRM.setLocation(dcHost,
                                         getService().getHeartbeatId(),
                                         onHost,
                                         score,
                                         locationId);
                     }
                 }
             }
             storeHostScores();
         }
 
         /**
          * Returns hash with changed operation ids and all name, value pairs.
          * This works for new heartbeats >= 2.99.0
          */
         private Map<String, Map<String, String>> getOperations(
                                                         String heartbeatId) {
             final Map<String, Map<String, String>> operations =
                                   new HashMap<String, Map<String, String>>();
 
             for (final String op : HB_OPERATIONS) {
                 final Map<String, String> opHash =
                                                 new HashMap<String, String>();
                 String opId = clusterStatus.getOpId(heartbeatId, op);
                 if (opId == null) {
                     /* generate one */
                     opId = "op-" + heartbeatId + "-" + op;
                 }
                 /* operations have different kind of default, that is
                  * recommended, but not used by default. */
                 for (final String param : HB_OPERATION_PARAM_LIST) {
                     boolean atLeastOneValue = false;
                     if (HB_OPERATION_PARAMS.get(op).contains(param)) {
                         if (cloneInfo == null
                             && (HB_OP_DEMOTE.equals(op)
                                 || HB_OP_PROMOTE.equals(op))) {
                             continue;
                         }
                         final GuiComboBox cb =
                             (GuiComboBox) operationsComboBoxHash.get(op,
                                                                      param);
                         String value;
                         if (cb != null) {
                             value = cb.getStringValue();
                         } else {
                             value = "0";
                         }
                         if (value != null && !"".equals(value)) {
                             if (cb != null) {
                                 atLeastOneValue = true;
                             }
                             opHash.put(param, value);
                         }
                     }
                     if (atLeastOneValue && opHash.size() > 0) {
                         operations.put(op, opHash);
                         opHash.put("id", opId);
                         opHash.put("name", op);
                     }
                 }
             }
             return operations;
         }
 
         /**
          * Creates new heartbeat id label.
          */
         protected void createNewHeartbeatIdLabel() {
             heartbeatIdLabel = new JLabel(getService().getHeartbeatId());
         }
 
         /**
          * Sets the visible heartbeat id.
          */
         protected void setHeartbeatIdLabel() {
             heartbeatIdLabel.setText(getService().getHeartbeatId());
         }
 
         /**
          * Returns pacemaker id label.
          */
         protected JLabel getHeartbeatIdLabel() {
             return heartbeatIdLabel;
         }
 
         /**
          * Applies the changes to the service parameters.
          */
         public void apply() {
             /* TODO: make progress indicator per resource. */
             setUpdated(true);
             final String[] params = getParametersFromXML();
             String cloneId = null;
             String[] cloneParams = null;
             boolean master = false;
             if (cloneInfo != null) {
                 cloneId = cloneInfo.getService().getHeartbeatId();
                 cloneParams = cloneInfo.getParametersFromXML();
                 master = cloneInfo.getService().isMaster();
 
             }
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                     idField.setEnabled(false);
                     if (cloneInfo != null) {
                         cloneInfo.getIdField().setEnabled(false);
                     }
                 }
             });
 
             /* add myself to the hash with service name and id as
              * keys */
             removeFromServiceInfoHash(this);
             final String oldHeartbeatId = getService().getHeartbeatId();
             if (oldHeartbeatId != null) {
                 heartbeatIdToServiceInfo.remove(oldHeartbeatId);
                 heartbeatIdList.remove(oldHeartbeatId);
             }
             if (getService().isNew()) {
                 final String id = idField.getStringValue();
                 getService().setId(id);
                 typeRadioGroup.setEnabled(false);
             }
             setHeartbeatIdLabel();
             addToHeartbeatIdList(this);
             addNameToServiceInfoHash(this);
 
             final ServiceInfo addedService = addResourceBefore();
 
             Map<String,String> cloneMetaArgs = new HashMap<String,String>();
             Map<String,String> pacemakerResAttrs =
                                                 new HashMap<String,String>();
             Map<String,String> pacemakerResArgs = new HashMap<String,String>();
             Map<String,String> pacemakerMetaArgs = new HashMap<String,String>();
             final String raClass = getService().getResourceClass();
             final String type = getResource().getName();
             final String provider = resourceAgent.getProvider();
             String heartbeatId = getService().getHeartbeatId();
             pacemakerResAttrs.put("id",       heartbeatId);
             pacemakerResAttrs.put("class",    raClass);
             if (!HB_HEARTBEAT_CLASS.equals(raClass)) {
                 pacemakerResAttrs.put("provider", provider);
             }
             pacemakerResAttrs.put("type",     type);
             String groupId = null; /* for pacemaker */
             if (groupInfo != null) {
                 if (groupInfo.getService().isNew()) {
                     groupInfo.apply();
                 }
                 groupId = groupInfo.getService().getHeartbeatId();
             }
 
             final Host dcHost = getDCHost();
             if (getService().isNew()) {
                 /* TODO: there are more attributes. */
                 final String hbV = dcHost.getHeartbeatVersion();
                 if (cloneInfo != null) {
                     for (String param : cloneParams) {
                         String value = cloneInfo.getComboBoxValue(param);
                         if (value.equals(cloneInfo.getParamDefault(param))) {
                                 continue;
                         }
                         if (!"".equals(value)) {
                             cloneMetaArgs.put(param, value);
                         }
                     }
                 }
                 for (final String param : params) {
                     String value = getComboBoxValue(param);
                     if (value.equals(getParamDefault(param))) {
                             continue;
                     }
                     if (!"".equals(value)) {
                         /* for pacemaker */
                         if (isMetaAttr(param)) {
                             pacemakerMetaArgs.put(param, value);
                         } else {
                             pacemakerResArgs.put(param, value);
                         }
                     }
                 }
                 String command = "-C";
                 if ((groupInfo != null && !groupInfo.getService().isNew())
                     || (cloneInfo != null && !cloneInfo.getService().isNew())) {
                     command = "-U";
                 }
                 CRM.setParameters(dcHost,
                                   command,
                                   heartbeatId,
                                   cloneId,
                                   master,
                                   cloneMetaArgs,
                                   groupId,
                                   pacemakerResAttrs,
                                   pacemakerResArgs,
                                   pacemakerMetaArgs,
                                   null,
                                   null,
                                   getOperations(heartbeatId),
                                   null);
                 if (groupInfo == null) {
                     final String[] parents = heartbeatGraph.getParents(this);
                     String hbId = heartbeatId;
                     if (cloneInfo != null) {
                         hbId = cloneInfo.getService().getHeartbeatId();
                     }
                     final List<Map<String, String>> colAttrsList =        
                                new ArrayList<Map<String, String>>();
                     final List<Map<String, String>> ordAttrsList =
                                new ArrayList<Map<String, String>>();
                     for (final String parentId : parents) {
                         final ServiceInfo parentInfo =
                                         heartbeatIdToServiceInfo.get(parentId);
                         final Map<String, String> colAttrs =        
                                            new LinkedHashMap<String, String>();
                         final Map<String, String> ordAttrs =
                                            new LinkedHashMap<String, String>();
                         colAttrs.put("score", "INFINITY");
                         ordAttrs.put("score", "INFINITY");
                         if (parentInfo.getService().isMaster()) {
                             colAttrs.put("with-rsc-role", "Master");
                             ordAttrs.put("first-action", "promote");
                             ordAttrs.put("then-action", "start");
                         }
                         colAttrsList.add(colAttrs);
                         ordAttrsList.add(ordAttrs);
                     }
                     CRM.setOrderAndColocation(dcHost,
                                               hbId,
                                               parents,
                                               colAttrsList,
                                               ordAttrsList);
                 }
             } else {
                 if (cloneInfo != null) {
                     for (String param : cloneParams) {
                         //final String oldValue =
                         //               cloneInfo.getResource().getValue(param);
                         String value = cloneInfo.getComboBoxValue(param);
                         //if (value.equals(oldValue)) {
                         //    continue;
                         //}
 
                         if (value.equals(cloneInfo.getParamDefault(param))) {
                                 continue;
                         }
                         if (!"".equals(value)) {
                             cloneMetaArgs.put(param, value);
                         }
                     }
                 }
                 /* update parameters */
                 final StringBuffer args = new StringBuffer("");
                 for (String param : params) {
                     //final String oldValue = getResource().getValue(param);
                     String value = getComboBoxValue(param);
                     if (value.equals(getParamDefault(param))) {
                             continue;
                     }
                     //if (value.equals(oldValue)) {
                     //    continue;
                     //}
 
                     if (!"".equals(value)) {
                         if (isMetaAttr(param)) {
                             pacemakerMetaArgs.put(param, value);
                         } else {
                             pacemakerResArgs.put(param, value);
                         }
                     }
                 }
 
                 groupId = null; /* we don't want to replace the whole group */
                 CRM.setParameters(
                         dcHost,
                         "-R",
                         heartbeatId,
                         cloneId,
                         master,
                         cloneMetaArgs,
                         groupId,
                         pacemakerResAttrs,
                         pacemakerResArgs,
                         pacemakerMetaArgs,
                         clusterStatus.getResourceInstanceAttrId(heartbeatId),
                         clusterStatus.getParametersNvpairsIds(heartbeatId),
                         getOperations(heartbeatId),
                         clusterStatus.getOperationsId(heartbeatId));
             }
 
             if (groupInfo == null) { // TODO: what to do with res grp locations?
                 if (cloneInfo == null) {
                     setLocations(heartbeatId);
                 } else {
                     cloneInfo.setLocations(heartbeatId);
                 }
             }
             storeComboBoxValues(params);
             if (cloneInfo != null) {
                 cloneInfo.storeComboBoxValues(cloneParams);
             }
 
             reload(getNode());
             heartbeatGraph.repaint();
 
         }
 
         /**
          * Removes order.
          */
         public void removeOrder(final ServiceInfo parent) {
             parent.setUpdated(true);
             setUpdated(true);
             final String parentHbId = parent.getService().getHeartbeatId();
             final String orderId =
                     clusterStatus.getOrderId(parentHbId,
                                                getService().getHeartbeatId());
             final String score =
                 clusterStatus.getOrderScore(
                                     parent.getService().getHeartbeatId(),
                                     getService().getHeartbeatId());
             final String symmetrical =
                 clusterStatus.getOrderSymmetrical(
                                     parent.getService().getHeartbeatId(),
                                     getService().getHeartbeatId());
             CRM.removeOrder(getDCHost(),
                             orderId,
                             parentHbId,
                             getService().getHeartbeatId(),
                             score,
                             symmetrical);
         }
 
         /**
          * Adds order constraint from this service to the parent.
          */
         public void addOrder(final ServiceInfo parent) {
             parent.setUpdated(true);
             setUpdated(true);
             final String parentHbId = parent.getService().getHeartbeatId();
             final Map<String, String> attrs =
                                           new LinkedHashMap<String, String>();
             attrs.put("score", "INFINITY");
             if (parent.getCloneInfo() != null
                 && parent.getCloneInfo().getService().isMaster()) {
                 attrs.put("first-action", "promote");
                 attrs.put("then-action", "start");
             }
             CRM.addOrder(getDCHost(),
                          null, /* order id */
                          parentHbId,
                          getService().getHeartbeatId(),
                          attrs);
         }
 
         /**
          * Removes colocation.
          */
         public void removeColocation(final ServiceInfo parent) {
             parent.setUpdated(true);
             setUpdated(true);
             final String parentHbId = parent.getService().getHeartbeatId();
             final String colocationId =
                 clusterStatus.getColocationId(parentHbId,
                                                 getService().getHeartbeatId());
             final String score =
                 clusterStatus.getColocationScore(
                                     parent.getService().getHeartbeatId(),
                                     getService().getHeartbeatId());
             CRM.removeColocation(getDCHost(),
                                  colocationId,
                                  getService().getHeartbeatId(), /* to */
                                  parentHbId, /* from */
                                  score);
         }
 
         /**
          * Adds colocation constraint from this service to the parent. The
          * parent - child order is here important, in case colocation
          * constraint is used along with order constraint.
          */
         public void addColocation(final ServiceInfo parent) {
             parent.setUpdated(true);
             setUpdated(true);
             final String parentHbId = parent.getService().getHeartbeatId();
             final Map<String, String> attrs =
                                    new LinkedHashMap<String, String>();
             attrs.put("score", "INFINITY");
             if (parent.getCloneInfo() != null
                 && parent.getCloneInfo().getService().isMaster()) {
                 attrs.put("with-rsc-role", "Master");
             }
             CRM.addColocation(getDCHost(),
                               null, /* col id */
                               parentHbId,
                               getService().getHeartbeatId(),
                               attrs);
         }
 
         /**
          * Returns panel with graph.
          */
         public JPanel getGraphicalView() {
             return heartbeatGraph.getGraphPanel();
         }
 
         /**
          * Adds service panel to the position 'pos'.
          */
         public ServiceInfo addServicePanel(final ResourceAgent newRA,
                                            final Point2D pos,
                                            final boolean reloadNode,
                                            final boolean master) {
             ServiceInfo newServiceInfo;
 
             final String name = newRA.getName();
             if (newRA.isFilesystem()) {
                 newServiceInfo = new FilesystemInfo(name, newRA);
             } else if (newRA.isLinbitDrbd()) {
                 newServiceInfo = new LinbitDrbdInfo(name, newRA);
             } else if (newRA.isDrbddisk()) {
                 newServiceInfo = new DrbddiskInfo(name, newRA);
             } else if (newRA.isIPaddr()) {
                 newServiceInfo = new IPaddrInfo(name, newRA);
             } else if (newRA.isGroup()) {
                 newServiceInfo = new GroupInfo(newRA);
             } else if (newRA.isClone()) {
                 String cloneName;
                 if (master) {
                     cloneName = PM_MASTER_SLAVE_SET_NAME;
                 } else {
                     cloneName = PM_CLONE_SET_NAME;
                 }
                 newServiceInfo = new CloneInfo(newRA,
                                                cloneName,
                                                master);
             } else {
                 newServiceInfo = new ServiceInfo(name, newRA);
             }
             addToHeartbeatIdList(newServiceInfo);
 
             addServicePanel(newServiceInfo, pos, reloadNode);
             return newServiceInfo;
         }
 
         /**
          * Adds service panel to the position 'pos'.
          * TODO: is it used?
          */
         public void addServicePanel(final ServiceInfo serviceInfo,
                                     final Point2D pos,
                                     final boolean reloadNode) {
 
             serviceInfo.getService().setResourceClass(
                         serviceInfo.getResourceAgent().getResourceClass());
             if (heartbeatGraph.addResource(serviceInfo, this, pos)) {
                 /* edge added */
                 final String parentId = getService().getHeartbeatId();
                 final String heartbeatId =
                                     serviceInfo.getService().getHeartbeatId();
                 final List<Map<String, String>> colAttrsList =
                                         new ArrayList<Map<String, String>>();
                 final List<Map<String, String>> ordAttrsList =
                                         new ArrayList<Map<String, String>>();
                 final Map<String, String> colAttrs =
                                            new LinkedHashMap<String, String>();
                 final Map<String, String> ordAttrs =
                                            new LinkedHashMap<String, String>();
                 colAttrs.put("score", "INFINITY");
                 ordAttrs.put("score", "INFINITY");
                 if (getService().isMaster()) {
                     colAttrs.put("with-rsc-role", "Master");
                     ordAttrs.put("first-action", "promote");
                     ordAttrs.put("then-action", "start");
                 }
                 colAttrsList.add(colAttrs);
                 ordAttrsList.add(ordAttrs);
                 CRM.setOrderAndColocation(getDCHost(),
                                           heartbeatId,
                                           new String[]{parentId},
                                           colAttrsList,
                                           ordAttrsList);
             } else {
                 addNameToServiceInfoHash(serviceInfo);
                 final DefaultMutableTreeNode newServiceNode =
                                         new DefaultMutableTreeNode(serviceInfo);
                 serviceInfo.setNode(newServiceNode);
 
                 servicesNode.add(newServiceNode);
                 if (reloadNode) {
                     reload(servicesNode);
                     reload(newServiceNode);
                 }
             }
             if (serviceInfo.getResourceAgent().isMasterSlave()) {
                 serviceInfo.changeType("Master / Slave");
             }
             heartbeatGraph.reloadServiceMenus();
         }
 
         /**
          * Returns service that belongs to this info object.
          */
         public Service getService() {
             return (Service) getResource();
         }
 
         /**
          * Starts resource in crm.
          */
         public void startResource() {
             setUpdated(true);
             CRM.startResource(getDCHost(), getService().getHeartbeatId());
         }
 
         /**
          * Stops resource in crm.
          */
         public void stopResource() {
             setUpdated(true);
             CRM.stopResource(getDCHost(), getService().getHeartbeatId());
         }
 
         /**
          * Migrates resource in heartbeat from current location.
          */
         public void migrateResource(final String onHost) {
             setUpdated(true);
             CRM.migrateResource(getDCHost(),
                                 getService().getHeartbeatId(),
                                 onHost);
         }
 
         /**
          * Removes constraints created by resource migrate command.
          */
         public void unmigrateResource() {
             setUpdated(true);
             CRM.unmigrateResource(getDCHost(),
                                   getService().getHeartbeatId());
         }
 
         /**
          * Moves resource up in the group.
          */
         public void moveGroupResUp() {
             setUpdated(true);
             CRM.moveGroupResUp(getDCHost(), getService().getHeartbeatId());
         }
 
         /**
          * Moves resource down in the group.
          */
         public void moveGroupResDown() {
             setUpdated(true);
             CRM.moveGroupResDown(getDCHost(), getService().getHeartbeatId());
         }
 
         /**
          * Cleans up the resource.
          */
         public void cleanupResource() {
             setUpdated(true);
             List<Host> dirtyHosts = new ArrayList<Host>();
             for (final Host host : getClusterHosts()) {
                 if (getFailCount(host.getName()) != null) {
                     dirtyHosts.add(host);
                 }
             }
             CRM.cleanupResource(getDCHost(),
                                 getService().getHeartbeatId(),
                                 dirtyHosts.toArray(
                                           new Host[dirtyHosts.size()]));
         }
 
         /**
          * Removes the service without confirmation dialog.
          */
         protected void removeMyselfNoConfirm() {
             setUpdated(true);
             getService().setRemoved(true);
             if (cloneInfo != null) {
                 cloneInfo.removeMyselfNoConfirm();
             }
 
             if (getService().isNew()) {
                 heartbeatGraph.getServicesInfo().setAllResources();
             } else {
                 final Host dcHost = getDCHost();
                 if (groupInfo == null) {
                     final HbConnectionInfo[] hbcis =
                                         heartbeatGraph.getHbConnections(this);
                     for (final HbConnectionInfo hbci : hbcis) {
                         heartbeatGraph.removeOrder(hbci);
                         heartbeatGraph.removeColocation(hbci);
                     }
 
                     for (String locId : clusterStatus.getLocationIds(getService().getHeartbeatId())) {
                         final String locScore =
                             clusterStatus.getLocationScore(locId);
                         CRM.removeLocation(dcHost,
                                            locId,
                                            getService().getHeartbeatId(),
                                            locScore);
                     }
                 }
                 if (!getResourceAgent().isGroup()) {
                     String groupId = null; /* for pacemaker */
                     if (groupInfo != null) {
                         /* get group id only if there is only one resource in a
                          * group.
                          */
                         final String group = groupInfo.getService().getHeartbeatId();
                         final Enumeration e = groupInfo.getNode().children();
                         while (e.hasMoreElements()) {
                             DefaultMutableTreeNode n =
                                       (DefaultMutableTreeNode) e.nextElement();
                             final ServiceInfo child =
                                                (ServiceInfo) n.getUserObject();
                             child.getService().setModified(true);
                             child.getService().doneModifying();
                         }
                         if (clusterStatus.getGroupResources(
                                                           group).size() == 1) {
                             groupInfo.getService().setRemoved(true);
                             groupInfo.removeMyselfNoConfirmFromChild();
                             groupId = group;
                             groupInfo.getService().doneRemoving();
                         }
                     }
                     String cloneId = null;
                     boolean master = false;
                     if (cloneInfo != null) {
                         cloneId = cloneInfo.getService().getHeartbeatId();
                         master = cloneInfo.getService().isMaster();
                     }
                     CRM.removeResource(dcHost,
                                        getService().getHeartbeatId(),
                                        groupId,
                                        cloneId,
                                        master);
                 }
             }
             removeFromServiceInfoHash(this);
             infoPanel = null;
             getService().doneRemoving();
         }
 
         /**
          * Removes this service from the heartbeat with confirmation dialog.
          */
         public void removeMyself() {
             String desc = Tools.getString(
                             "ClusterBrowser.confirmRemoveService.Description");
 
             desc  = desc.replaceAll("@SERVICE@", toString());
             if (Tools.confirmDialog(
                    Tools.getString("ClusterBrowser.confirmRemoveService.Title"),
                    desc,
                    Tools.getString("ClusterBrowser.confirmRemoveService.Yes"),
                    Tools.getString("ClusterBrowser.confirmRemoveService.No"))) {
                 removeMyselfNoConfirm();
             }
             getService().setNew(false);
         }
 
         /**
          * Removes the service from some global hashes and lists.
          */
         public void removeInfo() {
             heartbeatIdToServiceInfo.remove(getService().getHeartbeatId());
             heartbeatIdList.remove(getService().getHeartbeatId());
             removeFromServiceInfoHash(this);
             super.removeMyself();
         }
 
         /**
          * Sets this service as part of a group.
          */
         public void setGroupInfo(final GroupInfo groupInfo) {
             this.groupInfo = groupInfo;
         }
 
         /**
          * Sets this service as part of a clone set.
          */
         public void setCloneInfo(final CloneInfo cloneInfo) {
             this.cloneInfo = cloneInfo;
         }
 
         /**
          * Returns the group to which this service belongs or null, if it is
          * not in any group.
          */
         public GroupInfo getGroupInfo() {
             return groupInfo;
         }
 
         /**
          * Returns the clone set to which this service belongs
          * or null, if it is not in such set.
          */
         public CloneInfo getCloneInfo() {
             return cloneInfo;
         }
 
         /**
          * Returns list of items for service popup menu with actions that can
          * be executed on the heartbeat services.
          */
         public List<UpdatableItem> createPopup() {
             final List<UpdatableItem> items = new ArrayList<UpdatableItem>();
             /* remove service */
             final MyMenuItem removeMenuItem = new MyMenuItem(
                         Tools.getString(
                                 "ClusterBrowser.Hb.RemoveService"),
                         REMOVE_ICON) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     // TODO: if it was migrated
                     return !getService().isRemoved();
                 }
 
                 public void action() {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             getPopup().setVisible(false);
                         }
                     });
                     removeMyself();
                     heartbeatGraph.getVisualizationViewer().repaint();
                 }
             };
             items.add((UpdatableItem) removeMenuItem);
             registerMenuItem((UpdatableItem) removeMenuItem);
 
             if (groupInfo == null && cloneInfo == null) {
                 /* add new group and dependency*/
                 final MyMenuItem addGroupMenuItem =
                     new MyMenuItem(Tools.getString(
                                         "ClusterBrowser.Hb.AddDependentGroup"),
                                    null,
                                    null) {
                         private static final long serialVersionUID = 1L;
 
                         public boolean enablePredicate() {
                             return !getService().isRemoved();
                         }
 
                         public void action() {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     getPopup().setVisible(false);
                                 }
                             });
                             final StringInfo gi = new StringInfo(PM_GROUP_NAME,
                                                                  PM_GROUP_NAME);
                             addServicePanel(crmXML.getHbGroup(),
                                             getPos(),
                                             true,
                                             false);
                             heartbeatGraph.getVisualizationViewer().repaint();
                         }
                     };
                 items.add((UpdatableItem) addGroupMenuItem);
                 registerMenuItem((UpdatableItem) addGroupMenuItem);
 
                 /* add new service and dependency*/
                 final MyMenu addServiceMenuItem = new MyMenu(
                                     Tools.getString(
                                         "ClusterBrowser.Hb.AddDependency")) {
                     private static final long serialVersionUID = 1L;
 
                     public void update() {
                         super.update();
                         removeAll();
                         final Point2D pos = getPos();
                         final ResourceAgent fsService =
                                  crmXML.getResourceAgent("Filesystem",
                                                          HB_HEARTBEAT_PROVIDER,
                                                          "ocf");
                         if (crmXML.isLinbitDrbdPresent()) {/* just skip it,
                                                                if it is not */
                             final ResourceAgent linbitDrbdService =
                                                       crmXML.getHbLinbitDrbd();
                             final MyMenuItem ldMenuItem = new MyMenuItem(
                              Tools.getString("ClusterBrowser.linbitDrbdMenuName")) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     if (!linbitDrbdConfirmDialog()) {
                                         return;
                                     }
 
                                     final FilesystemInfo fsi = (FilesystemInfo)
                                                                    addServicePanel(
                                                                         fsService,
                                                                         getPos(),
                                                                         true,
                                                                         false);
                                     fsi.setDrbddiskIsPreferred(false);
                                     heartbeatGraph.getVisualizationViewer().repaint();
                                 }
                             };
                             if (isOneDrbddisk()
                                 || !crmXML.isLinbitDrbdPresent()) {
                                 ldMenuItem.setEnabled(false);
                             }
                             ldMenuItem.setPos(pos);
                             add(ldMenuItem);
                         }
                         if (crmXML.isDrbddiskPresent()) {/* just skip it,
                                                                if it is not */
                             final ResourceAgent drbddiskService =
                                                         crmXML.getHbDrbddisk();
                             final MyMenuItem ddMenuItem = new MyMenuItem(
                              Tools.getString("ClusterBrowser.DrbddiskMenuName")) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     final FilesystemInfo fsi = (FilesystemInfo)
                                                                addServicePanel(
                                                                     fsService,
                                                                     getPos(),
                                                                     true,
                                                                     false);
                                     fsi.setDrbddiskIsPreferred(true);
                                     heartbeatGraph.getVisualizationViewer().repaint();
                                 }
                             };
                             if (isOneLinbitDrbd()
                                 || !crmXML.isDrbddiskPresent()) {
                                 ddMenuItem.setEnabled(false);
                             }
                             ddMenuItem.setPos(pos);
                             add(ddMenuItem);
                         }
                         final ResourceAgent ipService = crmXML.getResourceAgent(
                                                              "IPaddr2",
                                                              HB_HEARTBEAT_PROVIDER,
                                                              "ocf");
                         if (ipService != null) { /* just skip it, if it is not*/
                             final MyMenuItem ipMenuItem =
                                    new MyMenuItem(ipService.getMenuName()) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     addServicePanel(ipService,
                                                     getPos(),
                                                     true,
                                                     false);
                                     heartbeatGraph.getVisualizationViewer().repaint();
                                 }
                             };
                             ipMenuItem.setPos(pos);
                             add(ipMenuItem);
                         }
                         if (fsService != null) { /* just skip it, if it is not*/
                             final MyMenuItem fsMenuItem =
                                    new MyMenuItem(fsService.getMenuName()) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     addServicePanel(fsService,
                                                     getPos(),
                                                     true,
                                                     false);
                                     heartbeatGraph.getVisualizationViewer().repaint();
                                 }
                             };
                             fsMenuItem.setPos(pos);
                             add(fsMenuItem);
                         }
                         for (final String cl : HB_CLASSES) {
                             final MyMenu classItem =
                                             new MyMenu(HB_CLASS_MENU.get(cl));
                             DefaultListModel m = new DefaultListModel();
                             for (final ResourceAgent ra : getAddServiceList(cl)) {
                                 final MyMenuItem mmi =
                                        new MyMenuItem(ra.getMenuName()) {
                                     private static final long serialVersionUID = 1L;
                                     public void action() {
                                         SwingUtilities.invokeLater(new Runnable() {
                                             public void run() {
                                                 getPopup().setVisible(false);
                                             }
                                         });
                                         if (ra.isLinbitDrbd()
                                             && !linbitDrbdConfirmDialog()) {
                                             return;
                                         }
                                         addServicePanel(ra,
                                                         getPos(),
                                                         true,
                                                         false);
                                         heartbeatGraph.getVisualizationViewer().repaint();
                                     }
                                 };
                                 mmi.setPos(pos);
                                 m.addElement(mmi);
                             }
                             classItem.add(Tools.getScrollingMenu(classItem, m));
                             add(classItem);
                         }
                     }
                 };
                 items.add((UpdatableItem) addServiceMenuItem);
                 registerMenuItem((UpdatableItem) addServiceMenuItem);
 
                 /* add existing service dependency*/
                 final ServiceInfo thisClass = this;
                 final MyMenu existingServiceMenuItem =
                                 new MyMenu(
                                     Tools.getString(
                                         "ClusterBrowser.Hb.AddStartBefore")) {
                     private static final long serialVersionUID = 1L;
 
                     public void update() {
                         super.update();
                         removeAll();
 
                         DefaultListModel m = new DefaultListModel();
                         for (final ServiceInfo asi
                                         : getExistingServiceList(thisClass)) {
                             if (asi.getGroupInfo() != null
                                 || asi.getCloneInfo() != null) {
                                 /* skip services that are in group. */
                                 continue;
                             }
                             final MyMenuItem mmi =
                                                 new MyMenuItem(asi.toString()) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     final Thread thread = new Thread(
                                         new Runnable() {
                                             public void run() {
                                                 SwingUtilities.invokeLater(new Runnable() {
                                                     public void run() {
                                                         getPopup().setVisible(
                                                                          false);
                                                     }
                                                 });
                                                 addServicePanel(asi,
                                                                 null,
                                                                 true);
                                                 SwingUtilities.invokeLater(new Runnable() {
                                                     public void run() {
                                                         repaint();
                                                     }
                                                 });
                                             }
                                         });
                                     thread.start();
                                 }
                             };
                             m.addElement(mmi);
                         }
                         add(Tools.getScrollingMenu(this, m));
                     }
                 };
                 items.add((UpdatableItem) existingServiceMenuItem);
                 registerMenuItem((UpdatableItem) existingServiceMenuItem);
             } else { /* group service */
                 final MyMenuItem moveUpMenuItem =
                     new MyMenuItem(Tools.getString(
                                             "ClusterBrowser.Hb.ResGrpMoveUp"),
                                    null, /* upIcon, */
                                    null) {
                         private static final long serialVersionUID = 1L;
 
                         public boolean enablePredicate() {
                             // TODO: don't if it is up
                             return getService().isAvailable();
                         }
 
                         public void action() {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     getPopup().setVisible(false);
                                 }
                             });
                             moveGroupResUp();
                         }
                     };
                 items.add(moveUpMenuItem);
                 registerMenuItem(moveUpMenuItem);
 
                 /* move down */
                 final MyMenuItem moveDownMenuItem =
                     new MyMenuItem(Tools.getString(
                                             "ClusterBrowser.Hb.ResGrpMoveDown"),
                                    null, // TODO: downIcon,
                                    null) {
                         private static final long serialVersionUID = 1L;
 
                         public boolean enablePredicate() {
                             // TODO: don't if it is down
                             return getService().isAvailable();
                         }
 
                         public void action() {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     getPopup().setVisible(false);
                                 }
                             });
                             moveGroupResDown();
                         }
                     };
                 items.add(moveDownMenuItem);
                 registerMenuItem(moveDownMenuItem);
             }
 
             /* start resource */
             final MyMenuItem startMenuItem =
                 new MyMenuItem(
                       Tools.getString("ClusterBrowser.Hb.StartResource"),
                       START_ICON,
                       Tools.getString("ClusterBrowser.Hb.StartResource.ToolTip")
                      ) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         return getService().isAvailable() && !isStarted();
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         startResource();
                     }
                 };
             items.add((UpdatableItem) startMenuItem);
             registerMenuItem((UpdatableItem) startMenuItem);
 
             /* stop resource */
             final MyMenuItem stopMenuItem =
                 new MyMenuItem(
                        Tools.getString("ClusterBrowser.Hb.StopResource"),
                        STOP_ICON,
                        Tools.getString("ClusterBrowser.Hb.StopResource.ToolTip")
                       ) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         return getService().isAvailable() && !isStopped();
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         stopResource();
                     }
                 };
             items.add((UpdatableItem) stopMenuItem);
             registerMenuItem((UpdatableItem) stopMenuItem);
 
             /* clean up resource */
             final MyMenuItem cleanupMenuItem =
                 new MyMenuItem(
                    Tools.getString("ClusterBrowser.Hb.CleanUpFailedResource"),
                    SERVICE_RUNNING_ICON,
                    Tools.getString("ClusterBrowser.Hb.CleanUpFailedResource"),
                    Tools.getString("ClusterBrowser.Hb.CleanUpResource"),
                    SERVICE_RUNNING_ICON,
                    Tools.getString("ClusterBrowser.Hb.CleanUpResource")) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean predicate() {
                         return getService().isAvailable()
                                && isOneFailed();
                     } 
 
                     public boolean enablePredicate() {
                         return getService().isAvailable()
                                && isOneFailedCount();
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         cleanupResource();
                     }
                 };
             items.add((UpdatableItem) cleanupMenuItem);
             registerMenuItem((UpdatableItem) cleanupMenuItem);
 
 
             /* manage resource */
             final MyMenuItem manageMenuItem =
                 new MyMenuItem(
                       Tools.getString("ClusterBrowser.Hb.ManageResource"),
                       START_ICON, // TODO: icons
                       Tools.getString("ClusterBrowser.Hb.ManageResource.ToolTip"),
 
                       Tools.getString("ClusterBrowser.Hb.UnmanageResource"),
                       STOP_ICON, // TODO: icons
                       Tools.getString("ClusterBrowser.Hb.UnmanageResource.ToolTip")
                      ) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean predicate() {
                         return !isManaged();
                     }
                     public boolean enablePredicate() {
                         return getService().isAvailable();
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         if (this.getText().equals(Tools.getString(
                                         "ClusterBrowser.Hb.ManageResource"))) {
                             setManaged(true);
                         } else {
                             setManaged(false);
                         }
                     }
                 };
             items.add((UpdatableItem) manageMenuItem);
             registerMenuItem((UpdatableItem) manageMenuItem);
             addMigrateMenuItems(items);
             /* view log */
             final MyMenuItem viewLogMenu = new MyMenuItem(
                             Tools.getString("ClusterBrowser.Hb.ViewServiceLog"),
                             null,
                             null) {
 
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     return !getService().isNew();
                 }
 
                 public void action() {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             getPopup().setVisible(false);
                         }
                     });
                     ServiceLogs l =
                                 new ServiceLogs(getCluster(),
                                                 getService().getHeartbeatId());
                     l.showDialog();
                 }
             };
             registerMenuItem(viewLogMenu);
             items.add(viewLogMenu);
             return items;
         }
         
         /**
          * Adds migrate and unmigrate menu items.
          */
         protected void addMigrateMenuItems(final List<UpdatableItem> items) {
             /* migrate resource */
             for (final String hostName : getHostNames()) {
                 final MyMenuItem migrateMenuItem =
                     new MyMenuItem(
                             Tools.getString(
                                  "ClusterBrowser.Hb.MigrateResource")
                                  + " " + hostName,
                             MIGRATE_ICON,
                             Tools.getString(
                                  "ClusterBrowser.Hb.MigrateResource.ToolTip"),
 
                             Tools.getString(
                                  "ClusterBrowser.Hb.MigrateResource")
                                  + " " + hostName + " (inactive)",
                             MIGRATE_ICON,
                             Tools.getString(
                                  "ClusterBrowser.Hb.MigrateResource.ToolTip")
                            ) {
                         private static final long serialVersionUID = 1L;
 
                         public boolean predicate() {
                             return isActiveNode(hostName);
                         }
 
                         public boolean enablePredicate() {
                             final List<String> runningOnNodes =
                                                            getRunningOnNodes();
                             if (runningOnNodes == null
                                 || runningOnNodes.size() == 0) {
                                 return false;
                             }
                             final String runningOnNode =
                                         runningOnNodes.get(0).toLowerCase();
                             return getService().isAvailable()
                                    && !hostName.toLowerCase().equals(
                                              runningOnNode)
                                    && isActiveNode(hostName);
                         }
 
                         public void action() {
                             SwingUtilities.invokeLater(new Runnable() {
                                 public void run() {
                                     getPopup().setVisible(false);
                                 }
                             });
                             migrateResource(hostName);
                         }
                     };
 
                 items.add((UpdatableItem) migrateMenuItem);
                 registerMenuItem((UpdatableItem) migrateMenuItem);
             }
 
             /* unmigrate resource */
             final MyMenuItem unmigrateMenuItem =
                 new MyMenuItem(Tools.getString(
                                     "ClusterBrowser.Hb.UnmigrateResource")) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         // TODO: if it was migrated
                         return getService().isAvailable();
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         unmigrateResource();
                     }
                 };
             items.add((UpdatableItem) unmigrateMenuItem);
             registerMenuItem((UpdatableItem) unmigrateMenuItem);
         }
 
         /**
          * Returns tool tip for the hearbeat service.
          */
         public String getToolTipText() {
             String nodeString = null;
             final List<String> nodes = getRunningOnNodes();
             if (nodes != null && nodes.size() > 0) {
                 nodeString =
                      Tools.join(", ", nodes.toArray(new String[nodes.size()]));
             }
             final Host[] hosts = cluster.getHostsArray();
             if (allHostsDown()) {
                 nodeString = "unknown";
             }
             final StringBuffer sb = new StringBuffer(200);
             sb.append("<b>");
             sb.append(toString());
             if (isFailed()) {
                 sb.append("</b> <b>Failed</b>");
             } else if (isStopped() || nodeString == null) {
                 sb.append("</b> not running");
             } else {
                 sb.append("</b> running on: ");
                 sb.append(nodeString);
             }
             if (!isManaged()) {
                 sb.append(" (unmanaged)");
             }
             return sb.toString();
         }
 
         /**
          * Returns heartbeat service class.
          */
         public ResourceAgent getResourceAgent() {
             return resourceAgent;
         }
 
         /**
          * Sets whether the info object is being updated.
          */
         protected void setUpdated(final boolean updated) {
             if (updated && !isUpdated()) {
                 heartbeatGraph.startAnimation(this);
             } else if (!updated) {
                 heartbeatGraph.stopAnimation(this);
             }
             super.setUpdated(updated);
         }
 
         /**
          * Returns text with lines as array that appears in the cluster graph.
          */
         protected Subtext[] getSubtextsForGraph() {
             Color color = null;
             final List<Subtext> texts = new ArrayList<Subtext>();
             if (isFailed()) {
                 texts.add(new Subtext("not running:", null));
             } else if (isStopped()) {
                 texts.add(new Subtext("stopped",
                                       Tools.getDefaultColor(
                                           "HeartbeatGraph.FillPaintStopped")));
             } else {
                 String runningOnNodeString = null;
                 if (allHostsDown()) {
                     runningOnNodeString = "unknown";
                 } else {
                     final List<String> runningOnNodes = getRunningOnNodes();
                     if (runningOnNodes != null && runningOnNodes.size() > 0) {
                         runningOnNodeString = runningOnNodes.get(0);
                         color = cluster.getHostColors(runningOnNodes).get(0);
                     }
                 }
                 if (runningOnNodeString != null) {
                     texts.add(new Subtext("running on: " + runningOnNodeString,
                                           color));
                 } else {
                     texts.add(new Subtext("not running",
                                           Tools.getDefaultColor(
                                            "HeartbeatGraph.FillPaintStopped")));
                 }
             }
             if (isOneFailedCount()) {
                 for (final Host host : getClusterHosts()) {
                     if (host.isClStatus()
                         && getFailCount(host.getName()) != null) {
                         texts.add(new Subtext("    " + host.getName()
                                               + getFailCountString(
                                                             host.getName()),
                                   null));
                     }
                 }
             }
             return texts.toArray(new Subtext[texts.size()]);
         }
 
         /**
          * Returns null, when this service is not a clone.
          */
         public ServiceInfo getContainedService() {
             return null;
         }
 
         /**
          * Returns type radio group.
          */
         public GuiComboBox getTypeRadioGroup() {
             return typeRadioGroup;
         }
 
         /**
          * Returns the id field.
          */
         public GuiComboBox getIdField() {
             return idField;
         }
 
         /**
          * Returns units.
          */
         protected final Unit[] getUnits() {
             return new Unit[]{
                 new Unit("", "", "Second", "Seconds"), /* default unit */
                 new Unit("ms", "ms", "Millisecond", "Milliseconds"),
                 new Unit("us", "us", "Microsecond", "Microseconds"),
                 new Unit("s",  "s",  "Second",      "Seconds"),
                 new Unit("m",  "m",  "Minute",      "Minutes"),
                 new Unit("h",   "h",  "Hour",        "Hours")
             };
         }
 
         /**
          * Returns text that appears above the icon in the graph.
          */
         public String getIconTextForGraph() {
             if (allHostsDown()) {
                 return Tools.getString("ClusterBrowser.Hb.NoInfoAvailable");
             }
             if (isStarted()) {
                 if (isRunning()) {
                     return null;
                 } else if (isFailed()) {
                     return Tools.getString("ClusterBrowser.Hb.StartingFailed");
 
                 } else {
                     return Tools.getString("ClusterBrowser.Hb.Starting");
                 }
             } else if (isStopped()) {
                 if (isRunning()) {
                     return Tools.getString("ClusterBrowser.Hb.Stopping");
                 } else {
                     return null;
                 }
             }
             return null;
         }
     }
 
     /**
      * This class holds clone service info object.
      */
     class CloneInfo extends ServiceInfo {
         /** Service that belongs to this clone. */
         private ServiceInfo containedService = null;
         /**
          * Creates new CloneInfo object.
          */
         public CloneInfo(final ResourceAgent ra,
                          final String name,
                          final boolean master) {
             super(name, ra);
             getService().setMaster(master);
         }
 
         /**
          * Adds service to this clone set. Adds it in the submenu in
          * the menu tree and initializes it.
          */
         public final void addCloneServicePanel(
                                             final ServiceInfo newServiceInfo) {
             containedService = newServiceInfo;
             newServiceInfo.getService().setResourceClass(
                     newServiceInfo.getResourceAgent().getResourceClass());
             newServiceInfo.setCloneInfo(this);
 
             addToHeartbeatIdList(newServiceInfo);
             addNameToServiceInfoHash(newServiceInfo);
             final DefaultMutableTreeNode newServiceNode =
                                     new DefaultMutableTreeNode(newServiceInfo);
             newServiceInfo.setNode(newServiceNode);
             getNode().add(newServiceNode);
             reload(getNode());
             reload(newServiceNode);
         }
         /**
          * Adds service to this clone set it is called when the submenu
          * was already initialized but it was changed to be m/s set.
          */
         public final void setCloneServicePanel(
                                             final ServiceInfo newServiceInfo) {
             containedService = newServiceInfo;
             addNameToServiceInfoHash(this);
             addToHeartbeatIdList(this);
             newServiceInfo.setCloneInfo(this);
             final DefaultMutableTreeNode node =
                                               new DefaultMutableTreeNode(this);
             setNode(node);
             servicesNode.add(node);
             node.add(newServiceInfo.getNode());
             reload(node);
         }
 
         /**
          * Returns items for the clone popup.
          */
         public final List<UpdatableItem> createPopup() {
             final List<UpdatableItem>items = super.createPopup();
             ///* add group service */
             //final MyMenu addGroupServiceMenuItem =new MyMenu(
             //            Tools.getString("ClusterBrowser.Hb.AddGroupService")) {
             //    private static final long serialVersionUID = 1L;
 
             //    public void update() {
             //        super.update();
 
             //        removeAll();
             //        for (final String cl : HB_CLASSES) {
             //            final MyMenu classItem =
             //                                new MyMenu(HB_CLASS_MENU.get(cl));
             //            DefaultListModel m = new DefaultListModel();
             //            for (final ResourceAgent ra
             //                                    : getAddGroupServiceList(cl)) {
             //                final MyMenuItem mmi =
             //                        new MyMenuItem(ra.getMenuName()) {
             //                    private static final long serialVersionUID = 1L;
             //                    public void action() {
             //                        getPopup().setVisible(false);
             //                        addGroupServicePanel(ra);
             //                        repaint();
             //                    }
             //                };
             //                m.addElement(mmi);
             //            }
             //            classItem.add(Tools.getScrollingMenu(classItem, m));
             //            add(classItem);
             //        }
             //    }
             //};
             //items.add(1, (UpdatableItem) addGroupServiceMenuItem);
             //registerMenuItem((UpdatableItem) addGroupServiceMenuItem);
 
             return items;
         }
         public final JComponent getInfoPanel() {
             if (containedService != null) {
                 return containedService.getInfoPanel();
             } else {
                 return new JPanel();
             }
         }
 
         /**
          * Returns whether the resource has failed to start.
          */
         public boolean isFailed() {
             final ServiceInfo ci = containedService;
             if (ci != null) {
                 return ci.isFailed();
             }
             return false;
         }
         /**
          * Returns fail count.
          */
         protected final String getFailCount(final String hostName) {
             final ServiceInfo ci = containedService;
             if (ci != null) {
                 return ci.getFailCount(hostName);
             }
             return "";
         }
 
         /**
          * Returns the main text that appears in the graph.
          */
         public final String getMainTextForGraph() {
             if (containedService == null) {
                 return super.getMainTextForGraph();
             } else {
                 return containedService.getMainTextForGraph();
             }
         }
 
         /**
          * Returns node name of the host where this service is slave.
          */
         public List<String> getSlaveOnNodes() {
             return clusterStatus.getSlaveOnNodes(
                                                 getService().getHeartbeatId());
         }
 
         /**
          * Returns color for the host vertex.
          */
         public List<Color> getHostColors() {
              List<String> nodes = getRunningOnNodes();
              final List<String> slaves = getSlaveOnNodes();
              int nodesCount = 0;
              if (nodes != null) {
                  nodesCount = nodes.size();
              } else {
                  nodes = new ArrayList<String>();
              }
              int slavesCount = 0;
              if (slaves != null) {
                  slavesCount = slaves.size();
              }
              if (nodesCount + slavesCount < getClusterHosts().length) {
                  final List<Color> colors = new ArrayList<Color>();
                  colors.add(Tools.getDefaultColor(
                                           "HeartbeatGraph.FillPaintStopped"));
                  return colors;
              } else {
                  return cluster.getHostColors(nodes);
              }
         }
 
         /**
          * Returns fail count string that appears in the graph.
          */
         private String getFailCountString(final String hostName) {
             String fcString = "";
             if (containedService != null) {
                 final String failCount =
                                        containedService.getFailCount(hostName);
                 if (failCount != null) {
                     if ("INFINITY".equals(failCount)) {
                         fcString = " failed";
                     } else {
                         fcString = " failed: " + failCount;
                     }
                 }
             }
             return fcString;
         }
 
         /**
          * Returns text with lines as array that appears in the cluster graph.
          */
         protected final Subtext[] getSubtextsForGraph() {
             final List<Subtext> texts = new ArrayList<Subtext>();
             final Set<String> notRunningOnNodes = new LinkedHashSet<String>();
             for (final Host h : getClusterHosts()) {
                 notRunningOnNodes.add(h.getName());
             }
             texts.add(new Subtext(toString(), null));
             if (allHostsDown()) {
                 return texts.toArray(new Subtext[texts.size()]);
             }
             List<String> runningOnNodes = getRunningOnNodes();
             if (runningOnNodes != null && runningOnNodes.size() > 0) {
                 if (containedService != null
                     && containedService.getResourceAgent().isLinbitDrbd()) {
                     texts.add(new Subtext("primary on:", null));
                 } else if (getService().isMaster()) {
                     texts.add(new Subtext("master on:", null));
                 } else {
                     texts.add(new Subtext("running on:", null));
                 }
                 final List<Color> colors =
                                        cluster.getHostColors(runningOnNodes);
                 int i = 0;
                 for (final String n : runningOnNodes) {
                     texts.add(new Subtext("    " + n + getFailCountString(n),
                                           colors.get(i)));
                     notRunningOnNodes.remove(n);
                     i++;
                 }
             }
             if (getService().isMaster()) {
                 List<String> slaveOnNodes = getSlaveOnNodes();
                 if (slaveOnNodes != null && slaveOnNodes.size() > 0) {
                     final List<Color> colors =
                                            cluster.getHostColors(slaveOnNodes);
                     int i = 0;
                     if (containedService != null
                         && containedService.getResourceAgent().isLinbitDrbd()) {
                         texts.add(new Subtext("secondary on:", null));
                     } else {
                         texts.add(new Subtext("slave on:", null));
                     }
                     for (final String n : slaveOnNodes) {
                         texts.add(new Subtext("    " + n
                                               + getFailCountString(n),
                                               colors.get(i)));
                         notRunningOnNodes.remove(n);
                         i++;
                     }
                 }
             }
             if (notRunningOnNodes.size() > 0) {
                 final Color nColor = Tools.getDefaultColor(
                                             "HeartbeatGraph.FillPaintStopped");
                 if (isStopped()) {
                     texts.add(new Subtext("stopped", nColor));
                 } else {
                     texts.add(new Subtext("not running on:", nColor));
                     for (final String n : notRunningOnNodes) {
                         Color color = nColor;
                         if (failedOnHost(n)) {
                             color = null;
                         }
                         texts.add(new Subtext("    "
                                               + n
                                               + getFailCountString(n),
                                               color));
                     }
                 }
             }
             return texts.toArray(new Subtext[texts.size()]);
         }
 
         /**
          * Returns service that belongs to this clone.
          */
         public final ServiceInfo getContainedService() {
             return containedService;
         }
 
         /**
          * Remove contained service and from there this clone service
          * will be removed.
          */
         public final void removeMyself() {
             containedService.removeMyself();
         }
 
         /**
          * In clone resource check its conaining service.
          */
         public final boolean checkResourceFields(final String param,
                                                  final String[] params) {
             final boolean cor = containedService.checkResourceFieldsCorrect(
                                       param,
                                       containedService.getParametersFromXML());
             if (cor) {
                 return containedService.checkResourceFieldsChanged(
                                       param,
                                       containedService.getParametersFromXML());
             }
             return cor;
         }
 
         /**
          * Returns whether service is started.
          */
         public final boolean isStarted() {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 return super.isStarted();
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     return cs.isStarted();
                 }
                 return false;
             }
         }
 
         /**
          * Returns whether service is started.
          */
         public final boolean isStopped() {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 return super.isStopped();
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     return cs.isStopped();
                 }
                 return false;
             }
         }
 
         /**
          * Returns whether service is managed.
          */
         public final boolean isManaged() {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 return super.isManaged();
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     return cs.isManaged();
                 }
                 return false;
             }
         }
 
         /**
          * Cleans up the resource.
          */
         public final void cleanupResource() {
             final ServiceInfo cs = containedService;
             if (cs != null) {
                 final String hbV = getDCHost().getHeartbeatVersion();
                 if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                     for (int i = 0; i < getClusterHosts().length; i++) {
                         CRM.cleanupResource(getDCHost(),
                                             cs.getService().getHeartbeatId()
                                             + ":" + Integer.toString(i),
                                             getClusterHosts());
                     }
                 } else {
                     super.cleanupResource();
                 }
             }
         }
 
         /**
          * Starts resource in crm.
          */
         public final void startResource() {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 super.startResource();
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     cs.startResource();
                 }
             }
         }
 
         /**
          * Stops resource in crm.
          */
         public final void stopResource() {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 super.stopResource();
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     cs.stopResource();
                 }
             }
         }
 
         /**
          * Migrates resource in heartbeat from current location.
          */
         public final void migrateResource(final String onHost) {
             final ServiceInfo cs = containedService;
             if (cs != null) {
                 cs.migrateResource(onHost);
             }
         }
 
         /**
          * Removes constraints created by resource migrate command.
          */
         public final void unmigrateResource() {
             final ServiceInfo cs = containedService;
             if (cs != null) {
                 cs.unmigrateResource();
             }
         }
 
         /**
          * Sets whether the service is managed.
          */
         public void setManaged(final boolean isManaged) {
             final String hbV = getDCHost().getHeartbeatVersion();
             if (Tools.compareVersions(hbV, "2.1.4") <= 0) {
                 super.setManaged(isManaged);
             } else {
                 final ServiceInfo cs = containedService;
                 if (cs != null) {
                     cs.setManaged(isManaged);
                 }
             }
         }
 
         /**
          * Adds migrate and unmigrate menu items.
          */
         protected void addMigrateMenuItems(final List<UpdatableItem> items) {
             /* no migrate / unmigrate menu items for clones. */
         }
     }
 
     /**
      * This class is used for all kind of categories in the heartbeat
      * hierarchy. Its point is to show heartbeat graph all the time, ane
      * heartbeat category is clicked.
      */
     class HbCategoryInfo extends CategoryInfo {
         /**
          * Creates the new HbCategoryInfo object with name of the category.
          */
         HbCategoryInfo(final String name) {
             super(name);
         }
 
         /**
          * Returns type of the info text. text/plain or text/html.
          */
         protected String getInfoType() {
             return "text/html";
         }
         /**
          * Returns info for the category.
          */
         public String getInfo() {
             return "<h2>" + getName() + "</h2>";
         }
 
         /**
          * Returns heartbeat graph.
          */
         public JPanel getGraphicalView() {
             return heartbeatGraph.getGraphPanel();
         }
     }
 
     /**
      * This class holds data that describe the heartbeat as whole.
      */
     class HeartbeatInfo extends HbCategoryInfo {
         /**
          * Prepares a new <code>ServicesInfo</code> object.
          *
          * @param name
          *      name that will be shown in the tree
          */
         public HeartbeatInfo(final String name) {
             super(name);
         }
 
         /**
          * Returns icon for the heartbeat menu item.
          */
         public ImageIcon getMenuIcon() {
             return null;
         }
 
         /**
          * Returns type of the info text. text/plain or text/html.
          */
         protected String getInfoType() {
             return "text/html";
         }
         /**
          * Returns info for the Heartbeat menu.
          */
         public String getInfo() {
             final StringBuffer s = new StringBuffer(30);
             s.append("<h2>");
             s.append(getName());
             if (crmXML == null) {
                 s.append("</h2><br>info not available");
                 return s.toString();
             }
 
             final Host[] hosts = getClusterHosts();
             int i = 0;
             final StringBuffer hbVersion = new StringBuffer();
             boolean differentHbVersions = false;
             for (Host host : hosts) {
                 if (i == 0) {
                     hbVersion.append(host.getHeartbeatVersion());
                 } else if (!hbVersion.toString().equals(
                                                 host.getHeartbeatVersion())) {
                     differentHbVersions = true;
                     hbVersion.append(", ");
                     hbVersion.append(host.getHeartbeatVersion());
                 }
 
                 i++;
             }
             s.append(" (" + hbVersion.toString() + ")</h2><br>");
             if (differentHbVersions) {
                 s.append(Tools.getString(
                                 "ClusterBrowser.DifferentHbVersionsWarning"));
                 s.append("<br>");
             }
             return s.toString();
         }
     }
 
     /**
      * This class holds info data for services view and global heartbeat
      * config.
      */
     class ServicesInfo extends EditableInfo {
         /** Cache for the info panel. */
         private JComponent infoPanel = null;
 
         /**
          * Prepares a new <code>ServicesInfo</code> object.
          *
          * @param name
          *      name that will be shown in the tree
          */
         public ServicesInfo(final String name) {
             super(name);
             setResource(new Resource(name));
             heartbeatGraph.setServicesInfo(this);
             initApplyButton();
         }
 
         /**
          * Sets info panel.
          */
         public void setInfoPanel(final JComponent infoPanel) {
             this.infoPanel = infoPanel;
         }
 
         /**
          * Returns icon for services menu item.
          */
         public ImageIcon getMenuIcon() {
             return null;
         }
 
         /**
          * Returns names of all global parameters.
          */
         public String[] getParametersFromXML() {
             return crmXML.getGlobalParameters();
         }
 
         /**
          * Returns long description of the global parameter, that is used for
          * tool tips.
          */
         protected String getParamLongDesc(final String param) {
             return crmXML.getGlobalParamLongDesc(param);
         }
 
         /**
          * Returns short description of the global parameter, that is used as
          * label.
          */
         protected String getParamShortDesc(final String param) {
             return crmXML.getGlobalParamShortDesc(param);
         }
 
         /**
          * Returns default for this global parameter.
          */
         protected String getParamDefault(final String param) {
             return crmXML.getGlobalParamDefault(param);
         }
 
         /**
          * Returns preferred value for this global parameter.
          */
         protected String getParamPreferred(final String param) {
             return crmXML.getGlobalParamPreferred(param);
         }
 
         /**
          * Returns possible choices for pulldown menus if applicable.
          */
         protected Object[] getParamPossibleChoices(final String param) {
             return crmXML.getGlobalParamPossibleChoices(param);
         }
 
         /**
          * Checks if the new value is correct for the parameter type and
          * constraints.
          */
         protected boolean checkParam(final String param,
                                      final String newValue) {
             return crmXML.checkGlobalParam(param, newValue);
         }
 
         /**
          * Returns whether the global parameter is of the integer type.
          */
         protected boolean isInteger(final String param) {
             return crmXML.isGlobalInteger(param);
         }
 
         /**
          * Returns whether the global parameter is of the time type.
          */
         protected boolean isTimeType(final String param) {
             return crmXML.isGlobalTimeType(param);
         }
 
         /**
          * Returns whether the global parameter is required.
          */
         protected boolean isRequired(final String param) {
             return crmXML.isGlobalRequired(param);
         }
 
         /**
          * Returns whether the global parameter is of boolean type and
          * requires a checkbox.
          */
         protected boolean isCheckBox(final String param) {
             return crmXML.isGlobalBoolean(param);
         }
 
         /**
          * Returns type of the global parameter.
          */
         protected String getParamType(final String param) {
             return crmXML.getGlobalParamType(param);
         }
 
         /**
          * Returns section to which the global parameter belongs.
          */
         protected String getSection(final String param) {
             return crmXML.getGlobalSection(param);
         }
 
         /**
          * Applies changes that user has entered.
          */
         public void apply() {
             final String[] params = getParametersFromXML();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                 }
             });
 
             /* update heartbeat */
             final Map<String,String> args = new HashMap<String,String>();
             for (String param : params) {
                 //final String oldValue = getResource().getValue(param);
                 String value = getComboBoxValue(param);
                 //if (value.equals(oldValue)) {
                 //    continue;
                 //}
                 if (value.equals(getParamDefault(param))) {
                     continue;
                 }
 
                 if ("".equals(value)) {
                     continue;
                 }
                 args.put(param, value);
             }
             CRM.setGlobalParameters(getDCHost(), args);
             storeComboBoxValues(params);
         }
 
         /**
          * Sets heartbeat global parameters after they were obtained.
          */
         public void setGlobalConfig() {
             final String[] params = getParametersFromXML();
             for (String param : params) {
                 String value = clusterStatus.getGlobalParam(param);
                 final String oldValue = getResource().getValue(param);
                 if (value != null && !value.equals(oldValue)) {
                     getResource().setValue(param, value);
                     final GuiComboBox cb = paramComboBoxGet(param, null);
                     if (cb != null) {
                         cb.setValue(value);
                     }
                 }
             }
             final Host[] hosts = cluster.getHostsArray();
             for (Host host : hosts) {
                 if (!clusterStatus.isActiveNode(host.getName())) {
                     //TODO: something's missing here
                     //System.out.println("is active node: " + host.getName());
                 }
             }
             if (infoPanel == null) {
                 selectMyself();
             }
         }
 
         /**
          * Check if this connection is filesystem with drbd ra and if so, set
          * it.
          */
         private void setFilesystemWithDrbd(final ServiceInfo siP,
                                            final ServiceInfo si) {
             DrbdResourceInfo dri;
             if (siP.getResourceAgent().isLinbitDrbd()) {
                 /* linbit::drbd -> Filesystem */
                 ((FilesystemInfo) si).setLinbitDrbdInfo((LinbitDrbdInfo) siP);
                 dri = drbdResHash.get(((LinbitDrbdInfo) siP).getResourceName());
             } else {
                 /* drbddisk -> Filesystem */
                 ((FilesystemInfo) si).setDrbddiskInfo((DrbddiskInfo) siP);
                 dri = drbdResHash.get(((DrbddiskInfo) siP).getResourceName());
             }
             if (dri != null) {
                 dri.setUsedByCRM(true);
             }
         }
 
         /**
          * This functions goes through all services, constrains etc. in
          * clusterStatus and updates the internal structures and graph.
          */
         public void setAllResources() {
             final String[] allGroups = clusterStatus.getAllGroups();
             heartbeatGraph.clearVertexIsPresentList();
             List<ServiceInfo> groupServiceIsPresent =
                                                 new ArrayList<ServiceInfo>();
             groupServiceIsPresent.clear();
             for (String group : allGroups) {
                 //TODO: need hb class here
                 for (String hbId : clusterStatus.getGroupResources(group)) {
                     final ResourceAgent newRA =
                                          clusterStatus.getResourceType(hbId);
                     if (newRA == null) {
                         /* This is bad. There is a service but we do not have
                          * the heartbeat script of this service or the we look
                          * in the wrong places.
                          */
                         Tools.appWarning(hbId + ": could not find hb script");
                         continue;
                     }
                     GroupInfo newGi = null;
                     CloneInfo newCi = null;
                     if (!"none".equals(group)) {
                         if (clusterStatus.isClone(group)) {
                             /* clone */
                             newCi = (CloneInfo) heartbeatIdToServiceInfo.get(
                                                                         group);
                             if (newCi == null) {
                                 final Point2D p = null;
                                 newCi = (CloneInfo) heartbeatGraph.getServicesInfo().addServicePanel(
                                             crmXML.getHbClone(),
                                             p,
                                             true,
                                             group);
                                 newCi.getService().setNew(false);
                                 addToHeartbeatIdList(newCi);
                                 final Map<String, String> resourceNode =
                                       clusterStatus.getParamValuePairs(
                                           newCi.getService().getHeartbeatId());
                                 newCi.setParameters(resourceNode);
                             } else {
                                 final Map<String, String> resourceNode =
                                       clusterStatus.getParamValuePairs(
                                           newCi.getService().getHeartbeatId());
                                 newCi.setParameters(resourceNode);
                                 newCi.setUpdated(false);
                                 heartbeatGraph.repaint();
                             }
                             heartbeatGraph.setVertexIsPresent(newCi);
                         } else {
                             /* group */
                             newGi = (GroupInfo) heartbeatIdToServiceInfo.get(group);
                             if (newGi == null) {
                                 final Point2D p = null;
                                 newGi = (GroupInfo) heartbeatGraph.getServicesInfo().addServicePanel(
                                             crmXML.getHbGroup(),
                                             p,
                                             true,
                                             group);
                                 //newGi.getService().setHeartbeatId(group); // TODO: to late
                                 newGi.getService().setNew(false);
                                 addToHeartbeatIdList(newGi);
                                 final Map<String, String> resourceNode =
                                       clusterStatus.getParamValuePairs(
                                           newGi.getService().getHeartbeatId());
                                 newGi.setParameters(resourceNode);
                             } else {
                                 final Map<String, String> resourceNode =
                                         clusterStatus.getParamValuePairs(
                                           newGi.getService().getHeartbeatId());
                                 newGi.setParameters(resourceNode);
                                 newGi.setUpdated(false);
                                 heartbeatGraph.repaint();
                             }
                             heartbeatGraph.setVertexIsPresent(newGi);
                         }
                     }
                     /* continue of creating/updating of the
                      * service in the gui.
                      */
                     ServiceInfo newSi = heartbeatIdToServiceInfo.get(hbId);
                     final Map<String, String> resourceNode =
                                     clusterStatus.getParamValuePairs(hbId);
                     if (newSi == null) {
                         // TODO: get rid of the service name? (everywhere)
                         final String serviceName = newRA.getName();
                         if (newRA.isFilesystem()) {
                             newSi = new FilesystemInfo(serviceName,
                                                        newRA,
                                                        hbId,
                                                        resourceNode);
                         } else if (newRA.isLinbitDrbd()) {
                             newSi = new LinbitDrbdInfo(serviceName,
                                                        newRA,
                                                        hbId,
                                                        resourceNode);
                         } else if (newRA.isDrbddisk()) {
                             newSi = new DrbddiskInfo(serviceName,
                                                      newRA,
                                                      hbId,
                                                      resourceNode);
                         } else if (newRA.isIPaddr()) {
                             newSi = new IPaddrInfo(serviceName,
                                                    newRA,
                                                    hbId,
                                                    resourceNode);
 
                         } else {
                             newSi = new ServiceInfo(serviceName,
                                                     newRA,
                                                     hbId,
                                                     resourceNode);
                         }
                         newSi.getService().setHeartbeatId(hbId);
                         addToHeartbeatIdList(newSi);
                         final Point2D p = null;
 
                         if (newGi != null) {
                             newGi.addGroupServicePanel(newSi);
                         } else if (newCi != null) {
                             newCi.addCloneServicePanel(newSi);
                         } else {
                             heartbeatGraph.getServicesInfo().addServicePanel(
                                                                         newSi,
                                                                         p,
                                                                         true);
                         }
                     } else {
                         newSi.setParameters(resourceNode);
                         newSi.setUpdated(false);
                         heartbeatGraph.repaint();
                     }
                     newSi.getService().setNew(false);
                     newSi.getTypeRadioGroup().setEnabled(false);
                     heartbeatGraph.setVertexIsPresent(newSi);
                     if (newGi != null || newCi != null) {
                         groupServiceIsPresent.add(newSi);
                     }
                 }
             }
             heartbeatGraph.clearColocationList();
             final Map<String, List<String>> colocationMap =
                                             clusterStatus.getColocationMap();
             for (final String heartbeatIdP : colocationMap.keySet()) {
                 final List<String> tos = colocationMap.get(heartbeatIdP);
                 for (final String heartbeatId : tos) {
                     final ServiceInfo si  =
                                     heartbeatIdToServiceInfo.get(heartbeatId);
                     final ServiceInfo siP =
                                     heartbeatIdToServiceInfo.get(heartbeatIdP);
                     heartbeatGraph.addColocation(siP, si);
                 }
             }
 
             heartbeatGraph.clearOrderList();
             final Map<String, List<String>> orderMap =
                                             clusterStatus.getOrderMap();
             for (final String heartbeatIdP : orderMap.keySet()) {
                 for (final String heartbeatId : orderMap.get(heartbeatIdP)) {
                     final ServiceInfo si =
                             heartbeatIdToServiceInfo.get(heartbeatId);
                     if (si != null) { /* not yet complete */
                         final ServiceInfo siP =
                                     heartbeatIdToServiceInfo.get(heartbeatIdP);
                         if (siP != null && siP.getResourceAgent() != null) {
                             /* dangling orders and colocations */
                             if ((siP.getResourceAgent().isDrbddisk()
                                  || siP.getResourceAgent().isLinbitDrbd()) 
                                 && si.getName().equals("Filesystem")) {
                                 final List<String> colIds =
                                                     colocationMap.get(heartbeatIdP);
                                 // TODO: race here
                                 if (colIds != null) {
                                     for (String colId : colIds) {
                                         if (colId != null
                                             && colId.equals(heartbeatId)) {
                                             setFilesystemWithDrbd(siP, si);
                                         }
                                     }
                                 }
                             }
                             heartbeatGraph.addOrder(siP, si);
                         }
                     }
                 }
             }
 
             final Enumeration e = getNode().children();
             while (e.hasMoreElements()) {
                 DefaultMutableTreeNode n =
                           (DefaultMutableTreeNode) e.nextElement();
                 final ServiceInfo g =
                                    (ServiceInfo) n.getUserObject();
                 if (g.getResourceAgent().isGroup()
                     || g.getResourceAgent().isClone()) {
                     final Enumeration ge = g.getNode().children();
                     while (ge.hasMoreElements()) {
                         DefaultMutableTreeNode gn =
                                   (DefaultMutableTreeNode) ge.nextElement();
                         final ServiceInfo s =
                                            (ServiceInfo) gn.getUserObject();
                         if (!groupServiceIsPresent.contains(s)
                             && !s.getService().isNew()) {
                             /* remove the group service from the menu that does
                              * not exist anymore. */
                             s.removeInfo();
                         }
                     }
                 }
             }
             heartbeatGraph.killRemovedEdges();
             heartbeatGraph.killRemovedVertices();
         }
 
         /**
          * Clears the info panel cache, forcing it to reload.
          */
         public boolean selectAutomaticallyInTreeMenu() {
             return infoPanel == null;
         }
 
         /**
          * Returns type of the info text. text/plain or text/html.
          */
         protected String getInfoType() {
             return "text/html";
         }
 
         /**
          * Returns info for info panel, that hb status failed or null, in which
          * case the getInfoPanel() function will show.
          */
         public String getInfo() {
             if (clStatusFailed()) {
                 return Tools.getString("ClusterBrowser.ClStatusFailed");
             }
             return null;
         }
 
         /**
          * Returns editable info panel for global heartbeat config.
          */
         public JComponent getInfoPanel() {
             /* if don't have hb status we don't have all the info we need here.
              * TODO: OR we need to get hb status only once
              */
             if (clStatusFailed()) {
                 return super.getInfoPanel();
             }
             if (infoPanel != null) {
                 heartbeatGraph.pickBackground();
                 return infoPanel;
             }
             infoPanel = new JPanel();
             infoPanel.setBackground(PANEL_BACKGROUND);
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             if (crmXML == null) {
                 return infoPanel;
             }
             final JPanel mainPanel = new JPanel();
             mainPanel.setBackground(PANEL_BACKGROUND);
             mainPanel.setLayout(new BoxLayout(mainPanel,
                                               BoxLayout.Y_AXIS));
             final JPanel optionsPanel = new JPanel();
             optionsPanel.setBackground(PANEL_BACKGROUND);
             optionsPanel.setLayout(new BoxLayout(optionsPanel,
                                                  BoxLayout.Y_AXIS));
             optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             final JPanel extraOptionsPanel = new JPanel();
             extraOptionsPanel.setBackground(EXTRA_PANEL_BACKGROUND);
             extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel,
                                                       BoxLayout.Y_AXIS));
             extraOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
 
             final JPanel buttonPanel = new JPanel(new BorderLayout());
             buttonPanel.setBackground(STATUS_BACKGROUND);
             buttonPanel.setMinimumSize(new Dimension(0, 50));
             buttonPanel.setPreferredSize(new Dimension(0, 50));
             buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             final JMenu serviceCombo = getActionsMenu();
             updateMenus(null);
             mb.add(serviceCombo);
             buttonPanel.add(mb, BorderLayout.EAST);
 
             infoPanel.add(buttonPanel);
 
             final String[] params = getParametersFromXML();
             addParams(optionsPanel,
                       extraOptionsPanel,
                       params,
                       Tools.getDefaultInt("ClusterBrowser.DrbdResLabelWidth"),
                       Tools.getDefaultInt("ClusterBrowser.DrbdResFieldWidth")
                       );
 
             applyButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         final Thread thread = new Thread(
                             new Runnable() {
                                 public void run() {
                                     clStatusLock();
                                     apply();
                                     clStatusUnlock();
                                 }
                             }
                         );
                         thread.start();
                     }
                 }
             );
 
             /* apply button */
             addApplyButton(buttonPanel);
             applyButton.setEnabled(checkResourceFields(null, params));
             /* expert mode */
             buttonPanel.add(Tools.expertModeButton(extraOptionsPanel));
 
             mainPanel.add(optionsPanel);
             mainPanel.add(extraOptionsPanel);
 
             infoPanel.add(new JScrollPane(mainPanel));
             infoPanel.add(Box.createVerticalGlue());
 
             heartbeatGraph.pickBackground();
             return infoPanel;
         }
 
         /**
          * Returns heartbeat graph.
          */
         public JPanel getGraphicalView() {
             return heartbeatGraph.getGraphPanel();
         }
 
         /**
          * Adds service to the list of services.
          * TODO: are they both used?
          */
         public ServiceInfo addServicePanel(final ResourceAgent newRA,
                                            final Point2D pos,
                                            final boolean reloadNode,
                                            final String heartbeatId) {
             ServiceInfo newServiceInfo;
             final String name = newRA.getName();
             if (newRA.isFilesystem()) {
                 newServiceInfo = new FilesystemInfo(name, newRA);
             } else if (newRA.isLinbitDrbd()) {
                 newServiceInfo = new LinbitDrbdInfo(name, newRA);
             } else if (newRA.isDrbddisk()) {
                 newServiceInfo = new DrbddiskInfo(name, newRA);
             } else if (newRA.isIPaddr()) {
                 newServiceInfo = new IPaddrInfo(name, newRA);
             } else if (newRA.isGroup()) {
                 newServiceInfo = new GroupInfo(newRA);
             } else if (newRA.isClone()) {
                 final boolean master = clusterStatus.isMaster(heartbeatId);
                 String cloneName;
                 if (master) {
                     cloneName = PM_MASTER_SLAVE_SET_NAME;
                 } else {
                     cloneName = PM_CLONE_SET_NAME;
                 }
                 newServiceInfo = new CloneInfo(newRA, cloneName, master);
             } else {
                 newServiceInfo = new ServiceInfo(name, newRA);
             }
             if (heartbeatId != null) {
                 addToHeartbeatIdList(newServiceInfo);
                 newServiceInfo.getService().setHeartbeatId(heartbeatId);
             }
             addServicePanel(newServiceInfo, pos, reloadNode);
             return newServiceInfo;
         }
 
         /**
          * Adds new service to the specified position. If position is null, it
          * will be computed later. ReloadNode specifies if the node in
          * the menu should be reloaded and get uptodate.
          */
         public void addServicePanel(final ServiceInfo newServiceInfo,
                                     final Point2D pos,
                                     final boolean reloadNode) {
 
             newServiceInfo.getService().setResourceClass(
                     newServiceInfo.getResourceAgent().getResourceClass());
 
             if (!heartbeatGraph.addResource(newServiceInfo, null, pos)) {
                 addNameToServiceInfoHash(newServiceInfo);
                 final DefaultMutableTreeNode newServiceNode =
                                     new DefaultMutableTreeNode(newServiceInfo);
                 newServiceInfo.setNode(newServiceNode);
                 servicesNode.add(newServiceNode);
                 if (reloadNode) {
                     /* show it */
                     reload(servicesNode);
                     reload(newServiceNode);
                 }
                 //heartbeatGraph.scale();
             }
             if (newServiceInfo.getResourceAgent().isMasterSlave()) {
                 newServiceInfo.changeType("Master / Slave");
             }
             heartbeatGraph.reloadServiceMenus();
         }
 
 
         /**
          * Returns 'add service' list for graph popup menu.
          */
         public List<ResourceAgent> getAddServiceList(final String cl) {
             return globalGetAddServiceList(cl);
         }
 
         /**
          * Returns background popup. Click on background represents cluster as
          * whole.
          */
         public List<UpdatableItem> createPopup() {
             final List<UpdatableItem> items = new ArrayList<UpdatableItem>();
 
             final MyMenuItem removeMenuItem = new MyMenuItem(
                     Tools.getString(
                             "ClusterBrowser.Hb.RemoveAllServices"),
                     REMOVE_ICON) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     return !getExistingServiceList().isEmpty();
                 }
 
                 public void action() {
                     SwingUtilities.invokeLater(new Runnable() {
                         public void run() {
                             getPopup().setVisible(false);
                         }
                     });
                     if (Tools.confirmDialog(
                          Tools.getString(
                              "ClusterBrowser.confirmRemoveAllServices.Title"),
                          Tools.getString(
                          "ClusterBrowser.confirmRemoveAllServices.Description"),
                          Tools.getString(
                              "ClusterBrowser.confirmRemoveAllServices.Yes"),
                          Tools.getString(
                              "ClusterBrowser.confirmRemoveAllServices.No"))) {
 
                         for (ServiceInfo si : getExistingServiceList()) {
                             if (si.getGroupInfo() == null) {
                                 si.removeMyselfNoConfirm();
                             }
                         }
                         heartbeatGraph.getVisualizationViewer().repaint();
                     }
                 }
             };
             items.add((UpdatableItem) removeMenuItem);
             registerMenuItem((UpdatableItem) removeMenuItem);
 
             /* add group */
             final MyMenuItem addGroupMenuItem =
                 new MyMenuItem(Tools.getString("ClusterBrowser.Hb.AddGroup"),
                                null,
                                null) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         return true;
                     }
 
                     public void action() {
                         SwingUtilities.invokeLater(new Runnable() {
                             public void run() {
                                 getPopup().setVisible(false);
                             }
                         });
                         final StringInfo gi = new StringInfo(PM_GROUP_NAME,
                                                              PM_GROUP_NAME);
                         addServicePanel(crmXML.getHbGroup(),
                                         getPos(),
                                         true,
                                         null);
                         heartbeatGraph.getVisualizationViewer().repaint();
                     }
                 };
             items.add((UpdatableItem) addGroupMenuItem);
             registerMenuItem((UpdatableItem) addGroupMenuItem);
 
             /* add service */
             final MyMenu addServiceMenuItem = new MyMenu(
                             Tools.getString("ClusterBrowser.Hb.AddService")) {
                 private static final long serialVersionUID = 1L;
 
                 public void update() {
                     super.update();
                     removeAll();
                     Point2D pos = getPos();
                     final ResourceAgent fsService =
                                 crmXML.getResourceAgent("Filesystem",
                                                         HB_HEARTBEAT_PROVIDER,
                                                         "ocf");
                     if (crmXML.isLinbitDrbdPresent()) {/* just skip it,
                                                                 if it is not */
                         final ResourceAgent linbitDrbdService =
                                                        crmXML.getHbLinbitDrbd();
                         final MyMenuItem ldMenuItem = new MyMenuItem(
                          Tools.getString("ClusterBrowser.linbitDrbdMenuName")) {
                             private static final long serialVersionUID = 1L;
                             public void action() {
                                 SwingUtilities.invokeLater(new Runnable() {
                                     public void run() {
                                         getPopup().setVisible(false);
                                     }
                                 });
                                 if (!linbitDrbdConfirmDialog()) {
                                     return;
                                 }
 
                                 final FilesystemInfo fsi = (FilesystemInfo)
                                                                addServicePanel(
                                                                     fsService,
                                                                     getPos(),
                                                                     true,
                                                                     null);
                                 fsi.setDrbddiskIsPreferred(false);
                                 heartbeatGraph.getVisualizationViewer().repaint();
                             }
                         };
                         if (isOneDrbddisk()
                             || !crmXML.isLinbitDrbdPresent()) {
                             ldMenuItem.setEnabled(false);
                         }
                         ldMenuItem.setPos(pos);
                         add(ldMenuItem);
                     }
                     if (crmXML.isDrbddiskPresent()) {/* just skip it,
                                                            if it is not */
                         final ResourceAgent drbddiskService =
                                                     crmXML.getHbDrbddisk();
                         final MyMenuItem ddMenuItem = new MyMenuItem(
                          Tools.getString("ClusterBrowser.DrbddiskMenuName")) {
                             private static final long serialVersionUID = 1L;
                             public void action() {
                                 SwingUtilities.invokeLater(new Runnable() {
                                     public void run() {
                                         getPopup().setVisible(false);
                                     }
                                 });
                                 final FilesystemInfo fsi = (FilesystemInfo)
                                                                addServicePanel(
                                                                     fsService,
                                                                     getPos(),
                                                                     true,
                                                                     null);
                                 fsi.setDrbddiskIsPreferred(true);
                                 heartbeatGraph.getVisualizationViewer().repaint();
                             }
                         };
                         if (isOneLinbitDrbd()
                             || !crmXML.isDrbddiskPresent()) {
                             ddMenuItem.setEnabled(false);
                         }
                         ddMenuItem.setPos(pos);
                         add(ddMenuItem);
                     }
                     final ResourceAgent ipService = crmXML.getResourceAgent(
                                                          "IPaddr2",
                                                          HB_HEARTBEAT_PROVIDER,
                                                          "ocf");
                     if (ipService != null) { /* just skip it, if it is not*/
                         final MyMenuItem ipMenuItem =
                                new MyMenuItem(ipService.getMenuName()) {
                             private static final long serialVersionUID = 1L;
                             public void action() {
                                 SwingUtilities.invokeLater(new Runnable() {
                                     public void run() {
                                         getPopup().setVisible(false);
                                     }
                                 });
                                 addServicePanel(ipService,
                                                 getPos(),
                                                 true, null);
                                 heartbeatGraph.getVisualizationViewer().repaint();
                             }
                         };
                         ipMenuItem.setPos(pos);
                         add(ipMenuItem);
                     }
                     for (final String cl : HB_CLASSES) {
                         final MyMenu classItem =
                                             new MyMenu(HB_CLASS_MENU.get(cl));
                         DefaultListModel m = new DefaultListModel();
                         for (final ResourceAgent ra : getAddServiceList(cl)) {
                             final MyMenuItem mmi =
                                     new MyMenuItem(ra.getMenuName()) {
                                 private static final long serialVersionUID = 1L;
                                 public void action() {
                                     SwingUtilities.invokeLater(new Runnable() {
                                         public void run() {
                                             getPopup().setVisible(false);
                                         }
                                     });
                                     if (ra.isLinbitDrbd()
                                         && !linbitDrbdConfirmDialog()) {
                                         return;
                                     }
                                     addServicePanel(ra, getPos(), true, null);
                                     heartbeatGraph.repaint();
                                 }
                             };
                             mmi.setPos(pos);
                             m.addElement(mmi);
                         }
                         classItem.add(Tools.getScrollingMenu(classItem, m));
                         add(classItem);
                     }
                 }
             };
             items.add((UpdatableItem) addServiceMenuItem);
             registerMenuItem((UpdatableItem) addServiceMenuItem);
 
             /* view logs */
             final MyMenuItem viewLogsItem =
                 new MyMenuItem(Tools.getString("ClusterBrowser.Hb.ViewLogs"),
                                null,
                                null) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         return true;
                     }
 
                     public void action() {
                         ClusterLogs l = new ClusterLogs(getCluster());
                         l.showDialog();
                     }
                 };
             items.add((UpdatableItem) viewLogsItem);
             registerMenuItem((UpdatableItem) viewLogsItem);
             return items;
         }
 
         /**
          * Return list of all services.
          */
         public List<ServiceInfo> getExistingServiceList() {
             final List<ServiceInfo> existingServiceList =
                                             new ArrayList<ServiceInfo>();
             for (final String name : nameToServiceInfoHash.keySet()) {
                 final Map<String, ServiceInfo> idHash =
                                             nameToServiceInfoHash.get(name);
                 for (final String id : idHash.keySet()) {
                     final ServiceInfo si = idHash.get(id);
                     existingServiceList.add(si);
                 }
             }
             return existingServiceList;
         }
 
         /**
          * Returns units.
          */
         protected final Unit[] getUnits() {
             return new Unit[]{
                 new Unit("", "", "", ""),
                 new Unit("ms",  "ms", "Millisecond", "Milliseconds"),
                 new Unit("us",  "us", "Microsecond", "Microseconds"),
                 new Unit("s",   "s",  "Second",      "Seconds"),
                 new Unit("min", "m",  "Minute",      "Minutes"),
                 new Unit("h",   "h",  "Hour",        "Hours")
             };
         }
     }
 
     /**
      * Returns nw hb connection info object. This is called from heartbeat
      * graph.
      */
     public final HbConnectionInfo getNewHbConnectionInfo() {
         return new HbConnectionInfo();
     }
 
     /**
      * Interface for either order or colocation constraint.
      */
     private interface HbConstraintInterface {
         /** Returns true if it is order, false if colocation. */
         boolean isOrder();
         String[] getParametersFromXML();
         void addParams(final JPanel optionsPanel,
                        final JPanel extraOptionsPanel,
                        final String[] params,
                        final int leftWidth,
                        final int rightWidth);
         boolean checkResourceFields(final String param,
                                     final String[] params);
         boolean checkResourceFieldsCorrect(final String param,
                                            final String[] params);
         boolean checkResourceFieldsChanged(final String param,
                                            final String[] params);
         void apply();
         Service getService();
         void addField(final JPanel panel,
                       final JComponent left,
                       final JComponent right,
                       final int leftWidth,
                       final int rightWidth);
         void addLabelField(final JPanel panel,
                            final String left,
                            final String right,
                            final int leftWidth,
                            final int rightWidth);
         String getName();
         String getRsc1();
         String getRsc2();
     }
     
     /**
      * Object that holds an order constraint information.
      */
     private class HbOrderInfo extends EditableInfo
                              implements HbConstraintInterface {
         /** Parent resource in order constraint. */
         private ServiceInfo serviceInfoParent;
         /** Child resource in order constraint. */
         private ServiceInfo serviceInfoChild;
         /** Cache for the info panel. */
         private JComponent infoPanel = null;
         /** Connection that keeps this constraint. */
         private HbConnectionInfo connectionInfo;
 
         /**
          * Prepares a new <code>HbOrderInfo</code> object.
          */
         public HbOrderInfo(final HbConnectionInfo connectionInfo,
                            final ServiceInfo serviceInfoParent,
                            final ServiceInfo serviceInfoChild) {
             super("Order");
             setResource(new Service("Order"));
             this.connectionInfo = connectionInfo;
             this.serviceInfoParent = serviceInfoParent;
             this.serviceInfoChild = serviceInfoChild;
         }
 
         /**
          * Sets the order's parameters.
          */
         public final void setParameters() {
             final String rscParent = 
                             serviceInfoParent.getService().getHeartbeatId();
             final String rscChild = 
                             serviceInfoChild.getService().getHeartbeatId();
             final String score = clusterStatus.getOrderScore(rscParent,
                                                                rscChild);
             final String symmetrical = clusterStatus.getOrderSymmetrical(
                                                                     rscParent,
                                                                     rscChild);
             final String firstAction = clusterStatus.getOrderFirstAction(
                                                                     rscParent,
                                                                     rscChild);
             final String thenAction = clusterStatus.getOrderThenAction(
                                                                     rscParent,
                                                                     rscChild);
             final String ordId = clusterStatus.getOrderId(rscParent,
                                                             rscChild);
             final Map<String, String> resourceNode =
                                                 new HashMap<String, String>();
             resourceNode.put("score", score);
             resourceNode.put("symmetrical", symmetrical);
             resourceNode.put("first-action", firstAction);
             resourceNode.put("then-action", thenAction);
 
             final String[] params = crmXML.getOrderParameters();
             if (params != null) {
                 for (String param : params) {
                     String value = resourceNode.get(param);
                     if (value == null) {
                         value = getParamDefault(param);
                     }
                     if ("".equals(value)) {
                         value = null;
                     }
                     final String oldValue = getResource().getValue(param);
                     if ((value == null && value != oldValue)
                         || (value != null && !value.equals(oldValue))) {
                         getResource().setValue(param, value);
                         final GuiComboBox cb = paramComboBoxGet(param, null);
                         if (cb != null) {
                             cb.setValue(value);
                         }
                     }
                 }
             }
         }
         /**
          * Returns that this is order constraint.
          */
         public final boolean isOrder() {
             return true;
         }
 
         /**
          * Returns long description of the parameter, that is used for
          * tool tips.
          */
         protected final String getParamLongDesc(final String param) {
             final String text = crmXML.getOrderParamLongDesc(param);
             return text.replaceAll("@FIRST-RSC@", serviceInfoParent.toString())
                        .replaceAll("@THEN-RSC@", serviceInfoChild.toString());
         }
 
         /**
          * Returns short description of the parameter, that is used as * label.
          */
         protected final String getParamShortDesc(final String param) {
             return crmXML.getOrderParamShortDesc(param);
         }
 
         /**
          * Checks if the new value is correct for the parameter type and
          * constraints.
          */
         protected final boolean checkParam(final String param,
                                      final String newValue) {
             return crmXML.checkOrderParam(param, newValue);
         }
 
         /**
          * Returns default for this parameter.
          */
         protected final String getParamDefault(final String param) {
             return crmXML.getOrderParamDefault(param);
         }
 
         /**
          * Returns preferred value for this parameter.
          */
         protected final String getParamPreferred(final String param) {
             return crmXML.getOrderParamPreferred(param);
         }
 
         /**
          * Returns lsit of all parameters as an array.
          */
         public final String[] getParametersFromXML() {
             return crmXML.getOrderParameters();
         }
 
         /**
          * Possible choices for pulldown menus, or null if it is not a pull
          * down menu.
          */
         protected final Object[] getParamPossibleChoices(final String param) {
             if ("first-action".equals(param)) {
                 return crmXML.getOrderParamPossibleChoices(
                                     param,
                                     serviceInfoParent.getService().isMaster());
             } else if ("then-action".equals(param)) {
                 return crmXML.getOrderParamPossibleChoices(
                                     param,
                                     serviceInfoChild.getService().isMaster());
             } else {
                 return crmXML.getOrderParamPossibleChoices(param, false);
             }
         }
 
         /**
          * Returns parameter type, boolean etc.
          */
         protected final String getParamType(final String param) {
             return crmXML.getOrderParamType(param);
         }
 
         /**
          * Returns section to which the global belongs.
          */
         protected final String getSection(final String param) {
             return crmXML.getOrderSection(param);
         }
 
         /**
          * Returns whether the parameter is of the boolean type and needs the
          * checkbox.
          */
         protected final boolean isCheckBox(final String param) {
             return crmXML.isOrderBoolean(param);
         }
 
         /**
          * Returns true if the specified parameter is of time type.
          */
         protected final boolean isTimeType(final String param) {
             return crmXML.isOrderTimeType(param);
         }
 
         /**
          * Returns true if the specified parameter is integer.
          */
         protected final boolean isInteger(final String param) {
             return crmXML.isOrderInteger(param);
         }
 
         /**
          * Returns true if the specified parameter is required.
          */
         protected final boolean isRequired(final String param) {
             return crmXML.isOrderRequired(param);
         }
 
         /**
          * Checks resource fields of all constraints that are in this
          * connection with this constraint.
          */
         public final boolean checkResourceFields(final String param,
                                            final String[] params) {
             return connectionInfo.checkResourceFields(param, null);
         }
 
         /**
          * Applies changes to the order parameters.
          */
         public final void apply() {
             final String[] params = getParametersFromXML();
             final Map<String, String> attrs =
                                           new LinkedHashMap<String, String>();
             boolean changed = false;
             for (final String param : params) {
                 final String value = getComboBoxValue(param);
                 if (!value.equals(getResource().getValue(param))) {
                     changed = true;
                 }
                 if (!value.equals(getParamDefault(param))) {
                     attrs.put(param, value);
                 }
             }
             if (changed) {
                 CRM.addOrder(getDCHost(),
                              getService().getHeartbeatId(),
                              serviceInfoParent.getService().getHeartbeatId(),
                              serviceInfoChild.getService().getHeartbeatId(),
                              attrs);
                 storeComboBoxValues(params);
             }
         }
 
         /**
          * Returns service that belongs to this info object.
          */
         public final Service getService() {
             return (Service) getResource();
         }
 
         /**
          * Get parent resource in order constraint.
          */
         public final String getRsc1() {
             return serviceInfoParent.toString();
         }
 
         /**
          * Get child resource in order constraint.
          */
         public final String getRsc2() {
             return serviceInfoChild.toString();
         }
     }
 
     /**
      * Object that holds a colocation constraint information.
      */
     private class HbColocationInfo extends EditableInfo
                                   implements HbConstraintInterface {
         /** Resource 1 in colocation constraint. */
         private ServiceInfo serviceInfoRsc;
         /** Resource 2 in colocation constraint. */
         private ServiceInfo serviceInfoWithRsc;
         /** Cache for the info panel. */
         private JComponent infoPanel = null;
         /** Connection that keeps this constraint. */
         private HbConnectionInfo connectionInfo;
 
         /**
          * Prepares a new <code>HbColocationInfo</code> object.
          */
         public HbColocationInfo(final HbConnectionInfo connectionInfo,
                                 final ServiceInfo serviceInfoRsc,
                                 final ServiceInfo serviceInfoWithRsc) {
             super("Colocation");
             setResource(new Service("Colocation"));
             this.connectionInfo = connectionInfo;
             this.serviceInfoRsc = serviceInfoRsc;
             this.serviceInfoWithRsc = serviceInfoWithRsc;
         }
 
         /**
          * Sets the colocation's parameters.
          */
         public final void setParameters() {
             final String rsc = serviceInfoRsc.getService().getHeartbeatId();
             final String withRsc =
                               serviceInfoWithRsc.getService().getHeartbeatId();
             final String score = clusterStatus.getColocationScore(rsc,
                                                                     withRsc);
             final String rscRole = clusterStatus.getColocationRscRole(
                                                                       rsc,
                                                                       withRsc);
             final String withRscRole = clusterStatus.getColocationWithRscRole(
                                                                       rsc,
                                                                       withRsc);
             final Map<String, String> resourceNode =
                                                 new HashMap<String, String>();
             resourceNode.put("score", score);
             resourceNode.put("rsc-role", rscRole);
             resourceNode.put("with-rsc-role", withRscRole);
 
             final String[] params = crmXML.getColocationParameters();
             if (params != null) {
                 for (String param : params) {
                     String value = resourceNode.get(param);
                     if (value == null) {
                         value = getParamDefault(param);
                     }
                     if ("".equals(value)) {
                         value = null;
                     }
                     final String oldValue = getResource().getValue(param); 
                     if ((value == null && value != oldValue)
                         || (value != null && !value.equals(oldValue))) {
                         getResource().setValue(param, value);
                         final GuiComboBox cb = paramComboBoxGet(param, null);
                         if (cb != null) {
                             cb.setValue(value);
                         }
                     }
                 }
             }
         }
 
         /**
          * Returns that this is order constraint.
          */
         public final boolean isOrder() {
             return false;
         }
         /**
          * Returns long description of the parameter, that is used for
          * tool tips.
          */
         protected final String getParamLongDesc(final String param) {
 
             final String text = crmXML.getColocationParamLongDesc(param);
             return text.replaceAll("@RSC@", serviceInfoRsc.toString())
                        .replaceAll("@WITH-RSC@", serviceInfoWithRsc.toString());
         }
 
         /**
          * Returns short description of the parameter, that is used as * label.
          */
         protected final String getParamShortDesc(final String param) {
             return crmXML.getColocationParamShortDesc(param);
         }
 
         /**
          * Checks if the new value is correct for the parameter type and
          * constraints.
          */
         protected final boolean checkParam(final String param,
                                      final String newValue) {
             return crmXML.checkColocationParam(param, newValue);
         }
 
         /**
          * Returns default for this parameter.
          */
         protected final String getParamDefault(final String param) {
             return crmXML.getColocationParamDefault(param);
         }
 
         /**
          * Returns preferred value for this parameter.
          */
         protected final String getParamPreferred(final String param) {
             return crmXML.getColocationParamPreferred(param);
         }
 
         /**
          * Returns lsit of all parameters as an array.
          */
         public final String[] getParametersFromXML() {
             return crmXML.getColocationParameters();
         }
 
         /**
          * Possible choices for pulldown menus, or null if it is not a pull
          * down menu.
          */
         protected final Object[] getParamPossibleChoices(
                                                          final String param) {
             if ("with-rsc-role".equals(param)) {
                 return crmXML.getColocationParamPossibleChoices(
                                     param,
                                     serviceInfoWithRsc.getService().isMaster());
             } else if ("rsc-role".equals(param)) {
                 return crmXML.getColocationParamPossibleChoices(
                                     param,
                                     serviceInfoRsc.getService().isMaster());
             } else {
                 return crmXML.getColocationParamPossibleChoices(param, false);
             }
         }
 
         /**
          * Returns parameter type, boolean etc.
          */
         protected final String getParamType(final String param) {
             return crmXML.getColocationParamType(param);
         }
 
         /**
          * Returns section to which the global belongs.
          */
         protected final String getSection(final String param) {
             return crmXML.getColocationSection(param);
         }
 
         /**
          * Returns whether the parameter is of the boolean type and needs the
          * checkbox.
          */
         protected final boolean isCheckBox(final String param) {
             return crmXML.isColocationBoolean(param);
         }
 
         /**
          * Returns true if the specified parameter is of time type.
          */
         protected final boolean isTimeType(final String param) {
             return crmXML.isColocationTimeType(param);
         }
 
         /**
          * Returns true if the specified parameter is integer.
          */
         protected final boolean isInteger(final String param) {
             return crmXML.isColocationInteger(param);
         }
 
         /**
          * Returns true if the specified parameter is required.
          */
         protected final boolean isRequired(final String param) {
             return crmXML.isColocationRequired(param);
         }
 
         /**
          * Checks resource fields of all constraints that are in this
          * connection with this constraint.
          */
         public final boolean checkResourceFields(final String param,
                                                  final String[] params) {
             return connectionInfo.checkResourceFields(param, null);
         }
 
         /**
          * Applies changes to the colocation parameters.
          */
         public final void apply() {
             final String[] params = getParametersFromXML();
             final Map<String, String> attrs =
                                           new LinkedHashMap<String, String>();
             boolean changed = true;
             for (final String param : params) {
                 final String value = getComboBoxValue(param);
                 if (!value.equals(getResource().getValue(param))) {
                     changed = true;
                 }
                     attrs.put(param, value);
             }
             if (changed) {
                 CRM.addColocation(
                               getDCHost(),
                               getService().getHeartbeatId(),
                               serviceInfoRsc.getService().getHeartbeatId(),
                               serviceInfoWithRsc.getService().getHeartbeatId(),
                               attrs);
             }
             storeComboBoxValues(params);
         }
 
         /**
          * Returns service that belongs to this info object.
          */
         public final Service getService() {
             return (Service) getResource();
         }
 
         /**
          * Resource 1 in colocation constraint.
          */
         public final String getRsc1() {
             return serviceInfoWithRsc.toString();
         }
         /**
          * Resource 2 in colocation constraint.
          */
         public final String getRsc2() {
             return serviceInfoRsc.toString();
         }
     }
 
     /**
      * This class describes a connection between two heartbeat services.
      * It can be order, colocation or both.
      */
     public class HbConnectionInfo extends EditableInfo {
         /** Cache for the info panel. */
         private JComponent infoPanel = null;
         /** Constraints. */
         private final List<HbConstraintInterface> constraints =
                                        new ArrayList<HbConstraintInterface>();
         /** Resource 1 in colocation constraint (the last one). */
         private ServiceInfo lastServiceInfoRsc = null;
         /** Resource 2 in colocation constraint (the last one). */
         private ServiceInfo lastServiceInfoWithRsc = null;
         /** Parent resource in order constraint (the last one). */
         private ServiceInfo lastServiceInfoParent = null;
         /** Child resource in order constraint (the last one). */
         private ServiceInfo lastServiceInfoChild = null;
         /** List of colocation ids. */
         private Map<String, HbColocationInfo> colocationIds =
                                        new HashMap<String, HbColocationInfo>();
         /** List of order ids. */
         private Map<String, HbOrderInfo> orderIds =
                                             new HashMap<String, HbOrderInfo>();
 
         /**
          * Prepares a new <code>HbConnectionInfo</code> object.
          */
         public HbConnectionInfo() {
             super("HbConnectionInfo");
             initApplyButton();
         }
 
         /**
          * Returns long description of the parameter, that is used for
          * tool tips.
          */
         protected final String getParamLongDesc(final String param) {
             return null;
         }
 
         /**
          * Returns short description of the parameter, that is used as * label.
          */
         protected final String getParamShortDesc(final String param) {
             return null;
         }
 
         /**
          * Checks if the new value is correct for the parameter type and
          * constraints.
          */
         protected final boolean checkParam(final String param,
                                      final String newValue) {
             return false;
         }
 
         /**
          * Returns default for this parameter.
          */
         protected final String getParamDefault(final String param) {
             return null;
         }
 
         /**
          * Returns preferred value for this parameter.
          */
         protected final String getParamPreferred(final String param) {
             return null;
         }
 
         /**
          * Returns lsit of all parameters as an array.
          */
         public final String[] getParametersFromXML() {
             return null;
         }
 
         /**
          * Possible choices for pulldown menus, or null if it is not a pull
          * down menu.
          */
         protected final Object[] getParamPossibleChoices(final String param) {
             return null;
         }
 
         /**
          * Returns parameter type, boolean etc.
          */
         protected final String getParamType(final String param) {
             return null;
         }
 
         /**
          * Returns section to which the global belongs.
          */
         protected final String getSection(final String param) {
             return null;
         }
 
         /**
          * Returns whether the parameter is of the boolean type and needs the
          * checkbox.
          */
         protected final boolean isCheckBox(final String param) {
             return false;
         }
 
         /**
          * Returns true if the specified parameter is of time type.
          */
         protected boolean isTimeType(final String param) {
             return false;
         }
 
         /**
          * Returns true if the specified parameter is integer.
          */
         protected final boolean isInteger(final String param) {
             return false;
         }
 
         /**
          * Returns true if the specified parameter is required.
          */
         protected final boolean isRequired(final String param) {
             return true;
         }
 
         /**
          * Returns parent resource in order constraint.
          */
         public final ServiceInfo getLastServiceInfoParent() {
             return lastServiceInfoParent;
         }
 
         /**
          * Returns child resource in order constraint.
          */
         public final ServiceInfo getLastServiceInfoChild() {
             return lastServiceInfoChild;
         }
 
         /**
          * Returns resource 1 in colocation constraint.
          */
         public final ServiceInfo getLastServiceInfoRsc() {
             return lastServiceInfoRsc;
         }
 
         /**
          * Returns resource 2 in colocation constraint.
          */
         public final ServiceInfo getLastServiceInfoWithRsc() {
             return lastServiceInfoWithRsc;
         }
 
         /**
          * Returns heartbeat graphical view.
          */
         public final JPanel getGraphicalView() {
             return heartbeatGraph.getGraphPanel();
         }
 
         /**
          * Applies the changes to the constraints.
          */
         public final void apply() {
             for (final HbConstraintInterface c : constraints) {
                 c.apply();
             }
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                 }
             });
         }
 
         /**
          * Check order and colocation constraints.
          */
         public final boolean checkResourceFields(final String param,
                                                  final String[] params) {
             boolean correct = true;
             for (final HbConstraintInterface c : constraints) {
                 final boolean cor = c.checkResourceFieldsCorrect(
                                       param,
                                       c.getParametersFromXML());
                 if (!cor) {
                     correct = false;
                     break;
                 }
             }
             if (correct) {
                 boolean changed = false;
                 for (final HbConstraintInterface c : constraints) {
                     final boolean chg = c.checkResourceFieldsChanged(
                                       param,
                                       c.getParametersFromXML());
                     if (chg) {
                         changed = true;
                         break;
                     }
                 }
                 return changed;
             }
             return correct;
         }
 
         /**
          * Returns info panel for hb connection (order and/or colocation
          * constraint.
          */
         public final JComponent getInfoPanel() {
             if (infoPanel != null) {
                 return infoPanel;
             }
             final JPanel mainPanel = new JPanel();
             mainPanel.setBackground(PANEL_BACKGROUND);
             mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 
             final JPanel buttonPanel = new JPanel(new BorderLayout());
             buttonPanel.setBackground(STATUS_BACKGROUND);
             buttonPanel.setMinimumSize(new Dimension(0, 50));
             buttonPanel.setPreferredSize(new Dimension(0, 50));
             buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
 
             final JPanel optionsPanel = new JPanel();
             optionsPanel.setBackground(PANEL_BACKGROUND);
             optionsPanel.setLayout(new BoxLayout(optionsPanel,
                                                  BoxLayout.Y_AXIS));
             optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             final JPanel extraOptionsPanel = new JPanel();
             extraOptionsPanel.setBackground(EXTRA_PANEL_BACKGROUND);
             extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel,
                                                       BoxLayout.Y_AXIS));
             extraOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
             mainPanel.add(buttonPanel);
 
             /* Actions */
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             final JMenu serviceCombo = getActionsMenu();
             updateMenus(null);
             mb.add(serviceCombo);
             buttonPanel.add(mb, BorderLayout.EAST);
 
             /* params */
             for (final HbConstraintInterface c : constraints) {
                 final String[] params = c.getParametersFromXML();
                 /* heartbeat id */
                 final JPanel panel = getParamPanel(c.getName());
                 int rows = 3;
                 c.addLabelField(panel,
                                 Tools.getString("ClusterBrowser.HeartbeatId"),
                                 c.getService().getHeartbeatId(),
                                 SERVICE_LABEL_WIDTH,
                                 SERVICE_FIELD_WIDTH);
                 c.addLabelField(panel,
                                 "rsc1",
                                 c.getRsc1(),
                                 SERVICE_LABEL_WIDTH,
                                 SERVICE_FIELD_WIDTH);
                 c.addLabelField(panel,
                                 "rsc2",
                                 c.getRsc2(),
                                 SERVICE_LABEL_WIDTH,
                                 SERVICE_FIELD_WIDTH);
                 SpringUtilities.makeCompactGrid(panel, rows, 2, /* rows, cols */
                                                 1, 1,        /* initX, initY */
                                                 1, 1);       /* xPad, yPad */
 
                 extraOptionsPanel.add(panel);
                 c.addParams(optionsPanel,
                             extraOptionsPanel,
                             params,
                             SERVICE_LABEL_WIDTH,
                             SERVICE_FIELD_WIDTH);
             }
 
             applyButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         Thread thread = new Thread(new Runnable() {
                             public void run() {
                                 clStatusLock();
                                 apply();
                                 clStatusUnlock();
                             }
                         });
                         thread.start();
                     }
                 }
             );
 
             /* apply button */
             addApplyButton(buttonPanel);
             applyButton.setEnabled(checkResourceFields(null, null));
             /* expert mode */
             buttonPanel.add(Tools.expertModeButton(extraOptionsPanel));
 
             mainPanel.add(optionsPanel);
             mainPanel.add(extraOptionsPanel);
 
             infoPanel = new JPanel();
             infoPanel.setBackground(PANEL_BACKGROUND);
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             infoPanel.add(buttonPanel);
             infoPanel.add(new JScrollPane(mainPanel));
             infoPanel.add(Box.createVerticalGlue());
             infoPanel.setMinimumSize(new Dimension(
                     Tools.getDefaultInt("HostBrowser.ResourceInfoArea.Width"),
                     Tools.getDefaultInt("HostBrowser.ResourceInfoArea.Height")
                     ));
             infoPanel.setPreferredSize(new Dimension(
                     Tools.getDefaultInt("HostBrowser.ResourceInfoArea.Width"),
                     Tools.getDefaultInt("HostBrowser.ResourceInfoArea.Height")
                     ));
             return infoPanel;
         }
 
         /**
          * Creates popup menu for heartbeat order and colocation dependencies.
          * These are the edges in the graph.
          */
         public final List<UpdatableItem> createPopup() {
             final List<UpdatableItem> items = new ArrayList<UpdatableItem>();
 
             final HbConnectionInfo thisClass = this;
 
             final MyMenuItem removeEdgeItem = new MyMenuItem(
                          Tools.getString("ClusterBrowser.Hb.RemoveEdge"),
                          REMOVE_ICON,
                          Tools.getString("ClusterBrowser.Hb.RemoveEdge.ToolTip")
                         ) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean enablePredicate() {
                     return true;
                 }
 
                 public void action() {
                     heartbeatGraph.removeConnection(thisClass);
                 }
             };
             registerMenuItem(removeEdgeItem);
             items.add(removeEdgeItem);
 
             /* remove/add order */
             final MyMenuItem removeOrderItem =
                 new MyMenuItem(Tools.getString("ClusterBrowser.Hb.RemoveOrder"),
                     REMOVE_ICON,
                     Tools.getString("ClusterBrowser.Hb.RemoveOrder.ToolTip"),
 
                     Tools.getString("ClusterBrowser.Hb.AddOrder"),
                     null,
                     Tools.getString("ClusterBrowser.Hb.AddOrder.ToolTip")) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean predicate() {
                     return heartbeatGraph.isOrder(thisClass);
                 }
 
                 public boolean enablePredicate() {
                     return true;
                 }
 
                 public void action() {
                     if (this.getText().equals(Tools.getString(
                                             "ClusterBrowser.Hb.RemoveOrder"))) {
                         heartbeatGraph.removeOrder(thisClass);
                     } else {
                         /* there is colocation constraint so let's get the
                          * endpoints from it. */
                         addOrder(getLastServiceInfoRsc(),
                                  getLastServiceInfoWithRsc());
                         heartbeatGraph.addOrder(thisClass);
                     }
                 }
             };
 
             registerMenuItem(removeOrderItem);
             items.add(removeOrderItem);
 
             /* remove/add colocation */
             final MyMenuItem removeColocationItem =
                     new MyMenuItem(
                         Tools.getString("ClusterBrowser.Hb.RemoveColocation"),
                         REMOVE_ICON,
                         Tools.getString(
                                 "ClusterBrowser.Hb.RemoveColocation.ToolTip"),
 
                         Tools.getString("ClusterBrowser.Hb.AddColocation"),
                         null,
                         Tools.getString(
                                 "ClusterBrowser.Hb.AddColocation.ToolTip")
                        ) {
                 private static final long serialVersionUID = 1L;
 
                 public boolean predicate() {
                     return heartbeatGraph.isColocation(thisClass);
                 }
 
                 public boolean enablePredicate() {
                     return true;
                 }
 
                 public void action() {
                     if (this.getText().equals(Tools.getString(
                                        "ClusterBrowser.Hb.RemoveColocation"))) {
                         heartbeatGraph.removeColocation(thisClass);
                     } else {
                         /* add colocation */
                         /* there is order constraint so let's get the endpoints
                          * from it. */
                         addColocation(getLastServiceInfoParent(),
                                       getLastServiceInfoChild());
                         heartbeatGraph.addColocation(thisClass);
                     }
                 }
             };
 
             registerMenuItem(removeColocationItem);
             items.add(removeColocationItem);
             ///* TODO: reverse order */
             //final MyMenuItem reverseOrderItem =
             //    new MyMenuItem(
             //        Tools.getString("ClusterBrowser.Hb.ReverseOrder"),
             //        null,
             //        Tools.getString("ClusterBrowser.Hb.ReverseOrder.ToolTip")) {
             //    private static final long serialVersionUID = 1L;
 
             //    public boolean enablePredicate() {
             //        return heartbeatGraph.isOrder(thisClass);
             //    }
 
             //    public void action() {
             //        ServiceInfo child = lastServiceInfoChild;
             //        ServiceInfo parent = lastServiceInfoParent;
             //        heartbeatGraph.removeOrder(thisClass);
             //        // TODO: or should I wait till it is really removed?
             //        try {
             //            Thread.sleep(1000);
             //        } catch (InterruptedException ex) {
             //            Thread.currentThread().interrupt();
             //        }
             //        //serviceInfoChild = parent;
             //        //serviceInfoParent = child;
             //        constraints.removeOrders();
             //        heartbeatGraph.addOrder(thisClass);
             //    }
             //};
             //registerMenuItem(reverseOrderItem);
             //items.add(reverseOrderItem);
             return items;
         }
 
         /**
          * Removes colocations or orders.
          */
         private void removeOrdersOrColocations(boolean isOrder) {
             final List<HbConstraintInterface> constraintsToRemove =
                                         new ArrayList<HbConstraintInterface>();
             for (final HbConstraintInterface c : constraints) {
                 if (c.isOrder() == isOrder) {
                     constraintsToRemove.add(c);
                 }
             }
             for (final HbConstraintInterface c : constraintsToRemove) {
                constraints.remove(c);
             }
         }
 
         /**
          * Removes all orders.
          */
         public final void removeOrders() {
             removeOrdersOrColocations(true);
         }
 
         /**
          * Removes all colocations.
          */
         public final void removeColocations() {
             removeOrdersOrColocations(false);
         }
 
         /**
          * Adds a new order.
          */
         public final void addOrder(final ServiceInfo serviceInfoParent,
                                    final ServiceInfo serviceInfoChild) {
             final String ordId = clusterStatus.getOrderId(
                             serviceInfoParent.getService().getHeartbeatId(),
                             serviceInfoChild.getService().getHeartbeatId());
 
             if (ordId == null) {
                 /* we'll get it later with id. */
                 return;
             }
             if (orderIds.containsKey(ordId)) {
                 orderIds.get(ordId).setParameters();
                 return;
             }
             lastServiceInfoParent = serviceInfoParent;
             lastServiceInfoChild = serviceInfoChild;
             final HbOrderInfo oi = new HbOrderInfo(this,
                                                    serviceInfoParent,
                                                    serviceInfoChild);
             orderIds.put(ordId, oi);
             oi.getService().setHeartbeatId(ordId);
             oi.applyButton = applyButton;
             oi.setParameters();
             constraints.add(oi);
         }
 
         /**
          * Adds a new colocation.
          */
         public final void addColocation(final ServiceInfo serviceInfoRsc,
                                         final ServiceInfo serviceInfoWithRsc) {
             final String colId =
             clusterStatus.getColocationId(
                              serviceInfoRsc.getService().getHeartbeatId(),
                              serviceInfoWithRsc.getService().getHeartbeatId());
             if (colId == null) {
                 /* we'll get it later with id. */
                 return;
             }
             if (colocationIds.containsKey(colId)) {
                 colocationIds.get(colId).setParameters();
                 return;
             }
             lastServiceInfoRsc = serviceInfoRsc;
             lastServiceInfoWithRsc = serviceInfoWithRsc;
             final HbColocationInfo ci = new HbColocationInfo(
                                                        this,
                                                        serviceInfoRsc,
                                                        serviceInfoWithRsc);
             colocationIds.put(colId, ci);
             ci.getService().setHeartbeatId(colId);
             ci.applyButton = applyButton;
             ci.setParameters();
             constraints.add(ci);
         }
     }
 
     /**
      * This class provides drbd info. For one it shows the editable global
      * drbd config, but if a drbd block device is selected it forwards to the
      * block device info, which is defined in HostBrowser.java.
      */
     public class DrbdInfo extends EditableInfo {
         /** Selected block device. */
         private BlockDevInfo selectedBD = null;
         /** Cache for the info panel. */
         private JComponent infoPanel    = null;
 
         /**
          * Prepares a new <code>DrbdInfo</code> object.
          *
          * @param name
          *      name that will be shown in the tree
          */
         public DrbdInfo(final String name) {
             super(name);
             setResource(new Resource(name));
             drbdGraph.setDrbdInfo(this);
             initApplyButton();
         }
 
         /**
          * Returns menu drbd icon.
          */
         public final ImageIcon getMenuIcon() {
             return null;
         }
 
         /**
          * Sets selected block device.
          */
         public final void setSelectedNode(final BlockDevInfo bdi) {
             this.selectedBD = bdi;
         }
 
         /**
          * Gets combo box for paremeter in te global config. usage-count is
          * disabled.
          */
         protected final GuiComboBox getParamComboBox(final String param,
                                                      final String prefix,
                                                      final int width) {
             final GuiComboBox cb = super.getParamComboBox(param, prefix, width);
             if ("usage-count".equals(param)) {
                 cb.setEnabled(false);
             }
             return cb;
         }
 
         /**
          * Creates drbd config.
          */
         public final void createDrbdConfig()
                    throws Exceptions.DrbdConfigException {
             final StringBuffer config = new StringBuffer(160);
             config.append("## generated by drbd-gui ");
             config.append(Tools.getRelease());
             config.append("\n\n");
 
             final StringBuffer global = new StringBuffer(80);
             final String[] params = drbdXML.getGlobalParams();
             global.append("global {\n");
             for (String param : params) {
                 if ("usage-count".equals(param)) {
                     final String value = "yes";
                     global.append("\t\t");
                     global.append(param);
                     global.append('\t');
                     global.append(Tools.escapeConfig(value));
                     global.append(";\n");
                 } else {
                     String value = getResource().getValue(param);
                     if (value == null && "usage-count".equals(param)) {
                         value = "yes";
                     }
                     if (value == null) {
                         continue;
                     }
                     if (!value.equals(drbdXML.getParamDefault(param))) {
                         global.append("\t\t");
                         global.append(param);
                         global.append('\t');
                         global.append(Tools.escapeConfig(value));
                         global.append(";\n");
                     }
                 }
             }
             global.append("}\n");
             if (global.length() > 0) {
                 config.append(global);
             }
             final Host[] hosts = cluster.getHostsArray();
             for (Host host : hosts) {
                 final StringBuffer resConfig = new StringBuffer("");
                 final Enumeration drbdResources = drbdNode.children();
                 while (drbdResources.hasMoreElements()) {
                     DefaultMutableTreeNode n =
                            (DefaultMutableTreeNode) drbdResources.nextElement();
                     final DrbdResourceInfo drbdRes =
                                         (DrbdResourceInfo) n.getUserObject();
                     if (drbdRes.resourceInHost(host)) {
                         resConfig.append('\n');
                         try {
                             resConfig.append(drbdRes.drbdResourceConfig());
                         } catch (Exceptions.DrbdConfigException dce) {
                             throw dce;
                         }
                     }
                 }
                 host.getSSH().createConfig(config.toString()
                                              + resConfig.toString(),
                                            "drbd.conf",
                                            "/etc/",
                                            "0600",
                                            true);
                 //DRBD.adjust(host, "all"); // it can't be here
             }
         }
 
         /**
          * Returns lsit of all parameters as an array.
          */
         public final String[] getParametersFromXML() {
             return drbdXML.getGlobalParams();
         }
 
         /**
          * Checks parameter's new value if it is correct.
          */
         protected final boolean checkParam(final String param,
                                            final String newValue) {
             return drbdXML.checkParam(param, newValue);
         }
 
         /**
          * Returns default value of the parameter.
          */
         protected final String getParamDefault(final String param) {
             return drbdXML.getParamDefault(param);
         }
 
         /**
          * Returns the preferred value of the parameter.
          */
         protected final String getParamPreferred(final String param) {
             return drbdXML.getParamPreferred(param);
         }
 
         /**
          * Possible choices for pulldown menus, or null if it is not a pull
          * down menu.
          */
         protected final Object[] getParamPossibleChoices(final String param) {
             return drbdXML.getPossibleChoices(param);
         }
 
         /**
          * Returns paramter short description, for user visible text.
          */
         protected final String getParamShortDesc(final String param) {
             return drbdXML.getParamShortDesc(param);
         }
 
         /**
          * Returns parameter long description, for tool tips.
          */
         protected final String getParamLongDesc(final String param) {
             return drbdXML.getParamLongDesc(param);
         }
 
         /**
          * Returns section to which this parameter belongs.
          * This is used for grouping in the info panel.
          */
         protected final String getSection(final String param) {
             return drbdXML.getSection(param);
         }
 
         /**
          * Returns whether the parameter is required.
          */
         protected final boolean isRequired(final String param) {
             return drbdXML.isRequired(param);
         }
 
         /**
          * Returns whether the parameter is of the integer type.
          */
         protected final boolean isInteger(final String param) {
             return drbdXML.isInteger(param);
         }
 
         /**
          * Returns whether the parameter is of the time type.
          */
         protected final boolean isTimeType(final String param) {
             /* not required */
             return false;
         }
 
         /**
          * Returns true if unit has prefix.
          */
         protected final boolean hasUnitPrefix(final String param) {
             return drbdXML.hasUnitPrefix(param);
         }
 
         /**
          * Returns long name of the unit, for user visible uses.
          */
         protected final String getUnitLong(final String param) {
             return drbdXML.getUnitLong(param);
         }
 
         /**
          * Returns the default unit of the parameter.
          */
         protected final String getDefaultUnit(final String param) {
             return drbdXML.getDefaultUnit(param);
         }
 
         /**
          * Returns whether the parameter is check box.
          */
         protected final boolean isCheckBox(final String param) {
             final String type = drbdXML.getParamType(param);
             if (type == null) {
                 return false;
             }
             if (DRBD_RES_BOOL_TYPE_NAME.equals(type)) {
                 return true;
             }
             return false;
         }
 
         /**
          * Returns parameter type, boolean etc.
          */
         protected final String getParamType(final String param) {
             return drbdXML.getParamType(param);
         }
 
         /**
          * Applies changes made in the info panel by user.
          */
         public final void apply() {
             final String[] params = getParametersFromXML();
             SwingUtilities.invokeLater(new Runnable() {
                 public void run() {
                     applyButton.setEnabled(false);
                 }
             });
             storeComboBoxValues(params);
         }
 
         /**
          * Returns info panel for drbd. If a block device was selected, its
          * info panel is shown.
          */
         public final JComponent getInfoPanel() {
             if (selectedBD != null) { /* block device is not in drbd */
                 return selectedBD.getInfoPanel();
             }
             if (infoPanel != null) {
                 return infoPanel;
             }
             final JPanel mainPanel = new JPanel();
             if (drbdXML == null) {
                 mainPanel.add(new JLabel("drbd info not available"));
                 return mainPanel;
             }
             mainPanel.setBackground(PANEL_BACKGROUND);
             mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
 
             final JPanel buttonPanel = new JPanel(new BorderLayout());
             buttonPanel.setBackground(STATUS_BACKGROUND);
             buttonPanel.setMinimumSize(new Dimension(0, 50));
             buttonPanel.setPreferredSize(new Dimension(0, 50));
             buttonPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 50));
 
             final JPanel optionsPanel = new JPanel();
             optionsPanel.setBackground(PANEL_BACKGROUND);
             optionsPanel.setLayout(new BoxLayout(optionsPanel,
                                                  BoxLayout.Y_AXIS));
             optionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             final JPanel extraOptionsPanel = new JPanel();
             extraOptionsPanel.setBackground(EXTRA_PANEL_BACKGROUND);
             extraOptionsPanel.setLayout(new BoxLayout(extraOptionsPanel,
                                                       BoxLayout.Y_AXIS));
             extraOptionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
 
             mainPanel.add(buttonPanel);
 
             /* Actions */
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             final JMenu serviceCombo = getActionsMenu();
             updateMenus(null);
             mb.add(serviceCombo);
             buttonPanel.add(mb, BorderLayout.EAST);
 
             final String[] params = getParametersFromXML();
             addParams(optionsPanel,
                       extraOptionsPanel,
                       params,
                       Tools.getDefaultInt("ClusterBrowser.DrbdResLabelWidth"),
                       Tools.getDefaultInt("ClusterBrowser.DrbdResFieldWidth")
                       );
 
             applyButton.addActionListener(
                 new ActionListener() {
                     public void actionPerformed(final ActionEvent e) {
                         Thread thread = new Thread(new Runnable() {
                             public void run() {
                                 clStatusLock();
                                 apply();
                                 clStatusUnlock();
                             }
                         });
                         thread.start();
                     }
                 }
             );
 
             /* apply button */
             addApplyButton(buttonPanel);
             applyButton.setEnabled(checkResourceFields(null, params));
             /* expert mode */
             buttonPanel.add(Tools.expertModeButton(extraOptionsPanel));
 
             mainPanel.add(optionsPanel);
             mainPanel.add(extraOptionsPanel);
 
             infoPanel = new JPanel();
             infoPanel.setBackground(PANEL_BACKGROUND);
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             infoPanel.add(buttonPanel);
             infoPanel.add(new JScrollPane(mainPanel));
             infoPanel.add(Box.createVerticalGlue());
             return infoPanel;
         }
 
         /**
          * Clears info panel cache.
          * TODO: should select something.
          */
         public final boolean selectAutomaticallyInTreeMenu() {
             return infoPanel == null;
         }
 
         /**
          * Returns drbd graph in a panel.
          */
         public final JPanel getGraphicalView() {
             if (selectedBD != null) {
                 drbdGraph.pickBlockDevice(selectedBD);
             }
             return drbdGraph.getGraphPanel();
         }
 
         /**
          * Selects and highlights this node. This function is overwritten
          * because block devices don't have here their own node, but
          * views change depending on selectedNode variable.
          */
         public final void selectMyself() {
             if (selectedBD == null || !selectedBD.getBlockDevice().isDrbd()) {
                 reload(getNode());
                 nodeChanged(getNode());
             } else {
                 reload(selectedBD.getNode());
                 nodeChanged(selectedBD.getNode());
             }
         }
 
         /**
          * Returns new drbd resource index, the one that is not used .
          */
         private int getNewDrbdResourceIndex() {
             final Iterator it = drbdResHash.keySet().iterator();
             int index = -1;
 
             while (it.hasNext()) {
                 final String name = (String) it.next();
                 // TODO: should not assume r0
                 final Pattern p = Pattern.compile("^" + "r" + "(\\d+)$");
                 final Matcher m = p.matcher(name);
 
                 if (m.matches()) {
                     final int i = Integer.parseInt(m.group(1));
                     if (i > index) {
                         index = i;
                     }
                 }
             }
             return index + 1;
         }
 
         /**
          * Adds drbd resource. If resource name and drbd device are null.
          * They will be created with first unused index. E.g. r0, r1 ...
          * /dev/drbd0, /dev/drbd1 ... etc.
          *
          * @param name
          *              resource name.
          * @param drbdDevStr
          *              drbd device like /dev/drbd0
          * @param bd1
          *              block device
          * @param bd2
          *              block device
          * @param interactive
          *              whether dialog box will be displayed
          */
         public final void addDrbdResource(String name,
                              String drbdDevStr,
                              final BlockDevInfo bd1,
                              final BlockDevInfo bd2,
                              final boolean interactive) {
             DrbdResourceInfo dri;
             if (bd1 == null || bd2 == null) {
                 return;
             }
             drbdGraph.startAnimation(bd1);
             drbdGraph.startAnimation(bd2);
 
             if (name == null && drbdDevStr == null) {
                 int index = getNewDrbdResourceIndex();
                 name = "r" + Integer.toString(index);
                 drbdDevStr = "/dev/drbd" + Integer.toString(index);
 
                 /* search for next available drbd device */
                 while (drbdDevHash.containsKey(drbdDevStr)) {
                     index++;
                     drbdDevStr = "/dev/drbd" + Integer.toString(index);
                 }
                 dri = new DrbdResourceInfo(name, drbdDevStr, bd1, bd2);
             } else {
                 dri = new DrbdResourceInfo(name, drbdDevStr, bd1, bd2);
 
                 final String[] sections = drbdXML.getSections();
                 for (String section : sections) {
                     final String[] params = drbdXML.getSectionParams(section);
                     for (String param : params) {
                         String value =
                             drbdXML.getConfigValue(name, section, param);
                         if ("".equals(value)) {
                             value = drbdXML.getParamDefault(param);
                         }
                         dri.getDrbdResource().setValue(param, value);
                     }
                 }
                 dri.getDrbdResource().setCommited(true);
             }
             /* We want next port number on both devices to be the same,
              * although other port numbers may not be the same on both. */
             final int viPort1 = bd1.getNextVIPort();
             final int viPort2 = bd2.getNextVIPort();
             final int viPort;
             if (viPort1 > viPort2) {
                 viPort = viPort1;
             } else {
                 viPort = viPort2;
             }
             bd1.setDefaultVIPort(viPort + 1);
             bd2.setDefaultVIPort(viPort + 1);
 
             dri.getDrbdResource().setDefaultValue(DRBD_RES_PARAM_NAME,
                                                   name);
             dri.getDrbdResource().setDefaultValue(DRBD_RES_PARAM_DEV,
                                                   drbdDevStr);
             drbdResHash.put(name, dri);
             drbdDevHash.put(drbdDevStr, dri);
 
             if (bd1 != null) {
                 bd1.setDrbd(true);
                 bd1.setDrbdResourceInfo(dri);
                 bd1.setInfoPanel(null); /* reload panel */
                 bd1.getInfoPanel();
                 bd1.selectMyself();
             }
             if (bd2 != null) {
                 bd2.setDrbd(true);
                 bd2.setDrbdResourceInfo(dri);
                 bd2.setInfoPanel(null); /* reload panel */
                 bd2.getInfoPanel();
                 bd2.selectMyself();
             }
 
             final DefaultMutableTreeNode drbdResourceNode =
                 new DefaultMutableTreeNode(dri);
             dri.setNode(drbdResourceNode);
 
             drbdNode.add(drbdResourceNode);
 
             final DefaultMutableTreeNode drbdBDNode1 =
                 new DefaultMutableTreeNode(bd1);
             bd1.setNode(drbdBDNode1);
             final DefaultMutableTreeNode drbdBDNode2 =
                 new DefaultMutableTreeNode(bd2);
             bd2.setNode(drbdBDNode2);
             drbdResourceNode.add(drbdBDNode1);
             drbdResourceNode.add(drbdBDNode2);
 
             drbdGraph.addDrbdResource(dri, bd1, bd2);
             final DrbdResourceInfo driF = dri;
             if (interactive) {
                 final Thread thread = new Thread(
                     new Runnable() {
                         public void run() {
                             //reload(getNode());
                             reload(drbdResourceNode);
                             AddDrbdConfigDialog adrd
                                 = new AddDrbdConfigDialog(driF);
                             adrd.showDialogs();
                             if (adrd.isCanceled()) {
                                 driF.removeMyselfNoConfirm();
                                 drbdGraph.stopAnimation(bd1);
                                 drbdGraph.stopAnimation(bd2);
                                 return;
                             }
 
                             updateCommonBlockDevices();
                             drbdXML.update(bd1.getHost());
                             drbdXML.update(bd2.getHost());
                             resetFilesystems();
                         } });
                 thread.start();
             } else {
                 resetFilesystems();
             }
             drbdGraph.stopAnimation(bd1);
             drbdGraph.stopAnimation(bd2);
         }
     }
 
     /**
      * This class holds all hosts that are added to the GUI as opposite to all
      * hosts in a cluster.
      */
     private class AllHostsInfo extends Info {
         /** infoPanel cache. */
         private JPanel infoPanel = null;
 
         /**
          * Creates a new AllHostsInfo instance.
          */
         public AllHostsInfo() {
             super(Tools.getString("ClusterBrowser.AllHosts"));
         }
 
         /**
          * Returns info panel of all hosts menu item. If a host is selected,
          * its tab is selected.
          */
         public final JComponent getInfoPanel() {
             if (infoPanel != null) {
                 return infoPanel;
             }
             infoPanel = new JPanel();
 
             infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
             infoPanel.setBackground(PANEL_BACKGROUND);
             final JPanel bPanel =
                            new JPanel(new BorderLayout());
             bPanel.setMaximumSize(new Dimension(10000, 60));
             bPanel.setBackground(STATUS_BACKGROUND);
             final JMenuBar mb = new JMenuBar();
             mb.setBackground(PANEL_BACKGROUND);
             final JMenu actionsMenu = getActionsMenu();
             updateMenus(null);
             mb.add(actionsMenu);
             bPanel.add(mb, BorderLayout.EAST);
             infoPanel.add(bPanel);
             return infoPanel;
         }
 
         /**
          * Creates the popup for all hosts.
          */
         public final List<UpdatableItem> createPopup() {
             final List<UpdatableItem>items = new ArrayList<UpdatableItem>();
 
             /* host wizard */
             final MyMenuItem newHostWizardItem =
                 new MyMenuItem(Tools.getString("EmptyBrowser.NewHostWizard"),
                                HOST_ICON,
                                null) {
                     private static final long serialVersionUID = 1L;
 
                     public boolean enablePredicate() {
                         return true;
                     }
 
                     public void action() {
                         final AddHostDialog dialog = new AddHostDialog();
                         dialog.showDialogs();
                     }
                 };
             items.add(newHostWizardItem);
             registerMenuItem(newHostWizardItem);
             Tools.getGUIData().registerAddHostButton(newHostWizardItem);
             return items;
         }
     }
 }
