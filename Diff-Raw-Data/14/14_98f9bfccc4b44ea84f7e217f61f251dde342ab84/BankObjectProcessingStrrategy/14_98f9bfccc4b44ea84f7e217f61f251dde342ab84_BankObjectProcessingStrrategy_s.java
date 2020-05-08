 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package week2;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.lang.reflect.Method;
 import java.util.*;
 
 /**
  *
  * @author Lars Rasmussen
  */
 public class BankObjectProcessingStrrategy implements ObjectProcessingStrategy{
 	Map<String,List<Object>> objects;
 	
 	public BankObjectProcessingStrrategy(){
 		// Create available object list
		objects = new HashMap<>();
		List banks = new ArrayList<>();
 		BankInterface firstBank = new Bank();
 		banks.add(firstBank);
 		objects.put("Bank", banks);
		List accounts = new ArrayList<>();
 		objects.put("Account", accounts);
 	}
 	
 	@Override
 	public boolean process(String[] path, Map<String, String> map, OutputStream out) {
 		String[] pathParts = path[0].split("\\/");
 		String cls = pathParts[1];
 		if(!objects.containsKey(cls))
 			return false;
 		String method = pathParts[3];
 		
 		/* Create needed variables */
 		int id;
 		String code = "200 OK";
 		String body = "";
 		
 		// Get the specified ID
 		try{
 			id = Integer.parseInt(pathParts[2]);
 		}catch(NumberFormatException e){
 			body = "Invalid request ID. The request should be formatted like this: /type/id/method?args";
 			code = "400 Bad Request";
 
 			writeOutput(code, body, out);
 			return false;
 		}
 		
 		
 		// Test what object we're working with
 		if(cls.equals("Bank")){
 			if(id >= objects.get("Bank").size()){
 				body = "No such bank.";
 				code = "404 Not Found";
 				
 				writeOutput(code, body, out);
 				return false;
 			}
 			
 			// Get the Bank object
 			Bank bank = (Bank)objects.get("Bank").get(id);
 			if(method.equals("getAccount")){
 				// do some more stuff here
 				Account account = bank.getAccount(map.get("name"));
 				
 				if(objects.get("Account").contains(account)){
 					body = "" + objects.get("Account").indexOf(account);
 				}
 				else{
 					objects.get("Account").add(account);
 					body = "" + (objects.get("Account").size()-1);
 					code = "201 Created";
 				}
 			}
 		}else if(cls.equals("Account")) {
 			if(id >= objects.get("Account").size()){
 				body = "No such account.";
 				code = "404 Not Found";
 				
 				writeOutput(code, body, out);
 				return false;
 			}
 			
 			Account account = (Account)objects.get("Account").get(id);
 			if(method.equals("getName")){
 				body = account.getName();
 			}
 			else if(method.equals("getBalance")) {
 				body = "" + account.getBalance();
 			}
 			else if(method.equals("deposit")){
 				account.deposit(Double.parseDouble(map.get("amount")));
 				body = "Deposit successful.";
 			}
 			else if(method.equals("withdraw")){
 				account.withdraw(Double.parseDouble(map.get("amount")));
 				body = "Withdraw successful.";
 			}
 			
 		}
 		
 		if(body.isEmpty()){
 			code = "400 Bad Request";
 			body = "Request not understood.";
 			writeOutput(code, body, out);
 			
 			return false;
 		}
 		
 		return writeOutput(code, body, out);
 	}
 	
 	private boolean writeOutput(String code, String body, OutputStream out){
 		StringBuilder output = new StringBuilder();
 		output.append("HTTP/1.0 ");
 		output.append(code);
 		output.append("\r\n");
 		output.append("Content-Type: text/plain\r\n");
 		output.append("Date: ");
 		output.append(new Date());
 		output.append("\r\n");
 		output.append("Server: dDist HTTPServer 1.0\r\n\r\n");
 		output.append(body);
 		
 		try{
 			out.write(output.toString().getBytes());
 			return true;
 		}catch(IOException e){
 			return false;
 		}
 	}
 
 	@Override
 	public String getContentType() { return "text/html"; }
 	
 }
