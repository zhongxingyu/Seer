 package ru.tulupov.nsuconnect.helper;
 
 
 import android.text.TextUtils;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ru.tulupov.nsuconnect.model.SearchParameters;
 import ru.tulupov.nsuconnect.request.Constants;
 
 public class SearchSettingHelper {
     private static final String PARAMS_GENDER = "gender";
     private static final String PARAMS_AGE = "age";
     private static final String PARAMS_SEARCH_GENDER = "search_gender";
     private static final String PARAMS_SEARCH_AGE = "search_age";
     private static final String PARAMS_CITY_ID = "city_id";
     private static final String PARAMS_NSU_DEPARTMENT = "nsu_department";
     private static final String PARAMS_SEARCH_NSU_DEPARTMENT = "search_nsu_department";
     private static final String PARAMS_PROVIDER = "provider";
 
     private static String encodeYourGender(List<Integer> list) {
         if (!list.isEmpty()) {
             return String.valueOf(list.get(0));
         }
         return "0";
     }
 
     private static String encodeTargetGender(List<Integer> list) {
         List<String> flags = new ArrayList<String>();
         for (int i = 0; i < 2; i++) {
             flags.add("0");
         }
 
         for (Integer position : list) {
             flags.set(position, "1");
         }
 
         return TextUtils.join(",", flags);
     }
 
     private static String encodeYourAge(List<Integer> list) {
         if (!list.isEmpty()) {
             return String.valueOf(list.get(0));
         }
         return "0";
     }
 
     private static String encodeTargetAge(List<Integer> list) {
         List<String> flags = new ArrayList<String>();
         for (int i = 0; i < 5; i++) {
             flags.add("0");
         }
 
         for (Integer position : list) {
             flags.set(position, "1");
         }
 
         return TextUtils.join(",", flags);
     }
 
     public static String generate(SearchParameters searchParameters) {
         Map<String, String> params = new HashMap<String, String>();
 
 
         params.put(PARAMS_GENDER, encodeYourGender(searchParameters.getYourGender()));
         params.put(PARAMS_AGE, encodeYourAge(searchParameters.getYourAge()));
         params.put(PARAMS_SEARCH_GENDER, encodeTargetGender(searchParameters.getTargetGender()));
         params.put(PARAMS_SEARCH_AGE, encodeTargetAge(searchParameters.getTargetAge()));
         params.put(PARAMS_CITY_ID, "0");
         params.put(PARAMS_NSU_DEPARTMENT, "0");
         params.put(PARAMS_SEARCH_NSU_DEPARTMENT, "0,0,0,0,0,0,0,0,0,0,0,0,0,0,0");
        params.put(PARAMS_PROVIDER, "1"); // provider=nsu
 
         List<String> encoded = new ArrayList<String>();
         for (Map.Entry<String, String> entry : params.entrySet()) {
             try {
                 encoded.add(String.format("%s=%s", entry.getKey(), URLEncoder.encode(entry.getValue(), "utf-8")));
             } catch (UnsupportedEncodingException e) {
 
             }
         }
         return TextUtils.join("; ", encoded);
     }
 }
