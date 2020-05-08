 package spiralpoly;
 
 public class Graham {
 
     ListNode listHead = null;
     ListNode newListhead = null;
     int numToKeep = 0;
 
 
     // made non static so it can store what the supposed listHead is supposed to be
     public Graham(ListNode listHead) {
         this.listHead = listHead;
         this.numToKeep = numToKeep;
     }
 
     public Graham() {
 
     }
 
 
 	public LinkedList graham(LinkedList sortedList) {
 		// return straight away if there is only one element in the list
 		if (sortedList.header.next.next == sortedList.tail) {
 			return sortedList;
 		}
 
 		// Separate the points to for upper convex hull and lower convex hull
 		LinkedList upper = new LinkedList();
 		LinkedList lower = new LinkedList();
 
 
 
 
 		//Get first and last node
 		ListNode firstNode = sortedList.getHead();
 
         ListNode currentNode = firstNode;
 
             while (currentNode.next.element != null){
 			currentNode = currentNode.next;
 		}
 		ListNode lastNode = currentNode;
 		currentNode = firstNode.next;
 
 		upper.appendToHull(firstNode);
 //		lower.appendToHull(firstNode);
 
 		while (currentNode.next.element != null){
 			if (ccw(firstNode, lastNode, currentNode) <= 0){
 				lower.appendToHull(currentNode);
 			}
 			else{
 				upper.appendToHull(currentNode);
 			}
 			currentNode = currentNode.next;
 		}
 //		upper.appendToHull(lastNode);
 		lower.appendToHull(lastNode);
         lower.reverse_2();
         upper.concatenate(lower);
         LinkedList convexHull = findHull(upper);
         convexHull.printDetailed();
 
 		//Declare convexHull for the output
 //		LinkedList convexHull;
 
 
 		// Make each convex hull
 //		LinkedList lowerHull = findHull(lower,1);
 //		LinkedList upperHull = findHull(upper,-1);
 
 //		upperHull.pop();
 //		//Remove the same beginning and end of the lower hull
 //		lowerHull.reverse_2();
 //		lowerHull.pop();
 //		// Combine the two hulls
 //		convexHull = upperHull;
 //		convexHull.concatenate(lowerHull);
         // delete them from the origin list
         if (newListhead != null) convexHull.rotateToHead(newListhead);
         convexHull.removeOriginals(2); // leave the last two always
         return convexHull;
 	}
 
 	public LinkedList findHull(LinkedList setOfPoints){
 
 		//Put first two points in the hull
         LinkedListItr itr = setOfPoints.first();
         ListNode k;
         ListNode kPlusOne;
         ListNode kPlusTwo;
 
         System.out.println("SPRIALING");
         System.out.println(setOfPoints);
 
         // wrap the loop into a circuit
         ListNode originalHead = setOfPoints.getHead();
         ListNode originalTail = setOfPoints.getTail();
         originalTail.setNext(originalHead);
 
         // if the new head is the second point, set it as thus.
 //        if (itr.current.next.original == listHead) newListhead = itr.current.next;
 		//Graham scan
         // check if we are on last point
         k = itr.current;
         kPlusOne = k.next;
         kPlusTwo = kPlusOne.next;
        int i = 0;
 
 
         while (true) {
            i++;
            System.out.println("ON LOOP " + i);
            if (kPlusTwo.original == listHead) newListhead = kPlusTwo;
 
 			// if the last two points in the hull and the point to be considered form convex,
 			// accept the point and keep scanning
             if (ccw(k, kPlusOne, kPlusTwo) <= 0){
                 System.out.println("POINT ACCEPTED");
                 itr.advance();
                 if (itr.current == originalHead) break;
             }
 			// if not remove the last point added
 			else{
                 kPlusOne.removeSelf();
                 if (k != originalHead) {
                     itr.goBack();
                     System.out.println("GOING BACK");
                 }
                else {
                    System.out.println("GOING FORWARD");
                    itr.advance();
                    if (itr.current == originalHead) break;
                }

             }
 
             k = itr.current;
             kPlusOne = k.next;
             kPlusTwo = k.next.next;
             System.out.println(k);
             System.out.println(k == originalHead);
 
         }
 
         // unwrap the loop
         setOfPoints.header.setNext(kPlusOne);
         k.setNext(setOfPoints.tail);
 
 		return setOfPoints;
 	}
 
 	public static double ccw(ListNode P1, ListNode P2, ListNode P3){
 		//Three points are a counter-clockwise turn if ccw > 0, clockwise if
 		//ccw < 0, and collinear if ccw = 0
 		double product1, product2;
         product1 = (P2.element[0] - P1.element[0])*(P3.element[1] - P2.element[1]);
 		product2 = (P2.element[1] - P1.element[1])*(P3.element[0] - P2.element[0]);
 		return (product1 - product2);
 	}
 
 	public static void main(String[] args){
 		//unsortedList is the input LinkedList
 		LinkedList data = new LinkedList();
 		LinkedListItr p = data.zeroth();
 		/*
 		double[] x={ 0 , 5.0 };
 		data.insert(x,p);
 		p.advance();
 		double[] a={ 5.0 , 10 };
 		data.insert(a,p);
 		p.advance();
 		double[] b={ 10 , 5 };
 		data.insert(b,p);
 		p.advance();
 		double[] c={ 5 , 0 };
 		data.insert(c,p);
 		p.advance();
 		double[] d={ 2.591231629 , 0.6184665998 };
 		data.insert(d,p);
 		p.advance();
 		double[] e={ 7.679133975 , 0.7783603725 };
 		data.insert(e,p);
 		p.advance();
 		double[] f={ 7.679133975 , 9.221639628 };
 		data.insert(f,p);
 		p.advance();
 		double[] g={ 2.591231629 , 9.3815334 };
 		data.insert(g,p);
 		p.advance();
 		 */
 
 		double[] e={ 6 , 5 };
 		data.insert(e,p);
 		p.advance();
 		double[] d={ 5 , 4 };
 		data.insert(d,p);
 		p.advance();
 		double[] c={ 4 , 6 };
 		data.insert(c,p);
 		p.advance();
 		double[] b={ 3, 8 };
 		data.insert(b,p);
 		p.advance();
 		double[] a={ 2 , 5 };
 		data.insert(a,p);
 		p.advance();
 		double[] x={ 1 , 1 };
 		data.insert(x,p);
 		p.advance();
 
 		//Test
 		System.out.println("Given sorted points (X-ascending, Y-ascending): ");
 		LinkedList.printList(data);
 		LinkedList convexResult = new Graham().graham(data);
 		System.out.println();
 		System.out.println("Convex Hull: ");
 		LinkedList.printList(convexResult);
         System.out.println("Remaining points");
         LinkedList.printList(data);
     }
 }
