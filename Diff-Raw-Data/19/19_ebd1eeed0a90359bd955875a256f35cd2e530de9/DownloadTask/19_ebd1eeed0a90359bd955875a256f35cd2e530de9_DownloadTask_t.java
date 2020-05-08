 package info.sholes.camel.updater;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import android.os.AsyncTask;
 
 public class DownloadTask extends AsyncTask<Object, Integer, Exception> {
 
 	@Override
 	protected Exception doInBackground(Object... params) {
 		try {
 			InputStream in = (InputStream)params[0];
 			OutputStream os = (OutputStream)params[1];
 
 			byte[] buf = new byte[40960];
			long total = 0;
 			int read;
			int readSinceProgress = 0;
			long lastupdate = 0;
 
 			while((read = in.read(buf)) > 0) {
 				os.write(buf, 0, read);
				readSinceProgress += read;
				total += read;

				if(readSinceProgress < 4096)
					continue;

				long now = System.currentTimeMillis();
				if(now - lastupdate > 100) {
					lastupdate = now;
					publishProgress(new Integer(readSinceProgress));
					readSinceProgress = 0;
					Thread.yield();
				}
 			}
 			os.close();
 			in.close();
 
 			return null;
 		} catch(Exception e) {
 			return e;
 		}
 	}
 
 }
