 import org.inb.util.DBQuery;
 import org.inb.dbtask.FixingTasksHub;
 
 /*
 import java.lang.Integer;
 import java.lang.RuntimeException;
 import java.lang.String;
 import java.lang.System;
 */
 import java.lang.Integer;
 import java.lang.String;
 import java.lang.System;
 import java.util.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 /*
 import java.org.inb.util.ArrayList;
 
 import java.org.inb.util.HashMap;
 */
 
 public class TestSqlRunnerJar {
 
   private static String DEFAULT_DB_URL = "jdbc:postgresql://localhost:4321/appform"; // 170613";
   private static String DEFAULT_DB_USR = "gcomesana";
   private static String DEFAULT_DB_PASS = "appform";
   private static String DEFAULT_DB_HOST = "localhost";
 
   public static void main(String argv[]) {
     boolean simulation = true;
     // RemoveMultAnsByDateDBTask rmvdateTask = new RemoveMultAnsByDateDBTask(simulation);
 
     TestSqlRunnerJar tsj = new TestSqlRunnerJar();
 
     String dbUrl = TestSqlRunnerJar.DEFAULT_DB_URL;
     String dbUser = TestSqlRunnerJar.DEFAULT_DB_USR;
     String dbPass = TestSqlRunnerJar.DEFAULT_DB_PASS;
 
     // fs.curateAnswers(dbUrl, dbUser, dbPass, simulation);
     // System.out.println("dbtask.FixTasks: Se fine...");
 
     System.out.println("Starting test...");
     DBQuery dbq = new DBQuery(TestSqlRunnerJar.DEFAULT_DB_URL,
       TestSqlRunnerJar.DEFAULT_DB_USR, TestSqlRunnerJar.DEFAULT_DB_PASS);
 
     try {
       System.out.println("Test 1: DELETE PATIENTS");
       // tsj.delPatientsTest(dbq);
 
       System.out.println("\nTest 2: DELETE INTERVIEWS");
       tsj.delInterviews();
 
       System.out.println("\nTest 3: CHANGE PATIENTS");
       // tsj.changePatientCodes();
     }
     catch (java.lang.Exception ex) {
       ex.printStackTrace();
     }
   }
 
 
   public void delInterviews() throws java.io.UnsupportedEncodingException {
     HashMap<String, ArrayList<String>> hashMap = new HashMap<String, ArrayList<String>>();
     String qes = new String("QES_Español".getBytes("ISO-8859-1"));
     qes = "QES_Español";
     System.out.print("\n" + qes + "-> bytes: ");
 /*
     byte resqes[] = qes.getBytes("ISO-8859-1");
     for (int i=0; i<resqes.length; i++)
       System.out.print(resqes[i]+", ");
 
     System.out.println("\n");
 */
 
     hashMap.put("157071001", new ArrayList<String>(Arrays.asList("Dieta")));
     hashMap.put("157071004", new ArrayList<String>(Arrays.asList("Dieta")));
     hashMap.put("157071005", new ArrayList<String>(Arrays.asList(qes, "Dieta", "Calidad_Entrevista")));
     hashMap.put("157071026", new ArrayList<String>(Arrays.asList("Dieta")));
     hashMap.put("157071027", new ArrayList<String>(Arrays.asList("Dieta")));
     // hashMap.put("157071068", new ArrayList<String>(Arrays.asList(qes)));
 
     hashMap.put("157072023", new ArrayList<String>(Arrays.asList("Calidad_Entrevista")));
     hashMap.put("157072025", new ArrayList<String>(Arrays.asList("Calidad_Entrevista")));
     // hashMap.put("157072050", new ArrayList<String>(Arrays.asList(qes)));
     // hashMap.put("157072202", new ArrayList<String>(Arrays.asList(qes)));
 
 		boolean simulation = true;
    FixingTasksHub fs = new FixingTasksHub(TestSqlRunnerJar.DEFAULT_DB_URL,
      TestSqlRunnerJar.DEFAULT_DB_USR,
      TestSqlRunnerJar.DEFAULT_DB_PASS);

     HashMap<String, Object> jsonMap =
       (HashMap<String, Object>) fs.deleteInterviews(
         TestSqlRunnerJar.DEFAULT_DB_HOST,
         TestSqlRunnerJar.DEFAULT_DB_USR,
         TestSqlRunnerJar.DEFAULT_DB_PASS,
         simulation, hashMap);
 
 
 
 
     String jsonOut = "{\"rows_affected\": "+jsonMap.get("rows_affected").toString()+",";
     jsonOut += "\"samples\": [";
 
     HashMap patSamples = (HashMap)jsonMap.get("pats_with_samples");
     List samples = new ArrayList();
     samples.addAll(patSamples.values());
 
 
     Iterator sampleIt = samples.iterator();
     while (sampleIt.hasNext()) {
       jsonOut += sampleIt.next().toString()+",";
     }
     jsonOut = samples.size()>0? jsonOut.substring(0, jsonOut.length()-1): jsonOut;
     jsonOut += "], \"interviews_deleted\":[";
 
 
     List deletedOnes = (List)jsonMap.get("interviews_deleted");
     Iterator deletedIt = deletedOnes.iterator();
     while (deletedIt.hasNext()) {
      // jsonOut += "\""+deletedIt.next().toString()+"\",";
      List pair = (List)deletedIt.next();
      jsonOut += "{\"codpat\":\""+pair.get(0)+"\", \"intrv\":\""+pair.get(1)+"\"},";
     }
     jsonOut = deletedOnes.size()>0? jsonOut.substring(0, jsonOut.length()-1):jsonOut;
 
     jsonOut += "]}";
 
     System.out.println("DeleteInterviews:\n"+jsonOut);
     /*
     Iterator it = jsonMap.entrySet().iterator();
     while (it.hasNext()) {
       Map.Entry pairs = (Map.Entry) it.next();
       System.out.println("** " + pairs.getKey() + " => " + pairs.getValue());
       it.remove(); // avoids a ConcurrentModificationException
     }
     */
   }
 
 
 
   public void delPatientsTest(DBQuery dbq) {
     System.out.println("delPatientsTest method running...");
 
     FixingTasksHub fs = new FixingTasksHub();
     LinkedHashMap<Integer, String> res =
       (LinkedHashMap<Integer, String>) dbq.getSamples4Patient("188011009");
 
     Set<Integer> enKeys = res.keySet();
 
     System.out.println("Samples for 188011009:");
     for (Iterator<Integer> it = enKeys.iterator(); it.hasNext(); ) {
       Integer key = it.next();
       System.out.println(key + ":" + res.get(key));
     }
 
     ArrayList<String> patients = new ArrayList<String>();
     patients.add("188011009");
     patients.add("157102002");
     patients.add("15750101501");
     patients.add("15750101504");
     patients.add("15750101507");
     patients.add("15750101508");
     patients.add("15750101509");
     patients.add("15750101604");
     patients.add("15750101607");
     patients.add("15750101608");
     patients.add("15750101609");
     patients.add("15750101701");
     patients.add("15750101707");
     patients.add("15750101708");
     patients.add("15750101709");
 
     patients.add("188011320");
     patients.add("15750101807");
     patients.add("15750101808");
     patients.add("15750101809");
     patients.add("15750102001");
     patients.add("15750102101");
     patients.add("15750102104");
     patients.add("15750102107");
     patients.add("15750102108");
     patients.add("15750102109");
 
     patients.add("162131001");
     /*
         HashMap<String, Object> map = new HashMap<String, Object>();
         map.put("patients", patients);
         map.put("sim", true);
     */
 		boolean simulation = true;
     String jsonOut = "";
     HashMap<String, Object> deletions =
       (HashMap<String, Object>) fs.deletePatients(TestSqlRunnerJar.DEFAULT_DB_HOST,
         TestSqlRunnerJar.DEFAULT_DB_USR,
         TestSqlRunnerJar.DEFAULT_DB_PASS,
         simulation, patients);
 
     ArrayList<String> noDeletions = new ArrayList<String>();
     noDeletions = fs.getNoDeletedPatients();
 
     ////////////////////////////////////////////// OUTPUT
     Integer deletedPats = (Integer)deletions.get("rows_affected");
 
     HashMap<String, HashMap<String, Integer>> patientSamples =
                           (HashMap)deletions.get("pats_with_samples");
     Iterator itOne = patientSamples.entrySet().iterator();
     String jsonPat = "";
     while (itOne.hasNext()) {
       Map.Entry pair = (Map.Entry)itOne.next();
       String patientCode = (String)pair.getKey();
 
       HashMap samples = (HashMap)pair.getValue();
       Iterator sampleIt = samples.entrySet().iterator();
       String jsonSamples = "\"samples\": [";
       while (sampleIt.hasNext()) {
         Map.Entry samplePair = (Map.Entry)sampleIt.next();
         jsonSamples += "{\"sample_code\":\""+samplePair.getKey()+"\",";
         jsonSamples += "\"num_of_answers\": "+samplePair.getValue()+"},";
       }
       jsonSamples = jsonSamples.substring(0, jsonSamples.length()-1)+"]";
 
       jsonPat += "{\"patient_code\": \""+patientCode+"\", ";
       jsonPat += jsonSamples+"},";
     }
     jsonPat = jsonPat.substring(0, jsonPat.length()-1);
 
     ArrayList<String> patients_affected = (ArrayList)deletions.get("pats_affected");
     String jsonPatsAff = "\"patients_deleted\": [";
     for (String patientCode: patients_affected)
       jsonPatsAff += "\""+patientCode+"\",";
 
     jsonPatsAff = jsonPatsAff.substring(0, jsonPatsAff.length()-1)+"]";
 
     jsonOut = "{\"deletions\": "+deletedPats+", " + jsonPatsAff+","+
       " \"pats_with_samples\":["+jsonPat+"]}";
     System.out.println("REAL JSON:");
     System.out.println(jsonOut);
     // EO OUTPUT //////////////////////////////////////////////
 
 
 
     Iterator it = deletions.entrySet().iterator();
     jsonOut = "{\"totalDelete\": "+deletions.size()+", {\"deletions\":[";
     while (it.hasNext()) {
       Map.Entry pairs = (Map.Entry)it.next();
       jsonOut += "{\"key\": \""+pairs.getKey() + "\", \"value\":" +
         pairs.getValue() + "},";
       it.remove(); // avoids a ConcurrentModificationException
     }
     jsonOut = jsonOut.substring(0, jsonOut.length()-1)+"]}}";
     System.out.println(jsonOut);
 
 
     jsonOut = "{\"totalNoDelete\": "+noDeletions.size()+", \"nodelete\":[";
     Iterator it2 = noDeletions.iterator();
     while (it2.hasNext()){
       jsonOut += it2.next()+",";
       it2.remove();
     }
     jsonOut = jsonOut.substring(0, jsonOut.length()-1)+"]}";
     System.out.println("jsonResp:\n"+jsonOut);
   }
 
 
   public void changePatientCodes() {
     System.out.println("Change patient codes in progress...");
 
     HashMap<String, String> hashMap = new HashMap<String, String>();
     hashMap.put("157072005", "157072017");
     hashMap.put("157072012", "157072056");
     hashMap.put("157072025", "157072072");
     hashMap.put("157072029", "157072074");
     hashMap.put("157072019", "157072073");
     hashMap.put("157072030", "157072084");
     hashMap.put("157072020", "157072080");
     hashMap.put("157072008", "157072048");
     hashMap.put("157072031", "157072053");
 
 
 		boolean simulation = false;
     FixingTasksHub fs = new FixingTasksHub();
     HashMap<String, Object> jsonMap;
     jsonMap = (HashMap<String, Object>) fs.changeSubjecsCode(
       TestSqlRunnerJar.DEFAULT_DB_HOST,
       TestSqlRunnerJar.DEFAULT_DB_USR,
       TestSqlRunnerJar.DEFAULT_DB_PASS, simulation, hashMap);
 
     Iterator it = jsonMap.entrySet().iterator();
     while (it.hasNext()) {
       Map.Entry pairs = (Map.Entry) it.next();
       System.out.println("** " + pairs.getKey() + " => " + pairs.getValue());
       it.remove(); // avoids a ConcurrentModificationException
     }
   }
 }
