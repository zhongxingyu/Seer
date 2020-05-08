 package brutes.server.net.response;
 
 import brutes.net.NetworkWriter;
 import brutes.net.Protocol;
 import brutes.server.db.DatasManager;
 import brutes.server.db.entity.BonusEntity;
 import brutes.server.db.entity.BruteEntity;
 import brutes.server.db.entity.NotFoundEntityException;
 import brutes.server.game.Brute;
 import brutes.server.game.Fight;
 import brutes.server.game.User;
 import brutes.server.net.NetworkResponseException;
 import brutes.server.ui;
 import java.io.IOException;
 import java.sql.SQLException;
 
 /**
  *
  * @author Thiktak
  */
 public class BruteResponse extends Response {
 
     public BruteResponse(NetworkWriter writer) {
         super(writer);
     }
 
     public void readDataBrute(int id) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         Brute brute = BruteEntity.findOneById(id);
 
         brute.setBonuses(BonusEntity.findAllByBrute(brute));
 
         this.getWriter().writeDiscriminant(Protocol.R_DATA_BRUTE)
                 .writeLongInt(id)
                 .writeString(brute.getName())
                 .writeShortInt((short) brute.getLevel())
                 .writeShortInt((short) brute.getLife())
                 .writeShortInt((short) brute.getStrength())
                 .writeShortInt((short) brute.getSpeed())
                 .writeLongInt(brute.getImageID())
                 .writeLongIntArray(brute.getBonusesIDs())
                 .send();
     }
 
     public void readGetChallengerBruteId(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         User user = BruteResponse.checkTokenAndReturnUser(token);
         Fight fight = FightResponse.getFightWithChallenger(user);
 
         this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                 .writeLongInt(fight.getBrute2().getId())
                 .send();
     }
 
     public void readGetMyBruteId(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         User user = BruteResponse.checkTokenAndReturnUser(token);
         Brute brute = BruteEntity.findOneByUser(user);
 
         this.getWriter().writeDiscriminant(Protocol.R_BRUTE)
                 .writeLongInt(brute.getId())
                 .send();
     }
 
     public void readCreateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         User user = BruteResponse.checkTokenAndReturnUser(token);
 
        // Brute already exists !
        /*if( BruteEntity.findByUser(user) != null ) {
         throw new NetworkResponseException(Protocol.ERROR_CREATE_BRUTE);
         }*/
 
         if (name.isEmpty()) {
             throw new NetworkResponseException(Protocol.ERROR_CREATE_BRUTE); // @TODO Protocol.ERROR_INPUT_DATAS
         }
 
         /* @TODO
          * if( BruteEntity.findByName(name) != null ) {
          *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
          * }
          */
 
         short level = 1;
         short strength = (short) ui.random(3, 10);
         short speed = (short) ui.random(3, 10);
         short life = (short) (ui.random(10, 20) + strength / 3);
        int imageID = ui.random(1, 13);
 
         Brute brute = new Brute(0, name, level, life, strength, speed, imageID);
         brute.setUserId(user.getId());
        //brute.setImageID(imageID);
         DatasManager.insert(brute);
 
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     public void readUpdateBrute(String token, String name) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         User user = BruteResponse.checkTokenAndReturnUser(token);
 
         if (name.isEmpty()) {
             throw new NetworkResponseException(Protocol.ERROR_UPDATE_BRUTE); // @TODO Protocol.ERROR_INPUT_DATAS
         }
 
         /* @TODO define it !
          * if( BruteEntity.findByName(name) != null ) {
          *    throw new NetworkResponseException(Protocol.ERROR_BRUTE_ALREADY_USED);
          * }
          */
 
         Brute brute = BruteEntity.findOneByUser(user);
         brute.setName(name);
         DatasManager.save(brute);
 
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 
     public void readDeleteBrute(String token) throws IOException, SQLException, NetworkResponseException, NotFoundEntityException {
         User user = BruteResponse.checkTokenAndReturnUser(token);
         Brute brute = BruteEntity.findOneByUser(user);
 
         DatasManager.delete(brute);
 
         this.getWriter().writeDiscriminant(Protocol.R_ACTION_SUCCESS)
                 .send();
     }
 }
