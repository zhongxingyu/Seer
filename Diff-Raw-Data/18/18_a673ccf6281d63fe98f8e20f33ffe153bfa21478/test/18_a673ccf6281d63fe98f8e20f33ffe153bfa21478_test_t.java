 
 public class test {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		long a =System.currentTimeMillis();
		System.out.print(a);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(System.currentTimeMillis()-a);
 	}
 
 }
