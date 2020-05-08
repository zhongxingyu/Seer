 package view.decorator;
 
 import model.AbstractAddress;
 
 public class AddressDecorator extends AbstractAddress {
 	protected final AbstractAddress address;
 	
 	public AddressDecorator( final AbstractAddress address){
 		this.address = address;
		address.setDirty(true);
 	}
 	
 	public AbstractAddress getUndecoratedAddress(){
 		return address;
 	}
 	
 	@Override
 	public String toString(){
 		
 		StringBuffer result = new StringBuffer();
 		
 		if(address.isDirty())
 			result.append("> ");
 		else
 			result.append("");
 		
 		return result.append(address.toString()).toString();
 		
 	}
 	
 	
 
 }
