 package gov.nih.nci.caxchange.ctom.viewer.util;
 
 import gov.nih.nci.caxchange.ctom.viewer.viewobjects.ParticipantSearchResult;
 
 import org.displaytag.decorator.TableDecorator;
 
 /**
  * @author Anupama Sharma
  */
 public class ParticipantSearchDecorator extends TableDecorator
 {
 
 	private static final String NBSP = "&nbsp;";
 
 	public ParticipantSearchDecorator()
 	{
 
 		super();
 	}
 
	public final String getFirstName()
 	{
 
 		ParticipantSearchResult partSearchResult = (ParticipantSearchResult) getCurrentRowObject();
 		String fName = NBSP;
 		if (partSearchResult.getFirstName() != null && !partSearchResult.getFirstName().equals("")
 				&& !partSearchResult.getFirstName().equals("null"))
 		{
 			fName = partSearchResult.getFirstName();
 		}
 		return fName;
 	}
 
 	public final String getLastName()
 	{
 
 		ParticipantSearchResult partSearchResult = (ParticipantSearchResult) getCurrentRowObject();
 		String lName = NBSP;
 		if (partSearchResult.getLastName() != null && !partSearchResult.getLastName().equals("")
 				&& !partSearchResult.getLastName().equals("null"))
 		{
 			lName = partSearchResult.getLastName();
 		}
 		return lName;
 	}
 
 	public final String getStudyId()
 	{
 
 		ParticipantSearchResult partSearchResult = (ParticipantSearchResult) getCurrentRowObject();
 		String studyId = NBSP;
 		if (partSearchResult.getStudyId() != null && !partSearchResult.getStudyId().equals("")
 				&& !partSearchResult.getStudyId().equals("null"))
 		{
 			studyId = partSearchResult.getStudyId();
 		}
 		return studyId;
 	}
 
 }
