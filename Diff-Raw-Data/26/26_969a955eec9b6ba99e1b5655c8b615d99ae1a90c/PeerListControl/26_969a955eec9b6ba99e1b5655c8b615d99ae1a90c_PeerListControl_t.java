 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.ui.launch;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.TreeEvent;
 import org.eclipse.swt.events.TreeListener;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.TreeColumn;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.tcf.internal.debug.launch.TCFUserDefPeer;
 import org.eclipse.tcf.internal.debug.ui.Activator;
 import org.eclipse.tcf.internal.debug.ui.ImageCache;
 import org.eclipse.tcf.internal.debug.ui.model.TCFModel;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IPeer;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.ILocator;
 
 // Cloned from TCFTargetTab
 public class PeerListControl implements ISelectionProvider {
 
     private Tree peer_tree;
     private final PeerInfo peer_info = new PeerInfo();
     private Display display;
     private final ListenerList selection_listeners = new ListenerList(ListenerList.IDENTITY);
     private String initial_peer_id;
 
     public static class PeerInfo {
         public String id;
         public IPeer peer;
 
         PeerInfo parent;
         Map<String,String> attrs;
         PeerInfo[] children;
         boolean children_pending;
         Throwable children_error;
         IChannel channel;
         ILocator locator;
         LocatorListener listener;
         Runnable item_update;
     }
 
     private class LocatorListener implements ILocator.LocatorListener {
 
         private final PeerInfo parent;
 
         LocatorListener(PeerInfo parent) {
             this.parent = parent;
         }
 
         public void peerAdded(final IPeer peer) {
             if (display == null) return;
             final String id = peer.getID();
             final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (parent.children_error != null) return;
                     PeerInfo[] arr = parent.children;
                     for (PeerInfo p : arr) assert !p.id.equals(id);
                     PeerInfo[] buf = new PeerInfo[arr.length + 1];
                     System.arraycopy(arr, 0, buf, 0, arr.length);
                     PeerInfo info = new PeerInfo();
                     info.parent = parent;
                     info.id = id;
                     info.attrs = attrs;
                     info.peer = peer;
                     buf[arr.length] = info;
                     parent.children = buf;
                     updateItems(parent);
                 }
             });
         }
 
         public void peerChanged(final IPeer peer) {
             if (display == null) return;
             final String id = peer.getID();
             final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (parent.children_error != null) return;
                     PeerInfo[] arr = parent.children;
                     for (int i = 0; i < arr.length; i++) {
                         if (arr[i].id.equals(id)) {
                             arr[i].attrs = attrs;
                             arr[i].peer = peer;
                             updateItems(parent);
                         }
                     }
                 }
             });
         }
 
         public void peerRemoved(final String id) {
             if (display == null) return;
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (parent.children_error != null) return;
                     PeerInfo[] arr = parent.children;
                     PeerInfo[] buf = new PeerInfo[arr.length - 1];
                     int j = 0;
                     for (int i = 0; i < arr.length; i++) {
                         if (arr[i].id.equals(id)) {
                             final PeerInfo info = arr[i];
                             Protocol.invokeLater(new Runnable() {
                                 public void run() {
                                     disconnectPeer(info);
                                 }
                             });
                         }
                         else {
                             buf[j++] = arr[i];
                         }
                     }
                     parent.children = buf;
                     updateItems(parent);
                 }
             });
         }
 
         public void peerHeartBeat(final String id) {
             if (display == null) return;
             display.asyncExec(new Runnable() {
                 public void run() {
                     if (parent.children_error != null) return;
                     PeerInfo[] arr = parent.children;
                     for (int i = 0; i < arr.length; i++) {
                         if (arr[i].id.equals(id)) {
                             if (arr[i].children_error != null) {
                                 TreeItem item = findItem(arr[i]);
                                 boolean visible = item != null;
                                 while (visible && item != null) {
                                     if (!item.getExpanded()) visible = false;
                                     item = item.getParentItem();
                                 }
                                 if (visible) loadChildren(arr[i]);
                             }
                             break;
                         }
                     }
                 }
             });
         }
     }
 
     public PeerListControl(Composite parent) {
         display = parent.getDisplay();
         parent.addDisposeListener(new DisposeListener() {
             public void widgetDisposed(DisposeEvent e) {
                 handleDispose();
             }
         });
         loadChildren(peer_info);
         createPeerListArea(parent);
     }
 
     public void setInitialSelection(String id) {
         if (id == null) return;
         if (id.length() == 0) return;
         PeerInfo info = findPeerInfo(id);
         if (info != null) {
             setSelection(new StructuredSelection(info));
             fireSelectionChangedEvent();
             onPeerSelected(info);
         }
         else {
             String p = id;
             for (;;) {
                 int i = p.lastIndexOf('/');
                 if (i < 0) break;
                 p = p.substring(0, i);
                 TreeItem item = findItem(p);
                 if (item != null) item.setExpanded(true);
             }
             initial_peer_id = id;
         }
     }
 
     public Tree getTree() {
         return peer_tree;
     }
 
     private void createPeerListArea(Composite parent) {
         Font font = parent.getFont();
         Composite composite = new Composite(parent, SWT.NONE);
         GridLayout layout = new GridLayout(2, false);
         composite.setFont(font);
         composite.setLayout(layout);
         composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));
 
         peer_tree = new Tree(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
         GridData gd = new GridData(GridData.FILL_BOTH);
         gd.minimumHeight = 150;
         gd.minimumWidth = 470;
         peer_tree.setLayoutData(gd);
 
         for (int i = 0; i < 6; i++) {
             TreeColumn column = new TreeColumn(peer_tree, SWT.LEAD, i);
             column.setMoveable(true);
             switch (i) {
             case 0:
                 column.setText("Name");
                 column.setWidth(160);
                 break;
             case 1:
                 column.setText("OS");
                 column.setWidth(100);
                 break;
             case 2:
                 column.setText("User");
                 column.setWidth(100);
                 break;
             case 3:
                 column.setText("Transport");
                 column.setWidth(60);
                 break;
             case 4:
                 column.setText("Host");
                 column.setWidth(100);
                 break;
             case 5:
                 column.setText("Port");
                 column.setWidth(40);
                 break;
             }
         }
 
         peer_tree.setHeaderVisible(true);
         peer_tree.setFont(font);
         peer_tree.addListener(SWT.SetData, new Listener() {
             @Override
             public void handleEvent(Event event) {
                 TreeItem item = (TreeItem)event.item;
                 PeerInfo info = findPeerInfo(item);
                 if (info == null) {
                     updateItems(item.getParentItem(), false);
                 }
                 else {
                     fillItem(item, info);
                     onPeerListChanged();
                 }
             }
         });
         peer_tree.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetDefaultSelected(SelectionEvent e) {
                 TreeItem[] selections = peer_tree.getSelection();
                 if (selections.length == 0) return;
                 final PeerInfo info = findPeerInfo(selections[0]);
                 if (info == null) return;
                 new PeerPropsDialog(peer_tree.getShell(), getImage(info), info.attrs,
                         info.peer instanceof TCFUserDefPeer).open();
                 if (!(info.peer instanceof TCFUserDefPeer)) return;
                 Protocol.invokeLater(new Runnable() {
                     public void run() {
                         ((TCFUserDefPeer)info.peer).updateAttributes(info.attrs);
                         TCFUserDefPeer.savePeers();
                     }
                 });
             }
             @Override
             public void widgetSelected(SelectionEvent e) {
                 fireSelectionChangedEvent();
                 TreeItem[] selections = peer_tree.getSelection();
                 if (selections.length > 0) {
                     assert selections.length == 1;
                     PeerInfo info = findPeerInfo(selections[0]);
                     if (info != null) {
                         initial_peer_id = null;
                         onPeerSelected(info);
                     }
                 }
                 onPeerListChanged();
             }
         });
         peer_tree.addTreeListener(new TreeListener() {
             @Override
             public void treeCollapsed(TreeEvent e) {
                 updateItems((TreeItem)e.item, false);
             }
             @Override
             public void treeExpanded(TreeEvent e) {
                 updateItems((TreeItem)e.item, true);
             }
         });
     }
 
     private void handleDispose() {
         Protocol.invokeAndWait(new Runnable() {
             public void run() {
                 disconnectPeer(peer_info);
                 display = null;
             }
         });
     }
 
     private void disconnectPeer(final PeerInfo info) {
         assert Protocol.isDispatchThread();
         if (info.children != null) {
             for (PeerInfo p : info.children) disconnectPeer(p);
         }
         if (info.listener != null) {
             info.locator.removeListener(info.listener);
             info.listener = null;
             info.locator = null;
         }
         if (info.channel != null) {
             info.channel.close();
         }
     }
 
     private boolean canHaveChildren(PeerInfo parent) {
         return parent == peer_info || parent.attrs.get(IPeer.ATTR_PROXY) != null;
     }
 
     private void loadChildren(final PeerInfo parent) {
         assert Thread.currentThread() == display.getThread();
         if (parent.children_pending) return;
         assert parent.children == null;
         parent.children_pending = true;
         parent.children_error = null;
         Protocol.invokeAndWait(new Runnable() {
             public void run() {
                 assert parent.listener == null;
                 assert parent.channel == null;
                 if (!canHaveChildren(parent)) {
                     doneLoadChildren(parent, null, new PeerInfo[0]);
                 }
                 else if (parent == peer_info) {
                     peer_info.locator = Protocol.getLocator();
                     doneLoadChildren(parent, null, createLocatorListener(peer_info));
                 }
                 else {
                     final IChannel channel = parent.peer.openChannel();
                     parent.channel = channel;
                     parent.channel.addChannelListener(new IChannel.IChannelListener() {
                         boolean opened = false;
                         boolean closed = false;
                         public void congestionLevel(int level) {
                         }
                         public void onChannelClosed(final Throwable error) {
                             assert !closed;
                             if (parent.channel != channel) return;
                             if (!opened) {
                                 doneLoadChildren(parent, error, null);
                             }
                             else {
                                 if (display != null) {
                                     display.asyncExec(new Runnable() {
                                         public void run() {
                                             if (parent.children_pending) return;
                                             parent.children = null;
                                             parent.children_error = error;
                                             updateItems(parent);
                                         }
                                     });
                                 }
                             }
                             closed = true;
                             parent.channel = null;
                             parent.locator = null;
                             parent.listener = null;
                         }
                         public void onChannelOpened() {
                             assert !opened;
                             assert !closed;
                             if (parent.channel != channel) return;
                             opened = true;
                             parent.locator = parent.channel.getRemoteService(ILocator.class);
                             if (parent.locator == null) {
                                 parent.channel.terminate(new Exception("Service not supported: " + ILocator.NAME));
                             }
                             else {
                                 doneLoadChildren(parent, null, createLocatorListener(parent));
                             }
                         }
                     });
                 }
             }
         });
     }
 
     private PeerInfo[] createLocatorListener(PeerInfo peer) {
         assert Protocol.isDispatchThread();
         Map<String,IPeer> map = peer.locator.getPeers();
         PeerInfo[] buf = new PeerInfo[map.size()];
         int n = 0;
         for (IPeer p : map.values()) {
             PeerInfo info = new PeerInfo();
             info.parent = peer;
             info.id = p.getID();
             info.attrs = new HashMap<String,String>(p.getAttributes());
             info.peer = p;
             buf[n++] = info;
         }
         peer.listener = new LocatorListener(peer);
         peer.locator.addListener(peer.listener);
         return buf;
     }
 
     private void doneLoadChildren(final PeerInfo parent, final Throwable error, final PeerInfo[] children) {
         assert Protocol.isDispatchThread();
         assert error == null || children == null;
         if (display == null) return;
         display.asyncExec(new Runnable() {
             public void run() {
                 assert parent.children_pending;
                 assert parent.children == null;
                 parent.children_pending = false;
                 parent.children = children;
                 parent.children_error = error;
                 updateItems(parent);
             }
         });
     }
 
     private ArrayList<PeerInfo> filterPeerList(PeerInfo parent, boolean expanded) {
         ArrayList<PeerInfo> lst = new ArrayList<PeerInfo>();
         HashMap<String,PeerInfo> local_agents = new HashMap<String,PeerInfo>();
         HashSet<String> ids = new HashSet<String>();
         for (PeerInfo p : parent.children) {
             String id = p.attrs.get(IPeer.ATTR_AGENT_ID);
             if (id == null) continue;
             if (!"TCP".equals(p.attrs.get(IPeer.ATTR_TRANSPORT_NAME))) continue;
             if (!"127.0.0.1".equals(p.attrs.get(IPeer.ATTR_IP_HOST))) continue;
             if (isFiltered(p)) continue;
             local_agents.put(id, p);
             ids.add(p.id);
         }
         for (PeerInfo p : parent.children) {
             PeerInfo i = local_agents.get(p.attrs.get(IPeer.ATTR_AGENT_ID));
             if (i != null && i != p) continue;
             if (isFiltered(p)) continue;
             lst.add(p);
         }
         if (parent != peer_info && expanded) {
             for (PeerInfo p : peer_info.children) {
                 if (p.peer instanceof TCFUserDefPeer && !ids.contains(p.id)) {
                     PeerInfo x = new PeerInfo();
                     x.parent = parent;
                     x.id = p.id;
                     x.attrs = p.attrs;
                     x.peer = p.peer;
                     ids.add(x.id);
                     lst.add(x);
                 }
             }
         }
         return lst;
     }

     private boolean isFiltered(PeerInfo p) {
        boolean filtered = false;
        if (p != null && p.attrs != null) {
             filtered |= p.attrs.get("ValueAdd") != null && ("1".equals(p.attrs.get("ValueAdd").trim()) || Boolean.parseBoolean(p.attrs.get("ValueAdd").trim())); //$NON-NLS-1$
            filtered |= p.attrs.get(IPeer.ATTR_NAME) != null
                            && (p.attrs.get(IPeer.ATTR_NAME).startsWith("Eclipse CLI") //$NON-NLS-1$
                                    || p.attrs.get(IPeer.ATTR_NAME).endsWith("CLI Server") //$NON-NLS-1$
                                     || p.attrs.get(IPeer.ATTR_NAME).endsWith("CLI Client")); //$NON-NLS-1$
        }
        return filtered;
     }
 
     private void updateItems(TreeItem parent_item, boolean reload) {
         final PeerInfo parent_info = findPeerInfo(parent_item);
         if (parent_info == null) {
             parent_item.setText("Invalid");
         }
         else {
             if (reload && parent_info.children_error != null) {
                 loadChildren(parent_info);
             }
             display.asyncExec(new Runnable() {
                 public void run() {
                     updateItems(parent_info);
                 }
             });
         }
     }
 
     private void updateItems(final PeerInfo parent) {
         if (display == null) return;
         assert Thread.currentThread() == display.getThread();
         parent.item_update = new Runnable() {
             public void run() {
                 if (display == null) return;
                 if (parent.item_update != this) return;
                 if (Thread.currentThread() != display.getThread()) {
                     display.asyncExec(this);
                     return;
                 }
                 parent.item_update = null;
                 TreeItem[] items = null;
                 boolean expanded = true;
                 if (parent.children == null || parent.children_error != null) {
                     if (parent == peer_info) {
                         peer_tree.setItemCount(1);
                         items = peer_tree.getItems();
                     }
                     else {
                         TreeItem item = findItem(parent);
                         if (item == null) return;
                         expanded = item.getExpanded();
                         item.setItemCount(1);
                         items = item.getItems();
                     }
                     assert items.length == 1;
                     if (parent.children_pending) {
                         items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                         fillItem(items[0], "Connecting...");
                     }
                     else if (parent.children_error != null) {
                         String msg = TCFModel.getErrorMessage(parent.children_error, false);
                         items[0].setForeground(display.getSystemColor(SWT.COLOR_RED));
                         fillItem(items[0], msg);
                     }
                     else if (expanded) {
                         loadChildren(parent);
                         items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                         fillItem(items[0], "Connecting...");
                     }
                     else {
                         Protocol.invokeAndWait(new Runnable() {
                             public void run() {
                                 disconnectPeer(parent);
                             }
                         });
                         fillItem(items[0], "");
                     }
                 }
                 else {
                     ArrayList<PeerInfo> lst = null;
                     if (parent == peer_info) {
                         lst = filterPeerList(parent, expanded);
                         peer_tree.setItemCount(lst.size() > 0 ? lst.size() : 1);
                         items = peer_tree.getItems();
                     }
                     else {
                         TreeItem item = findItem(parent);
                         if (item == null) return;
                         expanded = item.getExpanded();
                         lst = filterPeerList(parent, expanded);
                         item.setItemCount(expanded && lst.size() > 0 ? lst.size() : 1);
                         items = item.getItems();
                     }
                     if (expanded && lst.size() > 0) {
                         assert items.length == lst.size();
                         for (int i = 0; i < items.length; i++) fillItem(items[i], lst.get(i));
                     }
                     else if (expanded) {
                         fillItem(items[0], "No peers");
                     }
                     else {
                         Protocol.invokeAndWait(new Runnable() {
                             public void run() {
                                 disconnectPeer(parent);
                             }
                         });
                         fillItem(items[0], "");
                     }
                 }
                 onPeerListChanged();
                 if (initial_peer_id != null) {
                     setInitialSelection(initial_peer_id);
                 }
             }
         };
         if (parent.children_pending) parent.item_update.run();
         else Protocol.invokeLater(200, parent.item_update);
     }
 
     public TreeItem findItem(String path) {
         assert Thread.currentThread() == display.getThread();
         if (path == null) return null;
         int z = path.lastIndexOf('/');
         if (z < 0) {
             int n = peer_tree.getItemCount();
             for (int i = 0; i < n; i++) {
                 TreeItem x = peer_tree.getItem(i);
                 PeerInfo p = (PeerInfo)x.getData("TCFPeerInfo");
                 if (p != null && p.id.equals(path)) return x;
             }
         }
         else {
             TreeItem y = findItem(path.substring(0, z));
             if (y == null) return null;
             String id = path.substring(z + 1);
             int n = y.getItemCount();
             for (int i = 0; i < n; i++) {
                 TreeItem x = y.getItem(i);
                 PeerInfo p = (PeerInfo)x.getData("TCFPeerInfo");
                 if (p != null && p.id.equals(id)) return x;
             }
         }
         return null;
     }
 
     public TreeItem findItem(PeerInfo info) {
         if (info == null) return null;
         assert info.parent != null;
         if (info.parent == peer_info) {
             int n = peer_tree.getItemCount();
             for (int i = 0; i < n; i++) {
                 TreeItem x = peer_tree.getItem(i);
                 if (x.getData("TCFPeerInfo") == info) return x;
             }
         }
         else {
             TreeItem y = findItem(info.parent);
             if (y == null) return null;
             int n = y.getItemCount();
             for (int i = 0; i < n; i++) {
                 TreeItem x = y.getItem(i);
                 if (x.getData("TCFPeerInfo") == info) return x;
             }
         }
         return null;
     }
 
     public String getPath(PeerInfo info) {
         if (info == peer_info) return "";
         if (info.parent == peer_info) return info.id;
         return getPath(info.parent) + "/" + info.id;
     }
 
     public PeerInfo findPeerInfo(String path) {
         TreeItem i = findItem(path);
         if (i == null) return null;
         return (PeerInfo)i.getData("TCFPeerInfo");
     }
 
     private PeerInfo findPeerInfo(TreeItem item) {
         assert Thread.currentThread() == display.getThread();
         if (item == null) return peer_info;
         return (PeerInfo)item.getData("TCFPeerInfo");
     }
 
     private void fillItem(TreeItem item, PeerInfo info) {
         assert Thread.currentThread() == display.getThread();
         Object data = item.getData("TCFPeerInfo");
         if (data != null && data != info) item.removeAll();
         item.setData("TCFPeerInfo", info);
         String text[] = new String[6];
         text[0] = info.attrs.get(IPeer.ATTR_NAME);
         text[1] = info.attrs.get(IPeer.ATTR_OS_NAME);
         text[2] = info.attrs.get(IPeer.ATTR_USER_NAME);
         text[3] = info.attrs.get(IPeer.ATTR_TRANSPORT_NAME);
         text[4] = info.attrs.get(IPeer.ATTR_IP_HOST);
         text[5] = info.attrs.get(IPeer.ATTR_IP_PORT);
         for (int i = 0; i < text.length; i++) {
             if (text[i] == null) text[i] = "";
         }
         item.setText(text);
         item.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
         item.setImage(getImage(info));
         if (!canHaveChildren(info)) item.setItemCount(0);
         else if (info.children == null || info.children_error != null) item.setItemCount(1);
         else item.setItemCount(info.children.length);
     }
 
     private void fillItem(TreeItem item, String text) {
         item.setText(text);
         item.setData("TCFPeerInfo", null);
         int n = peer_tree.getColumnCount();
         for (int i = 1; i < n; i++) item.setText(i, "");
         item.setImage((Image)null);
         item.removeAll();
     }
 
     private Image getImage(PeerInfo info) {
         return ImageCache.getImage(ImageCache.IMG_TARGET_TAB);
     }
 
     public void addSelectionChangedListener(ISelectionChangedListener listener) {
         selection_listeners.add(listener);
     }
 
     public ISelection getSelection() {
         TreeItem[] items = peer_tree.getSelection();
         PeerInfo[] peers = new PeerInfo[items.length];
         int i = 0;
         for (TreeItem item : items) {
             peers[i++] = findPeerInfo(item);
         }
         return new StructuredSelection(peers);
     }
 
     public void removeSelectionChangedListener(ISelectionChangedListener listener) {
         selection_listeners.remove(listener);
     }
 
     public void setSelection(ISelection selection) {
         peer_tree.deselectAll();
         if (selection instanceof IStructuredSelection) {
             Object[] elements = ((IStructuredSelection) selection).toArray();
             for (Object object : elements) {
                 if (object instanceof PeerInfo) {
                     TreeItem item = findItem((PeerInfo) object);
                     if (item != null) {
                         peer_tree.select(item);
                     }
                 }
             }
         }
     }
 
     private void fireSelectionChangedEvent() {
         SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
         Object[] listeners = selection_listeners.getListeners();
         for (Object listener : listeners) {
             try {
                 ((ISelectionChangedListener) listener).selectionChanged(event);
             }
             catch (Exception e) {
                 Activator.log(e);
             }
         }
     }
 
     protected void onPeerListChanged() {
     }
 
     protected void onPeerSelected(PeerInfo info) {
     }
 }
