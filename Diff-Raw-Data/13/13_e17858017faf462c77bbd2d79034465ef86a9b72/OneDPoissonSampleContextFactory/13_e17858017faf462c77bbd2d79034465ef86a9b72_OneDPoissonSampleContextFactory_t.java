 /*
  * Copyright (C) 2013 Man YUAN <epsilon@epsilony.net>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.epsilony.mf.sample;
 
 import static net.epsilony.mf.util.MFUtils.rudeDefinition;
 import static net.epsilony.mf.util.MFUtils.rudeListDefinition;
 import static net.epsilony.mf.util.MFUtils.singletonName;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import net.epsilony.mf.model.AnalysisModel;
 import net.epsilony.mf.model.RawAnalysisModel;
 import net.epsilony.mf.model.factory.ChainAnalysisModelFactory;
 import net.epsilony.mf.model.influence.ConstantInfluenceRadiusCalculator;
 import net.epsilony.mf.model.influence.InfluenceRadiusCalculator;
 import net.epsilony.mf.model.sample.OneDPoissonSamplePhysicalModel;
 import net.epsilony.mf.model.sample.OneDPoissonSamplePhysicalModel.OneDPoissonSample;
 import net.epsilony.mf.process.MFLinearProcessor;
 import net.epsilony.mf.process.PostProcessor;
 import net.epsilony.mf.process.integrate.ChainIntegrateTaskFactory;
 import net.epsilony.tb.Factory;
 import net.epsilony.tb.TestTool;
 
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 /**
  * @author Man YUAN <epsilon@epsilony.net>
  * 
  */
 public class OneDPoissonSampleContextFactory implements Factory<Map<String, Object>> {
 
     int nodesNum = 21;
     double influenceRadiusRatio = 3.5;
    Integer threadNum = Runtime.getRuntime().availableProcessors();
     InfluenceRadiusCalculator influenceRadiusCalculator;
     AnnotationConfigApplicationContext context;
     Map<String, Object> result;
     OneDPoissonSample sampleChoice;
     private AnalysisModel analysisModel;
 
     @Configuration
     public static class ConfigurationClass {
         @Resource(name = "influenceRadius")
         double influenceRadius;
 
         @Bean
         public InfluenceRadiusCalculator influenceRadiusCalculator() {
             return new ConstantInfluenceRadiusCalculator(influenceRadius);
         }
 
         @Resource(name = "analysisModelHolder")
         List<AnalysisModel> analysisModelHolder;
 
         @Bean
         public AnalysisModel analysisModel() {
             return analysisModelHolder.get(0);
         }
 
         @Resource(name = "threadNumHolder")
         List<Integer> threadNumHolder;
 
         @Bean
         public Integer threadNum() {
             return threadNumHolder.get(0);
         }
     }
 
     @Override
     public Map<String, Object> produce() {
         result = new HashMap<>();
 
         genAnalysisModel();
 
         genContext();
 
         MFLinearProcessor processor = context.getBean(MFLinearProcessor.class);
         put(MFLinearProcessor.class, processor);
 
         put(ApplicationContext.class, context);
 
         return result;
     }
 
     private void genAnalysisModel() {
         OneDPoissonSamplePhysicalModel oneDPoissonSamplePhysicalModel = new OneDPoissonSamplePhysicalModel();
         oneDPoissonSamplePhysicalModel.setChoice(sampleChoice);
         put(OneDPoissonSamplePhysicalModel.class, oneDPoissonSamplePhysicalModel);
 
         ChainAnalysisModelFactory analysisModelFactory = new ChainAnalysisModelFactory();
         analysisModelFactory.setChainPhysicalModel(oneDPoissonSamplePhysicalModel);
         analysisModelFactory.setFractionLengthCap(genFractionLengthCap());
         analysisModel = analysisModelFactory.produce();
 
         tempIntegrateUnitMethod(analysisModel);
 
         put(AnalysisModel.class, analysisModel);
     }
 
     private void genContext() {
         context = new AnnotationConfigApplicationContext();
         context.register(OneDPoissonConf.class, ConfigurationClass.class);
         context.registerBeanDefinition("analysisModelHolder", rudeListDefinition(analysisModel));
         context.registerBeanDefinition("threadNumHolder", rudeListDefinition(threadNum));
         context.registerBeanDefinition("influenceRadius", rudeDefinition(Double.class, genInfluenceRadius()));
         context.refresh();
     }
 
    @SuppressWarnings({ "unchecked", "rawtypes" })
     private void tempIntegrateUnitMethod(AnalysisModel analysisModel) {
         ChainIntegrateTaskFactory factory = new ChainIntegrateTaskFactory();
         factory.setChainAnalysisModel(analysisModel);
         RawAnalysisModel rawModel = (RawAnalysisModel) analysisModel;
         rawModel.setIntegrateUnitsGroup((Map) factory.produce());
     }
 
     private double genInfluenceRadius() {
         OneDPoissonSamplePhysicalModel model = (OneDPoissonSamplePhysicalModel) result
                 .get(singletonName(OneDPoissonSamplePhysicalModel.class));
 
         double start = model.getTerminalPoistion(true);
         double end = model.getTerminalPoistion(false);
         double radials = (end - start) / (nodesNum - 1) * influenceRadiusRatio;
         if (radials <= 0) {
             throw new IllegalStateException();
         }
         return radials;
     }
 
     private double genFractionLengthCap() {
         OneDPoissonSamplePhysicalModel model = (OneDPoissonSamplePhysicalModel) result
                 .get(singletonName(OneDPoissonSamplePhysicalModel.class));
         double start = model.getTerminalPoistion(true);
         double end = model.getTerminalPoistion(false);
         return (end - start) / (nodesNum - 1.1);
     }
 
     private void put(Class<?> valueType, Object value) {
         result.put(singletonName(valueType), value);
     }
 
     public int getNodesNum() {
         return nodesNum;
     }
 
     public void setNodesNum(int nodesNum) {
         this.nodesNum = nodesNum;
     }
 
     public double getInfluenceRadiusRatio() {
         return influenceRadiusRatio;
     }
 
     public void setInfluenceRadiusRatio(double influenceRadiusRatio) {
         this.influenceRadiusRatio = influenceRadiusRatio;
     }
 
     public Integer getThreadNum() {
         return threadNum;
     }
 
     public void setThreadNum(Integer threadNum) {
         this.threadNum = threadNum;
     }
 
     public OneDPoissonSample getSampleChoice() {
         return sampleChoice;
     }
 
     public void setSampleChoice(OneDPoissonSample sampleChoice) {
         this.sampleChoice = sampleChoice;
     }
 
     public static void main(String[] args) {
         OneDPoissonSampleContextFactory contextFactory = new OneDPoissonSampleContextFactory();
         contextFactory.setSampleChoice(OneDPoissonSample.ZERO);
 
         Map<String, Object> context = contextFactory.produce();
         MFLinearProcessor processor = (MFLinearProcessor) context.get(singletonName(MFLinearProcessor.class));
 
         processor.preprocess();
         processor.solve();
         PostProcessor postProcessor = processor.genPostProcessor();
         for (double x : TestTool.linSpace(0.1, 0.9, 10)) {
             double[] result = postProcessor.value(new double[] { x, 0 }, null);
             System.out.println("at " + x + " result = " + result[0]);
         }
     }
 }
