 /**
 Copyright (c) 2012 Delcyon, Inc.
 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.
 
 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.delcyon.capo.parsers;
 
 import java.io.FileInputStream;
 import java.util.Vector;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.w3c.dom.Document;
 
 import com.delcyon.capo.xml.XPath;
 
 /**
  * @author jeremiah
  *
  */
 public class GrammarParserTest
 {
 
     @Test
     public void test() throws Exception
     {
         GrammarParser grammarParser = new GrammarParser();
         grammarParser.loadNotationGrammer(new FileInputStream("test-data/parser_test_data/SIMPLE.notation"));       
         grammarParser.loadGrammer(new FileInputStream("test-data/parser_test_data/SIMPLE.grammer"));
        grammarParser.parse(new FileInputStream("test-data/parser_test_data/SIMPLE.input"));
     }
 
     @Test
     public void testZipcode() throws Exception
     {
         GrammarParser grammarParser = new GrammarParser();
                
         grammarParser.loadGrammer(new FileInputStream("test-data/parser_test_data/ZIPCODE.grammer"));
         FileInputStream zipCodeFileInputStream = new FileInputStream("test-data/parser_test_data/ZIPCODE.input"); 
         grammarParser.parse(zipCodeFileInputStream);
         System.out.println("==========REMAINDER============");
         while(true)
         {
             int value = zipCodeFileInputStream.read();
             if (value < 0)
             {
                 break;
             }
             else
             {           
                 System.out.print((char)value);
             }
         }
         System.out.println("\n==========END REMAINDER============");
         zipCodeFileInputStream.close();
     }
     
     @Test
     public void testDocumentUsage() throws Exception
     {
         GrammarParser grammarParser = new GrammarParser();
         //grammerParser.setNamespace("dt","gal/doctype");
         grammarParser.loadGrammer(new FileInputStream("test-data/parser_test_data/document_usage.grammar"));
         FileInputStream csvFileInputStream = new FileInputStream("test-data/parser_test_data/document_usage.csv"); 
         Document parseResultDocument = grammarParser.parse(csvFileInputStream);
         //XPath.dumpNode(parseResultDocument, System.out);
         System.out.println("==========REMAINDER============");
         while(true)
         {
             int value = csvFileInputStream.read();
             if (value < 0)
             {
                 break;
             }
             else
             {           
                 System.out.print((char)value);
             }
         }
         System.out.println("\n==========END REMAINDER============");
         csvFileInputStream.close();
     }
 }
