 package com.hixi_hyi.idumo.console.exec;
 
 import com.hixi_hyi.idumo.core.ApplicationController;
 import com.hixi_hyi.idumo.core.IdumoException;
 import com.hixi_hyi.idumo.core.IdumoExecution;
 import com.hixi_hyi.idumo.core.IdumoRunnable;
 import com.hixi_hyi.idumo.core.IdumoRuntimeException;
 import com.hixi_hyi.idumo.core.exec.AbstractExecutionComponent;
 import com.hixi_hyi.idumo.core.front.IdumoContainer;
 
 public class ConsoleExecution implements IdumoExecution {
 	private AbstractExecutionComponent	component;

 	public ConsoleExecution(AbstractExecutionComponent component) {
 		this.component = component;
 		this.component.setContainer(new IdumoContainer());
 	}

 	@Override
 	public void onIdumoCreated() throws IdumoException {
 		component.onIdumoMakeFlowChart();
 		component.setup();
 		component.onIdumoPrepare();
 	}

 	@Override
 	public void onIdumoStart() {
 		component.onIdumoPrepare();
 		for (ApplicationController controller : component.getApplicationControllers()) {
 			controller.onIdumoStart();
 		}
 		component.setReady(true);
 	}

 	@Override
 	public void onIdumoStop() {
 		for (ApplicationController controller : component.getApplicationControllers()) {
 			controller.onIdumoStop();
 		}
 		component.setReady(false);
 	}

 	@Override
 	public void onIdumoExec() throws IdumoRuntimeException {
 		while (!component.isReady()) {
 			try {
 				Thread.sleep(component.getSleepTime());
 			} catch (InterruptedException e) {}
 		}
 		IdumoRunnable runnable = component.getRunnable();
 		while (!runnable.isReady()) {
 			try {
 				Thread.sleep(component.getSleepTime());
 			} catch (InterruptedException e) {}
 		}
 		int count = component.getLoopCount();
 		if (count == -1) {
			while(true){
				runnable.run();
 				try {
 					Thread.sleep(component.getSleepTime());
 				} catch (InterruptedException e) {}
 			}
 		} else {
			for (int i = 0; i < count; i++) {
				runnable.run();
 				try {
 					Thread.sleep(component.getSleepTime());
 				} catch (InterruptedException e) {}
 			}
 		}

 	}
 }
