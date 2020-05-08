 /**
  * 
  */
 package cern.accsoft.steering.jmad.modeldefs.defs.lhc;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import cern.accsoft.steering.jmad.domain.file.CallableModelFileImpl;
 import cern.accsoft.steering.jmad.domain.file.ModelPathOffsets;
 import cern.accsoft.steering.jmad.domain.file.ModelPathOffsetsImpl;
 import cern.accsoft.steering.jmad.domain.file.ModelFile.ModelFileLocation;
 import cern.accsoft.steering.jmad.domain.machine.RangeDefinitionImpl;
 import cern.accsoft.steering.jmad.modeldefs.create.OpticDefinitionSet;
 import cern.accsoft.steering.jmad.modeldefs.create.OpticDefinitionSetBuilder;
 import cern.accsoft.steering.jmad.modeldefs.create.OpticModelFileBuilder;
 import cern.accsoft.steering.jmad.modeldefs.domain.JMadModelDefinitionImpl;
 import cern.accsoft.steering.jmad.modeldefs.lhc.LhcUtil;
 
 /**
  * The model definition factory for the LHC model
  * 
  * @author muellerg
  */
 public class LhcNominalModelDefinitionFactory extends AbstractLhcModelDefinitionFactory {
 
     @Override
     protected void addInitFiles(JMadModelDefinitionImpl modelDefinition) {
         modelDefinition.addInitFile(new CallableModelFileImpl("init-constants.madx", ModelFileLocation.RESOURCE));
         modelDefinition.addInitFile(new CallableModelFileImpl("V6.5.seq"));
         modelDefinition.addInitFile(new CallableModelFileImpl("install_additional_elements.madx"));
         modelDefinition.addInitFile(new CallableModelFileImpl("install-ac-dipole.madx", ModelFileLocation.RESOURCE));
     }
 
     @Override
     protected ModelPathOffsets createModelPathOffsets() {
         ModelPathOffsetsImpl offsets = new ModelPathOffsetsImpl();
         offsets.setResourceOffset("lhc");
         offsets.setRepositoryOffset("lhc/optics/V6.503");
         return offsets;
     }
 
     @Override
     protected String getModelDefinitionName() {
         return LhcUtil.NOMINAL_MODEL_DEFINITION_NAME;
     }
 
     /**
      * @param rangeDefinition
      */
     @Override
     protected void addPostUseFiles(RangeDefinitionImpl rangeDefinition) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     protected List<OpticDefinitionSet> getOpticDefinitionSets() {
         List<OpticDefinitionSet> definitionSetList = new ArrayList<OpticDefinitionSet>();
 
         definitionSetList.add(this.createSqueezeFlatAllIrsOpticsSet());
         definitionSetList.add(this.createPartialSqueeze2011OpticsSet());
         definitionSetList.add(this.createTotemOpticsSet());
         definitionSetList.add(this.createLowBeta2011OpticsSet());
 
         return definitionSetList;
     }
 
     /**
      * create the optic definition set for full squeeze in all IRs + INJ and tuneshift optics
      * 
      * @return
      */
     private OpticDefinitionSet createSqueezeFlatAllIrsOpticsSet() {
 
         OpticDefinitionSetBuilder builder = OpticDefinitionSetBuilder.newInstance();
 
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("V6.5.inj_special.str"));
 
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("ac-dipole.str").isResource());
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("match-chroma.madx").isResource()
                 .doNotParse());
 
         builder.addOptic("A1100C1100A1000L1000_INJ_2011", new OpticModelFileBuilder[0]);
         builder.addOptic("A1100C1100A1000L1000_FLAT_INJ", new OpticModelFileBuilder[0]); /* for compatibility */
 
         /* Tune shift Optic */
         builder.addOptic("A1100C1100A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str") });
 
         /* Full combined Squeeze to 2m b* in all IP's */
         builder.addOptic("A1100C1100A1000_0.00951L1000_0.00951_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00951_beta10.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00951_beta10.00m.str") });
         builder.addOptic("A1100C1100A982_0.00941L1000_0.00951_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00951_beta10.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00941_beta9.82m.str") });
         builder.addOptic("A1100C1100A950_0.00928L950_0.00949_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00949_beta9.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00928_beta9.50m.str") });
         builder.addOptic("A900C900A900_0.00915L900_0.00949_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00949_beta9.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00915_beta9.00m.str") });
         builder.addOptic("A900C900A850_0.00907L850_0.00945_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00945_beta8.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00907_beta8.50m.str") });
         builder.addOptic("A900C900A800_0.00901L800_0.00942_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00942_beta8.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00901_beta8.00m.str") });
         builder.addOptic("A900C900A750_0.00897L750_0.00932_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00932_beta7.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00897_beta7.50m.str") });
         builder.addOptic("A700C700A700_0.00893L700_0.00923_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00923_beta7.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00893_beta7.00m.str") });
         builder.addOptic("A700C700A650_0.00891L650_0.00915_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00915_beta6.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00891_beta6.50m.str") });
         builder.addOptic("A700C700A600_0.00889L600_0.00909_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00909_beta6.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta6.00m.str") });
         builder.addOptic("A700C700A550_0.00889L550_0.00904_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00904_beta5.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.50m.str") });
         builder.addOptic("A500C500A500_0.00889L500_0.00900_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.00m.str") });
         builder.addOptic("A500C500A450_0.00889L450_0.00896_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00896_beta4.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.50m.str") });
         builder.addOptic("A400C400A400_0.00889L400_0.00893_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00893_beta4.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.00m.str") });
         builder.addOptic("A400C400A400_0.00889L375_0.00888_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00888_beta3.75m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.00m.str") });
         builder.addOptic("A350C350A350_0.00889L350_0.00882_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00882_beta3.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A350C350A350_0.00889L325_0.00878_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00878_beta3.25m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A350C350A300_0.00889L300_0.00875_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A250C250A250_0.00889L250_0.00872_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00872_beta2.50m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.50m.str") });
         builder.addOptic("A200C200A200_0.00889L200_0.00872_FLAT", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00872_beta2.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.00m.str") });
 
         return builder.build();
     }
 
     private OpticDefinitionSet createPartialSqueeze2011OpticsSet() {
         /* Partly Squeeze IP1/5 down to 0.55 - IP8 to 5m - IP2 unsqueezed */
         OpticDefinitionSetBuilder builder = OpticDefinitionSetBuilder.newInstance();
 
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("V6.5.inj_special.str"));
 
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("ac-dipole.str").isResource());
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("match-chroma.madx").isResource()
                 .doNotParse());
 
         builder.addOptic("A1100C1100A1000L1000_0.00951_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00951_beta10.00m.str") });
         builder.addOptic("A1100C1100A1000L950_0.00949_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00949_beta9.50m.str") });
         builder.addOptic("A900C900A1000L900_0.00949_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00949_beta9.00m.str") });
         builder.addOptic("A900C900A1000L850_0.00945_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00945_beta8.50m.str") });
         builder.addOptic("A900C900A1000L800_0.00942_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00942_beta8.00m.str") });
         builder.addOptic("A900C900A1000L750_0.00932_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00932_beta7.50m.str") });
         builder.addOptic("A700C700A1000L700_0.00923_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00923_beta7.00m.str") });
         builder.addOptic("A700C700A1000L650_0.00915_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00915_beta6.50m.str") });
         builder.addOptic("A700C700A1000L600_0.00909_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00909_beta6.00m.str") });
         builder.addOptic("A700C700A1000L550_0.00904_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00904_beta5.50m.str") });
         builder.addOptic("A500C500A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A400C400A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A350C350A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A300C300A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A250C250A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A200C200A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A150C150A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A110C110A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.10.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.10.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A80C80A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_0.80.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_0.80.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A65C65A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_0.65.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_0.65.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A55C55A1000L500_0.00900_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_0.55.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_0.55.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str") });
         builder.addOptic("A500C500A1000L450_0.00896_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00896_beta4.50m.str") });
         builder.addOptic("A400C400A1000L400_0.00893_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00893_beta4.00m.str") });
         builder.addOptic("A400C400A1000L375_0.00888_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00888_beta3.75m.str") });
         builder.addOptic("A350C350A1000L350_0.00882_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00882_beta3.50m.str") });
         builder.addOptic("A350C350A1000L325_0.00878_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00878_beta3.25m.str") });
         builder.addOptic("A350C350A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A300C300A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A250C250A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A200C200A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A160C160A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.60.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.60.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A150C150A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.50.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A400C400A1000L350_0.00882_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00882_beta3.50m.str") });
 
         return builder.build();
     }
 
     private OpticDefinitionSet createTotemOpticsSet() {
         OpticDefinitionSetBuilder builder = OpticDefinitionSetBuilder.newInstance();
 
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("V6.5.inj_special.str"));
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("IR5/IP5_beta11.00.str"));
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("IR1/IP1_beta11.00.str"));
 
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("ac-dipole.str").isResource());
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("totem-matching-main-quads.madx")
                 .isResource().doNotParse());
 
         builder.addOptic("A7500C7500A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_75.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_75.str") });
 
         /* combined unsqueeze Optics IR1/5 */
 
         builder.addOptic("A6700C6700A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_67.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_67.str") });
         builder.addOptic("A6000C6000A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_60.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_60.str") });
         builder.addOptic("A5400C5400A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_54.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_54.str") });
         builder.addOptic("A5100C5100A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_51.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_51.str") });
         builder.addOptic("A4600C4600A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_46.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_46.str") });
         builder.addOptic("A4300C4300A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_43.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_43.str") });
         builder.addOptic("A4000C4000A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_40.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_40.str") });
         builder.addOptic("A3600C3600A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_36.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_36.str") });
         builder.addOptic("A3300C3300A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_33.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_33.str") });
         builder.addOptic("A3000C3000A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_30.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_30.str") });
         builder.addOptic("A2500C2500A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_25.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_25.str") });
         builder.addOptic("A2200C2200A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_22.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_22.str") });
         builder.addOptic("A1920C1920A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_19.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_19.str") });
         builder.addOptic("A1670C1670A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_16.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_16.str") });
         builder.addOptic("A1450C1450A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_14.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_14.str") });
         builder.addOptic("A1260C1260A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP1_12.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/un-squeeze-11-to-90m/IP5_12.str") });
         builder.addOptic("A9000C9000A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("HiBeta/IP5_beta90_2010.str"),
                 OpticModelFileBuilder.createInstance("HiBeta/IP1_beta90.str") });
 
         return builder.build();
     }
 
     private OpticDefinitionSet createLowBeta2011OpticsSet() {
 
         // 1) New optics import for beta* below 1.5 in IP1 and IP5.
         // 2) Also added squeeze in IP1 and IP5 only for Ramp&squeeze studies:
         // a) Optics available at the beginning of 2011 for beta* 11m -> 1.5m
         // b) New optics as in (1) for beta* below 1.5m
         //
         // 16/10/2011
         // 3) Added squeeze in IP2 with all other IPs squeeze.
         //
         // 18/01/2012
         // 4) Added a first set of optics for the simultaneous squeeze in all 
         //    IPs, still using the old 2011 sequences. For this first case,
         //    I add only the matched points used in IP8 during 2011.
 
         OpticDefinitionSetBuilder builder = OpticDefinitionSetBuilder.newInstance();
 
         builder.addInitialCommonOpticFile(OpticModelFileBuilder.createInstance("V6.5.inj_special.str"));
 
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("ac-dipole.str").isResource());
         builder.addFinalCommonOpticFile(OpticModelFileBuilder.createInstance("match-chroma.madx").isResource()
                 .doNotParse());
 
         // 1)
         builder.addOptic("A55C55A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.55m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.55m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A60C60A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.60m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.60m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A65C65A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.65m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.65m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A70C70A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.70m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.70m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A80C80A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.80m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.80m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A90C90A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.90m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.90m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A100C100A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A110C110A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.10m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.10m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A120C120A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.20m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.20m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A130C130A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.30m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.30m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
         builder.addOptic("A140C140A1000L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.40m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.40m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str") });
 
         // 2a)
         builder.addOptic("A150C150A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.50.str") });
         builder.addOptic("A160C160A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.60.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.60.str") });
         builder.addOptic("A200C200A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str") });
         builder.addOptic("A250C250A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str") });
         builder.addOptic("A300C300A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.00.str") });
         builder.addOptic("A350C350A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str") });
         builder.addOptic("A400C400A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str") });
         builder.addOptic("A500C500A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str") });
         builder.addOptic("A700C700A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str") });
         builder.addOptic("A900C900A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
                 OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str") });
 
         // 2b)
         builder.addOptic("A55C55A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.55m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.55m.str") });
         builder.addOptic("A60C60A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.60m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.60m.str") });
         builder.addOptic("A65C65A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.65m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.65m.str") });
         builder.addOptic("A70C70A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.70m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.70m.str") });
         builder.addOptic("A80C80A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.80m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.80m.str") });
         builder.addOptic("A90C90A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.90m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.90m.str") });
         builder.addOptic("A100C100A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str") });
         builder.addOptic("A110C110A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.10m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.10m.str") });
         builder.addOptic("A120C120A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.20m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.20m.str") });
         builder.addOptic("A130C130A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.30m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.30m.str") });
         builder.addOptic("A140C140A1000L1000_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.40m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.40m.str") });
 
         // 3)
         builder.addOptic("A100C100A1000_0.00951L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00951_beta10.00m.str") });
         builder.addOptic("A100C100A982_0.00941L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00941_beta9.82m.str") });
         builder.addOptic("A100C100A950_0.00928L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00928_beta9.50m.str") });
         builder.addOptic("A100C100A900_0.00915L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00915_beta9.00m.str") });
         builder.addOptic("A100C100A850_0.00907L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00907_beta8.50m.str") });
         builder.addOptic("A100C100A800_0.00901L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00901_beta8.00m.str") });
         builder.addOptic("A100C100A750_0.00897L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00897_beta7.50m.str") });
         builder.addOptic("A100C100A700_0.00893L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00893_beta7.00m.str") });
         builder.addOptic("A100C100A650_0.00891L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00891_beta6.50m.str") });
         builder.addOptic("A100C100A600_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta6.00m.str") });
         builder.addOptic("A100C100A550_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.50m.str") });
         builder.addOptic("A100C100A500_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.00m.str") });
         builder.addOptic("A100C100A450_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.50m.str") });
         builder.addOptic("A100C100A400_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.00m.str") });
         builder.addOptic("A100C100A350_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A100C100A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A100C100A275_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.75m.str") });
         builder.addOptic("A100C100A250_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.50m.str") });
         builder.addOptic("A100C100A225_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.25m.str") });
         builder.addOptic("A100C100A200_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta2.00m.str") });
         builder.addOptic("A100C100A190_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.90m.str") });
         builder.addOptic("A100C100A180_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.80m.str") });
         builder.addOptic("A100C100A160_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.60m.str") });
         builder.addOptic("A100C100A150_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.50m.str") });
         builder.addOptic("A100C100A140_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.40m.str") });
         builder.addOptic("A100C100A120_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.20m.str") });
         builder.addOptic("A100C100A110_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.10m.str") });
         builder.addOptic("A100C100A100_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta1.00m.str") });
         builder.addOptic("A100C100A090_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta0.90m.str") });
         builder.addOptic("A100C100A080_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta0.80m.str") });
         builder.addOptic("A100C100A070_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta0.70m.str") });
         builder.addOptic("A100C100A060_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta0.60m.str") });
         builder.addOptic("A100C100A050_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
                 OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
                 OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
                 OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta0.50m.str") });
 
         // 4)
         builder.addOptic("A900C900A900_0.00915L900_0.00949_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00949_beta9.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00915_beta9.00m.str") });    
         builder.addOptic("A900C900A750_0.00897L750_0.00932_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
            OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00932_beta7.50m.str"),
            OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00897_beta7.50m.str") });    
         builder.addOptic("A700C700A750_0.00897L750_0.00932_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00932_beta7.50m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00897_beta7.50m.str") });    
         builder.addOptic("A700C700A600_0.00889L600_0.00909_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00909_beta6.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta6.00m.str") });
         builder.addOptic("A400C400A500_0.00889L500_0.00900_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.00m.str") });    
         builder.addOptic("A500C500A500_0.00889L500_0.00900_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_5.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_5.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.00m.str") });    
         builder.addOptic("A400C400A350_0.00889L375_0.00888_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00888_beta3.75m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });    
         builder.addOptic("A350C350A350_0.00889L350_0.00882_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00882_beta3.50m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A350C350A350_0.00889L325_0.00878_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.50.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.50.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00878_beta3.25m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A300C300A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
 
         builder.addOptic("A250C250A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A200C300A200_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A160C160A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.60.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.60.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A150C150A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.50.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.50.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A140C140A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.40m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.40m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A130C130A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.30m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.30m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A120C120A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.20m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.20m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A110C110A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.10m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.10m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
 //        builder.addOptic("A100C100A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
 //            OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_1.00m.str"),
 //            OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_1.00m.str"),
 //            OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
 //            OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A90C90A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.90m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.90m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A80C80A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.80m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.80m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A70C70A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.70m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.70m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A65C65A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.65m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.65m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A60C60A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.60m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.60m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
         builder.addOptic("A55C55A300_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/new_ip1_b2_squeeze/IP1_beta_0.55m.str"),
             OpticModelFileBuilder.createInstance("IR5/new_ip5_b2_squeeze/IP5_beta_0.55m.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.00m.str") });
 
         // Additional iteration on (4) after some thoughts (see my OP logbook, p.119)
         builder.addOptic("A900C900A900_0.00915L750_0.00932_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_9.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_9.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00932_beta7.50m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00915_beta9.00m.str") }); 
         builder.addOptic("A700C700A750_0.00897L600_0.00909_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00909_beta6.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00897_beta7.50m.str") });    
         builder.addOptic("A700C700A600_0.00889L500_0.00900_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_7.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta6.00m.str") });
         builder.addOptic("A300C300A500_0.00889L375_0.00888_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_3.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_3.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00888_beta3.75m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta5.00m.str") });    
         builder.addOptic("A250C250A450_0.00889L350_0.00882_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.50.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.50.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00882_beta3.50m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.50m.str") });
         builder.addOptic("A200C200A400_0.00889L325_0.00878_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_2.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_2.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00878_beta3.25m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta4.00m.str") });
         builder.addOptic("A160C160A350_0.00889L300_0.00875_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_1.60.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_1.60.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00875_beta3.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta3.50m.str") });
         builder.addOptic("A400C400A600_0.00889L500_0.00900_2011", new OpticModelFileBuilder[] {
             OpticModelFileBuilder.createInstance("IR1/IP1_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR5/IP5_beta_4.00.str"),
             OpticModelFileBuilder.createInstance("IR8/3.5TeV/special/ip8_0.00900_beta5.00m.str"),
             OpticModelFileBuilder.createInstance("IR2/3.5TeV/special/ip2_0.00889_beta6.00m.str") });
 
         return builder.build();
 
     }
 }
