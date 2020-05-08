 /*
  * Copyright 2012 Barrie Treloar <barrie.treloar@gmail.com>
  *
  *  This file is part of USRPROPS Xtext Editor.
  *
  *  USRPROPS Xtext Editor is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU Lesser General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  USRPROPS Xtext Editor is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with USRPROPS Xtext Editor.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.github.usrprops_xtext.formatting;
 
 import org.eclipse.xtext.formatting.impl.AbstractDeclarativeFormatter;
 import org.eclipse.xtext.formatting.impl.FormattingConfig;
 
 import com.github.usrprops_xtext.services.UsrpropsDslGrammarAccess;
 
 /**
  * This class contains custom formatting description.
  *
  * see : http://www.eclipse.org/Xtext/documentation/latest/xtext.html#formatting
  * on how and when to use it
  *
  * Also see {@link org.eclipse.xtext.xtext.XtextFormattingTokenSerializer} as an
  * example
  */
 public class UsrpropsDslFormatter extends AbstractDeclarativeFormatter {
 
     /**
      * Formatter for Usrprops
      * <p>
      * It does not make sense to blanket wrap around all keywords. The output of
      * this is not very pretty.
      * </p>
      * <p>
      * There does not appear to be an easy way to programmatically loop through
      * a grammar rule to set values. e.g. Property rule in this code should all
      * be on one line (including sub-rules). See
      * http://www.eclipse.org/forums/index.php/m/988459/#msg_988459
      * </p>
      * <p>
      * Notes:
      * <ul>
      * <li>grammar.get*Rule() is used to specify line wrapping for most DSL
      * rules
      * <li>grammar.get*Access().getBeginAssignment* (and .getEndAssignment*) are
      * used to overwrite line wrapping around sub-rule begin/end statements when
      * needed.
      * </ul>
      * </p>
      */
     @Override
     protected void configureFormatting(FormattingConfig c) {
         UsrpropsDslGrammarAccess grammar = (UsrpropsDslGrammarAccess) getGrammarAccess();
 
         c.setAutoLinewrap(300);
 
         // BEGIN/END Blocks are indented
         c.setIndentationIncrement().after(grammar.getBeginRule());
         c.setIndentationDecrement().before(grammar.getEndRule());
         c.setLinewrap().around(grammar.getBeginRule());
         c.setLinewrap().around(grammar.getEndRule());
 
         // Top level rule are on their own lines
         // Major rules get an extra whitespace between them.
         // Everything else can have betwen 1 and 5 newlines between them
         c.setLinewrap(1, 1, 5).before(grammar.getIncludeRule());
         c.setLinewrap(1, 1, 5).before(grammar.getRenameRule());
         c.setLinewrap(2).before(grammar.getListRule());
         c.setLinewrap(2).before(grammar.getDiagramRule());
         c.setLinewrap(2).before(grammar.getSymbolRule());
         c.setLinewrap(2).before(grammar.getDefinitionRule());
         c.setLinewrap(1,2,2).before(grammar.getAssignInToRule());
 
         // REM can have 0 to 5 newlines, defaults to 1.
         c.setLinewrap(0, 1, 5).around(grammar.getREMRule());
 
         // Addressable is on its own line.
         c.setLinewrap().after(grammar.getAddressableRule());
 
         // Hierarchical is on its own line.
         c.setLinewrap().after(grammar.getHierachicalRule());
 
         // Chapter is on its own line.
         c.setLinewrap(2).before(grammar.getChapterRule());
         c.setLinewrap().after(grammar.getChapterRule());
 
         // Property and its sub-rules are on one line.
         c.setNoLinewrap().around(grammar.getPropertyAccess().getBeginAssignment_2());
         c.setNoLinewrap().around(grammar.getEditRule());
         c.setNoLinewrap().around(grammar.getPropertyAccess().getOptionsPropertyOptionParserRuleCall_3_0());
         c.setNoLinewrap().around(grammar.getDisplayAccess().getBeginAssignment_1());
         c.setNoLinewrap().around(grammar.getDisplayAccess().getEndAssignment_4());
         c.setNoLinewrap().around(grammar.getBordersAccess().getBeginAssignment_1());
         c.setNoLinewrap().around(grammar.getBordersAccess().getEndAssignment_9());
         c.setNoLinewrap().around(grammar.getDepictionsAccess().getBeginAssignment_1());
         c.setNoLinewrap().before(grammar.getDepictionsAccess().getEndAssignment_3());
         c.setLinewrap(0,1,1).after(grammar.getDepictionsAccess().getEndAssignment_3());
         c.setNoLinewrap().around(grammar.getFillColorAccess().getBeginAssignment_1());
         c.setNoLinewrap().around(grammar.getFillColorAccess().getEndAssignment_7());
         c.setNoLinewrap().around(grammar.getKeyedByAccess().getBeginAssignment_1());
         c.setNoLinewrap().around(grammar.getKeyedByAccess().getEndAssignment_4());
         c.setNoSpace().around(grammar.getKeyedByClauseAccess().getColonKeyword_1_1_0());
         c.setNoSpace().before(grammar.getKeyedByAccess().getCommaKeyword_3_0());
         c.setNoLinewrap().around(grammar.getPlacementAccess().getBeginAssignment_1());
         c.setNoLinewrap().around(grammar.getPlacementAccess().getEndAssignment_5());
         c.setNoSpace().around(grammar.getLabelPositionAccess().getLeftParenthesisKeyword_1());
         c.setNoSpace().around(grammar.getLabelPositionAccess().getCommaKeyword_3());
         c.setNoSpace().before(grammar.getLabelPositionAccess().getRightParenthesisKeyword_5());
         c.setNoSpace().around(grammar.getPropertyPositionAccess().getLeftParenthesisKeyword_1());
         c.setNoSpace().around(grammar.getPropertyPositionAccess().getCommaKeyword_3());
         c.setNoSpace().before(grammar.getPropertyPositionAccess().getRightParenthesisKeyword_5());
         c.setNoSpace().around(grammar.getPropertySizeAccess().getLeftParenthesisKeyword_1());
         c.setNoSpace().around(grammar.getPropertySizeAccess().getCommaKeyword_3());
         c.setNoSpace().before(grammar.getPropertySizeAccess().getRightParenthesisKeyword_5());

 
         // LIST Value are on own line
         c.setLinewrap().before(grammar.getValueRule());
 
         // CONTROL
         c.setNoLinewrap().around(grammar.getControlAccess().getBeginAssignment_2());
         c.setNoLinewrap().before(grammar.getControlAccess().getEndAssignment_5());
 
         // TESTPROC
         c.setLinewrap().before(grammar.getConditionalCapabilityCommandGroupAccess().getBeginAssignment_0());
         c.setNoLinewrap().after(grammar.getConditionalCapabilityCommandGroupAccess().getBeginAssignment_0());
         c.setNoLinewrap().around(grammar.getConditionalCapabilityCommandGroupAccess().getStringBeginAssignment_6());
         c.setNoLinewrap().around(grammar.getConditionalCapabilityCommandGroupAccess().getStringEndAssignment_8());
         c.setNoLinewrap().before(grammar.getConditionalCapabilityCommandGroupAccess().getEndAssignment_9());
         c.setLinewrap().after(grammar.getConditionalCapabilityCommandGroupAccess().getEndAssignment_9());
 
     }
 }
