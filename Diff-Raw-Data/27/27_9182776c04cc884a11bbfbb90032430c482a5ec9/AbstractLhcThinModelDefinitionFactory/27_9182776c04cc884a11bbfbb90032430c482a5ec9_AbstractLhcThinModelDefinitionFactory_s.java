 package cern.accsoft.steering.jmad.modeldefs.defs.lhc;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import cern.accsoft.steering.jmad.domain.file.CallableModelFileImpl;
 import cern.accsoft.steering.jmad.domain.file.ModelFile;
 import cern.accsoft.steering.jmad.domain.file.ModelPathOffsets;
 import cern.accsoft.steering.jmad.domain.file.ModelPathOffsetsImpl;
 import cern.accsoft.steering.jmad.domain.file.CallableModelFile.ParseType;
 import cern.accsoft.steering.jmad.domain.file.ModelFile.ModelFileLocation;
 import cern.accsoft.steering.jmad.domain.machine.ApertureDefinition;
 import cern.accsoft.steering.jmad.domain.machine.ApertureDefinitionImpl;
 import cern.accsoft.steering.jmad.domain.machine.RangeDefinitionImpl;
 import cern.accsoft.steering.jmad.modeldefs.ModelDefinitionFactory;
 import cern.accsoft.steering.jmad.modeldefs.domain.OpticsDefinition;
 import cern.accsoft.steering.jmad.modeldefs.domain.OpticsDefinitionImpl;
 
 public abstract class AbstractLhcThinModelDefinitionFactory implements ModelDefinitionFactory {
 
     public AbstractLhcThinModelDefinitionFactory() {
         super();
     }
 
     protected ModelPathOffsets createModelPathOffsets() {
         ModelPathOffsetsImpl offsets = new ModelPathOffsetsImpl();
         offsets.setResourceOffset("lhc");
         offsets.setRepositoryOffset("lhc/optics/V6.503");
         return offsets;
     }
 
     protected List<OpticsDefinition> createOpticsDefinitions() {
         List<OpticsDefinition> opticsDefinitions = new ArrayList<OpticsDefinition>();
 
         for (OpticsDef opticsDef : OpticsDef.values()) {
 
             /*
              * first collect the model files
              */
             List<ModelFile> modelFiles = new ArrayList<ModelFile>();
             for (String fileName : opticsDef.getFileNames()) {
                 ParseType parseType = ParseType.STRENGTHS;
                 ModelFileLocation modelFileLocation = ModelFileLocation.REPOSITORY;
                 if (fileName.toLowerCase().endsWith(".madx")) {
                     parseType = ParseType.NONE;
                     modelFileLocation = ModelFileLocation.RESOURCE;
                 }
 
                 ModelFile modelFile = new CallableModelFileImpl(fileName, modelFileLocation, parseType);
                 modelFiles.add(modelFile);
             }
 
             /*
              * XXX quick hack for common resource strength-file
              */
             for (String fileName : OpticsDef.COMMON_FILE_NAMES_RESOURCE) {
                 ModelFile modelFile = new CallableModelFileImpl(fileName, ModelFileLocation.RESOURCE,
                         ParseType.STRENGTHS);
                 modelFiles.add(modelFile);
             }
 
             /*
              * next create the actual definition
              */
 
             OpticsDefinition opticsDefinition = new OpticsDefinitionImpl(opticsDef.toString(),
                     modelFiles.toArray(new ModelFile[modelFiles.size()]));
 
             opticsDefinitions.add(opticsDefinition);
 
         }
 
         return opticsDefinitions;
     }
 
     protected void addPostUseFiles(RangeDefinitionImpl rangeDefinition) {
     }
 
     protected ApertureDefinition createB1ApertureDefinition() {
         ApertureDefinitionImpl aperture = new ApertureDefinitionImpl(new CallableModelFileImpl(
                 "aperture/APERIDX.LHC.B1.tfs", ModelFileLocation.RESOURCE));
 
         aperture.addPartFile(new CallableModelFileImpl("aperture/twiss.ir1_prof.b1.n1_inj.tfs.bz2"));
 
         return aperture;
 
     }
 
     private enum OpticsDef {
     	A080C080L200A200_THIN(new String[] {"V6.5.thin.coll_special.3.5TeV_0.8m2m0.8m2m.str"}),
    	A110C110L200A200_THIN(new String[] {"V6.5.thin.coll_special.3.5TeV_1m2m1m2m.str"}),
     	A200C200L200A200_THIN(new String[] {"V6.5.thin.coll_special.3.5TeV_2m.str"}),
     	A350C350L350A350_THIN(new String[] {"V6.5.thin.coll_special.3.5TeV_3.5m.str"}),
     	A055C055L1000A1000_THIN(new String[] {"V6.5.thin.coll.str"}),
     	A1100C1100L1000A1000_THIN(new String[] {"V6.5.thin.inj.str"}),
     	A055C055L050A010_THIN(new String[] {"V6.5.thin.lowb.str"});
     	
         private final static String[] COMMON_FILE_NAMES_RESOURCE = new String[] { "ac-dipole.str" };
 
         private String[] fileNames;
 
         private OpticsDef(String[] fileNames) {
             this.fileNames = fileNames;
         }
 
         public List<String> getFileNames() {
             List<String> fileNames = new ArrayList<String>(Arrays.asList(this.fileNames));
             return fileNames;
         }
 
         @Override
         public String toString() {
             return this.name().replaceAll("x", ".");
         }
     }
 
 }
