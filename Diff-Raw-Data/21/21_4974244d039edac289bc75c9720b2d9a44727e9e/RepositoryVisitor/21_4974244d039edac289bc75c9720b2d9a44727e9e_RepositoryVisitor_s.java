 package pp.eclipse.open;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 
 import pp.eclipse.open.parse.Parser;
 import pp.eclipse.open.parse.Parser.ParserResult;
 
 public class RepositoryVisitor implements IResourceProxyVisitor 
 {
     private final Pattern ignore;
     private final Callable<Boolean> cancelled;
     private final List<Container> containers;
     private final Parser parser;
     private final Map<String, Container> pathCache;
     private final Map<String, List<Item>> hashCache;
     private final Hasher hasher = new Hasher();
 
     public RepositoryVisitor(Pattern ignore, Callable<Boolean> cancelled, List<Container> containers,
                              Parser parser,
                              Map<String, Container> pathCache,
                              Map<String, List<Item>> hashCache)
     {
         this.ignore = ignore;
         this.cancelled = cancelled;
         this.containers = containers;
         this.parser = parser;
         this.pathCache = pathCache;
         this.hashCache = hashCache;
     }
 
     public boolean visit(IResourceProxy proxy) {
         try {
             if (cancelled.call()) {
                 return false;
             }
         } catch (Exception e) {
             return false;
         }
         if (proxy.getType() == IResource.FOLDER) {
             if (ignore != null && isIgnored(proxy.requestFullPath().toPortableString())) {
                 return false;
             }
             return true;
         }
         if (!proxy.getName().endsWith(".xml")) {
             return true;
         }
         IPath fullPath = proxy.requestFullPath();
         String portablePath = fullPath.toPortableString();
         if (isIgnored(portablePath)) {
             return true;
         }
         Container container = pathCache.get(portablePath);
         if (container != null && container.modified == proxy.getModificationStamp()) {
             // Resource not updated
             containers.add(container);
             return true;
         }
         IResource resource = proxy.requestResource();
         if (!(resource instanceof IFile)) {
             return true;
         }
         IFile file = (IFile) resource;
         String hash = readHash(file);
         if (container != null && hash != null && hash.equals(container.hash)) {
             // Resource updated, but hash the same
             container = container.withModification(proxy.getModificationStamp());
             pathCache.put(portablePath, container);
             containers.add(container);
             return true;
         }
         List<Item> items = hashCache.get(hash);
         if (items != null) {
             // Resource matched by hash (moved or restored from vcs)
            container = new Container(fullPath, proxy.getModificationStamp(), hash, items);
             pathCache.put(portablePath, container);
             if (items.size() > 0) {
                 containers.add(container);
             }
             return true;
         }
         items = readItems(file);
         container = new Container(fullPath, proxy.getModificationStamp(), hash == null ? "unavailable" : hash, items);
         if (hash != null) {
             hashCache.put(hash, items);
         }
         pathCache.put(portablePath, container);
         if (items.size() > 0) {
             containers.add(container);
         }
         return true;
     }
 
     private boolean isIgnored(String portablePath) 
     {
         return ignore != null && ignore.matcher(portablePath).matches();
     }
 
     private String readHash(IFile iResource)
     {
         InputStream content = null;
         try {
             try {
                 content = iResource.getContents();
                 return hasher.hash(content);
             } catch (CoreException e) {
                 if (e.getMessage() != null && e.getMessage().startsWith("Resource is out of sync with the file system:")) {
                     try {
                         iResource.refreshLocal(1, null);
                         content = iResource.getContents();
                         return hasher.hash(content);
                     } catch (CoreException e1) {
                         // Ignore Report original Error
                     }
                 }
                 Logger.getLogger("pp.eclipse.read").fine("Read of " + iResource.getName() + " failed: " + e.getMessage());
                 Logger.getLogger("pp.eclipse.hash").log(Level.FINER, "Hash failure", e);
             }
         } catch (IOException e) {
             Logger.getLogger("pp.eclipse.read").fine("Read of " + iResource.getName() + " failed: " + e.getMessage());
             Logger.getLogger("pp.eclipse.hash").log(Level.FINER, "Hash failure", e);
         } finally {
             if (content != null) {
                 try {
                     content.close();
                 } catch (IOException e) {
                     // Ignore
                 }
             }
         }
         return null;
     }
 
     private List<Item> readItems(IFile iResource)
     {
         InputStream content = null;
         List<Item> result = Collections.emptyList();
         try {
             content = iResource.getContents();
             String charset = iResource.getCharset();
             if (charset == null) {
                 charset = "UTF8";
             }
             try {
                 BufferedReader reader = new BufferedReader(new InputStreamReader(content, charset));
                 ParserResult parsed = parser.parse(reader);
                 List<Item> parsedcontents = parsed.items;
                 List<Item> updated = new ArrayList<Item>(parsedcontents.size());
                 IPath fullPath = iResource.getFullPath();
                 for (Item parse : parsedcontents) {
                     updated.add(parse.path(fullPath));
                 }
                 for (Set<Item> references : parsed.references.values()) {
                     for (Item reference : references) {
                         updated.add(reference.path(fullPath));
                     }
                 }
                 result = updated;
             } catch (UnsupportedEncodingException e) {
                 Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
                 Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
             } catch (Exception e) {
                 Logger.getLogger("pp.eclipse.parse").fine("Parse of " + iResource.getName() + " failed: " + e.getMessage());
                 Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Parse failure", e);
             }
         } catch (CoreException e) {
             Logger.getLogger("pp.eclipse.read").fine("Read of " + iResource.getName() + " failed: " + e.getMessage());
             Logger.getLogger("pp.eclipse.parse").log(Level.FINER, "Read failure", e);
         } finally {
             if (content != null) {
                 try {
                     content.close();
                 } catch (Exception e) {
                     // Skip
                 }
             }
         }
         return result;
     }
 }
