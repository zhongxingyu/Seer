 package cli.interaction;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 import cli.backend.URLManager;
 import cli.io.Printer;
 
 public class Interactor {
 
 	private enum InteractionState{
 		MENU,
 		EXIT,
 		REQUEST
 	}
 
 	private InteractionState state;
 	private Printer o;
 	private BufferedReader i;
 	private String lastDownloadName;
 
 	public Interactor(){
 		super();
 		this.state = InteractionState.MENU;
 		this.o = new Printer();
 		this.i = new BufferedReader(new InputStreamReader(System.in));
 		this.lastDownloadName = "";
 	}
 
 	public void interact() throws IOException{
 		while(this.state != InteractionState.EXIT){
 			o.clear();
 			choices();
 			act(i.readLine());
 		}
 	}
 
 	private void act(String valueRead) {
 		int item;
 		try{
 			item = Integer.parseInt(valueRead);
 		} catch(NumberFormatException e){
 			o.print("You must type a number and "+valueRead+" is not a number. Please try to type a correct number.");
 			return;
 		}
 		switch(state){
 		case MENU:
 			actMenu(item);
 			break;
 		case REQUEST:
 			actRequest(item);
 			break;
 		case EXIT:
 			break;
 		}
 	}
 
 	private void actMenu(int item) {
 		switch(item){
 		case 1:
			o.print("Please write your request as you feel it. There are no special keywords, however it is important to specify which drug is causing you trouble.");
 			String request = "";
 			try {
 				request = i.readLine();
 			} catch (IOException e) {
 				o.print("A problem occured while computing your request. Would you please retry?");
 				return;
 			}
 			this.lastDownloadName = Browser.getInstance().download(URLManager.getInstance().getURL("service", request));
 			if(this.lastDownloadName != null)
 				this.state = InteractionState.REQUEST;
 			break;
 		case 2:
 			Browser.getInstance().open(URLManager.getInstance().getURL("web",""));
 			break;
 		case 3:
 			Browser.getInstance().open(URLManager.getInstance().getURL("emergency",""));
 			break;
 		case 4:
 			this.state = InteractionState.EXIT;
 			break;
 		default:
 			o.print("You chose an incorrect option. Please choose a number between 1 and 4.");
 			break;
 		}
 	}
 
 	private void actRequest(int item) {
 		switch(item){
 		case 1:
 			Browser.getInstance().openFile(this.lastDownloadName);
 			break;
 		case 2:
 			this.state = InteractionState.MENU;
 			break;
 		case 3:
 			this.state = InteractionState.EXIT;
 			break;
 		default:
 			o.print("You chose an incorrect option. Please choose a number between 1 and 3.");
 			break;
 		}
 	}
 
 	private void choices() {
 		switch(state){
 		case MENU:
 			o.print("\t\tAnonymous substance abuse counseling e-service");
 			o.print("\t\t\tCommand-line interface");
 			o.print();
 			o.print("\tWhat do you want to do?");
 			o.print("[1] Write a written request to Anonymous substance abuse e-service");
 			o.print("[2] Open browser to Anonymous substance abuse e-service website");
 			o.print("[3] Open browser to Centers of Disease Control and Prevention");
 			o.print("[4] Exit");
 			o.print();
 			o.print("\tPress a number key between 1 and 4");
 			break;
 		case REQUEST:
 			o.print();
 			o.print("\tA file concerning your problems has been successfully downloaded to "+this.lastDownloadName);
 			o.print("[1] Open file with PDF reader");
 			o.print("[2] Go back to menu");
 			o.print("[3] Exit");
 			o.print();
 			o.print("\tPress a number key between 1 and 3");
 			break;
 		case EXIT:
 			o.print();
 			o.print("Goodbye! And good luck!");
 			break;
 		}
 
 	}
 
 }
