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
 import dto.OperatoerDTO;
 
 public class Brugere {
 
 	public void handleAdminiBruger(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/adminibruger.jsp").forward(request, response);
 	}
 	public void handleCreateOpr(ServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 	}
 	public void handleShowOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>ID</td><td>Navn</td><td>Initialer</td><td>CPR</td><td>Password</td><td>Aktør</td></tr>";
 		int id,aktoer;
 		String cpr,navn,ini,pass;
 		List<OperatoerDTO> temp = funktionalitetsLaget.getOprDAO().getOperatoerList(); 
 		for (int i=0; i < temp.size(); i++) {
 			id = temp.get(i).getOprId();
 			navn = temp.get(i).getOprNavn();
 			ini = temp.get(i).getIni();
 			cpr = temp.get(i).getCpr();
 			pass = temp.get(i).getPassword();
 			aktoer = temp.get(i).getAktoer();
 			html += "<tr><td>"+id+"</td><td>"+navn+"</td><td>"+ini+"</td><td>"+cpr+"</td><td>"+pass+"</td><td>"+aktoer+"</td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/showopr.jsp").forward(request, response);
 	}
 
 	public void handleRemoveOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int oprId, oprAktoer;
 		String oprNavn;
 		List<OperatoerDTO> oprList = funktionalitetsLaget.getOprDAO().getOperatoerList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			oprId = oprList.get(i).getOprId();
 			oprNavn = oprList.get(i).getOprNavn();
 			oprAktoer = oprList.get(i).getAktoer();
 			if (oprAktoer != 1)
 				html += "<option value='"+oprId+"'>"+oprId+" - "+oprNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("oprList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/removeopr.jsp").forward(request, response);
 	}
 	public void handleUpdateOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int oprId;
 		String oprNavn;
 		List<OperatoerDTO> oprList = funktionalitetsLaget.getOprDAO().getOperatoerList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			oprId = oprList.get(i).getOprId();
 			oprNavn = oprList.get(i).getOprNavn();
 			html += "<option value='"+oprId+"'>"+oprId+" - "+oprNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("oprList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/updateopr.jsp").forward(request, response);
 	}
 	
 	public void handleCreateOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		String navn = request.getParameter("navn");
 		String init = request.getParameter("init");
 		String cpr = request.getParameter("cpr");
		cpr = cpr.replace("-", "");
 		String newPw = funktionalitetsLaget.generatePassword();
 		int aktoer = Integer.parseInt(request.getParameter("aktoer"));
 		int oprID = 0;
 		try {
 			oprID = funktionalitetsLaget.getOprDAO().getHighestOprID().getOprId()+1;
 		} catch (DALException e) {
 			e.printStackTrace();
 		}
 		request.setAttribute("autoid", oprID);
		String errorMsg = "";
		if (navn.length() == 0)
			errorMsg = "Navn er ikke defineret! ";
		if (init.length() == 0)
			errorMsg += "Initialer er ikke defineret! ";
		if (cpr.length() == 0)
			errorMsg += "CPR-nummeret er ikke defineret! ";
		if (cpr.length() != 10)
			errorMsg += "Et CPR nummer er 10 cifre!";
		if (errorMsg.length() == 0) {
			funktionalitetsLaget.getOprDAO().createOperatoer(new OperatoerDTO(id, navn, init, cpr, newPw, aktoer));
			request.setAttribute("password", "<br>Brugerens password er: " + newPw);
			request.setAttribute("succes", "Operatør oprettet!");
		}
		else
			request.setAttribute("fail", errorMsg);
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/createopr.jsp").forward(request, response);
 	}
 
 	public void handleRemoveOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		funktionalitetsLaget.getOprDAO().removeOperatoer(funktionalitetsLaget.getOprDAO().getOperatoer(id));
 		
 		int oprId, oprAktoer;
 		String oprNavn;
 		List<OperatoerDTO> oprList = funktionalitetsLaget.getOprDAO().getOperatoerList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			oprId = oprList.get(i).getOprId();
 			oprNavn = oprList.get(i).getOprNavn();
 			oprAktoer = oprList.get(i).getAktoer();
 			if (oprAktoer != 1)
 				html += "<option value='"+oprId+"'>"+oprId+" - "+oprNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("oprList", html);
 		request.setAttribute("succes", "Operatør slettet!");
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/removeopr.jsp").forward(request, response);
 	}
 	public void handleUpdateOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		String navn = request.getParameter("navn");
 		String init = request.getParameter("init");
 		String cpr = request.getParameter("cpr");
 		String newPw = funktionalitetsLaget.generatePassword();
 		int aktoer = Integer.parseInt(request.getParameter("aktoer"));
 		funktionalitetsLaget.getOprDAO().updateOperatoer(new OperatoerDTO(id, navn, init, cpr, newPw, aktoer));
 		
 		int oprId;
 		String oprNavn;
 		List<OperatoerDTO> oprList = funktionalitetsLaget.getOprDAO().getOperatoerList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			oprId = oprList.get(i).getOprId();
 			oprNavn = oprList.get(i).getOprNavn();
 			html += "<option value='"+oprId+"'>"+oprId+" - "+oprNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("oprList", html);
 		
 		request.setAttribute("password", "<br>Brugerens password er: " + newPw);
 		
 		request.setAttribute("succes", "Operatør opdateret!");
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/updateopr.jsp").forward(request, response);
 	}
 
 }
