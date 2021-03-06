 package ru.intellijeval;
 
 import com.intellij.openapi.actionSystem.ActionManager;
 import com.intellij.openapi.application.PathManager;
 import com.intellij.openapi.components.ApplicationComponent;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.util.io.FileUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import org.jetbrains.annotations.NotNull;
 import ru.intellijeval.toolwindow.PluginToolWindowManager;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import static com.intellij.openapi.project.Project.DIRECTORY_STORE_FOLDER;
 import static java.util.Arrays.asList;
 import static ru.intellijeval.toolwindow.PluginToolWindowManager.ExamplePluginInstaller;
 
 /**
  * @author DKandalov
  */
 public class EvalComponent implements ApplicationComponent { // TODO implement DumbAware?
 	private static final Logger LOG = Logger.getInstance(EvalComponent.class);
 
 	public static final String COMPONENT_NAME = "EvalComponent";
 	public static final String MAIN_SCRIPT = "plugin.groovy";
 
 	private static final String DEFAULT_PLUGIN_PATH = "/ru/intellijeval/exampleplugins";
 	private static final String DEFAULT_PLUGIN_SCRIPT = "default-plugin.groovy";
 
 	private static final String DEFAULT_IDEA_OUTPUT_FOLDER = "out";
 
 	public static String pluginsRootPath() {
 		return PathManager.getPluginsPath() + "/intellij-eval-plugins";
 	}
 
 	public static Map<String, String> pluginToPathMap() {
 		final boolean containsIdeaProjectFolder = new File(pluginsRootPath() + "/" + DIRECTORY_STORE_FOLDER).exists();
 
 		File[] files = new File(pluginsRootPath()).listFiles(new FileFilter() {
 			@SuppressWarnings("SimplifiableIfStatement")
 			@Override public boolean accept(File file) {
 				if (containsIdeaProjectFolder && file.getName().equals(DEFAULT_IDEA_OUTPUT_FOLDER)) return false;
 				if (file.getName().equals(DIRECTORY_STORE_FOLDER)) return false;
 				return file.isDirectory();
 			}
 		});
 		if (files == null) return new HashMap<String, String>();
 
 		HashMap<String, String> result = new HashMap<String, String>();
 		for (File file : files) {
 			result.put(file.getName(), file.getAbsolutePath());
 		}
 		return result;
 	}
 
 	public static boolean isInvalidPluginFolder(VirtualFile virtualFile) {
 		File file = new File(virtualFile.getPath());
 		if (!file.isDirectory()) return false;
 		String[] files = file.list(new FilenameFilter() {
 			@Override public boolean accept(File dir, String name) {
 				return name.equals(MAIN_SCRIPT);
 			}
 		});
 		return files.length < 1;
 	}
 
 	public static String defaultPluginScript() {
 		return readSampleScriptFile(DEFAULT_PLUGIN_PATH, DEFAULT_PLUGIN_SCRIPT);
 	}
 
 	public static String readSampleScriptFile(String pluginPath, String file) {
 		try {
 			String path = pluginPath + "/" + file;
 			return FileUtil.loadTextAndClose(EvalComponent.class.getClassLoader().getResourceAsStream(path));
 		} catch (IOException e) {
 			LOG.error(e);
 			return "";
 		}
 	}
 
 	public static boolean pluginExists(String pluginId) {
 		return pluginToPathMap().keySet().contains(pluginId);
 	}
 
	@Override
	public void initComponent() {
 		Settings settings = Settings.getInstance();
 		if (settings.justInstalled) {
 			installHelloWorldPlugin();
 			settings.justInstalled = false;
 		}
 		if (settings.runAllPluginsOnIDEStartup) {
 			runAllPlugins();
 		}
 
 		new PluginToolWindowManager().init();
 	}
 
 	private static void runAllPlugins() {
 		Util.runAction(
 				ActionManager.getInstance().getAction("InetlliJEval.EvalAllPlugins"),
 				Evaluator.RUN_ALL_PLUGINS_ON_IDE_START
 		);
 	}
 
 	private static void installHelloWorldPlugin() {
 		ExamplePluginInstaller pluginInstaller = new ExamplePluginInstaller("/ru/intellijeval/exampleplugins/helloWorld", asList("plugin.groovy"));
 		pluginInstaller.installPlugin(new ExamplePluginInstaller.Listener() {
 			@Override public void onException(Exception e, String pluginPath) {
 				LOG.warn("Failed to install plugin: " + pluginPath, e);
 			}
 		});
 	}
 
	@Override
	public void disposeComponent() {
 	}
 
 	@Override
 	@NotNull
 	public String getComponentName() {
 		return COMPONENT_NAME;
 	}
 }
