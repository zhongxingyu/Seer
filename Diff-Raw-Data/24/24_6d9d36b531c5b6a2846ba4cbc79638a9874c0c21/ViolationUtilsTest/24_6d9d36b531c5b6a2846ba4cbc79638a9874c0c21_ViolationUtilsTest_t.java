 package org.sonar.ide.shared;
 
 import org.junit.Test;
 import org.sonar.wsclient.services.Violation;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class ViolationUtilsTest {
   @Test
   public void testConvertPriority() throws Exception {
     assertThat(ViolationUtils.convertPriority("Blocker"), is(0));
     assertThat(ViolationUtils.convertPriority("Critical"), is(1));
     assertThat(ViolationUtils.convertPriority("Major"), is(2));
     assertThat(ViolationUtils.convertPriority("Minor"), is(3));
     assertThat(ViolationUtils.convertPriority("Info"), is(4));
     assertThat(ViolationUtils.convertPriority("Other"), is(4));
   }
 
   @Test
   public void testSortByPriority() throws Exception {
     Violation minor = newViolation("Minor", 1);
     Violation blocker = newViolation("Blocker", 1);
 
     List<Violation> violations = ViolationUtils.sortByPriority(Arrays.asList(minor, blocker));
 
     assertThat(violations.size(), is(2));
     assertThat(violations.get(0), sameInstance(blocker));
     assertThat(violations.get(1), sameInstance(minor));
   }
 
   @Test
   public void testDivideByLines() throws Exception {
     Violation v1 = newViolation("Minor", 2);
     Violation v2 = newViolation("Minor", 1);
     Violation v3 = newViolation("Blocker", 2);
 
     Map<Integer, List<Violation>> map = ViolationUtils.splitByLines(Arrays.asList(v1, v2, v3));
 
     assertThat(map.size(), is(2));
     assertThat(map.get(1).size(), is(1));
    assertThat(map.get(1), hasItem(v2));
     assertThat(map.get(2).size(), is(2));
    assertThat(map.get(2), hasItems(v1, v3));
   }
 
   @Test
   public void testGetDescription() {
     Violation violation = mock(Violation.class);
     when(violation.getRuleName()).thenReturn("Unused");
     when(violation.getMessage()).thenReturn("Avoid unused");
     assertThat(ViolationUtils.getDescription(violation), is("Unused : Avoid unused"));
   }
 
   protected Violation newViolation(String priority, int line) {
     Violation violation = new Violation();
     violation.setPriority(priority);
     violation.setLine(line);
     return violation;
   }
 }
