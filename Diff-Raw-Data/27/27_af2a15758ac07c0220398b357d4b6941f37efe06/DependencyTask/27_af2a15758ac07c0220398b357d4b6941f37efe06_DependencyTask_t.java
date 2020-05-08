 /*
  * Created On:  26-Jan-07 6:00:00 PM
  */
 package com.thinkparity.antx;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.Path.PathElement;
 
 import com.thinkparity.antx.Dependency.Scope;
 import com.thinkparity.antx.Dependency.Type;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public class DependencyTask extends AntXTask {
 
     private static final String PROPERTY_NAME_CVS_BRANCH;
 
     private static final String PROPERTY_NAME_CVS_COMPRESSION_LEVEL;
 
     private static final String PROPERTY_NAME_CVS_ROOT;
 
     private static final String PROPERTY_NAME_TARGET_CLASSES_DIR;
 
     private static final String PROPERTY_NAME_TARGET_TEST_CLASSES_DIR;
 
     private static final String PROPERTY_NAME_VENDOR_DIR;
 
     static {
         PROPERTY_NAME_CVS_BRANCH = "cvs.branch";
         PROPERTY_NAME_CVS_COMPRESSION_LEVEL = "cvs.compressionlevel";
         PROPERTY_NAME_CVS_ROOT = "cvs.cvsroot";
         PROPERTY_NAME_TARGET_CLASSES_DIR = "target.classes.dir";
         PROPERTY_NAME_TARGET_TEST_CLASSES_DIR = "target.test-classes.dir";
         PROPERTY_NAME_VENDOR_DIR = "antx.vendor-dir";
     }
 
     /**
      * Obtain a class path id for the scope.
      * 
      * @param scope
      *            An enumerated dependency <code>Scope</code>.
      * @return A class path id <code>String</code>.
      */
     private static final String getClassPathId(final Scope scope) {
         return MessageFormat.format("{0}.classpath",
                 scope.toString().toLowerCase());
     }
 
     /**
      * Obtain a file set id for the type and scope.
      * 
      * @param type
      *            A dependency <code>Type</code>.
      * @param scope
      *            A dependncy <code>Scope</code>.
      * @return A file set id <code>String</code>.
      */
     private static final String getFileSetId(final Type type, final Scope scope) {
         return MessageFormat.format("{0}.dependencies-{1}",
                 scope.name().toLowerCase(), type.name().toLowerCase());
     }
 
     /**
      * Obtain a library path id for the scope.
      * 
      * @param scope
      *            An enumerated dependency <code>Scope</code>.
      * @return A library path id <code>String</code>.
      */
     private static final String getLibraryPathId(final Scope scope) {
         return MessageFormat.format("{0}.librarypath",
                 scope.toString().toLowerCase());
     }
 
     /**
      * Panic. Create a build error including all specified properties.
      * 
      * @param message
      *            A build error message.
      * @return A <code>BuildException</code>.
      */
     private static final BuildException panic(final String message,
             final String path, final String provider, final Scope scope,
             final Type type, final String version, final File location) {
         final StringBuffer actualMessage = new StringBuffer(message);
         if (null != path)
             actualMessage.append("{0}  path:").append(path);
         if (null != provider)
             actualMessage.append("{0}  provider:").append(provider);
         if (null != scope)
             actualMessage.append("{0}  scope:").append(scope.name());
         if (null != type)
             actualMessage.append("{0}  type:").append(type.name());
         if (null != version)
             actualMessage.append("{0}  version:").append(version);
         if (null != location)
             actualMessage.append("{0}  location:").append(location.getAbsolutePath());
         return panic(actualMessage.toString(), LINE_SEPARATOR);
     }
 
     /** A cvs <code>Locator</code>. */
     private Locator cvsLocator;
 
     /** The list of dependencies added by this task invocation. */
     private final List<Dependency> dependencies;
 
     /** A dependency path <code>String</code>. */
     private String path;
 
     /** A dependency provider <code>String</code>. */
     private String provider;
 
     /** A dependency's <code>Scope</code>. */
     private Scope scope;
 
     /** A dependency's <code>Type</code>. */
     private Type type;
 
     /** A dependency version <code>String</code>. */
     private String version;
 
     /**
      * Create Dependency.
      *
      */
     public DependencyTask() {
         super();
         this.dependencies = new ArrayList<Dependency>();
     }
 
     /**
      * Set resource.
      *
      * @param path
      *		A path <code>String</code>.
      */
     public void setPath(final String path) {
         /* TODO path contains an os platform which is an enum which is
          * upper case - not sure what to do with that */
         this.path = path;
         this.path = this.path.replace("LINUX", "linux");
         this.path = this.path.replace("WIN32", "win32");
         this.path = this.path.replace("UNIX", "unix");
     }
 
     /**
      * Set the provider.
      *
      * @param provider
      *      A provider <code>String</code>.
      */
     public void setProvider(final String provider) {
         this.provider = provider;
     }
 
     /**
      * Set scope.
      *
      * @param scope
      *      A String.
      */
     public void setScope(final String scope) {
         try {
             this.scope = Scope.valueOf(scope.toUpperCase());
         } catch (final IllegalArgumentException iax) {
             throw panic("Unknown scope:  {0}.", scope);
         }
     }
 
     /**
      * Set type.
      *
      * @param type
      *      A String.
      */
     public void setType(final String type) {
         try {
             this.type = Type.valueOf(type.toUpperCase());
         } catch (final IllegalArgumentException iax) {
             throw panic("Unknown type:  {0}.", type);
         }
     }
 
     /**
      * Set version.
      *
      * @param version
      *		A String.
      */
     public void setVersion(final String version) {
         this.version = version;
     }
 
     /**
      * Debug the state.
      *
      */
     @Override
     protected void debug() {
         final StringBuffer message = new StringBuffer();
         Object reference;
         for (final Scope scope : Scope.values()) {
             message.setLength(0);
             message.append(scope.toString().toLowerCase())
                 .append(":");
             reference = getProject().getReference(getClassPathId(scope));
             if (null == reference)
                 message.append("Not specified.");
             else
                 message.append(reference);
             log(message.toString(), Project.MSG_DEBUG);
         }
     }
 
     /**
      * @see com.thinkparity.antx.AntXTask#doExecute()
      *
      */
     @Override
     protected void doExecute() throws BuildException {
         addPathElements();
     }
 
     /**
      * Validate the task.  Check for path; scope and version.
      *
      */
     @Override
     protected void validate() throws BuildException {
         if (null == path)
             throw panic("Dependency path is not specified.", path, provider, scope, type, version, null);
         if (null == provider)
             throw panic("Dependency provider is not specified.", path, provider, scope, type, version, null);
         if (null == scope)
             throw panic("Dependency scope is not specified.", path, provider, scope, type, version, null);
         if (null == type)
             throw panic("Dependency type is not specified.", path, provider, scope, type, version, null);
         if (null == version)
             throw panic("Dependency version is not specified.", path, provider, scope, type, version, null);
         // validate scope/type combination
         if (Type.NATIVE == type && Scope.COMPILE == scope)
             throw panic("Dependency type/scope combination is invalid.", path, provider, scope, type, version, null);
         validateFileProperty(getProject(), PROPERTY_NAME_VENDOR_DIR);
         dependencies.clear();
         final File location;
         Dependency dependency;
         switch (type) {
         case JAVA:
             location = new File(getVendorDirectory(), path);
             if (!location.exists())
                 locate(path);
             if (!location.exists())
                 throw panic("Dependency does not exist and cannot be located.", path, provider, scope, type, version, location);
             if (!location.isFile())
                 throw panic("Dependency location is not a file.", path, provider, scope, type, version, location);
             dependency = new Dependency();
             dependency.setLocation(location);
             dependency.setPath(path);
             dependency.setProvider(provider);
             dependency.setScope(scope);
             dependency.setType(type);
             dependency.setVersion(version);
             dependencies.add(dependency);
             break;
         case NATIVE:
             location = new File(getVendorDirectory(), path);
             if (!location.exists())
                 locate(path);
             if (!location.exists())
                 throw panic("Dependency does not exist and cannot be located.", path, provider, scope, type, version, location);
             if (!location.isDirectory())
                 throw panic("Dependency location is not a directory.", path, provider, scope, type, version, location);
 
             final File[] nativeFiles = location.listFiles(new FileFilter() {
                 public boolean accept(final File pathname) {
                     return pathname.isFile();
                 }
             });
             for (final File nativeFile : nativeFiles) {
                 dependency = new Dependency();
                 dependency.setLocation(nativeFile);
                 dependency.setPath(path + File.separator + nativeFile.getName());
                 dependency.setProvider(provider);
                 dependency.setScope(scope);
                 dependency.setType(type);
                 dependency.setVersion(version);
                 dependencies.add(dependency);
             }
             break;
         default:
             throw panic("Unknown type {0}", path, provider, scope, type, version, null);
         }
         validateFileProperty(getProject(), PROPERTY_NAME_TARGET_CLASSES_DIR);
         validateFileProperty(getProject(), PROPERTY_NAME_TARGET_TEST_CLASSES_DIR);
     }
 
     /**
      * Add a path element for a given path id. A path element is created and
      * appended to the existing path for the given path id. If the existing path
      * does not exist; it is created.
      * 
      * @param scope
      *            A <code>Scope</code>.
      * @param dependency
      *            A <code>Dependency</code>.
      */
     private void addClassPathElement(final Scope scope,
             final Dependency dependency) {
         // obtain the existing class path
         final String classPathId = getClassPathId(scope);
         Path classPath = (Path) getProject().getReference(classPathId);
         if (null == classPath) {
             // create a new class path
             classPath = createClassPath(scope);
         }
         // add the location
         final PathElement referencePath = classPath.new PathElement();
         referencePath.setLocation(dependency.getLocation());
         classPath.add(referencePath);
         // save the path
         getProject().addReference(classPathId, classPath);
     }
 
     /**
      * Add a fileset element for a location of a dependency type in a dependency
      * scope.
      * 
      * @param type
      *            A dependency <code>Type</code>.
      * @param scope
      *            A dependency <code>Scope</code>.
      */
     private void addFilesetLocation(final Type type, final Scope scope,
             final Dependency dependency) {
         final String fileSetId = getFileSetId(type, scope);
         FileSet fileSet = (FileSet) getProject().getReference(fileSetId);
         if (null == fileSet) {
             fileSet = new FileSet();
             fileSet.setProject(getProject());
             fileSet.setDir(getVendorDirectory());
         }
         fileSet.appendIncludes(new String[] {dependency.getPath()});
         getProject().addReference(fileSetId, fileSet);
     }
 
     /**
      * Add a library path element for a given scope and location. A path element
      * is created and appended to the existing path for the given path id. If
      * the existing path does not exist; it is created.
      * 
      * @param scope
      *            A <code>Scope</code>.
      * @param location
      *            A location <code>File</code>.
      */
     private void addLibraryPathElement(final Scope scope, final File location) {
         // obtain the existing class path
         final String pathId = getLibraryPathId(scope);
         Path existingPath = (Path) getProject().getReference(pathId);
         if (null == existingPath) {
             // create a new class path
             existingPath = createLibraryPath(scope);
         }
         // add the location
         final PathElement referencePath = existingPath.new PathElement();
         referencePath.setLocation(location);
         existingPath.add(referencePath);
         // save the path
         getProject().addReference(pathId, existingPath);
     }
 
     /**
      * Add path elements for a given location. Based upon the type and scope of
      * the dependency the location will be added as a class or library path
      * within one or more scopes.
      * 
      */
     private void addPathElements() {
         for (final Dependency dependency : dependencies) {
             if (isTracked(dependency))
                 continue;
     
             switch (dependency.getType()) {
             case JAVA:
                 switch (dependency.getScope()) {
                 case COMPILE:
                     addClassPathElement(Scope.COMPILE, dependency);
                     addClassPathElement(Scope.RUNTIME, dependency);
                     addClassPathElement(Scope.TEST, dependency);
     
                     addFilesetLocation(Type.JAVA, Scope.RUNTIME, dependency);
                     addFilesetLocation(Type.JAVA, Scope.TEST, dependency);
     
                     track(Scope.COMPILE, dependency);
                     track(Scope.RUNTIME, dependency);
                     track(Scope.TEST, dependency);
                     break;
                 case RUNTIME:
                     addClassPathElement(Scope.RUNTIME, dependency);
                     addClassPathElement(Scope.TEST, dependency);
     
                     addFilesetLocation(Type.JAVA, Scope.RUNTIME, dependency);
                     addFilesetLocation(Type.JAVA, Scope.TEST, dependency);
     
                     track(Scope.RUNTIME, dependency);
                     track(Scope.TEST, dependency);
                     break;
                 case TEST:
                     addClassPathElement(Scope.TEST, dependency);
     
                     addFilesetLocation(Type.JAVA, Scope.TEST, dependency);
     
                     track(Scope.TEST, dependency);
                     break;
                 default:
                     throw panic("Unknown scope {0}", dependency.getScope().name());
                 }
                 break;
             case NATIVE:
                 switch (dependency.getScope()) {
                 case COMPILE:
                     throw panic("Unexpected scope {0} for type {1}.",
                             scope.name(), type.name());
                 case RUNTIME:
                     addLibraryPathElement(Scope.RUNTIME, dependency.getLocation());
                     addLibraryPathElement(Scope.TEST, dependency.getLocation());
     
                     addFilesetLocation(Type.NATIVE, Scope.RUNTIME, dependency);
                     addFilesetLocation(Type.NATIVE, Scope.TEST, dependency);
     
                     track(Scope.RUNTIME, dependency);
                     track(Scope.TEST, dependency);
                     break;
                 case TEST:
                     addLibraryPathElement(Scope.TEST, dependency.getLocation());
     
                     addFilesetLocation(Type.NATIVE, Scope.TEST, dependency);
     
                     track(Scope.TEST, dependency);
                     break;
                 default:
                     throw panic("Unknown scope {0}", dependency.getScope().name());
                 }
                 break;
             default:
                 throw panic("Unknown type {0}", dependency.getType().name());
             }
         }
     }
 
     /**
      * Create a new class path for a given scope. If the scope is compile or
      * runtime the default output directory will be added; if the scope is test,
      * the default output directory as well as the test output directory are
      * added.
      * 
      * @param scope
      *            The <code>Scope<code>.
      * @return The new clas <code>Path</code>.
      */
     private Path createClassPath(final Scope scope) {
         final Project project = getProject();
 
         final Path classPath = new Path(project);
         PathElement target;
         switch (scope) {
         case COMPILE:
             target = classPath.new PathElement();
             target.setLocation(getFileProperty(project, PROPERTY_NAME_TARGET_CLASSES_DIR));
             classPath.add(target);
             break;
         case RUNTIME:
             target = classPath.new PathElement();
             target.setLocation(getFileProperty(project, PROPERTY_NAME_TARGET_CLASSES_DIR));
             classPath.add(target);
             break;
         case TEST:
             target = classPath.new PathElement();
             target.setLocation(getFileProperty(project, PROPERTY_NAME_TARGET_CLASSES_DIR));
             classPath.add(target);
 
             target = classPath.new PathElement();
             target.setLocation(getFileProperty(project, PROPERTY_NAME_TARGET_TEST_CLASSES_DIR));
             classPath.add(target);
             break;
         default:
             throw panic("Unknown scope {0}", scope.name());
         }
         return classPath;
     }
 
     /**
      * Create a library path for a given scope.
      * 
      * @param scope
      *            The <code>Scope<code>.
      * @return The new library <code>Path</code>.
      */
     private Path createLibraryPath(final Scope scope) {
         final Project project = getProject();
         return new Path(project);
     }
 
     /**
      * Obtain the vendor root directory.
      * 
      * @return A directory <code>File</code>.
      */
     private File getVendorDirectory() {
         final String vendorDirname = getProperty(getProject(), PROPERTY_NAME_VENDOR_DIR);
        final File vendorDir = new File(vendorDirname);
        if (!vendorDir.exists()) {
            if (!vendorDir.mkdirs()) {
                throw panic("Vendor directory {0} does not exist and cannot be created.", vendorDirname);
            }
        }
        if (!vendorDir.isDirectory()) {
            throw panic("Vendor directory {0} is not a directory.", vendorDirname);
        }
        if (!vendorDir.canRead()) {
            throw panic("Vendor directory {0} cannot be read.", vendorDirname);
        }
        if (!vendorDir.canWrite()) {
            throw panic("Vendor directory {0} cannot be written to.", vendorDirname);
        }
        return vendorDir;
     }
 
     /**
      * Attempt to locate the dependency.
      *
      */
     private void locate(final String dependencyPath) {
         if (null == cvsLocator) {
             final Project project = getProject();
             cvsLocator = new CvsLocator(getProperty(project, PROPERTY_NAME_CVS_ROOT),
                     Integer.valueOf(getProperty(project, PROPERTY_NAME_CVS_COMPRESSION_LEVEL)),
                     getProperty(project, PROPERTY_NAME_CVS_BRANCH),
                    getVendorDirectory().getParentFile());
         }
         cvsLocator.locate(dependencyPath);
     }
 }
