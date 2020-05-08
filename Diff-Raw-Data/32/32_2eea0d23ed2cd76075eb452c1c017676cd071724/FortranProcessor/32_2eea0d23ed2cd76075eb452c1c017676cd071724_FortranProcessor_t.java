 package codemate.Fortran;
 
 /**
  * FortranProcessor
  *
  * This program is used to process Fortran codes with augmented template
  * mechanism. By using visitor model, the nodes in the parse tree will be
  * visited, during which the code will be rewrited and the templates will be
  * expanded.
  *
  * @author      Li Dong <dongli@lasg.iap.ac.cn>
  */
 
 import org.antlr.v4.runtime.*;
 import org.antlr.v4.runtime.tree.*;
 
 import codemate.project.*;
 import codemate.ui.*;
 
 import java.io.*;
 
 public class FortranProcessor {
     /**
      * process
      * 
      * This method processes the given project by calling 'callParser',
      * 'callTemplater' and 'callRewriter'.
      * 
      * @param		project			The project to be processed
      * 
      * @author 		Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static void process(Project project) {
     	// first read template definitions under the root of project
     	FortranTemplater.readTemplates(project.getRoot());
     	for (CodeEntity entity : project.entities) {
    		UI.notice("FortranProcessor", "Process code "+entity.getPath()+".");
     		try {
     			callParser(entity);
     		} catch (Exception e) {
     			UI.error("FortranProcessor",
     					"Encounter error while parsing "+entity.getPath()+"!");
     		}
 			if (callTemplater(entity)) {
 				callRewriter(entity, project);
 			}
 			callModuleDepend(entity, project);
     	}
     }
     
     /**
      * process
      * 
      * This method only processes a single Fortran file.
      * 
      * @param file
      * 
      * @author Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static void process(File file) {
     	FortranTemplater.readTemplates(file.getParentFile());
     	CodeEntity entity = new CodeEntity(file.getPath());
 		try {
 			callParser(entity);
 		} catch (Exception e) {
 			UI.error("FortranProcessor",
 					"Encounter error while parsing "+entity.getPath()+"!");
 		}
 		callTemplater(entity);
     	callRewriter(entity);
     }
 
     /**
      * callParser
      *
      * This method parses the given input stream from root 'file' rule.
      *
      * @param       entity     		The code entity to be parsed
      *
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static void callParser(CodeEntity entity) throws Exception {
     	InputStream inputStream = new FileInputStream(entity.getPath());
         ANTLRInputStream input = new ANTLRInputStream(inputStream);
         FortranLexer lexer = new FortranLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         FortranParser parser = new FortranParser(tokens);
         entity.setParseTree(parser.file());
     }
 
     /**
      * callParser
      *
      * This method parses pieces of Fortran code. It is called by
      * FortranTemplater.
      *
      * @param       type        	The starting parser rule.
      * @param       source          A input string for parsing.
      * @return                      The parse tree.
      *
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static ParseTree callParser(
     		int ruleIndex, String source) throws Exception {
         ANTLRInputStream input = new ANTLRInputStream(source);
         FortranLexer lexer = new FortranLexer(input);
         CommonTokenStream tokens = new CommonTokenStream(lexer);
         FortranParser parser = new FortranParser(tokens);
         ParseTree tree = null;
         // NOTE: We set the rules that can be used.
         if (ruleIndex == FortranParser.RULE_declarationStatements) {
             tree = parser.declarationStatements();
         } else if (ruleIndex == FortranParser.RULE_executableStatements) {
             tree = parser.executableStatements();
         } else if (ruleIndex == FortranParser.RULE_typeDeclarationStatement) {
         	tree = parser.typeDeclarationStatement();
         } else if (ruleIndex == FortranParser.RULE_containedProcedures) {
         	tree = parser.containedProcedures();
         } else if (ruleIndex == FortranParser.RULE_derivedTypeName) {
         	tree = parser.derivedTypeName();
         }
         return tree;
     }
 
     /**
      * callTemplater 
      *
      * This method calls FortranTemplater class to process the template
      * instances in the Fortran codes.
      * 
      * @param       entity      The code entity to be processed for template
      * @return      boolean     Return true if encounter template instance.
      *
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static boolean callTemplater(CodeEntity entity) {
     	FortranTemplater templater = new FortranTemplater();
         return templater.instantiate(entity.getParseTree());
     }
 
     /**
      * callRewriter 
      *
      * This method calls FortranRewriter class to rewrite the Fortran codes.
      *
      * @param      entity    The code entity to be rewritten
      * 
      * @author      Li Dong <dongli@lasg.iap.ac.cn>
      */
     public static void callRewriter(CodeEntity entity) {
         FortranRewriter rewriter = new FortranRewriter();
         rewriter.rewrite(entity.getParseTree());
         // write new code into error channel, then user can redirect it
         System.err.println(rewriter.getNewCode());
     }
     
     /**
      * callRewriter
      * 
      * This method rewrites the given entity in a project to a new location that
      * is <project root>/.codemate/processed_codes.
      * 
      * @param      entity    The code entity to be rewritten
      * @param      project   The project
      * 
      * @author Li Dong <dongli@lasg.iap.ac.cn>
      */
     private static void callRewriter(CodeEntity entity, Project project) {
     	File dir = new File(project.getRoot()+"/.codemate/processed_codes");
     	if (!dir.isDirectory())
     		if (!dir.mkdirs())
     			UI.error("FortranProcessor",
     					"Couldn't create directory "+dir.getPath()+
     					" for storing processed codes!");
         FortranRewriter rewriter = new FortranRewriter();
         rewriter.rewrite(entity.getParseTree());
     	String fileName = dir.getPath()+"/"+entity.getName()+".t.F90";
     	UI.notice("FortranProcessor",
     			"Processed code is written to "+fileName+".");
     	try {
     		PrintWriter out = new PrintWriter(new FileWriter(fileName));
     		out.print(rewriter.getNewCode());
     		out.close();
     	} catch (Exception e) {
     		e.printStackTrace();
     	}
     }
     
     public static void callModuleDepend(CodeEntity entity, Project project) {
     	FortranDepend moduleDepend = new FortranDepend();
     	moduleDepend.extract(entity, project);
     }
 }
