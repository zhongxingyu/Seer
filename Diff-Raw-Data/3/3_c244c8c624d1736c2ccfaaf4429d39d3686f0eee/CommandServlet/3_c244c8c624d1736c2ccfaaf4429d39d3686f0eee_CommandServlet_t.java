 package com.kremerk.commandprocessor;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.kremerk.commandprocessor.exception.CommandProcessorException;
 import com.kremerk.commandprocessor.response.BinaryResponse;
 import com.kremerk.commandprocessor.response.JsonResponse;
 import com.kremerk.commandprocessor.response.Response;
 import com.kremerk.commandprocessor.response.ResponseType;
 
 public class CommandServlet extends HttpServlet {
 	
 	public CommandServlet(CommandProcessor processor) {
 		this.cmdProcessor = processor;
 	}
 
 	/**
 	 * The CRUDServlet expects to take a command and a series of parameters.
 	 * 
 	 * <p>
 	 * The url format is as follows:
 	 * 
 	 * <P>
 	 * http://server:port/cmd=MyCoolCommand&param=1&param=2&param=blah
 	 * http://server:port/CommandServletMapping/CommandSetName/CommandName/?param=1&param=2&param=blah
 	 * 
 	 * <p>
 	 * The implementation of the MyCoolCommand class will need to appropriately
 	 * handle the params passed in in the given order.
 	 */
 	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String[] commandParts = parseCommandParts(request);
 		String commandSetName = commandParts[0];
 		String commandName = commandParts[1];
 		ResponseType rspType = ResponseType.getResponseTypeFromString(request.getParameter("type"));
 		String[] parameters = request.getParameterValues("param");
 
 		Response rsp = null;
 
 		try {
 			if (mockMode) {
 				cmdProcessor = new MockCommandProcessor(mockCommandRoot);
 			} 
 			if(rspType == ResponseType.JSON) {
 				rsp = new JsonResponse(cmdProcessor.processCommand(commandSetName, commandName, parameters));
 				
 				response.setContentType(rsp.getContentType());
 				response.getWriter().write((String) rsp.getResponse());
				response.setStatus(HttpServletResponse.SC_OK);
 			}
 			else if(rspType == ResponseType.BINARY) {
 				rsp = new BinaryResponse(cmdProcessor.processBinaryCommand(commandSetName, commandName, parameters));
 				response.setContentType(rsp.getContentType());
				response.setStatus(HttpServletResponse.SC_OK);
 				ServletOutputStream out = response.getOutputStream();
 				out.write((byte[]) rsp.getResponse());
 				out.flush();
 				out.close();
 			}
 			else if(rspType == ResponseType.UNSUPPORTED){
 			    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, String.format("Error executing command with type %s. Type %s is not supported.",rspType.getType(), rspType.getType()));
 			}
 			else {
 			    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Type must be supplied when calling a command");
 			}
 		} catch (CommandProcessorException e) {
 			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error executing command " + commandName);
 			return;
 		}
 
 
 	}
 
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		doGet(request, response);
 	}
 	
 	public void setMockMode(boolean mockMode) {
 		this.mockMode = mockMode;
 	}
 	
 	public boolean isMockMode() {
 		return mockMode;
 	}
 	
 	public void setMockRoot(String rootDirectory, String commandRoot) {
 		mockCommandRoot = rootDirectory + File.separator + commandRoot;
 	}
 	
 	public void setMockRoot(String commandRoot) {
 		setMockRoot(System.getProperty("user.dir"), commandRoot);
 	}
 	
 	public String[] parseCommandParts(HttpServletRequest request) {
 		String path = request.getPathInfo();
 		path = path.replaceFirst("/", "");
 		return path.split("/");
 	}
 	
 	private boolean mockMode = false;
 	private String mockCommandRoot;
 	private CommandProcessor cmdProcessor;
 	private static final long serialVersionUID = 8946402369349157361L;
 }
