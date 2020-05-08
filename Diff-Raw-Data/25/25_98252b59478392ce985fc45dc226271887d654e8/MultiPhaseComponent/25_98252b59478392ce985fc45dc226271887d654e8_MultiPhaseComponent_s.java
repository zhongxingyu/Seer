 package org.pillarone.riskanalytics.core.components;
 
 import org.pillarone.riskanalytics.core.packets.PacketList;
 import org.pillarone.riskanalytics.core.wiring.ITransmitter;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Splits the calculation logic into several phases. Each channel has to be assigned to a phase. As soon as all
  * in channels of a specific phase have received their packets, the corresponding part/phase of their doCalculation()
  * implementation is executed and the out channels belonging to the same phase are fired.<br/>
  * In and out channels are reset once all phases have been finished.<br/>
  * Restrictions:<ul>
  * <li>If the component is not used as a start component, there has to be at least one in channel for each
 * phase.</li>
 * <li>All channels have to be connected (no test cases available for unconnected channels)</li></ul>
  *
  * @author stefan.kunz (at) intuitive-collaboration (dot) com
  */
 abstract public class MultiPhaseComponent extends Component implements IChannelAllocation {
 
     private Map<ITransmitter, String> phasePerTransmitterInput = new HashMap<ITransmitter, String>();
     private Map<ITransmitter, String> phasePerTransmitterOutput = new HashMap<ITransmitter, String>();
     private Map<String, Integer> numberOfTransmitterPerPhaseInput = new HashMap<String, Integer>();
     private Map<String, Integer> numberOfTransmitterPerPhaseOutput = new HashMap<String, Integer>();
     private Map<String, Integer> numberOfNotifiedTransmittersPerPhase = new HashMap<String, Integer>();
     private Integer numberOfPhases;
     private int finishedPhases;
 
     /**
      * Executed once only per instance. Calls allocateChannelsToPhases() and initialized numberOfNotifiedTransmittersPerPhase
      */
     private void init() {
         if (numberOfPhases != null) return;
         allocateChannelsToPhases();
         for (String phase : numberOfTransmitterPerPhaseInput.keySet()) {
             numberOfNotifiedTransmittersPerPhase.put(phase, 0);
         }
         numberOfPhases = Math.max(numberOfTransmitterPerPhaseOutput.size(), numberOfTransmitterPerPhaseInput.size());
     }
 
     /**
      * nothing happens as doCalculation(phase) is used in the context of a MultiPhaseComponent
      */
     @Override
     protected final void doCalculation() {
     }
 
     /**
      * @param phase
      */
     protected final void calculateAndPublish(String phase){
         init();
         doCalculation(phase);
         publishResults(phase);
     }
 
     abstract public void doCalculation(String phase);
 
     /**
      * Execute doCalculation of all phases
      */
     @Override
     protected void execute() {
         init();
         for (String phase : numberOfTransmitterPerPhaseOutput.keySet()) {
             calculateAndPublish(phase);
         }
         reset();
     }
 
     @Override
     public void notifyTransmitted(ITransmitter transmitter) {
         init();
         String transmitterPhase = phasePerTransmitterInput.get(transmitter);
         increaseNumberOfTransmitter(numberOfNotifiedTransmittersPerPhase, transmitterPhase);
         if (numberOfNotifiedTransmittersPerPhase.get(transmitterPhase).equals(numberOfTransmitterPerPhaseInput.get(transmitterPhase))) {
             calculateAndPublish(transmitterPhase);
         }
     }
 
     /**
      * @param packetList adds the corresponding transmitter as key to the phasePerTransmitterOutput map
      * @param phase used as corresponding value and stored within phasePerTransmitterOutput
      */
     public void setTransmitterPhaseOutput(PacketList packetList, String phase) {
        for (ITransmitter transmitter : getAllOutputTransmitter()) {
            if (transmitter.getSource() == (packetList)) {
                phasePerTransmitterOutput.put(transmitter, phase);
                increaseNumberOfTransmitter(numberOfTransmitterPerPhaseOutput, phase);
             }
         }
     }
 
     /**
      * @param packetList adds the corresponding transmitter as key to the phasePerTransmitterInput map
      * @param phase used as corresponding value and stored within phasePerTransmitterInput
      */
     public void setTransmitterPhaseInput(PacketList packetList, String phase) {
        for (ITransmitter transmitter : getAllInputTransmitter()) {
            if (transmitter.getTarget() == (packetList)) {
                phasePerTransmitterInput.put(transmitter, phase);
                increaseNumberOfTransmitter(numberOfTransmitterPerPhaseInput, phase);
             }
         }
     }
 
     /**
      * @param map key: phase, value: number of transmitters
      * @param phase the value of the corresponding phase will be increased by 1
      */
     private void increaseNumberOfTransmitter(Map<String, Integer> map, String phase) {
         Integer numberOfTransmitter = map.get(phase);
         map.put(phase, numberOfTransmitter == null ? 1 : numberOfTransmitter + 1);
     }
 
     /**
      * nothing happens as publishResults(phase) is used in the context of a multi phase component
      */
     protected void publishResults() {
     }
     /**
      * Publish results of transmitters belonging to the phase and call reset if all phases have been executed
      * @param phase
      */
     protected void publishResults(String phase) {
         for (Map.Entry<ITransmitter, String> entry : phasePerTransmitterOutput.entrySet()) {
             if (entry.getValue().equals(phase)) {
                 entry.getKey().transmit();
             }
         }
         finishedPhases++;
         if (finishedPhases == numberOfPhases) {
             reset();
         }
     }
 
     @Override
     protected void reset() {
         super.reset();
         finishedPhases = 0;
         numberOfNotifiedTransmittersPerPhase.clear();
     }
 }
