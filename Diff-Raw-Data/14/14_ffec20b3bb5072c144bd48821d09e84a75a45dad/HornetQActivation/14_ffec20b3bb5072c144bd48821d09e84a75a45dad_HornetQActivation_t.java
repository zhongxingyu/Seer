 /*
  * Copyright 2009 Red Hat, Inc.
  * Red Hat licenses this file to you under the Apache License, version
  * 2.0 (the "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *    http://www.apache.org/licenses/LICENSE-2.0
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  * implied.  See the License for the specific language governing
  * permissions and limitations under the License.
  */
 package org.hornetq.ra.inflow;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.jms.Destination;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.Queue;
 import javax.jms.Topic;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.resource.ResourceException;
 import javax.resource.spi.endpoint.MessageEndpointFactory;
 import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
 import javax.resource.spi.work.WorkManager;
 
 import org.hornetq.api.core.SimpleString;
 import org.hornetq.api.core.client.ClientSession;
 import org.hornetq.jms.client.HornetQConnectionFactory;
 import org.hornetq.jms.client.HornetQDestination;
 import org.hornetq.api.jms.HornetQJMSClient;
 import org.hornetq.core.logging.Logger;
 import org.hornetq.ra.HornetQResourceAdapter;
 import org.hornetq.ra.Util;
 
 /**
  * The activation.
  *
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @author <a href="jesper.pedersen@jboss.org">Jesper Pedersen</a>
  * @author <a href="mailto:andy.taylor@jboss.org">Andy Taylor</a>
  * @version $Revision: $
  */
 public class HornetQActivation
 {
    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(HornetQActivation.class);
 
    /**
     * Trace enabled
     */
    private static boolean trace = HornetQActivation.log.isTraceEnabled();
 
    /**
     * The onMessage method
     */
    public static final Method ONMESSAGE;
 
    /**
     * The resource adapter
     */
    private final HornetQResourceAdapter ra;
 
    /**
     * The activation spec
     */
    private final HornetQActivationSpec spec;
 
    /**
     * The message endpoint factory
     */
    private final MessageEndpointFactory endpointFactory;
 
    /**
     * Whether delivery is active
     */
    private final AtomicBoolean deliveryActive = new AtomicBoolean(false);
 
    /**
     * The destination type
     */
    private boolean isTopic = false;
 
    /**
     * Is the delivery transacted
     */
    private boolean isDeliveryTransacted;
 
    private HornetQDestination destination;
    
    /** The name of the temporary subscription name that all the sessions will share */
    private SimpleString topicTemporaryQueue;
 
    private final List<HornetQMessageHandler> handlers = new ArrayList<HornetQMessageHandler>();
 
    private HornetQConnectionFactory factory;
 
    static
    {
       try
       {
          ONMESSAGE = MessageListener.class.getMethod("onMessage", new Class[] { Message.class });
       }
       catch (Exception e)
       {
          throw new RuntimeException(e);
       }
    }
 
    /**
     * Constructor
     *
     * @param ra              The resource adapter
     * @param endpointFactory The endpoint factory
     * @param spec            The activation spec
     * @throws ResourceException Thrown if an error occurs
     */
    public HornetQActivation(final HornetQResourceAdapter ra,
                             final MessageEndpointFactory endpointFactory,
                             final HornetQActivationSpec spec) throws ResourceException
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("constructor(" + ra + ", " + endpointFactory + ", " + spec + ")");
       }
 
       this.ra = ra;
       this.endpointFactory = endpointFactory;
       this.spec = spec;
       try
       {
          isDeliveryTransacted = endpointFactory.isDeliveryTransacted(HornetQActivation.ONMESSAGE);
       }
       catch (Exception e)
       {
          throw new ResourceException(e);
       }
    }
 
    /**
     * Get the activation spec
     *
     * @return The value
     */
    public HornetQActivationSpec getActivationSpec()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("getActivationSpec()");
       }
 
       return spec;
    }
 
    /**
     * Get the message endpoint factory
     *
     * @return The value
     */
    public MessageEndpointFactory getMessageEndpointFactory()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("getMessageEndpointFactory()");
       }
 
       return endpointFactory;
    }
 
    /**
     * Get whether delivery is transacted
     *
     * @return The value
     */
    public boolean isDeliveryTransacted()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("isDeliveryTransacted()");
       }
 
       return isDeliveryTransacted;
    }
 
    /**
     * Get the work manager
     *
     * @return The value
     */
    public WorkManager getWorkManager()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("getWorkManager()");
       }
 
       return ra.getWorkManager();
    }
 
    /**
     * Is the destination a topic
     *
     * @return The value
     */
    public boolean isTopic()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("isTopic()");
       }
 
       return isTopic;
    }
 
    
    /**
     * Start the activation
     *
     * @throws ResourceException Thrown if an error occurs
     */
    public void start() throws ResourceException
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("start()");
       }
       deliveryActive.set(true);
       ra.getWorkManager().scheduleWork(new SetupActivation());
    }
    
    /**
     * @return the topicTemporaryQueue
     */
    public SimpleString getTopicTemporaryQueue()
    {
       return topicTemporaryQueue;
    }
 
    /**
     * @param topicTemporaryQueue the topicTemporaryQueue to set
     */
    public void setTopicTemporaryQueue(SimpleString topicTemporaryQueue)
    {
       this.topicTemporaryQueue = topicTemporaryQueue;
    }
 
    /**
     * Stop the activation
     */
   public void stop()
    {
       if (HornetQActivation.trace)
       {
          HornetQActivation.log.trace("stop()");
       }
 
       deliveryActive.set(false);
      try
      {
         ra.getWorkManager().scheduleWork(new TearDownActivation());
      }
      catch (WorkException e)
      {
         log.warn("exception while scheduling activation teardown " + this, e);
      }
    }
 
    /**
     * Setup the activation
     *
     * @throws Exception Thrown if an error occurs
     */
    protected synchronized void setup() throws Exception
    {
       HornetQActivation.log.info("Setting up " + spec);
 
       setupCF();
 
       setupDestination();
       for (int i = 0; i < spec.getMaxSessionInt(); i++)
       {
          ClientSession session = setupSession();
 
          HornetQMessageHandler handler = new HornetQMessageHandler(this, session, i);
          handler.setup();
          session.start();
          handlers.add(handler);
       }
 
       HornetQActivation.log.info("Setup complete " + this);
    }
 
    /**
     * Teardown the activation
     */
    protected synchronized void teardown()
    {
       HornetQActivation.log.info("Tearing down " + spec);
 
       for (HornetQMessageHandler handler : handlers)
       {
          handler.teardown();
       }
       if (spec.isHasBeenUpdated())
       {
          factory.close();
          factory = null;
       }
       HornetQActivation.log.info("Tearing down complete " + this);
    }
 
    protected void setupCF() throws Exception
    {
       if (spec.isHasBeenUpdated())
       {
          factory = ra.createHornetQConnectionFactory(spec);
       }
       else
       {
          factory = ra.getDefaultHornetQConnectionFactory();
       }
    }
 
    /**
     * Setup a session
     * @return The connection
     * @throws Exception Thrown if an error occurs
     */
    protected ClientSession setupSession() throws Exception
    {
       ClientSession result = null;
 
       try
       {
          result = ra.createSession(factory.getCoreFactory(),
                                    spec.getAcknowledgeModeInt(),
                                    spec.getUser(),
                                    spec.getPassword(),
                                    ra.getPreAcknowledge(),
                                    ra.getDupsOKBatchSize(),
                                    ra.getTransactionBatchSize(),
                                    isDeliveryTransacted,
                                    spec.isUseLocalTx());
 
          HornetQActivation.log.debug("Using queue connection " + result);
 
          return result;
       }
       catch (Throwable t)
       {
          try
          {
             if (result != null)
             {
                result.close();
             }
          }
          catch (Exception e)
          {
             HornetQActivation.log.trace("Ignored error closing connection", e);
          }
          if (t instanceof Exception)
          {
             throw (Exception)t;
          }
          throw new RuntimeException("Error configuring connection", t);
       }
    }
 
    public SimpleString getAddress()
    {
       return destination.getSimpleAddress();
    }
 
    protected void setupDestination() throws Exception
    {
 
       String destinationName = spec.getDestination();
 
       if (spec.isUseJNDI())
       {
          Context ctx = new InitialContext();
          HornetQActivation.log.debug("Using context " + ctx.getEnvironment() + " for " + spec);
          if (HornetQActivation.trace)
          {
             HornetQActivation.log.trace("setupDestination(" + ctx + ")");
          }
 
          String destinationTypeString = spec.getDestinationType();
          if (destinationTypeString != null && !destinationTypeString.trim().equals(""))
          {
             HornetQActivation.log.debug("Destination type defined as " + destinationTypeString);
 
             Class<?> destinationType;
             if (Topic.class.getName().equals(destinationTypeString))
             {
                destinationType = Topic.class;
                isTopic = true;
             }
             else
             {
                destinationType = Queue.class;
             }
 
             HornetQActivation.log.debug("Retrieving destination " + destinationName +
                                         " of type " +
                                         destinationType.getName());
             try
             {
                destination = (HornetQDestination)Util.lookup(ctx, destinationName, destinationType);
             }
             catch (Exception e)
             {
                if (destinationName == null)
                {
                   throw e;
                }
                // If there is no binding on naming, we will just create a new instance
                if (isTopic)
                {
                   destination = (HornetQDestination) HornetQJMSClient.createTopic(destinationName.substring(destinationName.lastIndexOf('/') + 1));
                }
                else
                {
                   destination = (HornetQDestination) HornetQJMSClient.createQueue(destinationName.substring(destinationName.lastIndexOf('/') + 1));
                }
             }
          }
          else
          {
             HornetQActivation.log.debug("Destination type not defined");
             HornetQActivation.log.debug("Retrieving destination " + destinationName +
                                         " of type " +
                                         Destination.class.getName());
 
             destination = (HornetQDestination)Util.lookup(ctx, destinationName, Destination.class);
             if (destination instanceof Topic)
             {
                isTopic = true;
             }
          }
       }
       else
       {
          if (Topic.class.getName().equals(spec.getDestinationType()))
          {
             destination = (HornetQDestination) HornetQJMSClient.createTopic(spec.getDestination());
          }
          else
          {
             destination = (HornetQDestination) HornetQJMSClient.createQueue(spec.getDestination());
          }
       }
 
       HornetQActivation.log.debug("Got destination " + destination + " from " + destinationName);
    }
 
    /**
     * Get a string representation
     *
     * @return The value
     */
    @Override
    public String toString()
    {
       StringBuffer buffer = new StringBuffer();
       buffer.append(HornetQActivation.class.getName()).append('(');
       buffer.append("spec=").append(spec.getClass().getName());
       buffer.append(" mepf=").append(endpointFactory.getClass().getName());
       buffer.append(" active=").append(deliveryActive.get());
       if (spec.getDestination() != null)
       {
          buffer.append(" destination=").append(spec.getDestination());
       }
       /*if (session != null)
       {
          buffer.append(" connection=").append(session);
       }*/
       // if (pool != null)
       // buffer.append(" pool=").append(pool.getClass().getName());
       buffer.append(" transacted=").append(isDeliveryTransacted);
       buffer.append(')');
       return buffer.toString();
    }
 
    /**
     * Handles the setup
     */
    private class SetupActivation implements Work
    {
       public void run()
       {
          try
          {
             setup();
          }
          catch (Throwable t)
          {
             HornetQActivation.log.error("Unable to start activation ", t);
          }
       }
 
       public void release()
       {
       }
    }
 
    /**
     * Handles the setup
     */
    private class TearDownActivation implements Work
    {
       public void run()
       {
          try
          {
             teardown();
          }
          catch (Throwable t)
          {
             HornetQActivation.log.error("Unable to tear down activation ", t);
          }
       }
 
       public void release()
       {
       }
    }
 }
