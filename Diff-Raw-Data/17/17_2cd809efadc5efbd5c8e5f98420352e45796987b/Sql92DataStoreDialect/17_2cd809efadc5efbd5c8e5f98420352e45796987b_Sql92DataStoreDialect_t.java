 package com.surevine.profileserver.db.jdbc.dialect;
 
 import com.surevine.profileserver.db.jdbc.JDBCDataStore.DataStoreSQLDialect;
 
 public class Sql92DataStoreDialect implements DataStoreSQLDialect {
 
 	private static final String GET_OWNER = "SELECT * FROM \"owners\" WHERE \"owner\" = ?;";
 
 	private static final String DELETE_OWNER = "DELETE FROM \"owners\" where \"owner\" = ?;";
 
 	private static final String ADD_OWNER = "INSERT INTO \"owners\" VALUES (?, NOW());";
 
 	private static final String CLEAR_ROSTER_ITEMS = "DELETE FROM \"roster\" WHERE \"owner\" = ?;";
 
 	private static final String GET_ROSTER_GROUPS = "SELECT DISTINCT(\"group\") FROM \"roster\" WHERE \"owner\" = ? ORDER BY \"group\" ASC;";
 
 	private static final String GET_ROSTER_GROUPS_FOR_VCARD = "SELECT DISTINCT(\"group\") FROM \"rostermap\" WHERE \"owner\" = ? AND \"vcard\" = ? ORDER BY \"group\" ASC;";
 
 	private static final String GET_ROSTER_GROUP = "SELECT \"group\" FROM \"roster\" WHERE \"owner\" = ? and \"user\" = ?";
 
 	private static final String ADD_ROSTER_ENTRY = "INSERT INTO \"roster\" VALUES (?, ?, ?);";
 
 	private static final String GET_VCARD = "SELECT \"vcard\' FROM \"vcards\" WHERE \"owner\" = ? AND \"name\" = ?;";
 
 	private static final String GET_PUBLIC_VCARD = "SELECT \"vcard\" FROM \"vcards\" WHERE \"owner\" = ? AND \"default\" = true;";
 
	private static final String GET_VCARD_FOR_USER = "SELECT \"vcards\".\"vcard\" FROM \"vcards\", \"rostermap\" WHERE \"rostermap\".\"owner\" = ? AND \"rostermap\".\"group\" IN (SELECT \"group\" FROM \"roster\" WHERE \"owner\" = ? AND \"user\" = ?) AND \"rostermap\".\"owner\" = \"vcards\".\"owner\" AND \"rostermap\".\"vcard\" = \"vcards\".\"name\" ORDER BY \"rostermap\".\"priority\" DESC LIMIT 1;";
 
 	private static final String CLEAR_DEFAULT = "UPDATE \"vcards\".\"vcard\" SET \"default\" = false WHERE \"owner\" = ?;";
 	
	private static final String ADD_VCARD_ENTRY = "INSERT INTO \"vcards\" VALUES(?, ?, ?, false, NOW());";
 	
 	private static final String UPDATE_VCARD_ENTRY = "UPDATE \"vcards\" SET \"vcard\" = ? WHERE \"owner\" = ? AND \"name\" = ?;";
 
 	@Override
 	public String selectOwner() {
 		return GET_OWNER;
 	}
 
 	@Override
 	public String deleteOwner() {
 		return DELETE_OWNER;
 	}
 
 	@Override
 	public String addOwner() {
 		return ADD_OWNER;
 	}
 
 	@Override
 	public String clearRoster() {
 		return CLEAR_ROSTER_ITEMS;
 	}
 
 	@Override
 	public String getRosterGroups() {
 		return GET_ROSTER_GROUPS;
 	}
 
 	@Override
 	public String getRosterGroupsForVCard() {
 		return GET_ROSTER_GROUPS_FOR_VCARD;
 	}
 
 	@Override
 	public String addRosterEntry() {
 		return ADD_ROSTER_ENTRY;
 	}
 
 	@Override
 	public String getRosterGroup() {
 		return GET_ROSTER_GROUP;
 	}
 
 	@Override
 	public String getVcard() {
 		return GET_VCARD;
 	}
 
 	@Override
 	public String getVcardForUser() {
 		return GET_VCARD_FOR_USER;
 	}
 
 	@Override
 	public String getPublicVcard() {
 		return GET_PUBLIC_VCARD;
 	}
 
 	public String clearDefault() {
 		return CLEAR_DEFAULT;
 	}
 
 	public String updateVCard() {
 		return UPDATE_VCARD_ENTRY;
 	}
 
 	public String addVCard() {
 		return ADD_VCARD_ENTRY;
 	}
 }
