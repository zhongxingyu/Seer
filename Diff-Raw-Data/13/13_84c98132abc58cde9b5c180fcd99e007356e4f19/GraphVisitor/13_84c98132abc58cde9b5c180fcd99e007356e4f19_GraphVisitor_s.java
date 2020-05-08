 package sdl.ist.osaka_u.newmasu.Plugin.pdg;
 
 import com.sun.tools.javac.util.Pair;
 import org.eclipse.jdt.core.dom.*;
 import sdl.ist.osaka_u.newmasu.data.BindingManager;
 
 import java.util.*;
 
 public class GraphVisitor extends ASTVisitor{
 
     public ClassTree classTrees = null;
     private MethodTree nowMethod = null;
     private Node nowNode = null;
 
     // nodes - possible to be jumped
     private Stack<Node> enter = new Stack<>();
     private Stack<Node> exit = new Stack<>();
 
     public void def(ASTNode node){
         final Node n = new Node(node.toString(),false,"ellipse", node);
         nowNode = nowNode.addChildren(n);
     }
 
     @Override
     public boolean visit(IfStatement node){
         nowNode = nowNode.addChildren(new Node("{", true, "ellipse"));
 
         final Node condNode = new Node(node.getExpression().toString(), false, "diamond", node.getExpression());
         condNode.setRawASTNode(node.getExpression());
         nowNode = nowNode.addChildren(condNode);
 
         final Node dummy = new Node("}", true, "ellipse");
 
         // create then branch
         GraphProcessor.get(node.getThenStatement(), this).process(node.getThenStatement());
         nowNode.addChildren(dummy);
         // create edge label
         final Pair<Node,Node> thenEdge = new Pair<>(condNode, condNode.getChildren().get(0));
         nowMethod.edge.put(thenEdge, "true");
 
         // create else branch
         nowNode = condNode;
         if( node.getElseStatement() != null )
             GraphProcessor.get(node.getElseStatement(), this).process(node.getElseStatement());
         nowNode = nowNode.addChildren(dummy);
         // create edge label
         if(condNode.getChildren().size()==1){  // when "then statement" is empty
             nowMethod.edge.put(thenEdge, "true/false");
         }
         else{
             final Pair<Node,Node> elseEdge = new Pair<>(condNode, condNode.getChildren().get(1));
             nowMethod.edge.put(elseEdge, "false");
         }
 
         return false;
     }
 
     @Override
     public boolean visit(SwitchStatement node){
         nowNode = nowNode.addChildren(new Node("{", true, "ellipse"));
 
         final Node condNode = new Node(node.getExpression().toString(), false, "diamond", node.getExpression());
         nowNode = nowNode.addChildren(condNode);
 
         final Node dummy = new Node("}", true, "ellipse");
 
         final List<String> caseLabel = new ArrayList<>();
         boolean isBeforeCase = false;
         for(Object tmp : node.statements()){
             final Statement s = (Statement)tmp;
             if(s instanceof SwitchCase){
                 final SwitchCase sc = (SwitchCase)s;
                 /*
                 if(sc.getExpression()!=null)
                     caseLabel.add(sc.getExpression().toString());
                 else
                     caseLabel.add("default");
 */
 //                nowNode = condNode;
                 isBeforeCase = true;
             }
             else if(s instanceof BreakStatement){
                 final BreakStatement bs = (BreakStatement)s;
                 nowNode.addChildren(dummy);
                 nowNode = condNode;
             }
             else{
                 GraphProcessor.get(s, this).process(s);
                 if(isBeforeCase){
                     condNode.addChildren(nowNode);
                     isBeforeCase = false;
                 }
             }
 
         }
 
         /*
         if(caseLabel.size()==0)
             nowNode = nowNode.addChildren(dummy);  // condNode.add
         else{
             for(int i=0; i<caseLabel.size(); i++){
                 // create edge label
                 final Pair<Node,Node> thenEdge = new Pair<>(condNode, condNode.getChildren().get(i));
                 edge.put(thenEdge, caseLabel.get(i));
             }
         }
         */
         nowNode = dummy;
 
         return false;
     }
 
     @Override
     public boolean visit(ForStatement node){
 
         nowNode =  nowNode.addChildren(new Node("{", true, "ellipse"));
         for( Object o : node.initializers() ){
             Expression exp = (Expression)o;
             nowNode = nowNode.addChildren(new Node(exp.toString(), false, "ellipse", exp));
         }
 
        final Node condNode = new Node(node.getExpression().toString(), false, "diamond", node.getExpression());
         nowNode = nowNode.addChildren(condNode);
 
         final Node dummy = new Node("}", true, "ellipse");
 
         // set enter & exit node
         enter.push(condNode);
         exit.push(dummy);
 
         // create then branch
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
         for( Object o : node.initializers() ){
             Expression exp = (Expression)o;
             nowNode = nowNode.addChildren(new Node(exp.toString(), false, "ellipse", exp));
         }
         nowNode = nowNode.addChildren(condNode);
         // create edge label
         final Pair<Node,Node> thenEdge = new Pair<>(
                 nowNode, nowNode.getChildren().get(0));
        nowMethod.edge.put(thenEdge, "true");
 
         // create else branch
         condNode.addChildren(dummy);
         nowNode = dummy;
         // create edge label
         final Pair<Node,Node> elseEdge = new Pair<>(condNode, dummy);
         nowMethod.edge.put(elseEdge, "false");
 
         // pop enter & exit node
         enter.pop();
         exit.pop();
 
         return false;
     }
     @Override
     public boolean visit(WhileStatement node){
         nowNode =  nowNode.addChildren(new Node("{", true, "ellipse"));
         final Node condNode = new Node(node.getExpression().toString(), false, "diamond", node.getExpression());
         nowNode = nowNode.addChildren(condNode);
         final Node dummy = new Node("}", true, "ellipse");
 
         // set enter & exit node
         enter.push(condNode);
         exit.push(dummy);
 
         // create then branch
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
         nowNode = nowNode.addChildren(condNode);
         // create edge label
         final Pair<Node,Node> thenEdge = new Pair<>(nowNode, nowNode.getChildren().get(0));
         nowMethod.edge.put(thenEdge, "true");
 
         // create else branch
         condNode.addChildren(dummy);
         nowNode = dummy;
         // create edge label
         final Pair<Node,Node> elseEdge = new Pair<>(condNode, dummy);
         nowMethod.edge.put(elseEdge, "false");
 
         // pop enter & exit node
         enter.pop();
         exit.pop();
 
         return false;
     }
     @Override
     public boolean visit(DoStatement node){
         final Node doStart = new Node("do {", true, "ellipse");
         nowNode = nowNode.addChildren(doStart);
         final Node dummy = new Node("}", true, "ellipse");
 
         // set enter & exit node
         enter.push(doStart);
         exit.push(dummy);
 
         // create then branch
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
 
         final Node condNode = new Node(node.getExpression().toString(), false, "diamond", node.getExpression());
         nowNode = nowNode.addChildren(condNode);
         nowNode = nowNode.addChildren(doStart);
         // create edge label
         final Pair<Node,Node> thenEdge = new Pair<>(condNode,doStart);
         nowMethod.edge.put(thenEdge, "true");
 
         // create else branch
         condNode.addChildren(dummy);
         nowNode = dummy;
         // create edge label
         final Pair<Node,Node> elseEdge = new Pair<>(condNode, dummy);
         nowMethod.edge.put(elseEdge, "false");
 
         // pop enter & exit node
         enter.pop();
         exit.pop();
 
         return false;
     }
     @Override
     public boolean visit(EnhancedForStatement node){
         nowNode =  nowNode.addChildren(new Node("{", true, "ellipse"));
         final Node condNode = new Node(
                 node.getParameter().toString() + " <- " + node.getExpression().toString(),
                 false, "diamond");
         condNode.addBindings( node.getParameter() );
         condNode.addBindings( node.getExpression() );
 
         nowNode = nowNode.addChildren(condNode);
         final Node dummy = new Node("}", true, "ellipse");
 
         // set enter & exit node
         enter.push(condNode);
         exit.push(dummy);
 
         // create then branch
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
         nowNode = nowNode.addChildren(condNode);
         // create edge label
         final Pair<Node,Node> thenEdge = new Pair<>(nowNode, nowNode.getChildren().get(0));
         nowMethod.edge.put(thenEdge, "true");
 
         // create else branch
         condNode.addChildren(dummy);
         nowNode = dummy;
         // create edge label
         final Pair<Node,Node> elseEdge = new Pair<>(condNode, dummy);
         nowMethod.edge.put(elseEdge, "false");
 
         // pop enter & exit node
         enter.pop();
         exit.pop();
 
         return false;
     }
 
     @Override
     public boolean visit(TryStatement node){
         final Node prev = nowNode;
         nowNode =  nowNode.addChildren(new Node("{", true, "ellipse"));
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
         final Node dummy = new Node("}", true, "ellipse");
         nowNode = nowNode.addChildren(dummy);
 
         for( Object o: node.catchClauses() ){
             final CatchClause clause = (CatchClause)o;
             final Node cond = new Node(clause.getException().toString(), false, "diamond", true);
 
             nowNode = prev.addChildren(cond);
             nowNode = nowNode.addChildren(new Node("{", true, "ellipse"));
             GraphProcessor.get(clause.getBody(), this).process(clause.getBody());
             nowNode = nowNode.addChildren(new Node("}", true, "ellipse"));
 
             // create edge label
             final Pair<Node,Node> trythenEdge = new Pair<>(cond, cond.getChildren().get(0));
             nowMethod.edge.put(trythenEdge, "try-true");
 
             nowNode = nowNode.addChildren(dummy);
         }
 
         if( node.getFinally() != null){
             // now, nowNode is dummy
             nowNode =  nowNode.addChildren(new Node("{", true, "ellipse"));
 
 //            // create edge label
 //            final Pair<Node,Node> edge = new Pair<>(dummy, nowNode);
 //            nowMethod.edge.put(edge, "finally");
 
             GraphProcessor.get(node.getFinally(), this).process(node.getFinally());
             nowNode = nowNode.addChildren(new Node("}", true, "ellipse"));
         }
 
         return false;
     }
 
 
     @Override
     public boolean visit(BreakStatement node){
         nowNode = nowNode.addChildren(new Node(node.toString(), false, "triangle", node));
         nowNode.addChildren(exit.peek());
         return false;
     }
     @Override
     public boolean visit(ReturnStatement node){
         nowNode = nowNode.addChildren(new Node(node.toString(), false, "triangle", node));
         nowNode.addChildren(exit.get(0));
         return false;
     }
     @Override
     public boolean visit(ContinueStatement node){
         nowNode = nowNode.addChildren(new Node(node.toString(), false, "triangle", node));
         nowNode.addChildren(enter.peek());
         return false;
     }
 
     @Override
     public boolean visit(Block node){
         List<Statement> list = node.statements();
         for( Statement s : list)
             GraphProcessor.get(s, this).process(s);
         return false;
     }
 
     @Override
     public boolean visit(FieldDeclaration node){
         classTrees.fields.add(new Node(node.toString(), false, "octagon", node));
         return false;
     }
 
     @Override
     public boolean visit(SynchronizedStatement node){   // ignore
         GraphProcessor.get(node.getBody(), this).process(node.getBody());
         return false;
     }
 
     @Override public boolean visit(CompilationUnit node){
         classTrees = new ClassTree();
         classTrees.rawNode = node;
         Writer.newFile(BindingManager.getRel().getCalleeMap().get(node).getFileName().toString());
         return true;
     }
 
     @Override public void endVisit(CompilationUnit node){
         classTrees.outputGraph();
     }
 
     @Override
     public boolean visit(MethodDeclaration node){
         nowMethod = new MethodTree();
         nowMethod.rawNode = node;
         classTrees.methods.add(nowMethod);
 
         final StringBuilder sb = new StringBuilder();
         if(!node.isConstructor())
             sb.append(node.getReturnType2().toString() + " ");
         sb.append(node.getName() + "(");
         for(Object t : node.parameters()){
             final SingleVariableDeclaration s = (SingleVariableDeclaration)t;
             sb.append(" " + s.getType().toString() + " " + s.getName());
             nowMethod.args.add(new Node(s.toString(), false, "trapezium", s));
         }
         sb.append(")");
 
         final Node start = new Node("Start " + sb.toString(), false, "rect");
         nowMethod.root = start;
         nowNode = nowMethod.root;
 
         enter.push(start);
         exit.push(new Node("End " + sb.toString(), false, "rect"));
 
         return true;
     }
     @Override
     public void endVisit(MethodDeclaration node){
         nowNode.addChildren(exit.pop());
         nowMethod.root = enter.pop();
 
         nowMethod.root.used.clear();
         nowMethod.root.removeDummy(nowMethod.edge);
 
         assert enter.isEmpty();
         assert exit.isEmpty();
     }
 }
