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
 package com.celements.course.service;
 
 import static org.easymock.EasyMock.*;
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.common.test.AbstractBridgedComponentTestCase;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 public class CourseServiceTest extends AbstractBridgedComponentTestCase {
 
   private CourseService courseService;
   private String db;
   private DocumentReference docRef;
 
   @Before
   public void setUp_CourseScriptServiceTest() throws Exception {
     courseService = (CourseService) getComponentManager().lookup(ICourseServiceRole.class);
     db = "db";
     docRef = new DocumentReference(db, "CourseSpace", "CourseX");
   }
 
   @Test
  public void testCetCourseTypeForCourse() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
     DocumentReference typeDocRef = new DocumentReference(db, "TypeSpace", "TypeX");
     
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     BaseObject obj = new BaseObject();
     obj.setXClassReference(courseService.getCourseClasses().getCourseClassRef(db));
     obj.setStringValue("type", courseService.webUtilsService.serializeRef(typeDocRef));
     doc.addXObject(obj);
     
     replayDefault();
     DocumentReference ret = courseService.getCourseTypeForCourse(docRef);
     verifyDefault();
     assertEquals(typeDocRef, ret);
   }
 
   @Test
  public void testCetCourseTypeForCourse_noType() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     BaseObject obj = new BaseObject();
     obj.setXClassReference(courseService.getCourseClasses().getCourseClassRef(db));
     doc.addXObject(obj);
     
     replayDefault();
     DocumentReference ret = courseService.getCourseTypeForCourse(docRef);
     verifyDefault();
     assertNull(ret);
   }
 
   @Test
  public void testCetCourseTypeForCourse_noObj() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     
     replayDefault();
     DocumentReference ret = courseService.getCourseTypeForCourse(docRef);
     verifyDefault();
     assertNull(ret);
   }
 
   @Test
  public void testCetCourseTypeForCourse_XWE() throws Exception {
     Throwable cause = new XWikiException();
     
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andThrow(cause
         ).once();
     
     replayDefault();
     try {
       courseService.getCourseTypeForCourse(docRef);
       fail("expecting XWE");
     } catch (XWikiException exc) {
       assertSame(cause, exc);
     }
     verifyDefault();
   }
 
   @Test
   public void testGetCourseTypeName() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
     String name = "asdf";
     
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     BaseObject typeObj = new BaseObject();
     typeObj.setXClassReference(courseService.getCourseClasses().getCourseTypeClassRef(db));
     typeObj.setStringValue("typeName", name);
     doc.addXObject(typeObj);
     
     replayDefault();
     String ret = courseService.getCourseTypeName(docRef);
     verifyDefault();
     assertEquals(name, ret);
   }
 
   @Test
   public void testGetCourseTypeName_noName() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     BaseObject typeObj = new BaseObject();
     typeObj.setXClassReference(courseService.getCourseClasses().getCourseTypeClassRef(db));
     doc.addXObject(typeObj);
     
     replayDefault();
     String ret = courseService.getCourseTypeName(docRef);
     verifyDefault();
     assertEquals("", ret);
   }
 
   @Test
   public void testGetCourseTypeName_noObj() throws Exception {
    XWikiDocument doc = new XWikiDocument(docRef);
    
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andReturn(doc
         ).once();
     
     replayDefault();
     String ret = courseService.getCourseTypeName(docRef);
     verifyDefault();
     assertEquals("", ret);
   }
 
   @Test
   public void testGetCourseTypeName_XWE() throws Exception {
     Throwable cause = new XWikiException();
     
     expect(getWikiMock().getDocument(eq(docRef), same(getContext()))).andThrow(cause
         ).once();
     
     replayDefault();
     try {
       courseService.getCourseTypeName(docRef);
       fail("expecting XWE");
     } catch (XWikiException exc) {
       assertSame(cause, exc);
     }
     verifyDefault();
   }
 
 }
