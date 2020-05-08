 package lea.generator;
 
 import java.io.IOException;
 import java.util.Map;
 
 import lea.ConstantTable;
 import lea.constants.Constant;
 
 public class ConstantGenerator {
 
 	ConstantTable cstTable;
 	NameGenerator ng;
 
 	public ConstantGenerator(ConstantTable cstTable) throws IOException {
 		this.cstTable = cstTable;
 		this.ng = new NameGenerator();
 	}
 
 	public void generate(CodeWriter cw) throws IOException {
 		for (Map.Entry<String, Constant> entry : cstTable.entrySet()) {
 			cw.writeLine("public final " + entry.getValue().getType().toString()
 					+ " " + ng.generateName(entry.getKey()) + " = "
 					+ entry.getValue().toString() + ";");
 
 		}
 	}
 }
