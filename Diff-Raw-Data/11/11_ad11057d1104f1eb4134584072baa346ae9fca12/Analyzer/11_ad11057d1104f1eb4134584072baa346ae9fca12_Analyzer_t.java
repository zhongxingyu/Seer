 package ids;
 
 import java.io.FileInputStream;
 import java.util.Vector;
 import jpcap.packet.Packet;
 import jpcap.packet.TCPPacket;
 import alice.tuprolog.*;
 import alice.tuprolog.Long;
 
 public class Analyzer extends Thread{ 
 	
 	private Prolog engine;
 	private Vector<TCPPacket> packets = new Vector<TCPPacket>();
 	
 	Analyzer(Prolog e) {this.engine = e;}
 	
 	public synchronized void retractPackets() {
 		
 		for (int i = 0; i < packets.size(); i++) {
 			Term retractPacket = createPacket(packets.get(i));
 			
 			Term retract_query = new Struct("retract",retractPacket );
 			System.out.println(retract_query);
 			try{
 		    	SolveInfo solve = engine.solve(retract_query);
 		    	Term solution = solve.getSolution();
 		    	System.out.println(solution);
 		    }
 		    catch(Exception e){}
 			
 			
 		}
 		
 		
 	}
 	
 	public Term createPacket(TCPPacket tcp_packet){
 		
 	    Term t1 = new Int(tcp_packet.src_port);
 	    Term t2 = new Int(tcp_packet.dst_port);
 	    Term t3 = null;
 	    Long t7 = null;
 	    if (tcp_packet.syn)
 	    	t3 = new Struct("syn");
 	    
 	    if(tcp_packet.rst){
 	    	t3 = new Struct("rst");
 	    
 	    }
 	    Term t4 = new Struct(tcp_packet.src_ip.toString());
 		
 	    Term t5 = new Struct(tcp_packet.dst_ip.toString());
 	    
 		Long t6 = new Long(tcp_packet.sequence);
   
 	    
 	    if(tcp_packet.ack){
 	    	t7 = new Long(tcp_packet.ack_num);
 	    }
 	      
 	    else
 	    	t7 = new Long(0);
 	    
 	    Term[] arg_t;
 	    
 	    if (t3 != null){
 	    	Term[] arg_temp = { t1,t2,t3,t4,t5,t6,t7 };
 		    arg_t = arg_temp;
 	    }
 	    else{
 		   	Term[] arg_temp = { t1,t2,t4,t5,t6,t7 };
 		    arg_t = arg_temp;
 	    }
 	    
     	Term pair = new Struct( "pacchetto", arg_t );
     	return pair;
 		
 	}
 	
 	
 	
 	public synchronized void assertPacket(Packet p){
 		
 		if(p instanceof TCPPacket){
 			
 			TCPPacket tcp_packet = (TCPPacket)p;
 			packets.add(tcp_packet);
 
 			Term packet = createPacket(tcp_packet);
 
 		    Term assert_query = new Struct("assert",packet );
 	
 		    try{
 		    	SolveInfo solve = engine.solve(assert_query);
 		    	Term solution = solve.getSolution();
 		    	System.out.println(solution);
 		    	
 		    	
 	    	}
 	    	catch(Exception e){
 	    		e.printStackTrace();
 	    		
 	    	}
 	    	
 		}
 	}
 	
 	/* la stringa indica porta_chiusa o connessione_tcp */
 	public String createStringScan(Integer n,String scan,String s){
 		
 		
 		String t1 = scan;
 		String t2 = "";
 		String n1 = "";
 		String n2 = "";
 		
 		for (int i = 1; i < n; i+=2) {
 			
 			n1 = "A"+(i);
 			n2 = "A"+(i+1);
 		
 			if (i%2==1){
 			  t2 += s + "(X,Y,"+n1+","+n2+"),";
 			  t2 += n1 + "\\=" + n2 + ",";
 			  
 			  
 			}
 			
 			
 			
 		}
 		
 
 		for(int i = 2; i < n; i+=2){
 			
 			for(int j = i; j < n; j+=2){
 				n1 = "A"+(i);
 				n2 = "A"+(j+2);
 				
 				t2 += n1 + "\\=" + n2 + ",";
 			}
 			
 		}
 		
 		t1 += t2.substring(0, t2.length()-1) + "."; //t2;
 
 		return t1;
 
 		
 	}
 	
 	
 	
 	public void createRuleTcpScan(int connCount,int closedCount){
 		
 		try {
 		
 			String generated_rule = createStringScan(closedCount,"tcp_scan(X,Y):- ","porta_chiusa");
 			String generated_rule2 = createStringScan(connCount,"tcp_scan(X,Y):- ","connessione_tcp");
 			System.out.println(generated_rule);
 			System.out.println(generated_rule2);
 
 			String generated_rule3 = createStringScan(connCount,"syn_scan(X,Y):- ","connessione_syn");
 			System.out.println(generated_rule3);
 			Theory rule = new Theory(generated_rule);
 			Theory rule2 = new Theory(generated_rule2);
 			Theory rule3 = new Theory(generated_rule3);
 			engine.addTheory(rule);
 			engine.addTheory(rule2);
 			engine.addTheory(rule3);
 		
 			
 		} catch (InvalidTheoryException e) {
 		}
 		
 		
 		
 		
 		
 	}
 	
 	
 	
 
 	/* legge da file kb.pl e crea stringa e aggiunge la regola con n premesse */
 	
 	public void initializeKB(String file,int connCount,int closedCount){
 		 	
 		
 		try { 
 			Theory kb = new Theory(new FileInputStream(file));
 			engine.setTheory(kb);
 			createRuleTcpScan(connCount,closedCount);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 
 		}
 
 		
 		
 		
 	}
 	
 	
 	public boolean query(){
 		
 		Term args2[] = { 
 			new Var("X"),
 			new Var("Y"),
 		};
 		/*
 		Term args3[] = {
 		new Var("SOURCE"),
 		new Var("DESTINATION"),
 		new Var("SD"),
 		new Var("DP"),
 		};		
 		*/
 
 		Term query = new Struct("tcp_scan",args2);
 		Term query2 = new Struct("syn_scan",args2);
 		/*Term query3 = new Struct("connessione_syn",args3);
 
 	    try {
 	
 		SolveInfo info = engine.solve(query3);
 		while (info.isSuccess()){
 			System.out.println("solution: "+info.getSolution()+
 			" - bindings: "+info);
 			if (engine.hasOpenAlternatives()){
 				info=engine.solveNext();
 			} else {
 			break;
 		}
 
 	     	SolveInfo solve3 = engine.solve(query3);
 	       	Term solution3 = solve3.getSolution();
 	       	System.out.println(solution3);
 		return true;
 	  }
 	}
 	    catch(Exception e){
     
 
 	        	
 	    }
 	*/
 	try{
   	
 	       	SolveInfo solve = engine.solve(query);
 	       	Term solution = solve.getSolution();
 	       	System.out.println(solution);
 		return true;
 
 	    }
 	    catch(Exception e){
 	       
 	    
 	        	
 	    }
 	
 		try{
 
 	       	SolveInfo solve2 = engine.solve(query2);
 	       	Term solution2 = solve2.getSolution();
 	       	System.out.println(solution2);
 
 	       	return true;
 	    }
 	    catch(Exception e){
 	       
 
 	        	
 	    }
 	return false;					
 	}
 
 
 }
