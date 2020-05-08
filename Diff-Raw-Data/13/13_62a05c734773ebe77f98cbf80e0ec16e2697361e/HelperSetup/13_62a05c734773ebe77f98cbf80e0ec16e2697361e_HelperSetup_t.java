 package esir.dom11.nsoc.datactrl.helper;
 
 import esir.dom11.nsoc.datactrl.dao.factory.DAOFactory;
 import esir.dom11.nsoc.model.Command;
 import esir.dom11.nsoc.model.Data;
 import esir.dom11.nsoc.model.DataType;
 import esir.dom11.nsoc.model.Log;
 import esir.dom11.nsoc.model.device.Device;
 import esir.dom11.nsoc.model.device.Sensor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Date;
 
 public abstract class HelperSetup {
 
     /*
     * Class Attributes
     */
 
     private static Logger logger = LoggerFactory.getLogger(HelperSetup.class.getName());
 
     /*
      * Attributes
      */
 
     protected DAOFactory _daoFactory;
 
     /*
      * Constructors
      */
 
     public HelperSetup(DAOFactory daoFactory) {
         _daoFactory = daoFactory;
     }
 
     /*
      * Methods
      */
 
     public abstract void setupTable();
 
     public abstract Database exportDb();
 
     public void setupData() {
 
      /*  // Devices
         Sensor deviceTemp = (Sensor)_daoFactory.getDeviceDAO().create(new Sensor(DataType.TEMPERATURE,"bat7/930"));
         Sensor deviceBrightness = (Sensor)_daoFactory.getDeviceDAO().create(new Sensor(DataType.BRIGHTNESS,"bat7/930"));
         Sensor deviceHumidity = (Sensor)_daoFactory.getDeviceDAO().create(new Sensor(DataType.HUMIDITY,"bat7/930"));
         Sensor devicePower =  (Sensor)_daoFactory.getDeviceDAO().create(new Sensor(DataType.POWER,"bat7/930"));
 
 
         // Datas
         _daoFactory.getDataDAO().create(new Data(deviceTemp,"19.6", new Date(new Long("1326098200720"))));
         _daoFactory.getDataDAO().create(new Data(deviceTemp,"20", new Date(new Long("1326098202743"))));
         _daoFactory.getDataDAO().create(new Data(deviceTemp,"20.3", new Date(new Long("1326098204754"))));
         _daoFactory.getDataDAO().create(new Data(deviceTemp,"19.8", new Date(new Long("1326098206765"))));
         _daoFactory.getDataDAO().create(new Data(deviceTemp,"20.1", new Date(new Long("1326098208787"))));
 
         _daoFactory.getDataDAO().create(new Data(deviceBrightness,"865", new Date(new Long("1326098200720"))));
         _daoFactory.getDataDAO().create(new Data(deviceBrightness,"854", new Date(new Long("1326098202743"))));
         _daoFactory.getDataDAO().create(new Data(deviceBrightness,"821", new Date(new Long("1326098204754"))));
         _daoFactory.getDataDAO().create(new Data(deviceBrightness,"725", new Date(new Long("1326098206765"))));
         _daoFactory.getDataDAO().create(new Data(deviceBrightness,"745", new Date(new Long("1326098208787"))));
 
         _daoFactory.getDataDAO().create(new Data(deviceHumidity,"54", new Date(new Long("1326098200720"))));
         _daoFactory.getDataDAO().create(new Data(deviceHumidity,"58", new Date(new Long("1326098202743"))));
         _daoFactory.getDataDAO().create(new Data(deviceHumidity,"61", new Date(new Long("1326098204754"))));
         _daoFactory.getDataDAO().create(new Data(deviceHumidity,"59", new Date(new Long("1326098206765"))));
         _daoFactory.getDataDAO().create(new Data(deviceHumidity,"60", new Date(new Long("1326098208787"))));
 
         _daoFactory.getDataDAO().create(new Data(devicePower,"437", new Date(new Long("1326098200720"))));
         _daoFactory.getDataDAO().create(new Data(devicePower,"435", new Date(new Long("1326098202743"))));
         _daoFactory.getDataDAO().create(new Data(devicePower,"436", new Date(new Long("1326098204754"))));
         _daoFactory.getDataDAO().create(new Data(devicePower,"498", new Date(new Long("1326098206765"))));
         _daoFactory.getDataDAO().create(new Data(devicePower,"497", new Date(new Long("1326098208787"))));
    */}
 
     public void importDb(Database database) {
         for (Device device : database.getDeviceList()) {
             _daoFactory.getDeviceDAO().create(device);
         }
 
         for (Data data : database.getDataList()) {
             _daoFactory.getDataDAO().create(data);
         }
 
         for (Command command : database.getCommandList()) {
             _daoFactory.getCommandDAO().create(command);
         }
 
         for (Log log : database.getLogList()) {
             _daoFactory.getLogDAO().create(log);
         }
     }
 }
