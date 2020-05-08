 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  * 
  * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
  * 
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  * 
  * If you wish your version of this file to be governed by only the CDDL
  * or only the GPL Version 2, indicate your decision by adding
  * "[Contributor] elects to include this software in this distribution
  * under the [CDDL or GPL Version 2] license." If you do not indicate a
  * single choice of license, a recipient has the option to distribute
  * your version of this file under either the CDDL, the GPL Version 2 or
  * to extend the choice of license to its licensees as provided above.
  * However, if you add GPL Version 2 code and therefore, elected the GPL
  * Version 2 license, then the option applies only if the new code is
  * made subject to such option by the copyright holder.
  * 
  * Contributor(s):
  * 
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 /*
  * Install.java
  *
  */
 package org.netbeans.javafx.install;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 /**
  *
  * @author  Martin Ryzl
  */
 public class Install {
 
     /** The operating system on which NetBeans runs */
     private static int operatingSystem = -1;
 
     /** Operating system is IBM AIX.  */
     public static final int OS_AIX = 64;
 
     /** Operating system is HP-UX.  */
     public static final int OS_HP = 32;
 
     /** Operating system is SGI IRIX.  */
     public static final int OS_IRIX = 128;
 
     /** Operating system is Linux.  */
     public static final int OS_LINUX = 16;
 
     /** Operating system is Mac.  */
     public static final int OS_MAC = 2048;
 
     /** Operating system is OS/2.  */
     public static final int OS_OS2 = 1024;
 
     /** Operating system is unknown.  */
     public static final int OS_OTHER = 65536;
 
     /** Operating system is Solaris.  */
     public static final int OS_SOLARIS = 8;
 
     /** Operating system is Sun OS.  */
     public static final int OS_SUNOS = 256;
 
     /** Operating system is Compaq TRU64 Unix  */
     public static final int OS_TRU64 = 512;
 
     /** A mask for Unix platforms.  */
     public static final int OS_UNIX_MASK = OS_SOLARIS | OS_LINUX | OS_HP | OS_AIX | OS_IRIX | OS_SUNOS | OS_TRU64 | OS_MAC;
 
     /** Operating system is Compaq OpenVMS  */
     public static final int OS_VMS = 8192;
 
     /** Operating system is Windows 2000.  */
     public static final int OS_WIN2000 = 4096;
 
     /** Operating system is Windows 95.  */
     public static final int OS_WIN95 = 2;
 
     /** Operating system is Windows 98.  */
     public static final int OS_WIN98 = 4;
 
     /** Operating system is Windows NT.  */
     public static final int OS_WINNT = 1;
 
     /**
      * Operating system is one of the Windows variants but we don't know which
      * one it is
      */
     public static final int OS_WIN_OTHER = 16384;
 
     /** A mask for Windows platforms.  */
     public static final int OS_WINDOWS_MASK = OS_WINNT | OS_WIN95 | OS_WIN98 | OS_WIN2000 | OS_WIN_OTHER;
 
 
     /** Creates a new instance of Install */
     public Install() {
     }
 
     public static void install(String targetDir) throws IOException {
         if (isWindows()) installWindows(targetDir);
         else if (isUnix()) installUnix(targetDir);
     }
 
     private static void installWindows(String targetDir) throws IOException {
     }
 
     private static void installUnix(String targetDir) throws IOException {
 		setPermission(targetDir + File.separatorChar + "bin", "javafx");
 		setPermission(targetDir + File.separatorChar + "bin", "javafxc");
 		setPermission(targetDir + File.separatorChar + "bin", "javafxdoc");
 		setPermission(targetDir + File.separatorChar + "bin", "javafxpackager");
 		setPermission(targetDir + File.separatorChar + "lib" + File.separatorChar + "desktop", "jmcServerDaemon");
 		setPermission(targetDir + File.separatorChar + "emulator" + File.separatorChar + "mobile" + File.separatorChar + "bin", "preverify");
 		setPermission(targetDir + File.separatorChar + "emulator" + File.separatorChar + "mobile" + File.separatorChar + "bin", "emulator");
 		setPermission(targetDir + File.separatorChar + "emulator" + File.separatorChar + "tv" + File.separatorChar + "bin", "cvm");
     }
 
     private static void setPermission(String folder, String file) throws IOException {
         File f= new File(folder, file);
         if (f.isFile()) {
             Runtime.getRuntime().exec(new String[] {
                 "chmod", // NOI18N
                 "+x", // NOI18N,
                 f.getCanonicalPath()
             });
         }
      }
 
     private static String quoteString(String s) {
         if (s.indexOf(' ') != -1) return '\"' + s + '\"';
         return s;
     }
 
     public static void main(String[] args) {
         try {
             String sdkUser = System.getProperty("sdk.home.user"); // NOI18N
             String sdkInstall = System.getProperty("sdk.home.install"); // NOI18N    
             if (sdkUser != null) install(sdkUser);
             if (sdkInstall != null) install(sdkInstall);
         } catch (IOException ex) {
             // under normal circumstances it should never happen
             ex.printStackTrace();
         }
     }
 
     /** Test whether the IDE is running on some variant of Unix.
      * Linux is included as well as the commercial vendors.
      * @return <code>true</code> some sort of Unix, <code>false</code> if some other manner of operating system
      */
     public static final boolean isUnix() {
         return (getOperatingSystem() & OS_UNIX_MASK) != 0;
     }
 
     /** Test whether the IDE is running on some variant of Windows.
      * @return <code>true</code> if Windows, <code>false</code> if some other manner of operating system
      */
     public static final boolean isWindows() {
         return (getOperatingSystem() & OS_WINDOWS_MASK) != 0;
     }
 
     /** Get the operating system on which the IDE is running.
      * @return one of the <code>OS_*</code> constants (such as {@link #OS_WINNT})
      */
     public static final int getOperatingSystem() {
         if (operatingSystem == -1) {
             String osName = System.getProperty("os.name");
             if ("Windows NT".equals(osName)) // NOI18N
                 operatingSystem = OS_WINNT;
             else if ("Windows 95".equals(osName)) // NOI18N
                 operatingSystem = OS_WIN95;
             else if ("Windows 98".equals(osName)) // NOI18N
                 operatingSystem = OS_WIN98;
             else if ("Windows 2000".equals(osName)) // NOI18N
                 operatingSystem = OS_WIN2000;
             else if (osName.startsWith("Windows ")) // NOI18N
                 operatingSystem = OS_WIN_OTHER;
             else if ("Solaris".equals(osName)) // NOI18N
                 operatingSystem = OS_SOLARIS;
             else if (osName.startsWith("SunOS")) // NOI18N
                 operatingSystem = OS_SOLARIS;
             // JDK 1.4 b2 defines os.name for me as "Redhat Linux" -jglick
             else if (osName.endsWith("Linux")) // NOI18N
                 operatingSystem = OS_LINUX;
             else if ("HP-UX".equals(osName)) // NOI18N
                 operatingSystem = OS_HP;
             else if ("AIX".equals(osName)) // NOI18N
                 operatingSystem = OS_AIX;
             else if ("Irix".equals(osName)) // NOI18N
                 operatingSystem = OS_IRIX;
             else if ("SunOS".equals(osName)) // NOI18N
                 operatingSystem = OS_SUNOS;
             else if ("Digital UNIX".equals(osName)) // NOI18N
                 operatingSystem = OS_TRU64;
             else if ("OS/2".equals(osName)) // NOI18N
                 operatingSystem = OS_OS2;
             else if ("OpenVMS".equals(osName)) // NOI18N
                 operatingSystem = OS_VMS;
             else if (osName.equals("Mac OS X")) // NOI18N
                 operatingSystem = OS_MAC;
             else if (osName.startsWith("Darwin")) // NOI18N
                 operatingSystem = OS_MAC;
             else
                 operatingSystem = OS_OTHER;
         }
         return operatingSystem;
     }
 
 }
