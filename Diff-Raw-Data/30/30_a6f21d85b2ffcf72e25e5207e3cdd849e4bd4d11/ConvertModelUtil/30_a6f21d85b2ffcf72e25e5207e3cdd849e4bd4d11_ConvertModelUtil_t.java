 package edu.wustl.cab2b.server.path;
 import gov.nih.nci.cagrid.metadata.MetadataUtils;
 import gov.nih.nci.cagrid.metadata.dataservice.DomainModel;
 import gov.nih.nci.cagrid.metadata.xmi.*;
 import java.util.Properties;
 import edu.wustl.cab2b.common.util.Utility;
 
 
 import java.io.File;
 import java.io.FileWriter;
 
 public class ConvertModelUtil {
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        String xmiFileName =null;
        String projectShortName =null;
       String projectLongName =null;
       String projectDescription =null;
        String projectVersion =null;
        String xmiFileType =null; 
        String xmlFileName =null;
        DomainModel model = null;
 
        if(args.length <4){
     	   throw new IllegalArgumentException("Missing arguments. Only passed"+args.length);
        }
 
        xmiFileName=args[0];
       projectLongName = args[1];
       projectShortName = args[2];
       projectDescription = args[3];
       projectVersion = args[4];
       xmiFileType = args[5];
        
        Properties p = Utility.getPropertiesFromFile("server.properties");
        int maxPathlength = Integer.parseInt(p.getProperty("max.path.length"));
       xmlFileName = p.getProperty("model.dir")+File.separator+projectShortName+projectVersion+".xml";
 
        
 
 
        try{
            File xmiFile = new File(xmiFileName);
            // convert the model
           XMIParser parser = new XMIParser(projectShortName, projectVersion);             
	   parser.setProjectLongName(projectLongName);
	   parser.setProjectDescription(projectDescription);
            
            if(xmiFileType.compareToIgnoreCase("SDK_32_EA") == 0){
                model = parser.parse(xmiFile,XmiFileType.SDK_32_EA);
            }
            else if (xmiFileType.compareToIgnoreCase("SDK_40_EA") == 0){
                model = parser.parse(xmiFile,XmiFileType.SDK_40_EA);
            }
            else if (xmiFileType.compareToIgnoreCase("SDK_40_ARGO") == 0){
                model = parser.parse(xmiFile,XmiFileType.SDK_40_ARGO);
            }
            else{
                throw new IllegalArgumentException("Unknown XMI file format "+ xmiFileType);
            }
             File modelFile = new File(xmlFileName);
            FileWriter writer = new FileWriter(modelFile);
            MetadataUtils.serializeDomainModel(model, writer);
            writer.flush();
            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
 }
 
