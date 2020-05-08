 package awongdev.android.cedict.database;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.zip.GZIPInputStream;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import awongdev.android.cedict.HexUtil;
 import awongdev.android.cedict.database.DictionaryTaskManager.DetailedProgress;
 import awongdev.android.cedict.database.DictionaryTaskManager.TaskProgressListener;
 
 public class DownloadDatabaseTask extends AsyncTask<Void, DetailedProgress, File> {
 	private static final String LOG_TAG = DownloadDatabaseTask.class.getCanonicalName();
 	private final int BUFFER_SIZE = 4096;
 	private final String BASE_URL = "http://awong-dev.github.io/ccedict/dictionaries/";
 	private final String DOWNLOAD_DIR = "downloaded";
 	private final Dictionary dictionary;
 	private final TaskProgressListener<DetailedProgress, Void> listener;
 	private final File downloadDir;
 	
 	public DownloadDatabaseTask(
 			Context context, Dictionary dictionary,
 			TaskProgressListener<DetailedProgress, Void> listener) {
 		this.dictionary = dictionary;
 		this.listener = listener;
 		this.downloadDir = new File(DatabaseUtil.getDatabaseDir(context), DOWNLOAD_DIR);
 	}
 
 	@Override
 	protected void onPreExecute() {
 		listener.onBegin(new DetailedProgress("Loading Metadata..."));
 	}
 	
 	@Override
 	protected File doInBackground(Void... params) {
 		VersionMetadata metadata;
 		try {
 			metadata = downloadLatestMetadata();
 		} catch (IOException e) {
 			Log.e(LOG_TAG, "Unable to load metadata", e);
 			publishProgress(new DetailedProgress("Error getting metadata!"));
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e1) {
 			}
 			return null;
 		}
 		// If the current dictionary matches the digest, then we are already up to date.
 		if (isValidDictionary(dictionary.getDictionaryPath(), metadata)) {
 			return null;
 		}
 		File newDictionaryPath = downloadDictionary(metadata);
 		publishProgress(new DetailedProgress("Replacing Dictionary..."));
 		dictionary.replaceDictionary(newDictionaryPath);
 		// TODO(awong): We should delete the whole downloadDir in a finally block.
 		downloadDir.delete();
 		return null;
 	}
 
 	@Override
 	protected void onProgressUpdate(DetailedProgress... progress) {
 		listener.onProgress(progress[0]);
 	}
 	
 	@Override
 	protected void onPostExecute(File newDictionaryPath) {
 		listener.onComplete(null);
 	}
 
 	private static class VersionMetadata {
 		public static enum Compression { GZIP, NONE }
 		public static enum Format { SQLITE }
 		
 		VersionMetadata(String digest_type, String digest, String format,
 				     String compression, String filename) {
 			this.digest_type = digest_type;
 			this.digest = digest;
 			this.filename = filename;
 			if (format.equals("sqlite")) {
 				this.format = Format.SQLITE;
 			} else {
 				throw new RuntimeException("Unknown format: " + format);
 			}
 			
 			if (compression.equals("gzip")) {
 				this.compression = Compression.GZIP;
 			} else if (compression.equals("none")) {
 				this.compression = Compression.NONE;
 			} else {
 				throw new RuntimeException("Unknown compression: " + compression);
 			}
 		}
 		
 		final Compression compression;
 		final Format format;
 		final String digest_type;
 		final String digest;
 		final String filename;
 	}
 
 	private File makeFileName(File downloadDir, VersionMetadata metadata) {
 		return new File(downloadDir, metadata.digest + ".u8.db");
 	}
 
 	private VersionMetadata downloadLatestMetadata() throws IOException {
 		HttpURLConnection urlConnection = null;
 		try {
 			URL latestUrl = new URL(BASE_URL + "LATEST.json");
 			urlConnection = (HttpURLConnection) latestUrl.openConnection();
 			// The full line comes out to around 185 bytes. 256 should be
 			// a plenty big buffer.
 			final int maxLastLineLength = 256;
 			StringBuilder sb = new StringBuilder();
 			BufferedReader reader =
 				new BufferedReader(
 					new InputStreamReader(urlConnection.getInputStream(), "UTF-8"),
 					maxLastLineLength);
 			String line = null;
 			while ((line = reader.readLine()) != null) {
 				sb.append(line + '\n');
 			}
 			JSONObject json = new JSONObject(sb.toString());
 			// {
 			//   "digest-type": "SHA-1",
 			//   "digest": "b9eb6874b3a4fcea767ca2b0a288cef2e612946b",
 			//   "format": "sqlite",
 			//   "compression": "gzip",
 			//   "name": "ccedict-2013-04-14[b9eb6874].u8.db.gz"
 			// }
 			String digest_type = json.getString("digest-type");
 			String digest = json.getString("digest");
 			String format = json.getString("format");
 			String compression = json.getString("compression");
 			String filename = json.getString("filename");     
 			return new VersionMetadata(digest_type, digest, format, compression, filename);
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		} finally {
 			if (urlConnection != null) {
 				urlConnection.disconnect();
 			}
 		}
 	}
 
 	private File downloadDictionary(VersionMetadata metadata) {
 		HttpURLConnection urlConnection = null;
 		BufferedOutputStream outputStream = null;
 		InputStream instream = null;
 		try {
 			downloadDir.mkdirs();
 			
 			File downloadPath = makeFileName(downloadDir, metadata);
 			if (downloadPath.exists()) {
 				if (isValidDictionary(downloadPath, metadata)) {
 					Log.i(LOG_TAG, "Reusing old file: " + downloadPath);
 					return downloadPath;
 				}
 				downloadPath.delete();
 			}
 			
 			URL dictionaryUrl = new URL(BASE_URL + metadata.filename);
 			Log.i(LOG_TAG, "digest type: " + metadata.digest_type);
 			Log.i(LOG_TAG, "format: " + metadata.format);
 			Log.i(LOG_TAG, "digest: " + metadata.digest);
 			Log.i(LOG_TAG, "dictionary URL: " + dictionaryUrl);
 			
 			urlConnection = (HttpURLConnection) dictionaryUrl.openConnection();
 			int contentLength = urlConnection.getContentLength();
 			
 			publishProgress(new DetailedProgress("Downloading...", 0, contentLength));
 			
 			MessageDigest md = MessageDigest.getInstance(metadata.digest_type);
 			CountingInputStream countingStream = new CountingInputStream(urlConnection.getInputStream());
 			instream = countingStream;
 	
 			if (metadata.compression == VersionMetadata.Compression.GZIP) {
 				instream = new GZIPInputStream(instream, BUFFER_SIZE);
 			} else {
 				instream = new BufferedInputStream(instream, BUFFER_SIZE);
 			}
 			instream = new DigestInputStream(instream, md);
 			
 			if (metadata.format == VersionMetadata.Format.SQLITE) {
 				outputStream = new BufferedOutputStream(new FileOutputStream(downloadPath), BUFFER_SIZE);
 				byte[] buffer = new byte[BUFFER_SIZE];
 				int length;
 				while ((length = instream.read(buffer)) > 0) {
 					publishProgress(new DetailedProgress(
 							"Downloading...", (int)countingStream.getBytesRead(), contentLength));
 					outputStream.write(buffer, 0, length);
 				}
 				
 				// TODO(awong): Actually write to the side then verify with digest.
 				if (metadata.digest.equals(HexUtil.bytesToHex(md.digest()))) {
 					return downloadPath;
 				} else {
 					Log.w(LOG_TAG, "Got digest: " + HexUtil.bytesToHex(md.digest())
 							+ " but expected: " + metadata.digest);
 					return null;
 				}
 			} else {
 				Log.e(LOG_TAG, "Unknown fileformat: " + metadata.format);
 				return null;
 			}
 		} catch (IOException ioe) {
 			new RuntimeException(ioe);
 		} catch (NoSuchAlgorithmException e) {
 			new RuntimeException(e);
 		} finally {
 			if (urlConnection != null) {
 				urlConnection.disconnect();
 			}
 			if (instream != null) {
 				try {
 					instream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			if (outputStream != null) {
 				try {
 					outputStream.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean isValidDictionary(File dictionaryPath, VersionMetadata metadata) {
 		InputStream instream = null;
 		try {
 			MessageDigest md = MessageDigest.getInstance(metadata.digest_type);
 			instream = new BufferedInputStream(new FileInputStream(dictionaryPath), BUFFER_SIZE);
 			instream = new DigestInputStream(instream, md);
 			byte[] buffer = new byte[BUFFER_SIZE];
 			int bytesRead = 0;
 			int length;
 			int filesize = (int)dictionaryPath.length();
 			while ((length = instream.read(buffer)) > 0) {
 				bytesRead += length;
 				publishProgress(
						new DetailedProgress("Checking Current Dictionary...",
								bytesRead, filesize));
 			}
 			
 			if (metadata.digest.equals(HexUtil.bytesToHex(md.digest()))) {
 				return true;
 			}
 		} catch (IOException e) {
 			Log.e(LOG_TAG, "Error reading file", e);
 		} catch (NoSuchAlgorithmException e) {
 			throw new RuntimeException(e);
 		} finally {
 			try {
 				instream.close();
 			} catch (IOException e) {
 				Log.e(LOG_TAG, "Unable to close file", e);
 			}
 		}
 		return false;
 	}
 
 }
