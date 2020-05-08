 package net.ednovak.nearby;
 
 import java.math.BigInteger;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Random;
 
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.location.LocationManager;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 public class protocol {
 
 	//treeQueue leaves; // The span
 	tree user; // Location of the user (leaf node)
 
 	// Constructor does nothing! This is probably a design flaw :P
 	public protocol() {
 	}
 
 	// Homomorphic Addition (E(m1) * E(m2)) % n^2 = (m1 + m2) % n
 	// This function works in the encrypted domain to get clear domain addition
 	public BigInteger homoAdd(BigInteger em1, BigInteger em2, BigInteger n) {
 		return (em1.multiply(em2)).mod(n.multiply(n));
 	}
 
 	// Homomorphic Multiplication ( E(m1)^m2) ) % n^2 = (m1 * m2) % n
 	// This function works in the encrypted domain to get clear text
 	// multiplication
 	public BigInteger homoMult(BigInteger em1, BigInteger m2, BigInteger n) {
 		return em1.modPow(m2, n.multiply(n));
 	}
 
 	// Homomorphic Exponentiation ( (E(m1) ^ m1) ^ m1) ^ ... ) % n^2 = (m1^n) %
 	// n
 	// For these I NEED to have em1 and m1
 	public BigInteger homoExpo(BigInteger em1, BigInteger m1, BigInteger m2,
 			BigInteger n) {
 		// Pretend m2 = 2
 		BigInteger cur = homoMult(em1, m1, n);
 		int intM2 = m2.intValue() - 2;
 		while (intM2 > 0) {
 			cur = homoMult(cur, m1, n);
 			intM2--;
 		}
 		return cur;
 	}
 
 	// Evaluates the polynmial represented by the encrypted coefficients in poly
 	// (in reverse)
 	// at the point b. The n is that used in the homomorphic encryption of the
 	// coefficients
 	public BigInteger homoEval(BigInteger b, BigInteger[] poly, BigInteger n) {
 		BigInteger[] terms = new BigInteger[poly.length];
 
 		// Find the terms between the addition
 		for (int i = 0; i < poly.length; i++) {
 			BigInteger tmp = b.pow(i);
 			terms[i] = homoMult(poly[i], tmp, n);
 		}
 
 		// Add up the terms
 		BigInteger sum = new BigInteger(String.valueOf(terms[0]));
 		for (int i = 1; i < terms.length; i++) {
 			sum = homoAdd(sum, terms[i], n);
 		}
 
 		return sum;
 	}
 
 	// Creates the leaf nodes from a left and right longitude value
 	// X is the leaf that the user is at
 	public treeQueue genLeaves(int left, int right, int x, String type) {
 		// left, right, and x are leaf node values (integers)
 		System.out.println("Creating the leaves");
 		treeQueue leaves = new treeQueue();
 		int cur = left;
 		while (cur <= right) {
 			// The map is the binary rep of this nodes value, neat huh?
 			String mapString = new StringBuffer(Integer.toBinaryString(cur))
 					.toString(); // R is 0, L is 1;
 			// System.out.println(cur + " => " + mapString); // char of binary
 			// string from this int ()
 			leaves.push(new tree(cur, mapString.toCharArray(), null, null, 0, type));
 			if (cur == x) {
 				leaves.peek(-1).special = "User!";
 				user = leaves.peek(-1);
 			}
 			cur++;
 		}
 		return leaves;
 	}
 
 	// Convert latitude to leaf Value
 	public int latitudeToLeaf(double latitude) {
 		if (latitude < -90 || latitude > 90) {
 			System.out.println("Input latitude out of range: " + latitude);
 			System.exit(102);
 		}
 
 		latitude = latitude + 90;
 		// On a line that goes through the poles .0000899 degrees is about 10
 		// meters.
 		// This (like the longitude) means the protocol granularity is at best
 		// 10 meters
 		// I am assuming the Earth is a perfect sphere with radius 6371. Even
 		// though
 		// it is not.
 		return (int) Math.round((latitude / 0.000044966242717));
 	}
 
 	// Converts a longitude to a leaf node value
 	public int longitudeToLeaf(double longitude) {
 		if (longitude < -180 || longitude > 180) {
 			System.out.println("Input longitude out of range: " + longitude);
 			System.exit(101);
 		}
 		longitude = longitude + 180;
 		// Along the equator 10 meters is about .0000899 degrees. This
 		// (like latitude) means that the protocol granularity is at best 10
 		// meters
 		// I am assuming the Earth is a perfect sphere with radius 6371. Even
 		// though
 		// it is not.
 		return (int) Math.round((longitude / 0.000089932161921));
 	}
 
 	public double findLat(double orig_lon_1, double orig_lat_1, int distance) {
 		// Log.d("location", "coming in");
 		// Log.d("location", "orig_lon_1: " + orig_lon_1);
 		// Log.d("location", "orig_lat_1: " + orig_lat_1);
 		// Log.d("location", "distance: " + distance);
 
 		double d = (double) distance / 1000;
 		double dist = d / 6371.0;
 		double brng = 0 * (Math.PI / 180);
 		double lat1 = orig_lat_1 * (Math.PI / 180);
 		// double lon1 = orig_lon_1 * (Math.PI/180);
 
 		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
 				+ Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
 		lat2 = lat2 * (180 / Math.PI);
 		// Log.d("location", "lat2degrees: " + lat2);
 		return lat2;
 	}
 
 	// Given a lon, lat, and distance, returns the lon at the same latitude at
 	// that distance
 	public double findLong(double orig_lon_1, double orig_lat_1, int distance) {
 		// Log.d("location", "coming in");
 		// Log.d("location", "orig_lon_1: " + orig_lon_1);
 		// Log.d("location", "orig_lat_1: " + orig_lat_1);
 		// Log.d("location", "distance: " + distance);
 
 		double d = (double) distance / 1000;
 		double dist = d / 6371.0;
 		double brng = 90 * (Math.PI / 180);
 		double lat1 = orig_lat_1 * (Math.PI / 180);
 		double lon1 = orig_lon_1 * (Math.PI / 180);
 
 		double lat2 = Math.asin(Math.sin(lat1) * Math.cos(dist)
 				+ Math.cos(lat1) * Math.sin(dist) * Math.cos(brng));
 		// Log.d("location", "lat2: " + lat2);
 
 		// double lat2deg = lat2 * (180 / Math.PI);
 		// Log.d("location", "lat2 in degrees: " + lat2deg);
 
 		double lon2 = lon1
 				+ Math.atan2(Math.sin(brng) * Math.sin(dist) * Math.cos(lat1),
 						Math.cos(dist) - Math.sin(lat1) * Math.sin(lat2));
 
 		// Log.d("location", "lon2 before normalizing: " + lon2);
 		lon2 = (lon2 + 3 * Math.PI) % (2 * Math.PI) - Math.PI; // Apparently
 																// normalizes it
 																// to with
 																// [-180,180] ?
 
 		// Log.d("location", "lon2 before radians->degrees: " + lon2);
 		lon2 = lon2 * (180 / Math.PI); // Convert from radians to degrees
 
 		Log.d("location", "lon2: " + lon2);
 		return lon2;
 	}
 
 	// Takes their policy and converts it to leaf node distance to create a span
 	// In this case the nodes are 10m apart (because of longitudeToInt)
 	public int policyToWidth(int policy) {
 		// Policy should be a factor of 10 but I didn't enforce that here out of
 		// laziness
 		return policy / 10;
 	}
 	
 	
 	
 
 	// Returns a string of the entire tree from 'root' downward (depth first
 	// search)
 	//public String treeToStringDown(tree root) {
 	//	if (root == null) {
 	//		return "";
 	//	}
 
 	//	return root.value + "\n" + treeToStringDown(root.left)
 	//			+ treeToStringDown(root.right);
 	//}
 
 	// Multiplys two polynomials. We define a polynomial as a reverse string of
 	// integers. For example poly a = x^2 - 4x + 5 int[] a = {5, -4, 1}
 	// For any i > 2; a[i] = 0;
 	private BigInteger[] multPolys(BigInteger[] a, BigInteger[] b) {
 
 		/*
 		 * // Print A and B for (int i = 0; i < a.length; i++){ Log.d("poly",
 		 * "a[" + i +"]: " + a[i]); } for (int i = 0; i < b.length; i++){
 		 * Log.d("poly", "b[" + i +"]: " + b[i]); }
 		 */
 
 		// Initialize C
 		int cLength = (a.length - 1) + (b.length - 1) + 1;
 		BigInteger[] c = new BigInteger[cLength];
 		// Log.d("poly", "c.length: " + c.length);
 
 		// Pad a & b
 		BigInteger[] newA = new BigInteger[cLength];
 		for (int i = 0; i < newA.length; i++) {
 			if (i < a.length) {
 				newA[i] = a[i];
 			} else {
 				newA[i] = new BigInteger("0");
 			}
 		}
 
 		BigInteger[] newB = new BigInteger[cLength];
 		for (int i = 0; i < newB.length; i++) {
 			if (i < b.length) {
 				newB[i] = b[i];
 			} else {
 				newB[i] = new BigInteger("0");
 			}
 		}
 
 		/*
 		 * // Print a & b padded for (int i = 0; i < newA.length; i++){
 		 * Log.d("poly", "newa[" + i +"]: " + newA[i]); } for (int i = 0; i <
 		 * newB.length; i++){ Log.d("poly", "newB[" + i +"]: " + newB[i]); }
 		 */
 
 		// Generate C
 		for (int n = 0; n < c.length; n++) {
 			BigInteger tmp = new BigInteger("0");
 			// Log.d("poly", "Generating c[" + n + "]");
 			for (int k = 0; k <= n; k++) {
 				// Log.d("poly", "k: " + k + "    n:" + n);
 				tmp = tmp.add(newA[k].multiply(newB[n - k]));
 				// Log.d("test", newA[k] + " * " + newB[n - k] + " = " + tmp);
 			}
 			// Log.d("test", "Adding coefficient to result big poly: " + tmp);
 			c[n] = tmp;
 			// Log.d("poly", "c[" + n + "]: " + c[n]);
 		}
 
 		/*
 		 * // Print C for (int i = 0; i < c.length; i++){ Log.d("poly", "c[" + i
 		 * +"]: " + c[i]); }
 		 */
 		return c;
 
 	}
 	
 	// This function builds the parents of a given row of leaves
 	private treeQueue buildRow(treeQueue bottom){
 		treeQueue newRow = new treeQueue();
 		for (int i = 0; i < bottom.length; i++){
 			tree cur = bottom.peek(i);
 			tree parent = cur.createParent();
 			newRow.push(parent); // Maybe unique push but that shouldn't be an issue
 			cur.setParent(parent);
 	
 			// Set the parent's children
 			if (cur.upRightward()){
 				parent.left = cur;
 				if (i+1 < bottom.length){ // We're not on the right edge and a i+1 exists
 					parent.right = bottom.peek(i+1);
 					bottom.peek(i+1).setParent(parent);
 				}
 				i = i + 1; // Get to skip a node, yay
 			}
 			
 			else {
 				parent.right = cur;
 				if (i-1 >= 0){ // We're not on the left edge and a i-1 exists
 					parent.left = bottom.peek(i-1);
 					bottom.peek(i-1).setParent(parent);
 				}
 			}
 		}
 		return newRow;
 	}
 	
 	
 	// This function takes a set of leaves and builds all the nodes upward until we have a complete tree
 	// The stopping criteria: when the current row of nodes produces 1 parent among all (both) nodes.
 	public tree buildUp(treeQueue leaves){
 		treeQueue top = new treeQueue();
 		treeQueue bottom = leaves;
 		
 		while (top.length != 1 ){
 			top = buildRow(bottom);	
 			bottom = top;
 		}
 		
 		return top.peek(0);
 	}
 	
 	
 	// Find the path from the given node to the root.
 	// This used to be the "covering set" but that name is confusing
 	// leaf is the node to start from (doesn't have to be a leaf)
 	// height is the desired height to ascend to (counted from starting point)
 	public treeQueue findPath(tree leaf, int height){
 		treeQueue answer = new treeQueue();
 		tree cur = leaf;
 		while (cur.height < height){
 			answer.push(cur);
 			cur = cur.createParent();
 		}
 		return answer;
 	}
 	
 	
 	// Find the wall given the edges
 	// Starts at the root in the top queue.
 	public treeQueue findWall(tree leftEnd, tree rightEnd, tree root){
 		treeQueue answer = new treeQueue();
 		treeQueue bottom = new treeQueue();
 		treeQueue top = new treeQueue();
 		top.push(root);
 		while (top.length != 0){
 			for (int i = 0; i < top.length; i++){
 				tree cur = top.peek(i);
 				// If a leaf is outside the span than it isn't in my tree and we'll see null
 				if (cur.leftLeaf() == null || cur.rightLeaf() == null){
 					if (cur.left != null){
 						bottom.push(cur.left);
 					}
 					if (cur.right != null){
 						bottom.push(cur.right);
 					}
 				}
 				else{ answer.push(cur); } // The left and right leaf was within the bounds
 			}
 			top = bottom;
 			bottom = new treeQueue();
 		}
 		return answer;
 	}
 	
 
 	private BigInteger[][] genPolysFromRoots(treeQueue trees) {
 		// Generate Result
 		BigInteger result[][] = new BigInteger[trees.length][2];
 		for (int i = 0; i < trees.length; i++) {
 			BigInteger[] tmp = new BigInteger[2];
 			tmp[0] = new BigInteger(String.valueOf(trees.peek(i).value * -1));
 			tmp[1] = new BigInteger("1");
 			result[i] = tmp;
 			// Log.d("poly", "result[" + i + "]: " + result[i][0] + " " +
 			// result[i][1]);
 		}
 
 		// Print Result
 
 		for (int i = 0; i < result.length; i++) {
 			for (int j = 0; j < result[i].length; j++) {
 				// Log.d("poly", "result[" + i +"][" + j + "]: " +
 				// result[i][j]);
 			}
 		}
 
 		return result;
 	}
 
 	// Generates one large polynomial rooted at all the points in the rep set
 	public BigInteger[] makeCoefficientsTwo(treeQueue repSet) {
 
 		BigInteger polys[][] = genPolysFromRoots(repSet);
 		BigInteger[] cur = multPolys(polys[0], polys[1]);
 		for (int i = 2; i < polys.length; i++) {
 			cur = multPolys(cur, polys[i]);
 		}
 		return cur;
 	}
 
 	// Returns an array of coefficients that each form a poly that
 	// is rooted at a node from the given repSet
 	public BigInteger[] makeCoefficientsOne(treeQueue repSet) {
 		BigInteger[] answer = new BigInteger[repSet.length];
 		for (int i = 0; i < repSet.length; i++) {
 			answer[i] = new BigInteger(
 					String.valueOf(repSet.peek(i).value * -1));
 		}
 
 		return answer;
 	}
 
 	public BigInteger[] makeCoefficients(treeQueue repSet, int method) {
 
 		if (method == 1) {
 			Log.d("poly", "Several Small Polys");
 			// This returns the coefficients of several polynomials
 			// They are of the form (x - coe). The 1 in front of the x is
 			// implied
 			return makeCoefficientsOne(repSet);
 		}
 
 		else if (method == 2) {
 			Log.d("poly", "One Large Poly");
 			// This returns coefficients of one polynomial
 			// It is of the form (c_nx^n + c_n-1 * x^n-1 ...)
 			return makeCoefficientsTwo(repSet);
 		}
 
 		else {
 			Log.d("poly", "N preference selected!");
 			return null;
 		}
 	}
 
 	// Does Bob's calculations
 	public BigInteger[] computation(treeQueue coveringSet, BigInteger[] encCoe,
 			int bits, BigInteger g, BigInteger n, int method) {
 
 		BigInteger[] results = null;
 		Random rand = new Random();
 
 		Paillier paillierE = new Paillier(false);
 		paillierE.loadPublicKey(g, n);
 		//BigInteger[] pub = paillierE.publicKey();
 		//Log.d("enc", "Homomorphic Computation  g: " + pub[0] + "  n: " + pub[1]);
 
 		if (method == 1) { // Several small polys
 			results = new BigInteger[encCoe.length * coveringSet.length];
 
 			// This should probs be a protocol function
 			// Evaluate the polys
 			int k = 0;
 			for (int j = 0; j < coveringSet.length; j++) {
 				int tmp = coveringSet.peek(j).value;
 				BigInteger bob = new BigInteger(String.valueOf(tmp));
 				bob = paillierE.Encryption(bob);
 				for (int i = 0; i < encCoe.length; i++) { // The last token is
 															// the width
 					BigInteger alice = encCoe[i];
 					BigInteger c = bob.multiply(alice).mod(paillierE.nsquare);
 					results[k] = c;
 					
 					// Random Number to hide stuff from Bob
 					BigInteger r = new BigInteger(String.valueOf(rand.nextInt(9999999)));
 					results[k] = homoMult(results[k], r, n);
 					
 					k++;
 				}
 			}
 		}
 
 		else if (method == 2) { // On large poly
 			results = new BigInteger[coveringSet.length];
 			for (int i = 0; i < coveringSet.length; i++) {
 				BigInteger b = new BigInteger(String.valueOf(coveringSet
 						.peek(i).value));
 				// Log.d("bobCalc", "Evaluating " + b);
 				results[i] = homoEval(b, encCoe, n);
 				
 				// The random number
 				BigInteger r = new BigInteger(String.valueOf(rand.nextInt(9999999)));
 				results[i] = homoMult(results[i], r, n);
 			}	
 		}
 		
 		// Shuffle the results
 		Collections.shuffle(Arrays.asList(results));
 		return results;
 	}
 	
 	
 	public Paillier getKey(int bits){
 		shareSingleton share = shareSingleton.getInstance();
 		if (share.pKey == null){
 			share.pKey = new Paillier(bits, 64);
 		}
 		return share.pKey;
 	}
 	
 	
 	// Encrypts an array of BigIntegers (properly generates key
 	public BigInteger[] encryptArray(BigInteger[] clear, Context context){
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		int bits = Integer.valueOf(prefs.getString("bits", "1024"));
 		Paillier p = getKey(bits);
 		BigInteger[] priv = p.privateKey();
 		//Log.d("enc", "Encrypting.  g: " + priv[0] + "  lambda: " + priv[1] + "  n:" + priv[2]);
 		BigInteger[] encCoe = new BigInteger[clear.length];
 		for (int i = 0; i < encCoe.length; i++){
 			encCoe[i] = p.Encryption(clear[i]);
 		}
 		
 		return encCoe;
 	}
 	
 	
 	public int[] makeSpan(int stage, Location loc, int policy){
 		double edge = 0.0;
 		int edgeLeafNumber = 0;
 		int userLeafNumber = 0;
 		
 		
 		if (stage == 2 || stage == 3){
 			edge = findLong(loc.getLongitude(), loc.getLatitude(), policy);
 			edgeLeafNumber = longitudeToLeaf(edge);
 			userLeafNumber = longitudeToLeaf(loc.getLongitude());
 		}
 		
 		else if (stage == 5 || stage == 6){
 			edge = findLat(loc.getLongitude(), loc.getLatitude(), policy);
 			edgeLeafNumber = latitudeToLeaf(edge);
 			userLeafNumber = latitudeToLeaf(loc.getLatitude());
 		}
 		
 		// Find the leaves on the edge and build the span
 		//Log.d("stage " + stage, "User's leaf value: " + userLeafNumber);
 		//Log.d("stage " + stage, "Edge gps value: " + edge);
 		//Log.d("stage " + stage, "edge leaf value: " + edgeLeafNumber);
 
 		int spanLength = (Math.abs(edgeLeafNumber - userLeafNumber) * 2) + 1;
 		Log.d("stats", "User's leaf node span: " + spanLength);
 
 		int left = userLeafNumber - (spanLength / 2);
 		int right = userLeafNumber + (spanLength / 2);
 
 		Log.d("stage " + stage, "User's leaf nodes go from " + left + " to "
 				+ right + ".  That's: " + spanLength + " nodes.  Their node is: "
 				+ userLeafNumber);
 		
 		return new int[] {left, userLeafNumber, right};
 	}
 
 	// Alice for stage 1 || 3
 
 
 	// This is the function Bob uses to check Alice's C values
 	// tokens example [sender:@@X:c_1:c_2:c_3:...:c_n]
 	public boolean check(String[] tokens, Context context, int bits) {
 		Paillier paillierD = getKey(bits);
 		//BigInteger[] priv = paillierD.privateKey();
 		//Log.d("enc", "Decrypting g: " + priv[0] + "  lambda: " + priv[1] + "  n: " + priv[2]);
 
 		long start = System.currentTimeMillis();
 		boolean found = false;
 		for (int i = 0; i < tokens.length; i++) {
 			BigInteger val = new BigInteger(tokens[i], 32);
 			//Log.d("enc", "Decrypting: " + val);
 			String clear = paillierD.Decryption(val).toString();
 			Log.d("prot", "unenc: " + clear);
 			if (clear.equals("0")) {
 				Log.d("hooray!", "It was 0");
 				found = true;
 				break;
 			}
 		}
 		long end = System.currentTimeMillis();
 		long total_checkTime = end - start;
 		Log.d("stats", "Time for Bob to check Alice's numbers: "
 				+ total_checkTime + "ms");
 
 		return found;
 
 		/*
 		 * if (stage == 4) { Intent intent = new Intent(context,
 		 * answerAct.class); intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		 * intent.putExtra("found", found); if ( found ){
 		 * intent.putExtra("answer", "Bob is located near you1"); } else {
 		 * intent.putExtra("answer", "Bob is not located near you"); }
 		 * context.startActivity(intent); }
 		 */
 
 	}
 
 	// Send a FB message
 	public String sendFBMessage(String rec, String message, Context context) {
 		Random gen = new Random();
 		String session_id = String.format("%05d", gen.nextInt(9000));
 		xmppService.sendMessage(rec, message, 1, session_id, context);
 		return session_id;
 	}
 	
 	
 	//@Overload
 	public void sendFBMessage(String rec, String message, int stage, String session, Context context){
 		xmppService.sendMessage(rec, message, stage, session, context);
 	}
 
 	
 	public Location locSimple(Context context) {
 		
 		// Get Bob's location
 		LocationManager lManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
 
 		// Try to get location from gps
 		Location lastKnownLocation = lManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 
 		// Try to get location from network if GPS is null
 		if (lastKnownLocation == null) {
 			//Log.d("receive", "lastKnown was null");
 			lastKnownLocation = lManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
 		}
 
 		// Get fake location is preference is turned on (overriding other
 		// locations!)
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 		if (prefs.getBoolean("fake_locations", false)) {
 			Log.d("location", "SETTING FAKE LOCATION");
 			Double fake_lat = Double.valueOf(prefs.getString("fake_lat",
 					"37.2708"));
 			Double fake_lon = Double.valueOf(prefs.getString("fake_lon",
 					"-76.7113"));
 			Log.d("location", "fake_lat: " + fake_lat + "    fake_lon: " + fake_lon);
 			lastKnownLocation = new Location("");
 			lastKnownLocation.setLatitude(fake_lat);
 			lastKnownLocation.setLongitude(fake_lon);
 			lastKnownLocation.setTime(System.currentTimeMillis());
 			//Log.d("location", "FAKE LOCATION SET");	
 		}
 
 		// Error logging if no location was obtained through any of these
 		// methods
 		if (lastKnownLocation == null) {
 			Log.d("recieve", "location is null!");
 		}
 
 		return lastKnownLocation;
 	}
 	
 	public void notification(String ticker, String title, String content, Context context, Intent notificationIntent){
 		
 		// 1
 		NotificationManager mNM = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
 		
 		// 2
 		Notification notification = new Notification(R.drawable.ic_launcher, ticker, System.currentTimeMillis());
 		
 		// 3
 		PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
 		notification.setLatestEventInfo(context, title, content, contentIntent);
 		
 		// 4
 		mNM.notify(1, notification);
 	}
 	
 	
	// The policy here is the MAX distance away that the use will allow
	// Alice may be 'policy' meters from Bob East or West (N or S for latitude) therefore we should 
	// find the height to cover twice this policy
 	public int getPathLength(int policy){
		double top = Math.log((double)policy * 2.0 / 10.0);
		double real = top / Math.log(2);
		return Math.min(15, (int)Math.round(real)+1);
 	}
 }
