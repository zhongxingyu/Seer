 /*
 Copyright (c) 2010 Geoff Lewis <gsl@gslsrc.net>
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 */
 
 package net.gslsrc.dmex.exercise.tables.render;
 
 import net.gslsrc.dmex.exercise.tables.TablesRevisionProblem;
 import net.gslsrc.dmex.render.xsl.DefaultProblemTemplates;
 
 /**
 * Provides the XSL templates for tables revision work sheets.  This class is
 * registered as a provider for the
 * {@link net.gslsrc.dmex.render.xsl.ProblemTemplates} service for the
 * {@link TablesRevisionProblem} type.
 * <p>
 * The mapping of
 * {@link net.gslsrc.dmex.render.xsl.ProblemTemplates.OutputType} to XSL
 * stylesheet is given by the "TablesRevisionTemplates.properties" resource.
 * All instances of this resource on the classpath will be loaded so you can
 * extend the available templates by adding stylesheets in a library with its
 * own "TablesRevisionTemplates.properties" file.
  *
  * @author Geoff Lewis
  */
 public class TablesRevisionTemplates extends DefaultProblemTemplates {
 
     private static final String PROPS_FILE =
             "TablesRevisionTemplates.properties";
 
     public TablesRevisionTemplates() {
         super(TablesRevisionProblem.class, PROPS_FILE);
     }
 
     @Override
     public String toString() {
         return "TablesRevisionTemplates";
     }
 }
