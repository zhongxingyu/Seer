 /*
  Copyright (c) 2000-2005 University of Washington.  All rights reserved.
 
  Redistribution and use of this distribution in source and binary forms,
  with or without modification, are permitted provided that:
 
    The above copyright notice and this permission notice appear in
    all copies and supporting documentation;
 
    The name, identifiers, and trademarks of the University of Washington
    are not used in advertising or publicity without the express prior
    written permission of the University of Washington;
 
    Recipients acknowledge that this distribution is made available as a
    research courtesy, "as is", potentially with defects, without
    any obligation on the part of the University of Washington to
    provide support, services, or repair;
 
    THE UNIVERSITY OF WASHINGTON DISCLAIMS ALL WARRANTIES, EXPRESS OR
    IMPLIED, WITH REGARD TO THIS SOFTWARE, INCLUDING WITHOUT LIMITATION
    ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
    PARTICULAR PURPOSE, AND IN NO EVENT SHALL THE UNIVERSITY OF
    WASHINGTON BE LIABLE FOR ANY SPECIAL, INDIRECT OR CONSEQUENTIAL
    DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR
    PROFITS, WHETHER IN AN ACTION OF CONTRACT, TORT (INCLUDING
    NEGLIGENCE) OR STRICT LIABILITY, ARISING OUT OF OR IN CONNECTION WITH
    THE USE OR PERFORMANCE OF THIS SOFTWARE.
  */
 /* **********************************************************************
     Copyright 2005 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
 */
 package org.bedework.dumprestore.restore;
 
 import org.bedework.appcommon.configs.DumpRestoreConfig;
 import org.bedework.calenv.CalOptions;
 import org.bedework.calfacade.BwCalendar;
 import org.bedework.calfacade.BwSystem;
 import org.bedework.calfacade.BwUser;
 import org.bedework.calfacade.svc.BwAdminGroup;
 import org.bedework.calfacade.svc.BwPreferences;
 import org.bedework.calfacade.svc.BwSubscription;
 import org.bedework.calfacade.svc.BwView;
 import org.bedework.dumprestore.Defs;
 import org.bedework.dumprestore.restore.rules.RestoreRuleSet;
 
 import java.io.FileReader;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.digester.RegexMatcher;
 import org.apache.commons.digester.RegexRules;
 import org.apache.commons.digester.SimpleRegexMatcher;
 import org.apache.log4j.Logger;
 
 /** Application to restore from an XML calendar dump..
  *
  * @author Mike Douglass   douglm@rpi.edu
  * @version 3.1
  */
 public class Restore implements Defs {
   private transient Logger log;
 
   /* Properties can be supplied via the command line or via a the properties
    * file with the prefix propertyPrefix + appName + "."
    */
   private String appPrefix = "org.bedework.app.";
 
   private String appName;
 
   /* File we restore from */
   private String filename;
 
   //private boolean concatdesc = false;
 
   //private boolean printData = false;
 
   private RestoreGlobals globals = new RestoreGlobals();
 
   private Digester digester;
 
   /** ===================================================================
    *                       Constructor
    *  =================================================================== */
 
   public Restore() {
   }
 
   /** ===================================================================
    *                       Restore methods
    *  =================================================================== */
 
   /**
    * @throws Throwable
    */
   void open() throws Throwable {
     if (globals.rintf == null) {
 //      globals.rintf = new JdbcRestore();
       globals.rintf = new HibRestore(globals.config.getDebug());
       globals.rintf.init(globals);
       globals.rintf.open();
     }
 
     if (globals.config.getFrom2p3px()) {
       // System prefs are set up by run time pars
 
       globals.rintf.restoreSyspars(globals.syspars);
 
       // Ensure timezones get saved
       globals.getTzcache();
     }
   }
 
   void close() throws Throwable {
     if (globals.rintf != null) {
       globals.rintf.close();
     }
   }
 
   void doRestore() throws Throwable {
     digester = new Digester();
 
     RegexMatcher m = new SimpleRegexMatcher();
     digester.setRules(new RegexRules(m));
 
     digester.addRuleSet(new RestoreRuleSet(globals));
     digester.parse(new FileReader(filename));
 
     if (globals.config.getFrom2p3px()) {
       makePrefs();
       if (globals.rintf != null) {
         globals.rintf.restoreAdminGroup((BwAdminGroup)globals.getSuperGroup());
       }
     }
   }
 
   void stats() {
     globals.stats();
   }
 
   /** ===================================================================
    *                 Make prefs for 2.3.2
    *  =================================================================== */
 
   /** Called to finish off by writing out prefs.
    *
    * @throws Throwable
    */
   private void makePrefs() throws Throwable {
     Iterator it = globals.usersTbl.values().iterator();
     int nextId = 1;
 
     if (globals.publicUser == null) {
       throw new Exception("Public user must be defined");
     }
 
     while (it.hasNext()) {
       BwUser o = (BwUser)it.next();
       BwPreferences prefs = new BwPreferences();
 
       prefs.setId(nextId);
       nextId++;
       prefs.setOwner(o);
 
       // Create the default view
 
       BwView curView = new BwView();
 
       curView.setName(globals.syspars.getDefaultUserViewName()); // XXX
       curView.setOwner(o);
 
       prefs.addView(curView);
       prefs.setPreferredView(curView.getName());
 
       if (globals.publicUser.equals(o)) {
         /* Subscribe to all top level collections
          */
         BwCalendar root = globals.publicCalRoot;
 
         Iterator calit = root.iterateChildren();
         while (calit.hasNext()) {
           BwCalendar cal = (BwCalendar)calit.next();
           addSubscription(prefs, curView, o, cal, true, false, true);
         }
       } else {
         BwCalendar cal = (BwCalendar)globals.defaultCalendars.get(
             new Integer(o.getId()));
         prefs.setDefaultCalendar(cal);
 
         // Add default subscription for default calendar.
         addSubscription(prefs, curView, o, cal, true, true, false);
 
         // Add default subscription for trash calendar.
 
         cal = (BwCalendar)globals.trashCalendars.get(new Integer(o.getId()));
         addSubscription(prefs, null, o, cal, false, false, false);
 
         Collection s = globals.subscriptionsTbl.getCalendarids(o);
 
         if (s != null) {
           Iterator subit = s.iterator();
           globals.subscribedUsers++;
 
           while (subit.hasNext()) {
             /* Fixing up calendars for move from 2.3.2. Table value is the
              * id of a calendar
              * Make up a subscription.
              */
             cal = (BwCalendar)globals.filterToCal.get((Integer)subit.next());
             addSubscription(prefs, curView, o, cal, true, false, false);
           }
         }
       }
 
       if (globals.rintf != null) {
         globals.rintf.restoreUserPrefs(prefs);
       }
     }
   }
 
   /** Add a subscription to the current users entry.
    *
    * @param p
    * @param view
    * @param user     BwUser object
    * @param cal      BwCalendar object
    * @param display
    * @param freeBusy
    * @param publicCalendar
    * @throws Throwable
    */
   private void addSubscription(BwPreferences p, BwView view, BwUser user,
                                BwCalendar cal, boolean display,
                                boolean freeBusy,
                                boolean publicCalendar) throws Throwable {
     globals.subscriptions++;
     BwSubscription sub = BwSubscription.makeSubscription(cal, cal.getName(),
                                                          display, freeBusy, false);
     sub.setOwner(user);
     p.addSubscription(sub);
     if (view != null) {
       view.addSubscription(sub);
     }
 
     if (publicCalendar) {
       // Also add a single subscription
       BwView v = new BwView();
       v.setName(cal.getName());
       v.setOwner(user);
       v.addSubscription(sub);
       p.addView(v);
     }
   }
 
   boolean processArgs(String[] args) throws Throwable {
     if (args == null) {
       return true;
     }
 
     for (int i = 0; i < args.length; i++) {
       if (args[i].equals("-debug")) {
         globals.config.setDebug(true);
       } else if (args[i].equals("-ndebug")) {
         globals.config.setDebug(false);
       } else if (args[i].equals("")) {
         // null arg generated by ant
       } else if (args[i].equals("-noarg")) {
         // noop
       } else if (argpar("-appname", args, i)) {
         i++;
         // done earlier
       } else if (args[i].equals("-initSyspars")) {
         // done earlier
       } else if (args[i].equals("-skipspecialcals")) {
         globals.skipSpecialCals = true;
       } else if (args[i].equals("-fixcaltype")) {
         globals.fixCaltype = true;
       } else if (argpar("-f", args, i)) {
         i++;
         filename = args[i];
         /* Can we override these in the hibernate properties?
       } else if (argpar("-d", args, i)) {
         i++;
         driver = args[i];
       } else if (argpar("-i", args, i)) {
         i++;
         id = args[i];
       } else if (argpar("-p", args, i)) {
         i++;
         pw = args[i];
       } else if (argpar("-u", args, i)) {
         i++;
         url = args[i];
         */
 
       } else if (argpar("-onlyusers", args, i)) {
         i++;
         if (args[i].equals("*")) {
           // means ignore this par
         } else {
           globals.onlyUsersMap.setOnlyUsers(true);
           String[] users = args[i].split(",");
           for (int oui = 0; oui < users.length; oui++) {
             String ou = users[oui];
             info("Only user: " + ou);
 
             globals.onlyUsersMap.add(ou);
           }
         }
 
       } else {
         error("Illegal argument: '" + args[i] + "'");
         usage();
         return false;
       }
     }
 
     return true;
   }
 
   void usage() {
     System.out.println("Usage:");
     System.out.println("args   -appname name");
     System.out.println("       -f restorefilename");
     System.out.println("");
   }
 
   boolean argpar(String n, String[] args, int i) throws Exception {
     if (!args[i].equals(n)) {
       return false;
     }
 
     if ((i + 1) == args.length) {
       throw new Exception("Invalid args");
     }
     return true;
   }
 
   void getConfigProperties(String[] args) throws Throwable {
    /* Look for the appname and initSyspars arg */
    boolean initSyspars = false;
 
     if (args != null) {
       for (int i = 0; i < args.length; i++) {
         if (argpar("-appname", args, i)) {
           i++;
           appName = args[i];
         } else if (args[i].equals("-initSyspars")) {
          initSyspars = true;
         }
       }
     }
 
     if (appName == null) {
       error("Missing required argument -appname");
       throw new Exception("Invalid args");
     }
 
     globals.init((DumpRestoreConfig)CalOptions.getProperty(appPrefix + appName));
    if (initSyspars ||
        globals.config.getInitSyspars() ||
        globals.config.getFrom2p3px()) {
       globals.syspars = (BwSystem)CalOptions.getProperty("org.bedework.syspars");
     }
   }
 
   protected Logger getLog() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   protected void error(String msg) {
     getLog().error(msg);
   }
 
   protected void info(String msg) {
     getLog().info(msg);
   }
 
   protected void trace(String msg) {
     getLog().debug(msg);
   }
 
   /** Main
    *
    * @param args
    */
   public static void main(String[] args) {
     Restore r = null;
 
     try {
       long startTime = System.currentTimeMillis();
 
       r = new Restore();
 
       r.getConfigProperties(args);
 
       if (!r.processArgs(args)) {
         return;
       }
 
       r.open();
 
       r.doRestore();
 
       r.close();
 
       r.stats();
 
       long millis = System.currentTimeMillis() - startTime;
       long seconds = millis / 1000;
       long minutes = seconds / 60;
       seconds -= (minutes * 60);
 
       r.info("Elapsed time: " + minutes + ":" + twoDigits(seconds));
     } catch (Throwable t) {
       t.printStackTrace();
     } finally {
       try {
         r.close();
       } catch (Throwable t1) {
         t1.printStackTrace();
       }
     }
   }
 
   private static String twoDigits(long val) {
     if (val < 10) {
       return "0" + val;
     }
 
     return String.valueOf(val);
   }
 }
