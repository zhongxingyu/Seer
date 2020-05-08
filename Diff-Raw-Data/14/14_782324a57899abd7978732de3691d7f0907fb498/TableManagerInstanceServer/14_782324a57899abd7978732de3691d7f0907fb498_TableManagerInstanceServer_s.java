 package org.h2o.db.manager;
 
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.h2o.db.H2OMarshaller;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.json.JSONArray;
 
 import uk.ac.standrews.cs.nds.rpc.ApplicationServer;
 import uk.ac.standrews.cs.nds.rpc.IHandler;
 import uk.ac.standrews.cs.nds.rpc.JSONValue;
 import uk.ac.standrews.cs.nds.rpc.Marshaller;
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.NetworkUtil;
 
 /**
  * This class maintains a Map of exported Table Managers and provides an RMI mechanism over the underlying JSON RPC mechanism.
  * It assumes that the Table Manager is provided as the zeroth parameter on the wire.
  * This parameter is removed before calling the appropriate method in the appropriate TableManager.
  *
  * @author Alan Dearle (al@cs.st-andrews.ac.uk)
  */
 public class TableManagerInstanceServer extends ApplicationServer {
 
     private static final String DEFAULT_REGISTRY_KEY = null;
 
     private final H2OMarshaller marshaller;
 
     /**
      *  Map from fully qualified table name to table manage instance.
      */
     private final Map<String, TableManagerServer> table_manager_instances;
 
     public TableManagerInstanceServer(final int port) {
 
         super.setPort(port);
         try {
             super.setLocalAddress(NetworkUtil.getLocalIPv4Address());
         }
         catch (final UnknownHostException e) {
             ErrorHandling.hardExceptionError(e, "Couldn't find local IP address.");
         }
 
         marshaller = new H2OMarshaller();
         registry_key = null;
 
         table_manager_instances = new HashMap<String, TableManagerServer>();
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     @Override
     public Marshaller getMarshaller() {
 
         return marshaller;
     }
 
     @Override
     public String getApplicationRegistryKey() {
 
         return registry_key;
     }
 
     // -------------------------------------------------------------------------------------------------------
 
     /**
      * Exposes a ITableManagerRemote so that it can be accessed remotely.
      * @param tm a table manager to be made eternally addressable
      */
     public void exportObject(final ITableManagerRemote tm) {
 
         try {
             table_manager_instances.put(tm.getTableInfo().getFullTableName(), new TableManagerServer(tm));
         }
         catch (final RPCException e) {
             // This should never happen because the object being exported is always local
             // Therefore hard Error.
             ErrorHandling.hardError("RPC error on (what should be) local object");
         }
     }
 
     /**
      * Makes an RMI call on the object identified in by the zeroth parameter from the JSON array of args
      * and on the method whose name is specified in @param method_name.
      */
     @Override
     public IHandler getHandler(final String method_name) {
 
         return new IHandler() {
 
             @Override
             public JSONValue execute(final JSONArray args) throws Exception {
 
                 final String table_name = args.getString(0);
                 args.remove(0); // remove the table name from the parameter list
                 final TableManagerServer object_server = table_manager_instances.get(table_name);
                 return object_server.getHandler(method_name).execute(args);
             }
         };
     }
 }
