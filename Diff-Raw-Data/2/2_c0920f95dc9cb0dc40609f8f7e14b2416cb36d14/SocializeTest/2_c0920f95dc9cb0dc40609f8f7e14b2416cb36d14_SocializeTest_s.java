 /*
  * Copyright (c) 2011 Socialize Inc.
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package com.socialize.test;
 
 import com.google.android.testing.mocking.AndroidMock;
 import com.google.android.testing.mocking.UsesMocks;
 import com.socialize.Socialize;
 import com.socialize.SocializeService;
 import com.socialize.android.ioc.IOCContainer;
 import com.socialize.api.SocializeSession;
 import com.socialize.entity.Comment;
 import com.socialize.error.SocializeException;
 import com.socialize.listener.comment.CommentAddListener;
 
 /**
  * @author Jason Polites
  *
  */
 @UsesMocks({IOCContainer.class, SocializeService.class, SocializeSession.class})
 public class SocializeTest extends SocializeUnitTest {
 	
 	public void testSocializeInitDestroy() {
 		
 		IOCContainer container = AndroidMock.createMock(IOCContainer.class);
 		SocializeService service = AndroidMock.createMock(SocializeService.class, getContext());
 		
 		AndroidMock.expect(container.getBean("socializeService")).andReturn(service);
 		
 		container.destroy();
 		
 		AndroidMock.replay(container);
 		
 		Socialize socialize = new Socialize();
 		socialize.init(getContext(), container);
 		
		assertFalse(socialize.isInitialized());
 		
 		socialize.destroy();
 		
 		assertFalse(socialize.isInitialized());
 		
 		AndroidMock.verify(container);
 	}
 	
 	@UsesMocks ({CommentAddListener.class})
 	public void testAddCommentSuccess() {
 		IOCContainer container = AndroidMock.createMock(IOCContainer.class);
 		SocializeService service = AndroidMock.createMock(SocializeService.class, getContext());
 		CommentAddListener listener = AndroidMock.createMock(CommentAddListener.class);
 		SocializeSession session = AndroidMock.createMock(SocializeSession.class);
 		
 		final String key = "foo", comment = "bar";
 		
 		AndroidMock.expect(container.getBean("socializeService")).andReturn(service);
 
 		service.addComment(session, key, comment, listener);
 		
 		AndroidMock.replay(container);
 		AndroidMock.replay(service);
 		
 		Socialize socialize = new Socialize();
 		socialize.init(getContext(), container);
 		
 		assertTrue(socialize.isInitialized());
 		
 		socialize.addComment(session, key, comment, listener);
 		
 		AndroidMock.verify(container);
 		AndroidMock.verify(service);
 	}
 	
 	public void testAddCommentFail() {
 		SocializeSession session = AndroidMock.createMock(SocializeSession.class);
 		
 		final String key = "foo", comment = "bar";
 		
 		CommentAddListener listener = new CommentAddListener() {
 			@Override
 			public void onError(SocializeException error) {
 				addResult(error);
 				
 			}
 			@Override
 			public void onCreate(Comment entity) {}
 		};
 
 		Socialize socialize = new Socialize();
 		
 		assertFalse(socialize.isInitialized());
 		
 		socialize.addComment(session, key, comment, listener);
 		
 		Exception error = getResult();
 		
 		assertNotNull(error);
 		assertTrue(error instanceof SocializeException);
 		
 	}
 	
 }
