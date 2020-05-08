 /*******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Robert Fuhrer (rfuhrer@watson.ibm.com) - initial API and implementation
 
 *******************************************************************************/
 
 package org.eclipse.imp.releng;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.imp.releng.actions.VersionScanner;
 import org.eclipse.imp.releng.metadata.FeatureInfo;
 import org.eclipse.imp.releng.metadata.FileVersionMap;
 import org.eclipse.imp.releng.metadata.PluginInfo;
 import org.eclipse.imp.releng.metadata.UpdateSiteInfo;
 import org.eclipse.imp.releng.metadata.PluginInfo.FileAdded;
 import org.eclipse.imp.releng.metadata.PluginInfo.FileChange;
 import org.eclipse.imp.releng.metadata.PluginInfo.FileDeleted;
 import org.eclipse.imp.releng.metadata.PluginInfo.NewPluginChange;
 import org.eclipse.imp.releng.metadata.UpdateSiteInfo.FeatureRef;
 import org.eclipse.imp.releng.utils.Pair;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.ltk.core.refactoring.Change;
 import org.eclipse.ltk.core.refactoring.CompositeChange;
 import org.eclipse.ltk.core.refactoring.PerformChangeOperation;
 import org.eclipse.ltk.core.refactoring.RefactoringCore;
 import org.eclipse.ltk.core.refactoring.TextFileChange;
 import org.eclipse.team.core.ProjectSetCapability;
 import org.eclipse.team.core.ProjectSetSerializationContext;
 import org.eclipse.team.core.RepositoryProvider;
 import org.eclipse.team.core.RepositoryProviderType;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.core.history.IFileHistory;
 import org.eclipse.team.core.history.IFileHistoryProvider;
 import org.eclipse.team.core.history.IFileRevision;
 import org.eclipse.team.internal.ccvs.core.CVSException;
 import org.eclipse.team.internal.ccvs.core.CVSProviderPlugin;
 import org.eclipse.team.internal.ccvs.core.CVSTag;
 import org.eclipse.team.internal.ccvs.core.CVSTeamProvider;
 import org.eclipse.team.internal.ccvs.core.CVSWorkspaceSubscriber;
 import org.eclipse.team.internal.ccvs.core.client.Command;
 import org.eclipse.team.internal.ccvs.core.client.Session;
 import org.eclipse.team.internal.ccvs.core.client.Command.LocalOption;
 import org.eclipse.team.internal.ccvs.core.resources.CVSWorkspaceRoot;
 import org.eclipse.text.edits.MultiTextEdit;
 import org.eclipse.text.edits.ReplaceEdit;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.eclipse.ui.progress.IProgressService;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public abstract class ReleaseTool {
     protected IWorkspaceRoot fWSRoot= ResourcesPlugin.getWorkspace().getRoot();
 
     /**
      * The list of features that are to be processed. Can be a subset of the
      * features contained in the workspace.
      */
     protected final List<FeatureInfo> fFeatureInfos= new ArrayList<FeatureInfo>();
 
     /**
      * The list of all plugins contained in the features that are to be processed.
      */
     protected final List<PluginInfo> fPluginInfos= new ArrayList<PluginInfo>();
 
     /**
      * The set of plugins that are excluded from having their versions incremented
      */
     protected final Set<String> fExclusions= new  HashSet<String>();
 
     protected final List<UpdateSiteInfo> fUpdateSiteInfos= new ArrayList<UpdateSiteInfo>();
 
     /**
      * If true, all checks have succeeded, and incrementing can be safely performed.
      */
     protected boolean fCanProceed;
 
     public List<FeatureInfo> getFeatureInfos() {
         return fFeatureInfos;
     }
 
     public void saveFeatureProjectSets(List<FeatureInfo> selectedFeatures) {
         for(FeatureInfo fi: selectedFeatures) {
             Map<String/*repoTypeID*/,Set<String/*repoRef*/>> repoRefMap= new HashMap<String,Set<String>>();
 
             for(PluginInfo pi: fi.fPluginInfos) {
                 IProject pluginProject= fWSRoot.getProject(pi.fPluginID);
 
                 if (pluginProject == null || !pluginProject.exists()) {
                     ReleaseEngineeringPlugin.getMsgStream().println("Unable to open project for plugin ID " + pi.fPluginID + "; ignoring.");
                     continue;
                 }
 
                 Pair<String/*repoTypeID*/,String/*repoRef*/> repoDesc= getRepoRefForProject(pluginProject);
 
                 addMapEntry(repoDesc.first, repoDesc.second, repoRefMap);
             }
 
             IProject featureProject= fi.fProject;
             writeProjectSet(repoRefMap, featureProject, "feature.psf");
         }
     }
 
     public void writeProjectSet(Map<String, Set<String>> repoRefMap, IProject hostProject, String fileName) {
         String psfContents= projectSetFor(repoRefMap);
         IFile psf= hostProject.getFile(fileName);
         InputStream is= new ByteArrayInputStream(psfContents.getBytes());
 
         try {
             if (!psf.exists()) {
                 psf.create(is, true, new NullProgressMonitor());
             } else {
                 psf.setContents(is, 0, new NullProgressMonitor());
             }
         } catch (CoreException e) {
             postError("Unable to write team project set file for " + hostProject.getName() + ": " + e.getMessage(), e);
         }
     }
 
     protected void postError(String errorMsg, Exception e) {
         System.err.println(errorMsg);
     }
 
     public Pair<String /*repoTypeID*/,String/*repoRef*/> getRepoRefForProject(IProject project) {
         RepositoryProvider repoProvider= RepositoryProvider.getProvider(project);
         String repoTypeID= repoProvider.getID();
         RepositoryProviderType repoType= RepositoryProviderType.getProviderType(repoTypeID);
         ProjectSetCapability projectSetCapability= repoType.getProjectSetCapability();
         ProjectSetSerializationContext context= new ProjectSetSerializationContext();
 
         try {
             String repoRef= projectSetCapability.asReference(new IProject[] { project }, context, new NullProgressMonitor())[0];
 
             return new Pair<String, String>(repoTypeID, repoRef);
         } catch (TeamException e) {
             ReleaseEngineeringPlugin.getMsgStream().println("Unable to compute repository reference for project " + project.getName());
         }
         return null;
     }
 
     public static <K,V> void addMapEntry(K key, V value, Map<K, Set<V>> map) {
         Set<V> values= map.get(key);
         if (values == null) {
             map.put(key, values= new HashSet<V>());
         }
         values.add(value);
     }
 
     protected String projectSetFor(Map<String, Set<String>> repoRefMap) {
         StringBuilder sb= new StringBuilder();
         sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); nl(sb);
         sb.append("<psf version=\"2.0\">"); nl(sb);
         for(String repoTypeID: repoRefMap.keySet()) {
             Set<String> repoRefs= repoRefMap.get(repoTypeID);
 
             sb.append("    <provider id=\"" + repoTypeID + "\">"); nl(sb);
             for(String repoRef: repoRefs) {
                 sb.append("        <project reference=\"" + repoRef + "\"/>"); nl(sb);
             }
             sb.append("    </provider>"); nl(sb);
         }
         sb.append("</psf>"); nl(sb);
         return sb.toString();
     }
 
     protected void nl(StringBuilder sb) {
         sb.append("\n");
     }
 
     public void incrementVersions() {
 	fCanProceed= true; // Assume ok until error encountered
 
         collectMetaData(false);
 
         if (fFeatureInfos.size() == 0)
             return;
 
         collectVersionInfo();
 
         if (!fCanProceed) {
             ReleaseEngineeringPlugin.getMsgStream().println("*** Errors encountered; will not rewrite files.");
             return;
         }
 
         computeNewVersions();
 
         if (confirmChanges()) {
             performAllChanges();
         }
     }
 
     public void collectMetaData(boolean allFeatures) {
     	fUpdateSiteInfos.clear();
     	fFeatureInfos.clear();
         fPluginInfos.clear();
         fExclusions.clear();
         fCanProceed= true; // assume the best until proven otherwise
 
         collectFeatures(allFeatures);
         collectUpdateSites();
 
         if (fFeatureInfos.size() == 0)
             return;
 
         collectPluginsFromFeatures();
         collectExclusions();
     }
 
     private void collectVersionInfo() {
 	readOldPluginVersionMaps();
         buildNewPluginVersionMaps();
         scanPluginsForDiffs();
     }
 
     private void computeNewVersions() {
 	incrementPluginInfoVersions();
         incrementFeatureVersions();
         fixupFeatureRequires();
     }
 
     private void performAllChanges() {
 	List<IFile> changedFiles= new ArrayList<IFile>();
         List<Change> changes= new ArrayList<Change>();
 
         writeNewPluginVersionMaps(changedFiles);
         rewriteFeatureManifests(changes, changedFiles);
         rewritePluginManifests(changes, changedFiles);
 
         doPerformChange(changes, "Version edits", changedFiles);
 //      setVCTags();
     }
 
     private void setVCTags() {
         for(PluginInfo pi: fPluginInfos) {
             IProject project= fWSRoot.getProject(pi.fPluginID);
             if (!RepositoryProvider.isShared(project)) {
                 // Ask the user whether s/he wants to share the project to a repository...
                 if (confirmShareProject(project)) {
                     // There doesn't seem to be published code to do this.
                     // org.eclipse.team.internal.ui.actions.ConfigureProjectAction
                     // uses ConfigureProjectWizard, but neither of them are visible.
                 }
             }
             // Don't know that this is possible in a provider-independent way.
             // See for example org.eclipse.team.internal.ccvs.ui.actions.TagLocalAction,
             // which implements the CVS version of this operation.
 //          RepositoryProvider repoProvider= RepositoryProvider.getProvider(project);
         }
     }
 
     protected abstract boolean confirmShareProject(IProject project);
 
     private boolean confirmChanges() {
         Comparator<PluginInfo> pluginInfoComparator= new Comparator<PluginInfo>() {
             public int compare(PluginInfo arg0, PluginInfo arg1) {
         	return arg0.fPluginID.compareTo(arg1.fPluginID);
             }
         };
 	final Set<PluginInfo> changedPlugins= new TreeSet<PluginInfo>(pluginInfoComparator);
 
 	for(PluginInfo pi: fPluginInfos) {
             if (pi.getChangeState().isChange()) {
         	changedPlugins.add(pi);
             }
         }
 
 	Set<PluginInfo> unchangedPlugins= new TreeSet<PluginInfo>(pluginInfoComparator);
 
 	unchangedPlugins.addAll(fPluginInfos);
         unchangedPlugins.removeAll(changedPlugins);
         return doConfirm(changedPlugins, unchangedPlugins);
     }
 
     protected abstract boolean doConfirm(Set<PluginInfo> changedPlugins, Set<PluginInfo> unchangedPlugins);
 
     /**
      * @param projects
      */
     private void collectFeatures(boolean allFeatures) {
         IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
         Set<IProject> allFeatureProjects= collectAllFeatureProjects(wsRoot);
         Set<IProject> selectedProjects= allFeatures ? allFeatureProjects : selectFeatureProjects(allFeatureProjects);
 
         for(IProject project: selectedProjects) {
             try {
                 IFile manifestFile= project.getFile("feature.xml");
 
                 fFeatureInfos.add(new FeatureInfo(project, manifestFile));
             } catch (ParserConfigurationException e) {
         	ReleaseEngineeringPlugin.logError(e);
             } catch (SAXException e) {
         	ReleaseEngineeringPlugin.logError(e);
             } catch (IOException e) {
         	ReleaseEngineeringPlugin.logError(e);
             }
         }
     }
 
     protected Set<IProject> selectFeatureProjects(final Set<IProject> allFeatureProjects) {
 	return allFeatureProjects;
     }
 
     protected List<FeatureInfo> selectFeatureInfos() {
         return fFeatureInfos;
     }
 
     protected Set<IProject> collectAllFeatureProjects(IWorkspaceRoot wsRoot) {
 	Set<IProject> result= new TreeSet<IProject>(new Comparator<IProject>() {
 	    public int compare(IProject arg0, IProject arg1) {
 		return arg0.getName().compareTo(arg1.getName());
 	    }
 	});
         final IProject[] allProjects= wsRoot.getProjects();
 
         for(int i= 0; i < allProjects.length; i++) {
             final IProject proj= allProjects[i];
 
             if (proj.exists(new Path("feature.xml"))) {
         	result.add(proj);
             }
         }
         return result;
     }
 
     private void collectPluginsFromFeatures() {
         for(FeatureInfo fi: fFeatureInfos) {
             fi.collectPlugins(this);
         }
         for(PluginInfo pi: fPluginInfos) {
             fCanProceed= fCanProceed && pi.pluginOk();
         }
     }
 
     private void collectUpdateSites() {
         IWorkspaceRoot wsRoot= ResourcesPlugin.getWorkspace().getRoot();
 
         for(IProject project: wsRoot.getProjects()) {
             try {
                 IFile manifestFile= project.getFile("site.xml");
 
                 if (manifestFile.exists()) {
                     fUpdateSiteInfos.add(new UpdateSiteInfo(project, manifestFile));
                 }
             } catch (ParserConfigurationException e) {
                 ReleaseEngineeringPlugin.logError(e);
             } catch (SAXException e) {
                 ReleaseEngineeringPlugin.logError(e);
             } catch (IOException e) {
                 ReleaseEngineeringPlugin.logError(e);
             }
         }
     }
 
     /**
      * Collects the set of plugins that are exempt from having their version number incremented.
      */
     private void collectExclusions() {
         fExclusions.add("com.ibm.wala.shrike");
         fExclusions.add("polyglot");
         fExclusions.add("lpg.runtime");
     }
 
     /**
      * Increment all feature versions, along with all changed, non-excluded plugins
      * that they contain.<br>
      * Purely an in-memory metadata operation; does not modify any workspace files.
      */
     private void incrementFeatureVersions() {
         ReleaseEngineeringPlugin.getMsgStream().println("***");
         for(FeatureInfo fi: fFeatureInfos) {
             Document manifestDoc= fi.fManifestDoc;
             String featureID= fi.fFeatureID;
             String oldFeatureVersion= fi.fFeatureVersion;
             String newFeatureVersion= incrementVersionRelease(oldFeatureVersion);
 
             fi.setNewVersion(newFeatureVersion);
 
             ReleaseEngineeringPlugin.getMsgStream().println("Feature " + featureID + " new version #: " + newFeatureVersion);
 
             for(Node plugin: fi.fPlugins) {
                 String pluginID= getPluginIDNode(plugin);
                 Node pluginVersionNode= getPluginVersionNode(plugin);
                 String pluginVersion= pluginVersionNode.getNodeValue();
                 PluginInfo pi= findPluginInfo(pluginID);
                 String newPluginVersion= pi.fPluginNewVersion;
 
                 if (fExclusions.contains(pluginID)) {
                     ReleaseEngineeringPlugin.getMsgStream().println("   Plugin " + pluginID + " excluded; using same version #: " + pluginVersion);
                     pi.fPluginNewVersion= newPluginVersion= pluginVersion;
                 } else {
                     ReleaseEngineeringPlugin.getMsgStream().println("   Plugin " + pluginID + " new version #: " + newPluginVersion);
                 }
                 pluginVersionNode.setNodeValue(newPluginVersion);
             }
         }
     }
 
     // TODO Perhaps the next few methods belong on PluginInfo
     private Node getPluginVersionNode(Node plugin) {
         return plugin.getAttributes().getNamedItem("version");
     }
 
     private String getPluginIDNode(Node plugin) {
         return plugin.getAttributes().getNamedItem("id").getNodeValue();
     }
 
     public void addPlugin(PluginInfo pluginInfo) {
         fPluginInfos.add(pluginInfo);
     }
 
     public List<PluginInfo> getPluginInfos() {
         return fPluginInfos;
     }
 
     /**
      * @return the PluginInfo for the plugin with the given plugin ID
      */
     protected PluginInfo findPluginInfo(String pluginID) {
         for(PluginInfo pi: fPluginInfos) {
             if (pi.fPluginID.equals(pluginID)) {
                 return pi;
             }
         }
         return null;
     }
 
     /**
      * Increments the version numbers of the PluginInfos for all known plugins
      * that have changed since the previous release.
      */
     private void incrementPluginInfoVersions() {
         for(PluginInfo pi: fPluginInfos) {
             if (pi.getChangeState().isChange()) {
                 pi.fPluginNewVersion= incrementVersionRelease(pi.fPluginVersion);
                 if (pi.fNewMap != null) {
                     pi.fNewMap.setVersion(pi.fManifest, incrementFileVersion(pi.fNewMap.getVersion(pi.fManifest)));
                 }
             } else {
                 pi.fPluginNewVersion= pi.fPluginVersion;
             }
         }
     }
 
     /**
      * @return the given file version, with its last component incremented by 1
      */
     protected String incrementFileVersion(String version) {
         StringTokenizer st= new StringTokenizer(version, ".");
         List<Integer> versionComps= new ArrayList<Integer>();
         while (st.hasMoreTokens()) {
             versionComps.add(Integer.parseInt(st.nextToken()));
         }
         int last= versionComps.size()-1;
         versionComps.set(last, versionComps.get(last) + 1);
         StringBuilder sb= new StringBuilder();
         for(Integer comp: versionComps) {
             sb.append('.');
             sb.append(comp);
         }
         return sb.toString().substring(1);
     }
 
     /**
      * @return the given version number, with its release component incremented by 1
      */
     protected String incrementVersionRelease(String version) {
         StringTokenizer st= new StringTokenizer(version, ".");
         return st.nextToken() + "." + st.nextToken() + "." + (Integer.parseInt(st.nextToken()) + 1);
     }
 
     private void fixupFeatureRequires() {
         for(FeatureInfo fi: fFeatureInfos) {
             Document manifestDoc= fi.fManifestDoc;
             Node featureNode= manifestDoc.getChildNodes().item(0);
             String featureVersion= featureNode.getAttributes().getNamedItem("version").getNodeValue();
 
             ReleaseEngineeringPlugin.getMsgStream().println("Processing feature " + featureNode.getAttributes().getNamedItem("id") + " version " + featureVersion + " from " + fi.fManifestFile.getLocation().toPortableString());
 
             Set<Node> featureRequires= getChildNodesNamed("requires", featureNode);
 
             for(Node required: featureRequires) {
                 Set<Node> imports= getChildNodesNamed("import", required);
                 for(Node imprt: imports) {
                     if (imprt.getAttributes().getNamedItem("feature") != null) {
                         String requiredFeatureID= imprt.getAttributes().getNamedItem("feature").getNodeValue();
                         Node requiredFeature= findFeature(requiredFeatureID);
 
                         if (!fExclusions.contains(requiredFeatureID) && requiredFeature != null) {
                             String requiredFeatureVersion= requiredFeature.getChildNodes().item(0).getAttributes().getNamedItem("version").getNodeValue();
 
                             imprt.getAttributes().getNamedItem("version").setNodeValue(requiredFeatureVersion);
                         }
                     }
                 }
             }
         }
     }
 
     public FeatureInfo findFeatureInfo(String featureID) {
         for(FeatureInfo fi: fFeatureInfos) {
             if (fi.fFeatureID.equals(featureID))
                 return fi;
         }
         return null;
     }
 
     /**
      * @return the XML document Node corresponding to the feature with the given featureID,
      * if known, otherwise null
      */
     protected Node findFeature(String featureID) {
         FeatureInfo fi= findFeatureInfo(featureID);
         if (fi != null) {
             return fi.fManifestDoc;
         }
         return null;
     }
 
     /**
      * Reads the existing file version map, if any, for all of the plugins
      * listed in <code>fPluginInfos</code>.
      */
     private void readOldPluginVersionMaps() {
         for(PluginInfo pi: fPluginInfos) {
             IProject project= fWSRoot.getProject(pi.fPluginID);
             FileVersionMap fvMap= new FileVersionMap(project, pi.fPluginID, pi.fPluginVersion);
 
             if (fvMap.isEmpty()) {
                 ReleaseEngineeringPlugin.getMsgStream().println("No existing version map file for " + project.getName() + "; presumably a previously-unreleased project?");
             }
             pi.fCurMap= fvMap;
         }
     }
 
     private Comparator<IFile> fFileComparator= new Comparator<IFile>() {
         public int compare(IFile o1, IFile o2) {
             return o1.getLocation().toPortableString().compareTo(o2.getLocation().toPortableString());
         }
     };
 
     /**
      * Builds the file version maps for the new plugin versions, based on the
      * source repository version information gleaned from the current workspace
      * contents. Assumes that the workspace is clean wrt the repository.
      */
     private void buildNewPluginVersionMaps() {
 	VersionScanner scanner= new VersionScanner();
         Set<IFile> dirtyFiles= new TreeSet<IFile>(fFileComparator);
 
 	for(PluginInfo pi: fPluginInfos) {
             String pluginID= pi.fPluginID;
             IProject pluginProject= fWSRoot.getProject(pluginID);
 
             if (pluginProject == null)
                 continue;
 
             final FileVersionMap fileVersionMap= new FileVersionMap(pluginProject, pi.fPluginID, incrementVersionRelease(pi.fPluginVersion));
             RepositoryProvider repoProvider= RepositoryProvider.getProvider(pluginProject);
 
             if (repoProvider == null) {
                 continue;
             }
 
             final IFileHistoryProvider fileHistProvider= repoProvider.getFileHistoryProvider();
 
             if (fileHistProvider == null) {
                 continue;
             }
 
             List<IPath> buildItems= readBuildProperties(pluginProject);
 
             // TODO Need to check ALL files that are included in the plugin's distribution (icons, plugin.xml, etc.)
 
             dirtyFiles.addAll(scanner.scanSrcFolders(pluginProject, fileHistProvider, fileVersionMap));
             dirtyFiles.addAll(scanner.checkFile("plugin.xml", pluginProject, fileHistProvider, fileVersionMap));
             dirtyFiles.addAll(scanner.scanFolder("icons", pluginProject, fileHistProvider, fileVersionMap));
             dirtyFiles.addAll(scanner.scanFolder("templates", pluginProject, fileHistProvider, fileVersionMap));
             for(IPath path : buildItems) {
 		if (pluginProject.getFolder(path).exists()) {
                     dirtyFiles.addAll(scanner.scanFolder(path.toPortableString(), pluginProject, fileHistProvider, fileVersionMap));
 		} else if (pluginProject.getFile(path).exists()) {
                     dirtyFiles.addAll(scanner.checkFile(path.toPortableString(), pluginProject, fileHistProvider, fileVersionMap));
 		}
 	    }
             pi.fNewMap= fileVersionMap;
         }
         fCanProceed= fCanProceed && confirmDirtyFiles(dirtyFiles);
     }
 
     protected boolean confirmDirtyFiles(Set<IFile> dirtyFiles) {
         if (dirtyFiles.isEmpty()) {
             return true;
         }
         MessageConsoleStream msgStream= ReleaseEngineeringPlugin.getMsgStream();
 
         msgStream.println("*** Dirty files: ***");
         for(IFile file: dirtyFiles) {
             msgStream.println("  " + file.getLocation().toPortableString());
         }
         return false;
     }
 
     /**
      * Reads the build.properties file for the given plugin project, and returns
      * the list of IPaths specified in the "bin.includes" directive.
      */
     private List<IPath> readBuildProperties(IProject pluginProject) {
         List<IPath> buildItems= new ArrayList<IPath>();
         try {
             // read the build.properties file and retrieve the elements in "bin.includes"
             String buildProperties= getFileContents(new InputStreamReader(pluginProject.getFile("build.properties").getContents()));
             List<String> stanzas= coalesceStanzas(buildProperties.split("\n"));
 
             for(String stanza : stanzas) {
 		if (stanza.startsWith("bin.includes")) {
 		    String[] paths= stanza.substring(stanza.indexOf('=')+1).trim().split(",");
 		    for(String path : paths) {
 			if (!path.equals(".") && !path.equals("bin/")) {
 			    buildItems.add(new Path(path));
 			}
 		    }
 		}
 	    }
         } catch (CoreException e) {
             ReleaseEngineeringPlugin.logError(e);
         }
         return buildItems;
     }
 
     private List<String> coalesceStanzas(String[] stanzas) {
 	List<String> result= new ArrayList<String>();
 	for(int i= 0; i < stanzas.length; i++) {
 	    String stanza= stanzas[i].trim();
 	    if (stanza.endsWith(",\\") || stanza.endsWith(",\\")) {
 		stanzas[i+1]= stanza.replaceAll(",\\\\", ",") + stanzas[i+1].trim();
 	    } else {
 		result.add(stanza);
 	    }
 	}
 	return result;
     }
 
     private void scanPluginsForDiffs() {
         for(PluginInfo pi: fPluginInfos) {
             FileVersionMap oldMap= pi.fCurMap;
             FileVersionMap newMap= pi.fNewMap;
 
             if (fExclusions.contains(pi.fPluginID)) {
                 continue;
             }
 
             if (oldMap == null || oldMap.getAllFiles().isEmpty()) {
                 ReleaseEngineeringPlugin.getMsgStream().println("Plugin " + pi.fPluginID + " presumably previously unreleased; will build fresh version map.");
                 pi.updateReason(NewPluginChange.getInstance());
                 continue;
             }
 
             Set<IFile> oldFiles= oldMap.getAllFilesSorted();
 
             for(IFile f: oldFiles) {
         	if (newMap.getVersion(f) == null) {
                     ReleaseEngineeringPlugin.getMsgStream().println("Plugin " + pi.fPluginID + " changed due to changed/deleted file " + f.getFullPath().toPortableString());
                     pi.updateReason(new FileDeleted(f.getFullPath()));
         	} else if (!oldMap.getVersion(f).equals(newMap.getVersion(f))) {
                     ReleaseEngineeringPlugin.getMsgStream().println("Plugin " + pi.fPluginID + " changed due to changed/deleted file " + f.getFullPath().toPortableString());
                     pi.updateReason(new FileChange(f.getFullPath()));
                 }
             }
             for(IFile newF: newMap.getAllFilesSorted()) {
                 if (!oldFiles.contains(newF)) {
                     ReleaseEngineeringPlugin.getMsgStream().println("Plugin " + pi.fPluginID + " changed due to new file " + newF.getFullPath().toPortableString());
                     pi.updateReason(new FileAdded(newF.getFullPath()));
                     break;
                 }
             }
             if (!pi.getChangeState().isChange()) {
                 ReleaseEngineeringPlugin.getMsgStream().println("Plugin " + pi.fPluginID + " unchanged.");
             }
         }
     }
 
     private void writeNewPluginVersionMaps(List<IFile> changedFiles) {
         for(PluginInfo pi: fPluginInfos) {
             if (!pi.getChangeState().isChange())
                 continue;
 
             String pluginID= pi.fPluginID;
             IProject pluginProject= fWSRoot.getProject(pluginID);
 
             if (pluginProject == null)
                 continue;
 
             FileVersionMap newMap= pi.fNewMap;
 
             if (newMap == null)
                 continue;
 
             changedFiles.add(newMap.writeMap());
         }
     }
 
     protected void rewriteFeatureManifest(FeatureInfo featureInfo) {
         List<IFile> changedFiles= new ArrayList<IFile>();
         List<Change> changes= new ArrayList<Change>();
 
         try {
             rewriteFeatureManifest(featureInfo, changes, changedFiles);
             doPerformChange(changes, "Feature manifest edits", changedFiles);
         } catch (IOException e) {
             ReleaseEngineeringPlugin.logError(e);
         }
     }
 
     private void rewriteFeatureManifest(FeatureInfo fi, List<Change> changes, List<IFile> changedFiles) throws IOException {
         ReleaseEngineeringPlugin.getMsgStream().println("Rewriting feature manifest " + fi.fManifestFile.getLocation().toPortableString());
 
         try {
 	        Document manifestDoc= fi.fManifestDoc;
 	        DOMSource source= new DOMSource(manifestDoc);
 	        StringWriter strWriter= new StringWriter();
 	        StreamResult result= new StreamResult(strWriter);
 	        TransformerFactory tf= TransformerFactory.newInstance();
 	        Transformer serializer= tf.newTransformer();
 
 	        serializer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
 	        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
 	        serializer.transform(source, result);
 
 	        TextFileChange tfc= new TextFileChange("Version increment for " + fi.fFeatureID, fi.fManifestFile);
 	        long curFileLength= new File(fi.fManifestFile.getLocation().toOSString()).length();
 
 	        tfc.setEdit(new MultiTextEdit());
 	        tfc.addEdit(new ReplaceEdit(0, (int) curFileLength, strWriter.toString()));
 	
 	        changedFiles.add(fi.fManifestFile);
 	        changes.add(tfc);
         } catch (TransformerConfigurationException e) {
         	ReleaseEngineeringPlugin.logError(e);
         } catch (TransformerException e) {
         	ReleaseEngineeringPlugin.logError(e);
 		}
     }
 
     protected void rewriteFeatureManifests(List<Change> changes, List<IFile> changedFiles) {
         for(FeatureInfo fi: fFeatureInfos) {
             try {
                 rewriteFeatureManifest(fi, changes, changedFiles);
             } catch (IOException io) {
         	ReleaseEngineeringPlugin.logError(io);
             }
         }
     }
 
     private void rewritePluginManifests(List<Change> changes, List<IFile> changedFiles) {
         for(PluginInfo pi: fPluginInfos) {
             IFile manifest= pi.fManifest;
 
             ReleaseEngineeringPlugin.getMsgStream().println("Rewriting plugin manifest " + manifest.getLocation().toPortableString());
 
             TextFileChange tfc= new TextFileChange("Version increment for " + pi.fPluginID, manifest);
             File file= new File(manifest.getLocation().toOSString());
             long curFileLength= file.length();
 
             try {
                 Pattern versionPattern= Pattern.compile("Bundle-Version: [0-9a-zA-Z]+(\\.[0-9a-zA-Z]+)*");
                 String newVersionDecl= "Bundle-Version: " + pi.fPluginNewVersion;
                 String oldContents= getFileContents(new FileReader(file));
                 Matcher versionMatcher= versionPattern.matcher(oldContents);
                 String newContents= versionMatcher.replaceFirst(newVersionDecl);
 
                 tfc.setEdit(new MultiTextEdit());
                 tfc.addEdit(new ReplaceEdit(0, (int) curFileLength, newContents));
 
                 changes.add(tfc);
                 changedFiles.add(manifest);
             } catch(FileNotFoundException fnf) {
         	ReleaseEngineeringPlugin.logError(fnf);
             }
         }
     }
 
     private void doPerformChange(List<Change> changes, String editName, List<IFile> changedFiles) {
         Change change= new CompositeChange(editName, changes.toArray(new Change[changes.size()]));
         change.initializeValidationData(new NullProgressMonitor());
         PerformChangeOperation changeOp= new PerformChangeOperation(change);
 
         try {
             changeOp.setUndoManager(RefactoringCore.getUndoManager(), editName);
             ResourcesPlugin.getWorkspace().run(changeOp, new NullProgressMonitor());
         } catch (CoreException e) {
             ReleaseEngineeringPlugin.logError(e);
         }
         for(IFile f: changedFiles) {
             try {
                 f.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
             } catch (CoreException e) {
         	ReleaseEngineeringPlugin.logError(e);
             }
         }
     }
 
     public void updateFeatureProjectSets() {
         collectMetaData(false);
 
         List<FeatureInfo> selectedFeatures= fFeatureInfos; // selectFeatures();
     
         saveFeatureProjectSets(selectedFeatures);
     }
 
     public void writeSiteFeatureSet(UpdateSiteInfo siteInfo) {
         Map<String/*repoTypeID*/,Set<String/*repoRef*/>> repoRefMap= new HashMap<String,Set<String>>();
 
         for(FeatureRef feature: siteInfo.fFeatureRefs) {
             Pair<String/*repoTypeID*/,String/*repoRef*/> repoDesc= getRepoRefForProject(feature.findProject());
 
             WorkbenchReleaseTool.addMapEntry(repoDesc.first, repoDesc.second, repoRefMap);
         }
         writeProjectSet(repoRefMap, siteInfo.fProject, "features.psf");
     }
 
     public void writeAllSiteFeatureSets() {
         for(UpdateSiteInfo siteInfo: fUpdateSiteInfos) {
             writeSiteFeatureSet(siteInfo);
         }
     }
 
     public void updateUpdateSites() {
         collectMetaData(true);
 
         if (fFeatureInfos.size() == 0)
             return;
 
         List<UpdateSiteInfo> sites= confirmUpdateSites();
 
         if (!sites.isEmpty() && addLatestFeaturesToUpdateSites(sites)) {
             rewriteUpdateSiteManifests(sites);
         } else {
             confirmNoSiteUpdates();
         }
     }
 
     protected abstract void confirmNoSiteUpdates();
 
     private boolean addLatestFeaturesToUpdateSites(List<UpdateSiteInfo> sites) {
         boolean anyChanges= false;
 
         for(UpdateSiteInfo site: sites) {
             Set<String> featureIDs= new HashSet<String>();
             // Collect the IDs of all the features that are presently on the update site
             for(FeatureRef featRef: site.fFeatureRefs) {
                 featureIDs.add(featRef.getID());
             }
             // For each such feature, find the corresponding FeatureInfo and ask the
             // update site to add a ref to the latest version of that feature.
             for(String featureID: featureIDs) {
                 boolean change= site.addFeatureIfMissing(findFeatureInfo(featureID));
                 anyChanges= anyChanges || change;
             }
         }
         return anyChanges;
     }
 
     protected abstract List<UpdateSiteInfo> confirmUpdateSites();
 
     protected void rewriteUpdateSiteManifests(List<UpdateSiteInfo> sites) {
         List<IFile> changedFiles= new ArrayList<IFile>();
         List<Change> changes= new ArrayList<Change>();
 
         for(UpdateSiteInfo site: sites) {
             site.rewriteManifest(changes, changedFiles);
         }
         doPerformChange(changes, "Update site edits", changedFiles);
     }
 
     protected Map<String,Set<String>> readProjectSet(IFile projectSetFile) {
         Map<String,Set<String>> result= new HashMap<String,Set<String>>();
     
         try {
             DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
             DocumentBuilder docBuilder= factory.newDocumentBuilder();
             Document featureSetDoc= docBuilder.parse(new File(projectSetFile.getLocation().toOSString()));
             Node psfNode= featureSetDoc.getFirstChild();
             NodeList providers= psfNode.getChildNodes();
     
             for(int i=0; i < providers.getLength(); i++) {
                 Node provider= providers.item(i);
                 if ("provider".equals(provider.getNodeName())) {
                     String providerType= provider.getAttributes().getNamedItem("id").getNodeValue();
                     NodeList projects= provider.getChildNodes();
 
                     for(int j=0; j < projects.getLength(); j++) {
                         Node project= projects.item(j);
 
                         if (!"project".equals(project.getNodeName()))
                             continue;
 
                         String projectRef= project.getAttributes().getNamedItem("reference").getNodeValue();
 
                         addMapEntry(providerType, projectRef, result);
                     }
                 }
             }
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (IOException e) {
             if (e instanceof FileNotFoundException) {
                 // do nothing; this is ok
             }
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         }
         return result;
     }
 
     public List<FeatureInfo> readUpdateFeatureInfos(UpdateSiteInfo siteInfo) {
         IProject updateProject= siteInfo.fProject;
         IFile featureSetFile= updateProject.getFile("features.psf");
         Map<String,Set<String>> providerToProjectRefs= readProjectSet(featureSetFile);
         List<FeatureInfo> result= new ArrayList<FeatureInfo>();
 
         for(Set<String> projectRefs: providerToProjectRefs.values()) {
             for(String projectRef: projectRefs) {
                 String projectName= projectRef.substring(projectRef.lastIndexOf(',')+1);
 
                 if (projectName.endsWith(".feature")) {
                     projectName= projectName.substring(0, projectName.lastIndexOf(".feature"));
                 }
                 result.add(findFeatureInfo(projectName));
             }
         }
         return result;
     }
 
     protected static List<IFileRevision> getRevisionsSinceLastRelease(IFile file, PluginInfo pi, IFileHistoryProvider histProvider) {
         List<IFileRevision> result= new ArrayList<IFileRevision>();
     
         IFileHistory fileHistory= histProvider.getFileHistoryFor(file, 0, new NullProgressMonitor());
         IFileRevision[] revs= fileHistory.getFileRevisions();
         String curRev= pi.fCurMap.getVersion(file);
         String wsRev= histProvider.getWorkspaceFileRevision(file).getContentIdentifier();
         for(int i= 0; i < revs.length; i++) {
             IFileRevision rev= revs[i];
             String revID= rev.getContentIdentifier();
             if (curRev == null || (compareRevs(curRev, revID) < 0 && compareRevs(revID, wsRev) <= 0)) {
                 result.add(rev);
             }
         }
         return result;
     }
 
     protected static int compareRevs(String revA, String revB) {
         String[] revAComps= revA.split("\\.");
         String[] revBComps= revB.split("\\.");
         for(int i=0; i < Math.min(revAComps.length, revBComps.length); i++) {
             int comp= Integer.valueOf(revAComps[i]).compareTo(Integer.valueOf(revBComps[i]));
             if (comp < 0) return -1;
             if (comp > 0) return 1;
         }
         if (revAComps.length < revBComps.length) return -1;
         if (revAComps.length > revBComps.length) return 1;
         return 0;
     }
 
     protected boolean isDirty(IFile file) {
         // Following is adapted from CVSLightweightDecorator
         try {
             return getCVSSubscriber().isDirty(file, null);
         } catch (CVSException e) {
             return false;
         }
         // Following is also copied from CVSLightweightDecorator
 //        IDiff node = getSubscriber().getDiff(file);
 //        if (node != null) {
 //                if (node instanceof IThreeWayDiff) {
 //                        IThreeWayDiff twd = (IThreeWayDiff) node;
 //                        cvsDecoration.setDirty(twd.getDirection() == IThreeWayDiff.OUTGOING 
 //                                || twd.getDirection() == IThreeWayDiff.CONFLICTING);
 //                }
 //        }
     }
 
     public abstract boolean retrieveFeatures();
 
     public abstract boolean retrievePlugins();
 
     protected void getProjectsFromRefs(Map<String, Set<String>> projectRefs, IProgressMonitor monitor) {
         MessageConsoleStream msgStream= ReleaseEngineeringPlugin.getMsgStream();
 
         monitor.beginTask("Retrieving projects", collectSetsFromMap(projectRefs).size());
 
         try {
             for(String providerTypeID: projectRefs.keySet()) {
                 RepositoryProviderType repoType= RepositoryProviderType.getProviderType(providerTypeID);
                 ProjectSetCapability projectSetCapability= repoType.getProjectSetCapability();
 
                 List<String> sortedProjectRefs= new ArrayList<String>();
 
                 sortedProjectRefs.addAll(projectRefs.get(providerTypeID));
                 Collections.sort(sortedProjectRefs);
 
                 for(String projectRef: sortedProjectRefs) {
                     if (monitor.isCanceled()) {
                         return;
                     }
 
                     String projectName= projectSetCapability.getProject(projectRef);

                     monitor.subTask("Retrieving " + projectName);
                     try {
                         projectSetCapability.addToWorkspace(new String[] { projectRef }, new ProjectSetSerializationContext(), monitor);
                     } catch (TeamException e) {
                         msgStream.println("Exception encountered while retrieving " + projectName);
                     }
                     monitor.worked(1);
                 }
             }
         } finally {
             monitor.done();
         }
     }
 
     private boolean fTemporarilySuppressAutoBuild= true;
 
     protected void retrieveProjectsWithProgress(final Map<String, Set<String>> projectRefs) {
         IWorkspace workspace= fWSRoot.getWorkspace();
         IWorkspaceDescription wsDesc= workspace.getDescription();
         boolean wasAutoBuilding= wsDesc.isAutoBuilding();
 
         // Temporarily turn off auto-building while we're checking out projects,
         // to avoid re-building lots of stuff, since the projects won't come in
         // in dependency order anyway.
         if (fTemporarilySuppressAutoBuild && wasAutoBuilding) {
             setAutoBuilding(false, workspace, wsDesc);
         }
 
         IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
     
         try {
             progressService.run(true, true, new IRunnableWithProgress() {
                 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                     getProjectsFromRefs(projectRefs, monitor);
                 }
             });
         } catch (InvocationTargetException e) {
             postError("Exception encountered while retrieving projects: " + e.getMessage(), e);
         } catch (InterruptedException e) {
            postError("Exception encountered while retrieving projects: " + e.getMessage(), e);
         }
 
         if (fTemporarilySuppressAutoBuild && wasAutoBuilding) {
             setAutoBuilding(wasAutoBuilding, workspace, wsDesc);
         }
     }
 
     private void setAutoBuilding(boolean b, IWorkspace workspace, IWorkspaceDescription wsDesc) {
         wsDesc.setAutoBuilding(b);
         try {
             workspace.setDescription(wsDesc);
         } catch (CoreException e) {
         }
     }
 
     public static final String CVS_UPDATE_SITE_PROVIDER= "org.eclipse.team.cvs.core.cvsnature";
     public static final String SVN_UPDATE_SITE_PROVIDER= "org.tigris.subversion.subclipse.core.svnnature";
 
     public static final String IMP_UPDATE_SITE_PROJECT= "org.eclipse.imp.update";
     public static final String IMP_REPO_SERVER= "dev.eclipse.org";
     public static final String IMP_REPO_PATH= "/svnroot/technology/org.eclipse.imp";
 
     public static final String IMP_UPDATE_SITE_REF= "0.9.3,https://dev.eclipse.org/svnroot/technology/org.eclipse.imp/org.eclipse.imp.update/trunk,org.eclipse.imp.update";
 
     public void retrieveProject(String projectRef, String providerType) {
         Map<String, Set<String>> projectRefs= new HashMap<String, Set<String>>();
 
         projectRefs.put(providerType, Collections.singleton(projectRef));
 
         retrieveProjectsWithProgress(projectRefs);
     }
 
     /**
      * Works around SVN provider's inability to produce a project name
      * from a given project ref (cool!).
      */
     protected String projectNameFromProjectRef(String projectRef, ProjectSetCapability projectSetCapability) {
         String projectName= projectSetCapability.getProject(projectRef);
         if (projectName == null) {
             // Produce *something*!
             projectName= projectRef.substring(projectRef.lastIndexOf(',')+1);
         }
         return projectName;
     }
 
     protected <K,V> List<V> collectSetsFromMap(Map<K, Set<V>> map) {
         List<V> result= new ArrayList<V>();
         for(Set<V> s: map.values()) {
             result.addAll(s);
         }
         return result;
     }
 
     protected <K, V> void mergeMapInto(Map<K, Set<V>> src, Map<K, Set<V>> dest) {
         for(K key: src.keySet()) {
             for(V value: src.get(key)) {
                 addMapEntry(key, value, dest);
             }
         }
     }
 
     protected List<String> collectProjectNamesFromProviderMap(Map<String, Set<String>> map) {
         List<String> result= new ArrayList<String>();
         for(String providerTypeID: map.keySet()) {
             RepositoryProviderType repoType= RepositoryProviderType.getProviderType(providerTypeID);
             ProjectSetCapability projectSetCapability= repoType.getProjectSetCapability();
             
             for(String projectRef: map.get(providerTypeID)) {
                 String projectName= projectNameFromProjectRef(projectRef, projectSetCapability);
                 result.add(projectName);
             }
         }
         Collections.sort(result);
         return result;
     }
 
     public void tagFeatures() {
         // For inspiration, see org.eclipse.releng.tools.TagAndReleaseOperation
         //
         // N.B. SVN doesn't support tags, so this may have to copy the given
         // folder to a name that embeds the desired tag, and set the SVN
         // "final" property on the resulting folder.
         //
         collectMetaData(true);
 
         List<FeatureInfo> featureInfos= selectFeatureInfos();
         final Map<IProject,String/*tag*/> projectTagMap= collectProjectTags(featureInfos);
         IProgressService progressService= PlatformUI.getWorkbench().getProgressService();
 
         try {
             progressService.run(true, true, new IRunnableWithProgress() {
                 public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                     tagFeatureProjects(projectTagMap, monitor);
                 }
             });
         } catch (InvocationTargetException e) {
             postError("Exception encountered while retrieving projects: " + e.getMessage(), e);
         } catch (InterruptedException e) {
             postError("Exception encountered while retrieving projects: " + e.getMessage(), e);
         }
     }
 
     private Map<IProject, String/*tag*/> collectProjectTags(List<FeatureInfo> featureInfos) {
         Map<IProject,String/*tag*/> result= new HashMap<IProject, String>();
 
         for(FeatureInfo featureInfo: featureInfos) {
             String featureID= featureInfo.fFeatureID;
             String featureVersion= featureInfo.fFeatureVersion;
             IProject project= featureInfo.fProject;
             String tag= featureID.replace('.', '-') + "_" + featureVersion.replace('.', '_');
 
             result.put(project, tag);
 
             Set<PluginInfo> plugins= featureInfo.fPluginInfos;
 
             for(PluginInfo pluginInfo: plugins) {
                 IProject pluginProject= fWSRoot.getProject(pluginInfo.fPluginID);
 
                 if (pluginProject == null) {
                     postError("Unable to find project for plugin " + pluginInfo.fPluginID, null);
                 } else {
                     result.put(pluginProject, tag);
                 }
             }
         }
         return result;
     }
 
     private void tagFeatureProjects(Map<IProject,String/*tag*/> projectTagMap, IProgressMonitor progress) {
         int scale= projectTagMap.keySet().size();
         progress.beginTask(null, 100 * scale);
 
         for(IProject project: projectTagMap.keySet()) {
             String projectTag= projectTagMap.get(project);
             Set localOptions = new HashSet();
             LocalOption[] commandOptions = (LocalOption[]) localOptions.toArray(new LocalOption[localOptions.size()]);
 
             commandOptions = Command.DO_NOT_RECURSE.removeFrom(commandOptions);
 
             CVSTag tag= new CVSTag(projectTag, CVSTag.VERSION);
             CVSTeamProvider provider = (CVSTeamProvider) RepositoryProvider.getProvider(project);
 
             // Build the arguments list
             String[] arguments = getStringArguments(new IResource[] { project });
             Session s= null;
 
             try {
                 // Execute the command
                 CVSWorkspaceRoot root= provider.getCVSWorkspaceRoot();
                 s = new Session(root.getRemoteLocation(), root.getLocalRoot());
 
                 // Opening the session takes 20% of the time
                 s.open(subMonitorFor(progress, 20), true /* open for modification */);
                 IStatus status= Command.TAG.execute(s,
                         Command.NO_GLOBAL_OPTIONS,
                         commandOptions,
                         tag,
                         arguments,
                         null,
                         subMonitorFor(progress, 80));
                 if (status.getSeverity() != IStatus.OK) {
                     System.err.println("Tag command execution finished: " + status.getMessage());
                     IStatus[] children= status.getChildren();
                     if (children != null && children.length > 0) {
                         for(int i= 0; i < children.length; i++) {
                             System.err.println(children[i].getMessage());
                         }
                     }
                 }
             } catch (CVSException e) {
                 e.printStackTrace();
             } finally {
                 if (s != null)
                     s.close();
             }
         }
         progress.done();
     }
 
     private IProgressMonitor subMonitorFor(IProgressMonitor monitor, int ticks) {
         if (monitor == null)
             return new NullProgressMonitor();
         if (monitor instanceof NullProgressMonitor)
             return monitor;
         return new SubProgressMonitor(monitor, ticks);
     }
 
     protected String[] getStringArguments(IResource[] resources) {
         List arguments = new ArrayList(resources.length);
         for (int i=0;i<resources.length;i++) {
             IPath cvsPath = resources[i].getFullPath().removeFirstSegments(1);
             if (cvsPath.segmentCount() == 0) {
                 arguments.add(Session.CURRENT_LOCAL_FOLDER);
             } else {
                 arguments.add(cvsPath.toString());
             }
         }
         return (String[])arguments.toArray(new String[arguments.size()]);
     }
 
     private static CVSWorkspaceSubscriber getCVSSubscriber() {
         return CVSProviderPlugin.getPlugin().getCVSWorkspaceSubscriber();
     }
 
     public static Set<Node> getChildNodesNamed(String name, Node parent) {
         Set<Node> result= new HashSet<Node>();
         NodeList children= parent.getChildNodes();
 
         for(int i=0; i < children.getLength(); i++) {
             Node child= children.item(i);
             if (child.getNodeName().equals(name))
                 result.add(child);
         }
         return result;
     }
 
     public static String getFileContents(Reader reader) {
 	// In this case we don't know the length in advance, so we have to
 	// accumulate the reader's contents one buffer at a time.
 	StringBuilder sb= new StringBuilder(4096);
 	char[] buff= new char[4096];
 	int len;
 
 	while(true) {
 	    try {
 		len= reader.read(buff);
 	    } catch (IOException e) {
 		break;
 	    }
 	    if (len < 0)
 		break;
 	    sb.append(buff, 0, len);
 	}
 	return sb.toString();
     }
 
     public UpdateSiteInfo findSiteByName(String name) {
         for(UpdateSiteInfo siteInfo: fUpdateSiteInfos) {
             if (siteInfo.fProject.getName().equals(name)) {
                 return siteInfo;
             }
         }
         return null;
     }
 
     public abstract void updateFeatureList();
 }
