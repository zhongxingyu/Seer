 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.util;
 
 public final class StringUtils {
    private static final String SPACES;
    private static final String DASHES;
    private static final String SHARPS;
 
    private static final String NULL_STRING = "<null>";
 
    static {
       String sp = "                                                                               ";
       sp = sp + sp;
       sp = sp + sp;
       sp = sp + sp;
       SPACES = sp;
 
       String d = "-------------------------------------------------------------------------------";
       d = d + d;
       d = d + d;
       d = d + d;
       DASHES = d;
 
       String s = "###############################################################################";
       s = s + s;
       s = s + s;
       s = s + s;
       SHARPS = s;
    }
 
 
    private StringUtils() {
    }
 
 
    public static String toString(final Object obj) {
       if (obj == null) {
          return StringUtils.NULL_STRING;
       }
       return obj.toString();
    }
 
 
    public static String toString(final Object[] collection) {
       if (collection == null) {
          return StringUtils.NULL_STRING;
       }
 
       final StringBuilder buffer = new StringBuilder();
       boolean first = true;
       for (final Object o : collection) {
          if (!first) {
             first = false;
             buffer.append(",");
          }
          buffer.append(o);
       }
 
       return buffer.toString();
    }
 
 
    public static String spaces(final int count) {
       return StringUtils.SPACES.substring(0, count);
    }
 
 
    public static String dashes(final int count) {
       return StringUtils.DASHES.substring(0, count);
    }
 
 
    public static String sharps(final int count) {
      return StringUtils.SHARPS.substring(0, count);
    }
 
 }
