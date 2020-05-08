 package nodes;
 
 /**
  *
  * @author Johanna 
  */
 public abstract class OperationNode extends Node {
     
     protected Node[] children;
          
     public static final int LEFT_CHILD=0;
     public static final int RIGHT_CHILD=1;
         
     public OperationNode(){
        this.children = new Node[2];
         this.children[LEFT_CHILD]=null;
         this.children[RIGHT_CHILD] = null;
     }
 
     @Override
     public Node getChild(int child) {
         return this.children[child];
     }
 
     public void insertNode(Node node){
        if(this.children[LEFT_CHILD] == null)
            this.children[LEFT_CHILD] = node;
        else{
            if(this.children[RIGHT_CHILD]==null)
                this.children[RIGHT_CHILD] = node;
            else{
                if( this.children[RIGHT_CHILD] instanceof OperationNode)
                    ((OperationNode)  this.children[RIGHT_CHILD]).insertNode(node);
            }     
        }      
     }
     
     protected double evaluateNode(Node node){
         if(node != null) return node.evaluate();
         else return -1;
     }
    
 }
