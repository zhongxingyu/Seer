 /*
  * Copyright (C) 2003-2010 eXo Platform SAS.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, see<http://www.gnu.org/licenses/>.
  */
 package org.exoplatform.services.document.tika;
 
 import org.exoplatform.services.document.DCMetaData;
 import org.exoplatform.services.document.DocumentReader;
 import org.exoplatform.services.document.DocumentReaderService;
 import org.exoplatform.services.document.test.BaseStandaloneTest;
 import org.exoplatform.services.document.test.TestPropertiesExtracting;
 
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Properties;
 
 /**
  * Created by The eXo Platform SAS.
  * 
  * <br/>Date: 
  *
  * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
  * @version $Id: TestPropertiesExtractionOnTika.java 111 2008-11-11 11:11:11Z serg $
  */
 public class TestPropertiesExtractionOnTika extends BaseStandaloneTest
 {
    DocumentReaderService service;
 
    @Override
    public void setUp() throws Exception
    {
       super.setUp();
       service = (DocumentReaderService)getComponentInstanceOfType(DocumentReaderService.class);
 
    }
 
    // TODO fix this test
    //   public void testPDFDocumentReaderServiceXMPMetadata() throws Exception
    //   {
    //      InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/MyTest.pdf");
    //      try
    //      {
    //         DocumentReader rdr = service.getDocumentReader("application/pdf");
    //         Properties testprops = rdr.getProperties(is);
    //         Properties etalon = new Properties();
    //         etalon.put(DCMetaData.TITLE, "Test de convertion de fichier tif");
    //         etalon.put(DCMetaData.CREATOR, "Christian Klaus");
    //         etalon.put(DCMetaData.SUBJECT, "20080901 TEST Christian Etat OK");
    //         Calendar c = ISO8601.parseEx("2008-09-01T08:01:10+00:00");
    //         etalon.put(DCMetaData.DATE, c);
    //         evalProps(etalon, testprops);
    //      }
    //      finally
    //      {
    //         is.close();
    //      }
    //   }
 
    public void testPDFDocumentReaderServiceXMPMetadataTikasFile() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/tikaTestPDF.pdf");
       try
       {
          DocumentReader rdr = service.getDocumentReader("application/pdf");
          Properties testprops = rdr.getProperties(is);
          Properties etalon = new Properties();
          etalon.put(DCMetaData.TITLE, "Document title");
          etalon.put(DCMetaData.CREATOR, "Document author");
          evalProps(etalon, testprops);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testWordDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.doc");
       try
       {
          Properties props = service.getDocumentReader("application/msword").getProperties(is);
          Properties etalon = new Properties();
          etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, "Tue Aug 31 12:31:00 EEST 2010");
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
          etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testPPTDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.ppt");
       try
       {
          Properties props = service.getDocumentReader("application/powerpoint").getProperties(is);
          Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(41);
         date.set(2010, 7, 31, 12, 34, 15);
          etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime().toString());
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
          etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testExcelDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.xls");
       try
       {
          Properties props = service.getDocumentReader("application/excel").getProperties(is);
          Properties etalon = new Properties();
         Calendar date = Calendar.getInstance();
         date.setTimeInMillis(0);
         date.set(2010, 7, 31, 12, 34, 53);

          etalon.put(DCMetaData.TITLE, "test-Title");
         etalon.put(DCMetaData.DATE, date.getTime().toString());
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "KHANH NGUYEN GIA");
          etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testXWordDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.docx");
       try
       {
          Properties props =
             service.getDocumentReader("application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                .getProperties(is);
          Properties etalon = new Properties();
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(0);
          date.set(2010, 7, 31, 7, 53, 0);
 
          etalon.put(DCMetaData.TITLE, "test-Title");
          // TODO dates
          //etalon.put(DCMetaData.DATE, date.getTime().toString());
          etalon.put(DCMetaData.SUBJECT, "Subject");
          etalon.put(DCMetaData.CREATOR, "nikolaz");
          //etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testXPPTDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.pptx");
       try
       {
          Properties props =
             service.getDocumentReader("application/vnd.openxmlformats-officedocument.presentationml.presentation")
                .getProperties(is);
          Properties etalon = new Properties();
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(0);
          date.set(2010, 7, 31, 7, 59, 37);
 
          etalon.put(DCMetaData.TITLE, "test-Title");
          //TODO dates
          //etalon.put(DCMetaData.DATE, date.getTime().toString());
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "Max Yakimenko");
          etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testXExcelDocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.xlsx");
       try
       {
          Properties props =
             service.getDocumentReader("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                .getProperties(is);
          Properties etalon = new Properties();
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(0);
          date.set(2010, 7, 31, 8, 7, 25);
 
          etalon.put(DCMetaData.TITLE, "test-Title");
          // TODO dates
          //etalon.put(DCMetaData.DATE, date.getTime().toString());
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "KHANH NGUYEN GIA");
          //etalon.put(DCMetaData.CONTRIBUTOR, "Max Yakimenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    public void testOODocumentReaderService() throws Exception
    {
       InputStream is = TestPropertiesExtracting.class.getResourceAsStream("/test.odt");
       try
       {
          Properties props = service.getDocumentReader("application/vnd.oasis.opendocument.text").getProperties(is);
          printProps(props);
          Properties etalon = new Properties();
          Calendar date = Calendar.getInstance();
          date.setTimeInMillis(0);
          date.set(2010, 7, 31, 14, 13, 23);
 
          etalon.put(DCMetaData.TITLE, "test-Title");
          etalon.put(DCMetaData.LANGUAGE, "ru-RU");
          etalon.put(DCMetaData.DATE, "2010-09-03T14:37:59.10");
          etalon.put(DCMetaData.SUBJECT, "test-Subject");
          etalon.put(DCMetaData.CREATOR, "Sergiy Karpenko");
          etalon.put(DCMetaData.DESCRIPTION, "test-Comments");
 
          evalProps(etalon, props);
       }
       finally
       {
          is.close();
       }
    }
 
    private void printProps(Properties props)
    {
       Iterator it = props.entrySet().iterator();
       props.toString();
       while (it.hasNext())
       {
          Map.Entry entry = (Map.Entry)it.next();
          System.out.println(" " + entry.getKey() + " -> [" + entry.getValue() + "]");
       }
    }
 
    private void evalProps(Properties etalon, Properties testedProps)
    {
       Iterator it = etalon.entrySet().iterator();
       while (it.hasNext())
       {
          Map.Entry prop = (Map.Entry)it.next();
          Object tval = testedProps.get(prop.getKey());
          assertNotNull(prop.getKey() + " property not founded. ", tval);
          assertEquals(prop.getKey() + " property value is incorrect", prop.getValue(), tval);
       }
       //TODO When all troubles with metadata will be fixed - properties count must be checked too.
       //assertEquals("size is incorrect", etalon.size(), testedProps.size());
    }
 
 }
