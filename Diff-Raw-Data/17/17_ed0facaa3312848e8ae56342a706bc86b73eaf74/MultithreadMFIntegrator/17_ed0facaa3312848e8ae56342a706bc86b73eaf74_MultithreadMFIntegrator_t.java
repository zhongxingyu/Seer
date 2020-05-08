 /* (c) Copyright by Man YUAN */
 package net.epsilony.mf.process.integrate;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.EnumMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import net.epsilony.mf.process.MFMixer;
 import net.epsilony.mf.process.MFProcessType;
 import net.epsilony.mf.process.assembler.Assembler;
 import net.epsilony.mf.process.assembler.matrix_merge.BigDecimalLagrangleDiagCompatibleMatrixMerger;
 import net.epsilony.mf.process.assembler.matrix_merge.LagrangleDiagCompatibleMatrixMerger;
 import net.epsilony.mf.process.assembler.matrix_merge.MatrixMerger;
 import net.epsilony.mf.process.assembler.matrix_merge.SimpBigDecimalMatrixMerger;
 import net.epsilony.mf.process.assembler.matrix_merge.SimpMatrixMerger;
 import net.epsilony.mf.util.SynchronizedFactoryWrapper;
 import net.epsilony.mf.util.matrix.BigDecimalMFMatrix;
 import net.epsilony.mf.util.matrix.MFMatrix;
 import net.epsilony.tb.Factory;
 import org.apache.commons.lang3.SerializationUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a>
  */
 public class MultithreadMFIntegrator extends AbstractMFIntegrator {
 
     Integer forcibleThreadNum;
     ArrayList<MFIntegrator> subIntegrators;
     Factory<MFMixer> synchronizedMixerFactory;
     Factory<? extends MFMatrix> synchronizedMainMatrixFactory;
     Factory<? extends MFMatrix> synchronizedMainVectorFactory;
     Logger logger = LoggerFactory.getLogger(MultithreadMFIntegrator.class);
 
     public MultithreadMFIntegrator() {
     }
 
     public MultithreadMFIntegrator(Integer forcibleThreadNum) {
         this.forcibleThreadNum = forcibleThreadNum;
     }
 
     @Override
     public void integrate() {
         integrateResult = null;
         produceIntegrators();
         executeIntegrators();
         mergeSubIntegrateResults();
     }
 
     private void produceIntegrators() {
         int threadsNum = getThreadsNum();
         subIntegrators = new ArrayList<>(threadsNum);
         for (int i = 0; i < threadsNum; i++) {
             subIntegrators.add(genSubIntegrator());
         }
         logger.info("produced {} sub-integrators", threadsNum);
     }
 
     private void executeIntegrators() {
         ExecutorService executor = Executors.newFixedThreadPool(subIntegrators.size());
         ArrayList<Future<?>> futures = new ArrayList<>(subIntegrators.size());
         for (MFIntegrator subIntegrator : subIntegrators) {
             Future<?> future = executor.submit(new IntegrateRunnable(subIntegrator));
             futures.add(future);
         }
         executor.shutdown();
         logger.info("integrating with {} threads", subIntegrators.size());
         waitTillExecutorFinished(executor);
         logger.info("all sub-integrators' missions accomplished");
         for (Future<?> future : futures) {
             try {
                 future.get();
             } catch (InterruptedException | ExecutionException ex) {
                 logger.error("sub integrator execution error");
                 throw new IllegalStateException(ex);
             }
         }
     }
 
     private void mergeSubIntegrateResults() {
 
         Iterator<MFIntegrator> subIntegratorsIter = subIntegrators.iterator();
         MFIntegrator firstIntegrator = subIntegratorsIter.next();
         MFIntegrateResult firstIntegrateResult = firstIntegrator.getIntegrateResult();
 
         integrateResult = new RawMFIntegrateResult();
         integrateResult.set(firstIntegrateResult);
 
         if (subIntegrators.size() == 1) {
             return;
         }
         logger.info("start merging {} sub-integrators' work", subIntegrators.size());
         int count = 1;
         MatrixMerger mainMatrixMerger = getMainMatrixMerger();
         MatrixMerger mainVectorMerger = getMainVectorMerger();
         mainMatrixMerger.setDestiny(integrateResult.mainMatrix);
         mainVectorMerger.setDestiny(integrateResult.mainVector);
         do {
             MFIntegrator subIntegrator = subIntegratorsIter.next();
             MFIntegrateResult subIntegrateResult = subIntegrator.getIntegrateResult();
 
             if (subIntegrateResult.isLagrangle() != integrateResult.isLagrangle() || subIntegrateResult.getLagrangleDimension() != integrateResult.getLagrangleDimension()) {
                 throw new IllegalStateException();
             }
 
             mainMatrixMerger.setSource(subIntegrateResult.getMainMatrix());
             mainMatrixMerger.merge();
             mainVectorMerger.setSource(subIntegrateResult.getMainVector());
             mainVectorMerger.merge();
             count++;
             logger.info("mergied {}/{} assemblers", count, subIntegrators.size());
 
         } while (subIntegratorsIter.hasNext());
     }
 
     @Override
     public MFIntegrateResult getIntegrateResult() {
         return integrateResult;
     }
 
     private MatrixMerger getMainMatrixMerger() {
         if (integrateResult.getMainMatrix() instanceof BigDecimalMFMatrix) {
             if (integrateResult.isLagrangle()) {
                 BigDecimalLagrangleDiagCompatibleMatrixMerger merger = new BigDecimalLagrangleDiagCompatibleMatrixMerger();
                 merger.setLagrangleSize(integrateResult.getLagrangleDimension());
                 return merger;
             } else {
                 return new SimpBigDecimalMatrixMerger();
             }
         } else if (integrateResult.isLagrangle()) {
             LagrangleDiagCompatibleMatrixMerger merger = new LagrangleDiagCompatibleMatrixMerger();
             merger.setLagrangleSize(integrateResult.getLagrangleDimension());
             return merger;
         } else {
             return new SimpMatrixMerger();
         }
     }
 
     private MatrixMerger getMainVectorMerger() {
         if (integrateResult.mainVector instanceof BigDecimalMFMatrix) {
             return new SimpBigDecimalMatrixMerger();
         } else {
             return new SimpMatrixMerger();
         }
     }
 
     private static class IntegrateRunnable implements Runnable {
 
         MFIntegrator integrator;
 
         public IntegrateRunnable(MFIntegrator integrator) {
             this.integrator = integrator;
         }
 
         @Override
         public void run() {
             integrator.integrate();
         }
     }
 
     private void waitTillExecutorFinished(ExecutorService executor) {
         int lastVolume = -1;
         int lastNeumann = -1;
         int lastDirichlet = -1;
         while (!executor.isTerminated()) {
             try {
                 executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
 
                 int vol = integrateUnitsGroup.get(MFProcessType.VOLUME).getCount();
                 int neu = integrateUnitsGroup.get(MFProcessType.NEUMANN).getCount();
                 int diri = integrateUnitsGroup.get(MFProcessType.DIRICHLET).getCount();
                 boolean needLog = false;
                 if (vol != lastVolume) {
                     lastVolume = vol;
                     needLog = true;
                 }
                 if (neu != lastNeumann) {
                     lastNeumann = neu;
                     needLog = true;
                 }
                 if (diri != lastDirichlet) {
                     lastDirichlet = diri;
                     needLog = true;
                 }
                 if (!needLog) {
                     continue;
                 }
                 logger.info("processed (V,N,D)= {}/{},{}/{},{}/{}",
                         vol,
                         integrateUnitsGroup.get(MFProcessType.VOLUME).getEstimatedSize(),
                         neu,
                         integrateUnitsGroup.get(MFProcessType.NEUMANN).getEstimatedSize(),
                         diri,
                         integrateUnitsGroup.get(MFProcessType.DIRICHLET).getEstimatedSize());
             } catch (InterruptedException ex) {
                 logger.error("Processing interrupted {}", ex);
                 throw new IllegalStateException(ex);
             }
         }
     }
 
     private int getThreadsNum() {
         if (null == forcibleThreadNum) {
             return Runtime.getRuntime().availableProcessors();
         } else {
             return forcibleThreadNum;
         }
     }
 
     private MFIntegrator genSubIntegrator() {
         SimpMFIntegrator simpMFIntegrator = new SimpMFIntegrator();
         simpMFIntegrator.setIntegrateUnitsGroup(integrateUnitsGroup);
         simpMFIntegrator.setAssemblersGroup(cloneAssemblersGroup());
         simpMFIntegrator.setIntegratorCoresGroup(cloneIntegratorCoresGroup());
         simpMFIntegrator.setMixerFactory(synchronizedMixerFactory);
         simpMFIntegrator.setMainMatrixFactory(synchronizedMainMatrixFactory);
         simpMFIntegrator.setMainVectorFactory(synchronizedMainVectorFactory);
         return simpMFIntegrator;
     }
 
     private Map<MFProcessType, Assembler> cloneAssemblersGroup() {
         return cloneMapWithSameKeyAndClonedValue(assemblersGroup);
     }
 
     private Map<MFProcessType, MFIntegratorCore> cloneIntegratorCoresGroup() {
         return cloneMapWithSameKeyAndClonedValue(integratorCoresGroup);
     }
 
     private static <K extends Enum<K>, V extends Serializable> Map<K, V> cloneMapWithSameKeyAndClonedValue(Map<K, V> src) {
         EnumMap result = new EnumMap(MFProcessType.class);
         for (Map.Entry<K, V> entry : src.entrySet()) {
             result.put(entry.getKey(), SerializationUtils.clone(entry.getValue()));
         }
         return result;
     }
 
     @Override
     public void setMainMatrixFactory(Factory<? extends MFMatrix> mainMatrixFactory) {
         super.setMainMatrixFactory(mainMatrixFactory);
         synchronizedMainMatrixFactory = new SynchronizedFactoryWrapper<>(mainMatrixFactory);
     }
 
     @Override
     public void setMainVectorFactory(Factory<? extends MFMatrix> mainVectorFactory) {
         super.setMainVectorFactory(mainVectorFactory);
         synchronizedMainVectorFactory = new SynchronizedFactoryWrapper<>(mainVectorFactory);
     }
 
     @Override
     public void setMixerFactory(Factory<? extends MFMixer> mixerFactory) {
         super.setMixerFactory(mixerFactory);
         synchronizedMixerFactory = new SynchronizedFactoryWrapper<>(mixerFactory);
     }
 
     public Integer getForcibleThreadNum() {
         return forcibleThreadNum;
     }
 
     public void setForcibleThreadNum(Integer forcibleThreadNum) {
         if (null != forcibleThreadNum) {
             if (forcibleThreadNum < 1) {
                 throw new IllegalArgumentException("forcible thread num should be null or >= 1, not " + forcibleThreadNum);
             }
         }
         this.forcibleThreadNum = forcibleThreadNum;
     }
 }
