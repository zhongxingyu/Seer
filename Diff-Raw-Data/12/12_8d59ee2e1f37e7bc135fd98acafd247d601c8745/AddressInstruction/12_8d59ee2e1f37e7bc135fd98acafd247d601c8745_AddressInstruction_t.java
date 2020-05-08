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
    if(name.equals("storeai")) {
      return new String(name + " " + src + ", " + dest + ", " + offset);
    } else if(name.equals("loadai")) {
      return new String(name + " " + src + ", " + offset + ", " + dest);
    } else {
      return new String(name + " " + src + ", " + dest + ", " + offset);
    }
   }
}
