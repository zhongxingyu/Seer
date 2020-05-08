 /*******************************************************************************
  * 
  * Copyright 2002 - 2005 NCHELP
  * 
  * Author:	Priority Technologies, Inc.
  * 
  * 
  * This code is part of the Meteor system as defined and specified by the
  * National Council of Higher Education Loan Programs, Inc. (NCHELP) and the
  * Meteor Sponsors, and developed by Priority Technologies, Inc. (PTI).
  * 
  * 
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *  
  ******************************************************************************/
 /*
  * Derived from the book: Programming Web Services with SOAP By James Snell,
  * Doug Tidwell, Pavel Kulchenko Published by O'Reilly & Associates ISBN:
  * 0-596-00095-2
  *  
  */
 
 package org.nchelp.meteor.security.saml;
 
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import org.nchelp.meteor.util.XMLDataTypes;
 
 
 public class Assertion implements AssertionAbstractType
 {
 	private static transient final Log log = LogFactory.getLog(Assertion.class);
 	private IDType assertionID;
 	private String issuer;
 	private Date issueInstant;
 	private Vector statementList = new Vector();
 	private Conditions conditions;
 
 	/**
 	 * @see AssertionAbstractType#getMajorVersion()
 	 */
 	public String getMajorVersion ()
 	{
 		return "1";
 	}
 
 	/**
 	 * @see AssertionAbstractType#getMinorVersion()
 	 */
 	public String getMinorVersion ()
 	{
 		return "0";
 	}
 
 	/**
 	 * @see AssertionAbstractType#getAssertionID()
 	 */
 	public IDType getAssertionID ()
 	{
 		return this.assertionID;
 	}
 
 	/**
 	 * @see AssertionAbstractType#serAssertionID(IDType)
 	 */
 	public void setAssertionID (IDType assertionID)
 	{
 		this.assertionID = assertionID;
 	}
 
 	/**
 	 * @see AssertionAbstractType#getIssuer()
 	 */
 	public String getIssuer ()
 	{
 		return this.issuer;
 	}
 
 	/**
 	 * @see AssertionAbstractType#setIssuer(String)
 	 */
 	public void setIssuer (String issuer)
 	{
 		this.issuer = issuer;
 	}
 
 	/**
 	 * @see AssertionAbstractType#getIssueInstant()
 	 */
 	public Date getIssueInstant ()
 	{
 		return this.issueInstant;
 	}
 
 	/**
 	 * @see AssertionAbstractType#setIssueInstant(Date)
 	 */
 	public void setIssueInstant (Date issueInstant)
 	{
 		this.issueInstant = issueInstant;
 	}
 
 	public void addStatement (Statement s)
 	{
 		statementList.add(s);
 	}
 
 	public Statement getStatement (int index)
 	{
 		return (Statement)statementList.get(index);
 	}
 
 	public String getAttributeValue (String name, String namespace)
 	{
 		return this.getAttributeValue(name, namespace, null);
 	}
 
 	public String getAttributeValue (String name, String namespace,
 			String defaultValue)
 	{
 		for (int i = 0; i < statementList.size(); i++)
 		{
 			Statement s = (Statement)statementList.get(i);
 			if (s instanceof AttributeStatement)
 			{
 				AttributeStatement as = (AttributeStatement)s;
 				for (int j = 0; j < as.getAttributeSize(); j++)
 				{
 					Attribute att = as.getAttribute(j);
 
 					if (att.getName().equals(name) && att.getNamespace().equals(namespace))
 					{
 						return att.getValue().trim();
 					}
 				}
 			}
 		}
 
 		return defaultValue;
 	}
 
 	/**
 	 * @param namespace
 	 * @return List of org.nchelp.meteor.security.saml.Attribute objects that
 	 *         belong to the requested namespace.
 	 */
 	public List getAttributes (String namespace)
 	{
 		List list = new ArrayList();
 
 		for (int i = 0; i < statementList.size(); i++)
 		{
 			Statement s = (Statement)statementList.get(i);
 			if (s instanceof AttributeStatement)
 			{
 				AttributeStatement as = (AttributeStatement)s;
 				for (int j = 0; j < as.getAttributeSize(); j++)
 				{
 					Attribute att = as.getAttribute(j);
 
 					if (att.getNamespace().equals(namespace))
 					{
 						list.add(att);
 					}
 				}
 			}
 		}
 
 		return list;
 	}
 
 	public void setAttributeValue (String name, String namespace, String value)
 	{
 
 		Subject subject = null;
 
 		for (int i = 0; i < statementList.size(); i++)
 		{
 			subject = ((Statement)statementList.get(i)).getSubject();
 
 			// If we got a valid one then use it.
 			if (subject != null)
 				break;
 		}
 
 		AttributeStatement as = new AttributeStatement();
 		as.setSubject(subject);
 
 		as.addAttribute(name, namespace, value);
 
 		this.addStatement(as);
 
 	}
 
 	protected void serializeAttributes (Element e)
 	{
 		e.setAttribute("MajorVersion", getMajorVersion());
 		e.setAttribute("MinorVersion", getMinorVersion());
 		if (assertionID != null)
 			e.setAttribute("AssertionID", assertionID.getText());
 
 		if (issuer != null)
 			e.setAttribute("Issuer", issuer);
 
 		if (issueInstant != null)
 			e.setAttribute("IssueInstant", XMLDataTypes.dateTimeToXml(issueInstant));
 
 	}
 
 	protected void deserializeAttributes (Element source)
 	{
 		String s1 = source.getAttribute("AssertionID");
 		String s2 = source.getAttribute("Issuer");
 		String s3 = source.getAttribute("IssueInstant");
 
 		if (s1 != null)
 			setAssertionID(new IDType(s1));
 		if (s2 != null)
 			setIssuer(s2);
 		if (s3 != null)
 		{
 			try
 			{
 				setIssueInstant(XMLDataTypes.xmlToDateTime(s3));
 			}
 			catch (ParseException e)
 			{
 				log.error("Error parsing the date: IssueInstant");
 				// what can I possibly do here?!?
 			}
 		}
 	}
 
 	public void serialize (Element parent)
 	{
 		Document doc = parent.getOwnerDocument();
 		Element e = doc.createElementNS(SAMLUtil.NS, SAMLUtil.NSID + ":Assertion");
 		e.setAttribute("xmlns:" + SAMLUtil.NSID, SAMLUtil.NS);
 
 		serializeAttributes(e);
 
 		for (int i = 0; i < statementList.size(); i++)
 		{
 			Statement s = (Statement)statementList.get(i);
 			s.serialize(e);
 		}
 		if(this.conditions != null){
 			this.conditions.serialize(e);
 		}
 		parent.appendChild(e);
 	}
 
 	public void deserialize (Element source)
 	{
 		deserializeAttributes(source);
 		NodeList nl = source.getChildNodes();
 		for (int n = 0; n < nl.getLength(); n++)
 		{
 			Node node = nl.item(n);
 			if (node.getNodeType() == Node.ELEMENT_NODE)
 			{
 				Element e = (Element)node;
 				if ("AuthenticationStatement".equals(e.getLocalName()))
 				{
 					AuthenticationStatement as = new AuthenticationStatement();
 					as.deserialize(e);
 					addStatement(as);
 				}
 				if ("AttributeStatement".equals(e.getLocalName()))
 				{
 					AttributeStatement as = new AttributeStatement();
 					as.deserialize(e);
 					addStatement(as);
 				}
				if ("Condition".equals(e.getLocalName()))
 				{
 					Conditions c = new Conditions();
 					c.deserialize(e);
 					this.conditions = c;
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return Conditions Conditions object containing the NotBefore and NotOnOrAfter
 	 */
 	public Conditions getConditions() {
 		if(conditions == null){
 			conditions = new Conditions();
 		}
 		return conditions;
 	}
 
 	/**
 	 * @param conditions Conditions object containing the NotBefore and NotOnOrAfter
 	 */
 	public void setConditions(Conditions conditions) {
 		this.conditions = conditions;
 	}
 
 }
