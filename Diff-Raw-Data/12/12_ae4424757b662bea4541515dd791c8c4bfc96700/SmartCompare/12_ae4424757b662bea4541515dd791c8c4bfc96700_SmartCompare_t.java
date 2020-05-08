 import java.io.IOException;
 import java.lang.reflect.Modifier;
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Nick Ebbutt
  * Date: 14-Jul-2009
  * Time: 13:38:06
  *
  * SmartCompare is a utility which compares two Objects to get (or print) a list of differences.
  *
  * A FieldIntrospector is used to find a list of Fields for the comparison.
  * Usually, a value comparison is performed for each Field identified (using reference equality, a configurable FieldComparator,
  * Comparable.compareTo() or Object.equals() to determine whether values differ).
  * You can also specify that a Field should be ignored, or its values should be introspected further, generating another
  * list of Fields to compare for the two values identified.
  *
  * The meaning of 'Field' is this context is flexible - a 'Field' does not necessarily imply a field on a class
  * at language level, although the default FieldIntrospector does use reflection to find the fields defined by the
  * classes of the objects being compared. SmartCompare has it's own FieldIntrospector and Field abstractions which
  * allow a list of Fields to be determined in other ways: For example, the MapIntrospector can be used where the objects
  * being compared are Maps - in this case the fields are identified by the keys in the Map.
  *
  * You can configure comparison behaviour based on the path through the reference graph, where each token is
  * a field name. This is done by binding ConfigRule to paths, by specifying a path matching regular expression.
  * For example, if a class 'Category', representing a hieararchy of categories, defines Fields for
  * 'categoryName' and 'parent', you could bind a rule to apply to the name of the grandparent Category using the path
  * 'parent.parent.categoryName'
  *
  * You can bind a rule directly using new SmartCompare().bindRule(myRule, myPathExpression).
  * There are shorthand ways to bind rule using the methods smartCompare.ignorePaths(), introspectPaths(),
  * bindIntrospector(), comparePaths() and bindComparator()
  *
  * If you wanted to compare two categories up the Category tree recursively, considering the Fields of each parent, you
  * would need to tell SmartCompare to introspect the parent Field:
  * new SmartCompare().introspectPaths(".*parent");   //introspect any path ending in parent
  * s.printDifferences(cat1, cat2)
  *
  * If you also wanted to set a FieldComparator for the grandparent 'name' field which ignored case, you could create a class
  * IgnoreCaseComparator, and bind the following rules:
  * new SmartCompare().introspectPaths(".*parent").bindComparator(new IgnoreCaseComparator(), "parent.parent.name");
  * s.getDifferences(cat1, cat2);
  *
  * You can also ignore Fields based on path. Given two objects of the following class Category, the following comparison
  * would find differences in category 'name' up the category hierarchy, but would ignore differences in 'priority'
  * class Category {
  *   String name;
  *   int priority;
  *   Category parent;
  * }
  * SmartCompare s = new SmartCompare().introspectPaths(".*parent").ignorePaths(".*priority");
  * s.printDifferences(cat1, cat2);
  */
 public class SmartCompare {
 
     public static final String INPUT_OBJECT_TEXT = "";
     private List<String> path;
     private String pathAsString;
     private volatile String description1;
     private volatile String description2;
     private List<Object> visitedNodes1;
     private List<Object> visitedNodes2;
     private Config config;
 
     public SmartCompare() {
         this(new Config());
     }
 
     public SmartCompare(FieldIntrospector fieldIntrospector) {
         this(new Config(fieldIntrospector));
     }
 
     public SmartCompare(Config config) {
         this("object1", "object2", config);
     }
 
     public SmartCompare(String description1, String description2) {
         this(description1, description2, new Config(), Collections.EMPTY_LIST, new ArrayList<Object>(), new ArrayList<Object>());
     }
 
     public SmartCompare(String description1, String description2, Config config) {
         this(description1, description2, config, Collections.EMPTY_LIST, new ArrayList<Object>(), new ArrayList<Object>());
     }
 
     private SmartCompare(String description1, String description2, Config config, List<String> path, List<Object> visitedNodes1, List<Object> visitedNodes2) {
         this.config = config;
         this.path = path;
         this.description1 = description1;
         this.description2 = description2;
         this.visitedNodes1 = visitedNodes1;
         this.visitedNodes2 = visitedNodes2;
         this.pathAsString = ClassUtils.getPathAsString(path);
     }
 
     public void setDescription1(String description1) {
         this.description1 = description1;
     }
 
     public void setDescription2(String description2) {
         this.description2 = description2;
     }
 
     public synchronized SmartCompare ignorePaths(String... pathPatterns) {
         config.ignorePaths(pathPatterns);
         return this;
     }
 
     public synchronized SmartCompare introspectPaths(String... pathPatterns) {
         config.introspectPaths(pathPatterns);
         return this;
     }
 
     public synchronized SmartCompare comparePaths(String... pathPatterns) {
         config.comparePaths(pathPatterns);
         return this;
     }
 
     public synchronized SmartCompare bindIntrospector(FieldIntrospector f, String... pathPatterns) {
         config.bindIntrospector(f, pathPatterns);
         return this;
     }
 
     public synchronized SmartCompare bindComparator(FieldComparator c, String... pathPatterns) {
         config.bindComparator(c, pathPatterns);
         return this;
     }
 
     public synchronized SmartCompare bindRule(ConfigRule r, String... pathPatterns) {
         config.bindRule(r, pathPatterns);
         return this;
     }
 
     /**
      * Print a list of differences to standard out
      * @throws ComparisonException, if an error prevents the comparison
      */
     public void printDifferences(Object o1, Object o2) throws ComparisonException {
         printDifferences(o1, o2, System.out);
     }
 
     /**
      * Print a list of differences to the supplied Appendable.
      * @throws ComparisonException, if an error prevents the comparison
      */
     public void printDifferences(Object o1, Object o2, Appendable s) throws ComparisonException {
         List<Difference> differences = getDifferences(o1, o2);
         try {
             Iterator<Difference> i = differences.iterator();
             while( i.hasNext()) {
                 String difference = i.next().toString();
                 s.append(i.hasNext() ? difference + "\n" : difference);
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Compare the two objects to get a list of Differences.
      * @throws ComparisonException, if an error prevents the comparison
      */
     public synchronized List<Difference> getDifferences(Object o1, Object o2) throws ComparisonException {
         List<Difference> result = new ArrayList<Difference>();
         try {
             if ( o1 != null || o2 != null) {
                 boolean nullDifference = addNullDifference(result, INPUT_OBJECT_TEXT, o1, o2);
                 if ( ! nullDifference ) {
                     boolean cycleExists = addCycleDifference(result, o1, o2);
                     if ( ! cycleExists ) {
                         addClassDifference(result, o1, o2);
                         addFieldDifferences(result, o1, o2);
                     }
                 }
             }
         } catch (Throwable t) {
             throw new ComparisonException(t);
         }
         return result;
     }
 
     private boolean addCycleDifference(List<Difference> differences, Object o1, Object o2) {
         int cycle1PathIndex = getIndexByReferenceEquality(visitedNodes1, o1);
         int cycle2PathIndex = getIndexByReferenceEquality(visitedNodes2, o2);
         boolean cycleExists = false;
         if ( cycle1PathIndex != -1 || cycle2PathIndex != -1 ) {
             cycleExists = true;
             //if the cycle points to the same index in the path for both objects we don't treat this as a difference.
             //we just stop parsing so that we don't end up in an infinite loop
             if ( cycle1PathIndex != cycle2PathIndex ) {
                 differences.add(
                     new Difference(DifferenceType.CYCLE, path,
                         INPUT_OBJECT_TEXT,
                         getDifferenceDescription(
                             ClassUtils.getPathAsString(path.subList(0, cycle1PathIndex)),
                             ClassUtils.getPathAsString(path.subList(0, cycle2PathIndex))
                         ),
                         o1,
                         o2
                     )
                 );
             }
         }
         return cycleExists;
     }
 
     private int getIndexByReferenceEquality(List<Object> visitedNodes, Object o) {
         int result = -1;
         int index = 0;
         for ( Object node : visitedNodes ) {
             if ( node == o) {
                 result = index;
                 break;
             }
             index++;
         }
         return result;
     }
 
 
     private boolean addClassDifference(List<Difference> differences, Object o1, Object o2) {
         boolean result = false;
         //if these are the original input object rather than a bean path further down we use a special description
         String fieldName = path.size() == 0 ? INPUT_OBJECT_TEXT : "";
         if (! o1.getClass().equals(o2.getClass())) {
             result = differences.add(new Difference(DifferenceType.CLASS, path, fieldName, "different class type: object1: [" + o1.getClass().getName() + "] object2: [" + o2.getClass().getName() + "]", o1.getClass(), o2.getClass()));
         }
         return result;
     }
 
     private boolean addFieldDifferences(List<Difference> differences, Object o1, Object o2) throws Exception {
         List<Field> introspectionFields = new ArrayList<Field>();
         List<Field> comparisonFields = new ArrayList<Field>();
 
         Class clazz = ClassUtils.getCommonSuperclass(o1.getClass(), o2.getClass());
         addFields(clazz, o1, o2, introspectionFields, comparisonFields);
 
         boolean result = differences.addAll(getFieldDifferences(comparisonFields, new ComparisonFieldDiffCalculator(), o1, o2));
         result |= differences.addAll(getFieldDifferences(introspectionFields, new IntrospectionFieldDifferenceCalculator(o1, o2), o1, o2));
         return result;
     }
 
     private void addFields(Class commonSuperclass, Object o1, Object o2, List<Field> introspectionFields, List<Field> comparisonFields) {
         FieldIntrospector i = config.getFieldIntrospector(pathAsString, commonSuperclass, o1, o2);
         List<Field> fields = i.getFields(pathAsString, commonSuperclass, o1, o2);
         for ( Field f : fields) {
             switch ( config.getType(f)) {
                 case INTROSPECTION:
                     introspectionFields.add(f);
                     break;
                 case COMPARISON:
                     comparisonFields.add(f);
                     break;
             }
         }
     }
 
     private List<Difference> getFieldDifferences(List<Field> fields, FieldDiffCalculator fieldDiffCalculator, Object o1, Object o2) throws Exception {
         List<Difference> result = new ArrayList<Difference>();
 
         for ( Field f : fields) {
             Object fieldValue1 = f.getValue(o1);
             Object fieldValue2 = f.getValue(o2);
 
             if ( fieldValue1 != fieldValue2) {
                 //check to see if one of the fields is null
                 Difference d = getUndefinedFieldDifference(f.getName(), fieldValue1, fieldValue2);
                 if ( d != null ) {
                     result.add(d);
                 } else {
                     boolean nullDifference = addNullDifference(result, f.getName(), fieldValue1, fieldValue2);
                     if ( ! nullDifference ){
                         result.addAll(fieldDiffCalculator.getFieldDifferences(
                             f, fieldValue1, fieldValue2)
                         );
                     }
                 }
             }
         }
         return result;
     }
 
     //An introspection field, in which case we test for equality by reference, and if that
     //test fails we drill down to look for differences one further step down the object graph
     private class IntrospectionFieldDifferenceCalculator implements FieldDiffCalculator {
         private List<Object> newVisitedNodes1;
         private List<Object> newVisitedNodes2;
 
         public IntrospectionFieldDifferenceCalculator(Object o1, Object o2) {
             newVisitedNodes1 = getNewVisitedNodes(visitedNodes1, o1);
             newVisitedNodes2 = getNewVisitedNodes(visitedNodes2, o2);
         }
 
         public List<Difference> getFieldDifferences(Field f, Object fieldValue1, Object fieldValue2) {
             List<String> newPath = new ArrayList<String>(path);
             newPath.add(f.getName());
 
             List<Difference> result = new ArrayList<Difference>();
             SmartCompare l = new SmartCompare(
                     description1,
                     description2,
                     config,
                     newPath,
                     newVisitedNodes1,
                     newVisitedNodes2
             );
             result.addAll(l.getDifferences(fieldValue1, fieldValue2));
             return result;
         }
 
         private List<Object> getNewVisitedNodes(List<Object> visited, Object newNode) {
             ArrayList<Object> o = new ArrayList<Object>(visited);
             o.add(newNode);
             return o;
         }
     }
 
     //A comparison field, in which case we test for equality to determine differences
     //using a supplied Comparator, compareTo for Comparables, or equals()
     private class ComparisonFieldDiffCalculator implements FieldDiffCalculator {
 
         public List<Difference> getFieldDifferences(Field f, Object fieldValue1, Object fieldValue2) {
             Difference fieldDifference = getFieldDifference(f, fieldValue1, fieldValue2);
 
             List<Difference> result = new ArrayList<Difference>();
             if ( fieldDifference != null) {
                 result.add(fieldDifference);
             }
             return result;
         }
 
         private Difference getFieldDifference(Field f, Object fieldValue1, Object fieldValue2) {
             Difference fieldDifference;
             FieldComparator c = config.getComparator(f);
             if ( c != null ) {
                 boolean isEqual = c.isEqual(f, fieldValue1, fieldValue2);
                 fieldDifference = createComparisonDifference(f.getName(), fieldValue1, fieldValue2, isEqual);
             } else if ( fieldValue1 instanceof Comparable ) {
                 int comparison = ((Comparable)fieldValue1).compareTo(fieldValue2);
                 fieldDifference = createComparisonDifference(f.getName(), fieldValue1, fieldValue2, comparison == 0);
             } else {
                 fieldDifference = createComparisonDifference(f.getName(), fieldValue1, fieldValue2, fieldValue1.equals(fieldValue2));
             }
             return fieldDifference;
         }
     }
 
     private interface FieldDiffCalculator {
         List<Difference> getFieldDifferences(Field f, Object fieldValue1, Object fieldValue2);
     }
 
     private Difference createComparisonDifference(String fieldName, Object fieldValue1, Object fieldValue2, boolean comparisonSucceeded) {
         Difference d = null;
         if (! comparisonSucceeded) {
             d = new Difference(DifferenceType.VALUE, path, fieldName, getDifferenceDescription(fieldValue1, fieldValue2), fieldValue1, fieldValue2);
         }
         return d;
     }
 
     private boolean addNullDifference(List<Difference> differences, String fieldName, Object fieldValue1, Object fieldValue2) {
         boolean isNull1 = fieldValue1 == null;
         boolean isNull2 = fieldValue2 == null;
         boolean result = false;
         if ( isNull1 != isNull2 ) {
             result = differences.add(createComparisonDifference(fieldName, fieldValue1, fieldValue2, false));
         }
         return result;
     }
 
     //one of the two comparison objects does not define a field which the other defines
     //e.g. these are two different subclasses with differing fields at a subclass level
     private Difference getUndefinedFieldDifference(String fieldName, Object fieldValue1, Object fieldValue2) {
         Difference d = null;
         if (fieldValue1 == Field.UNDEFINED_FIELD_VALUE || fieldValue2 == Field.UNDEFINED_FIELD_VALUE) {
             d = new Difference(DifferenceType.FIELD, path, fieldName, getDifferenceDescription(fieldValue1, fieldValue2), fieldValue1, fieldValue2);
         }
         return d;
     }
 
     private String getDifferenceDescription(Object differenceValue1, Object differenceValue2) {
         return description1 + ":[" + differenceValue1 + "] " + description2 + ":[" + differenceValue2 + "]";
     }
 
     public boolean isSupportedForComparison(Field f) {
         return false;
     }
 
 
     /**
      * General util methods for parsing class information
      */
     private static class ClassUtils {
 
         //stacks local to the thread as an optimisation to prevent object cycling by creating many stacks
         private static ThreadLocal<Stack> classStack1ByThread = new StackThreadLocal();
         private static ThreadLocal<Stack> classStack2ByThread = new StackThreadLocal();
 
         static List getList(Object array) {
             List result;
             //this is horrible, is there a better way?
             //We could use reflection to create the List but that would be slower.
             if ( array instanceof Object[] ) {
                 result = Arrays.asList((Object[])array);
             } else {
                 result = new ArrayList();
                 if ( array instanceof int[] ) {
                     for ( int i : (int[]) array) {
                         result.add(i);
                     }
                 } else if ( array instanceof float[] ) {
                     for ( float f : (float[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof byte[] ) {
                     for ( byte f : (byte[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof char[] ) {
                     for ( char f : (char[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof double[] ) {
                     for ( double f : (double[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof short[] ) {
                     for ( short f : (short[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof long[] ) {
                     for ( long f : (long[]) array) {
                         result.add(f);
                     }
                 } else if ( array instanceof boolean[] ) {
                     for ( boolean f : (boolean[]) array) {
                         result.add(f);
                     }
                 } else {
                     throw new UnsupportedOperationException("Cannot convert array to List, argument was not an array instance");
                 }
             }
             return result;
         }
 
         static String getPathAsString(List<String> pathFromRoot) {
             Iterator<String> i = pathFromRoot.iterator();
             StringBuilder sb = new StringBuilder();
             while(i.hasNext()) {
                 sb.append(i.next());
                 if ( i.hasNext()) {
                     sb.append(".");
                 }
             }
             return sb.toString();
         }
 
         static Class getCommonSuperclass(Class c1, Class c2) {
             Class result;
             if ( c1 == c2 ) {
                 result = c1;
             } else if ( c1 == null) {
                 result = c2;
             } else if (c2 == null) {
                 result = c1;
             } else {
                 //the classes differ find the shared superclass
                 Stack classStack1 = classStack1ByThread.get();
                 Stack classStack2 = classStack2ByThread.get();
                 addToStack(classStack1, c1);
                 addToStack(classStack2, c2);
                 result = findFirstMatching(classStack1, classStack2);
                 classStack1.clear();
                 classStack2.clear();
             }
             return result == null ? Object.class : result;
         }
 
         //we have 2 stacks of classes with Object.class at the top and then more specific classes
         //this should find the first point in the stack where the classes differ, then return the previous
         //classtype (which should be the most specific common superclass)
         private static Class<?> findFirstMatching(Stack<Class> classStack1, Stack<Class> classStack2) {
             Class result = Object.class;
             while(classStack1.size() > 0 && classStack2.size() > 0) {
                 Class c1 = classStack1.pop();
                 Class c2 = classStack2.pop();
                 if ( c1 != c2 ) {
                     break;
                 }
                 result = c1;
             }
             return result;
         }
 
         //recursively add to stack until we reach Object
         private static void addToStack(Stack<Class> classStack, Class c) {
             classStack.add(c);
             Class superclass = c.getSuperclass();
             if ( superclass != null) {
                 addToStack(classStack, superclass);
             }
         }
 
         public static boolean isPrimativeOrStringArray(Class<?> type) {
             return type.isArray() &&
               (int[].class.equals(type) ||
                float[].class.equals(type) ||
                double[].class.equals(type) ||
                long[].class.equals(type) ||
                String[].class.equals(type) ||
                byte[].class.equals(type) ||
                char[].class.equals(type) ||
                short[].class.equals(type) ||
                boolean[].class.equals(type));
         }
 
         private static class StackThreadLocal extends ThreadLocal<Stack> {
             public Stack initialValue() {
                 return new Stack();
             }
         }
     }
 
     public static class Difference {
 
         private List<String> path;
         private String fieldName;
         private String description;
         private Object fieldValue1;
         private Object fieldValue2;
         private String fullFieldPath;
         private DifferenceType differenceType;
 
         public Difference(DifferenceType differenceType, String fieldName, String description, Object fieldValue1, Object fieldValue2) {
             this(differenceType, Collections.EMPTY_LIST, fieldName, description, fieldValue1, fieldValue2);
         }
 
         public Difference(DifferenceType differenceType, List<String> path, String fieldName, String description, Object fieldValue1, Object fieldValue2) {
             this.differenceType = differenceType;
             this.path = path;
             this.fieldName = fieldName;
             this.description = description;
             this.fieldValue1 = fieldValue1;
             this.fieldValue2 = fieldValue2;
             calculateFieldPath();
         }
 
         public Object getFieldValue1() {
             return fieldValue1;
         }
 
         public Object getFieldValue2() {
             return fieldValue2;
         }
 
         public String getFieldPath() {
             return fullFieldPath;
         }
 
         public DifferenceType getDifferenceType() {
             return differenceType;
         }
 
         private void calculateFieldPath() {
             StringBuilder b = new StringBuilder();
             appendPath(b);
             appendfieldName(b);
             this.fullFieldPath = b.toString();
         }
 
         private void appendfieldName(StringBuilder b) {
             if ( fieldName != null && fieldName.length() > 0) {
                 if ( path.size() > 0 ) {
                     b.append(".");
                 }
                 b.append(fieldName);
             }
         }
 
         private void appendPath(StringBuilder b) {
             Iterator<String> i = path.iterator();
             String p;
             while(i.hasNext()) {
                 b.append(i.next());
                 if ( i.hasNext()) {
                     b.append(".");
                 }
             }
         }
 
         public String toString() {
             StringBuilder b = new StringBuilder(fullFieldPath);
             b.append("->").append(description);
             return b.toString();
         }
 
         @Override
         public boolean equals(Object o) {
             if (this == o) return true;
             if (o == null || getClass() != o.getClass()) return false;
 
             Difference that = (Difference) o;
 
             if (description != null ? !description.equals(that.description) : that.description != null) return false;
             if (differenceType != that.differenceType) return false;
             if (fieldValue1 != null ? !fieldValue1.equals(that.fieldValue1) : that.fieldValue1 != null) return false;
             if (fieldValue2 != null ? !fieldValue2.equals(that.fieldValue2) : that.fieldValue2 != null) return false;
             if (fullFieldPath != null ? !fullFieldPath.equals(that.fullFieldPath) : that.fullFieldPath != null)
                 return false;
 
             return true;
         }
 
         @Override
         public int hashCode() {
             int result = path != null ? path.hashCode() : 0;
             result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
             result = 31 * result + (description != null ? description.hashCode() : 0);
             result = 31 * result + (fieldValue1 != null ? fieldValue1.hashCode() : 0);
             result = 31 * result + (fieldValue2 != null ? fieldValue2.hashCode() : 0);
             result = 31 * result + (fullFieldPath != null ? fullFieldPath.hashCode() : 0);
             result = 31 * result + (differenceType != null ? differenceType.hashCode() : 0);
             return result;
         }
     }
 
     public static enum DifferenceType {
         CYCLE,  //when introspecting field values there are cyclic references which differ between the two objects
         FIELD,  //a field exists for one object in the comparison but is not defined for the other
         CLASS,  //the class of the object returned for a given field differs
         VALUE   //the values for a field differ
     }
 
     public static class Config {
 
        private static final String[] ALL_PATHS_PATTERN = new String[] {".*"};
         private static DefaultConfigRule DEFAULT_RULE = new DefaultConfigRule();
         private static IgnoreRule IGNORE_RULE = new IgnoreRule();
         private static IntrospectRule INTROSPECT_RULE = new IntrospectRule();
         private static CompareRule COMPARE_RULE = new CompareRule();
 
         private LinkedHashMap<Pattern, ConfigRule> patternToRule = new LinkedHashMap<Pattern, ConfigRule>();
 
         //the path maps/caches are the results of applying the current patterns/rules, they are invalidated when patterns change
         private Map<String, FieldType> pathToFieldType = new HashMap<String, FieldType>();
         private Map<String, FieldComparator> pathToComparator = new HashMap<String, FieldComparator>();
         private Map<String, FieldIntrospector> pathToIntrospector = new HashMap<String, FieldIntrospector>();
         private List<Map> pathCaches = Arrays.asList(new Map[] {pathToFieldType, pathToComparator, pathToIntrospector});
 
         public Config() {}
 
         public Config(FieldIntrospector rootIntrospector) {
             bindIntrospector(rootIntrospector, "");
         }
 
         public FieldType getType(Field f) {
             FieldType type = pathToFieldType.get(f.getPath());
             if (type == null) {
                 type = getTypeFromRules(f);
                 pathToFieldType.put(f.getPath(), type);
             }
             return type;
         }
 
         private FieldType getTypeFromRules(Field f) {
             List<ConfigRule> rules = getRules(f.getPath());
             FieldType type = null;
             for ( ConfigRule r : rules) {
                 type = r.getType(type, f);
             }
             return type;
         }
 
         public FieldComparator getComparator(Field f) {
            FieldComparator comparator;
            //note that the map may contain a null comparator value for the key, which may be the correct result
            //we have to check if the key exists when deciding whether to apply the rules, not for null
            if (! pathToComparator.containsKey(f.getPath())) {
                 comparator = getComparatorFromRules(f);
                 pathToComparator.put(f.getPath(), comparator);
            } else {
                comparator = pathToComparator.get(f.getPath());
             }
             return comparator;
         }
 
         private FieldComparator getComparatorFromRules(Field f) {
             List<ConfigRule> rules = getRules(f.getPath());
             FieldComparator comparator = null;
             for ( ConfigRule r : rules) {
                 comparator = r.getComparator(comparator, f);
             }
             return comparator;
         }
 
         public FieldIntrospector getFieldIntrospector(String path, Class commonSuperclass, Object o1, Object o2) {
             FieldIntrospector i = pathToIntrospector.get(path);
             if ( i == null) {
                 i = getIntrospectorFromRules(path, commonSuperclass, o1, o2);
                 pathToIntrospector.put(path, i);
             }
             return i;
         }
 
         private FieldIntrospector getIntrospectorFromRules(String path, Class commonSuperclass, Object o1, Object o2) {
             List<ConfigRule> rules = getRules(path);
             FieldIntrospector introspector = null;
             for ( ConfigRule r : rules) {
                 introspector = r.getFieldIntrospector(introspector, path, commonSuperclass, o1, o2);
             }
             return introspector;
         }
 
         private List<ConfigRule> getRules(String path) {
             List<ConfigRule> rules = new ArrayList<ConfigRule>();
             rules.add(DEFAULT_RULE);
             for (Pattern p : patternToRule.keySet()) {
                 if ( p.matcher(path).matches() ) {
                     ConfigRule r = patternToRule.get(p);
                     rules.add(r);
                 }
             }
             return rules;
         }
 
         public Config ignorePaths(String... pathPattern) {
             bindRule(IGNORE_RULE, pathPattern);
             return this;
         }
 
         public Config introspectPaths(String... pathPattern) {
             bindRule(INTROSPECT_RULE, pathPattern);
             return this;
         }
 
         public Config comparePaths(String... pathPattern) {
             bindRule(COMPARE_RULE, pathPattern);
             return this;
         }
 
         public Config bindIntrospector(FieldIntrospector f, String... pathPattern) {
             pathPattern = getDefaultPatternIfNoSpecified(pathPattern);
             bindRule(new IntrospectorRule(f), pathPattern);
             return this;
         }
 
         public Config bindComparator(FieldComparator c, String... pathPattern) {
             pathPattern = getDefaultPatternIfNoSpecified(pathPattern);
             bindRule(new ComparatorRule(c), pathPattern);
             return this;
         }
 
         public void bindRule(ConfigRule rule, String... pathPattern) {
             pathPattern = getDefaultPatternIfNoSpecified(pathPattern);
             for ( String pattern : pathPattern) {
                 Pattern p = Pattern.compile(pattern);
                 patternToRule.put(p, rule);
             }
             clearPathCaches();
         }
 
         private String[] getDefaultPatternIfNoSpecified(String... pathPattern) {
             if ( pathPattern.length == 0) {
                 pathPattern = ALL_PATHS_PATTERN;
             }
             return pathPattern;
         }
 
         private void clearPathCaches() {
             for (Map m : pathCaches) {
                 m.clear();
             }
         }
 
         private static class ComparatorRule extends ConfigRuleBase {
             private FieldComparator comparator;
 
             public ComparatorRule(FieldComparator comparator) {
                 this.comparator = comparator;
             }
 
             public FieldType getType(FieldType defaultType, Field f) {
                 return FieldType.COMPARISON;
             }
 
             public FieldComparator getComparator(FieldComparator defaultComparator, Field f) {
                 return comparator;
             }
         }
 
         private static class IntrospectorRule extends ConfigRuleBase {
             private FieldIntrospector introspector;
 
             public IntrospectorRule(FieldIntrospector introspector) {
                 this.introspector = introspector;
             }
 
             public FieldType getType(FieldType defaultType, Field f) {
                 return FieldType.INTROSPECTION;
             }
 
             public FieldIntrospector getFieldIntrospector(FieldIntrospector defaultIntrospector, String path, Class classType, Object o1, Object o2) {
                 return introspector;
             }
 
             public String toString() {
                 return "Introspector Rule";
             }
         }
 
         private static class IgnoreRule extends ConfigRuleBase {
             public FieldType getType(FieldType defaultType, Field f) {
                 return FieldType.IGNORE;
             }
 
             public String toString() {
                 return "Ignore Rule";
             }
         }
 
         private static class IntrospectRule extends ConfigRuleBase {
             public FieldType getType(FieldType defaultType, Field f) {
                 return FieldType.INTROSPECTION;
             }
 
             public String toString() {
                 return "Introspect Rule";
             }
         }
 
         private static class CompareRule extends ConfigRuleBase {
             public FieldType getType(FieldType defaultType, Field f) {
                 return FieldType.COMPARISON;
             }
 
             public String toString() {
                 return "Compare Rule";
             }
         }
     }
 
     /**
      * A base class for ConfigRules, which simply returns the values from the previous rule in the rule chain
      */
     public static class ConfigRuleBase implements ConfigRule {
 
         public FieldType getType(FieldType defaultType, Field f) {
             return defaultType;
         }
 
         public FieldComparator getComparator(FieldComparator defaultComparator, Field f) {
             return defaultComparator;
         }
 
         public FieldIntrospector getFieldIntrospector(FieldIntrospector defaultIntrospector, String path, Class classType, Object o1, Object o2) {
             return defaultIntrospector;
         }
     }
 
     public static class DefaultConfigRule implements ConfigRule {
 
         private static final ArrayAsListComparator arrayAsListComparator = new ArrayAsListComparator();
         private MapIntrospector mapIntrospector = new MapIntrospector();
         private IterableIntrospector  iterableIntrospector = new IterableIntrospector();
         private ArrayIntrospector arrayIntrospector = new ArrayIntrospector();
         private SubclassFieldIntrospector subclassFieldIntrospector = new SubclassFieldIntrospector();
         private UnsortedSetIntrospector unsortedSetIntrospector = new UnsortedSetIntrospector();
 
         public FieldType getType(FieldType defaultType, Field f) {
             return isIntrospectByDefault(f) ? FieldType.INTROSPECTION : FieldType.COMPARISON;
         }
 
         private boolean isIntrospectByDefault(Field f) {
             return ClassUtils.isPrimativeOrStringArray(f.getClassType()) ||
                Map.class.isAssignableFrom(f.getClassType()) ||
                Iterable.class.isAssignableFrom(f.getClassType());
         }
 
         public FieldComparator getComparator(FieldComparator defaultComparator, Field f) {
             FieldComparator result = null;
             if ( f.getClassType().isArray()) {
                 return arrayAsListComparator;
             }
             return result;
         }
 
         public FieldIntrospector getFieldIntrospector(FieldIntrospector defaultIntrospector, String path, Class classType, Object o1, Object o2) {
             FieldIntrospector result;
             if ( Map.class.isAssignableFrom(classType) ) {
                 result = mapIntrospector;
             } else if (Set.class.isAssignableFrom(classType)) {
                 result = SortedSet.class.isAssignableFrom(classType) ? iterableIntrospector : unsortedSetIntrospector;
             } else if (Iterable.class.isAssignableFrom(classType)){
                 result = iterableIntrospector;
             } else if (classType.isArray()) {
                 result = arrayIntrospector;
             } else {
                 //an introspector which includes differences for fields which are only present in one of the two input objects
                 result = subclassFieldIntrospector;
             }
             return result;
         }
 
         public String toString() {
             return "Default Rule";
         }
     }
 
     private static class ArrayAsListComparator implements FieldComparator {
         public boolean isEqual(Field f, Object o1, Object o2) {
             boolean result = true;
             if ( o1 != o2) {
                 List l1 = ClassUtils.getList(o1);
                 List l2 = ClassUtils.getList(o2);
                 result = l1.equals(l2);
             }
             return result;
         }
     }
 
     /**
      * Includes differences for subclass fields which are not shared
      */
     public static class SubclassFieldIntrospector extends ReflectionFieldIntrospector {
         private Class commonSuperclass;
         private Class o1;
         private Class o2;
 
         protected void prepareIntrospector(String pathFromRoot, Class commonSuperclass, Object o1, Object o2) {
             this.commonSuperclass = commonSuperclass;
             this.o1 = o1.getClass();
             this.o2 = o2.getClass();
         }
 
         protected void clearIntrospector() {
             this.commonSuperclass = null;
             this.o1 = this.o2 = null;
         }
 
         protected List<Field> doGetFields() {
             List<Field> result = new ArrayList<Field>();
             //add fields from shared superclass
             addFieldsRecursivelyUntilReachingClass(result, commonSuperclass, Object.class);
             addFieldsRecursivelyUntilReachingClass(result, o1, commonSuperclass); //fields only in o1 hierarchy
             addFieldsRecursivelyUntilReachingClass(result, o2, commonSuperclass); //fields only in o2 hierarchy
             return result;
         }
     }
 
     /**
      * This class introspects fields from the common superclass upwards
      * Fields at a subclass level (which are not shared) are not included
      */
     public static class SuperclassFieldIntrospector extends ReflectionFieldIntrospector {
         private Class commonSuperclass;
 
         protected void prepareIntrospector(String path, Class commonSuperclass, Object o1, Object o2) {
             this.commonSuperclass = commonSuperclass;
         }
 
         protected void clearIntrospector() {
             this.commonSuperclass = null;
         }
 
         protected List<Field> doGetFields() {
             List<Field> result = new ArrayList<Field>();
             //add fields from shared superclass
             addFieldsRecursivelyUntilReachingClass(result, commonSuperclass, Object.class);
             return result;
         }
     }
 
     /*
     * This introspector uses reflection to find fields for two objects which are of identical class
     * If the class type differs no fields will be returned
     */
     public static class IdenticalClassFieldIntrospector extends ReflectionFieldIntrospector {
 
         private Class commonSuperclass;
         private Class o1;
         private Class o2;
 
         protected void prepareIntrospector(String path, Class commonSuperclass, Object o1, Object o2) {
             this.commonSuperclass = commonSuperclass;
             this.o1 = o1.getClass();
             this.o2 = o2.getClass();
         }
 
         protected void clearIntrospector() {
             commonSuperclass = null;
             o1 = null;
             o2 = null;
         }
 
         protected List<Field> doGetFields() {
             List<Field> result = new ArrayList<Field>();
             //only introspect fields if the two objects are exactly the same class type, no subclasses
             if ( o1 == o2 && o1 == commonSuperclass ) {
                 addFieldsRecursivelyUntilReachingClass(result, commonSuperclass, Object.class);
             }
             return result;
         }
     }
 
      public static abstract class ReflectionFieldIntrospector extends AbstractFieldIntrospector {
 
          //find all the fields for this class and superclasses
          protected void addFieldsRecursivelyUntilReachingClass(List<Field> result, Class c, Class endClass) {
             if ( c != endClass ) {
                 java.lang.reflect.Field[] fields = c.getDeclaredFields();
                 for ( final java.lang.reflect.Field f : fields) {
                     if ( ! isIgnoreField(f) ) {
                         result.add(new Field() {
                             public Class<?> getClassType() {
                                 return f.getType();
                             }
 
                             public Object getValue(Object o1) throws IllegalAccessException {
                                 if ( ! f.isAccessible()) {
                                     f.setAccessible(true);
                                 }
                                 return f.getDeclaringClass().isAssignableFrom(o1.getClass()) ? f.get(o1) : UNDEFINED_FIELD_VALUE;
                             }
 
                             public String getName() {
                                 return f.getName();
                             }
 
                             public String getPath() {
                                 return ReflectionFieldIntrospector.this.getPath(f.getName());
                             }
                         });
                     }
                 }
 
                 Class superclass = c.getSuperclass();
                 if ( superclass != null) {
                     addFieldsRecursivelyUntilReachingClass(result, superclass, endClass);
                 }
             }
         }
 
         protected boolean isIgnoreField(java.lang.reflect.Field f) {
             return Modifier.isStatic(f.getModifiers());
         }
     }
 
     public abstract static class AbstractFieldIntrospector implements FieldIntrospector {
 
         private String introspectorPath;
 
         private void setIntrospectorPath(String pathFromRoot) {
             introspectorPath = pathFromRoot.length() > 0 ? pathFromRoot + "." : "";
         }
 
         public String getPath(String fieldName) {
             return introspectorPath + fieldName;
         }
 
         public final List<Field> getFields(String path, Class commonSuperclass, Object object1, Object object2) {
             setIntrospectorPath(path);
             prepareIntrospector(path, commonSuperclass, object1, object2);
             List<Field> result = doGetFields();
             clearIntrospector();
             return result;
         }
 
         //called before getFields to allow the introspector to initialize required values
         protected abstract void prepareIntrospector(String path, Class commonSuperclass, Object object1, Object object2);
 
         protected abstract List<Field> doGetFields();
 
         //called after doGetFields so introspector can clear down references to prevent memory leaks
         protected abstract void clearIntrospector();
     }
 
     /**
      * The AbstractListIntrospector can be used to introspect any classes for which we can obtain an ordered list of fields
      * The 'intelligent matching' mode (on by default) ignores phantom differences which occur due to extra elements appearing in
      * one or the other list - without intelligent matching turned on a difference at an early index would cause
      * every subsequent index in the lists to show a difference, since the comparison is performed index by index.
      * Intelligent matching used Object.equals() to try to determine which indexes are actually matched in order to flag up the
      * initial difference alone, and avoid showing phantom differences.
      */
     public abstract static class AbstractListIntrospector extends AbstractFieldIntrospector {
 
         private boolean useIntelligentMatching = true;
         private List list1;
         private Object o1;
         private List list2;
         private Object o2;
 
         protected void prepareIntrospector(String path, Class sharedSuperclass, Object object1, Object object2) {
             this.list1 = getList(object1);
             this.o1 = object1;
             this.list2 = getList(object2);
             this.o2 = object2;
             if ( useIntelligentMatching ) {
                 applyIntelligentMatching();
             }
         }
 
         protected abstract List getList(Object object);
 
         protected void clearIntrospector() {
             this.list1 = this.list2 = null;
             this.o1 = this.o2 = null;
         }
 
         protected void setUseIntelligentMatching(boolean useIntelligentMatching) {
             this.useIntelligentMatching = useIntelligentMatching;
         }
 
         private void applyIntelligentMatching() {
             List newListOne = new ArrayList();
             List newListTwo = new ArrayList();
 
             LinkedList stackOne = new LinkedList(list1);
             LinkedList stackTwo = new LinkedList(list2);
 
             Match m = findNextMatch(stackOne, stackTwo);
             while (m.isMatched()) {
                 popAndInsertToNextMatchIndex(newListOne, m.listOneIndex, stackOne);
                 addUndefinedValues(newListOne, m.listTwoIndex);
                 addUndefinedValues(newListTwo, m.listOneIndex);
                 popAndInsertToNextMatchIndex(newListTwo, m.listTwoIndex, stackTwo);
                 m = findNextMatch(stackOne, stackTwo);
             }
             //now add the reamining tail of both lists, which have no matches
             newListOne.addAll(stackOne);
             addUndefinedValues(newListOne, stackTwo.size());
             addUndefinedValues(newListTwo, stackOne.size());
             newListTwo.addAll(stackTwo);
             assert(newListOne.size() == newListTwo.size());
             list1 = newListOne;
             list2 = newListTwo;
         }
 
         //find the match with the lowest index from either stack
         private Match findNextMatch(LinkedList stackOne, LinkedList stackTwo) {
             Match m = new Match();
             for ( int oneIndex = 0; oneIndex < stackOne.size() && oneIndex < m.getMinIndex(); oneIndex++) {
                 for ( int twoIndex = 0; twoIndex < stackTwo.size() && oneIndex < m.getMinIndex(); twoIndex++) {
                     if ( isEqual(stackOne.get(oneIndex), stackTwo.get(twoIndex))) {
                         m.listOneIndex = oneIndex;
                         m.listTwoIndex = twoIndex;
                     }
                 }
             }
             return m;
         }
 
         private boolean isEqual(Object o, Object o1) {
             return o == o1 || ( o != null && o.equals(o1));
         }
 
         private void addUndefinedValues(List list, int numberToAdd) {
             for ( int count = 0; count < numberToAdd; count++) {
                 list.add(Field.UNDEFINED_FIELD_VALUE);
             }
         }
 
         private void popAndInsertToNextMatchIndex(List newListOne, int toIndex, LinkedList stack) {
             for ( int loop=0; loop <= toIndex; loop++) {
                 newListOne.add(stack.removeFirst());
             }
         }
 
         protected List<Field> doGetFields() {
             List<Field> result = new ArrayList<Field>();
             int maxSize = Math.max(list1.size(), list2.size());
             for (int loop=0; loop < maxSize; loop++) {
                 final int currentIndex = loop;
                 Field f = new Field() {
                     //need to copy the field state from the enclosing class, which will have
                     //its state cleared during cleanIntrospector()
                     List list1 = AbstractListIntrospector.this.list1;
                     List list2 = AbstractListIntrospector.this.list2;
                     Object o1 = AbstractListIntrospector.this.o1;
                     Object o2 = AbstractListIntrospector.this.o2;
 
                     public Class<?> getClassType() {
                         return ClassUtils.getCommonSuperclass(
                             getClassAt(list1, currentIndex),
                             getClassAt(list2, currentIndex)
                         );
                     }
 
                     private Class getClassAt(List l, int fieldIndex) {
                         Class result = null;
                         if ( l.size() > fieldIndex) {
                             Object o = l.get(fieldIndex);
                             if ( o != null) {
                                 result = o.getClass();
                             }
                         }
                         return result;
                     }
 
                     public Object getValue(Object o) {
                         return o == o1 ? getValueAt(list1, currentIndex) : getValueAt(list2, currentIndex);
                     }
 
                     private Object getValueAt(List l, int fieldIndex) {
                         return ( l.size() > fieldIndex) ? l.get(fieldIndex) : UNDEFINED_FIELD_VALUE;
                     }
 
                     public String getName() {
                         return String.valueOf(currentIndex);
                     }
 
                     public String getPath() {
                         return AbstractListIntrospector.this.getPath(getName());
                     }
                 };
                 result.add(f);
             }
             return result;
         }
 
         private class Match {
             int listOneIndex = Integer.MAX_VALUE;
             int listTwoIndex = Integer.MAX_VALUE;
 
             int getMinIndex() { return Math.min(listOneIndex, listTwoIndex); }
             boolean isMatched() { return listOneIndex != Integer.MAX_VALUE; }
         }
     }
 
     public static class ArrayIntrospector extends AbstractListIntrospector {
 
         protected List getList(Object o) {
             return ClassUtils.getList(o);
         }
     }
 
     public static class IterableIntrospector extends AbstractListIntrospector {
 
         protected List getList(Object o) {
             Iterable i = (Iterable)o;
             List result;
             if ( i instanceof List && i instanceof RandomAccess ) {
                 result = (List)i;
             } else if ( i instanceof Collection ) {
                 result = new ArrayList((Collection)i);
             } else {
                 result = new ArrayList();
                 for ( Object object: i) {
                     result.add(object);
                 }
             }
             return result;
         }
     }
 
     /**
      * Map introspector
      */
     public static class MapIntrospector extends AbstractFieldIntrospector {
 
         private Map o1;
         private Map o2;
 
         protected void prepareIntrospector(String path, Class commonSuperclass, Object object1, Object object2) {
             this.o1 = (Map)object1;
             this.o2 = (Map)object2;
         }
 
         protected void clearIntrospector() {
             this.o1 = this.o2 = null;
         }
 
         protected List<Field> doGetFields() {
             List<Object> allKeys = new ArrayList<Object>();
             //we want to process fields for the superset of all keys in the maps but provide predictable ordering for
             //SortedMap instances, otherwise testing is hard. We can't guarantee keys from both maps are comparable so
             //can't sort the superset, but we can use a List to at least provide a predictable result
             allKeys.addAll(o1.keySet());
             allKeys.addAll(o2.keySet());
 
             Set<Object> keysProcessed = new HashSet<Object>();
             List<Field> fields = new ArrayList<Field>();
             for ( final Object key : allKeys) {
                 if ( ! keysProcessed.contains(key)) {
                     fields.add(new Field() {
                         //need to copy the field state from the enclosing class, which will have
                         //its state cleared during cleanIntrospector()
                         Map o1 = MapIntrospector.this.o1;
                         Map o2 = MapIntrospector.this.o2;
 
                         public Class<?> getClassType() {
                             return getCommonSuperclass(key);
                         }
 
                         private Class<?> getCommonSuperclass(Object key) {
                             Class result;
                             //this is a bit tricky because the values in the maps may have different class types
                             //in which case we need to find and return the most specific common superclass.
                             Class c1 = getClass(key, o1);
                             Class c2 = getClass(key, o2);
                             result = ClassUtils.getCommonSuperclass(c1, c2);
                             return result;
                         }
 
                         private Class getClass(Object key, Map o1) {
                             Object o = o1.get(key);
                             return o == null ? null : o.getClass();
                         }
 
                         public Object getValue(Object o) {
                             if ( o == o1) {
                                 return doGetValue(o1, key);
                             } else {
                                 return doGetValue(o2, key);
                             }
                         }
 
                         public String getName() {
                             return key.toString();
                         }
 
                         public String getPath() {
                             return MapIntrospector.this.getPath(getName());
                         }
                     });
                 }
                 keysProcessed.add(key);
             }
             return fields;
         }
 
         private Object doGetValue(Map m, Object key) {
             return m.containsKey(key) ? m.get(key) : Field.UNDEFINED_FIELD_VALUE;
         }
     }
 
     /**
      * Convert unsorted sets to lists of objects for comparison
      * This attempts to align equal objects in the sets so they appear first in the lists,
      * followed by the unique objects to set 1 and the unique objects to set2
      *
      * The exact ordering of fields which results will be non-deterministic, due to the lack of guaranteed ordering
      * in an unsorted set - but this method should at least enable a sensible comparison in most cases
      */
     public static class UnsortedSetIntrospector extends AbstractListIntrospector {
         Set o1;
         Set o2;
         private List l1;
         private List l2;
 
         public UnsortedSetIntrospector() {
             //we're going to order the common (.equals() = true) elements to appear first
             //then the unique to set 1, then unique to set 2
             //so the intelligent matching algorithm is redundant here.
             setUseIntelligentMatching(false);
         }
 
         protected void prepareIntrospector(String path, Class commonSuperclass, Object object1, Object object2) {
             o1 = (Set)object1;
             o2 = (Set)object2;
             l1 = new LinkedList();
             l2 = new LinkedList();
 
             HashSet commonElements = new HashSet(o1);
             commonElements.retainAll(o2);
             for ( Object o : commonElements) {
                 l1.add(o);
                 l2.add(o);
             }
 
             HashSet setOneOnly = new HashSet(o1);
             setOneOnly.removeAll(commonElements);
             addValueAndUndefinedValue(setOneOnly, l1, l2);
 
             HashSet setTwoOnly = new HashSet(o2);
             setTwoOnly.removeAll(commonElements);
             addValueAndUndefinedValue(setTwoOnly, l2, l1);
             super.prepareIntrospector(path, commonSuperclass, object1, object2);
         }
 
         private void addValueAndUndefinedValue(HashSet setOneOnly, List listOne, List listTwo) {
             for (Object o : setOneOnly) {
                 listOne.add(o);
                 listTwo.add(Field.UNDEFINED_FIELD_VALUE);
             }
         }
 
         protected List getList(Object object) {
             return object == o1 ? l1 : l2;
         }
 
         protected void clearIntrospector() {
             o1 = o2 = null;
             l1 = l2 = null;
         }
     }
 
     /**
      * For each Field in the comparison, comparison behaviour is determined by applying a chain of rules which are
      * bound to field paths using path-matching regular expressions. Rules are evaluated in the order they were added to the
      * config. A rule may affect the FieldType, FieldComparator and FieldIntrospector used for a given field path.
      * Where a rule does not need to change one of these it should simply return the result provided by the previous rule
      * in the chain. The easy way to do this is to extend ConfigRuleBase
      */
     public static interface ConfigRule {
 
         /**
          * @return a type which indicates whether the values for this field should be compared, introspected or ignored
          */
         FieldType getType(FieldType lastTypeFromRuleChain, Field f);
 
         /**
          * This method is called to obtain an FieldComparator in cases were the final result of config.getType() was
          * FieldType.COMPARISON
          *
          * If this method returns null, the comparison objects will be considered equal if -->
          * 1- the object class implements Comparable and compareTo returns zero, or
          * 2- o1.equals(o2) returns true
          *
          * @return comparator to use for Field in cases where the fields compared are not equal by reference, or null.
          */
         FieldComparator getComparator(FieldComparator lastComparatorFromRuleChain, Field f);
 
 
         /**
          * This method will be called to get a FieldIntrospector for the values for Fields where the final result of
          * config.getType() was FieldType.INTROSPECTION
          *
          * @param classType, class of the Objects to compare, or the most specific common superclass
          * @return a FieldIntrospector which is responsible for determining a list of Fields given two objects for introspection.
          */
         FieldIntrospector getFieldIntrospector(FieldIntrospector lastIntrospectorFromRuleChain, String fieldPath, Class classType, Object o1, Object o2);
 
     }
 
     /**
      * FieldIntrospector calculates a list of logical Fields for two objects which are being introspected.
      * The objects will usually be of the same class type, so will have identical fields,
      * but in some cases fields may be included in the List which are only defined for one of the two objects.
      * This may happen, for example, if the two Objects are instances of classes with a shared superclass (so have some
      * common superclass fields), but the subclasses also define their own fields (so some fields are unique to each object)
      * In this case the field value for the object which does not support the field will be Field.UNDEFINED_FIELD_VALUE.
      * Alternatively, for MapIntrospector for example, in which Map entries are treated as Fields, one of the maps may not
      * contain keys which are present in the other.
      */
     public static interface FieldIntrospector {
 
         /**
          * @param pathPrefix - the path already parsed which should be used to prefix the field returned by this introspector
          * @param commonSuperclass - most specific common superclass to the two objects
          * @param object1 - first object for which we want to get fields
          * @param object2 - second object for which we want to determine fields
          */
         List<Field> getFields(String pathPrefix, Class commonSuperclass, Object object1, Object object2);
     }
 
     /**
      * An abstract representation of a Field discovered by introspecting a class/object instance
      */
     public static interface Field {
 
         //a value which should be returned by Field.getValue() if a field is undefined
         //for a given object, or a value cannot otherwise be determined for some reason
         Object UNDEFINED_FIELD_VALUE = new Object() {
             public String toString() {
                 return "Undefined";
             }
         };
 
         /**
          * @return class type for this field, which may be a superclass of the actual instance data
          */
         Class<?> getClassType();
 
         /**
          * Get a value for the object provided
          * @param o, either the object1 or object2 which was provided to the FieldIntrospector which generated this Field definition
          * @return a value for this field which may be null or UNDEFINED_FIELD_VALUE if a value cannot be determined for this object
          * @throws Exception, if an error occurs which is serious enough to abort the comparison
          */
         Object getValue(Object o) throws Exception;
 
         /**
          * Get the name of the field, which is also the last token from the field path
          * The field names should perferably be unique per class, but duplicate names are tolerated
          * (there are cases where a superclass and subclass may have a field with the same name)
          * @return Name for this field, to be used in the difference descriptions.
          */
         String getName();
 
         /**
          * @return String representing the full path of this field from the comparison root object
          */
         String getPath();
     }
 
     public enum FieldType {
         COMPARISON,
         INTROSPECTION,
         IGNORE
     }
 
     private class ComparisonException extends RuntimeException {
         public ComparisonException(Throwable e) {
             super(e);
         }
     }
 
     /**
      * A comparator which can be used to determine whether two field values are equal
      * without relying on Object.equals()
      */
     public static interface FieldComparator<E> {
         boolean isEqual(Field field, E object1, E object2);
     }
 }
