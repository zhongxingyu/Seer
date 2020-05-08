 package com.cs174.starrus.controller;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import com.cs174.starrus.view.IView;
 import com.cs174.starrus.view.RegView;
 
 public class RegSubmitController implements IController{
 	private Connection conn;
 	private RegView rV = RegView.getView();
 	@Override
 	public void setView(IView view) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void process(String model) {
 		conn = DBconnector.getConnection();
 		
 		try {
 			int age, taxid;
 			String username, cname, phone, psw, email,state;
 			username = rV.getTxtUsername().getText();
 			psw = rV.getTxtPassword().getText();
 			cname = rV.getTxtCName().getText();	
 			phone = rV.getTxtPhone().getText();
 			state = rV.getTxtState().getText();		
 			email = rV.getTxtEmail().getText();
 			taxid = Integer.parseInt(rV.getTxtTaxid().getText());

 			String query = "insert into customer(username, cname, phone_num, state, tax_id, psd, email, clevel ) values ("
 				+ "'" 	+ username + "' ,"
 				+ "'" 	+ cname + "',"
 				+ "'" 	+ phone + "',"
 				+ "'" 	+ state + "'," 
 				+ taxid + ","
 				+ "'" 	+ psw + "',"
 				+ "'" 	+ email + "',"
				+ 2 	+ ","	
 				+ ")";
 			Statement stmt	= conn.createStatement();
 			stmt.executeQuery(query);
 			rV.getTxtEmail().setText(null);
 			rV.getTxtCName().setText(null);
 			rV.getTxtPassword().setText(null);
 			rV.getTxtPhone().setText(null);
 			rV.getTxtState().setText(null);
 			rV.getTxtTaxid().setText(null);
 			rV.getTxtUsername().setText(null);
 			rV.dispose();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			rV.getLblWarning().setText("username, password, taxID empty");
 			rV.getTxtEmail().setText(null);
 			rV.getTxtCName().setText(null);
 			rV.getTxtPassword().setText(null);
 			rV.getTxtPhone().setText(null);
 			rV.getTxtState().setText(null);
 			rV.getTxtTaxid().setText(null);
 			rV.getTxtUsername().setText(null);
 		} // Specify the SQL Query to run
 	}
 
 }
