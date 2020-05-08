 package life;
 
 import java.io.File;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Stack;
 
 import ui.LifeController;
 import util.RLE;
 
 public class EvolveManager implements Runnable {
 
 	LifeAlgo algo;
 	LifeController control;
 	int speed = 1;
 	boolean running = false;
 	boolean forcedState = false;
 	boolean preventEvolve = false;
 	long stepNumber = 0;
 	
 	Queue<Order> orders = new LinkedList<Order>();
 	Stack<EvolveManagerState> stateStack = new Stack<EvolveManagerState>();
 	
 	abstract class Order{
 		abstract void doOrder();
 	}
 
 	class ResetFromStateOrder extends Order{
 		EvolveManagerState state;
 		ResetFromStateOrder(EvolveManagerState s){
 			state = s;
 		}
 		void doOrder(){
 			algo = state.algo;
 			stepNumber = state.nSteps;
 			algo.setState(state.state);
 		}
 	}
 	
 	class LoadFromFileOrder extends Order{
 		File file;
 		LoadFromFileOrder(File f){
 			file = f;
 		}
 		void doOrder(){
 			int[][] array = RLE.read(file);
 			algo.loadFromArray(array);
 			int dimw = array[0].length;
 			int dimh = array.length;
 			int w = control.getDrawer().getWidth(), h = control.getDrawer().getHeight();
 			int zoom;
 			System.out.println(w - dimw);
 			if(w > dimw) {
 				zoom = 31 - Integer.numberOfLeadingZeros(w / dimw);
 			} else {
 				zoom = Integer.numberOfLeadingZeros(dimw / w) - 33;
 			}
 			if(h > dimh) {
 				zoom = Math.min(zoom, 31 - Integer.numberOfLeadingZeros(h / dimh));
 			} else {
 				zoom = Math.min(zoom, Integer.numberOfLeadingZeros(dimh / h) - 33);
 			}
 			System.out.println(zoom);
 			control.getDrawer().setView(zoom);
 		}
 	}
 	
 	class SaveToFileOrder extends Order{
 		File file;
 		SaveToFileOrder(File f){
 			file = f;
 		}
 		void doOrder(){
 			RLE.write(file, algo.saveToArray());
 		}
 	}
 
 	class ForcedOrder extends Order{
 		void doOrder(){
 			forcedState = true;
 		}
 	}
 
 	class PreventEvolveOrder extends Order{
 		void doOrder(){
 			preventEvolve = true;
 		}
 	}
 
 	class ToggleCellOrder extends Order{
 		int x, y;
 		ToggleCellOrder(int a, int b){
 			x = a;
 			y = b;
 		}
 		void doOrder(){
 			algo.toggleCellAt(x, y);
 		}
 	}
 	
 	public EvolveManager(LifeController controller, LifeAlgo a){
 		controller.setEvolver(this);
 		setController(controller);
 		setAlgo(a);
 	}
 	
 	public void getBackToClosestState(long target){
 		while (stateStack.size() > 1 && stateStack.peek().nSteps > target){
 			stateStack.pop();
 		}
 	}
 	
 	public void loadFromFile(File f){
 		synchronized(orders){
 			orders.add(new LoadFromFileOrder(f));
 			orders.add(new ForcedOrder());
 			orders.add(new PreventEvolveOrder());
 		}
 	}
 	
 	public void saveToFile(File f){
 		synchronized(orders){
 			orders.add(new SaveToFileOrder(f));
 			orders.add(new ForcedOrder());
 			orders.add(new PreventEvolveOrder());
 		}
 	}
 
 	public void toggleCell(int x, int y){
 		synchronized(orders){
 			orders.add(new ToggleCellOrder(x, y));
 			orders.add(new ForcedOrder());
 			orders.add(new PreventEvolveOrder());
 		}
 	}
 	
 	public void forceNext(){
 		synchronized(orders){
 			orders.add(new ForcedOrder());
 		}
 	}
 	
 	public void setController(LifeController c){
 		control = c;
 	}
 	
 	public LifeAlgo getAlgo(){
 		return algo;
 	}
 	
 	public void setAlgo(LifeAlgo a){
 		algo = a;
 	}
 	
 	public int getSpeed(){
 		return speed;	
 	}
 	
 	public void setSpeed(int s){
		System.out.println("SetSpeed " + s);
 		speed = s;
 	}
 	
 	public void stop(){
 		running = false;
 	}
 	
 	public void run(){
 		running = true;
 		
 		while(running){
 			boolean hasOrders = false;
 			while(! control.needsMoreEvolve() && ! hasOrders){
 				try {
 					Thread.sleep(1);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				synchronized(orders){
 					hasOrders = !orders.isEmpty();
 				}
 			}
 						
 			Queue<Order> copiedOrders;
 			synchronized(orders){
 				copiedOrders = orders;
 				orders = new LinkedList<Order>();
 			}
 			
 			for(Order order : copiedOrders){
 				order.doOrder();
 			}
 
 			if(! preventEvolve){
 				int currentSpeed = speed;
 				if(currentSpeed >= 0){
 					algo.evolve(currentSpeed);
 					stepNumber += currentSpeed;
 				}else{
 					long target = stepNumber + currentSpeed;
 					getBackToClosestState(target);
 					
 					algo.setState(stateStack.peek().state);
 					long reachedEvent = stateStack.peek().nSteps;
 					stepNumber = reachedEvent;
 					
 					if(reachedEvent < target){
 						int offset = (int) (target - reachedEvent);
						//System.out.println("Target: " + target + " Reached " + reachedEvent + " Offset " + offset);
 
 						algo.evolve(offset);
						stepNumber += offset;//reachedEvent;//target; 
 					}
 				}
 			}
 
 			EvolveManagerState evolveState = new EvolveManagerState(stepNumber, forcedState, algo);
 			
			stateStack.add(evolveState);
 			control.onNewState(evolveState);
 			
 			forcedState = false;
 			preventEvolve = false;
 		}
 	}
 
 	public void resetState(EvolveManagerState lastDrawnState) {
 		synchronized(orders){
 			orders.add(new ResetFromStateOrder(lastDrawnState));
 		}
 	}
 }
