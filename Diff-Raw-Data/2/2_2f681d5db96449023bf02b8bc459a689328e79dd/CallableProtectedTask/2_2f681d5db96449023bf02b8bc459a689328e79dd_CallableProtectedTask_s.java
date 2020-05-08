 package net.idea.restnet.c.task;
 
 import java.util.UUID;
 
 import net.idea.restnet.i.aa.IAuthToken;
 import net.idea.restnet.i.task.ICallableTask;
 import net.idea.restnet.i.task.TaskResult;
 
 public abstract class CallableProtectedTask<USERID> implements ICallableTask, IAuthToken {
 
 	protected UUID uuid;
 	private USERID token;
 	protected String title;
 	
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public CallableProtectedTask(USERID token) {
 		this.token = token;
 	}
 	
 	@Override
 	public UUID getUuid() {
 		return uuid;
 	}
 
 	@Override
 	public void setUuid(UUID uuid) {
 		this.uuid = uuid;
 
 	}
 
 	@Override
 	public String getToken() {
 		return token==null?null:token.toString();
 	}
 	public abstract TaskResult doCall() throws Exception;
 	
 	@Override
 	public TaskResult call() throws Exception {
 		try {
 			ClientResourceWrapper.setTokenFactory(this);
 			return doCall();
 		} catch (Exception x) {
 			throw x;
 		} finally {
 			ClientResourceWrapper.setTokenFactory(null);
 		}
 	}
 	
 	@Override
 	public String getTaskCategory() {
 		return null;
 	}
 
 	
 	@Override
 	public String toString() {
		return title==null?toString():title;
 	}
 }
