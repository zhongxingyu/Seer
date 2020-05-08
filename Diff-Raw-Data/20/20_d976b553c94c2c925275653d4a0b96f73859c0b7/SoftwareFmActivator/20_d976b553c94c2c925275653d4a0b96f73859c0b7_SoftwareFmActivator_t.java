 package org.softwareFm.eclipse;
 
 import java.util.List;
 import java.util.Map;
 import java.util.UUID;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.resource.ImageRegistry;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.ISelectionService;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 import org.osgi.service.prefs.BackingStoreException;
 import org.softwareFm.configuration.SoftwareFmPropertyAnchor;
 import org.softwareFm.display.GuiBuilder;
 import org.softwareFm.display.SoftwareFmDataComposite;
 import org.softwareFm.display.SoftwareFmLayout;
 import org.softwareFm.display.actions.ActionStore;
 import org.softwareFm.display.composites.CompositeConfig;
 import org.softwareFm.display.data.GuiDataStore;
 import org.softwareFm.display.data.IUrlDataCallback;
 import org.softwareFm.display.data.IUrlToData;
 import org.softwareFm.display.displayer.DisplayerStore;
 import org.softwareFm.display.editor.EditorContext;
 import org.softwareFm.display.editor.EditorFactory;
 import org.softwareFm.display.editor.IEditorFactory;
 import org.softwareFm.display.editor.IUpdateStore;
 import org.softwareFm.display.largeButton.ILargeButtonFactory;
 import org.softwareFm.display.largeButton.LargeButtonDefn;
 import org.softwareFm.display.lists.ListEditorStore;
 import org.softwareFm.display.smallButtons.SmallButtonStore;
 import org.softwareFm.httpClient.response.IResponse;
 import org.softwareFm.jdtBinding.api.BindingRipperResult;
 import org.softwareFm.jdtBinding.api.IBindingRipper;
 import org.softwareFm.jdtBinding.api.JavaProjects;
 import org.softwareFm.jdtBinding.api.RippedResult;
 import org.softwareFm.repositoryFacard.IRepositoryFacard;
 import org.softwareFm.repositoryFacard.IRepositoryFacardCallback;
 import org.softwareFm.softwareFmImages.BasicImageRegisterConfigurator;
 import org.softwareFm.utilities.callbacks.ICallback;
 import org.softwareFm.utilities.collections.Files;
 import org.softwareFm.utilities.collections.Lists;
 import org.softwareFm.utilities.exceptions.WrappedException;
 import org.softwareFm.utilities.maps.Maps;
 import org.softwareFm.utilities.resources.IResourceGetter;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class SoftwareFmActivator extends AbstractUIPlugin {
 	public static String dataStoreConfiguratorId = "org.softwareFm.eclipse.dataStore";
 	public static String smallButtonStoreConfiguratorId = "org.softwareFm.eclipse.smallButton";
 	public static String actionStoreConfiguratorId = "org.softwareFm.eclipse.action";
 	public static String displayerStoreConfiguratorId = "org.softwareFm.eclipse.displayer";
 	public static String listEditorStoreConfiguratorId = "org.softwareFm.eclipse.listEditor";
 	public static String editorConfiguratorId = "org.softwareFm.eclipse.editor";
 	public static String largeButtonConfiguratorId = "org.softwareFm.eclipse.largeButton";
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.softwareFm.eclipse.eclipse";
 	// The shared instance
 	private static SoftwareFmActivator plugin;
 
 	private CompositeConfig compositeConfig;
 	private EditorContext editorContext;
 	private GuiBuilder guiBuilder;
 	private DisplayerStore displayerStore;
 	private ActionStore actionStore;
 	private SmallButtonStore smallButtonStore;
 	private ListEditorStore listEditorStore;
 	private GuiDataStore guiDataStore;
 	private IEditorFactory editorFactory;
 	private List<LargeButtonDefn> largeButtonDefns;
 	private SelectedArtifactSelectionManager selectedArtifactSelectionManager;
 	IRepositoryFacard repository;
 	private IUpdateStore updateStore;
 	private IResourceGetter resourceGetter;
 	private String uuid;
 
 	public SoftwareFmActivator() {
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		super.stop(context);
 		compositeConfig = null;
 		editorContext = null;
 		guiBuilder = null;
 		displayerStore = null;
 		actionStore = null;
 		smallButtonStore = null;
 		listEditorStore = null;
 		guiDataStore = null;
 		if (selectedArtifactSelectionManager != null) {
 			Plugins.walkSelectionServices(new ICallback<ISelectionService>() {
 				@Override
 				public void process(ISelectionService t) throws Exception {
 					t.removePostSelectionListener(selectedArtifactSelectionManager);
 				}
 			});
 		}
 		if (repository != null)
 			repository.shutdown();
 		repository = null;
 	}
 
 	public String getUuid() {
 		return uuid == null ? uuid=findOrMakeUuid() : uuid;
 	}
 
 	private String findOrMakeUuid() {
 		try {
 			@SuppressWarnings("deprecation")
 			IEclipsePreferences prefs = new InstanceScope().getNode(PLUGIN_ID);
 			String uuid = prefs.get("Uuid", null);
 			if (uuid == null) {
 				uuid = UUID.randomUUID().toString();
 				prefs.put("Uuid", uuid);
 				prefs.flush();
 			}
 			return uuid;
 		} catch (BackingStoreException e) {
 			throw WrappedException.wrap(e);
 		}
 	}
 
 	public static SoftwareFmActivator getDefault() {
 		return plugin;
 	}
 
 	public ICallback<Throwable> onException() {
 		return ICallback.Utils.sysErrCallback();
 	}
 
 	public CompositeConfig getCompositeConfig(Display display) {
 		if (compositeConfig == null) {
 			ImageRegistry imageRegistry = getImageRegistry();
 			new BasicImageRegisterConfigurator().registerWith(display, imageRegistry);
 			IResourceGetter resourceGetter = getResourceGetter();
 			SoftwareFmLayout layout = new SoftwareFmLayout();
 			compositeConfig = new CompositeConfig(display, layout, imageRegistry, resourceGetter);
 		}
 		return compositeConfig;
 	}
 
 	public IResourceGetter getResourceGetter() {
 		return resourceGetter == null ? resourceGetter = IResourceGetter.Utils.noResources().with(SoftwareFmPropertyAnchor.class, "SoftwareFmDisplay") : resourceGetter;
 	}
 
 	public EditorContext getEditorContext(Display display) {
 		return editorContext == null ? editorContext = new EditorContext(getCompositeConfig(display)) : editorContext;
 	}
 
 	public GuiBuilder getGuiBuilder() {
 		return guiBuilder == null ? guiBuilder = new GuiBuilder(getSmallButtonStore(), getActionStore(), getDisplayerStore(), getListEditorStore()) : guiBuilder;
 	}
 
 	ListEditorStore getListEditorStore() {
 		return listEditorStore == null ? listEditorStore = Plugins.configureMainWithCallbacks(new ListEditorStore(), listEditorStoreConfiguratorId, "class", onException()) : listEditorStore;
 	}
 
 	private DisplayerStore getDisplayerStore() {
 		return displayerStore == null ? displayerStore = Plugins.configureMainWithCallbacks(new DisplayerStore(), displayerStoreConfiguratorId, "class", onException()) : displayerStore;
 	}
 
 	private ActionStore getActionStore() {
 		return actionStore == null ? actionStore = Plugins.configureMainWithCallbacks(new ActionStore(), actionStoreConfiguratorId, "class", onException()) : actionStore;
 	}
 
 	private SmallButtonStore getSmallButtonStore() {
 		return smallButtonStore == null ? smallButtonStore = Plugins.configureMainWithCallbacks(new SmallButtonStore(), smallButtonStoreConfiguratorId, "class", onException()) : smallButtonStore;
 	}
 
 	private IUpdateStore getUpdateStore() {
 		return updateStore == null ? updateStore = new RepositoryUpdateStore(getRepository(), new IStoreUpdatedCallback() {
 			@Override
 			public void storeUpdates(String url, String entity, String attribute, Object newValue) {
 				guiDataStore.clearCache(url, entity, attribute);
				Object lastRawData = guiDataStore.getLastRawData("jar");
				guiDataStore.processData("jar", lastRawData, Maps.<String, Object> newMap());
 
 			}
 		}) : updateStore;
 	}
 
 	public static ImageDescriptor getImageDescriptor(String path) {
 		return imageDescriptorFromPlugin(PLUGIN_ID, path);
 	}
 
 	public GuiDataStore getGuiDataStore() {
 		final IRepositoryFacard repository = getRepository();
 		return guiDataStore == null ? guiDataStore = Plugins.configureMainWithCallbacks(new GuiDataStore(new IUrlToData() {
 			@Override
 			public void getData(final String entity, final String url, final Map<String, Object> context, final IUrlDataCallback callback) {
 				repository.get(url, new IRepositoryFacardCallback() {
 					@Override
 					public void process(IResponse response, Map<String, Object> data) {
 						callback.processData(entity, url, context, data);
 					}
 				});
 			}
 		}, getResourceGetter(), onException()), dataStoreConfiguratorId, "class", onException()) : guiDataStore;
 	}
 
 	public IEditorFactory getEditorFactory(Display display) {
 		return editorFactory == null ? editorFactory = Plugins.configureMainWithCallbacks(new EditorFactory(getEditorContext(display)), editorConfiguratorId, "class", onException()) : editorFactory;
 	}
 
 	public List<LargeButtonDefn> getLargeButtonDefns() {
 		if (largeButtonDefns == null) {
 			largeButtonDefns = Lists.newList();
 			final GuiBuilder guiBuilder = getGuiBuilder();
 			Plugins.useClasses(largeButtonConfiguratorId, new IPlugInCreationCallback<ILargeButtonFactory>() {
 
 				@Override
 				public void process(ILargeButtonFactory t, IConfigurationElement element) throws Exception {
 					largeButtonDefns.add(t.apply(guiBuilder));
 				}
 
 				@Override
 				public void onException(Throwable throwable, IConfigurationElement element) {
 					try {
 						SoftwareFmActivator.this.onException().process(throwable);
 					} catch (Exception e) {
 						throw WrappedException.wrap(e);
 					}
 				}
 			});
 		}
 		return largeButtonDefns;
 	}
 
 	public ISelectedBindingManager getSelectedBindingManager() {
 		return selectedArtifactSelectionManager == null ? selectedArtifactSelectionManager = makeNewSelectedArtifactManager() : selectedArtifactSelectionManager;
 
 	}
 
 	private SelectedArtifactSelectionManager makeNewSelectedArtifactManager() {
 		final SelectedArtifactSelectionManager result = new SelectedArtifactSelectionManager(IBindingRipper.Utils.ripper());
 		Plugins.walkSelectionServices(new ICallback<ISelectionService>() {
 			@Override
 			public void process(ISelectionService t) throws Exception {
 				t.addPostSelectionListener(result);
 			}
 		});
 		return result;
 	}
 
 	public IRepositoryFacard getRepository() {
 		return repository == null ? repository = IRepositoryFacard.Utils.defaultFacardWithHeaders("SoftwareFm", getUuid()) : repository;
 	}
 
 	public SoftwareFmDataComposite makeComposite(Composite parent) {
 		Display display = parent.getDisplay();
 		final GuiDataStore guiDataStore = getGuiDataStore();
 		getSelectedBindingManager().addSelectedArtifactSelectionListener(new ISelectedBindingListener() {
 			@Override
 			public void selectionOccured(final BindingRipperResult rippedResult) {
 				String javadoc = JavaProjects.findJavadocFor(rippedResult.classpathEntry);
 				String source = JavaProjects.findSourceFor(rippedResult.packageFragmentRoot);
 				ICallback<String> sourceMutator = new ICallback<String>() {
 					@Override
 					public void process(String newValue) throws Exception {
 						JavaProjects.setSourceAttachment(rippedResult.javaProject, rippedResult.classpathEntry, newValue);
 					}
 				};
 				ICallback<String> javadocMutator = new ICallback<String>() {
 					@Override
 					public void process(String newValue) throws Exception {
 						JavaProjects.setJavadoc(rippedResult.javaProject, rippedResult.classpathEntry, newValue);
 					}
 				};
 				IPath path = rippedResult.path;
 				if (path != null) {
 					String name = path.lastSegment().toString();
 					String extension = Files.extension(name);
 					String hexDigest = extension.equals("jar") ? rippedResult.hexDigest : null;
 					RippedResult result = new RippedResult(hexDigest, path.toOSString(), name, javadoc, source, javadocMutator, sourceMutator);
 					guiDataStore.processData("jar", result, Maps.<String, Object> newMap());
 				}
 			}
 		});
 		SoftwareFmDataComposite composite = new SoftwareFmDataComposite(parent, guiDataStore, getCompositeConfig(display), getActionStore(), getEditorFactory(display), getUpdateStore(), getListEditorStore(), onException(), getLargeButtonDefns());
 		return composite;
 
 	}
 
 }
