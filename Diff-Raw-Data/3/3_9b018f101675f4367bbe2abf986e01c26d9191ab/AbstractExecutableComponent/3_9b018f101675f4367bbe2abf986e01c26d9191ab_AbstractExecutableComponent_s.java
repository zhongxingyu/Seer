 /**
 *
 * University of Illinois/NCSA
 * Open Source License
 *
 * Copyright (c) 2008, NCSA.  All rights reserved.
 *
 * Developed by:
 * The Automated Learning Group
 * University of Illinois at Urbana-Champaign
 * http://www.seasr.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal with the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 *
 * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimers.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimers in
 * the documentation and/or other materials provided with the distribution.
 *
 * Neither the names of The Automated Learning Group, University of
 * Illinois at Urbana-Champaign, nor the names of its contributors may
 * be used to endorse or promote products derived from this Software
 * without specific prior written permission.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE CONTRIBUTORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS WITH THE SOFTWARE.
 *
 */
 
 package org.seasr.meandre.components.abstracts;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.meandre.annotations.ComponentOutput;
 import org.meandre.annotations.ComponentProperty;
 import org.meandre.core.ComponentContext;
 import org.meandre.core.ComponentContextException;
 import org.meandre.core.ComponentContextProperties;
 import org.meandre.core.ComponentExecutionException;
 import org.meandre.core.ExecutableComponent;
 import org.meandre.core.system.components.ext.StreamInitiator;
 import org.meandre.core.system.components.ext.StreamTerminator;
