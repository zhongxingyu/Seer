 package com.bk.converter;
 
import com.bk.model.Book;
import com.bk.service.BookService;
import java.util.logging.Level;
import java.util.logging.Logger;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.convert.Converter;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 /**
  * @author Andrei Petraru
  * Date: 3/2/13
  */
 @Component
 public class BookConverter implements Converter {
 
 	@Autowired
 	BookService bookService;
 
 	@Override
 	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		if (value.isEmpty()) {
			return null;
		}
		try {
			return bookService.findById(Long.parseLong(value));
		}
		catch (NumberFormatException ex) {
			Logger.getLogger(BookConverter.class.getName()).log(Level.SEVERE, null, ex);
 			return null;
 		}
 	}
 
 	@Override
 	public String getAsString(FacesContext context, UIComponent component, Object value) {
 		if (value == null) {
 			return null;
 		}
 		Book book = (Book) value;
 		return String.valueOf(book.getId());
 	}
 }
