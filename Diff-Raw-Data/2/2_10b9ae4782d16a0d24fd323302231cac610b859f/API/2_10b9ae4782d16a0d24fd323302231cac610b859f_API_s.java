 /*
  * $Id$
  */
 package org.xins.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import javax.servlet.ServletRequest;
 import org.xins.util.io.FastStringWriter;
 import org.znerd.xmlenc.XMLOutputter;
 
 /**
  * Base class for API implementation classes.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  */
 public abstract class API
 extends Object
 implements DefaultReturnCodes {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified value is <code>null</code> or an empty string.
     * Only if it is then <code>true</code> is returned.
     *
     * @param value
     *    the value to check.
     *
     * @return
     *    <code>true</code> if and only if <code>value != null &amp;&amp;
     *    value.length() != 0</code>.
     */
    protected static final boolean isMissing(String value) {
       return value == null || value.length() == 0;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>API</code> object.
     */
    protected API() {
       _functionsByName = new HashMap();
       _functionList    = new ArrayList();
       _contextsByThread = new HashMap();
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * Map that maps function names to <code>Function</code> instances.
     * Contains all functions associated with this API.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _functionsByName;
 
    /**
     * List of all functions. This field cannot be <code>null</code>.
     */
    private final List _functionList;
 
    /**
     * Map that maps threads to <code>CallContext</code> instances.
     *
     * <p />This field is initialized to a non-<code>null</code> value by the
     * constructor.
     */
    private final Map _contextsByThread;
 
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Initializes this API.
     *
     * <p />The implementation of this method in class {@link API} is empty.
     *
     * @param properties
     *    the properties, can be <code>null</code>.
     *
     * @throws Throwable
     *    if the initialisation fails.
     */
    public void init(Properties properties)
    throws Throwable {
       // empty
    }
 
    /**
     * Callback method invoked when a function is constructed.
     *
     * @param function
     *    the function that is added, not <code>null</code>.
     *
     * @throws NullPointerException
     *    if <code>function == null</code>.
     */
    final void functionAdded(Function function) {
       _functionsByName.put(function.getName(), function);
       _functionList.add(function);
    }
 
    /**
     * Returns the function with the specified name.
     *
     * @param name
     *    the name of the function, will not be checked if it is
     *    <code>null</code>.
     *
     * @return
     *    the function with the specified name, or <code>null</code> if there
     *    is no match.
     */
    final Function getFunction(String name) {
       return (Function) _functionsByName.get(name);
    }
 
    /**
     * Forwards a call to the <code>handleCall(CallContext)</code> method.
     *
     * @param request
     *    the original servlet request, not <code>null</code>.
     *
     * @param out
     *    the output stream to write to, not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    final void handleCall(ServletRequest request, PrintWriter out)
    throws IOException {
 
       // Get call context
       Thread thread = Thread.currentThread();
       CallContext context = (CallContext) _contextsByThread.get(thread);
       if (context == null) {
          context = new CallContext();
          _contextsByThread.put(thread, context);
       }
 
       // Configure the call context
       context.reset(request);
 
       FastStringWriter stringWriter = context.getStringWriter();
 
       // Determine the function name
       String functionName = context.getFunction();
       if (functionName == null || functionName.length() == 0) {
          context.startResponse(false, MISSING_FUNCTION_NAME);
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Detect special functions
       if (functionName.charAt(0) == '_') {
          if ("_GetFunctionList".equals(functionName)) {
             doGetFunctionList(context);
          } else if ("_GetStatistics".equals(functionName)) {
             doGetStatistics(context);
          } else if ("_GetVersion".equals(functionName)) {
             doGetVersion(context);
          } else {
             context.startResponse(false, NO_SUCH_FUNCTION);
          }
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Get the function object
       Function f = getFunction(functionName);
 
       // Detect case where function is not recognized
       if (f == null) {
          context.startResponse(false, NO_SUCH_FUNCTION);
          context.endResponse();
          out.print(stringWriter.toString());
          return;
       }
 
       // Forward the call
       boolean exceptionThrown = true;
       boolean success;
       String code;
       try {
          handleCall(context);
          success = context.getSuccess();
          code    = context.getCode();
          exceptionThrown = false;
       } catch (Throwable exception) {
          success = false;
          code    = INTERNAL_ERROR;
 
          XMLOutputter xmlOutputter = context.getXMLOutputter();
          xmlOutputter.reset(out, "UTF-8");
          xmlOutputter.startTag("result");
          xmlOutputter.attribute("success", "false");
          xmlOutputter.attribute("code", code);
          xmlOutputter.startTag("param");
          xmlOutputter.attribute("name", "_exception.class");
          xmlOutputter.pcdata(exception.getClass().getName());
 
          String message = exception.getMessage();
          if (message != null && message.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.message");
             xmlOutputter.pcdata(message);
          }
 
          FastStringWriter stWriter = new FastStringWriter();
          PrintWriter printWriter = new PrintWriter(stWriter);
          exception.printStackTrace(printWriter);
          String stackTrace = stWriter.toString();
          if (stackTrace != null && stackTrace.length() > 0) {
             xmlOutputter.endTag();
             xmlOutputter.startTag("param");
             xmlOutputter.attribute("name", "_exception.stacktrace");
             xmlOutputter.pcdata(stackTrace);
          }
          xmlOutputter.close();
       }
 
       if (!exceptionThrown) {
          out.print(stringWriter.toString());
       }
       long start    = context.getStart();
       long duration = System.currentTimeMillis() - start;
       f.performedCall(start, duration, success, code);
    }
 
    /**
     * Handles a call to this API.
     *
     * @param context
     *    the context for this call, never <code>null</code>.
     *
     * @throws Throwable
     *    if anything goes wrong.
     */
    protected abstract void handleCall(CallContext context)
    throws Throwable;
 
    /**
     * Returns a list of all functions in this API. Per function the name and
     * the version are returned.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetFunctionList(CallContext context)
    throws IOException {
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          context.startTag("function");
          context.attribute("name",    function.getName());
          context.attribute("version", function.getVersion());
          context.endTag();
       }
    }
 
    /**
     * Returns the call statistics for all functions in this API.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetStatistics(CallContext context)
    throws IOException {
       int count = _functionList.size();
       for (int i = 0; i < count; i++) {
          Function function = (Function) _functionList.get(i);
          Function.Statistics stats = function.getStatistics();
 
          long successfulCalls       = stats.getSuccessfulCalls();
          long unsuccessfulCalls     = stats.getUnsuccessfulCalls();
          long successfulDuration    = stats.getSuccessfulDuration();
          long unsuccessfulDuration  = stats.getUnsuccessfulDuration();
 
          String successfulAverage;
          String successfulMin;
          String successfulMax;
          String lastSuccessfulStart;
          String lastSuccessfulDuration;
          if (successfulCalls == 0) {
             successfulAverage = "NA";
             successfulMin     = "NA";
             successfulMax     = "NA";
             lastSuccessfulStart    = "NA";
             lastSuccessfulDuration = "NA";
          } else if (successfulDuration == 0) {
             successfulAverage = "0";
             successfulMin     = String.valueOf(stats.getSuccessfulMin());
             successfulMax     = String.valueOf(stats.getSuccessfulMax());
             lastSuccessfulStart    = String.valueOf(stats.getLastSuccessfulStart());
             lastSuccessfulDuration = String.valueOf(stats.getLastSuccessfulDuration());
          } else {
             successfulAverage = String.valueOf(successfulDuration / successfulCalls);
             successfulMin     = String.valueOf(stats.getSuccessfulMin());
             successfulMax     = String.valueOf(stats.getSuccessfulMax());
             lastSuccessfulStart    = String.valueOf(stats.getLastSuccessfulStart());
             lastSuccessfulDuration = String.valueOf(stats.getLastSuccessfulDuration());
          }
 
          String unsuccessfulAverage;
          String unsuccessfulMin;
          String unsuccessfulMax;
          String lastUnsuccessfulStart;
          String lastUnsuccessfulDuration;
          if (unsuccessfulCalls == 0) {
             unsuccessfulAverage = "NA";
             unsuccessfulMin     = "NA";
             unsuccessfulMax     = "NA";
             lastUnsuccessfulStart    = "NA";
             lastUnsuccessfulDuration = "NA";
          } else if (unsuccessfulDuration == 0) {
             unsuccessfulAverage = "0";
             unsuccessfulMin     = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMax     = String.valueOf(stats.getUnsuccessfulMax());
             lastUnsuccessfulStart    = String.valueOf(stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          } else {
             unsuccessfulAverage = String.valueOf(unsuccessfulDuration / unsuccessfulCalls);
             unsuccessfulMin     = String.valueOf(stats.getUnsuccessfulMin());
             unsuccessfulMax     = String.valueOf(stats.getUnsuccessfulMax());
             lastUnsuccessfulStart    = String.valueOf(stats.getLastUnsuccessfulStart());
             lastUnsuccessfulDuration = String.valueOf(stats.getLastUnsuccessfulDuration());
          }
 
          context.startTag("function");
          context.attribute("name", function.getName());
          context.startTag("successful");
          context.attribute("count", String.valueOf(successfulCalls));
          context.attribute("min",   successfulMin);
          context.attribute("max",   successfulMax);
          context.attribute("average", successfulAverage);
          context.startTag("last");
          context.attribute("start",    lastSuccessfulStart);
          context.attribute("duration", lastSuccessfulDuration);
          context.endTag(); // last
          context.endTag(); // successful
          context.startTag("unsuccessful");
          context.attribute("count",   String.valueOf(unsuccessfulCalls));
          context.attribute("min",     unsuccessfulMin);
          context.attribute("max",     unsuccessfulMax);
          context.attribute("average", unsuccessfulAverage);
          context.startTag("last");
          context.attribute("start",    lastUnsuccessfulStart);
          context.attribute("duration", lastUnsuccessfulDuration);
          context.endTag(); // last
          context.endTag(); // unsuccessful
          context.endTag(); // function
       }
    }
 
    /**
     * Returns the XINS version.
     *
     * @param context
     *    the context, guaranteed to be not <code>null</code>.
     *
     * @throws IOException
     *    if an I/O error occurs.
     */
    private final void doGetVersion(CallContext context)
    throws IOException {
       context.param("xins.version",   Library.getVersion());
       context.param("xmlenc.version", XMLOutputter.getVersion());
    }
 }
