 package me.guillsowns.docgym.web;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import me.guillsowns.docgym.business.PDFCalendar;
 import me.guillsowns.docgym.domain.ScheduleEntry;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.lowagie.text.DocumentException;
 
 @Controller
 public class PDFController
 {
 	@RequestMapping("/print.pdf")
 	public void printPdf(HttpServletResponse response, HttpSession session, Locale locale) throws IOException, DocumentException
 	{
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		PDFCalendar cal = new PDFCalendar();
 
 		Date calStart = (Date) session.getAttribute("calStart");
 		Date calEnd = (Date) session.getAttribute("calEnd");
 		Date calCurrent = (Date) session.getAttribute("calCurrent");
 
 		List<ScheduleEntry> entries = ScheduleEntry.findScheduleEntrysByTimeStartBetween(calStart, calEnd).getResultList();
 
 		Calendar midPointCalendar = Calendar.getInstance();
 		midPointCalendar.setTime(calCurrent);
 		int year = midPointCalendar.get(Calendar.YEAR);
 		int month = midPointCalendar.get(Calendar.MONTH);
 
 		cal.createPdf(baos, locale, entries, year, month);
 
 		response.setStatus(HttpServletResponse.SC_OK);
 		response.setContentType("Content-type: application/pdf");
 		response.setContentLength(baos.size());
 
 		response.getOutputStream().write(baos.toByteArray());
 		response.getOutputStream().flush();
 		response.getOutputStream().close();
 	}
 
 }
