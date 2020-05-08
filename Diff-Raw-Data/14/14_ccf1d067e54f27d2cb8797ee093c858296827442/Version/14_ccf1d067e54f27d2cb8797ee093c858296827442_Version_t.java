 /*---------------------------------------------------------------------------*\
   $Id$
   ---------------------------------------------------------------------------
   This software is released under a Berkeley-style license:
 
   Copyright (c) 2004 Brian M. Clapper. All rights reserved.
 
   Redistribution and use in source and binary forms are permitted provided
   that: (1) source distributions retain this entire copyright notice and
   comment; and (2) modifications made to the software are prominently
   mentioned, and a copy of the original software (or a pointer to its
   location) are included. The name of the author may not be used to endorse
   or promote products derived from this software without specific prior
   written permission.
 
   THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
   Effectively, this means you can do what you want with the software except
   remove this notice or take advantage of the author's name. If you modify
   the software and redistribute your modified version, you must indicate that
   your version is a modification of the original, and you must provide either
   a pointer to or a copy of the original.
 \*---------------------------------------------------------------------------*/
 
 package org.clapper.curn;
 
 import java.lang.System;
 
 import java.io.PrintWriter;
 import java.io.PrintStream;
 
 import java.util.ResourceBundle;
 import java.util.MissingResourceException;
 
 import org.clapper.util.misc.BuildInfo;
 
 /**
  * <p>Contains the software version for the <i>org.clapper.util</i>
  * library. Also contains a main program which, invoked, displays the
  * name of the API and the version on standard output.</p>
  *
  * @version <tt>$Revision$</tt>
  *
  * @author Copyright &copy; 2004 Brian M. Clapper
  */
 public final class Version
 {
     /*----------------------------------------------------------------------*\
                              Public Constants
     \*----------------------------------------------------------------------*/
 
     public static final String VERSION = "1.4";
 
     /**
      * The name of the resource bundle containing the build info.
      */
     public static final String BUNDLE_NAME
         = "org.clapper.curn.BuildInfoBundle";
 
     /*----------------------------------------------------------------------*\
                                Main Program
     \*----------------------------------------------------------------------*/
 
     /**
      * Get the full program version string, which contains the program name
      * and the version number. This is the string that the
     * {@link #showVersion()} method displays.
      *
      * @return the full version string
      *
      * @see #showVersion()
      * @see #showVersion(PrintWriter)
     * @see #showVersion(PrintStream)
      */
     public static String getFullVersion()
     {
         return "curn, version " + VERSION;
     }
 
     /**
      * Display version information only to standard output.
      *
      * @see #showVersion(PrintWriter)
      * @see #showVersion(PrintStream)
      */
     public static void showVersion()
     {
         showVersion (System.out);
     }
 
     /**
      * Display version information to the specified <tt>PrintWriter</tt>.
      *
      * @param out  where to write the version string
      *
      * @see #showVersion()
      * @see #showVersion(PrintStream)
      */
     public static void showVersion (PrintWriter out)
     {
         out.println (getFullVersion());
     }
 
     /**
      * Display version information to the specified <tt>PrintStream</tt>.
      *
      * @param out  where to write the version string
      *
      * @see #showVersion()
      * @see #showVersion(PrintWriter)
      */
     public static void showVersion (PrintStream out)
     {
         out.println (getFullVersion());
     }
 
     /**
      * Display build information to standard output.
      *
      * @see #showBuildInfo(PrintWriter)
      * @see #showBuildInfo(PrintStream)
      */
     public static void showBuildInfo()
     {
         showBuildInfo (System.out);
     }
 
     /**
      * Display build information to the specified <tt>PrintStream</tt>.
      *
      * @param out  where to write the build information
      *
      * @see #showBuildInfo()
      * @see #showBuildInfo(PrintWriter)
      */
     public static void showBuildInfo (PrintStream out)
     {
         showBuildInfo (new PrintWriter (out));
     }
 
     /**
      * Display build information to the specified <tt>PrintWriter</tt>.
      *
      * @param out  where to write the build information
      *
      * @see #showBuildInfo()
      * @see #showBuildInfo(PrintStream)
      */
     public static void showBuildInfo (PrintWriter out)
     {
         BuildInfo buildInfo = new BuildInfo (BUNDLE_NAME);
 
         out.println ();
         showVersion (out);
         out.println ("Build date:     " + buildInfo.getBuildDate());
         out.println ("Built by:       " + buildInfo.getBuildUserID());
         out.println ("Built on:       " + buildInfo.getBuildOperatingSystem());
         out.println ("Build Java VM:  " + buildInfo.getBuildJavaVM());
         out.println ("Build compiler: " + buildInfo.getBuildJavaCompiler());
         out.println ("Ant version:    " + buildInfo.getBuildAntVersion());
     }
 }
