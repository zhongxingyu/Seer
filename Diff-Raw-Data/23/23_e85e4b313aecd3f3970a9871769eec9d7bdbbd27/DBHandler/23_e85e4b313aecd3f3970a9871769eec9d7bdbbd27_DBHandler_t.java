 package bd;
 
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import outros.Consts;
 import outros.Utils;
 
 /**
  * A classe-inv�lucro da Base de Dados (BD).
  * Comunica directamente com ela e executa queries e updates.
  */
 public class DBHandler
 {
 	private static Connection conn;
 	
 	/**
 	 * O m�todo main. Apenas para prop�sitos de teste.
 	 * @param args os argumentos da linha de comandos.
 	 */
 	public static void main(String args[]) {
		/*String str = "22 : (1212-3453-346 -- 4645.75) : (2009) : cen()[]as d : e ( ) { } [ ]  quecas [UMD]";
		String res = str.split(" : ", 3)[2];
 		res = res.substring(res.indexOf(") ")+2);
 		res = res.substring(0, res.lastIndexOf(" ["));
		System.out.println(res);*/
 		open();
 		if (conn != null) {
 			//Icon icon = new ImageIcon("MV5BMTI5Mjc2MTE3OV5BMl5BanBnXkFtZTcwNTc2MzI2Mg@@._V1._CR341,0,1365,1365_SS80_.jpg");
 			//System.out.println(Utils.list(getClientesOrdNome(), ","));
 			//Utils.printStringArrayVector(dbh.getFilmes());
 			//System.out.println(Utils.list(getFilme("2"), ","));
 			//executeNoCommit("");
 
 
 			/*Vector<String[]> vec = select("SELECT * from requisicoes WHERE data_limite is null");
 
 			if(vec.get(0)[8] == null)
 				Utils.dbg("true null");
 			else if(vec.get(0)[8].equals("null"))
 				Utils.dbg("false null");
 			else
 				Utils.dbg("other. wtf?");*/
 			Utils.dbg(montanteActualRequisicao("0"));
 			//Utils.printStringArrayln(Utils.strArrayVectorToArray(select("SELECT montanteAPagar(2) FROM DUAL")));
 			close();
 			System.out.println("tudo bem");
 		} else
 			System.out.println("deu bode");
 	}
 
 	/**
 	 * Instancia um novo objecto de manuseamento da base de dados, criando uma
 	 * liga��o a esta.
 	 */
 	public DBHandler() {
 		open();
 	}
 	
 	/* ----------------------------------------------------------------- */
 	/* ---------------------------- LIGA��O ---------------------------- */
 	/* ----------------------------------------------------------------- */
 	/**
 	 * Cria uma liga��o � BD.
 	 */
 	public static void open() {
 		try {
 			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
 			Class.forName("oracle.jdbc.driver.OracleDriver").newInstance();
 			conn = DriverManager.getConnection(Consts.ORACLE_URL, Consts.ORACLE_USER, Consts.ORACLE_PASS);
 		} catch (SQLException e) {
 			Utils.printError(e);
 		} catch (InstantiationException e) {
 			Utils.printError(e);
 		} catch (IllegalAccessException e) {
 			Utils.printError(e);
 		} catch (ClassNotFoundException e) {
 			Utils.printError(e);
 		}
 	}
 
 	/**
 	 * Fecha a liga��o � BD.
 	 */
 	public static void close() {
 		try {
 			conn.close();
 		} catch (SQLException e) {
 			Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, e);
 		}
 	}
 
 	/* --------------------------------------------------------------------------------- */
 	/* ---------------------------- GETS DE NOMES DE CAMPOS ---------------------------- */
 	/* --------------------------------------------------------------------------------- */
 	
 	public static String[] getCamposClientes() {		return new String[]{"ID_PES", "NOME_PESSOA", "BI", "PASSWORD", "MORADA", "E_MAIL", "TELEFONE", "VALIDO", "DATA_REGISTO"};}
 	public static String[] getCamposEmpregados() {		return new String[]{"ID_PES", "IS_ADMIN", "SALARIO", "NOME_PESSOA", "BI", "PASSWORD", "MORADA", "E_MAIL", "TELEFONE", "VALIDO", "DATA_REGISTO"};}
 	public static String[] getCamposFilmes() {			return new String[]{"ID_FIL", "TITULO", "ANO", "REALIZADOR", "RANKIMDB", "PAIS", "PRODUTORA", "DESCRICAO", "CAPA", "VALIDO"};}
 	public static String[] getCamposFilmeGenero() {		return new String[]{"ID_GEN", "ID_FIL"};}
 	public static String[] getCamposFormatos() {		return new String[]{"ID_FOR", "NOME_FORMATO"};}
 	public static String[] getCamposGeneros() {			return new String[]{"ID_GEN", "NOME_GENERO"};}
 	public static String[] getCamposMaquinasATM() {		return new String[]{"ID_MAQ", "PRECO", "VALIDO", "DATA_INSTALACAO"};}
 	public static String[] getCamposPagamentos() {		return new String[]{"ID_REQ", "MONTANTE"};}
 	public static String[] getCamposRequisicoes() {		return new String[]{"ID_REQ", "ID_MAQ", "EMP_ID_PES", "ID_PES", "ID_FIL", "ID_FOR", "DATA", "DATA_LIMITE", "DATA_ENTREGA"};}
 	public static String[] getCamposStocks() {			return new String[]{"ID_FIL", "ID_FOR", "DISPONIVEIS", "QUANT", "CUSTO_COMPRA", "CUSTO_ALUGUER"};}
 
 	public static String[] getToSetCamposClientes() {		return new String[]{"NOME_PESSOA", "BI", "PASSWORD", "MORADA", "E_MAIL", "TELEFONE"};}
 	public static String[] getToSetCamposEmpregados() {		return new String[]{"IS_ADMIN", "SALARIO", "NOME_PESSOA", "BI", "PASSWORD", "MORADA", "E_MAIL", "TELEFONE"};}
 	public static String[] getToSetCamposFilmes() {			return new String[]{"TITULO", "ANO", "REALIZADOR", "RANKIMDB", "PAIS", "PRODUTORA", "DESCRICAO", "CAPA"};}
 	public static String[] getToSetCamposFilmeGenero() {	return new String[]{};}
 	public static String[] getToSetCamposFormatos() {		return new String[]{"NOME_FORMATO"};}
 	public static String[] getToSetCamposGeneros() {		return new String[]{"NOME_GENERO"};}
 	public static String[] getToSetCamposMaquinasATM() {	return new String[]{"PRECO"};}
 	public static String[] getToSetCamposPagamentos() {		return new String[]{"MONTANTE"};}
 	public static String[] getToSetCamposRequisicoes() {	return new String[]{"DATA_LIMITE", "DATA_ENTREGA"};}
 	public static String[] getToSetCamposStocks() {			return new String[]{"QUANT", "CUSTO_COMPRA", "CUSTO_ALUGUER"};}
 
 	/* ------------------------------------------------------------------ */
 	/* ---------------------------- CLIENTES ---------------------------- */
 	/* ------------------------------------------------------------------ */
 	/**
 	 * Obt�m os clientes existentes.
 	 * @return Vector com os campos de cada cliente.
 	 */
 	public static Vector<String[]> getClientes() {
 		return selectAll("clientes", true);
 	}
 	
 	public static Vector<String[]> getClientesOrdNome() {
 		return selectAll("clientes", "NOME_PESSOA", true);
 	}
 	
 	/**
 	 * Obt�m os dados de um cliente.
 	 * @param id o ID do cliente.
 	 * @return os campos do cliente.
 	 */
 	public static String[] getCliente(String id) {
 		Vector<String[]> vec = selectAll("clientes", "ID_PES", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	public static String[] getClienteBI(String bi) {
 		Vector<String[]> vec = selectAll("clientes", "BI", bi, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona um cliente � BD.
 	 * @param nome o nome do cliente a adicionar.
 	 * @param bi o BI do cliente a adicionar.
 	 * @param password a password do cliente a adicionar.
 	 * @param morada a morada do cliente a adicionar.
 	 * @param email o e-mail do cliente a adicionar.
 	 * @param telefone o n�mero de telefone do cliente a adicionar.
 	 */
 	public static void adicionaCliente(String nome, String bi, String password, String morada, String email, String telefone) {
 		adicionaObjecto("clientes",
 						new String[]{"seq_pessoa_id.NEXTVAL", p(nome), bi, p(password), p(morada), p(email), telefone, "1", "SYSDATE"});
 	}
 	
 	/**
 	 * Actualiza um cliente na BD.
 	 * @param id o ID do cliente a actualizar.
 	 * @param nome o novo nome do cliente a actualizar.
 	 * @param bi o novo BI do cliente a actualizar.
 	 * @param password a nova password do cliente a actualizar.
 	 * @param morada a nova morada do cliente a actualizar.
 	 * @param email o novo e-mail do cliente a actualizar.
 	 * @param telefone o novo n�mero de telefone do cliente a actualizar.
 	 */
 	public static void actualizaCliente(String id, String nome, String bi, String password, String morada, String email, String telefone) {
 		actualizaObjecto("clientes", "ID_PES", id,
 						 getToSetCamposClientes(),
 						 new String[]{p(nome), bi, p(password), p(morada), p(email), telefone});
 	}
 	
 	public static void actualizaCliente(String nome, String bi, String password, String morada, String email, String telefone) {
 		actualizaObjecto("clientes", "BI", bi,
 						 getToSetCamposClientes(),
 						 new String[]{p(nome), bi, p(password), p(morada), p(email), telefone});
 	}
 
 	/**
 	 * Invalida um cliente na BD.
 	 * @param id o ID do cliente a invalidar.
 	 */
 	public static void invalidaCliente(String id) {
 		invalidaObjecto("clientes", "ID_PES", id);
 	}
 	
 	public static void invalidaClienteBI(String bi) {
 		invalidaObjecto("clientes", "BI", bi);
 	}
 	
 	/**
 	 * Re-valida um cliente na BD.
 	 * @param id o ID do cliente a re-validar.
 	 */
 	public static void validaCliente(String id) {
 		validaObjecto("clientes", "ID_PES", id);
 	}
 	
 	public static Vector<String[]> getClientesComEntregasPorFazer() {
 		String query = "SELECT c.ID_PES, c.NOME_PESSOA, c.BI,Count(r.ID_REQ) " +
 					   " FROM clientes c, requisicoes r" +
 					   " WHERE c.ID_PES = r.ID_PES" +
 					   " AND r.DATA_ENTREGA = null" +
 					   " GROUP BY c.ID_PES, c.NOME_PESSOA, c.BI" +
 					   " HAVING COUNT(r.ID_REQ) > 0";
 		return select(query);
 	}
 	
 	public static Vector<String[]> getClientesComEntregasForaDePrazo() {
 		String query = "SELECT c.ID_PES, c.NOME_PESSOA, c.BI ,Count(r.ID_REQ) " +
 					   " FROM clientes c, requisicoes r" +
 					   " WHERE c.ID_PES = r.ID_PES" +
 					   " AND r.DATA_ENTREGA = null" +
 					   " AND r.DATA_LIMITE < SYSDATE" +
 					   " GROUP BY c.ID_PES, c.NOME_PESSOA, c.BI" +
 					   " HAVING COUNT(r.ID_REQ) > 0";
 		return select(query);
 	}
 	
 	public static Vector<String[]> procuraClientes(String nome, String morada, String email, String telefone) {
 		String query = "SELECT ID_PES, NOME_PESSOA, BI" +
 					   " FROM clientes" +
 					   " WHERE ID_PES = ID_PES" +	// redund�ncia para evitar o caso em que o WHERE fica sem nada
 					   (nome.isEmpty()?"":" AND nome_pessoa LIKE "+p("%"+nome+"%")) +
 					   (morada.isEmpty()?"":" AND morada LIKE "+p("%"+morada+"%")) +
 					   (email.isEmpty()?"":" AND e_mail LIKE "+p("%"+email+"%")) +
 					   (telefone.isEmpty()?"":" AND telefone = "+telefone);
 		return select(query);
 	}
 
 	/* -------------------------------------------------------------------- */
 	/* ---------------------------- EMPREGADOS ---------------------------- */
 	/* -------------------------------------------------------------------- */
 	/**
 	 * Obt�m os empregados existentes.
 	 * @return Vector com os campos de cada empregado.
 	 */
 	public static Vector<String[]> getEmpregados() {
 		return selectAll("empregados", true);
 	}
 	
 	/**
 	 * Obt�m os dados de um empregado.
 	 * @param id o ID do empregado.
 	 * @return os campos do empregado.
 	 */
 	public static String[] getEmpregado(String id) {
 		Vector<String[]> vec = selectAll("empregados", "ID_PES", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	public static String[] getEmpregadoBI(String bi) {
 		Vector<String[]> vec = selectAll("empregados", "BI", bi, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona um empregado � BD.
 	 * @param is_admin "1" se o empregado a adicionar � administrador. "0" caso contr�rio.
 	 * @param salario o sal�rio do empregado a adicionar.
 	 * @param nome o nome do empregado a adicionar.
 	 * @param bi o BI do empregado a adicionar.
 	 * @param password a password do empregado a adicionar.
 	 * @param morada a morada do empregado a adicionar.
 	 * @param email o e-mail do empregado a adicionar.
 	 * @param telefone o n�mero de telefone do empregado a adicionar.
 	 * @param data_registo a data de registo do empregado a adicionar.
 	 */
 	public static void adicionaEmpregado(String is_admin, String salario, String nome, String bi, String password, String morada, String email, String telefone) {
 		adicionaObjecto("empregados",
 						new String[]{"seq_pessoa_id.NEXTVAL", is_admin, salario, p(nome), bi, p(password), p(morada), p(email), telefone, "1", "SYSDATE"});
 	}
 	
 	/**
 	 * Actualiza um empregado na BD.
 	 * @param id o ID do empregado a actualizar.
 	 * @param is_admin "1" se o empregado a actualizar fica definido como administrador. "0" caso contr�rio.
 	 * @param salario o novo sal�rio do empregado a actualizar.
 	 * @param nome o novo nome do empregado a actualizar.
 	 * @param bi o novo BI do empregado a actualizar.
 	 * @param password a nova password do empregado a actualizar.
 	 * @param morada a nova morada do empregado a actualizar.
 	 * @param email o novo e-mail do empregado a actualizar.
 	 * @param telefone o novo n�mero de telefone do empregado a actualizar.
 	 */
 	public static void actualizaEmpregado(String id, String is_admin, String salario, String nome, String bi, String password, String morada, String email, String telefone) {
 		actualizaObjecto("empregados", "ID_PES", id,
 						 getToSetCamposEmpregados(),
 					 	 new String[]{is_admin, salario, p(nome), bi, p(password), p(morada), p(email), telefone});
 	}
 	
 	public static void actualizaEmpregado(String is_admin, String salario, String nome, String bi, String password, String morada, String email, String telefone) {
 		actualizaObjecto("empregados", "BI", bi,
 						 getToSetCamposEmpregados(),
 					 	 new String[]{is_admin, salario, p(nome), bi, p(password), p(morada), p(email), telefone});
 	}
 
 	/**
 	 * Invalida um empregado na BD.
 	 * @param id o ID do empregado a invalidar.
 	 */
 	public static void invalidaEmpregado(String id) {
 		invalidaObjecto("empregados", "ID_PES", id);
 	}
 	
 	public static void invalidaEmpregadoBI(String bi) {
 		invalidaObjecto("empregados", "BI", bi);
 	}
 	
 	/**
 	 * Re-valida um empregado na BD.
 	 * @param id o ID do empregado a re-validar.
 	 */
 	public static void validaEmpregado(String id) {
 		invalidaObjecto("empregados", "ID_PES", id);
 	}
 	
 	public static Vector<String[]> procuraEmpregados(String is_admin, String salarioLow, String salarioHigh, String nome, String morada, String email, String telefone) {
 		String query = "SELECT ID_PES, NOME_PESSOA, BI" +
 					   " FROM clientes" +
 					   " WHERE ID_PES = ID_PES" +	// redund�ncia para evitar o caso em que o WHERE fica sem nada
 					   (is_admin.isEmpty()?"":" AND is_admin = "+is_admin) +
 					   (salarioLow.isEmpty()||salarioHigh.isEmpty()?"":" AND salario BETWEEN "+salarioLow+" AND "+salarioHigh) +
 					   (nome.isEmpty()?"":" AND nome_pessoa LIKE "+p("%"+nome+"%")) +
 					   (morada.isEmpty()?"":" AND morada LIKE "+p("%"+morada+"%")) +
 					   (email.isEmpty()?"":" AND e_mail LIKE "+p("%"+email+"%")) +
 					   (telefone.isEmpty()?"":" AND telefone = "+telefone);
 		return select(query);
 	}
 
 	/* ---------------------------------------------------------------- */
 	/* ---------------------------- FILMES ---------------------------- */
 	/* ---------------------------------------------------------------- */
 	/**
 	 * Obt�m os filmes existentes.
 	 * @return Vector com os campos de cada filme.
 	 */
 	public static Vector<String[]> getFilmes() {
 		return selectAll("filmes", true);
 	}
 	
 	public static Vector<String[]> getFilmesOrdTitulo() {
 		return selectAll("filmes", "TITULO", true);
 	}
 	
 	public static Vector<String[]> getFilmesOrdAno() {
 		return selectAll("filmes", "ANO", true);
 	}
 	
 	public static Vector<String[]> getFilmesOrdRankIMDB() {
 		return selectAll("filmes", "RANKIMDB", true);
 	}
 	
 	public static Vector<String[]> getFilmesPlusInvalidos() {
 		return selectAll("filmes", false);
 	}
 	
 	public static Vector<String[]> getFilmesOrdTituloPlusInvalidos() {
 		return selectAll("filmes", "TITULO", false);
 	}
 	
 	public static Vector<String[]> getFilmesOrdAnoPlusInvalidos() {
 		return selectAll("filmes", "ANO", false);
 	}
 	
 	public static Vector<String[]> getFilmesOrdRankIMDBPlusInvalidos() {
 		return selectAll("filmes", "RANKIMDB", false);
 	}
 	
 	/**
 	 * Obt�m os dados de um filme.
 	 * @param id o ID do filme.
 	 * @return os campos do filme.
 	 */
 	public static String[] getFilme(String id) {
 		Vector<String[]> vec = selectAll("filmes", "ID_FIL", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona um filme � BD.
 	 * @param titulo o t�tulo do filme a adicionar.
 	 * @param ano o ano do filme a adicionar.
 	 * @param realizador o realizador do filme a adicionar.
 	 * @param ratingIMDB o rating da IMDB do filme a adicionar.
 	 * @param pais o pa�s de origem do filme a adicionar.
 	 * @param produtora a produtora do filme a adicionar.
 	 * @param descricao a descri��o do filme a adicionar.
 	 * @param capa a capa do filme a adicionar.
 	 */
 	public static void adicionaFilme(String titulo, String ano, String realizador, String ratingIMDB, String pais, String produtora, String descricao, String capa, String[] generos) {
 		adicionaObjecto("filmes",
 						new String[]{"seq_filme_id.NEXTVAL", p(titulo), p(ano), p(realizador), ratingIMDB, p(pais), p(produtora), p(descricao), p(capa), "0"});
 		String currval = select("SELECT seq_filme_id.CURRVAL FROM DUAL").get(0)[0];
 		for(String gen : generos)
 			adicionaFilmeGeneroNome(currval, gen);
 	}
 	
 	/**
 	 * Actualiza um filme na BD.
 	 * @param id o ID do filme a actualizar.
 	 * @param titulo o novo t�tulo do filme a actualizar.
 	 * @param ano o novo ano do filme a actualizar.
 	 * @param realizador o novo realizador do filme a actualizar.
 	 * @param ratingIMDB o novo rating da IMDB do filme a actualizar.
 	 * @param pais o novo pa�s de origem do filme a actualizar.
 	 * @param produtora a nova produtora do filme a actualizar.
 	 * @param descricao a nova descri��o do filme a actualizar.
 	 * @param capa a nova capa do filme a actualizar.
 	 */
 	public static void actualizaFilme(String id, String titulo, String ano, String realizador, String ratingIMDB, String pais, String produtora, String descricao, String capa, String[] generos) {
 		actualizaObjecto("filmes", "ID_FIL", id,
 						 getToSetCamposFilmes(),
 					 	 new String[]{p(titulo), ano, p(realizador), ratingIMDB, p(pais), p(produtora), p(descricao), p(capa)});
 		String[] gensActuais = getGenerosFilmeNome(id);
 		int i;
 		for(String gen : generos) {
 			for(i=0; i<gensActuais.length; i++) {
 				if(gensActuais[i].equals(gen))
 					break;
 			}
 			if(i == gensActuais.length)
 				adicionaFilmeGeneroNome(id, gen);
 		}
 	}
 
 	/**
 	 * Invalida um filme na BD.
 	 * @param id o ID do filme a invalidar.
 	 */
 	public static void invalidaFilme(String id) {
 		invalidaObjecto("filmes", "ID_FIL", id);
 	}
 	
 	/**
 	 * Re-valida um filme na BD.
 	 * @param id o ID do filme a re-validar.
 	 */
 	public static void validaFilme(String id) {
 		invalidaObjecto("filmes", "ID_FIL", id);
 	}
 	
 	public static Vector<String[]> procuraFilmes(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos) {
 		return procuraFilmes(titulo, anoLow, anoHigh, realizador, ratingIMDBLow, ratingIMDBHigh, pais, produtora, generos, true, true);
 	}
 	
 	public static Vector<String[]> procuraFilmesPlusInvalidos(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos) {
 		return procuraFilmes(titulo, anoLow, anoHigh, realizador, ratingIMDBLow, ratingIMDBHigh, pais, produtora, generos, true, false);
 	}
 	
 	public static Vector<String[]> procuraFilmesAnyGen(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos) {
 		return procuraFilmes(titulo, anoLow, anoHigh, realizador, ratingIMDBLow, ratingIMDBHigh, pais, produtora, generos, false, true);
 	}
 	
 	public static Vector<String[]> procuraFilmesPlusInvalidosAnyGen(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos) {
 		return procuraFilmes(titulo, anoLow, anoHigh, realizador, ratingIMDBLow, ratingIMDBHigh, pais, produtora, generos, false, false);
 	}
 	
 	public static Vector<String[]> procuraFilmes(String titulo, String anoLow, String anoHigh, String realizador, String ratingIMDBLow, String ratingIMDBHigh, String pais, String produtora, String[] generos, boolean restrictGen, boolean soValidos) {
 		String op = (restrictGen ? " AND " : " OR ");
 		String query = "SELECT f.ID_FIL, f.ANO, f.TITULO" +
 					   " FROM filmes f";
 		if(generos != null) {
 			query += ", generos g, filme_genero fg" +
 					 " WHERE f.ID_FIL = fg.ID_FIL" +
 					   " AND g.ID_GEN = fg.ID_GEN";
 			for(String gen : generos)
 				query += op + "g.NOME_GENERO = " + p(gen);
 		} else {
 			query += " WHERE f.ID_FIL = f.ID_FIL";		//redund�ncia para evitar o caso em que o WHERE fica sem nada
 		}
 		query += (titulo.isEmpty()?"":" AND titulo LIKE "+p("%"+titulo+"%")) +
 			     (realizador.isEmpty()?"":" AND realizador LIKE "+p("%"+realizador+"%")) +
 			     (pais.isEmpty()?"":" AND pais LIKE "+p("%"+pais+"%")) +
 			     (produtora.isEmpty()?"":" AND produtora LIKE "+p("%"+produtora+"%")) +
 			     (anoLow.isEmpty()||anoHigh.isEmpty()?"":" AND ano BETWEEN "+anoLow+" AND "+anoHigh) +
 			     (ratingIMDBLow.isEmpty()||ratingIMDBHigh.isEmpty()?"":" AND rankIMDB BETWEEN "+ratingIMDBLow+" AND "+ratingIMDBHigh) +
 			     (!soValidos ? "":" AND VALIDO = 1");
 		return select(query);
 	}
 
 	/* ---------------------------------------------------------------------- */
 	/* ---------------------------- FILME/G�NERO ---------------------------- */
 	/* ---------------------------------------------------------------------- */
 	/**
 	 * Obt�m as rela��es filme/g�nero.
 	 * @return Vector com os IDs do filme e g�nero que formam a rela��o.
 	 */
 	public static Vector<String[]> getFilmeGenero() {
 		return selectAll("filme_genero", false);
 	}
 	
 	/**
 	 * Obt�m os g�neros de um filme.
 	 * @param id_fil o ID do filme.
 	 * @return os g�neros do filme.
 	 */
 	public static String[] getGenerosFilmeID(String id_fil) {
 		Vector<String[]> selected = select("filme_genero", new String[]{"ID_GEN"}, "ID_FIL", id_fil, false);
 		return Utils.strArrayVectorToArray(selected, 0);
 	}
 	
 	public static String[] getGenerosFilmeNome(String id_fil) {
 		Vector<String[]> selected = select("SELECT g.NOME_GENERO" +
 										   " FROM generos g, filme_genero fg" +
 										   " WHERE fg.ID_FIL =" + id_fil +
 											 " AND fg.ID_GEN = g.ID_GEN");
 		return Utils.strArrayVectorToArray(selected);
 	}
 	
 	public static Vector<String[]> getGenerosFilme(String id_fil) {
 		return select("SELECT g.ID_GEN, g.NOME_GENERO" +
 					  " FROM generos g, filme_genero fg" +
 					  " WHERE fg.ID_FIL =" + id_fil +
 						" AND fg.ID_GEN = g.ID_GEN");
 	}
 
 	/**
 	 * Adiciona uma rela��o filme/g�nero � BD.
 	 * @param id_fil ID do filme na rela��o.
 	 * @param id_gen ID do g�nero na rela��o.
 	 */
 	public static void adicionaFilmeGenero(String id_fil, String id_gen) {
 		adicionaObjecto("filme_genero",
 						new String[]{id_fil, id_gen});
 	}
 	
 	public static void adicionaFilmeGeneroNome(String id_fil, String nome_genero) {
 		adicionaObjecto("filme_genero",
 						new String[]{id_fil, "(SELECT id_gen FROM generos WHERE nome_genero = "+p(nome_genero)+")"});
 	}
 	
 	/**
 	 * Remove uma rela��o filme/g�nero da BD.
 	 * @param id_fil ID do filme na rela��o.
 	 * @param id_gen ID do g�nero na rela��o.
 	 */
 	public static void removeFilmeGenero(String id_fil, String id_gen) {
 		removeObjecto("filme_genero",
 					  getToSetCamposFilmeGenero(),
 					  new String[]{id_fil, id_gen});
 	}
 
 	/* ------------------------------------------------------------------ */
 	/* ---------------------------- FORMATOS ---------------------------- */
 	/* ------------------------------------------------------------------ */
 	/**
 	 * Obt�m os formatos de filme existentes.
 	 * @return Vector com os campos de cada formato.
 	 */
 	public static Vector<String[]> getFormatos() {
 		return selectAll("formatos", false);
 	}
 	
 	public static Vector<String[]> getFormatosOrdNome() {
 		return selectAll("formatos", "NOME_FORMATO", false);
 	}
 	
 	/**
 	 * Obt�m os dados de um formato.
 	 * @param id o ID do formato.
 	 * @return os campos do formato.
 	 */
 	public static String[] getFormato(String id) {
 		Vector<String[]> vec = selectAll("formatos", "ID_FOR", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	public static String getFormatoNome(String id) {
 		Vector<String[]> vec = selectAll("formatos", "ID_FOR", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0)[1]);
 	}
 	
 	public static String getFormatoID(String nome_formato) {
 		Vector<String[]> vec = selectAll("formatos", "NOME_FORMATO", p(nome_formato), false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0)[0]);
 	}
 	
 	/**
 	 * Adiciona um formato � BD.
 	 * @param nome o nome do formato a adicionar.
 	 */
 	public static void adicionaFormato(String nome) {
 		adicionaObjecto("formatos",
 						new String[]{"seq_formato_id.NEXTVAL", p(nome)});
 	}
 	
 	/**
 	 * Actualiza um formato na BD.
 	 * @param id o ID do formato a actualizar.
 	 * @param nome o novo nome para o formato a actualizar.
 	 */
 	public static void actualizaFormato(String id, String nome) {
 		actualizaObjecto("formatos", "ID_FOR", id,
 						 getToSetCamposFormatos(),
 						 new String[]{p(nome)});
 	}
 
 	/**
 	 * Remove um formato da BD.
 	 * @param id o ID do formato a remover.
 	 */
 	public static void removeFormato(String id) {
 		removeObjecto("formatos", "ID_FOR", id);
 	}
 
 	/**
 	 * Remove da BD os formatos com um dado nome.
 	 * @param nome o nome do formato a remover.
 	 */
 	public static void removeFormatoNome(String nome) {
 		removeObjecto("formatos", "NOME_FORMATO", p(nome));
 	}
 
 	/* ----------------------------------------------------------------- */
 	/* ---------------------------- G�NEROS ---------------------------- */
 	/* ----------------------------------------------------------------- */
 	/**
 	 * Obt�m os g�neros de filme existentes.
 	 * @return Vector com os campos de cada g�nero.
 	 */
 	public static Vector<String[]> getGeneros() {
 		return selectAll("generos", false);
 	}
 	
 	public static Vector<String[]> getGenerosOrdNome() {
 		return selectAll("generos", "NOME_GENERO", false);
 	}
 	
 	/**
 	 * Obt�m os dados de um g�nero.
 	 * @param id o ID do g�nero.
 	 * @return os campos do g�nero.
 	 */
 	public static String[] getGenero(String id) {
 		Vector<String[]> vec = selectAll("generos", "ID_GEN", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	public static String getGeneroNome(String id) {
 		Vector<String[]> vec = selectAll("generos", "ID_GEN", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0)[1]);
 	}
 	
 	/**
 	 * Adiciona um g�nero � BD.
 	 * @param nome o nome do g�nero a adicionar.
 	 */
 	public static void adicionaGenero(String nome) {
 		adicionaObjecto("generos",
 						new String[]{"seq_genero_id.NEXTVAL", p(nome)});
 	}
 	
 	/**
 	 * Actualiza um g�nero na BD.
 	 * @param id o ID do g�nero a actualizar.
 	 * @param nome o novo nome para o g�nero a actualizar.
 	 */
 	public static void actualizaGenero(String id, String nome) {
 		actualizaObjecto("generos", "ID_GEN", id,
 						 getToSetCamposGeneros(),
 						 new String[]{p(nome)});
 	}
 
 	/**
 	 * Remove um g�nero da BD.
 	 * @param id o ID do g�nero a remover.
 	 */
 	public static void removeGenero(String id) {
 		removeObjecto("generos", "ID_GEN", id);
 	}
 
 	/**
 	 * Remove da BD os g�neros com um dado nome.
 	 * @param nome o nome do g�nero a remover.
 	 */
 	public static void removeGeneroNome(String nome) {
 		removeObjecto("generos", "NOME_GENERO", p(nome));
 	}
 
 	/* ---------------------------------------------------------------------- */
 	/* ---------------------------- M�QUINAS ATM ---------------------------- */
 	/* ---------------------------------------------------------------------- */
 	/**
 	 * Obt�m as m�quinas ATM existentes.
 	 * @return Vector com os campos de cada m�quina ATM.
 	 */
 	public static Vector<String[]> getMaquinasATM() {
 		return selectAll("maquinasatm", false);
 	}
 	
 	/**
 	 * Obt�m os dados de uma m�quina ATM.
 	 * @param id o ID da m�quina ATM.
 	 * @return os campos da m�quina ATM.
 	 */
 	public static String[] getMaquinaATM(String id) {
 		Vector<String[]> vec = selectAll("maquinasatm", "ID_MAQ", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona uma m�quina ATM � BD.
 	 * @param preco o pre�o da m�quina a adicionar.
 	 * @param data_instalacao a data de instala��o da m�quina a adicionar.
 	 */
 	public static void adicionaMaquinaATM(String preco) {
 		adicionaObjecto("maquinasatm",
 						new String[]{"seq_maquinaatm_id.NEXTVAL", preco, "1", "SYSDATE"});
 	}
 	
 	/**
 	 * Actualiza uma m�quina ATM na BD.
 	 * @param id o ID da m�quina a actualizar.
 	 * @param preco o novo pre�o da m�quina a actualizar.
 	 * @param data_instalacao a nova data de instala��o da m�quina a actualizar.
 	 */
 	public static void actualizaMaquinaATM(String id, String preco) {
 		actualizaObjecto("maquinasatm", "ID_MAQ", id,
 						 getToSetCamposMaquinasATM(),
 						 new String[]{preco});
 	}
 
 	/**
 	 * Invalida uma m�quina ATM na BD.
 	 * @param id o ID da m�quina ATM a invalidar.
 	 */
 	public static void invalidaMaquinaATM(String id) {
 		invalidaObjecto("maquinasatm", "ID_MAQ", id);
 	}
 	
 	/**
 	 * Re-valida uma m�quina ATM na BD.
 	 * @param id o ID da m�quina ATM a re-validar.
 	 */
 	public static void validaMaquinaATM(String id) {
 		invalidaObjecto("maquinasatm", "ID_MAQ", id);
 	}
 
 	/* -------------------------------------------------------------------- */
 	/* ---------------------------- PAGAMENTOS ---------------------------- */
 	/* -------------------------------------------------------------------- */
 	/**
 	 * Obt�m os pagamentos existentes.
 	 * @return Vector com os campos de cada pagamento.
 	 */
 	public static Vector<String[]> getPagamentos() {
 		return selectAll("pagamentos", false);
 	}
 	
 	/**
 	 * Obt�m os dados de um pagamento.
 	 * @param id_req o ID da requisi��o do pagamento.
 	 * @return os campos do pagamento.
 	 */
 	public static String[] getPagamento(String id_req) {
 		Vector<String[]> vec = selectAll("pagamentos", "ID_REQ", id_req, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	/**
 	 * Adiciona um pagamento � BD.
 	 * @param id_req o ID da requisi��o do pagamento a adicionar.
 	 * @param montante o montante do pagamento a adicionar.
 	 */
 	public static void adicionaPagamento(String id_req, String montante) {
 		adicionaObjecto("pagamentos",
 						new String[]{id_req, montante});
 	}
 	
 	/**
 	 * Actualiza um pagamento na BD.
 	 * @param id o ID da requisi��o do pagamento a actualizar.
 	 * @param montante o novo montante para o pagamento a actualizar.
 	 */
 	public static void actualizaPagamento(String id, String montante) {
 		actualizaObjecto("pagamentos", "ID_REQ", id,
 						 getToSetCamposPagamentos(),
 						 new String[]{montante});
 	}
 
 	/**
 	 * Remove um pagamento da BD.
 	 * @param id o ID da requisi��o do pagamento a remover.
 	 */
 	public static void removePagamento(String id) {
 		removeObjecto("pagamentos", "ID_REQ", id);
 	}
 	
 	/* --------------------------------------------------------------------- */
 	/* ---------------------------- REQUISI��ES ---------------------------- */
 	/* --------------------------------------------------------------------- */
 	/**
 	 * Obt�m as requisi��es existentes.
 	 * @return Vector com os campos de cada requisi��o.
 	 */
 	public static Vector<String[]> getRequisicoes() {
 		return selectAll("requisicoes", false);
 	}
 	
 	public static Vector<String[]> getRequisicoesCliente(String id) {
 		return selectAll("requisicoes", "ID_PES", id, false);
 	}
 	
 	public static Vector<String[]> getRequisicoesClientePlus(String id) {
 		return select("SELECT r.*, f.ano, f.titulo, fo.nome_formato" +
 					  " FROM requisicoes r, filmes f, formatos fo" +
 					  " WHERE r.ID_FIL = f.ID_FIL" +
 					    " AND r.ID_FOR = fo.ID_FOR" +
 						" AND r.ID_PES =" + id);
 	}
 	
 	public static Vector<String[]> getRequisicoesClienteBI(String bi) {
 		return selectAll("requisicoes", "BI", bi, false);
 	}
 	
 	public static Vector<String[]> getRequisicoesClienteBIPlus(String bi) {
 		return select("SELECT r.*, f.ano, f.titulo, fo.nome_formato" +
 					  " FROM requisicoes r, filmes f, formatos fo, clientes c" +
 					  " WHERE r.ID_FIL = f.ID_FIL" +
 					    " AND r.ID_FOR = fo.ID_FOR" +
 					    " AND r.ID_PES = c.ID_PES" +
 						" AND c.BI =" + bi);
 	}
 	
 	/**
 	 * Obt�m os dados de uma requisi��o.
 	 * @param id o ID da requisi��o.
 	 * @return os campos da requisi��o.
 	 */
 	public static String[] getRequisicao(String id) {
 		Vector<String[]> vec = selectAll("requisicoes", "ID_REQ", id, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona uma requisi��o � BD.
 	 */
 	public static void adicionaRequisicao(String id_maq, String emp_id_pes, String id_pes, String id_fil, String id_for) {
 		adicionaObjecto("requisicoes",
 						new String[]{id_maq, emp_id_pes, id_pes, id_fil, id_for, "SYSDATE", "SYSDATE + " + Consts.LIMITE_DIAS, "null"});
 	}
 
 	public static void adicionaRequisicaoNomeFormato(String id_maq, String emp_id_pes, String id_pes, String id_fil, String nome_formato) {
 		String id_for = getFormatoID(nome_formato);
 		adicionaObjecto("requisicoes",
 						new String[]{id_maq, emp_id_pes, id_pes, id_fil, id_for, "SYSDATE", "SYSDATE + " + Consts.LIMITE_DIAS, "null"});
 	}
 	
 	/**
 	 * Actualiza uma requisi��o na BD.
 	 * @param id o ID da requisi��o a actualizar.
 	 * @param data_limite a nova data limite de entrega do material da requisi��o.
 	 * @param data_entrega a data de entrega do material da requisi��o.
 	 */
 	public static void actualizaRequisicao(String id, String data_limite, String data_entrega) {
 		actualizaObjecto("requisicoes", "ID_REQ", id,
 						 getToSetCamposRequisicoes(),
 						 new String[]{p(data_limite), p(data_entrega)});
 	}
 	
 	/**
 	 * Actualiza uma requisi��o na BD com a data de entrega.
 	 * @param id o ID da requisi��o a actualizar.
 	 * @param data_entrega a data de entrega do material da requisi��o.
 	 */
 	public static void actualizaRequisicao(String id) {
 		actualizaObjecto("requisicoes", "ID_REQ", id, "DATA_ENTREGA", "SYSDATE");
 	}
 
 	/**
 	 * Remove uma requisi��o da BD.
 	 * @param id o ID da requisi��o a remover.
 	 */
 	public static void removeRequisicao(String id) {
 		removeObjecto("requisicoes", "ID_REQ", id);
 	}
 
 	public static String montanteActualRequisicao(String id) {
 		String res = select("SELECT montanteAPagar("+id+") FROM DUAL").get(0)[0];
 		return (res==null ? "0.00" : res);
 	}
 	
 	/* ---------------------------------------------------------------- */
 	/* ---------------------------- STOCKS ---------------------------- */
 	/* ---------------------------------------------------------------- */
 	/**
 	 * Obt�m os stocks existentes.
 	 * @return Vector com os campos de cada stock.
 	 */
 	public static Vector<String[]> getStocks() {
 		return selectAll("stocks", false);
 	}
 	
 	public static Vector<String[]> getStocksDeFilme(String id_fil) {
 		String query = "SELECT s.*, f.NOME_FORMATO" +
 					   " FROM stocks s, formatos f" +
 					   " WHERE s.ID_FOR = f.ID_FOR" +
 					     " AND ID_FIL = " + id_fil;
 		return select(query);
 	}
 	
 	/**
 	 * Obt�m os dados de um stock.
 	 * @param id_fil o ID do filme do stock.
 	 * @param id_for o ID do formato do stock.
 	 * @return os campos do stock.
 	 */
 	public static String[] getStock(String id_fil, String id_for) {
 		Vector<String[]> vec = selectAll("stocks",
 										 new String[]{"ID_FIL", "ID_FOR"},
 										 new String[]{id_fil, id_for}, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 	
 	public static String[] getStockNomeFormato(String id_fil, String nome_formato) {
 		String id_for = getFormatoID(nome_formato);
 		if(id_for == null)
 			return null;
 		Vector<String[]> vec = selectAll("stocks",
 										 new String[]{"ID_FIL", "ID_FOR"},
 										 new String[]{id_fil, id_for}, false);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0));
 	}
 
 	/**
 	 * Adiciona um stock � BD.
 	 * @param id_fil o ID do filme do stock a adicionar.
 	 * @param id_for o ID do formato do stock a adicionar.
 	 * @param quant a quantidade total de filmes existentes no stock.
 	 * @param custo_compra o custo de compra (� distribuidora) associado a um filme no stock.
 	 * @param custo_aluguer o custo de aluguer associado a um filme no stock.
 	 */
 	public static void adicionaStock(String id_fil, String id_for, String quant, String custo_compra, String custo_aluguer) {
 		adicionaObjecto("stocks",
 						new String[]{id_fil, id_for, quant, quant, custo_compra, custo_aluguer});
 	}
 	
 	public static void adicionaStockNomeFormato(String id_fil, String nome_formato, String quant, String custo_compra, String custo_aluguer) {
 		String id_for = getFormatoID(nome_formato);
 		adicionaObjecto("stocks",
 						new String[]{id_fil, id_for, quant, quant, custo_compra, custo_aluguer});
 	}
 	
 	/**
 	 * Actualiza um stock na BD.
 	 * @param id_fil o ID do filme do stock a actualizar.
 	 * @param id_for o ID do formato do stock a actualizar.
 	 * @param quant a nova quantidade total de filmes existentes no stock.
 	 * @param custo_compra o novo custo de compra (� distribuidora) associado a um filme no stock.
 	 * @param custo_aluguer o novo custo de aluguer associado a um filme no stock.
 	 */
 	public static void actualizaStock(String id_fil, String id_for, String quant, String custo_compra, String custo_aluguer) {
 		actualizaObjecto("stocks",
 						 new String[]{"ID_FIL", "ID_FOR"},
 						 new String[]{id_fil, id_for},
 						 getToSetCamposStocks(),
 						 new String[]{quant, custo_compra, custo_aluguer});
 	}
 	
 	public static void actualizaStockNomeFormato(String id_fil, String nome_formato, String quant, String custo_compra, String custo_aluguer) {
 		String id_for = getFormatoID(nome_formato);
 		actualizaObjecto("stocks",
 						 new String[]{"ID_FIL", "ID_FOR"},
 						 new String[]{id_fil, id_for},
 						 getToSetCamposStocks(),
 						 new String[]{quant, custo_compra, custo_aluguer});
 	}
 	
 	public static void actualizaQuantStock(String id_fil, String id_for, String quant) {
 		String comando = "UPDATE stocks SET quant = " + quant +
 						 " WHERE ID_FIL = " + id_fil + " AND ID_FOR = " + id_for;
 		execute(comando);
 	}
 	
 	public static void actualizaQuantStockIncr(String id_fil, String id_for, String incr) {
 		String comando = "UPDATE stocks SET quant = quant + " + incr +
 						 " WHERE ID_FIL = " + id_fil + " AND ID_FOR = " + id_for;
 		execute(comando);
 	}
 	
 	/**
 	 * Actualiza o n�mero de filmes dispon�veis num stock da BD.
 	 * @param id_fil o ID do filme do stock a actualizar.
 	 * @param id_for o ID do formato do stock a actualizar.
 	 * @param incr o incremento (ou decremento) a aplicar ao n�mero de filmes dispon�veis em stock.
 	 */
 	public static void actualizaDisponiveisStock(String id_fil, String id_for, String incr) {
 		String comando = "UPDATE stocks SET disponiveis = disponiveis + " + incr +
 						 " WHERE ID_FIL = " + id_fil + " AND ID_FOR = " + id_for;
 		execute(comando);
 	}
 
 	/**
 	 * Remove um stock da BD.
 	 * @param id_fil o ID do filme do stock a remover.
 	 * @param id_for o ID do formato do stock a remover.
 	 */
 	public static void removeStock(String id_fil, String id_for) {
 		removeObjecto("stocks",
 					  new String[]{"ID_FIL", "IF_FOR"},
 					  new String[]{id_fil, id_for});
 	}
 	
 	public static void removeStockNomeFormato(String id_fil, String nome_formato) {
 		String id_for = getFormatoID(nome_formato);
 		removeStock(id_fil, id_for);
 	}
 	
 	/* -------------------------------------------------------------------------------- */
 	/* ---------------------------- M�TODOS DE VERIFICA��O ---------------------------- */
 	/* -------------------------------------------------------------------------------- */
 	/**
 	 * Verifica se existe um cliente com determinado BI, para al�m do cliente com o ID passado.
 	 * @param id_cli o ID do cliente a excluir da verifica��o.
 	 * @param bi o BI a procurar.
 	 * @return true, se existe outro cliente com esse BI. false, caso contr�rio.
 	 */
 	public static boolean biClienteExiste(String id_cli, String bi) {
 		return valorExiste("clientes", "BI", bi, "ID_PES", id_cli, true);
 	}
 	
 	public static boolean biClienteExiste(String bi) {
 		return valorExiste("clientes", "BI", bi, true);
 	}
 	
 	/**
 	 * Verifica se existe um empregado com determinado BI, para al�m do empregado com o ID passado.
 	 * @param id_emp o ID do empregado a excluir da verifica��o.
 	 * @param bi o BI a procurar.
 	 * @return true, se existe outro empregado com esse BI. false, caso contr�rio.
 	 */
 	public static boolean biEmpregadoExiste(String id_emp, String bi) {
 		return valorExiste("empregados", "BI", bi, "ID_PES", id_emp, true);
 	}
 	
 	public static boolean biEmpregadoExiste(String bi) {
 		return valorExiste("empregados", "BI", bi, true);
 	}
 	
 	
 	public static boolean empregadoEAdmin(String id_emp) {
 		String query = "SELECT IS_ADMIN" +
 					   " FROM empregados" +
 					   " WHERE ID_PES = " + id_emp;
 		Vector<String[]> vec = select(query);
 		return (vec==null||vec.isEmpty() ? false : vec.get(0)[0].equals("1"));
 	}
 	
 	public static boolean empregadoEAdminBI(String bi) {
 		String query = "SELECT IS_ADMIN" +
 					   " FROM empregados" +
 					   " WHERE BI = " + bi;
 		Vector<String[]> vec = select(query);
 		return (vec==null||vec.isEmpty() ? null : vec.get(0)[0].equals("1"));
 	}
 	
 	/**
 	 * Vertifica se o empregado � administrador e � o �nico.
 	 * @param id_emp o ID do empregado a verificar.
 	 * @return true, se o empregado � o �nico administrador. false, caso contr�rio.
 	 */
 	public static boolean empregadoEUnicoAdmin(String id_emp) {
 		return !valorExiste("empregados", "IS_ADMIN", "1", "ID_PES", id_emp, true);
 	}
 	
 	public static boolean empregadoEUnicoAdminBI(String bi) {
 		return !valorExiste("empregados", "IS_ADMIN", "1", "BI", bi, true);
 	}
 	
 	/**
 	 * Verifica se determinado nome de g�nero existe, exclu�ndo o g�nero com o ID passado
 	 * (compara��o n�o olha a diferen�as de mai�sculas/min�sculas).
 	 * @param id_gen o ID do g�nero a excluir da verifica��o.
 	 * @param nome o nome do g�nero cuja exist�ncia tem de ser verificada.
 	 * @return true, se existe outro g�nero com esse nome.  false, caso contr�rio.
 	 */
 	public static boolean generoExiste(String id_gen, String nome) {
 		return valorExiste("generos", "NOME_GENERO", p(nome), "ID_GEN", id_gen, false);
 	}
 	
 	/**
 	 * Verifica se determinado nome de formato existe, exclu�ndo o formato com o ID passado
 	 * (compara��o n�o olha a diferen�as de mai�sculas/min�sculas).
 	 * @param id_for o ID do formato a excluir da verifica��o.
 	 * @param nome o nome do formato cuja exist�ncia tem de ser verificada.
 	 * @return true, se existe outro formato com esse nome. false, caso contr�rio.
 	 */
 	public static boolean formatoExiste(String id_for, String nome) {
 		return valorExiste("formatos", "NOME_FORMATO", p(nome), "ID_FOR", id_for, false);
 	}
 
 	/**
 	 * Verifica se determinado stock para um certo filme e formato existe.
 	 * @param id_fil o ID do filme a verificar stock.
 	 * @param id_for o ID do formato a verificar stock.
 	 * @return true, se existe stock para o filme e formato referidos. false, caso contr�rio.
 	 */
 	public static boolean stockExiste(String id_fil, String id_for) {
 		return valorExiste("stocks",
 						   new String[]{"ID_FIL", "ID_FOR"},
 						   new String[]{id_fil, id_for},
 						   false);
 	}
 	
 	public static boolean stockExisteNomeFormato(String id_fil, String nome_formato) {
 		/*String query = "SELECT s.ID_FIL, s.ID_FOR" +
 					   " FROM stocks s, formatos f" +
 					   " WHERE s.ID_FOR = f.ID_FOR" +
 					   " AND s.ID_FIL = " + id_fil +
 					   " AND f.NOME_FORMATO = " + nome_formato;
 		Vector<String[]> vec = select(query);
 		return (vec.size() > 0);*/
 		String id_for = getFormatoID(nome_formato);
 		return stockExiste(id_fil, id_for);
 	}
 
 	/**
 	 * Verifica se existe algum stock para um certo formato (para qualquer filme).
 	 * Por outras palavras, verifica se o formato est� a ser usado para algo.
 	 * @param id_for o ID do formato a verificar.
 	 * @return true, se existe pelo menos um stock para o formato. false, caso contr�rio.
 	 */
 	public static boolean formatoEmUso(String id_for) {
 		return valorExiste("stocks", "ID_FOR", id_for, false);
 	}
 	
 	public static boolean formatoEmUsoNome(String nome) {
 		Vector<String[]> vec = select("formatos", new String[]{"ID_FOR"}, "NOME_FORMATO", p(nome), false);
 		if(vec == null || vec.isEmpty())
 			return false;
 		return valorExiste("stocks", "ID_FOR", vec.get(0)[0], false);
 	}
 	
 	/**
 	 * Verifica se existe algum filme com um certo g�nero.
 	 * Por outras palavras, verifica se o g�nero est� a ser usado para algo.
 	 * @param id_gen o ID do g�nero a verificar.
 	 * @return true, se existe pelo menos um filme com esse g�nero. false, caso contr�rio.
 	 */
 	public static boolean generoEmUso(String id_gen) {
 		return valorExiste("filme_genero", "ID_GEN", id_gen, false);
 	}
 	
 	public static boolean generoEmUsoNome(String nome) {
 		Vector<String[]> vec = select("generos", new String[]{"ID_GEN"}, "NOME_GENERO", nome, false);
 		if(vec == null || vec.isEmpty())
 			return false;
 		return valorExiste("filme_genero", "ID_GEN", vec.get(0)[0], false);
 	}
 	
 	/**
 	 * Verifica se um filme tem apenas determinado g�nero e mais nenhum.
 	 * @param id_fil o ID do filme a verificar.
 	 * @param id_gen o ID do g�nero a verificar.
 	 * @return true, se o filme tem apenas esse g�nero e mais nenhum. false, caso contr�rio.
 	 */
 	public static boolean filmeSoTemGenero(String id_fil, String id_gen) {
 		Vector<String[]> vec = getGenerosFilme(id_fil);
 		if(vec == null)
 			return true;
 		return (vec.size() == 1 && vec.get(0).equals(id_gen));
 	}
 	
 	public static boolean loginClienteCorrecto(String id_pes, String password) {
 		return valorExiste("clientes",
 						   new String[]{"ID_PES", "PASSWORD"},
 						   new String[]{id_pes, p(password)},
 						   true);
 	}
 	
 	public static boolean loginClienteCorrectoBI(String bi, String password) {
 		return valorExiste("clientes",
 						   new String[]{"BI", "PASSWORD"},
 						   new String[]{bi, p(password)},
 						   true);
 	}
 	
 	public static boolean loginEmpregadoCorrecto(String id_pes, String password) {
 		return valorExiste("empregados",
 						   new String[]{"ID_PES", "PASSWORD"},
 						   new String[]{id_pes, p(password)},
 						   true);
 	}
 	
 	public static boolean loginEmpregadoCorrectoBI(String bi, String password) {
 		return valorExiste("empregados",
 						   new String[]{"BI", "PASSWORD"},
 						   new String[]{bi, p(password)},
 						   true);
 	}
 	
 	/**
 	 * Verifica se determinado valor existe em determinado campo numa dada tabela (fun��o gen�rica).
 	 * @param tabela a tabela a verificar.
 	 * @param campo o campo cujo valor verificar.
 	 * @param valor o valor a encontrar no campo.
 	 * @return true, se o valor foi encontrado no campo referido de algum elemento. false, caso contr�rio.
 	 */
 	private static boolean valorExiste(String tabela, String campo, String valor, boolean soValidos) {
 		Vector<String[]> vec = selectAll(tabela, campo, valor, soValidos);
 		return (vec.size() > 0);
 	}
 	
 	/**
 	 * Verifica se determinados valores existem (em simult�neo) em v�rios campos de uma dada tabela (fun��o gen�rica).
 	 * @param tabela a tabela a verificar.
 	 * @param campos os campos cujo valor verificar.
 	 * @param valores os valores a encontrar nos campos.
 	 * @return true, se os valores existem em simult�neo nos campos referidos de algum elemento. false, caso contr�rio.
 	 */
 	private static boolean valorExiste(String tabela, String[] campos, String[] valores, boolean soValidos) {
 		Vector<String[]> vec = selectAll(tabela, campos, valores, soValidos);
 		return (vec.size() > 0);
 	}
 
 	/**
 	 * Verifica se determinado valor existe em determinado campo numa dada tabela,
 	 * exlu�ndo elementos com determinado valor em determinado campo (fun��o gen�rica).
 	 * @param tabela a tabela a verificar.
 	 * @param campo o campo cujo valor verificar.
 	 * @param valor o valor a encontrar no campo.
 	 * @param exceptCampo o campo a verificar para excluir elementos da procura.
 	 * @param exceptValor o valor que o campo de exclus�o deve ter para excluir elementos da procura.
 	 * @return true, se o valor foi encontrado no campo referido de algum elemento (exclu�ndo os devidos). false, caso contr�rio.
 	 */
 	private static boolean valorExiste(String tabela, String campo, String valor, String exceptCampo, String exceptValor, boolean soValidos) {
 		Vector<String[]> vec = select("SELECT *" +
 									  " FROM " + tabela +
 									  " WHERE " + campo + "=" + valor +
 									  " AND " + exceptCampo + "!=" + exceptValor +
 									  (!soValidos ? "":" AND VALIDO = 1"));
 		return (vec.size() > 0);
 	}
 	
 	/* --------------------------------------------------------------------------- */
 	/* ---------------------------- M�TODOS GEN�RICOS ---------------------------- */
 	/* --------------------------------------------------------------------------- */
 	/**
 	 * Adiciona um objecto � BD. Fun��o gen�rica.
 	 * @param tabela a tabela � qual adicionar o objecto.
 	 * @param valores os valores do objecto a adicionar.
 	 */
 	private static void adicionaObjecto(String tabela, String[] valores) {
 		execute("INSERT INTO " + tabela +
 				" VALUES(" + Utils.list(valores, ",") + ")");
 	}
 	
 	/**
 	 * Actualiza um objecto na BD. Fun��o gen�rica.
 	 * @param tabela a tabela na qual actualizar o objecto.
 	 * @param campo o campo utilizado para encontrar o objecto a actualizar.
 	 * @param valor o valor que deve ter o campo no objecto a actualizar.
 	 * @param campoAct o campo a actualizar no objecto.
 	 * @param valorAct o valor do campo a actualizar.
 	 */
 	private static void actualizaObjecto(String tabela, String campo, String valor, String campoAct, String valorAct) {
 		execute("UPDATE " + tabela +
 				" SET " + campoAct + "=" + valorAct +
 				" WHERE " + campo + "=" + valor);
 	}
 	
 	/**
 	 * Actualiza um objecto na BD. Fun��o gen�rica.
 	 * @param tabela a tabela na qual actualizar o objecto.
 	 * @param campo o campo utilizado para encontrar o objecto a actualizar.
 	 * @param valor o valor que deve ter o campo no objecto a actualizar.
 	 * @param camposAct os campos a actualizar no objecto.
 	 * @param valoresAct os valores dos campos a actualizar.
 	 */
 	private static void actualizaObjecto(String tabela, String campo, String valor, String[] camposAct, String[] valoresAct) {
 		execute("UPDATE " + tabela +
 				" SET " + Utils.list(camposAct, "=", valoresAct, ",") +
 				" WHERE " + campo + "=" + valor);
 	}
 	
 	/**
 	 * Actualiza um objecto na BD. Fun��o gen�rica.
 	 * @param tabela a tabela na qual actualizar o objecto.
 	 * @param campos os campos utilizados para encontrar o objecto a actualizar.
 	 * @param valores os valores que devem ter os campos no objecto a actualizar.
 	 * @param camposAct os campos a actualizar no objecto.
 	 * @param valoresAct os valores dos campos a actualizar.
 	 */
 	private static void actualizaObjecto(String tabela, String[] campos, String[] valores, String[] camposAct, String[] valoresAct) {
 		execute("UPDATE " + tabela +
 			   " SET " + Utils.list(camposAct, "=", valoresAct, ",") +
 			   " WHERE " + Utils.list(campos, "=", valores, " AND "));
 	}
 	
 	/**
 	 * Invalida um objecto na BD. Fun��o gen�rica.
 	 * @param tabela a tabela na qual invalidar o objecto.
 	 * @param campo o campo utilizado para encontrar o objecto a invalidar.
 	 * @param valor o valor que deve ter o campo no objecto a invalidar.
 	 */
 	private static void invalidaObjecto(String tabela, String campo, String valor) {
 		actualizaObjecto(tabela, campo, valor, "valido", "0");
 	}
 	
 	/**
 	 * Valida um objecto na BD. Fun��o gen�rica.
 	 * @param tabela a tabela na qual validar o objecto.
 	 * @param campo o campo utilizado para encontrar o objecto a validar.
 	 * @param valor o valor que deve ter o campo no objecto a validar.
 	 */
 	private static void validaObjecto(String tabela, String campo, String valor) {
 		actualizaObjecto(tabela, campo, valor, "valido", "1");
 	}
 	
 	/**
 	 * Remove os objectos da BD que tenham determinado valor num determinado campo. Fun��o gen�rica.
 	 * @param tabela a tabela da qual remover o(s) objecto(s).
 	 * @param campo o campo a verificar para encontrar o(s) objecto(s) a remover.
 	 * @param valor o valor do campo no(s) objecto(s) a ser removido(s).
 	 */
 	private static void removeObjecto(String tabela, String campo, String valor) {
 		execute("DELETE FROM " + tabela +
 				" WHERE " + campo + " = " + valor);
 	}
 	
 	/**
 	 * Remove os objectos da BD que tenham determinados valores em determinados campos. Fun��o gen�rica.
 	 * @param tabela a tabela da qual remover o(s) objecto(s).
 	 * @param campos os campos a verificar para encontrar o(s) objecto(s) a remover.
 	 * @param valores os valores do campos no(s) objecto(s) a ser removido(s).
 	 */
 	private static void removeObjecto(String tabela, String[] campos, String[] valores) {
 		execute("DELETE FROM " + tabela +
 				" WHERE " + Utils.list(campos, "=", valores, " AND "));
 	}
 
 	/**
 	 * Obt�m todos campos de todos os objectos existentes numa dada tabela.
 	 * @param tabela tabela de onde obter os dados.
 	 * @return Vector com os campos de cada linha da tabela.
 	 */
 	private static Vector<String[]> selectAll(String tabela, boolean soValidos) {
 		return select("SELECT *" +
 					  " FROM " + tabela +
 					  (!soValidos ? "":" WHERE VALIDO = 1"));
 	}
 	
 	private static Vector<String[]> selectAll(String tabela, String campoOrder, boolean soValidos) {
 		return select("SELECT *" +
 					  " FROM " + tabela +
 					  (!soValidos ? "":" WHERE VALIDO = 1") +
 					  (" ORDER BY " + campoOrder));
 	}
 	
 	/**
 	 * Obt�m todos os campos do objecto existente numa dada tabela que contiver
 	 * certo valor num determinado campo.
 	 * @param tabela tabela de onde obter os dados.
 	 * @param campo o campo que seleccionar� o objecto.
 	 * @param valor o valor a procurar no campo.
 	 * @return Vector com todos os campos dos objectos.
 	 */
 	private static Vector<String[]> selectAll(String tabela, String campo, String valor, boolean soValidos) {
 		return select("SELECT *" +
 					  " FROM " + tabela +
 					  " WHERE " + campo + "=" + valor +
 					  (!soValidos ? "":" AND VALIDO = 1"));
 	}
 	
 	private static Vector<String[]> selectAll(String tabela, String[] campos, String[] valores, boolean soValidos) {
 		return select("SELECT *" +
 					  " FROM " + tabela +
 					  " WHERE " + Utils.list(campos, "=", valores, " AND ") +
 					  (!soValidos ? "":" AND VALIDO = 1"));
 	}
 	
 	/**
 	 * Obt�m os campos seleccionados dos objectos existentes numa dada tabela que contiverem
 	 * certo valor num determinado campo.
 	 * @param tabela tabela onde fazer SELECT.
 	 * @param campos nomes das colunas a obter da tabela.
 	 * @param campo o campo que seleccionar� o objecto.
 	 * @param valor o valor a procurar no campo.
 	 * @return Vector com os campos seleccionados dos objectos.
 	 */
 	private static Vector<String[]> select(String tabela, String[] camposSel, String campo, String valor, boolean soValidos) {
 		return select("SELECT " + Utils.list(camposSel, ",") +
 					  " FROM " + tabela +
 					  " WHERE " + campo + "=" + valor +
 					  (!soValidos ? "":" AND VALIDO = 1"));
 	}
 	
 	private static Vector<String[]> select(String query) {
 		Vector<String[]> objectos = new Vector<String[]>();
 		String[] objecto;
 		try {
 			Statement st = conn.createStatement();
 			ResultSet rset = st.executeQuery(query);
 			ResultSetMetaData rsmd = rset.getMetaData();
 			int n = rsmd.getColumnCount();
 
 			while (rset.next()) {
 				objecto = new String[n];
 				for (int i = 0; i < n; i++)
 					objecto[i] = rset.getString(i+1);
 				objectos.add(objecto);
 			}
 			st.close();
 		} catch (SQLException e) {
 			Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, e);
 		}
 		return objectos;
 	}
 
 	/**
 	 * Actualiza a BD com o comando passado por argumento.
 	 * @param comando o comando a executar sobre a BD.
 	 */
 	private static void execute(String comando) {
 		try {
 			Statement st = conn.createStatement();
 			st.executeUpdate(comando);
 			conn.commit();
 		} catch (SQLException ex) {
 			Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	private static void executeNoCommit(String comando) {
 		try {
 			Statement st = conn.createStatement();
 			st.executeUpdate(comando);
 		} catch (SQLException ex) {
 			Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 
 
 	/**
          *
          * @param comando
          */
 	private static void executeNoCommitEst(String comando) {
 		try {
 			Statement st = conn.createStatement();
 			st.execute(comando);
 		} catch (SQLException ex) {
 			Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	/*private static void executeFunction(String funcao, String[] parametros) {
 		String query = funcao + "(" + Utils.list(parametros, ",") + ")";
 		Statement st = conn.createStatement();
 		st.execute(query);
 		ResultSet rs = st.getResultSet();
 		
 		//PreparedStatement ps = conn.prepareStatement(query);
 		//ResultSet rs = ps.executeQuery(); 
 		rs.next();
 		return rs.getString(1);
 	}*/
 
     /* ---------------------------------------------------------------- */
 	/* ------------------------- ESTAT�STICAS ------------------------- */
 	/* ---------------------------------------------------------------- */
 
         public static String estatisticasContabilidade(){
             try {
                 
                 CallableStatement cs = conn.prepareCall ("{ call contabilidade()}");
                 cs.execute();
                 Vector <String []> out=select("SELECT col1, col2, col4 FROM temp");
                 conn.commit();
                 
                 return "LUCRO: "+out.get(0)[0]+
                         "€\n DESPESAS "+out.get(0)[1]+
                         "€\n TOTALFACTURADO "+out.get(0)[2]+"€";
             } catch (SQLException ex) {
                 Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
                
             }
             return "";
         }
 
         public static String estatisticasTop10Clientes(){
             try {
                
                 //executeNoCommitEst("EXECUTE top10clientes");
                 
                 CallableStatement cs = conn.prepareCall ("{ call top10clientes()}");
                 cs.execute();
                 Vector <String []> out=select("SELECT col1, col2, col3, col4 FROM temp");
                 conn.commit();
                 
                 
                 String output="TOP 10 Clientes\nID:\tBI:\tNOME:\tNº Requisições\n";
                 if(out!=null){
                     for(int i=0; i<out.size();i++){
                         output+=out.get(i)[0]+"\t"+out.get(i)[1]+"\t"+out.get(i)[2]+"\t"+out.get(i)[3]+"\n";
                     }
                     return output;
                 }
                 return "";
             } catch (SQLException ex) {
                 Utils.dbg("excepção no Commit?");
                 Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
                 
             }
             
             return "";
         }
 
         public static String estatisticasTopMaquinas(){
             try {
 
                 CallableStatement cs = conn.prepareCall ("{ call topMaquinas()}");
                 cs.execute();
                 Vector <String []> out=select("SELECT col1, col2 FROM temp");
                 conn.commit();
 
 
                 String output="TOP Maquinas\nID:\tNº Requisições\n";
                 if(out!=null){
                     for(int i=0; i<out.size();i++){
                         output+=out.get(i)[0]+"\t"+out.get(i)[1]+"\n";
                     }
                     return output;
                 }
                 return "";
             } catch (SQLException ex) {
                 Utils.dbg("excepção no Commit?");
                 Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
 
             }
 
             return "";
         }
 
         public static String estatisticasTop10Filmes(){
             try {
                 //executeNoCommit("EXECUTE top10filmes");
                 
                 CallableStatement cs = conn.prepareCall ("{ call top10filmes()}");
                 cs.execute();
                 Vector <String []> out=select("SELECT col1, col3, col4 FROM temp");
                 conn.commit();
                 
                 String output="TOP 10 filmes\nID:\tTITULO:\tNº Requisições\n";
                 if(out!=null){
                     for(int i=0; i<out.size();i++){
                         output+=out.get(i)[0]+"\t"+out.get(i)[1]+"\t"+out.get(i)[2]+"\n";
                     }
                     return output;
                 }
                 return "";
             } catch (SQLException ex) {
                 Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
             }
             
             return "";
         }
 
 
 
         public static String estatisticasEmpregados(){
             try {
                 CallableStatement cs = conn.prepareCall ("{ call empregadosInfo()}");
                 cs.execute();
 
                 Vector <String []> out=select("SELECT col1, col2, col4 FROM temp");
                 conn.commit();
                 String output="NºEMPREGADOS:\t% ADMINISTRADORES:\tDESPESAS C/ORDENADOS\n";
                 if(out!=null){
                     for(int i=0; i<out.size();i++){
                         output+=out.get(i)[1]+"\t"+out.get(i)[2]+"\t"+out.get(i)[0]+"\n";
                     }
                     return output;
                 }
                 return "";
             } catch (SQLException ex) {
                 Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
             }
             return "";
         }
 
 	/* ---------------------------------------------------------------- */
 	/* ---------------------------- OUTROS ---------------------------- */
 	/* ---------------------------------------------------------------- */
 	/**
 	 * Coloca plicas na string passada. Substitui todos os ap�strofes na palavra
 	 * por ap�strofes duplos para fazer "escape".
 	 * @param s a string � qual aplicar plicas.
 	 * @return a string, com plicas e os ap�strofes diplicados.
 	 */
 	private static String p(String s) {
 		return "'" + s.replace("'", "''") + "'";
 	}
 }
