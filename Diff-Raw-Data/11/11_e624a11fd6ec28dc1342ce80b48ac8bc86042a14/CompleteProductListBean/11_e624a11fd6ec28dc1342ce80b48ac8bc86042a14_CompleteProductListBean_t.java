 package beans;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 public class CompleteProductListBean {
 	private Collection<CompleteProductBean> productList;
 	private String url = null;
 	private Connection conn = null;
 	private Statement stmt = null;
 	private ResultSet rs = null;
 
 	
 	
 	public CompleteProductListBean() {}
 	
 	public CompleteProductListBean(String _url) throws Exception {
 		this.url = _url;
 		initList(url);
 	}
 	
 	private void initList(String _url) throws Exception{
 		this.productList = new ArrayList<CompleteProductBean>();
 		
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
			String sql = "SELECT B.BOOK_ID, B.TITLE, B.DESCRIPTION, AV.FINAL_PRICE AS PRICE, ";
			sql += "B.PROFIT, B.VISIBLE, B.COM_ID AS AUTHOR_ID, B.NAME, B.SURNAME, ";
			sql += "B.IN_STOCK as QTY, B.C_PRICE, B.QTY AS NEEDED ";
			sql += "FROM NEEDED AS B LEFT JOIN AVAILABILITY AS AV ON ";
			sql += "B.BOOK_ID = AV.BOOK_ID ORDER BY BOOK_ID";
 			this.rs = this.stmt.executeQuery(sql);
 			
 			int pid = -1;
 			CompleteProductBean cpb = null;
 			while(this.rs.next()) {
 				if(pid != rs.getInt("BOOK_ID")) {
 					cpb = new CompleteProductBean();
 					pid = rs.getInt("BOOK_ID");
 					cpb.setId(pid);
 					cpb.setProduct(rs.getString("TITLE"));
 					cpb.setDescription(rs.getString("DESCRIPTION"));
 					cpb.setPrice(rs.getInt("PRICE"));
 					cpb.setProfit(rs.getInt("PROFIT"));
 					cpb.setVisbile(rs.getBoolean("VISIBLE"));
 					ComponentBean cb = new ComponentBean();
 					cb.setId(rs.getInt("AUTHOR_ID"));
 					cb.setManufacturer(rs.getString("NAME"));
 					cb.setType(rs.getString("SURNAME"));
 					cb.setQuantity(rs.getInt("QTY"));
 					cb.setPrice(rs.getInt("C_PRICE"));
 					cb.setNeeded(rs.getInt("NEEDED"));
 					cpb.addComponent(cb);
 					this.productList.add(cpb);
 				}
 				else {
 					ComponentBean cb = new ComponentBean();
 					cb.setId(rs.getInt("AUTHOR_ID"));
 					cb.setManufacturer(rs.getString("NAME"));
 					cb.setType(rs.getString("SURNAME"));
 					cb.setQuantity(rs.getInt("QTY"));
 					cb.setPrice(rs.getInt("C_PRICE"));
 					cb.setNeeded(rs.getInt("NEEDED"));
 					cpb.addComponent(cb);
 				}
 			}
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				this.rs.close();
 			} catch (Exception e) {}
 			try {
 				this.stmt.close();
 			} catch (Exception e) {}
 			try {
 				this.conn.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	public Collection<CompleteProductBean> getProductList() {
 		return this.productList;
 	}
 	
 	
 	
 	public String getXml() throws Exception{
 		initList(url);
 		StringBuffer xmlOut = new StringBuffer();
 		Iterator<CompleteProductBean> iter = this.productList.iterator();
 		CompleteProductBean cpb = null;
 		
 		xmlOut.append("<productlist>");
 		while(iter.hasNext()) {
 			cpb = iter.next();
 			xmlOut.append(cpb.getXml());
 		}
 		xmlOut.append("</productlist>");
 		return xmlOut.toString();
 	}
 	
 	
 	
 	public CompleteProductBean getById(int id) {
 		CompleteProductBean cpb = null;
 		Iterator<CompleteProductBean> iter = this.productList.iterator();
 		while(iter.hasNext()) {
 			cpb = iter.next();
 			if(cpb.getId() == id)
 				return cpb;
 		}
 		return null;
 	}
 	
 	
 	private void deleteComponent(int productId, int componentId) throws Exception {
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			String sql = "DELETE FROM COMPOSITION WHERE ";
 			sql += "EL_ID = " + productId + " AND ";
 			sql += "COM_ID = " + componentId;
 			this.stmt.executeUpdate(sql);
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				this.stmt.close();
 			} catch (Exception e) {}
 			try {
 				this.conn.close();
 			} catch (Exception e) {}
 		}
 		
 	}
 	
 	
 	private void updateComponent(int productId, int componentId, int newQuantity) throws Exception {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			String sql = "UPDATE COMPOSITION SET QTY = " + newQuantity;
 			sql += " WHERE EL_ID = " + productId + " AND";
 			sql += " COM_ID = " + componentId;
 			this.stmt.executeUpdate(sql);
 			
 			sql = "UPDATE";
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				this.stmt.close();
 			} catch (Exception e) {}
 			try {
 				this.conn.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	
 	
 	private ComponentBean insertComponent(int productId, int componentId, int quantity) throws Exception {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			String sql = "SELECT * FROM AUTHORS WHERE AUTHOR_ID=" + componentId;
 			this.rs = this.stmt.executeQuery(sql);
 			
 			ComponentBean cb = new ComponentBean();
 			rs.next();
 			cb.setId(rs.getInt("AUTHOR_ID"));
 			cb.setManufacturer(rs.getString("NAME"));
 			cb.setType(rs.getString("SURNAME"));
 			cb.setQuantity(rs.getInt("QTY"));
 			cb.setPrice(rs.getInt("C_PRICE"));
 			cb.setNeeded(quantity);
 			
 			sql = "INSERT INTO COMPOSITION VALUES(" + productId;
 			sql += ", " + componentId + ", " +  quantity + ")";
 			this.stmt.executeUpdate(sql);
 			
 			return cb;
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				this.rs.close();
 			} catch (Exception e) {}
 			try {
 				this.stmt.close();
 			} catch (Exception e) {}
 			try {
 				this.conn.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	
 	
 	public void removeComponent(int productId, int componentId, int quantity) throws Exception {
 		Iterator<CompleteProductBean> iter = this.productList.iterator();
 		CompleteProductBean cpb = null;
 		while(iter.hasNext()) {
 			cpb = iter.next();
 			if(cpb.getId() == productId) {
 				Iterator<ComponentBean> it = cpb.getComponents().iterator();
 				ComponentBean cb = null;
 				while(it.hasNext()) {
 					cb = it.next();
 					if(cb.getId() == componentId) {
 						if(cb.getNeeded() == quantity) {
 							deleteComponent(productId, componentId);
 							cpb.setPrice(cpb.getPrice() - (quantity*cb.getPrice()));
 							it.remove();
 							break;
 						}
 						else {
 							updateComponent(productId, componentId, cb.getNeeded()-quantity);
 							cb.setNeeded(cb.getNeeded()-quantity);
 							cpb.setPrice(cpb.getPrice() - (quantity*cb.getPrice()));
 							break;
 						}
 					}
 				}
 				break;
 			}
 		}
 	}
 	
 	
 	
 	public void addComponent(int productId, int componentId, int quantity) throws Exception {
 		Iterator<CompleteProductBean> iter = this.productList.iterator();
 		CompleteProductBean cpb = null;
 		while(iter.hasNext()) {
 			cpb = iter.next();
 			if(cpb.getId() == productId) {
 				Iterator<ComponentBean> it = cpb.getComponents().iterator();
 				ComponentBean cb = null;
 				boolean found = false;
 				while(it.hasNext()) {
 					cb = it.next();
 					if(cb.getId() == componentId) {
 						updateComponent(productId, componentId, cb.getNeeded()+quantity);
 						cb.setNeeded(cb.getNeeded()+quantity);
 						cpb.setPrice(cpb.getPrice() + (quantity*cb.getPrice()));
 						found = true;
 						break;
 					}
 				}
 				if(!found) {
 					ComponentBean ncb = null;
 					ncb = insertComponent(productId, componentId, quantity);
 					cpb.addComponent(ncb);
 					cpb.setPrice(cpb.getPrice() + (quantity*ncb.getPrice()));
 				}
 				break;
 			}
 		}
 	}
 	
 	
 	
 	public void updateProduct(int productId, String _product,
 			String _description, boolean _visible, int _profit) throws Exception {
 		Iterator<CompleteProductBean> iter = this.productList.iterator();
 		CompleteProductBean cpb = null;
 		boolean found = false;
 		while(iter.hasNext() && !found) {
 			cpb = iter.next();
 			if(cpb.getId() == productId) {
 				found = true;
 			}
 		}
 		if(!found)
 			return;
 		
 		int profit_diff = _profit - cpb.getProfit();
 		
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			this.conn = DriverManager.getConnection(this.url);
 			
 			this.stmt = this.conn.createStatement();
 			
 			String sql = "UPDATE BOOKS SET TITLE = '" + _product + "', ";
 			sql += "DESCRIPTION = '" + _description + "', ";
 			sql += "PROFIT = " + _profit + ", ";
 			sql += "PRICE = " + (cpb.getPrice() + profit_diff) + ", ";
 			if(_visible)
 				sql += "VISIBLE = 1 ";
 			else
 				sql += "VISIBLE = 0 ";
 			sql += "WHERE BOOK_ID = " + productId;
 			this.stmt.executeUpdate(sql);
 			
 			cpb.setProduct(_product);
 			cpb.setDescription(_description);
 			cpb.setProfit(_profit);
 			cpb.setVisbile(_visible);
 			cpb.setPrice(cpb.getPrice() + profit_diff);
 			
 		} catch (SQLException sqle) {
 			throw new Exception(sqle);
 		}
 		
 		finally {
 			try {
 				this.stmt.close();
 			} catch (Exception e) {}
 			try {
 				this.conn.close();
 			} catch (Exception e) {}
 		}
 	}
 	
 	
 	
 }
