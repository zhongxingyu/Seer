 package uk.org.sappho.code.change.management.data.validation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import net.sf.oval.Validator;
 import net.sf.oval.configuration.annotation.AbstractAnnotationCheck;
 import net.sf.oval.context.FieldContext;
 import net.sf.oval.context.OValContext;
 import net.sf.oval.exception.OValException;
 
 import uk.org.sappho.code.change.management.data.IssueData;
 import uk.org.sappho.code.change.management.data.RawData;
 import uk.org.sappho.code.change.management.data.RevisionData;
 
 public class IssueKeyMappingCheck extends AbstractAnnotationCheck<IssueKeyMappingConstraint> {
 
     private static final long serialVersionUID = 2875771845270320136L;
 
     public boolean isSatisfied(Object validatedObject, Object valueToValidate, OValContext context, Validator validator)
             throws OValException {
 
         RawData rawData = null;
         Map<String, String> map = null;
         Collection<String> keys = null;
         Collection<String> values = null;
         try {
             rawData = (RawData) validatedObject;
             if (rawData != null) {
                 map = (Map<String, String>) valueToValidate;
                 keys = map.keySet();
                 values = map.values();
             }
         } catch (Throwable t) {
             // catches cast exceptions leaving keys as null
         }
         String error = "";
         if (values == null) {
             error = "is missing, invalid or does not cast to Map<String, String>, or RawData is not being validated or is also invalid";
         } else {
             List<String> unmappedToRevision = new ArrayList<String>();
             for (String key : keys) {
                 List<RevisionData> revisionsReferencingIssue = new ArrayList<RevisionData>();
                 try {
                     revisionsReferencingIssue = rawData.getRevisionsReferencingIssue(key);
                 } catch (Throwable t) {
                     // if there's an error getting the revision list assume there aren't any revisions
                 }
                 if (revisionsReferencingIssue.size() == 0)
                     unmappedToRevision.add(key);
             }
             List<String> unmappedToIssue = new ArrayList<String>();
             for (String value : values) {
                 IssueData issueData = null;
                 try {
                     issueData = rawData.getIssueData(value);
                 } catch (Throwable t) {
                     // if there's an error getting the issue assume there isn't an issue
                 }
                 if (issueData == null)
                     unmappedToIssue.add(value);
             }
             if (unmappedToRevision.size() != 0)
                 error += "unreferenced by any revision: " + unmappedToRevision;
             if (unmappedToIssue.size() != 0) {
                 if (unmappedToRevision.size() != 0)
                     error += " ";
                 error += "references to missing issues: " + unmappedToIssue;
             }
         }
         if (error.length() != 0) {
             if (context instanceof FieldContext) {
                 FieldContext fieldContext = (FieldContext) context;
                 error = fieldContext.getField().getDeclaringClass().getName() + "." + fieldContext.getField().getName()
                         + " " + error;
             }
             setMessage(error);
         }
         return error.length() == 0;
     }
 }
