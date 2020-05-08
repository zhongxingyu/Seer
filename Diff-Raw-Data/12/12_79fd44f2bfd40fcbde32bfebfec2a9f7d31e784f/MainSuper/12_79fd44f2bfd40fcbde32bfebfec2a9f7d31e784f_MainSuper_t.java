 /*
  * Copyright (C) 1990-2001 DMS Decision Management Systems Ges.m.b.H.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
 * $Id: MainSuper.java,v 1.3 2004-02-09 16:04:40 ostermann Exp $
  */
 
 package org.caesarj.compiler;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Vector;
 
 import org.caesarj.classfile.ClassFileFormatException;
 import org.caesarj.compiler.ast.JCompilationUnit;
 import org.caesarj.compiler.ast.KjcPrettyPrinter;
 import org.caesarj.compiler.constants.CaesarMessages;
 import org.caesarj.compiler.constants.KjcMessages;
 import org.caesarj.compiler.export.CSourceClass;
 import org.caesarj.compiler.optimize.BytecodeOptimizer;
 import org.caesarj.compiler.types.CStdType;
 import org.caesarj.compiler.types.KjcSignatureParser;
 import org.caesarj.compiler.types.KjcTypeFactory;
 import org.caesarj.compiler.types.TypeFactory;
 import org.caesarj.tools.antlr.extra.CompilerBase;
 import org.caesarj.util.CWarning;
 import org.caesarj.util.InconsistencyException;
 import org.caesarj.util.Messages;
 import org.caesarj.util.PositionedError;
 import org.caesarj.util.TokenReference;
 import org.caesarj.util.UnpositionedError;
 import org.caesarj.util.Utils;
 
 /**
  * This class implements the entry point of the Java compiler
  */
 public abstract class MainSuper extends CompilerBase {
 
   // ----------------------------------------------------------------------
   // ENTRY POINT
   // ----------------------------------------------------------------------
 
 
   /**
    * Creates a new compiler instance.
    *
    * @param	workingDirectory	the working directory
    * @param	diagnosticOutput	the diagnostic output stream
    */
   public MainSuper(String workingDirectory, PrintWriter diagnosticOutput) {
     super(workingDirectory, diagnosticOutput);
   }
 
   // --------------------------------------------------------------------
   // Language
   // --------------------------------------------------------------------
   
   /**
    * Sets the version of the source code 
    * 
    * @param     version        version  of the source code
    */
   public void setSourceVersion(int version) {
     this.version = version;
   }
 
   /**
    * Returns the version of the source code
    * 
    * @return     version of the code
    */
   public int getSourceVersion() {
     return this.version;
   }
  // ----------------------------------------------------------------------
   // RUN FROM COMMAND LINE
   // ----------------------------------------------------------------------
 
   /**
    * Runs a compilation session
    *
    * @param	args		the command line arguments
    */
   public abstract boolean run(String[] args);
 
   protected KjcEnvironment createEnvironment(KjcOptions options) {
     KjcClassReader      reader = new KjcClassReader(options.classpath, 
                                                     options.extdirs,
                                                     new KjcSignatureParser());
     return  new KjcEnvironment(reader, 
                                new KjcTypeFactory(reader, options.generic),
                                options);
   }
 
   /**
    * Parse the argument list
    */
   public boolean parseArguments(String[] args) {
     options = new KjcOptions();
     if (!options.parseCommandLine(args)) {
       return false;
     }
     infiles = Utils.toVector(options.nonOptions);
     return true;
   }
 
   /**
    * Generates the code from an array of compilation unit and
    * a destination
    *
    * @param	destination	the directory where to write classfiles
    */
   public void genCode(TypeFactory factory) {
     CSourceClass[]	classes = getClasses();
     BytecodeOptimizer	optimizer = new BytecodeOptimizer(options.optimize);
 
     this.classes.setSize(0);
 
     try {
 	for (int count = 0; count < classes.length; count++) {
 	  long		lastTime = System.currentTimeMillis();
 
 	  classes[count].genCode(optimizer, options.destination, factory);
 	  if (verboseMode() && !classes[count].isNested()) {
 	    inform(CaesarMessages.CLASSFILE_GENERATED,
 		   classes[count].getQualifiedName().replace('/', '.'),
 		   new Long(System.currentTimeMillis() - lastTime));
 	  }
 	  classes[count] = null;
         }
     } catch (PositionedError e) {
       reportTrouble(e);
     } catch (ClassFileFormatException e) {
       e.printStackTrace();
       reportTrouble(new UnpositionedError(Messages.FORMATTED_ERROR, e.getMessage()));
     } catch (IOException e) {
       reportTrouble(new UnpositionedError(Messages.IO_EXCEPTION,
 					  "classfile",	//!!!FIXME !!!
 					  e.getMessage()));
     }
   }
 
   /**
    * Initialize the compiler (read classpath, check classes.zip)
    */
   protected void initialize(KjcEnvironment environment) {
     //    ClassPath.init(options.classpath);
         CStdType.init(this, environment);
   }
 
   /**
    * returns true iff compilation in verbose mode is requested.
    */
   public boolean verboseMode() {
     return options.verbose;
   }
 
   // ----------------------------------------------------------------------
   // PROTECTED METHODS
   // ----------------------------------------------------------------------
 
   /**
    * parse the givven file and return a compilation unit
    * side effect: increment error number
    * @param	file		the name of the file (assert exists)
    * @return	the compilation unit defined by this file
    */
   protected abstract JCompilationUnit parseFile(File file, KjcEnvironment environment);
   /**
    * creates the class hierarchie (superclass, interfaces, ...)
    * @param	cunit		the compilation unit
    */
  public void join(JCompilationUnit cunit) {
     try {
       cunit.join(this);
     } catch (PositionedError e) {
       reportTrouble(e);
     }
   }
 
   /**
    * check that interface of a given compilation unit is correct
    * side effect: increment error number
    * @param	cunit		the compilation unit
    */
   protected void checkInterface(JCompilationUnit cunit) {
     try {
       cunit.checkInterface(this);
     } catch (PositionedError e) {
       reportTrouble(e);
     }
   }
 
   /**
    * check that interface of a given compilation unit is correct
    * side effect: increment error number
    * @param	cunit		the compilation unit
    */
   protected void checkInitializers(JCompilationUnit cunit) {
     try {
       cunit.checkInitializers(this, classes);
     } catch (PositionedError e) {
       reportTrouble(e);
     }
   }
 
   /**
    * check that body of a given compilation unit is correct
    * side effect: increment error number
    * @param	cunit		the compilation unit
    */
   protected void checkBody(JCompilationUnit cunit) {
     long	lastTime = System.currentTimeMillis();
 
     try {
       cunit.checkBody(this, classes);
     } catch (PositionedError e) {
       reportTrouble(e);
     }
 
     if (verboseMode()) {
       inform(CaesarMessages.BODY_CHECKED, cunit.getFileName(), new Long(System.currentTimeMillis() - lastTime));
     }
   }
   /**
    * check the conditions
    * side effect: increment error number
    * @param	cunit		the compilation unit
    */
   protected void checkCondition(JCompilationUnit cunit) {
     long	lastTime = System.currentTimeMillis();
 
     try {
       cunit.analyseConditions();
     } catch (PositionedError e) {
       reportTrouble(e);
     }
 
     if (verboseMode()) {
       inform(CaesarMessages.CONDITION_CHECKED, cunit.getFileName(), new Long(System.currentTimeMillis() - lastTime));
     }
   }
 
   /**
    * generate the source code of parsed compilation unit
    * @param	cunit		the compilation unit
    */
   protected void generateJavaCode(final JCompilationUnit cunit,
                                   final TypeFactory factory)
   {
     final long                  lastTime = System.currentTimeMillis();
     final String                fileName;
 
     if (options.destination == null || options.destination.equals("")) {
       fileName = cunit.getTokenReference().getName() + ".gen";
     } else {
       fileName = options.destination + File.separatorChar + cunit.getTokenReference().getName();
     }
 
     try {
       final KjcPrettyPrinter      pp;
 
       pp = getPrettyPrinter(fileName, factory);
 
       cunit.accept(pp);
       pp.close();
     } catch (IOException ioe) {
       ioe.printStackTrace();
       System.err.println("cannot write: " + fileName);
     }
 
     if (verboseMode()) {
       inform(CaesarMessages.JAVA_CODE_GENERATED,
              cunit.getFileName(),
              new Long(System.currentTimeMillis() - lastTime));
     }
   }
 
 
   // --------------------------------------------------------------------
   // COMPILER
   // --------------------------------------------------------------------
 
   /**
    * Reports a trouble (error or warning).
    *
    * @param	trouble		a description of the trouble to report.
    */
   public void reportTrouble(PositionedError trouble) {
     if (trouble instanceof CWarning) {
       if (options.warning != 0 && filterWarning((CWarning)trouble)) {
 	inform(trouble);
       }
     } else {
       if (trouble.getTokenReference() != TokenReference.NO_REF) {
 	inform(trouble);
 	errorFound = true;
       } else {
 	inform(trouble);
       }
     }
   }
 
   /**
    * Reports a trouble.
    *
    * @param	trouble		a description of the trouble to report.
    */
   public void reportTrouble(UnpositionedError trouble) {
     inform(trouble);
     errorFound = true;
   }
 
   protected boolean filterWarning(CWarning warning) {
     WarningFilter	filter = getFilter();
     int			value = filter.filter(warning);
 
     switch (value) {
     case WarningFilter.FLT_REJECT:
       return false;
     case WarningFilter.FLT_FORCE:
       return true;
     case WarningFilter.FLT_ACCEPT:
       return warning.getSeverityLevel() <= options.warning;
     default:
       throw new InconsistencyException();
     }
   }
 
   protected WarningFilter getFilter() {
     if (filter == null) {
       if (options.filter != null) {
         try {
           filter = (WarningFilter)Class.forName(options.filter).newInstance();
         } catch (Exception e) {
           inform(KjcMessages.FILTER_NOT_FOUND, options.filter);
         }
       }
       if (filter == null) {
         filter = new DefaultFilter();
       }
     }
 
     return filter;
   }
 
   /**
    * Returns true iff comments should be parsed (false if to be skipped)
    */
   public boolean parseComments() {
     return options.deprecation || (options.beautify && !options.nowrite);
   }
 
   /**
    * Returns the classes to generate
    */
   public CSourceClass[] getClasses() {
     return (CSourceClass[])org.caesarj.util.Utils.toArray(classes, CSourceClass.class);
   }
 
   /**
    * This method has to be overridden in the sub-classes.
    *
    * @return the corresponding PrettyPrinter
    */
   protected KjcPrettyPrinter getPrettyPrinter(String fileName,
                                               TypeFactory factory)
     throws IOException 
   {
     return new KjcPrettyPrinter(fileName, factory);
   }
   
   // ----------------------------------------------------------------------
   // PROTECTED DATA MEMBERS
   // ----------------------------------------------------------------------
 
   protected Vector		infiles = new Vector();
   protected boolean		errorFound;
 
   protected KjcOptions		options;
   private int			version = 0;
 
   // it must be initialized to null otherwise the filter option is not used
   private WarningFilter		filter = null;
 
   // all generated classes
   protected Vector		classes = new Vector(100);
 }
 
