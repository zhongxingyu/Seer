 /*
  * @(#)OrganizationTagLib.java
  *
  * Copyright 2009 Instituto Superior Tecnico
  * Founding Authors: João Figueiredo, Luis Cruz, Paulo Abrantes, Susana Fernandes
  * 
  *      https://fenix-ashes.ist.utl.pt/
  * 
  *   This file is part of the Organization Module for the MyOrg web application.
  *
  *   The Organization Module is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU Lesser General Public License as published
  *   by the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.*
  *
  *   The Organization Module is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *   GNU Lesser General Public License for more details.
  *
  *   You should have received a copy of the GNU Lesser General Public License
  *   along with the Organization Module. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 
 package module.organization.presentationTier.renderers.tagLib;
 
 import java.io.IOException;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.tagext.TagSupport;
 
 import module.organization.domain.Party;
 import module.organization.domain.PartyPredicate;
 import module.organization.presentationTier.renderers.OrganizationView;
 import module.organization.presentationTier.renderers.OrganizationViewConfiguration;
 import module.organization.presentationTier.renderers.decorators.PartyDecorator;
 import pt.ist.fenixWebFramework.renderers.layouts.Layout;
 
 public class OrganizationTagLib extends TagSupport implements OrganizationView {
 
     static private final long serialVersionUID = 2726718182435118282L;
 
     private Map<String, String> values = new HashMap<String, String>();
     private OrganizationViewConfiguration config;
 
     public OrganizationTagLib() {
 	initDefaultConfig();
     }
 
     private void initDefaultConfig() {
 	values.put("rootClasses", "tree");
 	values.put("childListStyle", "display:none");
 
 	values.put("blankImage", "/organization/images/blank.gif");
 	values.put("minusImage", "/organization/images/minus.gif");
 	values.put("plusImage", "/organization/images/plus.gif");
 
 	values.put("viewPartyUrl", "/organization.do?method=viewParty&amp;partyOid=%s");
 
 	values.put("organization", null);
 	values.put("configuration", null);

     }
 
     @Override
     public int doStartTag() throws JspException {
 	return EVAL_BODY_INCLUDE;
     }
 
     @Override
     public int doEndTag() throws JspException {
 	try {
 	    drawOrganization();
 	} catch (IOException e) {
 	    e.printStackTrace();
 	}
	release(); // force release
 	return EVAL_PAGE;
     }
 
     @Override
     public void release() {
 	super.release();
 	values.clear();
     }
 
     private void drawOrganization() throws IOException {
 	final Layout layout = (Layout) getConfig().getLayout().saveView(this);
 	final Object object = pageContext.findAttribute(values.get("organization"));
 	if (object == null) {
 	    throw new IllegalArgumentException("invalid.organization.value");
 	}
 	layout.createComponent(object, null).draw(pageContext);
     }
 
     public String getRootClasses() {
 	return values.get("rootClasses");
     }
 
     public void setRootClasses(String rootClasses) {
 	values.put("rootClasses", rootClasses);
     }
 
     public String getChildListStyle() {
 	return values.get("childListStyle");
     }
 
     public void setChildListStyle(String childListStyle) {
 	values.put("childListStyle", childListStyle);
     }
 
     public String getBlankImage() {
 	return values.get("blankImage");
     }
 
     public void setBlankImage(String blankImage) {
 	values.put("blankImage", blankImage);
     }
 
     public String getMinusImage() {
 	return values.get("minusImage");
     }
 
     public void setMinusImage(String minusImage) {
 	values.put("minusImage", minusImage);
     }
 
     public String getPlusImage() {
 	return values.get("plusImage");
     }
 
     public void setPlusImage(String plusImage) {
 	values.put("plusImage", plusImage);
     }
 
     public String getViewPartyUrl() {
 	return values.get("viewPartyUrl");
     }
 
     public void setViewPartyUrl(String viewPartyUrl) {
 	values.put("viewPartyUrl", viewPartyUrl);
     }
 
     public String getOrganization() {
 	return values.get("organization");
     }
 
     public void setOrganization(String organization) {
 	values.put("organization", organization);
     }
 
     public String getConfiguration() {
 	return values.get("configuration");
     }
 
     public void setConfiguration(String configuration) {
 	values.put("configuration", configuration);
     }
 
     private OrganizationViewConfiguration getConfig() {
 	if (this.config == null) {
 	    this.config = (OrganizationViewConfiguration) pageContext.findAttribute(values.get("configuration"));
 	}
 	return (OrganizationViewConfiguration) this.config;
     }
 
     @Override
     public PartyDecorator getDecorator() {
 	return getConfig().getDecorator();
     }
 
     @Override
     public PartyPredicate getPredicate() {
 	return getConfig().getPredicate();
     }
 
     @Override
     public Comparator<Party> getSortBy() {
 	return getConfig().getSortBy();
     }
 
     @Override
     public void setProperty(final String name, final Object value) {
 	values.put(name, (String) value);
     }
 }
