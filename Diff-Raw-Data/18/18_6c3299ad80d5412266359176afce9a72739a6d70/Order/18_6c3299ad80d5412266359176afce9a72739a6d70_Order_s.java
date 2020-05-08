 package com.vikrant.org;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
 import org.apache.camel.dataformat.bindy.annotation.DataField;
 
 @XmlRootElement
 @XmlAccessorType(XmlAccessType.FIELD)
 @CsvRecord(separator = ",", skipFirstLine = true)
 public class Order implements Serializable{
 	 	@XmlAttribute
 	    @DataField(pos = 1)
 	    private String CustomerName;
 	 	
 	 	@XmlAttribute
 		@DataField(pos = 2)
 		private int OrderID;
 	    
 	    @XmlAttribute
 	    @DataField(pos = 3)
 	    private String MessageType;
 	    
 	    public Order() {
 	    }
 	    
 	    public Order(String CustomerName, int OrderID, String MessageType) {
 	        this.CustomerName = CustomerName;
 	        this.OrderID = OrderID;
 	        this.MessageType = MessageType;
 	    }
 	    
 	    /* override toString and equal */
 	    @Override
 	    public boolean equals(Object other) {
 	        if (this == other) {
 	            return true;
 	        }
 	        if (other == null) {
 	            return false;
 	        }
 	        if (this.getClass() != other.getClass()) {
 	            return false;
 	        }
 	        Order that = (Order) other;
	        return this.CustomerName.equals(that.CustomerName) && 
	        		this.MessageType.equals(that.MessageType) && this.OrderID == that.OrderID;
 	    }
 	    
 	    @Override
 	    public String toString() {
 	        return "Order[" + CustomerName + " , " + OrderID + " , " + MessageType + "]";
 	    }
 
 }
