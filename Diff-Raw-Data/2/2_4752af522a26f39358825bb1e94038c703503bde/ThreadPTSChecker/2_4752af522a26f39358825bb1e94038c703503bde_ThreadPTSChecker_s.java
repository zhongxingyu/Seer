 package util.PTS;
 
 import util.PTS.Log.In;
 import util.PTS.Log.Log;
 import util.PTS.Log.On;
 import util.TCP.ThreadSingleTCPServer;
 import util.TCP.ThreadTCPChecker;
 import util.TCP.ThreadTCPServer;
 
public class ThreadPTSChecker extends ThreadTCPChecker{
 
 	public ThreadPTSChecker(ThreadTCPServer threadTCPServer){
 		super(threadTCPServer);
 	}
 
 	public ThreadPTSChecker(ThreadPTSServer threadPTSServer){
 		super(threadPTSServer);
 	}
 
 	protected void check(){
 		// TODO: Fazer todas as checagens e broadcasts aqui!!!
 		// OBS: isso fica num loop que roda no ran da classe ThreadTCPServer
 		checkLogin();
 		checkRegister();
 		checkBroadcast();
 		checkCall();
 		clean();
 	}
 
 	synchronized private void checkCall(){// TODO: testar
 		for(int index = 0; index < getThreadTCPServer()
 				.getThreadSingleTCPServer().size(); index++){
 			for(int index1 = 0; index1 < getThreadTCPServer()
 					.getThreadSingleTCPServer().get(index).getCall().size(); index1++){
 				PTS tempPTS = new PTS(getThreadTCPServer()
 						.getThreadSingleTCPServer().get(index).getCall()
 						.get(index1));
 				for(int index2 = 0; index2 < getThreadTCPServer()
 						.getThreadSingleTCPServer().size(); index2++){
 					if(getThreadTCPServer()
 							.getThreadSingleTCPServer()
 							.get(index2)
 							.getUsername()
 							.equals(tempPTS.getPts()
 									.get(tempPTS.getPts().size() - 1)
 									.getValue())){
 						tempPTS.getPts().remove(tempPTS.getPts().size() - 1);
 						getThreadTCPServer().getThreadSingleTCPServer()
 								.get(index2).send(tempPTS.toString());
 					}
 				}
 
 			}
 		}
 	}
 	synchronized private void clean(){
 		for(int index = 0; index < getThreadTCPServer()
 				.getThreadSingleTCPServer().size(); index++){
 			if(!getThreadTCPServer().getThreadSingleTCPServer().get(index)
 					.isConnected()){
 				ThreadSingleTCPServer tempThreadSingleTCPServer = getThreadTCPServer()
 						.getThreadSingleTCPServer().get(index);
 				getThreadTCPServer().getThreadSingleTCPServer().remove(index);
 				logOff(tempThreadSingleTCPServer);
 				sendLogoff(tempThreadSingleTCPServer);
 				index = 0;
 			}
 		}
 	}
 
 	private void logOff(ThreadSingleTCPServer tempThreadSingleTCPServer){
 		if(!tempThreadSingleTCPServer.getUsername().isEmpty()){
 			for(int index = 0; index < getThreadTCPServer().getLogged().size(); index++){
 				if(getThreadTCPServer().getLogged().get(index)
 						.equals(tempThreadSingleTCPServer.getUsername())){
 					getThreadTCPServer().getLogged().remove(index);
 					break;
 				}
 			}
 		}
 	}
 
 	private void sendLogin(ThreadSingleTCPServer tempThreadSingleTCPServer){
 		if(!tempThreadSingleTCPServer.getUsername().isEmpty()){
 			for(ThreadSingleTCPServer iterable_element : getThreadTCPServer()
 					.getThreadSingleTCPServer()){
 				if(!tempThreadSingleTCPServer.getUsername().equals(
 						iterable_element.getUsername())){
 					iterable_element
 							.send(Log.getLogin(
 									tempThreadSingleTCPServer.getUsername())
 									.toString());
 				}
 			}
 		}
 	}
 
 	private void sendLogoff(ThreadSingleTCPServer tempThreadSingleTCPServer){
 		if(!tempThreadSingleTCPServer.getUsername().isEmpty()){
 			for(ThreadSingleTCPServer iterable_element : getThreadTCPServer()
 					.getThreadSingleTCPServer()){
 				if(!tempThreadSingleTCPServer.getUsername().equals(
 						iterable_element.getUsername())){
 					iterable_element
 							.send(Log.getLogoff(
 									tempThreadSingleTCPServer.getUsername())
 									.toString());
 				}
 			}
 		}
 	}
 
 	private void sendAllLogged(ThreadSingleTCPServer tempThreadSingleTCPServer){
 		for(int index = 0; index < getThreadTCPServer().getLogged().size(); index++){
 			tempThreadSingleTCPServer.send(Log.getLogin(
 					getThreadTCPServer().getLogged().get(index)).toString());
 		}
 	}
 
