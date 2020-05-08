 package ca.etsmtl.capra.tools.networkmonitor.client.gui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import ca.etsmtl.capra.tools.networkmonitor.client.ConnectionToServer;
 import ca.etsmtl.capra.tools.networkmonitor.client.ConnectionToServer.IConnectionHandler;
 import ca.etsmtl.capra.tools.networkmonitor.client.ConnectionToServer.IUpdateReceiver;
 import ca.etsmtl.capra.tools.networkmonitor.client.gui.control.SimpleRow;
 import ca.etsmtl.capra.tools.networkmonitor.client.util.IPathMonitor;
 import ca.etsmtl.capra.tools.networkmonitor.client.util.ObservableHashtable;
 import ca.etsmtl.capra.tools.networkmonitor.client.util.ObservableHashtable.ObservableEvent;
 
public class MonitorTab extends JPanel implements IPathMonitor {
    
    private static final long serialVersionUID = 6086765317233242524L;
    
    private MonitorPane mMonitorPane = null;
    
    private ConnectionToServer mServer;
    
    public MonitorTab(ConnectionToServer iServer) {
       mServer = iServer;
       
       mMonitorPane = new MonitorPane();
      this.setLayout(new GridBagLayout());
       this.setName("Monitor");
       
      GridBagConstraints c = new GridBagConstraints();
      c.gridx = 0;
      c.gridy = 0;
      c.weightx = 1.0;
      c.weighty = 0;
      c.fill = GridBagConstraints.BOTH;
      c.anchor = GridBagConstraints.PAGE_START;
      this.add(mMonitorPane,c);
       
       mServer.mConnectorList.add(mMonitorPane);
       mServer.mReceiverList.add(mMonitorPane);
    }
    
   private static class MonitorPane extends JScrollPane implements IConnectionHandler, IUpdateReceiver {
       private static final long serialVersionUID = 8502768242897026501L;
       
       public ObservableHashtable<String, SimpleRow> mElementList = new ObservableHashtable<String, SimpleRow>();
       
      JPanel mMainPanel = new JPanel(new GridBagLayout());
       
       public MonitorPane() {
          mElementList.eventList.add(new ElementEvent());
         this.getViewport().add(mMainPanel);
       }
       
       private class ElementEvent implements ObservableEvent<SimpleRow> {
          GridBagConstraints c = new GridBagConstraints();
          
          public ElementEvent() {
             c.gridx = 0;
             c.gridy = 0;
             c.fill = GridBagConstraints.HORIZONTAL;
             c.anchor = GridBagConstraints.PAGE_START;
             c.weightx = 1.0;
             c.weighty = 0;
          }
          
          @Override
          public void OnInsert(SimpleRow element) {
             mMainPanel.add(element,c);
             c.gridy += 1;
          }
 
          @Override
          public void OnDelete(SimpleRow element) {
             reDrawAll();
          }
          
          private void reDrawAll() {
             mMainPanel.removeAll();
             c.gridy = 0;
             for (SimpleRow wRow : mElementList.values()) {
                mMainPanel.add(wRow, c);
             }
          }
          
       }
       
       @Override
       public void newValue(String iPath, String iValue) {
          if(mElementList.get(iPath) != null)
             mElementList.get(iPath).setValue(iValue);
       }
 
       @Override
       public void newType(String iPath, String iType) {
          if(mElementList.get(iPath) != null)
             mElementList.get(iPath).setType(iType);
       }
 
       @Override
       public void newVisibility(String iPath, String iVisibility) {
          //TODO not supported yet.
       }
 
       @Override
       public void OnDisconnected() {
          mElementList.clear();
          mMainPanel.removeAll();
       }
 
       @Override
       public void OnConnecting() {
       }
 
       @Override
       public void OnConnected() {
       }
    }
 
    @Override
    public void registerPath(String iPath) {
       mMonitorPane.mElementList.put(iPath, new SimpleRow(iPath));
       mServer.registerObject(iPath);
       mServer.forcedUpdate(iPath);
    }
 
    @Override
    public void unregisterPath(String iPath) {
       mMonitorPane.mElementList.remove(iPath);
       mServer.unregisterObject(iPath);
       mServer.forcedUpdate(iPath);
    }
 
    @Override
    public Boolean isRegister(String iPath) {
       return mMonitorPane.mElementList.containsKey(iPath);
    }
    
   
 }
