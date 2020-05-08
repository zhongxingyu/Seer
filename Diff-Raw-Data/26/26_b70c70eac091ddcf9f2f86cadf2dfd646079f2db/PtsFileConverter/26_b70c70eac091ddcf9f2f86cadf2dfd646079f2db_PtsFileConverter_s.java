 package kinect.kinect;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.*;
 
 import kinect.classify.FeatureExtractor;
 import kinect.classify.FeatureExtractor.FeatureType;
 
 import april.util.*;
 
 /** Take in a pts file and output a feature vector file */
 public class PtsFileConverter
 {
     public PtsFileConverter(GetOpt opts)
     {
         FileInputStream fin;
         DataInputStream ins = null;
         FileWriter fout = null;
         try {
         	
         	String inFileString = opts.getString("infile");
         	String[] inFiles = inFileString.split(",");
             fout = new FileWriter(opts.getString("outfile"), false);
         	for(int i = 0; i < inFiles.length; i++){
                 fin = new FileInputStream(inFiles[i]);
                 ins = new DataInputStream(fin);
 
                 FeatureType type;
                 if(opts.getString("type").equals("color")){
                 	type = FeatureType.COLOR;
                 } else if(opts.getString("type").equals("shape")){
                 	type = FeatureType.SHAPE;
                 } else {
                 	type = FeatureType.SIZE;
                 }
                 
                 if (ins != null && fout != null) {
                     convertFile(ins, fout, type);
                 }
                 ins.close();
                 fin.close();
         	}
 
         } catch (Exception ex) {
             System.err.println("ERR: "+ex);
             ex.printStackTrace();
         }
 
     }
 
     private void convertFile(DataInputStream ins, FileWriter fout, FeatureType type)
     {
         BinaryStructureReader bsr = new BinaryStructureReader(ins);
         PrintWriter pwout;
         try {
             pwout = new PrintWriter(fout);
         } catch (Exception ioex) {
             ioex.printStackTrace();
             return;
         }
 
         try {
             while (true) {
                 // Read in data
                 int nlabels = bsr.readInt();
                 bsr.blockBegin();
                 ArrayList<String> labels = new ArrayList<String>();
                 for (int i = 0; i < nlabels; i++) {
                     String label = bsr.readString();
                     labels.add(label);
                 }
                 bsr.blockEnd();
 
                 int npoints = bsr.readInt();
                 ArrayList<double[]> points = new ArrayList<double[]>();
                 bsr.blockBegin();
                 for (int i = 0; i < npoints; i++) {
                     points.add(bsr.readDoubles());
                 }
                 bsr.blockEnd();
                 
                 String featureString = FeatureExtractor.getFeatureString(points, type);
                 
                 Set<String> validFeatures = new HashSet<String>();
                 String[] validFeatureList;
                 switch(type){
                 case COLOR:
                 	validFeatureList = "red,orange,yellow,green,blue,purple".split(",");
                 	break;
                 case SHAPE:
                 	validFeatureList = "arch,rectangle,triangular,rectangular,triangle,square,cylinder,t-shaped,l-shaped,half-cylinder".split(",");
                 	break;
                 case SIZE:
                 	validFeatureList = "small,medium,large".split(",");
                 	break;
                 default:
                 	validFeatureList = new String[]{};
                 }
                 for(int i = 0; i < validFeatureList.length; i++){
                 	validFeatures.add(validFeatureList[i]);
                 }
                 
                 boolean hasLabel = false;
                 for(int i = 0; i < labels.size(); i++){
                	if(validFeatures.contains(labels.get(i))){
                		featureString += String.format(" {%s}\n", labels.get(i));
                 		hasLabel = true;
                 		break;
                 	}
                 }
                 if(hasLabel){
                 	pwout.print(featureString);
                 	pwout.flush();
                }     
             }
         } catch (Exception ex) {
         }
     }
 
     static public void main(String[] args)
     {
         GetOpt opts = new GetOpt();
 
         opts.addBoolean('h', "help", false, "Show this help screen");
         opts.addString('i', "infile", null, "Input .pts file");
         opts.addString('o', "outfile", null, "Output feature vector file");
         opts.addString('t', "type", "shape", "Type of features to extract: {color, shape}");
 
         if (!opts.parse(args)) {
             System.err.println("ERR: "+opts.getReason());
             System.exit(1);
         }
         if (opts.getBoolean("help")) {
             opts.doHelp();
             System.exit(1);
         }
 
         PtsFileConverter pfc = new PtsFileConverter(opts);
     }
 }
