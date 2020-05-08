 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.process;
 
 import java.util.EnumMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import net.epsilony.mf.model.AnalysisModel;
 import net.epsilony.mf.model.GeomModel2DUtils;
 import net.epsilony.mf.model.MFNode;
 import net.epsilony.mf.model.influence.InfluenceRadiusCalculator;
 import net.epsilony.mf.model.load.MFLoad;
 import net.epsilony.mf.model.load.NodeLoad;
 import net.epsilony.mf.model.load.SegmentLoad;
 import net.epsilony.mf.process.assembler.Assembler;
 import net.epsilony.mf.process.assembler.AutoSparseMatrixFactory;
 import net.epsilony.mf.process.assembler.LagrangleAssembler;
 import net.epsilony.mf.process.integrate.MFIntegrateResult;
 import net.epsilony.mf.process.integrate.MFIntegrateTask;
 import net.epsilony.mf.process.integrate.MultithreadMFIntegrator;
 import net.epsilony.mf.process.integrate.RawMFIntegrateTask;
 import net.epsilony.mf.process.integrate.point.MFIntegratePoint;
 import net.epsilony.mf.process.solver.MFSolver;
 import net.epsilony.mf.project.MFProject;
 import net.epsilony.mf.util.matrix.MFMatrix;
 import net.epsilony.tb.solid.GeomUnit;
 import net.epsilony.tb.synchron.SynchronizedIterator;
 import org.apache.commons.lang3.SerializationUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import static net.epsilony.mf.project.MFProjectKey.*;
 import static net.epsilony.mf.process.MFPreprocessorKey.*;
 import net.epsilony.mf.shape_func.MFShapeFunction;
 import net.epsilony.mf.util.MFKey;
 import net.epsilony.mf.util.matrix.MatrixFactory;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class MFLinearProcessor {
 
     public static Logger logger = LoggerFactory.getLogger(MFLinearProcessor.class);
     protected MFProject project;
     protected MFNodesIndesProcessor nodesIndesProcessor = new MFNodesIndesProcessor();
     protected MFNodesInfluenceRadiusProcessor nodesInfluenceRadiusProcessor = new MFNodesInfluenceRadiusProcessor();
     protected MFMixerFactory mixerFactory = new MFMixerFactory();
     protected RawMFIntegrateTask integrateTaskCopy = new RawMFIntegrateTask();
     protected Map<MFKey, Object> settings = MFPreprocessorKey.getDefaultSettings();
    protected MultithreadMFIntegrator integrator;
 
     public void setProject(MFProject project) {
         this.project = project;
     }
 
     public Map<MFKey, Object> getSettings() {
         return settings;
     }
 
     public void preprocess() {
         logger.info("start preprocessing");
         prepare();
         integrate();
     }
 
     public MFIntegrateResult getIntegrateResult() {
         return integrator.getIntegrateResult();
     }
 
     public void solve() {
         MFSolver solver = (MFSolver) settings.get(MAIN_MATRIX_SOLVER);
         MFIntegrateResult integrateResult = getIntegrateResult();
         solver.setMainMatrix(integrateResult.getMainMatrix());
         solver.setMainVector(integrateResult.getMainVector());
         solver.solve();
 
         fillNodeValues(solver.getResult());
     }
 
     private void fillNodeValues(MFMatrix result) {
         int nodeValueDimension = (int) project.get(VALUE_DIMENSION);
         for (MFNode node : nodesIndesProcessor.getAllProcessNodes()) {
             int nodeValueIndex = node.getAssemblyIndex() * nodeValueDimension;
             if (nodeValueIndex >= 0) {
                 double[] nodeValue = new double[nodeValueDimension];
                 for (int i = 0; i < nodeValueDimension; i++) {
                     nodeValue[i] = result.get(i + nodeValueIndex, 0);
                     node.setValue(nodeValue);
                 }
             }
             int lagrangeValueIndex = node.getLagrangeAssemblyIndex();
             MFMatrix mainMatrix = getIntegrateResult().getMainMatrix();
             if (lagrangeValueIndex >= 0) {
                 double[] lagrangeValue = new double[nodeValueDimension];
                 boolean[] lagrangeValueValidity = new boolean[nodeValueDimension];
                 for (int i = 0; i < nodeValueDimension; i++) {
                     int index = lagrangeValueIndex * nodeValueDimension + i;
                     lagrangeValue[i] = result.get(index, 0);
                     lagrangeValueValidity[i] = mainMatrix.get(index, index) == 0;  //a prototyle of validity
                 }
                 node.setLagrangeValue(lagrangeValue);
                 node.setLagrangeValueValidity(lagrangeValueValidity);
             }
         }
         logger.info("filled nodes values");
     }
 
     public PostProcessor genPostProcessor() {
         PostProcessor result = new PostProcessor();
         result.setShapeFunction(SerializationUtils.clone((MFShapeFunction) project.get(SHAPE_FUNCTION)));
         result.setNodeValueDimension((int) project.get(VALUE_DIMENSION));
         result.setSupportDomainSearcher(nodesInfluenceRadiusProcessor.getSupportDomainSearcherFactory().produce());
         result.setMaxInfluenceRad(nodesInfluenceRadiusProcessor.getMaxNodesInfluenceRadius());
         return result;
     }
 
     private void prepare() {
         logger.info("start preparing");
         prepareIntegrateTask();
         prepareProcessNodesDatas();
         prepareMixerFactory();
         prepareAssemblersGroup();
         logger.info("prepared!");
     }
 
     private void integrate() {
         logger.info("start integrating");
        integrator = new MultithreadMFIntegrator();
         logger.info("integrate processor: {}", integrator);
 
         integrator.setAssemblersGroup((Map) project.get(ASSEMBLERS_GROUP));
         integrator.setIntegrateUnitsGroup(genIntegrateUnitsGroup());
         integrator.setMixerFactory(mixerFactory);
 
         AutoSparseMatrixFactory matrixFactory = new AutoSparseMatrixFactory();
         matrixFactory.setDenseMatrixFactory((MatrixFactory<? extends MFMatrix>) settings.get(DENSE_MAIN_MATRIX_FACTORY));
         matrixFactory.setSparseMatrixFactory((MatrixFactory<? extends MFMatrix>) settings.get(SPARSE_MAIN_MATRIX_FACTORY));
         matrixFactory.setNumRows(getMainMatrixSize());
         matrixFactory.setNumCols(getMainMatrixSize());
         integrator.setMainMatrixFactory(matrixFactory);
 
         MatrixFactory<? extends MFMatrix> vectorFactory = (MatrixFactory<? extends MFMatrix>) settings.get(MAIN_VECTOR_FACTORY);
         vectorFactory.setNumRows(getMainMatrixSize());
         vectorFactory.setNumCols(1);
         integrator.setMainVectorFactory(vectorFactory);
 
         integrator.integrate();
     }
 
     private void prepareIntegrateTask() {
         MFIntegrateTask projectTask = (MFIntegrateTask) project.get(INTEGRATE_TASKS);
         integrateTaskCopy.setVolumeTasks(projectTask.volumeTasks());
         integrateTaskCopy.setNeumannTasks(projectTask.neumannTasks());
         integrateTaskCopy.setDirichletTasks(projectTask.dirichletTasks());
         logger.info("made a integrate task copy {}", integrateTaskCopy);
     }
 
     private Map<MFProcessType, SynchronizedIterator<MFIntegratePoint>> genIntegrateUnitsGroup() {
         EnumMap<MFProcessType, SynchronizedIterator<MFIntegratePoint>> result = new EnumMap<>(MFProcessType.class);
         result.put(MFProcessType.VOLUME, SynchronizedIterator.produce(integrateTaskCopy.volumeTasks()));
         result.put(MFProcessType.NEUMANN, SynchronizedIterator.produce(integrateTaskCopy.neumannTasks()));
         result.put(MFProcessType.DIRICHLET, SynchronizedIterator.produce(integrateTaskCopy.dirichletTasks()));
         return result;
     }
 
     private int getMainMatrixSize() {
         int valueDimension = (Integer) project.get(VALUE_DIMENSION);
         return valueDimension * (nodesIndesProcessor.getAllGeomNodes().size() + nodesIndesProcessor.getLagrangleNodesNum());
     }
 
     private void prepareProcessNodesDatas() {
         AnalysisModel model = (AnalysisModel) project.get(ANALYSIS_MODEL);
         int spatialDimension = (int) project.get(SPATIAL_DIMENSION);
         nodesIndesProcessor.setSpaceNodes(model.getSpaceNodes());
         nodesIndesProcessor.setGeomRoot(model.getFractionizedModel().getGeomRoot());
         nodesIndesProcessor.setApplyDirichletByLagrange(isAssemblyDirichletByLagrange());
         nodesIndesProcessor.setDirichletBnds(searchDirichletBnds(model));
         nodesIndesProcessor.setSpatialDimension(spatialDimension);
         nodesIndesProcessor.process();
 
         nodesInfluenceRadiusProcessor.setAllNodes(nodesIndesProcessor.getAllGeomNodes());
         nodesInfluenceRadiusProcessor.setSpaceNodes(nodesIndesProcessor.getSpaceNodes());
         nodesInfluenceRadiusProcessor.setDimension(spatialDimension);
         switch (spatialDimension) {
             case 1:
                 nodesInfluenceRadiusProcessor.setBoundaries(null);
                 break;
             case 2:
                 nodesInfluenceRadiusProcessor.setBoundaries(GeomModel2DUtils.getAllSegments(model.getFractionizedModel().getGeomRoot()));
                 break;
             default:
                 throw new IllegalStateException();
         }
         nodesInfluenceRadiusProcessor.setInfluenceRadiusCalculator((InfluenceRadiusCalculator) project.get(INFLUENCE_RADIUS_CALCULATOR));
         nodesInfluenceRadiusProcessor.process();
 
         logger.info("nodes datas prepared");
     }
 
     private void prepareMixerFactory() {
         logger.info("start preparing mixer factory");
         MFShapeFunction shapeFunction = (MFShapeFunction) project.get(SHAPE_FUNCTION);
         logger.info("shape function: {}", shapeFunction);
         mixerFactory.setMaxNodesInfluenceRadius(nodesInfluenceRadiusProcessor.getMaxNodesInfluenceRadius());
         shapeFunction.setDimension((int) project.get(SPATIAL_DIMENSION));
         mixerFactory.setShapeFunction(shapeFunction);
         mixerFactory.setSupportDomainSearcherFactory(nodesInfluenceRadiusProcessor.getSupportDomainSearcherFactory());
 
     }
 
     protected void prepareAssemblersGroup() {
         logger.info("start preparing assembler");
         Map<MFProcessType, Assembler> assemblerGroup = (Map<MFProcessType, Assembler>) project.get(ASSEMBLERS_GROUP);
         for (Entry<MFProcessType, Assembler> entry : assemblerGroup.entrySet()) {
             int allGeomNodesSize = nodesIndesProcessor.getAllGeomNodes().size();
             Assembler assembler = entry.getValue();
             assembler.setNodesNum(allGeomNodesSize);
             assembler.setSpatialDimension((int) project.get(SPATIAL_DIMENSION));
             assembler.setValueDimension((int) project.get(VALUE_DIMENSION));
             if (assembler instanceof LagrangleAssembler) {
                 LagrangleAssembler sL = (LagrangleAssembler) assembler;
                 sL.setAllLagrangleNodesNum(nodesIndesProcessor.getLagrangleNodesNum());
             }
         }
         logger.info(
                 "prepared assemblers group: {}",
                 assemblerGroup);
     }
 
     protected boolean isAssemblyDirichletByLagrange() {
         Map<MFProcessType, Assembler> assemblerGroup = (Map<MFProcessType, Assembler>) project.get(ASSEMBLERS_GROUP);
         return assemblerGroup.get(MFProcessType.DIRICHLET) instanceof LagrangleAssembler;
     }
 
     public static List<GeomUnit> searchDirichletBnds(AnalysisModel model) {
         LinkedList<GeomUnit> dirichletBnd = new LinkedList<>();
 
         for (Map.Entry<GeomUnit, MFLoad> entry : model.getFractionizedModel().getLoadMap().entrySet()) {
             MFLoad load = entry.getValue();
             if (load instanceof SegmentLoad) {
                 SegmentLoad segLoad = (SegmentLoad) load;
                 if (!segLoad.isDirichlet()) {
                     continue;
                 }
             } else if (load instanceof NodeLoad) {
                 NodeLoad nodeLoad = (NodeLoad) load;
                 if (!nodeLoad.isDirichlet()) {
                     continue;
                 }
             } else {
                 continue;
             }
 
             dirichletBnd.add(entry.getKey());
         }
         return dirichletBnd;
     }
 }
