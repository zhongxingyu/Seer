 package org.eclipse.dltk.ui.documentation;
 
 import java.io.Reader;
 
 import org.eclipse.dltk.core.IMember;
 
 
 /**
  * Interface for org.eclipse.dltk.ui.scriptDocumentationProviders extension point.
  * 
  */
 public interface IScriptDocumentationProvider {
 	
 	/**
 	 * Fetches info for a member like proc or variable. Also it
 	 * may look into parents, if child contains no documentaton. 
 	 * 
 	 * @param element Element to get info for.
 	 * @param lookIntoParents Allows to look into parents for a doc.
 	 * @param lookIntoExternal Allows to look into external resources.
 	 * @return Reader object, providing doc.
 	 */
 	public Reader getInfo (IMember element, boolean lookIntoParents, boolean lookIntoExternal);
 		
 	/**
 	 * Fetches some info about provided keyword.
 	 * 
 	 * @param content Keyword to get info for.
 	 * @return  Reader object, providing doc.
 	 */
 	public Reader getInfo (String content);
 	
 }
