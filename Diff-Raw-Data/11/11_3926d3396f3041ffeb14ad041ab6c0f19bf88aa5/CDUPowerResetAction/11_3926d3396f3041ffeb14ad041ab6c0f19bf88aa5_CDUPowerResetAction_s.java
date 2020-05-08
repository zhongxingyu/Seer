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
  * @date 4th July 2010
  */
 package au.edu.labshare.rigclient.action.reset;
 
 import au.edu.labshare.rigclient.internal.CabinetDistrubitionUnitControl;
 import au.edu.labshare.rigclient.primitive.CDUPowerController;
 import au.edu.uts.eng.remotelabs.rigclient.rig.IResetAction;
 import au.edu.uts.eng.remotelabs.rigclient.util.ConfigFactory;
 import au.edu.uts.eng.remotelabs.rigclient.util.ILogger;
 import au.edu.uts.eng.remotelabs.rigclient.util.LoggerFactory;
 
 /**
  * Reset action which reboots an outlet on a ServerTech Cabinet Distrubtion
  * Board. The configuration required for this action is:
  * <ul>
  *  <li><tt>CDU_Outlet</tt> - The outlet that is to be powercycled
  *  <li><tt>CDU_Address</tt> - The IP address or hostname to the CDU web 
  *  interface.</li>
  *  <li><tt>CDU_HTTP_Port</tt> - The port number of the CDU web server
  *  is listening on. This is optional and if it is not configured,
  *  port 80 is used.</li>
  *  <li><tt>CDU_Auth_Basic</tt> - The HTTP basic authentication header. 
  *  This is the user name and password concatenated by the ':' character and
 *  converted to a base64 string.</li>
  *  <li><tt>CDU_C0_Cookie</tt> - The value of the web interface cookie 
  *  with name 'c0'. Should have a value like 'FF00FF00FF000000FF00FF00FF000000'</li>
  * </ul> 
  * The {@link CDUPowerController} uses the same configuration properties
  * and may be used in combination with this class.
  * @see CDUPowerController
  */
 public class CDUPowerResetAction implements IResetAction
 {
     /** CDU control class. */
     private CabinetDistrubitionUnitControl control;
     
     /** Outlet to control. */
     private String outlet;
     
     /** Reason the previous invocation of reset failed. */
     private String failureReason;
     
     /** Logger. */
     private ILogger logger;
     
     public CDUPowerResetAction()
     {
         String auth, address, c0;
         int port;
         
         this.logger = LoggerFactory.getLoggerInstance();
         
         if ((address = ConfigFactory.getInstance().getProperty("CDU_Address")) == null)
         {
             this.logger.error("Unable to initialise the CDUPowerController. The property 'CDU_Address' was not found. " +
                     "This must be configured with the network address of the CDU board.");
         }
         else this.logger.info("Loaded CDU network address is: " + address + '.');
         
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
         }
         else this.logger.info("Loaded CDU Auth Basic header is '" + auth + "'.");
         
         if ((c0 = ConfigFactory.getInstance().getProperty("CDU_C0_Cookie")) == null)
         {
             this.logger.error("Unable to initalise the CDUPowerController. The property 'CDU_C0_Cookie' was not " +
                     "found. This must be configured with the C0 cookie of the board (e.g. FF00FF00FF000000FF00FF00FF000000).");
         }
         else this.logger.info("Loaded CDU C0 cookie is '" + c0 + "'.");
         
         if ((this.outlet = ConfigFactory.getInstance().getProperty("CDU_Outlet")) == null)
         {
             this.logger.error("Unable to initalise the CDUPowerController. The property 'CDU_Outlet' was not " +
                     "found. This must be configured with the outlet name or identifier.");
         }
         else this.logger.info("Loaded CDU outlet is '" + this.outlet + "'.");
         
         if (address == null || auth == null || this.outlet == null || c0 == null)
         {
             throw new IllegalArgumentException();
         }
         
         this.control = new CabinetDistrubitionUnitControl(address, port, auth, c0);
     }
     
 
     @Override
     public boolean reset()
     {
         try
         {
             this.logger.debug("Rebooting socket '" + this.outlet + "'.");
             this.control.reboot(this.outlet);
             this.failureReason = null;
             return true;
         }
         catch (Exception e)
         {
             this.failureReason = "Failed to reboot outlet, because of exception '" + e.getClass().getSimpleName() + 
             "', message '" + e.getMessage() + "'.";
             this.logger.error(this.failureReason);
         }
         
         return false;
     }
     
     @Override
     public String getFailureReason()
     {
         return this.failureReason;
     }
 
     @Override
     public String getActionType()
     {
         return "CDU Power Reset";
     }
 }
