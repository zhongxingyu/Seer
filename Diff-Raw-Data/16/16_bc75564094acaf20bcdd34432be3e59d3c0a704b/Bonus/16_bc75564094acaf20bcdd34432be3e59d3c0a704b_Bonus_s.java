 package pl.edu.agh.to1.dice.logic.figures;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  * @author Michal Partyka
  */
 public class Bonus {
     private final List<IFigure> countedList;
     private final Integer require;
     private final Integer bonus;
     private Integer points=0;
     private final String name;
 
     public Bonus(List<IFigure> countedList, Integer require, Integer bonus, String name) {
         this.countedList = countedList;
         this.require = require;
         this.bonus = bonus;
         this.name = name;
     }
 
     public List<IFigure> getCountedList() {
         return countedList;
     }
 
     public Integer getRequire() {
         return require;
     }
 
     public Integer getBonus() {
         return bonus;
     }
 
     public void countBonus(final Map<IFigure, Integer> map) {
        Integer sum=0;
         for (Map.Entry<IFigure, Integer> point : map.entrySet()) {
             if (countedList.contains(point.getKey())) {
                 sum += point.getValue();
             }
         }
         if (sum>=require) {
             points = bonus;
         } else {
             points = 0;
         }
     }
 
     public Integer getPoints() {
         return points;
     }
 
     @Override
     public String toString() {
         return name + " " + points;
     }
 
     public String getName() {
         return name;
     }
 }
