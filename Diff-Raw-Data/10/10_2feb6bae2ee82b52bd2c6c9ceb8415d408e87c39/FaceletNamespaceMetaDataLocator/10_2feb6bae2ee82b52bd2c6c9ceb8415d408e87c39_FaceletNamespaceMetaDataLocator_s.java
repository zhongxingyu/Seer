 package org.eclipse.jst.jsf.facelet.core.internal.metadata;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.content.IContentType;
 import org.eclipse.jface.util.SafeRunnable;
 import org.eclipse.jst.jsf.common.metadata.internal.AbstractMetaDataLocator;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataChangeNotificationEvent;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataLocator;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataObserver;
 import org.eclipse.jst.jsf.common.metadata.internal.IMetaDataSourceModelProvider;
 import org.eclipse.jst.jsf.common.metadata.internal.IPathSensitiveMetaDataLocator;
 import org.eclipse.jst.jsf.common.metadata.internal.MetaDataChangeNotificationEvent;
 import org.eclipse.jst.jsf.common.runtime.internal.view.model.common.Namespace;
 import org.eclipse.jst.jsf.core.internal.CompositeTagRegistryFactory;
 import org.eclipse.jst.jsf.core.internal.CompositeTagRegistryFactory.TagRegistryIdentifier;
 import org.eclipse.jst.jsf.core.metadata.internal.INamespaceModelProvider;
 import org.eclipse.jst.jsf.designtime.internal.view.model.ITagRegistry;
 import org.eclipse.jst.jsf.designtime.internal.view.model.ITagRegistry.ITagRegistryListener;
 import org.eclipse.jst.jsf.designtime.internal.view.model.ITagRegistry.TagRegistryChangeEvent;
 import org.eclipse.jst.jsf.facelet.core.internal.FaceletCorePlugin;
 
 
 /**
  * Locates Facelet {@link Namespace} metadata
  */
 public class FaceletNamespaceMetaDataLocator 
 		extends AbstractMetaDataLocator
 		implements IPathSensitiveMetaDataLocator, ITagRegistryListener {

	private static final IContentType FACELET_CONTENTTYPE = Platform.getContentTypeManager().findContentTypeFor("foo.xhtml"); //$NON-NLS-1$
 	private IProject _project;
 	private ITagRegistry _reg; 
 
 	public List<IMetaDataSourceModelProvider> locateMetaDataModelProviders(final String uri) {
 		final List<IMetaDataSourceModelProvider> providers = new ArrayList<IMetaDataSourceModelProvider>();
 		if (_reg != null) {
 			final Namespace ns = _reg.getTagLibrary(uri);
 		    
 			if (ns != null) {
 		    	providers.add(new NamespaceSourceModel(ns));
 		    }
 		}
         return providers;
 	}
 
 	public void startLocating() {
		final TagRegistryIdentifier tagRegId = new TagRegistryIdentifier(_project, FACELET_CONTENTTYPE);
 		_reg = CompositeTagRegistryFactory.getInstance().getRegistry(tagRegId);
 		if (_reg != null) {
 			_reg.addListener(this);
 		}
 
 	}
 
 	public void stopLocating() {
 		if (_reg != null) {
 			_reg.removeListener(this);
 			_reg = null;
 		}
 	}
 
 	public void setProjectContext(final IProject project) {
 		_project = project;
 	}
 
 	public void registryChanged(final TagRegistryChangeEvent changeEvent) {
 		for (final Namespace ns : changeEvent.getAffectedObjects()) {
 			if (adaptTagRegistryEvent(changeEvent) != IMetaDataChangeNotificationEvent.ADDED) {
 				final IMetaDataChangeNotificationEvent mdEvent = new MetaDataChangeNotificationEvent(this, ns.getNSUri(), adaptTagRegistryEvent(changeEvent));
 				fireEvent(mdEvent);
 			}
 		}
 	}
 
 	private int adaptTagRegistryEvent(final TagRegistryChangeEvent event) {
 		switch (event.getType()) {
 		case ADDED_NAMESPACE:
 			return IMetaDataChangeNotificationEvent.ADDED;
 		case REMOVED_NAMESPACE:			
 		case REGISTRY_DISPOSED:
 			return IMetaDataChangeNotificationEvent.REMOVED;
 		default:
 			return IMetaDataChangeNotificationEvent.CHANGED;
 		}
 	}
 	
 	private void fireEvent(final IMetaDataChangeNotificationEvent event) {
 		SafeRunnable.run(new ISafeRunnable(){
 
 			public void handleException(Throwable exception) {
 				FaceletCorePlugin.log("Error while firing metadataChangeNotificationEvent" , exception)	; //$NON-NLS-1$
 			}
 	
 			public void run() throws Exception {				
 				for (final IMetaDataObserver observer : getObservers()){					
 					observer.notifyMetadataChanged(event);
 				}
 			}
 	
 		});
 	}
 	
 	private class NamespaceSourceModel implements INamespaceModelProvider {
 
 		private Namespace ns;
 		private IMetaDataLocator locator;
 
 		NamespaceSourceModel(final Namespace ns){
 			this.ns = ns;
 		}
 		
 		public Object getSourceModel() {
 			return ns;
 		}
 
 		public IMetaDataLocator getLocator() {
 			return locator;
 		}
 
 		public void setLocator(IMetaDataLocator locator) {
 			this.locator = locator;			
 		}
 
 		public Object getAdapter(Class klass) {
 			return null;
 		}
 
 		public Namespace getNamespace() {
 			return ns;
 		}
 	}
 
 }
