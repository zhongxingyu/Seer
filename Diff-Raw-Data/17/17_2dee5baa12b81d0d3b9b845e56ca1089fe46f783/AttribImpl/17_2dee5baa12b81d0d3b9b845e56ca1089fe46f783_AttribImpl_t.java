 package yapl;
 
 import yapl.interfaces.Attrib;
 import yapl.interfaces.Symbol;
 import yapl.lib.Type;
 
 public class AttribImpl implements Attrib {
 
 	private boolean isConstant;
 	private boolean isGlobal;
 	private AttribKind kind = AttribKind.None;
 	private int offset;
 	private Attrib offsetAttrib;
 	private byte register;
 	private Type type;
 
 	public AttribImpl(Type type) {
 		this.type = type;
 	}
 	
 	public AttribImpl(Symbol symbol) {
 		this.type = symbol.getType();
 		this.offset = symbol.getOffset();
		this.isGlobal = symbol.isGlobal();
 	}
 	
 	@Override
 	public AttribKind getKind() {
 		return kind;
 	}
 
 	@Override
 	public int getOffset() {
 		return offset;
 	}
 
 	@Override
 	public Attrib getOffsetAttrib() {
 		return offsetAttrib;
 	}
 
 	@Override
 	public byte getRegister() {
 		return register;
 	}
 
 	@Override
 	public Type getType() {
 		return type;
 	}
 
 	@Override
 	public boolean isConstant() {
 		return isConstant;
 	}
 
 	@Override
 	public boolean isGlobal() {
 		return isGlobal;
 	}
 
 	@Override
 	public void setConstant(boolean isConstant) {
 		this.isConstant = isConstant;
 	}
 
 	@Override
 	public void setGlobal(boolean isGlobal) {
 		this.isGlobal = isGlobal;
 	}
 
 	@Override
 	public void setKind(AttribKind kind) {
 		this.kind = kind;
 	}
 
 	@Override
 	public void setOffset(int offset) {
 		this.offset = offset;
 	}
 
 	@Override
 	public void setOffsetAttrib(Attrib offset) {
 		offsetAttrib = offset;
 	}
 
 	@Override
 	public void setRegister(byte register) {
 		this.register = register;
 	}
 
 	@Override
 	public void setType(Type type) {
 		this.type = type;
 	}
 
 }
