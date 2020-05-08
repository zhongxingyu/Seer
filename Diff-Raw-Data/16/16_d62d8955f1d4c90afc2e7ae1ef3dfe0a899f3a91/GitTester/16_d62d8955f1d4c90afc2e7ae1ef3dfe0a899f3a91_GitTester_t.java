 public class GitTester
 {
 
 	public GitTester()
 	{
 		System.out.println("I have a constructor");
 		System.out.println("Lets just be a pain");
 	}
	public void runLoop()
	{
		for (int i = 0; i < 10; i++)
		{
			if (i ==9)
			{
				System.out.println("I'm in the loop");
			}
		}
	}

 	public static void main(String[] args)
 	{
 		GitTester gt = new GitTester();
		gt.runLoop();
		System.out.println("Hello Git");

 	}
 }
