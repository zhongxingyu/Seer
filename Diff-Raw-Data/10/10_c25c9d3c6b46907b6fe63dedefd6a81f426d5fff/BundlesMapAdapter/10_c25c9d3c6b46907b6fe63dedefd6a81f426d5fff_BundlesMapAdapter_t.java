 package cz.zcu.kiv.bp.unimocker.bindings.adapted;
 
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import javax.xml.bind.annotation.adapters.XmlAdapter;
 
 import cz.zcu.kiv.bp.unimocker.bindings.TBundle;
 import cz.zcu.kiv.bp.unimocker.bindings.TBundleList;
 import cz.zcu.kiv.bp.unimocker.bindings.TSimulatedService;
 
 public class BundlesMapAdapter extends XmlAdapter<TBundleList, BundlesMap>
 {
 
     @Override
     public BundlesMap unmarshal(TBundleList v) throws Exception
     {
         BundlesMap ret = new BundlesMap();
         
         for (TBundle bundle : v.getBundle())
         { 
        	HashMap<String, TSimulatedService> bundleServices = new HashMap<String, TSimulatedService>();
             for (TSimulatedService service : bundle.getService())
             {
                 bundleServices.put(service.getInterface(), service);
             }
 
             String bundleKey = bundle.getSymbolicName() + ":" + bundle.getVersion();   
             ret.put(bundleKey, bundleServices);
         }
         
        for (Entry<String, HashMap<String, TSimulatedService>> entry : ret.entrySet())
        {
        	System.out.println(entry.getKey() + " => " + entry.getValue());
        }
        
         return ret;
     }
 
     @Override
     public TBundleList marshal(BundlesMap v) throws Exception
     {
         TBundleList ret = new TBundleList();
 
         for (Entry<String, HashMap<String, TSimulatedService>> entry : v.entrySet())
         {
             TBundle bndl = new TBundle();
             bndl.setSymbolicName(entry.getKey().split(":")[0]);
             bndl.setVersion(entry.getKey().split(":")[1]);
             bndl.getService().addAll(entry.getValue().values());
             
             ret.getBundle().add(bndl);
         }
         
         return ret;
     }
 
 }
