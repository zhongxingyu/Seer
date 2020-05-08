 /*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
 package javax.xml.registry;
 
 import java.util.Collection;
 import java.util.Properties;
 
 /** This is the abstract base class for factory classes for creating a JAXR
  * connection. A JAXR ConnectionFactory object is configured in a
  * provider-specific way to create connections with registry providers.
  * 
  * Looking Up a ConnectionFactory Using the JNDI API
  * The preferred way for a client to look up a JAXR ConnectionFactory is within
  * the Java Naming and Directory InterfaceTM (JNDI) API. A ConnectionFactory
  * object is registered with a naming service in a provider specific way, such
  * as one based on the JNDI API. This registration associates the
  * ConnectionFactory object with a logical name. When an application wants to
  * establish a connection with the provider associated with that
  * ConnectionFactory object, it does a lookup, providing the logical name.
  * The application can then use the ConnectionFactory object that is returned
  * to create a connection to the messaging provider.
  * 
  * Looking Up a ConnectionFactory Without Using the JNDI API
  * The JAXR API provides an alternative way to look up a JAXR ConnectionFactory
  * that does not require the use of the JNDI API. This is done using the
  * newInstance static method on the abstract class ConnectionFactory provided
  * in the JAXR API. The newInstance method returns a JAXR ConnectionFactory.
  * The client may indicate which factory class should be instantiated by the
  * newInstance method by defining the system property
  * javax.xml.registry.ConnectionFactoryClass. If this property is not set,
  * the JAXR provider must return a default ConnectionFactory instance.
  * 
  * @author Scott.Stark@jboss.org
  * @author Farrukh S. Najmi (javadoc)
  * @version $Revision$
  */
 public abstract class ConnectionFactory
 {
    private static final String SYS_PROP_NAME =
       "javax.xml.registry.ConnectionFactoryClass";
 
    public static ConnectionFactory newInstance() throws JAXRException
    {
       String factoryName = null;
       ConnectionFactory factory = null;
       try
       {
          // Default to scout for now
         String defaultName = "org.apache.ws.scout.registry.ConnectionFactoryImpl";
          factoryName = System.getProperty(SYS_PROP_NAME, defaultName);
          ClassLoader loader = Thread.currentThread().getContextClassLoader();
          Class factoryClass;
          try {
              factoryClass = loader.loadClass(factoryName);
          } catch (ClassNotFoundException e) {
              // Fall back to defining CL
              factoryClass = ConnectionFactory.class.getClassLoader().loadClass(factoryName);             
          }
 
          factory = (ConnectionFactory) factoryClass.newInstance();
       }
       catch(Throwable e)
       {
          throw new JAXRException("Failed to create instance of: "+factoryName, e);
       }
       return factory;
    }
 
    public ConnectionFactory()
    {
    }
 
    public abstract Connection createConnection()
       throws JAXRException;
    public abstract FederatedConnection createFederatedConnection(Collection connections)
       throws JAXRException;
 
    public abstract Properties getProperties()
       throws JAXRException;
    /** Sets the Properties used during createConnection and
     * createFederatedConnection calls.
     * Standard Connection Properties: 
     * javax.xml.registry.queryManagerURL - URL String for the query manager
     * service within the target registry provider
     * javax.xml.registry.lifeCycleManagerURL - URL String for the life cycle
     * manager service within the target registry provider. If unspecified,
     * must default to value of the queryManagerURL described above
     * javax.xml.registry.semanticEquivalences - String that allows
     * specification of semantic equivalences
     * javax.xml.registry.security.authenticationMethod - string that provides
     * a hint to the JAXR provider on the authentication method to be used when
     * authenticating with the registry provider. Possible value include but
     * are not limited to "UDDI_GET_AUTHTOKEN", "HTTP_BASIC",
     * "CLIENT_CERTIFICATE", "MS_PASSPORT"
     * javax.xml.registry.uddi.maxRows - integer that specifies the maximum
     * number of rows to be returned for find operations. This property is
     * specific for UDDI providers
     * javax.xml.registry.postalAddressScheme - String that specifies the id of
     * a ClassificationScheme that is used as the default postal address scheme
     * for this connection 
     * @param factoryProps
     * @throws JAXRException
     */ 
    public abstract void setProperties(Properties factoryProps)
       throws JAXRException;
 
 }
