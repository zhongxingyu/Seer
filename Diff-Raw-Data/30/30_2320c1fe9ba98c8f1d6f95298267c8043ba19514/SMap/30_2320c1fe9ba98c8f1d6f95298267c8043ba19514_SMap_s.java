 /*
  * Copyright 2012 Bill La Forge
  *
  * This file is part of AgileWiki and is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License (LGPL) as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This code is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  * or navigate to the following url http://www.gnu.org/licenses/lgpl-2.1.txt
  *
  * Note however that only Scala, Java and JavaScript files are being covered by LGPL.
  * All other files are covered by the Common Public License (CPL).
  * A copy of this license is also included and can be
  * found as well at http://www.opensource.org/licenses/cpl1.0.txt
  */
 package org.agilewiki.incdes.impl.collection.vlenc.map;
 
 import org.agilewiki.incdes.Collection;
 import org.agilewiki.incdes.IncDes;
 import org.agilewiki.incdes.MapEntry;
 import org.agilewiki.incdes.PAMap;
 import org.agilewiki.incdes.impl.collection.vlenc.SList;
 import org.agilewiki.incdes.impl.factory.ActorFactory;
 import org.agilewiki.incdes.impl.factory.FactoryLocatorImpl;
 import org.agilewiki.pactor.Mailbox;
 import org.agilewiki.pactor.Request;
 import org.agilewiki.pactor.RequestBase;
 import org.agilewiki.pactor.ResponseProcessor;
 import org.agilewiki.pautil.Ancestor;
 
 /**
  * Holds a map.
  */
 abstract public class SMap<KEY_TYPE extends Comparable<KEY_TYPE>, VALUE_TYPE extends IncDes>
         extends SList<MapEntry<KEY_TYPE, VALUE_TYPE>>
         implements PAMap<KEY_TYPE, VALUE_TYPE>, Collection<MapEntry<KEY_TYPE, VALUE_TYPE>> {
 
     public ActorFactory valueFactory;
 
     private Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getFirstReq;
     private Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getLastReq;
 
     public Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getFirstReq() {
         return getFirstReq;
     }
 
     public Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getLastReq() {
         return getLastReq;
     }
 
     /**
      * Returns the IncDesFactory for the key.
      *
      * @return The IncDesFactory for the key.
      */
     abstract protected ActorFactory getKeyFactory() throws Exception;
 
     /**
      * Converts a string to a key.
      *
      * @param skey The string to be converted.
      * @return The key.
      */
     abstract protected KEY_TYPE stringToKey(String skey);
 
     /**
      * Returns the jid type of all the elements in the list.
      *
      * @return The jid type of all the elements in the list.
      */
     @Override
     final protected ActorFactory getEntryFactory()
             throws Exception {
        return FactoryLocatorImpl.getActorFactory(this, "E." + getJidType());
     }
 
     /**
      * Returns the IncDesFactory for the values in the map.
      *
      * @return The jid factory of the values in the list.
      */
     protected ActorFactory getValueFactory()
             throws Exception {
         if (valueFactory != null)
             return valueFactory;
         throw new IllegalStateException("valueFactory not set");
     }
 
     /**
      * Locate the entry with a matching first element.
      *
      * @param key The key which matches to the entry's first element.
      * @return The index or - (insertion point + 1).
      */
     final public int search(KEY_TYPE key)
             throws Exception {
         initializeList();
         int low = 0;
         int high = size() - 1;
         while (low <= high) {
             int mid = (low + high) >>> 1;
             MapEntryImpl<KEY_TYPE, VALUE_TYPE> midVal = (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(mid);
             int c = midVal.compareKeyTo(key);
             if (c < 0)
                 low = mid + 1;
             else if (c > 0)
                 high = mid - 1;
             else
                 return mid;
         }
         return -(low + 1);
     }
 
     /**
      * Locate the entry with a higher key.
      *
      * @param key The key which matches to the entry's first element.
      * @return The index or -1.
      */
     final public int higher(KEY_TYPE key)
             throws Exception {
         int i = search(key);
         if (i > -1)
             i += 1;
         else {
             i = -i - 1;
         }
         if (i == size())
             return -1;
         return i;
     }
 
     /**
      * Locate the entry with the first element >= a key, or the last entry.
      *
      * @param key The key which matches to the entry's first element, or size.
      * @return The index, or size.
      */
     final public int match(KEY_TYPE key)
             throws Exception {
         int i = search(key);
         if (i > -1)
             return i;
         i = -i - 1;
         return i;
     }
 
     /**
      * Locate the entry with the first element >= a key.
      *
      * @param key The key which matches to the entry's first element.
      * @return The index or -1.
      */
     final public int ceiling(KEY_TYPE key)
             throws Exception {
         int i = match(key);
         if (i == size())
             return -1;
         return i;
     }
 
     @Override
     public Request<Boolean> kMakeReq(final KEY_TYPE _key) {
         return new RequestBase<Boolean>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<Boolean> _rp) throws Exception {
                 _rp.processResponse(kMake(_key));
             }
         };
     }
 
     /**
      * Add an entry to the map unless there is an entry with a matching first element.
      *
      * @param key Used to match the first element of the entries.
      * @return True if a new entry was created.
      */
     @Override
     final public Boolean kMake(KEY_TYPE key)
             throws Exception {
         int i = search(key);
         if (i > -1)
             return false;
         i = -i - 1;
         iAdd(i);
         MapEntryImpl<KEY_TYPE, VALUE_TYPE> me = (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(i);
         me.setKey(key);
         return true;
     }
 
     @Override
     public Request<Boolean> kMakeReq(final KEY_TYPE _key, final byte[] _bytes) {
         return new RequestBase<Boolean>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<Boolean> _rp) throws Exception {
                 _rp.processResponse(kMake(_key, _bytes));
             }
         };
     }
 
     /**
      * Add a tuple to the map unless there is a tuple with a matching first element.
      *
      * @param key   Used to match the first element of the tuples.
      * @param bytes The serialized form of a JID of the appropriate type.
      * @return True if a new tuple was created; otherwise the old value is unaltered.
      */
     public Boolean kMake(KEY_TYPE key, byte[] bytes)
             throws Exception {
         if (!kMake(key))
             return false;
         kSet(key, bytes);
         return true;
     }
 
     final public MapEntryImpl<KEY_TYPE, VALUE_TYPE> kGetEntry(KEY_TYPE key)
             throws Exception {
         int i = search(key);
         if (i < 0)
             return null;
         return (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(i);
     }
 
     @Override
     public Request<VALUE_TYPE> kGetReq(final KEY_TYPE _key) {
         return new RequestBase<VALUE_TYPE>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<VALUE_TYPE> _rp) throws Exception {
                 _rp.processResponse(kGet(_key));
             }
         };
     }
 
     /**
      * Returns the JID value associated with the key.
      *
      * @param key The key.
      * @return The jid assigned to the key, or null.
      */
     @Override
     final public VALUE_TYPE kGet(KEY_TYPE key)
             throws Exception {
         MapEntryImpl<KEY_TYPE, VALUE_TYPE> entry = kGetEntry(key);
         if (entry == null)
             return null;
         return entry.getValue();
     }
 
     @Override
     public Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getHigherReq(final KEY_TYPE _key) {
         return new RequestBase<MapEntry<KEY_TYPE, VALUE_TYPE>>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<MapEntry<KEY_TYPE, VALUE_TYPE>> _rp) throws Exception {
                 _rp.processResponse(getHigher(_key));
             }
         };
     }
 
     /**
      * Returns the JID value with a greater key.
      *
      * @param key The key.
      * @return The matching jid, or null.
      */
     @Override
     final public MapEntryImpl<KEY_TYPE, VALUE_TYPE> getHigher(KEY_TYPE key)
             throws Exception {
         int i = higher(key);
         if (i < 0)
             return null;
         return (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(i);
     }
 
     @Override
     public Request<MapEntry<KEY_TYPE, VALUE_TYPE>> getCeilingReq(final KEY_TYPE _key) {
         return new RequestBase<MapEntry<KEY_TYPE, VALUE_TYPE>>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<MapEntry<KEY_TYPE, VALUE_TYPE>> _rp) throws Exception {
                 _rp.processResponse(getCeiling(_key));
             }
         };
     }
 
     /**
      * Returns the JID value with the smallest key >= the given key.
      *
      * @param key The key.
      * @return The matching jid, or null.
      */
     @Override
     final public MapEntryImpl<KEY_TYPE, VALUE_TYPE> getCeiling(KEY_TYPE key)
             throws Exception {
         int i = ceiling(key);
         if (i < 0)
             return null;
         return (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(i);
     }
 
     @Override
     public Request<Boolean> kRemoveReq(final KEY_TYPE _key) {
         return new RequestBase<Boolean>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<Boolean> _rp) throws Exception {
                 _rp.processResponse(kRemove(_key));
             }
         };
     }
 
     /**
      * Removes the item identified by the key.
      *
      * @param key The key.
      * @return True when the item was present and removed.
      */
     @Override
     final public boolean kRemove(KEY_TYPE key)
             throws Exception {
         int i = search(key);
         if (i < 0)
             return false;
         iRemove(i);
         return true;
     }
 
     /**
      * Resolves a JID pathname, returning a JID actor or null.
      *
      * @param pathname A JID pathname.
      * @return A JID actor or null.
      * @throws Exception Any uncaught exception which occurred while processing the request.
      */
     @Override
     final public IncDes resolvePathname(String pathname)
             throws Exception {
         if (pathname.length() == 0) {
             return this;
         }
         int s = pathname.indexOf("/");
         if (s == -1)
             s = pathname.length();
         if (s == 0)
             throw new IllegalArgumentException("pathname " + pathname);
         String ns = pathname.substring(0, s);
         IncDes jid = kGet(stringToKey(ns));
         if (jid == null)
             return null;
         if (s == pathname.length())
             return jid;
         return jid.resolvePathname(pathname.substring(s + 1));
     }
 
     public MapEntryImpl<KEY_TYPE, VALUE_TYPE> getFirst()
             throws Exception {
         return (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(0);
     }
 
     public MapEntryImpl<KEY_TYPE, VALUE_TYPE> getLast()
             throws Exception {
         return (MapEntryImpl<KEY_TYPE, VALUE_TYPE>) iGet(-1);
     }
 
     public KEY_TYPE getLastKey()
             throws Exception {
         return getLast().getKey();
     }
 
     @Override
     public Request<Void> kSetReq(final KEY_TYPE _key, final byte[] _bytes) {
         return new RequestBase<Void>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<Void> _rp) throws Exception {
                 kSet(_key, _bytes);
                 _rp.processResponse(null);
             }
         };
     }
 
     @Override
     public void kSet(KEY_TYPE key, byte[] bytes)
             throws Exception {
         MapEntryImpl<KEY_TYPE, VALUE_TYPE> entry = kGetEntry(key);
         if (entry == null)
             throw new IllegalArgumentException("not present: " + key);
         entry.setValueBytes(bytes);
     }
 
     public void initialize(final Mailbox mailbox, Ancestor parent, ActorFactory factory) throws Exception {
         super.initialize(mailbox, parent, factory);
         getFirstReq = new RequestBase<MapEntry<KEY_TYPE, VALUE_TYPE>>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<MapEntry<KEY_TYPE, VALUE_TYPE>> _rp) throws Exception {
                 _rp.processResponse(getFirst());
             }
         };
         getLastReq = new RequestBase<MapEntry<KEY_TYPE, VALUE_TYPE>>(getMailbox()) {
             @Override
             public void processRequest(ResponseProcessor<MapEntry<KEY_TYPE, VALUE_TYPE>> _rp) throws Exception {
                 _rp.processResponse(getLast());
             }
         };
     }
 }
