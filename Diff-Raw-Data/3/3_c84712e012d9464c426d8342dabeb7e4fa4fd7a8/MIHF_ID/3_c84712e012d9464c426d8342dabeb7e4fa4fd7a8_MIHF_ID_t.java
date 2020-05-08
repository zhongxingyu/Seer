 /*
  *  OpenJ21 Copyright (C) 2010 Paulo Pires
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
  */
 package net.openj21.mih.datatype.identification;
 
import net.openj21.mih.datatype.basic.OCTET_STRING;
 import net.openj21.mih.datatype.basic.SEQUENCE;
 import net.openj21.mih.datatype.basic.SEQUENCE_ELEMENT;
 
 /**
  * The MIHF Identifier: MIHF_ID is a network access identifier (NAI). NAI shall
  * be unique as per IETF RFC 4282. If L3 communication is used and MIHF entity
  * resides in the network node, then MIHF_ID is the fully qualified domain name
  * or NAI-encoded IP address (IP4_ADDR or IP6_ADDR) of the entity that hosts the
  * MIH Services. If L2 communication is used then MIHF_ID is the NAI-encoded
  * link- layer address (LINK_ADDR) of the entity that hosts the MIH services. In
  * an NAI-encoded IP address or link-layer address, each octet of binary-encoded
  * IP4_ADDR, IP6_ADDR and LINK_ADDR data is encoded in the username part of the
  * NAI as “\” followed by the octet value. A multicast MIHF identifier is
  * defined as an MIHF ID of zero length. When an MIH protocol message with
  * multicast MIHF ID is transmitted over the L2 data plane, a group MAC address
  * (01-80-C2- 00-00-0E) shall be used (see IEEE P802.1aj/D2.2). The maximum
  * length is 253 octets.
  */
 @SEQUENCE
 public class MIHF_ID {
 	@SEQUENCE_ELEMENT(order = 1, basicType = true)
 	@OCTET_STRING(size = 253)
 	private String mihfId;
 
 	public MIHF_ID() {
 	}
 
 	public MIHF_ID(String mihf_id) {
 		this.mihfId = mihf_id;
 	}
 
 	public String getMihfId() {
 		return mihfId;
 	}
 
 	public void setMihfId(String mihfId) {
 		this.mihfId = mihfId;
 	}
 
 	@Override
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		if (o == null || getClass() != o.getClass())
 			return false;
 
 		MIHF_ID mihf_id = (MIHF_ID) o;
 
 		return !(mihfId != null ? !mihfId.equals(mihf_id.mihfId)
 				: mihf_id.mihfId != null);
 	}
 
 	@Override
 	public int hashCode() {
 		return mihfId != null ? mihfId.hashCode() : 0;
 	}
 
 	@Override
 	public String toString() {
 		return "MIHF_ID{" + "mihfId='" + mihfId + '\'' + '}';
 	}
 }
