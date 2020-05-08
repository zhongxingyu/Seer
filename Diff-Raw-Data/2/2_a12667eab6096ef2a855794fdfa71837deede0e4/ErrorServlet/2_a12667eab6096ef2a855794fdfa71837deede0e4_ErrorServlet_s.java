 package com.pk.cwierkacz.controller;
 
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.pk.cwierkacz.exception.ProcessingException;
 import com.pk.cwierkacz.http.RequestBuilder;
 import com.pk.cwierkacz.http.Status;
 import com.pk.cwierkacz.http.response.Response;
 import com.pk.cwierkacz.http.response.ResponseImpl;
 import com.pk.cwierkacz.model.transformer.JsonTransformer;
 
 public class ErrorServlet extends HttpServlet
 {
     private static final long serialVersionUID = -227196281703855928L;
 
     @Override
     protected void doPost( HttpServletRequest req, HttpServletResponse resp ) throws javax.servlet.ServletException,
                                                                              IOException {
         doGet(req, resp);
     }
 
     @Override
     public void doGet( HttpServletRequest request, HttpServletResponse response ) throws IOException {
 
         Throwable throwable = (Throwable) request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
         String token = null;
         Cookie[] cookies = request.getCookies();
         for ( Cookie cookie : cookies ) {
             if ( cookie.getName().equals(RequestBuilder.TOKEN) ) {
                 token = cookie.getValue();
                 break;
             }
         }
         Response responseResult = ResponseImpl.create(Status.ERROR,
                                                      "Wystąpił wewnętrzy bład aplikacji." +
                                                               throwable.getMessage(),
                                                       Long.parseLong(token));
 
         String responseJson;
         try {
             responseJson = JsonTransformer.responseToJson(responseResult);
         }
         catch ( ProcessingException e ) {
             responseJson = "Fail to creat JSON";
         }
 
         if ( request.getParameter("callback") != null ) {
             responseJson = request.getParameter("callback") + "(" + responseJson + ");";
         }
 
         Cookie cookie = new Cookie("token", token);
         cookie.setMaxAge(60 * 60);
         response.addCookie(cookie);
         response.setCharacterEncoding("UTF-8");
         response.setContentType("text/html; charset=UTF-8");
         response.setStatus(HttpServletResponse.SC_OK);
         Writer out = response.getWriter();
         out.write(responseJson);
     }
 }
