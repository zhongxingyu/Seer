 public class CLI extends Interface {
     private Database db;
 
     public static void main(String[] args) {
 	db = new Database(getConnection());
         new CLI().run();
     }
 
     @Override
     public void insert() {
         String table_name = getStringPrompt("Which table?");
         List<String> columns = getStringPrompt("Which columns?").split(" *, *");
         List<String> values = getStringPrompt("Which values?").split(" *, *");
 
         try {
             db.insert(table_name,columns,values);
         } catch (Exception e) {
             System.out.println("Invalid syntax!");
             System.out.println(e);
         }
     }
 
     @Override
     public void update() {
         String table_name = getStringPrompt("Which table?");
        List<String> set = getStringPrompt("Which set instruction?").split(" *, *");
 	String condition = getStringPrompt("Which condition?");
 	List<String> columns = new ArrayList();
 	List<String> valuess = new ArrayList();
 	
 
 	for (String x:set){
 	    List<String> tuple=x.split(" *= *");
 	    if (tuple.size()!=2){
 		System.out.println("Invalid syntax!");
 		return;
 	    }
 	    columns.add(tuple.get(0));
 	    values.add(tuple.get(1));
 	}
 
         try {
             db.insert(table_name,columns,values,condition);
         } catch (Exception e) {
             System.out.println("Invalid syntax!");
             System.out.println(e);
         }
     }
 
     @Override
     public void remove() {
 
         String table_name = getStringPrompt("Which table?");
 	String condition = getStringPrompt("Which condition?");
 
         try {
             db.remove(table_name,condition);
         } catch (Exception e) {
             System.out.println("Invalid syntax!");
             System.out.println(e);
         }
     }
     
     @Override
     public void select() {
         String table_name = getStringPrompt("Which table?");
         List<String> columns = getStringPrompt("Which columns?").split(" *, *");
 	String condition = getStringPrompt("Which condition?");
 	ResultSet r;
 
         try {
             r=db.select(table_name,columns,condition);
         } catch (Exception e) {
             System.out.println("Invalid syntax!");
             System.out.println(e);
 	    return;
         }
 	
 	printResult(r);
     }
 
     public static void printResult(ResultSet rs) {
 	try {
 	    if(rs != null){
 		while(rs.next()){
 		    for (int i = 1; true; i++) {
 			try{
 			    if(i > 1)
 				System.out.print(",\t");
 			    System.out.print(rs.getString(i));
 			    //If there are no more strings, break
 			}catch(Exception c){break;}
 		    }
 		    System.out.println();
 		}
 	    }
 	} catch(Exception c) {
 	    System.out.println("Ni borde inte se detta.");
 	}
     }
 
 }
