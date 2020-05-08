 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 public class ProblemDescription
 {
     public ProblemDescription() {}
 
     public void pushVariable(String name)
     {
         pushVariable(name, false);
     }
 
     public void pushVariable(String name, boolean checkRedefinition)
     {
         if ( checkRedefinition )
         {
             if (get(name) == null)
             {
                 newvars.addFirst(name);
             }
             else
             {
                 log("Warning: ignoring redefinition of variable " + name + ".");
             }
         }
         else
         {
             newvars.addFirst(name);
         }
     }
 
     public void commitVariableListToObservables()
     {
         for ( String name : newvars )
         {
             ObservableVariable ov = new ObservableVariable(name);
             observables.addLast(ov);
             variables.put(name, ov);
         }
         newvars.clear();
     }
 
     public void commitVariableListToNonObservables()
     {
         for ( String name : newvars )
         {
             NonObservableVariable nov = new NonObservableVariable(name);
             nonobservables.addLast(nov);
             variables.put(name, nov);
         }
         newvars.clear();
     }
 
     public Variable get(String name)
     {
         return variables.get(name);
     }
 
     public void setNonObservable(String name, String value)
     throws NumberFormatException
     {
         Variable var = get(name);
         if ( var == null )
         {
             log("ERROR: variable " + name + " does not exist!");
             foundError = true;
             return;
         }
         try
         {
             NonObservableVariable nov = (NonObservableVariable)var;
             nov.setProbability( new Probability(value) );
         }
         catch (ClassCastException e)
         {
             log("ERROR: trying to set probability for observable variable " + var + "!");
             foundError = true;
             return;
         }
     }
 
     public void setObservable(String name)
     {
         Variable var = get(name);
         if ( var == null )
         {
             log("ERROR: variable " + name + " does not exist!");
             foundError = true;
             return;
         }
         try
         {
             ObservableVariable ov = (ObservableVariable)var;
             ov.setExpression( newexpression );
             equations.addLast( ov );
         }
         catch (ClassCastException e)
         {
             log("ERROR: trying to set expression for non-observable variable " + var + "!");
             foundError = true;
         }
         catch (VariableUndefinedException e)
         {
             log("ERROR: " + e.toString());
             foundError = true;
         }
         newexpression = new LinkedList<ExpressionNode>();
     }
 
     public void pushNotToExpression()
     {
         newexpression.addLast( new OperationNot() );
     }
 
     public void pushAndToExpression()
     {
         newexpression.addLast( new OperationAnd() );
     }
 
     public void pushXorToExpression()
     {
         newexpression.addLast( new OperationXor() );
     }
 
     public void pushOrToExpression()
     {
         newexpression.addLast( new OperationOr() );
     }
 
     public void pushConstantToExpression(String value)
     {
         newexpression.addLast( new Constant(value) );
     }
 
     public void pushVariableToExpression(String name)
     {
         Variable var = get(name);
         if ( var == null )
         {
             foundError = true;
             log("ERROR in expression! Variable " + name + " does not exist.");
             return;
         }
         if ( !var.isDefined() )
         {
             foundError = true;
             log("ERROR in expression! Variable " + name + " is not defined.");
             return;
         }
         newexpression.addLast(var);
     }
 
     public String toString()
     {
         if ( isOK() )
         {
             StringBuffer out = new StringBuffer("");
             out.append("OBS: ");
             for ( Variable s : observables )
                 out.append(s.getName() + ",");
             out.append("\n");
             out.append("EXO: ");
             for ( Variable s : nonobservables )
                 out.append(s.getName() + ",");
             out.append("\n");
             out.append("\n");
             for ( ObservableVariable ov : equations )
             {
                 out.append( ov.toString() );
                 out.append("\n");
             }
             return out.toString();
         }
         else
         {
             return "(ERROR)";
         }
     }
 
     public boolean isOK()
     {
         return !foundError;
     }
 
     public void log(String s)
     {
         System.err.println(s);
     }
 
     public void pushIntervention(String variableName, String value)
     {
         int val = Integer.parseInt( value );
         if ( val == 0 )
             newInterventions.put( variableName, Boolean.FALSE );
         else
             newInterventions.put( variableName, Boolean.TRUE );
     }
 
     public void pushCondition(String variableName, String value)
     {
         int val = Integer.parseInt( value );
         if ( val == 0 )
             newConditions.put( variableName, Boolean.FALSE );
         else
             newConditions.put( variableName, Boolean.TRUE );
     }
 
     public void commitConditionListToOutput( String outputNumber )
     throws VariableUndefinedException, ClassCastException
     {
         Integer num = new Integer( outputNumber );
         Map<ObservableVariable,Boolean> conds =
                                 new TreeMap<ObservableVariable,Boolean>();
         for ( Map.Entry<String, Boolean> entry : newConditions.entrySet() )
         {
             String name = entry.getKey(); 
             Boolean value = entry.getValue(); 
             ObservableVariable ov = (ObservableVariable)get(name);
             if ( ov == null || !marginSet.get(num).contains(ov) )
             {
                 String err = "undefined variable " + name +
                           " in COND line in output " + outputNumber;
                 throw new VariableUndefinedException(err);
             }
             conds.put(ov, value);
         }
         conditions.put( num, conds );
         newConditions.clear();
     }
 
     public void commitInterventionListToOutput( String outputNumber,
                                                 String comment )
     throws RedefinitionException
     {
         if ( comment == null )
         {
             comment = "";
         }
         Integer num = new Integer( outputNumber );
         if ( outputComments.get( num ) != null )
         {
             throw new RedefinitionException("output " + outputNumber +
                                             " redefined");
         }
         outputComments.put( num, comment );
         interventions.put( num, newInterventions );
        newInterventions = new HashMap<String,Boolean>();
     }
 
     public void commitVariableListToOutput(String outputNumber)
     throws VariableUndefinedException, ClassCastException
     {
         Integer num = new Integer( outputNumber );
 
         ArrayList<ObservableVariable> newMargins =
                     new ArrayList<ObservableVariable>();
         TreeSet<ObservableVariable> newMarginSet =
                     new TreeSet<ObservableVariable>();
 
         for ( String name : newvars )
         {
             ObservableVariable ov = (ObservableVariable)get(name);
             if ( ov == null )
             {
                 throw new VariableUndefinedException(
                           "undefined variable " + name +
                           " in output " + outputNumber);
             }
             newMargins.add(ov);
             newMarginSet.add(ov);
         }
         margins.put( num, newMargins );
         marginSet.put( num, newMarginSet );
         newvars.clear();
     }
 
     public HashMap<String,Variable> variables =
                     new HashMap<String,Variable>();
     public LinkedList<ObservableVariable> observables =
                     new LinkedList<ObservableVariable>();
     public LinkedList<NonObservableVariable> nonobservables =
                     new LinkedList<NonObservableVariable>();
     public LinkedList<ObservableVariable> equations =
                     new LinkedList<ObservableVariable>();
     public TreeMap<Integer, List<ObservableVariable> > margins =
                     new TreeMap<Integer, List<ObservableVariable> >();
     public TreeMap<Integer, TreeSet<ObservableVariable> > marginSet =
                     new TreeMap<Integer, TreeSet<ObservableVariable> >();
     public TreeMap<Integer, Map<String,Boolean> > interventions =
                     new TreeMap<Integer, Map<String,Boolean> >();
     public TreeMap<Integer, Map<ObservableVariable,Boolean> > conditions =
                     new TreeMap<Integer, Map<ObservableVariable,Boolean> >();
     public TreeMap<Integer, String> outputComments =
                     new TreeMap<Integer, String>();
 
     protected boolean foundError = false;
 
     protected Map<String,Boolean> newConditions =
                       new HashMap<String,Boolean>();
     protected Map<String,Boolean> newInterventions =
                       new HashMap<String,Boolean>();
     protected LinkedList<String> newvars = new LinkedList<String>();
     protected LinkedList<ExpressionNode> newexpression =
                     new LinkedList<ExpressionNode>();
 
     public class RedefinitionException extends Exception
     {
         RedefinitionException(String str)
         {
             super(str);
         }
     }
 }
