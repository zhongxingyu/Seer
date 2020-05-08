 package polly.rx.http;
 
 import java.util.List;
 
 import polly.rx.core.FleetDBManager;
 import polly.rx.entities.BattleReport;
 import polly.rx.entities.BattleReportShip;
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
         this.prepareContext(report.getAttackerShips(), "Attacker", c);
         this.prepareContext(report.getDefenderShips(), "Defender", c);
 
         return c;
     }
     
     
     
     private void prepareContext(List<BattleReportShip> ships, 
                 String postfix, HttpTemplateContext c) {
         
         int pzDamage = 0;
         int maxPzDamage = 0;
         int minPzDamage = Integer.MAX_VALUE;
         int avgPzDamage = 0;
         
         int shieldDamage = 0;
         int maxShieldDamage = 0;
         int minShieldDamage = Integer.MAX_VALUE;
         int avgShieldDamage = 0;
         
         int capiXp = 0;
         int maxCapiXp = 0;
         int minCapiXp = Integer.MAX_VALUE;
         int avgCapiXp = 0;
         
         int crewXp = 0;
         int maxCrewXp = 0;
         int minCrewXp = Integer.MAX_VALUE;
         int avgCrewXp = 0;
         
         int maxWend = 0;
         int minWend = Integer.MAX_VALUE;
         
         for (BattleReportShip ship : ships) {
             pzDamage += ship.getPzDamage();
             maxPzDamage = Math.max(maxPzDamage, ship.getPzDamage());
             minPzDamage = Math.min(minPzDamage, ship.getPzDamage());
             
             shieldDamage += ship.getShieldDamage();
             maxShieldDamage = Math.max(maxShieldDamage, ship.getShieldDamage());
             minShieldDamage = Math.min(minShieldDamage, ship.getShieldDamage());
             
             capiXp += ship.getCapiXp();
             maxCapiXp = Math.max(maxCapiXp, ship.getCapiXp());
             minCapiXp = Math.min(minCapiXp, ship.getCapiXp());
             
             crewXp += ship.getCrewXp();
             maxCrewXp = Math.max(maxCrewXp, ship.getCrewXp());
             minCrewXp = Math.min(minCrewXp, ship.getCrewXp());
             
             maxWend = Math.max(maxWend, ship.getMaxWend());
             minWend = Math.min(minWend, ship.getMaxWend());
         }
         
         avgPzDamage = pzDamage / ships.size();
         avgShieldDamage = shieldDamage / ships.size();
         avgCapiXp = capiXp / ships.size();
         avgCrewXp = crewXp / ships.size();
         
         c.put("pzDamage" + postfix, pzDamage);
         c.put("maxPzDamage" + postfix, maxPzDamage);
         c.put("minPzDamage" + postfix, minPzDamage);
         c.put("avgPzDamage" + postfix, avgPzDamage);
         c.put("shieldDamage" + postfix, shieldDamage);
         c.put("maxShieldDamage" + postfix, maxShieldDamage);
         c.put("minShieldDamage" + postfix, minShieldDamage);
         c.put("avgShieldDamage" + postfix, avgShieldDamage);
         c.put("capiXp" + postfix, capiXp);
         c.put("maxCapiXp" + postfix, maxCapiXp);
         c.put("minCapiXp" + postfix, minCapiXp);
         c.put("avgCapiXp" + postfix, avgCapiXp);
         c.put("crewXp" + postfix, crewXp);
         c.put("maxCrewXp" + postfix, maxCrewXp);
         c.put("minCrewXp" + postfix, minCrewXp);
         c.put("avgCrewXp" + postfix, avgCrewXp);
         c.put("maxWend" + postfix, maxWend);
         c.put("minWend" + postfix, minWend);
     }
 
 }
