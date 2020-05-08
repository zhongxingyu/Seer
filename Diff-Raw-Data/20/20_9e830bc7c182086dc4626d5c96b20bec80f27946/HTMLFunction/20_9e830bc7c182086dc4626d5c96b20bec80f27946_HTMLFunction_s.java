 package supersql.codegenerator.HTML;
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 import org.jsoup.nodes.Attributes;
 import org.jsoup.nodes.DataNode;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Node;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.parser.Tag;
 
 import supersql.codegenerator.CodeGenerator;
 import supersql.codegenerator.DecorateList;
 import supersql.codegenerator.FuncArg;
 import supersql.codegenerator.Function;
 import supersql.codegenerator.Manager;
 import supersql.codegenerator.TFE;
 import supersql.common.GlobalEnv;
 import supersql.common.Log;
 import supersql.dataconstructor.DataConstructor;
 import supersql.extendclass.ExtList;
 import supersql.parser.SSQLparser;
 
 public class HTMLFunction extends Function {
 
 	private static int meterId = 0;
     //added by goto 20130914  "SEQ_NUM"
     static ArrayList<Integer> seq_num = new ArrayList<Integer>();
     static ArrayList<String> seq_num_ClassID = new ArrayList<String>();
     static ArrayList<Integer> seq_num_startNum = new ArrayList<Integer>();
     static ArrayList<Boolean> seq_num_DESC_Flg = new ArrayList<Boolean>();
     static String classID = "";
 
 
 	// 鐃緒申鐃藷ストラク鐃緒申
 	@Deprecated
 	/** use HTMLFunction() instead **/
 	public HTMLFunction(Manager manager, HTMLEnv henv, HTMLEnv henv2) {
 		super();
 	}
 	public HTMLFunction(){
 		super();
 	}
 
 	@Override
 	public Node createNode(ExtList<ExtList<String>> data_info) {
 		this.setDataList(data_info);
 		String FuncName = this.getFuncName();
 		if (FuncName.equalsIgnoreCase("imagefile")
 				|| FuncName.equalsIgnoreCase("image")
 				|| FuncName.equalsIgnoreCase("img")) {
 			return FuncImagefile();
 		} else if (FuncName.equalsIgnoreCase("invoke")) {
 			return FuncInvoke();
 		} else if (FuncName.equalsIgnoreCase("foreach")) {
 			return FuncForeach(data_info);
 		} else if (FuncName.equalsIgnoreCase("sinvoke")
 				|| FuncName.equalsIgnoreCase("link")) {
 			return FuncSinvoke(data_info);
 		} else if (FuncName.equalsIgnoreCase("null")) {
 			return FuncNull();
 		} else if (FuncName.equalsIgnoreCase("url")
 				|| FuncName.equalsIgnoreCase("anchor")
 				|| FuncName.equalsIgnoreCase("a")) {
 			return FuncUrl(false);
 		} else if (FuncName.equalsIgnoreCase("mail")) {
 			return FuncUrl(true);
 		} else if (FuncName.equalsIgnoreCase("object")) {
 			return FuncObject("");
 		} else if (FuncName.equalsIgnoreCase("seq_num")) {
 			return Func_seq_num();
 		} else if (FuncName.equalsIgnoreCase("submit")) {
 			return FuncSubmit();
 		} else if (FuncName.equalsIgnoreCase("select")) {
 			return FuncSelect();
 		} else if (FuncName.equalsIgnoreCase("checkbox")) {
 			return FuncCheckbox();
 		} else if (FuncName.equalsIgnoreCase("radio")) {
 			return FuncRadio();
 		} else if (FuncName.equalsIgnoreCase("inputtext")) {
 			return FuncInputtext();
 		} else if (FuncName.equalsIgnoreCase("textarea")) {
 			return FuncTextarea();
 		} else if (FuncName.equalsIgnoreCase("hidden")) {
 			return FuncHidden();
 		} else if (FuncName.equalsIgnoreCase("session")) {
 			// Func_session(); not use
 			return new Element(Tag.valueOf(""), "");
 		} else if (FuncName.equalsIgnoreCase("youtube")) {
 			return funcYoutube();
 		} else if (FuncName.equalsIgnoreCase("video")) {
 			return funcVideoFile();
 		} else if (FuncName.equalsIgnoreCase("meter")) {
 			return funcMeter();
 		}
 		// added by goto 20130308 "url���"
 		else if (FuncName.equalsIgnoreCase("url")
 				|| FuncName.equalsIgnoreCase("anchor")
 				|| FuncName.equalsIgnoreCase("a")) {
 			return FuncUrl(false);
 		}
 		// added by goto 20130417 "mail"
 		else if (FuncName.equalsIgnoreCase("mail")) {
 			FuncUrl(true);
 		}
 		// tk start//////////////////////////////////
 		else if (FuncName.equalsIgnoreCase("embed")) {
 			Log.out("[enter embed]");
 			return FuncEmbed(data_info);
 		}
 		// tk end////////////////////////////////////
 		Log.out("TFEId = " + HTMLEnv.getClassID(this));
 		return null;
 	}
 
 	private void checkArgsNumber(int expected) {
 		if (expected != Args.size()) {
 			System.err.println("Argument number error for the function "
 					+ getFuncName() + "\n This function has " + expected
 					+ " required parameters");
 			System.exit(1);
 		}
 	}
 
 	private void checkArgType(FuncArg argument, int index, Class<? extends TFE>... expectedClass) {
 		for (int i = 0; i < expectedClass.length; i++) {
 			if (expectedClass[i] == HTMLC0.class
 					&& argument.getTFEClass() == HTMLAttribute.class)
 				return;
 
 			if ((expectedClass[i].isAssignableFrom(argument.getTFEClass()))) {
 				return;
 			}
 		}
 		System.err.println("The argument number " + index + " of the "
 				+ getFuncName()
 				+ " function has a wrong type. It should be a "
 				+ expectedClass.toString());
 		System.exit(1);
 	}
 
 	private Element FuncEmbed(ExtList<ExtList<String>> data_info) {
 		String file = this.getAtt("file");
 		String where = this.getAtt("where");
 		String att = this.getAtt("att");
 		String att2 = this.getAtt("attString");
 		String condition = new String();
 		boolean is_hidden = false;
 		Element result = null;
 
 		if (decos.containsKey("status"))
 			if (decos.getStr("status").compareTo("hidden") == 0)
 				is_hidden = true;
 
 		// for tab
 		if (decos.containsKey("tab")) {
 			result = new Element(Tag.valueOf("div"), "");
 			result.attr("id", "myTab");
 			if (decos.containsKey("class"))
 				result.addClass(decos.getStr("class"));
 			Element div = new Element(Tag.valueOf("div"), "")
 					.attr("id", "mTab").addClass("yui-navset");
 			result.appendChild(div);
 
 			Element script = new Element(Tag.valueOf("script"), "");
 			script.append("var mTab = new YAHOO.widget.TabView(\"mTab\");\new YAHOO.util.DDTarget(\"myTab\", \"myTab\");");
 			result.appendChild(script);
 
 			return result;
 		}
 
 		if (!is_hidden) {
 			result = new Element(Tag.valueOf("table"), "").addClass("att")
 					.addClass(HTMLEnv.getOutlineModeAtt());
 
 			if (decos.containsKey("class"))
 				result.addClass(decos.getStr("class"));
 			else
 				result.addClass(HTMLEnv.getClassID(this));
 			result.append("<tr><td></td></tr>");
 			// htmlEnv.code.append("><tr><td>");
 		}
 
 		// for ajax div id
 		// //////////////////////////////////////////////////////
 
 		String divname = "";
 		boolean has_divid = false;
 
 		if (decos.containsKey("divid")) {
 			has_divid = true;
 			Log.out("embed contains decos with divid");
 			String tmpdivid = decos.getStr("divid");
 			String tmp;
 			String ans;
 
 			if (tmpdivid.contains("+")) {
 				ans = tmpdivid.substring(0, tmpdivid.indexOf("+"));
 				tmp = tmpdivid.substring(tmpdivid.indexOf("+") + 1,
 						tmpdivid.length());
 
 				if (tmp.compareTo("att") == 0) {
 					tmp = att;
 				}
 				divname = ans + "_" + tmp;
 				Log.out("ans :" + ans + " tmp:" + tmp + " divname:" + divname);
 			} else {
 				divname = decos.getStr("divid");
 			}
 		}
 		Element script = null;
 		if (GlobalEnv.isAjax() && decos.containsKey("droppable")) {
 			script = new Element(Tag.valueOf("script"), "")
 					.append("new YAHOO.util.DDTarget(\"" + divname + "\", \""
 							+ divname + "\");");
 		}
 		// ajax & decos contains status=hidden
 		if (is_hidden && GlobalEnv.isAjax()) {
 
 			result = new Element(Tag.valueOf("div"), "").attr("id", divname);
 
 			HTMLEnv.code.append("<div id=\"" + divname + "\" ");
 			if (script != null) {
 				result.appendChild(script);
 			}
 			if (decos.containsKey("class"))
 				HTMLEnv.code.append("class=\"" + decos.getStr("class") + "\" ");
 
 			HTMLEnv.code.append("></div>");
 			Log.out("<div id=" + divname + "></div>");
 
 			return result;
 		}
 		if (att.compareTo("") != 0) {
 			condition = condition + where + att;
 		} else if (att2.compareTo("") != 0) {
 			condition = condition + where + "'" + att2 + "'";
 		}
 		// store original config
 		Hashtable<String, String> tmphash = GlobalEnv.getEnv();
 
 		String[] args;
 		if (GlobalEnv.isAjax()) {
 			if (condition.equals("")) {
 				args = new String[3];
 				args[0] = "-f";
 				args[1] = file;
 				args[2] = "-ajax";
 
 			} else {
 				args = new String[5];
 				args[0] = "-f";
 				args[1] = file;
 				args[2] = "-cond";
 				args[3] = condition;
 				args[4] = "-ajax";
 			}
 		} else {
 			if (GlobalEnv.isOpt()) {
 				args = new String[5];
 				args[0] = "-f";
 				args[1] = file;
 				args[2] = "-cond";
 				args[3] = condition;
 				args[4] = "-optimizer";
 			} else {
 				args = new String[4];
 				args[0] = "-f";
 				args[1] = file;
 				args[2] = "-cond";
 				args[3] = condition;
 			}
 		}
 
 		HTMLEnv.embedCount++;
 
 		if (file.contains(".sql")) {
 
 			String makedfilename = file.substring(file.lastIndexOf("\\") + 1,
 					file.indexOf("."));
 
 			if (att.compareTo("") != 0)
 				makedfilename = makedfilename.concat("_" + att);
 			if (att2.compareTo("") != 0)
 				makedfilename = makedfilename.concat("_" + att2);
 
 			makedfilename = makedfilename.concat(".html");
 
 			Log.out("embed tmpfilename:" + makedfilename + " option:"
 					+ GlobalEnv.getEmbedOption());
 
 			File makedfile = new File(GlobalEnv.getEmbedTmp(), makedfilename);
 
 			if (makedfile.exists() && GlobalEnv.isNewEmbed() == 1) {
 				Log.out("[Enter new Embed]");
 				Log.out("embed read tmp file");
 				BufferedReader dis;
 				String line = new String();
 				try {
 					dis = new BufferedReader(new FileReader(makedfile));
 
 					try {
 						while (!line.equalsIgnoreCase(" ")) {
 							Log.out("line : " + line);
 							line = dis.readLine();
 							if (line != null)
 								HTMLEnv.code.append(line);
 						}
 					} catch (NullPointerException e) {
 						Log.out("no more lines");
 					}
 
 					dis.close();
 				} catch (IOException ioe) {
 					System.out.println("IOException: " + ioe);
 				}
 			} else {
 				Log.out("embed make file");
 
 				GlobalEnv.setGlobalEnvEmbed(args);
 
 				SSQLparser parser;
 				if (file.contains("http")) {
 					parser = new SSQLparser("online");
 				} else {
 					parser = new SSQLparser(10000 * (HTMLEnv.embedCount + 1));
 				}
 
 				CodeGenerator codegenerator = parser.getcodegenerator();
 				DataConstructor dc = new DataConstructor(parser);
 
 				StringBuffer returnedcode = codegenerator.generateCode2(parser,
 						dc.getData());
 
 				// ajax add div
 				// tag////////////////////////////////////////////////////////////////////
 				if (GlobalEnv.isAjax()) {
 					if (!has_divid) {
 						// online file
 						if (file.contains("/")) {
 							divname = file.substring(file.lastIndexOf("/") + 1,
 									file.indexOf(".sql"));
 						}
 						// ofline file
 						else if (file.contains("\\")) {
 							divname = file.substring(
 									file.lastIndexOf("\\") + 1,
 									file.indexOf(".sql"));
 						}
 						// only file name
 						else {
 							divname = file.substring(0, file.indexOf(".sql"));
 						}
 					}
 
 					HTMLEnv.code.append("<div id=\"" + divname + "\" ");
 
 					if (decos.containsKey("class"))
 						HTMLEnv.code.append("class=\"" + decos.getStr("class")
 								+ "\" ");
 
 					HTMLEnv.code.append(">");
 					Log.out("<div id=" + divname + ">");
 				}
 
 				// xml�����
 				if (!is_hidden) {
 					HTMLEnv.code.append("<EMBED>");
 					HTMLEnv.code.append(returnedcode);
 					HTMLEnv.code.append(returnedcode);
 					HTMLEnv.code.append("</EMBED>");
 				}
 
 				if (GlobalEnv.isAjax())
 					HTMLEnv.code.append("</div>");
 				// end ajax
 				// /////////////////////////////////////////////////////////////////
 
 				if (HTMLEnv.embedCount >= 1) {
 					HTMLEnv.css.append(codegenerator.generateCode3(parser,
 							dc.getData()));
 					HTMLEnv.cssFile.append(codegenerator.generateCssfile(
 							parser, dc.getData()));
 				}
 
 				// restore original config
 				GlobalEnv.setEnv(tmphash);
 
 				// writing tmpfile
 				Log.out("embed hogehoge:" + GlobalEnv.isNewEmbed());
 				Log.out("enb:" + GlobalEnv.getEnv());
 
 				if (GlobalEnv.isNewEmbed() == 1) {
 					GlobalEnv.addEmbedFile(makedfilename);
 					Log.out("embed start writing");
 					String filename = GlobalEnv.getEmbedTmp();
 
 					if (filename.endsWith("/") || filename.endsWith("\\"))
 						filename = filename + makedfilename;
 					else
 						filename = filename + "/" + makedfilename;
 
 					try {
 						OutputStream fout = new FileOutputStream(filename);
 						OutputStream bout = new BufferedOutputStream(fout);
 						OutputStreamWriter out = new OutputStreamWriter(bout,
 								"UTF-8");
 
 						out.write(HTMLEnv.header.toString());
 						out.write(returnedcode.toString());
 						out.write(HTMLEnv.footer.toString());
 
 						out.close();
 					} catch (FileNotFoundException fe) {
 
 						fe.printStackTrace();
 						System.err
 								.println("Error: specified embedtmp outdirectory \""
 										+ GlobalEnv.getEmbedTmp()
 										+ "\" is not found to write "
 										+ HTMLEnv.fileName);
 
 						GlobalEnv
 								.addErr("Error: specified embedtmp outdirectory \""
 										+ GlobalEnv.getEmbedTmp()
 										+ "\" is not found to write "
 										+ HTMLEnv.fileName);
 					} catch (IOException e) {
 						System.err
 								.println("Error[HTMLManager]: File IO Error in HTMLManager at embed");
 						e.printStackTrace();
 						GlobalEnv
 								.addErr("Error[HTMLManager]: File IO Error in HTMLManager at embed");
 					}
 				}
 
 			}
 		}
 		// embed html file
 		else if (file.contains(".html")) {
 			String line = new String();
 
 			if (decos.containsKey("divid"))
 				divname = decos.getStr("divid");
 			else if (file.contains("\\"))
 				divname = file.substring(file.lastIndexOf("\\") + 1,
 						file.indexOf(".html"));
 			else if (file.contains("/"))
 				divname = file.substring(file.lastIndexOf("/") + 1,
 						file.indexOf(".html"));
 			else
 				divname = file.substring(0, file.indexOf(".html"));
 
 			BufferedReader dis;
 			try {
 				if (file.contains("http://")) {
 					URL fileurl = new URL(file);
 
 					URLConnection fileurlConnection = fileurl.openConnection();
 					dis = new BufferedReader(new InputStreamReader(
 							fileurlConnection.getInputStream()));
 				} else {
 					try {
 						Log.out("embed file (html):" + file);
 						dis = new BufferedReader(new FileReader(new File(file)));
 					} catch (IOException ioe) {
 						String path = HTMLEnv.outFile;
 						if (path.contains("\\"))
 							path = path
 									.substring(0, path.lastIndexOf("\\") + 1);
 						else if (path.contains("/"))
 							path = path.substring(0, path.lastIndexOf("/") + 1);
 						if (file.startsWith("./")) {
 							file = file.substring(1, file.length());
 						}
 						Log.out("embed file (html):" + path + file);
 						if (path.startsWith("http:")) {
 							URL fileurl = new URL(path + file);
 							URLConnection fileurlConnection = fileurl
 									.openConnection();
 							dis = new BufferedReader(new InputStreamReader(
 									fileurlConnection.getInputStream()));
 						} else {
 							dis = new BufferedReader(new FileReader(new File(
 									path + file)));
 
 						}
 					}
 				}
 				line = dis.readLine(); // read <BODY> and/or <HEAD>
 				if (line.contains("<head>")) {
 				} else {
 					line = dis.readLine(); // read <HEAD>
 				}
 
 				while (!line.equalsIgnoreCase("</head>")) {
 					line = dis.readLine();
 					if (!line.equalsIgnoreCase("</head>"))
 						HTMLEnv.header.append(line + "\n");
 				}
 				line = dis.readLine(); // read <body>
 
 				HTMLEnv.code.append("<div id=\"" + divname + "\" ");
 
 				if (decos.containsKey("class"))
 					HTMLEnv.code.append("class=\"" + decos.getStr("class")
 							+ "\" ");
 
 				HTMLEnv.code.append(">");
 
 				HTMLEnv.code.append("<EMBED>");
 				while (!line.equalsIgnoreCase("</body>")) {
 					Log.out("line : " + line);
 					line = dis.readLine();
 					if (!line.equalsIgnoreCase("</body>")) {
 						HTMLEnv.code.append(line);
 						if (line.contains("&"))
 							line = line.replace("&", "&amp;");
 						if (line.contains("<"))
 							;
 						line = line.replace("<", "&lt;");
 						if (line.contains(">"))
 							line = line.replace(">", "&gt;");
 						if (line.contains("���"))
 							line = line.replace("���", "&#65374;");
 						HTMLEnv.code.append(line);
 					}
 				}
 				HTMLEnv.code.append("</EMBED>");
 
 				HTMLEnv.code.append("</div>");
 				dis.close();
 
 			} catch (MalformedURLException me) {
 				System.out.println("MalformedURLException: " + me);
 			} catch (IOException ioe) {
 				System.out.println("HTMLFuncEmbed:IOException: " + ioe);
 			}
 
 		}
 		if (!is_hidden)
 			HTMLEnv.code.append("</td></tr></table>");
 
 		HTMLEnv.embedCount += 1;
 		return result;
 	}
 
 	private Element FuncHidden() {
 		return FuncFormCommon("hidden");
 	}
 
 	private Element FuncTextarea() {
 		return FuncFormCommon("textarea");
 	}
 
 	private Element FuncInputtext() {
 		return FuncFormCommon("text");
 	}
 
 	private Element FuncRadio() {
 		if (!this.getAtt("checked").equals("")) {
 			HTMLEnv.setChecked(this.getAtt("checked"));
 		}
 		return FuncFormCommon("radio");
 	}
 
 	private Element FuncCheckbox() {
 		Element result = FuncFormCommon("checkbox");
 
 		if (!this.getAtt("checked").equals("")) {
 			HTMLEnv.setChecked(this.getAtt("checked"));
 		}
 
 		return result;
 	}
 
 	private Element FuncSelect() {
 		if (!this.getAtt("selected").equals("")) {
 			HTMLEnv.setSelected(this.getAtt("selected"));
 		}
 		return FuncFormCommon("select");
 	}
 
 	private Element FuncSubmit() {
 		Element result = new Element(Tag.valueOf("form"), "");
 		boolean openFormInThis = false;
 
 		// submit only ----- no "@{form}"
 		if (!HTMLEnv.getFormItemFlg() && !decos.containsKey("form")) {
 			result = createForm();
 			openFormInThis = true;
 		} else if (decos.containsKey("form")) {
 			result = createForm(decos);
 			openFormInThis = true;
 		}
 
 		HTMLEnv.setFormItemFlg(true, "submit");
 
 		String value = "";
 		if (!this.getAtt("default").equals(null)) {
 			value = this.getAtt("default");
 		}
 
 		result.appendChild(JsoupFactory.createInput("submit", "", value));
 		if (openFormInThis == true) {
 			HTMLEnv.setFormItemFlg(false, null);
 			openFormInThis = false;
 		} else {
 			HTMLEnv.setFormItemFlg(true, null);
 		}
 
 		return result;
 	}
 
 	private Element FuncNull() {
 		return new Element(Tag.valueOf("span"), "");
 	}
 
 	private Attributes getAttributes() {
 		Attributes attributes = new Attributes();
 		for (String key : ArgHash.keySet()) {
 			attributes.put(key, getAtt(key));
 		}
 		return attributes;
 	}
 
 	private Element FuncSinvoke(ExtList<ExtList<String>> data_info) {
 		checkArgsNumber(2);
 
 		FuncArg content = this.Args.get(0);
 		FuncArg href = this.Args.get(1);
 
 		checkArgType(content, 0, TFE.class);
 		checkArgType(href, 1, HTMLC0.class);
 
 		Element result = new Element(Tag.valueOf("a"), "");
 		result.addClass("box").addClass("link");
 		result.appendChild((Element) content.createNode());
 		result.attr("href", ((Element) href.createNode()).text());
 		result.attributes().addAll(getAttributes());
 
		if (true || GlobalEnv.isAjax()) {
 			HTMLEnv.ajaxCond = this.getAtt("ajaxcond") + "="
 					+ this.getAtt("att");
 
 			Date d2 = new Date();
 			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyymmddHHmmss");
 			String today2 = sdf2.format(d2);
 
 			HTMLEnv.dragDivId = HTMLEnv.ajaxQuery + "+" + HTMLEnv.ajaxCond
 					+ "&" + today2;
 
 			if (decos.containsKey("in")) {
 				String effect = decos.getStr("in");
 
 				if (effect.equalsIgnoreCase("blind"))
 					HTMLEnv.inEffect = 1;
 				if (effect.equalsIgnoreCase("fade"))
 					HTMLEnv.inEffect = 2;
 			}
 			if (decos.containsKey("out")) {
 				String effect = decos.getStr("out");
 
 				if (effect.equalsIgnoreCase("blind"))
 					HTMLEnv.outEffect = 1;
 				if (effect.equalsIgnoreCase("fade"))
 					HTMLEnv.outEffect = 2;
 			}
 
 			if (decos.containsKey("panel")) {
 				HTMLEnv.isPanel = true;
 			}
 			if (decos.containsKey("dispdiv")) {
 				String dispdiv = decos.getStr("dispdiv");
 				if (dispdiv.contains("+")) {
 					String tmp2 = dispdiv
 							.substring(0, dispdiv.lastIndexOf("+"));
 					String tmp3 = dispdiv.substring(
 							dispdiv.lastIndexOf("+") + 1, dispdiv.length());
 
 					if (tmp3.compareTo("att") == 0) {
 						HTMLEnv.ajaxtarget = tmp2 + "_" + this.getAtt("att");
 					} else
 						HTMLEnv.ajaxtarget = dispdiv;
 				} else {
 					HTMLEnv.ajaxtarget = dispdiv;
 				}
 				HTMLEnv.hasDispDiv = true;
 			} else if (decos.containsKey("dragto")) {
 				HTMLEnv.draggable = true;
 
 				// drag to
 				String value = decos.getStr("dragto");
 				String[] droptarget = new String[100];
 				int targetnum = 0;
 
 				if (value.contains("+")) {
 					while (true) {
 						if (!value.contains("+")) {
 							droptarget[targetnum] = value;
 							targetnum++;
 							break;
 						}
 						droptarget[targetnum] = value.substring(0,
 								value.indexOf("+"));
 						value = value.substring(value.indexOf("+") + 1,
 								value.length());
 
 						targetnum++;
 					}
 				} else
 					droptarget[0] = value;
 
 				// script 鐃緒申鐃緒申
 				Date d1 = new Date();
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
 				String today = sdf.format(d1);
 
 				String scriptname = "drop" + today + HTMLEnv.scriptNum;
 				HTMLEnv.script.append(scriptname + " = new DragDrop(\""
 						+ HTMLEnv.dragDivId + "\", \"" + droptarget[0]
 						+ "\");\n");
 
 				// for tab
 				HTMLEnv.script.append(scriptname + ".addToGroup(\"myTab\");\n");
 
 				for (int i = 1; i < targetnum; ++i) {
 					HTMLEnv.script.append(scriptname + ".addToGroup(\""
 							+ droptarget[i] + "\");\n");
 				}
 
 				HTMLEnv.scriptNum++;
 			}
 		}
 
 		return result;
 	}
 
 	private Element FuncForeach(ExtList<ExtList<String>> data_info) {
 		String att = new String();
 		for (int i = 0; i < this.countconnectitem(); i++) {
 			att = att + "_" + this.getAtt(Integer.toString(i));
 		}
 		try {
 			att = URLEncoder.encode(att, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		String filename = HTMLEnv.outFile + att + ".html";
 
 		HTMLEnv.fileName = filename;
 		return null;
 	}
 
 	private Element FuncInvoke() {
 
 		String path = this.getAtt("path", ".");
 		if (!GlobalEnv.getFileDirectory().equals(".")) {
 			path = GlobalEnv.getFileDirectory();
 		}
 		String filename = this.getAtt("filename");
 		if (!filename.startsWith("/") && (path != null)) {
 			filename = path + "/" + filename;
 		}
 
 		HTMLEnv.linkUrl = this.getAtt("server_path",
 				GlobalEnv.getInvokeServletPath())
 				+ "?"
 				+ "config="
 				+ path
 				+ "/config.ssql"
 				+ "&"
 				+ "query="
 				+ filename + "&" + "cond=" + this.getAtt("condition");
 
 		Element result = (Element) this.createNodeAtt("default");
 
 		return result;
 	}
 
 	@SuppressWarnings("unused")
 	private String cleanPathString(String path) {
 		String result = "";
 		if (!path.startsWith("/")) {
 			String basedir = GlobalEnv.getBaseDir();
 			if (basedir != null && basedir != "") {
 				result = GlobalEnv.getBaseDir() + "/" + path;
 			}
 		}
 		if (GlobalEnv.isServlet()) {
 			result = GlobalEnv.getFileDirectory() + path;
 		}
 		return result;
 	}
 
 	private Element FuncImagefile() {
 		checkArgsNumber(1);
 
 		FuncArg src = Args.get(0);
 
 		Element result = new Element(Tag.valueOf("img"), "");
 		result.attributes().addAll(getAttributes());
 
 		String srcString = ((Element) src.createNode()).text();
 		result.attr("src", srcString);
 
 		if (decos.containsKey("lightbox")) {
 			Date d1 = new Date();
 			SimpleDateFormat sdf = new SimpleDateFormat("yyyymmddHHmmss");
 			String today = sdf.format(d1);
 
 			Element link = JsoupFactory.createLink(
 					srcString + "/" + this.getAtt("default"), "", "");
 			link.attr("rel", "lightbox[lb" + today + "]");
 			result.appendChild(link);
 
 			if (decos.getStr("lightbox").compareTo("root") == 0
 					|| decos.getStr("lightbox").compareTo("thumb") == 0) {
 				Element img = new Element(Tag.valueOf("img"), "")
 						.addClass(HTMLEnv.getClassID(this));
 				HTMLEnv.code.append("<img class=\"" + HTMLEnv.getClassID(this)
 						+ " ");
 
 				img.attr("src", srcString + "/" + this.getAtt("default")).attr(
 						"onLoad", "initLightBox()");
 				link.appendChild(img);
 			}
 		} else {
 			result.addClass(HTMLEnv.getClassID(this));
 		}
 		return result;
 	}
 
 	private Element FuncUrl(boolean mailFncFlg) {
 		Element result = null;
 
 		FuncArg fa1 = (FuncArg) this.Args.get(0), fa2, fa3;
 		String url, name, type;
 
 		try { // 鐃緒申鐃�鐃緒申 or 3鐃縦の常申鐃�
 			fa2 = (FuncArg) this.Args.get(1);
 			url = ((mailFncFlg) ? ("mailto:") : ("")) + fa2.getStr();
 			name = fa1.getStr();
 
 			try { // 鐃緒申鐃�鐃縦の常申鐃�
 				fa3 = (FuncArg) this.Args.get(2);
 				type = fa3.getStr();
 
 				// type=1 -> 文鐃緒申
 				if (type.equals("1") || type.equals("text") || type.equals("")) {
 					result = getTextAnchor(url, name);
 
 					// url鐃旬ワ申鐃緒申(鐃叔ワ申鐃緒申鐃夙ップ￥申鐃緒申丱鐃緒申覿�申鐃�
 				} else if (type.equals("3") || type.equals("button")
 						|| type.equals("bt")) {
 					result = JsoupFactory.createInput("button", "", name).attr(
 							"onClick", "location.href='" + url + "");
 					result.addClass(className());
 
 					// url鐃旬ワ申鐃緒申 width,height鐃緒申鐃緒申鐃緒申僚鐃緒申鐃�
 					if (decos.containsKey("width")
 							|| decos.containsKey("height")) {
 						String style = "";
 						if (decos.containsKey("width"))
 							style += "WIDTH:"
 									+ decos.getStr("width").replace("\"", "")
 									+ "; ";
 						if (decos.containsKey("height"))
 							style += "HEIGHT:"
 									+ decos.getStr("height").replace("\"", "")
 									+ "; "; // 100; ";
 						result.attr("style", style);
 					}
 
 					// type=3 -> url鐃緒申鐃緒申
 				} else if (type.equals("2") || type.equals("image")
 						|| type.equals("img")) {
 					result = JsoupFactory.createLink(url, "", "", className());
 					result.attr("data-transition", transition());
 					if (decos.containsKey("prefetch")
 							|| decos.containsKey("pref"))
 						result.attr("data-prefetch", "");
 
 					Element img = new Element(Tag.valueOf("img"), "");
 
 					// url鐃緒申鐃緒申 width,height鐃緒申鐃緒申鐃緒申僚鐃緒申鐃�
 					if (decos.containsKey("width"))
 						img.attr("width",
 								decos.getStr("width").replace("\"", ""));
 					else {
 						// added by goto 20130312 "Default width: 100%"
 						img.attr("width", "100%");
 					}
 					if (decos.containsKey("height"))
 						img.attr("height",
 								decos.getStr("height").replace("\"", "")); // 100;
 
 					result.appendChild(img);
 				}
 
 			} catch (Exception e) { // 鐃緒申鐃�鐃縦の常申鐃� statement =
 			}
 
 		} catch (Exception e) { // 鐃緒申鐃�鐃縦の常申鐃�
 			url = fa1.getStr();
 			result = JsoupFactory.createLink(
 					((mailFncFlg) ? ("mailto:") : ("")), "", url);
 			result.attr("data-transition", transition());
 			if (decos.containsKey("prefetch") || decos.containsKey("pref"))
 				result.attr("data-prefetch", "");
 		}
 
 		// 鐃銃逸申鐃緒申鐃祝緒申鐃緒申鐃縮わ申HTML鐃祝書きわ申鐃緒申
 		return result;
 	}
 
 	private Element getTextAnchor(String url, String name) {
 		// [ ]鐃叔囲わ申譴随申鐃淑�申鐃熟ワ申鐃術￥申鐃緒申鵐�砲鐃緒申鐃� //ex) a("[This] is anchor.","URL")
 		String A = "";
 		int a1 = 0, a2 = name.length() - 1;
 		try {
 			for (int i = 0; i < name.length(); i++) {
 				if (i > 0 && name.charAt(i) == '['
 						&& name.charAt(i - 1) != '\\')
 					a1 = i;
 				else if (i > 0 && name.charAt(i) == ']'
 						&& name.charAt(i - 1) != '\\')
 					a2 = i;
 			}
 			if (a1 == 0 && a2 == name.length() - 1)
 				A = name.substring(a1, a2 + 1);
 			else
 				A = name.substring(a1 + 1, a2);
 			A = A.replaceAll("\\\\\\[", "[").replaceAll("\\\\\\]", "]");
 			name.substring(0, a1).replaceAll("\\\\\\[", "[")
 					.replaceAll("\\\\\\]", "]");
 			name.substring(a2 + 1).replaceAll("\\\\\\[", "[")
 					.replaceAll("\\\\\\]", "]");
 		} catch (Exception e) {
 		}
 		Element result = JsoupFactory.createLink(url, "", A, className()).attr(
 				"data-transition", transition());
 		if (decos.containsKey("prefetch") || decos.containsKey("pref"))
 			result.attr("data-prefetch", "");
 		return result;
 	}
 
 	private String className() { // added 20130703
 		if (decos.containsKey("class"))
 			return decos.getStr("class");
 		return "";
 	}
 
 	private String transition() {
 		// 鐃緒申鐃緒申鐃緒申鐃旬ワ申鐃祝メー鐃緒申鐃緒申鐃�data-transition)鐃緒申鐃緒申鐃緒申僚鐃緒申鐃�
 		// //鐃緒申鐃緒申鐃緒申鐃准￥申鐃緒申鐃舜わ申鐃緒申鐃旬にわ申鐃出縁申鐃緒申鐃銃わ申鐃淑わ申
 		if (decos.containsKey("transition"))
 			return decos.getStr("transition");
 		if (decos.containsKey("trans"))
 			return decos.getStr("trans");
 		return "";
 	}
 
 	private Node FuncObject(String path) {
 		String classID = HTMLEnv.getClassID(this);
 		Element result = null;
 
 		if (path.equals("")) {
 			try {
 				path = ((FuncArg) this.Args.get(0)).getStr().trim();
 			} catch (Exception e) {
 			}
 		}
 
 		// 各引数毎に処理した結果をHTMLに書きこむ
 		if (path.endsWith(".php")) { // .php file
 			String phpCode = "";
 			BufferedReader in;
 			try {
 				in = new BufferedReader(new InputStreamReader(
 						new FileInputStream(path), "UTF-8"));
 				String line = null;
 				while (true) {
 					line = in.readLine();
 					if (line == null)
 						break;
 					else
 						phpCode += line + "\n";
 				}
 				in.close();
 				return new TextNode(phpCode, "");
 			} catch (Exception e) {
 				System.err.println("<Warning> Can't open '" + path + "'.");
 			}
 		} else if (path.endsWith(".js")) // .js file
 			result = JsoupFactory.createJsElement(path);
 		else
 			result = new Element(Tag.valueOf("object"), "").attr("data", path)
 					.addClass(classID).html("\n");
 		// .html, .pdf, .swf, .gif, .mp4, etc.
 		return result;
 	}
 
 
     /*  SEQ_NUM( [Start number [, ASC or DESC] ] )  */
     private Node Func_seq_num() {
         classID = HTMLEnv.getClassID(this);
         int i;
         for(i=0; i<seq_num_ClassID.size()+1; i++){
             try{
                 if(classID.equals(seq_num_ClassID.get(i)))
                     break;
             }catch(Exception e1){
                 seq_num_ClassID.add(i, classID);
                 try{
                     //第一引数 Start number
                     seq_num_startNum.add(i, Integer.parseInt(getValue(1)));
                     //第二引数 ASC or DESC
                     if(getValue(2).toLowerCase().trim().equals("desc"))	seq_num_DESC_Flg.add(i, true);
                     else                                               	seq_num_DESC_Flg.add(i, false);
                 }catch(Exception e2){
                     seq_num_startNum.add(i, 1);        //default: 1
                     seq_num_DESC_Flg.add(i, false);    //default: false
                 }
                 seq_num.add(i, seq_num_startNum.get(i));
                 break;
             }
         }
         
         // 各引数毎に処理した結果をHTMLに書きこむ
 //        htmlEnv.code.append(""+((!seq_num_DESC_Flg.get(i))? (seq_num.get(i)):(seq_num.get(i))));
         if(!seq_num_DESC_Flg.get(i))    seq_num.set(i,seq_num.get(i)+1);
         else                    		seq_num.set(i,seq_num.get(i)-1);
         return null;
     }
     //seq_num end
     //added by goto 20130914  "SEQ_NUM"
     static void Func_seq_num_initialization() {    //initialize seq_num
         try{
         	for(int i=0; i<seq_num_ClassID.size(); i++){
         		if(seq_num_ClassID.get(i).equals(classID)){
         			seq_num.set(i, seq_num_startNum.get(i));	//replace
         			break;
         		}
         	}
         }catch(Exception e){}
         return;
     }
 
 	// // for practice 2012/02/09
 	// private void Func_button() {
 	// String statement ="";
 	// String button_media = this.getArgs().get(0).toString();
 	// if (button_media.equals("\"goback\"")){
 	// // 鐃緒申鐃旬ワ申鐃緒申鐃緒申鐃緒申鐃緒申鐃緒申鐃�// statement =
 	// "<form><INPUT type=\"button\" onClick='history.back();' value=\"鐃緒申鐃�"></form>";
 	// }else if(button_media.equals("\"bookmark\"")){
 	// // 鐃緒申鐃緒申鐃祝ブッワ申鐃殉￥申鐃緒申鐃緒申鐃緒申魑�劼鐃緒申鐃�// }else
 	// if(button_media.equals("\"facebook\"")){
 	// // facebook鐃塾わ申鐃緒申鐃粛￥申鐃旬ワ申鐃緒申僚鐃緒申鐃薯記述わ申鐃緒申
 	// }else{
 	// // 鐃獣に誌申鐃所が鐃淑わ申鐃緒申鐃緒申鐃緒申椒鐃緒申鐃祝わ申鐃緒申
 	// statement =
 	// "<form><INPUT type=\"button\" onClick='history.back();' value=\"鐃緒申鐃�"></form>";
 	// }
 	// // 鐃銃逸申鐃緒申鐃祝緒申鐃緒申鐃縮わ申HTML鐃祝書きわ申鐃緒申
 	// html_env.code.append(statement);
 	// return;
 	// }
 
 	private Element FuncFormCommon(String s) {
 		Element result = new Element(Tag.valueOf("div"), "");
 
 		Node form = new TextNode("", "");
 
 		boolean openFormInThis = false;
 
 		if (!HTMLEnv.getFormItemFlg()) {
 			form = createForm(decos);
 			openFormInThis = true;
 		}
 
 		HTMLEnv.setFormItemFlg(true, s);
 
 		String att = new String();
 		Integer attNo = 1;
 		while (!this.getAtt("att" + attNo).equals("")) {
 			if (attNo > 1)
 				att += ",";
 			att += this.getAtt("att" + attNo);
 			Log.out("att:" + att + " attNo:" + attNo);
 			attNo++;
 		}
 		if (attNo == 1 && !this.getAtt("att").equals("")) {
 			att += this.getAtt("att");
 			Log.out("att:" + att + " attNo:" + attNo);
 		}
 
 		if (!this.getAtt("name").equals("")) {
 			HTMLEnv.setFormPartsName(this.getAtt("name"));
 			HTMLEnv.exFormName();
 		} else {
 			HTMLEnv.setFormPartsName(null);
 		}
 
 		if (!this.getAtt("id").equals("")) {
 			HTMLEnv.nameId = this.getAtt("id");
 		}
 
 		if (!this.getAtt("cond_name").equals("")) {
 			HTMLEnv.condName = this.getAtt("cond_name");
 		}
 		if (!this.getAtt("cond").equals("")) {
 			HTMLEnv.cond = this.getAtt("cond");
 		}
 
 		result.appendChild(form);
 
 		if (this.Args.get(0) instanceof FuncArg) {
 			HTMLEnv.setFormValueString(att);
 			Log.out("ARGS are function");
 			FuncArg fa = (FuncArg) this.Args.get(0);
 			result.appendChild((Node) fa.createNode());
 		} else {
 			result.appendChild((Node) this.createNodeAtt("default"));
 		}
 
 		if (openFormInThis == true) {
 			HTMLEnv.setFormItemFlg(false, null);
 			openFormInThis = false;
 		} else {
 			HTMLEnv.setFormItemFlg(true, null);
 		}
 		return result;
 	}
 
 	private Element createForm() {
 		Element result = new Element(Tag.valueOf("form"), "");
 		String path = new String();
 		if (this.getAtt("path") != null && !this.getAtt("path").isEmpty()) {
 			path = this.getAtt("path").replaceAll("\"", "");
 		} else {
 			path = ".";
 		}
 
 		result.attr("method", "POST").attr("action",
 				path + "/servlet/supersql.form.FormServlet");
 		result.appendChild(JsoupFactory.createInput("hidden", "configfile",
 				path + "/config.ssql"));
 
 		if (this.getAtt("link") != null && !this.getAtt("link").isEmpty()) {
 			result.appendChild(JsoupFactory.createInput("hidden", "sqlfile",
 					path + "/" + this.getAtt("link").replaceAll("\"", "")));
 		} else if (this.getAtt("linkfile") != null
 				&& !this.getAtt("linkfile").isEmpty()) {
 			result.appendChild(JsoupFactory.createInput("hidden", "sqlfile",
 					path + "/" + this.getAtt("linkfile").replaceAll("\"", "")));
 		}
 
 		if (this.getAtt("cond") != null && !this.getAtt("cond").isEmpty()) {
 			if (!this.getAtt("cond").replaceAll("\"", "").isEmpty())
 				result.appendChild(JsoupFactory.createInput("hidden", "cond1",
 						this.getAtt("cond").replaceAll("\"", "")));
 		}
 
 		String att = new String();
 		Integer attNo = 1;
 		while (!this.getAtt("att" + attNo).equals("")) {
 			if (attNo > 1)
 				att += ",";
 			att += this.getAtt("att" + attNo);
 			attNo++;
 			Log.out("att:" + att + " attNo:" + attNo);
 		}
 
 		if (attNo == 1 && !this.getAtt("att").equals("")) {
 			att += this.getAtt("att");
 			Log.out("att:" + att + " attNo:" + attNo);
 		}
 
 		if (this.getAtt("update") != null && !this.getAtt("update").isEmpty()) {
 			result.appendChild(JsoupFactory.createInput("hidden", "updatefile",
 					path + "/" + this.getAtt("update").replaceAll("\"", "")
 							+ "(" + att + ")"));
 		} else if (this.getAtt("updatefile") != null
 				&& !this.getAtt("updatefile").isEmpty()) {
 			result.appendChild(JsoupFactory.createInput("hidden", "updatefile",
 					path + "/" + this.getAtt("updatefile").replaceAll("\"", "")
 							+ "(" + att + ")"));
 		}
 		return result;
 	}
 
 	protected static Element createForm(DecorateList decos) {
 		Element result = new Element(Tag.valueOf("form"), "");
 		String path = new String();
 		if (decos.containsKey("path")) {
 			path = decos.getStr("path").replaceAll("\"", "");
 		} else {
 			path = ".";
 		}
 
 		result.attr("method", "POST");
 		result.attr("action", path + "/supersql.form.FormServlet");
 		result.attr("name", HTMLEnv.getFormName());
 
 		result.appendChild(JsoupFactory.createInput("hidden", "configFile",
 				GlobalEnv.getFileDirectory() + "/config.ssql"));
 
 		if (decos.containsKey("link")) {
 			opt(decos.getStr("link"));
 			result.appendChild(JsoupFactory.createInput("hidden", "sqlfile",
 					path + "/" + decos.getStr("link").replaceAll("\"", "")));
 		}
 
 		if (decos.containsKey("cond")) {
 			result.appendChild(JsoupFactory.createInput("hidden", "cond1", path
 					+ "/" + decos.getStr("link").replaceAll("\"", "")));
 		}
 
 		if (decos.containsKey("updatefile")) {
 			result.appendChild(JsoupFactory.createInput("hidden", "updateFile",
 					path + "/" + opt(decos.getStr("updatefile"))));
 		}
 		if (decos.containsKey("linkfile")) {
 			opt(decos.getStr("linkfile"));
 			result.appendChild(JsoupFactory.createInput("hidden", "linkfile",
 					path + "/" + decos.getStr("linkfile").replaceAll("\"", "")));
 		}
 		if (decos.containsKey("cond")) {
 			result.appendChild(JsoupFactory.createInput("hidden", "linkcond",
 					decos.getStr("cond").replaceAll("\"", "")));
 		}
 		return result;
 	}
 
 	public static String opt(String s) {
 		if (s.contains("\"")) {
 			s = s.replaceAll("\"", "");
 		}
 		if (s.startsWith("./")) {
 			s = s.substring(2, s.length());
 		}
 		if (s.startsWith("/")) {
 			s = s.substring(1, s.length());
 		}
 		return s;
 	}
 
 	private Element funcYoutube() {
 		checkArgsNumber(1);
 		FuncArg src = Args.get(0);
 		checkArgType(src, 0, HTMLC0.class);
 		String srcString = "http://www.youtube.com/embed/"
 				+ ((Element) src.createNode()).text();
 
 		Element iframe = new Element(Tag.valueOf("iframe"), "");
 		iframe.attr("allowFullScreen");
 		iframe.attr("frameborder", "0");
 		iframe.attr("src", srcString);
 		iframe.attributes().addAll(getAttributes());
 
 		if (decos.containsKey("width"))
 			iframe.attr("width", decos.getStr("width"));
 		else
 			iframe.attr("width", "560");
 
 		if (decos.containsKey("height"))
 			iframe.attr("height", decos.getStr("height"));
 		else
 			iframe.attr("height", "315");
 
 		return iframe;
 	}
 
 	private Element funcVideoFile() {
 		checkArgsNumber(1);
 		FuncArg src = Args.get(0);
 		checkArgType(src, 0, HTMLC1.class, HTMLC0.class);
 
 		Element video = new Element(Tag.valueOf("video"), "");
 
 		for (Element e : ((Element) src.createNode()).getElementsByClass("att")) {
 			String srcString = e.text();
 			String extension = srcString.split("\\.")[srcString.split("\\.").length - 1];
 			video.appendElement("source").attr("type", "video/" + extension)
 					.attr("src", srcString);
 		}
 
 		video.append("Your browser does not support the video tag.");
 
 		video.attributes().addAll(getAttributes());
 
 		return video;
 	}
 
 	private Element funcMeter() {
 		/*
 		 * <div><script type="text/javascript">addEvent(window,"load",function()
 		 * { var layout = [ 200, //width 40, //height 170, //m_width 25,
 		 * //m_height 100, //max 0, //min 30, //low 70 //high ] var color = [
 		 * "#FF0000", //max_color "#00FFFF", //low_color "#00FFFF", //high_color
 		 * "#00FF00", //mid_color "#CCCCCC" //bg_color ]
 		 * draw("meter2",layout,color,70); });</script>
 		 * 
 		 * <canvas width="200" height="40" id="meter2"></canvas></div>
 		 */
 		meterId++;
 		checkArgsNumber(1);
 		FuncArg attribute = Args.get(0);
 		checkArgType(attribute, 0, HTMLAttribute.class);
 
 		Element result = new Element(Tag.valueOf("div"), "");
 		Element script = new Element(Tag.valueOf("script"), "").attr("type",
 				"text/javascript");
 		Element canvas = new Element(Tag.valueOf("canvas"), "");
 		String scriptString = "window.onload = function() { \nvar layout = [";
 
 		canvas.attr("id", "meter" + meterId);
 
 		HashMap<String, String> propertiesHashMap = new HashMap<String, String>();
 		{ // The order in which we put the values here is important.
 			propertiesHashMap.put("width", "200");
 			propertiesHashMap.put("height", "40");
 			propertiesHashMap.put("m_width", "170");
 			propertiesHashMap.put("m_height", "25");
 			propertiesHashMap.put("max", "100");
 			propertiesHashMap.put("min", "0");
 			propertiesHashMap.put("low", "30");
 			propertiesHashMap.put("high", "70");
 			propertiesHashMap.put("max_color", "#FF0000");
 			propertiesHashMap.put("low_color", "#00FFFF");
 			propertiesHashMap.put("high_color", "#00FFFF");
 			propertiesHashMap.put("mid_color", "#00FF00");
 			propertiesHashMap.put("bg_color", "#CCCCCC");
 		}
 
 		for (String deco : decos.keySet()) {
 			if (propertiesHashMap.keySet().contains(deco))
 				propertiesHashMap.put(deco, decos.getStr(deco));
 		}
 
 		for (Entry<String, String> currentProperty : propertiesHashMap
 				.entrySet()) {
 			String key = currentProperty.getKey();
 			String value = currentProperty.getValue();
 			scriptString += "\"" + value + "\"";
 			if (key.equalsIgnoreCase("high"))
 				scriptString += "]; \nvar color = [";
 			else
 				scriptString += ",";
 			if (key.equalsIgnoreCase("width") || key.equalsIgnoreCase("height"))
 				canvas.attr(key, value);
 		}
 
 		scriptString += "]; \ndraw(\"meter" + meterId + "\",layout,color,";
 		scriptString += ((Element) (attribute.createNode())).text();
 		scriptString += ");}</script>";
 		script.appendChild(new DataNode(scriptString, ""));
 
 		result.appendChild(canvas);
 		HTMLEnv.getHtmlEnv1().head()
 				.appendChild(JsoupFactory.createJsElement("js/canvas.js"));
 		HTMLEnv.getHtmlEnv1().head().appendChild(script);
 
 		return result;
 	}
 
 	// 20130920
 	private String getValue(int x) {
 		try {
 			String str = ((FuncArg) this.Args.get(x - 1)).getStr(); // 第x引数
 			if (!str.equals(""))
 				return str;
 			else
 				return "";
 		} catch (Exception e) {
 			return "";
 		}
 	}
 }
