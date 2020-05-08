 package com.operativus.senacrs.audit.graph.edges;
 
 import com.operativus.senacrs.audit.exceptions.ExceptionMessagesEnum;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.messages.Messages;
 import com.operativus.senacrs.audit.messages.MessagesCentral;
 
 @SuppressWarnings("serial")
 public class IllegalSourceNodeException
 		extends IllegalArgumentException {
 
	public IllegalSourceNodeException(final Node file) {
 
		super(getMessage(file));
 	}
 
	private static String getMessage(final Node file) {
 
 		String result = null;
 		Messages key = null;
 
		key = ExceptionMessagesEnum.EVAL_TYPE_MISMATCH;
		result = MessagesCentral.getMessage(key, file);
 
 		return result;
 	}
 
	public IllegalSourceNodeException(final Throwable cause, final Node file) {
 
		super(getMessage(file), cause);
 	}
 }
