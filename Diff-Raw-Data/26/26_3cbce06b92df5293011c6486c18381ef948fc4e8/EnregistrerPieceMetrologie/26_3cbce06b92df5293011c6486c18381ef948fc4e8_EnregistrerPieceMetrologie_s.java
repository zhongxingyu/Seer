 /*****************************************************
 * Module : Controlleur
 * Fichier : EnregisterPieceMetrologie
 * Description : 
 * Projet : SKF Traceability
 * Auteur : GC
 * Date : 12/12/12
 * Version : 0.5
 ******************************************************/
 
 package Controlleur;
 
 import java.io.IOException;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import Modele.Piece;
 import dao.PieceDAO;
 
 /**
  * Servlet implementation class EnregisterPieceMetrologie
  */
@WebServlet(description = "Valider la pice en mtrologie", urlPatterns = { "/EnregisterPieceMetrologie" })
 public class EnregistrerPieceMetrologie extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public EnregistrerPieceMetrologie() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		System.out.println("servlet : EnregisterPieceMetrologie");
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		PieceDAO pieceDao;
 		try {
 			String numPiece = request.getParameter( "num_piece" );
 			pieceDao = PieceDAO.getInstance();
 			Piece p = pieceDao.getPiece(numPiece);
 			String statut = request.getParameter("statut");
 			boolean stat = false;
 			if(statut.equals("1")){
 				stat = true;
 			}
 			int result = pieceDao.modifierStatut(p.getP_ID(),stat);
 			RequestDispatcher dispatcher;
			dispatcher = request.getRequestDispatcher("Metrologie/");
 			dispatcher.forward( request, response );
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
