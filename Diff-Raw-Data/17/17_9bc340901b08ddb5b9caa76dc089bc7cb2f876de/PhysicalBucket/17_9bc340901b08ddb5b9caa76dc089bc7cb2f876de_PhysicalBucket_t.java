 /**********************************************************************
  $Id: PhysicalBucket.java,v 1.8 2007/05/25 04:13:03 jinli Exp $
 
 
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
 
 
 package niagara.physical;
 
 import niagara.utils.*;
 
 import niagara.logical.*;
 import niagara.optimizer.colombia.*;
 import niagara.query_engine.*;
 
 import org.w3c.dom.*;
 import java.lang.Object;
 import java.util.ArrayList;
 
 /**
  * Implementation of the Select operator.
  */
  
 public class PhysicalBucket extends PhysicalOperator {
     // No blocking source streams
     private static final boolean[] blockingSourceStreams = { false };
 
     //window Attributes
     private Attribute windowAttribute;
     private int windowType;
     private long range;
     private long slide;
     private int count = 0;
     private String widName;
     private long windowId_from = 0;
     private long windowId_to = 0;    
     private int windowCount = 0;
 
     private Document doc;
     
    //Data template for creating punctuation
    private String rgstDataChild[] = null;
    private short rgiDataType[];
    private String stDataRoot;
    // Data template for creating punctuation
    Tuple tupleDataSample = null;  
    private long startSecond;
    private long start;
    
    AtomicEvaluator ae;
    
    
     public PhysicalBucket() {
 	setBlockingSourceStreams(blockingSourceStreams);
     }
     
     public void opInitFrom(LogicalOp logicalOperator) { //I think I should change the logical operator to groupOp and related stuff
 	// Get the averaging attribute of the average logical operator
 	windowAttribute = ((Bucket) logicalOperator).getWindowAttr();
 	windowType = ((Bucket) logicalOperator).getWindowType();		
 	range = ((Bucket) logicalOperator).getWindowRange();
 	slide = ((Bucket) logicalOperator).getWindowSlide();
 	widName = ((Bucket) logicalOperator).getWid();
 	start = ((Bucket) logicalOperator).getStarttime();
 	ae = new AtomicEvaluator (windowAttribute.getName());
 	
     }
 
     public Op opCopy() {
 	PhysicalBucket p = new PhysicalBucket();
 	p.windowAttribute = windowAttribute;
 	p.windowType = windowType;
 	p.range = range;
 	p.slide = slide;
 	p.widName = widName;
 	p.start = start;
 	p.ae = ae;
 
 	return p;
     }
     
     public void opInitialize() {
 	setBlockingSourceStreams(blockingSourceStreams);
 	ae.resolveVariables(inputTupleSchemas[0], 0);
 	}   
     
     /**
      * This function processes a tuple element read from a source stream
      * when the operator is non-blocking. This over-rides the corresponding
      * function in the base class.
      *
      * @param tupleElement The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
 
     protected void processTuple (
 			     Tuple inputTuple, int streamId)
 	throws ShutdownException, InterruptedException {
 	Tuple result;
 	//If we haven't already picked up a template tuple,
 	// copy this one.
 	if (tupleDataSample == null)
 		tupleDataSample = inputTuple;
 			
 	count++;
     result = appendWindowId(inputTuple, streamId);
     putTuple(result, 0);	    
 	if ((count % slide == 0) && (windowType == 0)) {
 		//output a punctuation to say windowId_from is completed			
 		putTuple (createPunctuation (inputTuple, windowId_from), 0);
 	}    		
         
     }
    
     protected Tuple appendWindowId (Tuple inputTuple, int steamId) 
     throws ShutdownException, InterruptedException {
    	
     	// tuple-based window
     	if (windowType == 0) {
     		windowId_from = count / slide;
     		windowId_to = (count + range) / slide - 1;
     	} else if (windowType == 1) { //value-based window
     		long timestamp;
     		try {
     			ArrayList values = new ArrayList (); 
     			ae.getAtomicValues(inputTuple, values);
     			timestamp = Long.parseLong(((BaseAttr)values.get(0)).toASCII());
     		} catch (NumberFormatException nfe) {
     			timestamp = 0;
     		}
     		
     		if ( count==1 ) {
     			startSecond = timestamp;
     			if (start == Long.MIN_VALUE)
     				start = startSecond;
     		}
         		
     		windowId_from = (timestamp - start) / slide;
     		windowId_to = ((timestamp - start) + range) / slide - 1;
     		
     	}
     	int outSize = outputTupleSchema.getLength();
     	
     	LongAttr from, to; 
     	
     	from = new LongAttr(windowId_from);
     	to = new LongAttr(windowId_to);
     	inputTuple.appendAttribute(from);
     	inputTuple.appendAttribute(to);
     	return inputTuple;
     }
     
     /*protected Tuple appendWindowId (Tuple inputTuple, int steamId) 
     throws ShutdownException, InterruptedException {
 	
 	long mod;
 	long numOfWindows;
 	long prev = 0;
 	
 	//wid_from = 1, 2, 3 ...
 	//count = 1, 2, 3, ...	
     	if (windowType == 0) { // tuple-based window    		
 		// Compute the window Id
 		//
 		if ((range % slide) == 0) {
 			numOfWindows = range / slide;
 			windowId_from = count / slide;
 			
 			mod = count % slide;
 			if (mod != 0)
 				windowId_from += 1;
 			windowId_to = windowId_from + numOfWindows - 1;		
 		}
 		else {
 			numOfWindows = range / slide + 1;
 			windowId_from = count / slide;
 			mod = count % slide;
 			if (mod != 0)
 				windowId_from += 1;
 			prev = windowId_from;
 			windowId_to = (range + count) / slide;
 			if ((range + count) % slide == 0)
 				windowId_to -= 1;			
 		}
 		
     	}
 	else if (windowType == 1) { // value-based window
 				
 		long timestamp;
 		try {
 			ArrayList values = new ArrayList (); 
 			ae.getAtomicValues(inputTuple, values);
 			timestamp = Long.parseLong(((BaseAttr)values.get(0)).toASCII());
 		} catch (NumberFormatException nfe) {
 			timestamp = 0;
 		}
 		if ( count==1 )
 			startSecond = timestamp;
     		
 		if ((range % slide) == 0) {
 			numOfWindows = range / slide;
 			windowId_from = (timestamp - startSecond + 1) / slide;
 			mod = (timestamp - startSecond + 1) % slide;
 			if (mod != 0)
 				windowId_from += 1;
 			prev = windowId_from;
 			windowId_to = windowId_from + numOfWindows - 1;
 			}
 			else {
 				numOfWindows = range / slide + 1;
 				windowId_from = (timestamp - startSecond + 1) / slide;
 				mod = (timestamp - startSecond + 1) % slide;
 				if (mod != 0)
 					windowId_from += 1;
 				prev = windowId_from;
 				windowId_to = (range + (timestamp - startSecond + 1)) / slide;
 				if (( (range + (timestamp - startSecond + 1)) / slide ) == 0) {
 					windowId_to -= 1;
 				}
     			
 			} 
 		}	
     		
 	int outSize = outputTupleSchema.getLength();
 	
 	LongAttr from, to; 
 	
 	from = new LongAttr(windowId_from);
 	to = new LongAttr(windowId_to);
 	inputTuple.appendAttribute(from);
 	inputTuple.appendAttribute(to);
 	return inputTuple;
 	
     }*/
     
     /**
      * This function processes a punctuation element read from a source stream
      * when the operator is non-blocking. This over-rides the corresponding
      * function in the base class.
      *
      * Punctuations can simply be sent to the next operator from Select
      *
      * @param inputTuple The tuple element read from a source stream
      * @param streamId The source stream from which the tuple was read
      *
      * @exception ShutdownException query shutdown by user or execution error
      */
 /*    protected void processPunctuation(StreamPunctuationElement inputTuple,
 				      int streamId)
 	throws ShutdownException, InterruptedException {
 	if (inputTuple.isPunctuation()) {  // for time-based bucket
 		
 	    windowCount++;  	    
 	    Element from = doc.createElement("wid_from");
 	    from.appendChild(doc.createTextNode("(," + String.valueOf(windowCount) + ")"));   //windowId_from? 	
 	    Element to = doc.createElement("wid_to");
 	    to.appendChild(doc.createTextNode(String.valueOf("*")));    
 	    
 	    inputTuple.appendAttribute(from);
 	    inputTuple.appendAttribute(to);
 	    
 	    putTuple(inputTuple, streamId);
 	    
 	}
 	//putTuple(inputTuple, streamId);
     }*/
     
 	protected void processPunctuation(Punctuation inputTuple,
 					  int streamId)
 	throws ShutdownException, InterruptedException {
  
	   long wid=0;
 	   long timestamp;

 	   String punctVal;
 	   
 	   ArrayList values = new ArrayList();
 	   ae.getAtomicValues(inputTuple, values);
 
 	   //assume that the punctuation on winattr should have the format of xxx, which means the end of xxx
 	   punctVal = ((BaseAttr)values.get(0)).toASCII().trim();
 	   //length = punctVal.length();
 	   //String tmp = punctVal.substring(1, length - 1).trim();
 	   //length = tmp.length();
 	   //punctVal = tmp.substring(1, length);
	   try { 
		   timestamp = Long.parseLong(punctVal);
	   } catch (NumberFormatException e) {
		   System.err.println (punctVal + " is not a long integer - Bucket cannot handle such a punctuating attribute");
		   return;
	   }
 	   
 	   // punctuation may come in a finer granularity; e.g., 20 seconds rather than 1 minute;
 	   if (slide != 1)
 		   if ((timestamp - startSecond) == 0 || (timestamp - startSecond) % slide != 0)
 			   return;
 	      
 	   wid = (long)((timestamp+1) - start) / slide - 1;
 	   
 	   System.err.println("window_id: "+wid + "   timestamp: "+timestamp);
 	   if (timestamp < -1000) 
 		   System.err.println("sth is wrong ...");
 
 	   
 	 /*  if ((range % slide) == 0) {
 		   wid = (long)(timestamp - startSecond + 1) / slide;
 		   mod = (long)(timestamp - startSecond + 1) % slide;
 		   if (mod != 0)
 			   wid += 1;
 	   }
 	   else {
 			wid =(long) (timestamp - startSecond + 1) / slide;
 			mod = (long) (timestamp - startSecond + 1) % slide;
 	   }*/ 
 	   
 	   Punctuation punct = null;
 	    	    
 	   //Element ts = doc.createElement(windowAttrName);
 	   //ts.appendChild(doc.createTextNode("*"));
 	   
 	   StringAttr ts = new StringAttr("*");
 	   inputTuple.setAttribute(ae.attributeId, ts);
 		
 	   StringAttr from, to;
 	   
 	   from = new StringAttr(String.valueOf(wid));
 	   to = new StringAttr("*");
 	    
 	   inputTuple.appendAttribute(from);	   	    
 	   inputTuple.appendAttribute(to);
 
 	   putTuple(inputTuple, streamId);
 	}
 	    
 	/**
 	 * This function generates a punctuation based on the timer value
 	 * using the template generated by setupDataTemplate
 	 */
 	private Punctuation createPunctuation(
 	Tuple inputTuple, long value) {
 	//This input came from the timer. Generate punctuation 
 	// based on the timer value, where the time value is
 	// (,last), indicating that all values from the beginning
 	// to `last' have been seen. Note this assumes the 
 	// attribute is strictly non-decreasing.
 	Element eChild;
 	Text tPattern;
 	short nodeType;
 	
 	//Create a new punctuation element
 	Punctuation spe =
 		new Punctuation(false);
 	for (int iAttr=0; iAttr<tupleDataSample.size(); iAttr++) {
 		//Node ndSample = tupleDataSample.getAttribute(iAttr);
 		Object ndSample = tupleDataSample.getAttribute(iAttr);
 		
 		if (ndSample instanceof BaseAttr) {
 			spe.appendAttribute(((BaseAttr)ndSample).copy());		
 		} else if (ndSample instanceof Node) {
 			nodeType = ((Node)ndSample).getNodeType();
 			if (nodeType == Node.ATTRIBUTE_NODE) {
 				Attr attr = doc.createAttribute(((Node)ndSample).getNodeName());
 				attr.appendChild(doc.createTextNode("*"));
 				spe.appendAttribute(attr);
 			} else {
 				String stName = ((Node)ndSample).getNodeName();
 				if (stName.compareTo("#document") == 0)
 				stName = new String("document");
 				Element ePunct = doc.createElement(stName);
 				ePunct.appendChild(doc.createTextNode("*"));
 				spe.appendAttribute(ePunct);
 			}			
 		}
 	}
 	//tPattern = doc.createTextNode("(," +Long.toString(value) + ")");
 	//hack
 	/*tPattern = doc.createTextNode(Long.toString(value));
 
 	eChild = doc.createElement("wid_from_"+widName);
 	eChild.appendChild(tPattern);
 	spe.appendAttribute(eChild);
 	
 	tPattern = doc.createTextNode("*");
 
 	eChild = doc.createElement("wid_to_"+widName);
 	eChild.appendChild(tPattern);
 	spe.appendAttribute(eChild);*/
 	spe.appendAttribute(new StringAttr(value));
 	spe.appendAttribute(new StringAttr("*"));
 		
 	return spe;
 	}	
     public boolean isStateful() {
 	return false;
     }
     
     /**
      * @see niagara.optimizer.colombia.PhysicalOp#FindLocalCost(ICatalog, LogicalProperty, LogicalProperty[])
      */
     public Cost findLocalCost(ICatalog catalog, LogicalProperty[] inputLogProp) {
 	double trc = catalog.getDouble("tuple_reading_cost");
 	double sumCards = 0;
 	for (int i = 0; i < inputLogProp.length; i++)
 	    sumCards += inputLogProp[i].getCardinality();
 	return new Cost(trc * sumCards);
     }
     
 
     public boolean equals(Object o) {
 	 if (o == null || !(o instanceof PhysicalBucket))
 	     return false;
 	 if (o.getClass() != PhysicalBucket.class)
 	     return false;
 	 
 	 PhysicalBucket another = (PhysicalBucket) o;
 	 
 	 boolean match = another.windowAttribute.equals(this.windowAttribute) && 
 	 another.range == this.range && another.slide  == this.slide &&
 	 another.windowType == this.windowType && 
 	 another.start == this.start;
 	 if (match)
 		 System.err.println("PhysicalBucket equal true");
 	 return match;
 	 //return getArity() == ((PhysicalBucket) o).getArity();
      }
 
 
     /**
      * @see java.lang.Object#hashCode()
      */
     public int hashCode() {
 	return getArity();
     }    
     /**
      * @see niagara.optimizer.colombia.PhysicalOp#FindPhysProp(PhysicalProperty[])
      */
     public PhysicalProperty findPhysProp(PhysicalProperty[] input_phys_props) {
 	return input_phys_props[0];
     }
     
     public void setResultDocument(Document doc) {
 	this.doc = doc;
     }    
     
 	 /**
 	  * @see niagara.query_engine.SchemaProducer#constructTupleSchema(TupleSchema[])
 	  */
 	 public void constructTupleSchema(TupleSchema[] inputSchemas) {
 		 super.constructTupleSchema(inputSchemas);
 		/*inputTupleSchemas = inputSchemas;
 	 	outputTupleSchema = inputSchemas[0].copy();*/
 /*	 	Attribute attrFrom, attrTo;
 	 	
 		attrFrom = logProp.getAttr("wid_from_"+widName);
 		attrTo = logProp.getAttr("wid_to_"+widName);
 
 		outputTupleSchema.addMapping(attrFrom);
 	 	outputTupleSchema.addMapping(attrTo);*/
 	 }        
 }
 
 
