 /**
  * Copyright (c) 2010-2012, Netradius, LLC
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 	* Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  * 	* Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * 	* Neither the name of Netradius, LLC nor the names of its
  * contributors may be used to endorse or promote products derived from this
  * software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
  * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
  * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package com.netradius.commons.servlet;
 
 import org.junit.Test;
 
 import javax.servlet.ServletException;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import static org.junit.Assert.assertTrue;
 
 public class YUICompressorTest {
 
 	@Test
 	public void testYUIFilter() throws IOException, ServletException {
//		MockFilterConfig config = new MockFilterConfig();
//		String path = getClass().getResource("date.js").getPath();
//		File file = new File(path);
//		config.setInitParameter(YUICompressorFilter.RESOURCE_LOCATION, path);
//		MockServletContext ctx = new MockServletContext();
//		config.setServletContext(ctx);
//
//		MockHttpServletRequest req = new MockHttpServletRequest();
//
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		MockHttpServletResponse res = new MockHttpServletResponse(out);
//		MockFilterChain chain = new MockFilterChain();
//		YUICompressorFilter filter = new YUICompressorFilter();
//		filter.init(config);
//		filter.doFilter(req, res, chain);
//		assertTrue(file.length() > out.size());
 	}
 
 }
