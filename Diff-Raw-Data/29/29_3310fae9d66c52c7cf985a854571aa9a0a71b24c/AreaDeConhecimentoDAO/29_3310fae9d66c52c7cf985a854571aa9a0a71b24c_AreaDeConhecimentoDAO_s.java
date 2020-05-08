 package br.com.puc.sispol.dao;
 
 import java.sql.Connection;
import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import br.com.puc.sispol.ConnectionFactory;
 import br.com.puc.sispol.modelo.AreaDeConhecimento;
import br.com.puc.sispol.modelo.Tarefa;
 
 public class AreaDeConhecimentoDAO {
 	private final Connection connection;
 
 	public AreaDeConhecimentoDAO() {
 		try {
 			this.connection = new ConnectionFactory().getConnection();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void adiciona(AreaDeConhecimento areaDeConhecimento) {
 		String sql = "insert into AreaDeConhecimento (Titulo, Descricao) values (?,?)";
 		PreparedStatement stmt;
 		try {
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, areaDeConhecimento.getTitulo());
 			stmt.setString(2, areaDeConhecimento.getDescricao());
 			stmt.execute();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<AreaDeConhecimento> lista() {
 		try {
 			List<AreaDeConhecimento> areaDeConhecimento = new ArrayList<AreaDeConhecimento>();
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select CodAreaDeConhecimento, Titulo, Descricao from AreaDeConhecimento");
 
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				areaDeConhecimento.add(populaAreaDeConhecimento(rs));
 			}
 
 			rs.close();
 			stmt.close();
 
 			return areaDeConhecimento;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private AreaDeConhecimento populaAreaDeConhecimento(ResultSet rs)
 			throws SQLException {
 		AreaDeConhecimento areaDeConhecimento = new AreaDeConhecimento();
 
 		// popula o objeto areaDeConhecimento
 
 		areaDeConhecimento.setCodAreaDeConhecimento(rs
 				.getLong("CodAreaDeConhecimento"));
 		areaDeConhecimento.setTitulo(rs.getString("Titulo"));
 		areaDeConhecimento.setDescricao(rs.getString("Descricao"));
 		return areaDeConhecimento;
 	}
 
 	public AreaDeConhecimento busca(Long codigo) {
 		AreaDeConhecimento areaDeConhecimento = null;
 		// TODO Auto-generated method stub
 		if (codigo == null) {
 			throw new IllegalStateException(
 					"Codigo da area de conhecimento não deve ser nulo.");
 		}
 		try {
 
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select CodAreaDeConhecimento, Titulo, Descricao from AreaDeConhecimento where CodAreaDeConhecimento = ?");
 			stmt.setLong(1, codigo);
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				areaDeConhecimento = populaAreaDeConhecimento(rs);
 			}
 
 			rs.close();
 			stmt.close();
 
 			return areaDeConhecimento;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<AreaDeConhecimento> buscaPorTitulo(String titulo) {
 		// TODO Auto-generated method stub
 		if (titulo == null) {
 			throw new IllegalStateException(
 					"Titulo da area de conhecimento não deve ser nulo.");
 		}
 		try {
 			List<AreaDeConhecimento> areasDeConhecimento = new ArrayList<AreaDeConhecimento>();
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select CodAreaDeConhecimento, Titulo, Descricao from AreaDeConhecimento where lower(Titulo) like lower(trim(?))");
 			stmt.setString(1, "%" + titulo + "%");
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				areasDeConhecimento.add(populaAreaDeConhecimento(rs));
 			}
 
 			rs.close();
 			stmt.close();
 
 			return areasDeConhecimento;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void remove(Long codigo) {
 		if (codigo == null) {
 			throw new IllegalStateException(
 					"Codigo da Area de Conhecimento não deve ser nulo.");
 		}
 
 		String sql = "delete from sispol.AreaDeConhecimento where CodAreaDeConhecimento = ?";
 		PreparedStatement stmt;
 		try {
 			stmt = connection.prepareStatement(sql);
 			stmt.setLong(1, codigo);
 			stmt.execute();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public void altera(AreaDeConhecimento areaDeConhecimento) {
 		String sql = "update sispol.AreaDeConhecimento set titulo = ?, descricao = ? where CodAreaDeConhecimento = ?";
 		PreparedStatement stmt;
 		try {
 			stmt = connection.prepareStatement(sql);
 			stmt.setString(1, areaDeConhecimento.getTitulo());
 			stmt.setString(2, areaDeConhecimento.getDescricao());
 			stmt.setLong(3, areaDeConhecimento.getCodAreaDeConhecimento());
 			stmt.execute();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
