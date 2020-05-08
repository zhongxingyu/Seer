 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.common.dao.search;
 
 import com.smartitengineering.common.dao.search.impl.CommonAsyncFreeTextPersistentDaoImpl.ArrayBuilder;
 import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
 import junit.framework.TestCase;
 
 /**
  *
  * @author imyousuf
  */
 public class ArrayBuilderTest extends TestCase {
 
   public void testGenericArrayCreation() {
    ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
    queue.add("test");
    queue.add("test1");
    ArrayBuilder<String> builder = new ArrayBuilder<String>(String.class, queue);
     final String[] values = new String[]{"test", "test1"};
     assertTrue(Arrays.equals(values, builder.toArray()));
    queue.add("test");
    queue.add("test1");
    System.out.println(queue);
    builder = new ArrayBuilder<String>(null, queue);
     assertTrue(Arrays.equals(values, builder.toArray()));
   }
 }
