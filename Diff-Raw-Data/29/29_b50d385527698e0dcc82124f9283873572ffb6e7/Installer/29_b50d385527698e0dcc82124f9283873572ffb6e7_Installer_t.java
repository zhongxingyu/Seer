 /*
  * Copyright 2013 Simon Brakhane
  *
  * This file is part of AnJaRoot.
  *
  * AnJaRoot is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  *
  * AnJaRoot is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * AnJaRoot. If not, see http://www.gnu.org/licenses/.
  */
 package org.failedprojects.anjaroot;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class Installer {
 	private static final String LOGTAG = "AnJaRootInstallerCls";
 	private final Context ctx;
 
 	private static enum InstallMode {
 		SystemInstall, RecoveryInstall, SystemUninstall
 	}
 
 	private static enum RebootMode {
 		Recovery, System
 	}
 
 	public interface Handler {
 		void preExecute();
 
 		void postExecute(boolean result);
 	}
 
 	public Installer(Context ctx) {
 		this.ctx = ctx;
 	}
 
 	private String readFileFromAssets(String file) {
 		try {
 			InputStream in = ctx.getAssets().open(file);
 			int size = in.available();
 
 			byte[] buf = new byte[size];
 			in.read(buf);
 			in.close();
 
 			return new String(buf);
 
 		} catch (IOException e) {
 			Log.e(LOGTAG, "Failed to read install-template.sh", e);
 		}
 
 		return "";
 	}
 
 	private String getInstallerLocation() {
 		final String basepath = ctx.getApplicationInfo().dataDir + "/lib/";
 		final String installer = basepath + "libanjarootinstaller.so";
 
 		if (new File(installer).exists()) {
 			return installer;
 		} else {
 			return "/system/bin/anjarootinstaller";
 		}
 	}
 
 	private int runWithSu(String[] command, String[] environment) {
 		try {
			ArrayList<String> execenv = new ArrayList<String>();
			Map<String, String> myenv = System.getenv();
			for (String name : myenv.keySet()) {
				execenv.add(String.format("%s=%s", name, myenv.get(name)));

			}
			execenv.addAll(Arrays.asList(environment));

			Process p = Runtime.getRuntime().exec(command,
					execenv.toArray(new String[execenv.size()]));
 
 			int ret = p.waitFor();
 			Log.v(LOGTAG, String.format("Command returned '%d'", ret));
 
 			return ret;
 		} catch (Exception e) {
 			e.printStackTrace();
 			Log.e(LOGTAG, "Failed to run command", e);
 		}
 
 		return -1;
 	}
 
 	private File buildScript(String command) {
 
 		String installTemplate = readFileFromAssets("install-template.sh");
		String content = installTemplate.replace("%COMMAND%", command);
 		File script = ctx.getFileStreamPath("install.sh");
 		try {
			FileOutputStream stream = ctx.openFileOutput("install.sh", 0);
			stream.write(content.getBytes());
 			stream.flush();
 			stream.close();
 		} catch (IOException e) {
			Log.e(LOGTAG, "Failed to write install.sh", e);
 			return null;
 		}
 
 		return script;
 	}
 
 	private class InstallTask extends AsyncTask<InstallMode, Integer, Boolean> {
 		private final Handler handler;
 
 		public InstallTask(Handler handler) {
 			this.handler = handler;
 		}
 
 		private String getLibraryLocation() {
 			final String basepath = ctx.getApplicationInfo().dataDir + "/lib/";
 			return basepath + "libanjarootinstaller.so";
 		}
 
 		private String getApkLocation() {
 			return ctx.getPackageCodePath();
 		}
 
 		private String buildCommand(InstallMode mode) {
 
 			String command;
 			switch (mode) {
 			case RecoveryInstall:
 				command = String.format("%s --recovery --apkpath='%s'\n",
 						getInstallerLocation(), getApkLocation());
 				break;
 			case SystemInstall:
 				command = String.format(
 						"%s --install --apkpath='%s' --srclibpath='%s'\n",
 						getInstallerLocation(), getApkLocation(),
 						getLibraryLocation());
 				break;
 			case SystemUninstall:
 				command = String.format("%s --uninstall\n",
 						getInstallerLocation());
 				break;
 			default:
 				command = "false";
 				break;
 			}
 
 			return command;
 		}
 
 		private String[] buildEnvironment(InstallMode mode) {
 			switch (mode) {
 			case SystemInstall:
 			case SystemUninstall:
 				return new String[] { "MOUNTSYSTEMRW=1" };
 			case RecoveryInstall:
 			default:
 				return new String[0];
 			}
 		}
 
 		@Override
 		protected void onPreExecute() {
 			handler.preExecute();
 		}
 
 		@Override
 		protected Boolean doInBackground(InstallMode... params) {
 			File script = buildScript(buildCommand(params[0]));
 			if (script == null) {
 				return false;
 			}
 
 			String[] env = buildEnvironment(params[0]);
 			String[] command = new String[] { "su", "-p", "-c",
 					String.format("sh %s", script.getAbsolutePath()) };
 			int ret = runWithSu(command, env);
 			return ret == 0;
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			handler.postExecute(result);
 		}
 	}
 
 	private class RebootTask extends AsyncTask<RebootMode, Integer, Boolean> {
 		private final Handler handler;
 
 		public RebootTask(Handler handler) {
 			this.handler = handler;
 		}
 
 		private String buildCommand(RebootMode mode) {
 			String cmdswitch;
 
 			switch (mode) {
 			case Recovery:
 				cmdswitch = "--reboot-recovery";
 				break;
 			default:
 			case System:
 				cmdswitch = "--reboot-system";
 				break;
 			}
 
 			return String.format("%s %s", getInstallerLocation(), cmdswitch);
 		}
 
 		@Override
 		protected Boolean doInBackground(RebootMode... params) {
 			File script = buildScript(buildCommand(params[0]));
 			if (script == null) {
 				return false;
 			}
 
 			String[] command = { "su", "-c",
 					String.format("sh %s", script.getAbsolutePath()) };
			int ret = runWithSu(command, null);
 			return ret == 0;
 		}
 
 		@Override
 		protected void onPreExecute() {
 			handler.preExecute();
 		}
 
 		@Override
 		protected void onPostExecute(Boolean result) {
 			handler.postExecute(result);
 		}
 	}
 
 	public void doRecoveryInstall(Handler handler) {
 		Log.v(LOGTAG, "Starting recovery install process");
 		InstallTask task = new InstallTask(handler);
 		task.execute(InstallMode.RecoveryInstall);
 	}
 
 	public void doSystemInstall(Handler handler) {
 		Log.v(LOGTAG, "Starting system install process");
 		InstallTask task = new InstallTask(handler);
 		task.execute(InstallMode.SystemInstall);
 	}
 
 	public void doSystemUninstall(Handler handler) {
 		Log.v(LOGTAG, "Starting system uninstall process");
 		InstallTask task = new InstallTask(handler);
 		task.execute(InstallMode.SystemUninstall);
 	}
 
 	public void doSystemReboot(Handler handler) {
 		Log.v(LOGTAG, "Performing system reboot");
 		RebootTask task = new RebootTask(handler);
 		task.execute(RebootMode.System);
 	}
 
 	public void doRecoveryReboot(Handler handler) {
 		Log.v(LOGTAG, "Performing recovery reboot");
 		RebootTask task = new RebootTask(handler);
 		task.execute(RebootMode.Recovery);
 	}
 }
