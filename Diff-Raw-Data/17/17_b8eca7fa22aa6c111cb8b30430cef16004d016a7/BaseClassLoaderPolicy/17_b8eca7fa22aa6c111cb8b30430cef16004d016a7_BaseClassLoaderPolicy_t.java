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
 package org.jboss.classloader.spi.base;
 
 import java.security.AccessControlContext;
 import java.security.AccessController;
 import java.security.ProtectionDomain;
 import java.util.List;
 
 import org.jboss.classloader.spi.ClassLoaderDomain;
 import org.jboss.classloader.spi.DelegateLoader;
 import org.jboss.logging.Logger;
 
 /**
  * Base ClassLoader policy.<p>
  * 
  * This class hides some of the implementation details and allows
  * package access to the protected methods.
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public abstract class BaseClassLoaderPolicy
 {
    /** The log */
    private static final Logger log = Logger.getLogger(BaseClassLoaderPolicy.class);
    
    /** The classloader for this policy */
    private volatile BaseClassLoader classLoader;
 
    /** The domain for this policy */
    private volatile BaseClassLoaderDomain domain;
 
    /** The access control context for this policy */
    private AccessControlContext access;
    
    /**
     * Create a new BaseClassLoaderPolicy.
     * 
     * @throws SecurityException if the caller does not have permission to create a classloader
     */
    public BaseClassLoaderPolicy()
    {
       SecurityManager sm = System.getSecurityManager();
       if (sm != null)
          sm.checkCreateClassLoader();
       
       access = AccessController.getContext();
    }
 
    /**
     * Get the access control context for this policy
     * 
     * @return the access control context
     */
    protected AccessControlContext getAccessControlContext()
    {
       return access;
    }
    
    /**
     * Get the delegate loader for exported stuff<p>
     *
     * NOTE: Protected access for security reasons
     * 
     * @return the delegate loader
     */
    protected abstract DelegateLoader getExported();
 
    /**
     * Get a simple name for the classloader
     * 
     * @return the name
     */
    protected String getName()
    {
       return "";
    }
    
    /**
     * Get the exported packages<p>
     *
     * Provides a hint for indexing
     * 
     * @return the package names
     */
    public abstract String[] getPackageNames();
 
    /**
     * Get the delegate loaders for imported stuff<p>
     * 
     * NOTE: Protected access for security reasons
     * 
     * @return the delegate loaders
     */
    protected abstract List<? extends DelegateLoader> getDelegates();
 
    /**
     * Whether to import all exports from other classloaders in the domain
     * 
     * @return true to import all
     */
    protected abstract boolean isImportAll();
 
    /**
     * Get the protection domain<p>
     * 
     * NOTE: Defined as protected here for security reasons
     * 
     * @param className the class name
     * @param path the path
     * @return the protection domain
     */
    protected abstract ProtectionDomain getProtectionDomain(String className, String path);
    
    /**
     * Transform the byte code<p>
     * 
     * By default, this delegates to the domain
     * 
     * @param className the class name
     * @param byteCode the byte code
     * @param protectionDomain the protection domain
     * @return the transformed byte code
     * @throws Exception for any error
     */
    protected byte[] transform(String className, byte[] byteCode, ProtectionDomain protectionDomain) throws Exception
    {
       BaseClassLoaderDomain domain = getClassLoaderDomain();
       if (domain != null)
          return domain.transform(getClassLoader(), className, byteCode, protectionDomain);
       return byteCode;
    }
 
    /**
     * Whether to cache<p>
     * 
     * @return true to cache
     */
    protected abstract boolean isCachable();
 
    /**
     * Whether to cache misses<p>
     * 
     * @return true to cache misses
     */
    protected abstract boolean isBlackListable();
 
    /**
     * Check whether this a request from the jdk if it is return the relevant classloader
     * 
     * @param name the class name
     * @return the classloader
     */
    protected abstract ClassLoader isJDKRequest(String name);
    
    /**
     * A long version of toString()
     * 
     * @return the long string
     */
    public String toLongString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append(getClass().getSimpleName());
       builder.append("@").append(Integer.toHexString(System.identityHashCode(this)));
       builder.append("{domain=");
       if (domain == null)
          builder.append("null");
       else
          builder.append(domain.toLongString());
       toLongString(builder);
       builder.append('}');
       return builder.toString();
    }
    
    /**
     * For subclasses to add information for toLongString()
     * 
     * @param builder the builder
     */
    protected void toLongString(StringBuilder builder)
    {
    }
 
    @Override
    public String toString()
    {
       StringBuilder builder = new StringBuilder();
       builder.append(getClass().getSimpleName());
       builder.append("@").append(Integer.toHexString(System.identityHashCode(this)));
      builder.append("{").append(getName()).append("}");
       return builder.toString();
    }
    
    /**
     * Get the classloader domain name
     * 
     * @return the domain
     */
    public String getDomainName()
    {
       if (domain == null)
          return null;
       return ((ClassLoaderDomain) domain).getName();
    }
    
    /**
     * Get the classloader domain
     * 
     * @return the domain
     */
    BaseClassLoaderDomain getClassLoaderDomain()
    {
       return domain;
    }
    
    /**
     * Set the classloader domain
     * 
     * @param domain the domain
     * @throws IllegalStateException if the policy already has a domain
     */
    void setClassLoaderDomain(BaseClassLoaderDomain domain)
    {
       if (this.domain != null)
          throw new IllegalStateException("Policy already has a domain " + this);
       this.domain = domain;
    }
 
    
    /**
     * Unset the classloader domain
     * 
     * @param domain the domain
     * @throws IllegalStateException if the policy is not part of that domain
     */
    void unsetClassLoaderDomain(BaseClassLoaderDomain domain)
    {
       if (this.domain != domain)
          throw new IllegalStateException("Policy is not a part of the domain " + this + " domain=" + domain);
       shutdownPolicy();
       this.domain = null;
    }
    /**
     * Get the classloader
     * 
     * @return the classloader
     */
    synchronized BaseClassLoader getClassLoader()
    {
       if (classLoader == null)
          throw new IllegalStateException("No classloader associated with policy therefore it is no longer registered " + toLongString());
       return classLoader;
    }
    
    /**
     * Set the classloader<p>
     * 
     * NOTE: Package private for security reasons
     * 
     * @param classLoader the classloader
     * @throws IllegalStateException if the classloader is already set
     */
    synchronized void setClassLoader(BaseClassLoader classLoader)
    {
       if (this.classLoader != null)
          throw new IllegalStateException("Policy already has a classloader previous=" + classLoader);
       this.classLoader = classLoader;
    }
    
    /**
     * Shutdown the policy<p>
     * 
     * The default implementation removes and shutdowns the classloader
     */
    synchronized protected void shutdownPolicy()
    {
       log.debug(toLongString() + " shutdown!");
       BaseClassLoader classLoader = this.classLoader;
       this.classLoader = null;
       classLoader.shutdownClassLoader();
    }
    
    /**
     * Cleans the entry with the given name from the blackList
     *
     * @param name the name of the resource to clear from the blackList
     */
    protected void clearBlackList(String name)
    {
        if (domain != null)
        {
           domain.clearBlackList(name);
        }
    }
 }
