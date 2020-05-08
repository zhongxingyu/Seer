 /*
 Copyright (c) 2003-2006, Lucas Holt
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
   Redistributions of source code must retain the above copyright notice, this list of
   conditions and the following disclaimer.
 
   Redistributions in binary form must reproduce the above copyright notice, this
   list of conditions and the following disclaimer in the documentation and/or other
   materials provided with the distribution.
 
   Neither the name of the Just Journal nor the names of its contributors
   may be used to endorse or promote products derived from this software without
   specific prior written permission.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY
 OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.
 */
 
 package com.justjournal.utility;
 
 /**
  * @author Lucas Holt
  *         User: laffer1
  *         Date: Sep 24, 2003
  *         Time: 11:39:50 AM
  */
 public final class Xml {
 
     /**
      * converts characters that are special in xml
      * to their equivalents.
      * <p/>
      * This does not alter elements that may be part of DTDs
     * such as HTML's &nbsp;.
      * <p/>
      * It currently does not handle numerical escapes as
      * defined in XML either. &#xA0; etc
      *
      * @param input
      * @return A string with xml friendly escaped sequences.
      */
     public static String cleanString(final String input) {
         String work = input;
 
         // warning, if this is already correct,
         // the &amp replacement could really screw things
         // up.  Need to somehow verify the document is ok
         // or at least that & is alone?
         work = StringUtil.replace(work, '&', "&amp;");
         work = StringUtil.replace(work, '"', "&quot;");
         work = StringUtil.replace(work, '<', "&lt;");
         work = StringUtil.replace(work, '>', "&gt;");
         work = StringUtil.replace(work, '\'', "&apos;");
 
         return work;
     }
 }
