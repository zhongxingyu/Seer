 package org.softwaresynthesis.mytalk.server.message.servlet;
 
 import java.io.BufferedReader;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.MultipartConfig;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.Part;
 
 import org.softwaresynthesis.mytalk.server.abook.IUserData;
 import org.softwaresynthesis.mytalk.server.dao.UserDataDAO;
 import org.softwaresynthesis.mytalk.server.message.GenerateFileName;
 import org.softwaresynthesis.mytalk.server.message.IMessage;
 import org.softwaresynthesis.mytalk.server.message.Message;
 
 import sun.misc.IOUtils;
 
 
 /**
  * Servlet implementation class AddMessageServlet
  */
 @WebServlet("/AddMessageServlet")
 @MultipartConfig(maxFileSize = 16177215)
 public class AddMessageServlet extends HttpServlet
 {
 	private static final long serialVersionUID = 1000055L;
        
     /**
      * @see HttpServlet#HttpServlet()
      */
     public AddMessageServlet()
 	{
         super();
     }
 
     /**
 	 * Salva un messaggio in segreteria
 	 * tramite richiesta HTTP GET
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						(msgSegreteria) per il
 	 * 						corretto svolgimento dell'operazione	
 	 * @param	response	contiene le risposte prodotte dalla
 	 * 						servlet che verranno inviate ai client
 	 * @throws	{@link IOException} se si verificano errori durante
 	 * 			operazioni di IO
 	 * @throws	{@link ServletException} se si verificano errori
 	 * 			interni alla servlet
 	 */
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
 	{
 		this.doPost(request, response);
 	}
 
 	/**
 	 * Salva un messagggio in segreteria di un utente
 	 * tramite richiesta HTTP POST
 	 * 
 	 * @param	request		contiene i parametri di input
 	 * 						(username, password) per il
 	 * 						corretto svolgimento dell'operazione	
 	 * @param	response	contiene le risposte prodotte dalla
 	 * 						servlet che verranno inviate ai client
 	 * @throws	{@link IOException} se si verificano errori durante
 	 * 			operazioni di IO
 	 * @throws	{@link ServletException} se si verificano errori
 	 * 			interni alla servlet
 	 */
 	 protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		String result = null;
 		PrintWriter writer = null;
 		GenerateFileName file = null;
 		Long sender = null;
 		Long receiver = null;
 		InputStream inputStream = null;
 		Part filePart = null;
 		FileOutputStream out = null;
 		String path = null;
 		String separator = null;
 		UserDataDAO userDAO = null;
 		IUserData send = null;
 		IUserData rec = null;
 		IMessage message = null;
 		MessageDAO messageDAO = null;
 		GregorianCalendar gc = null;
 		String data = null;
 
 		try {
 			sender = Long.parseLong(getValue(request.getPart("sender")));
 			receiver = Long.parseLong(getValue(request.getPart("receiver")));
 			filePart = request.getPart("msg");
 			if (filePart != null)
 			{
 				inputStream = filePart.getInputStream();
 			}
 			path = System.getenv("MyTalkConfiguration");
 			separator = System.getProperty("file.separator");
 			path += separator + "MyTalk" + separator + "Secretariat";
 			file = new GenerateFileName();
 			path += file.next().toString() + ".wav";
 			out = new FileOutputStream(path);
 			out.write(IOUtils.readFully(inputStream, -1, false));
 			out.close();
 			userDAO = new UserDataDAO();
 			send = userDAO.getByID(sender);
 			rec = userDAO.getByID(receiver);
 			message = new Message();
 			message.setId(file.next());
 			message.setSender(send);
 			message.setReceiver(rec);
 			
 			gc = new GregorianCalendar();
 			int day = gc.get(Calendar.DAY_OF_MONTH);
 			int month = gc.get(Calendar.MONTH);
 			int year = gc.get(Calendar.YEAR);
 			data = "" + year + "-" + month + "-" + day;
 			message.setDate(data);
 			messageDAO = new MessageDAO();
 			messageDAO.insert(message);
 			
 			result = "true";
 			writer = response.getWriter();
 			writer.write(result);
 		
 	 } catch (Exception e) {
 			result = "false";
 			writer = response.getWriter();
 			writer.write(result);
 		}
 
 	}
 
 	private static String getValue(Part part) throws IOException
 	{
 		BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream(), "UTF-8"));
 		StringBuilder value = new StringBuilder();
 		char[] buffer = new char[1024];
 		for (int length = 0; (length = reader.read(buffer)) > 0;)
 		{
 			value.append(buffer, 0, length);
 		}
 		return value.toString();
 	}
 }
