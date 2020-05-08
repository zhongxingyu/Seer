 package pl.edu.agh.mabics.agents;
 
 import org.springframework.beans.BeansException;
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.beans.factory.BeanFactoryAware;
 import org.springframework.stereotype.Service;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Mateusz
  * Date: 09.12.12
  * Time: 17:20
  */
 @Service
 public class AgentFactory implements BeanFactoryAware {
 
     private BeanFactory beanFactory;
     private Integer portCounter = 8000;
     private static final String CONFIG_FILE_NAME = "..\\trunk\\src\\runner\\config3";
     private BufferedWriter confFileOut;
 
     public AgentFactory() {
         try {
             FileWriter fstream = new FileWriter(CONFIG_FILE_NAME);
             this.confFileOut = new BufferedWriter(fstream);
             this.confFileOut.write("../board/test3.bmp");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
         this.beanFactory = beanFactory;
     }
 
     public AbstractAgent createAgent() {
         portCounter++;
         AbstractAgent newAgent = (AbstractAgent) beanFactory.getBean("randomAgent");
         newAgent.startAgent(portCounter);
         try {
            confFileOut.write("\n" + portCounter.toString() + " " + "(2,0)" + "[(5,1)]" + " localhost:" + portCounter.toString());
         } catch (IOException e) {
             e.printStackTrace();
         }
         return newAgent;
     }
 
     public void finishAgentCreation() {
         try {
             confFileOut.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
 }
