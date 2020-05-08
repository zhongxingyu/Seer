 /**
  * Amazon-DTN - Lightweight Delay Tolerant Networking Implementation
  * Copyright (C) 2013  Dórian C. Langbeck
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package br.ufpa.adtn.routing.prophet;
 
 import br.ufpa.adtn.core.BPAgent;
 
 import br.ufpa.adtn.core.EID;
 import br.ufpa.adtn.routing.MessageLinkConnection;
 import br.ufpa.adtn.routing.prophet.ProphetDataRouting.NeighborPredict;
 import br.ufpa.adtn.routing.prophet.ProphetUtil.BundleSpec;
 import br.ufpa.adtn.util.Logger;
 
 /**
  * This class is the abstraction of a communication link with a PROPHET node. Responsible for
  * implementing the method of triggering parked event to begin the process of communication
  * with other neighbor, creating a ProphetMessageConnection. Has decision logic of Protocol
  * to set bundle routes according to their delivery predictability metric.
  * 
  * @author Douglas Cirqueira
  */
 public class ProphetLinkConnection extends MessageLinkConnection<ProphetLinkConnection, ProphetBundleRouter, ProphetMessageConnection, ProphetTLV> {
 	private static final Logger LOGGER = new Logger("ProphetLinkConnection");
 	
 	private final ProphetDataRouting dataRout;
 	
 	public ProphetLinkConnection(ProphetDataRouting dataRout) {
 		super(ProphetTLV.PARSER);
 		this.dataRout = dataRout;
 	}
 	
 	@Override
 	protected void onParked() {
 		LOGGER.v("onParked event");	
 		
 		if (getRouter().updatePresence(this))
 			getMessageProvider().create();
 	}
 
 	@Override
 	public ProphetMessageConnection createMessageConnection() {
 		return new ProphetMessageConnection();
 	}
 	
 	public ProphetDataRouting getProphetDataRouting() {
 		return dataRout;
 	}
 	
 	public BundleSpec[] update(NeighborPredict[] preds) {
 		/*
 		 * Considering that I'm node A, my neighbor is node B
 		 * and others are C.
 		 */
 		final EID remote_eid = getRegistrationEndpointID();
 		for (int i = 0, len = preds.length; i < len; i++) {
 			final NeighborPredict np = preds[i];
 			final EID c_eid = np.getEID();
 			
			if (c_eid == getLocalEndpointID())
 				continue;
 			
 			final float p_ac = dataRout.getPredict(c_eid);
 			final float p_bc = np.getPredict();
 			final float p_ab = dataRout.getPredict(remote_eid);
 			
 			//Updating transitivity
 			dataRout.updateTransitivity(c_eid, p_ab, p_bc);
 			
 			//Check better candidate to delivering
 			if (p_ac < p_bc){
 				BPAgent.routeLink(c_eid, remote_eid);
 				LOGGER.d("RouteLink to " + remote_eid);
 			}
 			else {
 				BPAgent.routeUnlink(c_eid, remote_eid);
 				LOGGER.d("RouteUnlink to " + remote_eid);
 			}				
 		}
 		
 		// Decide which bundles will be offered and return it.
 		return ProphetUtil.getSpec(BPAgent.getBundlesFor(remote_eid));
 	}
 }
