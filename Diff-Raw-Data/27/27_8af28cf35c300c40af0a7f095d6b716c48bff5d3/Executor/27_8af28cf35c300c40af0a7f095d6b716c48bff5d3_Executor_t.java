 package ncl.military.controller.handle.executors;
 
 import ncl.military.controller.handle.Executable;
 import ncl.military.dao.DAO;
 import ncl.military.dao.tools.Alias;
 import ncl.military.dao.tools.EntityValue;
 import ncl.military.dao.tools.Filter;
 import ncl.military.dao.tools.FilterType;
 import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author gural
  * @version 1.0
  *          Date: 20.04.12
  *          Time: 11:37
  */
 public abstract class Executor implements Executable {
 
    protected static final Logger log = Logger.getLogger("controller");

     private DAO dao;
     protected Executor executor;
 
     public Executor(DAO dao) {
         this.dao = dao;
     }
 
     public Executor(Executor executor) {
         this.executor = executor;
     }
 
     protected DAO getDao() {
         if (dao == null)
             return executor.getDao();
         else
             return dao;
     }
 
     private Executor getExecutor() {
         return executor;
     }
 
     public Executor setExecutor(Executor executor) {
         this.executor = executor;
         return this.executor;
     }
 
     protected List<Filter> getFiltersOfSearch(List<Alias> aliases, Map<String, Object> params) {
         List<Filter> filters = new ArrayList<Filter>();
         for (Alias alias : aliases) {
             String param = (String) params.get(alias.getLabelAsQueried());
             if (StringUtils.isNotEmpty(param)) {
                 filters.add(new Filter(alias, FilterType.LIKE, param));
             }
         }
         return filters;
     }
 
     /**
      * Gets values from params using aliases of attributes from POJO
      *
      * @param aliases list of names of attributes oj POJO
      * @param params  must contains this aliases
      * @return
      */
     protected List<EntityValue> getValues(List<Alias> aliases, Map<String, Object> params) {
         List<EntityValue> values = new ArrayList<EntityValue>();
         for (Alias alias : aliases) {
             String param = (String) params.get(alias.getLabelAsQueried());
             if (StringUtils.isNotEmpty(param)) {
                 values.add(new EntityValue(alias, param));
             }
         }
         return values;
     }
 
     protected List<EntityValue> getValuesOfSolider(Map<String, Object> params) {
         return getValues(
                 new ArrayList<Alias>() {{
                     add(Alias.SOLDIER_NAME);
                     add(Alias.SOLDIER_UNIT);
                     add(Alias.SOLDIER_RANK);
                     add(Alias.SOLDIER_COMMANDER);
                     add(Alias.SOLDIER_BIRTHDATE);
                 }}
                 , params);
     }
 
     protected List<EntityValue> getValuesOfUnit(Map<String, Object> params) {
         List<EntityValue> values = getValues(
                 new ArrayList<Alias>() {{
                     add(Alias.UNIT_NAME);
                     add(Alias.UNIT_LOCATION);
                     add(Alias.UNIT_HEAD_ID);
                 }}
                 , params);
         if (params.get(Alias.UNIT_HEAD_ID.getLabelAsQueried()) != null)
             values.add(new EntityValue(Alias.UNIT_HEAD_NAME, getDao().getSoldierById((String) params.get(Alias.UNIT_HEAD_ID.getLabelAsQueried())).getName()));
         return values;
     }
 
     protected List<EntityValue> getValuesOfLocation(Map<String, Object> params) {
         return getValues(
                 new ArrayList<Alias>() {{
                     add(Alias.LOCATION_NAME);
                     add(Alias.LOCATION_REGION);
                     add(Alias.LOCATION_CITY);
                 }}
                 , params);
     }
 }
