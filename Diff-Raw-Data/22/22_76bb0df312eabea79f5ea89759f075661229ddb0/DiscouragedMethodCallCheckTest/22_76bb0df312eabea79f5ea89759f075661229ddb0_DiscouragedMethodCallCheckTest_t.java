 package edu.kit.checkstyle.checks;
 
 import java.util.Arrays;
 
 import org.junit.Test;
 
 import com.puppycrawl.tools.checkstyle.BaseCheckTestSupport;
 import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
 
 
 public class DiscouragedMethodCallCheckTest extends BaseCheckTestSupport {
 
   private final DefaultConfiguration config = createCheckConfig(DiscouragedMethodCallCheck.class);
 
   @Test
   public void noErrorInCheckIfNoMethodsArePassed() {
     config.addAttribute("checkedMethods", "");
     test(config, "noErrorInMainMethod", NO_ERR);
   }
 
   @Test
   public void noErrorInMainMethod() throws Exception {
     config.addAttribute("checkedMethods", "main:System.exit");
     test(config, NO_ERR);
   }
 
   @Test
   public void errorOutsideOfMainMethod() throws Exception {
     config.addAttribute("checkedMethods", "main:System.exit");
     test(config, Arrays.asList(errAt(7, 12)));
   }
 
   @Test
   public void noSystemOutExists() throws Exception {
     config.addAttribute("checkedMethods", "System.out");
     test(config, NO_ERR);
   }
 
   @Test
   public void systemOutFound() throws Exception {
     config.addAttribute("checkedMethods", "System.out");
     test(config, Arrays.asList(errAt(7, 12)));
   }
 
  @Test
   public void multipleErrorsFound() throws Exception {
     config.addAttribute("checkedMethods", "main:System.exit,System.out,System.in");
     test(config, Arrays.asList(errAt(6, 12), errAt(7, 12), errAt(8, 12)));
   }
 }
