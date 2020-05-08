 package si.fri.iskalnikOseb.servlet;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.TreeMap;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import si.fri.iskalnikOseb.dataHelpers.FinanceData;
 import si.fri.iskalnikOseb.dataHelpers.FinanceStructure;
 import si.fri.iskalnikOseb.dataHelpers.OdvetnikStructure;
 import si.fri.iskalnikOseb.dataHelpers.OdvetnikiData;
 import si.fri.iskalnikOseb.dataHelpers.SicrisData;
 import si.fri.iskalnikOseb.dataHelpers.SicrisStructure;
 import si.fri.iskalnikOseb.dataHelpers.TisData;
 import si.fri.iskalnikOseb.dataHelpers.TisStructure;
 import si.fri.iskalnikOseb.dataHelpers.ZdravnikStructure;
 import si.fri.iskalnikOseb.dataHelpers.ZdravnikiData;
 
 /**
  * Servlet implementation class isciServlet
  */
 @WebServlet("/IskanjeOsebeHandlerServlet")
 public class IskanjeOsebeHandlerServlet extends HttpServlet {
 	private static final long serialVersionUID = 1L;
 
 	/**
 	 * Default constructor.
 	 */
 	public IskanjeOsebeHandlerServlet() {
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		request.setCharacterEncoding("UTF-8");
 		doPost(request, response);
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
 	 *      response)
 	 */
 	protected void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		request.setCharacterEncoding("UTF-8");
 		String nameOfPerson = request.getParameter("nameOfPerson");
 		try {
 			ZdravnikStructure zdravnik = ZdravnikiData
 					.getZdravnikData(
 							request.getServletContext()
 									.getRealPath("/WEB-INF/"), nameOfPerson);
 			SicrisStructure[] osebeSicris = SicrisData
 					.getSicrisData(nameOfPerson);
 			TisStructure[] osebeTis = TisData.submittingForm(nameOfPerson);
 			OdvetnikStructure odvetnik = OdvetnikiData
 					.getOdvetnikData(nameOfPerson);
 			ArrayList<FinanceStructure> povezave = FinanceData
 					.getPovezaveOsebe(nameOfPerson);
 			String[] clanki = FinanceData.getClankeOsebe(nameOfPerson);
 
 			request.getSession().setAttribute("iskanaOseba", nameOfPerson);
 			request.getSession().setAttribute("osebeTis", osebeTis);
 			request.getSession().setAttribute("osebeSicris", osebeSicris);
 			request.getSession().setAttribute("zdravnik", zdravnik);
 			request.getSession().setAttribute("odvetnik", odvetnik);
 			request.getSession().setAttribute("povezave", povezave);
 			request.getSession().setAttribute("clanki", clanki);
 
 			response.sendRedirect("izpisZadetkov.jsp");

 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
