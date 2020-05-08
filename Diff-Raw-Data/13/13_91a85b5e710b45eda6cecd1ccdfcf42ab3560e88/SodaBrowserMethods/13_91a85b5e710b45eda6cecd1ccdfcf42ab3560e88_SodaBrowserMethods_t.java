 /*
 Copyright 2011 Trampus Richmond. All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are
 permitted provided that the following conditions are met:
 
    1. Redistributions of source code must retain the above copyright notice, this list of
       conditions and the following disclaimer.
 
    2. Redistributions in binary form must reproduce the above copyright notice, this list
       of conditions and the following disclaimer in the documentation and/or other materials
       provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY TRAMPUS RICHMOND ``AS IS'' AND ANY EXPRESS OR IMPLIED
 WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 TRAMPUS RICHMOND OR
 CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 The views and conclusions contained in the software and documentation are those of the authors and 
 should not be interpreted as representing official policies, either expressed or implied, of Trampus Richmond.
  
  */
 
 package voodoodriver;
 
/**
 * An enum class for all of the valid browser actions.
 * 
 * @author trampus
 *
 */
 public enum SodaBrowserMethods {
 	BROWSER_cssprop,
 	BROWSER_cssvalue,
 	BROWSER_assertnot,
 	BROWSER_url,
 	BROWSER_send_keys,
 	BROWSER_assertPage,
 	BROWSER_exist,
 	BROWSER_jscriptevent,
 	BROWSER_assert;
 
	/**
	 * Checks to see if a given name exists in this enum.
	 * 
	 * @param aName The name of the action you want to see if it exists for this class.
	 * @return {@link boolean}
	 */
 	static public boolean isMember(String aName) {
 		boolean result = false;
 		SodaBrowserMethods[] values = SodaBrowserMethods.values();
 		
 		for (SodaBrowserMethods amethod : values) {
 			if (amethod.name().equals(aName)) {
 				result = true;
 				break;
 			}
 		}
 
 		return result;
 	}
 }
