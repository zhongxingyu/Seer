 package com.inspedio.system.helper.payment;
 
 import javax.microedition.lcdui.Graphics;
 
 import com.inspedio.entity.primitive.InsCallback;
 import com.inspedio.enums.KeyCode;
 import com.inspedio.system.core.InsGlobal;
 import com.inspedio.system.helper.InsKeys;
 import com.inspedio.system.helper.InsPointer;
 import com.tqm.tqp.EventListener;
 import com.tqm.tqp.PaymentListener;
 import com.tqm.tqp.TequilaPlanetApplication;
 import com.tqm.tqp.VirtualGoodDetails;
 
 public class InsPaymentTequila implements PaymentListener, EventListener{
 
 	private static TequilaPlanetApplication teqInstance = null;
 	public static boolean doneInit = false;
 	
 	private static InsPaymentTequila instance = null;
 	
 	private static InsCallback paymentSuccessCallback = null;
 	
 	private static InsCallback paymentFailedCallback = null;
 	
 	private InsPaymentTequila(){
 	}
 	
 	public static InsPaymentTequila getInstance(){
 		if(instance == null){
 			instance = new InsPaymentTequila();
 		}
 		return instance;
 	}
 	
 	public static void init(){
 		if(!doneInit){
 			System.out.println("Initiating Tequila Instance");
 			teqInstance = TequilaPlanetApplication.getInstance(InsGlobal.midlet, TequilaPlanetApplication.LANG_ENGLISH);
 			teqInstance.addPaymentListener(getInstance());
 			teqInstance.setEventListener(getInstance());
 			doneInit = true;
 			System.out.println("Tequila Instance succesfully Created");
 		}
 		showAdvertisement();
 	}
 	
 	public static void showAdvertisement(){
 		System.out.println("Trying to show Advertisement Dialog");
		getTequilaInstance().showAdvertisementDialog(true);
 		System.out.println("Advertisement Dialog succesfully shown");
 	}
 	
 	public static TequilaPlanetApplication getTequilaInstance(){
 		if(teqInstance == null){
 			init();
 		}
 		return teqInstance;
 	}
 	
 	public static VirtualGoodDetails[] getVirtualGoods(){
 		System.out.println("Trying to get Virtual Goods Detail");
 		VirtualGoodDetails[] vg = getTequilaInstance().getVirtualGoodDetails();
 		System.out.println("VG Data acquired.");
 		if(vg != null){
 			System.out.println("VG Instance Count : " + vg.length);
 		} else {
 			System.out.println("VG Data is null");
 		}
 		
 		return vg;
 	}
 	
 	public static void requestPayment(VirtualGoodDetails vg, InsCallback successCallback, InsCallback failedCallback){
 		paymentSuccessCallback = successCallback;
 		paymentFailedCallback = failedCallback;
 		System.out.println("Trying to Request VG Payment");
 		getTequilaInstance().showVirtualGoodPaymentDialog(vg);
 		System.out.println("VG Payment Request Sent");
 	}
 	
 	public void notifyPaymentStatus(int arg) {
 		switch (arg) {
 			case PAYMENT_STATUS_ACCEPTED:
 				System.out.println("Payment Accepted");
 				if(paymentSuccessCallback != null){
 					paymentSuccessCallback.call();
 				}
 				break;
 			case PAYMENT_STATUS_REJECTED:
 				System.out.println("Payment Rejected");
 				if(paymentFailedCallback != null){
 					paymentFailedCallback.call();
 				}
 				break;
 		}
 	}
 	
 	public void update(){
 		this.handleKeyEvent(InsGlobal.keys);
 		this.handlePointerEvent(InsGlobal.pointer);
 	}
 	
 	public void handleKeyEvent(InsKeys key){
 		for(int i = 0; i < 7; i++){
 			if(key.justPressed(KeyCode.getKey(i))){
 				getTequilaInstance().keyActionPressed(translateKeyCode(i));
 			}
 			
 			if(key.justReleased(KeyCode.getKey(i))){
 				getTequilaInstance().keyActionReleased(translateKeyCode(i));
 			}
 		}
 	}
 		
 	public int translateKeyCode(int Code){
 		switch (Code) {
 			case 0:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_LEFT;
 			case 1:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_RIGHT;
 			case 2:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_UP;
 			case 3:
 				return TequilaPlanetApplication.KEY_ACTION_ARROW_DOWN;
 			case 4:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_MIDDLE;
 			case 5:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_LEFT;
 			case 6:
 				return TequilaPlanetApplication.KEY_ACTION_SOFTKEY_RIGHT;
 		}
 		return 0;
 	}
 	
 	public void handlePointerEvent(InsPointer pointer){
 		for(int i = 0; i < pointer.pressed.length; i++){
 			getTequilaInstance().pointerPressed(pointer.pressed[i].x, pointer.pressed[i].y);
 		}
 		
 		for(int i = 0; i < pointer.released.length; i++){
 			getTequilaInstance().pointerReleased(pointer.released[i].x, pointer.released[i].y);
 		}
 		
 		for(int i = 0; i < pointer.dragged.length; i++){
 			getTequilaInstance().pointerDragged(pointer.dragged[i].x, pointer.dragged[i].y);
 		}
 	}
 	
 	public void draw(Graphics g){
 		getTequilaInstance().paint(g);
 	}
 	
 	public void dailyReward(int arg0) {
 	}
 
 	public void gainFocus() {
 		System.out.println("Application gain Focus");
 		InsGlobal.onFocusPayment = false;
 		InsGlobal.resumeGame();
 		System.out.println("Application is Resumed");
 	}
 
 	public boolean isAcceptingDailyReward() {
 		return false;
 	}
 
 	public boolean isAcceptingImages() {
 		return false;
 	}
 
 	public void loseFocus() {
 		System.out.println("Application Lost Focus");
 		InsGlobal.onFocusPayment = true;
 		InsGlobal.pauseGame();
 		System.out.println("Application is Paused");
 	}
 
 	public void requestQuit() {
 		System.out.println("Application need to Exit");
 		InsGlobal.save.save();
 		InsGlobal.exitGame();
 	}
 
 	
 }
