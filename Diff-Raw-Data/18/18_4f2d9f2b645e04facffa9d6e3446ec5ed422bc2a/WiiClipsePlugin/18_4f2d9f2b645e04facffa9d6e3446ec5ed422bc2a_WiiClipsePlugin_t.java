 package com.github.wiiclipse.core;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.cdt.core.model.CoreModel;
 import org.eclipse.cdt.core.settings.model.CProjectDescriptionEvent;
 import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
 import org.eclipse.cdt.core.settings.model.ICLanguageSetting;
 import org.eclipse.cdt.core.settings.model.ICProjectDescription;
 import org.eclipse.cdt.core.settings.model.ICProjectDescriptionListener;
 import org.eclipse.cdt.core.settings.model.ICTargetPlatformSetting;
 import org.eclipse.core.runtime.Plugin;
 import org.eclipse.swt.layout.GridData;
 import org.osgi.framework.BundleContext;
 
 import com.github.wiiclipse.managedbuild.WiiClipseCExternalSettingsProvider;
 
 public class WiiClipsePlugin extends Plugin implements
 		ICProjectDescriptionListener {
 
 	public WiiClipsePlugin() {
 		super();
 		WiiClipsePathResolver.resolvePaths();
 	}
 
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		// System.out.println("START");
 		CoreModel.getDefault().addCProjectDescriptionListener(this,
 				CProjectDescriptionEvent.ABOUT_TO_APPLY);
 	}
 
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		super.stop(context);
 		// System.out.println("STOP");
 		CoreModel.getDefault().removeCProjectDescriptionListener(this);
 	}
 
 	private static final String WIICLIPSE_TARGET_PLATFORM = "com.github.wiiclipse.toolchain.platform.base";
 	public static final String ID = "com.github.wiiclipse.plugin";
 
 	private static boolean isWiiClipseConfig(ICConfigurationDescription config) {
 		ICTargetPlatformSetting platform = config.getTargetPlatformSetting();
 		if (platform == null)
 			return false;
 
 		return platform.getId().startsWith(WIICLIPSE_TARGET_PLATFORM);
 	}
 
 	@Override
 	public void handleEvent(CProjectDescriptionEvent event) {
 		ICProjectDescription projDesc = event.getNewCProjectDescription();
 		ICConfigurationDescription[] configurations = projDesc
 				.getConfigurations();
 		for (ICConfigurationDescription config : configurations) {
 			System.out.println(config);
 			if (config.isValid()) {
 				List<String> extSettingProviders = new ArrayList<String>(
 						Arrays.asList(config.getExternalSettingsProviderIds()));
 				if (isWiiClipseConfig(config)) {
 					if (!extSettingProviders
 							.contains(WiiClipseCExternalSettingsProvider.ID)) {
 						extSettingProviders
 								.add(WiiClipseCExternalSettingsProvider.ID);
 					}
 
 					config.setExternalSettingsProviderIds(extSettingProviders
 							.toArray(new String[0]));
					config.updateExternalSettingsProviders(extSettingProviders
							.toArray(new String[0]));
 				} else if (extSettingProviders
 						.contains(WiiClipseCExternalSettingsProvider.ID)) {
 					extSettingProviders
 							.remove(WiiClipseCExternalSettingsProvider.ID);
 					config.setExternalSettingsProviderIds(extSettingProviders
 							.toArray(new String[0]));
					config.updateExternalSettingsProviders(extSettingProviders
							.toArray(new String[0]));
 				}
 			}
 		}
 	}
 }
