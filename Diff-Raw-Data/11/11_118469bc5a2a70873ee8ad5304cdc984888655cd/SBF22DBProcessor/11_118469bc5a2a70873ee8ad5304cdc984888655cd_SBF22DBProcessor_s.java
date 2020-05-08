 package tw.com.citi.cdic.batch.item;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.batch.item.ItemProcessor;
 import org.springframework.beans.BeanUtils;
 
 import tw.com.citi.cdic.batch.model.A74;
 import tw.com.citi.cdic.batch.model.CDICF22R;
 
 /**
  * @author Lancelot
  * @since 2010/10/11
  */
 public class SBF22DBProcessor implements ItemProcessor<CDICF22R, List<A74>> {
 
     protected static final Logger logger = LoggerFactory.getLogger(SBF22DBProcessor.class);
 
     private Set<String> pKeySet = new HashSet<String>();
 
     private A74 generateBaseA74(CDICF22R item) {
         A74 a74 = new A74();
         a74.setUnit("021");
         a74.setBranchNo("0000");
         a74.setCurrencyCode(item.getCcyCode());
         a74.setRateType(item.getProdCode() + item.getCustType());
         a74.setType("N".equals(item.getRateType()) ? "1" : "2");
         String period = " ";
         switch (item.getTenorUnit().intValue()) {
         case 1:
             period = "D";
             break;
         case 2:
             period = "W";
             break;
         case 3:
             period = "M";
             break;
         case 4:
             period = "Y";
             break;
         }
         DecimalFormat df = new DecimalFormat("000");
         String len = "000";
         try {
             len = df.format(item.getTenorLen());
         } catch (Exception e) {
             // 如果發生 Exception，len 預設為 "000"
         }
         a74.setPeriod(period + len.substring(1, 3));
         a74.setEffectiveDate(item.getKeyDate());
         return a74;
     }
 
     private List<A74> generateOtherData(CDICF22R item, A74 a74) {
         List<A74> list = new ArrayList<A74>();
         if (a74.getLargeMax() > 9999999) {
             a74.setLargeMax(9999999);
         }
         if (checkA74(a74)) {
             if (item.getCustType() == null || "".equals(item.getCustType().trim())) {
                 for (int i = 0; i < 11; i++) {
                     A74 a74Other = new A74();
                     BeanUtils.copyProperties(a74, a74Other);
                     a74Other.setRateType(item.getProdCode() + Integer.toHexString(i + 1).toUpperCase());
                     list.add(a74Other);
                 }
             } else {
                 list.add(a74);
             }
         }
         return list;
     }
 
     private boolean checkA74(A74 a74) {
         boolean tf = true;
         if (pKeySet.contains(a74.getKey())) {
             tf = false;
             logger.error(a74.getKey() + ", unique violation");
         } else {
             pKeySet.add(a74.getKey());
         }
         return tf;
     }
 
     @Override
     public List<A74> process(CDICF22R item) {
         List<A74> a74List = new ArrayList<A74>();
         double largeMax = 0;
         if (item != null) {
             if ("D".equals(item.getRecType())) {
                 if ("CHPS".equals(item.getProdCode()) || "OHPS".equals(item.getProdCode())) {
                     A74 a74 = generateBaseA74(item);
                     a74.setRate(Double.parseDouble(item.getTierRate1())
                             + Double.parseDouble(item.getTierRateFraction1()) / 1000000);
                     largeMax = Double.parseDouble(item.getTierMinAmt1())
                             + Double.parseDouble(item.getTierMinAmtFraction1()) / 100;
                     // 若為台幣則以百萬為單位，XAU 百元為單位，其他取到整數位。
                     if ("TWD".equalsIgnoreCase(a74.getCurrencyCode())) {
                         a74.setLargeMax((int) (largeMax / 1000000));
                     } else if ("XAU".equalsIgnoreCase(a74.getCurrencyCode())) {
                        a74.setLargeMax((int) (largeMax));
                    } else {
                         a74.setLargeMax((int) (largeMax / 100));
                     }
                     a74List.addAll(generateOtherData(item, a74));
                 } else {
                     for (int i = 0; i < item.getNoOfTier(); i++) {
                         A74 a74 = generateBaseA74(item);
                         switch (i) {
                         case 0:
                             a74.setRate(Double.parseDouble(item.getTierRate0())
                                     + Double.parseDouble(item.getTierRateFraction0()) / 1000000);
                             largeMax = Double.parseDouble(item.getMinAmtDep())
                                     + Double.parseDouble(item.getMinAmtDepFraction()) / 100;
                             break;
                         case 1:
                             a74.setRate(Double.parseDouble(item.getTierRate1())
                                     + Double.parseDouble(item.getTierRateFraction1()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt1())
                                     + Double.parseDouble(item.getTierMinAmtFraction1()) / 100;
                             break;
                         case 2:
                             a74.setRate(Double.parseDouble(item.getTierRate2())
                                     + Double.parseDouble(item.getTierRateFraction2()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt2())
                                     + Double.parseDouble(item.getTierMinAmtFraction2()) / 100;
                             break;
                         case 3:
                             a74.setRate(Double.parseDouble(item.getTierRate3())
                                     + Double.parseDouble(item.getTierRateFraction3()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt3())
                                     + Double.parseDouble(item.getTierMinAmtFraction3()) / 100;
                             break;
                         case 4:
                             a74.setRate(Double.parseDouble(item.getTierRate4())
                                     + Double.parseDouble(item.getTierRateFraction4()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt4())
                                     + Double.parseDouble(item.getTierMinAmtFraction4()) / 100;
                             break;
                         case 5:
                             a74.setRate(Double.parseDouble(item.getTierRate5())
                                     + Double.parseDouble(item.getTierRateFraction5()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt5())
                                     + Double.parseDouble(item.getTierMinAmtFraction5()) / 100;
                             break;
                         case 6:
                             a74.setRate(Double.parseDouble(item.getTierRate6())
                                     + Double.parseDouble(item.getTierRateFraction6()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt6())
                                     + Double.parseDouble(item.getTierMinAmtFraction6()) / 100;
                             break;
                         case 7:
                             a74.setRate(Double.parseDouble(item.getTierRate7())
                                     + Double.parseDouble(item.getTierRateFraction7()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt7())
                                     + Double.parseDouble(item.getTierMinAmtFraction7()) / 100;
                             break;
                         case 8:
                             a74.setRate(Double.parseDouble(item.getTierRate8())
                                     + Double.parseDouble(item.getTierRateFraction8()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt8())
                                     + Double.parseDouble(item.getTierMinAmtFraction8()) / 100;
                             break;
                         case 9:
                             a74.setRate(Double.parseDouble(item.getTierRate9())
                                     + Double.parseDouble(item.getTierRateFraction9()) / 1000000);
                             largeMax = Double.parseDouble(item.getTierMinAmt9())
                                     + Double.parseDouble(item.getTierMinAmtFraction9()) / 100;
                             break;
                         }
                         // 若為台幣則以百萬為單位，XAU 百元為單位，其他取到整數位。
                         if ("TWD".equalsIgnoreCase(a74.getCurrencyCode())) {
                             a74.setLargeMax((int) (largeMax / 1000000));
                         } else if ("XAU".equalsIgnoreCase(a74.getCurrencyCode())) {
                            a74.setLargeMax((int) (largeMax));
                        } else {
                             a74.setLargeMax((int) (largeMax / 100));
                         }
                         a74List.addAll(generateOtherData(item, a74));
                     }
                 }
             }
         }
         return a74List.size() == 0 ? null : a74List;
     }
 }
