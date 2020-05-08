 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.parser;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import org.junit.Before;
 import org.junit.Test;
 import ru.histone.Histone;
 import ru.histone.HistoneBuilder;
 import ru.histone.HistoneException;
 
 import java.io.StringReader;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 public class ASTSignatureTest {
 
     private Histone histone;
 
     @Before
     public void before() throws HistoneException {
         HistoneBuilder histoneBuilder = new HistoneBuilder();
         histone = histoneBuilder.build();
     }
 
     @Test
     public void testBasicInput() throws HistoneException {
         String input = "test";
         /*
         Expected AST:
             [
                 ["HISTONE",
                 {
                     "version": "1.0.6"
                 }],
                 ["test"]
             ]
          */
 
         JsonNode outputAST = histone.parseTemplateToAST(new StringReader(input));
 
         assertTrue(outputAST.isArray());
         assertEquals(2, outputAST.size());
 
         assertTrue(outputAST.get(0).isArray());
         assertTrue(outputAST.get(1).isArray());
 
         assertEquals("HISTONE", outputAST.get(0).get(0).asText());
         assertTrue(outputAST.get(0).get(1).isObject());
         assertTrue(outputAST.get(0).get(1).has("version"));
         assertTrue(outputAST.get(0).get(1).get("version").isTextual());
         assertTrue(outputAST.get(0).get(1).get("version").asText().length() > 0);
 
         assertEquals(1, outputAST.get(1).size());
         assertTrue(outputAST.get(1).get(0).isTextual());
         assertEquals("test", outputAST.get(1).get(0).asText());
     }
 
     @Test
     public void testEmptyInput() throws HistoneException {
         String input = "";
         /*
         Expected AST:
             [
                 ["HISTONE",
                 {
                     "version": "1.0.6"
                 }],
                 []
             ]
          */
 
         JsonNode outputAST = histone.parseTemplateToAST(new StringReader(input));
 
         assertTrue(outputAST.isArray());
         assertEquals(2, outputAST.size());
 
         assertTrue(outputAST.get(0).isArray());
         assertTrue(outputAST.get(1).isArray());
 
         assertEquals("HISTONE", outputAST.get(0).get(0).asText());
         assertTrue(outputAST.get(0).get(1).isObject());
         assertTrue(outputAST.get(0).get(1).has("version"));
         assertTrue(outputAST.get(0).get(1).get("version").isTextual());
         assertTrue(outputAST.get(0).get(1).get("version").asText().length() > 0);
 
         assertEquals(0, outputAST.get(1).size());
     }
 }
