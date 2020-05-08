 /**
  * Created on 09.11.2004
  * 
  * Copyright (c) 2004, Olaf Landsiedel, Protocol Engineering and 
  * Distributed Systems, University of Tuebingen
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  *
  * Neither the name of the University of California, Los Angeles nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package avrora.monitors;
 
 import avrora.core.ControlFlowGraph.Block;
 import avrora.core.Instr;
 import avrora.core.Program;
 import avrora.core.Program.Location;
 import avrora.sim.Simulator;
 import avrora.sim.State;
 import avrora.util.Terminal;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 
 
 /**
  * The <code>EnergyProfiler</code> class is a monitor that tracks the power consumption of the cpu
  * instructions. It provides a breakdown to the procedures and records the exact cycles spend in each
  * procedure.
  *
  * @author Olaf Landsiedel
  */
 public class EnergyProfiler extends MonitorFactory {
 
     /**
      * The <code>EnergyProfiler</code> class is a monitor that tracks the power consumption of the cpu
      * instructions. It provides a breakdown to the procedures and records the exact cycles spend in each
      * procedure.
      */
     public class Monitor implements avrora.monitors.Monitor {
         private final Simulator simulator;
         private final Program program;
 
         /**
          * <code>labelLookup</code> provides a HashMap to lookup procedures, e.g. labels. The HashMap key is
          * the label address, as value EnergyProfile is used
          */
         private final HashMap labelLookup;
 
         /**
          * <code>profiles</code>: list of all EnergyProfiles
          */
         private final LinkedList profiles;
 
         /**
          * <code>procedureProbe</code>: probes for entering a new basic block, always need to check whether we
          * are entering a new procedure
          */
         private final ProcedureProbe procedureProbe;
         /**
          * <code>sleepProbe</code>: Probe for entering sleep mode
          */
         private final SleepProbe sleepProbe;
 
         /**
          * <code>currentMode</code>: the current procedure, the program is in
          */
         private EnergyProfile currentMode;
         /**
          * <code>lastChange</code>: the cycle count of the moment the program entered a new basic block
          * lasttime
          */
         private long lastChange;
         /**
          * <code>sleepCycles</code>: counts the cycles spend in a sleep mode
          */
         private long sleepCycles;
 
         /**
          * construct a new monitor
          *
          * @param s Simulator
          */
         Monitor(Simulator s) {
             simulator = s;
             program = s.getProgram();
             labelLookup = new HashMap();
             profiles = new LinkedList();
 
             procedureProbe = new ProcedureProbe();
             sleepCycles = 0;
             sleepProbe = new SleepProbe();
             //find all sleep opcodes in the program
             findSleep();            
             
             //scan all labels and put the in the list
             setupLabels();
 
             //startup values
             lastChange = 0;
             currentMode = nearestLabel(0);
             //scan each basic block and fine the corresponding label, e.g. procedure
             Iterator it = program.getCFG().getSortedBlockIterator();
             int address = 0;
             int size = 0;
             while (it.hasNext()) {
                 Block block = (Block)it.next();
                 size = block.getSize();
                 address = block.getAddress();
                 if (size > 0 && program.readInstr(address) != null) {
                     //System.out.print("lookup new address: " + address + " " + size + "    ");
                     labelLookup.put(new Integer(address), nearestLabel(address));
                     s.insertProbe(procedureProbe, address);
                 }
             }
         }
 
         /**
          * scan all labels and put them in the list
          */
         private void setupLabels() {
             Iterator it = program.getLabels().entrySet().iterator();
             while (it.hasNext()) {
                 Map.Entry entry = (Map.Entry)it.next();
                 Location tempLoc = (Location)entry.getValue();
                 profiles.add(new EnergyProfile(tempLoc));
             }
         }
 
         /**
          * find the label nearest to the address. This returns the name of the procedure the address is in.
          *
         * @param address address to lookup
          * @return the nearest Label
          */
         private EnergyProfile nearestLabel(int address) {
             Iterator it = profiles.iterator();
             EnergyProfile match = null;
             while (it.hasNext()) {
                 EnergyProfile temp = (EnergyProfile)it.next();
                 if ((temp.location.address <= address) && ((match == null) || (temp.location.address > match.location.address))) {
                     match = temp;
                 }
             }
             //System.out.println("basic block start:" + address + " , fitting label " + match.location.name + " @ " + match.location.address);
             return match;
         }
 
         /**
          * find all sleep opcodes in the program
          */
         private void findSleep() {
             int i = 0;
             //boolean done = false;
             while (i < program.program_length) {
                 Instr instr = program.readInstr(i);
                 //System.out.println(i);
                 if (instr != null) {
                     if (instr.properties.name.equals("sleep")) {
                         simulator.insertProbe(sleepProbe, i);
                         //System.out.println("found sleep" + i );
                     }
                     i = i + instr.getSize();
                 } else
                     i = i + 1;
             }
         }
 
         /**
          * The <code>report()</code> method generates a textual report after the simulation is complete. The
          * text contains a breakdown each procedure called in the program and the corresponding number of
          * cycles spent in it during program execution. Addionally the number of cycles spent in sleep mode is
          * provided.
          */
         public void report() {
             //log current state
             long cycles = simulator.getState().getCycles() - lastChange;
             if (cycles > 0) {
                 if (currentMode != null) {
                     //system not sleeping
                     currentMode.cycles += cycles;
                 } else {
                     //system sleeping
                     sleepCycles += cycles;
                 }
             }
             //display data
             Terminal.printCyan("\nEnergy Consumption Procedure Breakdown:\n\n");
             Terminal.printCyan("notation: procedureName@Address: cycles\n");
             Iterator it = profiles.iterator();
             while (it.hasNext()) {
                 EnergyProfile profile = (EnergyProfile)it.next();
                 if (profile.cycles > 0) {
                     Terminal.println("   " + profile.location.name + "@" + profile.location.address + ": " + profile.cycles);
                 }
             }
             if (sleepCycles > 0)
                 Terminal.println("   sleeping: " + sleepCycles);
             Terminal.println("");
         }
 
         /**
          * @author Olaf Landsiedel
          *         <p/>
          *         Class for a Probe which is called when a new basic block is entered
          */
         public class ProcedureProbe implements Simulator.Probe {
 
             /**
              * fired before the basic block is entered, it logs the previos state
              *
              * @see avrora.sim.Simulator.Probe#fireBefore(avrora.core.Instr, int, avrora.sim.State)
              */
             public void fireBefore(Instr i, int address, State s) {
                 //System.out.println("reached new basic block at " + address + "   label: " + ((EnergyProfile)labelLookup.get(new Integer(address))).location.name);            
                 long cycles = simulator.getState().getCycles() - lastChange;
                 if (cycles > 0) {
                     if (currentMode != null) {
                         //system not sleeping
                         currentMode.cycles += cycles;
                     } else {
                         //system sleeping
                         sleepCycles += cycles;
                     }
                 }
                 lastChange = simulator.getState().getCycles();
                 currentMode = (EnergyProfile)labelLookup.get(new Integer(address));
             }
 
             /**
              * In this case, nothing is done. However, the interface requires an implementation
              *
              * @see avrora.sim.Simulator.Probe#fireAfter(avrora.core.Instr, int, avrora.sim.State)
              */
             public void fireAfter(Instr i, int address, State s) {
                 // do nothing
             }
         }
 
         /**
          * @author Olaf Landsiedel
          *         <p/>
          *         Class for a probe when a sleep mode is enered
          */
         public class SleepProbe implements Simulator.Probe {
 
             /**
              * fired before a sleep mode is entered, it logs the previos state
              *
              * @see avrora.sim.Simulator.Probe#fireBefore(avrora.core.Instr, int, avrora.sim.State)
              */
             public void fireBefore(Instr i, int address, State s) {
                 //System.out.println("enter sleep");
                 long cycles = simulator.getState().getCycles() - lastChange;
                 if (cycles > 0) {
                     if (currentMode != null) {
                         //system not sleeping
                         currentMode.cycles += cycles;
                     } else {
                         //system sleeping
                         sleepCycles += cycles;
                     }
                 }
                 lastChange = simulator.getState().getCycles();
                 currentMode = null;
             }
 
             /**
              * In this case, nothing is done. However, the interface requires an implementation
              *
              * @see avrora.sim.Simulator.Probe#fireAfter(avrora.core.Instr, int, avrora.sim.State)
              */
             public void fireAfter(Instr i, int address, State s) {
                 // do nothing
             }
         }
     }
 
     /**
      * The constructor for the <code>EnergyProfiler</code> class builds a new <code>MonitorFactory</code>
      * capable of creating monitors for each <code>Simulator</code> instance passed to the
      * <code>newMonitor()</code> method.
      */
     public EnergyProfiler() {
         super("energyProfiler", "The \"energy profile\" monitor tracks the power consumption of procedures");
     }
 
     /**
      * The <code>newMonitor()</code> method creates a new monitor that is capable of energy profiling. It
      * provides a breakdown to the individual procedures of the program.
      *
      * @param s the simulator to create a monitor for
      * @return an instance of the <code>Monitor</code> interface for the specified simulator
      */
     public avrora.monitors.Monitor newMonitor(Simulator s) {
         return new Monitor(s);
     }
 
     /**
      * @author Olaf Landsiedel
      *         <p/>
      *         Simple class for a energy profile. It contains the location, e.g. address of the procedure and
      *         the number of cycles spent in it.
      */
     public class EnergyProfile {
         /**
          * <code>cycles</code>: number of cycles spend in this procedure
          */
         public long cycles;
         /**
          * <code>location</code>: name and address of this procedure
          */
         public Location location;
 
         /**
          * construct a new energy profile
          *
          * @param loc Location, e.g. name and address, of the profile
          */
         public EnergyProfile(Location loc) {
             cycles = 0;
             location = loc;
         }
     }
 
 
 }
 
 
 
