 package applets.Termumformungen$in$der$Technik_03_Logistik;
 
import sun.tools.jstat.Operator;

 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 
 public class VTMeta extends VTContainer  {
 	
 	private final Applet applet;
 	private VisualThing[] extern; // extern things for \object
 	private Runnable updater; // used by selector and text
 	private List<Utils.Var> vars = new LinkedList<Utils.Var>();
 	
 	public VTMeta(Applet applet, String name, int stepX, int stepY, String content, VisualThing[] extern, Runnable updater) {
 		super((name == null || name.isEmpty()) ? ("__VTMeta") : name, stepX, stepY, null);
 		this.applet = applet;
 		this.extern = extern;
 		this.updater = updater;
 		
 		things = getThingsByContentStr(content);
 	}
 
 	public VTMeta(Applet applet, int stepX, int stepY, String content, VisualThing[] extern, Runnable updater) {
 		this(applet, null, stepX, stepY, content, extern, updater);
 	}
 
 	public Utils.Var getVar(String name) {
 		for(Utils.Var var : vars) {
 			if(var.name.compareTo(name) == 0)
 				return var;
 		}
 		return null;
 	}
 	
 	public Utils.Var getVar(String name, boolean createNewIfNotThere) {
 		Utils.Var var = getVar(name);
 		if(var == null && createNewIfNotThere) {
 			var = new Utils.Var();
 			var.name = name;
 			vars.add(var);
 		}
 		return var;
 	}
 	
 	public String getVarValue(String name) {
 		Utils.Var var = getVar(name);
 		if(var == null) return null;
 		return var.value;
 	}
 	
 	public void setVarValue(String name, String value) {
 		Utils.Var v = getVar(name, true);
 		v.value = value;
 	}
 	
 	private VisualThing getExternThing(String name) {
 		if(extern == null) return null;
 		for(VisualThing anExtern : extern) {
 			if(anExtern.getComponent().getName().compareTo(name) == 0)
 				return anExtern;
 		}
 		System.err.println("getExternThing: '" + name + "' not found");
 		return null;
 	}
 	
 	public VisualThing createSimpleContainer(List<VisualThing> thing_list) {
 		return new VTContainer(0, 0, getArrayByThingList(thing_list));
 	}
 	
 	public VisualThing[] getArrayByThingList(List<VisualThing> thing_list) {
 		VisualThing[] things = new VisualThing[thing_list.size()];
 		for(int i = 0; i < things.length; i++)
 			things[i] = thing_list.get(i);
 		return things;
 	}
 	
 	public VisualThing[] getThingsByContentStr(String content) {
 		Utils.Ref<Integer> endpos = new Utils.Ref<Integer>(0);
 		List<VisualThing> things = getThingsByContentStr(content, 0, endpos);
 		if(endpos.value <= content.length())
 			System.err.println("getThingsByContentStr: not parsed until end");
 		/*for(VisualThing thing : things) {
 			// debug
 			System.out.println(thing.getDebugString());
 		}*/
 		return getArrayByThingList(things);
 	}
 	
 	protected String getTextOutOfVisualThing(VisualThing thing) {
 		if(thing == null)
 			return "";
 		else if(thing instanceof VTLabel) {
 			return ((VTLabel)thing).getText();
 		} else if(thing instanceof VTContainer) {
 			VTContainer cont = (VTContainer) thing;
 			String ret = "";
 			for(int i = 0; i < cont.things.length; i++)
 				ret += getTextOutOfVisualThing(cont.things[i]);
 			return ret;
 		} else {
 			return "";
 		}
 	}
 	
 	protected VisualThing resetAllFonts(VisualThing base, String fontName) {
 		if(base == null) return null;
 		else if(base instanceof VTContainer) {
 			VTContainer con = (VTContainer) base;
 			for(int i = 0; i < con.getThings().length; i++) {
 				con.getThings()[i] = resetAllFonts(con.getThings()[i], fontName);
 			}
 		}
 		else if(base instanceof VTLabel) {
 			((VTLabel) base).setFontName(fontName);
 		}
 		return base;
 	}
 	
 	protected VisualThing resetAllColors(VisualThing base, Color color) {
 		if(base == null) return null;
 		else if(base instanceof VTContainer) {
 			VTContainer con = (VTContainer) base;
 			for(int i = 0; i < con.getThings().length; i++)
 				con.getThings()[i] = resetAllColors(con.getThings()[i], color);
 		}
 		else if(base instanceof VTMatrix.VTArc) {
 			((VTMatrix.VTArc) base).color = color;
 		}
 		else if(base instanceof VTLabel) {
 			((VTLabel) base).setColor(color);
 		}
 		return base;
 	}
 	
 	@SuppressWarnings({"ConstantConditions"})
 	protected VisualThing handleTag(String tagname, VisualThing baseparam, String extparam, VisualThing lowerparam, VisualThing upperparam) {
 		if(tagname.compareTo("frac") == 0) {
 			return new VTFrac(0, 0, upperparam, lowerparam);
 		}
 		else if(tagname.compareTo("matrix") == 0) {
 			return new VTMatrix(0, 0, baseparam);
 		}
 		else if(tagname.compareTo("lim") == 0) {
 			return Applet.newVTLimes(0, 0, lowerparam);
 		}
 		else if(tagname.compareTo("text") == 0) {
 			Runnable action = updater;
 			String name = getExtParamVar(extparam, "name", true);
 			String widthStr = getExtParamVar(extparam, "width");
 			if(widthStr.length() == 0)
 				return new VTText(name, 0, 0, action);
 			else
 				return new VTText(name, 0, 0, Integer.parseInt(widthStr), action);
 		}
 		else if(tagname.compareTo("button") == 0) {
 			int index = (int) Applet.parseNum(getExtParamVar(extparam, "index"));
 			if(index == -666) index = 1;
 			String text = getTextOutOfVisualThing(baseparam);
 			Runnable action = null;
 			if(getExtParamVar(extparam, "type").compareToIgnoreCase("help") == 0) {
 				action = this.applet.createHelpButtonListener(index);
 				if(text.equals("")) text = "Hilfe";
 			}
 			else if(getExtParamVar(extparam, "type").compareToIgnoreCase("check") == 0) {
 				String source = getExtParamVar(extparam, "source");
 				if(source.length() > 0 && getExternThing(source) instanceof Applet.CorrectCheck) {
 					action = this.applet.createCheckButtonListener(index, (Applet.CorrectCheck)getExternThing(source));
 				}
 				else
 					action = this.applet.createCheckButtonListener(index);
 				if(text.equals("")) text = "überprüfen";
 			}
 			else if(getExtParamVar(extparam, "type").compareToIgnoreCase("next") == 0) {
 				final int fixed_index = index;
 				action = new Runnable() {
 					public void run() { applet.content.next(fixed_index); }
 				};
 			}
 			String name = getExtParamVar(extparam, "name");
 			return new VTButton(name, text, 0, 0, action);
 		}
 		else if(tagname.compareTo("label") == 0) {
 			String name = getExtParamVar(extparam, "name", true);
 			return new VTLabel(name, getTextOutOfVisualThing(baseparam), 0, 0);
 		}
 		else if(tagname.compareTo("selector") == 0) {
 			Runnable action = updater;
 			String[] items = getStringArrayFromString(getTextOutOfVisualThing(baseparam));
 			String name = getExtParamVar(extparam, "name", true);
 			return new VTSelector(name, items, 0, 0, action);
 		}
 		else if(tagname.compareTo("container") == 0) {
 			return new VTContainer(getExtParamVar(extparam, "name", true), 0, 0, new VisualThing[] { baseparam });
 			//return baseparam;
 			//return new VTContainer(0, 0, new VisualThing[] { baseparam });
 		}
 		else if(tagname.compareTo("hint") == 0) {
 			return new VTHintContainer(0, 0, new VisualThing[] { baseparam });
 		}
 		else if(tagname.compareTo("equinput") == 0) {
 			VTEquationsInput equationInput = new VTEquationsInput(getExtParamVar(extparam, "name", true), 0, 0, applet.getWidth() - 40);
 			EquationSystem eqSys = new EquationSystem();
 			String finalEqu = null;
 			for(String l : extparam.split("\n")) {
 				l = Utils.trimStr(l);
 				if(l.isEmpty()) continue;
 
 				if(l.startsWith("?"))
 					finalEqu = Utils.trimStr(l.substring(1));
 				else
 					eqSys.addAuto(l);
 			}
 
 			if(finalEqu == null) {
 				System.err.println("in equinput: missing final equation (must start with '?')");
 				return null;
 			}
 
 			OperatorTree finalEquParsed = OTParser.parse(finalEqu, ": ,", null).mergeOps(",");
 			if(!finalEquParsed.op.equals(":") || finalEquParsed.entities.size() != 2) {
 				System.err.println("in equinput: final equation not in right format (? X : a,b,c)");
 				return null;
 			}
 
 			String finalEquLeft = finalEquParsed.entities.get(0).toString();
 			List<String> finalEquVars;
 			if(finalEquParsed.entities.get(1).asTree().op.equals(","))
 				finalEquVars = new ArrayList<String>(Utils.map(finalEquParsed.entities.get(1).asTree().entities, Utils.toStringFunc()));
 			else
 				finalEquVars = Utils.listFromArgs(finalEquParsed.entities.get(1).toString());
 
 			//System.out.println( finalEquLeft + " --- " + finalEquVars );
 			equationInput.setEquationSystem(eqSys, finalEquLeft, finalEquVars);
 			return equationInput;
 		}
 		else if(tagname.compareTo("object") == 0) {
 			return getExternThing(extparam);
 		}
 		else if(tagname.compareTo("m") == 0) {
 //			return resetAllColors(baseparam, Color.blue); 
 			return resetAllColors(resetAllFonts(baseparam, "monospace"), Color.blue); 
 		}
 		else if(tagname.compareTo("define") == 0) {
 			class DefineParamWalker implements ExtParamWalker {
 				public void onNewParam(int index, String param) {
 					// ignore
 				}
 				public void onNewParam(int index, String param, String value) {
 					getVar(param, true).value = value;
 				}
 			}
 			DefineParamWalker walker = new DefineParamWalker();
 			walkExtParams(extparam, walker);
 			return null;
 		}
 		
 		// mathematische Symbole
 		else if(tagname.compareTo("alpha") == 0) {
 			return new VTLabel("α", 0, 0);
 		}
 		else if(tagname.compareTo("beta") == 0) {
 			return new VTLabel("β", 0, 0);
 		}
 		else if(tagname.compareTo("gamma") == 0) {
 			return new VTLabel("γ", 0, 0);
 		}
 		else if(tagname.compareTo("delta") == 0) {
 			return new VTLabel("δ", 0, 0);
 		}
 		else if(tagname.compareTo("eps") == 0) {
 			return new VTLabel("ε", 0, 0);
 		}
 		else if(tagname.compareTo("theta") == 0) {
 			return new VTLabel("θ", 0, 0);
 		}
 		else if(tagname.compareTo("lamda") == 0) {
 			return new VTLabel("λ", 0, 0);
 		}
 		else if(tagname.compareTo("mu") == 0) {
 			return new VTLabel("μ", 0, 0);
 		}
 		else if(tagname.compareTo("pi") == 0) {
 			return new VTLabel("π", 0, 0);
 		}
 		else if(tagname.compareTo("pi") == 0) {
 			return new VTLabel("π", 0, 0);
 		}
 		else if(tagname.compareTo("rightarrow") == 0) {
 			return new VTLabel("→", 0, 0);
 		}
 		else if(tagname.compareTo("Rightarrow") == 0) {
 			return new VTLabel("⇒", 0, 0);
 		}
 		else if(tagname.compareTo("Leftrightarrow") == 0) {
 			return new VTLabel("⇔", 0, 0);
 		}
 		else if(tagname.compareTo("in") == 0) {
 			return new VTLabel("∈", 0, 0);
 		}
 		else if(tagname.compareTo("notin") == 0) {
 			return new VTLabel("∉", 0, 0);
 		} 
 		else if(tagname.compareTo("infty") == 0) {
 			return new VTLabel("∞", 0, 0);
 		}
 		else if(tagname.compareTo("R") == 0) {
 			return new VTLabel("ℝ", 0, 0);
 		}
 		else if(tagname.compareTo("Z") == 0) {
 			return new VTLabel("ℤ", 0, 0);
 		}
 		else if(tagname.compareTo("N") == 0) {
 			return new VTLabel("ℕ", 0, 0);
 		}
 		else if(tagname.compareTo("Q") == 0) {
 			return new VTLabel("ℚ", 0, 0);
 		}
 		else if(tagname.compareTo("leq") == 0) {
 			return new VTLabel("≤", 0, 0);
 		}
 		else if(tagname.compareTo("empty") == 0) {
 			return new VTLabel("∅", 0, 0);
 		}
 		else if(tagname.compareTo("subset") == 0) {
 			return new VTLabel("⊂", 0, 0);
 		}
 		else if(tagname.compareTo("supset") == 0) {
 			return new VTLabel("⊃", 0, 0);
 		}
 		else if(tagname.compareTo("subseteq") == 0) {
 			return new VTLabel("⊆", 0, 0);
 		}
 		else if(tagname.compareTo("supseteq") == 0) {
 			return new VTLabel("⊇", 0, 0);
 		}
 		else if(tagname.compareTo("subsetneq") == 0) {
 			return new VTLabel("⊊", 0, 0);
 		}
 		else if(tagname.compareTo("supsetneq") == 0) {
 			return new VTLabel("⊋", 0, 0);
 		}
 		else if(tagname.compareTo("cap") == 0) {
 			return new VTLabel("∩", 0, 0);
 		}
 		else if(tagname.compareTo("cup") == 0) {
 			return new VTLabel("∪", 0, 0);
 		}
 		else if(tagname.compareTo("cdot") == 0) {
 			return new VTLabel("∙", 0, 0);
 		}
 		else if(tagname.compareTo("times") == 0) {
 			return new VTLabel("×", 0, 0);
 		}
 		else if(tagname.compareTo("div") == 0) {
 			return new VTLabel("÷", 0, 0);
 		}
 		else if(tagname.compareTo("pm") == 0) {
 			return new VTLabel("±", 0, 0);
 		}
 		else if(tagname.compareTo("dash") == 0) {
 			return new VTLabel("―", 0, 0);
 		}
 		else if(tagname.compareTo("neq") == 0) {
 			return new VTLabel("≠", 0, 0);
 		}
 		else if(tagname.compareTo("sqrt") == 0) {
 			return new VTLabel("√", 0, 0);
 		}
 		else if(tagname.compareTo("approx") == 0) {
 			return new VTLabel("≈", 0, 0);
 		}
 		
 		System.err.println("handleTag: don't know tag " + tagname);
 		return null;
 	}
 
 	protected VisualThing handleTag(String tag, VisualThing baseparam) {
 		return handleTag(tag, baseparam, "", null, null);
 	}
 
 	protected VisualThing handleTag(String tag) {
 		return handleTag(tag, null);
 	}
 	
 	protected void addNewVT(List<VisualThing> things, String curstr, VisualThing newVT) {
 		if(curstr.length() > 0) things.add(new VTLabel(curstr, 0, 0));
 		if(newVT != null) things.add(newVT);
 	}
 	
 	protected class Tag {
 		public String name = "";
 		public VisualThing baseparam = null;
 		public String extparam = "";
 		public VisualThing lowerparam = null;
 		public VisualThing upperparam = null;
 		
 		public boolean isSet() {
 			return name.length() != 0;
 		}
 		
 		public boolean everythingExceptNameIsNotSet() {
 			return baseparam == null && extparam.length() == 0 && lowerparam == null && upperparam == null;
 		}
 		
 		public void reset() {
 			name = ""; baseparam = null; extparam = ""; lowerparam = null; upperparam = null;
 		}
 		
 		public VisualThing handle() {
 			return handleTag(name, baseparam, extparam, lowerparam, upperparam);
 		}
 	}
 	
 	public static interface ExtParamWalker {
 		void onNewParam(int index, String param, String value);
 		void onNewParam(int index, String param);
 	}
 
 	@SuppressWarnings({"ConstantConditions"})
 	public List<VisualThing> getThingsByContentStr(String content, int startpos, Utils.Ref<Integer> endpos) {
 		int state = 0;
 		int pos = startpos;
 		List<VisualThing> lastlines = new LinkedList<VisualThing>(); // VTLineCombiners
 		List<VisualThing> things = new LinkedList<VisualThing>(); // current things which are filled
 		String curstr = "";
 		Tag curtag = new Tag();
 		Utils.Ref<Integer> newpos = new Utils.Ref<Integer>(0); // if recursive calls will be done, this is for getting the new pos
 		String curtagtmpstr = ""; // used by lowerparam and upperparam in simple mode
 		
 		while(state >= 0) {
 			int c = (pos >= content.length()) ? -1 : content.charAt(pos);
 			//System.out.println("t: " + pos + "," + state + ": " + c);
 			
 			switch(state) {
 			case 0: // default + clean up
 				curstr = ""; curtag.reset(); state = 1;
 			case 1: // default
 				switch(c) {
 				case '\\': curtag.reset(); state = 10; break;
 				case -1: case '}': case ']': // these marks the end at all
 					state = -1;
 					// no break here, pass down
 				case '\n': // new line
 					addNewVT(things, curstr, null); curstr = "";
 					lastlines.add(new VTLineCombiner(10, 7, getArrayByThingList(things)));
 					things.clear();
 					break;
 				case '{': // without tag, so handle it as container
 					// NOTE: VTMatrix.splitThing currently depends on exactly this behavior
 					addNewVT(things, curstr, createSimpleContainer(getThingsByContentStr(content, pos+1, newpos)));
 					pos = newpos.value - 1;
 					break;
 				default: curstr += (char)c;
 				}
 				break;
 				
 			case 10: // we got a '\', tagmode
 				if(!curtag.isSet()) switch(c) { // check first for special chars if curtag is not set yet
 				case '\\': case '{': case '}':
 				case '[': case ']': case '_':
 				case '^': case -1:
 					curstr += (char)c;
 				case '\n':
 					state = 1; 
 				}
 				if(state == 1) break;
 
 				if(c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c >= '0' && c <= '9')
 					curtag.name += (char)c;
 				else switch(c) {
 				case '{': state = 11; break;
 				case '[': state = 12; break;
 				case '_': state = 13; curtagtmpstr = ""; break;
 				case '^': state = 14; curtagtmpstr = ""; break;
 				default: // nothing special, so tag ended here
 					addNewVT(things, curstr, curtag.handle());
 					state = 0;
 					if(c != ' ' || !curtag.everythingExceptNameIsNotSet())
 						pos--; // handle this char again 
 				}
 				break;
 			case 11: // tagmode, baseparam starting
 				curtag.baseparam = createSimpleContainer(getThingsByContentStr(content, pos, newpos));
 				pos = newpos.value - 1; state = 10;
 				break;
 			case 12: // tagmode, extparam starting
 				if(c == ']')
 					state = 10;
 				else
 					curtag.extparam += (char)c;
 				break;
 			case 13: // tagmode, lowerparam simple (directly after '_')
 				switch(c) {					
 				case -1: case '\\': pos--;
 				case ' ': case 8: case '\n':
 				case '^':
 					curtag.lowerparam = new VTLabel(curtagtmpstr, 0, 0);
 					curtagtmpstr = "";
 					if(c == '^') state = 14;
 					else state = 10;
 					break;
 				case '{': state = 15; break;
 				default: curtagtmpstr += (char)c;
 				}
 				break;
 			case 14: // tagmode, upperparam simple (directly after '^')
 				switch(c) {					
 				case -1: case '\\': pos--;
 				case ' ': case 8: case '\n':
 				case '_':
 					curtag.upperparam = new VTLabel(curtagtmpstr, 0, 0);
 					curtagtmpstr = "";
 					if(c == '_') state = 13;
 					else state = 10;
 					break;
 				case '{': state = 16; break;
 				default: curtagtmpstr += (char)c;
 				}
 				break;
 			case 15: // tagmode, lowerparam normal (in {...})
 				curtag.lowerparam = createSimpleContainer(getThingsByContentStr(content, pos, newpos));
 				pos = newpos.value - 1; state = 10;
 				break;
 			case 16: // tagmode, upperparam normal (in {...})
 				curtag.upperparam = createSimpleContainer(getThingsByContentStr(content, pos, newpos));
 				pos = newpos.value - 1; state = 10;
 				break;
 				
 			
 			
 			default:
 				System.err.println("getThingsByContentStr: unknown state " + state);
 				state = 0;
 			}
 			
 			pos++;
 		}
 		endpos.value = pos;
 		
 		// we fill the last things in the automata automatically in lastlines at the end
 		if(lastlines.size() == 1) {
 			lastlines.get(0).setStepX(0);
 			lastlines.get(0).setStepY(0);
 		}
 		return lastlines;
 	}
 
 	// seperated string like "bla1,bla2" is input
 	public String[] getStringArrayFromString(String base) {
 		final List<String> items = new LinkedList<String>();
 		walkExtParams(base, new ExtParamWalker() {
 			public void onNewParam(int index, String param) {
 				items.add(param);
 			}
 			public void onNewParam(int index, String param, String value) {
 				// ignore
 			}
 		});
 		
 		String[] res = new String[items.size()];
 		for(int i = 0; i < res.length; i++)
 			res[i] = items.get(i);
 		return res;
 	}
 
 	/*
 	 * expect extparam as "param1=value1,param2=value2,..."
 	 */
 	protected void walkExtParams(String extparam, ExtParamWalker walker) {
 		int state = 0;
 		String curparam = ""; 
 		String curvar = "";
 		int pos = 0;
 		int parcount = 0;
 		int lastc = -1;
 		
 		while(state >= 0) {
 			int c = pos < extparam.length() ? extparam.charAt(pos) : -1;
 			//System.out.println("w: " + pos + "," + state + ": " + c);
 			
 			switch(state) {
 			case 0: // paramname
 				switch(c) {
 				case -1: state = -1;
 				case ',':
 					walker.onNewParam(parcount, curparam);
 					curparam = "";
 					parcount++;
 					break;
 				case '\"': state = 2; break;
 				case '=': state = 5; break;
 				case ' ': case '\n':
 					break; // ignore
 				default:
 					curparam += (char)c;
 				}
 				break;
 	
 			case 2: // var in ""
 				switch(c) {
 				case -1: state = 0; break;
 				case '\"':
 					if(lastc != '\\') {
 						state = 0;
 						break;
 					}
 				default: curparam += (char)c;
 				}
 				break;
 	
 			case 5: // var
 				switch(c) {
 				case -1:
 				case ',':
 					walker.onNewParam(parcount, curparam, curvar);
 					curparam = ""; curvar = "";
 					parcount++;
 					state = 0; break;
 				case '\"':
 					state = 6; break;
 				default: curvar += (char)c;
 				}
 				break;
 	
 			case 6: // var in ""
 				switch(c) {
 				case -1: state = 5; break;
 				case '\"':
 					if(lastc != '\\') {
 						state = 5;
 						break;
 					}
 				default: curvar += (char)c;
 				}
 				break;
 				
 			}
 			
 			lastc = c;
 			pos++;
 		}
 	}
 
 	public String getExtParamVar(String extparam, final String param, final boolean matchIfNoParams) {
 		class Walker implements ExtParamWalker {
 			public String ret = "";
 			
 			public void onNewParam(int index, String param) {
 				if(matchIfNoParams && ret.length() == 0) ret = param;
 			}
 			public void onNewParam(int index, String p, String value) {
 				if(param.compareTo(p) == 0) ret = value;
 			}
 		}
 		Walker walker = new Walker();
 		walkExtParams(extparam, walker);
 		
 		return walker.ret;
 	}
 
 	public String getExtParamVar(String extparam, String param) {
 		return getExtParamVar(extparam, param, false);
 	}
 
 	public VisualThing[] getExtern() {
 		return extern;
 	}
 
 	public void setExtern(VisualThing[] extern) {
 		this.extern = extern;
 	}
 	
 }
