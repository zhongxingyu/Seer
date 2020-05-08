 // Copyright 2007-2009, PensioenPage B.V.
 package org.xins.logdoc.ant;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.Project;
 import static org.apache.tools.ant.Project.MSG_ERR;
 import static org.apache.tools.ant.Project.MSG_VERBOSE;
 import static org.apache.tools.ant.Project.MSG_WARN;
 import org.apache.tools.ant.taskdefs.Execute;
 import org.apache.tools.ant.taskdefs.ExecuteStreamHandler;
 import org.apache.tools.ant.taskdefs.ExecuteWatchdog;
 import org.apache.tools.ant.taskdefs.MatchingTask;
 import org.apache.tools.ant.taskdefs.PumpStreamHandler;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.util.FileUtils;
 
 import org.xins.logdoc.def.LogDef;
 
 /**
  * Abstract base class for the Logdoc Ant task implementations.
  *
  * <p>The most notable parameters supported by task implementations derived
  * from this class are:
  *
  * <dl>
  * <dt>in
  * <dd>The input directory, to read the Logdoc definitions from.
  *     Optional, defaults to project base directory.
  *
  * <dt>out
  * <dd>The output directory, to write the web pages to.
  *     Optional, defaults to source directory.
  * </dl>
  *
  * <p>This task supports more parameters and contained elements, inherited
  * from {@link MatchingTask}. For more information, see
  * <a href="http://ant.apache.org/manual/dirtasks.html">the Ant site</a>.
  *
  * @author <a href="mailto:ernst@ernstdehaan.com">Ernst de Haan</a>
  */
 public abstract class AbstractLogdocTask extends MatchingTask {
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    /**
     * Returns a quoted version of the specified string,
     * or <code>"(null)"</code> if the argument is <code>null</code>.
     *
     * @param s
     *    the character string, can be <code>null</code>,
     *    e.g. <code>"foo bar"</code>.
     *
     * @return
     *    the quoted string, e.g. <code>"\"foo bar\""</code>,
     *    or <code>"(null)"</code> if the argument is <code>null</code>.
     */
    static final String quote(String s) {
       return s == null ? "(null)" : "\"" + s + '"';
    }
 
    /**
     * Returns a quoted version string representation,
     * or <code>"(null)"</code> if the argument is <code>null</code>.
     *
     * @param o
     *    the object, can be <code>null</code>.
     *
     * @return
     *    the quoted string representation of the specified object,
     *    e.g. <code>"\"foo bar\""</code>,
     *    or <code>"(null)"</code> if the argument is <code>null</code>.
     */
    static final String quote(Object o) {
       return o == null ? "(null)" : quote(o.toString());
    }
 
    /**
     * Determines if the specified character string matches the regular
     * expression.
     *
     * @param s
     *    the string to research, or <code>null</code>.
     *
     * @param regex
     *    the regular expression, cannot be <code>null</code>.
     *
     * @return
     *    <code>true</code> if <code>s</em> matches the regular expression;
     *    <code>false</code> if it does not.
     *
     * @throws IllegalArgumentException
     *    if <code>regex == null</code> or if it has an invalid syntax. 
     */
    static final boolean matches(String s, String regex)
    throws IllegalArgumentException {
 
       // Check preconditions
       if (regex == null) {
          throw new IllegalArgumentException("regex == null");
       }
 
       // Compile the regular expression pattern
       Pattern pattern;
       try {
          pattern = Pattern.compile(regex);
       } catch (PatternSyntaxException cause) {
          throw new IllegalArgumentException("Invalid regular expression \"" + regex + "\".", cause);
       }
 
       // Short-circuit if the string is null
       if (s == null) {
          return false;
       }
 
       // Find a match
       return pattern.matcher(s).find();
    }
 
    /**
     * Checks if the specified string is either null or empty (after trimming
     * the whitespace off).
     *
     * @param s
     *    the string to check.
     *
     * @return
     *    <code>true</code> if <code>s == null || s.trim().length() &lt; 1</code>;
     *    <code>false</code> otherwise.
     */
    static final boolean isEmpty(String s) {
       return s == null || s.trim().length() < 1;
    }
 
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>AbstractLogdocTask</code> object.
     */
    protected AbstractLogdocTask() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    /**
     * The directory to read the image files from.
     * See {@link #setIn(File)}.
     */
    protected File _sourceDir;
 
    /**
     * The directory to write the PNG image files to.
     * See {@link #setOut(File)}.
     */
    protected File _destDir;
 
    /**
     * Flag that indicates if each existing file should always be overwritten,
     * even if it is newer than the source file. Default is <code>false</code>.
     */
    protected boolean _overwrite;
 
    
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    /**
     * Checks if the specified abstract path name refers to an existing
     * directory.
     *
     * @param description
     *    the description of the directory, cannot be <code>null</code>.
     *
     * @param path
     *    the abstract path name as a {@link File} object.
     *
     * @param mustBeReadable
     *    <code>true</code> if the directory must be readable.
     *
     * @param mustBeWritable
     *    <code>true</code> if the directory must be writable.
     *
     * @throws IllegalArgumentException
     *    if <code>location == null
     *          || {@linkplain #isEmpty(String) isEmpty}(description)</code>.
     *
     * @throws BuildException
     *    if <code>  path == null
     *          || ! path.exists()
     *          || ! path.isDirectory()
     *          || (mustBeReadable &amp;&amp; !path.canRead())
     *          || (mustBeWritable &amp;&amp; !path.canWrite())</code>.
     */
    private final void checkDir(String  description,
                                File    path,
                                boolean mustBeReadable,
                                boolean mustBeWritable,
                                boolean createIfNonexistent)
    throws IllegalArgumentException, BuildException {
 
       // Check preconditions
       if (isEmpty(description)) {
          throw new IllegalArgumentException("description is empty (" + quote(description) + ')');
       }
 
       // Make sure the path is set
       if (path == null) {
          throw new BuildException(description + " is not set.");
 
       // Create the directory if this is requested
       } else if (createIfNonexistent && ! path.exists()) {
          log("Creating directory " + quote(path) + '.', MSG_VERBOSE);
          if (! path.mkdirs()) {
             throw new BuildException(description + " (\"" + path + "\") could not be created.");
          }
 
       // Make sure the path refers to an existing directory
       } else if (! path.exists()) {
          throw new BuildException(description + " (\"" + path + "\") does not exist.");
       } else if (! path.isDirectory()) {
          throw new BuildException(description + " (\"" + path + "\") is not a directory.");
 
       // Make sure the directory is readable, if that is required
       } else if (mustBeReadable && (! path.canRead())) {
          throw new BuildException(description + " (\"" + path + "\") is not readable.");
 
       // Make sure the directory is writable, if that is required
       } else if (mustBeWritable && (! path.canWrite())) {
          throw new BuildException(description + " (\"" + path + "\") is not writable.");
       }
    }
 
    /**
     * Sets the path to the source directory. This parameter is required.
     *
     * @param dir
     *    the location of the source directory, or <code>null</code>.
     */
    public void setIn(File dir) {
       log("Setting \"in\" to: " + quote(dir) + '.', MSG_VERBOSE);
       _sourceDir = dir;
    }
 
    /**
     * Sets the path to the destination directory. The default is the same
     * directory.
     *
     * @param dir
     *    the location of the destination directory, or <code>null</code>.
     */
    public void setOut(File dir) {
       log("Setting \"out\" to: " + quote(dir) + '.', MSG_VERBOSE);
       _destDir = dir;
    }
 
    /**
     * Sets the <em>overwrite</em> flag.
     *
     * @param flag
     *    the value for the flag.
     */
    public void setOverwrite(boolean flag) {
       log("Setting \"overwrite\" to: \"" + flag + "\".", MSG_VERBOSE);
       _overwrite = flag;
    }
 
    @Override
    public void execute() throws BuildException {
 
       // Source directory defaults to current directory
       if (_sourceDir == null) {
          _sourceDir = getProject().getBaseDir();
       }
 
       // Destination directory defaults to source directory
       if (_destDir == null) {
          _destDir = _sourceDir;
       }
 
       // Check the directories
       checkDir("Source directory",     _sourceDir,  true, false, false);
       checkDir("Destination directory",  _destDir, false,  true,  true);
 
       // Process the files
       log("Processing from " + _sourceDir.getPath() + " to " + _destDir.getPath() + '.', MSG_VERBOSE);
       long start = System.currentTimeMillis();
 
       // Load and validate the definitions
      LogDef logDef;
       try {
         logDef = LogDef.loadFromDirectory(_sourceDir);
       } catch (Exception cause) {
          throw new BuildException("Failed to load log definition.", cause);
       }
 
       // Do the actual work
      executeImpl(logDef);
 
       // Log the total result
       long duration = System.currentTimeMillis() - start;
       log("Processed definitions in " + duration + " ms.");
    }
 
   protected abstract void executeImpl(LogDef logDef) throws BuildException;
 }
