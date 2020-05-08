 /*
  * See the NOTICE file distributed with this work for additional
  * information regarding copyright ownership.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.celements.course.classcollections;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.classes.AbstractClassCollection;
 import com.xpn.xwiki.XWiki;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.classes.BaseClass;
 
 @Component("CelCourseClasses")
 public class CourseClasses extends AbstractClassCollection {
 
   public static final String COURSE_PARTICIPANT_CLASS_DOC = "CourseParticipantClass";
 
   public static final String COURSE_CLASS_DOC = "CourseClass";
 
   public static final String COURSE_TYPE_CLASS_DOC = "CourseTypeClass";
 
   public static final String COURSE_CLASSES_SPACE = "CourseClasses";
 
   private static Log LOGGER = LogFactory.getFactory().getInstance(CourseClasses.class);
 
   @Override
   protected Log getLogger() {
     return LOGGER;
   }
 
   public String getConfigName() {
     return "celCourse";
   }
   
   @Override
   protected void initClasses() throws XWikiException {
     getCourseTypeClass();
     getCourseClass();
     getCourseParticipantClass();
   }
 
   public DocumentReference getCourseTypeClassRef(String wikiName) {
     return new DocumentReference(wikiName, COURSE_CLASSES_SPACE, COURSE_TYPE_CLASS_DOC);
   }
   
   BaseClass getCourseTypeClass() throws XWikiException {
     DocumentReference classRef = getCourseTypeClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (Exception e) {
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
     needsUpdate |= bclass.addTextField("typeName", "Course Type Name", 30);
     needsUpdate |= bclass.addTextField("shortName", "Course Type Short Name", 30);
     needsUpdate |= bclass.addTextField("prefix", "Prefix", 30);
     needsUpdate |= bclass.addTextField("type_img_path", "Type Image Path", 30);
     needsUpdate |= bclass.addTextAreaField("details", "Details", 80, 15);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCourseClassRef(String wikiName) {
     return new DocumentReference(wikiName, COURSE_CLASSES_SPACE,
         COURSE_CLASS_DOC);
   }
   
   BaseClass getCourseClass() throws XWikiException {
     DocumentReference classRef = getCourseClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (Exception e) {
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
    needsUpdate |= bclass.addDBListField("type", "Type", 3, false, "select distinct"
         + " doc.fullName,ct.typeName from XWikiDocument as doc, BaseObject as obj,"
         + " " + COURSE_CLASSES_SPACE + "." + COURSE_TYPE_CLASS_DOC + " as ct"
         + " where doc.translation=0 and doc.space='CourseType' and "
         + " doc.fullName=obj.name and obj.id=ct.id and obj.className='"
         + COURSE_CLASSES_SPACE + "." + COURSE_TYPE_CLASS_DOC + "'"
         + " order by ct.typeName");
     needsUpdate |= bclass.addTextField("number", "Number", 30);
     needsUpdate |= bclass.addNumberField("seats", "Seats", 10, "integer");
     needsUpdate |= bclass.addTextAreaField("info", "Info", 80, 15);
     //Teacher is not mapped since a DBList can not be mapped to a String. If a mapping is
     //    needed later, this field could be changed to a String, or maybe XWiki could be 
     //    changed to be able to save it alternatively as a string
     needsUpdate |= bclass.addTextField("price", "Price", 30);
     needsUpdate |= bclass.addDBListField("teacher", "Teacher", 3, true, "select " +
         "distinct doc.fullName, doc.title from XWikiDocument as doc where doc.space=" +
         "'Teachers' and doc.name <> 'WebPreferences'");
     needsUpdate |= bclass.addDateField("startTimeStamp", "Start Timestamp", "dd.MM.yyyy",
         0);
     needsUpdate |= bclass.addDateField("endTimeStamp", "End Timestamp", "dd.MM.yyyy", 0);
 
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
   public DocumentReference getCourseParticipantClassRef(String wikiName) {
     return new DocumentReference(wikiName, COURSE_CLASSES_SPACE,
         COURSE_PARTICIPANT_CLASS_DOC);
   }
 
   BaseClass getCourseParticipantClass() throws XWikiException {
     DocumentReference classRef = getCourseParticipantClassRef(getContext().getDatabase());
     XWikiDocument doc;
     XWiki xwiki = getContext().getWiki();
     boolean needsUpdate = false;
     
     try {
       doc = xwiki.getDocument(classRef, getContext());
     } catch (Exception e) {
       doc = new XWikiDocument(classRef);
       needsUpdate = true;
     }
     
     BaseClass bclass = doc.getXClass();
     bclass.setDocumentReference(classRef);
 
     needsUpdate |= bclass.addTextField("title", "Title", 30);
     needsUpdate |= bclass.addTextField("firstname", "Firstname", 30);
     needsUpdate |= bclass.addTextField("lastname", "Lastname", 30);
     needsUpdate |= bclass.addTextField("address", "Address", 30);
     needsUpdate |= bclass.addTextField("zip", "ZIP", 30);
     needsUpdate |= bclass.addTextField("city", "City", 30);
     needsUpdate |= bclass.addTextField("phone", "Phone", 30);
     needsUpdate |= bclass.addTextField("email", "Email", 30);
     needsUpdate |= bclass.addDateField("dob", "Day of Birth", null, 0);
     needsUpdate |= bclass.addStaticListField("status", "Status", 1, false, 
         "unconfirmed|confirmed|cancelled", "select", ",|");
     needsUpdate |= bclass.addPasswordField("validkey", "Validation Key", 10);
     
     if(!"internal".equals(bclass.getCustomMapping())){
       needsUpdate = true;
       bclass.setCustomMapping("internal");
     }
     
     setContentAndSaveClassDocument(doc, needsUpdate);
     return bclass;
   }
 
 }
