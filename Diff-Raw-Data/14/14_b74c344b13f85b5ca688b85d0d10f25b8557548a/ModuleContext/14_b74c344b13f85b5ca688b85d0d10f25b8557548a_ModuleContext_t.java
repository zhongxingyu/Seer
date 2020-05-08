 package translation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 
 import exceptions.ConfigFileErrorException;
 import exceptions.FrontEndException;
 import exceptions.SemanticErrorException;
 
 import tla2sany.semantic.ASTConstants;
 import tla2sany.semantic.AssumeNode;
 import tla2sany.semantic.ExprNode;
 import tla2sany.semantic.ExprOrOpArgNode;
 import tla2sany.semantic.FormalParamNode;
 import tla2sany.semantic.LetInNode;
 import tla2sany.semantic.ModuleNode;
 import tla2sany.semantic.OpApplNode;
 import tla2sany.semantic.OpArgNode;
 import tla2sany.semantic.OpDeclNode;
 import tla2sany.semantic.OpDefNode;
 import tla2sany.semantic.SemanticNode;
 import tla2sany.semantic.Subst;
 import tla2sany.semantic.SubstInNode;
 import tla2sany.semantic.SymbolNode;
 import tlc2.tool.BuiltInOPs;
 import tlc2.tool.ToolGlobals;
 import util.StandardModules;
 
 // contains the content of a tla+ module
 public class ModuleContext implements ASTConstants, ToolGlobals, TranslationGlobals {
 	private final ModuleNode moduleNode;
 	protected final int letParamsId;
 	protected Hashtable<String, OpDefNode> definitions;
 
 	// Constants which appear in the resulting B machine
 	private ArrayList<String> bConstants;
 	protected ArrayList<LetDef> globalLets;
 	protected ArrayList<String> letsNames;
 
 	protected Hashtable<String, ConstantObj> conObjs;
 	protected Hashtable<String, String> overrides;
 	protected ArrayList<String> setEnumeration;
 	protected ArrayList<String> invariants = new ArrayList<String>();
 	protected ArrayList<BOperation> bOperations;
 	protected ArrayList<BInit> inits;
 	protected ExprNode next;
 
 	public ModuleContext(ModuleNode rootModule, ConfigTypeChecker ctc)
 			throws FrontEndException, ConfigFileErrorException,
 			SemanticErrorException {
 
 		// set root Module
 		this.moduleNode = rootModule;
 
 		this.letParamsId = 13;
 
 		globalLets = new ArrayList<LetDef>();
 		letsNames = new ArrayList<String>();
 
 		evalDefinitions();
 		evalVariables();
 		evalConstants();
 
 		// determine the constants of the module
 		// evalConstants();
 
 		if (ctc.getSpec() != null) {
 			evalSpec(ctc.getSpec());
 		} else {
 			evalInit(ctc.getInit());
 			evalNext(ctc.getNext());
 		}
 
 		if (moduleNode.getVariableDecls().length > 0) {
 			if (inits.size() == 0) {
 				throw new ConfigFileErrorException(
 						"No initialisation predicate in the configuration file");
 			}
 		}
 
 		overrides = ctc.getOverrides();
 		if (ctc.getEnumeratedSet().size() > 0)
 			setEnumeration = ctc.getEnumeratedSet();
 		bConstants = ctc.getbConstants();
 		if (ctc.getInvariants().size() > 0) {
 			invariants = ctc.getInvariants();
 
 			for (int i = 0; i < invariants.size(); i++) {
 				OpDefNode def = definitions.get(invariants.get(i));
 				def.setToolObject(PRINT_DEFINITION, true);
 			}
 		}
 
 		if (ctc.getConObjs().size() > 0)
 			conObjs = ctc.getConObjs();
 
 		evalUsedDefintions();
 	}
 
 	public ModuleContext(ModuleNode moduleNode) throws FrontEndException,
 			SemanticErrorException, ConfigFileErrorException {
 		this.moduleNode = moduleNode;
 
 		this.letParamsId = 13;
 
 		globalLets = new ArrayList<LetDef>();
 		letsNames = new ArrayList<String>();
 
 		evalDefinitions();
 		evalVariables();
 		evalConstants();
 
 		// every TLA+ Constant is a B constant
 		bConstants = new ArrayList<String>();
 		for (int i = 0; i < moduleNode.getConstantDecls().length; i++) {
 			bConstants.add(moduleNode.getConstantDecls()[i].getName()
 					.toString());
 		}
 
 		/*
 		 * Conventions: if there is no config file use the 'Next' defintion as
 		 * next state relation, 'Init' as initialisation predicate
 		 */
 		if (definitions.containsKey("Spec")) {
 			evalSpec("Spec");
 		} else {
 			evalInit("Init");
 			evalNext("Next");
 		}
 
 		if (moduleNode.getVariableDecls().length > 0) {
 			if (inits.size() == 0) {
 				throw new ConfigFileErrorException(
 						"No initialisation predicate in the configuration file");
 			}
 		}
 
 		evalUsedDefintions();
 	}
 
 	private void evalInit(String defName) {
 		inits = new ArrayList<BInit>();
 		if (defName == null)
 			return;
 
 		if (definitions.containsKey(defName)) {
 			OpDefNode init = definitions.get(defName);
 			init.setToolObject(PRINT_DEFINITION, false);
 			inits.add(new BInit("", init.getBody()));
 		}
 	}
 
 	private void evalNext(String defName) throws FrontEndException {
 		bOperations = new ArrayList<BOperation>();
 		if (defName == null)
 			return;
 
 		if (definitions.containsKey(defName)) {
 			OpDefNode next = definitions.get(defName);
 			next.setToolObject(PRINT_DEFINITION, false);
 			findOperations(next.getBody(), defName);
 		}
 	}
 
 	private void evalSpec(String spec) throws SemanticErrorException,
 			FrontEndException {
 		inits = new ArrayList<BInit>();
 		bOperations = new ArrayList<BOperation>();
 		OpDefNode def = definitions.get(spec);
 		ExprNode e = def.getBody();
 		processConfigSpec(e, "");
 	}
 
 	private void processConfigSpec(ExprNode exprNode, String prefix)
 			throws SemanticErrorException, FrontEndException {
 		
 		if(exprNode instanceof SubstInNode){
 			SubstInNode substInNode = (SubstInNode) exprNode;
 			processConfigSpec(substInNode.getBody(), prefix);
 			return;
 		}
 		
 		if (exprNode instanceof OpApplNode) {
 			OpApplNode opApp = (OpApplNode) exprNode;
 			ExprOrOpArgNode[] args = opApp.getArgs();
 			if (args.length == 0) {
 				SymbolNode opNode = opApp.getOperator();
 				if (opNode instanceof OpDefNode) {
 					OpDefNode def = (OpDefNode) opNode;
 					ExprNode body = def.getBody();
 					if (body.getLevel() == 1) {
 						inits.add(new BInit(prefix, exprNode));
 					} else {
 						String defName = def.getName().toString();
 						String newPrefix = prefix +defName.substring(0, defName.lastIndexOf('!') + 1);
 						processConfigSpec(body, newPrefix);
 					}
 					return;
 				}
 				throw new SemanticErrorException(
 						"Can not handle specification conjunction.");
 			}
 
 			int opcode = BuiltInOPs.getOpCode(opApp.getOperator().getName());
 			if (opcode == OPCODE_cl || opcode == OPCODE_land) {
 				for (int i = 0; i < args.length; i++) {
 					this.processConfigSpec((ExprNode) args[i],prefix);
 				}
 				return;
 			}
 
 			if (opcode == OPCODE_box) {
 				SemanticNode boxArg = args[0];
 				if ((boxArg instanceof OpApplNode)
 						&& BuiltInOPs.getOpCode(((OpApplNode) boxArg)
 								.getOperator().getName()) == OPCODE_sa) {
 					ExprNode next = (ExprNode) ((OpApplNode) boxArg).getArgs()[0];
 					this.next = next;
 					findOperations(next, prefix+"Next");
 					return;
 				}
 			}
 		}
 
 		if (exprNode.getLevel() <= 1) {
 			// init
 			inits.add(new BInit(prefix, exprNode));
 		} else if (exprNode.getLevel() == 3) {
 			// temporal
 
 		} else {
 			throw new SemanticErrorException(
 					"Can not handle specification conjunction.");
 		}
 
 	}
 
 	private void evalDefinitions() {
 		OpDefNode[] opDefs = moduleNode.getOpDefs();
 
 		class OpDefNodeComparator implements Comparator<OpDefNode> {
 			public int compare(OpDefNode a, OpDefNode b) {
 				if (a.getLocation().equals(b.getLocation())) {
 					if (a.getSource().getUid() < b.getSource().getUid())
 						return -1;
 					if (a.getSource().getUid() > b.getSource().getUid())
 						return 1;
 					return 0;
 				}
 				if (a.getUid() < b.getUid())
 					return -1;
 				if (a.getUid() > b.getUid())
 					return 1;
 				return 0;
 			}
 		}
 		Arrays.sort(opDefs, new OpDefNodeComparator());
 
 		definitions = new Hashtable<String, OpDefNode>();
 		for (int i = 0; i < opDefs.length; i++) {
 			OpDefNode def = opDefs[i];
 			// Definition in this module
 			if (StandardModules.contains(def.getOriginallyDefinedInModuleNode()
 					.getName().toString())
 					|| StandardModules.contains(def.getSource()
 							.getOriginallyDefinedInModuleNode().getName()
 							.toString())) {
 				continue;
 			}
 			definitions.put(def.getName().toString(), def);
 		}
 	}
 
 	class OpDeclNodeComparator implements Comparator<OpDeclNode> {
 		public int compare(OpDeclNode a, OpDeclNode b) {
 			if (a.getUid() < b.getUid())
 				return -1;
 			if (a.getUid() > b.getUid())
 				return 1;
 			return 0;
 		}
 	}
 
 	private void evalConstants() {
 		OpDeclNode[] cons = moduleNode.getConstantDecls();
 		Arrays.sort(cons, new OpDeclNodeComparator());
 	}
 
 	private void evalVariables() {
 		OpDeclNode[] vars = moduleNode.getVariableDecls();
 		Arrays.sort(vars, new OpDeclNodeComparator());
 	}
 
 	public ModuleNode getRoot() {
 		return moduleNode;
 	}
 
 	public ArrayList<String> getInvariants() {
 		return invariants;
 	}
 
 	public boolean containsDef(String defName) {
 		return definitions.containsKey(defName);
 	}
 
 	public OpDefNode getDef(String defName) {
 		return definitions.get(defName);
 	}
 
 	public void removeBCon(String conName) {
 		bConstants.remove(conName);
 	}
 
 	public ArrayList<String> getBConstants() {
 		ArrayList<String> clone = new ArrayList<String>(bConstants);
 		return clone;
 	}
 
 	public void setOverrides(Hashtable<String, String> overrides) {
 		this.overrides = overrides;
 	}
 
 	public Hashtable<String, String> getOverrides() {
 		return this.overrides;
 	}
 
 	public void addEnum(String modelValue) {
 		setEnumeration.add(modelValue);
 	}
 
 	public boolean containsEnum(String modelValue) {
 		return setEnumeration.contains(modelValue);
 	}
 
 	public ArrayList<String> getEnum() {
 		return setEnumeration;
 	}
 
 	private void findOperations(ExprNode e, String opName)
 			throws FrontEndException {
 		String name = opName;
 		ArrayList<OpApplNode> existQuans = new ArrayList<OpApplNode>();
 		findOperationsInSemanticNode(e, name, existQuans,
 				new LinkedHashMap<String, LetDef>());
 	}
 
 	private void findOperationsInSemanticNode(SemanticNode node, String name,
 			ArrayList<OpApplNode> existQuans,
 			LinkedHashMap<String, LetDef> localDefintions)
 			throws FrontEndException {
 		switch (node.getKind()) {
 		case OpApplKind: {
 			findOperationsInOpApplNode((OpApplNode) node, name, existQuans,
 					localDefintions);
 			return;
 		}
 		case LetInKind: {
 			// LetInNode letNode = (LetInNode) node;
 			// BOperation op = new BOperation(name, letNode, existQuans);
 			// bOperations.add(op);
 
 			LetInNode l = (LetInNode) node;
 			LinkedHashMap<String, LetDef> letsList = new LinkedHashMap<String, LetDef>();
 			for (int i = 0; i < l.getLets().length; i++) {
 				OpDefNode d = l.getLets()[i];
 
 				d.setToolObject(letParamsId, evalQuantorParams(existQuans));
 
 				String letName = createName(d.getName().toString());
 				LetDef letDef = new LetDef(letName, d,
 						evalQuantorParams(existQuans),
 						new LinkedHashMap<String, LetDef>(letsList));
 				globalLets.add(letDef);
 				letsList.put(d.getName().toString(), letDef);
 
 				// visit lets to determine used definitions
 				ArrayList<String> ps = new ArrayList<String>();
 				ps.addAll(evalQuantorParams(existQuans));
 				ps.addAll(getParams(d));
 				String prefix = name.substring(0, name.lastIndexOf('!') + 1);
 				visitExprNode(d.getBody(), prefix, ps);
 			}
 
 			letsList.putAll(localDefintions);
 			findOperationsInSemanticNode(l.getBody(), name, existQuans,
 					letsList);
 			return;
 		}
 		case SubstInKind: {
 			SubstInNode substInNode = (SubstInNode) node;
 
 			BOperation op = new BOperation(name, substInNode, existQuans,
 					localDefintions);
 			bOperations.add(op);
 			// findOperations(substInNode.getBody(), name);
 			return;
 		}
 		case LabelKind:
 		default:
 			throw new RuntimeException("not implemented");
 		}
 
 	}
 
 	private ArrayList<String> evalQuantorParams(ArrayList<OpApplNode> existQuans) {
 		ArrayList<String> res = new ArrayList<String>();
 		for (int i = 0; i < existQuans.size(); i++) {
 			OpApplNode n = existQuans.get(i);
 			FormalParamNode[][] params = n.getBdedQuantSymbolLists();
 			for (int k = 0; k < params.length; k++) {
 				for (int j = 0; j < params[k].length; j++) {
 					res.add(params[k][j].getName().toString());
 				}
 			}
 		}
 		return res;
 	}
 
 	private void findOperationsInOpApplNode(OpApplNode n, String name,
 			ArrayList<OpApplNode> existQuans,
 			LinkedHashMap<String, LetDef> localDefintions)
 			throws FrontEndException {
 		int kind = n.getOperator().getKind();
 		if (kind == UserDefinedOpKind
 				&& !BBuiltInOPs.contains(n.getOperator().getName())) {
 			String defName = n.getOperator().getName().toString();
 			String prefix = name.substring(0, name.lastIndexOf('!') + 1);
 			OpDefNode def = definitions.get(prefix + defName);
 			if (def.getParams().length > 0) {
 				def.setToolObject(PRINT_DEFINITION, true);
 				BOperation op = new BOperation(def.getName().toString(), n,
 						existQuans, localDefintions);
 				bOperations.add(op);
 				return;
 			} else {
 				def.setToolObject(PRINT_DEFINITION, false);
 				findOperationsInSemanticNode(def.getBody(), def.getName()
 						.toString(), existQuans,
 						new LinkedHashMap<String, LetDef>());
 				return;
 			}
 
 		} else if (kind == BuiltInKind) {
 			int opcode = BuiltInOPs.getOpCode(n.getOperator().getName());
 			switch (opcode) {
 			case OPCODE_dl: // DisjList
 			{
 				if (n.getArgs().length == 1) {
 					findOperationsInSemanticNode(n.getArgs()[0], name,
 							existQuans, localDefintions);
 					return;
 				}
 				for (int i = 0; i < n.getArgs().length; i++) {
 					findOperationsInSemanticNode(n.getArgs()[i],
 							name + (i + 1), existQuans, localDefintions);
 				}
 				return;
 			}
 			case OPCODE_lor: {
 				for (int i = 0; i < n.getArgs().length; i++) {
 					findOperationsInSemanticNode(n.getArgs()[i],
 							name + (i + 1), existQuans, localDefintions);
 				}
 				return;
 			}
 			case OPCODE_be: { // BoundedExists
 				ArrayList<OpApplNode> clone = new ArrayList<OpApplNode>(
 						existQuans);
 				clone.add(n);
 				findOperationsInSemanticNode(n.getArgs()[0], name, clone,
 						localDefintions);
 				return;
 			}
 
 			case OPCODE_unchanged:
 			case OPCODE_eq: // =
 			case OPCODE_noteq: // /=, #
 			case OPCODE_neg: // Negation
 			case OPCODE_lnot: // Negation
 			case OPCODE_cl: // $ConjList
 			case OPCODE_land: // \land
 			case OPCODE_equiv: // \equiv
 			case OPCODE_implies: // =>
 			case OPCODE_bf: // \A x \in S : P
 			case OPCODE_in: // \in
 			case OPCODE_notin: // \notin
 			case OPCODE_subseteq: // \subseteq - subset or equal
 			case OPCODE_fa: // $FcnApply f[1]
 			case OPCODE_ite: // IF THEN ELSE
 			case OPCODE_case: {
 				BOperation op = new BOperation(name, n, existQuans,
 						localDefintions);
 				bOperations.add(op);
 				return;
 			}
 			}
 		}
 		throw new FrontEndException(String.format(
 				"Expected an action at '%s' :\n%s", n.getOperator().getName()
 						.toString(), n.getLocation().toString()));
 
 	}
 
 	private ArrayList<String> getParams(OpDefNode def) {
 		ArrayList<String> res = new ArrayList<String>();
 		for (int i = 0; i < def.getParams().length; i++) {
 			res.add(def.getParams()[i].getName().toString());
 		}
 		return res;
 	}
 
 	/**
 	 * 
 	 */
 	private void evalUsedDefintions() {
 		AssumeNode[] assumes = moduleNode.getAssumptions();
 		for (int i = 0; i < assumes.length; i++) {
 			visitExprNode(assumes[i].getAssume(), "", new ArrayList<String>());
 		}
 
 		for (int i = 0; i < inits.size(); i++) {
 			BInit bInit = inits.get(i);
 			visitExprNode(bInit.getNode(), bInit.getPrefix(), new ArrayList<String>());
 		}
 
 		for (int i = 0; i < bOperations.size(); i++) {
 			BOperation op = bOperations.get(i);
 			String name = op.getName();
 			String prefix = name.substring(0, name.lastIndexOf('!') + 1);
 			ArrayList<String> ps = new ArrayList<String>();
 			for (int j = 0; j < op.getExistQuans().size(); j++) {
 				OpApplNode node = op.getExistQuans().get(j);
 				ExprNode[] in = node.getBdedQuantBounds();
 				for (int k = 0; k < in.length; k++) {
 					visitExprNode(in[k], prefix, new ArrayList<String>());
 				}
 				ps.addAll(op.getOpParams());
 			}
 			visitExprNode(bOperations.get(i).getNode(), prefix, ps);
 		}
 
 		for (int i = 0; i < invariants.size(); i++) {
 			String invName = invariants.get(i);
 			OpDefNode def = definitions.get(invName);
 			def.setToolObject(PRINT_DEFINITION, true);
 			String newPrefix = invName.substring(0,
 					invName.lastIndexOf('!') + 1);
 			visitExprNode(def.getBody(), newPrefix, new ArrayList<String>());
 		}
 
 		if (overrides != null) {
 			for (Iterator<String> i = overrides.values().iterator(); i
 					.hasNext();) {
 				String defName = i.next();
 				OpDefNode def = definitions.get(defName);
 				def.setToolObject(PRINT_DEFINITION, true);
 				String newPrefix = defName.substring(0,
 						defName.lastIndexOf('!') + 1);
 				visitExprNode(def.getBody(), newPrefix, new ArrayList<String>());
 			}
 		}
 
 	}
 
 	private void visitExprNode(ExprNode node, String prefix,
 			ArrayList<String> parameters) {
 		switch (node.getKind()) {
 		case OpApplKind: {
 			visitOpApplNode((OpApplNode) node, prefix, new ArrayList<String>(
 					parameters));
 			return;
 		}
 		case LetInKind: {
 			LetInNode l = (LetInNode) node;
 			for (int i = 0; i < l.getLets().length; i++) {
 				OpDefNode d = l.getLets()[i];
 				d.setToolObject(letParamsId, new ArrayList<String>(parameters));
 
 				// visit lets to determine used definitions
 				ArrayList<String> ps = new ArrayList<String>();
 				ps.addAll(parameters);
 				ps.addAll(getParams(d));
 				visitExprNode(d.getBody(), prefix, ps);
 			}
 			visitExprNode(l.getBody(), prefix,
 					new ArrayList<String>(parameters));
 			return;
 		}
 		case SubstInKind: {
 			SubstInNode substInNode = (SubstInNode) node;
 			Subst[] substs = substInNode.getSubsts();
 			for (int i = 0; i < substs.length; i++) {
 				visitExprOrOpArgNode(substs[i].getExpr(), prefix, new ArrayList<String>(parameters));
 			}
 			visitExprNode(substInNode.getBody(), prefix, new ArrayList<String>(
 					parameters));
 			return;
 		}
 		}
 	}
 
 	/**
 	 * @param node
 	 */
 	private void visitOpApplNode(OpApplNode node, String prefix,
 			ArrayList<String> parameters) {
 		switch (node.getOperator().getKind()) {
 
 		case BuiltInKind: {
 			visitBuiltInKind(node, prefix, new ArrayList<String>(parameters));
 			return;
 		}
 
 		case UserDefinedOpKind: {
 			if (BBuiltInOPs.contains(node.getOperator().getName())) {
 				visitBuiltInKind(node, prefix,
 						new ArrayList<String>(parameters));
 				return;
 			}
 
 			String defName;
			String name = node.getOperator().getName().toString();
 			if (node.getOperator().getName().toString().toString()
 					.startsWith(prefix)) {
 				defName = node.getOperator().getName().toString();
 			} else {
 				defName = prefix + node.getOperator().getName().toString();
 			}
 
 			String newPrefix = defName.substring(0,
 					defName.lastIndexOf('!') + 1);
 
 			// let
 			if (!this.definitions.containsKey(defName)) {
				if(this.definitions.containsKey(name)){
					defName = name;
				}else{
					OpDefNode def = (OpDefNode) node.getOperator();
					visitExprNode(def.getBody(), newPrefix, new ArrayList<String>(
							parameters));
					return;
				}
 			}
 
 			OpDefNode def = definitions.get(defName);
 			def.setToolObject(PRINT_DEFINITION, true);
 			visitExprNode(def.getBody(), newPrefix, getParams(def));
 			return;
 		}
 
 		}
 
 	}
 
 	protected String createName(String name) {
 		String res = name;
 		for (int i = 1; letsNames.contains(res); i++) {
 			res = name + i;
 		}
 		letsNames.add(res);
 		return res;
 	}
 
 	/**
 	 * @param node
 	 */
 	private void visitBuiltInKind(OpApplNode node, String prefix,
 			ArrayList<String> parameters) {
 		switch (BuiltInOPs.getOpCode(node.getOperator().getName())) {
 
 		case OPCODE_be:
 		case OPCODE_bf:
 		case OPCODE_soa:
 		case OPCODE_sso:
 		case OPCODE_nrfs:
 		case OPCODE_fc: {
 			ExprNode[] in = node.getBdedQuantBounds();
 			for (int i = 0; i < in.length; i++) {
 				visitExprNode(in[i], prefix, new ArrayList<String>(parameters));
 			}
 			ArrayList<String> ps = new ArrayList<String>();
 			ps.addAll(parameters);
 			ArrayList<OpApplNode> existQuans = new ArrayList<OpApplNode>();
 			existQuans.add(node);
 			ps.addAll(evalQuantorParams(existQuans));
 			visitExprOrOpArgNode(node.getArgs()[0], prefix, ps);
 			return;
 		}
 
 		case OPCODE_unchanged: {
 			return;
 		}
 
 		default: {
 			for (int i = 0; i < node.getArgs().length; i++) {
 				visitExprOrOpArgNode(node.getArgs()[i], prefix,
 						new ArrayList<String>(parameters));
 			}
 			return;
 		}
 		}
 	}
 
 	/**
 	 * @param exprOrOpArgNode
 	 */
 	private void visitExprOrOpArgNode(ExprOrOpArgNode n, String prefix,
 			ArrayList<String> parameters) {
 		if (n instanceof ExprNode) {
 			visitExprNode((ExprNode) n, prefix, new ArrayList<String>(
 					parameters));
 		} else if (n instanceof OpArgNode){
 			OpArgNode opArgNode = (OpArgNode) n;
 			
 			String defName;
 			if (opArgNode.getName().toString().toString()
 					.startsWith(prefix)) {
 				defName = opArgNode.getName().toString();
 			} else {
 				defName = prefix + opArgNode.getName().toString();
 			}
 
 			String newPrefix = defName.substring(0,
 					defName.lastIndexOf('!') + 1);
 
 			// let
 			if (!this.definitions.containsKey(defName)) {
 				OpDefNode def = (OpDefNode) opArgNode.getOp();
 				visitExprNode(def.getBody(), newPrefix, new ArrayList<String>(
 						parameters));
 				return;
 			}
 
 			OpDefNode def = definitions.get(defName);
 			def.setToolObject(PRINT_DEFINITION, true);
 			visitExprNode(def.getBody(), newPrefix, getParams(def));
 			return;
 		}
 	}
 
 }
