 package ch.zhaw.i11b.pwork.sem2.server.controller;
 
 import ch.zhaw.i11b.pwork.sem2.server.MessageTask;
 import ch.zhaw.i11b.pwork.sem2.beans.Message;
 import ch.zhaw.i11b.pwork.sem2.beans.Messages;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.UUID;
 
 /**
  * @author  oups
  */
 public class MessageController {
 	//Singleton impl
 	/**
 	 * @uml.property  name="_instance"
 	 * @uml.associationEnd  
 	 */
 	static protected MessageController _instance = null;
 	
 	static public MessageController Instance() {
 		if (_instance == null) {
 			_instance = new MessageController();
 		}
 		return _instance;
 	}
 	
 	/**
 	 * 
 	 */
 	protected MessageController() {
 		// add a empty message to cancled for debug purpos
 		this.messages.cancled.add(new Message());
 	}
 	
 	
 	/**
 	 * @uml.property  name="messages"
 	 * @uml.associationEnd  
 	 */
 	protected Messages messages = new Messages();
 	protected HashMap<String, MessageTask> openTasks = new HashMap<String, MessageTask>();
 	protected HashMap<String, Timer> timers = new HashMap<String, Timer>();
 	
 	//public Interface
 	/**
 	 * @param msg
 	 */
 	public synchronized void addMessage(Message msg) {
 		msg.id = UUID.randomUUID().toString();
 		if (msg.sendtime == null) {
 			msg.sendtime = new Date();
 		}
 		Timer timer = new Timer();
 		MessageTask task = new MessageTask(msg);
 		timer.schedule(task, msg.sendtime);
 		if (msg.reminder) {
 			MessageTask reminderTask = new MessageTask(msg, true);
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(msg.sendtime);
 			cal.set(Calendar.HOUR, -1);
 			timer.schedule(reminderTask, cal.getTime());
 			this.openTasks.put("reminder_"+msg.id, reminderTask);
 		}
 		this.openTasks.put(msg.id, task);
 		this.timers.put(msg.id, timer);
 	}
 	
 	/**
 	 * @param msg
 	 */
 	protected synchronized void clearMessage(Message msg) {
 		Timer timer = this.timers.get(msg.id);
 		timer.cancel();
 		timer.purge();
 		this.timers.remove(msg.id);
 		this.openTasks.remove(msg.id);
 		if (msg.reminder) {
 			this.openTasks.remove("reminder_"+msg.id);
 		}
 		this.messages.open.remove(msg);
 	}
 	
 	/**
 	 * @param msg
 	 */
 	public synchronized void finishMessage(Message msg) {
 		this.clearMessage(msg);
 		this.messages.finished.add(msg);
 	}
 
 	/**
 	 * @param msg
 	 */
 	public synchronized void errorMessage(Message msg) {
 		this.clearMessage(msg);
 		this.messages.errors.add(msg);
 	}
 	
 	/**
 	 * @param msg
 	 */
 	public synchronized void cancleMessage(Message msg) {
 		this.clearMessage(msg);
 		this.messages.cancled.add(msg);
 	}
 	
 	/**
 	 * @return
 	 * @uml.property  name="messages"
 	 */
 	public synchronized Messages getMessages() {
 		return this.messages;
 	}
 }
