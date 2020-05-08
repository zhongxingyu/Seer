 // Copyright 2006 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.web_send.graph;
 
 import org.ref_send.promise.Promise;
 
 /**
  * A model maker.
  */
 public interface
 Host {
 
     /**
      * Creates a new model.
      * <p>
      * The factory class specified by the given<code>typename</code> must have
      * a method with signature:</p>
      * <p><code>static public T build({@link Framework} framework)</code></p>
      * <p>or, a public constructor with signature:</p>
      * <p><code>T({@link Eventual} _)</code></p>
      * @param <T> exported object type
      * @param label     model label
      * @param typename  object factory typename
      * @return promise for an object exported from the new model
      */
     <T> Promise<T>
     share(String label, String typename);
 }
