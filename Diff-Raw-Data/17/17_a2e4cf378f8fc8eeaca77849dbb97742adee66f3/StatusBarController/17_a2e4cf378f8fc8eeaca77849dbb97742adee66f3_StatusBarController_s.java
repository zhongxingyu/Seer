 package jDistsim.application.designer.controller;
 
 import jDistsim.application.designer.model.StatusBarModel;
 import jDistsim.application.designer.view.StatusBarView;
 import jDistsim.utils.action.ActionArgument;
 import jDistsim.utils.action.ActionObjectListener;
 import jDistsim.utils.gc.MemoryMonitoring;
 import jDistsim.utils.logging.LogMessage;
 import jDistsim.utils.logging.Logger;
 import jDistsim.utils.logging.handlers.ILoggerHandler;
 import jDistsim.utils.pattern.mvc.AbstractController;
 import jDistsim.utils.pattern.mvc.AbstractFrame;
 import jDistsim.utils.pattern.observer.IObserver;
 import jDistsim.utils.pattern.observer.Observable;
 
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.10.12
  * Time: 0:10
  */
 public class StatusBarController extends AbstractController<StatusBarModel> implements ILoggerHandler, IObserver {
 
     private final MemoryMonitoring memoryMonitoring;
 
     public StatusBarController(AbstractFrame mainFrame, StatusBarModel statusBarModel) {
         super(mainFrame, statusBarModel);
         Logger.getLoggerHandlerManager().addHandler(this);
         getModel().addObserver(this);
 
         memoryMonitoring = new MemoryMonitoring();
         memoryMonitoring.addActionObjectListener(new ActionObjectListener() {
             @Override
             public void ActionPerformed(ActionArgument actionArgument) {
                 getModel().setRightContentText("Current used memory " + memoryMonitoring.getUsedMemory() + "M of " + memoryMonitoring.getTotalMemory() + "M");
             }
         });
         new Thread(memoryMonitoring).start();
     }
 
     @Override
     public void publish(LogMessage logMessage) {
        getModel().setLeftContentText(logMessage.toString());
     }
 
     @Override
     public void update(Observable observable, Object arguments) {
         StatusBarView view = getMainFrame().getView(StatusBarView.class);
         view.getContentPane().setLabelLeftText(getModel().getLeftContentText());
         view.getContentPane().setLabelRightText(getModel().getRightContentText());
     }
 }
