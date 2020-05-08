 package org.jenkinsci.plugins.jobprofiles;
 
 import net.oneandone.sushi.fs.World;
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 public class ScmTest {
 
     private final String svn = "https://pustefix.svn.sourceforge.net/svnroot/pustefix/trunk";
     private final String git = "https://github.com/mlhartme/sushi.git";
     private final World world = new World();
 
     @Test
     public void getGitFromScmFactory() throws Exception {
         Scm scm;
         List<String> gitURLs;
 
         gitURLs = new ArrayList<String>();
         //gitURLs.add("ssh://user@host.xz:0/path/to/repo.git/");
         //gitURLs.add("ssh://user@host.xz/path/to/repo.git/");
         //gitURLs.add("ssh://host.xz:port/path/to/repo.git/");
         //gitURLs.add("ssh://host.xz/path/to/repo.git/");
         //gitURLs.add("ssh://user@host.xz/path/to/repo.git/");
         //gitURLs.add("ssh://host.xz/path/to/repo.git/");
         //gitURLs.add("ssh://user@host.xz/~user/path/to/repo.git/");
         //gitURLs.add("ssh://host.xz/~user/path/to/repo.git/");
         //gitURLs.add("ssh://user@host.xz/~/path/to/repo.git");
         //gitURLs.add("ssh://host.xz/~/path/to/repo.git");
         //gitURLs.add("user@host.xz:/path/to/repo.git/");
         //gitURLs.add("host.xz:/path/to/repo.git/");
         //gitURLs.add("user@host.xz:~user/path/to/repo.git/");
         //gitURLs.add("host.xz:~user/path/to/repo.git/");
         //gitURLs.add("user@host.xz:path/to/repo.git");
         //gitURLs.add("host.xz:path/to/repo.git");
         //gitURLs.add("rsync://host.xz/path/to/repo.git/");
         //gitURLs.add("git://host.xz/path/to/repo.git/");
         //gitURLs.add("git://host.xz/~user/path/to/repo.git/");
         //gitURLs.add("http://host.xz/path/to/repo.git/");
         //gitURLs.add("https://host.xz/path/to/repo.git/");
         //gitURLs.add("/path/to/repo.git/");
         //gitURLs.add("path/to/repo.git/");
         //gitURLs.add("~/path/to/repo.git");
         //gitURLs.add("file:///path/to/repo.git/");
         //gitURLs.add("file://~/path/to/repo.git/");
         gitURLs.add(git);
         for (String gitUrl : gitURLs) {
             scm = Scm.get(gitUrl, world);
             Assert.assertTrue(scm instanceof ScmGit);
         }
     }
 
     @Test
     public void getSVNFromScmFactory() throws Exception {
         Scm scm;
         scm = Scm.get(svn, world);
         Assert.assertTrue(scm instanceof ScmSVN);
     }
 
     @Test
     public void getPomfromSCMS() throws Exception {
         getPom(svn);
         getPom(git);
     }
 
     private void getPom(String scmLocation) {
         Scm scm;
         scm = Scm.get(scmLocation, world);
 
         Assert.assertTrue(scm.getPom().length() > 1);
     }
 
     @Test(expected = JobProfileException.class)
     public void WrongSVNUrl() throws Exception {
         Scm.get(svn + "/", world).getPom();
     }
 
     @Test(expected = JobProfileException.class)
     public void WrongGitUrl() throws Exception {
         Scm.get("http:ajdlfjlsdjfö.git", world).getPom();
     }
 
     @Test(expected = JobProfileException.class)
     public void GitRepoWithoutPom() throws Exception {
         Scm.get("git@github.com:maxbraun/puppet-phantomjs.git", world).getPom();
     }
 
     @Test
     public void GitProfile() throws Exception {
         getProfile("https://github.com/maxbraun/job-profiles-examles.git", "git-maven");
     }
 
     @Test
     public void SvnProfile() throws Exception {
         getProfile("https://github.com/maxbraun/job-profiles-examles/trunk", "git-maven");
     }
 
     private void getProfile(String scmUrl, String profileName) throws Exception {
         Scm scm;
         scm = Scm.get(scmUrl, world);
         Map<String, String> profile;
 
         profile = scm.getProfile(profileName);
 
         Assert.assertTrue(profile.size() >= 1);
     }
 }
