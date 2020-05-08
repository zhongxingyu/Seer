 /**
  * 
  */
 package org.vimeoid.util;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.os.Bundle;
 
 /**
  * <dl>
  * <dt>Project:</dt> <dd>vimeoid</dd>
  * <dt>Package:</dt> <dd>org.vimeoid.util</dd>
  * </dl>
  *
  * <code>ApiParams</code>
  *
  * <p>Description</p>
  *
  * @author Ulric Wilfred <shaman.sir@gmail.com>
  * @date Sep 27, 2010 7:49:03 PM 
  *
  */
 public class ApiParams {
     
     public static final String BUNDLE_NAMES_ARR = "ap__names";
     public static final String BUNDLE_VALUES_ARR = "ap__values";
     
     private final List<NameValuePair> params = new LinkedList<NameValuePair>();
     
     public ApiParams() { }
     
     public ApiParams add(String name, String value) {
         params.add(new BasicNameValuePair(name, value));
         return this;
     }
     
     public List<NameValuePair> getValue() {
         return params;
     }
 
     public boolean isEmpty() {
         return params.isEmpty();
     }
     
     @Override
     public String toString() {
         final StringBuffer result = new StringBuffer().append('(');
         for (NameValuePair pair: params) {
             result.append('(').append(pair.getName()).append(' ').append(pair.getValue()).append(") ");
         }
         return result.append(')').toString();
     }
     
     public Bundle toBundle() {
         final Bundle bundle = new Bundle();
         final String[] names = new String[params.size()];
         final String[] values = new String[params.size()];
         int i = 0;
        for (NameValuePair nvp: params) {
            names[i] = nvp.getName();
            values[i]= nvp.getValue();
         }
         bundle.putStringArray(BUNDLE_NAMES_ARR, names);
         bundle.putStringArray(BUNDLE_VALUES_ARR, values);
         return bundle;
     }
     
     public static ApiParams fromBundle(Bundle source) {
         final String[] names = source.getStringArray(BUNDLE_NAMES_ARR);
         final String[] values = source.getStringArray(BUNDLE_VALUES_ARR);
         final ApiParams params = new ApiParams();
         for (int i = 0; i < names.length; i++) {
             params.add(names[i], values[i]);
         }
         return params;
     }
 
 }
