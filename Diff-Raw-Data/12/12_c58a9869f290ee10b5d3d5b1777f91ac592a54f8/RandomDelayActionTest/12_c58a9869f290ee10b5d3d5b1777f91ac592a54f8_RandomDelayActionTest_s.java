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
 import net.nicoulaj.benchmark.mockwebapp.config.then.RandomDelayAction;
 import org.testng.annotations.Test;
 import org.xml.sax.SAXException;
 
 import java.io.IOException;
 
 import static org.testng.Assert.assertTrue;
 
 /**
  * Tests for {@link RandomDelayAction}.
  *
  * @author Julien Nicoulaud <julien.nicoulaud@gmail.com>
  * @since 1.0.0
  */
 public class RandomDelayActionTest extends DelayActionTest {
 
     /**
      * Assert the time elapsed for processing the request is in the range.
      *
      * @param delay the delay to use for the test.
      * @throws IOException  should never happen.
      * @throws SAXException should never happen.
      */
     @Test(dataProvider = "delayRangesDataProvider")
     public void elapsedTimeShouldBeInRange(int delay) throws IOException, SAXException {
 
         final RandomDelayAction stmt = new RandomDelayAction();
         stmt.min = delay;
         stmt.max = delay + 1000;
 
         final GetMethodWebRequest webRequest = new GetMethodWebRequest("http://localhost/test");
         final long startTime = System.currentTimeMillis();
         stmt.process(getRequest(webRequest), getResponse(webRequest));
         final long elapsedTime = System.currentTimeMillis() - startTime;
         assertTrue(elapsedTime >= stmt.min);
        assertTrue(elapsedTime <= stmt.max + SAFETY_MARGIN);
     }
 
     /**
      * Assert validating a {@link RandomDelayAction} with a negative minimum delay setting throws an error.
      *
      * @throws Throwable should always happen.
      */
     @Test(expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "The delay lower bound cannot be negative")
     public void minDelayShouldBePositive() throws Throwable {
         final RandomDelayAction stmt = new RandomDelayAction();
         stmt.min = -1;
         stmt.max = 10;
         stmt.validate();
     }
 
     /**
      * Assert validating a {@link RandomDelayAction} with a negative maximum delay setting throws an error.
      *
      * @throws Throwable should always happen.
      */
     @Test(expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "The delay upper bound cannot be negative")
     public void maxDelayShouldBePositive() throws Throwable {
         final RandomDelayAction stmt = new RandomDelayAction();
         stmt.min = 10;
         stmt.max = -1;
         stmt.validate();
     }
 
     /**
      * Assert validating a {@link RandomDelayAction} with a maximum delay lower than the minimum delay throws an error.
      *
      * @throws Throwable should always happen.
      */
     @Test(expectedExceptions = Throwable.class,
           expectedExceptionsMessageRegExp = "The delay upper bound must be superior to the delay lower bound")
     public void maxShouldBeGreaterThanMin() throws Throwable {
         final RandomDelayAction stmt = new RandomDelayAction();
         stmt.min = 10;
         stmt.max = 9;
         stmt.validate();
     }
 }
