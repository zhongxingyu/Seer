 package org.bonitasoft.connectors.sap;
 
import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bonitasoft.connectors.sap.bos5connector.SAPCallFunctionConnector;
 import org.bonitasoft.connectors.sap.bos5connector.SAPConstants;
 import org.bonitasoft.engine.connector.Connector;
 import org.bonitasoft.engine.connector.ConnectorException;
 import org.bonitasoft.engine.connector.ConnectorValidationException;
 
 public class SapCallFunction implements Connector {
     public static final String SERVER_TYPE = "serverType";
     public static final String CLIENT = "client";
     public static final String USER = "user";
     public static final String PASSWORD = "password";
     public static final String LANGUAGE = "language";
     public static final String HOST = "host";
     public static final String SYSTEM_NUMBER = "systemNumber";
     public static final String SYSTEM_ID = "systemId";
     public static final String GROUP_NAME = "groupName";
     public static final String REPOSITORY = "repository";
     public static final String FUNCTION_NAME = "functionName";
     public static final String COMMIT_ON_SUCCESS = "commitOnSuccess";
     public static final String ROLLBACK_ON_FAILURE = "rollbackOnFailure";
     public static final String INPUT_PARAMETERS = "inputParameters";
     public static final String OUTPUT_PARAMETERS = "outputParameters";
     public static final String HTML_OUTPUT = "htmlOutput";
     public static final String OUTPUT_RESULTS = "outputResults";
 
     SAPCallFunctionConnector bos5Connector = new SAPCallFunctionConnector();
 
 
     @Override
     public void setInputParameters(Map<String, Object> stringObjectMap) {
         String serverType = (String) stringObjectMap.get(SERVER_TYPE);
         String client = (String) stringObjectMap.get(CLIENT);
         String user = (String) stringObjectMap.get(USER);
         String password = (String) stringObjectMap.get(PASSWORD);
         String language = (String) stringObjectMap.get(LANGUAGE);
         String host = (String) stringObjectMap.get(HOST);
         String systemNumber = (String) stringObjectMap.get(SYSTEM_NUMBER);
         String systemId = (String) stringObjectMap.get(SYSTEM_ID);
         String groupName = (String) stringObjectMap.get(GROUP_NAME);
         String repository = (String) stringObjectMap.get(REPOSITORY);
         String functionName = (String) stringObjectMap.get(FUNCTION_NAME);
         Boolean commitOnSuccess = (Boolean) stringObjectMap.get(COMMIT_ON_SUCCESS);
         Boolean rollbackOnFailure = (Boolean) stringObjectMap.get(ROLLBACK_ON_FAILURE);
         List<List<Object>> inputParameters = (List<List<Object>>) stringObjectMap.get(INPUT_PARAMETERS);
         List<List<String>> outputParameters = (List<List<String>>) stringObjectMap.get(OUTPUT_PARAMETERS);
         String htmlOutput = (String) stringObjectMap.get(HTML_OUTPUT);
 
         bos5Connector.setServerType(serverType);
         bos5Connector.setClient(client);
         bos5Connector.setUser(user);
         bos5Connector.setPassword(password);
         if (language == null || language.isEmpty()) {
             language = "EN";
         }
         bos5Connector.setLanguage(language);
         bos5Connector.setHost(host);
         bos5Connector.setSystemNumber(systemNumber);
         bos5Connector.setSystemId(systemId);
         bos5Connector.setGroupName(groupName);
         if (repository == null || repository.isEmpty()) {
             repository = SAPConstants.DEFAULT_REPOSITORY_NAME;
         }
         bos5Connector.setRepository(repository);
         bos5Connector.setFunctionName(functionName);
         bos5Connector.setCommitOnSuccess(commitOnSuccess);
         bos5Connector.setRollbackOnFailure(rollbackOnFailure);
         bos5Connector.setReleaseClient(true);
         bos5Connector.setInputParameters(inputParameters);
         bos5Connector.setOutputParameters(outputParameters);
         bos5Connector.setHtmlOutput(htmlOutput);
         bos5Connector.setUseExitingConnection(false);
     }
 
     @Override
     public void validateInputParameters() throws ConnectorValidationException {
         List<String> errors = bos5Connector.validateValues();
 
         if (!errors.isEmpty()) {
             throw new ConnectorValidationException(this, errors);
         }
     }
 
     @Override
     public Map<String, Object> execute() throws ConnectorException {
         Map<String, Object> results = new HashMap<String, Object>();
         try {
             bos5Connector.executeConnector();
             results.put(OUTPUT_RESULTS, bos5Connector.getResults());
         } catch (Exception e) {
             throw new ConnectorException(e);
         }
        return Collections.emptyMap();
     }
 
     @Override
     public void connect() throws ConnectorException {
 
     }
 
     @Override
     public void disconnect() throws ConnectorException {
 
     }
 
 }
