 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package packageAccess;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.SQLIntegrityConstraintViolationException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import packageException.AlbumException;
 import packageException.CommandeException;
 import packageException.ConnexionException;
 import packageException.GenreException;
 import packageException.InscriptionException;
 import packageException.ListAlbumException;
 import packageModel.Album;
 import packageModel.AlbumCart;
 import packageModel.Categorie;
 import packageModel.Utilisateur;
 
 /**
  * @author BAVAY Emilien - THILLAYE Joachim
  */
 public class AccessDB {
     private Connection connexion;
     
     public Utilisateur connexion(String login, String pass) throws ConnexionException 
     {
         Utilisateur user = new Utilisateur();
         user.setMail("");
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource)ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();        
             
             String requeteSQL = "SELECT motdepasse, prenom, IDUTILISATEUR FROM utilisateur WHERE mail = ?";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setString(1, login);
             ResultSet donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 String passTest = donnees.getString(1);
                 if (passTest.equals(pass) != true)
                 {
                     throw new ConnexionException("wrongPass");
                 }
                 else
                 {
                     user.setMail(login);  
                     user.setPrenom(donnees.getString(2));
                     user.setIdUtilisateur(donnees.getInt(3));
                 }
             }    
             
             
         }
         catch (SQLException e)
         {
             throw new ConnexionException("sqlConnexionError");
         }    
         catch (NamingException e)
         {
             throw new ConnexionException("sqlException");
         }    
         finally
         {
             try
             {
             connexion.close(); 
             }
             catch (SQLException e)
             {
             throw new ConnexionException("sqlConnexionError");
             }  
         }
         return user;
     }
     
     public ArrayList<Album> getLastAlbums(Integer nbAlbum) throws ListAlbumException
     {
         ArrayList<Album> arrayAlbum = new ArrayList<Album>();
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL = "SELECT Album.idAlbum, Album.titre, Album.image, Artiste.nom, Album.Prix, "
                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                 + "     THEN true "
                 + "     ELSE false "
                 + " END, "
                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                 + "     THEN (Album.Prix - (Album.Prix * Promotion.prcremise * 0.01))"
                 + " END"
                 + " FROM Album, Artiste_Album, Artiste, Promotion, Promotion_Artiste "
                 + " WHERE Artiste_Album.idAlbum = Album.idAlbum AND Artiste_Album.idArtiste = Artiste.idArtiste "
                 + " AND Promotion_Artiste.idPromotion = Promotion.idPromotion AND Promotion.datedeb <= current_date AND Promotion.datefin >= current_date"
                 + " ORDER BY album.idalbum DESC"   ;
             
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             if(nbAlbum != 0) {
                 prepStat.setMaxRows(nbAlbum);
             }
             
             ResultSet donnees = prepStat.executeQuery();   
             while (donnees.next())
             {
                 
                 Album album = new Album();
                 album.setIdAlbum(donnees.getInt(1));
                 album.setTitre(donnees.getString(2));
                 album.setImage(donnees.getString(3));
                 album.setArtiste(donnees.getString(4));  
                 album.setPrix(donnees.getDouble(5));
                 album.setPromo(donnees.getBoolean(6));
                 album.setPrixPromo(donnees.getDouble(7));
 
                 arrayAlbum.add(album);
             }
             
             if (arrayAlbum.isEmpty() == true) // Envoi erreur si aucune album
             {    
                 throw new ListAlbumException("emptyListAlbum");
             }            
             
         }
         catch (SQLException e)
         {
             throw new ListAlbumException("listAlbumException"+e.getMessage());
         }
         catch (NamingException e) 
         {
             throw new ListAlbumException("sqlException");
         }
         finally
         {
             try
             {
             connexion.close(); 
             }
             catch (SQLException e)
             {
                 throw new ListAlbumException("sqlConnexionError");
             }  
         }
         
         return arrayAlbum;
     }
 
     public Album getAlbum(Integer idAlbum) throws ListAlbumException
     {
         Album album = new Album();
         
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
                 String requeteSQL = "SELECT Album.idAlbum, Album.titre, Album.prix, Album.image, Artiste.nom, Label.Nom, Label.Image,"
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN true "
                                 + "     ELSE false "
                                 + " END, "
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN (Album.Prix - (Album.Prix * Promotion.prcremise * 0.01))"
                                 + " END,"
                                 + " Artiste.idartiste "
                                 + " FROM ALBUM,ARTISTE,PROMOTION,PROMOTION_ARTISTE,ARTISTE_ALBUM,LABEL"
                                 + " WHERE ALBUM.IDALBUM = ARTISTE_ALBUM.IDALBUM AND ARTISTE_ALBUM.IDARTISTE = ARTISTE.IDARTISTE "
                                 + " AND ALBUM.IDALBUM = ? AND LABEL.IDLABEL=ALBUM.IDLABEL"
                                 + " AND Promotion_Artiste.idPromotion = Promotion.idPromotion AND Promotion.datedeb <= current_date AND Promotion.datefin >= current_date";
                 
                 PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
                 prepStat.setInt(1, idAlbum);
                 ResultSet donnees = prepStat.executeQuery();
             
                 if(donnees.next())
                 {
                     album.setIdAlbum(donnees.getInt(1));
                     album.setTitre(donnees.getString(2));
                     album.setPrix(donnees.getDouble(3));
                     album.setImage(donnees.getString(4));
                     album.setArtiste(donnees.getString(5));
                     album.setLabel(donnees.getString(6));
                     album.setLabelImg(donnees.getString(7));
                     album.setPromo(donnees.getBoolean(8));
                     album.setPrixPromo(donnees.getDouble(9));
                     album.setIdArtiste(donnees.getInt(10));
                 }
                 else
                 {
                     throw new ListAlbumException("albumNotExist"); // si aucun album trouvé, c'est que l'id n'existe pas
                 }
         
                 return album; 
         }
         catch (SQLException e)
         {
             throw new ListAlbumException("getAlbumException");
         }
         catch (NamingException e) 
         {
             throw new ListAlbumException("sqlException");
         }
         finally
         {
             try
             {
             connexion.close(); 
             }
             catch (SQLException e)
             {
                 throw new ListAlbumException("sqlConnexionError");
             }  
         }
         
         
     }
     
     public void ajoutUtilisateur(Utilisateur util) throws InscriptionException
     {
         try
         {
             Context cont = new InitialContext();
             DataSource source = (DataSource)cont.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL = "INSERT INTO UTILISATEUR" + 
                                 "(NOM, PRENOM, ADR_RUE, ADR_NUMERO, ADR_BOITE, ADR_CODEPOSTAL, ADR_LOCALITE,MAIL,MOTDEPASSE,NUMTEL)"
                                 + "VALUES(?,?,?,?,?,?,?,?,?,?)";
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setString(1, util.getNom());
             prepStat.setString(2,util.getPrenom());
             prepStat.setString(3,util.getRue());
             prepStat.setInt(4,util.getNumero());
             prepStat.setString(5, util.getBoite());
             prepStat.setInt(6,util.getCodepostal() );  
             prepStat.setString(7, util.getLocalite());
             prepStat.setString(8,util.getMail() );
             prepStat.setString(9, util.getPassword());
             prepStat.setString(10, util.getNumTel());
             
             prepStat.executeUpdate();
             
             
             
         }        
         catch(SQLIntegrityConstraintViolationException ex)
         {
             throw new InscriptionException("errorMailUsed");
         }
         catch(SQLException ex)
         {
             throw new InscriptionException("addUserException");
         }
         catch(NamingException ex)
         {
             throw new InscriptionException("sqlException");
         }
         finally
         {
             try
             {
                 connexion.close(); 
             }
             catch (SQLException e)
             {
                 throw new InscriptionException("sqlConnexionError");
             }  
         }
         
     }
     
     public Album getArtistePromo() throws ListAlbumException
     {                
         try
         {
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
 
             //String requeteSQL = "select Album.idAlbum, Album.titre, Album.prix, Album.image, Artiste.nom, PROMOTION.PRCREMISE from ARTISTE, ALBUM, ARTISTE_ALBUM, LABEL, PROMOTION,PROMOTION_ARTISTE where PROMOTION.DATEFIN >= CURRENT_DATE and PROMOTION_ARTISTE.IDARTISTE = ARTISTE.IDARTISTE and PROMOTION_ARTISTE.IDPROMOTION = PROMOTION.IDPROMOTION and ARTISTE_ALBUM.IDARTISTE = ARTISTE.IDARTISTE and ARTISTE_ALBUM.IDALBUM = ALBUM.IDALBUM and ALBUM.IDLABEL = LABEL.IDLABEL";
             String requeteSQL = "SELECT Artiste.nom, Promotion.prcremise,Artiste.idArtiste"
                     + " FROM Artiste, Album, Artiste_Album, Label, Promotion, Promotion_Artiste"
                     + " WHERE Promotion.datedeb <= current_date"
                     + " AND Promotion.datefin >= current_date"
                     + " AND Promotion_Artiste.idArtiste = Artiste.idArtiste"
                     + " AND Promotion_Artiste.idPromotion = Promotion.idPromotion"
                     + " AND Artiste_Album.idArtiste = Artiste.idArtiste"
                     + " AND Artiste_Album.idAlbum = Album.idAlbum"
                     + " AND Album.idLabel = label.idLabel";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             ResultSet donnees = prepStat.executeQuery();
             
             if(donnees.next())
             {
                 
                 Album album = new Album();
                 album.setArtiste(donnees.getString(1));
                 album.setPrcRemise(donnees.getInt(2));
                 album.setIdArtiste(donnees.getInt(3));
                 
                 return album;
             }
             else
             {    
                 throw new ListAlbumException("noPromo");
             }            
             
         }
         catch (SQLException e)
         {
             throw new ListAlbumException("sqlException");
         }
         catch (NamingException e) 
         {
             throw new ListAlbumException("errorNaming");
         }
         finally
         {
             try
             {
             connexion.close(); 
             }
             catch (SQLException e)
             {
                 throw new ListAlbumException("sqlConnexionError");
             }  
         }        
     }
     
     
     public void ConfirmerCommande(Utilisateur util)throws CommandeException
     {
         try
         {
             for (Iterator iter = util.getHasmMapPanier().entrySet().iterator(); iter.hasNext();) //Vérification des quantités dans la hashmap
             {
                     Map.Entry data = (Map.Entry)iter.next();
                     AlbumCart album = (AlbumCart)data.getValue();
                     if(album.getQte()<1)
                     {
                         throw new CommandeException("errorQteCommande");
                     }
             }
             Context cont = new InitialContext();
             DataSource source = (DataSource)cont.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL = "INSERT INTO COMMANDE (IDUTILISATEUR,STATUT,DATE) VALUES(?,?,CURRENT DATE)";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setInt(1,util.getIdUtilisateur() );
             prepStat.setString(2, "0");
             prepStat.executeUpdate();
             
             requeteSQL = "SELECT IDCommande, IDUtilisateur from Commande where IDUTILISATEUR=? AND IDCommande=(SELECT MAX(IDCommande) from Commande)";
             prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setInt(1, util.getIdUtilisateur());   
             
             ResultSet donnees = prepStat.executeQuery();
             
             donnees.next();
             Integer idCommande = donnees.getInt(1);
             
             if(donnees.getInt(2)==util.getIdUtilisateur())
             {
                 
                 for (Iterator iter = util.getHasmMapPanier().entrySet().iterator(); iter.hasNext();) //Vérification des quantités dans la hashmap
                 {
                         Map.Entry data = (Map.Entry)iter.next();
                         AlbumCart album = (AlbumCart)data.getValue();
                         requeteSQL="INSERT INTO LIGNECOMMANDE (IDALBUM,IDCOMMANDE,QUANTITE,PRIX) VALUES (?,?,?,?)";
                         prepStat = connexion.prepareStatement(requeteSQL);
                         prepStat.setInt(1, album.getIdAlbum());
                         prepStat.setInt(2, idCommande);
                         prepStat.setInt(3, album.getQte());
                         if(album.getPromo())
                             prepStat.setDouble(4, album.getPrixPromo());
                         else
                             prepStat.setDouble(4, album.getPrix());
                         
                         prepStat.executeUpdate();
                 }
             }
             
         }
         catch(SQLException ex)
         {
             throw new CommandeException("errorSQL");
         }
         catch(NamingException ex)
         {
             throw new CommandeException("errorNaming");
         }
         finally
         {
             try
             {
             connexion.close(); 
             }
             catch (SQLException e)
             {
                 throw new CommandeException("sqlConnexionError");
             }  
         }       
     }
     
     
     public ArrayList<Categorie> getCategories() throws GenreException
     {
         try
         {
             ArrayList<Categorie> arrCat = new ArrayList<Categorie>();
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL= "SELECT * FROM GENRE";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);           
             ResultSet donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 Categorie newCat = new Categorie();
                 newCat.setIdCategorie(donnees.getInt(1));
                 newCat.setLibelle(donnees.getString(2));
                 arrCat.add(newCat);
             }
             if(arrCat.isEmpty())
             {
                 throw new GenreException("errorGenreNotExist");
             }
             return arrCat;
         }
         catch(SQLException ex)
         {
             throw new GenreException("errorSQL");
         }
         catch(NamingException ex)
         {
             throw new GenreException("errorNaming");
         }
         catch(GenreException ex)
         {
             throw new GenreException(ex.toString());
         }
         finally
         {
             try{connexion.close();}
             catch(SQLException ex)
             {
                 throw new GenreException("errorSQL");
             }
         }
     }
     
     public ArrayList<Album> getAlbumsCategorie(Integer idCat)throws GenreException
     {
         try
         {
             ArrayList<Album> arrAlb = new ArrayList<Album>();
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
             String requeteSQL ="SELECT Album.IDALBUM, Album.TITRE, Album.IMAGE, Artiste.NOM, Album.PRIX,"
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN true "
                                 + "     ELSE false "
                                 + " END, "
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN (Album.Prix - (Album.Prix * Promotion.prcremise * 0.01))"
                                 + " END"
                                 + " FROM ALBUM,ARTISTE,PROMOTION,PROMOTION_ARTISTE,ARTISTE_ALBUM,GENREALBUM "
                                 + " WHERE ALBUM.IDALBUM = ARTISTE_ALBUM.IDALBUM AND ARTISTE_ALBUM.IDARTISTE = ARTISTE.IDARTISTE "
                                 + " AND GENREALBUM.IDALBUM = ALBUM.IDALBUM AND GENREALBUM.IDGENRE = ?" 
                                 + " AND Promotion_Artiste.idPromotion = Promotion.idPromotion AND Promotion.datedeb <= current_date AND Promotion.datefin >= current_date";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setInt(1,idCat);
             ResultSet donnees;
             donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 Album album = new Album();
                 
                 album.setIdAlbum(donnees.getInt(1));
                 album.setTitre(donnees.getString(2));
                 album.setImage(donnees.getString(3));
                 album.setArtiste(donnees.getString(4));  
                 album.setPrix(donnees.getDouble(5));
                 album.setPromo(donnees.getBoolean(6));
                 album.setPrixPromo(donnees.getDouble(7));
                 
                 arrAlb.add(album);
             }
             
             return arrAlb;
         }
         
         catch(SQLException ex)
         {
             throw new GenreException(ex.toString());
         }
         catch(NamingException ex)
         {
             throw new GenreException("errorNaming");
         }
         finally
         {
             try{connexion.close();}
             catch(SQLException ex)
             {
                 throw new GenreException("errorSQL");
             }
         }
     }
     
     public String getDescAlbum(Integer idAlbum, String langue)throws AlbumException
     {
          try
         {
             ArrayList<Album> arrAlb = new ArrayList<Album>();
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
 
             String requeteSQL ="select TRADDESCRIPTIFALBUM " +
                                 " from TRADUCTIONALBUM " +
                                 " where IDALBUM = ? AND IDLANGUE=?";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setInt(1,idAlbum);
             prepStat.setString(2, langue);
             ResultSet donnees;
             donnees = prepStat.executeQuery();
             
             String desc="";
             
             if(donnees.next())
             {
                 desc = donnees.getString(1);
             }
             
             return desc;
         }
         
         catch(SQLException ex)
         {
             throw new AlbumException(ex.toString());
         }
         catch(NamingException ex)
         {
             throw new AlbumException("errorNaming");
         }
         finally
         {
             try{connexion.close();}
             catch(SQLException ex)
             {
                 throw new AlbumException("errorSQL");
             }
         }
     }
     
 
 
 
      public ArrayList<Album> getAlbumsArtiste(Integer idArtiste)throws AlbumException
     {
         try
         {
             ArrayList<Album> arrAlb = new ArrayList<Album>();
             Context ctx = new InitialContext();
             DataSource source = (DataSource) ctx.lookup("jdbc/MusicStore");
             connexion = source.getConnection();
             
            String requeteSQL ="SELECT Album.IDALBUM, Album.TITRE, Album.IMAGE, Artiste.NOM, Album.PRIX,"
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN true "
                                 + "     ELSE false "
                                 + " END, "
                                 + " CASE WHEN Promotion_Artiste.idArtiste = Artiste.idArtiste "
                                 + "     THEN (Album.Prix - (Album.Prix * Promotion.prcremise * 0.01))"
                                 + " END"
                                 + " FROM ALBUM,ARTISTE,PROMOTION,PROMOTION_ARTISTE,ARTISTE_ALBUM "
                                 + " WHERE ALBUM.IDALBUM = ARTISTE_ALBUM.IDALBUM AND ARTISTE_ALBUM.IDARTISTE = ARTISTE.IDARTISTE "
                                 + " AND ARTISTE_ALBUM.IDARTISTE =? " 
                                 + " AND Promotion_Artiste.idPromotion = Promotion.idPromotion AND Promotion.datedeb <= current_date AND Promotion.datefin >= current_date";
             
             PreparedStatement prepStat = connexion.prepareStatement(requeteSQL);
             prepStat.setInt(1,idArtiste);
             ResultSet donnees;
             donnees = prepStat.executeQuery();
             
             while (donnees.next())
             {
                 Album album = new Album();
                 
                 album.setIdAlbum(donnees.getInt(1));
                 album.setTitre(donnees.getString(2));
                 album.setImage(donnees.getString(3));
                 album.setArtiste(donnees.getString(4));  
                 album.setPrix(donnees.getDouble(5));
                album.setPromo(donnees.getBoolean(6));
                album.setPrixPromo(donnees.getDouble(7));
                 
                 arrAlb.add(album);
             }
             
             return arrAlb;
         }
         
         catch(SQLException ex)
         {
             throw new AlbumException(ex.toString());
         }
         catch(NamingException ex)
         {
             throw new AlbumException("errorNaming");
         }
         finally
         {
             try{connexion.close();
         }
             catch(SQLException ex)
             {
                 throw new AlbumException("errorSQL");
             }
         }
     }      
  }
