 // BSD-licensed, see COPYRIGHT file
 // Copyright 2011, Ernst de Haan
 package org.znerd.uasniffer;
 
 import org.znerd.util.text.TextUtils;
 
 /**
  * Class responsible for determining the user agent details.
  */
 public final class Sniffer {
 
     private static final String[] UA_MOBILE_DEVICE_SNIPPETS = new String[] { "windows ce", "windowsce", "symbian", "nokia", "opera mini", "wget", "fennec", "opera mobi", "windows; ppc", "blackberry", "portable", "vita" };
     private static final String[] UA_TABLET_DEVICE_SNIPPETS = new String[] { "ipad", "xoom", "tablet" };
     private static final String[] UA_MOBILE_DEVICE_WITHOUT_TEL_SUPPORT = new String[] { "opera/8.", "opera/7.", "opera/6.", "opera/5.", "opera/4.", "opera/3.", "ipod", "playstation" };
     private static final String[] UA_BOT_SNIPPETS = new String[] { "spider", "bot", "crawl", "miner", "checker", "java", "pingdom" };
 
     private Sniffer() {
     }
 
     /**
      * Analyzes the specified user agent string.
      * 
      * @param agentString
      *            the user agent string, cannot be <code>null</code>.
      * @return an {@link UserAgent} instance that describes the user agent, never <code>null</code>.
      * @throws IllegalArgumentException
      *             if <code>agentString == null</code>.
      */
     public static final UserAgent analyze(String agentString) throws IllegalArgumentException {
         UserAgent ua = new UserAgent(agentString);
         analyze(ua);
         return ua;
     }
 
     private static final void analyze(UserAgent ua) {
 
         String agentString = ua.getLowerCaseAgentString();
 
         // Detect specific devices
         boolean android = agentString.contains("android");
         boolean appleTouch = agentString.contains("ipod") || agentString.contains("iphone") || agentString.contains("ipad");
         boolean nook = agentString.contains("nook ") || agentString.contains("nook/") || agentString.contains("bntv250");
         boolean psp = agentString.contains("playstation portable") || agentString.contains("playstation vita");
         boolean kindleFire = agentString.contains("silk-accelerated") && agentString.contains("silk/1.1."); // TODO
 
         // Mobile devices
         boolean matchFound = false;
         String uaType = "desktop";
         boolean isPhone = false;
         boolean isTablet = false;
 
         if (nook) {
             matchFound = true;
             uaType = "ereader";
             isPhone = false;
             isTablet = false;
         } else if (kindleFire) {
             matchFound = true;
             uaType = "ereader";
             isPhone = false;
             isTablet = true;
         } else {
             for (String mobileDeviceSnippet : UA_MOBILE_DEVICE_SNIPPETS) {
                 if (agentString.contains(mobileDeviceSnippet)) {
                     matchFound = true;
                     uaType = "mobile";
                     isPhone = true;
 
                     for (String mobileWithoutTelSnippet : UA_MOBILE_DEVICE_WITHOUT_TEL_SUPPORT) {
                         if (agentString.contains(mobileWithoutTelSnippet)) {
                             isPhone = false;
                         }
                     }
                 }
             }
 
             // Tablets
             for (String tabletDeviceSnippet : UA_TABLET_DEVICE_SNIPPETS) {
                 if (agentString.contains(tabletDeviceSnippet)) {
                     isTablet = true;
                 }
             }
         }
 
         if (!matchFound) {
             if (agentString.contains("ipod")) { // iPod
                 matchFound = true;
                 uaType = "desktop";
                 isPhone = false;
 
             } else if (agentString.contains("iphone")) { // iPhone
                 matchFound = true;
                 uaType = "desktop";
                 isPhone = true;
 
             } else if (agentString.contains("ipad")) { // iPad
                 matchFound = true;
                 uaType = "tablet";
                 isPhone = false;
 
             } else if (!isTablet && agentString.contains("android")) { // Android
                 matchFound = true;
                 uaType = "desktop";
                 isPhone = true;
 
             } else if (agentString.contains("pre/")) { // Palm Pre
                 matchFound = true;
                 uaType = "desktop";
                 isPhone = true;
 
             } else if (agentString.contains("kindle/")) { // Amazon Kindle
                 matchFound = true;
                 uaType = "ereader";
                 isPhone = false;
 
             } else { // Bots
                 for (String botSnippet : UA_BOT_SNIPPETS) {
                     if (agentString.contains(botSnippet)) {
                         matchFound = true;
                         uaType = "bot";
                         isPhone = false;
                     }
                 }
             }
         }
 
         // Categorize Device
         if (isPhone) {
             ua.addName("Device-Phone");
         } else {
             ua.addName("Device-NoPhone");
         }
 
         if ("ereader".equals(uaType)) {
             ua.addName("Device-Mobile");
             ua.addName("Device-Ereader");
         } else if ("mobile".equals(uaType) || appleTouch || android || agentString.contains("webos/")) {
             ua.addName("Device-Mobile");
         } else if ("bot".equals(uaType)) {
             ua.addName("Device-Bot");
         } else if (!isTablet) {
             ua.addName("Device-Desktop");
         }
 
         if (isTablet) {
             ua.addName("Device-Tablet");
         }
 
         if (psp) {
             ua.addName("Device-Gaming");
             ua.addName("Device-Mobile");
             ua.addName("Device-PSP");
             if (agentString.contains("vita")) {
                 analyze(ua, agentString, "Device-PSP-Vita", "vita ", 2, false);
             }
         }
         
         if (kindleFire) {
             ua.addName("Device-AmazonKindle");
             ua.addName("Device-AmazonKindle-Fire");
             
             if (agentString.contains("silk-accelerated=true")) {
                 ua.addName("CloudAcceleration-Yes");
             } else if (agentString.contains("silk-accelerated=false")) {
                 ua.addName("CloudAcceleration-No");
             }
         }
 
         if (appleTouch) {
             ua.addName("Device-AppleTouch");
             if (agentString.contains("ipod")) {
                 ua.addName("Device-AppleTouch-iPod");
             } else if (agentString.contains("ipad")) {
                 ua.addName("Device-AppleTouch-iPad");
             } else {
                 ua.addName("Device-AppleTouch-iPhone");
             }
         } else if (agentString.contains("blackberry")) {
             analyze(ua, agentString, "Device-Blackberry", "blackberry", 1, false);
             analyze(ua, agentString, "Device-Blackberry", "blackberry ", 1, false);
         } else if (agentString.contains("kindle/")) {
             analyze(ua, agentString, "Device-AmazonKindle", "kindle/", 2, false);
         }
 
         // Detect OS, browser engine and browser
         if (!"bot".equals(uaType)) {
             detectBrowserOS(ua);
             detectBrowserEngine(ua);
             detectBrowser(ua);
         }
     }
 
     private static final void detectBrowserOS(UserAgent ua) {
 
         String agentString = ua.getLowerCaseAgentString();
 
         boolean nook = agentString.contains("nook ") || agentString.contains("nook/") || agentString.contains("bntv250");
 
         // Maemo - check before Linux
         if (agentString.contains("maemo")) {
             ua.addName("BrowserOS-NIX");
             ua.addName("BrowserOS-Linux");
             ua.addName("BrowserOS-Linux-Maemo");
         }
 
         // Linux
         if (agentString.contains("linux") || agentString.contains("android") || nook) {
             ua.addName("BrowserOS-NIX");
             ua.addName("BrowserOS-Linux");
             if (agentString.contains("linux 2.")) {
                 analyze(ua, agentString, "BrowserOS-Linux", "linux ");
             }
 
             // Android
             if (agentString.contains("android") || nook) {
                 analyze(ua, agentString, "BrowserOS-Linux-Android", "android ");
             }
 
             // Google Chrome OS
         } else if (agentString.contains("cros ")) {
             ua.addName("BrowserOS-CrOS");
 
             // webOS, by Palm
         } else if (agentString.contains("webos/")) {
             analyze(ua, agentString, "BrowserOS-WebOS", "webos/");
 
             // iOS (detect before Mac OS)
         } else if (agentString.contains("iphone") || agentString.contains("ipod") || agentString.contains("ipad")) {
             analyze(ua, agentString.replace('_', '.').replace("mac os x", ""), "BrowserOS-iOS", "OS ");
 
             // Mac OS
         } else if (agentString.contains("mac os") || agentString.contains("mac_") || agentString.contains("macintosh")) {
             ua.addName("BrowserOS-MacOS");
 
             // Mac OS X
             if (agentString.contains("mac os x")) {
                 ua.addName("BrowserOS-NIX");
                 ua.addName("BrowserOS-MacOS-10");
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x ", 0, false);
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x tiger ", 0, false);
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x leopard ", 0, false);
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x snow leopard ", 0, false);
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x lion ", 0, false);
                 analyze(ua, agentString.replace('_', '.'), "BrowserOS-MacOS", "mac os x mountain lion ", 0, false);
             }
 
             // Windows
         } else if (agentString.contains("windows") || agentString.contains("win3.") || agentString.contains("win9") || agentString.contains("winnt") || agentString.contains("wince")) {
             ua.addName("BrowserOS-Windows");
             if (agentString.contains("windows nt")) {
                 analyze(ua, agentString, "BrowserOS-Windows-NT", "windows nt ", 2, true);
             } else if (agentString.contains("windows 5.") || agentString.contains("windows 6.")) {
                 analyze(ua, agentString, "BrowserOS-Windows-NT", "windows ", 2, false);
             } else if (agentString.contains("windows vista")) {
                 analyze(ua, "nt/6.0", "BrowserOS-Windows-NT", "nt/", 2, false);
             } else if (agentString.contains("windows xp")) {
                 analyze(ua, "nt/5.1", "BrowserOS-Windows-NT", "nt/", 2, false);
             } else if (agentString.contains("windows 2000")) {
                 analyze(ua, "nt/5.0", "BrowserOS-Windows-NT", "nt/", 2, false);
             } else if (agentString.contains("winnt")) {
                 analyze(ua, agentString, "BrowserOS-Windows-NT", "winnt", 2, true);
 
                 // Windows ME (needs to be checked before Windows 98)
             } else if (agentString.contains("win 9x 4.90") || agentString.contains("windows me")) {
                 ua.addName("BrowserOS-Windows-ME");
 
                 // Windows 98
             } else if (agentString.contains("windows 98") || agentString.contains("win98")) {
                 ua.addName("BrowserOS-Windows-98");
 
                 // Windows 95
             } else if (agentString.contains("windows 95") || agentString.contains("win95")) {
                 ua.addName("BrowserOS-Windows-95");
 
                 // Windows Mobile
             } else if (agentString.contains("windows mobile") || agentString.contains("windows; ppc") || agentString.contains("windows ce") || agentString.contains("wince")) {
                 analyze(ua, agentString, "BrowserOS-Windows-Mobile", "windows mobile ", 3, true);
 
                 // Windows 3.x
             } else if (agentString.contains("windows 3.")) {
                 analyze(ua, agentString, "BrowserOS-Windows", "windows ", 3, true);
             } else if (agentString.contains("win3.")) {
                 int indexWin3 = agentString.indexOf("win3.");
                 int indexWindows = agentString.indexOf("windows");
                 String s = (indexWindows >= 0 && indexWindows < indexWin3) ? agentString.substring(indexWindows + 1) : agentString;
 
                 analyze(ua, s, "BrowserOS-Windows", "win", 3, true);
             }
 
             // Add some marketing names for various Windows versions
             if (ua.hasName("BrowserOS-Windows-NT-5-0")) {
                 ua.addName("BrowserOS-Windows-2000");
             } else if (ua.hasName("BrowserOS-Windows-NT-5")) {
                 ua.addName("BrowserOS-Windows-XP");
             } else if (ua.hasName("BrowserOS-Windows-NT-6-0")) {
                 ua.addName("BrowserOS-Windows-Vista");
             } else if (ua.hasName("BrowserOS-Windows-NT-6-1")) {
                 ua.addName("BrowserOS-Windows-7");
             } else if (ua.hasName("BrowserOS-Windows-NT-6-2")) {
                 ua.addName("BrowserOS-Windows-8");
             }
 
             // DragonFlyBSD, extra check
         } else if (agentString.contains("dragonfly")) {
             ua.addName("BrowserOS-NIX");
             ua.addName("BrowserOS-BSD");
             ua.addName("BrowserOS-BSD-DragonFlyBSD");
 
             // Other BSD variants
         } else if (agentString.contains("bsd")) {
             ua.addName("BrowserOS-NIX");
             ua.addName("BrowserOS-BSD");
             if (agentString.contains("netbsd")) {
                 ua.addName("BrowserOS-BSD-NetBSD");
             } else if (agentString.contains("openbsd")) {
                 ua.addName("BrowserOS-BSD-OpenBSD");
             } else if (agentString.contains("freebsd")) {
                 ua.addName("BrowserOS-BSD-FreeBSD");
             }
 
             // AIX
         } else if (agentString.contains("aix")) {
             ua.addName("BrowserOS-NIX");
             analyze(ua, agentString, "BrowserOS-AIX", "aix ", 1, false);
 
             // IRIX
         } else if (agentString.contains("irix")) {
             ua.addName("BrowserOS-NIX");
             analyze(ua, agentString, "BrowserOS-IRIX", "irix ", 2, false);
             analyze(ua, agentString, "BrowserOS-IRIX", "irix64 ", 2, false);
 
             // HP-UX
         } else if (agentString.contains("hp-ux")) {
             ua.addName("BrowserOS-NIX");
             ua.addName("BrowserOS-HPUX");
 
             // Sun Solaris
         } else if (agentString.contains("sunos")) {
             ua.addName("BrowserOS-NIX");
             analyze(ua, agentString, "BrowserOS-Solaris", "sunos ", 1, false);
 
             // Sun Solaris
         } else if (agentString.contains("beos")) {
             ua.addName("BrowserOS-BeOS");
 
             // OS/2 (a.k.a. Ecomstation)
         } else if (agentString.contains("(os/2")) {
             analyze(ua, agentString, "BrowserOS-OS2", "warp ", 1, false);
 
             // Symbian
         } else if (agentString.contains("symbian")) {
             analyze(ua, agentString, "BrowserOS-Symbian", "symbianos/", 3, false);
 
         } else if (agentString.contains("bada/")) {
             analyze(ua, agentString, "BrowserOS-Bada", "bada/", 2, false);
         }
     }
 
     private static final void detectBrowserEngine(UserAgent ua) {
         String agentString = ua.getLowerCaseAgentString();
 
         // Apple WebKit
         if (agentString.contains("applewebkit/")) {
             analyze(ua, agentString, "BrowserEngine-WebKit", "applewebkit/", 4, false);
         } else if (agentString.contains("apple webkit/")) {
             analyze(ua, agentString, "BrowserEngine-WebKit", "apple webkit/", 4, false);
 
             // Mozilla Gecko
         } else if (agentString.contains("gecko/")) {
             analyze(ua, agentString, "BrowserEngine-Gecko", "rv:", 4, false);
 
             // Opera Presto
         } else if (agentString.contains("presto/")) {
             analyze(ua, agentString, "BrowserEngine-Presto", "presto/", 3, false);
         } else if (agentString.contains("presto")) {
             analyze(ua, agentString, "BrowserEngine-Presto", "presto ", 3, false);
 
             // Microsoft Trident
         } else if (agentString.contains("trident/")) {
             analyze(ua, agentString, "BrowserEngine-Trident", "trident/", 3, false);
         } else if (agentString.contains("trident")) {
             analyze(ua, agentString, "BrowserEngine-Trident", "trident ", 3, false);
 
             // KDE KHTML
         } else if (agentString.contains("khtml/")) {
             analyze(ua, agentString, "BrowserEngine-KHTML", "khtml/", 3, false);
         }
     }
 
     private static final void detectBrowser(UserAgent ua) {
 
         String agentString = ua.getLowerCaseAgentString();
 
         // Lunascape, can use different rendering engines
         // E.g.: Lunascape5 (Webkit) - Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/528+ (KHTML, like Gecko, Safari/528.0) Lunascape/5.0.3.0
         if (agentString.contains("lunascape")) {
             analyze(ua, agentString, "Browser-Lunascape", "lunascape ", 4, false);
             analyze(ua, agentString, "Browser-Lunascape", "lunascape/", 4, false);
 
             // Maxthon
         } else if (agentString.contains("maxthon")) {
             analyze(ua, agentString, "Browser-Maxthon", "maxthon ", 4, false);
             analyze(ua, agentString, "Browser-Maxthon", "maxthon/", 4, false);
 
             // Blackberry
         } else if (agentString.contains("blackberry")) {
             analyze(ua, agentString, "Browser-Blackberry", "version/");
 
             // Konqueror (needs to be detected before Gecko-based browsers)
             // E.g.: Mozilla/5.0 (compatible; Konqueror/4.1; Linux) KHTML/4.1.2 (like Gecko)
         } else if (agentString.contains("konqueror")) {
             analyze(ua, agentString, "Browser-Konqueror", "konqueror/", 2, false);
             ua.addName("BrowserEngine-KHTML");
 
             // Fennec
             // E.g.: Mozilla/5.0 (Macintosh; U; Intel Mac OS X; en-US; rv:1.9.2a1pre) Gecko/20090317 Fennec/1.0b1
             // Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.1b2pre) Gecko/20081015 Fennec/1.0a1
             // Mozilla/5.0 (X11; U; Linux armv7l; en-US; rv:1.9.2a1pre) Gecko/20090322 Fennec/1.0b2pre
         } else if (agentString.contains("fennec")) {
             analyze(ua, agentString, "Browser-Fennec", "fennec/");
             analyze(ua, agentString, "Browser-MobileFirefox", "fennec/");
 
             // Epiphany
             // E.g.: Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.7.3) Gecko/20041007 Epiphany/1.4.7
         } else if (agentString.contains("epiphany")) {
             analyze(ua, agentString, "Browser-Epiphany", "epiphany/");
 
             // Flock (needs to be detected before Firefox and Chrome)
             // E.g.: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.18) Gecko/20081107 Firefox/2.0.0.18 Flock/1.2.7
             // or: Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.7 (KHTML, like Gecko) Flock/3.5.2.4599 Chrome/7.0.517.442 Safari/534.7
         } else if (agentString.contains("flock")) {
             analyze(ua, agentString, "Browser-Flock", "flock/", 4, false);
 
             // Camino (needs to be detected before Firefox)
             // E.g.: Mozilla/5.0 (Macintosh; U; Intel Mac OS X; nl; rv:1.8.1.14) Gecko/20080512 Camino/1.6.1 (MultiLang) (like Firefox/2.0.0.14)
         } else if (agentString.contains("camino")) {
             analyze(ua, agentString, "Browser-Camino", "camino/");
 
             // SeaMonkey
             // E.g.: Mozilla/5.0 (X11; U; Linux x86_64; en-US; rv:1.9.1b3pre) Gecko/20090302 SeaMonkey/2.0b1pre
         } else if (agentString.contains("seamonkey/")) {
             analyze(ua, agentString, "Browser-SeaMonkey", "seamonkey/");
 
             // SeaMonkey (again)
             // E.g.: Seamonkey-1.1.13-1(X11; U; GNU Fedora fc 10) Gecko/20081112
         } else if (agentString.contains("seamonkey-")) {
             analyze(ua, agentString, "Browser-SeaMonkey", "seamonkey-");
             ua.addName("BrowserEngine-Gecko");
 
             // Netscape Navigator (needs to be detected before Firefox)
             // E.g.: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.5pre) Gecko/20070712 Firefox/2.0.0.4 Navigator/9.0b2
         } else if (agentString.contains("navigator/")) {
             analyze(ua, agentString, "Browser-Netscape", "navigator/");
             ua.addName("BrowserEngine-Gecko");
 
             // Firefox
         } else if (agentString.contains("firefox")) {
             analyze(ua, agentString, "Browser-Firefox", "firefox/");
             if (agentString.contains("mobile") || agentString.contains("tablet")) {
                 analyze(ua, agentString, "Browser-MobileFirefox", "firefox/");
             }
         } else if (agentString.contains("minefield/")) {
             analyze(ua, agentString, "Browser-Firefox", "minefield/");
             if (agentString.contains("mobile")) {
                 analyze(ua, agentString, "Browser-MobileFirefox", "firefox/");
             }
         } else if (agentString.contains("namoroka/")) {
             analyze(ua, agentString, "Browser-Firefox", "namoroka/"); // Firefox 3.6 pre-releases
         } else if (agentString.contains("shiretoko/")) {
             analyze(ua, agentString, "Browser-Firefox", "shiretoko/"); // Firefox 3.5 pre-releases
        } else if (agentString.contains("granparadiso/")) {
            analyze(ua, agentString, "Browser-Firefox", "granparadiso/"); // Firefox 3.0/3.1 pre-releases
         } else if (agentString.contains("firebird/")) {
             analyze(ua, agentString, "Browser-Firefox", "firebird/"); // Before 1.0
         } else if (agentString.contains("phoenix/")) {
             analyze(ua, agentString, "Browser-Firefox", "phoenix/"); // Before 1.0 (and before Firebird code-name)
 
             // Opera
         } else if (agentString.startsWith("opera/")) {
 
             ua.addName("BrowserEngine-Presto");
             ua.addName("Browser-Opera");
 
             // Opera Mobile
             if (agentString.contains("tablet")) {
                 analyze(ua, agentString, "Browser-OperaTablet", "version/", 3, true);
             } else if (agentString.contains("mobi/")) {
                 analyze(ua, agentString, "Browser-OperaMobile", agentString.contains("version/") ? "version/" : "opera/", 3, true);
 
                 // Opera Mini
             } else if (agentString.contains("mini/")) {
                 analyze(ua, agentString, "Browser-OperaMini", "mini/", 3, true);
 
                 // Opera Desktop
             } else {
                 analyze(ua, agentString, "Browser-OperaDesktop", agentString.contains("version/") ? "version/" : "opera/", 3, true);
             }
 
             // Opera (older releases)
         } else if (agentString.contains("opera")) {
             ua.addName("Browser-Opera");
             analyze(ua, agentString, "Browser-OperaDesktop", "opera ");
             ua.addName("BrowserEngine-Presto");
 
             // Palm Pre browser - this one needs to be checked before Safari
         } else if (agentString.contains("pre/")) {
             analyze(ua, agentString, "Browser-PalmPreBrowser", "version/");
 
             // OmniWeb - this one needs to be checked before Safari
         } else if (agentString.contains("omniweb")) {
             ua.addName("Browser-OmniWeb");
 
             // RockMelt - this one needs to be checked before Google Chrome
             // e.g.: Mozilla/5.0 (Windows; U; Windows NT 6.1; en-US) AppleWebKit/534.13 (KHTML, like Gecko) RockMelt/0.9.48.51 Chrome/9.0.597.107 Safari/534.13
         } else if (agentString.contains("rockmelt")) {
             analyze(ua, agentString, "Browser-RockMelt", "rockmelt/", 4, false);
 
             // Google Chrome - this one needs to be checked before Safari
             // e.g.: Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/525.13 (KHTML, like Gecko) Chrome/0.X.Y.Z Safari/525.13.
         } else if (agentString.contains("chrome/")) {
             analyze(ua, agentString, "Browser-Chrome", "chrome/", 4, false);
 
             // Nokia browser - needs to be checked before Safari
         } else if (agentString.contains("symbianos")) {
             if (agentString.contains("version/") || !agentString.contains("browserng/")) {
                 analyze(ua, agentString, "Browser-Nokia", "version/", 3, false);
             } else {
                 analyze(ua, agentString, "Browser-Nokia", "browserng/", 3, false);
             }
 
             // NetFront
         } else if (agentString.contains("netfront")) {
             analyze(ua, agentString, "Browser-NetFront", "netfront/", 3, true);
 
             // Amazon Kindle browser (detect after NetFront but before Safari)
         } else if (agentString.contains("kindle/")) {
             analyze(ua, agentString, "Browser-Kindle", "version/", 2, true);
 
             // Dolphin, check before Safari
         } else if (agentString.contains("dolfin")) {
             analyze(ua, agentString, "Browser-Dolphin", "dolfin/", 2, true);
 
             // Nook, check before Safari
         } else if (agentString.contains("nook ") || agentString.contains("bntv250 ")) {
             if (agentString.contains("nook browser/")) {
                 analyze(ua, agentString, "Browser-Nook", "browser/", 2, true);
             } else {
                 analyze(ua, agentString, "Browser-Nook", "version/", 2, true);
             }
 
         } else if (agentString.contains("silk/")) {
             analyze(ua, agentString, "Browser-Silk", "silk/", 2, true);
 
             // iCab, check before Safari
             // E.g.: iCab/4.5 (Macintosh; U; Mac OS X Leopard 10.5.7)
         } else if (agentString.contains("icab")) {
             analyze(ua, agentString, "Browser-iCab", "icab/");
             analyze(ua, agentString, "Browser-iCab", "icab ");
 
             // iCab 4 uses the WebKit rendering engine, although the user agent
             // string does not advertise that
             if (ua.hasName("Browser-iCab-4")) {
                 ua.addName("BrowserEngine-WebKit");
             }
 
             // Apple Safari
         } else if (agentString.contains("safari") || agentString.contains("applewebkit")) {
             ua.addName("BrowserEngine-WebKit");
             ua.addName("Browser-Safari");
 
             if (agentString.contains("mobile/") || agentString.contains("android")) {
                 analyze(ua, agentString, "Browser-MobileSafari", "version/");
             } else {
                 analyze(ua, agentString, "Browser-DesktopSafari", "version/");
             }
 
             // Netscape (again)
         } else if (agentString.contains("netscape6")) {
             analyze(ua, agentString, "Browser-Netscape", "netscape6/");
             ua.addName("Browser-Netscape");
             ua.addName("Browser-Netscape-6");
             ua.addName("BrowserEngine-Gecko");
         } else if (agentString.contains("netscape")) {
             analyze(ua, agentString, "Browser-Netscape", "netscape/", 3, true);
             ua.addName("BrowserEngine-Gecko");
 
 
             // Internet Explorer
         } else if (agentString.contains("msie")) {
             ua.addName("BrowserEngine-Trident");
             ua.addName("Browser-MSIE");
 
             // Mobile IE
             if (agentString.contains("iemobile")) {
                 analyze(ua, agentString, "Browser-MobileMSIE", "iemobile ", 3, true);
             } else if (ua.hasName("BrowserOS-Windows-Mobile")) {
                 ua.addName("Browser-MobileMSIE");
             } else {
                 analyze(ua, agentString, "Browser-DesktopMSIE", "msie ", 3, true);
 
                 // Chrome Frame
                 if (agentString.contains("chromeframe")) {
                     analyze(ua, agentString, "BrowserEngine-ChromeFrame", "chromeframe/", 4, false);
                 }
             }
 
             // NCSA Mosaic
         } else if (agentString.startsWith("ncsa_mosaic") || agentString.startsWith("ncsa mosaic")) {
             analyze(ua, agentString.replace('_', ' '), "Browser-Mosaic", "ncsa mosaic/", 2, true);
 
             // Netscape 1, 2, 3, 4
         } else if (!agentString.contains("(compatible") && TextUtils.matches(agentString, "mozilla\\/[1234]")) {
             analyze(ua, agentString, "Browser-Netscape", "mozilla/", 3, true);
         }
     }
 
     private static final void analyze(UserAgent ua, String agentString, String basicName, String versionPrefix) {
         analyze(ua, agentString, basicName, versionPrefix, 3, false);
     }
 
     private static final void analyze(UserAgent ua, String agentString, String basicName, String versionPrefix, int minVersionParts, boolean splitSecondVersionPart) {
 
         versionPrefix = versionPrefix.toLowerCase();
 
         // First add the basic name
         ua.addName(basicName);
 
         // Find the location of the version number after the prefix
         int index = agentString.indexOf(versionPrefix);
         if (index >= 0) {
 
             // Get the version number in a string
             String version = cutVersionEnd(agentString.substring(index + versionPrefix.length()).trim());
 
             if (version.length() > 0 && (!version.startsWith("00"))) {
 
                 // Split the version number in pieces
                 String[] versionParts = version.split("\\.");
 
                 // First version part can always be done immediately
                 String specificName = basicName + '-' + versionParts[0];
                 ua.addName(specificName);
 
                 int versionPartsFound;
                 if (splitSecondVersionPart && versionParts.length == 2) {
                     versionPartsFound = 1;
 
                     String secondVersionPart = versionParts[1];
                     for (int i = 0; i < secondVersionPart.length(); i++) {
                         specificName += "-" + secondVersionPart.charAt(i);
                         ua.addName(specificName);
                         versionPartsFound++;
                     }
                 } else {
                     for (int i = 1; i < versionParts.length; i++) {
                         if (TextUtils.matches(versionParts[i], "^0[0-9]")) {
                             specificName += '-' + "0";
                             ua.addName(specificName);
                             versionParts[i] = versionParts[i].substring(1);
                         }
 
                         specificName += '-' + versionParts[i];
                         ua.addName(specificName);
                     }
                     versionPartsFound = versionParts.length;
                 }
 
                 for (int i = versionPartsFound; i < minVersionParts; i++) {
                     specificName += "-0";
                     ua.addName(specificName);
                 }
             }
         }
     }
 
     private static final String cutVersionEnd(String s) {
         String result = "";
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (Character.isDigit(c) || c == '.') {
                 result += c;
             } else {
                 break;
             }
         }
 
         return result;
     }
 }
