 import java.util.Enumeration;
 import java.util.LinkedHashMap;
 
 ///
 ///This class provides all register functionality for
 ///integer and floating point register files.
 ///
 
 public class RegisterFiles{
      public static final int NUM_INT_REGISTERS = 32;  ///< Total number of integer registers-- not including R0.
      public static final int NUM_FP_REGISTERS  = 32;  ///< Total number of floating point registers.
 
      private LinkedHashMap<String, String> registers_int; ///< Consists of all integer registers.
      private LinkedHashMap<String, String> registers_fp;  ///< Consists of all floating point registers.
      
      ///
      ///Constructs the register files; initializes all floating point and integer registers to "0";
      ///
      public RegisterFiles(){
           registers_int = new LinkedHashMap<String, String>();
           registers_fp  = new LinkedHashMap<String, String>();
           
           //Initialize R0
          registers_int.put( "R0", "0" );
           
           //Initialize the Integer Registers R1 to Rn
           for( int i = 0; i < NUM_INT_REGISTERS; i++ ){
                String temp_reg =  "R" + ( i + 1 );
                registers_int.put( temp_reg, ""  );
           }
           
           //Initialize the FP Registers f0 to Fn
           for( int i = 0; i < NUM_FP_REGISTERS; i++ ){
                String temp_reg =  "F" + (  2*i  );
                registers_fp.put( temp_reg, "" );
           }
      }
      
      ///
      ///Generates a copy of an existing RegisterFiles object.
      ///
      public RegisterFiles( RegisterFiles to_copy ){
           registers_int = new LinkedHashMap<String, String>();
           registers_fp  = new LinkedHashMap<String, String>();
 
           //Copy Integer and FP Register Files
           this.registers_int = new LinkedHashMap<String, String>( to_copy.registers_int ) ;
           this.registers_fp  = new LinkedHashMap<String, String>( to_copy.registers_fp ) ;
      }
      
      ///
      /// Returns a LinkedHashMap that conatins a copy of the current integer register file.
      ///
      public LinkedHashMap<String, String> getIntegerRegisters(){
           return new LinkedHashMap<String, String>( registers_int );
      }
 
      ///
      /// Returns a LinkedHashMap that conatins a copy of the current floating point register file.
      ///
      public LinkedHashMap<String, String> getFPRegisters(){
           return new LinkedHashMap<String, String>( registers_fp );
      }
      
      ///
      ///Returns the current value of the specified register. An "Invalid register ID" exception is thrown if 
      ///the specified register does not exist. 
      ///
      public String getRegister( String r_id ) /*throws Exception*/{
           String to_return="";
           
           if( r_id.charAt(0) == 'R' ){
                to_return = registers_int.get( r_id );
           }
           else if( r_id.charAt(0) == 'F' ){
                to_return = registers_fp.get( r_id );
           }
           /*else{
                throw new Exception(){
                     public String toString(){
                          return "Invalid Register ID.";
                     }
                };
           }*/
           
           if( to_return.equals("") ){
                to_return = r_id;
           }
           
           return to_return;
      }
      
      ///
      ///Sets a new value for the specified register. An "Invalid register ID" exception is thrown if 
      ///the specified register does not exist. 
      ///
      public void setRegister( String r_id, String val_in ) /*throws Exception*/{
           if( r_id.charAt(0) == 'R' && registers_int.containsKey( r_id ) ){
                registers_int.put( r_id, val_in );
           }
           else if( r_id.charAt(0) == 'F' && registers_fp.containsKey( r_id )){
                registers_fp.put( r_id, val_in );
           }/*
           else{
                throw new Exception(){
                     public String toString(){
                          return "Invalid Register ID.";
                     }
                };
           }*/
           
           //System.out.println( r_id + " " + val_in );
      }
      
      ///
      ///Generates the string representation of the RegisterFiles Object.
      ///
      public String toString(){
           return "Integer Registers: " + NUM_INT_REGISTERS + "\n" +
                  "    Values: \n" + registers_int.toString() + "\n" +
                  "FP Registers:      " + NUM_FP_REGISTERS + "\n" +
                  "    Values: \n" + registers_fp.toString() + "\n" ;
      } 
 }
