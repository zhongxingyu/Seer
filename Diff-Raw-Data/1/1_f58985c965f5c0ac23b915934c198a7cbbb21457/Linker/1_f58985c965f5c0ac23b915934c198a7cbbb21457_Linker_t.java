<<<<<<< HEAD:src/java/ua/edu/sumdu/lab3/controller/Linker.java
 package ua.edu.sumdu.lab3.controller;
 
 import ua.edu.sumdu.lab3.model.*;
 import ua.edu.sumdu.lab3.model.exceptions.GetDataException;
 import ua.edu.sumdu.lab3.model.exceptions.EditDataException;
 import ua.edu.sumdu.lab3.CollectionWrapper;
 
 import javax.servlet.http.*;
 import javax.servlet.*;
 
 import java.io.*;
 import java.text.*;
 import java.util.*;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.text.ParseException;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.BasicConfigurator;
 
 public class Linker extends HttpServlet {
     DateFormat df = null;
     private OperableDAO dao = null;
     private boolean datachanged = true;
     private Logger log = null;
             
     public void init() throws ServletException {
         super.init();
 
         log = Logger.getLogger(Linker.class);
         df = new SimpleDateFormat("dd.MM.yy");
         Locale.setDefault(Locale.ENGLISH);
         ServletContext context = getServletContext();
         Map props = new HashMap();
 
         dao = DaoFactory.getDao("oracle");
     }
     
     /**
      * Realization of the doGet method.
      */ 
     protected void doGet(HttpServletRequest request,
             HttpServletResponse response)
             throws ServletException, IOException {
                 
         String spath = request.getServletPath();
         DateFormat df = new SimpleDateFormat("yyyy");
         PrintWriter out = response.getWriter();
         
         try {
             if ("".equals(spath)) {
                 request.setAttribute("random",dao.getRandomAlbum());
                 request.setAttribute("latest",dao.getLatestAlbums(5));
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/index.jsp").forward(request,response);
             
             } else if ("/artist".equals(spath)) {
                 String id = request.getParameter("id");
                 int aid = Integer.parseInt(id);
                 request.setAttribute("artist",dao.getArtist(aid));
                 Artist artForGenres = new Artist();
                 artForGenres.setId(aid);
                 request.setAttribute("genres",new CollectionWrapper(dao.getGenres(artForGenres)));
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showartist.jsp").forward(request,response);
             
             } else if("/artist/all".equals(spath)) {
                 String page = request.getParameter("page");
                 String country = request.getParameter("country");
                 int first = 0;
                 int last = 0;
                 if(page == null) {
                     last = 10;
                 } else {
                     last = Integer.parseInt(page)*10;
                     first = last - 9;
                 }
                 int number = 0;
                 Object artists;
                 if(country != null) {
                     number = (int)Math.ceil((double)dao.getArtistNumber(country) / 10);
                     artists = dao.getArtists(country,first,last);
                 } else {
                     number = (int)Math.ceil((double)dao.getArtistNumber() / 10);
                     artists = dao.getArtists(first,last);
                 }
                 
                 request.setAttribute("number",new Integer(number));
                 request.setAttribute("artists",artists);
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showartists.jsp").forward(request,response);
             
             } else if("/album".equals(spath)) {
                 String id = request.getParameter("id");
                 request.setAttribute("album",dao.getAlbum(Integer.parseInt(id)));
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showalbum.jsp").forward(request,response);
                         
             } else if("/album/all".equals(spath)) {
                 String page = request.getParameter("page");
                 int first = 0;
                 int last = 0;
                 if(page == null) {
                     last = 10;
                 } else {
                     last = Integer.parseInt(page)*10;
                     first = last - 9;
                 }
                 String year = request.getParameter("year");
                 String genre = request.getParameter("genre");
                 int number = 0;
                 Object albums = null;
                 if(year != null){
                     Date date = df.parse(year);
                     number = (int)Math.ceil((double)dao.getAlbumNumber(date) / 10);
                     albums = dao.getAlbums(date,first,last);
                 } else if(genre != null) {
                     number = (int)Math.ceil((double)dao.getAlbumNumber(genre) / 10);
                     albums = dao.getAlbums(genre,first,last);
                 } else {
                     number = (int)Math.ceil((double)dao.getAlbumNumber() / 10);
                     albums = dao.getAlbums(first,last);
                 }
                 request.setAttribute("number",new Integer(number));
                 request.setAttribute("albums",albums);
                 
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showalbums.jsp").forward(request,response);
                         
             } else if("/label".equals(spath)) {
                 String id = request.getParameter("id");
                 int lid = Integer.parseInt(id);
                 request.setAttribute("label",dao.getLabel(lid));
                 request.setAttribute("path",new CollectionWrapper(dao.getLabelPath(lid)));
                 request.setAttribute("children",new CollectionWrapper(dao.getLabels(lid)));
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showlabel.jsp").forward(request,response);
 
             } else if("/label/all".equals(spath)) {
                 String page = request.getParameter("page");
                 int first = 0;
                 int last = 0;
                 if(page == null) {
                     last = 10;
                 } else {
                     last = Integer.parseInt(page)*10;
                     first = last - 9;
                 }
                 int number = (int)Math.ceil((double)dao.getAlbumNumber() / 10);
                 Object labels = null;
                 labels = dao.getLabels(first,last);
                 request.setAttribute("number",new Integer(number));
                 request.setAttribute("labels",labels);
                 
                 getServletConfig().getServletContext().getRequestDispatcher(
                     "/pages/showlabels.jsp").forward(request,response);
 
             } else if("/remove".equals(spath)) {
                 String what = request.getParameter("obj");
                 String id = request.getParameter("id");
                 if("album".equals(what)) {
                     dao.deleteAlbum(Integer.parseInt(id));
                 } else if("artist".equals(what)) {
                     dao.deleteArtist(Integer.parseInt(id));
                 } else if("label".equals(what)) {
                     dao.deleteLabel(Integer.parseInt(id));
                 }
                 
                 response.sendRedirect(request.getHeader("Referer"));
                 
             } else if("/date/all".equals(spath)) {
                 request.setAttribute("dates",dao.getDates());
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/showdates.jsp").forward(request,response);
             
             } else if ("/addalbum".equals(spath)) {
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/addalbum.jsp").forward(request,response);
             
             } else if ("/editlabel".equals(spath)) {
                 int id = Integer.parseInt(
                         request.getParameter("id"));
                         
                 Label label = (Label)dao.getLabel(id);
                 request.setAttribute("label", label);
                     
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/editlabel.jsp").forward(request,response);
             
             } else if ("/editartist".equals(spath)) {
                 
                 int id = Integer.parseInt(
                         request.getParameter("id"));
 
                 Artist artist = (Artist)dao.getArtist(id);
                 request.setAttribute("artist", artist);
                     
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/editartist.jsp").forward(request,response);
             
             } else if ("/editalbum".equals(spath)) {
                 
                 int id = Integer.parseInt(
                         request.getParameter("id"));
 
                 Album album = (Album)dao.getAlbum(id);
                 request.setAttribute("album", album);
                 
                 getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/editalbum.jsp").forward(request,response);
             } else if ("/check".equals(spath)) {
                 
                 /* check?object=artist&artist=SomeName
                  * check?object=label&label=SomeName
                  * */
                 String artist = request.getParameter("artist");
                 if (artist != null) {
                     out.print(dao.findArtist(artist));
                 }
                 String label = request.getParameter("label");
                 if (label != null) {
                     out.print(dao.findArtist(label));
                 }
             } else if ("/showartists".equals(spath)) {
                 List artists = dao.getArtists(1, 9999);
                 Iterator itr = artists.iterator();
                 while(itr.hasNext()){
                     out.print(((Artist)itr.next()).getName() + "\n");
                 }
             } else if ("/showlabels".equals(spath)) {
                 List labels = dao.getLabels();
                 Iterator itr = labels.iterator();
                 while(itr.hasNext()){
                     out.print(((Label)itr.next()).getName() + "\n");
                 }
             } else if ("/search".equals(spath)) {
                 String by = (String)request.getParameter("by");
                 if (by == null) {
                     getServletConfig().getServletContext().getRequestDispatcher(
                         "/pages/search.jsp").forward(request,response);
                 } else {
                     String query = request.getParameter("search");
                     List albums = null;
                     
                     if ("name".equals(by)) 
                         albums = dao.getAlbumsByName(query, 1, 9999);
                     
                     
                     if ("artist".equals(by)) {
                         int aid = dao.findArtist(query);
                         Artist artist = dao.getArtist(aid);
                         if (artist != null)
                             albums = dao.getAlbums(artist, 1, 9999);
                     }
                     
                     if ("label".equals(by)) {
                         int lid = dao.findLabel(query);
                         Label label = dao.getLabel(lid);
                         if (label != null)
                             albums = dao.getAlbums(label, 1, 9999);
                     }
                     
                     if ("date".equals(by)) 
                         albums = dao.getAlbums(df.parse(query), 1, 9999);
                     
                     
                     if ("genre".equals(by)) 
                         albums = dao.getAlbums(query, 1, 9999);
                     
                     
                     request.setAttribute("albums", albums);
                     request.setAttribute("number",new Integer(5));
                     
                     getServletConfig().getServletContext().getRequestDispatcher(
                             "/pages/showalbums.jsp").forward(request,response);
                 }
             } 
         } catch (GetDataException e) {
             throw new ServletException(e);
         } catch (EditDataException e) {
             throw new ServletException(e);
         } catch (ParseException e) {
             throw new ServletException(e);
         }
     }
     
     /**
      * Realization of the doPot method.
      */
     protected void doPost(HttpServletRequest request,
             HttpServletResponse response) 
             throws ServletException, IOException {
         PrintWriter out = response.getWriter();       
         String spath = request.getServletPath();
         
         try {
             if ("/addalbum".equals(spath)){
                 String name = request.getParameter("name");
                 String type = request.getParameter("type");
                 Date release = df.parse(request.getParameter("date"));
                 String genre = request.getParameter("genre");
                 String cover = request.getParameter("cover");
                 String review = request.getParameter("review");
                 String artistName = request.getParameter("artistslist");
                 String labelName = request.getParameter("labelslist");
                 int artist = dao.findArtist(artistName); 
                 int label = dao.findLabel(labelName);
                 
                 Album album = new Album();
                 album.setName(name);
                 album.setType(type);
                 album.setRelease(release);
                 album.setGenre(genre);
                 album.setCover(cover);
                 album.setReview(review);
                 album.setArtistName(artistName);
                 album.setLabelName(labelName);
                 album.setArtist(artist);
                 album.setLabel(label);
 
                 dao.addAlbum(album);
                 response.sendRedirect("/discs");
             } else 
             
             if ("/addartist".equals(spath)) {
                 String name = request.getParameter("name");
                 String country = request.getParameter("country");
                 String info = request.getParameter("info");
                             
                 if (artistInList(name)){
                     out.print("Artist already exists");  
                 } else {
                     Artist artist = new Artist();
                     artist.setName(name);
                     artist.setInfo(info);
                     artist.setCountry(country);
                     
                     dao.addArtist(artist);
                     out.print("1");   
                 }         
             } else if ("/addlabel".equals(spath)) {
                 String name = request.getParameter("name");
                 String info = request.getParameter("info");
                 String logo = request.getParameter("logo");
                 String major = request.getParameter("major");
                 
                 int majorId = 0;
                 if (!("none".equals(major))){
                     majorId = dao.findLabel(major);
                 } 
                 
                 Label label = new Label();
                 label.setName(name);
                 label.setInfo(info);
                 label.setLogo(logo);
                 label.setMajorName(major);
                 label.setMajor(majorId);
 
                 dao.addLabel(label);
                 out.print("1");   
             
             } else 
             
             if ("/editlabel".equals(spath)) {
                 int id = Integer.parseInt(
                         request.getParameter("labelid"));   
                 String name = request.getParameter("labelname");
                 String info = request.getParameter("labelinfo");
                 String logo = request.getParameter("labellogo");
                 String major = request.getParameter("labelslist");
                
                 int majorId = 0;
                 if (!("none".equals(major))){
                     majorId = dao.findLabel(major);
                 } 
                 Label label = new Label();
                 label.setId(id);
                 label.setMajor(majorId);
                 label.setName(name);
                 label.setInfo(info);
                 label.setLogo(logo);
                 label.setMajorName(major);
                 
                 dao.editLabel(label);
                 
                 response.sendRedirect("label?id=" + id);
             }
             
             if ("/editartist".equals(spath)) {
                 int id = Integer.parseInt(
                         request.getParameter("artistid"));
                 
                 String name = request.getParameter("artistname");
                 String country = request.getParameter("artistcountry");
                 String info = request.getParameter("artistinfo");
                 if (artistInList(name)){
                     request.setAttribute("error", "Artist already exists");
                     getServletConfig().getServletContext().
                             getRequestDispatcher("/pages/error.jsp").
                             forward(request,response);
                 } else {
                     Artist artist = new Artist();
                     artist.setId(id);
                     artist.setName(name);
                     artist.setCountry(country);
                     artist.setInfo(info);
                     
                     dao.editArtist(artist);
                     
                     response.sendRedirect("artist?id=" + id);
                 }
             }
             
             if ("/editalbum".equals(spath)){
                 int id = Integer.parseInt(
                         request.getParameter("id"));
                 String name = request.getParameter("name");
                 String type = request.getParameter("type");
                 Date release = df.parse(request.getParameter("date"));
                 String genre = request.getParameter("genre");
                 String cover = request.getParameter("cover");
                 String review = request.getParameter("review");
                 String artistName = request.getParameter("artistslist");
                 String labelName = request.getParameter("labelslist");
                 int artist = dao.findArtist(artistName);
                 int label = dao.findLabel(labelName);
                 
                 Album album = new Album();
                 album.setId(id);
                 album.setName(name);
                 album.setType(type);
                 album.setRelease(release);
                 album.setGenre(genre);
                 album.setCover(cover);
                 album.setReview(review);
                 album.setArtistName(artistName);
                 album.setLabelName(labelName);
                 album.setArtist(artist);
                 album.setLabel(label);
                 
                 out.print(album);
                 dao.editAlbum(album);
                 
                 response.sendRedirect("album?id=" + id);
             }
             
         } catch (Exception e) {
             throw new ServletException(e);
         }
     }
 
     /**
      * Returns true if artist already in storage.
      * @param name artist's name.
      * @return true if artist already in storage.
      */ 
     private boolean artistInList(String name) throws GetDataException {
         List artists = dao.getArtists(1, dao.getArtistNumber());
         Iterator itr = artists.iterator();
         while (itr.hasNext()){
             String existName = ((Artist)itr.next()).getName();
             if (name.equals(existName)) return true;
         }
         return false;
     }
 }
