 /*
  	org.manalith.ircbot.plugin.calc/CalcRunner.java
  	ManalithBot - An open source IRC bot based on the PircBot Framework.
  	Copyright (C) 2011  Seong-ho, Cho <darkcircle.0426@gmail.com>
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.manalith.ircbot.plugin.calc;
 
 import org.manalith.ircbot.plugin.calc.exceptions.EmptyTokenStreamException;
 import org.manalith.ircbot.plugin.calc.exceptions.TokenAnalysisException;
 
 public class CalcRunner {
 	public static String run(String expr) {
 
 		String result = "";
 		// Token analysis phase
 		TokenArray tArray = new TokenArray();
 		CalcTokenAnalyzer cta = new CalcTokenAnalyzer(expr);
 
 		try {
 			tArray = cta.getTokenArray();
 		} catch (EmptyTokenStreamException ets) {
			return "입력 문자열이 비어있습니다."
 		} catch (TokenAnalysisException e) {
 			result = " === 해석 오류! === " + e.getMessage();
 			return result;
 		}
 
 		ParseTreeUnit ptu = null;
 
 		try {
 			// Parse tree generation phase
 			ptu = CalcParseTreeGenerator.generateParseTree(tArray);
 
 			// Computation phase
 			if (ptu.getResultType().equals("Integer"))
 				result = " => " + ptu.getIntFpResult();
 			else
 				result = " => " + ptu.getFpResult();
 		} catch (Exception e) {
 			result = "Computation Error! : " + e.getMessage();
 			return result;
 		}
 
 		return result;
 	}
 }
