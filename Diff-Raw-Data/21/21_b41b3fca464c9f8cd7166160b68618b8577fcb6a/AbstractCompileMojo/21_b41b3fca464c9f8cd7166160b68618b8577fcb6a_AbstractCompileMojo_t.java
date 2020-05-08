 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.tools.ant.Project;
 
 /**
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/AbstractCompileMojo.java 6ae92433be9e 2007/07/18 17:45:27 duns $
  */
 public abstract class AbstractCompileMojo extends AbstractDependencyMojo {
 
     /**
      * C++ Compiler
      *
      * @parameter expression=""
      */
     private Cpp cpp;
 
     /**
      * C Compiler
      *
      * @parameter expression=""
      */
     private C c;
 
     /**
      * Fortran Compiler
      *
      * @parameter expression=""
      */
     private Fortran fortran;
 	
     /**
     * Maximum number of Cores/CPU's to use. 0 means unlimited.
     * 
     * @parameter expression=""
     */
    protected int maxCores = 0;
    
    /**
      * Name of the output
      *
      * @parameter expression="${project.artifactId}-${project.version}"
      */
     private String output;
     
     /**
      * Fail on compilation/linking error.
      *
      * @parameter expression="" default-value="true"
      * @required
      */
     private boolean failOnError;
 
     /**
      * Sets the type of runtime library, possible values "dynamic", "static".
      *
      * @parameter expression="" default-value="dynamic"
      * @required
      */
     private String runtime;
 
     /**
      * Set use of libtool. If set to true, the "libtool " will be prepended to the command line for compatible processors.
      *
      * @parameter expression="" default-value="false"
      * @required
      */
     private boolean libtool;
 
     /**
      * The home of the Java system.
      * Defaults to a derived value from ${java.home} which is OS specific.
      *
      * @parameter expression=""
      * @readonly
      */
     private File javaHome;
      
     /**
      * List of libraries to create
      *
      * @parameter expression=""
      */
     private List libraries;
 
     /**
      * List of tests to create
      *
      * @parameter expression=""
      */
     private List tests;
     
     /**
      * Javah info
      *
      * @parameter expression=""
      */
     private Javah javah;
     
     /**
      * Java info for includes and linking
      *
      * @parameter expression=""
      */
     private Java java;
     
     private Project antProject;
 
     protected Project getAntProject() {
         if (antProject == null) {
             // configure ant project
             antProject = new Project();
             antProject.setName("NARProject");
             antProject.addBuildListener(new NarLogger(getLog()));
         }
         return antProject;
     }
 
     protected C getC() {
         if (c == null) c = new C();
         c.setAbstractCompileMojo(this);
         return c;
     }
    
     protected Cpp getCpp() {
         if (cpp == null) cpp = new Cpp();
         cpp.setAbstractCompileMojo(this);
         return cpp;
     }
    
     protected Fortran getFortran() {
         if (fortran == null) fortran = new Fortran();
         fortran.setAbstractCompileMojo(this);
         return fortran;
     }
 
     protected boolean useLibtool() {
         return libtool;
     }
     
     protected boolean failOnError() {
         return failOnError;
     }
     
     protected String getRuntime() {
         return runtime;
     }
     
     protected String getOutput() {
         return output;
     }
 
     protected File getJavaHome() {
     	javaHome = NarUtil.getJavaHome(javaHome, getOS());    	
     	return javaHome;       
     }
 
     protected List getLibraries() {
         if (libraries == null) libraries = Collections.EMPTY_LIST;
         return libraries;
     }
    
     protected List getTests() {
         if (tests == null) tests = Collections.EMPTY_LIST;
         return tests;
     }
 
     protected Javah getJavah() {
         if (javah == null) javah = new Javah();
         javah.setAbstractCompileMojo(this);
         return javah;
     }
           
     protected Java getJava() {
         if (java == null) java = new Java();
         java.setAbstractCompileMojo(this);
         return java;
     }
 }
