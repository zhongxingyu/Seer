 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.dbgp.internal.utils;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.dbgp.IDbgpSessionInfo;
 import org.eclipse.dltk.dbgp.IDbgpStatus;
 import org.eclipse.dltk.dbgp.breakpoints.IDbgpBreakpoint;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.dbgp.exceptions.DbgpProtocolException;
 import org.eclipse.dltk.dbgp.internal.DbgpFeature;
 import org.eclipse.dltk.dbgp.internal.DbgpProperty;
 import org.eclipse.dltk.dbgp.internal.DbgpSessionInfo;
 import org.eclipse.dltk.dbgp.internal.DbgpStackLevel;
 import org.eclipse.dltk.dbgp.internal.DbgpStatus;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpCallBreakpoint;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpConditionalBreakpoint;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpExceptionBreakpoint;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpLineBreakpoint;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpReturnBreakpoint;
 import org.eclipse.dltk.dbgp.internal.breakpoints.DbgpWatchBreakpoint;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class DbgpXmlEntityParser extends DbgpXmlParser {
 
 	private static String ENCODING_NONE = "none";
 	private static String ENCODING_BASE64 = "base64";
 
 	protected DbgpXmlEntityParser() {
 
 	}
 
 	public static DbgpStackLevel parseStackLevel(Element element)
 			throws DbgpException {
 		final String ATTR_LEVEL = "level";
 		final String ATTR_CMDBEGIN = "cmdbegin";
 		final String ATTR_CMDEND = "cmdend";
 		final String ATTR_LINENO = "lineno";
 		final String ATTR_FILENAME = "filename";
 		final String ATTR_WHERE = "where";
 
 		int level = Integer.parseInt(element.getAttribute(ATTR_LEVEL));
 
 		// TODO: understand type attribute
 		// String type = element.getAttribute(ATTR_TYPE);
 
 		String cmdBegin = element.getAttribute(ATTR_CMDBEGIN);
 		String cmdEnd = element.getAttribute(ATTR_CMDEND);
 
 		int lineBegin = -1;
 		int lineEnd = -1;
 		if (!"".equals(cmdBegin) && !"".equals(cmdEnd)) {
 			lineBegin = getPosition(cmdBegin);
 			lineEnd = getPosition(cmdEnd);
 		}
 
 		int lineNumber = Integer.parseInt(element.getAttribute(ATTR_LINENO));
 
 		final String fileName = element.getAttribute(ATTR_FILENAME);
 		final URI fileUri = URI.create(fileName);
 
 		final String where = element.getAttribute(ATTR_WHERE);
 
 		return new DbgpStackLevel(fileUri, where, level, lineNumber, lineBegin,
 				lineEnd);
 	}
 
 	public static DbgpFeature parseFeature(Element element)
 			throws DbgpProtocolException {
 		final String ATTR_FEATURE_NAME = "feature_name";
 		final String ATTR_SUPPORTED = "supported";
 
 		String name = element.getAttribute(ATTR_FEATURE_NAME);
 		boolean supported = makeBoolean(element.getAttribute(ATTR_SUPPORTED));
 		String value = parseContent(element);
 		return new DbgpFeature(supported, name, value);
 	}
 
 	public static IDbgpProperty parseProperty(Element property) {
 		final String ATTR_NAME = "name";
 		final String ATTR_FULLNAME = "fullname";
 		final String ATTR_TYPE = "type";
 		final String ATTR_CHILDREN = "children";
 		final String ATTR_NUMCHILDREN = "numchildren";
 		final String ATTR_CONSTANT = "constant";
 		final String ATTR_SIZE = "size";
 		final String ATTR_KEY = "key";
 
 		/*
 		 * attributes: name, fullname, type, children, numchildren, constant,
 		 * encoding, size, key
 		 */
 
 		// may exist as an attribute of the property or as child element
 		final String name = getFromChildOrAttr(property, ATTR_NAME);
 		final String fullName = getFromChildOrAttr(property, ATTR_FULLNAME);
 		final String type = property.getAttribute(ATTR_TYPE);
 
 		// hasChildren
 		boolean hasChildren = false;
 		if (property.hasAttribute(ATTR_CHILDREN)) {
 			hasChildren = makeBoolean(property.getAttribute(ATTR_CHILDREN));
 		}
 
 		// Children count
 		int childrenCount = -1;
 		if (property.hasAttribute(ATTR_NUMCHILDREN)) {
 			childrenCount = Integer.parseInt(property
 					.getAttribute(ATTR_NUMCHILDREN));
 		}
 
 		// Size
 		int size = -1;
 		if (property.hasAttribute(ATTR_SIZE)) {
 			size = Integer.parseInt(property.getAttribute(ATTR_SIZE));
 		}
 
 		// Constant
 		boolean constant = false;
 		if (property.hasAttribute(ATTR_CONSTANT)) {
 			constant = makeBoolean(property.getAttribute(ATTR_CONSTANT));
 		}
 
 		// Key
 		String key = null;
 		if (property.hasAttribute(ATTR_KEY)) {
 			key = property.getAttribute(ATTR_KEY);
 		}
 
 		// Value
 		String value = "";
 
 		if (!hasChildren) {
 			NodeList list = property.getElementsByTagName("value");
 			if (list.getLength() == 0) {
 				value = getEncodedValue(property);
 			} else {
 				value = getEncodedValue((Element) list.item(0));
 			}
 		}
 
 		// Children
 		List availableChildren = new ArrayList();
 		if (hasChildren) {
 			NodeList properties = property.getChildNodes();
 			for (int i = 0; i < properties.getLength(); ++i) {
 				Node item = properties.item(i);
 				if (item instanceof Element) {
 					availableChildren.add(parseProperty((Element) item));
 				}
 			}
 		}
 
 		return new DbgpProperty(name, fullName, type, value, size,
 				childrenCount, hasChildren, constant, key,
 				(IDbgpProperty[]) availableChildren
 						.toArray(new IDbgpProperty[availableChildren.size()]));
 	}
 
 	public static IDbgpStatus parseStatus(Element element)
 			throws DbgpProtocolException {
 		final String ATTR_REASON = "reason";
 		final String ATTR_STATUS = "status";
 
 		String status = element.getAttribute(ATTR_STATUS);
 		String reason = element.getAttribute(ATTR_REASON);
 		return DbgpStatus.parse(status, reason);
 	}
 
 	public static IDbgpBreakpoint parseBreakpoint(Element element) {
 		final String LINE_BREAKPOINT = "line";
 		final String CALL_BREAKPOINT = "call";
 		final String RETURN_BREAKPOINT = "return";
 		final String EXCEPTION_BREAKPOINT = "exception";
 		final String CONDITIONAL_BREAKPOINT = "conditional";
 		final String WATCH_BREAKPOINT = "watch";
 
 		final String ATTR_ID = "id";
 		final String ATTR_TYPE = "type";
 		final String ATTR_STATE = "state";
 		final String ATTR_HIT_COUNT = "hit_count";
 		final String ATTR_HIT_VALUE = "hit_value";
 		final String ATTR_HIT_CONDITION = "hit_condition";
 		final String ATTR_FILENAME = "filename";
 		final String ATTR_LINENO = "lineno";
 		final String ATTR_FUNCTION = "function";
 		final String ATTR_EXCEPTION = "exception";
 		final String ATTR_EXPRESSION = "expression";
 
 		// ActiveState Tcl
 
 		// ActiveState Python
 		// <response xmlns="urn:debugger_protocol_v1" command="breakpoint_get"
 		// transaction_id="1">
 		// <breakpoint id="1"
 		// type="line"
 		// filename="c:\distrib\dbgp\test\test1.py"
 		// lineno="8"
 		// state="enabled"
 		// temporary="0">
 		// </breakpoint>
 		// </response>
 
 		String type = element.getAttribute(ATTR_TYPE);
 
 		String id = element.getAttribute(ATTR_ID);
 		boolean enabled = element.getAttribute(ATTR_STATE).equals("enabled");
 		int hitCount = Integer.parseInt(element.getAttribute(ATTR_HIT_COUNT));
 		int hitValue = Integer.parseInt(element.getAttribute(ATTR_HIT_VALUE));
 		String hitCondition = element.getAttribute(ATTR_HIT_CONDITION);
 
 		if (type.equals(LINE_BREAKPOINT)) {
 			String fileName = element.getAttribute(ATTR_FILENAME);
			int lineNumber = Integer
					.parseInt(element.getAttribute(ATTR_LINENO));
 			return new DbgpLineBreakpoint(id, enabled, hitValue, hitCount,
 					hitCondition, fileName, lineNumber);
 		} else if (type.equals(CALL_BREAKPOINT)) {
 			String function = element.getAttribute(ATTR_FUNCTION);
 			return new DbgpCallBreakpoint(id, enabled, hitValue, hitCount,
 					hitCondition, function);
 		} else if (type.equals(RETURN_BREAKPOINT)) {
 			String function = element.getAttribute(ATTR_FUNCTION);
 			return new DbgpReturnBreakpoint(id, enabled, hitValue, hitCount,
 					hitCondition, function);
 		} else if (type.equals(EXCEPTION_BREAKPOINT)) {
 			String exception = element.getAttribute(ATTR_EXCEPTION);
 			return new DbgpExceptionBreakpoint(id, enabled, hitValue, hitCount,
 					hitCondition, exception);
 		} else if (type.equals(CONDITIONAL_BREAKPOINT)) {
 			String expression = element.getAttribute(ATTR_EXPRESSION);
 			return new DbgpConditionalBreakpoint(id, enabled, hitValue,
 					hitCount, hitCondition, expression);
 		} else if (type.equals(WATCH_BREAKPOINT)) {
 			String expression = element.getAttribute(ATTR_EXPRESSION);
 			return new DbgpWatchBreakpoint(id, enabled, hitValue, hitCount,
 					hitCondition, expression);
 		}
 
 		return null;
 	}
 
 	public static IDbgpSessionInfo parseSession(Element element) {
 		final String ATTR_APPID = "appid";
 		final String ATTR_IDEKEY = "idekey";
 		final String ATTR_SESSION = "session";
 		final String ATTR_THREAD = "thread";
 		final String ATTR_PARENT = "parent";
 		final String ATTR_LANGUAGE = "language";
 
 		String appId = element.getAttribute(ATTR_APPID);
 		String ideKey = element.getAttribute(ATTR_IDEKEY);
 		String session = element.getAttribute(ATTR_SESSION);
 		String threadId = element.getAttribute(ATTR_THREAD);
 		String parentId = element.getAttribute(ATTR_PARENT);
 		String language = element.getAttribute(ATTR_LANGUAGE);
 
 		return new DbgpSessionInfo(appId, ideKey, session, threadId, parentId,
 				language, null);
 	}
 
 	protected static String getFromChildOrAttr(Element property, String name) {
 		NodeList list = property.getElementsByTagName(name);
 
 		if (list.getLength() == 0) {
 			return property.getAttribute(name);
 		}
 
 		/*
 		 * this may or may not need to be base64 decoded - need to see output
 		 * from an ActiveState's python debugging session to determine. gotta
 		 * love protocol changes that have made their way back into the
 		 * published spec
 		 */
 		return getEncodedValue((Element) list.item(0));
 	}
 
 	protected static String getEncodedValue(Element element) {
 		final String ATTR_ENCODING = "encoding";
 
 		String encoding = ENCODING_NONE;
 		if (element.hasAttribute(ATTR_ENCODING)) {
 			encoding = element.getAttribute(ATTR_ENCODING);
 		}
 
 		if (ENCODING_NONE.equals(encoding)) {
 			return parseContent(element);
 		}
 
 		if (ENCODING_BASE64.equals(encoding)) {
 			return parseBase64Content(element);
 		}
 
 		throw new AssertionError("invalid encoding [" + encoding + "]");
 	}
 }
