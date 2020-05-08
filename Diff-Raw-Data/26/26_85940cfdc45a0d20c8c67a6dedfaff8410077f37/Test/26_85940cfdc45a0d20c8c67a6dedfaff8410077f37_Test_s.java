 
 public class Test {
 
 	public static void main(String[] args) {
		Tree t = new Tree(new TreeNode(12));
		int[] numbers = {8, 10 , 9, 15, 4, 18, 13, 12, 5, 2, 11, 16, 1, 3, 53, 12, 7};
		for(int i = 0; i < numbers.length; i++){
			t.add(numbers[i]);
 		}
 		System.out.println("Preorder: " + t.preorderToString());
 		System.out.println("Ordered: " + t.toString());
 		System.out.println("Postorder: " + t.postorderToString());
		
 		t.convert();
 		System.out.println(t);
 	}
 
 }
