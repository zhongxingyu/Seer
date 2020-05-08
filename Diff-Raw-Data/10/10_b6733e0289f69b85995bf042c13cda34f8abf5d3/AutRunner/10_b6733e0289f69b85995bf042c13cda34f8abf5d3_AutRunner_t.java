 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.autrun;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jubula.app.autrun.i18n.Messages;
 import org.eclipse.jubula.autagent.commands.IStartAut;
 import org.eclipse.jubula.communication.connection.ConnectionState;
 import org.eclipse.jubula.communication.connection.RestartAutProtocol;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.registration.AutIdentifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author BREDEX GmbH
  * @created Sept 07, 2011
  */
 public class AutRunner {
 
     /** response OK when thread was started */
     private static final String RESPONSE_OK = "Response.OK"; //$NON-NLS-1$
 
     /** the thread name */
     private static final String AGENT_CONNECTION_THREAD_NAME = "AUT Agent Connection"; //$NON-NLS-1$
 
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(AutRunner.class);
 
     /** settings used to start the AUT */
     private Map<String, Object> m_autConfiguration;
     
     /** the object responsible for actually starting the AUT */
     private IStartAut m_startAut;
 
     /** the address for the AUT Agent */
     private InetSocketAddress m_agentAddr;
     
 
     /**
      * Constructor
      * 
      * @param autToolkit Toolkit for the AUT managed by this instance.
      * @param autIdentifier Identifier for the AUT managed by this instance.
      * @param agentAddr Address of the Aut Agent with which the AUT should be
      *                  registered.
      * @param autConfiguration Properties required for starting the AUT.
      * 
      * @throws ClassNotFoundException If no class can be found for starting an
      *                                AUT for the given toolkit.
      * @throws InstantiationException 
      * @throws IllegalAccessException
      */
     public AutRunner(String autToolkit, AutIdentifier autIdentifier, 
             InetSocketAddress agentAddr, Map<String, Object> autConfiguration) 
         throws ClassNotFoundException, InstantiationException, 
                IllegalAccessException {
         String className = "org.eclipse.jubula.autagent.commands.Start" //$NON-NLS-1$
             + autToolkit + "AutServerCommand"; //$NON-NLS-1$
         Class< ? > autServerClass = Class.forName(className);
         m_agentAddr = agentAddr;
         m_autConfiguration = new HashMap<String, Object>(autConfiguration);
         m_autConfiguration.put(AutConfigConstants.AUT_AGENT_HOST, 
                 agentAddr.getHostName());
         m_autConfiguration.put(AutConfigConstants.AUT_AGENT_PORT, 
                 String.valueOf(agentAddr.getPort()));
         m_autConfiguration.put(AutConfigConstants.AUT_NAME, 
                 autIdentifier.getExecutableName());
         m_startAut = (IStartAut)autServerClass.newInstance();
     }
     
     /**
      * Starts the AUT managed by the receiver.
      * 
      * @throws ConnectException If unable to connect to the AUT Agent (if, 
      *         for example, there is no AUT Agent running on the given 
      *         hostname / port)
      * @throws IOException if an I/O error occurs during AUT startup.
      */
     public void run() throws IOException, ConnectException {
         // Establish connection to AUT Agent
        if (m_agentAddr.getAddress() == null) {
            LOG.error(Messages.errorAutAgentHost);
            throw new ConnectException(Messages.errorAutAgentHost);
        }
         final Socket agentSocket = 
             new Socket(m_agentAddr.getAddress(), m_agentAddr.getPort());
 
         final PrintWriter writer = new PrintWriter(
                 agentSocket.getOutputStream(), true);
         final BufferedReader reader = new BufferedReader(
                 new InputStreamReader(agentSocket.getInputStream()));
         
         // wait for the "Client Type Request" message
         reader.readLine();
         writer.println(ConnectionState.CLIENT_TYPE_AUTRUN);
         
         writer.println(
                 m_autConfiguration.get(AutConfigConstants.AUT_NAME));
         
         Thread agentConnectionThread = new Thread
         (AGENT_CONNECTION_THREAD_NAME) {
             public void run() {
                 try {
                     String line = reader.readLine();
                     if (line != null) {
                         if (line.equals(
                                 RestartAutProtocol.REQ_PREPARE_FOR_RESTART)) {
                             
                             // make sure that we have a system thread running so
                             // the JVM won't shut down during AUT restart
                             Thread restartThread = new Thread() {
                                 public void run() {
                                     writer.println(RESPONSE_OK);
                                     
                                     try {
                                         String restartReq = reader.readLine();
                                         if (RestartAutProtocol.REQ_RESTART
                                             .equals(restartReq)) {
                                             
                                             AutRunner.this.run();
                                         }
                                     } catch (IOException e) {
                                         LOG.error(Messages.restartAutFailed, e);
                                     } finally {
                                         try {
                                             agentSocket.close();
                                         } catch (IOException e) {
                                             // Error while closing socket. Ignore.
                                         }
                                     }
                                 }
                             };
 
                             restartThread.setDaemon(false);
                             restartThread.start();
                         }
                     }
                 } catch (IOException e) {
                     LOG.error(Messages.restartAutFailed, e);
                 }
             }
         };
 
         agentConnectionThread.setDaemon(true);
         agentConnectionThread.start();
         
         m_startAut.startAut(m_autConfiguration);
     }
 
 }
