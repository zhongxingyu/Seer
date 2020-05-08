 public class Type
 {
  public boolean global = false;
   public boolean isInt()
   {
     return this instanceof IntType;
   }
   public boolean isBool()
   {
     return this instanceof BoolType;
   }
   public boolean isStruct()
   {
     return this instanceof StructType;
   }
   public boolean isFunc()
   {
     return this instanceof FuncType;
   }
 }
