 package tk.blackwolf12333.grieflog.utils.searching;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import tk.blackwolf12333.grieflog.utils.logging.Events;
 
 public class ArgumentParser implements Serializable {
 
 	private static final long serialVersionUID = -1852087723338527110L;
 	public boolean eventNullError = false;
 	public boolean argsNullError = false;
 	
 	public String player;
 	public String world;
 	public String event;
 	public String blockFilter;
 	public boolean worldedit;
 	
 	public ArgumentParser(String[] args) {
		ArrayList<String> arguments = new ArrayList<String>();
		
		for(int i = 0; i < args.length; i++) {
			if(i >= 1) {
				arguments.add(args[i]);
 			}
 		}
		parse(arguments);
 	}
 	
 	public void parse(ArrayList<String> args) {
 		try {
 			if(args.size() == 1) {
 				if(args.get(0).equalsIgnoreCase("we")) {
 					this.worldedit = true;
 					return; // this is not possible!
 				} else {
 					parse1Argument(args.get(0));
 				}
 			} else if(args.size() == 2) {
 				if(args.get(0).equalsIgnoreCase("we")) {
 					this.worldedit = true;
 					parse1Argument(args.get(1));
 				} else {
 					parse2Arguments(args.get(0), args.get(1));
 				}
 			} else if(args.size() == 3) {
 				if(args.get(0).equalsIgnoreCase("we")) {
 					this.worldedit = true;
 					parse2Arguments(args.get(1), args.get(2));
 				} else {
 					parse3Arguments(args.get(0), args.get(1), args.get(2));
 				}
 			} else if(args.size() == 4) {
 				if(args.get(0).equalsIgnoreCase("we")) {
 					this.worldedit = true;
 					parse3Arguments(args.get(1), args.get(2), args.get(3));
 				} else {
 					parse4Arguments(args.get(0), args.get(1), args.get(2), args.get(3));
 				}
 			}
 		} catch(NullPointerException e) {
 			eventNullError = true;
 		} catch(ArrayIndexOutOfBoundsException e) {
 			argsNullError = true;
 		}
 		
 	}
 	
 	private void parse1Argument(String arg) {
 		String[] split1 = arg.split(":");
 		char ch1 = split1[0].charAt(0);
 		
 		switch(ch1) {
 		case 'p':
 			player = split1[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split1[1]);
 			break;
 		case 'w':
 			world = split1[1];
 			break;
 		case 'b':
 			blockFilter = split1[1];
 		default:
 			break;	
 		}
 	}
 	
 	private void parse2Arguments(String arg, String arg1) {
 		String[] split1 = arg.split(":");
 		String[] split2 = arg1.split(":");
 		char ch1 = split1[0].charAt(0);
 		char ch2 = split2[0].charAt(0);
 		
 		switch(ch1) {
 		case 'p':
 			player = split1[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split1[1]);
 			break;
 		case 'w':
 			world = split1[1];
 			break;
 		case 'b':
 			blockFilter = split1[1];
 		default:
 			break;	
 		}
 		
 		switch(ch2) {
 		case 'p':
 			player = split2[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split2[1]);
 			break;
 		case 'w':
 			world = split2[1];
 			break;
 		case 'b':
 			blockFilter = split2[1];
 		default:
 			break;
 		}
 	}
 	
 	private void parse3Arguments(String arg, String arg1, String arg2) {
 		String[] split1 = arg.split(":");
 		String[] split2 = arg1.split(":");
 		String[] split3 = arg2.split(":");
 		char ch1 = split1[0].charAt(0);
 		char ch2 = split2[0].charAt(0);
 		char ch3 = split3[0].charAt(0);
 		
 		switch(ch1) {
 		case 'p':
 			player = split1[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split1[1]);
 			break;
 		case 'w':
 			world = split1[1];
 			break;
 		case 'b':
 			blockFilter = split1[1];
 		default:
 			break;	
 		}
 		
 		switch(ch2) {
 		case 'p':
 			player = split2[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split2[1]);
 			break;
 		case 'w':
 			world = split2[1];
 			break;
 		case 'b':
 			blockFilter = split2[1];
 		default:
 			break;	
 		}
 		
 		switch(ch3) {
 		case 'p':
 			player = split3[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split3[1]);
 			break;
 		case 'w':
 			world = split3[1];
 			break;
 		case 'b':
 			blockFilter = split3[1];
 		default:
 			break;	
 		}
 	}
 	
 	private void parse4Arguments(String arg, String arg1, String arg2, String arg3) {
 		String[] split1 = arg.split(":");
 		String[] split2 = arg1.split(":");
 		String[] split3 = arg2.split(":");
 		String[] split4 = arg3.split(":");
 		char ch1 = split1[0].charAt(0);
 		char ch2 = split2[0].charAt(0);
 		char ch3 = split3[0].charAt(0);
 		char ch4 = split4[0].charAt(0);
 		
 		switch(ch1) {
 		case 'p':
 			player = split1[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split1[1]);
 			break;
 		case 'w':
 			world = split1[1];
 			break;
 		case 'b':
 			blockFilter = split1[1];
 		default:
 			break;	
 		}
 		
 		switch(ch2) {
 		case 'p':
 			player = split2[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split2[1]);
 			break;
 		case 'w':
 			world = split2[1];
 			break;
 		case 'b':
 			blockFilter = split2[1];
 		default:
 			break;	
 		}
 		
 		switch(ch3) {
 		case 'p':
 			player = split3[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split3[1]);
 			break;
 		case 'w':
 			world = split3[1];
 			break;
 		case 'b':
 			blockFilter = split3[1];
 		default:
 			break;
 		}
 		
 		switch(ch4) {
 		case 'p':
 			player = split4[1];
 			break;
 		case 'e':
 			event = getEventFromAlias(split4[1]);
 			break;
 		case 'w':
 			world = split4[1];
 			break;
 		case 'b':
 			blockFilter = split4[1];
 		default:
 			break;
 		}
 	}
 	
 	public String getEventFromAlias(String alias) {
 		return Events.getEvent(alias).getEventName();
 	}
 }
