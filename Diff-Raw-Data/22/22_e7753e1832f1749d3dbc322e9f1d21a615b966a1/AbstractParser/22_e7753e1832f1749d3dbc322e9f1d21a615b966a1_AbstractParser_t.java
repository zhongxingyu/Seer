 package org.sigwinch.xacml.parser;
 
 import java.util.HashMap;
 
 import org.sigwinch.xacml.tree.DenyOverridesRule;
 import org.sigwinch.xacml.tree.Error;
 import org.sigwinch.xacml.tree.FirstApplicableRule;
 import org.sigwinch.xacml.tree.OnlyOneRule;
 import org.sigwinch.xacml.tree.PermitOverridesRule;
 import org.sigwinch.xacml.tree.Predicate;
 import org.sigwinch.xacml.tree.Scope;
 import org.sigwinch.xacml.tree.SimplePredicate;
 import org.sigwinch.xacml.tree.Tree;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * AbstractParser.java
  * 
  * 
  * Created: Tue Oct 21 19:40:18 2003
  * 
  * @author <a href="mailto:graham@sigwinch.org">Graham Hughes</a>
  * @version 1.0
  */
 abstract public class AbstractParser extends XACMLParser {
     static HashMap<String, HashMap<String, AbstractParser>> nodeLookup;
 
     static HashMap<String, BinaryTreeCreator> ruleLookup;
 
     static {
         nodeLookup = new HashMap<String, HashMap<String, AbstractParser>>();
         HashMap<String, AbstractParser> subtable = new HashMap<String, AbstractParser>();
         nodeLookup.put(xacmlns, subtable);
         subtable.put("Policy", new PolicyParser());
         subtable.put("PolicySet", new PolicySetParser());
         subtable.put("Rule", new RuleParser());
 
         ruleLookup = new HashMap<String, BinaryTreeCreator>();
         BinaryTreeCreator rule = new BinaryTreeCreator() {
             public Tree go(Tree first, Tree second) {
                 return new DenyOverridesRule(first, second);
             }
         };
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:deny-overrides",
                         rule);
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:deny-overrides",
                         rule);
 
         rule = new BinaryTreeCreator() {
             public Tree go(Tree first, Tree second) {
                 return new PermitOverridesRule(first, second);
             }
         };
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:permit-overrides",
                         rule);
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:permit-overrides",
                         rule);
 
         rule = new BinaryTreeCreator() {
             public Tree go(Tree first, Tree second) {
                 return new OnlyOneRule(first, second);
             }
         };
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable",
                         rule);
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:only-one-applicable",
                         rule);
 
         rule = new BinaryTreeCreator() {
             public Tree go(Tree first, Tree second) {
                 return new FirstApplicableRule(first, second);
             }
         };
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable",
                         rule);
         ruleLookup
                 .put(
                         "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable",
                         rule);
     }
 
     public Tree parseElement(Element e) {
         System.out.println("{" + e.getNamespaceURI() + "}" + e.getNodeName());
         return null;
     }
 
     protected Tree maybeScope(Element element, Tree node) {
         Node target = findNode(getList(element, "Target"));
         if (target == null)
             return node;
         return new Scope(node, parseScoping(target));
     }
 
     protected Tree maybeConditions(Element element, Tree node) {
         Node target = findNode(getList(element, "Condition"));
         if (target == null)
             return node;
         Tree result = new Scope(node, parseCondition(target));
         Predicate error = parseError(target);
         if (error == SimplePredicate.FALSE)
             return result;
         return new Error(result, error);
     }
 
     protected Node findNode(NodeList targets) {
         Node target = null;
         if (targets == null)
             return target;
         for (int i = 0; i < targets.getLength(); i++)
             if (targets.item(i).getNodeType() == Node.ELEMENT_NODE) {
                 target = targets.item(i);
                 break;
             }
         return target;
     }
 
     protected Predicate parseCondition(Node target) {
         return ExpressionParser.parseExpression((Element) target);
     }
 
     protected Predicate parseError(Node target) {
         return ExpressionParser.parseError((Element) target);
     }
 
     protected Predicate parseScoping(Node target) {
         Node child = target.getFirstChild();
         Predicate condition = SimplePredicate.TRUE;
         while (child != null) {
             Node grandchild = child.getFirstChild();
            Predicate childcondition = null;
             while (grandchild != null) {
                 if (grandchild.getNodeName().equals("AnySubject")
                         || grandchild.getNodeName().equals("AnyResource")
                         || grandchild.getNodeName().equals("AnyAction"))
                     break;
 
                 // else we go in another level...
                 Predicate grandchildcondition = null;
                 Node grandgrandchild = grandchild.getFirstChild();
                 while (grandgrandchild != null) {
                     if (grandgrandchild.getNodeName().equals("ResourceMatch")
                             || grandgrandchild.getNodeName().equals(
                                     "SubjectMatch")
                             || grandgrandchild.getNodeName().equals(
                                     "ActionMatch")) {
                         if (grandchildcondition == null)
                             grandchildcondition = ExpressionParser
                                     .parseExpression((Element) grandgrandchild);
                         else
                             grandchildcondition = grandchildcondition
                                    .andWith(ExpressionParser
					     .parseExpression((Element) grandgrandchild));
                     }
                     grandgrandchild = grandgrandchild.getNextSibling();
                 }
                if (grandchildcondition != null) {
		    if (childcondition == null)
			childcondition = grandchildcondition;
		    else
			childcondition = childcondition
                            .orWith(grandchildcondition);
		}
                 grandchild = grandchild.getNextSibling();
             }
	    if (childcondition != null)
		condition = condition.andWith(childcondition);
             child = child.getNextSibling();
         }
         return condition;
     }
 
     protected Tree ruleToTree(String text, Tree first, Tree second) {
         return ruleLookup.get(text).go(first, second);
     }
 
     static public Tree parse(Element e) {
         String ns = e.getNamespaceURI();
         if (ns == null)
             ns = xacmlns;
         String name = e.getNodeName();
         AbstractParser parser = nodeLookup.get(ns).get(name);
         return parser.parseElement(e);
     }
 }
 /*
  * arch-tag: 13F35F83-0439-11D8-9466-000A95A2610A
  */
