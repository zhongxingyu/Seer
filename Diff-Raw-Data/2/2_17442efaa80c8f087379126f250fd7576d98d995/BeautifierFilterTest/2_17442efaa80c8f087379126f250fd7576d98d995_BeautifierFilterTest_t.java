 /*
  * Copyright (c) 2010 HtmlUnit team.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.sourceforge.htmlunit.proxy;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 /**
  * Test for {@link BeautifierFilter}.
  *
  * @author Ahmed Ashour
  * @version $Revision$
  */
 public class BeautifierFilterTest {
 
     /**
      */
     @Test
     public void test() {
         final String source = "function oa(){if(!c.isReady){try{s.documentElement.doScroll(\"left\");}catch(a){"
             + "setTimeout(oa,1);return;}c.ready();}}"
             + "function La(a,b){b.src?c.ajax({url:b.src,async:false,dataType:\"script\"}):"
             + "c.globalEval(b.text||b.textContent||b.innerHTML||\"\");b.parentNode&&b.parentNode.removeChild(b);}"
             + "function $(a,b,d,f,e,i){var j=a.length;if(typeof b===\"object\"){for(var o in b)$(a,o,b[o],f,e,d);"
             + "return a;}if(d!==w){f=!i&&f&&c.isFunction(d);for(o=0;o<j;o++)e(a[o],b,f?d.call(a[o],o,e(a[o],b)):d,i);"
             + "return a;}return j?e(a[0],b):null;}function K(){return(new Date()).getTime();}function aa(){"
             + "return false;}"
             + "function ba(){return true;}function pa(a,b,d){d[0].type=a;return c.event.handle.apply(b,d);}"
             + "function qa(a){var b=true,d=[],f=[],e=arguments,i,j,o,p,n,t=c.extend({},c.data(this,\"events\").live);"
             + "for(p in t){j=t[p];if(j.live===a.type||j.altLive&&c.inArray(a.type,j.altLive)>-1){"
             + "i=j.data;i.beforeFilter&&i.beforeFilter[a.type]&&!i.beforeFilter[a.type](a)||f.push(j.selector);}"
             + "else delete t[p];}i=c(a.target).closest(f,a.currentTarget);n=0;for(l=i.length;n<l;n++)for(p in t){"
             + "j=t[p];o=i[n].elem;f=null;if(i[n].selector===j.selector){"
             + "if(j.live===\"mouseenter\"||j.live===\"mouseleave\")f=c(a.relatedTarget).closest(j.selector)[0];"
             + "if(!f||f!==o)d.push({elem:o,fn:j});}}n=0;for(l=d.length;n<l;n++){i=d[n];a.currentTarget=i.elem;"
             + "a.data=i.fn.data;if(i.fn.apply(i.elem,e)===false){b=false;break;}}return b;}"
             + "function ra(a,b){return[\"live\",a,b.replace(/\\./g,\"`\").replace(/ /g,\"&\")].join(\".\");}"
             + "function sa(a){return!a||!a.parentNode||a.parentNode.nodeType===11;}"
             + "function ta(a,b){var d=0;b.each(function(){if(this.nodeName===(a[d]&&a[d].nodeName)){"
             + "var f=c.data(a[d++]),e=c.data(this,f);if(f=f&&f.events){delete e.handle;e.events={};for(var i in f)"
            + "for(var j in f[i])c.event.add(this,i,f[i][j],f[i][j].data);}}});}" + "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!";
 
         final String beautified = new BeautifierFilter().beautify(source);
 
         assertEquals(source.replaceAll("\\s", ""), beautified.replaceAll("\\s", ""));
     }
 
 }
