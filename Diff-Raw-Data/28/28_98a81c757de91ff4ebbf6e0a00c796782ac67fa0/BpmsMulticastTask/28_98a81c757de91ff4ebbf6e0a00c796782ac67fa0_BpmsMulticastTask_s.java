 /*
  * Copyright 2013 Roland Gisler, GISLER iNFORMATiK, Switzerland.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package ch.gitik.bpms.ant;
 
 import java.io.IOException;
 import java.net.UnknownHostException;
 
import org.apache.tools.ant.BuildException;

 import ch.gitik.bpms.common.ConfigException;
 import ch.gitik.bpms.common.Message;
 import ch.gitik.bpms.multicast.MulticastConfig;
 import ch.gitik.bpms.multicast.MulticastSender;
 import ch.gitik.bpms.multicast.MulticastSenderFactory;
 
 /**
  * ANT-Task fuer BPMS (Build Process Messaging System). Dieser ANT-Task kann in
  * ein build.xml eingebunden werden und versendet dann Messages an interessierte
  * Benutzer ueber das Multicast Protokoll.
  * @author Roland Gisler
  */
 public class BpmsMulticastTask extends AbstractBpmsTask {
 
    /** Servername. */
    private String multicastip = null;
 
    /** Serverport. */
    private int port = 0;
 
    /** XMPP Configuration. */
    private MulticastConfig config = null;
 
    /** JabberBean. */
    private MulticastSender multicast = null;
 
    /**
     * Check the input and throw a BuildException when it is null.
    * @throws BuildException
    *            Buildfehler
    * @throws IllegalArgumentException
    *            Fehlendes Attribut
     */
   public void checkAttributes() throws BuildException {
 
       // IP und Port sind MUST-Attribute
       if (this.multicastip == null) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + "MulticastIP not defined");
       }
       if (this.port == 0) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + "Port not defined");
       }
 
       // Konfig erzeugen, und erneut pruefen
       this.config = new MulticastConfig(this.multicastip, this.port);
       try {
          this.config.validate();
       } catch (ConfigException e) {
          throw new IllegalArgumentException(MSG_MISSING_PARA + e);
       }
    }
 
    /**
     * Liefert den Servernamen zurueck.
     * @return Name des Servers
     */
    public final String getMulticastip() {
       return multicastip;
    }
 
    /**
     * Setzt den Namen des Servers.
     * @param string
     *           Servername
     */
    public final void setMulticastip(final String string) {
       multicastip = string;
    }
 
   /**
     * @see ch.gitik.bpms.ant.AbstractBpmsTask#sendMessage(ch.gitik.bpms.common.Message)
     */
    @Override
   public void sendMessage(Message msg) {
       try {
          this.multicast.send(msg);
       } catch (UnknownHostException e) {
          e.printStackTrace();
       } catch (IOException e) {
          e.printStackTrace();
       }
    }
 
   /**
     * @see ch.gitik.bpms.ant.AbstractBpmsTask#initialize()
     */
    @Override
   public void initialize() {
       try {
          this.multicast = MulticastSenderFactory.getSender(config);
       } catch (ConfigException e) {
          e.printStackTrace();
       }
    }
 
    /**
     * Liefert den Port.
     * @return int Port.
     */
   public int getPort() {
       return port;
    }
 
    /**
     * Setzt den Port.
     * @param port
     *           int.
     */
   public void setPort(int port) {
       this.port = port;
    }
 }
