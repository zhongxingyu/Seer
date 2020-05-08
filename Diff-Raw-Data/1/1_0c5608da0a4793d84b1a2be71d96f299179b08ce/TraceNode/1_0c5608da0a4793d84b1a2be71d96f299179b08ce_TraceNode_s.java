 package chum.gl.render;
 
 import chum.engine.GameNode;
 import chum.engine.common.HookNode;
 import chum.gl.RenderContext;
 import chum.gl.RenderNode;
 import chum.util.gl.TraceGL10;
 import chum.util.gl.TraceGL11;
 
 
 /**
    A node to trace opengl calls generated on its subtree
 */
 public class TraceNode extends HookNode {
 
     /** The tracer */
     public TraceGL10 trace10;
     
     
     /** Whether to pass thru calls to the real GL instance */
     public boolean pass = true;
 
 
     /** The number of iterations to trace.  < 0 means forever */
     public int traceCount = 1;
     
     
     /** Whether to automatically detach when tracing is done */
     public boolean detachAfterTracing = true;
     
     
     public TraceNode(GameNode node) {
         super(node);
     }
 
 
     @Override
     public void attach() {
         super.attach();
         setTracer();
         annotateTree();
     }
 
 
     @Override
     public void detach() {
         deannotateTree();
         super.detach();
     }
     
     
     protected void setTracer() {
         if ( this.trace10 == null ) {
             RenderNode renderNode = (RenderNode)realNode;
             if ( renderNode.renderContext != null ) {
                 if ( renderNode.renderContext.isGL11 ) this.trace10 = new TraceGL11();
                 else this.trace10 = new TraceGL10();
             }   
         }
     }
     
     
     public void annotateTree() {
         // For every child node (recursively), create
         // a Annotation hook node
         GameNode.Visitor annotater = new GameNode.Visitor(){
             public void run(GameNode node) { new Annotation(node,TraceNode.this); }
         };
         
         for(int i=0; i<realNode.num_children; ++i) {
             realNode.children[i].visit(annotater,false);
         }
     }
 
     
     public void deannotateTree() {
         GameNode.Visitor deannotater = new GameNode.Visitor(){
             public void run(GameNode node) {
                 if ( !(node instanceof Annotation) ) return;
                 Annotation anno = (Annotation)node;
                 if ( anno.traceNode == TraceNode.this ) anno.detach();
             }
         };
         
         for(int i=0; i<realNode.num_children; ++i) {
             realNode.children[i].visit(deannotater,false);
         }
     }
 
     
     @Override
     public boolean update(long millis) {
         if ( trace10 == null || traceCount == 0 )
             return super.update(millis);
         
         // Swap out the GL10 in the RenderContext, then swap it back
         // for renderPostfix
         RenderNode render = (RenderNode)realNode;
         RenderContext rc = render.renderContext;
         
         trace10.realGL10 = rc.gl10;
         rc.gl10 = trace10;
         if (rc.gl11 != null && trace10 instanceof TraceGL11) {
             TraceGL11 trace11 = (TraceGL11)trace10;
             trace11.realGL11 = rc.gl11;
             rc.gl11 = trace11;   
         }
         
         boolean updated = super.update(millis);
         
         // Swap the original GL10 back to the RenderContext
         rc.gl10 = trace10.realGL10;
         if (rc.gl11 != null && trace10 instanceof TraceGL11) {
             TraceGL11 trace11 = (TraceGL11)trace10;
             rc.gl11 = trace11.realGL11;
         }
         
         if ( traceCount > 0 ) traceCount--;
         if ( traceCount == 0 && detachAfterTracing ) {
             detach();
         }
         
         return updated;
     }
 
 
     /**
      * Annotation nodes are spliced into the tree under a TraceNode
      * to add trace events for every node that gets visited.  That makes
      * it clear where the specific GL call traces occur.
      * 
      * @author jeremy
      *
      */
     public static class Annotation extends HookNode {
 
         protected TraceNode traceNode;
         
         public Annotation(GameNode realNode,TraceNode traceNode) {
             super(realNode);
             this.traceNode = traceNode;
         }
         
         @Override
         public boolean update(long millis) {
             traceNode.trace10.trace(String.format("+++ %s '%s'",realNode,realNode.name));
             boolean updated = super.update(millis);
             traceNode.trace10.trace(String.format("--- %s '%s'",realNode,realNode.name));
             return updated;
         }
     }
 }
