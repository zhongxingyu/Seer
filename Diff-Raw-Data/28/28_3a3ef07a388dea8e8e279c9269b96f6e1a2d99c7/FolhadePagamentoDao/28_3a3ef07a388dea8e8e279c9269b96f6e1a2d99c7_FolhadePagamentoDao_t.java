 package br.com.dao;
 
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JOptionPane;
 
 
 import br.com.exception.DaoException;
 import br.com.util.DbUtil;
 
 import br.com.bean.FolhaPagamento;
 
 
 public class FolhadePagamentoDao {
 
 	private static final String EXCLUIR_PAGAMENTO = 
 			"delete from tbfolhadepagamento where codDep = ?";
 	
 	private static final String INSERIR_PAGAMENTO =
 			"insert into tbfolhadepagamento(numFunc, nomeFunc, salarioFunc, " +
 			"comissaoFuncTotal, bonusFunc, totalFunc, profissaoFunc, dataInicio, dataFim)" +
 			"values (?,?,?,?,?,?,?,?,?)";
 	
 	private static final String ATUALIZAR_PAGAMENTO =
 			"update tbfolhadepagamento set " +
 			"numFunc = ?, " +
 			"nomeFunc = ?, " +			
 			"salarioFunc = ?, " +
 			"comissaoFuncTotal = ?, " +
 			"bonusFunc = ?, " +
 			"totalFunc = ?, " +
 			"profissaoFunc = ?, " +
 			"dataInicio = ?, " +
 			"dataFim = ? " +
 			"where codDep = ? ";
 	
 	
 	private static final  String CONSULTA_PAGAMENTO=
 			"select * from tbfolhadepagamento order by codDep DESC";
 	
 	private static final  String CONSULTA_PAGAMENTO_ID = 
 			"select * from tbfolhadepagamento where codDep = ?";
 	
 	private static final  String CONSULTA_PAGAMENTO_NOME =
 			"select * from tbfolhadepagamento where nomeFunc like ? order by nomeFunc";	
 	
 	private static final  String PROCURAR_DATA_PAGAMENTO =
 			"select dataInicio, dataFim from tbfolhadepagamento where codDep = ?";	
 	
 
 	private static final String QUANTIDADE_PEDIDO =
			"SELECT COUNT(numPed) FROM tbPedido where numFunc = ? and situacaoPed = 'A' ";
 	
 	
 	public List<FolhaPagamento> consultarPagamento(String nome) throws DaoException{		
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		List<FolhaPagamento> listaFolha = new ArrayList<FolhaPagamento>();
 		try {
 			if(nome.equals("")){
 				statement = conn.prepareStatement(CONSULTA_PAGAMENTO);
 			}else{
 				statement = conn.prepareStatement(CONSULTA_PAGAMENTO_NOME);
 				statement.setString(1, "%"+nome+"%");
 			}
 			result = statement.executeQuery();
 			while (result.next()) {
 				FolhaPagamento objFolha = new FolhaPagamento();
 				objFolha.setCodDep(result.getInt(1)); 
 				objFolha.setNumFunc(result.getInt(2)); 
 				objFolha.setNomeFunc(result.getString(3));
 				objFolha.setSalarioFunc(result.getDouble(4));
 				objFolha.setComissaoFuncTotal(result.getDouble(5));
 				objFolha.setBonusFunc(result.getDouble(6));
 				objFolha.setTotalFunc(result.getDouble(7));
 				objFolha.setProfissaoFunc(result.getString(8));
 				objFolha.setDataInicio(result.getDate(9));
 				objFolha.setDataFim(result.getDate(10));
 				listaFolha.add(objFolha);
 			}
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return listaFolha;		
 	}	
 	
 	public List<FolhaPagamento> consultarPagamento() throws DaoException{		
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		List<FolhaPagamento> listaFolha = new ArrayList<FolhaPagamento>();
 		try {
 			statement = conn.prepareStatement(CONSULTA_PAGAMENTO);
 			result = statement.executeQuery();
 			while (result.next()) {
 				FolhaPagamento objFolha = new FolhaPagamento();
 				objFolha.setCodDep(result.getInt(1)); 
 				objFolha.setNumFunc(result.getInt(2)); 
 				objFolha.setNomeFunc(result.getString(3));
 				objFolha.setSalarioFunc(result.getDouble(4));				
 				objFolha.setComissaoFuncTotal(result.getDouble(5));
 				objFolha.setBonusFunc(result.getDouble(6));
 				objFolha.setTotalFunc(result.getDouble(7));
 				objFolha.setProfissaoFunc(result.getString(8));
 				objFolha.setDataInicio(result.getDate(9));
 				objFolha.setDataFim(result.getDate(10));
 				listaFolha.add(objFolha);
 			}
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return listaFolha;		
 	}
 
 	public FolhaPagamento consultarPagamentoID(int idFunc) throws DaoException{		
 		FolhaPagamento objFolha = new FolhaPagamento();
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(CONSULTA_PAGAMENTO_ID);
 			statement.setInt(1, idFunc);
 			result = statement.executeQuery();
 			while (result.next()) {
 				objFolha.setCodDep(result.getInt(1)); 
 				objFolha.setNumFunc(result.getInt(2)); 
 				objFolha.setNomeFunc(result.getString(3));
 				objFolha.setSalarioFunc(result.getDouble(4));
 				objFolha.setProfissaoFunc(result.getString(8));
 				objFolha.setComissaoFuncTotal(result.getDouble(5));
 				objFolha.setBonusFunc(result.getDouble(6));
 				objFolha.setTotalFunc(result.getDouble(7));
 				objFolha.setDataInicio(result.getDate(9));
 				objFolha.setDataFim(result.getDate(10));
 			}
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return objFolha;		
 	}
 
 	public boolean inserirPagamento(FolhaPagamento obj) throws DaoException{		
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(INSERIR_PAGAMENTO);
 			statement.setInt(1, obj.getNumFunc());
 			statement.setString(2, obj.getNomeFunc());
 			statement.setDouble(3, obj.getSalarioFunc());
 			statement.setDouble(4, obj.getComissaoFuncTotal());
 			statement.setDouble(5, obj.getBonusFunc());
 			statement.setDouble(6, obj.getTotalFunc());
 			statement.setString(7, obj.getProfissaoFunc());
 			statement.setDate(8, DbUtil.getSqlDate(obj.getDataInicio()));
 			statement.setDate(9, DbUtil.getSqlDate( obj.getDataFim()));
 			statement.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return true;		
 	}
 	
 	public boolean atualizarPagamento(FolhaPagamento objFolha) throws DaoException{		
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(ATUALIZAR_PAGAMENTO);			
 			statement.setInt(1, objFolha.getNumFunc());
 			statement.setString(2, objFolha.getNomeFunc());
 			statement.setDouble(3, objFolha.getSalarioFunc());			
 			statement.setDouble(4, objFolha.getComissaoFuncTotal());
 			statement.setDouble(5, objFolha.getBonusFunc());
 			statement.setDouble(6, objFolha.getTotalFunc());
 			statement.setString(7, objFolha.getProfissaoFunc());			
 			statement.setInt(8, objFolha.getCodDep());
 			statement.setDate(9, DbUtil.getSqlDate(objFolha.getDataInicio()));
 			statement.setDate(10, DbUtil.getSqlDate( objFolha.getDataFim()));
 			statement.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return true;		
 	}
 
 	public boolean excluirPagamento(int idFunc) throws DaoException{		
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(EXCLUIR_PAGAMENTO);
 			statement.setInt(1, idFunc);
 			statement.executeUpdate();
 
 		} catch (SQLException e) {
 			throw new DaoException(e);
 		} finally {
 			DbUtil.close(conn, statement, result);
 		}
 		return true;		
 	}
 	
 	public FolhaPagamento procurarDataPag(Integer numFolha) throws DaoException{
 		FolhaPagamento objFolha = new FolhaPagamento();
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(PROCURAR_DATA_PAGAMENTO);
 			statement.setInt(1, numFolha);
 			result = statement.executeQuery();
 			if(result.next()) {
 				objFolha.setDataInicio(result.getDate(1));
 				objFolha.setDataFim(result.getDate(2));
 				
 			}else{
 				JOptionPane.showMessageDialog(null, "Pagamento no encontrado!");
 			}
 		}
 			catch (SQLException e) {
 				throw new DaoException(e);
 			} finally {
 				DbUtil.close(conn, statement, result);
 			}
 			return objFolha;	
 	}
 	
 	public int quantidadePedido(Integer numFunc) throws DaoException{
 		int qntd;
 		Connection conn = DbUtil.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = conn.prepareStatement(QUANTIDADE_PEDIDO);
 			statement.setInt(1, numFunc);
 			
 			result = statement.executeQuery();
 			
 			result.next();
 			qntd = result.getInt(1);
 				
 			
 		}
 			catch (SQLException e) {
 				throw new DaoException(e);
 			} finally {
 				DbUtil.close(conn, statement, result);
 			}
 			return qntd;	
 		
 	}
 }
 
