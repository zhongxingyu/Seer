 package org.rapidbeans.rapidenv.config;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ConnectException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.UnknownHostException;
 import java.util.List;
 
 import org.rapidbeans.core.util.FileHelper;
 import org.rapidbeans.rapidenv.RapidEnvException;
 import org.rapidbeans.rapidenv.RapidEnvInterpreter;
 import org.rapidbeans.rapidenv.config.cmd.ExceptionMap;
 import org.rapidbeans.rapidenv.security.Verifyer;
 
 public class HttpDownload {
 
 	/**
 	 * Load an arbitrary file via HTTP.
 	 *
 	 * @param url URL of the remote source file
 	 * @param target local target file
 	 */
 	public static void download(final URL url, final File target,
 			final List<Filecheck> filechecks) {
 		// Hack for local tests
 		if (url.toString().startsWith("http://D:/")) {
 			FileHelper.copyFile(new File(url.toString().substring(7)), target);
 		} else {
 			InputStream is = null;
 			FileOutputStream os = null;
 			try {
 				final URLConnection urlc = url.openConnection();
 				final byte[] buffer = new byte[1024];
 				is = urlc.getInputStream();
 				os = new FileOutputStream(target);
 				int bytesRead;
 				int retry = 0;
				while (retry < 5) {
 					bytesRead = is.read(buffer);
 					switch (bytesRead) {
 					case -1:
 						retry = Integer.MAX_VALUE;
 						break;
 					case 0:
 						try {
							Thread.sleep(100);
 						} catch (InterruptedException e) {
 							throw new RapidEnvException(e);
 						}
 						retry++;
 						break;
 					default:
 						os.write(buffer, 0, bytesRead);
 						break;
 					}
 				}
 			} catch (UnknownHostException e) {
 				throw new RapidEnvException("Download failed from unknown host \""
 							+ e.getMessage() + "\"\n"
 							+ "Please check if you are connected to the LAN or Internet.",
 						e, ExceptionMap.ERRORCODE_HTTP_DOWNLOAD);
 			} catch (ConnectException e) {
 				if (e.getMessage().startsWith("Connection timed out")) {
 					throw new RapidEnvException("Download failed from URL \""
 							+ url.toString() + "\"\n"
 							+ "Connection timed out.",
 						e, ExceptionMap.ERRORCODE_HTTP_DOWNLOAD_CONNECTION_TIMEOUT);
 				} else {
 					throw new RapidEnvException("Download failed from URL \""
 							+ url.toString() + "\"\n"
 							+ "Connection probleme \"" + e.getMessage() + "\"",
 						e, ExceptionMap.ERRORCODE_HTTP_DOWNLOAD_CONNECTION_PROBLEM);
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 				throw new RapidEnvException("HTPP download failed from URL \""
 							+ url.toString() + "\" to file \""
 							+ target.getAbsolutePath(),
 						e);
 			} finally {
 				if (is != null) {
 					try {
 						is.close();
 					} catch (IOException e) {
 						throw new RapidEnvException("closing input stream failed "
 								+ "for URL \"" + url.toString(), e);
 					}
 				}
 				if (os != null) {
 					try {
 						os.close();
 					} catch (IOException e) {
 						throw new RapidEnvException("Closing output stream failed for file \""
 								+ target.getAbsolutePath(), e);
 					}
 				}
 			}
 		}
 		if (target.exists()
 				&& filechecks != null) {
 			for (final Filecheck check : filechecks) {
 				final String checksum = Verifyer.hashValue(target,
 						check.getHashalgorithm());
 				if (checksum.equals(check.getHashvalue())) {
 					final RapidEnvInterpreter interpreter =
 							RapidEnvInterpreter.getInstance();
 					if (interpreter != null) {
 						interpreter.getOut().println("  "
 								+ check.getHashalgorithm().name()
 								+ " Hashvalue OK");
 					}
 				} else {
 					throw new RapidEnvException("File \""
 							+ target.getAbsolutePath() + "\" has an icorrect"
 							+ " hash value \"" + checksum + "\".");
 				}
 			}
 		}
 	}
 }
