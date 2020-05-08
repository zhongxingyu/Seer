 package com.appspot.AccentNijkerk.controller;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.appspot.AccentNijkerk.model.Competentie;
 import com.appspot.AccentNijkerk.model.CompetentieLijst;
 import com.appspot.AccentNijkerk.model.CompetentieLijstDao;
 import com.appspot.AccentNijkerk.model.CompetentieLijstDaoOfyImpl;
 import com.appspot.AccentNijkerk.model.Stage;
 import com.appspot.AccentNijkerk.model.StageDao;
 import com.appspot.AccentNijkerk.model.StageDaoOfyImpl;
 
 public class StageToevoegenServlet extends HttpServlet {
 	private static final long serialVersionUID = -5060943264223383201L;
	private static final Logger log = Logger.getLogger(CompetentieLijstToevoegenServlet.class.getName());
 	
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
 		
 		RequestDispatcher rd = req.getRequestDispatcher("index.jsp");
 		Long leerlingId = Long.parseLong(req.getParameter("leerling"));
 		Long bedrijfId = Long.parseLong(req.getParameter("bedrijf"));
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
 		Date datumvan = null;
 		Date datumtot = null;
 		String dsv = req.getParameter("datumv");
 		String dst = req.getParameter("datumt");
 		
 		if(dsv != null && dsv!=null){
 		try {
 			datumvan = sdf.parse(dsv);
 			datumtot = sdf.parse(dst);
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		}
 		log.info(dsv + "" + dst);
 		
 		
 
 		if(leerlingId == null || bedrijfId == null || datumvan ==null || datumtot == null) {
 			//Aanmaken mislukt
 			req.setAttribute("msg", "<div class='nosucces'>Niet alle velden zijn ingevuld</div>");
 			rd = req.getRequestDispatcher("stage-toevoegen.jsp");
 		} else {
 				
 			//Nieuwe Stage aanmaken
 			Stage s = new Stage(bedrijfId, datumvan, datumtot, bedrijfId);
 			StageDao StageDao = new StageDaoOfyImpl();
 			StageDao.voegStageToe(s);
 				
 			//evt. dingen
 
 				
 			log.info("Stage voor " + leerlingId + " succesvol aangemaakt");
 		}
 
 		rd.forward(req, resp);
 	}
 }
