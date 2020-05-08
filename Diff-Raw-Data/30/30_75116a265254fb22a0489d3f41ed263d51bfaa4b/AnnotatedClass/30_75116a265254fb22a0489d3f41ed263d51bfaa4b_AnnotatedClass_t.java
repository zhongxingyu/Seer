package experiments;

import com.reeltwo.jumble.annotations.TestClass;

/**
 * Class used for testing.
 * @author Tin Pavlinic
 * @version $Revision: $
 */
@TestClass({"experiments.JumblerExperimentTest" })
public class AnnotatedClass {
  public int addOne(int x) {
    return x + 1;
  }
}
