 package pl.edu.agh.mabics;
 
 import org.springframework.beans.factory.BeanFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import pl.edu.agh.mabics.agents.AbstractAgent;
 import pl.edu.agh.mabics.agents.AgentFactory;
 
 import javax.swing.*;
 
 public class Main {
     public static void main(String[] args) {
         ApplicationContext context = new ClassPathXmlApplicationContext("spring-config.xml");
         BeanFactory factory = context;
         AgentFactory agentFactory = (AgentFactory) factory.getBean("agentFactory");
         AbstractAgent agent1 = agentFactory.createAgent();
         AbstractAgent agent2 = agentFactory.createAgent();
         AbstractAgent agent3 = agentFactory.createAgent();
         agentFactory.finishAgentCreation();
 
         JFrame frame = new JFrame("ExperimentsGUI");
         frame.setContentPane(new ExperimentsGUI().Main);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setVisible(true);
 
//        CommandLineHelper commandLineHelper = (CommandLineHelper) factory.getBean("commandLineHelper");
//        String[] commands = {"cd ..\\trunk\\src\\runner\\", "start python runner.py -c config2 -v True"};
//        commandLineHelper.runCommand(commands);
     }
 }
