 package nl.sense_os.commonsense.client.utility;
 
 import java.util.logging.Logger;
 
 import nl.sense_os.commonsense.client.common.models.DeviceModel;
 import nl.sense_os.commonsense.client.common.models.GroupModel;
 import nl.sense_os.commonsense.client.common.models.SensorModel;
 import nl.sense_os.commonsense.client.common.models.UserModel;
 
 import com.extjs.gxt.ui.client.data.ModelKeyProvider;
 import com.extjs.gxt.ui.client.data.TreeModel;
 
 /**
  * Key provider to help grids and trees distinguish between different sensors.
  */
 public class SenseKeyProvider<M extends TreeModel> implements ModelKeyProvider<M> {
 
    private static final Logger logger = Logger.getLogger("SenseKeyProvider");
 
    @SuppressWarnings("unchecked")
     @Override
    public String getKey(M model) {
         String key = "";
         while (model != null) {
             key += getPartialKey(model);
            model = (M) model.getParent();
         }
         return key;
     }
 
    private String getPartialKey(M model) {
 
         if (model instanceof DeviceModel) {
             DeviceModel device = (DeviceModel) model;
             return "D_" + device.getId() + ": " + device.getUuid() + "; ";
         } else if (model instanceof GroupModel) {
             GroupModel group = (GroupModel) model;
             return "G_" + group.getId() + ": " + group.getName() + "; ";
         } else if (model instanceof SensorModel) {
             SensorModel sensor = (SensorModel) model;
             return "S_" + sensor.getId() + sensor.getName() + sensor.getPhysicalSensor()
                     + sensor.getType() + "; ";
         } else if (model instanceof UserModel) {
             UserModel user = (UserModel) model;
             return "U_" + user.getId() + ": " + user.getName() + "; ";
         } else {
            logger.severe("Unexpected class: " + model);
             return model + "; ";
         }
     }
 }
