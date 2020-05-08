 /**********************************************************************
  $Id: Join.java,v 1.4 2007/04/28 21:27:20 jinli Exp $
 
 
   NIAGARA -- Net Data Management System                                 
                                                                         
   Copyright (c)    Computer Sciences Department, University of          
                        Wisconsin -- Madison                             
   All Rights Reserved.                                                  
                                                                         
   Permission to use, copy, modify and distribute this software and      
   its documentation is hereby granted, provided that both the           
   copyright notice and this permission notice appear in all copies      
   of the software, derivative works or modified versions, and any       
   portions thereof, and that both notices appear in supporting          
   documentation.                                                        
                                                                         
   THE AUTHORS AND THE COMPUTER SCIENCES DEPARTMENT OF THE UNIVERSITY    
   OF WISCONSIN - MADISON ALLOW FREE USE OF THIS SOFTWARE IN ITS "        
   AS IS" CONDITION, AND THEY DISCLAIM ANY LIABILITY OF ANY KIND         
   FOR ANY DAMAGES WHATSOEVER RESULTING FROM THE USE OF THIS SOFTWARE.   
                                                                         
   This software was developed with support by DARPA through             
    Rome Research Laboratory Contract No. F30602-97-2-0247.  
 **********************************************************************/
 
 /**
  * This class is used to represent the join operator.
  *
  */
 package niagara.logical;
 
 import gnu.regexp.RE;
 import gnu.regexp.REException;
 import gnu.regexp.REMatch;
 
 import java.util.ArrayList;
 
 import niagara.connection_server.Catalog;
 import niagara.connection_server.InvalidPlanException;
 import niagara.logical.predicates.*;
 import niagara.optimizer.colombia.Attribute;
 import niagara.optimizer.colombia.Attrs;
 import niagara.optimizer.colombia.ICatalog;
 import niagara.optimizer.colombia.LogicalProperty;
 import niagara.optimizer.colombia.Op;
 
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class Join extends BinaryOperator {
     // values for extension join...
     public static final int NONE = 0;
     public static final int LEFT = 1;
     public static final int RIGHT = 2;
     public static final int BOTH = 3;
 
     protected Predicate pred; // non-equijoin part of the predicate
 
     // for equi-join represents the attributes of the left relation
     // that will join with those of the right relation
     protected EquiJoinPredicateList equiJoinPredicates;
 
     /** The attributes we're projecting on (null means keep all attributes) */
     protected Attrs projectedAttrs;
 
     protected int extensionJoin;
     
     private String[] punctAttrs = null;
 
     public Join() {
 	extensionJoin = NONE; // default
     }
 
     public Join(
         Predicate pred,
         EquiJoinPredicateList equiJoinPredicates,
         Attrs projectedAttrs,
        int extensionJoin, String[] punctAttrs) {
         this.pred = pred;
         this.equiJoinPredicates = equiJoinPredicates;
         this.projectedAttrs = projectedAttrs;
         this.extensionJoin = extensionJoin;
         this.punctAttrs = punctAttrs;
     }
 
     public Join(Predicate pred, EquiJoinPredicateList equiJoinPredicates,
 		  int extensionJoin) {
         this(pred, equiJoinPredicates, null, extensionJoin, null);
     }
 
     public EquiJoinPredicateList getEquiJoinPredicates() {
         return equiJoinPredicates;
     }
 
     public Predicate getNonEquiJoinPredicate() {
         return pred;
     }
 
     public int getExtensionJoin(){
 	return extensionJoin;
     }
 
     /**
      * @param p non-equijoin part of the predicate
      * @param left attributes of the equi-join
      * @param right attributes of the equi-join
      */
     public void setJoin(Predicate p, ArrayList left, ArrayList right,
 			int extensionJoin) {
         equiJoinPredicates = new EquiJoinPredicateList(left, right);
         if (p != null)
             pred = p;
         else
             pred = True.getTrue();
 	this.extensionJoin = extensionJoin;
     }
 
     public void setCartesian(Predicate p) {
         equiJoinPredicates = new EquiJoinPredicateList();
         pred = True.getTrue();
     }
 
     /**
      * print the operator to the standard output
      */
     public void dump() {
         System.out.println("Join : ");
         if (pred != null)
             pred.dump(1);
     }
 
     /**
      * dummy toString method
      *
      * @return the String representation of the operator
      */
     public String toString() {
         StringBuffer strBuf = new StringBuffer();
         strBuf.append("Join");
 
         return strBuf.toString();
     }
 
     public void dumpAttributesInXML(StringBuffer sb) {
         equiJoinPredicates.toXML(sb);
     }
     public void dumpChildrenInXML(StringBuffer sb) {
         sb.append(">");
         pred.toXML(sb);
         sb.append("</join>");
     }
 
     /**
      * @see niagara.optimizer.colombia.LogicalOp#findLogProp(ICatalog, LogicalProperty[])
      */
     public LogicalProperty findLogProp(
         ICatalog catalog,
         LogicalProperty[] input) {
         LogicalProperty left = input[0];
         LogicalProperty right = input[1];
 
         // check the joined predicates(attributes) are in the schema
         assert(left.getAttrs().contains(equiJoinPredicates.getLeft()));
         assert(right.getAttrs().contains(equiJoinPredicates.getRight()));
 
         LogicalProperty result = left.copy();
 
         Predicate allPredicates =
             And.conjunction(equiJoinPredicates.toPredicate(), pred);
         result.setCardinality(
             left.getCardinality()
                 * right.getCardinality()
                 * allPredicates.selectivity());
 
         result.setHasLocal(left.hasLocal() || right.hasLocal());
         result.setHasRemote(left.hasRemote() || right.hasRemote());
 
         // Derive the schema
         if (projectedAttrs == null)
             result.getAttrs().merge(right.getAttrs());
         else
             result.setAttrs(projectedAttrs);
 
         return result;
     }
 
     public boolean isCartesian() {
         return equiJoinPredicates.size() == 0;
     }
 
     public boolean isEquiJoin() {
         return equiJoinPredicates.size() > 0;
     }
 
     /** Can we push any of the non-equijoin predicates of this join down? */
     public boolean hasPushablePredicates(Attrs left, Attrs right) {
         And conj = pred.split(left);
         if (!conj.getLeft().equals(True.getTrue()))
             return true;
         conj = pred.split(right);       
         if (!conj.getLeft().equals(True.getTrue()))
             return true;
             
         return false;
     }
     
     public Op opCopy() {
         return new Join(
             pred,
             equiJoinPredicates,
             (projectedAttrs == null)?null:projectedAttrs.copy(),
 	    extensionJoin, punctAttrs);
     }
 
     public boolean equals(Object obj) {
         if (obj == null || !(obj instanceof Join))
             return false;
         if (obj.getClass() != Join.class)
             return obj.equals(this);
         Join other = (Join) obj;
         
        if ((punctAttrs==null) ^ (other.punctAttrs==null))
        	return false;
        
        return pred.equals(other.pred) &&
			   equiJoinPredicates.equals(other.equiJoinPredicates) &&
			   equalsNullsAllowed(projectedAttrs, other.projectedAttrs) &&
			   (extensionJoin == other.extensionJoin);
     }
 
     public int hashCode() {
         return equiJoinPredicates.hashCode()
             ^ pred.hashCode()
             ^ hashCodeNullsAllowed(projectedAttrs);
     }
 
     public void projectedOutputAttributes(Attrs outputAttrs) {
         projectedAttrs = outputAttrs;
     }
 
     public Attrs requiredInputAttributes(Attrs inputAttrs) {
         ArrayList al = new ArrayList();
         equiJoinPredicates.getReferencedVariables(al);
         pred.getReferencedVariables(al);
         Attrs reqd = new Attrs(al);
         assert inputAttrs.contains(reqd);
         return reqd;
     }
 
     public Attrs getProjectedAttrs() {
         return projectedAttrs;
     }
 
     /** @return a copy of this join, with an additional condition */
     public Join withExtraCondition(Predicate newPred, Attrs leftAttrs, Attrs rightAttrs) {
         Join newJoin = (Join) this.copy();
         
         // Determine which parts of the new predicate contribute to the equijoin, and which don't
         And newJoinPred = (And) newPred.splitEquiJoin(leftAttrs, rightAttrs);
         Predicate equiPred = newJoinPred.getLeft();
         Predicate nonEquiPred = newJoinPred.getRight();
         
         if (!equiPred.equals(True.getTrue())) {
             UpdateableEquiJoinPredicateList p = equiJoinPredicates.updateableCopy();
             p.addAll(equiPred.toEquiJoinPredicateList(leftAttrs, rightAttrs));
             newJoin.equiJoinPredicates = p;
         }
         
         newJoin.pred = And.conjunction(nonEquiPred, pred);
         return newJoin;
     }
 
     public void loadFromXML(Element e, LogicalProperty[] inputProperties, Catalog catalog)
         throws InvalidPlanException {
         String id = e.getAttribute("id");
 
         NodeList children = e.getChildNodes();
         Element predElt = null;
 
         for (int i = 0; i < children.getLength(); i++) {
             if (children.item(i) instanceof Element) {
                 predElt = (Element) children.item(i);
                 break;
             }
         }
 
         Predicate pred = Predicate.loadFromXML(predElt, inputProperties);
 
         // In case of an equijoin we have to parse "left" 
         // and "right" to get additional equality predicates
         String leftattrs = e.getAttribute("left");
         String rightattrs = e.getAttribute("right");
 
         ArrayList leftVars = new ArrayList();
         ArrayList rightVars = new ArrayList();
 
         if (leftattrs.length() > 0) {
             try {
                 RE re = new RE("(\\$)?[a-zA-Z0-9_]+");
                 REMatch[] all_left = re.getAllMatches(leftattrs);
                 REMatch[] all_right = re.getAllMatches(rightattrs);
                 for (int i = 0; i < all_left.length; i++) {
                     Attribute leftAttr =
                         Variable.findVariable(
                             inputProperties[0],
                             all_left[i].toString());
                     Attribute rightAttr =
                         Variable.findVariable(
                             inputProperties[1],
                             all_right[i].toString());
                     leftVars.add(leftAttr);
                     rightVars.add(rightAttr);
                 }
             } catch (REException rx) {
                 throw new InvalidPlanException(
                     "Syntax error in equijoin predicate specification for "
                         + id);
             }
         }
         String extensionJoinAttr = e.getAttribute("extensionjoin");
         int extJoin;
         if (extensionJoinAttr.equals("right")) {
             extJoin = Join.RIGHT;
         } else if (extensionJoinAttr.equals("left")) {
             extJoin = Join.LEFT;
         } else if (extensionJoinAttr.equals("none")) {
             extJoin = Join.NONE;
         } else if (extensionJoinAttr.equals("both")) {
             extJoin = Join.BOTH;
         } else {
             throw new InvalidPlanException(
                 "Invalid extension join value " + extensionJoinAttr);
         }
 
         setJoin(pred, leftVars, rightVars, extJoin);
         
         String punctattr = e.getAttribute("punctattr");
         if (punctattr=="")
         	return;
         punctAttrs = punctattr.split("[\t| ]+");
                 
         Variable.findVariable(inputProperties[0], punctAttrs[0]);
         Variable.findVariable(inputProperties[1], punctAttrs[1]);
     }
     public String[] getPunctAttrs() {
     	return punctAttrs;
     }
     
 }
