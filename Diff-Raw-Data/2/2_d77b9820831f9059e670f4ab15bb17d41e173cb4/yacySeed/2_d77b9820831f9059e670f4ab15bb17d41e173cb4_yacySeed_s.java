 // yacySeed.java 
 // -------------------------------------
 // (C) by Michael Peter Christen; mc@anomic.de
 // first published on http://www.anomic.de
 // Frankfurt, Germany, 2004
 //
 // $LastChangedDate$
 // $LastChangedRevision$
 // $LastChangedBy$
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 //
 // Using this software in any meaning (reading, learning, copying, compiling,
 // running) means that you agree that the Author(s) is (are) not responsible
 // for cost, loss of data or any harm that may be caused directly or indirectly
 // by usage of this softare or this documentation. The usage of this software
 // is on your own risk. The installation and usage (starting/running) of this
 // software may allow other people or application to access your computer and
 // any attached devices and is highly dependent on the configuration of the
 // software which must be done by the user of the software; the author(s) is
 // (are) also not responsible for proper configuration and usage of the
 // software, even if provoked by documentation provided together with
 // the software.
 //
 // Any changes to this file according to the GPL as documented in the file
 // gpl.txt aside this file in the shipment you received can be done to the
 // lines that follows this copyright notice here, but changes must not be
 // done inside the copyright notive above. A re-distribution must contain
 // the intact and unchanged copyright notice.
 // Contributions and changes to the program code must be marked as such.
 
 /*
   YACY stands for Yet Another CYberspace
   
   the yacySeed Object is the object that bundles and carries all information about
   a single peer in the yacy space.
   The yacySeed object is carried along peers using a string representation, that can
   be compressed and/or scrambled, depending on the purpose of the process.
 
   the yacy status
   any value that is defined here will be overwritten each time the proxy is started
   to prevent that the system gets confused, it should be set to "" which means
   undefined. Other status' that can be reached at run-time are
   junior    - a peer that has no public socket, thus cannot be reached on demand
   senior    - a peer that has a public socked and serves search queries
   principal - a peer like a senior socket and serves as gateway for network definition
 
 */
 
 package de.anomic.yacy;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import de.anomic.net.natLib;
 import de.anomic.plasma.plasmaSwitchboard;
 import de.anomic.server.serverCodings;
 import de.anomic.server.serverDate;
 import de.anomic.server.serverCore;
 import de.anomic.tools.bitfield;
 import de.anomic.tools.crypt;
 
 public class yacySeed {
 
     public static final String INDEX_OUT = "sI";
     public static final String INDEX_IN  = "rI";
     public static final String URL_OUT = "sU";
     public static final String URL_IN  = "rU";
 
     public static final String PEERTYPE_VIRGIN = "virgin";
     public static final String PEERTYPE_JUNIOR = "junior";
     public static final String PEERTYPE_SENIOR = "senior";
     public static final String PEERTYPE_PRINCIPAL = "principal";
     public static final String PEERTYPE = "PeerType";
 
     public static final String STR_YOURTYPE  = "yourtype";
     public static final String STR_LASTSEEN  = "LastSeen";
 
     public static final String STR_IP        = "IP";
     public static final String STR_YOURIP    = "yourip";
     public static final String STR_MYTIME    = "mytime";
     public static final String STR_SEED      = "seed";
     public static final String STR_EQUAL     = "=";
 
     // class variables
     public String hash;
     private Map dna;
     public int available;
     public int selectscore = -1; // only for debugging
 
     public yacySeed(String hash, Map dna) {
         // create a seed with a pre-defined hash map
         this.hash = hash;
         this.dna = dna;
         this.available = 0;
     }
 
     public yacySeed(String hash) {
         dna = new HashMap();
 
         // settings that can only be computed by originating peer:
         // at first startup -
         this.hash = hash;                // the hash key of the peer - very important. should be static somehow, even after restart
         dna.put("Name", "&empty;");     // the name that the peer has given itself
         dna.put("BDate", "&empty;");    // birthdate - first startup
         dna.put("UTC", "+0000");
         // later during operation -
         dna.put("ISpeed", "0");   // the speed of indexing (pages/minute) of the peer
         dna.put("Uptime", "0");   // the number of minutes that the peer is up in minutes/day (moving average MA30)
         dna.put("LCount", "0");   // the number of links that the peer has stored (LURL's)
         dna.put("NCount", "0");   // the number of links that the peer has noticed, but not loaded (NURL's)
         dna.put("ICount", "0");   // the number of words that the peer has indexed (as it says)
         dna.put("SCount", "0");   // the number of seeds that the peer has stored
         dna.put("CCount", "0");   // the number of clients that the peer connects (as connects/hour)
         dna.put("Version", "0");  // the applications version
 
         // settings that is created during the 'hello' phase - in first contact
         dna.put("IP", "");       // 123.234.345.456
         dna.put("Port", "&empty;"); // 
         dna.put(PEERTYPE, PEERTYPE_VIRGIN); // virgin/junior/senior/principal
         dna.put("IPType", "&empty;");   // static/dynamic (if the ip changes often for any reason)
 
         // settings that can only be computed by visiting peer
         dna.put(STR_LASTSEEN, yacyCore.universalDateShortString(new Date()));  // for last-seen date
         dna.put("USpeed", "0");   // the computated uplink speed of the peer
 
         // settings that are needed to organize the seed round-trip
         dna.put("Flags", "0000");
         setFlagDirectConnect(false);
         setFlagAcceptRemoteCrawl(true);
         setFlagAcceptRemoteIndex(true);
 
         // index transfer
         dna.put(INDEX_OUT, "0"); // send index
         dna.put(INDEX_IN, "0");  // received Index
         dna.put(URL_OUT, "0");   // send url
         dna.put(URL_IN, "0");    // received URL
 
         available = 0;
     }
 
     public String get(String key, String dflt) {
         final Object o = dna.get(key);
         if (o == null) { return dflt; } else { return (String) o; }
     }
 
     public void put(String key, String value) {
         dna.put(key, value);
     }
 
     public Map getMap() {
         return dna;
     }
 
     public String getName() {
         return get("Name", "&empty;");
     }
 
     public String getHexHash() {
         return b64Hash2hexHash(hash);
     }
 
     public void incSI(int count) {
         String v = (String) dna.get(INDEX_OUT);
         if (v == null) { v = "0"; }
         dna.put(INDEX_OUT, Integer.toString(Integer.parseInt(v) + count));
     }
 
     public void incRI(int count) {
         String v = (String) dna.get(INDEX_IN);
         if (v == null) { v = "0"; }
         dna.put(INDEX_IN, Integer.toString(Integer.parseInt(v) + count));
     }
 
     public void incSU(int count) {
         String v = (String) dna.get(URL_OUT);
         if (v == null) { v = "0"; }
         dna.put(URL_OUT, Integer.toString(Integer.parseInt(v) + count));
     }
 
     public void incRU(int count) {
         String v = (String) dna.get(URL_IN);
         if (v == null) { v = "0"; }
         dna.put(URL_IN, Integer.toString(Integer.parseInt(v) + count));
     }
 
     // 12 * 6 bit = 72 bit = 9 byte
     public static String hexHash2b64Hash(String hexHash) {
         return serverCodings.enhancedCoder.encodeBase64(serverCodings.decodeHex(hexHash));
     }
 
     public static String b64Hash2hexHash(String b64Hash) {
         // the hash string represents 12 * 6 bit = 72 bits. This is too much for a long integer.
         return serverCodings.encodeHex(serverCodings.enhancedCoder.decodeBase64(b64Hash));
     }
 
     public float getVersion() {
         try {
             return Float.parseFloat(get("Version", "0"));
         } catch (NumberFormatException e) {
             return 0;
         }
     }
 
     public String getAddress() {
         final String ip   = (String) dna.get("IP");
         final String port = (String) dna.get("Port");
         if (ip != null && ip.length() >= 8 && port != null && port.length() >= 2) {
             return ip + ":" + port;
         } else {
             return null;
         }
     }
 
     public long getUTCDiff() {
         String utc = (String) dna.get("UTC");
         if (utc == null) { utc = "+0200"; }
         return serverDate.UTCDiff(utc);        
     }
 
     public long getLastSeenTime() {
         try {
             final long t = yacyCore.shortFormatter.parse(get(STR_LASTSEEN, "20040101000000")).getTime();
             // the problem here is: getTime applies a time shift according to local time zone:
             // it substracts the local UTF offset, but it should substract the remote UTC offset
             // so we correct it by first adding the local UTF offset and then subtractibg the remote
             // but the time zone was originally the seeds time zone
             // we correct this here
             return t - getUTCDiff() + serverDate.UTCDiff();
         } catch (java.text.ParseException e) {
             return System.currentTimeMillis();
         } catch (java.lang.NumberFormatException e) {
             return System.currentTimeMillis();
         }
     }
 
     public int getAge() {
         // returns the age as number of days
         try {
             final long t = yacyCore.shortFormatter.parse(get("BDate", "20040101000000")).getTime();
             return (int) ((System.currentTimeMillis() - (t - getUTCDiff() + serverDate.UTCDiff())) / 1000 / 60 / 60 / 24);
         } catch (java.text.ParseException e) {
             return -1;
         } catch (java.lang.NumberFormatException e) {
             return -1;
         }
     }
 
     public void setLastSeenTime() {
         // if we set a last seen time, then we need to respect the seeds UTC offset
         put(STR_LASTSEEN, yacyCore.shortFormatter.format(new Date(System.currentTimeMillis() - serverDate.UTCDiff() + getUTCDiff())));
     }
 
     public int getPPM() {
         try {
             return Integer.parseInt(get("ISpeed", "0"));
         } catch (NumberFormatException e) {
             return 0;
         }
     }
 
     public long getLinkCount() {
         try {
             return Long.parseLong(get("LCount", "0"));
         } catch (NumberFormatException e) {
             return 0;
         }
     }
 
     private boolean getFlag(int flag) {
         final String flags = get("Flags", "0000");
         return (new bitfield(flags.getBytes())).get(flag);
     }
 
     private void setFlag(int flag, boolean value) {
         final String flags = get("Flags", "0000");
         final bitfield f = new bitfield(flags.getBytes());
         f.set(flag, value);
         put("Flags", f.toString());
     }
 
     public void setFlagDirectConnect(boolean value) {setFlag(0, value);}
     public void setFlagAcceptRemoteCrawl(boolean value) {setFlag(1, value);}
     public void setFlagAcceptRemoteIndex(boolean value) {setFlag(2, value);}
     public boolean getFlagDirectConnect() {return getFlag(0);}
     public boolean getFlagAcceptRemoteCrawl() {
         //if (getVersion() < 0.300) return false;
         //if (getVersion() < 0.334) return true;
         return getFlag(1);
     }
     public boolean getFlagAcceptRemoteIndex() {
         //if (getVersion() < 0.335) return false;
         return getFlag(2);
     }
     public boolean isVirgin() {
         return get(PEERTYPE, "").equals(PEERTYPE_VIRGIN);
     }
     public boolean isJunior() {
         return get(PEERTYPE, "").equals(PEERTYPE_JUNIOR);
     }
     public boolean isSenior() {
         return get(PEERTYPE, "").equals(PEERTYPE_SENIOR);
     }
     public boolean isPrincipal() {
         return get(PEERTYPE, "").equals(PEERTYPE_PRINCIPAL);
     }
     public boolean isOnline() {
         return (isSenior() || isPrincipal());
     }
 
     public String encodeLex(long c, int length) {
         if (length < 0) { length = 0; }
         String s = "";
         if (c == 0) {
             s = '-' + s;
         } else {
             while (c > 0) {
                 s = ((char) (32 + (c % 96))) + s;
                 c = c / 96;
             }
         }
         if (length != 0 && s.length() > length) {
             throw new RuntimeException("encodeLex result '" + s + "' exceeds demanded length of " + length + " digits");
         }
         if (length == 0) { length = 1; } // rare exception for the case that c == 0
         while (s.length() < length) { s = '-' + s; }
         return s;
     }
 
     public long decodeLex(String s) {
         long c = 0;
         for (int i = 0; i < s.length(); i++) {
             c = c * 96 + (byte) s.charAt(i) - 32;
         }
         return c;
     }
 
     private static long maxLex(int len) {
         // computes the maximum number that can be coded with a lex-encoded String of length len
         long c = 0;
         for (int i = 0; i < len; i++) {
             c = c * 96 + 90;
         }
         return c;
     }
 
     private static long minLex(int len) {
         // computes the minimum number that can be coded with a lex-encoded String of length len
         long c = 0;
         for (int i = 0; i < len; i++) {
             c = c * 96 + 13;
         }
         return c;
     }
 
     public static final long minDHTNumber   = minLex(9);
     public static final long maxDHTDistance = maxLex(9) - minDHTNumber;
 
     public long dhtDistance(String wordhash) {
         // computes a virtual distance, the result must be set in relation to maxDHTDistace
         // if the distance is small, this peer is more responsible for that word hash
         // if the distance is big, this peer is less responsible for that word hash
         final long myPos = decodeLex(hash.substring(0,9));
         final long wordPos = decodeLex(wordhash.substring(0,9));
         return (myPos > wordPos) ? (myPos - wordPos) : (myPos + maxDHTDistance - wordPos);
     }
 
     public long dhtDistance() {
         // returns an absolute value
         return decodeLex(hash.substring(0,9)) - minDHTNumber;
     }
 
     public static yacySeed genLocalSeed(plasmaSwitchboard sb) {
     // genera a seed for the local peer
     // this is the birthplace of a seed, that then will start to travel to other peers
 
     // at first we need a good peer hash
     // that hash should be as static as possible, so that it depends mainly on system
     // variables and can even then be reconstructed if the local seed has disappeared
     final Properties sp = System.getProperties();
     final String slow = 
         sp.getProperty("file.encoding", "") + 
         sp.getProperty("file.separator", "") + 
         sp.getProperty("java.class.path", "") + 
         sp.getProperty("java.vendor", "") +
         sp.getProperty("os.arch", "") +
         sp.getProperty("os.name", "") +
         sp.getProperty("path.separator", "") +
         sp.getProperty("user.dir", "") +
         sp.getProperty("user.home", "") +
         sp.getProperty("user.language", "") +
         sp.getProperty("user.name", "") +
         sp.getProperty("user.timezone", "");
     final String medium =
         sp.getProperty("java.class.version", "") +
         sp.getProperty("java.version", "") +
         sp.getProperty("os.version", "") +
         sb.getConfig("peerName", "noname");
     final String fast = Long.toString(System.currentTimeMillis());
     // the resultinh hash does not have any information than can be used to reconstruct the
     // original system information that has been collected here to create the hash
     // We simply distinuguish three parts of the hash: slow, medium and fast changing character of system idenfification
     // the Hash is constructed in such a way, that the slow part influences the main aerea of the distributed hash location
     // more than the fast part. The effect is, that if the peer looses it's seed information and is reconstructed, it
     // still hosts most information of the distributed hash the an appropriate 'position'
     String hash =
         serverCodings.encodeMD5B64(slow, true).substring(0, 4) + 
         serverCodings.encodeMD5B64(medium, true).substring(0, 4) + 
         serverCodings.encodeMD5B64(fast, true).substring(0, 4);
     yacyCore.log.logInfo("init: OWN SEED = " + hash);
 
     if (hash.length() != yacySeedDB.commonHashLength) {
         yacyCore.log.logSevere("YACY Internal error: distributed hash conceptual error");
         System.exit(-1);
     }
 
     final yacySeed newSeed = new yacySeed(hash);
 
     // now calculate other information about the host
     newSeed.dna.put("Name", sb.getConfig("peerName", "unnamed"));
     if ((serverCore.portForwardingEnabled) && (serverCore.portForwarding != null)) {
         newSeed.dna.put("Port",Integer.toString(serverCore.portForwarding.getPort()));
     } else {
         newSeed.dna.put("Port", sb.getConfig("port", "8080"));
     }
     newSeed.dna.put("BDate", yacyCore.universalDateShortString(new Date()));
     newSeed.dna.put(STR_LASTSEEN, newSeed.dna.get("BDate")); // just as initial setting
     newSeed.dna.put("UTC", serverDate.UTCDiffString());
     newSeed.dna.put(PEERTYPE, PEERTYPE_VIRGIN);
 
     return newSeed;
     }
 
     public static yacySeed genRemoteSeed(String seedStr, String key) {
         // this method is used to convert the external representation of a seed into a seed object
        if (seedStr == null || seedStr.length() < yacySeedDB.commonHashLength) { return null; }
         final String seed = crypt.simpleDecode(seedStr, key);
         if (seed == null) { return null; }
         final HashMap dna = serverCodings.string2map(seed);
         final String hash = (String) dna.remove("Hash");
         return new yacySeed(hash, dna);
     }
 
     public String toString() {       
         synchronized (dna) {
             dna.put("Hash", this.hash);      // set hash into seed code structure
             final String s = dna.toString(); // generate string representation
             dna.remove("Hash");              // reconstruct original: hash is stored external
             return s;
         }
     }
 
     public String genSeedStr(String key) {
         // use a default encoding
         return genSeedStr('b', key);
     }
 
     public String genSeedStr(char method, String key) {
         return crypt.simpleEncode(toString(), key, method);
     }
 
     public String isProper() {
         // checks if everything is ok with that seed
         if (this.hash == null) { return "hash is null"; }
         if (this.hash.length() != yacySeedDB.commonHashLength) { return "wrong hash length (" + this.hash.length() + ")"; }
         final String ip = (String) dna.get("IP");
         if (ip == null) { return "IP is null"; }
         if (ip.length() < 8) { return "IP is too short: " + ip; }
         if (!natLib.isProper(ip)) { return "IP is not proper: " + ip; }
         return null;
     }
 
     public void save(File f) throws IOException {
         final String out = genSeedStr('p', null);
         final FileWriter fw = new FileWriter(f);
         fw.write(out, 0, out.length());
         fw.close();
     }
 
     public static yacySeed load(File f) throws IOException {
         final FileReader fr = new FileReader(f);
         final char[] b = new char[(int) f.length()];
         fr.read(b, 0, b.length);
         fr.close();
         return genRemoteSeed(new String(b), null);
     }
 
     public Object clone() {
         return new yacySeed(this.hash, (HashMap) (new HashMap(dna)).clone());
     }
 
     /*
     public static void main(String[] argv) {
     try {
         plasmaSwitchboard sb = new plasmaSwitchboard("../httpProxy.init", "../httpProxy.conf");
         yacySeed ys = genLocalSeed(sb);
         String yp, yz, yc;
         System.out.println("YACY String    = " + ys.toString());
         System.out.println("YACY SeedStr/p = " + (yp = ys.genSeedStr('p', null)));
         //System.out.println("YACY SeedStr/z = " + (yz = ys.genSeedStr('z', null)));
         System.out.println("YACY SeedStr/c = " + (yc = ys.genSeedStr('c', "abc")));
         System.out.println("YACY remote/p  = " + genRemoteSeed(yp, null).toString());
         //System.out.println("YACY remote/z  = " + genRemoteSeed(yz, null).toString());
         System.out.println("YACY remote/c  = " + genRemoteSeed(yc, "abc").toString());
         System.exit(0);
     } catch (IOException e) {
         e.printStackTrace();
     }
     }
     */
 
 }
