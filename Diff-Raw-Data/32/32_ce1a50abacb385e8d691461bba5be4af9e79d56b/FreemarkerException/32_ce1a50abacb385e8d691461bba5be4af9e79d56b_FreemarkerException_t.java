 /*
  * Copyright (C) 2010-2012 Klaus Reimer <k@ailis.de>
  * See LICENSE.md for licensing information.
  */
 package de.ailis.jasdoc.freemarker;
 
 /**
  * Thrown when something goes wrong with freemarker templates.
  *
  * @author Klaus Reimer (k@ailis.de)
  */
 public final class FreemarkerException extends RuntimeException
 {
    /** Serial version UID. */
     private static final long serialVersionUID = -2215161122911612446L;
 
     /**
     * Constructor.
      *
      * @param message
      *            The exception message
      */
 
     public FreemarkerException(final String message)
     {
         super(message);
     }
 
     /**
     * Constructor.
      *
      * @param message
      *            The exception message
      * @param cause
      *            The root cause
      */
 
     public FreemarkerException(final String message, final Throwable cause)
     {
         super(message, cause);
     }
 }
