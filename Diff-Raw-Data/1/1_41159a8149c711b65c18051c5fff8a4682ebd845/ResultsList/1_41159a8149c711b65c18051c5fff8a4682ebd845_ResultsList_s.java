 package com.redshape.utils.validators.result;
 
 import java.util.HashSet;
 
 /**
  * @author nikelin
  * @date 18/04/11
  * @package com.redshape.validators.result
  */
 public class ResultsList extends HashSet<IValidationResult>
 						 implements IResultsList {
 	private static final long serialVersionUID = 3441164189894402471L;
 	
 	private String name;
 	private boolean success;
 
 	public ResultsList() {
 		this(true);
 	}
 
 	public ResultsList( boolean success ) {
 		this( null, success );
 	}
 
 	public ResultsList( String name, boolean success ) {
 		super();
 
 		this.name = name;
 	}
 
 	@Override
 	public void markValid( boolean value ) {
 		this.success = value;
 	}
 
 	@Override
 	public String getMessage() {
 		return null;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 
 	@Override
 	public boolean isValid() {
 		boolean result = this.success;
 		if ( !result ) {
 			return result;
 		}
 		
 		for ( IValidationResult item : this ) {
 			result = result && item.isValid();
 		}
 
 		return result;
 	}
 }
