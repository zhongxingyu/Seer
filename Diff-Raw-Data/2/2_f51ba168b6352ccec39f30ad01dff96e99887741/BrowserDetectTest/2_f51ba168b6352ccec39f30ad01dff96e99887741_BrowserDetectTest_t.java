 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tests.embedded.jsf;
 
 import com.flexive.war.FxRequest;
 import static com.flexive.war.FxRequest.Browser;
 import static com.flexive.war.FxRequest.OperatingSystem;
 import com.flexive.war.filter.BrowserDetect;
 import com.google.common.collect.Maps;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import java.util.Map;
 
 /**
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 @Test(groups = {"jsf"})
 public class BrowserDetectTest {
 
     private static final Map<String, Expected> tests = Maps.newHashMap();
 
     static {
         tests.put(
                 "Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.0.1) Gecko/20060111 Firefox/1.5.0.1",
                 expect(Browser.FIREFOX,  1.5, OperatingSystem.WINDOWS)
         );
         tests.put(
                 "Mozilla/5.0 (Windows; U; Windows NT 5.1; de-DE; rv:1.4) Gecko/20030619 Netscape/7.1 (ax)",
                expect(Browser.GECKO, 1.4, OperatingSystem.WINDOWS)   // netscape not handled
         );
         tests.put(
                 "Mozilla/5.0 Galeon/1.2.7 (X11; Linux i686; U;) Gecko/20030131",
                 expect(Browser.GALEON, 1.2, OperatingSystem.LINUX)
         );
         tests.put(
                 "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.3) Gecko/20040924 Epiphany/1.4.4 (Ubuntu)",
                 expect(Browser.EPIPHANY, 1.4, OperatingSystem.LINUX)
         );
         tests.put(
                 "Mozilla/5.0 (compatible; Konqueror/3.5; Linux) KHTML/3.5.5 (like Gecko).",
                 expect(Browser.KONQUEROR, 3.5, OperatingSystem.LINUX)
         );
         tests.put(
                 "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; de-de) AppleWebKit/125.2 (KHTML, like Gecko) Safari/125.8",
                 expect(Browser.SAFARI, 125.8, OperatingSystem.MAC)
         );
         tests.put(
                 "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.0)",
                 expect(Browser.IE, 7.0, OperatingSystem.WINDOWS)
         );
         tests.put(
                 "Mozilla/4.0 (compatible; MSIE 5.0; Windows 98) Opera 5.12 [de]",
                 expect(Browser.OPERA, 5.12, OperatingSystem.WINDOWS)
         );
         tests.put(
                 "Lynx/2.8.6rel.4 libwww-FM/2.14",
                 expect(Browser.UNKNOWN, -1, OperatingSystem.UNKNOWN)
         );
     }
 
     public void browserIdentity() {
         for (Map.Entry<String, Expected> test : tests.entrySet()) {
             final Browser browser = new BrowserDetect(test.getKey()).getBrowser();
             Assert.assertEquals(
                     browser,
                     test.getValue().getBrowser(),
                     "Unexpected browser for user agent '" + test.getKey() + "': " + browser
             );
         }
     }
 
     public void browserVersions() {
         for (Map.Entry<String, Expected> test : tests.entrySet()) {
             final double version = new BrowserDetect(test.getKey()).getBrowserVersion();
             if (test.getValue().getVersion() > 0) {
                 Assert.assertEquals(
                         version,
                         test.getValue().getVersion(),
                         "Unexpected version for user agent '" + test.getKey() + "': " + version
                 );
             }
         }
     }
 
     private static Expected expect(Browser browser, double browserVersion, FxRequest.OperatingSystem os) {
         return new Expected(browser, browserVersion, os);
     }
 
     private static class Expected {
         private final FxRequest.Browser browser;
         private final double version;
         private final FxRequest.OperatingSystem os;
 
         private Expected(FxRequest.Browser browser, double version, FxRequest.OperatingSystem os) {
             this.browser = browser;
             this.version = version;
             this.os = os;
         }
 
         public FxRequest.Browser getBrowser() {
             return browser;
         }
 
         public double getVersion() {
             return version;
         }
 
         public FxRequest.OperatingSystem getOs() {
             return os;
         }
     }
 
 }
