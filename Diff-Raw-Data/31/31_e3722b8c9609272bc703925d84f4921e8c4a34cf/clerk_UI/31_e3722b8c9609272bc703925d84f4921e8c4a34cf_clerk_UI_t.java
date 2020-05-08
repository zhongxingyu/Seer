 import java.util.Scanner;
 
 public class clerk_UI {
 
 	static Scanner scan = new Scanner(System.in);
 	public static void check_class_table(class_plan_table table) {
 
 		main_UI.message_err("clerk_UI check_class_table", true);
 		main_UI.show_table(table);
 		return;
 	}
 
 	public static int decide_add_attend_class_licence_DB(class_plan_table table) {
 
 		main_UI.message_err("clerk_UI decide_add_attend_class_licence_DB", true);
		System.out.println("1   :add licence");
		System.out.println("0   :don't add licence");
		System.out.println("else:exit");
 		int res= scan.nextInt();
 		return res;
 	}
 
 	public static int choose_act(){
 		main_UI.message_err("clerkt_UI choose_act", true);
 		int res;
 		System.out.println("input what you want");
 		System.out.println("1:show uncheck table");
 		System.out.println("0:logged out");
 		res=scan.nextInt();
 		return res;
 	}
 
 }
