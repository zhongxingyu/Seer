 package uk.ac.dundee.computing.aec.controller;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.RequestDispatcher;
 
 import uk.ac.dundee.computing.aec.util.*;
 import uk.ac.dundee.computing.aec.model.*;
 import java.util.LinkedList;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.Point2D.Double;
 /**
  * Servlet implementation class Experiment
  */
 @WebServlet({ "/Experiment", "/Experiment/*" })
 //Usage /Experiment/#Experiment/#stage/#substage
//      Arg0 -> site
//      Arg1 -> Experiement
//      Arg2  -> #Experiment
//      Arg3 -> #stage
//      Arg4 -> #substage
 
 public class Experiment extends HttpServlet {
 	private static final long serialVersionUID = 1L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public Experiment() {
         super();
         // TODO Auto-generated constructor stub
     }
 
 	/**
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 		
 		String [] args= Convertors.SplitRequestPath(request);
 		int arg[] = new int[20];
 		int argv =args.length;
 		System.out.println(""+argv);
 		for (int i=0;i<argv;i++){
 			System.out.println(i+" : "+ args[i]);
 			try{
 				arg[i]=Integer.parseInt(args[i]);
 			}catch(Exception et){
 				System.out.println("Can Not convert number "+et);
 			}
 		}
 		MassSpec ms = new MassSpec();
 		if (argv>1){
			switch(arg[3]){
 			case 1:if (argv>4){
 				List <Point2D.Double> Ids = ms.getIdentifedSpectra();
 				request.setAttribute("Data", Ids);
 			}else{
 				List <Point2D.Double> Points = ms.getMassSpec();
 				request.setAttribute("Data", Points);
 			}
 			break;
 			case 2:List <Point2D.Double> Centroids = ms.getCentroids();
 				request.setAttribute("Data", Centroids);
 				break;
 		}
 		}
 		RequestDispatcher rdjson=request.getRequestDispatcher("/RenderJson");
 		rdjson.forward(request,response);
 		
 		
 	}
 
 	/**
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		// TODO Auto-generated method stub
 	}
 
 }
