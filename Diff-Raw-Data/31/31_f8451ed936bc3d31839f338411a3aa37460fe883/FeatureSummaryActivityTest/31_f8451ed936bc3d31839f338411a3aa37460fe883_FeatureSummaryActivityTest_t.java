 /*
  * Copyright (C) 2010 The Android Open Source Project
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
 
 package com.android.cts.verifier.features;
 
 import com.android.cts.verifier.features.FeatureSummaryActivity.Feature;
 
 import android.content.pm.PackageManager;
 import android.os.Build;
 
 import java.lang.reflect.Field;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 public class FeatureSummaryActivityTest extends TestCase {
 
     public void testAllFeatures() throws Exception {
         int version = Build.VERSION.SDK_INT;
 
         Set<String> expectedFeatures = getFeatureConstants();
 
         Set<String> actualFeatures = new HashSet<String>();
         for (Feature feature : FeatureSummaryActivity.ALL_ECLAIR_FEATURES) {
             actualFeatures.add(feature.name);
         }
         if (version >= Build.VERSION_CODES.FROYO) {
             for (Feature feature : FeatureSummaryActivity.ALL_FROYO_FEATURES) {
                 actualFeatures.add(feature.name);
             }
         }
        if (version >= Build.VERSION_CODES.GINGERBREAD) {
            for (Feature feature : FeatureSummaryActivity.ALL_GINGERBREAD_FEATURES) {
                actualFeatures.add(feature.name);
            }
        }
 
         assertEquals("Feature list needs to be updated.",
                 expectedFeatures.size(), actualFeatures.size());
     }
 
     private static Set<String> getFeatureConstants()
             throws IllegalArgumentException, IllegalAccessException {
         Set<String> features = new HashSet<String>();
         Field[] fields = PackageManager.class.getFields();
         for (Field field : fields) {
             if (field.getName().startsWith("FEATURE_")) {
                 String feature = (String) field.get(null);
                 features.add(feature);
             }
         }
         return features;
     }
 }
