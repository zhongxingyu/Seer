 /*
  * This file is part of the PSL software.
  * Copyright 2011 University of Maryland
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package edu.umd.cs.psl.model.kernel.rule;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 import edu.umd.cs.psl.model.atom.Atom;
 import edu.umd.cs.psl.model.atom.GroundAtom;
import edu.umd.cs.psl.model.formula.Conjunction;
 import edu.umd.cs.psl.model.formula.Formula;
 import edu.umd.cs.psl.model.formula.Negation;
 import edu.umd.cs.psl.model.formula.Tnorm;
 import edu.umd.cs.psl.model.formula.traversal.FormulaEvaluator;
 import edu.umd.cs.psl.model.kernel.BindingMode;
 import edu.umd.cs.psl.model.kernel.GroundKernel;
 import edu.umd.cs.psl.model.kernel.Kernel;
 import edu.umd.cs.psl.reasoner.function.ConstantNumber;
 import edu.umd.cs.psl.reasoner.function.FunctionSum;
 import edu.umd.cs.psl.reasoner.function.FunctionSummand;
 
 /**
  * Currently, we assume that the body of the rule is a conjunction and the head of the rule is
  * a disjunction of atoms.
  * 
  * @author Matthias Broecheler
  * @author Stephen Bach
  */
 
 abstract public class AbstractGroundRule implements GroundKernel {
 	
 	public static final Tnorm tnorm = Tnorm.LUKASIEWICZ;
 	public static final FormulaEvaluator formulaNorm =FormulaEvaluator.LUKASIEWICZ;
 	
 	protected final AbstractRuleKernel kernel;
 	protected final List<GroundAtom> posLiterals;
 	protected final List<GroundAtom> negLiterals;
 	
 	protected int numGroundings;
 
 	private final int hashcode;
 	
 	AbstractGroundRule(AbstractRuleKernel k, List<GroundAtom> posLiterals, List<GroundAtom> negLiterals) {
 		kernel = k;
 		this.posLiterals = new ArrayList<GroundAtom>(posLiterals);
 		this.negLiterals = new ArrayList<GroundAtom>(negLiterals);
 		
 		numGroundings=1;
 		
 		HashCodeBuilder hcb = new HashCodeBuilder();
 		hcb.append(kernel);
 		for (GroundAtom atom : posLiterals)
 			hcb.append(atom);
 		for (GroundAtom atom : negLiterals)
 			hcb.append(atom);
 		
 		hashcode = hcb.toHashCode();
 	}
 	
 	int getNumGroundings() {
 		return numGroundings;
 	}
 	
 	void increaseGroundings() {
 		numGroundings++;
 	}
 	
 	void decreaseGroundings() {
 		numGroundings--;
 		if (numGroundings <= 0)
 			throw new IllegalStateException("Non-positive number of groundings.");
 	}
 	
 	@Override
 	public boolean updateParameters() {
 		return true;
 	}
 	
 	protected FunctionSum getFunction(double multiplier) {
 		FunctionSum sum = new FunctionSum();
 		
 		for (GroundAtom atom : posLiterals)
 			sum.add(new FunctionSummand(multiplier, atom.getVariable()));
 		
 		for (GroundAtom atom : negLiterals)
 			sum.add(new FunctionSummand(-1*multiplier, atom.getVariable()));
 		
 		sum.add(new FunctionSummand(multiplier, new ConstantNumber(1.0 - posLiterals.size())));
 		
 		return sum;
 	}
 	
 	@Override
 	public Set<GroundAtom> getAtoms() {
 		HashSet<GroundAtom> atoms = new HashSet<GroundAtom>();
 		for (GroundAtom atom : posLiterals)
 			atoms.add(atom);
 		for (GroundAtom atom : negLiterals)
 			atoms.add(atom);
 		
 		return atoms;
 	}
 
 	@Override
 	public Kernel getKernel() {
 		return kernel;
 	}
 	
 	public double getTruthValue() {
 		return 1 - Math.max(getFunction(1.0).getValue(), 0);
 	}
 	
 	@Override
 	public BindingMode getBinding(Atom atom) {
 		if (getAtoms().contains(atom)) {
 			return BindingMode.StrongRV;
 		}
 		return BindingMode.NoBinding;
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if (other==this)
 			return true;
 		if (other==null || !(other instanceof AbstractGroundRule))
 			return false;
 		
 		AbstractGroundRule otherRule = (AbstractGroundRule) other;
 		if (!kernel.equals(otherRule.getKernel()))
 			return false;
 		
 		return posLiterals.equals(otherRule.posLiterals)
 				&& negLiterals.equals(otherRule.negLiterals);
 	}
 	
 	@Override
 	public int hashCode() {
 		return hashcode;
 	}
 	
 	@Override
 	public String toString() {
 		Formula[] literals = new Formula[posLiterals.size() + negLiterals.size()];
 		int i;
 		for (i = 0; i < posLiterals.size(); i++)
			literals[i] = posLiterals.get(i);
 		for (int j = 0; j < negLiterals.size(); j++)
			literals[i++] = new Negation(negLiterals.get(j));
 		
		return new Conjunction(literals).toString();
 	}
 }
