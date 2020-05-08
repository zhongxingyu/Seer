 import ch.ntb.usb.Device;
 import ch.ntb.usb.USB;
 import ch.ntb.usb.USBException;
 import ch.ntb.usb.USBTimeoutException;
 import java.awt.Robot;
 import java.awt.event.KeyEvent;
 import java.io.FileInputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class Main
 {
     private static final int VENDOR_SAITEK = 0x6a3;
     private static final int DEVICE_SWITCH = 0xd67;
     private static final int DEVICE_RADIO = 0xd05;
     private static final int DEVICE_AUTOPILOTS = 0x0000;
     private static final boolean DEBUG_FLAGS = false;
     private static final boolean DEBUG_SWITCHES = false;
    private static final boolean DEBUG_RADIO = true;
    private static final boolean DEBUG_AUTOPILOTS = true;
     
    private static final boolean DEBUG_SWITCHES_DISABLE_SHORTCUTS = true;
    private static final boolean DEBUG_RADIO_DISABLE_SHORTCUTS = true;
     private static final boolean DEBUG_AUTOPILOTS_DISABLE_SHORTCUTS = true;
     
     static Properties properties = new Properties();
     static Map<String, Shortcut> shortcuts = new HashMap<>();
     static Robot robot;
     
     public static void main(String[] args) throws Exception
     {
         robot = new Robot();
 
         System.out.println("CLOSING THIS WINDOW WILL CLOSE THE PROGRAM");
         System.out.println("Starting...");
         
         String name = args.length > 0 ? args[0] : "keys.properties";
         try (FileInputStream in = new FileInputStream(name))
         {
             properties.load(in);
             for (Object o: properties.keySet())
             {
                 String key = o.toString().trim();
                 switch (key)
                 {
                     case "SWITCH_KEY_MS":
                         SWITCH_KEY_MS = Long.parseLong(properties.getProperty(key));
                         break;
                     case "KNOB_DELAY_HOLD_MS":
                         KNOB_DELAY_HOLD_MS = Long.parseLong(properties.getProperty(key));
                         break;
                     default:
                         shortcuts.put(key, new Shortcut(properties.getProperty(key)));
                         break;
                 }
             }
         }
         System.out.println("Shortcuts read ("+shortcuts.size()+").");
         System.out.println();
         
         switchReader.start();
         radioReader.start();
        autopilotsReader.start();
     }
 
     static String toBIN(byte[] b)
     {
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < b.length; i++)
         {
             String s = Integer.toBinaryString(b[i] & 0xff);
             for (int j = 8; j > s.length(); j--)
                 sb.append("0");
             sb.append(Integer.toBinaryString(b[i] & 0xff)).append(" ");
         }
         return sb.toString();
     }
 
     static boolean flagChanged(byte oldV, byte newV, int mask)
     {
         return ((oldV ^ newV) & mask) > 0;
     }
 
     static boolean flagValue(byte v, int mask)
     {
         return (v & mask) > 0;
     }
     
     static final Object SHORTCUT_LOCK = new Object();
     static void applyShortcut(String property, boolean value, long ms)
     {
         String key = property + (value ? ".ON" : ".OFF");
         if (shortcuts.containsKey(key))
         {
             Shortcut shortcut = shortcuts.get(key);
             synchronized(SHORTCUT_LOCK)
             {
                 try
                 {
                     for (int i = 0; i < shortcut.modifiers.length; i++)
                         robot.keyPress(shortcut.modifiers[i]);
                     robot.keyPress(shortcut.keycode);
                     Thread.sleep(ms);
                     robot.keyRelease(shortcut.keycode);
                     for (int i = shortcut.modifiers.length-1; i >= 0; i--)
                         robot.keyRelease(shortcut.modifiers[i]);
                 }
                 catch (InterruptedException e)
                 {
                 }
             }
         }
     }
     
     /**
      * Shortcut property parser
      */
     static class Shortcut
     {
         public Integer[] modifiers;
         public int keycode;
 
         public Shortcut(String propSyntax)
         {
             List<Integer> mods = new ArrayList<>();
             int code = 0;
             String[] tokens = propSyntax.split("\\+");
             for (String t: tokens)
             {
                 t = t.trim();
                 switch (t)
                 {
                     case "ALTGR":
                         mods.add(KeyEvent.VK_ALT_GRAPH);
                         break;
                     case "ALT":
                         mods.add(KeyEvent.VK_ALT);
                         break;
                     case "CTRL":
                         mods.add(KeyEvent.VK_CONTROL);
                         break;
                     case "SHIFT":
                         mods.add(KeyEvent.VK_SHIFT);
                         break;
                     default:
                         code = t.toUpperCase().charAt(0);
                         break;
                 }
             }
             
             keycode = code;
             modifiers = mods.toArray(new Integer[mods.size()]);
         }
     }
     
     // -------------------------------------------------------------------------
     // SWITCH PART
     
     private static long SWITCH_KEY_MS = 300;
     
     enum SwitchKeys
     {
         // Group 0
         MASTER_BAT(0, 0b1, "SWITCHKEY_MASTER_BAT"),
         MASTER_ALT(0, 0b10, "SWITCHKEY_MASTER_ALT"),
         AVIONICS_MASTER(0, 0b100, "SWITCHKEY_AVIONICS_MASTER"),
         FUEL_PUMP(0, 0b1000, "SWITCHKEY_FUEL_PUMP"),
         DE_ICE(0, 0b10000, "SWITCHKEY_DE_ICE"),
         PITOT_HEAT(0, 0b100000, "SWITCHKEY_PITOT_HEAT"),
         CLOSE_COWL(0, 0b1000000, "SWITCHKEY_CLOSE_COWL"),
        LIGHTS_PANEL(0, 0b10000000, "SWITCHKEY_CLOSE_COWL"),
         // Group 1
         LIGHTS_BEACON(1, 0b1, "SWITCHKEY_LIGHTS_BEACON"),
         LIGHTS_NAV(1, 0b10, "SWITCHKEY_LIGHTS_NAV"),
         LIGHTS_STROBE(1, 0b100, "SWITCHKEY_LIGHTS_STROBE"),
         LIGHTS_TAXI(1, 0b1000, "SWITCHKEY_LIGHTS_TAXI"),
         LIGHTS_LANDING(1, 0b10000, "SWITCHKEY_LIGHTS_LANDING"),
         ENGINE_OFF(1, 0b100000, "SWITCHKEY_ENGINE_OFF"),
         ENGINE_RIGHT(1, 0b1000000, "SWITCHKEY_ENGINE_RIGHT"),
         ENGINE_LEFT(1, 0b10000000, "SWITCHKEY_ENGINE_LEFT"),
         // Group 2
         ENGINE_BOTH(2, 0b1, "SWITCHKEY_ENGINE_BOTH"),
         ENGINE_START(2, 0b10, "SWITCHKEY_ENGINE_START"),
         GEAR_UP(2, 0b100, "SWITCHKEY_GEAR_UP"),
         GEAR_DOWN(2, 0b1000, "SWITCHKEY_GEAR_DOWN");
         
         private int group;
         private int mask;
         private String property;
         
         SwitchKeys(int group, int mask, String property)
         {
             this.group = group;
             this.mask = mask;
             this.property = property;
         }
         
         public int getMask()
         {
             return mask;
         }
         
         public int getGroup()
         {
             return group;
         }
         
         public String getProperty()
         {
             return property;
         }
     }
     
     public static void dataChangedSwitches(byte[] oldData, byte[] newData)
     {
         if (oldData == null || newData == null || oldData.length != newData.length || Arrays.equals(oldData, newData))
             return;
 
         for (SwitchKeys key: SwitchKeys.values())
             checkSwitch(oldData[key.getGroup()], newData[key.getGroup()], key.getMask(), key);
     }
 
     static void checkSwitch(byte oldV, byte newV, int mask, SwitchKeys key)
     {
         if (oldV != newV && flagChanged(oldV, newV, mask))
         {
             final boolean value = flagValue(newV, mask);
             if (DEBUG_SWITCHES)
                 System.out.println(key.getProperty() + ": " + (value ? "ON" : "OFF"));
             if (!DEBUG_SWITCHES_DISABLE_SHORTCUTS)
                 applyShortcut(key.getProperty(), value, SWITCH_KEY_MS);
         }
     }
     
     static Thread switchReader = new Thread(new Runnable()
     {
         @Override
         public void run()
         {
             try
             {
                 System.out.println("starting switch panel reader");
                 boolean firstRun = true;
                 Device dev = USB.getDevice((short) VENDOR_SAITEK, (short) DEVICE_SWITCH);
                 try
                 {
                     dev.open(1, 0, -1);
                     //Initialize it to: all OFF, gear DOWN & engine OFF
                     byte[] data = new byte[]    {0b00000000, 0b00100000, 0b00001000, 0b00000000};
                     byte[] oldData = new byte[] {0b00000000, 0b00100000, 0b00001000, 0b00000000};
                     while (!Thread.interrupted())
                     {
                         try
                         {
                             dev.readInterrupt(0x81, data, data.length, -1, false);
                         }
                         catch (USBTimeoutException e)
                         {
                             // Just ignore timeouts
                         }
 
                         if (!Arrays.equals(oldData, data) && !firstRun)
                         {
                             if (DEBUG_FLAGS)
                             {
                                 System.out.println("------------------");
                                 System.out.println("SWITCH OLD: " + toBIN(oldData));
                                 System.out.println("SWITCH NEW: " + toBIN(data));
                                 System.out.println("------------------");
                             }
                             dataChangedSwitches(oldData, data);
                             System.arraycopy(data, 0, oldData, 0, data.length);
                         }
 
                         firstRun = false;
                     }
                 }
                 finally
                 {
                     if (dev.isOpen())
                     {
                         dev.close();
                         dev.reset();
                     }
                 }
             }
             catch (USBException e)
             {
                 throw new RuntimeException(e);
             }
         }
     });
     
     // -------------------------------------------------------------------------
     // RADIO PART
     
     private static long KNOB_DELAY_HOLD_MS = 30;
     
     enum RadioKeys
     {
         KNOB1_SMAPP_RIGHT(2, 0b1, "RADIO_KNOB1_SMALL_RIGHT"),
         KNOB1_SMAPP_LEFT(2, 0b10, "RADIO_KNOB1_SMALL_LEFT"),
         KNOB1_BIG_RIGHT(2, 0b100, "RADIO_KNOB1_BIG_RIGHT"),
         KNOB1_BIG_LEFT(2, 0b10000, "RADIO_KNOB1_BIG_LEFT"),
         KNOB2_SMAPP_RIGHT(2, 0b10000, "RADIO_KNOB2_SMALL_RIGHT"),
         KNOB2_SMAPP_LEFT(2, 0b100000, "RADIO_KNOB2_SMALL_LEFT"),
         KNOB2_BIG_RIGHT(2, 0b1000000, "RADIO_KNOB2_BIG_RIGHT"),
         KNOB2_BIG_LEFT(2, 0b10000000, "RADIO_KNOB2_BIG_LEFT"),
         SWITCH1(1, 0b1000000, "RADIO_SWITCH1"),
         SWITCH2(1, 0b10000000, "RADIO_SWITCH2"),
         SELECTOR1_COM1(0, 0b1, "RADIO_SELECTOR1_COM1"),
         SELECTOR1_COM2(0, 0b10, "RADIO_SELECTOR1_COM2"),
         SELECTOR1_NAV1(0, 0b100, "RADIO_SELECTOR1_NAV1"),
         SELECTOR1_NAV2(0, 0b1000, "RADIO_SELECTOR1_NAV2"),
         SELECTOR1_ADF(0, 0b10000, "RADIO_SELECTOR1_ADF"),
         SELECTOR1_DME(0, 0b100000, "RADIO_SELECTOR1_DME"),
         SELECTOR1_XPDR(0, 0b1000000, "RADIO_SELECTOR1_XPDR"),
         SELECTOR2_COM1(0, 0b10000000, "RADIO_SELECTOR2_COM1"),
         SELECTOR2_COM2(1, 0b1, "RADIO_SELECTOR2_COM2"),
         SELECTOR2_NAV1(1, 0b10, "RADIO_SELECTOR2_NAV1"),
         SELECTOR2_NAV2(1, 0b100, "RADIO_SELECTOR2_NAV2"),
         SELECTOR2_ADF(1, 0b1000, "RADIO_SELECTOR2_ADF"),
         SELECTOR2_DME(1, 0b10000, "RADIO_SELECTOR2_DME"),
         SELECTOR2_XPDR(1, 0b100000, "RADIO_SELECTOR2_XPDR");
         private int group;
         private int mask;
         private String property;
         
         RadioKeys(int group, int mask, String property)
         {
             this.group = group;
             this.mask = mask;
             this.property = property;
         }
 
         public int getGroup()
         {
             return group;
         }
 
         public int getMask()
         {
             return mask;
         }
 
         public String getProperty()
         {
             return property;
         }
     }
     
     public static void dataChangedRadio(byte[] oldData, byte[] newData)
     {
         if (oldData == null || newData == null || oldData.length != newData.length || Arrays.equals(oldData, newData))
             return;
 
         for (RadioKeys key: RadioKeys.values())
             checkRadio(oldData[key.getGroup()], newData[key.getGroup()], key.getMask(), key);
     }
 
     static void checkRadio(byte oldV, byte newV, int mask, RadioKeys key)
     {
         if (oldV != newV && flagChanged(oldV, newV, mask))
         {
             final boolean value = flagValue(newV, mask);
             if (DEBUG_RADIO)
                 System.out.println(key.getProperty() + ": " + (value ? "ON" : "OFF"));
             if (!DEBUG_RADIO_DISABLE_SHORTCUTS)
                 applyShortcut(key.getProperty(), value, KNOB_DELAY_HOLD_MS);
         }
     }
     
     static Thread radioReader = new Thread(new Runnable()
     {
         @Override
         public void run()
         {
             try
             {
                 System.out.println("starting radio panel reader");
                 Device dev = USB.getDevice((short) VENDOR_SAITEK, (short) DEVICE_RADIO);
                 try
                 {
                     // BEWARE, all radio inputs are always doubled (WTF???)
                     dev.open(1, 0, -1);
                     // Start with: SELECTOR1 to COM1 and SELECTOR2 to COM1
                     byte[] data = new byte[]    {(byte)0b10000001, 0b00000000, 0b00000000};
                     byte[] oldData = new byte[] {(byte)0b10000001, 0b00000000, 0b00000000};
                     while (!Thread.interrupted())
                     {
                         try
                         {
                             dev.readInterrupt(0x81, data, data.length, -1, false);
                         }
                         catch (USBTimeoutException e)
                         {
                         }
 
                         if (!Arrays.equals(oldData, data))
                         {
                             if (DEBUG_FLAGS)
                             {
                                 System.out.println("------------------");
                                 System.out.println("RADIO OLD: " + toBIN(oldData));
                                 System.out.println("RADIO NEW: " + toBIN(data));
                                 System.out.println("------------------");
                             }
                             dataChangedRadio(oldData, data);
                             System.arraycopy(data, 0, oldData, 0, data.length);
                         }
                     }
                 }
                 finally
                 {
                     if (dev.isOpen())
                     {
                         dev.close();
                         dev.reset();
                     }
                 }
             }
             catch (USBException e)
             {
                 throw new RuntimeException(e);
             }
         }
     });
     
     
     // -------------------------------------------------------------------------
     // AUTOPILOTS PART
     
     private static long AUTOPILOTS_KEY_MS = 300;
     
     enum AutopilotKeys
     {
         // Group 0 (example)
         EXAMPLE(0, 0b1000, "AUTOPILOT_EXAMPLE");
         
         private int group;
         private int mask;
         private String property;
         
         AutopilotKeys(int group, int mask, String property)
         {
             this.group = group;
             this.mask = mask;
             this.property = property;
         }
         
         public int getMask()
         {
             return mask;
         }
         
         public int getGroup()
         {
             return group;
         }
         
         public String getProperty()
         {
             return property;
         }
     }
     
     public static void dataChangedAutopilot(byte[] oldData, byte[] newData)
     {
         if (oldData == null || newData == null || oldData.length != newData.length || Arrays.equals(oldData, newData))
             return;
 
         for (AutopilotKeys key: AutopilotKeys.values())
             checkAutopilot(oldData[key.getGroup()], newData[key.getGroup()], key.getMask(), key);
     }
 
     static void checkAutopilot(byte oldV, byte newV, int mask, AutopilotKeys key)
     {
         if (oldV != newV && flagChanged(oldV, newV, mask))
         {
             final boolean value = flagValue(newV, mask);
             if (DEBUG_AUTOPILOTS)
                 System.out.println(key.getProperty() + ": " + (value ? "ON" : "OFF"));
             if (!DEBUG_AUTOPILOTS_DISABLE_SHORTCUTS)
                 applyShortcut(key.getProperty(), value, AUTOPILOTS_KEY_MS);
         }
     }
     
     static Thread autopilotsReader = new Thread(new Runnable()
     {
         @Override
         public void run()
         {
             try
             {
                 System.out.println("starting autopilots panel reader");
                 boolean firstRun = true;
                 Device dev = USB.getDevice((short) VENDOR_SAITEK, (short) DEVICE_AUTOPILOTS);
                 try
                 {
                     dev.open(1, 0, -1);
                     //Initialize it to: ???
                     // TODO: don't even know how many bytes there are to read!!!
                     byte[] data = new byte[]    {0b00000000, 0b00100000, 0b00001000, 0b00000000};
                     byte[] oldData = new byte[] {0b00000000, 0b00100000, 0b00001000, 0b00000000};
                     while (!Thread.interrupted())
                     {
                         try
                         {
                             dev.readInterrupt(0x81, data, data.length, -1, false);
                         }
                         catch (USBTimeoutException e)
                         {
                             // Just ignore timeouts
                         }
 
                         if (!Arrays.equals(oldData, data) && !firstRun)
                         {
                             if (DEBUG_FLAGS)
                             {
                                 System.out.println("------------------");
                                 System.out.println("AUTOPILOTS OLD: " + toBIN(oldData));
                                 System.out.println("AUTOPILOTS NEW: " + toBIN(data));
                                 System.out.println("------------------");
                             }
                             dataChangedAutopilot(oldData, data);
                             System.arraycopy(data, 0, oldData, 0, data.length);
                         }
 
                         firstRun = false;
                     }
                 }
                 finally
                 {
                     if (dev.isOpen())
                     {
                         dev.close();
                         dev.reset();
                     }
                 }
             }
             catch (USBException e)
             {
                 throw new RuntimeException(e);
             }
         }
     });
 }
