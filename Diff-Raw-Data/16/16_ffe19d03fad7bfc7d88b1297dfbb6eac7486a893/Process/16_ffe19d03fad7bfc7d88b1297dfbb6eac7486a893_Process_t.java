 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.sim.comp.program;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlID;
 import javax.xml.bind.annotation.XmlIDREF;
 import com.quartercode.disconnected.sim.comp.OperatingSystem;
 import com.quartercode.disconnected.sim.comp.file.File;
 import com.quartercode.disconnected.util.InfoString;
 
 /**
  * This class represents a process which is basically a running instance of a program.
  * On the creation, the program object will be backed up, as well as a new program executor instance.
  * 
  * @see Program
  * @see ProgramExecutor
  * @see File
  */
 @XmlAccessorType (XmlAccessType.FIELD)
 public class Process implements InfoString {
 
     @XmlIDREF
     @XmlAttribute
     private OperatingSystem     host;
     @XmlIDREF
     private Process             parent;
     @XmlAttribute
     private int                 pid;
     @XmlIDREF
     private File                file;
     private ProgramExecutor     executor;
 
     @XmlElementWrapper (name = "children")
     @XmlElement (name = "child")
     private final List<Process> children = new ArrayList<Process>();
 
     /**
      * Creates a new empty process.
      * This is only recommended for direct field access (e.g. for serialization).
      */
     protected Process() {
 
     }
 
     /**
      * Creates a new process using the program stored in the given file.
      * This should only be used directly by the operating system.
      * 
      * @param host The host operating system the process will be ran on.
      * @param parent The parent process which created this process.
      * @param pid A unique process id the process has This is used to identify the process.
      * @param file The process launch file which contains the program for the process.
      * @param arguments The argument map which contains values for the defined parameters.
      * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
      */
     public Process(OperatingSystem host, Process parent, int pid, File file, Map<String, Object> arguments) {
 
         if ( (parent == null || pid == 0) && ! (parent == null && pid == 0)) {
             throw new IllegalArgumentException("Can't start a kernel process without parent==null, pid==0");
         } else if (pid != 0 && ! (file.getContent() instanceof Program)) {
             throw new IllegalArgumentException("Process launch file must contain a program");
         }
 
         this.host = host;
         this.parent = parent;
         this.pid = pid;
         this.file = file;
 
         if (pid != 0) {
             Program program = (Program) file.getContent();
             executor = program.createExecutor(this, arguments);
         }
     }
 
     /**
      * Returns the host operating system the process will be ran on.
      * 
      * @return The host operating system the process will be ran on.
      */
     public OperatingSystem getHost() {
 
         return host;
     }
 
     /**
      * Returns the parent process which created this process.
      * 
      * @return The parent process which created this process.
      */
     public Process getParent() {
 
         return parent;
     }
 
     /**
      * Returns a unique process id the process has This is used to identify the process.
      * 
      * @return A unique process id the process has. This is used to identify the process.
      */
     public int getPid() {
 
         return pid;
     }
 
     /**
      * Returns the process launch file which contains the program for the process.
      * 
      * @return The process launch file which contains the program for the process.
      */
     public File getFile() {
 
         return file;
     }
 
     /**
      * Returns the program executor which takes care of running the process.
      * 
      * @return The program executor which takes care of running the process.
      */
     public ProgramExecutor getExecutor() {
 
         return executor;
     }
 
     /**
      * Returns the child processes this process started.
      * 
      * @return The child processes this process started.
      */
     public List<Process> getChildren() {
 
         return Collections.unmodifiableList(children);
     }
 
     /**
      * Returns all child processes and their child processes etc. recursively.
      * 
      * @return All child processes and their child processes etc.
      */
     public List<Process> getAllChildren() {
 
         List<Process> allChildren = new ArrayList<Process>();
         for (Process child : children) {
             allChildren.add(child);
             allChildren.addAll(child.getAllChildren());
         }
         return allChildren;
     }
 
     /**
      * Creates a new child process using the program stored in the given file.
      * The new process will be a child of this process.
      * 
      * @param file The process launch file which contains the program for the process.
      * @param arguments The argument map which contains values for the defined parameters.
      * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
      */
     public Process createChild(File file, Map<String, Object> arguments) {
 
         return createChild(file, arguments, host.requestPid());
     }
 
     /**
      * Creates a new child process using the program stored in the given file using the given pid.
      * The new process will be a child of this process.
      * 
      * @param file The process launch file which contains the program for the process.
      * @param arguments The argument map which contains values for the defined parameters.
      * @param pid A unique process id the process has This is used to identify the process.
      * @throws IllegalArgumentException No or wrong argument type for a specific parameter.
      */
     public Process createChild(File file, Map<String, Object> arguments, int pid) {
 
         Process process = new Process(host, this, pid, file, arguments);
         children.add(process);
         return process;
     }
 
     /**
      * Unregisters a process from the operating system.
      * This should only be used by the process object.
      * 
      * @param process The process to unregister from this operating system.
      */
     public void unregisterChild(Process process) {
 
         children.remove(process);
     }
 
     /**
      * Returns the unique id the process has.
      * 
      * @return The unique id the process has.
      */
     @XmlID
     @XmlAttribute
     public String getId() {
 
         return host.getHost().getId() + "-" + pid;
     }
 
     @Override
     public int hashCode() {
 
         final int prime = 31;
         int result = 1;
         result = prime * result + (children == null ? 0 : children.hashCode());
         result = prime * result + (executor == null ? 0 : executor.hashCode());
         result = prime * result + (file == null ? 0 : file.hashCode());
         result = prime * result + pid;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
 
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         Process other = (Process) obj;
         if (children == null) {
             if (other.children != null) {
                 return false;
             }
         } else if (!children.equals(other.children)) {
             return false;
         }
         if (executor == null) {
             if (other.executor != null) {
                 return false;
             }
         } else if (!executor.equals(other.executor)) {
             return false;
         }
         if (file == null) {
             if (other.file != null) {
                 return false;
             }
         } else if (!file.equals(other.file)) {
             return false;
         }
         if (pid != other.pid) {
             return false;
         }
         return true;
     }
 
     @Override
     public String toInfoString() {
 
        return "pid " + pid + " on " + host.getId() + ", source " + file.getGlobalPath(host) + ", controlled by " + executor.getClass().getName() + ", " + children.size() + " children";
     }
 
     @Override
     public String toString() {
 
         return getClass().getName() + " [" + toInfoString() + "]";
     }
 
 }
