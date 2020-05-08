 package ljas.commons.application.client;
 
 import java.util.List;
 
 import ljas.commons.exceptions.TaskException;
 
 public class ClientApplicationException extends Exception {
 	private static final long serialVersionUID = -5654461562758170368L;
 
 	public ClientApplicationException(String message) {
 		super(message);
 	}
 
 	public ClientApplicationException(Throwable t) {
 		super(t);
 	}
 
 	public ClientApplicationException(List<TaskException> taskExceptions) {
 		super(buildMessageForMultipleTaskExceptions(taskExceptions));
 	}
 
 	private static String buildMessageForMultipleTaskExceptions(
 			List<TaskException> taskExceptions) {
 		final String LB = "\n";
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("The following exceptions occured while executing a task:");
 		sb.append(LB);
 		for (TaskException taskException : taskExceptions) {
 			sb.append(taskException.getMessage());
 			sb.append(LB);
 		}
 
 		return sb.toString();
 	}
 }
