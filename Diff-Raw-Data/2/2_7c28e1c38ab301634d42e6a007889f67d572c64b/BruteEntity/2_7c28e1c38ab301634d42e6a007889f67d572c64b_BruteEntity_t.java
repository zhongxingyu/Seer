 package brutes.server.db.entity;
 
 import brutes.server.db.DatasManager;
 import brutes.server.db.Entity;
 import brutes.server.game.Bonus;
 import brutes.server.game.Brute;
 import brutes.server.game.User;
 import brutes.server.ui;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  *
  * @author Thiktak
  */
 public class BruteEntity implements Entity {
 
     public static Brute create(ResultSet r) throws IOException, SQLException {
         Brute brute = new Brute(r.getInt("id"), r.getString("name"), r.getShort("level"), r.getShort("life"), r.getShort("strength"), r.getShort("speed"), r.getInt("image_id"));
         brute.setBonuses(BonusEntity.findAllByBrute(brute));
         return brute;
     }
 
     public static void save(Connection con, Brute brute) throws IOException, SQLException {
         PreparedStatement psql = con.prepareStatement("UPDATE Brutes SET name = ?, level = ?, life = ?, strength = ?, speed = ?, image_id = ? WHERE id = ?");
         psql.setString(1, brute.getName());
         psql.setInt(2, brute.getLevel());
         psql.setInt(3, brute.getLife());
         psql.setInt(4, brute.getStrength());
         psql.setInt(5, brute.getSpeed());
         psql.setInt(6, brute.getImageID());
         psql.setInt(7, brute.getId());
         psql.executeUpdate();
         
         psql = con.prepareStatement("DELETE FROM Shop WHERE brute_id = ?");
         psql.setInt(1, brute.getId());
         psql.executeUpdate();
         
         psql = con.prepareStatement("INSERT INTO Shop (brute_id, bonus_id) VALUES(?, ?)");
         for(int i = 0; i < Brute.MAX_BONUSES; i++){
             if(brute.getBonuses()[i] != Bonus.EMPTY_BONUS){
                 psql.setInt(1, brute.getId());
                 psql.setInt(2, brute.getBonuses()[i].getId());
                 psql.executeUpdate();
             }
         }
     }
 
     public static Brute insert(Connection con, Brute brute) throws IOException, SQLException {
         PreparedStatement psql = con.prepareStatement("INSERT INTO Brutes (name, level, life, strength, speed, image_id) VALUES(?, ?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
         psql.setString(1, brute.getName());
         psql.setInt(2, brute.getLevel());
         psql.setInt(3, brute.getLife());
         psql.setInt(4, brute.getStrength());
         psql.setInt(5, brute.getSpeed());
         psql.setInt(6, brute.getImageID());
         psql.executeUpdate();
         brute.setId(psql.getGeneratedKeys().getInt(1));
         return brute;
     }
 
     public static void delete(Connection con, Brute brute) throws IOException, SQLException {
         PreparedStatement psql = con.prepareStatement("DELETE FROM Brutes WHERE id = ?");
         psql.setInt(1, brute.getId());
         psql.executeUpdate();
     }
 
     public static Brute findById(int id) throws IOException, SQLException, NotFoundEntityException {
         PreparedStatement psql = DatasManager.prepare("SELECT * FROM Brutes WHERE id = ?");
         psql.setInt(1, id);
         ResultSet rs = psql.executeQuery();
         if (rs.next()) {
             return BruteEntity.create(rs);
         }
         throw new NotFoundEntityException(Brute.class);
     }
 
     public static Brute findByUser(User user) throws IOException, SQLException, NotFoundEntityException {
         PreparedStatement psql = DatasManager.prepare("SELECT b.* FROM Brutes b LEFT JOIN users u ON (u.brute_id = b.id) WHERE u.id = ? ORDER BY id DESC");
         psql.setInt(1, user.getId());
         ResultSet rs = psql.executeQuery();
         if (rs.next()) {
             return BruteEntity.create(rs);
         }
         throw new NotFoundEntityException(User.class);
     }
 
     public static Brute findRandomAnotherToBattleByUser(User user) throws IOException, SQLException, NotFoundEntityException {
         PreparedStatement psql = DatasManager.prepare("SELECT * FROM Brutes WHERE id <> ? ORDER BY RANDOM() LIMIT 1");
        psql.setInt(1, user.getBrute());
         ResultSet rs = psql.executeQuery();
         
         if (rs.next()) {
             return BruteEntity.create(rs);
         }
         throw new NotFoundEntityException(User.class);
     }
 
     public static Brute findOneRandomAnotherToBattleByUser(User user) throws IOException, SQLException, NotFoundEntityException {
         Brute object = findRandomAnotherToBattleByUser(user);
         if (object == null) {
             throw new NotFoundEntityException(User.class);
         }
         return object;
     }
 
     public static Brute findByName(String name) throws IOException, SQLException, NotFoundEntityException {
         PreparedStatement psql = DatasManager.prepare("SELECT * FROM Brutes WHERE name = ? ORDER BY id DESC");
         psql.setString(1, name);
         ResultSet rs = psql.executeQuery();
 
         if (rs.next()) {
             return BruteEntity.create(rs);
         }
         throw new NotFoundEntityException(Brute.class);
     }
 }
