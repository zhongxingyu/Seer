 /* Copyright (c) 2011-2014, Dźmitry Laŭčuk
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
 import java.util.TreeMap;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.JspTagException;
 import javax.servlet.jsp.tagext.JspFragment;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import afc.util.DevNull;
 
 /**
  * <p>An abstract simple tag whose sub-classes produce a sequence of events which can be handled
  * by event handlers to render a complex structure by small pieces. Event handlers are
  * the {@link EventTag &lt;event&gt;} tags in the {@code <builder>} tag body. The {@code name}
  * attribute of the {@code event} tag defines the event this tag subscribes to. The algorithmic
  * part of the rendering process is implemented by the {@link #build()} function. Each time an event
  * is raised by {@code <builder>} (see {@link #raiseEvent(String)}) the correspondent
  * {@code <event>} tag body is evaluated. All parameters to the event handler (if any) are passed
  * in the JSP context.</p>
  * 
  * <p>The advantage of this approach to render content is clear separation between code that
  * generates structure (it is enclosed in the tag's {@code build()} function) and code that renders
  * output (it is enclosed in event handler tags' bodies). This allows for having simple and
  * straightforward JSP code while keeping all algorithmic complexity in Java code (which is easier
  * to create, test, and document). In addition, it is possible to implement different representations
  * on top of the same algorithm.</p>
  * 
  * <p>Below is the additional information that is useful to implement a custom {@code <builder>}
  * tag and use is properly:</p>
  * <ul>
  *  <li>{@code <builder>}'s body is evaluated once and before {@code build()} is invoked. The content
  *      generated while evaluating the body is omitted.</li>
  *  <li>It is allowed to have no body for the {@code <builder>} tag. The {@code build()} function
  *      is invoked in this case while having no output rendered.</li>
  *  <li>If an exception is thrown while evaluating {@code <builder>}'s body then {@code build()}
  *      is not invoked.</li>
  *  <li>{@code <event>} tags could be direct or indirect inner tags of the {@code <builder>} tag
  *      to be registered as event handlers.</li>
  *  <li>Only a single event handler could be registered to listen for a given event.</li>
  *  <li>It is allowed to have no event handler for an event. In this case the events are ignored
  *      with no error. Event handler tags with empty bodies have the same effect.</li>
  * </ul>
  * 
  * <p>An example below demonstrates a simple use case of the {@code <builder>}/{@code <event>} tags.</p>
  * <h3>Example</h3>
  * <h4>The builder tag</h4>
  * <pre>{@literal
  * public class DigitsTag extends BuilderTagBase
  * { 
  *   protected void build() throws IOException, JspException
  *   {
  *     raiseEvent("start");
 *     for (int i = 0; i < 10; ++i) {
  *       if (i != 0) {
  *         raiseEvent("separator");
  *       }
  *       getJspContext().setAttribute("digit", i);
  *       raiseEvent("digit");
  *     }
  *     raiseEvent("end");
  *   }
  * }}</pre>
  * 
  * <h4>JSP fragment</h4>
  * <pre>{@literal
  * <afc:digits>
  *   <afc:event name="start">digits: [</afc:event>
  *   <afc:event name="end">];</afc:event>
  *   <afc:event name="digit">${digit}</afc:event>
  *   <afc:event name="separator"> </afc:event>
  * </afc:digits>}</pre>
  * 
  * <h4>JSP output</h4>
  * <pre>{@literal digits: [0 1 2 3 4 5 6 7 8 9];}</pre>
  * 
  * <p>In this example the JSP code has neither loops nor branches.</p>
  *
  * @author D&#378;mitry La&#365;&#269;uk
  */
 public abstract class BuilderTagBase extends SimpleTagSupport
 {
     private final TreeMap<String, EventTag> events = new TreeMap<String, EventTag>();
     
     final void register(final EventTag eventHandler) throws JspTagException
     {
         final String name = eventHandler.getName();
         // TODO check if this could be checked by a tag validator
         if (name == null || name.length() == 0) {
             throw new JspTagException("Event name is undefined.");
         }
         // TODO check if this could be checked by a tag validator
         if (events.put(name, eventHandler) != null) {
             throw new JspTagException("Duplicate event handler for the event '" + name + "'.");
         }
     }
     
     @Override
     public final void doTag() throws JspException, IOException
     {
         final JspFragment body = getJspBody();
         if (body != null) { // else invoke build against no events
             body.invoke(DevNull.instance);
         }
         build();
     }
     
     protected abstract void build() throws JspException, IOException;
     
     protected final void raiseEvent(final String name) throws JspException, IOException
     {
         final EventTag event = events.get(name);
         if (event != null) {
             event.raise();
         }
     }
 }
