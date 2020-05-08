 /*
  GossipNetSim
  Copyright (C) 2012  michael theodorides <mc.theodorides@gmail.com>
 
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gossipsettingsgeneraitor;
 
 import java.sql.ResultSet;
 import us.elfua.gossipnetsim.helpers.data.CustomSQLConnection;
 
 /**
  *
  * @author michael
  */
 public class GossipSettingsGeneraitor {
 
     /**
      * @param args the command line arguments
      */
     private static String name = "simulation_variable_q_and_r_size_1000";
     private static int time = -1;
     private static int simulationSize = 100;
     private static int gossipNum = 1;
     private static String geometryType = "euclidean_circle";
     private static String networkType = "circle_network";
     private static String algorithmType = "spatial";
     private static double dencity = 1.0;
     private static int injectionRate = 1;
     private static double nodeRange = -1.0;
     private static double Qvar = 0.5;
     private static double Rvar = 0.5;
     private static CustomSQLConnection con;
 
     public static void main(String[] args) throws Exception {
 
         gen();
     }
 
     private static void gen() throws Exception {
         con = new CustomSQLConnection();
         String sql;
         ResultSet res;
 
 
 
         sql = "INSERT INTO batch_simulations ( descriptetion ) VALUES ( '"
                 + name
                 + "' );";
         con.nonQuery(sql);
 
 
 
         sql = "select currval('batch_simulations_id_seq');";
         res = con.Query(sql);
 
         int bachId = 0;
         if (res.next()) {
             bachId = res.getInt(1);
         }
 
 
         boolean QFlag = true;
         boolean RFlag = true;
         boolean loop = true;
 
         while (loop) {
 
             double q = 1;
             double r = 0;
 
             while (QFlag) {
                 r = 0;
                 RFlag = true;
                 while (RFlag) {
 
                     sql = "INSERT INTO settings "
                             + "("
                             + "time_length, "
                             + "simulation_size, "
                             + "number_of_gossips, "
                             + "geometry_Type, "
                             + "network_type, "
                             + "algorithm_type, "
                             + "dencity, "
                             + "injection_rate, "
                             + "node_range, "
                             + "Qvar, "
                             + "Rvar, "
                             + "Kvar, "
                             + "Yvar) "
                             + "VALUES "
                             + "("
                             + "'" + time + "', "
                             + "'" + simulationSize + "', "
                             + "'" + gossipNum + "', "
                             + "'" + geometryType + "', "
                             + "'" + networkType + "', "
                             + "'" + algorithmType + "', "
                             + "'" + dencity + "', "
                             + "'" + injectionRate + "', "
                             + "'" + nodeRange + "', "
                             + "'" + q + "', "
                             + "'" + r + "', "
                             + "'-1', "
                             + "'-1' "
                             + ");";
 
 
                     con.nonQuery(sql);
 
 
                     sql = "select currval('settings_id_seq');";
                     res = con.Query(sql);
 
                     int settingsId = 0;
                     if (res.next()) {
                         settingsId = res.getInt(1);
                     }
 
                     sql = "INSERT INTO simulations ( "
                            + "babch_simulation_id, "
                             + "descriptetion, "
                             + "settings_id) VALUES ( "
                             + "'" + bachId + "', "
                             + "'" + name + q + r + "', "
                             + "'" + settingsId + "');";
 
                     con.nonQuery(sql);
 
                     r += Rvar;
                     if (r < 1.0001 && r > 0.9999) {
                         RFlag = false;
                     }
                 }
                 q += Qvar;
                 if (q < 2.0001 && q > 1.9999) {
                     QFlag = false;
                 }
             }
             loop = false;
         }
     }
 }
