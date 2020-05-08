 /**
  * Rig Client Commons.
  *
  * @license See LICENSE in the top level directory for complete license terms.
  *
  * Copyright (c) 2010, University of Technology, Sydney
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
  * @date 28th June 2010
  */
 package au.edu.labshare.rigclient.primitive;
 
 import au.edu.labshare.rigclient.internal.CabinetDistrubitionUnitControl;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveRequest;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IRigControl.PrimitiveResponse;
 import au.edu.uts.eng.remotelabs.rigclient.rig.primitive.IPrimitiveController;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 
 /**
  * Controller for a ServerTech Cabinet Distribution Unit (CDU) network
  * powerboard. Allows a configured outlet to be turned on, off and rebooted and 
  * their power state to be determined. For this controller to work,
  * the CDU HTTP interface must be enabled. The action methods contained in 
  * this controller are:
  * <ul>
  *  <li><tt>on</tt> - Turns on the outlet.</li>
  *  <li><tt>off</tt> - Turns off the outlet.</li>
  *  <li><tt>reboot</tt> - Reboots the outlet.</li>
  *  <li><tt>status</tt> - Gets the status of the outlet. Either 'ON' or 
  *  'OFF'.</li>
  * </ul>
  * The outlet identification passed to or provided by this controller's 
  * action methods may be either the outlet name (e.g. fpga1) or the
  * outlet identifier (e.g. AA1).
  * The required configuration configuration for this controller is:
  * <ul>
  *  <li><tt>CDU_Outlet</tt> - The outlet that is to be power cycled
  *  <li><tt>CDU_Address</tt> - The IP address or hostname to the CDU web 
  *  interface.</li>
  *  <li><tt>CDU_HTTP_Port</tt> - The port number of the CDU web server
  *  is listening on. This is optional and if it is not configured,
  *  port 80 is used.</li>
  *  <li><tt>CDU_Auth_Basic</tt> - The HTTP basic authentication header. 
  *  This is the user name and password concatenated by the ':' character and
 *  converted to a base64 string. Should have a value like 
 *  '<code>Basic Zm9vOmJhcgo=</code>'</li>
  *  <li><tt>CDU_C0_Cookie</tt> - The value of the web interface cookie 
  *  with name 'c0'. Should have a value like 'FF00FF00FF000000FF00FF00FF000000'</li>
  * </ul> 
  */
 public class CDUPowerController implements IPrimitiveController
 {
     /** CDU control class. */
     private CabinetDistrubitionUnitControl control;
     
     /** Outlet to control. */
     private String outlet;
     
     /** Logger. */
     private ILogger logger;
     
 
     @Override
     public boolean initController()
     {
         String auth, address, c0;
         int port;
         
         this.logger = LoggerFactory.getLoggerInstance();
         
         if ((address = ConfigFactory.getInstance().getProperty("CDU_Address")) == null)
         {
             this.logger.error("Unable to initialise the CDUPowerController. The property 'CDU_Address' was not found. " +
             		"This must be configured with the network address of the CDU board.");
             return false;
         }
         this.logger.info("Loaded CDU network address is: " + address + '.');
         
         try
         {
             port = Integer.parseInt(ConfigFactory.getInstance().getProperty("CDU_HTTP_Port", "80"));
         }
         catch (NumberFormatException ex)
         {
             port = 80;
         }
         this.logger.info("Using port " + port + " to connect to the CDU web server.");
         
         if ((auth = ConfigFactory.getInstance().getProperty("CDU_Auth_Basic")) == null)
         {
             this.logger.error("Unable to initialise the CDUPowerController. The property 'CDU_Auth_Basic' was not " +
             		"found. This must be configured with the HTTP Auth Basic request header value.");
             return false;
         }
         this.logger.info("Loaded CDU Auth Basic header is '" + auth + "'.");
         
         if ((c0 = ConfigFactory.getInstance().getProperty("CDU_C0_Cookie")) == null)
         {
             this.logger.error("Unable to initalise the CDUPowerController. The property 'CDU_C0_Cookie' was not " +
                     "found. This must be configured with the C0 cookie of the board (e.g. FF00FF00FF000000FF00FF00FF000000).");
             return false;
         }
         this.logger.info("Loaded CDU C0 cookie is '" + c0 + "'.");
         
         if ((this.outlet = ConfigFactory.getInstance().getProperty("CDU_Outlet")) == null)
         {
             this.logger.error("Unable to initalise the CDUPowerController. The property 'CDU_Outlet' was not " +
                     "found. This must be configured with the outlet name or identifier.");
             return false;
         }
         this.logger.info("Loaded CDU outlet is '" + this.outlet + "'.");
         
         this.control = new CabinetDistrubitionUnitControl(address, port, auth, c0);
         return true;
     }
 
     @Override
     public boolean preRoute()
     {
         return true;
     }
     
     /**
      * Turns on the configured outlet.
      * 
      * @param request
      * @return response
      */
     public PrimitiveResponse on(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         try
         {
             this.control.turnOn(this.outlet);
             response.setSuccessful(true);
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("Exception: " + e.getClass().getSimpleName() + ", message: '" + e.getMessage() + "'.");
         }
         return response;
     }
     
     /**
      * Turns off the configured outlet.
      * 
      * @param request
      * @return response
      */
     public PrimitiveResponse off(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         try
         {
             this.control.turnOff(this.outlet);
             response.setSuccessful(true);
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("Exception: " + e.getClass().getSimpleName() + ", message: '" + e.getMessage() + "'.");
         }
         return response;
     }
     
     /**
      * Reboots the configured outlet.
      * 
      * @param request
      * @return response
      */
     public PrimitiveResponse reboot(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         try
         {
             this.control.reboot(this.outlet);
             response.setSuccessful(true);
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("Exception: " + e.getClass().getSimpleName() + ", message: '" + e.getMessage() + "'.");
         }
         return response;
     }
     
     /**
      * Gets the status of the configured outlet. The status will be stored in
      * the parameter '<tt>status</tt>' and will contain either 'On' or 'Off'
      * if the outlet is on or off respectively.
      * 
      * @param request
      * @return response
      */
     public PrimitiveResponse status(PrimitiveRequest request)
     {
         PrimitiveResponse response = new PrimitiveResponse();
         try
         {
             response.addResult("status", this.control.isOn(this.outlet) ? "On" : "Off");
             response.setSuccessful(true);
         }
         catch (Exception e)
         {
             response.setSuccessful(false);
             response.setErrorCode(1);
             response.setErrorReason("Exception: " + e.getClass().getSimpleName() + ", message: '" + e.getMessage() + "'.");
         }
         
         return response;
     }
 
     @Override
     public boolean postRoute()
     {
         return true;
     }
 
     @Override
     public void cleanup()
     { /* Nothing to clean up. */ }
 }
