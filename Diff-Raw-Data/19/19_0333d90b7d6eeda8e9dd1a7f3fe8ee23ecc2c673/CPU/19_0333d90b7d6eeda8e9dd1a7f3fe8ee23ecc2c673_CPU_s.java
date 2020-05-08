 package ch.zhaw.minipc.base;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Observable;
 
 import ch.zhaw.minipc.commands.Command;
 import ch.zhaw.minipc.component.Befehlswerk;
 import ch.zhaw.minipc.component.Befehlszaehler;
 import ch.zhaw.minipc.component.IBefehlswerk;
 import ch.zhaw.minipc.component.IBefehlszaehler;
 import ch.zhaw.minipc.io.CodeReader;
 import ch.zhaw.minipc.memory.IMemory;
 import ch.zhaw.minipc.memory.Memory;
 import ch.zhaw.minipc.memory.MemoryCell;
 
 public class CPU extends Observable implements Runnable{
 	
 	private MemoryCell akku;
 	private IMemory memory;
 	private IBefehlszaehler counter;
 	private IBefehlswerk werk;
 	private HashMap<String,MemoryCell> registerList;
 	private RunModes runMode = RunModes.AUTO;
 	private boolean fPause = false;
 	private ReturnValues initValues;
 	private static boolean carryFlag = false;
 	
 	public CPU(String path){
         CodeReader reader  = new CodeReader();
         
 		List<String> codeList = new ArrayList<String>();
 		List<String> paramList = new ArrayList<String>();
         
 		codeList = reader.readCodeFromFile(path);
 		paramList = reader.readParameterFromFile(path);
 		this.init(codeList, paramList);
 	}
 
 	
 	private void init(List<String> commandList,List<String> paramList){
 		this.akku = new MemoryCell();
 		this.memory = new Memory();
 		this.registerList = new HashMap<String, MemoryCell>();
 		
 		this.registerList.put("R1", new MemoryCell());
 		this.registerList.put("R2", new MemoryCell());
 		this.registerList.put("R3", new MemoryCell());
 		
 		this.counter = new Befehlszaehler();
 		
 		this.werk = new Befehlswerk(memory, akku, registerList, counter);
 		
 		memory.initMemory(commandList, paramList);
 		int i = 0;
 		initValues = new ReturnValues(memory, counter, registerList, akku,i);
 	}
 	
 
 	public void startAutoEmulator(){
 		int i = 0;
 		ReturnValues returnValues = new ReturnValues(memory, counter, registerList, akku, i);
 
 		while(i < memory.getCommandMemorySize()){
 			int position = counter.getPosition();
 
 			Command command = memory.getCommandMemoryField(position);
 
 			werk.excecuteCommand(command);
 
 			if(runMode == RunModes.SLOW){
 				try {
 					//TODO: set time dynamically
 					Thread.sleep(2000);
 				} catch (InterruptedException e) {
 
 					e.printStackTrace();
 				}
 				returnValues = new ReturnValues(memory, counter, registerList, akku, i);
 				this.setChanged();
 				this.notifyObservers(returnValues);
 			}
 			returnValues = new ReturnValues(memory, counter, registerList, akku, i);
 			counter.incrementBefehlszaehler();
 			i++;
 			
 		}
 		this.setChanged();
 		this.notifyObservers(returnValues);	
 	}
 	
 	
 	private void startStepEmulator(){
 
 		int i = 0;
 		ReturnValues returnValues = new ReturnValues(memory, counter, registerList, akku, i);
 
 		while(i < memory.getCommandMemorySize()){
 			int position = counter.getPosition();
 
 			Command command = memory.getCommandMemoryField(position);
 
 			werk.excecuteCommand(command);
 			//Todo find out the right place for this, to get the actual infos in the gui
			counter.incrementBefehlszaehler();
			i++;
 			this.setChanged();
 			this.notifyObservers(returnValues);
 			
 			synchronized (this) {
 				while (fPause) {
 					try {
 						wait();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}
 			
 
 		}
 	}
 
 	public void run() {
 		if(this.runMode == RunModes.STEP){
 			this.startStepEmulator();
 		}else{
 			this.startAutoEmulator();
 		}
 		
 	}
 
 	public void pause() {
 		fPause = true;
 	}
 
 	public void proceed() {
 		fPause = false;
 		synchronized (this) {
 			notify();
 		}
 	}
 
 	public ReturnValues getInitialConifg(){
 		return this.initValues;
 	}
 
 
 	public void setRunMode(RunModes runMode) {
 		this.runMode = runMode;
 	}
 
 
 	public static boolean getCarryFlag() {
 		return carryFlag;
 	}
 
 
 	public static void setCarryFlag(boolean carryFlag) {
 		CPU.carryFlag = carryFlag;
 	}
 
 }
