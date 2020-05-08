 import java.util.ArrayList;
 import java.util.Scanner;
 
 public class main_UI {
 	static Scanner scan = new Scanner(System.in);
 
 	public static void message(String a){
 		System.out.println(a);
 	}
 
 	public static ArrayList<Integer> Login() {
 		ArrayList<Integer> ID=new ArrayList<Integer>();
 		System.out.println("plese enter your ID");
 		System.out.println("your ID must be integer");
 		int a;
 
 		a=scan.nextInt();
 		System.out.println("input complete");
 		ID.add(a);
 		return ID;
 	}
 
 	public static void LoginStatus(int ID){
 		if(ID==0){
 			System.out.println("you are clerk  Please do your jobs");
 		}else if(ID>0){
 			System.out.println("your id is ["+ID+"]  Login succeeded");
 		}else{
 			System.out.println("login faild");
 		}
 	}
 
 	public static int Logout() {
 		System.out.println("logouted");
 		System.out.println("do you want next login?");
 		System.out.println("y:1 n:0");
 		int a=scan.nextInt();
 		int res;
 		if(a>0){
 			res=-1;
 		}else {
 			res =1;
 		}
 		return res;
 
 	}
 
 }
