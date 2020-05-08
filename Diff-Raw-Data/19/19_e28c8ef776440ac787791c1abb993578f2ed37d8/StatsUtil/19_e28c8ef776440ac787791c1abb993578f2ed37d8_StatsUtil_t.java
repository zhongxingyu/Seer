 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cdiddy.objects.util;
 
 import cdiddy.objects.Player;
 import cdiddy.objects.Stat;
 import cdiddy.objects.WeeklyStat;
 import cdiddy.objects.league.YahooLeague;
 import cdiddy.objects.league.YahooLeagueStatModifier;
 import cdiddy.objects.league.YahooLeagueStatModifierBonus;
 import cdiddy.objects.league.YahooLeagueStatModifierBonusList;
 import cdiddy.objects.league.YahooLeagueStatModifierObj;
 import java.math.BigDecimal;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author cedric
  */
 public class StatsUtil 
 {
     
     public static BigDecimal calculateFantasyPointsForWeek (Player p, YahooLeague yl, int week)
     {
         return calculateFantasyPoints (p, yl, false, week);
     }
     
     public static BigDecimal calculateFantasyPoints (Player p, YahooLeague yl, boolean isSeason, int week)
     {
         BigDecimal result =  new BigDecimal(0);
          List<WeeklyStat> playerStats = p.getWeeklyStats();
             if(!isSeason)
             {
                 Map<String, WeeklyStat> statMap = new HashMap<String, WeeklyStat>();
                 for (WeeklyStat weeklyStat : playerStats)
                 {
                     statMap.put(weeklyStat.getWeek(), weeklyStat);
                 }
                 WeeklyStat playerWeekStat = statMap.get(Integer.toString(week));
                 if(playerWeekStat != null)
                 {
                     List<YahooLeagueStatModifierObj> modifierList = yl.getSettings().getStat_modifiers().getStats().getStat();
                     Map<String, YahooLeagueStatModifierObj> modifierMap = new HashMap<String, YahooLeagueStatModifierObj>();
                     for (YahooLeagueStatModifierObj statModifier : modifierList)
                     {
                         modifierMap.put(statModifier.getStat_id(), statModifier);
                     }
                     
                     for(Stat stat : playerWeekStat.getStats())
                     {
                         YahooLeagueStatModifierObj currModifier = modifierMap.get(stat.getStat_id());
                        YahooLeagueStatModifierBonusList currBonus = null;
                        BigDecimal modifierValue = new BigDecimal(0);
                        
                        if(currModifier != null)
                        {
                            currBonus= currModifier.getBonuses();
                            modifierValue = new BigDecimal(currModifier.getValue());
                            
                        }                           
                         BigDecimal statValue = new BigDecimal(stat.getValue());
                         BigDecimal statYahooValue = statValue.multiply(modifierValue);
                         BigDecimal statYahooBonus = new BigDecimal(0);
                         if(currBonus != null)
                         {
                             for(YahooLeagueStatModifierBonus bonus : currBonus.getBonus())
                             {
                                BigDecimal bonusTarget = new BigDecimal(bonus.getTarget());
                                if (statValue.compareTo(bonusTarget) > -1)
                                {
                                    statYahooBonus.add(new BigDecimal(bonus.getPoints()));
                                }
                             }
                         }
                         result = result.add(statYahooValue).add(statYahooBonus);
                     }
                 }
                 
                 
             }
         
         return result;
     }
     
 }
