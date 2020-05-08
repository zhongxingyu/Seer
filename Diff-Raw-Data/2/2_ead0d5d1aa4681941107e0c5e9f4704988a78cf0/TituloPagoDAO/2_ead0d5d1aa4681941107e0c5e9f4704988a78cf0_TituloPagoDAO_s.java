 package br.com.am.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import br.com.am.bo.TituloBO;
 import br.com.am.dao.connections.ConnectionFactory;
 import br.com.am.dao.interfaces.TituloPagoDAOInterface;
 import br.com.am.model.Titulo;
 import br.com.am.model.TituloPago;
 
 public class TituloPagoDAO implements TituloPagoDAOInterface{
 
 	@Override
 	public void registrarTituloPago(Titulo titulo) {
 		
 		//Conexo
 		Connection conn = ConnectionFactory.getConnectionOracle();
 		
 		//Comunicao
 		String sql = "INSERT INTO AM_TITULO_PAGO(NR_TITULO, DT_PAGAMENTO, VL_PAGO) VALUES " +
 				     "(?,?,?) ";
 		
 		PreparedStatement ps = null;
 		
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, titulo.getNumeroTitulo());
 			ps.setDate(2, new java.sql.Date(new Date().getTime()));
 			ps.setDouble(3, titulo.getValorDocumento());
 			
 			ps.execute();
 			
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
			ConnectionFactory.close(conn, ps, rs);
 		}
 		
 	}
 
 	@Override
 	public List<TituloPago> consultarTitulosPagosPorProcesso(int numeroProcesso) {
 		
 		//Conexo
 		Connection conn = ConnectionFactory.getConnectionOracle();
 		
 		//Comunicao
 		String sql = "SELECT AM_TITULO_PAGO.NR_TITULO AS TITULO, DT_PAGAMENTO, VL_PAGO " +
 				     "FROM AM_TITULO INNER JOIN  AM_TITULO_PAGO " +
 				     "ON AM_TITULO.NR_TITULO = AM_TITULO_PAGO.NR_TITULO " +
 				     "WHERE NR_PROCESSO = ? ";
 		
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		TituloPago tituloPago = null;
 		List<TituloPago> titulosPagos = new ArrayList<TituloPago>();
 		
 		try {
 			ps = conn.prepareStatement(sql);
 			ps.setInt(1, numeroProcesso);
 			
 			rs = ps.executeQuery();
 			
 			while(rs.next()) {
 				
 				tituloPago = new TituloPago();
 				
 				Titulo titulo = TituloBO.consultarTitulo(rs.getInt("TITULO"));
 				tituloPago.setTitulo(titulo);
 				
 				tituloPago.setDataPagamento(rs.getDate("DT_PAGAMENTO"));
 				tituloPago.setValorPago(rs.getDouble("VL_PAGO"));
 				
 				titulosPagos.add(tituloPago);
 			}
 			
 		} catch(SQLException e) {
 			e.printStackTrace();
 		} finally {
 			ConnectionFactory.close(conn, ps, rs);
 		}
 		
 		return titulosPagos;
 
 	}
 
 
 }
