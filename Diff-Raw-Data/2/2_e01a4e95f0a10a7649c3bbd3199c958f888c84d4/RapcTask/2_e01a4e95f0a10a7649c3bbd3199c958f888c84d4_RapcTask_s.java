 /*
  * Copyright 2007 Josh Kropf
  *
  * This file is part of bb-ant-tools.
  *
  * bb-ant-tools is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * bb-ant-tools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with bb-ant-tools; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package ca.slashdev.bb.tasks;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.lang.management.ManagementFactory;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.taskdefs.Execute;
 import org.apache.tools.ant.taskdefs.Java;
 import org.apache.tools.ant.types.Environment;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.Reference;
 import org.apache.tools.ant.util.FileUtils;
 
 import ca.slashdev.bb.types.JdpType;
 import ca.slashdev.bb.types.TypeAttribute;
 import ca.slashdev.bb.util.Utils;
 
 /**
  * @author josh
  */
 public class RapcTask extends BaseTask
 {
    private File jdkHome;
 
    private File destDir;
    private String output;
 
    private boolean quiet = true;
    private boolean verbose;
    private boolean nodebug;
    private boolean nowarn;
    private boolean warnerror;
    private boolean noconvert;
    private boolean nopreverify;
    private String encoding;
 
    private boolean generateSourceList = false;
    private String sourceListFile = "sources.txt";
 
    private Path srcs;
    private Path imports;
 
    private File exePath;
 
    private JdpType jdp = new JdpType();
 
    private String definesLine;
    private Vector<Define> defines = new Vector<Define>();
 
    /**
     * Represents a single preprocessor define.
     */
    public static class Define {
       private String tag;
       private String ifCond;
       private String unlessCond;
 
       public String getTag() {
          return tag;
       }
 
       public void setTag(String val) {
          tag = val;
       }
 
       /**
        * Sets the if attribute.  The define is evaluated when the property
        * named by this attribute is defined in the project.
        * @param ifCond
        */
       public void setIf(String ifCond) {
          this.ifCond = ifCond;
       }
 
       /**
        * Sets the unless attribute.  The define is evaluated when the property
        * named by this attribute is NOT defined in the project.
        * @param unlessCond
        */
       public void setUnless(String unlessCond) {
          this.unlessCond = unlessCond;
       }
 
       @Override
       public String toString() {
          return tag;
       }
 
       public boolean valid(Project p) {
          if (ifCond != null && p.getProperty(ifCond) == null) {
             return false;
          } else if (unlessCond != null && p.getProperty(unlessCond) != null) {
             return false;
          } else {
             return true;
          }
       }
    }
 
    @Override
    public void init() throws BuildException {
       srcs = new Path(getProject());
       imports = new Path(getProject());
       super.init();
    }
 
    /**
     * Sets the jde home directory and attempts to locate the rapc.jar file
     * and the net_rim_api.jar file.
     */
    @Override
    public void setJdeHome(File jdeHome) {
       super.setJdeHome(jdeHome);
    }
 
    /**
     * Sets the jdk (or jre) home directory of the jvm to use for launching
     * the rapc command.  Set this property if the rim jde requires an older
     * version of the jvm.
     * @param jdkHome jdk home directory
     */
    public void setJdkHome(File jdkHome) {
       File bin = new File(jdkHome, "bin");
       if (!bin.isDirectory()) {
          throw new BuildException("jdk home missing \"bin\" directory");
       }
 
       this.jdkHome = jdkHome;
    }
 
    /**
     * Sets the path for the preverify tool.
     */
    public void setExePath(File exePath) {
       this.exePath = exePath;
    }
 
    /**
     * Sets output name (eg: ca_slashdev_MyApp).  This name is used by
     * the rapc compiler to create various output files such as the .cod,.cso,
     * and .jar files.
     * @param output
     */
    public void setOutput(String output) {
       this.output = output;
    }
 
    /**
     * Tells the rapc compiler to be less chatty, default is true.
     * @param quiet
     */
    public void setQuiet(boolean quiet) {
       this.quiet = quiet;
    }
 
    /**
     * Turn on verbose output from rapc compiler, default is false.  The verbose
     * flag overrides the quiet flag.
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
       this.verbose = verbose;
    }
 
    /**
     * Disable generation of debug information, default is false.  Note: this
     * causes rapc to skip creating the .debug file and has no effect on the
     * final cod file.
     * @param nodebug
     */
    public void setNodebug(boolean nodebug) {
       this.nodebug = nodebug;
    }
 
    /**
     * Disable warning messages printed by rapc compiler, default is false.
     * @param nowarn
     */
    public void setNowarn(boolean nowarn) {
       this.nowarn = nowarn;
    }
 
    /**
     * Treat warnings as errors, default is false.
     * @param warnerror
     */
    public void setWarnerror(boolean warnerror) {
       this.warnerror = warnerror;
    }
 
    /**
     * Don't convert images to PNG, default is false.
     * @param noconvert
     */
    public void setNoconvert(boolean noconvert) {
       this.noconvert = noconvert;
    }
 
    /**
     * Don't call the preverifier, default is false.
     * @param nopreverify
     */
    public void setNopreverify(boolean nopreverify) {
       this.nopreverify = nopreverify;
    }
 
    /**
     * Set file encoding name for source files (eg: UTF-8 or windows-1252).
     * @param encoding
     */
    public void setEncoding(String encoding) {
       this.encoding = encoding;
    }
 
    /**
     * Generate file containing list of source files.  The file will contain
     * full path names of each source file separated by newline characters.
     * The result is passed to the rapc compiler to work around the command
     * line length limitation in Windows.  This flag is set to false by default.
     * @param generateSourceList true to generate source list
     * @see RapcTask#setSourceListFile(String)
     */
    public void setGenerateSourceList(boolean generateSourceList) {
       this.generateSourceList = generateSourceList;
    }
 
    /**
     * Sets name of source list file.  This file will be created in the
     * destination directory.  The default value is <code>sources.txt</code>.
     * @param sourceListFile source list file name
     * @see RapcTask#
     */
    public void setSourceListFile(String sourceListFile) {
       this.sourceListFile = sourceListFile;
    }
 
    /**
     * Sets working directory in which to run rapc compiler.  All output
     * files will be generated here.
     * @param destDir
     */
    public void setDestDir(File destDir) {
       this.destDir = destDir;
    }
 
    /**
     * Creates an implicit FileSet and adds it to the path of source files.
     * @param srcDir
     */
    public void setSrcDir(File srcDir) {
       FileSet srcFiles = new FileSet();
       srcFiles.setDir(srcDir);
       srcs.addFileset(srcFiles);
    }
 
    /**
     * Adds importPath to the path of import jars.
     * @param importPath
     */
    public void setImport(Path importPath) {
       imports.add(importPath);
    }
 
    /**
     * Adds to path of import jars by reference.  If the referenced object
     * is not a Path object, an exception is raised.
     * @param importRef
     */
    public void setImportRef(Reference importRef) {
       Object obj = importRef.getReferencedObject(getProject());
 
       if (!(obj instanceof Path)) {
          throw new BuildException("importref must be a path");
       }
 
       imports.add((Path)obj);
    }
 
    /**
     * Add srcPath to the path of source files.
     * @param srcPath
     */
    public void addSrc(Path srcPath) {
       srcs.add(srcPath);
    }
 
    /**
     * Add importPath to the path of import jars.
     * @param importPath
     */
    public void addImport(Path importPath) {
       imports.add(importPath);
    }
 
    /**
     * Sets the project settings object.  This task only supports one
     * nested &lt;jdp&gt; element.
     * @param jdp
     */
    public void addJdp(JdpType jdp) {
       this.jdp = jdp;
    }
 
    /**
     * Sets delimiter separated list of preprocessor defines.  The delimiter
     * is platform specific (semi-colon for Windows, colon for Unix).
     * @param defines delimiter separated list of defines
     */
    public void setDefines(String defines) {
       definesLine = defines;
    }
 
    /**
     * Add preprocessor define to collection of defines.
     * @param def preprocessor define to add
     */
    public void addDefine(Define def) {
       defines.add(def);
    }
 
    @Override
    public void execute() throws BuildException {
       super.execute();
 
       if (jdeHome == null) {
          throw new BuildException("jdehome not set");
       }
 
       File lib = new File(jdeHome, "lib");
       if (lib.isDirectory()) {
          Path apiPath = new Path(getProject());
          apiPath.setLocation(new File(lib, "net_rim_api.jar"));
          imports.add(apiPath);
       } else {
          throw new BuildException("jde home missing \"lib\" directory");
       }
 
       if (output == null) {
          throw new BuildException("output is a required attribute");
       }
 
       if (destDir == null) {
          destDir = getProject().getBaseDir();
       } else {
          if (!destDir.isDirectory()) {
             throw new BuildException("destdir must be a directory");
          }
       }
 
       if (srcs.size() == 0) {
          throw new BuildException("srcdir attribute or <src> element required!");
       }
 
       String[] files = srcs.list();
       srcs = new Path(getProject());
       File f;
 
       // iterate through all source files
       for (String file : files) {
          f = new File(file);
 
          // when source file is actually a directory, create fileset
          if (f.isDirectory()) {
             FileSet fs = new FileSet();
             fs.setDir(f);
             srcs.addFileset(fs);
          } else { // otherwise add the source file back into the path object
             srcs.setLocation(f);
          }
       }
 
       // blackberry jde will create this file and pass it to the rapc command
       jdp.writeManifest(new File(destDir, output+".rapc"), output);
 
       if (!Utils.isUpToDate(srcs, new File(destDir, output+".cod"))) {
          log(String.format("Compiling %d source files to %s", srcs.size(), output+".cod"));
          executeRapc();
       } else {
          log("Compilation skipped, cod is up to date", Project.MSG_VERBOSE);
       }
    }
 
    @SuppressWarnings("unchecked")
    protected void executeRapc() {
       File rapcJar;
       File bin = new File(jdeHome, "bin");
       if (bin.isDirectory()) {
          rapcJar = new File(bin, "rapc.jar");
       } else {
          throw new BuildException("jde home missing \"bin\" directory");
       }
 
       Java java = (Java)getProject().createTask("java");
 
       java.setTaskName(getTaskName());
       java.setClassname("net.rim.tools.compiler.Compiler");
 
       // must fork in order to set working directory and/or new environment
       java.setFork(true);
 
       // we want to fail if rapc returns non-zero
       java.setFailonerror(true);
 
       // loop through parent JVM arguments and add -X args to the java task
       // compiling large projects sometimes results in OutOfMemory exceptions
       // so the -Xmx vm argument needs to be propagated through to the rapc VM
       List<String> jvmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
       for (String arg : jvmArgs) {
          if (arg.startsWith("-X"))
             java.createJvmarg().setValue(arg);
       }
 
       // loop through the systems environment variables looking for PATH
       Vector<String> env = Execute.getProcEnvironment();
       for (String line : env) {
 
          // setup our own path variable
         if (line.toUpperCase().startsWith("PATH")) {
 
             // create new env variable using jde bin directory as the value
             Environment.Variable var = new Environment.Variable();
             var.setKey("PATH");
             var.setFile(new File(jdeHome, "bin"));
 
             // now add the systems current PATH value back into the new var
             var.setValue(String.format("%s%c%s",
                   line.substring(line.indexOf('=')+1),
                   File.pathSeparatorChar,
                   var.getValue()));
 
             java.addEnv(var);
 
             break;
          }
       }
 
       // Markus Winter posted a description of the reason for this on the
       // mailing list. Basically the rapc compiler will use tools.jar on
       // the classpath and compile .java files inside the VM. If tools.jar
       // is not on the classpath, it will use external javac command which
       // could result in "CreateProcess error=87, The parameter is incorrect"
       // malarkey on winblows.
       File toolsJar = null;
 
       if (jdkHome != null) {
          // jdk home was specified, set the jvm command
          java.setJvm(String.format("%s%c%s",
                new File(jdkHome, "bin").getAbsolutePath(),
                File.separatorChar, "java"));
 
          // assume when explicitly setting jdk home, it's in fact jdk not jre
          toolsJar = new File(jdkHome, "lib/tools.jar");
       } else {
           // we need the tools.jar file from the jdk
           // in a jdk the java.home points to the jre folder inside the jdk
           // normally so we check if the parent is called jre
           File javaHome = new File(System.getProperty("java.home"));
           if (javaHome.getName().equals("jre")) {
               toolsJar = new File(javaHome.getParentFile(), "lib/tools.jar");
           }
       }
 
       if (toolsJar != null && toolsJar.isFile()) {
           java.createClasspath().setLocation(toolsJar);
       }
 
       // directory from which command will be launched
       java.setDir(destDir);
 
       // add rapc jar file to classpath
       java.createClasspath().setLocation(rapcJar);
 
       if (verbose) java.createArg().setValue("-verbose");
       else if (quiet) java.createArg().setValue("-quiet");
 
       if (nodebug) java.createArg().setValue("-nodebug");
       if (nowarn) java.createArg().setValue("-nowarn");
       if (warnerror) java.createArg().setValue("-wx");
       if (noconvert) java.createArg().setValue("-noconvertpng");
       if (nopreverify) java.createArg().setValue("-nopreverified");
       if (encoding != null) java.createArg().setValue("-Dfile.encoding="+encoding);
 
       if (exePath != null)
          java.createArg().setValue("-exepath="+exePath.getAbsolutePath());
 
       if (definesLine != null || defines.size() > 0) {
          StringBuffer def = new StringBuffer();
 
          if (definesLine != null) {
             def.append(File.pathSeparatorChar).append(definesLine);
          }
 
          for (Define define : defines) {
             if (define.valid(getProject())) {
                def.append(File.pathSeparatorChar).append(define);
             }
          }
 
          String defs = def.toString();
          if (defs.length() > 0) {
             // defs contains Windows path separators on a Unix host
             if (defs.indexOf(';') != -1 && File.pathSeparatorChar == ':') {
                log("converting Windows style path separators to Unix style",
                      Project.MSG_WARN);
                defs = defs.replace(';', ':');
 
             // defs contains Unix path separators on a Windows host
             } else if (defs.indexOf(':') != -1 && File.pathSeparatorChar == ';') {
                log("converting Unix style path separators to Windows style",
                      Project.MSG_WARN);
                defs = defs.replace(':', ';');
             }
          }
 
          java.createArg().setValue("-define=PREPROCESSOR" + defs);
       }
 
       java.createArg().setValue("import="+imports.toString());
 
       String type = jdp.getType().getValue();
       if (TypeAttribute.MIDLET.equals(type)) {
          java.createArg().setValue("codename="+output);
          java.createArg().setValue("-midlet");
       } else if (TypeAttribute.CLDC.equals(type)) {
          java.createArg().setValue("codename="+output);
       } else if (TypeAttribute.LIBRARY.equals(type)) {
          java.createArg().setValue("library="+output);
       }
 
       // manifest file is last parameter before file list
       java.createArg().setValue(output+".rapc");
 
       // when true, generate text file containing list of source files
       if (generateSourceList) {
          PrintStream output = null;
          try {
             output = new PrintStream(new File(destDir, sourceListFile));
 
             for (String file : srcs.list()) {
                // full path of each file, followed by newline character
                output.println(file);
             }
 
             // rapc command expects parameter to begin with '@'
             java.createArg().setValue("@" + sourceListFile);
          } catch (Exception e) {
             throw new BuildException("error creating source list file", e);
          } finally {
             FileUtils.close(output);
          }
       } else {
          // add each of the items in the srcs path as file args
          for (String file : srcs.list()) {
             java.createArg().setFile(new File(file));
          }
       }
 
       log(java.getCommandLine().toString(), Project.MSG_DEBUG);
       java.execute();
    }
 }
