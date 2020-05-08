 package html2windows.dom;
 import java.util.ArrayList;

 public class NodeList extends ArrayList<Node>{
 	
 	//目的:回傳index的Node
     public Node item(long index){
         return get((int)index);
     }
     
     //目的:回傳此List的長度
     public long length(){
         return size();
     }
 }
