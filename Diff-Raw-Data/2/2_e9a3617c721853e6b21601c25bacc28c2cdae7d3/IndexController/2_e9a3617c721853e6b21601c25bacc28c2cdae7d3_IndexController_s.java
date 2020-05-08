 package com.jin.tpdb.controller;
 
 import com.jin.tpdb.persistence.DAO;
 import com.jin.tpdb.entities.News;
 import com.jin.tpdb.entities.Album;
 import java.io.*;
 import java.util.List;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.persistence.*;
 import javax.persistence.criteria.*;
 
 public class IndexController extends HttpServlet {
 	
 	public void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 		
 		List<News> newsList = DAO.getList(News.class);
 		List<Album> albumsList = DAO.getList(Album.class);		
 		List<Album> featuredAlbumsList = DAO.getList(Album.class);
		Long lol = DAO.countAlbumComments(0);
 		
 		
 		for(Album fAlbum : featuredAlbumsList) {
 		
 		String coverPath = "images/albums/cover_" + 
 							fAlbum.getArtist().getName().replace(" ", "_").toLowerCase() +
 							"_" + 
 							fAlbum.getName().replace(" ", "_").toLowerCase() +
 							".jpg";
 							
 		fAlbum.setCover(coverPath);
 		}
 		
 		request.setAttribute("lol", lol);
 		request.setAttribute("news", newsList);
 		request.setAttribute("albums", albumsList);		
 		request.setAttribute("featuredAlbums", featuredAlbumsList);
 		
 		request.setCharacterEncoding("UTF-8");
 		RequestDispatcher jsp = request.getRequestDispatcher("index.jsp");
 		jsp.forward(request, response);
 		
 	}	
 	
 	@Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 	
 	@Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException {
         processRequest(request, response);
     }
 }
 
