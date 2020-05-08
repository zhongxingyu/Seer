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
 
 import java.util.ArrayList;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.CallTarget;
 import org.apache.tools.ant.taskdefs.Property;
 import org.apache.tools.ant.taskdefs.Ant.Reference;
 import org.apache.tools.ant.types.PropertySet;
 
 public class CallTargetForModules extends Task
 {
     private CallTarget antcall;
     private ArrayList<ModuleElement> moduleElements;
     private ModuleLoader moduleLoader;
     // If defined then the correspondent Module object is set to this property for each module being processed.
     private String moduleProperty;
     
     private boolean targetSet;
    private Property moduleParam;
     
     @Override
     public void init() throws BuildException
     {
         moduleProperty = null;
         targetSet = false;
         moduleElements = new ArrayList<ModuleElement>();
         antcall = (CallTarget) getProject().createTask("antcall");
         antcall.init();
     }
     
     @Override
     public void execute() throws BuildException
     {
         if (!targetSet) {
             throw new BuildException("Target is not set.");
         }
         if (moduleLoader == null) {
             throw new BuildException("The module loader element is required.");
         }
         
         final ModuleRegistry registry = new ModuleRegistry(moduleLoader);
         
         try {
             final ArrayList<Module> modules = new ArrayList<Module>(moduleElements.size());
             for (final ModuleElement moduleParam : moduleElements) {
                 if (moduleParam.path == null) {
                     throw new BuildException("Module path is undefined.");
                 }
                 modules.add(registry.resolveModule(moduleParam.path));
             }
             
             // TODO make dependency resolver configurable
             final DependencyResolver dependencyResolver = new SerialDependencyResolver();
             dependencyResolver.init(modules);
             
             Module module;
             // TODO add support for parallelism
             while ((module = dependencyResolver.getFreeModule()) != null) {
                 callTarget(module);
                 
                 dependencyResolver.moduleProcessed(module);
             }
         }
         catch (ModuleNotLoadedException ex) {
             throw new BuildException(ex.getMessage(), ex);
         }
         catch (CyclicDependenciesDetectedException ex) {
             throw new BuildException(ex.getMessage(), ex);
         }
     }
     
     private void callTarget(final Module module)
     {
         if (moduleProperty != null) {
            /* Replacing the value of the module param. This param is re-used
               so that there are no multiple conflicting params when there are
               multiple modules to be processed. */
            if (moduleParam == null) {
                moduleParam = antcall.createParam();
                moduleParam.setName(moduleProperty);
            }
            moduleParam.setValue(module);
         }
         antcall.perform();
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
     
     public void setModuleProperty(final String propertyName)
     {
         moduleProperty = propertyName;
     }
     
     public void setTarget(final String target)
     {
         antcall.setTarget(target);
         targetSet = true;
     }
     
     public void setInheritAll(final boolean inheritAll)
     {
         antcall.setInheritAll(inheritAll);
     }
     
     public void setInheritRefs(final boolean inheritRefs)
     {
         antcall.setInheritRefs(inheritRefs);
     }
     
     public Property createParam()
     {
         return antcall.createParam();
     }
     
     public void addReference(final Reference reference)
     {
         antcall.addReference(reference);
     }
     
     public void addPropertyset(final PropertySet propertySet)
     {
         antcall.addPropertyset(propertySet);
     }
     
     // TODO support defining modules using regular expressions
     public static class ModuleElement
     {
         private String path;
         
         public void setPath(final String path)
         {
             if (path == null) {
                 throw new BuildException("Module path is undefined.");
             }
             this.path = path;
         }
     }
 }
