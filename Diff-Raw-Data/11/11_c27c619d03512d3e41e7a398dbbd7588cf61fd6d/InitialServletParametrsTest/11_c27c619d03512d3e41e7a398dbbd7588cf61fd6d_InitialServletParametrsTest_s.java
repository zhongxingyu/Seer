 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package org.lafayette.server.web.servlet;
 
import org.lafayette.server.web.servlet.InitialServletParameters;
 import javax.servlet.ServletContext;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.assertThat;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class InitialServletParametrsTest {
 
     private static final String REALM = "This is the realm.";
 
     @Test
     public void testGetRealm() {
         final ServletContext context = mock(ServletContext.class);
         when(context.getInitParameter(InitialServletParameters.PARAM_NAME_REALM)).thenReturn(REALM);
         final InitialServletParameters sut = new InitialServletParameters(context);
         assertThat(sut.getRealm(), is(REALM));
     }
 
     @Test
     public void testGetRealm_isNull() {
         final InitialServletParameters sut = new InitialServletParameters();
         assertThat(sut.getRealm(), is(""));
     }
 }
