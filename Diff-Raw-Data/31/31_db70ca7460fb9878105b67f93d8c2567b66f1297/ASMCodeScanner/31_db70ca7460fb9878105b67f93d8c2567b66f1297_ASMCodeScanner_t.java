 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
 
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
 
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */ 
 package org.lyllo.kickassplugin.editor;
 
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.RuleBasedScanner;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.swt.graphics.Device;
 import org.lyllo.kickassplugin.Activator;
 import org.lyllo.kickassplugin.Constants;
 import org.lyllo.kickassplugin.TextAttributeConverter;
 
 
 /**
  * RuleBasedScanner for the ASMEditor.
  * 
  * @author Andy Reek
  * @since 15.11.2005
  */
 public class ASMCodeScanner extends RuleBasedScanner implements IPropertyChangeListener, IRuleListener {
 
 	private Token instructionToken;
 
 	private Token segmentToken;
 
 	private Token functionToken;
 
 	private Token constantToken;
 
 	private Token classToken;
 
 	private ASMEditor editor;
 
 	private ITokenMatch lastMatch = new ITokenMatch("", 0, Token.UNDEFINED);
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param editor The underlying ASMEditor for the CodeScanner.
 	 */
 	public ASMCodeScanner(final ASMEditor editor) {
 		this.editor = editor;
 		ArrayList<IRule> rules = new ArrayList<IRule>();
 		createTokens(editor.getSite().getShell().getDisplay());
 
 		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
 
 		rules.add( createCaseSensitiveWordRuleMapping(ASMInstructionSet.getInstructions(), instructionToken) );
 		rules.add( createCaseSensitiveWordRuleMapping(ASMInstructionSet.getSegments(), segmentToken) );
 		rules.add( createCaseSensitiveWordRuleMapping(ASMInstructionSet.getFunctions(), functionToken) );
 		rules.add( createCaseSensitiveWordRuleMapping(ASMInstructionSet.getConstants(), constantToken) );
 		rules.add( createCaseSensitiveWordRuleMapping(ASMInstructionSet.getClasses(), classToken) );
 
 		IRule immediateRule = new IRule() {
 
 			public IToken evaluate(ICharacterScanner scanner){
 
 				if (lastMatch.getToken() != instructionToken && scanner.getColumn() < lastMatch.getColumn())
 					return Token.UNDEFINED;
 
 				int count = 0;
 				IToken token = Token.UNDEFINED;
 				count++;
 				if ('#' == scanner.read()){
 					token = instructionToken;	
 					tokenFound(new ITokenMatch("#", scanner.getColumn(), token));
 				} else {
 					for(int i=0; i < count; i++)
 						scanner.unread();
 				}
 
 				return token;
 			}
 		};
 
 		IRule immediateConstantRule = new IRule() {
 
 			public IToken evaluate(ICharacterScanner scanner){
 
 				if (lastMatch.getToken() != instructionToken || scanner.getColumn() < lastMatch.getColumn() || !"#".equals(lastMatch.getString()))
 					return Token.UNDEFINED;
 
 				IToken token = Token.UNDEFINED;
 				int c = 0;
 				int count = 0;
 				do {
 					c = scanner.read();
 					count++;
 				} while (c != ICharacterScanner.EOF && Character.isWhitespace(c));
 				
 				StringBuffer buf = new StringBuffer();
 				if (!Character.isWhitespace(c))
 					buf.append((char)c);
 				
 				while ((c=scanner.read()) != ICharacterScanner.EOF && !Character.isWhitespace(c)){
 					count++;
 					buf.append((char)c);
 				}
 
 				if (buf.length() != 0){
 					token = constantToken;
 					tokenFound(new ITokenMatch(buf.toString(), scanner.getColumn(), token));
 				} else {
 					for(int i=0; i < count; i++)
 						scanner.unread();
 
 				}
 
 				return token;
 			}
 		};
 
 		IRule labelRule = new IRule() {
 
 			public IToken evaluate(ICharacterScanner scanner) {
 				int count = 0;
 				IToken token = Token.UNDEFINED;
 				count++;
 				StringBuffer buf = new StringBuffer();
 				int c = 0;
 				if(Character.isJavaIdentifierStart((c=scanner.read()))){
 					buf.append((char)c);
 					while ((c=scanner.read()) != ICharacterScanner.EOF && Character.isJavaIdentifierPart(c)){
 						count++;
 						buf.append((char)c);
 					}
 					if (c == ':'){
 						token = segmentToken;
 						tokenFound(new ITokenMatch(buf.toString(), scanner.getColumn(), token));
 					}
 				}
 
 				if (token.equals(Token.UNDEFINED))
 					for(int i=0; i < count; i++)
 						scanner.unread();
 				return token;
 			}
 		};
 
 		IRule macroRule = new IRule() {
 
 			public IToken evaluate(ICharacterScanner scanner) {
 				IToken token = Token.UNDEFINED;
 				int count = 1;
 
 				if(scanner.read() == ':'){
 					StringBuffer buf = new StringBuffer();
 					int c = 0;
 					while ((c=scanner.read()) != ICharacterScanner.EOF && Character.isJavaIdentifierPart(c)){
 						count++;
 						buf.append((char)c);
 					}
 					if (Character.isWhitespace(c) || c =='(' || scanner.getColumn()==0){
 						token = segmentToken;
 						tokenFound(new ITokenMatch(buf.toString(), scanner.getColumn(), token));
 					}
 				}
 
 				if (token.equals(Token.UNDEFINED))
 					for(int i=0; i < count; i++)
 						scanner.unread();
 				return token;
 			}
 		};
 
 		rules.add(immediateRule);
 		rules.add(immediateConstantRule);
 		rules.add(labelRule);
 		rules.add(macroRule);
 		setRules(rules.toArray(new IRule[] {}));
 	}
 
 	private IRule createCaseSensitiveWordRuleMapping(
 			Map<String, String> map, Token token) {
		WordRuleCaseInsensitive rule = createWordRuleMapping(map, token, true);
 		
 		return rule;
 	}
 
	private WordRuleCaseInsensitive createWordRuleMapping(Map<String, String> map, IToken token, boolean caseSensitive) {
 		WordRuleCaseInsensitive wordRule = new WordRuleCaseInsensitive(this);
		wordRule.setCaseSensitive(caseSensitive);
		
 		if (map != null) {
 			for (String word : map.keySet()) {
 				wordRule.addWord(word, token);
 			}
 		}
 		return wordRule;
 	}
 
 	/**
 	 * Disposes the PropertyChangeListener from the PreferenceStore.
 	 */
 	public void dispose() {
 		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
 	}
 
 	/**
 	 * Create all Tokens.
 	 * 
 	 * @param device The device is needed for the color of the Tokens.
 	 */
 	private void createTokens(Device device) {
 		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
 
 		instructionToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION);
 		segmentToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_SEGMENT);
 		functionToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_FUNCTION);
 		classToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_CLASS);
 		constantToken = getToken(store, Constants.PREFERENCES_TEXTCOLOR_CONSTANT);
 
 	}
 
 	private static Token getToken(IPreferenceStore store, String key) {
 
 		return new Token(TextAttributeConverter
 				.preferenceDataToTextAttribute(store.getString(key)));
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void propertyChange(PropertyChangeEvent event) {
 		if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_INSTRUCTION)) {
 			instructionToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
 		} else if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_SEGMENT)) {
 			segmentToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
 		}   else if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_FUNCTION)) {
 			functionToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
 		}  else if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_CONSTANT)) {
 			constantToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
 		}  else if (event.getProperty().equals(Constants.PREFERENCES_TEXTCOLOR_CLASS)) {
 			classToken.setData(TextAttributeConverter.preferenceDataToTextAttribute((String) event.getNewValue()));
 		}
 
 		editor.refreshSourceViewer();
 	}
 
 	public void tokenFound(ITokenMatch match) {
 		this.lastMatch = match;
 	}
 }
