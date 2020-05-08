 package dbmigrate.model.db;
 
 public class Column implements IColumn {
 	private String name;
 	private Boolean nullable = null;
 	private TypeEnum type;
 	private Boolean signed = null;
 	private int length = -1;
 	
 	public String getSqlDescription() {
 		String desc = "";
 		StringBuffer buf = new StringBuffer();
 		
 		buf.append(getName()).append(' ');
 		if (getLength() > -1) {
 			buf.append('(').append(getLength()).append(") ");
 		}
 		
		buf.append(getType().toString()).append(' ');
		
 		if (getSigned() != null)  {
 			if (getSigned()) {
 				buf.append("SIGNED ");
 			}
 			else {
 				buf.append("UNSIGNED ");
 			}
 		}
 		
 		if (getNullable() != null) {
 			if (getNullable()) {
 				buf.append("NULL ");
 			}
 			else {
 				buf.append("NOT NULL ");
 			}
 		}
 				
 		desc = buf.toString();
 		return desc;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public TypeEnum getType() {
 		return type;
 	}
 	public void setType(TypeEnum type) {
 		this.type = type;
 	}
 	public Boolean getNullable() {
 		return nullable;
 	}
 
 	public void setNullable(Boolean nullable) {
 		this.nullable = nullable;
 	}
 
 	public Boolean getSigned() {
 		return signed;
 	}
 
 	public void setSigned(Boolean signed) {
 		this.signed = signed;
 	}
 
 	public int getLength() {
 		return length;
 	}
 	public void setLength(int length) {
 		this.length = length;
 	}
 	
 	
 	
 }
