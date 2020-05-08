 // Copyright (C) Billy Melicher 2012 wrm2ja@virginia.edu
 package GCParser;
 import java.util.*;
 import YaoGC.State;
 import YaoGC.Circuit;
 import GCParser.Operation.CircuitDescriptionException;
 import java.math.BigInteger;
 public class Variable_Context {
 
   private Map<String, Variable> variables;
   private Map<Integer, Set<Input_Variable> > partyMap;
   private Set<String> inputs;
   private Map<String, Boolean> outVar;
   private Map<String, Input_Variable> collapsedVars;
   private Set<Variable> computedParty;
   private boolean isReset;
   private boolean local;
   public Variable_Context(){
     isReset = true;
     local = false;
     inputs = new HashSet<String>();
     variables = new HashMap<String, Variable>();
     partyMap = new HashMap<Integer, Set<Input_Variable> >();
    partyMap.put( Input_Variable.SERVER, new HashSet<Input_Variable>() );
    partyMap.put( Input_Variable.CLIENT, new HashSet<Input_Variable>() );
     outVar = new HashMap<String,Boolean>();
     collapsedVars = new HashMap<String, Input_Variable>();
     computedParty = new HashSet<Variable>();
   }
   public void putVar( Variable v ) throws CircuitDescriptionException {
     v.validate();
     Variable old = variables.put( v.getId(), v );
     if( old != null )
       throw v.createException(
 	("Variable \""+v.getId()+"\" previously defined (on line "+old.getLineNum()+")") );
     if( v instanceof Input_Variable ){
       Input_Variable inv = (Input_Variable) v;
       inputs.add( inv.getId() );
       Set<Input_Variable> inlist = partyMap.get( inv.getParty() );
       if( inlist == null ){
 	inlist = new HashSet<Input_Variable>();
 	partyMap.put(inv.getParty(),inlist);
       }
       inlist.add( inv );
     } else if( ( v.getParty() == Input_Variable.SERVER || v.getParty() == Input_Variable.CLIENT ) && !local) {
       computedParty.add( v );
     }
   }
   public Set<String> getInputs() {
     return inputs;
   }
   public Set<String> getOutputs() {
     return outVar.keySet();
   }
   public boolean isSigned( String name ){
     return outVar.get(name);
   }
   public void addOutput( String name, boolean signed ){
     outVar.put( name, signed );
   }
   public Variable get( String name ){
     return variables.get(name);
   }
   public Input_Variable getInVar(String name ){
     Variable v = get(name);
     if( v instanceof Input_Variable ){
       return (Input_Variable)v;
     } else {
       return null;
     }
   }
   public void collapseLocalVars( Map<String,BigInteger> in, int party ) throws Exception {
     for( String s : in.keySet() ){
       Input_Variable v = getInVar(s);
       if( v == null ){
 	v = collapsedVars.get(s);
       }
       v.setState(new State( in.get(s), v.bitcount ));
     }
     local = true;
     for( Variable com : computedParty ){
       com.localEval( party, this );
     }
     local = false;
   }
   public void remove( Variable v ){
     variables.remove(v.getId());
     inputs.remove(v.getId());
     Set<Input_Variable> list = partyMap.get( v.getParty() );
     if( list != null )
       list.remove(v);
     if( v instanceof Input_Variable )
       collapsedVars.put( v.getId(),(Input_Variable) v );
   }
   public void validate() throws CircuitDescriptionException {
     if( ! variables.keySet().containsAll( getOutputs() ) ){
       getOutputs().removeAll( variables.keySet() );
       String error = "Output variable(s) not defined: ";
       Iterator<String> it = getOutputs().iterator();
       while( it.hasNext() ){
 	error+= it.next();
 	if( it.hasNext() ){
 	  error+=", ";
 	}
       }
       throw new CircuitDescriptionException(error);
     }
   }
   public void resetCircuit(){
     Iterator<String> outit = getOutputs().iterator();
     while( outit.hasNext() ){
       String id = outit.next();
       Variable out = get(id);
       if( out == null ){
 	System.out.println("Output variable \""+id+"\" not defined...Exiting");
 	System.exit(1);
       }
       out.reset();
     }
     isReset = true;
   }
   public Map<String,State> execCircuit(){
     OperationNameResolver.initOperations();
     Iterator<String> outit = getOutputs().iterator();
     Map<String, State> ans = new HashMap<String, State>();
     while( outit.hasNext() ){
       String id = outit.next();
       Variable out = get(id);
       if( out == null ){
 	System.out.println("Output variable \""+id+"\" not defined...Exiting");
 	System.exit(1);
       }
       ans.put( id, out.executeDebug() );
     }
     return ans;
   }
   private void setInVals( Map<String, State> in ){
     Iterator<String> it = in.keySet().iterator();
     while( it.hasNext() ){
       String id = it.next();
       Input_Variable var = (Input_Variable) get( id );
       var.setState( in.get( id ) );
     }
   }
   public Map<String,State> execCircuit( Map<String, State> inputVals ){
     if( !isReset )
       resetCircuit();
     setInVals( inputVals );
     return execCircuit();
   }
   public Collection<Input_Variable> getInVarsOfParty( int party ){
     return partyMap.get( party );
   }
   public Collection<Input_Variable> getPrivInOfParty( int party ){
     // get private inputs to supply
     Set<Input_Variable> ans = new HashSet<Input_Variable>();
     for( Input_Variable i : getInVarsOfParty(party) ){
       if( !(i instanceof Collapsed_In_Var ) ){
 	ans.add(i);
       }
     }
     for( String v : collapsedVars.keySet() ){
       ans.add( collapsedVars.get(v) );
     }
     return ans;
   }
   public int getBitsOfParty( int party ){
     Collection<Input_Variable> list = getInVarsOfParty( party );
     int accum = 0;
     for( Input_Variable v : list ){
       accum += v.bitcount;
     }
     return accum;
   }
   public int getNumVarsOfParty( int party ){
     return getInVarsOfParty( party ).size();
   }
   public void addContextWithMapping( Variable_Context other, Map<String,Variable> inMap, Map<String,String> outMap , int lineNum) throws CircuitDescriptionException {
     for( Iterator<String> it = other.getInputs().iterator(); it.hasNext(); ){
       String otherInId = it.next();
       Variable arg = inMap.get(otherInId);
       Input_Variable join = (Input_Variable)(other.get(otherInId));
       join.replaceWith(arg);
     }
     for( Iterator<String> it = other.getOutputs().iterator(); it.hasNext(); ){
       String otherOutId = it.next();
       String newVarName = outMap.get( otherOutId );
       Variable newVar = other.get( otherOutId );
       Variable dummyVar = Dummy_Variable.newInstance( newVarName, lineNum, newVar );
       putVar( dummyVar );
     }
   }
   public void debugPrint(){
     for( String s: getOutputs() ){
       get(s).debugPrint();
     }
   }
 }
