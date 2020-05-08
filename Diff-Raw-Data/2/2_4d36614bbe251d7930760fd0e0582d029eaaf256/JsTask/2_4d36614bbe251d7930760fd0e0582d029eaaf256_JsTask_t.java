 package com.theminequest.common.quest.js;
 
 import static com.theminequest.common.util.I18NMessage._;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import org.mozilla.javascript.Context;
 import org.mozilla.javascript.Scriptable;
 import org.mozilla.javascript.ScriptableObject;
 
 import com.theminequest.api.CompleteStatus;
 import com.theminequest.api.Managers;
 import com.theminequest.api.quest.Quest;
 import com.theminequest.api.quest.QuestTask;
 import com.theminequest.api.quest.event.QuestEvent;
 
 public class JsTask implements QuestTask {
 	
 	private static volatile ScriptableObject STD_OBJ = null;
 	private static final Object SYNCLOCK = new Object();
 	
 	private JsQuest quest;
 	private Thread jsThread;
 	private JsObserver observer;
 	
 	private CompleteStatus status;
 	private String taskDescription;
 	
 	protected JsTask(JsQuest quest) {
 		this.quest = quest;
 		this.jsThread = null;
 		this.observer = new JsObserver();
 		
 		this.status = null;
 		this.taskDescription = _("No description given - ask the quest maker to use util.setTaskDescription!");
 	}
 	
 	@Override
 	public void start() {
 		if (jsThread != null)
 			return;
 		
 		jsThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				Context cx = Context.enter();
 				
 				try {
 					synchronized(SYNCLOCK) {
 						if (STD_OBJ == null) {
 							STD_OBJ = cx.initStandardObjects(null, true);
 							STD_OBJ.sealObject();
 						}
 					}
 					
 					Scriptable global = cx.newObject(STD_OBJ);
 					global.setPrototype(STD_OBJ);
 					global.setParentScope(null);
 					
 					ScriptableObject.putConstProperty(global, "details", Context.toObject(quest.getDetails(), global));
 					ScriptableObject.putConstProperty(global, "color", Context.toObject(Managers.getPlatform().chatColor(), global));
 					ScriptableObject.putConstProperty(global, "util", Context.toObject(new JsQuestFunctions(JsTask.this, global), global));
 					
 					cx.setDebugger(observer, new Integer(0));
 					cx.setGeneratingDebug(true);
 					cx.setOptimizationLevel(-1);
 					
 					// FIXME we don't specify security for now
					Object result = cx.evaluateString(global, (String) quest.getDetails().getProperty(JsQuestDetails.JS_SOURCE), quest.getDetails().getName(), (int) quest.getDetails().getProperty(JsQuestDetails.JS_LINESTART), null);
 					
 					if (observer.isDisconnected()) {
 						Managers.getPlatform().scheduleSyncTask(new Runnable() {
 
 							@Override
 							public void run() {
 								status = CompleteStatus.CANCELED;
 								completed();
 							}
 							
 						});
 						return;
 					}
 					
 					status = CompleteStatus.ERROR;
 					
 					if (result == null || result.equals(0))
 						status = CompleteStatus.SUCCESS;
 					else if (result.equals(1))
 						status = CompleteStatus.FAIL;
 					else if (result.equals(2))
 						status = CompleteStatus.WARNING;
 					else if (result.equals(-2))
 						status = CompleteStatus.IGNORE;
 					else if (result.equals(-1))
 						status = CompleteStatus.CANCELED;
 					
 					Managers.getPlatform().scheduleSyncTask(new Runnable() {
 
 						@Override
 						public void run() {
 							completed();
 						}
 						
 					});
 					
 				} finally {
 					Context.exit();
 				}
 			}
 			
 		});
 		
 		jsThread.start();
 	}
 	
 	private void completed() {
 		quest.completeTask(this, status, -1);
 	}
 	
 	@Override
 	public CompleteStatus isComplete() {
 		return status;
 	}
 	
 	@Override
 	public Quest getQuest() {
 		return quest;
 	}
 	
 	@Override
 	public int getTaskID() {
 		return 0;
 	}
 	
 	@Override
 	public String getTaskDescription() {
 		return taskDescription;
 	}
 	
 	protected void setTaskDescription(String taskDescription) {
 		this.taskDescription = taskDescription;
 	}
 	
 	@Override
 	public Collection<QuestEvent> getEvents() {
 		return new LinkedList<QuestEvent>();
 	}
 	
 	@Override
 	public void checkTasks() {
 		// does nothing
 	}
 	
 	@Override
 	public void cancelTask() {
 		// this will toggle the Js runtime to stop
 		// and will call (as above) completed(CANCELED)
 		observer.setDisconnected(true);
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status) {
 		// does nothing (but maybe it should?)
 	}
 	
 	@Override
 	public void completeEvent(QuestEvent event, CompleteStatus status, int nextTask) {
 		// does nothing (but maybe it should?)
 	}
 	
 }
