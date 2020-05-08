 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.ant.modular;
 
 import java.io.File;
 import java.net.URL;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicReference;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Location;
 import org.apache.tools.ant.ProjectComponent;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.CallTarget;
 import org.apache.tools.ant.taskdefs.Property;
 import org.apache.tools.ant.taskdefs.Ant.Reference;
 import org.apache.tools.ant.types.Path;
 import org.apache.tools.ant.types.PropertySet;
 
 public class CallTargetForModules extends Task
 {
     private ArrayList<ModuleElement> moduleElements = new ArrayList<ModuleElement>();
     private ModuleLoader moduleLoader;
     // If defined then the correspondent Module object is set to this property for each module being processed.
     private String moduleProperty;
     
     private String target;
     private final ArrayList<ParamElement> params = new ArrayList<ParamElement>();
     private final ArrayList<Reference> references = new ArrayList<Reference>();
     private final PropertySet propertySet = new PropertySet();
     
     // Default values match antcall's defaults.
     private boolean inheritAll = true;
     private boolean inheritRefs = false;
     
     private int threadCount = 1;
     
     @Override
     public void execute() throws BuildException
     {
         if (target == null) {
             throw new BuildException("The attribute 'target' is undefined.");
         }
         if (moduleLoader == null) {
             throw new BuildException("No module loader is defined.");
         }
         
         final int moduleCount = moduleElements.size();
         if (moduleCount == 0) {
             throw new BuildException("At least one <module> element is required.");
         }
         
         for (int i = 0; i < moduleCount; ++i) {
             final ModuleElement moduleParam = moduleElements.get(i);
             if (moduleParam.path == null) {
                 throw new BuildException("There is a <module> element with the attribute 'path' undefined.");
             }
         }
         
         final ModuleRegistry registry = new ModuleRegistry(moduleLoader);
         
         try {
             final ArrayList<Module> modules = new ArrayList<Module>(moduleCount);
             for (int i = 0, n = moduleCount; i < n; ++i) {
                 final ModuleElement moduleParam = moduleElements.get(i);
                 
                 modules.add(registry.resolveModule(moduleParam.path));
             }
             
             if (threadCount == 1) {
                 processModulesSerial(modules);
             } else {
                 processModulesParallel(modules);
             }
         }
         catch (ModuleNotLoadedException ex) {
             throw buildException(ex);
         }
         catch (CyclicDependenciesDetectedException ex) {
             throw buildException(ex);
         }
     }
     
     private void callTarget(final Module module)
     {
         final CallTarget antcall = (CallTarget) getProject().createTask("antcall");
         antcall.init();
         
         if (moduleProperty != null) {
             final Property moduleParam = antcall.createParam();
             moduleParam.setName(moduleProperty);
             moduleParam.setValue(module);
         }
         for (int i = 0, n = params.size(); i < n; ++i) {
             final ParamElement param = params.get(i);
             param.populate(antcall.createParam());
         }
         for (int i = 0, n = references.size(); i < n; ++i) {
             antcall.addReference(references.get(i));
         }
         antcall.addPropertyset(propertySet);
         antcall.setInheritAll(inheritAll);
         antcall.setInheritRefs(inheritRefs);
         antcall.setTarget(target);
         
         try {
             antcall.perform();
         }
         catch (RuntimeException ex) {
             throw buildExceptionForModule(ex, module);
         }
     }
     
     private BuildException buildException(final Throwable cause)
     {
         Location location = getLocation();
         if (location == null) {
             location = Location.UNKNOWN_LOCATION;
         }
         
         return new BuildException(cause.getMessage(), cause, location);
     }
     
     private BuildException buildExceptionForModule(final Throwable cause, final Module module)
     {
         final BuildException ex = new BuildException(MessageFormat.format(
                 "Module ''{0}'': {1}", module.getPath(), cause.getMessage()), cause);
         
         // Gathering all information available from the original build exception.
         if (cause instanceof BuildException) {
             /* The thread that generated this exception is either the current thread or
                a dead thread so synchronisation is not needed to read location. */
             final Location location = ((BuildException) cause).getLocation();
             ex.setLocation(location == null ? Location.UNKNOWN_LOCATION : location);
             ex.setStackTrace(cause.getStackTrace());
         }
         
         return ex;
     }
     
     private void processModulesSerial(final ArrayList<Module> modules) throws CyclicDependenciesDetectedException
     {
         final SerialDependencyResolver dependencyResolver = new SerialDependencyResolver();
         dependencyResolver.init(modules);
         
         Module module;
         while ((module = dependencyResolver.getFreeModule()) != null) {
             callTarget(module);
             
             dependencyResolver.moduleProcessed(module);
         }
     }
     
     private void processModulesParallel(final ArrayList<Module> modules) throws CyclicDependenciesDetectedException
     {
         final ParallelDependencyResolver dependencyResolver = new ParallelDependencyResolver();
         dependencyResolver.init(modules);
         
         final AtomicBoolean buildFailed = new AtomicBoolean(false);
         final AtomicReference<Throwable> buildFailureException = new AtomicReference<Throwable>();
         
         final Thread[] threads = new Thread[threadCount];
         for (int i = 0; i < threadCount; ++i) {
             final Thread t = new Thread()
             {
                 @Override
                 public void run()
                 {
                     try {
                         // This thread is non-interruptible because its workflow does not need this.
                         for (;;) {
                             if (buildFailed.get()) {
                                 // No need to get another module is the build has failed already.
                                 return;
                             }
                             
                             final Module module = dependencyResolver.getFreeModule();
                             if (module == null) {
                                 return;
                             }
                             
                             /* Do not call dependencyResolver#moduleProcessed in case of exception!
                              * This could make the modules that depend upon this module
                              * (whose processing has failed!) free for acquisition, despite of
                              * their dependee module did not succeed.
                              * 
                              * Instead, dependencyResolver#abort() is called.
                              */
                             callTarget(module);
                             
                             // Reporting this module as processed if no error is encountered.
                             dependencyResolver.moduleProcessed(module);
                         }
                     }
                     catch (Throwable ex) {
                         buildFailed.set(true);
                         buildFailureException.set(ex);
                        /* Ensure that other threads will stop module processing right after
                            their current module is processed. */
                         dependencyResolver.abort();
                     }
                 }
             };
             threads[i] = t;
             t.start();
         }
         try {
             for (final Thread t : threads) {
                 t.join();
             }
         }
         catch (InterruptedException ex) {
             Thread.currentThread().interrupt();
             throw new BuildException("The build thread was interrupted.", ex);
         }
         
         if (buildFailed.get()) {
             final Throwable ex = (Throwable) buildFailureException.get();
             if (ex instanceof BuildException) {
                 throw (BuildException) ex;
             }
             throw new BuildException(MessageFormat.format("Build failed. Cause: ''{0}''.", ex.getMessage()), ex);
         }
     }
     
     public ModuleElement createModule()
     {
         final ModuleElement module = new ModuleElement();
         moduleElements.add(module);
         return module;
     }
     
     public void addConfigured(final ModuleLoader loader)
     {
         if (moduleLoader != null) {
             throw new BuildException("Only a single module loader element is allowed.");
         }
         moduleLoader = loader;
     }
     
     public void setThreadCount(final int threadCount)
     {
         if (threadCount <= 0) {
             throw new BuildException(MessageFormat.format(
                     "Invalid thread count: ''{0}''. It must be a positive value.",
                     String.valueOf(threadCount)));
         }
         this.threadCount = threadCount;
     }
     
     public void setModuleProperty(final String propertyName)
     {
         moduleProperty = propertyName;
     }
     
     public void setTarget(final String target)
     {
         this.target = target;
     }
     
     public void setInheritAll(final boolean inheritAll)
     {
         this.inheritAll = inheritAll;
     }
     
     public void setInheritRefs(final boolean inheritRefs)
     {
         this.inheritRefs = inheritRefs;
     }
     
     public ParamElement createParam()
     {
         final ParamElement param = new ParamElement();
         param.setProject(getProject());
         params.add(param);
         return param;
     }
     
     public void addReference(final Reference reference)
     {
         references.add(reference);
     }
     
     public void addPropertyset(final PropertySet propertySet)
     {
         this.propertySet.addPropertyset(propertySet);
     }
     
     // TODO support defining modules using regular expressions
     public static class ModuleElement
     {
         private String path;
         
         public void setPath(final String path)
         {
             this.path = path;
         }
     }
     
     public static class ParamElement extends ProjectComponent
     {
         private String name;
         private String value;
         private File location;
         private File file;
         private URL url;
         private String resource;
         private Path classpathAttribute;
         private Path classpath;
         private Reference classpathRef;
         private String environment;
         private Reference reference;
         private String prefix;
         // relative and basedir introduced in Ant 1.8.0 are not available because Ant 1.6+ is supported.
         // prefixValues introduced in Ant 1.8.2 is not available because Ant 1.6+ is supported.
         
         private boolean nameSet;
         private boolean valueSet;
         private boolean locationSet;
         private boolean fileSet;
         private boolean urlSet;
         private boolean resourceSet;
         private boolean classpathAttributeSet;
         private boolean classpathRefSet;
         private boolean environmentSet;
         private boolean referenceSet;
         private boolean prefixSet;
         
         public void setName(final String name)
         {
             this.name = name;
             nameSet = true;
         }
         
         public void setValue(final String value)
         {
             this.value = value;
             valueSet = true;
         }
         
         public void setLocation(final File location)
         {
             this.location = location;
             locationSet = true;
         }
         
         public void setFile(final File file)
         {
             this.file = file;
             fileSet = true;
         }
         
         public void setUrl(final URL url)
         {
             this.url = url;
             urlSet = true;
         }
         
         public void setResource(final String resource)
         {
             this.resource = resource;
             resourceSet = true;
         }
         
         public void setClasspath(final Path classpath)
         {
             classpathAttribute = classpath;
             classpathAttributeSet = true;
         }
         
         public Path createClasspath()
         {
             if (classpath == null) {
                 return classpath = new Path(getProject());
             } else {
                 return classpath.createPath();
             }
         }
         
         public void setClasspathRef(final Reference reference)
         {
             classpathRef = reference;
             classpathRefSet = true;
         }
         
         public void setEnvironment(final String environment)
         {
             this.environment = environment;
             environmentSet = true;
         }
         
         public void setRefid(final Reference reference)
         {
             this.reference = reference;
             referenceSet = true;
         }
         
         public void setPrefix(final String prefix)
         {
             this.prefix = prefix;
             prefixSet = true;
         }
         
         private void populate(final Property property)
         {
             if (nameSet) {
                 property.setName(name);
             }
             if (valueSet) {
                 property.setValue(value);
             }
             if (locationSet) {
                 property.setLocation(location);
             }
             if (fileSet) {
                 property.setFile(file);
             }
             if (urlSet) {
                 property.setUrl(url);
             }
             if (resourceSet) {
                 property.setResource(resource);
             }
             if (classpathAttributeSet) {
                 property.setClasspath(classpathAttribute);
             }
             if (classpath != null) {
                 property.createClasspath().add(classpath);
             }
             if (classpathRefSet) {
                 property.setClasspathRef(classpathRef);
             }
             if (environmentSet) {
                 property.setEnvironment(environment);
             }
             if (referenceSet) {
                 property.setRefid(reference);
             }
             if (prefixSet) {
                 property.setPrefix(prefix);
             }
         }
     }
 }
