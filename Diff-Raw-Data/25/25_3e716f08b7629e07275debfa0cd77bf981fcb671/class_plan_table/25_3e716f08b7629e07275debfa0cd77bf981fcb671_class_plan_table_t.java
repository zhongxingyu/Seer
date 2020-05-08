 import java.util.ArrayList;
 
 public class class_plan_table {
 
 	private ArrayList<Integer> class_id;
 
 	private int student_id;
 
 
 	public boolean add_classes(ArrayList<Integer> list){
		if(list==null){
			return false;
		}
 		class_id.addAll(list);
 		return true;
 	}
 	public boolean add_class(int id) {
 		this.class_id.add(id);
 		return true;
 	}
 
 	public boolean delete_class(int id) {
		if(this.class_id.remove(id)==id){
			return true;
		}else{
			return false;
		}
 	}
 
 	public class_plan_table(int s_id) {
 		student_id=s_id;
 		class_id=new ArrayList<Integer>();
 	}
 
 	public int get_student_id(){
 		return student_id;
 	}
 
 	public ArrayList<Integer> get_class_ids(){
 		return class_id;
 	}
 
 }
