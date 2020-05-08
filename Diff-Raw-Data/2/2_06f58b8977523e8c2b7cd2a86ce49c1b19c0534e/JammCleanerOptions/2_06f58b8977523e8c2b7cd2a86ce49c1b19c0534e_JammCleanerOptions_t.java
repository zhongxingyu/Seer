 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.tools;
 
 /**
  * The options that any part of JammCleaner could need access to.
  */
 public final class JammCleanerOptions
 {
     /**
      * Gets the value of verbose
      *
      * @return the value of verbose
      */
     public static boolean isVerbose() 
     {
         return JammCleanerOptions.mVerbose;
     }
 
     /**
      * Sets the value of verbose
      *
      * @param verbose Value to assign to this.verbose
      */
     public static void setVerbose(boolean verbose)
     {
         JammCleanerOptions.mVerbose = verbose;
     }
 
     /**
      * Gets the value of assumeYes
      *
      * @return the value of assumeYes
      */
     public static boolean isAssumeYes() 
     {
         return JammCleanerOptions.mAssumeYes;
     }
 
     /**
      * Sets the value of assumeYes
      *
      * @param assumeYes Value to assign to this.assumeYes
      */
     public static void setAssumeYes(boolean assumeYes)
     {
         JammCleanerOptions.mAssumeYes = assumeYes;
     }
 
     /**
      * Gets the value of bindDn
      *
      * @return the value of bindDn
      */
     public static String getBindDn() 
     {
         return JammCleanerOptions.mBindDn;
     }
 
     /**
      * Sets the value of bindDn
      *
      * @param bindDn Value to assign to this.bindDn
      */
     public static void setBindDn(String bindDn)
     {
         JammCleanerOptions.mBindDn = bindDn;
     }
 
     /**
      * Gets the value of password
      *
      * @return the value of password
      */
     public static String getPassword() 
     {
         return JammCleanerOptions.mPassword;
     }
 
     /**
      * Sets the value of password
      *
      * @param password Value to assign to this.password
      */
     public static void setPassword(String password)
     {
         JammCleanerOptions.mPassword = password;
     }
 
     /**
      * Gets the value of host
      *
      * @return the value of host
      */
     public static String getHost() 
     {
         return JammCleanerOptions.mHost;
     }
 
     /**
      * Sets the value of host
      *
      * @param host Value to assign to this.host
      */
     public static void setHost(String host)
     {
         JammCleanerOptions.mHost = host;
     }
 
     /**
      * Gets the value of port
      *
      * @return the value of port
      */
     public static int getPort() 
     {
         return JammCleanerOptions.mPort;
     }
 
     /**
      * Sets the value of port
      *
      * @param port Value to assign to this.port
      */
     public static void setPort(int port)
     {
         JammCleanerOptions.mPort = port;
     }
 
     /**
      * Gets the value of baseDn
      *
      * @return the value of baseDn
      */
     public static String getBaseDn() 
     {
         return JammCleanerOptions.mBaseDn;
     }
 
     /**
      * Sets the value of baseDn
      *
      * @param baseDn Value to assign to this.baseDn
      */
     public static void setBaseDn(String baseDn)
     {
         JammCleanerOptions.mBaseDn = baseDn;
     }
 
     /**
      * Returns the value of debug
      *
      * @return true or false
      */
     public static boolean isNonDestructive()
     {
         return JammCleanerOptions.mNonDestructive;
     }
 
     /**
      * sets the value of debug
      *
     * @param nonDestructive value to assign to debug
      */
     public static void setNonDestructive(boolean nonDestructive)
     {
         JammCleanerOptions.mNonDestructive = nonDestructive;
     }
 
     /**
      * Dump out the args for debugging purposes
      *
      * @return a string with the args
      */
     public static String argDump()
     {
         StringBuffer sb = new StringBuffer();
         sb.append("verbose: ").append(mVerbose).append("\n");
         sb.append("assume yes: ").append(mAssumeYes).append("\n");
         sb.append("host: ").append(mHost).append("\n");
         sb.append("port: ").append(mPort).append("\n");
         sb.append("bind dn: ").append(mBindDn).append("\n");
         sb.append("base dn: ").append(mBaseDn).append("\n");
 
         return sb.toString();
     }
         
     /** verbosity */
     private static boolean mVerbose = false;
     /** assume yes to questions */
     private static boolean mAssumeYes = false;
     /** what's the DN to connect as */
     private static String mBindDn = null;
     /** The password to use */
     private static String mPassword = null;
     /** The host to connect to */
     private static String mHost = "localhost";
     /** The port to connect using */
     private static int mPort = 389;
     /** Base DN */
     private static String mBaseDn;
     /** non destructive */
     private static boolean mNonDestructive;
 }
