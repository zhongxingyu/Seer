 import java.io.IOException;
 
 
 public class RandomThrower extends Client {
 
 	@Override
 	protected synchronized void resultHandler(Result result) {
		System.out.printf("Result: I threw: %-10s They threw: %-10s\n",result.iThrew.name(),result.theyThrew.name());
 
 		//put result handling code here, not a lot to it
 		//obviously we aren't handling results
 	}
 
 	@Override
 	protected synchronized void throwRequestHandler() {
 		int pick=(int) (Math.random()*3);
 		try {
 			switch(pick){
 			case 0:
 				this.throwScissors();
 				break;
 			case 1:
 				this.throwPaper();
 				break;
 			case 2:
 				this.throwRock();
 				break;
 			}
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public RandomThrower(int securePort) {
 		super(securePort);
 		this.name="Random Thrower";
 	}
 
 	public static void main(String[] args) {
 
 		if(args.length<1){
 			System.out.println("error useage: RandomThrower port");
 		}
 		int secure_port=Integer.parseInt(args[0]);
 
 		RandomThrower client=new RandomThrower(secure_port);
 		client.playGame();
 	}
 
 }
