 package org.eclipse.jst.j2ee.internal.componentcore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.zip.ZipException;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.JavaEEArchiveUtilities;
 import org.eclipse.jst.j2ee.internal.componentcore.EnterpriseBinaryComponentHelper.IReferenceCountedArchive;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.javaee.applicationclient.ApplicationClient;
 import org.eclipse.jst.javaee.applicationclient.ApplicationclientFactory;
 import org.eclipse.jst.javaee.core.DisplayName;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.ejb.EJBJar;
 import org.eclipse.jst.javaee.ejb.EjbFactory;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.jee.archive.ArchiveModelLoadException;
 import org.eclipse.jst.jee.archive.ArchiveOpenFailureException;
 import org.eclipse.jst.jee.archive.ArchiveOptions;
 import org.eclipse.jst.jee.archive.IArchive;
 import org.eclipse.jst.jee.archive.IArchiveFactory;
 import org.eclipse.jst.jee.archive.IArchiveLoadAdapter;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.BinaryComponentHelper;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 public class JavaEEBinaryComponentHelper extends BinaryComponentHelper {
 
 	private IArchive archive;
 	private EnterpriseBinaryComponentHelper legacyBinaryHelper;
 
 	private int localArchiveAccessCount = 0;
 
 	public static JavaEEQuickPeek getJavaEEQuickPeek(IVirtualComponent aBinaryComponent) {
 		JavaEEBinaryComponentHelper helper = null;
 		try {
 			helper = new JavaEEBinaryComponentHelper(aBinaryComponent);
 			IArchive archive = helper.getArchive();
 			if (archive == null) {
 				return new JavaEEQuickPeek(null);
 			}
 			JavaEEQuickPeek qp = JavaEEArchiveUtilities.INSTANCE.getJavaEEQuickPeek(archive);
 			return qp;
 		} finally {
 			if (helper != null) {
 				helper.dispose();
 			}
 		}
 	}
 
 	public JavaEEBinaryComponentHelper(IVirtualComponent aBinaryComponent) {
 		super(aBinaryComponent);
 	}
 
 	public IPath getAbsolutePath() {
 		IArchiveLoadAdapter loadAdapter = null;
 		if(getArchive().getArchiveOptions().hasOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER)){
 			loadAdapter = (IArchiveLoadAdapter)getArchive().getArchiveOptions().getOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER);
 		} else {
 			loadAdapter = (IArchiveLoadAdapter)getArchive().getArchiveOptions().getOption(ArchiveOptions.LOAD_ADAPTER);
 		}
 		return ((JavaEEBinaryComponentLoadAdapter) loadAdapter).getArchivePath();
 	}
 	
 	@Override
 	public EObject getPrimaryRootObject() {
 		JavaEEQuickPeek qp = JavaEEArchiveUtilities.INSTANCE.getJavaEEQuickPeek(getArchive());
 		IPath ddPath = null;
 		switch (qp.getType()) {
 		case JavaEEQuickPeek.APPLICATION_CLIENT_TYPE:
 			ddPath = new Path(J2EEConstants.APP_CLIENT_DD_URI);
 			break;
 		case JavaEEQuickPeek.EJB_TYPE:
 			ddPath = new Path(J2EEConstants.EJBJAR_DD_URI);
 			break;
 		case JavaEEQuickPeek.WEB_TYPE:
 			ddPath = new Path(J2EEConstants.WEBAPP_DD_URI);
 			break;
 		case JavaEEQuickPeek.CONNECTOR_TYPE:
 			ddPath = new Path(J2EEConstants.RAR_DD_URI);
 			break;
 		}
 		if (ddPath != null) {
 			if (getArchive().containsArchiveResource(ddPath)) {
 				try {
 					return (EObject) getArchive().getModelObject(ddPath);
 				} catch (ArchiveModelLoadException e) {
 					J2EEPlugin.logError(e);
 				}
 			} else {
 				String displayName = getComponent().getName();
 				//because this component is binary, its project must be the EAR.
 				IVirtualComponent earComponent = ComponentCore.createComponent(getComponent().getProject());
 				IVirtualReference [] refs = earComponent.getReferences();
 				for(IVirtualReference ref:refs){
 					if(ref.getReferencedComponent().equals(getComponent())){
 						displayName = ref.getArchiveName();
 						break;
 					}
 				}
 				DisplayName dn = JavaeeFactory.eINSTANCE.createDisplayName();
 				dn.setValue(displayName);
 				EObject root = null;
 				switch (qp.getType()) {
 				case JavaEEQuickPeek.APPLICATION_CLIENT_TYPE:
 					ApplicationClient applicationClient = ApplicationclientFactory.eINSTANCE.createApplicationClient();
 					applicationClient.getDisplayNames().add(dn);
 					root = (EObject) applicationClient;
 					break;
 				case JavaEEQuickPeek.EJB_TYPE:
 					EJBJar ejbJar = EjbFactory.eINSTANCE.createEJBJar();
 					ejbJar.getDisplayNames().add(dn);
 					root = (EObject) ejbJar;
 					break;
 				case JavaEEQuickPeek.WEB_TYPE:
 					WebApp webApp = WebFactory.eINSTANCE.createWebApp();
 					webApp.getDisplayNames().add(dn);
 					root = (EObject) webApp;
 					break;
 				}
 				return root;
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public Resource getResource(URI uri) {
 		IPath path = new Path(uri.toString());
 		try {
 			IArchiveLoadAdapter loadAdapter = null;
 			if(getArchive().getArchiveOptions().hasOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER)){
 				loadAdapter = (IArchiveLoadAdapter)getArchive().getArchiveOptions().getOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER);
 			} else {
 				loadAdapter = (IArchiveLoadAdapter)getArchive().getArchiveOptions().getOption(ArchiveOptions.LOAD_ADAPTER);
 			}
 			EObject modelRoot = (EObject)((JavaEEBinaryComponentLoadAdapter) loadAdapter).getModelObject(path); 
 			return modelRoot.eResource();
 		} catch (ArchiveModelLoadException e) {
 			J2EEPlugin.logError(e);
 		}
 		return null;
 	}
 
 	public IArchive accessArchive() {
 		IArchive archive = getArchive();
 		if (null != archive) {
 			ArchiveCache cache = ArchiveCache.getInstance();
 			cache.accessArchive(archive);
 			synchronized (this) {
 				localArchiveAccessCount++;
 			}
 		}
 		return archive;
 	}
 	
 	protected void safeReleaseArchive(IArchive archive){
 		int count = 0;
 		synchronized(this){
 			count = localArchiveAccessCount;
 		}
 		if(count > 0){
 			releaseArchive(archive);
 		}
 	}
 
 	public void releaseArchive(IArchive archive) {
 		if (archive != this.archive) {
 			throw new RuntimeException("The IArchive parameter must be the same IArchive retrieved from accessArchive"); //$NON-NLS-1$
 		}
 		if (null != archive) {
 			ArchiveCache cache = ArchiveCache.getInstance();
 			cache.releaseArchive(archive);
 			synchronized (this) {
 				localArchiveAccessCount--;
 			}
 		}
 	}
 
 	private int preSwapAccessCount = 0;
 	protected void preFileSwap(){
 		int count = 0;
 		synchronized (this) {
 			preSwapAccessCount = localArchiveAccessCount;
 			count = preSwapAccessCount;
 		}
 		while (count > 0){
 			count --;
 			releaseArchive(archive);
 		}
 	}
 	
 	protected void postFileSwap() {
 		int count = 0;
 		synchronized (this) {
 			count = preSwapAccessCount;
 			preSwapAccessCount = 0;
 		}
 		while(count > 0){
 			count --;
 			accessArchive();
 		}
 	}
 	
 	public Archive accessLegacyArchive() {
 		if(legacyBinaryHelper == null){
 			JavaEEQuickPeek qp = getJavaEEQuickPeek(getComponent());
 			switch (qp.getType()) {
 			case JavaEEQuickPeek.APPLICATION_CLIENT_TYPE:
 				legacyBinaryHelper = new AppClientBinaryComponentHelper(getComponent()){
 					@Override
 					protected void aboutToClose() {
 						safeReleaseArchive(JavaEEBinaryComponentHelper.this.archive);
 					}
 					@Override
 					protected void preCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						preFileSwap();
 					}
 					@Override
 					protected void postCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						postFileSwap();
 					}
 				};
 				break;
 			case JavaEEQuickPeek.EJB_TYPE:
 				legacyBinaryHelper = new EJBBinaryComponentHelper(getComponent()){
 					@Override
 					protected void aboutToClose() {
 						safeReleaseArchive(JavaEEBinaryComponentHelper.this.archive);
 					}
 					@Override
 					protected void preCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						preFileSwap();
 					}
 					@Override
 					protected void postCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						postFileSwap();
 					}
 				};
 				break;
 			case JavaEEQuickPeek.WEB_TYPE:
 				legacyBinaryHelper = new WebBinaryComponentHelper(getComponent()){
 					@Override
 					protected void aboutToClose() {
 						safeReleaseArchive(JavaEEBinaryComponentHelper.this.archive);
 					}
 					@Override
 					protected void preCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						preFileSwap();
 					}
 					@Override
 					protected void postCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						postFileSwap();
 					}
 				};
 				break;
 			case JavaEEQuickPeek.CONNECTOR_TYPE:
 				legacyBinaryHelper = new JCABinaryComponentHelper(getComponent()){
 					@Override
 					protected void aboutToClose() {
 						safeReleaseArchive(JavaEEBinaryComponentHelper.this.archive);
 					}
 					@Override
 					protected void preCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						preFileSwap();
 					}
 					@Override
 					protected void postCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						postFileSwap();
 					}
 				};
 				break;
 			default: //utility jar
 				legacyBinaryHelper = new UtilityBinaryComponentHelper(getComponent()){
 					@Override
 					protected void aboutToClose() {
 						safeReleaseArchive(JavaEEBinaryComponentHelper.this.archive);
 					}
 					@Override
 					protected void preCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						preFileSwap();
 					}
 					@Override
 					protected void postCleanupAfterTempSave(String uri, File original, File destinationFile) {
 						postFileSwap();
 					}
 				};
 				break;
 			}
 		}
 		accessArchive();
 		Archive legacyArchive = legacyBinaryHelper.accessArchive();
 		return legacyArchive;
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		int count = 0;
 		synchronized (this) {
 			count = localArchiveAccessCount;
 		}
 		if (count > 0 && archive != null) {
 			ArchiveCache cache = ArchiveCache.getInstance();
 			for (int i = 0; i < count; i++) {
 				cache.releaseArchive(archive);
 				synchronized (this) {
 					localArchiveAccessCount--;
 				}
 			}
 		}
 		if(legacyBinaryHelper != null){
 			IReferenceCountedArchive legacyArchive = legacyBinaryHelper.archive;
 			if(legacyArchive != null){
 				legacyArchive.forceClose();
 			}
 		}
 	}
 
 	protected IArchive getArchive() {
 		if (archive == null) {
 			archive = getUniqueArchive();
 		}
 		return archive;
 	}
 
 	protected IArchive getUniqueArchive() {
 		try {
 			return openArchive();
 		} catch (ArchiveOpenFailureException e) {
 			J2EEPlugin.logError(e);
 		}
 		return null;
 	}
 
 	protected IArchive openArchive() throws ArchiveOpenFailureException {
 		ArchiveCache cache = ArchiveCache.getInstance();
 		IArchive archive = cache.getArchive(getComponent());
 		if (archive == null) {
 			archive = cache.openArchive(this);
 		}
 		if (archive != null) {
 			cache.accessArchive(archive);
 			synchronized (this) {
 				localArchiveAccessCount++;
 			}
 		}
 		return archive;
 	}
 
 	public static void clearDisconnectedArchivesInEAR(IVirtualComponent earComponent) {
 		ArchiveCache.getInstance().clearDisconnectedArchivesInEAR(earComponent);
 	}
 
 	public static void clearAllArchivesInProject(IProject earProject) {
 		ArchiveCache.getInstance().clearAllArchivesInProject(earProject);
 	}
 
 	/**
 	 * This cache manages IArchives across all
 	 * {@link JavaEEBinaryComponentHelper} instances. If multiple
 	 * {@link JavaEEBinaryComponentHelper} instances exist for the same
 	 * underlying archive file (e.g. a jar file on disk) all will use the exact
 	 * same IArchive instance. Care needs to be taken in managing the opening
 	 * and closing of this IArchive which should only be done through internal
 	 * methods within {@link JavaEEBinaryComponentHelper}
 	 */
 	private static class ArchiveCache {
 
 		private static class ArchiveCacheInstanceHolder {
 			private static ArchiveCache instance = new ArchiveCache();
 		}
 
 		public static ArchiveCache getInstance() {
 			return ArchiveCacheInstanceHolder.instance;
 		}
 
 		protected Map<IVirtualComponent, IArchive> componentsToArchives = new Hashtable<IVirtualComponent, IArchive>();
 
 		protected Map<IArchive, Integer> archiveAccessCount = new Hashtable<IArchive, Integer>();
 
 		public synchronized void accessArchive(IArchive archive) {
 			Integer count = archiveAccessCount.get(archive);
 			Integer newCount = new Integer(count.intValue() + 1);
 			archiveAccessCount.put(archive, newCount);
 			if (newCount.intValue() == 1) {
 				JavaEEBinaryComponentLoadAdapter binaryAdapter = null;
 				if (archive.getArchiveOptions().hasOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER)) {
 					binaryAdapter = (JavaEEBinaryComponentLoadAdapter) archive.getArchiveOptions().getOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER);
 				} else {
 					binaryAdapter = (JavaEEBinaryComponentLoadAdapter) archive.getArchiveOptions().getOption(ArchiveOptions.LOAD_ADAPTER);
 				}
 				if (!binaryAdapter.isPhysicallyOpen()) {
 					try {
 						binaryAdapter.physicallyOpen();
 					} catch (ZipException e) {
 						org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin.logError(e);
 					} catch (IOException e) {
 						org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin.logError(e);
 					}
 				}
 			}
 		}
 
 		public synchronized void releaseArchive(IArchive archive) {
 			Integer count = archiveAccessCount.get(archive);
 			Integer newCount = new Integer(count.intValue() - 1);
 			archiveAccessCount.put(archive, newCount);
 			if (newCount.intValue() == 0) {
 				JavaEEBinaryComponentLoadAdapter binaryAdapter = null;
 				if (archive.getArchiveOptions().hasOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER)) {
 					binaryAdapter = (JavaEEBinaryComponentLoadAdapter) archive.getArchiveOptions().getOption(JavaEEArchiveUtilities.WRAPPED_LOAD_ADAPTER);
 				} else {
 					binaryAdapter = (JavaEEBinaryComponentLoadAdapter) archive.getArchiveOptions().getOption(ArchiveOptions.LOAD_ADAPTER);
 				}
 				if (binaryAdapter.isPhysicallyOpen()) {
 					binaryAdapter.physicallyClose();
 				}
 			}
 		}
 
 		public synchronized IArchive getArchive(IVirtualComponent component) {
 			IArchive archive = componentsToArchives.get(component);
 			return archive;
 		}
 
 		public synchronized void clearDisconnectedArchivesInEAR(IVirtualComponent earComponent) {
 			if (componentsToArchives.isEmpty()) {
 				return;
 			}
 			Set<IVirtualComponent> liveBinaryComponnts = new HashSet<IVirtualComponent>();
 			IVirtualReference[] refs = earComponent.getReferences();
 			IVirtualComponent component = null;
 			for (int i = 0; i < refs.length; i++) {
 				component = refs[i].getReferencedComponent();
 				if (component.isBinary()) {
 					liveBinaryComponnts.add(component);
 				}
 			}
 			clearArchivesInProject(earComponent.getProject(), liveBinaryComponnts);
 		}
 
 		public synchronized void clearAllArchivesInProject(IProject earProject) {
 			if (componentsToArchives.isEmpty()) {
 				return;
 			}
 			clearArchivesInProject(earProject, null);
 		}
 
 		private void clearArchivesInProject(IProject earProject, Set excludeSet) {
 			Iterator iterator = componentsToArchives.entrySet().iterator();
 			IVirtualComponent component = null;
 			IArchive archive = null;
 			while (iterator.hasNext()) {
 				Map.Entry entry = (Map.Entry) iterator.next();
 				component = (IVirtualComponent) entry.getKey();
 				if (component.getProject().equals(earProject) && (excludeSet == null || !excludeSet.contains(component))) {
 					archive = (IArchive) entry.getValue();
 					IArchiveFactory.INSTANCE.closeArchive(archive);
 					iterator.remove();
 					archiveAccessCount.remove(archive);
 				}
 			}
 		}
 
 		public synchronized IArchive openArchive(JavaEEBinaryComponentHelper helper) throws ArchiveOpenFailureException {
 			IArchive archive = JavaEEArchiveUtilities.INSTANCE.openArchive(helper.getComponent());
 			componentsToArchives.put(helper.getComponent(), archive);
 			archiveAccessCount.put(archive, new Integer(0));
 			return archive;
 		}
 	}
 
 }
