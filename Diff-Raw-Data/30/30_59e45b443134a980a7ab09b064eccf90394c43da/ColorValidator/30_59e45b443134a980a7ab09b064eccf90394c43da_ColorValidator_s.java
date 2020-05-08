 import javax.swing.JTextArea;
 
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 
 
 public class ColorValidator extends SheetValidator {
 	static String hexa = "0123456789abcdef";
 	
 	public static boolean isRGB(String st) {
 		if (st.charAt(0) != '#') return false;
 		if (st.length() != 7) return false;
 		
 		for (int i = 1; i < st.length(); i ++) {
 			if (hexa.indexOf(st.charAt(i)) == -1) return false; 
 		}
 		return true;		
 	}
 	
 	@Override
 	public String proceed(XSSFRow row,int x) {
 		String st = row.getCell(0).toString();
 		String result = "";
 		if (st.charAt(0) != idstyle) {
			result.concat("Error at cell " +x +  "A ID must start with a \""+idstyle+"\"\n");
 		}
 		
 		if (id.contains(st)) {
			result.concat("Error at cell " + x + "A ID already existed ! \n");
 		}
 		
 		// checking if cell contains new line 
 		st = row.getCell(1).toString();
 		if (isRGB(st) == false) {
			result.concat(String.format("Cell %d%s does not contains a valid RGB value \n",x,IntToColIndex(1)));
 		}
 		for (int y = 0; y < row.getLastCellNum(); y ++ ) {
 			String cell = row.getCell(y).toString();
 			if (cell.contains(newline)) {
				result.concat(String.format("Cell %d%s contains newline \n",x,IntToColIndex(y)));				
 			}
 		}
 		if (result.equals("")) id.add(st);
 		return result;	
 	}
 	
 	public ColorValidator(XSSFSheet sheet, JTextArea log) {
 		super(sheet, log);
 		// TODO Auto-generated constructor stub
 	}
 	
 }
