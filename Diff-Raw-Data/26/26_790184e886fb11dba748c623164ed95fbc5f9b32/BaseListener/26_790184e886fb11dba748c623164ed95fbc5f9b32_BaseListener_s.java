 package com.laboki.eclipse.plugin.jcolon.listeners.abstraction;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.laboki.eclipse.plugin.jcolon.instance.EventBusInstance;
 import com.laboki.eclipse.plugin.jcolon.instance.Instance;
 import com.laboki.eclipse.plugin.jcolon.main.EditorContext;
 import com.laboki.eclipse.plugin.jcolon.task.BaseTask;
 import com.laboki.eclipse.plugin.jcolon.task.Task;
 import com.laboki.eclipse.plugin.jcolon.task.TaskMutexRule;
 
 public abstract class BaseListener extends EventBusInstance
 	implements
 		IListener {
 
 	private static final int ONE_SECOND = 1000;
 	public static final String FAMILY = "ABSTRACT_LISTENER_FAMILY";
 	private static final TaskMutexRule RULE = new TaskMutexRule();
 	private static final Logger LOGGER =
 		Logger.getLogger(BaseListener.class.getName());
 
	public BaseListener() {
		super();
	}

 	@Override
 	public final Instance
 	start() {
 		try {
 			this.add();
 		}
 		catch (final Exception e) {
 			BaseListener.LOGGER.log(Level.FINEST, e.getMessage(), e);
 		}
 		return super.start();
 	}
 
 	@Override
	public void
	add() {}
 
 	@Override
 	public final Instance
 	stop() {
 		try {
 			this.remove();
 		}
 		catch (final Exception e) {
 			BaseListener.LOGGER.log(Level.FINEST, e.getMessage(), e);
 		}
 		return super.stop();
 	}
 
 	@Override
	public void
	remove() {}
 
 	protected final static void
 	scheduleErrorChecking() {
		EditorContext.cancelAllJobs();
		BaseListener.scheduleTask();
 	}
 
 	protected static void
 	scheduleTask() {
 		new Task() {
 
 			@Override
 			public boolean
 			shouldSchedule() {
 				return BaseTask.noTaskFamilyExists(BaseListener.FAMILY);
 			}
 
 			@Override
 			public void
 			execute() {
 				EditorContext.scheduleErrorChecking();
 			}
 		}.setRule(BaseListener.RULE)
 			.setFamily(BaseListener.FAMILY)
 			.setDelay(BaseListener.ONE_SECOND)
 			.start();
 	}
 }
