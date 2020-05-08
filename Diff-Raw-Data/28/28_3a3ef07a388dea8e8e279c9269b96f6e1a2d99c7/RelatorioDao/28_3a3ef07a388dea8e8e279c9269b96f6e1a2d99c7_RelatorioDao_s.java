 package br.com.dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 
 
 import br.com.exception.DaoException;
 import br.com.util.DbUtil;
 
 public class RelatorioDao {
 	private static final String RELATORIO =
 			"SELECT *  FROM tbPedido ORDER BY nomeCliente";
 	
 
 	private static final String RELATORIO_DATA =
 			"SELECT * FROM tbPedido where dataEntrega = ? ORDER BY nomeCliente";
 	
 	private static final String RELATORIO_ENTRE_DATAS =
 			" select * from tbPedido " + 
					" where dataEntrega between ? and ? ";
 	
 	private static final String FOLHA_DE_PAGAMENTO =
 			"SELECT a.nomeCliente, a.nomePaciente, a.observacoesPed, a.totalPedido, a.dataEntrega," +
 			" b.nomeFunc, b.salarioFunc, b.comissaoFuncTotal, b.bonusFunc, b.dataInicio, b.dataFim," +
 			" b.totalFunc FROM tbPedido a " +
 			" inner join tbFolhadePagamento b " +
			" on a.numFunc = b.numFunc and a.dataEntrega between b.dataInicio and b.dataFim " +
 			" WHERE b.numFunc = ? and a.dataEntrega between ? and ?";
 	
 	
 	private static final String FOLHA_DE_PAGAMENTO_SIMPLES =
 			"SELECT * FROM tbFolhadePagamento WHERE numFunc = ? and dataInicio = ? and dataFim = ? ";
 	
 	 public ResultSet pedidosResultSet() throws DaoException {		 
 			Connection conn = DbUtil.getConnection();
 			PreparedStatement statement = null;			
 	        ResultSet rs = null;		
 			
 	        try {	          
 	        	statement = conn.prepareStatement(RELATORIO);
 	        	rs = statement.executeQuery();
 	        } catch (SQLException e) {
 	        	throw new DaoException(e);			
 			}
 	        
 		
 	        return rs;
 	    }
 	 
 	 public ResultSet pedidosResultSet(Date mes) throws DaoException {		 
 			Connection conn = DbUtil.getConnection();
 			PreparedStatement statement = null;			
 	        ResultSet rs = null;		
 			
 	        try {	          
 	        	statement = conn.prepareStatement(RELATORIO_DATA);
 	        	statement.setDate(1, DbUtil.getSqlDate( mes));
 	        	rs = statement.executeQuery();
 	        } catch (SQLException e) {
 	        	throw new DaoException(e);			
 			}
 	        
 		
 	        return rs;
 	    }
 	 
 	 public ResultSet pedidosResultSet(Date data1, Date data2) throws DaoException {		 
 			Connection conn = DbUtil.getConnection();
 			PreparedStatement statement = null;			
 	        ResultSet rs = null;		
 			
 	        try {	          
 	        	statement = conn.prepareStatement(RELATORIO_ENTRE_DATAS);
 	        	statement.setDate(1, DbUtil.getSqlDate( data1));
 	        	statement.setDate(2, DbUtil.getSqlDate( data2));
 	        	rs = statement.executeQuery();
 	        } catch (SQLException e) {
 	        	throw new DaoException(e);			
 			}
 	        
 		
 	        return rs;
 	    }
 	 
 	 /** Relatrio do pagamento do funcionrio */	
 	 public ResultSet folhaResultSet(Integer func, Date date, Date date2) throws DaoException {		 
 			Connection conn = DbUtil.getConnection();
 			PreparedStatement statement = null;			
 	        ResultSet rs = null;		
 			
 	        try {	          
 	        	statement = conn.prepareStatement(FOLHA_DE_PAGAMENTO);
 	        	statement.setInt(1, func);
 	        	statement.setDate(2, DbUtil.getSqlDate( date));
 	        	statement.setDate(3, DbUtil.getSqlDate( date2));
 	        	rs = statement.executeQuery();
 	        } catch (SQLException e) {
 	        	throw new DaoException(e);			
 			}
 	        
 		
 	        return rs;
 	    }
 	 
 	 public ResultSet folhaResultSimples(Integer func, Date date, Date date2) throws DaoException {		 
 			Connection conn = DbUtil.getConnection();
 			PreparedStatement statement = null;			
 	        ResultSet rs = null;		
 			
 	        try {	          
 	        	statement = conn.prepareStatement(FOLHA_DE_PAGAMENTO_SIMPLES);
 	        	statement.setInt(1, func);
 	        	statement.setDate(2, DbUtil.getSqlDate( date));
 	        	statement.setDate(3, DbUtil.getSqlDate( date2));
 	        	rs = statement.executeQuery();
 	        } catch (SQLException e) {
 	        	throw new DaoException(e);			
 			}
 	        
 		
 	        return rs;
 	    }
 }
