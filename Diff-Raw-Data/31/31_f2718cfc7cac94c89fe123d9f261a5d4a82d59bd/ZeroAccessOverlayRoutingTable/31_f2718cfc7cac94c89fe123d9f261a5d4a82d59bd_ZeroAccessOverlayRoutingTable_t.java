 /*
  * Copyright (c) 2012-2013 Open Source Community - <http://www.peerfact.org>
  * Copyright (c) 2011-2012 University of Paderborn - UPB
  * Copyright (c) 2005-2011 KOM - Multimedia Communications Lab
  *
  * This file is part of PeerfactSim.KOM.
  * 
  * PeerfactSim.KOM is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * any later version.
  * 
  * PeerfactSim.KOM is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with PeerfactSim.KOM.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 
 package org.peerfact.impl.overlay.unstructured.zeroaccess.components;
 
 import java.math.BigInteger;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.peerfact.api.overlay.OverlayID;
 import org.peerfact.api.overlay.OverlayRoutingTable;
 
 /**
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * This part of the Simulator is not maintained in the current version of
  * PeerfactSim.KOM. There is no intention of the authors to fix this
  * circumstances, since the changes needed are huge compared to overall benefit.
  * 
  * If you want it to work correctly, you are free to make the specific changes
  * and provide it to the community.
  * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! !!!!!!!!!!!!!!!!!!!!!!!!!!!!
  * 
  * 
  * @author <info@peerfact.org>
  * @version 05/06/2011
  * 
  */
 public class ZeroAccessOverlayRoutingTable implements
 		OverlayRoutingTable<ZeroAccessOverlayID, ZeroAccessOverlayContact> {
 
 	private OverlayID<BigInteger> overlayID;
 
 	private final int maximum_peer_list_size = 256;
 
 	private final int maximum_peer_list_size_in_getL = 16;
 
 	private LinkedList<ZeroAccessOverlayContact> Contacts = new LinkedList<ZeroAccessOverlayContact>();
 
 	private Map<OverlayID<BigInteger>, ZeroAccessOverlayContact> overlayIDs = new LinkedHashMap<OverlayID<BigInteger>, ZeroAccessOverlayContact>();
 
 	public ZeroAccessOverlayRoutingTable(OverlayID<BigInteger> overlayID) {
 		this.overlayID = overlayID;
 	}
 
 	@Override
 	public void addContact(ZeroAccessOverlayContact c) {
 		ZeroAccessOverlayContact contact = new ZeroAccessOverlayContact(c);
 
 		if (!this.Contacts.contains(contact)) {
 			this.Contacts.add(contact);
 			overlayIDs.put(contact.getOverlayID(), contact);
 		}
 
 		Collections.sort(Contacts, new Comparator<ZeroAccessOverlayContact>() {
 
 			@Override
 			public int compare(ZeroAccessOverlayContact o1,
 					ZeroAccessOverlayContact o2) {
 				// TODO Auto-generated method stub
 				return (int) (o1.getLastRefresh() - o2.getLastRefresh());
 			}
 
 		});
 
 		if (this.Contacts.size() > maximum_peer_list_size)
 		{
			ZeroAccessOverlayContact contact_to_remove = Contacts.pollFirst();
			overlayIDs.remove(contact_to_remove.getOverlayID());
 		}
 	}
 
 	@Override
 	public List<ZeroAccessOverlayContact> allContacts() {
 		return new LinkedList<ZeroAccessOverlayContact>(Contacts);
 	}
 
 	public int numberOfContacts() {
 		return Contacts.size();
 	}
 
 	public LinkedList<ZeroAccessOverlayContact> getLatestContacts(int num)
 	{
 		LinkedList<ZeroAccessOverlayContact> latestContacts = new LinkedList<ZeroAccessOverlayContact>();
 		int read_count = num;
 
 		if (read_count > maximum_peer_list_size_in_getL) {
 			read_count = maximum_peer_list_size_in_getL;
 		}
 		if (read_count > Contacts.size()) {
 			read_count = Contacts.size();
 		}
 		for (int i = 0; i < read_count; i++)
 		{
 			latestContacts.add(Contacts.get(i));
 		}
 
 		return latestContacts;
 	}
 
 	@Override
 	public ZeroAccessOverlayContact getContact(ZeroAccessOverlayID oid) {
 		return this.overlayIDs.get(oid);
 	}
 
 	@Override
 	public void removeContact(ZeroAccessOverlayID oid) {
 		throw new RuntimeException("The method removeContact() within "
 				+ this.getClass().getSimpleName() + "is not implemented.");
 	}
 
 	@Override
 	public void clearContacts() {
 		this.overlayIDs.clear();
 		this.Contacts.clear();
 	}
 }
