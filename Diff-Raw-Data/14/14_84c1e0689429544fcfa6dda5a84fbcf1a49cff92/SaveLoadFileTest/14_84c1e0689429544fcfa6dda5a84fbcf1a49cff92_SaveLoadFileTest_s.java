 package com.quanleimu.activity.test;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.quanleimu.entity.BXLocation;
 import com.quanleimu.entity.UserBean;
 import com.quanleimu.util.Util;
 
 import android.test.AndroidTestCase;
 import android.util.Pair;
 
 public class SaveLoadFileTest extends AndroidTestCase {
 	public void setUp()
 	{
 		try {
 			super.setUp();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void testSaveAndLoadSer()
 	{
 		//First check 
 		
 		String tmpDir = System.currentTimeMillis() + "";
 		
 		String ss = new String("abcd");
 		String path = Util.saveSerializableToPath(getContext(), tmpDir, "abc.ser", ss);
 		assertNotNull(path);
 		assertTrue(new File(path).exists());
 		
 		List<String> files = Util.listFiles(getContext(), tmpDir);
 		assertEquals(1, files.size());
 		assertEquals(files.get(0), path);
 		
 		String ssResult = (String) Util.loadSerializable(path);
 		assertEquals(ssResult, ss);
 		
 		File f = new File(path);
 		f.delete();
 	}
 	
 	public void testSaveAndLoadString()
 	{
 		String tmpDir = System.currentTimeMillis() + "";
 		
 		String ss = new String("abcd");
 		String path = Util.saveDataToFile(getContext(), tmpDir, "abc.dat", ss.getBytes());
 		assertNotNull(path);
 		assertTrue(new File(path).exists());
 		
 		String path2 = Util.saveDataToFile(getContext(), tmpDir, "cd.dat", ss.getBytes());
 		assertNotNull(path2);
 		assertTrue(new File(path2).exists());
 		
 		List<String> files = Util.listFiles(getContext(), tmpDir);
 		assertEquals(2, files.size());
 		assertEquals(files.get(0), path);
 		assertEquals(files.get(1), path2);
 		
 		String ssResult = new String(Util.loadData(path));
 		assertEquals(ssResult, ss);
 		
 		File f = new File(path);
 		f.delete();
 	}
 	
 	public void testSaveAndLoadToLocate()
 	{
 		UserBean userBean = new UserBean();
 		userBean.setId("123");
 		userBean.setPassword("123");
 		userBean.setPhone("123456");
 		
 		final String fileName = System.currentTimeMillis() + ".json";
 		String retValue = Util.saveDataToLocate(getContext(), fileName, userBean);
 		assertEquals("保存成功", retValue);
 		
 		UserBean resultBean = (UserBean) Util.loadDataFromLocate(getContext(), fileName, UserBean.class);
 		assertNotNull(resultBean);
 		assertEquals(userBean.getId(), resultBean.getId());
 		assertEquals(userBean.getPassword(), resultBean.getPassword());
 		assertEquals(userBean.getPhone(), resultBean.getPhone());
 		
 	}
 	
 	public void testSaveAndLoadLocation()
 	{
 		BXLocation location = new BXLocation(true);
 		final String fileName = System.currentTimeMillis() + ".json";
 		
 		String retValue = Util.saveDataToLocate(getContext(), fileName, location);
 		assertEquals("保存成功", retValue);
 		
 		BXLocation locationLoad = (BXLocation) Util.loadDataFromLocate(getContext(), fileName, BXLocation.class);
 		assertNotNull(locationLoad);
 		assertEquals(location.address, locationLoad.address);
 		assertEquals(location.cityName, locationLoad.cityName);
 	}
 	
	public void saveAndLoadJson()
 	{
 		String json = "{abc:123}";
 		long timestamp = System.currentTimeMillis()/1000;
 		final String fileName = System.currentTimeMillis() + ".json";
 		
 		Util.saveJsonAndTimestampToLocate(getContext(), fileName, json, timestamp);
 		
 		Pair<Long, String> p = Util.loadJsonAndTimestampFromLocate(getContext(), fileName);
 		assertNotNull(p);
 		assertEquals(p.first.longValue(), timestamp);
 	}
 	
	public void saveAndLoadArray()
 	{
 		List<String> arrayList = new ArrayList<String>();
 		for (int i=0; i<10; i++)
 		{
 			arrayList.add(String.valueOf(i));
 		}
 		final String fileName = System.currentTimeMillis() + ".json";
 		Util.saveDataToLocate(getContext(), fileName, arrayList);
 		
 		String[] result = (String[]) Util.loadDataFromLocate(getContext(), fileName, String[].class);
 		assertNotNull(result);
 		for (int i=0; i<10; i++)
 		{
 			assertEquals(arrayList.get(i), result[i]);
 		}
 	}
 }
