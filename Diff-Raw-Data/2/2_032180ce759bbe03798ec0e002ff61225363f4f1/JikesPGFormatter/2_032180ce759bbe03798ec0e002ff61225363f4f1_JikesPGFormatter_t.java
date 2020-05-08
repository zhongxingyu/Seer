 /*
  * Created on Mar 24, 2006
  */
 package org.jikespg.uide.editor;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import lpg.lpgjavaruntime.IToken;
 
 import org.eclipse.uide.core.ILanguageService;
 import org.eclipse.uide.editor.ISourceFormatter;
 import org.eclipse.uide.parser.IParseController;
 import org.jikespg.uide.parser.JikesPGParser;
 import org.jikespg.uide.parser.JikesPGParser.*;
 
 public class JikesPGFormatter implements ILanguageService, ISourceFormatter {
     private int fIndentSize= 6;
     private String fIndentString;
 
     private boolean fIndentProducesToWidestNonTerm= false;
 
     public void formatterStarts(String initialIndentation) {
         // Should pick up preferences here
         fIndentSize= 4;
         StringBuffer buff= new StringBuffer(fIndentSize);
         for(int i=0; i < fIndentSize; i++)
             buff.append(' ');
         fIndentString= buff.toString();
         fIndentProducesToWidestNonTerm= false;
     }
 
     public String format(IParseController parseController, String content, boolean isLineStart, String indentation, int[] positions) {
         final StringBuffer buff= new StringBuffer();
         final Set adjunctTokens= new HashSet();
         JikesPG root= (JikesPG) parseController.getCurrentAst();
 
         root.accept(new JikesPGParser.AbstractVisitor() {
             private int prodCount;
             private int prodIndent;
             public void unimplementedVisitor(String s) {
                 System.out.println("Unhandled node type: " + s);
             }
             public void preVisit(ASTNode n) {
                 IToken left= n.getLeftIToken();
                IToken[] adjuncts= left.getPrsStream().getPrecedingAdjuncts(left.getTokenIndex());
                 for(int i= 0; i < adjuncts.length; i++) {
                     IToken adjunct= adjuncts[i];
                     if (!adjunctTokens.contains(adjunct)) {
                         buff.append(adjunct);
                         buff.append('\n');
                     }
                     adjunctTokens.add(adjunct);
                 }
             }
             public void postVisit(ASTNode n) {
                 IToken right= n.getRightIToken();
                 IToken[] adjuncts= right.getPrsStream().getFollowingAdjuncts(right.getTokenIndex());
                 for(int i= 0; i < adjuncts.length; i++) {
                     IToken adjunct= adjuncts[i];
                     if (!adjunctTokens.contains(adjunct)) {
                         buff.append(adjunct);
                         buff.append('\n');
                     }
                     adjunctTokens.add(adjunct);
                 }
             }
             public boolean visit(option_spec n) {
                 buff.append("%options ");
                 return true;
             }
             public void endVisit(option_list n) {
                 if (n.getoption_list() != null)
                     buff.append(',');
             }
             public void endVisit(option_spec n) {
                 buff.append('\n');
             }
             public boolean visit(option n) {
                 buff.append(n.getSYMBOL());
                 return true;
             }
             public boolean visit(option_value0 n) {
                 buff.append("=" + n.getSYMBOL());
                 return false;
             }
             public boolean visit(option_value1 n) {
                 buff.append('(');
                 SYMBOLList symList= n.getsymbol_list();
                 for(int i=0; i < symList.size(); i++) {
                     if (i > 0) buff.append(',');
                     buff.append(symList.getSYMBOLAt(i));
                 }
                 buff.append(')');
                 return false;
             }
             public boolean visit(NoticeSeg n) {
                 buff.append("$Notice\n");
                 return true;
             }
             public void endVisit(NoticeSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(GlobalsSeg n) {
                 buff.append("$Globals\n");
                 return true;
             }
             public void endVisit(GlobalsSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(globals_segment1 n) {
                 buff.append(fIndentString);
                 return true;
             }
             public boolean visit(HeadersSeg n) {
                 buff.append("$Headers\n");
                 return true;
             }
             public void endVisit(HeadersSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(IdentifierSeg n) {
                 buff.append("$Identifier\n");
                 return true;
             }
             public void endVisit(IdentifierSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(EofSeg n) {
                 buff.append("$EOF\n");
                 return true;
             }
             public void endVisit(EofSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(terminal_symbol0 n) {
                 buff.append(fIndentString);
                 buff.append(n.getSYMBOL());
                 buff.append('\n');
                 return false;
             }
             public boolean visit(DefineSeg n) {
                 buff.append("$Define\n");
                 return true;
             }
             public void endVisit(DefineSeg n) {
                 buff.append("$End\n\n");
             }
             public void endVisit(define_segment1 n) {
                 buff.append(fIndentString);
                 buff.append(n.getmacro_name_symbol());
                 buff.append(' ');
                 buff.append(n.getmacro_segment());
                 buff.append('\n');
             }
             public boolean visit(TerminalsSeg n) {
                 buff.append("$Terminals\n");
                 return true;
             }
             public void endVisit(TerminalsSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(terminal n) {
                 buff.append(fIndentString + n.getterminal_symbol());
                 if (n.getoptTerminalAlias() != null)
                     buff.append(" ::= " + n.getoptTerminalAlias().getname());
                 buff.append('\n');
                 return false;
             }
             public boolean visit(StartSeg n) {
                 buff.append("$Start\n");
                 return true;
             }
             public void endVisit(StartSeg n) {
                 buff.append("$End\n\n");
             }
             public boolean visit(start_symbol0 n) {
                 buff.append(fIndentString);
                 buff.append(n.getSYMBOL());
                 buff.append('\n');
                 return false;
             }
             public boolean visit(start_symbol1 n) {
                 buff.append(n.getMACRO_NAME());
                 return false;
             }
             public boolean visit(RulesSeg n) {
                 buff.append("$Rules\n");
                 if (fIndentProducesToWidestNonTerm) {
                     rules_segment rulesSegment= n.getrules_segment();
                     nonTermList nonTermList= rulesSegment.getnonTermList();
                     int maxLHSSymWid= 0;
                     for(int i=0; i < nonTermList.size(); i++) {
                         int lhsSymWid= nonTermList.getElementAt(i).getLeftIToken().toString().length();
                         if (lhsSymWid > maxLHSSymWid) maxLHSSymWid= lhsSymWid;
                     }
                     prodIndent= fIndentSize + maxLHSSymWid + 1;
                 }
                 return true;
             }
             public void endVisit(RulesSeg n) {
                 buff.append("$End\n");
             }
             public boolean visit(nonTerm n) {
                 buff.append(fIndentString);
                 buff.append(n.getSYMBOL());
                 if (n.getclassName() != null)
                     buff.append(n.getclassName());
                 if (n.getarrayElement() != null)
                     buff.append(n.getarrayElement());
                 if (fIndentProducesToWidestNonTerm) {
                     for(int i=n.getSYMBOL().toString().length() + fIndentSize + 1; i <= prodIndent; i++)
                         buff.append(' ');
                 } else
                     buff.append(' ');
                 buff.append(n.getproduces());
                 prodCount= 0;
                 if (!fIndentProducesToWidestNonTerm)
                     prodIndent= fIndentSize + n.getSYMBOL().toString().length() + 1;
                 return true;
             }
             public void endVisit(nonTerm n) {
                 buff.append('\n');
             }
             public boolean visit(rhs n) {
                 if (prodCount > 0) {
                     buff.append('\n');
                     for(int i=0; i < prodIndent; i++)
                         buff.append(' ');
                     buff.append("|  ");
                 }
                 prodCount++;
                 return true;
             }
             public boolean visit(action_segment n) {
                 buff.append(n.getBLOCK());
                 buff.append('\n');
                 return false;
             }
             public boolean visit(symWithAttrs0 n) {
                 buff.append(' ');
                 buff.append(n.getEMPTY_KEY());
                 return false;
             }
             public boolean visit(symWithAttrs1 n) {
                 buff.append(' ');
                 buff.append(n.getSYMBOL());
                 return false;
             }
             public boolean visit(symWithAttrs2 n) {
                 buff.append(' ');
                 buff.append(n.getSYMBOL());
                 buff.append(n.getMACRO_NAME());
                 return false;
             }
         });
 
 	return buff.toString();
     }
 
     public void formatterStops() {
 	// TODO Auto-generated method stub
     }
 }
