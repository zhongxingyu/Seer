 package ru.iitp.proling.features;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.antlr.runtime.ANTLRStringStream;
 import org.antlr.runtime.CharStream;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.RecognitionException;
 import org.antlr.runtime.tree.Tree;
 
 
 public class FeatureParser {
 	FeatureRegister fb;
 	
 	public FeatureParser(FeatureRegister fb) {
 		this.fb = fb;
 	}
 	
 	
 	
 	public FeatureExtractor parse(String def) throws RecognitionException {
 		CharStream stream = new ANTLRStringStream(def);
 		return parse(stream);
 	}
 	
 	FeatureExtractor parse(CharStream stream) throws RecognitionException {
 		FeaturesLanguageLexer lexer = new FeaturesLanguageLexer(stream);
 		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
 		FeaturesLanguageParser parser = new FeaturesLanguageParser(tokenStream);
 		Tree tree = parser.features().tree;
 		return parse(tree);
 	}
 	
 	FeatureExtractor parse(Tree tree) {
 		FeatureExtractor eval = new FeatureExtractor();
 		if(tree.getText() != null) {
 			eval.addFeature(parseFeature(tree, eval));
 		} else {
 			for(int i = 0; i != tree.getChildCount(); i++)
 				eval.addFeature(parseFeature(tree.getChild(i),eval));
 		}
 		
 		return eval;
 	}
 	
 	FeatureValue parseFeature(Tree tree, FeatureExtractor eval) {
 		String name = tree.getText();
 		List<Value> args = new ArrayList<Value>();
 		/*if(fb.hasInjectedArg(name))
 			args.add(eval.getRoot());*/
 		
 		for(int i = 0; i != tree.getChildCount(); i++) {
 			args.add(parseArg(tree.getChild(i), eval));
 		}
 		
 		Feature f = fb.build(name, args);
 		FeatureValue fv = eval.rewrite(new FeatureValue(f, new Values.Simple(), args));
 		Value fv0 = eval.get(fv);
 		
 		if(fv == fv0) {
 			for(Value arg : args)
 				eval.setArg(fv, arg);
 		}
 		
 		return (FeatureValue) fv0;
 	}
 	
 	Value parseArg(Tree tree, FeatureExtractor eval) {
 		return tree.getChildCount() == 0? parseSimpleArg(tree, eval) : parseFeature(tree, eval);
 	}
 		
 	Value parseSimpleArg(Tree tree, FeatureExtractor eval) {
 		String text = tree.getText();
 		if(text.isEmpty())
 			return new Values.Final("");
 		
 		if(text.charAt(0) == '"')
 			return parseString(text);
 		
 		int i = Integer.parseInt(text);
 		return new Values.Final(Integer.valueOf(i));
 	}
 
 
 
 	Value parseString(String text) {
 		StringBuilder sb = new StringBuilder();
 		
 		for(int i = 1; i != text.length() - 1; i++) {
 			if(text.charAt(i) == '\\') {
 				sb.append(text.charAt(++i));
 			} else {
 				sb.append(text.charAt(i));
 			}
 				
 		}
 
 		return new Values.Final(sb.toString());
 	}
 
 }
