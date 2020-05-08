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
 package org.exoplatform.jcr.benchmark.jcrapi.webdav;
 
 import com.sun.japex.TestCase;
 
 /**
  * @author <a href="mailto:dmitry.kataev@exoplatform.com">Dmytro Katayev</a>
  * @version $Id$
  *
  */
 public abstract class AbstractWebdavPropertyTest extends AbstractWebdavTest
 {
 
   protected String property = "D:testProp";
    
    /**
     * @see org.exoplatform.jcr.benchmark.jcrapi.webdav.AbstractWebdavTest#doPrepare(com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.jcrapi.webdav.WebdavTestContext)
     */
    @Override
    public void doPrepare(TestCase tc, WebdavTestContext context) throws Exception
    {
       super.doPrepare(tc, context);
       
       item.addNode(nodeName, "nt:untstructured", "".getBytes());
    }
    
    /**
     * @see org.exoplatform.jcr.benchmark.jcrapi.webdav.AbstractWebdavTest#doRun(com.sun.japex.TestCase, org.exoplatform.jcr.benchmark.jcrapi.webdav.WebdavTestContext)
     */
    @Override
    public abstract void doRun(TestCase tc, WebdavTestContext context) throws Exception;
    
 }
