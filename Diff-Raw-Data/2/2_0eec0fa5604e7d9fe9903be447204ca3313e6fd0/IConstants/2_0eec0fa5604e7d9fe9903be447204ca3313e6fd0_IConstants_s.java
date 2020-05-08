 /*******************************************************************************
  * Copyright (c) 2006 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Oracle Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.bpel.validator.model;
 
 
 /**
  * @author Michal Chmielewski (michal.chmielewski@oracle.com)
  * @date Sep 18, 2006
  *
  */
 
 @SuppressWarnings("nls")
 public interface IConstants {
 
 	/**
 	 * Ask for the resource Path that the INode belongs to.
 	 */
 	public static final String META_RESOURCE_PATH = "meta.resource.path"; //$NON-NLS-1$
 	
 	
 	/**
 	 * We use the JAXEN XPath Expression parser. Return the function meta
 	 * information for the validator from IModelQuery.
 	 */
 	public static final String META_JAXEN_XPATH_FUNCTION_CONTEXT = "meta.jaxen.xpath.function.context"; //$NON-NLS-1$
 	
 	/** 
 	 * Parse a duration string.
 	 */
 	public static final String META_XML_PARSE_DURATION = "meta.parse.xml.duration"; //$NON-NLS-1$
 	
 	/**
 	 * Parse XML Date and time
 	 */
 	public static final String META_XML_PARSE_DATE_AND_TIME = "meta.parse.xml.dateAndTime"; //$NON-NLS-1$
 	
 	/**
 	 * Will print errors/warnings if the runtime indicates that XPath functions must be resovled
 	 * and there is no meta information about them.
 	 */
 	public static final String META_XPATH_MUST_RESOLVE_FUNCTIONS = "meta.must.resolve.functions";  //$NON-NLS-1$
 	
 	/**
 	 * Lookup the prefix and return the Namespace given.
 	 */
 	
 	public static final String META_XMLNS_PREFIX2NS = "meta.prefix.to.ns";
 	
 	
 	/** The default expression language */
 	public static final String XMLNS_XPATH_EXPRESSION_LANGUAGE  = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0"; //$NON-NLS-1$
 
 	/** A variant from previous spec iterations */
 	public static final String XMLNS_XPATH_EXPRESSION_LANGUAGE_2 = "http://www.w3.org/TR/1999/REC-xpath-19991116"; //$NON-NLS-1$
 	
 	/** The default query language */
 	public static final String XMLNS_XPATH_QUERY_LANGUAGE = "urn:oasis:names:tc:wsbpel:2.0:sublang:xpath1.0";
 
 	public static final String XMLNS_XPATH_QUERY_LANGUAGE_2 = "http://www.w3.org/TR/1999/REC-xpath-19991116";
 	
 	/** Namespaces ...
 	 * 
 	 */
 	
 	public static final String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
 
 	public static final String XMLNS_XSD = "http://www.w3.org/2001/XMLSchema";
 
 	public static final String XMLNS_WSDL = "http://schemas.xmlsoap.org/wsdl/";
 
 	/** vprop namespace */
 	public static final String XMLNS_VPROP = "http://docs.oasis-open.org/wsbpel/2.0/varprop";
 
 	/** old vprop namespace */
 	public static final String XMLNS_VPROP_OLD = "http://schemas.xmlsoap.org/ws/2004/03/business-process/";
 	
 	
 	public static final String XMLNS_SREF = "http://docs.oasis-open.org/wsbpel/2.0/serviceref";
 
 	/** Partner link namespace */
 	public static final String XMLNS_PLNK = "http://docs.oasis-open.org/wsbpel/2.0/plnktype";
 
 	/** Other partner link namespace */
 	public static final String XMLNS_PLNK_OLD = "http://schemas.xmlsoap.org/ws/2004/03/partner-link/";
 
 	
 	public static final String XMLNS_BPEL20_EXECUTABLE = "http://docs.oasis-open.org/wsbpel/2.0/process/executable";
 	
 	public static final String XMLNS_BPEL20_OLD = "http://schemas.xmlsoap.org/ws/2004/03/business-process/"; //$NON-NLS-1$
 	
 	public static final String XMLNS_ABSTRACT = "http://docs.oasis-open.org/wsbpel/2.0/process/abstract";
 	
 	
 	/**
 	 *  The BPEL 1.1 namespace.
 	 */	
 	public static final String XMLNS_BPEL_11 = "http://";  //$NON-NLS-1$
 	
 	/**
 	 * The default BPEL 2.0 namespace.
 	 */
 	public static final String XMLNS_BPEL = XMLNS_BPEL20_EXECUTABLE;
 	
 	//
     // constants
     //
     public static final String YES = "yes";
     public static final String NO  = "no";
     public static final String JOIN = "join";
     
     public static final String REQUEST = "request";
     public static final String RESPONSE = "response";
     public static final String REQUEST_RESPONSE = "request-response";
     //
     
     public static final String XMLNS = "xmlns";
     
     // node names (bpel XML)
     public static final String ND_PROCESS = "process";
     public static final String ND_EMPTY = "empty";
     public static final String ND_INVOKE = "invoke";
     public static final String ND_RECEIVE = "receive";
     public static final String ND_REPLY = "reply";
     public static final String ND_ASSIGN = "assign";
     public static final String ND_WAIT = "wait";
     public static final String ND_THROW = "throw";
     public static final String ND_FLOW = "flow";
     public static final String ND_WHILE = "while";
     public static final String ND_REPEAT_UNTIL = "repeatUntil";
     public static final String ND_SEQUENCE = "sequence";
     public static final String ND_PICK = "pick";
     public static final String ND_SCOPE = "scope";
     public static final String ND_PARTNER_LINKS = "partnerLinks";
     public static final String ND_PARTNER_LINK = "partnerLink";
     public static final String ND_EVENT_HANDLERS = "eventHandlers";
     public static final String ND_FAULT_HANDLERS = "faultHandlers";
     public static final String ND_CATCH = "catch";
     public static final String ND_CATCH_ALL = "catchAll";
     public static final String ND_ON_MESSAGE = "onMessage";
     public static final String ND_ON_ALARM = "onAlarm";
     public static final String ND_ON_EVENT = "onEvent";
     public static final String ND_VARIABLES = "variables";
     public static final String ND_VARIABLE = "variable";
     public static final String ND_CORRELATION_SETS = "correlationSets";
     public static final String ND_CORRELATION_SET = "correlationSet";
     public static final String ND_SOURCE = "source";
     public static final String ND_SOURCES = "sources";
     public static final String ND_TARGET = "target";
     public static final String ND_TARGETS = "targets";
     public static final String ND_JOIN_CONDITION = "joinCondition";
     public static final String ND_TRANSITION_CONDITION = "transitionCondition";
     public static final String ND_COPY = "copy";
     public static final String ND_FROM = "from";
     public static final String ND_TO = "to";
     public static final String ND_LINKS = "links";
     public static final String ND_LINK = "link";        
     public static final String ND_IF = "if";
     public static final String ND_ELSEIF = "elseif";
     public static final String ND_ELSE = "else";
     public static final String ND_IMPORT = "import";
     public static final String ND_FROM_PART = "fromPart";
     public static final String ND_FROM_PARTS = "fromParts";
     public static final String ND_TO_PART = "toPart";
     public static final String ND_TO_PARTS = "toParts";
     public static final String ND_LITERAL = "literal";
     public static final String ND_QUERY = "query";
     public static final String ND_CONDITION = "condition";
     public static final String ND_UNTIL = "until";
     public static final String ND_FOR = "for";
     
     public static final String ND_FOR_EACH = "forEach";
     public static final String ND_START_COUNTER_VALUE = "startCounterValue";
     public static final String ND_FINAL_COUNTER_VALUE = "finalCounterValue";
     public static final String ND_COMPLETION_CONDITION = "completionCondition";
     public static final String ND_BRANCHES = "branches";
     public static final String ND_EXIT = "exit";
     public static final String ND_COMPENSATE_SCOPE = "compensateScope";
     public static final String ND_VALIDATE = "validate";
     public static final String ND_EXTENSION_ACTIVITY = "extensionActivity";
     
     public static final String ND_EXTENSION_ASSIGN_OPERATION = "extensionAssignOperation";
     
     public static final String ND_CORRELATIONS = "correlations";
     public static final String ND_CORRELATION = "correlation";
     public static final String ND_COMPENSATE = "compensate";
     public static final String ND_COMPENSATION_HANDLER = "compensationHandler";
     public static final String ND_RETHROW = "rethrow";
     public static final String ND_TERMINATION_HANDLER = "terminationHandler";
     public static final String ND_REPEAT_EVERY = "repeatEvery";
     
     public static final String ND_MESSAGE_EXCHANGES = "messageExchanges";
     public static final String ND_MESSAGE_EXCHANGE = "messageExchange";
     
     public static final String ND_EXTENSION = "extension";
     public static final String ND_EXTENSIONS = "extensions";
     
     
   
     // Attribute nodes
     public static final String AT_NAME = "name";
     public static final String AT_TARGET_NAMESPACE = "targetNamespace";    
     public static final String AT_ISOLATED = "isolated";
     public static final String AT_QUERYLANGUAGE = "queryLanguage";
     public static final String AT_EXPRESSIONLANGUAGE = "expressionLanguage";
     public static final String AT_EXIT_ON_STANDARD_FAULT = "exitOnStandardFault";    
     public static final String AT_SUPPRESS_JOIN_FAILURE = "suppressJoinFailure";
     public static final String AT_JOIN_CONDITION = "joinCondition";
     public static final String AT_VARIABLE_ACCESS_SERIALIZABLE = "variableAccessSerializable";
     public static final String AT_ENABLE_INSTANCE_COMPENSATION = "enableInstanceCompensation";
     public static final String AT_ABSTRACT_PROCESSES = "abstractProcess";
     public static final String AT_PARTNER_LINK_TYPE = "partnerLinkType";
     public static final String AT_FAULT_NAME = "faultName";
     public static final String AT_FAULT_VARIABLE = "faultVariable";
     public static final String AT_PARTNER = "partner";
     public static final String AT_PARTNER_LINK = "partnerLink";
     public static final String AT_PORT_TYPE = "portType";
     public static final String AT_OPERATION = "operation";
     public static final String AT_VARIABLE = "variable";
     public static final String AT_FOR = "for";
     public static final String AT_UNTIL = "until";
     public static final String AT_MESSAGE_TYPE = "messageType";
     public static final String AT_TYPE = "type";
     public static final String AT_ELEMENT = "element";
     public static final String AT_PROPERTIES = "properties";
     public static final String AT_LINK_NAME = "linkName";
     public static final String AT_TRANSITION_CONDITION = "transitionCondition";
     public static final String AT_INPUT_VARIABLE = "inputVariable";
     public static final String AT_OUTPUT_VARIABLE = "outputVariable";
     public static final String AT_CREATE_INSTANCE = "createInstance";
     public static final String AT_PART = "part";
     public static final String AT_QUERY = "query";
     public static final String AT_OPAQUE = "opaque";
     public static final String AT_PROPERTY = "property";
     public static final String AT_EXPRESSION = "expression";
     public static final String AT_CONDITION = "condition";
     public static final String AT_MY_ROLE = "myRole";
     public static final String AT_PARTNER_ROLE = "partnerRole";
     public static final String AT_SET = "set";
     public static final String AT_INITIATE = "initiate";
     public static final String AT_PATTERN = "pattern";
     public static final String AT_LANGUAGE = "language";
     public static final String AT_VERSION = "version";
     public static final String AT_IMPORT = "import";
     public static final String AT_IMPORT_TYPE = "importType";
     public static final String AT_LOCATION = "location";
     public static final String AT_NAMESPACE = "namespace";
     public static final String AT_ENDPOINT_REFERENCE = "endpointReference";
     public static final String AT_SCOPE = "scope";
     public static final String AT_LABEL = "label";
     public static final String AT_LABEL_STATUS = "status";
     public static final String AT_TIMEOUT = "timeout";
     public static final String AT_SUPPRESS_COORD_FAILURE = "suppressCoordinationFailure";
     public static final String AT_FROM = "from";
     public static final String AT_TO = "to";
     public static final String AT_VARIABLES = "variables";
     public static final String AT_TARGET = "target";
     public static final String AT_TO_VARIABLE = "toVariable";
     public static final String AT_FROM_VARIABLE = "fromVariable";
     public static final String AT_INITIALIZE_PARTNER_ROLE = "initializePartnerRole";
     public static final String AT_VALIDATE = "validate";
     public static final String AT_KEEP_SRC_ELEMENT_NAME = "keepSrcElementName";
     public static final String AT_ACTIVITY = "activity";
     public static final String AT_ACTIVITIES = "activities";
     public static final String AT_LINKS = "links";
     public static final String AT_SUCCESSFUL_BRANCHES_ONLY = "successfulBranchesOnly" ;    
     public static final String AT_FAULT_MESSAGE_TYPE = "faultMessageType";
     public static final String AT_FAULT_ELEMENT = "faultElement";
     public static final String AT_COUNTER_NAME = "counterName";
     public static final String AT_PARALLEL = "parallel";
     public static final String AT_KEEP_SRC_ELEMENT = "keepSrcElement";
     public static final String AT_MUST_UNDERSTAND = "mustUnderstand";
     
     // Some attribute values
     public static final String AT_VAL_IMPORT_XSD  = "http://www.w3.org/2001/XMLSchema";
     public static final String AT_VAL_IMPORT_WSDL = "http://schemas.xmlsoap.org/wsdl/";
         
     
     /** These are used in message.properties */
     public static final int    KIND_ATTRIBUTE= 2;
     public static final int    KIND_ACTIVITY = 1;
     public static final int    KIND_NODE     = 0;
     
     
     // WSDL nodes of interest
     public static final String WSDL_ND_OPERATION = "operation";
     public static final String WSDL_ND_MESSAGE = "message";
     public static final String WSDL_ND_PARTNER_LINK_TYPE = "partnerLinkType";
     public static final String WSDL_ND_ROLE = "role";
     
     public static final String WSDL_ND_PORT_TYPE = "portType";
     public static final String WSDL_ND_PART = "part";
     
     public static final String WSDL_ND_PROPERTY = "property";
     public static final String WSDL_ND_PROPERTY_ALIAS = "propertyAlias";    
     public static final String WSDL_ND_QUERY = "query";
     
     public static final String WSDL_ND_DEFINITIONS = "definitions";
     
     public static final String WSDL_ND_INPUT = "input";
     public static final String WSDL_ND_OUTPUT = "output";
     public static final String WSDL_ND_FAULT = "fault";
     
     // WSDL attributes of interest
     public static final String WSDL_AT_INPUT = "input";
     public static final String WSDL_AT_OUTPUT = "output";
     public static final String WSDL_AT_MESSAGE = "message";
     public static final String WSDL_AT_PROPERTY_NAME = "propertyName";
     
     
     // Property
     public static final String EXT_ND_PROPERTY = "property";    
     
     public static final String BOOLEAN_VALUES[] = { YES, NO };
     
     public static final String INITIATE_VALUES[] = { YES, JOIN, NO };
     
     public static final String ENDPOINT_VALUES [] = { AT_MY_ROLE, AT_PARTNER_ROLE };
     
     public static final String PATTERN_VALUES [] = { REQUEST , RESPONSE, REQUEST_RESPONSE };
     
     public static final String REPEATABLE_NODES [] = { 
     	ND_WHILE, ND_REPEAT_UNTIL, ND_FOR_EACH, ND_EVENT_HANDLERS, ND_COMPENSATION_HANDLER }; 
    
     
     public static final String FAULT_HANDLER_BOUNDARY_NODES [] = {
     	ND_CATCH,ND_CATCH_ALL,ND_TERMINATION_HANDLER
     };
     
     /** FCT-Handlers as defined by the spec */
     public static final String FCT_HANDLERS [] = {
     	ND_CATCH, ND_CATCH_ALL, ND_COMPENSATION_HANDLER, ND_TERMINATION_HANDLER 
     };
     
     static String[] BPEL_ACTIVITIES = {
    		ND_RECEIVE,
 		ND_REPLY,
 		ND_INVOKE,
 		ND_ASSIGN,
 		ND_THROW,
 		ND_EXIT,
 		ND_WAIT,
 		ND_EMPTY,
 		ND_SEQUENCE,
 		ND_IF,
 		ND_WHILE,
 		ND_REPEAT_UNTIL,
 		ND_FOR_EACH,
 		ND_PICK,
 		ND_FLOW,
 		ND_SCOPE,
 		ND_COMPENSATE,
 		ND_COMPENSATE_SCOPE,
 		ND_RETHROW,
 		ND_VALIDATE,
 		ND_EXTENSION_ACTIVITY 	
 		
 		/*
 		<receive>, <reply>, <invoke>, <assign>, <throw>, <exit>, <wait>
 		<empty>, <sequence>, <if>, <while>, <repeatUntil>, <forEach>, <pick>
 		<flow>, <scope>, <compensate>, <compensateScope>, <rethrow>, <validate>
 		<extensionActivity>
 		*/
     };
     
     
     static String[] BPEL_ACTIVITIES_CONTAINERS = {
 		ND_SEQUENCE,
 		ND_IF,
 		ND_WHILE,
 		ND_REPEAT_UNTIL,
 		ND_FOR_EACH,
 		ND_PICK,
 		ND_FLOW,
 		ND_PROCESS,
 		ND_SCOPE,
 		ND_COMPENSATE,
 		ND_COMPENSATE_SCOPE,
 		ND_CATCH,
 		ND_CATCH_ALL,
 		ND_EXTENSION_ACTIVITY ,
 		
 		/** These three below can contain activities too */
 		ND_ON_ALARM,
 		ND_ON_EVENT,
 		ND_ON_MESSAGE,
 		
 		/** Strangely enough these two can as well. */
 		ND_TERMINATION_HANDLER,
 		ND_COMPENSATION_HANDLER
 		
 		/*
 		<receive>, <reply>, <invoke>, <assign>, <throw>, <exit>, <wait>
 		<empty>, <sequence>, <if>, <while>, <repeatUntil>, <forEach>, <pick>
 		<flow>, <scope>, <compensate>, <compensateScope>, <rethrow>, <validate>
 		<extensionActivity>
 		*/
     };
  
     static String[] BPEL_STANDARD_FAULTS = {
   			"ambiguousReceive",
   			"completionConditionFailure",
     		"conflictingReceive",
     		"conflictingRequest",
     		"correlationViolation",
     		"invalidBranchCondition",
     		"invalidExpressionValue",
     		"invalidVariables",
     		"joinFailure",
     		"mismatchedAssignmentFailure",
     		"missingReply",
     		"missingRequest",
     		"scopeInitializationFailure",
     		"selectionFailure",
     		"subLanguageExecutionFault",
     		"uninitializedPartnerRole",
     		"uninitializedVariable",
     		"unsupportedReference",
     		"xsltInvalidSource",
     		"xsltStylesheetNotFound"
     };
     
 }
