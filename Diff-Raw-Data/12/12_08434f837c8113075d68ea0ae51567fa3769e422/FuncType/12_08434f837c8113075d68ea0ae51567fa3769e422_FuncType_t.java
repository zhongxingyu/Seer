 package types;
 
 public class FuncType extends Type 
 {
    private TypeList args;
    private Type ret;
    
    public FuncType(TypeList args, Type returnType)
    {
       this.args = args;
       this.ret = returnType;
    }
    
    public Type returnType()
    {
       return ret;
    }
    
    @Override
    public Type call(Type that)
    {
 	   // can only call this function if the arguments of that match with the 
	   // arguments of thi
	   return args.equivalent(that) ? ret : super.call(that);
    }
    public TypeList arguments()
    {
       return args;
    }
    
    @Override
    public String toString()
    {
       return "func(" + args + "):" + ret;
    }
 
    @Override
    public boolean equivalent(Type that)
    {
       if (that == null)
          return false;
       if (!(that instanceof FuncType))
          return false;
       
       FuncType aType = (FuncType)that;
       return this.ret.equivalent(aType.ret) && this.args.equivalent(aType.args);
    }
 }
