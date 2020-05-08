 package GCParser;
 import YaoGC.*;
 import GCParser.Operation.*;
 import java.util.*;
 import java.math.BigInteger;
 public class Computed_Variable extends Variable {
   private Variable[] children;
   private OpDirections op;
   private int numBits = -1;
   private State value;
   public Computed_Variable( String id, int lineNumArg, Variable[] childarg, OpDirections OP ){
     this( id, Input_Variable.ALL, lineNumArg, childarg, OP );
   }
   public Computed_Variable( String id, int parg ,int lineNumArg, Variable[] childarg, OpDirections OP ){
     super(id,parg,lineNumArg);
     op = OP;
     children = childarg;
     value = null;
     for( int i = 0; i < children.length; i++ ){
       if( children[i] != null )
 	children[i].addParent( this, i );
     }
   }
   public Variable[] getChildren(){
     return children;
   }
   public void reset(){
     if( value != null ) {
       for( int i = 0; i < children.length; i++ ){
 	children[i].reset();
       }
     }
     value = null;
   }
   public State execute() throws Exception {
     if( value == null ){
       State[] inputs = new State[children.length];
       for( int i = 0; i < children.length; i++ ){
 	inputs[i] = children[i].executeDebug();
       }
       value = op.execute( inputs );
     }
     return value;
   }
   public int validate() throws CircuitDescriptionException {
     if( numBits == -1 ){
       if( getParty() != Input_Variable.ALL )
 	for( Variable v : children ){
 	  if( v == null ){
 	    throw createException("Variable \""+getId()+"\"'s operand not initialized");
 	  }
 	  if( v.getParty() != getParty() && v.getParty() != Input_Variable.NEUTRAL ) {
 	    throw createException("Variable \""+getId()+"\" cannot be evaluated local to party "+getParty()+" when it depends on variable \""+v.getId()+"\" of party "+v.getParty());
 	  }
 	}
      numBits = op.validate(children,this);
    } 
    return numBits;
   }
   public void localEval( int party, Variable_Context con ) throws Exception {
     super.localEval( party, con );
     if( !feedsLocally( con ) && ( getParty() == Input_Variable.CLIENT || getParty() == Input_Variable.SERVER ) ){
       // replace with input variable
       Input_Variable newV = createFrom( con );
       newV.localEval(party,con);
       replaceWith( newV );
       con.remove(this);
       if( con.getOutputs().contains( getId() ) ){
 	con.putVar( Dummy_Variable.newInstance( getId(), getLineNum(), newV ) );
       }
     }
     for( Variable v : children ){
       v.localEval( party, con );
     }
   }
   public void debugPrint(int tabs){
     super.debugPrint(tabs);
     int newtabs = tabs+1;
     for( Variable v : children ){
       if( v != null )
 	v.debugPrint( newtabs );
     }
   }
   public String toString(){
     if( op == null ){
       return super.toString();
     }
     return super.toString()+"(operation:"+op.getOp_name()+")";
   }
 }
