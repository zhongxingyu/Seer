 import connection.ChatRoom;
 import data.IdentiteReseau;
 import data.Message;
 import affichage.Fenetre;
 
 
 public class Main {
 
 	public static void main(String[] args) {
		//new Fenetre();
 		Message msg = new Message("ce message est un test tous con");
 		msg.setAuthor(new IdentiteReseau("123.4.4.1",1331,"louis"));
		ChatRoom cth = (new ChatRoom("127.0.0.1",3007, new IdentiteReseau("127.0.0.1",9007,"admin")));
		for(int i = 0; i < 34678567;i++) i=i+0;
		cth.sendMessage(msg);
 	}
 
 }
