 package LinkFuture.Core.DBHelper;
 
 import LinkFuture.Core.DBHelper.Model.FieldInfo;
 import LinkFuture.Core.DBHelper.Model.SPInfo;
 import LinkFuture.Core.Debugger;
 import TestModel.CityInfo;
 import TestModel.CountryInfo;
 import org.junit.Test;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class DBHelperTest {
     @Test
     public void testInsert() throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
         DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/hibuImage","root","MSNbbh2gd");
         helper.addParameter("$ProviderID",1);
         helper.addParameter("$MerchantListID",1);
         helper.addParameter("$ProviderProductID",1);
         helper.executeSP("PSP_FindNextJob");
     }
     @Test
     public void testfindClassFieldInfo() throws Exception {
         HashMap<String,FieldInfo> results= DBHelper.findClassFieldInfo(CityInfo.class);
         assertEquals("testfindClassFieldInfo", results.size(),3);
     }
 
     @Test
     public void testExecuteSPWithOneResult() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             ArrayList<CityInfo> cityList = helper.executeSP(SPName, CityInfo.class);
             assertEquals("One class execute test",cityList.size(),9);
             assertTrue("countryCodeTTTTTT",cityList.get(0).countryCodeTTTTTT!=null);
             assertTrue("Extend.district",cityList.get(0).Extend.district!=null);
             assertEquals("One class execute test,output",helper.getOutputParameterList().get("$test"),(long)9999);
         }
     }
     @Test
     public void testExecuteSPToXml() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             String result = helper.executeSPToXml(SPName);
             Debugger.traceln(result);
             assertTrue("testExecuteSPToXml",result.length()>0);
         }
     }
     @Test
     public void testExecuteSP() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             helper.addParameter("$ID",10);
             helper.addParameter("$Code","ABW");
             helper.addOutParameter("$test");
             ArrayList<ArrayList<?>> result = helper.executeSP(SPName, CityInfo.class,CountryInfo.class);
             ArrayList<CityInfo> cityList = (ArrayList<CityInfo>)result.get(0);
             ArrayList<CountryInfo> countryList = (ArrayList<CountryInfo>)result.get(1);
 //            Debugger.traceln(Utility.convertToXml(cityList));
 //            Debugger.traceln(Utility.convertToXml(countryList));
 //            Debugger.traceln(Utility.convertToXml(helper.getOutputParameterList()));
             assertEquals("One class execute test",cityList.size(),9);
             assertEquals("One class execute test",countryList.size(),1);
             assertEquals("One class execute test,output",helper.getOutputParameterList().get("$test"),(long)9999);
         }
     }
     @Test
     public void testFindSpInfo() throws Exception {
         try(DBHelper helper = new DBHelper("jdbc:mysql://localhost:3306/world","root","MSNbbh2gd")){
             String SPName = "PSP_City_Select";
             SPInfo meta = helper.findSPInfo(SPName);
             assertEquals("jdbc:mysql://localhost:3306/world",meta.dbName);
             assertEquals(SPName,meta.spName);
             assertEquals("$id",meta.parameterList.get(0).parameterName);
         }
     }
 }
