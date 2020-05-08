 package model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.swing.JFileChooser;
 
 import action.Action;
 
 import derivative.Derivative;
 import derivative.Option;
 import derivative.Stock;
 
 import order.LimitOrder;
 import order.MarketOrder;
 import order.Order;
 import order.StopLimitOrder;
 import order.StopOrder;
 import view.SimulationView;
 
 public class SessionController {
 
 	public SessionController(){
 
 	}
 	
 	public void save(String folder, Portfolio portfolio, String date){
 		
 		JFileChooser fc = new JFileChooser("packages/"+folder+"/saves");
         int returnVal = fc.showSaveDialog(fc);
         if (returnVal != JFileChooser.APPROVE_OPTION) {
         	return;
         }
         File file = fc.getSelectedFile();
 		
 		StringBuffer sb = new StringBuffer();		
 		sb.append(date+"\n");
 
 		sb.append(portfolio.getCredit()+"\n");
 
 		sb.append("Orders\n");
 		LinkedList<Order> olist = portfolio.getOrder();
 		for(Order o: olist){
 			if (o instanceof MarketOrder){
 				MarketOrder mo = (MarketOrder) o;
 				sb.append(mo.toString());
 			}else if (o instanceof LimitOrder){
 				LimitOrder lo = (LimitOrder) o;
 				sb.append(lo.toString());
 			}else if (o instanceof StopLimitOrder){
 				StopLimitOrder slo = (StopLimitOrder) o;
 				sb.append(slo.toString());
 			}else if (o instanceof StopOrder){
 				StopOrder so = (StopOrder) o;
 				sb.append(so.toString());
 			}
 			sb.append("\n");
 			Derivative d = o.getUnderlying();
 			if(d instanceof Option){
 				Option op = (Option)d;
 				sb.append(op.toString());
 			}else{
 				sb.append(d.toString());				
 			}
 			sb.append("\n");
 		}
 		
 		sb.append("Onhand\n");
 		
 		LinkedList<Derivative> dlist = portfolio.getOnhand();
 		for(Derivative d: dlist){
 			if(d instanceof Option){
 				Option op = (Option)d;
 				sb.append(op.toString());
 			}else{
 				sb.append(d.toString());				
 			}
 			sb.append("\n");
 		}
 		
 		sb.append("Action\n");
 		
 		LinkedList<Action> alist = portfolio.getAction();
 		for(Action a: alist){			
 			sb.append(a.toString());
 			sb.append("\n");
 			Derivative d = a.getUnderlying();
 			if(d instanceof Option){
 				Option op = (Option)d;
 				sb.append(op.toString());
 			}else{
 				sb.append(d.toString());				
 			}
 			sb.append("\n");
 		}	
 		
 		//File file = new File("packages/"+folder+"/save.csv");
 		try {
 			FileWriter fw = new FileWriter(file);
 			BufferedWriter bw = new BufferedWriter(fw);
 			bw.write(sb.toString());
 			bw.flush();
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 
	public void load(SimulationView sv, File file){
 		
 		LinkedList<Derivative> onhand = new LinkedList<Derivative>();
 		LinkedList<Order> orders = new LinkedList<Order>();
 		LinkedList<Action> actions = new LinkedList<Action>();
 		
 		float credit = 0;
 		String date = null;
 		try {
 			FileReader fr = new FileReader(file);
 			BufferedReader bf = new BufferedReader(fr);
 			String line = null;
 			String [] splited;
 			String [] prev = null;
 			int counter =0;
 			boolean flag = true;
 			
 			try {
 				while((line = bf.readLine())!=null){
 					splited = line.split(",", -1);
 					if(splited.length == 1){
 						if(counter == 0){ //0 date 1 credit 2 Orders 3 Onhand 4 Action
 							date = splited[0];
 						}
 						if(counter == 1){ 
 							credit = Float.parseFloat(splited[0]);
 						}
 						counter++;
 					}else{
 						if(counter == 3){
 							//System.out.println("flag "+flag);
 
 							if(flag){//order id+","+strategyid+","+longshort+","+tag; +type of order
 								prev = line.split(",", -1);
 							}else{//underlying
 								//System.out.println("sc "+line);
 
 								Derivative d = getUnder(splited);
 								Order o = getOrder(prev, d);
 								//System.out.println("SessionController "+o.toString());
 								orders.add(o);
 							}
 							flag = !flag;
 
 						}else if(counter == 4){
 							Derivative d = getUnder(splited);
 							onhand.add(d);
 						}else if(counter == 5){
 							if(flag){//order id+","+strategyid+","+longshort+","+tag; +type of order
 								prev = line.split(",", -1);
 							}else{//underlying
 								
 								Derivative d = getUnder(splited);
 								Action a = getAction(prev, d);
 								actions.add(a);
 							}
 							flag = !flag;
 						}						
 					}
 			
 				}
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 					
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Portfolio p = new Portfolio(10000);
 		
 		p.setOrders(orders);
 		p.setOnhand(onhand);
 		p.setActions(actions);
 		p.setCredit(credit);
 		
 		
 		sv.loadme(date);
 		sv.setPortfolio(p);
 			
 	}
 
 	private Action getAction(String[] s, Derivative d){
 		if(s.length ==2){// time+","+desc;
 			return new Action(s[0], s[1], d);			
 		}
 		return null;
 	}
 
 	
 	private Order getOrder(String[] s, Derivative d){
 		//id+","+strategyid+","+longshort+","+tag; +type of order
 		
 		//System.out.println("SessionC getOrder "+s[0]+s[1]+s[2]+s[3]+s[4]);
 
 		
 		if(s.length==5){//market
 			
 			return new MarketOrder(s[0],s[1],s[2],s[3],d ,s[4]);
 			
 		}else if(s.length == 7){//stop limit
 			return new StopLimitOrder(s[0],s[1],s[2],s[3],d,s[5],s[6]);
 			
 		}else if(s[4].equals("Limit")){
 			return new LimitOrder(s[0],s[1],s[2],s[3],d,s[5]);
 			
 		}else if(s[4].equals("Stop")){
 			return new StopOrder(s[0],s[1],s[2],s[3],d,s[5]);
 			
 		}
 		return null;
 	}
 	
 	private Derivative getUnder(String[] s) {
 		// TODO Auto-generated method stub
 		if(s.length>6){//option
 			return new Option(s[0],s[1],s[2],s[3],s[4],s[5],s[6],s[7],s[8]);
 		}else{//stock
 			return new Stock(s[0],s[1],s[2],s[3],s[4],s[5]);			
 		}
 		
 	}
 
 }
