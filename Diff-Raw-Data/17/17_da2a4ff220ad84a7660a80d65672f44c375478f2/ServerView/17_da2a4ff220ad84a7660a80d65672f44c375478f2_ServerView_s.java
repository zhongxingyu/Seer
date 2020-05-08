 package org.nuxeo.ide.sdk.server.ui;
 
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.sdk.NuxeoSDK;
 import org.nuxeo.ide.sdk.SDKChangedListener;
 import org.nuxeo.ide.sdk.server.ServerConstants;
 import org.nuxeo.ide.sdk.server.ServerController;
 import org.nuxeo.ide.sdk.server.ServerLifeCycleAdapter;
 import org.nuxeo.ide.sdk.server.ui.widgets.ConsoleText;
 import org.nuxeo.ide.sdk.server.ui.widgets.StyleFactory;
 
 public class ServerView extends ViewPart implements ISelectionProvider,
         SDKChangedListener, IServerView {
 
     /**
      * The ID of the view as specified by the extension.
      */
     public static final String ID = "org.nuxeo.ide.sdk.server.ui.ServerView";
 
     // protected Text console;
 
     protected ConsoleText console;
 
     protected ServerController ctrl;
 
     protected MyServerLifeCycleListener listener;
 
     protected ListenerList selectionListeners;
 
     protected boolean scrollLock = false;
 
     /**
      * The constructor.
      */
     public ServerView() {
         selectionListeners = new ListenerList();
         listener = new MyServerLifeCycleListener();
         initServer();
         NuxeoSDK.addSDKChangedListener(this);
     }
 
     protected void initServer() {
         if (ctrl != null) {
             int state = ctrl.getState();
             if (state == ServerConstants.STARTED
                     || state == ServerConstants.STARTING) {
                 try {
                     ctrl.stopAsJob();
                 } catch (Exception e) {
                     UI.showError("Failed to stop running server", e);
                 }
             }
             ctrl.removeServerLifeCycleListener(listener);
             ctrl = null;
         }
         NuxeoSDK sdk = NuxeoSDK.getDefault();
         if (sdk != null) {
             ctrl = sdk.getController();
             ctrl.addServerLifeCycleListener(listener);
         }
         setSelection(getSelection());
     }
 
     @Override
     public void handleSDKChanged(NuxeoSDK sdk) {
         initServer();
     }
 
     public ConsoleText getConsole() {
         return console;
     }
 
     public void start(boolean isDebug) throws Exception {
         // now start
         clearConsole();
         if (isDebug) {
             console.setText("=== Starting Nuxeo Server (Debug) ===\r\n");
         } else {
             console.setText("=== Starting Nuxeo Server ===\r\n");
         }
         // hot deploy projects
         try {
             Deployment deployment = DeploymentPreferences.load().getDefault();
             NuxeoSDK.getDefault().reloadDeployment(deployment);
         } catch (Exception de) {
             UI.showError("Cannot hot deploy bundles, please restart", de);
         }
 
         ctrl.startAsJob(isDebug);
     }
 
     public void start() throws Exception {
         start(false);
     }
 
     public void debug() throws Exception {
         start(true);
     }
 
     public void stop() throws Exception {
         if (ctrl == null) {
             return;
         }
         ctrl.stopAsJob();
     }
 
     @Override
     public void clearConsole() {
         if (console != null) {
             console.resetStyles();
             console.setText("");
         }
     }
 
     @Override
     public void setScrollLock(boolean lock) {
         scrollLock = lock;
     }
 
     @Override
     public boolean getScrollLock() {
         return scrollLock;
     }
 
     @Override
     public void append(String text) {
         if (scrollLock) {
             console.append(text);
         } else {
             console.appendAndScroll(text);
         }
     }
 
     /**
      * This is a callback that will allow us to create the viewer and initialize
      * it.
      */
     @Override
     public void createPartControl(Composite parent) {
         console = new ConsoleText(parent, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL
                 | SWT.H_SCROLL | SWT.FLAT | SWT.READ_ONLY);
         console.installStackTraceSupport();
         console.getStyleManager().addStyleFactory(new StyleFactory() {
             @Override
             public StyleRange getStyle(String string) {
                 if (string.startsWith("=")) {
                     StyleRange style = new StyleRange(0, string.length(), null,
                             null);
                     style.fontStyle = SWT.BOLD;
                     return style;
                 }
                 return null;
             }
         });
         console.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
     }
 
     @Override
     public void dispose() {
         NuxeoSDK.removeSDKChangedListener(this);
         if (listener != null) {
             ctrl.removeServerLifeCycleListener(listener);
             listener = null;
         }
         if (ctrl != null) {
             try {
                 ctrl.stop(false);
             } catch (Exception e) {
                 UI.showError("Faield to stop the server.", e);
             }
             ctrl = null;
         }
         super.dispose();
         selectionListeners.clear();
         selectionListeners = null;
 
         console.dispose();
         console = null;
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     @Override
     public void setFocus() {
         console.setFocus();
     }
 
     class MyServerLifeCycleListener extends ServerLifeCycleAdapter {
         @Override
         public void handleConsoleError(ServerController ctrl, Throwable t) {
             t.printStackTrace();
         }
 
         @Override
         public void handleConsoleText(ServerController ctrl, final String text) {
             if (console != null) {
                 Display.getDefault().asyncExec(new Runnable() {
                     @Override
                     public void run() {
                         append(text);
                     }
                 });
             }
         }
 
         @Override
         public void serverStateChanged(ServerController ctrl, final int state) {
             if (state == ServerConstants.STARTED) {
                 handleConsoleText(ctrl, "=== Nuxeo Server Started ===\r\n");
             } else if (state == ServerConstants.STOPPED) {
                 handleConsoleText(ctrl, "=== Nuxeo Server Stopped ===\r\n");
             }
             Display.getDefault().asyncExec(new Runnable() {
                 @Override
                 public void run() {
                     setSelection(new ServerState(state));
                 }
             });
         }
     }
 
     @Override
     public void addSelectionChangedListener(ISelectionChangedListener listener) {
         selectionListeners.add(listener);
     }
 
     @Override
     public void removeSelectionChangedListener(
             ISelectionChangedListener listener) {
         if (selectionListeners != null) {
             selectionListeners.remove(listener);
         }
     }
 
     @Override
     public void setSelection(ISelection selection) {
         SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
         for (Object obj : selectionListeners.getListeners()) {
             ((ISelectionChangedListener) obj).selectionChanged(event);
         }
     }
 
     @Override
     public ISelection getSelection() {
         return new ServerState(ctrl == null ? ServerConstants.UNDEFINED
                 : ctrl.getState());
     }
 
     @Override
     public void init(IViewSite site) throws PartInitException {
         super.init(site);
         site.setSelectionProvider(this);
     }
 
     @Override
     public ServerController getController() {
         return ctrl;
     }
 }
