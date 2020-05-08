 /**
  * Copyright (c) 2008 Oracle Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Oracle Corporation - initial API and implementation
  */
 package org.eclipse.jst.jsf.apache.trinidad.tagsupport.converter.operations;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jst.jsf.apache.trinidad.tagsupport.Messages;
 import org.eclipse.jst.jsf.apache.trinidad.tagsupport.model.SelectItem;
 import org.eclipse.jst.jsf.apache.trinidad.tagsupport.model.SelectItemModel;
 import org.eclipse.jst.pagedesigner.converter.ConvertPosition;
 import org.w3c.dom.Element;
 
 /**
  * ITransformOperation implementation specifically for "selectManyShuttle" and
  * "selectOrderShuttle" JSF Elements.
  * 
  * <br><b>Note:</b> requires ITransformOperation.setTagConverterContext(...) to
  * have been called to provide a valid ITagConverterContext instance prior to
  * a call to the transform(...) method.
  * 
  * @author Ian Trimble - Oracle
  */
 public class SelectShuttleOperation extends AbstractTrinidadTransformOperation {
 
 	private static final String STYLECLASS_OUTERTABLE = "af_selectManyShuttle p_AFRequired"; //$NON-NLS-1$
 	private static final String STYLECLASS_HEADER = "OraShuttleHeader"; //$NON-NLS-1$
 	private static final String STYLECLASS_REQUIRED = "AFRequiredIconStyle"; //$NON-NLS-1$
 	private static final String STYLECLASS_LISTCOLUMN = "af_selectManyShuttle_box-content"; //$NON-NLS-1$
 	private static final String STYLECLASS_SELECT = "af_selectManyListbox_content"; //$NON-NLS-1$
 	private static final String STYLECLASS_DESCRIPTION = "AFInstructionText"; //$NON-NLS-1$
	private static final String STYLECLASS_LINK = "OraLinkText"; //$NON-NLS-1$
 	private static final String DEFAULT_SIZE = "10"; //$NON-NLS-1$
 
 	private static final String[] LINKS_MOVE = new String[]{
 		Messages.SelectShuttleOperation_LinkMove,
 		Messages.SelectShuttleOperation_LinkMoveAll,
 		Messages.SelectShuttleOperation_LinkRemove,
 		Messages.SelectShuttleOperation_LinkRemoveAll
 	};
 	private static final String[] LINKS_ORDER = new String[]{
 		Messages.SelectShuttleOperation_LinkTop,
 		Messages.SelectShuttleOperation_LinkUp,
 		Messages.SelectShuttleOperation_LinkDown,
 		Messages.SelectShuttleOperation_LinkBottom
 	};
 
 	private boolean showOrderLinks;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.pagedesigner.dtmanager.converter.operations.AbstractTransformOperation#transform(org.w3c.dom.Element, org.w3c.dom.Element)
 	 */
 	@Override
 	public Element transform(Element srcElement, Element curElement) {
 		//get parameter
 		if (getParameters().length < 1) {
 			getLog().error("Warning.TransformOperationFactory.TooFewParameters", getTransformOperationID()); //$NON-NLS-1$
 			return null;
 		} else {
 			showOrderLinks = Boolean.parseBoolean(getParameters()[0]);
 		}
 		
 		//build outer table element
 		Element outerTableElement = createElement("table"); //$NON-NLS-1$
 		appendAttribute(outerTableElement, "cellpadding", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(outerTableElement, "cellspacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(outerTableElement, "border", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(outerTableElement, "width", "10%"); //$NON-NLS-1$ //$NON-NLS-2$
 		String inlineStyle = srcElement.getAttribute("inlineStyle"); //$NON-NLS-1$
 		if (inlineStyle != null && inlineStyle.length() > 0) {
 			appendAttribute(outerTableElement, "style", inlineStyle); //$NON-NLS-1$
 		}
 		String styleClass = srcElement.getAttribute("styleClass"); //$NON-NLS-1$
 		if (styleClass != null && styleClass.length() > 0) {
 			appendAttribute(outerTableElement, "class", styleClass + " " + STYLECLASS_OUTERTABLE); //$NON-NLS-1$ //$NON-NLS-2$
 		} else {
 			appendAttribute(outerTableElement, "class", STYLECLASS_OUTERTABLE); //$NON-NLS-1$
 		}
 
 		//build header row
 		Element trHeaderElement = appendChildElement("tr", outerTableElement); //$NON-NLS-1$
 		Element tdLeadingHeaderElement = appendChildElement("td", trHeaderElement); //$NON-NLS-1$
 		String leadingHeader = srcElement.getAttribute("leadingHeader"); //$NON-NLS-1$
 		if (leadingHeader != null && leadingHeader.length() > 0) {
 			appendAttribute(tdLeadingHeaderElement, "class", STYLECLASS_HEADER); //$NON-NLS-1$
 			appendAttribute(tdLeadingHeaderElement, "valign", "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendChildText(leadingHeader, tdLeadingHeaderElement);
 		}
 		appendChildElement("td", trHeaderElement); //$NON-NLS-1$
 		Element tdTrailingHeaderElement = appendChildElement("td", trHeaderElement); //$NON-NLS-1$
 		String required = srcElement.getAttribute("required"); //$NON-NLS-1$
 		String trailingHeader = srcElement.getAttribute("trailingHeader"); //$NON-NLS-1$
 		if ((required != null && Boolean.parseBoolean(required)) || (trailingHeader != null && trailingHeader.length() > 0)) {
 			appendAttribute(tdTrailingHeaderElement, "class", STYLECLASS_HEADER); //$NON-NLS-1$
 			appendAttribute(tdTrailingHeaderElement, "valign", "bottom"); //$NON-NLS-1$ //$NON-NLS-2$
 			if (required != null && Boolean.parseBoolean(required)) {
 				Element spanRequiredElement = appendChildElement("span", tdTrailingHeaderElement); //$NON-NLS-1$
 				appendAttribute(spanRequiredElement, "class", STYLECLASS_REQUIRED); //$NON-NLS-1$
 				appendChildText("*", spanRequiredElement); //$NON-NLS-1$
 			}
 			if (trailingHeader != null && trailingHeader.length() > 0) {
 				appendChildText(trailingHeader, tdTrailingHeaderElement);
 			}
 		}
 
 		//build content row
 		Element trContentElement = appendChildElement("tr", outerTableElement); //$NON-NLS-1$
 		buildListColumn(srcElement, trContentElement, true);
 		buildLinkColumn(trContentElement, LINKS_MOVE);
 		buildListColumn(srcElement, trContentElement, false);
 
 		return outerTableElement;
 	}
 
 	private void buildListColumn(Element srcElement, Element parentElement, boolean isLeadingColumn) {
 		//build outer structure
 		Element tdElement0 = appendChildElement("td", parentElement); //$NON-NLS-1$
 		Element tableElement0 = appendChildElement("table", tdElement0); //$NON-NLS-1$
 		appendAttribute(tableElement0, "style", "width:100%;"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tableElement0, "cellpadding", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tableElement0, "cellspacing", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tableElement0, "border", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		Element tbodyElement0 = appendChildElement("tbody", tableElement0); //$NON-NLS-1$
 		Element trElement0 = appendChildElement("tr", tbodyElement0); //$NON-NLS-1$
 		Element tdElement1 = appendChildElement("td", trElement0); //$NON-NLS-1$
 		Element divElement0 = appendChildElement("div", tdElement1); //$NON-NLS-1$
 		appendAttribute(divElement0, "class", STYLECLASS_LISTCOLUMN); //$NON-NLS-1$
 		Element tableElement1 = appendChildElement("table", divElement0); //$NON-NLS-1$
 		appendAttribute(tableElement1, "cellpadding", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tableElement1, "border", "0"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		//build filter structure
 		if (isLeadingColumn) {
 			Element filterFacet = getChildFacetByName(srcElement, "filter"); //$NON-NLS-1$
 			if (filterFacet != null) {
 				Element trFilterElement = appendChildElement("tr", tableElement1); //$NON-NLS-1$
 				Element tdFilterElement = appendChildElement("td", trFilterElement); //$NON-NLS-1$
 				appendAttribute(tdFilterElement, "nowrap", ""); //$NON-NLS-1$ //$NON-NLS-2$
 				appendAttribute(tdFilterElement, "valign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
 				appendAttribute(tdFilterElement, "colspan", "3"); //$NON-NLS-1$ //$NON-NLS-2$
 				tagConverterContext.addChild(filterFacet, new ConvertPosition(tdFilterElement, 0));
 			}
 		}
 
 		//build select element structure
 		Element trSelectElement = appendChildElement("tr", tableElement1); //$NON-NLS-1$
 		Element tdSelectElement = appendChildElement("td", trSelectElement); //$NON-NLS-1$
 		appendAttribute(tdSelectElement, "nowrap", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tdSelectElement, "valign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
 		Element spanSelectElement = appendChildElement("span", tdSelectElement); //$NON-NLS-1$
 		Element selectElement = appendChildElement("select", spanSelectElement); //$NON-NLS-1$
 		appendAttribute(selectElement, "multiple", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(selectElement, "class", STYLECLASS_SELECT); //$NON-NLS-1$
 		appendAttribute(selectElement, "size", getSizeString(srcElement)); //$NON-NLS-1$
 
 		if (isLeadingColumn) {
 			List<SelectItem> selectItems = SelectItemModel.getModel(srcElement);
 			Iterator<SelectItem> itSelectItems = selectItems.iterator();
 			while (itSelectItems.hasNext()) {
 				SelectItem selectItem = itSelectItems.next();
 				Element optionElement = appendChildElement("option", selectElement); //$NON-NLS-1$
 				appendChildText(selectItem.getLabel(), optionElement);
 			}
 		}
 		Element optionElement = appendChildElement("option", selectElement); //$NON-NLS-1$
 		appendChildText("_______________", optionElement); //$NON-NLS-1$
 
 		//build order links
 		if (!isLeadingColumn && showOrderLinks) {
 			buildLinkColumn(trSelectElement, LINKS_ORDER);
 		}
 
 		//build description structure
 		boolean showDesc = false;
 		String showDescAttrName;
 		if (isLeadingColumn) {
 			showDescAttrName = "leadingDescShown"; //$NON-NLS-1$
 		} else {
 			showDescAttrName = "trailingDescShown"; //$NON-NLS-1$
 		}
 		String showDescAttr = srcElement.getAttribute(showDescAttrName);
 		if (showDescAttr != null && showDescAttr.length() > 0) {
 			showDesc = Boolean.parseBoolean(showDescAttr);
 		}
 		if (showDesc) {
 			Element trDescElement = appendChildElement("tr", tableElement1); //$NON-NLS-1$
 			Element tdDescElement = appendChildElement("td", trDescElement); //$NON-NLS-1$
 			appendAttribute(tdDescElement, "nowrap", ""); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(tdDescElement, "valign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(tdDescElement, "colspan", "3"); //$NON-NLS-1$ //$NON-NLS-2$
 			Element spanDescElement = appendChildElement("span", tdDescElement); //$NON-NLS-1$
 			appendAttribute(spanDescElement, "class", STYLECLASS_DESCRIPTION); //$NON-NLS-1$
 			appendChildText(Messages.SelectShuttleOperation_Description, spanDescElement);
 			appendChildElement("div", tdDescElement); //$NON-NLS-1$
 			Element textareaElement = appendChildElement("textarea", tdDescElement); //$NON-NLS-1$
 			appendAttribute(textareaElement, "rows", "2"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(textareaElement, "cols", "18"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(textareaElement, "readonly", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 
 		//build footer structure
 		String footerFacetName;
 		if (isLeadingColumn) {
 			footerFacetName = "leadingFooter"; //$NON-NLS-1$
 		} else {
 			footerFacetName = "trailingFooter"; //$NON-NLS-1$
 		}
 		Element footerFacet = getChildFacetByName(srcElement, footerFacetName); 
 		if (footerFacet != null) {
 			Element trFooterElement = appendChildElement("tr", tableElement1); //$NON-NLS-1$
 			Element tdFooterElement = appendChildElement("td", trFooterElement); //$NON-NLS-1$
 			appendAttribute(tdFooterElement, "nowrap", ""); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(tdFooterElement, "valign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(tdFooterElement, "colspan", "3"); //$NON-NLS-1$ //$NON-NLS-2$
 			tagConverterContext.addChild(footerFacet, new ConvertPosition(tdFooterElement, 0));
 		}
 	}
 
 	private void buildLinkColumn(Element parentElement, String[] linkText) {
 		Element tdElement = appendChildElement("td", parentElement); //$NON-NLS-1$
 		appendAttribute(tdElement, "align", "center"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tdElement, "valign", "middle"); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tdElement, "nowrap", ""); //$NON-NLS-1$ //$NON-NLS-2$
 		appendAttribute(tdElement, "style", "padding:5px;"); //$NON-NLS-1$ //$NON-NLS-2$
 		for (int i = 0; i < linkText.length; i++) {
 			Element aElement = appendChildElement("a", tdElement); //$NON-NLS-1$
 			appendAttribute(aElement, "href", "#"); //$NON-NLS-1$ //$NON-NLS-2$
 			appendAttribute(aElement, "class", STYLECLASS_LINK); //$NON-NLS-1$
 			appendChildText(linkText[i], aElement);
 			if (i < linkText.length - 1) {
 				appendChildElement("br", tdElement); //$NON-NLS-1$
 			}
 		}
 	}
 
 	private String getSizeString(Element srcElement) {
 		String size = DEFAULT_SIZE;
 		String sizeAttr = srcElement.getAttribute("size"); //$NON-NLS-1$
 		if (sizeAttr != null && sizeAttr.length() > 0) {
 			try {
 				int iSize = Integer.parseInt(sizeAttr);
 				iSize = Math.max(iSize, 10);
 				iSize = Math.min(iSize, 20);
 				size = String.valueOf(iSize);
 			} catch(NumberFormatException nfe) {
 				//ignore - default of "10" will be returned
 			}
 		}
 		return size;
 	}
 
 }
