 package masg.dd.representations.dag;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import masg.dd.operations.BinaryOperation;
 import masg.dd.operations.UnaryOperation;
 import masg.dd.variables.DDVariable;
 import masg.util.BitMap;
 
 public class ImmutableDDLeaf extends BaseDDNode implements ImmutableDDElement{
 	protected Double value;
 
 	public ImmutableDDLeaf(double value) {
 		this.value = value;
 	}
 	
 	public ImmutableDDLeaf(MutableDDLeaf leaf) {
 		this.value = leaf.getValue();
 	}
 
 	@Override
 	public void getIsMeasure(HashMap<DDVariable, Boolean> map) {
 	}
 
 	@Override
 	public HashMap<DDVariable, Boolean> getIsMeasure() {
 		return new HashMap<DDVariable, Boolean>();
 	}
 
 	@Override
 	public Double getTotalWeight() {
 		return value;
 	}
 
 	@Override
 	public Double getValue(ArrayList<DDVariable> vars, BitMap r) {
 		return value;
 	}
 	
 
 	@Override
 	public ArrayList<Double> getValues(
 			HashMap<DDVariable, HashSet<BitMap>> keyMap) {
 		
 		ArrayList<Double> values = new ArrayList<Double>();
 		values.add(value);
 		return values;
 	}
 	
 	public Double getValue() {
 		return value;
 	}
 
 	@Override
 	public ArrayList<DDVariable> getVariables() {
 		ArrayList<DDVariable> vars = new ArrayList<DDVariable>();
 		return vars;
 	}
 
 	@Override
 	public String toString(String spacer) {
 		return spacer + ":" + value + "\n";
 	}
 
 	@Override
 	public boolean isMeasure() {
 		return true;
 	}
 
 	@Override
 	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix, UnaryOperation oper, MutableDDElement newCollection) {
 		newCollection.setValue(prefixVars, prefix, oper.invoke(value));
 	}
 
 	@Override
 	public void apply(HashMap<DDVariable, HashSet<BitMap>> prevKeys,
 			UnaryOperation oper, MutableDDElement newCollection) {
 		newCollection.setValue(prevKeys, oper.invoke(value));
 	}
 	
 	@Override
 	public void apply(HashMap<DDVariable, HashSet<BitMap>> prevKeys,
 			BinaryOperation oper,
 			ArrayList<ImmutableDDElement> otherCollections,
 			MutableDDElement newCollection) {
 		
 		double val = value;
 		for(ImmutableDDElement otherDD:otherCollections) {
 			for(Double d:otherDD.getValues(prevKeys)) {
 				val = oper.invoke(val, d);
 			}
 		}
 		
 		newCollection.setValue(prevKeys, val);
 	}
 	
 	@Override
 	public void apply(ArrayList<DDVariable> prefixVars, BitMap prefix,
 			BinaryOperation oper,
 			ArrayList<ImmutableDDElement> otherCollections,
 			MutableDDElement newCollection) {
 		
 		double val = value;
 		for(ImmutableDDElement otherDD:otherCollections) {
 			val = oper.invoke(val, otherDD.getValue(prefixVars, prefix));
 		}
 		
 		newCollection.setValue(prefixVars, prefix, val);
 		
 	}
 	
 	@Override
 	public MutableDDElement restrict(Map<DDVariable, Integer> elimVarValues) {
 		return new MutableDDLeaf(value);
 	}
 
 	@Override
 	public void copy(ArrayList<DDVariable> prefixVars, BitMap prefix,
 			BinaryOperation oper, MutableDDElement newCollection) {
 		double val = newCollection.getValue(prefixVars, prefix);
 		newCollection.setValue(prefixVars, prefix, oper.invoke(val, value));
 	}
 
 	@Override
 	public void restrict(ArrayList<DDVariable> prefixVars, BitMap prefix,
 			ArrayList<DDVariable> restrictVars, BitMap restrictKey,
 			MutableDDElement newCollection) {
 		
 		newCollection.setValue(prefixVars, prefix, value);
 	}
 
 	@Override
 	public MutableDDElement eliminateVariables(ArrayList<DDVariable> elimVars,
 			BinaryOperation oper) {
 		return new MutableDDLeaf(value);
 	}
 	
 	@Override
 	public DDVariable getVariable() {
 		return null;
 	}
 
 	@Override
 	public MutableDDElement apply(UnaryOperation oper) {
 		return new MutableDDLeaf(oper.invoke(value));
 	}
 
 	@Override
 	public MutableDDElement apply(BinaryOperation oper,
 			ImmutableDDElement otherColl) {
 		return new MutableDDLeaf(oper.invoke(value,otherColl.getTotalWeight()));
 	}
 
 	@Override
 	public MutableDDElement apply(BinaryOperation oper,
 			ArrayList<ImmutableDDElement> otherColl) {
 		double val = value;
 		for(ImmutableDDElement otherDD:otherColl) {
 			val = oper.invoke(val, otherDD.getTotalWeight());
 		}
 		
 		return new MutableDDLeaf(val);
 	}
 
 	public boolean equals(Object o) {
 		if(o==this)
 			return true;
 		
 		if(o instanceof ImmutableDDLeaf) {
 			ImmutableDDLeaf leaf = (ImmutableDDLeaf) o;
			return Math.abs(leaf.getValue().doubleValue()-getValue().doubleValue())<0.00001f;
 		}
 		else {
 			return o.equals(this);
 		}
 	}
 
 	@Override
 	public MutableDDElement primeVariables() {
 		return new MutableDDLeaf(value);
 	}
 
 	@Override
 	public void primeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,
 			MutableDDElement newColl) {
 		ArrayList<DDVariable> primedPrefixVars = new ArrayList<DDVariable>();
 		for(DDVariable v:prefixVars) {
 			primedPrefixVars.add(v.getPrimed());
 		}
 		newColl.setValue(primedPrefixVars, prefix, value);
 		
 	}
 	
 	@Override
 	public MutableDDElement unprimeVariables() {
 		return new MutableDDLeaf(value);
 	}
 
 	@Override
 	public void unprimeVariables(ArrayList<DDVariable> prefixVars, BitMap prefix,
 			MutableDDElement newColl) {
 		ArrayList<DDVariable> unprimedPrefixVars = new ArrayList<DDVariable>();
 		for(DDVariable v:prefixVars) {
 			unprimedPrefixVars.add(v.getUnprimed());
 		}
 		//System.out.println(unprimedPrefixVars + "=" + value);
 		newColl.setValue(unprimedPrefixVars, prefix, value);
 		
 	}
 
 	
 }
