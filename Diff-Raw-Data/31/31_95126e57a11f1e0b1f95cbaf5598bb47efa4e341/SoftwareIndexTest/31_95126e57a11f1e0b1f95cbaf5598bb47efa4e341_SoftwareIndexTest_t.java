 package org.jenkinsci.plugins.jobprofiles;
 
 import lombok.extern.slf4j.Slf4j;
 import net.oneandone.sushi.fs.World;
 import org.junit.Test;
 
 
 @Slf4j
 public class SoftwareIndexTest {
     @Test
     public void getAssets() throws Exception {
         World world;
         SoftwareIndex foo;
 
         world = new World();
 
         foo = SoftwareIndex.load(world.node("svn:https://github.com/maxbraun/job-profiles/trunk/src/main/resources/softreg.xml"));
         SoftwareIndexTest.log.info(foo.toString());
     }
 
 
 }
