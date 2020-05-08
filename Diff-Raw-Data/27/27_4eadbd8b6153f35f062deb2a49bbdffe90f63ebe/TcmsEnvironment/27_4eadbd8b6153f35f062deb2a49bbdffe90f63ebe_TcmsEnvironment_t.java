 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration;
 
 import com.redhat.nitrate.Env;
 import com.redhat.nitrate.Env.Value;
 import com.redhat.nitrate.TcmsConnection;
 import java.util.Hashtable;
 import redstone.xmlrpc.XmlRpcArray;
 import redstone.xmlrpc.XmlRpcFault;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsEnvironment {
 
     public final String env;
     private Integer envId;
     private Env.Group env_obj;
     private Hashtable<String, Env.Property> properties;
     private Hashtable<Integer, Env.Value> values_by_id;
     private Hashtable<String, Hashtable<String, Env.Value>> values;
 
     public TcmsEnvironment(String env) {
         this.env = env;
     }
 
     public String getEnv() {
         return env;
     }
 
     public Integer getEnvId() {
         return envId;
     }
     TcmsConnection connection;
 
     public void setConnection(TcmsConnection connection) {
         this.connection = connection;
     }
 
     public void reloadEnvId() throws XmlRpcFault {
         envId = null;
         Env.filter_groups get = new Env.filter_groups();
         get.name = env;
         Object o = connection.invoke(get);
         if (o instanceof XmlRpcArray) {
             if (((XmlRpcArray) o).size() == 0) {
                 envId = null;
                 return;
             }
             o = ((XmlRpcArray) o).get(0);
         }
         if (o instanceof XmlRpcStruct) {
             env_obj = TcmsConnection.rpcStructToFields((XmlRpcStruct) o, Env.Group.class);
             envId = env_obj.id;
         }
         reloadProperties();
         reloadValues();
     }
 
     private void reloadProperties() throws XmlRpcFault {
         if (env_obj != null) {
             Env.get_properties get = new Env.get_properties();
             get.env_group_id = env_obj.id;
             Object o = connection.invoke(get);
             properties = new Hashtable<String, Env.Property>();
             if (o instanceof XmlRpcArray) {
                 XmlRpcArray rpcArray = (XmlRpcArray) o;
                 for (Object pr : rpcArray) {
                     if (pr instanceof XmlRpcStruct) {
                         XmlRpcStruct pr_str = (XmlRpcStruct) pr;
                         Env.Property pr_obj = TcmsConnection.rpcStructToFields(pr_str, Env.Property.class);
                         properties.put(pr_obj.name, pr_obj);
 
                     }
                 }
             }
 
         }
     }
 
     private void reloadValues() throws XmlRpcFault {
         values = new Hashtable<String, Hashtable<String, Value>>();
         values_by_id = new Hashtable<Integer, Env.Value>();
         for (Env.Property property : properties.values()) {
             Env.get_values get = new Env.get_values();
             get.env_property_id = property.id;
             Object o = connection.invoke(get);
             Hashtable<String, Env.Value> value_set = new Hashtable<String, Env.Value>();
             if (o instanceof XmlRpcArray) {
                 XmlRpcArray rpcArray = (XmlRpcArray) o;
                 for (Object vl : rpcArray) {
                     if (vl instanceof XmlRpcStruct) {
                         XmlRpcStruct vl_str = (XmlRpcStruct) vl;
                         Env.Value vl_obj = TcmsConnection.rpcStructToFields(vl_str, Env.Value.class);
                         value_set.put(vl_obj.value, vl_obj);
                         values_by_id.put(vl_obj.id, vl_obj);
                     }
                 }
             }
             values.put(property.name, value_set);
         }
     }
 
     public Hashtable<String, Hashtable<String, Value>> getValues() {
         return values;
     }
 
     public Env.Value getValueById(Integer i) {
         return values_by_id.get(i);
     }
 
     public boolean containsProperty(String property) {
         if (properties != null) {
             return properties.containsKey(property);
         }
         return false;
     }
 
     public boolean containsValue(String property, String value) {
         if (containsProperty(property) == false) {
             return false;
         }
         return values.get(property).containsKey(value);
     }
    
    
    /*
     * Returns true if environment is not initialized with any value
     */
    public boolean isEmpty(){
        if(env != null) return env.isEmpty();
        return true;
    }
 }
