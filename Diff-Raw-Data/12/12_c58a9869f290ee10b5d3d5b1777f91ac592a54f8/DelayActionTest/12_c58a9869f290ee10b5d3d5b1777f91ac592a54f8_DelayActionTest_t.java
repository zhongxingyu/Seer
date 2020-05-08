 /*
  * Copyright (c) 2011 Julien Nicoulaud <julien.nicoulaud@gmail.com>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.nicoulaj.benchmark.mockwebapp.test.config.then;
 
 import com.meterware.httpunit.GetMethodWebRequest;
 import net.nicoulaj.benchmark.mockwebapp.config.then.DelayAction;
 import net.nicoulaj.benchmark.mockwebapp.test.AbstractMockWebAppTest;
 import org.testng.annotations.DataProvider;
 import org.testng.annotations.Test;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import static org.testng.Assert.assertTrue;
 
 /**
  * Tests for {@link DelayAction}.
  *
  * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
  * @since 1.0.0
  */
 public class DelayActionTest extends AbstractMockWebAppTest {
 
     /**
      * The safety margin to use when calculating the time elapsed.
      */
    protected static final int TOLERANCE = 50;
 
     /**
      * Provide a list of coherent delays.
      * <p/>
      * Can be used by tests method with a (int delay) signature.
      *
      * @return an {@link Iterator} of delays as single argument.
      */
     @DataProvider
     public Iterator<Object[]> delayRangesDataProvider() {
         final List<Object[]> params = new ArrayList<Object[]>();
         for (int i : new int[]{5, 50, 100, 300, 1000}) params.add(new Object[]{i});
         return params.iterator();
     }
 
     /**
      * Assert the time elapsed for processing the request is the right one.
      *
      * @param delay the delay to use for the test.
      * @throws IOException  should never happen.
      * @throws SAXException should never happen.
      */
     @Test(dataProvider = "delayRangesDataProvider")
     public void elapsedTimeShouldEqualDelay(int delay) throws IOException, SAXException {
 
         final DelayAction stmt = new DelayAction();
         stmt.time = delay;
 
         final GetMethodWebRequest webRequest = new GetMethodWebRequest("http://localhost/test");
         final long startTime = System.currentTimeMillis();
         stmt.process(getRequest(webRequest), getResponse(webRequest));
         final long elapsedTime = System.currentTimeMillis() - startTime;
 
         assertTrue(elapsedTime >= stmt.time);
        assertTrue(elapsedTime <= stmt.time + TOLERANCE);
     }
 
     /**
      * Assert validating a {@link DelayAction} with a negative delay setting throws an error.
      *
      * @throws Throwable should always happen.
      */
     @Test(expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "The delay cannot be negative")
     public void delayShouldBePositive() throws Throwable {
         final DelayAction stmt = new DelayAction();
         stmt.time = -1;
         stmt.validate();
     }
 }
