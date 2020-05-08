 package org.eclipse.dltk.dbgp.internal.commands;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.dltk.dbgp.breakpoints.DbgpBreakpointConfig;
 import org.eclipse.dltk.dbgp.breakpoints.IDbgpBreakpoint;
 import org.eclipse.dltk.dbgp.commands.IDbgpBreakpointCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.dbgp.internal.DbgpRequest;
 import org.eclipse.dltk.dbgp.internal.utils.DbgpXmlEntityParser;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class DbgpBreakpointCommands extends DbgpBaseCommands implements
 		IDbgpBreakpointCommands {
 
 	private static final String BREAKPOINT_SET_COMMAND = "breakpoint_set";
 
 	private static final String BREAKPOINT_GET_COMMAND = "breakpoint_get";
 
 	private static final String BREAKPOINT_UPDATE_COMMAND = "breakpoint_update";
 
 	private static final String BREAKPOINT_REMOVE_COMMAND = "breakpoint_remove";
 
 	private static final String BREAKPOINT_LIST_COMMAND = "breakpoint_list";
 
 	private static final String BREAKPOINT_TAG = "breakpoint";
 
 	final String LINE_BREAKPOINT = "line";
 
 	final String CALL_BREAKPOINT = "call";
 
 	final String RETURN_BREAKPOINT = "return";
 
 	final String EXCEPTION_BREAKPOINT = "exception";
 
 	final String CONDITIONAL_BREAKPOINT = "conditional";
 
 	final String WATCH_BREAKPOINT = "watch";
 
 	protected List parseBreakpointsResponse(Element response) {
 		List list = new ArrayList();
 
 		NodeList breakpoints = response.getElementsByTagName(BREAKPOINT_TAG);
 		for (int i = 0; i < breakpoints.getLength(); ++i) {
 			list.add(DbgpXmlEntityParser.parseBreakpoint((Element) breakpoints
 					.item(i)));
 		}
 
 		return list;
 	}
 
 	protected String parseSetBreakpointResponse(Element response)
 			throws DbgpException {
 		return response.getAttribute("id");
 	}
 
 	protected String setBreakpoint(String type, URI uri, Integer lineNumber,
 			String function, String exception, String expression,
 			DbgpBreakpointConfig info) throws DbgpException {
 
 		DbgpRequest request = createRequest(BREAKPOINT_SET_COMMAND);
 		request.addOption("-t", type);
 
 		if (uri != null) {
 			request.addOption("-f", uri.toASCIIString());
 		}
 
 		if (lineNumber != null) {
 			request.addOption("-n", lineNumber.toString());
 		}
 
 		if (function != null) {
 			request.addOption("-m", function);
 		}
 
 		if (exception != null) {
 			request.addOption("-x", exception);
 		}
 
 		// TODO: expression handling
 
 		if (info != null) {
 			request.addOption("-s", info.getStateString());
 			request.addOption("-r", info.getTemporaryString());
 			if (info.getHitValue() != -1 && info.getHitCondition() != -1) {
 				request.addOption("-h", info.getHitValue());
 				request.addOption("-o", info.getHitConditionString());
 			}
 		}
 
 		if (expression != null) {
 			request.setData(expression);
 		}
 
 		return parseSetBreakpointResponse(communicate(request));
 	}
 
 	public DbgpBreakpointCommands(IDbgpCommunicator communicator) {
 		super(communicator);
 	}
 
 	public String setLineBreakpoint(URI uri, int lineNumber,
 			DbgpBreakpointConfig info) throws DbgpException {
 		return setBreakpoint(LINE_BREAKPOINT, uri, new Integer(lineNumber),
 				null, null, null, info);
 	}
 
 	public String setCallBreakpoint(String function, DbgpBreakpointConfig info)
 			throws DbgpException {
 		return setBreakpoint(CALL_BREAKPOINT, null, null, function, null, null,
 				info);
 	}
 
 	public String setReturnBreakpoint(String function, DbgpBreakpointConfig info)
 			throws DbgpException {
 		return setBreakpoint(RETURN_BREAKPOINT, null, null, function, null,
 				null, info);
 	}
 
 	public String setExceptionBreakpoint(String exception,
 			DbgpBreakpointConfig info) throws DbgpException {
 		return setBreakpoint(EXCEPTION_BREAKPOINT, null, null, null, exception,
 				null, info);
 	}
 
 	public String setConditionalBreakpoint(URI uri, int lineNumber,
 			String expression, DbgpBreakpointConfig info) throws DbgpException {
 		return setBreakpoint(CONDITIONAL_BREAKPOINT, uri, new Integer(
 				lineNumber), null, null, expression, info);
 	}
 
 	public String setWatchBreakpoint(String expression,
 			DbgpBreakpointConfig info) throws DbgpException {
 		return setBreakpoint(WATCH_BREAKPOINT, null, null, null, null,
 				expression, info);
 	}
 
 	public IDbgpBreakpoint getBreakpoint(String id) throws DbgpException {
 		DbgpRequest request = createRequest(BREAKPOINT_GET_COMMAND);
 		request.addOption("-d", id);
 		// List list = parseBreakpointsResponse(communicate(request));
 		// return (IDbgpBreakpoint) list.get(0);
 		// return null;
 		throw new Error("Not implemeneted");
 	}
 
 	public void removeBreakpoint(String id) throws DbgpException {
 		DbgpRequest request = createRequest(BREAKPOINT_REMOVE_COMMAND);
 		request.addOption("-d", id);
 		communicate(request);
 	}
 
 	public void updateBreakpoint(String id, DbgpBreakpointConfig config)
 			throws DbgpException {
 		DbgpRequest request = createRequest(BREAKPOINT_UPDATE_COMMAND);
 		request.addOption("-d", id);
 		request.addOption("-s", config.getStateString());
 
 		if (config.getHitValue() != -1) {
 			request.addOption("-h", config.getHitValue());
 		}
 
 		if (config.getHitCondition() != -1) {
 			request.addOption("-o", config.getHitConditionString());
 		}
 
 		communicate(request);
 	}
 
 	public List getBreakpoints() throws DbgpException {
 		return parseBreakpointsResponse(communicate(createRequest(BREAKPOINT_LIST_COMMAND)));
 	}
 }
