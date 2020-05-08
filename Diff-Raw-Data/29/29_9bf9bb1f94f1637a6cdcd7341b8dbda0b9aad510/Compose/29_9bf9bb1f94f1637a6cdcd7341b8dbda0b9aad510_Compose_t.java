 // Copyright 2008 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.ref_send.promise;
 
 import java.io.Serializable;
 
 /**
  * Implementation plumbing that users should ignore.
  * @param <P> parameter type
  * @param <R> return type
  */
 /* package */ final class
 Compose<P,R> extends Do<P,Void> implements Serializable {
     static private final long serialVersionUID = 1L;
 
     protected final Do<P,R> block;
     private   final Resolver<R> resolver;
     
     /**
      * Constructs a call return block.
      * @param block     code block to execute
      * @param resolver  code block's return resolver
      */
     public
     Compose(final Do<P,R> block, final Resolver<R> resolver) {
         this.block = block;
         this.resolver = resolver;
     }
     
    // org.ref_send.promise.Do interface
     
     public Void
     fulfill(final P a) {
         final R r;
         try {
             r = block.fulfill(a);
         } catch (final Exception e) {
             resolver.reject(e);
             return null;
         }
         resolver.run(r);
         return null;
     }
 
     public Void
     reject(final Exception reason) {
         final R r;
         try {
             r = block.reject(reason);
         } catch (final Exception e) {
             resolver.reject(e);
             return null;
         }
         resolver.run(r);
         return null;
     }
 }
