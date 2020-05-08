 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import java.io.Serializable;
 
 import org.joe_e.array.ConstArray;
 
 /**
  * The set of remote hosts with pending requests.
  */
 /* package */ final class
 Outbound implements Serializable {
     static private final long serialVersionUID = 1L;
     
     /**
      * pending peers
      */
     private ConstArray<Pipeline> pending;
     
     protected
     Outbound() {
        pending = ConstArray.array(new Pipeline[] {});
     }
     
     // org.waterken.remote.http.Outbound interface
     
     /**
      * pending peers
      */
     protected ConstArray<Pipeline>
     getPending() { return pending; }
 
     protected Pipeline
     find(final String peer) {
         for (final Pipeline x : pending) {
             if (x.peer.equals(peer)) { return x; }
         }
         return null;
     }
     
     protected void
     add(final Pipeline msgs) {
         pending = pending.with(msgs);
     }
     
     protected void
     remove(final Pipeline msgs) {
         int i = 0;
         for (final Pipeline x : pending) {
             if (x.equals(msgs)) {
                 pending = pending.without(i);
                 break;
             }
             ++i;
         }
     }
 }
