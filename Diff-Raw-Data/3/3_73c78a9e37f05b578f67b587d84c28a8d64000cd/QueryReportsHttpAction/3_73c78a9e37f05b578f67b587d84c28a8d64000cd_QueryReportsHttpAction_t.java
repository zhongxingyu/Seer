 package polly.rx.http;
 
 import java.util.List;
 
 import polly.rx.core.FleetDBManager;
 import polly.rx.entities.BattleReport;
 
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.InsufficientRightsException;
 import de.skuzzle.polly.sdk.http.HttpAction;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 import de.skuzzle.polly.sdk.http.HttpTemplateSortHelper;
 
 
 public class QueryReportsHttpAction extends HttpAction {
 
     private FleetDBManager fleetDBManager;
     
     public QueryReportsHttpAction(MyPolly myPolly, FleetDBManager fleetDBManager) {
         super("/query_reports", myPolly);
         this.fleetDBManager = fleetDBManager;
         this.requirePermission(FleetDBManager.VIEW_BATTLE_REPORT_PERMISSION);
     }
     
     
 
     @Override
     public HttpTemplateContext execute(HttpEvent e) throws 
             HttpTemplateException, InsufficientRightsException {
         
         HttpTemplateContext c = new HttpTemplateContext("pages/query_reports.html");
         
         String action = e.getProperty("action");
         boolean isDelete = e.getProperty("delete") != null;
         boolean isSelect = e.getProperty("select") != null;
        String query = e.getProperty("query") == null ? "" : e.getProperty("query");
        
         c.put("action", action);
         c.put("query", query);
         
         if (action != null && action.equals("idSelect") && isSelect) {
             int[] ids = this.getIdList(e);
             List<BattleReport> reports = this.fleetDBManager.getReportByIdList(ids);
             TemplateContextHelper.prepareForReportsList(c, reports);
         } else if (action != null && action.equals("idSelect") && isDelete) {
             
             if (!this.getMyPolly().roles().hasPermission(e.getSession().getUser(), 
                     FleetDBManager.DELETE_BATTLE_REPORT_PERMISSION)) {
                 throw new InsufficientRightsException(this);
             }
             
             int[] ids = this.getIdList(e);
             
             try {
                 this.fleetDBManager.deleteReportByIdList(ids);
             } catch (DatabaseException e1) {
                 e.throwTemplateException(e1);
             }
         } else if (action != null && action.equals("byClan")) {
             List<BattleReport> reports = this.fleetDBManager.getReportsWithClan(query);
             TemplateContextHelper.prepareForReportsList(c, reports);
         } else if (action != null && action.equals("byLocation")) {
             List<BattleReport> reports = this.fleetDBManager.getReportsByLocation(query);
             TemplateContextHelper.prepareForReportsList(c, reports);
         } else if (action != null && action.equals("byVenad")) {
             List<BattleReport> reports = this.fleetDBManager.getReportsWithVenad(query);
             TemplateContextHelper.prepareForReportsList(c, reports);
         } else if (action != null && action.equals("byMe")) {
             List<BattleReport> reports = this.fleetDBManager.getReportByUserId(
                 e.getSession().getUser().getId());
             TemplateContextHelper.prepareForReportsList(c, reports);
         } 
         
         HttpTemplateSortHelper.makeListSortable(c, e, "sortKey", "dir", "getDate");
         
         return c;
     }
     
     
     
     private int[] getIdList(HttpEvent e) {
         String[] stringIds = e.getProperty("selectRP").split(";");
         int ids[] = new int[stringIds.length];
         
         for (int i = 0; i < stringIds.length; ++i) {
             ids[i] = Integer.parseInt(stringIds[i]);
         }
         return ids;
     }
 
 }
