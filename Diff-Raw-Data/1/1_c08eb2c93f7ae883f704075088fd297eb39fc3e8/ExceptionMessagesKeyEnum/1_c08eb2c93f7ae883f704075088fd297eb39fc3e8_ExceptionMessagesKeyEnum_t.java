 package com.operativus.senacrs.audit.exceptions;
 
 import com.operativus.senacrs.audit.properties.PropertyKey;
 
 public enum ExceptionMessagesKeyEnum implements PropertyKey {
 
 	// Runtime
 	NULL_ARGUMENT("exception.null.argument"),
 	// Form
 	EVAL_TYPE_MISMATCH("exception.eval.type.mismatch"),
 	// Config
 	MISSING_MINIMAL_CONFIG_ENTRY("exception.missing.config.entry"),
 	// Graph
 	ILLEGAL_NODE_START("exception.illegal.node.start"),
 	ILLEGAL_NODE_TYPE("exception.illegal.node.type"),
 	// WebDriver
 	ILLEGAL_NODE_CLASS("exception.illegal.node.class"),
	ILLEGAL_NODE_START_TYPE("exception.illegal.node.start.type"),
 	ILLEGAL_XPATH_PARAM_AMOUNT("exception.illegal.xpath.param.amount"),
 	// Variarg
 	ILLEGAL_VARARGS_AMOUNT("exception.illegal.varargs.amount"), ;
 
 	private final String key;
 
 	private ExceptionMessagesKeyEnum(final String key) {
 
 		this.key = key;
 	}
 
 	@Override
 	public String getKey() {
 
 		return this.key;
 	}
 }
