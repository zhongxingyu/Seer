 /***************************************************************
  *  This file is part of the [fleXive](R) project.
  *
  *  Copyright (c) 1999-2007
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/copyleft/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
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
 package com.flexive.shared;
 
 import com.flexive.shared.exceptions.FxLookupException;
 import com.flexive.shared.interfaces.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.naming.NameClassPair;
 import javax.naming.NamingEnumeration;
 import javax.transaction.TransactionManager;
import javax.ejb.SessionContext;
 import java.util.Hashtable;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 
 /**
  * Utility class for EJB lookups.
  *
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class EJBLookup {
     private static transient Log LOG = LogFactory.getLog(EJBLookup.class);
     public static String APPNAME = "flexive";
 
     private enum STRATEGY {
         APP_SIMPLENAME_LOCAL,
         JAVA_COMP_ENV,
         APP_SIMPLENAME_REMOTE,
         COMPLEXNAME,
         SIMPLENAME_LOCAL,
         SIMPLENAME_REMOTE,
         UNKNOWN
     }
 
     private static STRATEGY used_strategy = null;
     private static final ConcurrentMap<String, Object> ejbCache = new ConcurrentHashMap<String, Object>(100);
 
     /**
      * Protected default constructor to avoid instantiation.
      */
     protected EJBLookup() {
     }
 
     /**
      * Lookup of the AccountEngine EJB.
      *
      * @return a reference to the AccountEngine EJB
      */
     public static AccountEngine getAccountEngine() {
         return getEngine(AccountEngine.class);
     }
 
     /**
      * Lookup of the SqlSearch EJB.
      *
      * @return a reference to the SqlSearch EJB
      */
     public static SearchEngine getSearchEngine() {
         return getEngine(SearchEngine.class);
     }
 
     /**
      * Lookup of the Briefcase EJB.
      *
      * @return a reference to the Briefcase EJB
      */
     public static BriefcaseEngine getBriefcaseEngine() {
         return getEngine(BriefcaseEngine.class);
     }
 
     /**
      * Lookup of the Template EJB.
      *
      * @return a reference to the Briefcase EJB
      */
     public static TemplateEngine getTemplateEngine() {
         return getEngine(TemplateEngine.class);
     }
 
 
     /**
      * Lookup of the GroupEngine EJB.
      *
      * @return a reference to the GroupEngine EJB
      */
     public static UserGroupEngine getUserGroupEngine() {
         return getEngine(UserGroupEngine.class);
     }
 
     /**
      * Lookup of the MandatorEngine EJB.
      *
      * @return a reference to the MandatorEngine EJB
      */
     public static MandatorEngine getMandatorEngine() {
         return getEngine(MandatorEngine.class);
     }
 
     /**
      * Lookup of the ACLEngine EJB.
      *
      * @return a reference to the ACLEngine EJB
      */
     public static ACLEngine getACLEngine() {
         return getEngine(ACLEngine.class);
     }
 
 
     /**
      * Lookup of the Language EJB.
      *
      * @return a reference to the Language EJB
      */
     public static LanguageEngine getLanguageEngine() {
         return getEngine(LanguageEngine.class);
     }
 
     /**
      * Lookup of the UserConfigurationEngine EJB.
      *
      * @return a reference to the UserConfigurationEngine EJB
      */
     public static UserConfigurationEngine getUserConfigurationEngine() {
         return getEngine(UserConfigurationEngine.class);
     }
 
     /**
      * Lookup of the DivisionConfigurationEngine EJB.
      *
      * @return a reference to the DivisionConfigurationEngine EJB
      */
     public static DivisionConfigurationEngine getDivisionConfigurationEngine() {
         return getEngine(DivisionConfigurationEngine.class);
     }
 
     /**
      * Lookup of the GlobalConfigurationBean EJB.
      *
      * @return a reference to the GlobalConfigurationBean EJB.
      */
     public static GlobalConfigurationEngine getGlobalConfigurationEngine() {
         return getEngine(GlobalConfigurationEngine.class);
     }
 
     /**
      * Lookup of the FxConfiguration EJB.
      *
      * @return a reference to the FxConfiguration EJB.
      */
     public static ConfigurationEngine getConfigurationEngine() {
         return getEngine(ConfigurationEngine.class);
     }
 
     /**
      * Lookup of the AssignmentEngine EJB.
      *
      * @return a reference to the AssignmentEngine EJB.
      */
     public static AssignmentEngine getAssignmentEngine() {
         return getEngine(AssignmentEngine.class);
     }
 
     /**
      * Lookup of the TypeEngine EJB.
      *
      * @return a reference to the TypeEngine EJB.
      */
     public static TypeEngine getTypeEngine() {
         return getEngine(TypeEngine.class);
     }
 
     /**
      * Lookup of the SelectListEngine EJB.
      *
      * @return a reference to the SelectListEngine EJB
      */
     public static SelectListEngine getSelectListEngine() {
         return getEngine(SelectListEngine.class);
     }
 
     /**
      * Lookup of the WorkflowEngine EJB.
      *
      * @return a reference to the WorkflowEngine EJB.
      */
     public static WorkflowEngine getWorkflowEngine() {
         return getEngine(WorkflowEngine.class);
     }
 
     /**
      * Lookup of the StepEngine EJB.
      *
      * @return a reference to the StepEngine EJB.
      */
     public static StepEngine getWorkflowStepEngine() {
         return getEngine(StepEngine.class);
     }
 
     /**
      * Lookup of the RouteEngine EJB.
      *
      * @return a reference to the RouteEngine EJB.
      */
     public static RouteEngine getWorkflowRouteEngine() {
         return getEngine(RouteEngine.class);
     }
 
     /**
      * Lookup of the StepDefinitionEngine EJB.
      *
      * @return a reference to the StepDefinitionEngine EJB.
      */
     public static StepDefinitionEngine getWorkflowStepDefinitionEngine() {
         return getEngine(StepDefinitionEngine.class);
     }
 
     /**
      * Lookup of the ContentEngine EJB.
      *
      * @return a reference to the ContentEngine EJB.
      */
     public static ContentEngine getContentEngine() {
         return getEngine(ContentEngine.class);
     }
 
     /**
      * Lookup of the StatelessTest EJB.
      *
      * @return a reference to the StatelessTest EJB.
      */
     public static StatelessTest getStatelessTestInterface() {
         return getEngine(StatelessTest.class);
     }
 
     /**
      * Lookup of the SequencerEngine EJB.
      *
      * @return a reference to the SequencerEngine EJB.
      */
     public static SequencerEngine getSequencerEngine() {
         return getEngine(SequencerEngine.class);
     }
 
     /**
      * Lookup of the ScriptingEngine EJB
      *
      * @return a reference to the ScriptingEngine EJB
      */
     public static ScriptingEngine getScriptingEngine() {
         return getEngine(ScriptingEngine.class);
     }
 
     /**
      * Lookup of the FxTimerService EJB
      *
      * @return a reference to the FxTimerService EJB
      */
     public static FxTimerService getTimerService() {
         return getEngine(FxTimerService.class);
     }
 
     /**
      * Lookup of the FxTree EJB
      *
      * @return a reference to the FxTree EJB
      */
     public static TreeEngine getTreeEngine() {
         return getEngine(TreeEngine.class);
     }
 
     /**
      * Lookup of the ResultPreferencesEngine EJB.
      *
      * @return a reference to the ResultPreferencesEngine EJB.
      */
     public static ResultPreferencesEngine getResultPreferencesEngine() {
         return getEngine(ResultPreferencesEngine.class);
     }
 
     /**
      * Get a reference of the transaction manager
      *
      * @return TransactionManager
      */
     public static TransactionManager getTransactionManager() {
         try {
             InitialContext ctx = new InitialContext();
             try {
                 return (TransactionManager) ctx.lookup("java:TransactionManager");
             } catch (NamingException e) {
                 try {
                     return (TransactionManager) ctx.lookup("java:comp/TransactionManager");
                 } catch (NamingException e2) {
                     return (TransactionManager) ctx.lookup("java:appserver/TransactionManager");
                 }
             }
         } catch (Exception e) {
             throw new FxLookupException("Failed to lookup transaction manager: " + e.getMessage(), e).asRuntimeException();
         }
    }
 
    /**
     * Get a reference of the current EJB session context.
     *
     * @return  the EJB session context
     */
    public static SessionContext getSessionContext() {
        try {
            final InitialContext ctx = new InitialContext();
            return (SessionContext) ctx.lookup("java:comp/EJBContext");
        } catch (NamingException e) {
            throw new FxLookupException("Failed to lookup session context: " + e.getMessage(), e).asRuntimeException();
        }
     }
 
     /**
      * Lookup the EJB found under the given Class's name. Uses default flexive naming scheme.
      *
      * @param type        EJB interface class instance
      * @param appName     EJB application name
      * @param environment optional environment for creating the initial context
      * @param <T>         EJB interface type
      * @return a reference to the given EJB
      */
     protected static <T> T getInterface(Class<T> type, String appName, Hashtable environment) {
         // Try to obtain interface from the lookup cache
         Object ointerface = ejbCache.get(type.getName());
         if (ointerface != null) {
             return type.cast(ointerface);
         }
 
         // Cache miss: obtain interface and store it in the cache
         synchronized (EJBLookup.class) {
             String name = "<unknown>";
             try {
                 if (environment == null)
                     environment = new Hashtable();
                 InitialContext ctx = new InitialContext(environment);
                 if (used_strategy == null) {
                     appName = discoverStrategy(appName, ctx, type);
                     if (used_strategy != null) {
                         LOG.info("Working lookup strategy: " + used_strategy);
                     } else {
                         LOG.error("No working lookup strategy found! Possibly because of pending redeployment.");
                     }
                 }
                 name = buildName(appName, type);
                 ointerface = ctx.lookup(name);
                 ejbCache.putIfAbsent(type.getName(), ointerface);
 
                 return type.cast(ointerface);
             } catch (Exception exc) {
                 throw new FxLookupException(LOG, exc, "ex.ejb.lookup.failure", type, exc).asRuntimeException();
             }
         }
     }
 
     /**
      * Discover which lookup strategy works for the given class
      *
      * @param appName EJB application name
      * @param ctx     InitialContext
      * @param type    the class
      * @return appName (may have changed)
      */
     private static <T> String discoverStrategy(String appName, InitialContext ctx, Class<T> type) {
         for (STRATEGY strat : STRATEGY.values()) {
             if (strat == STRATEGY.UNKNOWN)
                 continue;
             used_strategy = strat;
             try {
                 ctx.lookup(buildName(appName, type));
                 return appName;
             } catch (Exception e) {
                 //ignore and try next
             }
         }
         //houston, we have a problem - try locale and remote with appname again iterating through all "root" ctx bindings
         //this can happen if the ear is not named flexive.ear
         try {
             NamingEnumeration<NameClassPair> ncpe = ctx.list("");
             while( ncpe.hasMore() ) {
                 NameClassPair ncp = ncpe.next();
                 if( ncp.getClassName().endsWith("NamingContext") ) {
                     appName = ncp.getName();
                     try {
                         used_strategy = STRATEGY.APP_SIMPLENAME_LOCAL;
                         ctx.lookup(buildName(ncp.getName(), type));
                         APPNAME = ncp.getName();
                         LOG.info("Using application name ["+appName+"] for lookups!");
                         return APPNAME;
                     } catch( Exception e) {
                         //ignore and try remote
                     }
                     try {
                         used_strategy = STRATEGY.APP_SIMPLENAME_REMOTE;
                         ctx.lookup(buildName(ncp.getName(), type));
                         APPNAME = ncp.getName();
                         LOG.info("Using application name ["+appName+"] for lookups!");
                         return APPNAME;
                     } catch( Exception e) {
                         //ignore and try remote
                     }
                 }
             }
 
         } catch (Exception e) {
             LOG.warn(e);
         }
         used_strategy = null;
         return appName;
     }
 
     /**
      * Build the correct JNDI name to request for lookups depending on the discovered lookup strategy
      *
      * @param appName EJB application name
      * @param type    the class to lookup
      * @return JNDI name
      */
     private static <T> String buildName(String appName, Class<T> type) {
         switch (used_strategy) {
             case APP_SIMPLENAME_LOCAL:
                 return appName + "/" + type.getSimpleName() + "/local";
             case APP_SIMPLENAME_REMOTE:
                 return appName + "/" + type.getSimpleName() + "/remote";
             case COMPLEXNAME:
                 return type.getCanonicalName();
             case SIMPLENAME_LOCAL:
                 return type.getSimpleName() + "/local";
             case SIMPLENAME_REMOTE:
                 return type.getSimpleName() + "/remote";
             case JAVA_COMP_ENV:
                 return "java:comp/env/" + type.getSimpleName();
             default:
                 throw new FxLookupException("Unsupported/unknown lookup strategy " + used_strategy + "!").asRuntimeException();
         }
     }
 
     /**
      * Lookup the EJB found under the given Class's name. Uses default flexive naming scheme.
      *
      * @param appName EJB application name
      * @param type    EJB interface class instance
      * @param <T>     EJB interface type
      * @return a reference to the given EJB
      */
     public static <T> T getEngine(String appName, Class<T> type) {
         return getInterface(type, appName, null);
     }
 
     /**
      * Lookup the EJB found under the given Class's name. Uses default flexive naming scheme.
      *
      * @param type EJB interface class instance
      * @param <T>  EJB interface type
      * @return a reference to the given EJB
      */
     public static <T> T getEngine(Class<T> type) {
         return getInterface(type, APPNAME, null);
     }
 }
