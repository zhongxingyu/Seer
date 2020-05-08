 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mosedb.dao;
 
 import com.mosedb.models.Format;
 import com.mosedb.models.Format.MediaFormat;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  *
  * @author llybeck
  */
 public class MovieFormatDao extends AbstractDao {
 
     public MovieFormatDao() throws SQLException {
         super();
     }
 
     public boolean addMovieFormat(int movieid, int formatid) throws SQLException {
         String sql = "insert into mosedb.movieformat (movieid,formatid) values (?,?)";
         return executeUpdate(sql, movieid, formatid);
     }
 
     public boolean removeMovieFormat(int movieid, int formatid) throws SQLException {
         String sql = "delete from mosedb.movieformat where movieid=? and formatid=?";
         return executeUpdate(sql, movieid, formatid);
     }
 
     public boolean removeMovieFormat(int movieid) throws SQLException {
         String sql = "delete from mosedb.movieformat where movieid=?";
         return executeUpdate(sql, movieid);
     }
 
     public List<Integer> getFormatIds(int movieid) throws SQLException {
         String sql = "select formatid from mosedb.movieformat where movieid=?";
         ResultSet result = executeQuery(sql, movieid);
         List<Integer> list = new ArrayList<Integer>();
         while (result.next()) {
             list.add(result.getInt("formatid"));
         }
         result.close();
         return list;
     }
 
     public Set<Integer> getMovieIdsByMediaFormat(String mediaformat) throws SQLException {
         String sql = "select movieid from mosedb.format f, mosedb.movieformat mf "
                 + "where f.formatid=mf.formatid and f.mediaformat=cast(? as mosedb.mediaformat)";
         ResultSet result = executeQuery(sql, mediaformat);
         Set<Integer> set = new HashSet<Integer>();
         while (result.next()) {
             int id = result.getInt("movieid");
             if (!set.contains(id)) {
                 set.add(id);
             }
         }
         result.close();
         return set;
     }
 
     public List<Format> getFormats(int movieid) throws SQLException {
         String sql = "select f.mediaformat, f.filetype, f.resox, f.resoy "
                 + "from mosedb.movieformat mf, mosedb.format f "
                 + "where mf.movieid=? and f.formatid=mf.formatid";
         ResultSet result = executeQuery(sql, movieid);
         List<Format> list = new ArrayList<Format>();
         while (result.next()) {
             Format format;
             MediaFormat mediaFormat = Format.getMediaFormat(result.getString("mediaformat"));
             if (mediaFormat == MediaFormat.dc) {
                 String filetype = result.getString("filetype");
                 int resox = result.getInt("resox");
                 int resoy = result.getInt("resoy");
                 if (resox == 0 || resoy == 0) {
                     format = new Format(mediaFormat, filetype);
                 } else {
                     format = new Format(mediaFormat, filetype, resox, resoy);
                 }
             } else {
                format = new Format(mediaFormat);
             }
             list.add(format);
         }
         return list;
     }
 
     public static void main(String[] args) throws SQLException {
         MovieFormatDao movieFormatDao = new MovieFormatDao();
         System.out.println(movieFormatDao.getFormats(4));
         movieFormatDao.closeConnection();
     }
 }
