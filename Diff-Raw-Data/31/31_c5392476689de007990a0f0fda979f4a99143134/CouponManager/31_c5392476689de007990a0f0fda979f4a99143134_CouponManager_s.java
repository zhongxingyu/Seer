 package net.lala.CouponCodes.api;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import net.lala.CouponCodes.CouponCodes;
 import net.lala.CouponCodes.api.coupon.Coupon;
 import net.lala.CouponCodes.api.coupon.EconomyCoupon;
 import net.lala.CouponCodes.api.coupon.ItemCoupon;
 import net.lala.CouponCodes.api.events.EventHandle;
 
 /**
  * CouponManager.java - Allows other plugins to interact with coupons
  * @author mike101102
  */
 public class CouponManager implements CouponAPI {
 	
 	private CouponCodes plugin;
 	private SQLAPI sql;
 	
 	public CouponManager(CouponCodes plugin) {
 		this.plugin = plugin;
 		this.sql = plugin.getSQLAPI();
 	}
 	
 	public CouponManager(SQLAPI sql) {
 		this.sql = sql;
 	}
 	
 	@Override
 	public boolean addCouponToDatabase(Coupon coupon) throws SQLException {		
 		Connection con = sql.getConnection();
 		if (coupon instanceof ItemCoupon) {
 			ItemCoupon c = (ItemCoupon) coupon;
 			PreparedStatement p = con.prepareStatement("INSERT INTO couponcodes VALUES(?, ?, ?, ?, ?, ?)");
 			p.setString(1, c.getName());
 			p.setString(2, c.getType());
 			p.setInt(3, c.getUseTimes());
			p.setObject(4, c.getUsedPlayers());
			p.setObject(5, c.getIDs());
 			p.setInt(6, 0);
 			con.setAutoCommit(false);
 			p.executeBatch();
 			con.setAutoCommit(true);
 		}
 		else if (coupon instanceof EconomyCoupon) {
 			EconomyCoupon c = (EconomyCoupon) coupon;
 			con.createStatement().executeUpdate("INSERT INTO couponcodes VALUES('"+c.getName()+"', '"+c.getType()+"', "+c.getUseTimes()+", "+c.getUsedPlayers()+", "+c.getMoney());
 			PreparedStatement p = con.prepareStatement("INSERT INTO couponcodes VALUES(?, ?, ?, ?, ?, ?)");
 			p.setString(1, c.getName());
 			p.setString(2, c.getType());
 			p.setInt(3, c.getUseTimes());
			p.setObject(4, c.getUsedPlayers());
			p.setObject(5, null);
 			p.setInt(6, c.getMoney());
 			con.setAutoCommit(false);
 			p.executeBatch();
 			con.setAutoCommit(true);
 		}
 		EventHandle.callCouponAddToDatabaseEvent(coupon);
 		return true;
 	}
 	
 	@Override
 	public boolean removeCouponFromDatabase(Coupon coupon) throws SQLException {
 		if (!couponExists(coupon)) return false;		
 		sql.query("DELETE FROM couponcodes WHERE name='"+coupon.getName()+"'");
 		EventHandle.callCouponRemoveFromDatabaseEvent(coupon);
 		return true;
 	}
 	
 	@Override
 	public boolean couponExists(Coupon coupon) throws SQLException {
 		return getCoupons().contains(coupon.getName());
 	}
 	
 	@Override
 	public boolean couponExists(String name) throws SQLException {
 		return getCoupons().contains(name);
 	}
 	
 	@Override
 	public ArrayList<String> getCoupons() throws SQLException {
 		ResultSet rs = sql.query("SELECT name FROM couponcodes");
 		if (rs.equals(null)) return null;
 		ArrayList<String> c = new ArrayList<String>();
 		while (rs.next())
 			c.add(rs.getString(1));
 		return c;
 	}
 	
 	@Override
 	public Coupon getCoupon(String coupon) throws SQLException {
 		int usetimes = sql.query("SELECT usetimes FROM couponcodes WHERE name='"+coupon+"'").getInt(1);
 		ArrayList<String> usedplayers = plugin.convertStringToArrayList(sql.query("SELECT usedplayers FROM couponcodes WHERE name='"+coupon+"'").getString(1));
 		
 		if (sql.query("SELECT ctype FROM couponcodes WHERE name='"+coupon+"'").getString(1).equalsIgnoreCase("Item")) {
 			return createNewItemCoupon(coupon, usetimes, plugin.convertStringToHash(sql.query("SELECT ids FROM couponcodes WHERE name='"+coupon+"'").getString(1)), usedplayers);
 		}
 		
 		else if (sql.query("SELECT ctype FROM couponcodes WHERE name='"+coupon+"'").getString(1).equalsIgnoreCase("Economy")) {
 			return createNewEconomyCoupon(coupon, usetimes, usedplayers, sql.query("SELECT money FROM couponcodes WHERE name='"+coupon+"'").getInt(1));
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public Coupon createNewItemCoupon(String name, int usetimes, HashMap<Integer, Integer> ids, ArrayList<String> usedplayers) {
 		return new ItemCoupon(name, usetimes, usedplayers, ids);
 	}
 	
 	@Override
 	public Coupon createNewEconomyCoupon(String name, int usetimes, ArrayList<String> usedplayers, int money) {
 		return new EconomyCoupon(name, usetimes, usedplayers, money);
 	}
 }
