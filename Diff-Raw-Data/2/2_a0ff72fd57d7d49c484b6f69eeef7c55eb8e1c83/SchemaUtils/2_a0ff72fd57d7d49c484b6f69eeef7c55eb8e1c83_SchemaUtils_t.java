 package com.dustyneuron.bitprivacy.schemas;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.codec.binary.Hex;
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ConstraintFormula;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ConstraintFormula.ForEach;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItemSpecifier;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element.ElementType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Expression;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.PartyType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Variable;
 import com.google.bitcoin.core.Sha256Hash;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.GeneratedMessage;
 
 public class SchemaUtils {
 	
 	static public Sha256Hash getSchemaKey(TransactionSchema schema) {
 		return Sha256Hash.create(schema.toByteArray());
 	}
 	
 	public static class VariableValues {
 		Map<Variable, BigInteger> values = new HashMap<Variable, BigInteger>();
 		
 		public void put(Variable getV, BigInteger b) {
 			byte[] getVBytes = getV.toByteArray();
 			for (Variable v : values.keySet()) {
 				byte[] vBytes = v.toByteArray();
 				if (Arrays.equals(vBytes, getVBytes)) {
 					values.put(v, b);
 					return;
 				}
 			}
 			
 			values.put(getV, b);
 		}
 		public BigInteger get(Variable getV) {
 			byte[] getVBytes = getV.toByteArray();
 			for (Variable v : values.keySet()) {
 				byte[] vBytes = v.toByteArray();
 				if (Arrays.equals(vBytes, getVBytes)) {
 					return values.get(v);
 				}
 			}
 			return null;
 		}
 	}
 	
 	static BigInteger evaluateVariable(List<DataItem> allData, Variable v) throws Exception {
 				
 		BigInteger result = new BigInteger("0");
 		
 		switch (v.getVariableType()) {
 			case SUM_BTCVALUES_TYPE:
 				for (DataItem d : allData) {
 					if ((d.getReference().getRefType() == v.getRefType())
 							&& (d.hasValue())) {
 						result = result.add(SchemaUtils.readBigInteger(d.getValue()));
 					}
 				}
 				return result;
 				
 			case SUM_BTCVALUES_TYPEINDEX:
 				for (DataItem d : allData) {
 					if ((d.getReference().getRefType() == v.getRefType())
 							&& (d.getReference().getRefIdx() == v.getRefTypeIndex())
 							&& (d.hasValue())) {
 						result = result.add(SchemaUtils.readBigInteger(d.getValue()));
 					}
 				}
 				return result;
 
 			case SUM_TYPE:
 				for (DataItem d : allData) {
 					if (d.getReference().getRefType() == v.getRefType()) {
 						result = result.add(new BigInteger("1"));
 					}
 				}
 				return result;
 
 			case SUM_TYPEINDEX: 
 				for (DataItem d : allData) {
 					if ((d.getReference().getRefType() == v.getRefType())
 							&& (d.getReference().getRefIdx() == v.getRefTypeIndex())) {
 						result = result.add(new BigInteger("1"));
 					}
 				}
 				return result;
 
 			case FOREACH_BTCVALUE:
 				throw new Exception("unhandled variable type " + v.getVariableType());
 				
 			default:
 				throw new Exception("unhandled variable type " + v.getVariableType());
 		}
 	}
 
 	// This ignores foreach vars
 	static VariableValues evaluateVariables(Trade trade) throws Exception {
 		VariableValues variableValues = new VariableValues();
 		
 		List<Expression> expressions = new ArrayList<Expression>();
 		for (ConstraintFormula constraint : trade.getSchema().getConstraintsList()) {
 			if (constraint.getForEach() == ForEach.NONE) {
 				expressions.addAll(constraint.getLhsList());
 				expressions.addAll(constraint.getRhsList());
 			}
 		}
 		
 		List<DataItem> allData = new ArrayList<DataItem>();
 		for (SinglePartyData s : trade.getAllPartiesDataList()) {
 			allData.addAll(s.getDataList());
 		}
 
 		
 		for (Expression e : expressions) {
 			if (e.getElement().getElementType() == ElementType.VARIABLE) {
 				Variable v = e.getElement().getVariable();
 				variableValues.put(v, evaluateVariable(allData, v));
 			}
 		}
 		
 		return variableValues;
 	}
 	
 	public enum TradeConstraintsStatus {CONSTRAINTS_OK, CONSTRAINTS_BROKEN};
 	
 	static BigInteger evaluateConstraintElement(VariableValues variableValues, Element element) throws Exception {
 		
 		if (element.getElementType() == ElementType.VALUE) {
 			return SchemaUtils.readBigInteger(element.getValue());
 		}
 		
 		return variableValues.get(element.getVariable());
 	}
 	
 	static BigInteger evaluateConstraintExpression(VariableValues variableValues, Expression expression) throws Exception {
 
 		BigInteger value = evaluateConstraintElement(variableValues, expression.getElement());
 		
 		if (expression.getMinus()) {
 			value = new BigInteger("0").subtract(value);
 		}
 		if (expression.getMultiplier() != null) {
 			value = value.multiply(SchemaUtils.readBigInteger(expression.getMultiplier()));
 		}
 		if (expression.getDivisor() != null) {
 			value = value.divide(SchemaUtils.readBigInteger(expression.getDivisor()));
 		}
 		
 		return value;
 	}
 	
 	static TradeConstraintsStatus evaluateConstraints(VariableValues variableValues, List<ConstraintFormula> constraints) throws Exception {
 
 		for (ConstraintFormula constraint : constraints) {
 			
 			// TODO: implement foreach
 			if (constraint.getForEach() == ForEach.TYPEINDEX) {
 				throw new Exception("foreach is unimplemented");
 			}
 			
 			TradeConstraintsStatus status;
 			
 			BigInteger lhs = new BigInteger("0");
 			for (Expression e : constraint.getLhsList()) {
 				lhs = lhs.add(evaluateConstraintExpression(variableValues, e));
 			}
 			BigInteger rhs = new BigInteger("0");
 			for (Expression e : constraint.getRhsList()) {
 				rhs = rhs.add(evaluateConstraintExpression(variableValues, e));
 			}
 			
 			int compareTo = lhs.compareTo(rhs);
 			
 			switch (constraint.getComparator()) {
 				case EQ:
 					if (compareTo == 0) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				case NEQ:
 					if (compareTo != 0) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				case GT:
 					if (compareTo == 1) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				case LT:
 					if (compareTo == -1) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				case GTE:
 					if ((compareTo == 1) || (compareTo == 0)) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				case LTE:
 					if ((compareTo == -1) || (compareTo == 0)) {
 						status = TradeConstraintsStatus.CONSTRAINTS_OK;
 					} else {
 						status = TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 					}
 					break;
 				default:
 					throw new Exception("unhandled comparator " + constraint.getComparator());
 			}
 			
 			if (status == TradeConstraintsStatus.CONSTRAINTS_BROKEN) {
 				return TradeConstraintsStatus.CONSTRAINTS_BROKEN;
 			}
 		}
 		
 		return TradeConstraintsStatus.CONSTRAINTS_OK;
 	}
 	
 		
 	public static class TradeCombinationResult {
 		public Trade trade;
 		public boolean complete;
 	}
 
 	static public TradeCombinationResult tryAddTrade(Trade original, Trade newTrade) throws Exception {
 		
 		// we assume that 'original' is valid, within func limits, etc
 		
 		if (!SchemaUtils.areIdentical(original.getSchema(), newTrade.getSchema())) {
 			System.out.println("Cannot add trade, schemas differ");
 			return null;
 		}
 		System.out.println("schemas match, good");
 		
 		List<SinglePartyData> partiesData = newTrade.getAllPartiesDataList();
 		for (SinglePartyData partyData : partiesData) {
 			if (!isPartyDataValid(original.getSchema(), partyData)) {
 				System.out.println("Cannot add party data to trade as it doesn't validate against the schema");
 				return null;
 			}
 		}
 		System.out.println("validated extra party data against the schema ok");
 		
 		Trade.Builder provisionalTradeBuilder = Trade.newBuilder(original);
 		for (SinglePartyData partyData : partiesData) {
 			provisionalTradeBuilder.addAllPartiesData(partyData);
 		}
 		Trade provisionalTrade = provisionalTradeBuilder.build();
 
 		
 		VariableValues variableValues = evaluateVariables(provisionalTrade);
 		TradeConstraintsStatus constraintsStatus = evaluateConstraints(variableValues, provisionalTrade.getSchema().getConstraintsList());
 		if (constraintsStatus == TradeConstraintsStatus.CONSTRAINTS_BROKEN) {
 			System.out.println("Cannot add party data to trade as it breaks function limits");
 			return null;
 		}
 		System.out.println("Added party data, all constraints are ok");
 		
 		TradeConstraintsStatus completeStatus = evaluateConstraints(variableValues, provisionalTrade.getSchema().getCompletionRequirementsList());
 		
 		TradeCombinationResult result = new TradeCombinationResult();
 		result.trade = provisionalTrade;
 		result.complete = (completeStatus == TradeConstraintsStatus.CONSTRAINTS_OK);
 		
 		System.out.println("Completion status = " + result.complete);
 		return result;
 	}
 		
 	static public boolean areIdentical(GeneratedMessage a, GeneratedMessage b) {
 		return Arrays.equals(a.toByteArray(), b.toByteArray());
 	}
 	
 	static boolean dataItemMatchesSpecifier(DataItem item, DataItemSpecifier specifier) throws Exception {
 		System.out.println("dataItemMatchesSpecifier:");
 		System.out.println(item.toString());		
 		
 		if (!areIdentical(item.getReference(), specifier.getReference())) {
 			System.out.println("data item ref does not match specifier ref");
 			return false;
 		}
 		
 		if (specifier.getFixValue()) {
 			if (!item.hasValue()) {
 				System.out.println("data item does not have value");
 				return false;
 			}
 		}
 		else {
 			if (item.hasValue()) {
 				System.out.println("data item has value");
 				return false;
 			}
 		}
 		
 		if (specifier.getFixAddress()) {
 			switch (specifier.getReference().getRefType()) {
 				case INPUT:
 					if (!item.hasTxId() || !item.hasBlockId() || !item.hasOutputIndex()) {
 						System.out.println("data item doesn't have required fields for INPUT");
 						return false;
 					}
 					if (item.hasAddress()) {
 						System.out.println("data item has too many fields for INPUT");
 						return false;
 					}
 					break;
 				case OUTPUT:
 					if (!item.hasAddress()) {
 						System.out.println("data item doesn't have required fields for OUTPUT");
 						return false;
 					}
 					if (item.hasTxId() || item.hasBlockId() || item.hasOutputIndex()) {
 						System.out.println("data item has too many fields for OUTPUT");
 						return false;
 					}
 					break;
 				default:
 					throw new Exception("unhandled ref type");
 			}
 		}
 		else {
 			if (item.hasTxId() || item.hasBlockId() || item.hasOutputIndex() || item.hasAddress()) {
 				System.out.println("data item has too many fields for value");
 				return false;
 			}
 		}
 
 		return true;
 	}
 	
 	static boolean isPartyDataValid(TransactionSchema schema, SinglePartyData data) throws Exception {
 		
 		System.out.println("isPartyDataValid:");
 		System.out.println(data.toString());
 		
 		int partyTypeIdx = data.getPartyIdx();
 		if (schema.getPartyTypesCount() <= partyTypeIdx) {
 			System.out.println("party data type idx " + partyTypeIdx + " is invalid");
 			return false;
 		}
 		
 		// This checks that there is a 1-1 match between DataItems and Specifiers
 		//
		List<DataItem> unvalidatedItems = new ArrayList<DataItem>(data.getDataList());
 		PartyType partyType = schema.getPartyTypes(partyTypeIdx);
 		for (DataItemSpecifier specifier : partyType.getSpecifiersList()) {
 			DataItem match = null;
 			for (DataItem item : unvalidatedItems) {
 				if (dataItemMatchesSpecifier(item, specifier)) {
 					match = item;
 					break;
 				}
 			}
 			if (match == null) {
 				System.out.println("Specifier had no matching DataItem");
 				System.out.println(specifier.toString());
 				return false;
 			} else {
 				System.out.println("Matched specifier to DataItem");
 				System.out.println(match.toString());
 				unvalidatedItems.remove(match);
 			}
 		}
 		if (unvalidatedItems.size() > 0) {
 			System.out.println("not all DataItems validated against the schema PartyType");
 			for (DataItem i : unvalidatedItems) {
 				System.out.println(i.toString());
 			}
 			return false;
 		}
 
 		return true;
 	}
 
 
 	public static BigInteger readBigInteger(TransactionSchemaProtos.BigInteger btcValue) {
 		byte[] orig = btcValue.getData().toByteArray();
 		byte[] list = orig;
 		
 		System.out.println("readBigInteger " + new String(Hex.encodeHex(orig)));
 		/*
 		byte[] list = new byte[orig.length];
 		for (int i = 0; i < list.length; ++i) {
 			list[i] = orig[list.length - i - 1];
 		}
 		*/
 		return new BigInteger(list);
 	}
 	
 	public static TransactionSchemaProtos.BigInteger writeBigInteger(BigInteger v) {
 		System.out.println("writeBigInteger " + new String(Hex.encodeHex(v.toByteArray())));
 		
 		TransactionSchemaProtos.BigInteger btcValue = TransactionSchemaProtos.BigInteger.newBuilder()
 				.setData(ByteString.copyFrom(v.toByteArray()))
 				.build();
 		
 		return btcValue;
 	}
 }
