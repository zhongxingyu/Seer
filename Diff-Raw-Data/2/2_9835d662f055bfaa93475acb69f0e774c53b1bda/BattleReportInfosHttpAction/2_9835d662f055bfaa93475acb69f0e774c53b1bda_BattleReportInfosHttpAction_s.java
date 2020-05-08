 package polly.rx.http;
 
 import polly.rx.core.FleetDBManager;
 import polly.rx.core.SumQueries;
 import polly.rx.entities.BattleReport;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.http.HttpAction;
 import de.skuzzle.polly.sdk.http.HttpEvent;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 import de.skuzzle.polly.sdk.http.HttpTemplateException;
 
 
 public class BattleReportInfosHttpAction extends HttpAction {
 
     private FleetDBManager fleetDBManager;
     
     
     
     public BattleReportInfosHttpAction(MyPolly myPolly, 
             FleetDBManager fleetDBManager) {
         super("/battlereport_info", myPolly);
         this.fleetDBManager = fleetDBManager;
         this.requirePermission(FleetDBManager.VIEW_BATTLE_REPORT_PERMISSION);
     }
     
     
 
     @Override
     public HttpTemplateContext execute(HttpEvent e) throws HttpTemplateException {
         HttpTemplateContext c = new HttpTemplateContext("pages/battlereport_info.html");
         
         int id = Integer.parseInt(e.getProperty("id"));
         BattleReport report = this.fleetDBManager.getReportById(id);
         
         c.put("report", report);
         c.put("pzDamageAttacker", report.querySumAttacker(SumQueries.PZ_DAMAGE));
        c.put("pzDamageDefender", report.querySumAttacker(SumQueries.PZ_DAMAGE));
         c.put("damageAttacker", report.querySumAttacker(SumQueries.TOTAL_DAMAGE));
         c.put("damageDefender", report.querySumDefender(SumQueries.TOTAL_DAMAGE));
         c.put("capiXpAttacker", report.querySumAttacker(SumQueries.CAPI_XP));
         c.put("crewXpAttacker", report.querySumAttacker(SumQueries.CREW_XP));
         c.put("capiXpDefender", report.querySumDefender(SumQueries.CAPI_XP));
         c.put("crewXpDefender", report.querySumDefender(SumQueries.CREW_XP));
         return c;
     }
 
 }
