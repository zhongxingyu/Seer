 /* How to insert a element in heap if it not duplicated*/
 /* We use the Heap Structured Binary Tree, to insert element into heap, the running time will be O(logn) */
 /* Because basically Heap is a tree structure*/
 
 /********************************
                1                                  
           _____|____                      
          |          |                        
          4         20                         
        __|__      __|_                  
       |     |    |    |                 
       6     12  22   30           
      _|_              |                           
     |   |            31           
    10  11
 ********************************/
 
 
 
 public static void insertElement(Node root, Node new_element)
 {
 	int val = new_element.value;
 	if(val < root.value)
 	{
 		new_element.left = root;
 		new_element.right = null;
 	}
 	else
 	{
 		while(1)
 		{
				if(root.right !=null && val > root.right.value)
 			{
 				root = root.right;
 			}
 			else if(root.left !=null && val > root.left.value)
 			{
 				root = root.left; 
 			}
 			else 
 			{
 				if(root.right != null)
 				{
 					new_element.left = root.left;
 				}
 				else
 				{
 					root.right = root.left;
 					root.left = new_element
 				}
 			}
 		}
 	}
 }
