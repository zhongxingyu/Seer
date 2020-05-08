 package jetbrick.schema.app.task;
 
 import java.util.Map;
 import jetbrick.dao.dialect.Dialect;
 import jetbrick.schema.app.Task;
 import jetbrick.schema.app.model.TableInfo;
 
 public class HibernateTask extends Task {
 
     public HibernateTask() {
         name = "hibernate";
     }
 
     @Override
     public void execute() throws Throwable {
         String packagePath = getPackagePath();
 
         // output hibernate file for each table.
         for (TableInfo table : schema.getTables()) {
             Map<String, Object> context = getTemplateContext();
             context.put("table", table);
 
             for (Dialect dialect : getDialects()) {
                 schema.setDialect(dialect);
                 writeFile("pojo.hbm.xml.httl", packagePath + "/data/hbm_" + dialect.getName() + "/" + table.getTableClass() + ".hbm.xml", context);
             }
 
             writeFile("pojo.java.httl", packagePath + "/data/" + table.getTableClass() + ".java", context);
         }
 
         Map<String, Object> context = getTemplateContext();
         for (Dialect dialect : getDialects()) {
             schema.setDialect(dialect);
             writeFile("sessionFactory.xml.httl", "xml/" + dialect.getName() + "/appContext-sessionFactory.xml", context);
         }
     }
 
 }
