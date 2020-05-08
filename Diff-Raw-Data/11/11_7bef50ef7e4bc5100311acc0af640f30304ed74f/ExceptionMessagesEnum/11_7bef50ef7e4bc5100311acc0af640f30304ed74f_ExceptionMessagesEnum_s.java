 package com.operativus.senacrs.audit.exceptions;
 
 import com.operativus.senacrs.audit.messages.Messages;
 
 public enum ExceptionMessagesEnum implements Messages {
 
	EVAL_TYPE_MISMATCH("exception.eval.type.mismatch"),
 	NULL_ARGUMENT("exception.null.argument"),
	MISSING_MINIMAL_CONFIG_ENTRY("exception.missing.config.entry"), ;
 
 	private final String key;
 
 	private ExceptionMessagesEnum(final String key) {
 
 		this.key = key;
 	}
 
 	@Override
 	public String getKey() {
 
 		return this.key;
 	}
 }
