 /*
  * Copyright (c) 2004 UNINETT FAS
  *
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation; either version 2 of the License, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * $Id$
  */
 
 package no.feide.moria.webservices.v1_1;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import no.feide.moria.controller.AuthenticationException;
 import no.feide.moria.controller.AuthorizationException;
 import no.feide.moria.controller.DirectoryUnavailableException;
 import no.feide.moria.controller.IllegalInputException;
 import no.feide.moria.controller.InoperableStateException;
 import no.feide.moria.controller.MoriaController;
 import no.feide.moria.controller.UnknownTicketException;
 import no.feide.moria.log.MessageLogger;
 
 import org.apache.axis.MessageContext;
 import org.apache.axis.session.Session;
 import org.apache.axis.transport.http.AxisHttpSession;
 
 /**
  * @author Bjrn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
  * @version $Revision$
  */
 public final class Authentication implements AuthenticationIF {
 
     /** Class wide logger. */
     private MessageLogger messageLogger;
 
     /** Log message for AuthorizationExceptions. */
     private static final String AUTHZ_EX_MESSAGE = "Authorization failed. Throwing RemoteException to service: ";
 
     /** Log message for AuthenticationExceptions. */
     private static final String AUTHN_EX_MSG = "Authentication failed. Throwing RemoteException to service: ";
 
     /** Log message for DirectoryUnavailableExceptions. */
     private static final String DIR_UNAV_EX_MSG = "Directory unavailable. Throwing RemoteException to service: ";
 
     /** Log message for MoriaControllerExceptions. */
     private static final String MORIACTRL_EX_MESSAGE = "Exception from MoriaController. Throwing RemoteException to service: ";
 
     /** Log message for InoperableStateExceptions. */
     private static final String INOP_STATE_EX_MSG = "Controller in inoperable state. Throwing RemoteException to service: ";
 
     /** Log message for UnknownTicketExceptions. */
     private static final String UNKNOWN_TICKET_EX_MSG = "Ticket is unknown. Throwing RemoteException to service: ";
 
     /**
      * Default constructor.
      * Initiates logger.
      */
     public Authentication() {
         messageLogger = new MessageLogger(Authentication.class);
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#initiateAuthentication(java.lang.String[],
      *      java.lang.String, java.lang.String, boolean)
      */
     public String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix,
             final boolean forceInteractiveAuthentication) throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         String urlPrefix = null;
         Session genericSession = messageContext.getSession();
 
         if (genericSession instanceof AxisHttpSession) {
             AxisHttpSession axisHttpSession = (AxisHttpSession) genericSession;
             urlPrefix = ((Properties) axisHttpSession.getRep().getServletContext().getAttribute("no.feide.moria.web.config"))
                     .getProperty("no.feide.moria.web.login.url_prefix");
         }
 
         try {
             return urlPrefix
                     + MoriaController.initiateAuthentication(attributes, returnURLPrefix, returnURLPostfix,
                             forceInteractiveAuthentication, servicePrincipal);
         } catch (AuthorizationException ae) {
             messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#directNonInteractiveAuthentication(java.lang.String[],
      *      java.lang.String, java.lang.String)
      */
     public Attribute[] directNonInteractiveAuthentication(final String[] attributes, final String username, final String password)
             throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         try {
             Map returnAttributes = MoriaController.directNonInteractiveAuthentication(attributes, username, password,
                     servicePrincipal);
             return mapToAttributeArray(returnAttributes, null);
         } catch (AuthorizationException ae) {
             messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (AuthenticationException ae) {
             messageLogger.logWarn(AUTHN_EX_MSG + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (DirectoryUnavailableException due) {
             messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, due);
             throw new RemoteException(due.getMessage());
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#proxyAuthentication(java.lang.String[],
      *      java.lang.String)
      */
     public Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket) throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         try {
             Map returnAttributes = MoriaController.proxyAuthentication(attributes, proxyTicket, servicePrincipal);
             return mapToAttributeArray(returnAttributes, proxyTicket);
         } catch (AuthorizationException ae) {
             messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         } catch (UnknownTicketException ute) {
             messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, ute);
             throw new RemoteException(ute.getMessage());
 
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#getProxyTicket(java.lang.String,
      *      java.lang.String)
      */
     public String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal) throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         try {
             return MoriaController.getProxyTicket(ticketGrantingTicket, proxyServicePrincipal, servicePrincipal);
         } catch (AuthorizationException ae) {
             messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         } catch (UnknownTicketException ute) {
             messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, ute);
             throw new RemoteException(ute.getMessage());
 
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#getUserAttributes(java.lang.String)
      */
     public Attribute[] getUserAttributes(final String serviceTicket) throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         try {
             Map returnAttributes = MoriaController.getUserAttributes(serviceTicket, servicePrincipal);
             return mapToAttributeArray(returnAttributes, serviceTicket);
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         } catch (UnknownTicketException ute) {
             messageLogger.logWarn(UNKNOWN_TICKET_EX_MSG + servicePrincipal, ute);
             throw new RemoteException(ute.getMessage());
 
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#getGroupAttributes(java.lang.String)
      */
     public Attribute[] getGroupAttributes(final String groupname) throws RemoteException {
         // TODO: There's no support for this in the underlying layers at the moment.
         return new Attribute[] {};
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#verifyUserExistence(java.lang.String)
      */
     public boolean verifyUserExistence(final String username) throws RemoteException {
 
         /* Axis message context containg request data. */
         MessageContext messageContext = MessageContext.getCurrentContext();
         String servicePrincipal = messageContext.getUsername();
 
         try {
             return MoriaController.verifyUserExistence(username, servicePrincipal);
         } catch (AuthorizationException ae) {
             messageLogger.logWarn(AUTHZ_EX_MESSAGE + servicePrincipal, ae);
             throw new RemoteException(ae.getMessage());
         } catch (DirectoryUnavailableException due) {
             messageLogger.logWarn(DIR_UNAV_EX_MSG + servicePrincipal, due);
             throw new RemoteException(due.getMessage());
         } catch (IllegalInputException iie) {
             messageLogger.logWarn(MORIACTRL_EX_MESSAGE + servicePrincipal, iie);
             throw new RemoteException(iie.getMessage());
         } catch (InoperableStateException ise) {
             messageLogger.logCritical(INOP_STATE_EX_MSG + servicePrincipal, ise);
             throw new RemoteException(ise.getMessage());
         }
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#verifyGroupExistence(java.lang.String)
      */
     public boolean verifyGroupExistence(final String groupname) throws RemoteException {
         // TODO: There's no support for this in the underlying layers at the moment.
         return false;
     }
 
     /**
      * @see no.feide.moria.webservices.v1_1.AuthenticationIF#verifyUserMemberOfGroup(java.lang.String,
      *      java.lang.String)
      */
     public boolean verifyUserMemberOfGroup(final String username, final String groupname) throws RemoteException {
         // TODO: There's no support for this in the underlying layers at the moment.
         return false;
     }
 
     /**
      * Utility method that converts a Map to an array of Attributes.
      *
      * @param map
      *          the Map to be converted
      * @param activeTicketId
      *          optional variable for logging purposes
      * @return array of attribute objects
      */
     private Attribute[] mapToAttributeArray(final Map map, final String activeTicketId) {
 
         /* Get iterator for map keys. */
         Iterator iterator = map.keySet().iterator();
 
         /* List to hold finished Attributes while processing map. */
         List attributeList = new ArrayList();
 
         /* Iterate over keys in map. */
         while (iterator.hasNext()) {
             Object key = iterator.next();
             Object value = map.get(key);
 
             Attribute attribute = new Attribute();
 
             /* Check that key is a String, if not will ignore the whole entry. */
             if (key instanceof String) {
                 /* Add the key as name of attribute. */
                 attribute.setName((String) key);
 
                 /*
                  * Check type of value. If not String or String[] we don't add it to the attribute
                  * list resulting in the whole entry beeing ignored.
                  */
                 if (value instanceof String) {
                     /* Create one-element String[] of value before setting. */
                     attribute.setValues(new String[] {(String) value});
                     attributeList.add(attribute);
                 } else if (value instanceof String[]) {
                     attribute.setValues((String[]) value);
                     attributeList.add(attribute);
                } else if (value != null) {
                     messageLogger.logInfo("Attribute value not String or String[]. Entry not added to Attribute[]. ",
                             activeTicketId);
                 }
            } else if (value != null) {
                 messageLogger.logInfo("Attribute key not String. Entry not added to Attribute[]", activeTicketId);
             }
         }
         return (Attribute[]) attributeList.toArray(new Attribute[] {});
     }
 }
