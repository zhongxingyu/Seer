 /**********************************************************************
  $Id: BucketOp.java,v 1.1 2003/07/23 22:19:28 jinli Exp $
 
 
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
  * The class <code>dupOp</code> is the class for operator Duplicate.
  * 
  * @version 1.0
  *
  * @see op 
  */
 package niagara.xmlql_parser.op_tree;
 
 import org.w3c.dom.Element;
 import niagara.connection_server.InvalidPlanException;
 import niagara.logical.Variable;
 import niagara.optimizer.colombia.*;
 import niagara.xmlql_parser.syntax_tree.*;
 
 import java.util.StringTokenizer;
 
 public class BucketOp extends unryOp {
 	private final static int SEC_AS_MILLISECS = 1000;
 	private final static int MIN_AS_MILLISECS = 60 * SEC_AS_MILLISECS;
 	private final static int HOUR_AS_MILLISECS = 60 * MIN_AS_MILLISECS;
 	private final static int DAY_AS_MILLISECS = 24 * HOUR_AS_MILLISECS;	
 
 	protected Attribute windowAttr;
 	protected int windowType;
 	protected int range;
 	protected int slide;	
 	protected String stInput;
 	private String name;
 
     public void dump() {
 	System.out.println("BucketOp");
     }
    
     public Op opCopy() {
 	BucketOp op = new BucketOp();
 	op.windowAttr = windowAttr;
 	op.windowType = windowType;
 	op.range = range;
 	op.slide = slide;
 	//op.attrInput = attrInput;
 	op.stInput = stInput;
 	return op;
     }
     
     public int hashCode() {
 	return System.identityHashCode(this);
     }
     
     public boolean equals(Object obj) {
 	if (obj == null || !(obj instanceof BucketOp)) return false;
 	if (obj.getClass() != BucketOp.class) return obj.equals(this);
 	BucketOp other = (BucketOp) obj;
 	if (windowAttr != null) 
 		if (!windowAttr.equals(other.windowAttr))
 			return false;
 
 	if (stInput != null)
 		if(!stInput.equals(other.stInput))
 			return false;			
 	return (windowType == other.windowType) &&
 		(range == other.range) &&
 		(slide == other.slide);
     }
 
     protected void loadWindowAttrsFromXML(Element e, 
 						LogicalProperty inputLogProp) 
 	throws InvalidPlanException {
 	String windowAttribute = e.getAttribute("winattr");
 	String type = e.getAttribute("wintype");
 	String windowRange = e.getAttribute("range");
 	String windowSlide = e.getAttribute("slide");
 	
 	if (type.length() == 0) {
 		range = 0;
 		slide = 0;
 		windowType = -1;
 	}
 	else {
		range = parseTimeInterval(windowRange);
		slide = parseTimeInterval(windowSlide);
 		windowType = new Integer(type).intValue();
 		if(windowAttribute.length() != 0)
 			windowAttr = Variable.findVariable(inputLogProp, windowAttribute);					    					
 	}			
     }
     
     public void loadFromXML(Element e, LogicalProperty[] inputProperties)    
 	throws InvalidPlanException {
 		name = e.getAttribute("id");
 		loadWindowAttrsFromXML(e, inputProperties[0]);
 		stInput = e.getAttribute("input");
     }
     
     public Attribute getWindowAttr() {
 	return windowAttr;
     }
     public int getWindowType() {
 	return windowType;
     }
     public int getWindowRange() {
 	return range;
     }
     public int getWindowSlide() {
 	return slide;
     }   
     public String getInput() {
     	return stInput;
     }
     /**
      * 
      * @see niagara.optimizer.colombia.LogicalOp#findLogProp(ICatalog, LogicalProperty[])
      */
     
 	public LogicalProperty findLogProp(
 		ICatalog catalog,
 		LogicalProperty[] input) {
 		
 		LogicalProperty inpLogProp = input[0];
 		// XXX vpapad: Really crude, fixed restriction factor for groupbys
 		float card =
 			inpLogProp.getCardinality() / catalog.getInt("restrictivity");
 		//Vector groupbyAttrs = groupingAttrs.getVarList();
 		// We keep the group-by attributes (possibly rearranged)
 		// and we add an attribute for the aggregated result
 		Attrs data = inpLogProp.getAttrs();
 		Attrs attrs = new Attrs(data.size() + 1);
 		for (int i = 0; i < data.size(); i++) {			
 			Attribute a = (Attribute) data.get(i);
 			attrs.add(a);
 		}
 		
 		attrs.add(new Variable("wid_from", varType.ELEMENT_VAR));
 		attrs.add(new Variable("wid_to", varType.ELEMENT_VAR));
         
 		return  new LogicalProperty(card, attrs, inpLogProp.isLocal());
 	}
 	public int parseTimeInterval(String intervalStr)
 		throws InvalidPlanException {
 		StringTokenizer strtok = new StringTokenizer(intervalStr);
 		boolean expectNumber = true;
 		int currentNumber = 0;
 		int total = 0;
 		while (strtok.hasMoreTokens()) {
 			String tok = strtok.nextToken();
 			if (expectNumber) {
 				try {
 					currentNumber = Integer.parseInt(tok);
 					expectNumber = false;
 				} catch (NumberFormatException nfe) {
 					throw new InvalidPlanException(
 						"Expected integer, found "
 							+ tok
 							+ "while parsing "
 							+ name);
 				}
 			} else {
 				tok = tok.toLowerCase();
 				if (tok.indexOf("day") >= 0)
 					total += currentNumber * DAY_AS_MILLISECS;
 				else if (tok.indexOf("hour") >= 0)
 					total += currentNumber * HOUR_AS_MILLISECS;
 				else if (tok.indexOf("minute") >= 0)
 					total += currentNumber * MIN_AS_MILLISECS;
 				else if (tok.indexOf("second") >= 0)
 					total += currentNumber * SEC_AS_MILLISECS;
 				else if (tok.indexOf("millisecond") >= 0)
 					total += currentNumber;
 				else
 					throw new InvalidPlanException(
 						"Expected time term, found "
 							+ tok
 							+ " while parsing "
 							+ name);
 			}
 		}
 		return total;
 	}
 	
 }
 
