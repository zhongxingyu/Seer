 package org.jikespg.uide.parser;
 
 import org.eclipse.uide.parser.IASTNodeLocator;
 import org.jikespg.uide.parser.JikesPGParser.ASTNode;
 import org.jikespg.uide.parser.JikesPGParser.ASTNodeToken;
 import org.jikespg.uide.parser.JikesPGParser.JikesPG;
 import org.jikespg.uide.parser.JikesPGParser.nonTerm;
 import org.jikespg.uide.parser.JikesPGParser.symWithAttrs1;
 import org.jikespg.uide.parser.JikesPGParser.terminal_symbol1;
 import lpg.lpgjavaruntime.IToken;
 import lpg.lpgjavaruntime.PrsStream;
 
 public class NodeLocator implements IASTNodeLocator {
     private final ParseController fController;
     private ASTNode fResult= null;
 
     public NodeLocator(ParseController controller) {
         fController= controller;
     }
 
     public Object findNode(Object ast, int offset) {
         ASTNode root= (ASTNode) ast;
 
         root.accept(new LocatingVisitor(offset));
         return fResult;
     }
 
     public Object findNode(Object ast, int startOffset, int endOffset) {
         ASTNode root= (ASTNode) ast;
 
         root.accept(new LocatingVisitor(startOffset, endOffset));
         return fResult;
     }
 
     private class LocatingVisitor extends JikesPGParser.AbstractVisitor {
         private final int fStartOffset;
         private final int fEndOffset;
         private final PrsStream fParseStream= fController.getParser().getParseStream();
 
         public LocatingVisitor(int offset) {
            this(offset, offset+1);
         }
 
         public LocatingVisitor(int startOffset, int endOffset) {
             fStartOffset= startOffset;
             fEndOffset= endOffset;
         }
 
         public ASTNode getResult() { return fResult; }
 
         public void unimplementedVisitor(String s) {
 //            System.out.println(s);
         }
 
         public void endVisit(JikesPG n) {
 //            if (n.getJikesPG_INPUT() != null)
 //                return n.getJikesPG_INPUT().accept(this);
         }
 
 //        public void endVisit(JikesPG_INPUT n) {
 //            Object o= null;
 //            if (n.getJikesPG_INPUT() != null)
 //                o= n.getJikesPG_INPUT().accept(this);
 //            if (o == null)
 //                o= n.getJikesPG_item().accept(this);
 //            return o;
 //        }
 
 //        public Object visit(HeadersSeg n) {
 //            return n.getheaders_segment().accept(this);
 //        }
 
 //        public Object visit(headers_segment0 n) {
 //            return null; // just the HEADERS_KEY
 //        }
 
 //        public Object visit(headers_segment1 n) {
 //            Object o= n.getheaders_segment().accept(this);
 //            if (o == null)
 //                o= n.getaction_segment().accept(this);
 //            return o;
 //        }
 
 //        public Object visit(TerminalsSeg n) {
 //            return n.getterminals_segment().accept(this);
 //        }
 
 //        public Object visit(terminals_segment1 n) {
 //            return null; // this is just for the TERMINALS_KEY
 //        }
 
 //        public Object visit(terminals_segment2 n) {
 //            Object o= n.getterminals_segment().accept(this);
 //            if (o == null)
 //                o= n.getterminal_symbol().accept(this);
 //            return o;
 //        }
 
         public void endVisit(terminal_symbol1 n) {
             IToken symTok= n.getLeftIToken();
             if (fStartOffset >= symTok.getStartOffset() && fEndOffset <= symTok.getEndOffset())
                 fResult= n;
         }
 
 //        public Object visit(RulesSeg n) {
 //            return n.getrules_segment().accept(this);
 //        }
 
 //        public Object visit(rules_segment n) {
 //            Object ret= null;
 //            if (n.getaction_segment_list() != null) {
 //                ret= n.getaction_segment_list().accept(this);
 //            }
 //            if (ret == null)
 //                ret= n.getnonTermList().accept(this);
 //            return ret;
 //        }
 //        public Object visit(nonTermList n) {
 //            for(int i= 0; i < n.size(); i++) {
 //                Object ret= n.getnonTermAt(i).accept(this);
 //                if (ret != null)
 //                    return ret;
 //            }
 //            return null;
 //        }
         public void endVisit(nonTerm n) {
             IToken tok= n.getSYMBOL().getIToken();
             if (fStartOffset >= tok.getStartOffset() && fEndOffset <= tok.getEndOffset())
         	fResult= n;
         }
 //        public Object visit(rhsSymbol n) {
 //            Object o= null;
 //            if (n.getrhs() != null)
 //                o= n.getrhs().accept(this);
 //            if (o == null)
 //                o= n.getSYMBOL().accept(this);
 //            return o;
 //        }
 //        public Object visit(rhsSymbolMacro n) {
 //            Object o= n.getSYMBOL().accept(this);
 //            if (o == null)
 //                o= n.getMACRO_NAME().accept(this);
 //            if (o == null)
 //                o= n.getrhs().accept(this);
 //            return o;
 //        }
         public void endVisit(symWithAttrs1 n) {
             IToken sym= n.getSYMBOL();
             if (fStartOffset >= sym.getStartOffset() && fEndOffset <= sym.getEndOffset()+1)
         	fResult= n;
         }
         public void endVisit(ASTNodeToken n) {
             IToken lt= n.getLeftIToken();
             IToken rt= n.getRightIToken();
 
             if (fStartOffset >= lt.getStartOffset() && fEndOffset <= rt.getEndOffset()+1)
         	fResult= n;
         }
     }
 }
