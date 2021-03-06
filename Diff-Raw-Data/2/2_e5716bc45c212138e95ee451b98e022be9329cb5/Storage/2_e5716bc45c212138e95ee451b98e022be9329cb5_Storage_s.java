 package org.carlspring.strongbox.storage;
 
 import com.thoughtworks.xstream.annotations.XStreamAlias;
 import org.carlspring.strongbox.storage.repository.Repository;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 /**
  * @author mtodorov
  */
 public class Storage
 {
 
     @XStreamAlias(value = "name")
     private String name;
 
     @XStreamAlias(value = "basedir")
     private String basedir;
 
     @XStreamAlias(value = "repositories")
     private Map<String, Repository> repositories = new LinkedHashMap<String, Repository>();
 
 
     public Storage()
     {
     }
 
     public Storage(String name, String basedir)
     {
         this.name = name;
         this.basedir = basedir;
     }
 
     public boolean containsRepository(String repository)
     {
         return getRepositories().containsKey(repository);
     }
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getBasedir()
     {
         return basedir;
     }
 
     public void setBasedir(String basedir)
     {
         this.basedir = basedir;
     }
 
     public Map<String, Repository> getRepositories()
     {
         return repositories;
     }
 
     public void setRepositories(Map<String, Repository> repositories)
     {
         this.repositories = repositories;
     }
 
     public void addRepository(Repository repository)
     {
         repositories.put(repository.getName(), repository);
     }
 
     public Repository getRepository(String repository)
     {
         return repositories.get(repository);
     }
 
     public void removeRepository(Repository repository)
     {
         repositories.remove(repository.getName());
     }
 
     public boolean hasRepositories()
     {
        return repositories.size() > 0;
     }
 
 }
