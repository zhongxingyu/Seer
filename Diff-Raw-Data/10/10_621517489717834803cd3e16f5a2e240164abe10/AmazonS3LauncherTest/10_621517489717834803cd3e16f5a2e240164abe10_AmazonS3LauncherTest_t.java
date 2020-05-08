 package org.lastbamboo.common.amazon.s3;
 
import org.junit.Ignore;
 import org.junit.Test;
 import org.lastbamboo.common.amazon.stack.AwsUtils;
 
// Ignoring because props files are not always present.
@Ignore
 public class AmazonS3LauncherTest
     {
 
    @Test public void testLaunching() 
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
