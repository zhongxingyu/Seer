 /**
  * JBoss, Home of Professional Open Source
  *
  * Distributable under LGPL license.
  * See terms of license at gnu.org.
  */
 package org.jboss.jms.server.destination;
 
 import org.jboss.system.ServiceMBeanSupport;
 import org.jboss.jms.server.DestinationManager;
 import org.jboss.jms.server.SecurityManager;
 import org.jboss.jms.server.ServerPeer;
 import org.jboss.jms.server.plugin.contract.ChannelMapper;
 import org.w3c.dom.Element;
 
 import javax.management.ObjectName;
 
 /**
  * The base of a  JBoss Messaging destination service. Both deployed or programatically created
  * destinations will eventually get one of these.
  *
  * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
  * @version <tt>$Revision$</tt>
  *
  * $Id$
  */
 public abstract class DestinationServiceSupport extends ServiceMBeanSupport
 {
    // Constants -----------------------------------------------------
 
    // Static --------------------------------------------------------
    
    // Attributes ----------------------------------------------------
 
    protected ObjectName serverPeerObjectName;
    protected DestinationManager dm;
    protected ChannelMapper cm;
    protected SecurityManager sm;
    protected Element securityConfig;
 
    protected String name;
    protected String jndiName;
 
    protected boolean started = false;
    private boolean createdProgrammatically = false;
 
    // Constructors --------------------------------------------------
 
    public DestinationServiceSupport(boolean createdProgrammatically)
    {
       this.createdProgrammatically = createdProgrammatically;
    }
 
    // ServiceMBeanSupport overrides ---------------------------------
 
    public synchronized void startService() throws Exception
    {
       try
       {
          
          started = true;
    
          if (serviceName != null)
          {
             name = serviceName.getKeyProperty("name");
          }
    
          if (name == null || name.length() == 0)
          {
             throw new IllegalStateException( "The " + (isQueue() ? "queue" : "topic") +
                                              " name was not properly set in the service's ObjectName");
          }
    
          ServerPeer serverPeer = (ServerPeer)server.getAttribute(serverPeerObjectName, "Instance");
          dm = serverPeer.getDestinationManager();
          sm = serverPeer.getSecurityManager();
          cm = serverPeer.getChannelMapperDelegate();
    
          jndiName = dm.registerDestination(isQueue(), name, jndiName, securityConfig);
          cm.deployCoreDestination(isQueue(), name, serverPeer.getMessageStoreDelegate(), serverPeer.getPersistenceManagerDelegate());
    
          log.info(this + " started");
       }
       catch (Exception e)
       {
          log.error("Failed to start service", e);
          throw e;
       }
    }
 
    public void stopService() throws Exception
    {
       try
       {
          dm.unregisterDestination(isQueue(), name);
          cm.undeployCoreDestination(isQueue(), name);
          started = false;
          log.info(this + " stopped");
       }
       catch (Exception e)
       {
          log.error("Failed to stop service", e);
          throw e;
       }
    }
 
    // JMX managed attributes ----------------------------------------
 
    public String getJNDIName()
    {
       return jndiName;
    }
 
    public void setJNDIName(String jndiName)
    {
       if (started)
       {
          log.warn("Cannot change the value of the JNDI name after initialization!");
          return;
       }
 
       this.jndiName = jndiName;
    }
 
    public void setServerPeer(ObjectName on)
    {
       if (started)
       {
          log.warn("Cannot change the value of associated " +
                   "server's ObjectName after initialization!");
          return;
       }
 
       serverPeerObjectName = on;
    }
 
    public ObjectName getServerPeer()
    {
       return serverPeerObjectName;
    }
 
    public void setSecurityConfig(Element securityConfig) throws Exception
    {
       // push security update to the server
       if (sm != null)
       {
          sm.setSecurityConfig(isQueue(), name, securityConfig);
       }
 
       this.securityConfig = securityConfig;
    }
 
    public Element getSecurityConfig()
    {
       return securityConfig;
    }
 
    public String getName()
    {
       return name;
    }
 
    public boolean isCreatedProgrammatically()
    {
       return createdProgrammatically;
    }
 
    // JMX managed operations ----------------------------------------
 
    // TODO implement the following:
 
 //   void removeAllMessages() throws Exception;
 //
 //   public MessageCounter[] getMessageCounter();
 //
 //   public MessageStatistics[] getMessageStatistics() throws Exception;
 //
 //   public String listMessageCounter();
 //
 //   public void resetMessageCounter();
 //
 //   public String listMessageCounterHistory();
 //
 //   public void resetMessageCounterHistory();
 //
 //   public void setMessageCounterHistoryDayLimit( int days );
 //
 //   public int getMessageCounterHistoryDayLimit();
 //
 //   public int getMaxDepth();
 //
 //   public void setMaxDepth(int depth);
 //
 //   public boolean getInMemory();
 //
 //   public void setInMemory(boolean mode);
 //
 //   public int getRedeliveryLimit();
 //
 //   public void setRedeliveryLimit(int limit);
 //
 //   public long getRedeliveryDelay();
 //
 //   public void setRedeliveryDelay(long rDelay);
 //
 //   public Class getReceiversImpl();
 //
 //   public void setReceiversImpl(Class receivers);
 //
 //   public int getRecoveryRetries();
 //
 //   public void setRecoveryRetries(int retries);
 
    // Public --------------------------------------------------------
 
    public String toString()
    {
       String nameFromJNDI = jndiName;
       int idx = -1;
       if (jndiName != null)
       {
         jndiName.lastIndexOf('/');
       }
       if (idx != -1)
       {
          nameFromJNDI = jndiName.substring(idx + 1);
       }
       StringBuffer sb = new StringBuffer();
       if (isQueue())
       {
          sb.append("Queue");
       }
       else
       {
          sb.append("Topic");
       }
       sb.append('[');
       if (name.equals(nameFromJNDI))
       {
          sb.append(jndiName);
       }
       else
       {
          sb.append(jndiName).append(", name=").append(name);
       }
       sb.append(']');
       return sb.toString();
    }
 
    // Package protected ---------------------------------------------
 
    // Protected -----------------------------------------------------
 
    protected abstract boolean isQueue();
 
    // Private -------------------------------------------------------
 
    // Inner classes -------------------------------------------------
 }
