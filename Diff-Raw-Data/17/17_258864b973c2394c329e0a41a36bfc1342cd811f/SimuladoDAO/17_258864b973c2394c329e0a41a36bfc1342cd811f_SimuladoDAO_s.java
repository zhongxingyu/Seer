 package br.com.puc.sispol.dao;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import br.com.puc.sispol.ConnectionFactory;
 import br.com.puc.sispol.modelo.AreaDeConhecimentoQuantidade;
 import br.com.puc.sispol.modelo.Questao;
 import br.com.puc.sispol.modelo.Simulado;
 
 public class SimuladoDAO {
 	private final Connection connection;
 
 	public SimuladoDAO() {
 		try {
 			this.connection = new ConnectionFactory().getConnection();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// UCS - Criar Simulado
 	public Long adiciona(Simulado simulado) {
 		String sql = "insert into Simulado (DataDeRealizacao, HoraDeRealizacao, Duracao, PontuacaoMinima, Titulo) values (?,?,?,?,?)";
 		PreparedStatement stmt;
 		ResultSet rs = null;
 		long codSimulado = 0;
 		try {
 
 			stmt = connection.prepareStatement(sql,
 					Statement.RETURN_GENERATED_KEYS);
 			stmt.setDate(1, simulado.getDataDeRealizacao() != null ? new Date(
 					simulado.getDataDeRealizacao().getTimeInMillis()) : null);
 			stmt.setString(2, simulado.getHoraDeRealizacao());
 			stmt.setLong(3, simulado.getDuracao());
 			stmt.setLong(4, simulado.getPontuacaoMinima());
 			stmt.setString(5, simulado.getTitulo());
 			stmt.executeUpdate();
 			rs = stmt.getGeneratedKeys();
 			if (rs != null && rs.next()) {
 				System.out.println("Generated CodSimulado: " + rs.getInt(1));
 				codSimulado = (long) rs.getInt(1);
 
 			}
 		} catch (SQLException e) {
 
 			throw new RuntimeException(e);
 		}
 
 		sql = "insert into SimuladoPossuiQuestao (CodQuestao, CodSimulado) values (?,?)";
 
 		for (Questao questao : simulado.getQuestoes()) {
 			try {
 
 				stmt = connection.prepareStatement(sql);
 				stmt.setLong(1, questao.getCodQuestao());
 				stmt.setLong(2, codSimulado);
 				stmt.execute();
 			} catch (SQLException e) {
 				System.out.println(stmt);
 				throw new RuntimeException(e);
 			}
 		}
 		return codSimulado;
 
 	}
 
 	// UCS - Efetuar inscrição Simulado
	public List<Simulado> listaAguardandoInscricao() {
 
 		try {
 			List<Simulado> simulados = new ArrayList<Simulado>();
 			PreparedStatement stmt = this.connection
					.prepareStatement("select * from Simulado where DataDeRealizacao > current_date()");

 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				simulados.add(populaSimulado(rs));
 			}
 
 			rs.close();
 			stmt.close();
 
 			return simulados;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	private Simulado populaSimulado(ResultSet rs) throws SQLException {
 		Simulado simulado = new Simulado();
 
 		// popula o objeto tarefa
 		simulado.setCodSimulado(rs.getLong("CodSimulado"));
 		simulado.setTitulo(rs.getString("Titulo"));
 		simulado.setHoraDeRealizacao(rs.getString("HoraDeRealizacao"));
 		simulado.setDuracao(rs.getInt("Duracao"));
 		simulado.setPontuacaoMinima(rs.getInt("PontuacaoMinima"));
 
 		// popula a data de finalizacao da tarefa, fazendo a conversao
 		Date data = rs.getDate("DataDeRealizacao");
 		if (data != null) {
 			Calendar dataDeRealizacao = Calendar.getInstance();
 			dataDeRealizacao.setTime(data);
 			simulado.setDataDeRealizacao(dataDeRealizacao);
 		}
 		return simulado;
 	}
 
 	// UCS - Efetuar inscrição Simulado
 	public void efetuaInscricao(Long codSimulado, Long codUsuario) {
 
 		if (codSimulado == null) {
 			throw new IllegalStateException(
 					"Código do Simulado não deve ser nulo.");
 		}
 
 		if (codUsuario == null) {
 			throw new IllegalStateException(
 					"Código do Usuário não deve ser nulo.");
 		}
 
 		String sql = "INSERT INTO Resultado (CodSimulado, CodUsuario, NotaDoSimulado) VALUES (?,?,?)";
 		PreparedStatement stmt;
 		try {
 			stmt = connection.prepareStatement(sql);
 			stmt.setLong(1, codSimulado);
 			stmt.setLong(2, codUsuario);
 			stmt.setLong(3, 0);
 			stmt.execute();
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	// UCS - Realizar Simulado Inscrito
 	public Simulado buscaPorUsuario(Long codUsuario) {
 		Simulado simulado = new Simulado();
 		try {
 			System.out.println("Consulta Simulado...");
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select "
 							+ "    *"
 							+ " from "
 							+ "     Simulado AS s"
 							+ "     INNER JOIN Resultado AS r"
 							+ "        ON (s.CodSimulado = r.CodSimulado)"
 							+ " WHERE"
 							+ "    concat(curdate(), ' ' , curtime())"
 							+ "        BETWEEN "
 							+ "            TIMESTAMP(concat(s.DataDeRealizacao, ' ' , s.HoraDeRealizacao))"
 							+ "            AND DATE_ADD(TIMESTAMP(concat(s.DataDeRealizacao, ' ' , s.HoraDeRealizacao)), INTERVAL Duracao HOUR)"
 							+ "    AND r.CodUsuario = ?" + "  LIMIT 0,1");
 			stmt.setLong(1, codUsuario);
 
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 				simulado = populaSimulado(rs);
 			}
 
 			System.out.println("Consulta areas de Conhecimento do Simulado...");
 			List<AreaDeConhecimentoQuantidade> areasDeConhecimentoQuantidade = new ArrayList<AreaDeConhecimentoQuantidade>();
 			stmt = this.connection
 					.prepareStatement("	SELECT "
 							+ " a.Titulo, count(*) AS total "
 							+ " FROM "
 							+ "		sispol.SimuladoPossuiQuestao AS s "
 							+ "		INNER JOIN sispol.Questao AS q "
 							+ "			ON (s.CodQuestao = q.CodQuestao) "
 							+ "		INNER JOIN sispol.AreaDeConhecimento AS a "
 							+ "			ON (q.CodAreaDeConhecimento = a.CodAreaDeConhecimento) "
 							+ " WHERE " + "		CodSimulado = ? "
 							+ " GROUP BY a.Titulo ORDER BY a.Titulo");
 			stmt.setLong(1, simulado.getCodSimulado());
 			System.out.println(stmt);
 			rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				AreaDeConhecimentoQuantidade areaDeConhecimentoQuantidade = new AreaDeConhecimentoQuantidade();
 
 				// popula o objeto tarefa
 				areaDeConhecimentoQuantidade.setTitulo(rs.getString("Titulo"));
 				areaDeConhecimentoQuantidade.setQuantidade(rs.getInt("total"));
 				areasDeConhecimentoQuantidade.add(areaDeConhecimentoQuantidade);
 			}
 
 			simulado.setAreasDeConhecimentoQuantidade(areasDeConhecimentoQuantidade);
 
 			System.out.println("Consulta questões do Simulado...");
 			// funccao entra aqui
 			simulado.setQuestoes(getQuestoesDoSimulado(simulado));
 
 			rs.close();
 			stmt.close();
 
 			return simulado;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// UCS - Consultar Classificacao oficial
 	public List<Simulado> listaUltimosSimuladosRealizados() {
 
 		try {
 			List<Simulado> simulados = new ArrayList<Simulado>();
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select * from Simulado AS s where concat(curdate(), ' ' , curtime()) > DATE_ADD(TIMESTAMP(concat(s.DataDeRealizacao, ' ' , s.HoraDeRealizacao)), INTERVAL Duracao HOUR) LIMIT 0,20");
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				// adiciona a tarefa na lista
 				simulados.add(populaSimulado(rs));
 			}
 
 			rs.close();
 			stmt.close();
 
 			return simulados;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	public Simulado buscaPorCodigo(Long codSimulado) {
 		if (codSimulado == null) {
 			throw new IllegalStateException(
 					"Codigo do Simulado não deve ser nulo.");
 		}
 		Simulado simulado = new Simulado();
 		try {
 			PreparedStatement stmt = this.connection
 					.prepareStatement("select * from sispol.Simulado where CodSimulado = ?");
 			
 			stmt.setLong(1, codSimulado);
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			if (rs.next()) {
 				 simulado = populaSimulado(rs);
 			}
 			
 			simulado.setQuestoes(getQuestoesDoSimulado(simulado));
 			
 			stmt = this.connection
 					.prepareStatement("SELECT a.Titulo AS Titulo, count(*) AS Quantidade FROM sispol.Simulado AS s INNER JOIN sispol.SimuladoPossuiQuestao AS spq ON (s.CodSimulado = spq.CodSimulado) INNER JOIN sispol.Questao AS q ON (q.CodQuestao = spq.CodQuestao) INNER JOIN sispol.AreaDeConhecimento AS a ON (q.CodAreaDeConhecimento = a.CodAreaDeConhecimento) WHERE s.CodSimulado = ? GROUP BY 1");
 			stmt.setLong(1, codSimulado);
 			System.out.println(stmt);
 			rs = stmt.executeQuery();
 			
 			List<AreaDeConhecimentoQuantidade> areas = new ArrayList<AreaDeConhecimentoQuantidade>();
 			while (rs.next()) {
 				AreaDeConhecimentoQuantidade acQuantidade = new AreaDeConhecimentoQuantidade();
 				acQuantidade.setQuantidade(rs.getInt("Quantidade"));
 				acQuantidade.setTitulo(rs.getString("Titulo"));
 				areas.add(acQuantidade);
 				
 			}
 			simulado.setAreasDeConhecimentoQuantidade(areas);
 			
 			
 			rs.close();
 			stmt.close();
 
 			return simulado;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public List<Questao> getQuestoesDoSimulado(Simulado simulado) {
 		List<Questao> questoes = new ArrayList<Questao>();
 		try {
 			PreparedStatement stmt = this.connection
 					.prepareStatement("	SELECT "
 							+ " q.CodQuestao, "
 							+ " q.Enunciado, "
 							+ " q.OpcaoA, "
 							+ " q.OpcaoB, "
 							+ " q.OpcaoC, "
 							+ " q.OpcaoD, "
 							+ "   q.OpcaoE, "
 							+ "   q.OpcaoCorreta "
 							+ " FROM "
 							+ " 	sispol.SimuladoPossuiQuestao AS s "
 							+ "		INNER JOIN sispol.Questao AS q "
 							+ "			ON (s.CodQuestao = q.codQuestao) "
 							+ " 	INNER JOIN sispol.AreaDeConhecimento AS a "
 							+ " 		ON (q.CodAreaDeConhecimento = a.CodAreaDeConhecimento) "
 							+ " WHERE " + "		s.CodSimulado = ? "
 							+ " ORDER BY a.Titulo ");
 			stmt.setLong(1, simulado.getCodSimulado());
 			System.out.println(stmt);
 			ResultSet rs = stmt.executeQuery();
 
 			while (rs.next()) {
 				Questao questao = new Questao();
 				System.out.println("CodQuestao:" + rs.getLong("CodQuestao")
 						+ " Enunciado: " + rs.getString("Enunciado")
 						+ " OpcaoA:" + rs.getString("OpcaoA") + " OpcaoB:"
 						+ rs.getString("OpcaoB") + " OpcaoC:"
 						+ rs.getString("OpcaoC"));
 				// popula o objeto tarefa
 				questao.setCodQuestao(rs.getLong("CodQuestao"));
 				questao.setEnunciado(rs.getString("Enunciado"));
 				questao.setOpcaoA(rs.getString("OpcaoA"));
 				questao.setOpcaoB(rs.getString("OpcaoB"));
 				questao.setOpcaoC(rs.getString("OpcaoC"));
 				questao.setOpcaoD(rs.getString("OpcaoD"));
 				questao.setOpcaoE(rs.getString("OpcaoE"));
 				questao.setOpcaoCorreta(rs.getString("OpcaoCorreta"));
 				questoes.add(questao);
 			}
 			rs.close();
 			stmt.close();
 			return questoes;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
