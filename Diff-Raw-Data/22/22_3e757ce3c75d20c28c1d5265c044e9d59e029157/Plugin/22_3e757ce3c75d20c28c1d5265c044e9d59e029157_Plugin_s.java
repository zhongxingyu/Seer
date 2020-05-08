 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
  */

 package org.jdesktop.wonderland.common.annotation;
 
 import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
 import java.lang.annotation.Target;
 
 /**
  * Annotation indicating this class implements a server or client plugin.
  * The type determines whether it is a client or server plugin.
  * <p>
  * ClientPlugins must implement the ClientPlugin interface, while server
  * plugins must implement the ServerPlugin interface.
  * @author jkaplan
  */
 @Target(ElementType.TYPE)
 public @interface Plugin {
 }
