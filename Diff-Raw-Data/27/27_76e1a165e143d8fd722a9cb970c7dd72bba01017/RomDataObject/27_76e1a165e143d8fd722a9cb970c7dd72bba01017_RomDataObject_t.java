 package com.lemoulinstudio.gfa.nb.filetype.rom;
 
 import com.lemoulinstudio.gfa.core.GfaDevice;
 import com.lemoulinstudio.gfa.nb.screen.ScreenTopComponent;
 import com.lemoulinstudio.gfa.nb.screen.ScreenTopComponentFactory;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.openide.cookies.OpenCookie;
 import org.openide.filesystems.FileObject;
 import org.openide.loaders.DataObjectExistsException;
 import org.openide.loaders.MultiDataObject;
 import org.openide.nodes.CookieSet;
 import org.openide.nodes.Node;
 import org.openide.util.Exceptions;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.Lookups;
 import org.openide.windows.TopComponent;
 
 public class RomDataObject extends MultiDataObject {
 
   private class OpenSupport implements OpenCookie {
     public void open() {
       TopComponent tc = getTopComponent();
       tc.open();
       tc.requestActive();
     }
   }
 
   public class Resetable implements Node.Cookie {
     public void reset() {
       if (getGfaDeviceState() == GfaDeviceState.Running)
         stoppable.stop();
 
       setGfaDeviceState(GfaDeviceState.Undefined);
       getGfaDevice().reset();
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public class Runnable implements Node.Cookie {
     public void run() {
       cpuThread = new Thread(getGfaDevice().getCpu());
       cpuThread.start();
       setGfaDeviceState(GfaDeviceState.Running);
     }
   }
 
   public class Steppable implements Node.Cookie {
     public void step() {
       getGfaDevice().getCpu().step();
       setGfaDeviceState(GfaDeviceState.Undefined);
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public class Stoppable implements Node.Cookie {
     public void stop() {
       getGfaDevice().getCpu().stopPlease();
       try {
         cpuThread.join();
         cpuThread = null;
       }
       catch (InterruptedException ex) {Exceptions.printStackTrace(ex);}
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
   }
 
   public class StoppedState implements Node.Cookie {
     public RomDataObject getRomDataObject() {
       return RomDataObject.this;
     }
   }
   
   public enum GfaDeviceState {
     Undefined,
     Stopped,
     Running
   }
 
   private Resetable resetable = new Resetable();
   private Runnable runnable = new Runnable();
   private Steppable steppable = new Steppable();
   private Stoppable stoppable = new Stoppable();
   private StoppedState stoppedState = new StoppedState();
 
   private GfaDeviceState gfaDeviceState = GfaDeviceState.Undefined;
   private Thread cpuThread = null;
   private Map<GfaDeviceState, List<Node.Cookie>> stateToCookies =
           new HashMap<GfaDeviceState, List<Node.Cookie>>();
 
   private ScreenTopComponent screenTopComponent;
 
   public RomDataObject(FileObject pf, RomDataLoader loader) throws DataObjectExistsException, IOException {
     super(pf, loader);
 
     CookieSet cookies = getCookieSet();
     cookies.add(new OpenSupport());
 
     stateToCookies.put(GfaDeviceState.Undefined, Collections.<Node.Cookie>emptyList());
     stateToCookies.put(GfaDeviceState.Stopped, Arrays.<Node.Cookie>asList(stoppedState, resetable, runnable, steppable));
     stateToCookies.put(GfaDeviceState.Running, Arrays.<Node.Cookie>asList(resetable, stoppable));
   }
 
   @Override
   protected Node createNodeDelegate() {
     return new RomDataNode(this, getLookup());
   }
 
   @Override
   public Lookup getLookup() {
     return getCookieSet().getLookup();
   }
 
   private TopComponent getTopComponent() {
     if (screenTopComponent == null) {
       // Todo: Document this hook.
       ScreenTopComponentFactory factory =
               Lookups.forPath("Gfa/ScreenTopComponentFactory")
               .lookup(ScreenTopComponentFactory.class);
       screenTopComponent = factory.createScreenTopComponent(RomDataObject.this);
     }
 
     return screenTopComponent;
   }
 
   public GfaDeviceState getGfaDeviceState() {
     return gfaDeviceState;
   }
 
   public synchronized void setGfaDeviceState(GfaDeviceState gfaDeviceState) {
     List<Node.Cookie> cookiesBefore = stateToCookies.get(this.gfaDeviceState);
     List<Node.Cookie> cookiesAfter = stateToCookies.get(gfaDeviceState);
 
     CookieSet cookieSet = getCookieSet();
 
     for (Node.Cookie cookie : cookiesBefore)
       if (!cookiesAfter.contains(cookie))
         cookieSet.remove(cookie);
     
     this.gfaDeviceState = gfaDeviceState;
 
     for (Node.Cookie cookie : cookiesAfter)
       if (!cookiesBefore.contains(cookie))
         cookieSet.add(cookie);
   }
 
   private GfaDevice gfaDevice;
 
   public synchronized GfaDevice getGfaDevice() {
     if (gfaDevice == null) {
       // Create the device.
       gfaDevice = new GfaDevice();
 
       // Load the bios.
       gfaDevice.getMemory().loadBios("roms/bios.gba");
 
       // Load the rom.
       try {gfaDevice.getMemory().loadRom(getPrimaryFile().getInputStream());}
       catch (FileNotFoundException e) {}
      catch (OutOfMemoryError oome) {} // tmp fix
 
       // Set the state of the device.
       setGfaDeviceState(GfaDeviceState.Stopped);
     }
     
     return gfaDevice;
   }
 
   public synchronized void releaseResources() {
     // Stop the device if needed.
     if (getGfaDeviceState() == GfaDeviceState.Running)
       stoppable.stop();
 
     // Set state to undefined, so that no unwanted cookies are left in the lookup.
     setGfaDeviceState(GfaDeviceState.Undefined);
 
     // Release the reference to the device.
     gfaDevice = null;
 
     // Release the reference to the top component.
     screenTopComponent = null;
   }
 
 }
