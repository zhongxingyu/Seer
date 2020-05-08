 package org.ldv.sio.getap.app.service.impl;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.ldv.sio.getap.app.Classe;
 import org.ldv.sio.getap.app.Discipline;
 import org.ldv.sio.getap.app.User;
 import org.ldv.sio.getap.app.service.dao.IFUserDAO;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Service;
 
 @Service("userDAO")
 public class UserDAOJdbc implements IFUserDAO {
 
   private static JdbcTemplate jdbcTemplate;
 
   @Autowired
   public void setDataSource(DataSource dataSource) {
     jdbcTemplate = new JdbcTemplate(dataSource);
   }
 
   // classe pour passage d'une ligne d'une table à un objet
   private static final class UserMapper implements RowMapper<User> {
     public User mapRow(ResultSet rs, int rowNum) throws SQLException {
       User user = new User();
       user.setId(rs.getLong("id"));
       user.setPrenom(rs.getString("prenom"));
       user.setNom(rs.getString("nom"));
       user.setRole(rs.getString("role"));
       user.setHash(rs.getString("hash"));
       try {
         user.setDureeTotal(rs.getInt("dureeTotal"));
       } catch (SQLException ex) {
 
       }
 
       DisciplineDAOJdbc disciplineDao = new DisciplineDAOJdbc();
       ClasseDAOJdbc classeDao = new ClasseDAOJdbc();
       Classe classe = classeDao.getClasseById(rs.getInt("idClasse"));
       Discipline dis = disciplineDao.getDisciplineById(rs
           .getInt("idDiscipline"));
       user.setDiscipline(dis);
       user.setClasse(classe);
       user.setLogin(rs.getString("login"));
       user.setPass(rs.getString("mdp"));
       user.setMail(rs.getString("mail"));
       return user;
     }
   }
 
   public boolean isInteger(String s) {
     try {
       Integer.parseInt(s);
       return true;
     } catch (NumberFormatException nfe) {
       return false;
     }
   }
 
   public String generate(int length) {
     String chars = "abcdefghijkmnopqrstuvwxyz1234567890";
     String pass = "";
     for (int x = 0; x < length; x++) {
       int i = (int) Math.floor(Math.random() * chars.length());
       pass += chars.charAt(i);
     }
     return pass;
   }
 
   public static String getEncodedPassword(String key) {
     byte[] uniqueKey = key.getBytes();
     byte[] hash = null;
     try {
       hash = MessageDigest.getInstance("MD5").digest(uniqueKey);
     } catch (NoSuchAlgorithmException e) {
       e.printStackTrace();
     }
     StringBuffer hashString = new StringBuffer();
     for (int i = 0; i < hash.length; ++i) {
       String hex = Integer.toHexString(hash[i]);
       if (hex.length() == 1) {
         hashString.append('0');
         hashString.append(hex.charAt(hex.length() - 1));
       } else {
         hashString.append(hex.substring(hex.length() - 2));
       }
     }
     return hashString.toString();
   }
 
   public List<User> getAllProf() {
     return jdbcTemplate.query(
         "select * from user where hash <> '' AND role like 'prof%'",
         new UserMapper());
 
   }
 
   public List<User> getAllProfInter() {
     return jdbcTemplate.query(
         "select * from user where hash <> '' AND role = 'prof-intervenant'",
         new UserMapper());
   }
 
   public List<User> getAllProfPrinc() {
     return jdbcTemplate.query(
         "select * from user where hash <> '' AND role = 'prof-principal'",
         new UserMapper());
   }
 
   public List<User> getAllEleve() {
     return jdbcTemplate.query(
         "select * from user where hash <> '' AND role = 'eleve'",
         new UserMapper());
   }
 
   public List<User> getAllEleveByClasse() {
     return jdbcTemplate
         .query(
             "select user.*, sum(dctap.dureeAP) as dureeTotal "
                 + "from user "
                 + "left join dctap on dctap.idEleve = user.id and (dctap.Etat = 1 or dctap.Etat = 32) "
                 + "left join classe on classe.id = user.idClasse where user.role = 'eleve'"
                 + "group by user.id having hash <> '' order by dureeTotal DESC, user.nom",
             new UserMapper());
   }
 
   public List<User> getAllEleveByPP(User user) {
     Long id = user.getId();
     return jdbcTemplate
         .query(
             "select * from user where hash <> '' AND idClasse in (select idClasse from prof_principal where idUser ="
                 + id + ")", new UserMapper());
   }
 
   public User getUserById(Long id) {
     User user;
     try {
       user = jdbcTemplate.queryForObject("select * from user where id = ?",
           new Object[] { id }, new UserMapper());
 
     } catch (EmptyResultDataAccessException e) {
       user = null;
     }
     return user;
   }
 
   public User addUser(User user) {
     String nom = user.getNom();
     String prenom = user.getPrenom();
     String login;
     if ((user.getPrenom().charAt(0) + user.getNom()).length() >= 6) {
       login = (user.getPrenom().charAt(0) + user.getNom()).toLowerCase();
     } else if ((user.getPrenom().charAt(0) + user.getNom()).length() == 5) {
       login = (user.getPrenom().charAt(0) + "_" + user.getNom()).toLowerCase();
     } else if ((user.getPrenom() + user.getNom()).length() < 6) {
       login = (user.getPrenom() + "_" + user.getNom()).toLowerCase();
 
     } else {
       login = (user.getPrenom() + user.getNom()).toLowerCase();
     }
     if (login.length() > 10) {
       login = login.substring(0, 10);
     }
     if (login.contains('é' + "") || login.contains('è' + "")) {
       login = login.replace('é', 'e');
       login = login.replace('è', 'e');
     }
     if (login.contains('à' + "") || login.contains('â' + "")) {
       login = login.replace('à', 'a');
       login = login.replace('â', 'a');
     }
     if (login.contains("'" + "")) {
       login = login.replace("'", "");
     }
     String mail = user.getMail();
     try {
       User user2 = jdbcTemplate.queryForObject(
           "select * from user where login like " + "'" + login + "%'"
               + " and nom = ? and prenom = ? order by id desc limit 0,1",
           new Object[] { nom, prenom }, new UserMapper());
 
       if (user2 != null) {
         int max = 2;
         String log = user2.getLogin();
         String sNb = log.charAt(log.length() - 1) + "";
 
         if (isInteger(sNb)) {
           int nb = Integer.parseInt(sNb);
           max = nb + 1;
         }
         String sMax = String.valueOf(max);
         login += sMax;
       }
     } catch (EmptyResultDataAccessException e) {
 
     }
     String mdp = generate(5);
     String hash = getEncodedPassword(mdp);
     String role = user.getRole();
     int classe = 0;
     if (role.equals("eleve"))
       classe = user.getClasse().getId();
 
     User user3;
 
     if (role.equals("prof-principal")) {
       jdbcTemplate
           .update(
               "insert into user(nom,prenom,login,mdp,hash, role,idClasse, mail) values(?,?,?,?,?,?,?,?)",
               new Object[] { nom, prenom, login, mdp, hash, role, null, mail });
       user3 = jdbcTemplate
           .queryForObject(
               "select * from user where login = ? and mdp = ? order by id desc limit 0,1",
               new Object[] { login, mdp }, new UserMapper());
       Long idUser = user3.getId();
 
       for (int i = 0; i < user.getLesClasses().length; i++) {
         jdbcTemplate.update(
             "insert into prof_principal(idUser,idClasse) values(?,?)",
             new Object[] { idUser, user.getLesClasses()[i] });
       }
     } else if (!role.equals("eleve")) {
       jdbcTemplate
           .update(
               "insert into user(nom,prenom,login,mdp, hash, role,idClasse, mail) values(?,?,?,?,?,?,?,?)",
               new Object[] { nom, prenom, login, mdp, hash, role, null, mail });
     } else {
       jdbcTemplate
           .update(
               "insert into user(nom,prenom,login,mdp, hash, role,idClasse, mail) values(?,?,?,?,?,?,?,?)",
               new Object[] { nom, prenom, login, mdp, hash, role, classe, mail });
     }
 
     if (role.startsWith("prof")) {
       user3 = jdbcTemplate
           .queryForObject(
               "select * from user where login = ? and mdp = ? order by id desc limit 0,1",
               new Object[] { login, mdp }, new UserMapper());
       jdbcTemplate.update("update user set idDiscipline = ? where id = ?",
           new Object[] { user.getDiscipline().getId(), user3.getId() });
     }
 
     User userInfo = new User();
     userInfo.setLogin(login);
     userInfo.setPass(mdp);
     return userInfo;
 
   }
 
   /**
    * rétablit le mot de passe initial de l'utilisateur (écrase l'ancien dans le
    * cas où il aurait été modifié par l'utilisateur)
    */
   public void updatePass(User user) {
     Long id = user.getId();
     String pass = user.getPass();
     String hash = getEncodedPassword(pass);
 
     jdbcTemplate.update("update user set hash = ? where id = ?", new Object[] {
         hash, id });
 
   }
 
   public void updateUser(User user) {
     Long id = user.getId();
     String nom = user.getNom();
     String prenom = user.getPrenom();
     String role = user.getRole();
     int idClasse = 0;
     if (role.equals("eleve")) {
       idClasse = user.getClasse().getId();
     }
     String login = user.getLogin();
     String pass = user.getPass();
     String hash = getEncodedPassword(pass);
     String mail = user.getMail();
     int dis = 0;
     if (role.startsWith("prof")) {
       dis = user.getDiscipline().getId();
     }
 
     jdbcTemplate.update("delete from prof_principal where idUser = ?",
         new Object[] { id });
 
     if (role.equals("prof-principal")) {
       for (int i = 0; i < user.getLesClasses().length; i++) {
         jdbcTemplate.update(
             "insert into prof_principal(idUser,idClasse) values(?,?)",
             new Object[] { id, user.getLesClasses()[i] });
       }
     }
     if (role.equals("eleve")) {
       jdbcTemplate
           .update(
               "update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?,hash = ?, mail = ?, idDiscipline= ? where id = ?",
               new Object[] { nom, prenom, role, idClasse, login, hash, mail,
                   null, id });
     } else if (role.equals("admin")) {
       jdbcTemplate
           .update(
               "update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?, hash = ?, mail = ?, idDiscipline= ? where id = ?",
               new Object[] { nom, prenom, role, null, login, hash, mail, null,
                   id });
     } else {
       jdbcTemplate
           .update(
               "update user set nom = ?, prenom = ?, role = ?, idClasse = ?, login = ?, hash = ?, mail = ?, idDiscipline= ? where id = ?",
               new Object[] { nom, prenom, role, null, login, hash, mail, dis,
                   id });
     }
 
   }
 
   public void updateProfil(User user) {
     String login = user.getLogin();
     String hash = user.getHash();
     String mail = user.getMail();
     Long id = user.getId();
     jdbcTemplate.update(
         "update user set login = ?, hash = ?, mail = ? where id = ?",
         new Object[] { login, hash, mail, id });
   }
 
   public void deleteUser(User user) {
     Long id = user.getId();
 
     if (user.getRole().equals("prof-principal")) {
       jdbcTemplate.update("delete from prof_principal where idUser = ? ",
           new Object[] { id });
     }
 
    jdbcTemplate.update("update dctap set idProf = null where idProf = ?",
         new Object[] { id });
 
     jdbcTemplate.update("delete from user where id = ?", new Object[] { id });
 
   }
 
 }
