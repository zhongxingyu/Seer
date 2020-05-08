 package ecologylab.serialization;
 
import java.util.ArrayList;
 import java.util.List;
 
 import ecologylab.generic.Debug;
 import ecologylab.net.ParsedURL;
 
 /**
  * There are certain rules one has to follow while using this framework for
  * translation from Java to XML and back. This exception class enforces those
  * rules on the user. So, if the user fails to follow any of the rule, this
  * exception is thrown with appropriate error message encapsulated in it. This
  * exception is thrown in the following conditions:
  * <ul>
  * <li> If the user is translating a class which has some Collection type object
  * in it (e.g Vector, Hashtable)and the class does not overrides the methods
  * <code>getCollection()</code> and <code>addElement()</code> inherited from
  * <code>ElementState.</code> </li>
  * <li> If the user is translating a class which has some Collection type object
  * in it and the collection does not contain items of objects derived from
  * <code>ElementState</code> type. </li>
  * <li> The classes to be translated does not provide a constructor with zero
  * arguments. In this case the exception is thrown only when the user is
  * building Java classes from xml. </li>
  * <li> The class to be translated does not provide setter method for setting
  * the values of the primitive type variables which are translated. In this case
  * the exception is thrown only when the user is building Java classes from xml.
  * </li>
  * </ul>
  * 
  * @author Andruid Kerne
  * @author Madhur Khandelwal
  * @version 0.5
  */
 
 public class SIMPLTranslationException extends Exception implements
         XMLTranslationExceptionTypes
 {
     private static final long serialVersionUID = -8326348358064487418L;
 
     private int               exceptionType    = 0;
     
     private List<SimplIssue> simplIssues;
 
     private Object remnantObject = null;
 
 	private List<Exception> childExceptions;
     
     public SIMPLTranslationException()
     {
         super();
         simplIssues = new ArrayList<SimplIssue>();
         childExceptions = new ArrayList<Exception>();
     }
     
     
     public SIMPLTranslationException(SimplIssue si)
     {
     	this(si.errorExplanation);
     	this.simplIssues = new ArrayList<SimplIssue>();
         childExceptions = new ArrayList<Exception>();
     	simplIssues.add(si);
     }
     
     public SIMPLTranslationException(SimplIssue si, Object remnant)
     {
     	this(si);
     	this.remnantObject = remnant;
     }
 
     public SIMPLTranslationException(String msg)
     {
         super(msg);
 
     	this.simplIssues = new ArrayList<SimplIssue>();
         childExceptions = new ArrayList<Exception>();
     }
 
     public SIMPLTranslationException(String msg, Exception e)
     {
         super("XmlTranslationException\n" + msg + "\n\tThe error is "
                 + e.toString() + " in" + "\n\t" + e.getStackTrace()[0] + "\n\t"
                 + e.getStackTrace()[1] + "\n\t" + e.getStackTrace()[2] + "\n\t"
                 + e.getStackTrace()[3] + "\n\t");
     }
 
     public SIMPLTranslationException(int exceptionType)
     {
         super();
 
         this.exceptionType = exceptionType;
     }
 
     public SIMPLTranslationException(String msg, int exceptionType)
     {
         this(msg);
 
         this.exceptionType = exceptionType;
     }
 
     public SIMPLTranslationException(String msg, Exception e, int exceptionType)
     {
         this(msg, e);
 
         this.exceptionType = exceptionType;
     }
 
     /**
      * Returns the type of exception that generated the XmlTranslationException.
      * These can be referenced from the interface XmlTranslationExceptionTypes.
      * 
      * @return
      */
     public int getExceptionType()
     {
         return exceptionType;
     }
     
     public void printTraceOrMessage(Debug that, String msg, ParsedURL purl)
     {
     	switch (getExceptionType())
     	{
     	case FILE_NOT_FOUND:
         	String purlMsg	= (purl == null) ? "" : purl.toString();
 			that.warning("File not found - " + msg + " - "+purlMsg);
     		break;
     	case NULL_PURL:
 			Debug.weird(that, "\tCan't open " + msg + " - ParsedURL is null");
    		break;
     	default:
     		this.printStackTrace();
     		break;
     	}
     }
     
     /**
      * While carrying out a simpl translation, there may be some exception or mistake that occurs during the process.
      * Rather than returning nothing and making everyone have a bad day, this method provides some way for a consumer to 
      * salvage the remnants of whatever simpl could deserialize. There's minimal guarentees that the data will be useful,
      * but if you like, you can try to do something with these remnants.
      * @return Whatever simpl could de/serialize, the "remnants" from some exception state
      */
     public Object getObjectRemnant()
     {
     	return this.remnantObject;
     }
     	
     /**
      * Sometimes, there are mistakes or problems which create issues in simpl. 
      * A "simplIssue" is a more concise way of representing these issues. 
      * They may provide text or some representation of the issue, along with a helpful error message.
      * @return
      */
     public List<SimplIssue> getSimplIssues() 
     {
     	return this.simplIssues;
     	
     }
     
     /**
      * Appends all of the issues from another simple Execption to this exception.
      * Makes this exception suitable to pass to other exceptions!
      * Holds onto an instance of the exception for the sake of using stack traces, too! 
      * @param anotherException
      */
     public void AppendIssuesFrom(SIMPLTranslationException anotherException)
     {
     	this.simplIssues.addAll(anotherException.getSimplIssues());
     	this.childExceptions.add(anotherException);
     }
 
 
 	public void setObjectRemnant(Object result) {
 		this.remnantObject = result;
 	}
 }
