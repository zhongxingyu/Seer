 package main;
 
 public class Tool {
 	private boolean isCheckedOut;
 	private String name;
 	private String UPC;
 	
 	public Tool(String name, String UPC) {
 		isCheckedOut = false;
 		this.name = name;
 		this.UPC = UPC;
 	}
 	
 	public void checkoutTool() {
 		isCheckedOut = true;
 	}
 	
 	public void returnTool() {
 		isCheckedOut = false;
 	}
 
 	public boolean isCheckedOut() {
 		return isCheckedOut;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getUPC() {
 		return UPC;
 	}
 	
 	public String toString() {
 		return name;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (!(o instanceof Tool))
 			return false;
 		Tool obj = (Tool) o;
		return (this.UPC == obj.getUPC());
 	}
 
 }
