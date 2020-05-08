 /**
  * 
  */
 package org.feature.cluster.model.editor.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.zest.core.widgets.GraphConnection;
 import org.eclipse.zest.core.widgets.GraphNode;
 import org.emftext.term.propositional.expression.And;
 import org.emftext.term.propositional.expression.FeatureRef;
 import org.emftext.term.propositional.expression.Nested;
 import org.emftext.term.propositional.expression.Not;
 import org.emftext.term.propositional.expression.Or;
 import org.emftext.term.propositional.expression.Term;
 import org.featuremapper.models.feature.Constraint;
 import org.featuremapper.models.feature.Feature;
 import org.featuremapper.models.feature.FeatureModel;
 import org.featuremapper.models.feature.Group;
 import org.js.model.csp.TextExpressionParser;
 import org.js.model.csp.analyze.FeatureModelAnalyzer;
 
 /**
  * Utility class for the cluster editor.
  * @author Tim Winkelmann
  * 
  */
 public class Util {
 	public static final Color attachedGraphNodeBackgroundColor = ColorConstants.green;
 	public static final Color unattachedGraphNodeBackgroundColor = new Color(null, 216, 228, 248);
 	public static final Color attachedGraphConnectionLineColor = ColorConstants.green;
 	public static final Color unattachedGraphConnectionLineColor = new Color(null, 192, 192, 192);
 	public static List<GraphNode> attachedGraphNodes = new LinkedList<GraphNode>();
 	public static List<GraphConnection> highlightedGraphConnections = new LinkedList<GraphConnection>();
 	public static Logger log = Logger.getLogger(Util.class);
 	
 	/**
 	 * creates a view from the {@link FeatureModel}
 	 * @param featureModel the original {@link FeatureModel}
 	 * @param features which are part of the view.
 	 * @return a view from a {@link FeatureModel}
 	 */
 	public static FeatureModel createFeatureModel(FeatureModel featureModel, Set<Feature> features){
 		if (featureModel.getAllFeatures().size() == features.size()) {
 			return featureModel;
 		}
 		FeatureModel view = EcoreUtil.copy(featureModel);
 		traverseFeatureModelAndRemoveFeatures(view.getRoot(),features);
 		assert view.getAllFeatures().size() == features.size();
 		removeUnusedConstrtaints(view,featureModel);
 		return view;
 	}
 	
 	/**
 	 * removes condition where the condition is a tautology, due to missing features and simple implications and exclusions.
 	 * @param view a subset of the {@link FeatureModel}.
 	 * @param featureModel the full {@link FeatureModel}.
 	 */
 	private static void removeUnusedConstrtaints(FeatureModel view, FeatureModel featureModel) {
 		log.debug("view features:\t" + view.getAllFeatures().size());
 		log.debug("featureModel features:\t" + featureModel.getAllFeatures().size());
 		EList<Constraint> constraints = view.getConstraints();
 		List<org.emftext.term.propositional.expression.Term> orgConstraints = TextExpressionParser.parseExpressions(featureModel);
 		Set<Constraint> constraintsToRemove = new HashSet<Constraint>();
 		for (org.emftext.term.propositional.expression.Term term : orgConstraints) {
 			Set<Feature> featuresFromTerm = TextExpressionParser.getFeaturesFromTerm(term);
 			boolean allFeaturesMissing = true;
 			boolean minOneFeatureIsMissig = false;
 			for (Feature feature : featuresFromTerm) {
 				if (contains(feature, view.getAllFeatures())) {
 					allFeaturesMissing = false;
 				}else{
 					minOneFeatureIsMissig = true;
 				}
 			}
 			boolean remove = false;
 			if (minOneFeatureIsMissig) {
 				Set<Feature> implication = isImplication(term);
 				if (implication.isEmpty()) {
 					//exclusion
 					if (isExclusion(term)) {//remove constraint by missing of one feature.
 						remove = true;
 					}
 				}else{//remove constraint by missing of the right feature.
 					for (Feature feature : implication) {
 						if (contains(feature, featuresFromTerm)) {
 							remove = true;
 						}
 					}
 				}
 			}
 			if (allFeaturesMissing || remove) {//remove
 				//find constraint
 				Constraint constraint = constraints.get(orgConstraints.indexOf(term));
 				if (constraint != null) {
 					//remove constraint
 					constraintsToRemove.add(constraint);
 				}else{
 					log.warn("Could not find constraint for removal" + term);
 				}
 			}
 		}
 		view.getConstraints().removeAll(constraintsToRemove);
 		log.debug("Constraints removed: " + constraintsToRemove.size());
 	}
 
 	/**
 	 * Checks if the {@link Term} is an exclusion.
 	 * @param term the {@link Term} to be checked.
 	 * @return true if it is an exclusion.
 	 */
 	private static boolean isExclusion(Term term) {
 		boolean isExclusion = false;
 		if (term instanceof And) {
 			And and = (And) term;
 			Term operand1 = and.getOperand1();
 			Term operand2 = and.getOperand2();
 			if (operand1 instanceof Nested) {
 				Nested nested = (Nested) operand1;
 				operand1 = nested.getOperand();
 			}
 			if (operand2 instanceof Nested) {
 				Nested nested = (Nested) operand1;
 				operand2 = nested.getOperand();
 			}
 			if (operand1 instanceof Not && operand2 instanceof FeatureRef) {
 				Not not = (Not) operand1;
 				if (not.getOperand() instanceof FeatureRef) {
 					isExclusion = true;
 				}
 			}
 		}		
 		return isExclusion;
 	}
 
 	/**
 	 * Checks if the {@link Term} is an Implication.
 	 * @param term the {@link Term} to be checked.
 	 * @return the right operand {@link Feature}.
 	 */
 	private static Set<Feature> isImplication(Term term) {
 		Set<Feature> rightFeatures = new HashSet<Feature>();
 		if (term instanceof Or) {
 			Or or = (Or) term;
 			Term operand1 = or.getOperand1();
 			Term operand2 = or.getOperand2();
 			if (operand1 instanceof Nested) {
 				Nested nested = (Nested) operand1;
 				operand1 = nested.getOperand();
 			}
 			if (operand2 instanceof Nested) {
				Nested nested = (Nested) operand1;
 				operand2 = nested.getOperand();
 			}
 			if (operand1 instanceof Not && operand2 instanceof FeatureRef) {
 				FeatureRef featureRef = (FeatureRef) operand2;
 				rightFeatures.add(featureRef.getFeature());
 			}
 		}		
 		return rightFeatures;
 	}
 
 	/**
 	 * removes all {@link Feature} from the {@link FeatureModel} which are not in the {@link Set}.
 	 * @param parentFeature a parent {@link Feature}. First call with root {@link Feature}.
 	 * @param features {@link Feature} which are part of the view.
 	 */
 	private static void traverseFeatureModelAndRemoveFeatures(Feature parentFeature,Set<Feature> features) {
 		if (contains(parentFeature, features)) {
 			EList<Group> groups = parentFeature.getGroups();
 			Set<Group> groupsToRemove = new HashSet<Group>();
 			for (Group group : groups) {
 				EList<Feature> childFeatures = group.getChildFeatures();
 				Set<Feature> featuresToRemove = new HashSet<Feature>();
 				for (Feature feature : childFeatures) {
 					if (contains(feature, features)) {
 						traverseFeatureModelAndRemoveFeatures(feature, features);
 					}else{//Remove feature
 						featuresToRemove.add(feature);
 					}
 				}
 				for (Feature feature : featuresToRemove) {
 //					if (feature.getMinCardinality() == 1) { //mandatory feature
 //						if (group.getMinCardinality() > 1) {
 //							group.setMinCardinality(group.getMinCardinality() -1);
 //						}
 //					}
 //					if (group.getMaxCardinality() > 1) {
 //						group.setMaxCardinality(group.getMaxCardinality() -1);
 //					}
 					group.getChildFeatures().remove(feature);
 				}
 				if (group.getChildFeatures().isEmpty()) {
 					groupsToRemove.add(group);
 				}
 //				if (group.getMinCardinality() > group.getChildFeatures().size()) {//TODO mit Julia diskutieren
 //					group.setMinCardinality(group.getChildFeatures().size());
 //				}
 			}
 			for (Group group : groupsToRemove) {
 				parentFeature.getGroups().remove(group);
 			}
 		}
 	}
 
 	/**
 	 * checks if the set contains the {@link Feature}. Check is made by name.
 	 * @param feature the {@link Feature}.
 	 * @param features the set of {@link Feature}.
 	 * @return true if the name of the {@link Feature} is equal to one in the set.
 	 */
 	private static boolean contains(Feature feature, Collection<Feature> features){
 		for (Feature feature2 : features) {
 			if (feature.getName().equals(feature2.getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * checks if the view is able to create a variant.
 	 * @param view the filtered {@link FeatureModel}
 	 * @return true if there is at least one variant.
 	 */
 	public static boolean isConsistent(FeatureModel view){
 		FeatureModelAnalyzer featureModelAnalyzer = new FeatureModelAnalyzer(view);
 		return !featureModelAnalyzer.getDerivableVariants().isEmpty();
 	}
 	
 	/**
 	 * checks the hierarchy of the features, the cardininalities and the constraint.
 	 * @return true is it is consistent.
 	 */
 	public static boolean isConsistent(Set<Feature> features){
 		return  checkHierarchy(features) & checkCardinalities(features) & checkConstraints(features);
 	}
 
 	/**
 	 * checks every constraint from the features.
 	 * @return true if every constraint related feature is in features
 	 */
 	private static boolean checkConstraints(Set<Feature> features) {
 		for (Feature feature : features) {
 			EList<Constraint> constraints = feature.getConstraints();
 			for (Constraint constraint : constraints) {
 				// check constraint
 				//skip excludes
 				if (constraint.getExpression().toLowerCase().contains("not")) {
 					continue;
 				}
 				if(features.containsAll(constraint.getConstrainedFeatures())){
 					if (constraint.getLanguage().equals("OWL")) {
 						boolean exprCheck = false;
 						for (Feature feature2 : features) {
 							if (constraint.getExpression().contains(feature2.getName())) {
 								exprCheck = true;
 								break;
 							}
 						}
 						if (!exprCheck) {
 //							log.debug("Constraintcheck: expression doesnt fit");
 							return false;
 						}
 					}
 				}else{
 //					log.debug("Constraintcheck: missing feature");
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * checks the cardinalities from the features.
 	 * @return true if a no cardinality is compromised
 	 */
 	private static boolean checkCardinalities(Set<Feature> features) {
 		for (Feature feature : features) {
 			EList<org.featuremapper.models.feature.Group> groups = feature.getGroups();
 			for (org.featuremapper.models.feature.Group fGroup : groups) {
 				if (fGroup.getMinCardinality() > 0) {
 					EList<Feature> childFeatures = fGroup.getChildFeatures();
 					int i = 0;
 					for (Feature feature2 : childFeatures) {
 						if (features.contains(feature2)) {
 							i++;
 						}
 					}
 					if (i < fGroup.getMinCardinality()) {
 //						log.debug("Cardinalitycheck: " + feature.getName() + " l:" + fGroup.getMinCardinality() + " l':" + i);
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * checks every ancestor from all features
 	 * @return true if the hierarchy is complete for all features
 	 */
 	private static boolean checkHierarchy(Set<Feature> features) {
 		for (Feature feature : features) {
 			if (!checkAncestors(feature,features)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * checks the ancestors of a feature.
 	 * @param feature the feature to be checked.
 	 * @return true if the hierarchy is complete
 	 */
 	private static boolean checkAncestors(Feature feature,Set<Feature> features) {
 		if (feature.getParentGroup() == null) {
 			return true;//root feature
 		}
 		Feature parentFeature = feature.getParentGroup().getParentFeature();
 		if (features.contains(parentFeature)) {
 			return checkAncestors(parentFeature,features);
 		}else{
 //			log.debug("\t" + feature.getName() + " is missing his parent: " + parentFeature.getName());
 			return false;
 		}
 	}
 	
 	/**
 	 * Opens a save Dialog.
 	 * @param defaultFileName a default file name.
 	 * @param shell the shell is a needed parent for the dialog.
 	 * @return the filename and path.
 	 */
 	public static String save(String defaultFileName, Shell shell) {
 		FileDialog fd = new FileDialog(shell,SWT.SAVE); //find shell
 		fd.setFileName(defaultFileName);
 		return fd.open();
 	}
 }
