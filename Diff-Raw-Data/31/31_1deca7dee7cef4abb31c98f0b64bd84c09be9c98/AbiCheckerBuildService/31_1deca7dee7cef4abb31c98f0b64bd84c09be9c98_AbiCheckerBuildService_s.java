 package ch.cern.dss.teamcity.agent;
 
 
 import ch.cern.dss.teamcity.common.AbiCheckerConstants;
 import jetbrains.buildServer.RunBuildException;
 import jetbrains.buildServer.agent.runner.BuildServiceAdapter;
 import jetbrains.buildServer.agent.runner.ProgramCommandLine;
 import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine;
 import jetbrains.buildServer.log.Loggers;
 import org.jetbrains.annotations.NotNull;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 import java.io.BufferedInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 public class AbiCheckerBuildService extends BuildServiceAdapter {
 
     @NotNull
     @Override
     public ProgramCommandLine makeProgramCommandLine() throws RunBuildException {
         final List<String> arguments = new ArrayList<String>();
         final Map<String, String> runnerParameters = getRunnerParameters();
         final Map<String, String> environment = new HashMap<String, String>(System.getenv());
         environment.putAll(getBuildParameters().getEnvironmentVariables());
 
         String executablePath = runnerParameters.get(AbiCheckerConstants.UI_ABI_CHECKER_EXECUTABLE_PATH);
         String referenceBuildType = runnerParameters.get(AbiCheckerConstants.UI_BUILD_TYPE);
         String referenceTag = runnerParameters.get(AbiCheckerConstants.UI_REFERENCE_TAG);
         String artifactPath = runnerParameters.get(AbiCheckerConstants.UI_ARTIFACT_FILES);
 
         String serverUrl = getAgentConfiguration().getServerUrl();
         String restUrl = serverUrl + "/guestAuth/app/rest/builds/buildType:" + referenceBuildType + ",tag:"
                 + referenceTag + ",personal:false,count:1,status:SUCCESS";
         Loggers.AGENT.info(">>>>>>> REST url: " + restUrl);
 
         SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
         SAXParser saxParser;
         BuildInfoXmlResponseHandler handler;
         String artifactDownloadUrl = "";
 
         try {
             saxParser = saxParserFactory.newSAXParser();
             handler = new BuildInfoXmlResponseHandler();
             saxParser.parse(new InputSource(new URL(restUrl).openStream()), handler);
             artifactDownloadUrl = serverUrl + handler.getArtifactDownloadUrl();
         } catch (ParserConfigurationException e) {
             Loggers.AGENT.error("Error configuring SAX parser: " + e.getMessage());
         } catch (SAXException e) {
             Loggers.AGENT.error("Error parsing XML: " + e.getMessage());
         } catch (MalformedURLException e) {
             Loggers.AGENT.error("Error retrieving XML: " + e.getMessage());
         } catch (IOException e) {
             Loggers.AGENT.error("Error: " + e.getMessage());
         }
 
         Loggers.AGENT.info("Artifact download URL: " + artifactDownloadUrl);
 
         String referenceArtifactFilename = getWorkingDirectory().getAbsolutePath() + "/artifacts-" + referenceTag;
         Loggers.AGENT.info("Artifacts will be d/led to: " + referenceArtifactFilename);
 
         try {
             saveUrl(referenceArtifactFilename, artifactDownloadUrl);
         } catch (IOException e) {
             Loggers.AGENT.error("Error downloading artifacts: " + e.getMessage());
         }
 
         // Tell TeamCity to publish artifacts now.
         System.out.println("##teamcity[publishArtifacts '" + artifactPath + "']");
 
         final SimpleProgramCommandLine commandLine = new SimpleProgramCommandLine(environment,
                getWorkingDirectory().getAbsolutePath(), "abi-compliance-checker", arguments);
         return commandLine;
     }
 
     public void saveUrl(String filename, String urlString) throws MalformedURLException, IOException {
         BufferedInputStream in = null;
         FileOutputStream out = null;
         try {
             in = new BufferedInputStream(new URL(urlString).openStream());
             out = new FileOutputStream(filename);
 
             byte data[] = new byte[1024];
             int count;
             while ((count = in.read(data, 0, 1024)) != -1) {
                 out.write(data, 0, count);
             }
         } finally {
             if (in != null)
                 in.close();
             if (out != null)
                 out.close();
         }
     }
 
 }
