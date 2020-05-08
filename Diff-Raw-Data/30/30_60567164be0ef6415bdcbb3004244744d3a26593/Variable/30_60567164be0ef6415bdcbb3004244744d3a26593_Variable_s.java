 package GCParser;
 
 import YaoGC.*;
 import java.util.*;
 import java.math.BigInteger;
 import GCParser.Operation.CircuitDescriptionException;
 
 public abstract class Variable implements Comparable<Variable> {
   private String id;
   private int debugLineNum;
   private int party;
   protected Map<Computed_Variable, Integer> parents;
   private boolean feedsLocally = true;
 
   public Variable(String idarg, int partyarg ,int lineNumArg){
     party = partyarg;
     id = idarg;
     debugLineNum = lineNumArg;
    parents = new TreeMap<Computed_Variable, Integer>();
   }
   public Variable( String idarg, int partyarg ){
     this( idarg, partyarg, -1 );
   }
   public int getParty(){
     return party;
   }
   public void setParty( int p ){
     party = p;
   }
   public String getId(){
     return id;
   }
   public int getLineNum(){
     return debugLineNum;
   }
   public int compareTo( Variable v ){
     return id.compareTo(v.id);
   }
 
   public abstract State execute() throws Exception; // the outputs of this variable
   // returns the number of bits expected to output, or throws an exception if it cannot
   public abstract int validate() throws CircuitDescriptionException;
 
   public void addParent( Computed_Variable v, int childNum ){
     if( v.getParty() != Input_Variable.CLIENT && v.getParty() != Input_Variable.SERVER )
       feedsLocally = false;
     parents.put(v, childNum);
   }
   public Variable replaceWith( Variable v ){
     for( Iterator<Computed_Variable> it = parents.keySet().iterator(); it.hasNext(); ){
       Computed_Variable parent = it.next();
       int i = parents.get(parent);
       Variable[] siblings = parent.getChildren();
       siblings[ i ] = v;
       v.addParent( parent, i );
     }
     return this;
   }
 
   public Map<Computed_Variable,Integer> getParents(){
     return parents;
   }
   public boolean feedsLocally( Variable_Context con ){
     if( ! con.getOutputs().contains(getId()) && ( getParty() == Input_Variable.SERVER || getParty() == Input_Variable.CLIENT ) )
       return feedsLocally;
     else 
       return false;
   }
   public State executeDebug(){
     State ans;
     try {
       ans = execute();
     } catch ( Exception e ) {
       System.out.print("Error evaluating variable \""+id+"\" ");
       if( debugLineNum != -1 )
 	System.out.println(" on line "+debugLineNum);
       e.printStackTrace();
       System.exit(1);
       ans = null;
     }
     return ans;
   }
   public CircuitDescriptionException createException( String mes ){
     return new CircuitDescriptionException( mes, getLineNum() );
   }
   public void reset(){}
   public void localEval( int party, Variable_Context con ) throws Exception {
     if( feedsLocally(con) )
       con.remove(this);
   }
   public int bitCount() {
     try{
       return validate();
     } catch(CircuitDescriptionException e){
       return -1;
     }
   }
   public void debugPrint(){
     debugPrint(0);
   }
   public void debugPrint(int tabs){
     for( int i = 0; i < tabs; i++ ){
       System.out.print("\t");
     }
     System.out.println(this);
   }
   public String toString(){
     return getId()+"(party:"+getParty()+")(bits:"+bitCount()+")";
   }
 
   public Input_Variable createFrom(Variable_Context con) throws Exception {
     Collapsed_In_Var newV;
     Variable v = con.get( "_"+getId() );
     if( v != null && v instanceof Collapsed_In_Var )
       newV = (Collapsed_In_Var) v;
     else {
       newV = new Collapsed_In_Var( this );
       con.putVar( newV );
     }
     return newV;
   }
 }
