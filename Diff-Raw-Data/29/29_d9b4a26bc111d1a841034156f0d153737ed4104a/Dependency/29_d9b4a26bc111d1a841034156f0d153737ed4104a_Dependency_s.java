 package au.edu.labshare.schedserver.scormpackager.manifest;
 
 public class Dependency 
 {
 	public static final int STRING_MAX_LENGTH = 2000;
 	
 	String identifierref;
 	
 	public String getIdentiferRef()
 	{
 		return identifierref;
 	}
 	
 	public void setIdentifierRef(String identifierref)
 	{
 		if(identifierref.length() <= STRING_MAX_LENGTH)
 			this.identifierref = identifierref;
 	}
 }
