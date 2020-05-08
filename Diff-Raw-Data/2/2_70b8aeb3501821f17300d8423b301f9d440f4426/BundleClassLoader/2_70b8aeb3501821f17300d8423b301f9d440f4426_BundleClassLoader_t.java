 package org.jboss.vfs.bundle;
 
 import java.io.IOException;
 import java.net.URL;
 import java.security.SecureClassLoader;
 import java.util.Enumeration;
 
 import org.jboss.classloading.spi.ClassLoadingDomain;
 import org.jboss.classloading.spi.DomainClassLoader;
 import org.jboss.logging.Logger;
 import org.osgi.framework.Bundle;
 
 public class BundleClassLoader extends SecureClassLoader
    implements DomainClassLoader
 {
 
    private static Logger log = Logger.getLogger(BundleClassLoader.class);
 
    public BundleClassLoader(Bundle bundle)
    {
 	   
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#getClasspath()
     */
    public URL[] getClasspath()
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#getPackagNames()
     */
   public String[] getPackageNames()
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see java.lang.ClassLoader#getPackages()
     */
    @Override
    public Package[] getPackages()
    {
       // TODO Auto-generated method stub
       return super.getPackages();
    }
 
    /* (non-Javadoc)
     * @see java.lang.ClassLoader#getPackage(java.lang.String)
     */
    @Override
    public Package getPackage(String name)
    {
       // TODO Auto-generated method stub
       return super.getPackage(name);
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#getDomain()
     */
    public ClassLoadingDomain getDomain()
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#loadClassLocally(java.lang.String, boolean)
     */
    public Class loadClassLocally(String name, boolean resolve) throws ClassNotFoundException
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#loadResourceLocally(java.lang.String)
     */
    public URL loadResourceLocally(String name)
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#findResourcesLocally(java.lang.String)
     */
    public Enumeration<URL> findResourcesLocally(String name) throws IOException
    {
       // TODO Auto-generated method stub
       return null;
    }
 
    /* (non-Javadoc)
     * @see org.jboss.classloading.spi.DomainClassLoader#setDomain(org.jboss.classloading.spi.ClassLoadingDomain)
     */
    public void setDomain(ClassLoadingDomain domain)
    {
       // TODO Auto-generated method stub
       
    }
 }
