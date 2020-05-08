 package web_interface;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import businessLogic_layer.Functionality;
 import dao_interfaces.DALException;
 import dto.RaavareDTO;
 import dto.ReceptDTO;
 import dto.ReceptKompDTO;
 
 public class Recepter {
 	
 	public void handleAdminiRecept(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/recept/adminirecept.jsp").forward(request, response);
 	}
 	public void handleCreateRecept(ServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int raavareid;
 		String raavareNavn,html = "";
 		List<RaavareDTO> raavare = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		for (int i=0; i < raavare.size(); i++) {
 			raavareid = raavare.get(i).getRaavareId();
 			raavareNavn = raavare.get(i).getRaavareNavn();
 			html += "<option value='"+raavareid+"'>"+raavareNavn+"</option>";
 		}
 		request.setAttribute("raavarer", html);
 		request.getRequestDispatcher("/WEB-INF/admin/recept/createrecept.jsp").forward(request, response);
 	}
 	public void handleShowRecept(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>ID</td><td>Navn</td><td>Receptkomponenter</td></tr>";
 		int receptid, raavareid;
 		double nomNetto, tolerance;
 		String navn, receptkomp;
 		List<ReceptDTO> recept = funktionalitetsLaget.getReceptDAO().getReceptList(); 
 		for (int i=0; i < recept.size(); i++) {
 			receptid = recept.get(i).getReceptId();
 			navn = recept.get(i).getReceptNavn();
 			List<ReceptKompDTO> receptkomplist = funktionalitetsLaget.getReceptKompDAO().getReceptKompList(receptid);
 			receptkomp = "<table border=1>";
 			for (int x=0; x < receptkomplist.size(); x++) {
 				if (x == 0) {
 					receptkomp +=  "<tr><td>RåvareID</td><td>Nominel Nettovægt</td><td>Tolerance</td></tr>";
 				}
 				raavareid = receptkomplist.get(x).getRaavareId();
 				nomNetto = receptkomplist.get(x).getNomNetto();
 				tolerance = receptkomplist.get(x).getTolerance();
 				receptkomp += "<tr><td>"+raavareid+"</td><td>"+nomNetto+"</td><td>"+tolerance+"</td></tr>";
 			}
 			receptkomp += "</table>";
 			html += "<tr><td>"+receptid+"</td><td>"+navn+"</td><td>"+receptkomp+"</td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/recept/showrecept.jsp").forward(request, response);
 	}
 
 	public void handleCreateReceptSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int receptid = Integer.parseInt(request.getParameter("receptid"));
 		String receptnavn = request.getParameter("receptnavn");
 		funktionalitetsLaget.getReceptDAO().createRecept(new ReceptDTO(receptid, receptnavn));
 		int x = 0;
 		int raavareid;
		double nomNetto, tolerance;
 		while (x < 5) {
 			if (request.getParameter("raavareid"+(x+1)).length() > 0 && request.getParameter("nomnetto"+(x+1)).length() > 0 && request.getParameter("tolerance"+(x+1)).length() > 0) {
 				raavareid = Integer.parseInt(request.getParameter("raavareid"+(x+1)));
				nomNetto = Double.parseDouble(request.getParameter("nomnetto"+(x+1)));
				tolerance = Double.parseDouble(request.getParameter("tolerance"+(x+1)));
				funktionalitetsLaget.getReceptKompDAO().createReceptKomp(new ReceptKompDTO(receptid,raavareid,nomNetto,tolerance));
 			}
 			x++;
 		}
 		int raavareid2;
 		String raavareNavn,html = "";
 		List<RaavareDTO> raavare = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		for (int i=0; i < raavare.size(); i++) {
 			raavareid2 = raavare.get(i).getRaavareId();
 			raavareNavn = raavare.get(i).getRaavareNavn();
 			html += "<option value='"+raavareid2+"'>"+raavareNavn+"</option>";
 		}
 		request.setAttribute("raavarer", html);
		request.setAttribute("succes", "Recept oprettet!");
 		request.getRequestDispatcher("/WEB-INF/admin/recept/createrecept.jsp").forward(request, response);
 	}
 }
