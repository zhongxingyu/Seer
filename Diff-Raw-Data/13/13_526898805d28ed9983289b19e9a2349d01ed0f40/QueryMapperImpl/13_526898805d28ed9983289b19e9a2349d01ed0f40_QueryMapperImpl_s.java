 package org.vamdc.tapservice.querymapper;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.apache.cayenne.exp.Expression;
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.tapservice.vss2.LogicNode;
 import org.vamdc.tapservice.vss2.LogicNode.Operator;
 import org.vamdc.tapservice.vss2.RestrictExpression;
 
 public class QueryMapperImpl implements QueryMapper{
 
 	private Map<Restrictable, KeywordMapper> mappers = new TreeMap<Restrictable,KeywordMapper>();
 	
 	@Override
 	public void addMapper(KeywordMapper mapper) {
 		mappers.put(mapper.getKeyword(), mapper);
 	}
 
 	@Override
 	public Collection<Restrictable> getRestrictables() {
 		return mappers.keySet();
 	}
 
 	@Override
 	public Expression mapTree(LogicNode root, int queryIndex) {
		return mapSubTree(root,queryIndex,null,null);
 	}
 	
 	@Override
 	public Expression mapAliasedTree(LogicNode root, int queryIndex,String alias, String replacement) {
		return mapSubTree(root,queryIndex,alias,replacement);
 	}
 
 	@Override
 	public boolean isReady() {
 		int length=0;
 		for (KeywordMapper mapper:mappers.values()){
 			if (length>0 && length!=mapper.getMaxQueryIndex())
 				return false;
 			else if (length==0)
 				length = mapper.getMaxQueryIndex();
 		}
 		return true;
 	}
 
 	
 	private Expression mapSubTree(LogicNode root, int queryIndex, String alias, String replacement){
 		if (root==null)
 			throw new IllegalArgumentException("Incoming tree should not be null");
 		
 		if (root instanceof RestrictExpression){
 			return mapExpression((RestrictExpression) root, queryIndex,alias,replacement);
 		}else{
 			Expression result=null,newExpr=null;
 			switch (root.getOperator()){
 			case NOT:
 				return mapSubTree((LogicNode) root.getValue(),queryIndex,alias,replacement).notExp();
 			default:
 				for (Object branch:root.getValues()){
 					newExpr = mapSubTree((LogicNode)branch,queryIndex,alias,replacement);
 					if (newExpr==null)
 						continue;
 					if (result==null)
 						result = newExpr;
 					else if (root.getOperator()==Operator.OR)
 						result = result.orExp(newExpr);
 					else if (root.getOperator()==Operator.AND)
 						result = result.andExp(newExpr);
 					else
 						throw new IllegalArgumentException("Unknown expression operator "+root.getOperator());
 				}
 			};
 			return result;
 		}
 	}
 
 	public Expression mapExpression(RestrictExpression node, int queryIndex, String alias, String replacement) {
 		KeywordMapper mapper = mappers.get(node.getColumn());
 		if (mapper!=null)
 			return mapper.translateAlias(node, queryIndex,alias,replacement);
 		else 
 			throw new IllegalArgumentException("Query contains a keyword "+node.getColumn()+" that is somehow not supported");
 			//TODO: add configuration option to return true here
 	}
 	
 }
