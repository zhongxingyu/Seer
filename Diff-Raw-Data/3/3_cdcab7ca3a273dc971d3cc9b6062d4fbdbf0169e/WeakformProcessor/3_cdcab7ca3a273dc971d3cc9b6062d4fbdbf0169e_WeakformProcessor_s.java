 /* (c) Copyright by Man YUAN */
 package net.epsilony.tsmf.process;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import net.epsilony.tsmf.process.assemblier.WeakformLagrangeAssemblier;
 import net.epsilony.tsmf.process.assemblier.WeakformAssemblier;
 import net.epsilony.tsmf.cons_law.ConstitutiveLaw;
 import net.epsilony.tsmf.model.Model2D;
 import net.epsilony.tsmf.model.Node;
 import net.epsilony.tsmf.model.Segment2D;
 import net.epsilony.tsmf.model.influence.InfluenceRadiusCalculator;
 import net.epsilony.tsmf.model.support_domain.SupportDomainSearcherFactory;
 import net.epsilony.tsmf.shape_func.ShapeFunction;
 import net.epsilony.tsmf.util.IntIdentityMap;
 import net.epsilony.tsmf.util.NeedPreparation;
 import net.epsilony.tsmf.util.TimoshenkoAnalyticalBeam2D;
 import net.epsilony.tsmf.util.matrix.ReverseCuthillMcKeeSolver;
 import net.epsilony.tsmf.util.synchron.SynchronizedIteratorWrapper;
 import no.uib.cipr.matrix.DenseVector;
 import no.uib.cipr.matrix.Matrix;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class WeakformProcessor implements NeedPreparation {
 
     public static final int DENSE_MATRIC_SIZE_THRESHOLD = 200;
     public static final boolean SUPPORT_COMPLEX_CRITERION = false;
     public static final boolean DEFAULT_ENABLE_MULTITHREAD = true;
     WeakformQuadratureTask weakformQuadratureTask;
     InfluenceRadiusCalculator influenceRadiusCalculator;
     Model2D model;
     ShapeFunction shapeFunction;
     WeakformAssemblier assemblier;
     LinearLagrangeDirichletProcessor lagProcessor;
     ConstitutiveLaw constitutiveLaw;
     private List<WeakformQuadraturePoint> volumeProcessPoints;
     private List<WeakformQuadraturePoint> dirichletProcessPoints;
     private List<WeakformQuadraturePoint> neumannProcessPoints;
     SynchronizedIteratorWrapper<WeakformQuadraturePoint> volumeIteratorWrapper;
     SynchronizedIteratorWrapper<WeakformQuadraturePoint> neumannIteratorWrapper;
     SynchronizedIteratorWrapper<WeakformQuadraturePoint> dirichletIteratorWrapper;
     IntIdentityMap<Node, ProcessNodeData> nodesProcessDataMap;
     SupportDomainSearcherFactory supportDomainSearcherFactory;
     boolean enableMultiThread = DEFAULT_ENABLE_MULTITHREAD;
 
     public void setup(WeakformProject project) {
         setModel(project.getModel());
         setInfluenceRadiusCalculator(project.getInfluenceRadiusCalculator());
         setWeakformQuadratureTask(project.getWeakformQuadratureTask());
         setShapeFunction(project.getShapeFunction());
         setAssemblier(project.getAssemblier());
         setConstitutiveLaw(project.getConstitutiveLaw());
     }
 
     public void process() {
         int coreNum = Runtime.getRuntime().availableProcessors();
         ArrayList<WeakformAssemblier> assemblierAvators = new ArrayList<>(coreNum);
         assemblierAvators.add(assemblier);
         for (int i = 1; i < coreNum; i++) {
             assemblierAvators.add(assemblier.synchronizeClone());
         }
         ExecutorService executor = Executors.newFixedThreadPool(coreNum);
         for (int i = 0; i < assemblierAvators.size(); i++) {
             Mixer mixer = new Mixer(
                     shapeFunction.synchronizeClone(), supportDomainSearcherFactory.produce(), nodesProcessDataMap);
             WeakformProcessRunnable runnable = new WeakformProcessRunnable();
             runnable.setAssemblier(assemblierAvators.get(i));
             runnable.setMixer(mixer);
             runnable.setLagrangeProcessor(lagProcessor.synchronizeClone());
             runnable.setVolumeSynchronizedIterator(volumeIteratorWrapper);
             runnable.setDirichletSynchronizedIterator(dirichletIteratorWrapper);
             runnable.setNeumannSynchronizedIterator(neumannIteratorWrapper);
             executor.execute(runnable);
         }
 
         executor.shutdown();
         while (!executor.isTerminated()) {
             try {
                 executor.awaitTermination(1000, TimeUnit.MICROSECONDS);
             } catch (InterruptedException ex) {
                 break;
             }
         }
         for (int i = 1; i < assemblierAvators.size(); i++) {
             assemblier.mergeWithBrother(assemblierAvators.get(i));
         }
     }
 
     public boolean isActuallyMultiThreadable() {
         if (!isEnableMultiThread()) {
             return false;
         }
         int coreNum = Runtime.getRuntime().availableProcessors();
         if (coreNum <= 1) {
             return false;
         }
         return true;
     }
 
     public boolean isEnableMultiThread() {
         return enableMultiThread;
     }
 
     public void setEnableMultiThread(boolean enableMultiThread) {
         this.enableMultiThread = enableMultiThread;
     }
 
     @Override
     public void prepare() {
         prepareProcessIteratorWrappers();
 
         prepareSupportDomainSearcherFactoryWithoutInfluenceRadiusFilter();
 
         prepareProcessNodesDatas();
 
         supportDomainSearcherFactory.setNodesProcessDatasMap(nodesProcessDataMap);
 
         prepareAssemblier();
     }
 
     private void prepareProcessIteratorWrappers() {
         volumeProcessPoints = weakformQuadratureTask.volumeTasks();
         dirichletProcessPoints = weakformQuadratureTask.dirichletTasks();
         neumannProcessPoints = weakformQuadratureTask.neumannTasks();
         volumeIteratorWrapper =
                 new SynchronizedIteratorWrapper<>(volumeProcessPoints.iterator());
         neumannIteratorWrapper =
                 new SynchronizedIteratorWrapper<>(neumannProcessPoints.iterator());
         dirichletIteratorWrapper =
                 new SynchronizedIteratorWrapper<>(dirichletProcessPoints.iterator());
     }
 
     private void prepareSupportDomainSearcherFactoryWithoutInfluenceRadiusFilter() {
         supportDomainSearcherFactory = new SupportDomainSearcherFactory();
         supportDomainSearcherFactory.getNodesSearcher().setAll(model.getAllNodes());
         supportDomainSearcherFactory.getSegmentsSearcher().setAll(model.getPolygon().getSegments());
     }
 
     private void prepareProcessNodesDatas() {
         nodesProcessDataMap = new IntIdentityMap<>();
         nodesProcessDataMap.appendNullValues(model.getAllNodes().size());
         int index = 0;
         influenceRadiusCalculator.setSupportDomainSearcher(supportDomainSearcherFactory.produce());
         for (Node nd : model.getSpaceNodes()) {
             double rad = influenceRadiusCalculator.calcInflucenceRadius(nd, null);
             ProcessNodeData data = new ProcessNodeData();
             data.setInfluenceRadius(rad);
             data.setAssemblyIndex(index++);
             nodesProcessDataMap.put(nd, data);
         }
 
         for (Segment2D seg : model.getPolygon()) {
             Node nd = seg.getHead();
             double rad = influenceRadiusCalculator.calcInflucenceRadius(nd, seg);
             ProcessNodeData data = new ProcessNodeData();
             data.setInfluenceRadius(rad);
             data.setAssemblyIndex(index++);
             nodesProcessDataMap.put(nd, data);
         }
 
         if (isAssemblyDirichletByLagrange()) {
             for (WeakformQuadraturePoint qp : dirichletProcessPoints) {
                 ProcessNodeData[] datas = new ProcessNodeData[]{
                     nodesProcessDataMap.get(qp.segment.getHead()),
                     nodesProcessDataMap.get(qp.segment.getRear())};
                 for (ProcessNodeData lagNodeData : datas) {
                     if (null != lagNodeData) {
                         if (lagNodeData.getLagrangeAssemblyIndex() < 0) {
                             lagNodeData.setLagrangeAssemblyIndex(index++);
                         }
                     } else {
                         ProcessNodeData newData = new ProcessNodeData();
                         newData.setLagrangeAssemblyIndex(index++);
                         nodesProcessDataMap.put(qp.segment.getHead(), newData);
                     }
                 }
             }
         }
     }
 
     void prepareAssemblier() {
         assemblier.setConstitutiveLaw(constitutiveLaw);
         assemblier.setNodesNum(model.getAllNodes().size());
         boolean dense = model.getAllNodes().size() <= DENSE_MATRIC_SIZE_THRESHOLD;
         assemblier.setMatrixDense(dense);
         if (isAssemblyDirichletByLagrange()) {
             lagProcessor = new LinearLagrangeDirichletProcessor(nodesProcessDataMap);
             WeakformLagrangeAssemblier sL = (WeakformLagrangeAssemblier) assemblier;
             sL.setDirichletNodesNum(lagProcessor.getDirichletNodesSize());
         }
         assemblier.prepare();
     }
 
     public boolean isAssemblyDirichletByLagrange() {
         return assemblier instanceof WeakformLagrangeAssemblier;
     }
 
     public void solve() {
         Matrix mainMatrix = assemblier.getMainMatrix();
         DenseVector mainVector = assemblier.getMainVector();
         ReverseCuthillMcKeeSolver rcm = new ReverseCuthillMcKeeSolver(mainMatrix, assemblier.isUpperSymmertric());
         DenseVector nodesValue = rcm.solve(mainVector);
         int nodeValueDimension = getNodeValueDimension();
         for (ProcessNodeData nodeData : nodesProcessDataMap) {
 
             int nodeValueIndex = nodeData.getAssemblyIndex() * nodeValueDimension;
             if (nodeValueIndex >= 0) {
                 double[] nodeValue = new double[nodeValueDimension];
                 for (int i = 0; i < nodeValueDimension; i++) {
                     nodeValue[i] = nodesValue.get(i + nodeValueIndex);
                     nodeData.setValue(nodeValue);
                 }
             }
 
             int lagrangeValueIndex = nodeData.getLagrangeAssemblyIndex() * nodeValueDimension;
             if (lagrangeValueIndex >= 0) {
                 double[] lagrangeValue = new double[nodeValueDimension];
                 for (int i = 0; i < nodeValueDimension; i++) {
                     lagrangeValue[i] = nodesValue.get(i + lagrangeValueIndex);
                     nodeData.setLagrangleValue(lagrangeValue);
                 }
             }
 
         }
     }
 
     public PostProcessor postProcessor() {
         return new PostProcessor(
                 shapeFunction,
                 supportDomainSearcherFactory.produce(),
                 nodesProcessDataMap, getNodeValueDimension());
     }
 
     public WeakformQuadratureTask getWeakformQuadratureTask() {
         return weakformQuadratureTask;
     }
 
     public void setWeakformQuadratureTask(WeakformQuadratureTask weakformQuadratureTask) {
         this.weakformQuadratureTask = weakformQuadratureTask;
     }
 
     public Model2D getModel() {
         return model;
     }
 
     public void setModel(Model2D model) {
         this.model = model;
     }
 
     public ShapeFunction getShapeFunction() {
         return shapeFunction;
     }
 
     public void setShapeFunction(ShapeFunction shapeFunction) {
         this.shapeFunction = shapeFunction;
     }
 
     public WeakformAssemblier getAssemblier() {
         return assemblier;
     }
 
     public void setAssemblier(WeakformAssemblier assemblier) {
         this.assemblier = assemblier;
     }
 
     public ConstitutiveLaw getConstitutiveLaw() {
         return constitutiveLaw;
     }
 
     public void setConstitutiveLaw(ConstitutiveLaw constitutiveLaw) {
         this.constitutiveLaw = constitutiveLaw;
     }
 
     public InfluenceRadiusCalculator getInfluenceRadiusCalculator() {
         return influenceRadiusCalculator;
     }
 
     public void setInfluenceRadiusCalculator(InfluenceRadiusCalculator influenceRadiusCalculator) {
         this.influenceRadiusCalculator = influenceRadiusCalculator;
     }
 
     public static WeakformProcessor genTimoshenkoProjectProcess() {
         TimoshenkoAnalyticalBeam2D timoBeam = new TimoshenkoAnalyticalBeam2D(48, 12, 3e7, 0.3, -1000);
         int quadDomainSize = 2;
         int quadDegree = 4;
         double inflRads = quadDomainSize * 4.1;
         TimoshenkoStandardTask task = new TimoshenkoStandardTask(timoBeam, quadDomainSize, quadDomainSize, quadDegree);
         WeakformProcessor res = new WeakformProcessor();
         res.setup(task.processPackage(quadDomainSize, inflRads));
         return res;
     }
 
     public static void main(String[] args) {
         WeakformProcessor process = genTimoshenkoProjectProcess();
         process.prepare();
         process.process();
         process.solve();
         PostProcessor pp = process.postProcessor();
         pp.value(new double[]{0.1, 0}, null);
     }
 
     public int getNodeValueDimension() {
         return assemblier.getNodeValueDimension();
     }
 }
