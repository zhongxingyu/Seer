 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2007, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
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
 package org.jboss.classloader.spi;
 
 import java.security.ProtectionDomain;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jboss.classloader.plugins.system.ClassLoaderSystemBuilder;
 import org.jboss.classloader.spi.base.BaseClassLoaderSystem;
 import org.jboss.logging.Logger;
 import org.jboss.util.loading.Translator;
 
 /**
  * ClassLoaderSystem.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class ClassLoaderSystem extends BaseClassLoaderSystem
 {
    /** The log */
    private static final Logger log = Logger.getLogger(ClassLoaderSystem.class);
 
    /** The name of the default domain */
    public static final String DEFAULT_DOMAIN_NAME = "<DEFAULT>";
 
    /** The class loading system builder */
    private static final ClassLoaderSystemBuilder builder = new ClassLoaderSystemBuilder();
 
    /** The default domain */
    private ClassLoaderDomain defaultDomain;
    
    /** The registered domains by name */
    private Map<String, ClassLoaderDomain> registeredDomains = new HashMap<String, ClassLoaderDomain>();
 
    /** Any translator */
    private Translator translator;
    
    /** Whether the system is shutdown */
    private boolean shutdown = false;
    
    /**
     * Get the classloading system instance
     * 
     * @return the instance
     * @throws SecurityException if the caller does not have authority to create a classloader
     */
    public static final ClassLoaderSystem getInstance()
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
          sm.checkCreateClassLoader();
       return builder.get();
    }
 
    /**
     * Get the default classloading domain
     * 
     * @return the default domain
     */
    public synchronized ClassLoaderDomain getDefaultDomain()
    {
       if (shutdown)
          throw new IllegalStateException("The classloader system is shutdown: " + toLongString());
       
       // Already constructed
       if (defaultDomain != null)
          return defaultDomain;
       
       // See if explicitly registered
       defaultDomain = registeredDomains.get(DEFAULT_DOMAIN_NAME);
       if (defaultDomain != null)
          return defaultDomain;
       
       // Create it
       defaultDomain = createDefaultDomain();
       
       // Register it
       internalRegisterDomain(DEFAULT_DOMAIN_NAME, defaultDomain);
       
       return defaultDomain;
    }
    
    /**
     * Create the default domain<p>
     * 
     * By default this just invokes {@link #createDomain(String)} with {@link #DEFAULT_DOMAIN_NAME}
     * 
     * @return the default domain
     */
    protected ClassLoaderDomain createDefaultDomain()
    {
       return createDomain(DEFAULT_DOMAIN_NAME);
    }
    
    /**
     * Create a domain
     * 
     * @param name the name of the domain
     * @return the domain
     * @throws IllegalArgumentException for a null name
     */
    protected abstract ClassLoaderDomain createDomain(String name);
    
    /**
     * Create and register a domain
     * 
     * @param name the name of the domain
     * @return the domain
     * @throws IllegalArgumentException for a null name
     * @throws IllegalStateException if there already is a domain with that name
     */
    public ClassLoaderDomain createAndRegisterDomain(String name)
    {
       return createAndRegisterDomain(name, ParentPolicy.BEFORE, null);
    }
    
    /**
     * Create and register a domain with the given parent classloading policy
     * 
     * @param name the name of the domain
     * @param parentPolicy the parent classloading policy
     * @return the domain
     * @throws IllegalArgumentException for a null name or policy
     * @throws IllegalStateException if there already is a domain with that name
     */
    public ClassLoaderDomain createAndRegisterDomain(String name, ParentPolicy parentPolicy)
    {
       return createAndRegisterDomain(name, parentPolicy, null);
    }
    
    /**
     * Create and register a domain with the given parent classloading policy
     * 
     * @param name the name of the domain
     * @param parentPolicy the parent classloading policy
     * @param parent the parent
     * @return the domain
     * @throws IllegalArgumentException for a null argument
     * @throws IllegalStateException if there already is a domain with that name
     */
    public ClassLoaderDomain createAndRegisterDomain(String name, ParentPolicy parentPolicy, Loader parent)
    {
       ClassLoaderDomain result = createDomain(name);
       result.setParentPolicy(parentPolicy);
       result.setParent(parent);
       registerDomain(result);
       return result;
    }
 
    /**
     * Get a domain
     * 
     * @param name the domain name
     * @return the domain
     * @throws IllegalArgumentException for a null name
     */
    public synchronized ClassLoaderDomain getDomain(String name)
    {
       if (name == null)
          throw new IllegalArgumentException("Null name");
 
       if (shutdown)
          throw new IllegalStateException("The classloader system is shutdown: " + toLongString());
 
       ClassLoaderDomain result = registeredDomains.get(name);
       
       // See whether this is the default domain
       if (result == null && DEFAULT_DOMAIN_NAME.equals(name))
          result = getDefaultDomain();
       
       return result;
    }
 
    /**
     * Is a domain name registered
     * 
     * @param name the domain name
     * @return true when the domain is registered
     * @throws IllegalArgumentException for a null name
     */
    public boolean isRegistered(String name)
    {
       return getDomain(name) != null;
    }
 
    /**
     * Is a domain registered
     * 
     * @param domain the domain
     * @return true when the domain is registered
     * @throws IllegalArgumentException for a null domain
     */
    public boolean isDomainRegistered(ClassLoaderDomain domain)
    {
       if (domain == null)
          throw new IllegalArgumentException("Null domain");
       return getDomain(domain.getName()) == domain;
    }
 
    /**
     * Register a domain
     * 
     * @param domain the domain
     * @throws IllegalArgumentException for a null domain
     * @throws IllegalStateException if a domain is already registered with this name
     */
    public synchronized void registerDomain(ClassLoaderDomain domain)
    {
       if (domain == null)
          throw new IllegalArgumentException("Null domain");
       
       String name = domain.getName();
       if (isRegistered(name))
          throw new IllegalStateException("A domain is already registered with name " + name);
       
       internalRegisterDomain(name, domain);
    }
 
    /**
     * Do the registration
     * 
     * @param name the name
     * @param domain the domain
     */
    private void internalRegisterDomain(String name, ClassLoaderDomain domain)
    {
       if (shutdown)
          throw new IllegalStateException("The classloader system is shutdown: " + toLongString());
 
       registeredDomains.put(name, domain);
       super.registerDomain(domain);
       
       log.debug(this + " registered domain=" + domain.toLongString());
    }
    
    /**
     * Unregister a domain
     * 
     * @param domain the domain
     * @throws IllegalArgumentException for a null domain or if you attempt to unregister the default domain
     * @throws IllegalStateException if a domain is not registered
     */
    public synchronized void unregisterDomain(ClassLoaderDomain domain)
    {
       if (isDomainRegistered(domain) == false)
          throw new IllegalStateException("Domain is not registered " + domain);
 
       if (DEFAULT_DOMAIN_NAME.equals(domain.getName()))
          throw new IllegalArgumentException("Cannot unregister the default domain");
       
       internalUnregisterDomain(domain);
    }
    
    /**
     * Unregister a domain
     * 
     * @param domain the domain
     * @throws IllegalArgumentException for a null domain or if you attempt to unregister the default domain
     * @throws IllegalStateException if a domain is not registered
     */
    private synchronized void internalUnregisterDomain(ClassLoaderDomain domain)
    {
       registeredDomains.remove(domain.getName());
       super.unregisterDomain(domain);
       
       log.debug(this + " unregistered domain=" + domain.toLongString());
    }
    
    /**
     * Register a policy with the default domain<p>
     * 
     * Equivalent to {@link #registerClassLoaderPolicy(ClassLoaderDomain, ClassLoaderPolicy)} using
     * {@link #getDefaultDomain()} as the ClassLoaderDomain
     * 
     * @param policy the policy
     * @return the classloader
     * @throws IllegalArgumentException if a parameter is null
     * @throws IllegalStateException if the policy is already registered with a domain  
     */
    public ClassLoader registerClassLoaderPolicy(ClassLoaderPolicy policy)
    {
       return registerClassLoaderPolicy(getDefaultDomain(), policy);
    }
 
    /**
     * Register a classloader policy, possibly constructing the domain with a BEFORE parent policy
     * 
     * @param domainName the domain name
     * @param policy the classloader policy
     * @return the policy
     * @throws IllegalArgumentException for a null parameter
     */
    public ClassLoader registerClassLoaderPolicy(String domainName, ClassLoaderPolicy policy)
    {
       return registerClassLoaderPolicy(domainName, ParentPolicy.BEFORE, null, policy);
    }
 
    /**
     * Register a classloader policy, possibly constructing the domain
     * 
     * @param domainName the domain name
     * @param parentPolicy the parent policy
     * @param policy the classloader policy
     * @return the policy
     * @throws IllegalArgumentException for a null parameter
     */
    public ClassLoader registerClassLoaderPolicy(String domainName, ParentPolicy parentPolicy, ClassLoaderPolicy policy)
    {
       return registerClassLoaderPolicy(domainName, parentPolicy, null, policy);
    }
 
    /**
     * Register a classloader policy, possibly constructing the domain
     * 
     * @param domainName the domain name
     * @param parentPolicy the parent policy
     * @param parentDomainName the parent domain (can be null)
     * @param policy the classloader policy
     * @return the policy
     * @throws IllegalArgumentException for a null parameter
     * @throws IllegalStateException if the parent domain does not exist
     */
    public ClassLoader registerClassLoaderPolicy(String domainName, ParentPolicy parentPolicy, String parentDomainName, ClassLoaderPolicy policy)
    {
       if (domainName == null)
          throw new IllegalArgumentException("Null domain name");
       if (parentPolicy == null)
          throw new IllegalArgumentException("Null parent policy");
       if (policy == null)
          throw new IllegalArgumentException("Null classloader policy");
       
       ClassLoaderDomain domain;
       synchronized (this)
       {
          // See whether the domain already exists
          domain = getDomain(domainName);
          if (domain == null)
          {
             if (parentDomainName != null)
             {
                // See whether the parent domain exists
                ClassLoaderDomain parentDomain = getDomain(parentDomainName);
                if (parentDomain == null)
                  throw new IllegalStateException("Parent domain: " + parentDomainName + "does not exist.");
                
                // Create the domain with a parent
                domain = createAndRegisterDomain(domainName, parentPolicy, parentDomain);
             }
             else
             {
                // Create a domain without a parent
                domain = createAndRegisterDomain(domainName, parentPolicy);
             }
          }
       }
       
       // Register the classloader policy in the domain
       return registerClassLoaderPolicy(domain, policy);
    }
    
    /**
     * Register a policy with a domain
     * 
     * @param domain the domain
     * @param policy the policy
     * @return the classloader
     * @throws IllegalArgumentException if a parameter is null
     * @throws IllegalStateException if the domain is not registered or if the policy is already registered with a domain  
     */
    public ClassLoader registerClassLoaderPolicy(ClassLoaderDomain domain, ClassLoaderPolicy policy)
    {
       if (isDomainRegistered(domain) == false)
          throw new IllegalStateException("Domain is not registered: " + domain);
       
       synchronized (this)
       {
          if (shutdown)
             throw new IllegalStateException("The classloader system is shutdown: " + toLongString());
       }
       return super.registerClassLoaderPolicy(domain, policy);
    }
    
    /**
     * Unregister a policy from its domain
     * 
     * @param policy the policy
     * @throws IllegalArgumentException if a parameter is null
     * @throws IllegalStateException if the policy is not registered with the default domain  
     */
    public void unregisterClassLoaderPolicy(ClassLoaderPolicy policy)
    {
       super.unregisterClassLoaderPolicy(policy);
    }
    
    /**
     * Unregister a classloader from its domain
     * 
     * @param classLoader classLoader
     * @throws IllegalArgumentException if a parameter is null
     * @throws IllegalStateException if the policy is not registered with the default domain  
     */
    public void unregisterClassLoader(ClassLoader classLoader)
    {
       super.unregisterClassLoader(classLoader);
    }
 
    /**
     * Shutdown the classloader system<p>
     * 
     * Unregisters all domains by default
     */
    public synchronized void shutdown()
    {
       if (shutdown)
          return;
 
       log.debug(toLongString() + " SHUTDOWN!");
       shutdown = true;
       
       while (true)
       {
          List<ClassLoaderDomain> domains = new ArrayList<ClassLoaderDomain>(registeredDomains.values());
          Iterator<ClassLoaderDomain> iterator = domains.iterator();
          if (iterator.hasNext() == false)
             break;
          
          while (iterator.hasNext())
          {
             ClassLoaderDomain domain = iterator.next();
             internalUnregisterDomain(domain);
          }
       }
    }
    
    /**
     * Get the translator.
     * 
     * @return the translator.
     */
    public Translator getTranslator()
    {
       return translator;
    }
 
    /**
     * Set the translator.
     * 
     * @param translator the translator.
     */
    public void setTranslator(Translator translator)
    {
       log.debug(this + " set translator to " + translator);
       this.translator = translator;
    }
 
    @Override
    protected byte[] transform(ClassLoader classLoader, String className, byte[] byteCode, ProtectionDomain protectionDomain) throws Exception
    {
       if (translator != null)
          return translator.transform(classLoader, className, null, protectionDomain, byteCode);
       return super.transform(classLoader, className, byteCode, protectionDomain);
    }
 
    @Override
    protected void afterUnregisterClassLoader(ClassLoader classLoader)
    {
       try
       {
          if (translator != null)
             translator.unregisterClassLoader(classLoader);
       }
       catch (Throwable t)
       {
          log.warn("Error unregistering classloader from translator " + classLoader, t);
       }
    }
 
    @Override
    protected void toLongString(StringBuilder builder)
    {
       if (shutdown)
          builder.append("SHUTDOWN! ");
       super.toLongString(builder);
    }
 }
