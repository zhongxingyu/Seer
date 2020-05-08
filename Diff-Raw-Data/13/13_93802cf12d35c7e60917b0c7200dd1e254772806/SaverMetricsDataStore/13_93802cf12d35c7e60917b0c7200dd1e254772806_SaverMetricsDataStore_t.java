 package Analyzer.save;
 
 import Analyzer.code.java.metrics.ClassMetrics;
 import Analyzer.code.java.metrics.MethodMetrics;
 import Analyzer.code.java.metrics.Metrics;
 import Analyzer.code.java.metrics.PackageMetrics;
 import java.util.Date;
 import org.codemetric.definition.CubeDefinition;
 import org.codemetric.definition.DataStoreDefinition;
 import org.codemetric.definition.DimensionDefinition;
 import org.sumus.dwh.cube.Cube;
 import org.sumus.dwh.datastore.Context;
 import org.sumus.dwh.datastore.DataStore;
 import org.sumus.dwh.datastore.Entity;
 import org.sumus.dwh.datastore.State;
 import org.sumus.dwh.datastore.Tuple;
 import org.sumus.dwh.dimension.Dimension;
 
 public class SaverMetricsDataStore {
 
     private final DataStore dataStore;
 
     public SaverMetricsDataStore(DataStore dataStore) {
         this.dataStore = dataStore;
     }
 
     public void save(PackageMetrics rootPackageMetrics) {
        for (Metrics currentClassMetrics : rootPackageMetrics.getClassMetricsList()) {
            childrenSaver(currentClassMetrics);
        }
        for (Metrics currentPackageMetrics : rootPackageMetrics.getPackageMetricsList()) {
            childrenSaver(currentPackageMetrics);
         }
     }
 
     private void childrenSaver(Metrics currentMetrics) {
         if (currentMetrics.isPackageMetrics()) {
             PackageMetrics metrics = (PackageMetrics) currentMetrics;
             insertPackageEntity(metrics);
            insertPackageTuple(metrics);
             save(metrics);
         }
         if (currentMetrics.isClassMetrics()) {
             ClassMetrics metrics = (ClassMetrics) currentMetrics;
             insertClassEntity(metrics);
            insertClassTuple(metrics);
             for (MethodMetrics methodMetrics : metrics.getMethodMetricsList()) {
                 insertMethodEntity(methodMetrics);
                insertMethodTuple(methodMetrics);
             }
         }
     }
 
     public DataStore getDataStore() {
         return dataStore;
     }
 
     private void insertPackageEntity(PackageMetrics packageMetrics) {
         Entity component = new Entity(packageMetrics.getName(), getDimension(DataStoreDefinition.MODULES));
         component.addFeature(DataStoreDefinition.NAME.getName(), packageMetrics.getName());
         component.addFeature(DataStoreDefinition.TYPE.getName(), "Package");
         add(component);
     }
 
     private void insertClassEntity(ClassMetrics classMetrics) {
         Entity component = new Entity(classMetrics.getName(), getDimension(DataStoreDefinition.MODULES));
         component.addFeature(DataStoreDefinition.NAME.getName(), classMetrics.getName());
         component.addFeature(DataStoreDefinition.TYPE.getName(), "Class");
         add(component);
     }
 
     private void insertMethodEntity(MethodMetrics methodMetrics) {
         Entity component = new Entity(methodMetrics.getName(), getDimension(DataStoreDefinition.MODULES));
         component.addFeature(DataStoreDefinition.NAME.getName(), methodMetrics.getName());
         component.addFeature(DataStoreDefinition.TYPE.getName(), "Method");
         add(component);
     }
 
     private void insertPackageTuple(PackageMetrics packageMetrics) {
         Context context = buildPackageContext(packageMetrics);
         State state = buildPackageState(packageMetrics);
         Tuple factShip = new Tuple(getCube(DataStoreDefinition.PACKAGE_CUBE), context, state);
         add(factShip);
     }
 
     private Context buildPackageContext(PackageMetrics packageMetrics) {
         Context context = new Context(new Date());
         context.put(DataStoreDefinition.MODULES.getName(), packageMetrics.getName());
         return context;
     }
 
     private State buildPackageState(PackageMetrics packageMetrics) {
         State state = new State();
         state.put(DataStoreDefinition.LINES_OF_CODE.getName(), packageMetrics.getCodeLines().getAllLines());
         state.put(DataStoreDefinition.CLASSES.getName(), packageMetrics.getNumberClass());
         state.put(DataStoreDefinition.EFFERENT_COUPLING_INTERNAL.getName(), 4.);
         state.put(DataStoreDefinition.EFFERENT_COUPLING_LIBRARY.getName(), 2.);
         state.put(DataStoreDefinition.AFFERENT_COUPLING.getName(), 4.);
         return state;
     }
 
     private void insertClassTuple(ClassMetrics classMetrics) {
         Context context = buildClassContext(classMetrics);
         State state = buildClassState(classMetrics);
         Tuple factShip = new Tuple(getCube(DataStoreDefinition.CLASS_CUBE), context, state);
         add(factShip);
     }
 
     private Context buildClassContext(ClassMetrics classMetrics) {
         Context context = new Context(new Date());
         context.put(DataStoreDefinition.MODULES.getName(), classMetrics.getName());
         return context;
     }
 
     private State buildClassState(ClassMetrics classMetrics) {
         State state = new State();
         state.put(DataStoreDefinition.LINES_OF_CODE.getName(), classMetrics.getCodeLines().getAllLines());
         state.put(DataStoreDefinition.ATTRIBUTES.getName(), classMetrics.getNumberOfAttribute());
         state.put(DataStoreDefinition.METHODS.getName(), classMetrics.getNumberOfMethod());
         state.put(DataStoreDefinition.LACK_OF_COHESION_OF_METHODS.getName(), classMetrics.getLackOfCohesion());
         state.put(DataStoreDefinition.EFFERENT_COUPLING_INTERNAL.getName(), 2.);
         state.put(DataStoreDefinition.AFFERENT_COUPLING.getName(), 4.);
         return state;
     }
 
     private void insertMethodTuple(MethodMetrics methodMetrics) {
         Context context = buildMethodContext(methodMetrics);
         State state = buildMethodState(methodMetrics);
         Tuple factShip = new Tuple(getCube(DataStoreDefinition.METHOD_CUBE), context, state);
         add(factShip);
     }
 
     private Context buildMethodContext(MethodMetrics methodMetrics) {
         Context context = new Context(new Date());
         context.put(DataStoreDefinition.MODULES.getName(), methodMetrics.getName());
         return context;
     }
 
     private State buildMethodState(MethodMetrics methodMetrics) {
         State state = new State();
         state.put(DataStoreDefinition.LINES_OF_CODE.getName(), methodMetrics.getCodeLines().getAllLines());
         state.put(DataStoreDefinition.PARAMETERS.getName(), methodMetrics.getNumberOfParameters());
         state.put(DataStoreDefinition.CYCLOMATIC_COMPLEXITY.getName(), methodMetrics.getCyclomaticComplexity());
         return state;
     }
 
     private Dimension getDimension(DimensionDefinition definition) {
         return getDataStore().getDimension(definition.getName());
     }
 
     private Cube getCube(CubeDefinition definition) {
         return getDataStore().getCube(definition.getName());
     }
 
     private void add(Entity entity) {
         getDataStore().insert(entity);
     }
 
     private void add(Tuple tuple) {
         getDataStore().insert(tuple);
     }
 }
