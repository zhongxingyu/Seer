 public class Register implements Operand {
    public static int regCounter = 0;
    public String id = "";
    public StructType type;
    public Integer offset; // Only used in Sparc loads and stores.
 
    public Register() {
       regCounter = regCounter + 1;
       this.id = "r" + regCounter;
    }
 
    public Register(String in) {
       this.id = in;
    }
 
    public StructType getType() {
       return this.type;
    }
 
    public void setType(Type type) {
       // We only keep track of StructTypes.
       if (type instanceof StructType) {
          this.type = (StructType) type;
       }
    }
 
    /**
     * Hack that's nice for the printing of Sparc loads and stores.
     */
    public void addOffset(Integer offset) {
       this.offset = offset;
    }
 
    public String toString() {
       if (this.id.contains("%")) {
          return this.id;
       } else {
          return "%" + this.id;
       }
    }
 
    public String toSparc() {
       if (this.offset != null) {
          if (this.offset == 0) {
             return "[" + toString() + "]";
          } else {
             return "[" + toString() + "+" + this.offset + "]";
          }
       }
 
       return toString();
    }
 
    public String toILOC() {
       return this.toString();
    }
 
    public boolean equals(Object other) {
       if (other instanceof Register) {
          return toString().equals(other.toString());
       }
 
       return false;
    }
 
    public int hashCode() {
       return id.hashCode();
    }
 }
