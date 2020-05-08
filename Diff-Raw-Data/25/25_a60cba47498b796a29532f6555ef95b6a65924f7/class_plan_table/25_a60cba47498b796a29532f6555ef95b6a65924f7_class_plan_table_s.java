 import java.util.ArrayList;
 
 public class class_plan_table {
 
 	private ArrayList<Integer> class_id;
 
 	private int student_id;
 	private boolean checked;
 	public class_plan_table(String s){
 		String[] split=s.split(":");
 		student_id=Integer.parseInt(split[0]);
 		checked=Boolean.parseBoolean(split[1]);
 		for(int i=2;i<split.length;i++){
 			class_id.add(Integer.parseInt(split[i]));
 		}
 	}
 
 	public String toString(){
 		String res=String.valueOf(student_id)+":"+checked;
 		for(int i=0;i<class_id.size();i++){
			res+=":"+class_id;
 		}
 		return res;
 	}
 
 
 	public void check(){
 		checked=true;
 	}
 
 	public boolean add_classes(ArrayList<Integer> list){
 		main_UI.message_err("class_plan_table add_classes", true);
 		if(list==null){
 			return false;
 		}
 		int size=list.size();
 		for(int i=0;i<size;i++){
 			if(class_id.indexOf(list.get(i))<0){
 				class_id.add(list.get(i));
 			}
 		}
 		return true;
 	}
 	public boolean add_class(int id) {
 		main_UI.message_err("class_plan_table add_class", true);
 
 		this.class_id.add(id);
 		return true;
 	}
 
 	public boolean delete_class(int id) {
 		main_UI.message_err("class_plan_table delete_class", true);
 		if(this.class_id.remove(id)==id){
 			return true;
 		}else{
 			return false;
 		}
 	}
 
 	public class_plan_table(int s_id) {
 		main_UI.message_err("new class_plan_table ", true);
 		student_id=s_id;
 		checked=false;
 		class_id=new ArrayList<Integer>();
 	}
 
 	public int get_student_id(){
 		main_UI.message_err("class_plan_table get_student_id", true);
 		return student_id;
 	}
 
 	public ArrayList<Integer> get_class_ids(){
 		main_UI.message_err("class_plan_table get_class_ids", true);
 		if(class_id==null)class_id=new ArrayList<Integer>();
 		return class_id;
 	}
 
 	public boolean get_checked(){
 		main_UI.message_err("class_plan_table get_checked", true);
 		return checked;
 	}
 
 }
