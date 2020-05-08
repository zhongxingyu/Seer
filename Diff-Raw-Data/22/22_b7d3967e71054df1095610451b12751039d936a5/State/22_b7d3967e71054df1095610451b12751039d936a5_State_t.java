 /*
 Copyright (c) 2012, Ulrik Pagh Schultz, University of Southern Denmark
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without
 modification, are permitted provided that the following conditions are met: 
 
 1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer. 
 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution. 
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 The views and conclusions contained in the software and documentation are those
 of the authors and should not be interpreted as representing official policies, 
 either expressed or implied, of the University of Southern Denmark.
 */
 
 package statemachine.year2.framework;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
import java.util.Set;
 
 import statemachine.year1.library.Event;
 
 /**
  * A generic state representation: belongs to a machine, has a name, and a number
  * of transitions.  When an event is delivered, the transitions are tested in the
  * same order as they were initially inserted into the state, the first one that is
  * applicable is performed by executing its effect (if any) and performing the
  * corresponding transition (if any).
  * @author ups
  */
 public class State {
 
     /**
      * The machine to which this state belongs
      */
     private Machine machine;
     /**
      * The name of the state
      */
     private String name;
     /**
      * Transitions associated with this state
      */
     private Map<String,List<Transition>> transitions = new HashMap<String,List<Transition>>();
 
     /**
      * Instantiate state for a given state machine
     * @param machine the state machine to which the state belongs
      * @param name the name of the state
      */
     public State(Machine machine, String name) {
         this.machine = machine;
         this.name = name;
     }
 
     /**
      * Add a transition that is tested in this state when the corresponding event is received
      * @param eventName name of event that can trigger this transition
      * @param transition the transition
      */
     public void addTransition(String eventName, Transition transition) {
         List<Transition> matches = transitions.get(eventName);
         if(matches==null) {
             matches = new ArrayList<Transition>();
             transitions.put(eventName, matches);
         }
         matches.add(transition);
     }
     
     /**
      * Process event by testing isApplicable in sequence, and then performing the action which
      * may have an effect and returns the name of a new state, if a transition is to be made.
      */
     public void processEvent(Event event) {
         List<Transition> matches = transitions.get(event.code());
         if(matches==null) return;
         for(Transition tran: matches)
             if(tran.isApplicable()) { 
                 String newMaybe = tran.action();
                 if(newMaybe!=null) machine().setState(newMaybe);
                 return;
             }
     }
     
     /**
     * Get the machine to which this state belongs
      * @return the machine
      */
     public Machine machine() {
         return machine;
     }
 
     /**
      * Get the name of the state
      */
     public String toString() {
         return this.getName();
     }
 
     /**
      * Get the name of the state
      * @return the name
      */
     public String getName() {
         return name;
     }
    
    /**
     * Get the set of events that can be handled in this state
     */
    public Set<String> getApplicableEvents() {
    	return transitions.keySet();
    }
    
    /**
     * Get the set of transitions that correspond to a given event ID
     */
    public List<Transition> getTransitionsForEvent(String event) {
    	return transitions.get(event);
    }
 }
