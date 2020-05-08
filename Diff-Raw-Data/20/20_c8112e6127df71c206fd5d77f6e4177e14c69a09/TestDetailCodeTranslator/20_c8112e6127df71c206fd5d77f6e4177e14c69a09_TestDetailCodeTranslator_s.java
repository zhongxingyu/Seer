 /*******************************************************************************
  * Copyright (c) 2011 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.generator.base;
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.etrice.core.room.ActorClass;
 import org.eclipse.etrice.core.room.Attribute;
 import org.eclipse.etrice.core.room.DetailCode;
 import org.eclipse.etrice.core.room.FreeType;
 import org.eclipse.etrice.core.room.FreeTypedID;
 import org.eclipse.etrice.core.room.InterfaceItem;
 import org.eclipse.etrice.core.room.Message;
 import org.eclipse.etrice.core.room.Operation;
 import org.eclipse.etrice.core.room.Port;
 import org.eclipse.etrice.core.room.PrimitiveType;
 import org.eclipse.etrice.core.room.ProtocolClass;
 import org.eclipse.etrice.core.room.RoomFactory;
 import org.eclipse.etrice.core.room.RoomModel;
 import org.eclipse.etrice.core.room.Type;
 import org.eclipse.etrice.core.room.TypedID;
 import org.eclipse.etrice.generator.base.DetailCodeTranslator.ITranslationProvider;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Henrik Rentz-Reichert
  *
  */
 public class TestDetailCodeTranslator {
 
 	
 	/**
 	 * @author Henrik Rentz-Reichert
 	 *
 	 */
 	private final class TestTranslationProvider implements ITranslationProvider {
 		
 		@Override
 		public String getAttributeText(Attribute att, String orig) {
 			return ">"+att.getName()+"<";
 		}
 
 		@Override
 		public String getOperationText(Operation op, ArrayList<String> args, String orig) {
 			return ">"+op.getName()+"("+getArgList(args)+")<";
 		}
 
 		@Override
 		public String getInterfaceItemMessageText(InterfaceItem item, Message msg, ArrayList<String> args, String orig) {
 			return ">"+item.getName()+"."+msg.getName()+"("+getArgList(args)+")<";
 		}
 		
 		@Override
 		public String getInterfaceItemMessageValue(InterfaceItem item, Message msg, String orig) {
 			return ">"+item.getName()+"."+msg.getName()+"<";
 		}
 
 		private String getArgList(ArrayList<String> args) {
 			StringBuilder result = new StringBuilder();
 			for (String string : args) {
 				result.append(string+", ");
 			}
 			int end = args.isEmpty()? result.length():result.length()-2;
 			return result.substring(0, end);
 		}
 	}
 
 	private RoomModel model;
 	private ActorClass ac;
 	private DetailCodeTranslator translator;
 
 	@Before
 	public void setUp() {
 		model = RoomFactory.eINSTANCE.createRoomModel();
 		
 		ProtocolClass pc = RoomFactory.eINSTANCE.createProtocolClass();
 		model.getProtocolClasses().add(pc);
 		pc.setName("TestProtocol");
 		Message out1 = RoomFactory.eINSTANCE.createMessage();
 		out1.setName("out1");
 		Message out2 = RoomFactory.eINSTANCE.createMessage();
 		out2.setName("out2");
 		TypedID typedID = RoomFactory.eINSTANCE.createTypedID();
 		typedID.setName("param");
 		Type type = RoomFactory.eINSTANCE.createType();
 		type.setPrim(PrimitiveType.INT32);
 		typedID.setType(type);
 		out2.setData(typedID);
 		pc.getOutgoingMessages().add(out1);
 		pc.getOutgoingMessages().add(out2);
 		Message in1 = RoomFactory.eINSTANCE.createMessage();
 		in1.setName("in1");
 		pc.getIncomingMessages().add(in1);
 		
 		ac = RoomFactory.eINSTANCE.createActorClass();
 		model.getActorClasses().add(ac);
 		ac.setName("TestActor");
 		
 		Port port = RoomFactory.eINSTANCE.createPort();
 		port.setName("fct");
 		port.setProtocol(pc);
 		ac.getIntPorts().add(port);
 		
 		Attribute attr = RoomFactory.eINSTANCE.createAttribute();
 		attr.setName("value");
 		attr.setType(EcoreUtil.copy(type));
 		ac.getAttributes().add(attr);
 
 		Operation op0 = RoomFactory.eINSTANCE.createOperation();
 		op0.setName("bar0");
 		ac.getOperations().add(op0);
 		
 		Operation op1 = RoomFactory.eINSTANCE.createOperation();
 		op1.setName("bar1");
 		FreeTypedID param1 = RoomFactory.eINSTANCE.createFreeTypedID();
 		param1.setName("param");
 		FreeType ft = RoomFactory.eINSTANCE.createFreeType();
 		ft.setPrim(PrimitiveType.INT16);
 		param1.setType(ft);
 		op1.getArguments().add(param1);
 		ac.getOperations().add(op1);
 		
 		Operation op2 = RoomFactory.eINSTANCE.createOperation();
 		op2.setName("bar2");
 		FreeTypedID param2 = RoomFactory.eINSTANCE.createFreeTypedID();
 		param2.setName("param");
 		ft.setPrim(PrimitiveType.INT32);
 		param2.setType(EcoreUtil.copy(ft));
 		op2.getArguments().add(EcoreUtil.copy(param1));
 		op2.getArguments().add(param2);
 		ac.getOperations().add(op2);
 		
 		translator = new DetailCodeTranslator(ac, new TestTranslationProvider());
 	}
 	
 	@Test
 	public void testSingleComment() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("//");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("comment", "//", result);
 	}
 	
 	@Test
 	public void testMultiComment() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("/* some comment");
 		dc.getCommands().add("continued");
 		dc.getCommands().add("*/");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("comment", "/* some comment\ncontinued\n*/", result);
 	}
 	
 	@Test
 	public void testPortNonExMsg() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out();");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.non_ex_message _no_ replacement", "fct.out();", result);
 	}
 	
 	@Test
 	public void testPortMsg() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out1();");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message replacement", ">fct.out1()<;", result);
 	}
 	
 	@Test
 	public void testPortMsgValue() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("x = 2*fct.in1;");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message as value (getter) replacement", "x = 2*>fct.in1<;", result);
 	}
 	
 	@Test
 	public void testPortMsgValueNoReplace() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("x = 2*fct.out1;");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message as value (getter) replacement", "x = 2*fct.out1;", result);
 	}
 	
 	@Test
 	public void testPortMsgComments() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct . out1  (/*comment*/");
 		dc.getCommands().add("//comment");
 		dc.getCommands().add("  );");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message (with comments and white space) replacement", ">fct.out1()<;", result);
 	}
 	
 	@Test
 	public void testPortMsgData() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out2(123);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message(data) replacement", ">fct.out2(123)<;", result);
 	}
 	
 	@Test
 	public void testPortMsgDataComment() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out2(123/4 /*comment*/);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message(data) replacement", ">fct.out2(123/4 /*comment*/)<;", result);
 	}
 	
 	@Test
 	public void testPortMsgDataFloat() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out2(123.4);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message(data) replacement", ">fct.out2(123.4)<;", result);
 	}
 	
 	@Test
 	public void testPortMsgDataComplex() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("fct.out2(foxy(abc, 12.3));");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("port.message(data) replacement", ">fct.out2(foxy(abc, 12.3))<;", result);
 	}
 	
 	@Test
 	public void testAttribute() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("value = 1;");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("attribute replacement", ">value< = 1;", result);
 	}
 	
 	@Test
 	public void testOperation0() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar0();");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("fct() replacement", ">bar0()<;", result);
 	}
 	
 	@Test
 	public void testOperation1() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar1(123);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("bar1(123) replacement", ">bar1(123)<;", result);
 	}
 	
 	@Test
 	public void testOperation2() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar2(123, 456);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("bar2(123, 456) replacement", ">bar2(123, 456)<;", result);
 	}
 	
 	@Test
 	public void testOperation3() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar2(123, value);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("bar2(123, value) recursive replacement", ">bar2(123, >value<)<;", result);
 	}
 	
 	@Test
 	public void testOperation4() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar2(123, bar1(value));");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("bar2(123, value) recursive replacement", ">bar2(123, >bar1(>value<)<)<;", result);
 	}
 	
 	@Test
 	public void testOperationWrongNArg() {
 		DetailCode dc = RoomFactory.eINSTANCE.createDetailCode();
 		dc.getCommands().add("bar2(123, 456, 789);");
 		
 		String result = translator.translateDetailCode(dc);
 		
 		assertEquals("fct(123) no replacement", "bar2(123, 456, 789);", result);
 	}
 
 }
