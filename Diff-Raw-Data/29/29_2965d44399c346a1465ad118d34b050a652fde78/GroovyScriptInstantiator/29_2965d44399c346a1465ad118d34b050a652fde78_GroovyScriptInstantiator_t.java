 /*
  * Copyright (C) 2009 eXo Platform SAS.
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
 package org.exoplatform.services.script.groovy;
 
 import groovy.lang.GroovyClassLoader;
 import groovy.lang.GroovyCodeSource;
 
 import org.codehaus.groovy.control.CompilationFailedException;
 import org.exoplatform.commons.utils.SecurityHelper;
 import org.exoplatform.container.ExoContainer;
 import org.exoplatform.container.ExoContainerContext;
 import org.exoplatform.container.component.ComponentPlugin;
 import org.exoplatform.services.log.ExoLogger;
 import org.exoplatform.services.log.Log;
 import org.exoplatform.services.script.groovy.jarjar.JarJarClassLoader;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Constructor;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.PrivilegedAction;
 import java.security.PrivilegedActionException;
 import java.security.PrivilegedExceptionAction;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
  * @version $Id: $
  */
 public class GroovyScriptInstantiator
 {
 
    /** Our logger. */
    private static final Log LOG = ExoLogger.getLogger(GroovyScriptInstantiator.class);
 
    /**
     * eXo Container.
     */
    private ExoContainer container;
 
    /** The global mapping. */
    private Map<String, String> mapping;
 
    /**
     * @param containerContext container context
     */
    public GroovyScriptInstantiator(ExoContainerContext containerContext)
    {
       this.container = containerContext.getContainer();
       this.mapping = Collections.synchronizedMap(new HashMap<String, String>());
    }
 
    /**
     * Load script from given address.
     *
     * @param spec the resource's address.
     * @return the object created from groovy script.
     * @throws MalformedURLException if parameter <code>url</code> have wrong
     *         format.
     * @throws IOException if can't load script from given <code>url</code>.
     * @see GroovyScriptInstantiator#instantiateScript(URL)
     * @see GroovyScriptInstantiator#instantiateScript(InputStream)
     */
    public Object instantiateScript(String spec) throws MalformedURLException, IOException
    {
       return instantiateScript(new URL(spec));
    }
 
    /**
     * Load script from given address.
     *
     * @param url the resource's address.
     * @return the object created from groovy script.
     * @throws IOException if can't load script from given <code>url</code>.
     * @see GroovyScriptInstantiator#instantiateScript(InputStream)
     */
    public Object instantiateScript(URL url) throws IOException
    {
       String name = url.toString();
       return instantiateScript(new BufferedInputStream(url.openStream()), name);
    }
 
    /**
     * Parse given stream, the stream must represents groovy script.
     *
     * @param stream the stream represented groovy script.
     * @return the object created from groovy script.
     * @throws IOException if stream can't be parsed or object can't be created
     *         cause to illegal content of stream
     */
    public Object instantiateScript(InputStream stream) throws IOException
    {
       return instantiateScript(stream, null);
    }
 
    /**
     * Parse given stream, the stream must represents groovy script.
     *
     * @param stream the stream represented groovy script.
     * @param name script name is null or empty string that groovy completer will
     *        use default name
     * @return the object created from groovy script.
     * @throws IOException if stream can't be parsed or object can't be created
     *         cause to illegal content of stream
     */
    public Object instantiateScript(InputStream stream, String name) throws IOException
    {
       GroovyClassLoader loader;
       if (mapping.size() > 0)
       {
          JarJarClassLoader jarjarLoader = SecurityHelper.doPrivilegedAction(new PrivilegedAction<JarJarClassLoader>()
          {
             public JarJarClassLoader run()
             {
                return new JarJarClassLoader();
             }
          });
 
          jarjarLoader.addMapping(mapping);
          loader = jarjarLoader;
       }
       else
       {
          loader = SecurityHelper.doPrivilegedAction(new PrivilegedAction<GroovyClassLoader>()
          {
             public GroovyClassLoader run()
             {
                return new GroovyClassLoader();
             }
          });
       }
       return instantiateScript(stream, name, loader);
    }
 
    /**
     * Parse given stream, the stream must represents groovy script and use given
     * class-loader. If <code>loader == null</code> then
     * {@link groovy.lang.GroovyClassLoader} will be is use.
     *
     * @param stream the stream represented groovy script.
     * @param name script name is null or empty string that groovy completer will
     *        use default name
     * @param loader GroovyClassLoader or <code>null</code>
     * @return the object created from groovy script.
     * @throws IOException if stream can't be parsed or object can't be created
     *         cause to illegal content of stream
     */
    public Object instantiateScript(final InputStream stream, final String name, GroovyClassLoader loader)
       throws IOException
    {
       if (loader == null)
       {
          loader = new GroovyClassLoader();
       }
       Class<?> clazz = null;
       try
       {
          final GroovyClassLoader fLoader = loader;
          clazz = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Class<?>>()
          {
             public Class<?> run() throws Exception
             {
                if (name != null && name.length() > 0)
                {
                   return fLoader.parseClass(stream, name);
                }
                else
                {
                   return fLoader.parseClass(stream);
                }
             }
          });
       }
       catch (PrivilegedActionException pae)
       {
          Throwable cause = pae.getCause();
          if (cause instanceof CompilationFailedException)
          {
            throw new IOException("Error occurs when parse stream, compiler error:\n " + cause.getMessage(), cause);
          }
          else if (cause instanceof RuntimeException)
          {
             throw (RuntimeException)cause;
          }
          else
          {
             throw new RuntimeException(cause);
          }
       }
 
       try
       {
          return createObject(clazz);
       }
       catch (Exception e)
       {
          throw new RuntimeException("Can't instantiate groovy script: " + e.getMessage(), e);
       }
       finally
       {
          stream.close();
       }
    }
 
    /**
     * Instantiate script from give {@link GroovyCodeSource} and use given
     * class-loader. If <code>loader == null</code> then
     * {@link groovy.lang.GroovyClassLoader} will be is use.
     *
     * @param codeSource code source
     * @param loader
     * @return
     */
    public Object instantiateScript(final GroovyCodeSource codeSource, GroovyClassLoader loader)
    {
       if (loader == null)
       {
          loader = new GroovyClassLoader();
       }
 
       final GroovyClassLoader fLoader = loader;
       Class<?> clazz = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Class<?>>()
       {
          public Class<?> run()
          {
             return fLoader.parseClass(codeSource);
          }
       });
 
       try
       {
          return createObject(clazz);
       }
       catch (Exception e)
       {
          throw new RuntimeException("Can't instantiate groovy script: " + e.getMessage(), e);
       }
    }
 
    /**
     * Created object from given class, if class has parameters in constructor,
     * then this parameters will be searched in container.
     *
     * @param clazz java-groovy class
     */
    private Object createObject(Class<?> clazz) throws Exception
    {
 
       Constructor<?>[] constructors = clazz.getConstructors();
 
       /*
        * Sort constructors by number of parameters. With more parameters must be
        * first.
        */
       Arrays.sort(constructors, COMPARATOR);
 
       l : for (Constructor<?> c : constructors)
       {
          Class<?>[] parameterTypes = c.getParameterTypes();
          if (parameterTypes.length == 0)
          {
             return c.newInstance();
          }
 
          List<Object> parameters = new ArrayList<Object>(parameterTypes.length);
 
          for (Class<?> parameterType : parameterTypes)
          {
             Object param = container.getComponentInstanceOfType(parameterType);
             if (param == null)
             {
                continue l;
             }
             parameters.add(param);
          }
 
          return c.newInstance(parameters.toArray(new Object[parameters.size()]));
       }
       return null;
 
    }
 
    private static final ConstructorsComparator COMPARATOR = new ConstructorsComparator();
 
    /**
     * Sorts array of constructors by number of parameters.
     */
    private static class ConstructorsComparator implements Comparator<Constructor<?>>
    {
 
       /**
        * {@inheritDoc}
        */
       public int compare(Constructor<?> constructor1, Constructor<?> constructor2)
       {
          int c1 = constructor1.getParameterTypes().length;
          int c2 = constructor2.getParameterTypes().length;
          if (c1 < c2)
          {
             return 1;
          }
          if (c1 > c2)
          {
             return -1;
          }
          return 0;
       }
 
    }
 
    public void addPlugin(ComponentPlugin plugin)
    {
       if (plugin instanceof GroovyScriptJarJarPlugin)
       {
          GroovyScriptJarJarPlugin jarjarPlugin = (GroovyScriptJarJarPlugin)plugin;
          if (LOG.isDebugEnabled())
          {
             LOG.debug("Add mapping to groovy instantiator:" + jarjarPlugin.getMapping());
          }
          mapping.putAll(jarjarPlugin.getMapping());
       }
    }
 
 }
