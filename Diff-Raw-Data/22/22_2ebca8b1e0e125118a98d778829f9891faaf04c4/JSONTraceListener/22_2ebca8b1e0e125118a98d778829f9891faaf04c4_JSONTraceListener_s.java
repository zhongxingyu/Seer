 package ru.imobilco;
 
 import java.io.PrintStream;
 import java.text.CharacterIterator;
 import java.text.StringCharacterIterator;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.w3c.dom.Node;
 
 import com.icl.saxon.Context;
 import com.icl.saxon.NodeHandler;
 import com.icl.saxon.expr.Expression;
 import com.icl.saxon.om.Navigator;
 import com.icl.saxon.om.NodeInfo;
 import com.icl.saxon.style.LiteralResultElement;
 import com.icl.saxon.style.StyleElement;
 import com.icl.saxon.style.XSLCopyOf;
 import com.icl.saxon.style.XSLElement;
 import com.icl.saxon.style.XSLVariable;
 import com.icl.saxon.trace.TraceListener;
 
 /**
  * JSON trace listener
  * @author Sergey Chikuyonok (serge.che@gmail.com)
  * @link http://chikuyonok.ru
  *
  */
 public class JSONTraceListener implements TraceListener {
     private PrintStream out = System.err;
     private NodeInfo tmp_source;
     private List<String> allowedXslTags = new ArrayList<String>();
     private XSLCopyOfPrecessor coProcessor;
    private boolean skipTag = false;
     
     protected RootTag root;
     protected Tag cur_tag;
     
     public static final String TYPE_LRE = "LRE";
     public static final String TYPE_XSL = "XSL";
     
     public JSONTraceListener() {
 		root = new RootTag("root");
 		allowedXslTags.add("xsl:template");
 		allowedXslTags.add("xsl:call-template");
 		allowedXslTags.add("xsl:apply-templates");
 		allowedXslTags.add("xsl:apply-imports");
 		allowedXslTags.add("xsl:element");
 		allowedXslTags.add("xsl:copy-of");
 		allowedXslTags.add("xsl:copy");
 		
 		coProcessor = new XSLCopyOfPrecessor();
 	}
 	
 	/**
      * Called at start
      */
     public void open() {
     	cur_tag = root;
     }
 
     /**
      * Called at end
      */
     public void close() {
     	out.println(root.toString());
     }
     
     public void toplevel(NodeInfo element) {
     	
     }
     
     /**
      * Called when a node of the source tree gets processed
      */
     public void enterSource(NodeHandler handler, Context context) {
     	tmp_source = context.getContextNodeInfo();
     }
     
     public void leaveSource(NodeHandler handler, Context context) {
     	
     }
     
     /**
      * Called when an element of the stylesheet gets processed
      */
     public void enter(NodeInfo element, Context context) {
     	if (element instanceof XSLVariable)
    		skipTag = true;
    	
    	if (skipTag)
    		return;
    	
    	if (allowedElement(element)) {
     		Tag tag = new Tag(makeName(element, context));
     		tag.setType(getNodeType(element));
     		cur_tag.addChild(tag);
     		
//    		if (tag.getType() == TYPE_LRE)
//    			tag.setXpath(tag.getParentResultXpath() + "/" + 
//    					makeName(element, context) + 
//    					"[" + getLRENumber(element, context) + "]");
    		
     		String collectionName = (element instanceof StyleElement) ? "xsl" : "xml";
     		tag.setSourceReference(collectionName, element.getSystemId(), 
     				Navigator.getPath(element), element.getLineNumber());
             
             if (tmp_source != null) {
             	tag.setContextReference(tmp_source.getSystemId(), Navigator.getPath(tmp_source), tmp_source.getLineNumber());
             	tmp_source = null;
             }
             
             cur_tag = tag;
     	}
     }
     
     /**
      * Called after an instruction of the stylesheet got processed
      */
 
     public void leave(NodeInfo element, Context context) {
     	if (element instanceof XSLVariable)
    		skipTag = false;
    	else if (!skipTag && allowedElement(element)) {
 			if (element instanceof XSLCopyOf)
 				coProcessor.process((XSLCopyOf) element, context, cur_tag);
 			
 			cur_tag = cur_tag.getParent();
     	}
     }
     
 
     private boolean allowedElement(NodeInfo element) {
     	return element.getNodeType() == NodeInfo.ELEMENT && 
     		(getNodeType(element) == TYPE_LRE || 
     			allowedXslTags.contains(element.getDisplayName()));
 	}
     
     /**
      * Get the trace element tagname to be used for a particular construct. Return null for
      * trace events that are ignored by this trace listener.
      */
     protected String getNodeType(NodeInfo info) {
         if (info instanceof LiteralResultElement || info instanceof XSLElement) {
         	return TYPE_LRE;
         } else {
         	return TYPE_XSL;
         }
     }
 
     /**
      * Set the output destination (default is System.err)
      * @param stream the output destination for tracing output
      */
 
     public void setOutputDestination(PrintStream stream) {
         out = stream;
     }
 
     /**
      * Get the output destination
      */
 
     public PrintStream getOutputDestination() {
         return out;
     }
     
     /**
      * Creates element's display name. It handles xsl:element properly, 
      * returning expanded element name. 
      * @param node
      * @param context
      * @return
      */
     private String makeName(NodeInfo node, Context context) {
 		if (node instanceof XSLElement) {
 			XSLElement elem = (XSLElement) node;
 			String nameAttr = elem.getAttribute("name");
 			if (nameAttr != null) {
 				Expression elementName;
 				try {
 					elementName = elem.makeAttributeValueTemplate(nameAttr);
 					return elementName.evaluateAsString(context);
 				} catch (Exception e) {
 					return nameAttr;
 				}
 			} else {
 				return nameAttr;
 			}
 		} else {
 			return node.getDisplayName();
 		}
 	}
     
     /**
      * Get an absolute XPath expression that identifies a given node within 
      * result document
      */
 
      public String getLREPath(NodeInfo node, Context context, NodeInfo parent) {
          String xpath = "";
          NodeInfo iterNode = node;
          do {
         	 if (iterNode.getNodeType() == NodeInfo.ELEMENT) {
         		 if (getNodeType(iterNode) == TYPE_LRE) {
         			 xpath = "/" + makeName(iterNode, context) + "[" + getLRENumber(iterNode, context) + "]" + xpath;
         		 }
         	 }
         	 
         	 iterNode = iterNode.getParent();
          } while(iterNode != parent);
          
          return xpath;
      }
      
      /**
       * Get simple node number. This is defined as one plus the number of previous siblings of the
       * same node type and name. It is not accessible directly in XSL. This version doesn't require
       * the context, and therefore doesn't remember previous results
       */
 
       public int getLRENumber(NodeInfo node, Context context) {
     	  int pos = 1;
     	  String curName = makeName(node, context);
     	  if (node instanceof Node) {
     		  Node prev = (Node) node;
     		  prev = prev.getPreviousSibling();
     		  while (prev != null) {
     			  if (curName.equals(makeName((NodeInfo) prev, context))) {
     				  pos++;
     			  }
     			  prev = prev.getPreviousSibling();
     		  }
     	  }
     	  
     	  return pos;
       }
       
       /**
        * Setup base XML and XSL files used for transformation
        * @param xmlFile
        * @param xslFile
        */
       public void setBaseFiles(String xmlFile, String xslFile) {
     	  root.setBaseFiles(xmlFile, xslFile);
       }
       
       public static String escapeHTML(String text) {
     	  final StringBuilder result = new StringBuilder();
     	  final StringCharacterIterator iterator = new StringCharacterIterator(text);
     	  char character =  iterator.current();
     	  while (character != CharacterIterator.DONE ) {
     		  if (character == '\'') {
     			  result.append("\\'");
     		  } else if (character == '"') {
     			  result.append("\\\"");
     		  } else if (character != '\n' && character != '\r') {
     			  result.append(character);
     		  }
     		  
     		  character = iterator.next();
     	  }
     	  
     	  return result.toString();
       }
 }
