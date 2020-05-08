 package machineTest;
 import java.io.*;
 public class VendingMachine{
 	private static int c50 = 0;
 	private static int c100 = 0;
 	private static int c500 = 0;
 	private static int coinAmount = 0;
 	private static boolean retFlag = false;
 	private static boolean iovrFlag = false;
 	private static boolean sovrFlag = false;
 	private static boolean r50Flag = false;
 	private static boolean r100Flag = false;
 	private static boolean r500Flag = false;
 	private static boolean req150Flag = false;
 	private static boolean req200Flag = false;
 	private static boolean req450Flag = false;
 	private static boolean twiceFlag = false;
 	private static boolean sel150Flag = false;
 	private static boolean sel200Flag = false;
 	private static boolean sel450Flag = false;
 	private static boolean[] selFlag = {sel150Flag,sel200Flag,sel450Flag};
 	private static boolean no50Flag = false;
 	private static boolean no100Flag = false;
 	private static int chargeCoin50 = 0;
 	private static int chargeCoin100 = 0;
 	private static int chargeCoin500 = 0;
 	
 	public VendingMachine(){
 	System.out.println("VendingMachine start.");
 	}	
 	/////////////////////////not io/////////////////////////
 	public boolean judgeError(String coin, int num){
 		if(coin.equals("c50")){
 			if((coinAmount + c50*num)>1000){
 			iovrFlag = true;
 			}	
 		}else if(coin.equals("c100")){
 			if((coinAmount + c100*num)>1000){
 				iovrFlag = true;
 			}
 		}else if(coin.equals("c500")){
 			if((coinAmount + c500*500)>1000){
 				iovrFlag = true;
 			}
 		}
		if(num>100){
 			sovrFlag = true;
 		}
 		return (sovrFlag || iovrFlag);
 	}
 	public void setAmount(){
 		coinAmount = c50*50 + c100*100 + c500*500;
 	}
 	public void setCharge(){
 		while(coinAmount!=0 && retFlag == true){
 			if(coinAmount - 500 >= 0 && c500 > 0){
 				coinAmount-=500;
 				c500--;
 				chargeCoin500++;
 			}else if(coinAmount -100 >= 0 && c100 > 0) {
 				coinAmount-=100;
 				c100--;
 				chargeCoin100++;
 			}else if(coinAmount -50 >= 0 && c50 > 0){
 				coinAmount-=50;
 				c50--;
 				chargeCoin50++;
 			}
 		}
 	}
 	//////////////////////not io///////////////////////////////
 	
 	public void ioC50(){
 		if(!judgeError("c50",c50)){
 			c50++;
 			System.out.println("add coin50\nc50: "+ c50);
 			setAmount();
 		}
 		
 	}
 	public void ioC100(){
 		if(!judgeError("c50",c50)){
 			c100++;
 			System.out.println("add coin100\nc100: " + c100);
 			setAmount();
 		}
 	}
 	public void ioC500(){
 		if(!judgeError("c500",c500)){
 			c500++;
 			System.out.println("add coin500\nc500: " + c500);
 			setAmount();
 		}
 		
 	}
 	public void ioREQ150(){
 		req150Flag = true;
 	}
 	public void ioREQ200(){
 		req200Flag = true;
 	}
 	public void ioREQ450(){
 		req450Flag = true;
 	}
 	public void ioTWICE(){
 		twiceFlag = true;
 	}
 	public void ioRET(){
 		retFlag = true;
 		setCharge();
 	}
 	public void ioRST(){}
 	public void ioACK(){
 		if(twiceFlag==false){
 			for(int i=0;i<selFlag.length;i++){
 				selFlag[i] = false;
 			}
 		}
 	}
 	public void ioSTB(){}
 	public boolean ioIOVR(){
 		return iovrFlag;
 	}
 	public boolean ioSOVR(){
 		return sovrFlag;
 	}
 	public boolean ioSEL150(){
 		if(coinAmount >= 150 && req150Flag==true){
 			sel150Flag = true;
 			coinAmount -= 150;
 		}
 		setCharge();
 		return sel150Flag;
 	}
 	public boolean ioSEL200(){
 		if(coinAmount >= 200 && req200Flag==true){
 			sel200Flag = true;
 			coinAmount -= 200;
 		}
 		setCharge();
 		return sel200Flag;
 	}
 	public boolean ioSEL450(){
 		if(coinAmount >= 450 && req450Flag==true){
 			sel450Flag = true;
 			coinAmount -= 450;
 		}
 		setCharge();
 		return sel450Flag;
 	}
 	
 	
 	public boolean ioR50(){
 	if(chargeCoin50 > 0){
 		r50Flag = true;
 		chargeCoin50--;
 	}else{
 		r50Flag = false;
 	}
 	return r50Flag;
 	}
 	public boolean ioR100(){
 		if(chargeCoin100 > 0){
 			r50Flag = true;
 			chargeCoin100--;
 		}else{
 			r100Flag = false;
 		}
 		return r100Flag;
 	}
 	public boolean ioR500(){
 		if(chargeCoin500 > 0){
 			r50Flag = true;
 			chargeCoin500--;
 		}else{
 			r500Flag = false;
 		}
 		return r500Flag;
 	}
 	public boolean ioNO50(){
 		if(c50==0){
 			no50Flag = true;
 		}
 		return no50Flag;
 	}
 	public boolean ioNO100(){
 		if(c100==0){
 			no100Flag = true;
 		}
 		return no100Flag;
 	}
 	
 	public void ioInit(){}
 	
 	
 	
 	
 	
 }
