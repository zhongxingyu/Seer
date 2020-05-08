 /*
  * Copyright 2008 Pentaho Corporation.  All rights reserved. 
  * This software was developed by Pentaho Corporation and is provided under the terms 
  * of the Mozilla Public License, Version 1.1, or any later version. You may not use 
  * this file except in compliance with the license. If you need a copy of the license, 
  * please go to http://www.mozilla.org/MPL/MPL-1.1.txt. The Original Code is the Pentaho 
  * BI Platform.  The Initial Developer is Pentaho Corporation.
  *
  * Software distributed under the Mozilla Public License is distributed on an "AS IS" 
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to 
  * the license for the specific language governing your rights and limitations.
  *
  * Created  
  * @author David Kincade
  */
 package org.pentaho.experimental.chart;
 
 import org.jfree.resourceloader.LibLoaderInfo;
 import org.jfree.xmlns.LibXmlInfo;
 import org.pentaho.reporting.libraries.base.LibBaseInfo;
 import org.pentaho.reporting.libraries.base.versioning.ProjectInformation;
 import org.pentaho.reporting.libraries.css.LibCssInfo;
 
 public class ChartInfo extends ProjectInformation {
  public static final ProjectInformation INSTANCE = new ChartInfo();
 
   @SuppressWarnings("nls")
   private ChartInfo() {
     super("chartbundle", "ChartBundle");
 
     setBootClass(ChartBoot.class.getName());
     setLicenseName("LGPL");
     setInfo("http://pentaho.org/chartbundle/");
     setCopyright("(C)opyright 2008, by Pentaho Corporation and Contributors");
 
    addLibrary(LibBaseInfo.INSTANCE);
     addLibrary(LibLoaderInfo.getInstance());
     addLibrary(LibXmlInfo.getInstance());
     addLibrary(LibCssInfo.getInstance());
   }
 }
