 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
  * Contributor(s):
  *
  * The Original Software is NetBeans. The Initial Developer of the Original
  * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
  * Microsystems, Inc. All Rights Reserved.
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
  */
 
 package org.netbeans.modules.javafx.platform.platformdefinition;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 import java.net.MalformedURLException;
 import org.openide.util.Exceptions;
 
 import org.openide.util.NbBundle;
 import org.openide.filesystems.FileUtil;
 import org.netbeans.api.java.platform.*;
 import org.netbeans.api.java.classpath.*;
 import org.openide.modules.InstalledFileLocator;
 
 /**
  * Implementation of the "Default" platform. The information here is extracted
  * from the NetBeans' own runtime.
  *
  * @author Svata Dedic
  */
 public class DefaultPlatformImpl extends JavaFXPlatformImpl {
 
 
     public static final String DEFAULT_PLATFORM_ANT_NAME = "default_fx_platform";           //NOI18N
 
     private ClassPath standardLibs;
     
     @SuppressWarnings("unchecked")  //Properties cast to Map<String,String> // NOI18N
     static JavaPlatform create(Map<String,String> properties, List<URL> sources, List<URL> javadoc) {
         File javaHome = FileUtil.normalizeFile(new File(System.getProperty("java.home")));       //NOI18N
         assert javaHome != null : "java.home supposed to be set";
         assert javaHome.isDirectory() : "java.home supposed to be directory";
         List<URL> javaFolders = new ArrayList<URL>();
         URL fxFolder = null;
         try {
             javaFolders.add(javaHome.toURI().toURL());
         } catch (MalformedURLException mue) {
             Exceptions.printStackTrace(mue);
         }
         File fxPath = InstalledFileLocator.getDefault().locate("javafx-sdk/lib/shared/javafxc.jar", "org.netbeans.modules.javafx", false); // NOI18N
         if (fxPath == null) //try to find runtime in the root javafx folder as for public compiler
             fxPath = InstalledFileLocator.getDefault().locate("lib/shared/javafxc.jar", "org.netbeans.modules.javafx", false); // NOI18N
         if (fxPath != null && fxPath.isFile()) try {
             fxPath = fxPath.getParentFile().getParentFile().getParentFile();
             fxFolder = fxPath.toURI().toURL();
             javaFolders.add(fxFolder);
         } catch (MalformedURLException mue) {
             Exceptions.printStackTrace(mue);
         }
         if ((fxPath!= null) && (sources == null || javadoc == null)) {
             List<URL> src = new ArrayList<URL>(), jdc = new ArrayList<URL>();
            findSourcesAndJavadoc(src, jdc, javaHome, fxPath);
             if (sources == null) sources = src;
             if (javadoc == null) javadoc = jdc;
         }
         if (properties == null) properties = new HashMap<String,String> ();
         loadProfileProperties(fxPath, properties);
         return new DefaultPlatformImpl(javaFolders, fxFolder, properties, new HashMap(System.getProperties()), sources,javadoc);
     }
     
     private DefaultPlatformImpl(List<URL> javaFolders, URL fxFolder, Map<String,String> platformProperties,
         Map<String,String> systemProperties, List<URL> sources, List<URL> javadoc) {
         super(null,DEFAULT_PLATFORM_ANT_NAME, javaFolders, fxFolder, platformProperties, systemProperties, sources, javadoc);
     }
 
     public void setAntName(String antName) {
         throw new UnsupportedOperationException (); //Default platform ant name can not be changed
     }
     
     public String getDisplayName () {
         String displayName = super.getDisplayName();
         if (displayName == null) {
             displayName = NbBundle.getMessage(DefaultPlatformImpl.class,"TXT_DefaultPlatform", getSpecification().getVersion().toString()); // NOI18N
             this.internalSetDisplayName (displayName);
         }
         return displayName;
     }
     
     public void setDisplayName(String name) {
         throw new UnsupportedOperationException (); //Default platform name can not be changed
     }
 
     public ClassPath getStandardLibraries() {
         if (standardLibs != null)
             return standardLibs;
         String s = System.getProperty(SYSPROP_JAVA_CLASS_PATH);       //NOI18N
         if (s == null) {
             s = ""; // NOI18N
         }
         return standardLibs = Util.createClassPath (s);
     }
 
 }
