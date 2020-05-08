 package org.soulspace.template.method.impl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.soulspace.template.method.AbstractMethod;
 import org.soulspace.template.value.Value;
 import org.soulspace.template.value.impl.ListValueImpl;
 
 public class ReverseListMethodImpl extends AbstractMethod {
 
 	private static final String NAME = "reverse";
 	protected static final Class<? extends Value> RETURN_TYPE = ListValueImpl.class;
 	protected static final List<Class<? extends Value>> DEFINED_TYPES = new ArrayList<Class<? extends Value>>();
 	protected static final List<Class<? extends Value>> ARGUMENT_TYPES = new ArrayList<Class<? extends Value>>();
 
 	static {
 		DEFINED_TYPES.add(ListValueImpl.class);
 	}
 
 	public ReverseListMethodImpl() {
 		super();
 		this.returnType = RETURN_TYPE;
 		this.argumentTypes = ARGUMENT_TYPES;
 		this.definedTypes = DEFINED_TYPES;
 		this.name = NAME;
 	}
 
 	@Override
 	protected Value doEvaluation(List<Value> arguments) {
 		List<Value> resultList = new ArrayList<Value>();
 		ListValueImpl list = (ListValueImpl) arguments.get(0);
 		List<Value> inputList = list.getData();
		for(int i = inputList.size() - 1; i >= 0 ; i++) {
 			resultList.add(inputList.get(i));
 		}
 		return new ListValueImpl(resultList);
 	}
 
 }
