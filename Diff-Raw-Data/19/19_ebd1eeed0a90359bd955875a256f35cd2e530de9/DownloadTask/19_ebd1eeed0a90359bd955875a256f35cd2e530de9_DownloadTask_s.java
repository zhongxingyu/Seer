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
 			int read;
 
 			while((read = in.read(buf)) > 0) {
 				os.write(buf, 0, read);
				publishProgress(new Integer(read));
 			}
 			os.close();
 			in.close();
 
 			return null;
 		} catch(Exception e) {
 			return e;
 		}
 	}
 
 }
