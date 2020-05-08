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
 
 package truelauncher.launcher;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 
 import truelauncher.utils.LauncherUtils;
 
 public class LauncherUpdateThread extends Thread {
 
 	private LauncherUpdateDialog lu;
 	private String urlfrom;
 
 	LauncherUpdateThread(LauncherUpdateDialog lu, String urlfrom) {
 		this.lu = lu;
 		this.urlfrom = urlfrom;
 	}
 
 	public void ldownloader(String urlfrom, String lto) throws Exception {
 		URL url = new URL(urlfrom);
 		URLConnection conn = url.openConnection();
 
 		if ((conn instanceof HttpURLConnection)) {
 			conn.setRequestProperty("Cache-Control", "no-cache");
 			conn.connect();
 		}
 		InputStream inputstream = conn.getInputStream();
 
 		FileOutputStream writer = new FileOutputStream(lto);
 		byte[] buffer = new byte[153600];
 
 		int downloadedAmount = 0;
 		final int totalAmount = conn.getContentLength();
 
 		lu.lpbar.setMaximum(totalAmount);
 		lu.lpbar.setMinimum(0);
 
 		int bufferSize = 0;
 		while ((bufferSize = inputstream.read(buffer)) > 0) {
 			writer.write(buffer, 0, bufferSize);
 			buffer = new byte[153600];
 			downloadedAmount += bufferSize;
 			final int pbam = downloadedAmount;
 
 			lu.lpbar.setValue(pbam);
 
 		}
 
 		writer.close();
 		inputstream.close();
 	}
 
 	public void lcopy(String from, String to) throws Exception {
 		InputStream in = new FileInputStream(from);
 		OutputStream out = new FileOutputStream(to);
 
 		byte[] buf = new byte[1024];
 		int len;
 		while ((len = in.read(buf)) > 0) {
 			out.write(buf, 0, len);
 		}
 		in.close();
 		out.close();
 	}
 
 	public void lrestart(String launcherfilename) throws Exception {
 		ArrayList<String> lls = new ArrayList<String>();
 		lls.add("java");
 		lls.add("-jar");
 		lls.add(launcherfilename);
 		ProcessBuilder lpb = new ProcessBuilder();
 		lpb.directory(new File(".").getCanonicalFile());
 		lpb.command(lls);
 		lpb.start();
 	}
 
 	public void run() {
 		try {
 			String launcherfilename = new File(System.getProperty("sun.java.command")).getName();
 			//download launcher
			ldownloader(urlfrom, launcherfilename);
 			new File(launcherfilename).setExecutable(true);
 			//start new launcher
 			lrestart(launcherfilename);
 			//close this launcher
 			Runtime.getRuntime().exit(0);
 		} catch (final Exception e) {
 			lu.lstatus.setText("Ошибка обновления");
 			lu.later.setEnabled(true);
 			LauncherUtils.logError(e);
 		}
 	}
 }
