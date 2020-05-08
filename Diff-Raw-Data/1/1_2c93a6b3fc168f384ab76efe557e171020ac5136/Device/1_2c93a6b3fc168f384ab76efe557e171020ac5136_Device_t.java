 package com.intelix.digihdmi.model;
 
 import com.intelix.net.*;
 import com.intelix.net.payload.*;
 import com.thoughtworks.xstream.annotations.XStreamOmitField;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.URL;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A Device has a set of inputs and outputs as well as an active connection matrix.
  * Inputs and outputs are 1-based on the device itself! The model assumes a zero-based
  * system, and does the translation to the 1-based system internally. That is, everything
  * working with the device should assume that things are one-based; for example,
  * setting the selected output to 1 if the first output is selected. It would never
  * be set to 0.
  *
  * @author Michael Caron <michael.r.caron@gmail.com>
  */
 public class Device implements PropertyChangeListener {
 
     @XStreamOmitField
     private Logger logger;
 
     @XStreamOmitField
     PropertyChangeSupport pcsupport = new PropertyChangeSupport(this);
 
     @XStreamOmitField
     private int selectedOutput;
 
     @XStreamOmitField
     private int selectedInput;
     private ArrayList<Connector> inputs = new ArrayList();
     private ArrayList<Connector> outputs = new ArrayList();
     private ArrayList<Preset> presets = new ArrayList();
     private HashMap<Integer, Integer> cxnMatrix = new HashMap();  /* KEY=Output,VALUE=Input */
 
     @XStreamOmitField
     private static Properties config;
 
     @XStreamOmitField
     private boolean connected;
     private IPConnection connection;
     private int numInputs = 0;
     private int numOutputs = 0;
     private int numPresets = 0;
 
     @XStreamOmitField
     private int maxPresetNameLength = 0;
 
     @XStreamOmitField
     private int maxIONameLength = 0;
 
     @XStreamOmitField
     private int maxPassLength = 0;
     // PropertyChangeListeners will get reports about this one
 
     @XStreamOmitField
     private float progress = 0f;
     private static int MAX_TRIES = 3;
 
     @XStreamOmitField
     private boolean locked = false;
     private String unlockPassword = "abcd";
     private String adminPassword = "abcd";
     // flag used to determine if we need to visit the device for input/output information
     @XStreamOmitField
     private boolean resetInput = true;
 
     @XStreamOmitField
     private boolean resetOutput = true;
 
     @XStreamOmitField
     private boolean resetPresets = true;
 
     @XStreamOmitField
     private String activeAdminPassword = "";
 
     @XStreamOmitField
     private boolean resetXP = true;
 
     @XStreamOmitField
     private boolean adminUnlocked;
 
     @XStreamOmitField
     private static URL propertiesFile;
 
     // DEBUG PROPERTIES
    @XStreamOmitField
     private static int DELAY = 0;
 
     //------------------------------------------------------------------------
     private static Properties getConfiguration() {
         if (config == null) {
             //config = PropertyResourceBundle.getBundle("Device");
             propertiesFile = ClassLoader.getSystemResource("Device.properties");
             config = new Properties();
             try {
                 config.load(new FileInputStream(propertiesFile.getFile()));
             } catch (Exception ex) {
                 Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
                 config = null;
             }
         }
         return config;
     }
 
     private static void saveConfiguration() {
         if (config != null)
         {
             try {
                 OutputStream out = new FileOutputStream(propertiesFile.getFile());
                 config.store(out, "---Routine Save---");
                 out.close();
             } catch (Exception ex) {
                 Logger.getLogger(Device.class.getName()).log(Level.SEVERE,
                         "Error while saving Device properties file", ex);
             }
         }
     }
 
     //------------------------------------------------------------------------
     /* Initialize the Device */
     public Device() {
         logger = Logger.getLogger(getClass().getCanonicalName());
 
         connected = false;
         connection = new IPConnection();
 
         try {
             String delay = getConfiguration().getProperty("delay");
             DELAY = Integer.parseInt(delay);
         } catch (Exception e) {
             // IGNORE - We'll just have a 0 delay then, and a 4 length password
         }
 
         try {
             maxPresetNameLength = Integer.parseInt(
                     getConfiguration().getProperty("MAX_PRESET_NAME_LENGTH"));
             maxPassLength = Integer.parseInt(
                     getConfiguration().getProperty("MAX_PASS_LENGTH"));
             maxIONameLength = Integer.parseInt(
                     getConfiguration().getProperty("MAX_IO_NAME_LENGTH"));
         } catch (Exception e) {
             // IGNORE
         }
 
         try {
             connection.setIpAddr(getConfiguration().getProperty("ipAddr"));
             connection.setPort(Integer.parseInt(getConfiguration().getProperty("port")));
             numInputs = Integer.parseInt(getConfiguration().getProperty("MAX_INPUTS"));
             numOutputs = Integer.parseInt(getConfiguration().getProperty("MAX_OUTPUTS"));
             numPresets = Integer.parseInt(getConfiguration().getProperty("MAX_PRESETS"));
 
         } catch (NullPointerException ex) {
             connection = null;
         } catch (MissingResourceException ex) {
             connection = null;
         }
 
         inputs = new ArrayList();
         outputs = new ArrayList();
 
         // MRC - This initialization assumes that # inputs == # outputs
         for (int i = 0; i < numInputs; ++i) {
 
             Input inpt = new Input("I_" + (i + 1), "", i + 1);
             inpt.addPropertyChangeListener(this);
 
             Output otpt = new Output("O_" + (i + 1), "", i + 1);
             otpt.addPropertyChangeListener(this);
 
             inputs.add(inpt);            // Connectors are 1-based for their index
             outputs.add(otpt);
             cxnMatrix.put(i, 0);
         }
 
         for (int i = 0; i < numPresets; i++) {
             Preset p = new Preset("PS_" + (i + 1), i + 1);
             for (int j = 0; j < numInputs; ++j) {
                 p.makeConnection((j * i) % numInputs, (j + i) % numInputs);
             }
             presets.add(p);
         }
     }
 
     //------------------------------------------------------------------------
     @Override
     protected void finalize() throws Throwable {
         /* Kill the connection on finailzation in the case that it's still up. */
         super.finalize();
         if (connected) {
             disconnect();
         }
     }
 
     //------------------------------------------------------------------------
     /* connect to the actual HDMI devine if we can. */
     public void connect()
             throws IOException {
         if (connection != null && !connection.isConnected()) {
             connection.connect();
 
             boolean connectedNew = connection.isConnected();
             pcsupport.firePropertyChange("connected", connected, connectedNew);
             connected = connectedNew;
             
             setFullReset(true);
         } else {
             throw new IOException("Device can't be found. Check your device's configuration.");
         }
     }
 
     //------------------------------------------------------------------------
     /* Disconnect from the device */
     public void disconnect() throws IOException {
         if (connection != null && connection.isConnected()) {
 
             if (adminUnlocked)
                 lockAdmin();
 
             connection.disconnect();
 
             boolean connectedNew = connection.isConnected();
             pcsupport.firePropertyChange("connected", connected, connectedNew);
             connected = connectedNew;
 
         }
     }
 
     //------------------------------------------------------------------------
     public boolean isConnected() {
         return connection.isConnected();
     }
 
     //------------------------------------------------------------------------
     public Connector getInputForSelectedOutput() {
         return getInputForSelectedOutput(true);
     }
 
     public Connector getInputForSelectedOutput(boolean live) {
         if (connected && live) {
             try {
                 Command c = new GetCrosspointCommand(selectedOutput + 1);
                 if (deviceWriteRead(c, PairSequencePayload.class)) {
                     PairSequencePayload p = (PairSequencePayload) c.getPayload();
                     selectedInput = p.get(selectedOutput + 1) - 1;
                     cxnMatrix.put(selectedOutput, selectedInput);
                 }
             } catch (Exception ex) {
                 logger.log(Level.SEVERE, null, ex);
             }
         }
         return (Connector) inputs.get(cxnMatrix.get(selectedOutput));
     }
 
     //------------------------------------------------------------------------
     public boolean makeConnection() {
         if (connected) {
             logger.info("input: " + selectedInput + ", output: " + selectedOutput);
             if ((selectedInput < 0) || (selectedOutput < 0)) {
                 return false;
             }
             Command c = new SetCrosspointCommand(selectedInput + 1, selectedOutput + 1);
             if (deviceWriteRead(c, PairSequencePayload.class, 1500)) {
                 PairSequencePayload p = (PairSequencePayload) c.getPayload();
                 selectedInput = p.get(selectedOutput + 1) - 1;
             }
         }
         cxnMatrix.put(selectedOutput, selectedInput);
         return true;
     }
 
     //------------------------------------------------------------------------
     public Enumeration<Preset> getPresets() {
         return new Enumeration() {
 
             int index = 0;
 
             @Override
             public boolean hasMoreElements() {
                 if (connected && resetPresets) {
                     boolean r = index < numPresets;
                     if (!r) {
                         resetPresets = false;
                     }
                     return r;
                 }
                 return index < presets.size();
             }
 
             @Override
             public Preset nextElement() {
                 if (connected && resetPresets) {
                     Command c = new GetPresetNameCommand(index + 1);
                     if (deviceWriteRead(c, IdNamePayload.class)) {
                         IdNamePayload p = (IdNamePayload) c.getPayload();
                         String name = p.getStrData();
                         presets.set(index, new Preset(name, index + 1));
                     }
                 }
                 if (DELAY > 0) {
                     try {
                         Thread.sleep(DELAY);
                     } catch (InterruptedException ex) {
                         logger.log(Level.SEVERE, null, ex);
                     }
                 }
                 setProgress((float) index / presets.size());
                 return (Preset) presets.get(index++);
             }
         };
     }
 
     //------------------------------------------------------------------------
     public Enumeration<Connector> getInputs() {
         return getInputs(false);
     }
 
     public Enumeration<Connector> getInputs(boolean live) {
         return new ConnectorEnumeration(inputs, live) {
 
             @Override
             public boolean isReset() {
                 return resetInput;
             }
 
             @Override
             public void setReset(boolean r) {
                 resetInput = r;
             }
 
             @Override
             public Command getNameLookupCommand(int index) {
                 return new GetInputNameCommand(index);
             }
 
             @Override
             public int getMax() {
                 return numInputs;
             }
 
             @Override
             protected Connector makeNewConnector(String name, String string, int i) {
                 return new Input(name, string, i);
             }
         };
     }
 
     //------------------------------------------------------------------------
     public Enumeration<Connector> getOutputs() {
         return getOutputs(false);
     }
 
     public Enumeration<Connector> getOutputs(boolean live) {
         return new ConnectorEnumeration(outputs, live) {
 
             @Override
             public boolean isReset() {
                 return resetOutput;
             }
 
             @Override
             public void setReset(boolean r) {
                 resetOutput = r;
             }
 
             @Override
             public Command getNameLookupCommand(int index) {
                 return new GetOutputNameCommand(index);
             }
 
             @Override
             public int getMax() {
                 return numOutputs;
             }
 
             @Override
             protected Connector makeNewConnector(String name, String string, int i) {
                 return new Output(name, string, i);
             }
         };
     }
 
     //------------------------------------------------------------------------
     public HashMap<Integer, Integer> getCrossPoints() {
         return getCrossPoints(true);
     }
 
     public HashMap<Integer, Integer> getCrossPoints(boolean live) {
         if (connected && (resetXP || live)) {
             Command c = new GetAllCrosspointsCommand();
             if (deviceWriteRead(c, SequencePayload.class)) {
                 SequencePayload p = (SequencePayload) c.getPayload();
                 for (int i = 0; i < p.size(); i++) {
                     cxnMatrix.put(i/*Output*/, p.get(i) - 1/*Input*/);
                 }
                 resetXP = false;
             }
         }
         return cxnMatrix;
     }
 
     //------------------------------------------------------------------------
     public void setFullReset(boolean reset) {
         resetInput = resetOutput = resetPresets = resetXP = reset;
     }
     //------------------------------------------------------------------------
 
     public void setSelectedOutput(int selectedOutput) {
         this.selectedOutput = selectedOutput;
     }
 
     //------------------------------------------------------------------------
     public Connector getSelectedOutput() {
         try {
             return (Connector) outputs.get(selectedOutput);
         } catch (ArrayIndexOutOfBoundsException ex) {
             return null;
         }
     }
 
     //------------------------------------------------------------------------
     public void setSelectedInput(int selectedInput) {
         this.selectedInput = selectedInput;
     }
 
     //------------------------------------------------------------------------
     public Connector getSelectedInput() {
         try {
             return (Connector) inputs.get(selectedInput);
         } catch (ArrayIndexOutOfBoundsException ex) {
             return null;
         }
 
     }
 
     //------------------------------------------------------------------------
     public void loadPreset(int number) {
         // get the preset from the array
         Preset preset = presets.get(number);
 
         if (connected) {
             Command cmd = new TriggerPresetCommand(preset.getIndex());
             deviceWriteAndSkip(cmd);
 
             cmd = new GetAllCrosspointsCommand();
             if (deviceWriteRead(cmd, SequencePayload.class,2000)) {
                 SequencePayload p = (SequencePayload) cmd.getPayload();
                 for (int i = 0; i < p.size(); i++) {
                     cxnMatrix.put(i/*Output*/, p.get(i) - 1/*Input*/);
                 }
                 resetXP = false;
             }
 
             //if (deviceWriteRead(cmd, PresetReportPayload.class))
             //    preset = readPresetReport(preset.getName(),
             //                              preset.getIndex(),
             //                              (PresetReportPayload) cmd.getPayload());
         }
 
         // set the connection matrix from this
         cxnMatrix = preset.getConnections();
     }
 
     //------------------------------------------------------------------------
     public void savePreset(int number, String name) {
         // Get current preset
         Preset newPreset = new Preset(name, number+1);
 
         // Setup the set preset command while filling up new preset
         SetPresetCommand saveCmd = new SetPresetCommand(number+1);
         for (int i = 0; i < cxnMatrix.size(); i++) {
             // add the input to the payload in the correct slot
             SequencePayload p = (SequencePayload) saveCmd.getPayload();
             p.add(cxnMatrix.get(i) + 1);
 
             // add the input and output to the new internal Preset
             newPreset.makeConnection(cxnMatrix.get(i), i);
         }
 
         if (connected) {
             setProgress(1f / 3);
             if (deviceWriteRead(saveCmd, PresetReportPayload.class, 2000)) {
                 newPreset = readPresetReport(name, number+1, (PresetReportPayload) saveCmd.getPayload());
             }
             setProgress(2f / 3);
             SetPresetNameCommand nameCmd = new SetPresetNameCommand(number+1, name);
             if (deviceWriteRead(nameCmd, IdNamePayload.class))
                 newPreset.setName( ((IdNamePayload)nameCmd.getPayload()).getStrData() );
             setProgress(3f / 3);
         }
 
         presets.set(number, newPreset);
         resetPresets = true;
     }
 
     //------------------------------------------------------------------------
     private Preset readPresetReport(String name, int number, PresetReportPayload pld) {
         Preset p = new Preset(name, number);
         for (int i = 1; i <= numOutputs; i++) {
             p.makeConnection(pld.getInputForOutput(i), i);
         }
         return p;
     }
 
     //------------------------------------------------------------------------
     public int getNumOutputs() {
         return numOutputs;
     }
 
     public void setNumOutputs(int no) {
         numOutputs = no;
         resetOutput = true;
     }
 
     public int getNumInputs() {
         return numInputs;
     }
 
     public void setNumInputs(int ni) {
         numInputs = ni;
         resetInput = true;
     }
 
     public int getNumPresets() {
         return numPresets;
     }
 
     public void setNumPresets(int np) {
         numPresets = np;
         resetPresets = true;
     }
 
     public int getPassLength() {
         return maxPassLength;
     }
     public void setPassLength(int length) {
         maxPassLength = length;
     }
     
     public int getIONameLength() {
         return maxIONameLength;
     }
     
     public int getPresetNameLength() {
         return maxPresetNameLength;
     }
 
     public void setPresetNameLength(int pnl) {
         maxPresetNameLength = pnl;
     }
 
     public void setConnection(Connection cxn) throws IOException {
         if (isConnected()) {
             disconnect();
         }
         connection = (IPConnection) cxn;
         getConfiguration().setProperty("ipAddr", ((IPConnection)cxn).getIpAddr());
         getConfiguration().setProperty("port", ""+((IPConnection)cxn).getPort());
         saveConfiguration();
     }
 
     public Connection getConnection() {
         return connection;
     }
 
     /* For property change listener support */
     private void setProgress(float progress) {
         pcsupport.firePropertyChange("progress", this.progress, progress);
         this.progress = progress;
     }
 
     public void addPropertyChangeListener(PropertyChangeListener listener) {
         pcsupport.addPropertyChangeListener(listener);
     }
 
     public void removePropertyChangeListener(PropertyChangeListener listener) {
         pcsupport.removePropertyChangeListener(listener);
     }
 
     //------------------------------------------------------------------------
     public byte[] getPasswordHash(String pwd) {
         MessageDigest md;
         byte[] digested = null;
         try {
             md = MessageDigest.getInstance("MD5");
             md.update(pwd.getBytes());
             digested = md.digest();
         } catch (NoSuchAlgorithmException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
 
         return digested;
     }
 
     //------------------------------------------------------------------------
     // Password operations
     // Network library just truncates password if it's too long.
     private String setPassword(Command cmdIn, String pwd) {
         String newPwd = pwd;
         if (connected) {
             if (deviceWriteRead(cmdIn, SequencePayload.class)) {
                 SequencePayload p = (SequencePayload) cmdIn.getPayload();
                 int status = p.get(0);
                 if (status != 0) {
                     newPwd = this.adminPassword;
                 }
             }
         }
         return newPwd;
     }
 
     public void setAdminPassword(String pwd) {
         this.adminPassword = setPassword(new SetAdminPasswordCommand(pwd), pwd);
     }
 
     public void setUnlockPassword(String pwd) {
         this.unlockPassword = setPassword(new SetUnlockPasswordCommand(pwd), pwd);
     }
 
     //------------------------------------------------------------------------
     // Lock functionality
     //------------------------------------------------------------------------
     // Network library just truncates password if it's too long.
     public boolean unlock(String password) {
         boolean success = false;
 
         if (connected) {
             Command cmd = new ToggleLockCommand(password);
             if (deviceWriteRead(cmd, SequencePayload.class)) {
                 SequencePayload p = (SequencePayload) cmd.getPayload();
                 int status = p.get(0);
                 locked = status == 0;
             }
         } else {
             // get actual pass digest
             byte[] passHash = getPasswordHash(unlockPassword);
 
             // digest submitted password
             MessageDigest md;
             byte[] submittedDigest = null;
             try {
                 md = MessageDigest.getInstance("MD5");
                 md.update(password.getBytes());
                 submittedDigest = md.digest();
             } catch (NoSuchAlgorithmException ex) {
                 // bah!
             }
 
             // check equality
             if (passHash != null && submittedDigest != null
                     && java.util.Arrays.equals(passHash, submittedDigest)) {
                 locked = false;
             }
         }
 
         return !locked;
     }
 
     public void lock() {
         // send code to machine to lock itself.
         if (connected) {
             Command c = new ToggleLockCommand();
             if (deviceWriteRead(c, SequencePayload.class)) {
 
                 SequencePayload p = (SequencePayload) c.getPayload();
                 int status = p.get(0);
                 locked = status == 0;
             }
         } else {
             locked = true;
         }
     }
 
     public boolean isLocked() {
         return locked;
     }
 
     public void push() {
         if (!isConnected()) // throw exception
         {
             return;
         }
 
         return;
     }
 
     private void pushInputName(Input i) {
         if (connected)
         {
             resetInput = true;
         }
     }
 
     private void pushOutputName(Output o) {
         if (connected)
         {
             resetOutput = true;
         }
     }
 
     private void deviceWriteAndSkip(Command cmdOut)
     {
         try {
             connection.write(cmdOut);
             connection.clearInput(500);
         } catch (IOException ex) {
             Logger.getLogger(Device.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /// WARNING!!! MUTATES cmdOut!!!
     private boolean deviceWriteRead(Command cmdOut, Class payloadClass) {
         return deviceWriteRead(cmdOut, payloadClass, 0);
     }
 
     private boolean deviceWriteRead(Command cmdOut, Class payloadClass, int sleepTime) {
         boolean obtained = false;
         Command cmdIn = null;
         int i = 0;
         while (!obtained && i < MAX_TRIES) {
             try {
                 // 'clear' the input stream before a write
                 //connection.getInStream().skip(connection.getInStream().available());
                 connection.write(cmdOut);
                 if (sleepTime > 0) {
                     Thread.sleep(sleepTime);
                 }
                 cmdIn = connection.readOne();
                 obtained = payloadClass.isInstance(cmdIn.getPayload());
             } catch (IOException ex) {
                 logger.log(Level.WARNING, null, ex);
             } catch (InterruptedException ex) {
                 logger.log(Level.WARNING, null, ex);
             }
         }
         if (obtained) {
             cmdOut.setPayload(cmdIn.getPayload());
         }
         return obtained;
     }
 
     @Override
     public void propertyChange(PropertyChangeEvent evt) {
         Object src = evt.getSource();
         if (src instanceof Connector) {
             if ("name".equals(evt.getPropertyName())) {
                 if (src instanceof Input) {
                     Input i = (Input) src;
                     Logger.getLogger(getClass().getCanonicalName()).info("Detected name change on input #" + i.getIndex());
                     pushInputName(i);
                 } else {
                     Output o = (Output) src;
                     Logger.getLogger(getClass().getCanonicalName()).info("Detected name change on output #" + o.getIndex());
                     pushOutputName(o);
                 }
             }
             if ("icon".equals(evt.getPropertyName())) {
             }
         }
     }
 
     public void lockAdmin()
     {
         if (isConnected())
         {
             Command cmd = new ToggleUtilityLockCommand();
             if (deviceWriteRead(cmd, SequencePayload.class))
             {
                 if (((SequencePayload)cmd.getPayload()).get(0) > 0)
                 {
                     adminUnlocked = false;
                 } else {
                     activeAdminPassword = "";
                     adminUnlocked = true;
                 }
             }
         }
     }
 
     public void unlockAdmin(String password)
     {
         if (isConnected())
         {
             Command cmd = new ToggleUtilityLockCommand(password);
             if (deviceWriteRead(cmd, SequencePayload.class))
             {
                 if (((SequencePayload)cmd.getPayload()).get(0) > 0)
                 {
                     activeAdminPassword = password;
                     adminUnlocked = true;
                 } else {
                     activeAdminPassword = "";
                     adminUnlocked = false;
                 }
             }
         }
     }
 
     public boolean isAdminLocked() {
         return ! adminUnlocked;
     }
 
     //------------------------------------------------------------------------
     abstract class ConnectorEnumeration
             implements Enumeration<Connector> {
 
         int index = 0;
         List<Connector> list = null;
         boolean live;
 
         public ConnectorEnumeration() {
         }
 
         public ConnectorEnumeration(List<Connector> l) {
             this(l, false);
         }
 
         public ConnectorEnumeration(List<Connector> l, boolean live) {
             list = l;
             this.live = live;
         }
 
         @Override
         public boolean hasMoreElements() {
             if (connected && (live || isReset())) {
                 boolean r = index < getMax();
                 // We do not need to fetch from the device any longer
                 if (!r) {
                     setReset(false);
                 }
                 return r;
             }
             return index < list.size();
         }
 
         @Override
         public Connector nextElement() {
             if (connected && (live || isReset())) {
                 Command c = getNameLookupCommand(index + 1);
                 if (deviceWriteRead(c, IdNamePayload.class)) {
                     IdNamePayload p = (IdNamePayload) c.getPayload();
                     String name = p.getStrData();
 
                     Connector ctr = makeNewConnector(name, "", index + 1);
                     list.set(index, ctr);
                 }
             }
             if (DELAY > 0) {
                 try {
                     Thread.sleep(DELAY);
                 } catch (InterruptedException ex) {
                     logger.log(Level.SEVERE, null, ex);
                 }
             }
             setProgress((float) index / list.size());
             return (Connector) list.get(index++);
         }
 
         public abstract boolean isReset();
 
         public abstract void setReset(boolean r);
 
         public abstract Command getNameLookupCommand(int paramInt);
 
         public abstract int getMax();
 
         protected abstract Connector makeNewConnector(String name, String string, int i);
     }
 }
