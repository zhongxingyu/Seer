 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf(at)googlemail(dot)com>
  */
 package de.weltraumschaf.commons.token;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class TokenTest {
 
     @Test
     public void testToString_literal() {
         final Token<String> t = Token.newLiteralToken("foo");
         assertThat(t.toString(), is("Token{type=LITERAL, value=foo}"));
     }
 
     @Test
     public void getValue_literal() {
         final Token<String> t = Token.newLiteralToken("foo");
         assertThat(t.getValue(), is("foo"));
     }
 
     @Test
     public void getType_literal() {
         final Token<String> t = Token.newLiteralToken("foo");
         assertThat(t.getType(), is(TokenType.LITERAL));
     }
 
     @Test
     public void testToString_string() {
         final Token<String> t = Token.newStringToken("foo");
         assertThat(t.toString(), is("Token{type=STRING, value=foo}"));
     }
 
     @Test
     public void getValue_string() {
         final Token<String> t = Token.newStringToken("foo");
         assertThat(t.getValue(), is("foo"));
     }
 
     @Test
     public void getType_string() {
         final Token<String> t = Token.newStringToken("foo");
         assertThat(t.getType(), is(TokenType.STRING));
     }
 
     @Test
     public void testToString_keyword() {
         final Token<String> t = Token.newKeywordToken("foo");
         assertThat(t.toString(), is("Token{type=KEYWORD, value=foo}"));
     }
 
     @Test
     public void getValue_keyword() {
         final Token<String> t = Token.newKeywordToken("foo");
         assertThat(t.getValue(), is("foo"));
     }
 
     @Test
     public void getType_keyword() {
         final Token<String> t = Token.newKeywordToken("foo");
         assertThat(t.getType(), is(TokenType.KEYWORD));
     }
 
     @Test
     public void testToString_integer() {
         final Token<Integer> t = Token.newIntegerToken(123);
        assertThat(t.toString(), is("Token{type=INTEGER, value=123}"));
     }
 
     @Test
     public void getValue_integer() {
         final Token<Integer> t = Token.newIntegerToken(123);
         assertThat(t.getValue(), is(123));
     }
 
     @Test
     public void getType_integer() {
         final Token<Integer> t = Token.newIntegerToken(123);
         assertThat(t.getType(), is(TokenType.INTEGER));
     }
 
     @Test
     public void testToString_float() {
         final Token<Float> t = Token.newFloatToken(3.14f);
         assertThat(t.toString(), is("Token{type=FLOAT, value=3.14}"));
     }
 
     @Test
     public void getValue_float() {
         final Token<Float> t = Token.newFloatToken(3.14f);
         assertThat(t.getValue(), is(3.14f));
     }
 
     @Test
     public void getType_float() {
         final Token<Float> t = Token.newFloatToken(3.14f);
         assertThat(t.getType(), is(TokenType.FLOAT));
     }
 
     @Test
     public void testToString_boolean() {
         final Token t = Token.newBooleanToken(true);
         assertThat(t.toString(), is("Token{type=BOOLEAN, value=true}"));
     }
 
     @Test
     public void getValue_boolean() {
         final Token<Boolean> t = Token.newBooleanToken(true);
         assertThat(t.getValue(), is(true));
     }
 
     @Test
     public void getType_boolean() {
         final Token<Boolean> t = Token.newBooleanToken(true);
         assertThat(t.getType(), is(TokenType.BOOLEAN));
     }
 
 }
