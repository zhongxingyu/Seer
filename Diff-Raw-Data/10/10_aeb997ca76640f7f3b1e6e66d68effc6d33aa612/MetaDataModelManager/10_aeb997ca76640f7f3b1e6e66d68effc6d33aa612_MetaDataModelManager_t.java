 /*******************************************************************************
  * Copyright (c) 2007 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle - initial API and implementation
  *    
  ********************************************************************************/
 package org.eclipse.jst.jsf.common.metadata.internal;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jst.jsf.common.JSFCommonPlugin;
 import org.eclipse.jst.jsf.common.metadata.Model;
 
 /**
  * Singleton instance for each IProject used to manage all standard metdata models for that project.
  *
  * Manager is responsible for loading and caching MetaDataModels.  Models are keyed by URI.
  * 
  * The manager listens for project closing resource events so that the resources can be freed up.
  */
 public final class MetaDataModelManager extends AbstractMetaDataModelManager {
 
 	private static IMetaDataModelManager SHARED_INSTANCE;
 	
 	// used to lock all instance calls for getModel
 	private static final Lock  GLOBAL_INSTANCE_LOCK = new ReentrantLock();
 	private final ModelMap models; 
 	
 	
 	/**
 	 * @return instance that is project agnostic. 
 	 */
 	public synchronized static IMetaDataModelManager getSharedInstance(){
 		if (SHARED_INSTANCE == null) {
 			SHARED_INSTANCE = new MetaDataModelManager(null);
 		}
 		return SHARED_INSTANCE;
 	}
 
 	/**
 	 * @param project
 	 */
 	MetaDataModelManager(final IProject project) {
 //		this.project = project;
 		models = new ModelMap();  
 	}
 
     public Model getModel(
             final IMetaDataModelContext modelContext) 
     {
         boolean gotLock = false;
         try
         {
             final int maxTries = 6;
             int numTries = 0;
             final Job currentJob = Job.getJobManager().currentJob();
             while (numTries < maxTries &&
                     !(gotLock = GLOBAL_INSTANCE_LOCK.tryLock(5000, TimeUnit.MILLISECONDS)))
             {
                 numTries++;
                 if (currentJob != null)
                 {
                     currentJob.yieldRule(null);
                 }
             }
 
             if (!gotLock)
             {
                 return null;
             }
             StandardModelFactory.debug(">START getModel: "+modelContext, StandardModelFactory.DEBUG_MD_GET); //$NON-NLS-1$
 
             MetaDataModel model = models.get(modelContext);
             if (model == null) {
                 // long in = System.currentTimeMillis();
                 model = loadMetadata(modelContext);
                 //System.out.println("Time to load "+modelContext.getURI()+": "+
                 // String.valueOf(System.currentTimeMillis() - in));
             } else if (model.needsRefresh()) {
                 try {
                     model.reload();
                 } catch (ModelNotSetException e) {
                     // simply load it - should not get here
                     model = loadMetadata(modelContext);
                 }
             }
             
 //            if (model != null && model.getRoot() != null)
 //                ((Model) model.getRoot())
 //                        .setCurrentModelContext(modelContext);
 
             StandardModelFactory.debug(">END getModel: "+modelContext, StandardModelFactory.DEBUG_MD_GET); //$NON-NLS-1$
             if (model != null && !model.isEmpty()){			
     			return (Model)model.getRoot();
     		}
     		return null;
         }
         catch (final InterruptedException e)
         {
             return null;
         }
         finally
         {
             if (gotLock)
             {
                 GLOBAL_INSTANCE_LOCK.unlock();
             }
         }
     }
 
 	private MetaDataModel loadMetadata(final IMetaDataModelContext context) {
 //        if (!Thread.holdsLock(GLOBAL_INSTANCE_LOCK.)) {
 //            JSFCommonPlugin
 //                    .log(IStatus.ERROR,
 //                            "Internal Error: loadMetadata must not be called if class lock not held"); //$NON-NLS-1$
 //            return null;
 //        }
 
         final IDomainLoadingStrategy strategy = DomainLoadingStrategyRegistry
                 .getInstance().getLoadingStrategy(
                         context.getDomainId());
         ;
         if (strategy == null) {
             JSFCommonPlugin
                     .log(
                             IStatus.ERROR,
                             "Internal Error: Unable to locate metadata loading strategy for: " + context.toString()); //$NON-NLS-1$
             return null;
         }
         final MetaDataModel model = StandardModelFactory.getInstance().createModel(
                 context, strategy);// new MetaDataModel(modelKey,
                                               // strategy);
         model.load();
         addModel(model);
 
         return model;
     }
 
     private void addModel(final MetaDataModel model) {
         if (model != null)
             models.put(model);
     }
 
    @Override
     public void dispose() {
     	super.dispose();
     	models.dispose();
     }

    @Override
    public void destroy() {
        // no persistent data to cleanup. just call dispose
        dispose();
    }

     /**
      * Map of models keyed by DOMAIN_ID:MODEL_ID from the context.   Project is not part of key.
      * 
      */
     private static class ModelMap 
     {
         final Map<String, MetaDataModel> map;
         private final AtomicBoolean _isDisposed = new AtomicBoolean(false);
 
         ModelMap() {
             map = new HashMap<String, MetaDataModel>();
         }
 
         /**
          * @param model
          *            adds model to the map using the given key descriptor
          */
         public void put(final MetaDataModel model) {
             assert !_isDisposed.get();
             final String key = calculateKey(model);
             synchronized (this) {
                 map.put(key, model);
             }
         }
 
         /**
          * @param context
          * @return MetaDataModel for this context. May return null.
          */
         public MetaDataModel get(final IMetaDataModelContext context) {
             assert !_isDisposed.get();
 
             final String key = calculateKey(context);
 
             synchronized (this) 
             {
                 return map.get(key);
             }
         }
 
         public void dispose() {
             if (_isDisposed.compareAndSet(false, true)) {
                 synchronized(this)
                 {
                     for (final Iterator<Map.Entry<String, MetaDataModel>> it = map.entrySet().iterator(); it.hasNext();) 
                     {
                         // System.out.println("kill mmModel: "+model.toString());
                         final Map.Entry<String, MetaDataModel> entry = it.next();
                         final MetaDataModel model = entry.getValue();
 
                         if (model != null)
                         {
                             model.cleanup();
                         }
                         it.remove();
                     }
                 }
             }
         }
 
         private String calculateKey(final MetaDataModel model) {        	
             return calculateKey(model.getModelContext());
         }
 
         private String calculateKey(final IMetaDataModelContext context) {
         	final StringBuffer buf = new StringBuffer(context.getDomainId()).append(":").append(context.getModelIdentifier()); //$NON-NLS-1$; 
             return buf.toString();
         }
     }
 }
