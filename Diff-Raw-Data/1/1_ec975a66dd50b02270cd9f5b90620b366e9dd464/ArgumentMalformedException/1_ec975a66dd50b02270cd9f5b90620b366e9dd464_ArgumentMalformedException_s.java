 /*
  * $Id: ArgumentMalformedException.java 1011 2008-06-16 17:57:36Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *     * Neither the name of the jCoderZ.org Project nor the names of
  *       its contributors may be used to endorse or promote products
  *       derived from this software without specific prior written
  *       permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons;
 
import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Dummy implementation of the ArgumentMalfromedException.
  * 
  * Will be replaced after bootstrap phase.
  * 
  * @author Michael Griffel
  */
 public class ArgumentMalformedException
         extends RuntimeException
 {
 	private static final long serialVersionUID = 1L;
 
 	public ArgumentMalformedException (String param, String value, String hint, 
             Throwable cause)
     {
         super(generateMessage(param, value, hint), cause);
     }
 
     public ArgumentMalformedException (String param, String value, String hint)
     {
         super(generateMessage(param, value, hint));
     }
     
     private static String generateMessage (String param, String value, String hint)
     {
         return "The given argument is at least partly malformed. " 
         + "Argument '" + param + "' cannot be set to " 
         + "value '" + value + "'. Detailed description: " + hint + ".";
     }
 
     public List<String> getParameter (String name)
     {
        return null;
     }
 
     public Set<String> getParameterNames ()
     {
        return null;
     }
 }
