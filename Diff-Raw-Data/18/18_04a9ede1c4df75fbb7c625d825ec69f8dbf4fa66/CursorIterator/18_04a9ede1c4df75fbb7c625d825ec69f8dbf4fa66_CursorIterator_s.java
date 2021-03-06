 /*
  *  Licensed to the Apache Software Foundation (ASF) under one
  *  or more contributor license agreements.  See the NOTICE file
  *  distributed with this work for additional information
  *  regarding copyright ownership.  The ASF licenses this file
  *  to you under the Apache License, Version 2.0 (the
  *  "License"); you may not use this file except in compliance
  *  with the License.  You may obtain a copy of the License at
  *  
  *    http://www.apache.org/licenses/LICENSE-2.0
  *  
  *  Unless required by applicable law or agreed to in writing,
  *  software distributed under the License is distributed on an
  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  *  KIND, either express or implied.  See the License for the
  *  specific language governing permissions and limitations
  *  under the License. 
  *  
  */
 package org.apache.directory.server.core.cursor;
 
 
 import java.util.Iterator;
 
 
 /**
  * An Iterator over a Cursor so Cursors can be Iterable for using in foreach
  * constructs.
  *
  * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
  * @version $$Rev$$
  */
 public class CursorIterator<E> implements Iterator<E>
 {
    private Cursor<E> cursor;
 
 
     public CursorIterator( Cursor cursor )
     {
         this.cursor = cursor;
     }
 
 
     public boolean hasNext()
     {
        return cursor.available();
     }
 
 
     public E next()
     {
         try
         {
             E element = cursor.get();
            cursor.next();
             return element;
         }
         catch ( Exception e )
         {
             throw new RuntimeException( "Failure on underlying Cursor.", e );
         }
     }
 
 
     public void remove()
     {
         throw new UnsupportedOperationException( "Underlying Cursor does not support removal." );
     }
 }
