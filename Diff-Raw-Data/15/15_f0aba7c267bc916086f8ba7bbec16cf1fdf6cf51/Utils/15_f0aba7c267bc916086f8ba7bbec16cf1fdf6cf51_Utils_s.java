 package edu.utwente.vb.example.util;
 
 import java.io.File;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.util.List;
 
 import org.antlr.runtime.CommonToken;
 import org.objectweb.asm.Type;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import org.antlr.runtime.Token;
 import org.antlr.runtime.tree.BaseTree;
 import org.antlr.runtime.tree.CommonTree;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 import edu.utwente.vb.example.CodegenPreparation.Usage;
 import edu.utwente.vb.symbols.ExampleType;
 import edu.utwente.vb.symbols.Prelude;
 import edu.utwente.vb.tree.AppliedOccurrenceNode;
 import edu.utwente.vb.tree.BindingOccurrenceNode;
 import edu.utwente.vb.tree.TypedNode;
 
 public class Utils {
 	private static Logger log = LoggerFactory.getLogger(Utils.class);
 	
 	public static String camelCaseName(String fileName){
 		List<String> words = Lists.newArrayList();
 		StringBuffer word = new StringBuffer();
 		
 		for(Character c : Lists.charactersOf(fileName)){
 			if(c.equals('_') || c.equals('-')){
 				words.add(word.toString());
 				word = new StringBuffer();
 			} else if(c.equals('.')){
 				break;//extensie begint
 			} else {
 				word.append(c);
 			} 
 		}
 		words.add(word.toString());
 				
 		StringBuffer camelCase = new StringBuffer();
 		for(String w : words){
 			if(w.length() >=  2){
 				camelCase.append(w.substring(0,1).toUpperCase());
 				camelCase.append(w.substring(1));
 			} else {
 				camelCase.append(System.currentTimeMillis());
 			}
 		}
 		
 		return camelCase.toString();
 	}
 	
 	public static void stripQuotes(Token token){
     	CommonToken t = ((CommonToken)token);
         t.setStartIndex(t.getStartIndex() + 1);
         t.setStopIndex(t.getStopIndex() - 1);
 	}
 	
 	public static boolean isUnknownVarNode(Object node){
 		if(!(node instanceof BindingOccurrenceNode))
 			return false;
 		
 		log.debug("Is unknown? {}", ((BindingOccurrenceNode)node).getNodeType());
 		
 		return ExampleType.UNKNOWN.equals(((BindingOccurrenceNode)node).getNodeType());	
 	}
 	
 	public static void updateUsage(TypedNode node, Usage usage){
 	    BindingOccurrenceNode binding;
 	    if(node instanceof AppliedOccurrenceNode){
 		binding = (BindingOccurrenceNode)((AppliedOccurrenceNode)node).getBindingNode();
 	    } else {
 		binding = (BindingOccurrenceNode)node;
 	    }
 	    
 	    switch(usage){
 	    	case READ:
 	    	    binding.incReadCount();
 	    	    break;
 	    	case WRITE:
 	    	    binding.incAssignCount();
 	    	    break;
 	    }
 	    
 	    log.debug("Status of {}", binding);
 	}
 }
