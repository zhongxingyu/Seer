 package elw.dp.app;
 
 import elw.dp.mips.Instruction;
 import elw.dp.mips.Instructions;
 import elw.dp.mips.asm.Data;
 import org.akraievoy.gear.G4Str;
 
 import javax.swing.table.AbstractTableModel;
 
 public class InstructionsTableModel extends AbstractTableModel {
 	public static final String COL_ADDR = "Addr";
 	public static final String COL_BIN = "Hex";
 	public static final String COL_LABELS = "Labels";
 	public static final String COL_CODE = "Code";
 	public static final String COL_ACC = "rw";
 
 	protected final String[] columns = new String[]{COL_ACC, COL_ADDR, COL_BIN, COL_CODE, COL_LABELS};
 	protected final Instructions instructions;
 
 	public InstructionsTableModel(Instructions instructions) {
 		this.instructions = instructions;
 	}
 
 	public String getColumnName(int column) {
 		return columns[column];
 	}
 
 	public int getColumnCount() {
 		return columns.length;
 	}
 
 	public int getRowCount() {
 		return instructions.getSize();
 	}
 
 	public Object getValueAt(int rowIndex, int columnIndex) {
 		final String colName = columns[columnIndex];
 		final int address = instructions.getAddressAt(rowIndex);
 
 		if (COL_ADDR.equals(colName)) {
 			return Data.int2hex(address, 2);
 		} else if (COL_BIN.equals(colName)) {
 			final Instruction internal = instructions.getInternal(address);
 			if (internal != null) {
 				final String code = internal.getBinaryCode();
 				return groupBy(code, 4);
 			}
 		} else if (COL_CODE.equals(colName)) {
 			final Instruction internal = instructions.getInternal(address);
 			if (internal != null) {
				return internal.getCodeLine().replace("\t", "  ");
 			}
 		} else if (COL_ACC.equals(colName)) {
 			return getAccessMod(address);
 		} else if (COL_LABELS.equals(colName)) {
 			final Instruction internal = instructions.getInternal(address);
 			if (internal != null) {
 				return G4Str.join(internal.getLabels(), ", ");
 			}
 		}
 
 		return "";
 	}
 
 	private String groupBy(final String code, final int group) {
 		if (code.length() > group) {
 			return code.substring(0, group) + " " + groupBy(code.substring(group), group);
 		}
 
 		return code;
 	}
 
 	protected String getAccessMod(int rowIndex) {
 		StringBuffer accessMod = new StringBuffer();
 
 		if (instructions.getReadAddresses().contains(rowIndex)) {
 			accessMod.append("r");
 		}
 
 		return accessMod.toString();
 	}
 }
