 package org.jor.rest.report.report;
 
 import java.util.List;
 
 import org.jor.server.services.db.DataService;
 import com.google.visualization.datasource.datatable.DataTable;
 import com.google.visualization.datasource.datatable.value.ValueType;
 import com.google.visualization.datasource.query.Query;
 
 public class InsiderActivityLast14Days extends BaseReport
 {
     public InsiderActivityLast14Days(Query query)
     {
         super(query);
     }
     
     @Override
     public DataTable getData()
     {
         DataService service = DataService.getDataService("metrics-postgres");
         
         String sql =
               "SELECT email," +
               "   (COUNT(event_type_id) - SUM ( CASE WHEN (event_type_id = 16) THEN 1 ELSE 0 END)) AS activity_count,"
             + "    SUM ( CASE WHEN (event_type_id = 0) THEN 1 ELSE 0 END) AS unknown,"
             + "    SUM ( CASE WHEN (event_type_id = 1) THEN 1 ELSE 0 END) AS project_created,"
             + "    SUM ( CASE WHEN (event_type_id = 2) THEN 1 ELSE 0 END) AS project_opened,"
             + "    SUM ( CASE WHEN (event_type_id = 3) THEN 1 ELSE 0 END) AS file_added,"
             + "    SUM ( CASE WHEN (event_type_id = 4) THEN 1 ELSE 0 END) AS file_downloaded,"
             + "    SUM ( CASE WHEN (event_type_id = 5) THEN 1 ELSE 0 END) AS file_deleted,"
             + "    SUM ( CASE WHEN (event_type_id = 6) THEN 1 ELSE 0 END) AS viewer_opened,"
             + "    SUM ( CASE WHEN (event_type_id = 7) THEN 1 ELSE 0 END) AS comment_added,"
             + "    SUM ( CASE WHEN (event_type_id = 8) THEN 1 ELSE 0 END) AS file_comment_added,"
             + "    SUM ( CASE WHEN (event_type_id = 9) THEN 1 ELSE 0 END) AS pin_comment_added,"
             + "    SUM ( CASE WHEN (event_type_id = 10) THEN 1 ELSE 0 END) AS project_owner_added,"
             + "    SUM ( CASE WHEN (event_type_id = 11) THEN 1 ELSE 0 END) AS collaborator_added,"
             + "    SUM ( CASE WHEN (event_type_id = 12) THEN 1 ELSE 0 END) AS limited_collaborator_added,"
             + "    SUM ( CASE WHEN (event_type_id = 13) THEN 1 ELSE 0 END) AS project_owner_deleted,"
             + "    SUM ( CASE WHEN (event_type_id = 14) THEN 1 ELSE 0 END) AS collaborator_deleted,"
             + "    SUM ( CASE WHEN (event_type_id = 15) THEN 1 ELSE 0 END) AS limited_collaborator_deleted"
             + " FROM"
             + " ("
             + "  SELECT m.email, e.member_id, e.event_type_id"
             + "   FROM events e"
             + "        INNER JOIN member_dimension m ON (e.member_id = m.id AND m.email NOT LIKE '%grabcad.com')"
             + "        INNER JOIN project_dimension p ON (e.project_id = p.id AND p.is_private = true)"
             + "   WHERE e.event_time > extract (epoch FROM (now() - interval '14 days'))"
             + " ) a"
             + " GROUP by email"
             + " ORDER BY COUNT(event_type_id)";
 
         List<Object[]> rows = service.runSQLQuery(sql);
 
         // Create a data table,
         addColumn("email", ValueType.TEXT, "Email");
         addColumn("activity_count", ValueType.NUMBER, "Activity Count");
         addColumn("unknown", ValueType.NUMBER, "unknown");
         addColumn("project_created", ValueType.NUMBER, "project_created");
         addColumn("project_opened", ValueType.NUMBER, "project_opened");
         addColumn("file_added", ValueType.NUMBER, "file_added");
         addColumn("file_downloaded", ValueType.NUMBER, "file_downloaded");
         addColumn("file_deleted", ValueType.NUMBER, "file_deleted");
         addColumn("viewer_opened", ValueType.NUMBER, "viewer_opened");
         addColumn("comment_added", ValueType.NUMBER, "comment_added");
         addColumn("file_comment_added", ValueType.NUMBER, "file_comment_added");
         addColumn("pin_comment_added", ValueType.NUMBER, "pin_comment_added");
         addColumn("project_owner_added", ValueType.NUMBER, "project_owner_added");
         addColumn("collaborator_added", ValueType.NUMBER, "collaborator_added");
         addColumn("limited_collaborator_added", ValueType.NUMBER, "limited_collaborator_added");
         addColumn("project_owner_deleted", ValueType.NUMBER, "project_owner_deleted");
         addColumn("collaborator_deleted", ValueType.NUMBER, "collaborator_deleted");
         addColumn("limited_collaborator_deleted", ValueType.NUMBER, "limited_collaborator_deleted");
 
         // Fill the data table.
         for (int i = 0; i < rows.size(); i ++)
         {
             Object[] row = rows.get(i);
             addRow(row);
         }
         
         return getTable();
     }
 }
