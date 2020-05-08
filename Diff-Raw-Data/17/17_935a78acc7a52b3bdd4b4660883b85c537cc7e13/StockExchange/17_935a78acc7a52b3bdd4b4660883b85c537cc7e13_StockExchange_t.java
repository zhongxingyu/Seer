 /**
  * 
  */
 package stockExchangePac;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.Vector;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import javax.security.auth.login.LoginException;
 import javax.swing.text.html.MinimalHTMLWriter;
 import javax.swing.text.html.HTMLDocument.Iterator;
 
 
 import Stomp.Client;
 import Stomp.Listener;
 
 /**
  * @author tom
  *
  */
 public class StockExchange implements Listener {
 	static final int N=4; // max number of clients per broker
 	Client _stockExchangeStompClient;
 	Map<String,Company> _companies;
 	TreeSet<StockExchangeBroker> _brokers;
 	double _cash;
 	int _numOfClosedBrockers;
 	Vector<String> _newBrokers;
 	Vector<String> _newClients;
 	int _day;
 	int _numActiveClients;
 	int _numActiveCBrokers;
 
 
 	public StockExchange(String server, int port, String login, String pass) throws FileNotFoundException, IOException, LoginException {
 		_stockExchangeStompClient = new Client(server,port,login,pass);
 		_companies=initCompanies();  
 		_brokers= new TreeSet<StockExchangeBroker>(new BrokerCompare()); 
 
 		_cash=0;
 		_numOfClosedBrockers=0;
 		_day=-1;
 		_numActiveClients=0;
 		_numActiveCBrokers=0;
 		_newBrokers= new Vector<String>();
 		_newClients= new Vector<String>();
 		//_clients= new HashMap<String,StockExchangeBroker>();
 		_stockExchangeStompClient.subscribe("/topic/bConnect",this);
 		_stockExchangeStompClient.subscribe("/topic/cConnect",this);
 		_stockExchangeStompClient.subscribe("/topic/Orders",this);
 		_stockExchangeStompClient.subscribe("/topic/cDisconnect",this);
 	}
 
 	private Map<String, Company> initCompanies() throws FileNotFoundException, IOException {
 		HashMap<String, Company> companies = new HashMap<String, Company>();
 		Properties p = new Properties();
 		p.load(new FileInputStream("stocks.ini"));
 		int numOfStocks = Integer.parseInt(p.getProperty("numOfStocks"));
 		for(int i=1; i <= numOfStocks;i++) {
 			String stockName=p.getProperty("stock"+i+"Name");
 			double stockPrice=Double.parseDouble(p.getProperty("stock"+i+"InitialPrice"));
 			int stockNumFloatShares= Integer.parseInt(p.getProperty("stock"+i+"NumFloatShares"));;
 			companies.put(stockName, new Company(stockName, stockNumFloatShares, stockPrice));
 		}
 		return companies;
 	}
 
 	public StockExchange(String server, int port) throws LoginException, IOException {
 		this(server,port,"login","pass");
 	}
 
 	/* (non-Javadoc)
 	 * @see Stomp.Listener#message(java.util.Map, java.lang.String)
 	 */
 	@Override
 	public void message(Map headers, String body,String origin) {
 		body=body.replace("\n","");
 		body=body.replace("\r","");
 		Vector<String> parts = new Vector<String>();
 		for (String s : body.split(" "))
 			parts.add(s);
 		// Broker connected 
 		if ((origin != null) && (origin.equals("/topic/bConnect")) && (parts.elementAt(0).equals("connect")) && (parts.size() == 2)) {
 			connectBroker(parts.elementAt(1));
 			return;
 		}
 		// Client connected
 		if ((origin != null) && (origin.equals("/topic/cConnect")) && (parts.elementAt(0).equals("connect")) && (parts.size() == 2)) {
 			connectClient(parts.elementAt(1));
 			return;
 		}
 		// Client disConnected
 		if ((parts.elementAt(0).equals("disconnect")) && (parts.size() == 2)) {
 			disconnectClient(parts.elementAt(1));
 			return;
 		}
 		// Broker closeDay
 		if ((parts.elementAt(0).equals("closeDay")) && (parts.size() == 3)) {
 			brokerClosedTheDay(parts.elementAt(1));
 			return;
 		}
 		// Broker passed a buyOrder
 		if ((parts.elementAt(0).equals("buyOrder"))  && (parts.size() == 6)) {
 			addBuyOrder(parts.elementAt(1),parts.elementAt(2),Integer.parseInt(parts.elementAt(3)),parts.elementAt(4),Double.parseDouble(parts.elementAt(5)));
 			return;
 		}
 		// Broker passed a sellOrder
 		if ((parts.elementAt(0).equals("sellOrder"))  && (parts.size() == 6)) {
 			addSellOrder(parts.elementAt(1),parts.elementAt(2),Integer.parseInt(parts.elementAt(3)),parts.elementAt(4),Double.parseDouble(parts.elementAt(5)));
 			return;
 		}
 
 	}
 
 	private void disconnectClient(String client) {
 		for(Company company : _companies.values()) { 
 			while (company.getBuyOrders().remove(client));
 			while (company.getSellOrders().remove(client));
 //			StockOrder order = company.getBuyOrders().remove(client);
 //			while (order != null) { //remove buy orders
 //				company.getBuyOrders().remove(client);
 //				order = company.getBuyOrders().get(client);
 //			}
 //			order = company.getSellOrders().get(client);
 //			while (order != null) { //sell buy orders
 //				company.getSellOrders().remove(client);
 //				order = company.getSellOrders().get(client);
 //			}
 		}
 		_numActiveClients--;
 		for (StockExchangeBroker broker : _brokers)
 			broker.removeClient(client);
 //		//_brokers.get(_clients.get(client)).decClientNum(); TODO:
 //		_clients.remove(client);
 		_stockExchangeStompClient.unsubscribe("/topic/cDeals-"+client,this);
 		_stockExchangeStompClient.send("/topic/cDisconnected","disconnected "+ client+"\n");
 	}
 
 	private void brokerClosedTheDay(String brokerName) {
 		_numOfClosedBrockers++;
 		if(_numOfClosedBrockers == _numActiveCBrokers)
 			endTheDay();
 	}
 
 	private void endTheDay() {
 		computeDeals();
 		updatePrices();
 		startNewDay();
 	}
 
 	private void computeDeals() {
 		for(Company company : _companies.values()) {
 			while((company._buyOrders.size() > 0) && (company._sellOrders.size() > 0)) {	
 				StockOrder buy = company._buyOrders.pollFirst();
 				StockOrder sell = company._sellOrders.pollFirst();
 				if (sell.getPrice() > buy.getPrice()) break;
 				double price=Math.min(buy.getPrice(),sell.getPrice());
 				int amount=Math.min(buy.getAmount(), sell.getAmount());
 				String mes="deal "+ buy.getClientName()+" "+ buy.getBrokerName() +" "+ sell.getClientName()+" "+ sell.getBrokerName() +" " + buy.getStockName()+ " " 
 				+ amount + " "+ price + "\n";
 				if (sell.getClientName().equals("StockExchange")) {
 					_cash+=price;
 					_stockExchangeStompClient.send("/topic/bDeals-"+buy.getBrokerName(),mes);
 				} else {
 					_stockExchangeStompClient.send("/topic/bDeals-"+buy.getBrokerName(),mes);
 					_stockExchangeStompClient.send("/topic/bDeals-"+sell.getBrokerName(),mes);
 				}
 			}
 		}
 	}
 
 
 
 	private void updatePrices() {
 		for(Company company : _companies.values())
 			company.endDay();
 	}
 
 	public void startNewDay() {
 		_numOfClosedBrockers=0;
 		_day++;
 		_stockExchangeStompClient.send("/topic/Calendar", "newDay "+_day+"\n");
 		connectNewBrokers();
 		connectNewClients();
 		publishPrices();
 	}
 
 	private void publishPrices() {
 		String mesg="Prices " +_day + ":\n";
 		for(Company company : _companies.values()) 
 			mesg+=company.getName()+" "+  company.getPrice()+"\n"; 
 		_stockExchangeStompClient.send("/topic/Prices", mesg);
 	}
 
 	private  void connectNewClients() {
 		while (_numActiveClients == 0 && _newClients.size() == 0) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		for(String client : _newClients) {
 //			StockExchangeBroker keyB = _brokers.firstKey();
 //			StockExchangeBroker broker=_brokers.get(keyB);;
 			StockExchangeBroker broker=_brokers.pollFirst();;
 			if (broker.getNumOfClients() == N) {
 				_stockExchangeStompClient.send("/topic/Connected","connectFailed "+client+"\n");
 				_numActiveClients++;
 			} else {
 				if (broker.getNumOfClients() == 0)
 					_numActiveCBrokers++;
 				broker.addClient(client);
 				_stockExchangeStompClient.send("/topic/cConnected","connected "+client+" "+broker.getName() +"\n");
 				_stockExchangeStompClient.subscribe("/topic/cDeals-"+client, this);
 				_numActiveClients++;
 			}
 			_brokers.add(broker);
 		}
 		_newClients.clear();
 	}
 
 	private  void connectNewBrokers() {
 		while (_numActiveCBrokers == 0 && _newBrokers.size() == 0) {
 			try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		for(String broker : _newBrokers) {
 			_brokers. add(new StockExchangeBroker(broker));
 			_stockExchangeStompClient.send("/topic/bConnected", "connected "+broker+"\n");			
 		}
 		_newBrokers.clear();
 	}
 
 	private void addSellOrder(String clientName,String brokerName, int shares,String stockName, double price) {
 		_companies.get(stockName).addSellOrder(clientName,brokerName,shares,stockName,price);
 	}
 
 	private void addBuyOrder(String clientName,String brokerName, int shares,String stockName, double price) {
 		_companies.get(stockName).addBuyOrder(clientName,brokerName,shares,stockName,price);
 	}
 
 
 	private void connectClient(String clientName) {
 		_newClients.add(clientName);
 	}
 
 	private void connectBroker(String brokerName) {
 		_newBrokers.add(brokerName);
 	}
 }
