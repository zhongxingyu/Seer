 public class AddressInstruction extends Instruction
 {
   public Register src;
   public Register dest;
   public String offset;
   
   public AddressInstruction(String name, Register src, Register dest, String offset)
   {
     super(name);
     this.src = src;
     this.dest = dest;
     this.offset = offset;
   }
   
   public String toString()
   {
    return new String(name + " " + src + ", " + dest + ", " + offset);
   }
}
