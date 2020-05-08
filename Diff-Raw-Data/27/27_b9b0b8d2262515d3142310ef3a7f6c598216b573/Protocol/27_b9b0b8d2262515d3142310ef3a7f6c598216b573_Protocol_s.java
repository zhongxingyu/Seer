 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Andrew Nisbet
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
  * MA 02110-1301, USA.
  *
  */
 package mecard;
 
 import api.Request;
import com.google.gson.Gson;
 import mecard.Exception.UnsupportedCommandException;
 import mecard.Exception.MalformedCommandException;
 import mecard.Exception.UnsupportedResponderException;
 import mecard.config.ConfigFileTypes;
 import mecard.config.LibraryPropertyTypes;
 import mecard.config.PropertyReader;
 import mecard.responder.BImportResponder;
 import mecard.responder.ResponderMethodTypes;
 import mecard.responder.Responder;
 import mecard.responder.SIP2Responder;
 import mecard.responder.APIResponder;
 import mecard.security.SecurityManager;
 
 /**
  * Responsible for interpreting incoming MeCard requests.
  *
  * The language is loosely based on SIP but it IS NOT SIP in any way. The
  * command structure has pipe-delimited fields. Typically:
  * <code>QA0|dfae434324354asdfa344|</code><br/>
  * The second field is a MD5 computed hash of the command, salted with a shared
  * secret, to ensure the authenticity of the request.
  *
  * @author metro
  */
 public class Protocol
 {
    public final static String DELIMITER   = "|";
     public final static String DEFAULT_FIELD = "X";
     public final static String TRUE        = "Y";
     public final static String FALSE       = "N";
    public final static String TERMINATE   = "XX0" + DELIMITER;
    public final static String ACKNOWLEDGE = "XK0" + DELIMITER;
    public final static String ERROR       = "XE0" + DELIMITER;
     
     private boolean debugMode;
     public Protocol()
     { 
         String debug = PropertyReader.getProperties(ConfigFileTypes.ENVIRONMENT)
                 .getProperty(LibraryPropertyTypes.DEBUG.toString());
         debugMode = Boolean.valueOf(debug);
     }
 
     /**
      * Checks incoming commands to the server, parses what the command, its 
      * security token and if valid then runs the required activity.
      *
      * @param cmd command string request.
      * @return formatted response string.
      */
     public String processInput(String cmd)
     {
         String response = TERMINATE; // initialize response.
         try
         {
             String jsonCommand = SecurityManager.unEncrypt(cmd);
             Request request = new Request(jsonCommand);
             Responder responder = getResponder(request);
             response = responder.getResponse();
         }
         catch (RuntimeException ex)
         {
             response = Responder.getExceptionResponse(ex);
         }
         finally
         {
             return SecurityManager.encrypt(response);
         }
     }
 
     /**
      *
      *
      * @param command the value of command
      */
     protected Responder getResponder(Request command)
             throws UnsupportedCommandException, UnsupportedResponderException, MalformedCommandException
     {
         QueryTypes queryType = command.getCommandType();
         String serviceType;
         switch (queryType)
         {
             case CREATE_CUSTOMER:
                 serviceType = PropertyReader.
                     getProperties(ConfigFileTypes.ENVIRONMENT).
                     getProperty(LibraryPropertyTypes.CREATE_SERVICE.toString());
                 return mapResponderType(serviceType, command);
             case UPDATE_CUSTOMER:
                 serviceType = PropertyReader.
                     getProperties(ConfigFileTypes.ENVIRONMENT).
                     getProperty(LibraryPropertyTypes.UPDATE_SERVICE.toString());
                 return mapResponderType(serviceType, command);
             case GET_STATUS:
                 serviceType = PropertyReader.
                     getProperties(ConfigFileTypes.ENVIRONMENT).
                     getProperty(LibraryPropertyTypes.STATUS_SERVICE.toString());
                 return mapResponderType(serviceType, command);
             case GET_CUSTOMER:
                 serviceType = PropertyReader.
                     getProperties(ConfigFileTypes.ENVIRONMENT).
                     getProperty(LibraryPropertyTypes.GET_SERVICE.toString());
                 return mapResponderType(serviceType, command);
             default:
                 throw new UnsupportedCommandException(); 
         }
     }
     
     /**
      * Creates the appropriate responder based on what string value was entered
      * in the environment configuration file.
      *
      * @param configRequestedService
      * @param command
      * @return
      * @throws UnsupportedCommandException 
      */
     
     private Responder mapResponderType(
             String configRequestedService, Request command)
         throws UnsupportedCommandException
     {
         if (configRequestedService.equalsIgnoreCase(ResponderMethodTypes.BIMPORT.toString()))
         {
             return new BImportResponder(command, debugMode);
         }
         else if (configRequestedService.equalsIgnoreCase(ResponderMethodTypes.SIP2.toString()))
         {
             return new SIP2Responder(command, debugMode);
         }
         else if (configRequestedService.equalsIgnoreCase(ResponderMethodTypes.LOCAL_CALL.toString()))
         {
             return new APIResponder(command, debugMode);
         }
         else
         {
             throw new UnsupportedResponderException(configRequestedService + 
                     " can't respond to request " + 
                     command.getCommandType().name());
         }
     }
 }
