 import java.io.IOException;
 import java.sql.SQLException;
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import businessLogic_layer.Functionality;
 import dao_interfaces.DALException;
 import db_connection.Connector;
 import dto.OperatoerDTO;
 import dto.ProduktBatchDTO;
 import dto.RaavareBatchDTO;
 import dto.RaavareDTO;
 import dto.ReceptDTO;
 import dto.ReceptKompDTO;
 
 public class Servlet extends HttpServlet {
 
 	private static final long serialVersionUID = 1L;
 
 	public Servlet(){
 		super();
 	}
 
 	public void init(ServletConfig config) throws ServletException{
 		try { new Connector(); } 
 		catch (InstantiationException e) { e.printStackTrace(); }
 		catch (IllegalAccessException e) { e.printStackTrace(); }
 		catch (ClassNotFoundException e) { e.printStackTrace(); }
 		catch (SQLException e) { e.printStackTrace(); }
 	}
 
 	public void destroy(){
 
 	}
 
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 	}
 
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
 		Functionality funktionalitetsLaget = new Functionality();
 		
 		// Lav session hvis den ikke findes
 		
 		if (request.getParameter("login") != null && request.getParameter("login").equals("Log ind")) 
 			handleLogIn(request, response, funktionalitetsLaget);
 		if (request.getParameter("logoff") != null && request.getParameter("logoff").equals("Log af"))
 			handleLogOff(request, response);
 		if (request.getParameter("hovedmenu") != null && request.getParameter("hovedmenu").equals("Tilbage til hovedmenu"))
 			handleGoToHovedmenu(request, response);
 		if (request.getParameter("adminibruger") != null && request.getParameter("adminibruger").equals("Administrere brugere"))
 			handleAdminiBruger(request, response, funktionalitetsLaget);
 		if (request.getParameter("createopr") != null && request.getParameter("createopr").equals("Opret bruger")) {
 			int oprID = 0;
 			try {
 				oprID = funktionalitetsLaget.getOprDAO().getHighestOprID().getOprId()+1;
 			} catch (DALException e) {
 				e.printStackTrace();
 			}
 			request.setAttribute("autoid", oprID);
 			handleCreateOpr(request, response, funktionalitetsLaget);
 		}
 		if (request.getParameter("showopr") != null && request.getParameter("showopr").equals("Vis brugere")){
 			try{
 				handleShowOpr(request, response, funktionalitetsLaget);
 			} catch (DALException e){
 				e.getMessage();
 			}
 		}
 			
 		if (request.getParameter("removeopr") != null && request.getParameter("removeopr").equals("Slet bruger"))
 			try {
 				handleRemoveOpr(request, response, funktionalitetsLaget);
 			} catch (DALException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 		if (request.getParameter("updateopr") != null && request.getParameter("updateopr").equals("Opdatér bruger"))
 			try {
 				handleUpdateOpr(request, response, funktionalitetsLaget);
 			} catch (DALException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 		if (request.getParameter("adminivare") != null && request.getParameter("adminivare").equals("Administrere råvarer"))
 			handleAdminiVare(request, response, funktionalitetsLaget);
 		if (request.getParameter("createvare") != null && request.getParameter("createvare").equals("Opret råvare"))
 			handleCreateVare(request, response, funktionalitetsLaget);
 		if (request.getParameter("showvare") != null && request.getParameter("showvare").equals("Vis råvare")){
 			try{
 				handleShowVare(request, response, funktionalitetsLaget);
 			} catch (DALException e){
 				e.getMessage();
 			}
 		}
 		if (request.getParameter("updatevare") != null && request.getParameter("updatevare").equals("Opdater råvare"))
 			try {
 				handleUpdateVare(request, response, funktionalitetsLaget);
 			} catch (DALException e2) {
 				// TODO Auto-generated catch block
 				e2.printStackTrace();
 			}
 		if (request.getParameter("adminirecept") != null && request.getParameter("adminirecept").equals("Administrere recepter"))
 			handleAdminiRecept(request, response, funktionalitetsLaget);
 		if (request.getParameter("createrecept") != null && request.getParameter("createrecept").equals("Opret recept"))
 			try {
 				handleCreateRecept(request, response, funktionalitetsLaget);
 			} catch (DALException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		if (request.getParameter("showrecept") != null && request.getParameter("showrecept").equals("Vis recepter")){
 			try{
 				handleShowRecept(request, response, funktionalitetsLaget);
 			} catch (DALException e){
 				e.getMessage();
 			}
 		}
 		if (request.getParameter("adminiraavarebatch") != null && request.getParameter("adminiraavarebatch").equals("Administrere råvarebatches"))
 			handleAdminiRaavareBatch(request, response, funktionalitetsLaget);
 		if (request.getParameter("createraavarebatch") != null && request.getParameter("createraavarebatch").equals("Opret råvarebatch"))
 			try {
 				handleCreateRaavareBatch(request, response, funktionalitetsLaget);
 			} catch (DALException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		if (request.getParameter("showraavarebatch") != null && request.getParameter("showraavarebatch").equals("Vis råvarebatch")){
 			try{
 				handleShowRaavareBatch(request, response, funktionalitetsLaget);
 			} catch (DALException e){
 				e.getMessage();
 			}
 		}
 		if (request.getParameter("adminiproduktbatch") != null && request.getParameter("adminiproduktbatch").equals("Administrere produktbatches"))
 			handleAdminiProduktBatch(request, response, funktionalitetsLaget);
 		if (request.getParameter("createproduktbatch") != null && request.getParameter("createproduktbatch").equals("Opret produktbatch"))
 			try {
 				handleCreateProduktBatch(request, response, funktionalitetsLaget);
 			} catch (DALException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 		if (request.getParameter("showproduktbatch") != null && request.getParameter("showproduktbatch").equals("Vis produktbatch")){
 			try{
 				handleShowProduktBatch(request, response, funktionalitetsLaget);
 			} catch (DALException e){
 				e.getMessage();
 			}
 		}
 		if (request.getParameter("createopr_submit") != null && request.getParameter("createopr_submit").equals("Opret Operatør")){
 			try {
 				handleCreateOprSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Operatør oprettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/bruger/createopr.jsp").forward(request, response);
 		}
 		if (request.getParameter("removeopr_submit") != null && request.getParameter("removeopr_submit").equals("Slet Operatør")){
 			try {
 				handleRemoveOprSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Operatør slettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/bruger/removeopr.jsp").forward(request, response);	
 		}
 		if (request.getParameter("updateopr_submit") != null && request.getParameter("updateopr_submit").equals("Opdater operatør")){
 			try {
 				handleUpdateOprSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Operatør opdateret!");
 			request.getRequestDispatcher("/WEB-INF/admin/bruger/updateopr.jsp").forward(request, response);
 		}
 		if (request.getParameter("createraavare_submit") != null && request.getParameter("createraavare_submit").equals("Opret råvare")){
 			try {
 				handleCreateVareSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Råvare oprettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/raavare/createvare.jsp").forward(request, response);
 		}
 		if (request.getParameter("updateraavare_submit") != null && request.getParameter("updateraavare_submit").equals("Opdater råvare")){
 			try {
 				handleUpdateVareSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Råvare opdateret!");
 			request.getRequestDispatcher("/WEB-INF/admin/raavare/updatevare.jsp").forward(request, response);
 		}
 		if (request.getParameter("createrecept_submit") != null && request.getParameter("createrecept_submit").equals("Opret recept")){
 			try {
 				handleCreateReceptSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Recept oprettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/recept/createrecept.jsp").forward(request, response);
 		}
 		if (request.getParameter("createraavarebatch_submit") != null && request.getParameter("createraavarebatch_submit").equals("Opret råvarebatch")){
 			try {
 				handleCreateRaavareBatchSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Råvarebatch oprettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/raavarebatch/createraavarebatch.jsp").forward(request, response);
 		}
 		if (request.getParameter("createproduktbatch_submit") != null && request.getParameter("createproduktbatch_submit").equals("Opret produktbatch")){
 			try {
 				handleCreateProduktBatchSubmit(request, response, funktionalitetsLaget);
 			} catch (DALException e) {
 				e.getMessage();
 			}
 			request.setAttribute("succes", "Produktbatch oprettet!");
 			request.getRequestDispatcher("/WEB-INF/admin/produktbatch/createproduktbatch.jsp").forward(request, response);
 		}
 	}
 
 	private void handleLogIn(HttpServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException{
 		HttpSession session = request.getSession(true);
 	
 		
 		int id = Integer.parseInt(request.getParameter("Id"));
 		String pw = request.getParameter("Password");
 		try {
 			if (funktionalitetsLaget.testId(id)) { // checker om ID findes i DB.
 				if(funktionalitetsLaget.testPassword(id, pw)){
 					session.setAttribute("operatoerNavn", funktionalitetsLaget.getOprDAO().getOperatoer(id).getOprNavn());
 					session.setAttribute("operatoerAktoer", funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer());
 					if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 1)
 						request.getRequestDispatcher("/WEB-INF/admin.jsp").forward(request, response);
 					else if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 2)
 						request.getRequestDispatcher("/WEB-INF/pharmacist.jsp").forward(request, response);
 					else if(funktionalitetsLaget.getOprDAO().getOperatoer(id).getAktoer() == 3)
 						request.getRequestDispatcher("/WEB-INF/supervisor.jsp").forward(request, response);
 					else
 						request.getRequestDispatcher("/WEB-INF/operator.jsp").forward(request, response);
 					
 
 				}
 			}
 			else {
				request.setAttribute("error", "Unknown user, please try again");
 				request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 			}
 		} catch (DALException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void handleLogOff(ServletRequest request, ServletResponse response) throws ServletException, IOException{
 		request.getRequestDispatcher("/WEB-INF/login.jsp").forward(request, response);
 	}
 	private void handleGoToHovedmenu(HttpServletRequest request, ServletResponse response) throws ServletException, IOException{
 		HttpSession session = request.getSession(true);
 		int aktoer = Integer.parseInt(String.valueOf(session.getAttribute("operatoerAktoer")));
 		if (aktoer == 1)
 			request.getRequestDispatcher("/WEB-INF/admin.jsp").forward(request, response);
 		else if (aktoer == 2)
 			request.getRequestDispatcher("/WEB-INF/pharmacist.jsp").forward(request, response);
 		else if (aktoer == 3)
 			request.getRequestDispatcher("/WEB-INF/supervisor.jsp").forward(request, response);
 		else
 			request.getRequestDispatcher("/WEB-INF/operator.jsp").forward(request, response);
 		
 	}
 	private void handleAdminiBruger(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/adminibruger.jsp").forward(request, response);
 	}
 	private void handleCreateOpr(ServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/bruger/createopr.jsp").forward(request, response);
 	}
 	private void handleShowOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 
 	private void handleRemoveOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	private void handleUpdateOpr(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	private void handleAdminiVare(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/raavare/adminivare.jsp").forward(request, response);
 	}
 	private void handleCreateVare(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/raavare/createvare.jsp").forward(request, response);
 	}
 	private void handleShowVare(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>ID</td><td>Navn</td><td>Leverandør</td></tr>";
 		int id;
 		String navn,leverandoer;
 		List<RaavareDTO> temp = funktionalitetsLaget.getRaavareDAO().getRaavareList(); 
 		for (int i=0; i < temp.size(); i++) {
 			id = temp.get(i).getRaavareId();
 			navn = temp.get(i).getRaavareNavn();
 			leverandoer = temp.get(i).getLeverandoer();
 			html += "<tr><td>"+id+"</td><td>"+navn+"</td><td>"+leverandoer+"</td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/raavare/showvare.jsp").forward(request, response);
 	}
 	private void handleAdminiRaavareBatch(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/raavarebatch/adminiraavarebatch.jsp").forward(request, response);
 	}
 	private void handleCreateRaavareBatch(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int vareId;
 		String vareNavn;
 		List<RaavareDTO> oprList = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		String html = "<select name='raavareid'>";
 		for (int i=0; i < oprList.size(); i++) {
 			vareId = oprList.get(i).getRaavareId();
 			vareNavn = oprList.get(i).getRaavareNavn();
 			html += "<option value='"+vareId+"'>"+vareId+" - "+vareNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("vareList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/raavarebatch/createraavarebatch.jsp").forward(request, response);
 	}
 	private void handleShowRaavareBatch(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>Råvarebatch ID</td><td>Råvare ID</td><td>Mængde</td></tr>";
 		int raavarebatchid, raavareid;
 		double maengde;
 		List<RaavareBatchDTO> raavarebatch = funktionalitetsLaget.getRaavareBatchDAO().getRaavareBatchList(); 
 		for (int i=0; i < raavarebatch.size(); i++) {
 			raavarebatchid = raavarebatch.get(i).getRbId();
 			raavareid = raavarebatch.get(i).getRaavareId();
 			maengde = raavarebatch.get(i).getMaengde();
 			html += "<tr><td>"+raavarebatchid+"</td><td>"+raavareid+"</td><td>"+maengde+"</td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/raavarebatch/showraavarebatch.jsp").forward(request, response);
 	}
 	
 	private void handleAdminiRecept(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/recept/adminirecept.jsp").forward(request, response);
 	}
 	private void handleCreateRecept(ServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	private void handleShowRecept(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	private void handleUpdateVare(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int vareId;
 		String vareNavn;
 		List<RaavareDTO> oprList = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			vareId = oprList.get(i).getRaavareId();
 			vareNavn = oprList.get(i).getRaavareNavn();
 			html += "<option value='"+vareId+"'>"+vareId+" - "+vareNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("vareList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/raavare/updatevare.jsp").forward(request, response);
 	}
 	private void handleAdminiProduktBatch(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException {
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/adminiproduktbatch.jsp").forward(request, response);
 	}
 	private void handleCreateProduktBatch(ServletRequest request, ServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int receptId;
 		String receptNavn;
 		List<ReceptDTO> receptList = funktionalitetsLaget.getReceptDAO().getReceptList();
 		String html = "<select name='receptid'>";
 		for (int i=0; i < receptList.size(); i++) {
 			receptId = receptList.get(i).getReceptId();
 			receptNavn = receptList.get(i).getReceptNavn();
 			html += "<option value='"+receptId+"'>"+receptId+" - "+receptNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("receptList", html);
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/createproduktbatch.jsp").forward(request, response);
 	}
 	private void handleShowProduktBatch(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		String html = "<table border=1>";
 		html += "<tr><td>Produktbatch ID</td><td>Recept ID</td><td>Dato</td><td>Status</td></tr>";
 		int produktbatchid, receptid, status;
 		String dato;
 		List<ProduktBatchDTO> produktbatch = funktionalitetsLaget.getProduktBatchDAO().getProduktBatchList(); 
 		for (int i=0; i < produktbatch.size(); i++) {
 			produktbatchid = produktbatch.get(i).getPbId();
 			receptid = produktbatch.get(i).getReceptId();
 			dato = produktbatch.get(i).getDato();
 			status = produktbatch.get(i).getStatus();
 			html += "<tr><td>"+produktbatchid+"</td><td>"+receptid+"</td><td>"+dato+"</td><td>"+statusToString(status)+"</td></tr>";
 		}
 		html +="</table>";
 		request.setAttribute("list", html);
 		request.getRequestDispatcher("/WEB-INF/admin/produktbatch/showproduktbatch.jsp").forward(request, response);
 	}
 	private void handleCreateOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		String navn = request.getParameter("navn");
 		String init = request.getParameter("init");
 		String cpr = request.getParameter("cpr");
 		String newPw = funktionalitetsLaget.generatePassword();
 		int aktoer = Integer.parseInt(request.getParameter("aktoer"));
 		funktionalitetsLaget.getOprDAO().createOperatoer(new OperatoerDTO(id, navn, init, cpr, newPw, aktoer));
 		request.setAttribute("password", "<br>Brugerens password er: " + newPw);
 		int oprID = 0;
 		try {
 			oprID = funktionalitetsLaget.getOprDAO().getHighestOprID().getOprId()+1;
 		} catch (DALException e) {
 			e.printStackTrace();
 		}
 		request.setAttribute("autoid", oprID);
 
 	}
 
 	private void handleRemoveOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	}
 	private void handleUpdateOprSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	}
 	private void handleCreateVareSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		String navn = request.getParameter("navn");
 		String leverandoer = request.getParameter("leverandoer");
 		funktionalitetsLaget.getRaavareDAO().createRaavare(new RaavareDTO(id, navn, leverandoer));
 	}
 	private void handleUpdateVareSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int id = Integer.parseInt(request.getParameter("id"));
 		String navn = request.getParameter("navn");
 		String leverandoer = request.getParameter("leverandoer");
 		funktionalitetsLaget.getRaavareDAO().updateRaavare(new RaavareDTO(id, navn, leverandoer));
 		
 		int vareId;
 		String vareNavn;
 		List<RaavareDTO> oprList = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		String html = "<select name='id'>";
 		for (int i=0; i < oprList.size(); i++) {
 			vareId = oprList.get(i).getRaavareId();
 			vareNavn = oprList.get(i).getRaavareNavn();
 			html += "<option value='"+vareId+"'>"+vareId+" - "+vareNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("vareList", html);
 	}
 	private void handleCreateReceptSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
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
 	}
 	private void handleCreateRaavareBatchSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int raavarebatchid = Integer.parseInt(request.getParameter("raavarebatchid"));
 		int raavareid = Integer.parseInt(request.getParameter("raavareid"));
 		double maengde = Double.parseDouble(request.getParameter("maengde"));
 		funktionalitetsLaget.getRaavareBatchDAO().createRaavareBatch(new RaavareBatchDTO(raavarebatchid, raavareid, maengde));
 		
 		int vareId;
 		String vareNavn;
 		List<RaavareDTO> oprList = funktionalitetsLaget.getRaavareDAO().getRaavareList();
 		String html = "<select name='raavareid'>";
 		for (int i=0; i < oprList.size(); i++) {
 			vareId = oprList.get(i).getRaavareId();
 			vareNavn = oprList.get(i).getRaavareNavn();
 			html += "<option value='"+vareId+"'>"+vareId+" - "+vareNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("vareList", html);
 	}
 	private void handleCreateProduktBatchSubmit(HttpServletRequest request, HttpServletResponse response, Functionality funktionalitetsLaget) throws ServletException, IOException, DALException {
 		int produktbatchid = Integer.parseInt(request.getParameter("produktbatchid"));
 		int receptid = Integer.parseInt(request.getParameter("receptid"));
 		int status = Integer.parseInt(request.getParameter("status"));
 		Calendar d = Calendar.getInstance();
 		DecimalFormat df = new DecimalFormat("00");
 		String dato = d.get(Calendar.YEAR) + "-"
 		+ df.format(d.get(Calendar.MONTH) + 1) + "-"
 		+ df.format(d.get(Calendar.DATE)) + " "
 		+ df.format(d.get(Calendar.HOUR_OF_DAY)) + ":"
 		+ df.format(d.get(Calendar.MINUTE)) + ":"
 		+ df.format(d.get(Calendar.SECOND));
 		funktionalitetsLaget.getProduktBatchDAO().createProduktBatch(new ProduktBatchDTO(produktbatchid,receptid,dato,status));
 		
 		int receptId;
 		String receptNavn;
 		List<ReceptDTO> receptList = funktionalitetsLaget.getReceptDAO().getReceptList();
 		String html = "<select name='receptid'>";
 		for (int i=0; i < receptList.size(); i++) {
 			receptId = receptList.get(i).getReceptId();
 			receptNavn = receptList.get(i).getReceptNavn();
 			html += "<option value='"+receptId+"'>"+receptId+" - "+receptNavn+"</option>";
 		}
 		html +="</select>";
 		request.setAttribute("receptList", html);
 	}
 	private String statusToString(int id) {
 		String status;
 		switch(id) {
 			case 0:
 				status = "Startet";
 				break;
 			case 1:
 				status = "Under produktion";
 				break;
 			case 2:
 				status = "Afsluttet";
 				break;
 			default:
 				status = "Invalid status";
 				break;
 		}
 		return status;
 	}
 }
