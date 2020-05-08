 package cat.mobilejazz.database;
 
 import android.text.TextUtils;
 import cat.mobilejazz.utilities.format.StringFormatter;
 import cat.mobilejazz.utilities.format.StringTemplate;
 import cat.mobilejazz.utilities.format.SymbolTable;
 import cat.mobilejazz.utilities.format.TreeObject;
 
import com.google.gson.JsonElement;

 public class Column implements TreeObject {
 
 	private int type;
 	private int affinity;
 	private String constraint;
 	private String name;
	private String[] path;
 	private boolean hasPath;
 	private StringTemplate delegate;
 	private int storage;
 
 	private String declaredName;
 	private DataParser<?> parser;
 	private Table parent;
 
 	private String defaultValue;
 
 	private boolean isUID;
 
 	public Column(int type, int affinity, String constraint, int storage, String name, String declaredName,
 			String delegate, String defaultValue, DataParser<?> parser, boolean isUID, Table parent) {
 		this.type = type;
 		this.affinity = affinity;
 		this.constraint = constraint;
 		this.storage = storage;
 		this.name = name;
 		this.declaredName = declaredName;
 		this.defaultValue = defaultValue;
 		this.parser = parser;
 		this.parent = parent;
 		this.delegate = new StringTemplate(delegate);
 		this.isUID = isUID;
 		
 		this.path = this.name.split("\\$");
 		this.hasPath = path.length > 1;
 	}
 
 	public boolean hasParser() {
 		return parser != null;
 	}
 
 	public Object parse(JsonElement value) {
 		if (parser != null) {
 			return parser.parse(value);
 		} else {
 			throw new IllegalStateException("This column has no parser");
 		}
 	}
 
 	public int getAffinity() {
 		return affinity;
 	}
 
 	public int getType() {
 		return type;
 	}
 
 	public String getConstraint() {
 		return constraint;
 	}
 
 	public int getStorage() {
 		return storage;
 	}
 
 	public String getName() {
 		return name;
 	}
 	
 	public String[] getPath() {
 		return path;
 	}
 	
 	public boolean hasPath() {
 		return hasPath;
 	}
 
 	public String getDeclaredName() {
 		return declaredName;
 	}
 
 	public Table getParent() {
 		return parent;
 	}
 
 	public String getFullName() {
 		return parent.getName() + "." + name;
 	}
 
 	public boolean isUID() {
 		return isUID;
 	}
 
 	/**
 	 * Returns the value for the delegate table that this column points to.
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public String getDelegate(SymbolTable<?> symbols) {
 		if (delegate != null) {
 			return delegate.render(symbols);
 		} else {
 			return null;
 		}
 	}
 
 	public String toString() {
 		StringBuilder s = new StringBuilder();
 		s.append(name);
 		s.append(' ');
 		s.append(Affinity.asString(affinity));
 		if (!TextUtils.isEmpty(defaultValue)) {
 			s.append(" DEFAULT ");
 			s.append(defaultValue);
 		}
 		if (!TextUtils.isEmpty(constraint)) {
 			s.append(' ');
 			s.append(constraint);
 		}
 		return s.toString();
 	}
 
 	@Override
 	public StringBuilder dump(StringBuilder result, int indent) {
 		StringFormatter.appendWS(result, indent * 2);
 		result.append(declaredName).append("[").append(Type.asString(type)).append("]").append(": ").append(toString());
 		return result;
 	}
 
 }
