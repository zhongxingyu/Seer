 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import com.google.common.base.Joiner;
 import com.google.common.collect.ObjectArrays;
 
 public class AppDeployer {
 	private static final String[] IGNORE_FILES = {"testing*", "DeployApp.app*", "PlayBookSigner.app*"};
 	private static AppDeployerWindow window;
 	//	# Debug Token Request
 	//	"$TABLET_SDK_BIN"/blackberry-debugtokenrequest -storepass $PLAYBOOK_PASSWORD -devicepin $PLAYBOOK_PIN "$DEBUG_TOKEN"
 	//
 	//	# Install Debug Token
 	//	"$TABLET_SDK_BIN"/blackberry-deploy -installDebugToken "$DEBUG_TOKEN" -device $PLAYBOOK_IP -password $PLAYBOOK_PASSWORD
 	//
 	//	# Zip App
 	//	cd "$SOURCE_PATH"
 	//	zip -r "$SOURCE_PATH" *
 	//
 	//	# Package App
 	//	"$BBWP" $ZIPPED_SOURCE_PATH -d -o $PACKAGED_SOURCE_PATH
 	//
 	//	# Install App
 	//	"$TABLET_SDK_BIN"/blackberry-deploy -installApp -password $PLAYBOOK_PASSWORD -device $PLAYBOOK_IP -package "$PACKAGED_SOURCE_FILE"
 
 	public static void main(String[] args) {
 		window = new AppDeployerWindow();
 	}
 
 	private static void printToConsole(InputStream is) throws IOException {
 		String line;
 		BufferedReader input = new BufferedReader(new InputStreamReader(is));
 		while ((line = input.readLine()) != null) {
 			window.printlnToConsole(line);
 		}
 		input.close();
 	}
 
 	private static void runCommand(String[] command) throws IOException, InterruptedException {
 		runCommand(command, null);
 	}
 
 	private static void runCommand(String[] command, File dir) throws IOException, InterruptedException {
 		window.printlnToConsole("Running Command: " + Joiner.on(" ").join(command));
 		Process p = Runtime.getRuntime().exec(command, new String[0], dir);
 		p.waitFor();
 		printToConsole(p.getInputStream());
 		printToConsole(p.getErrorStream());
 		window.printlnToConsole("Finished Command");
 		window.printlnToConsole("");
 	}
 
 	public static void deploy() throws IOException, InterruptedException {
 		String[] debugTokenRequest = {getDebugTokenRequestPath(), "-storepass", window.getPlaybookPassword(), "-devicepin", window.getPlaybookPin(), getDebugTokenPath()};
 		String[] installDebugToken = {getDeployCommandPath(), "-installDebugToken", getDebugTokenPath(), "-device", window.getPlaybookIp(), "-password", window.getPlaybookPassword()};
 		String[] zip = {"zip", "-r", window.getProjectPath(), ".", "*", "-x", ".*"};
 		String[] packageApp = {getBbwpPath(), window.getProjectPath() + ".zip", "-d", "-o", window.getProjectPath()};
 		String[] deployApp = {getDeployCommandPath(), "-installApp", "-password", window.getPlaybookPassword(), "-device", window.getPlaybookIp(), "-package", getPackagePath()};
 		String[] cleanUp = {"rm", window.getProjectPath() + ".zip", getPackagePath()};
 
 		runCommand(cleanUp);
 		runCommand(debugTokenRequest);
 		runCommand(installDebugToken);
 		runCommand(ObjectArrays.concat(zip, IGNORE_FILES, String.class), new File(window.getProjectPath()));
 		runCommand(packageApp);
 		runCommand(deployApp);
 		runCommand(cleanUp);
 		window.printlnToConsole("Finished All Commands");
 	}
 
 	private static String getSdkBin() {
 		return window.getSdkPath() + "/bbwp/blackberry-tablet-sdk/bin";
 	}
 
 	private static String getDebugTokenRequestPath() {
 		return getSdkBin() + "/blackberry-debugtokenrequest";
 	}
 
 	private static String getDebugTokenPath() {
		return window.getSdkPath() + "/bbwp/blackberry-tablet-sdk/debug-tokens/playbook_debug_token.bar";
 	}
 
 	private static String getPackagePath() {
 		return window.getProjectPath() + window.getProjectPath().substring(window.getProjectPath().lastIndexOf("/")) + ".bar";
 	}
 
 	private static String getDeployCommandPath() {
 		return getSdkBin() + "/blackberry-deploy";
 	}
 
 	private static String getBbwpPath() {
 		return window.getSdkPath() + "/bbwp/bbwp";
 	}
 }
