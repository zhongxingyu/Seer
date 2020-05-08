 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.cimi.domain;
 
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import javax.xml.bind.annotation.XmlType;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.codehaus.jackson.annotate.JsonPropertyOrder;
 import org.codehaus.jackson.map.annotate.JsonSerialize;
 import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
 import org.ow2.sirocco.cimi.domain.collection.CimiForwardingGroupNetworkCollection;
 
 /**
  * Class ForwardingGroup.
  */
 @XmlRootElement(name = "ForwardingGroup")
 @XmlType(propOrder = {"id", "name", "description", "created", "updated", "propertyArray", "networks", "operations",
    "providerInfo", "xmlExtensionAttributes"})
@JsonPropertyOrder({"resourceURI", "id", "name", "description", "created", "updated", "properties", "networks", "operations",
    "providerInfo"})
 @JsonSerialize(include = Inclusion.NON_NULL)
 public class CimiForwardingGroup extends CimiObjectCommonAbstract {
 
     /** Serial number */
     private static final long serialVersionUID = 1L;
 
     /**
      * Field "networks".
      * <p>
      * Read only
      * </p>
      */
     private CimiForwardingGroupNetworkCollection networks;
 
     private ProviderInfo providerInfo;
 
     /**
      * Default constructor.
      */
     public CimiForwardingGroup() {
         super();
     }
 
     /**
      * Parameterized constructor.
      * 
      * @param href The reference
      */
     public CimiForwardingGroup(final String href) {
         super(href);
     }
 
     /**
      * Return the value of field "networks".
      * 
      * @return The value
      */
     public CimiForwardingGroupNetworkCollection getNetworks() {
         return this.networks;
     }
 
     /**
      * Set the value of field "networks".
      * 
      * @param networks The value
      */
     public void setNetworks(final CimiForwardingGroupNetworkCollection networks) {
         this.networks = networks;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.domain.CimiObjectCommonAbstract#hasValues()
      */
     @Override
     public boolean hasValues() {
         boolean has = super.hasValues();
         // Next read-only
         // has = has || (null != this.getNetworks());
         return has;
     }
 
     /**
      * {@inheritDoc}
      * 
      * @see org.ow2.sirocco.cimi.domain.CimiExchange#getExchangeType()
      */
     @Override
     @XmlTransient
     @JsonIgnore
     public ExchangeType getExchangeType() {
         return ExchangeType.ForwardingGroup;
     }
 
     public ProviderInfo getProviderInfo() {
         return this.providerInfo;
     }
 
     public void setProviderInfo(final ProviderInfo providerInfo) {
         this.providerInfo = providerInfo;
     }
 
 }
