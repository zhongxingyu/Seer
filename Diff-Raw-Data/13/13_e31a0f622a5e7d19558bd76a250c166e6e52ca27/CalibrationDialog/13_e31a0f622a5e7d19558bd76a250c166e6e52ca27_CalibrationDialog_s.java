 package org.concord.waba.extra.ui;
 import extra.util.*;
 import org.concord.waba.extra.event.*;
 import waba.ui.*;
 import org.concord.waba.extra.probware.probs.CCProb;
 import org.concord.waba.extra.probware.*;
 
 public class CalibrationDialog extends Dialog implements DataListener{
 int			nContainers = 0;
 int			currContainer = 0;
 CCButton bClose = null;
 CCButton bApply = null;
 CCButton bStart = null;
 CCButton bStop = null;
 TabBar 	 tabBar = null;
 
 waba.ui.Label  contName = null;
 Container []propertiesPanes =null;
 Container   currentPane =null;
 int 	bHeight = 16;
 ExtraMainWindow owner = null;
 WTable 		calTable = null;
 CCProb	probe = null;
 String []nameTabs = {"Preferences","Calibration"};
 int nChannels = 1;
 int   []calibratedRows = null;
 
 final static int PROP_PANE = 0;
 final static int CAL_PANE = 1;
 
 public static ProbManager pb = null;
 
 int 		dataDim = 128;
 //float 	[]dataFFT = new float[dataDim*2];
 //int		dataPointer = 0;
 
 float       	deviation = 100.0f;
 float		totalSumm = 0.0f;
 float		totalSamples = 0f;
 DeviationControl	devControl;
 	public CalibrationDialog(ExtraMainWindow owner,DialogListener l,String title, CCProb probe,int interfaceManager){
 		super(title);
 		this.probe = probe;
 		this.owner = owner;
 		nContainers = (probe != null && probe.needCalibration())?2:1;
 		currContainer = 0;
 		addDialogListener(l);
 		owner.setDialog(this);
 		pb = ProbManager.getProbManager(interfaceManager);
 //		pb.setMode(CCInterfaceManager.A2D_24_MODE);
 //		pb.setMode(CCInterfaceManager.A2D_10_MODE);
 		pb.registerProb(probe);
 		pb.addDataListenerToProb(probe.getName(),this);
 		devControl = new DeviationControl(20.0f);
 		
 		nChannels = probe.getActiveCalibrationChannels();
 		
 	}
 
 	public void setButtons(){
 		waba.fx.Rect contentRect = getContentPane().getRect();
 		waba.fx.FontMetrics fm = getFontMetrics(getFont());
 		int bWidthClose	= fm.getTextWidth("Close") + 5;
 		int bWidthApply	= fm.getTextWidth("Apply") + 5;
 		if(bClose == null) 	bClose = new CCButton("Close");
 		else 				getContentPane().remove(bClose);
 		bClose.setRect(contentRect.width/2 - 5 - bWidthClose,contentRect.height - 5 - bHeight,bWidthClose,bHeight);
 		getContentPane().add(bClose);
 		if(bApply == null) bApply = new CCButton("Apply");
 		else 				getContentPane().remove(bApply);
 		bApply.setRect(contentRect.width/2 + 5 ,contentRect.height - 5 - bHeight,bWidthApply,bHeight);
 		getContentPane().add(bApply);
 		bApply.setEnabled(false);
 	}
 	public void setTabBar(){
 		if(tabBar == null){
 			tabBar = new TabBar();
 			for(int i = 0; i < nContainers; i++){
 				MyTab tab = new MyTab(nameTabs[i]);
 				tabBar.add(tab);
 			}
 		}else{
 			getContentPane().remove(tabBar);
 		}
 		tabBar.setRect(widthBorder+2, 0, width - 2*widthBorder - 4, 20);
 		getContentPane().add(tabBar);
 	}
 	public void setContent(){
 		setButtons();
 		setTabBar();
 		setPropertiesPane();
 		
 	}
 	 public void setUpTable(){
  		if(currContainer != CAL_PANE) return;
  		if(nContainers < 2) return;
  		if(calTable == null){
   			waba.fx.Rect contentRect = getContentPane().getRect();
 	 		calTable = new WTable(getWabaWindow());
 	 		CalibrationDesc caldesc = probe.getCalibrationDesc();
 	 		int nRows = caldesc.countAvailableParams();
 	 		if(calibratedRows == null){
 	 			calibratedRows = new int[nRows];
 	 		}else if(calibratedRows.length != nRows){
 	 			calibratedRows = new int[nRows];
 	 		}
 	 		for(int i = 0; i < nRows; i++) calibratedRows[i] = -1;
 			if(nChannels == 1){
 				calTable.addColumn("Raw");
 			}else{
 				calTable.addColumn("Raw1");
 				calTable.addColumn("Raw2");
 			}
 			calTable.addColumn("Calibrated",true);
 			String []s =new String[nChannels + 1];
 			for(int i = 0; i < s.length; i++) s[i] = "";
 			for(int i = 0; i < nRows;i++) calTable.addRow(s);
 			waba.fx.Rect paneRect = currentPane.getRect();
 			calTable.setRect(0, bHeight + 2, paneRect.width, paneRect.height - 25);
 			calTable.pack();
 			waba.fx.FontMetrics fm = getFontMetrics(getFont());
 			int bWidthStart 	= fm.getTextWidth("Start") + 5;
 			int bWidthStop 	= fm.getTextWidth("Stop") + 5;
 			if(bStart == null){
 				bStart = new CCButton("Start");
 				bStart.setRect(paneRect.width- bWidthStart,1,bWidthStart,bHeight);
 				currentPane.add(bStart);
 			}
 			if(bStop == null){
 				bStop = new CCButton("Stop");
 				bStop.setRect(paneRect.width - 2 - (bWidthStop +bWidthStart) ,1,bWidthStop,bHeight);
 				currentPane.add(bStop);
 			}
 			currentPane.add(calTable);
 			if(devControl == null) devControl = new DeviationControl(4f);
 			devControl.setRect(paneRect.width/2 - 50,paneRect.height - 25,100,23);
 			currentPane.add(devControl);
 		}
 
 	 }
 
  	public void setPropertiesPane(){
  		waba.fx.Rect contentRect = getContentPane().getRect();
 		if(propertiesPanes == null){
  			propertiesPanes = new Container[nContainers];
  		}
  		if(currentPane != null){
 			getContentPane().remove(currentPane);
  		}
  		boolean needNewProp = (currContainer == PROP_PANE) && (propertiesPanes[currContainer] == null);
  		if(propertiesPanes[currContainer] == null){
 			propertiesPanes[currContainer] = new Container();
  		}
 		int pHeight = contentRect.height - (5 + bHeight) - 20;
 		int pWidth = contentRect.width - 2*widthBorder;
 		propertiesPanes[currContainer].setRect(widthBorder,20, pWidth ,pHeight);
 		currentPane = propertiesPanes[currContainer];
  		doStop();
  		if(currContainer == CAL_PANE){
  			setUpTable();
 			bStart.setEnabled(false);
 			if(calTable != null) calTable.selectRow(1);
  		}else if(needNewProp && (probe != null)){
  			waba.fx.Rect r = propertiesPanes[currContainer].getRect();
  			int nProperties = probe.countProperties();
 			int y0 = r.height / 2  - (nProperties * 20) / 2;
 			if (y0 < 0) y0 = 0;
 			int x0 = 5;
 			for(int i = 0; i < nProperties; i++){
 				PropObject po = (PropObject)probe.getProperty(i);
 				String name = po.getName();
 				String value = po.getValue();
 				waba.ui.Label lName = new waba.ui.Label(name);
 				waba.ui.Control c = null;
 				String []possibleValues = po.getPossibleValues();
 				if(possibleValues == null){
 					waba.ui.Edit   eValue = new waba.ui.Edit();
 					eValue.setText(value);
 					c = eValue;
 				}else{
 					int index = -1;
 					for(int j = 0; j < possibleValues.length; j++){
 						if(value.equals(possibleValues[j])){
 							index = j;
 							break;
 						}
 					}
 					Choice ch = new Choice(possibleValues);
 					if(index >= 0){
 						ch.setSelectedIndex(index);
 					}
 					c = ch;
 				}
 				po.setValueKeeper(c);
 				lName.setRect(width/2 - 65,y0,60,16);
 				c.setRect(width/2 + 5,y0,60,16);
 				y0 += 20;
 				propertiesPanes[currContainer].add(lName);
 				propertiesPanes[currContainer].add(c);
 			}
 			
  		}
 /*
 		waba.util.Vector prop = propContainer.getProperties(currContainer);
 		if(prop == null) return;
 		int nProperties = prop.getCount();
  		if(propertiesPanes[currContainer] == null){
 			propertiesPanes[currContainer] = new Container();
 			int pHeight = contentRect.height - (25 + bHeight);
 			int pWidth = contentRect.width - 2*widthBorder;
 			propertiesPanes[currContainer].setRect(widthBorder,20, pWidth ,pHeight);
 			int y0 = pHeight / 2  - (nProperties * 20) / 2;
 			if (y0 < 0) y0 = 0;
 			int x0 = 5;
 			for(int i = 0; i < nProperties; i++){
 				PropObject po = (PropObject)prop.get(i);
 				String name = po.getName();
 				String value = po.getValue();
 				waba.ui.Label lName = new waba.ui.Label(name);
 				waba.ui.Control c = null;
 				String []possibleValues = po.getPossibleValues();
 				if(possibleValues == null){
 					waba.ui.Edit   eValue = new waba.ui.Edit();
 					eValue.setText(value);
 					c = eValue;
 				}else{
 					int index = -1;
 					for(int j = 0; j < possibleValues.length; j++){
 						if(value.equals(possibleValues[j])){
 							index = j;
 							break;
 						}
 					}
 					Choice ch = new Choice(possibleValues);
 					if(index >= 0){
 						ch.setSelectedIndex(index);
 					}
 					c = ch;
 				}
 				po.setValueKeeper(c);
 				lName.setRect(width/2 - 65,y0,60,16);
 				c.setRect(width/2 + 5,y0,60,16);
 				y0 += 20;
 				propertiesPanes[currContainer].add(lName);
 				propertiesPanes[currContainer].add(c);
 			}
 		}
 */
 		getContentPane().add(propertiesPanes[currContainer]);
 		currentPane = propertiesPanes[currContainer];
  	}
 
 	public void updateProperties(boolean clearKeepers){
 		if(probe == null) return;
 		int oldContainers = nContainers;
  		int nProperties = probe.countProperties();
 		for(int j = 0; j < nProperties; j++){
 			PropObject po = (PropObject)probe.getProperty(j);
 			Control c = po.getValueKeeper();
 			if(c instanceof waba.ui.Edit){
 				probe.setPropertyValue(j,((waba.ui.Edit)c).getText());
 				((waba.ui.Edit)c).setText(probe.getPropertyValue(j));
 			}else if(c instanceof Choice){
 				probe.setPropertyValue(j,((Choice)c).getSelected());
 				((Choice)c).setSelectedIndex(probe.getPropertyValue(j));
 			}
 			if(clearKeepers) po.setValueKeeper(null);
 		}
 		int newContainers = (probe != null && probe.needCalibration())?2:1;
 		if(newContainers != oldContainers){
 		}
 		repaint();
 	}
 	
 	private void checkApplyEnabled(){
 		boolean applyEnabled = false;
 		if(currContainer == PROP_PANE){
 			applyEnabled = true;
 		}else{
 			if((calTable != null) && (calibratedRows != null)){
 				applyEnabled = (calibratedRows.length > 0);
 				for(int i = 0; i < calibratedRows.length; i++){
 					if(calibratedRows[i] != 1){
 						applyEnabled = false;
 						break;
 					}
 				}
 			}
 		}
 		bApply.setEnabled(applyEnabled);
 	}
 	
 	public void doStop(){
 		if(probe != null) probe.clearCalibrationListener();
 		if(pb != null) pb.stop();
 		if(calTable != null) calTable.setClickable(true);
 		deviation = 0.0f;
 		devControl.setValue(deviation);
 		drawDeviation();
 		if((calTable != null) && (calibratedRows != null)){
 			calTable.setClickable(true);
 			int selIndex = calTable.getSelectIndex() - 1;
 			if(selIndex >= 0 && selIndex < calibratedRows.length){
 				if(calibratedRows[selIndex] == 0) calibratedRows[selIndex] = 1;
 			}
 		}
 		checkApplyEnabled();
 	}
 	public void doStart(){
 //		dataPointer = 0;//FFT
 		deviation = 100.0f;
 		totalSumm = 0.0f;
 		totalSamples = 0;
 		if(probe != null) probe.setCalibrationListener(this);
 		if(pb != null) pb.start();
 		deviation = 100.0f;
 		devControl.setValue(deviation);
 		drawDeviation();
 		if((calTable != null) && (calibratedRows != null)){
 			calTable.setClickable(false);
 			int selIndex = calTable.getSelectIndex() - 1;
 			if(selIndex >= 0 && selIndex < calibratedRows.length){
 				calibratedRows[selIndex] = 0;
 			}
 		}
 	}
  	public void onEvent(waba.ui.Event event){
 		if (event.type == PenEvent.PEN_UP && (calTable != null)){
 			bStart.setEnabled(calTable.getSelectIndex() > 0);
 		}
 		if (event.type == waba.ui.ControlEvent.PRESSED){
 			if(event.target instanceof MyTab){
 				String contName = ((MyTab)event.target).getText();
 		    		int index = -1;
 				for(int i = 0; i < nContainers; i++){
 					if(contName.equals(nameTabs[i])){
 						index = i;
 						break;
 					}
 				}
 				if(index >= 0){
 					currContainer = index;
 					setPropertiesPane();
 				}
 			}
 			if(event.target instanceof CCButton){
 				if (event.target == bStart){
 					doStart();
 				}else if (event.target == bStop){
 					doStop();
 				}
				if(listener != null){
					String message = ((CCButton)event.target).getText();
					Object info = probe;
					int infoType = org.concord.waba.extra.event.DialogEvent.OBJECT;
					listener.dialogClosed(new org.concord.waba.extra.event.DialogEvent(this,null,message,info,infoType));
				}
 				if((event.target == bClose) || (event.target == bApply)){				
 					doStop();
 					if(event.target == bClose){
 						hide();
 						pb.removeDataListenerFromProb(probe.getName(),this);
 						owner.setDialog(null);
 					}
 					if((probe != null) && (event.target == bApply)){
 						if(currContainer == PROP_PANE){
 							updateProperties(false);
 							if(nChannels != probe.getActiveCalibrationChannels()){
 								nChannels = probe.getActiveCalibrationChannels();
 								calTable = null;
 							}
 						}else{
 		 					CalibrationDesc caldesc = probe.getCalibrationDesc();
 		 					int nRows = caldesc.countAvailableParams();
 		 					//nChannels
 							float []row1 = new float[nRows];
 							float []calibrated = new float[nRows];
 							float []row2 = null;
 							if(nChannels > 1){
 								row2 = new float[nRows];
 							}
 							for(int i = 0; i < nRows; i++){
 								Object cell = calTable.getCell(0,i+1);
 								if((cell != null) && (cell instanceof CCLabel)){
 									row1[i] = ConvertExtra.toFloat(((CCLabel)cell).getText());
 								}
 								cell = calTable.getCell(nChannels,i+1);
 								if((cell != null) && (cell instanceof CCLabel)){
 									calibrated[i] = ConvertExtra.toFloat(((CCLabel)cell).getText());
 								}
 								if(row2 != null){
 									cell = calTable.getCell(1,i+1);
 									if((cell != null) && (cell instanceof CCLabel)){
 										row2[i] = ConvertExtra.toFloat(((CCLabel)cell).getText());
 									}
 								}
 							}
 							probe.calibrationDone(row1,row2,calibrated);
 						}
 					}
 				}
 				return;
 			}
 		}
   	}
   	
   	public void drawDeviation(){
   		if(devControl == null) return;
   		waba.fx.Graphics g = devControl.createGraphics();
   		if(g != null){
   			devControl.onPaint(g);
   			g.free();
   		}
   	}
   	
 	public void dataReceived(DataEvent dataEvent){
 		float[] data = dataEvent.getData();
 		float t0 = dataEvent.getTime();
 		float dt = dataEvent.getDataDesc().getDt();
 		int    chPerSample = dataEvent.getDataDesc().getChPerSample();
 		Object cell = null;
 
 		if(data != null && calTable != null){
 			int selIndex = calTable.getSelectIndex();
 			
 			cell = calTable.getCell(nChannels,selIndex);
 			if((cell != null) && (cell instanceof CCLabel)){
 				((CCLabel)cell).setText(""+data[0]);
 			}
 			cell = calTable.getCell(0,selIndex);
 			if((cell != null) && (cell instanceof CCLabel)){
 				((CCLabel)cell).setText(""+data[1]);
 			}
 			if(nChannels == 2 && data.length > 1){
 				cell = calTable.getCell(1,selIndex);
 				if((cell != null) && (cell instanceof CCLabel)){
 					((CCLabel)cell).setText(""+data[2]);
 				}
 			}
 			waba.fx.Graphics g = calTable.createGraphics();
 			if(g != null){
 				calTable.onPaint(g);
 				g.free();
 			}
 		}
 
 
 		int ndata = dataEvent.getNumbSamples()*dataEvent.getDataDesc().getChPerSample();
 		int nOffset = dataEvent.getDataOffset();
 		float  dtChannel = dt / (float)chPerSample;
 //		boolean doFFT = false;
 		if(dataEvent.getNumbSamples() > 0){
 //		    if(!doFFT) dataFFT[dataPointer++] = data[nOffset];
 //		    if(dataPointer >= dataDim) doFFT = true;
 		    totalSumm += data[nOffset];
 		    totalSamples++;
 		    if(totalSamples > 1){
 			float av = (totalSumm/totalSamples);
 			//				System.out.println("data[nOffset+i] "+data[nOffset+i]+" totalSumm "+ totalSumm + " av "+av+" totalSamples "+totalSamples);
 			deviation = 100.0f*(data[nOffset] - av)/av;
 //				System.out.println("data["+i+"]="+data[nOffset+i]);
 			if(deviation > 100.0f) deviation = 100.0f;
 			if(totalSamples > 16){
 			    totalSamples = 1;
 			    totalSumm = av;
 			}
 		    }
 		}
 		
 //		System.out.println("deviation "+deviation);
 //		System.out.println("totalSamples "+totalSamples);
 //		System.out.println("av "+(totalSumm/totalSamples));
 
 		devControl.setValue(deviation);
 		drawDeviation();
 
 
 /*
 		if(doFFT){
 			dataPointer = 0;
 			
 			float maxData = 0.0f;
 			float summ = 0.0f;
 			float summ2 = 0.0f;
 			for(int k = 0; k < dataDim; k++){
 				float d = dataFFT[k];
 				summ += d;
 				summ2 += d*d;
 				if(maxData < d) maxData = d;
 			}
 			float ave = summ/dataDim;
 			for(int k = 0; k < dataDim; k++){
 				dataFFT[k] = dataFFT[k] - ave;
 			}
 			float disp = extra.util.Maths.sqrt(summ2/(float)dataDim - ave*ave)/ave;
 			//System.out.println("FFT Ave: "+ave+" disp "+disp);
 			//org.concord.waba.extra.util.FFT.realft(dataFFT,dataDim,1);
 			float []normKoeff = new float[dataDim/2];
 			
 			
 			float maxKoeff = 0.0f;
 			for(int k = 1; k <= dataDim;k+=2){
 				float nk = extra.util.Maths.sqrt(dataFFT[k]*dataFFT[k]+dataFFT[k+1]*dataFFT[k+1]);
 				if(k == 1) nk /= 2.0;
 				normKoeff[(k - 1)/2] = nk;
 				if(nk > maxKoeff) maxKoeff = nk;
 			}
 			for(int k = 0; k < normKoeff.length ;k++){
 			    //System.out.println("index "+k+"; freq: "+(int)(100.0f*normKoeff[k]/maxKoeff+0.5));
 			}
 		}
 */
 	}
 	
 	
 	
 	
 	class MyTab extends waba.ui.Tab{
 		public MyTab(String text){super(text);}
 		public String getText(){return text;}
 	}
 }
 
 
 class DeviationControl extends Container{
 float value = 0.0f;
 float maxValue = 100.0f;
 ImagePane ip = null;
 waba.fx.Image bufIm = null;
 	public DeviationControl(float maxValue){
 		this.maxValue = Maths.abs(maxValue);
 		ip = new ImagePane("cc_extra/icons/deviation.bmp");
 		ip.setRect(0,0,23,23);
 		add(ip);
 	}
 	public void setValue(float value){
 		this.value = value;
 		if(value > maxValue) this.value = maxValue;
 		if(value < - maxValue) this.value = -maxValue;
 //		System.out.println("deviation "+value);
 	}
 	
 	public void onPaint(waba.fx.Graphics g){
 		if(g == null) return;
       		if(bufIm == null) bufIm=new waba.fx.Image(width,height);
 		waba.fx.Graphics ig = new waba.fx.Graphics(bufIm);
 		ig.setColor(0xDE,0xDE,0xDE);
 		ig.fillRect(0,0,width,height);
 		ig.setColor(255,0,0);
 		ig.drawLine(13 + width/2-1,0,13 + width/2-1,height - 1);
 		ig.drawLine(13 + width/2,0,13 + width/2,height - 1);
 		ig.drawLine(13 + width/2+1,0,13 + width/2+1,height - 1);
 		
 		float fw = (float)(width/2 - 13 - 10)*value/maxValue;
 		int w = Maths.round(fw);
 		ig.setColor(0,0,255);
 		if(w > 0){
 			ig.fillRect(13 + width/2 + 2,2,w,height - 4);
 		}else{
 			ig.fillRect(13 + width/2 - 1 + w,2,-w,height - 4);
 		}		
 		ig.setColor(0,0,0);
 		ip.onPaint(ig);
      		g.copyRect(bufIm,0,0,width,height,0,0);
      		ig.free();
 	}
 	
 }
