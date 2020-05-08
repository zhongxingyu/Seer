 /*
  * utils - HtmlFilter.java - Copyright © 2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.template;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Filters HTML by replacing all characters that match a defined HTML entity by
  * that entity. Unknown characters that are outside of the US-ASCII range (0 to
  * 127) are encoded using the {@code &amp;#1234;} syntax.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class HtmlFilter implements Filter {
 
 	/** Map of defined HTML entities. */
 	private static final Map<Character, String> htmlEntities = new HashMap<Character, String>();
 
 	static {
 		htmlEntities.put('Â', "Acirc");
 		htmlEntities.put('â', "acirc");
 		htmlEntities.put('´', "acute");
 		htmlEntities.put('Æ', "AElig");
 		htmlEntities.put('æ', "aelig");
 		htmlEntities.put('À', "Agrave");
 		htmlEntities.put('à', "agrave");
 		htmlEntities.put('ℵ', "alefsym");
 		htmlEntities.put('Α', "alpha");
 		htmlEntities.put('α', "alpha");
 		htmlEntities.put('&', "amp");
 		htmlEntities.put('∧', "and");
 		htmlEntities.put('∠', "ang");
 		htmlEntities.put('\'', "apos");
 		htmlEntities.put('Å', "Aring");
 		htmlEntities.put('å', "aring");
 		htmlEntities.put('≈', "asymp");
 		htmlEntities.put('Ã', "Atilde");
 		htmlEntities.put('ã', "atilde");
 		htmlEntities.put('Ä', "Auml");
 		htmlEntities.put('ä', "auml");
 		htmlEntities.put('„', "bdquo");
 		htmlEntities.put('Β', "Beta");
 		htmlEntities.put('β', "beta");
 		htmlEntities.put('¦', "brvbar");
 		htmlEntities.put('•', "bull");
 		htmlEntities.put('∩', "cap");
 		htmlEntities.put('Ç', "Ccedil");
 		htmlEntities.put('ç', "ccedil");
 		htmlEntities.put('¸', "cedil");
 		htmlEntities.put('¢', "cent");
 		htmlEntities.put('Χ', "Chi");
 		htmlEntities.put('χ', "chi");
 		htmlEntities.put('ˆ', "circ");
 		htmlEntities.put('♣', "clubs");
 		htmlEntities.put('≅', "cong");
 		htmlEntities.put('©', "copy");
 		htmlEntities.put('↵', "crarr");
 		htmlEntities.put('∪', "cup");
 		htmlEntities.put('¤', "curren");
 		htmlEntities.put('‡', "Dagger");
 		htmlEntities.put('†', "dagger");
 		htmlEntities.put('⇓', "dArr");
 		htmlEntities.put('↓', "darr");
 		htmlEntities.put('°', "deg");
 		htmlEntities.put('Δ', "Delta");
 		htmlEntities.put('δ', "delta");
 		htmlEntities.put('♦', "diams");
 		htmlEntities.put('÷', "divide");
 		htmlEntities.put('É', "Eacute");
 		htmlEntities.put('é', "eacute");
 		htmlEntities.put('Ê', "Ecirc");
 		htmlEntities.put('ê', "ecirc");
 		htmlEntities.put('È', "Egrave");
 		htmlEntities.put('è', "egrave");
 		htmlEntities.put('∅', "empty");
 		htmlEntities.put('\u2003', "emsp");
 		htmlEntities.put('\u2002', "ensp");
 		htmlEntities.put('Ε', "Epsilon");
 		htmlEntities.put('ε', "epsilon");
 		htmlEntities.put('≡', "equiv");
 		htmlEntities.put('Η', "Eta");
 		htmlEntities.put('η', "eta");
 		htmlEntities.put('Ð', "ETH");
 		htmlEntities.put('ð', "eth");
 		htmlEntities.put('Ë', "Euml");
 		htmlEntities.put('ë', "euml");
 		htmlEntities.put('€', "euro");
 		htmlEntities.put('∃', "exist");
 		htmlEntities.put('ƒ', "fnof");
 		htmlEntities.put('∀', "forall");
 		htmlEntities.put('½', "frac12");
 		htmlEntities.put('¼', "frac14");
 		htmlEntities.put('¾', "frac34");
 		htmlEntities.put('⁄', "frasl");
 		htmlEntities.put('Γ', "Gamma");
 		htmlEntities.put('γ', "gamma");
 		htmlEntities.put('≥', "ge");
 		htmlEntities.put('>', "gt");
 		htmlEntities.put('⇔', "hArr");
 		htmlEntities.put('↔', "harr");
 		htmlEntities.put('♥', "hearts");
 		htmlEntities.put('…', "hellip");
 		htmlEntities.put('Í', "Iacute");
 		htmlEntities.put('í', "iacute");
 		htmlEntities.put('Î', "Icirc");
 		htmlEntities.put('î', "icirc");
 		htmlEntities.put('¡', "iexcl");
 		htmlEntities.put('Ì', "Igrave");
 		htmlEntities.put('ì', "igrave");
 		htmlEntities.put('ℑ', "image");
 		htmlEntities.put('∞', "infin");
 		htmlEntities.put('∫', "int");
 		htmlEntities.put('Ι', "Iota");
 		htmlEntities.put('ι', "iota");
 		htmlEntities.put('¿', "iquest");
 		htmlEntities.put('∈', "isin");
 		htmlEntities.put('Ï', "Iuml");
 		htmlEntities.put('ï', "iuml");
 		htmlEntities.put('Κ', "Kappa");
 		htmlEntities.put('κ', "kappa");
 		htmlEntities.put('Λ', "Lambda");
 		htmlEntities.put('λ', "lambda");
 		htmlEntities.put('〈', "lang");
 		htmlEntities.put('«', "laquo");
 		htmlEntities.put('⇐', "lArr");
 		htmlEntities.put('←', "larr");
 		htmlEntities.put('⌈', "lceil");
 		htmlEntities.put('“', "ldquo");
 		htmlEntities.put('≤', "le");
 		htmlEntities.put('⌊', "lfloor");
 		htmlEntities.put('∗', "lowast");
 		htmlEntities.put('◊', "loz");
 		htmlEntities.put('\u200e', "lrm");
 		htmlEntities.put('‹', "lsaquo");
 		htmlEntities.put('‘', "lsquo");
 		htmlEntities.put('<', "lt");
 		htmlEntities.put('¯', "macr");
 		htmlEntities.put('—', "mdash");
 		htmlEntities.put('µ', "micro");
 		htmlEntities.put('·', "middot");
 		htmlEntities.put('−', "minus");
 		htmlEntities.put('Μ', "Mu");
 		htmlEntities.put('μ', "mu");
 		htmlEntities.put('∇', "nabla");
 		htmlEntities.put('\u00a0', "nbsp");
 		htmlEntities.put('–', "ndash");
 		htmlEntities.put('≠', "ne");
 		htmlEntities.put('∋', "ni");
 		htmlEntities.put('¬', "not");
 		htmlEntities.put('∉', "notin");
 		htmlEntities.put('⊄', "nsub");
 		htmlEntities.put('Ñ', "Ntilde");
 		htmlEntities.put('ñ', "ntilde");
 		htmlEntities.put('Ν', "Nu");
 		htmlEntities.put('ν', "nu");
 		htmlEntities.put('Ó', "Oacute");
 		htmlEntities.put('ó', "oacute");
 		htmlEntities.put('Ô', "Ocirc");
 		htmlEntities.put('ô', "ocirc");
 		htmlEntities.put('Œ', "OElig");
 		htmlEntities.put('œ', "oelig");
 		htmlEntities.put('Ò', "Ograve");
 		htmlEntities.put('ò', "ograve");
 		htmlEntities.put('‾', "oline");
 		htmlEntities.put('Ω', "Omega");
 		htmlEntities.put('ω', "omega");
 		htmlEntities.put('Ο', "Omicron");
 		htmlEntities.put('ο', "omicron");
 		htmlEntities.put('⊕', "oplus");
 		htmlEntities.put('∨', "or");
 		htmlEntities.put('ª', "ordf");
 		htmlEntities.put('º', "ordm");
 		htmlEntities.put('Ø', "Oslash");
 		htmlEntities.put('ø', "oslash");
 		htmlEntities.put('Õ', "Otilde");
 		htmlEntities.put('õ', "otilde");
 		htmlEntities.put('⊗', "otimes");
 		htmlEntities.put('Ö', "Ouml");
 		htmlEntities.put('ö', "ouml");
 		htmlEntities.put('¶', "para");
 		htmlEntities.put('∂', "part");
 		htmlEntities.put('‰', "permil");
 		htmlEntities.put('⊥', "perp");
 		htmlEntities.put('Φ', "Phi");
 		htmlEntities.put('φ', "phi");
 		htmlEntities.put('Π', "pi");
 		htmlEntities.put('π', "pi");
 		htmlEntities.put('ϖ', "piv");
 		htmlEntities.put('±', "plusmn");
 		htmlEntities.put('£', "pound");
 		htmlEntities.put('″', "Prime");
 		htmlEntities.put('′', "prime");
 		htmlEntities.put('∏', "prod");
 		htmlEntities.put('∝', "prop");
 		htmlEntities.put('Ψ', "Psi");
 		htmlEntities.put('ψ', "psi");
 		htmlEntities.put('"', "quot");
 		htmlEntities.put('√', "radic");
 		htmlEntities.put('〉', "rang");
 		htmlEntities.put('»', "raquo");
 		htmlEntities.put('⇒', "rArr");
 		htmlEntities.put('→', "rarr");
 		htmlEntities.put('⌉', "rceil");
 		htmlEntities.put('”', "rdquo");
 		htmlEntities.put('ℜ', "real");
 		htmlEntities.put('®', "reg");
 		htmlEntities.put('⌋', "rfloor");
 		htmlEntities.put('Ρ', "Rho");
 		htmlEntities.put('ρ', "rho");
 		htmlEntities.put('\u200f', "rlm");
 		htmlEntities.put('›', "rsaquo");
 		htmlEntities.put('’', "rsquo");
 		htmlEntities.put('‚', "sbquo");
 		htmlEntities.put('Š', "Scaron");
 		htmlEntities.put('š', "scaron");
 		htmlEntities.put('⋅', "sdot");
 		htmlEntities.put('§', "sect");
 		htmlEntities.put('\u00ad', "shy");
 		htmlEntities.put('Σ', "Sigma");
 		htmlEntities.put('σ', "sigma");
 		htmlEntities.put('ς', "sigmaf");
 		htmlEntities.put('∼', "sim");
 		htmlEntities.put('♠', "spades");
 		htmlEntities.put('⊂', "sub");
 		htmlEntities.put('⊆', "sube");
 		htmlEntities.put('∑', "sum");
 		htmlEntities.put('⊃', "sup");
 		htmlEntities.put('¹', "sup1");
 		htmlEntities.put('²', "sup2");
 		htmlEntities.put('³', "sup3");
 		htmlEntities.put('⊇', "supe");
 		htmlEntities.put('ß', "szlig");
 		htmlEntities.put('Τ', "Tau");
 		htmlEntities.put('τ', "tau");
 		htmlEntities.put('∴', "there4");
 		htmlEntities.put('Θ', "Theta");
 		htmlEntities.put('θ', "theta");
 		htmlEntities.put('ϑ', "thetasym");
 		htmlEntities.put('\u2009', "thinsp");
 		htmlEntities.put('Þ', "THORN");
 		htmlEntities.put('þ', "thorn");
 		htmlEntities.put('˜', "tilde");
 		htmlEntities.put('×', "times");
 		htmlEntities.put('™', "trade");
 		htmlEntities.put('Ú', "Uacute");
 		htmlEntities.put('ú', "uacute");
 		htmlEntities.put('⇑', "hArr");
 		htmlEntities.put('↑', "harr");
 		htmlEntities.put('Û', "Ucirc");
 		htmlEntities.put('û', "ucirc");
 		htmlEntities.put('Ù', "Ugrave");
 		htmlEntities.put('ù', "ugrave");
 		htmlEntities.put('¨', "uml");
 		htmlEntities.put('ϒ', "upsih");
 		htmlEntities.put('Υ', "Upsilon");
 		htmlEntities.put('υ', "upsilon");
 		htmlEntities.put('Ü', "Uuml");
 		htmlEntities.put('ü', "uuml");
 		htmlEntities.put('℘', "weierp");
 		htmlEntities.put('Ξ', "Xi");
 		htmlEntities.put('ξ', "xi");
 		htmlEntities.put('Ý', "Yacute");
 		htmlEntities.put('ý', "yacute");
 		htmlEntities.put('¥', "yen");
 		htmlEntities.put('Ÿ', "Yuml");
 		htmlEntities.put('ÿ', "yuml");
 		htmlEntities.put('Ζ', "Zeta");
 		htmlEntities.put('ζ', "zeta");
 		htmlEntities.put('\u200d', "zwj");
 		htmlEntities.put('\u200c', "zwnj");
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public String format(DataProvider dataProvider, Object data, Map<String, String> parameters) {
 		StringBuilder htmlOutput = new StringBuilder();
 		for (char c : (data != null) ? String.valueOf(data).toCharArray() : new char[0]) {
 			if (htmlEntities.containsKey(c)) {
 				htmlOutput.append('&').append(htmlEntities.get(c)).append(';');
 				continue;
 			}
 			if (c > 127) {
				htmlOutput.append("&#").append(c);
 				continue;
 			}
 			htmlOutput.append(c);
 		}
 		return htmlOutput.toString();
 	}
 
 }
