 package com.celements.course.classcollections;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.CelementsClassCollection;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 @Component("CelCourseClasses")
 public class CourseClasses extends CelementsClassCollection {
   private static Log mLogger = LogFactory.getFactory().getInstance(CourseClasses.class);
 
   @Override
   protected Log getLogger() {
     return mLogger;
   }
 
   @Override
   protected void initClasses(XWikiContext context) throws XWikiException {
     getCourseTypeClass(context);
     getCourseClass(context);
     getCourseParticipantClass(context);
   }
 
   public String getConfigName() {
     return "course";
   }
   
   protected BaseClass getCourseTypeClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseTypeClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
     needsUpdate |= bclass.addTextField("prefix", "Prefix", 30);
     needsUpdate |= bclass.addTextAreaField("details", "Details", 80, 15);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
   
   protected BaseClass getCourseClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
     needsUpdate |= bclass.addTextField("number", "Number", 30);
    needsUpdate |= bclass.addTextField("type", "Type", 30);
     needsUpdate |= bclass.addTextAreaField("info", "Info", 80, 15);
    needsUpdate |= bclass.addTextAreaField("teacher", "Teacher", 80, 15);
     needsUpdate |= bclass.addNumberField("seats", "Seats", 10, "integer");
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
   
   protected BaseClass getCourseParticipantClass(XWikiContext context) throws XWikiException {
     DocumentReference docRef = new DocumentReference(context.getDatabase(), 
         "Classes", "CourseParticipantClass");
     XWikiDocument doc;
     XWiki xwiki = context.getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(docRef, context);
     } catch (Exception e) {
       doc = new XWikiDocument(docRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(docRef);
 
     needsUpdate |= bclass.addTextField("title", "Title", 30);
     needsUpdate |= bclass.addTextField("firstname", "Firstname", 30);
     needsUpdate |= bclass.addTextField("lastname", "Lastname", 30);
     needsUpdate |= bclass.addTextField("address", "Address", 30);
     needsUpdate |= bclass.addTextField("zip", "ZIP", 30);
     needsUpdate |= bclass.addTextField("city", "City", 30);
     needsUpdate |= bclass.addTextField("phone", "Phone", 30);
     needsUpdate |= bclass.addTextField("email", "Email", 30);
     needsUpdate |= bclass.addDateField("dob", "Day of Birth", null, 0);
     needsUpdate |= bclass.addTextField("registrationNumber", "Registration Number", 30);
     needsUpdate |= bclass.addDateField("registrationExpiry", "Registration Expiry", null,
         0);
    needsUpdate |= bclass.addTextField("status", "Status", 30);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     String content = doc.getContent();
     if ((content == null) || (content.equals(""))) {
       needsUpdate = true;
       doc.setContent(" ");
     }
     
     if (needsUpdate){
       xwiki.saveDocument(doc, context);
     }
     return bclass;
   }
 }
