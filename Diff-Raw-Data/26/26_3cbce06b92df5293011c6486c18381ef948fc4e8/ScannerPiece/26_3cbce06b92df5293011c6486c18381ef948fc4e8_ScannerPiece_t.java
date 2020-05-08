 /*****************************************************
 * Module : Controlleur
 * Fichier : ScannerPiece
 * Description : 
 * Projet : SKF Traceability
 * Auteur : GC
 * Date : 12/12/12
 * Version : 0.5
 ******************************************************/
 
 package Controlleur;
 
 import java.io.IOException;
 import java.util.Date;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import dao.LocalisationPieceDAO;
 import dao.PieceDAO;
 
 import Modele.LocalisationPiece;
 import Modele.Parametre;
 import Modele.Piece;
 
 /**
  * Servlet implementation class ScannerPiece
  */
 @WebServlet(description = "Scanne le code barre des pi�ces", urlPatterns = { "/ScannerPiece" })
 public class ScannerPiece extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public ScannerPiece() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		PieceDAO pieceDao;
 		RequestDispatcher dispatcher;
 		try {
 			pieceDao = PieceDAO.getInstance();
 			String numPiece = request.getParameter( "num_piece" );
 			String lieu = request.getParameter("lieu");
			System.out.println(lieu);
 			Piece piece = pieceDao.getPiece(numPiece);
 			if(piece != null)
 			{
 				HttpSession session = request.getSession(true);  
 				session.setAttribute("pieceActive", piece);
 				
 				LocalisationPieceDAO localDAO = LocalisationPieceDAO.getInstance();
 				
 				LocalisationPiece locPiece = new LocalisationPiece(0, new Date(), 1, Integer.parseInt(lieu), Integer.parseInt(numPiece));
 				localDAO.ajouter(locPiece);
 				System.out.println(lieu);
 				dispatcher = request.getRequestDispatcher("servlet/"+lieu);
 				dispatcher.forward( request, response );
 				
 			}
 			else
 			{
 					System.out.println("piece KO");
 			}
 		}catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		System.out.println("servlet : ScannerPiece");
 		
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
