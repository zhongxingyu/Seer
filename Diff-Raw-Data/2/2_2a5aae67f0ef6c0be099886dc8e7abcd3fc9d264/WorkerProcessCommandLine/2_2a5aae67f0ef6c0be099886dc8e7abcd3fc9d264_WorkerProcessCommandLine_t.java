 // Copyright (C) 2004 - 2011 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.engine.agent;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import net.grinder.common.GrinderProperties;
 import net.grinder.engine.common.EngineException;
 import net.grinder.engine.process.WorkerProcessEntryPoint;
 import net.grinder.util.Directory;
 
 
 /**
  * Builds the worker process command line.
  *
  * @author Philip Aston
  */
 final class WorkerProcessCommandLine implements CommandLine {
 
   private static final String AGENT_JAR_FILENAME_PREFIX = "grinder-dcr-agent";
 
   private final Directory m_workingDirectory;
   private final List<String> m_command;
   private final int m_commandClassIndex;
 
   public WorkerProcessCommandLine(GrinderProperties properties,
                                   Properties systemProperties,
                                   String jvmArguments,
                                   Directory workingDirectory)
     throws EngineException {
 
     m_workingDirectory = workingDirectory;
     m_command = new ArrayList<String>();
     m_command.add(properties.getProperty("grinder.jvm", "java"));
 
     final String systemClasspath =
       systemProperties.getProperty("java.class.path");
 
     if (systemClasspath != null) {
       final File agent = findAgentJarFile(systemClasspath);
 
       if (agent != null) {
         try {
           m_command.add("-javaagent:" +
                         workingDirectory.rebaseFile(agent));
         }
         catch (IOException e) {
           throw new EngineException(e.getMessage(), e);
         }
       }
     }
 
     if (jvmArguments != null) {
       // Really should allow whitespace to be escaped/quoted.
       final StringTokenizer tokenizer = new StringTokenizer(jvmArguments);
 
       while (tokenizer.hasMoreTokens()) {
         final String token = tokenizer.nextToken();
 
         m_command.add(token);
       }
     }
 
     final String additionalClasspath =
       properties.getProperty("grinder.jvm.classpath");
 
     final StringBuilder classpath = new StringBuilder();
 
     if (additionalClasspath != null) {
       classpath.append(additionalClasspath);
     }
 
     if (systemClasspath != null) {
       if (classpath.length() > 0) {
         classpath.append(File.pathSeparatorChar);
       }
 
       classpath.append(systemClasspath);
     }
 
     if (classpath.length() > 0) {
       m_command.add("-classpath");
 
       try {
         m_command.add(workingDirectory.rebasePath(classpath.toString()));
       }
       catch (IOException e) {
         throw new EngineException(e.getMessage(), e);
       }
     }
 
     m_commandClassIndex = m_command.size();
     m_command.add(WorkerProcessEntryPoint.class.getName());
   }
 
   /**
    * {@inheritDoc}
    */
   @Override public Directory getWorkingDirectory() {
     return m_workingDirectory;
   }
 
   /**
    * {@inheritDoc}
    */
  @Override public List<String> getCommandList() {
     return m_command;
   }
 
   private static final Set<String> s_unquoted = new HashSet<String>() { {
       add("-classpath");
       add("-client");
       add("-cp");
       add("-jar");
       add("-server");
     } };
 
   public String toString() {
     final String[] commandArray = getCommandList().toArray(new String[0]);
 
     final StringBuilder buffer = new StringBuilder(commandArray.length * 10);
 
     for (int j = 0; j < commandArray.length; ++j) {
       if (j != 0) {
         buffer.append(" ");
       }
 
       final boolean shouldQuote =
         j != 0 &&
         j != m_commandClassIndex &&
         !s_unquoted.contains(commandArray[j]);
 
       if (shouldQuote) {
         buffer.append("'");
       }
 
       buffer.append(commandArray[j]);
 
       if (shouldQuote) {
         buffer.append("'");
       }
     }
 
     return buffer.toString();
   }
 
   /**
    * Package scope for unit tests.
    *
    * @param path The path to search.
    */
   static File findAgentJarFile(String path) {
     for (String pathEntry : path.split(File.pathSeparator)) {
       final File f = new File(pathEntry).getParentFile();
       final File parentFile = f != null ? f : new File(".");
 
       for (File candidate : parentFile.listFiles()) {
         if (candidate.getName().startsWith(AGENT_JAR_FILENAME_PREFIX)) {
           return candidate;
         }
       }
     }
 
     return null;
   }
 }