 	private void sendAllOff(ThreadSingleTCPServer tempThreadSingleTCPServer){
 		for(int index = 0; index < getThreadTCPServer().getResgistered().size(); index++){
 			if(!getThreadTCPServer().getLogged().contains(
 					getThreadTCPServer().getResgistered().get(index))){
 				tempThreadSingleTCPServer
 						.send(Log.getLogoff(
 								getThreadTCPServer().getLogged().get(index))
 								.toString());
 			}
 		}
 	}
 
 	synchronized private void checkBroadcast(){
 		for(ThreadSingleTCPServer iterable_element : getThreadTCPServer()
 				.getThreadSingleTCPServer()){
 			for(int index = 0; index < iterable_element.getBroadcast().size();){
 				for(ThreadSingleTCPServer iterable_element2 : getThreadTCPServer()
 						.getThreadSingleTCPServer()){
 					if(!iterable_element2.getUsername().equals(
 							iterable_element.getUsername())
 							&& !iterable_element2.getUsername().isEmpty()){
 						System.out.println("De:"
 								+ iterable_element.getUsername());
 						System.out.println("Para:"
 								+ iterable_element2.getUsername());
 						iterable_element2.send(iterable_element.getBroadcast()
 								.get(0));
 					}
 				}
 				iterable_element.getBroadcast().remove(0);
 			}
 		}
 	}
 
 	synchronized private void checkRegister(){
 		// TODO: Fazer todas as checagens de registros aqui!!!
 		for(ThreadSingleTCPServer iterable_element : getThreadTCPServer()
 				.getThreadSingleTCPServer()){
 
 			for(int index = 0; index < iterable_element.getToCheck().size();){
 				if(new On(new PTS(iterable_element.getToCheck().get(0))).isOn()){
 					boolean ok = true;
 					for(String iterable_element2 : getThreadTCPServer()
 							.getResgistered()){
 						if(iterable_element.getUsername().equals(
 								iterable_element2)){
 							iterable_element.send(Log.getError());
 							System.out.println("erro");
 							ok = false;
 							iterable_element.getToCheck().remove(0);
 							break;
 						}
 					}
 
 					if(ok){
 						iterable_element.setRegister(true);
 						System.out.println("Registrado:"
 								+ iterable_element.getUsername());
 						getThreadTCPServer().getResgistered().add(
 								iterable_element.getUsername());
 						iterable_element.send(Log.getOk());
 						sendLogoff(iterable_element);
 						System.out.println("ok");
 					}
 
 					iterable_element.getToCheck().remove(0);
 					System.out.println("removeu!");
 					System.out.println("tamanho:"
 							+ iterable_element.getToCheck().size());
 				}
 			}
 
 		}
 	}
 
 	synchronized private void checkLogin(){
 		// TODO: Fazer todas as checagens de login aqui!!!
 		for(ThreadSingleTCPServer iterable_element : getThreadTCPServer()
 				.getThreadSingleTCPServer()){
 			// System.out.println("entrou 1");
 			for(int index = 0; index < iterable_element.getToCheck().size();){
 				int tempSize = iterable_element.getToCheck().size();
 				System.out.println("entrou 2");
 				if(new In(new PTS(iterable_element.getToCheck().get(0))).isIn()){
 					System.out.println("entrou 3");
 					boolean ok = false;
 
 					for(String iterable_element2 : getThreadTCPServer()
 							.getResgistered()){
 						if(iterable_element.getUsername().equals(
 								iterable_element2)){
 							ok = true;
 						}
 					}
 
 					if(!ok){
 						System.out.println("entrou 4");
 						iterable_element.send(Log.getError());// no
 																// registrado
 						iterable_element.getToCheck().remove(0);
 						break;
 					}
 					System.out.println("saiu 4");
 
 					for(String iterable_element2 : getThreadTCPServer()
 							.getLogged()){
 						if(!iterable_element.equals(iterable_element2)){
 							if(iterable_element.getUsername().equals(
 									iterable_element2)){
 								iterable_element.send(Log.getError());// j
 																		// loggado
 								ok = false;
 								iterable_element.getToCheck().remove(0);
 								break;
 							}
 						}
 					}
 
 					if(ok){
 						iterable_element.setLogin(true);
 						getThreadTCPServer().getLogged().add(
 								iterable_element.getUsername());
 						iterable_element.send(Log.getOk());
 						sendAllLogged(iterable_element);
 						sendAllOff(iterable_element);
 						sendLogin(iterable_element);
 						iterable_element.getToCheck().remove(0);
 						break;
 					}
 				}
 				if(tempSize == iterable_element.getToCheck().size()){
 					index++;
 				}
 			}
 		}
 	}
 }
