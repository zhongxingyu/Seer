 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.process.integrate;
 
 import net.epsilony.mf.process.MFProcessType;
 import net.epsilony.mf.process.assembler.Assembler;
 import net.epsilony.mf.process.assembler.LagrangleAssembler;
 import net.epsilony.mf.process.integrate.point.MFIntegratePoint;
 import net.epsilony.tb.synchron.SynchronizedIterator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class SimpMFIntegrator extends AbstractMFIntegrator {
 
     public static Logger logger = LoggerFactory.getLogger(SimpMFIntegrator.class);
     MFIntegratorObserver observer;
 
     @Override
     public void integrate() {
         initMainMatrixVector();
         for (MFProcessType type : MFProcessType.values()) {
             integrateByType(type);
         }
     }
 
     private void initMainMatrixVector() {
         integrateResult = new RawMFIntegrateResult();
         integrateResult.mainMatrix = mainMatrixFactory.produce();
         integrateResult.mainVector = mainVectorFactory.produce();
     }
 
     private void integrateByType(MFProcessType type) {
         MFIntegratorCore core = integratorCoresGroup.get(type);
         SynchronizedIterator<MFIntegratePoint> integrateUnits = integrateUnitsGroup.get(type);
 
         if (null == integrateUnits) {
             return;
         }
         Assembler assembler = assemblersGroup.get(type);
         assembler.setMainMatrix(integrateResult.mainMatrix);
         assembler.setMainVector(integrateResult.mainVector);
         core.setAssembler(assembler);
         core.setMixer(mixerFactory.produce());
        MFIntegratePoint integrateUnit = integrateUnits.nextItem();
        while (integrateUnit != null) {
             core.setIntegrateUnit(integrateUnit);
             core.integrate();
             if (null != observer) {
                 observer.integrated(this);
             }
            integrateUnit = integrateUnits.nextItem();
         }
     }
 
     public void setObserver(MFIntegratorObserver observer) {
         this.observer = observer;
     }
 
     @Override
     public MFIntegrateResult getIntegrateResult() {
 
         Assembler dirichletAssembler = assemblersGroup.get(MFProcessType.DIRICHLET);
 
        boolean lagrangle = dirichletAssembler != null && dirichletAssembler instanceof LagrangleAssembler;
         integrateResult.setLagrangle(lagrangle);
        if (!lagrangle) {
            return integrateResult;
         }
 
        LagrangleAssembler lagAssembler = (LagrangleAssembler) dirichletAssembler;
        integrateResult.setLagrangleDimension(lagAssembler.getLagrangeDimension());

         return integrateResult;
 
     }
 }
