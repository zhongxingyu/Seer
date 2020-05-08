 package nl.sense_os.commonsense.client.common.models;
 
 import java.util.List;
 import java.util.Map;
 
 import com.extjs.gxt.ui.client.data.BaseTreeModel;
 import com.extjs.gxt.ui.client.data.TreeModel;
 
 public class SensorModel extends BaseTreeModel {
 
     public static final String DATA_STRUCTURE = "data_structure";
     public static final String DATA_TYPE = "data_type";
     public static final String DATA_TYPE_ID = "data_type_id";
     public static final String PHYSICAL_SENSOR = "device_type";
     public static final String ID = "id";
     public static final String NAME = "name";
     public static final String DISPLAY_NAME = "display_name";
     public static final String TYPE = "type";
     public static final String PAGER_TYPE = "pager_type";
 
     public static final String ALIAS = "alias";
     public static final String USERS = "users";
     public static final String AVAIL_SERVICES = "available_services";
 
     public static final String OWNER = "owner";
     public static final String OWNER_ID = "owner_id";
     public static final String OWNER_MOBILE = "owner_mobile";
     public static final String OWNER_NAME = "owner_name";
     public static final String OWNER_SURNAME = "owner_surname";
     public static final String OWNER_USERNAME = "owner_username";
     public static final String OWNER_EMAIL = "owner_email";
 
     public static final String DEVICE = "device";
     public static final String DEVICE_ID = "device_id";
     public static final String DEVICE_TYPE = "nested_device_type";
     public static final String DEVICE_UUID = "device_uuid";
 
     public static final String ENVIRONMENT = "environment";
     public static final String ENVIRONMENT_ID = "environment_id";
     public static final String ENVIRONMENT_NAME = "environment_name";
     public static final String ENVIRONMENT_FLOORS = "environment_floors";
     public static final String ENVIRONMENT_OUTLINE = "environment_outline";
     public static final String ENVIRONMENT_POSITION = "environment_position";
 
     private static final long serialVersionUID = 1L;
 
     public SensorModel() {
         super();
     }
 
     public SensorModel(Map<String, Object> properties) {
         super(properties);
     }
 
     public SensorModel(TreeModel parent) {
         super(parent);
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof SensorModel) {
             final SensorModel sensor = (SensorModel) obj;

            boolean idEqual = this.getId().equals(sensor.getId());
            boolean ownerEqual = this.getOwner() == null
                    || (this.getOwner().equals(sensor.getOwner()));
            boolean environmentEqual = this.getEnvironment() == null
                    || (this.getEnvironment().equals(sensor.getEnvironment()));
            boolean deviceEqual = this.getDevice() == null
                    || (this.getDevice().equals(sensor.getDevice()));
            boolean parentEqual = this.getParent() == null
                    || (this.getParent().equals(sensor.getParent()));

            return idEqual && ownerEqual && environmentEqual && deviceEqual && parentEqual;
         } else {
             return super.equals(obj);
         }
     }
 
     public String getAlias() {
         return get(ALIAS);
     }
 
     public List<ServiceModel> getAvailServices() {
         return get(AVAIL_SERVICES);
     }
 
     public String getDataStructure() {
         return get(DATA_STRUCTURE);
     }
 
     public String getDataType() {
         return get(DATA_TYPE);
     }
 
     public String getDataTypeId() {
         return get(DATA_TYPE_ID);
     }
 
     public DeviceModel getDevice() {
         return get(DEVICE);
     }
 
     public String getDisplayName() {
         return get(DISPLAY_NAME);
     }
 
     public EnvironmentModel getEnvironment() {
         return get(ENVIRONMENT);
     }
 
     public String getId() {
         return get(ID);
     }
 
     public String getName() {
         return get(NAME);
     }
 
     public UserModel getOwner() {
         return get(OWNER);
     }
 
     public String getPagerType() {
         return get(PAGER_TYPE);
     }
 
     public String getPhysicalSensor() {
         return get(PHYSICAL_SENSOR);
     }
 
     public String getType() {
         return get(TYPE);
     }
 
     public List<UserModel> getUsers() {
         return get(USERS);
     }
 
     @Override
     public String toString() {
         return get(DISPLAY_NAME, "Sensor #" + getId());
     }
 }
