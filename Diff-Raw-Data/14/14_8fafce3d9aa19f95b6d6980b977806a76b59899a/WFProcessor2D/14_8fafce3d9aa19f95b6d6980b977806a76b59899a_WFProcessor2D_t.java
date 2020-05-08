 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.tsmf.process;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import gnu.trove.list.array.TIntArrayList;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import net.epsilony.tsmf.assemblier.SupportLagrange;
 import net.epsilony.tsmf.assemblier.WFAssemblier;
 import net.epsilony.tsmf.cons_law.ConstitutiveLaw;
 import net.epsilony.tsmf.model.LinearLagrangeDirichletProcessor;
 import net.epsilony.tsmf.model.Model2D;
 import net.epsilony.tsmf.model.Node;
 import net.epsilony.tsmf.model.Segment2D;
 import net.epsilony.tsmf.model.influence.InfluenceRadsCalc;
 import net.epsilony.tsmf.shape_func.ShapeFunction;
 import net.epsilony.tsmf.util.WithDiffOrderUtil;
 import net.epsilony.tsmf.util.matrix.ReverseCuthillMcKeeSolver;
 import no.uib.cipr.matrix.DenseVector;
 import no.uib.cipr.matrix.Matrix;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class WFProcessor2D {
 
     public static final int DEFAULT_CAPACITY = 60;
     public static final int DENSE_MATRIC_SIZE_THRESHOLD = 200;
     Project project;
     Model2D model;
     ShapeFunction shapeFunction;
     WFAssemblier assemblier;
     InfluenceRadsCalc inflRadCalc;
     double maxIfluenceRad;
     boolean complexCriterion = false;
     LinearLagrangeDirichletProcessor lagProcessor;
     ConstitutiveLaw constitutiveLaw;
     DenseVector nodesValue;
     private List<ProcessPoint> balanceProcessPoints;
     private List<ProcessPoint> dirichletProcessPoints;
     private List<ProcessPoint> neumannProcessPoints;
     private boolean lagDiri;
 
     public WFProcessor2D(Model2D model, InfluenceRadsCalc inflRadCalc, Project project, ShapeFunction shapeFunction, WFAssemblier assemblier, ConstitutiveLaw constitutiveLaw) {
         this.model = model;
         this.shapeFunction = shapeFunction;
         this.assemblier = assemblier;
         this.inflRadCalc = inflRadCalc;
         this.project = project;
         this.constitutiveLaw = constitutiveLaw;
     }
 
     public WFProcessor2D(ProcessPackage pack) {
         this(pack.model, pack.influenceRadCalc, pack.project, pack.shapeFunc, pack.assemblier, pack.constitutiveLaw);
     }
 
     public void process() {
         prepareInfluenceRads();
         processBalance();
         processNeumann();
         processDirichlet();
     }
 
     public void prepareAssemblier() {
         balanceProcessPoints = project.balance();
         dirichletProcessPoints = project.dirichlet();
         neumannProcessPoints = project.neumann();
 
         assemblier.setConstitutiveLaw(constitutiveLaw);
         assemblier.setNodesNum(model.getAllNodes().size());
         boolean dense = model.getAllNodes().size() <= DENSE_MATRIC_SIZE_THRESHOLD;
         assemblier.setMatrixDense(dense);
         lagDiri = isAssemblyDirichletByLagrange();
         if (lagDiri) {
             lagProcessor = new LinearLagrangeDirichletProcessor(dirichletProcessPoints, model.getAllNodes().size());
             SupportLagrange sL = (SupportLagrange) assemblier;
             sL.setDirichletNodesNums(lagProcessor.getDirichletNodesSize());
         }
         assemblier.prepare();
     }
 
     public void prepareInfluenceRads() {
 
         for (Node nd : model.getSpaceNodes()) {
             double rad = inflRadCalc.influcenceRadius(nd, null, model);
             model.setInfluenceRad(nd, rad);
         }
 
         for (Segment2D seg : model.getPolygon()) {
             Node nd = seg.getHead();
             double rad = inflRadCalc.influcenceRadius(nd, seg, model);
             model.setInfluenceRad(nd, rad);
         }
 
         maxIfluenceRad = model.maxInfluenceRad();
     }
 
     public boolean isAssemblyDirichletByLagrange() {
         return assemblier instanceof SupportLagrange;
     }
 
     public void processBalance() {
         final int diffOrder = 1;
 
         List<ProcessPoint> points = balanceProcessPoints;
         ArrayList<Node> nodes = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<double[]> coords = new ArrayList<>(DEFAULT_CAPACITY);
        ArrayList<Segment2D> segs = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<Node> blockedNds = null;
         ArrayList<Segment2D> blockNdsSegs = null;
         TIntArrayList nodesIds = new TIntArrayList(DEFAULT_CAPACITY, -1);
         TDoubleArrayList infRads = new TDoubleArrayList(DEFAULT_CAPACITY);
         TDoubleArrayList[] shapeFuncVals = WithDiffOrderUtil.initOutput(null, DEFAULT_CAPACITY, 2, diffOrder);
 
         if (complexCriterion) {
             blockedNds = new ArrayList<>(DEFAULT_CAPACITY);
             blockNdsSegs = new ArrayList<>(DEFAULT_CAPACITY);
         }
 
         shapeFunction.setDiffOrder(diffOrder);
         for (ProcessPoint pt : points) {
             model.searchModel(pt.coord, null, maxIfluenceRad, true, nodes, segs, blockedNds, blockNdsSegs);
 
             if (complexCriterion) {
                 throw new UnsupportedOperationException();
             }
 
             fromNodesToIdsCoordsInfRads(nodes, nodesIds, coords, infRads);
             shapeFunction.values(pt.coord, coords, infRads, null, shapeFuncVals);
 
             assemblier.asmBalance(pt.weight, nodesIds, shapeFuncVals, pt.value);
         }
     }
 
     public void processNeumann() {
         final int diffOrder = 0;
 
         List<ProcessPoint> points = neumannProcessPoints;
 
         ArrayList<Node> nodes = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<double[]> coords = new ArrayList<>(DEFAULT_CAPACITY);
        ArrayList<Segment2D> segs = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<Node> blockedNds = null;
         ArrayList<Segment2D> blockNdsSegs = null;
         TIntArrayList nodesIds = new TIntArrayList(DEFAULT_CAPACITY, -1);
         TDoubleArrayList infRads = new TDoubleArrayList(DEFAULT_CAPACITY);
         TDoubleArrayList[] shapeFuncVals = WithDiffOrderUtil.initOutput(null, DEFAULT_CAPACITY, 2, diffOrder);
 
         if (complexCriterion) {
             blockedNds = new ArrayList<>(DEFAULT_CAPACITY);
             blockNdsSegs = new ArrayList<>(DEFAULT_CAPACITY);
         }
 
         shapeFunction.setDiffOrder(diffOrder);
         for (ProcessPoint pt : points) {
             model.searchModel(pt.coord, pt.seg, maxIfluenceRad, true, nodes, segs, blockedNds, blockNdsSegs);
 
             if (complexCriterion) {
                 throw new UnsupportedOperationException();
             }
 
             fromNodesToIdsCoordsInfRads(nodes, nodesIds, coords, infRads);
             shapeFunction.values(pt.coord, coords, infRads, null, shapeFuncVals);
 
             assemblier.asmNeumann(pt.weight, nodesIds, shapeFuncVals, pt.value);
         }
     }
 
     public void processDirichlet() {
         final int diffOrder = 0;
 
         List<ProcessPoint> points = dirichletProcessPoints;
 
         ArrayList<Node> nodes = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<double[]> coords = new ArrayList<>(DEFAULT_CAPACITY);
        ArrayList<Segment2D> segs = new ArrayList<>(DEFAULT_CAPACITY);
         ArrayList<Node> blockedNds = null;
         ArrayList<Segment2D> blockNdsSegs = null;
         TIntArrayList nodesIds = new TIntArrayList(DEFAULT_CAPACITY, -1);
         TDoubleArrayList infRads = new TDoubleArrayList(DEFAULT_CAPACITY);
         TDoubleArrayList[] shapeFuncVals = WithDiffOrderUtil.initOutput(null, DEFAULT_CAPACITY, 2, diffOrder);
 
         if (complexCriterion) {
             blockedNds = new ArrayList<>(DEFAULT_CAPACITY);
             blockNdsSegs = new ArrayList<>(DEFAULT_CAPACITY);
         }
 
         shapeFunction.setDiffOrder(diffOrder);
         for (ProcessPoint pt : points) {
             model.searchModel(pt.coord, pt.seg, maxIfluenceRad, true, nodes, segs, blockedNds, blockNdsSegs);
 
             if (complexCriterion) {
                 throw new UnsupportedOperationException();
             }
 
             fromNodesToIdsCoordsInfRads(nodes, nodesIds, coords, infRads);
             shapeFunction.values(pt.coord, coords, infRads, null, shapeFuncVals);
 
             if (lagDiri) {
                 lagProcessor.process(pt, nodesIds, shapeFuncVals[0]);
             }
 
             assemblier.asmDirichlet(pt.weight, nodesIds, shapeFuncVals, pt.value, pt.mark);
         }
     }
 
     public void solve() {
         Matrix mainMatrix = assemblier.getMainMatrix();
         DenseVector mainVector = assemblier.getMainVector();
         ReverseCuthillMcKeeSolver rcm = new ReverseCuthillMcKeeSolver(mainMatrix, assemblier.isUpperSymmertric());
         nodesValue = rcm.solve(mainVector);
     }
 
     void fromNodesToIdsCoordsInfRads(
             Collection<? extends Node> nodes,
             TIntArrayList ids,
             ArrayList<double[]> coords,
             TDoubleArrayList infRads) {
         coords.clear();
         coords.ensureCapacity(nodes.size());
         ids.resetQuick();
         ids.ensureCapacity(nodes.size());
         for (Node nd : nodes) {
             coords.add(nd.coord);
             ids.add(nd.getId());
             infRads.add(model.getInfluenceRad(nd));
         }
     }
 }
