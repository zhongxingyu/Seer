 package brutes.client.net;
 
 import brutes.client.ScenesContext;
 import brutes.client.game.Bonus;
 import brutes.client.game.Brute;
 import brutes.client.game.media.DataImage;
 import brutes.net.Network;
 import brutes.net.Protocol;
 import java.io.File;
 import java.io.IOException;
 import java.net.Socket;
 
 /**
  *
  * @author Karl
  */
 public class NetworkClient extends Network{
     public static final String CACHE_DIR = "cache/";
     
     public NetworkClient(Socket connection) throws IOException{
         super(connection);
     }
 
     public String sendLogin(String user, String password) throws IOException, ErrorResponseException, InvalidResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_LOGIN)
                 .writeString(user)
                 .writeString(password)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_LOGIN_SUCCESS:
                 return this.getReader().readString();
             case Protocol.ERROR_LOGIN_NOT_FOUND:
                 throw new ErrorResponseException(Protocol.ERROR_LOGIN_NOT_FOUND);
             case Protocol.ERROR_WRONG_PASSWORD:
                 throw new ErrorResponseException(Protocol.ERROR_WRONG_PASSWORD);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendLogout(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_LOGOUT)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_LOGOUT_SUCCESS:
                 break;
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     
     public void sendCreateBrute(String token, String name) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_CREATE_BRUTE)
                 .writeString(token)
                 .writeString(name)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_CREATE_BRUTE:
                 throw new ErrorResponseException(Protocol.ERROR_CREATE_BRUTE);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendUpdateBrute(String token, String name) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_UPDATE_BRUTE)
                 .writeString(token)
                 .writeString(name)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_UPDATE_BRUTE:
                 throw new ErrorResponseException(Protocol.ERROR_UPDATE_BRUTE);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public void sendDeleteBrute(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_DELETE_BRUTE)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_ACTION_SUCCESS:
                 break;
             case Protocol.ERROR_DELETE_BRUTE:
                 throw new ErrorResponseException(Protocol.ERROR_UPDATE_BRUTE);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     
     private int sendGetBruteId(byte getIdDiscriminant, String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(getIdDiscriminant)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_BRUTE:
                 return this.getReader().readLongInt();
             case Protocol.ERROR_BRUTE_NOT_FOUND:
                 throw new ErrorResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public int sendGetMyBruteId(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return this.sendGetBruteId(Protocol.D_GET_MY_BRUTE_ID, token);
     }
     public int sendGetChallengerBruteId(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return this.sendGetBruteId(Protocol.D_GET_CHALLENGER_BRUTE_ID, token);
     }
     
     private boolean sendFight(byte fightType, String token) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(fightType)
                 .writeString(token)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_FIGHT_RESULT:
                 return this.getReader().readBoolean();
             case Protocol.ERROR_FIGHT:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             case Protocol.ERROR_TOKEN:
                 throw new ErrorResponseException(Protocol.ERROR_TOKEN);
             default:
                 throw new InvalidResponseException();
         }
     }
     public boolean sendCheatFightWin(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_WIN, token);
     }
     public boolean sendCheatFightLoose(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_LOOSE, token);
     }
     public boolean sendCheatFightRandom(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_CHEAT_FIGHT_RANDOM, token);
     }
     public boolean sendDoFight(String token) throws IOException, InvalidResponseException, ErrorResponseException{
         return sendFight(Protocol.D_DO_FIGHT, token);
     }
     public Brute getDataBrute(int id) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_GET_BRUTE)
                 .writeLongInt(id)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_DATA_BRUTE:
                 int chId = this.getReader().readLongInt();
                 String name = this.getReader().readString();
                 short level = this.getReader().readShortInt();
                 short life = this.getReader().readShortInt();
                 short strength = this.getReader().readShortInt();
                 short speed = this.getReader().readShortInt();
                 int imageID = this.getReader().readLongInt();
                 int[] bonusesID = this.getReader().readLongIntArray();
                 this.close();
                 Bonus[] bonuses = new Bonus[(bonusesID.length < Brute.MAX_BONUSES)?bonusesID.length:Brute.MAX_BONUSES];
                 for(int i = 0; i < bonuses.length; i++){
                     try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                         bonuses[i] = connection.getDataBonus(bonusesID[i]);
                     }
                 }
                 DataImage image;
                 try{
                     image = this.getDataImageInCache(imageID);
                 } catch(UncachedException ex){
                     try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                         image = connection.getDataImage(imageID);
                     }
                 }
                 return new Brute(chId, name, level, life, strength, speed, image, bonuses);
             case Protocol.ERROR_BRUTE_NOT_FOUND:
                 throw new ErrorResponseException(Protocol.ERROR_BRUTE_NOT_FOUND);
             default:
                 throw new InvalidResponseException();
         }
     }
     public Bonus getDataBonus(int id) throws IOException, InvalidResponseException, ErrorResponseException{
         this.getWriter().writeDiscriminant(Protocol.D_GET_BONUS)
                 .writeLongInt(id)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_DATA_BONUS:
                 int boId = this.getReader().readLongInt();
                 String name = this.getReader().readString();
                 short level = this.getReader().readShortInt();
                 short life = this.getReader().readShortInt();
                 short strength = this.getReader().readShortInt();
                 short speed = this.getReader().readShortInt();
                 int imageID = this.getReader().readLongInt();
                 DataImage image;
                 try{
                     image = this.getDataImageInCache(imageID);
                 } catch(UncachedException ex){
                     try (NetworkClient connection = new NetworkClient(new Socket(ScenesContext.getInstance().getSession().getServer(), Protocol.CONNECTION_PORT))) {
                         image = connection.getDataImage(imageID);
                     }
                 }
                 return new Bonus(boId, name, level, life, strength, speed, image);
             case Protocol.ERROR_BONUS_NOT_FOUND:
                 throw new ErrorResponseException(Protocol.ERROR_BONUS_NOT_FOUND);
             default:
                 throw new InvalidResponseException();
         }
     }
     private DataImage getDataImageInCache(int id) throws UncachedException{
         String imageUri = new StringBuilder().append(NetworkClient.CACHE_DIR).append(id).append(".png").toString();
         File imageFile = new File(imageUri);
         if(imageFile.exists()){
             return new DataImage(imageUri);
         }
         else{
             throw new UncachedException();
         }
     }
     public DataImage getDataImage(int id) throws IOException, InvalidResponseException, ErrorResponseException{
         String imageUri = new StringBuilder().append(NetworkClient.CACHE_DIR).append(id).append(".png").toString();
         this.getWriter().writeDiscriminant(Protocol.D_GET_IMAGE)
                 .writeLongInt(id)
                 .send();
         this.getReader().readMessageSize();
         switch(this.getReader().readDiscriminant()){
             case Protocol.R_DATA_IMAGE:
                 if(this.getReader().readLongInt() != id){
                     throw new InvalidResponseException();
                 }
                 File cacheDir = new File(NetworkClient.CACHE_DIR);
                 if(!cacheDir.exists()){
                     cacheDir.mkdirs();
                 }
                 return new DataImage(this.getReader().readImage(imageUri));
             case Protocol.ERROR_IMAGE_NOT_FOUND:
                 throw new ErrorResponseException(Protocol.ERROR_BONUS_NOT_FOUND);
             default:
                 throw new InvalidResponseException();
         }
     }
     
     public static void clearCache(){
         File cacheDir = new File(NetworkClient.CACHE_DIR);
        String[] files = cacheDir.list();
        for (int i = 0; i < files.length; i++){
            new File(cacheDir, files[i]).delete();
         }
        cacheDir.delete();
     }
 }
