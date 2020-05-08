 /*
  * This file is part of jHaushalt.
  * jHaushalt is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  * jHaushalt is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License
  * along with jHaushalt; if not, see <http://www.gnu.org/licenses/>.
  * (C)opyright 2002-2010 Dr. Lars H. Hahn
  */
 
 package haushalt.gui;
 
 import haushalt.daten.Euro;
 
 import javax.swing.text.AttributeSet;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.PlainDocument;
 
 /**
  * Textfeld um Euro-Werte zu editieren.
  * 0€ werden als leeres Feld angezeigt.
  * Bei der Eingabe werden keine Buchstaben akzeptiert.
  * 
  * @author Dr. Lars H. Hahn
  * @version 2.6/2009.07.28
  */
 
 /*
  * 2009.07.28 Eingabe des Plus-Zeichens ('+') erlaubt
  * 2007.02.12 Hinzufügen der Methode getValue()
  * 2004.08.22 Version 2.0
  */
 
 public class EuroField extends DeleteableTextField {
 
 	private static final long serialVersionUID = 1L;
 
 	public EuroField() {
 		this(new Euro());
 	}
 
 	public EuroField(final Euro euro) {
 		super("" + euro, 8);
 	}
 
 	public Euro getValue() {
 		return new Euro(getText());
 	}
 
 	@Override
 	public void setText(String text) {
 		if (text.equals("" + Euro.NULL_EURO)) {
 			text = "";
 		}
 		super.setText(text);
 	}
 
 	@Override
 	protected Document createDefaultModel() {
 		return new EuroDocument();
 	}
 
 	private static class EuroDocument extends PlainDocument {
 
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void insertString(final int offs, final String str, final AttributeSet a) throws BadLocationException {
 			final char[] source = str.toCharArray();
 			final int anzChar = source.length;
 			final char[] result = new char[anzChar];
 			int j = 0;
 
 			for (int i = 0; i < anzChar; i++) {
				if (Character.isDigit(source[i]) || (source[i] == '€') || (source[i] == '-') ||
 						(source[i] == '+') || (source[i] == ' ') || (source[i] == '.') || (source[i] == ',')) {
 					result[j++] = source[i];
 				}
 			}
 			super.insertString(offs, new String(result, 0, j), a);
 		}
 	}
 }
