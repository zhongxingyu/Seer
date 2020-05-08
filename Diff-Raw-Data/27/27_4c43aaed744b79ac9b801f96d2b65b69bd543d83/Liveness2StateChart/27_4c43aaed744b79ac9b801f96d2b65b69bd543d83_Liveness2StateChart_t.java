 /**
  * 
  */
 package statechart.diagram.part;
 
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import statechart.Model;
 import statechart.Node;
 import statechart.StatechartFactory;
 import statechart.Transition;
 
 /**
  * @author angelica
  *
  */
 public class Liveness2StateChart {
 	/**
 	 * @generated NOT
 	 */
 	String liveness = null;
 	/**
 	 * @generated NOT
 	 */
 	Hashtable<String, String> formulas = new Hashtable<String, String>();
 	/**
 	 * @generated NOT
 	 */
 	Model model = null;
 	/**
 	 * @generated NOT
 	 */
 	Node root = null;
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_OR = "OR";
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_AND = "AND";
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_START = "START";
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_END = "END";
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_CONDITION = "CONDITION";
 	/**
 	 * @generated NOT
 	 */
 	public static final String TYPE_BASIC = "BASIC";
 	/**
 	 * @generated NOT
 	 */
 	public String stName;
 	/**
 	 * @generated NOT
 	 */
 	
 	
 	public Liveness2StateChart(){
 		this.liveness = new String();
 		this.stName = new String();
 	}
 	/**
 	 * @generated NOT
 	 */
 	public Liveness2StateChart(String liveness, String stName){
 		this.liveness = liveness;
 		this.stName = stName;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	/**
 	 * @generated NOT
 	 */
 	public Model formula(String formula, String Name){
 		try{
 			model = StatechartFactory.eINSTANCE.createModel();
 			model.setName(Name);
 			this.liveness = new String(formula);
 			liveness = liveness.replaceAll(" ", "");
 			transform();
 						
 		}catch(Exception e){
 			
 		}
 		
 		return model;
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	public Model transform() {
 		StringTokenizer line = new StringTokenizer(this.liveness, "\n");
 		while (line.hasMoreTokens()) {
 			String tmp = line.nextToken();
 			StringTokenizer formulaElements = new StringTokenizer(tmp, "=");
 			String leftHandSide = formulaElements.nextToken();
 			String formula = formulaElements.nextToken();
 			formulas.put(leftHandSide, formula);
 		}
 		line = new StringTokenizer(liveness, "\n");
 		StringTokenizer formulaElements = new StringTokenizer(line.nextToken(),
 				"=");
 		String leftHandSide = formulaElements.nextToken();
 		line = null;
 		formulaElements = null;
 
 		
 		
 		model.setName(stName);    ///////////////////////////////////////////////////////////
 
 		root = StatechartFactory.eINSTANCE.createNode();
 		root.setLabel("0");
 		root.setName(leftHandSide);
 		root.setType("OR");
 		model.getNodes().add(root);
 
 		// call the createStatechart recursive process
 		this.createStatechart(formulas.get(leftHandSide), root);
 		return model;
 
 	}
 	
 	/**
 	 * @generated NOT
 	 */
 	public void createStatechart(String expression, Node father) {
 		// this integer will be used for selecting the connector for getting the
 		// terms after the if section
 		int expressionType = 0;
 		// pattern for parallelExpression : expressionType=1
 		Pattern patternParallelExpression = Pattern
 				.compile("(((\\(.+\\))|([\\w&&[^()]])+)([~+*]?)\\|\\|)+((\\(.+\\))|([\\w&&[^()]])+)([~+*]?)");
 		// .compile("(((\\(.+\\))|([\\w&&[^()]])+)(~?)\\|\\|)+((\\(.+\\))|([\\w&&[^()]])+)(~?)");
 		// original
 		Matcher parallelMatcher = patternParallelExpression.matcher(expression);
 		// pattern for orExpression : expressionType=2
 		Pattern patternOrExpression = Pattern
 				.compile("(((\\[.+\\])|(\\(.+\\))|([\\w&&[^()]])+)([~+*]?)\\|)+((\\[.+\\])|(\\(.+\\))|([\\w&&[^()]])+)([~+*]?)");
 		Matcher orMatcher = patternOrExpression.matcher(expression);
 		// pattern for sequentialExpression : expressionType=3
 		Pattern patternSequentialExpression = Pattern
 				.compile("(((\\[.+\\])|(\\(.+\\))|([\\w&&[^()]])+)([~+*]?)\\.)+((\\[.+\\])|(\\(.+\\))|([\\w&&[^()]])+)([~+*]?)");
 		// .compile("(((\\(.+\\))|([\\w&&[^()]])+)(~?)\\.)+((\\(.+\\))|([\\w&&[^()]])+)(~?)");
 		// original
 		Matcher sequentialMatcher = patternSequentialExpression
 				.matcher(expression);
 		Node tmpNode;
 		Transition tmpTransition;
 		if (sequentialMatcher.find()
 				&& (sequentialMatcher.group().length() == expression.length())) {
 			expressionType = 3;
 			//System.out.print("a sequential expression processed: " + expression+ "\n");
 			father.setType(TYPE_OR);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".1");
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_START);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //		
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			int k = 2;
 			for (Iterator<String> iterator = this.findTermsInExpression(
					expression, "\\.").iterator(); iterator.hasNext();) {
 				String term = iterator.next();
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + "." + k);
 				tmpNode.setName(this.computeNodeName(term));
 				tmpNode.setFather_of(father);
 //				
 //				model.getChildren().add(tmpNode);
 				father.getChildren().add(tmpNode);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(father
 						.getLabel()
 						+ "." + (k - 1))));
 				tmpTransition.setTarget(getNodeByLabel(new String(father
 						.getLabel()
 						+ "." + k)));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				k = k + 1;
 			}
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + "." + k);
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_END);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 			tmpTransition.setSource(getNodeByLabel(new String(father.getLabel()
 					+ "." + (k - 1))));
 			tmpTransition.setTarget(getNodeByLabel(new String(father.getLabel()
 					+ "." + k)));
 			tmpTransition.setName(tmpTransition.getSource().getName() + "TO"
 					+ tmpTransition.getTarget().getName());
 			model.getTransitions().add(tmpTransition);
 		} else if (orMatcher.find()
 				&& (orMatcher.group().length() == expression.length())) {
 			expressionType = 2;
 			//System.out.print("an or expression processed: " + expression + "\n");
 			father.setType(TYPE_OR);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".1");
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_START);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".2");
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_CONDITION);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 			tmpTransition.setSource(getNodeByLabel(new String(father.getLabel()
 					+ ".1")));
 			tmpTransition.setTarget(getNodeByLabel(new String(father.getLabel()
 					+ ".2")));
 			tmpTransition.setName(tmpTransition.getSource().getName() + "TO"
 					+ tmpTransition.getTarget().getName());
 			model.getTransitions().add(tmpTransition);
 			int k = 3;
 			for (Iterator<String> iterator = this.findTermsInExpression(
					expression, "\\|").iterator(); iterator.hasNext();) {
 				// the above line creates a bag if there are parallel operators
 				// in the or expression
 				String term = iterator.next();
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + "." + k);
 				tmpNode.setName(this.computeNodeName(term));
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(father);
 //				
 //				model.getChildren().add(tmpNode);
 				father.getChildren().add(tmpNode);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(father
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(father
 						.getLabel()
 						+ "." + k)));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				k = k + 1;
 			}
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + "." + k);
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_END);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //		
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			int tmpCounter = k;
 			for (k = tmpCounter - 1; k > 2; k--) {
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(father
 						.getLabel()
 						+ "." + k)));
 				tmpTransition.setTarget(getNodeByLabel(new String(father
 						.getLabel()
 						+ "." + tmpCounter)));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 			}
 		} else if (parallelMatcher.find()
 				&& (parallelMatcher.group().length() == expression.length())) {
 			expressionType = 1;
 			//System.out.print("a parallel expression processed: " + expression	+ "\n");
 			father.setType(TYPE_OR);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".1");
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_START);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".2");
 			// if not for the following line all AND states are not transformed
 			// to JADE behaviours
 			tmpNode.setName(this.computeNodeName(expression));
 			tmpNode.setType(TYPE_AND);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			
 			tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 			tmpTransition.setSource(getNodeByLabel(new String(father.getLabel()
 					+ ".1")));
 			tmpTransition.setTarget(getNodeByLabel(new String(father.getLabel()
 					+ ".2")));
 			tmpTransition.setName(tmpTransition.getSource().getName() + "TO"
 					+ tmpTransition.getTarget().getName());
 			model.getTransitions().add(tmpTransition);
 			tmpNode = StatechartFactory.eINSTANCE.createNode();
 			tmpNode.setLabel(father.getLabel() + ".3");
 			tmpNode.setName(tmpNode.getLabel());
 			tmpNode.setType(TYPE_END);
 //			tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 			tmpNode.setFather_of(father);
 //			
 //			model.getChildren().add(tmpNode);
 			father.getChildren().add(tmpNode);
 			tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 			tmpTransition.setSource(getNodeByLabel(new String(father.getLabel()
 					+ ".2")));
 			tmpTransition.setTarget(getNodeByLabel(new String(father.getLabel()
 					+ ".3")));
 			tmpTransition.setName(tmpTransition.getSource().getName() + "TO"
 					+ tmpTransition.getTarget().getName());
 			model.getTransitions().add(tmpTransition);
 			int k = 1;
 			for (Iterator<String> iterator = this.findTermsInExpression(
					expression, "\\|\\|").iterator(); iterator.hasNext();) {
 				String term = iterator.next();
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + ".2." + k);
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_OR);
 				Node tmp2node = getNodeByLabel(new String(father.getLabel() + ".2"));
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(tmp2node	);
 //				
 //				model.getChildren().add(tmpNode);
 				tmp2node.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + ".2." + k + ".1");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_START);
 				tmp2node = getNodeByLabel(new String(father.getLabel()	+ ".2." + k));
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(tmp2node);
 //				
 //				model.getChildren().add(tmpNode);
 				tmp2node.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + ".2." + k + ".2");
 				tmpNode.setName(this.computeNodeName(term));
 				tmp2node = getNodeByLabel(new String(father.getLabel()+ ".2." + k));
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(tmp2node	);
 //				
 //				model.getChildren().add(tmpNode);
 				tmp2node.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(father.getLabel() + ".2." + k + ".3");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_END);
 				tmp2node = getNodeByLabel(new String(father.getLabel()	+ ".2." + k));
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(tmp2node);
 //				
 //				model.getChildren().add(tmpNode);
 				tmp2node.getChildren().add(tmpNode);
 				
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(father
 						.getLabel()
 						+ ".2." + k + ".1")));
 				tmpTransition.setTarget(getNodeByLabel(new String(father
 						.getLabel()
 						+ ".2." + k + ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(father
 						.getLabel()
 						+ ".2." + k + ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(father
 						.getLabel()
 						+ ".2." + k + ".3")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				k = k + 1;
 			}
 		}
 		List<String> myTerms = null;
 		switch (expressionType) {
 		case 0:
 			myTerms = new LinkedList<String>();
 			myTerms.add(expression);
 			break;
 		case 1:
			myTerms = this.findTermsInExpression(expression, "\\|\\|");
 			break;
 		case 2:
			myTerms = this.findTermsInExpression(expression, "\\|");
 			break;
 		case 3:
			myTerms = this.findTermsInExpression(expression, "\\.");
 			break;
 		}
 		for (Iterator<String> iterator = myTerms.iterator(); iterator.hasNext();) {
 			String term = iterator.next();
 			// pattern for basicTerm
 			Pattern patternBasicTerm = Pattern.compile("^\\w+$");
 			Matcher basicTermMatcher = patternBasicTerm.matcher(term);
 			// pattern for (term)
 			Pattern patternComplexParenthesisTerm = Pattern
 					.compile("^\\(.+\\)$");
 			Matcher complexParenthesisTermMatcher = patternComplexParenthesisTerm
 					.matcher(term);
 			// pattern for [term]
 			Pattern patternComplexOptionalTerm = Pattern.compile("^\\[.+\\]$");
 			Matcher complexOptionalTermMatcher = patternComplexOptionalTerm
 					.matcher(term);
 			// pattern for term~
 			Pattern patternForeverTerm = Pattern.compile(".+~$");
 			Matcher foreverTermMatcher = patternForeverTerm.matcher(term);
 			// pattern for term+
 			Pattern patternOneOrMoreTimesTerm = Pattern.compile(".+\\+$");
 			Matcher oneOrMoreTimesTermMatcher = patternOneOrMoreTimesTerm
 					.matcher(term);
 			// pattern for term*
 			Pattern patternZeroOrMoreTimesTerm = Pattern.compile(".+\\*$");
 			Matcher zeroOrMoreTimesTermMatcher = patternZeroOrMoreTimesTerm
 					.matcher(term);
 			// pattern for |term~|n
 			Pattern patternParallelManyTimesTerm = Pattern
 					.compile("^\\|.+(~\\|(\\d)+)$");
 			Matcher parallelManyTimesTermMatcher = patternParallelManyTimesTerm
 					.matcher(term);
 			if (complexParenthesisTermMatcher.find()
 					&& (complexParenthesisTermMatcher.group().length() == term
 							.length())) {
 				this.createStatechart(term.substring(1, term.length() - 1),
 						this.getNode(father, term));
 			} else if (complexOptionalTermMatcher.find()
 					&& (complexOptionalTermMatcher.group().length() == term
 							.length())) {
 				Node tmpFather = this.getNode(father, term);
 				if (tmpFather == null) tmpFather = father;
 				tmpFather.setType(TYPE_OR);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".1");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_START);
 //				tmpNode.setFather_of(StatechartFactory.eINSTANCE.createNode());
 				tmpNode.setFather_of(tmpFather);
 //				
 //				model.getChildren().add(tmpNode);
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".2");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_CONDITION);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".3");
 				String insideTerm = term.substring(1, term.length() - 1);
 				tmpNode.setName(this.computeNodeName(insideTerm));
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				basicTermMatcher = patternBasicTerm.matcher(insideTerm);
 				if (basicTermMatcher.find()
 						&& (basicTermMatcher.group().length() == insideTerm
 								.length())) {
 					this.handleBasicTerm(insideTerm, tmpNode);
 				} else {
 					this.createStatechart(insideTerm, tmpNode);
 				}
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".4");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_END);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".1")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".4")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".4")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 			} else if (zeroOrMoreTimesTermMatcher.find()
 					&& (zeroOrMoreTimesTermMatcher.group().length() == term
 							.length())) {
 				Node tmpFather = this.getNode(father, term);
 				if (tmpFather == null) tmpFather = father;
 				tmpFather.setType(TYPE_OR);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".1");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_START);
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".2");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_CONDITION);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".3");
 				String insideTerm = term.substring(0, term.length() - 1);
 				tmpNode.setName(this.computeNodeName(insideTerm));
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				basicTermMatcher = patternBasicTerm.matcher(insideTerm);
 				if (basicTermMatcher.find()
 						&& (basicTermMatcher.group().length() == insideTerm
 								.length())) {
 					this.handleBasicTerm(insideTerm, tmpNode);
 				} else {
 					this.createStatechart(insideTerm, tmpNode);
 				}
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".4");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_END);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".1")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".4")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".4")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 			} else if (foreverTermMatcher.find()
 					&& (foreverTermMatcher.group().length() == term.length())) {
 				Node tmpFather = this.getNode(father, term);
 				if (tmpFather == null) tmpFather = father;
 				tmpFather.setType(TYPE_OR);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".1");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_START);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".2");
 				String insideTerm = term.substring(0, term.length() - 1);
 				tmpNode.setName(this.computeNodeName(insideTerm));
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				basicTermMatcher = patternBasicTerm.matcher(insideTerm);
 				if (basicTermMatcher.find()
 						&& (basicTermMatcher.group().length() == insideTerm
 								.length())) {
 					this.handleBasicTerm(insideTerm, tmpNode);
 				} else {
 					this.createStatechart(insideTerm, tmpNode);
 				}
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".1")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 			} else if (oneOrMoreTimesTermMatcher.find()
 					&& (oneOrMoreTimesTermMatcher.group().length() == term
 							.length())) {
 				Node tmpFather = this.getNode(father, term);
 				if (tmpFather == null) tmpFather = father;
 				tmpFather.setType(TYPE_OR);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".1");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_START);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".2");
 				String insideTerm = term.substring(0, term.length() - 1);
 				tmpNode.setName(this.computeNodeName(insideTerm));
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				basicTermMatcher = patternBasicTerm.matcher(insideTerm);
 				if (basicTermMatcher.find()
 						&& (basicTermMatcher.group().length() == insideTerm
 								.length())) {
 					this.handleBasicTerm(insideTerm, tmpNode);
 				} else {
 					this.createStatechart(insideTerm, tmpNode);
 				}
 				tmpNode = StatechartFactory.eINSTANCE.createNode();
 				tmpNode.setLabel(tmpFather.getLabel() + ".3");
 				tmpNode.setName(tmpNode.getLabel());
 				tmpNode.setType(TYPE_END);
 
 				tmpNode.setFather_of(tmpFather);
 
 				tmpFather.getChildren().add(tmpNode);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".1")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 				tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 				tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".2")));
 				tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 						.getLabel()
 						+ ".3")));
 				tmpTransition.setName(tmpTransition.getSource().getName()
 						+ "TO" + tmpTransition.getTarget().getName());
 				model.getTransitions().add(tmpTransition);
 
 			} else if (parallelManyTimesTermMatcher.find()
 					&& (parallelManyTimesTermMatcher.group().length() == term
 							.length())) {
 				Node tmpFather = this.getNode(father, term);
 				if (tmpFather == null) tmpFather = father;
 				tmpFather.setType(TYPE_AND);
 				for (int k = 1; k <= Integer.parseInt(term.substring(term
 						.lastIndexOf("|") + 1, term.length())); k++) {
 					tmpNode = StatechartFactory.eINSTANCE.createNode();
 					tmpNode.setLabel(tmpFather.getLabel() + "." + k);
 					tmpNode.setName(tmpNode.getLabel());
 					tmpNode.setType(TYPE_OR);
 
 					tmpNode.setFather_of(tmpFather);
 
 					tmpFather.getChildren().add(tmpNode);
 					tmpNode = StatechartFactory.eINSTANCE.createNode();
 					tmpNode.setLabel(tmpFather.getLabel() + "." + k + ".1");
 					tmpNode.setName(tmpNode.getLabel());
 					tmpNode.setType(TYPE_START);
 					Node tmp2node = getNodeByLabel(tmpFather.getLabel() + "." + k);
 
 					tmpNode.setFather_of(tmp2node);
 
 					tmp2node.getChildren().add(tmpNode);
 					tmpNode = StatechartFactory.eINSTANCE.createNode();
 					tmpNode.setLabel(tmpFather.getLabel() + "." + k + ".2");
 					String insideTerm = term.substring(term.indexOf("|") + 1,
 							term.lastIndexOf("~"));
 					tmpNode.setName(this.computeNodeName(insideTerm));
 					tmp2node = getNodeByLabel(tmpFather.getLabel() + "." + k);
 
 					tmpNode.setFather_of(tmp2node);
 ;
 					tmp2node.getChildren().add(tmpNode);
 					basicTermMatcher = patternBasicTerm.matcher(insideTerm);
 					if (basicTermMatcher.find()
 							&& (basicTermMatcher.group().length() == insideTerm
 									.length())) {
 						this.handleBasicTerm(insideTerm, tmpNode);
 						this.handleBasicTerm(insideTerm,
 								getNodeByLabel(getNode(father, term).getLabel()
 										+ "." + k + ".2"));
 					} else {
 						this.createStatechart(insideTerm,
 								getNodeByLabel(getNode(father, term).getLabel()
 										+ "." + k + ".2"));
 					}
 					tmpNode = StatechartFactory.eINSTANCE.createNode();
 					tmpNode.setLabel(tmpFather.getLabel() + "." + k + ".3");
 					tmpNode.setName(tmpNode.getLabel());
 					tmpNode.setType(TYPE_END);
 					tmp2node = getNodeByLabel(tmpFather.getLabel() + "." + k);
 
 					tmpNode.setFather_of(tmp2node);
 
 					tmp2node.getChildren().add(tmpNode);
 					tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 					tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 							.getLabel()
 							+ "." + k + ".1")));
 					tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 							.getLabel()
 							+ "." + k + ".2")));
 					tmpTransition.setName(tmpTransition.getSource().getName()
 							+ "TO" + tmpTransition.getTarget().getName());
 					model.getTransitions().add(tmpTransition);
 					tmpTransition = StatechartFactory.eINSTANCE.createTransition();
 					tmpTransition.setSource(getNodeByLabel(new String(tmpFather
 							.getLabel()
 							+ "." + k + ".2")));
 					tmpTransition.setTarget(getNodeByLabel(new String(tmpFather
 							.getLabel()
 							+ "." + k + ".3")));
 					tmpTransition.setName(tmpTransition.getSource().getName()
 							+ "TO" + tmpTransition.getTarget().getName());
 					model.getTransitions().add(tmpTransition);
 				}
 			} else if (basicTermMatcher.find()
 					&& (basicTermMatcher.group().length() == term.length())) {
 				this.handleBasicTerm(term, getNode(father, term));
 			}
 
 		}
 	}
 	/**
 	 * @generated NOT
 	 */
 	public String parent(String node) {
 		return (node.substring(0, node.lastIndexOf(".")));
 	}
 	/**
 	 * @generated NOT
 	 */
 	public void handleBasicTerm(String term, Node node) {
 		boolean isBasic = true;
 		if (formulas.containsKey(term)) {
 			node.setType(TYPE_OR);
 			this.createStatechart(formulas.get(term), node);
 			isBasic = false;
 		}
 		if (isBasic) {
 			node.setType(TYPE_BASIC);
 		}
 	}
 	/**
 	 * @generated NOT
 	 */
 	public String computeNodeName(String term) {
 		String nodeName = new String(term);
 		nodeName = nodeName.replaceAll("\\|\\|", "_parallel_");
 		nodeName = nodeName.replaceAll("~", "_forever_");
 		nodeName = nodeName.replaceAll("\\.", "_sequence_");
 		nodeName = nodeName.replaceAll("\\|", "_or_");
 		nodeName = nodeName.replaceAll("\\*", "_zero_or_more_times_");
 		nodeName = nodeName.replaceAll("\\+", "_one_or_more_times_");
 		nodeName = nodeName.replaceAll("\\(", "_open_group_");
 		nodeName = nodeName.replaceAll("\\)", "_close_group_");
 		nodeName = nodeName.replaceAll("\\[", "_open_option_");
 		nodeName = nodeName.replaceAll("\\]", "_close_option_");
 		return nodeName;
 	}
 	/**
 	 * @generated NOT
 	 */
 	public Node getNodeByLabel(String label) {
 		if(root.getLabel().equals(label))
 			return root;
 		else
 			return DFSearch(root, label);
 	}
 	/**
 	 * @generated NOT
 	 */
 	public Node DFSearch(Node root, String label){
 		Iterator<Node> it = root.getChildren().iterator();
 		Node search = null;
 		while( it.hasNext()){
 			search = it.next();
 			if(search.getLabel().equals(label)){
 				return search;
 			}
 			else if(search.getChildren().size()>0){
 				Node newsearch = DFSearch(search, label);
 				if (newsearch!=null)
 					return newsearch;
 			}
 		}
 		return null;
 	}
 	/**
 	 * @generated NOT
 	 */
 	public Node getNode(Node father, String term) {
 		LinkedList<Node> queue = new LinkedList<Node>();
 		queue.addLast(father);
 		while (queue.size() > 0) {
 			Node first = queue.removeFirst();
 			if (first.getName().equalsIgnoreCase(this.computeNodeName(term))) {
 				return first;
 			} else {
 				if(root.getLabel().equals(first.getLabel()))
 					queue.addAll(root.getChildren());
 				else{
 					Node possFather = null;
 					possFather = DFSearch(root, first.getLabel());
 					if(possFather!=null && possFather.getChildren().size()>0)
 						queue.addAll(possFather.getChildren());
 				}
 			}
 		}
 		return null;
 	}
 	/**
 	 * @generated NOT
 	 */
 	public List<String> findTermsInExpression(String expression,
 			String connector) {
 		List<String> foundTerms = new LinkedList<String>();
	//	StringTokenizer t = new StringTokenizer(expression, connector);
		String[] tokens = expression.split(connector);
 		String currentTerm = new String();
		for(int j=0; j<tokens.length; j++) {
 			int parenthesis = 0;
 			int brackets = 0;
			currentTerm = currentTerm + tokens[j];
 			for (int i = 0; i < currentTerm.length(); i++) {
 				if (currentTerm.regionMatches(i, "(", 0, 1))
 					parenthesis++;
 				if (currentTerm.regionMatches(i, ")", 0, 1))
 					parenthesis--;
 				if (currentTerm.regionMatches(i, "[", 0, 1))
 					brackets++;
 				if (currentTerm.regionMatches(i, "]", 0, 1))
 					brackets--;
 			}
 			if ((parenthesis == 0) && (brackets == 0)) {
 				foundTerms.add(currentTerm);
 				//System.out.print("found term: " + currentTerm + "\n");
 				currentTerm = new String();
 			} else
 				currentTerm = currentTerm + connector;
 		}
 		return foundTerms;
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 }
