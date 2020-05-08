 package com.imaginea.android.sugarcrm.restapi;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.exception.MethodInvocationException;
 import org.apache.velocity.exception.ParseErrorException;
 
 import android.test.suitebuilder.annotation.LargeTest;
 import android.util.Log;
 
 import com.imaginea.android.sugarcrm.util.ModuleField;
 import com.imaginea.android.sugarcrm.util.RestUtil;
 
 /**
  * Do not run this, exclude this from the test suite
  * 
  * @author chander
  * 
  */
 public class GenerateModuleFieldsTest extends RestAPITest {
 
 	String moduleName = "Accounts";
 
 	String[] fields = new String[] {};
 
 	String[] customFields = new String[] { "a", "b" };
 
 	LinkedHashSet<String> moduleFields = new LinkedHashSet<String>();
 
 	List<String> moduleFieldList = new ArrayList<String>();
 
 	public final static String LOG_TAG = "ModuleFieldTest";
 
 	/**
 	 * the values are stored in a linked hashset so order is preserved. Add new
 	 * modules at the end, so you know the new elements for which constants have
 	 * to be created
 	 * 
 	 * @throws Exception
 	 */
 	@LargeTest
 	public void testGetAllModuleFields() throws Exception {
 
 		List<ModuleField> moduleFields = RestUtil.getModuleFields(url,
				mSessionId, "Accounts", fields);
 		assertNotNull(moduleFields);
 		addToModuleFieldList(moduleFields);
 
 		moduleFields = RestUtil.getModuleFields(url, mSessionId, "Contacts",
				fields);
 		addToModuleFieldList(moduleFields);
 		moduleFields = RestUtil.getModuleFields(url, mSessionId,
				"Opportunities", fields);
 		addToModuleFieldList(moduleFields);
 		moduleFields = RestUtil.getModuleFields(url, mSessionId, "Leads",
				fields);
 		addToModuleFieldList(moduleFields);
 		moduleFields = RestUtil.getModuleFields(url, mSessionId, "Campaigns",
				fields);
 		addToModuleFieldList(moduleFields);
 		moduleFields = RestUtil.getModuleFields(url, mSessionId, "Meetings",
				fields);
 		addToModuleFieldList(moduleFields);
 
 		moduleFields = RestUtil.getModuleFields(url, mSessionId, "Cases",
				fields);
 		addToModuleFieldList(moduleFields);
 
 		for (Iterator iterator = moduleFields.iterator(); iterator.hasNext();) {
 			String field = (String) iterator.next();
 			Log.i("ModuleFields:", field);
 		}
 		generateClass(moduleFields);
 		// Log.i("ModuleFields:"+ moduleName.)
 	}
 
 	private void addToModuleFieldList(List<ModuleField> moduleFields) {
 		for (ModuleField moduleField : moduleFields) {
 			moduleFieldList.add(moduleField.getName());
 		}
 	}
 
 	private void addToModFields(Iterator keys) {
 		for (Iterator iterator = keys; iterator.hasNext();) {
 			String object = (String) iterator.next();
 			moduleFields.add(object);
 
 		}
 	}
 
 	/**
 	 * This class is a generate Class file using the Velocity Template Engine
 	 */
 
 	public void generateClass(List set) {
 		/* first, we init the runtime engine. Defaults are not fine in android. */
 
 		try {
 			Properties prop = new Properties();
 			prop.put("runtime.log.logsystem.class",
 					"org.apache.velocity.runtime.log.NullLogSystem");
 			Velocity.init(prop);
 			// Velocity.addProperty("runtime.log.logsystem.class",
 			// "org.apache.velocity.runtime.log.NullLogSystem");
 		} catch (Exception e) {
 			Log.e(LOG_TAG, "Problem initializing Velocity : " + e);
 			return;
 		}
 
 		/* lets make a Context and put data into it */
 
 		VelocityContext context = new VelocityContext();
 		context.put("fields", set);
 
 		/* lets render a template */
 
 		StringWriter w = new StringWriter();
 		/*
 		 * lets dynamically 'create' our template and use the evaluate() method
 		 * to render it. Its currently in assets folder of test package, but to
 		 * generate it we need it in the main project, but do not check-in this
 		 * file. This is really a tool and can be kept outside of our android
 		 * project, but what the heck, we want everything in one place
 		 */
 		String s = "";
 		try {
 			InputStream is = super.getContext().getAssets()
 					.open("classFile.vm");
 			// InputStream is =
 			// super.getContext().getAssets().open("fields.vm");
 
 			// We guarantee that the available method returns the total
 			// size of the asset... of course, this does mean that a single
 			// asset can't be more than 2 gigs.
 			int size = is.available();
 
 			// Read the entire asset into a local byte buffer.
 			byte[] buffer = new byte[size];
 			is.read(buffer);
 			is.close();
 
 			// Convert the buffer into a string.
 			s = new String(buffer);
 		} catch (IOException e) {
 			// Should never happen!
 			throw new RuntimeException(e);
 		}
 
 		w = new StringWriter();
 
 		try {
 			Velocity.evaluate(context, w, "mystring", s);
 		} catch (ParseErrorException pee) {
 			/*
 			 * thrown if something is wrong with the syntax of our template
 			 * string
 			 */
 			Log.e(LOG_TAG, "ParseErrorException : " + pee);
 		} catch (MethodInvocationException mee) {
 			/*
 			 * thrown if a method of a reference called by the template throws
 			 * an exception. That won't happen here as we aren't calling any
 			 * methods in this example, but we have to catch them anyway
 			 */
 			Log.e(LOG_TAG, "MethodInvocationException : " + mee);
 		} catch (Exception e) {
 			Log.e(LOG_TAG, "Exception : " + e);
 		}
 
 		Log.d(LOG_TAG, " string : " + w);
 	}
 }
