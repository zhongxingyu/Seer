 package org.concord.waba.extra.probware;
 import waba.ui.Control;
 import waba.sys.Vm;
 import waba.io.SerialPort;
 import waba.ui.Event;
 import waba.ui.ControlEvent;
 import waba.ui.Timer;
 import org.concord.waba.extra.event.DataListener;
 import org.concord.waba.extra.event.DataEvent;
 import extra.util.DataDesc;
 import org.concord.waba.extra.probware.probs.CCProb;
 import org.concord.waba.extra.ui.*;
 
 public class CCInterfaceManager extends Control{
 static protected CCInterfaceManager im = null;
 protected  SerialPort port;
 public int		startTimer =  0;
 
 protected Timer	timer = null;
 
 public 	waba.util.Vector 	dataListeners = new waba.util.Vector();
 public final static int COMMAND_MODE = 0;
 public final static int A2D_24_MODE = 1;
 public final static int A2D_10_MODE = 2;
 public final static int DIG_COUNT_MODE = 3;
 
 
 public final static int INTERFACE_0 = 0;
 public final static int INTERFACE_2 = 2;
 int activeChannels = 2;
 int interfaceType = INTERFACE_0;
 
 
 
 protected ProbManager	pb = null;
 	protected CCInterfaceManager(int interfaceType){
 		this.interfaceType = interfaceType;
 	}
 	
 	public void setProbManager(ProbManager pb){
 		this.pb = pb;
 	}
 	
 	public void start(){
 		port = new SerialPort(1,9600);
 		if((port == null) || !port.isOpen()) return;
 		port.setFlowControl(false);
 	    	setByteStreamProperties();
 		setCurTime(0.0f);
 		gotChannel0 = false;
 		dDesc.setDt(timeStepSize);
 		dDesc.setChPerSample(2);
 		dEvent.setDataOffset(0);
 		dEvent.setDataDesc(dDesc);
 		char startC = getStartChar();
 		if(mode == A2D_10_MODE)  dEvent.setData(valueData);
 		dEvent.setType(DataEvent.DATA_READY_TO_START);
 		pb.startSampling(dEvent);
 		dEvent.setType(DataEvent.DATA_RECEIVED);
 
 		if(!startA2D(startC)){
 		    Dialog.showMessageDialog(null, "Interface Error","Error in interface start", 
 					     "Bummer", Dialog.ERR_DIALOG);
 		    return;
 		}
 		startTimer = Vm.getTimeStamp();
 		timer = addTimer(getRightMilliseconds());
 	}
 	public void stop(){
 		if(timer != null){
 			removeTimer(timer);
 			timer = null;
 		}
 		if(port != null){
 		    buf[0] = (byte)'c';
 		    port.writeBytes(buf, 0, 1);
 			port.close();
 			port = null;
 		}
 	}
 	
 	public static CCInterfaceManager getInterfaceManager(int interfaceType){
 /*
 		if(im == null){
 			im = (interfaceType == INTERFACE_2)?new CCInterfaceManager2():new CCInterfaceManager();
 		}else{
 			int oldInterfaceType = (im instanceof CCInterfaceManager2)?INTERFACE_2:INTERFACE_0;
 			if(oldInterfaceType != interfaceType){
 				im = (interfaceType == INTERFACE_2)?new CCInterfaceManager2():new CCInterfaceManager();
 			}
 		}
 */
 		if(im == null){
 			im = new CCInterfaceManager(interfaceType);
 		}
 		return im;
 	}
 	//we need optimization probably: dynamically calculate getRightMilliseconds
     // This really needs to be figured out
 	public int getRightMilliseconds(){return 50;}
 	
 	public void onEvent(Event event){
     		if (event.type==ControlEvent.TIMER){
 		    int ret = doRightThings();
 		    if(ret < 0){
 			Dialog.showMessageDialog(null, "Interface Error","Error in interface step ", 
 						 "Bummer", Dialog.ERR_DIALOG);
 			stop();
 		    }  else if(ret == WARN_SPEC_CONDITIONS){
 			Dialog.showMessageDialog(null, "Interface Error","Error 2 in interface step ", 
 						 "Bummer", Dialog.ERR_DIALOG);
 			stop();
 		    }
 		}
 	}
 	
 	protected int doRightThings(){
 		if(mode == A2D_10_MODE){
 			return step10bit();
 		}else{
 			return stepGeneric();
 		}
 	}
 
 	int step10bit(){
 		if(interfaceType == INTERFACE_2) return step10bit_2();
 		if((port == null) || !port.isOpen() || pb == null) return ERROR_PORT;
 		int ret = -1;
 		byte tmp;
 		byte pos;
 		int i,j;
 		int value;
 		int curPos;
 		int curChannel = 0;
 
 //		response = OK;
 
 		
 		while(port != null && port.isOpen()){
 			curChannel = 0;
 			ret = port.readBytes(buf, bufOffset, readSize - bufOffset);
 			if(ret <= 0) break; // there are no bytes available
 			ret += bufOffset;	    
 			if(ret < 16){
 				bufOffset = ret;//too few?
 				break;
 			}
 			curPos = 0;
 			int endPos = ret - 1;
 
 			curDataPos = 0;
 			dEvent.setTime(curStepTime);
 			while(curPos < endPos){
 						// Check if the buf has enough space
 						// if not this means a partial package was read
 
 				value = 0;
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != (byte)0x00) continue; // We found a bogus char 
 
 				value |= (tmp & (byte)0x07F) << 6;
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != (byte)0x80) continue; // We found a bogus char 
 
 				value |= (tmp & (byte)0x03F);
 				// Ignore the change bit
 				// The channel bit is reversed on the 10bit converter hence
 				// the 2 -
 				curChannel = 1 - ((value & 0x01000) >> 12);
 				value &= 0x03FF;
 
 				// Return a reasonable resolution
 				float rValue = (float)value * tuneValue;
 
 				if(gotChannel0 && curChannel == 1){
 					curStepTime += timeStepSize;
 					gotChannel0 = false;
 					curData[curChannel + 1] = rValue;
 					curData[0] = curStepTime;
 					valueData[curDataPos++] = curData[1];
 					valueData[curDataPos++] = curData[2];
 				} else {
 					curData[1] = rValue;
 					gotChannel0 = (curChannel == 0);
 				}
 			}
 			dEvent.setNumbSamples(curDataPos/dDesc.getChPerSample());
 			dEvent.setData(valueData);
 			pb.dataArrived(dEvent);
 			if((ret - curPos) > 0){
 				for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 				bufOffset = j;
 			}
 		}
 		// Should have a special error condition
 		if(ret < 0) return WARN_SPEC_CONDITIONS;
 
 		dEvent.setType(DataEvent.DATA_COLLECTING);
 		pb.idle(dEvent);
 		dEvent.setType(DataEvent.DATA_RECEIVED);
 		return OK;
     }
 	
 	protected void notifyProbManager(DataEvent e){
 	    
 	    if(pb != null) pb.dataArrived(e);
 /*
 		if(dataListeners != null){
 			for(int cl = 0; cl < dataListeners.getCount(); cl++){
 				DataListener l = (DataListener)dataListeners.get(cl);
 				l.dataReceived(e);
 			}
 		}
 */
 	}
 	
 	int stepGeneric(){
 		if(interfaceType == INTERFACE_2) return stepGeneric_2();
 		if((port == null) || !port.isOpen()) return ERROR_PORT;
 		int ret = -1;
 		int offset;
 		byte tmp;
 		byte pos;
 		int i,j;
 		int value;
 		int curPos;
 
 //		response = OK;
 		ret = port.readBytes(buf, bufOffset, readSize);
 		if(ret == 0) return OK;
 
 		// Should return some special case
 		if(ret < 0){
 		    //			System.out.println("ret < 0");
 			return WARN_SPEC_CONDITIONS;
 		}
 
 		ret += bufOffset;	    
 		curPos = 0;
 		int endPos = ret;
 		int packEnd = 0;
 
 		curDataPos = 0;
 		dEvent.setTime(curStepTime);
 
 		boolean clearBufferOffset = true;
 		while(curPos < endPos){
 		    // Check if the buf has enough space
 		    // if not this means a partial package was read
 			if((ret - curPos) < numBytes){
 				for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 				bufOffset = j;
 				clearBufferOffset = false;
 				break;
 			}
 		  	value = 0;
 			for(i=0; i < numBytes; i++){
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != position[i]){
 					// We found a bogus char 
 					bufOffset = 0;
 					//response = ERROR;
 					//msg = "Error in serial stream:" + i + ":" + pos;
 
 					// set the buf to the next byte
 					for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 					bufOffset = j;
 					return WARN_WRONG_POSITION;
 				}
 				value |= (tmp & (byte)~MASK) << (((numBytes-1)-i)*bitsPerByte);
 			}
 			int curChannel 			= 0;
 			boolean syncChannels 	= false;
 			if(mode == A2D_24_MODE){
 				// Ignore the change bit
 				curChannel = ((value & 0x8000000) >> 27);
 				value &= 0x7FFFFFF;
 				// Offset the value to zero
 				value = value - (int)0x4000000;
 				// Return ar reasonable resolution
 				syncChannels = true;
 			}else if(mode == A2D_10_MODE){
 				// Ignore the change bit
 				// The channel bit is reversed on the 10bit converter hence
 				// the 2 -
 				curChannel = 1 - ((value & 0x02000) >> 13);
 				value = (value & 0x03F) | ((value >> 1) & 0x03C0);
 				// Return a reasonable resolution
 				syncChannels = true;
 			}else if(mode == DIG_COUNT_MODE){
 	    			curData[0] = curStepTime;
 	   			curStepTime += timeStepSize;
 	   			valueData[curDataPos++] = value;
 			}
 			curData[curChannel+1] = (float)value * tuneValue;
 			if(syncChannels){
 				if(gotChannel0 && curChannel == 1){
 					curData[0] = curStepTime;
 					curStepTime += timeStepSize;
 					gotChannel0 = false;
 					valueData[curDataPos++] = curData[1];
 					valueData[curDataPos++] = curData[2];
 				} else {
 					gotChannel0 = (curChannel == 0);
 				}
 			}
 //		    	convertValA2D(value);
 		}
 		if(curDataPos > 0){
 			dEvent.setNumbSamples(curDataPos/dDesc.getChPerSample());
 			dEvent.setData(valueData);
 			notifyProbManager(dEvent);
 		}
 		if(clearBufferOffset) bufOffset = 0;
 		return OK;
     }
 
 	public void addDataListener(DataListener l){
 		dataListeners.add(l);
 	}
 	
 	public int Command(byte command, byte response){
 		if(port == null) return 0;
 		port.setReadTimeout(0);
 
 		while(port.readBytes(buf, 0, BUF_SIZE) > 0);
 
 
 		buf[0] = command;
 		int tmp = port.writeBytes(buf, 0, 1);
 		
 		if(tmp != 1) return -1; //error
 
 		port.setReadTimeout(1000);
 		Vm.sleep(200);
 		while(true){
 			tmp = port.readBytes(buf, 0, 1);
 			if(tmp > 0){	       
 				if(buf[0] == response) {
 				    port.setReadTimeout(0);
 				    return 1;
 				}else{
 					continue;
 				}
 			}else{
 				return 0;
 			}
 		}
 	}
 
 
 	boolean stopSampling(){
 		// Let the device wake up a bit
 		// But try to stop it as soon as we can
 		
 		int tmp = 0 ;
 		buf[0] = (byte)'c';
 		for(int i=0; i<10; i++){
 			tmp = port.writeBytes(buf, 0, 1);
 			Vm.sleep(100);
 		}
 		// in case the the port is left open stop it
 		waba.sys.Vm.sleep(500);
 		if(interfaceType == INTERFACE_2){
 			buf[0] = (byte)'9';
 			port.writeBytes(buf, 0, 1);
 		}else if(interfaceType == INTERFACE_0){
 			int ret;
 			if((ret = Command((byte)'c', (byte)67)) != 1){
 				port.close();
 				port = null;
 				return false;
 			}
 		}
 		port.setReadTimeout(0);
 
 		tmp = port.readBytes(buf, 0, BUF_SIZE);//workaround 
 		if(tmp < 0){
 		    // There might have been a line error
 		    // Try again
 		    if(port.readBytes(buf, 0, BUF_SIZE) < 0){
 			port.close();
 			port = null;
 			return false;
 		    }
 		}
 		    
 		return true;
 	}
 
 	boolean startA2D(char startChar){	
 		if(!stopSampling()) return false;
 		buf[0] = (byte)startChar;
 		port.writeBytes(buf, 0, 1);
 		bufOffset = 0;
 		return true;
 	}
 
 
 	
 	public void dispose(){
 		stop();
 	}
 	
     	protected void finalize() throws Throwable {
     		dispose();
 	}
 	
 	
 	int bufOffset = 0;
 	public final static int BUF_SIZE = 1000;
 	byte position[] = {(byte)0x00,(byte)0x80,(byte)0x80,(byte)0x80,};
 	byte [] buf = new byte[BUF_SIZE];
 	float []valueData = new float[1 + BUF_SIZE / 2]; //0 init time, 1 - deltat, 2 - numb data(total)
     	int mode = A2D_10_MODE;
 
 	public int getMode(){return mode;}
 	public void setMode(int mode){
 		this.mode = mode;
 		if(port != null){
 			stop();
 			start();
 		}
 	}
 
 	char getStartChar(){
 		if(interfaceType == INTERFACE_2) return getStartChar_2();
 		if(mode == COMMAND_MODE) return 0;
 		if(mode == A2D_24_MODE) return 'd';
 		if(mode == A2D_10_MODE) return 'a';
 		if(mode == DIG_COUNT_MODE) return 'e';
 		return 0;
 	}
 	
 	public void setByteStreamProperties(){
 		numBytes = 4;
 		bitsPerByte = 7;
 		position[0] = (byte)0x00;
 		position[1] = position[2] = position[3] = (byte)0x80;
 		timeStepSize = (float)0.333333;
 		curDataPos = 0;
 		tuneValue = 1.0f;
 		readSize = 512;
 		switch(mode){
 			case A2D_24_MODE:
 				MASK = (byte)(0x0FF << bitsPerByte);
 	    			tuneValue = 0.000075f;
 				break;
 			case A2D_10_MODE:
 	   			numBytes = 2;
 	   			bitsPerByte = 7;
 				MASK = (byte)(0x0FF << bitsPerByte);
 	    			timeStepSize = (float)0.005;
 				if(interfaceType == INTERFACE_2){
 				    tuneValue = 2.441406f;
 				} else {
 				    tuneValue = 3.22f;
 				}
 				break;
 			case DIG_COUNT_MODE:
 	    			numBytes = 1;
 	    			bitsPerByte = 8;
 	    			MASK = (byte)(0x00);
 	    			timeStepSize = (float)0.01;
 	    			readSize = 100;
 				break;
 		}
 	}
 	int stepGeneric_2(){
 		if((port == null) || !port.isOpen()) return ERROR_PORT;
 		int ret = -1;
 		int offset;
 		byte tmp;
 		byte pos;
 		int i,j;
 		int value;
 		int curPos;
 
 //		response = OK;
 		ret = port.readBytes(buf, bufOffset, readSize);
 		if(ret == 0) return OK;
 		if(ret < 0)  return WARN_SPEC_CONDITIONS;
 
 		ret += bufOffset;	    
 		curPos = 0;
 		int endPos = ret;
 		int packEnd = 0;
 
 		curDataPos = 0;
 		dEvent.setTime(curStepTime);
 
 		boolean clearBufferOffset = true;
 		while(curPos < endPos){
 		    // Check if the buf has enough space
 		    // if not this means a partial package was read
 			if((ret - curPos) < numBytes){
 				for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 				bufOffset = j;
 				clearBufferOffset = false;
 				break;
 			}
 		  	value = 0;
 			for(i=0; i < numBytes; i++){
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != position[i]){
 					// We found a bogus char 
 					bufOffset = 0;
 					//response = ERROR;
 					//msg = "Error in serial stream:" + i + ":" + pos;
 
 					// set the buf to the next byte
 					for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 					bufOffset = j;
 					return WARN_WRONG_POSITION;
 				}
 				value |= (tmp & (byte)~MASK) << (((numBytes-1)-i)*bitsPerByte);
 			}
 			int curChannel 			= 0;
 			boolean syncChannels 	= false;
 			if(mode == A2D_24_MODE){
 				// Ignore the change bit
 				curChannel = ((value & 0x4000000) >> 26);
 				value &= 0x3FFFFFF;
 				// Offset the value to zero
 				value = value - (int)0x2000000;
 				// Return ar reasonable resolution
 				syncChannels = true;
 			}else if(mode == A2D_10_MODE){
 				// Ignore the change bit
 				// The channel bit is reversed on the 10bit converter hence
 			    // This is wrong for the new interface.
 				// the 2 -
 				curChannel = 1 - ((value & 0x02000) >> 13);
 				value = (value & 0x03F) | ((value >> 1) & 0x03C0);
 				// Return a reasonable resolution
 				syncChannels = true;
 			}else if(mode == DIG_COUNT_MODE){
 	   			curStepTime += timeStepSize;
 	   			valueData[curDataPos++] = value;
 			}
 			if(syncChannels){
 			    // This is a hack
 			    curData[curChannel+1] = (float)value * tuneValue*2f;
 				if(gotChannel0 && curChannel == 1){
 					curData[0] = curStepTime;
 					curStepTime += timeStepSize;
 					gotChannel0 = false;
 					valueData[curDataPos++] = curData[1];
 					valueData[curDataPos++] = curData[2];
 				} else {
 					gotChannel0 = (curChannel == 0);
 				}
 			}
 //		    	convertValA2D(value);
 		}
 		if(curDataPos > 0){
 			dEvent.setNumbSamples(curDataPos/dDesc.getChPerSample());
 			dEvent.setData(valueData);
 			notifyProbManager(dEvent);
 		}
 		if(clearBufferOffset) bufOffset = 0;
 		return OK;
     }
 	int step10bit_2(){
 		if((port == null) || !port.isOpen() || pb == null) return ERROR_PORT;
 
 		if(activeChannels == 1) return step10bitFast_2();
 
 		int ret = -1;
 		byte tmp;
 		byte pos;
 		int i,j;
 		int value;
 		int curPos;
 		int curChannel = 0;
 
 //		response = OK;
 		while(port != null && port.isOpen()){
 			curChannel = 0;
 			ret = port.readBytes(buf, bufOffset, readSize - bufOffset);
 			if(ret <= 0) break; // there are no bytes available
 			ret += bufOffset;	    
 			if(ret < 32){
 				bufOffset = ret;//too few?
 				break;
 			}
 			curPos = 0;
 			int endPos = ret - 1;
 
 			curDataPos = 0;
 			dEvent.setTime(curStepTime);
 			while(curPos < endPos){
 						// Check if the buf has enough space
 						// if not this means a partial package was read
 
 				value = 0;
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != (byte)0x00){
 					continue; // We found a bogus char 
 				}

 				value |= (tmp & (byte)0x00F) << 7;
 				tmp = buf[curPos++];
 				pos = (byte)(tmp & MASK);
 				if(pos != (byte)0x80){
 					continue; // We found a bogus char 
 				}

 				value |= (tmp & (byte)0x07F);
				// Ignore the change bit
				// The channel bit is reversed on the 10bit converter hence
				// the 2 -
				// Don't know if this is true any more
 				curChannel = ((value & 0x00400) >> 10);
 				value &= 0x03FF;
 
 				if(gotChannel0 && curChannel == 1){
 				    gotChannel0 = false;
 				    valueData[curDataPos++] = curDataCh0;
 				    // Return a reasonable resolution
 				    valueData[curDataPos++] = (float)value * tuneValue;
 				} else {
 				    // Return a reasonable resolution
 				    gotChannel0 = (curChannel == 0);
 				   if(gotChannel0) curDataCh0 = (float)value * tuneValue;
 				}
 			}

 			dEvent.numbSamples = (curDataPos/activeChannels);
 			curStepTime += dEvent.numbSamples*timeStepSize;
 			pb.dataArrived(dEvent);
 
 			if((ret - curPos) > 0){
 				for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 				bufOffset = j;
 			}


 		}
 		// Should have a special error condition
 		if(ret < 0) return WARN_SPEC_CONDITIONS;
 		dEvent.setType(DataEvent.DATA_COLLECTING);
 		pb.idle(dEvent);
 		dEvent.setType(DataEvent.DATA_RECEIVED);
 		return OK;
 	}
 
 	int step10bitFast_2(){
 
 		int ret = -1;
 		byte tmp;
 		byte pos;
 		int i,j;
 		int value;
 		int curPos;
 		int curChannel;
 
 		while(port != null && port.isOpen()){
 		    ret = port.readBytes(buf, bufOffset, readSize - bufOffset);
 		    if(ret <= 0) break; // there are no bytes available
 		    ret += bufOffset;	    
 		    if(ret < 16){
 			bufOffset = ret;//too few?
 			break;
 		    }
 		    curPos = 0;
 		    int endPos = ret - 1;
 
 		    curDataPos = 0;
 		    dEvent.setTime(curStepTime);
 		    while(curPos < endPos){
 			// Check if the buf has enough space
 			// if not this means a partial package was read
 
 			value = 0;
 			tmp = buf[curPos++];
 			pos = (byte)(tmp & MASK);
 			if(pos != (byte)0x00) continue; // We found a bogus char 
 			
 			value |= (tmp & (byte)0x00F) << 7;
 			tmp = buf[curPos++];
 			pos = (byte)(tmp & MASK);
 			if(pos != (byte)0x80) continue; // We found a bogus char 
 
 			value |= (tmp & (byte)0x07F);
 			// Ignore the change bit
 			// The channel bit is reversed on the 10bit converter hence
 			// the 2 -
 			// Don't know if this is true any more
 			curChannel = ((value & 0x00400) >> 10);
 			value &= 0x03FF;
 
 
 			// Return a reasonable resolution
 			valueData[curDataPos++] = (float)value * tuneValue;
 
 		    }
 
 		    dEvent.numbSamples = curDataPos/dDesc.chPerSample;
 		    curStepTime += dEvent.numbSamples*timeStepSize;
 			
 		    dEvent.setData(valueData);
 		    pb.dataArrived(dEvent);
 		    if((ret - curPos) > 0){
 			for(j=0; j<(ret-curPos); j++) buf[j] = buf[curPos + j];
 			bufOffset = j;
 		    }
 		}
 		// Should have a special error condition
 		if(ret < 0) return WARN_SPEC_CONDITIONS;
 
 		dEvent.setType(DataEvent.DATA_COLLECTING);
 		pb.idle(dEvent);
 		dEvent.setType(DataEvent.DATA_RECEIVED);
 		return OK;
 	}
 	
 	char getStartChar_2(){
 		if(mode == COMMAND_MODE) return 0;
 		if(pb == null) return 0;
 
 		activeChannels = 2;
 		if(mode == DIG_COUNT_MODE){
 		    activeChannels = 1;
 		    dDesc.setChPerSample(1);				    
 		    return 'r';
 		}
 		int numbProbs = pb.getNumbProbs();
 		if(numbProbs < 1) return 0;
 		if(numbProbs == 1){
 			CCProb pr = pb.getProbByIndex(0);
 			if(pr == null) return 0;
 			int interfacePort = pr.getInterfacePort();
 			if(mode == A2D_24_MODE){
 				if(interfacePort == CCProb.INTERFACE_PORT_A){
 					return 'a';
 				}else{
 					return 'b';
 				}
 			}else if(mode == A2D_10_MODE){
 				activeChannels = pr.getActiveChannels();
 				if(activeChannels == 1){
 				    timeStepSize = timeStepSize/2;
 				    dDesc.setDt(timeStepSize);
 				    dDesc.setChPerSample(1);				    
 				    if(interfacePort == CCProb.INTERFACE_PORT_A){					
 					return 'e';
 				    }else{
 					return 'f';
 				    }
 				}else{
 				    if(interfacePort == CCProb.INTERFACE_PORT_A){
 					return 'g';
 				    }else{
 					return 'h';
 				    }
 				}
 			}
 		}else if(numbProbs == 2){
 			if(mode == A2D_24_MODE){
 				return 'd';
 			}else if(mode == A2D_10_MODE){
 				activeChannels = pb.getProbByIndex(0).getActiveChannels();
 				int activeChannels1 = pb.getProbByIndex(1).getActiveChannels();
 				if(activeChannels1 > activeChannels) activeChannels = activeChannels1;
 				if(activeChannels == 1){
 				    dDesc.setChPerSample(1);
 				    return 'i';					
 				}else if(activeChannels == 2){
 				    timeStepSize = timeStepSize*2;
 				    dDesc.setDt(timeStepSize);
 				    return 'j';
 				}
 				
 			}
 		}
 		return 0;
 	}
 	
 	public int 		numBytes = 4;
 	public int 		bitsPerByte = 7;
 	public byte 		MASK = (byte)(0x0FF << bitsPerByte);
     	float 			timeStepSize = (float)0.333333;
 	int 				curDataPos = 0;
     	float 			curStepTime = 0f;
     	int 				readSize = 512;
     	public float		getCurTime(){return curStepTime;}
     	public void		setCurTime(float val){curStepTime = val;}
 
     	public final static int ERROR_GENERAL			= -1;
     	public final static int ERROR_PORT				= -2;
     	public final static int OK 					= 0;
     	public final static int WARN_WRONG_POSITION	= 1;
     	public final static int WARN_SPEC_CONDITIONS	= 2;
 
     	public final static int NUMB_CHANNELS = 2;
 	float				[]curData = new float[1+NUMB_CHANNELS];
     	float 			curDataCh0;
 	
 	public float		tuneValue = 1.0f;
     	boolean 			gotChannel0 = false;
 
 	public DataDesc	dDesc = new DataDesc();
 	public DataEvent	dEvent = new DataEvent();
 
 }
