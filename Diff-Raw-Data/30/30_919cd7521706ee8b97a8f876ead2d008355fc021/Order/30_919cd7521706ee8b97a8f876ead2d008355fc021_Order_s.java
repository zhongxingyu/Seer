 package ch.cern.atlas.apvs.domain;
 
 import java.io.Serializable;
 
 public class Order implements Message, Serializable {
 
 	private static final long serialVersionUID = 2917315876156737998L;
 	
 	private String ptuId;
 	private String name;
 	private String parameter;
 	private Number value;
 
 	public Order() {
 	}
 
 	public Order(String ptuId, String name, String parameter, Number value) {
 		this.ptuId = ptuId;
 		this.name = name;
 		this.parameter = parameter;
 		this.value = value;
 	}
 
     @Override
 	public String getPtuId() {
 		return ptuId;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getParameter() {
 		return parameter;
 	}
 
 	public Number getValue() {
 		return value;
 	}
 
 	@Override
 	public String getType() {
		return "order";
 	}
 }
