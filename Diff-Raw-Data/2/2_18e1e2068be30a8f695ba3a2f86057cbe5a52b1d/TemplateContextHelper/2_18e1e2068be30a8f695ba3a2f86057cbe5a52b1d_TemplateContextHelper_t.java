 package polly.rx.http;
 
 import java.util.List;
 
 import polly.rx.core.SumQueries;
 import polly.rx.entities.BattleDrop;
 import polly.rx.entities.BattleReport;
 import de.skuzzle.polly.sdk.http.HttpTemplateContext;
 
 
 public class TemplateContextHelper {
 
     public final static void prepareForReportsList(HttpTemplateContext c, 
         List<BattleReport> reports) {
         
         BattleDrop[] dropSum = new BattleDrop[14];
         BattleDrop[] dropMax = new BattleDrop[14];
         BattleDrop[] dropMin = new BattleDrop[14];
         
         int capiXpSumAttacker = 0;
         int crewXpSumAttacker = 0;
         int capiXpSumDefender = 0;
         int crewXpSumDefender = 0;
         int pzDamageAttacker = 0;
         int pzDamageDefender = 0;
         int artifacts = 0;
         
         for (BattleReport report : reports) {
             for (int i = 0; i < 14; ++i) {
                 BattleDrop drop = report.getDrop().get(i);
                 
                 if (dropSum[i] == null) {
                     dropSum[i] = new BattleDrop(drop.getRessource(),
                         drop.getAmount());
                     dropMin[i] = new BattleDrop(drop.getRessource(),
                         drop.getAmount());
                     dropMax[i] = new BattleDrop(drop.getRessource(),
                         drop.getAmount());
                     
                 } else {
                     dropSum[i].incAmout(report.getDrop().get(i).getAmount());
                     dropMin[i].setAmount(Math.min(dropMin[i].getAmount(), 
                         drop.getAmount()));
                    dropMax[i].setAmount(Math.max(dropMax[i].getAmount(), 
                         drop.getAmount()));
                 }
             }
             capiXpSumAttacker += report.querySumAttacker(SumQueries.CAPI_XP);
             crewXpSumAttacker += report.querySumAttacker(SumQueries.CREW_XP);
             capiXpSumDefender+= report.querySumDefender(SumQueries.CAPI_XP);
             crewXpSumDefender += report.querySumDefender(SumQueries.CREW_XP);
             pzDamageAttacker += report.querySumAttacker(SumQueries.PZ_DAMAGE);
             pzDamageDefender += report.querySumDefender(SumQueries.PZ_DAMAGE);
             artifacts += report.hasArtifact() ? 1 : 0;
         }
         
         c.put("capiXpSumAttacker", capiXpSumAttacker);
         c.put("crewXpSumAttacker", crewXpSumAttacker);
         c.put("capiXpSumDefender", capiXpSumDefender);
         c.put("crewXpSumDefender", crewXpSumDefender);
         c.put("pzDamageAttacker", pzDamageAttacker);
         c.put("pzDamageDefender", pzDamageDefender);
         c.put("artifacts", artifacts);
         double chance = reports.isEmpty() 
             ? 0.0 : (double) artifacts / (double) reports.size();
         c.put("chance", chance);
         
         c.put("dropSum", dropSum);
         c.put("dropMax", dropMax);
         c.put("dropMin", dropMin);
         c.put("allReports", reports);
     }
     
     
     
     private TemplateContextHelper() {}
 }
