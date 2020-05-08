 package org.eclipse.gmf.internal.xpand.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.expression.SyntaxConstants;
 
 /**
 * Node: no support for relative paths
  * @author artem
  */
 public class BundleResourceManager extends ResourceManagerImpl {
 	private final URL[] paths;
 
 	public BundleResourceManager(URL... paths) {
 		assert paths != null && paths.length > 0; 
		this.paths = paths;
 	}
 
 	@Override
 	protected Reader resolve(String fullyQualifiedName, String extension) throws IOException {
 		final String urlPath = fullyQualifiedName.replaceAll(SyntaxConstants.NS_DELIM, "/") + '.' + extension;
 		for (int i = 0; i < paths.length; i++) {
 			try {
 				URL u = new URL(paths[i], urlPath);
 				InputStream is = u.openStream();
 				// XXX here we ignore the fact paths[i] may point to workspace location
 				// and hence charset can be derived from IFile
 				return new InputStreamReader(is);
 			} catch (MalformedURLException ex) {
 				/*IGNORE*/
 			} catch (IOException ex) {
 				/*IGNORE*/
 			} catch (Exception ex) {
 				// just in case
 				Activator.logError(ex);
 			}
 		}
 		return null;
 	}
 }
