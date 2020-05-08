 /*
  * File name:           $RCSfile: $
  *
  * Revision:            $Revision: $
  * Last revised by:     $Author: $
  * Last revision date:  $Date: $
  *
  * Original Author:     Arthur Copeland
  *
  * Licensed using GPL Available - http://opensource.org/licenses/gpl-license.php
  *
  */
 package org.yestech.jmlnitrate.transformation.inbound;
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.yestech.jmlnitrate.core.Kernel;
 import org.yestech.jmlnitrate.core.KernelContext;
 import org.yestech.jmlnitrate.core.KernelProcess;
 import org.yestech.jmlnitrate.handler.request.HttpServletRequestHandler;
 import org.yestech.jmlnitrate.handler.request.RequestHandler;
 import org.yestech.jmlnitrate.transformation.TransformationParameter;
 /**
  *  This class represents an {InboundTransformation} that is specific for {@link
  *  HttpServletRequest}s. The result of this transformation will be a {@link
  *  KernelContext}. This transformation is not Thread-Safe.
  *
  * @author  Arthur Copeland
  * @version  $Revision: 3 $
  */
 public class HttpServletRequestInboundTransformation
      extends BaseInboundTransformation {
     //--------------------------------------------------------------------------
     // S T A T I C   V A R I A B L E S
     //--------------------------------------------------------------------------
     /**
      *  Holds the logger
      */
     final private static Log logger = LogFactory.getLog(HttpServletRequestInboundTransformation.class);
 
     //--------------------------------------------------------------------------
     // M E M B E R   V A R I A B L E S
     //--------------------------------------------------------------------------
     /**
      *  Holds the HttpServletRequestHandler to transform.
      */
     private HttpServletRequestHandler handler;
 
 
     //--------------------------------------------------------------------------
     // C O N S T R U C T O R S
     //--------------------------------------------------------------------------
     /**
      *  Default Ctor.
      */
     public HttpServletRequestInboundTransformation() {
         super();
     }
 
 
     //--------------------------------------------------------------------------
     // I N B O U N D T R A N S F O R M A T I O N   C O N T R A C T
     // M E T H O D S
     //--------------------------------------------------------------------------
     /**
      *  Transforms a {@link RequestHandler} to a {@link
     *  org.jmlnitrate.core.KernelContext}. <br>
      *  The format of the Request: <i>Required</i>
      *  <ul>
      *    <li> <i>className</i> - The FQN Class Name of the Class to execute
      *    <li> ctorType - The Class Types needed for the Constructor to use by the
      *    Class Name. If not specified then a default ctor is assumed. (ie
      *    ctorType=java.lang.String)
      *    <li> ctorArg - The Class Arguments needed for the Constructor to use by
      *    the Class Name. If not specified then a default ctor is assumed. (ie
      *    ctorArg=Billy)
      *    <li> staticMethod - Whether the method is static or not. If not set then
      *    it is assumed to be static. (True,Y,1 : False,N,0)
      *    <li> <i>methodName</i> - The name of the method to execute.
      *  </ul>
      *  All the other parameters are assumed to be the arguments needed by the
      *  method during execution. The format of the method arguments is: <br>
      *  FQN ClassName Type# = Value. (ie java.lang.String1=Test Argument <br>
      *  It is assumes that the Constructor Object will be <b>in order</b> .
      *
      * @param  request The Request to transform
      * @throws  Exception if an error happens
      */
     public void transform(RequestHandler request) throws Exception {
         this.handler = (HttpServletRequestHandler) request;
         KernelProcess process = createKernelProcess();
         setTransformationResult(new KernelContext(process));
     }
 
 
     //--------------------------------------------------------------------------
     // I N T E R N A L   M E T H O D S
     //--------------------------------------------------------------------------
     /**
      *  Creates a KernelProcess from the Request.
      *
      * @return  the KernelProcess Created
      * @exception  Exception Description of the Exception
      */
     private KernelProcess createKernelProcess() throws Exception {
         String className = getClassName();
         String processName = className;
         Object[] ctor = getConstructor();
         Object[] method = getMethod();
         return Kernel.createKernelProcess(processName, className, ctor,
             method);
     }
 
 
     /**
      *  Retrieves the class name from the handler.
      *
      * @return  The className value
      */
     private String getClassName() {
         String className =
             handler.getValue(TransformationParameter.CLASS.getName());
         if (className == null || className.equals("")) {
             logger.error("Parameter " + TransformationParameter.CLASS.getName()
                  + " must be supplied....");
             throw new NullPointerException("Parameter " +
                 TransformationParameter.CLASS.getName()
                  + " must be supplied....");
         }
         return className;
     }
 
 
     /**
      *  Retrieves the Constructor from the handler.
      *
      * @return  The constructor value
      * @exception  Exception Description of the Exception
      */
     private Object[] getConstructor() throws Exception {
         return getConstructor(handler.getValue(
             TransformationParameter.CONSTRUCTOR.getName()));
     }
 
 
     /**
      *  Retrieves the Method from the handler.
      *
      * @return  The method value
      * @exception  Exception Description of the Exception
      */
     private Object[] getMethod() throws Exception {
         Object[] method = getMethod(handler.getValue(
             TransformationParameter.METHOD.getName()));
 
         //get the method arguments
         int arg = method.length - 2;
         Object argumentList[] = new Object[arg];
         for (int i = 0; i < arg; i++) {
             argumentList[i] = handler.getValues(
                 TransformationParameter.METHOD_ARGUMENT.getName() + i);
         }
 
         Object[] arguments =
             getMethodArguments(argumentList);
 
         return getMethod(method, arguments);
     }
 
 }
