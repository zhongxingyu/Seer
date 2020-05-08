 package uk.co.bubobubo.cuke.steps;
 
 import cucumber.api.java.en.When;
 import org.openrdf.query.Query;
 import org.openrdf.query.QueryLanguage;
 import org.openrdf.query.QueryResult;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.http.HTTPRepository;
 import uk.co.bubobubo.cuke.bean.RequestAttribute;
 import uk.co.bubobubo.cuke.utils.querystrategy.QueryStrategy;
 
 import java.util.List;
 
 import static uk.co.bubobubo.cuke.utils.querystrategy.QueryStrategyFactory.askQueryStrategy;
 import static uk.co.bubobubo.cuke.utils.querystrategy.QueryStrategyFactory.constructQueryStrategy;
 import static uk.co.bubobubo.cuke.utils.querystrategy.QueryStrategyFactory.selectQueryStrategy;
 
 public class OpenRDFStepDefs {
 
	private String repositoryBaseUri = "http://localhost:8080/openrdf-sesame/";
	private String username = "test@user.com";
	private String password = "password";
 
 	private boolean askResult;
 	private QueryResult queryResult;
 	private String resultAsString;
 
	@When("^I use open-rdf libs to SELECT from \"([^\"]*)\" with the parameters$")
 	public void I_use_open_rdf_libs_to_SELECT_from_with_the_parameters(String repositoryId, List<RequestAttribute> params)
 			throws Throwable {
 
 		doInSesame(repositoryId, params, selectQueryStrategy());
 	}
 
 	@When("^I use open-rdf libs to ASK from \"([^\"]*)\" with the parameters$")
 	public void I_use_open_rdf_libs_to_ASK_from_with_the_parameters(String repositoryId, List<RequestAttribute> params)
 			throws Throwable {
 
 		doInSesame(repositoryId, params, askQueryStrategy());
 	}
 
 	@When("^I use open-rdf libs to CONSTRUCT from \"([^\"]*)\" with the parameters$")
 	public void I_use_open_rdf_libs_to_CONSTRUCT_from_with_the_parameters(String repositoryId, List<RequestAttribute> params)
 			throws Throwable {
 
 		doInSesame(repositoryId, params, constructQueryStrategy());
 
 	}
 
 	private void doInSesame(String repositoryId, List<RequestAttribute> params, QueryStrategy strategy)
             throws Throwable {
 
 		String queryParameter = getParameter("query", params);
 		String acceptHeader = getHeader("accept", params);
         String language = getParameter("queryLn", params);
 
 		RepositoryConnection connection = connect(repositoryId);
 
         QueryLanguage queryLanguage = (language == null ? QueryLanguage.SPARQL : QueryLanguage.valueOf(language));
 		Query query = strategy.prepareQuery(connection, queryLanguage, queryParameter);
 
 		if(acceptHeader == null) {
 			// use default format
 			queryResult = strategy.evaluateQuery(query);
 		} else {
 			// set result format accordingly
 			resultAsString = strategy.queryResultAsString(query, acceptHeader);
 		}
 	}
 
 	private RepositoryConnection connect(final String repositoryId) throws Exception {
 		HTTPRepository repository = new HTTPRepository(repositoryBaseUri, repositoryId);
 		repository.setUsernameAndPassword(username, password);
 		return repository.getConnection();
 	}
 
 	private String getParameter(String name, List<RequestAttribute> params) {
 		return extractParam(name, "parameter", params);
 	}
 
 	private String getHeader(String name, List<RequestAttribute> params) {
 		return extractParam(name, "header", params);
 	}
 
 	private String extractParam(String name, String type, List<RequestAttribute> params) {
 		for(RequestAttribute requestAttribute : params) {
 			if(requestAttribute.getType().equalsIgnoreCase(type)) {
 				if(requestAttribute.getName().equalsIgnoreCase(name)) {
 					return requestAttribute.getValue();
 				}
 			}
 		}
 		return null;
 	}
 
 	public void setRepositoryBaseUri(String repositoryBaseUri) {
 		this.repositoryBaseUri = repositoryBaseUri;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 }
 
