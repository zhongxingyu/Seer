 package com.jjs.demerits.server;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.PrintWriter;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.jjs.demerits.shared.DemeritsProto;
 import com.jjs.demerits.shared.Note;
 
 public class PostNote extends HttpServlet {
 	@Override
 	protected void service(HttpServletRequest req, HttpServletResponse res)
 			throws ServletException, IOException {
 		DemeritsProto.Note note = 
 				DemeritsProto.Note.parseFrom(req.getInputStream());
 		PersistenceManager pm = PMF.get().getPersistenceManager();
 		try {
 			Note newNote = new Note();
 			newNote.setFrom(note.getFrom());
 			newNote.setTo(note.getTo());
 			newNote.setText(note.getText());
 			newNote.setDate(note.getDate());
 			pm.makePersistent(newNote);
 		}
 		finally {
 			pm.close();
 		}
 		
 		PrintWriter output = res.getWriter();
 		output.write("Saved your demerit: " + note.getText());
 	}
 }
