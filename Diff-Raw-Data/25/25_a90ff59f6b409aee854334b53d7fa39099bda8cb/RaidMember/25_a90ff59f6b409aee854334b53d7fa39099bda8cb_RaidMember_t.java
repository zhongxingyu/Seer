 package models.raidtracker;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 
 import models.wowapi.character.Avatar;
 import play.db.jpa.Model;
 import play.libs.Codec;
import utils.StringUtils;
 
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
		name = name.substring(0,1).toUpperCase() + name.substring(1).toLowerCase();
 		return RaidMember.find("hash = ?", Codec.hexMD5(name)).first();
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
