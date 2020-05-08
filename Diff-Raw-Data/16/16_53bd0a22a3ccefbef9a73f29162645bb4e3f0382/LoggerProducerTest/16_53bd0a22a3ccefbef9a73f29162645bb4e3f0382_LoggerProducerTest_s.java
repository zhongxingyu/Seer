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
        assertThat(this.testee, is(not(nullValue())));
        this.testee.finest("FINEST");
        this.testee.finer("FINER");
        this.testee.fine("FINE");
        this.testee.config("CONFIG");
        this.testee.info("INFO");
        this.testee.warning("WARNING");
        this.testee.severe("SEVERE");
     }
 }
