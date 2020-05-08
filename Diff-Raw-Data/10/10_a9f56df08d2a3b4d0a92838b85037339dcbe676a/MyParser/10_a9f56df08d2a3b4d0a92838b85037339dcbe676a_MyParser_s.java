 import java.io.*;
 import java.util.*;
 
 
 public class MyParser {
 	static ArrayList<Vector> parseNumCSV(String filename)
 	{
 		ArrayList<Vector> answer = new ArrayList<Vector>();
 		
 		FileInputStream fstream = null;
 		try {
 			fstream = new FileInputStream(filename);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.err.println("File Not Found.");
 		}
 		
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String strLine = null;
 		
 		try {
 			while ((strLine = br.readLine()) != null)
 				{
 					Vector tempVec = stringToVec(strLine);
 					if(tempVec != null)
 					{
 						answer.add(tempVec);
 					}
 					
 				}
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.err.println("Exception while reading file.");
 		}
 		
 
 		return answer;
 	}
 
 	static Vector stringToVec(String s){
 		s = s.replaceAll("^\\s*,", "0,");
 		s = s.replaceAll(",{2}", ",0,");
 		s = s.replaceAll(",{2}", ",0,");
 		s = s.replaceAll(",\\s*$", ",0");
 
 		boolean empyString = true;
 		Vector answer = new Vector();
 
 		StringTokenizer st = new StringTokenizer(s,",");
 
 		while (st.hasMoreTokens()) {
 			empyString = false;
 			try{
 				answer.addElement(Double.parseDouble(st.nextToken()));
 				empyString = false;
 			}catch(java.lang.NumberFormatException e){
 				answer.addElement(0.0);
 			}
 		}
 		
 		if(empyString){
 			return null;
 		}
 		return answer;
 	}
 	
 	static ArrayList<TextToken> parseTextFile(String filename)
 	{
 		ArrayList<TextToken> answer = new ArrayList<TextToken>();
 		
 		FileInputStream fstream = null;
 		try {
 			fstream = new FileInputStream(filename);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.err.println("File Not Found.");
 		}
 		
 		DataInputStream in = new DataInputStream(fstream);
 		BufferedReader br = new BufferedReader(new InputStreamReader(in));
 		String strLine = null;
 		
 		try {
 			while ((strLine = br.readLine()) != null)
 			{
 				//for each line...
 				//System.out.println("Now processing: "+ strLine);
 				answer = stringToTextTokens(strLine,answer);
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 			System.err.println("Exception while reading file.");
 		}
 		
 		return answer;
 	}
 
 	static ArrayList<TextToken> stringToTextTokens(String s, ArrayList<TextToken> tokens){
 		if(s.isEmpty() && !(tokens.get(tokens.size()-1) instanceof EOP)){
 			if(!(tokens.get(tokens.size()-1) instanceof EOS))
 			{
 				tokens.add(new EOS());
 				tokens.add(new EOP());
 			}
 			else
 			{
 				tokens.add(new EOP());
 			}
 		}
 		else
 		{
 			StringTokenizer st = new StringTokenizer(s, " ");
 			while (st.hasMoreTokens()) {
 				String currChunk = st.nextToken();
 				ArrayList<TextToken> bufferTokens = new ArrayList<TextToken>();
 				boolean done = false;
 				while(!done)
 				{
					
					if(currChunk.length() >= 4 && 
 							currChunk.substring(currChunk.length()-4, currChunk.length()).compareTo("....") == 0)
 					{
 						bufferTokens.add(new Chars("..."));
 						bufferTokens.add(new Chars("."));
 						currChunk = currChunk.substring(0,currChunk.length()-4);
 					}
 					else if(currChunk.length() >= 3 && 
 							currChunk.substring(currChunk.length()-3, currChunk.length()).compareTo("...") == 0)
 					{
 						bufferTokens.add(new Chars("..."));
 						currChunk = currChunk.substring(0,currChunk.length()-3);
 					}
					else if(currChunk.charAt(0) == '-' && currChunk.length() ==1)
 					{
 						tokens.add(new Chars("-"));
 						break;
 					}
 					else if(currChunk.charAt(0) == ':')
 					{
 						tokens.add(new Chars(":"));
 						currChunk = currChunk.substring(1,currChunk.length());
 					}
 					else if(currChunk.charAt(0) == '(')
 					{
 						tokens.add(new Chars("("));
 						currChunk = currChunk.substring(1,currChunk.length());
 					}
 					else if(currChunk.charAt(0) == '’')
 					{
 						tokens.add(new Chars("\'"));
 						currChunk = currChunk.substring(1,currChunk.length());
 					}
 					else if(currChunk.charAt(0) == '"')
 					{
 						tokens.add(new Chars("\""));
 						currChunk = currChunk.substring(1,currChunk.length());
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == '"')
 					{
 						bufferTokens.add(new Chars("\""));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == '’')
 					{
 						bufferTokens.add(new Chars("’"));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == ')')
 					{
 						bufferTokens.add(new Chars(")"));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == ':')
 					{
 						bufferTokens.add(new Chars(":"));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == ';')
 					{
 						bufferTokens.add(new Chars(";"));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == ',')
 					{
 						bufferTokens.add(new Chars(","));
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == '.')
 					{
 						bufferTokens.add(new Chars("."));
 						bufferTokens.add(new EOS());
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == '?')
 					{
 						bufferTokens.add(new Chars("?"));
 						bufferTokens.add(new EOS());
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else if(currChunk.charAt(currChunk.length()-1) == '!')
 					{
 						bufferTokens.add(new Chars("!"));
 						bufferTokens.add(new EOS());
 						currChunk = currChunk.substring(0,currChunk.length()-1);
 					}
 					else
 					{
 						done = true;
 						tokens.add(new Words(currChunk));
 						while(!bufferTokens.isEmpty())
 						{
 							tokens.add(bufferTokens.get(0));
 							bufferTokens.remove(0);
 						}
 					}
 				}				
 			}
 		}
 		return tokens;
 	}
 }
