 package org.silix.the9ull.microbit.model;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigDecimal;
 import java.net.Authenticator;
 import java.net.ConnectException;
 import java.net.PasswordAuthentication;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.stringtree.json.JSONReader;// test only
 import org.stringtree.json.JSONValidatingReader;
 import org.stringtree.json.JSONWriter;
 
 import com.googlecode.jj1.ServiceProxy;
 
 public class Bitcoin {
 
 	static String rpcuser;
 	static String rpcpassword;
 	static private BigDecimal fee = null; 
 	
 	private int minconf = 2;
 	private ServiceProxy proxy;
 
 	public Bitcoin(boolean authentication) throws ConnectException {
 		
 		System.out.println("new Bitcoin()");
 	
 		if(authentication)
 			try {
 				if(!Bitcoin.readRpcCredentials()){
 					System.out.println("Credential not found");
 				}
 			} catch (IOException e) {
 				System.out.println("Credential not found");
 			}
 		
 		System.out.println("Cred:"+rpcuser+" "+rpcpassword);
 		
 		// Set Authenticator
 		Authenticator.setDefault(new Authenticator() {
 		    protected PasswordAuthentication getPasswordAuthentication() {
 		        return new PasswordAuthentication (rpcuser, rpcpassword.toCharArray());
 		    }
 		});
 		
 		proxy = new ServiceProxy("http://localhost:8332");
 		System.out.println("Bitcoin: init done");
 	}
 	
 	boolean validateaddress(String address) {
 		@SuppressWarnings("unchecked")
 		HashMap<String,Object> result = (HashMap<String,Object>) proxy.call("validateaddress",address);
 		//System.out.println(result.toString());
 		//System.out.println(""+result.get("isvalid"));
 		
 		return (Boolean)result.get("isvalid");
 	}
 	
 	public String sendtoaddress(String address, BigDecimal amount) {
 		return (String) proxy.call("sendtoaddress", address, amount, "From Microbit (com.googlecode.jj1), sendtoaddress");
 	}
 	public String sendtoaddress(int user_id, String address, BigDecimal amount) {
 		return (String) proxy.call("sendfrom", "user"+user_id, address, amount);
 	}
 	
 	
 	String getnewaddress(String account) {
 		String result;
 		if(account==null)
 			result = (String) proxy.call("getnewaddress");
 		else
 			result = (String) proxy.call("getnewaddress", account); 
 		return result;
 	}
 	
 
 	double getbalance(String account) {
 		return (Double) proxy.call("getbalance",account,minconf);
 	}
 	
 	String getblockhash(long index) {
 		return (String) proxy.call("getblockhash",index);
 	}
 	
 	@SuppressWarnings("unchecked")
 	Map<String,Object> listsinceblock(String hash) {
 		Map<String,Object> result; // "transactions" : {tx obj (see getTx)}, "lastblock" : "hash"
 		if(hash==null)
 			result = (Map<String,Object>)proxy.call("listsinceblock");
 		else
 			result = (Map<String,Object>)proxy.call("listsinceblock", hash);
 		return result;
 	}
 	
 	@SuppressWarnings("unchecked")
 	Map<String,Object> getblock(String hash){
 		return (Map<String,Object>)proxy.call("getblock",hash);
 	}
 	Map<String,Object> getblock(long index){
 		return getblock(getblockhash(index));
 	}
 	
	public BigDecimal getbalance() {
		return new BigDecimal((Double)proxy.call("getbalance"));
	}
	
	@Deprecated
 	public BigDecimal getbalanceall() {
 		BigDecimal balance = new BigDecimal(0.0);
 		for(double d: listaccounts().values()){
			System.out.println("!!! "+d);
 			balance = balance.add(new BigDecimal(d));
			System.out.println("!!! Balance "+balance);
 		}
 		return balance.setScale(8,BigDecimal.ROUND_HALF_DOWN);
 	}
 	
 	
 	@SuppressWarnings("unchecked")
 	HashMap<String,Double> listaccounts() {
 		return (HashMap<String,Double>) proxy.call("listaccounts",minconf);
 	}
 	
 	void movetoaccount(BigDecimal amount, int user_id) {
 		proxy.call("move","","user"+user_id,amount);
 	}
 	
 	@SuppressWarnings("unchecked")
 	Collection<Map<String,Object>> listtransactions() {
 		return (Collection<Map<String,Object>>) proxy.call("listtransactions","",50);
 	}
 
 	@SuppressWarnings("unchecked")
 	Collection<Map<String,Object>> listtransactions(int user_id, int count, int from) {
 		return (Collection<Map<String,Object>>) proxy.call("listtransactions","user"+user_id,count,from);
 	}
 	Collection<Map<String,Object>> listtransactions(int user_id) {
 		return listtransactions(user_id, 50, 0);
 	}
 
 	@SuppressWarnings("unchecked")
 	Map<String,Object> lasttransaction(int user_id) {
 		Collection<Map<String,Object>> ret = (Collection<Map<String,Object>>) proxy.call("listtransactions","user"+user_id,1);
 		if(ret==null || ret.isEmpty())
 			return null;
 		return new ArrayList<Map<String, Object>>(ret).get(0);
 	}
 	
 	public long getblockcount() {
 		return (Long) proxy.call("getblockcount");
 	}
 	
 	public void test() {
 		//System.out.println(proxy.call("setgenerate",false));
 	}
 	
 	public static boolean readRpcCredentials() throws IOException {
 	
 		String home = System.getProperty("user.home");
 		
 	    FileInputStream fstream = new FileInputStream(home+"/.bitcoin/bitcoin.conf");
 	    DataInputStream in = new DataInputStream(fstream);
 	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
 	    String strLine;
 	    boolean bu=false, bp=false;
 	    while ((strLine = br.readLine()) != null){
 	    	if(strLine.startsWith("rpcuser=")){
 	    		rpcuser = strLine.substring(8);
 	    		bu=true;
 	    	} else if(strLine.startsWith("rpcpassword=")) {
 	    		rpcpassword = strLine.substring(12);
 	    		bp=true;
 	    	}
 	    }
 	
 	    in.close();
 		
 	    //System.out.println(rpcuser+" "+rpcpassword);
 		
 	    return bu && bp;
 	}
 
 	public static Object jsonToJava(String json) {
 		//debug
 		JSONReader reader = new JSONValidatingReader();
 		Object result = reader.read(json);
 		
 		//System.out.println("JSONReader result is " + result +
 		//		" of class " + result.getClass());
 		
 		return result;
 	}
 	public static String javaToJson(Object o){
 		//debug
 		JSONWriter writer = new JSONWriter();
 		return writer.write(o);
 	}
 	
 	/**
 	 * @param args
 	 * @throws ConnectException 
 	 */
 	public static void main(String[] args) {
 		
 		try {
 			Bitcoin.readRpcCredentials();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		
 		Bitcoin bc = null;
 		try {
 			bc = new Bitcoin(false);
 		} catch (ConnectException e) {
 			System.out.println("Bitcoin server connection problem");
 			return;
 		}
 		
 		//System.out.println(bc);
 		//System.out.println( bc.validateaddress("n2NAjt6oZBPZY5bhTGEBHkENTDtrpYc7fS") );
 		//System.out.println( bc.validateaddress("14UWwaX1BdCHEfXQF9KqivMmK3EbWjHDoV") );
 		//System.out.println(bc.listaccounts());
 		//System.out.println(bc.getb`ckcount());
 		//System.out.println(bc.listtransactions());
 		//System.out.println(bc.getnewaddress(null));
 		
 
 		//System.out.println(bc.getbalanceall());
 		
 		Random r = new Random();
 		//Tx tx = bc.sendtoaddress("mrZQpbfo4RqEoBeJw4u5E9u6CEJsycjEM5", new BigDecimal(r.nextFloat()/1000.0));
 		
 		
 		//System.out.println(""+tx.getAmount().multiply(new BigDecimal(-1))+" + "+
 		//		tx.getFee().multiply(new BigDecimal(-1))+" = "+
 		//		tx.getAmount().add(tx.getFee()).multiply(new BigDecimal(-1)));
 		//System.out.println(bc.getbalanceall());
 		
 		
 		//bc.updatefunds(false);
 
 		System.out.println(bc.sendtoaddress("mrZQpbfo4RqEoBeJw4u5E9u6CEJsycjEM5", new BigDecimal("0.2")));
 		
 		//System.out.println(Bitcoin.jsonToJava("{\"minconf\":\"2\"}"));
 		
 		  
 	}
 	
 
 }
