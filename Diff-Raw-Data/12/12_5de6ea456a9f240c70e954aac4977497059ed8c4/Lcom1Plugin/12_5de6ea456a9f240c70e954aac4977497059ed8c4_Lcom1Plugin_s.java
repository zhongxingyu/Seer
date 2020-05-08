 package jp.ac.osaka_u.ist.sel.metricstool.lcom1;
 
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.FieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetClassInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetFieldInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.data.target.TargetMethodInfo;
 import jp.ac.osaka_u.ist.sel.metricstool.main.plugin.AbstractClassMetricPlugin;
 
 
 /**
  * 
  * LCOM1(CKgNXLCOM)vvOCNX.
  * <p>
  * SẴIuWFNgwɑΉ.
  * NXC\bhCtB[hC\bh̏KvƂ.
  * @author kou-tngt
  *
  */
 public class Lcom1Plugin extends AbstractClassMetricPlugin {
     //  IuWFNgx̂邽߂ɃtB[hƂĐ
     /**
      * ΏۃNX̃\bhꗗ.
      * ̃IuWFNg͍ėp.
      */
     final List<TargetMethodInfo> methods = new ArrayList<TargetMethodInfo>(100);
 
     /** 
      * ΏۃNX̃CX^XtB[hꗗ.
      * ̃IuWFNg͍ėp.
      */
     final Set<TargetFieldInfo> instanceFields = new HashSet<TargetFieldInfo>();
 
     /**
      * gpꂽtB[hꗗ
      * ̃IuWFNg͍ėp.
      */
     final Set<FieldInfo> usedFields = new HashSet<FieldInfo>();
 
     /**
      * ėpĂIuWFNgɂ.
      */
     protected void clearReusedObjects() {
         methods.clear();
         instanceFields.clear();
         usedFields.clear();
     }
 
     /**
     * IuWFNgėp̂߂̏.
     */
    @Override
    protected void beforeMeasure() {
        clearReusedObjects();
    }

    /**
      * IuWFNgėp̌n.
      */
     @Override
     protected void afterMeasure() {
         clearReusedObjects();
     }
 
     /**
      * gNX̌v.
      * 
      * @param targetClass Ώۂ̃NX
      */
     @Override
     protected float measureClassMetric(TargetClassInfo targetClass) {
         int p = 0;
         int q = 0;
 
         methods.addAll(targetClass.getDefinedMethods());
 
         //̃NX̃CX^XtB[h̃Zbg擾
         instanceFields.addAll(targetClass.getDefinedFields());
         for (Iterator<TargetFieldInfo> it = instanceFields.iterator(); it.hasNext();) {
             if (it.next().isStaticMember()) {
                 it.remove();
             }
         }
 
         final int methodCount = methods.size();
 
         //tB[h𗘗p郁\bh1Ȃǂ
         boolean allMethodsDontUseAnyField = true;
 
         //S\bhi΂
         for (int i = 0; i < methodCount; i++) {
             //\bhi擾āCorQƂĂtB[hSsetɓ
             final TargetMethodInfo firstMethod = methods.get(i);
             usedFields.addAll(firstMethod.getAssignmentees());
             usedFields.addAll(firstMethod.getReferencees());
 
             //NX̃CX^XtB[hc
             usedFields.retainAll(instanceFields);
 
             if (allMethodsDontUseAnyField) {
                 //܂ǂ̃\bh1tB[h𗘗pĂȂꍇ
                 allMethodsDontUseAnyField = usedFields.isEmpty();
             }
 
             //iȍ~̃\bhjɂ
             for (int j = i + 1; j < methodCount; j++) {
                 //\bhj擾āCQƂĂtB[hPłsetɂ邩ǂ𒲂ׂ
                 final TargetMethodInfo secondMethod = methods.get(j);
                 boolean isSharing = false;
                 for (final FieldInfo secondUsedField : secondMethod.getReferencees()) {
                     if (usedFields.contains(secondUsedField)) {
                         isSharing = true;
                         break;
                     }
                 }
 
                 //ĂtB[hPłsetɂ邩ǂ𒲂ׂ
                 if (!isSharing) {
                     for (final FieldInfo secondUsedField : secondMethod.getAssignmentees()) {
                         if (usedFields.contains(secondUsedField)) {
                             isSharing = true;
                             break;
                         }
                     }
                 }
 
                 //LĂtB[hqCȂp𑝂₷
                 if (isSharing) {
                     q++;
                 } else {
                     p++;
                 }
             }
 
             usedFields.clear();
         }
 
         if (p <= q || allMethodsDontUseAnyField) {
             //pqȉC܂͑SẴ\bhtB[h𗘗pȂꍇlcom0
             return 0;
         } else {
             //łȂȂp-qlcom
             return p - q;
         }
     }
 
     /**
      * ̃vOC̊ȈՐPsŕԂ
      * @return ȈՐ
      */
     @Override
     protected String getDescription() {
         return "Measuring the LCOM1 metric(CK-metrics's LCOM).";
     }
 
     /**
      * ̃vOC̏ڍאԂ
      * @return@ڍא
      */
     @Override
     protected String getDetailDescription() {
         return DETAIL_DESCRIPTION;
     }
 
     /**
      * gNXԂD
      * 
      * @return gNX
      */
     @Override
     protected String getMetricName() {
         return "LCOM1";
     }
 
     /**
      * ̃vOCtB[hɊւ𗘗p邩ǂԂ\bhD
      * trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useFieldInfo() {
         return true;
     }
 
     /**
      * ̃vOC\bhɊւ𗘗p邩ǂԂ\bhD
      * trueԂD
      * 
      * @return trueD
      */
     @Override
     protected boolean useMethodInfo() {
         return true;
     }
 
     /**
      * ڍא萔
      */
     private final static String DETAIL_DESCRIPTION;
 
     static {
         final String lineSeparator = "\n";//System.getProperty("line.separator");//TODO@̕ӂ̃ZLeB͊ɘa
         final StringBuilder builder = new StringBuilder();
 
         builder.append("This plugin measures the LCOM1 metric(CK-metrics's LCOM).");
         builder.append(lineSeparator);
         builder
                 .append("The LCOM1 is one of the class cohesion metrics measured by following steps:");
         builder.append(lineSeparator);
         builder.append("1. P is a set of pairs of methods which do not share any field.");
         builder.append("If all methods do not use any field, P is a null set.");
         builder.append(lineSeparator);
         builder.append("2. Q is a set of pairs of methods which share some fields.");
         builder.append(lineSeparator);
         builder.append("3. If |P| > |Q|, the result is measured as |P| - |Q|, otherwise 0.");
         builder.append(lineSeparator);
 
         DETAIL_DESCRIPTION = builder.toString();
     }
 }
