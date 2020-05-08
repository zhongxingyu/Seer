 /**
  * 
  */
 package hu.modembed.pic.simulator;
 
 import hu.modembed.simulator.IByte;
 import hu.modembed.simulator.ICore;
 import hu.modembed.simulator.IMemory;
 import hu.modembed.simulator.IWord;
 import hu.modembed.simulator.impl.ByteInMemory;
 import hu.modembed.simulator.impl.WritableMemory;
 
 /**
  * @author balazs.grill
  *
  */
 public abstract class PIC16Core implements ICore{
 	
 	protected final IMemory memory = new WritableMemory(memorySize());
 	
 	protected abstract int memorySize();
 	
 	/**
 	 * Extend address with the currently selected bank.
 	 * 
 	 * @param address
 	 * @return
 	 */
 	protected abstract long bank(long address);
 	
 	protected abstract IMemory memory();
 	
 	/**
 	 * Working register
 	 * @return
 	 */
 	protected abstract IByte W();
 	
 	/**
 	 * Program counter
 	 * @return
 	 */
 	protected abstract IWord PC();
 	
 	/**
 	 * 0: C
 	 * 1: DC
 	 * 2: Z
 	 */
 	protected final IByte STATUS = new ByteInMemory(memory, 3);
 	
 	protected void setZ(boolean b){
 		if (b){
 			STATUS.set(STATUS.get() | 4);
 		}else{
 			STATUS.set(STATUS.get() & ~4);
 		}
 	}
 
 	protected void setC(boolean b){
 		if (b){
 			STATUS.set(STATUS.get() | 1);
 		}else{
 			STATUS.set(STATUS.get() & ~1);
 		}
 	}
 	
 	public void ADDWF(long f, long d){
 		int v = memory().getValue(bank(f));
 		v += W().get();
 		setC(v > 255);
 		v = v&0xFF;
 		setZ(v == 0);
 		if (d == 0){
 			W().set(v);
 		}else{
 			memory().setValue(bank(f), v);
 		}
 	}
 	
 	public void ANDWF(long f, long d){
 		int v = memory().getValue(bank(f));
 		v &= W().get();
 		v = v&0xFF;
 		setZ(v == 0);
 		if (d == 0){
 			W().set(v);
 		}else{
 			memory().setValue(bank(f), v);
 		}
 	}
 	
 	public void MOVF(long f, long d){
 		int v = memory().getValue(bank(f));
 		setZ(v == 0);
 		if (d == 0){
 			W().set(v);
 		}else{
 			memory().setValue(bank(f), v);
 		}
 	}
 	
 	public void CLRF(long f){
 		memory().setValue(bank(f), 0);
 	}
 	
 	public void SUBWF(long f, long d){
 		int v = memory().getValue(bank(f));
 		v -= W().get();
 		setC(v < 0);
 		if (v < 0){
 			v += 255;
 		}
 		v = v&0xFF;
 		setZ(v == 0);
 		if (d == 0){
 			W().set(v);
 		}else{
 			memory().setValue(bank(f), v);
 		}
 	}
 	
 	public void BTFSC(long f, long b){
 		int v = memory().getValue(bank(f));
		int bit = (int)(2^b);
 		boolean set = (v & bit) != 0;
 		
 		if (!set){
 			PC().set(PC().get()+1);
 		}
 	}
 	
 	public void CLRW(){
 		W().set(0);
 	}
 	
 	public void GOTO(long k){
 		PC().set(k);
 	}
 	
 	public void MOVLW(long k){
 		W().set((int)k);
 	}
 	
 	public void MOVWF(long f){
 		memory().setValue(bank(f), W().get());
 	}
 }
