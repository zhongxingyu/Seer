 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package littlesmarttool2.model.oldData;
 
 import java.util.*;
 import littlesmarttool2.model.*;
 import littlesmarttool2.util.JSON;
 
 /**
  *
  * @author marcher89
  */
 public class Converter {
     
 
     public static void main(String[] args){
         OldIRCommand[] oldIRs = JSON.readObjectFromFile("oldData/functions-list.json", OldIRCommand[].class);
         OldCameraBrand[] oldBrands = JSON.readObjectFromFile("oldData/camera-list.json", OldCameraBrand[].class);
         
         HashMap<String, ArrayList<CameraModel>> map = new HashMap<>();
         
         
         CameraBrand[] brands = new CameraBrand[oldBrands.length];
         int nBrand = 0;
         for (OldCameraBrand cameraBrand : oldBrands) {
             System.out.println(cameraBrand.getBrandName());
             
             CameraModel[] models = new CameraModel[cameraBrand.getProfiles().length];
             int nModel = 0;
             for (OldCameraProfile cameraProfile : cameraBrand.getProfiles()) {
                 System.out.println("\t"+cameraProfile.getModels());
                 CameraModel tempModel = new CameraModel(cameraBrand.getBrandName()+" - "+cameraProfile.getModels(), cameraProfile.getModels(), new ConnectionType[]{ConnectionType.IR, ConnectionType.LANC, ConnectionType.Wire});
                 for (OldCameraFunction cameraFunction : cameraProfile.getFunctions()) {
                     System.out.println("\t\t"+cameraFunction.getId());
                     if(!map.containsKey(cameraFunction.getId()))
                         map.put(cameraFunction.getId(), new ArrayList<CameraModel>());
                     map.get(cameraFunction.getId()).add(tempModel);
                 }
                 models[nModel++] = tempModel;
             }
             brands[nBrand++] = new CameraBrand(cameraBrand.getBrandName(), models);
             System.out.println("---------------");
         }
         
         
         //IR commands / functions
         IRCommand[] commands = new IRCommand[oldIRs.length];
         int nIR = 0;
         for (OldIRCommand ir : oldIRs) {
             System.out.println(ir.id);
             ArrayList<CameraModel> modelList = map.get(ir.id);
             CameraModel[] models = new CameraModel[modelList.size()];
             int i = 0;
             for (CameraModel model : modelList) {
                 models[i++]=model;
             }
             
             if(ir.highdurations.length != ir.lowdurations.length){
                 System.out.println("\t Can't use this function, there are not equal number of high and low durations.");
                 continue;
             }
             
             int[] pulsedata = new int[ir.highdurations.length+ir.lowdurations.length];
             
             i=0;
             for (int high : ir.highdurations) {
                 pulsedata[i*2] = high;
                 i++;
             }
             i=0;
             for (int low : ir.lowdurations) {
                 pulsedata[i*2+1] = low;
                 i++;
             }
             
            commands[nIR++] = new IRCommand(ir.action, ir.action, models, pulsedata, 0, ir.signalrepeat, 38);
         }
         
         
         JSON.writeObjectToFile(brands, "cameraList-output.json");
         JSON.writeObjectToFile(commands, "IRCommandList-output.json");
     }
 }
