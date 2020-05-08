 /* Copyright (c) 2013, Dźmitry Laŭčuk
    All rights reserved.
 
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met: 
 
    1. Redistributions of source code must retain the above copyright notice, this
       list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
       this list of conditions and the following disclaimer in the documentation
       and/or other materials provided with the distribution.
 
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */
 package afc.jsp.tag;
 
 import java.io.IOException;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.JspTag;
 import javax.servlet.jsp.tagext.SimpleTag;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import junit.framework.TestCase;
 
 public class BuilderTagBase_BoundaryCasesTest extends TestCase
 {
     private MockJspContext ctx;
     private StringJspWriter out;
     private GridTag gridTag;
     
     @Override
     protected void setUp()
     {
         out = new StringJspWriter();
         ctx = new MockJspContext(out);
         
         gridTag = new GridTag();
         gridTag.setJspContext(ctx);
     }
     
    @Override
    protected void tearDown()
    {
        ctx = null;
        out = null;
        gridTag = null;
    }
    
     private EventTag initEvent(final String name, final char charToPrint)
     {
         return initEvent(gridTag, name, charToPrint);
     }
     
     private EventTag initEvent(final JspTag parent, final String name, final char charToPrint)
     {
         final EventTag tag = new EventTag();
         tag.setJspContext(ctx);
         tag.setParent(parent);
         tag.setName(name);
         tag.setJspBody(new PrintCharJspFragment(charToPrint));
         return tag;
     }
     
     /**
      * Tests that non-registered events handlers are just ignored by BuilderTagBase.
      */
     public void testBuildGrid_NotAllEventHandlersRegistered() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag cellTag = initEvent("cell", '-');
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, cellTag);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         gridTag.doTag();
         
         assertTrue(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("^--^--^--", out.getOutput());
     }
     
     /**
      * Tests that events handlers for the events that are not used are allowed (though useless).
      */
     public void testBuildGrid_UnusedEventsRegistered() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag rowEndTag = initEvent("rowEnd", '$');
         final EventTag cellTag = initEvent("cell", '-');
         final EventTag unusedTag = initEvent("unused", '-');
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, rowEndTag, cellTag, unusedTag);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         gridTag.doTag();
         
         assertTrue(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("^--$^--$^--$", out.getOutput());
     }
     
     /**
      * Tests that event tag's parents are scanned deeply to find a BuilderTagBase parent.
      */
     public void testBuildGrid_EventsWithIndirectBuilderParentTag() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag rowEndTag = initEvent("rowEnd", '$');
         
         final SimpleTag cellTagParent = new SimpleTagSupport()
         {
             @Override
             public void doTag() throws IOException, JspException
             {
                 getJspBody().invoke(getJspContext().getOut());
             }
         };
         cellTagParent.setJspContext(ctx);
         cellTagParent.setParent(gridTag);
         final EventTag cellTag = initEvent(cellTagParent, "cell", '-');
         cellTagParent.setJspBody(new CallTagsJspFragment(cellTag));
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, rowEndTag, cellTagParent);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         gridTag.doTag();
         
         assertTrue(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("^--$^--$^--$", out.getOutput());
     }
     
     public void testBuildGrid_EventWithNullNameTag() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag rowEndTag = initEvent("rowEnd", '$');
         final EventTag cellTag = initEvent(null, '-');
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, rowEndTag, cellTag);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         try {
             gridTag.doTag();
             fail();
         }
         catch (JspException ex) {
             assertEquals("Event name is undefined.", ex.getMessage());
         }
         
         assertFalse(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("", out.getOutput());
     }
     
     public void testBuildGrid_EventWithEmptyNameTag() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag rowEndTag = initEvent("rowEnd", '$');
        final EventTag cellTag = initEvent("", '-');
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, rowEndTag, cellTag);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         try {
             gridTag.doTag();
             fail();
         }
         catch (JspException ex) {
             assertEquals("Event name is undefined.", ex.getMessage());
         }
         
         assertFalse(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("", out.getOutput());
     }
     
     public void testBuildGrid_EventsWithTheSameNameTag() throws Exception
     {
         final EventTag rowStartTag = initEvent("rowStart", '^');
         final EventTag rowEndTag = initEvent("rowEnd", '$');
         final EventTag cellTag = initEvent("cell", '-');
         final EventTag cellTag2 = initEvent("cell", '-');
         
         final CallTagsJspFragment body = new CallTagsJspFragment("foo", rowStartTag, rowEndTag, cellTag, cellTag2);
         gridTag.setJspBody(body);
         
         gridTag.rowCount = 3;
         gridTag.columnCount = 2;
         
         try {
             gridTag.doTag();
             fail();
         }
         catch (JspException ex) {
             assertEquals("Duplicate event handler for the event 'cell'.", ex.getMessage());
         }
         
         assertFalse(gridTag.buildInvoked);
         assertTrue(body.invoked);
         assertEquals("", out.getOutput());
     }
 }
