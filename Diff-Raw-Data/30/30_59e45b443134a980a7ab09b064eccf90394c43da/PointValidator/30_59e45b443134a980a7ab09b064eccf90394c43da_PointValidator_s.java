 import java.util.ArrayList;
 
 import javax.swing.JTextArea;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 
 
 public class PointValidator extends SheetValidator {
 	ArrayList<Integer> colorcell = new ArrayList<Integer>();
 	public PointValidator(XSSFSheet sheet, JTextArea log) {
 		super(sheet, log);
 		colorcell.add(15);
 		colorcell.add(23);
 		// TODO Auto-generated constructor stub
 	}
 	
 	@Override	
 	public String proceed(XSSFRow row,int x) {
 		// TODO Auto-generated method stub
 		// check expert style
 		String result = "";
 		String st = row.getCell(8).toString();
 		if (st.equals("") == false) {
 			for (int y = 0; y < row.getLastCellNum(); y ++ ) {
 				Cell c = row.getCell(y);
 				if (c == null ) continue;
 				String cell = c.toString();
 				if (cell.equals("") == false) {
					result.concat(String.format("Error: Row %d has an expert style so others column must be empty.\n",x));
 				}
 			}
 		}
 		
 		//checking id name
 		st = row.getCell(5).toString();
 		if (st.charAt(0) != idstyle) {
			return ("Error: Row " + x + " ID must start with a \""+idstyle+"\"\n");
 		}
 		
 		if (id.contains(st)) {
			return ("Error: Row " + x + " ID already existed ! \n");
 		}
 		
 		for (int i = 0; i < colorcell.size(); i ++) {
 			Cell c = row.getCell(colorcell.get(i));
 			if (c != null) {
 				st = c.toString();
 				if (!(ColorValidator.isRGB(st) || ColorValidator.isin(st))) {
					result.concat(String.format("Error: Cell %d%s is not a valid color",x,IntToColIndex(colorcell.get(i))));
 				}
 			}
 		}
 		
 		
 		// checking if cell contains new line
 		
 		for (int y = 0; y < row.getLastCellNum(); y ++ ) {
 			String cell = row.getCell(y).toString();
 			if (cell.contains(newline)) {
				return String.format("Error: Cell %d%s contains newline \n",x,IntToColIndex(y));
 			}
 		}
 		return result;
 	}
 }
