 package nomaoi;
 
 import java.awt.Dimension;
 import javax.sound.midi.*;
 import javax.swing.*;
 
 public class NomaoiController implements Runnable {
     MidiDevice midiIn;
     MidiDevice midiOut;
 
     public NomaoiController() {}
 
     public void setup(int indexMidiIn, int indexMidiOut) throws MidiUnavailableException {
         midiIn = findMidiInDevice(indexMidiIn);
         if (midiIn == null) {
             System.err.println("cannot find midi input device.");
             System.exit(1);
         }
         midiOut = findMidiOutDevice(indexMidiOut);
         Receiver recv = new AsSoonAsPossibleReceiver(midiOut.getReceiver());
         Transmitter trans = midiIn.getTransmitter();
         trans.setReceiver(recv);
         midiIn.open();
         midiOut.open();
     }
 
     private MidiDevice findMidiInDevice(int index) throws MidiUnavailableException {
         MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
         if (0 <= index && index < infos.length) {
             return MidiSystem.getMidiDevice(infos[index]);
         }
         final String klass = "class com.sun.media.sound.MidiInDevice";
         for (int i = 0; i < infos.length; ++ i) {
             MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
             if (klass.equals(dev.getClass().toString())) {
                 return dev;
             }
         }
         return null;
     }
 
     private MidiDevice findMidiOutDevice(int index) throws MidiUnavailableException {
         MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
         if (0 <= index && index < infos.length) {
             return MidiSystem.getMidiDevice(infos[index]);
         }
         final String klass = "class com.sun.media.sound.MidiOutDevice";
         for (int i = 0; i < infos.length; ++ i) {
             MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
             if (dev instanceof Synthesizer
                 || klass.equals(dev.getClass().toString())) {
                 return dev;
             }
         }
         return null;
     }
 
     public void dumpMidiDevices() throws MidiUnavailableException {
         MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
         for (int i = 0; i < infos.length; ++i) {
             System.out.println("device#" + i);
             MidiDevice dev = MidiSystem.getMidiDevice(infos[i]);
             System.out.println(" " + dev.getClass());
             System.out.println(" " + infos[i].getName());
             System.out.println(" " + infos[i].getDescription());
             System.out.println(" " + infos[i].getVendor());
         }
     }
 
     public void createAndShowGui() {
         JFrame frame = createFrame();
         frame.setVisible(true);
     }
 
     private JFrame createFrame() {
        JFrame frame = new JFrame("NomaoiKeyboard");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setContentPane(createPane());
         frame.pack();
         return frame;
     }
 
     private JPanel createPane() {
         JPanel pane = new JPanel();
         pane.setPreferredSize(new Dimension(320, 240));
         return pane;
     }
 
     public void run() {
         createAndShowGui();
     }
 
     public static void main(String[] args) throws Exception {
         NomaoiController app = new NomaoiController();
         app.dumpMidiDevices();
         int indexMidiIn = getDeviceIndexMidiIn(args);
         int indexMidiOut = getDeviceIndexMidiOut(args);
         app.setup(indexMidiIn, indexMidiOut);
         SwingUtilities.invokeLater(app);
     }
 
     private static int getDeviceIndexMidiIn(String[] args) {
         return getOptionArg(args, "-i");
     }
 
     private static int getDeviceIndexMidiOut(String[] args) {
         return getOptionArg(args, "-o");
     }
 
     private static int getOptionArg(String[] args, String opt) {
         for (int i = 0; i < args.length; ++i) {
             if (opt.equals(args[i])) {
                 if (i + 1 >= args.length) {
                     return -1;
                 }
                 try {
                     return Integer.parseInt(args[i + 1]);
                 } catch (NumberFormatException e) {
                     System.out.println("illegal index: " + args[i + 1]);
                     return -1;
                 }
             }
         }
         return -1;
     }
 }
 
 class AsSoonAsPossibleReceiver implements Receiver {
     private Receiver receiver;
 
     public AsSoonAsPossibleReceiver(Receiver realReceiver) {
         receiver = realReceiver;
     }
 
     public void close() {
         if (receiver == null) {
             return;
         }
         receiver.close();
     }
 
     public void send(MidiMessage message, long timeStamp) {
         receiver.send(message, -1);
     }
 }
