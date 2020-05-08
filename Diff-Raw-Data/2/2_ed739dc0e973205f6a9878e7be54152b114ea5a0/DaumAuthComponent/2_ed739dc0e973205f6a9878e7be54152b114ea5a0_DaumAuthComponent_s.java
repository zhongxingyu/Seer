 package org.daum.library.android.daumauth;
 
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Toast;
 import org.daum.library.android.daumauth.util.ConnectionTask;
 import org.daum.library.android.daumauth.view.DaumAuthView;
 import org.daum.library.ormH.store.ReplicaStore;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.cache.ReplicaService;
 import org.daum.library.replica.listener.ChangeListener;
 import org.kevoree.ContainerNode;
 import org.kevoree.ContainerRoot;
 import org.kevoree.android.framework.helper.UIServiceHandler;
 import org.kevoree.android.framework.service.KevoreeAndroidService;
 import org.kevoree.annotation.*;
 import org.kevoree.api.service.core.handler.ModelListener;
 import org.kevoree.api.service.core.script.KevScriptEngine;
 import org.kevoree.framework.AbstractComponentType;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: max
  * Date: 06/07/12
  * Time: 11:29
  * To change this template use File | Settings | File Templates.
  */
 
 @Library(name = "Android")
 @Requires({
         @RequiredPort(name = "service", type = PortType.SERVICE, className = ReplicaService.class, optional = true)
 })
 @Provides({
         @ProvidedPort(name = "notify", type = PortType.MESSAGE)
 })
 @DictionaryType({
         @DictionaryAttribute(name = "connTimeout", defaultValue = "15000", optional = false)
 })
 @ComponentType
 public class DaumAuthComponent extends AbstractComponentType implements DaumAuthView.OnClickListener, DaumAuthEngine.OnStoreSyncedListener {
 
     private static final String TAG = "DaumAuthComponent";
     private static final Logger logger = LoggerFactory.getLogger(DaumAuthComponent.class);
     private static final String TAB_NAME = "Connexion";
     private static final String TEXT_LOADING = "Tentative de connexion...";
     private static final String TEXT_CONN_FAILED = "Mauvais matricule et/ou mot de passe";
     private static final String TEXT_CONN_TIMEDOUT = "Impossible de se connecter, réessayez plus tard.";
 
     private static final int CONNECTION_TIMEOUT = 1000*15; // 15 seconds
 
     private KevoreeAndroidService uiService;
     private DaumAuthEngine engine;
     private DaumAuthView authView;
     private ConnectionTask connTask;
     private int connTimeout = CONNECTION_TIMEOUT;
     private static ChangeListener listener = new ChangeListener();
     private boolean storeSynced = false;
 
     @Start
     public void start() {
         uiService = UIServiceHandler.getUIService();
         int value = Integer.parseInt(getDictionary().get("connTimeout").toString());
         if (value >= 0) connTimeout = value;
         else logger.warn(TAG, "Dictionary connTimeout value must be >= 0 (set to default: "+CONNECTION_TIMEOUT+")");
 
         initUI();
 
         getModelService().registerModelListener(new ModelListener() {
             @Override
             public boolean preUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                 return true;
             }
 
             @Override
             public boolean initUpdate(ContainerRoot containerRoot, ContainerRoot containerRoot1) {
                 return true;
             }
 
             @Override
             public void modelUpdated() {
                 uiService.getRootActivity().runOnUiThread(new Runnable() {
                     @Override
                     public void run() {
                         try {
                             ReplicaService replicatingService = getPortByName("service", ReplicaService.class);
                             ReplicaStore store = new ReplicaStore(replicatingService);
                             engine = new DaumAuthEngine(getNodeName(), store);
                             engine.setOnStoreSyncedListener(DaumAuthComponent.this);
 
                         } catch (PersistenceException e) {
                             Log.e(TAG, "Error on component startup", e);
                         }
                     }
                 });
             }
         });
 
     }
 
     private void initUI() {
         uiService.getRootActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 Window window = uiService.getRootActivity().getWindow();
                 window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
             }
         });
 
         authView = new DaumAuthView(uiService.getRootActivity());
         authView.setOnClickListener(this);
         uiService.addToGroup(TAB_NAME, authView);
     }
 
     @Stop
     public void stop() {
         uiService.remove(authView);
     }
 
     @Update
     public void update() {
         stop();
         start();
     }
 
     @Port(name = "notify")
     public void notifiedByReplica(final Object m) {
         uiService.getRootActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 DaumAuthComponent.getChangeListener().receive(m);
             }
         });
     }
 
     @Override
     public void onConnectionButtonClicked(String matricule, String password) {
         final ProgressDialog pDialog = new ProgressDialog(uiService.getRootActivity());
         pDialog.setIndeterminate(true);
         pDialog.setCancelable(false);
         pDialog.setMessage(TEXT_LOADING);
         showDialog(pDialog);
 
         this.connTask = new ConnectionTask(engine, matricule, password, connTimeout, storeSynced);
         connTask.setOnEventListener(new ConnectionTask.OnEventListener() {
             @Override
             public void onConnectionTimedOut() {
                 Log.w(TAG, "onConnectionTimedOut");
                 dismissDialog(pDialog, TEXT_CONN_TIMEDOUT);
             }
 
             @Override
             public void onConnectionSucceeded(String matricule) {
                 Log.w(TAG, "onConnectionSucceeded");
                 dismissDialog(pDialog, null);
                 generateModel();
 
             }
 
             @Override
             public void onConnectionFailed(String matricule) {
                 Log.w(TAG, "onConnectionFailed");
                 dismissDialog(pDialog, TEXT_CONN_FAILED);
             }
         });
         connTask.start();
     }
 
     private void generateModel() {
         KevScriptEngine engine = getKevScriptEngineFactory().createKevScriptEngine();
 
         // node name variable
         engine.addVariable("nodeName", getNodeName());
 
         // kevScript model for SITAC, Moyens & Messages
         engine.append("merge 'mvn:org.daum.library.android/org.daum.library.android.sitac/1.8.3-SNAPSHOT'");
         engine.append("merge 'mvn:org.daum.library.android/org.daum.library.android.messages/1.8.2-SNAPSHOT'");
         engine.append("merge 'mvn:org.daum.library.android/org.daum.library.android.moyens/1.8.2-SNAPSHOT'");
 
         engine.append("addComponent sitacComp@{nodeName} : SITACComponent {}");
         engine.append("addComponent replicaComp@{nodeName} : Replica {}");
         engine.append("addComponent moyensComp@{nodeName} : MoyensComponent {}");
         engine.append("addChannel defServ0 : defSERVICE {}");
         engine.append("addChannel socketChan : SocketChannel {port='9001',replay='false',maximum_size_messaging='50',timer='2000'}");
         engine.append("addChannel defMsg0 : defMSG {}");
         engine.append("bind sitacComp.service@{nodeName} => defServ0");
         engine.append("bind replicaComp.service@{nodeName} => defServ0");
         engine.append("bind replicaComp.remote@{nodeName} => socketChan");
         engine.append("bind replicaComp.broadcast@{nodeName} => socketChan");
         engine.append("bind moyensComp.service@{nodeName} => defServ0");
         engine.append("bind replicaComp.notification@{nodeName} => defMsg0");
         engine.append("bind moyensComp.notify@{nodeName} => defMsg0");
         engine.append("bind sitacComp.notify@{nodeName} => defMsg0");
 
         engine.interpretDeploy();
     }
 
     private void showDialog(final ProgressDialog dialog) {
         uiService.getRootActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 dialog.show();
             }
         });
     }
 
     private void dismissDialog(final ProgressDialog dialog, final String msg) {
         uiService.getRootActivity().runOnUiThread(new Runnable() {
             @Override
             public void run() {
                 dialog.dismiss();
                 if (msg != null) {
                    Toast.makeText(uiService.getRootActivity(), TEXT_CONN_FAILED, Toast.LENGTH_SHORT).show();
                 }
             }
         });
     }
 
     public static ChangeListener getChangeListener() {
         return listener;
     }
 
     @Override
     public void onStoreSynced() {
         Log.w(TAG, ">>>>> onStoreSynced called");
         this.storeSynced = true;
         if (connTask != null) connTask.setSynced(storeSynced);
     }
 }
