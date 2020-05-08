 import netP5.NetAddress;
 import controlP5.*;
 import processing.core.PApplet;
 import rwmidi.*;
 import oscP5.*;
 
 
 public class ButtonPadModule extends AbstractModule {
 	private int[] buttonpad = new int[16];
 	private int[] oldbuttonpad = new int[16];
 	private int[] ledstate = new int[16];
 	private int[] ledstatez = new int[16];
 	private int thedragstate;
 	Modulome that;
 	private ControlP5 cp5;
 	private Group controls;
 	private Group midisettings;
 	private Group oscsettings;
 	private Group feedbacksettings;
 	private int modsize;
 
 	//Midi variables
 	private boolean isMidiOn, isNoteOn, isMomentary;
 	private int firstNote, midichannel;
 	private MidiOutput midiout;
 	private MidiInput midiin;
 
 	//Feedback Variables
 	private boolean isExternalFeedback;
 	private int feedbacksource;
 	private boolean ismidifeedback, isoscfeedback, isechofeedback;
 
 	//OSC Variables
 	private boolean isOscOn;
 	private boolean SerialOsc;
 	//private String pre;
 	private int oscport;
 	private int monomesize;
 	private int monomepos;
 	private int owplus,ohplus,otimes;
 	private OscP5 oscP5;
 	private int[] osctypew = new int[]{0,4,0,4,0,0,4,8,12,0,4,8,12};
 	private int[] osctypeh = new int[]{0,0,4,4,0,0,0,0,0,4,4,4,4};
 	private int[] osctypex = new int[]{1,1,1,1,2,1,1,1,1,1,1,1};
 
 	//private NetAddress myRemoteLocation;
 
 
 
 	public ButtonPadModule(int ID, int Colour, int modsize2, int addr, Modulome tha, ControlP5 thep5, MidiOutput mout, MidiInput min, OscP5 oscar, NetAddress mrl){
 
 		setID(ID);
 		setColour(Colour);
 		modsize = modsize2;
 		setmodsize(modsize);
 		hideSettings();
 		setaddress(addr);
 		that = tha;
 		cp5 = thep5;	
 		midiout = mout;
 		midiin = min;
 		oscP5 = oscar;
 		//myRemoteLocation = mrl;
 		SerialOsc = true;
		pre = "mlr";
 
 		// SETTINGS GROUP // ID 0
 		controls =  cp5.addGroup("Module "+addr+ " Settings")
 				.setId(addr * 100)
 				.setPosition(getXposition()+(modsize/10)+1,getYposition()+(modsize/10)+1)
 				.hideBar()
 				.hide()
 				.setBackgroundHeight(4*modsize/5)
 				.setWidth(4*modsize/5)
 				.setBackgroundColor(that.color(255,50))
 				;
 
 		//MIDI GROUP //ID 2
 		midisettings =  cp5.addGroup("Module "+addr+ " MIDI")
 				.setId(addr * 100 + 2)
 				.setPosition(0,modsize/4)
 				.hideBar()
 				.moveTo(controls)
 				//.hide()
 				.setBackgroundHeight(modsize/2)
 				.setWidth(4*modsize/5)
 				//.setBackgroundColor(that.color(30,140,255,150))
 				;
 
 		// OSC GROUP // ID 3
 		oscsettings =  cp5.addGroup("Module "+addr+ " OSC")
 				.setId(addr * 100 + getID() + 3)
 				.setPosition(0,modsize/4)
 				.hideBar()
 				.moveTo(controls)
 				//.hide()
 				.setBackgroundHeight(modsize/2)
 				.setWidth(4*modsize/5)
 				//.setBackgroundColor(that.color(130,30,140,150))
 				;
 
 		// FEEDBACK GROUP // ID 4
 		feedbacksettings =  cp5.addGroup("Module "+addr+ " Feedback")
 				.setId(addr * 100 + 4)
 
 				.setPosition(0,modsize/4)
 				.hideBar()
 				.moveTo(controls)
 				//.hide()
 				.setBackgroundHeight(modsize/2)
 				.setWidth(4*modsize/5)
 				//.setBackgroundColor(that.color(255,140,30,150))
 				;
 
 		// MODE SELECT // ID 1
 
 		RadioButton rb = cp5.addRadioButton("modeselect"+addr)
 				.setId(addr * 100 + 1)
 				.moveTo(controls)
 				.setWidth(modsize/4)
 				.setItemsPerRow(3)
 				.addItem("Feedback" + addr,0)
 				.addItem("MIDI " + addr,1)
 				.addItem("OSC " + addr,2)
 				.setSpacingColumn(0)
 				.setLabelPadding((modsize/4)*-1, 30)
 				.setPosition(0,0)
 				//.hideLabels()
 				.setLabel("Feedback")
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				//.set
 				.setSize(4*modsize/15+1,modsize/8)
 				.setValue(0);
 		rb.getItem(0).setLabel("Feedback");
 		rb.getItem(1).setLabel("MIDI");
 		rb.getItem(2).setLabel("OSC");
 
 		//MIDI settings for Button Pad
 
 		// Midi ON?  // ID 5
 		Toggle midion = cp5.addToggle("midion" + addr)
 				.setId(addr*100 + 5)
 				.setPosition((modsize/20) , ((modsize/20) * - 1) - 2)
 				.setLabel("Midi On")
 				.setHeight(3*modsize/10)
 				.setWidth(3*modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(midisettings);
 
 
 		//CC or Note On // ID 6
 		DropdownList ccornote = cp5.addDropdownList("CCorNote"+addr, (modsize/20), 5*modsize/10, (modsize/2) - (modsize/5), modsize/5)
 				.setBarHeight(modsize/10)
 				.setId(addr*100 + 6)
 				.setLabel("CC or Note")
 				.actAsPulldownMenu(true)
 				.setItemHeight(modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(midisettings);
 
 		ccornote.addItem("Control Change", 0);
 		ccornote.addItem("Note On",1);
 
 		//What is the MIDI CHANNEL?  //   ID 7
 		DropdownList midichan = cp5.addDropdownList("midichan"+addr, (8*modsize/20), 5*modsize/10, (modsize/2) - (modsize/5), modsize/5)
 				.setId(addr*100 + 7)
 				.setBarHeight(modsize/10)
 				.setLabel("Midi Channel")
 				.actAsPulldownMenu(true);
 		midichan.captionLabel();
 		midichan.setItemHeight(modsize/10)
 		.setColorBackground(Colour)
 		.setColorForeground(Colour + (0x00222222))
 		.setColorActive(Colour + (0x00333333))
 		.moveTo(midisettings);
 
 		for (int i = 0; i < 16; i++){
 			midichan.addItem(""+ (i+1), i);
 		}
 
 		//First Value // ID 8
 
 		DropdownList firstvalue = cp5.addDropdownList("firstvalue"+addr, (8*modsize/20), modsize/20, (3*modsize/10), 4*modsize/10)
 				.setBarHeight(modsize/10)
 				.setId(addr*100 + 8)
 				.setLabel("First Value")
 				.actAsPulldownMenu(true)
 				.setItemHeight(modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(midisettings);
 
 		for (int i = 0; i < 112; i++){
 			firstvalue.addItem(""+ (i+1), i);
 
 		}
 		//Feedback Options //ID 50
 		Toggle feedbackon = cp5.addToggle("feedbackon" + addr)
 
 				.setId(addr*100 + 50)
 				.setPosition((modsize/20) , ((modsize/20) * - 1) - 2)
 				.setLabel("External LED Feedback")
 				.setHeight(3*modsize/10)
 				.setWidth(7*modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(feedbacksettings);
 
 		//Feedback Type //ID 51
 		RadioButton feedback = cp5.addRadioButton("Feedback Type"+addr)
 				.setId(addr * 100 + 51)
 				.moveTo(feedbacksettings)
 				//.setItemWidth(modsize/4);
 				//feedback.setItemHeight(modsize/6)
 				.setItemsPerRow(3)
 				.setSize((modsize/5)+1, modsize/10);
 		feedback.addItem("Serial Echo Feedback" + addr,0)
 		.addItem("MIDI Feedback" + addr,1)
 		.addItem("OSC Feedback" + addr,2)
 		.setSpacingColumn(modsize/20)
 		.setPosition(modsize/20,2*modsize/5)
 		//.setCaptionLabel()
 		//.hideLabels()
 		.setLabel("LED Feedback Source")
 		.setColorBackground(Colour)
 		.setColorForeground(Colour + (0x00222222))
 		.setColorActive(Colour + (0x00333333))
 		.setLabelPadding((modsize/5)*-1, 30);
 
 		feedback.getItem(0).setLabel("Echo");
 		feedback.getItem(1).setLabel("MIDI");
 		feedback.getItem(2).setLabel("OSC");
 
 
 
 		//OSC Controls for ButtonPad
 
 		//OSC ON // ID 80
 		Toggle oscon = cp5.addToggle("oscon" + addr)
 				.setId(addr*100 + 80)
 				.setPosition((modsize/20) , ((modsize/20) * - 1) - 2)
 				.setLabel("OSC ON")
 				.setHeight(3*modsize/10)
 				.setWidth(3*modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(oscsettings);
 
 
 		//OSC PREFIX // ID 81
 		cp5.addTextfield("OSCprefix" + addr)
 		.setId(addr*100 + 81)
 		.setLabel("OSC Prefix")
 		.setPosition((8*modsize/20), 7 * (modsize/20))
 		.setSize((3*modsize/10), (modsize/10))
 		.setFocus(false)
 		.setAutoClear(false)
 		.setColorBackground(Colour)
 		.setColorForeground(Colour + (0x00222222))
 		.setColorActive(Colour + (0x00333333))
 		.setColor(Colour + (0x00333333))
 		.moveTo(oscsettings);
 		;
 
 		//OSC PORT // ID 82
 		cp5.addTextfield("OSCport" + addr)
 		.setId(addr*100 + 82)
 		.setLabel("OSC Port")
 		.setPosition((8*modsize/20), 3 * (modsize/20))
 		.setSize((3*modsize/10), (modsize/10))
 		.setFocus(false)
 		.setAutoClear(false)
 		.setColorBackground(Colour)
 		.setColorForeground(Colour + (0x00222222))
 		.setColorActive(Colour + (0x00333333))
 		.setColor(Colour + (0x00333333))
 		.moveTo(oscsettings);
 		;
 
 
 		//OSC MONOME SIZE/SHAPE // ID 83
 		DropdownList monomevalue = cp5.addDropdownList("monomevalue"+addr, (8*modsize/20), modsize/20, (3*modsize/10), 4*modsize/10)
 				.setBarHeight(modsize/10)
 				.setId(addr*100 + 83)
 
 				.actAsPulldownMenu(true)
 				.setItemHeight(modsize/10)
 				.setColorBackground(Colour)
 				.setColorForeground(Colour + (0x00222222))
 				.setColorActive(Colour + (0x00333333))
 				.moveTo(oscsettings)
 				.setCaptionLabel("Monome Type");
 		monomevalue.addItem("40h 1", 0);
 		monomevalue.addItem("40h 2", 1);
 		monomevalue.addItem("40h 3", 2);
 		monomevalue.addItem("40h 4", 3);
 		monomevalue.addItem("40h All", 4);
 		monomevalue.addItem("128 1", 5);
 		monomevalue.addItem("128 2", 6);
 		monomevalue.addItem("128 3", 7);
 		monomevalue.addItem("128 4", 8);
 		monomevalue.addItem("128 5", 9);
 		monomevalue.addItem("128 6", 10);
 		monomevalue.addItem("128 7", 11);
 		monomevalue.addItem("128 8", 12);
 		monomevalue.setValue((ID)%12);
 
 
 
 		//SET MY INITIAL VALUES
 
 		isMidiOn = false;
 		isOscOn = false;
 		isNoteOn = true;
 		isMomentary = true;
 		isExternalFeedback = false;
 		firstNote = 32;
 		midichannel = getID();
 
 		midiin.plug(this, "noteOnReceived", midichannel);
 
 		midisettings.hide();
 		oscsettings.hide();
 
 		ohplus = osctypeh[ID%12];
 		owplus = osctypew[ID%12];
 		otimes = osctypex[ID%12];
 	}
 
 	@Override
 	public void draw(PApplet g) {
 
 		int x = getXposition();
 		int y = getYposition();
 		int x2 = x;
 
 		if (getfade() < 255){
 
 			if (controls.isVisible() == true) controls.hide();
 
 
 
 			g.stroke(150);
 			g.strokeWeight(2);
 			//g.noStroke();
 			for (int i = 0; i < 16; i++) {
 
 				g.fill(getColour() + (0x00222222)*ledstate[i]); // *0 is where buttonpad[i] will indicate whether to draw it or not
 				g.rect(x2 + getmodsize()/20 + 2, y + getmodsize()/20 + 2, getmodsize()/5, getmodsize()/5);
 				x2+= (getmodsize()/10) + (getmodsize()*2/15);
 				if (x2 >= (x+getmodsize()*4/5)) {
 					y += (getmodsize()/10) + (getmodsize()*2/15);
 					x2 = x;
 				}
 			}
 		}
 
 		y = getYposition();
 		g.fill(0,getfade());
 		g.stroke(150);
 		g.strokeWeight(2);
 		g.rect(x+2,y+2,getmodsize() - 4,getmodsize() - 4);
 
 		if (getfade() == 255){
 
 			controls.show();
 		}
 		if(isExternalFeedback){
 
 			int a = 0;
 			int b = 0;
 			boolean sendsomejunk = false;
 
 			for (int i = 0; i < 16; i++){
 
 				if (isechofeedback){
 					ledstate[i] = buttonpad[i];
 				}
 
 
 				if (ledstate[i] != ledstatez[i]){
 					sendsomejunk = true;
 					ledstatez[i] = ledstate[i];	
 				}
 
 				if (i < 8){
 
 					a |= (ledstate[i] << i);
 				}
 
 				else {
 					b |= (ledstate[i] << (i-8));
 				}
 
 
 
 			}
 			if (sendsomejunk){
 
 				that.println("Junk sent " + Integer.toBinaryString(a) + "   " + Integer.toBinaryString(b));
 				int[] payload = {1,a,b};
 				that.xbeeoutput(getaddress(),payload);
 
 			}
 		}
 
 	}
 
 	@Override
 	public void processdata(int[] data) {
 		for (int i = 0; i < 8; i++) {
 			int bytea = (data[7] >> i) & 1;
 			buttonpad[i] = bytea;
 			int byteb = (data[8] >> i) & 1;
 			buttonpad[i+8] = byteb;
 		}
 		output();
 	}
 
 	@Override
 
 	public void output(){
 
 		for (int i = 0; i < 16; i++){
 			if (buttonpad[i] != oldbuttonpad[i]){
 
 				//SEND MIDI
 				if (isMidiOn){
 					if (isNoteOn){
 						midiout.sendNoteOn(midichannel, ((4-(i/4))*4) + (i%4) + firstNote, buttonpad[i]*127);
 					}
 					else if (!isNoteOn){
 						midiout.sendController(midichannel, i + firstNote, buttonpad[i]*127);
 					}
 				}
 
 				if (isOscOn){
 
 					//TODO  PUT OSC CODE IN HERE
 					OscMessage myMessage;
 					if (SerialOsc == false){
 						myMessage = new OscMessage(pre+"/press");
 
 
 
 					}
 					else{
 						myMessage = new OscMessage(pre+"/grid/key");  
 					}
 					//myMessage = new OscMessage(pre+"grid/key");
 					myMessage.add(((i%4) + owplus) * otimes);
 					myMessage.add(((i/4)+ ohplus)*otimes);
 					myMessage.add(buttonpad[i]);
 					oscP5.send(myMessage, myRemoteLocation);
 					that.println("SENT MESSAGE  : " + pre+"/grid/key"+(((i%4) + owplus) * otimes)+"/"+(((i/4)+ ohplus)*otimes)+ "/"+buttonpad[i]);
 					that.println("to destination   :" + myRemoteLocation);
 				} 
 
 
 
 
 
 				oldbuttonpad[i] = buttonpad[i];
 			}
 		}
 	}
 
 
 	@Override
 
 	public void mousePressed(int thebutton, int thex, int they){
 
 
 		if (thebutton == 37){
 			if (getfade() == 0){
 				if((thex > ((getmodsize()/20)+2)) && (thex < ((19*(getmodsize()/20))+2))){
 
 					int x = thex - ((getmodsize()/20) + 2);
 					if((they > ((getmodsize()/20)+2)) && (they < ((19*(getmodsize()/20))+2))){
 
 						int y = they - ((getmodsize()/20) + 2);
 
 
 						int i = (x / ((getmodsize()/5) + getmodsize()/30)) + (y / ((getmodsize()/5 + getmodsize()/30)) * 4);
 
 						buttonpad[i] = Math.abs(buttonpad[i]  - 1);
 						//TODO put this in a variable so you can turn it into emulator or not.
 						output();
 						ledstate[i] = buttonpad[i];
 
 						thedragstate = buttonpad[i];
 
 					}
 				}
 
 			}
 			else {
 
 
 			}
 		}
 		else if ((thebutton == 38)&&(getfade()==255)){
 			controls.remove();
 			that.moduleremover(getID());
 
 		}
 
 
 
 	}
 
 	public void mouseMoved(int thebutton, int thex, int they){
 
 		if (getfade() == 0){
 
 
 			if (thebutton == 37){
 
 				if((thex > ((getmodsize()/20)+2)) && (thex < ((19*(getmodsize()/20))+2))){
 
 					int x = thex - ((getmodsize()/20) + 2);
 					if((they > ((getmodsize()/20)+2)) && (they < ((19*(getmodsize()/20))+2))){
 
 						int y = they - ((getmodsize()/20) + 2);
 
 
 						int i = (x / ((getmodsize()/5) + getmodsize()/30)) + (y / ((getmodsize()/5 + getmodsize()/30)) * 4);
 
 						buttonpad[i] = thedragstate;
 						ledstate[i] = thedragstate;
 
 					}
 				}
 
 			}
 
 
 		}
 	}
 
 	@Override
 	public void modulemoved() {
 
 		controls.setId(getID());
 		controls.setTitle("Module "+getaddress()+ " Settings");
 		controls.setPosition(getXposition()+(modsize/10)+1,getYposition()+(modsize/10)+1);	
 
 	}
 
 	@Override
 	public void remove() {
 		controls.remove();		
 		midiin.unplug(this, "noteOnReceived", midichannel);
 
 	}
 
 	@Override
 	public void controlEvent(ControlEvent theevent) {
 		int thecontrol = (theevent.getId() - (getaddress()*100));
 
 		if (thecontrol == 1){
 			if(theevent.getValue() == 0){
 				midisettings.hide();
 				oscsettings.hide();
 				feedbacksettings.show();
 				that.println("feedback");
 			}
 			if(theevent.getValue() == 1){
 				midisettings.show();
 				oscsettings.hide();
 				feedbacksettings.hide();
 				that.println("Midi");
 			}
 			if(theevent.getValue() == 2){
 				midisettings.hide();
 				oscsettings.show();
 				feedbacksettings.hide();
 				that.println("osc");
 			}
 		}
 		else if(thecontrol == 5){
 
 			isMidiOn = (theevent.getValue() == 1);
 		}
 
 
 		else if(thecontrol == 6){
 
 			isNoteOn = (theevent.getValue() == 1);
 		}
 
 		else if(thecontrol == 9){
 
 			isMomentary = (theevent.getValue() == 1);
 		}
 
 		else if(thecontrol == 7){
 
 			midiin.unplug(this, "noteOnReceived", midichannel);
 			midichannel = (int)(theevent.getValue());
 			midiin.plug(this, "noteOnReceived", midichannel);
 
 		}
 
 		else if(thecontrol == 8){
 
 			firstNote = (int)(theevent.getValue());
 		}
 		else if(thecontrol == 50){
 
 			isExternalFeedback = (theevent.getValue()==1);
 			if (isExternalFeedback){
 				that.println("feedbackon");
 				togglefeedback(255);
 			}
 			else if (!isExternalFeedback){
 				that.println("feedbackoff");
 				togglefeedback(1);
 			}
 		}
 		else if(thecontrol == 51){
 
 			if((theevent.getValue()) == 1){
 				ismidifeedback = true;
 				isoscfeedback = false;
 				isechofeedback = false;
 			}
 			else if((theevent.getValue()) == 0){
 				ismidifeedback = false;
 				isoscfeedback = false;
 				isechofeedback = true;
 			}
 			else if((theevent.getValue()) == 2){
 				ismidifeedback = false;
 				isoscfeedback = true;
 				isechofeedback = false;
 			}
 		}
 
 
 		// OSC MONOME SETTINGS
 
 		else if(thecontrol == 80){
 
 			isOscOn = (theevent.getValue() == 1);
 			that.println("OSC is set to  :" + theevent.getValue());
 		}
 		else if(thecontrol == 83){
 
 			owplus = osctypew[(int) theevent.getValue()];
 			ohplus = osctypeh[(int) theevent.getValue()];
 			otimes = osctypex[(int) theevent.getValue()];
 
 		}
 
 
 
 
 	}
 
 	private void togglefeedback(int i) {
 		int[] payload = {255,i,0};
 		that.xbeeoutput(getaddress(),payload);		
 	}
 
 	public void noteOnReceived(Note note){
 
 		//If the Note is on is on
 		if (isNoteOn){
 
 			//If midi feedback is turned on
 			if (ismidifeedback){
 				int i = (note.getPitch() - firstNote - 4);
 				//This is just a fancy line to change the midi feedback to
 				//be upside down (like an MPC)
 				ledstate[12 - (4 *(i/4))+(i%4)] = note.getVelocity()/126;
 
 			}
 
 		}
 	}
 
 	public void processOSC(int x, int y, int l) {
 
 		if (isExternalFeedback){
 			if (isoscfeedback){
 				if ((x%otimes == 0) && (y%otimes == 0)){
 				if ((x/otimes >= owplus) && (y/otimes >= ohplus)){
 					if ((x/otimes) - owplus < 4 && ((y/otimes) - ohplus) < 4){
 
 
 						ledstate[((x/otimes) - owplus) + (4* ((y/otimes) - ohplus))] = l;
 					}
 				}
 				}
 			}
 		}
 	}
 
 	public void processOSCcol(int x, int y, int l){
 		if (isExternalFeedback){
 
 			if (isoscfeedback){
 
 				if (x%2 == 0){
 
 
 					if ((x/otimes >= owplus) && ( x/otimes < (owplus + 4))){
 						//IF the offset is within the button range
 						if (y/otimes < (ohplus+4)){
 
 							for(int i = ((y/otimes)-ohplus); i < 4; i++) {
 								if ( l  <= 1){
 									if (i >= 0){
 										if ((i * 4) + ((x/otimes) - owplus) < 16){
 											ledstate[(i * 4) + ((x/otimes) - owplus)] =  l;
 										}
 									}
 								}
 								else if ( l  > 1){
 									if (i >= 0){
 										if ((i * 4) + ((x/otimes) - owplus) < 16){
 											ledstate[(i * 4) + ((x/otimes) - owplus)] =  ((l & (1 << (ohplus+i))) > 0)? 1 : 0;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void processOSCrow(int x, int y, int l) {
 		if (isExternalFeedback){
 
 			if (isoscfeedback){
 
 				if (y%otimes == 0){
 
 					if ((y/otimes >= ohplus) && ( y/otimes < (ohplus + 4))){
 						//IF the offset is within the button range
 						if (x/otimes < (owplus+4)){
 
 							for(int i = ((x/otimes)-owplus); i < 4; i++) {
 								if ( l  <= 1){
 									if (i >= 0){
 										if ((i) + (((y/otimes) - ohplus)*4) < 16){
 											ledstate[(i) + (((y/otimes) - ohplus)*4)] =  l;
 										}
 									}
 								}
 								else if ( l  > 1){
 									if (i >= 0){
 										if ((i * 4) + ((y/otimes) - ohplus) < 16){
 											ledstate[(i) + (((y/otimes) - ohplus)*4)] =  ((l & (1 << (owplus+i))) > 0)? 1 : 0;
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public void processOSCall(int l) {
 		if (isExternalFeedback){
 
 			if (isoscfeedback){
 				for (int i = 0; i < 16; i++){
 					ledstate[i] = l;
 				}
 			}
 
 		}
 	}
 }
