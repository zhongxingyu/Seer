 package at.jku.busem.diff.core.helper;
 
 import at.jku.busem.diff.results.*;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import java.io.File;
 import java.util.*;
 
 public class ResultWriter {
 
     public void createResultXML(ResultPool resultPool) {
         JAXBContext jc;
         try {
             jc = JAXBContext.newInstance(Results.class);
             Marshaller marshaller = jc.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             File xml = new File("files/result.xml");
 
             //bring map keys and values to ResultRule structure
             //1-get all rule names
             Set<String> distinctRules = new HashSet<>();
             for (Map.Entry<String, List<Result>> entry : resultPool.getResultMap().entrySet()) {
                 distinctRules.add(entry.getKey());
             }
             for (Map.Entry<String, List<ResultGroup>> entry : resultPool.getResultGroupMap().entrySet()) {
                 distinctRules.add(entry.getKey());
             }
 
             //2-get all results and resultGroups for each rule
            List<ResultRule> resultRules = new ArrayList<>();
             ResultRule resultRule = null;
             Iterator<String> it = distinctRules.iterator();
             while (it.hasNext()) {
                 String ruleName = it.next();
                 resultRule = new ResultRule();
                 resultRule.setName(ruleName);
                 resultRule.setResults(resultPool.getResults(ruleName));
                 resultRule.setResultGroups(resultPool.getResultGroups(ruleName));
                 resultRules.add(resultRule);
             }
 
             //do marshalling
             Results resultsRoot = new Results();
             resultsRoot.setResultRules(resultRules);
             marshaller.marshal(resultsRoot, xml);
         } catch (JAXBException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
 }
