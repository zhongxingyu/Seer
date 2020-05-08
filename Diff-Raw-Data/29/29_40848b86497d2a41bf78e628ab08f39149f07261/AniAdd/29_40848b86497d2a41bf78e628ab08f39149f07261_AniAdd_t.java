 package aniAdd;
 
 import aniAdd.Modules.IModule;
 import aniAdd.Communication.ComEvent;
 import aniAdd.Communication.ComListener;
 import aniAdd.misc.Misc;
 import aniAdd.misc.Mod_Memory;
 import gui.GUI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.TreeMap;
 import processing.Mod_EpProcessing;
 import udpApi.Mod_UdpApi;
 
 /**
  *
  * @author Arokh
  */
 public class AniAdd implements IAniAdd{
    final static int CURRENTVER = 4;
 
     TreeMap<String, IModule> modules;
     EventHandler eventHandler;
     Mod_Memory mem;
 
     public AniAdd() {
         modules = new TreeMap<String, IModule>();
         eventHandler = new EventHandler();
 
         IModule mod;
         mod = new Mod_Memory();
         modules.put(mod.ModuleName(), mod);
         eventHandler.AddEventHandler(mod);
         mem = (Mod_Memory)mod;
           
         mod = new Mod_EpProcessing();
         modules.put(mod.ModuleName(), mod);
         eventHandler.AddEventHandler(mod);
 
         mod = new Mod_UdpApi();
         modules.put(mod.ModuleName(), mod);
         eventHandler.AddEventHandler(mod);
 
         mod = new GUI();
         modules.put(mod.ModuleName(), mod);
         eventHandler.AddEventHandler(mod);
     }
 
     public void Start(){
         ComFire(new ComEvent(this, ComEvent.eType.Information, IModule.eModState.Initializing));
         mem.put("FirstStart", CURRENTVER);
 
         for (IModule module : modules.values()) {
             System.out.println("Initializing: " + module.ModuleName());
             module.Initialize(this);
         }
 
         boolean allModsInitialized = false;
         while(!allModsInitialized){
             try {Thread.sleep(100);} catch (InterruptedException ex) {}
 
             allModsInitialized = true;
             for (IModule module : modules.values()) { allModsInitialized &= module.ModState() == IModule.eModState.Initialized; }
         }
 
         ComFire(new ComEvent(this, ComEvent.eType.Information, IModule.eModState.Initialized));
     }
     
     public void Stop(){
         ComFire(new ComEvent(this, ComEvent.eType.Information, IModule.eModState.Terminating));
 
         for (IModule module : modules.values()) {
             System.out.println("Terminating: " + module.ModuleName());
             module.Terminate();
         }
 
         boolean allModsTerminated = false;
         while(!allModsTerminated){
             try {Thread.sleep(100);} catch (InterruptedException ex) {}
             
             allModsTerminated = true;
             for (IModule module : modules.values()) { allModsTerminated &= module.ModState() == IModule.eModState.Terminated; }
         }
 
         
         ComFire(new ComEvent(this, ComEvent.eType.Information, IModule.eModState.Terminated));
     }
 
     public IModule GetModule(String modName) { return modules.get(modName); }
 
     public Collection<IModule> GetModules(){return modules.values();}
 
     class EventHandler implements ComListener{
         public void AddEventHandler(IModule mod){ mod.AddComListener(this);}
         public void EventHandler(ComEvent comEvent) {
             System.out.println("Event: " + comEvent.toString());
         }
     }
 
 
     //Com System
 	private ArrayList<ComListener> listeners = new ArrayList<ComListener>();
     protected void ComFire(ComEvent comEvent){
         System.out.println("AniAdd Event: " + comEvent.toString());
         for (ComListener listener : listeners) listener.EventHandler(comEvent);
 
     }
 	public void AddComListener(ComListener comListener){ listeners.add(comListener); }
 	public void RemoveComListener(ComListener comListener){ listeners.remove(comListener); }
     //Com System End
 }
