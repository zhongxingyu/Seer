 /*
  * Copyright 2008 Fedora Commons, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.mulgara.webquery;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import org.mulgara.connection.Connection;
 import org.mulgara.connection.SessionConnection;
 import org.mulgara.itql.TqlInterpreter;
 import org.mulgara.parser.Interpreter;
 import org.mulgara.parser.MulgaraLexerException;
 import org.mulgara.parser.MulgaraParserException;
 import org.mulgara.query.Answer;
 import org.mulgara.query.Query;
 import org.mulgara.query.QueryException;
 import org.mulgara.query.TuplesException;
 import org.mulgara.query.operation.Command;
 import org.mulgara.server.AbstractServer;
 import org.mulgara.server.SessionFactory;
 import org.mulgara.sparql.SparqlInterpreter;
 import org.mulgara.util.SparqlUtil;
 import org.mulgara.util.StackTrace;
 import org.mulgara.util.functional.C;
 import org.mulgara.util.functional.Fn;
 import org.mulgara.util.functional.Fn1E;
 import org.mulgara.util.functional.Pair;
 
 import static org.mulgara.webquery.Template.*;
 
 import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
 import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
 
 /**
  * A web UI for the server.
  *
  * @created Jul 28, 2008
  * @author Paul Gearon
  * @copyright &copy; 2008 <a href="http://www.fedora-commons.org/">Fedora Commons</a>
  */
 public class QueryServlet extends HttpServlet {
 
   /** Serialization by default */
   private static final long serialVersionUID = -8407263937557243990L;
 
   /** The default name to use for the host. */
   private static final String DEFAULT_HOSTNAME = "localhost";
 
   /** Session value for database connection. */
   private static final String CONNECTION = "session.connection";
 
   /** Session value for the TQL interpreter. */
   private static final String TQL_INTERPRETER = "session.tql.interpreter";
 
   /** Session value for the SPARQL interpreter. */
   private static final String SPARQL_INTERPRETER = "session.sparql.interpreter";
 
   /** The name of the host for the application. */
   private final String hostname;
 
   /** The name of the server for the application. */
   private final String servername;
 
   /** The server for finding a session factory. */
   private final AbstractServer server;
 
   /** The default graph URI to use. */
   private String defaultGraphUri;
 
   /** Session factory for accessing the database. */
   private SessionFactory cachedSessionFactory;
 
   /** The path down to the template resource. */
   private String templatePath;
 
   /** The path prefix for resources. */
   private String resourcePath;
 
   /** Debugging text. */
   private String debugText = "";
 
   /**
    * Creates the servlet for the named host.
    * @param hostname The host name to use, or <code>null</code> if this is not known.
    * @param servername The name of the current server.
    */
   public QueryServlet(String hostname, String servername, AbstractServer server) throws IOException {
     this.hostname = (hostname != null) ? hostname : DEFAULT_HOSTNAME;
     this.servername = servername;
     this.cachedSessionFactory = null;
     this.server = server;
     URL path = getClass().getClassLoader().getResource(ResourceFile.RESOURCES + getTemplateFile());
     if (path == null) throw new IOException("Resource not found: " + ResourceFile.RESOURCES + getTemplateFile());
     templatePath = path.toString();
     resourcePath = templatePath.split("!")[0];
     defaultGraphUri = "rmi://" + hostname + "/" + servername + "#sampledata";
   }
 
 
   /**
    * Respond to a request for the servlet.
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     String path = req.getPathInfo();
     debugText = path;
     // case analysis for request type
     String ext = getExtension(path);
     if (ext.equals(".jpg") || ext.equals(".png") || ext.equals(".jpeg")) {
       resp.setContentType("image/jpeg");
       new ResourceBinaryFile(path).sendTo((OutputStream)resp.getOutputStream());
     } else if (ext.equals(".css")) {
       resp.setContentType("text/css");
       new ResourceBinaryFile(path).sendTo(resp.getOutputStream());
     } else {
       // file request
       resp.setContentType("text/html");
       resp.setHeader("pragma", "no-cache");
 
       // check for some parameters
       String resultOrdinal = req.getParameter(RESULT_ORD_ARG);
       String queryGetGraph = req.getParameter(GRAPH_ARG);
 
       // return the appropriate page for the given parameters
       if (resultOrdinal != null) {
         doNextPage(req, resp, resultOrdinal);
       } else if (queryGetGraph != null) {
         doQuery(req, resp, queryGetGraph);
       } else {
         clearOldResults(req);
         outputStandardTemplate(resp);
       }
     }
   }
 
 
   /**
    * Respond to a request for the servlet.
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
     if (!req.getPathInfo().startsWith("/" + EXECUTE_LINK)) {
       resp.sendError(SC_BAD_REQUEST, "Sent a command to the wrong page.");
       return;
     }
     
     doQuery(req, resp, req.getParameter(GRAPH_ARG));
   }
 
 
   /**
    * Provide a description for the servlet.
    * @see javax.servlet.GenericServlet#getServletInfo()
    */
   public String getServletInfo() {
     return "Mulgara Query UI";
   }
 
 
   /**
    * Generates the standard page from the template HTML file.
    * @param resp The object used to respond to the client.
    * @throws IOException Caused by an error writing to the client.
    */
   private void outputStandardTemplate(HttpServletResponse resp) throws IOException {
     PrintWriter out = resp.getWriter();
     new ResourceTextFile(getTemplateFile(), getTemplateTags()).sendTo(out);
     out.close();
   }
 
 
   /**
    * Execute the appropriate query, and display the results.
    * @param req The user request.
    * @param resp The response object for output to the user.
    * @param graphUri The URI in a user request.
    * @throws IOException Error sending a response to the client.
    */
   private void doQuery(HttpServletRequest req, HttpServletResponse resp, String graphUri) throws IOException {
     clearOldResults(req);
 
     // work out which commands to run
     String command = generateCommand(req, graphUri);
 
     // No command to run, so show the entry page
     if (command == null || command.length() == 0) {
       outputStandardTemplate(resp);
       return;
     }
 
     // execute all the commands, and accumulate the results
     List<Object> results = null;
     List<Command> cmds = null;
     long time = 0;
     try {
       // record how long this takes
       time = System.currentTimeMillis();
       final Connection c = getConnection(req);
       cmds = getInterpreter(req, command, graphUri).parseCommands(command);
       results = C.map(cmds, new Fn1E<Command,Object,Exception>() { public Object fn(Command cmd) throws Exception { return cmd.execute(c); } });
       time = System.currentTimeMillis() - time;
     } catch (MulgaraParserException mpe) {
       resp.sendError(SC_BAD_REQUEST, "Error parsing command: " + mpe.getMessage());
       return;
    } catch (RequestException re) {
      resp.sendError(SC_BAD_REQUEST, "Error processing request: " + re.getMessage());
      return;
     } catch (IllegalStateException ise) {
       resp.sendError(SC_SERVICE_UNAVAILABLE, ise.getMessage());
       return;
     } catch (Exception e) {
      resp.sendError(SC_BAD_REQUEST, "Error executing command. Reason: " + StackTrace.getReasonMessage(e));
       return;
     }
 
     // Use the first graph mentioned as the future default
     for (Command c: cmds) {
       if (c instanceof Query) {
         updateDefaultGraph(c);
         break;
       }
     }
 
     // Get the tags to use in the page template
     Map<String,String> templateTags = getTemplateTagMap();
     templateTags.put(GRAPH_TAG, defaultGraph(graphUri));
     // Generate the page
     QueryResponsePage page = new QueryResponsePage(req, resp, templateTags, getTemplateHeaderFile(), getTemplateTailFile());
     page.writeResults(time, cmds, results);
   }
 
 
   /**
    * Print out the next page of a set of results, specified by the ordinal number
    * @param req The request environment.
    * @param resp The response object.
    * @param ordinalStr The result number to display the next page for.
    * @throws IOException Error responding to the client.
    */
   private void doNextPage(HttpServletRequest req, HttpServletResponse resp, String ordinalStr) throws IOException {
     // get the session/request parameters, and validate
     Map<Answer,Pair<Long,Command>> unfinishedResults = getUnfinishedResults(req);
     int ordinal = 0;
     try {
       ordinal = Integer.parseInt(ordinalStr);
     } catch (NumberFormatException nfe) {
       ordinal = -1;
     }
 
     if (ordinal <= 0 || unfinishedResults == null || ordinal > unfinishedResults.size()) {
       resp.sendError(SC_BAD_REQUEST, "Result not available. Did you use the \"Back button\"? (result " + ordinalStr +
                  " of " + ((unfinishedResults == null) ? 0 : unfinishedResults.size()) + ")");
       clearOldResults(req);
       return;
     }
 
     // Close and remove all the results we don't need
     Answer remaining = closeExcept(unfinishedResults.keySet(), ordinal);
     
     // Get the tags to use in the page template
     Map<String,String> templateTags = getTemplateTagMap();
     templateTags.put(GRAPH_TAG, defaultGraph(req.getParameter(GRAPH_ARG)));
     
     // Generate the page
     QueryResponsePage page = new QueryResponsePage(req, resp, templateTags, getTemplateHeaderFile(), getTemplateTailFile());
     page.writeResult(unfinishedResults.get(remaining).second(), remaining);
   }
 
 
   /**
    * Close all but one answer.
    * @param answers The answers to close.
    * @param ordinal The ordinal (1-based) of the answer to NOT close.
    * @return The Answer that did NOT get removed.
    */
   private Answer closeExcept(Set<Answer> answers, int ordinal) {
     Answer excludedResult = null;
 
     Iterator<Answer> i = answers.iterator();
     int nrResults = answers.size();
     for (int r = 0; r < nrResults; r++) {
       assert i.hasNext();
       if (r == ordinal - 1) {
         excludedResult = i.next();
       } else {
         try {
           i.next().close();
         } catch (TuplesException e) { /* ignore */ }
         i.remove();
       }
     }
     assert !i.hasNext();
     assert excludedResult != null;
 
     return excludedResult;
   }
 
 
   /**
    * Clears out any old results found in the session.
    * @param req The current environment.
    */
   private void clearOldResults(HttpServletRequest req) {
     Map<Answer,Pair<Long,Command>> results = getUnfinishedResults(req);
     try {
       if (results != null) {
         for (Answer a: results.keySet()) a.close();
         results.clear();
       }
     } catch (TuplesException e) {
       // ignoring these problems, since the answer is being thrown away
     }
   }
 
 
   /**
    * Finds any unfinished data in the current session.
    * @param req The current request environment.
    * @return The unfinished results that were recorded in this session, or <code>null</code>
    *         if there were no unfinished results.
    */
   @SuppressWarnings("unchecked")
   private Map<Answer,Pair<Long,Command>> getUnfinishedResults(HttpServletRequest req) {
     Map<Answer,Pair<Long,Command>> oldResultData = (Map<Answer,Pair<Long,Command>>)req.getSession().getAttribute(UNFINISHED_RESULTS);
     return (oldResultData == null) ? null : oldResultData;
   }
 
 
   /**
    * Analyse the request parameters and work out what kind of query to generate.
    * Resource and Literal queries are mutually exclusive, and both override
    * an explicit query argument.
    * @param req The request environment.
    * @param graphUri The graphUri set for this request.
    * @return The command or commands to execute.
    */
   private String generateCommand(HttpServletRequest req, String graphUri) {
     String queryResource = req.getParameter(QUERY_RESOURCE_ARG);
     if (queryResource != null) return buildResourceQuery(graphUri, queryResource);
 
     String queryLiteral = req.getParameter(QUERY_LITERAL_ARG);
     if (queryLiteral != null) return buildLiteralQuery(graphUri, queryLiteral);
 
     return req.getParameter(QUERY_TEXT_ARG);
   }
 
 
   /**
    * Get a query string to display a resource.
    * @param graph The name of the graph to get the resource from.
    * @param resource The name of the resource.
    * @return The query string.
    */
   private String buildResourceQuery(String graph, String resource) {
     StringBuilder urlString = new StringBuilder("select $Predicate $Object from <");
     urlString.append(graph).append("> where <").append(resource).append("> $Predicate $Object;");
     urlString.append("select $Subject $Predicate from <").append(graph);
     urlString.append("> where $Subject $Predicate <").append(resource).append(">;");
     urlString.append("select $Subject $Object from <").append(graph);
     urlString.append("> where $Subject <").append(resource).append("> $Object;");
     return urlString.toString();
   }
 
 
   /**
    * Get a query string to display a literal.
    * @param graph The name of the graph to get the resource from.
    * @param literal The value of the literal.
    * @return The query string.
    */
   private String buildLiteralQuery(String graph, String literal) {
     return "select $Subject $Predicate  from <" + graph + "> where $Subject $Predicate " + literal + ";";
   }
 
 
   /**
    * Takes each of the template tags and creates a map out of them.
    * @return A map of all tags to the data to replace them.
    */
   private Map<String,String> getTemplateTagMap() {
     Map<String,String> tagMap = new HashMap<String,String>();
     String[][] source = getTemplateTags();
     for (String[] tag: source) tagMap.put(tag[0], tag[1]);
     return tagMap;
   }
 
 
   /**
    * Gets the list of tags to be replaced in a template document, along with the values
    * to replace them with.
    * @return An array of string pairs. The first string in the pair is the tag to replace,
    *         the second string is the value to repace the tag with.
    */
   private String[][] getTemplateTags() {
     return new String[][] {
         new String[] {HOSTNAME_TAG, hostname},
         new String[] {SERVERNAME_TAG, servername},
         new String[] {JARURL_TAG, resourcePath},
         new String[] {EXECUTE_TAG, EXECUTE_LINK},
         new String[] {DEBUG_TAG, debugText},
     };
   }
 
 
   /**
    * Updates the default graph URI to one found in a Query.
    * @param cmd A Command that contains a query.
    */
   private void updateDefaultGraph(Command cmd) {
     assert cmd instanceof Query;
     Set<URI> graphs = ((Query)cmd).getModelExpression().getGraphURIs();
     if (!graphs.isEmpty()) defaultGraphUri = C.first(graphs).toString();
   }
 
   /**
    * Creates the default graph name for the sample data.
    * @param The graph name that the user has already set.
    * @return The default graph name to use when no graph has been set.
    */
   private String defaultGraph(String graphParam) {
     if (graphParam != null && graphParam.length() > 0) return graphParam;
     return defaultGraphUri;
   }
 
 
   /**
    * Gets the interpreter for the current session, creating it if it doesn't exist yet.
    * @param req The current request environment.
    * @param cmd The command the interpreter will be used on.
    * @param graphUri The string form of the URI for the default graph to use in the interpreter.
    * @return A connection that is tied to this HTTP session.
    * @throws RequestException An internal error occured with the default graph URI.
    */
   private Interpreter getInterpreter(HttpServletRequest req, String cmd, String graphUri) throws RequestException {
     RegInterpreter ri = getRegInterpreter(cmd);
     HttpSession httpSession = req.getSession();
     Interpreter interpreter = (Interpreter)httpSession.getAttribute(ri.getRegString());
     if (interpreter == null) {
       interpreter = ri.getInterpreterFactory().fn();
       httpSession.setAttribute(ri.getRegString(), interpreter);
     }
     setDefaultGraph(interpreter, graphUri);
     return interpreter;
   }
 
 
   /**
    * Sets the default graph on an interpreter
    * @param i The interpreter to set the default graph for.
    * @param graph The graph to use with the interpreter.
    * @throws RequestException An internal error where a valid graph could not be refered to.
    */
   private void setDefaultGraph(Interpreter i, String graph) throws RequestException {
     // set the default graph, if applicable
     try {
       if (graph != null && !"".equals(graph)) i.setDefaultGraphUri(graph);
     } catch (Exception e) {
       try {
         i.setDefaultGraphUri(defaultGraphUri);
       } catch (URISyntaxException e1) {
         throw new RequestException("Unable to create URI for: " + defaultGraphUri, e1);
       }
     }
   }
 
   /**
    * Gets a factory for creating an interpreter, along with the name for that type of interpreter
    * to be registered under
    * @param query The query to determine the interpreter type
    * @return An interpreter constructor and name
    */
   private RegInterpreter getRegInterpreter(String query) {
     Fn<Interpreter> factory = null;
     String attr = null;
     if (SparqlUtil.looksLikeSparql(query)) {
       factory = new Fn<Interpreter>(){ public Interpreter fn(){ return new SparqlInterpreter(); }};
       attr = SPARQL_INTERPRETER;
     } else {
       factory = new Fn<Interpreter>(){ public Interpreter fn() { return new TerminatingTqlInterpreter(); }};
       attr = TQL_INTERPRETER;
     }
     return new RegInterpreter(factory, attr);
   }
 
 
   /**
    * Gets the connection for the current session, creating it if it doesn't exist yet.
    * @param req The current request environment.
    * @return A connection that is tied to this HTTP session.
    * @throws IOException When an error occurs creating a new session.
    */
   private Connection getConnection(HttpServletRequest req) throws IOException, IllegalStateException {
     HttpSession httpSession = req.getSession();
     Connection connection = (Connection)httpSession.getAttribute(CONNECTION);
     if (connection == null) {
       try {
         connection = new SessionConnection(getSessionFactory().newSession(), null, null);
       } catch (QueryException qe) {
         throw new IOException("Unable to create a connection to the database. " + qe.getMessage());
       }
       httpSession.setAttribute(CONNECTION, connection);
     }
     return connection;
   }
 
 
   /**
    * This method allows us to put off getting a session factory until the server is
    * ready to provide one.
    * @return A new session factory.
    */
   private SessionFactory getSessionFactory() throws IllegalStateException {
     if (cachedSessionFactory == null) {
       cachedSessionFactory = server.getSessionFactory();
       if (cachedSessionFactory == null) throw new IllegalStateException("Server not yet ready. Try again soon.");
     }
     return cachedSessionFactory;
   }
 
 
   /**
    * Get the name of the file to be used for the template.
    * @return The absolute file path, with a root set at the resource directory.
    */
   protected String getTemplateFile() {
     return TEMPLATE;
   }
 
 
   /**
    * Get the name of the file to be used for the header template.
    * @return The absolute file path, with a root set at the resource directory.
    */
   protected String getTemplateHeaderFile() {
     return TEMPLATE_HEAD;
   }
 
 
   /**
    * Get the name of the file to be used for the footer template.
    * @return The absolute file path, with a root set at the resource directory.
    */
   protected String getTemplateTailFile() {
     return TEMPLATE_TAIL;
   }
 
 
   /**
    * Returns the filename extension for a given path.
    * @param path The path to get the extension for.
    * @return The extension, including the . character. If there is no extension, then an empty string.
    */
   private static String getExtension(String path) {
     int dot = path.lastIndexOf('.');
     if (dot < 0) return "";
     return path.substring(dot);
   }
 
 
   /**
    * Registerable Interpreter. This contains a factory for an interpreter, plus the name it should
    * be registered under.
    */
   private static class RegInterpreter {
     /** The interpreter factory */
     private final Fn<Interpreter> intFactory;
 
     /** The registration name for the interpreter built from the factory */
     private final String regString;
 
     /** Create a link between an interpreter factory and the name it should be registered under */
     public RegInterpreter(Fn<Interpreter> intFactory, String regString) {
       this.intFactory = intFactory;
       this.regString = regString;
     }
 
     /** Get the method for creating an interpreter */
     public Fn<Interpreter> getInterpreterFactory() {
       return intFactory;
     }
 
     /** Get the name constructed interpreters should be created under */
     public String getRegString() {
       return regString;
     }
   }
 
 
   /**
    * Extension of TQL interpreter that will automatically terminate any
    * commands that are not already terminated.
    */
   private static class TerminatingTqlInterpreter extends TqlInterpreter {
 
     /** The terminating character. */
     private static final String TERMINATOR = ";";
 
     /**
      * Calls TqlInterpreter#parseCommands(String) with a guaranteed termination of ";".
      * @see TqlInterpreter#parseCommands(String)
      */
     public List<Command> parseCommands(String command) throws MulgaraParserException, MulgaraLexerException, IOException {
       command = command.trim();
       if (!command.endsWith(TERMINATOR)) command += TERMINATOR;
       return super.parseCommands(command);
     }
   }
 
 }
