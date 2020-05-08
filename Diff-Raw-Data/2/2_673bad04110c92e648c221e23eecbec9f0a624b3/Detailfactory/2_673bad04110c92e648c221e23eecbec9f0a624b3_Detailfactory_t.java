 package com.app.getconnected.factories.details;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import com.app.getconnected.R;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 
 @SuppressLint("DefaultLocale")
 public class Detailfactory {
 	
 	private static final String MODE_TAG = "mode";
 	private static final String VIEW_PREFIX = "transport_detail_view_";
 	private static final String VIEW_DEFAULT = "transport_detail_view_default";
 
 	/**
 	 * Gets the view
 	 * @param leg
 	 * @param context
 	 * @return
 	 * @throws JSONException
 	 * @throws ClassNotFoundException
 	 * @throws NoSuchMethodException
 	 * @throws IllegalArgumentException
 	 * @throws InstantiationException
 	 * @throws IllegalAccessException
 	 * @throws InvocationTargetException
 	 */
 	public static View getView(JSONObject leg, Context context) throws JSONException {
 		// Get the mode string in lower case, i.e. BUS -> bus
 		String mode = getMode(leg);
 	    // Get the resource id according to the resource name
 	    int resId = getRecourceId(mode, context);
 		// inflate the view for our generator
 		View view = inflateView(resId,context);
 	    // Get the mode with the first char to upper, i.e. Bus
 		mode = Character.toUpperCase(mode.charAt(0)) + mode.substring(1);
 		// Get the according generator class name
 	    String className = Detailfactory.class.getPackage() + mode + "DetailGenerator"; // get the current package name?
 	    // Use reflection to get the corresponding class
 		Class<?> detailGenerator = getDetailGeneratorClass(className);
 		// get the construc tor of the earlier created class		
 		return getDetailGeneratorInstance(view, context, leg,detailGenerator).getView();
 	}
 	private static String getMode(JSONObject leg) throws JSONException{
 		return leg.getString(MODE_TAG).toLowerCase();
 	}
 	
 
 	/**
 	 * Inflates the view
 	 * @param resource
 	 * @param context
 	 * @return
 	 */
 	private static View inflateView(int resource, Context context) {
 		ViewGroup wrapper = (ViewGroup) ((Activity) context)
 				.findViewById(R.id.transport_details_wrapper);
 		LayoutInflater inflater = (LayoutInflater) context
 				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		return inflater.inflate(resource, (ViewGroup) wrapper, false);
 	}
 	
 	private static Class<?> getDetailGeneratorClass(String className){
 		try{
 			return Class.forName(className);
 		}catch(ClassNotFoundException e){
 			return BaseDetailGenerator.class;
 		}
 	}
 	
 	private static int getRecourceId(String viewName, Context context){
 		String packageName = context.getPackageName();
 		int resource = context.getResources().getIdentifier(VIEW_PREFIX + viewName, "layout", packageName);
 		return resource != 0 ? resource : context.getResources().getIdentifier(VIEW_DEFAULT, "layout", packageName);		
 	}
 	
 	private static BaseDetailGenerator getDetailGeneratorInstance(View view, Context context, JSONObject leg, Class<?> detailGenerator) throws JSONException{
 		try{
 		Constructor<?> constructor = detailGenerator.getConstructor(View.class, Context.class,JSONObject.class);
 		return (BaseDetailGenerator)constructor.newInstance(view, context,leg);
 		}catch(Exception e){
 			return new DefaultDetailGenerator(view, context,leg);
 		}
 	}
 
 }
