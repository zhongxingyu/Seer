 package com.mutation.events;
 
 import java.util.List;
 
 import com.mutation.EMutationOperator;
 import com.mutation.Mutant;
 import com.mutation.util.ToStringUtils;
 
 public class MutantsGenerated implements IEvent{
	private List<Mutant> generateMutants;
 	private String classUnderTest;
 	private EMutationOperator operator;
 	public String getClassUnderTest() {
 		return classUnderTest;
 	}
	public List<Mutant> getGenerateMutants() {
		return generateMutants;
 	}
 	public EMutationOperator getOperator() {
 		return operator;
 	}
 	public MutantsGenerated(List<Mutant> generateMutants, String classUnderTest, EMutationOperator operator) {
 		super();
		this.generateMutants = generateMutants;
 		this.classUnderTest = classUnderTest;
 		this.operator = operator;
 	}
 	
 	@Override
 	public String toString() {
 		return ToStringUtils.toString(this);
 	}
 }
