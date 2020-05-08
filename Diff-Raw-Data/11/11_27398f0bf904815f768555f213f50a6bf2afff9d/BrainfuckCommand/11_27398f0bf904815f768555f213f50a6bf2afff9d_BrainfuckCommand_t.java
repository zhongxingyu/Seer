 package commands;
 
 import org.pircbotx.Channel;
 import org.pircbotx.User;
 
 import backend.Bot;
 import backend.Util;
 
 public class BrainfuckCommand extends Command {
 	int instructionNum = 1;
 	
 	@Override
 	protected void initialize() {
 		setName("BrainfuckInterpreter");
 		setHelpText("Let's interpret some Brainfuck code! ( Wikipedia: http://goo.gl/sEtF )");
 		getAliases().add("brainfuck");
 	}
 	
 	@Override
 	protected String format() {
 		return super.format() + " [brainfuck code] (optional input)";
 	}
 	
 	private void debugBF(String command, int pos, String text) {
 		debugBF(command, pos, text, true);
 	}
 	
 	private void debugBF(String command, int pos, String text, boolean increasecounter) {
		debug("(" + (increasecounter ? ++instructionNum : instructionNum) + ") Pos: " + pos + " '" + command + "' " + text);
 	}
 
 	@Override
 	public void execute(Bot bot, Channel chan, User user, String message) {
 		String target = getTarget(chan, user);
 		
 		byte bytes[] = new byte[1024];
 		int pos = 0;
 		int curinput = 0;
 		int lastnum = 0;
 		int infloop_suspect = 0;
 		String output = "";
 		
 		instructionNum = 0;
 	   
 		for(int i = 0; i < 1024; i++) { bytes[i] = 0; }
 	   
 		String[] args = null;
 	   
 		if(Util.checkArgs(message,2)) {
 				args = message.split(" ", 3);
 				args[1] = args[1].trim();
 				String allowedops = "<>+-.,[]";
 			   
 				for(int i = 0; i < args[1].length(); i++) {
 						if(!allowedops.contains( "" + args[1].charAt(i) ) ) {
 								bot.sendMessage(target, "ERROR: Invalid operator: " + args[1].charAt(i));
 								return;
 						}
 				}
 			   
 				for(int i = 0; i < args[1].length(); i++) {
 					switch(args[1].charAt(i)) {
 						case '>': if(++pos >= 1024) pos = 0; debugBF(""+args[1].charAt(i), i, "array pos now " + pos); break;
 						case '<': if(--pos < 0) pos = 1023; debugBF(""+args[1].charAt(i), i, "array pos now " + pos); break;
 						case '+': bytes[pos]++; debugBF(""+args[1].charAt(i), i, "a[" + pos + "] = " + bytes[pos]); break;
 						case '-': bytes[pos]--; debugBF(""+args[1].charAt(i), i, "a[" + pos + "] = " + bytes[pos]); break;
 						case '.': output += "" + (char)bytes[pos]; debugBF(""+args[1].charAt(i), i, "output " + bytes[pos] + ": " + (char)bytes[pos]); break;
 						case ',':
 							if(args.length > 2 && curinput < args[2].length()) {
 								bytes[pos] = (byte)args[2].charAt(curinput++);
 								debugBF(""+args[1].charAt(i), i, "read " + bytes[pos] + ": " + (char)bytes[pos]);
 							}
 							break;
 						case '[': {
 							int oldi = i;
 							int startbracketsfound = 0;
 							if(bytes[pos] == 0) {
 								while(i < args[1].length()) {
 									++i;
 									if(args[1].charAt(i) == '[') {
 										startbracketsfound++;
 									}
 									if(args[1].charAt(i) == ']') {
 										if(startbracketsfound-- == 0) break;
 									}
 								}
 							}
 							
 							debugBF(""+args[1].charAt(i), oldi, "a[" + pos + "] = " + bytes[pos]);
 							break;
 						}
 						case ']': {
 							debugBF(""+args[1].charAt(i), i, "a[" + pos + "] = " + bytes[pos]);
 							if(bytes[pos] != 0) {
 								int oldi = i;
 								if(lastnum == bytes[pos]) {
 									if(infloop_suspect++ > 1024*1024) {
 										bot.sendMessage(target, "Suspected infinite loop, stopping execution after " + infloop_suspect + " run(s)...");
 										return;
 									}
 								}
 								lastnum = bytes[pos];
 								int endbracketsfound = 0;
 								while(i >= 0) {
 									--i;
 									if(args[1].charAt(i) == ']') {
 										endbracketsfound++;
 									}
 									if(args[1].charAt(i) == '[') {
 										if(endbracketsfound-- == 0) break;
 									}
 								}
 								debugBF(""+args[1].charAt(oldi), oldi, "going back to " + i, false);
 								--i;
 							}
 							
 							break;
 						}
 					}
 				}
 				output = output.replace("\n", "[\\n]");
 				if(!target.equals(user.getNick())) output = truncateOutput(output);
 				bot.sendMessage(target, "Output: " + output);
 		}
 		else {
 				bot.sendMessage(target, "Invalid format: " + format());
 		}
 	}
 	
 	private String truncateOutput(String text) {
 		if(text.length() > 340) return text.substring(0, 340) + "... (truncated at 340 symbols; use in a PM to get full output)";
 		else return text;
 	}
 }
