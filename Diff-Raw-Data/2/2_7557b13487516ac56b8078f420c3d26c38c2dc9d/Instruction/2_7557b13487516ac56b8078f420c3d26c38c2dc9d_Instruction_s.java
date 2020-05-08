 import java.util.*;
 import java.lang.*;
 
 public abstract class Instruction {
    public static Integer operandCount = 0;
    private ArrayList<Operand> operands = new ArrayList<Operand>();
    private ArrayList<Register> dests = new ArrayList<Register>();
    private ArrayList<Register> srcs = new ArrayList<Register>();
 
    protected ArrayList<String> sparcs = new ArrayList<String>();
 
    public String toString() {
       return "NULL INSTRUCTION.";
    }
 
    public String toILOC() {
       return "noop";
    }
 
    public ArrayList<Sparc> toSparc() {
       ArrayList<Sparc> instructions = new ArrayList<Sparc>();
 
       for (String instr : this.sparcs) {
          String cap = instr.toUpperCase();
          String classname = cap.substring(0,1) + instr.substring(1) + "Sparc";
 
          try {
             Class cls = Class.forName(classname);
             Sparc i = (Sparc)cls.newInstance();
 
             // TODO: Need to add code here to deal with Immediates > 13 bits.
             if (this.sparcs.size() == 1) {
 
                // Conditional moves need a %icc
                if (instr.length() > 3 && instr.substring(0,3).equals("mov")) {
                   i.addSource(new ConditionCodeRegister());
                }
 
                for (Operand o : this.getOperands()) {
                   // Don't write out %icc for cmp.
                   if (!(instr.equals("cmp") && 
                    o instanceof ConditionCodeRegister)) {
                      i.addOp(o);
                   }
                }
             }
 
             // To deal with jumpi basically.
             if (instr.equals("ba") && this.sparcs.size() <= 2) {
               for (Operand o : this.getSources()) {
                   i.addSource(o);
                }
             }
 
             instructions.add(i);
          } catch (ClassNotFoundException e) {
             Evil.error("No such class " + classname 
              + ": " + e.getMessage());
 
          } catch (InstantiationException e) {
             Evil.error("Could not instantiate object " 
              + classname + ": " + e.getMessage());
          } catch (IllegalAccessException e) {
             Evil.error("Could not access constructor for object " 
              + classname + ": " + e.getMessage());
          }
       }
 
       // Conditional Branches.
       String firstthree = this.getClass().getName().toLowerCase().substring(0,3);
       if (firstthree.equals("cbr")) {
          instructions.get(0).addOp(this.getOperands().get(1));
          instructions.get(2).addOp(this.getOperands().get(2));
       } else if (this instanceof CallInstruction) {
          instructions.get(0).addOp(this.getOperands().get(0));
       }
 
       return instructions;
    }
 
    public ArrayList<Register> getDestinations() {
       return this.dests;
    }
 
    public ArrayList<Register> getSources() {
       return this.srcs;
    }
 
    public ArrayList<Operand> getOperands() {
       return this.operands;
    }
 
    /**
     * Add a register to the source and operand list.
     */
    public void addSource(Register in) {
       if (in instanceof ConditionCodeRegister) {
          System.out.println(this + " has it!");
       }
       this.srcs.add(in);
       this.operands.add(in);
    }
 
    /**
     * Add a register the the destination and operand list.
     */
    public void addDest(Register in) {
       if (in instanceof ConditionCodeRegister) {
          System.out.println(this + " has it!");
       }
       this.dests.add(in);
       this.operands.add(in);
    }
 
    /**
     * The rest of the addX functions are wrappers around addOp.
     */
    public void addOp(Operand op) {
       this.operands.add(op);
    }
 
    public void addLabel(Label in) {
       this.addOp(in);
    }
 
    public void addLabel(String in) {
       this.addOp(new Label(in));
    }
 
    public void addID(String in) {
       this.addOp(new ID(in));
    }
 
    public void addField(Field in) {
       this.addOp(in);
    }
 
    public void addRegister(Register in) {
       this.addOp(in);
    }
 
    public void addImmediate(Immediate in) {
       this.addOp(in);
    }
 
    public void addImmediate(Integer in) {
       this.addOp(new Immediate(in));
    }
 
    /**
     * Instead of having a parent class for conditional moves.
     */
    public boolean isConditionalMove() {
       return this instanceof MoveqInstruction
        || this instanceof MovgeInstruction
        || this instanceof MovgtInstruction
        || this instanceof MovleInstruction
        || this instanceof MovleInstruction
        || this instanceof MovneInstruction;
    }
 
    /**
     * Instead of having a parent class for call-like functions.
     */
    public boolean isCall() {
       return this instanceof CallInstruction
        || this instanceof PrintInstruction
        || this instanceof PrintlnInstruction
        || this instanceof ReadInstruction
        || this instanceof NewInstruction;
    }
 
    public void transformRegisters(Map<Register, Register> allocations) {
       Register virtual, real;
       int ndx = 0;
 
       // Transform operands list.
       for (Operand op : this.operands) {
          if (op instanceof Register && !(op instanceof ConditionCodeRegister)) {
             virtual = (Register) op;
 
             real = allocations.get(virtual);
 
             if (real != null) {
                operands.set(ndx, real);
             } else {
                Evil.warning("No mapping for register " + virtual + ".");
             }
          }
 
          ndx++;
       }
 
       // Transform destination list.
       for (ndx = 0; ndx < dests.size(); ndx++) {
          virtual = dests.get(ndx);
          real = allocations.get(virtual);
 
          if (real != null) {
             dests.set(ndx, real);
          } else {
             Evil.warning("No mapping for register " + virtual + ".");
          }
       }
 
       // Transform sources list.
       for (ndx = 0; ndx < srcs.size(); ndx++) {
          virtual = srcs.get(ndx);
          real = allocations.get(virtual);
 
          if (real != null) {
             srcs.set(ndx, real);
          } else {
             Evil.warning("No mapping for register " + virtual + ".");
          }
       }
    }
 }
