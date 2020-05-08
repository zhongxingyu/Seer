 // Copyright 2009 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.dns.editor;
 
 import static org.ref_send.promise.Eventual.ref;
 
 import java.io.Serializable;
 
 import org.joe_e.array.ByteArray;
 import org.joe_e.array.ConstArray;
 import org.joe_e.array.PowerlessArray;
 import org.ref_send.promise.Promise;
 import org.ref_send.promise.Receiver;
 import org.waterken.dns.Resource;
 import org.waterken.menu.Menu;
 import org.waterken.menu.TooMany;
 
 /**
  * A {@link Resource} {@link Menu} maker.
  */
 public final class
 HostMaker {
     private HostMaker() {}
 
     /**
      * maximum number of {@link Resource}s per host
      */
     static public final int maxEntries = 8;
     
     /**
      * Constructs a {@link Resource} {@link Menu}.
      */
     static public Menu<ByteArray>
     make() {
         class MenuX implements Menu<ByteArray>, Serializable {
             static private final long serialVersionUID = 1L;
 
            private ConstArray<ResourceVariable> vars =
                ConstArray.array(new ResourceVariable[] {});
 
             public Promise<PowerlessArray<ByteArray>>
             getSnapshot() {
                 final PowerlessArray.Builder<ByteArray> r =
                     PowerlessArray.builder(vars.length());
                 for (final ResourceVariable x : vars) { r.append(x.get()); }
                 return ref(r.snapshot());
             }
 
             public Receiver<ByteArray>
             grow() {
                 if (vars.length() == maxEntries) { throw new TooMany(); }
                 final ResourceVariable r = new ResourceVariable();
                 vars = vars.with(r);
                 return r;
             }
         }
         return new MenuX();
     }
 }
