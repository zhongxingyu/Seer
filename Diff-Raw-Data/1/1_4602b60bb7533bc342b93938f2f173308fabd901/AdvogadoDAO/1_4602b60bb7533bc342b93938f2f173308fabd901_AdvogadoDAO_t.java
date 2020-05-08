 package br.com.am.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.am.dao.connections.ConnectionFactory;
 import br.com.am.dao.interfaces.AdvogadoDAOInterface;
 import br.com.am.model.Advogado;
 
 
 public class AdvogadoDAO implements AdvogadoDAOInterface{
 
 	@Override
 	public List<Advogado> consultarAdvogados() {
 
 		//Conexo
		//xx
 		Connection conn = ConnectionFactory.getConnectionOracle();
 		
 		//Comunicao
 		String sql = "SELECT CD_PESSOA_ADV, NR_OAB,  NR_CPF,  NR_RG,  DS_EMAIL,  DS_PASSWORD FROM AM_ADVOGADO";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		Advogado advogado = null;
 		List<Advogado> advogados = new ArrayList<Advogado>();
 		
 		try {
 			
 			ps = conn.prepareStatement(sql);
 			rs = ps.executeQuery();
 			
 			while(rs.next()) {
 				
 				advogado = new Advogado();
 				advogado.setCodigoPessoa(rs.getInt("CD_PESSOA_ADV"));
 				advogado.setRegistroOAB(rs.getInt("NR_OAB"));
 				advogado.setCpf(rs.getLong("NR_CPF"));
 				advogado.setRg(rs.getString("NR_RG"));
 				advogado.setEmail(rs.getString("DS_EMAIL"));
 				advogado.setPassword(rs.getString("DS_PASSWORD"));
 				
 				advogados.add(advogado);
 				
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
 			ConnectionFactory.close(conn, ps, rs);
 		}
 		
 		return advogados;
 	}
 
 	@Override
 	public Advogado consultarAdvogado(int codigoAdvogado) {
 		
 		//Conexo
 		Connection conn = ConnectionFactory.getConnectionOracle();
 		
 		//Comunicao
 		String sql = "SELECT CD_PESSOA_ADV, NR_OAB,  NR_CPF,  NR_RG,  DS_EMAIL,  DS_PASSWORD FROM AM_ADVOGADO WHERE CD_PESSOA_ADV = ?";
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		Advogado advogado = null;
 		
 		try {
 			
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, codigoAdvogado);
 			
 			rs = ps.executeQuery();
 			
 			if(rs.next()) {
 				
 				advogado = new Advogado();
 				advogado.setCodigoPessoa(rs.getInt("CD_PESSOA_ADV"));
 				advogado.setRegistroOAB(rs.getInt("NR_OAB"));
 				advogado.setCpf(rs.getLong("NR_CPF"));
 				advogado.setRg(rs.getString("NR_RG"));
 				advogado.setEmail(rs.getString("DS_EMAIL"));
 				advogado.setPassword(rs.getString("DS_PASSWORD"));
 				
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
 			ConnectionFactory.close(conn, ps, rs);
 		}
 		
 		return advogado;
 	}
 
 }
