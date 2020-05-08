 
 /**
  * LimesServiceImplStub.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
  */
         package de.uni_leipzig.simba.limeswebservice.server;
 
 import org.apache.axis2.Constants;
 
 
         
 
         /*
         *  LimesServiceImplStub java implementation
         */
 
         
         public class LimesServiceImplStub extends org.apache.axis2.client.Stub
         {
         protected org.apache.axis2.description.AxisOperation[] _operations;
        // URLS
        public static final String localhost = 	"http://localhost:8080/";
        public static final String host = "http://139.18.2.158:8080/";
        
         //hashmaps to keep the fault mapping
         private java.util.HashMap faultExceptionNameMap = new java.util.HashMap();
         private java.util.HashMap faultExceptionClassNameMap = new java.util.HashMap();
         private java.util.HashMap faultMessageMap = new java.util.HashMap();
 
         private static int counter = 0;
 
         private static synchronized java.lang.String getUniqueSuffix(){
             // reset the counter if it is greater than 99999
             if (counter > 99999){
                 counter = 0;
             }
             counter = counter + 1; 
             return java.lang.Long.toString(java.lang.System.currentTimeMillis()) + "_" + counter;
         }
 
     
     private void populateAxisService() throws org.apache.axis2.AxisFault {
 
      //creating the Service with a unique name
      _service = new org.apache.axis2.description.AxisService("LimesServiceImpl" + getUniqueSuffix());
      addAnonymousOperations();
 
         //creating the operations
         org.apache.axis2.description.AxisOperation __operation;
 
         _operations = new org.apache.axis2.description.AxisOperation[10];
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "fetchMetricMap"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[0]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "fetchSourceData"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[1]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "fetchTargetData"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[2]=__operation;
             
         
                     __operation = new org.apache.axis2.description.OutOnlyAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "getMapping"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[3]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "startSession"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[4]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "getMetricAdvice"));
             
           _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[5]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "polling"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[6]=__operation;
             
         
                    __operation = new org.apache.axis2.description.OutInAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "continueSession"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[7]=__operation;
             
         
                     __operation = new org.apache.axis2.description.OutOnlyAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "setMetricSpec"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[8]=__operation;
             
         
                     __operation = new org.apache.axis2.description.OutOnlyAxisOperation();
                 
 
             __operation.setName(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de", "setSpecification"));
 	    _service.addOperation(__operation);
 	    
 
 	    
 	    
             _operations[9]=__operation;
             
         
         }
 
     //populates the faults
     private void populateFaults(){
          
 
 
     }
 
     /**
       *Constructor that takes in a configContext
       */
 
     public LimesServiceImplStub(org.apache.axis2.context.ConfigurationContext configurationContext,
        java.lang.String targetEndpoint)
        throws org.apache.axis2.AxisFault {
          this(configurationContext,targetEndpoint,false);
    }
 
 
    /**
      * Constructor that takes in a configContext  and useseperate listner
      */
    public LimesServiceImplStub(org.apache.axis2.context.ConfigurationContext configurationContext,
         java.lang.String targetEndpoint, boolean useSeparateListener)
         throws org.apache.axis2.AxisFault {
          //To populate AxisService
          populateAxisService();
          populateFaults();
 
         _serviceClient = new org.apache.axis2.client.ServiceClient(configurationContext,_service);
         
 	
         _serviceClient.getOptions().setTo(new org.apache.axis2.addressing.EndpointReference(
                 targetEndpoint));
         _serviceClient.getOptions().setUseSeparateListener(useSeparateListener);
         
             //Set the soap version
             _serviceClient.getOptions().setSoapVersionURI(org.apache.axiom.soap.SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);
         _serviceClient.engageModule(Constants.MODULE_ADDRESSING);
     
     }
 
     /**
      * Default Constructor
      */
     public LimesServiceImplStub(org.apache.axis2.context.ConfigurationContext configurationContext) throws org.apache.axis2.AxisFault {

                    this(configurationContext, host+"LimesWebService/services/LimesServiceImpl.LimesServiceImplHttpSoap12Endpoint/" );
                 
     }
 
     /**
      * Default Constructor
      */
     public LimesServiceImplStub() throws org.apache.axis2.AxisFault {
         
                    this(host+"LimesWebService/services/LimesServiceImpl.LimesServiceImplHttpSoap12Endpoint/" );
                 
     }
 
     /**
      * Constructor taking the target endpoint
      */
     public LimesServiceImplStub(java.lang.String targetEndpoint) throws org.apache.axis2.AxisFault {
         this(null,targetEndpoint);
     }
 
 
 
         
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#fetchMetricMap
                      * @param fetchMetricMap0
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse fetchMetricMap(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap fetchMetricMap0)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
               _operationClient.getOptions().setAction("urn:fetchMetricMap");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchMetricMap0,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchMetricMap")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchMetricMap"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startfetchMetricMap
                     * @param fetchMetricMap0
                 
                 */
                 public  void startfetchMetricMap(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap fetchMetricMap0,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[0].getName());
              _operationClient.getOptions().setAction("urn:fetchMetricMap");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchMetricMap0,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchMetricMap")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchMetricMap"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultfetchMetricMap(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorfetchMetricMap(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchMetricMap"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorfetchMetricMap(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchMetricMap(f);
                                             }
 									    } else {
 										    callback.receiveErrorfetchMetricMap(f);
 									    }
 									} else {
 									    callback.receiveErrorfetchMetricMap(f);
 									}
 								} else {
 								    callback.receiveErrorfetchMetricMap(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorfetchMetricMap(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[0].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[0].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                 
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#fetchSourceData
                      * @param fetchSourceData2
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse fetchSourceData(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData fetchSourceData2)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
               _operationClient.getOptions().setAction("urn:fetchSourceData");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchSourceData2,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchSourceData")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchSourceData"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startfetchSourceData
                     * @param fetchSourceData2
                 
                 */
                 public  void startfetchSourceData(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData fetchSourceData2,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[1].getName());
              _operationClient.getOptions().setAction("urn:fetchSourceData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchSourceData2,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchSourceData")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchSourceData"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultfetchSourceData(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorfetchSourceData(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchSourceData"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorfetchSourceData(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchSourceData(f);
                                             }
 									    } else {
 										    callback.receiveErrorfetchSourceData(f);
 									    }
 									} else {
 									    callback.receiveErrorfetchSourceData(f);
 									}
 								} else {
 								    callback.receiveErrorfetchSourceData(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorfetchSourceData(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[1].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[1].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                 
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#fetchTargetData
                      * @param fetchTargetData4
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse fetchTargetData(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData fetchTargetData4)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
               _operationClient.getOptions().setAction("urn:fetchTargetData");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchTargetData4,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchTargetData")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchTargetData"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startfetchTargetData
                     * @param fetchTargetData4
                 
                 */
                 public  void startfetchTargetData(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData fetchTargetData4,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[2].getName());
              _operationClient.getOptions().setAction("urn:fetchTargetData");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     fetchTargetData4,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchTargetData")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "fetchTargetData"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultfetchTargetData(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorfetchTargetData(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"fetchTargetData"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorfetchTargetData(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorfetchTargetData(f);
                                             }
 									    } else {
 										    callback.receiveErrorfetchTargetData(f);
 									    }
 									} else {
 									    callback.receiveErrorfetchTargetData(f);
 									}
 								} else {
 								    callback.receiveErrorfetchTargetData(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorfetchTargetData(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[2].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[2].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                  
                 
                 /**
                   * Auto generated method signature
                   * 
                   */
                 public void  getMapping(
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping getMapping6
 
                 ) throws java.rmi.RemoteException
                 
                 
                 {
                 org.apache.axis2.context.MessageContext _messageContext = null;
 
                 
                 org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[3].getName());
                 _operationClient.getOptions().setAction("urn:getMapping");
                 _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
                 
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
                 org.apache.axiom.soap.SOAPEnvelope env = null;
                  _messageContext = new org.apache.axis2.context.MessageContext();
 
                 
                                                     //Style is Doc.
                                                     
                                                                     
                                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                     getMapping6,
                                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "getMapping")),new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "getMapping"));
                                                                 
 
               //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
                 // create message context with that soap envelope
 
             _messageContext.setEnvelope(env);
 
             // add the message contxt to the operation client
             _operationClient.addMessageContext(_messageContext);
 
              _operationClient.execute(true);
 
            
               if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
               }
            
              return;
            }
             
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startSession
                      * @param startSession7
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse startSession(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession startSession7)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
               _operationClient.getOptions().setAction("urn:startSession");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     startSession7,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "startSession")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "startSession"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startstartSession
                     * @param startSession7
                 
                 */
                 public  void startstartSession(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession startSession7,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[4].getName());
              _operationClient.getOptions().setAction("urn:startSession");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     startSession7,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "startSession")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "startSession"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultstartSession(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorstartSession(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"startSession"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorstartSession(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorstartSession(f);
                                             }
 									    } else {
 										    callback.receiveErrorstartSession(f);
 									    }
 									} else {
 									    callback.receiveErrorstartSession(f);
 									}
 								} else {
 								    callback.receiveErrorstartSession(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorstartSession(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[4].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[4].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                 
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#getMetricAdvice
                      * @param getMetricAdvice9
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse getMetricAdvice(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice getMetricAdvice9)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
               _operationClient.getOptions().setAction("urn:getMetricAdvice");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     getMetricAdvice9,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "getMetricAdvice")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "getMetricAdvice"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startgetMetricAdvice
                     * @param getMetricAdvice9
                 
                 */
                 public  void startgetMetricAdvice(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice getMetricAdvice9,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[5].getName());
              _operationClient.getOptions().setAction("urn:getMetricAdvice");
   //TODO check if it work
              _operationClient.getOptions().setUseSeparateListener(true);
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     getMetricAdvice9,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "getMetricAdvice")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "getMetricAdvice"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                             	
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultgetMetricAdvice(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorgetMetricAdvice(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"getMetricAdvice"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorgetMetricAdvice(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorgetMetricAdvice(f);
                                             }
 									    } else {
 										    callback.receiveErrorgetMetricAdvice(f);
 									    }
 									} else {
 									    callback.receiveErrorgetMetricAdvice(f);
 									}
 								} else {
 								    callback.receiveErrorgetMetricAdvice(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorgetMetricAdvice(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[5].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[5].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                 
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#polling
                      * @param polling11
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse polling(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling polling11)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
               _operationClient.getOptions().setAction("urn:polling");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     polling11,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "polling")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "polling"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startpolling
                     * @param polling11
                 
                 */
                 public  void startpolling(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling polling11,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[6].getName());
              _operationClient.getOptions().setAction("urn:polling");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     polling11,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "polling")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "polling"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultpolling(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorpolling(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"polling"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorpolling(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorpolling(f);
                                             }
 									    } else {
 										    callback.receiveErrorpolling(f);
 									    }
 									} else {
 									    callback.receiveErrorpolling(f);
 									}
 								} else {
 								    callback.receiveErrorpolling(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorpolling(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[6].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[6].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                 
                     /**
                      * Auto generated method signature
                      * 
                      * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#continueSession
                      * @param continueSession13
                     
                      */
 
                     
 
                             public  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse continueSession(
 
                             de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession continueSession13)
                         
 
                     throws java.rmi.RemoteException
                     
                     {
               org.apache.axis2.context.MessageContext _messageContext = null;
               try{
                org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
               _operationClient.getOptions().setAction("urn:continueSession");
               _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
               // create a message context
               _messageContext = new org.apache.axis2.context.MessageContext();
 
               
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env = null;
                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     continueSession13,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "continueSession")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "continueSession"));
                                                 
         //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // set the message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message contxt to the operation client
         _operationClient.addMessageContext(_messageContext);
 
         //execute the operation client
         _operationClient.execute(true);
 
          
                org.apache.axis2.context.MessageContext _returnMessageContext = _operationClient.getMessageContext(
                                            org.apache.axis2.wsdl.WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                 org.apache.axiom.soap.SOAPEnvelope _returnEnv = _returnMessageContext.getEnvelope();
                 
                 
                                 java.lang.Object object = fromOM(
                                              _returnEnv.getBody().getFirstElement() ,
                                              de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse.class,
                                               getEnvelopeNamespaces(_returnEnv));
 
                                
                                         return (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse)object;
                                    
          }catch(org.apache.axis2.AxisFault f){
 
             org.apache.axiom.om.OMElement faultElt = f.getDetail();
             if (faultElt!=null){
                 if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"))){
                     //make the fault by reflection
                     try{
                         java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"));
                         java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
                         java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                         java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
                         //message class
                         java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"));
                         java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
                         java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
                         java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
                                    new java.lang.Class[]{messageClass});
                         m.invoke(ex,new java.lang.Object[]{messageObject});
                         
 
                         throw new java.rmi.RemoteException(ex.getMessage(), ex);
                     }catch(java.lang.ClassCastException e){
                        // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.ClassNotFoundException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }catch (java.lang.NoSuchMethodException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     } catch (java.lang.reflect.InvocationTargetException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }  catch (java.lang.IllegalAccessException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }   catch (java.lang.InstantiationException e) {
                         // we cannot intantiate the class - throw the original Axis fault
                         throw f;
                     }
                 }else{
                     throw f;
                 }
             }else{
                 throw f;
             }
             } finally {
                 if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                 }
             }
         }
             
                 /**
                 * Auto generated method signature for Asynchronous Invocations
                 * 
                 * @see de.uni_leipzig.simba.limeswebservice.server.LimesServiceImpl#startcontinueSession
                     * @param continueSession13
                 
                 */
                 public  void startcontinueSession(
 
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession continueSession13,
 
                   final de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplCallbackHandler callback)
 
                 throws java.rmi.RemoteException{
 
               org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[7].getName());
              _operationClient.getOptions().setAction("urn:continueSession");
              _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
               
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
 
 
               // create SOAP envelope with that payload
               org.apache.axiom.soap.SOAPEnvelope env=null;
               final org.apache.axis2.context.MessageContext _messageContext = new org.apache.axis2.context.MessageContext();
 
                     
                                     //Style is Doc.
                                     
                                                     
                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                     continueSession13,
                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "continueSession")), new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                     "continueSession"));
                                                 
         // adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
         // create message context with that soap envelope
         _messageContext.setEnvelope(env);
 
         // add the message context to the operation client
         _operationClient.addMessageContext(_messageContext);
 
 
                     
                         _operationClient.setCallback(new org.apache.axis2.client.async.AxisCallback() {
                             public void onMessage(org.apache.axis2.context.MessageContext resultContext) {
                             try {
                                 org.apache.axiom.soap.SOAPEnvelope resultEnv = resultContext.getEnvelope();
                                 
                                         java.lang.Object object = fromOM(resultEnv.getBody().getFirstElement(),
                                                                          de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse.class,
                                                                          getEnvelopeNamespaces(resultEnv));
                                         callback.receiveResultcontinueSession(
                                         (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse)object);
                                         
                             } catch (org.apache.axis2.AxisFault e) {
                                 callback.receiveErrorcontinueSession(e);
                             }
                             }
 
                             public void onError(java.lang.Exception error) {
 								if (error instanceof org.apache.axis2.AxisFault) {
 									org.apache.axis2.AxisFault f = (org.apache.axis2.AxisFault) error;
 									org.apache.axiom.om.OMElement faultElt = f.getDetail();
 									if (faultElt!=null){
 										if (faultExceptionNameMap.containsKey(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"))){
 											//make the fault by reflection
 											try{
 													java.lang.String exceptionClassName = (java.lang.String)faultExceptionClassNameMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"));
 													java.lang.Class exceptionClass = java.lang.Class.forName(exceptionClassName);
 													java.lang.reflect.Constructor constructor = exceptionClass.getConstructor(String.class);
                                                     java.lang.Exception ex = (java.lang.Exception) constructor.newInstance(f.getMessage());
 													//message class
 													java.lang.String messageClassName = (java.lang.String)faultMessageMap.get(new org.apache.axis2.client.FaultMapKey(faultElt.getQName(),"continueSession"));
 														java.lang.Class messageClass = java.lang.Class.forName(messageClassName);
 													java.lang.Object messageObject = fromOM(faultElt,messageClass,null);
 													java.lang.reflect.Method m = exceptionClass.getMethod("setFaultMessage",
 															new java.lang.Class[]{messageClass});
 													m.invoke(ex,new java.lang.Object[]{messageObject});
 													
 					
 										            callback.receiveErrorcontinueSession(new java.rmi.RemoteException(ex.getMessage(), ex));
                                             } catch(java.lang.ClassCastException e){
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (java.lang.ClassNotFoundException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (java.lang.NoSuchMethodException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (java.lang.reflect.InvocationTargetException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (java.lang.IllegalAccessException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (java.lang.InstantiationException e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             } catch (org.apache.axis2.AxisFault e) {
                                                 // we cannot intantiate the class - throw the original Axis fault
                                                 callback.receiveErrorcontinueSession(f);
                                             }
 									    } else {
 										    callback.receiveErrorcontinueSession(f);
 									    }
 									} else {
 									    callback.receiveErrorcontinueSession(f);
 									}
 								} else {
 								    callback.receiveErrorcontinueSession(error);
 								}
                             }
 
                             public void onFault(org.apache.axis2.context.MessageContext faultContext) {
                                 org.apache.axis2.AxisFault fault = org.apache.axis2.util.Utils.getInboundFaultFromMessageContext(faultContext);
                                 onError(fault);
                             }
 
                             public void onComplete() {
                                 try {
                                     _messageContext.getTransportOut().getSender().cleanup(_messageContext);
                                 } catch (org.apache.axis2.AxisFault axisFault) {
                                     callback.receiveErrorcontinueSession(axisFault);
                                 }
                             }
                 });
                         
 
           org.apache.axis2.util.CallbackReceiver _callbackReceiver = null;
         if ( _operations[7].getMessageReceiver()==null &&  _operationClient.getOptions().isUseSeparateListener()) {
            _callbackReceiver = new org.apache.axis2.util.CallbackReceiver();
           _operations[7].setMessageReceiver(
                     _callbackReceiver);
         }
 
            //execute the operation client
            _operationClient.execute(false);
 
                     }
                  
                 
                 /**
                   * Auto generated method signature
                   * 
                   */
                 public void  setMetricSpec(
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec setMetricSpec15
 
                 ) throws java.rmi.RemoteException
                 
                 
                 {
                 org.apache.axis2.context.MessageContext _messageContext = null;
 
                 
                 org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[8].getName());
                 _operationClient.getOptions().setAction("urn:setMetricSpec");
                 _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
                 
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
                 org.apache.axiom.soap.SOAPEnvelope env = null;
                  _messageContext = new org.apache.axis2.context.MessageContext();
 
                 
                                                     //Style is Doc.
                                                     
                                                                     
                                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                     setMetricSpec15,
                                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "setMetricSpec")),new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "setMetricSpec"));
                                                                 
 
               //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
                 // create message context with that soap envelope
 
             _messageContext.setEnvelope(env);
 
             // add the message contxt to the operation client
             _operationClient.addMessageContext(_messageContext);
 
              _operationClient.execute(true);
 
            
               if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
               }
            
              return;
            }
              
                 
                 /**
                   * Auto generated method signature
                   * 
                   */
                 public void  setSpecification(
                  de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification setSpecification16
 
                 ) throws java.rmi.RemoteException
                 
                 
                 {
                 org.apache.axis2.context.MessageContext _messageContext = null;
 
                 
                 org.apache.axis2.client.OperationClient _operationClient = _serviceClient.createClient(_operations[9].getName());
                 _operationClient.getOptions().setAction("urn:setSpecification");
                 _operationClient.getOptions().setExceptionToBeThrownOnSOAPFault(true);
 
                 
               
                   addPropertyToOperationClient(_operationClient,org.apache.axis2.description.WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR,"&");
               
                 org.apache.axiom.soap.SOAPEnvelope env = null;
                  _messageContext = new org.apache.axis2.context.MessageContext();
 
                 
                                                     //Style is Doc.
                                                     
                                                                     
                                                                     env = toEnvelope(getFactory(_operationClient.getOptions().getSoapVersionURI()),
                                                                     setSpecification16,
                                                                     optimizeContent(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "setSpecification")),new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                     "setSpecification"));
                                                                 
 
               //adding SOAP soap_headers
          _serviceClient.addHeadersToEnvelope(env);
                 // create message context with that soap envelope
 
             _messageContext.setEnvelope(env);
 
             // add the message contxt to the operation client
             _operationClient.addMessageContext(_messageContext);
 
              _operationClient.execute(true);
 
            
               if (_messageContext.getTransportOut() != null) {
                       _messageContext.getTransportOut().getSender().cleanup(_messageContext);
               }
            
              return;
            }
             
 
 
        /**
         *  A utility method that copies the namepaces from the SOAPEnvelope
         */
        private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env){
         java.util.Map returnMap = new java.util.HashMap();
         java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
         while (namespaceIterator.hasNext()) {
             org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
             returnMap.put(ns.getPrefix(),ns.getNamespaceURI());
         }
        return returnMap;
     }
 
     
     
     private javax.xml.namespace.QName[] opNameArray = null;
     private boolean optimizeContent(javax.xml.namespace.QName opName) {
         
 
         if (opNameArray == null) {
             return false;
         }
         for (int i = 0; i < opNameArray.length; i++) {
             if (opName.equals(opNameArray[i])) {
                 return true;   
             }
         }
         return false;
     }
      //http://localhost:8080/LimesWebService/services/LimesServiceImpl.LimesServiceImplHttpSoap12Endpoint/
         public static class PollingResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "pollingResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected int local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(int param){
                             
                                        // setting primitive attribute tracker to true
                                        local_returnTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":pollingResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "pollingResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
                                                if (local_return==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("return cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static PollingResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             PollingResponse object =
                 new PollingResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"pollingResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (PollingResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"return" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.set_return(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchTargetDataResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchTargetDataResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected java.lang.String local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(java.lang.String param){
                             local_returnTracker = true;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchTargetDataResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchTargetDataResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
 
                                           if (local_return==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(local_return);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                          elementList.add(local_return==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchTargetDataResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchTargetDataResponse object =
                 new FetchTargetDataResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchTargetDataResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchTargetDataResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class SetMetricSpec
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "setMetricSpec",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
                         /**
                         * field for MetricMap
                         */
 
                         
                                     protected java.lang.String localMetricMap ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localMetricMapTracker = false ;
 
                            public boolean isMetricMapSpecified(){
                                return localMetricMapTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String getMetricMap(){
                                return localMetricMap;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param MetricMap
                                */
                                public void setMetricMap(java.lang.String param){
                             localMetricMapTracker = true;
                                    
                                             this.localMetricMap=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":setMetricSpec",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "setMetricSpec",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              } if (localMetricMapTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "metricMap", xmlWriter);
                              
 
                                           if (localMetricMap==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(localMetricMap);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             } if (localMetricMapTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "metricMap"));
                                  
                                          elementList.add(localMetricMap==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMetricMap));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static SetMetricSpec parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             SetMetricSpec object =
                 new SetMetricSpec();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"setMetricSpec".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (SetMetricSpec)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","metricMap").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setMetricMap(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class GetMetricAdviceResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "getMetricAdviceResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected java.lang.String local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(java.lang.String param){
                             local_returnTracker = true;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":getMetricAdviceResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "getMetricAdviceResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
 
                                           if (local_return==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(local_return);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                          elementList.add(local_return==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static GetMetricAdviceResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             GetMetricAdviceResponse object =
                 new GetMetricAdviceResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"getMetricAdviceResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (GetMetricAdviceResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class StartSession
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "startSession",
                 "ns1");
 
             
 
                         /**
                         * field for EmailAddress
                         */
 
                         
                                     protected java.lang.String localEmailAddress ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localEmailAddressTracker = false ;
 
                            public boolean isEmailAddressSpecified(){
                                return localEmailAddressTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String getEmailAddress(){
                                return localEmailAddress;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param EmailAddress
                                */
                                public void setEmailAddress(java.lang.String param){
                             localEmailAddressTracker = true;
                                    
                                             this.localEmailAddress=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":startSession",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "startSession",
                            xmlWriter);
                    }
 
                
                    }
                 if (localEmailAddressTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "emailAddress", xmlWriter);
                              
 
                                           if (localEmailAddress==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(localEmailAddress);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localEmailAddressTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "emailAddress"));
                                  
                                          elementList.add(localEmailAddress==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localEmailAddress));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static StartSession parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             StartSession object =
                 new StartSession();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"startSession".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (StartSession)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","emailAddress").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setEmailAddress(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchMetricMapResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchMetricMapResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected java.lang.String local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(java.lang.String param){
                             local_returnTracker = true;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchMetricMapResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchMetricMapResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
 
                                           if (local_return==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(local_return);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                          elementList.add(local_return==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchMetricMapResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchMetricMapResponse object =
                 new FetchMetricMapResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchMetricMapResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchMetricMapResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchMetricMap
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchMetricMap",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchMetricMap",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchMetricMap",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchMetricMap parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchMetricMap object =
                 new FetchMetricMap();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchMetricMap".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchMetricMap)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class GetMetricAdvice
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "getMetricAdvice",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":getMetricAdvice",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "getMetricAdvice",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static GetMetricAdvice parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             GetMetricAdvice object =
                 new GetMetricAdvice();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"getMetricAdvice".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (GetMetricAdvice)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class GetMapping
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "getMapping",
                 "ns1");
 
             
 
                         
                             
 
                       
                             
 
                       
                             
 
                         /**
                         * field for sessionId
                         */
 
                         
                                     protected int localMailAddress ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localMailAddressTracker = false ;
 
                            public boolean isMailAddressSpecified(){
                                return localMailAddressTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  int getMailAddress(){
                                return localMailAddress;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param MailAddress
                                */
                                public void setMailAddress(int param){
                             localMailAddressTracker = true;
                                    
                                             this.localMailAddress=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":getMapping",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "getMapping",
                            xmlWriter);
                    }
 
                
                    }
                  if (localMailAddressTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "mailAddress", xmlWriter);
                              
 
                                           if (localMailAddress==Integer.MIN_VALUE){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMailAddress));
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 if (localMailAddressTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "mailAddress"));
                                  
                                          elementList.add(localMailAddress==Integer.MIN_VALUE?Integer.MIN_VALUE:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localMailAddress));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static GetMapping parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             GetMapping object =
                 new GetMapping();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"getMapping".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (GetMapping)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                    
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","mailAddress").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setMailAddress(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class StartSessionResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "startSessionResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected int local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(int param){
                             
                                        // setting primitive attribute tracker to true
                                        local_returnTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":startSessionResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "startSessionResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
                                                if (local_return==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("return cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static StartSessionResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             StartSessionResponse object =
                 new StartSessionResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"startSessionResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (StartSessionResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"return" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.set_return(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchSourceData
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchSourceData",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchSourceData",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchSourceData",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchSourceData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchSourceData object =
                 new FetchSourceData();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchSourceData".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchSourceData)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class Polling
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "polling",
                 "ns1");
 
             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":polling",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "polling",
                            xmlWriter);
                    }
 
                
                    }
                
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                 
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static Polling parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             Polling object =
                 new Polling();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"polling".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (Polling)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class ContinueSession
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "continueSession",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":continueSession",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "continueSession",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static ContinueSession parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             ContinueSession object =
                 new ContinueSession();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"continueSession".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (ContinueSession)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchSourceDataResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchSourceDataResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected java.lang.String local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(java.lang.String param){
                             local_returnTracker = true;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchSourceDataResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchSourceDataResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
 
                                           if (local_return==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(local_return);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                          elementList.add(local_return==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchSourceDataResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchSourceDataResponse object =
                 new FetchSourceDataResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchSourceDataResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchSourceDataResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class ExtensionMapper{
 
           public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                        java.lang.String typeName,
                                                        javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
 
               
              throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
           }
 
         }
     
         public static class ContinueSessionResponse
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "continueSessionResponse",
                 "ns1");
 
             
 
                         /**
                         * field for _return
                         */
 
                         
                                     protected int local_return ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean local_returnTracker = false ;
 
                            public boolean is_returnSpecified(){
                                return local_returnTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int get_return(){
                                return local_return;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param _return
                                */
                                public void set_return(int param){
                             
                                        // setting primitive attribute tracker to true
                                        local_returnTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.local_return=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":continueSessionResponse",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "continueSessionResponse",
                            xmlWriter);
                    }
 
                
                    }
                 if (local_returnTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "return", xmlWriter);
                              
                                                if (local_return==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("return cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (local_returnTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "return"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(local_return));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static ContinueSessionResponse parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             ContinueSessionResponse object =
                 new ContinueSessionResponse();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"continueSessionResponse".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (ContinueSessionResponse)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","return").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"return" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.set_return(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.set_return(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class FetchTargetData
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "fetchTargetData",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":fetchTargetData",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "fetchTargetData",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static FetchTargetData parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             FetchTargetData object =
                 new FetchTargetData();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"fetchTargetData".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (FetchTargetData)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
         public static class SetSpecification
         implements org.apache.axis2.databinding.ADBBean{
         
                 public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                 "http://server.limeswebservice.simba.uni_leipzig.de",
                 "setSpecification",
                 "ns1");
 
             
 
                         /**
                         * field for SessionId
                         */
 
                         
                                     protected int localSessionId ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSessionIdTracker = false ;
 
                            public boolean isSessionIdSpecified(){
                                return localSessionIdTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return int
                            */
                            public  int getSessionId(){
                                return localSessionId;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param SessionId
                                */
                                public void setSessionId(int param){
                             
                                        // setting primitive attribute tracker to true
                                        localSessionIdTracker =
                                        param != java.lang.Integer.MIN_VALUE;
                                    
                                             this.localSessionId=param;
                                     
 
                                }
                             
 
                         /**
                         * field for Source
                         */
 
                         
                                     protected java.lang.String localSource ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localSourceTracker = false ;
 
                            public boolean isSourceSpecified(){
                                return localSourceTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String getSource(){
                                return localSource;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param Source
                                */
                                public void setSource(java.lang.String param){
                             localSourceTracker = true;
                                    
                                             this.localSource=param;
                                     
 
                                }
                             
 
                         /**
                         * field for Target
                         */
 
                         
                                     protected java.lang.String localTarget ;
                                 
                            /*  This tracker boolean wil be used to detect whether the user called the set method
                           *   for this attribute. It will be used to determine whether to include this field
                            *   in the serialized XML
                            */
                            protected boolean localTargetTracker = false ;
 
                            public boolean isTargetSpecified(){
                                return localTargetTracker;
                            }
 
                            
 
                            /**
                            * Auto generated getter method
                            * @return java.lang.String
                            */
                            public  java.lang.String getTarget(){
                                return localTarget;
                            }
 
                            
                         
                             /**
                                * Auto generated setter method
                                * @param param Target
                                */
                                public void setTarget(java.lang.String param){
                             localTargetTracker = true;
                                    
                                             this.localTarget=param;
                                     
 
                                }
                             
 
      
      
         /**
         *
         * @param parentQName
         * @param factory
         * @return org.apache.axiom.om.OMElement
         */
        public org.apache.axiom.om.OMElement getOMElement (
                final javax.xml.namespace.QName parentQName,
                final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{
 
 
         
                org.apache.axiom.om.OMDataSource dataSource =
                        new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
                return factory.createOMElement(dataSource,MY_QNAME);
             
         }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                        javax.xml.stream.XMLStreamWriter xmlWriter)
                                 throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                            serialize(parentQName,xmlWriter,false);
          }
 
          public void serialize(final javax.xml.namespace.QName parentQName,
                                javax.xml.stream.XMLStreamWriter xmlWriter,
                                boolean serializeType)
             throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
             
                 
 
 
                 java.lang.String prefix = null;
                 java.lang.String namespace = null;
                 
 
                     prefix = parentQName.getPrefix();
                     namespace = parentQName.getNamespaceURI();
                     writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                 
                   if (serializeType){
                
 
                    java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://server.limeswebservice.simba.uni_leipzig.de");
                    if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            namespacePrefix+":setSpecification",
                            xmlWriter);
                    } else {
                        writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                            "setSpecification",
                            xmlWriter);
                    }
 
                
                    }
                 if (localSessionIdTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "sessionId", xmlWriter);
                              
                                                if (localSessionId==java.lang.Integer.MIN_VALUE) {
                                            
                                                          throw new org.apache.axis2.databinding.ADBException("sessionId cannot be null!!");
                                                       
                                                } else {
                                                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                                                }
                                     
                                    xmlWriter.writeEndElement();
                              } if (localSourceTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "source", xmlWriter);
                              
 
                                           if (localSource==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(localSource);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              } if (localTargetTracker){
                                     namespace = "http://server.limeswebservice.simba.uni_leipzig.de";
                                     writeStartElement(null, namespace, "target", xmlWriter);
                              
 
                                           if (localTarget==null){
                                               // write the nil attribute
                                               
                                                      writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","nil","1",xmlWriter);
                                                   
                                           }else{
 
                                         
                                                    xmlWriter.writeCharacters(localTarget);
                                             
                                           }
                                     
                                    xmlWriter.writeEndElement();
                              }
                     xmlWriter.writeEndElement();
                
 
         }
 
         private static java.lang.String generatePrefix(java.lang.String namespace) {
             if(namespace.equals("http://server.limeswebservice.simba.uni_leipzig.de")){
                 return "ns1";
             }
             return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
         }
 
         /**
          * Utility method to write an element start tag.
          */
         private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                        javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
             if (writerPrefix != null) {
                 xmlWriter.writeStartElement(namespace, localPart);
             } else {
                 if (namespace.length() == 0) {
                     prefix = "";
                 } else if (prefix == null) {
                     prefix = generatePrefix(namespace);
                 }
 
                 xmlWriter.writeStartElement(prefix, localPart, namespace);
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
         }
         
         /**
          * Util method to write an attribute with the ns prefix
          */
         private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (xmlWriter.getPrefix(namespace) == null) {
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             xmlWriter.writeAttribute(namespace,attName,attValue);
         }
 
         /**
          * Util method to write an attribute without the ns prefix
          */
         private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                     java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
             if (namespace.equals("")) {
                 xmlWriter.writeAttribute(attName,attValue);
             } else {
                 registerPrefix(xmlWriter, namespace);
                 xmlWriter.writeAttribute(namespace,attName,attValue);
             }
         }
 
 
            /**
              * Util method to write an attribute without the ns prefix
              */
             private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                              javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
                 java.lang.String attributeNamespace = qname.getNamespaceURI();
                 java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                 if (attributePrefix == null) {
                     attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                 }
                 java.lang.String attributeValue;
                 if (attributePrefix.trim().length() > 0) {
                     attributeValue = attributePrefix + ":" + qname.getLocalPart();
                 } else {
                     attributeValue = qname.getLocalPart();
                 }
 
                 if (namespace.equals("")) {
                     xmlWriter.writeAttribute(attName, attributeValue);
                 } else {
                     registerPrefix(xmlWriter, namespace);
                     xmlWriter.writeAttribute(namespace, attName, attributeValue);
                 }
             }
         /**
          *  method to handle Qnames
          */
 
         private void writeQName(javax.xml.namespace.QName qname,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
             java.lang.String namespaceURI = qname.getNamespaceURI();
             if (namespaceURI != null) {
                 java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                 if (prefix == null) {
                     prefix = generatePrefix(namespaceURI);
                     xmlWriter.writeNamespace(prefix, namespaceURI);
                     xmlWriter.setPrefix(prefix,namespaceURI);
                 }
 
                 if (prefix.trim().length() > 0){
                     xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 } else {
                     // i.e this is the default namespace
                     xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                 }
 
             } else {
                 xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
             }
         }
 
         private void writeQNames(javax.xml.namespace.QName[] qnames,
                                  javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
 
             if (qnames != null) {
                 // we have to store this data until last moment since it is not possible to write any
                 // namespace data after writing the charactor data
                 java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                 java.lang.String namespaceURI = null;
                 java.lang.String prefix = null;
 
                 for (int i = 0; i < qnames.length; i++) {
                     if (i > 0) {
                         stringToWrite.append(" ");
                     }
                     namespaceURI = qnames[i].getNamespaceURI();
                     if (namespaceURI != null) {
                         prefix = xmlWriter.getPrefix(namespaceURI);
                         if ((prefix == null) || (prefix.length() == 0)) {
                             prefix = generatePrefix(namespaceURI);
                             xmlWriter.writeNamespace(prefix, namespaceURI);
                             xmlWriter.setPrefix(prefix,namespaceURI);
                         }
 
                         if (prefix.trim().length() > 0){
                             stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         } else {
                             stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                         }
                     } else {
                         stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                     }
                 }
                 xmlWriter.writeCharacters(stringToWrite.toString());
             }
 
         }
 
 
         /**
          * Register a namespace prefix
          */
         private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
             java.lang.String prefix = xmlWriter.getPrefix(namespace);
             if (prefix == null) {
                 prefix = generatePrefix(namespace);
                 javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                 while (true) {
                     java.lang.String uri = nsContext.getNamespaceURI(prefix);
                     if (uri == null || uri.length() == 0) {
                         break;
                     }
                     prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                 }
                 xmlWriter.writeNamespace(prefix, namespace);
                 xmlWriter.setPrefix(prefix, namespace);
             }
             return prefix;
         }
 
 
   
         /**
         * databinding method to get an XML representation of this object
         *
         */
         public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                     throws org.apache.axis2.databinding.ADBException{
 
 
         
                  java.util.ArrayList elementList = new java.util.ArrayList();
                  java.util.ArrayList attribList = new java.util.ArrayList();
 
                  if (localSessionIdTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "sessionId"));
                                  
                                 elementList.add(
                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSessionId));
                             } if (localSourceTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "source"));
                                  
                                          elementList.add(localSource==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSource));
                                     } if (localTargetTracker){
                                       elementList.add(new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de",
                                                                       "target"));
                                  
                                          elementList.add(localTarget==null?null:
                                          org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localTarget));
                                     }
 
                 return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
             
             
 
         }
 
   
 
      /**
       *  Factory class that keeps the parse method
       */
     public static class Factory{
 
         
         
 
         /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         *                If this object is a complex type, the reader is positioned at the end element of its outer element
         */
         public static SetSpecification parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
             SetSpecification object =
                 new SetSpecification();
 
             int event;
             java.lang.String nillableValue = null;
             java.lang.String prefix ="";
             java.lang.String namespaceuri ="";
             try {
                 
                 while (!reader.isStartElement() && !reader.isEndElement())
                     reader.next();
 
                 
                 if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                   java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                         "type");
                   if (fullTypeName!=null){
                     java.lang.String nsPrefix = null;
                     if (fullTypeName.indexOf(":") > -1){
                         nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                     }
                     nsPrefix = nsPrefix==null?"":nsPrefix;
 
                     java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                     
                             if (!"setSpecification".equals(type)){
                                 //find namespace for the prefix
                                 java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                 return (SetSpecification)ExtensionMapper.getTypeObject(
                                      nsUri,type,reader);
                               }
                         
 
                   }
                 
 
                 }
 
                 
 
                 
                 // Note all attributes that were handled. Used to differ normal attributes
                 // from anyAttributes.
                 java.util.Vector handledAttributes = new java.util.Vector();
                 
 
                 
                     
                     reader.next();
                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","sessionId").equals(reader.getName())){
                                 
                                     nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                     if ("true".equals(nillableValue) || "1".equals(nillableValue)){
                                         throw new org.apache.axis2.databinding.ADBException("The element: "+"sessionId" +"  cannot be null");
                                     }
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSessionId(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToInt(content));
                                               
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                                object.setSessionId(java.lang.Integer.MIN_VALUE);
                                            
                                     }
                                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","source").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setSource(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                 
                                     
                                     while (!reader.isStartElement() && !reader.isEndElement()) reader.next();
                                 
                                     if (reader.isStartElement() && new javax.xml.namespace.QName("http://server.limeswebservice.simba.uni_leipzig.de","target").equals(reader.getName())){
                                 
                                        nillableValue = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","nil");
                                        if (!"true".equals(nillableValue) && !"1".equals(nillableValue)){
                                     
 
                                     java.lang.String content = reader.getElementText();
                                     
                                               object.setTarget(
                                                     org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));
                                             
                                        } else {
                                            
                                            
                                            reader.getElementText(); // throw away text nodes if any.
                                        }
                                       
                                         reader.next();
                                     
                               }  // End of if for expected property start element
                                 
                                     else {
                                         
                                     }
                                   
                             while (!reader.isStartElement() && !reader.isEndElement())
                                 reader.next();
                             
                                 if (reader.isStartElement())
                                 // A start element we are not expecting indicates a trailing invalid property
                                 throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getName());
                             
 
 
 
             } catch (javax.xml.stream.XMLStreamException e) {
                 throw new java.lang.Exception(e);
             }
 
             return object;
         }
 
         }//end of factory class
 
         
 
         }
            
     
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
             private  org.apache.axiom.om.OMElement  toOM(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification param, boolean optimizeContent)
             throws org.apache.axis2.AxisFault {
 
             
                         try{
                              return param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification.MY_QNAME,
                                           org.apache.axiom.om.OMAbstractFactory.getOMFactory());
                         } catch(org.apache.axis2.databinding.ADBException e){
                             throw org.apache.axis2.AxisFault.makeFault(e);
                         }
                     
 
             }
         
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
                                     
                                         private  org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification param, boolean optimizeContent, javax.xml.namespace.QName methodQName)
                                         throws org.apache.axis2.AxisFault{
 
                                              
                                                     try{
 
                                                             org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();
                                                             emptyEnvelope.getBody().addChild(param.getOMElement(de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification.MY_QNAME,factory));
                                                             return emptyEnvelope;
                                                         } catch(org.apache.axis2.databinding.ADBException e){
                                                             throw org.apache.axis2.AxisFault.makeFault(e);
                                                         }
                                                 
 
                                         }
                                 
                              
                              /* methods to provide back word compatibility */
 
                              
 
 
         /**
         *  get the default envelope
         */
         private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory){
         return factory.getDefaultEnvelope();
         }
 
 
         private  java.lang.Object fromOM(
         org.apache.axiom.om.OMElement param,
         java.lang.Class type,
         java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault{
 
         try {
         
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMap.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchMetricMapResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchSourceDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetData.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.FetchTargetDataResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMapping.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSession.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.StartSessionResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdvice.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.GetMetricAdviceResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.Polling.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.PollingResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSession.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.ContinueSessionResponse.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetMetricSpec.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
                 if (de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification.class.equals(type)){
                 
                            return de.uni_leipzig.simba.limeswebservice.server.LimesServiceImplStub.SetSpecification.Factory.parse(param.getXMLStreamReaderWithoutCaching());
                     
 
                 }
            
         } catch (java.lang.Exception e) {
         throw org.apache.axis2.AxisFault.makeFault(e);
         }
            return null;
         }
 
 
 
     
    }
    
