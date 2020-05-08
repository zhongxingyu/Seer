 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.core.security;
 
 import org.acegisecurity.context.SecurityContextImpl;
 
 /** SecurityContext implementation to support cloning.
  *
  * This is used in HttpSessionContextIntegrationFilter to clone the
  * SecurityContext instance found in the session. This is used to guarantee
  * that the SecurityContext always contains a valid authority.
  *
  * Without this, two threads hitting the same session object may incur in a
  * race condition, because they will be modifying the same SecurityContext.
  */
 public class CloneableSecurityContext extends SecurityContextImpl
   implements Cloneable {
 
  /** The serial version id.
   */
  private static final long serialVersionUID = 1L;

/** {@inheritDoc}
    */
   @Override
   public CloneableSecurityContext clone()
       throws CloneNotSupportedException {
     return (CloneableSecurityContext) super.clone();
   }
 }
 
