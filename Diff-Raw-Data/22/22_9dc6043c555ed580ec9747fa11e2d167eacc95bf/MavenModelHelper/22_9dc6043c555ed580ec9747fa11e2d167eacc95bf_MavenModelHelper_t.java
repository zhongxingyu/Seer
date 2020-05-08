 package hornetq_project_gen;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.DependencyManagement;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Repository;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 
 public abstract class MavenModelHelper {
    protected File repoBase;
    protected File sourceRoot;
    protected File pom;
    protected Model model;
    protected List<String> ignoredLinks = new ArrayList<String>();
    protected List<String> specialLinks = new ArrayList<String>();
    protected GitHelper gitHelper;
    protected Map<String, Dependency> excludedDeps = new HashMap<String, Dependency>();
 
    public MavenModelHelper(File sourceRoot, File pomFile, File repoBase)
          throws IOException, XmlPullParserException {
       this.repoBase = repoBase;
       this.sourceRoot = sourceRoot;
       this.pom = pomFile;
       MavenXpp3Reader reader = new MavenXpp3Reader();
       model = reader.read(new FileReader(pom));
       gitHelper = new GitHelper(sourceRoot);
    }
 
    /*
     * Using projects version string
     */
    public String generateProjectName() throws IOException {
       String version = model.getVersion();
       String branch = gitHelper.getBranch();
       if (version != null) {
          return "HQ-" + version + "<" + branch + ">";
       }
       return "HornetQ";
    }
 
    public Model getModel() {
       return model;
    }
 
    /**
     * search java sources under source root. for example
     * src_root/hornetq-server/src/main/java will be loaded as a linked folder,
     * folder name is "hornetq-server".
     * 
     * @param project
     * 
     * @param sources
     */
    public void getProjectSources(IProject project, Map<String, IClasspathEntry> cpList) {
       HornetQSourceFilter filter = new HornetQSourceFilter();
       sourceRoot.listFiles(filter);
       // now linke them
       Iterator<SourceEntry> entries = filter.sources.values().iterator();
       while (entries.hasNext()) {
          SourceEntry ent = entries.next();
          IFolder folder = project.getFolder(ent.name);
          String sourceUrl = ent.sourceLocation.getAbsolutePath();
          IPath target = Path.fromOSString(sourceUrl);
          try {
             folder.createLink(target, IResource.ALLOW_MISSING_LOCAL, null);
          } catch (CoreException e) {
             throw new IllegalStateException("Error: " + e);
          }
          IClasspathEntry newPath = JavaCore
                .newSourceEntry(folder.getFullPath());
          cpList.put(folder.getFullPath().toOSString(), newPath);
       }
    }
 
    public abstract void findMissingEntries(IProject project,
          Map<String, IClasspathEntry> sources) throws CoreException;
 
    private boolean ignoredUrl(String path) {
       for (String key : ignoredLinks) {
          if (path.indexOf(key) != -1) {
             // check special include
             for (String k : specialLinks) {
                if (path.indexOf(k) != -1) {
                   return false;
                }
             }
             return true;
          }
       }
       return false;
    }
 
    protected void addExtraDependencies(List<Dependency> deps) {
       
    }
    
    protected boolean isExcluded(Dependency d) {
       return excludedDeps.containsKey(getKey(d));
    }
    
    protected String getKey(Dependency d) {
       return d.getGroupId() + d.getArtifactId() + d.getVersion();
    }
 
    protected void addExcludedDep(String gid, String aid, String ver) {
       Dependency d = new Dependency();
       d.setGroupId(gid);
       d.setArtifactId(aid);
       d.setVersion(ver);
       excludedDeps.put(getKey(d), d);
    }
 
    public void getDependencies(Map<String, IClasspathEntry> sources) {
       DependencyManagement manager = model.getDependencyManagement();
       List<Dependency> dependencies = manager.getDependencies();

      List<String> modules = model.getModules();
       
       addExtraDependencies(dependencies);
 
       for (Dependency d : dependencies) {
          if (isExcluded(d)) continue;
          
          String gid = d.getGroupId();
          String partialPath = gid.replace('.', '/');
          String artId = d.getArtifactId();
          String version = d.getVersion();
          String sysPath = d.getSystemPath();
 
         System.out.println("dep location: " + d.getSystemPath());

          version = this.resolveVar(version);
 
          if (sysPath != null) {
             sysPath = this.resolveVar(sysPath);
             File localFile = new File(sysPath);
             if (!localFile.exists()) {
                System.err.println("Invalid location "
                      + localFile.getAbsolutePath());
             } else {
                String key = localFile.getAbsolutePath();
                if (!sources.containsKey(key)) {
                   IPath jarPath = Path.fromOSString(key);
                   IClasspathEntry entry = JavaCore.newLibraryEntry(jarPath, null,
                         null, false);
                   sources.put(key, entry);
                }
             }
          } else {
             System.out.println("find dep: " + partialPath + " artid: " + artId
                   + " ver: " + version);
             File file1 = new File(repoBase, partialPath);
             File file2 = new File(file1, artId);
             File file3 = new File(file2, version);
             if (!file3.exists()) {
                System.err
                      .println("Invalid location " + file3.getAbsolutePath());
             } else {
                File[] jars = file3.listFiles(new FilenameFilter() {
                   @Override
                   public boolean accept(File dir, String name) {
                      return name.endsWith(".jar");
                   }
                });
 
                for (File j : jars) {
                   String key = j.getAbsolutePath();
                   if (!sources.containsKey(key)) {
                      IPath jarPath = Path.fromOSString(j.getAbsolutePath());
                      IClasspathEntry entry = JavaCore.newLibraryEntry(jarPath,
                            null, null, false);
                      sources.put(key, entry);
                   }
                }
             }
          }
       }
    }
 
    private String resolveVar(String var) {
       String newVar = var;
       int inta = var.indexOf("${");
       if (inta != -1) {
          int intb = var.indexOf("}");
          String key = var.substring(inta + 2, intb);
          String value = model.getProperties().getProperty(key);
          if (value == null) {
             if ("project.version".equals(key)) {
                value = model.getVersion();
             } else {
                value = System.getProperty(key);
                if (value == null) {
                   throw new IllegalStateException("unknown property: " + key);
                }
             }
          }
          newVar = var.substring(0, inta) + value + var.substring(intb + 1);
          newVar = resolveVar(newVar);
       }
       return newVar;
    }
 
    private void dumpMavenModel(Model m) {
       System.out.println("artifact id: " + m.getArtifactId());
       System.out.println("netty version: "
             + m.getProperties().get("netty.version"));
       List<Dependency> deps = m.getDependencyManagement().getDependencies();
 
       for (Dependency d : deps) {
          System.out.println("dep: " + d.getArtifactId() + " version "
                + d.getVersion());
       }
 
       List<Repository> repo = m.getRepositories();
       for (Repository r : repo) {
          System.out.println("repo: " + r);
       }
    }
 
    private class HornetQSourceFilter implements FileFilter {
       private Set<String> excludedPath = new HashSet<String>();
       public Map<String, SourceEntry> sources = new HashMap<String, SourceEntry>();
       public Map<String, SourceEntry> sourceNames = new HashMap<String, SourceEntry>();
       public Map<SourceEntry, String> debugSources = new HashMap<SourceEntry, String>();
 
       public HornetQSourceFilter() {
          excludedPath.add(".settings");
       }
 
       @Override
       public boolean accept(File pathname) {
          if (!validSourcePath(pathname)) {
             return false;
          }
          try {
             resolveSourcePath(pathname);
          } catch (IOException e) {
             e.printStackTrace();
          }
          return false;
       }
 
       private boolean validSourcePath(File p) {
          if (p.isDirectory()) {
             if (excludedPath.contains(p.getName())) {
                return false;
             }
             return true;
          }
          return false;
       }
 
       /**
        * parse a possible source dir. for example
        * ./hornetq-server/src/main/java/.../*.java
        * ./hornetq-server/src/main/resources/...
        * ./hornetq-server/src/test/java/..../*.java
        * ./hornetq-server/src/test/resources/...
        * 
        * will map to classpath:
        * 
        * horneq-server |_main ---> <./hornetq-server/src/main/java> |_test --->
        * <./hornetq-server/src/test/java>
        * 
        * resources are ignored for the moment
        * 
        * @throws IOException
        */
       private void resolveSourcePath(File potentialDir) throws IOException {
          findJavaFiles(potentialDir);
       }
 
       private void findJavaFiles(File dir) throws IOException {
          File[] allFiles = dir.listFiles();
          for (File f : allFiles) {
             if (f.isDirectory()) {
                findJavaFiles(f);
             }
             String path = f.getName();
             if (path.endsWith(".java")) {
                updateEntry(f);
             }
          }
       }
 
       private void updateEntry(File javaFile) throws IOException {
          String filePath = javaFile.getAbsolutePath();
          if (ignoredUrl(filePath)) {
             return;
          }
          // read package file
          String pkgName = getPackageName(javaFile);
          File sourcePath = javaFile.getParentFile();
          if (!"".equals(pkgName)) {
             String[] segments = pkgName.split("\\.");
             for (int i = segments.length - 1; i >= 0; i--) {
                if (sourcePath.getName().equals(segments[i])) {
                   sourcePath = sourcePath.getParentFile();
                } else {
                   throw new IllegalStateException("Invalid location: "
                         + javaFile.getAbsolutePath());
                }
             }
          }
          // now sourcePath is like src_root/hornetq-server/src/main/java
          String absPath = sourcePath.getAbsolutePath();
          SourceEntry entry = sources.get(absPath);
          if (entry == null) {
             // calculate the source folder name
             String rootPath = sourceRoot.getAbsolutePath();
             String thePath = sourcePath.getAbsolutePath().replace(rootPath, "")
                   .trim();
             String name = getMeaningfulFolderName(thePath);
             entry = new SourceEntry(name, sourcePath);
 
             if (debugSources.containsKey(entry)) {
                System.out.println("hey there is the entry already: " + entry + " for " + debugSources.get(entry));
                System.out.println("now the one: " + absPath);
             } else {
                sources.put(absPath, entry);
                debugSources.put(entry, absPath);
                sourceNames.put(name, entry);
                
             }
          }
       }
 
       // Path is like /hornetq-server/src/main/java
       private String getMeaningfulFolderName(String thePath) {
 
          String[] elements = thePath.split("/");
 
          String base = null;
          for (int i = 0; i < elements.length; i++) {
             if (elements[i].trim().equals("")) {
                continue;
             }
             if (base == null) {
                base = elements[i];
             } else if (base.equals("tests")) {
                base = elements[i];
                break;
             } else if (elements[i].equals("main")) {
                base = base + "-main";
                break;
             } else if (elements[i].equals("test")) {
                base = base + "-test";
                break;
             }
          }
 
          int i = 0;
          String realPath = base;
          while (sourceNames.containsKey(realPath)) {
             realPath = base + i;
             i++;
          }
 
          return realPath;
       }
 
       private String getPackageName(File javaFile) throws IOException {
          String pkgName = "";
          BufferedReader reader = null;
          try {
             reader = new BufferedReader(new FileReader(javaFile));
             String line = reader.readLine();
             while (line != null) {
                String trimmed = line.trim();
                int index = trimmed.indexOf("package ");
 
                if (index != -1) {
                   int last = trimmed.lastIndexOf(";");
                   if (last != -1) {
                      pkgName = trimmed.substring(index + 8, last);
                      break;
                   }
                }
                line = reader.readLine();
             }
          } finally {
             reader.close();
          }
          return pkgName;
       }
    }
 
    private class SourceEntry {
       public String name;
       public File sourceLocation;
 
       public SourceEntry(String name, File sourceLocation) {
          this.name = name;
          this.sourceLocation = sourceLocation;
       }
    }
 
    public void copyResources(IProject project, Map<String, IClasspathEntry> sources)
          throws CoreException {
       // TODO Auto-generated method stub
 
    }
 
    public String getPom() {
       StringBuilder builder = new StringBuilder();
       BufferedReader reader = null;
       try {
          reader = new BufferedReader(new FileReader(pom));
          String line = reader.readLine();
          while (line != null) {
             builder.append(line);
             builder.append("\n");
             line = reader.readLine();
          }
       } catch (FileNotFoundException e) {
          builder.append(e.getMessage());
       } catch (IOException e) {
          builder.append(e.getMessage());
       } finally {
          if (reader != null) {
             try {
                reader.close();
             } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
             }
          }
       }
       return builder.toString();
    }
    
    protected void copySingleFileToFolder(String path, IFolder folder) throws CoreException {
       File srcFile = new File(sourceRoot, path);
       String fileName = path.substring(path.lastIndexOf("/") + 1);
       IFile newFile = folder.getFile(fileName);
       
       try {
              newFile.create(new FileInputStream(srcFile), false, null);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }  
    }
 
    protected void addSingleClassFromTargetDir(String path, IFolder parent) throws CoreException {
 
       final String keyStr = "target/classes/";
       int start = path.indexOf(keyStr);
       int end = path.lastIndexOf("/");
       String fileName = path.substring(end + 1);
       
       String pkgPath = path.substring(start + keyStr.length(), end);
       String[] segs = pkgPath.split("/");
       
       IFolder folder = parent;
       for (String s : segs) {
          IFolder folder1 = folder.getFolder(s);
          if (!folder1.exists()) {
             folder1.create(false,  true,  null);
          }
          folder = folder1;
       }
 
       File srcFile = new File(sourceRoot, path);
       IFile newFile = folder.getFile(fileName);
       try {
           newFile.create(new FileInputStream(srcFile), false, null);
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       }
 
    }
 
    protected static void addDep(List<Dependency> deps, String groupId, String artId, String ver) {
       Dependency dd = new Dependency();
       dd.setGroupId(groupId);
       dd.setArtifactId(artId);
       dd.setVersion(ver);
       deps.add(dd);
    }
 }
