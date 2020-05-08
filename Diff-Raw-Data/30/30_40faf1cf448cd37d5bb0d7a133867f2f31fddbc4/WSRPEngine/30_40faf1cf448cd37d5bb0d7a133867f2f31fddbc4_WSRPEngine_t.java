 /*
  * Copyright 2000-2001,2004 The Apache Software Foundation.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.wsrp4j.producer.driver;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 
 import oasis.names.tc.wsrp.v1.types.BlockingInteractionResponse;
 import oasis.names.tc.wsrp.v1.types.ClonePortlet;
 import oasis.names.tc.wsrp.v1.types.DestroyFailed;
 import oasis.names.tc.wsrp.v1.types.DestroyPortlets;
 import oasis.names.tc.wsrp.v1.types.DestroyPortletsResponse;
 import oasis.names.tc.wsrp.v1.types.GetMarkup;
 import oasis.names.tc.wsrp.v1.types.GetPortletDescription;
 import oasis.names.tc.wsrp.v1.types.GetPortletProperties;
 import oasis.names.tc.wsrp.v1.types.GetPortletPropertyDescription;
 import oasis.names.tc.wsrp.v1.types.GetServiceDescription;
 import oasis.names.tc.wsrp.v1.types.InitCookie;
 import oasis.names.tc.wsrp.v1.types.MarkupParams;
 import oasis.names.tc.wsrp.v1.types.MarkupResponse;
 import oasis.names.tc.wsrp.v1.types.MarkupType;
 import oasis.names.tc.wsrp.v1.types.MissingParametersFault;
 import oasis.names.tc.wsrp.v1.types.ModifyRegistration;
 import oasis.names.tc.wsrp.v1.types.PerformBlockingInteraction;
 import oasis.names.tc.wsrp.v1.types.PortletContext;
 import oasis.names.tc.wsrp.v1.types.PortletDescription;
 import oasis.names.tc.wsrp.v1.types.PortletDescriptionResponse;
 import oasis.names.tc.wsrp.v1.types.PortletPropertyDescriptionResponse;
 import oasis.names.tc.wsrp.v1.types.PropertyList;
 import oasis.names.tc.wsrp.v1.types.RegistrationContext;
 import oasis.names.tc.wsrp.v1.types.RegistrationData;
 import oasis.names.tc.wsrp.v1.types.RegistrationState;
 import oasis.names.tc.wsrp.v1.types.ReleaseSessions;
 import oasis.names.tc.wsrp.v1.types.ReturnAny;
 import oasis.names.tc.wsrp.v1.types.ServiceDescription;
 import oasis.names.tc.wsrp.v1.types.SetPortletProperties;
 import oasis.names.tc.wsrp.v1.types.StateChange;
 
 import org.apache.wsrp4j.exception.ErrorCodes;
 import org.apache.wsrp4j.exception.WSRPException;
 import org.apache.wsrp4j.exception.WSRPXHelper;
 import org.apache.wsrp4j.log.LogManager;
 import org.apache.wsrp4j.log.Logger;
 import org.apache.wsrp4j.producer.ConsumerRegistry;
 import org.apache.wsrp4j.producer.ConsumerRegistryAccess;
 import org.apache.wsrp4j.producer.ProviderAccess;
 import org.apache.wsrp4j.producer.Registration;
 import org.apache.wsrp4j.producer.provider.ConsumerConfiguredPortlet;
 import org.apache.wsrp4j.producer.provider.DescriptionHandler;
 import org.apache.wsrp4j.producer.provider.Portlet;
 import org.apache.wsrp4j.producer.provider.PortletSessionManager;
 import org.apache.wsrp4j.producer.provider.PortletStateManager;
 import org.apache.wsrp4j.producer.provider.ProducerOfferedPortlet;
 import org.apache.wsrp4j.producer.provider.Provider;
 import org.apache.wsrp4j.util.Constants;
 import org.apache.wsrp4j.util.LocaleHelper;
 import org.apache.wsrp4j.util.ParameterChecker;
 
 
 /**
  * <p>Implements the WSRP-interfaces:
  * <ul>
  *   <li>Service Description Interface (required): Defines an operation for
  *   acquiring the Producer's metadata.</li>
  *   <li>Markup Interface (required): Defines operations for getting markup
  *   from a portlet as well as processing user interactions with that markup.
  *   Contains also the operation for Consumer assistance in pre-initializing
  *   HTTP-cookies.</li>
  *   <li>Registration Interface (optional): Defines operations for establishing,
  *   updating and destroying a regisration. Each registration reflects a particular
  *   relationship between a Consumer and a Producer.</li>
  *   <li>Portlet Management Interface (optional): Defines operations for getting
  *   portlet metadata, cloning portlets for further customization and interacting
  *   with the property interface.</li>
  * </li>
  * </p>
  * <p>All methods are implemented by calling the corresponding sub-components
  * (Producer- and Provider-side components). Additionally each method implementation
  * performs high level checks (e.g. a parameter validation to ensure that all required
  * input parameters are available) and maps internal exceptions to exceptions defined
  * vy WSRP.</p> 
  * <p>Involves additional servlet related objects -> no mapping of the interfaces
  * possible</p>
  */
 
 public class WSRPEngine
     implements
         oasis.names.tc.wsrp.v1.intf.WSRP_v1_PortletManagement_PortType,
         oasis.names.tc.wsrp.v1.intf.WSRP_v1_Markup_PortType,
         oasis.names.tc.wsrp.v1.intf.WSRP_v1_Registration_PortType,
         oasis.names.tc.wsrp.v1.intf.WSRP_v1_ServiceDescription_PortType
 {
 
     private boolean registrationRequired;
 
     // provider
     private Provider provider = null;
 
     // consumer registry
     private ConsumerRegistry consumerRegistry = null;
 
     //holds an instance of the WSRPEngine
     private static WSRPEngine instance = null;
 
     // helper instance for WSRP parameter validation
     private ParameterChecker paramCheck = new ParameterChecker();
 
     // log and trace support
     private Logger logger = LogManager.getLogManager().getLogger(this.getClass());
 
     private PortletSessionManager sessionManager;
     
     /**
      * Constructor reading the config from the properties file
      */
     private WSRPEngine() throws java.rmi.RemoteException
     {
         String MN = "Constructor";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         try
         {
             provider = ProviderAccess.getProvider();
             consumerRegistry = ConsumerRegistryAccess.getConsumerRegistry();
 
             registrationRequired = consumerRegistry.isRegistrationRequired();
             sessionManager = provider.getSessionManager();
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
     }
 
     /**
       Internal method.
       Returns an instance of the WSRPEngine
       @return WSRPEngine 
      */
     public synchronized static WSRPEngine getInstance() throws java.rmi.RemoteException
     {
         if (instance == null)
         {
             instance = new WSRPEngine();
         }
 
         return instance;
     }
 
     /**
      * Service Description Interface
      */
 
     /**
      * <p>Allows a Producer to provide information about its capabilities. Producers
      * may choose to restrict the information returned in ServiceDescription based on
      * the supplied RegistrationContext.</p>
      * <p>Checks registration (if required) by querying the ConsumerRegistry and calls
      * the DescriptionHandler to get the service description.</p> 
      *
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return ServiceDescription
      * 
      * @exception java.rmi.RemoteException
      */
     public ServiceDescription getServiceDescription(GetServiceDescription request) throws java.rmi.RemoteException
     {
 
         String MN = "getServiceDescription";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         ServiceDescription description = null;
 
         try
         {
             // perform parameter check
             paramCheck.check(request);
 
             DescriptionHandler descrHandler = provider.getDescriptionHandler();
             RegistrationContext regContext = request.getRegistrationContext();
 
             boolean registered = false;
 
             if (regContext != null)
             {
                 registered = consumerRegistry.check(regContext.getRegistrationHandle());
             }
 
             // check registration aspects, then fetch corresponding service description
             // TODO: also check if regHandle==null?
             if (!registrationRequired || ((regContext != null) && (registered)))
             {
                 description = descrHandler.getServiceDescription(regContext, request.getDesiredLocales());
             } else
             {
                 if (regContext == null && !registered)
                 {
 
                     description = descrHandler.getServiceDescription(null, request.getDesiredLocales());
                 } else
                 {
 
                     throw new WSRPException(ErrorCodes.INVALID_REGISTRATION);
                 }
             }
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         } catch (Throwable t)
         {
             t.printStackTrace();
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return description;
     }
 
     /**
      * Registration Interface
      */
 
     /**
      * Calls the ConsumerRegistry to register a certain consumer.
      * Returns a registration context including a new registration handle. 
      *
      * @param request a generated wrapper containing all input arguments for this method
      * 
      * @return RegistrationContext
      * 
      * @exception java.rmi.RemoteException
      */
     public RegistrationContext register(RegistrationData request) throws java.rmi.RemoteException
     {
 
         String MN = "register";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         RegistrationContext regContext = null;
 
         try
         {
 
             // perform parameter check
             paramCheck.check(request, Constants.NILLABLE_FALSE);
 
             // add consumer to consumer registry
             Registration consumerRegistration = consumerRegistry.register(request);
 
             // return registration context (containing a regHandle)
             regContext = consumerRegistration.getRegistrationContext();
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return regContext;
     }
 
     /**
      * Deregisters a certain consumer identified by a registration handle
      * within the ConsumerRegistry. Throws an InvalidRegistration if
      * there is no consumer registered with the given registration handle.  
      * 
      * @param request a generated wrapper containing all input arguments for this method
      * 
      * @exception java.rmi.RemoteException
      * 
      * @throws InvalidRegistration
      *         The given registrationhandle is invalid / not yet registered
      */
     public ReturnAny deregister(RegistrationContext request) throws java.rmi.RemoteException
     {
 
         String MN = "deregister";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         // perform parameter check
         paramCheck.check(request, Constants.NILLABLE_FALSE);
 
         try
         {
 
             checkRegistration(request);
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return new ReturnAny();
     }
 
     /**
      * Modifies the registration of a certain consumer. Throws an InvalidRegistration
      * if there is no consumer registered with the given registration handle.
      * 
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return A RegistrationState-object if the Producer chooses to have the Consumer provide
      *         persistent storage for those registration states, else null.
      * 
      * @exception java.rmi.RemoteException
      */
     public RegistrationState modifyRegistration(ModifyRegistration request) throws java.rmi.RemoteException
     {
 
         String MN = "modifyRegistration";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         // perform parameter check
         paramCheck.check(request);
 
         // TODO: what behaviour is there required=
         // just set the new stuff
         // or merge it in?
 
         RegistrationState regState = null;
 
         try
         {
 
             checkRegistration(request.getRegistrationContext());
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
         }
 
         Registration registration = consumerRegistry.get(request.getRegistrationContext().getRegistrationHandle());
 
         // set new attributes
         registration.setRegistrationData(request.getRegistrationData());
         registration.setRegistrationContext(request.getRegistrationContext());
 
         // return changed registration state
         // -> case Producer chooses to have Consumer provide persistent storage
         regState = new RegistrationState();
         regState.setRegistrationState(registration.getRegistrationContext().getRegistrationState());
         regState.setExtensions(null);
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return regState;
 
     }
 
     /**
      * check if the registration handle is valid
      * if not throw some RuntimeException
      * 
      * @param registrationContext
      *               the registration handle of the consumer
      * 
      * @throws InvalidRegistration
      * @throws MissingParameterFault
      */
     private void checkRegistration(RegistrationContext registrationContext)
         throws java.rmi.RemoteException, WSRPException
     {
 
         String MN = "checkRegistration";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         paramCheck.check(registrationContext, Constants.NILLABLE_FALSE);
 
         if (!consumerRegistry.check(registrationContext.getRegistrationHandle()))
         {
             throw new WSRPException(ErrorCodes.INVALID_REGISTRATION);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
     }
 
     /**
      * Get the required portlet AND check for the registration.
      * If the producer is enabled for registration the
      * portlet's registration policy is being checked.
      * 
      * @param registrationContext
      * @param portletHandle
      * @return 
      * @exception java.rmi.RemoteException
      */
     private Portlet getPortlet(RegistrationContext registrationContext, String portletHandle)
         throws java.rmi.RemoteException
     {
 
         String MN = "getPortlet";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         Portlet portlet = null;
 
         try
         {
 
             portlet = provider.getPortletPool().get(portletHandle);
 
             if (registrationRequired)
             {
                 ProducerOfferedPortlet parent = null;
 
                 if (portlet instanceof ProducerOfferedPortlet)
                 {
                     parent = (ProducerOfferedPortlet)portlet;
                 } else
                 {
                     //validate the consumers registration for the requested clone portlet
                     String regHandle = registrationContext.getRegistrationHandle();
                     if (!provider.getPortletRegistrationFilter().isAvailable(regHandle, portletHandle))
                     {
                         throw new WSRPException(ErrorCodes.ACCESS_DENIED);
                     }
 
                     // get parent
                     String parentHandle = ((ConsumerConfiguredPortlet)portlet).getParentHandle();
 
                     parent = (ProducerOfferedPortlet)provider.getPortletPool().get(parentHandle);
 
                 }
 
                 if ((parent != null) && parent.isRegistrationRequired())
                 {
                     checkRegistration(registrationContext);
                 }
 
             }
 
         } catch (WSRPException e)
         {
             WSRPXHelper.handleWSRPException(e);
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return portlet;
     }
 
 
     /**
      * This method checks the MarkupParams from the consumer request and tries to
      * find a match between the mimeType, mode, windowState and locale of the
      * portlet description and the requested values. The first match which is found
      * is returned in a <code>MarkupType</code> object where the arrays
      * for modes and windowStates and locales have a length of one.  
      *  
      * @param portletDescription The description of the portlet.
      * @param markupParams The markup params from the consumer request
      * @return The <code>MarkupType</code> object which should be used to call the portlet.
      * @throws WSRPException In case no match was found
      **/
     private MarkupType checkMarkupParams(PortletDescription portletDescription, MarkupParams markupParams)
         throws WSRPException
     {
 
         String MN = "checkMarkupParams";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         MarkupType match = new MarkupType();
 
         String[] reqMimeTypes = markupParams.getMimeTypes();
         String reqMode = markupParams.getMode();
         String reqWindowState = markupParams.getWindowState();
         String[] reqLocales = markupParams.getLocales();
         boolean foundMime, foundWindow, foundMode;
         foundMime = foundWindow = foundMode = false;
 
         MarkupType[] markupTypes = portletDescription.getMarkupTypes();
         for (int i = 0; i < markupTypes.length; i++)
         {
 
             // check if mime type is supported by portlet
             String mimeType = markupTypes[i].getMimeType();
             for (int j = 0; j < reqMimeTypes.length; j++)
             {
 
                 // TODO: Checking of mime types that are
                 //        not fully qualified e.g. 'text/*'
                 if (mimeType.equalsIgnoreCase(reqMimeTypes[j]))
                 {
                     foundMime = true;
                     match.setMimeType(mimeType);
 
                     // check if the mode is supported for this mime type
                     String[] modes = markupTypes[i].getModes();
                     for (int k = 0; k < modes.length; k++)
                     {
 
                         if (modes[k].equalsIgnoreCase(reqMode))
                         {
                             foundMode = true;
                             match.setModes(new String[] { reqMode });
 
                             //check window state
                             String[] windowStates = markupTypes[i].getWindowStates();
                             for (int l = 0; l < windowStates.length; l++)
                             {
 
                                 if (windowStates[l].equalsIgnoreCase(reqWindowState))
                                 {
                                     foundWindow = true;
                                     match.setWindowStates(new String[] { reqWindowState });
 
                                     // check locales
                                     String[] locales = markupTypes[i].getLocales();
                                     if (locales == null)
                                     {
                                         if (logger.isLogging(Logger.TRACE_HIGH))
                                         {
                                             logger.exit(Logger.TRACE_HIGH, MN);
                                         }
                                         return match;
                                     }
 
                                     for (int m = 0; m < locales.length; m++)
                                     {
                                         for (int n = 0; n < reqLocales.length; n++)
                                         {
                                             if (locales[m].equalsIgnoreCase(reqLocales[n]))
                                             {
                                                 match.setLocales(new String[] { locales[m] });
 
                                                 if (logger.isLogging(Logger.TRACE_HIGH))
                                                 {
                                                     logger.exit(Logger.TRACE_HIGH, MN);
                                                 }
                                                 return match;
                                             } else
                                             {
                                                 // lets see if at least a the language can be done
                                                 if (LocaleHelper
                                                     .getLanguageCode(locales[m])
                                                     .equalsIgnoreCase(LocaleHelper.getLanguageCode(reqLocales[n])))
                                                 {
                                                     match.setLocales(
                                                         new String[] { LocaleHelper.getLanguageCode(locales[m])});
 
                                                     if (logger.isLogging(Logger.TRACE_HIGH))
                                                     {
                                                         logger.exit(Logger.TRACE_HIGH, MN);
                                                     }
 
                                                     return match;
                                                 }
 
                                             }
                                         }
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         if (!foundMime)
             throw new WSRPException(ErrorCodes.UNSUPPORTED_MIME_TYPE);
         if (!foundMode)
             throw new WSRPException(ErrorCodes.UNSUPPORTED_MODE);
         if (!foundWindow)
             throw new WSRPException(ErrorCodes.UNSUPPORTED_WINDOW_STATE);
 
         throw new WSRPException(ErrorCodes.UNSUPPORTED_LOCALE);
     }
 
     /**
      * Markup Interface
      */
 
     /**
      * <p>Requests the markup for rendering the current state of a portlet by calling
      * the PortletInvoker.</p>
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool that
      * corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      * <p>Fetches the portlet description from the DescriptionHandler to figure out
      * whether the portlet supports template processing or not. If so, it checks whether
      * templates are provided or not.</p>
      *  
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return MarkupResponse
      *
      * @exception java.rmi.RemoteException
      */
     public MarkupResponse getMarkup(GetMarkup request) throws java.rmi.RemoteException
     {
 
         String MN = "getMarkup";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         MarkupResponse markupResponse = null;
         
         try
         {
             // perform parameter check
             paramCheck.check(request);
             String userId = request.getUserContext().getUserContextKey();
 
             // Do the session thing
             sessionManager.setCurrentSession(false);
             sessionManager.setCurrentUser(userId);
            
             RegistrationContext regContext = request.getRegistrationContext();
 
             PortletDescription portletDescription = null;
 
             portletDescription =
                 provider.getDescriptionHandler().getPortletDescription(
                     request.getPortletContext().getPortletHandle(),
                     regContext,
                     null, request.getMarkupParams().getLocales());
 
             // check the markup params and find the locale, mimetype, mode, state whcih is used
             // for render
             MarkupType markupType = this.checkMarkupParams(portletDescription, request.getMarkupParams());
 
             //replace with markup types from the request
             request.getMarkupParams().setLocales(markupType.getLocales());
             request.getMarkupParams().setMimeTypes(new String[] { markupType.getMimeType()});
             request.getMarkupParams().setMode(markupType.getModes()[0]);
             request.getMarkupParams().setWindowState(markupType.getWindowStates()[0]);
             request.getMarkupParams().setWindowState(markupType.getWindowStates()[0]);
 
             // check if template processing is supported
             Boolean templateProcessing = portletDescription.getDoesUrlTemplateProcessing();
             if (templateProcessing == null)
             {
                 // TODO get default
                 templateProcessing = Boolean.FALSE;
             }
 
             if (templateProcessing.booleanValue())
             {
                 // consumer has to provide templates
                 try
                 {
                     paramCheck.check(request.getRuntimeContext().getTemplates());
                 } catch (MissingParametersFault e)
                 {
                     templateProcessing = Boolean.FALSE;
                 }
             }
 
             // invoke the portlet service
             markupResponse = provider.getPortletInvoker().invokeGetMarkup(request);
 
             // TODO: Check if markup requires rewriting.
             //        For now: If no template processing supported set requiresRewriting=true
             if (!templateProcessing.booleanValue())
             {
                 markupResponse.getMarkupContext().setRequiresUrlRewriting(Boolean.TRUE);
             } else
             {
                 markupResponse.getMarkupContext().setRequiresUrlRewriting(Boolean.FALSE);
             }
 
         } catch (WSRPException e)
         {
             WSRPXHelper.handleWSRPException(e);
         }
         finally {
             sessionManager.resetCurrentSession();
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return markupResponse;
     }
 
     /**
      * <p>Invokes a blocking interaction with the adressed portlet by calling
      * the PortletInvoker</p>
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool that
      * corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      *
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return BlockingInteractionResponse
      *
      * @exception java.rmi.RemoteException
      */
     public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction request)
         throws java.rmi.RemoteException
     {
         String MN = "performBlockingInteraction";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         BlockingInteractionResponse interactionResponse = null;
         
         try
         {
 
             // perform parameter check
             paramCheck.check(request);
             String userId = request.getUserContext().getUserContextKey();
 
             sessionManager.setCurrentSession(false);
             sessionManager.setCurrentUser(userId);
 
             // check PortletStateChange setting
             // might clone before write
             PortletContext newPortletContext = null;
             /*
             // TODO: Address Cloning
             newPortletContext = handlePortletStateChange(request);
             */
 
             
             // check the markup params and find the locale, mimetype, mode, state whcih is used
             // for render
             PortletDescription portletDescription = null;
 
             portletDescription =
                 provider.getDescriptionHandler().getPortletDescription(
                     request.getPortletContext().getPortletHandle(),
                     request.getRegistrationContext(),
                     null, request.getMarkupParams().getLocales());
 
             MarkupType markupType = this.checkMarkupParams(portletDescription, request.getMarkupParams());
 
             //replace with markup types from the request
             request.getMarkupParams().setLocales(markupType.getLocales());
             request.getMarkupParams().setMimeTypes(new String[] { markupType.getMimeType()});
             request.getMarkupParams().setMode(markupType.getModes()[0]);
             request.getMarkupParams().setWindowState(markupType.getWindowStates()[0]);
 
             // invoke the service
             interactionResponse = provider.getPortletInvoker().invokePerformBlockingInteraction(request);
             if (newPortletContext != null)
             {
                 interactionResponse.getUpdateResponse().setPortletContext(newPortletContext);
             }
            // check if template processing is supported
            Boolean templateProcessing = portletDescription.getDoesUrlTemplateProcessing();
            if (templateProcessing == null)
            {
                // TODO get default
                templateProcessing = Boolean.FALSE;
            }

            if (templateProcessing.booleanValue())
            {
                // consumer has to provide templates
                try
                {
                    paramCheck.check(request.getRuntimeContext().getTemplates());
                } catch (MissingParametersFault e)
                {
                    templateProcessing = Boolean.FALSE;
                }
            }

            // TODO: Check if markup requires rewriting.
            //        For now: If no template processing supported set requiresRewriting=true
            if (!templateProcessing.booleanValue())
            {
                interactionResponse.getUpdateResponse().getMarkupContext().setRequiresUrlRewriting(Boolean.TRUE);
            } else
            {
                interactionResponse.getUpdateResponse().getMarkupContext().setRequiresUrlRewriting(Boolean.TRUE);
            }

         } catch (WSRPException e)
         {
             WSRPXHelper.handleWSRPException(e);
         }
         finally {
             sessionManager.resetCurrentSession();
         }
 
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return interactionResponse;
     }
 
     /**
      * checks if portlet may be accessed according to the PortletStateChange setting
      * throws the appropriate exceptions
      * or clones before write
      * @param request
      */
     private PortletContext handlePortletStateChange(PerformBlockingInteraction request)
         throws RemoteException, WSRPException
     {
 
         final String MN = "handlePortletStateChange";
 
         PortletContext portletContext = null;
         Portlet portlet = null;
         try
         {
             portlet = provider.getPortletPool().get(request.getPortletContext().getPortletHandle());
         } catch (WSRPException e)
         {
             if (e.getErrorCode() == ErrorCodes.GET_PORTLET_FAILED)
             {
                 // portlet not in pool, must be an invalid handle
                 WSRPXHelper.throwX(logger, Logger.ERROR, MN, ErrorCodes.INVALID_HANDLE);
             } else
             {
                 WSRPXHelper.throwX(e.getErrorCode());
             }
         }
 
         // use the bad way to distinguish the portlet type
         // TODO: need to refine that
         StateChange stateChange = null;
         stateChange = request.getInteractionParams().getPortletStateChange();
         if (portlet instanceof ProducerOfferedPortlet)
         {
             if (stateChange.toString().equals(StateChange._readOnly))
             {
                 // force the consumer to clone since we can't make sure the portlet won't change state
                 // in JSR168 case
                 WSRPXHelper.throwX(ErrorCodes.PORTLET_STATE_CHANGE_REQUIRED);
             } else if (stateChange.toString().equals(StateChange._cloneBeforeWrite))
             {
                 // need to clone the portlet
                 portletContext = handleCloneBeforeWrite(request);
             } else
             {
                 // must be readWrite, can't access a POP in readWrite mode
                 WSRPXHelper.throwX(ErrorCodes.OPERATION_FAILED);
             }
         } else if (portlet instanceof ConsumerConfiguredPortlet)
         {
             if (stateChange.toString().equals(StateChange._readOnly))
             {
                 // does it make sense to access a CCP in readOnly?
                 // force consumer to clone if he access a CCP in readOnly
                 WSRPXHelper.throwX(ErrorCodes.PORTLET_STATE_CHANGE_REQUIRED);
             } else if (stateChange.toString().equals(StateChange._cloneBeforeWrite))
             {
                 // need to clone the portlet
                 portletContext = handleCloneBeforeWrite(request);
             } else
             {
                 // must be readWrite, readWrite is fine, continue
             }
         } else
         {
             // what is it then :-)
             WSRPXHelper.throwX(logger, Logger.ERROR, MN, ErrorCodes.OPERATION_FAILED);
         }
 
         return portletContext;
     }
 
     /**
      * clone portlet and its session before performBlockingInteraction continues
      * @param request
      */
     private PortletContext handleCloneBeforeWrite(PerformBlockingInteraction request)
         throws WSRPException, java.rmi.RemoteException
     {
 
         // clone the portlet
         PortletContext portletContext = null;
         ClonePortlet cloneRequest = new ClonePortlet();
         cloneRequest.setPortletContext(request.getPortletContext());
         cloneRequest.setRegistrationContext(request.getRegistrationContext());
         cloneRequest.setUserContext(request.getUserContext());
 
         portletContext = clonePortlet(cloneRequest);
 
         // change the portletContext in the original request
         // thus we continue with the new cloned portlet
         request.setPortletContext(portletContext);
 
         // TODO: copy session        
 
         return portletContext;
     }
 
     /**                                                                          
      * Creates a new HTTP session and initializes it
      * <p>Throws InvalidRegistration if there is no registration with the given
      * registration handle available within the ConsumerRegistry (if registration is
      * required).</p>
      *
      * @param request a generated wrapper containing all input arguments for this method
      * 
      * @exception java.rmi.RemoteException
      *
      * @return null
      * 
      * @throws MissingParameterFault
      * @throws InvalidRegistration
      */
     public ReturnAny initCookie(InitCookie request) throws java.rmi.RemoteException
     {
 
         String MN = "initCookie";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
         try
         {
 
             // perform parameter check
             paramCheck.check(request);
 
             // if registration is required, a valid registration must exist
             // for the given registration handle
             if (registrationRequired)
             {
                 checkRegistration(request.getRegistrationContext());
             }
 			
 			// create the HTTP session
             PortletSessionManager sm = provider.getSessionManager();
             sm.setCurrentSession(true);
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return new ReturnAny();
     }
 
     /**
      * <p>Destroys the sessions adressed by the delivered sessionIDs.
      * Returns null by default.</p>
      * <p>Throws InvalidRegistration if there is no registration with the given
      * registration handle available within the ConsumerRegistry (case registration is
      * required).</p>
      *
      * @param request a generated wrapper containing all input arguments for this method
      * 
      * @return null
      */
     public ReturnAny releaseSessions(ReleaseSessions request) throws java.rmi.RemoteException
     {
 
         String MN = "releaseSession";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
 		// since we don't use the WSRP sessions
 		// we always report success
 
 		if (logger.isLogging(Logger.TRACE_HIGH))
 		{
 			logger.exit(Logger.TRACE_HIGH, MN);
 		}
         return new ReturnAny();
     }
 
     /**
      * Portlet Management Interface
      */
 
     /**
      * <p>Fetches a PortletDescription from the DescriptionHandler.</p>
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool that
      * corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      * 
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return 
      *
      * @exception java.rmi.RemoteException
      */
     public PortletDescriptionResponse getPortletDescription(GetPortletDescription request)
         throws java.rmi.RemoteException
     {
 
         String MN = "getPortletDescription";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         PortletDescription portletDescription = null;
         PortletDescriptionResponse response = null;
 
         try
         {
 
             // perform parameter check
             paramCheck.check(request);
 
             RegistrationContext regContext = request.getRegistrationContext();
 
             portletDescription =
                 provider.getDescriptionHandler().getPortletDescription(
                     request.getPortletContext().getPortletHandle(),
                     regContext,
                     null, request.getDesiredLocales());
 
             response = new PortletDescriptionResponse();
             response.setPortletDescription(portletDescription);
             response.setResourceList(null);
             response.setExtensions(null);
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return response;
     }
 
     /**
      * <p>Calls the PortletPool to clone a portlet.</p>
      * <p>Throws InvalidHandleFault if there is no portlet to be cloned in the PortletPool
      * that corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      * 
      * @param request a generated wrapper containing all input arguments for this method
      * @return 
      * @exception java.rmi.RemoteException
      */
     public PortletContext clonePortlet(ClonePortlet request) throws java.rmi.RemoteException
     {
 
         String MN = "clonePortlet";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         PortletContext portletContext = null;
 
         try
         {
             // parameter validation
             paramCheck.check(request);
 
             PortletStateManager portletStateManager = null;
 
             portletStateManager = provider.getPortletStateManager();
 
             String orgPortletHandle = request.getPortletContext().getPortletHandle();
 
             Portlet portletClone = null;
 
             // clone portlet
             portletClone = provider.getPortletPool().clone(orgPortletHandle);
 
             if (portletClone != null)
             {
 
                 String newPortletHandle = portletClone.getPortletHandle();
 
                 // create portlet context
                 portletContext = new PortletContext();
                 portletContext.setPortletHandle(newPortletHandle);
                 portletContext.setPortletState(portletStateManager.getAsString(newPortletHandle).getBytes());
                 portletContext.setExtensions(null);
 
                 // remember clone handle for this registration
                 String regHandle = request.getRegistrationContext().getRegistrationHandle();
                 if (regHandle != null && consumerRegistry.check(regHandle) == true)
                 {
                     provider.getPortletRegistrationFilterWriter().makeAvailable(regHandle, newPortletHandle);
                 }
 
             } else
             {
                 throw new WSRPException(ErrorCodes.OPERATION_FAILED);
             }
 
         } catch (WSRPException e)
         {
             WSRPXHelper.handleWSRPException(e);
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return portletContext;
     }
 
     /**
      * <p>Removes all portlets corresponding to the provided portlet handles from the
      * PortletPool. All portlet handles refering to portlets that cannot be destroyed
      * (e.g. portlet handles from Producer Offered Portlets) will be returned within the
      * DestroyPortletResponse as DestroyFailed-objects.</p>
      * <p>Throws InvalidRegistration if there is no registration with the given
      * registration handle available within the ConsumerRegistry (case registration
      * is required).</p>
      *
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @exception java.rmi.RemoteException
      */
     public DestroyPortletsResponse destroyPortlets(DestroyPortlets request) throws java.rmi.RemoteException
     {
 
         String MN = "destroyPortlets";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         // perform paramCheck.check
         paramCheck.check(request);
 
         try
         {
 
             checkRegistration(request.getRegistrationContext());
 
             // remove ConsumerPortletRegistrations
             String regHandle = request.getRegistrationContext().getRegistrationHandle();
             if (regHandle != null && consumerRegistry.check(regHandle) == true)
             {
                 provider.getPortletRegistrationFilterWriter().remove(
                     regHandle,
                     Arrays.asList(request.getPortletHandles()).iterator());
             }
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
         }
 
         Iterator handles = Arrays.asList(request.getPortletHandles()).iterator();
 
         // result iterator contains portlet handles refering to those portlets
         // that could not be deleted (e.g. producer offered portlets)
         Iterator result = provider.getPortletPool().destroySeveral(handles);
 
         ArrayList failedHandles = new ArrayList();
 
         while (result.hasNext())
         {
 
             DestroyFailed failed = new DestroyFailed();
             failed.setPortletHandle(result.next().toString());
             failed.setReason("Portlet handle refers to a producer offered portlet!");
             failedHandles.add(failed);
 
         }
 
         DestroyFailed[] destroyFailedArray = new DestroyFailed[failedHandles.size()];
         failedHandles.toArray(destroyFailedArray);
 
         DestroyPortletsResponse response = new DestroyPortletsResponse();
         response.setDestroyFailed(destroyFailedArray);
         response.setExtensions(null);
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return response;
     }
 
     /**
      * <p>Sets the properties of a portlet.</p>
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool
      * that corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      *
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return 
      * @exception java.rmi.RemoteException
      */
     public PortletContext setPortletProperties(SetPortletProperties request) throws java.rmi.RemoteException
     {
 
         String MN = "setPortletProperties";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         PortletContext portletContext = null;
 
         try
         {
 
             // perform parameter check
             paramCheck.check(request);
 
             String portletHandle = request.getPortletContext().getPortletHandle();
             Portlet portlet = getPortlet(request.getRegistrationContext(), portletHandle);
 
             // check if portlet is a consumer configured portlet //TODO? 
             if (portlet instanceof ConsumerConfiguredPortlet)
             {
 
                 provider.getPortletStateManager().setAsPropertyList(portletHandle, request.getPropertyList());
                 portletContext = request.getPortletContext();
 
             } else
             {
                 throw new WSRPException(ErrorCodes.INCONSISTENT_PARAMETERS);
             }
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return portletContext;
 
     }
 
     /**
      * <p>Fetches the current property values of a portlet and returns them.</p> 
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool
      * that corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      * 
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return PropertyList containing all properties of the adressed portlet
      *
      * @exception java.rmi.RemoteException
      */
     public PropertyList getPortletProperties(GetPortletProperties request) throws java.rmi.RemoteException
     {
 
         String MN = "getPortletProperties";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         PropertyList propertyList = null;
 
         try
         {
             // perform parameter check
             paramCheck.check(request);
 
             String portletHandle = request.getPortletContext().getPortletHandle();
             getPortlet(request.getRegistrationContext(), portletHandle);
 
             PortletStateManager portletStateManager = provider.getPortletStateManager();
             propertyList = portletStateManager.getAsPropertyList(portletHandle);
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return propertyList;
     }
 
     /**
      * <p>Returns a model description containing the property descriptions of all properties
      * of a portlet.</p>
      * <p>Throws InvalidHandleFault if there is no portlet in the PortletPool
      * that corresponds to the provided portlet handle. Throws InvalidRegistration
      * if there is no registration with the given registration handle available within
      * the ConsumerRegistry (case registration is required).</p>
      * 
      * @param request a generated wrapper containing all input arguments for this method
      *
      * @return 
      *
      * @exception java.rmi.RemoteException
      */
     public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription request)
         throws java.rmi.RemoteException
     {
 
         String MN = "getPortletPropertyDescription";
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.entry(Logger.TRACE_HIGH, MN);
         }
 
         PortletPropertyDescriptionResponse response = null;
 
         try
         {
 
             // perform parameter check
             paramCheck.check(request);
 
             String portletHandle = request.getPortletContext().getPortletHandle();
             getPortlet(request.getRegistrationContext(), portletHandle);
 
             response = new PortletPropertyDescriptionResponse();
             response.setModelDescription(
                 provider.getPortletStateManager().getModelDescription(
                     portletHandle,
                     request.getDesiredLocales(),
                     false));
             response.setResourceList(null);
             response.setExtensions(null);
 
         } catch (WSRPException e)
         {
 
             WSRPXHelper.handleWSRPException(e);
 
         }
 
         if (logger.isLogging(Logger.TRACE_HIGH))
         {
             logger.exit(Logger.TRACE_HIGH, MN);
         }
 
         return response;
     }
 
 }
