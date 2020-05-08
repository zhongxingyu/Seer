 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * 
  * @author andrew
  *
  */
 public class NFAGenerator {
 	private final boolean DEBUG = true;
 	private StateTable nfa;
 	private int index;
 	private Lexical lex;
 	private String regex;
 	private String token;
 	private int entry_ind;
 	private boolean toggleStar;
 	private boolean togglePlus;
 	private boolean toggleEpsilon;
 	
 	public NFAGenerator(Lexical l){
 		lex = l;
 		index = 0;
 		entry_ind = 0;
 		nfa = new StateTable();
 		regex = new String();
 		token = new String();
 		toggleStar = false;
 		toggleEpsilon = false;
 	}
 	
 	public NFAGenerator(String r){
 		index = 0;
 		entry_ind = 0;
 		nfa = new StateTable();
 		regex = r;
 		token = new String();
 		toggleStar = false;
 		toggleEpsilon = false;
 	}
 	
 	public StateTable genNFA(){
 		if(DEBUG)System.out.println("genNFA()");
 		populate("@");
 		int subnfa;
 		for(TokenC t: lex.getTokens()){
 			subnfa = entry_ind;
 			regex = t.getLegal().get(0);
 			token = t.getTitle().substring(1);
 			index = 0;
 			if(DEBUG)System.out.println(token);
 			if(DEBUG)System.out.println(regex);
 			regex();
 			concat(1,subnfa);
 		}
 //		regex();
 		return nfa;
 	}
 	
 	private boolean regex(){
 		if(DEBUG)System.out.println("regex()");
 		boolean result = rexp();
 		nfa.getTableRowArray(entry_ind-1).get(0).setAccept(true);//set the very last entry to accept
 		nfa.getTableRowArray(entry_ind-1).get(0).setType(token);
 		return result;
 /*		if(rexp()){
 			return true;
 		}
 		else{
 			return false;
 		}*/
 	}
 	
 	private boolean rexp(){
 		if(DEBUG)System.out.println("rexp()");
 		populate("@");
 		int epsilon = entry_ind-1;
 		int state1 = entry_ind;
 		if(rexp1()){//find rexp1
 			
 			int state2=entry_ind;
 			if(peekChar()=='|'){//if UNION
 				if(rexpprime()){
 					union(epsilon,state1,state2);
 					return true;
 				}
 			}
 			else
 			{
 				concat(epsilon,state1);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean rexpprime(){
 		if(DEBUG)System.out.println("rexpprime()");
 		populate("@");
 		if(index>=regex.length()||peekChar()==')'){//end of regex
 			return true;
 		}
 		if(peekChar()=='|'){//if another UNION
 			if(match('|')){
 				int epsilon = entry_ind-1;
 				int state1 = entry_ind;
 				if(rexp1()){//first subnfa
					nfa.getTableRowArray(entry_ind-1).get(0).setAccept(true);
					nfa.getTableRowArray(entry_ind-1).get(0).setType(token);
 					int state2 = entry_ind;
 					if(peekChar()=='|'){//yet another UNION
 						rexpprime();//second subnfa;
 						union(epsilon,state1,state2);
 						return true;
 					}
 				}
 			}
 			else
 				return false;
 		}
 		toggleEpsilon=true;;
 		return true;
 	}
 	
 	private boolean rexp1(){
 		if(DEBUG)System.out.println("rexp1()");
 		if(rexp2()){
 			if(toggleStar){
 				toggleStar=false;
 				concat(entry_ind-4,entry_ind-3);
 			}
 			else{
 				concat(entry_ind-3,entry_ind-2);
 			}
 			if(rexp1prime()){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private boolean rexp1prime(){
 		if(DEBUG)System.out.println("rexp1prime()");
 		if(index>=regex.length()||peekChar()==')'){
 			return true;
 		}
 		if(rexp2()){
 			if(toggleStar){
 				toggleStar=false;
 				concat(entry_ind-4,entry_ind-3);
 			}
 			else if(togglePlus){
 				togglePlus=false;
 				concat(entry_ind-3,entry_ind-2);
 			}
 			else{
 				concat(entry_ind-3,entry_ind-2);
 			}
 			if(!toggleEpsilon&&rexp1prime()){
 				toggleEpsilon = false;
 				return true;
 			}
 		}
 		toggleEpsilon=true;
 		return true;
 	}
 	
 	private boolean rexp2(){
 		if(DEBUG)System.out.println("rexp2()");
 		int state1 = entry_ind-1;
 		if(peekChar()=='('){
 			if(match('(')&&rexp()&&match(')')){
 				int state2 = entry_ind-2;
 				rexp2_tail();
 				if(toggleStar){
 					return true;
 				}
 				else if(togglePlus){
 					concat(entry_ind-1,state1);
 					concat(state1,state1+1);
 					return true;
 				}
 				else{
 					concat(state1,state1+1);
 					toggleEpsilon=false;
 					return true;
 				}
 			}
 		}
 		if(isRE_CHAR(peekChar())){
 			char temp = peekChar();
 			if(match(peekChar())){
 				populate(String.valueOf(temp));
 				int state2 = entry_ind-2;
 				rexp2_tail();
 				if(toggleEpsilon){
 					toggleEpsilon = false;
 					return true;
 				}
 				else if(toggleStar){
 					concat(state1,state2);
 					toggleStar = false;
 					return true;
 				}
 				else if(togglePlus){
 					concat(entry_ind-1,state1);
 					togglePlus = false;
 					return true;
 				}
 			}
 		}
 		if(rexp3()){
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean rexp2_tail(){
 		if(DEBUG)System.out.println("rexp2_tail()");
 		if(index>=regex.length()||peekChar()==')'){
 			return true;
 		}
 		if(peekChar()=='*'){
 			//System.out.println("Index at Star: "+entry_ind);
 			match('*');
 /*			TableRow nextRow = new TableRow(new HashMap<String,ArrayList<TableRow>>(), Integer.toString(entry_ind), "Invalid Type");
 			nfa.add(nextRow, entry_ind);
 			entry_ind++;
 			concat(entry_ind-3,entry_ind-1);
 			concat(entry_ind-2,entry_ind-3);
 			concat(entry_ind-2,entry_ind-1);
 */			toggleStar=true;
 			return true;
 		}
 		if(peekChar()=='+'){
 			match('+');
 //			concat(entry_ind-1,entry_ind-2);
 			togglePlus=true;
 			return true;
 		}
 		else{
 			toggleEpsilon = true;
 			return true;
 		}
 	}
 	
 	private boolean rexp3(){
 		if(DEBUG)System.out.println("rexp3()");
 		if(index>=regex.length()||peekChar()==')'){
 			return true;
 		}
 		if(char_class()){
 			return true;
 		}
 		toggleEpsilon = true;
 		if(DEBUG)System.out.println(toggleEpsilon);
 		return true;
 	}
 	
 	private boolean char_class(){
 		if(DEBUG)System.out.println("char_class()");
 		if(peekChar()=='.'){
 			match('.');
 			populate(".");
 			return true;
 		}
 		if(peekChar()=='['){
 			if(match('[')&&char_class1())
 				return true;
 		}
 		String temp = defined_class();
 		if(temp!=null){
 			populate(temp);
 			return true;
 		}
 		return false;
 	}
 	
 	private boolean char_class1(){
 		if(DEBUG)System.out.println("char_class1()");
 		if(char_set_list())
 			return true;
 		if(exclude_set())
 			return true;
 		return false;
 	}
 	
 	private boolean char_set_list(){
 		if(DEBUG)System.out.println("char_set_list()");
 		if(char_set()&&char_set_list()){
 			return true;
 		}
 		if(peekChar()==']'){
 			if(match(']'))
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean char_set(){
 		if(DEBUG)System.out.println("char_set()");
 		if(isCLS_CHAR(peekChar())){
 			if(match(peekChar())&&char_set_tail())
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean char_set_tail(){
 		if(DEBUG)System.out.println("char_set_tail()");
 		if(index>=regex.length()||peekChar()==')'){
 			return true;
 		}
 		if(peekChar()=='-'){
 			if(match('-')&&isCLS_CHAR(peekChar())){
 				match(peekChar());
 				return true;
 			}
 		}
 		toggleEpsilon = true;
 		return true;
 	}
 	
 	private boolean exclude_set(){
 		if(DEBUG)System.out.println("exclude_set()");
 		if(peekChar()=='^'){
 			if(match('^')&&char_set()&&match(']')&&match('I')&&match('N')&&exclude_set_tail())
 				return true;
 		}
 		return false;
 	}
 	
 	private boolean exclude_set_tail(){
 		if(DEBUG)System.out.println("exclude_set_tail()");
 		if(peekChar()=='['){
 			if(match('[')&&char_set()&&match(']'))
 				return true;
 		}
 		String temp = defined_class();
 		if(temp!=null){
 			return true;
 		}
 		return false;
 	}
 
 	private String defined_class(){
 		if(DEBUG)System.out.println("define_class()");
 		String token = "";
 		if(peekChar()=='$'){
 			token+='$';
 			match('$');
 			while(isUpper(peekChar())){
 				token+=peekChar();
 				match(peekChar());
 			}
 			if(DEBUG)System.out.println(token);
 			return token;
 		}
 		return null;
 	}
 	
 	private char peekChar(){
 		if(index>=regex.length())
 			return '\0';
 		if(DEBUG)System.out.println(regex.charAt(index));
 		return regex.charAt(index);
 	}
 	
 	private char getChar(){
 		char result = regex.charAt(index);
 		index++;
 		return result;
 	}
 	
 	private boolean match(char c){
 		if(peekChar()==c){
 			if(DEBUG)System.out.printf("Consumed: %c\n",getChar());
 			else{
 				getChar();
 			}
 			return true;
 		}
 		
 		else{
 			System.out.printf("Error: Expected %c but found %c\n", c,peekChar());
 			return false;
 		}
 	}
 	
 	private boolean isRE_CHAR(char c){
 		if(c>=0x20&&c<=0x7E){
 			if(c!='\\'&&c!='*'&&c!='+'&&c!='?'&&c!='|'&&c!='['&&c!=']'&&c!='('&&c!=')'&&c!='.'&&c!='\''&&c!='\"'&&c!='$'&&c!='@'){
 				return true;
 			}
 			else if(c=='\\'){
 				match('\\');
 				char t = peekChar();
 				if(t==' '||t=='\\'||t=='*'||t=='+'||t=='?'||t=='|'||t=='['||t==']'||t=='('||t==')'||t=='.'||t=='\''||t=='\"'||t=='$'||t=='@'){
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private boolean isCLS_CHAR(char c){
 		if(c>=0x20&&c<=0x7E){
 			if(c!='\\'&&c!='^'&&c!='-'&&c!='['&&c!=']'&&c!='@'){
 				return true;
 			}
 			else if(c=='\\'){
 				match('\\');
 				char t = peekChar();
 				if(t=='\\'||t=='^'||t=='-'||t=='['||t==']'||t=='@'){
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	private boolean isUpper(char c){
 		return c>='A'&&c<='Z';
 	}
 	
 	private void populate(String c){
 		Map<String,ArrayList<TableRow>> trans = new HashMap<String,ArrayList<TableRow>>();
 		TableRow nextRow = new TableRow(new HashMap<String,ArrayList<TableRow>>(), Integer.toString(entry_ind+1), "Invalid Type");
 		nfa.add(null, entry_ind);
 		nfa.add(nextRow, entry_ind+1);
 		trans.put(c, nfa.getTableRowArray(entry_ind+1));
 		nfa.addState(trans, Integer.toString(entry_ind), "Invalid Type", entry_ind, false);
 		entry_ind+=2;
 	}
 	
 	private void concat(int x, int y){
 //		if(entry_ind>2){
 			if(DEBUG)System.out.println("Concated: "+x+" & "+y);
 			ArrayList<TableRow> curr = nfa.getTableRowArray(x);
 			ArrayList<TableRow> next = nfa.getTableRowArray(y);
 			Map<String,ArrayList<TableRow>> nextStates = curr.get(0).getSuccessorStates();
 			if(nextStates.get("@")!=null){
 				nextStates.get("@").add(next.get(0));
 			}
 			else{
 				nextStates.put("@", next);
 			}
 			curr.get(0).setSuccessorStates(nextStates);
 //		}
 	}
 	
 	private void union(int epsilon, int state1, int state2){
 		if(DEBUG)System.out.println("United: "+state1+" & "+ state2);
 		populate("@");
 		ArrayList<TableRow> first_next = nfa.getTableRowArray(state1);
 		ArrayList<TableRow> second_next = nfa.getTableRowArray(state2);
 		//glue to epsilon
 		nfa.getTableRowArray(epsilon).get(0).getSuccessorStates().put("@",first_next);
 		nfa.getTableRowArray(epsilon).get(0).getSuccessorStates().get("@").add(second_next.get(0));
 		//merge to epsilon
 		concat(state2-1,entry_ind-2);
 		concat(entry_ind-3,entry_ind-2);
 //		if(first_next.get(0).getSuccessorStates().get("@")==null){
 //			first_next.get(0).getSuccessorStates().put("@",nfa.getTableRowArray(entry_ind-2));
 //		}
 //		else{
 //			first_next.get(0).getSuccessorStates().get("@").add(nfa.getTableRowArray(entry_ind-2).get(0));
 //		}
 //		if(second_next.get(0).getSuccessorStates().get("@")==null){
 //			second_next.get(0).getSuccessorStates().put("@",nfa.getTableRowArray(entry_ind-2));
 //		}
 //		else{
 //			second_next.get(0).getSuccessorStates().get("@").add(nfa.getTableRowArray(entry_ind-2).get(0));
 //		}
 	}
 }
