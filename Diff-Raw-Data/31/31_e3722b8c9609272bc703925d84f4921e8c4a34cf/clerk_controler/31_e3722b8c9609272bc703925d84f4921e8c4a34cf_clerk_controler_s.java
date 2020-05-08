 public class clerk_controler extends user_controler {
 	void clerk_conroler(){
 
 	}
 	public void check_class_table() {
 		main_UI.message_err("clerk_controler", true);
 		int a=1;
		while(a>0){
 			class_plan_table check;
 			check= class_plan_table_DB.get_table_unchecked();
 			clerk_UI.check_class_table(check);
 			a=clerk_UI.decide_add_attend_class_licence_DB(check);
 			boolean j=false;
 			if(a==1){
 				main_UI.message("cecked "+check.get_student_id()+"'s table");
 				j=true;
 				attend_class_license license =new attend_class_license(check.get_student_id(), j);
 				add_attend_class_licence_DB(license);
 			}else if(a==0){
 				main_UI.message("cecked "+check.get_student_id()+"'s table");
 				j=false;
 				attend_class_license license =new attend_class_license(check.get_student_id(), j);
 				add_attend_class_licence_DB(license);
 			}else{
 				main_UI.message("uncecked "+check.get_student_id()+"'s table");
 			}
		}
 	}
 
 	public void add_attend_class_licence_DB(attend_class_license license) {
 		main_UI.message_err("clerk_controler", true);
 		class_plan_table_DB.check_table(license.get_student_id());
 		attend_class_license_DB.add(license);
 
 	}
 
 	public int controll() {
 		main_UI.message_err("clerk_controler", true);
 		int a=1;
 		while(a>0){
 			a=clerk_UI.choose_act();
			while(a>0){
				switch(a){
				case 1:
					check_class_table();
					break;
				}
 			}

 		}
 		return main_controler.Logout();
 
 	}
 
 }
