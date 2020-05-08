 package cm.model;
 
 import java.awt.Color;
 
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 /**
  * Defines a D&D 4e creature power.
  * @author matthew.rinehart
  *
  */
 public class Power {
 	private String _name = "", _typeCode = "", _action = "", _keywords = "", _descCoded = "", _url = "";
 	private Integer _aura = 0;
 	
 	/**
 	 * A blank power.
 	 */
 	public Power() {
 		clearAll();
 	}
 
 	/**
 	 * A new power with the provided properties.
 	 * @param name the name
 	 * @param type the full type
 	 * @param action the action
 	 * @param keywords the keywords
 	 * @param desc the description
 	 * @param aura the size of the aura
 	 */
 	public Power(String name, String type, String action, String keywords, String desc, Integer aura) {
 		setName(name);
 		setType(type);
 		setAction(action);
 		setKeywords(keywords);
 		setDesc(desc);
 		setAura(aura);
 	}
 
 	/**
 	 * A power created from RTF data.
 	 * @param line1 RTF data
 	 * @param type power type
 	 */
 	public Power (String line1, String type) {
 		detailImport(line1, type);
 	}
 
 	/**
 	 * A power created as a copy of another power.
 	 * @param pow the source power
 	 */
 	public Power (Power pow) {
 		copy(pow);
 	}
 
 	/**
 	 * Returns the power's action
 	 * @return the action
 	 */
 	public String getAction() {
 		return _action;
 	}
 
 	/**
 	 * Sets the power's action
 	 * @param action the action
 	 */
 	public void setAction(String action) {
 		_action = action;
 	}
 
 	/**
 	 * Returns recharge dice in the DnD4Attack font
 	 * @return recharge dice
 	 */
 	private String getActionDiceHTML() {
 		String out = "";
 		out += getAction();
 	    out.replace("recharge 6", "recharge <font face='DnD4Attack'>6</font>");
 	    out.replace("recharge 5", "recharge <font face='DnD4Attack'>5 6</font>");
 	    out.replace("recharge 4", "recharge <font face='DnD4Attack'>4 5 6</font>");
 	    out.replace("recharge 3", "recharge <font face='DnD4Attack'>3 4 5 6</font>");
 	    out.replace("recharge 2", "recharge <font face='DnD4Attack'>2 3 4 5 6</font>");
 	    return out;
 	}
 
 	/**
 	 * Returns an action line, indicating the power usage frequency or aura-ness.
 	 * @return the action line
 	 */
 	public String getActionLine() {
 		String line = "";
 		
 		if (isAura()) {
 			line += "aura " + getAura().toString();
 		} else if (getAction().isEmpty()) {
 			return "  ";
 		} else if (getAction().toLowerCase().contains("recharges")) {
 			line += "recharge *";
 		} else if (_action.toLowerCase().contains("recharge ")) {
 			line += "recharge ";
 			if (Integer.valueOf(getAction().substring(getAction().indexOf("recharge" ) + 9)) > 0) {
 				line += getAction().substring(getAction().indexOf("recharge" ) + 9);
 			} else {
 				line += "*";
 			}
 		} else if (getAction().toLowerCase().contains("encounter")) {
 			line += "encounter";
 		} else if (getAction().toLowerCase().contains("at-will")) {
 			line += "at-will";
 		} else if (getAction().toLowerCase().contains("daily")) {
 			line += "daily";
 		} else if (getAction().toLowerCase().contains("item")) {
 			line += "item";
 		} else {
 			line += "special";
 		}
 		
 		return " (" + line + ")";
 	}
 
 	/**
 	 * Indicates if the power is an action point placeholder.
 	 * @return true if the power's name contains "action point"
 	 */
 	public Boolean isActionPoint() {
 		return (getName().toLowerCase().contains("action point"));
 	}
 
 	/**
 	 * Indicates if the power is an aura.
 	 * @return true if there is a defined aura for this power
 	 */
 	public Boolean isAura() {
 		return (getAura() > 0);
 	}
 	
 	/**
 	 * Returns the size of the power's aura.
 	 * @return the aura size
 	 */
 	private Integer getAura() {
 		return _aura;
 	}
 
 	/**
 	 * Sets the size of the power's aura.
 	 * @param aura the aura size
 	 */
 	private void setAura(Integer aura) {
 		_aura = aura;
 	}
 
 	/**
 	 * Returns the background {@link Color}.
 	 * @return the background {@link Color}
 	 */
 	public Color getBackColor() {
 		if (isAura()) {
 			return Color.BLUE;
 		}
 		
 		if (getAction().toLowerCase().contains("recharge")) {
 			return Color.ORANGE;
 		} else if (getAction().toLowerCase().contains("encounter")) {
 			return Color.RED;
 		} else if (getAction().toLowerCase().contains("daily")) {
 			return Color.BLACK;
 		} else if (getAction().toLowerCase().contains("at-will")) {
 			return Color.GREEN;
 		} else {
 			return Color.GRAY;
 		}
 	}
 
 	/**
 	 * Returns the description of the power.
 	 * @return the power description
 	 */
 	public String getDesc() {
 		return getDescCoded().replace("###", "\n");
 	}
 
 	/**
 	 * Sets the power's description.
 	 * @param value the description
 	 */
 	public void setDesc(String value) {
 		setDescCoded(value.replace("\n", "###"));
 	}
 
 	/**
 	 * Returns the coded description.
 	 * @return the coded description
 	 */
 	private String getDescCoded() {
 		return _descCoded;
 	}
 
 	/**
 	 * Sets the coded description.
 	 * @param descCoded the coded description
 	 */
 	private void setDescCoded(String descCoded) {
 		_descCoded = descCoded;
 	}
 	
 	/**
 	 * Returns an HTML version of the power description with dice expressions hyperlinked.
 	 * @return an HTML-description
 	 */
 	public String getDescHTML() {
 		String out = getDescCoded();
 		return out.replaceAll("###", "<br>").replaceAll("(([0-9]+)d([0-9]+))? *?[+] *?([0-9]+)", "<a href=\"#$0\">$0</a>");
 	}
 
 	/**
 	 * Indicates if the power is a daily usage frequency.
 	 * @return true if the power's action contains "daily"
 	 */
 	public Boolean isDaily() {
 		return (getAction().toLowerCase().contains("daily"));
 	}
 
 	/**
 	 * Indicates if the power is an item power.
 	 * @return true if the power's action contains "item"
 	 */
 	public Boolean isItem() {
 		return (getAction().toLowerCase().contains("item"));
 	}
 
 	/**
 	 * Returns the foreground {@link Color}.
 	 * @return the foreground {@link Color}
 	 */
 	public Color getForeColor() {
 		if (isAura()) {
 			return Color.WHITE;
 		}
 		
 		if (getAction().toLowerCase().contains("recharge")) {
 			return Color.WHITE;
 		} else if (getAction().toLowerCase().contains("encounter")) {
 			return Color.WHITE;
 		} else if (getAction().toLowerCase().contains("daily")) {
 			return Color.WHITE;
 		} else if (getAction().toLowerCase().contains("at-will")) {
 			return Color.WHITE;
 		} else {
 			return Color.BLACK;
 		}
 	}
 
 	/**
 	 * Returns the power keywords
 	 * @return the keywords
 	 */
 	public String getKeywords() {
 		return _keywords;
 	}
 
 	/**
 	 * Sets the power's keywords
 	 * @param keywords the keywords
 	 */
 	public void setKeywords(String keywords) {
 		_keywords = keywords;
 	}
 
 	/**
 	 * Returns the power name
 	 * @return the name
 	 */
 	public String getName() {
 		return _name;
 	}
 
 	/**
 	 * Sets the power's name
 	 * @param name the name
 	 */
 	public void setName(String name) {
 		_name = name;
 	}
 
 	/**
 	 * Returns HTML-formatted power details.
 	 * @return HTML power details
 	 */
 	public String getPowerHTML() {
 		String out = "";
 		
 		if (getAura() > 0) {
 			out += "<div class='ggmed'><b>" + _name + "</b>";
 			if (!getKeywords().isEmpty()) {
 				out += " (" + getKeywords() + ")";
 			}
 			out += " Aura " + getAura().toString() + "; ";
 			out += "</div>";
 			out += "<div class='gglt'><div class='ggindent'>";
 			out += getDescHTML();
 			out += "</div></div>";
 		} else {
 			out += "<div class='ggmed'>";
 			if (!getTypeCode().isEmpty()) {
 				out += "<font face='DnD4Attack'>" + getTypeCode() + "</font> ";
 			}
 			out += "<b>" + _name.replace("*", "&bull;") + "</b>";
 			if (!getAction().isEmpty()) {
 				out += " (" + getActionDiceHTML() + ")";
 			}
 			if (!_keywords.isEmpty()) {
 				out += " &bull;  " + getKeywords();
 			}
 			out += "</div>";
 			if (!getDescCoded().isEmpty()) {
 				out += "<div class='gglt'><div class='ggindent'>";
 				out += getDescHTML();
 				out += "</div></div>";
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Indicates if the power is a psionic power point placeholder.
 	 * @return true if the power's action contains "power point"
 	 */
 	private Boolean isPowerPoint() {
 		return (getAction().toLowerCase().contains("power point"));
 	}
 
 	/**
 	 * Returns the required d6 result to recharge this power.
 	 * @return the recharge value
 	 */
 	public Integer getRechargeVal() {
 		if (getAction() == null || getAction().contentEquals("")) {
 			return 0;
 		} else if (getAction().toLowerCase().contains("recharge 2")) {
 			return 2;
 		} else if (getAction().toLowerCase().contains("recharge 3")) {
 			return 3;
 		} else if (getAction().toLowerCase().contains("recharge 4")) {
 			return 4;
 		} else if (getAction().toLowerCase().contains("recharge 5")) {
 			return 5;
 		} else if (getAction().toLowerCase().contains("recharge 6")) {
 			return 6;
 		} else {
 			return 0;
 		}
 	}
 
 	/**
 	 * Returns the power type, e.g. "Basic Melee", "Close", etc.
 	 * @return the power type
 	 */
 	public String getType() {
 		if (isAura()) {
 			return "Aura " + _aura.toString();
 		} else if (getTypeCode().contentEquals("m")) {
 			return "Basic Melee";
 		} else if (getTypeCode().contentEquals("M")) {
 			return "Melee";
 		} else if (getTypeCode().contentEquals("r")) {
 			return "Basic Ranged";
 		} else if (getTypeCode().contentEquals("R")) {
 			return "Ranged";
 		} else if (getTypeCode().contentEquals("A")) {
 			return "Area";
 		} else if (getTypeCode().contentEquals("C")) {
 			return "Close";
 		} else if (getTypeCode().isEmpty()) {
 			return "(no icon)";
 		} else {
 			return "???";
 		}
 	}
 	
 	/**
 	 * Sets the type of the power.
 	 * @param value the type
 	 */
 	public void setType(String value) {
 		String type = value.trim().toLowerCase();
 		
 		if (type.startsWith("aura")) {
			setAura(Integer.valueOf(type.replace("aura", "").trim()));
 			setTypeCode("");
 		} else if (type.contentEquals("basic melee")) {
 			setTypeCode("m");
 		} else if (type.contentEquals("melee")) {
 			setTypeCode("M");
 		} else if (type.contentEquals("basic ranged")) {
 			setTypeCode("r");
 		} else if (type.contentEquals("ranged")) {
 			setTypeCode("R");
 		} else if (type.contentEquals("area")) {
 			setTypeCode("A");
 		} else if (type.contentEquals("close")) {
 			setTypeCode("C");
 		} else {
 			setTypeCode("");
 		}
 	}
 
 	/**
 	 * Returns the single-letter type code for the power. For example, a basic
 	 * ranged attack has code 'r'.
 	 * @return the type code
 	 */
 	private String getTypeCode() {
 		return _typeCode;
 	}
 
 	/**
 	 * Sets the type code of the power.
 	 * @param typeCode the type code
 	 */
 	private void setTypeCode(String typeCode) {
 		_typeCode = typeCode;
 	}
 
 	/**
 	 * Returns the power's information URL
 	 * @return the URL
 	 */
 	public String getURL() {
 		return _url;
 	}
 
 	/**
 	 * Sets the power's URL
 	 * @param url the URL
 	 */
 	public void setURL(String url) {
 		_url = url;
 	}
 
 	/**
 	 * Resets all power properties to default values (blank).
 	 */
 	private void clearAll() {
 		setName("(unnamed power)");
 		setTypeCode("");
 		setAction("");
 		setKeywords("");
 		setDesc("(no description)");
 		setAura(0);
 	}
 
 	/**
 	 * Sets this power's properties from another power.
 	 * @param pow the other power
 	 */
 	private void copy(Power pow) {
 		setName(pow.getName());
 		setTypeCode(pow.getTypeCode());
 		setAction(pow.getAction());
 		setKeywords(pow.getKeywords());
 		setDescCoded(pow.getDescCoded());
 		setURL(pow.getURL());
 		setAura(pow.getAura());
 	}
 
 	/**
 	 * Import power data from RTF.
 	 * @param line1 the RTF data
 	 * @param type the power type
 	 */
 	public void detailImport(String line1, String type) {
 		String temp = line1.trim();
 		
 		if (temp.endsWith("#")) {
 			temp = temp.replace("#", "").trim();
 			setAura(0);
 			
 			if (!getDescCoded().isEmpty()) {
 				setDescCoded(getDescCoded() + "###" + temp);
 			} else {
 				setDescCoded(temp);
 			}
 			
 			if (temp.startsWith("@B ") || temp.startsWith("@O ")
 					|| temp.startsWith("@M ") || temp.startsWith("@m ")
 					|| temp.startsWith("@R ") || temp.startsWith("@r ")
 					|| temp.startsWith("@A ") || temp.startsWith("@C ")) {
 				String action = "";
 				String keywords = "";
 				String name = "";
 				if (temp.startsWith("@O ")) {
 					setAura((int) Math.round(Double.valueOf(temp.substring(temp.indexOf("*") + 7).trim())));
 					if (temp.contains("Aura " + getAura().toString() + " ")) {
 						setDescCoded(temp.substring((temp.indexOf("*") + 9)).trim());
 					}
 				}
 				if (temp.startsWith("@M ") || temp.startsWith("@m ")
 						|| temp.startsWith("@R ") || temp.startsWith("@r ")
 						|| temp.startsWith("@A ") || temp.startsWith("@C ")) {
 					setTypeCode(temp.substring(temp.indexOf("@") + 1, 1));
 				}
 				if (temp.contains("*")) {
 					action = temp.substring(temp.indexOf("*") + 2);
 				}
 				if (temp.contains("(")) {
 					name = temp.substring(3, temp.indexOf("(") - 3).trim();
 					keywords = temp.substring(temp.indexOf("(") + 1, temp.indexOf(")") - temp.indexOf("(") - 1);
 				} else if (temp.contains("*")) {
 					name = temp.substring(3, temp.indexOf("*") - 3).trim();
 				} else {
 					name = temp.substring(3).trim();
 				}
 				setAction(action.toLowerCase());
 				setKeywords(keywords);
 				setName(name);
 				if (type.contentEquals("standard") || 
 						type.contentEquals("move") ||
 						type.contentEquals("minor") || 
 						type.contentEquals("free") || 
 						type.contentEquals("triggered")) {
 					if(getAction().isEmpty()) {
 						setAction(type);
 					} else {
 						setAction(type + "; " + getAction());
 					}
 				} else if (getName().isEmpty()) {
 					setName(temp);
 				} else if (!getDescCoded().isEmpty()) {
 					if (getDescCoded().endsWith("~")) {
 						setDescCoded(getDescCoded().replace("~", " "));
 					} else {
 						setDescCoded(getDescCoded() + "###");
 					}
 					if (temp.toLowerCase().startsWith("attack")) {
 						setDescCoded(getDescCoded() + temp + "~");
 					} else {
 						setDescCoded(getDescCoded() + temp);
 					}
 				} else if (temp.toLowerCase().startsWith("attack")) {
 					setDescCoded(temp + "~");
 				} else {
 					setDescCoded(temp);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Writes the power to an XML stream
 	 * @param writer the XML stream
 	 * @throws XMLStreamException from the writer
 	 */
 	public void exportXML(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeStartElement("power");
 		
 		writer.writeStartElement("name");
 		writer.writeCharacters(getName());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("type");
 		writer.writeCharacters(getType());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("act");
 		writer.writeCharacters(getAction());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("key");
 		writer.writeCharacters(getKeywords());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("desc");
 		writer.writeCharacters(getDescCoded());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("url");
 		writer.writeCharacters(getURL());
 		writer.writeEndElement();
 		
 		writer.writeEndElement();
 	}
 	
 	/**
 	 * Sets this power's properties from an XML stream
 	 * @param reader the XML stream
 	 * @return true on success
 	 * @throws XMLStreamException from the reader
 	 */
 	public Boolean importXML(XMLStreamReader reader) throws XMLStreamException {
 		String elementName = "";
 		
 		if (reader.isStartElement() && reader.getName().toString().contentEquals("power")) {
 			clearAll();
 			
 			while(reader.hasNext()) {
 				reader.next();
 				
 				if (reader.isStartElement()) {
 					elementName = reader.getName().toString();
 				} else if (reader.isCharacters()) {
 					if (elementName.contentEquals("name")) {
 						setName(reader.getText());
 					} else if (elementName.contentEquals("type")) {
 						setType(reader.getText());
 					} else if (elementName.contentEquals("act")) {
 						setAction(reader.getText());
 					} else if (elementName.contentEquals("key")) {
 						setKeywords(reader.getText());
 					} else if (elementName.contentEquals("desc")) {
 						setDescCoded(reader.getText());
 					} else if (elementName.contentEquals("url")) {
 						setURL(reader.getText());
 					}
 				} else if (reader.isEndElement()) {
 					elementName = "";
 					if (reader.getName().toString().contentEquals("power")) {
 						return true;
 					}
 				}
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Returns {@link #getName()}.
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 		return getName();
 	}
 }
 /*
     Public ReadOnly Property Power_RTF_Out() As String
         Get
             Dim output As New System.Text.StringBuilder
 
             If nAura > 0 Then
                 ' Add Aura Name
                 output.Append(RTF_Bold(sName))
 
                 If sKeywords <> "" Then
                     output.Append(" (" & sKeywords & ")")
                 End If
 
                 ' Add aura value
                 output.Append(" aura " & CStr(nAura) & "; ")
 
                 ' Add details
                 output.Append(sDesc)
 
                 ' Finish Aura line
                 output.AppendLine("\par")
             Else
                 ' Add Type
                 If sType <> "" Then
                     output.Append(RTF_DnDSymbol(sType) & " ")
                 End If
 
                 ' Add Power Name
                 output.Append(RTF_Bold(sName.Replace("*", "\bullet ")))
 
                 If sAction <> "" Then
                     output.Append(" (" & sActionDice & ")")
                 End If
 
                 ' Add Keywords
                 If sKeywords <> "" Then
                     output.Append(" \bullet  " & sKeywords)
                 End If
 
                 ' Finish First line
                 output.AppendLine("\par")
 
                 ' Create Second Line
                 If sDesc <> "" Then
                     output.Append("  " & sDesc)
                     output.AppendLine("\par")
                 End If
             End If
 
             output.Replace("###", "\par" & ControlChars.NewLine & "  ")
 
             Return output.ToString
         End Get
     End Property
     Private ReadOnly Property sActionDice() As String
         Get
             Dim line As New System.Text.StringBuilder
             line.AppendLine(sAction)
 
             line.Replace("recharge 6", "recharge " & RTF_DnDSymbol("6"))
             line.Replace("recharge 5", "recharge " & RTF_DnDSymbol("5 6"))
             line.Replace("recharge 4", "recharge " & RTF_DnDSymbol("4 5 6"))
             line.Replace("recharge 3", "recharge " & RTF_DnDSymbol("3 4 5 6"))
             line.Replace("recharge 2", "recharge " & RTF_DnDSymbol("2 3 4 5 6"))
 
             Return line.ToString
         End Get
     End Property
 End Class
 */
