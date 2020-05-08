 /*
  * Copyright (c) 2008, Damian Carrillo
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted 
  * provided that the following conditions are met:
  * 
  *   * Redistributions of source code must retain the above copyright notice, this list of 
  *     conditions and the following disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of 
  *     conditions and the following disclaimer in the documentation and/or other materials 
  *     provided with the distribution.
  *   * Neither the name of the copyright holder's organization nor the names of its contributors 
  *     may be used to endorse or promote products derived from this software without specific 
  *     prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR 
  * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL 
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
  * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT 
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package co.cdev.agave.template;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import javax.servlet.jsp.JspException;
 
 /**
  * Supplies a JSP fragment to be used by the applied template.
  * 
  * @author damian
  */
 public class SupplyFragmentTag extends TemplateTag {
 
 	private static final long serialVersionUID = 1L;
 	private String name;
     private boolean additive = false;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public boolean isAdditive() {
         return additive;
     }
 
     public void setAdditive(boolean additive) {
         this.additive = additive;
     }
     
     @Override
     public int doEndTag() throws JspException {
         super.doEndTag();
 
        if (bodyContent != null) {
            if (additive) {
                Map<String, List<String>> globalFragments = getGlobalFragmentMap();
                if (globalFragments.get(name) == null) {
                    globalFragments.put(name, new ArrayList<String>());
                }
                globalFragments.get(name).add(bodyContent.getString());
            } else {
                Map<String, String> templateFragments = getCurrentFragmentMap();
                templateFragments.put(name, bodyContent.getString());
             }
         }
         
         return EVAL_PAGE;
     }
 
 }
