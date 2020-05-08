 package org.nuxeo.ide.connect;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.ui.PlatformUI;
 import org.nuxeo.ide.common.IOUtils;
 import org.nuxeo.ide.common.UI;
 
 public class RepositoryManager implements IBindingListener, IStudioListener {
 
     protected final File root;
 
     protected static class Entry {
         protected final String projectId;
 
         protected final File file;
 
         Entry(String projectId, File file) {
             this.projectId = projectId;
             this.file = file;
         }
 
         @Override
         public String toString() {
             return file.getName();
         }
 
         public void download() {
             try {
                 PlatformUI.getWorkbench().getActiveWorkbenchWindow().run(false,
                         true, new RepositoryDownloadTask(this));
             } catch (Exception e) {
                 UI.showError("Download of " + this + " was canceled", e);
             }
         }
     }
 
     protected final Map<String, Entry> entries = new HashMap<String, Entry>();
 
     public RepositoryManager(File root) {
         this.root = new File(root, "repository");
     }
 
     public void handleNewBinding(StudioProjectBinding binding) {
         for (String projectId : binding.getProjectIds()) {
             Entry entry = entries.get(projectId);
             if (!entries.containsKey(projectId)) {
                 entry = new Entry(projectId, new File(root, projectId
                         + "-0.0.0-SNAPSHOT.jar"));
                 entry.download();
                 entries.put(projectId, entry);
             }
         }
     }
 
     public void reload() {
         if (!root.exists()) {
             root.mkdirs();
         } else {
             for (File file : root.listFiles()) {
                 String name = file.getName();
                 int suffixIndex = name.lastIndexOf("-0.0.0-SNAPSHOT.jar");
                 String projectId = name.substring(0, suffixIndex);
                 Entry entry = new Entry(projectId, file);
                 entries.put(projectId, entry);
             }
         }
     }
 
     public void garbage(Set<StudioProjectBinding> bindings) {
         Set<String> projectIds = new HashSet<String>();
         for (StudioProjectBinding binding : bindings) {
             projectIds.addAll(Arrays.asList(binding.getProjectIds()));
         }
         Iterator<Entry> it = entries.values().iterator();
         while (it.hasNext()) {
             Entry entry = it.next();
             if (projectIds.contains(entry.projectId)) {
                 entry.file.delete();
                 it.remove();
             }
         }
     }
 
     public void dispose() {
         entries.clear();
     }
 
     public File getFile(String id) {
         return entries.get(id).file;
     }
 
     @Override
     public void handleProjectsUpdate(StudioProvider provider) {
         for (Entry entry : entries.values()) {
             entry.download();
         }
     }
 
     public void erase() {
         IOUtils.deleteTree(root);
         root.mkdirs();
         entries.clear();
     }
 }
