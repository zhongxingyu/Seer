 package com.netspective.sparx.util;
 
 import javax.servlet.http.*;
 
 /** Browser.java
  *  @version 2.0.
  *  @author Garrett S Smith
  *  (web: <a href='http://dhtmlkitchen.com'>http://dhtmlkitchen.com</a>)
  *  You may use this for free.  You may add to or modify this code.
  *  Please do not remove this notice.
  * <p>
  *  To use the methods, get the session instance of the Browser object
  *  by calling the static method Browser.getInstance(request).
  *  <pre> Browser b = Browser.getInstance(request) </pre>
  *  The public constructor (previously deprecated) has been removed.
  * </p>
  */
public class Browser implements java.io.Serializable
 {
 
     private boolean moz = false, NS6 = false, NS7 = false, minNS61 = false, gecko = false;
     private boolean NS4 = false;
 
     private boolean IE = false;
     private boolean minIE5 = false, IE5 = false, IE55 = false, IE56 = false, IE6 = false;
     private boolean IE51 = false, IE51b = false, IE52 = false;
 
     private boolean mac = false, win = false;
     private boolean win95 = false, win98 = false, winME = false, winNT = false, win2k = false;
 
     private boolean unix = false, OS2 = false, sun = false, irix = false, hpux = false, aix = false,
     dec = false, SCO = false, VMS = false, linux = false, sinux = false, reliant = false,
     freeBSD = false, openBSD = false, netBSD = false, BSD = false, unixWare = false, NCR = false,
     X11 = false;
 
     private boolean w3cValidator = false;
     private boolean macIE5 = false, winIE5 = false;
     private boolean opera = false;
     private boolean opera5 = false;
     private boolean opera6 = false;
     private boolean opera7 = false;
     private boolean konqueror = false;
     private boolean konqueror2 = false;
     private boolean konqueror3 = false;
     private boolean safari = false;
 
     private boolean icab = false;
     private boolean firefly = false;
     private boolean galeon = false, webTV = false, AOL = false, omniweb = false;
 
     private boolean lynx = false;
 
     private boolean googlebot = false;
     private boolean ia_archiver = false;
 
     private boolean unknown = false;
     private String ua, origUa;
     private String IEVersion = "-1";
 
 
     /** Constructor that builds browser object.
      *
      *  It is necessary to pass an HttpServletRequest to this constructor.
      *  There is no default or empty constructor.
      *  @deprecated
      *  It is not necessary to create more than one Browser per session.
      *  @see #getInstance(javax.servlet.http.HttpServletRequest)
      *  getInstance(javax.servlet.http.HttpServletRequest).
      */
     private Browser(javax.servlet.http.HttpServletRequest request)
     {
         this.origUa = request.getHeader("User-Agent");
         if (origUa == null || origUa.length() == 0)
             return;
 
         this.ua = origUa.toLowerCase();
 
         // check platform first.
         this.mac = ua.indexOf("mac") >= 0;
         this.win = ua.indexOf("win") >= 0;
 
         if (win)
         { // whole bunch of windows browsers.
             this.win95 = (ua.indexOf("95") > 0);
             this.win98 = (ua.indexOf("98") > 0);
             this.winME = (ua.indexOf("win 9x 4.90") > 0);
             this.winNT = (ua.indexOf("nt") > 0);
             this.win2k = (ua.indexOf("nt 5") > 0);
         }
 
 
         this.webTV = ua.indexOf("webtv") > 0;
         if (this.webTV) return;
 
         this.unix = (!win && !mac && !webTV);
         if (unix)
         { // bunch of 'nix browsers.
             this.OS2 = ua.indexOf("os/2") > 0;
             this.sun = ua.indexOf("sunos") > 0;
             this.irix = ua.indexOf("irix") > 0;
             this.hpux = ua.indexOf("hpux") > 0
                     || ua.indexOf("hp-ux") > 0;
             this.aix = ua.indexOf("aix") > 0;
             this.dec = (ua.indexOf("dec") > 0
                     || ua.indexOf("alpha") > 0
                     || ua.indexOf("osf1") > 0
                     || ua.indexOf("ultrix") > 0);
             this.SCO = (ua.indexOf("sco") > 0 || ua.indexOf("unix_sv") > 0);
             this.VMS = (ua.indexOf("vax") > 0 || ua.indexOf("openvms") > 0);
             this.linux = ua.indexOf("linux") > 0;
             this.sinux = ua.indexOf("sinix") > 0;
             this.reliant = ua.indexOf("reliantunix") > 0;
             this.freeBSD = ua.indexOf("freebsd") > 0;
             this.openBSD = ua.indexOf("openbsd") > 0;
             this.netBSD = ua.indexOf("netbsd") > 0;
             this.BSD = ua.indexOf("bsd") > 0;
             this.unixWare = ua.indexOf("unix_system_v") > 0;
             this.NCR = ua.indexOf("ncr") > 0;
             this.X11 = ua.indexOf("x11") > 0;
         }
 
 
         this.opera = ua.indexOf("opera") >= 0;
         this.opera5 = ua.indexOf("opera 5") >= 0;
         this.opera6 = ua.indexOf("opera 6") >= 0;
         this.opera7 = ua.indexOf("opera 7") >= 0;
         // eliminate browsers that lie.
         if (this.opera) return;
 
 
         this.konqueror = ua.indexOf("konqueror") >= 0;
         this.konqueror2 = ua.indexOf("konqueror/2") >= 0;
         this.konqueror3 = ua.indexOf("konqueror/3") >= 0;
 
         // eliminate browsers that lie.
         if (this.konqueror) return;
 
         this.safari = ua.indexOf("safari") >= 0;
 
         // eliminate browsers that lie.
         if (this.safari) return;
 
         this.AOL = ua.indexOf("aol") > 0;
 
         this.omniweb = ua.indexOf("omniweb") > 0;
         if (this.omniweb) return;
 
         this.galeon = ua.indexOf("galeon") > 0;
 
         this.NS6 = ua.indexOf("netscape6") > 0;
         this.NS7 = ua.indexOf("netscape/7") > 0;
 
         this.minNS61 = ua.indexOf("netscape6/6.") > 0 || NS7;
         this.gecko = ua.indexOf("gecko") > 0;
         this.moz = (gecko && !NS6);
         this.firefly = ua.indexOf("firefly/") >= 0;
         if (firefly) return;
         if (moz) return;
         if (galeon) return;
 
         this.IE = ua.indexOf("msie") > 0;
         this.IEVersion = getLocalIEVersion();
 
         this.minIE5 = ua.indexOf("msie 5") > 0 || ua.indexOf("msie 6") > 0;
         this.IE5 = ua.indexOf("msie 5.0") > 0;
         this.IE55 = ua.indexOf("msie 5.5") > 0;
         this.IE56 = ua.indexOf("msie 5.6") > 0;
         this.IE6 = ua.indexOf("msie 6") > 0;
 
         this.winIE5 = (minIE5 && win);
 
         this.icab = ua.indexOf("icab") >= 0;
         this.IE51 = ua.indexOf("msie 5.1") >= 0;
         this.IE52 = ua.indexOf("msie 5.2") >= 0;
         this.IE51b = ua.indexOf("msie 5.1b") >= 0;
         this.macIE5 = (minIE5 && mac);
 
         this.lynx = ua.indexOf("lynx") >= 0;
 
         this.googlebot = ua.indexOf("googlebot") >= 0;
         this.ia_archiver = ua.indexOf("ia_archiver") >= 0;
 
         this.NS4 = (ua.indexOf("mozilla/4") == 0
                 && ua.indexOf("compatible") == -1 && !IE && !opera && !gecko && !icab && !konqueror && !firefly);
 
         this.w3cValidator = (ua.indexOf("w3c_validator") == 0);
         this.unknown = (!IE && !gecko && !NS4 && !AOL && !icab && !konqueror && !firefly);
 
     }
 
     private String getLocalIEVersion()
     {
         if (!this.IE) return "-1";
         int startNum = ua.indexOf("msie") + 4;
         if (ua.length() < startNum) return "-1";
 
         String IEVer = ua.substring(startNum).trim();
 
         try
         {
             IEVer = IEVer.substring(0, IEVer.indexOf(" "));
             if (IEVer.indexOf(";") > 0 && IEVer.indexOf(";") <= 4)
                 IEVer = IEVer.substring(0, IEVer.indexOf(";"));
 
             return (IEVer);
         }
         catch (Exception e)
         {
             this.unknown = true;
             return "-1";
         }
     }
 
     /** returns the Browser from the session.
      *  if the session does not have a Browser,
      *  a new Browser is created and stored in
      *  the session.
      */
     public static Browser getInstance(final javax.servlet.http.HttpServletRequest request)
     {
 
         HttpSession session = request.getSession(true);
         String sessionBrowserNS = "com.dhtmlkitchen.Browser";
 
         Browser browser = (Browser) session.getAttribute(sessionBrowserNS);
 
         if (browser == null)
         {
             browser = new Browser(request);
             session.setAttribute(sessionBrowserNS, browser);
         }
 
         return browser;
     }
 
     /** returns a string representation of the browser.
      *  The string returned by getBrowserName() is the browser name + the version.
      *  For IE, the platform preceeds the browser name and version.
      *  "unknown" is returned if the browser is not defined in this class.
      */
     public String getBrowserName()
     {
         return opera ? "Opera" :
                 omniweb ? "Omniweb" :
                 konqueror ? "Konqueror" :
                 icab ? "iCab" :
                 gecko ? NS6 ? minNS61 ? "Netscape 6.1 or higher" : "Netscape 6" : "Gecko" :
                 NS4 ? "Netscape 4" :
 
                 firefly ? "FireFly" :
                 win && IE ?
                 IE6 ? "IE6" :
                 IE56 ? "IE5.6" :
                 IE55 ? "IE5.5" :
                 winIE5 && IE5 ? "Win IE5.0" : "Win IE" + getIEVersion() :
 
                 mac && IE ?
                 macIE5 ? IE51b ? "Mac IE 5.1 beta" : IE51 ? "Mac IE 5.1" : "Mac IE 5.0" :
                 "Mac IE" + getIEVersion() :
                 "unknown";
     }
 
     /** returns a String representing the floating-point version of Internet Explorer.
      */
     public String getIEVersion()
     {
         return this.IEVersion;
     }
 
     /**
      *  returns true if the browser is Gecko (Gecko includes Mozilla, Galeon, and NS6).
      */
     public boolean isGecko()
     {
         return this.gecko;
     }
 
 
     /** returns true if the browser is Netscape 4 only.
      * returns false for all other versions of Netscape
      */
     public boolean isNS4()
     {
         return this.NS4;
     }
 
     /** returns true if the browser is Netscape 6.
      * also returns true if the browser is Netscape 6.x
      */
     public boolean isMinNS6()
     {
         return this.NS6 || this.NS7;
     }
 
     /** returns true only if the browser is over Netscape 6.1.
      */
     public boolean isMinNS61()
     {
         return this.minNS61;
     }
 
     public boolean isNS7()
     {
         return this.NS7;
     }
 
     /** returns true only if the browser is Gecko and <b>Not</b> Netscape 6.1.
      */
     public boolean isMoz()
     {
         return this.moz;
     }
 
     /** returns true if the browser is IE.
      */
     public boolean isIE()
     {
         return this.IE;
     }
 
     /** returns true if the browser is IE5.x or IE6.x.
      *  returns false for any version of IE that is not 5.0.
      */
     public boolean isMinIE5()
     {
         return this.minIE5;
     }
 
     /** returns true if the browser is IE5.0.
      *  returns false for any version of IE that is not 5.0.
      */
     public boolean isIE5()
     {
         return this.IE5;
     }
 
     /** returns true if the browser is IE5.5.
      *  returns false for any version of IE that is not 5.5.
      */
     public boolean isIE55()
     {
         return this.IE55;
     }
 
     /** returns true if the browser is IE5.6.
      *  returns false for any version of IE that is not 5.6.
      */
     public boolean isIE56()
     {
         return this.IE56;
     }
 
     /** returns true if the browser is IE6 or IE6.x.
      */
     public boolean isIE6()
     {
         return this.IE6;
     }
 
     /** returns true if the browser is a Microsoft Windows version.
      */
 
     /** returns true if the browser is Mac IE5.x.
      */
     public boolean isMacIE5()
     {
         return this.macIE5;
     }
 
 
     /* returns true only if the browser is Opera. */
     public boolean isOpera()
     {
         return this.opera;
     }
 
     /* returns true only if the browser is Opera. */
     public boolean isOpera5()
     {
         return this.opera5;
     }
 
     public boolean isOpera6()
     {
         return this.opera6;
     }
 
     public boolean isOpera7()
     {
         return this.opera7;
     }
 
     /** Mac only browser, supports document.getElementById,
      *  document.classes, document.tags, and innerHTML.
      */
     public boolean isIcab()
     {
         return this.icab;
     }
 
 
     /** From the developer:
      *  "FireFly is a new Gnutella servent.
      *  It supports segmented downloading
      *  (ie downloading same file from different sources),
      *  previewing of downloads, etc.".
      */
     public boolean isFirefly()
     {
         return this.firefly;
     }
 
     /** Linux browser with some DOM support. */
     public boolean isKonqueror()
     {
         return this.konqueror;
     }
 
     public boolean isKonqueror2()
     {
         return this.konqueror2;
     }
 
     public boolean isKonqueror3()
     {
         return this.konqueror3;
     }
 
 
     /** Mac-OS X browser that is similar to Konqueror.
      *  Safari, only available for Mac-OS X, supports HTML DOM.
      *  There are no versions of this browser.
      */
     public boolean isSafari()
     {
         return this.safari;
     }
 
     /** Another <b>Gecko&trade;</b> browser. */
     public boolean isGaleon()
     {
         return this.galeon;
     }
 
     /**@deprecated replaced with isWebTV() */
     public boolean isWebtv()
     {
         return this.webTV;
     }
 
     public boolean isWebTV()
     {
         return this.webTV;
     }
 
     /**@deprecated replaced with isAOL() */
     public boolean isAol()
     {
         return this.AOL;
     }
 
     public boolean isAOL()
     {
         return this.AOL;
     }
 
     /** A Mac OS X browser. The current version is 4
      * and has with poor standards support, but it supports
      * document.layers.
      */
     public boolean isOmniweb()
     {
         return this.omniweb;
     }
 
 
     public boolean isWin()
     {
         return this.win;
     }
 
     /** returns true if the browser is Windows IE5 or Higher
      *  (true for IE6, too).
      */
     public boolean isWinIE5()
     {
         return this.winIE5;
     }
 
 
     /** returns true if the browser is Mac IE5.1.
      */
     public boolean isIE51()
     {
         return this.IE51;
     }
 
     /** returns true if the browser is Mac IE5.2.
      */
     public boolean isIE52()
     {
         return this.IE52;
     }
 
     /** returns true if the browser is the  beta version of Mac IE5.1.
      */
     public boolean isIE51b()
     {
         return this.IE51b;
     }
 
     /** returns true if the browser is Mac.
      */
     public boolean isMac()
     {
         return this.mac;
     }
 
 
     /** returns true for all flavors of unix
      * (OS2, sun, irix, hpux, aix, dec, VMS, linux, sinix, reliant, bsd, unixware, mpras).
      */
     public boolean isUnix()
     {
         return this.unix;
     }
 
     public boolean isWin95()
     {
         return win ? this.win95 : false;
     }
 
     public boolean isWin98()
     {
         return win ? this.win98 : false;
     }
 
     /** @deprecated replaced with isWinME() */
     public boolean isWinme()
     {
         return win ? this.winME : false;
     }
 
     public boolean isWinME()
     {
         return win ? this.winME : false;
     }
 
     /** @deprecated replaced with iswinNT() */
     public boolean isWinnt()
     {
         return win ? this.winNT : false;
     }
 
     public boolean isWinNT()
     {
         return win ? this.winNT : false;
     }
 
     public boolean isWin2k()
     {
         return win ? this.win2k : false;
     }
 
     /** @deprecated replaced with isOS2() */
     public boolean isOs2()
     {
         return unix ? this.OS2 : false;
     }
 
     public boolean isOS2()
     {
         return unix ? this.OS2 : false;
     }
 
     public boolean isSun()
     {
         return unix ? this.sun : false;
     }
 
     public boolean isIrix()
     {
         return unix ? this.irix : false;
     }
 
     public boolean isHpux()
     {
         return unix ? this.hpux : false;
     }
 
     public boolean isAix()
     {
         return unix ? this.aix : false;
     }
 
     /** Returns true if the client machine is Unix Tru64. */
     public boolean isDec()
     {
         return unix ? this.dec : false;
     }
 
     /** @deprecated replaced by isSCO. */
     public boolean isSco()
     {
         return unix ? this.SCO : false;
     }
 
     /** Returns true if the client machine is a
      *  Santa Cruz Operation make.
      */
     public boolean isSCO()
     {
         return unix ? this.SCO : false;
     }
 
     /** @deprecated replaced by isVMS */
     public boolean isVms()
     {
         return unix ? this.VMS : false;
     }
 
     /** Returns true if the client machine is
      *  VMS (Vax Messaging System) or Open VMS (Open Vax Messaging System).
      */
     public boolean isVMS()
     {
         return unix ? this.VMS : false;
     }
 
     public boolean isLinux()
     {
         return unix ? this.linux : false;
     }
 
     public boolean isSinix()
     {
         return unix ? this.sinux : false;
     }
 
     public boolean isReliant()
     {
         return unix ? this.reliant : false;
     }
 
     public boolean isFreeBSD()
     {
         return unix ? this.freeBSD : false;
     }
 
     /** @deprecated replaced by isOpenBSD*/
     public boolean isOpenbsd()
     {
         return unix ? this.openBSD : false;
     }
 
     public boolean isOpenBSD()
     {
         return unix ? this.openBSD : false;
     }
 
     /** @deprecated replaced by isNetBSD*/
     public boolean isNetbsd()
     {
         return unix ? this.netBSD : false;
     }
 
     public boolean isNetBSD()
     {
         return unix ? this.netBSD : false;
     }
 
     /** @deprecated replaced by isBSD*/
     public boolean isBsd()
     {
         return unix ? this.BSD : false;
     }
 
     public boolean isBSD()
     {
         return unix ? this.BSD : false;
     }
 
     /** @deprecated replaced by isUnixWare */
     public boolean isUnixware()
     {
         return unix ? this.unixWare : false;
     }
 
     public boolean isUnixWare()
     {
         return unix ? this.unixWare : false;
     }
 
     /** @deprecated replaced by isNCR */
     public boolean isNcr()
     {
         return unix ? this.NCR : false;
     }
 
     public boolean isNCR()
     {
         return unix ? this.NCR : false;
     }
 
     /** Returns true if the browser is
      * X Window System, Version 11
      */
     public boolean isX11()
     {
         return unix ? this.X11 : false;
     }
 
     /** googlebot is the Google search engine robot. */
     public boolean isGooglebot()
     {
         return this.googlebot;
     }
 
     /** ia_archiver is the Alexa search engine robot. */
     public boolean isIa_archiver()
     {
         return this.ia_archiver;
     }
 
     public boolean isw3cValidator()
     {
         return this.w3cValidator;
     }
 
     /** returns true for browsers that are <em>not</em> : IE, Gecko, Netscape 4, Opera,
      * aol, Omniweb, FireFly, the w3cValidator.
      */
     public boolean isUnknown()
     {
         return this.unknown;
     }
 
     /** returns the full user agent string. */
     public String toString()
     {
         return this.origUa;
     }
 
 }
