 /* OpenMark online assessment system
    Copyright (C) 2007 The Open University
 
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
 package om.stdcomponent;
 
 import java.util.List;
 
 import om.OmDeveloperException;
 import om.OmException;
 import om.OmFormatException;
 import om.OmUnexpectedException;
 import om.question.ActionParams;
 import om.stdquestion.QComponent;
 import om.stdquestion.QContent;
 import om.stdquestion.QDocument;
 
 import org.w3c.dom.Element;
 
 import util.misc.Strings;
 import util.xml.XML;
 
 /**
  * A text field that can allow the user to enter superscripts and subscripts, or
  * performs automatic subscript formatting for chemical formulae.<br/>
  * Subscript and superscript modes can be changed using linked checkboxes or by
  * using the up and down arrow keys. <br/>
  * The <b>plain</b> text version of the control asks the user to type the sub
  * and sup tags themselves and in the chemical formula mode to ignore the
  * formatting. <br/>
  * 
  * <h2>XML usage</h2> &lt;advancedfield id='reaction' type='superscript' /&gt;
  * <h2>Properties</h2>
  * <table border="1">
  * <tr>
  * <th>Property</th>
  * <th>Values</th>
  * <th>Effect</th>
  * </tr>
  * <tr>
  * <td>id</td>
  * <td>(string)</td>
  * <td>Specifies unique ID</td>
  * </tr>
  * <tr>
  * <td>display</td>
  * <td>(boolean)</td>
  * <td>Includes in/removes from output</td>
  * </tr>
  * <tr>
  * <td>enabled</td>
  * <td>(boolean)</td>
  * <td>Activates/deactivates this control</td>
  * </tr>
  * <tr>
  * <td>lang</td>
  * <td>(string)</td>
  * <td>Specifies the language of the content, like the HTML lang attribute. For
  * example 'en' = English, 'el' - Greek, ...</td>
  * </tr>
  * <tr>
  * <td>cols</td>
  * <td>(integer)</td>
  * <td>Number of columns (approx) to allow space for the component</td>
  * </tr>
  * <tr>
  * <td>value</td>
  * <td>(string)</td>
  * <td>Current value of field. (See below)</td>
  * </tr>
  * <tr>
  * <td>type</td>
  * <td>(string; 'superscript' | 'subscript' | 'both' | 'chem')</td>
  * <td>Type of field.</td>
  * </tr>
  * </table>
  * <br/>
  * If type='chem' is specified then there are no subscript or subscript
  * checkboxes and the text is automatically displayed formatted with subscripts
  * appropriate to chemical formulae eg "3H<sub>2</sub>O + 2CO<sub>2</sub>". The
  * value returned does <b>not</b> include the formatting tags in this case. <br/>
  * For types other than 'chem', the value returned includes the <b>sub</b> and
  * <b>sup</b> elements for subscripts and superscripts. Thus 10<sup>3</sup> is
  * returned as '10&lt;sup>3&lt;/sup>'
  * 
  **/
 public class AdvancedFieldComponent extends QComponent implements Labelable {
 
 	private static String DIV = "div";
 
 	static String TEXT_AREA_REF = "elm1";
 
 	private static String CLASS = "class";
 
 	private static String ADVANCED_FIELD = "advancedfield";
 
 	protected static String SUPERSCRIPT = "superscript";
 
 	protected static String BOTH = "both";
 
 	protected static String SUBSCRIPT = "subscript";
 
 	private static String BR = "br";
 
 	private static String NEW_LINE = "\n";
 
 	private static String INPUT = "input";
 
 	private static String TYPE = "type";
 
 	private static String HIDDEN = "hidden";
 
 	private static String NAME = "name";
 
 	private static String ID = "id";
 
 	private static String VALUE = "value";
 
 	/** Maximum length of single line */
 	private final static int MAXCHARS_ADVANCEDFIELD = 100;
 
 	/** Property name for value of editfield */
 	public final static String PROPERTY_VALUE = "value";
 	/** Number of columns */
 	public static final String PROPERTY_COLS = "cols";
 	/** Label text */
 	public static final String PROPERTY_LABEL = "label";
 	/** Type of editfield (affects JS) */
 	public final static String PROPERTY_TYPE = "type";
 
 	private boolean bGotLabel = false;
 
 	private boolean appliedTinyMCEJavascript = false;
 
 	private boolean appliedTinyMCEByAnotherComponentInDocument = false;
 
 	public boolean isAppliedTinyMCEJavascript() {
 		return appliedTinyMCEJavascript;
 	}
 
 	public void setAppliedTinyMCEJavascript(boolean b) {
 		this.appliedTinyMCEJavascript = b;
 	}
 
 	/** @return Tag name (introspected; this may be replaced by a 1.5 annotation) */
 	public static String getTagName() {
 		return ADVANCED_FIELD;
 	}
 
 	@Override
 	protected String[] getRequiredAttributes() {
 		return new String[] { "type" };
 	}
 
 	@Override
 	protected void defineProperties() throws OmDeveloperException {
 		super.defineProperties();
 		defineString(PROPERTY_VALUE);
 		defineInteger(PROPERTY_COLS);
 		defineString(PROPERTY_LABEL);
 		defineString(PROPERTY_TYPE, "superscript|subscript|both|chem");
 		setString(PROPERTY_VALUE, "");
 		setInteger(PROPERTY_COLS, 20);
 	}
 
 	@Override
 	protected void initChildren(Element eThis) throws OmException {
 		if (eThis.getFirstChild() != null)
 			throw new OmFormatException(
 					"<editfield> may not contain other content");
 	}
 
 	@Override
 	public void produceVisibleOutput(QContent qc, boolean bInit, boolean bPlain)
 		throws OmException {
 		determineExistingTinyMCEOutput();
 		SubSupEnum ssb = determineOutputType(getString(PROPERTY_TYPE));
 		if (!(bGotLabel || isPropertySet(PROPERTY_LABEL))) {
 			throw new OmFormatException("<advancedfield> " + getID()
 					+ ": Requires <label> or label=");
 		}
 		if (bPlain) {
 			generateVisibleOutputForPlainDisplay(ssb, qc);
 		} else {
 			generateVisibleOutput(qc, ssb);
 		}
 	}
 
 	/**
 	 * Simply resets the component state so that it is ready for rendering
 	 *  again.
 	 * @author Trevor Hinson
 	 */
 	public void resetIndividualComponentState() throws OmException {
 		appliedTinyMCEJavascript = false;
 		appliedTinyMCEByAnotherComponentInDocument = false;
 	}
 	
 	protected void determineExistingTinyMCEOutput() {
 		boolean hasAlready = false;
 		List<AdvancedFieldComponent> advs = getQDocument()
 			.find(AdvancedFieldComponent.class);
 		if (null != advs ? advs.size() > 0 : false) {
 			x : for (AdvancedFieldComponent adv : advs) {
 				 hasAlready = adv.isAppliedTinyMCEJavascript();
 				 if (hasAlready) {
 					 break x;
 				 }
 			}
 		}
 		if (hasAlready) {
 			appliedTinyMCEJavascript = hasAlready;
 			if (appliedTinyMCEJavascript) {
 				appliedTinyMCEByAnotherComponentInDocument = true;
 			}
 		}
 	}
 
 	protected SubSupEnum determineOutputType(String sType) throws OmException {
 		SubSupEnum enu = null;
 		if (SubSupEnum.superscript.toString().equals(sType)) {
 			enu = SubSupEnum.superscript; 
 		} else if (SubSupEnum.subscript.toString().equals(sType)) {	
 			enu = SubSupEnum.subscript;
 		} else if (SubSupEnum.both.toString().equals(sType)) {
 			enu = SubSupEnum.both;
 		}
 		return enu;
 	}
 
 	protected String determineOutputType(SubSupEnum enu) throws OmException {
 		String queryString = "sub_sup";
 		if (SubSupEnum.superscript.toString().equals(enu.toString())) {
 			queryString = "sup";
 		} else if (SubSupEnum.subscript.toString().equals(enu.toString())) {	
 			queryString = "sub";
 		}
 		return queryString;
 	}
 
 	protected void generateVisibleOutput(QContent qc, SubSupEnum enu)
 		throws OmException {
 		String sType = enu.toString();
 		Element eDiv = qc.createElement(DIV);
 		qc.addInlineXHTML(eDiv);
 		eDiv.setAttribute(CLASS, ADVANCED_FIELD);
 		double dZoom = getQuestion().getZoom();
 		applyTextArea(qc, eDiv, enu, dZoom);
 		applyHiddenInputField(qc, eDiv);
 		applyLineBreaks(qc, eDiv);
 		applyScript(qc, eDiv, sType, dZoom);
 	}
 
 	enum SubSupEnum {
 		
 		superscript, both, subscript
 		
 	}
 
 	protected String capitaliseFirstCharacter(String initial) {
 		return Strings.uppercaseFirstCharacter(initial);
 	}
 
 	private void applyLineBreaks(QContent qc, Element eDiv) {
 		eDiv.appendChild(qc.createElement(BR));
 		eDiv.appendChild(qc.getOutputDocument().createTextNode(NEW_LINE));
 	}
 
 	protected void applyHiddenInputField(QContent qc, Element eDiv)
 		throws OmException {
 		Element eHidden = qc.createElement(INPUT);
 		eDiv.appendChild(eHidden);
 		eHidden.setAttribute(TYPE, HIDDEN);
 		eHidden.setAttribute(NAME, QDocument.ID_PREFIX
 			+ QDocument.VALUE_PREFIX + getID());
 		eHidden.setAttribute(ID, QDocument.ID_PREFIX + QDocument.VALUE_PREFIX
 			+ getID());
 		eHidden.setAttribute(VALUE, getString(PROPERTY_VALUE));
 	}
 
 	protected void applyTextArea(QContent qc, Element eDiv, SubSupEnum enu,
 		double dZoom) throws OmException {
 		if (!appliedTinyMCEByAnotherComponentInDocument) {
 			Element s1 = qc.createElement("script");
 			s1.setAttribute("type", "text/javascript");
 			s1.setAttribute("src", "tiny_mce/tiny_mce_src.js");
 			eDiv.appendChild(s1);
 			setAppliedTinyMCEJavascript(true);
 		}
 		Element s2 = qc.createElement("script");
 		s2.setAttribute("type", "text/javascript");
 		String outputType = determineOutputType(enu);
 		String elements = QDocument.ID_PREFIX + QDocument.OM_PREFIX
 			+ getID() + "_iframe";
 		s2.setAttribute("src", "tiny_mce/tiny_mce_settings.js?"
 			+ "&h=" + "" + (int) (60 * dZoom)
 			+ "&w=" + (int) (10 * dZoom * getInteger(PROPERTY_COLS))
 			+ "&t=" + outputType
 			+ "&e=" + elements
 			+ "&ro=" + isEnabled()
 			+ "&es=" + "om"+getID()+"iframe");
 		s2.setAttribute("defer", "defer");
 		eDiv.appendChild(s2);
 		Element textarea = qc.createElement("textarea");
 		textarea.setAttribute("id", elements);
 		textarea.setAttribute("name", elements);
 		textarea.setAttribute("class", "om"+getID()+"iframe");
 		textarea.setAttribute("mysubtype", outputType);
		textarea.setTextContent(getValue());
 		eDiv.appendChild(textarea);
 		addLangAttributes(textarea);
 	}
 
 	protected void applyScript(QContent qc, Element eDiv, String sType,
 		double dZoom) throws OmException {
 		Element eScript = qc.createElement("script");
 		eDiv.appendChild(eScript);
 		eScript.setAttribute("type", "text/javascript");
 		String sfg = getQuestion().getFixedColourFG();
 		String sbg = getQuestion().getFixedColourBG();
 		if (sbg == null)
 			sbg = "#FFFFFF";
 		if (sfg == null) {
 			if (isEnabled())
 				sfg = "#000000";
 			else
 				sfg = "#999999";
 		}
 		XML.createText(eScript, "addOnLoad( function() { advancedfieldFix('"
 			+ getID() + "','" + QDocument.ID_PREFIX + "',"
 			+ (isEnabled() ? "true" : "false") + ",'" + sType + "',"
 			+ dZoom + ",'" + sfg + "','" + sbg + "'); } );");
 
 		// Can be focused (hopefully)
 //		if (isEnabled()) {
 ////			qc.informFocusableFullJS(QDocument.ID_PREFIX + getID(),
 ////				"document.getElementById('" + QDocument.ID_PREFIX
 ////					+ QDocument.OM_PREFIX + getID()
 ////					+ "_iframe').contentWindow", false);
 //		}
 	}
 
 	protected void generateVisibleOutputForPlainDisplay(SubSupEnum enu,
 			QContent qc) throws OmException {
 		String sType = enu.toString();
 		if (isPropertySet(PROPERTY_LABEL)
 				&& !getString(PROPERTY_LABEL).equals("")) {
 			Element eLabel = qc.createElement("label");
 			qc.addInlineXHTML(eLabel);
 			eLabel.setAttribute("for", QDocument.ID_PREFIX
 					+ QDocument.VALUE_PREFIX + getID());
 			XML.createText(eLabel, getString(PROPERTY_LABEL));
 		}
 		Element eDiv = qc.createElement("div");
 		qc.addInlineXHTML(eDiv);
 		XML
 				.createText(
 						eDiv,
 						"p",
 						"(In the following edit field: "
 								+ ((sType.equals("superscript") || sType
 										.equals("both")) ? " Type { before, and "
 										+ "} after, any text you want superscripted."
 										: "")
 								+ ((sType.equals("subscript") || sType
 										.equals("both")) ? " Type [ before, and "
 										+ "] after, any text you want subscripted."
 										: "")
 								+ ((sType.equals("chem")) ? " Type your answer ignoring subscripting. "
 										+ "For example, two water molecules should be entered as 2H2O."
 										: "") + ")");
 
 		Element eInput = XML.createChild(eDiv, "input");
 		eInput.setAttribute("type", "text");
 		eInput.setAttribute("size", "" + getInteger(PROPERTY_COLS));
 		eInput.setAttribute("name", QDocument.ID_PREFIX
 				+ QDocument.VALUE_PREFIX + getID());
 		eInput.setAttribute("id", QDocument.ID_PREFIX + QDocument.VALUE_PREFIX
 				+ getID());
 		String sValue = getString(PROPERTY_VALUE);
 		sValue = sValue.replaceAll("<sup>", "{");
 		sValue = sValue.replaceAll("</sup>", "}");
 		sValue = sValue.replaceAll("<sub>", "[");
 		sValue = sValue.replaceAll("</sub>", "]");
 		eInput.setAttribute("value", sValue);
 		if (!isEnabled())
 			eInput.setAttribute("disabled", "disabled");
 		addLangAttributes(eInput);
 	}
 
 	/**
 	 * Replaces something repeatedly until it's DEAD. (I think you might need
 	 * this when replacing something where the source string might overlap with
 	 * the next replacement.)
 	 * 
 	 * @param sValue
 	 *            Original string
 	 * @param sFind
 	 *            Thing to find
 	 * @param sReplace
 	 *            Thing to replace
 	 * @return Resulting string
 	 */
 	private static String replaceContinuous(String sValue, String sFind,
 			String sReplace) {
 		while (true) {
 			String sNew = sValue.replaceFirst(sFind, sReplace);
 			if (sNew.equals(sValue))
 				return sNew;
 			sValue = sNew;
 		}
 	}
 
 	@Override
 	protected void formSetValue(String sValue, ActionParams ap)
 			throws OmException {
 		// In plain mode they enter {} and []. We try hard to make it into valid
 		// xhtml
 		// but uh, I don't guarantee it.
 		if (ap.hasParameter("plain")) {
 			// Replace double-opening {[ (nesting is not allowed)
 			sValue = replaceContinuous(sValue, "(\\{[^}]*)\\{", "$1");
 			sValue = replaceContinuous(sValue, "(\\[[^\\]]*)\\[", "$1");
 			// Replace double-closing }]
 			sValue = replaceContinuous(sValue, "(\\}[^{]*)\\}", "$1");
 			sValue = replaceContinuous(sValue, "(\\][^\\[]*)\\]", "$1");
 
 			// Untangle tangled groups { [ becomes { }[
 			sValue = sValue.replaceAll("(\\{[^}]*)\\[", "$1}[");
 			sValue = sValue.replaceAll("(\\[[^\\]]*)\\{", "$1]{");
 			// Add missing closes
 			sValue = sValue.replaceFirst("(\\{[^}]*)$", "$1}");
 			sValue = sValue.replaceFirst("(\\[[^\\]]*)$", "$1]");
 			// Remove extra closes
 			sValue = sValue.replaceFirst("(\\{[^}]*)$", "$1}");
 			sValue = sValue.replaceFirst("(\\[[^\\]]*)$", "$1]");
 
 			// The above might mess up the first part again, so...
 
 			// Replace double-opening {[ (nesting is not allowed)
 			sValue = replaceContinuous(sValue, "(\\{[^}]*)\\{", "$1");
 			sValue = replaceContinuous(sValue, "(\\[[^\\]]*)\\[", "$1");
 			// Replace double-closing }]
 			sValue = replaceContinuous(sValue, "(\\}[^{]*)\\}", "$1");
 			sValue = replaceContinuous(sValue, "(\\][^\\[]*)\\]", "$1");
 
 			// Replace with HTML tags
 			sValue = sValue.replaceAll("\\[", "<sub>");
 			sValue = sValue.replaceAll("\\{", "<sup>");
 			sValue = sValue.replaceAll("\\]", "</sub>");
 			sValue = sValue.replaceAll("\\}", "</sup>");
 		} else {
 			// firefox adds nbsp which should be replaced with space
 			sValue = sValue.replaceAll("&nbsp;", " ");
 			// uppercase tags
 			sValue = sValue.replaceAll("<SUP>", "<sup>");
 			sValue = sValue.replaceAll("</SUP>", "</sup>");
 			sValue = sValue.replaceAll("<SUB>", "<sub>");
 			sValue = sValue.replaceAll("</SUB>", "</sub>");
 			// remove redundant pairs
 			sValue = sValue.replaceAll("</sup><sup>|</sub><sub>", "");
 			// if not chemical formula need to save sup and sub
 			if (!getString(PROPERTY_TYPE).equals("chem"))
 				sValue = sValue.replaceAll("<(sup|/sup|sub|/sub)>",
 						"`om~$1~mo`");
 			// remove all other tags
 			sValue = sValue.replaceAll("<[^<]+>", "");
 			// put sub and sup tags back again
 			sValue = sValue.replaceAll("`om~(sup|/sup|sub|/sub)~mo`", "<$1>");
 
 			sValue = sValue.replaceAll("&lt;", "<"); // in case typed literally
 			sValue = sValue.replaceAll("&gt;", ">"); // in case typed literally
 		}
 
 		setString(PROPERTY_VALUE, trim(sValue, MAXCHARS_ADVANCEDFIELD));
 	}
 
 	/**
 	 * @return Current value of edit field (may include HTML codes). Trimmed to
 	 *         100 characters.
 	 */
 	public String getValue() {
 		try {
 			return getString(PROPERTY_VALUE);
 		} catch (OmDeveloperException e) {
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	/**
 	 * @param sValue
 	 *            New value for edit field (may include HTML codes)
 	 */
 	public void setValue(String sValue) {
 		try {
 			setString(PROPERTY_VALUE, sValue);
 		} catch (OmDeveloperException e) {
 			throw new OmUnexpectedException(e);
 		}
 	}
 
 	public String getLabelTarget(boolean bPlain) throws OmDeveloperException {
 		if (isPropertySet(PROPERTY_LABEL))
 			throw new OmFormatException(
 					"<advancedfield>: You cannot have both a label= and a <label> for the same field");
 		else {
 			bGotLabel = true;
 			if (bPlain)
 				return QDocument.ID_PREFIX + QDocument.VALUE_PREFIX + getID();
 			else
 				return QDocument.ID_PREFIX + QDocument.OM_PREFIX + getID()
 						+ "_iframe";
 		}
 	}
 }
