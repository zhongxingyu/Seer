 /*
  * $ Id $
  * (c) Copyright 2009 Marcus Thiesen (marcus@thiesen.org)
  *
  *  This file is part of ecj4ant.
  *
  *  ecj4ant is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  ecj4ant is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with ecj4ant.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package org.thiesen.ecj4ant;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
import java.util.Map.Entry;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.Reference;
 import org.eclipse.jdt.core.compiler.CategorizedProblem;
 import org.eclipse.jdt.internal.compiler.Compiler;
 import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
 import org.eclipse.jdt.internal.compiler.IProblemFactory;
 import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
 import org.eclipse.jdt.internal.compiler.batch.FileSystem;
 import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
 import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
 import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
 import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
 import org.eclipse.jdt.internal.compiler.util.Util;
 
 public class EcjTask extends Task {
 
     private static final String DEFAULT_ENCODING = "utf-8";
 
     private final List<FileSet> _fileSets;
 
     private String _failOnError;
     private String _config;
     
     private Path _path;
     
     private String _maxWarnings;
     private String _maxErrors;
     
     private boolean _suppressWarnings;
     private boolean _suppressErrors;
     private boolean _suppressInfos;
     
     
     public String getMaxWarnings() {
         return _maxWarnings;
     }
 
     public void setMaxWarnings( final String maxWarnings ) {
         _maxWarnings = maxWarnings;
     }
 
     public String getMaxErrors() {
         return _maxErrors;
     }
 
     public void setMaxErrors( final String maxErrors ) {
         _maxErrors = maxErrors;
     }
 
     private final Classpath[] _baseClasspath;
 
     public EcjTask() {
         _fileSets = new ArrayList<FileSet>();
 
         final CustomBatchCompiler batchCompilerMain = new CustomBatchCompiler( getOutWriter(), getErrWriter(), false, null, null );
         batchCompilerMain.initPaths();
         _baseClasspath = batchCompilerMain.getCheckedClasspaths();
 
         log( "Ecj4Ant - 2009 by Marcus Thiesen (marcus@thiesen.org)" );
         log( "Using " + batchCompilerMain.bind("compiler.name") + " " +  
                 batchCompilerMain.bind("compiler.version") +  " " +
                 batchCompilerMain.bind("compiler.copyright" ) );
     }
 
     @Override
     public void execute() {
         final long startTime = System.currentTimeMillis();
         
         final List<String> sourceFilenames = getSourceFilenames();
         final List<String> classPathEntries = getClassPathEntries();
 
         final FileSystem environment = getLibraryAccess( sourceFilenames, classPathEntries );
 
         log( "Checking " + sourceFilenames.size() + " source files");
 
         final CompilerOptions compilerOptions = makeCompilerOptions();
 
         log( compilerOptions.toString(), Project.MSG_VERBOSE );
         
         final BatchRequestor requestor = getBatchRequestor();
 
         final Compiler batchCompiler =
             new Compiler(
                     environment,
                     getHandlingPolicy(),
                     compilerOptions,
                     requestor,
                     getProblemFactory(),
                     getOutputWrapper(),
                     null );
 
         compilerOptions.verbose = false;
 
         batchCompiler.compile( getCompilationUnits( sourceFilenames ) );
 
         // cleanup
         environment.cleanup();
 
 
         final long endTime = System.currentTimeMillis() - startTime;
         
         log("Compile finished after " + endTime + "ms with " + requestor.getErrors() + " errors, " + requestor.getWarnings() + " warnings and " + requestor.getInfos() + " info messages" );
 
         checkFailErrors( requestor.getErrors() );
         checkFailWarnings( requestor.getWarnings() );
     }
     
     private void checkFailWarnings( final int warnings ) {
         final String maxWarnings = getMaxWarnings();
         checkConfigOption( warnings, maxWarnings, "More warnings than specified max warnings"  );
     }
 
     private void checkFailErrors( final int errors ) {
         final String maxErrors = getMaxErrors();
         checkConfigOption( errors, maxErrors, "More errors than specified max errors"  );
     }
 
     private void checkConfigOption( final int actualValue, final String configuredMaximumValue, final String message ) {
         if ( !"".equals( configuredMaximumValue ) && configuredMaximumValue != null ) {
             if ( Integer.parseInt( configuredMaximumValue ) < actualValue ) {
                 throw new BuildException( message  );
             }
         }
     }
 
     private CompilerOptions makeCompilerOptions() {
        final Map<String, String> compilerOptions = new HashMap<String, String>( loadConfigWithDefault() );
         
         final CompilerOptions retval = new CompilerOptions( compilerOptions );
 
         retval.performMethodsFullRecovery = false;
         retval.performStatementsRecovery = false;
         
         retval.sourceLevel = ClassFileConstants.JDK1_6;
         retval.complianceLevel = ClassFileConstants.JDK1_6;
         
         return retval;
     }
 
 
    private Map<String,String> loadConfigWithDefault() {
         final String filename = getConfig();
         
         if ( filename != null ) {
             
             final Properties p = new Properties();
             
             try {
                 p.load( new FileInputStream( filename ) );
             
                final Map<String,String> returnMap = new HashMap<String,String>();
                for ( final Entry<Object, Object> entry : p.entrySet() ) {
                    returnMap.put( (String)entry.getKey(), (String)entry.getValue() );
                }
             
                return returnMap;
             } catch ( final FileNotFoundException e ) {
                 throw new BuildException( e ); 
             
             } catch ( final IOException e ) {
                 throw new BuildException( e );
             }
          
             
         }
         
        return Options.saneOptions();
     }
 
     private List<String> getClassPathEntries() {
         return Arrays.asList( _path.toString().split( ":" ) );
     }
 
 
     private List<String> getSourceFilenames() {
         final List<String> filenames = new LinkedList<String>();
         for ( final FileSet fs : _fileSets ) {
             DirectoryScanner ds = null;
             try {
                 ds = fs.getDirectoryScanner( getProject() );
             } catch ( final BuildException e ) {
                 if ( "true".equals( getFailOnError() ) || !getMessage( e ).endsWith( " not found." ) ) { throw e; }
                 log( "Warning: " + getMessage( e ), Project.MSG_ERR );
                 continue;
 
             }
             final File basedir = ds.getBasedir();
             final String basedirString = basedir.getAbsolutePath();
 
             final String[] srcFiles = ds.getIncludedFiles();
 
             for ( final String filename : srcFiles ) {
                 filenames.add( basedirString + File.separator + filename );
 
             }
 
         }
         return filenames;
     }
 
     private PrintWriter getOutputWrapper() {
         return new PrintWriter( System.out );
     }
 
     private PrintWriter getOutWriter() {
         return getOutputWrapper();
     }
 
     private PrintWriter getErrWriter() {
         return getOutputWrapper();
     }
 
     public IErrorHandlingPolicy getHandlingPolicy() {
 
         return new IErrorHandlingPolicy() {
             public boolean proceedOnErrors() {
                 return true;
             }
             public boolean stopOnFirstError() {
                 return false;
             }
         };
     }
 
     public BatchRequestor getBatchRequestor() {
         return new BatchRequestor( new ProblemOutput() {
 
             private String createMessage( final CategorizedProblem problem ) {
                 final String filename = String.valueOf( problem.getOriginatingFileName() );
                 return problem.toString() + " in " +  filename + " at line " + problem.getSourceLineNumber();
 
             }
 
             @Override
             public void warning( final CategorizedProblem problem ) {
                 if ( isSuppressWarnings() ) {
                     return;
                 }
                 log( "[WARN] " + createMessage( problem ), Project.MSG_WARN );
             }
 
             @Override
             public void info( final CategorizedProblem problem ) {
                 if ( isSuppressInfos() ) {
                     return;
                 }
                 log( "[INFO] " + createMessage( problem ), Project.MSG_INFO );
 
             }
 
             @Override
             public void error( final CategorizedProblem problem ) {
                 if ( isSuppressErrors() ) {
                    return;
                 }
                    
                log( "[ERROR] " + createMessage( problem ), Project.MSG_ERR );
             }
 
         } );
 
     }
 
     public IProblemFactory getProblemFactory() {
         return new DefaultProblemFactory(Locale.getDefault());
     }
 
     public FileSystem getLibraryAccess(final List<String> sourceFilenames, final List<String> classPathEntries) {
 
         final String[] classPathEntriesArray = convertAndCombine( _baseClasspath, classPathEntries ); 
 
         return new FileSystem(  classPathEntriesArray , sourceFilenames.toArray(new String[0]), DEFAULT_ENCODING );
     }
 
     private String[] convertAndCombine( final Classpath[] baseClasspath, final List<String> classPathEntries ) {
         final List<String> entries = new LinkedList<String>();
 
         for ( final Classpath classpath : baseClasspath ) {
             entries.add( classpath.getPath() );
         }
 
         entries.addAll( classPathEntries );
 
         return entries.toArray( new String[0] );
     }
 
 
     public CompilationUnit[] getCompilationUnits( final List<String> sourceFiles ) {
         final List<CompilationUnit> units = new ArrayList<CompilationUnit>( sourceFiles.size() );
 
         String defaultEncoding = DEFAULT_ENCODING;
         if (Util.EMPTY_STRING.equals(defaultEncoding))
             defaultEncoding = null;
 
         for ( final String sourceFile : sourceFiles ) {
             units.add( new CompilationUnit(null, sourceFile, null, null ) );
         }
         return units.toArray(new CompilationUnit[0]);
     }
 
 
     public void addFileset( final FileSet set ) {
         _fileSets.add( set );
     }
 
     public void setClasspathRef( final Reference r ) {
         if ( _path == null ) {
             _path = new Path( getProject() );
         }
         _path.setRefid( r );
     }
 
 
     public String getFailOnError() {
         return _failOnError;
     }
 
 
     public void setFailOnError( final String failOnError ) {
         _failOnError = failOnError;
     }
 
     private String getMessage( final Exception ex ) {
         return ex.getMessage() == null ? ex.toString() : ex.getMessage();
     }
 
     public String getConfig() {
         return _config;
     }
 
     public void setConfig( final String eclipseJdtConfigFile ) {
         _config = eclipseJdtConfigFile;
     }
 
     public void setSuppressWarnings( final boolean suppressWarnings ) {
         _suppressWarnings = suppressWarnings;
     }
 
     public boolean isSuppressWarnings() {
         return _suppressWarnings;
     }
 
     public void setSuppressErrors( final boolean suppressErrors ) {
         _suppressErrors = suppressErrors;
     }
 
     public boolean isSuppressErrors() {
         return _suppressErrors;
     }
 
     public void setSuppressInfos( final boolean suppressInfos ) {
         _suppressInfos = suppressInfos;
     }
 
     public boolean isSuppressInfos() {
         return _suppressInfos;
     }
 
 
 }
