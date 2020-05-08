 package com.jeremyhaberman.raingauge.processor.test;
 
 import android.test.AndroidTestCase;
 import com.jeremyhaberman.raingauge.Service;
 import com.jeremyhaberman.raingauge.ServiceManager;
 import com.jeremyhaberman.raingauge.processor.DefaultProcessorFactory;
 import com.jeremyhaberman.raingauge.processor.ForecastProcessor;
 import com.jeremyhaberman.raingauge.processor.ObservationsProcessor;
 import com.jeremyhaberman.raingauge.processor.ProcessorFactory;
 import com.jeremyhaberman.raingauge.processor.ResourceProcessor;
 import com.jeremyhaberman.raingauge.rest.method.DefaultRestMethodFactory;
 import com.jeremyhaberman.raingauge.service.WeatherService;
 import com.jeremyhaberman.raingauge.util.TestUtil;
 
 public class DefaultProcessorFactoryTest extends AndroidTestCase {
 
 	private ProcessorFactory mFactory;
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		mFactory = DefaultProcessorFactory.getInstance(getContext());
 		ServiceManager.loadService(getContext(), Service.PROCESSOR_FACTORY, mFactory);
 		ServiceManager.loadService(getContext(), Service.REST_METHOD_FACTORY,
 				DefaultRestMethodFactory.getInstance(getContext()));
 	}
 
 	protected void tearDown() throws Exception {
 		TestUtil.resetServiceManager(getContext());
 		super.tearDown();
 	}
 
 	public void testGetProcessor() {
 		ResourceProcessor processor =
 				mFactory.getProcessor(WeatherService.RESOURCE_TYPE_OBSERVATIONS);
 		assertTrue(processor instanceof ObservationsProcessor);
 
 		processor = mFactory.getProcessor(WeatherService.RESOURCE_TYPE_FORECAST);
 		assertTrue(processor instanceof ForecastProcessor);
 	}
 
 	public void testGetProcessorWithInvalidType() {
 		try {
			mFactory.getProcessor(2);
 			fail("Should have thrown IllegalArgumentException");
 		} catch (IllegalArgumentException e) {
 			assertTrue(true);
 		}
 	}
 
 	public void testGetInstance() {
 		assertNotNull(mFactory);
 	}
 
 }
