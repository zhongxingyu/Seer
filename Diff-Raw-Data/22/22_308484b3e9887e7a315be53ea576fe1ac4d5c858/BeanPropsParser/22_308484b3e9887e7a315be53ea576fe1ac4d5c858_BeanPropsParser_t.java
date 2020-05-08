 /*---
    Copyright 2006-2007 Visual Systems Corporation.
    http://www.vscorp.com
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
         http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 ---*/
 
 package wicket.contrib.webbeans.model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.StreamTokenizer;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A recursive descent parser to parse a ".beanprops" stream. <p>
  * 
  * @author Dan Syrstad 
  */
 public class BeanPropsParser
 {
     private String streamName;
     private InputStream stream;
     private StreamTokenizer tokenizer;
 
     /**
      * Construct a BeanPropsParser. 
      *
      * @param stream
      * @param beanMetaData
      * @param context
      */
     public BeanPropsParser(String streamName, InputStream stream)
     {
         this.streamName = streamName;
         this.stream = stream;
     }
     
     /**
      * Generate a error message via an exception.
      */
     private RuntimeException generateError(String msg) 
     {
         return new RuntimeException("Error: " + streamName + " at line " + tokenizer.lineno() + ": " + msg);
     }
     
     /**
      * @return the current token from the tokenizer. Note that this returns an empty string rather than null.
      */
     private String getToken()
     {
         String value = tokenizer.sval;
         if (value == null && tokenizer.ttype >= 0) {
             value = String.valueOf((char) tokenizer.ttype);
         }
         
         return value == null ? "" : value;
     }
     
     /**
      * Gets the next token from the tokenizer. Note that this returns an empty string rather than null.
      * This is a combination of tokenizer.nextToken() and getToken(). 
      *
      * @return the next token.
      */
     private String getNextToken()
     {
         try {
             if (tokenizer.nextToken() == StreamTokenizer.TT_EOF) {
                 throw new RuntimeException("Unexpected EOF reading from " + streamName);
             }
             
             return getToken();
         }
         catch (IOException e) {
             throw new RuntimeException("Error reading from " + streamName, e);
         }
         
     }
     
     /**
      * Generate an "expected" error message via an exception.
      */
     private RuntimeException generateExpectedError(String expectedThing) 
     {
         return generateError("Expected '" + expectedThing + "', but got '" + getToken() + "'");
     }
     
     /**
      * Reads and expects a token.
      *
      * @param token
      */
     private void expect(String token)
     {
         if (!getNextToken().equals(token)) {
             throw generateExpectedError(token);
         }
     }
     
     /**
      * Parses the stream given on construction and updates the BeanMetaData.
      *
      * @return a list of Bean ASTs.
      * 
      * @throws RuntimeException if a parsing error occurs.
      */
     public List<Bean> parse()
     {
         tokenizer = new StreamTokenizer( new BufferedReader( new InputStreamReader(stream) ) );
         tokenizer.resetSyntax();
         tokenizer.wordChars('a', 'z');
         tokenizer.wordChars('A', 'Z');
         tokenizer.wordChars('0', '9');
         tokenizer.wordChars('_', '_');
         tokenizer.wordChars('-', '-');
         tokenizer.wordChars('.', '.');
         tokenizer.wordChars('*', '*');
         tokenizer.wordChars('$', '$'); // For inner classes/macros
         tokenizer.wordChars(128 + 32, 255);
         tokenizer.whitespaceChars(0, ' ');
         tokenizer.commentChar('#');
         tokenizer.quoteChar('"');
         
         List<Bean> beans = new ArrayList<Bean>();
         try {
             while (tokenizer.nextToken() != StreamTokenizer.TT_EOF) {
                 if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                     beans.add( parseBean() );
                 }
                 else {
                     throw generateExpectedError("bean name");
                 }
             }
         }
         catch (IOException e) {
             throw new RuntimeException("Error reading " + streamName, e);
         }
         
         return beans;
     }
     
     private Bean parseBean()
     {
         String beanName = getToken();
 
         // Context "[context]" or "[context extends context]" specified?
         String context = null;
         String extendsContext = null;
         String nextToken = getNextToken();
         if (nextToken.equals("[")) {
             context = getNextToken();
             nextToken = getNextToken();
             if (nextToken.equals("extends")) {
                 extendsContext = getNextToken();
                 nextToken = getNextToken();
             }
             
             if (!nextToken.equals("]")) {
                 throw generateExpectedError("extends' or ']");
             }
         }
         else {
             tokenizer.pushBack();
         }
         
         expect("{");
         
        // Allow empty blocks...
        List<Parameter> params;
        if (!getNextToken().equals("}")) {
            tokenizer.pushBack();
            params = parseParameters();
            expect("}");
        }
        else {
            params = new ArrayList<Parameter>();
        }
         
         return new Bean(beanName, context, extendsContext, params);
     }
     
     private List<Parameter> parseParameters()
     {
         List<Parameter> params = new ArrayList<Parameter>();
         while (true) {
             params.add( parseParameter() );
             
             String nextToken = getNextToken();
             tokenizer.pushBack();
             
             if (nextToken.equals("}")) {
                 break;
             }
         }
         
         return params;
     }
     
     private Parameter parseParameter()
     {
         String paramName = getNextToken();
         expect(":");
         
         List<ParameterValue> values = parseParameterValues();
 
         String nextToken = getNextToken();
         if (!nextToken.equals(";")) {
             if (nextToken.equals("}")) {
                 tokenizer.pushBack();
             }
             else {
                 throw generateExpectedError(";' or '}");
             }
         }
         
         return new Parameter(paramName, values);
     }
 
     private List<ParameterValue> parseParameterValues()
     {
         List<ParameterValue> paramValues = new ArrayList<ParameterValue>();
         do {
             paramValues.add( parseParameterValue() );
         }
         while (getNextToken().equals(","));
      
         tokenizer.pushBack();
         return paramValues;
     }
     
     private ParameterValue parseParameterValue()
     {
         ParameterValue param = new ParameterValue(getNextToken());
         String nextToken = getNextToken();
         // Start of parameters for value?
         if (nextToken.equals("{")) {
            // Allow empty blocks...
            if (!getNextToken().equals("}")) {
                tokenizer.pushBack();
                param.setParameters( parseParameters() );
                expect("}");
            }
         }
         else {
             tokenizer.pushBack();
         }
         
         return param;
     }
 }
 
