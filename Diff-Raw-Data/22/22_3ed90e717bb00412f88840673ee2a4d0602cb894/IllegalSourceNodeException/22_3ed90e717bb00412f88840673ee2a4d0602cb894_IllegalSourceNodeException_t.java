 package com.operativus.senacrs.audit.graph.edges;
 
import java.util.Arrays;

 import com.operativus.senacrs.audit.exceptions.ExceptionMessagesEnum;
 import com.operativus.senacrs.audit.graph.nodes.Node;
 import com.operativus.senacrs.audit.messages.Messages;
 import com.operativus.senacrs.audit.messages.MessagesCentral;
 
 @SuppressWarnings("serial")
 public class IllegalSourceNodeException
 		extends IllegalArgumentException {
 
	public IllegalSourceNodeException(final Node source, final Node... valid) {
 
		super(getMessage(source, valid));
 	}
 
	private static String getMessage(final Node source, final Node... valid) {
 
 		String result = null;
 		Messages key = null;
 
		key = ExceptionMessagesEnum.ILLEGAL_START_NODE;
		result = MessagesCentral.getMessage(key, source, Arrays.toString(valid));
 
 		return result;
 	}
 
	public IllegalSourceNodeException(final Throwable cause, final Node source, final Node... valid) {
 
		super(getMessage(source, valid), cause);
 	}
 }
