 package gpqo;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 public class Join extends Gene {
 	public int joinId;
 	public char joinType;
 	
 	public Gene inner; //Left child
 	public Gene outer; //Right child
 	
 	public Relation leftRelation;
 	public Relation rightRelation;
 	
 	//S = sort-merge join, B = block nested loops, H = hash join, I = index nested
 	public static String joinTypes = "SBHI";
 	
 	public static double bufferSize = 10;
 	
 	public int joinAttribute = -1;
 	
 	//Copy constructor
 	public Join(Join join) {
 		this.joinId = join.joinId;
 		this.joinType = join.joinType;
 		
 		this.leftRelation = join.leftRelation;
 		this.rightRelation = join.rightRelation;
 		
 		if(join.inner instanceof Join){
 			this.inner = new Join((Join)join.inner);
 		}
 		else{
 			this.inner = new Relation((Relation)join.inner);
 		}
 		if(join.outer instanceof Join){
 			this.outer = new Join((Join)join.outer);
 		}
 		else{
 			this.outer = new Relation((Relation)join.outer);
 		}
 		
 		this.joinAttribute = join.joinAttribute;
 		
 	}
 	
 	//Constructor for random join
 	public Join(int joinId, Relation leftRelation, Relation rightRelation, Gene inner, Gene outer,
 			int joinAttribute){
 		this.joinId = joinId;
 		
 		Random generator = new Random();
 		int randomIndex = generator.nextInt(joinTypes.length());
 		
 		this.joinType = joinTypes.charAt(randomIndex);
 		
 		this.leftRelation = leftRelation;
 		this.rightRelation = rightRelation;
 		
 		this.inner = inner;
 		this.outer = outer;
 		
 		this.joinAttribute = joinAttribute;
 	}
 
 
 	public ArrayList<JoinGraphNode> getJoinGraph() {
 		ArrayList<JoinGraphNode> graph = new ArrayList<JoinGraphNode>();
 		
 		graph.add(new JoinGraphNode(joinId, leftRelation.relationId, 
 				rightRelation.relationId, leftRelation.size, rightRelation.size,
 				leftRelation.clustIndAttrib, rightRelation.clustIndAttrib, joinAttribute,
 				leftRelation.clustIndCard, rightRelation.clustIndCard));
 		
 		if(inner instanceof Join){
 			graph.addAll(((Join)inner).getJoinGraph());
 		}
 		if(outer instanceof Join){
 			graph.addAll(((Join)outer).getJoinGraph());
 		}
 		
 		return graph;
 	}
 	
 	public double[] cost(){
 		
 		double innerCost = 0, innerSize = 0, outerCost = 0, outerSize = 0;
 		double[] returnCostAndSize = new double[2];
 		char innerType = 'R', outerType = 'R'; // R = relation
 		
 		if (inner instanceof Relation){
 			innerSize = ((Relation)inner).size;
 			innerCost = 0;
 		}
 		else{
 			double[] costAndSize = ((Join)inner).cost();
 			innerType = ((Join)inner).joinType;
 			innerCost = costAndSize[0];
 			innerSize = costAndSize[1];
 		}
 			
 		if (outer instanceof Relation){
 			outerSize = ((Relation)outer).size;
 			outerCost = 0;
 		}
 		else{
 			double[] costAndSize = ((Join)outer).cost();
 			outerType = ((Join)outer).joinType;
 			outerCost = costAndSize[0];
 			outerSize = costAndSize[1];
 		}
 		
 		// Assuming a reduction factor of 10 if there isn't an index on the join attribute
 		// in both cases.
 		double reductionFactor = 10;
 		if (innerType == 'R' && outerType == 'R' && 
 				((Relation)inner).clustIndAttrib == ((Relation)outer).clustIndAttrib &&
 				((Relation)inner).clustIndAttrib == this.joinAttribute)
 			reductionFactor = Math.max(((Relation)inner).clustIndCard, 
 				((Relation)outer).clustIndCard);
 		
 		returnCostAndSize[0] = calcCost(innerSize, outerSize, innerType, outerType) 
 			+ innerCost + outerCost;
 		returnCostAndSize[1] = Math.ceil(innerSize * outerSize / reductionFactor); 
 		
 		return returnCostAndSize;
 	}
 	
 	protected double calcCost(double innerSize, double outerSize, char innerType, char outerType){
 		// Assuming B+ tree clustered index
 		double cost = -1;
 		
 		switch (joinType){
 			case 'S': 
 				cost = calcSortMergeJoin(innerSize, outerSize, innerType, outerType);
 				break;
 			
 			case 'B': 
 				cost = calcBlockNestedJoin(innerSize, outerSize, innerType, outerType);
 				break;
 			
 			case 'H': 
 				cost = calcHashJoin(innerSize, outerSize, innerType, outerType);
 				break;
 			case 'I':
 				cost = calcIndexNestedJoin(innerSize, outerSize, innerType, outerType);
 				break;
 			default: 
 				System.err.println("Invalid join type!"); 
 				break;
 		}
 		
 		return cost;
 	}
 	
 	
 	protected double calcSortMergeJoin(double innerSize, double outerSize, char innerType,
 			char outerType){
 
 		double ratioNtoBInner = Math.ceil(innerSize / bufferSize);
 		double ratioNtoBOuter = Math.ceil(outerSize / bufferSize);
 		double innerCost = 0, outerCost = 0;
 		
 		// if the inner is a relation and has a clustered index or is a sort-merge join => ignore sorting
 		// otherwise, the cost is equal to the cost of writing (need to get all the output) + sorting
 		if ((innerType == 'R' && ((Relation)inner).clustIndAttrib != this.joinAttribute) ||
				innerType == 'B' || innerType == 'H' || innerType == 'I' ||
				(innerType == 'S' && ((Join)inner).joinAttribute != this.joinAttribute)){
			innerCost += 2 * innerSize * (Math.ceil(Math.log(ratioNtoBInner) / Math.log(bufferSize - 1)) + 1);
		
			if(innerType != 'R')
				innerCost += innerSize; //Cost of writing inner
		}
		
 		// if the outer is a relation (has clustered index) or a sort-merge join => ignore sorting
 		// otherwise, the cost is equal to the cost of writing (need to get all the output) + sorting
 		if ((outerType == 'R' && ((Relation)outer).clustIndAttrib != this.joinAttribute) ||
				outerType == 'B' || outerType == 'H' || outerType == 'I' ||
				(outerType == 'S' && ((Join)outer).joinAttribute != this.joinAttribute)){
			outerCost += 2 * outerSize * (Math.ceil(Math.log(ratioNtoBOuter) / Math.log(bufferSize - 1)) + 1);
			
			if(outerType != 'R')
				outerCost += outerSize; //Cost of writing outer
		}
 		
 		// Total cost = (cost of sorting if necessary) + (cost of merging)
 		return (innerCost + outerCost) + (innerSize + outerSize);
 	}
 	
 
 	protected double calcIndexNestedJoin(double innerSize, double outerSize, char innerType,
 			char outerType){
 		
 		// costPerProbing = (cost per probing the index) + (cost of retrieving the page)
 		double costPerProbing = 3 + 1;
 		
 		double cost = -1;
 		boolean INLJoinInner = (innerType == 'R' && ((Relation)inner).clustIndAttrib == this.joinAttribute);
 		boolean INLJoinOuter = (outerType == 'R' && ((Relation)outer).clustIndAttrib == this.joinAttribute);
 		
 		if(INLJoinInner && INLJoinOuter){
 			// Since there is a clustered index on both, so we pick the smaller one 
 			// to be outer.
 			double smallerRel = Math.min(innerSize, outerSize);
 			cost = smallerRel + smallerRel * Relation.defaultNumTuples * costPerProbing;
 			
 		} else if(INLJoinInner && outerType != 'R' ) { // pipelining
 			// For every tuple in outer, probe the index.
 			cost = outerSize * Relation.defaultNumTuples * costPerProbing;
 			
 		} else if(INLJoinInner && outerType == 'R') {
 			cost = outerSize + outerSize * Relation.defaultNumTuples * costPerProbing;
 			
 		} else if(INLJoinOuter && innerType != 'R') { // pipelining
 			// For every tuple in inner, probe the index.
 			cost = innerSize * Relation.defaultNumTuples * costPerProbing;
 			
 		} else if (INLJoinOuter && innerType == 'R') {
 			cost = innerSize + innerSize * Relation.defaultNumTuples * costPerProbing;
 				
 		} else {
 			cost = calcBlockNestedJoin(innerSize, outerSize, innerType, outerType);
 		}
 		
 		return cost;
 	}
 	
 	
 	protected double calcBlockNestedJoin(double innerSize, double outerSize, char innerType,
 			char outerType){
 		
 		double cost = -1;
 		
 		if (innerType == 'R' && outerType == 'R'){
 			double smallerRel = Math.min(innerSize, outerSize);
 			double biggerRel = Math.max(innerSize, outerSize);
 			// Standard formula.
 			cost = smallerRel + biggerRel * Math.ceil(smallerRel / (bufferSize - 2));
 			
 		} else if (innerType == 'R'){ // pipelining
 			// For each block of pages produced by outer, we iterate over inner.
 			cost = innerSize * Math.ceil(outerSize / (bufferSize - 2));
 			
 		} else if (outerType == 'R'){ // pipelining
 			// For each block of pages produced by inner, we iterate over outer.
 			cost = outerSize * Math.ceil(innerSize / (bufferSize - 2));
 			
 		} else {
 			// In this case both inner and outer are Join-s
 			// Need to write one of the join outputs because we will be iterating over it.
 			// We pick the inner.
 			cost = innerSize; // Cost of writing.
 			cost += innerSize * Math.ceil(outerSize / (bufferSize - 2)); // pipelining
 		}
 		
 		return cost;
 	}
 	
 
 	protected double calcHashJoin(double innerSize, double outerSize, char innerType, char outerType){
 		
 		// Can't have pipelining, so we need to write temporary relations
 		double cost = 0;
 		
 		if (innerType != 'R') cost += innerSize; // Cost of writing
 		if (outerType != 'R') cost += outerSize; // Cost of writing
 		
 		// Adding the cost of Hash-Join
 		cost += 3 * (innerSize + outerSize);
 		return cost;
 	}
 
 	public ArrayList<Join> pOrder(){
 		
 		ArrayList<Join> returnList = new ArrayList<Join>();
 		
 		if(!(inner == null) && (inner instanceof Join))
 			returnList.addAll(((Join)inner).pOrder());
 		
 		if(!(outer == null) && (outer instanceof Join))
 			returnList.addAll(((Join)outer).pOrder());
 		
 		returnList.add(this);
 		return returnList;
 	}
 	
 	public Join findJoinWithId(int id) {
 		if(joinId == id){
 			return this;
 		}
 		if(outer instanceof Join){
 			Join outerResult = ((Join)outer).findJoinWithId(id);
 			if(outerResult != null)
 				return outerResult;
 		}
 		if(inner instanceof Join){
 			Join innerResult = ((Join)inner).findJoinWithId(id);
 			if(innerResult != null)
 				return innerResult;
 		}
 		
 		return null;
 	}
 	
 	public void randomNewJoinAlgorithm() {
 		char newAlg;
 		int randomIndex;
 		Random generator = new Random();
 		
 		do{
 			randomIndex = generator.nextInt(joinTypes.length());
 			
 			newAlg = joinTypes.charAt(randomIndex);
 			
 		} while(newAlg == joinType);
 		
 		joinType = newAlg;
 	}
 	
 	public int subtreeSize() {
 		int size = 1;
 		
 		if(inner instanceof Join)
 			size += ((Join)inner).subtreeSize();
 		if(outer instanceof Join)
 			size += ((Join)outer).subtreeSize();
 		
 		return size;
 	}
 	
 	public boolean isJoinLeaf() {
 		return (inner instanceof Relation) && (outer instanceof Relation);
 	}
 	
 	public String toString(){
 		return "J" + joinId;
 	}
 
 
 
 
 	
 }
