 package models;
 
 
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import models.utils.Utils;
 
 import javax.annotation.Nullable;
 import java.util.*;
 
 /**
  * Gives an overview of all informations which are required in GUI .
  *
  * @author fsznajderman
  *         date :  03/03/12
  */
 public class ActivityOverView {
 
     private List<ActivityWrapper> activityWrappers;
     private static final Long ZERO = 0l;
 
 
     public String getBestNamePost() {
         ActivityWrapper aw = Ordering.natural().onResultOf(new CompileBestPost()).greatestOf(activityWrappers, 1).get(0);
         return Utils.getActivityAuthor(aw);
     }
 
     public String getBestTitrePost() {
         ActivityWrapper aw = Ordering.natural().onResultOf(new CompileBestPost()).greatestOf(activityWrappers, 1).get(0);
 
         return Utils.getActivityTitle(aw);
     }
 
     public String getPlusOneMatrix() {
         Map<Date, Long> matrix = Maps.newTreeMap();
         for (ActivityWrapper aw : activityWrappers) {
             matrix.put(new Date(aw.getPublicationDate().getValue()), aw.getNbPlusOners());
         }
 
         return Joiner.on(",").join(matrix.values());
     }
 
     public String getSharedMatrix() {
         Map<Date, Long> matrix = Maps.newTreeMap();
 
         for (ActivityWrapper aw : activityWrappers) {
             matrix.put(new Date(aw.getPublicationDate().getValue()), aw.getNbReshared());
         }
 
         return Joiner.on(",").join(matrix.values());
     }
 
     public Collection<String> getTitleMatrix() {
         Map<Date, String> matrix = Maps.newTreeMap();
         for (ActivityWrapper aw : activityWrappers) {
             matrix.put(new Date(aw.getPublicationDate().getValue()), aw.getTitle());
         }
 
 
         return matrix.values();
     }
 
     /**
      * compile each plus one
      *
      * @return
      */
     public Long getCompileSumPlusOnePage() {
         Function<Iterator<ActivityWrapper>, Long> f = recurssiveFunctionOnePlus();
         return f.apply(activityWrappers.iterator());
     }
 
     /**
      * Compile all time that one post has shared
      *
      * @return
      */
     public Long getCompileSumShared() {
         Function<Iterator<ActivityWrapper>, Long> f = recurssiveFunctionShared();
         return f.apply(activityWrappers.iterator());
     }
 
     private Function<Iterator<ActivityWrapper>, Long> recurssiveFunctionShared() {
         return new Function<Iterator<ActivityWrapper>, Long>() {
 
             public Long apply(@Nullable Iterator<ActivityWrapper> activityWrappers) {
 
                 if (!activityWrappers.hasNext()) {
                     return ZERO;
                 } else {
 
                     Long nbPlusOne = activityWrappers.next().getNbReshared();
 
                     final Long nb = (nbPlusOne != null ? nbPlusOne : ZERO);
                     final Long r = apply(activityWrappers);
 
                     return (r != null ? r : ZERO) + nb;
                 }
 
             }
         };
     }
 
 
     private Function<Iterator<ActivityWrapper>, Long> recurssiveFunctionOnePlus() {
         return new Function<Iterator<ActivityWrapper>, Long>() {
 
             public Long apply(@Nullable Iterator<ActivityWrapper> activityWrappers) {
 
                 if (!activityWrappers.hasNext()) {
                     return ZERO;
                 } else {
 
                     Long nbPlusOne = activityWrappers.next().getNbPlusOners();
                     final Long nb = (nbPlusOne != null ? nbPlusOne : ZERO);
                     final Long r = apply(activityWrappers);
 
                     return (r != null ? r : ZERO) + nb;
                 }
 
             }
         };
     }
 
 
     public List<ActivityWrapper> getActivityWrappers() {
 
 
         return activityWrappers;
     }
 
     public void setActivityWrappers(List<ActivityWrapper> activityWrappers) {
 
 
 
         this.activityWrappers = activityWrappers;
     }
 }
