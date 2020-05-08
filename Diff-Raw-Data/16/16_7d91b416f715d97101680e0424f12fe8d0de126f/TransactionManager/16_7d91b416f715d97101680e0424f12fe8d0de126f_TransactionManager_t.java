 package edu.nyu.ads;
 import java.util.*;
 import java.io.*;
 public class TransactionManager {
 	PrintWriter out;
 	Map<String,List<Site>> varToSite;
 	Map<Integer,Site> sites;
 	//Map<String,Transaction> transactions;
 	Map<String,Transaction> transactions;
 	Map<String,List<Transaction>> blockedTransactions;
 	LockManager lockManager;
 	String outputFile;
 	static int timestamp;
                   
 	
 	TransactionManager(Map<String,List<Site>> varToSite,Map<Integer,Site> sites, LockManager lockManager,String outputFile) throws IOException{
 		this.varToSite = new HashMap<String,List<Site>>(varToSite);
 		this.sites = new HashMap<Integer,Site>(sites);
 		this.outputFile=outputFile;
 		this.out = new PrintWriter(new FileWriter(outputFile));
 		this.lockManager=lockManager;
 		//transactions=new HashMap<String,Transaction>();
 		this.transactions=new HashMap<String,Transaction>();
 		this.blockedTransactions=new HashMap<String,List<Transaction>>();
 	}
 	
 	void closeFile(){
 		out.close();
 	}
 	
 	void block(String variable,Transaction t,boolean read,int value,int timestamp){
 		
 		if(blockedTransactions.containsKey(variable)){
 			List<Transaction> blockedTransaction=blockedTransactions.get(variable);
			if(blockedTransaction == null){
				blockedTransaction = new ArrayList<Transaction>();
				blockedTransactions.put(variable,blockedTransaction);
			}
 			
 			for(Transaction temp: blockedTransaction){
 					if(!temp.isRead() && temp.getTimestamp()<t.getTimestamp()){
 							out.println("Transaction "+t.getName()+" aborted"+"at timestamp"+timestamp);
 							abort(t,timestamp);
 							return;
 					}
 				
 			}
 			
 			blockedTransaction.add(t);
 			t.block(value, read);
 			out.println(t+" Blocked On "+variable+" at timestamp "+timestamp );
 		}
 		
 		else{
 			ArrayList<Transaction>  newBlockedList =new ArrayList<Transaction>();
 			newBlockedList.add(t);
 			t.block(value, read);
 			blockedTransactions.put(variable,newBlockedList);
 			out.println(t+" Blocked On "+variable+" at timestamp "+timestamp );
 		}
 	}
 	
 	void abort(Transaction t,int timestamp){
 		out.println("Transaction "+t.getName()+" aborting at timestamp "+timestamp);
 		System.out.println(blockedTransactions);
 		Map<String, String> m=t.end(TransactionState.Aborted);
 		Set<String> variables=m.keySet();
 		System.out.println(variables);
 		for(Integer siteNum:sites.keySet()){
 			Site site=sites.get(siteNum);
 			site.abort(t);
 		}
 		
 		for(String v:variables){
 			if(blockedTransactions.containsKey(v)){
 				List<Transaction> blockedTransaction=blockedTransactions.get(v);
 				Transaction temp = blockedTransaction.remove(0);
 				if(blockedTransaction.size()==0)
 					blockedTransactions.remove(v);
 				Map<Boolean,Integer> map=temp.unblock();
 				if(map.containsKey(true)){
 					//true means it was a read operation that it was blocked on.
 					read(temp.getName(),v,timestamp);
 				}
 				else{
 					write(temp.getName(),v,map.get(false),timestamp);
 				}
 			}
 		}
 		transactions.remove(t);
 	}
 	
 	void abortSiteFailure(Transaction t,int timestamp,List<String> nonReplicatedVariables){
 		out.println("Transaction "+t.getName()+" aborting at timestamp "+timestamp);
 		System.out.println(blockedTransactions);
 		Map<String, String> m=t.end(TransactionState.Aborted);
 		Set<String> variables=m.keySet();
 		System.out.println(variables);
 		for(Integer siteNum:sites.keySet()){
 			Site site=sites.get(siteNum);
 			site.abort(t);
 		}
 		for(String v:variables){
 			if(!nonReplicatedVariables.contains(v)){
 				List<Transaction> blockedTransaction=blockedTransactions.get(v);
 				if(blockedTransaction!=null){
 				Transaction temp = blockedTransaction.remove(0);
 				if(blockedTransaction.size()==0)
 					blockedTransactions.remove(v);
 				Map<Boolean,Integer> map=temp.unblock();
 				if(map.containsKey(true)){
 					//true means it was a read operation that it was blocked on.
 					read(temp.getName(),v,timestamp);
 				}
 				else{
 					write(temp.getName(),v,map.get(false),timestamp);
 				}
 				}
 			}
 		}
 		
 		transactions.remove(t);
 	}
 	
 	/**
 	 * 
 	 * @param variable Variable name	
 	 * @param t Transaction that wants to lock the variable
 	 * @param read true if the lock is for "read" and false if the lock is for "write"
 	 * @param value the value to be written. This value wont matter if read is true.
 	 * @return
 	 */
 	void lock(String variable,Transaction t,boolean read,int value,int timestamp){
 		Status s=lockManager.getLock(t, variable,read);
 		if(read){
 			if(s.equals(Status.Abort)){
 				out.println("Wait Die Protocol-->3 ");
 				abort(t,timestamp);
 			}
 			if(s.equals(Status.Block)){
 				t.block(value, read);
 				out.println(t+ "is Blocked, could not get the locks but is older..");
 				block(variable,t,read,value,timestamp);
 
 				//blockedTransactions.get(variable).;
 			}
 			if(s.equals(Status.GetLock)){
 				List<Site> list=varToSite.get(variable);
 				for(Site site:list){
 					if(site.isUp()){
 						Variable v=null;
 						try{
 							v=site.readVariable(variable);
 						}
 						catch(IllegalStateException e){
 							continue;
 						}
 						site.lock(variable, t,read);
 						t.read(variable);
 						out.println(t+ " READS variable "+variable+"."+site.getSiteNumber()+" = "+ v.getValue()+ "at timestamp" + timestamp );
 						return;
 					}
 				}
 				out.println("All sites with this variable("+variable+") are down-->");
 				block(variable,t,read,value,timestamp);
 			//	blockedTransactions.put(variable,t);
 			}
 		}
 		else{
 			if(s.equals(Status.Abort)){
 				out.println("Wait Die Protocol-->");
 				abort(t,timestamp);
 			}
 			if(s.equals(Status.Block)){
 				t.block(value, read);
 
 				out.println(t+ "is Blocked, could not get the locks but is older..");
 
 				block(variable,t,read,value,timestamp);
 
 				//blockedTransactions.put(variable, t);
 			}
 			if(s.equals(Status.GetLock)){
 				List<Site> list=varToSite.get(variable);
 				boolean wrote=false;
 				for(Site site:list){
 					if(site.isUp()){
 						site.lock(variable, t,read);
 						site.writeVariable(variable, value);
 						t.write(variable, value);
 						out.println(t+ " WRITES "+value+" to  variable "+variable+"."+site.getSiteNumber()+ " at timestamp " + timestamp );
 						wrote=true;
 					}
 				}
 				if(!wrote){
 					out.println("All sites with this variable("+variable+") are down-->");
 					block(variable,t,read,value,timestamp);
 					//blockedTransactions.put(variable,t);
 				}
 			}
 		}
 		
 	}
 	
 	void begin(String s,int timestamp){
 		Transaction temp=new ReadWriteTransaction(s, timestamp);
 		transactions.put(s,temp);
 		out.println("BEGIN READWRITE "+temp);
 	}
 	
 	void beginRO(String s,int timestamp){
 		
 		Map<String,Variable> variables=new HashMap<String,Variable>();
 		for(String var:varToSite.keySet()){
 			
 			List<Site> siteList=varToSite.get(var);
 			
 			FOR: for(Site sit:siteList){
 				if(sit.isUp()){
 					Variable v=null;
 					try{
 						v=sit.readVariableBackup(var);	
 						
 					}
 					catch(IllegalStateException e){
 					continue;
 					}
 					Variable newVar = new Variable(v);
 					variables.put(newVar.getName(),newVar);
 					break FOR;
 				}
 				
 			}
 			
 			//check variables map for the entry of this variable
 			//if not found, do what prof says...(doubt)
 		}
 		Transaction temp=new ReadOnlyTransaction(s,timestamp,variables);
 		transactions.put(s, temp);
 		out.println("BEGIN READONLY "+temp);
 	}
 	
 	boolean end(String s,int timestamp){
 		if(transactions.get(s).getState().equals(TransactionState.Aborted)){
 			transactions.get(s).end(TransactionState.Aborted);
 			out.println("Transaction "+transactions.get(s)+"already aborted");
 			return false;
 			
 		}
 		
 		if(transactions.containsKey(s)){
 			for(Integer siteNum: sites.keySet()){
 				sites.get(siteNum).commit(transactions.get(s));
 			}
 		}
 		if(transactions.get(s).getType().equals(TransactionType.ReadOnly)){
 			transactions.get(s).end(TransactionState.Commited);
 			out.println("END transaction"+transactions.get(s));
 			return false;
 			
 		}
 		transactions.get(s).end(TransactionState.Commited);
 		out.println("END transaction"+transactions.get(s));
 		Map<String, String> m=transactions.get(s).end(TransactionState.Commited);
 		Set<String> variables=m.keySet();
 		for(String v:variables){
 			if(blockedTransactions.containsKey(v)){
 				List<Transaction> blockedTransaction=blockedTransactions.get(v);
 				Transaction temp = blockedTransaction.remove(0);
 				if(blockedTransaction.size()==0)
 					blockedTransactions.remove(v);
 				Map<Boolean,Integer> map=temp.unblock();
 				if(map.containsKey(true)){
 					//true means it was a read operation that it was blocked on.
 					read(temp.getName(),v,timestamp);
 				}
 				else{
 					write(temp.getName(),v,map.get(false),timestamp);
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	boolean read(String transaction, String variable,int timestamp){
 		Transaction temp=transactions.get(transaction);
 		if(null==temp ||temp.getState().equals(TransactionState.Aborted))
 		{
 				out.println("Transaction "+transaction+" already aborted.");
 				return false;
 		}
 			
 		
 		if(temp!=null){
 			if(temp.type.equals(TransactionType.ReadOnly)){
 				Integer value=temp.read(variable);
 				if(value==null){
 					out.println(transactions.get(transaction).getTimestamp()+" waiting for variable"+variable);
 					List<Transaction> blockedTransactionsList  = blockedTransactions.get(variable);  
 					if(blockedTransactionsList == null){
 						blockedTransactionsList = new ArrayList<Transaction>();
 						blockedTransactions.put(variable,blockedTransactionsList);
 					}
 					blockedTransactionsList.add(temp);
 					return false;
 				}
 				List<Site> sitesList = varToSite.get(variable);
 				if(sitesList.size()==1 && !sitesList.get(0).isUp()){
 					out.println(transactions.get(transaction)+" waiting for variable"+variable);
 					List<Transaction> blockedTransactionsList  = blockedTransactions.get(variable);  
 			
 					if(blockedTransactionsList == null){
 						blockedTransactionsList = new ArrayList<Transaction>();
 						blockedTransactions.put(variable,blockedTransactionsList);
 					}
 					blockedTransactionsList.add(temp);
 					return false;
 				}
 						
 				out.println("Transaction "+transactions.get(transaction)+" reads "+variable+" as "+value+" on timestamp= "+timestamp);
 				return true;
 			}
 			else{
 				lock(variable,transactions.get(transaction),true,0,timestamp);
 				return true;
 			}
 		}
 		else{
 			out.println("Transaction"+ transaction+"is already aborted");
 			return false;
 		}
 	}
 	
 	
 	boolean write(String transaction, String variable, int value,int timestamp)
 	{
 		
 		if(null==transactions.get(transaction) || transactions.get(transaction).getState().equals(TransactionState.Aborted))
 		{
 				out.println("Transaction "+transaction+" already aborted.");
 				return false;
 		}
 		lock(variable,transactions.get(transaction),false,value,timestamp);
 		return true;
 	}
 	
 	void dump(int timestamp){
 		out.println("COMPLETE DUMP AT TIMESTAMP= "+timestamp);
 		for(Integer siteNum:sites.keySet()){
 			dump(siteNum,timestamp);
 		}
 		
 	}
 	
 	
 	void dump(int siteNum,int timestamp){
 		out.println("DUMPing Site= "+siteNum+" at TimeStamp "+timestamp);
 		
 			Site site=sites.get(siteNum);
 			if(site.isUp()){
 				out.println(siteNum+" variables are as follows:");
 				out.println(site.dump());
 			}
 			else{
 				out.println(siteNum+" is DOWN.");
 			}
 			
 	}
 	
 	void dump(String variable,int timestamp){
 		out.println("Dumping Variable "+variable+" at timestamp"+timestamp);
 		List<Site> siteList=varToSite.get(variable);
 		for(Site sit:siteList){
 			if(sit.isUp()){
 				try{
 					Variable v=sit.readVariable(variable);	
 					out.println("At Site "+sit+": "+v.getValue());
 				}
 				catch(IllegalStateException e){
 					out.println("At Site "+sit+": CANNOT READ, STALE COPY.");
 					continue;
 				}
 			}
 			else{
 				out.println("Site "+sit+" IS DOWN.");
 			}
 		}	
 		
 	}
 	
 	void fail(int site,int timestamp){
 		
 		//site lock map
 		
 		List<String> nonReplicatedVariables = new ArrayList<String>();
 		Map<String,Variable> variables = sites.get(site).getVariableBackup();
 		for(String v: variables.keySet()){
 			if(!variables.get(v).isVariableReplicated()){
 				nonReplicatedVariables.add(v);
 			}
 		}
 		
 		Set<Transaction> transactionSet = sites.get(site).getTransactionOnSite();
 		for(Transaction t:transactionSet){
 			this.abortSiteFailure(t, timestamp,nonReplicatedVariables);
 		}
 		
 		
 		sites.get(site).fail();
 		out.println("fail"+sites.get(site));
 	}
 	
 	void recover(int site,int timestamp){
 		Site s = sites.get(site);
 		s.recover();
 		out.println("recover"+site);
 		boolean write = false;
 		boolean read = false;
 		Map<String,Variable> backupMap = s.getVariableBackup();
 		List<String> notReplicatedVariables = new ArrayList<String>();
 		for(Variable  v: backupMap.values()){
 			if(!v.isVariableReplicated()){
 				notReplicatedVariables.add(v.getName());
 			}
 		}
 		
 		for(String var:notReplicatedVariables){
 			List<Transaction> blockedTransactionList  =  blockedTransactions.get(var);
 			
 			if(blockedTransactionList != null){
 				List<Transaction> tempBlockedTransactionList = new ArrayList<Transaction>(blockedTransactionList);
 				for(Transaction blockedTransaction:  tempBlockedTransactionList){
 					if(blockedTransaction.getType().equals(TransactionType.ReadOnly)){
 						blockedTransaction.unblock();
 						blockedTransaction.read(var);
 						blockedTransactionList.remove(blockedTransaction);
 						out.println("ReadOnly Transaction"+blockedTransaction.getName()+"unblocked on"+var+"at timestamp"+timestamp);
 						read(blockedTransaction.getName(),var,timestamp);
 					}
 					else {
 						Map<Boolean,Integer> map=blockedTransaction.unblock();
						
 						if(map.containsKey(true) && !write){
 							//true means it was a read operation that it was blocked on.
 							read(blockedTransaction.getName(),var,timestamp);
 							read = true;
							out.println("ReadWrite Transaction"+blockedTransaction.getName()+"unblocked on"+var+"at timestamp"+timestamp);
							blockedTransactionList.remove(blockedTransaction);
 						}
 						else if(map.containsKey(false) && !read){
 							write(blockedTransaction.getName(),var,map.get(false),timestamp);
 							write = true;
							out.println("ReadWrite Transaction"+blockedTransaction.getName()+"unblocked on"+var+"at timestamp"+timestamp);
							blockedTransactionList.remove(blockedTransaction);
 						}
						
 						
 						
 					}
 				}
 				if(blockedTransactionList.isEmpty())
 					blockedTransactions.remove(var);
 			}
 			
 			
 		}
 		
 		
 	}
 	
 	void queryState(){
 		
 	}
 
 	
 }
