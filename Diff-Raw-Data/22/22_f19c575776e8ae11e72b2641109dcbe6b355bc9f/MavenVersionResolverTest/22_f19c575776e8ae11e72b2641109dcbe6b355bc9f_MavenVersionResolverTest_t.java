 package org.kevoree.resolver;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * User: Erwan Daubert - erwan.daubert@gmail.com
  * Date: 18/06/13
  * Time: 14:02
  *
  * @author Erwan Daubert
  * @version 1.0
  */
 public class MavenVersionResolverTest {
 
     @Test
     public void toDrop() {
         MavenResolver resolver = new MavenResolver();
         Set<String> urls = new HashSet<String>();
         urls.add("http://repo1.maven.org/maven2/");
       // urls.add("https://oss.sonatype.org/content/groups/public/");
 
       // File kevoreeBase = resolver.resolve("org.kevoree.platform", "org.kevoree.platform.standalone", "latest", "jar", urls);
    //    System.out.println(kevoreeBase.getAbsolutePath());
 
 
         Set<String> versions = resolver.listVersion("org.kevoree.platform", "org.kevoree.platform.standalone", "jar", urls);
         for(String s : versions){
             System.out.println(">"+s);
         }
     }
 
 
     @Test
     public void testfoundMaxVersion() throws IOException {
         MavenResolver resolver = new MavenResolver();
 
         HashSet<String> sona = new HashSet<String>();
         sona.add("http://oss.sonatype.org/content/groups/public");
         File result = resolver.resolve("org.kevoree", "org.kevoree.core", "RELEASE", "jar", sona);
         System.out.println(result.getAbsolutePath());
         Assert.assertTrue("RELEASE", !result.getAbsolutePath().contains("SNAPSHOT"));
 
         HashSet<String> central = new HashSet<String>();
         central.add("http://repo1.maven.org/maven2");
 
         File result2 = resolver.resolve("org.kevoree", "org.kevoree.core", "RELEASE", "jar", central);
         System.out.println(result2.getAbsolutePath());
         Assert.assertTrue("RELEASE", !result2.getAbsolutePath().contains("SNAPSHOT"));
 
 
         File result3 = resolver.resolve("org.kevoree", "org.kevoree.core", "LATEST", "jar", sona);
         System.out.println(result3.getCanonicalPath());
         Assert.assertTrue("SNAPSHOT", result3.getAbsolutePath().contains("SNAPSHOT"));
 
         /*
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
        // artifact = resolver.foundMaxVersion(artifact, "http://oss.sonatype.org/content/groups/public", false, true);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals(null, artifact.getVersion());
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
       //  artifact = resolver.foundMaxVersion(artifact, "http://maven.kevoree.org/release", false, false);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
       //  artifact = resolver.foundMaxVersion(artifact, "http://oss.sonatype.org/content/groups/public", false, false);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals("2.0.0-SNAPSHOT", artifact.getVersion());
 
         // local resolution => is it possible ?
         /*artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
         artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, true);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
         artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, true);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals(null, artifact.getVersion());
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
         artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, false);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals("2.0.0-ALPHA", artifact.getVersion());
 
         artifact = new MavenArtefact();
         artifact.setGroup("org.kevoree");
         artifact.setName("org.kevoree.core");
         artifact = resolver.foundMaxVersion(artifact, System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository", true, false);
 
         System.out.println(artifact.getVersion());
         Assert.assertEquals("2.0.0-SNAPSHOT", artifact.getVersion());*/
     }
 
 }
