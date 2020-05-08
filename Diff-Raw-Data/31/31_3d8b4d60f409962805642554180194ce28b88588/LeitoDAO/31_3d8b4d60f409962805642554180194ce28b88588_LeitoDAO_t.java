 package dao;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Leito;
 
 public class LeitoDAO {
 	
 	public boolean insert(Leito l) {
 		try {
 			Connection con = ConexaoBD.getInstancia().getConexao();
 			String sqlLeito = "insert into tb_leito (co_ala, co_patrimonio) values (" +
 					""+l.getCodigoAla()+", " +
 					""+l.getCodigoPatrimonio()+")";
 			Statement smt = con.createStatement();
 		    smt.execute(sqlLeito);
 		    
 		    String sqlCodigoLeito = "select co_leito from tb_leito " +
 		    		"where co_patrimonio = "+l.getCodigoPatrimonio();
 		    
 		    ResultSet res = smt.executeQuery(sqlCodigoLeito);
 		    if (res.next()) {
 		    	l.setCodigoLeito(res.getInt("co_leito"));
 		    }
 		    res.close();
 		    
 		    String sqlStatusLeito = "insert into tb_status_leito (co_status_leito, co_leito, co_status, dt_inicial) values (" +
		    		"1, " +
 					""+l.getCodigoLeito()+", " +
					"1, " +
 					"NOW())";
 		    smt.execute(sqlStatusLeito);
 		    smt.close();
 		    
 		    return true;
 			
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public boolean update(Leito l) {
 		try {
 			Connection con = ConexaoBD.getInstancia().getConexao();
 			String sqlUpdateLeitoAntigo = "update tb_status_leito set " +
 				"dt_final = NOW() " + 
 				"where co_leito = "+l.getCodigoLeito()+" and " +
 				"dt_final is null";
 			Statement smt = con.createStatement();
 		    smt.executeUpdate(sqlUpdateLeitoAntigo);
 		    
 		    String sqlInsertLeitoStatusNovo = "insert into tb_status_leito (co_status_leito, co_leito, co_status, dt_inicial) values (" +
 		    		"(select count(temp.co_leito)+1 from (select co_leito from tb_status_leito) as temp where temp.co_leito = "+l.getCodigoLeito()+"), " +
 					""+l.getCodigoLeito()+", " +
 					""+l.getCodigoStatusLeito()+", " +
 					"NOW())";
 		    
 		    smt.execute(sqlInsertLeitoStatusNovo);
 		    smt.close();
 		    
 		    return true;
 			
 			
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 	
 	public List<Leito> getLeitosLivres() {
 		try{
 			List<Leito> lle = new ArrayList<Leito>();
 			Connection con = ConexaoBD.getInstancia().getConexao();
 			Statement stm = con.createStatement();
 			ResultSet res = stm.executeQuery("select tb_leito.co_leito, tb_leito.co_ala, tb_leito.co_patrimonio, tb_status_leito.co_status, tb_status.ds_status from tb_leito " +
 					"join tb_status_leito on tb_status_leito.co_leito = tb_leito.co_leito " +
 					"join tb_status on tb_status_leito.co_status = tb_status.co_status " +
 					"where tb_status_leito.co_status = 3 and tb_status_leito.dt_final is null");
 			while (res.next()) {
 				Leito l = new Leito();
 				l.setCodigoLeito(res.getInt("tb_leito.co_leito"));
 				l.setCodigoAla(res.getInt("tb_leito.co_ala"));
 				l.setCodigoPatrimonio(res.getInt("tb_leito.co_patrimonio"));
 				l.setCodigoStatusLeito(res.getInt("tb_status_leito.co_status"));
 				l.setDescricaoStatusLeito(res.getString("tb_status.ds_status"));
 				lle.add(l);				
 			}
 			return lle;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}		
 	}
 	
 	public List<Leito> getLeitos() {
 		try{
 			List<Leito> lle = new ArrayList<Leito>();
 			Connection con = ConexaoBD.getInstancia().getConexao();
 			Statement stm = con.createStatement();
 			ResultSet res = stm.executeQuery("select tb_leito.co_leito, tb_leito.co_ala, tb_leito.co_patrimonio, tb_status_leito.co_status, tb_status.ds_status from tb_leito " +
 					"join tb_status_leito on tb_status_leito.co_leito = tb_leito.co_leito " +
 					"join tb_status on tb_status_leito.co_status = tb_status.co_status " +
 					"where tb_status_leito.dt_final is null order by tb_leito.co_patrimonio");
 			while (res.next()) {
 				Leito l = new Leito();
 				l.setCodigoLeito(res.getInt("tb_leito.co_leito"));
 				l.setCodigoAla(res.getInt("tb_leito.co_ala"));
 				l.setCodigoPatrimonio(res.getInt("tb_leito.co_patrimonio"));
 				l.setCodigoStatusLeito(res.getInt("tb_status_leito.co_status"));
 				l.setDescricaoStatusLeito(res.getString("tb_status.ds_status"));
 				lle.add(l);				
 			}
 			return lle;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public List<Leito> getLeitos(int codigoAla) {
 		try{
 			List<Leito> lle = new ArrayList<Leito>();
 			Connection con = ConexaoBD.getInstancia().getConexao();
 			Statement stm = con.createStatement();
 			ResultSet res = stm.executeQuery("select tb_leito.co_leito, tb_leito.co_ala, tb_leito.co_patrimonio, tb_status_leito.co_status, tb_status.ds_status from tb_leito " +
 					"join tb_status_leito on tb_status_leito.co_leito = tb_leito.co_leito " +
 					"join tb_status on tb_status_leito.co_status = tb_status.co_status " +
 					"where tb_leito.co_ala = "+codigoAla+" and tb_status_leito.dt_final is null order by tb_leito.co_patrimonio");
 			while (res.next()) {
 				Leito l = new Leito();
 				l.setCodigoLeito(res.getInt("tb_leito.co_leito"));
 				l.setCodigoAla(res.getInt("tb_leito.co_ala"));
 				l.setCodigoPatrimonio(res.getInt("tb_leito.co_patrimonio"));
 				l.setCodigoStatusLeito(res.getInt("tb_status_leito.co_status"));
 				l.setDescricaoStatusLeito(res.getString("tb_status.ds_status"));
 				lle.add(l);				
 			}
 			return lle;
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
