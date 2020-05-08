 import java.io.IOException;
 
 public class Avalanch extends Client {
 
 	@Override
 	protected synchronized void resultHandler(Result result) {
		//System.out.printf("Result: I threw: %-10s They threw: %-10s\n",result.iThrew.name(),result.theyThrew.name());
 
 		//put result handling code here, not a lot to it
 		//obviously we aren't handling results
 	}
 
 	@Override
 	protected synchronized void throwRequestHandler() {
 		try {
 			//BRING THE AVALANCH
 			this.throwRock();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public Avalanch(int securePort) {
 		super(securePort);
 		this.name="Avalanch";
 	}
 	
 	public static void main(String[] args) {
 		if(args.length<1){
 			System.out.println("error useage: Client port");
 		}
 		int secure_port=Integer.parseInt(args[0]);
 		Avalanch client=new Avalanch(secure_port);
 		client.playGame();
 	}
 }
