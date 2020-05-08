 /*
  * Metro allows customers from any affiliate library to join any other member library.
  *    Copyright (C) 2013  Edmonton Public Library
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
 
 import api.Command;
 import api.CommandStatus;
 import mecard.requestbuilder.ILSRequestBuilder;
 import java.util.Date;
 import mecard.config.CustomerFieldTypes;
 import mecard.customer.Customer;
 import mecard.customer.CustomerFormatter;
 import mecard.exception.ConfigurationException;
 import mecard.exception.MalformedCommandException;
 import mecard.exception.MetroSecurityException;
 import mecard.exception.UnsupportedCommandException;
 import mecard.exception.DummyException;
 import site.CustomerLoadNormalizer;
 import site.MeCardPolicy;
 
 /**
  *
  * @author Andrew Nisbet
  */
 public class Responder
 {
     private final static String SIP_AUTHORIZATION_FAILURE = "AFInvalid PIN";
     protected Request request;
     protected final boolean debug;
     
     /**
      *
      * @param cmd the value of cmd
      * @param debugMode the value of debugMode
      */
     public Responder(Request cmd, boolean debugMode)
     {
         this.debug = debugMode;
         this.request = cmd;
         if (debug)
         {
             System.out.println("CMD:\n  '"+request.toString()+"' '"+request.getCommandType().name()+"'");
             System.out.println("ELE:");
             System.out.println("  S:" + request.toString()+ ",");
         }
     }
     
     /**
      * Creates canned exception responses if something goes wrong.
      *
      * @param ex the exception thrown
      * @return String value of the response with code.
      */
     public static Response getExceptionResponse(RuntimeException ex)
     {
         Response response = new Response(ResponseTypes.UNKNOWN);
         if (ex instanceof MetroSecurityException)
         {
             response = new Response(ResponseTypes.UNAUTHORIZED);
         }
         else if (ex instanceof MalformedCommandException)
         {
             response = new Response(ResponseTypes.ERROR);
         }
         else if (ex instanceof ConfigurationException)
         {
             response = new Response(ResponseTypes.CONFIG_ERROR);
         }
         else if (ex instanceof UnsupportedCommandException)
         {
             response = new Response(ResponseTypes.UNKNOWN);
             response.setResponse("Command not implemented, make sure your server is up to date.");
         }
         else if (ex instanceof DummyException)
         {
             response = new Response(ResponseTypes.CONFIG_ERROR);
             response.setResponse("TEST: DummyCommand intentionally threw error.");
         }
         
         response.setResponse(ex.getMessage());
         return response;
     }
     
     /**
      * 
      * @return the Response of the command.
      */
     public Response getResponse()
     {
         // test for the operations that this responder is capable of performing
         // SIP can't create customers, BImport can't query customers.
         Response response = new Response();
         switch (request.getCommandType())
         {
             case CREATE_CUSTOMER:
                 createCustomer(response);
                 break;
             case UPDATE_CUSTOMER:
                 updateCustomer(response);
                 break;
             case GET_CUSTOMER:
                 getCustomer(response);
                 break;
             case GET_STATUS:
                 getILSStatus(response);
                 break;
             case NULL:
                 response.setCode(ResponseTypes.OK);
                 response.setResponse("null query back at you.");
                 break;
             default:
                 response.setCode(ResponseTypes.ERROR);
                 response.setResponse(Responder.class.getName() + " cannot " + request.toString());
         }
         return response;
     }
     
     /**
      * Creates and executes the "get customer" command, then 
      * executes the command, and populates the argument Response with the customer
      * object and or a message about the status of the command.
      * @param response object as a container for the results.
      */
     public void getCustomer(Response response)
     {
         String userId  = this.request.getUserId();
         String userPin = this.request.getUserPin();
         
         // So all this stuff will be put to the SIPCommand
         ILSRequestBuilder requestBuilder = ILSRequestBuilder.getInstanceOf(QueryTypes.GET_CUSTOMER, debug);
         Command command = requestBuilder.getCustomerCommand(userId, userPin, response);
         CommandStatus status = command.execute();
         CustomerFormatter customerFormatter = requestBuilder.getFormatter();
         Customer customer = customerFormatter.getCustomer(status.getStdout());
         response.setCustomer(customer);
         requestBuilder.isSuccessful(QueryTypes.GET_CUSTOMER, status, response);
         // SIPFormatter() will place AF message in the reserve field. If it is not "OK"
         // then interpretResults() further sets ISVALID to Protocol.FALSE.
         if (customer.get(CustomerFieldTypes.ISVALID).compareTo(Protocol.FALSE) == 0)
         {
             response.setCustomer(null);
             System.out.println(new Date() + " GET__STDOUT:"+status.getStdout());
             System.out.println(new Date() + " GET__STDERR:"+status.getStderr());
             return;
         }
         // You have this before the test metro requirements b/c it checks for PIN
         // and SIP2 does not return the pin.
         customer.set(CustomerFieldTypes.PIN, userPin);
         if (meetsMeCardRequirements(customer, status.getStdout()))
         {
             response.setCode(ResponseTypes.OK);
         }
         else
         {
             // this can happen if the user is barred, underage, non-resident, reciprocol, lostcard.
             response.setResponse("there is a problem with your account, please contact your home library for assistance");
             response.setCode(ResponseTypes.FAIL);
            response.setCustomer(null);
         }
         System.out.println(new Date() + " GET__STDOUT:"+status.getStdout());
         System.out.println(new Date() + " GET__STDERR:"+status.getStderr());
     }
     
     /**
      * Gets the status of the ILS server.
      * @param response
      */
     public void getILSStatus(Response response)
     {
         ILSRequestBuilder sipRequestBuilder = ILSRequestBuilder.getInstanceOf(QueryTypes.GET_STATUS, debug);
         Command sipCommand = sipRequestBuilder.getStatusCommand(response);
         CommandStatus status = sipCommand.execute();
         sipRequestBuilder.isSuccessful(QueryTypes.GET_STATUS, status, response);
         System.out.println(new Date() + " STAT_STDOUT:"+status.getStdout());
         System.out.println(new Date() + " STAT_STDERR:"+status.getStderr());
     }
     
     /**
      * Converts the customer into a ILS-meaningful expression to create a 
      * customer, then executes the command, and populates the argument 
      * response with the results.
      * 
      * @param response object
      */
     public void createCustomer(Response response)
     {
         Customer customer = request.getCustomer();
         normalizeBeforeCustomerLoad(response, customer);
         ILSRequestBuilder requestBuilder = ILSRequestBuilder.getInstanceOf(QueryTypes.CREATE_CUSTOMER, debug);
         Command command = requestBuilder.getCreateUserCommand(customer, response);
         CommandStatus status = command.execute();
         if (requestBuilder.isSuccessful(QueryTypes.CREATE_CUSTOMER, status, response) == false)
         {
             throw new ConfigurationException();
         }
         System.out.println(new Date() + " CRAT_STDOUT:"+status.getStdout());
         System.out.println(new Date() + " CRAT_STDERR:"+status.getStderr());
     }
 
     /**
      * Creates the ILS specific command to run to update a customer account, then
      * runs it and places the results into the response object.
      * @param response 
      */
     public void updateCustomer(Response response)
     {
         Customer customer = request.getCustomer();
         normalizeBeforeCustomerLoad(response, customer);
         ILSRequestBuilder requestBuilder = ILSRequestBuilder.getInstanceOf(QueryTypes.UPDATE_CUSTOMER, debug);
         Command command = requestBuilder.getUpdateUserCommand(customer, response);
         CommandStatus status = command.execute();
         if (requestBuilder.isSuccessful(QueryTypes.UPDATE_CUSTOMER, status, response) == false)
         {
             throw new ConfigurationException();
         }
         System.out.println(new Date() + " UPDT_STDOUT:"+status.getStdout());
         System.out.println(new Date() + " UPDT_STDERR:"+status.getStderr());
     }
 
     /**
      * Normalizes information from melibraries.ca into a format that the local ILS
      * can handle. Example: some libraries can only accept 4 digit pins. The 
      * rules for making that happen start here. Once completed the response
      * object will contain the changes that were made. In our example an 
      * explanation of that the over-sized PIN was truncated and what the new value
      * is can be added.
      * @param response
      * @param customer 
      */
     public void normalizeBeforeCustomerLoad(Response response, Customer customer)
     {
         if (customer == null)
         {
             return;
         }
         CustomerLoadNormalizer normalizer = CustomerLoadNormalizer.getInstanceOf(debug);
         String changes = normalizer.normalize(customer);
         response.setResponse(changes);
     }
     
     /**
      * Tests if the customer meets the required MeCard requirements. MeCard 
      * users must be:
      * <ul>
      * <li>Over the age of 18</li>
      * <li>Must to be a reciprocal customer at the home library.</li>
      * <li>Must be in good standing at their home library.</li>
      * <li>Must be a resident of the home library's service area.</li>
      * <li>Must have a valid expiry date.</li>
      * <li>Must have mandatory account fields filled with valid data.</li>
      * </ul>
      * @param customer
      * @param additionalData
      * @return true if the customer meets the MeCard participation requirements
      * and false otherwise.
      */
     protected boolean meetsMeCardRequirements(Customer customer, String additionalData)
     {
         if (customer == null || additionalData == null)
         {
             return false;
         }
         MeCardPolicy policy = MeCardPolicy.getInstanceOf(this.debug);
         // If everything goes well we expect the customer data to be sent back
         // to be loaded. Some libraries use case to distinguish customers, you
         // know who you are, so we standardize important fields for loading on 
         // a regular ILS.
         policy.normalizeCustomerFields(customer);
         if (policy.isEmailable(customer, additionalData) == false) 
         {
             System.out.println("Customer not emailable.");
             return false;
         }
         if (policy.isInGoodStanding(customer, additionalData) == false)
         {
             System.out.println("Customer not in good standing.");
             return false;
         }
         if (policy.isMinimumAge(customer, additionalData) == false)
         {
             System.out.println("Customer not minimum age.");
             return false;
         }
         if (policy.isReciprocal(customer, additionalData))
         {
             System.out.println("Customer cannot join because they are a reciprocal customer.");
             return false;
         } // reciprocals not allowed.
         if (policy.isResident(customer, additionalData) == false) 
         {
             System.out.println("Customer is not resident.");
             return false;
         }
         if (policy.isValidCustomerData(customer) == false) 
         {
             System.out.println("Customer's data is not valid.");
             return false;
         }
         if (policy.isValidExpiryDate(customer, additionalData) == false) 
         {
             System.out.println("Customer does not have a valid privilege date.");
             return false;
         }
         if (policy.isLostCard(customer, additionalData)) 
         {
             System.out.println("Customer's card reported as lost.");
             return false;
         }
    
         System.out.println("Customer cleared.");
         return true;
     }
     
     /**
      * Tests if the request was valid or not based on whether the supplied PIN
      * matched the user's pin.
      *
      * @param suppliedPin
      * @param customer the value of customer
      * @return true if the user is authorized and false otherwise.
      */
     public boolean isAuthorized(String suppliedPin, Customer customer)
     {
         // TODO this should be moved to the appropriate RequestBuilder.
         if (suppliedPin.contains(SIP_AUTHORIZATION_FAILURE))
         {
             return false;
         }
         return true;
     }
 }
