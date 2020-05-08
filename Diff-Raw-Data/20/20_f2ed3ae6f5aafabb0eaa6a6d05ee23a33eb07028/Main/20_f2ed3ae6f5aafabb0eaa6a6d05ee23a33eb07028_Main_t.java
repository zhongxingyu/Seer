 import static LJava.LJ.DIFFER;
 import static LJava.LJ.OR;
 import static LJava.LJ.associate;
 import static LJava.LJ.c;
 import static LJava.LJ.lz;
 import static LJava.LJ.r;
 import static LJava.LJ.varArray;
 import static LJava.MathFormulas.abs;
 
 import javax.swing.JOptionPane;
 
 import LJava.Constraint;
 import LJava.Group;
 import LJava.Lazy;
 import LJava.Variable;
 import LJava.VariableMap;
 
 public class Main {
 	
 	public static void main(String[] args) {
		
 		Object[] values=new Object[] {1,2,3,4,5,6,7,8};
 		associate(new Group("testLazyAll",values));
 		Variable[] vars = varArray(8);
 		Constraint[] cons=new Constraint[9];
 		cons[0]=c(abs,1,vars[0],vars[2]);
 		cons[1]=c(cons[0],OR,c(abs,1,vars[1],vars[2]));
 		cons[2]=c(cons[1],OR,c(abs,1,vars[1],vars[4]));
 		cons[3]=c(cons[2],OR,c(abs,1,vars[2],vars[3]));
 		cons[4]=c(cons[3],OR,c(abs,1,vars[2],vars[5]));
 		cons[5]=c(cons[4],OR,c(abs,1,vars[3],vars[6]));
 		cons[6]=c(cons[5],OR,c(abs,1,vars[4],vars[5]));
 		cons[7]=c(cons[6],OR,c(abs,1,vars[5],vars[6]));
 		cons[8]=c(cons[7],OR,c(abs,1,vars[5],vars[7]));
 		Lazy<Constraint, VariableMap> lazy=lz(r("testLazyAll",vars),DIFFER,cons[8]);
		
 		VariableMap m=new VariableMap();
 		while (!(m=lazy.lz()).isEmpty()) {
 			print(m.toArray(vars)); 
 			JOptionPane.showConfirmDialog(null, "continue?");
 		}
		
 	}
 	
 	
 	
 	public static void print(Variable[] vars) {
 		System.out.println("\n\n");
 		System.out.println("\t"+vars[0]);
 		System.out.println(vars[1]+"\t"+vars[2]+"\t"+vars[3]);
 		System.out.println(vars[4]+"\t"+vars[5]+"\t"+vars[6]);
 		System.out.println("\t"+vars[7]);
 	}
 	
 }
