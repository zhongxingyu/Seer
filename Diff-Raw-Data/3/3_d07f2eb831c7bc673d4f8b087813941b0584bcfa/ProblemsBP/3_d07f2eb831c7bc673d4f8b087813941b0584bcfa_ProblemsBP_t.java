 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.rcp.businessprocess;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Set;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jubula.client.core.Activator;
 import org.eclipse.jubula.client.core.businessprocess.TestExecution;
 import org.eclipse.jubula.client.core.businessprocess.compcheck.ProblemPropagator;
 import org.eclipse.jubula.client.core.businessprocess.db.TestJobBP;
 import org.eclipse.jubula.client.core.businessprocess.db.TestSuiteBP;
 import org.eclipse.jubula.client.core.businessprocess.problems.IProblem;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemFactory;
 import org.eclipse.jubula.client.core.businessprocess.problems.ProblemType;
 import org.eclipse.jubula.client.core.communication.ConnectionException;
 import org.eclipse.jubula.client.core.communication.AutAgentConnection;
 import org.eclipse.jubula.client.core.events.DataChangedEvent;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.DataState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ICompletenessCheckListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IDataChangedListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.IServerConnectionListener;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.ServerState;
 import org.eclipse.jubula.client.core.events.DataEventDispatcher.UpdateState;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO;
 import org.eclipse.jubula.client.core.model.IAUTMainPO;
 import org.eclipse.jubula.client.core.model.ICapPO;
 import org.eclipse.jubula.client.core.model.ICompNamesPairPO;
 import org.eclipse.jubula.client.core.model.IExecTestCasePO;
 import org.eclipse.jubula.client.core.model.INodePO;
 import org.eclipse.jubula.client.core.model.IPersistentObject;
 import org.eclipse.jubula.client.core.model.IProjectPO;
 import org.eclipse.jubula.client.core.model.IRefTestSuitePO;
 import org.eclipse.jubula.client.core.model.IReusedProjectPO;
 import org.eclipse.jubula.client.core.model.ITestCasePO;
 import org.eclipse.jubula.client.core.model.ITestJobPO;
 import org.eclipse.jubula.client.core.persistence.GeneralStorage;
 import org.eclipse.jubula.client.core.persistence.ProjectPM;
 import org.eclipse.jubula.client.core.utils.AbstractNonPostOperatingTreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeNodeOperation;
 import org.eclipse.jubula.client.core.utils.ITreeTraverserContext;
 import org.eclipse.jubula.client.core.utils.SpecTreeTraverser;
 import org.eclipse.jubula.client.core.utils.TreeTraverser;
 import org.eclipse.jubula.client.ui.constants.Constants;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.i18n.Messages;
 import org.eclipse.jubula.client.ui.rcp.utils.Utils;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.DebugConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.exception.JBException;
 import org.eclipse.jubula.tools.i18n.CompSystemI18n;
 import org.eclipse.jubula.tools.xml.businessmodell.InvalidAction;
 import org.eclipse.jubula.tools.xml.businessmodell.InvalidComponent;
 import org.eclipse.jubula.tools.xml.businessmodell.InvalidParam;
 import org.eclipse.jubula.tools.xml.businessmodell.Param;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * @author BREDEX GmbH
  * @created 12.03.2007
  */
 public class ProblemsBP implements ICompletenessCheckListener,
     IServerConnectionListener, IDataChangedListener {
     
     /** this instance */
     private static ProblemsBP instance; 
     
     /** the workspace root resource */
     private static final IWorkspaceRoot MARKER_ROOT = ResourcesPlugin
             .getWorkspace().getRoot();
     
     /** the logger */
     private static Logger log = LoggerFactory.getLogger(ProblemsBP.class);
 
     /** all comp names-related problem types */
     private final Set<Integer> m_compNameProblemTypes = 
         new HashSet<Integer> (Arrays.asList(new Integer [] {
             ProblemType.REASON_ACTION_DOES_NOT_EXIST.ordinal(),
             ProblemType.REASON_COMP_DOES_NOT_EXIST.ordinal(),
             ProblemType.REASON_DEPRECATED_ACTION.ordinal(),
             ProblemType.REASON_DEPRECATED_COMP.ordinal(),
             ProblemType.REASON_NO_COMPTYPE.ordinal(),
             ProblemType.REASON_PARAM_DOES_NOT_EXIST.ordinal(),
             ProblemType.REASON_PROJECT_DOES_NOT_EXIST.ordinal()
         }));
 
     /** all missing project related problem types */
     private final Set<Integer> m_missingProjectProblemTypes = 
         new HashSet<Integer> (Arrays.asList(new Integer [] {
             ProblemType.REASON_PROJECT_DOES_NOT_EXIST.ordinal(),
         }));
 
     /** a list with all problemMarkers */
     private List<IMarker> m_markerList = new ArrayList<IMarker>();
 
     /** a list with all problemMarkers */
     private List<IMarker> m_markerToShowList = new ArrayList<IMarker>();
 
     /** a list with all problems to show */
     private List<IProblem> m_allProblemsToShow = new ArrayList<IProblem>();
     
     /** a list with all local problems to show */
     private List<IProblem> m_localProblemsToShow = new ArrayList<IProblem>();
     
     /**
      * private constructor
      */
     private ProblemsBP() {
         // add business process to event dispatcher
         DataEventDispatcher ded = DataEventDispatcher.getInstance();
         ded.addAutAgentConnectionListener(this, true);
         ded.addCompletenessCheckListener(this);
         ded.addDataChangedListener(this, true);
         
         // trigger first problem check
         doProblemsCheck(true, null);
     }
     
     /**
      * @return the ProblemsBP instance
      */
     public static ProblemsBP getInstance() {
         if (instance == null) {
             instance = new ProblemsBP();
         }
         return instance;
     }
 
     /**
      * {@inheritDoc}
      */
     public void handleServerConnStateChanged(ServerState state) {
         doProblemsCheck(false, state);   
     }
 
     /**
      * @param node The node that is causing the problem.
      * @param label Name or GUID of the reused project. This string will be
      *              displayed in the problem description. If this string is
      *              <code>null</code>, the string displayed by the gui node
      *              will be used instead.
      */
     private void problemMissingReusedProject(IReusedProjectPO node, 
             String label) {
         String message = NLS.bind(Messages.ProblemCheckerProjectDoesNotExist,
                 label);
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, node, ProblemType.REASON_PROJECT_DOES_NOT_EXIST));
     }
     
     /**
      * Collect all problems
      * @param checkCompNamesPair
      *      boolean, also used to determine whether missing project problems
      *      will be reused or rechecked.
      * @param state 
      *      ServerState
      */
     private void doProblemsCheck(boolean checkCompNamesPair, 
         ServerState state) {
         clearOldProblems();
         
         // Is Project open ?
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project == null) {
             // Problem View
             problemNoProjectExists();
         } else {
             
             // checks if actual server and aut fit together
             if (TestSuiteBP.getListOfTestSuites().isEmpty()) {
                 problemNoTestSuiteExists();
             } 
             
             // checks if there is a Test Suite that lacks an AUT
             checkAllTestSuites();
             checkAllAutConfigs();
             
             checkAllTestJobs();
             
             if (project.getIsProtected()) {
                 problemProtectedProjectLoaded();
             }
         }
         if (checkCompNamesPair) {
             checkCompNamesPairs();
         } else {
             // Keep all problems related to Comp Names
             copyCompNamesProblems();
         }
 
         if (checkCompNamesPair) {
             checkMissingProjects();
         } else {
             // Keep all problems related to Missing Projects
             copyMissingProjectProblems();
         }
         
         checkReusedProjectLanguages();
         // check AutAgent Connection
         checkServerState(state);
 
         collectAdditionalProblemsWhichShouldBeMarked();
         
         createMarkers();
         
         // remove no needed items from ProblemView
         cleanupProblems();
         
         ProblemPropagator.getInstance().propagate();
     }
     
     /**
      * collect additional problems e.g. from completeness check itself
      */
     private void collectAdditionalProblemsWhichShouldBeMarked() {
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project == null) {
             return;
         }
         new TreeTraverser(project, 
             new CollectProblemsOperation(), false, true)
                 .traverse(true);
     }
     
     /**
      * check test jobs 
      */
     private void checkAllTestJobs() {
         for (ITestJobPO testJob : TestJobBP.getListOfTestJobs()) {
             List<INodePO> nodes = testJob.getUnmodifiableNodeList();
             for (INodePO node : nodes) {
                 if (node instanceof IRefTestSuitePO) {
                     IRefTestSuitePO refTS = (IRefTestSuitePO) node;
                     if (TestExecution.isAutNameSet(refTS.getTestSuiteAutID())) {
                         problemAUTNameNotSet(testJob, refTS);
                     }
                 }
             }
         }
     }
     
     /**
      * @param testJob the test job
      * @param refTS the ref test suite to create the problem for
      */
     private void problemAUTNameNotSet(ITestJobPO testJob, 
         IRefTestSuitePO refTS) {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                         Messages.TestDataDecoratorRefTsIncomplTooltip), NLS
                         .bind(Messages.TestDataDecoratorRefTsIncompl,
                                 testJob.getName()), refTS,
                 ProblemType.NO_QUICKFIX));
     }
     
     /**
      * remove all old problems
      */
     private void clearOldProblems() {
         for (IProblem problem : m_localProblemsToShow) {
             Object data = problem.getData();
             if (data instanceof INodePO) {
                 INodePO node = (INodePO) data;
                 node.removeProblem(problem);
             }
         }
         
         m_markerToShowList.clear();
         m_localProblemsToShow.clear();
         m_allProblemsToShow.clear();
     }
     
     /**
      * create marker for all problems
      */
     private void createMarkers() {
         m_allProblemsToShow.addAll(m_localProblemsToShow);
         for (IProblem problem : m_allProblemsToShow) {
             Object data = problem.getData();
             String location = data instanceof IPersistentObject
                             ? ((IPersistentObject) data).getName() 
                             : StringConstants.EMPTY;
             createMarker(problem.getUserMessage(),
                     getMarkerSeverity(problem), location, data,
                     problem.getProblemType());
         }
     }
     
     /**
      * Retains all problems dealing with Comp Names
      */
     private void copyCompNamesProblems() {
         for (IMarker marker : m_markerList) {
             
             if (isCompNameRelated(marker) 
                 && !m_markerToShowList.contains(marker)) {
                 
                 m_markerToShowList.add(marker);
             }
         }
     }
     
     /**
      * Retains all problems dealing with missing reused projects
      */
     private void copyMissingProjectProblems() {
         for (IMarker marker : m_markerList) {
             
             if (isMissingProjectRelated(marker) 
                 && !m_markerToShowList.contains(marker)) {
                 
                 m_markerToShowList.add(marker);
             }
         }
     }
 
     /**
      * 
      * @param marker The marker to check
      * @return <code>true</code> if the marker indicates a Comp Name problem
      */
     private boolean isCompNameRelated(IMarker marker) {
         try {
             return marker.getType().equals(Constants.JB_PROBLEM_MARKER) 
                 && m_compNameProblemTypes.contains(
                     marker.getAttribute(Constants.JB_REASON));
         } catch (CoreException ce) {
             log.error(Messages.CouldNotRetrieveTypeForMarker
                 + StringConstants.COLON
                 + StringConstants.SPACE
                 + marker
                 + StringConstants.DOT
                 + StringConstants.SPACE
                 + Messages.TheMarkerWillNotBeShown
                 + StringConstants.DOT, ce);
             return false;
         }
     }
     /**
      * 
      * @param marker The marker to check
      * @return <code>true</code> if the marker indicates a Comp Name problem
      */
     private boolean isMissingProjectRelated(IMarker marker) {
         try {
             return marker.getType().equals(Constants.JB_PROBLEM_MARKER) 
                 && m_missingProjectProblemTypes.contains(
                     marker.getAttribute(Constants.JB_REASON));
         } catch (CoreException ce) {
             log.error(Messages.CouldNotRetrieveTypeForMarker
                     + StringConstants.COLON
                     + StringConstants.SPACE
                     + marker
                     + StringConstants.DOT
                     + StringConstants.SPACE
                     + Messages.TheMarkerWillNotBeShown
                     + StringConstants.DOT, ce);
             return false;
         }
     }
     /**
      * checks if All TestSuites have runnable AUTs
      */
     private void checkAllTestSuites() {
         final IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project.getAutMainList().size() == 0) {
             problemNoAutForProjectExists();
         } else {
             checkAutConfigs();
         }
     }
     
     /**
      * 
      * @param problem The problem for which the severity is considered.
      * @return the marker severity (<code>SEVERITY_*</code> in {@link IMarker})
      *         corresponding to the given problem's severity.
      */
     private int getMarkerSeverity(IProblem problem) {
         int statusSeverity = problem.getStatus().getSeverity();
         int markerSeverity = IMarker.SEVERITY_INFO;
         if (statusSeverity == IStatus.WARNING) {
             markerSeverity = IMarker.SEVERITY_WARNING;
         } else if (statusSeverity == IStatus.ERROR
                 || statusSeverity == IStatus.CANCEL) {
             markerSeverity = IMarker.SEVERITY_ERROR;
         }
         
         return markerSeverity;
     }
     
     /**
      * If connected to a server, checks that each AUT has at least one config 
      * for the server.
      */
     private void checkAutConfigs() {
         InetAddress connectedServer = null;
         try {
             if (!AutAgentConnection.getInstance().isConnected()) {
                 return;
             }
             connectedServer = InetAddress.getByName(
                 AutAgentConnection.getInstance()
                     .getCommunicator().getHostName());
         } catch (UnknownHostException e) {
             // Host does not exist for connected server?!
             // This should NOT happen, do nothing
         } catch (ConnectionException ce) {
             // Not connected, don't check, just return
             return;
         }
         for (IAUTMainPO aut 
             : GeneralStorage.getInstance().getProject().getAutMainList()) {
             
             boolean isMatchingServer = false;
             InetAddress configServer = null;
             for (IAUTConfigPO config : aut.getAutConfigSet()) {
                 try {
                     configServer = InetAddress.getByName(config.getServer());
                     if ((configServer.equals(connectedServer)) 
                         || (configServer != null
                         && "127.0.0.1".equals(configServer.getHostAddress()) //$NON-NLS-1$
                         && connectedServer.getCanonicalHostName().equals(
                             InetAddress.getLocalHost()
                                 .getCanonicalHostName()))) {
                         
                         isMatchingServer = true;
                         break;
                     }
                 } catch (UnknownHostException e) {
                     // Host does not exist for config server
                     // The user has entered an invalid address
                     // do nothing
                 }
             }
             if (!isMatchingServer) {
                 problemNoAutConfigForServerExists(aut);
             }
         }
     }
     
     /**
      * checks if All AUTConfigs are correct
      */
     private void checkAllAutConfigs() {
         Set<IAUTMainPO> autList = GeneralStorage.getInstance()
             .getProject().getAutMainList();
         final String emtyString = StringConstants.EMPTY;
         for (IAUTMainPO mainPO : autList) {
             for (IAUTConfigPO config : mainPO.getAutConfigSet()) {
                 final String jarFile = config.getValue(
                         AutConfigConstants.JAR_FILE, null);
                 final String classPath = config.getValue(
                         AutConfigConstants.CLASSPATH, null);
                 if (emtyString.equals(jarFile)) { 
                     if (emtyString.equals(classPath)) {
                         problemNoJarOrClassPathForAutConfigExists(
                                 config, mainPO);
                     }
                 } else if (emtyString.equals(classPath) 
                     && emtyString.equals(jarFile)) {
 
                     problemNoJarOrClassPathForAutConfigExists(config, mainPO);
                 }
                 if (emtyString.equals(config.getServer())) {
                     
                     problemNoServerForAutConfigExists(config, mainPO);
                 }
             }
         }
     }
 
     
     /**
      * Checks if there is a Connection to AutAgent
      * @param state
      *      ServerState
      */
     private void checkServerState(ServerState state) {
         // Connection Check
         final String serverPortPref = 
                 Plugin.getDefault().getPreferenceStore().getString(
                         Constants.AUT_AGENT_SETTINGS_KEY);
         boolean isConnected = false;
         boolean isServerDefined = (serverPortPref.length() != 0);
         if (state == null) {
             try {
                 isConnected = AutAgentConnection.getInstance().isConnected();
             } catch (ConnectionException e) {
                 // ok
             }
         } else {
             switch (state) {
                 case Connected :
                     isConnected = true;
                     break;
                 case Disconnected :
                     isConnected = false;
                     break;
                 default:
                     break;
             }
             
         }
         if (!isServerDefined) {
             problemNoServerDefined();
 
         } else if (!isConnected) {
             problemNoAutStarterConnection();
         }
     }
     
     /**
      * Shows a message in the problems view.
      * 
      * @param message The message in the problems view.
      * @param messageType IMarker.SEVERITY_ERROR, IMarker.SEVERITY_INFO or IMarker.SEVERITY_WARNING
      * @param location The location of the problem, task, ....
      * @param object The object reference.
      * @param type The type of this problem
      */
     private void createMarker(String message, int messageType,
         String location, Object object, ProblemType type) {
         boolean existProblem = false;
         for (IMarker marker : m_markerList) {
             try {
                 if (marker.getAttribute(IMarker.LOCATION).equals(location)
                     && marker.getAttribute(IMarker.SEVERITY).equals(messageType)
                     && marker.getAttribute(IMarker.MESSAGE).equals(message)
                     && ((object != null && new Integer(object.hashCode())
                         .equals(
                         marker.getAttribute(Constants.JB_OBJECT_HASHCODE))) 
                         || (object == null && marker.getAttribute(
                             Constants.JB_OBJECT_HASHCODE) == null))) {
                     
                     existProblem = true;
                     m_markerToShowList.add(marker);
                 }
             } catch (CoreException e) {
                 // ok
             }
         }
         if (existProblem) {
             return;
         }
         try {
             IMarker marker = MARKER_ROOT
                 .createMarker(Constants.JB_PROBLEM_MARKER);
             marker.setAttribute(IMarker.LOCATION, location);
             marker.setAttribute(IMarker.SEVERITY, messageType);
             marker.setAttribute(IMarker.MESSAGE, message);
             // set specific attributes
             if (object != null) {
                 marker.setAttribute(Constants.JB_OBJECT_HASHCODE, 
                         object.hashCode());
             } else {
                 marker.setAttribute(Constants.JB_OBJECT_HASHCODE, 
                         null);
             }
             marker.setAttribute(Constants.JB_REASON, type.ordinal());
             if (object instanceof INodePO) {
                 INodePO node = (INodePO) object;
                 marker.setAttribute(Constants.JB_OBJECT_NAME, node.getName());
                 marker.setAttribute(Constants.JB_NODE_GUID, node.getGuid());
             } else {
                 if (object instanceof String) {
                     marker.setAttribute(Constants.JB_OBJECT_NAME,
                             object);
                 } else {
                     marker.setAttribute(Constants.JB_OBJECT_NAME,
                             StringConstants.EMPTY);
                 }
                 marker.setAttribute(Constants.JB_NODE_GUID,
                         StringConstants.EMPTY);
             }
             m_markerList.add(marker);
             m_markerToShowList.add(marker);
         } catch (CoreException e) {
             log.error(DebugConstants.ERROR, e);
         }
 
     }
 
     /**
      * @return all Markers from FrameWork
      */
     private IMarker[] findProblems() {
         String type = Constants.JB_PROBLEM_MARKER;
         IMarker[] markers = null;
         try {
             markers = MARKER_ROOT.findMarkers(type, true,
                 IResource.DEPTH_INFINITE);
         } catch (CoreException e) {
             // ok
         }
         return markers;
     }
 
     /**
      * Deletes all problems in the problems view.
      */
     private void cleanupProblems() {
         boolean doLoop = true;
         while (doLoop) {
             doLoop = false;
             for (IMarker marker : m_markerList) {
                 if (!m_markerToShowList.contains(marker)) {
                     try {
                         m_markerList.remove(marker);
                         marker.delete();
                     } catch (CoreException e) {
                         // ok
                     }
                     doLoop = true;
                     break;
                 }
             }
         }
         for (IMarker marker : findProblems()) {
             if (!m_markerList.contains(marker)
                 || !m_markerToShowList.contains(marker)) {
                 try {
                     m_markerList.remove(marker);
                     m_markerToShowList.remove(marker);
                     marker.delete();
                 } catch (CoreException e) {
                     // ok
                 }
             }
         }
     }
 
     /**
      * Shows the status of the server connection in GDProblemView.
      */
     private void problemNoAutStarterConnection() {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.INFO, Activator.PLUGIN_ID,
                         Messages.ProblemCheckerConnectToGDServer),
                 Messages.ProblemCheckerConnectToGDServer, Messages.Connection,
                 ProblemType.REASON_CONNECTED_TO_NO_SERVER));
     }
     
     /**
      * Shows the status of the project protection in GDProblemView.
      */
     private void problemProtectedProjectLoaded() {
         m_localProblemsToShow.add(ProblemFactory
                 .createProblemWithMarker(new Status(IStatus.INFO,
                         Activator.PLUGIN_ID,
                         Messages.ProblemCheckerProtectedProject),
                         Messages.ProblemCheckerProtectedProject,
                         Messages.ProtectedProject,
                         ProblemType.REASON_PROTECTED_PROJECT));
     }
     
     /**
      * Called when no server in Workspace.
      */
     private void problemNoServerDefined() {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(
                     IStatus.WARNING, Activator.PLUGIN_ID,
                     Messages.ProblemCheckerNoServer),
                     Messages.ProblemCheckerNoServer,
                     Messages.NoServer, 
                     ProblemType.REASON_NO_SERVER_DEFINED));
     }
 
     /**
      * Shows the existance of a project in GDProblemView.
      */
     private void problemNoProjectExists() {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(
                     IStatus.INFO, Activator.PLUGIN_ID,
                     Messages.GDStateControllerInfoNoProject),
                     Messages.GDStateControllerInfoNoProject,
                     Messages.Project, 
                     ProblemType.REASON_NO_PROJECT));
     }
 
     /**
      * Shows the existance of a project in GDProblemView.
      */
     private void problemNoTestSuiteExists() {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(
                     IStatus.INFO, Activator.PLUGIN_ID,
                     Messages.ProblemCheckerNoTestSuite),
                     Messages.ProblemCheckerNoTestSuite,
                     Messages.Project, 
                     ProblemType.REASON_NO_TESTSUITE));
     }
 
     /**
      * called when a project lacks an Aut
      */
     private void problemNoAutForProjectExists() {
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(
                     IStatus.WARNING, Activator.PLUGIN_ID,
                     Messages.ProblemCheckerNoAutExists),
                     Messages.ProblemCheckerNoAutExists,
                     Messages.Project, 
                     ProblemType.REASON_NO_AUT_FOR_PROJECT_EXISTS));
     }
 
     /**
      * called when an Aut lacks an AutConfig
      * @param config AutConfig where problem occurs
      * @param aut corresponding aut
      */
     private void problemNoJarOrClassPathForAutConfigExists(IAUTConfigPO config, 
             IAUTMainPO aut) {
         String message = NLS.bind(Messages.ProblemCheckerAutConfigMissesJar,
                 new String[] { config.getName(), aut.getName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, Messages.ProblemCheckerAUT + aut.getName(),
                 ProblemType.REASON_NO_JAR_FOR_AUTCONFIG));
     }
 
     /**
      * called when an Aut lacks an AutAgent
      * @param config AutConfig where problem occurs
      * @param aut corresponding aut
      */
     private void problemNoServerForAutConfigExists(IAUTConfigPO config, 
             IAUTMainPO aut) {
         String message = NLS.bind(Messages.ProblemCheckerAutConfigMissesJar,
                 new String[] { config.getName(), aut.getName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, Messages.ProblemCheckerAUT + aut.getName(),
                 ProblemType.REASON_NO_SERVER_FOR_AUTCONFIG));
     }
 
     /**
      * called when an Aut has no AutConfig
      * @param aut AUT where problem occurs
      */
     private void problemNoAutConfigForServerExists(IAUTMainPO aut) {
         String message = NLS.bind(
                 Messages.ProblemCheckerAutNoConfigurationForServer,
                 aut.getName());
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, Messages.ProblemCheckerAUT + aut.getName(),
                 ProblemType.REASON_NO_AUTCONFIG_FOR_SERVER_EXIST));
     }
 
     /**
      * Checks, if an execTC has compNames without compTypes
      */
     @SuppressWarnings("synthetic-access")
     private void checkCompNamesPairs() {
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project == null) {
             return;
         }
         final ITreeNodeOperation<INodePO> op = new CheckProblemsOperation();
         TreeTraverser traverser = new TreeTraverser(project, op, false, true);
         CheckForDeprecatedModulesOperation operation = 
                 new CheckForDeprecatedModulesOperation();
         traverser.addOperation(operation);
         traverser.traverse();
         TreeTraverser specTreeTraverser = new SpecTreeTraverser(project, op);
         specTreeTraverser.addOperation(operation);
         specTreeTraverser.traverse();
     }
     
     /**
      * Checks, if any reused projects no longer exist in the DB
      */
     private void checkMissingProjects() {
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project == null) {
             return;
         }
         
         for (IReusedProjectPO reused : project.getUsedProjects()) {
             boolean reusedProjectExists = false;
             try {
                reusedProjectExists = 
                        (ProjectPM.loadProjectFromMaster(reused) != null);
             } catch (JBException e) {
                 // Error while loading project; Project does not exist
                 // Do nothing
             }
             if (!reusedProjectExists) {
                 problemMissingReusedProject(null, reused.getName());
             }
         }
 
     }
 
     /**
      * Checks, if reused projects are missing languages used in this project
      */
     private void checkReusedProjectLanguages() {
         IProjectPO project = GeneralStorage.getInstance().getProject();
         if (project == null) {
             return;
         }
 
         List<Locale> langList = project.getLangHelper().getLanguageList();
         
         for (IReusedProjectPO reused : project.getUsedProjects()) {
             IProjectPO reusedProject = null;
             try {
                 reusedProject = ProjectPM.loadProjectFromMaster(reused);
                 if (reusedProject != null) { // not in DB
                     List<Locale> reusedLangList = reusedProject.getLangHelper()
                         .getLanguageList();
 
                     for (Locale lang : langList) {
                         if (!reusedLangList.contains(lang)) {
                             if (WorkingLanguageBP.getInstance()
                                 .getWorkingLanguage().equals(lang)) {
 
                                 // Report as error because it may mean that test
                                 // data is missing.
                                 problemReusedProjectMissingLang(lang, reused
                                     .getName(), IStatus.ERROR);
                             } else {
                                 problemReusedProjectMissingLang(lang, reused
                                     .getName(), IStatus.WARNING);
                             }
                         }
                     }
                 }
             } catch (JBException e) {
                 // Error while loading project; Project does not exist
                 // Do nothing
             }
         }
 
     }
 
     /**
      * Creates a problem to indicate that a language is not supported by a 
      * reused project.
      * 
      * @param lang The language that is not suported.
      * @param name The name of the reused project.
      * @param severity The severity of the problem.
      */
     private void problemReusedProjectMissingLang(
         Locale lang, String name, int severity) {
         String message = NLS.bind(
                 Messages.ProblemCheckerReusedProjectMissingLanguage,
                 new String[] { name, lang.getDisplayName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(severity, Activator.PLUGIN_ID, message),
                 message, StringConstants.EMPTY,
                 ProblemType.REASON_REUSED_PROJECT_MISSING_LANG));
     }
 
     /**
      * @param cap the corresponding cap
      */
     private void problemDeprecatedActionFound(ICapPO cap) {
         final ITestCasePO tcPO = (ITestCasePO) cap.getParentNode();
         String message = NLS.bind(Messages.ProblemCheckerDeprecatedAction,
                 new String[] { cap.getName(), tcPO.getName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, cap,
                 ProblemType.REASON_DEPRECATED_ACTION));
     }
     
     /**
      * @param cap the corresponding cap
      */
     private void problemDeprecatedCompFound(ICapPO cap) {
         final ITestCasePO tcPO = (ITestCasePO)cap.getParentNode();
         String message = NLS.bind(Messages.ProblemCheckerDeprecatedAction,
                 new String[] { cap.getName(), tcPO.getName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.WARNING, Activator.PLUGIN_ID, message),
                 message, cap,
                 ProblemType.REASON_DEPRECATED_COMP));
     }
     
     /**
      * @param cap the corresponding cap
      */
     private void problemCompDoesNotExist(ICapPO cap) {
         final ITestCasePO tcPO = (ITestCasePO)cap.getParentNode();
         
         String message = NLS.bind(Messages.ProblemCheckerCompDoesNotExist,
                         new String[] { cap.getName(), tcPO.getName(),
                                 CompSystemI18n.getString(
                                         cap.getComponentType()) });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID, message),
                 message, cap, ProblemType.REASON_COMP_DOES_NOT_EXIST));
     }
 
     /**
      * @param cap the corresponding cap
      */
     private void problemActionDoesNotExist(ICapPO cap) {
         final ITestCasePO tcPO = (ITestCasePO)cap.getParentNode();
         
         String message = NLS.bind(
                 Messages.ProblemCheckerCompDoesNotExist,
                 new String[] { cap.getName(), tcPO.getName(),
                         cap.getComponentName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID, message),
                 message, cap, ProblemType.REASON_COMP_DOES_NOT_EXIST));
     }
 
     /**
      * @param cap the corresponding cap
      */
     private void problemParamDoesNotExist(ICapPO cap) {
         final ITestCasePO tcPO = (ITestCasePO) cap.getParentNode();
 
         String message = NLS.bind(
                 Messages.ProblemCheckerCompDoesNotExist,
                 new String[] { cap.getName(), tcPO.getName(),
                         cap.getComponentName() });
         m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                 new Status(IStatus.ERROR, Activator.PLUGIN_ID, message),
                 message, cap, ProblemType.REASON_COMP_DOES_NOT_EXIST));
     }
 
     /**
      * @author BREDEX GmbH
      */
     private final class CollectProblemsOperation 
         extends AbstractNonPostOperatingTreeNodeOperation<INodePO> {
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx,
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (ProblemFactory.hasProblem(node)) {
                 for (IProblem problem : node.getProblems()) {
                     if (problem.hasUserMessage()) {
                         m_allProblemsToShow.add(problem);
                     }
                 }
             }
             return true;
         }
     }
     
     /**
      * refactored anonymous class to nested
      *
      * @author BREDEX GmbH
      * @created 02.03.2007
      */
     @SuppressWarnings("synthetic-access")
     private final class CheckForDeprecatedModulesOperation 
         extends AbstractNonPostOperatingTreeNodeOperation<INodePO> {
 
         /** {@inheritDoc} */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             if (node instanceof ICapPO) {
                 final ICapPO capPO = (ICapPO)node;
                 if (capPO.getMetaAction().isDeprecated()) {
                     problemDeprecatedActionFound(capPO);
                 }
                 if (capPO.getMetaComponentType().isDeprecated()) {
                     problemDeprecatedCompFound(capPO);
                 }
             }
             return true;
         }
     }
     
     /**
      * @author BREDEX GmbH
      * @created 12.02.2007
      */
     @SuppressWarnings("synthetic-access")
     private final class CheckProblemsOperation 
         extends AbstractNonPostOperatingTreeNodeOperation<INodePO> {
         
         /**
          * {@inheritDoc}
          */
         public boolean operate(ITreeTraverserContext<INodePO> ctx, 
                 INodePO parent, INodePO node, boolean alreadyVisited) {
             
             if (node instanceof IExecTestCasePO) {
                 IExecTestCasePO execTC = (IExecTestCasePO)node;
                 boolean hasCompleteTestCaseReferences =
                         !execTC.getProblems().contains(ProblemFactory
                                 .createMissingReferencedSpecTestCasesProblem());
                 if (hasCompleteTestCaseReferences) {
                     // Only check CompNamesPair if test case reference
                     // is available.
 
                     for (ICompNamesPairPO pair : execTC.getCompNamesPairs()) {
                         String type = Utils.getComponentType(execTC, pair
                             .getFirstName());
                         pair.setType(type);
                         if (type == null 
                                 || StringConstants.EMPTY.equals(type)) {
                             problemNoCompTypeForCompNamesPairExists(execTC);
                         }
                     }
                 }
             } else if (node instanceof ICapPO) {
                 ICapPO cap = (ICapPO)node;
                 if (cap.getMetaComponentType() instanceof InvalidComponent) {
                     String message = Messages.Component + StringConstants.COLON
                         + StringConstants.SPACE + cap.getComponentType();
                     log.error(Messages.CouldNotFind
                             + StringConstants.SPACE + message);
                     problemCompDoesNotExist(cap);
                 } else if (cap.getMetaAction() instanceof InvalidAction) {
                     String message = Messages.CouldNotFindAction
                         + StringConstants.SPACE
                         + CompSystemI18n.getString(cap.getActionName())
                         + StringConstants.NEWLINE + "in" + StringConstants.SPACE //$NON-NLS-1$
                         + Messages.Component + StringConstants.COLON
                         + StringConstants.SPACE + cap.getComponentType();
                     log.error(message);
                     problemActionDoesNotExist(cap);
                 }
                 for (Object paramObj : cap.getParameterList()) {
                     if (paramObj instanceof InvalidParam) {
                         Param param = (Param)paramObj;
                         String message = Messages.Component
                             + StringConstants.COLON
                             + StringConstants.SPACE 
                             + cap.getComponentType()
                             + StringConstants.NEWLINE
                             + Messages.Action
                             + StringConstants.COLON
                             + StringConstants.SPACE
                             + CompSystemI18n.getString(cap.getActionName())
                             + StringConstants.NEWLINE
                             + Messages.Parameter
                             + StringConstants.COLON
                             + StringConstants.SPACE
                             + CompSystemI18n.getString(param.getName());
                         log.error(Messages.CouldNotFind + StringConstants.SPACE
                                + message);
                         problemParamDoesNotExist(cap);
                     }
                 }
             }
             return true;
         }
         
         /**
          * @param execTC the execTC that has problems
          */
         private void problemNoCompTypeForCompNamesPairExists(
             IExecTestCasePO execTC) {
             if (execTC.getParentNode() == null) {
                 // in case of EventExecTestCase
                 return;
             }
             String name = execTC.getParentNode().getName();
             if (StringConstants.EMPTY.equals(name)
                 && execTC.getParentNode() instanceof IExecTestCasePO) {
 
                 name = ((IExecTestCasePO)execTC.getParentNode())
                     .getSpecTestCase().getName();
             } 
             m_localProblemsToShow.add(ProblemFactory.createProblemWithMarker(
                     new Status(IStatus.WARNING, Activator.PLUGIN_ID, 
                         NLS.bind(Messages.ProblemCheckerNoCompType, name)),
                         NLS.bind(Messages.ProblemCheckerNoCompType, name), 
                     execTC.getParentNode(), ProblemType.REASON_NO_COMPTYPE));
         }
     }
     
     /** {@inheritDoc} */
     public void completenessCheckFinished() {
         doProblemsCheck(true, null);
     }
     
     /** {@inheritDoc} */
     public void handleDataChanged(DataChangedEvent... events) {
         boolean shouldRun = false;
         for (DataChangedEvent dce : events) {
             if (dce.getUpdateState() != UpdateState.onlyInEditor) {
                 DataState ds = dce.getDataState();
                 if (ds == DataState.Added 
                         || ds == DataState.Deleted
                         || ds == DataState.StructureModified) {
                     shouldRun = true;
                     break;
                 }
             }
         }
 
         if (shouldRun) {
             doProblemsCheck(true, null);
         }
     }
 }
