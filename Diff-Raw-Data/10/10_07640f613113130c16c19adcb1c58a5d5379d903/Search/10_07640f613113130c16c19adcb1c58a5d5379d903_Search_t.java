 package edu.cmu.cs.diamond.opendiamond;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.*;
 
 import edu.cmu.cs.diamond.opendiamond.glue.*;
 
 public class Search {
     private static class SessionVariables {
         final private InetAddress address;
 
         final private Map<String, Double> map;
 
         public SessionVariables(InetAddress address, String names[],
                 double values[]) {
             this.address = address;
 
             Map<String, Double> m = new HashMap<String, Double>();
 
             for (int i = 0; i < names.length; i++) {
                 m.put(names[i], values[i]);
             }
 
             map = Collections.unmodifiableMap(m);
         }
 
         public InetAddress getAddress() {
             return address;
         }
 
         public Map<String, Double> getVariables() {
             return map;
         }
 
         @Override
         public String toString() {
             return address.getCanonicalHostName() + ": " + map.toString();
         }
     }
 
     static private Search singleton;
 
     final private SWIGTYPE_p_void handle;
 
     private Searchlet searchlet;
 
     private Scope scope;
 
     volatile private boolean isRunning;
 
     final private SWIGTYPE_p_p_void obj_handle = OpenDiamond
             .create_void_cookie();
 
     private int maxDevices;
 
     final private Set<SearchEventListener> searchEventListeners = new HashSet<SearchEventListener>();
 
     @Override
     protected void finalize() throws Throwable {
         OpenDiamond.delete_void_cookie(obj_handle);
     }
 
     public static Search getSharedInstance() {
         if (singleton == null) {
             singleton = new Search();
         }
 
         return singleton;
     }
 
     private Search() {
         handle = OpenDiamond.ls_init_search();
     }
 
     public void setScope(Scope scope) {
         this.scope = scope;
     }
 
     public void setSearchlet(Searchlet searchlet) {
         this.searchlet = searchlet;
     }
 
     public void start() {
         // set scope
         OpenDiamond.ls_set_searchlist(handle, scope.getGidsSize(), scope
                 .getGids());
 
         maxDevices = computeMaxDevices();
 
         // prepare searchlet
         File filterspec;
         try {
             filterspec = searchlet.createFilterSpecFile();
             File filters[] = searchlet.createFilterFiles();
             OpenDiamond.ls_set_searchlet(handle, device_isa_t.DEV_ISA_IA32,
                     filters[0].getAbsolutePath(), filterspec.getAbsolutePath());
             for (int i = 1; i < filters.length; i++) {
                 OpenDiamond
                         .ls_add_filter_file(handle, device_isa_t.DEV_ISA_IA32,
                                 filters[i].getAbsolutePath());
             }
 
         } catch (IOException e) {
             e.printStackTrace();
             return;
         }
 
         // begin
         OpenDiamond.ls_start_search(handle);
 
         setIsRunning(true);
     }
 
     public void stop() {
         OpenDiamond.ls_terminate_search(handle);
         setIsRunning(false);
     }
 
     private void setIsRunning(boolean running) {
         boolean oldRunning = isRunning;
 
         // XXX make dispatch thread?
         if (oldRunning != running) {
             synchronized (searchEventListeners) {
                 isRunning = running;
                 for (SearchEventListener s : searchEventListeners) {
                     SearchEvent e = new SearchEvent(this);
                     if (isRunning) {
                         s.searchStarted(e);
                     } else {
                         s.searchStopped(e);
                     }
                 }
             }
         }
     }
 
     public Result getNextResult() throws InterruptedException {
         // notice: polling is v. bad, but we have to do it here
 
         while (isRunning) {
             int r = OpenDiamond.ls_next_object(handle, obj_handle,
                     OpenDiamondConstants.LSEARCH_NO_BLOCK);
             switch (r) {
             case 0:
                 return new ObjHandleResult(OpenDiamond
                         .deref_void_cookie(obj_handle));
 
             case OpenDiamondConstants.EWOULDBLOCK:
                 // System.out.println("sleeping on result");
                 Thread.sleep(500);
                 continue;
 
             default:
                 setIsRunning(false);
                 return null;
             }
         }
         return null;
     }
 
     public ServerStatistics[] getStatistics() {
         ServerStatistics noResult[] = new ServerStatistics[0];
 
         List<ServerStatistics> result = new ArrayList<ServerStatistics>();
 
         SWIGTYPE_p_void[] devices = getDevices();
 
         // for each device, get statistics
         for (SWIGTYPE_p_void dev : devices) {
             dev_stats_t dst = null;
             try {
                 int odResult;
 
                 // XXX intensely ugly code, please delete when C API is fixed
 
                 // get size
                 int tmp[] = { OpenDiamond.get_dev_stats_size(32) };
 
                 // allocate
                 dst = OpenDiamond.create_dev_stats(tmp[0]);
 
                 odResult = OpenDiamond.ls_get_dev_stats(handle, dev, dst, tmp);
                 if (odResult != 0) {
                     // expand
                     OpenDiamond.delete_dev_stats(dst);
                     dst = OpenDiamond.create_dev_stats(tmp[0]);
                 }
 
                 // System.out.println(" " + tmp[0]);
 
                 odResult = OpenDiamond.ls_get_dev_stats(handle, dev, dst, tmp);
                 if (odResult != 0) {
                     // System.out.println(" *** bad ls_get_dev_stats");
                     // System.out.println(" " + odResult);
                     // System.out.println(" " + tmp[0]);
                     return noResult;
                 }
 
                 byte data[] = new byte[4];
                 OpenDiamond.get_ipv4addr_from_dev_handle(dev, data);
                 InetAddress a = InetAddress.getByAddress(data);
 
                 ServerStatistics s = new ServerStatistics(a, dst
                         .getDs_objs_total(), dst.getDs_objs_processed(), dst
                         .getDs_objs_dropped());
                 result.add(s);
             } catch (UnknownHostException e) {
                 e.printStackTrace();
             } finally {
                 OpenDiamond.delete_dev_stats(dst);
             }
         }
 
         return result.toArray(noResult);
     }
 
     private int computeMaxDevices() {
         int numDev = 0;
         groupidArray gids = scope.getGids();
         for (int i = 0; i < scope.getGidsSize(); i++) {
             int tmp[] = { 0 };
 
             OpenDiamond.glkup_gid_hosts(gids.getitem(i), tmp, null);
             numDev += tmp[0];
         }
         return numDev;
     }
 
     public boolean isRunning() {
         return isRunning;
     }
 
     private SWIGTYPE_p_void[] getDevices() {
         SWIGTYPE_p_void noResult[] = new SWIGTYPE_p_void[0];
 
         devHandleArray devList = new devHandleArray(maxDevices);
 
         // get device list
         int numDevices[] = { maxDevices };
         if (OpenDiamond.ls_get_dev_list(handle, devList, numDevices) != 0) {
             // System.out.println(" *** bad ls_get_dev_list");
             return noResult;
         }
 
         SWIGTYPE_p_void result[] = new SWIGTYPE_p_void[numDevices[0]];
 
         for (int i = 0; i < result.length; i++) {
             result[i] = devList.getitem(i);
         }
 
         return result;
     }
 
     public Map<String, Double> mergeSessionVariables(
             Map<String, Double> globalValues, DoubleComposer composer) {
         // collect all the session variables
         SessionVariables[] sv = getSessionVariables();
 
         // build new state
         composeVariables(globalValues, composer, sv);
 
         // set it all back
         setSessionVariables(globalValues);
 
         return globalValues;
     }
 
     private void composeVariables(Map<String, Double> globalValues,
             DoubleComposer composer, SessionVariables[] sv) {
 
         System.out.println("INPUT: " + Arrays.toString(sv));
 
        // first, gather all possible keys
        for (SessionVariables v : sv) {
            for (String key : v.getVariables().keySet()) {
                if (!globalValues.containsKey(key)) {
                    globalValues.put(key, 0.0);
                }
            }
        }
        
        // now, compose them
         for (Map.Entry<String, Double> e : globalValues.entrySet()) {
             String key = e.getKey();
 
             for (SessionVariables v : sv) {
                 Map<String, Double> localValues = v.getVariables();
 
                 // compose !
                 double global = e.getValue();
                 double local = localValues.containsKey(key) ? localValues
                         .get(key) : 0.0;
                 double composedValue = composer.compose(key, global, local);
                 globalValues.put(key, composedValue);
             }
         }
 
         System.out.println("OUTPUT: " + globalValues);
     }
 
     private SessionVariables[] getSessionVariables() {
         SessionVariables noResult[] = new SessionVariables[0];
         List<SessionVariables> result = new ArrayList<SessionVariables>();
 
         // get device list
         SWIGTYPE_p_void devices[] = getDevices();
 
         SWIGTYPE_p_p_device_session_vars_t varsHandle = OpenDiamond
                 .create_session_vars_handle();
 
         try {
             // for each device, get variables
             for (SWIGTYPE_p_void dev : devices) {
                 OpenDiamond.ls_get_dev_session_variables(handle, dev,
                         varsHandle);
                 device_session_vars_t vars = OpenDiamond
                         .deref_session_vars_handle(varsHandle);
                 try {
                     SWIGTYPE_p_p_char names = vars.getNames();
                     doubleArray values = vars.getValues();
 
                     int len = vars.getLen();
                     String namesArray[] = new String[len];
                     double valuesArray[] = new double[len];
 
                     for (int i = 0; i < len; i++) {
                         namesArray[i] = OpenDiamond
                                 .get_string_element(names, i);
                         valuesArray[i] = values.getitem(i);
                     }
 
                     byte data[] = new byte[4];
                     OpenDiamond.get_ipv4addr_from_dev_handle(dev, data);
                     InetAddress a = InetAddress.getByAddress(data);
                     SessionVariables sv = new SessionVariables(a, namesArray,
                             valuesArray);
                     result.add(sv);
                 } catch (UnknownHostException e) {
                     e.printStackTrace();
                 } finally {
                     OpenDiamond.delete_session_vars(vars);
                 }
             }
         } finally {
             OpenDiamond.delete_session_vars_handle(varsHandle);
         }
 
         return result.toArray(noResult);
     }
 
     private void setSessionVariables(Map<String, Double> map) {
         device_session_vars_t vars = OpenDiamond
                 .create_session_vars(map.size());
         try {
             SWIGTYPE_p_p_char names = vars.getNames();
             doubleArray values = vars.getValues();
 
             int i = 0;
             for (Map.Entry<String, Double> e : map.entrySet()) {
                 OpenDiamond.set_string_element(names, i, e.getKey());
                 values.setitem(i, e.getValue());
                 i++;
             }
 
             SWIGTYPE_p_void[] devices = getDevices();
             for (SWIGTYPE_p_void dev : devices) {
                 OpenDiamond.ls_set_dev_session_variables(handle, dev, vars);
             }
         } finally {
             OpenDiamond.delete_session_vars(vars);
         }
     }
 
     public void addSearchEventListener(SearchEventListener listener) {
         synchronized (searchEventListeners) {
             searchEventListeners.add(listener);
         }
     }
 
     public void removeSearchEventListener(SearchEventListener listener) {
         synchronized (searchEventListeners) {
             searchEventListeners.remove(listener);
         }
     }
 }
