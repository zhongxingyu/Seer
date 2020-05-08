 /*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
 package org.jboss.aop.classpool;
 
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.NotFoundException;
 
 
 
 /**
  * 
  * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
  * @version $Revision: 1.1 $
  */
 public abstract class AbstractClassPoolDomain implements ClassPoolDomainInternal
 {
    protected CtClass getCachedOrCreateFromPoolParent(BaseClassPool initiatingPool, String classname, boolean create)
    {
       if (initiatingPool == null)
       {
          return null;
       }
       ClassPool parentPool = initiatingPool.getParent();
       if (parentPool == null)
       {
          return null;
       }
        
       if (parentPool instanceof BaseClassPool)
       {
          return getCachedOrCreate((BaseClassPool)parentPool, classname, create);
       }
       else
       {
          return getCachedOrCreate(parentPool, classname, create);
       }
    }
 
    protected CtClass getCachedOrCreate(BaseClassPool parentPool, String classname, boolean create)
    {
       if (parentPool == null)
       {
          return null;
       }
       
       CtClass clazz = null;
       if (!parentPool.childFirstLookup)
       {
          clazz = getCachedOrCreateFromPoolParent(parentPool, classname, create); 
       }
       
      //We can use the exposed methods directly to avoid the overhead of NotFoundException
      clazz = parentPool.getCached(classname);
      if (clazz == null && create)
       {
         clazz = parentPool.createCtClass(classname, true);
       }

       if (clazz == null && !parentPool.childFirstLookup)
       {
          clazz = getCachedOrCreateFromPoolParent(parentPool, classname, create); 
       }
       return clazz;
    }
    
    protected CtClass getCachedOrCreate(ClassPool parentPool, String classname, boolean create)
    {
       try
       {
          //This will check the parents
          return parentPool.get(classname);
       }
       catch(NotFoundException e)
       {
          return null;
       }
    }
       
 }
