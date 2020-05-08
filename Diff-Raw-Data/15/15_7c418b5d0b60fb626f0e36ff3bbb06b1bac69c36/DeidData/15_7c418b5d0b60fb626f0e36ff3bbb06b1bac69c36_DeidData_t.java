 package dit;
 
 import java.io.File;
 import java.util.Hashtable;
 import java.util.Vector;
 
 /**
  *
  * @author Christian Prescott
  */
 public class DeidData {
 
     public static Vector<File> 
             inputFiles = new Vector<File>(),
             niftiFiles = new Vector<File>(),
             deidentifiedFiles = new Vector<File>();
     public static Hashtable<File, File> 
             // Nifti result => Dicom source
             NiftiConversionSourceTable = new Hashtable<File, File>(),
             ConvertedDicomHeaderTable = new Hashtable<File, File>();
     public static Hashtable<String, File> 
             unpackedFileLocation = new Hashtable<String, File>();
     // This table will map original filenames to their original IDs. Even
     // if the user chooses not to randomize IDs, it will be filled with the
     // correct key->value pairs. How convenient!
     public static Hashtable<String, String> IdFilename;
     public static int multimatchingFlag = 0;
     public static Vector<Object> errorlog;
     public static int curFlag = 0;
     public static String parentPath = "none";
     public static String anaPath = "/tmp/deid_output/fslchfiletypeOut";
     public static String dicomPath = "tmp/deid_output/dcm2niiOut";
     public static Vector<String> multimatchingNamelist;
     public static Hashtable<String, Integer> multinameSol = new Hashtable();;
     public static Hashtable<String,String> multinameSolFile = new Hashtable();;
     public static Hashtable<String, String> IdTable;
     public static String[] 
             selectedIdentifyingFields, 
             deselectedIdentifyingFields;
     public static DemographicTableModel demographicData;
     public static DemographicTableModel demographicDataforBack;
     public static File deidentifiedDemoFile;
     public static int IdColumn = 0;
     public static Boolean[] includeFileInTar = null;
     public static Boolean[] whetherredoImage =null;
     public static String UserFullName = "", UserInstitution = "";
     public static Object[][] data = new Object[0][0];
     public static int correctflag = 0;
     public static String defaceThreshold = "0.1";
     // Data declarations
     public static String outputPath = "dit_output/";
     public static String tarfilesavedpath = "";
     public static final String[] dicomVarIds = new String[]{
         "(0008,0070)",
         "(0010,1030)",
         "(0018,0020)",
         "(0018,0021)",
         "(0018,0022)",
         "(0018,0023)",
         "(0018,0024)",
         "(0018,0025)",
         "(0018,0050)",
         "(0018,0080)",
         "(0018,0081)",
         "(0018,0082)",
         "(0018,0083)",
         "(0018,0087)",
         "(0018,0089)",
         "(0018,0091)",
         "(0018,0093)",
         "(0018,0094)",
         "(0018,0095)",
         "(0018,1020)",
         "(0018,1251)",
         "(0018,1310)",
         "(0018,1312)",
         "(0018,1314)",
         "(0018,5100)",
         "(0020,0032)",
         "(0020,0037)",
         "(0020,0052)",
         "(0028,0010)",
         "(0028,0011)",};
     public static final String[] dicomVarNames = new String[]{
         "Manufacturer",
         "PatientWeight",
         "ScanningSequence",
         "SequenceVariant",
         "ScanOptions",
         "MrAcquisitionType",
         "SequenceName",
         "AngioFlag",
         "SliceThickness",
         "RepetitionTime",
         "EchoTime",
         "InversionTime",
         "NumberOfAverages",
         "MagneticFieldStrength",
         "PhaseEncodingSteps",
         "EchoTrainLength",
         "PercentSampling",
         "PercentPhaseFov",
         "PixelBandwidth",
         "SoftwareVersion",
         "TransmittingCoil",
         "AcquisitionMatrix",
         "PhaseEncodingDirection",
         "FlipAngle",
         "PatientPosition",
         "ImagePositionPatient",
         "ImageOrientationPatient",
         "FrameOfReferenceUid",
         "ImageRows",
         "ImageColumns",};
     
     public static void addInputFile(Vector<File> files)
     {
         for(int i =0;i<files.size();i++)
         {
             File currentFile=files.get(i);
             if(!isExistInputFile(currentFile))
             {
                 inputFiles.add(currentFile);
             }            
         }
     }
     
     public static void addInputFile(File file)
     {
        if(!isExistInputFile(file))
             {
                 inputFiles.add(file);
             }            
     }
 
     private static boolean isExistInputFile(File file)
     {
         String fileName=file.getAbsolutePath();
         if(fileName.endsWith("nii.gz"))
         {
             fileName=fileName.replace("nii.gz", "");
         }
         else
         {
             fileName=fileName.substring(0,fileName.lastIndexOf("."));
         }
         
         for(int i=0;i<inputFiles.size();i++)
         {
           
            String existFileName=inputFiles.get(i).getAbsolutePath();
            
             if(existFileName.contains(fileName))
            {
                 return true;
            }
         }
         
         return false;
     }
     
 }
