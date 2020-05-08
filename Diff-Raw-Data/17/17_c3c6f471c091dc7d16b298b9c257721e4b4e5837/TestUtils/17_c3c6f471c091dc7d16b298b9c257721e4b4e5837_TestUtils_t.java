 package com.qeevee.gq.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.URL;
 
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.io.SAXReader;
 
 import android.content.Intent;
 
 import com.qeevee.gq.history.History;
 import com.qeevee.gq.history.HistoryItem;
 import com.qeevee.gq.history.HistoryItemModifier;
 import com.xtremelabs.robolectric.Robolectric;
 
 import edu.bonn.mobilegaming.geoquest.GameLoader;
 import edu.bonn.mobilegaming.geoquest.GeoQuestActivity;
 import edu.bonn.mobilegaming.geoquest.GeoQuestApp;
 import edu.bonn.mobilegaming.geoquest.Start;
 import edu.bonn.mobilegaming.geoquest.mission.Mission;
 
 public class TestUtils {
 
     /**
      * @param gameName
      *            the name of the directory containing the game specification.
      * @return the game spec as xml document
      */
     public static Document loadTestGame(String gameName) {
 	Document document = null;
 	SAXReader reader = new SAXReader();
 	try {
 	    File gameFile = getGameFile(gameName);
 	    document = reader.read(gameFile);
 	    GeoQuestApp.setRunningGameDir(new File(gameFile.getParent()));
 	} catch (DocumentException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	return document;
     }
 
     /**
      * @param gameName
      *            the name of the directory containing the game specification.
      * @return the File object representing the game spec xml file.
      */
     public static File getGameFile(String gameName) {
 	URL xmlFileURL = TestUtils.class.getResource("/testgames/" + gameName
 		+ "/game.xml");
 	if (xmlFileURL == null)
 	    fail("Resource file not found for game: " + gameName);
 	return new File(xmlFileURL.getFile());
     }
 
     /**
      * Sets up the test for a single mission type using a prepared game file
      * (game.xml) which must be stored in "testgames" directory and named
      * "<missionName>Test".
      * 
      * @param missionType
      *            must be a valid mission type for which a class exists in the
      *            mission implementation package.
      * @param missionID
      * @return a new Activity object of the according type for the given mission
      *         type name. You can for example directly call onCreate() upon it
      *         to emulate the android framework behavior.
      * @throws ClassNotFoundException
      */
     public static GeoQuestActivity setUpMissionTest(String missionType,
 						    String missionID) {
 	Start start = startGameForTest(missionType + "Test");
 
 	return prepareMission(missionType,
			      missionID,
			      start);
     }
 
     /**
      * Prepares a mission activity which can then be started by calling its
      * onCreate() method.
      * 
      * @param missionType
      *            must be a valid mission type for which a class exists in the
      *            mission implementation package.
      * @param missionID
      * @return a new Activity object of the according type for the given mission
      *         type name. You can for example directly call onCreate() upon it
      *         to emulate the android framework behavior.
      * @throws ClassNotFoundException
      */
     public static GeoQuestActivity prepareMission(String missionType,
						  String missionID,
						  Start start) {
 	Class<?> missionClass = null;
 	GeoQuestActivity missionActivity = null;
 
 	try {
 	    missionClass = Class.forName(Mission.getPackageBaseName()
 		    + missionType);
 	    missionActivity = (GeoQuestActivity) missionClass.newInstance();
 	} catch (InstantiationException e) {
 	    throw new RuntimeException(e);
 	} catch (IllegalAccessException e) {
 	    throw new RuntimeException(e);
 	} catch (ClassNotFoundException e) {
 	    throw new RuntimeException(e);
 	}
 	Intent startMissionIntent = new Intent(start, missionClass);
 	startMissionIntent.putExtra("missionID",
 				    missionID);
 	Robolectric.shadowOf(missionActivity).setIntent(startMissionIntent);
 	return missionActivity;
     }
 
     public static Start startGameForTest(String gameFileName) {
 	Start start = new Start();
 	GeoQuestApp app = (GeoQuestApp) start.getApplication();
 	app.onCreate();
 	Mission.setMainActivity(start);
 	GameLoader.startGame(null,
 			     TestUtils.getGameFile(gameFileName));
 	return start;
     }
 
     /**
      * Lets you access the values of private or protected fields in your tests.
      * You will have to cast the resulting object down to the real type.
      * 
      * @param obj
      * @param fieldName
      * @return
      */
     public static Object getFieldValue(Object obj,
 				       String fieldName) {
 	Object value = null;
 	try {
 	    Field f = obj.getClass().getDeclaredField(fieldName);
 	    f.setAccessible(true);
 	    value = f.get(obj);
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (SecurityException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (NoSuchFieldException e) {
 	    fail("Implementation of type \"" + obj.getClass().getSimpleName()
 		    + "\" misses a field named \"" + fieldName + "\"");
 	}
 	return value;
     }
 
     public static Object callMethod(Object obj,
 				    String methodName,
 				    Class<?>[] parameterTypes,
 				    Object[] arguments) {
 	Object returnValue = null;
 	try {
	    Method m = obj.getClass().getDeclaredMethod(methodName,
							parameterTypes);
 	    m.setAccessible(true);
	    returnValue = m.invoke(obj,
 		     arguments);
 	} catch (SecurityException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (NoSuchMethodException e) {
 	    StringBuffer signature = new StringBuffer();
 	    signature.append(methodName + "(");
 	    for (int i = 0; i < parameterTypes.length; i++) {
 		if (i > 0)
 		    signature.append(", ");
 		signature.append(parameterTypes[i].getName());
 	    }
 	    signature.append(")");
 	    fail("Implementation of type \"" + obj.getClass().getSimpleName()
 		    + "\" misses a method \"" + signature + "\"");
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (InvocationTargetException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	}
 	return returnValue;
     }
 
     public static Object
 	    getStaticFieldValue(@SuppressWarnings("rawtypes") Class clazz,
 				String fieldName) {
 	Object value = null;
 	try {
 	    Field field = clazz.getDeclaredField(fieldName);
 	    field.setAccessible(true);
 	    value = field.get(null);
 	} catch (SecurityException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (NoSuchFieldException e) {
 	    fail("Implementation of type \"" + clazz.getSimpleName()
 		    + "\" misses a field named \"" + fieldName + "\"");
 	} catch (IllegalArgumentException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	} catch (IllegalAccessException e) {
 	    e.printStackTrace();
 	    throw new RuntimeException(e);
 	}
 	return value;
     }
 
     public static String getResString(int id) {
 	return GeoQuestApp.getContext().getResources().getString(id);
     }
 
     /**
      * Checks that the last n items in the history are correctly characterized
      * by the given item class and modifiers.
      * 
      * @param n
      * @param expectedItemClass
      * @param expectedItemModifier
      */
     public static
 	    void
 	    nthLastItemInHistoryShouldBe(int n,
 					 Class<? extends HistoryItem> expectedItemClass,
 					 HistoryItemModifier... expectedItemModifier) {
 	HistoryItem lastItem = History.getInstance().getNthLastItem(n);
 	assertEquals(expectedItemClass,
 		     lastItem.getClass());
 	for (int i = 0; i < expectedItemModifier.length; i++) {
 	    assertEquals(expectedItemModifier[i],
 			 lastItem.getModifier(expectedItemModifier[i]
 				 .getClass()));
 	}
     }
 
     public static void historyListShouldHaveLength(int i) {
 	assertEquals(i,
 		     History.getInstance().numberOfItems());
     }
 
 }
