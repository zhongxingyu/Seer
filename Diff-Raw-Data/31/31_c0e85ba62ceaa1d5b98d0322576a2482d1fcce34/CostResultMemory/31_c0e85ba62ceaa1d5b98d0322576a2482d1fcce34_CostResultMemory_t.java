 package sw10.spideybc.analysis;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.ibm.wala.ipa.callgraph.CGNode;
 import com.ibm.wala.types.TypeName;
 import com.ibm.wala.util.collections.Pair;
 
 public class CostResultMemory implements ICostResult {
 	
 	/* Dynamic allocation cost */
 	public long allocationCost;
 	
	/* Stack information (in units) */
	private int stackCost;
	private long accumStackCost;
	private int maxStackHeight;
	private int maxLocals;
 	
 	/* Unit size for locals and operand stack
 	 * The stackCost must be multiplied by this constant
 	 * (On JOP 1 unit = 4 bytes)
 	 */
 	private int stackUnitSize;
 	
 	public Map<TypeName, Integer> aggregatedArraySizeByTypeName;
 	public Map<TypeName, Integer> arraySizeByTypeName;
 	public Map<TypeName, Integer> countByTypename;
 	public Map<TypeName, Integer> aggregatedCountByTypename;
 	public Map<Integer, TypeName> typeNameByNodeId;
 	public Map<Integer, Pair<TypeName, Integer>> arraySizeByNodeId;
 	
 	public ArrayList<CGNode> worstcaseReferencesMethods;
 	public ICostResult.ResultType resultType;
 	public CGNode nodeForResult;
 
 	public CostResultMemory() {
 		this.allocationCost = 0;
 		this.stackCost = 0;
 		this.accumStackCost = 0;
 		
 		this.maxStackHeight = 0;
 		this.maxLocals = 0;
 		
 		aggregatedArraySizeByTypeName = new HashMap<TypeName, Integer>();
 		countByTypename = new HashMap<TypeName, Integer>();
 		aggregatedCountByTypename = new HashMap<TypeName, Integer>();
 		arraySizeByTypeName = new HashMap<TypeName, Integer>();
 		typeNameByNodeId = new HashMap<Integer, TypeName>();
 		arraySizeByNodeId = new HashMap<Integer, Pair<TypeName, Integer>>();
 		worstcaseReferencesMethods = new ArrayList<CGNode>();
 		resultType = ResultType.TEMPORARY_BLOCK_RESULT;		
 	}
 	
 	/*
 	 * Stack unit size (in bytes)
 	 */
 	public void setStackUnitSize(int stackUnitSize) {
 		this.stackUnitSize = stackUnitSize;
 	}
 	
 	public List<CGNode> getWorstCaseReferencedMethods() {
 		return worstcaseReferencesMethods;
 	}
 	
 	public long getAccumStackCost() {
 		return accumStackCost;
 	}
 	
	public long getAccumStackCostInBytes() {
		return accumStackCost * stackUnitSize;
	}
	
 	public void setAccumStackCost(long accumStackCost) {
 		this.accumStackCost = accumStackCost;
 	}
 	
 	public int getStackCost() {
 		return stackCost;
 	}
 	
 	public void setMaxStackHeight(int height) {
 		this.maxStackHeight = height;
		this.stackCost = maxStackHeight + maxLocals;
 	}
 	
 	public int getMaxStackHeight() {
 		return maxStackHeight;
 	}
 	
 	public void setMaxLocals(int number) {
 		this.maxLocals = number;
		this.stackCost = maxLocals + maxStackHeight;
 	}
 	
 	public int getMaxLocals() {
 		return maxLocals;
 	}
 	
 	@Override
 	public long getCostScalar() {
 		return allocationCost;
 	}
 	
 	@Override
 	public void resetCostScalar() {
 		this.allocationCost = 0;
 	}
 	
 	@Override
 	public CostResultMemory clone() {
 		CostResultMemory clone = new CostResultMemory();
 		clone.allocationCost = allocationCost;
 		
 		return clone;
 	}
 	
 	@Override
 	public CostResultMemory cloneTemporaryResult() {
 		CostResultMemory clone = new CostResultMemory();
 		clone.allocationCost = allocationCost;
 		clone.typeNameByNodeId.putAll(typeNameByNodeId);
 		clone.arraySizeByNodeId.putAll(arraySizeByNodeId);
 		
 		return clone;
 	}
 
 	@Override
 	public ResultType getResultType() {
 		return this.resultType;
 	}
 	
 	@Override
 	public boolean isFinalNodeResult() {
 		return this.resultType.equals(ResultType.COMPLETE_NODE_RESULT);
 	}
 }
