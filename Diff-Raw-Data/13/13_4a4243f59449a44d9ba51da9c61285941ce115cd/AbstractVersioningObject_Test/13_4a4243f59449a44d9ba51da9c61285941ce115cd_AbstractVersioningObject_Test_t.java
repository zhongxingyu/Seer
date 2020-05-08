 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.enterprise.domain;
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
import static zeroth.framework.enterprise.domain.AbstractVersioningObject_.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import com.googlecode.jeeunit.JeeunitRunner;
 import com.googlecode.jeeunit.Transactional;
 /**
  * {@link AbstractVersioningObject_} のユニットテスト
  * @author nilcy
  */
 @RunWith(JeeunitRunner.class)
 @Transactional
 @SuppressWarnings("all")
 public class AbstractVersioningObject_Test {
     @Test
     public final void test() {
        assertThat(version, is(not(nullValue())));
     }
 }
