 package scho;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 public class Git {
 
     public void gitLog(Jena J, boolean gitLog)
     {
         String CSid = null;
         String tmpP = null;
         String message = null;
         String date = null;
         String err = null;
         ChangeSet CS = null;
         Site S = null;
         PullFeed PF = null;
         Integer count=0;
         String cmd;
         
         if (gitLog)
         {
            cmd = "git log --abbrev-commit --parents  --pretty=format:%h%n%p%nci";
 
         }
         else
         {
             cmd = "/usr/local/bin/hg log  --debug --template {rev}:{node}\\n{parents}\\n{date|isodatesec}\\n";
         }
  
         try
         {
             Process p = Runtime.getRuntime().exec(cmd);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             while ((CSid = stdInput.readLine()) != null)
             {
                 count++;
                 if (!gitLog) CSid=CSid.split(":")[1];
 
                 CS=new ChangeSet("CS"+CSid);
                 
                 if ((tmpP = stdInput.readLine()) !=null)
                 {
                     String[] parents;
                     parents=tmpP.split(" ");
                     for (int i =0; i<parents.length; i++)
                     {
                         if (parents[i].equals("-1:0000000000000000000000000000000000000000")) parents[i]="";
 
                         if (!parents[i].isEmpty()) 
                         {
                             if (!gitLog) parents[i]=parents[i].split(":")[1];
                             CS.addPreviousChgSet("CS" + parents[i]);
                         }
                     }
 
                     if (tmpP.isEmpty())
                     {
                         String site="S"+CS.getChgSetID().substring(2);
                         S = new Site(site);
                         J.addSite(S);
                     }
 
                     if (CS.getPreviousChgSet().size()==2)
                     {
                         // pull feed
                         String site="S"+parents[1];
                         S = new Site(site);
                         J.addSite(S);
                         PF= new PullFeed("F"+parents[1]);
                         PF.setHeadPullFeed("CS"+parents[1]);
                         PF.setSite(S.getSiteID());
                         J.addPullFeed(PF);
                         //J.setPullFeed(CS, PF);
                     }
                 }
 
                 if ((date = stdInput.readLine()) !=null)
                 {
                     Date D;
                     try {
                         SimpleDateFormat sdf1 = new SimpleDateFormat(Main.dateFormatLog);
                         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
                         D = sdf1.parse(date);
                         SimpleDateFormat sdf2= new SimpleDateFormat(Main.dateFormatJena);
                         sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
                         date = sdf2.format(D);
                         CS.setDate(date);
                     } catch (ParseException ex) {
                         Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                 J.addChangeSet(CS);
             }
             while ((err = stdError.readLine()) != null)
             {
                 System.out.print("Error :");
                 System.out.println(err);
             }
             
         }
         catch (IOException ex)
         {
             Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
 /**
     public String gitGetFirstCS()
     {
         String out = null;
         String err=null;
         try {
            
             String cmd1="git log -n 1 --abbrev-commit --pretty=format:%h";
             
             Process p = Runtime.getRuntime().exec(cmd1);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             out = stdInput.readLine();
 
             while ((err = stdError.readLine()) != null) {
                 System.out.print("Error :");
                 System.out.println(err);
             }
         } catch (IOException ex) {
             Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
         }
         return out;
     }
 
     public ChangeSet gitGetCSdata(String CS)
     {
         ArrayList<String> out = new ArrayList<String>();
         String tmp = null;
         String err = null;
         ChangeSet CS1=new ChangeSet();
         try {
            
             String cmd1 = "git show --abbrev-commit --parents --format=%h%n%p%n%s" +CS;
             Process p = Runtime.getRuntime().exec(cmd1);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             while ((tmp = stdInput.readLine()) != null) {
                 out.add(tmp);
                 
             }
 
             CS1.setChgSetID(out.get(0));
 
             String[] tmpP;
             String parents=out.get(1);
             tmpP=parents.split(" ");
             for (int i =0; i<tmpP.length; i++)
             {
                 System.out.println(tmpP[i]);
             }
             while ((err = stdError.readLine()) != null) {
                 System.out.print("Error :");
                 System.out.println(err);
             }
         } catch (IOException ex) {
             Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
         }
         return CS1;
     }
 
 
  public void config() throws IOException, JavaGitException
     {
         JavaGitConfiguration.setGitPath(GitBin);
     }
 
     public void getVersion() throws JavaGitException
     {
 
         System.out.println("Git version : "+JavaGitConfiguration.getGitVersion());
 
     }
 
     public void getLog() throws JavaGitException, IOException
     {
         File repositoryDirectory = new File("/Users/klm/code/cakephp");
         DotGit dotGit = DotGit.getInstance(repositoryDirectory);
 
         System.out.println("Log size = "+dotGit.getLog().size())
                 ;
 
          //Print commit messages of the current branch
 
         //for (Commit c : dotGit.getLog()) {
 
         GitLogOptions options=new GitLogOptions();
         options.setOptOrderingReverse(true);
         options.setOptLimitFirstParent(true);
         options.setOptLimitFullHistory(true);
         //options.set
 
             Commit c= dotGit.getLog(options).get(0);
             System.out.println(c.getMessage());
             System.out.println(c.getAuthor());
             System.out.println(c.getDateString());
             System.out.println(c.getFiles());
             System.out.println(c.getFilesChanged());
             System.out.println(c.getLinesDeleted());
             System.out.println(c.getLinesInserted());
 
             if (c.getMergeDetails()!= null)
             {
                 Iterator i = c.getMergeDetails().iterator();
                 while (i.hasNext())
                 {
                     String MD = i.next().toString();
                     System.out.println("Merge Detail ----"+MD);
                 }
             }
 
         //}
     }
 
 
     public void gitLogNoMerge(Jena J)
     {
         String cmd1="git log --abbrev-commit --parents --no-merges --format=%h%n%p%n%s%n%ci";
         String CSid = null;
         String parent = null;
         String message = null;
         String date=null;
         String err = null;
         ChangeSet CS =null;
 
         try
         {
             Process p = Runtime.getRuntime().exec(cmd1);
             BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
             BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
             while ((CSid = stdInput.readLine()) != null)
             {
                 CS=new ChangeSet("CS"+CSid);
                 if ((parent = stdInput.readLine()) !=null)
                 {
                         if ((!parent.isEmpty())) CS.addPreviousChgSet("CS"+parent);
                 }
 
                 if ((message = stdInput.readLine()) !=null)
                 {
                     CS.setMessage(message);
                 }
                 if ((date = stdInput.readLine()) !=null)
                 {
                     Date D;
                     try {
                         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
                         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
                         D = sdf1.parse(date);
                         SimpleDateFormat sdf2= new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
                         sdf2.setTimeZone(TimeZone.getTimeZone("GMT"));
                         date = sdf2.format(D);
                         CS.setDate(date);
                     } catch (ParseException ex) {
                         Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
                 J.addChangeSet(CS);
             }
 
             while ((err = stdError.readLine()) != null)
             {
                 System.out.print("Error :");
                 System.out.println(err);
             }
 
         }
         catch (IOException ex)
         {
             Logger.getLogger(Git.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 */
 
 
 }
