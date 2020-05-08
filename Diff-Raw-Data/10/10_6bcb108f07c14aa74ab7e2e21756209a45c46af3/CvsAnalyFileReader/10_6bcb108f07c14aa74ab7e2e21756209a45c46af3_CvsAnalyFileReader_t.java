 package cvsanaly;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.hibernate.Session;
 
 //import seminer.ActionMiner;
 import seminer.File;
 import seminer.MinerUtils;
 import seminer.FileReader;
 
 import cvsanaly.Branches;
 import cvsanaly.FileLinks;
 import cvsanaly.FileTypes;
 import cvsanaly.Metrics;
 import cvsanaly.Repositories;
 
 public class CvsAnalyFileReader implements FileReader
 {
 
    private final static String subquery = "SELECT DISTINCT files.id as file_id, "
          + "file_links.file_path as file_path, actions.branch_id as branch_id "
          + "FROM files LEFT OUTER JOIN file_links ON file_links.file_id = files.id "
          + "LEFT OUTER JOIN actions ON actions.file_id = files.id";
    private final static String tagquery = "SELECT DISTINCT tags.name FROM tags "
          + "LEFT OUTER JOIN tag_revisions ON tag_revisions.tag_id = tags.id "
          + "LEFT OUTER JOIN file_links ON file_links.commit_id = tag_revisions.commit_id "
          + "LEFT OUTER JOIN actions ON actions.file_id = file_links.file_id";
 
    private boolean verbose;
    private boolean write;
    private boolean linkOnly;
 
    public boolean isLinkOnly()
    {
       return linkOnly;
    }
 
    public void setLinkOnly(boolean linkOnly)
    {
       this.linkOnly = linkOnly;
    }
 
    public boolean isWrite()
    {
       return write;
    }
 
    public void setWrite(boolean write)
    {
       this.write = write;
    }
 
    public boolean isVerbose()
    {
       return verbose;
    }
 
    public void setVerbose(boolean verbose)
    {
       this.verbose = verbose;
    }
 
    public List<File> parseFile(String projectName)
    {
       Map<Integer, Set<String>> cache = new HashMap<Integer, Set<String>>();
       List<File> fileObjList = new ArrayList<File>();
 
       Session effortMetricsSession = MinerUtils
             .openSession("effortmetrics/effortmetrics_hibernate.cfg.xml");
       effortMetricsSession.beginTransaction();
 
       Session cvsanalySession = MinerUtils.openSession("cvsanaly/cvsanaly_hibernate.cfg.xml");
       cvsanalySession.beginTransaction();
       int repositoryId = 0;
 
       try
       {
         Repositories repository = (Repositories) cvsanalySession.createSQLQuery("Select * FROM repositories WHERE name  LIKE '%" + projectName + "%'").addEntity("repositories",  Repositories.class).uniqueResult();
          repositoryId = repository.getId();
 
          List<FileLinks> result = cvsanalySession
                .createSQLQuery(
                      "SELECT file_links.* from file_links LEFT OUTER JOIN files ON "
                           + "file_links.file_id = files.id" /* LEFT OUTER JOIN file_types ON "
                           + "file_types.file_id = file_links.file_id */ + " WHERE files.repository_id = "
                           + repositoryId) /*+ " AND file_types.type IS NOT NULL")*/
                .addEntity("file_links", FileLinks.class).list();
          for (FileLinks fileLink : result)
          {
             if (!cache.containsKey(fileLink.getFileId()))
             {
                cache.put(fileLink.getFileId(), new HashSet<String>());
             }
             cache.get(fileLink.getFileId()).add(fileLink.getFilePath());
          }
          List<Set<String>> pathSets = new ArrayList<Set<String>>(cache.values());
          for (int i = 0; i < pathSets.size(); i++)
          {
             int j = i + 1;
             while (j < pathSets.size())
             {
                if (!Collections.disjoint(pathSets.get(i), pathSets.get(j)))
                {
                   pathSets.get(i).addAll(pathSets.get(j));
                   pathSets.remove(j);
                }
                else
                {
                   j++;
                }
             }
          }
          for (int i = 0; i < pathSets.size(); i++)
          {
             Set<String> paths = pathSets.get(i);
             System.out.println("Processing " + i + " of " + pathSets.size() + " " + paths);
             for (String string : paths)
             {
                Set<String> usedBranches = new HashSet<String>();
                List<Object[]> results = cvsanalySession
                      .createSQLQuery(
                            "select {file_types.*}, {branches.*} from ("
                                  + subquery
                                  + ") s LEFT OUTER JOIN file_types ON file_types.file_id = s.file_id "
                                  + "LEFT OUTER JOIN branches ON branches.id = s.branch_id WHERE s.file_path = '"
                                  + string + "'").addEntity("file_types", FileTypes.class)
                      .addEntity("branches", Branches.class).list();
                for (Object[] objects : results)
                {
                   FileTypes type = (FileTypes) objects[0];
                   Branches branch = (Branches) objects[1];
 
                   if (branch == null)
                   {
                      if (verbose)
                      {
                         System.out.println("Skipping " + type.getFileId() + " NULL branch");
                      }
                      continue;
                   }
 
                   if (usedBranches.contains(branch.getName()))
                   {
                      if (verbose)
                      {
                         System.out.println("Skipping " + type.getFileId()
                               + " repeat path and branch");
                      }
                      continue;
                   }
                   else
                   {
                      usedBranches.add(branch.getName());
                   }
 
                   if (repository.getType().equals("svn"))
                   {
                      if (string.startsWith("/" + branch.getName()))
                      {
                         string = string.substring(1 + branch.getName().length() + 1);
                      }
                      else
                      {
                         if (verbose)
                         {
                            System.out.println("Skipping mislabeled branch " + branch.getName());
                         }
                         continue;
                      }
                   }
 
                   if (verbose)
                   {
                      System.out.println("Processing " + type.getFileId() + " " + string
                            + " on branch " + branch.getName());
                   }
 
                   String release_number = (String) cvsanalySession.createSQLQuery(
                         tagquery + " where file_links.file_id = " + type.getFileId()
                               + " AND file_links.file_path = '" + string + "'"
                               + " AND actions.branch_id = " + branch.getId()).uniqueResult();
 
                   File effortMetricsFile = new File();
                   effortMetricsFile.setProject_name(projectName);
                   effortMetricsFile.setFile_id(i);
                   effortMetricsFile.setRelative_path(string);
                   effortMetricsFile.setFile_type(type.getType());
                   effortMetricsFile.setBranch_name(branch.getName());
                   effortMetricsFile.setRelease_number(release_number);
 
                   List<Metrics> metricsList = cvsanalySession
                         .createSQLQuery(
                               "SELECT s.*, max(date) FROM (SELECT * FROM metrics WHERE file_id = '"
                                     + type.getFileId()
                                     + "') s LEFT OUTER JOIN scmlog ON scmlog.id = s.commit_id")
                         .addEntity("s", Metrics.class).list();
                   if (metricsList.size() > 0 && metricsList.get(0) != null)
                   {
                      Metrics metrics = metricsList.get(0);
                      effortMetricsFile.setRaw_LOC(metrics.getLoc());
                      effortMetricsFile.setMccabe_max(metrics.getMccabeMax());
                      effortMetricsFile.setMccabe_mean(metrics.getMccabeMean());
                      effortMetricsFile.setMccabe_median(metrics.getMccabeMedian());
                      effortMetricsFile.setMccabe_min(metrics.getMccabeMin());
                      effortMetricsFile.setMccabe_sum(metrics.getMccabeSum());
                      effortMetricsFile.setHalstead_length(metrics.getHalsteadLength());
                      effortMetricsFile
                            .setHalstead_level((metrics.getHalsteadLevel() != null) ? Integer
                                  .valueOf(metrics.getHalsteadLevel().intValue()) : null);
                      effortMetricsFile.setHalstead_md(metrics.getHalsteadMd());
                      effortMetricsFile.setHalstead_vol(metrics.getHalsteadVol());
                   }
 
                   fileObjList.add(effortMetricsFile);
                }
             }
          }
       }
       finally
       {
          MinerUtils.commitAndCloseSession(cvsanalySession);
          MinerUtils.commitAndCloseSession(effortMetricsSession);
       }
 
       return fileObjList;
    }
 
    private boolean isTableDirty(Session s, String projectName)
    {
       List results = s.createQuery("FROM File WHERE project_name = '" + projectName + "'").list();
       if (results.size() > 0)
       {
          return false;
       }
       else
       {
          return true;
       }
    }
 
 }
