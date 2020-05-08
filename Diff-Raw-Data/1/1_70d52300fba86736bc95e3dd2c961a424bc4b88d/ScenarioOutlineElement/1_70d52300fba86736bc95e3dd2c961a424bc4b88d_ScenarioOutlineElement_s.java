 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.gui.elements;
 
 import com.trollsahead.qcumberless.model.Locale;
 import com.trollsahead.qcumberless.util.ElementHelper;
 
 public class ScenarioOutlineElement extends ScenarioElement {
     public ScenarioOutlineElement(int rootType) {
         super(rootType);
         type = TYPE_SCENARIO_OUTLINE;
         folded = true;
         appendExamples();
     }
 
     public ScenarioOutlineElement(int rootType, String title) {
         super(rootType, title);
         type = TYPE_SCENARIO_OUTLINE;
         folded = true;
         appendExamples();
     }
 
     public ScenarioOutlineElement(int rootType, String title, int width) {
         super(rootType, title, width);
         type = TYPE_SCENARIO_OUTLINE;
         folded = true;
         appendExamples();
     }
 
     private void appendExamples() {
         addChild(new ExamplesElement());
     }
 
     public BaseBarElement duplicate() {
         ScenarioOutlineElement element = new ScenarioOutlineElement(rootType, title, calculateRenderWidthFromRoot(rootType));
        //element.removeChild(element.getExamplesElement());
         duplicatePropertiesTo(element);
         return element;
     }
 
     public StringBuilder buildFeatureInternal(boolean addRunOutcome, long time) {
         StringBuilder sb = buildFeatureInternalSkipThis(addRunOutcome, time);
         sb.append(ElementHelper.EXPORT_INDENT).append(Locale.getString("scenario outline")).append(": ").append(title).append("\n");
         return sb;
     }
 
     public ExamplesElement getExamplesElement() {
         return children != null && children.size() > 0 ? (ExamplesElement) children.get(children.size() - 1) : null;
     }
 }
