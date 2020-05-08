 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.scan;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import gda.device.DeviceBase;
 import gda.device.DeviceException;
 import gda.scan.ScanDataPoint;
 
 /**
  * Device that acts as a store for information about a ScanDataPoint Runs on server and client. On the server it accepts
  * a ScanDataPoint and creates a token that is passed to the client which then reproduces the original ScanDataPoint.
  */
 public class ScanDataPointServer extends DeviceBase implements IScanDataPointServer {
 	HashMap<String, ScanData> store = new HashMap<String, ScanData>();
 	Vector<String> storeOrder = new Vector<String>();
 	
 	private static final ScanDataPointServer INSTANCE = new ScanDataPointServer();
 	
 	public static ScanDataPointServer getInstance() {
 		return INSTANCE;
 	}
 	
 	public ScanDataPointServer() {
 		setName(getClass().getSimpleName());
 	}
 
 	
 	@Override
 	public void configure(){
 		// no configuration required
 	}
 
 	/**
 	 * @param sdp
 	 * @return The token to pass to a ScanDataPointClient
 	 */
 	public ScanDataPointVar ___getToken(ScanDataPoint sdp) {
 		String uniqueName = sdp.getUniqueName();
 		if (!store.containsKey(uniqueName)) {
			while (storeOrder.size() > 300) {
 				store.remove(storeOrder.get(0));
 				storeOrder.remove(0);
 			}
 			store.put(uniqueName, new ScanData(sdp));
 			storeOrder.add(uniqueName);
 		}
 		return new ScanDataPointVar(sdp);
 	}
 	
 	public static ScanDataPointVar getToken(ScanDataPoint sdp) {
 		return INSTANCE.___getToken(sdp);
 	}
 	
 
 	@Override
 	public ScanData ___convertTokenId(String tokenId) throws DeviceException {
 		ScanData scanData = store.get(tokenId);
 		if (scanData == null)
 			throw new DeviceException("ScanDataPointServer - unable to find ScanData for " + tokenId);
 		return scanData;
 	}
 
 	/**
 	 * @param args
 	 * @return A ScanDataPoint generated from the given ScanDataPointToken
 	 * @throws DeviceException
 	 */
 	public static ScanData __convertTokenId(String args) throws DeviceException {
 		String[] s = args.split("&");
 		return INSTANCE.___convertTokenId(s[0]);
 	}
 }
