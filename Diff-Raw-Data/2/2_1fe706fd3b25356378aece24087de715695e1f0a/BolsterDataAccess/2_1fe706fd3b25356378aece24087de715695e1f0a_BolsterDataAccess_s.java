 package dataaccess;
 
 import celcius.Config;
 import com.mysql.jdbc.Connection;
 import com.mysql.jdbc.Statement;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 public class BolsterDataAccess {
 
     private static Connection connect;
     private static Statement statement;
     private static BolsterDataAccess instance;
     private ResultSet resultSet;
 
     private BolsterDataAccess() throws SQLException {
         connect = (Connection) DriverManager.getConnection(Config.connectionString);
 
     }
 
     public static BolsterDataAccess getInstance() throws SQLException {
         if (BolsterDataAccess.instance == null) {
             BolsterDataAccess.instance = new BolsterDataAccess();
         }
         return BolsterDataAccess.instance;
     }
 
     public String[] getProductRanges() throws SQLException {
         return new String[]{"Classic", "Super", "Other"};
     }
 
     public String[] getMaterialTypes() throws SQLException {
         statement = (Statement) connect.createStatement();
         resultSet = statement.executeQuery("select name from celcius.fabrics");
         ArrayList<String> arr = new ArrayList<String>();
         while (resultSet.next()) {
             arr.add(resultSet.getString("name"));
         }
         String[] arr2 = new String[arr.size()];
         for (int i = 0; i < arr2.length; i++) {
             arr2[i] = arr.get(i);
         }
         return arr2;
     }
 
     public String[] getMaterialTypes(String range) throws SQLException {
         statement = (Statement) connect.createStatement();
         if (range.equalsIgnoreCase("Classic") || range.equalsIgnoreCase("Super")) {
             resultSet = statement.executeQuery("select name from celcius.fabrics where " + range + " = true");
         } else {
             resultSet = statement.executeQuery("select name from celcius.fabrics");
         }
         ArrayList<String> arr = new ArrayList<String>();
         while (resultSet.next()) {
             arr.add(resultSet.getString("name"));
         }
         String[] arr2 = new String[arr.size()];
         for (int i = 0; i < arr2.length; i++) {
             arr2[i] = arr.get(i);
         }
         return arr2;
     }
 
     public String[] getBolsterSizes(String range) throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select size from celcius.bolsters where `range` = '" + range + "'";
         resultSet = statement.executeQuery(sql);
         ArrayList<String> arr = new ArrayList<String>();
         while (resultSet.next()) {
             arr.add(resultSet.getString("size"));
         }
         String[] arr2 = new String[arr.size()];
         for (int i = 0; i < arr2.length; i++) {
             arr2[i] = arr.get(i);
         }
         return arr2;
     }
 
     public Double getMaterialPrice(String material) throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.fabrics where name ='" + material + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public Double getWidthShrinkage(String material) throws Exception {
         statement = (Statement) connect.createStatement();
         String sql = "select width_shrinkage from celcius.fabrics where name ='" + material + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("width_shrinkage"));
     }
 
     public Double getHeightShrinkage(String material) throws Exception {
         statement = (Statement) connect.createStatement();
         String sql = "select height_shrinkage from celcius.fabrics where name ='" + material + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("height_shrinkage"));
     }
 
     public Integer getMaterialWidth(String material) throws Exception {
         statement = (Statement) connect.createStatement();
         String sql = "select width from celcius.fabrics where name ='" + material + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Integer.parseInt(resultSet.getString("width"));
     }
 
     public Double getSMVValue(String size, String range) throws SQLException {
         if (range.equalsIgnoreCase("Other")) {
             range = "Classic";
         }
         statement = (Statement) connect.createStatement();
         String sql = "select smv from celcius.bolsters where size ='" + size + "' and `range` ='" + range + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("smv"));
     }
 
     public Double getLableCost() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.bolster_accessories where name ='Lable'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public Double getTagCost() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.bolster_accessories where name ='Tag'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public Double getThreadCost() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.bolster_accessories where name ='Thread'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public Double getPEBag() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.bolster_accessories where name ='PE Bag'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public Double getCostPerLabourMinute() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select value from celcius.parameters where name ='cost per labour minute'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("value"));
     }
 
     public Double getPOHValue() throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select value from celcius.parameters where name ='POH per minute'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("value"));
     }
 
     public double[][] getSMVXYPairs(String range) throws SQLException {
         if (range.equalsIgnoreCase("Other")) {
             range = "Classic";
         }
         statement = (Statement) connect.createStatement();
         String sql = "select size,smv from celcius.bolsters where `range` ='" + range + "'";
         resultSet = statement.executeQuery(sql);
         ArrayList<String> sizes = new ArrayList<String>();
         ArrayList<Double> smvs = new ArrayList<Double>();
         while (resultSet.next()) {
             sizes.add(resultSet.getString("size"));
             smvs.add(Double.parseDouble(resultSet.getString("smv")));
         }
 
         double[][] arr = new double[2][sizes.size() + 1];
         //smv value should be zero at 0
         arr[0][0] = arr[1][0] = 0.0;
         for (int i = 0; i < sizes.size(); i++) {
             arr[0][i + 1] = 2 * 3.141 * Double.parseDouble(sizes.get(i).split("X")[0]) +
                     Double.parseDouble(sizes.get(i).split("X")[1]);
             arr[1][i + 1] = smvs.get(i);
         }
         return arr;
     }
 
     public String[] getFiberTypes() throws SQLException {
         statement = (Statement) connect.createStatement();
         resultSet = statement.executeQuery("select distinct name from celcius.fibers");
         ArrayList<String> arr = new ArrayList<String>();
         while (resultSet.next()) {
             arr.add(resultSet.getString("name"));
         }
         String[] arr2 = new String[arr.size()];
         for (int i = 0; i < arr2.length; i++) {
             arr2[i] = arr.get(i);
         }
         return arr2;
     }
 
     public String[] getFiberTypes(String range) throws SQLException {
         statement = (Statement) connect.createStatement();
         if (range.equalsIgnoreCase("Classic") || range.equalsIgnoreCase("Super")) {
             resultSet = statement.executeQuery("select distinct name from celcius.fibers where " + range.toLowerCase() + "=true");
         } else {
             resultSet = statement.executeQuery("select distinct name from celcius.fibers");
         }
         ArrayList<String> arr = new ArrayList<String>();
         while (resultSet.next()) {
             arr.add(resultSet.getString("name"));
         }
         String[] arr2 = new String[arr.size()];
         for (int i = 0; i < arr2.length; i++) {
             arr2[i] = arr.get(i);
         }
         return arr2;
     }
 
     public Double getFiberPrice(String fiberType) throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select price from celcius.fibers where name ='" + fiberType + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         return Double.parseDouble(resultSet.getString("price"));
     }
 
     public double[][] getFiberWeights(String name) throws SQLException {
         statement = (Statement) connect.createStatement();
         String sql = "select * from celcius.bolster_fiber_weights where name ='" + name + "'";
         resultSet = statement.executeQuery(sql);
         resultSet.next();
         double[][] result = new double[2][7];
         result[0][0] = 0.0;
         result[1][0] = 0.0;
         int count = 1;
        double val = Double.parseDouble(resultSet.getString("3.5_16"));
         if (val > 0) {
             result[0][count] = 3.5*3.5*16;
             result[1][count] = val;
             count++;
         }
 
         val = Double.parseDouble(resultSet.getString("4_18"));
         if (val > 0) {
             result[0][count] = 4.0*4.0*18;
             result[1][count] = val;
             count++;
         }
 
         val = Double.parseDouble(resultSet.getString("8_18"));
         if (val > 0) {
             result[0][count] = 8.0 * 8.0 * 18;
             result[1][count] = val;
             count++;
         }
 
         val = Double.parseDouble(resultSet.getString("8_24"));
         if (val > 0) {
             result[0][count] = 8.0 * 8.0 * 24;
             result[1][count] = val;
             count++;
         }
 
         val = Double.parseDouble(resultSet.getString("8_40"));
         if (val > 0) {
             result[0][count] = 8.0 * 8.0 * 40;
             result[1][count] = val;
             count++;
         }
 
         val = Double.parseDouble(resultSet.getString("10_30"));
         if (val > 0) {
             result[0][count] = 10.0 * 10.0 * 30;
             result[1][count] = val;
             count++;
         }
 
         double[][] rst = new double[2][];
         rst[0] = Arrays.copyOf(result[0], count);
         rst[1] = Arrays.copyOf(result[1], count);
 
         return rst;
     }
 }
