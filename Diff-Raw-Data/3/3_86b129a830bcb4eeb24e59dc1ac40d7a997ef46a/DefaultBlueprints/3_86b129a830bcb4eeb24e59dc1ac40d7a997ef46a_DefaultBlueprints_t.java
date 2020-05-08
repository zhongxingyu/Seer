 package au.net.netstorm.boost.spider.registry;
 
 import au.net.netstorm.boost.spider.flavour.DefaultInterfaceMap;
 import au.net.netstorm.boost.spider.flavour.InterfaceMap;
 import au.net.netstorm.boost.util.type.DefaultTypeMaster;
 import au.net.netstorm.boost.util.type.Implementation;
 import au.net.netstorm.boost.util.type.Interface;
 import au.net.netstorm.boost.util.type.TypeMaster;
 
 // FIX (Nov 28, 2007) IOC 2215 There feels like a lot of dupe here with DefaultInstances.
 public final class DefaultBlueprints implements Blueprints {
     private final InterfaceMap map = new DefaultInterfaceMap();
    private TypeMaster typer = new DefaultTypeMaster();
 
     public void put(Implementation host, Interface iface, Blueprint blueprint) {
         check(iface, blueprint);
         map.put(iface, blueprint);
     }
 
     public Blueprint get(Interface iface) {
         return (Blueprint) map.get(iface);
     }
 
     public boolean exists(Interface iface) {
         return map.exists(iface);
     }
 
     private void check(Interface iface, Blueprint blueprint) {
         Implementation impl = blueprint.getImplementation();
         if (!typer.implementz(impl, iface)) throw new WrongInterfaceRegistrationException(impl, iface);
     }
 }
