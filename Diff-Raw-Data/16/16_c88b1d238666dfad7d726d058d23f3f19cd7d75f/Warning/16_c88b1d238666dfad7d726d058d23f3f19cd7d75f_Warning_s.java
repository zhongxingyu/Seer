 // Copyright 2010 Waterken Inc. under the terms of the MIT X license
 // found at http://www.opensource.org/licenses/mit-license.html
 package org.waterken.remote.http;
 
 import org.ref_send.deserializer;
import org.ref_send.name;
 import org.ref_send.promise.Failure;
 
 /**
  * Indicates an HTTP response contained a <code>Warning</code> header.
  */
 public class
 Warning extends Failure {
     static private final long serialVersionUID = 1L;

    /**
     * Constructs an instance.
     * @param status    {@link #status}
     * @param phrase    {@link #phrase}
     */
    public @deserializer
    Warning(@name("status") final String status,
            @name("phrase") final String phrase) {
        super(status, phrase);
    }
     
     /**
      * Constructs an instance.
      */
    public
     Warning() {
         super("400", "state response");
     }
 }
