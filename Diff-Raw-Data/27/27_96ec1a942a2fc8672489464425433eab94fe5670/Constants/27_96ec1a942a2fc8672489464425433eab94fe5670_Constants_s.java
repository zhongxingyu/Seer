 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.sdk.templates;
 
 /**
  * The set of built-in keys of a template context
  * 
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public interface Constants {
 
    public static final String NXSDK_BROWSER_LINK_FOLDER = "nxsdk_browser";
 
     public static final String CLASSPATH_CONTAINER = "classpathContainer";
 
     public static final String TEST_CLASSPATH_CONTAINER = "testClasspathContainer";
 
     public final static String PROJECT_ID = "projectId";
 
     public final static String PROJECT_NAME = "projectName";
 
     public final static String PROJECT_PACKAGE = "package";
 
     /**
      * The '/' separated path of the java PROJECT_PACKAGE
      */
     public final static String PROJECT_PACKAGE_PATH = "packagePath";
 
     public final static String TARGET_VERSION = "targetVersion";
 
     public final static String BUNDLE_VERSION = "bundleVersion";
 
     public final static String GROUP_ID = "groupId";
 
     public final static String ARTIFACT_ID = "artifactId";
 
     public final static String ARTIFACT_VERSION = "artifactVersion";
 
     public final static String ARTIFACT_NAME = "artifactName";
 
     public final static String ARTIFACT_DESCRIPTION = "artifactDescription";
 
     public final static String PARENT_VERSION = "parentVersion";
 
     public final static String PARENT_GROUP_ID = "parentGroupId";
 
     public final static String PARENT_ARTIFACT_ID = "parentArtifactId";
 
 }
