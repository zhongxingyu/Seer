 package org.eclipse.dltk.ruby.internal.ui.templates;
 
import java.util.Collections;

import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.ASTVisitor;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.Expression;
import org.eclipse.dltk.ast.statements.Statement;
 import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ruby.internal.parser.RubySourceElementParser;
import org.eclipse.dltk.ruby.internal.ui.formatting.OldCodeFormatter;
 import org.eclipse.dltk.ui.templates.ScriptTemplateContext;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextUtilities;
 import org.eclipse.jface.text.templates.Template;
 import org.eclipse.jface.text.templates.TemplateBuffer;
 import org.eclipse.jface.text.templates.TemplateContextType;
 import org.eclipse.jface.text.templates.TemplateException;
 
 public class RubyTemplateContext extends ScriptTemplateContext {
 
 	public RubyTemplateContext(TemplateContextType type, IDocument document,
 			int completionOffset, int completionLength,
 			ISourceModule sourceModule) {
 		super(type, document, completionOffset, completionLength, sourceModule);
 	}
 
 	/*
 	private static class FormattingAstVisitor extends ASTVisitor
 	{	
 		private int indentLevel;
 		
 		public FormattingAstVisitor(){
 			indentLevel = 0;
 		}
 		
 		public boolean visit(Expression s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(Expression s)");
 			indentLevel++;
 			
 			return true;
 		}
 
 		public boolean visit(MethodDeclaration s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(MethodDeclaration s)");
 			indentLevel++;
 			
 			return true;
 		}
 
 		public boolean visit(ModuleDeclaration s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(ModuleDeclaration s)");
 			indentLevel++;
 			
 			return true;
 		}
 
 		public boolean visit(Statement s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(Statement s)");
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean visit(TypeDeclaration s) throws Exception {
 			System.out.println("FormattingAstVisitor.visit(TypeDeclaration s)");
 			
 			// TODO Auto-generated method stub
 			return true;
 		}
 
 		public boolean visitGeneral(ASTNode node) throws Exception {
 			return true;
 		}
 	
 		public boolean endvisit(Expression s) throws Exception {
 			--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(MethodDeclaration s) throws Exception {
 			--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(ModuleDeclaration s) throws Exception {
 			--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(Statement s) throws Exception {
 			--indentLevel;
 			return true;
 		}
 
 		public boolean endvisit(TypeDeclaration s) throws Exception {
 			--indentLevel;
 			return true;
 		}
 
 		public void endvisitGeneral(ASTNode node) throws Exception {
 			
 		}
 	}
 	*/
 	
 	// Just for testing
 	public TemplateBuffer test_evaluate(Template template)
 			throws BadLocationException, TemplateException {
 		if (!canEvaluate(template)) {
 			return null;
 		}
 
 		String indentTo = calculateIndent(getDocument(), getStart());
 		
 		System.out.println("Indent: |" + indentTo + "|");
 
 		String delimeter = TextUtilities.getDefaultLineDelimiter(getDocument());
 		String[] lines = template.getPattern().split("\n");
 		
 		//String p = "class XXX\ndef test\n print x\n end\n end\n";
 		//OldCodeFormatter formater = new OldCodeFormatter(Collections.EMPTY_MAP);
 		//String formatted = formater.formatString(p, 0);		
 		//System.out.println("Unformatted: |" + p + "|");
 		//System.out.println("Formatted: |" + formatted + "|");		
 		//ModuleDeclaration decl = RubySourceElementParser.parseModule(null, p.toCharArray(), null);
 		//try {
 		//decl.traverse(new FormattingAstVisitor());
 		//} catch (Exception e) {
 		// TODO Auto-generated catch block
 		//e.printStackTrace();
 		//}
 		
 		if (lines.length > 1 && indentTo != null && indentTo.length() > 0) {
 			StringBuffer buffer = new StringBuffer(lines[0]);
 
 			// Except first line
 			for (int i = 1; i < lines.length; i++) {
 				buffer.append(delimeter);
 				buffer.append(indentTo);
 				buffer.append(lines[i]);
 			}
 
 			template = new Template(template.getName(), template
 					.getDescription(), template.getContextTypeId(), buffer
 					.toString(), template.isAutoInsertable());
 		}
 
 		return super.evaluate(template);
 	}
 }
