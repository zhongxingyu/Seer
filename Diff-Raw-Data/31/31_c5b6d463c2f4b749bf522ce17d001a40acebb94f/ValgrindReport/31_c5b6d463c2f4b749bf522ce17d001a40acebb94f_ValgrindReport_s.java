 package com.facinghell.valgrind.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.kohsuke.stapler.export.Exported;
 
 public class ValgrindReport implements Serializable
 {
 	private static final long serialVersionUID = -9036045639715893780L;
 	
 	private List<ValgrindError> errors;	
 	
 	public void integrate( ValgrindReport valgrindReport )
 	{
 		if ( valgrindReport.errors != null )
 		{
 			for( ValgrindError error : valgrindReport.errors )
 			{
 				addError( error );
 			}
 		}		
 	}
 	
 	public ValgrindError findErrorById( String id )
 	{
 		for ( ValgrindError error : getAllErrors() )
 			if ( error.getUniqueId().equals( id ) )
 				return error;
 		
 		return null;
 	}
 	
 	public List<ValgrindError> getAllErrors()
 	{
 		return errors;
 	}
 	
 	@Exported
 	public int getErrorCount()
 	{
 		if ( errors == null )
 			return 0;
 		
 		return errors.size();
 	}
 	
 	@Exported
 	public int getInvalidReadErrorCount()
	{
		return getInvalidReadErrors().size();
 	}
 	
 	@Exported
 	public int getInvalidWriteErrorCount()
 	{
		return getInvalidWriteErrors().size();
 	}
 	
 	@Exported
 	public int getLeakDefinitelyLostErrorCount()
 	{
		return getLeakDefinitelyLostErrors().size();
 	}
 	
 	@Exported
 	public int getLeakPossiblyLostErrorCount()
 	{
		return getLeakPossiblyLostErrors().size();
 	}	
 	
 	public void addError( ValgrindError error )
 	{
 		if ( errors == null )
 			errors = new ArrayList<ValgrindError>();
 		
 		errors.add( error );
 	}	
 
 	public List<ValgrindError> getInvalidReadErrors()
 	{
 		return getErrorsByKind(ValgrindErrorKind.InvalidRead);
 	}
 
 	public List<ValgrindError> getInvalidWriteErrors()
 	{
 		return getErrorsByKind(ValgrindErrorKind.InvalidWrite);
 	}
 
 	public List<ValgrindError> getLeakDefinitelyLostErrors()
 	{
 		return getErrorsByKind(ValgrindErrorKind.Leak_DefinitelyLost);
 	}
 
 	public List<ValgrindError> getLeakPossiblyLostErrors()
 	{
 		return getErrorsByKind(ValgrindErrorKind.Leak_PossiblyLost);
 	}
 	
 	public List<ValgrindError> getErrorsByKind( ValgrindErrorKind valgrindErrorKind )
 	{		
 		List<ValgrindError> result = new ArrayList<ValgrindError>();
 		
 		for ( ValgrindError error: errors )
 			if ( error.getKind().equals( valgrindErrorKind ) )
 				result.add( error );
 		
 		return result;		
 	}
 }
