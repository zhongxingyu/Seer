 public abstract class Type {
 
    public boolean is_bool() {
       return this instanceof BoolType;
    }
 
    public boolean is_func() {
       return this instanceof FuncType;
    }
 
    public boolean is_int() {
       return this instanceof IntType;
    }
 
    public boolean is_struct() {
       return this instanceof StructType;
    }
 
    public boolean is_void() {
       return this instanceof VoidType;
    }
 
    public boolean is_null() {
       return this instanceof NullType;
    }
 
    public boolean equals(Object obj) {
      return obj.getClass().equals(this.getClass());
    }
 
    public String toString() {
       return this.getClass().getName();
    }
 }
