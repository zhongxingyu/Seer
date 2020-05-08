 /**
  * JBoss, Home of Professional Open Source
  * Copyright Red Hat, Inc., and individual contributors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.jboss.aerogear.unifiedpush.utils;
 
 import static org.junit.Assert.assertNotNull;
 
 import java.util.Map;
 
 import org.jboss.aerogear.unifiedpush.model.InstallationImpl;
 import org.json.simple.JSONObject;
 
 import com.jayway.restassured.RestAssured;
 import com.jayway.restassured.response.Response;
 
 public final class InstallationUtils {
 
     private InstallationUtils() {
     }
 
     public static InstallationImpl createInstallation(String deviceToken, String deviceType, String operatingSystem,
             String osVersion, String alias, String category, String simplePushEndpoint) {
         InstallationImpl installation = new InstallationImpl();
         installation.setDeviceToken(deviceToken);
         installation.setDeviceType(deviceType);
         installation.setOperatingSystem(operatingSystem);
         installation.setOsVersion(osVersion);
         installation.setAlias(alias);
         installation.setCategory(category);
         installation.setSimplePushEndpoint(simplePushEndpoint);
         return installation;
     }
 
     @SuppressWarnings("unchecked")
     public static Response registerInstallation(String variantID, String secret, InstallationImpl installation, String root) {
 
         assertNotNull(root);
 
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("deviceToken", installation.getDeviceToken());
         jsonObject.put("deviceType", installation.getDeviceType());
         jsonObject.put("operatingSystem", installation.getOperatingSystem());
         jsonObject.put("osVersion", installation.getOsVersion());
         jsonObject.put("alias", installation.getAlias());
         jsonObject.put("category", installation.getCategory());
         jsonObject.put("simplePushEndpoint", installation.getSimplePushEndpoint());
 
         Response response = RestAssured.given().contentType("application/json").auth().basic(variantID, secret)
                 .header("Accept", "application/json").body(jsonObject.toString()).post("{root}rest/registry/device", root);
 
         return response;
     }
 
     public static Response unregisterInstallation(String variantID, String secret, String token, String root) {
         assertNotNull(root);
         Response response = RestAssured.given().contentType("application/json").auth().basic(variantID, secret)
                 .delete("{root}rest/registry/device/{token}", root, token);
 
         return response;
     }
 
     /* methods of the InstallationManagementEndpoint used by the Admin UI begin */
 
     public static Response findInstallations(String variantID, Map<String, ?> cookies, String root) {
 
         assertNotNull(root);
 
         Response response = RestAssured.given().contentType("application/json").cookies(cookies)
                 .header("Accept", "application/json")
                 .get("{root}rest/applications/{variantID}/installations/", root, variantID);
 
         return response;
     }
 
     public static Response findInstallation(String variantID, String installationID, Map<String, ?> cookies, String root) {
 
         assertNotNull(root);
 
         Response response = RestAssured.given().contentType("application/json").cookies(cookies)
                 .header("Accept", "application/json")
                 .get("{root}rest/applications/{variantID}/installations/{installationID}", root, variantID, installationID);
 
         return response;
     }
 
     @SuppressWarnings("unchecked")
     public static Response updateInstallation(String variantID, String installationID, InstallationImpl installation,
             Map<String, ?> cookies, String root) {
 
         assertNotNull(root);
 
         JSONObject jsonObject = new JSONObject();
         jsonObject.put("deviceToken", installation.getDeviceToken());
         jsonObject.put("deviceType", installation.getDeviceType());
         jsonObject.put("operatingSystem", installation.getOperatingSystem());
         jsonObject.put("osVersion", installation.getOsVersion());
         jsonObject.put("alias", installation.getAlias());
         jsonObject.put("category", installation.getCategory());
         jsonObject.put("simplePushEndpoint", installation.getSimplePushEndpoint());
 
         Response response = RestAssured.given().contentType("application/json").cookies(cookies)
                 .header("Accept", "application/json").body(jsonObject.toString())
                .put("{root}rest/applications/{variantID}/installations/{installationID}");
 
         return response;
     }
 
     public static Response removeInstallation(String variantID, String installationID, Map<String, ?> cookies, String root) {
 
         assertNotNull(root);
 
         Response response = RestAssured.given()
                 // .contentType("application/json")
                 .cookies(cookies).header("Accept", "application/json")
                 .delete("{root}rest/applications/{variantID}/installations/{installationID}", root, variantID, installationID);
 
         return response;
     }
     /* methods of the InstallationManagementEndpoint used by the Admin UI end */
 }
