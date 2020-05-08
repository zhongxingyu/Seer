 package command;
 
 import java.util.StringTokenizer;
 
 import javax.xml.ws.Holder;
 
 import managementClient.commands.Subscribe;
 
 import server.logic.IUserRelated;
 
 
 
 public class CommandParser {
 	private ICommandList commandList=null;
 	private boolean paramterize=true;
 	private boolean chop=false; //chops the command
 
 	private IUserRelated userHolder=null;
 
 
 	/**
 	 * 
 	 * @param parameterize : parameter- tokenizer on/off
 	 */
 	public CommandParser(boolean parameterize, IUserRelated holder){
 		this.paramterize=parameterize;
 		this.userHolder=holder;
 	}
 
 	public void setCommandList(ICommandList list){
 		this.commandList=list;
 	}
 
 
 
 	public String parse(String inputLine){
 		//System.out.println(inputLine);
 		inputLine= inputLine.trim();
 		String commandKey;
 		try {
 			commandKey = getCommandKey(inputLine);
 		} catch (CommandException e) {
 			return e.getMessage();
 		}
 		ICommand command=null;
 
 		if(hasCommand(commandKey)){
 			//try{
 			command= commandList.get(commandKey);
 			if(command.needsRegistration() && (userHolder.getUser().equals("")) )
 				return "!print You need to login first!";
 
 			if(paramterize){
 				String[] params=null;
 				try{
 					//checkParameterCount(params, command.numberOfParams());
 					params= params(inputLine,command.numberOfParams());
 
 				}catch(CommandException e){
 					return e.getMessage();
 				}
 				return command.execute(params);
 			} else return command.execute(packInputLine(inputLine));
		} else if(commandKey.length()==0) return "";
 		else return "!print Unknown Command: "+commandKey+ " !";
 	}
 
 
 	private String[] params(String inputLine, int numberOfParams) throws CommandException {
 		int index=inputLine.indexOf(' ');
 		if(index!=-1)
 			inputLine= inputLine.substring(inputLine.indexOf(" "),inputLine.length()).trim(); //chop keyword
 		else if(numberOfParams>0){
 			throw new CommandException("!print missing parameters!");
 		}
 		StringTokenizer tokenizer= new StringTokenizer(inputLine, " ");
 
 		String[] params= new String[7];
 		String tmp="";
 		for(int i = 0; i<numberOfParams;i++){
 			if(tokenizer.hasMoreTokens()){
 				tmp=tokenizer.nextToken();
 				params[i]=tmp+" ";
 			}
 			else throw new CommandException("!print missing parameters!");
 		}
 
 		while((tokenizer.hasMoreTokens()&&(numberOfParams!=0))){
 			params[numberOfParams-1]+= tokenizer.nextToken()+" ";
 		}
 		//POLISISHING
 		for(int i=0;i<numberOfParams;i++){
 			params[i]= params[i].trim();
 		}
 
 		return params;
 	}
 	/*
 	private void checkParameterCount(String[] params, int numberOfParams) throws CommandException{
 		int c=0;
 		for(String s: params){
 			if(s!=null)
 				c++;
 		}
 		if(c<numberOfParams)
 			throw new CommandException("!print Parameters are missing! "+c);
 		else if(c>numberOfParams)
 			throw new CommandException("!print Too many parameters! "+c);
 
 
 
 	}
 
 	 */
 	private String getCommandKey(String inputLine)throws CommandException{	
 
 		//if(!Pattern.matches(commandPattern, inputLine)) throw new CommandException("Not a Command!");
 
 		String key="";
 		if(inputLine.contains(" "))
 			key= inputLine.substring(0, inputLine.indexOf(" "));
 		else key= inputLine.substring(0, inputLine.length());
 
 		return key;
 	}
 
 	private boolean hasCommand(String key){
 		return commandList.containsKey(key);
 	}
 	private String[] packInputLine(String input){
 		if(!chop)
 			return new String[]{input};
 		else{
 			return new String[]{chopKey(input)};
 		}
 	}
 
 
 	private String chopKey(String input) {
 		return input.substring(input.indexOf("!"), input.indexOf(" "));
 	}
 
 	/*
 	private String[] params(String inputLine){
 		String[] params= new String[5]; //maximal 5 parameters 
 
 		String tmp="";
 		int count=0;
 
 		String pattern;
 		if(inputLine.contains(" "))
 			pattern= inputLine.substring(inputLine.indexOf(" ")).trim(); //cut keyword
 		else pattern= inputLine.substring(inputLine.length()).trim(); //cut keyword
 
 
 		if(pattern.length()==0)
 			return params;
 		char[] line= pattern.toCharArray();
 		for(int i=0;i<=line.length-1;i++){
 			if((line[i]==' ') || (i==line.length-1)){
 				if(i==line.length-1){
 					tmp+=line[i];
 				}
 				params[count++]=tmp;
 				tmp="";
 			}else{
 				tmp+=line[i];
 			}
 		}
 
 		return params;
 	}
 	 */
 
 
 
 
 }
