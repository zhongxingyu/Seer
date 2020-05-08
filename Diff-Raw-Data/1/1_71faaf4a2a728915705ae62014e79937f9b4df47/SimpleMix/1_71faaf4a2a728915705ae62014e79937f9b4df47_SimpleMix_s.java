 package com.dustyneuron.bitprivacy.schemas;
 
 
 import java.math.BigInteger;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ConstraintFormula.ForEach;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Element.ElementType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Expression;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ConstraintFormula;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ConstraintFormula.Comparator;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.IOTypeReference;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItem;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.PartyType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.ReferenceType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.SinglePartyData;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.DataItemSpecifier;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Trade;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.TransactionSchema;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.InputType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.OutputType;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Variable;
 import com.dustyneuron.bitprivacy.TransactionSchemaProtos.Variable.VariableType;
 import com.google.bitcoin.core.Address;
 import com.google.bitcoin.core.Transaction;
 import com.google.bitcoin.core.Utils;
 
 
 public class SimpleMix {
 	static Map<TransactionSchema, BigInteger> schemas = new HashMap<TransactionSchema, BigInteger>();
 	
 	
 	public static Trade createTrade(Transaction tx, int unspentOutputIdx, BigInteger value, Address dest, int numParties) {
 		TransactionSchema schema = getSchema(value, numParties);
 		Trade t = Trade.newBuilder()
 				.setSchema(schema)
 				.setTimestamp(new Date().getTime())
 				.addAllPartiesData(SinglePartyData.newBuilder()
 						.setPartyIdx(0)
 						.addData(DataItem.newBuilder()
 								.setReference(IOTypeReference.newBuilder()
 										.setRefType(ReferenceType.INPUT)
 										.setRefIdx(0)
 										.build())
 								.setBlockId(tx.getAppearsInHashes().iterator().next().toString())
 								.setTxId(tx.getHash().toString())
 								.setOutputIndex(unspentOutputIdx)
 								.build())
 						.addData(DataItem.newBuilder()
 								.setReference(IOTypeReference.newBuilder()
 										.setRefType(ReferenceType.OUTPUT)
 										.setRefIdx(0)
 										.build())
 								.setAddress(dest.toString())
 								.build())
 						.build())
 				.build();
 		
 		return t;
 	}
 		
 	static TransactionSchema getSchema(BigInteger v, int numParties) {
 		System.out.println("SimpleMix.getSchema(BigInteger " + Utils.bitcoinValueToFriendlyString(v) + ", numParties " + numParties + ")");
 		
 		/*
 		for (TransactionSchema s : schemas.keySet()) {
 			if (schemas.get(s).compareTo(v) == 0) {
 				return s;
 			}
 		}
 		*/
 		
 		TransactionSchemaProtos.BigInteger btcValue = SchemaUtils.writeBigInteger(v);
 		
 		System.out.println("SimpleMix.getSchema built btc byte[], reads back as " + Utils.bitcoinValueToFriendlyString(SchemaUtils.readBigInteger(btcValue)));
 		
 		
     	TransactionSchema schema = 
     			TransactionSchema.newBuilder()
     				.addInputTypes(InputType.newBuilder()
 						.build())
     				.addOutputTypes(OutputType.newBuilder()
 						.build())
 					.addPartyTypes(PartyType.newBuilder()
 						.addSpecifiers(DataItemSpecifier.newBuilder()
 							.setReference(IOTypeReference.newBuilder()
 								.setRefType(ReferenceType.INPUT)
 								.setRefIdx(0)
 								.build())
 							.setFixAddress(true)
 							.build())
 						.addSpecifiers(DataItemSpecifier.newBuilder()
 							.setReference(IOTypeReference.newBuilder()
 								.setRefType(ReferenceType.OUTPUT)
 								.setRefIdx(0)
 								.build())
 							.setFixAddress(true)
 							.build())
 						.build())
 					.addConstraints(ConstraintFormula.newBuilder()
 						.setForEach(ForEach.TYPEREF)
 						.setForEachRef(IOTypeReference.newBuilder()
 							.setRefType(ReferenceType.INPUT)
 							.setRefIdx(0)
 							.build())
 						.setComparator(Comparator.EQ)
 						.addLhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VARIABLE)
 								.setVariable(Variable.newBuilder()
 									.setVariableType(VariableType.FOREACH_BTCVALUE)
 									.build())
 								.build())
 							.build())
 						.addRhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VALUE)
 								.setValue(btcValue)
 								.build())
 							.build())
 						.build())
 					.addConstraints(ConstraintFormula.newBuilder()
 						.setForEach(ForEach.TYPEREF)
 						.setForEachRef(IOTypeReference.newBuilder()
 							.setRefType(ReferenceType.OUTPUT)
 							.setRefIdx(0)
 							.build())
 						.setComparator(Comparator.EQ)
 						.addLhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VARIABLE)
 								.setVariable(Variable.newBuilder()
 									.setVariableType(VariableType.FOREACH_BTCVALUE)
 									.build())
 								.build())
 							.build())
 						.addRhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VALUE)
 								.setValue(btcValue)
 								.build())
 							.build())
 						.build())
 					.addConstraints(ConstraintFormula.newBuilder()
 						.setComparator(Comparator.EQ)
 						.addLhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VARIABLE)
 								.setVariable(Variable.newBuilder()
 									.setVariableType(VariableType.SUM_TYPE)
 									.setRefType(ReferenceType.INPUT)
 									.build())
 								.build())
 							.build())
 						.addRhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VARIABLE)
 								.setVariable(Variable.newBuilder()
 									.setVariableType(VariableType.SUM_TYPE)
 									.setRefType(ReferenceType.OUTPUT)
 									.build())
 								.build())
 							.build())
 						.build())
 					.addCompletionRequirements(ConstraintFormula.newBuilder()
 						.setComparator(Comparator.EQ)
 						.addLhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VARIABLE)
 								.setVariable(Variable.newBuilder()
 									.setVariableType(VariableType.SUM_PARTYTYPE)
 									.setPartyTypeIndex(0)
 									.build())
 								.build())
 							.build())
 						.addRhs(Expression.newBuilder()
 							.setElement(Element.newBuilder()
 								.setElementType(ElementType.VALUE)
 								.setValue(SchemaUtils.writeBigInteger(new BigInteger(Integer.toString(numParties))))
 								.build())
 							.build())
 						.build())
 					.build();
     	
     	schemas.put(schema, v);
     	
     	//System.out.println(Hex.encodeHex(schema.toByteArray()));
     	//System.out.println();
     	//System.out.println(schema.toString());
     	return schema;
 	}
 }
