 package com.spartansoftwareinc.okapi.ant;
 
 import java.io.File;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 
 import net.sf.okapi.applications.rainbow.pipeline.PipelineWrapper;
 import net.sf.okapi.common.filters.DefaultFilters;
 import net.sf.okapi.common.filters.FilterConfigurationMapper;
 import net.sf.okapi.common.plugins.PluginsManager;
 
 public abstract class BasePipelineTask extends Task {
 	
     public void execute() {
         checkConfiguration();
         // We need to save and restore the context class loader, because
         // Okapi's PluginsManager uses the CCL as the basis for the class
         // loader it creates to load plugins.  Ant's CCL does not contain
         // the classes used to load this task.  Therefore we need to
         // temporarily replace the CCL with the classloader this task was
         // loaded with, which includes all the okapi classes.
         ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
         try {
             Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
             executeWithOkapiClassloader();
         }
         finally {
             Thread.currentThread().setContextClassLoader(oldClassLoader);
         }
     }
     
     /**
      * Validate any parameters.
      * 
      * @throws BuildException if the task is misconfigured
      */
     abstract void checkConfiguration() throws BuildException;
     
     /**
      * Execute task logic, using a context ClassLoader that includes
      * the okapi installation referenced when the task was defined.
      */
     abstract void executeWithOkapiClassloader();
     
 	protected FilterConfigurationMapper getFilterMapper(String filterConfigPath, 
 						PluginsManager plManager) {
 		// Initialize filter configurations
         FilterConfigurationMapper fcMapper = new FilterConfigurationMapper();
         DefaultFilters.setMappings(fcMapper, false, true);
         if (plManager != null) {
         	fcMapper.addFromPlugins(plManager);
         }
         if (filterConfigPath != null) {
         	System.out.println("Loading custom filter configurations from " + 
         				       filterConfigPath);
             fcMapper.setCustomConfigurationsDirectory(filterConfigPath);
            fcMapper.updateCustomConfigurations();
         }
         return fcMapper;
     }
 	
 	protected PipelineWrapper getPipelineWrapper(File baseDir, 
 			FilterConfigurationMapper fcMapper, PluginsManager plManager) {
         PipelineWrapper pipelineWrapper = new PipelineWrapper(fcMapper, baseDir.getPath(),
                 plManager, baseDir.getPath(), baseDir.getPath(),
                 null, null, null);
         pipelineWrapper.addFromPlugins(plManager);
         return pipelineWrapper;
     }
 }
