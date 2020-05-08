 /**
  * SAHARA Rig Client
  * 
  * Software abstraction of physical rig to provide rig session control
  * and rig device control. Automatically tests rig hardware and reports
  * the rig status to ensure rig goodness.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2009, University of Technology, Sydney
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  * Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  * Redistributions in binary form must reproduce the above copyright 
  *    notice, this list of conditions and the following disclaimer in the 
  *    documentation and/or other materials provided with the distribution.
  *  * Neither the name of the University of Technology, Sydney nor the names 
  *    of its contributors may be used to endorse or promote products derived from 
  *    this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE 
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR 
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * @author Michael Diponio (mdiponio)
  * @date 23th February 2009
  */
 
 /**
  * This file was auto-generated from WSDL
  * by the Apache Axis2 version: 1.4.1 Built on : Aug 19, 2008 (10:13:39 LKT)
  */
 package au.edu.uts.eng.remotelabs.rigclient.status;
 
 import java.lang.reflect.Method;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import org.apache.axiom.om.OMElement;
 import org.apache.axiom.om.OMNamespace;
 import org.apache.axiom.soap.SOAPEnvelope;
 import org.apache.axiom.soap.SOAPFactory;
 import org.apache.axis2.AxisFault;
 import org.apache.axis2.addressing.EndpointReference;
 import org.apache.axis2.client.OperationClient;
 import org.apache.axis2.client.ServiceClient;
 import org.apache.axis2.client.Stub;
 import org.apache.axis2.context.ConfigurationContext;
 import org.apache.axis2.context.MessageContext;
 import org.apache.axis2.databinding.ADBException;
 import org.apache.axis2.description.AxisOperation;
 import org.apache.axis2.description.AxisService;
 import org.apache.axis2.description.OutInAxisOperation;
 import org.apache.axis2.description.WSDL2Constants;
 import org.apache.axis2.wsdl.WSDLConstants;
 
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RegisterRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RegisterRigResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RemoveRig;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.RemoveRigResponse;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.UpdateRigStatus;
 import au.edu.uts.eng.remotelabs.rigclient.status.types.UpdateRigStatusResponse;
 
 /**
  * Scheduling Server local rig provider client SOAP implementation.
  */
 @SuppressWarnings("unchecked")
 public class SchedulingServerProviderStub extends Stub
 {
     protected AxisOperation[] _operations;
 
     //hashmaps to keep the fault mapping
     private final HashMap faultExceptionNameMap = new HashMap();
     private final HashMap faultExceptionClassNameMap = new HashMap();
     private final HashMap faultMessageMap = new HashMap();
 
     private static int counter = 0;
     
     private final QName[] opNameArray = null;
 
     /**
      * Uses <tt>http://remotelabs.eng.uts.edu.au:8080/schedserver/localrigprovider</tt>
      * as the end point.
      */
     public SchedulingServerProviderStub() throws AxisFault
     {
         this("http://remotelabs.eng.uts.edu.au:8080/schedserver/localrigprovider");
     }
 
     public SchedulingServerProviderStub(final String targetEndpoint) throws AxisFault
     {
         this(null, targetEndpoint);
     }
 
     public SchedulingServerProviderStub(final ConfigurationContext configurationContext)
             throws AxisFault
     {
         this(configurationContext, "http://remotelabs.eng.uts.edu.au:8080/schedserver/localrigprovider");
     }
 
     public SchedulingServerProviderStub(final ConfigurationContext configurationContext,
             final String targetEndpoint) throws AxisFault
     {
         this(configurationContext, targetEndpoint, false);
     }
     
     public SchedulingServerProviderStub(ConfigurationContext configurationContext,
             final String targetEndpoint, final boolean useSeparateListener) throws AxisFault
     {
         this.populateAxisService();
         this.populateFaults();
 
         this._serviceClient = new ServiceClient(configurationContext, this._service);
        this._serviceClient.getServiceContext().getConfigurationContext();
 
         this._serviceClient.getOptions().setTo(new EndpointReference(targetEndpoint));
         this._serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
     }
     
     private static synchronized String getUniqueSuffix()
     {
         if (SchedulingServerProviderStub.counter > 99999)
         {
             SchedulingServerProviderStub.counter = 0;
         }
         SchedulingServerProviderStub.counter = SchedulingServerProviderStub.counter + 1;
         return Long.toString(System.currentTimeMillis()) + "_" + SchedulingServerProviderStub.counter;
     }
 
     private Object fromOM(final OMElement param, final Class type,
             final Map<String, String> extraNamespaces) throws AxisFault
     {
         try
         {
             if (RemoveRig.class.equals(type))
             {
                 return RemoveRig.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
 
             if (RemoveRigResponse.class.equals(type))
             {
                 return RemoveRigResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
 
             if (UpdateRigStatus.class.equals(type))
             {
                 return UpdateRigStatus.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
 
             if (UpdateRigStatusResponse.class.equals(type))
             {
                 return UpdateRigStatusResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
 
             if (RegisterRig.class.equals(type))
             {
                 return RegisterRig.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
 
             if (RegisterRigResponse.class.equals(type))
             {
                 return RegisterRigResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
             }
         }
         catch (final Exception e)
         {
             throw AxisFault.makeFault(e);
         }
         return null;
     }
 
     /**
      * A utility method that copies the namepaces from the SOAPEnvelope
      */
     private Map<String, String> getEnvelopeNamespaces(final SOAPEnvelope env)
     {
         final Map<String, String> returnMap = new HashMap<String, String>();
         final Iterator namespaceIterator = env.getAllDeclaredNamespaces();
         while (namespaceIterator.hasNext())
         {
             final OMNamespace ns = (OMNamespace) namespaceIterator.next();
             returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
         }
         return returnMap;
     }
 
     private boolean optimizeContent(final QName opName)
     {
         if (this.opNameArray == null)
         {
             return false;
         }
         for (final QName element : this.opNameArray)
         {
             if (opName.equals(element))
             {
                 return true;
             }
         }
         return false;
     }
 
     private void populateAxisService() throws AxisFault
     {
         this._service = new AxisService("LocalRigProvider" + SchedulingServerProviderStub.getUniqueSuffix());
         this.addAnonymousOperations();
 
         AxisOperation __operation;
         this._operations = new AxisOperation[3];
 
         /* Remove rig operation. */
         __operation = new OutInAxisOperation();
         __operation.setName(new QName("http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "removeRig"));
         this._service.addOperation(__operation);
         this._operations[0] = __operation;
 
         /* Update status operation. */
         __operation = new OutInAxisOperation();
         __operation.setName(new QName("http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "updateRigStatus"));
         this._service.addOperation(__operation);
         this._operations[1] = __operation;
 
         /* Register rig operation. */
         __operation = new OutInAxisOperation();
         __operation.setName(new QName("http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "registerRig"));
         this._service.addOperation(__operation);
         this._operations[2] = __operation;
 
     }
 
     private void populateFaults()
     {
         /* No defined faults. */
     }
 
     public RegisterRigResponse registerRig(final RegisterRig registerRig) throws RemoteException
 
     {
         MessageContext _messageContext = null;
         try
         {
             final OperationClient _operationClient = this._serviceClient.createClient(this._operations[2].getName());
             _operationClient.getOptions().setAction(
                     "http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider/registerRig");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
             this.addPropertyToOperationClient(_operationClient, WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");
 
             _messageContext = new MessageContext();
 
             /* create SOAP envelope with that pay load. */
             SOAPEnvelope env = this.toEnvelope(Stub.getFactory(_operationClient.getOptions().getSoapVersionURI()), 
                     registerRig, this.optimizeContent(new QName(
                             "http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "registerRig")));
 
             /* Add SOAP soap_headers. */
             this._serviceClient.addHeadersToEnvelope(env);
             /* Set the message context with that soap envelope. */
             _messageContext.setEnvelope(env);
 
             /* add the message context to the operation client. */
             _operationClient.addMessageContext(_messageContext);
 
             /* Execute the operation client. */
             _operationClient.execute(true);
 
             final MessageContext _returnMessageContext = _operationClient
                     .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
             final SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
 
             final Object object = this.fromOM(_returnEnv.getBody().getFirstElement(),
                     RegisterRigResponse.class, this.getEnvelopeNamespaces(_returnEnv));
 
             return (RegisterRigResponse) object;
 
         }
         catch (final AxisFault f)
         {
 
             final OMElement faultElt = f.getDetail();
             if (faultElt != null)
             {
                 if (this.faultExceptionNameMap.containsKey(faultElt.getQName()))
                 {
                     //make the fault by reflection
                     try
                     {
                         final String exceptionClassName = (String) this.faultExceptionClassNameMap
                                 .get(faultElt.getQName());
                         final Class exceptionClass = Class.forName(exceptionClassName);
                         final Exception ex = (Exception) exceptionClass.newInstance();
                         final String messageClassName = (String) this.faultMessageMap.get(faultElt.getQName());
                         final Class messageClass = Class.forName(messageClassName);
                         final Object messageObject = this.fromOM(faultElt, messageClass, null);
                         final Method m = exceptionClass.getMethod("setFaultMessage", new Class[] { messageClass });
                         m.invoke(ex, new Object[] { messageObject });
                         throw new RemoteException(ex.getMessage(), ex);
                     }
                     catch (final Exception e)
                     {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }
                 else
                 {
                     throw f;
                 }
             }
             else
             {
                 throw f;
             }
         }
         finally
         {
             _messageContext.getTransportOut().getSender().cleanup(_messageContext);
         }
     }
 
     public RemoveRigResponse removeRig(final RemoveRig removeRig) throws RemoteException
     {
         MessageContext _messageContext = null;
         try
         {
             final OperationClient _operationClient = this._serviceClient
                     .createClient(this._operations[0].getName());
             _operationClient.getOptions().setAction(
                     "http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider/removeRig");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
             this.addPropertyToOperationClient(_operationClient, WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,
                     "&");
 
             _messageContext = new MessageContext();
 
             SOAPEnvelope env = this.toEnvelope(Stub.getFactory(_operationClient.getOptions().getSoapVersionURI()), 
                     removeRig, this.optimizeContent(new QName(
                             "http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "removeRig")));
 
             this._serviceClient.addHeadersToEnvelope(env);
             _messageContext.setEnvelope(env);
             _operationClient.addMessageContext(_messageContext);
             _operationClient.execute(true);
 
             final MessageContext _returnMessageContext = _operationClient
                     .getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
             final SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
 
             final Object object = this.fromOM(_returnEnv.getBody().getFirstElement(),
                     RemoveRigResponse.class, this.getEnvelopeNamespaces(_returnEnv));
 
             return (RemoveRigResponse) object;
 
         }
         catch (final AxisFault f)
         {
 
             final OMElement faultElt = f.getDetail();
             if (faultElt != null)
             {
                 if (this.faultExceptionNameMap.containsKey(faultElt.getQName()))
                 {
                     //make the fault by reflection
                     try
                     {
                         final String exceptionClassName = (String) this.faultExceptionClassNameMap.get(faultElt.getQName());
                         final Class exceptionClass = Class.forName(exceptionClassName);
                         final Exception ex = (Exception) exceptionClass.newInstance();
 
                         final String messageClassName = (String) this.faultMessageMap.get(faultElt.getQName());
                         final Class messageClass = Class.forName(messageClassName);
                         final Object messageObject = this.fromOM(faultElt, messageClass, null);
                         final Method m = exceptionClass.getMethod("setFaultMessage", new Class[] { messageClass });
                         m.invoke(ex, new Object[] { messageObject });
 
                         throw new RemoteException(ex.getMessage(), ex);
                     }
                     catch (final Exception e)
                     {
                         throw f;
                     }
                 }
                 else
                 {
                     throw f;
                 }
             }
             else
             {
                 throw f;
             }
         }
         finally
         {
             _messageContext.getTransportOut().getSender().cleanup(_messageContext);
         }
     }
     
     public UpdateRigStatusResponse updateRigStatus(final UpdateRigStatus updateRigStatus) throws RemoteException
     {
         MessageContext _messageContext = null;
         try
         {
             final OperationClient _operationClient = this._serviceClient
                     .createClient(this._operations[1].getName());
             _operationClient.getOptions().setAction("http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider/updateRigStatus");
             _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
             this.addPropertyToOperationClient(_operationClient, WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR, "&");
 
             _messageContext = new MessageContext();
             SOAPEnvelope env = this.toEnvelope(Stub.getFactory(_operationClient.getOptions().getSoapVersionURI()), 
                     updateRigStatus, this.optimizeContent(new QName(
                             "http://remotelabs.eng.uts.edu.au/schedserver/localrigprovider", "updateRigStatus")));
             this._serviceClient.addHeadersToEnvelope(env);
             _messageContext.setEnvelope(env);
             _operationClient.addMessageContext(_messageContext);
             _operationClient.execute(true);
 
             final MessageContext _returnMessageContext = _operationClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
             final SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
 
             final Object object = this.fromOM(_returnEnv.getBody().getFirstElement(), UpdateRigStatusResponse.class, 
                     this.getEnvelopeNamespaces(_returnEnv));
 
             return (UpdateRigStatusResponse) object;
         }
         catch (final AxisFault f)
         {
 
             final OMElement faultElt = f.getDetail();
             if (faultElt != null)
             {
                 if (this.faultExceptionNameMap.containsKey(faultElt.getQName()))
                 {
                     try
                     {
                         final String exceptionClassName = (String) this.faultExceptionClassNameMap
                                 .get(faultElt.getQName());
                         final Class exceptionClass = Class.forName(exceptionClassName);
                         final Exception ex = (Exception) exceptionClass.newInstance();
                         final String messageClassName = (String) this.faultMessageMap.get(faultElt
                                 .getQName());
                         final Class messageClass = Class.forName(messageClassName);
                         final Object messageObject = this.fromOM(faultElt, messageClass, null);
                         final Method m = exceptionClass.getMethod("setFaultMessage",
                                 new Class[] { messageClass });
                         m.invoke(ex, new Object[] { messageObject });
 
                         throw new RemoteException(ex.getMessage(), ex);
                     }
                     catch (final Exception e)
                     {
                         throw f;
                     }
                 }
                 else
                 {
                     throw f;
                 }
             }
             else
             {
                 throw f;
             }
         }
         finally
         {
             _messageContext.getTransportOut().getSender().cleanup(_messageContext);
         }
     }
 
     private SOAPEnvelope toEnvelope(final SOAPFactory factory, final RegisterRig param, final boolean optimizeContent) throws AxisFault
     {
 
         try
         {
             final SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
             emptyEnvelope.getBody().addChild(param.getOMElement(RegisterRig.MY_QNAME, factory));
             return emptyEnvelope;
         }
         catch (final ADBException e)
         {
             throw AxisFault.makeFault(e);
         }
 
     }
 
     private SOAPEnvelope toEnvelope(final SOAPFactory factory, final RemoveRig param, final boolean optimizeContent)
             throws AxisFault
     {
         try
         {
             final SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
             emptyEnvelope.getBody().addChild(param.getOMElement(RemoveRig.MY_QNAME, factory));
             return emptyEnvelope;
         }
         catch (final ADBException e)
         {
             throw AxisFault.makeFault(e);
         }
 
     }
 
     private SOAPEnvelope toEnvelope(final SOAPFactory factory, final UpdateRigStatus param, 
             final boolean optimizeContent) throws AxisFault
     {
         try
         {
             final SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
             emptyEnvelope.getBody().addChild(param.getOMElement(UpdateRigStatus.MY_QNAME, factory));
             return emptyEnvelope;
         }
         catch (final ADBException e)
         {
             throw AxisFault.makeFault(e);
         }
 
     }
 }
