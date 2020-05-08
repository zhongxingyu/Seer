 package org.lastbamboo.common.amazon.s3;
 
 import org.junit.Test;
 import org.lastbamboo.common.amazon.stack.AwsUtils;
import org.littleshoot.util.SecurityUtils;

 
 public class AmazonS3LauncherTest
     {
 
    @Test public void testLaunching() throws Exception
         {
         if (!AwsUtils.hasPropsFile()) 
             {
             return;
             }
         Launcher.main(new String[] {"-h"});
         //System.out.println("\n\n");
         
         //System.out.println(SystemUtils.USER_DIR);
         Launcher.main(new String[] {"-ls", "littleshoot"});
         //Launcher.main(new String[] {"-p bucket test"});
         }
     }
