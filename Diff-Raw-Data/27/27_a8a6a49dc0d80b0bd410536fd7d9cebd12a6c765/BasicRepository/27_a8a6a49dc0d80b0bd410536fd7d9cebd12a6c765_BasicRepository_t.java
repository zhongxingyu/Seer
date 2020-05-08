 package pp.eclipse.common;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 
 import pp.eclipse.parse.Parser;
 
 public class BasicRepository<Item extends DefinedItem, Container extends DefiningFile<Item>> 
 	implements Repository<Item, Container>
 {
 	private final IContainer root;
 	private final DefiningFactory<Item, Container> factory;
 	private final Parser<Item> parser;
 
 	public BasicRepository(IContainer root, DefiningFactory<Item, Container> factory, Parser<Item> parser) 
 	{
 		this.root = root;
 		this.factory = factory;
 		this.parser = parser;
 	}
 	
 	@Override
 	public List<Container> list(IProgressMonitor monitor) 
 		throws CoreException
 	{
 		monitor.beginTask("Scanning", 1);
 		final List<Container> containers = new ArrayList<Container>();
 		root.accept(new IResourceProxyVisitor() {
 			@Override
 			public boolean visit(IResourceProxy proxy) throws CoreException {
 				if (proxy.getName().matches(".*\\.xml")) {
 					IResource resource = proxy.requestResource();
 					if (resource instanceof IFile) {
						Container read = read((IFile) resource);
						if (read != null) {
							containers.add(read);
						}
 					}
 				}
				return true;
 			}
 
 		}, 0);
 		monitor.worked(1);
 		return containers;
 	}
 	
	private Container read(IFile iResource) 
		throws CoreException 
	{
 		InputStream content = null;
 		try {
 			content = iResource.getContents();
 			String charset = iResource.getCharset();
 			if (charset == null) {
 				charset = "UTF8";
 			}
 			try {
 				List<Item> parse = parser.parse(new BufferedReader(new InputStreamReader(content, charset)));
 				return factory.create(iResource.getFullPath(), iResource.getModificationStamp(), parse);
 			} catch (UnsupportedEncodingException e) {
				Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
				Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
 			} catch (Exception e) {
				Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
				Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
 			}
 		} finally {
 			if (content != null) {
 				try { 
 					content.close();
 				} catch (Exception e) {
 					// Skip
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public boolean validate(DefinedItem item) {
 		return true;
 	}
 }