import org.meandre.core.utils.ExceptionFormatter;
 import org.seasr.datatypes.core.Names;
 import org.seasr.meandre.components.ComponentInputCache;
 import org.seasr.meandre.components.PackedDataComponents;
 import org.seasr.meandre.components.abstracts.util.ComponentLogFormatter;
 import org.seasr.meandre.components.abstracts.util.WebConsoleHandler;
 
 /**
  * @author Bernie Acs
  * @author Boris Capitanu
  */
 public abstract class AbstractExecutableComponent implements ExecutableComponent {
 
     //------------------------------ OUTPUTS -----------------------------------------------------
 
     @ComponentOutput(
             description = "This port is used to output any unhandled errors encountered during the execution of this component",
             name = Names.PORT_ERROR
     )
     public static final String OUT_ERROR = Names.PORT_ERROR;
 
 
     //------------------------------ PROPERTIES --------------------------------------------------
 
     @ComponentProperty(
             description = "Controls the verbosity of debug messages printed by the component during execution.<br/>" +
                           "Possible values are: off, severe, warning, info, config, fine, finer, finest, all<br>" +
                           "Append ',mirror' to any of the values above to mirror that output to the server logs.",
             defaultValue = "info",
             name = "_debug_level"
     )
     public static final String PROP_DEBUG_LEVEL = "_debug_level";
 
     @ComponentProperty(
             description = "Set to 'true' to ignore all unhandled exceptions and prevent the flow from being terminated. " +
                           "Setting this property to 'false' will result in the flow being terminated in the event " +
                           "an unhandled exception is thrown during the execution of this component",
             defaultValue = "false",
             name = "_ignore_errors"
     )
     public static final String PROP_IGNORE_ERRORS = "_ignore_errors";
 
     //--------------------------------------------------------------------------------------------
 
 
     public Set<String> inputPortNames = null;
     public Set<String> outputPortNames = null;
     //
     public ComponentInputCache componentInputCache;
     //
     public PackedDataComponents packedDataComponentsInput = null;
     public PackedDataComponents packedDataComponentsOutput = null;
 
     public Set<String> inputPortsWithInitiators = null;
     public Set<String> inputPortsWithTerminators = null;
 
     public ComponentContext componentContext = null;
     public Logger console = null;
     public boolean ignoreErrors = false;
 
 
     //--------------------------------------------------------------------------------------------
 
     /*
      * (non-Javadoc)
      *
      * @see org.meandre.core.ExecutableComponent#initialize(org.meandre.core.ComponentContextProperties)
      */
     public void initialize(ComponentContextProperties ccp)
             throws ComponentExecutionException, ComponentContextException {
 
         componentInputCache = new ComponentInputCache(new HashSet<String>(Arrays.asList(ccp.getInputNames())));
 
         Formatter formatter = new ComponentLogFormatter(ccp.getInstanceName(), ccp.getExecutionInstanceID(), ccp.getFlowExecutionInstanceID(), ccp.getFlowID());
         Handler consoleHandler = new WebConsoleHandler(ccp.getOutputConsole(), formatter);
         consoleHandler.setLevel(Level.ALL);
 
         console = Logger.getLogger(ccp.getFlowExecutionInstanceID() + "/" + ccp.getExecutionInstanceID());
         console.addHandler(consoleHandler);
 
         console.setParent(ccp.getLogger());
         console.setLevel(Level.ALL);
 
         String debugLevel = ccp.getProperty(PROP_DEBUG_LEVEL).trim();
         StringTokenizer st = new StringTokenizer(debugLevel, " ,;/+&");
         if (st.countTokens() > 2)
             throw new ComponentContextException("Invalid value for property '" + PROP_DEBUG_LEVEL + "' specified: " + debugLevel);
 
         Vector<String> tokens = new Vector<String>();
         while (st.hasMoreTokens())
             tokens.add(st.nextToken().trim().toUpperCase());
 
         boolean mirrorConsoleOutput = false;
         if (tokens.contains("MIRROR")) {
             mirrorConsoleOutput = true;
             tokens.remove("MIRROR");
         }
 
         console.setUseParentHandlers(mirrorConsoleOutput);
 
         try {
             Level consoleOutputLevel = Level.parse(tokens.get(0));
             console.setLevel(consoleOutputLevel);
         }
         catch (IllegalArgumentException e) {
             console.throwing(getClass().getName(), "initialize", e);
             throw new ComponentContextException(e);
         }
 
         ignoreErrors = Boolean.parseBoolean(ccp.getProperty(PROP_IGNORE_ERRORS));
         if (ignoreErrors)
             console.fine("Exceptions are being ignored per user's request.");
 
         inputPortNames = new HashSet<String>();
         for (String componentInput : ccp.getInputNames())
             inputPortNames.add(componentInput);
 
         outputPortNames = new HashSet<String>();
         for (String componentOutput : ccp.getOutputNames())
             outputPortNames.add(componentOutput);
 
         inputPortNames = Collections.unmodifiableSet(inputPortNames);
         outputPortNames = Collections.unmodifiableSet(outputPortNames);
 
         componentInputCache.setLogger(console);
 
         try {
             console.entering(getClass().getName(), "initializeCallBack", ccp);
             initializeCallBack(ccp);
             console.exiting(getClass().getName(), "initializeCallBack");
         }
         catch (ComponentContextException e) {
             console.throwing(getClass().getName(), "initializeCallBack", e);
             if (!ignoreErrors)
                 throw e;
         }
         catch (ComponentExecutionException e) {
             console.throwing(getClass().getName(), "initializeCallBack", e);
             if (!ignoreErrors)
                 throw e;
         }
         catch (Exception e) {
             console.throwing(getClass().getName(), "initializeCallBack", e);
             if (!ignoreErrors)
                 throw new ComponentContextException(e);
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.meandre.core.ExecutableComponent#execute(org.meandre.core.ComponentContext)
      */
     public void execute(ComponentContext cc)
             throws ComponentExecutionException, ComponentContextException {
 
         componentContext = cc;
 
         // Initialize the PackedDataComponent variables each iteration
         packedDataComponentsInput = new PackedDataComponents();
         packedDataComponentsOutput = new PackedDataComponents();
 
         inputPortsWithInitiators = new HashSet<String>();
         inputPortsWithTerminators = new HashSet<String>();
 
         try {
             boolean callExecute = true;
 
             for (String inputPort : inputPortNames) {
                 if (!cc.isInputAvailable(inputPort)) continue;
 
                 Object data = cc.getDataComponentFromInput(inputPort);
 
                 // show the inputs and data-types received on each input in "debug" mode
                 console.finer(String.format("Input port '%s' has data of type '%s'",
                             inputPort, data.getClass().getName()));
 
                 if (data instanceof StreamInitiator)
                     inputPortsWithInitiators.add(inputPort);
 
                 else
 
                 if (data instanceof StreamTerminator)
                     inputPortsWithTerminators.add(inputPort);
             }
 
             inputPortsWithInitiators = Collections.unmodifiableSet(inputPortsWithInitiators);
             inputPortsWithTerminators = Collections.unmodifiableSet(inputPortsWithTerminators);
 
             if (inputPortsWithInitiators.size() > 0) {
                 callExecute = false;
                 handleStreamInitiators();
             }
 
             if (inputPortsWithTerminators.size() > 0) {
                 callExecute = false;
                 handleStreamTerminators();
             }
 
             if (callExecute) {
                 console.entering(getClass().getName(), "executeCallBack", cc);
                 executeCallBack(cc);
                 console.exiting(getClass().getName(), "executeCallBack");
             }
         }
         catch (ComponentContextException e) {
             console.throwing(getClass().getName(), "executeCallBack", e);
             cc.pushDataComponentToOutput(OUT_ERROR, ExceptionFormatter.formatException(e));
 
             if (!ignoreErrors)
                 throw e;
         }
         catch (ComponentExecutionException e) {
             console.throwing(getClass().getName(), "executeCallBack", e);
             cc.pushDataComponentToOutput(OUT_ERROR, ExceptionFormatter.formatException(e));
 
             if (!ignoreErrors)
                 throw e;
         }
         catch (Exception e) {
             console.throwing(getClass().getName(), "executeCallBack", e);
             cc.pushDataComponentToOutput(OUT_ERROR, ExceptionFormatter.formatException(e));
 
             if (!ignoreErrors)
                 throw new ComponentExecutionException(e);
         }
     }
 
     /*
      * (non-Javadoc)
      *
      * @see org.meandre.core.ExecutableComponent#dispose(org.meandre.core.ComponentContextProperties)
      */
     public void dispose(ComponentContextProperties ccp)
             throws ComponentExecutionException, ComponentContextException {
 
         try {
             console.entering(getClass().getName(), "disposeCallBack", ccp);
             disposeCallBack(ccp);
             console.exiting(getClass().getName(), "disposeCallBack");
 
             componentInputCache.dispose();
         }
         catch (ComponentContextException e) {
             console.throwing(getClass().getName(), "disposeCallBack", e);
             throw e;
         }
         catch (ComponentExecutionException e) {
             console.throwing(getClass().getName(), "disposeCallBack", e);
             if (!ignoreErrors)
                 throw e;
         }
         catch (Exception e) {
             console.throwing(getClass().getName(), "disposeCallBack", e);
             if (!ignoreErrors)
                 throw new ComponentContextException(e);
         }
     }
 
     //--------------------------------------------------------------------------------------------
 
     public abstract void initializeCallBack(ComponentContextProperties ccp)
         throws Exception;
 
     public abstract void executeCallBack(ComponentContext cc)
         throws Exception;
 
     public abstract void disposeCallBack(ComponentContextProperties ccp)
         throws Exception;
 
     //--------------------------------------------------------------------------------------------
 
     /**
      * Forwards the stream initiator on all output ports (except the error port);
      * Make sure you override this method for components with FiringPolicy = any and simply invoke 'executeCallBack(componentContext);' and
      * deal with the stream delimiters there; this is the most reliable way to work with streams in such components;
      * Override if needing to handle the delimiters differently.
      *
      * @throws Exception Thrown in the event of an error
      */
     public void handleStreamInitiators() throws Exception {
         console.entering(getClass().getName(), "handleStreamInitiators", inputPortsWithInitiators);
 
         // Sanity check - make sure stream initiators arrived on all input ports at the same time
         if (!inputPortsWithInitiators.containsAll(inputPortNames))
             throw new ComponentExecutionException(getClass().getName() + ": Stream delimiters must arrive on all input ports at the same time for components with FiringPolicy = all; " +
             		"For FiringPolicy = any components you *must* override handleStreamInitiators() and handleStreamTerminators() to call 'executeCallBack(componentContext);' " +
             		"and then deal with stream delimiters in 'executeCallBack'. This is the most reliable way to work with streams.");
 
         StreamInitiator si = (StreamInitiator) componentContext.getDataComponentFromInput(componentContext.getInputNames()[0]);
         console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamInitiator.class.getSimpleName(), si.getStreamId()));
 
         for (String portName : componentContext.getOutputNames()) {
             if (portName.equals(OUT_ERROR)) continue;
 
             componentContext.pushDataComponentToOutput(portName, si);
         }
 
         console.exiting(getClass().getName(), "handleStreamInitiators");
     }
 
     /**
      * Forwards the stream terminator on all output ports (except the error port);
      * Make sure you override this method for components with FiringPolicy = any and simply invoke 'executeCallBack(componentContext);' and
      * deal with the stream delimiters there; this is the most reliable way to work with streams in such components;
      * Override if needing to handle the delimiters differently.
      *
      * @throws Exception Thrown in the event of an error
      */
     public void handleStreamTerminators() throws Exception {
         console.entering(getClass().getName(), "handleStreamTerminators", inputPortsWithTerminators);
 
         // Sanity check - make sure stream terminators arrived on all input ports at the same time
         if (!inputPortsWithTerminators.containsAll(inputPortNames))
             throw new ComponentExecutionException("Stream delimiters must arrive on all input ports at the same time for components with FiringPolicy = all; " +
                     "For FiringPolicy = any components you *must* override handleStreamInitiators() and handleStreamTerminators() to call 'executeCallBack(componentContext);' " +
                     "and then deal with stream delimiters in 'executeCallBack'. This is the most reliable way to work with streams.");
 
         StreamTerminator st = (StreamTerminator) componentContext.getDataComponentFromInput(componentContext.getInputNames()[0]);
         console.fine(String.format("Forwarding the %s (id: %d) on all output ports...", StreamTerminator.class.getSimpleName(), st.getStreamId()));
 
         for (String portName : componentContext.getOutputNames()) {
             if (portName.equals(OUT_ERROR)) continue;
 
             componentContext.pushDataComponentToOutput(portName, st);
         }
 
         console.exiting(getClass().getName(), "handleStreamTerminators");
     }
 
     //--------------------------------------------------------------------------------------------
 
     /**
      * Enables runtime interrogation to determine if a ComponentInput is
      * connected in a flow.
      *
      * @param componentInputName
      * @return
      */
     public boolean isComponentInputConnected(String componentInputName) {
         return inputPortNames.contains(componentInputName);
     }
 
     /**
      * Enables runtime interrogation to determine if a ComponentOutput is
      * connected in a flow.
      *
      * @param componentOutputName
      * @return
      */
     public boolean isComponentOutputConnected(String componentOutputName) {
         return outputPortNames.contains(componentOutputName);
     }
 
     public void outputError(String message, Level level) {
         outputError(message, null, level);
     }
 
     public void outputError(Exception e, Level level) {
         outputError(null, e, level);
     }
 
     public void outputError(String message, Exception e, Level level) {
         if (componentContext == null) return;
 
         String errorMsg;
 
         if (level != null)
             console.log(level, (message != null) ? message : "", e);
 
         if ((message != null) && (e != null))
             errorMsg = String.format("%s%n%s", message, ExceptionFormatter.formatException(e));
         else
             errorMsg = (message != null) ? message : ExceptionFormatter.formatException(e);
 
         try {
             componentContext.pushDataComponentToOutput(OUT_ERROR, errorMsg);
         }
         catch (ComponentContextException e1) {
             console.log(Level.SEVERE, "Cannot push to the error output!", e1);
         }
     }
 
     /**
      * Attempts to retrieve the value of a property
      *
      * @param propName The property name
      * @param ignoreWhitespace True if trim() should be called on the value, False otherwise
      * @param failIfEmptyValue True to throw exception if the value of the property is empty
      * @param cc The component context
      * @return The property value
      * @throws ComponentExecutionException Thrown if the property does not exist, or if failIfEmptyValue=true and the value of the property is empty
      */
     public String getPropertyOrDieTrying(String propName, boolean ignoreWhitespace, boolean failIfEmptyValue, ComponentContextProperties context)
         throws ComponentExecutionException {
 
         String propValue = context.getProperty(propName);
         if (propValue == null) {
             StringBuilder sb = new StringBuilder();
             for (String name : context.getPropertyNames())
                 sb.append(", ").append(name);
 
             throw new ComponentExecutionException(String.format(
                     "Missing property '%s' for component class '%s' - check the component RDF descriptor! " +
                     "Available properties: [%s]", propName, getClass().getSimpleName(), sb.substring(2)));
         }
 
         if (ignoreWhitespace)
             propValue = propValue.trim();
 
         if (failIfEmptyValue && propValue.length() == 0)
             throw new ComponentExecutionException(String.format("The value of the property '%s' for component " +
             		"class '%s' has not been set! Cannot continue!", propName, getClass().getSimpleName()));
 
         return propValue;
     }
 
     /**
     * Attempts to retrieve the value of a property trims whitespace and fails if the value is empty
     *
     * @param propName The property name
     * @param cc The component context
     * @return The property value
     * @throws ComponentExecutionException Thrown if the property does not exist, or if value of the property is empty
     */
     public String getPropertyOrDieTrying(String propName, ComponentContextProperties context) throws ComponentExecutionException {
     	return getPropertyOrDieTrying(propName, true, true, context);
     }
 }
