 /**
  * Copyright (c) 2004, Regents of the University of California
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
 
 package avrora.sim;
 
 import avrora.Avrora;
 import avrora.core.Instr;
 import avrora.core.Program;
 import avrora.sim.mcu.Microcontroller;
 import avrora.sim.util.PeriodicEvent;
 import avrora.util.StringUtil;
 import avrora.util.Terminal;
 import avrora.util.Verbose;
 
 /**
  * The <code>Simulator</code> class implements a full processor simulator for the AVR instruction set. It is
  * the base class of specific implementations that implement processor-specific behavior.
  *
  * @author Ben L. Titzer
  * @see Program
  * @see Instr
  * @see BaseInterpreter
  */
 public abstract class Simulator implements IORegisterConstants {
 
     public class Printer {
 
         public final boolean enabled;
 
         Printer(String category) {
             Verbose.Printer p = Verbose.getVerbosePrinter(category);
             enabled = p.enabled;
         }
 
         public void println(String s) {
             if (enabled) {
                 String idstr = StringUtil.rightJustify(id, 4);
                 String cycstr = StringUtil.rightJustify(clock.getCount(), 10);
                Terminal.println(idstr + " " + cycstr + "   " + s);
             } else {
                 throw Avrora.failure("Disabled printer: performance bug!");
             }
         }
     }
 
     public Simulator.Printer getPrinter(String c) {
         return new Printer(c);
     }
 
     Simulator.Printer eventPrinter = getPrinter("sim.event");
     Simulator.Printer interruptPrinter = getPrinter("sim.interrupt");
 
     /**
      * The <code>program</code> field allows descendants of the <code>Simulator</code> class to access the
      * program that is currently loaded in the simulator.
      */
     protected final Program program;
 
     /**
      * The <code>microcontroller</code> field stores a reference to the microcontroller being simulated.
      */
     protected final Microcontroller microcontroller;
 
     /**
      * The <code>interpreter</code> field stores a reference to the instruction set interpreter.
      */
     protected BaseInterpreter interpreter;
 
     /**
      * The <code>interrupts</code> array stores a reference to an <code>Interrupt</code> instance for each of
      * the interrupt vectors supported in the simulator.
      */
     protected Interrupt[] interrupts;
 
     /**
      * The <code>clock</code> field stores a reference to the <code>MainClock</code> instance that tracks the
      * clock cycles that have passed for this simulator.
      */
     protected MainClock clock;
 
     protected final int id;
 
     protected final InterpreterFactory factory;
 
     /**
      * The <code>MAX_INTERRUPTS</code> fields stores the maximum number of interrupt vectors supported by the
      * simulator.
      */
     public static int MAX_INTERRUPTS = 35;
 
     private EnergyControl energyControl;
 
     /**
      * The constructor creates the internal data structures and initial state of the processor. It constructs
      * an instance of the simulator that is ready to have devices attached, IO registers probed, and probes
      * and events inserted. Users should not create <code>Simulator</code> instances directly, but instead
      * should get an instance of the appropriate processor and load the program into it.
      *
      * @param p the program to load into the simulator
      */
     public Simulator(int i, InterpreterFactory f, Microcontroller mcu, Program p) {
         id = i;
         microcontroller = mcu;
         program = p;
         interrupts = new Interrupt[MAX_INTERRUPTS];
 
         // set all interrupts to ignore
         for (int cntr = 0; cntr < MAX_INTERRUPTS; cntr++)
             interrupts[cntr] = IGNORE;
 
         factory = f;
 
         // enable the energy modelling
         energyControl = new EnergyControlImpl();
 
         // reset the state of the simulation
         reset();
     }
 
     /**
      * The <code>Simulator.Probe</code> interface represents a programmer-defined probe that can be inserted
      * at a particular instruction in the program. or at every instruction. Probes can be usedfor profiling,
      * analysis, or program understanding. The <code>fireBefore()</code> and <code>fireAfter()</code> methods
      * are called before and after the target instruction executes in simulation. Probes can also be inserted
      * in the "main loop" of the interpreter, so that the probe fires before and after every instruction
      * executed.
      *
      * @author Ben L. Titzer
      */
     public interface Probe {
 
         /**
          * The <code>fireBefore()</code> method is called before the probed instruction executes.
          *
          * @param i       the instruction being probed
          * @param address the address at which this instruction resides
          * @param state   the state of the simulation
          */
         public void fireBefore(Instr i, int address, State state);
 
         /**
          * The <code>fireAfter()</code> method is called after the probed instruction executes.
          *
          * @param i       the instruction being probed
          * @param address the address at which this instruction resides
          * @param state   the state of the simulation
          */
         public void fireAfter(Instr i, int address, State state);
     }
 
     /**
      * The <code>Simulator.Event</code> interface represents an event that is fired when a timed event occurs
      * within the simulator. Users of the simulator can insert timed events that model environmental factors,
      * implement timeouts, timers, or any other type of functionality that is simulation-time dependent.
      */
     public interface Event {
         /**
          * The <code>fire()</code> method is called when the event to which it is tied happens with in the
          * simulator.
          */
         public void fire();
     }
 
     /**
      * The <code>MemoryProbe</code> interface represents a user probe that is fired when a watchpoint detects
      * an access to an address where this memory probe has been inserted.
      */
     public interface Watch {
 
         /**
          * The <code>fireBeforeRead()</code> method is called before the data address is read by the program.
          *
          * @param i         the instruction being probed
          * @param address   the address at which this instruction resides
          * @param state     the state of the simulation
          * @param data_addr the address of the data being referenced
          * @param value     the value of the memory location being read
          */
         public void fireBeforeRead(Instr i, int address, State state, int data_addr, byte value);
 
         /**
          * The <code>fireBeforeWrite()</code> method is called before the data address is written by the
          * program.
          *
          * @param i         the instruction being probed
          * @param address   the address at which this instruction resides
          * @param state     the state of the simulation
          * @param data_addr the address of the data being referenced
          * @param value     the value being written to the memory location
          */
         public void fireBeforeWrite(Instr i, int address, State state, int data_addr, byte value);
 
         /**
          * The <code>fireAfterRead()</code> method is called after the data address is read by the program.
          *
          * @param i         the instruction being probed
          * @param address   the address at which this instruction resides
          * @param state     the state of the simulation
          * @param data_addr the address of the data being referenced
          * @param value     the value of the memory location being read
          */
         public void fireAfterRead(Instr i, int address, State state, int data_addr, byte value);
 
         /**
          * The <code>fireAfterWrite()</code> method is called after the data address is written by the
          * program.
          *
          * @param i         the instruction being probed
          * @param address   the address at which this instruction resides
          * @param state     the state of the simulation
          * @param data_addr the address of the data being referenced
          * @param value     the value being written to the memory location
          */
         public void fireAfterWrite(Instr i, int address, State state, int data_addr, byte value);
     }
 
     /**
      * The <code>BreakPointException</code> is an exception that is thrown by the simulator before it executes
      * an instruction which has a breakpoint. When this exception is thrown within the simulator, the
      * simulator is left in a state where it is ready to be resumed where it left off by the
      * <code>start()</code> method. When resuming, the breakpointed instruction will not cause a second
      * <code>BreakPointException</code> until the the instruction is executed a second time.
      *
      * @author Ben L. Titzer
      */
     public static class BreakPointException extends RuntimeException {
         /**
          * The <code>instr</code> field stores the instruction that caused the breakpoint.
          */
         public final Instr instr;
 
         /**
          * The <code>address</code> field stores the address of the instruction that caused the breakpoint.
          */
         public final int address;
 
         /**
          * The <code>state</code> field stores a reference to the state of the simulator when the breakpoint
          * occurred, before executing the instruction.
          */
         public final State state;
 
         BreakPointException(Instr i, int a, State s) {
             super("breakpoint @ " + StringUtil.toHex(a, 4) + " reached");
             instr = i;
             address = a;
             state = s;
         }
     }
 
     /**
      * The <code>TimeoutException</code> is thrown by the simulator when a timeout reaches zero. Timeouts can
      * be used to ensure termination of the simulator during testing, and implementing timestepping in
      * surrounding tools such as interactive debuggers or visualizers.
      * <p/>
      * When the exception is thrown, the simulator is left in a state that is safe to be resumed by a
      * <code>start()</code> call.
      *
      * @author Ben L. Titzer
      */
     public static class TimeoutException extends RuntimeException {
 
         /**
          * The <code>instr</code> field stores the next instruction to be executed after the timeout.
          */
         public final Instr instr;
 
         /**
          * The <code>address</code> field stores the address of the next instruction to be executed after the
          * timeout.
          */
         public final int address;
 
         /**
          * The <code>state</code> field stores the state of the simulation at the point at which the timeout
          * occurred.
          */
         public final State state;
 
         /**
          * The <code>timeout</code> field stores the value (in clock cycles) of the timeout that occurred.
          */
         public final long timeout;
 
         TimeoutException(Instr i, int a, State s, long t, String l) {
             super("timeout @ " + StringUtil.addrToString(a) + " reached after " + t + " " + l);
             instr = i;
             address = a;
             state = s;
             timeout = t;
         }
     }
 
 
     /**
      * The <code>Interrupt</code> interface represents the behavior of an interrupt (how it manipulates the
      * state of the processor) when it is posted and when it is triggered (handler is executed by the
      * processor). For example, an external interrupt, when posted, sets a bit in an IO register, and if the
      * interrupt is not masked, will add it to the pending interrupts on the processor. When the interrupt
      * head, it remains flagged (the bit in the IO register remains on). Some interrupts clear bits in IO
      * registers on triggered (e.g. timer interrupts). This interface allows both of these behaviors to be
      * implemented.
      *
      * @author Ben L. Titzer
      */
     public interface Interrupt {
         /**
          * The <code>force()</code> method is called by the simulator when an interrupt is being forced by an
          * outside source (i.e. not of the simulation). For example, when stress testing a program by
          * bombarding it with interrupts, this method would be used.
          */
         public void force();
 
         /**
          * The <code>fire()</code> method is called by the simulator when the interrupt is about to be
          * processed (i.e. it has been posted, and is not masked). This method is called just before control
          * is transferred to the interrupt handler.
          */
         public void fire();
     }
 
 
     /**
      * The <code>IGNORE</code> field stores a reference to a singleton anonymous class that ignores posting
      * and firing of an interrupt. This is the default value for interrupts in a freshly initialized
      * <code>Simulator</code> instance.
      */
     public static final Interrupt IGNORE = new Interrupt() {
         public void force() {
         }
 
         public void fire() {
         }
     };
 
     /**
      * The <code>getMicrocontroller()</code> method gets a reference to the microcontroller being simulated.
      *
      * @return a reference to the microcontroller being simulated
      */
     public Microcontroller getMicrocontroller() {
         return microcontroller;
     }
 
     /**
      * The <code>getProgram()</code> method gets a reference to the program that has been loaded onto this
      * simulator.
      *
      * @return a reference to the <code>Program</code> instance representing the program loaded onto this
      *         <code>Simulator</code> object
      */
     public Program getProgram() {
         return program;
     }
 
     public Clock getClock() {
         return clock;
     }
 
     public int getID() {
         return id;
     }
 
     public BaseInterpreter getInterpreter() {
         return interpreter;
     }
 
     /**
      * The <code>getState()</code> retrieves a reference to the current state of the simulation, including the
      * values of all registers, the SRAM, the IO register, the program memory, program counter, etc. This
      * state is mutable.
      *
      * @return a reference to the current state of the simulation
      */
     public State getState() {
         return interpreter;
     }
 
     /**
      * The <code>start()</code> method begins the simulation. It causes the simulator to enter a runLoop that
      * executes instructions, firing probes and events as it executes. The <code>start()</code> method returns
      * normally when the </code>break</code> AVR instruction is executed, when a
      * <code>BreakPointException</code> is thrown, when a <code> TimeoutException</code> is thrown, or when
      * the <code>stop()</code> method on this simulator instance is called.
      */
     public void start() {
         interpreter.start();
     }
 
     /**
      * The <code>stop()</code> method stops the simulation if it is running. This method can be called from
      * within a probe or event or from another thread.
      */
     public void stop() {
         interpreter.stop();
     }
 
     /**
      * The <code>reset()</code> method stops the simulation and resets its state to the default initial state.
      * Probes inserted in the program are retained. All events are removed.
      */
     public void reset() {
         clock = new MainClock("MAIN", microcontroller.getHz());
         interpreter = factory.newInterpreter(this, program,
                 microcontroller.getFlashSize(),
                 microcontroller.getIORegSize(),
                 microcontroller.getRamSize());
     }
 
     /**
      * The <code>getInterruptVectorAddress()</code> method computes the location in memory to jump to for the
      * given interrupt number. On the Atmega128L, the starting point is the beginning of memory and each
      * interrupt vector slot is 4 bytes. On older architectures, this is not the case, therefore this method
      * has to be implemented according to the specific device being simulated.
      *
      * @param inum the interrupt number
      * @return the byte address that represents the address in the program to jump to when this interrupt is
      *         fired
      */
     protected int getInterruptVectorAddress(int inum) {
         return (inum - 1) * 4;
     }
 
     /**
      * The <code>insertProbe()</code> method allows a probe to be inserted that is executed before and after
      * every instruction that is executed by the simulator
      *
      * @param p the probe to insert
      */
     public void insertProbe(Probe p) {
         interpreter.insertProbe(p);
     }
 
     /**
      * The <code>insertProbe()</code> method allows a probe to be inserted at a particular address in the
      * program that corresponds to an instruction. The probe is then fired before and after that particular
      * instruction is executed.
      *
      * @param p    the probe to insert
      * @param addr the address at which to insert the probe
      */
     public void insertProbe(Probe p, int addr) {
         interpreter.insertProbe(p, addr);
     }
 
     /**
      * The <code>removeProbe()</code> method removes a probe from the global probe table (the probes executed
      * before and after every instruction). The comparison used is reference equality, not
      * <code>.equals()</code>.
      *
      * @param b the probe to remove
      */
     public void removeProbe(Probe b) {
         interpreter.removeProbe(b);
     }
 
     /**
      * The <code>removeProbe()</code> method removes a probe from the instruction at the specified the
      * address. The comparison used is reference equality, not <code>.equals()</code>.
      *
      * @param p    the probe to remove
      * @param addr the address from which to remove the probe
      */
     public void removeProbe(Probe p, int addr) {
         interpreter.removeProbe(p, addr);
     }
 
     /**
      * The <code>insertBreakPoint()</code> method inserts a breakpoint at the instruction at the specified
      * address. At most one breakpoint can be inserted at a particular instruction. Subsequent calls to this
      * method would then have no effect for the same address.
      *
      * @param addr
      */
     public void insertBreakPoint(int addr) {
         interpreter.insertBreakPoint(addr);
     }
 
     /**
      * The <code>removeBreakPoint</code> method removes all breakpoints at the specified instruction at the
      * specified address.
      *
      * @param addr
      */
     public void removeBreakPoint(int addr) {
         interpreter.removeBreakPoint(addr);
     }
 
     /**
      * The <code>insertWatch()</code> method allows a watch to be inserted at a memory location. The probe
      * will be executed before every read or write to that memory location.
      *
      * @param p         the probe to insert
      * @param data_addr the data address at which to insert the probe
      */
     public void insertWatch(Watch p, int data_addr) {
         interpreter.insertWatch(p, data_addr);
     }
 
     /**
      * The <code>removeWatch()</code> method removes a given watch from the memory location. Reference
      * equality is used to check for equality when removing probes, not <code>.equals()</code>.
      *
      * @param p         the probe to remove
      * @param data_addr the data address from which to remove the probe
      */
     public void removeWatch(Watch p, int data_addr) {
         interpreter.removeWatch(p, data_addr);
     }
 
     /**
      * The <code>forceInterrupt()</code> method forces the simulator to post the specified interrupt
      * regardless of the normal source of the interrupt. If there is a flag register associated with the
      * specified interrupt, then the flag register's value will be set as if the original source of the
      * interrupt (e.g. a timer) had posted the interrupt. As with a normal post of the interrupt, if the
      * interrupt is masked out via a mask register or the master interrupt enable bit, the interrupt will not
      * be delivered. The main reason that this interface exists is for forcing programs to handle interrupts
      * and observe their behavior.
      *
      * @param num the interrupt number to force
      */
     public void forceInterrupt(int num) {
         if (interruptPrinter.enabled)
             interruptPrinter.println("FORCE INTERRUPT: " + num);
         interrupts[num].force();
     }
 
     /**
      * The <code>triggerInterrupt()</code> method is used by device implementations when they detect that an
      * interrupt should be triggered. This method will check whether this interrupt is enabled by consulting
      * its own internal table of interrupts that is kept consistent during writes to IO registers.
      *
      * @param num the number of the interrupt to trigger
      */
     protected void triggerInterrupt(int num) {
         if (interruptPrinter.enabled)
             interruptPrinter.println("FIRE INTERRUPT: " + num);
         interrupts[num].fire();
     }
 
     /**
      * The <code>insertEvent()</code> method inserts an event into the event queue of the simulator with the
      * specified delay in clock cycles. The event will then be executed at the future time specified
      *
      * @param e      the event to be inserted
      * @param cycles the number of cycles in the future at which to fire
      */
     public void insertEvent(Event e, long cycles) {
         if (eventPrinter.enabled)
             eventPrinter.println("INSERT EVENT: " + e + " + " + cycles);
         clock.insertEvent(e, cycles);
     }
 
     /**
      * The <code>insertTimeout()</code> method inserts an event into the event queue of the simulator that
      * causes it to stop execution and throw a <code>Simulator.TimeoutException</code> when the specified
      * number of clock cycles have expired.
      *
      * @param cycles the number of cycles to run before timing out
      */
     public void insertTimeout(long cycles) {
         insertEvent(new ClockCycleTimeout(cycles), cycles);
     }
 
     /**
      * The <code>insertPeriodicEvent()</code> method inserts an event into the event queue of the simulator
      * with the specified period. The <code> PeriodicEvent</code> instance created will continually reinsert
      * the event after each firing to achieve predictable periodic behavior.
      *
      * @param e      the event to insert
      * @param period the period in clock cycles
      * @return the <code>PeriodicEvent</code> instance inserted
      */
     public PeriodicEvent insertPeriodicEvent(Event e, long period) {
         if (eventPrinter.enabled)
             eventPrinter.println("INSERT PERIODIC EVENT: " + e + " + " + period);
         PeriodicEvent pt = new PeriodicEvent(this, e, period);
         clock.insertEvent(pt, period);
         return pt;
     }
 
     /**
      * The <code>removeEvent()</code> method removes an event from the event queue of the simulator. The
      * comparison used is reference equality, not <code>.equals()</code>.
      *
      * @param e the event to remove
      */
     public void removeEvent(Event e) {
         if (eventPrinter.enabled)
             eventPrinter.println("REMOVE EVENT: " + e);
         clock.removeEvent(e);
     }
 
     /**
      * The <code>delay()</code> method introduces a delay in the execution of the instructions of the program.
      * This is used by some devices for correct timing behavior. For example, the EEPROM, when written, causes
      * a small delay in which no instructions are executed.
      *
      * @param cycles the number of cycles to delay the simulation
      */
     public void delay(long cycles) {
         interpreter.delay(cycles);
     }
 
     public EnergyControl getEnergyControl() {
         return energyControl;
     }
 
     /**
      * The <code>InstructionCountTimeout</code> class is a probe that simply counts down and throws a
      * <code>TimeoutException</code> when the count reaches zero. It is useful for ensuring termination of the
      * simulator, for performance testing, or for profiling and stopping after a specified number of
      * invocations.
      *
      * @author Ben L. Titzer
      */
     public static class InstructionCountTimeout implements Probe {
         public final long timeout;
         protected long left;
 
         /**
          * The constructor for <code>InstructionCountTimeout</code> creates with the specified initial value.
          *
          * @param t the number of clock cycles before timeout should occur
          */
         public InstructionCountTimeout(long t) {
             timeout = t;
             left = t;
         }
 
         /**
          * The <code>fireBefore()</code> method is called before the probed instruction executes. In the
          * implementation of the timeout, it does nothing.
          *
          * @param i       the instruction being probed
          * @param address the address at which this instruction resides
          * @param state   the state of the simulation
          */
         public void fireBefore(Instr i, int address, State state) {
             // do nothing
         }
 
         /**
          * The <code>fireAfter()</code> method is called after the probed instruction executes. In the
          * implementation of the timeout, it simply decrements the timeout and and throws a TimeoutException
          * when the count reaches zero.
          *
          * @param i       the instruction being probed
          * @param address the address at which this instruction resides
          * @param state   the state of the simulation
          */
         public void fireAfter(Instr i, int address, State state) {
             if (--left <= 0)
                 throw new TimeoutException(i, address, state, timeout, "instructions");
         }
     }
 
     /**
      * The <code>InstructionCountTimeout</code> class is a probe that simply counts down and throws an
      * exception when the count reaches zero. It is useful for ensuring termination of the simulator, for
      * performance testing, or for profiling and stopping after a specified number of invocations.
      *
      * @author Ben L. Titzer
      */
     public class ClockCycleTimeout implements Event {
         public final long timeout;
 
         /**
          * The constructor for <code>InstructionCountTimeout</code> creates a timeout event with the specified
          * initial value.
          *
          * @param t the number of cycles in the future
          */
         public ClockCycleTimeout(long t) {
             timeout = t;
         }
 
         /**
          * The <code>fire()</code> method is called when the timeout is up. It gathers the state from the
          * simulator and throws an instance of <code>Simulator.TimeoutException</code> that signals that the
          * timeout has been reached. This exception then falls through the <code>run()</code> method of the
          * caller of the simulator.
          */
         public void fire() {
             int pc = interpreter.getPC();
             throw new TimeoutException(interpreter.getInstr(pc), pc, interpreter, timeout, "clock cycles");
         }
 
     }
 
 }
