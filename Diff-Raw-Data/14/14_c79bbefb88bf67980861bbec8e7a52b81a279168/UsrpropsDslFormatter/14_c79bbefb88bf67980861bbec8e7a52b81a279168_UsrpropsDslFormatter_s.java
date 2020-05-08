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
      */
     @Override
     protected void configureFormatting(FormattingConfig c) {
         UsrpropsDslGrammarAccess grammar = (UsrpropsDslGrammarAccess) getGrammarAccess();
 
         c.setIndentationIncrement().after(grammar.getBeginRule());
         c.setIndentationDecrement().before(grammar.getEndRule());
         c.setLinewrap().before(grammar.getBeginRule());
         c.setLinewrap().after(grammar.getBeginRule());
         c.setLinewrap().before(grammar.getEndRule());
         c.setLinewrap().after(grammar.getEndRule());
 
         c.setLinewrap().before(grammar.getIncludeRule());
         c.setLinewrap(2).before(grammar.getListRule());
         c.setLinewrap(2).before(grammar.getDiagramRule());
         c.setLinewrap(2).before(grammar.getSymbolRule());
         c.setLinewrap(2).before(grammar.getDefinitionRule());
 
         c.setLinewrap().after(grammar.getChapterRule());

         c.setNoLinewrap().before(grammar.getPropertyAccess().getBeginAssignment_2());
         c.setNoLinewrap().after(grammar.getPropertyAccess().getBeginAssignment_2());
         c.setNoLinewrap().before(grammar.getEditRule());
         c.setNoLinewrap().after(grammar.getEditRule());
         c.setNoLinewrap().before(grammar.getPropertyAccess().getOptionsPropertyOptionParserRuleCall_5_0());
         c.setNoLinewrap().after(grammar.getPropertyAccess().getOptionsPropertyOptionParserRuleCall_5_0());
         c.setNoLinewrap().before(grammar.getDisplayAccess().getBeginAssignment_1());
         c.setNoLinewrap().after(grammar.getDisplayAccess().getBeginAssignment_1());
         c.setNoLinewrap().before(grammar.getDisplayAccess().getEndAssignment_6());
         c.setNoLinewrap().before(grammar.getPropertyAccess().getEndAssignment_6());
 
         c.setLinewrap().after(grammar.getListAccess().getValuesAssignment_3_1());
 
     }
 }
