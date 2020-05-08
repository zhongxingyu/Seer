 package com.operativus.senacrs.audit.exceptions;
 
 import com.operativus.senacrs.audit.properties.PropertyKey;
 
 public enum ExceptionMessagesEnum implements PropertyKey {
 
 	//Runtime
 	NULL_ARGUMENT("exception.null.argument"),
 	//Form
 	EVAL_TYPE_MISMATCH("exception.eval.type.mismatch"),
 	//Config
 	MISSING_MINIMAL_CONFIG_ENTRY("exception.missing.config.entry"),
 	//Graph
 	ILLEGAL_START_NODE("exception.illegal.start.node"),
 	ILLEGAL_NODE_TYPE("exception.illegal.node.type"),
	//WebDriver
	ILLEGAL_XPATH_PARAM_AMOUNT("exception.illegal.xpath.param.amount"),
 	;
 
 	private final String key;
 
 	private ExceptionMessagesEnum(final String key) {
 
 		this.key = key;
 	}
 
 	@Override
 	public String getKey() {
 
 		return this.key;
 	}
 }
