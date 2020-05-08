 /*
  * Copyright (c) 2006 - 2007 Damian Carrillo.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  *  o Redistributions of source code must retain the above copyright notice, 
  *    this list of conditions and the following disclaimer.
  *  o Redistributions in binary form must reproduce the above copyright notice, 
  *    this list of conditions and the following disclaimer in the documentation 
  *    and/or other materials provided with the distribution.
  *  o Neither the name of the <ORGANIZATION> nor the names of its contributors 
  *    may be used to endorse or promote products derived from this software 
  *    without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER 
  * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package agave;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import org.antlr.stringtemplate.StringTemplate;
 import org.antlr.stringtemplate.StringTemplateGroup;
 import agave.HandlerContext;
 import agave.HandlerException;
 import agave.MissingAnnotationException;
 import agave.ResourceHandler;
 import agave.XHTMLTemplateGroup;
 import agave.annotations.Template;
 
 /**
  *
  * @author <a href="mailto:damiancarrillo@gmail.com">Damian Carrillo</a>
  * @since 1.0.2
  */
 public abstract class StringTemplateHandler implements ResourceHandler {
 
     /**
      * Render the template that is located with the annotation.  The context
      * path is automatically set as a parameter associated with the {@code contextPath}
      * attribute name.
      * @param context
      * @throws IOException
      */
     public void render(HandlerContext context) throws HandlerException, IOException {
       
         Template templateAnn = getClass().getAnnotation(Template.class);
         if (templateAnn == null) {
             throw new MissingAnnotationException(Template.class);
         }
         
         String templatePath = context.getServletContext().getRealPath(templateAnn.path());
         XHTMLTemplateGroup group =
                 new XHTMLTemplateGroup(getClass().getName(), templatePath);
         StringTemplate template = group.getInstanceOf(templateAnn.name());
         template.setAttribute("contextPath", context.getServletContext().getContextPath());
         prepareTemplate(context, template);
         PrintWriter out = context.getResponse().getWriter();
         out.println(template.toString());
         out.close();
     }
     
    public abstract void prepareTemplate(HandlerContext context, StringTemplate template) throws HandlerException;
 
 }
