 package com.globalmesh.action.sale;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.globalmesh.dao.MovieDetailDAO;
 import com.globalmesh.dao.SaleDAO;
 import com.globalmesh.dto.MovieDetail;
 import com.globalmesh.dto.Sale;
 import com.globalmesh.util.Constants;
 import com.globalmesh.util.TicketPrinter;
 import com.globalmesh.util.Utility;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Rectangle;
 import com.itextpdf.text.pdf.PdfWriter;
 
 public class RedeemServlet extends HttpServlet {
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String type = req.getParameter("type");
 		
 		if(type.compareTo("getDetails") == 0) {
 			String verifiCode = req.getParameter("ticketSerial");
 			
 			resp.setContentType("text/plain");
 			resp.setCharacterEncoding("UTF-8");
 			
 			Sale sale = SaleDAO.INSTANCE.getSaleByVeriCode(verifiCode);			
 			
 			if(sale != null) {
 				DateFormat showDate = new SimpleDateFormat("yyyy-MM-dd");
 				DateFormat showTime = new SimpleDateFormat("hh:mm a");
 				
 				StringBuilder sb = new StringBuilder();
 				
 				sb.append(showDate.format(sale.getShowDate()));
 				sb.append(";");
 				
 				sb.append(showTime.format(sale.getShowDate()));
 				sb.append(";");
 				
 				sb.append(showDate.format(sale.getTransactionDate()));
 				sb.append(";");
 				
 				sb.append(sale.getTotal());
 								
 				resp.getWriter().write(sb.toString());
 			} else {
 				resp.getWriter().write("false");
 			}
 			
 		}
 		
 		if(type.compareTo("printTicket") == 0) {
 			
 			String veriCode = req.getParameter("serialNum");
 			Sale sale = SaleDAO.INSTANCE.getSaleByVeriCode(veriCode);
 			
 			if(!sale.isRedeem()){ // ticket is not printed
 				MovieDetail movie = MovieDetailDAO.INSTANCE.getMovieById(sale.getMovie());
 				
 				sale.setRedeem(true);
 				SaleDAO.INSTANCE.update(sale);
 						
 				resp.setContentType("application/pdf");
				Rectangle pagesize = new Rectangle(Utility.mmToPt(78), Utility.mmToPt(158));	
 				
				Document ticket = new Document(pagesize, Utility.mmToPt(5), Utility.mmToPt(5),
						Utility.mmToPt(5), Utility.mmToPt(5)); 
 				try {
 					
 					PdfWriter writer = PdfWriter.getInstance(ticket, resp.getOutputStream());
 					ticket.open();
 					TicketPrinter.printTicket(sale, ticket, movie.getMovieName());
 					ticket.close();
 					
 				} catch (DocumentException e) {
 					
 				}	
 			} else {
 				req.setAttribute("msgClass", Constants.MSG_CSS_ERROR);
 				req.setAttribute("message",Utility.getCONFG().getProperty(Constants.TICKET_PRINT_TWICE));
 				req.getRequestDispatcher("/ticketRedeem.jsp").forward(req, resp);	
 			}
 		}
 		
 	}
 	
 }
