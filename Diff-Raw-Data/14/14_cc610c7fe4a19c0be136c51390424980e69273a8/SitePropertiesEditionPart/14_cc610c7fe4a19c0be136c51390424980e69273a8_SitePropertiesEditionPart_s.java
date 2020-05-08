 /**
  * Generated with Acceleo
  */
 package org.eclipse.emf.eef.nonreg.parts;
 
import org.eclipse.emf.eef.runtime.api.parts.IPropertiesEditionPart;

// Start of user code for imports
 
 
  
 
 
 // End of user code
 /**
  * 
  */
 public interface SitePropertiesEditionPart {
 
 
 
 
 	/**
 	 * @return the NamedElement referenced view
 	 */
 	public IPropertiesEditionPart getNamedElementReferencedView();
 	/**
 	 * @return the name
 	 */
 	public String getName();
 
 	/**
 	 * Defines a new name
 	 * @param newValue the new name to set
 	 */
 	public void setName(String newValue);
 
 	public void setMessageForName(String msg, int msgLevel);
 
 	public void unsetMessageForName();
 
 	/**
 	 * @return the documentation
 	 */
 	public String getDocumentation();
 
 	/**
 	 * Defines a new documentation
 	 * @param newValue the new documentation to set
 	 */
 	public void setDocumentation(String newValue);
 
 	public void setMessageForDocumentation(String msg, int msgLevel);
 
 	public void unsetMessageForDocumentation();
 
 
 
 	// Start of user code for additional methods
 	
 	// End of user code
 }
 
