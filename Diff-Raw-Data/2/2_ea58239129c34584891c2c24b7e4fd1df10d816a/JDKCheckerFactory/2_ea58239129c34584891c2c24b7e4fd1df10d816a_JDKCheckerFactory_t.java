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
 package org.jboss.classloader.spi.jdk;
 
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import org.jboss.classloader.plugins.jdk.AbstractJDKChecker;
 
 /**
  * JDKCheckerFactory.
  * 
  * 
  * @author <a href="adrian@jboss.com">Adrian Brock</a>
  * @version $Revision: 1.1 $
  */
 public class JDKCheckerFactory
 {
    /** The checker */
    private static final JDKChecker checker; 
 
    static
    {
       checker = AccessController.doPrivileged(new PrivilegedAction<JDKChecker>()
       {
          public JDKChecker run()
          {
             // Decide what default checker to use based on the JDK (not implemented - YAGNI?)
             String defaultChecker = AbstractJDKChecker.class.getName();
             
             String className = System.getProperty(JDKChecker.class.getName(), defaultChecker);
             try
             {
                Class<?> clazz;
                try
                {
                   clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
                }
                catch (ClassNotFoundException e)
                {
                   try
                   {
                      clazz = getClass().getClassLoader().loadClass(className);
                   }
                   catch (ClassNotFoundException ignored)
                   {
                      throw e;
                   }
                }
                Object result = clazz.newInstance();
                return JDKChecker.class.cast(result);
             }
             catch (RuntimeException e)
             {
                throw e;
             }
             catch (Exception e)
             {
                throw new Error("Unexpected error loading JDKChecker " + className, e);
             }
          }
       });
    }
 
    /**
     * Retrieve the checker for the JDK we are running on
     * 
     * @return the checker
     */
    public static JDKChecker getChecker()
    {
      if (checker == null)
         return new AbstractJDKChecker();
       return checker;
    }
 }
