 /*******************************************************************************
  * Copyright (c) 2008, 2010 Standard for Technology in Automotive Retail  and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *Contributors:
  *    David Carver (STAR) - initial API and implementation
  *    Holger Voormann - bug 283646 - Document wizard throws NPW with DITA is selected
  *	  Igor Jacy Lino Campista - Java 5 warnings fixed (bug 311325)
  *    Florian Thienel - bug 299999 - completed implementation of validation
  *******************************************************************************/
 package org.eclipse.wst.xml.vex.core.internal.validator;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMAttributeDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMContent;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDataType;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMDocument;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMElementDeclaration;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMGroup;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNode;
 import org.eclipse.wst.xml.core.internal.contentmodel.CMNodeList;
 import org.eclipse.wst.xml.core.internal.contentmodel.ContentModelManager;
 import org.eclipse.wst.xml.core.internal.contentmodel.internal.util.CMValidator;
 import org.eclipse.wst.xml.core.internal.contentmodel.internal.util.CMValidator.ElementContentComparator;
 import org.eclipse.wst.xml.core.internal.contentmodel.internal.util.CMValidator.ElementPathRecordingResult;
 import org.eclipse.wst.xml.core.internal.contentmodel.internal.util.CMValidator.StringElementContentComparator;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.I.Validator;
 import org.eclipse.wst.xml.vex.core.internal.provisional.dom.impl.ValidatorImpl;
 
