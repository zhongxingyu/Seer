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
 
 package org.lafayette.server.webapp.api.template;
 
 import freemarker.template.TemplateException;
 import java.io.IOException;
 
 /**
  * Implementors can render them self to a string.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public interface Renderable {
 
     /**
      * Renders the template in a {@code Constants.DEFAULT_ENCODING} encoded string.
      *
      * @return never {@code null}
      * @throws IOException if template file can't be opened
      * @throws TemplateException if template can't be parsed
      */
    public String render() throws IOException, TemplateException;
 
 }
