 package models.raidtracker;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import models.wowapi.character.Avatar;
 import play.db.jpa.Model;
 import play.libs.Codec;
 
 @Entity
 public class RaidMember extends Model {
 
 	/**
 	 * Finds an RaidMember by name and realm, Binary Safe!
 	 * 
 	 * @param name
 	 * @param realm
 	 * @return RaidMember
 	 */
 	public static RaidMember findByName(String name) {
 		return RaidMember.find("hash = ?", Codec.hexMD5(name)).first();
		// try {
		// PreparedStatement ps =
		// DB.getConnection().prepareStatement("select id from RaidMember where BINARY name = ?");
		// ps.setString(1, name);
		// ResultSet rs = ps.executeQuery();
		// if (rs.first()) {
		// Logger.info("[RaidMember][findByNameAndRealm] RaidMember " + name +
		// " found");
		// return Avatar.findById(rs.getLong("id"));
		// } else {
		// Logger.info("[RaidMember][findByNameAndRealm] RaidMember " + name +
		// " not found");
		// return null;
		// }
		//
		// } catch (SQLException e) {
		// Logger.warn("[RaidMember][FindByNameAndRealm] " +
		// e.getLocalizedMessage(), e);
		// return null;
		// }

 	}
 
 	public String name;
 	public String hash;
 	public Date betreten;
 
 	public Date verlassen;
 	@ManyToOne
 	public Avatar avatar;
 
 	@ManyToOne
 	public Raid raid;
 
 	public RaidMember(String name, Date betreten, Date verlassen, Avatar avatar, Raid raid) {
 		this.name = name;
 		this.hash = Codec.hexMD5(name);
 		this.betreten = betreten;
 		this.verlassen = verlassen;
 		this.avatar = avatar;
 		this.raid = raid;
 	}
 
 }
