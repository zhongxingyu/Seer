 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.jboss.jreadline.console.settings.Settings;
 import org.osgi.framework.Bundle;
 
 import com.android.ddmlib.Log.ILogOutput;
 import com.android.ddmlib.Log.LogLevel;
 import com.voladroid.model.adb.DebugBridge;
 import com.voladroid.ui.cli.AbstractCli;
 import com.voladroid.ui.cli.ConsoleCli;
 import com.voladroid.ui.cli.FileCli;
 import com.voladroid.ui.cli.SimpleCli;
 
 public class VoladroidConsole implements IApplication {
 	@Override
 	public Object start(IApplicationContext context) throws Exception {
 		Settings.getInstance().setReadInputrc(false);
 		com.android.ddmlib.Log.setLogOutput(new ILogOutput() {
 
 			@Override
 			public void printLog(LogLevel arg0, String arg1, String arg2) {
 			}
 
 			@Override
 			public void printAndPromptLog(LogLevel arg0, String arg1,
 					String arg2) {
 
 			}
 		});
 
 		String[] arguments = (String[]) context.getArguments().get(
 				"application.args");
 		if (arguments == null) {
 			arguments = Platform.getCommandLineArgs();
 		}
 		List<String> args = Arrays.asList(arguments);
 		AbstractCli main = new SimpleCli();
 		if (args.contains("-d")) {
 			DebugBridge.getInstance().init(true);
 		}
 
 		if (args.contains("-h")) {
 			System.out.println("Usage: voladroid [command]");
 			System.out.println("  -d         Enable device operations");
 			System.out.println("  -c         Use more convenient "
 					+ "command line interface");
 			System.out.println("  -r <file>  Read commands from file");
 			System.out.println("  -h         Show this help");
 			return IApplication.EXIT_OK;
 		}
 
 		if (args.contains("-c")) {
 			main = new ConsoleCli();
 		} else if (args.contains("-r")) {
 			int idx = args.indexOf("-r");
 			String file = args.get(idx + 1);
 			try {
				main = new FileCli(file);
 			} catch (Exception ex) {
 				System.out.format("File not found, %s\n", ex.getMessage());
 				return IApplication.EXIT_OK;
 			}
 		}
 		main.start();
 
 		return IApplication.EXIT_OK;
 	}
 
 	@Override
 	public void stop() {
 		// TODO Auto-generated method stub
 
 	}
 
 }
