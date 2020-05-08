 import java.util.ArrayList;
 
 public class class_plan_table_DB extends DB{
 	private static String DB_name ="class_plan_table_DB.db";	private static ArrayList<class_plan_table> getAllList(){
 		ArrayList<class_plan_table> res=new ArrayList<class_plan_table>();
 		ArrayList<String> input_str=input(DB_name);
 		for(int i=0;i<input_str.size();i++){
 			res.add(new class_plan_table(input_str.get(i)));
 		}
 		return res;
 	}
 	private static void rewrite(ArrayList<class_plan_table> licenses){
 		ArrayList<String> outputstr=new ArrayList<String>();
 		for(int i=0;i<licenses.size();i++){
 			outputstr.add(licenses.get(i).toString());
 		}
 		init(DB_name);
 		output(DB_name, outputstr, false);
 	}
 	public static void add(class_plan_table license) {
		delete(license.get_student_id());
 		ArrayList<String> output_strs=new ArrayList<String>();
 		output_strs.add(license.toString());
 		output(DB_name,output_strs,true);
 
 		main_UI.message_err("class_plan_table_DB add()", true);
 	}
 
 	public static void delete(int student_id) {
 		ArrayList<class_plan_table> licenses =getAllList();
 		for(int i=0;i<licenses.size();i++){
 			while(licenses.get(i).get_student_id()==student_id){
 				licenses.remove(i);
				if((i<licenses.size())==false)break;
 			}
 		}
 		rewrite(licenses);
 		main_UI.message_err("class_plan_table_DB delete()", true);
 	}
 
 	public static class_plan_table show(int student_id) {
 		main_UI.message_err("class_plan_table_DB show()", true);
 		class_plan_table res=null;
 		ArrayList<String> input_str=input(DB_name);
 		for(int i=0;i<input_str.size();i++){
 			class_plan_table tmp=null;
 			tmp=new class_plan_table(input_str.get(i));
 			if(tmp.get_student_id()==student_id){
 				res =tmp;
 				break;
 			}
 		}
 		return res;
 	}
 
 
 
 
 	public static class_plan_table get_table_unchecked() {
 		main_UI.message_err("class_plan_table_DB show()", true);
 		class_plan_table res=null;
 		ArrayList<String> input_str=input(DB_name);
 		for(int i=0;i<input_str.size();i++){
 			class_plan_table tmp=null;
 			tmp=new class_plan_table(input_str.get(i));
 			if(tmp.get_checked()==false){
 				res =tmp;
 				break;
 			}
 		}
 		return res;
 	}
 
 	public static void check_table(int student_id) {
		main_UI.message_err("class_plan_tabele_DB check_table", true);
 
 		ArrayList<class_plan_table> licenses =getAllList();
 		for(int i=0;i<licenses.size();i++){
 			if(licenses.get(i).get_student_id()==student_id){
 				licenses.get(i).check();
 			}
 		}
 		rewrite(licenses);
 		main_UI.message_err("class_plan_table_DB delete()", true);
 
 
 	}
 
 }
