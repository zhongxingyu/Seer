 // Gate.java - class with information/methods for a single gate.
 // Copyright (C) 2004 Dahlia Malkhi, Yaron Sella. 
 // See full copyright license terms in file ../GPL.txt
 
 package SFE.BOAL;
 
 import java.io.*;
 import java.util.*;
 import org.apache.log4j.*;
 
 /**
  * This class holds information on a single gate
  * @author  : Dahlia Malkhi and Yaron Sella
  */
 public class Gate implements Serializable {
 	private static final long serialVersionUID = -3834046572803832410L;
 	private static final Logger logger = Logger.getLogger(Gate.class);
     public static final int REG_GATE = 1;
     public static final int INP_GATE = 2;
     public static final int NBYTESG = 10;
     final int PPOS = 19;
     final int MAX_INPUTS = 20;
     int n_inputs; // # of inputs 
     public int gate_index; // index in the circuit
     int gate_type;
     boolean alice_io;
     boolean bob_io;
     boolean out_gate;
     Gate[] in_gates; // keeps the gate-number per input 
     BitSet truth_table; // 2^(n_inputs) binary output values, which
                         // define the output of this gate per input
     int value; // records the gate's output value during evaluation
 
     // Garbling related data
     byte[] code0;
     byte[] code1;
     byte[] hcode0;
     byte[] hcode1;
     byte perm; // 0 - not permuted, 1 - permuted
     Vector encrypted_truth_table;
     byte[] encrypted_perm;
     byte[] garbled_value = null;
 
     // records the gate's garbeled output value during
     // evaluation of the garbeled circuit.
     byte garbled_perm;
 
     //---------------------------------------------------------------
 
     /**
      * Constructor for class Gate
      *
      * @param g_idx index of gate within the circuit.
      * @param n_ins number of input wires for this gate.
      * @param type the type of this gate (e.g., input/regular).
      * @param out_gate true if this gate goes to output.
      */
     public Gate(int g_idx, int n_ins, int type, boolean out_gate) {
         assert (n_ins <= MAX_INPUTS) : "Gate (constructor): too many inputs for gate";
 
         this.gate_type = type;
         this.out_gate = out_gate;
 
         alice_io = false;
         bob_io = false;
 
         gate_index = g_idx;
         n_inputs = n_ins;
         in_gates = new Gate[MAX_INPUTS];
         truth_table = new BitSet(1 << n_inputs);
         value = -1;
 
         // Fill in garbling related   
         code0 = new byte[NBYTESG];
         code1 = new byte[NBYTESG];
         hcode0 = null;
         hcode1 = null;
 
         MyUtil.randomBytes(code0);
         MyUtil.randomBytes(code1);
         perm = (byte) (MyUtil.randomByte() & 0x01);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Record the specified input gate number at the designated input
      * index.
      *
      * @param ind Index of input wire
      * @param in_gate Gate from which input is taken
      */
     public void setInput(int ind, Gate in_gate) {
         assert (in_gate != null) : "setInput: in_gate is null!";
         assert (isInput() && (ind == 0)) || ((ind >= 0) && (ind < n_inputs)) : "setInput: bad input_index = " +
         ind;
 
         in_gates[ind] = in_gate;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Record the specified output value at the designated input
      * index.
      *
      * @param ind Index of input
      * @param val binary output value
      */
     public void setOutput(int ind, boolean val) {
         assert ((ind >= 0) && (ind < truth_table.size())) : "setOutput: bad output_index = " +
         ind;
 
         truth_table.set(ind, val);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Force the specified value as output value of this gate.
      * It is used for setting the value of input gates.
      *
      * @param bit The output value to set.
      */
     public void setValue(int bit) {
         logger.debug("setValue: setting gate value to " + bit);
         value = bit;
         garbled_value = getCode(bit);
         garbled_perm = (byte) (perm ^ bit);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Mark an input gate as being Alice's or Bob's
      *
      * @param alice true if it's Alice's input
      */
     public void markAliceBob(boolean alice) {
         alice_io = alice;
         bob_io = !alice;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Force the specified value as output value of this gate.
      * It is used for setting the value of input gates.
      *
      * @param packed_code The code value to set.
      */
     public void setPackedCode(byte[] packed_code) {
         logger.debug("setPackedCode: setting gate value");
 
         garbled_value = new byte[NBYTESG];
 
         garbled_perm = (byte) (packed_code[0] - 1);
 
         for (int i = 0; i < NBYTESG; i++)
             garbled_value[i] = packed_code[i + 1];
 
         logger.debug("decrypted " + MyUtil.toHexString(packed_code));
     }
 
     //---------------------------------------------------------------
 
     /**
      * @return Is it an input gate?
      */
     public boolean isInput() {
         return (gate_type == INP_GATE);
     }
 
     //---------------------------------------------------------------
 
     /**
      * @return Is it an input gate of Alice?
      */
     public boolean isAliceInput() {
         return ((gate_type == INP_GATE) && alice_io);
     }
 
     //---------------------------------------------------------------
 
     /**
      * @return Is it an input gate of Bob?
      */
     public boolean isBobInput() {
         return ((gate_type == INP_GATE) && bob_io);
     }
 
     //---------------------------------------------------------------
 
     /**
      * @return Is it an output gate?
      */
     public boolean isOutput() {
         return (out_gate);
     }
     
     public void setOutput(boolean out_gate) {
     	this.out_gate=out_gate;
     }
  
     //---------------------------------------------------------------
 
     /**
      * @return Is it an output gate of Alice?
      */
     public boolean isAliceOutput() {
         return (out_gate && alice_io);
     }
     //---------------------------------------------------------------
 
     /**
      * @return Is it an output gate of Bob?
      */
     public boolean isBobOutput() {
         return (out_gate && bob_io);
     }
 
     //---------------------------------------------------------------
 
     /**
      * @return Is this gate correct?
      */
     public boolean isCorrect () {
         int n_ent = (1 << n_inputs); // n_ent = 2^(n_inputs)
         int i, j, permuted_index, count, alice_val, bob_val = (-1);
 		byte[] dec_key, cipher, code;
 	
 		if (isInput()) return (true);
 
         // Loop enumerates through all possible input values
         for (i = 0; i < n_ent; i++) {
 
 		    // Calculate value according to Alice
 		    // ==================================
 	
 	        if (truth_table.get(i))
                 alice_val = 1;
 		    else
                 alice_val = 0;
 	
 		    // Calculate value according to Bob
 		    // ================================
 	
 		    // Find permuted_index and decryption key
             permuted_index = genPermIndex (i);
             dec_key = genKey (i, permuted_index);
 
             // Decrypt
             cipher = (byte[]) (encrypted_truth_table.get(permuted_index));
             code = MyUtil.decArrays(cipher, dec_key);
 
             // Interpret result according to Bob
             for (j = count = 0; j < code0.length; j++)
                 if (code[j] == code0[j]) count += 1;
             if (count == code0.length) bob_val = 0;
 
             for (j = count = 0; j < code1.length; j++)
                 if (code[j] == code1[j]) count += 1;
             if (count == code1.length) bob_val = 1;
 
 		    // Sanity check - this should never happen
 		    if (bob_val == -1) {
                 logger.error("Entry no. " + i + ": evaluation for Bob failed!");
                 return false;
 		    }
 
             if (bob_val != alice_val) {
                logger.error("Entry no. " + i + ": alice_val = " + alice_val + ", bob_val = " + bob_val);
                return false;
             }
         }
         return true;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Provide a garbeling code for the designated binary value
      * of code0/code1 concatenated with garbled perm (for the
      * purpose of transferring both in a single OT)
      *
      * @param bit the desginated binary value (integer) to be garbled.
      * @return a byte array containing the garbling code for the
      * designated binary value concatenated with garbled perm
      */
     public byte[] getPackedCode(int bit) {
         assert ((bit == 0) || (bit == 1)) : "getPackedCode: input isn't a bit!";
 
         byte[] packed_code = new byte[NBYTESG + 1];
         int i;
 
         if (bit == 0) {
             packed_code[0] = (byte) (perm + 1); // +1 to escape the leading 0 problem
 
             for (i = 0; i < NBYTESG; i++)
                 packed_code[i + 1] = code0[i];
         } else {
             packed_code[0] = (byte) ((perm ^ 1) + 1); // +1 to escape the leading 0 problem
 
             for (i = 0; i < NBYTESG; i++)
                 packed_code[i + 1] = code1[i];
         }
 
         return packed_code;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Provide a garbeling code for the designated binary value.
      *
      * @param bit the desginated binary value (integer) to be garbled.
      * @return a byte array containing the garbling code for the
      * designated binary value.
      */
     public byte[] getCode(int bit) {
         byte[] res;
         assert ((bit == 0) || (bit == 1)) : "getCode: input isn't a bit!";
 
         if (bit == 0) {
             if (code0 == null) return(null);
             res = new byte[code0.length];
             for (int i = 0 ; i < code0.length ; i++)
                 res[i] = code0[i];
         } else {
             if (code1 == null) return(null);
             res = new byte[code1.length];
             for (int i = 0 ; i < code1.length ; i++)
                 res[i] = code1[i];
         }
         return (res);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return the value of this gate (after it was evaluated)
      *
      * @return a 0/1 value which is the output of this gate.
      */
     public int getValue() {
         if ((value != 0) && (value != 1)) {
             logger.error("getValue: called on un-evaluated gate") ;
 		    return (0) ;
 		}
 
         return (value);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return garbled value of this gate (after it was evaluated)
      *
      * @return a garbeled value which is the garbeled output of
      * this gate.
      */
     public byte[] getGarbledValue() {
         assert (garbled_value != null) : "getGarbledValue: called on un-evaluated gate";
 
         return (garbled_value);
     }
     //---------------------------------------------------------------
 
     /**
      * Set the garbled value of this gate and interpret it
      * (in practice - only for Bob output gates).
      *
      * @param gdataCode byte array containing garbeled value to set.
      */
     public void setGarbledValue(byte[] gdataCode) {
         assert (gdataCode.length !=  NBYTESG) : "setGarbledValue: input byte-array too short";
 
         garbled_value = new byte[NBYTESG];
 
         for (int i = 0 ; i < gdataCode.length ; i++)
         	garbled_value[i] = gdataCode[i];
 
         value = interpretCode();
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return a premutated index corresponding to this gate
      * for a given index into this gate's truth table.
      * The calculation is based on the permutations of all the
      * gates that contribute input to this gate.
      *
      * @param index - An index into the gate's truth table
      * @return permuted index
      */
     public int genPermIndex(int index) {
         int i;
         int permuted_index;
 
         assert (index < (1 << n_inputs)) : "genPermIndex: index >= 2^n_inputs (too big)";
 
         for (i = permuted_index = 0; i < n_inputs; i++)
             // Isolate relevant bit, flip it if required (perm==1),
             // and put it back in place.
             permuted_index |= ((((index >> i) & 1) ^ in_gates[i].perm) << i);
 
         return (permuted_index);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return an enc/dec mask corresponding to this gate
      * for a given index into this gate's truth table
      * If index is negative then the enc/dec mask is calculated based 
      * on this gate's evaluated garbled inputs.
      *
      * @param index - An index into the truth table of the gate 
      * @param permuted_index - Like index, but permuted
      * @return byte array of encryption key
      */
     public byte[] genKey(int index, int permuted_index) {
         int i, bit;
         byte[] key = null; 
 
         assert (index < (1 << n_inputs)) : "gen_key: index >= 2^n_inputs (too big)";
 
         MyUtil.md.reset();
         MyUtil.md.update(MyUtil.toByteArray(gate_index));
         MyUtil.md.update(MyUtil.toByteArray(permuted_index));
 
        if (n_inputs == 0) {
        	key = MyUtil.md.digest();
        } else {
        	for (i = 0 ; i < n_inputs ; i++) {
	            if (index >= 0) {
	                bit = (index >> i) & 1;
	                MyUtil.md.update(in_gates[i].getCode(bit));
	            } else
	                MyUtil.md.update(in_gates[i].garbled_value);
	
	            if (i == 0) key = MyUtil.md.digest();
	            else        key = MyUtil.xorArrays (key, MyUtil.md.digest());
	        }
         }
 
         return key;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return an encrypted permutation corresponding to this
      * gate for a given index into this gate's truth table.
      *
      * @param index - An index into the gate's truth table
      * @param enc_key - An encryption key (one time pad).
      * @return encrypted perm
      */
     public byte generateEncPerm(int index, byte enc_key) {
         byte output_bit_value;
         byte permuted_output;
         byte enc_permuted_output;
 
         if (truth_table.get(index)) {
             output_bit_value = 1;
         } else {
             output_bit_value = 0;
         }
 
         permuted_output = (byte) (output_bit_value ^ perm);
         enc_permuted_output = (byte) (permuted_output ^ enc_key);
 
         return (enc_permuted_output);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Return an encrypted value corresponding to this
      * gate for a given index into this gate's truth table.
      *
      * @param index - An index into the gate's truth table
      * @param enc_key - encryption key for this entry
      * @return byte array of encrypted value
      */
     public byte[] generateEncOutput(int index, byte[] enc_key) {
         byte[] plain;
         byte[] encrypted_value;
         int output_bit_value;
 
         if (truth_table.get(index)) {
             output_bit_value = 1;
         } else {
             output_bit_value = 0;
         }
 
         plain = getCode(output_bit_value);
         encrypted_value = MyUtil.encArrays(plain, enc_key);
 
         return (encrypted_value);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Fill in all encrypted values and perms of a gate
      */
     public void generateEncGate() {
         int i;
         int permuted_index;
         byte[] enc_key;
 
 		// !!!Added by FairplayPF
         MyUtil.randomBytes(code0);
         MyUtil.randomBytes(code1);
         perm = (byte) (MyUtil.randomByte() & 0x01);        
         
         if (isInput()) {
             return;
         }
 
         logger.debug("generateEncGate: encrypt gate with " + n_inputs +
             " inputs");
 
         int n_ent = (1 << n_inputs); // n_ent = 2^(n_inputs)
 
         // Create vector and array + fill vector with dummy data
         encrypted_truth_table = new Vector(n_ent);
         encrypted_perm = new byte[n_ent];
 
         for (i = 0; i < n_ent; i++)
             encrypted_truth_table.add(i, null);
 
         // Loop enumerates through all possible input values
         for (i = 0; i < n_ent; i++) {
 
             // create encrypted garbled value for specific input
             permuted_index = genPermIndex(i);
             enc_key = genKey (i, permuted_index);
             encrypted_truth_table.set(permuted_index, generateEncOutput(i, enc_key));
 
             // create encrypted garbled perm
             encrypted_perm[permuted_index] = generateEncPerm(i, enc_key[PPOS]);
             logger.debug("generateEncGate: i = " + i + " - encrypted_perm[" +
                 permuted_index + "] = " + encrypted_perm[permuted_index]);
         }
 
         if (isAliceOutput()) {
             hcode0 = MyUtil.hash(code0, NBYTESG);
             hcode1 = MyUtil.hash(code1, NBYTESG);
         }
     }
 
     //---------------------------------------------------------------
 
     /**
      * Measure size of encrypted payload in an encrypted gate
      *
      * @return int - gate's encrypted payload size in bytes
      */
     public int gmeasureEncPayload() {
         int n_ent, total_size;
         byte[] temp;
 
         if (isInput()) {
             return 0;
         }
 
         n_ent = encrypted_truth_table.size();
 
         // Count garbled perm bytes
         total_size = n_ent ; 
 
         // Count all bytes in garbled truth table
         temp = (byte[]) encrypted_truth_table.elementAt(0) ;
         total_size += n_ent * temp.length ;
 
         if (isAliceOutput()) // Count hashed codes bytes
             total_size += hcode0.length + hcode1.length;
 
         return (total_size);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Extract encrypted payload from an encrypted gate.
      *
      * @return a byte array with the relevant information.
      */
     public byte[] gextractEncPayload() {
         int i, j, k = 0, n_ent;
         int total_size;
         byte[] temp;
 
         total_size = gmeasureEncPayload();
         if (total_size == 0) return null;
 
         n_ent = encrypted_truth_table.size() ;
 
         // Create a result byte array with the appropriate size
 
         byte[] res = new byte[total_size];
 
         // Now gather all the data into a result byte array
 
         for (i = 0; i < n_ent; i++, k++) // Encrypted perm bytes
             res[k] = encrypted_perm[i];
 
         for (i = 0; i < n_ent; i++) { // Encrypted truth table
             temp = (byte[]) encrypted_truth_table.elementAt(i) ;
 	        for (j = 0; j < temp.length; j++, k++)
 	            res[k] = temp[j];
         }
 
 		if (isAliceOutput()) { // Hashes of Alice codes
 		   for (j = 0; j < hcode0.length; j++, k++)
                res[k] = hcode0[j];
 		   for (j = 0; j < hcode1.length; j++, k++)
                res[k] = hcode1[j];
         }
            
         return res;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Inject encrypted payload into a gate.
      *
      * @return an integer specifying how many bytes this gate consumed
      */
     public int ginjectEncPayload(byte[] info, int start) {
         int i, j, n_ent, k = start;
 
         if (isInput()) {
             return 0;
         }
         
         // Create vector and array of the correct size
 
         n_ent = 1 << n_inputs;
         encrypted_truth_table = new Vector(n_ent);
         encrypted_perm = new byte[n_ent];
 
         // Inject all data into appropriate structures
 
         for (i = 0 ; i < n_ent; i++, k++) // Encrypted perm bytes
             encrypted_perm[i] = info[k];
 
         for (i = 0; i < n_ent; i++) { // Encrypted truth table
             byte[] temp = new byte[NBYTESG] ;
 		    for (j = 0; j < NBYTESG; j++, k++)
 		         temp[j] = info[k] ;
             encrypted_truth_table.add(temp) ;
         }
 
 		if (isAliceOutput()) { // Hashes of Alice codes
             hcode0 = new byte[NBYTESG];
 		    for (j = 0; j < NBYTESG; j++, k++)
                hcode0[j] = info[k] ;
             hcode1 = new byte[NBYTESG];
 		    for (j = 0; j < NBYTESG; j++, k++)
                hcode1[j] = info[k] ;
         }
            
         return (k - start); // Number of bytes consumed by this gate
     }
 
     //---------------------------------------------------------------
 
     /**
      * Measure size of secret payload in an encrypted gate
      *
      * @return int - gate's secret payload size in bytes
      */
     public int gmeasureSecPayload() {
 
         return (2*NBYTESG+1);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Extract secret payload from an encrypted gate.
      *
      * @return a byte array with the relevant information.
      */
     public byte[] gextractSecPayload() {
         int i, k = 0 ;
 
         // Create a result byte array with the appropriate size
 
         byte[] res = new byte[gmeasureSecPayload()];
 
         // Gather all the data into a result byte array
 
 		for (i = 0; i < NBYTESG; i++, k++) // Code0
 	        res[k] = code0[i];
 		for (i = 0; i < NBYTESG; i++, k++) // Code1
 	        res[k] = code1[i];
         res[k] = perm;
 
         return res;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Inject secret payload into a gate.
      *
      * @return an integer specifying how many bytes this gate consumed
      */
     public int ginjectSecPayload(byte[] info, int start) {
         int i, k = start;
 
         code0 = new byte[NBYTESG];
         code1 = new byte[NBYTESG];
 
         // Inject all data into appropriate structures
 
 		for (i = 0 ; i < NBYTESG; i++, k++) // code0
 	        code0[i] = info[k];
 		for (i = 0 ; i < NBYTESG; i++, k++) // code1
 	        code1[i] = info[k];
         perm = info[k];
         k += 1;
 
         return (k - start); // Number of bytes consumed by this gate
     }
 
     //---------------------------------------------------------------
 
     /**
      * Measure size of input payload in an encrypted gate
      *
      * @return int - gate's input payload size in bytes
      */
     public int gmeasureInpPayload() {
 
         if (!isInput()) return(0);
         return (NBYTESG+1);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Extract input payload from an encrypted gate.
      *
      * @return a byte array with the relevant information.
      */
     public byte[] gextractInpPayload() {
         int i, k = 0 ;
         int total_size;
 
         total_size = gmeasureInpPayload();
         if (total_size == 0) return null;
 
         // Create a result byte array with the appropriate size
 
         byte[] res = new byte[total_size];
 
         // Gather all the data into a result byte array
 
 		for (i = 0; i < NBYTESG; i++, k++) // garbled_value
             res[k] = garbled_value[i];
 		res[k] = garbled_perm; // garbled_perm
 
         return res;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Inject input payload into a gate.
      *
      * @return an integer specifying how many bytes this gate consumed
      */
     public int ginjectInpPayload(byte[] info, int start) {
         int i, k = start;
 
         garbled_value = new byte[NBYTESG];
 
         // Inject all data into appropriate structures
 
 		for (i = 0 ; i < NBYTESG; i++, k++) // garbled_value
             garbled_value[i] = info[k];
         garbled_perm = info[k];
         k += 1;
 
         return (k - start); // Number of bytes consumed by this gate
     }
 
     //---------------------------------------------------------------
 
     /**
      * Measure size of output payload in an encrypted gate
      *
      * @return int - gate's input payload size in bytes
      */
     public int gmeasureOutPayload() {
 
         if (!isOutput()) return(0);
         return (NBYTESG);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Extract output payload from an encrypted gate.
      *
      * @return a byte array with the relevant information.
      */
     public byte[] gextractOutPayload() {
         int total_size;
 
         total_size = gmeasureOutPayload();
         if (total_size == 0) return null;
 
         assert (garbled_value != null) : "gextractInpPayload: called on un-evaluated gate";
 
         return garbled_value;
     }
 
     //---------------------------------------------------------------
 
     /**
      * Inject output payload into a gate.
      *
      * @return an integer specifying how many bytes this gate consumed
      */
     public int ginjectOutPayload(byte[] info, int start) {
         int i, k = start;
 
         garbled_value = new byte[NBYTESG];
 
         // Inject all data into appropriate structures
 
 		for (i = 0 ; i < NBYTESG; i++, k++) // garbled_value
 	        garbled_value[i] = info[k];
         value = interpretCode();
 
         return (k - start); // Number of bytes consumed by this gate
     }
 
     //---------------------------------------------------------------
 
     /**
      * Evaluate this gate and place result in member 'value'.
      */
     public void evalGate() {
         int i;
         int index;
         int bit;
 
         // Do something only if gate hasn't been evaluated yet
         if (value == -1) {
             for (i = index = 0; i < n_inputs; i++) {
                 bit = in_gates[i].getValue();
                 logger.debug("evalGate: Input " + i + " is " + bit);
                 index |= (bit << i);
             }
 
             if (truth_table.get(index)) {
                 value = 1;
             } else {
                 value = 0;
             }
         }
 
         logger.debug("evalGate: output value is " + value);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Evaluate this garbled gate and place result in member
      * garbled_value.
      *
      * @param alice_interpret - interpret Alice garbled output
      * @param bob_interpret - interpret Bob garbled output
      */
     public void evalGarbledGate(boolean alice_interpret, boolean bob_interpret)  {
         int i;
         int permuted_index;
         byte[] dec_key;
         byte[] cipher;
         byte perm_dec_key;
 
         // Do something only if gate hasn't been evaluated yet
         if (garbled_value == null) {
             // Construct permuted_index
             for (i = permuted_index = 0; i < n_inputs; i++)
                 permuted_index |= (in_gates[i].garbled_perm << i);
 
             // Calculate decryption keys
             dec_key = genKey(-1, permuted_index);
             perm_dec_key = dec_key[PPOS];
 
             // Decrypt
             cipher = (byte[]) (encrypted_truth_table.get(permuted_index));
             garbled_value = MyUtil.decArrays(cipher, dec_key);
             garbled_perm = (byte) (encrypted_perm[permuted_index] ^ perm_dec_key);
 
             // Sanity check
             if ((garbled_perm != 0) && (garbled_perm != 1)) {
                logger.warn("evalGarbledGate: garbled_perm isn't a bit! garbled_perm = " + garbled_perm);
 		       garbled_perm = 0;
 		       value = 0;
                return;
             }
 
             // Interpret the output only for relevant output gates
             if ((alice_interpret && isAliceOutput()) ||
                 (bob_interpret && isBobOutput())) {
                 value = interpretCode();
 		    }
         }
 
         logger.debug("evalGarbledGate: output value is " + value);
     }
 
     //---------------------------------------------------------------
 
     /**
      * Print a gate (for debugging only).
      */
     public void printGate() {
         byte[] arr = null;
         int i;
         int j;
 
         if (isInput()) {
             if (alice_io)
                System.out.print("input gate (Alice)");
 		    else if (bob_io)
                System.out.print("input gate (Bob)");
 		    else
                System.out.print("input gate (unknown)");
         }
 
 		if (encrypted_truth_table == null)
             System.out.println("ett = null");
 		else {
            for (i = 0; i < encrypted_truth_table.size(); i++) {
                System.out.print(" ett_entry[" + i + "] = ");
                arr = (byte[]) encrypted_truth_table.get(i);
 
                if (arr == null) {
                    System.out.println("null");
                } else {
                    for (j = 0; j < arr.length; j++)
                        System.out.print(arr[j]);
 
                    System.out.println("");
                }
 
 		       if (truth_table != null)
                    System.out.println("tt_entry[" + i + "] = " + truth_table.get(i));
            }
        }
 
        if (garbled_value == null)
            System.out.println(" garbled_value = null");
        else {
            System.out.print(" garbled_value = ");
            for (i = 0; i < garbled_value.length; i++)
                System.out.print(garbled_value[i]);
            System.out.println("");
        }
     }
 
     //---------------------------------------------------------------
 
     /**
      * Interpret a garbled code back to a bit (only for output gates)
      * Note - different treatment for Alice and Bob outputs:
      *  evaluation for Alice output against a hash of the garbled value,
      *  evaluation for Bob output against the garbled value itself.
      *
      * @return a bit (0/1)
      */
     public int interpretCode() {
         int i, count;
         byte[] code;
 
         // Evaluation for Alice output against a hash of the garbled value
         if (isAliceOutput()) {
             code = MyUtil.hash (garbled_value, NBYTESG);
 
             for (i = count = 0; i < hcode0.length; i++)
                 if (code[i] == hcode0[i]) count += 1;
             if (count == hcode0.length) return (0);
 
             for (i = count = 0; i < hcode1.length; i++)
                 if (code[i] == hcode1[i]) count += 1;
             if (count == hcode1.length) return (1);
         }
         // Evaluation for Bob output against the garbled value itself
         else if (isBobOutput()) {
             code = garbled_value;
 
             for (i = count = 0; i < code0.length; i++)
                 if (code[i] == code0[i]) count += 1;
             if (count == code0.length) return (0);
 
             for (i = count = 0; i < code1.length; i++)
                 if (code[i] == code1[i]) count += 1;
             if (count == code1.length) return (1);
 		}
 	
 		// This code should never be reached
         logger.error ("interpretCode: failed to interpret code");
 
         return (-1); // Can never be reached
     }
 
     public String toString() {
     	String s="index="+gate_index+" n_inputs="+n_inputs+" gate_type="+gate_type+" [";
     	for(int i=0; i<in_gates.length; i++) s+=(in_gates[i]!=null?" "+in_gates[i].gate_index:"");
     	s+=" ]";
     	if(garbled_value!=null) {
     	  s+=" garbled=["+ garbled_value[0]+"...]";
     	}
     	else {
     		s+=" 0:["+code0[0]+"...] 1:["+code1[0]+"...]";
     	}
     	return s;
     }
     //---------------------------------------------------------------
 
     /** Obsolete
      * @return Is this gate logically equivalent to a garbled gate?
      *
     public boolean isEquivGate(Gate gg) {
 
        if (gate_type != gg.gate_type) return false;
        if (out_gate!= gg.out_gate) return false;
        if (bob_io != gg.bob_io) return false;
        if (alice_io != gg.alice_io) return false;
        if (n_inputs != gg.n_inputs) return false;
 
        for (int i = 0; (gg.n_inputs > 0) && (i < gg.encrypted_truth_table.size()); i++) {
           if  (n_inputs != gg.n_inputs) return false;
        }
 
        return true;
     }
     */
 
     //---------------------------------------------------------------
 
     /** Obsolete
      * Duplicate the relevant data of a gate. 
      *
      * @return a duplicate gate
      *
     public Gate duplicateGate() {
 
         Gate new_gate = new Gate(n_inputs, gate_type, out_gate);
 
 		if (code0 != null) {
            new_gate.code0 = new byte[code0.length];
 		   for (int i = 0 ; i < code0.length ; i++)
                new_gate.code0[i] = code0[i];
 		}
 
 		if (code1 != null) {
            new_gate.code1 = new byte[code1.length];
 		   for (int i = 0 ; i < code1.length ; i++)
                new_gate.code1[i] = code1[i];
 		}
 
         new_gate.perm = this.perm;
 		new_gate.bob_io = this.bob_io;
 		new_gate.alice_io = this.alice_io;
 
 		return (new_gate);
     }
     */
 
     //---------------------------------------------------------------
 
     /** Obsolete
      * Clean a gate from its secret data (before Bob sends the
      * circuit to Alice).
      *
      * @return a copy of the data that was cleaned
      *
     public Gate cleanGate() {
 
         Gate new_gate = duplicateGate();
 
         // Set these fields to meaningless values
         perm = (-1);
 		truth_table = null;
         code0 = null;
         code1 = null;
 
 		return (new_gate);
     }
     */
 
     //---------------------------------------------------------------
 
     /** Obsolete
      * Restore the secret data of a gate from a given gate.
      *
      * @param g a gate to restore the secret data from.
      *
     public void restoreGate(Gate g) {
 
 		if (g.code0 != null) {
            this.code0 = new byte[g.code0.length];
 		   for (int i = 0 ; i < g.code0.length ; i++)
                code0[i] = g.code0[i];
 		}
 
 		if (g.code1 != null) {
            this.code1 = new byte[g.code1.length];
 		   for (int i = 0 ; i < g.code1.length ; i++)
                code1[i] = g.code1[i];
 		}
 
         perm = g.perm;
 		bob_io = g.bob_io;
 		alice_io = g.alice_io;
     }
     */
 
     //---------------------------------------------------------------
 
     /** Obsolete
      * Copy the garbled information from a given gate into this gate.
      *
     public void copyGarbledInfo (Gate g) {
 
         this.garbled_perm = g.garbled_perm;
 		int len = g.garbled_value.length;
 		this.garbled_value = new byte[len];
 
 		for (int i = 0 ; i < g.garbled_value.length ; i++)
            this.garbled_value[i] = g.garbled_value[i];
     }
     */
 }
