 import java.util.ArrayList;
 
 public class student_controler extends user_controler {
 
 	private int student_id;
 
 	private class_plan_table table;
 
 	private boolean get_table(){
 		table=class_plan_table_DB.show(student_id);
 		if(table==null){
 			table = new class_plan_table(student_id);
 			main_UI.message_err("student_controler get_table [take from DB]", false);
 		}else{
 			main_UI.message_err("student_controler get_table [take from DB]", true);
 		}
 		//DB上から取ってくる処理がない
 		return true;
 	}
 
 	public void make_class_plan_table() {
 		main_UI.message_err("make_calss_table", true);
 		ArrayList<Integer> add_list;
 		add_list= student_UI.make_class_plan_table();
 		table.add_classes(add_list);
 	}
 
 	public boolean add_class_plan_table_to_DB() {
 		main_UI.message_err("student_controler add_class_plan_table_to_DB", true);
 		if(table==null){
 			main_UI.message("table is not exist");
 			return false;
 		}
 		class_plan_table_DB.add(table);
 		attend_class_license_DB.delete(student_id);
 		return true;
 
 	}
 
 	public boolean get_attend_class_license() {
 		main_UI.message_err("student_controler get_attend_class_license", true);
 		attend_class_license show =attend_class_license_DB.show(student_id);
 		if(show==null){
 			System.out.println("licecse don't exist yet");
 		}else if(show.get_license()==true){
 			System.out.println(show.get_student_id()+" can attend classes");
 		}else{
 			System.out.println(show.get_student_id()+" can't attend classes");
 		}
 		return false;
 
 	}
 
 	public student_controler(int id) {
 		student_id=id;
		table=class_plan_table_DB.show(student_id);
 	}
 
 	public int controll() {
 		int a=1;
 		main_UI.message_err("学生の現在提出している講義予定を取得 student_controler get_table",get_table());
 		while(a>0){
 			student_UI.show_table(table);
 			a=student_UI.choose_act();
 			switch(a){
 			case 1://make_table
 				make_class_plan_table();
 				break;
 			case 2://submit
 				add_class_plan_table_to_DB();
 				break;
 			case 3://get_license
 				get_attend_class_license();
 				break;
 			}
 		}
 		return main_controler.Logout();
 	}
 
 }
