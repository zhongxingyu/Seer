 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 package org.mule.ibeans;
 
 import org.mule.ibeans.config.IBeanHolder;
 import org.mule.ibeans.config.IBeanHolderConfigurationBuilder;
 import org.mule.ibeans.test.AbstractIBeansTestCase;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 public class IBeansHolderConfigBuilderTestCase extends AbstractIBeansTestCase
 {
     public void testConfigBuilder() throws Exception
     {
         IBeanHolderConfigurationBuilder builder = new IBeanHolderConfigurationBuilder();
         builder.configure(muleContext);
         Collection<IBeanHolder> col = muleContext.getRegistry().lookupObjects(IBeanHolder.class);
         //Ensure IBeanHolder is comarable
         Set<IBeanHolder> beans = new TreeSet<IBeanHolder>(col);
 
         assertEquals(5, beans.size());
         String[] ids = new String[5];
         int i = 0;
         for (Iterator<IBeanHolder> iterator = beans.iterator(); iterator.hasNext(); i++)
         {
             IBeanHolder iBeanHolder = iterator.next();
             ids[i] = iBeanHolder.getId();
         }
        assertEquals("search", ids[0]);
         assertEquals("testexception", ids[1]);
         assertEquals("testimplicitpropertiesinfactory", ids[2]);
         assertEquals("testparamsfactory", ids[3]);
         assertEquals("testuri", ids[4]);
     }
 }
