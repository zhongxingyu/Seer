 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.shared;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import com.googlecode.jeeunit.JeeunitRunner;
 import com.googlecode.jeeunit.Transactional;
 /**
  * {@link LoggerProducer}
  * @author nilcy
  */
 @RunWith(JeeunitRunner.class)
 @Transactional
 @SuppressWarnings("all")
 public class LoggerProducerTest {
     @Inject
     private Logger testee;
     @Test
     public void test() {
        assertThat(testee, is(not(nullValue())));
        testee.finest("FINEST");
        testee.finer("FINER");
        testee.fine("FINE");
        testee.config("CONFIG");
        testee.info("INFO");
        testee.warning("WARNING");
        testee.severe("SEVERE");
     }
 }
