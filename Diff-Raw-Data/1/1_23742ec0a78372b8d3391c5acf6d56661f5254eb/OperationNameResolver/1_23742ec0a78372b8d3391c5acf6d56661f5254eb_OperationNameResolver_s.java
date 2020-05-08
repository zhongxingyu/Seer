 // Copyright (C) Billy Melicher 2012 wrm2ja@virginia.edu
 package GCParser;
 
 import java.util.*;
 import YaoGC.*;
 import GCParser.Operation.*;
 
 public class OperationNameResolver {
   private static boolean isInitialized = false;
   private static Map<String,OpDirections> resolver = new HashMap<String,OpDirections>();
   public static void initOperations() {
     if( isInitialized )
       return;
     isInitialized = true;
 
     new AddOperation();
     new XorOperation();
     new ConcatOperation();
     new MaxOperation();
     new MinOperation();
     new AndOperation();
     new SubOperation();
     new GteuOperation();
     new GtuOperation();
     new LteuOperation();
     new LtuOperation();
     new GtsOperation();
     new GtesOperation();
     new LtsOperation();
     new LtesOperation();
     new MinsOperation();
     new MaxsOperation();
     new SelectOperation();
     new EquOperation();
     new NequOperation();
     new NotOperation();
     new NegateOperation();
     new OrOperation();
     new TruncOperation();
     new SextendOperation();
     new ZextendOperation();
     new SetOperation();
     new ChoseOperation();
     new ConcatlsOperation();
     new ShiftLeftOperation();
     new ShiftRightOperation();
    new SboxOperation();
   }
   public static State executeFromName( String op_name, State[] operands ) throws Exception {
     return get(op_name).execute(operands);
   }
   public static OpDirections get( String op_name ) {
     return resolver.get( op_name );
   }
   public static void registerOp(String op, OpDirections dir){
     resolver.put(op, dir);
   }
 }
