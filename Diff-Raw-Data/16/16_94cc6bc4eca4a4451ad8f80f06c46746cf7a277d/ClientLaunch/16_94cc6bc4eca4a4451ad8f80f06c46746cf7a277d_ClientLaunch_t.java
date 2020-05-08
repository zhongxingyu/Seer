 /**
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  */
 
 package truelauncher.client;
 
 import java.io.File;
 import java.lang.ProcessBuilder.Redirect;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import truelauncher.config.AllSettings;
 import truelauncher.utils.LauncherUtils;
 
 public class ClientLaunch {
 
 	// Launch minecraft begin
 	public static void launchMC(String mcpath, String nick, String password, String mem, String jar, String mainlass, String cmdargs)
 	{
 		try {
 			// libs and java locations
 			String cps;
 			String java = System.getProperty("java.home");
 			if (System.getProperty("os.name").toLowerCase().contains("win")) {
 				cps = ";";
 				java+="/bin/javaw.exe";
 			} else {
 				cps = ":";
 				java+="/bin/java";
 			}
 			String libs = "";
 			//resolve libs
 			File libsfolder = new File(mcpath + File.separator + AllSettings.getClientsLibsFolder());
 			for (String lib : getLibs(libsfolder)) {
 				libs += lib + cps;
 			}
 			//replace nick
 			cmdargs = cmdargs.replace("{USERNAME}", nick);
 			List<String> cmdargsarray = Arrays.asList(cmdargs.split("\\s+"));
 			//now lets launch it
 			ProcessBuilder pb = new ProcessBuilder();
 			pb.directory(new File(mcpath).getCanonicalFile());
 			List<String> cc = new ArrayList<String>();
 			cc.add(java);
 			cc.add("-Xmx" + mem);
 			cc.add("-Djava.library.path=libraries/natives");
 			cc.add("-Dfml.ignoreInvalidMinecraftCertificates=true");
 			cc.add("-Dfml.ignorePatchDiscrepancies=true");
 			cc.add("-cp");
 			cc.add(libs + jar);
 			cc.add(mainlass);
 			cc.addAll(cmdargsarray);
 			cc.add("--version");
 			cc.add("fakeversion");
 			cc.add("--gameDir");
 			cc.add(mcpath);
 			cc.add("--assetsDir");
 			cc.add(mcpath + File.separator + "assets");
 			pb.command(cc);
 			pb.redirectInput(Redirect.INHERIT);
			pb.redirectErrorStream(true);
 			if (password.isEmpty())
 			{
				pb.redirectOutput(Redirect.INHERIT);
 				pb.start();
 			} else {
 				Process p = pb.start();
 				Thread reader = new OutReader(p, password);
 				reader.start();
 			}
 		} catch (Exception e) {
 			LauncherUtils.logError(e);
 		}
 	}
 	// Launch minecraft end
 
 	
 	private static List<String> getLibs(File libsfolder)
 	{
 		List<String> libs = new ArrayList<String>();
 		for (File file : libsfolder.listFiles())
 		{
 			if (file.isDirectory())
 			{
 				libs.addAll(getLibs(file));
 			} else
 			{
 				if (file.getName().endsWith(".jar"))
 				{
 					libs.add(file.getAbsolutePath());
 				}
 			}
 		}
 		return libs;
 	}
 	
 }
