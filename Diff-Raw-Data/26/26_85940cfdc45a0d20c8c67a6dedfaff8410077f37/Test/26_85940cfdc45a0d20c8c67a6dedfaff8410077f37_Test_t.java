 
 public class Test {
 
 	public static void main(String[] args) {
		Tree t = new Tree(new TreeNode(50));
		int[] numbers = {8, 10 , 9, 15, 4, 18, 13, 12};
//		for(int i = 0; i < numbers.length; i++){
//			t.add(numbers[i]);
//		}
		for(int i = 0; i < 20; i++){
			t.add((int)(Math.random()*100));
 		}
 		System.out.println("Preorder: " + t.preorderToString());
 		System.out.println("Ordered: " + t.toString());
 		System.out.println("Postorder: " + t.postorderToString());
		System.out.println(t.toString());
 		t.convert();
 		System.out.println(t);
 	}
 
 }
