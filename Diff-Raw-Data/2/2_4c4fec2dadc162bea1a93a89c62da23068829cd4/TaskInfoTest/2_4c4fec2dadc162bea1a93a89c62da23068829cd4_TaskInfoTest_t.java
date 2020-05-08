 /*
   This file is part of opensearch.
   Copyright © 2009, Dansk Bibliotekscenter a/s, 
   Tempovej 7-11, DK-2750 Ballerup, Denmark. CVR: 15149043
 
   opensearch is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   opensearch is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with opensearch.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package dk.dbc.opensearch.types;
 
 
 import dk.dbc.commons.xml.XMLUtils;
 import dk.dbc.opensearch.types.IIdentifier;
 
 /** \brief UnitTest for TaskInfo **/
 
 import mockit.Mocked;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.w3c.dom.Document;
 
 import org.custommonkey.xmlunit.XMLUnit;
 import org.custommonkey.xmlunit.Diff;
 
 public class TaskInfoTest {
 
     static final String referenceDataComplete = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"123456\" format=\"someFormat\" language=\"se\" mimetype=\"pdf\"/></referencedata>";
     static final String referenceDataNoReferenceElement = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><empty/>";
     static final String referenceDataNoInfoElement = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata></referencedata>";
     static final String referenceDataNoLang = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" mimetype=\"pdf\"/></referencedata>";
     static final String referenceDataEmptyLang = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" language=\"\" mimetype=\"pdf\"/></referencedata>";
     static final String referenceDataNoMimeType = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" language=\"se\"/></referencedata>";
     static final String referenceDataEmptyMimeType = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" language=\"se\" mimetype=\"\"/></referencedata>";
     static final String referenceDataIllegalAttribute = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><referencedata><info submitter=\"775100\" format=\"ebrary\" language=\"dk\" illegalattribute=\"illegal\"/></referencedata>";
    IIdentifier mockIdentifier = new IIdentifier (){};
     private Document xmldataComplete;
     private Document xmldataNoInfoElement;
     private Document xmldataNoReferenceElement;
     private Document xmldataNoLang;
     private Document xmldataEmptyLang;
     private Document xmldataNoMimeType;
     private Document xmldataEmptyMimeType;
     private Document xmldataIllegalAttribute;
 
     @Before
     public void setUp() throws Exception
     {
         xmldataComplete = XMLUtils.documentFromString( referenceDataComplete );
         xmldataNoInfoElement = XMLUtils.documentFromString( referenceDataNoInfoElement );
         xmldataNoReferenceElement = XMLUtils.documentFromString( referenceDataNoReferenceElement );
         xmldataNoLang = XMLUtils.documentFromString( referenceDataNoLang );
         xmldataEmptyLang = XMLUtils.documentFromString( referenceDataEmptyLang );
         xmldataNoMimeType = XMLUtils.documentFromString( referenceDataNoMimeType );
         xmldataEmptyMimeType = XMLUtils.documentFromString( referenceDataEmptyMimeType );
         xmldataIllegalAttribute = XMLUtils.documentFromString( referenceDataIllegalAttribute );
 
 	// Closing output to screen from expected errors
 	System.out.close();
     }
 
 
     /**
      * Constructor tests:
      */
     @Test(expected=IllegalArgumentException.class)
     public void TaskInfoIllegalAttributeTest() throws Exception
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataIllegalAttribute );
     }
 
     @Test( expected=IllegalArgumentException.class )
     public void IllegalStateForNullIdentifierTest() throws Exception
     {
         TaskInfo job = new TaskInfo( null, xmldataComplete );
     }
 
     @Test( expected=IllegalArgumentException.class )
     public void IllegalStateForNullReferenceDataTest() throws Exception
     {
         TaskInfo job = new TaskInfo( mockIdentifier, null );
     }
 
     @Test(expected=IllegalArgumentException.class)
     public void IllegalArgumentForWrongReferenceDataTest() throws Exception
     {
         Document reference = XMLUtils.documentFromString( "<?xml version=\"1.0\"?><error/>" );
         TaskInfo job = new TaskInfo( mockIdentifier, reference );
     }
 
 
     /**
      * This test check a correct XML with all possible values sat to
      * something meaningful will also retreive all the correct values.
      */
     @Test
     public void TaskInfoAllIsGoodTest()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataComplete );
         assertEquals( mockIdentifier, job.getIdentifier() );
         assertEquals( "123456", job.getSubmitter() );
         assertEquals( "someFormat", job.getFormat() );
 	assertEquals( "se", job.getLanguage() );
 	assertEquals( "pdf", job.getMimeType() );
     }
 
     @Test( expected = IllegalArgumentException.class )
     public void TaskInfoTestNoInfoElement()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataNoInfoElement );
     }
 
     @Test( expected = IllegalArgumentException.class )
     public void TaskInfoTestNoReferenceElement()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataNoReferenceElement );
     }
 
     @Test
     public void TaskInfoTestNoLang()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataNoLang );
         assertEquals( "775100", job.getSubmitter() );
         assertEquals( "ebrary", job.getFormat() );
 	assertEquals( "da", job.getLanguage() );
 	assertEquals( "pdf", job.getMimeType() );
     }
 
     @Test
     public void TaskInfoTestEmptyLang()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataEmptyLang );
 	assertEquals( "da", job.getLanguage() );
     }
 
     @Test
     public void TaskInfoTestNoMimeType()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataNoMimeType );
 	assertEquals( "text/xml", job.getMimeType() );
     }
 
     @Test
     public void TaskInfoTestEmptyMimeType()
     {
         TaskInfo job = new TaskInfo( mockIdentifier, xmldataEmptyMimeType );
 	assertEquals( "text/xml", job.getMimeType() );
     }
 
 
 
     /**
      * Test that we dont change the XML-document as a sideeffect.
      * We want to make sure we do not corrupt the XML inside TaskInfo.
      */
     @Test
     public void TaskInfoGetDocumentTest() throws Exception
     {
 	Document xmldataTmp = XMLUtils.documentFromString( referenceDataComplete );
     	TaskInfo job = new TaskInfo( mockIdentifier, xmldataComplete );
     	Diff diff = XMLUnit.compareXML( xmldataTmp, xmldataComplete );
         assertEquals( true, diff.identical() );
     }
 
 
 }
