 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.epsilony.simpmeshfree.model;
 
 import gnu.trove.list.array.TDoubleArrayList;
 import java.util.ArrayList;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 import net.epsilony.simpmeshfree.model2d.ShapeFunctions2D;
 import net.epsilony.simpmeshfree.utils.CountableQuadraturePointIterator;
 import net.epsilony.simpmeshfree.utils.QuadraturePoint;
 import net.epsilony.simpmeshfree.utils.QuadraturePointIterator;
 import net.epsilony.simpmeshfree.utils.QuadraturePointIterators;
 import net.epsilony.utils.geom.Coordinate;
 import net.epsilony.utils.math.EquationSolver;
 import no.uib.cipr.matrix.DenseVector;
 
 /**
  *
  * @author epsilonyuan@gmail.com
  */
 public class WeakformProcessor {
     WeakformProcessorMonitor monitor;
 
     public WeakformProcessorMonitor getMonitor() {
         return monitor;
     }
 
     public void setMonitor(WeakformProcessorMonitor monitor) {
         this.monitor = monitor;
     }
     ShapeFunctionFactory shapeFunFactory;
     public int arrayListSize = 100;
     WeakformAssemblier assemblier;
     WeakformProblem workProblem;
     public EquationSolver equationSolver;
     DenseVector equationResultVector;
     
     public int processThreadsNum = Integer.MAX_VALUE;
     private int dim;
     CountableQuadraturePointIterator balanceIterator;
     CountableQuadraturePointIterator dirichletIterator;
     CountableQuadraturePointIterator neumannIterator;
 
     /**
      *
      * @param shapeFun
      * @param assemblier
      * @param workProblem
      * @param power
      * @param equationSolver
      */
     public WeakformProcessor(ShapeFunctionFactory shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver) {
         this.shapeFunFactory = shapeFunFactory;
         this.assemblier = assemblier;
         this.workProblem = workProblem;
         this.equationSolver = equationSolver;
         setDim(2);
     }
 
     /**
      *
      * @param shapeFun
      * @param assemblier
      * @param workProblem
      * @param power
      * @param equationSolver
      */
     public WeakformProcessor(ShapeFunctionFactory shapeFunFactory, WeakformAssemblier assemblier, WeakformProblem workProblem, EquationSolver equationSolver, int dim) {
         this.shapeFunFactory = shapeFunFactory;
         this.assemblier = assemblier;
         this.workProblem = workProblem;
         this.equationSolver = equationSolver;
         setDim(dim);
     }
 
     public void process() {
         int aviCoreNum = Runtime.getRuntime().availableProcessors();
         process(aviCoreNum);
     }
 
     public void process(int coreNum) {
         if (null==monitor){
             monitor=new WeakformProcessorMonitors.SimpLogger();
             monitor.setProcessor(this);
         }
 
         int[] numOut = new int[1];
         QuadraturePointIterator qpIter = workProblem.volumeIterator(numOut);
         balanceIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));
 
         qpIter = workProblem.neumannIterator(numOut);
         neumannIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));
 
         qpIter = workProblem.dirichletIterator(numOut);
         dirichletIterator = (qpIter == null ? null : QuadraturePointIterators.wrap(qpIter, numOut[0], true));
 
         ExecutorService executor = Executors.newFixedThreadPool(coreNum);
         
         monitor.beforeProcess(executor, coreNum);
         for (int i = 0; i < coreNum; i++) {
             executor.execute(new ProcessCore(i));
         }
         executor.shutdown();
         
         monitor.processStarted(executor);
         
         while(!executor.isTerminated()){
             try {
                 executor.awaitTermination(100, TimeUnit.MICROSECONDS);
             } catch (InterruptedException ex) {
                 break;
             }
         }
         
         assemblier.uniteAvators();
         
         monitor.avatorUnited(assemblier);
     }
 
     class ProcessCore implements Runnable,WithId {
 
         int id;
 
         @Override
         public int getId() {
             return id;
         }
 
         public ProcessCore(int id) {
             this.id = id;
         }
 
         @Override
         public void run() {
             ShapeFunction shapeFun = shapeFunFactory.factory();
             WeakformAssemblier assemblierAvator = assemblier.avatorInstance();
             monitor.avatorInited(assemblier, shapeFun, id);
             
             assemblyBalanceEquation(shapeFun, assemblierAvator,id);
 
             if (null != neumannIterator) {
                 assemblyNeumann(shapeFun, assemblierAvator,id);
             }
 
             if (null != dirichletIterator) {
                 assemblyDirichlet(shapeFun, assemblierAvator,id);
             }
         }
 
         @Override
         public void setId(int id) {
             throw new UnsupportedOperationException("Not supported yet.");
         }
     }
 
     void assemblyBalanceEquation(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator,int id) {
 
         ArrayList<Node> shapeFunNodes = new ArrayList<>(arrayListSize);
         VolumeCondition volumnBoundaryCondition = workProblem.volumeCondition();
         shapeFun.setDiffOrder(1);
         QuadraturePoint qp = new QuadraturePoint();
         Coordinate qPoint = qp.coordinate;
         TDoubleArrayList[] shapeFunVals = initShapeFunVals(1);
         while (balanceIterator.next(qp)) {
             shapeFun.values(qPoint, null, shapeFunVals, shapeFunNodes);
             assemblierAvator.asmBalance(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition);
             monitor.balanceAsmed(qp, shapeFunNodes, shapeFunVals, volumnBoundaryCondition, id);
         }
     }
 
     void assemblyNeumann(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator,int id) {
         shapeFun.setDiffOrder(0);
         QuadraturePoint qp = new QuadraturePoint();
         ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
         TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
         while (neumannIterator.next(qp)) {
             Coordinate qPoint = qp.coordinate;
             Boundary bound = qp.boundary;
             shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
             assemblierAvator.asmNeumann(qp, shapeFunNds, shapeFunVals);
             monitor.neumannAsmed(qp, shapeFunNds, shapeFunVals, id);
         }
     }
 
     void assemblyDirichlet(ShapeFunction shapeFun, WeakformAssemblier assemblierAvator,int id) {
 
         ArrayList<Node> shapeFunNds = new ArrayList<>(arrayListSize);
         shapeFun.setDiffOrder(0);
         QuadraturePoint qp = new QuadraturePoint();
         TDoubleArrayList[] shapeFunVals = initShapeFunVals(0);
         while (dirichletIterator.next(qp)) {
             Coordinate qPoint = qp.coordinate;
             Boundary bound = qp.boundary;
             shapeFun.values(qPoint, bound, shapeFunVals, shapeFunNds);
             assemblierAvator.asmDirichlet(qp, shapeFunNds, shapeFunVals);
             monitor.dirichletAsmed(qp, shapeFunNds, shapeFunVals, id);
         }
     }
 
     private void setDim(int dim) {
         if (dim < 2 || dim > 3) {
             throw new IllegalArgumentException("The problem dimension should be 2D or 3D only, illegal dim: " + dim);
         }
         this.dim = dim;
     }
 
     private TDoubleArrayList[] initShapeFunVals(int diffOrder) {
         if (dim == 2) {
             return ShapeFunctions2D.initOutputResult(diffOrder);
         } else {
             //TODO 3Dissue
             throw new UnsupportedOperationException();
         }
     }
 
     public void solveEquation() {
         monitor.beforeEquationSolve();
         equationResultVector = equationSolver.solve(assemblier.getEquationMatrix(), assemblier.getEquationVector());
         monitor.equationSolved();
     }
 
     public DenseVector getNodesValue() {
         return equationResultVector;
     }
 }
