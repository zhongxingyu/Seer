 package jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.antlr;
 
 
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.Stack;
 
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.databuilder.ASTParseException;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstToken;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.token.AstTokenTranslator;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitEvent;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitListener;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitStrategy;
 import jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitor;
 import jp.ac.osaka_u.ist.sel.metricstool.main.parse.CommonASTWithLineNumber;
 import antlr.collections.AST;
 
 
 /**
  * antlrASTK₷ {@link AstVisitor}.
  * 
  * 
  * @author kou-tngt
  *
  */
 public class AntlrAstVisitor implements AstVisitor<AST> {
 
     /**
      * translatorŎw肳ꂽ {@link AstTokenTranslator} ƃftHg {@link AstVisitStrategy}
      * ݒ肷RXgN^.
      * ̃RXgN^琶ꂽftHgAstVisitStrategy̓NX⃁\bh̃m[hK₷悤ɃrW^[U.
      * 
      * @param translator@̃rW^[gpASTm[h̖|@
      */
     public AntlrAstVisitor(final AstTokenTranslator<AST> translator) {
         this(translator, true, true);
     }
 
     /**
      * translatorŎw肳ꂽ {@link AstTokenTranslator} ƃftHg {@link AstVisitStrategy}
      * ݒ肷RXgN^.
      * 
      * NX⃁\bh̃m[hK₷邩ǂintoClassintoMethodŎw肷.
      * 
      * @param translator@̃rW^[gpASTm[h̖|@
      * @param intoClass NX\AST̓K₷邩ǂw肷.K₷ꍇtrue.
      * @param intoMethod@\bh\AST̓K₷邩ǂw肷.K₷ꍇtrue.
      */
     public AntlrAstVisitor(final AstTokenTranslator<AST> translator, final boolean intoClass,
             final boolean intoMethod) {
         this(translator, new AntlrAstVisitStrategy(intoClass, intoMethod));
     }
 
     /**
      * Ŏw肳ꂽ {@link AstTokenTranslator}  {@link AstVisitStrategy}
      * ݒ肷RXgN^.
      * 
      * @param translator@̃rW^[gpASTm[h̖|@
      * @param strategy@̃rW^[̖KUAstVisitStrategyCX^X
      */
     public AntlrAstVisitor(final AstTokenTranslator<AST> translator,
             final AstVisitStrategy<AST> strategy) {
         if (null == translator) {
             throw new NullPointerException("translator is null.");
         }
         if (null == strategy) {
             throw new NullPointerException("starategy is null.");
         }
 
         this.visitStrategy = strategy;
         this.translator = translator;
     }
 
     /**
      * ̃rW^[se {@link AstVisitEvent} ̒ʒm󂯂郊Xio^.
      * 
      * @param listener o^郊Xi
      * @throws NullPointerException listenernull̏ꍇ
      */
     public void addVisitListener(final AstVisitListener listener) {
         if (null == listener) {
             throw new NullPointerException("listener is null.");
         }
 
         this.listeners.add(listener);
     }
 
     /**
      * ̃rW^[se {@link AstVisitEvent} ̒ʒm󂯂郊Xi폜.
      * 
      * @param listener@폜郊Xi
      * @throws NullPointerException listenernull̏ꍇ
      */
     public void removeVisitListener(final AstVisitListener listener) {
         this.listeners.remove(listener);
     }
 
     /**
      * ̃rW^[̏ԂԂɖ߂.
      * CxgXi͍폜Ȃ.
      */
     public void reset() {
         this.eventStack.clear();
         this.nodeStack.clear();
     }
 
     private void printAST(AST node, int nest){
         CommonASTWithLineNumber nextNode = (CommonASTWithLineNumber) node;
         while(null != nextNode){
             CommonASTWithLineNumber currentNode = nextNode;
             nextNode = (CommonASTWithLineNumber) nextNode.getNextSibling();
             AstToken token = this.translator.translate(currentNode);
             for(int i = 0; i < nest; i++){
                 System.out.print("  ");
             }
             System.out.println(token.toString() + " (" + currentNode.getText() + ")" + " : " + "[" + currentNode.getFromLine() + ", " + currentNode.getFromColumn() + "]" + "[" + currentNode.getToLine() + ", " + currentNode.getToColumn() + "]");
             printAST(currentNode.getFirstChild(), nest + 1);
         }
     }
     
     /* (non-Javadoc)
      * @see jp.ac.osaka_u.ist.sel.metricstool.main.ast.visitor.AstVisitor#startVisiting(java.lang.Object)
      */
     public void startVisiting(final AST startNode) throws ASTParseException {
         AST nextNode = startNode;
        // printAST(startNode, 0);
         AstToken parentToken = null;
         while (null != nextNode) {
             //̃m[h̃g[NAstTokenɕϊ
             final AstToken token = this.translator.translate(nextNode);
 
             //ʒu񂪗płȂ擾.
             int startLine = 0;
             int startColumn = 0;
             int endLine = 0;
             int endColumn = 0;
             if (nextNode instanceof CommonASTWithLineNumber) {
                 CommonASTWithLineNumber node = (CommonASTWithLineNumber) nextNode;
                 startLine = node.getFromLine();
                 startColumn = node.getFromColumn();
                 endLine = node.getToLine();
                 endColumn = node.getToColumn();
             }
             
             //KCxg쐬
             final AstVisitEvent event = new AstVisitEvent(this, token, nextNode.getText(), parentToken, startLine, startColumn,
                     endLine, endColumn);
 
             this.fireVisitEvent(event);
 
             if (this.visitStrategy.needToVisitChildren(nextNode, event.getToken())) {
                 //qm[hK₷ꍇ
 
                 this.fireEnterEvent(event);
                 this.eventStack.push(event);
                 this.nodeStack.push(nextNode);
                 nextNode = nextNode.getFirstChild();
                 
                 //qm[hK₷̂ŁC݂̃m[hem[hɂȂ
                 parentToken = token;
 
             } else {
                 //̌Zɐiޏꍇ
                 nextNode = nextNode.getNextSibling();
             }
 
             if (null == nextNode) {
                 //̍s悪Ȃ
 
                 AstVisitEvent exitedEvent = null;
                 
                 //܂X^bNkĂ܂HĂȂZT
                 while (!this.nodeStack.isEmpty()
                         && null == (nextNode = this.nodeStack.pop().getNextSibling())) {
                     exitedEvent = this.eventStack.pop();
                     this.fireExitEvent(exitedEvent);
                 }
 
                 if (!this.eventStack.isEmpty()) {
                     exitedEvent = this.eventStack.pop();
                     this.fireExitEvent(exitedEvent);
                 }
                 
                 if(null != exitedEvent) {
                     parentToken = exitedEvent.getParentToken();
                 }
             }
         }
     }
 
     /**
      * ݂̃m[h̓ɓCxg𔭍s
      * @param event@sCxg
      */
     private void fireEnterEvent(final AstVisitEvent event) {
         for (final AstVisitListener listener : this.listeners) {
             listener.entered(event);
         }
     }
 
     /**
      * ݂̃m[h̓oCxg𔭍s
      * @param event@sCxg
      */
     private void fireExitEvent(final AstVisitEvent event) throws ASTParseException {
         for (final AstVisitListener listener : this.listeners) {
             listener.exited(event);
         }
     }
 
     /**
      * m[hɖK₷Cxg𔭍s
      * @param event@sCxg
      */
     private void fireVisitEvent(final AstVisitEvent event) {
         for (final AstVisitListener listener : this.listeners) {
             listener.visited(event);
         }
     }
 
     /**
      * ̃rW^[̖KU.
      */
     private final AstVisitStrategy<AST> visitStrategy;
 
     /**
      * K₵ASTm[hAstTokenɕϊ
      */
     private final AstTokenTranslator<AST> translator;
 
     /**
      * CxgǗX^bN
      */
     private final Stack<AstVisitEvent> eventStack = new Stack<AstVisitEvent>();
 
     /**
      * m[hǗX^bN
      */
     private final Stack<AST> nodeStack = new Stack<AST>();
 
     /**
      * Cxgʒm󂯎郊Xi[̃Zbg
      */
     private final Set<AstVisitListener> listeners = new LinkedHashSet<AstVisitListener>();
 
 }
