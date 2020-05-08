 package edu.kit.checkstyle.checks;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
 import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
 
 
 public class ExitUsageCheckTest extends BaseCheckTestSupport {
 
   private final DefaultConfiguration config = createCheckConfig(ExitUsageCheck.class);
 
   @Test
   public void noErrorInMainMethod() throws Exception {
     config.addAttribute("checkedMethods", "main:System.exit");
     test(config, "noErrorInMainMethod", NO_ERR);
   }
 
   @Test
   public void errorOutsideOfMainMethod() throws Exception {
     config.addAttribute("checkedMethods", "main:System.exit");
     test(config, "errorOutsideOfMainMethod", Arrays.asList(errAt(7, 12)));
   }
 }
