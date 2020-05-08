 package edu.umd.cs.bachfna13.learning;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import edu.umd.cs.psl.application.learning.weight.maxlikelihood.VotedPerceptron;
 import edu.umd.cs.psl.application.util.GroundKernels;
 import edu.umd.cs.psl.config.ConfigBundle;
 import edu.umd.cs.psl.database.Database;
 import edu.umd.cs.psl.model.Model;
 import edu.umd.cs.psl.model.atom.Atom;
 import edu.umd.cs.psl.model.atom.GroundAtom;
 import edu.umd.cs.psl.model.atom.ObservedAtom;
 import edu.umd.cs.psl.model.atom.RandomVariableAtom;
 import edu.umd.cs.psl.model.kernel.BindingMode;
 import edu.umd.cs.psl.model.kernel.CompatibilityKernel;
 import edu.umd.cs.psl.model.kernel.GroundCompatibilityKernel;
 import edu.umd.cs.psl.model.kernel.GroundKernel;
 import edu.umd.cs.psl.model.parameters.NegativeWeight;
 import edu.umd.cs.psl.model.parameters.PositiveWeight;
 import edu.umd.cs.psl.model.parameters.Weight;
 import edu.umd.cs.psl.reasoner.function.FunctionSum;
 import edu.umd.cs.psl.reasoner.function.FunctionSummand;
 import edu.umd.cs.psl.reasoner.function.FunctionTerm;
 
 public class SVMStructRank extends VotedPerceptron {
 	
 	private static final Logger log = LoggerFactory.getLogger(SVMStructRank.class);
 	
 	protected Map<RandomVariableAtom, LinearPenaltyGroundKernel> lossMap;
 	protected RandomVariableAtom[] atoms;
 	protected int totalPos, totalNeg;
 
 	public SVMStructRank(Model model, Database rvDB, Database observedDB, ConfigBundle config) {
 		super(model, rvDB, observedDB, config);
 	}
 	
 	@Override
 	protected void doLearn() {
 		/* Modifies objective, collects atoms as array, and counts positive links */
 		totalPos = 0; totalNeg = 0;
 		lossMap = new HashMap<RandomVariableAtom, SVMStructRank.LinearPenaltyGroundKernel>(trainingMap.getTrainingMap().size());
 		List<RandomVariableAtom> atomList = new ArrayList<RandomVariableAtom>(trainingMap.getTrainingMap().size());
 		for (Map.Entry<RandomVariableAtom, ObservedAtom> e : trainingMap.getTrainingMap().entrySet()) {
 			RandomVariableAtom atom = e.getKey();
 			LinearPenaltyGroundKernel gk =  new LinearPenaltyGroundKernel(atom);
 			lossMap.put(atom, gk);
 			reasoner.addGroundKernel(gk);
 			atomList.add(atom);
 			if (e.getValue().getValue() == 1.0)
 				totalPos++;
 			else if (e.getValue().getValue() == 0.0)
 				totalNeg++;
 			else
 				throw new IllegalStateException("Only Boolean training data are supported.");
 		}
 		atoms = atomList.toArray(new RandomVariableAtom[atomList.size()]);
		log.warn("Total positive links: {}", totalPos);
 		
 		super.doLearn();
 		
 		/* Unmodifies objective */
 		for (LinearPenaltyGroundKernel gk : lossMap.values())
 			reasoner.removeGroundKernel(gk);
 	}
 
 	@Override
 	protected double[] computeExpectedIncomp() {
 		Weight oldWeight, newWeight;
 		boolean runInference;
 		int round = 0;
 		do {
 			int numberOfActiveLossTerms = 0;
			log.warn("Running inference to compute derivative, round {}", round);
 			reasoner.optimize();
			log.warn("Objective: {}", GroundKernels.getTotalWeightedIncompatibility(reasoner.getCompatibilityKernels()));
 			runInference = false;
 			int numPos = 0, numNeg = 0;
 			Arrays.sort(atoms, new SVMRankStructComparator());
 //			for (RandomVariableAtom atom : atoms) {
 //				System.out.println(atom + " " + atom.getValue() + " " + trainingMap.getTrainingMap().get(atom).getValue());
 //			}
 			for (int i = 0; i < atoms.length; i++) {
 				oldWeight = lossMap.get(atoms[i]).getWeight();
 				if (trainingMap.getTrainingMap().get(atoms[i]).getValue() == 1.0) {
 					newWeight = new PositiveWeight((double) numNeg / totalPos / totalNeg);
 //					newWeight = new PositiveWeight((double) numNeg);
 					numPos++;
 				}
 				else {
 					newWeight = new NegativeWeight(((double) numPos - totalPos) / totalPos / totalNeg);
 //					newWeight = new NegativeWeight(((double) numPos - totalPos));
 					numNeg++;
 				}
 				if (newWeight.getWeight() != 0)
 					numberOfActiveLossTerms++;
 				if (!oldWeight.equals(newWeight)) {
 					lossMap.get(atoms[i]).setWeight(newWeight);
 					reasoner.changedGroundKernelWeight(lossMap.get(atoms[i]));
 					runInference = true;
 				}
 			}
			log.warn("Number of active loss terms: {}", numberOfActiveLossTerms);
 			round++;
 		}
 		while (runInference);
 		
 		/* Computes incompatibility */
 		numGroundings = new double[kernels.size()];
 		double[] truthIncompatibility = new double[kernels.size()];
 		
 		/* Computes the observed incompatibilities and numbers of groundings */
 		for (int i = 0; i < kernels.size(); i++) {
 			for (GroundKernel gk : reasoner.getGroundKernels(kernels.get(i))) {
 				truthIncompatibility[i] += ((GroundCompatibilityKernel) gk).getIncompatibility();
 				numGroundings[i]++;
 			}
 		}
 		
 		return truthIncompatibility;
 	}
 	
 	protected class SVMRankStructComparator implements Comparator<RandomVariableAtom> {
 		
 		private static final double epsilon = 1e-3;
 
 		@Override
 		public int compare(RandomVariableAtom o1, RandomVariableAtom o2) {
 			if (Math.abs(o1.getValue() - o2.getValue()) < epsilon) {
 				double o1True = SVMStructRank.this.trainingMap.getTrainingMap().get(o1).getValue();
 				double o2True = SVMStructRank.this.trainingMap.getTrainingMap().get(o2).getValue();
 				
 				return Double.compare(o1True, o2True);
 			}
 			else return Double.compare(o2.getValue(), o1.getValue());
 		}
 		
 	}
 	
 	protected class LinearPenaltyGroundKernel implements GroundCompatibilityKernel {
 		
 		protected RandomVariableAtom atom;
 		protected Weight weight;
 		
 		protected LinearPenaltyGroundKernel(RandomVariableAtom atom) {
 			this.atom = atom;
 			weight = new PositiveWeight(0.0);
 		}
 
 		@Override
 		public boolean updateParameters() {
 			return false;
 		}
 
 		@Override
 		public Set<GroundAtom> getAtoms() {
 			Set<GroundAtom> atoms = new HashSet<GroundAtom>();
 			atoms.add(atom);
 			return atoms;
 		}
 
 		@Override
 		public BindingMode getBinding(Atom atom) {
 			if (this.atom.equals(atom))
 				return BindingMode.StrongRV;
 			else
 				return BindingMode.NoBinding;
 		}
 
 		@Override
 		public CompatibilityKernel getKernel() {
 			return null;
 		}
 
 		@Override
 		public Weight getWeight() {
 			return weight;
 		}
 
 		@Override
 		public void setWeight(Weight w) {
 			weight = w;
 		}
 
 		@Override
 		public FunctionTerm getFunctionDefinition() {
 			FunctionSum sum = new FunctionSum();
 			sum.add(new FunctionSummand(1.0, atom.getVariable()));
 			return sum;
 		}
 
 		@Override
 		public double getIncompatibility() {
 			return atom.getValue();
 		}
 		
 	}
 
 }
