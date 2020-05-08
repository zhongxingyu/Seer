 /*
  * Copyright 2010 Softgress - http://www.softgress.com/
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package sim.instrumentation.data;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import sim.data.MethodMetricsImpl;
 
 /**
  * @author mcq
  * 
  */
 public class MetricsTest {
 
 	/**
 	 * Test method for WallClockTime calculation by
 	 * {@link sim.instrumentation.data.Metrics}.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testWallClockTime() throws Exception {
 		MethodMetricsImpl mm = new MethodMetricsImpl(this.getClass().getName(), "testWallClockTime");
 		Metrics.beginReadMethodMetters(mm);
 		Assert.assertFalse(0 == mm.getBeginExecutionTime());
 		Thread.sleep(100);
 		Metrics.endReadMethodMetters(mm);
 		Assert.assertTrue(100 <= mm.getWallClockTime());
 		Assert.assertEquals(mm.getEndExecutionTime() - mm.getBeginExecutionTime(), mm.getWallClockTime());
 	}
 
 	/**
 	 * Test method for testThreadWaitTimeAndCount calculation by
 	 * {@link sim.instrumentation.data.Metrics}.
 	 * 
 	 * @throws Exception
 	 */
 	@Test
 	public void testThreadWaitTimeAndCount() throws Exception {
 		MethodMetricsImpl mm = new MethodMetricsImpl(this.getClass().getName(), "testThreadWaitTimeAndCount");
 		Metrics.beginReadMethodMetters(mm);
 		Assert.assertFalse(0L == mm.getThreadWaitTime());
 		Assert.assertFalse(0L == mm.getThreadWaitCount());
 		Thread.sleep(50);
 		Thread.sleep(50);
 		Metrics.endReadMethodMetters(mm);
		Assert.assertTrue(100 <= mm.getThreadWaitTime());
 		Assert.assertEquals(2, mm.getThreadWaitCount());
 	}
 }
