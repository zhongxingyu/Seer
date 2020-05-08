 package sdl.ist.osaka_u.newmasu.Plugin.pdg;
 
 import com.sun.tools.javac.util.Pair;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.IVariableBinding;
 
 import java.util.*;
 
 public class Node {
 
     private String label = "";
     private Boolean isDummy = false;
     private String shape = "ellipse";
     public void copy(Node node){
         label = node.label;
         isDummy = node.isDummy;
         shape = node.shape;
     }
     private Boolean isDisableEdge = false;
     private ASTNode rawASTNode = null;
 
     private List<IVariableBinding> decVar = new ArrayList<>();
     private List<IVariableBinding> useVar = new ArrayList<>();
 
     private List<Node> children = new LinkedList<>();
 
     public List<Node> getChildren() {
         return children;
     }
     public Node addChildren(Node node){
         if(!children.contains(node))
             children.add(node);
         return node;
     }
 
     public String getLabel() { return label; }
     public void setLabel(String str){ label = str; }
 
     public Boolean getDummy() { return isDummy; }
     public void setDummy(Boolean dummy) { isDummy = dummy; }
 
     public String getShape() { return shape; }
     public void setShape(String shape) { this.shape = shape; }
 
     public Boolean getDisableEdge() { return isDisableEdge; }
     public void setDisableEdge(Boolean disableEdge) { isDisableEdge = disableEdge; }
 
     public ASTNode getRawASTNode() { return rawASTNode; }
     public void setRawASTNode(ASTNode rawASTNode) { this.rawASTNode = rawASTNode; }
 
     public List<IVariableBinding> getDecVar() {
         return decVar;
     }
 
     public List<IVariableBinding> getUseVar() {
         return useVar;
     }
 
     public Node(String name, Boolean dummy, String shape){
         label=name; isDummy=dummy; this.shape=shape;
     }
 
     public Node(String name, Boolean dummy, String shape, Boolean dis){
         label=name; isDummy=dummy; this.shape=shape; this.setDisableEdge(dis);
     }
 
     public Node(String name, Boolean dummy, String shape, ASTNode n){
         this(name,dummy,shape);
         addBindings(n);
         setRawASTNode(n);
     }
 
     public void addBindings(ASTNode node){
         VariableDecVisitor vis = new VariableDecVisitor();
         node.accept(vis);
         decVar.addAll(vis.decBindings);
         useVar.addAll(vis.useBindings);
     }
 
     public String toGraphDefine(){
         return toGraphId() + " [label=\"" + toGraphLabel() + "\", shape=\"" + shape + "\"];";
     }
 
     public String toGraphLabel(){
         return sanitize(label.trim());
     }
     public String toGraphId(){
         return Integer.toString(hashCode());
     }
 
     public static Set<Node> used = new LinkedHashSet<>();
     public void removeDummy(Map<Pair<Node,Node>,String> edge){
         used.clear();
         __removeDummyWorker(edge);
     }
     private void __removeDummyWorker(Map<Pair<Node,Node>,String> edge){
         if(used.contains(this))
             return;
         used.add(this);
 
         // dummy node must have 0 or 1 children
         while(!children.isEmpty()){
             // if children contains dummy
             boolean hasDummy = false;
             for(Node c : children)
                 if(c.getDummy())
                     hasDummy = true;
             if(!hasDummy)
                 break;
 
             Pair<Node,Node> del = null;
             for(Node c1 : children){
                 for(Node c2 : c1.children){
                     if(c1.getDummy()){
                         del = new Pair<>(c1,c2);
                         final Pair<Node,Node> e1 = new Pair<>(this, c1);
                         final Pair<Node,Node> e2 = new Pair<>(c1, c2);
                         final Pair<Node,Node> rep = new Pair<>(this, c2);
                         if(edge.containsKey(e1)){
                             final String s = edge.get(e1);
                             edge.remove(e1);
                             edge.put(rep,s);
                         }
                         if(edge.containsKey(e2)){
                             final String s = edge.get(e2);
                             edge.remove(e2);
                             edge.put(rep,s);
                         }
                         break;
                     }
                 }
                 if(del!=null)
                     break;
             }
 
             if(del!=null){
                 children.remove(del.fst);
                 children.add(del.snd);
             }
         }
 
         for(Node t : children)
             t.__removeDummyWorker(edge);
     }
 
     public Map<Pair<Node,Node>,String> createVarEdge(List<Node> fields, List<Node> args){
         used.clear();
         Map<Pair<Node,Node>,String> edge = new LinkedHashMap<>();
         __createVarEdgeWorker(edge);
         used.clear();
         __createFieldEdgeWorker(edge, fields);
         used.clear();
         __createFieldEdgeWorker(edge, args);
         return edge;
     }
     public static Set<Node> eused = new LinkedHashSet<>();
     private void __createVarEdgeWorker(Map<Pair<Node,Node>,String> edge){
         if(used.contains(this))
             return;
         used.add(this);
 
         // visit all node to make variable-dependency edges
         eused.clear();
         __chkEdge(edge,this);
 
         for(Node t : children)
             t.__createVarEdgeWorker(edge);
     }
     private void __chkEdge(Map<Pair<Node,Node>,String> edge, Node target){
         if(eused.contains(this))
             return;
         eused.add(this);
 
         for( IVariableBinding dec : target.getDecVar() ){
             if( this.getUseVar().contains(dec) )
                 edge.put( new Pair<>(target, this), dec.getName() );
         }
 
         for(Node t : this.children)
             t.__chkEdge(edge, target);
     }
 
     private void __createFieldEdgeWorker(Map<Pair<Node,Node>,String> edge, List<Node> fields){
         if(used.contains(this))
             return;
         used.add(this);
 
        for(Node n : fields)
             __chkFieldEdge(edge,n);
 
         for(Node t : children)
            t.__createVarEdgeWorker(edge);
     }
     private void __chkFieldEdge(Map<Pair<Node,Node>,String> edge, Node target){
         if(eused.contains(this))
             return;
         eused.add(this);
 
         for( IVariableBinding dec : target.getDecVar() ){
             if( this.getUseVar().contains(dec) )
                 edge.put( new Pair<>(target, this), dec.getName() );
         }
 
         for(Node t : this.children)
            t.__chkEdge(edge, target);
     }
 
 
     private static Set<Node> usedNodeInToGraph = new LinkedHashSet<>();
     public void print(Map<Pair<Node,Node>,String> edge){
         usedNodeInToGraph.clear();
         Writer.println(__toGraph(edge));
     }
     private String __toGraph(Map<Pair<Node,Node>,String> edge){
         if(usedNodeInToGraph.contains(this))
             return "";
         usedNodeInToGraph.add(this);
 
         StringBuilder sb = new StringBuilder();
         sb.append(toGraphDefine());
         sb.append(System.lineSeparator());
 
         for(Node c : children){
             if(!c.getDisableEdge()){
                 sb.append(toGraphId() + " -> " + c.toGraphId());
 
                 StringBuilder edgesb = new StringBuilder();
                 edgesb.append(" [label=\"");
                 Pair<Node,Node> p = new Pair<>(this,c);
                 if(edge.containsKey(p)){
                     edgesb.append(sanitize(edge.get(p)));
                 }
                 edgesb.append("\"];");
                 sb.append(edgesb.toString());
 
                 sb.append(System.lineSeparator());
             }
         }
 
         for(Node f : children)
             sb.append( f.__toGraph(edge) );
 
         return sb.toString();
     }
 
     public String sanitize(String str){
         final StringBuilder sb = new StringBuilder();
         for(char c : str.toCharArray()){
             if(c=='"')
                 sb.append('\\');
             sb.append(c);
         }
         return sb.toString();
     }
 }
