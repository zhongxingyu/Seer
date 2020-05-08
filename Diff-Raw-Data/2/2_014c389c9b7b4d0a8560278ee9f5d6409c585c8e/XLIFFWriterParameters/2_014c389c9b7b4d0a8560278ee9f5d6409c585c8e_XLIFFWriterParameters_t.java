 /*===========================================================================
   Copyright (C) 2013 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common.filterwriter;
 
 import net.sf.okapi.common.BaseParameters;
 import net.sf.okapi.common.ParametersDescription;
 
 public class XLIFFWriterParameters extends BaseParameters {
 	private boolean useSourceForTranslated;
 	private boolean escapeGt;
 	private boolean placeholderMode;
 	private boolean includeNoTranslate;
 	private boolean setApprovedAsNoTranslate;
 	private boolean copySource;
 	private boolean includeAltTrans;
 	private boolean includeCodeAttrs;
 
 	private static final String USESOURCEFORTRANSLATED = "useSourceForTranslated";
 	private static final String ESCAPEGT = "escapeGt";
 	private static final String PLACEHOLDERMODE = "placeholderMode";
 	private static final String INCLUDENOTRANSLATE = "includeNoTranslate";
 	private static final String SETAPPROVEDASNOTRANSLATE = "setApprovedAsNoTranslate";
 	private static final String COPYSOURCE = "copySource";
 	private static final String INCLUDEALTTRANS = "includeAltTrans";
 	private static final String INCLUDECODEATTRS = "includeCodeAttrs";
 
 	public XLIFFWriterParameters() {
 		reset();
 		toString(); // fill the list
 	}
 
 	public boolean getUseSourceForTranslated() {
 		return useSourceForTranslated;
 	}
 
 	/**
 	 * Sets the flag indicating if the source text is used in the target, even if
 	 * a target is available.
 	 * <p>This is for the tools where we trust the target will be obtained by the tool
 	 * from the TMX from original. This is to allow editing of pre-translated items in XLIFF
 	 * editors that use directly the <target> element.
 	 * @param useSourceForTranslated true to use the source in the target even if a target text
 	 * is available.
 	 */
 	public void setUseSourceForTranslated(boolean useSourceForTranslated) {
 		this.useSourceForTranslated = useSourceForTranslated;
 	}
 
 	public boolean getEscapeGt() {
 		return escapeGt;
 	}
 
 	/**
 	 * Sets the flag indicating if '>' should be escaped or not.
 	 * @param escapeGt true to always escape '>', false to not escape it.
 	 */
 	public void setEscapeGt(boolean escapeGt) {
 		this.escapeGt = escapeGt;
 	}
 
 	/**
 	 * Sets the flag indicating if the inline code should use the place-holder notation (g and x elements).
 	 * @param placeholderMode true if the inline code should use the place-holder notation.
 	 */
 	public boolean getPlaceholderMode() {
 		return placeholderMode;
 	}
 
 	public void setPlaceholderMode(boolean placeholderMode) {
 		this.placeholderMode = placeholderMode;
 	}
 
 	public boolean getIncludeNoTranslate() {
 		return includeNoTranslate;
 	}
 
 	/**
 	 * Sets the flag indicating if non-translatable text units should be output or not.
 	 * @param includeNoTranslate true to include non-translatable text unit in the output.
 	 */
 	public void setIncludeNoTranslate(boolean includeNoTranslate) {
 		this.includeNoTranslate = includeNoTranslate;
 	}
 
 	public boolean getSetApprovedAsNoTranslate() {
 		return setApprovedAsNoTranslate;
 	}
 
 	/**
 	 * Sets the flag indicating to mark as not translatable all entries that are approved.
 	 * @param setApprovedasNoTranslate true to mark approved entries as not translatable.
 	 */
 	public void setSetApprovedAsNoTranslate(boolean setApprovedAsNoTranslate) {
 		this.setApprovedAsNoTranslate = setApprovedAsNoTranslate;
 	}
 
 	public boolean getCopySource() {
 		return copySource;
 	}
 
 	/**
 	 * Sets the copySource flag indicating to copy the source at the target spot if there is no target defined.
 	 * @param copySource true to copy the source at the target spot if there is no target defined.
 	 */
 	public void setCopySource(boolean copySource) {
 		this.copySource = copySource;
 	}
 
 	public boolean getIncludeAltTrans() {
 		return includeAltTrans;
 	}
 
 	/**
 	 * Sets the flag indicating if alt-trans elements should be output or not.
 	 * @param includeAltTrans true to include alt-trans element in the output.
 	 */
 	public void setIncludeAltTrans(boolean includeAltTrans) {
 		this.includeAltTrans = includeAltTrans;
 	}
 
 	public boolean getIncludeCodeAttrs() {
 		return includeCodeAttrs;
 	}
 
 	/**
 	 * Sets the flag indicating if extended code attributes should be output or not.
	 * @param includeCodeAttrs true to include extended code attributes in the output.
 	 */
 	public void setIncludeCodeAttrs(boolean includeCodeAttrs) {
 		this.includeCodeAttrs = includeCodeAttrs;
 	}
 
 	@Override
 	public void reset() {
 		useSourceForTranslated = false;
 		escapeGt = false;
 		placeholderMode = true;
 		includeNoTranslate = true;
 		setApprovedAsNoTranslate = false;
 		copySource = true;
 		includeAltTrans = true;
 		includeCodeAttrs = false;
 	}
 
 	@Override
 	public void fromString(String data) {
 		reset();
 		buffer.fromString(data);
 
 		useSourceForTranslated = buffer.getBoolean(USESOURCEFORTRANSLATED, useSourceForTranslated);
 		escapeGt = buffer.getBoolean(ESCAPEGT, escapeGt);
 		placeholderMode = buffer.getBoolean(PLACEHOLDERMODE, placeholderMode);
 		includeNoTranslate = buffer.getBoolean(INCLUDENOTRANSLATE, includeNoTranslate);
 		setApprovedAsNoTranslate = buffer.getBoolean(SETAPPROVEDASNOTRANSLATE, setApprovedAsNoTranslate);
 		copySource = buffer.getBoolean(COPYSOURCE, copySource);
 		includeAltTrans = buffer.getBoolean(INCLUDEALTTRANS, includeAltTrans);
 		includeCodeAttrs = buffer.getBoolean(INCLUDECODEATTRS, includeCodeAttrs);
 	}
 
 	@Override
 	public String toString() {
 		buffer.reset();
 
 		buffer.setBoolean(USESOURCEFORTRANSLATED, useSourceForTranslated);
 		buffer.setBoolean(ESCAPEGT, escapeGt);
 		buffer.setBoolean(PLACEHOLDERMODE, placeholderMode);
 		buffer.setBoolean(INCLUDENOTRANSLATE, includeNoTranslate);
 		buffer.setBoolean(SETAPPROVEDASNOTRANSLATE, setApprovedAsNoTranslate);
 		buffer.setBoolean(COPYSOURCE, copySource);
 		buffer.setBoolean(INCLUDEALTTRANS, includeAltTrans);
 		buffer.setBoolean(INCLUDECODEATTRS, includeCodeAttrs);
 
 		return buffer.toString();
 	}
 
 	@Override
 	public ParametersDescription getParametersDescription() {
 		ParametersDescription desc = new ParametersDescription(this);
 
 		desc.add(USESOURCEFORTRANSLATED, "Use the source text in the target, even if a target is available", null);
 		desc.add(ESCAPEGT, "Escape the greater-than characters as &gt;", null);
 		desc.add(PLACEHOLDERMODE, "Inline code should use the place-holder notation (g and x elements)", null);
 		desc.add(INCLUDENOTRANSLATE, "Output non-translatable text units", null);
 		desc.add(SETAPPROVEDASNOTRANSLATE, "Mark as not translatable all entries that are approved", null);
 		desc.add(COPYSOURCE, "Copy the source as target if there is no target defined", null);
 		desc.add(INCLUDEALTTRANS, "Output alt-trans elements", null);
 		desc.add(INCLUDECODEATTRS, "Output extended code attributes", null);
 
 		return desc;
 	}
 
 }
