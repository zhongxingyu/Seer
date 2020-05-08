 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package permissions;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author Administrator
  */
 public class PermissionLoader {
 
     private final static int GROUP = 1;
     public static ArrayList<Permissions> arrPermissions = new ArrayList();
     public static ArrayList<Permissions> arrGroupPermissions = new ArrayList();
 
     public void loadPermission(String filename, int type) throws Exception {
         BufferedReader br = new BufferedReader(new FileReader(filename));
         String line;
         while ((line = br.readLine()) != null) {
             StringTokenizer st = new StringTokenizer(line, ":");
             String name = null, description = null, remarks = null;
             int score = -1;
            //System.out.println(line);
             if (st.hasMoreTokens()) {
                 name = st.nextToken();
             }
             if (st.hasMoreTokens()) {
                 description = st.nextToken();
             }
             if (st.hasMoreTokens()) {
                 score = Integer.parseInt(st.nextToken());
             }
             if (st.hasMoreTokens()) {
                 remarks = st.nextToken();
             }
 
             if (name == null || description == null || remarks == null || score == -1) {
                 continue;
             }
             if (type == GROUP) {
                 getArrGroupPermissions().add(new Permissions(name, description, score, remarks));
             } else {
                 getArrPermissions().add(new Permissions(name, description, score, remarks));
             }
         }
         br.close();
     }
 
     /**
      * @return the arrPermissions
      */
     public ArrayList<Permissions> getArrPermissions() {
         return arrPermissions;
     }
 
     /**
      * @param arrPermissions the arrPermissions to set
      */
     public void setArrPermissions(ArrayList<Permissions> arrPermissions) {
         this.arrPermissions = arrPermissions;
     }
 
     /**
      * @return the arrGroupPermissions
      */
     public ArrayList<Permissions> getArrGroupPermissions() {
         return arrGroupPermissions;
     }
 
     /**
      * @param arrGroupPermissions the arrGroupPermissions to set
      */
     public void setArrGroupPermissions(ArrayList<Permissions> arrGroupPermissions) {
         this.arrGroupPermissions = arrGroupPermissions;
     }
 }
