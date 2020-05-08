 /*===========================================================================
   Copyright (C) 2008-2010 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.persistence;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.fail;
 
 import java.io.UnsupportedEncodingException;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.resource.InlineAnnotation;
 import net.sf.okapi.common.resource.TextContainer;
 import net.sf.okapi.common.resource.TextUnit;
 import net.sf.okapi.common.resource.TextUnitUtil;
 import net.sf.okapi.common.skeleton.GenericSkeleton;
 import net.sf.okapi.persistence.beans.v1.OkapiBeans;
 import net.sf.okapi.steps.xliffkit.common.persistence.sessions.OkapiJsonSession;
 
 import org.junit.Test;
 
 
 public class TestJsonSession {
 
 //	private String toJsonString(IAnnotation annotation) throws UnsupportedEncodingException {
 //		OkapiJsonSession session = new OkapiJsonSession();
 //		session.setItemClass(IAnnotation.class);
 //		session.setItemLabel("annotation");
 //		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 //		session.start(baos);
 //		session.serialize(annotation);
 //		session.end();
 //		
 //		return new String(baos.toByteArray(), "UTF-8");		
 //	}
 	
 	// DEBUG 
 	@Test
 	public void testReadWriteObject() throws UnsupportedEncodingException {
 		OkapiJsonSession session = new OkapiJsonSession();		
 		session.setVersion(OkapiBeans.VERSION);
 		
 		System.out.println("===== Annotation");
 		InlineAnnotation annot1 = new InlineAnnotation();
 		annot1.setData("test inline annotation");
 		String st1 = session.writeObject(annot1);
 		System.out.println(st1 + "\n\n");
 		
 		System.out.println("===== TextUnit");
 		TextUnit tu1 = TextUnitUtil.buildTU("source-text1" + (char) 2 + '"' + " : " + '"' + 
 				'{' + '"' + "ssssss " + ':' + '"' + "ddddd" + "}:" + '<' + '>' + "sssddd: <>dsdd");
		//tu1.setSkeleton(new GenericSkeleton());
		//------------
 		GenericSkeleton gs = new GenericSkeleton("before");
 		//--ClassCastException if using addContentPlaceholder
		gs.addContentPlaceholder(tu1);
 		gs.append("after");
 		
 		System.out.println(gs);
 		
 		tu1.setSkeleton(gs);
		//------------
 		tu1.setTarget(LocaleId.FRENCH, new TextContainer("french-text1"));
 		tu1.setTarget(LocaleId.TAIWAN_CHINESE, new TextContainer("chinese-text1"));
 		String st2 = session.writeObject(tu1);
 		System.out.println(st2);
 		
 		
 		session.setVersion(OkapiBeans.VERSION);
 		InlineAnnotation annot2 = session.readObject(st1, InlineAnnotation.class);
 		assertEquals(annot1.getData(), annot2.getData());
 		
 		TextUnit tu2 = session.readObject(st2, TextUnit.class);
 		assertEquals(tu1.getSource().toString(), tu2.getSource().toString());
 		
 		// Wrong version
 		session.setVersion("OKAPI 0.0");
 		try {
 			annot2 = session.readObject(st1, InlineAnnotation.class);
 			assertEquals(annot1.getData(), annot2.getData());
 			
 			tu2 = session.readObject(st2, TextUnit.class);
 			assertEquals(tu1.getSource().toString(), tu2.getSource().toString());
 		} catch (RuntimeException e) {
 			return;
 		}
 		fail("RuntimeException should have been thrown");
 	}
 	
 }
