 package org.rubyforge.debugcommons;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import org.rubyforge.debugcommons.RubyDebuggerProxy.DebuggerType;
 import org.rubyforge.debugcommons.model.RubyDebugTarget;
 
 import static org.rubyforge.debugcommons.RubyDebuggerProxy.CLASSIC_DEBUGGER;
 import static org.rubyforge.debugcommons.RubyDebuggerProxy.RUBY_DEBUG;
 
 public final class RubyDebuggerFactory {
     
     private static final String CLASSIC_DEBUG_NAME = "classic-debug.rb";
     private static final String CLASSIC_VERBOSE_DEBUG_NAME = "classic-debug-verbose.rb";
     
     private static final int DEFAULT_PORT = 1098;
     
     /**
      * Starts classic-debugger session for the given script. Debugger waits on
      * the first script's line.
      *
      * @param descriptor {@link Descriptor} to be used
      * @param pathToClassicDebugDir directory containing classic-debug.rb and
      *        classic-debug-verbose.rb.
      * @param interpreter interpreter to be used
      * @return {@link RubyDebugTarget} instance
      * @throws java.io.IOException
      * @throws org.rubyforge.debugcommons.RubyDebuggerException
      */
     public static RubyDebuggerProxy startClassicDebugger(
             final Descriptor descriptor,
             final String pathToClassicDebugDir,
             final String interpreter,
             final int timeout)
             throws IOException, RubyDebuggerException {
         descriptor.setType(CLASSIC_DEBUGGER);
         List<String> args = new ArrayList<String>();
         args.add(interpreter);
         args.addAll(descriptor.getAddtionalOptions());
         args.add("-I");
         args.add(pathToClassicDebugDir);
         if (!descriptor.defaultPortUsed()) {
             if (descriptor.getPort() != -1) {
                 try {
                     String path = createRemoteDebugPortFile(descriptor.getPort());
                     args.add("-r");
                     args.add(path);
                 } catch (IOException e) {
                     Util.severe("Could not create 'RemoteDebugPortFile'. Using default port.", e);
                 }
             }
         }
         appendIOSynchronizer(args, descriptor);
         args.add("-r");
         args.add(descriptor.isVerbose() ? CLASSIC_VERBOSE_DEBUG_NAME : CLASSIC_DEBUG_NAME);
         args.add(descriptor.getScriptPath());
         if (descriptor.getScriptArguments() != null) {
             args.addAll(Arrays.asList(descriptor.getScriptArguments()));
         }
         return startDebugger(descriptor, args, timeout);
     }
 
     /**
      * The same as {@link #startClassicDebugger(Descriptor, String, String, int)}.
      */
     public static RubyDebuggerProxy startClassicDebugger(
             final Descriptor descriptor,
             final String pathToClassicDebugDir,
             final String interpreter) throws IOException, RubyDebuggerException {
         return startClassicDebugger(descriptor, pathToClassicDebugDir, interpreter, 10);
     }
 
     /**
      * Delegates to {@link #startRubyDebug(Descriptor, String, String)} with
      * <code>null</code> for <code>interpreter</code> parameter.
      */
     public static RubyDebuggerProxy startRubyDebug(final Descriptor descriptor,
             final String rdebugExecutable, final int timeout) throws IOException, RubyDebuggerException {
         return startRubyDebug(descriptor, rdebugExecutable, null, timeout);
     }
     
     /**
      * Starts Kent Sibilev's ruby-debug session for the given script. Debugger
      * waits on the first script's line.
      *
      * @param descriptor {@link Descriptor} to be used
      * @param rdebugExecutable path to rdebug-ide
      * @param interpreter interpreter to be used for running
      *        <code>rdebugExecutable</code>. If <code>null</code>
      *        <code>rdebugExecutable</code> will be run directly using default
      *        system interpreter.
      * @return {@link RubyDebugTarget} instance
      * @throws java.io.IOException
      * @throws org.rubyforge.debugcommons.RubyDebuggerException
      */
     public static RubyDebuggerProxy startRubyDebug(
             final Descriptor descriptor,
             final String rdebugExecutable,
             final String interpreter,
             final int timeout) throws IOException, RubyDebuggerException {
         descriptor.setType(RUBY_DEBUG);
         List<String> args = new ArrayList<String>();
         if (interpreter != null) {
             args.add(interpreter);
             appendIOSynchronizer(args, descriptor);
         }
        args.addAll(descriptor.getAddtionalOptions());
         args.add(rdebugExecutable);
         args.add("-p");
         args.add(String.valueOf(descriptor.getPort()));
         if (descriptor.isVerbose()) {
             args.add("-d");
         }
         args.add("--");
         args.add(descriptor.getScriptPath());
         if (descriptor.getScriptArguments() != null) {
             args.addAll(Arrays.asList(descriptor.getScriptArguments()));
         }
         return startDebugger(descriptor, args, timeout);
     }
 
     private static RubyDebuggerProxy startDebugger(final Descriptor desc, final List<String> args, final int timeout)
             throws IOException, RubyDebuggerException {
         Util.fine("Running [basedir: " + desc.getBaseDirectory() + "]: \"" + getProcessAsString(args) + "\"");
         ProcessBuilder pb = new ProcessBuilder(args);
         pb.directory(desc.getBaseDirectory());
         if (desc.getEnvironment() != null) {
             pb.environment().putAll(desc.getEnvironment());
         }
         RubyDebuggerProxy proxy = new RubyDebuggerProxy(desc.getType(), timeout);
         RubyDebugTarget target = new RubyDebugTarget(proxy, pb.start(),
                 desc.getPort(), desc.getScriptPath(), desc.getBaseDirectory());
         proxy.connect(target);
         RubyDebuggerProxy.PROXIES.add(proxy);
         return proxy;
     }
     
     private static String createRemoteDebugPortFile(final int port) throws IOException {
         File debugParameterFile = File.createTempFile("classic-debug", ".rb");
         debugParameterFile.deleteOnExit();
         FileWriter fWriter = new FileWriter(debugParameterFile);
         new PrintWriter(fWriter).println("$RemoteDebugPort=" + port);
         fWriter.close();
         return debugParameterFile.getCanonicalPath();
     }
     
     private static String createIOSynchronizer() throws IOException {
         File ioSynchronizer = File.createTempFile("io-synchronizer", ".rb");
         ioSynchronizer.deleteOnExit();
         FileWriter fWriter = new FileWriter(ioSynchronizer);
         PrintWriter pwWriter = new PrintWriter(fWriter);
         pwWriter.println("$stdout.sync=true");
         pwWriter.println("$stderr.sync=true");
         fWriter.close();
         return ioSynchronizer.getCanonicalPath();
     }
 
     private static void appendIOSynchronizer(final List<? super String> args, final Descriptor descriptor) throws IOException {
         if (descriptor.isSynchronizedOutput()) {
             String path = createIOSynchronizer();
             args.add("-r");
             args.add(path);
         }
     }
 
     /** Describes a debugger session. */
     public static final class Descriptor {
         
         private int coputedPort = -1;
         
         private DebuggerType type;
         private boolean verbose;
         private boolean useDefaultPort;
         private String scriptPath;
         private File baseDir;
         private String[] scriptArguments;
         private Map<String, String> environment;
         private boolean synchronizedOutput;
         private Collection<? extends String> additionalOptions;
         
         public DebuggerType getType() {
             return type;
         }
 
         public void setType(DebuggerType type) {
             this.type = type;
         }
         
         public boolean isVerbose() {
             return verbose;
         }
         
         public void setVerbose(boolean verbose) {
             this.verbose = verbose;
         }
         
         public boolean defaultPortUsed() {
             return useDefaultPort;
         }
         
         public void useDefaultPort(boolean useDefaultPort) {
             this.useDefaultPort = useDefaultPort;
         }
         
         public String getScriptPath() {
             return scriptPath;
         }
         
         /**
          * @param scriptPath script to be debugged
          */
         public void setScriptPath(String scriptPath) {
             this.scriptPath = scriptPath;
         }
         
         /** @see #getBaseDirectory */
         public void setBaseDirectory(File baseDir) {
             this.baseDir = baseDir;
         }
         
         /**
          * Returns directory to be used as a base directory of the process. If
          * it was not {@link #setBaseDirectory set} or is <code>null</code>,
          * script's parent directory is used.
          */
         public File getBaseDirectory() {
             return baseDir != null ? baseDir : new File(getScriptPath()).getParentFile();
         }
         
         public String[] getScriptArguments() {
             return scriptArguments;
         }
         
         /**
          * @param scriptArguments scriptArguments arguments for the debugged script.
          */
         public void setScriptArguments(String[] scriptArguments) {
             this.scriptArguments = scriptArguments;
         }
         
         public Map<String, String> getEnvironment() {
             return environment;
         }
 
         public void setEnvironment(final Map<String, String> environment) {
             this.environment = environment;
         }
         
         public boolean isSynchronizedOutput() {
             return synchronizedOutput;
         }
         
         public void setSynchronizedOutput(boolean synchronizedOutput) {
             this.synchronizedOutput = synchronizedOutput;
         }
         
         public Collection<? extends String> getAddtionalOptions() {
             return additionalOptions == null ? Collections.<String>emptySet() : additionalOptions;
         }
         
         public void setAdditionalOptions(Collection<? extends String> additionalOptions) {
             this.additionalOptions = additionalOptions;
         }
         
         int getPort() {
             if (coputedPort == -1) {
                 coputedPort = defaultPortUsed() ? DEFAULT_PORT : Util.findFreePort();
             }
             return coputedPort;
         }
         
     }
     
     /** Just helper method for logging. */
     private static String getProcessAsString(List<? extends String> process) {
         StringBuilder sb = new StringBuilder();
         for (String arg : process) {
             sb.append(arg).append(' ');
         }
         return sb.toString().trim();
     }
     
 }
