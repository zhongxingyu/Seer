 package emulator;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 
 public class State {
     
     private ArrayList<Integer> memory = new ArrayList<Integer>();
     private ArrayList<Integer> registers = new ArrayList<Integer>();
     
     // should all be getters-only
     public Boolean isHalted;
     public String output;
     public int numBellRings;
     
     public int pipelineStep;
     public int executionStep;
     
     public Processor processor;
     
     public State( Processor processor ) {
         this.processor = processor;
         
         // because you can't assign past the end of arrays in java
         int i;
         for ( i = 0; i != processor.numMemoryAddresses; i++ ) {
             memory.add( 0 );
         }
         for ( i = 0; i != processor.getNumRegisters(); i++ ) {
             registers.add( 0 );
         }
         
         this.reset();
     }
     
     public State duplicate( ) {
         State newState = new State( this.processor );
         newState.setAllMemory( this.memory );
         newState.setAllRegisters( this.registers );
         
         newState.isHalted = this.isHalted;
         newState.output = this.output;
         newState.numBellRings = this.numBellRings;
         
         newState.pipelineStep = this.pipelineStep;
         newState.executionStep = this.executionStep;
         
         return newState;
     }
     
     public void reset( ) {
         int i;
         for ( i = 0; i != processor.numMemoryAddresses; i++ ) {
             memory.set(i, 0);
         }
         
         for ( i = 0; i != processor.getNumRegisters(); i++ ) {
             registers.set(i, 0);
         }
         
         isHalted = false;
         output = "";
         numBellRings = 0;
         pipelineStep = 0;
         executionStep = 0;
     }
     
     // ensure a value is within the allowed values of this processor
     private int constrainRegister( int value ) {
         int mask = (1 << processor.registerBitSize)-1;
         return (value & mask);
     }
     private int constrainMemory( int value ) {
         int mask = (1 << processor.memoryBitSize)-1;
         return (value & mask);
     }
     private int constrainAddress( int value ) {
         int newValue = value % processor.numMemoryAddresses;
         // wrap around negative addresses
         if ( newValue < 0 ) {
             newValue += processor.numMemoryAddresses; 
         }
         return newValue;
     }
     
     public int getRegister( String registerName ) {
         int registerIndex = processor.registerIndexLookup.get( registerName );
         return constrainRegister( registers.get( registerIndex ) );
     }
     
     public void setRegister( String registerName, int value ) {
         int registerIndex = processor.registerIndexLookup.get( registerName );
         registers.set(registerIndex, constrainRegister( value ));
     }
     
     public int getMemory( int address ) {
         address = constrainAddress( address );
         return constrainMemory( memory.get( address ) );
     }
     
     public void setMemory( int address, int value ) {
         address = constrainAddress( address );
         memory.set( address, constrainMemory( value ) );
     }
     
     public ArrayList<Integer> getAllMemory( ) {
         return new ArrayList<Integer>( memory );
     }
     
     public void setAllMemory( ArrayList<Integer> values ) {
         int i;
         for ( i = 0; i != processor.numMemoryAddresses; i++ ) {
             if ( i < values.size() ) {
                 memory.set( i, constrainMemory( values.get( i ) ) );
             } else {
                 // set rest of memory to 0
                 memory.set( i, 0 );
             }
         }
     }
     
     public void setAllRegisters( ArrayList<Integer> values ) {
         int i;
         for ( i = 0; i != processor.getNumRegisters(); i++ ) {
             if ( i < values.size() ) {
                 registers.set( i, constrainRegister( values.get( i ) ) );
             } else {
                 // set any unspecified registers to 0
                 registers.set(i, 0);
             }
         }
     }
     
     /*
      * Side-Effects
      */
     
     public void print( int value ) {
         output += Integer.toString( value );
        output += " ";
         //System.out.println(Integer.toString( value ));
     }
      
     public void printASCII( int value ) {
         output += (char) value;
         //System.out.println( (char) value );
     }
      
     public void ringBell( ) {
     	//System.out.print("**DING**");
         numBellRings++;
     }
      
     public void halt( ) {
         isHalted = true;
     }
 
     public String toJSON( ) {
     	StringBuilder sb = new StringBuilder( );
     	
     	Set<String> registerNames = processor.registerIndexLookup.keySet( );
     	Iterator<String> registerIterator = registerNames.iterator();
     	Iterator<Integer> memoryIterator = memory.iterator();
     	
     	sb.append("{\n");
     	
     	// registers
     	sb.append("    \"registers\": {\n");
     	while (registerIterator.hasNext()) {
     		String current = registerIterator.next();
     		sb.append("        \"");
     		sb.append(current);
     		sb.append("\": ");
     		sb.append(registers.get(processor.registerIndexLookup.get(current)));
     		
     		if (registerIterator.hasNext()) {
     			sb.append(",");
     		}
     		sb.append("\n");
     	}
     	sb.append("    },\n");
     	
     	// memory
     	sb.append("    \"memory\": [");
     	while (memoryIterator.hasNext()) {
     		Integer current = memoryIterator.next();
     		sb.append(current);
     		
     		if (memoryIterator.hasNext()) {
     			sb.append(", ");
     		}
     	}
     	sb.append("],\n");
     	
     	// isHalted
     	sb.append("    \"isHalted\": ");
     	sb.append(isHalted);
     	sb.append(",\n");
     	
     	// pipeline step
     	sb.append("    \"pipelineStep\": ");
     	sb.append(pipelineStep);
     	sb.append(",\n");
 
     	// output
     	sb.append("    \"output\": \"");
     	sb.append(output);
     	sb.append("\",\n");
 
     	// bell rings
     	sb.append("    \"numBellRings\": ");
     	sb.append(numBellRings);
     	sb.append(",\n");
     	
     	// execution steps
     	sb.append("    \"cycles\": ");
     	sb.append(executionStep);
     	sb.append("\n");
     	
     	sb.append("}");
     	
     	return sb.toString();
     }
 }
