 package org.ldv.sio.getap.app;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service("ServiceCSV")
 public class CSV {
 
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
					.executeQuery("SELECT * FROM user, classe where user.idClasse = classe.id and role = 'eleve' order by classe.libelle, user.nom");

 			try {
 				PrintWriter writer = response.getWriter();
 				// FileWriter writer = new FileWriter("F:/test2.csv");
 
				writer.println("nom;prenom;INE;Division");
 				while (rs.next()) {
 					writer.append(rs.getString("nom"));
 					writer.append(";");
 					writer.append(rs.getString("prenom"));
 					writer.append(";");
					writer.append(rs.getString("INE"));
 					writer.append(";");
 					writer.append(rs.getString("libelle"));
					writer.println("");
 				}
 				writer.flush();
 				writer.close();
 
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 			}
 
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 	}
 }
