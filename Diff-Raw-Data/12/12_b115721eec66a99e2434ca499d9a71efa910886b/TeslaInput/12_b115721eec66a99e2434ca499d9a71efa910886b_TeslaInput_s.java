 package bot;
 
 import java.awt.Toolkit;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 
 import com.skype.Call;
 import com.skype.Chat;
 import com.skype.ChatMessage;
 import com.skype.ContactList;
 import com.skype.Skype;
 import com.skype.SkypeException;
 import com.skype.Call.Status;
 import com.skype.connector.ConnectorException;
 
 public class TeslaInput {
 
 	private String chatMessageID;
 	private String status;
 	private String[] cmd;
 	private boolean justAdded;
 	
 	//Roll 2d6;
 	//Send intro message if no message history (?)
 	
 	Radio radio = new Radio();
 	
 	public void getInput(String input) throws ConnectorException, SkypeException, IOException{
 		if(input.equals("PONG")){
 			return;
 		}
 		cmd = input.split(" ");
 		if(cmd[0].equals("CHATMESSAGE")){
 			if(cmd.length==3){
 				System.out.println("Chat event occured");
 			}else{
 				try{
 					status = cmd[2] + " " + cmd[3];
 					if(status.equals("STATUS RECEIVED") || status.equals("STATUS SENDING")){
 						chatMessageID = cmd[1];
 						readMessage(chatMessageID);
 					}
 				}catch(Exception e){
 					e.printStackTrace();
 					System.out.println("Error in reading message.");
 					return;
 				}
 			}
 		}else if(cmd[0].equals("CALL")){
 			if(cmd.length == 4){
 				if(cmd[3].equals("RINGING")){
 					try{
 						Call call = new Call(cmd[1]);
 						if(radio.isListening()){
 							radio.play(call);
						}else if(!call.equals(radio.getCall())){
							System.out.println(call.getStatus().toString());
 							call.finish();
 						}
 					}catch(Exception e){
 						e.printStackTrace();
 						return;
 					}
 				}else if(cmd[2].startsWith("VAA")){
 					if(cmd[3].equals("FALSE")){
 						radio.songOver();
 					}
 				}
 			}
 		}else if(cmd[0].equals("CHAT")){
 			try{
				if(cmd[2].equals("ADDER") || justAdded==true){ //when somebody adds you to a chat
 					justAdded=false;
 					Chat newChat = new Chat(cmd[1]);
 					TeslaCmdsYou.addAllFriends(newChat, cmd[3], "");
					if(newChat.getAllMembers().length > 2){
 						newChat.send(TeslaCmdsYou.getFileInfo("greetings.txt"));
 					}
 				}else if(cmd[2].equals("MEMBERS")){
 					Chat newChat = new Chat(cmd[1]);
 					TeslaCmdsYou.addAllFriends(newChat, "who has added you to a group chat I'm in", formName(cmd,3));
 				}
 			}catch(Exception e){
 				return;
 			}
 		}else if(cmd[0].equals("USER")){
 			try{
 				if(cmd[2].equals("RECEIVEDAUTHREQUEST")){
 					ContactList contacts = new ContactList();
 					justAdded=true;
 					contacts.addFriend(cmd[1], "");
 				}
 			}catch(Exception e){
 				return;
 			}
 		}
 	}
 	private void readMessage(String msgID) throws ConnectorException, SkypeException, IOException{
 		ChatMessage msg = new ChatMessage(msgID);
 		String body = msg.getContent();
 		String speaker = msg.getSenderDisplayName();
 		if(!speaker.equals("Tesla Bot")){
 			msg.setSeen();
 		}
 		Date time = msg.getTime();
 		System.out.println(time + " : " + speaker + ": " + body);
 		
 		if(body.startsWith("!") || body.startsWith("http") || body.startsWith("www")){
 			if(body.startsWith("www")){
 				body = body.replace("www", "http://www");
 			}
 			String[] args = body.split("\\s+");
 			TeslaCmdsYou action = new TeslaCmdsYou(time, speaker, msg.getChat().getAllMembers());
 			String rtrn="";
 			if(body.startsWith("!")){
 				args[0] = args[0].toLowerCase();
 				if(args[0].equals("!roll")){
 					rtrn = action.rollDice(formName(args,1));
 				}else if(args[0].equals("!whereis")){
 					rtrn = action.lastSeen(formName(args,1));
 				}else if(args[0].equals("!users")){
 					rtrn = action.users();
 				}else if(args[0].equals("!poke")){
 					rtrn = action.poke(formName(args,1));
 				}else if(args[0].equals("!slap")){
 					rtrn = action.slap(formName(args,1));
 				}else if(args[0].equals("!search") || args[0].equals("!google")){
 					rtrn = action.googleLink("https://www.google.com/#hl=en&q=" + formName(args,1));
 				}else if(args[0].equals("!wiki")){
 					rtrn = action.wikipediaLink("http://en.wikipedia.org/wiki/" + formName(args,1));
 				}else if(args[0].equals("!ping")){
 					if(args.length==1){
 						rtrn = action.ping();
 					}else{
 						rtrn = action.ping(args[1]);
 					}					
 				}else if(args[0].equals("!pong")){
 					rtrn = "Apparently " + speaker + " thinks the name of the game is Pong-Ping. Everybody point and laugh.";
 				}
 				else if(args[0].equals("!quote")){
 					rtrn = action.getRndmLine("TeslaQuotes.txt");
 				}else if(args[0].equals("!help")){
 					rtrn = "Help has been delivered to you personally.";
 					action.getFullFile("txtCmd.txt");
 				}else if(args[0].equals("!add")){
 					rtrn = action.addFriends(formName(args,1));
 				}else if(args[0].equals("!spam")){
 					rtrn = action.getRndmLine("spambot.txt");
 				}else if(args[0].equals("!notify")){
 					rtrn = action.notifyChats(formName(args,1));
 				}else if(args[0].equals("!done")){
 					rtrn = action.doneAction(formName(args,1));
 				}else if(args[0].equals("easter")){
 					if(args.length > 1 && args[1].equals("egg")){
 						rtrn = "Whoa magical easter egg find.";
 					}
 				}else if(args[0].equals("!choose")){
 					rtrn = action.choseElement(formName(args, 1));
 				}else if(args[0].equals("!conch")){
 					rtrn = action.getRndmLine("magicconch.txt") + "\nTHE CONCH HAS SPOKEN";
 				}else if(args[0].equals("!repeat")){
 					if(args.length >= 3){
 						rtrn = action.repeat(args[1], formName(args, 2));
 					}else{
 						rtrn = "Invalid format. Must be !repeat <times> <message>";
 					}
 					
 				}else if(args[0].equals("!radio")){
 					if(args.length > 1 && args[1].equals("help")){
 						action.getFullFile("radioHelp.txt");
 						rtrn = "Help has been delivered to you personally.";
 					}else{
						rtrn = radio.getCommand(args, msg.getChat());
 					}
 				}else if(args[0].equals("!summon")){
 					msg.getChat().openChat();
 					Toolkit.getDefaultToolkit().beep();
 					rtrn = "My creator has been alerted of your existence.";
 				}else{
 					rtrn = "I do not know how to " + body.substring(1) + ". Use !help to see a list of commands.";
 					if(body.length()==1){
 						Long randomNumber = Math.round(Math.random());
 						if(randomNumber==1){
 							rtrn = "Don't you worry about blank, let me worry about blank.";
 						}else{
 							rtrn = "Blank? BLANK?! You're not looking at the big picture!";
 						}
 					}
 				}
 			}else if(body.startsWith("http")){
 				try{
 					if(args[0].contains("youtube.com/watch?")){
 						rtrn = action.youtubeLink(args[0]);
 					}else if(args[0].contains("imgur.com")){
 						rtrn = action.imgurLink(args[0]);
 					}else if(args[0].contains("twitter.com")){
 						rtrn = action.twitterLink(args[0]);
 					}else if(args[0].contains("reddit.com")){
 						rtrn = action.redditLink(args[0]);
 					}else if(args[0].contains("/forum/show")){
 						rtrn = action.forumLink(args[0]);
 					}
 				}catch(Exception e){
 					rtrn = "Link detected but could not be read.";
 				}
 			}
 			if(rtrn.length()!=0){
 				System.out.println(rtrn);
 				msg.getChat().send(rtrn);
 			}
 		}
 	}
 	private String formName(String[] args, int start){
 		String name = "";
 		for(int i=start;i<args.length;i++){
 			if(i<args.length-1){
 				name = name.concat(args[i]) + " ";
 			}else{
 				name = name.concat(args[i]);
 			}
 		}
 		return name;
 	}
 }
