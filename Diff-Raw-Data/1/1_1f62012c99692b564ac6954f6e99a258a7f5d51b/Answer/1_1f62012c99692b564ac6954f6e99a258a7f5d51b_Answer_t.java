 package play.question01;
 
 import java.util.Arrays;
 
 public class Answer {
 
 	private int n = 0;
 	private int[] a;
 	private int k = 0;
 	private boolean result;
 	
 	public void start(int n, int[] a, int k)
 	{
 		this.n = n;
 		this.a = a;
 		this.k = k;
 		int sum = 0;
 		Arrays.sort(a);
 		result = this.find(0, sum);
 	}
 	
 	public boolean find(int num, int sum)
 	{
 		if (k == sum) {
 			return true;
 		}
 		else if (n <= num || k < a[num] || k == sum) {
 			return false;
 		}
 		else if (k == a[num]) {
 			return true;
 		}
 
 		result = find(num + 1, sum);
 		if (result) {
 			return result;
 		}
 		sum = sum + a[num];
 		result = find(num + 1, sum);
 		
 		return result;
 	}
 	
 	public String print()
 	{
 		String answer;
 		if (result) {
 			answer = "Yes";
 		}
 		else {
 			answer = "No";
 		}
 		return answer;
 	}
 
 }
 
