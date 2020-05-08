 package $PACKAGE_NAME$;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.imp.services.base.LPGFolderBase;
 
 import lpg.runtime.*;
 
 $EXTRA_IMPORTS$
 
 /**
  * !!!!!!!!!!!!!!!!!!!!!!
  * !!! GENERATED FILE !!!
  * !!! DO NOT EDIT    !!!
  * !!!!!!!!!!!!!!!!!!!!!!
  * 
  * This file provides an implementation of the language-dependent aspects of a
  * source-text folder for $LANG_NAME$. This implementation was generated from
  * specification file $SPEC_NAME$ at $DATE_TIME$.
  */
 public class $FOLDER_CLASS_NAME$ extends LPGFolderBase {
     private void makeFoldable(ASTNode n) {
         makeAnnotation(n.getLeftIToken(), n.getRightIToken());
     }
 
     /*
      * A visitor for ASTs.  Its purpose is to create ProjectionAnnotations
      * for regions of text corresponding to various types of AST node or to
      * text ranges computed from AST nodes.  Projection annotations appear
      * in the editor as the widgets that control folding.
      */
     private class FoldingVisitor extends AbstractVisitor {
         public void unimplementedVisitor(String s) { }
 
         $VISITOR_METHODS$
     };
 
     public void sendVisitorToAST(HashMap newAnnotations, List annotations, Object ast) {
         $AST_NODE$ theAST= ($AST_NODE$) ast;
         prsStream= theAST.getLeftIToken().getPrsStream();
         AbstractVisitor abstractVisitor= new FoldingVisitor();
 
         theAST.accept(abstractVisitor);
        makeAdjunctAnnotations();     
     }
 }
