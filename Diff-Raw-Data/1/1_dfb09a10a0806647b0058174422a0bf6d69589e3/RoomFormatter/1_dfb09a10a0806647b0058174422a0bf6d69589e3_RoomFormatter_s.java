 /*******************************************************************************
  * Copyright (c) 2010 protos software gmbh (http://www.protos.de).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * CONTRIBUTORS:
  * 		Thomas Schuetz and Henrik Rentz-Reichert (initial contribution)
  * 
  *******************************************************************************/
 
 package org.eclipse.etrice.core.formatting;
 
 import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
 import org.eclipse.xtext.formatting.impl.FormattingConfig;
 import org.eclipse.xtext.util.Pair;
 
 import org.eclipse.xtext.Keyword;
 
 
 /**
  * @author Herward Ahlheit 
  * 
  * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an example
  */
 public class RoomFormatter extends AbstractDeclarativeFormatter {
 	
 	@Override
 	protected void configureFormatting(FormattingConfig c) {
 		org.eclipse.etrice.core.services.RoomGrammarAccess f = (org.eclipse.etrice.core.services.RoomGrammarAccess) getGrammarAccess();
 
 		// general
 		
 		c.setAutoLinewrap(120);
 		c.setLinewrap(2).before(f.getSL_COMMENTRule());
 		c.setLinewrap(2).before(f.getML_COMMENTRule());
 		
 		for (Pair<Keyword, Keyword> pair : f.findKeywordPairs("{", "}")) {
 			c.setLinewrap().after(pair.getFirst());
 			c.setIndentationIncrement().after(pair.getFirst());
 			c.setLinewrap().before(pair.getSecond());
 			c.setIndentationDecrement().before(pair.getSecond());
 		}		
 	
 		for (Keyword k: f.findKeywords("(", "|")) {
 			c.setNoSpace().around(k);
 		}
 		
 		for (Keyword k: f.findKeywords("<")) {
 			c.setNoSpace().after(k);
 		}
 		for (Keyword k: f.findKeywords(")", ">", ",")) {
 			c.setNoSpace().before(k);
 		}
 		
 		//"import"
 		c.setLinewrap(2).around(f.getImportRule());
 		
 		
 		// classes
 		
 		//"LogicalSystem"
 		c.setLinewrap(2).around(f.getLogicalSystemRule());
 		
 		//"SubSystemClass"
 		c.setLinewrap(2).around(f.getSubSystemClassRule());
 		
 		//"ActorClass"
 		c.setLinewrap(2).around(f.getActorClassRule());
 
 		//"DataClass"
 		c.setLinewrap(2).around(f.getDataClassRule());
 		
 		//"ProtocolClass"
 		c.setLinewrap(2).around(f.getProtocolClassRule());
 		
 		//"SubSystemRef"
 		c.setLinewrap().around(f.getSubSystemRefRule());
 		
 		
 		// structure classes
 		
 		//"ActorRef"
 		c.setLinewrap().around(f.getActorRefRule());
 		
 		//"LayerConnection"
 		c.setLinewrap().around(f.getLayerConnectionRule());
 		
 		//"Interface"
 		//c.setLinewrap().before(f.getActorClassAccess().getInterfaceKeyword_5_0());
 		
 		//"Port"
 		c.setLinewrap().around(f.getPortRule());
 		
 		//"ExternalPort"
 		c.setLinewrap().around(f.getExternalPortRule());
 		
 		//":"
 		for (Keyword k: f.findKeywords(":")) {
 			c.setNoSpace().before(k);
 		}
 		
 		//"LogicalThread"
 		c.setLinewrap().around(f.getLogicalThreadRule());
 
 		//commands+=STRING+
 		c.setLinewrap().around(f.getDetailCodeAccess().getCommandsAssignment_1());
 		
 		//bindings+=Binding*
 		c.setLinewrap().around(f.getBindingRule());
 		
 		//"."
 		for (Keyword k: f.findKeywords(".")) {
 			c.setNoSpace().around(k);
 		}
 
 		// state graph items
 		c.setLinewrap().around(f.getStateRule());
 		c.setLinewrap().around(f.getTrPointRule());
 		c.setLinewrap().around(f.getChoicePointRule());
 		c.setLinewrap().around(f.getTransitionRule());
 
 		//"SAP"
 		c.setLinewrap().around(f.getSAPRefRule());
 
 		//"SPP"
 		c.setLinewrap().around(f.getSPPRefRule());
 		
 		//"Attribute"
 		c.setLinewrap().around(f.getAttributeRule());
 		
 		for (Keyword k: f.findKeywords("entry", "exit", "StateMachine", "subgraph", "action", "cond", "regular", "conjugated",
 				"incoming", "outgoing", "Structure", "Behavior", "Interface", "usercode", "usercode1", "usercode2")) {
 			c.setLinewrap().before(k);
 		}
 
 		//"triggers"
 		c.setLinewrap().around(f.getTriggerRule());
 
 		// messages
 		c.setLinewrap().around(f.getMessageRule());
 
 		// operation
 		c.setLinewrap().around(f.getOperationRule());
 		
 		// protocol related
 		c.setLinewrap().after(f.getPortClassRule());
 		c.setLinewrap().around(f.getMessageHandlerRule());
 		c.setLinewrap().around(f.getProtocolSemanticsRule());
 		c.setLinewrap().around(f.getSemanticsRuleRule());
 	}
 }
