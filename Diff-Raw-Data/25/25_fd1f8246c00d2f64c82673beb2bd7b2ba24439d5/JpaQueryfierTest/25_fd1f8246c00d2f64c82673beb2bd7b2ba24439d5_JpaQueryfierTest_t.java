 package org.jpaqueryfier;
 
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 
 import static org.fest.assertions.api.Assertions.assertThat;
 
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.doReturn;
 
 public class JpaQueryfierTest {
 
 	@Mock
 	EntityManager em;
 
 	@Mock
 	Query query;
 
 	@Before
 	public void init() {
 		MockitoAnnotations.initMocks(this);
 	}
 
 	@Test
 	public void shouldReturnAQueryObjectWithNoParameters() {
 		String sql = "SELECT * FROM table";
 		doReturn(query).when(em).createQuery(sql);
 
 		assertThat(new JpaQueryfier(sql, em).queryfy()).isNotNull();
 	}
 
 	@Test
 	public void shouldExtractNoParametersFromSQLWithoutParameters() {
 		String sql = "SELECT * FROM table";
 
 		assertThat(new JpaQueryfier(sql).getParameters()).isEmpty();
 	}
 
 	@Test
 	public void shouldExtractParametersFromSQLWithParameters() {
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 
 		assertThat(new JpaQueryfier(sql).getParameters()).isNotEmpty();
 	}
 
 	@Test
	public void shouldExtractParametersFromJPAQLWithParameters() {
		String sql = "SELECT t FROM table t WHERE t.column = :column AND t.column2 = :column2";

		assertThat(new JpaQueryfier(sql).getParameters()).isNotEmpty();
	}

	@Test
 	public void shouldAppendParametersIntoQuery() {
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		List<QueryParameter> parameters = new JpaQueryfier(sql).with("123").and("456").getParameters();
 
 		assertThat(parameters).isNotEmpty();
 		assertThat(parameters).hasSize(2);
 		assertThat(parameters.get(0)).isEqualsToByComparingFields(new QueryParameter("column", "123"));
 		assertThat(parameters.get(1)).isEqualsToByComparingFields(new QueryParameter("column2", "456"));
 	}
 
 	@Test
 	public void shouldAppendBetweenParametersIntoQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE name = :name AND age BETWEEN :minAge AND :maxAge";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("Paul").and(18).and(40);
 		List<QueryParameter> parameters = queryfier.getParameters();
 		queryfier.queryfy();
 
 		assertThat(parameters).isNotEmpty();
 		assertThat(parameters).hasSize(3);
 		assertThat(parameters.get(0)).isEqualsToByComparingFields(new QueryParameter("name", "Paul"));
 		assertThat(parameters.get(1)).isEqualsToByComparingFields(new QueryParameter("minAge", 18));
 		assertThat(parameters.get(2)).isEqualsToByComparingFields(new QueryParameter("maxAge", 40));
 	}
 
 	@Test
 	public void shouldNotAppendMoreThanOneParameterWithSameName() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column";
 
 		QueryParameter queryParameter = new QueryParameter("column", null, true);
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with(queryParameter).and(queryParameter);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE column = :column");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 		assertThat(queryfier.getParameters()).hasSize(1);
 	}
 
 	@Test
 	public void shouldNotRemoveParametersFromSqlQueryWhenAllParametersAreNullAndIsSpecifiedToAllowNulls() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).allowingNulls().with(null).and(null);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE column = :column AND column2 = :column2");
 	}
 
 	@Test
 	public void shouldRemoveAllParametersFromSqlQueryWhenAllParametersAreNull() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table");
 	}
 
 	@Test
	public void shouldRemoveAllParametersFromJpaQLQueryWhenAllParametersAreNull() {
		doReturn(query).when(em).createQuery(anyString());
		String sql = "SELECT t FROM table t WHERE t.column = :column AND t.column2 = :column2";
		JpaQueryfier queryfier = new JpaQueryfier(sql, em);
		queryfier.queryfy();

		assertThat(queryfier.getSql()).isEqualTo("SELECT t FROM table t");
	}

	@Test
 	public void shouldRemoveNullParametersBetweenParametersIntoQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE name=:name AND age BETWEEN :minAge AND :maxAge";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("Paul");
 		List<QueryParameter> parameters = queryfier.getParameters();
 		queryfier.queryfy();
 
 		assertThat(parameters).isNotEmpty();
 		assertThat(parameters).hasSize(3);
 		assertThat(parameters.get(0)).isEqualsToByComparingFields(new QueryParameter("name", "Paul"));
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE name=:name");
 	}
 
 	@Test
 	public void shouldRemoveEmptyBetweenClause() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE age BETWEEN :minAge AND :maxAge";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em);
 		List<QueryParameter> parameters = queryfier.getParameters();
 		queryfier.queryfy();
 
 		assertThat(parameters).isNotEmpty();
 		assertThat(parameters).hasSize(2);
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table");
 	}
 
 	@Test
 	public void shouldRemoveNullParametersBetweenUsingLessThanAndGreaterThanParametersIntoQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE name = :name AND age >= :minAge AND age <= :maxAge";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("Paul");
 		List<QueryParameter> parameters = queryfier.getParameters();
 		queryfier.queryfy();
 
 		assertThat(parameters).isNotEmpty();
 		assertThat(parameters).hasSize(3);
 		assertThat(parameters.get(0)).isEqualsToByComparingFields(new QueryParameter("name", "Paul"));
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE name = :name");
 	}
 
 	@Test
 	public void shouldRemoveAllNullParametersFromSqlQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with(null).and(null);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 	}
 
 	@Test
 	public void shouldRemoveSecondNullParameterFromSqlQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("test");
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE column = :column");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 		assertThat(queryfier.getParameters()).hasSize(2);
 		assertThat(queryfier.getParameters().get(0)).isEqualsToByComparingFields(new QueryParameter("column", "test"));
 	}
 
 	@Test
 	public void shouldRemoveFirstNullParameterFromSqlQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column = :column AND column2 = :column2";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with(null).and("test");
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table  WHERE column2 = :column2");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 		assertThat(queryfier.getParameters()).hasSize(2);
 		assertThat(queryfier.getParameters().get(1)).isEqualsToByComparingFields(new QueryParameter("column2", "test"));
 	}
 
 	@Test
 	public void shouldAppendNullableParameterIntoSqlQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column IS :column";
 
 		QueryParameter queryParameter = new QueryParameter("column", null, true);
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with(queryParameter);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table WHERE column IS :column");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 		assertThat(queryfier.getParameters()).hasSize(1);
 		assertThat(queryfier.getParameters().get(0)).isEqualsToByComparingFields(queryParameter);
 	}
 
 	@Test
 	public void shouldRemoveNotNullableParameterIntoSqlQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT * FROM table WHERE column IS :column";
 
 		QueryParameter queryParameter = new QueryParameter("column", null);
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with(queryParameter);
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo("SELECT * FROM table");
 		assertThat(queryfier.getParameters()).isNotEmpty();
 		assertThat(queryfier.getParameters()).hasSize(1);
 		assertThat(queryfier.getParameters().get(0)).isEqualsToByComparingFields(queryParameter);
 	}
 
 	@Test
 	public void shouldAppendParametersIntoRealQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT ua_cd,replace(ua_nm,'RFB','') FROM uas_srf,"
 				+ "  (SELECT ygua_ua_cd, max(ygua_dt_max) dt_max FROM v_gerencial_uas "
 				+ "	 WHERE ygua_ua_cd BETWEEN :idUaInicio AND :idUaFim AND ygua_tafi_cd = :idAtividade "
 				+ "	 GROUP BY ygua_ua_cd) " + "WHERE ua_cd = ygua_ua_cd (+) "
 				+ "  AND ua_cd BETWEEN :idUaInicio AND :idUaFim "
 				+ "  AND (ua_nm_mnemonico IN ('DRF','SRRF','DEINF','DEAIN','DEMAC','DEFIS'))"
 				+ "  AND (ua_dt_extincao IS NULL OR :dataInicial <= dt_max) AND ua_b_in_delecao_logica = 'N' "
 				+ "ORDER BY 1";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("1010000").and("1019999").and(1L)
 				.and(Calendar.getInstance());
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isEqualTo(sql);
 	}
 
 	@Test
 	public void shouldRemoveNullParameterFromRealQuery() {
 		doReturn(query).when(em).createQuery(anyString());
 		String sql = "SELECT ua_cd,replace(ua_nm,'RFB','') FROM uas_srf,"
 				+ "  (SELECT ygua_ua_cd, max(ygua_dt_max) dt_max FROM v_gerencial_uas "
 				+ "	 WHERE ygua_ua_cd BETWEEN :idUaInicio AND :idUaFim AND ygua_tafi_cd = :idAtividade "
 				+ "	 GROUP BY ygua_ua_cd) " + "WHERE ua_cd = ygua_ua_cd (+) "
 				+ "  AND ua_cd BETWEEN :idUaInicio AND :idUaFim "
 				+ "  AND (ua_nm_mnemonico IN ('DRF','SRRF','DEINF','DEAIN','DEMAC','DEFIS'))"
 				+ "  AND (ua_dt_extincao IS NULL OR :dataInicial <= dt_max) AND ua_b_in_delecao_logica = 'N' "
 				+ "ORDER BY 1";
 		JpaQueryfier queryfier = new JpaQueryfier(sql, em).with("1010000").and("1019999").and(null)
 				.and(Calendar.getInstance());
 		queryfier.queryfy();
 
 		assertThat(queryfier.getSql()).isNotEqualTo(sql);
 	}
 
 	@Test
 	public void shouldReturnANativeQueryObjectWithNoParameters() {
 		String sql = "SELECT * FROM table";
 		doReturn(query).when(em).createNativeQuery(sql);
 
 		assertThat(new JpaQueryfier(sql, em).queryfyNative()).isNotNull();
 	}
 
 }
