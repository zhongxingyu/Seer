 package com.zarkonnen.catengine;
 
 import com.zarkonnen.catengine.util.Clr;
 import com.zarkonnen.catengine.util.Pt;
 import com.zarkonnen.catengine.util.Rect;
 import java.util.HashMap;
 import java.util.Map;
 
 public class Draw {
 	Frame f;
 	Hooks hs = new Hooks();
 		
 	public Hooks getHooks() { return hs; }
 	
 	public Draw(Frame f) {
 		this.f = f;
 	}
 	
 	public Draw(Frame f, Hooks hs) {
 		this.f = f;
 		this.hs = hs;
 	}
 	
 	public void shift(double dx, double dy) { f.shift(dx, dy); }
 	public void scale(double xScale, double yScale) { f.scale(xScale, yScale); }
 	public void rotate(double angle) { f.rotate(angle); }
 	public void resetTransforms() { f.resetTransforms(); }
 	
 	public Draw blit(Img img, double x, double y) {
 		f.blit(img, null, 1, x, y, 0, 0, 0);
 		return this;
 	}
 	
 	public Draw blit(Img img, double x, double y, double angle) {
 		f.blit(img, null, 1, x, y, 0, 0, angle);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y) {
 		f.blit(img, c, 1, x, y, 0, 0, 0);
 		return this;
 	}
 	
 	public Draw blit(Img img, double x, double y, double width, double height) {
 		f.blit(img, null, 1, x, y, width, height, 0);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y, double width, double height) {
		f.blit(img, c, 1, x, y, width, height, 0);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y, double angle) {
 		f.blit(img, c, 1, x, y, 0, 0, angle);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y, double width, double height, double angle) {
 		f.blit(img, c, 1, x, y, width, height, angle);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double alpha, double x, double y, double width, double height, double angle) {
 		f.blit(img, c, alpha, x, y, width, height, angle);
 		return this;
 	}
 	
 	public Draw rect(Clr c, double x, double y, double width, double height) {
 		f.rect(c, x, y, width, height, 0);
 		return this;
 	}
 	
 	public Draw rect(Clr c, double x, double y, double width, double height, double angle) {
 		f.rect(c, x, y, width, height, angle);
 		return this;
 	}
 	
 	public Draw blit(Img img, double x, double y, Hook hook) {
 		blit(img, x, y);
 		hs.add(new Rect(x, y, img.srcWidth, img.srcHeight), hook);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y, Hook hook) {
 		blit(img, c, x, y);
 		hs.add(new Rect(x, y, img.srcWidth, img.srcHeight), hook);
 		return this;
 	}
 	
 	public Draw blit(Img img, double x, double y, double width, double height, Hook hook) {
 		blit(img, x, y, width, height);
 		hs.add(new Rect(x, y, width, height), hook);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double x, double y, double width, double height, Hook hook) {
 		blit(img, x, y, width, height);
 		hs.add(new Rect(x, y, width, height), hook);
 		return this;
 	}
 	
 	public Draw blit(Img img, Clr c, double alpha, double x, double y, double width, double height, Hook hook) {
 		blit(img, c, alpha, x, y, width, height);
 		hs.add(new Rect(x, y, width, height), hook);
 		return this;
 	}
 	
 	public Draw rect(Clr c, double x, double y, double width, double height, Hook hook) {
 		rect(c, x, y, width, height);
 		hs.add(new Rect(x, y, width, height), hook);
 		return this;
 	}
 	
 	public void addSymbols(HashMap<String, Img> symbols) {
 		this.symbols.putAll(symbols);
 	}
 	
 	public Draw text(String text, Fount f, double x, double y) {
 		return text(text, f, x, y, 10000, 10000, true, new HashMap<String, Hook>());
 	}
 	
 	public Draw text(String text, Fount f, double x, double y, int maxWidth) {
 		return text(text, f, x, y, maxWidth, 10000, true, new HashMap<String, Hook>());
 	}
 	
 	public Draw text(String text, Fount f, double x, double y, Map<String, Hook> hooks) {
 		return text(text, f, x, y, 10000, 10000, true, hooks);
 	}
 	
 	public Draw text(String text, Fount f, double x, double y, int maxWidth, Map<String, Hook> hooks) {
 		return text(text, f, x, y, maxWidth, 10000, true, hooks);
 	}
 	
 	public Pt textSize(String text, Fount f) {
 		return textSize(text, f, 10000, 10000, true);
 	}
 	
 	public Rect textSize(String text, Fount f, double x, double y) {
 		Pt ts = textSize(text, f);
 		return new Rect(x, y, ts.x, ts.y);
 	}
 	
 	public Rect textSize(String text, Fount f, double x, double y, int maxWidth) {
 		Pt ts = textSize(text, f, maxWidth, 10000, true);
 		return new Rect(x, y, ts.x, ts.y);
 	}
 	
 	public Draw hook(double x, double y, double width, double height, Hook h) {
 		hs.add(new Rect(x, y, width, height), h);
 		return this;
 	}
 	
 	static HashMap<String, Clr> knownColors = new HashMap<String, Clr>();
 	
 	HashMap<String, Img> symbols = new HashMap<String, Img>();
 	
 	public Draw text(String text, Fount fount, double x, double y, int maxWidth, int maxHeight, boolean allowCommands, Map<String, Hook> hooksForStrings) {
 		for (Map.Entry<String, Hook> sub : hooksForStrings.entrySet()) {
 			text = text.replace(sub.getKey(), "[!" + sub.getKey() + "]" + sub.getKey() + "[!]");
 		}
 		int cols = maxWidth / fount.displayWidth;
 		int rows = maxHeight / fount.lineHeight;
 		int c = 0;
 		int r = 0;
 		int n = 0;
 		Clr bgC = null;
 		Clr tintC = null;
 		Clr defaultTintC = null;
 		
 		String hookText = null;
 		double hookX = 0, hookY = 0;
 		
 		while (n < text.length()) {
 			/*if (c >= cols) {
 				c = 0;
 				r++;
 			}*/
 			// Look ahead
 			int nextSpace = n;
 			int realNextSpace = n;
 			boolean inSquareBrackets = false;
 			boolean inCurlyBrackets = false;
 			while (nextSpace < text.length()) {
 				if (text.charAt(nextSpace) == ' ' || text.charAt(nextSpace) == '\n') {
 					break;
 				}
 				if (text.charAt(nextSpace) == '[') {
 					inSquareBrackets = true;
 				}
 				if (text.charAt(nextSpace) == ']') {
 					inSquareBrackets = false;
 				}
 				if (text.charAt(nextSpace) == '{') {
 					inCurlyBrackets = true;
 					realNextSpace += 2;
 				}
 				if (text.charAt(nextSpace) == '}') {
 					inCurlyBrackets = false;
 				}
 				if (!inCurlyBrackets && !inSquareBrackets) {
 					realNextSpace++;
 				}
 				nextSpace++;
 			}
 			boolean justIncrementedRow = false;
 			if (realNextSpace - n + c >= cols && realNextSpace != text.length()) {
 				c = 0;
 				r++;
 				justIncrementedRow = true;
 			}
 			if (r >= rows) {
 				return this;
 			}
 			if (text.charAt(n) == '\\' && allowCommands) {
 				n++;
 			} else {
 				if (text.charAt(n) == '\n') {
 					c = 0;
 					if (!justIncrementedRow) { r++; }
 					n++;
 					continue;
 				}
 				if (text.charAt(n) == '{' && allowCommands) {
 					int n2 = n + 1;
 					while (text.charAt(n2) != '}') { n2++; }
 					String nameS = text.substring(n + 1, n2);
 					String sym = null;
 					Clr symC = null;
 					if (nameS.startsWith("[") && nameS.contains("]")) {
 						String tintN = nameS.substring(1, nameS.indexOf("]"));
 						if (!tintN.isEmpty()) {
 							symC = knownColors.get(tintN);
 							if (symC == null) {
 								symC = Clr.fromHex(tintN);
 								if (symC != null) {
 									knownColors.put(tintN, symC);
 								}
 							}
 							if (symC == null) {
 								symC = Clr.getNamedColor(tintN);
 								if (symC != null) {
 									knownColors.put(tintN, symC);
 								}
 							}
 							if (symC != null) {
 								sym = nameS.substring(nameS.indexOf("]") + 1);
 							}
 						}
 					}
 					
 					if (sym == null) { sym = nameS; }
 					int overhang = 0; // qqDPS
 					Img symImg = symbols.get(sym);
 					blit(symImg, symC, 1, x + (c) * fount.displayWidth, y + r * fount.lineHeight + overhang);
 					Rect imgSz = symImg == null ? null : new Rect(x + (c) * fount.displayWidth, y + r * fount.lineHeight + overhang, symImg.srcWidth, symImg.srcHeight);
 					n = n2 + 1;
 					if (imgSz != null) {
 						hs.add(imgSz, hooksForStrings.get("{" + sym + "}"));
 						c += (imgSz.width / fount.displayWidth) + (imgSz.width % fount.displayWidth == 0 ? 0 : 1);
 					}
 					continue;
 				}
 				if (text.charAt(n) == '[' && allowCommands) {
 					int n2 = n + 1;
 					while (text.charAt(n2) != ']') { n2++; }
 					String tintN = text.substring(n + 1, n2);
 					if (tintN.startsWith("!")) {
 						if (tintN.equals("!")) {
 							if (hookText != null) {
 								//System.out.println(text);
 								//System.out.println(new Rect(hookX, hookY, x + (c) * fount.displayWidth - hookX, y + r * fount.height + fount.height - hookY));
 								hs.add(new Rect(hookX, hookY, x + (c) * fount.displayWidth - hookX, y + r * fount.lineHeight + fount.lineHeight - hookY), hooksForStrings.get(hookText));
 								hookText = null;
 							}
 						} else {
 							hookText = tintN.substring(1);
 							hookX = x + (c) * fount.displayWidth;
 							hookY = y + r * fount.lineHeight;
 						}
 						
 						n = n2 + 1;
 						continue;
 					}
 					
 					boolean def = false;
 					boolean bg = false;
 					if (tintN.startsWith("bg=")) {
 						bg = true;
 						tintN = tintN.substring(3);
 					}
 					if (tintN.startsWith("default=")) {
 						def = true;
 						tintN = tintN.substring("default=".length());
 					}
 					Clr newC = null;
 					if (!tintN.isEmpty()) {
 						newC = knownColors.get(tintN);
 						if (newC == null) {
 							newC = Clr.fromHex(tintN);
 							if (newC != null) {
 								knownColors.put(tintN, newC);
 							}
 						}
 						if (newC == null) {
 							newC = Clr.getNamedColor(tintN);
 							if (newC != null) {
 								knownColors.put(tintN, newC);
 							}
 						}
 					} else if (!bg) {
 						newC = defaultTintC;
 					}
 					if (bg) {
 						bgC = newC;
 					} else if (def) {
 						defaultTintC = newC;
 					} else {
 						tintC = newC;
 					}
 					n = n2 + 1;
 					continue;
 				}
 			}
 			//int codePt = Character.codePointAt(cs, n);
 			if (bgC != null) {
 				f.rect(bgC, x + c * fount.displayWidth - (n == 0 ? -2 : 1), y + r * fount.lineHeight - 2,
 					fount.displayWidth, fount.lineHeight, 0);
 			}
 			/*f.blit(
 					fount.img + codePt,
 					tintC,
 					x + c * fount.displayWidth, y + r * fount.lineHeight,
 					0,
 					0,
 					0
 					, 0, 0, 0, 0, false);*/
 			blit(fount.get(text.charAt(n)), tintC, x + c * fount.displayWidth, y + r * fount.lineHeight);
 			c++;
 			n++;
 		}
 		return this;
 	}
 	
 	public Pt textSize(String text, Fount fount, int maxWidth, int maxHeight, boolean allowCommands) {
 		int widthReached = 0;
 		int heightReached = 0;
 		
 		int cols = maxWidth / fount.displayWidth;
 		int rows = maxHeight / fount.lineHeight;
 		int c = 0;
 		int r = 0;
 		int n = 0;
 		char[] cs = text.toCharArray();
 		while (n < cs.length) {
 			/*if (c >= cols) {
 				c = 0;
 				r++;
 			}*/
 			// Look ahead
 			int nextSpace = n;
 			int realNextSpace = n;
 			boolean inSquareBrackets = false;
 			boolean inCurlyBrackets = false;
 			while (nextSpace < cs.length) {
 				if (cs[nextSpace] == ' ' || cs[nextSpace] == '\n') {
 					break;
 				}
 				if (cs[nextSpace] == '[') {
 					inSquareBrackets = true;
 				}
 				if (cs[nextSpace] == ']') {
 					inSquareBrackets = false;
 				}
 				if (cs[nextSpace] == '{') {
 					inCurlyBrackets = true;
 					realNextSpace += 2;
 				}
 				if (cs[nextSpace] == '}') {
 					inCurlyBrackets = false;
 				}
 				if (!inCurlyBrackets && !inSquareBrackets) {
 					realNextSpace++;
 				}
 				nextSpace++;
 			}
 			boolean justIncrementedRow = false;
 			if (realNextSpace - n + c >= cols && realNextSpace != cs.length) {
 				c = 0;
 				r++;
 				justIncrementedRow = true;
 			}
 			if (r >= rows) {
 				return new Pt(widthReached + 1, heightReached + 1);
 			}
 			if (cs[n] == '\\' && allowCommands) {
 				n++;
 			} else {
 				if (cs[n] == '\n') {
 					c = 0;
 					if (!justIncrementedRow) { r++; }
 					n++;
 					continue;
 				}
 				if (cs[n] == '{' && allowCommands) {
 					int n2 = n + 1;
 					while (cs[n2] != '}') { n2++; }
 					n = n2 + 1;
 					c += 1; // qqDPS
 					continue;
 				}
 				if (cs[n] == '[' && allowCommands) {
 					int n2 = n + 1;
 					while (cs[n2] != ']') { n2++; }
 					n = n2 + 1;
 					continue;
 				}
 			}
 			widthReached = Math.max(widthReached, c * fount.displayWidth + fount.displayWidth);
 			heightReached = Math.max(heightReached, r * fount.lineHeight + fount.lineHeight);
 			c++;
 			n++;
 		}
 		return new Pt(widthReached + 1, heightReached + 1);
 	}
 }
