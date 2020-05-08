 package elw.dp.mips;
 
 import elw.dp.mips.asm.Data;
 
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 
 public class Instruction {
 	//	binary code areas
 	public static final String T_REG_D = "$d";
 	public static final String T_REG_S = "$s";
 	public static final String T_REG_T = "$t";
 	public static final String T_IMM26 = "imm26";
 	public static final String T_IMM16 = "imm16";
 	public static final String T_H5 = "h5";
 	//	labels
 	public static final String T_ADDR16 = "addr16";
 	public static final String T_ADDR26 = "addr26";
 
 	public static final Map<String, String> tokenToMask = createTokenToMaskMap();
 
 	protected static Map<String, String> createTokenToMaskMap() {
 		final Map<String, String> map = new LinkedHashMap<String, String>();
 
 		map.put(T_IMM26, "iiiiiiiiiiiiiiiiiiiiiiiiii");
 		map.put(T_IMM16, "iiiiiiiiiiiiiiii");
 		map.put(T_REG_D, "ddddd");
 		map.put(T_REG_T, "ttttt");
 		map.put(T_REG_S, "sssss");
 		map.put(T_H5, "hhhhh");
 
 		return map;
 	}
 
 	private static final Pattern PATTERN_COMPLETE = Pattern.compile("^[-01]+$");
 
 	protected final InstructionDesc desc;
 	protected final String codeLine;
 	protected final int index;
 	protected final int lineIndex;
 	protected final String[] labels;
 
 	protected Reg s = Reg.fp;
 	protected Reg t = Reg.fp;
 	protected Reg d = Reg.fp;
 
 	protected int i16;
 	protected int i26;
 	protected int h;
 
 	protected String addr16 = "";
 	protected String addr26 = "";
 
 
 	public Instruction(InstructionDesc desc, String codeLine, int index, int lineIndex, String[] labels) {
 		this.desc = desc;
 		this.codeLine = codeLine;
 		this.index = index;
 		this.lineIndex = lineIndex;
 		this.labels = labels;
 	}
 
 	public String getOpName() {
 		final String syntax = desc.syntax();
 		final int firstSpace = syntax.indexOf(" ");
 
 		return firstSpace < 0 ? syntax : syntax.substring(0, firstSpace);
 	}
 
 	public String getBinaryCode() {
 		StringBuffer res = new StringBuffer(desc.template());
 
 		for (String token : tokenToMask.keySet()) {
 			final String mask = tokenToMask.get(token);
 			final int maskStart = res.indexOf(mask);
 			if (maskStart >= 0) {
 				res.replace(maskStart, maskStart + mask.length(), Data.str(getBits(token), 2, getWidth(token)));
 			}
 		}
 
 		return res.toString();
 	}
 
 	public boolean isAssembled() {
 		return PATTERN_COMPLETE.matcher(getBinaryCode()).matches();
 	}
 
 	public int getIndex() {
 		return index;
 	}
 
 	public String getCodeLine() {
 		return codeLine;
 	}
 
 	public String toString() {
 		return "'" + desc.syntax() + "' : '" + getBinaryCode() + "'";
 	}
 
 	public void setReg(String regId, Reg reg) {
 		if (T_REG_D.equals(regId)) {
 			d = reg;
 		} else if (T_REG_S.equals(regId)) {
 			s = reg;
 		} else if (T_REG_T.equals(regId)) {
 			t = reg;
 		} else {
 			throw new IllegalArgumentException("unknown regId: '" + regId + "'");
 		}
 	}
 
 	public Reg getReg(String regId) {
 		if (T_REG_D.equals(regId)) {
 			return d;
 		}
 		if (T_REG_S.equals(regId)) {
 			return s;
 		}
 		if (T_REG_T.equals(regId)) {
 			return t;
 		}
 
 		throw new IllegalArgumentException("unknown regId: '" + regId + "'");
 	}
 
 	public int getBits(String id) {
 		if (T_REG_D.equals(id)) {
 			return getReg(T_REG_D).ordinal();
 		}
 		if (T_REG_S.equals(id)) {
 			return getReg(T_REG_S).ordinal();
 		}
 		if (T_REG_T.equals(id)) {
			return getReg(T_REG_D).ordinal();
 		}
 		if (T_H5.equals(id)) {
 			return (int) Data.comp(h, 5);
 		}
 		if (T_IMM16.equals(id)) {
 			return desc.unsigned() ? (int) Data.comp(i16, 16) : i16;
 		}
 		if (T_ADDR16.equals(id)) {
 			throw new IllegalArgumentException("addr16 is a string");
 		}
 		if (T_ADDR26.equals(id)) {
 			throw new IllegalArgumentException("addr26 is a string");
 		}
 		if (T_IMM26.equals(id)) {
 			return desc.unsigned() ? (int) Data.comp(i26, 26) : i26;
 		}
 
 		throw new IllegalArgumentException("unknown token id: '" + id + "'");
 	}
 
 	public void setBits(String id, int bits) {
 		if (T_REG_D.equals(id)) {
 			setReg(T_REG_D, Reg.values()[bits]);
 		} else if (T_REG_S.equals(id)) {
 			setReg(T_REG_S, Reg.values()[bits]);
 		} else if (T_REG_T.equals(id)) {
 			setReg(T_REG_T, Reg.values()[bits]);
 		} else if (T_H5.equals(id)) {
 			h = bits;
 		} else if (T_IMM16.equals(id)) {
 			i16 = bits;
 		} else if (T_ADDR16.equals(id)) {
 			throw new IllegalArgumentException("addr16 is a string");
 		} else if (T_ADDR26.equals(id)) {
 			throw new IllegalArgumentException("addr26 is a string");
 		} else if (T_IMM26.equals(id)) {
 			i26 = bits;
 		} else {
 			throw new IllegalArgumentException("unknown token id: '" + id + "'");
 		}
 	}
 
 	public static int getWidth(String id) {
 		if (T_REG_D.equals(id)) {
 			return 5;
 		}
 		if (T_REG_S.equals(id)) {
 			return 5;
 		}
 		if (T_REG_T.equals(id)) {
 			return 5;
 		}
 		if (T_H5.equals(id)) {
 			return 5;
 		}
 		if (T_IMM16.equals(id)) {
 			return 16;
 		}
 		if (T_ADDR16.equals(id)) {
 			return 16;
 		}
 		if (T_ADDR26.equals(id)) {
 			return 26;
 		}
 		if (T_IMM26.equals(id)) {
 			return 26;
 		}
 
 		throw new IllegalArgumentException("unknown token id: '" + id + "'");
 	}
 
 	public String getAddr(final String addrId) {
 		if (T_ADDR16.equals(addrId)) {
 			return addr16;
 		}
 		if (T_ADDR26.equals(addrId)) {
 			return addr26;
 		}
 		throw new IllegalArgumentException("unknown address id: '" + addrId + "'");
 	}
 
 	public void setAddr(final String addrId, String addr) {
 		if (T_ADDR16.equals(addrId)) {
 			this.addr16 = addr;
 		} else if (T_ADDR26.equals(addrId)) {
 			this.addr26 = addr;
 		} else {
 			throw new IllegalArgumentException("unknown address id: '" + addrId + "'");
 		}
 	}
 
 	public String[] getLabels() {
 		return labels;
 	}
 
 	public boolean resolve(int codeBase, Map<String, Integer> labelIndex) {
 		if (addr16.length() > 0) {
 			final Integer offs16 = labelIndex.get(addr16);
 			if (offs16 != null) {
 				setBits(T_IMM16, offs16 - index);
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		if (addr26.length() > 0) {
 			final Integer offs26 = labelIndex.get(addr26);
 			if (offs26 != null) {
 				setBits(T_IMM26, (codeBase >> 2) + offs26);
 				return false;
 			} else {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public String getAddr() {
 		if (addr16.length() > 0) {
 			return addr16;
 		}
 		if (addr26.length() > 0) {
 			return addr26;
 		}
 		return "";
 	}
 
 	public int getLineIndex() {
 		return lineIndex;
 	}
 }
