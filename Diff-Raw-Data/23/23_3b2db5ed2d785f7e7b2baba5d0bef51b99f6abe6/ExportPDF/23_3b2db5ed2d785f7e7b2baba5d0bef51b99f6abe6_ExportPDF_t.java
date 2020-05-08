 package org.ldv.sio.getap.app;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.PageSize;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.pdf.PdfWriter;
 
 @Service("ServicePDF")
 public class ExportPDF {
 
 	private DataSource ds;
 
 	public DataSource getDs() {
 		return ds;
 	}
 
 	@Autowired
 	public void setDs(DataSource ds) {
 		this.ds = ds;
 	}
 
 	public void export(HttpServletResponse response) {
 
 		try {
 			Connection con = ds.getConnection();
 			Statement select = con.createStatement();
 			ResultSet rs = select
					.executeQuery("SELECT nom, prenom, libelle, login, mdp FROM user, classe where role='eleve' and user.idClasse = classe.id order by classe.libelle, user.nom, user.prenom");
 
 			Document document = new Document(PageSize.A4);
 
 			try {
 				OutputStream out = response.getOutputStream();
 				PdfWriter writer = PdfWriter.getInstance(document, out);
 				writer.setViewerPreferences(PdfWriter.PageLayoutSinglePage
 						| PdfWriter.PageModeUseThumbs);
 
 				document.open();
 				int cpt = 0;
 				while (rs.next()) {
 					String string = new String();
 					cpt += 3;
 					string = "Nom : "
 							+ rs.getString("nom")
 							+ ", Prenom :  "
 							+ rs.getString("prenom")
 							+ "    (Classe :  "
 							+ rs.getString("libelle")
 							+ ")\n Login : "
 							+ rs.getString("login")
 							+ ", Mot de passe : "
 							+ rs.getString("mdp")
 							+ "\n"
 							+ "--------------------------------------------------------------";
 
 					if (cpt >= 39) {
 						string += "\n\n\n";
 						cpt = 0;
 					}
 					document.add(new Paragraph(string));
 				}
 
 			} catch (DocumentException de) {
 				de.printStackTrace();
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 			document.close();
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
