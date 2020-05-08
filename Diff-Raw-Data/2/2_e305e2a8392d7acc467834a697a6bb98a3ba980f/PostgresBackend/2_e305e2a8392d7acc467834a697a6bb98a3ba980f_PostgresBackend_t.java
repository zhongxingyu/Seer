 package org.fnppl.opensdx.keyserver;
 
 /*
  * Copyright (C) 2010-2011 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
 */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Arrays;
 import java.util.Vector;
 
 import org.fnppl.opensdx.common.Util;
 import org.fnppl.opensdx.gui.DefaultMessageHandler;
 import org.fnppl.opensdx.keyserver.helper.IdGenerator;
 import org.fnppl.opensdx.keyserver.helper.SQLStatement;
 import org.fnppl.opensdx.security.Identity;
 import org.fnppl.opensdx.security.KeyApprovingStore;
 import org.fnppl.opensdx.security.KeyLog;
 import org.fnppl.opensdx.security.KeyLogAction;
 import org.fnppl.opensdx.security.KeyStatus;
 import org.fnppl.opensdx.security.MasterKey;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.security.SecurityHelper;
 import org.fnppl.opensdx.security.Signature;
 import org.fnppl.opensdx.security.SubKey;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 public class PostgresBackend implements KeyServerBackend {
 
 	private String DB_DRIVER = "org.postgresql.Driver";
 	
 	public Connection con;
 	private File data_path = null;
 	
 	private PostgresBackend() {
 		con = null;
 		
 		//Load DB_Driver
 		try {
 			Class.forName(DB_DRIVER);
 		} catch (Exception e) {}
 	}
 	
 	public static PostgresBackend init(String user, String pw, String dbname, File data_path) {
 		PostgresBackend be = new PostgresBackend();
 		if (data_path==null) {
 			be.data_path = new File(System.getProperty("user.home"), "db_data");
 		} else {
 			be.data_path = data_path;
 		}
 		be.data_path.mkdirs();
 		
 		System.out.println("PostgresBackend::init::using data_path: "+be.data_path.getAbsolutePath());
 		be.connect(user, pw, dbname);
 		return be;
 	}
 	
 	private File getFileFromID(long id, String ending) {
 		String name = ""+id;
 		if (name.length()>5) {
 			name = name.substring(0,name.length()-5)+File.separator+name;
 		}
 		if (ending!=null) name += ending;
 		return new File(data_path, name);
 	}
 	
 	public void addKeysAndLogsFromKeyStore(String filename) {
 		try {
 			File f = new File(filename);
 			KeyApprovingStore store = KeyApprovingStore.fromFile(f, new DefaultMessageHandler());
 			Vector<OSDXKey> keys = store.getAllKeys();
 			for (OSDXKey key : keys) {
 				addKey(key);
 			}
 			Vector<KeyLog> logs =store.getKeyLogs();
 			for (KeyLog log : logs) {
 				addKeyLog(log);
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public boolean hasKey(String keyid) {
 		keyid = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 		boolean has = false;
 		try {
 			SQLStatement sql = new SQLStatement("SELECT keysha1 FROM keys WHERE keysha1=?");
 			sql.setString(1, keyid);
 			
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			if (rs.next()) {
 				has = true;
 			}
 			rs.close();
 			stmt.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return has;
 	}
 	
 	public void setupEmptyDB() {
 		URL emptyDB = PostgresBackend.class.getResource("resources/setupEmptyDB.txt");
 		try {
 			Statement stmt = con.createStatement();
             BufferedReader in = new BufferedReader(new InputStreamReader(emptyDB.openStream()));
             String line = null;
             String nextCommand = "";
             while ((line = in.readLine())!=null) {
             	int trenn = line.indexOf(";");
             	if (trenn>=0) {
             		nextCommand += line.substring(0,trenn);
             		//executeCommand
             		if (nextCommand.length()>3) {
     					try {
     					System.out.println("SQL::"+nextCommand);
     					stmt.execute(nextCommand);
     					} catch (SQLException innerEx) {
     						innerEx.printStackTrace();
     					}		
     				}
             		nextCommand = line.substring(trenn+1);
             	} else {
             		nextCommand += line;
             	}
             }
             //execute last command
 			if (nextCommand.length()>3) {
 				try {
 				System.out.println("SQL::"+nextCommand);
 				stmt.execute(nextCommand);
 				} catch (SQLException innerEx) {
 					innerEx.printStackTrace();
 				}		
 			}
             in.close();
         } catch (Exception ex) {
         	ex.printStackTrace();
         }
         
 //        CREATE TABLE "keylogs" (
 //        		"id"				BIGINT,
 //        		"ipv4"				VARCHAR(15),
 //        		"ipv6"				VARCHAR(100),
 //        		"keyid_to" 			VARCHAR(200),
 //        		"action"			VARCHAR(30),
 //        		"action_id"			BIGINT,
 //        		"action_msg"		VARCHAR(200),
 //        		"sha256_complete"	BYTEA NOT NULL,
 //        		"sha256_restricted"	BYTEA NOT NULL,
 //        		"asig_md5"			BYTEA NOT NULL,
 //        		"asig_sha1"			BYTEA NOT NULL,
 //        		"asig_sha256"		BYTEA NOT NULL,
 //        		"asig_datetime"		TIMESTAMP,
 //        		"asig_dataname"		VARCHAR(200),
 //        		"asig_keyid"		VARCHAR(200),
 //        		"asig_bytes" 		BYTEA NOT NULL,
 //        		"sha256"			BYTEA NOT NULL,
 //        		"sig_md5"			BYTEA NOT NULL,
 //        		"sig_sha1"			BYTEA NOT NULL,
 //        		"sig_sha256"		BYTEA NOT NULL,
 //        		"sig_datetime"		TIMESTAMP,
 //        		"sig_dataname"		VARCHAR(200),
 //        		"sig_keyid"			VARCHAR(200),
 //        		"sig_bytes" 		BYTEA NOT NULL,
 //        		PRIMARY KEY(id),
 //        		FOREIGN KEY (asig_keyid) REFERENCES keys(keyid),
 //        		FOREIGN KEY (sig_keyid) REFERENCES keys(keyid)
 //        	);;
 	}
 	
 	public void connect(String user, String pw, String dbname) {
 		try {
 			con = DriverManager.getConnection(dbname, user, pw);
 			System.out.println("Connection established DB: "+dbname); 
 		} catch (Exception e) {
 			con = null;
 			e.printStackTrace();
 			throw new RuntimeException("Connection to DB could not be established.");
 		}
 	}
 	
 	public boolean isConnected() {
 		if (con==null) {
 			return false;
 		} else {
 			return true;
 		}
 	}
 	
 	
 	public void closeDBConnection() {
 		try {
 			con.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void addKey(OSDXKey key) {
 		if (hasKey(key.getKeyID())) return;
 		try {
 			SQLStatement sql = new SQLStatement("INSERT INTO keys (keysha1, keyserver, level, usage, valid_from, valid_until, algo, bits, modulus, exponent, parentkeysha1, parentkeyserver) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)");
 			sql.setString(1, key.getKeyModulusSHA1());
 			sql.setString(2, key.getAuthoritativekeyserver());
 			sql.setString(3, key.getLevelName());
 			sql.setString(4, key.getUsageName());
 			sql.setTimestamp(5, new Timestamp(key.getValidFrom()));
 			sql.setTimestamp(6, new Timestamp(key.getValidUntil()));
 			sql.setString(7, "RSA");
 			sql.setInt(8, key.getPubKey().getBitCount());
 			sql.setString(9, SecurityHelper.HexDecoder.encode(key.getPublicModulusBytes(),':',-1));
 			sql.setString(10, "0x"+SecurityHelper.HexDecoder.encode(key.getPubKey().getPublicExponentBytes(),':',-1));
 			
 			if (key.isSub() && ((SubKey)key).getParentKeyID()!=null) {
 				String pkid = ((SubKey)key).getParentKeyID();
 				String pkid_sha1 = OSDXKey.getFormattedKeyIDModulusOnly(pkid);
 				String pkid_ks = OSDXKey.getKeyServerFromKeyID(pkid);
 				if (pkid_ks==null || pkid_ks.equals("")) {
 					pkid_ks = key.getAuthoritativekeyserver();
 				}
 				sql.setString(11, pkid_sha1);
 				sql.setString(12, pkid_ks);
 			} else {
 				sql.setString(11, "");
 				sql.setString(12, key.getAuthoritativekeyserver());
 			}
 			
 			Statement stmt = con.createStatement();
 			System.out.println("addKey:: "+sql.toString());
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 			
 			//add identities for masterkey
 			if (key.isMaster()) {
 				Vector<Identity> ids = ((MasterKey)key).getIdentities();
 				if (ids!=null) {
 					for (Identity id : ids) {
 						addIdentity(id, key.getKeyID());
 					}
 				}
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public OSDXKey getKey(String keyid) {
 		String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 		OSDXKey key = null;
 		try {
 			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE keysha1=?");
 			sql.setString(1, keysha1);
 			
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			
 			if (rs.next()) {
 				key = buildKey(rs);
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return key;
 	}
 	
 	private OSDXKey buildKey(ResultSet rs) {
 		try {
 			Element pk = new Element("pubkey");
 			pk.addContent("keyid", rs.getString("keysha1")+"@"+rs.getString("keyserver"));
 			pk.addContent("level", rs.getString("level"));
 			pk.addContent("usage", rs.getString("usage"));
 			pk.addContent("valid_from", SecurityHelper.getFormattedDate(rs.getTimestamp("valid_from").getTime()));
 			pk.addContent("valid_until", SecurityHelper.getFormattedDate(rs.getTimestamp("valid_until").getTime()));
 			pk.addContent("algo", rs.getString("algo"));
 			pk.addContent("bits", ""+rs.getInt("bits"));
 			pk.addContent("modulus", rs.getString("modulus"));
 			pk.addContent("exponent", rs.getString("exponent"));
 			//Document.buildDocument(pk).output(System.out);
 			OSDXKey key = OSDXKey.fromPubKeyElement(pk);
 			if (key.isMaster()) {
 				//all OR only last IDs??
 				
 				//all for identities request
 				Vector<Identity> ids = getIdentities(key.getKeyID());
 				for (Identity idd : ids) {
 					((MasterKey)key).addIdentity(idd);
 				}
 				
 //				Identity idd = getLastIdentity(key.getKeyID());
 //				if (idd!=null) {
 //					((MasterKey)key).addIdentity(idd);
 //				}
 			}
 			if (key.isSub()) {
 				String parentkeysha1 = rs.getString("parentkeysha1");
 				if (parentkeysha1!=null && parentkeysha1.length()>0) {
 					((SubKey)key).setParentKeyID(parentkeysha1+"@"+rs.getString("parentkeyserver"));
 				}
 			}
 			return key;
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public Vector<Identity> getIdentities(String keyid) {
 		Vector<Identity> ids = new Vector<Identity>();
 		try {
 			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 			SQLStatement sql = new SQLStatement("SELECT * FROM identities WHERE keysha1=? ORDER BY identnum");
 			sql.setString(1, keysha1);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			while (rs.next()) {
 				Identity id = buildIdentitiy(rs);
 				if (id!=null) {
 					ids.add(id);
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		return ids;
 	}
 	
 	public Identity getLastIdentity(String keyid) {
 		try {
 			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 			SQLStatement sql = new SQLStatement("SELECT * FROM identities WHERE keysha1=? ORDER BY identnum DESC LIMIT 1");
 			sql.setString(1, keysha1);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			Identity id = null;
 			if (rs.next()) {
 				id = buildIdentitiy(rs);
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 			return id;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 //	public long addIdentity(Identity id) {
 //		return addIdentity(id, null);
 //	}
 
 	public long addIdentity(Identity id, String keyid) {
 		long idid = IdGenerator.getTimestamp();
 		try {
 			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 			SQLStatement sql = new SQLStatement("INSERT INTO identities (keysha1, keyserver, identnum, email, mnemonic, mnemonic_r, company, company_r, unit, unit_r, subunit, subunit_r, function, function_r, surname, surname_r, firstname, firstname_r, middlename, middlename_r, birthday, birthday_r, placeofbirth, placeofbirth_r, city, city_r, postcode, postcode_r, region, region_r, country, country_r, phone, phone_r, fax, fax_r, note, note_r, photo_id, photo_md5, photo_r, most_recent) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
 			sql.setString(1, keysha1);
 			sql.setString(2, OSDXKey.getKeyServerFromKeyID(keyid));
 			
 			sql.setInt(3, id.getIdentNum());
 			sql.setString(4, id.getEmail());
 			sql.setString(5, id.getMnemonic());
 			sql.setBoolean(6, id.is_mnemonic_restricted());
 			sql.setString(7, id.getCompany());
 			sql.setBoolean(8, id.is_company_restricted());
 			sql.setString(9, id.getUnit());
 			sql.setBoolean(10, id.is_unit_restricted());
 			sql.setString(11, id.getSubunit());
 			sql.setBoolean(12, id.is_subunit_restricted());
 			sql.setString(13, id.getFunction());
 			sql.setBoolean(14, id.is_function_restricted());
 			sql.setString(15, id.getSurname());
 			sql.setBoolean(16, id.is_surname_restricted());
 			sql.setString(17, id.getFirstNames());
 			sql.setBoolean(18, id.is_firstname_s_restricted());
 			sql.setString(19, id.getMiddlename());
 			sql.setBoolean(20, id.is_middlename_restricted());
 			sql.setString(21, id.getBirthdayGMTString());
 			sql.setBoolean(22, id.is_birthday_gmt_restricted());
 			sql.setString(23, id.getPlaceOfBirth());
 			sql.setBoolean(24, id.is_placeofbirth_restricted());
 			sql.setString(25, id.getCity());
 			sql.setBoolean(26, id.is_city_restricted());
 			sql.setString(27, id.getPostcode());
 			sql.setBoolean(28, id.is_postcode_restricted());
 			sql.setString(29, id.getRegion());
 			sql.setBoolean(30, id.is_region_restricted());
 			sql.setString(31, id.getCountry());
 			sql.setBoolean(32, id.is_country_restricted());
 			sql.setString(33, id.getPhone());
 			sql.setBoolean(34, id.is_phone_restricted());
 			sql.setString(35, id.getFax());
 			sql.setBoolean(36, id.is_fax_restricted());
 			sql.setString(37, id.getNote());
 			sql.setBoolean(38, id.is_note_restricted());
 			if (id.getPhotoBytes()!=null) {
 				long photoId = IdGenerator.getTimestamp();
 				String photoMD5 = SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5(id.getPhotoBytes()),':',-1);
 				File f = getFileFromID(photoId, ".png");
 				f.getParentFile().mkdirs();
 				Util.saveBytesToFile(id.getPhotoBytes(), f);
 				sql.setLong(39, photoId);
 				sql.setString(40, photoMD5);
 			} else {
 				sql.setLong(39, -1L);
 				sql.setString(40, null);
 			}
 			sql.setBoolean(41, id.is_photo_restricted());
 			sql.setBoolean(42, true); //most_recent
 			
 			Statement stmt = con.createStatement();
 			//System.out.println("add identity:: "+sql.toString());
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 			
 			//set all other most_recent for this keyid to false
 			sql = new SQLStatement("UPDATE identities SET most_recent=\'false\' WHERE keysha1=? AND identnum<>?");
 			sql.setString(1, keysha1);
 			sql.setInt(2, id.getIdentNum());
 			
 			stmt = con.createStatement();
 			stmt.executeUpdate(sql.toString());
 			
 //			if (keyid!=null) {
 //				long kiid = IdGenerator.getTimestamp();
 //				sql = new SQLStatement("INSERT INTO key_identity (id, keyid, identity) VALUES (?,?,?)");
 //				sql.setLong(1, kiid);
 //				sql.setString(2, keyid);
 //				sql.setLong(3, idid);
 //				stmt = con.createStatement();
 //				stmt.executeUpdate(sql.toString());
 //				stmt.close();
 //			}
 			return idid;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}	
 		return -1L;
 	}
 	
 	private Identity buildIdentitiy(ResultSet rs) {
 		try {
 			Identity idd = null;
 //			SQLStatement sql = new SQLStatement("SELECT * FROM identities WHERE id=?");
 //			sql.setLong(1, id);
 //			Statement stmt = con.createStatement();
 //			ResultSet rs = stmt.executeQuery(sql.toString());
 //			if (rs.next()) {
 				try {
 					idd = Identity.newEmptyIdentity();
 					idd.setIdentNum(rs.getInt("identnum"));
 					idd.setEmail(rs.getString("email"));
 					idd.setMnemonic(rs.getString("mnemonic"));
 					idd.set_mnemonic_restricted(rs.getBoolean("mnemonic_r"));
 					idd.setCompany(rs.getString("company"));
 					idd.set_company_restricted(rs.getBoolean("company_r"));
 					idd.setUnit(rs.getString("unit"));
 					idd.set_unit_restricted(rs.getBoolean("unit_r"));
 					idd.setSubunit(rs.getString("subunit"));
 					idd.set_subunit_restricted(rs.getBoolean("subunit_r"));
 					idd.setFunction(rs.getString("function"));
 					idd.set_function_restricted(rs.getBoolean("function_r"));
 					idd.setSurname(rs.getString("surname"));
 					idd.set_surname_restricted(rs.getBoolean("surname_r"));
 					idd.setMiddlename(rs.getString("middlename"));
 					idd.set_middlename_restricted(rs.getBoolean("middlename_r"));
 					String bd = rs.getString("birthday");
 					if (bd!=null) {
 						idd.setBirthday_gmt(bd);
 					}
 					idd.set_birthday_gmt_restricted(rs.getBoolean("birthday_r"));
 					idd.setPlaceofbirth(rs.getString("placeofbirth"));
 					idd.set_placeofbirth_restricted(rs.getBoolean("placeofbirth_r"));
 					idd.setCity(rs.getString("city"));
 					idd.set_city_restricted(rs.getBoolean("city_r"));
 					idd.setPostcode(rs.getString("postcode"));
 					idd.set_postcode_restricted(rs.getBoolean("postcode_r"));
 					idd.setRegion(rs.getString("region"));
 					idd.set_region_restricted(rs.getBoolean("region_r"));
 					idd.setCountry(rs.getString("country"));
 					idd.set_country_restricted(rs.getBoolean("country_r"));
 					idd.setPhone(rs.getString("phone"));
 					idd.set_phone_restricted(rs.getBoolean("phone_r"));
 					idd.setFax(rs.getString("fax"));
 					idd.set_fax_restricted(rs.getBoolean("fax_r"));
 					idd.setNote(rs.getString("note"));
 					idd.set_note_restricted(rs.getBoolean("note_r"));
 					long photoId = rs.getLong("photo_id");
 					if (photoId!=-1L) {
 						File f = getFileFromID(photoId, ".png");
 						if (!f.exists()) {	
 							throw new RuntimeException("DB DataBackend Error: File "+f.getAbsolutePath()+" does not exist.");
 						}
 						byte[] calc_md5 = SecurityHelper.getMD5(f);
 						byte[] given_md5 = SecurityHelper.HexDecoder.decode(rs.getString("photo_md5"));
 						if (!Arrays.equals(calc_md5, given_md5)) {
 							throw new RuntimeException("DB DataBackend Error: MD5 Check for file "+f.getAbsolutePath()+" FAILED!");
 						}
 						idd.setPhoto(f);
 					}
 					idd.set_photo_restricted(rs.getBoolean("photo_r"));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 //			}
 //			rs.close();
 //			stmt.close();
 //			//con.close();
 			return idd;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	
 	public void removeKey(OSDXKey key) {
 		try {
 			//TODO remove Identitites to key
 			String keysha1 = key.getKeyModulusSHA1();
 			SQLStatement sql = new SQLStatement("REMOVE FROM keys WHERE keysha1=?");
 			sql.setString(1, key.getKeyID());
 			
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	public void addKeyLog(KeyLog log) {
 		addKeyLogWithIdResult(log);
 	}
 	public long addKeyLogWithIdResult(KeyLog log) {
 		Signature asig = log.getActionSignature();
 		Signature sig = log.getSignature();
 		
 		addKey(asig.getKey());
 		addKey(sig.getKey());
 		
 		try {
 			long ts = IdGenerator.getTimestamp();
 			File f = getFileFromID(ts, "_keylog.xml");
 			f.getParentFile().mkdirs();
 			Document.buildDocument(log.toElement(true)).writeToFile(f);
 			String md5 = SecurityHelper.HexDecoder.encode(SecurityHelper.getMD5(f), ':', -1);
 			SQLStatement sql = new SQLStatement("INSERT INTO keylogs (keylogid, keylog_md5, keysha1_to, keyserver_to, keysha1_from, keyserver_from, asig_datetime, sig_datetime) VALUES (?,?,?,?,?,?,?,?)");
 			sql.setLong(1, ts);
 			sql.setString(2, md5);
 			String to_id = log.getKeyIDTo(); 
 			sql.setString(3, OSDXKey.getFormattedKeyIDModulusOnly(to_id));
 			sql.setString(4, OSDXKey.getKeyServerFromKeyID(to_id));
 			String from_id = log.getKeyIDFrom(); 
 			sql.setString(5, OSDXKey.getFormattedKeyIDModulusOnly(from_id));
 			sql.setString(6, OSDXKey.getKeyServerFromKeyID(from_id));
 			
 			sql.setTimestamp(7, new Timestamp(asig.getSignDatetime()));
 			sql.setTimestamp(8, new Timestamp(sig.getSignDatetime()));
 			
 			System.out.println("addKeylog:: "+sql.toString());
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 			return ts;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return -1L;
 	}
 	
 //	public void addKeyLog(KeyLog log) {
 //		Signature asig = log.getActionSignature();
 //		Signature sig = log.getSignature();
 //		
 //		addKey(asig.getKey());
 //		addKey(sig.getKey());
 //		
 //		try {
 //			long ts = IdGenerator.getTimestamp();
 //			
 //			SQLStatement sql = new SQLStatement("INSERT INTO keylogs (id, ipv4, ipv6, keyid_to, action, action_id, action_msg, " +
 //					"sha256_complete, sha256_restricted, asig_md5, asig_sha1, asig_sha256, asig_datetime, asig_dataname, asig_keyid, asig_bytes," +
 //												 "sha256, sig_md5,  sig_sha1,  sig_sha256,  sig_datetime,  sig_dataname,  sig_keyid,  sig_bytes)" +
 //					" VALUES (?,?,?,?,?,? ,?,?,?,?,?,? ,?,?,?,?,?,? ,?,?,?,?,?,?)");
 //			sql.setLong(1, ts);
 //			sql.setString(2, log.getIPv4());
 //			sql.setString(3, log.getIPv6());
 //			sql.setString(4, log.getKeyIDTo());
 //			sql.setString(5, log.getAction());
 //			Identity idd = log.getIdentity();
 //			if (idd!=null) {
 //				long idid = addIdentity(idd);
 //				sql.setLong(6, idid);
 //			}
 //			sql.setString(7, log.getMessage());
 //			
 //			//action signature
 //			sql.setBytes(8, log.getActionSha256ProofComplete());
 //			sql.setBytes(9, log.getActionSha256ProofRestricted());
 //			sql.setBytes(10, asig.getMD5());
 //			sql.setBytes(11, asig.getSHA1());
 //			sql.setBytes(12, asig.getSHA256());
 //			sql.setTimestamp(13, new Timestamp(asig.getSignDatetime()));
 //			sql.setString(14, asig.getDataName());
 //			sql.setString(15, asig.getKey().getKeyID());
 //			sql.setBytes(16, asig.getSignatureBytes());
 //			
 //			//signature
 //			sql.setBytes(17, log.getSHA256LocalProof());
 //			sql.setBytes(18, sig.getMD5());
 //			sql.setBytes(19, sig.getSHA1());
 //			sql.setBytes(20, sig.getSHA256());
 //			sql.setTimestamp(21, new Timestamp(sig.getSignDatetime()));
 //			sql.setString(22, sig.getDataName());
 //			sql.setString(23, sig.getKey().getKeyID());
 //			sql.setBytes(24, sig.getSignatureBytes());
 //			
 //			sql.executeUpdate();
 //			sql.close();
 //		} catch (Exception ex) {
 //			ex.printStackTrace();
 //		}
 //	}
 
 	
 	private long getKeylogIndex(KeyLog log) {
 		long index = -1L;
 		try {
 			Signature asig = log.getActionSignature();
 			
 			SQLStatement sql = new SQLStatement("SELECT keylogid FROM keylogs WHERE keysha1_to=? AND keysh1_from=? AND asig_datetime=?");
 			sql.setString(1, OSDXKey.getFormattedKeyIDModulusOnly(log.getKeyIDTo()));
 			sql.setString(2, OSDXKey.getFormattedKeyIDModulusOnly(log.getKeyIDFrom()));
 			sql.setTimestamp(3, new Timestamp(asig.getSignDatetime()));
 			System.out.println("getKeyLogIndex :: "+sql.toString());
 			
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			if (rs.next()) {
 				try {
 					index = rs.getLong(1);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return index;
 	}
 	
 	public void addOpenToken(String token, KeyLog log) {
 		long klIndex = getKeylogIndex(log);
 		if (klIndex<=0) {
 			klIndex = addKeyLogWithIdResult(log);
 		}
 		try {
 			SQLStatement sql = new SQLStatement("INSERT INTO approval_token (approvalid, token, keylogid) VALUES (?,?,?)");
 			long approvalid = IdGenerator.getTimestamp();
 			sql.setLong(1, approvalid);
 			sql.setString(2, token);
 			sql.setLong(3, klIndex);
 			
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public KeyLog getKeyLogFromTokenId(String id) {
 //		long klIndex = -1;
 		KeyLog log = null;
 		try {
 			SQLStatement sql = new SQLStatement("SELECT * FROM approval_token, keylogs WHERE approval_token.keylogid = keylogs.keylogid AND token=?");
 			sql.setString(1, id);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			if (rs.next()) {
 				try {
 					log = buildKeylog(rs);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 //		if (klIndex<0) {
 //			return null;
 //		}
 //		try {
 //			SQLStatement sql = new SQLStatement("SELECT * FROM keylogs WHERE id=?");
 //			sql.setLong(1, klIndex);
 //			Statement stmt = con.createStatement();
 //			ResultSet rs = stmt.executeQuery(sql.toString());
 //			if (rs.next()) {
 //				try {
 //					log = buildKeylog(rs);
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //				}
 //			}
 //			rs.close();
 //			stmt.close();
 //			//con.close();
 //		} catch (Exception ex) {
 //			ex.printStackTrace();
 //		}
 		return log;
 	}
 	
 	private KeyLog buildKeylog(ResultSet rs) {
 		try {
 			long id = rs.getLong("keylogid");
 			File f = getFileFromID(id, "_keylog.xml");
 			if (!f.exists()) {	
 				throw new RuntimeException("DB DataBackend Error: File "+f.getAbsolutePath()+" does not exist.");
 			}
 			byte[] calc_md5 = SecurityHelper.getMD5(f);
 			byte[] given_md5 = SecurityHelper.HexDecoder.decode(rs.getString("keylog_md5"));
 			if (!Arrays.equals(calc_md5, given_md5)) {
 				throw new RuntimeException("DB DataBackend Error: MD5 Check for file "+f.getAbsolutePath()+" FAILED!");
 			}
 			Element e = Document.fromFile(f).getRootElement();
 			KeyLog log = KeyLog.fromElement(e);
 			return log;
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return null; 
 	}
 	
 //	private KeyLog buildKeylog(ResultSet rs) {
 //		try {
 //			OSDXKey asigKey = getKey(rs.getString("asig_keyid"));
 //			Element e = new Element("keylog");
 //			e.addContent("ipv4",rs.getString("ipv4"));
 //			e.addContent("ipv6",rs.getString("ipv6"));
 //			
 //			Element ea = new Element("keylogaction");
 //			//action content
 //			ea.addContent("from_keyid", asigKey.getKeyID());
 //			ea.addContent("to_keyid", rs.getString("keyid_to"));
 //			Element eaa = new Element(rs.getString("action"));
 //			Identity idd = getIdentitiy(rs.getLong("action_id"));
 //			if (idd!=null) {
 //				eaa.addContent(idd.toElement(true));
 //			}
 //			String msg = rs.getString("action_msg");
 //			if (msg!=null) {
 //				eaa.addContent("message", msg);
 //			}
 //			ea.addContent(eaa);
 //			ea.addContent("sha256localproof_complete", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_complete"),':',-1));
 //			ea.addContent("sha256localproof_restricted", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256_restricted"),':',-1));
 //			
 //			Element asig = new Element("signature");
 //			Element asigData = new Element("data");
 //			asigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_md5"),':',-1));
 //			asigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha1"),':',-1));
 //			asigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_sha256"),':',-1));
 //			asigData.addContent("signdatetime", SecurityHelper.getFormattedDate(rs.getTimestamp("asig_datetime").getTime()));
 //			asigData.addContent("dataname", rs.getString("asig_dataname"));
 //			
 //			asig.addContent(asigData);
 //			asig.addContent(asigKey.getSimplePubKeyElement());
 //			asig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("asig_bytes"),'\0',-1));
 //			
 //			ea.addContent(asig);
 //			
 //			e.addContent(ea);
 //			
 //			e.addContent("sha256localproof", SecurityHelper.HexDecoder.encode(rs.getBytes("sha256"),':',-1));
 //			Element sig = new Element("signature");
 //			Element sigData = new Element("data");
 //			sigData.addContent("md5", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_md5"),':',-1));
 //			sigData.addContent("sha1", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha1"),':',-1));
 //			sigData.addContent("sha256", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_sha256"),':',-1));
 //			sigData.addContent("signdatetime", SecurityHelper.getFormattedDate(rs.getTimestamp("sig_datetime").getTime()));
 //			sigData.addContent("dataname", rs.getString("sig_dataname"));
 //			sig.addContent(sigData);
 //			sig.addContent(getKey(rs.getString("sig_keyid")).getSimplePubKeyElement());
 //			sig.addContent("signaturebytes", SecurityHelper.HexDecoder.encode(rs.getBytes("sig_bytes"),'\0',-1));
 //			e.addContent(sig);
 //			Document.buildDocument(e).output(System.out);
 //			KeyLog log = KeyLog.fromElement(e);
 //			return log;
 //		} catch (Exception ex) {
 //			ex.printStackTrace();
 //		}
 //		return null; 
 //	}
 
 	 
 	public Vector<KeyLog> getKeyLogsToID(String keyid) {
 		Vector<KeyLog> logs = new Vector<KeyLog>();
 		try {
 			System.out.println("get keylogs");
 			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(keyid);
 			SQLStatement sql = new SQLStatement("SELECT * FROM keylogs WHERE keysha1_to=? ORDER BY asig_datetime");
 			sql.setString(1, keysha1);
 			System.out.println("SQL: "+sql.toString());
 			
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			
 			while (rs.next()) {
 				try {
 					logs.add(buildKeylog(rs));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return logs;
 	}
 
 	 
 	public KeyStatus getKeyStatus(String keyid) {
 		Vector<KeyLog> kls = getKeyLogsToID(keyid);
 		if (kls==null || kls.size()==0) return null;
 		for (KeyLog kl : kls) {
 			System.out.println("found keylog... "+kl.getActionDatetimeString());
 		}
 		KeyLog kl = kls.lastElement();
 		String status = kl.getAction();
 		int validity = -1;
 		if (status.equals(KeyLogAction.APPROVAL)) {
 			validity =  KeyStatus.STATUS_VALID;
 		}
 		else if (status.equals(KeyLogAction.DISAPPROVAL)) {
 			validity =  KeyStatus.STATUS_UNAPPROVED;
 		}
 		else if (status.equals(KeyLogAction.APPROVAL_PENDING)) {
 			validity =  KeyStatus.STATUS_UNAPPROVED;
 		}
 		else if (status.equals(KeyLogAction.REVOCATION)) {
 			validity =  KeyStatus.STATUS_REVOKED;
 		}
 		
 		int approvalPoints = 100;
 		OSDXKey key = getKey(keyid);
 		long datetimeValidFrom = key.getValidFrom();
 		long datetimeValidUntil = key.getValidUntil();
 		
 		KeyStatus ks = new KeyStatus(validity, approvalPoints, datetimeValidFrom, datetimeValidUntil, kl);
 		return ks;
 	
 	}
 
 	 
 	public Vector<OSDXKey> getKeysToId(String email) {
 		Vector<OSDXKey> keys = new Vector<OSDXKey>();
 		try {
 			SQLStatement sql = new SQLStatement("SELECT DISTINCT keysha1,keyserver FROM identities WHERE email=?");
 			sql.setString(1, email);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			while (rs.next()) {
 				try {
 					OSDXKey key = getKey(rs.getString(1));
 					keys.add(key);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return keys;
 	}
 
 	 
 	public Vector<OSDXKey> getSubKeysToId(String parentkeyid) {
 		Vector<OSDXKey> keys = new Vector<OSDXKey>();
 		try {
 			String keysha1 = OSDXKey.getFormattedKeyIDModulusOnly(parentkeyid);
			SQLStatement sql = new SQLStatement("SELECT * FROM keys WHERE parentkeysha1=?");
 			sql.setString(1, keysha1);
 			System.out.println("SQL: "+sql.toString());
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(sql.toString());
 			while (rs.next()) {
 				try {
 					OSDXKey key = buildKey(rs);
 					if (key!=null) keys.add(key);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			rs.close();
 			stmt.close();
 			//con.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		return keys;
 	} 
 
 	 
 	public void removeOpenToken(String token) {
 		try {
 			SQLStatement sql = new SQLStatement("DELETE FROM approval_token WHERE token=?");
 			sql.setString(1, token);
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate(sql.toString());
 			stmt.close();
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	 
 	public void updateCache(OSDXKey k, KeyLog l) {
 		// do nothing
 	}
 
 }
 
