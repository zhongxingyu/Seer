 // This file is part of OpenTSDB.
 // Copyright (C) 2010  StumbleUpon, Inc.
 //
 // This program is free software: you can redistribute it and/or modify it
 // under the terms of the GNU Lesser General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or (at your
 // option) any later version.  This program is distributed in the hope that it
 // will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 // of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser
 // General Public License for more details.  You should have received a copy
 // of the GNU Lesser General Public License along with this program.  If not,
 // see <http://www.gnu.org/licenses/>.
 
 /** Quick CLI tool to get JMX MBean attributes.  */
 package com.stumbleupon.monitoring;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import javax.management.MBeanAttributeInfo;
 import javax.management.MBeanInfo;
 import javax.management.MBeanNotificationInfo;
 import javax.management.MBeanOperationInfo;
 import javax.management.MBeanServerConnection;
 import javax.management.MalformedObjectNameException;
 import javax.management.ObjectName;
 import javax.management.remote.JMXConnector;
 import javax.management.remote.JMXConnectorFactory;
 import javax.management.remote.JMXServiceURL;
 
 // Sun specific
 import com.sun.tools.attach.AgentInitializationException;
 import com.sun.tools.attach.AgentLoadException;
 import com.sun.tools.attach.AttachNotSupportedException;
 import com.sun.tools.attach.VirtualMachine;
 import com.sun.tools.attach.VirtualMachineDescriptor;
 
 // Sun private
 import sun.management.ConnectorAddressLink;
 import sun.jvmstat.monitor.HostIdentifier;
 import sun.jvmstat.monitor.MonitoredHost;
 import sun.jvmstat.monitor.MonitoredVm;
 import sun.jvmstat.monitor.MonitoredVmUtil;
 import sun.jvmstat.monitor.VmIdentifier;
 
 final class jmx {
 
   private static final String LOCAL_CONNECTOR_ADDRESS =
     "com.sun.management.jmxremote.localConnectorAddress";
 
   private static void usage() {
       System.out.println("Usage:\n"
                          + "  jmx -l                    Lists all reachable VMs.\n"
                          + "  jmx <JVM>                 Lists all MBeans for this JVM (PID or regexp).\n"
                          + "  jmx <JVM> <MBean>         Prints all the attributes of this MBean.\n"
                          + "  jmx <JVM> <MBean> <attr>  Prints the matching attributes of this MBean.");
   }
 
   public static void main(final String[] args) throws Exception {
     if (args.length == 0 || "-h".equals(args[0]) || "--help".equals(args[0])) {
       usage();
       System.exit(args.length == 0 ? 1 : 0);
       return;
     }
 
     HashMap<Integer, JVM> vms = getJVMs();
     if ("-l".equals(args[0])) {
       printVmList(vms.values());
       return;
     }
 
     final JVM jvm = selectJVM(args[0], vms);
     vms = null;
     final JMXConnector connection = JMXConnectorFactory.connect(jvm.jmxUrl());
     try {
       final MBeanServerConnection mbsc = connection.getMBeanServerConnection();
       if (args.length == 1) {
         for (final ObjectName mbean : listMBeans(mbsc)) {
           System.out.println(mbean);
         }
         return;
       }
 
       final ArrayList<ObjectName> objects = selectMBeans(args[1], mbsc);
       if (objects.isEmpty()) {
        System.err.println("No MBean matched " + args[1] + " in " + jvm.name());
        System.exit(1);
         return;
       }
       final boolean multiple = objects.size() > 1;
       boolean found = false;
       for (final ObjectName object : objects) {
         final MBeanInfo mbean = mbsc.getMBeanInfo(object);
         final Pattern wanted = args.length == 2 ? null : compile_re(args[2]);
         for (final MBeanAttributeInfo attr : mbean.getAttributes()) {
           if (wanted == null || wanted.matcher(attr.getName()).find()) {
             dumpMBean(mbsc, object, attr);
             found = true;
           }
         }
       }
       if (!found) {
        System.err.println("No attribute of " + objects + " matched "
                           + args[2] + " in " + jvm.name());
        System.exit(1);
         return;
       }
     } finally {
       connection.close();
     }
   }
 
   private static ArrayList<ObjectName> selectMBeans(final String selector,
                                                     final MBeanServerConnection mbsc) throws IOException {
     ObjectName object = null;
     Pattern object_re = null;
     try {
       object = new ObjectName(selector);
       final ArrayList<ObjectName> mbeans = new ArrayList<ObjectName>(1);
       mbeans.add(object);
       return mbeans;
     } catch (MalformedObjectNameException e) {
       object_re = compile_re(selector);
       final ArrayList<ObjectName> mbeans = new ArrayList<ObjectName>();
       final Iterator<ObjectName> it = listMBeans(mbsc).iterator();
       while (it.hasNext()) {
         final ObjectName o = it.next();
         if (object_re.matcher(o.toString()).find()) {
           mbeans.add(o);
         }
       }
       return mbeans;
     }
   }
 
   private static void dumpMBean(final MBeanServerConnection mbsc,
                                 final ObjectName object,
                                 final MBeanAttributeInfo attr) throws Exception {
     final String name = attr.getName();
     Object value = mbsc.getAttribute(object, name);
     if (value instanceof Object[]) {
       final StringBuilder buf = new StringBuilder();
       for (final Object o : (Object[]) value) {
         buf.append(o).append('\t');
       }
       buf.setLength(buf.length() - 1);
       value = buf.toString();
     }
     System.out.println(name + "\t" + value);
   }
 
   private static ArrayList<ObjectName> listMBeans(final MBeanServerConnection mbsc) throws IOException {
     ArrayList<ObjectName> mbeans = new ArrayList<ObjectName>(mbsc.queryNames(null, null));
     Collections.sort(mbeans, new Comparator<ObjectName>() {
       public int compare(final ObjectName a, final ObjectName b) {
         return a.toString().compareTo(b.toString());
       }
     });
     return mbeans;
   }
 
   private static Pattern compile_re(final String re) {
     try {
       return Pattern.compile(re);
     } catch (PatternSyntaxException e) {
      System.err.println("Invalid regexp: " + re + ", " + e.getMessage());
      System.exit(1);
       throw new AssertionError("Should never be here");
     }
   }
 
   private static final String MAGIC_STRING = "this.is.jmx.magic";
 
   private static JVM selectJVM(final String selector,
                                final HashMap<Integer, JVM> vms) {
     String error = null;
     try {
       final int pid = Integer.parseInt(selector);
       if (pid < 2) {
         throw new IllegalArgumentException("Invalid PID: " + pid);
       }
       final JVM jvm = vms.get(pid);
       if (jvm == null) {
         error = "Couldn't find a JVM with PID " + pid;
       }
     } catch (NumberFormatException e) {
       /* Ignore. */
     }
     if (error == null) {
       try {
         final Pattern p = compile_re(selector);
         final ArrayList<JVM> matches = new ArrayList<JVM>(2);
         for (final JVM jvm : vms.values()) {
           if (p.matcher(jvm.name()).find()) {
             matches.add(jvm);
           }
         }
         // Exclude ourselves from the matches.
         System.setProperty(MAGIC_STRING,
                            "LOL Java processes can't get their own PID");
         final Iterator<JVM> it = matches.iterator();
         while (it.hasNext()) {
           final JVM jvm = it.next();
           final VirtualMachine vm = VirtualMachine.attach(String.valueOf(jvm.pid()));
           try {
             if (vm.getSystemProperties().containsKey(MAGIC_STRING)) {
               it.remove();
               break;
             }
           } finally {
             vm.detach();
           }
         }
         System.clearProperty(MAGIC_STRING);
         if (matches.size() == 0) {
           error = "No JVM matched your regexp " + selector;
         } else if (matches.size() > 1) {
           printVmList(matches);
           error = matches.size() + " JVMs matched your regexp " + selector
             + ", it's too ambiguous, please refine it.";
         } else {
           return matches.get(0);
         }
       } catch (PatternSyntaxException e) {
         error = "Invalid pattern: " + selector + ", " + e.getMessage();
       } catch (Exception e) {
         e.printStackTrace();
         error = "Unexpected Exception: " + e.getMessage();
       }
     }
    System.err.println(error);
    System.exit(1);
     return null;
   }
 
   private static void printVmList(final Collection<JVM> vms) {
     final ArrayList<JVM> sorted_vms = new ArrayList<JVM>(vms);
     Collections.sort(sorted_vms, new Comparator<JVM>() {
       public int compare(final JVM a, final JVM b) {
         return a.pid() - b.pid();
       }
     });
     for (final JVM jvm : sorted_vms) {
       System.out.println(jvm.pid() + "\t" + jvm.name());
     }
   }
 
   private static final class JVM {
     final int pid;
     final String name;
     String address;
 
     public JVM(final int pid, final String name, final String address) {
       if (name.isEmpty()) {
         throw new IllegalArgumentException("empty name");
       }
       this.pid = pid;
       this.name = name;
       this.address = address;
     }
 
     public int pid() {
       return pid;
     }
 
     public String name() {
       return name;
     }
 
     public JMXServiceURL jmxUrl() {
       if (address == null) {
         ensureManagementAgentStarted();
       }
       try {
         return new JMXServiceURL(address);
       } catch (Exception e) {
         throw new RuntimeException("Error", e);
       }
     }
 
     public void ensureManagementAgentStarted() {
       if (address != null) {  // already started
         return;
       }
       VirtualMachine vm;
       try {
         vm = VirtualMachine.attach(String.valueOf(pid));
       } catch (AttachNotSupportedException e) {
         throw new RuntimeException("Failed to attach to " + this, e);
       } catch (IOException e) {
         throw new RuntimeException("Failed to attach to " + this, e);
       }
       try {
         // java.sun.com/javase/6/docs/technotes/guides/management/agent.html#gdhkz
         // + code mostly stolen from JConsole's code.
         final String home = vm.getSystemProperties().getProperty("java.home");
 
         // Normally in ${java.home}/jre/lib/management-agent.jar but might
         // be in ${java.home}/lib in build environments.
 
         String agent = home + File.separator + "jre" + File.separator
           + "lib" + File.separator + "management-agent.jar";
         File f = new File(agent);
         if (!f.exists()) {
           agent = home + File.separator +  "lib" + File.separator
             + "management-agent.jar";
           f = new File(agent);
           if (!f.exists()) {
             throw new RuntimeException("Management agent not found");
           }
         }
 
         agent = f.getCanonicalPath();
         try {
           vm.loadAgent(agent, "com.sun.management.jmxremote");
         } catch (AgentLoadException e) {
           throw new RuntimeException("Failed to load the agent into " + this, e);
         } catch (AgentInitializationException e) {
           throw new RuntimeException("Failed to initialize the agent into " + this, e);
         }
         address = (String) vm.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS);
       } catch (IOException e) {
         throw new RuntimeException("Error while loading agent into " + this, e);
       } finally {
         try {
           vm.detach();
         } catch (IOException e) {
           throw new RuntimeException("Failed to detach from " + vm + " = " + this, e);
         }
       }
       if (address == null) {
         throw new RuntimeException("Couldn't start the management agent.");
       }
     }
 
     public String toString() {
       return "JVM(" + pid + ", \"" + name + "\", "
         + (address == null ? null : '"' + address + '"') + ')';
     }
   }
 
   /**
    * Returns a map from PID to JVM.
    */
   private static HashMap<Integer, JVM> getJVMs() throws Exception {
     final HashMap<Integer, JVM> vms = new HashMap<Integer, JVM>();
     getMonitoredVMs(vms);
     getAttachableVMs(vms);
     return vms;
   }
 
   private static void getMonitoredVMs(final HashMap<Integer, JVM> out) throws Exception {
     final MonitoredHost host =
       MonitoredHost.getMonitoredHost(new HostIdentifier((String) null));
     @SuppressWarnings("unchecked")
     final Set<Integer> vms = host.activeVms();
     for (final Integer pid : vms) {
       try {
         final VmIdentifier vmid = new VmIdentifier(pid.toString());
         final MonitoredVm vm = host.getMonitoredVm(vmid);
         out.put(pid, new JVM(pid, MonitoredVmUtil.commandLine(vm),
                              ConnectorAddressLink.importFrom(pid)));
         vm.detach();
       } catch (Exception x) {
         System.err.println("Ignoring exception:");
         x.printStackTrace();
       }
     }
   }
 
   private static void getAttachableVMs(final HashMap<Integer, JVM> out) {
     for (final VirtualMachineDescriptor vmd : VirtualMachine.list()) {
       int pid;
       try {
         pid = Integer.parseInt(vmd.id());
       } catch (NumberFormatException e) {
         System.err.println("Ignoring invalid vmd.id(): " + vmd.id()
                            + ' ' + e.getMessage());
         continue;
       }
       if (out.containsKey(pid)) {
         continue;
       }
       try {
         final VirtualMachine vm = VirtualMachine.attach(vmd);
         out.put(pid, new JVM(pid, String.valueOf(pid),
                              (String) vm.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS)));
         vm.detach();
       } catch (AttachNotSupportedException e) {
         System.err.println("VM not attachable: " + vmd.id()
                            + ' ' + e.getMessage());
       } catch (IOException e) {
         System.err.println("Could not attach: " + vmd.id()
                            + ' ' + e.getMessage());
       }
     }
   }
 
 }
