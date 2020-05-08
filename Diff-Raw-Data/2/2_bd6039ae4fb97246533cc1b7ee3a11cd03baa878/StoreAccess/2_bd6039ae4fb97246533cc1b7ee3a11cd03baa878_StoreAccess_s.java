 /**
  * Copyright (c) 2002-2011 "Neo Technology,"
  * Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  *
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.kernel.impl.nioneo.store;
 
 import java.util.Iterator;
 
 import org.neo4j.helpers.collection.PrefetchingIterator;
 
public abstract class StoreAccess<T extends CommonAbstractStore, R extends Object>
 {
     final T store;
 
     StoreAccess( T store )
     {
         this.store = store;
     }
 
     public long getHighId()
     {
         return store.getHighId();
     }
 
     public void rebuildIdGenerators()
     {
         store.rebuildIdGenerators();
     }
 
     public abstract R forceGetRecord( long id );
 
     public Iterable<R> scan( final Filter<? super R>... filters )
     {
         return new Iterable<R>()
         {
             public Iterator<R> iterator()
             {
                 return new PrefetchingIterator<R>()
                 {
                     final long highId = getHighId();
                     int id = 0;
 
                     @Override
                     protected R fetchNextOrNull()
                     {
                         scan: while ( id <= highId && id >= 0 )
                         {
                             R record = forceGetRecord( id++ );
                             for ( Filter<? super R> filter : filters )
                             {
                                 if ( !filter.accept( record ) ) continue scan;
                             }
                             return record;
                         }
                         return null;
                     }
                 };
             }
         };
     }
 
     protected static long longFromIntAndMod( long base, long modifier )
     {
         return CommonAbstractStore.longFromIntAndMod( base, modifier );
     }
 }
