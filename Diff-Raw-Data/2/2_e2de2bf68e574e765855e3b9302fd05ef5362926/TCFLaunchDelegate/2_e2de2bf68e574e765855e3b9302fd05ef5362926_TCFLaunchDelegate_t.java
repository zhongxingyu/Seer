 /*******************************************************************************
  * Copyright (c) 2007, 2013 Wind River Systems, Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.debug.launch;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
 import org.eclipse.tcf.internal.debug.Activator;
 import org.eclipse.tcf.internal.debug.model.ITCFConstants;
 import org.eclipse.tcf.internal.debug.model.TCFLaunch;
 import org.eclipse.tcf.internal.debug.model.TCFMemoryRegion;
 import org.eclipse.tcf.protocol.JSON;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.services.IMemoryMap;
 import org.eclipse.tcf.services.IPathMap;
 import org.eclipse.tcf.util.TCFPathMapRule;
 import org.eclipse.tcf.util.TCFTask;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 
 public class TCFLaunchDelegate extends LaunchConfigurationDelegate {
 
     public static final String
         ATTR_PEER_ID = ITCFConstants.ID_TCF_DEBUG_MODEL + ".PeerID",
         ATTR_PROJECT_NAME = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProjectName",
         ATTR_LOCAL_PROGRAM_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".LocalProgramFile",
         ATTR_REMOTE_PROGRAM_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramFile",
         ATTR_COPY_TO_REMOTE_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".CopyToRemote",
         ATTR_PROGRAM_ARGUMENTS = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramArguments",
         ATTR_WORKING_DIRECTORY = ITCFConstants.ID_TCF_DEBUG_MODEL + ".WorkingDirectory",
         ATTR_ATTACH_CHILDREN = ITCFConstants.ID_TCF_DEBUG_MODEL + ".AttachChildren",
         ATTR_STOP_AT_ENTRY = ITCFConstants.ID_TCF_DEBUG_MODEL + ".StopAtEntry",
         ATTR_STOP_AT_MAIN = ITCFConstants.ID_TCF_DEBUG_MODEL + ".StopAtMain",
         ATTR_DISCONNECT_ON_CTX_EXIT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".DisconnectOnCtxExit",
         ATTR_USE_TERMINAL = ITCFConstants.ID_TCF_DEBUG_MODEL + ".UseTerminal",
         ATTR_RUN_LOCAL_AGENT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".RunLocalAgent",
         ATTR_USE_LOCAL_AGENT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".UseLocalAgent",
         ATTR_SIGNALS_DONT_STOP = ITCFConstants.ID_TCF_DEBUG_MODEL + ".SignalsDontStop",
        ATTR_SIGNALS_DONT_PASS = ITCFConstants.ID_TCF_DEBUG_MODEL + ".SignalsDontPass",
         ATTR_FILES = ITCFConstants.ID_TCF_DEBUG_MODEL + ".Files",
         ATTR_PATH_MAP = ITCFConstants.ID_TCF_DEBUG_MODEL + ".PathMap",
         ATTR_MEMORY_MAP = ITCFConstants.ID_TCF_DEBUG_MODEL + ".MemoryMap",
         ATTR_ATTACH_PATH = ITCFConstants.ID_TCF_DEBUG_MODEL + ".Attach";
 
     public static final String
         FILES_CONTEXT_FULL_NAME = "Context",
         FILES_CONTEXT_ID = "ContextID",
         FILES_FILE_NAME = "File",
         FILES_LOAD_SYMBOLS = "LoadSymbols",
         FILES_RELOCATE = "Relocate",
         FILES_ADDRESS = IMemoryMap.PROP_ADDRESS,
         FILES_OFFSET = IMemoryMap.PROP_OFFSET,
         FILES_SIZE = IMemoryMap.PROP_SIZE,
         FILES_DOWNLOAD = "Download",
         FILES_SET_PC = "SetPC",
         FILES_ENABLE_OSA = "EnableOSA";
 
     public static class PathMapRule extends TCFPathMapRule {
 
         public PathMapRule(Map<String,Object> props) {
             super(props);
         }
 
         /* (non-Javadoc)
          * @see java.lang.Object#equals(java.lang.Object)
          */
         @Override
         public boolean equals(Object obj) {
             boolean equal = super.equals(obj);
             if (!equal && obj instanceof PathMapRule) {
                 return this.toString().equals(((PathMapRule)obj).toString());
             }
             return equal;
         }
 
         @Override
         public String toString() {
             StringBuffer bf = new StringBuffer();
             Map<String,Object> props = getProperties();
             String[] keySet = props.keySet().toArray(new String[props.size()]);
             Arrays.sort(keySet);
             for (String nm : keySet) {
                 Object o = props.get(nm);
                 if (o != null) {
                     bf.append(nm);
                     bf.append('=');
                     String s = o.toString();
                     for (int i = 0; i < s.length(); i++) {
                         char ch = s.charAt(i);
                         if (ch >= ' ' && ch != '|' && ch != '\\') {
                             bf.append(ch);
                         }
                         else {
                             bf.append('\\');
                             bf.append((int)ch);
                             bf.append(';');
                         }
                     }
                     bf.append('|');
                 }
             }
             bf.append('|');
             return bf.toString();
         }
     }
 
     /**
      * Given value of ATTR_PATH_MAP, return array of PathMapRule objects.
      * @param s - value of ATTR_PATH_MAP.
      * @return array of PathMapRule objects.
      */
     public static ArrayList<PathMapRule> parsePathMapAttribute(String s) {
         ArrayList<PathMapRule> map = new ArrayList<PathMapRule>();
         StringBuffer bf = new StringBuffer();
         int i = 0;
         while (i < s.length()) {
             // To guarantee a predictable path map properties iteration order,
             // we have to use a LinkedHashMap.
             PathMapRule e = new PathMapRule(new LinkedHashMap<String,Object>());
             while (i < s.length()) {
                 char ch = s.charAt(i++);
                 if (ch == '|') {
                     map.add(e);
                     break;
                 }
                 bf.setLength(0);
                 bf.append(ch);
                 while (i < s.length()) {
                     ch = s.charAt(i++);
                     if (ch == '=') break;
                     bf.append(ch);
                 }
                 String nm = bf.toString();
                 bf.setLength(0);
                 while (i < s.length()) {
                     ch = s.charAt(i++);
                     if (ch == '|') {
                         if (bf.length() > 0) e.getProperties().put(nm, bf.toString());
                         break;
                     }
                     else if (ch == '\\') {
                         int n = 0;
                         while (i < s.length()) {
                             char d = s.charAt(i++);
                             if (d == ';') break;
                             n = n * 10 + (d - '0');
                         }
                         bf.append((char)n);
                     }
                     else {
                         bf.append(ch);
                     }
                 }
             }
         }
         return map;
     }
 
     /**
      * Given value of ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO,
      * return array of PathMapRule objects.
      * @param s - value of ATTR_PATH_MAP.
      * @return array of PathMapRule objects.
      */
     public static ArrayList<PathMapRule> parseSourceLocatorMemento(String s) throws CoreException {
         ArrayList<PathMapRule> map = new ArrayList<PathMapRule>();
         if (s == null || s.length() == 0) return map;
         Element root = DebugPlugin.parseDocument(s);
         NodeList list = root.getChildNodes();
         int length = list.getLength();
         for (int i = 0; i < length; i++) {
             Node node = list.item(i);
             short type = node.getNodeType();
             if (type == Node.ELEMENT_NODE) {
                 Element entry = (Element)node;
                 if (entry.getNodeName().equalsIgnoreCase("sourceContainers")) {
                     parseSourceContainers(map, entry);
                 }
             }
         }
         return map;
     }
 
     private static void parseSourceContainers(ArrayList<PathMapRule> map, Element element) throws CoreException {
         NodeList list = element.getChildNodes();
         int length = list.getLength();
         for (int i = 0; i < length; i++) {
             Node node = list.item(i);
             short type = node.getNodeType();
             if (type == Node.ELEMENT_NODE) {
                 Element entry = (Element)node;
                 String memento = entry.getAttribute("memento");
                 if (memento != null && memento.length() > 0) readSourceContainer(map, memento);
             }
         }
     }
 
     private static void readSourceContainer(ArrayList<PathMapRule> map, String s) throws CoreException {
         // The user may add source container which stores their memento not
         // as an XML string. For those containers, DebugPlugin.parseDocument(s)
         // will throw an CoreException and the debug launch fails. Such source
         // container should be ignored and the launch should continue.
         Element root = null;
         try {
             root = DebugPlugin.parseDocument(s);
         } catch (CoreException e) {
             // The memento does not represent a XML string
         }
         if (root == null) return;
 
         if ("mapping".equals(root.getNodeName())) {
             NodeList list = root.getChildNodes();
             int length = list.getLength();
             for (int i = 0; i < length; i++) {
                 Node node = list.item(i);
                 short type = node.getNodeType();
                 if (type == Node.ELEMENT_NODE) {
                     Element entry = (Element)node;
                     if (entry.getNodeName().equalsIgnoreCase("mapEntry")) {
                         String memento = entry.getAttribute("memento");
                         if (memento != null && memento.length() > 0) {
                             Element map_entry = DebugPlugin.parseDocument(memento);
                             String src = map_entry.getAttribute("backendPath");
                             String dst = map_entry.getAttribute("localPath");
                             if (src != null) src = src.replace('\\', '/');
                             // To guarantee a predictable path map properties iteration order,
                             // we have to use a LinkedHashMap.
                             Map<String,Object> props = new LinkedHashMap<String,Object>();
                             props.put(IPathMap.PROP_SOURCE, src);
                             props.put(IPathMap.PROP_DESTINATION, dst);
                             map.add(new PathMapRule(props));
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Given value of ATTR_MEMORY_MAP, add lists of TCFMemoryRegion objects into 'maps'.
      * @param maps - Map object to fill with memory maps.
      * @param s - value of ATTR_MEMORY_MAP.
      */
     @SuppressWarnings("unchecked")
     public static void parseMemMapsAttribute(Map<String,ArrayList<IMemoryMap.MemoryRegion>> maps, String s) throws Exception {
         if (s == null || s.length() == 0) return;
         Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)JSON.parseOne(s.getBytes("UTF-8"));
         if (list == null) return;
         for (Map<String,Object> map : list) {
             String id = (String)map.get(IMemoryMap.PROP_ID);
             if (id != null) {
                 ArrayList<IMemoryMap.MemoryRegion> l = maps.get(id);
                 if (l == null) {
                     l = new ArrayList<IMemoryMap.MemoryRegion>();
                     maps.put(id, l);
                 }
                 l.add(new TCFMemoryRegion(map));
             }
         }
     }
 
     /**
      * Read ATTR_MEMORY_MAP attribute of a launch configuration.
      * @param maps - Map object to fill with memory maps.
      * @param cfg - the launch configuration.
      * @throws Exception
      */
     public static void getMemMapsAttribute(Map<String,ArrayList<IMemoryMap.MemoryRegion>> maps,
             ILaunchConfiguration cfg) throws Exception {
         String maps_cfg = cfg.getAttribute(ATTR_MEMORY_MAP, (String)null);
         parseMemMapsAttribute(maps, maps_cfg);
     }
 
     /**
      * Given project name and program name returns absolute path of the program.
      * @param project_name - workspace project name.
      * @param program_name - launch program name.
      * @return program path or null if both project name and program name are null.
      */
     public static String getProgramPath(String project_name, String program_name) {
         if (program_name == null || program_name.length() == 0) return null;
         if (project_name == null || project_name.length() == 0) {
             File file = new File(program_name);
             if (!file.isAbsolute()) {
                 File ws = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
                 file = new File(ws, program_name);
             }
             return file.getAbsolutePath();
         }
         IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(project_name);
         IPath program_path = new Path(program_name);
         if (!program_path.isAbsolute()) {
             if (project == null || !project.getFile(program_name).exists()) return null;
             program_path = project.getFile(program_name).getLocation();
         }
         return program_path.toOSString();
     }
 
     /**
      * Create new TCF launch object.
      * @return new TCFLaunch object
      */
     @Override
     public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
         return new TCFTask<ILaunch>() {
             int cnt;
             public void run() {
                 // Need to delay at least one dispatch cycle to work around
                 // a possible racing between thread that calls getLaunch() and
                 // the process of activation of other TCF plug-ins.
                 if (cnt++ < 2) Protocol.invokeLater(this);
                 else done(new TCFLaunch(configuration, mode));
             }
         }.getE();
     }
 
     /**
      * Launch TCF session.
      */
     public void launch(final ILaunchConfiguration configuration, final String mode,
             final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
         String local_id = null;
         if (configuration.getAttribute(ATTR_RUN_LOCAL_AGENT, false)) {
             if (monitor != null) monitor.subTask("Starting TCF Agent"); //$NON-NLS-1$
             local_id = TCFLocalAgent.runLocalAgent();
         }
         else if (configuration.getAttribute(ATTR_USE_LOCAL_AGENT, true)) {
             if (monitor != null) monitor.subTask("Searching TCF Agent"); //$NON-NLS-1$
             local_id = TCFLocalAgent.getLocalAgentID();
             if (local_id == null) throw new CoreException(new Status(IStatus.ERROR,
                     Activator.PLUGIN_ID, 0,
                     "Cannot find TCF agent on the local host",
                     null));
         }
 
         final String id = configuration.getAttribute(ATTR_USE_LOCAL_AGENT, true) ?
                 local_id : configuration.getAttribute(ATTR_PEER_ID, "");
 
         new TCFTask<Boolean>() {
             public void run() {
                 ((TCFLaunch)launch).launchTCF(mode, id, this, monitor);
             }
         }.getE();
     }
 }
