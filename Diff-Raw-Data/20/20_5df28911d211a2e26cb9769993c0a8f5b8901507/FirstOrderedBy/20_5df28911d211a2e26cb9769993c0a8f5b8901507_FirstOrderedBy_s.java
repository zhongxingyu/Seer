 package in.chopl.hive.udaf;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.hadoop.hive.ql.exec.Description;
 import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
 import org.apache.hadoop.hive.ql.exec.UDFArgumentLengthException;
 import org.apache.hadoop.hive.ql.exec.UDFArgumentTypeException;
 import org.apache.hadoop.hive.ql.metadata.HiveException;
 import org.apache.hadoop.hive.ql.parse.SemanticException;
 import org.apache.hadoop.hive.ql.udf.UDFType;
 import org.apache.hadoop.hive.ql.udf.generic.AbstractGenericUDAFResolver;
 import org.apache.hadoop.hive.ql.udf.generic.GenericUDAFEvaluator;
 import org.apache.hadoop.hive.serde2.objectinspector.*;
 import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
 import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
 import org.apache.hadoop.io.Text;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 @Description(name = "first", value = "_FUNC_(val_col, cmp_col1, cmp_col_order1, cmp_col2, cmp_cols_order1, ...)" +
                                       "- Returns the first value specified with 'val_col' ordered by columns specified with 'cmp_cols'")
 public class FirstOrderedBy extends AbstractGenericUDAFResolver {
 
     static final Log LOG = LogFactory.getLog(FirstOrderedBy.class.getName());
     static final String VALUE_COLUMN = "val_col";
     static final String COMPARED_COLUMN = "cmp_col";
     static final String COMPARED_COLUMN_ORDER = "cmp_col_order";
 
     static final String ASC_ORDER = "ASC";
     static final String DESC_ORDER = "DESC";
 
     @Override
     public GenericUDAFEvaluator getEvaluator(TypeInfo[] parameters) throws SemanticException {
         int len = parameters.length;
 
         if (len < 3) {
             throw new UDFArgumentLengthException("At least one cmp_col is required");
         }
 
         if ((len % 2) != 1) {
             throw new UDFArgumentLengthException("A number of cmp_col and cmp_col_order are mismatched");
         }
 
         for (int i = 1; i < len; i += 2){
             ObjectInspector cmpColOI = TypeInfoUtils.getStandardJavaObjectInspectorFromTypeInfo(parameters[i]);
             ObjectInspector orderOI = TypeInfoUtils.getStandardJavaObjectInspectorFromTypeInfo(parameters[i+1]);
 
             if (!ObjectInspectorUtils.compareSupported(cmpColOI)) {
                 throw new UDFArgumentTypeException(i,
                         "A type of cmp_col must support comparison, but" +
                                 parameters[i].getTypeName() + " was passed as parameter " + i + ".");
             }
 
             if (orderOI.getCategory() != ObjectInspector.Category.PRIMITIVE ||
                 ((PrimitiveObjectInspector) orderOI).getPrimitiveCategory() != PrimitiveObjectInspector.PrimitiveCategory.STRING) {
                 throw new UDFArgumentTypeException(i + 1, "A type of cmp_col_order must be string");
             }
         }
 
         return new GenericUDAFFirstOrderedByEvaluator();
     }
 
     @UDFType(distinctLike=true)
     public static class GenericUDAFFirstOrderedByEvaluator extends GenericUDAFEvaluator {
         private ObjectInspector[] inputOIs;
         private StandardStructObjectInspector partialOI;
         private ObjectInspector outputOI;
 
         // converted inputOIs to StnadardObjectInspectors. used in AggregationBuffer.
         private ObjectInspector[] standardInputOIs;
 
         @Override
         public ObjectInspector init(Mode mode, ObjectInspector[] parameters) throws HiveException {
             super.init(mode, parameters);
 
            LOG.warn("mode: " + mode);
             switch (mode) {
                 case PARTIAL1:
                     inputOIs = parameters;
                     checkInputOIs(inputOIs);
                     standardInputOIs= makeStandardInputOI(inputOIs);
                     partialOI = makePartialOI(standardInputOIs);
                     return partialOI;
                 case PARTIAL2:
                     partialOI = (StandardStructObjectInspector) parameters[0];
                     inputOIs = makeInputOI(partialOI);
                     standardInputOIs= makeStandardInputOI(inputOIs);
                     return partialOI;
                 case FINAL:
                     partialOI = (StandardStructObjectInspector) parameters[0];
                     outputOI = partialOI.getStructFieldRef(VALUE_COLUMN).getFieldObjectInspector();
                     inputOIs = makeInputOI(partialOI);
                     standardInputOIs= makeStandardInputOI(inputOIs);
                     return outputOI;
                 case COMPLETE:
                     inputOIs = parameters;
                     outputOI = parameters[0];
                     checkInputOIs(inputOIs);
                     standardInputOIs= makeStandardInputOI(inputOIs);
                     return outputOI;
                 default:
                     throw new RuntimeException("Unknown aggregation mode: "+ mode);
             }
         }
 
         private StandardStructObjectInspector makePartialOI(ObjectInspector[] parameters) {
             int len = parameters.length;
             List<String> fieldNames = new ArrayList<String>(len);
 
             fieldNames.add(VALUE_COLUMN);
             for (int i = 1; i < len; i += 2) {
                 fieldNames.add(COMPARED_COLUMN + i);
                 fieldNames.add(COMPARED_COLUMN_ORDER + (i + 1));
             }
 
             return ObjectInspectorFactory.getStandardStructObjectInspector(fieldNames, Arrays.asList(parameters));
         }
 
         private ObjectInspector[] makeInputOI(StandardStructObjectInspector soi) {
             List<StructField> fields = (List<StructField>) soi.getAllStructFieldRefs();
             int len = fields.size();
             ObjectInspector[] ois = new ObjectInspector[len];
 
             for (int i = 0; i < len; i++) {
                 ois[i] = fields.get(i).getFieldObjectInspector();
             }
 
             return ois;
         }
 
         private ObjectInspector[] makeStandardInputOI(ObjectInspector[] ois) {
             int len = ois.length;
             ObjectInspector[] sois = new ObjectInspector[len];
             for (int i = 0; i < len; i++) {
                 sois[i] = ObjectInspectorUtils.getStandardObjectInspector(ois[i]);
             }
             return sois;
         }
 
         private void checkInputOIs(ObjectInspector[] ois) throws HiveException {
             int len = ois.length;
 
             // ois for cmp_col_orders
             for (int i = 2; i < len; i += 2) {
                 ObjectInspector oi = ois[i];
                 if (! (oi instanceof ConstantObjectInspector)) {
                     throw new UDFArgumentTypeException(i, "A cmp_col_order must be constant");
                 }
 
                 Text orderText = (Text) ((ConstantObjectInspector) oi).getWritableConstantValue();
                 String order = orderText.toString();
 
                 if (!order.equalsIgnoreCase(ASC_ORDER) && !order.equalsIgnoreCase(DESC_ORDER)) {
                     throw new UDFArgumentTypeException(i, order + " is invalid for cmp_col_order");
                 }
             }
         }
 
         static class FirstAgg implements AggregationBuffer {
             // whole parameters
             Object[] objects;
             // Objectinspectors for objects;
             ObjectInspector[] ois;
             // cmp_col_orders
             String[] orders;
 
             FirstAgg() {
                 init();
             }
 
             public void init() {
                 objects = null;
                 orders = null;
                 ois = null;
             }
 
             /**
              * Compare with parameters
              *
              * @param parameters
              *          UDAF parameters
              * @param paramOIs
              *          ObjectInspectors for parameters
              * @return
              *          When the buffered objects comes first by specified order, return > 0.
              *          When the buffered objects and parameter objects have same order, return 0.
              *          When parameter objects comes first by specified order, return < 0.
              */
             public int compareTo(Object[] parameters, ObjectInspector[] paramOIs) {
                 assert (objects.length == parameters.length);
                 assert (objects.length == paramOIs.length);
 
                 int len = objects.length;
 
                 // skip val_col
                 for (int i = 1; i < len; i += 2) {
                     Object obj = objects[i];
                     ObjectInspector oi = ois[i];
 
                     Object param = parameters[i];
                     ObjectInspector poi = paramOIs[i];
 
                     String order = orders[(i - 1) / 2];
 
                     int cmp = ObjectInspectorUtils.compare(obj, oi, param, poi);
                    LOG.warn("order: " + order + ", obj: " + obj + ", param: " + param + ", cmp :" + cmp);
                     if (cmp != 0) {
                         if (order.equals(ASC_ORDER)) {
                             return -cmp;
                         } else if (order.equals(DESC_ORDER)) {
                             return cmp;
                         } else {
                             throw new RuntimeException("Unexpected order string: " + order);
                         }
                     }
                 }
                 return 0;
             }
         }
 
         @Override
         public AggregationBuffer getNewAggregationBuffer() throws HiveException {
             FirstAgg agg = new FirstAgg();
             return agg;
         }
 
         @Override
         public void reset(AggregationBuffer agg) throws HiveException {
             ((FirstAgg) agg).init();
         }
 
         @Override
         public void iterate(AggregationBuffer agg, Object[] parameters) throws HiveException {
            LOG.warn("method: iterate");
             aggregate(agg, parameters, inputOIs);
         }
 
         @Override
         public Object terminatePartial(AggregationBuffer agg) throws HiveException {
            LOG.warn("method: terminatePartial");
             return ((FirstAgg) agg).objects;
         }
 
         @Override
         public void merge(AggregationBuffer agg, Object partial) throws HiveException {
            LOG.warn("method: merge");
             Object[] partialObjects = partialOI.getStructFieldsDataAsList(partial).toArray();
             aggregate(agg, partialObjects, inputOIs);
         }
 
         @Override
         public Object terminate(AggregationBuffer agg) throws HiveException {
            LOG.warn("method: terminate");
             return ((FirstAgg) agg).objects[0];
         }
 
         private void aggregate(AggregationBuffer agg, Object[] objects, ObjectInspector[] ois) throws UDFArgumentException {
             FirstAgg fagg = (FirstAgg) agg;
 
             if (fagg.orders == null) {
                 fagg.orders = selectOrders(objects, ois);
             }
 
             if (fagg.ois == null) {
                 fagg.ois = standardInputOIs;
             }
 
             if (fagg.objects == null){
                 fagg.objects =  copyObjects(objects, ois);
             } else {
                 int cmp = fagg.compareTo(objects, ois);
                LOG.warn("buf: " + Arrays.asList(fagg.objects) + ", param: " + Arrays.asList(objects) + ", cmp :" + cmp);
                 if (cmp < 0) {
                     fagg.objects =  copyObjects(objects, ois);
                 }
             }
         }
 
         private String[] selectOrders(Object[] objects, ObjectInspector[] ois) throws UDFArgumentException {
             String[] ret = new String[(ois.length - 1) / 2];
             int len = ret.length;
 
             for (int i = 0; i < len; i++) {
                 int index = (i + 1) * 2;
                 Object object = objects[index];
                 PrimitiveObjectInspector poi = (PrimitiveObjectInspector) ois[index];
                 String order = (String) poi.getPrimitiveJavaObject(object);
                 ret[i] = order.toUpperCase();
             }
             return ret;
         }
 
         private Object[] copyObjects(Object[] objects, ObjectInspector[] ois) {
             int len = objects.length;
             Object[] ret = new Object[len];
 
             for (int i = 0; i < len; i++) {
                 ret[i] = ObjectInspectorUtils.copyToStandardObject(objects[i], ois[i]);
             }
 
             return ret;
         }
     }
 }