@SuppressWarnings("restriction")
 public class WTPVEXValidator extends ValidatorImpl implements Validator {
 
 	private static final long serialVersionUID = -7632029717211069257L;
 
 	private static final ElementContentComparator ELEMENT_CONTENT_COMPARATOR = new StringElementContentComparator() {
 		@Override
 		public boolean isPCData(final Object o) {
 			return "#PCDATA".equals(o);
 		};
 	};
 
 	/** The XML schema (= content model), e.g. DTD or XSD */
 	private transient CMDocument schema;
 
 	private final transient CMValidator validator;
 
 	private final URL url;
 
 	private WTPVEXValidator(final URL url) {
 		this.url = url;
 		validator = new CMValidator();
 	}
 
 	private CMDocument getSchema() {
 		if (schema == null) {
 			final ContentModelManager modelManager = ContentModelManager.getInstance();
 			final String resolved = url.toString();
 			schema = modelManager.createCMDocument(resolved, null);
 		}
 		return schema;
 	}
 
 	public static WTPVEXValidator create(final URL url) {
 		return new WTPVEXValidator(url);
 	}
 
 	@Override
	public AttributeDefinition getAttributeDefinition(final String element, final String attribute) {
		final CMElementDeclaration cmelement = getElementDeclaration(element);
		final CMAttributeDeclaration attr = (CMAttributeDeclaration) cmelement.getAttributes().getNamedItem(attribute);
		final AttributeDefinition vexAttr = createAttributeDefinition(attr);
		return vexAttr;
 	}
 
 	@Override
 	public EList<AttributeDefinition> getAttributeDefinitions(final String element) {
 		final CMElementDeclaration cmelement = getElementDeclaration(element);
 		final EList<AttributeDefinition> attributeList = new BasicEList<AttributeDefinition>(cmelement.getAttributes().getLength());
 		final Iterator<?> iter = cmelement.getAttributes().iterator();
 		while (iter.hasNext()) {
 			final CMAttributeDeclaration attribute = (CMAttributeDeclaration) iter.next();
 			final AttributeDefinition vexAttr = createAttributeDefinition(attribute);
 			attributeList.add(vexAttr);
 		}
 
 		return attributeList;
 	}
 
 	private CMElementDeclaration getElementDeclaration(final String element) {
 		return (CMElementDeclaration) getSchema().getElements().getNamedItem(element);
 	}
 
 	private AttributeDefinition createAttributeDefinition(final CMAttributeDeclaration attribute) {
 		final String defaultValue = attribute.getDefaultValue();
 		String[] values = null;
 		AttributeDefinition.Type type = null;
 		if (attribute.getAttrType().equals(CMDataType.ENUM)) {
 			type = AttributeDefinition.Type.ENUMERATION;
 			values = attribute.getAttrType().getEnumeratedValues();
 		} else if (attribute.getAttrType().equals(CMDataType.NOTATION)) {
 			type = AttributeDefinition.Type.ENUMERATION;
 			values = attribute.getAttrType().getEnumeratedValues();
 		} else
 			type = AttributeDefinition.Type.get(attribute.getAttrType().getDataTypeName());
 		final boolean required = attribute.getUsage() == CMAttributeDeclaration.REQUIRED;
 		final boolean fixed = attribute.getUsage() == CMAttributeDeclaration.FIXED;
 		final AttributeDefinition vexAttr = new AttributeDefinition(attribute.getAttrName(), type, defaultValue, values, required, fixed);
 		return vexAttr;
 	}
 
 	@Override
 	public Set<String> getValidItems(final String element) {
 		final CMElementDeclaration elementDec = (CMElementDeclaration) getSchema().getElements().getNamedItem(element);
		final List<CMNode> nodes = getAvailableContent(element, elementDec);
 		final Set<String> results = new HashSet<String>();
 		for (final CMNode node : getAvailableContent(element, elementDec))
 			if (node instanceof CMElementDeclaration) {
 				final CMElementDeclaration elem = (CMElementDeclaration) node;
 				results.add(elem.getElementName());
 			}
 		return results;
 	}
 
 	/**
 	 * Returns a list of all CMNode 'meta data' that may be potentially added to
 	 * the element.
 	 */
 	protected List<CMNode> getAvailableContent(final String element, final CMElementDeclaration ed) {
 		final List<CMNode> list = new ArrayList<CMNode>();
 		if (ed.getContentType() == CMElementDeclaration.ELEMENT || ed.getContentType() == CMElementDeclaration.MIXED) {
 			final CMContent content = ed.getContent();
 			if (content instanceof CMElementDeclaration)
 				list.add(content);
 			else if (content instanceof CMGroup) {
 				final CMGroup groupContent = (CMGroup) content;
 				list.addAll(getAllChildren(groupContent));
 			}
 		}
 		return list;
 	}
 
 	private List<CMNode> getAllChildren(final CMGroup group) {
 		final List<CMNode> list = new ArrayList<CMNode>();
 		final CMNodeList nodeList = group.getChildNodes();
 
 		for (int i = 0; i < nodeList.getLength(); i++) {
 			final CMNode node = nodeList.item(i);
 			if (node instanceof CMElementDeclaration)
 				list.add(node);
 			else if (node instanceof CMGroup)
 				list.addAll(getAllChildren((CMGroup) node));
 		}
 		return list;
 	}
 
 	@Override
 	public Set<String> getValidRootElements() {
 		final Set<String> results = new HashSet<String>();
 		final Iterator<?> iter = getSchema().getElements().iterator();
 		while (iter.hasNext()) {
 			final CMElementDeclaration element = (CMElementDeclaration) iter.next();
 			results.add(element.getElementName());
 		}
 
 		return results;
 	}
 
 	@Override
 	public boolean isValidSequence(final String element, final EList<String> nodes, final boolean partial) {
 		final CMNode parent = getSchema().getElements().getNamedItem(element);
 		if (!(parent instanceof CMElementDeclaration))
 			return true;
 
 		final CMElementDeclaration elementDeclaration = (CMElementDeclaration) parent;
 		final ElementPathRecordingResult validationResult = new ElementPathRecordingResult();
 		validator.validate(elementDeclaration, nodes, ELEMENT_CONTENT_COMPARATOR, validationResult);
 
 		final int elementCount = getElementCount(nodes);
 		if (partial && elementCount > 0)
 			return validationResult.getPartialValidationCount() >= elementCount;
 
 		return validationResult.isValid;
 	}
 
 	private static int getElementCount(final EList<String> nodes) {
 		int count = 0;
 		for (final String node : nodes)
 			if (ELEMENT_CONTENT_COMPARATOR.isElement(node))
 				count++;
 		return count;
 	}
 
 	@Override
 	public boolean isValidSequence(final String element, final EList<String> seq1, final EList<String> seq2, final EList<String> seq3, final boolean partial) {
 		final EList<String> joinedSequence = new BasicEList<String>();
 		if (seq1 != null)
 			joinedSequence.addAll(seq1);
 		if (seq2 != null)
 			joinedSequence.addAll(seq2);
 		if (seq3 != null)
 			joinedSequence.addAll(seq3);
 		return isValidSequence(element, joinedSequence, partial);
 	}
 
 }
