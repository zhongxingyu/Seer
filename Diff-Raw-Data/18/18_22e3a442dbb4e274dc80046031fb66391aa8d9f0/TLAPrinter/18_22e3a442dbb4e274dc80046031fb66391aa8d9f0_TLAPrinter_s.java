 package de.b2tla.prettyprint;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import de.b2tla.analysis.MachineContext;
 import de.b2tla.analysis.PrecedenceCollector;
 import de.b2tla.analysis.PrimedNodesMarker;
 import de.b2tla.analysis.Renamer;
 import de.b2tla.analysis.TypeRestrictor;
 import de.b2tla.analysis.Typechecker;
 import de.b2tla.analysis.UnchangedVariablesFinder;
 import de.b2tla.analysis.UsedStandardModules;
 import de.b2tla.analysis.nodes.EqualsNode;
 import de.b2tla.analysis.nodes.NodeType;
 import de.b2tla.analysis.nodes.SubsetNode;
 import de.b2tla.btypes.BType;
 import de.b2tla.btypes.FunctionType;
 import de.b2tla.btypes.IntegerType;
 import de.b2tla.btypes.SetType;
 import de.b2tla.tla.ConfigFile;
 import de.b2tla.tla.TLADefinition;
 import de.b2tla.tla.TLAModule;
 import de.b2tla.tla.config.ConfigFileAssignment;
 import de.be4.classicalb.core.parser.Utils;
 import de.be4.classicalb.core.parser.analysis.DepthFirstAdapter;
 import de.be4.classicalb.core.parser.node.*;
 import static de.b2tla.analysis.StandardMadules.*;
 
 public class TLAPrinter extends DepthFirstAdapter {
 
 	private StringBuilder tlaModuleString;
 	private StringBuilder configFileString;
 
 	public StringBuilder getConfigString() {
 		return configFileString;
 	}
 
 	public StringBuilder getStringbuilder() {
 		return tlaModuleString;
 	}
 
 	private MachineContext machineContext;
 	private Typechecker typechecker;
 	private UnchangedVariablesFinder unchangedVariablesFinder;
 	private PrecedenceCollector precedenceCollector;
 	private UsedStandardModules usedStandardModules;
 	private TypeRestrictor typeRestrictor;
 	private TLAModule tlaModule;
 	private ConfigFile configFile;
 	private PrimedNodesMarker primedNodesMarker;
 	private Renamer renamer;
 
 	public TLAPrinter(MachineContext machineContext, Typechecker typechecker,
 			UnchangedVariablesFinder unchangedVariablesFinder,
 			PrecedenceCollector precedenceCollector,
 			UsedStandardModules usedStandardModules,
 			TypeRestrictor typeRestrictor, TLAModule tlaModule,
 			ConfigFile configFile, PrimedNodesMarker primedNodesMarker,
 			Renamer renamer) {
 		this.typechecker = typechecker;
 		this.machineContext = machineContext;
 		this.unchangedVariablesFinder = unchangedVariablesFinder;
 		this.precedenceCollector = precedenceCollector;
 		this.usedStandardModules = usedStandardModules;
 		this.typeRestrictor = typeRestrictor;
 		this.tlaModule = tlaModule;
 		this.configFile = configFile;
 		this.primedNodesMarker = primedNodesMarker;
 		this.renamer = renamer;
 
 		this.tlaModuleString = new StringBuilder();
 		this.configFileString = new StringBuilder();
 
 		// Start start = machineContext.getTree();
 		// start.apply(this);
 	}
 
 	public void start() {
 		printHeader();
 		printExtendedModules();
 		printConstants();
 		printVariables();
 		printDefinitions();
 		printAssume();
 		printInvariant();
 		printAssertions();
 		printInit();
 		printOperations();
 
 		tlaModuleString.append("====");
 
 		printConfig();
 	}
 
 	private void printConfig() {
 		if (this.configFile.isInit()) {
 			this.configFileString.append("INIT Init\n");
 		}
 		if (this.configFile.isInit()) {
 			this.configFileString.append("NEXT Next\n");
 		}
 		if (configFile.isInvariant()) {
 			this.configFileString.append("INVARIANT Invariant\n");
 		}
 
 		if (configFile.isGoal()) {
 			this.configFileString.append("INVARIANT NotGoal\n");
 		}
 
 		if (configFile.getAssertionSize() > 0) {
 			for (int i = 0; i < configFile.getAssertionSize(); i++) {
 				this.configFileString.append("INVARIANT Assertion" + (i + 1)
 						+ "\n");
 			}
 
 		}
 
 		// CONSTANTS
 
 		ArrayList<ConfigFileAssignment> assignments = configFile
 				.getAssignments();
 		if (assignments.size() != 0) {
 			configFileString.append("CONSTANTS\n");
 			for (int i = 0; i < assignments.size(); i++) {
 				configFileString.append(assignments.get(i).getString());
 			}
 		}
 	}
 
 	private void printHeader() {
 		tlaModuleString.append("---- MODULE ");
 		tlaModuleString.append(this.tlaModule.getModuleName());
 		tlaModuleString.append(" ----\n");
 	}
 
 	private void printExtendedModules() {
 		if (usedStandardModules.getUsedModules().size() > 0) {
 			tlaModuleString.append("EXTENDS ");
 			for (int i = 0; i < usedStandardModules.getUsedModules().size(); i++) {
 				if (i > 0) {
 					tlaModuleString.append(", ");
 				}
 				tlaModuleString.append(usedStandardModules.getUsedModules()
 						.get(i));
 			}
 			tlaModuleString.append("\n");
 		}
 	}
 
 	private void printDefinitions() {
 		ArrayList<TLADefinition> definitions = tlaModule.getDefinitions();
 		for (int i = 0; i < definitions.size(); i++) {
 			TLADefinition def = definitions.get(i);
 			if (def.getDefName() instanceof AEnumeratedSetSet) {
 				def.getDefName().apply(this);
 				continue;
 			}
 			def.getDefName().apply(this);
 			tlaModuleString.append(" == ");
 			Node e = def.getDefinition();
 			if (e == null) {
 				tlaModuleString.append(def.getInt());
 			} else {
 				e.apply(this);
 			}
 			tlaModuleString.append("\n");
 		}
 
 		ArrayList<PDefinition> bDefinition = tlaModule.getAllDefinitions();
 		if (null == bDefinition) {
 			return;
 		}
 		for (PDefinition node : bDefinition) {
 			node.apply(this);
 		}
 		if (configFile.isGoal()) {
 			tlaModuleString.append("NotGoal == ~GOAL\n");
 		}
 	}
 
 	private void printConstants() {
 		ArrayList<Node> list = this.tlaModule.getConstants();
 		if (list.size() == 0)
 			return;
 		tlaModuleString.append("CONSTANTS ");
 		for (int i = 0; i < list.size(); i++) {
 			list.get(i).apply(this);
 			if (i < list.size() - 1)
 				tlaModuleString.append(", ");
 		}
 		tlaModuleString.append("\n");
 	}
 
 	private void printAssume() {
 		ArrayList<Node> list = this.tlaModule.getAssume();
 		if (list.size() == 0)
 			return;
 
 		for (int i = 0; i < list.size(); i++) {
 			tlaModuleString.append("ASSUME ");
 			list.get(i).apply(this);
 			tlaModuleString.append("\n");
 		}
 
 	}
 
 	private void printVariables() {
 		ArrayList<Node> vars = this.tlaModule.getVariables();
 		if (vars.size() == 0)
 			return;
 		tlaModuleString.append("VARIABLES ");
 		for (int i = 0; i < vars.size(); i++) {
 			vars.get(i).apply(this);
 			if (i < vars.size() - 1)
 				tlaModuleString.append(", ");
 		}
 		tlaModuleString.append("\n");
 	}
 
 	private void printInvariant() {
 		PPredicate node = this.tlaModule.getInvariant();
 		if (node == null)
 			return;
 
 		tlaModuleString.append("Invariant == ");
 		node.apply(this);
 		tlaModuleString.append("\n");
 	}
 
 	private void printAssertions() {
 		ArrayList<Node> assertions = tlaModule.getAssertions();
 		if (assertions.size() == 0)
 			return;
 		for (int i = 0; i < assertions.size(); i++) {
 			tlaModuleString.append("Assertion" + (i + 1) + " == ");
 			assertions.get(i).apply(this);
 			tlaModuleString.append("\n");
 		}
 
 	}
 
 	private void printInit() {
 		ArrayList<Node> inits = this.tlaModule.getInitPredicates();
 		if (inits.size() == 0)
 			return;
 		tlaModuleString.append("Init == ");
 		for (int i = 0; i < inits.size(); i++) {
 			inits.get(i).apply(this);
 			if (i < inits.size() - 1)
 				tlaModuleString.append(" /\\ ");
 		}
 		tlaModuleString.append("\n");
 	}
 
 	private void printOperations() {
 		ArrayList<POperation> ops = this.tlaModule.getOperations();
 		if (ops.size() == 0) {
 			ArrayList<Node> vars = tlaModule.getVariables();
 			if (vars.size() > 0) {
 				tlaModuleString.append("Next == 1 = 2 /\\ UNCHANGED <<");
 				for (int i = 0; i < vars.size(); i++) {
 					vars.get(i).apply(this);
 					if (i < vars.size() - 1) {
 						tlaModuleString.append(", ");
 					}
 				}
 				tlaModuleString.append(">>\n");
 			}
 			return;
 		}
 		for (int i = 0; i < ops.size(); i++) {
 			ops.get(i).apply(this);
 		}
 		tlaModuleString.append("Next == \\/ ");
 		Iterator<Node> itr = this.machineContext.getOperations().values()
 				.iterator();
 		while (itr.hasNext()) {
 			Node operation = itr.next();
 			printOperationCall(operation);
 
 			if (itr.hasNext()) {
 				tlaModuleString.append("\n\t\\/ ");
 			}
 		}
 		tlaModuleString.append("\n");
 	}
 
 	private void printOperationCall(Node operation) {
 		AOperation op = (AOperation) operation;
 		List<PExpression> newList = new ArrayList<PExpression>();
 		newList.addAll(op.getParameters());
 		// newList.addAll(op.getReturnValues());
 		if (newList.size() > 0) {
 			tlaModuleString.append("\\E ");
 			for (int i = 0; i < newList.size(); i++) {
 				PExpression e = newList.get(i);
 				e.apply(this);
 				tlaModuleString.append(" \\in ");
 				printTypeOfIdentifier(e);
 				if (i < newList.size() - 1) {
 					tlaModuleString.append(", ");
 				}
 			}
 			tlaModuleString.append(" : ");
 		}
 
 		String opName = renamer.getNameOfRef(op);
 		tlaModuleString.append(opName);
 		if (newList.size() > 0) {
 			tlaModuleString.append("(");
 			for (int i = 0; i < newList.size(); i++) {
 				newList.get(i).apply(this);
 				if (i < newList.size() - 1) {
 					tlaModuleString.append(", ");
 				}
 
 			}
 			tlaModuleString.append(")");
 		}
 	}
 
 	@Override
 	public void defaultIn(final Node node) {
 		if (precedenceCollector.getBrackets().contains(node)) {
 			tlaModuleString.append("(");
 		}
 	}
 
 	@Override
 	public void defaultOut(final Node node) {
 		if (precedenceCollector.getBrackets().contains(node)) {
 			tlaModuleString.append(")");
 		}
 	}
 
 	/*
 	 * Treewalker
 	 */
 
 	@Override
 	public void caseAMachineHeader(AMachineHeader node) {
 		inAMachineHeader(node);
 		tlaModuleString.append(node);
 		{
 			List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(
 					node.getName());
 			for (TIdentifierLiteral e : copy) {
 				e.apply(this);
 			}
 		}
 		{
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getParameters());
 			for (PExpression e : copy) {
 				e.apply(this);
 			}
 		}
 		outAMachineHeader(node);
 	}
 
 	@Override
 	public void caseAEnumeratedSetSet(AEnumeratedSetSet node) {
 		inAEnumeratedSetSet(node);
 		{
 			List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(
 					node.getIdentifier());
 
 			String setName = Utils.getIdentifierAsString(copy);
 
 			tlaModuleString.append(renamer.getNameOfRef(node) + " == {");
 		}
 		{
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getElements());
 			for (int i = 0; i < copy.size(); i++) {
 				copy.get(i).apply(this);
 				if (i < copy.size() - 1) {
 					tlaModuleString.append(", ");
 				}
 			}
 		}
 		tlaModuleString.append("}\n");
 		outAEnumeratedSetSet(node);
 
 	}
 
 	@Override
 	public void caseADeferredSetSet(ADeferredSetSet node) {
 		inADeferredSetSet(node);
 		{
 			List<TIdentifierLiteral> copy = new ArrayList<TIdentifierLiteral>(
 					node.getIdentifier());
 			for (TIdentifierLiteral e : copy) {
 				e.apply(this);
 				tlaModuleString.append(e.getText());
 			}
 		}
 		outADeferredSetSet(node);
 	}
 
 	/**
 	 * Substitutions
 	 * 
 	 */
 
 	@Override
 	public void caseABecomesElementOfSubstitution(
 			ABecomesElementOfSubstitution node) {
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		for (int i = 0; i < copy.size(); i++) {
 			if (i != 0) {
 				tlaModuleString.append(" /\\ ");
 			}
 			copy.get(i).apply(this);
 			tlaModuleString.append(" \\in ");
 			node.getSet().apply(this);
 		}
 		printUnchangedVariables(node, true);
 	}
 
 	@Override
 	public void caseAAssignSubstitution(AAssignSubstitution node) {
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getLhsExpression());
 		List<PExpression> copy2 = new ArrayList<PExpression>(
 				node.getRhsExpressions());
 
 		for (int i = 0; i < copy.size(); i++) {
 			PExpression left = copy.get(i);
 			PExpression right = copy2.get(i);
 
 			if (left instanceof AFunctionExpression) {
 				printFunctionAssignment(left, right);
 
 			} else {
 				printNormalAssignment(left, right);
 			}
 
 			if (i < copy.size() - 1) {
 				tlaModuleString.append(" /\\ ");
 			}
 		}
 
 		printUnchangedVariables(node, true);
 	}
 
 	@Override
 	public void caseABecomesSuchSubstitution(ABecomesSuchSubstitution node) {
 		node.getPredicate().apply(this);
 		printUnchangedVariables(node, true);
 	}
 
 	private void printNormalAssignment(PExpression left, PExpression right) {
 		AIdentifierExpression id = (AIdentifierExpression) left;
 		String name = Utils.getIdentifierAsString(id.getIdentifier());
 		if (!machineContext.getVariables().containsKey(name)) {
 			tlaModuleString.append("TRUE");
 		} else {
 			left.apply(this);
 			tlaModuleString.append(" = ");
 			right.apply(this);
 		}
 	}
 
 	private void printFunctionAssignment(PExpression left, PExpression right) {
 		PExpression var = ((AFunctionExpression) left).getIdentifier();
 		LinkedList<PExpression> params = ((AFunctionExpression) left)
 				.getParameters();
 		BType type = typechecker.getType(var);
 		if (type instanceof FunctionType) {
 			var.apply(this);
 			tlaModuleString.append("' = [");
 			var.apply(this);
 			tlaModuleString.append(" EXCEPT ![");
 			for (Iterator<PExpression> iterator = params.iterator(); iterator
 					.hasNext();) {
 				PExpression pExpression = (PExpression) iterator.next();
 				pExpression.apply(this);
 				if (iterator.hasNext()) {
 					tlaModuleString.append(", ");
 				}
 			}
 			tlaModuleString.append("] = ");
 			right.apply(this);
 			tlaModuleString.append("]");
 		} else {
 			var.apply(this);
 			tlaModuleString.append("' = ");
 			tlaModuleString.append(REL_OVERRIDING + "(");
 			var.apply(this);
 			tlaModuleString.append(", {<<");
 
 			if (params.size() > 1) {
 				tlaModuleString.append("<<");
 				for (Iterator<PExpression> iterator = params.iterator(); iterator
 						.hasNext();) {
 					PExpression pExpression = (PExpression) iterator.next();
 					pExpression.apply(this);
 					if (iterator.hasNext()) {
 						tlaModuleString.append(", ");
 					}
 				}
 				tlaModuleString.append(">>");
 			} else {
 				params.get(0).apply(this);
 			}
 			tlaModuleString.append(", ");
 			right.apply(this);
 			tlaModuleString.append(">>})");
 		}
 	}
 
 	public void printUnchangedVariables(Node node, boolean printAnd) {
 		HashSet<Node> unchangedVariablesSet = unchangedVariablesFinder
 				.getUnchangedVariablesTable().get(node);
 		if (null != unchangedVariablesSet) {
 			ArrayList<Node> unchangedVariables = new ArrayList<Node>(
 					unchangedVariablesSet);
 			if (unchangedVariables.size() > 0) {
 				if (printAnd) {
 					tlaModuleString.append(" /\\");
 				}
 				tlaModuleString.append(" UNCHANGED <<");
 				for (int i = 0; i < unchangedVariables.size(); i++) {
 					unchangedVariables.get(i).apply(this);
 					if (i < unchangedVariables.size() - 1) {
 						tlaModuleString.append(", ");
 					}
 				}
 				tlaModuleString.append(">>");
 			} else {
 				if (!printAnd) {
 					// there is already a /\
 					tlaModuleString.append("TRUE");
 				}
 			}
 		}
 	}
 
 	@Override
 	public void caseAChoiceSubstitution(AChoiceSubstitution node) {
 		List<PSubstitution> copy = new ArrayList<PSubstitution>(
 				node.getSubstitutions());
 		tlaModuleString.append("(");
 		for (int i = 0; i < copy.size(); i++) {
 			tlaModuleString.append("(");
 			copy.get(i).apply(this);
 			tlaModuleString.append(")");
 			if (i < copy.size() - 1) {
 				tlaModuleString.append(" \\/ ");
 			}
 
 		}
 		tlaModuleString.append(")");
 		printUnchangedVariables(node, true);
 	}
 
 	@Override
 	public void caseASkipSubstitution(ASkipSubstitution node) {
 		printUnchangedVariables(node, false);
 	}
 
 	@Override
 	public void caseAIfSubstitution(AIfSubstitution node) {
 		tlaModuleString.append("(IF ");
 		node.getCondition().apply(this);
 		tlaModuleString.append(" THEN ");
 		node.getThen().apply(this);
 		List<PSubstitution> copy = new ArrayList<PSubstitution>(
 				node.getElsifSubstitutions());
 		for (PSubstitution e : copy) {
 			e.apply(this);
 		}
 		tlaModuleString.append(" ELSE ");
 		if (node.getElse() != null) {
 			node.getElse().apply(this);
 		} else {
 			printUnchangedVariablesNull(node, false);
 		}
 
 		tlaModuleString.append(")");
 		printUnchangedVariables(node, true);
 	}
 
 	public void printUnchangedVariablesNull(Node node, boolean printAnd) {
 		HashSet<Node> unchangedVariablesSet = unchangedVariablesFinder
 				.getUnchangedVariablesNull().get(node);
 		if (null != unchangedVariablesSet) {
 			ArrayList<Node> unchangedVariables = new ArrayList<Node>(
 					unchangedVariablesSet);
 			if (unchangedVariables.size() > 0) {
 				if (printAnd) {
 					tlaModuleString.append(" /\\");
 				}
 				tlaModuleString.append(" UNCHANGED <<");
 				for (int i = 0; i < unchangedVariables.size(); i++) {
 					unchangedVariables.get(i).apply(this);
 					if (i < unchangedVariables.size() - 1) {
 						tlaModuleString.append(", ");
 					}
 				}
 				tlaModuleString.append(">>");
 			}
 		}
 	}
 
 	@Override
 	public void caseAParallelSubstitution(AParallelSubstitution node) {
 		for (Iterator<PSubstitution> itr = node.getSubstitutions().iterator(); itr
 				.hasNext();) {
 			PSubstitution e = itr.next();
 
 			e.apply(this);
 
 			if (itr.hasNext()) {
 				tlaModuleString.append("\n\t/\\ ");
 			}
 		}
 
 		printUnchangedVariables(node, true);
 	}
 
 	@Override
 	public void caseAPreconditionSubstitution(APreconditionSubstitution node) {
 		inAPreconditionSubstitution(node);
 
 		node.getPredicate().apply(this);
 		tlaModuleString.append("\n\t/\\ ");
 		node.getSubstitution().apply(this);
 
 		outAPreconditionSubstitution(node);
 	}
 
 	@Override
 	public void caseAAssertionSubstitution(AAssertionSubstitution node) {
 		inAAssertionSubstitution(node);
 		node.getPredicate().apply(this);
 		tlaModuleString.append("\n\t/\\ ");
 		node.getSubstitution().apply(this);
 		outAAssertionSubstitution(node);
 	}
 
 	@Override
 	public void caseASelectSubstitution(ASelectSubstitution node) {
 		inASelectSubstitution(node);
 		tlaModuleString.append("(( ");
 
 		tlaModuleString.append("((");
 		node.getCondition().apply(this);
 		tlaModuleString.append(")");
 
 		tlaModuleString.append(" /\\ ");
 		tlaModuleString.append("(");
 		node.getThen().apply(this);
 		tlaModuleString.append("))");
 
 		List<PSubstitution> copy = new ArrayList<PSubstitution>(
 				node.getWhenSubstitutions());
 		for (PSubstitution e : copy) {
 			tlaModuleString.append("\n\t\\/ ");
 			e.apply(this);
 		}
 		if (node.getElse() != null) {
 			tlaModuleString.append("\n\t/\\ ");
 			node.getElse().apply(this);
 		}
 		tlaModuleString.append(")");
 		printUnchangedVariables(node, true);
 		tlaModuleString.append(")");
 		outASelectSubstitution(node);
 	}
 
 	@Override
 	public void caseASelectWhenSubstitution(ASelectWhenSubstitution node) {
 		tlaModuleString.append("(");
 		node.getCondition().apply(this);
 		tlaModuleString.append(" /\\ ");
 		node.getSubstitution().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAAnySubstitution(AAnySubstitution node) {
 		inAAnySubstitution(node);
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		if (copy.size() > 0) {
 			tlaModuleString.append("\\E ");
 			for (int i = 0; i < copy.size(); i++) {
 				PExpression e = copy.get(i);
 				e.apply(this);
 				tlaModuleString.append(" \\in ");
 				printTypeOfIdentifier(e);
 				if (i < copy.size() - 1) {
 					tlaModuleString.append(", ");
 				}
 			}
 			tlaModuleString.append(" : ");
 		}
 
 		node.getWhere().apply(this);
 		tlaModuleString.append("\n\t/\\ ");
 		node.getThen().apply(this);
 		printUnchangedVariables(node, true);
 		outAAnySubstitution(node);
 	}
 
 	@Override
 	public void caseALetSubstitution(ALetSubstitution node) {
 		inALetSubstitution(node);
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		if (copy.size() > 0) {
 			tlaModuleString.append("\\E ");
 			for (int i = 0; i < copy.size(); i++) {
 				PExpression e = copy.get(i);
 				e.apply(this);
 				tlaModuleString.append(" \\in ");
 				printTypeOfIdentifier(e);
 				if (i < copy.size() - 1) {
 					tlaModuleString.append(", ");
 				}
 			}
 			tlaModuleString.append(" : ");
 		}
 
 		node.getPredicate().apply(this);
 		tlaModuleString.append("\n\t/\\ ");
 		node.getSubstitution().apply(this);
 		printUnchangedVariables(node, true);
 
 		outALetSubstitution(node);
 	}
 
 	@Override
 	public void caseAOperation(AOperation node) {
 		String name = renamer.getNameOfRef(node);
 		tlaModuleString.append(name);
 		List<PExpression> output = new ArrayList<PExpression>(
 				node.getReturnValues());
 		List<PExpression> params = new ArrayList<PExpression>(
 				node.getParameters());
 		List<PExpression> newList = new ArrayList<PExpression>();
 		newList.addAll(params);
 		// newList.addAll(output);
 
 		if (newList.size() > 0) {
 			tlaModuleString.append("(");
 			for (int i = 0; i < newList.size(); i++) {
 				if (i != 0) {
 					tlaModuleString.append(", ");
 				}
 				newList.get(i).apply(this);
 			}
 			tlaModuleString.append(")");
 		}
 		tlaModuleString.append(" == ");
 		if (node.getOperationBody() != null) {
 			node.getOperationBody().apply(this);
 		}
 
 		printUnchangedConstants();
 
 		tlaModuleString.append("\n\n");
 	}
 
 	private void printUnchangedConstants() {
 		ArrayList<Node> vars = new ArrayList<Node>(tlaModule.getVariables());
 		vars.removeAll(machineContext.getVariables().values());
 		if (vars.size() > 0) {
 			tlaModuleString.append(" /\\ UNCHANGED <<");
 			for (int i = 0; i < vars.size(); i++) {
 				if (i != 0)
 					tlaModuleString.append(", ");
 				vars.get(i).apply(this);
 			}
 
 			tlaModuleString.append(">>");
 		}
 	}
 
 	/** Expression **/
 
 	@Override
 	public void caseAIdentifierExpression(AIdentifierExpression node) {
 		inAIdentifierExpression(node);
 		String name = renamer.getNameOfRef(node);
 		if (name == null) {
 			name = Utils.getIdentifierAsString(node.getIdentifier());
 		}
 		tlaModuleString.append(name);
 		if (primedNodesMarker.isPrimed(node)) {
 			tlaModuleString.append("'");
 		}
 		outAIdentifierExpression(node);
 	}
 
 	@Override
 	public void caseAPrimedIdentifierExpression(APrimedIdentifierExpression node) {
 		String name = renamer.getNameOfRef(node);
 		if (name == null) {
 			name = Utils.getIdentifierAsString(node.getIdentifier());
 		}
 		tlaModuleString.append(name);
 	}
 
 	@Override
 	public void caseAStringExpression(AStringExpression node) {
 		inAStringExpression(node);
 		tlaModuleString.append("\"");
 		tlaModuleString.append(node.getContent().getText().toString());
 		tlaModuleString.append("\"");
 		outAStringExpression(node);
 	}
 
 	@Override
 	public void caseAStringSetExpression(AStringSetExpression node) {
 		tlaModuleString.append("STRING");
 	}
 
 	/**
 	 * Logical Predicates
 	 */
 
 	@Override
 	public void caseAEqualPredicate(AEqualPredicate node) {
 		inAEqualPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" = ");
 		node.getRight().apply(this);
 		outAEqualPredicate(node);
 	}
 
 	@Override
 	public void caseANotEqualPredicate(ANotEqualPredicate node) {
 		inANotEqualPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" # ");
 		node.getRight().apply(this);
 		outANotEqualPredicate(node);
 	}
 
 	@Override
 	public void caseAConjunctPredicate(AConjunctPredicate node) {
 		boolean left = typeRestrictor.removeNode(node.getLeft());
 		boolean right = typeRestrictor.removeNode(node.getRight());
 		int i = left ? (right ? 1 : 2) : (right ? 3 : 4);
 
 		switch (i) {
 		case 1: // remove both
 			tlaModuleString.append("TRUE");
 			break;
 		case 2: // remove left
 			node.getRight().apply(this);
 			break;
 		case 3: // remove right
 			node.getLeft().apply(this);
 			break;
 		case 4:
 			inAConjunctPredicate(node);
 			node.getLeft().apply(this);
 			tlaModuleString.append(" /\\ ");
 			node.getRight().apply(this);
 			outAConjunctPredicate(node);
 			break;
 		}
 	}
 
 	@Override
 	public void caseADisjunctPredicate(ADisjunctPredicate node) {
 		inADisjunctPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\/ ");
 		node.getRight().apply(this);
 		outADisjunctPredicate(node);
 	}
 
 	@Override
 	public void caseAImplicationPredicate(AImplicationPredicate node) {
 		inAImplicationPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" => ");
 		node.getRight().apply(this);
 		outAImplicationPredicate(node);
 	}
 
 	@Override
 	public void caseAEquivalencePredicate(AEquivalencePredicate node) {
 		inAEquivalencePredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" <=> ");
 		node.getRight().apply(this);
 		outAEquivalencePredicate(node);
 	}
 
 	@Override
 	public void caseABoolSetExpression(ABoolSetExpression node) {
 		tlaModuleString.append("BOOLEAN");
 	}
 
 	@Override
 	public void caseABooleanTrueExpression(ABooleanTrueExpression node) {
 		inABooleanTrueExpression(node);
 		tlaModuleString.append("TRUE");
 		outABooleanTrueExpression(node);
 	}
 
 	@Override
 	public void caseABooleanFalseExpression(ABooleanFalseExpression node) {
 		inABooleanFalseExpression(node);
 		tlaModuleString.append("FALSE");
 		outABooleanFalseExpression(node);
 	}
 
 	@Override
 	public void caseAForallPredicate(AForallPredicate node) {
 		/*
 		 * B: !x,y(T => P) TLA: \A x \in type(x), y \in type(y): T => P
 		 */
 		inAForallPredicate(node);
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 
 		tlaModuleString.append("\\A ");
 		for (int i = 0; i < copy.size(); i++) {
 			PExpression e = copy.get(i);
 			e.apply(this);
 			tlaModuleString.append(" \\in ");
 			printTypeOfIdentifier(e);
 			if (i < copy.size() - 1) {
 				tlaModuleString.append(", ");
 			}
 		}
 		tlaModuleString.append(" : ");
 		node.getImplication().apply(this);
 		outAForallPredicate(node);
 	}
 
 	@Override
 	public void caseAExistsPredicate(AExistsPredicate node) {
 		/*
 		 * B: #x,y(T => P) TLA: \E x \in type(x), y \in type(y): T => P
 		 */
 		inAExistsPredicate(node);
 		tlaModuleString.append("\\E ");
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		for (int i = 0; i < copy.size(); i++) {
 			PExpression e = copy.get(i);
 			e.apply(this);
 			tlaModuleString.append(" \\in ");
 			printTypeOfIdentifier(e);
 			if (i < copy.size() - 1) {
 				tlaModuleString.append(", ");
 			}
 		}
 		tlaModuleString.append(" : ");
 		if (typeRestrictor.removeNode(node.getPredicate())) {
 			tlaModuleString.append("TRUE");
 		} else {
 			node.getPredicate().apply(this);
 		}
 		outAExistsPredicate(node);
 	}
 
 	@Override
 	public void caseANegationPredicate(ANegationPredicate node) {
 		inANegationPredicate(node);
 		tlaModuleString.append("\\neg(");
 		node.getPredicate().apply(this);
 		tlaModuleString.append(")");
 		outANegationPredicate(node);
 	}
 
 	@Override
 	public void caseAIntegerExpression(AIntegerExpression node) {
 		inAIntegerExpression(node);
 		if (node.getLiteral() != null) {
 			tlaModuleString.append(node.getLiteral().getText());
 			// node.getLiteral().apply(this);
 		}
 		outAIntegerExpression(node);
 	}
 
 	@Override
 	public void caseAPredicateDefinitionDefinition(
 			APredicateDefinitionDefinition node) {
 		String name = renamer.getNameOfRef(node);
 		if (null == name) {
 			name = node.getName().getText().trim();
 		}
 		printBDefinition(name, node.getParameters(), node.getRhs());
 	}
 
 	@Override
 	public void caseAExpressionDefinitionDefinition(
 			AExpressionDefinitionDefinition node) {
 		String name = renamer.getName(node);
 		if (null == name) {
 			name = node.getName().getText().trim();
 		}
 		printBDefinition(name, node.getParameters(), node.getRhs());
 	}
 
 	@Override
 	public void caseASubstitutionDefinitionDefinition(
 			ASubstitutionDefinitionDefinition node) {
 		String name = renamer.getNameOfRef(node);
 		if (null == name) {
 			name = node.getName().getText().trim();
 		}
 		printBDefinition(name, node.getParameters(), node.getRhs());
 	}
 
 	private void printBDefinition(String name, List<PExpression> args,
 			Node rightSide) {
 		tlaModuleString.append(name);
 		if (args.size() > 0) {
 			tlaModuleString.append("(");
 			for (int i = 0; i < args.size(); i++) {
 				if (i != 0)
 					tlaModuleString.append(", ");
 				args.get(i).apply(this);
 			}
 			tlaModuleString.append(")");
 		}
 
 		tlaModuleString.append(" == ");
 		rightSide.apply(this);
 		tlaModuleString.append("\n");
 	}
 
 	@Override
 	public void caseADefinitionExpression(ADefinitionExpression node) {
 		String name = renamer.getNameOfRef(node);
 		if (null == name) {
 			name = node.getDefLiteral().getText().trim();
 		}
 		printBDefinitionCall(name, node.getParameters());
 	}
 
 	@Override
 	public void caseADefinitionPredicate(ADefinitionPredicate node) {
 		String name = renamer.getNameOfRef(node);
 		if (null == name) {
 			name = node.getDefLiteral().getText().trim();
 		}
 		printBDefinitionCall(name, node.getParameters());
 	}
 
 	@Override
 	public void caseADefinitionSubstitution(ADefinitionSubstitution node) {
 		String name = renamer.getNameOfRef(node);
 		if (null == name) {
 			name = node.getDefLiteral().getText().trim();
 		}
 		printBDefinitionCall(name, node.getParameters());
 	}
 
 	public void printBDefinitionCall(String name, List<PExpression> args) {
 		tlaModuleString.append(name);
 		if (args.size() > 0) {
 			tlaModuleString.append("(");
 			for (int i = 0; i < args.size(); i++) {
 				if (i != 0)
 					tlaModuleString.append(", ");
 				args.get(i).apply(this);
 
 			}
 			tlaModuleString.append(")");
 		}
 	}
 
 	/**
 	 * Numbers
 	 */
 
 	@Override
 	public void caseAIntegerSetExpression(AIntegerSetExpression node) {
 		inAIntegerSetExpression(node);
 		tlaModuleString.append("Int");
 		outAIntegerSetExpression(node);
 	}
 
 	@Override
 	public void caseANaturalSetExpression(ANaturalSetExpression node) {
 		inANaturalSetExpression(node);
 		tlaModuleString.append("Nat");
 		outANaturalSetExpression(node);
 	}
 
 	@Override
 	public void caseANatural1SetExpression(ANatural1SetExpression node) {
 		inANatural1SetExpression(node);
 		tlaModuleString.append("(Nat \\ {0})");
 		outANatural1SetExpression(node);
 	}
 
 	@Override
 	public void caseAIntSetExpression(AIntSetExpression node) {
 		inAIntSetExpression(node);
 		tlaModuleString.append("Int");
 		outAIntSetExpression(node);
 	}
 
 	@Override
 	public void caseANatSetExpression(ANatSetExpression node) {
 		inANatSetExpression(node);
 		tlaModuleString.append("Nat");
 		outANatSetExpression(node);
 	}
 
 	@Override
 	public void caseANat1SetExpression(ANat1SetExpression node) {
 		inANat1SetExpression(node);
 		tlaModuleString.append("(Nat \\ {0})");
 		outANat1SetExpression(node);
 	}
 
 	@Override
 	public void caseAIntervalExpression(AIntervalExpression node) {
 		inAIntervalExpression(node);
 		tlaModuleString.append("(");
 		node.getLeftBorder().apply(this);
 		tlaModuleString.append(" .. ");
 		node.getRightBorder().apply(this);
 		tlaModuleString.append(")");
 		outAIntervalExpression(node);
 	}
 
 	@Override
 	public void caseAGreaterPredicate(AGreaterPredicate node) {
 		inAGreaterPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" > ");
 		node.getRight().apply(this);
 		outAGreaterPredicate(node);
 	}
 
 	@Override
 	public void caseALessPredicate(ALessPredicate node) {
 		inALessPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" < ");
 		node.getRight().apply(this);
 		outALessPredicate(node);
 	}
 
 	@Override
 	public void caseAGreaterEqualPredicate(AGreaterEqualPredicate node) {
 		inAGreaterEqualPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" >= ");
 		node.getRight().apply(this);
 		outAGreaterEqualPredicate(node);
 	}
 
 	@Override
 	public void caseALessEqualPredicate(ALessEqualPredicate node) {
 		inALessEqualPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" =< ");
 		node.getRight().apply(this);
 		outALessEqualPredicate(node);
 	}
 
 	@Override
 	public void caseAMinExpression(AMinExpression node) {
 		inAMinExpression(node);
 		tlaModuleString.append("CHOOSE min \\in ");
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : \\A p \\in ");
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : min =< p");
 		outAMinExpression(node);
 	}
 
 	@Override
 	public void caseAMaxExpression(AMaxExpression node) {
 		inAMaxExpression(node);
 		tlaModuleString.append("(CHOOSE max \\in ");
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : \\A p \\in ");
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : max >= p)");
 		outAMaxExpression(node);
 	}
 
 	@Override
 	public void caseAUnaryMinusExpression(AUnaryMinusExpression node) {
 		inAUnaryMinusExpression(node);
 		tlaModuleString.append("-");
 		node.getExpression().apply(this);
 		outAUnaryMinusExpression(node);
 	}
 
 	@Override
 	public void caseAAddExpression(AAddExpression node) {
 		inAAddExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" + ");
 		node.getRight().apply(this);
 		outAAddExpression(node);
 	}
 
 	@Override
 	public void caseADivExpression(ADivExpression node) {
 		inADivExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\div ");
 		node.getRight().apply(this);
 		outADivExpression(node);
 	}
 
 	@Override
 	public void caseAPowerOfExpression(APowerOfExpression node) {
 		inAPowerOfExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" ^ ");
 		node.getRight().apply(this);
 		outAPowerOfExpression(node);
 	}
 
 	@Override
 	public void caseAModuloExpression(AModuloExpression node) {
 		/**
 		 * TODO a mod b: IF a < 0 THEN ERROR ELSE a % b
 		 */
 		inAModuloExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" % ");
 		node.getRight().apply(this);
 		outAModuloExpression(node);
 	}
 
 	@Override
 	public void caseAGeneralProductExpression(AGeneralProductExpression node) {
 		inAGeneralProductExpression(node);
 		tlaModuleString.append("PI({");
 
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : ");
 
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		printIdentifierList(copy);
 
 		tlaModuleString.append(" \\in { ");
 
 		printIdentifierList(copy);
 
 		tlaModuleString.append(" \\in ");
 		printTypesOfIdentifierList(copy);
 		tlaModuleString.append(" : ");
 		node.getPredicates().apply(this);
 		tlaModuleString.append("}");
 
 		tlaModuleString.append("}");
 		outAGeneralProductExpression(node);
 	}
 
 	@Override
 	public void caseAGeneralSumExpression(AGeneralSumExpression node) {
 		inAGeneralSumExpression(node);
 		tlaModuleString.append("SIGMA({");
 
 		node.getExpression().apply(this);
 		tlaModuleString.append(" : ");
 
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		printIdentifierList(copy);
 
 		tlaModuleString.append(" \\in { ");
 
 		printIdentifierList(copy);
 
 		tlaModuleString.append(" \\in ");
 		printTypesOfIdentifierList(copy);
 		tlaModuleString.append(" : ");
 		node.getPredicates().apply(this);
 		tlaModuleString.append("}");
 		tlaModuleString.append("})");
 		outAGeneralSumExpression(node);
 	}
 
 	@Override
 	public void caseASuccessorExpression(ASuccessorExpression node) {
 		inASuccessorExpression(node);
 		tlaModuleString.append("succ");
 		outASuccessorExpression(node);
 	}
 
 	@Override
 	public void caseAPredecessorExpression(APredecessorExpression node) {
 		inAPredecessorExpression(node);
 		tlaModuleString.append("pred");
 		outAPredecessorExpression(node);
 	}
 
 	/**
 	 * Function
 	 */
 
 	private void printIdentifierList(List<PExpression> copy) {
 		if (copy.size() > 1) {
 			tlaModuleString.append("<<");
 		}
 		for (int i = 0; i < copy.size(); i++) {
 			copy.get(i).apply(this);
 			if (i < copy.size() - 1)
 				tlaModuleString.append(", ");
 		}
 		if (copy.size() > 1) {
 			tlaModuleString.append(">>");
 		}
 	}
 
 	private void printTypeOfIdentifier(PExpression e) {
 		// NodeType n = typeRestrictor.getRestrictedTypes().get(e);
 		ArrayList<NodeType> list = typeRestrictor.getRestrictedTypesSet(e);
 		if (list != null) {
 			for (int i = 0; i < list.size(); i++) {
 				if (i != 0)
 					tlaModuleString.append(" \\cap (");
 				printNodeType(list.get(i));
 				if (i != 0)
 					tlaModuleString.append(")");
 			}
 		} else {
 			tlaModuleString.append(typechecker.getType(e).getTlaType());
 		}
 	}
 
 	private void printNodeType(NodeType n) {
 		if (n instanceof EqualsNode) {
 			tlaModuleString.append("{");
 			n.getExpression().apply(this);
 			tlaModuleString.append("}");
 		} else if (n instanceof SubsetNode) {
 			tlaModuleString.append("SUBSET(");
 			n.getExpression().apply(this);
 			tlaModuleString.append(")");
 		} else {
 			n.getExpression().apply(this);
 		}
 	}
 
 	private void printTypesOfIdentifierList(List<PExpression> copy) {
 		if (copy.size() > 1) {
 			tlaModuleString.append("(");
 		}
 		for (int i = 0; i < copy.size(); i++) {
 			printTypeOfIdentifier(copy.get(i));
 
 			if (i < copy.size() - 1)
 				tlaModuleString.append(" \\times ");
 		}
 		if (copy.size() > 1) {
 			tlaModuleString.append(")");
 		}
 	}
 
 	/*****************************************************************************
 	 * Functions
 	 *****************************************************************************/
 
 	@Override
 	public void caseALambdaExpression(ALambdaExpression node) {
 		/**
 		 * B: %a,b.(P|e) TLA+ function: [<<a,b>> \in {<<a,b>> \in
 		 * type(a)*type(b) : P}|e] relation: TLA+: {<< <<a,b>>, e>>: <<a,b>> \in
 		 * {<<a,b>> \in type(a)*type(b): P}}
 		 */
 		inALambdaExpression(node);
 		if (this.typechecker.getType(node) instanceof SetType) {
 			tlaModuleString.append("{<<");
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getIdentifiers());
 			printIdentifierList(copy);
 			tlaModuleString.append(", ");
 			node.getExpression().apply(this);
 			tlaModuleString.append(">> : ");
 
 			printIdentifierList(copy);
 
 			tlaModuleString.append(" \\in ");
 
 			if (typeRestrictor.removeNode(node.getPredicate())) {
 				printTypesOfIdentifierList(copy);
 			} else {
 				tlaModuleString.append("{");
 				printIdentifierList(copy);
 				tlaModuleString.append(" \\in ");
 				printTypesOfIdentifierList(copy);
 				tlaModuleString.append(" : ");
 				node.getPredicate().apply(this);
 				tlaModuleString.append("}");
 			}
 			tlaModuleString.append("}");
 
 		} else {
 			tlaModuleString.append("[");
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getIdentifiers());
 			printIdentifierList(copy);
 
 			tlaModuleString.append(" \\in ");
 			if (typeRestrictor.removeNode(node.getPredicate())) {
 				printTypesOfIdentifierList(copy);
 
 			} else {
 				tlaModuleString.append("{");
 				printIdentifierList(copy);
 
 				tlaModuleString.append(" \\in ");
 				printTypesOfIdentifierList(copy);
 				tlaModuleString.append(" : ");
 				node.getPredicate().apply(this);
 				tlaModuleString.append("}");
 			}
 			tlaModuleString.append(" |-> ");
 			node.getExpression().apply(this);
 			tlaModuleString.append("]");
 		}
 		outALambdaExpression(node);
 	}
 
 	@Override
 	// Functioncall
 	public void caseAFunctionExpression(AFunctionExpression node) {
 		inAFunctionExpression(node);
 
 		BType type = this.typechecker.getType(node.getIdentifier());
 		if (type instanceof FunctionType) {
 			node.getIdentifier().apply(this);
 			tlaModuleString.append("[");
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getParameters());
 			for (int i = 0; i < copy.size(); i++) {
 				if (i != 0) {
 					tlaModuleString.append(", ");
 				}
 				copy.get(i).apply(this);
 			}
 			tlaModuleString.append("]");
 		} else {
 			tlaModuleString.append(REL_CALL + "(");
 			node.getIdentifier().apply(this);
 			tlaModuleString.append(", ");
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getParameters());
 			if (copy.size() > 1)
 				tlaModuleString.append("<<");
 			for (int i = 0; i < copy.size(); i++) {
 				if (i != 0) {
 					tlaModuleString.append(", ");
 				}
 				copy.get(i).apply(this);
 			}
 			if (copy.size() > 1)
 				tlaModuleString.append(">>");
 			tlaModuleString.append(")");
 		}
 
 		outAFunctionExpression(node);
 	}
 
 	@Override
 	public void caseARangeExpression(ARangeExpression node) {
 		if (typechecker.getType(node.getExpression()) instanceof FunctionType) {
 			tlaModuleString.append(RANGE);
 		} else {
 			tlaModuleString.append(REL_RANGE);
 		}
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAImageExpression(AImageExpression node) {
 		if (typechecker.getType(node.getLeft()) instanceof FunctionType) {
 			tlaModuleString.append(IMAGE);
 		} else {
 			tlaModuleString.append(REL_IMAGE);
 		}
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseATotalFunctionExpression(ATotalFunctionExpression node) {
 		BType type = this.typechecker.getType(node);
 		BType subtype = ((SetType) type).getSubtype();
 		if (subtype instanceof FunctionType) {
 			tlaModuleString.append("[");
 			node.getLeft().apply(this);
 			tlaModuleString.append(" -> ");
 			node.getRight().apply(this);
 			tlaModuleString.append("]");
 		} else {
			if (node.parent() instanceof AMemberPredicate) {
 				tlaModuleString.append(REL_TOTAL_FUNCTION_ELEMENT_OF);
 			} else {
 				tlaModuleString.append(REL_TOTAL_FUNCTION);
 			}
 			tlaModuleString.append("(");
 			node.getLeft().apply(this);
 			tlaModuleString.append(", ");
 			node.getRight().apply(this);
 			tlaModuleString.append(")");
 		}
 	}
 
 	private void setOfFuntions(Node node, String funcName, String relName,
 			String relEleOfName, Node left, Node right) {
 		BType type = this.typechecker.getType(node);
 		BType subtype = ((SetType) type).getSubtype();
 		if (subtype instanceof FunctionType) {
 			tlaModuleString.append(funcName);
 		} else {
			if (node.parent() instanceof AMemberPredicate) {
 				tlaModuleString.append(relEleOfName);
 			} else {
 				tlaModuleString.append(relName);
 			}
 		}
 
 		tlaModuleString.append("(");
 		left.apply(this);
 		tlaModuleString.append(", ");
 		right.apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseATotalInjectionExpression(ATotalInjectionExpression node) {
 		setOfFuntions(node, TOTAL_INJECTIVE_FUNCTION,
 				REL_TOTAL_INJECTIVE_FUNCTION,
 				REL_TOTAL_INJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseATotalSurjectionExpression(ATotalSurjectionExpression node) {
 		setOfFuntions(node, TOTAL_SURJECTIVE_FUNCTION,
 				REL_TOTAL_SURJECTIVE_FUNCTION,
 				REL_TOTAL_SURJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseATotalBijectionExpression(ATotalBijectionExpression node) {
 		setOfFuntions(node, TOTAL_BIJECTIVE_FUNCTION,
 				REL_TOTAL_BIJECTIVE_FUNCTION,
 				REL_TOTAL_BIJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseAPartialFunctionExpression(APartialFunctionExpression node) {
 		setOfFuntions(node, PARTIAL_FUNCTION, REL_PARTIAL_FUNCTION,
 				REL_PARTIAL_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseAPartialInjectionExpression(APartialInjectionExpression node) {
 		setOfFuntions(node, PARTIAL_INJECTIVE_FUNCTION,
 				REL_PARTIAL_INJECTIVE_FUNCTION,
 				REL_PARTIAL_INJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseAPartialSurjectionExpression(
 			APartialSurjectionExpression node) {
 		setOfFuntions(node, PARTIAL_SURJECTIVE_FUNCTION,
 				REL_PARTIAL_SURJECTIVE_FUNCTION,
 				REL_PARTIAL_SURJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	@Override
 	public void caseAPartialBijectionExpression(APartialBijectionExpression node) {
 		setOfFuntions(node, PARITAL_BIJECTIVE_FUNCTION,
 				REL_PARTIAL_BIJECTIVE_FUNCTION,
 				REL_PARTIAL_BIJECTIVE_FUNCTION_ELEMENT_OF, node.getLeft(),
 				node.getRight());
 	}
 
 	/**
 	 * Sets
 	 */
 
 	@Override
 	public void caseASetExtensionExpression(ASetExtensionExpression node) {
 		inASetExtensionExpression(node);
 		tlaModuleString.append("{");
 		{
 			List<PExpression> copy = new ArrayList<PExpression>(
 					node.getExpressions());
 			for (int i = 0; i < copy.size(); i++) {
 				if (i != 0) {
 					tlaModuleString.append(", ");
 				}
 				copy.get(i).apply(this);
 			}
 		}
 		tlaModuleString.append("}");
 		outASetExtensionExpression(node);
 	}
 
 	@Override
 	public void caseAEmptySetExpression(AEmptySetExpression node) {
 		tlaModuleString.append("{}");
 	}
 
 	@Override
 	public void caseAMemberPredicate(AMemberPredicate node) {
 		inAMemberPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\in ");
 		node.getRight().apply(this);
 		outAMemberPredicate(node);
 	}
 
 	@Override
 	public void caseANotMemberPredicate(ANotMemberPredicate node) {
 		inANotMemberPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\notin ");
 		node.getRight().apply(this);
 		outANotMemberPredicate(node);
 	}
 
 	@Override
 	public void caseAComprehensionSetExpression(AComprehensionSetExpression node) {
 		inAComprehensionSetExpression(node);
 		tlaModuleString.append("{");
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 		printIdentifierList(copy);
 		tlaModuleString.append(" \\in ");
 		printTypesOfIdentifierList(copy);
 		tlaModuleString.append(": ");
 		if (typeRestrictor.removeNode(node.getPredicates())) {
 			tlaModuleString.append("TRUE");
 		} else {
 			node.getPredicates().apply(this);
 		}
 		tlaModuleString.append("}");
 		outAComprehensionSetExpression(node);
 	}
 
 	@Override
 	public void caseAUnionExpression(AUnionExpression node) {
 		inAUnionExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\cup ");
 		node.getRight().apply(this);
 		outAUnionExpression(node);
 	}
 
 	@Override
 	public void caseAIntersectionExpression(AIntersectionExpression node) {
 		inAIntersectionExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\cap ");
 		node.getRight().apply(this);
 		outAIntersectionExpression(node);
 	}
 
 	@Override
 	public void caseASetSubtractionExpression(ASetSubtractionExpression node) {
 		inASetSubtractionExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\ ");
 		node.getRight().apply(this);
 		outASetSubtractionExpression(node);
 	}
 
 	@Override
 	public void caseAPowSubsetExpression(APowSubsetExpression node) {
 		inAPowSubsetExpression(node);
 		tlaModuleString.append("SUBSET(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 		outAPowSubsetExpression(node);
 	}
 
 	@Override
 	public void caseAPow1SubsetExpression(APow1SubsetExpression node) {
 		tlaModuleString.append("POW1(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAFinSubsetExpression(AFinSubsetExpression node) {
 		tlaModuleString.append("FIN(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAFin1SubsetExpression(AFin1SubsetExpression node) {
 		tlaModuleString.append("FIN1(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseACardExpression(ACardExpression node) {
 		inACardExpression(node);
 		tlaModuleString.append("Cardinality(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 		outACardExpression(node);
 	}
 
 	@Override
 	public void caseASubsetPredicate(ASubsetPredicate node) {
 		inASubsetPredicate(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\subseteq ");
 		node.getRight().apply(this);
 		outASubsetPredicate(node);
 	}
 
 	@Override
 	public void caseASubsetStrictPredicate(ASubsetStrictPredicate node) {
 		inASubsetStrictPredicate(node);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\subseteq  ");
 		node.getRight().apply(this);
 		tlaModuleString.append(" /\\ ");
 		node.getLeft().apply(this);
 		tlaModuleString.append(" # ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 		outASubsetStrictPredicate(node);
 	}
 
 	@Override
 	public void caseANotSubsetPredicate(ANotSubsetPredicate node) {
 		inANotSubsetPredicate(node);
 		tlaModuleString.append("notSubset(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 		outANotSubsetPredicate(node);
 	}
 
 	@Override
 	public void caseANotSubsetStrictPredicate(ANotSubsetStrictPredicate node) {
 		inANotSubsetStrictPredicate(node);
 		tlaModuleString.append("notStrictSubset(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 		outANotSubsetStrictPredicate(node);
 	}
 
 	@Override
 	public void caseAGeneralUnionExpression(AGeneralUnionExpression node) {
 		inAGeneralUnionExpression(node);
 		tlaModuleString.append("Union(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 		outAGeneralUnionExpression(node);
 	}
 
 	@Override
 	public void caseAGeneralIntersectionExpression(
 			AGeneralIntersectionExpression node) {
 		inAGeneralIntersectionExpression(node);
 		tlaModuleString.append("Inter(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 		outAGeneralIntersectionExpression(node);
 	}
 
 	@Override
 	public void caseAQuantifiedUnionExpression(AQuantifiedUnionExpression node) {
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 
 		tlaModuleString.append("Union({");
 		node.getExpression().apply(this);
 		tlaModuleString.append(": ");
 		printIdentifierList(copy);
 		tlaModuleString.append(" \\in {");
 		printIdentifierList(copy);
 		tlaModuleString.append(" \\in ");
 		printTypesOfIdentifierList(copy);
 		tlaModuleString.append(": ");
 		node.getPredicates().apply(this);
 		tlaModuleString.append("}");
 		tlaModuleString.append("})");
 	}
 
 	@Override
 	public void caseAQuantifiedIntersectionExpression(
 			AQuantifiedIntersectionExpression node) {
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getIdentifiers());
 
 		tlaModuleString.append("Inter({");
 		node.getExpression().apply(this);
 		tlaModuleString.append(": ");
 		printIdentifierList(copy);
 		tlaModuleString.append(" \\in {");
 		printIdentifierList(copy);
 		tlaModuleString.append(" \\in ");
 		printTypesOfIdentifierList(copy);
 		tlaModuleString.append(": ");
 		node.getPredicates().apply(this);
 		tlaModuleString.append("}");
 		tlaModuleString.append("})");
 	}
 
 	/**
 	 * Relations
 	 */
 
 	@Override
 	public void caseACoupleExpression(ACoupleExpression node) {
 		inACoupleExpression(node);
 		List<PExpression> copy = new ArrayList<PExpression>(node.getList());
 		for (int i = 0; i < copy.size() - 1; i++) {
 			tlaModuleString.append("<<");
 		}
 		for (int i = 0; i < copy.size(); i++) {
 			if (i != 0) {
 				tlaModuleString.append(", ");
 			}
 			copy.get(i).apply(this);
 			if (i != 0) {
 				tlaModuleString.append(">>");
 			}
 		}
 		outACoupleExpression(node);
 	}
 
 	@Override
 	public void caseARelationsExpression(ARelationsExpression node) {
 		tlaModuleString.append(RELATION + "(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseADomainExpression(ADomainExpression node) {
 		inADomainExpression(node);
 		if (typechecker.getType(node.getExpression()) instanceof FunctionType) {
 			tlaModuleString.append("DOMAIN ");
 			node.getExpression().apply(this);
 		} else {
 			tlaModuleString.append(REL_DOMAIN + "(");
 			node.getExpression().apply(this);
 			tlaModuleString.append(")");
 		}
 		outADomainExpression(node);
 	}
 
 	@Override
 	public void caseAIdentityExpression(AIdentityExpression node) {
 		inAIdentityExpression(node);
 		tlaModuleString.append(REL_ID + "(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 		outAIdentityExpression(node);
 	}
 
 	@Override
 	public void caseADomainRestrictionExpression(
 			ADomainRestrictionExpression node) {
 		tlaModuleString.append(REL_DOMAIN_RESTRICTION);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseADomainSubtractionExpression(
 			ADomainSubtractionExpression node) {
 		tlaModuleString.append(REL_DOMAIN_SUBSTRACTION);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseARangeRestrictionExpression(ARangeRestrictionExpression node) {
 		tlaModuleString.append(REL_RANGE_RESTRICTION);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseARangeSubtractionExpression(ARangeSubtractionExpression node) {
 		tlaModuleString.append(REL_RANGE_SUBSTRACTION);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAReverseExpression(AReverseExpression node) {
 		tlaModuleString.append(REL_INVERSE);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAOverwriteExpression(AOverwriteExpression node) {
 		tlaModuleString.append(REL_OVERRIDING);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseADirectProductExpression(ADirectProductExpression node) {
 		tlaModuleString.append(REL_DIRECT_PRODUCT);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAParallelProductExpression(AParallelProductExpression node) {
 		tlaModuleString.append(REL_PARALLEL_PRODUCT);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseACompositionExpression(ACompositionExpression node) {
 		tlaModuleString.append(REL_COMPOSITION);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAFirstProjectionExpression(AFirstProjectionExpression node) {
 		tlaModuleString.append(REL_PROJECTION_FUNCTION_FIRST);
 		tlaModuleString.append("(");
 		node.getExp1().apply(this);
 		tlaModuleString.append(", ");
 		node.getExp2().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseASecondProjectionExpression(ASecondProjectionExpression node) {
 		tlaModuleString.append(REL_PROJECTION_FUNCTION_SECOND);
 		tlaModuleString.append("(");
 		node.getExp1().apply(this);
 		tlaModuleString.append(", ");
 		node.getExp2().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAIterationExpression(AIterationExpression node) {
 		tlaModuleString.append(REL_ITERATE);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAClosureExpression(AClosureExpression node) {
 		tlaModuleString.append(REL_CLOSURE1);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAReflexiveClosureExpression(AReflexiveClosureExpression node) {
 		tlaModuleString.append(REL_CLOSURE);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseATransFunctionExpression(ATransFunctionExpression node) {
 		tlaModuleString.append(REL_FNC);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseATransRelationExpression(ATransRelationExpression node) {
 		tlaModuleString.append(REL_REL);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	/**
 	 * Sequences
 	 */
 
 	@Override
 	public void caseASequenceExtensionExpression(
 			ASequenceExtensionExpression node) {
 		List<PExpression> copy = new ArrayList<PExpression>(
 				node.getExpression());
 		BType type = typechecker.getType(node);
 		if (type instanceof FunctionType) {
 			tlaModuleString.append("<<");
 
 			for (int i = 0; i < copy.size(); i++) {
 				copy.get(i).apply(this);
 				if (i < copy.size() - 1)
 					tlaModuleString.append(", ");
 			}
 			tlaModuleString.append(">>");
 		} else {
 			tlaModuleString.append("{");
 			for (int i = 0; i < copy.size(); i++) {
 				tlaModuleString.append("<<");
 				tlaModuleString.append(i + 1);
 				tlaModuleString.append(", ");
 				copy.get(i).apply(this);
 				tlaModuleString.append(">>");
 				if (i < copy.size() - 1)
 					tlaModuleString.append(", ");
 			}
 			tlaModuleString.append("}");
 		}
 	}
 
 	private void evalFunctionOrRelation(Node node, String function,
 			String relation) {
 		BType type = typechecker.getType(node);
 		if (type instanceof FunctionType) {
 			tlaModuleString.append(function);
 		} else {
 			tlaModuleString.append(relation);
 		}
 	}
 
 	@Override
 	public void caseAEmptySequenceExpression(AEmptySequenceExpression node) {
 		evalFunctionOrRelation(node, "<<>>", "{}");
 	}
 
 	@Override
 	public void caseASeqExpression(ASeqExpression node) {
 		tlaModuleString.append("Seq");
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseASizeExpression(ASizeExpression node) {
 		tlaModuleString.append("Len");
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAConcatExpression(AConcatExpression node) {
 		inAConcatExpression(node);
 		node.getLeft().apply(this);
 		tlaModuleString.append(" \\o ");
 		node.getRight().apply(this);
 		outAConcatExpression(node);
 	}
 
 	@Override
 	public void caseAInsertTailExpression(AInsertTailExpression node) {
 		tlaModuleString.append("Append");
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAFirstExpression(AFirstExpression node) {
 		tlaModuleString.append("Head");
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseATailExpression(ATailExpression node) {
 		tlaModuleString.append("Tail");
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	/**
 	 * SequencesExtended
 	 */
 
 	@Override
 	public void caseAIseqExpression(AIseqExpression node) {
		if (node.parent() instanceof AMemberPredicate) {
 			tlaModuleString.append(INJECTIVE_SEQUENCE_ELEMENT_OF);
 		} else {
 			tlaModuleString.append(INJECTIVE_SEQUENCE);
 		}
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAIseq1Expression(AIseq1Expression node) {
		if (node.parent() instanceof AMemberPredicate) {
 			tlaModuleString.append(INJECTIVE_SEQUENCE_1_ELEMENT_OF);
 		} else {
 			tlaModuleString.append(INJECTIVE_SEQUENCE_1);
 		}
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseASeq1Expression(ASeq1Expression node) {
 		tlaModuleString.append(SEQUENCE_1);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseALastExpression(ALastExpression node) {
 		tlaModuleString.append(SEQUENCE_LAST_ELEMENT);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAInsertFrontExpression(AInsertFrontExpression node) {
 		tlaModuleString.append(SEQUENCE_PREPEND_ELEMENT);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAPermExpression(APermExpression node) {
 		tlaModuleString.append(SEQUENCE_PERMUTATION);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseARevExpression(ARevExpression node) {
 		tlaModuleString.append(SEQUENCE_REVERSE);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAFrontExpression(AFrontExpression node) {
 		tlaModuleString.append(SEQUENCE_FRONT);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseAGeneralConcatExpression(AGeneralConcatExpression node) {
 		tlaModuleString.append(SEQUENCE_GENERAL_CONCATINATION);
 		tlaModuleString.append("(");
 		node.getExpression().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseARestrictFrontExpression(ARestrictFrontExpression node) {
 		tlaModuleString.append(SEQUENCE_TAKE_FIRST_ELEMENTS);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	@Override
 	public void caseARestrictTailExpression(ARestrictTailExpression node) {
 		tlaModuleString.append(SEQUENCE_DROP_FIRST_ELEMENTS);
 		tlaModuleString.append("(");
 		node.getLeft().apply(this);
 		tlaModuleString.append(", ");
 		node.getRight().apply(this);
 		tlaModuleString.append(")");
 	}
 
 	/**
 	 * Special Operator
 	 */
 	@Override
 	public void caseAMinusOrSetSubtractExpression(
 			AMinusOrSetSubtractExpression node) {
 		inAMinusOrSetSubtractExpression(node);
 		node.getLeft().apply(this);
 
 		BType leftType = this.typechecker.getType(node.getLeft());
 		if (leftType instanceof IntegerType) {
 			tlaModuleString.append(" - ");
 		} else {
 			tlaModuleString.append(" \\ ");
 		}
 
 		node.getRight().apply(this);
 		outAMinusOrSetSubtractExpression(node);
 	}
 
 	@Override
 	public void caseAMultOrCartExpression(AMultOrCartExpression node) {
 		inAMultOrCartExpression(node);
 		node.getLeft().apply(this);
 
 		BType leftType = this.typechecker.getType(node.getLeft());
 		if (leftType instanceof IntegerType) {
 			tlaModuleString.append(" * ");
 		} else {
 			tlaModuleString.append(" \\times ");
 		}
 
 		node.getRight().apply(this);
 		outAMultOrCartExpression(node);
 	}
 }
