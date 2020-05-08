 package org.sensapp.android.sensappdroid.test;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.sensapp.android.sensappdroid.contentprovider.SensAppCPContract;
 import org.sensapp.android.sensappdroid.contentprovider.SensAppContentProvider;
 import org.sensapp.android.sensappdroid.restservice.PutMeasuresTask;
 
 import android.content.ContentValues;
 import android.net.Uri;
 import android.test.ProviderTestCase2;
 
 public class PutMeasuresTaskTest extends ProviderTestCase2<SensAppContentProvider> {
 
 	public PutMeasuresTaskTest() {
 		super(SensAppContentProvider.class, SensAppCPContract.AUTHORITY);
 	}
 
 	public void testNoMeasure() throws InterruptedException, ExecutionException, TimeoutException {
 		int nbUploaded = new PutMeasuresTask(getContext(), SensAppCPContract.Measure.CONTENT_URI).execute().get(5000, TimeUnit.MILLISECONDS);
 		assertEquals(0, nbUploaded);
 	}
 	
 	public void testMeasureWithoutSensor() throws InterruptedException, ExecutionException, TimeoutException {
 		ContentValues values = new ContentValues();
 		values.put(SensAppCPContract.Measure.ID, 11);
 		values.put(SensAppCPContract.Measure.BASETIME, 0);
 		values.put(SensAppCPContract.Measure.TIME, 1000);
 		values.put(SensAppCPContract.Measure.SENSOR, "testSensor0");
 		values.put(SensAppCPContract.Measure.VALUE, 999);
 		values.put(SensAppCPContract.Measure.UPLOADED, 0);
 		getContext().getContentResolver().insert(SensAppCPContract.Measure.CONTENT_URI, values);
		assertNull(new PutMeasuresTask(getContext(), SensAppCPContract.Measure.CONTENT_URI).execute().get(5000, TimeUnit.MILLISECONDS));
 	}
 	
 	public void testMeasureById() throws InterruptedException, ExecutionException, TimeoutException {
 		ContentValues values = new ContentValues();
 		values.put(SensAppCPContract.Measure.ID, 11);
 		values.put(SensAppCPContract.Measure.BASETIME, 0);
 		values.put(SensAppCPContract.Measure.TIME, 1000);
 		values.put(SensAppCPContract.Measure.SENSOR, "testSensor0");
 		values.put(SensAppCPContract.Measure.VALUE, 999);
 		values.put(SensAppCPContract.Measure.UPLOADED, 0);
 		getContext().getContentResolver().insert(SensAppCPContract.Measure.CONTENT_URI, values);
 		DataManager.insertSensors(getContext().getContentResolver(), 1);
 		int nbUploaded = new PutMeasuresTask(getContext(), Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" + 11)).execute().get(10, TimeUnit.SECONDS);
 		assertEquals(1, nbUploaded);
 	}
 	
 	public void testMeasuresBySensor() throws InterruptedException, ExecutionException, TimeoutException {
 		DataManager.insertMeasures(getContext().getContentResolver(), 15, 1);
 		DataManager.insertSensors(getContext().getContentResolver(), 1);
 		int nbUploaded = new PutMeasuresTask(getContext(), Uri.parse(SensAppCPContract.Measure.CONTENT_URI + "/" + "testSensor0")).execute().get(15, TimeUnit.SECONDS);
 		assertEquals(15, nbUploaded);
 	}
 	
 	public void testAllMeasures() throws InterruptedException, ExecutionException, TimeoutException {
 		DataManager.insertMeasures(getContext().getContentResolver(), 20, 3);
 		DataManager.insertSensors(getContext().getContentResolver(), 3);
 		int nbUploaded = new PutMeasuresTask(getContext(), SensAppCPContract.Measure.CONTENT_URI).execute().get(20, TimeUnit.SECONDS);
 		assertEquals(20, nbUploaded);
 	}
 	
 	@Override
 	public void tearDown() throws Exception {
 		DataManager.cleanAll(getContext().getContentResolver(), 3);
 	}
 }
