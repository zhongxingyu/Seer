 package fr.ybonnel.codestory.path;
 
 
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import fr.ybonnel.codestory.database.DatabaseManager;
 import fr.ybonnel.codestory.database.modele.Enonce;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Enumeration;
 
 public class InsertEnonceHandler extends AbstractPathHandler {
 
     private Gson gson = new GsonBuilder().create();
 
     @Override
     public PathResponse getResponse(HttpServletRequest request, String...params) {
         int id = Integer.parseInt(params[0]);
         Enonce enonce = contructEnonce(id, request);
         DatabaseManager.INSTANCE.getEnonceDao().insert(enonce);
         return new PathResponse(HttpServletResponse.SC_CREATED, gson.toJson(enonce));
     }
 
     public Enonce contructEnonce(int id, HttpServletRequest request) {
         Enumeration<?> parameters = request.getParameterNames();
         String titre = (String) parameters.nextElement();
        return new Enonce(id, "Enonce " + id, titre + "=" + request.getParameter(titre));
 
     }
 }
