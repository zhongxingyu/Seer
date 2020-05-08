 package ru.exorg.backend.services;
 
 import org.sphx.api.SphinxClient;
 import org.sphx.api.SphinxException;
 import org.sphx.api.SphinxMatch;
 import org.sphx.api.SphinxResult;
 import ru.exorg.core.model.PoiType;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: kate
  * Date: 04-May-2011
  * Time: 23:45:41
  * To change this template use File | Settings | File Templates.
  */
 public class PoiTypeService {
 
     private String sphinx_host;
 
     private int sphinx_port;
 
     private SphinxClient sphinxClient;
 
     private static final int Id = 0;
     private static final int Name = 1;
 
 
     public PoiTypeService() {
         sphinx_host = "localhost";
         sphinx_port = 9312;
         this.sphinxClient = new SphinxClient(sphinx_host, sphinx_port);
 
         try {
             this.sphinxClient.SetMatchMode(SphinxClient.SPH_MATCH_EXTENDED);
         } catch (Exception e) {
             System.out.println("Sun has raised in the west today :(");
         }
     }
 
     private PoiType getPOIFromMatch(SphinxMatch match)
     {
         ArrayList<String> inf = match.attrValues;
 
         long id = Long.parseLong(inf.get(Id));
         String name = inf.get(Name);
        name = name.substring(0, name.length()-5);
        System.out.println("getPOIFromMatch: "+name);
         PoiType poiType = new PoiType(id, name);
 
         return poiType;
     }
 
     public PoiType getPoiTypeById(long id)
     {
         PoiType poiType = null;
         try {
             SphinxResult result = sphinxClient.Query("@type_id " + String.valueOf(id), "poi_type_index");
 
             for(SphinxMatch match: result.matches)
             {
                 poiType = getPOIFromMatch(match);
                 if(poiType.getId() == id)
                 {
                     return poiType;
                 }
             }
 
         } catch (SphinxException e) {
             System.out.println(e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 
         }
         return poiType = null;
     }
 
     public PoiType getPoiTypeByName(String name)
     {
         PoiType poiType = null;
         try {
            String searchName = name + " all ";
            SphinxResult result = sphinxClient.Query("@name " + searchName, "poi_type_index");
            //System.out.println("getPoiTypeByName: search by name " + searchName);
 
             for(SphinxMatch match: result.matches)
             {
                 poiType = getPOIFromMatch(match);
                //System.out.println("getPoiTypeByName: found poi_type name: "+ poiType.getName());
                 if(poiType.getName().equals(name))
                 {
                     return poiType;
                 }
             }
 
         } catch (SphinxException e) {
             System.out.println(e.getMessage());
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 
         }
         return poiType = null;
     }
 
     public List<PoiType> getPoiTypes()
     {
         List<PoiType> typeList = new ArrayList<PoiType>();
         try {
            SphinxResult result = sphinxClient.Query("@* all", "poi_type_index");
             for (SphinxMatch match: result.matches)
             {
                //System.out.println("getPoiTypes");
                 typeList.add(getPOIFromMatch(match));
                //System.out.println(typeList.get(typeList.size()-1).toString());
             }
 
         } catch (SphinxException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         return typeList;
     }
 
   /*
     private JdbcTemplate jdbcTemplate;
 
     public JdbcTemplate getJdbcTemplate() {
         return jdbcTemplate;
     }
 
     @Required
     public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 
     private PoiType getPoiTypeById (final long id) {
         String q = String.format("SELECT name FROM poi_type WHERE id = %d;", id);
         RowMapper<PoiType> mapper = new RowMapper<PoiType>() {
             public PoiType mapRow(ResultSet rs, int i) throws SQLException {
                 PoiType poiType = new PoiType(id, rs.getString("name"));
                 return poiType;
             }
         };
         List<PoiType> list = jdbcTemplate.query(q, mapper);
         if (list != null)
             return list.get(0);
         return null;
     }
 
     public List<PoiType> getPoiTypes () {
         String q = new String("SELECT id FROM poi_type;");
         List<Integer> typeIds = jdbcTemplate.queryForList(q, Integer.class);
         if(!typeIds.isEmpty())
         {
             List<PoiType> result = new ArrayList<PoiType>();
             for(int i: typeIds)
             {
                  result.add(getPoiTypeById(i));
             }
             return result;
         }
         return null;
     }
 
     */
 }
