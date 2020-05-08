 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.awe.views.calls.enums;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.amanzi.awe.views.calls.statistics.constants.IStatisticsConstants;
 import org.amanzi.awe.views.calls.statistics.constants.SecondLevelConstants;
 import org.neo4j.graphdb.Node;
 
 /**
  * <p>
  * Headers for second level statistics.
  * </p>
  * @author Shcharbatsevich_A
  * @since 1.0.0
  */
 public enum AggregationStatisticsHeaders implements IAggrStatisticsHeaders {
 
     SC1("SC1", StatisticsType.PERCENT,UtilAggregationHeaders.SC_SUCC_SETUP_COUNT,UtilAggregationHeaders.SC_ATTEMPT_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_SC1){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC2_ZW2_AVG("SC2 ZW2(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.SC_SETUP_TIME_TOTAL,UtilAggregationHeaders.SC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()>SecondLevelConstants.MAX_SC2_AVERAGE){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC2_ZW2_MIN("SC2 ZW2(MIN)", StatisticsType.MIN,UtilAggregationHeaders.SC_SETUP_TIME_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     SC2_ZW2_MAX("SC2 ZW2(MAX)", StatisticsType.MAX,UtilAggregationHeaders.SC_SETUP_TIME_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()>SecondLevelConstants.MAX_SC2_MAX){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC3("SC3", StatisticsType.PERCENT,UtilAggregationHeaders.SC_CALL_DISC_TIME,UtilAggregationHeaders.SC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
            if(value!=null&&value.floatValue()>SecondLevelConstants.MIN_SC3){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC4("SC4", StatisticsType.PERCENT,UtilAggregationHeaders.SC_AUDIO_QUAL_SUCC,UtilAggregationHeaders.SC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
            if(value!=null&&value.floatValue()>SecondLevelConstants.MIN_SC4){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC4_ZW2_AVG("SC4 ZW2(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.SC_AUDIO_QUAL_TOTAL,UtilAggregationHeaders.SC_AUDIO_QUAL_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
            if(value!=null&&value.floatValue()>SecondLevelConstants.MIN_SC4_AVERAGE){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC4_ZW2_MIN("SC4 ZW2(MIN)", StatisticsType.MIN,UtilAggregationHeaders.SC_AUDIO_QUAL_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     SC4_ZW2_MAX("SC4 ZW2(MAX)", StatisticsType.MAX,UtilAggregationHeaders.SC_AUDIO_QUAL_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     SC5_ZW1_AVG("SC5 ZW1(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.SC_DELAY_TOTAL,UtilAggregationHeaders.SC_DELAY_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()>SecondLevelConstants.MAX_SC5_AVERAGE){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SC5_ZW1_MIN("SC5 ZW1(MIN)", StatisticsType.MIN,UtilAggregationHeaders.SC_DELAY_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     SC5_ZW1_MAX("SC5 ZW1(MAX)", StatisticsType.MAX,UtilAggregationHeaders.SC_DELAY_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC1("GC1", StatisticsType.PERCENT,UtilAggregationHeaders.GC_SUCC_SETUP_COUNT,UtilAggregationHeaders.GC_ATTEMPT_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_GC1){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC2_ZW2_AVG("GC2 ZW2(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.GC_SETUP_TIME_TOTAL,UtilAggregationHeaders.GC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC2_ZW2_MIN("GC2 ZW2(MIN)", StatisticsType.MIN,UtilAggregationHeaders.GC_SETUP_TIME_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC2_ZW2_MAX("GC2 ZW2(MAX)", StatisticsType.MAX,UtilAggregationHeaders.GC_SETUP_TIME_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()>SecondLevelConstants.MAX_GC2_MAX){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC3("GC3", StatisticsType.PERCENT,UtilAggregationHeaders.GC_CALL_DISC_TIME,UtilAggregationHeaders.GC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_GC3){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC4("GC4", StatisticsType.PERCENT,UtilAggregationHeaders.GC_AUDIO_QUAL_SUCC,UtilAggregationHeaders.GC_SUCC_SETUP_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_GC4){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC4_ZW2_AVG("GC4 ZW2(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.GC_AUDIO_QUAL_TOTAL,UtilAggregationHeaders.GC_AUDIO_QUAL_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_GC4_AVERAGE){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC4_ZW2_MIN("GC4 ZW2(MIN)", StatisticsType.MIN,UtilAggregationHeaders.GC_AUDIO_QUAL_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC4_ZW2_MAX("GC4 ZW2(MAX)", StatisticsType.MAX,UtilAggregationHeaders.GC_AUDIO_QUAL_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC5_ZW1_AVG("GC5 ZW1(AVG)", StatisticsType.AVERAGE,UtilAggregationHeaders.GC_DELAY_TOTAL,UtilAggregationHeaders.GC_DELAY_COUNT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()>SecondLevelConstants.MAX_GC5_AVERAGE){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     GC5_ZW1_MIN("GC5 ZW1(MIN)", StatisticsType.MIN,UtilAggregationHeaders.GC_DELAY_MIN) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     GC5_ZW1_MAX("GC5 ZW1(MAX)", StatisticsType.MAX,UtilAggregationHeaders.GC_DELAY_MAX) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;//TODO add condition
         }
     },
     INH_CC("INH CC", StatisticsType.PERCENT,UtilAggregationHeaders.INH_CC_SUCCESS,UtilAggregationHeaders.INH_CC_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_INH_CC){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     TSM("TSM", StatisticsType.PERCENT,UtilAggregationHeaders.TSM_SUCCESS,UtilAggregationHeaders.TSM_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_TSM){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     SDS("SDS", StatisticsType.PERCENT,UtilAggregationHeaders.SDS_SUCCESS,UtilAggregationHeaders.SDS_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_SDS){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     INH_AT("INH AT", StatisticsType.PERCENT,UtilAggregationHeaders.INH_ATT_SUCCESS,UtilAggregationHeaders.INH_ATT_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             if(value!=null&&value.floatValue()<SecondLevelConstants.MIN_INH_ATT){
                 return StatisticsFlags.RED;
             }
             return StatisticsFlags.NONE;
         }
     },
     EC1("EC1", StatisticsType.PERCENT,UtilAggregationHeaders.EC1_SUCCESS,UtilAggregationHeaders.EC1_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;
         }
     },
     EC2("EC2", StatisticsType.PERCENT,UtilAggregationHeaders.EC2_SUCCESS,UtilAggregationHeaders.EC2_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;
         }
     },
     IP("IP", StatisticsType.PERCENT,UtilAggregationHeaders.IP_SUCCESS,UtilAggregationHeaders.IP_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;
         }
     },
     CSD("CSD", StatisticsType.PERCENT,UtilAggregationHeaders.CSD_SUCCESS,UtilAggregationHeaders.CSD_ATTEMPT) {
         @Override
         public StatisticsFlags getFlagByValue(Number value) {
             return StatisticsFlags.NONE;
         }
     };
     
     private String headerTitle;
     private StatisticsType headerType;
     private List<IStatisticsHeader> dependent;
     
     private AggregationStatisticsHeaders(String title, StatisticsType type, IStatisticsHeader... dependendHeaders) {
         headerTitle = title;
         headerType = type;
         dependent = Arrays.asList(dependendHeaders);
     }
 
     @Override
     public List<IStatisticsHeader> getDependendHeaders() {
         return dependent;
     }
 
     @Override
     public String getTitle() {
         return headerTitle;
     }
 
     @Override
     public StatisticsType getType() {
         return headerType;
     }
 
     @Override
     public Number getStatisticsData(Node dataNode, IStatisticsConstants constants, boolean inclInconclusive) {
         return null;
     }
     
     /**
      * Check value for flag.
      *
      * @param value Number.
      * @return StatisticsFlags
      */
     public abstract StatisticsFlags getFlagByValue(Number value);
 }
