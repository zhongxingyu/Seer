 package org.vaadin.chronographer.gwt.client.connect;
 
 import org.vaadin.chronographer.gwt.client.ChronoGrapherWidget;
 import org.vaadin.chronographer.gwt.client.shared.ChronoGrapherState;
 
 import com.google.gwt.core.client.Scheduler;
 import com.google.gwt.core.client.Scheduler.RepeatingCommand;
 import com.vaadin.client.communication.RpcProxy;
 import com.vaadin.client.communication.StateChangeEvent;
 import com.vaadin.client.ui.AbstractComponentConnector;
 import com.vaadin.shared.ui.Connect;
 
 @Connect(org.vaadin.chronographer.ChronoGrapher.class)
 public class ChronoGrapherConnector extends AbstractComponentConnector {
 
     private static final long serialVersionUID = -3030798781172318095L;
 
     ChronoGrapherServerRpc rpc = RpcProxy.create(ChronoGrapherServerRpc.class,
             this);
 
     public ChronoGrapherConnector() {
         getWidget().setRpc(rpc);
     }
 
     @Override
     public ChronoGrapherWidget getWidget() {
         return (ChronoGrapherWidget) super.getWidget();
     }
 
     @Override
     public ChronoGrapherState getState() {
         return (ChronoGrapherState) super.getState();
     }
 
     @Override
     public void onStateChanged(StateChangeEvent stateChangeEvent) {
         super.onStateChanged(stateChangeEvent);
 
         if (getState().initialized) {
             if (stateChangeEvent.hasPropertyChanged("eventsJson")) {
                 getWidget().setEventsJson(getState().eventsJson);
             }
             if (stateChangeEvent.hasPropertyChanged("timelineThemes")) {
                 getWidget().setThemes(getState().timelineThemes);
             }
         }
 
         if (!getState().initialized) {
             Scheduler.get().scheduleFixedDelay(new RepeatingCommand() {
                 @Override
                 public boolean execute() {
                     getWidget().init(getState().width, getState().height,
                             getState().horizontal, getState().bandInfos,
                             getState().timelineThemes, getState().eventsJson);
                     getState().initialized = true;
                     return false;
                 }
            }, 3500);
         }
     }
 }
